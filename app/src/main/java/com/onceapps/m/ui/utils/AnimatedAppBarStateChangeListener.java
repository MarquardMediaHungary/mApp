package com.onceapps.m.ui.utils;

import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;

/**
 * AppBarStateChangeListener
 * Created by szipe on 28/04/16.
 */
public class AnimatedAppBarStateChangeListener extends AppBarStateChangeListener {

    private AppBarLayout mAppBarLayout;
    boolean isExpanding = false;
    boolean isCollapsing = false;

    private final Object mStateChangeLock = new Object();

    public AnimatedAppBarStateChangeListener(AppBarLayout appBarLayout, @Nullable State currentState) {
        this.mCurrentState = currentState == null ? State.EXPANDED : currentState;
        this.mAppBarLayout = appBarLayout;
    }

    @Override
    public void onStateChanged(AppBarLayout appBarLayout, State state) {
        synchronized (mStateChangeLock) {
//            Logger.debug("currState: "+ mCurrentState + " state: " + state);
            if(mCurrentState == State.EXPANDED && state == State.IDLE && !isCollapsing && !isExpanding) {
                //TODO fix this, its glitchy
                // collapse();
            }
            else if(mCurrentState == State.COLLAPSED && state == State.IDLE && !isCollapsing && !isExpanding) {
                //TODO fix this, its glitchy
                // expand();
            }
            else if(mCurrentState == State.IDLE && (state == State.COLLAPSED || state == State.EXPANDED)) {
                    isCollapsing = false;
                    isExpanding = false;
            }
            mCurrentState = state;
        }
    }

    public void expand() {
        mAppBarLayout.setExpanded(true, true);
        isExpanding = true;
        isCollapsing = false;
    }

    public void collapse() {
        mAppBarLayout.setExpanded(false, true);
        isExpanding = false;
        isCollapsing = true;
    }
}
