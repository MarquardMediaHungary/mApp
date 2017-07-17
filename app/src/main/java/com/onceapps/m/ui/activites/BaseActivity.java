package com.onceapps.m.ui.activites;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.firebase.iid.FirebaseInstanceId;
import com.onceapps.core.ui.CustomAppCompatActivity;
import com.onceapps.core.util.Broadcast;
import com.onceapps.core.util.Logger;
import com.onceapps.core.util.OfflineException;
import com.onceapps.m.R;
import com.onceapps.m.api.MagazineHandler;
import com.onceapps.m.api.RestClient;
import com.onceapps.m.models.FollowedList;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.models.User;
import com.onceapps.m.ui.PageRequest;
import com.onceapps.m.ui.utils.AnimatedAppBarStateChangeListener;
import com.onceapps.m.ui.utils.AppBarStateChangeListener;
import com.onceapps.m.ui.widgets.CustomAlertDialog;
import com.onceapps.m.ui.widgets.CustomAlertDialog_;
import com.onceapps.m.ui.widgets.LoadingLayout;
import com.onceapps.m.utils.Preferences;
import com.onceapps.m.utils.Utils;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.concurrent.atomic.AtomicBoolean;

@EActivity(R.layout.activity_base)
public abstract class BaseActivity extends CustomAppCompatActivity {

    @Extra
    protected int themeId = -1;

    @Bean
    protected Preferences mPreferences;

    @Bean
    protected RestClient mRestClient;

    @Bean
    protected MagazineHandler mMagazineHandler;

    @ViewById(R.id.drawer_layout)
    protected DrawerLayout mDrawerLayout;

    @ViewById(R.id.coordinator_layout)
    protected ViewGroup mCoordinatorLayout;

    @ViewById(R.id.navigation_view)
    protected NavigationView mNavigationView;

    @ViewById(R.id.app_bar_layout)
    protected AppBarLayout mAppBarLayout;

    @ViewById(R.id.collapsing_toolbar)
    protected CollapsingToolbarLayout mToolbarParent;

    @ViewById(R.id.extended_toolbar)
    protected RelativeLayout mExtendedToolbar;

    @ViewById(R.id.extended_toolbar_title)
    protected TextView mExtendedToolbarTitle;

    @ViewById(R.id.extended_toolbar_title_image)
    protected SimpleDraweeView mExtendedToolbarTitleImage;

    @ViewById(R.id.extended_toolbar_button)
    protected Button mExtedendedToolbarButton;

    @ViewById(R.id.toolbar_image)
    protected SimpleDraweeView mToolbarImage;

    @ViewById(R.id.toolbar)
    protected Toolbar mToolbar;

    @ViewById(R.id.toolbar_title_flipper)
    protected ViewFlipper mToolbarTitleFlipper;

    @ViewById(R.id.toolbar_logo)
    protected SimpleDraweeView mToolbarLogo;

    @ViewById(R.id.toolbar_title)
    protected TextView mTitle;

    @ViewById(R.id.header_back_button)
    protected ImageButton mBackButton;

    @ViewById(R.id.header_menu_button)
    protected ImageButton mMenuButton;

    @ViewById(R.id.profile_name)
    protected TextView mProfileName;

    @ViewById(R.id.followed_count)
    protected TextView mFollowedCount;

    @ViewById(R.id.loading_layout)
    protected LoadingLayout mLoadingLayout;

    private AtomicBoolean mIsLoadingBlurShowing = new AtomicBoolean(false);

    private CustomAlertDialog mAlertDialog, mOfflineDialog;
    private AtomicBoolean mAlertDialogIsShowing = new AtomicBoolean(false);

    protected int mLogoDrawableResId = 0;

    protected boolean mIsTablet;

    protected AnimatedAppBarStateChangeListener mAnimatedAppBarStateChangeListener;

    protected AtomicBoolean mOfflineDialogDismissed = new AtomicBoolean(true);

    protected Pair<Integer, Integer> mToolbarLogoSize, mToolbarImageSize, mExtendedToolbarTitleImageSize;

