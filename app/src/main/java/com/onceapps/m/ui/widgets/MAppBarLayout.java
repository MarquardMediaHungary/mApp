package com.onceapps.m.ui.widgets;

import android.content.Context;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.facebook.drawee.view.SimpleDraweeView;
import com.onceapps.m.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * MAppBarLayout
 * Created by szipe on 19/04/16.
 */
@EViewGroup(R.layout.inc_appbar_layout)
public class MAppBarLayout extends CollapsingToolbarLayout {

    @ViewById(R.id.collapsing_toolbar)
    protected CollapsingToolbarLayout mToolbarParent;

    @ViewById(R.id.image)
    protected SimpleDraweeView mImage;

    @ViewById(R.id.toolbar)
    protected Toolbar mToolbar;

    @ViewById(R.id.toolbar_title_flipper)
    protected ViewFlipper mToolbarTitleFlipper;

    @ViewById(R.id.toolbar_logo)
    protected ImageView mToolbarLogo;

    @ViewById(R.id.toolbar_title)
    protected TextView mTitle;

    @ViewById(R.id.header_back_button)
    protected ImageView mBackButton;

    @ViewById(R.id.header_menu_button)
    protected ImageView mMenuButton;

    public MAppBarLayout(Context context) {
        super(context);
    }

    public MAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MAppBarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @AfterViews
    protected void initUI() {

    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void setBackButtonVisibility(boolean isVisible) {
        mBackButton.setVisibility(isVisible ? VISIBLE : INVISIBLE);
    }

    public void setMenuButtonVisibility(boolean isVisible) {
        mMenuButton.setVisibility(isVisible ? VISIBLE : INVISIBLE);
    }

    public void setTitle(String text) {
        if(!TextUtils.isEmpty(text)) {
            mTitle.setText(text);
            mToolbarTitleFlipper.setDisplayedChild(1);
        }
        else {
            mToolbarTitleFlipper.setDisplayedChild(0);
        }
    }
}
