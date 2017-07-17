package com.onceapps.m.ui.activites;

import android.graphics.Point;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.models.MagazineContentItem;
import com.onceapps.m.models.MagazineContents;
import com.onceapps.m.models.MagazineStatus;
import com.onceapps.m.models.Topic;
import com.onceapps.m.ui.ItemOffsetDecoration;
import com.onceapps.m.ui.adapters.MagazineContentsAdapter;
import com.onceapps.m.ui.utils.AppBarStateChangeListener;
import com.onceapps.m.ui.widgets.LoadingViewFlipper;
import com.onceapps.m.utils.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.FileNotFoundException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TreeMap;

@EActivity(R.layout.activity_magazine_article_list)
public class MagazineArticleListActivity extends BaseActivity implements MagazineContentsAdapter.OnItemClickListener {

    protected final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy. MMMM", new Locale("hu", "HU"));

    private Topic topic = null;

    @Extra
    protected Magazine magazine;

    @ViewById(R.id.open_mobile_button)
    protected Button mOpenMobileButton;

    @ViewById(R.id.open_pdf_button)
    protected Button mOpenPdfButton;

    @ViewById(R.id.delete_button)
    protected Button mDeleteButton;

    @ViewById(R.id.date)
    protected TextView mDate;

    @ViewById(R.id.size)
    protected TextView mSize;

    @ViewById(R.id.recycler)
    protected RecyclerView mRecyclerView;

    @ViewById(R.id.loading_flipper)
    protected LoadingViewFlipper mFlipper;

    @ViewById(R.id.bottom_container)
    protected RelativeLayout mBottomContainer;

    @ViewById(R.id.topics_tabs)
    protected TabLayout mTopicsTabLayout;

    @ViewById(R.id.pull_refresh)
    protected SwipeRefreshLayout mRefreshLayout;

    @ViewById(R.id.pdf_fab)
    protected FloatingActionButton mPdfFab;

    private MagazineContents mContents = new MagazineContents();

    private TreeMap<Topic, MagazineContents> mContentsFiltered = new TreeMap<>();

    private MagazineContentsAdapter mAdapter;

    @InstanceState
    protected Parcelable mListViewState;

    @InstanceState
    protected Integer mSelectedTopicsTab;

    private StaggeredGridLayoutManager mLayoutManager;
    private final ArticleListOnScrollListener mOnScrollListener = new ArticleListOnScrollListener();

