package com.onceapps.m.ui.activites;

import android.annotation.SuppressLint;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.MuPDFView;
import com.artifex.mupdfdemo.OutlineActivityData;
import com.artifex.mupdfdemo.PageView;
import com.artifex.mupdfdemo.ReaderView;
import com.artifex.mupdfdemo.SearchTaskResult;
import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.ui.widgets.LoadingLayout;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.concurrent.atomic.AtomicInteger;

@EActivity(R.layout.activity_magazine_pdf)
public class MagazinePDFActivity extends BaseActivity {

    private MuPDFCore core;
    private MuPDFReaderView mDocView;

    @Extra
    protected Magazine magazine;

    @Extra
    protected Integer page = 0;

    @ViewById(R.id.loading)
    protected LoadingLayout mLoading;

    @ViewById(R.id.pdf_container)
    protected RelativeLayout mPdfContainer;

    @InstanceState
    protected Parcelable mListViewState;

    private volatile int mCurrentPage = 0;
    private volatile int mPages = 0;

    private AtomicInteger mLoadingCounter = new AtomicInteger(0);

    @SuppressLint("SetJavaScriptEnabled")
    @AfterViews
    protected void afterViews() {
        init();
        setTitle(magazine.getName());
    }

    private void init() {
        try {

            String path;
            if (magazine != null && mMagazineHandler.getMagazinePdf(magazine) != null) {
                path = mMagazineHandler.getMagazinePdf(magazine).getAbsolutePath();

                core = openFile(path);
                SearchTaskResult.set(null);

                if (core != null && core.needsPassword()) {
                    if (!core.authenticatePassword(magazine.getPdfPassword())) {
                        showErrorDialog(new PDFPasswordException("password not correct"));
                        return;
                    }
                }
                if (core != null) {
                    mPages = core.countPages();
                } else {
                    showErrorDialog(null);
                    return;
                }
            } else {
                showErrorDialog(null);
                return;
            }
        } catch (Exception | Error e) {
            showErrorDialog(e);
            return;
        }

        createUI();
    }

    @UiThread
    protected void showErrorDialog(Throwable error) {

        Logger.warn(error, "error opening magazine (id: %d) pdf", magazine.getId());

        showAlertDialog(
                R.string.error,
                R.string.error_opening_magazine,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissAlertDialog();
                        finish();
                    }
                },
                null);
    }

    public void createUI() {
        if (core == null)
            return;

        mDocView = new MuPDFReaderView(this) {
            @Override
            protected void onMoveToChild(int i) {
                if (core == null)
                    return;

                super.onMoveToChild(i);
                mCurrentPage = i;
                mDocView.zoomOut();
            }

            @Override
            protected void onTapMainDocArea() {

            }

            @Override
            protected void onDocMotion() {

            }

            @Override
            protected void onHit(MuPDFView.Hit item) {

            }


        };
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mDocView.setLayoutParams(lp);
        mDocView.setAdapter(new MuPDFPageAdapter(this, null, core));
        mDocView.setDisplayedViewIndex(page);

        mPdfContainer.addView(mDocView);
        hideLoading();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPage(page);
        checkForUpdate();
    }

    @Override
    protected void onDestroy() {
        if (mDocView != null) {
            mDocView.applyToChildren(new ReaderView.ViewMapper() {
                @Override
                public void applyToView(View view) {
                    ((MuPDFView) view).releaseBitmaps();
                }
            });
        }
        if (core != null)
            core.onDestroy();
        core = null;
        super.onDestroy();
    }

    protected void loadPage(int page) {
        if (page >= 0 && page < mPages) {
            mDocView.setDisplayedViewIndex(mCurrentPage);
        }
    }

    @Subscribe
    public void onShowLoadingEvent(PageView.ShowLoadingEvent event) {
        mLoadingCounter.incrementAndGet();
        showLoading();
    }

    @Subscribe
    public void onHideLoadingEvent(PageView.HideLoadingEvent event) {
        if (mLoadingCounter.decrementAndGet() <= 0) {
            mLoadingCounter.set(0);
            hideLoading();
        }
    }

    @UiThread
    protected void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
    }

    @UiThread
    protected void hideLoading() {
        mLoading.setVisibility(View.GONE);
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
                        MyIssuesActivity_.intent(MagazinePDFActivity.this).magazineToAdd(magazine).start();
                        finish();
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissAlertDialog();
                    }
                }
        );
    }

    private MuPDFCore openFile(String path) throws Exception, OutOfMemoryError, UnsatisfiedLinkError {
        core = new MuPDFCore(this, path);
        // New file: drop the old outline data
        OutlineActivityData.set(null);

        return core;
    }

    public static class PDFPasswordException extends Exception {

        public PDFPasswordException(String detailMessage) {
            super(detailMessage);
        }
    }
}