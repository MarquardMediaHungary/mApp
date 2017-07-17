package com.onceapps.m.ui.activites;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.api.BadResponseException;
import com.onceapps.m.api.UserErrorResponse;
import com.onceapps.m.models.User;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.commons.codec.binary.StringUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * SettingsActivity
 */

@EActivity(R.layout.activity_settings)
public class SettingsActivity extends BaseActivity {

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

    @ViewById(R.id.modify_button)
    protected TextView mModifyButton;

    @ViewById(R.id.sign_out_button)
    protected TextView mSignoutButton;

    @ViewById(R.id.push_switch)
    protected SwitchCompat mPushSwitch;

    private Date mDateOfBirth;
    private User.Gender mGender;
    private Boolean mEnablePush = true;

    @AfterViews
    protected void afterViews() {
        mMenuButton.setVisibility(View.VISIBLE);
        if (mPreferences.app.user() != null) {

            User user = User.fromJsonString(mPreferences.app.user().get());

            if (user != null) {
                if (user.getName() != null) mNameEditText.setText(user.getName());
                if (user.getUsername() != null) mEmailEditText.setText(user.getUsername());
                if (user.getDateOfBirth() != null) mDobTextView.setText(user.getDateOfBirth());
                if (user.getGender() != null) {
                    mGender = user.getGender();
                    setGenderText();
                }
                if (user.getGivePush() != null) {
                    mPushSwitch.setChecked(user.getGivePush());
                }
            } else {
                Logger.warn("something not right, user logged in, but no data in the preferences");
            }
        } else {
            Logger.warn("something not right, user logged in, but no data in the preferences");
        }
    }

    @Click(R.id.modify_button)
    protected void modifyButtonClicked() {

        if (mNameEditText.getText().length() < 1) {
            setEditTextError(mNameEditText, getString(R.string.input_error_empty));
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailEditText.getText()).matches()) {
            setEditTextError(mEmailEditText, getString(R.string.input_error_username));
            return;
        }

        if (!TextUtils.isEmpty(mPasswordEditText.getText()) && mPasswordEditText.getText().length() < 8) {
            setEditTextError(mPasswordEditText, getString(R.string.password_too_short));
            return;
        }

        if (!TextUtils.isEmpty(mPasswordAgainEditText.getText()) && mPasswordAgainEditText.getText().length() < 8) {
            setEditTextError(mPasswordAgainEditText, getString(R.string.password_too_short));
            return;
        }

        if (mPasswordEditText.getText().length() != mPasswordAgainEditText.getText().length() ||
                !StringUtils.equals(mPasswordEditText.getText(), mPasswordAgainEditText.getText())) {
            setEditTextError(mPasswordAgainEditText, getString(R.string.password_no_match));
            return;
        }

        setEditTextError(mNameEditText, null);
        setEditTextError(mEmailEditText, null);
        setEditTextError(mPasswordEditText, null);
        setEditTextError(mPasswordAgainEditText, null);

        setFormEnabled(false);

        doModifyUser();
    }

    @Click(R.id.sign_out_button)
    protected void onSignOutButtonClick() {
        doLogout();
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
                SettingsActivity.this.mDateOfBirth = c.getTime();
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
            mModifyButton.setEnabled(enabled);
            mSignoutButton.setEnabled(enabled);
        } catch (Throwable t) {
            Logger.error(t, "setFormEnabled: %b", enabled);
        }
    }

    @Background
    protected void doModifyUser() {
        try {
            UserErrorResponse response = mRestClient.modifyUser(
                    mEmailEditText.getText().toString(),
                    mPasswordEditText.getText().toString(),
                    mNameEditText.getText().toString(),
                    mDobTextView.getText() != null ? mDobTextView.getText().toString() : null,
                    mGender,
                    mPushSwitch.isChecked()
            );
            if (response != null && response.getError() != null) {
                StringBuilder sb = new StringBuilder();
                if (!TextUtils.isEmpty(response.getError().getUsername()))
                    sb.append(response.getError().getUsername()).append("\n");
                if (!TextUtils.isEmpty(response.getError().getDateOfBirth()))
                    sb.append(response.getError().getDateOfBirth()).append("\n");
                if (!TextUtils.isEmpty(response.getError().getPassword()))
                    sb.append(response.getError().getPassword()).append("\n");
                if (!TextUtils.isEmpty(response.getError().getName()))
                    sb.append(response.getError().getName()).append("\n");
                onModifyFailure(sb.toString());
            } else {
                try {
                    User user = mRestClient.getUser();
                    if (user != null) {
                        mPreferences.app.user().put(user.toString());
                    } else {
                        Logger.warn("Modify successful, but error getting user");
                    }
                } catch (Exception e) {
                    Logger.warn(e, "Modify successful, but error getting user");
                }

                onModifySuccess();
            }

        } catch (BadResponseException e) {
            if (e.getErrorResponse().getCode() == 401) {
                onModifyFailure(getString(R.string.unauthorized_error_message));
            } else {
                onModifyFailure(getString(R.string.unknown_error));
            }
        } catch (Exception e) {
            onModifyFailure(getString(R.string.unknown_error));
        }
    }

    @Background
    protected void doLogout() {
        try {
            mRestClient.logout();
        } catch (Exception e) {
            Logger.warn(e, "error logging out");
        } finally {
            LoginManager.getInstance().logOut();
            mPreferences.app.clear();
            StartActivity_.intent(this).start();
            finishAffinity();
        }
    }

    @UiThread
    protected void onModifyFailure(String message) {
        setFormEnabled(true);
        showAlertDialog(getString(R.string.modify_user_error_title), message, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAlertDialog();
            }
        }, null);
    }

    @UiThread
    protected void onModifySuccess() {
        setFormEnabled(true);
        setUserName();
        Snackbar.make(mCoordinatorLayout, R.string.modify_user_success, Snackbar.LENGTH_LONG).show();
    }

}
