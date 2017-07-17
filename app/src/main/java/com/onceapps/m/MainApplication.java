package com.onceapps.m;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.karumi.dexter.Dexter;
import com.onceapps.core.util.DeviceUtils;
import com.onceapps.core.util.Logger;
import com.onceapps.m.api.MagazineHandler;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.models.MagazineStatus;
import com.onceapps.m.services.MFirebaseInstanceIDService_;
import com.onceapps.m.services.MFirebaseMessagingService_;
import com.onceapps.m.utils.Preferences;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;
import org.apache.commons.io.FileUtils;

import java.util.ArrayList;
import java.util.List;

@EApplication
public class MainApplication extends MultiDexApplication {

    @Bean
    protected Preferences mPreferences;

    @Bean
    protected MagazineHandler mMagazineHandler;

    @Bean
    protected DeviceUtils mDeviceUtils;

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.init(getApplicationContext(), R.xml.analytics);

        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder()
                .setMaxCacheSize(64 * FileUtils.ONE_MB)
                .setBaseDirectoryName("fresco_cache")
                .setBaseDirectoryPath(getCacheDir())
                .build();

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(getApplicationContext())
                .setMainDiskCacheConfig(diskCacheConfig)
                .setSmallImageDiskCacheConfig(diskCacheConfig)
                .setDownsampleEnabled(true)
                .build();

        Fresco.initialize(getApplicationContext(), config);

        Dexter.initialize(getApplicationContext());

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        MFirebaseInstanceIDService_.intent(getApplicationContext()).start();
        MFirebaseMessagingService_.intent(getApplicationContext()).start();

        handleAppVersionUpgrade();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Background
    protected void handleAppVersionUpgrade() {
        int currentVersion = mDeviceUtils.getAppVersionCode();
        int prevVersion = mPreferences.app.appVersionPrevStart().getOr(0);
        if (currentVersion == prevVersion) return;

        mPreferences.app.appVersionPrevStart().put(currentVersion);
        if (prevVersion < 10023) {
            List<Magazine> magazines = new ArrayList<>();
            // pre-1.0.23 version has offline magazines without content descriptor downloaded
            for (MagazineStatus magazineStatus : mMagazineHandler.getMagazineRegistry().values()) {
                magazines.add(magazineStatus.getMagazine());
            }
            // double loop to prevent concurrent modification exception
            for (Magazine magazine : magazines) {
                mMagazineHandler.delete(magazine);
            }
        }

    }
}
