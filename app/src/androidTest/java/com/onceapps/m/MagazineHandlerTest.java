package com.onceapps.m;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;

import com.onceapps.core.util.Logger;
import com.onceapps.core.util.SerializationUtils;
import com.onceapps.m.api.MagazineHandler;
import com.onceapps.m.api.MagazineHandler_;
import com.onceapps.m.api.RestClient;
import com.onceapps.m.api.RestClient_;
import com.onceapps.m.models.Brand;
import com.onceapps.m.models.BrandList;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.models.MagazineList;
import com.onceapps.m.models.MagazineStatus;
import com.onceapps.m.models.MagazineStatuses;
import com.onceapps.m.utils.Preferences;
import com.onceapps.m.utils.Preferences_;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

import java.io.File;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class MagazineHandlerTest extends ApplicationTestCase<Application> {
    public MagazineHandlerTest() {
        super(Application.class);
    }

    private MagazineHandler mMagazineHandler;
    private RestClient mRestClient;
    private Preferences mPreferences;
    private static final String TEST_FILE_10MB = "http://cachefly.cachefly.net/10mb.test";

    private static final String TEST_GRANT_TYPE = "client_credentials";
    private static final String TEST_CLIENT_ID = "secret";
    private static final String TEST_CLIENT_SECRET = "secret";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mRestClient = RestClient_.getInstance_(getContext());
        mMagazineHandler = MagazineHandler_.getInstance_(getContext());
        mPreferences = Preferences_.getInstance_(getContext());
    }

    @LargeTest
    public void testDownloadMagazine() throws Exception {

        Magazine magazine = new Magazine();
        magazine.setId(42);
        magazine.setPackageHash("f1c9645dbc14efddc7d8a322685f26eb");
        magazine.setPackageUrl(TEST_FILE_10MB);
        magazine.setPackageSize(10485760L);

        if (mMagazineHandler.getStatus(magazine) != null) {
            mMagazineHandler.cancel(magazine);
        }

        mMagazineHandler.download(magazine);

        long timeout = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);

        Assert.assertNotNull(mMagazineHandler.getStatus(magazine));
        while (mMagazineHandler.getStatus(magazine).getPercent() < 100) {
            MagazineStatus status = mMagazineHandler.getStatus(magazine);
            System.out.println(String.format(Locale.getDefault(), "%s percent: %d", status.getMagazine(), status.getPercent()));
            if (System.currentTimeMillis() > timeout) {
                throw new TimeoutException();
            }
            Thread.sleep(500);
        }
    }

    @LargeTest
    public void testDownloadMagazineContinue() throws Exception {

        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        // todo check current active network is wifi

        Magazine magazine = new Magazine();
        magazine.setId(42);
        magazine.setPackageHash("f1c9645dbc14efddc7d8a322685f26eb");
        magazine.setPackageUrl(TEST_FILE_10MB);
        magazine.setPackageSize(10485760L);

        if (mMagazineHandler.getStatus(magazine) != null) {
            mMagazineHandler.cancel(magazine);
        }

        mMagazineHandler.download(magazine);

        long timeout = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(4);

        Assert.assertNotNull(mMagazineHandler.getStatus(magazine));
        while (mMagazineHandler.getStatus(magazine).getPercent() < 100) {
            MagazineStatus status = mMagazineHandler.getStatus(magazine);
            System.out.println(String.format(Locale.getDefault(), "%s percent: %d", status.getMagazine(), status.getPercent()));
            if (System.currentTimeMillis() > timeout) {
                throw new TimeoutException();
            }
            if (status.getPercent() > 2) {
                break;
            }
            Thread.sleep(50);
        }

        wifiManager.setWifiEnabled(false);
        Thread.sleep(1000);
        wifiManager.setWifiEnabled(true);

        while (mMagazineHandler.getStatus(magazine).getPercent() < 100) {
            MagazineStatus status = mMagazineHandler.getStatus(magazine);
            System.out.println(String.format(Locale.getDefault(), "%s percent: %d", status.getMagazine(), status.getPercent()));
            if (System.currentTimeMillis() > timeout) {
                throw new TimeoutException();
            }
            Thread.sleep(1000);
        }
    }

    @SmallTest
    public void testMagazineStatusesClass() throws Exception {
        final MagazineStatuses original = new MagazineStatuses();
        int size = new Random().nextInt(1000) + 1;

        for (int i = 0; i < size; i++) {
            Magazine magazine = new Magazine();
            magazine.setId(i);
            magazine.setName("magazine " + i);
            original.add(new MagazineStatus(magazine, 0, MagazineStatus.DownloadStatus.NOT_DOWNLOADED));
        }

        // test de/serialize
        String serialized = SerializationUtils.serializeToJson(original);
        MagazineStatuses fromJson = SerializationUtils.deserializeJson(MagazineStatuses.class, serialized);
        Assert.assertNotNull(fromJson);
        Assert.assertEquals(fromJson.size(), original.size());

        // test clean
        original.clear();
        Assert.assertEquals(original.size(), 0);

        // test add
        original.addAll(fromJson);
        Assert.assertEquals(fromJson.size(), original.size());
    }

    /*
    @LargeTest
    public void testZipPerformance() throws Exception {
        File zip = new File("/storage/sdcard0/test200m.zip");

        long start = System.currentTimeMillis();
        ZipFile zipFile = new ZipFile(zip);
        if (zipFile.isEncrypted()) {
            zipFile.setPassword("engedjbe");
        }
        zipFile.extractAll(mContext.getDir("decompress", Context.MODE_PRIVATE).getAbsolutePath());
        System.out.println("all = " + (System.currentTimeMillis() - start));
    }
    */

    public void testMagazineDownload() throws Exception {
        RestClientTest.checkLoggedIn(mPreferences, mRestClient);
        BrandList brandList = mRestClient.getBrands();

        test:
        for (Brand brand : brandList) {
            MagazineList magazineList = mRestClient.getMagazines(brand);
            for (Magazine magazine : magazineList) {
                if (!TextUtils.isEmpty(magazine.getPackageUrl()) && magazine.getPackageSize() > 0) {
                    if (mMagazineHandler.getStatus(magazine) != null) {
                        mMagazineHandler.cancel(magazine);
                    }
                    mMagazineHandler.download(magazine);

                    long timeout = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(4);

                    // waiting for download
                    while (mMagazineHandler.getStatus(magazine).getPercent() < 100) {
                        if (System.currentTimeMillis() > timeout) {
                            throw new TimeoutException();
                        }
                        Logger.info("percent: " + mMagazineHandler.getStatus(magazine).getPercent());
                        Thread.sleep(500);
                    }

                    Assert.assertTrue(mMagazineHandler.getMagazineDir(magazine).exists());
                    Assert.assertTrue(mMagazineHandler.getMagazineDir(magazine).isDirectory());
                    File magazineDirectory = mMagazineHandler.getMagazineDir(magazine);
                    Assert.assertTrue(FileUtils.sizeOfDirectory(magazineDirectory) > 100);
                    Assert.assertTrue(mMagazineHandler.getMagazinePage(magazine, 0).exists());
                    Assert.assertTrue(new File(magazineDirectory, String.format(Locale.getDefault(), "%d/1.html", magazine.getId())).exists());
                    Assert.assertTrue(new File(magazineDirectory, String.format(Locale.getDefault(), "%d/assets", magazine.getId())).exists());

                    break test;
                }
            }
            Assert.fail("no suitable magazine found");
        }

    }
}