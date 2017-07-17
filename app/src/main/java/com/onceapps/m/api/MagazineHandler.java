package com.onceapps.m.api;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.onceapps.core.util.Broadcast;
import com.onceapps.core.util.HTTP;
import com.onceapps.core.util.HttpProgressListener;
import com.onceapps.core.util.HttpResponse;
import com.onceapps.core.util.HttpUtils;
import com.onceapps.core.util.Logger;
import com.onceapps.core.util.SerializationUtils;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.models.MagazineStatus;
import com.onceapps.m.models.MagazineStatuses;
import com.onceapps.m.models.MediaStatus;
import com.onceapps.m.ui.PageRequest;
import com.onceapps.m.utils.Preferences;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeBackground;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by balage on 19/04/16.
 * download helper for magazine content
 */
@EBean(scope = EBean.Scope.Singleton)
public class MagazineHandler {

    private File mStorageDirectory;

    private static final String DIRECTORY = ".magazines";
    private static final String REGISTRY = "magazines.json";
    private static final String MAGAZINE_JSON = "magazine_%d.json";

    private static final String PDF_FILE_NAME = "/magazin.pdf";

    @RootContext
    protected Context mContext;

    @Bean
    protected Preferences mPreferences;

    @Bean
    protected HttpUtils mHttpUtils;

    @Bean
    protected RestClient mRestClient;

    private volatile boolean mIsInitialized = false;

    private volatile boolean mMagazineRegistryChanged = false;

    private final MagazineStatuses mMagazineRegistry = new MagazineStatuses();

    private final Lock mLock = new ReentrantLock();

    @AfterInject
    protected void onStart() {
        MediaStatus status = validateStorage();
        if (status == MediaStatus.Available) {
            init();
        } else {
            Broadcast.postEvent(status);
        }
    }

    private void init() {
        try {
            if (mPreferences.app.useInternalStorage().getOr(false)) {
                mStorageDirectory = new File(mContext.getFilesDir(), DIRECTORY);
            } else {
                mStorageDirectory = new File(mContext.getExternalFilesDir(null), DIRECTORY);
            }
            if (!mStorageDirectory.exists()) {
                FileUtils.forceMkdir(mStorageDirectory);
                FileUtils.writeStringToFile(new File(mStorageDirectory, ".nomedia"), ".");
            }
            mIsInitialized = true;

            // todo continue pending downloads

            // this may throws a file not found exception, which is normal
            mMagazineRegistry.addAll(SerializationUtils.deserializeJson(MagazineStatuses.class, mContext.openFileInput(REGISTRY)));
        } catch (FileNotFoundException fileNotFoundException) {
            Logger.debug(fileNotFoundException, "no register file found");
        } catch (Exception e) {
            Logger.error(e, "magazine handler init failed");
        } finally {
            checkRegistryChanged();
            processPendingMagazines();
        }
    }

    @Subscribe
    public void onMediaChanged(MediaStatus mediaStatus) {
        switch (mediaStatus) {
            case Mounted:
            case Unmounted:
                mIsInitialized = false;
                Broadcast.postEvent(validateStorage());
                break;
            case Available:
                init();
                break;
        }
    }

    private MediaStatus validateStorage() {
        if (mPreferences.app.useInternalStorage().getOr(false)) {
            return MediaStatus.Available;
        }
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return MediaStatus.Available;
        } else {
            return MediaStatus.Unavailable;
        }
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public MagazineStatus getStatus(@NonNull Magazine magazine) {
        return mMagazineRegistry.getByMagazine(magazine);
    }

    /**
     * alias for {@link #delete(Magazine)}
     * stop downloading and delete all contents for magazine
     *
     * @param magazine the magazine
     */
    public void cancel(@NonNull Magazine magazine) {
        mMagazineRegistry.replace(new MagazineStatus.Builder(getStatus(magazine))
                .setDownloadStatus(MagazineStatus.DownloadStatus.DOWNLOAD_CANCELLED)
                .build());
        delete(magazine);
    }

    /**
     * to delete all content for magazine
     *
     * @param magazine the magazine
     */
    public void delete(@NonNull Magazine magazine) {
        if (!mIsInitialized) throw new IllegalStateException("magazine handler not initialized");

        MagazineStatus status = getStatus(magazine);
        if (status == null) return;

        // deleting storage directory
        FileUtils.deleteQuietly(magazineDlFile(magazine));
        FileUtils.deleteQuietly(getMagazineDir(magazine));

        // removing from local registry
        mMagazineRegistry.remove(status);
        mMagazineRegistryChanged = true;
    }

    /**
     * start the download process for magazine
     *
     * @param magazine the magazine
     */
    public MagazineStatus download(@NonNull Magazine magazine) throws GeneralSecurityException {
        if (!mIsInitialized) {
            throw new IllegalStateException("magazine handler not initialized");
        }
        checkPermission();
        if (getStatus(magazine) != null) {
            throw new IllegalStateException("magazine %d already added to download queue");
        }

        MagazineStatus status = new MagazineStatus.Builder()
                .setMagazine(magazine)
                .setPercent(0)
                .setDownloadStatus(MagazineStatus.DownloadStatus.DOWNLOAD_IN_PROGRESS)
                .build();

        mMagazineRegistry.add(status);
        mMagazineRegistryChanged = true;

        return status;
    }

