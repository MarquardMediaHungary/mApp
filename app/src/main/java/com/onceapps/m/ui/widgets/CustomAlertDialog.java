package com.onceapps.m.ui.widgets;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.onceapps.m.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.layout_dialog)
public class CustomAlertDialog extends DialogFragment {

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_MESSAGE_PARAMETER = "message_parameter";
    public static final String KEY_TITLE = "title";
    public static final String KEY_POSITIVE_PAGE_REQUEST = "positive_page_request";
    public static final String KEY_NEGATIVE_PAGE_REQUEST = "negative_page_request";

    @ViewById(android.R.id.button1)
    protected Button button1;

    @ViewById(android.R.id.button2)
    protected Button button2;

    @ViewById(R.id.title)
    protected TextView title;

    @ViewById(R.id.message)
    protected TextView message;

    private View.OnClickListener button1OnClickListener;
    private View.OnClickListener button2OnClickListener;
    private int titleTextResId = 0;
    private int messageTextResId = 0;
    private int negativeButtonTextResId = 0;
    private int positiveButtonTextResId = 0;

    private String titleText = "";
    private String messageText = "";

    private boolean negativeButtonVisible = true;
    private boolean positiveButtonVisible = true;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Marquard_DialogStyle);
    }

    @SuppressWarnings("deprecation")
    @AfterViews
    protected void init() {
        message.setText(messageText);
        title.setText(titleText);
        if (messageTextResId > 0) message.setText(messageTextResId);
        if (negativeButtonTextResId > 0) button1.setText(negativeButtonTextResId);
        if (positiveButtonTextResId > 0) button2.setText(positiveButtonTextResId);
        if (titleTextResId > 0) title.setText(titleTextResId);
        if (!TextUtils.isEmpty(titleText)) title.setText(titleText);
        title.setVisibility(TextUtils.isEmpty(title.getText()) ? View.GONE : View.VISIBLE);
        message.setVisibility(TextUtils.isEmpty(message.getText()) ? View.GONE : View.VISIBLE);
        button1.setVisibility(negativeButtonVisible ? View.VISIBLE : View.GONE);
        button2.setVisibility(positiveButtonVisible ? View.VISIBLE : View.GONE);
    }

    public void setTitle(int resourceId) {
        titleTextResId = resourceId;
        if (message != null && resourceId != 0) message.setText(resourceId);
    }

    public void setMessage(int resourceId) {
        messageTextResId = resourceId;
        if (message != null && resourceId != 0) message.setText(resourceId);
    }

    public void setMessage(String text) {
        messageText = text;
        if (message != null) message.setText(messageText);
    }

    public void setTitle(String text) {
        titleText = text;
        if (title != null) title.setText(titleText);
    }

    public void setNegativeButtonText(int resourceId) {
        negativeButtonTextResId = resourceId;
        if (button1 != null && resourceId != 0) button1.setText(resourceId);
    }

    public void setPositiveButtonText(int resourceId) {
        positiveButtonTextResId = resourceId;
        if (button2 != null && resourceId != 0) button2.setText(resourceId);
    }

    public void setPositiveButtonListener(View.OnClickListener onClickListener) {
        button2OnClickListener = onClickListener;
    }

    public void setNegativeButtonListener(View.OnClickListener onClickListener) {
        button1OnClickListener = onClickListener;
    }

    @Click(android.R.id.button1)
    protected void button1Click() {
        button1OnClickListener.onClick(button1);
    }

    @Click(android.R.id.button2)
    protected void button2Click() {
        button2OnClickListener.onClick(button2);
    }

    public boolean isNegativeButtonVisible() {
        return negativeButtonVisible;
    }

    public void setNegativeButtonVisible(boolean negativeButtonVisible) {
        this.negativeButtonVisible = negativeButtonVisible;
    }

    public void setPositiveButtonVisible(boolean positiveButtonVisible) {
        this.positiveButtonVisible = positiveButtonVisible;
    }
}
