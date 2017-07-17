package com.onceapps.m.ui.activites;

import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.onceapps.core.util.Broadcast;
import com.onceapps.core.util.Logger;
import com.onceapps.core.util.OfflineException;
import com.onceapps.m.R;
import com.onceapps.m.models.Brand;
import com.onceapps.m.models.FollowedItem;
import com.onceapps.m.models.FollowedList;
import com.onceapps.m.models.Topic;
import com.onceapps.m.models.User;
import com.onceapps.m.ui.PageRequest;
import com.onceapps.m.ui.adapters.FollowedListViewAdapter;
import com.onceapps.m.ui.utils.AppBarStateChangeListener;
import com.onceapps.m.ui.utils.PreCachingLayoutManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.List;


@EActivity(R.layout.activity_followedlist)
public class FollowedListActivity extends BaseActivity implements FollowedListViewAdapter.OnItemClickListener {

    @ViewById(R.id.recycler)
    protected RecyclerView mRecyclerView;

    @ViewById(R.id.type_tabs)
    protected TabLayout mTypeTabLayout;

    @ViewById(R.id.loading_flipper)
    protected ViewFlipper mFlipper;

    @ViewById(R.id.pull_refresh)
    protected SwipeRefreshLayout mRefreshLayout;

    @ViewById(R.id.extended_toolbar_button)
    protected Button mSettingsButton;

    @ViewById(R.id.no_content)
    protected TextView mNoContent;

    @InstanceState
    protected Parcelable mListViewState;

    @InstanceState
    protected Integer mSelectedTab;

    private FollowedListViewAdapter mAdapter;

    HashMap<Integer, Brand> mBrands = new HashMap<>();
    HashMap<Integer, Topic> mTopics = new HashMap<>();

    private User mUser;

    private final LinearLayoutManager mLayoutManager = new PreCachingLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    private final ArticleListOnScrollListener mOnScrollListener = new ArticleListOnScrollListener();

    protected FollowedItem.Type mType = FollowedItem.Type.TOPIC;

