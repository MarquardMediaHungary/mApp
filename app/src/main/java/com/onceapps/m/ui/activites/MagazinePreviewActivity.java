package com.onceapps.m.ui.activites;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.onceapps.m.R;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.models.MagazineStatus;
import com.onceapps.m.utils.Utils;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.Locale;

@EActivity(R.layout.activity_magazine_preview)
public class MagazinePreviewActivity extends BaseActivity {

    protected final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy. MMMM", new Locale("hu", "HU"));

    @Extra
    protected Magazine magazine;

    @ViewById(R.id.download_button)
    protected Button mDownloadButton;

    @ViewById(R.id.date)
    protected TextView mDate;

    @ViewById(R.id.size)
    protected TextView mSize;

    @AfterViews
    protected void afterViews() {
        initToolBarImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI(false);
    }

    @SupposeUiThread
    protected void initToolBarImage() {

        if (magazine != null) {
            mExtendedToolbar.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(magazine.getCoverUrl())) {
                if (mToolbarImageSize == null) {
                    Utils.loadImageResized(mToolbarImage, magazine.getCoverUrl());
                } else {
                    Utils.loadImageResized(mToolbarImage, magazine.getCoverUrl(), mToolbarImageSize.first, mToolbarImageSize.second);
                }
                mToolbarImage.setAspectRatio(0.752f);
                mToolbarImage.setVisibility(View.VISIBLE);
            }
        }
    }

    @Click(R.id.download_button)
    protected void onMultifunctionButtonClicked() {
        MagazineStatus.DownloadStatus status = mMagazineHandler.getDownloadStatus(magazine);
        switch (status) {
            case DOWNLOAD_FINISHED:
                mDownloadButton.setText(R.string.open_magazine_button);
                MagazineArticleListActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        .magazine(magazine).start();
                finish();
                break;
            case NOT_DOWNLOADED:
            case DOWNLOAD_FAILED:
            case DOWNLOAD_CANCELLED:
                MyIssuesActivity_.intent(this).magazineToAdd(magazine).start();
                break;
            case DOWNLOAD_IN_PROGRESS:
                MyIssuesActivity_.intent(this).start();
                break;
        }
    }


    @UiThread
    protected void updateUI(boolean isStatusUpdate) {

        if (!isStatusUpdate) {

            if (magazine.getReleaseDate() != null)
                mDate.setText(mDateFormat.format(magazine.getReleaseDate()));
            if (magazine.getPackageSize() != null)
                mSize.setText(getString(R.string.size_format, magazine.getPackageSize() / 1024 / 1024));
        }

        MagazineStatus.DownloadStatus status = mMagazineHandler.getDownloadStatus(magazine);
        switch (status) {
            case DOWNLOAD_FINISHED:
                mDownloadButton.setText(R.string.open_magazine_button);
                MagazineArticleListActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        .magazine(magazine).start();
                finish();
                break;
            case NOT_DOWNLOADED:
            case DOWNLOAD_FAILED:
            case DOWNLOAD_CANCELLED:
                mDownloadButton.setText(R.string.download_magazine_button_text);
                break;
            case DOWNLOAD_IN_PROGRESS:
                mDownloadButton.setText(R.string.download_in_progress);
                break;
        }
    }

    @Subscribe
    public void statusUpdated(MagazineStatus status) {
        updateUI(true);
    }
}