    public MagazineStatus.DownloadStatus getDownloadStatus(@NonNull Magazine magazine) {
        if (!mIsInitialized) {
            throw new IllegalStateException("magazine handler not initialized");
        }

        MagazineStatus magazineStatus = mMagazineRegistry.getByMagazine(magazine);

        return magazineStatus == null
                ? MagazineStatus.DownloadStatus.NOT_DOWNLOADED
                : magazineStatus.getDownloadStatus();

    }

    @Background(delay = 2000)
    public void processPendingMagazines() {
        if (!mIsInitialized) return;
        if (mLock.tryLock()) {
            try {
                List<MagazineStatus> pendingMagazines = new ArrayList<>();
                for (final MagazineStatus magazineStatus : mMagazineRegistry) {
                    if (magazineStatus.getPercent() < 100)
                        pendingMagazines.add(magazineStatus);
                }

                for (final MagazineStatus magazineStatus : pendingMagazines) {

                    try {
                        // always update magazine before downloading (maybe hash, password or other info changed)
                        mRestClient.clearCacheForNextRequestOnThisThread();
                        final Magazine magazine = mRestClient.getMagazine(magazineStatus.getMagazine().getId());

                        mRestClient.clearCacheForNextRequestOnThisThread();
                        mRestClient.getMagazineContents(magazine);

                        startUpdateUI(magazine);
                        checkPermission();
                        if (magazineStatus.getPercent() < 100) {
                            // check if continue or start
                            final File dlFile = magazineDlFile(magazine);
                            Map<String, String> httpHeaders = new HashMap<>();
                            if (dlFile.exists()) {
                                // continue
                                httpHeaders.put(HTTP.RANGE, String.format(Locale.getDefault(), "bytes=%d-", dlFile.length()));
                            }

                            // start download
                            HttpResponse response = mHttpUtils.doHttpRequest(HttpUtils.Method.Get,
                                    new URL(magazine.getPackageUrl()),
                                    null, // input stream for request body, empty for http.get request
                                    httpHeaders,
                                    new HttpProgressListener() {

                                        private final long _startSize = dlFile.length();
                                        private final Magazine _magazine = magazine;

                                        @Override
                                        public void onConnected() {

                                        }

                                        @Override
                                        public void onProgress(int dataSize, int downloadedDataSize) {
                                            MagazineStatus status = mMagazineRegistry.getByMagazine(_magazine);
                                            if (status != null) {
                                                int percent = Math.min(99, (int) ((_startSize + downloadedDataSize) / (_magazine.getPackageSize() / 100f)));
                                                mMagazineRegistry.replace(new MagazineStatus.Builder(status)
                                                        .setPercent(percent)
                                                        .setDownloadStatus(MagazineStatus.DownloadStatus.DOWNLOAD_IN_PROGRESS)
                                                        .build());
                                            } else {
                                                throw new IllegalStateException(String.format("magazine %s download cancelled?", _magazine));
                                            }
                                        }
                                    },
                                    new FileOutputStream(dlFile, true)
                            );

                            File doneFile = magazineDlFile(magazine);
                            if (doneFile.length() == magazine.getPackageSize()) {
                                // after download finished
                                String doneFileMd5Hex = new String(Hex.encodeHex(DigestUtils.md5(new FileInputStream(doneFile))));
                                if (!doneFileMd5Hex.equals(magazine.getPackageHash())) {
                                    Logger.error("file hash invalid for magazine %s. hash=%s api hash=%s", magazine, doneFileMd5Hex, magazine.getPackageHash());
                                    FileUtils.deleteQuietly(doneFile);
                                    mMagazineRegistry.replace(new MagazineStatus.Builder(magazineStatus)
                                            .setPercent(0)
                                            .setDownloadStatus(MagazineStatus.DownloadStatus.DOWNLOAD_FAILED)
                                            .build());
                                } else {

                                    // hash valid
                                    // todo password zip, etc.
                                    Logger.info("hash valid for magazine: %s", magazine);

                                    // if (zipFile.isEncrypted()) {
                                    //  zipFile.setPassword("whatever");
                                    // }
                                    File dstDirectory = getMagazineDir(magazine);
                                    if (dstDirectory.exists()) {
                                        FileUtils.deleteQuietly(dstDirectory);
                                    }
                                    FileUtils.forceMkdir(dstDirectory);

                                    long startTime = System.currentTimeMillis();
                                    Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
                                    archiver.extract(doneFile, dstDirectory);
                                    long finishTime = System.currentTimeMillis();
                                    Logger.debug(String.format(Locale.getDefault(), "archive extract in %d ms", (finishTime - startTime)));

                                    mMagazineRegistry.replace(new MagazineStatus.Builder(magazineStatus)
                                            .setPercent(100)
                                            .setDownloadStatus(MagazineStatus.DownloadStatus.DOWNLOAD_FINISHED)
                                            .build());

                                    // save original magazine json to magazine folder to check generated date etc. later
                                    FileUtils.writeStringToFile(
                                            new File(mStorageDirectory, String.format(Locale.getDefault(), MAGAZINE_JSON, magazine.getId())),
                                            SerializationUtils.serializeToJson(magazine)
                                    );
                                    FileUtils.deleteQuietly(doneFile);
                                    mMagazineRegistryChanged = true;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Logger.info(e, "magazine download failed");
                        mMagazineRegistry.replace(new MagazineStatus.Builder(magazineStatus)
                                .setPercent(0)
                                .setDownloadStatus(MagazineStatus.DownloadStatus.DOWNLOAD_FAILED)
                                .build());
                    }
                }
            } finally {
                mLock.unlock();
            }
        } else {
            // if lock already taken
            return;
        }

        if (mIsInitialized) {
            processPendingMagazines();
        }
    }

    public File getMagazineDir(@NonNull Magazine magazine) {
        return new File(mStorageDirectory, "magazine_" + magazine.getId());
    }

    /**
     * get magazine content page
     *
     * @param magazine   magazine instance
     * @param pageNumber number of the page, starts from zero
     * @return File instance or null, if there is no page with that index
     */
    public File getMagazinePage(@NonNull Magazine magazine, int pageNumber) throws GeneralSecurityException, FileNotFoundException {
        checkPermission();
        if (getMagazineDir(magazine).listFiles().length == 0) {
            throw new FileNotFoundException();
        }
        String path = getMagazineDir(magazine).listFiles()[0].getAbsolutePath();
        return new File(path, String.format(Locale.getDefault(), "/%d.html", pageNumber));
    }

    public File getMagazinePdf(@NonNull Magazine magazine) throws GeneralSecurityException, FileNotFoundException {
        checkPermission();
        if (getMagazineDir(magazine).listFiles().length == 0) {
            throw new FileNotFoundException();
        }
        String path = getMagazineDir(magazine).listFiles()[0].getAbsolutePath();
        return new File(path, PDF_FILE_NAME);
    }

    @SupposeBackground
    public Magazine getDownloadedMagazineInfo(Magazine magazine) throws Exception {
        return SerializationUtils.deserializeJson(
                Magazine.class,
                new FileInputStream(new File(mStorageDirectory, String.format(Locale.getDefault(), MAGAZINE_JSON, magazine.getId())))
        );
    }

    public int getNumberOfPages(@NonNull Magazine magazine) throws GeneralSecurityException, FileNotFoundException {
        int pages = 0;
        while (getMagazinePage(magazine, pages).exists()) {
            pages++;
        }
        return pages;
    }

    private File magazineDlFile(@NonNull Magazine magazine) {
        return new File(mStorageDirectory, magazine.getId().toString() + "___");
    }

    private void registryChanged() {
        try {
            IOUtils.write(SerializationUtils.serializeToJson(mMagazineRegistry), mContext.openFileOutput(REGISTRY, Context.MODE_PRIVATE));
            mMagazineRegistryChanged = false;
        } catch (Exception e) {
            Logger.warn(e, "save registry to internal disk failed");
        }
    }

    @Background(delay = 1000)
    protected void checkRegistryChanged() {
        if (!mIsInitialized) return;
        synchronized (mMagazineRegistry) {
            if (mMagazineRegistryChanged) {
                registryChanged();
            }
        }
        if (mIsInitialized) {
            checkRegistryChanged();
        }
    }

    protected void startUpdateUI(final Magazine magazine) {
        final Timer uiNotifyTimer = new Timer();
        uiNotifyTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Broadcast.postEvent(getStatus(magazine));
                if (getDownloadStatus(magazine) == MagazineStatus.DownloadStatus.DOWNLOAD_FINISHED
                        || getDownloadStatus(magazine) == MagazineStatus.DownloadStatus.DOWNLOAD_CANCELLED
                        || getDownloadStatus(magazine) == MagazineStatus.DownloadStatus.DOWNLOAD_FAILED) {
                    uiNotifyTimer.cancel();
                }
            }
        }, 0, 500);
    }

    public MagazineStatuses getMagazineRegistry() {
        return mMagazineRegistry;
    }

    private boolean checkPermission() throws GeneralSecurityException {
        if (PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Broadcast.postEvent(new PageRequest(PageRequest.Page.StoragePermissionDenied));
            Logger.warn("permission denied: %s", android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            throw new GeneralSecurityException(String.format("permission denied: %s", android.Manifest.permission.WRITE_EXTERNAL_STORAGE));
        }
        return true;
    }

    @SupposeBackground
    public boolean isUpdateAvailable(@NonNull Magazine magazine) {
        try {
            // if magazine downloaded, but has different remote version, ask to download
            return getDownloadStatus(magazine) == MagazineStatus.DownloadStatus.DOWNLOAD_FINISHED
                    && getDownloadedMagazineInfo(magazine).getGeneratedAt().getTime() != magazine.getGeneratedAt().getTime();

        } catch (Exception e) {
            Logger.debug(e, "error checking magazine versions");
        }
        return false;
    }
}
