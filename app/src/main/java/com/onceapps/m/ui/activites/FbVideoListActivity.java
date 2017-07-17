package com.onceapps.m.ui.activites;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.onceapps.core.util.Logger;
import com.onceapps.core.util.OfflineException;
import com.onceapps.m.R;
import com.onceapps.m.models.FbVideo;
import com.onceapps.m.ui.adapters.FbVideoListViewAdapter;
import com.onceapps.m.ui.widgets.LoadingViewFlipper;
import com.onceapps.m.utils.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;


@EActivity(R.layout.activity_fbvideolist)
public class FbVideoListActivity extends BaseActivity implements FbVideoListViewAdapter.OnItemClickListener {

    @ViewById(R.id.recycler)
    protected RecyclerView mRecyclerView;

    @ViewById(R.id.loading_flipper)
    protected LoadingViewFlipper mFlipper;

    @ViewById(R.id.pull_refresh)
    protected SwipeRefreshLayout mRefreshLayout;

    @InstanceState
    protected Parcelable mListViewState;

    private FbVideoListViewAdapter mAdapter;

    private RecyclerView.LayoutManager mLayoutManager;

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
    }


    @AfterViews
    protected void afterViews() {
        init();
    }

    private void init() {

        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        mLayoutManager = new StaggeredGridLayoutManager(isTablet ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);

        setTitle(getString(R.string.menu_fb_video_list));

        mFlipper.showLoading();

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFbVideos(true);
            }
        });
        mRefreshLayout.setColorSchemeResources(R.color.mGreen, R.color.mYellow, R.color.mPink, R.color.mBlue01, R.color.mRed);

        initRecyclerView();
        setRecyclerAdapter();


        mExtendedToolbar.setVisibility(View.GONE);
        mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        getFbVideos(false);
    }

    @Background
    protected void getFbVideos(boolean forceReload) {

        try {
            if (forceReload) {
                checkLogin();
                mRestClient.clearCacheForNextRequestOnThisThread();
            }

            setFbVideos(mRestClient.getFbVideos());

        } catch (Exception e) {
            updateUI();
            if (!(e instanceof OfflineException)) {
                Logger.error(e, "error getting fb videos");
            }
        }
    }



    @UiThread
    protected void setFbVideos(final List<FbVideo> list) {
        mAdapter.setFbVideos(list);
        updateUI();
    }

    @UiThread
    protected void updateUI() {
        if (mAdapter.getItemCount() == 0) {
            mFlipper.showNoContent();
        } else {
            mFlipper.showContent();
        }
        mRefreshLayout.setRefreshing(false);
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemViewCacheSize(5);
    }

    private void setRecyclerAdapter() {
        mAdapter = new FbVideoListViewAdapter(this, null);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(View view, final FbVideo video) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(video.getVideoDeeplink()));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        else {
            showAlertDialog(
                    R.string.alert,
                    R.string.fb_app_missing_message_360,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dismissAlertDialog();
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("market://details?id=%s", Utils.FB_APP_PACKAGE_NAME))));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://play.google.com/store/apps/details?id=%s", Utils.FB_APP_PACKAGE_NAME))));
                            }
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
    }
}
