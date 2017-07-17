package com.onceapps.m.ui.utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class OnSwipeTouchListener implements OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context ctx) {
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            return onSwipeRight();
                        } else {
                            return onSwipeLeft();
                        }
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        return onSwipeDown();
                    } else {
                        return onSwipeUp();
                    }
                }

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return false;
        }
    }

    /**
     * event handler for swipe right
     *
     * @return true id event processed, false otherwise
     */
    public abstract boolean onSwipeRight();

    /**
     * event handler for swipe left
     *
     * @return true id event processed, false otherwise
     */
    public abstract boolean onSwipeLeft();

    /**
     * event handler for swipe up
     *
     * @return true id event processed, false otherwise
     */
    public abstract boolean onSwipeUp();

    /**
     * event handler for swipe down
     *
     * @return true id event processed, false otherwise
     */
    public abstract boolean onSwipeDown();
}