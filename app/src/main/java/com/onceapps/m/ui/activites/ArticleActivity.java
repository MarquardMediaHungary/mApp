package com.onceapps.m.ui.activites;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onceapps.core.util.Logger;
import com.onceapps.core.util.OfflineException;
import com.onceapps.m.R;
import com.onceapps.m.models.Article;
import com.onceapps.m.models.Favorite;
import com.onceapps.m.models.FavoriteList;
import com.onceapps.m.models.Topic;
import com.onceapps.m.ui.utils.AppBarStateChangeListener;
import com.onceapps.m.ui.widgets.LoadingViewFlipper;
import com.onceapps.m.ui.widgets.TopicTextWithIndicatorView;
import com.onceapps.m.ui.widgets.TopicTextWithIndicatorView_;
import com.onceapps.m.utils.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@EActivity(R.layout.activity_article)
public class ArticleActivity extends BaseActivity {

    private static final String ARTICLE_PREFIX = "marquard://article/";
    private static final String GALLERY_PREFIX = "marquard://gallery/";
    private static final String FB_PREFIX = "fb://";

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy. MMM dd.", new Locale("hu", "HU"));

    @Extra
    protected Article article;

    @ViewById(R.id.article_content)
    protected WebView articleContentWebView;

    @ViewById(R.id.loading_flipper)
    protected LoadingViewFlipper mFlipper;

    @ViewById(R.id.brand)
    protected TextView mBrand;

    @ViewById(R.id.date)
    protected TextView mDate;

    @ViewById(R.id.topics_parent)
    protected LinearLayout mTopicsParent;

    @ViewById(R.id.nested_scroll)
    protected NestedScrollView mNestedScroll;

    @ViewById(R.id.header_favorite_button)
    protected ImageButton mHeaderFavoriteButton;

    @ViewById(R.id.favorite)
    protected ImageButton mFavoriteButton;

    private String mArticleHtml = null;

    private AtomicBoolean mShouldClearHistory = new AtomicBoolean(false);

    private AtomicBoolean mOutsideContent = new AtomicBoolean(false);

    private String mOriginalUrl = null;

    protected FavoriteList mFavorites = new FavoriteList();

    private final ArticleOnScrollListener mOnScrollListener = new ArticleOnScrollListener();