    @Override
    protected void onPause() {
        super.onPause();
        mListViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mSelectedTab = mTypeTabLayout.getSelectedTabPosition();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSelectedTab != null && mSelectedTab > -1 && mTypeTabLayout != null && mTypeTabLayout.getTabCount() > mSelectedTab) {
            TabLayout.Tab tab = mTypeTabLayout.getTabAt(mSelectedTab);
            if (tab != null) {
                tab.select();
            }
        }
        if (mListViewState != null) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mListViewState);
            mRecyclerView.invalidate();
        }

        setUserName();

        getFollowedList(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        init();
    }

    @AfterViews
    protected void afterViews() {
        init();
    }

    private void init() {

        mToolbarImage.setAspectRatio(1.44f);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFollowedList(true);
            }
        });
        mRefreshLayout.setColorSchemeResources(R.color.mGreen, R.color.mYellow, R.color.mPink, R.color.mBlue01, R.color.mRed);

        initRecyclerView();
        setRecyclerAdapter();

        mExtendedToolbar.setVisibility(View.VISIBLE);

        setUserName();

        mSettingsButton.setVisibility(View.VISIBLE);
        mSettingsButton.setText(R.string.settings);

        mToolbarImage.setBackgroundColor(ContextCompat.getColor(this, R.color.mGrey12));
        mToolbarImage.setVisibility(View.VISIBLE);

        initTypeTabHost();

        getFollowedList(false);
    }

    @SupposeUiThread
    protected void setUserName() {
        mUser = User.fromJsonString(mPreferences.app.user().getOr(""));
        if (mUser != null) {
            setExtendedToolbarTitle(mUser.getName());
        } else {
            setExtendedToolbarTitle(getString(R.string.profile));
        }
    }

    @Background
    protected void getFollowedList(boolean forceReload) {

        try {
            if (forceReload) {
                mRestClient.clearCacheForNextRequestOnThisThread();
            }
            FollowedList followedItems = mRestClient.getFollowedList(mType);
            List<Brand> brandList = mRestClient.getBrands();
            List<Topic> topicList = mRestClient.getTopics();
            if (brandList != null) {
                for (Brand brand : brandList) {
                    mBrands.put(brand.getId(), brand);
                }
            }

            if (brandList != null) {
                for (Topic topic : topicList) {
                    mTopics.put(topic.getId(), topic);
                }
            }
            for (FollowedItem item : followedItems) {
                if (item.getType() == FollowedItem.Type.BRAND) {
                    if (brandList != null) {
                        for (Brand brand : brandList) {
                            if (brand.getId().equals(item.getFollowedId())) {
                                item.setName(brand.getName());
                            }
                        }
                    }
                } else if (item.getType() == FollowedItem.Type.TOPIC) {
                    for (Topic topic : topicList) {
                        if (topic.getId().equals(item.getFollowedId())) {
                            item.setName(topic.getName());
                        }
                    }
                }
            }
            setFollowedList(mType, followedItems);
        } catch (Exception e) {
            updateUI();
            if (!(e instanceof OfflineException)) {
                Logger.error(e, "error getting followed list");
            }
        }
    }

    @UiThread
    protected void setFollowedList(FollowedItem.Type type, final FollowedList list) {
        mAdapter.setFollowedList(list);
        if (type != FollowedItem.Type.BRAND) {
            mPreferences.app.edit()
                    .followedBrands().put(list.toString())
                    .apply();
        } else if (type != FollowedItem.Type.TOPIC) {
            mPreferences.app.edit()
                    .followedTopics().put(list.toString())
                    .apply();
        }
        updateUI();
    }

    @UiThread
    protected void initTypeTabHost() {

        mTypeTabLayout.removeAllTabs();

        mTypeTabLayout.addTab(mTypeTabLayout.newTab().setText(R.string.my_topics));
        mTypeTabLayout.addTab(mTypeTabLayout.newTab().setText(R.string.my_brands));

        mTypeTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    mType = FollowedItem.Type.TOPIC;
                    mNoContent.setText(R.string.follow_topic);
                } else {
                    mType = FollowedItem.Type.BRAND;
                    mNoContent.setText(R.string.follow_brand);
                }
                mFlipper.setDisplayedChild(0);
                getFollowedList(false);
                mRecyclerView.scrollToPosition(0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @UiThread
    protected void updateUI() {
        mFlipper.setDisplayedChild(mAdapter.getItemCount() > 0 ? 2 : 1);
        mRefreshLayout.setRefreshing(false);
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mRecyclerView.setItemViewCacheSize(5);
    }

    private void setRecyclerAdapter() {
        mAdapter = new FollowedListViewAdapter(null);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(View view, FollowedItem followedItem) {
        if (view.getId() == R.id.unfollow_button) {
            unfollowItem(followedItem);
        } else {
            if (followedItem.getType() == FollowedItem.Type.TOPIC) {
                ArticleListActivity_.intent(this).topic(mTopics.get(followedItem.getFollowedId())).start();
            } else if (followedItem.getType() == FollowedItem.Type.BRAND) {
                ArticleListActivity_.intent(this).brand(mBrands.get(followedItem.getFollowedId())).start();
            }
        }
    }

    @Click(R.id.extended_toolbar_button)
    protected void settingsButtonClicked() {
        Broadcast.postEvent(new PageRequest(PageRequest.Page.Settings));
    }

    @Click(R.id.follow_button)
    protected void followButtonClicked() {
        if (mType == FollowedItem.Type.TOPIC) {
            Broadcast.postEvent(new PageRequest(PageRequest.Page.TopicList));
        } else if (mType == FollowedItem.Type.BRAND) {
            Broadcast.postEvent(new PageRequest(PageRequest.Page.BrandList));
        }

    }

    @Background
    protected void unfollowItem(FollowedItem item) {
        try {
            mRestClient.unfollow(item);
            getFollowedList(true);
        } catch (Exception e) {
            Logger.error(e, "error trying to unfollow item: %d", item.getId());
        }
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
