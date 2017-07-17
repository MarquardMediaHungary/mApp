package com.onceapps.m.ui.activites;

import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.models.Brand;
import com.onceapps.m.models.Gallery;
import com.onceapps.m.ui.adapters.GalleryImagesAdapter;
import com.onceapps.m.ui.widgets.LoadingViewFlipper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_gallery)
public class GalleryActivity extends BaseActivity {

    @Extra
    protected Long galleryId;

    @Extra
    protected Brand brand;

    @Extra String title;

    @ViewById(R.id.recycler)
    protected RecyclerView recyclerView;

    @ViewById(R.id.loading_flipper)
    protected LoadingViewFlipper mFlipper;

    @ViewById(R.id.brand)
    protected TextView mBrandTextView;

    @ViewById(R.id.selected_page)
    protected TextView mSelectedPageTextView;

    @ViewById(R.id.pages)
    protected TextView mPages;

    private Gallery mGallery = new Gallery();

    @InstanceState
    protected Parcelable mListViewState;

    @InstanceState
    protected Integer mSelectedPage = 1;

    private final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
    private GalleryImagesAdapter mAdapter;

    @AfterViews
    protected void afterViews() {

        setTitle(title);

        initRecyclerView();

        getGallery();
    }

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
            setSelectedPage(mSelectedPage);
        }
        else {
            getGallery();
        }
    }

    @Background
    protected void getGallery() {
        try {
            mGallery = mRestClient.getGallery(galleryId);
            updateUI();
        } catch (Exception e) {
            Logger.error(e, "error getting gallery");
        }
    }



    private void initRecyclerView() {
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(mLayoutManager != null){
                    int selectedPage = mLayoutManager.findFirstCompletelyVisibleItemPosition() + 1;
                    if(selectedPage > 0)
                        setSelectedPage(selectedPage);
                }
            }
        });
    }

    @UiThread
    protected void updateUI() {
        if(mGallery != null && mGallery.getImages() != null) {
            setRecyclerAdapter(recyclerView);

            mPages.setText(getString(R.string.gallery_pages, mGallery.getImages().size()));
            setSelectedPage(mSelectedPage);
        }

        mFlipper.showContent(mGallery != null && mGallery.getImages() != null && mGallery.getImages().size() > 0);

        if(brand != null && brand.getName() != null) mBrandTextView.setText(brand.getName());
    }

    @UiThread
    protected void setSelectedPage(int page) {
        mSelectedPageTextView.setText(Integer.toString(page));
        mSelectedPage = page;
    }

    private void setRecyclerAdapter(RecyclerView recyclerView) {
        mAdapter = new GalleryImagesAdapter(this, mGallery.getImages());
        recyclerView.setAdapter(mAdapter);
    }
}