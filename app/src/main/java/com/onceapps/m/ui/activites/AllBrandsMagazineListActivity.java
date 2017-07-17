package com.onceapps.m.ui.activites;

import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.onceapps.core.util.Logger;
import com.onceapps.core.util.OfflineException;
import com.onceapps.m.R;
import com.onceapps.m.models.Brand;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.ui.adapters.AllBrandsBrandListViewAdapter;
import com.onceapps.m.ui.widgets.LoadingViewFlipper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.apptik.multiview.layoutmanagers.SnapperLinearLayoutManager;

@EActivity(R.layout.activity_all_brands_magazinelist)
public class AllBrandsMagazineListActivity extends BaseActivity {

    @ViewById(R.id.loading_flipper)
    protected LoadingViewFlipper mFlipper;

    @ViewById(R.id.recycler)
    protected RecyclerView recyclerView;

    @ViewById(R.id.pull_refresh)
    protected SwipeRefreshLayout mRefreshLayout;

    @InstanceState
    protected Parcelable mListViewState;

    private AllBrandsBrandListViewAdapter mAdapter;

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
            recyclerView.invalidate();
        }
    }

    @AfterViews
    protected void afterViews() {

        setTitle(getString(R.string.issues));

        initRecyclerView();

        mAdapter = new AllBrandsBrandListViewAdapter(this);
        mRefreshLayout.setColorSchemeResources(R.color.mGreen, R.color.mYellow, R.color.mPink, R.color.mBlue01, R.color.mRed);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMagazines(true);
            }
        });
        recyclerView.setAdapter(mAdapter);

        getMagazines(false);
    }

    @Background
    protected void getMagazines(boolean forceReload) {

        Map<Brand, List<Magazine>> magazines = new LinkedHashMap<>();
        try {
            if (forceReload) mRestClient.clearCacheForNextRequestOnThisThread();

            for (Brand brand : mRestClient.getBrands()) {
                magazines.put(brand, mRestClient.getMagazines(brand));
            }
        } catch (Exception e) {
            if (!(e instanceof OfflineException)) {
                Logger.error(e, "error getting brands");
            }
        }
        updateUI(magazines);
    }

    @UiThread
    protected void updateUI(Map<Brand, List<Magazine>> magazines) {
        recyclerView.scheduleLayoutAnimation();
        mAdapter.setContent(magazines);
        mFlipper.showContent(mAdapter.hasContent());
        mRefreshLayout.setRefreshing(false);
    }

    private void initRecyclerView() {
        final LinearLayoutManager layoutManager = new SnapperLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
    }
}