    @AfterExtras
    protected void afterExtrasBase() {
        if (themeId > 0) {
            setTheme(themeId);
        }

        TypedArray a = getTheme().obtainStyledAttributes(themeId, new int[]{R.attr.themedSmallLogoDrawable});
        mLogoDrawableResId = a.getResourceId(0, R.drawable.ic_logo_small_white);
        a.recycle();
    }

    @AfterInject
    protected void afterInjectBase() {
        doCheckLoginFromBgThread();
    }

    @AfterViews
    protected void afterViewsBase() {
        initToolbar();
        setUserName();
        setFollowedCount();
    }

    @SupposeUiThread
    protected void setUserName() {
        if (mProfileName != null) {
            User user = User.fromJsonString(mPreferences.app.user().getOr(""));
            if (user != null) {
                mProfileName.setText(user.getName());
            } else {
                mProfileName.setText(R.string.profile);
            }
        }
    }

    @SupposeUiThread
    protected void setFollowedCount() {
        if (mFollowedCount != null) {
            User user = User.fromJsonString(mPreferences.app.user().getOr(""));
            if (user != null) {
                mProfileName.setText(user.getName());
            } else {
                mProfileName.setText(R.string.profile);
            }

            FollowedList followedTopics = FollowedList.fromJsonString(mPreferences.app.followedTopics().getOr("[]"));
            FollowedList followedBrands = FollowedList.fromJsonString(mPreferences.app.followedBrands().getOr("[]"));

            int count = 0;
            if (followedBrands != null) count += followedBrands.size();
            if (followedTopics != null) count += followedTopics.size();
            mFollowedCount.setText(getString(R.string.follow_count, count));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Broadcast.register(this);
        hideLoadingBlur();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Broadcast.unregister(this);
        hideLoadingBlur();
    }

    @Background
    protected void doCheckLoginFromBgThread() {
        try {
            checkLogin();
        } catch (Exception e) {
            Logger.warn(e, "error logging in");
        }
    }

    @SupposeBackground
    protected void checkLogin() throws Exception {
        if (mPreferences.app.authToken().exists()) {
            User user = mRestClient.getUser();
            if (user != null) {
                mPreferences.app.edit()
                        .user().put(user.toString())
                        .apply();
            }
        }
    }

    private void initToolbar() {

        mIsTablet = getResources().getBoolean(R.bool.is_tablet);

        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        setBackButtonVisibility(!isTaskRoot());
        setTitle(null);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float percentage = 1.0f - ((float) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange() * 2);
                mExtendedToolbarTitle.setAlpha(percentage);
                mExtendedToolbarTitleImage.setAlpha(percentage);
                if (mExtedendedToolbarButton != null) {
                    mExtedendedToolbarButton.setAlpha(percentage);
                    mExtedendedToolbarButton.setEnabled(percentage > 0.5f);
                }
            }
        });

        mAnimatedAppBarStateChangeListener = new AnimatedAppBarStateChangeListener(mAppBarLayout, null);

        mToolbarLogo.setImageURI(ImageRequestBuilder.newBuilderWithResourceId(mLogoDrawableResId).getSourceUri());

        mAppBarLayout.addOnOffsetChangedListener(mAnimatedAppBarStateChangeListener);

        if (mToolbarLogoSize == null) {
            mToolbarLogo.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mToolbarLogoSize = new Pair<>(mToolbarLogo.getMeasuredWidth(), mToolbarLogo.getMeasuredHeight());
                    mToolbarLogo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        if (mToolbarImageSize == null) {
            if (mIsTablet) {
                mToolbarImage.setAspectRatio(1.8f);
            } else {
                mToolbarImage.setAspectRatio(1f);
            }
            mToolbarImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mToolbarImageSize = new Pair<>(mToolbarImage.getMeasuredWidth(), mToolbarImage.getMeasuredHeight());
                    mToolbarImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        if (mToolbarLogoSize == null) {
            mToolbarLogo.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mToolbarLogoSize = new Pair<>(mToolbarLogo.getMeasuredWidth(), mToolbarLogo.getMeasuredHeight());
                    mToolbarLogo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        if (mExtendedToolbarTitleImageSize == null) {
            mExtendedToolbarTitleImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mExtendedToolbarTitleImageSize = new Pair<>(mExtendedToolbarTitleImage.getMeasuredWidth(), mExtendedToolbarTitleImage.getMeasuredHeight());
                    mExtendedToolbarTitleImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    @Override
    @UiThread
    public void onBackPressed() {
        if (mDrawerLayout != null && mNavigationView != null && mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
            return;
        }
        super.onBackPressed();
    }

    @Subscribe
    public void onPageRequest(PageRequest pageRequest) {
        switch (pageRequest.getPage()) {

            case AllArticles:
                ArticleListActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP).start();
                break;
            case BrandList:
                BrandListActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).start();
                break;
            case TopicList:
                TopicListActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).start();
                break;
            case MyMagazine:
                ArticleListActivity_.intent(this).isMyMagazine(true).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP).start();
                break;
            case Favorites:
                ArticleListActivity_.intent(this).isFavoriteArticles(true).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP).start();
                break;
            case MyIssues:
                Magazine magazine = (Magazine) pageRequest.getExtras().getSerializable(MyIssuesActivity.KEY_MAGAZINE_TO_ADD);
                MyIssuesActivity_.intent(this).magazineToAdd(magazine).flags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).start();
                break;
            case AllIssues:
                AllBrandsMagazineListActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).start();
                break;
            case Impress:
                ImpressActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).start();
                break;
            case Settings:
                SettingsActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).start();
                break;
            case FollowedList:
                FollowedListActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).start();
                break;
            case FbVideoList:
                FbVideoListActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP).start();
                break;
            case Back:
                onBackPressed();
                break;
            case Offline:
                showOfflineDialog();
                break;
            case ShowDialog:
                if (!pageRequest.getExtras().containsKey(CustomAlertDialog.KEY_POSITIVE_PAGE_REQUEST)
                        && !pageRequest.getExtras().containsKey(CustomAlertDialog.KEY_NEGATIVE_PAGE_REQUEST)) {
                    showAlertDialog(pageRequest.getExtras().getInt(CustomAlertDialog.KEY_TITLE),
                            pageRequest.getExtras().getInt(CustomAlertDialog.KEY_MESSAGE));
                } else {
                    showAlertDialog(pageRequest.getExtras().getInt(CustomAlertDialog.KEY_TITLE),
                            pageRequest.getExtras().getInt(CustomAlertDialog.KEY_MESSAGE),
                            (PageRequest) pageRequest.getExtras().getSerializable(CustomAlertDialog.KEY_POSITIVE_PAGE_REQUEST),
                            (PageRequest) pageRequest.getExtras().getSerializable(CustomAlertDialog.KEY_NEGATIVE_PAGE_REQUEST));
                }

                return;
            case HideDialog:
                dismissAlertDialog();
                return;
            case StoragePermissionDenied:
                StoragePermissionRequestActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP).start();
                return;
            case ClientVersionDeprecated:
                if (!mPreferences.app.deprecatedMessageShown().get()) {
                    showAlertDialog(
                            R.string.client_version_deprecated_title,
                            R.string.client_version_deprecated_message,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if (mAlertDialog != null) {
                                        mPreferences.app.deprecatedMessageShown().put(true);
                                        dismissAlertDialog();
                                    }

                                    final String appPackageName = BaseActivity.this.getPackageName();
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }
                                }
                            },
                            new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (mAlertDialog != null) {
                                        mPreferences.app.deprecatedMessageShown().put(true);
                                        dismissAlertDialog();
                                    }
                                }
                            }
                    );
                }
                return;
            case ClientVersionUnsupported:
                showAlertDialog(
                        R.string.client_version_unsupported_title,
                        R.string.client_version_unsupported_message,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String appPackageName = BaseActivity.this.getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        },
                        null
                );
        }
    }

    @Subscribe
    public void onOutOfMemoryError(OutOfMemoryError error) {
        showAlertDialog(R.string.error, R.string.out_of_memory_error_message, new PageRequest(PageRequest.Page.Back), null);
    }

    protected final void setBackButtonVisibility(boolean isVisible) {
        mBackButton.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    protected final void setMenuButtonVisibility(boolean isVisible) {
        mMenuButton.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    protected final void setTitle(String text) {
        if (!TextUtils.isEmpty(text)) {
            mTitle.setText(text);
            if (mToolbarTitleFlipper.getDisplayedChild() != 1)
                mToolbarTitleFlipper.setDisplayedChild(1);
        } else {
            mToolbarLogo.setImageURI(ImageRequestBuilder.newBuilderWithResourceId(mLogoDrawableResId).getSourceUri());
            if (mToolbarTitleFlipper.getDisplayedChild() != 0)
                mToolbarTitleFlipper.setDisplayedChild(0);
        }
    }

    protected final void setTitleImage(String url) {
        if (mToolbarLogoSize == null) {
            Utils.loadImageResized(mToolbarLogo, url);
        } else {
            Utils.loadImageResized(mToolbarLogo, url, mToolbarLogoSize.first, mToolbarLogoSize.second);
        }
        if (mToolbarTitleFlipper.getDisplayedChild() != 0) {
            mToolbarTitleFlipper.setDisplayedChild(0);
        }
    }

    protected final void setExtendedToolbarTitle(String text) {
        if (!TextUtils.isEmpty(text)) {
            mExtendedToolbarTitle.setText(text);
        }
    }

    protected final void setExtendedToolbarTitleImage(String url) {
        if (mExtendedToolbarTitleImageSize == null) {
            Utils.loadImageResized(mExtendedToolbarTitleImage, url);
        } else {
            Utils.loadImageResized(mExtendedToolbarTitleImage, url, mToolbarImageSize.first, mToolbarImageSize.second);
        }
    }

    private void menuItemClicked(PageRequest.Page page) {
        Broadcast.postEvent(new PageRequest(page));
        onBackPressed();
    }

    @Click(R.id.header_back_button)
    protected void backClicked() {
        onBackPressed();
    }

    @Click(R.id.header_menu_button)
    protected void menuClicked() {
        mDrawerLayout.openDrawer(GravityCompat.END);
        setUserName();
        setFollowedCount();
    }

    @Click(R.id.menu_item_all_articles)
    protected void allArticlesMenuItemClicked() {
        menuItemClicked(PageRequest.Page.AllArticles);
    }

    @Click(R.id.menu_item_all_topics)
    protected void allTopicsMenuItemClicked() {
        menuItemClicked(PageRequest.Page.TopicList);
    }

    @Click(R.id.menu_item_brands)
    protected void brandsMenuItemClicked() {
        menuItemClicked(PageRequest.Page.BrandList);
    }

    @Click(R.id.menu_item_my_magazine)
    protected void myMagazineMenuItemClicked() {
        menuItemClicked(PageRequest.Page.MyMagazine);
    }

    @Click(R.id.menu_item_profile)
    protected void profileMenuItemClicked() {
        menuItemClicked(PageRequest.Page.FollowedList);
    }

    @Click(R.id.menu_item_fb_video_list)
    protected void fbVideoListMenuItemClicked() {
        menuItemClicked(PageRequest.Page.FbVideoList);
    }

    @Click(R.id.menu_item_favorites)
    protected void favoritesMenuItemClicked() {
        menuItemClicked(PageRequest.Page.Favorites);
    }

    @Click(R.id.menu_item_all_issues)
    protected void allIssuesMenuItemClicked() {
        menuItemClicked(PageRequest.Page.AllIssues);
    }

    @Click(R.id.menu_item_downloads)
    protected void downloadsMenuItemClicked() {
        menuItemClicked(PageRequest.Page.MyIssues);
    }

    @Click(R.id.menu_impress)
    protected void menuImpressClicked() {
        menuItemClicked(PageRequest.Page.Impress);
    }

    @SupposeUiThread
    protected void showAlertDialog(final int titleStringID, final int messageStringID) {

        showAlertDialog(
                titleStringID,
                messageStringID,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissAlertDialog();
                    }
                },
                null);
    }

    @SupposeUiThread
    protected void showAlertDialog(final int titleStringID, final int messageStringID,
                                   View.OnClickListener positiveListener,
                                   View.OnClickListener negativeListener) {

        showAlertDialog(
                getString(titleStringID),
                getString(messageStringID),
                positiveListener,
                negativeListener);
    }

    @SupposeUiThread
    protected void showAlertDialog(final String titleString, final String messageString,
                                   View.OnClickListener positiveListener,
                                   View.OnClickListener negativeListener) {

        showAlertDialog(
                titleString,
                messageString,
                -1,
                -1,
                positiveListener,
                negativeListener);
    }

    @SupposeUiThread
    protected void showAlertDialog(final int titleStringID, final int messageStringID,
                                   final int positiveButtonStringId, final int negativeButtonStringId,
                                   View.OnClickListener positiveListener,
                                   View.OnClickListener negativeListener) {

        showAlertDialog(
                getString(titleStringID),
                getString(messageStringID),
                positiveButtonStringId,
                negativeButtonStringId,
                positiveListener,
                negativeListener
                );
    }

    @SupposeUiThread
    protected void showAlertDialog(final int titleStringID, final int messageStringID,
                                   final PageRequest positivePageRequest,
                                   final PageRequest negativePageRequest) {

        View.OnClickListener positiveListener = null;
        if(positivePageRequest != null) {
            positiveListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Broadcast.postEvent(positivePageRequest);
                    // in case of page request is hide dialog
                    dismissAlertDialog();
                }
            };
        }

        View.OnClickListener negativeListener = null;
        if(positivePageRequest != null) {
            negativeListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Broadcast.postEvent(negativePageRequest);
                    dismissAlertDialog();
                }
            };
        }

        showAlertDialog(
                titleStringID,
                messageStringID,
                positiveListener,
                negativeListener
        );
    }

    @SupposeUiThread
    protected void showAlertDialog(final String titleString, final String messageString,
                                   final int positiveButtonStringId, final int negativeButtonStringId,
                                   View.OnClickListener positiveListener,
                                   View.OnClickListener negativeListener) {
            if(mAlertDialog != null && mAlertDialog.isAdded() || mAlertDialogIsShowing.get()) {
                return;
            }
            else if (mAlertDialog == null) {
                mAlertDialog = CustomAlertDialog_.builder().build();
            }

            mAlertDialog.setTitle(titleString);
            mAlertDialog.setMessage(messageString);
            mAlertDialog.setCancelable(false);

            if(positiveButtonStringId > 0) mAlertDialog.setPositiveButtonText(positiveButtonStringId);
            if(negativeButtonStringId > 0) mAlertDialog.setNegativeButtonText(negativeButtonStringId);

            if (negativeListener != null) {
                mAlertDialog.setNegativeButtonVisible(true);
                mAlertDialog.setNegativeButtonListener(negativeListener);
            } else {
                mAlertDialog.setNegativeButtonVisible(false);
            }

            if (positiveListener != null) {
                mAlertDialog.setPositiveButtonVisible(true);
                mAlertDialog.setPositiveButtonListener(positiveListener);
            } else {
                mAlertDialog.setPositiveButtonVisible(false);
            }

        if (mAlertDialog.getDialog() == null || !mAlertDialog.getDialog().isShowing() || mAlertDialogIsShowing.get()) {
            mAlertDialogIsShowing.set(true);
            mAlertDialog.show(getFragmentManager(), null);
        }
    }

    @UiThread
    protected void dismissAlertDialog() {
        mAlertDialogIsShowing.set(false);
        if(mAlertDialog != null && mAlertDialog.isAdded()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }

    @UiThread
    protected void dismissOfflineDialog() {
        if(mOfflineDialog != null && mOfflineDialog.isAdded()) {
            mOfflineDialog.dismiss();
            mOfflineDialog = null;
        }
    }

    private void showOfflineDialog() {
        dismissOfflineDialog();
        if (mOfflineDialog == null) {
            mOfflineDialog = CustomAlertDialog_.builder().build();
        }

        if (mOfflineDialogDismissed.getAndSet(false)) {
            mOfflineDialog.setTitle(R.string.error);
            mOfflineDialog.setMessage(getString(R.string.offline_message));
            mOfflineDialog.setCancelable(false);

            mOfflineDialog.setNegativeButtonVisible(false);

            mOfflineDialog.setPositiveButtonVisible(true);
            mOfflineDialog.setPositiveButtonListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissOfflineDialog();
                    mOfflineDialogDismissed.set(true);
                    hideLoadingBlur();
                }
            });
            if (mOfflineDialog.getDialog() == null || !mOfflineDialog.getDialog().isShowing()) {
                mOfflineDialog.show(getFragmentManager(), null);
            }
        }
    }

    @SupposeUiThread
    protected void lockAppBarClosed() {
        mAnimatedAppBarStateChangeListener.collapse();
        mExtendedToolbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAnimatedAppBarStateChangeListener.getCurrentState() == AppBarStateChangeListener.State.COLLAPSED) {
                    mExtendedToolbar.setVisibility(View.GONE);
                } else {
                    lockAppBarClosed();
                }
            }
        }, 500);
    }

    @SupposeUiThread
    protected void unlockAppBarOpen() {
        mAnimatedAppBarStateChangeListener.expand();
        mExtendedToolbar.setVisibility(View.VISIBLE);
    }

    protected void applyLoadingBlur() {
        mCoordinatorLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mCoordinatorLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                mCoordinatorLayout.buildDrawingCache();

                Bitmap bmp = mCoordinatorLayout.getDrawingCache();
                Utils.blur(BaseActivity.this, bmp, mLoadingLayout);
                return true;
            }
        });
    }

    @UiThread
    protected void onLoginFailure(String message) {
        setFormEnabled(true);
        showAlertDialog(
                getString(R.string.login_error_title),
                message,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissAlertDialog();
                    }
                },
                null);
    }

    @Background
    protected void onLoginSuccess() {
        try {
            User user = mRestClient.getUser();
            if (user != null) {
                mPreferences.app.user().put(user.toString());
            } else {
                Logger.warn("Login successful, but error getting user");
            }
        } catch (Exception e) {
            Logger.warn(e, "Login successful, but error getting user");
        }

        try {
            String deviceToken = FirebaseInstanceId.getInstance().getToken();
            if(!TextUtils.isEmpty(deviceToken)) {
                Logger.debug("deviceToken: %s", deviceToken);
                mRestClient.sendDeviceToken(deviceToken);
            }
            else {
                Logger.warn("no device token on client side");
            }

        } catch (Exception e) {
            if(!(e instanceof OfflineException))
                Logger.warn(e, "error sending device token");
        }

        ArticleListActivity_.intent(this).start();
        finishAffinity();
    }

    protected void setFormEnabled(boolean enabled) {
    }

    @UiThread
    protected void showLoadingBlur() {
        if(!mIsLoadingBlurShowing.getAndSet(true)) {
            applyLoadingBlur();
            mLoadingLayout.show();
        }
    }

    @UiThread
    protected void hideLoadingBlur(boolean animated) {
        if(mLoadingLayout != null && mLoadingLayout.getVisibility() == View.VISIBLE) {
            if(animated)
                mLoadingLayout.hide();
            else
                mLoadingLayout.hideImmediately();
        }
        mIsLoadingBlurShowing.set(false);
    }

    protected void hideLoadingBlur() {
        hideLoadingBlur(true);
    }

    @UiThread
    protected void setEditTextError(EditText editText, String message) {
        if (editText != null) {
            editText.setError(message);
        }
    }
}