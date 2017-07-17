package com.onceapps.m.ui.activites;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

/**
 * StartActivity
 */

@EActivity(R.layout.activity_login)
public class LoginActivity extends BaseActivity {

    @ViewById(R.id.email)
    protected EditText mEmailEditText;

    @ViewById(R.id.password)
    protected EditText mPasswordEditText;

    @ViewById(R.id.login_button)
    protected Button mLoginButton;

    @ViewById(R.id.forgot_password_button)
    protected TextView mForgotPasswordButton;

    @ViewById(R.id.register_button)
    protected TextView mRegisterButton;

    @AfterViews
    protected void afterViews() {
        if (Logger.isDevMode()) {
            mEmailEditText.setText(R.string.test_email);
            mPasswordEditText.setText(R.string.test_password);
        }
    }

    @Click(R.id.login_button)
    protected void loginButtonClicked() {

        // validate email pattern
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailEditText.getText()).matches()) {
            setEditTextError(mEmailEditText, getString(R.string.input_error_username));
            return;
        }
        setEditTextError(mEmailEditText, null);
        if (mPasswordEditText.getText().length() < 1) {
            setEditTextError(mPasswordEditText, getString(R.string.input_error_empty));
            return;
        }
        setEditTextError(mPasswordEditText, null);
        setFormEnabled(false);
        doLogin();
    }

    @Click(R.id.forgot_password_button)
    protected void forgotPasswordButtonClicked() {
        ForgotPasswordActivity_.intent(this).start();
    }

    @Click(R.id.register_button)
    protected void registerButtonClicked() {
        RegisterActivity_.intent(this).start();
    }

    @Click(R.id.email)
    protected void onEmailEditTextClicked() {
        setEditTextError(mEmailEditText, null);
    }

    @Click(R.id.password)
    protected void onPswEditTextClciked() {
        setEditTextError(mPasswordEditText, null);
    }

    @Background
    protected void doLogin() {
        try {
            OauthResponse response = mRestClient.login(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString());
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

    @Override
    @UiThread
    protected void setFormEnabled(boolean enabled) {
        try {
            if (enabled) {
                hideLoadingBlur();
            } else {
                showLoadingBlur();
            }
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!enabled) {
                View view = getCurrentFocus();
                if (view != null) {
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    view.clearFocus();
                }
            }
            mLoginButton.setEnabled(enabled);
            mForgotPasswordButton.setEnabled(enabled);
            mRegisterButton.setEnabled(enabled);
            if (enabled) {
                mEmailEditText.requestFocus();
                inputManager.showSoftInput(mEmailEditText, 0);
            }
        } catch (Throwable t) {
            Logger.error(t, "setFormEnabled: %b", enabled);
        }
    }
}
