package com.onceapps.m.ui.activites;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.onceapps.core.util.Logger;
import com.onceapps.core.util.OfflineException;
import com.onceapps.m.R;
import com.onceapps.m.models.Brand;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.models.MagazineStatus;
import com.onceapps.m.ui.ItemOffsetDecoration;
import com.onceapps.m.ui.adapters.MagazineListViewAdapter;
import com.onceapps.m.ui.utils.AppBarStateChangeListener;
import com.onceapps.m.ui.widgets.LoadingViewFlipper;
import com.onceapps.m.utils.Utils;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;


@EActivity(R.layout.activity_magazinelist)
public class MagazineListActivity extends BaseActivity implements MagazineListViewAdapter.OnItemClickListener, MagazineListViewAdapter.OnDownloadButtonClickListener {

    @Extra
    protected Brand brand;

    @ViewById(R.id.loading_flipper)
    protected LoadingViewFlipper mFlipper;

    @ViewById(R.id.recycler)
    protected RecyclerView recyclerView;

    @ViewById(R.id.nested_scroll)
    protected NestedScrollView mNestedScroll;

    @ViewById(R.id.pull_refresh)
    protected SwipeRefreshLayout mRefreshLayout;

    @InstanceState
    protected Parcelable mListViewState;

    private List<Magazine> mMagazineList = new ArrayList<>();

    private MagazineListViewAdapter mAdapter;

    private final MagazineListOnScrollListener mOnScrollListener = new MagazineListOnScrollListener();

    @Override
    protected void onPause() {
        super.onPause();
        mListViewState = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mListViewState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(mListViewState);
            updateUI();
        }
    }

    @AfterViews
    protected void afterViews() {

        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {

            State previousState = State.EXPANDED;

            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.COLLAPSED) {
                    if (brand != null) {
                        setTitle(brand.getName());
                    }
                } else if (previousState == State.COLLAPSED) {
                    setTitle("");
                }
                previousState = state;
            }
        });

        if (brand != null) {
            mExtendedToolbar.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(brand.getImage())) {
                if (mToolbarImageSize == null) {
                    Utils.loadImageResized(mToolbarImage, brand.getImage());
                } else {
                    Utils.loadImageResized(mToolbarImage, brand.getImage(), mToolbarImageSize.first, mToolbarImageSize.second);
                }
                mToolbarImage.setVisibility(View.VISIBLE);
            }

            if (brand.getLogo() != null && brand.getLogo().getCropped() != null && !TextUtils.isEmpty(brand.getLogo().getCropped().getWhite())) {
                setExtendedToolbarTitleImage(brand.getLogo().getCropped().getWhite());
            }
        }

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMagazines(true);
            }
        });
        mRefreshLayout.setColorSchemeResources(R.color.mGreen, R.color.mYellow, R.color.mPink, R.color.mBlue01, R.color.mRed);

        initRecyclerView();

        getMagazines(false);

    }

    @Background
    protected void getMagazines(boolean forceReload) {

        try {
            if (forceReload) mRestClient.clearCacheForNextRequestOnThisThread();
            mMagazineList = mRestClient.getMagazines(brand);
        } catch (Exception e) {
            if (!(e instanceof OfflineException)) {
                Logger.error(e, "error getting magazines");
            }
            mMagazineList.clear();
        }
        updateUI();
    }

    @UiThread
    protected void updateUI() {
        setRecyclerAdapter(recyclerView);
        recyclerView.scheduleLayoutAnimation();

        mFlipper.showContent(mMagazineList.size() > 0);
        mRefreshLayout.setRefreshing(false);
    }

    private void initRecyclerView() {
        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        recyclerView.setLayoutManager(new GridLayoutManager(this, isTablet ? 3 : 2));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.default_margin);
        recyclerView.addItemDecoration(itemDecoration);
        DefaultItemAnimator animator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
                return true;
            }
        };
        recyclerView.setItemAnimator(animator);
        mNestedScroll.setOnScrollChangeListener(mOnScrollListener);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void setRecyclerAdapter(RecyclerView recyclerView) {
        mAdapter = new MagazineListViewAdapter(this, mMagazineList);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnDownloadButtonClickListener(this);
        recyclerView.setAdapter(mAdapter);
    }

    @Subscribe
    public void statusUpdated(MagazineStatus status) {
        if (mAdapter != null)
            mAdapter.updateMagazinStatus(status);
    }

    @Override
    public void onItemClick(View view, Magazine magazine) {
        MagazinePreviewActivity_.intent(this).magazine(magazine).start();
    }

    @Override
    public void onDownloadButtonClick(View view, Magazine magazine) {
        MagazineStatus.DownloadStatus status = mMagazineHandler.getDownloadStatus(magazine);
        switch (status) {
            case DOWNLOAD_FINISHED:
                MagazinePreviewActivity_.intent(this).magazine(magazine).start();
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

    protected class MagazineListOnScrollListener implements NestedScrollView.OnScrollChangeListener {

        @Override
        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            if (!mNestedScroll.canScrollVertically(-1) && mAnimatedAppBarStateChangeListener.getCurrentState() == AppBarStateChangeListener.State.COLLAPSED) {
                mAnimatedAppBarStateChangeListener.expand();
            }
        }
    }
}