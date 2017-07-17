package com.onceapps.m.ui.activites;

import android.os.Parcelable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.models.Brand;
import com.onceapps.m.ui.ItemOffsetDecoration;
import com.onceapps.m.ui.adapters.BrandListViewAdapter;
import com.onceapps.m.ui.widgets.BrandTopicListBottomTabBar;
import com.onceapps.m.ui.widgets.LoadingViewFlipper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_brandlist)
public class BrandListActivity extends BaseActivity implements BrandListViewAdapter.OnItemClickListener {

    @ViewById(R.id.loading_flipper)
    protected LoadingViewFlipper mFlipper;

    @ViewById(R.id.recycler)
    protected RecyclerView recyclerView;

    @ViewById(R.id.bottom_tabbar)
    protected BrandTopicListBottomTabBar mBottomTabbar;

    private List<Brand> mBrandList = new ArrayList<>();

    @InstanceState
    protected Parcelable mListViewState;

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
        initRecyclerView();
        mBottomTabbar.selectBrands();

        getBrands();

    }

    @Background
    protected void getBrands() {

        try {
            mBrandList = mRestClient.getBrands();
            updateUI();
        } catch (Exception e) {
            Logger.error(e, "error getting brands");
        }
    }

    @UiThread
    protected void updateUI() {
        setRecyclerAdapter(recyclerView);
        recyclerView.scheduleLayoutAnimation();

        mFlipper.showContent(mBrandList.size() > 0);
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.default_item_offset);
        recyclerView.addItemDecoration(itemDecoration);
    }

    private void setRecyclerAdapter(RecyclerView recyclerView) {
        BrandListViewAdapter adapter = new BrandListViewAdapter(mBrandList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, Brand brand) {
        ArticleListActivity_.intent(this).brand(brand).start();
    }
}