    @AfterViews
    protected void afterViews() {

        init();

        initToolBarImage();

        getContents();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mListViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mListViewState != null) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mListViewState);
            mRecyclerView.invalidate();
        }

        if (mSelectedTopicsTab != null && mSelectedTopicsTab > -1 && mTopicsTabLayout != null && mTopicsTabLayout.getTabCount() > mSelectedTopicsTab) {
            TabLayout.Tab tab = mTopicsTabLayout.getTabAt(mSelectedTopicsTab);
            if (tab != null) {
                tab.select();
            }
        }
    }

    @Background
    protected void getContents() {
        try {
            showLoadingBlur();
            mContents = mRestClient.getMagazineContents(magazine);
            populateFilteredMagazineContents();
            initTopicsTabHost();
        } catch (Exception e) {
            Logger.error(e, "error getting contents");
        }
        updateUI();
    }

    @SupposeBackground
    protected void populateFilteredMagazineContents() {
        for (MagazineContentItem magazineContentItem : mContents) {
            if (magazineContentItem.getTopics() != null) {
                for (Topic topic : magazineContentItem.getTopics()) {
                    if (!mContentsFiltered.containsKey(topic)) {
                        mContentsFiltered.put(topic, new MagazineContents());
                    }
                    MagazineContents magazineContents = mContentsFiltered.get(topic);
                    magazineContents.add(magazineContentItem);
                    mContentsFiltered.put(topic, magazineContents);
                }
            }
        }
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

    @Click(R.id.delete_button)
    protected void onDeleteButtonClicked() {
        MagazineStatus.DownloadStatus status = mMagazineHandler.getDownloadStatus(magazine);
        switch (status) {
            case DOWNLOAD_FINISHED:
                mMagazineHandler.delete(magazine);
                finish();
                MagazinePreviewActivity_.intent(MagazineArticleListActivity.this).magazine(magazine).start();
                break;
        }
    }

    private void init() {

        mRefreshLayout.setEnabled(false);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {

            @Override
            public void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state) {
                if (state == AppBarStateChangeListener.State.COLLAPSED) {
                    if (magazine.getName() != null) {
                        setTitle(magazine.getName());
                    }
                    try {
                        mPdfFab.setVisibility(mMagazineHandler.getMagazinePdf(magazine).exists() ? View.VISIBLE : View.GONE);
                    } catch (GeneralSecurityException | FileNotFoundException e) {
                        Logger.warn(e, "error getMagazinePdf");
                        mPdfFab.setVisibility(View.GONE);
                    }
                } else if (mCurrentState == AppBarStateChangeListener.State.COLLAPSED) {
                    setTitle("");
                    mPdfFab.setVisibility(View.GONE);
                }
            }
        });

        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        mLayoutManager = new StaggeredGridLayoutManager(isTablet ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);


        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.list_margin);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        mFlipper.showLoading();

        setRecyclerAdapter();
    }

    @UiThread
    protected void updateUI() {

        mBottomContainer.setVisibility(View.VISIBLE);

        if (magazine.getReleaseDate() != null)
            mDate.setText(mDateFormat.format(magazine.getReleaseDate()));
        if (magazine.getPackageSize() != null)
            mSize.setText(getString(R.string.size_format, magazine.getPackageSize() / 1024 / 1024));

        MagazineContents contents = topic == null ? mContents : mContentsFiltered.get(topic);

        if (contents != null && contents.size() > 0) {
            mFlipper.setVisibility(View.VISIBLE);
            mOpenMobileButton.setVisibility(View.VISIBLE);
            mFlipper.showContent();

            mToolbarImage.setAspectRatio(0.752f);
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbarParent.getLayoutParams();
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);

            setMagazineContents(contents);
        } else {
            mOpenMobileButton.setVisibility(View.GONE);
            mFlipper.setVisibility(View.INVISIBLE);
            mToolbarImage.setAspectRatio(getDeviceAspectRatio());
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbarParent.getLayoutParams();
            params.setScrollFlags(0);

        }

        hideLoadingBlur(false);
    }

    private float getDeviceAspectRatio() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        return (float) width/height;
    }

    private void setRecyclerAdapter() {
        mAdapter = new MagazineContentsAdapter(this, null, magazine.getBrand());
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @UiThread
    protected void setMagazineContents(MagazineContents contents) {
        mAdapter.setArticles(contents);
    }

    @UiThread
    protected void initTopicsTabHost() {

        if (mContentsFiltered.size() > 0) {
            mTopicsTabLayout.setVisibility(View.VISIBLE);
            mTopicsTabLayout.addTab(mTopicsTabLayout.newTab().setText(R.string.tab_name_all_topics));
            for (Topic topic : mContentsFiltered.keySet()) {
                mTopicsTabLayout.addTab(mTopicsTabLayout.newTab().setText(topic.getName()));
            }

            mTopicsTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() > 0 && tab.getPosition() <= mContentsFiltered.size()) {
                        topic = (Topic) mContentsFiltered.keySet().toArray()[tab.getPosition() - 1];
                    } else {
                        topic = null;
                    }
                    mRecyclerView.removeOnScrollListener(mOnScrollListener);
                    mRecyclerView.scrollToPosition(0);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.addOnScrollListener(mOnScrollListener);
                        }
                    }, 300);
                    updateUI();

                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        } else {
            mTopicsTabLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(View view, MagazineContentItem magazineContentItem) {
        MagazineActivity_.intent(this)
                .magazine(magazine)
                .page(magazineContentItem == null ? 0 : magazineContentItem.getHtmlPage()).start();
    }

    @UiThread
    protected void showOpenButtons() {

        try {
            mOpenMobileButton.setVisibility(mMagazineHandler.getMagazinePage(magazine, 0).exists() ? View.VISIBLE : View.GONE);
            mFlipper.setVisibility(mMagazineHandler.getMagazinePage(magazine, 0).exists() ? View.VISIBLE : View.GONE);
            mOpenPdfButton.setVisibility(mMagazineHandler.getMagazinePdf(magazine).exists() ? View.VISIBLE : View.GONE);

            if (!mMagazineHandler.getMagazinePage(magazine, 0).exists() && !mMagazineHandler.getMagazinePdf(magazine).exists()) {
                showAlertDialog(
                        R.string.error,
                        R.string.error_opening_magazine,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMagazineHandler.delete(magazine);
                                dismissAlertDialog();
                                finish();
                                MagazinePreviewActivity_.intent(MagazineArticleListActivity.this).magazine(magazine).start();
                            }
                        },
                        null);
            }
        } catch (GeneralSecurityException e) {
            Logger.warn(e, "Error opening magazine (id: %d)", magazine.getId());
        } catch (FileNotFoundException e) {
            Logger.warn(e, "file not found");
        }
    }

    @Click(R.id.open_mobile_button)
    protected void openMobileButtonClicked() {
        mAnimatedAppBarStateChangeListener.collapse();
        mRecyclerView.scrollToPosition(0);
    }

    @Click({R.id.open_pdf_button, R.id.pdf_fab})
    protected void openPdfButtonClicked() {
        MagazinePDFActivity_.intent(this).magazine(magazine).start();
    }

    protected class ArticleListOnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (!recyclerView.canScrollVertically(-1) && mAnimatedAppBarStateChangeListener.getCurrentState() == AppBarStateChangeListener.State.COLLAPSED) {
                mAnimatedAppBarStateChangeListener.expand();
            }
        }
    }
}