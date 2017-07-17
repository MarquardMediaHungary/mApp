package com.onceapps.m.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.onceapps.m.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AllBrandsMagazineListItemViewHolder
 * Created by szipe on 25/04/16.
 */
public class AllBrandsMagazineListItemViewHolder extends RecyclerView.ViewHolder {

    public float scaleEnlarged = 1.0f;
    public float scaleReduced = 0.9f;
    protected float overlayAlphaEnlarged = 0.0f;
    protected float overlayAlphaReduced = 0.5f;

    protected Animator currentAnimator;
    public boolean enlarged = false;
    public AtomicBoolean initialScalingDone = new AtomicBoolean(false);

    public FrameLayout parent;
    public SimpleDraweeView cover;
    public TextView date;
    public View overlay;

    public AllBrandsMagazineListItemViewHolder(View itemView) {
        super(itemView);
        cover = (SimpleDraweeView) itemView.findViewById(R.id.cover);
        date = (TextView) itemView.findViewById(R.id.date);
        parent = (FrameLayout) itemView.findViewById(R.id.parent);
        overlay = itemView.findViewById(R.id.bg_dim_overlay);

    }

    public void enlarge(boolean withAnimation) {
        if (!enlarged) {

            if(currentAnimator != null) {
                currentAnimator.cancel();
                currentAnimator = null;
            }

            int duration = withAnimation? 300 : 0;

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(duration);

            List<Animator> animatorList = new ArrayList<>();
            animatorList.add(ObjectAnimator.ofFloat(parent, "scaleX", scaleEnlarged));
            animatorList.add(ObjectAnimator.ofFloat(parent, "scaleY", scaleEnlarged));
            animatorList.add(ObjectAnimator.ofFloat(overlay, "alpha", overlayAlphaEnlarged));

            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    ViewCompat.setElevation(parent, (10.0f));
                    currentAnimator = null;
                    enlarged = true;
                }
            });

            animatorSet.playTogether(animatorList);
            currentAnimator = animatorSet;
            animatorSet.start();


        }
    }

    public void reduce(boolean withAnimation) {
        if (enlarged) {
            if(currentAnimator != null) {
                currentAnimator.cancel();
                currentAnimator = null;
            }

            int duration = withAnimation? 300 : 0;

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(duration);

            List<Animator> animatorList = new ArrayList<>();
            animatorList.add(ObjectAnimator.ofFloat(parent, "scaleX", scaleReduced));
            animatorList.add(ObjectAnimator.ofFloat(parent, "scaleY", scaleReduced));
            animatorList.add(ObjectAnimator.ofFloat(overlay, "alpha", overlayAlphaReduced));

            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    ViewCompat.setElevation(parent, (0.0f));
                    currentAnimator = null;
                    enlarged = false;
                }
            });

            animatorSet.playTogether(animatorList);
            currentAnimator = animatorSet;
            animatorSet.start();


        }
    }

    public void newPosition(int position) {
        if (position == 1)
            enlarge(true);
        else
            reduce(true);
    }

    public void setEnlarged(boolean enlarged) {
        this.enlarged = enlarged;
    }
}
