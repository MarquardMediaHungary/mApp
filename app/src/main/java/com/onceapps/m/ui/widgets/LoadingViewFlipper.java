package com.onceapps.m.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.onceapps.m.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * LoadingViewFlipper
 * Created by szipe on 20/04/16.
 */
@EViewGroup(R.layout.loading_viewflipper)
public class LoadingViewFlipper extends ViewFlipper {

    private View mContentView;

    @ViewById(R.id.flipper)
    protected ViewFlipper mFlipper;

    public LoadingViewFlipper(Context context) {
        super(context);
    }

    public LoadingViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child.getId() == R.id.flipper) {
            super.addView(child, index, params);
        } else {
            mContentView = child;
        }
    }

    @AfterViews
    protected void afterViews() {
        if (mFlipper != null && mContentView != null) {
            mFlipper.addView(mContentView);
        }
    }

    public void showLoading() {
        showFlipperChild(0);
    }

    public void showNoContent() {
        showFlipperChild(1);
    }

    public void showContent() {
        showFlipperChild(2);
    }

    public void showContent(boolean hasContent) {
        if (hasContent) {
            showContent();
        } else {
            showNoContent();
        }
    }

    private void showFlipperChild(int index) {
        if (mFlipper != null && index >= 0 && index < mFlipper.getChildCount() && index != mFlipper.getDisplayedChild()) {
            mFlipper.setDisplayedChild(index);
        }
    }
}
