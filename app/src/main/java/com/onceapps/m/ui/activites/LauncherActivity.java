package com.onceapps.m.ui.activites;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.api.RestClient;
import com.onceapps.m.api.RestClient_;
import com.onceapps.m.models.User;
import com.onceapps.m.utils.Preferences;
import com.onceapps.m.utils.Preferences_;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

/**
 * LauncherActivity
 * Created by szipe on 28/04/16.
 */
@EActivity(R.layout.activity_launcher)
public class LauncherActivity extends AppCompatActivity {

    private final static int REQUEST_PERMISSION_SETTING = 42;

    private Preferences mPreferences;
    private RestClient mRestClient;

    private boolean mLoggedIn = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = Preferences_.getInstance_(this);
        mRestClient = RestClient_.getInstance_(this);

        mPreferences.app.deprecatedMessageShown().put(false);

        checkStoragePermission();
    }

    protected void startWhenAllChecksDone() {
        if(mLoggedIn) {
            ArticleListActivity_.intent(this).start();
        }
        else {
            StartActivity_.intent(this).start();
        }
        finish();
    }

    @UiThread
    protected void checkStoragePermission() {
        if (PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Logger.debug("storage permission denied");
            StoragePermissionRequestActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP).startForResult(REQUEST_PERMISSION_SETTING);
        } else {
            checkLogin();
        }
    }

    @Background
    protected void checkLogin() {

        mLoggedIn = mPreferences.app.authToken().exists();
        if (mLoggedIn) {
            try {
                User user  = mRestClient.getUser();
                if(user != null) {
                mPreferences.app.edit()
                        .user().put(user.toString())
                        .apply();
                }
            } catch (Exception e) {
                Logger.warn(e, "Error getting user");
            }
        }

        startWhenAllChecksDone();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            startWhenAllChecksDone();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
