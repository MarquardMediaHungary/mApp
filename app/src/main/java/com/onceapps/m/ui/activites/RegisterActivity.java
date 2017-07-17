package com.onceapps.m.ui.activites;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.api.BadResponseException;
import com.onceapps.m.api.OauthResponse;
import com.onceapps.m.models.User;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.Date;

/**
 * StartActivity
 */

@EActivity(R.layout.activity_register)
public class RegisterActivity extends BaseActivity {

    @ViewById(R.id.email)
    protected EditText mEmailEditText;

    @ViewById(R.id.name)
    protected EditText mNameEditText;

    @ViewById(R.id.date_of_birth)
    protected TextView mDobTextView;

    @ViewById(R.id.gender)
    protected TextView mGenderTextView;

    @ViewById(R.id.password)
    protected EditText mPasswordEditText;

    @ViewById(R.id.password_again)
    protected EditText mPasswordAgainEditText;

    @ViewById(R.id.register_button)
    protected Button mRegisterButton;

    private Date mDateOfBirth;
    private User.Gender mGender;

    @AfterViews
    protected void afterViews() {
        if (Logger.isDevMode()) {
            mNameEditText.setText(R.string.test_username);
            mEmailEditText.setText(R.string.test_email);
            mPasswordEditText.setText(R.string.test_password);
            mPasswordAgainEditText.setText(R.string.test_password);
        }
    }

    @Click(R.id.register_button)
    protected void registerButtonClicked() {

        if (mNameEditText.getText().length() < 1) {
            setEditTextError(mNameEditText, getString(R.string.input_error_empty));
            return;
        }

        // validate email pattern
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailEditText.getText()).matches()) {
            setEditTextError(mEmailEditText, getString(R.string.input_error_username));
            return;
        }

        if (mPasswordEditText.getText().length() < 1) {
            setEditTextError(mPasswordEditText, getString(R.string.input_error_empty));
            return;
        }

        if (mPasswordEditText.getText().length() < 8) {
            setEditTextError(mPasswordEditText, getString(R.string.password_too_short));
            return;
        }

        if (mPasswordAgainEditText.getText().length() < 1) {
            setEditTextError(mPasswordAgainEditText, getString(R.string.input_error_empty));
            return;
        }

        if (!mPasswordEditText.getText().toString().equals(mPasswordAgainEditText.getText().toString())) {
            setEditTextError(mPasswordAgainEditText, getString(R.string.password_no_match));
            return;
        }

        setEditTextError(mNameEditText, null);
        setEditTextError(mEmailEditText, null);
        setEditTextError(mPasswordEditText, null);
        setEditTextError(mPasswordAgainEditText, null);

        setFormEnabled(false);

        doRegister();
    }

    @Click(R.id.date_of_birth)
    protected void showDateOfBirthPickerDialog() {
        Calendar c = Calendar.getInstance();
        if (mDateOfBirth != null) {
            c.setTime(mDateOfBirth);
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, monthOfYear, dayOfMonth);
                RegisterActivity.this.mDateOfBirth = c.getTime();
                mDobTextView.setText(User.DATE_FORMAT.format(mDateOfBirth));
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    @Click(R.id.gender)
    protected void onGenderClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.register_gender_title)
                .setItems(R.array.genders_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mGender = User.Gender.fromValue(which + 1);
                        setGenderText();
                    }
                });
        builder.create().show();
    }

    @SupposeUiThread
    protected void setGenderText() {
        switch (mGender) {
            case MALE:
                mGenderTextView.setText(R.string.male);
                break;
            case FEMALE:
                mGenderTextView.setText(R.string.female);
                break;
        }
    }

    @Background
    protected void doRegister() {
        try {
            OauthResponse response = mRestClient.register(
                    mEmailEditText.getText().toString(),
                    mPasswordEditText.getText().toString(),
                    mNameEditText.getText().toString(),
                    mDobTextView.getText() != null ? mDobTextView.getText().toString() : null,
                    mGender
            );
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
                onRegisterFailure(sb.toString());
            } else if (!TextUtils.isEmpty(response.getAccessToken())) {
                mPreferences.app.edit()
                        .authToken().put(response.getAccessToken())
                        .authTokenType().put(response.getTokenType())
                        .apply();
                onLoginSuccess();
            } else {
                Logger.error("loginHandler: success but no token (wtf)");
                onRegisterFailure(getString(R.string.unknown_error));
            }

        } catch (BadResponseException e) {
            if (e.getErrorResponse().getCode() == 401) {
                onRegisterFailure(getString(R.string.unauthorized_error_message));
            } else {
                onRegisterFailure(getString(R.string.unknown_error));
            }
        } catch (Exception e) {
            onRegisterFailure(getString(R.string.unknown_error));
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
            mRegisterButton.setEnabled(enabled);
            if (enabled) {
                mNameEditText.requestFocus();
                inputManager.showSoftInput(mNameEditText, 0);
            }
        } catch (Throwable t) {
            Logger.error(t, "setFormEnabled: %b", enabled);
        }
    }

    @UiThread
    protected void onRegisterFailure(String message) {
        setFormEnabled(true);
        showAlertDialog(getString(R.string.register_error_title), message, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAlertDialog();
            }
        }, null);
    }

}
