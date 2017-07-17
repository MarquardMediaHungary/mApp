package com.onceapps.m.ui.activites;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.models.MagazineContentItem;
import com.onceapps.m.models.MagazineContents;
import com.onceapps.m.models.Topic;
import com.onceapps.m.ui.widgets.LoadingLayout;
import com.onceapps.m.ui.widgets.TopicTextWithIndicatorView;
import com.onceapps.m.ui.widgets.TopicTextWithIndicatorView_;
import com.onceapps.m.utils.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.FileNotFoundException;
import java.security.GeneralSecurityException;

@EActivity(R.layout.activity_magazine)
public class MagazineActivity extends BaseActivity {

    @Extra
    protected Magazine magazine;

    @Extra
    protected Integer page = 0;

    @ViewById(R.id.magazine_content)
    protected WebView mMagazineContentWebView;

    @ViewById(R.id.loading)
    protected LoadingLayout mLoading;

    @ViewById(R.id.article_flipper)
    protected ViewFlipper mFlipper;

    @ViewById(R.id.brand)
    protected TextView mBrand;

    @ViewById(R.id.date)
    protected TextView mDate;

    @ViewById(R.id.topics_parent)
    protected LinearLayout mTopicsParent;

    private MagazineContents mContents = new MagazineContents();

    @InstanceState
    protected Parcelable mListViewState;

    private volatile int mCurrentPage = 0;
    private volatile int mPages = 0;

    @SuppressLint("SetJavaScriptEnabled")
    @AfterViews
    protected void afterViews() {

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float percentage = 1.0f - ((float) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange() * 2);
                mExtendedToolbarTitle.setAlpha(percentage);
                mExtendedToolbarTitleImage.setAlpha(percentage);
                mBrand.setAlpha(percentage);
            }
        });


        if (magazine.getBrand() != null) {
            mBrand.setText(magazine.getBrand().getName());
        }

        mMagazineContentWebView.getSettings().setJavaScriptEnabled(true);

        mMagazineContentWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if (newProgress == 100) {
                    mFlipper.setDisplayedChild(1);
                } else if (mFlipper.getDisplayedChild() != 0) {
                    mFlipper.setDisplayedChild(0);
                }
            }
        });

        setTitle(magazine.getName());

        try {
            mPages = mMagazineHandler.getNumberOfPages(magazine);
        } catch (GeneralSecurityException e) {
            Logger.warn(e, "permission not granted");
        } catch (FileNotFoundException e) {
            Logger.warn(e, "file not found");
        }

        getContents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPage(page);
        checkForUpdate();
    }

    @Background
    protected void getContents() {
        try {
            mContents = mRestClient.getMagazineContents(magazine);
            updateHeaderUI();
        } catch (Exception e) {
            Logger.error(e, "error getting contents");
        }
    }

    @UiThread
    protected void updateUI() {

        try {
            mMagazineContentWebView.loadUrl("file://" + mMagazineHandler.getMagazinePage(magazine, mCurrentPage).getAbsolutePath());
            updateHeaderUI();
        } catch (GeneralSecurityException e) {
            Logger.warn(e, "permission not granted");
            onBackPressed();
        } catch (FileNotFoundException e) {
            Logger.warn(e, "file not found");
            onBackPressed();
        } catch (Exception e) {
            Logger.warn(e, "failed to load magazine page");
            onBackPressed();
        }
    }


    @UiThread
    protected void updateHeaderUI() {
        if (mContents != null && mContents.size() > 0) {
            MagazineContentItem item = mContents.getContentsItemByHTMLPageNumber(mCurrentPage);
            if (item != null) {
                setExtendedToolbarTitle(item.getTitle());
                if (mToolbarImageSize == null) {
                    Utils.loadImageResized(mToolbarImage, item.getFileUrlSmall());
                } else {
                    Utils.loadImageResized(mToolbarImage, item.getFileUrlSmall(), mToolbarImageSize.first, mToolbarImageSize.second);
                }
                mToolbarImage.getHierarchy().setActualImageFocusPoint(new PointF(0.5f, 0.0f));
                updateTopics(item);
                unlockAppBarOpen();
            } else {
                lockAppBarClosed();
            }
        }
    }

    @SupposeUiThread
    protected void updateTopics(MagazineContentItem item) {
        mTopicsParent.removeAllViews();
        if (item != null && item.getTopics() != null && item.getTopics().size() > 0) {
            mTopicsParent.setVisibility(View.VISIBLE);
            for (Topic topic : item.getTopics()) {
                TopicTextWithIndicatorView topicView = TopicTextWithIndicatorView_.build(this);
                topicView.setDataSource(topic, ContextCompat.getColor(this, R.color.mGrey02));
                mTopicsParent.addView(topicView);
            }
            mTopicsParent.invalidate();
        } else {
            mTopicsParent.setVisibility(View.INVISIBLE);
        }
        mTopicsParent.getParent().requestLayout();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mNavigationView != null && mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
            return;
        }
        super.onBackPressed();
    }


    protected void loadPage(int page) {
        if (page >= 0 && page < mPages) {
            mCurrentPage = page;
            updateUI();
        } else {
            onBackPressed();
        }
    }


    @Background
    protected void checkForUpdate() {
        if (mMagazineHandler.isUpdateAvailable(magazine)) {
            askToDownloadNewVersion();
        }
    }

    @UiThread
    protected void askToDownloadNewVersion() {

        showAlertDialog(
                getString(R.string.magazine_download_new_version_title),
                getString(R.string.magazine_download_new_version_message, magazine.getName()),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissAlertDialog();
                        mMagazineHandler.delete(magazine);
                        MyIssuesActivity_.intent(MagazineActivity.this).magazineToAdd(magazine).start();
                        finish();
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissAlertDialog();
                    }
                });
    }
}