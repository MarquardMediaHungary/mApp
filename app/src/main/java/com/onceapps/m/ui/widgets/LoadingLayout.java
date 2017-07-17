package com.onceapps.m.ui.widgets;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.onceapps.m.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * TopicTextWithIndicatorView
 * Created by szipe on 20/04/16.
 */
@EViewGroup(R.layout.inc_loading)
public class LoadingLayout extends LinearLayout {

    @ViewById(R.id.loading_anim)
    protected ImageView mLoadingAnim;

    private Animation mFadeInAnimation;
    private Animation mFadeOutAnimation;


    public LoadingLayout(Context context) {
        super(context);
    }

    public LoadingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @AfterViews
    protected void init() {
        if (isInEditMode()) return;
        AnimationDrawable animationDrawable = (AnimationDrawable) mLoadingAnim.getDrawable();
        animationDrawable.start();

        mFadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        mFadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LoadingLayout.this.setAlpha(1f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mFadeOutAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        mFadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LoadingLayout.this.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void show() {
        mFadeInAnimation.cancel();
        mFadeOutAnimation.cancel();
        startAnimation(mFadeInAnimation);
    }

    public void hide() {
        mFadeInAnimation.cancel();
        mFadeOutAnimation.cancel();
        startAnimation(mFadeOutAnimation);
    }

    public void hideImmediately() {
        mFadeInAnimation.cancel();
        mFadeOutAnimation.cancel();
        setVisibility(GONE);
    }
}
