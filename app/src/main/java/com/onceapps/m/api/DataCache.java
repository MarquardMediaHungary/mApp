package com.onceapps.m.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.onceapps.core.util.Logger;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeBackground;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by balage on 19/04/16.
 * offline request cache
 */
@EBean(scope = EBean.Scope.Singleton)
public class DataCache {

    private volatile File mWorkingDirectory;

    @RootContext
    protected Context mContext;

    @AfterInject
    public void init() {
        mWorkingDirectory = new File(mContext.getCacheDir(), "request-cache");
        try {
            if (!mWorkingDirectory.exists()) {
                FileUtils.forceMkdir(mWorkingDirectory);
            } else {
                cleanup();
            }
        } catch (Exception e) {
            Logger.error(e, "error init request cache");
        }
    }

    @Background
    protected void cleanup() {
        try {
            for (File file : mWorkingDirectory.listFiles()) {
                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
                long expires = dataInputStream.readLong();
                if (expires < System.currentTimeMillis()) {
                    Logger.debug("deleting expired cache file: %s", file.getAbsolutePath());
                    FileUtils.deleteQuietly(file);
                } else {
                    Logger.debug("cache file valid: %s", file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            Logger.warn(e, "data cache cleanup failed");
        }
    }

    @SupposeBackground
    public InputStream get(@NonNull String url, @Nullable Map<String, String> headers) throws Exception {
        File cacheFile = new File(mWorkingDirectory, calculateHash(url, headers));
        if (!cacheFile.exists() || cacheFile.length() < 8) return null;
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(cacheFile));
        long expires = dataInputStream.readLong();
        if (expires < System.currentTimeMillis()) {
            FileUtils.deleteQuietly(cacheFile);
            return null;
        }
        return dataInputStream;
    }

    @SupposeBackground
    public InputStream set(@NonNull String url, @Nullable Map<String, String> headers, @NonNull InputStream content, long expiresSec) throws Exception {
        File cacheFile = new File(mWorkingDirectory, calculateHash(url, headers));
        if (cacheFile.exists()) FileUtils.deleteQuietly(cacheFile);
        if (!mWorkingDirectory.exists() || !mWorkingDirectory.canWrite()) {
            Logger.error("cache directory: %s, exists: %b, writable:%b",
                    mWorkingDirectory,
                    mWorkingDirectory.exists(),
                    mWorkingDirectory.canWrite()
            );
            // tolerating missing storage or directory
            return content;
        }
        DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(cacheFile));
        outputStream.writeLong(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresSec));
        IOUtils.copy(content, outputStream);
        outputStream.flush();
        outputStream.close();

        return get(url, headers);
    }

    @SupposeBackground
    public void clear(@NonNull String url, @Nullable Map<String, String> headers) throws Exception {
        FileUtils.deleteQuietly(new File(mWorkingDirectory, calculateHash(url, headers)));
    }

    private String calculateHash(String url, Map<String, String> headers) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        if (url != null) digest.update(url.getBytes());
        if (headers != null) {
            for (String key : headers.keySet()) {
                digest.update(key.getBytes());
                digest.update(headers.get(key).getBytes());
            }
        }
        return new String(Hex.encodeHex(digest.digest()));
    }
}