    @SuppressLint("SetJavaScriptEnabled")
    @AfterViews
    protected void afterViews() {

        mNestedScroll.setOnScrollChangeListener(mOnScrollListener);

        mFavoriteButton.setVisibility(View.VISIBLE);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float percentage = 1.0f - ((float) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange() * 2);
                mExtendedToolbarTitle.setAlpha(percentage);
                mExtendedToolbarTitleImage.setAlpha(percentage);
                mBrand.setAlpha(percentage);
                mDate.setAlpha(percentage);
                if (mFavoriteButton != null) mFavoriteButton.setAlpha(percentage);
            }
        });

        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {

            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.COLLAPSED) {
                    if (article.getBrands() != null && article.getBrands().size() > 0 && article.getBrands().get(0) != null) {
                        setTitle(article.getBrands().get(0).getName());
                    }
                    mHeaderFavoriteButton.setVisibility(View.VISIBLE);
                } else if (mCurrentState == State.COLLAPSED) {
                    setTitle("");
                    mHeaderFavoriteButton.setVisibility(View.GONE);
                }
            }
        });

        getArticleHtml();

        articleContentWebView.getSettings().setJavaScriptEnabled(true);
        articleContentWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                showLoadingBlur();

                mShouldClearHistory.set(url.startsWith(ARTICLE_PREFIX));

                if (url.startsWith(FB_PREFIX)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        showAlertDialog(
                                R.string.alert,
                                R.string.fb_app_missing_message,
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


                    return true;
                } else if (url.startsWith(ARTICLE_PREFIX)) {
                    try {
                        Integer articleId = Integer.parseInt(url.replace(ARTICLE_PREFIX, ""));
                        navigateToArticle(articleId);
                        return true;
                    } catch (NumberFormatException e) {
                        Logger.warn(e, "Invalid article id in url: %s", url);
                    } catch (Exception e) {
                        Logger.warn(e, "Error downloading article details for url: %s", url);
                    }
                } else if (url.startsWith(GALLERY_PREFIX)) {
                    try {
                        Long galleryId = Long.parseLong(url.replace(GALLERY_PREFIX, ""));
                        GalleryActivity_.intent(ArticleActivity.this)
                                .galleryId(galleryId)
                                .brand(article.getBrands().get(0))
                                .title(article.getTitle())
                                .start();
                        return true;
                    } catch (NumberFormatException e) {
                        Logger.warn(e, "Invalid gallery id in url: %s", url);
                    }
                } else {
                    try {
                        mRestClient.checkEnvironment();
                    } catch (OfflineException e) {
                        hideLoadingBlur();
                    } catch (GeneralSecurityException e) {
                        Logger.warn(e, "permission error");
                        hideLoadingBlur();
                    }

                    mOutsideContent.set(true);
                    lockAppBarClosed();
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (!mOutsideContent.get()) {
                    setTitle("");
                }
                mFlipper.showLoading();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mFlipper.showContent();
                if (mShouldClearHistory.getAndSet(false)) {
                    articleContentWebView.clearHistory();
                }

                if (mOriginalUrl == null) {
                    mOriginalUrl = articleContentWebView.getOriginalUrl();
                }
                mNestedScroll.scrollTo(0, 0);
                hideLoadingBlurDelayed();

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                mFlipper.showNoContent();
                hideLoadingBlur();
            }
        });

        getFavorites();
    }

    @Background(delay = 300)
    protected void hideLoadingBlurDelayed() {
        hideLoadingBlur();
    }

    @Background
    protected void getArticleHtml() {

        try {
            mArticleHtml = mRestClient.getArticleHtml(article.getId());
        } catch (OfflineException e) {
            //do nothing, let the offline dialog show
        } catch (Exception e) {
            Logger.error(e, "error getting article html");
        }
        updateUI();
    }

    @Background
    protected void navigateToArticle(Integer articleId) {

        try {
            Article newArticle = mRestClient.getArticle(articleId);
            if (newArticle != null) {
                this.article = newArticle;
                getArticleHtml();
            } else {
                onBackPressed();
            }
        } catch (OfflineException e) {
            //do nothing, let the offline dialog show
        } catch (Exception e) {
            Logger.error(e, "error getting article with id %d", articleId);
            onBackPressed();
        }
    }

    @Background
    protected void getFavorites() {
        try {
            mFavorites.clear();
            mFavorites.addAll(mRestClient.getFavorites());
            updateFavoriteStatus();
        } catch (Exception e) {
            Logger.error(e, "error getting favorites");
        }
    }

    @Click({R.id.favorite, R.id.header_favorite_button})
    protected void favoriteButtonClicked() {
        enableFavoriteButtons(false);
        if (mFavorites != null) {
            if (mFavorites.isFavorite(article)) {
                Favorite favorite = mFavorites.getFavoriteByArticleId(article);
                if (favorite != null) {
                    removeFromFavorites(favorite);
                } else {
                    Logger.warn("favorite not found");
                }
            } else {
                addToFavorites(article);
            }
        }
    }

    @UiThread
    protected void updateUI() {

        hideLoadingBlur();

        if (article.getBrands() != null && article.getBrands().size() > 0) {
            mBrand.setText(article.getBrands().get(0).getName());
        }

        setExtendedToolbarTitle(article.getTitle());
        if (mToolbarImageSize == null) {
            Utils.loadImageResized(mToolbarImage, article.getFileUrlSmall());
        } else {
            Utils.loadImageResized(mToolbarImage, article.getFileUrlSmall(), mToolbarImageSize.first, mToolbarImageSize.second);
        }
        setDateText();

        mTopicsParent.removeAllViews();
        if (article.getTopics() != null && article.getTopics().size() > 0) {
            mTopicsParent.setVisibility(View.VISIBLE);
            for (Topic topic : article.getTopics()) {
                TopicTextWithIndicatorView topicView = TopicTextWithIndicatorView_.build(this);
                topicView.setDataSource(topic, ContextCompat.getColor(this, R.color.mGrey02));
                mTopicsParent.addView(topicView);
            }
            mTopicsParent.invalidate();
        } else {
            mTopicsParent.setVisibility(View.INVISIBLE);
        }
        mTopicsParent.getParent().requestLayout();

        if (!TextUtils.isEmpty(mArticleHtml)) {
            articleContentWebView.loadDataWithBaseURL(Utils.WEBVIEW_APP_BASE_URL, mArticleHtml, Utils.WEBVIEW_MIME_TYPE, Utils.WEBVIEW_ENCODING, null);
            mOutsideContent.set(false);
            unlockAppBarOpen();
        } else {
            mFlipper.showContent(false);
        }
    }

    @UiThread
    protected void updateFavoriteStatus() {
        if (mFavorites != null) {
            mFavoriteButton.setActivated(mFavorites.isFavorite(article));
            mHeaderFavoriteButton.setActivated(mFavorites.isFavorite(article));
        }
    }

    @SupposeUiThread
    protected void setDateText() {
        if (article.getDate() != null) {
            mDate.setVisibility(View.VISIBLE);
            long ago = System.currentTimeMillis() - article.getDate().getTime();
            if (TimeUnit.MILLISECONDS.toMinutes(ago) < 60) {
                mDate.setText(getString(R.string.minutes_ago, TimeUnit.MILLISECONDS.toMinutes(ago)));
            } else if (TimeUnit.MILLISECONDS.toHours(ago) < 24) {
                mDate.setText(getString(R.string.hours_ago, TimeUnit.MILLISECONDS.toHours(ago)));
            } else {
                mDate.setText(mDateFormat.format(article.getDate()));
            }
        } else {
            mDate.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mNavigationView != null && mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
            return;
        } else if (articleContentWebView.canGoBack() && mOutsideContent.get()) {
            articleContentWebView.goBack();

            if (mOriginalUrl == null || mOriginalUrl.equals(articleContentWebView.copyBackForwardList().getCurrentItem().getOriginalUrl())) {
                //the content of the original article doesn't reload automatically, so we force reload the webview
                updateUI();
            }
            return;
        }
        super.onBackPressed();
    }

    protected class ArticleOnScrollListener implements NestedScrollView.OnScrollChangeListener {

        @Override
        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            if (!mOutsideContent.get() && !mNestedScroll.canScrollVertically(-1) && mAnimatedAppBarStateChangeListener.getCurrentState() == AppBarStateChangeListener.State.COLLAPSED) {
                mAnimatedAppBarStateChangeListener.expand();
            }
        }
    }

    @Background
    protected void addToFavorites(Article article) {
        try {
            activateFavoriteButtons(true);
            Favorite favorite = mRestClient.addToFavorites(article.getId());
            if (favorite != null) {
                if (!TextUtils.isEmpty(favorite.getMessage())) {
                    Logger.warn("error adding to favorites: %s", favorite.getMessage());
                }
            } else {
                Logger.warn("error adding to favorites: no response");
            }
            getFavorites();
        } catch (Exception e) {
            Logger.error(e, "error adding to favorites");
            activateFavoriteButtons(false);
        } finally {
            enableFavoriteButtons(true);
        }
    }

    @Background
    protected void removeFromFavorites(Favorite favorite) {
        try {
            activateFavoriteButtons(false);
            mRestClient.removeFromFavorites(favorite);
            getFavorites();
        } catch (Exception e) {
            Logger.error(e, "error removing favorites");
            activateFavoriteButtons(true);
        } finally {
            enableFavoriteButtons(true);
        }
    }

    @UiThread
    protected void enableFavoriteButtons(boolean isEnabled) {
        mFavoriteButton.setEnabled(isEnabled);
        mHeaderFavoriteButton.setEnabled(isEnabled);
    }

    @UiThread
    protected void activateFavoriteButtons(boolean isActivated) {
        mFavoriteButton.setActivated(isActivated);
        mHeaderFavoriteButton.setActivated(isActivated);
    }
}