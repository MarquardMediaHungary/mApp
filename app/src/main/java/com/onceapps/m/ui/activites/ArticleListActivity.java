package com.onceapps.m.ui.activites;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.onceapps.core.util.Broadcast;
import com.onceapps.core.util.Logger;
import com.onceapps.core.util.OfflineException;
import com.onceapps.m.R;
import com.onceapps.m.models.Article;
import com.onceapps.m.models.Brand;
import com.onceapps.m.models.Favorite;
import com.onceapps.m.models.FavoriteList;
import com.onceapps.m.models.FollowedItem;
import com.onceapps.m.models.FollowedList;
import com.onceapps.m.models.Topic;
import com.onceapps.m.ui.ItemOffsetDecoration;
import com.onceapps.m.ui.PageRequest;
import com.onceapps.m.ui.adapters.ArticleListViewAdapter;
import com.onceapps.m.ui.utils.AppBarStateChangeListener;
import com.onceapps.m.utils.Utils;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


@EActivity(R.layout.activity_articlelist)
public class ArticleListActivity extends BaseActivity implements ArticleListViewAdapter.OnItemClickListener {

    @Extra
    protected Topic topic;

    @Extra
    protected Brand brand;

    @Extra
    protected Boolean isMyMagazine = false;

    @Extra
    protected Boolean isFavoriteArticles = false;

    @ViewById(R.id.recycler)
    protected RecyclerView mRecyclerView;

    @ViewById(R.id.topics_tabs)
    protected TabLayout mTopicsTabLayout;

    @ViewById(R.id.brands_tabs)
    protected TabLayout mBrandsTabLayout;

    @ViewById(R.id.magazine_fab)
    protected FloatingActionButton mMagazineFAB;

    @ViewById(R.id.loading_flipper)
    protected ViewFlipper mFlipper;

    @ViewById(R.id.pull_refresh)
    protected SwipeRefreshLayout mRefreshLayout;

    @ViewById(R.id.load_more)
    protected ViewGroup mLoadingMoreLayout;

    @ViewById(R.id.extended_toolbar_button)
    protected Button mFollowButton;

    @ViewById(R.id.no_content)
    protected TextView mNoContent;

    @ViewById(R.id.add_button)
    protected LinearLayout mAddButton;

    @ViewById(R.id.add_text)
    protected TextView mAddTextView;

    @InstanceState
    protected Parcelable mListViewState;

    @InstanceState
    protected Integer mSelectedTopicsTab, mSelectedBrandsTab;

    protected FollowedList mFollowedBrands, mFollowedTopics;

    protected FavoriteList mFavorites;

    private ArticleListViewAdapter mAdapter;

    private List<Brand> mBrandList = new ArrayList<>();

    private RecyclerView.LayoutManager mLayoutManager;
    private final ArticleListOnScrollListener mOnScrollListener = new ArticleListOnScrollListener();

    private final ReentrantLock mLoadMoreLock = new ReentrantLock();

    private Brand mOriginalBrand;
    private Topic mOriginalTopic;

