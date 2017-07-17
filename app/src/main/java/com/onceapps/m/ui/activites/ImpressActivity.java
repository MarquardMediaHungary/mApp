package com.onceapps.m.ui.activites;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onceapps.core.util.DeviceUtils;
import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.ui.widgets.LoadingViewFlipper;
import com.onceapps.m.utils.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_impress)
public class ImpressActivity extends BaseActivity {

    @ViewById(R.id.article_content)
    protected WebView articleContentWebView;

    @ViewById(R.id.loading_flipper)
    protected LoadingViewFlipper mFlipper;

    @ViewById(R.id.version)
    protected TextView mVersion;

    @ViewById(R.id.impress_bf)
    protected RelativeLayout mFooter;

    @Bean
    protected DeviceUtils mDeviceUtils;

    private String mArticleHtml = null;

    @SuppressLint("SetJavaScriptEnabled")
    @AfterViews
    protected void afterViews() {

        setTitle(getString(R.string.impress_title));

        getImpressHtml();

        mExtendedToolbar.setVisibility(View.GONE);
        articleContentWebView.getSettings().setJavaScriptEnabled(true);
        articleContentWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mFlipper.showLoading();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mFlipper.showContent();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                mFlipper.showNoContent();
            }
        });
        mVersion.setText(mDeviceUtils.getAppVersion());
    }

    @Click(R.id.impress_bf)
    protected void onFooterClick() {
        articleContentWebView.loadUrl("http://www.bigfish.hu");
        mFooter.setVisibility(View.GONE);
    }

    @Click(R.id.version_layout)
    protected void onAppVersionClick() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    @Background
    protected void getImpressHtml() {

        try {
            mArticleHtml = mRestClient.getImpressHtml();
        } catch (Exception e) {
            Logger.error(e, "error getting impress html");
        }
        updateUI();
    }

    @UiThread
    protected void updateUI() {
        if (!TextUtils.isEmpty(mArticleHtml)) {
            articleContentWebView.loadDataWithBaseURL(Utils.WEBVIEW_APP_BASE_URL, mArticleHtml, Utils.WEBVIEW_MIME_TYPE, Utils.WEBVIEW_ENCODING, null);
        } else {
            mFlipper.showContent(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mNavigationView != null && mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
            return;
        }
        super.onBackPressed();
    }
}