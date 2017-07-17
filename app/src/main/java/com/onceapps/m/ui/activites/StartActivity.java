package com.onceapps.m.ui.activites;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.api.BadResponseException;
import com.onceapps.m.api.OauthResponse;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;

/**
 * StartActivity
 */

@EActivity(R.layout.activity_start)
public class StartActivity extends BaseActivity {

    @ViewById(R.id.background)
    protected ImageView mBackground;

    @ViewById(R.id.login_button)
    protected TextView mLoginButton;

    @ViewById(R.id.fb_login_button)
    protected Button mFBLoginButton;

    private CallbackManager mCallbackManager = CallbackManager.Factory.create();
    private LoginManager mFBLoginManager = LoginManager.getInstance();

    @AfterViews
    protected void afterViews() {
        setMenuButtonVisibility(false);
        setBackButtonVisibility(false);

        mFBLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                loginWithFacebookToken(loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                Logger.debug("Facebook login cancelled");
                setFormEnabled(true);
            }

            @Override
            public void onError(FacebookException error) {
                Logger.warn(error, "error logging in with Facebook");
                setFormEnabled(true);

                showAlertDialog(R.string.error, R.string.facebook_login_error);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Click(R.id.login_button)
    protected void loginButtonClicked() {
        LoginActivity_.intent(this).start();
    }

    @Click(R.id.fb_login_button)
    protected void fbLoginButtonClicked() {
        setFormEnabled(false);
        mFBLoginManager.logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
    }

    @Override
    @UiThread
    protected void setFormEnabled(boolean enabled) {

        if (enabled) {
            hideLoadingBlur();
        } else {
            showLoadingBlur();
        }

        mLoginButton.setEnabled(enabled);
        mFBLoginButton.setEnabled(enabled);
    }

    @Background
    protected void loginWithFacebookToken(String fbToken) {
        setFormEnabled(false);
        try {
            OauthResponse response = mRestClient.loginWithFacebook(fbToken);
            if (response.getError() != null) {
                StringBuilder sb = new StringBuilder();
                if (!TextUtils.isEmpty(response.getError().getUsername()))
                    sb.append(response.getError().getUsername()).append("\n");
                if (!TextUtils.isEmpty(response.getError().getDateOfBirth()))
                    sb.append(response.getError().getDateOfBirth()).append("\n");
                if (!TextUtils.isEmpty(response.getError().getPassword()))
                    sb.append(response.getError().getPassword()).append("\n");
                if (!TextUtils.isEmpty(response.getError().getName()))
                    sb.append(response.getError().getName()).append("\n");
                onLoginFailure(sb.toString());
            } else if (!TextUtils.isEmpty(response.getAccessToken())) {
                mPreferences.app.edit()
                        .authToken().put(response.getAccessToken())
                        .authTokenType().put(response.getTokenType())
                        .apply();
                onLoginSuccess();
            } else {
                Logger.error("loginHandler: success but no token (wtf)");
                onLoginFailure(getString(R.string.unknown_error));
            }

        } catch (BadResponseException e) {
            if (e.getErrorResponse().getCode() == 401) {
                onLoginFailure(getString(R.string.unauthorized_error_message));
            } else {
                onLoginFailure(getString(R.string.unknown_error));
            }

        } catch (Exception e) {
            onLoginFailure(getString(R.string.unknown_error));
        }
    }
}