    @Override
    protected void onPause() {
        super.onPause();
        mListViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mSelectedTopicsTab = mTopicsTabLayout.getSelectedTabPosition();
        mSelectedBrandsTab = mBrandsTabLayout.getSelectedTabPosition();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSelectedTopicsTab != null && mSelectedTopicsTab > -1 && mTopicsTabLayout != null && mTopicsTabLayout.getTabCount() > mSelectedTopicsTab) {
            TabLayout.Tab tab = mTopicsTabLayout.getTabAt(mSelectedTopicsTab);
            if (tab != null) {
                tab.select();
            }
        }
        if (mSelectedBrandsTab != null && mSelectedBrandsTab > -1 && mBrandsTabLayout != null && mBrandsTabLayout.getTabCount() > mSelectedBrandsTab) {
            TabLayout.Tab tab = mBrandsTabLayout.getTabAt(mSelectedBrandsTab);
            if (tab != null) {
                tab.select();
            }
        }
        if (mListViewState != null) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mListViewState);
            mRecyclerView.invalidate();
        }

        getFavorites();
        getFollowedList();
        if (isFavoriteArticles || isMyMagazine) {
            mFlipper.setDisplayedChild(0);
            getArticles(true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        topic = null;
        brand = null;
        isMyMagazine = false;
        isFavoriteArticles = false;
        mListViewState = null;
        setIntent(intent);
        init();
    }

    @AfterViews
    protected void afterViews() {
        init();
    }

    @AfterExtras
    protected void afterExtras() {
        mOriginalBrand = brand;
        mOriginalTopic = topic;
    }

    private void init() {
        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        mLayoutManager = new StaggeredGridLayoutManager(isTablet ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);

        setTitle(isMyMagazine ?
                getString(R.string.menu_my_magazine) :
                isFavoriteArticles ?
                        getString(R.string.menu_favorites) :
                        "");

        mFlipper.setDisplayedChild(0);
        mAddTextView.setText(isMyMagazine ?
                getString(R.string.follow_stuff) :
                isFavoriteArticles ?
                        getString(R.string.save_articles) :
                        "");

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getArticles(true);
            }
        });
        mRefreshLayout.setColorSchemeResources(R.color.mGreen, R.color.mYellow, R.color.mPink, R.color.mBlue01, R.color.mRed);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {

            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.COLLAPSED) {
                    if (mOriginalBrand != null) {
                        setTitle(mOriginalBrand.getName());
                    } else if (mOriginalTopic != null) {
                        setTitle(mOriginalTopic.getName());
                    }
                } else if (mCurrentState == State.COLLAPSED) {
                    setTitle("");
                }
            }
        });

        initRecyclerView();
        setRecyclerAdapter();

        mFollowButton.setVisibility(View.VISIBLE);

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

            initTopicsTabHost();
        } else if (topic != null) {

            mMagazineFAB.setVisibility(View.GONE);
            mExtendedToolbar.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(topic.getColor())) {
                mExtendedToolbar.setBackgroundColor(Color.parseColor(topic.getColor()));
                mToolbarParent.setContentScrimColor(Color.parseColor(topic.getColor()));
            }

            if (!TextUtils.isEmpty(topic.getImage())) {
                mToolbarImage.setImageURI(Uri.parse(topic.getImage()));
            }
            setExtendedToolbarTitle(topic.getName());
            getBrands();
        } else {
            mMagazineFAB.setVisibility(!isFavoriteArticles && !isMyMagazine ? View.VISIBLE : View.GONE);
            mExtendedToolbar.setVisibility(View.GONE);
            mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            mTopicsTabLayout.setVisibility(View.GONE);
            mBrandsTabLayout.setVisibility(View.GONE);

            mFollowButton.setVisibility(View.GONE);
        }

        getArticles(false);
        getFollowedList();
        getFavorites();
    }

    @Background
    protected void getBrands() {

        try {
            mBrandList = mRestClient.getBrands();
            initBrandsTabHost();
        } catch (Exception e) {
            Logger.error(e, "error getting brands");
        }
    }

    @Background
    protected void getArticles(boolean forceReload) {

        try {
            if (forceReload) {
                checkLogin();
                mRestClient.clearCacheForNextRequestOnThisThread();
            }
            List<Article> articles = new ArrayList<>();
            if (isFavoriteArticles) {
                FavoriteList favoriteList = mRestClient.getFavorites();
                if (favoriteList != null) {
                    for (Favorite favorite : favoriteList) {
                        if (favorite.getArticle() != null) {
                            articles.add(favorite.getArticle());
                        }
                    }
                }
            } else {
                articles = isMyMagazine ?
                        mRestClient.getMyMagazineArticleList(null) :
                        mRestClient.getArticles(brand, topic, null);
            }
            setArticles(articles);
            forceSelectedBrandOnArticles(articles);
        } catch (Exception e) {
            updateUI();
            if (!(e instanceof OfflineException)) {
                Logger.error(e, "error getting articles");
            }
        }
    }

    @Background
    protected void getFollowedList() {
        try {
            mFollowedBrands = mRestClient.getFollowedList(FollowedItem.Type.BRAND);
            if (mFollowedBrands != null) {
                mPreferences.app.edit()
                        .followedBrands().put(mFollowedBrands.toString())
                        .apply();
                if (mOriginalBrand != null)
                    updateFollowButtonStatus(mFollowedBrands.isFollowing(mOriginalBrand.getId()));
            }

            mFollowedTopics = mRestClient.getFollowedList(FollowedItem.Type.TOPIC);
            if (mFollowedTopics != null) {
                mPreferences.app.edit()
                        .followedTopics().put(mFollowedTopics.toString())
                        .apply();
                if (mOriginalTopic != null)
                    updateFollowButtonStatus(mFollowedTopics.isFollowing(mOriginalTopic.getId()));
            }
        } catch (Exception e) {
            Logger.warn(e, "error getting followed list");
        }
    }


    @Background
    protected void getFavorites() {
        try {
            mFavorites = mRestClient.getFavorites();
            if (mFavorites != null) setFavorites(mFavorites);
        } catch (Exception e) {
            Logger.error(e, "error getting favorites");
        }
    }

    @Background
    protected void addToFavorites(@NonNull View favoriteButton, Article article) {
        try {
            activateFavoriteButton(favoriteButton, true);
            Favorite favorite = mRestClient.addToFavorites(article.getId());
            if (favorite != null) {
                if (!TextUtils.isEmpty(favorite.getMessage())) {
                    Logger.warn("error adding to favorites: %s", favorite.getMessage());
                }
            } else {
                Logger.warn("error adding to favorites: no response");
            }
            getFavorites();
            if (isFavoriteArticles) {
                getArticles(true);
            }
        } catch (Exception e) {
            Logger.error(e, "error adding to favorites");
            activateFavoriteButton(favoriteButton, false);
        } finally {
            enableFavoriteButton(favoriteButton, true);
        }
    }

    @Background
    protected void removeFromFavorites(@NonNull View favoriteButton, Favorite favorite) {
        try {
            activateFavoriteButton(favoriteButton, false);
            mRestClient.removeFromFavorites(favorite);
            getFavorites();
        } catch (Exception e) {
            Logger.error(e, "error removing favorites");
            activateFavoriteButton(favoriteButton, true);
        } finally {
            enableFavoriteButton(favoriteButton, true);
        }
    }

    @UiThread
    protected void setArticles(final List<Article> list) {
        mAdapter.setArticles(list);
        updateUI();
    }

    @UiThread
    protected void addArticles(final List<Article> list) {
        mAdapter.addArticles(list);
        updateUI();
    }

    @UiThread
    protected void setFavorites(final FavoriteList favorites) {
        if (mAdapter != null) mAdapter.setFavorites(favorites);
    }

    @UiThread
    protected void updateFollowButtonStatus(boolean isFollowing) {
        Drawable icFollow = ContextCompat.getDrawable(this, R.drawable.ic_follow_white);
        Drawable icUnfollow = ContextCompat.getDrawable(this, R.drawable.ic_close_black);
        mFollowButton.setCompoundDrawablesWithIntrinsicBounds(
                isFollowing ? icUnfollow : icFollow,
                null,
                null,
                null
        );
        mFollowButton.setActivated(isFollowing);
        mFollowButton.setText(isFollowing ? R.string.followed : R.string.follow);
    }

    //if it already contains it (or doesn't contain any)
    private void forceBrandOnArticle(@NonNull Article article, @NonNull Brand brand) {
        if (article.getBrands() != null || article.getBrands().isEmpty() || article.getBrands().contains(brand)) {
            List<Brand> forcedList = new ArrayList<>();
            forcedList.add(brand);
            article.setBrands(forcedList);
        }
    }

    private void forceSelectedBrandOnArticles(List<Article> articles) {
        if (brand != null) {
            for (Article article : articles) {
                forceBrandOnArticle(article, brand);
            }
        }
    }

    @UiThread
    protected void initTopicsTabHost() {
        if (brand.getTopics() != null && brand.getTopics().size() > 1) {
            mTopicsTabLayout.setVisibility(View.VISIBLE);
            mTopicsTabLayout.addTab(mTopicsTabLayout.newTab().setText(R.string.tab_name_all_topics));
            for (int i = 0; i < brand.getTopics().size(); i++) {
                mTopicsTabLayout.addTab(mTopicsTabLayout.newTab().setText(brand.getTopics().get(i).getName()));
            }

            mTopicsTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() > 0 && tab.getPosition() <= brand.getTopics().size()) {
                        topic = brand.getTopics().get(tab.getPosition() - 1);
                    } else {
                        topic = null;
                    }
                    mFlipper.setDisplayedChild(0);
                    getArticles(false);
                    mRecyclerView.scrollToPosition(0);
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

    @UiThread
    protected void initBrandsTabHost() {
        if (mBrandList != null && mBrandList.size() > 1) {
            mBrandsTabLayout.setVisibility(View.VISIBLE);

            mBrandsTabLayout.setSelectedTabIndicatorColor(Color.parseColor(topic.getColor()));
            mBrandsTabLayout.setTabTextColors(R.color.mGrey03, Color.parseColor(topic.getColor()));

            mBrandsTabLayout.addTab(mBrandsTabLayout.newTab().setText(R.string.tab_name_all_topics));
            for (int i = 0; i < mBrandList.size(); i++) {
                mBrandsTabLayout.addTab(mBrandsTabLayout.newTab().setText(mBrandList.get(i).getName()));
            }

            mBrandsTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() > 0 && tab.getPosition() <= mBrandList.size()) {
                        brand = mBrandList.get(tab.getPosition() - 1);
                    } else {
                        brand = null;
                    }
                    mFlipper.setDisplayedChild(0);
                    getArticles(false);
                    mRecyclerView.scrollToPosition(0);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        } else {
            mBrandsTabLayout.setVisibility(View.GONE);
        }
    }

    @UiThread
    protected void updateUI() {
        if (mAdapter.getItemCount() == 0) {
            if (isFavoriteArticles || isMyMagazine) {
                mFlipper.setDisplayedChild(2);
            } else {
                mFlipper.setDisplayedChild(1);
            }
        } else {
            mFlipper.setDisplayedChild(3);
        }
        mRefreshLayout.setRefreshing(false);
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mRecyclerView.setItemViewCacheSize(5);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.list_margin);
        mRecyclerView.addItemDecoration(itemDecoration);
    }

    private void setRecyclerAdapter() {
        mAdapter = new ArticleListViewAdapter(this, null);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(View view, final Article article) {
        if (view.getId() == R.id.favorite) {
            enableFavoriteButton(view, false);
            if (mFavorites != null) {
                if (mFavorites.isFavorite(article)) {
                    Favorite favorite = mFavorites.getFavoriteByArticleId(article);
                    if (favorite != null) {
                        removeFromFavorites(view, favorite);
                        if (isFavoriteArticles && favorite.getArticle() != null) {
                            mAdapter.removeArticle(favorite.getArticle());
                            mFlipper.setDisplayedChild(mAdapter.getItemCount() > 0 ? 3 : 2);

                            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, R.string.article_removed, Snackbar.LENGTH_LONG)
                                    .setActionTextColor(ContextCompat.getColor(this, R.color.mWhite))
                                    .setAction(R.string.undo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            addToFavorites(view, article);
                                        }
                                    });
                            snackbar.show();
                        }
                    } else {
                        Logger.warn("favorite not found");
                    }
                } else {
                    addToFavorites(view, article);
                }
            }
        } else {
            ArticleActivity_.intent(this).article(article).start();
        }
    }

    @Click(R.id.magazine_fab)
    protected void magazineButtonClicked() {
        if (brand != null) {
            MagazineListActivity_.intent(this).brand(brand).start();
        } else {
            AllBrandsMagazineListActivity_.intent(this).start();
        }
    }

    @Click(R.id.extended_toolbar_button)
    protected void followButtonClicked() {
        enableFollowButton(false);
        if (mOriginalBrand != null) {
            if (mFollowedBrands != null && mFollowedBrands.isFollowing(mOriginalBrand.getId())) {
                unfollowBrand();
            } else {
                followBrand();
            }
        } else if (mOriginalTopic != null) {
            if (mFollowedTopics != null && mFollowedTopics.isFollowing(mOriginalTopic.getId())) {
                unfollowTopic();
            } else {
                followTopic();
            }
        }
    }

    @Click(R.id.add_button)
    protected void addButtonClicked() {
        if (isFavoriteArticles) {
            Broadcast.postEvent(new PageRequest(PageRequest.Page.AllArticles));
        } else if (isMyMagazine) {
            Broadcast.postEvent(new PageRequest(PageRequest.Page.BrandList));
        }
    }

    @Background
    protected void followBrand() {
        try {
            updateFollowButtonStatus(true);
            mRestClient.followBrand(mOriginalBrand);
            getFollowedList();
        } catch (Exception e) {
            updateFollowButtonStatus(false);
            Logger.error(e, "error trying to follow brand: %s", mOriginalBrand.getName());
        } finally {
            enableFollowButton(true);
        }
    }

    @Background
    protected void followTopic() {
        try {
            updateFollowButtonStatus(true);
            mRestClient.followTopic(mOriginalTopic);
            getFollowedList();
        } catch (Exception e) {
            updateFollowButtonStatus(false);
            Logger.error(e, "error trying to follow topic: %s", mOriginalTopic.getName());
        } finally {
            enableFollowButton(true);
        }
    }

    @Background
    protected void unfollowBrand() {
        try {
            updateFollowButtonStatus(false);
            FollowedItem itemToUnfollow = null;
            if (mFollowedBrands != null) {
                for (FollowedItem item : mFollowedBrands) {
                    if (item.getFollowedId().equals(mOriginalBrand.getId())) {
                        itemToUnfollow = item;
                    }
                }
            } else {
                updateFollowButtonStatus(true);
                Logger.warn("error trying to unfollow brand: %s - item not found", mOriginalBrand.getName());
            }
            if (itemToUnfollow != null) {
                mRestClient.unfollow(itemToUnfollow);
                getFollowedList();
            } else {
                updateFollowButtonStatus(true);
                Logger.warn("error trying to unfollow brand: %s - item not found", mOriginalBrand.getName());
            }
        } catch (Exception e) {
            updateFollowButtonStatus(true);
            Logger.error(e, "error trying to unfollow brand: %s", mOriginalBrand.getName());
        } finally {
            enableFollowButton(true);
        }
    }

    @Background
    protected void unfollowTopic() {
        try {
            updateFollowButtonStatus(false);
            FollowedItem itemToUnfollow = null;
            if (mFollowedTopics != null) {
                for (FollowedItem item : mFollowedTopics) {
                    if (item.getFollowedId().equals(mOriginalTopic.getId())) {
                        itemToUnfollow = item;
                    }
                }
            } else {
                updateFollowButtonStatus(true);
                Logger.warn("error trying to unfollow topic: %s - item not found", mOriginalTopic.getName());
            }
            if (itemToUnfollow != null) {
                mRestClient.unfollow(itemToUnfollow);
                getFollowedList();
            } else {
                updateFollowButtonStatus(true);
                Logger.warn("error trying to unfollow topic: %s - item not found", mOriginalTopic.getName());
            }
        } catch (Exception e) {
            updateFollowButtonStatus(true);
            Logger.error(e, "error trying to unfollow topic: %s", mOriginalTopic.getName());
        } finally {
            enableFollowButton(true);
        }
    }

    @Background
    protected void onLastItemReached() {
        if (mLoadMoreLock.tryLock()) {
            displayLoadMore(true);

            try {
                addArticles(isMyMagazine ?
                        mRestClient.getMyMagazineArticleList(mAdapter.getLastItem().getDate()) :
                        mRestClient.getArticles(brand, topic, mAdapter.getLastItem().getDate()));

            } catch (Exception e) {
                Logger.debug(e, "load more item failed");
            } finally {
                displayLoadMore(false);
                mLoadMoreLock.unlock();
            }
        }

    }

    @UiThread
    protected void displayLoadMore(final boolean visible) {
        mLoadingMoreLayout.setVisibility(View.VISIBLE);
        mLoadingMoreLayout
                .animate()
                .alpha(visible ? 1.0f : 0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
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

            // if last item reached
            if (!recyclerView.canScrollVertically(1) && !isFavoriteArticles) {
                onLastItemReached();
            }
        }
    }

    @UiThread
    protected void enableFavoriteButton(@NonNull View favoriteButton, boolean isEnabled) {
        favoriteButton.setEnabled(isEnabled);
    }

    @UiThread
    protected void activateFavoriteButton(@NonNull View favoriteButton, boolean isActivated) {
        favoriteButton.setActivated(isActivated);
    }

    @UiThread
    protected void enableFollowButton(boolean isEnabled) {
        mFollowButton.setEnabled(isEnabled);
    }
}
