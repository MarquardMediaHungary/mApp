package com.onceapps.m.ui.activites;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.onceapps.core.util.Logger;
import com.onceapps.m.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * StartActivity
 */

@EActivity(R.layout.activity_forgot_password)
public class ForgotPasswordActivity extends BaseActivity {

    @ViewById(R.id.email)
    protected EditText mEmailEditText;

    @ViewById(R.id.send_password_button)
    protected Button mSendPasswordButton;


    @AfterViews
    protected void afterViews() {
    }

    @Click(R.id.send_password_button)
    protected void sendPasswordButtonClicked() {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailEditText.getText()).matches()) {
            setEditTextError(mEmailEditText, getString(R.string.input_error_username));
            return;
        }

        setEditTextError(mEmailEditText, null);

        setFormEnabled(false);

        doRequestNewPassword();
    }

    @Background
    protected void doRequestNewPassword() {
        try {

            if(mRestClient.requestNewPassword(mEmailEditText.getText().toString())) {
                onSuccess();
            }
            else {
                onFailure(getString(R.string.not_registered_email, mEmailEditText.getText().toString()));
            }
        }
        catch (Exception e) {
            onFailure(getString(R.string.unknown_error));
            Logger.warn(e, "error requesting new password");
        }
    }

    @UiThread
    protected void onFailure(String message) {
        setFormEnabled(true);
        showAlertDialog(getString(R.string.error), message, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAlertDialog();
            }
        }, null);
    }

    @UiThread
    protected void onSuccess() {
        setFormEnabled(true);
        showAlertDialog(getString(R.string.alert), getString(R.string.new_password_sent_message, mEmailEditText.getText().toString()), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAlertDialog();
                onBackPressed();
            }
        }, null);
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
            mSendPasswordButton.setEnabled(enabled);
        } catch (Throwable t) {
            Logger.error(t, "setFormEnabled: %b", enabled);
        }
    }

}
