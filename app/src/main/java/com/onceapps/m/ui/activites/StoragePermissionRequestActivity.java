package com.onceapps.m.ui.activites;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.api.MagazineHandler;
import com.onceapps.m.ui.widgets.CustomAlertDialog;
import com.onceapps.m.ui.widgets.CustomAlertDialog_;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

/**
 * StoragePermissionRequestActivity
 * Created by szipe on 03/05/16.
 */
@EActivity
public class StoragePermissionRequestActivity extends AppCompatActivity implements PermissionListener {

    private final static int REQUEST_PERMISSION_SETTING = 42;

    private CustomAlertDialog mPermissionDialog;
    private boolean mShowPermissionDialog = false;
    private boolean mPermanentlyDenied = false;

    @Bean
    protected MagazineHandler mMagazineHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkStoragePermission();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    private void checkStoragePermission() {
        if (!Dexter.isRequestOngoing()) {
            Logger.debug("checkStoragePermission");
            Dexter.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onPermissionGranted(PermissionGrantedResponse response) {
        Logger.debug("permission granted");
        mShowPermissionDialog = false;
        mPermanentlyDenied = false;
        mMagazineHandler.processPendingMagazines();
        finish();
    }

    @Override
    public void onPermissionDenied(PermissionDeniedResponse response) {
        Logger.debug("permission denied");

        mPermanentlyDenied = response.isPermanentlyDenied();
        mShowPermissionDialog = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            checkStoragePermission();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
        Logger.debug("permission rationale should be shown");
        token.continuePermissionRequest();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mShowPermissionDialog) {
            showPermissionDialog();
        }
    }

    private void showPermissionDialog() {

        if (mPermissionDialog == null) {
            mPermissionDialog = CustomAlertDialog_.builder().build();
        } else if (mPermissionDialog.isAdded()) {
            mPermissionDialog.dismiss();
        }
        mPermissionDialog.setTitle(getString(R.string.storage_permission_rationale_dialog_title));
        mPermissionDialog.setMessage(getString(R.string.storage_permission_rationale_dialog_message));
        mPermissionDialog.setCancelable(false);

        mPermissionDialog.setNegativeButtonVisible(true);
        mPermissionDialog.setNegativeButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.debug("showPermissionDialog negative");
                StoragePermissionRequestActivity.this.finish();
            }
        });

        mPermissionDialog.setPositiveButtonVisible(true);
        mPermissionDialog.setPositiveButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPermissionDialog.dismiss();
                if (mPermanentlyDenied) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                } else {
                    checkStoragePermission();
                }
            }
        });

        if (mPermissionDialog.getDialog() == null || !mPermissionDialog.getDialog().isShowing()) {
            mPermissionDialog.show(getFragmentManager(), null);
        }
    }
}
