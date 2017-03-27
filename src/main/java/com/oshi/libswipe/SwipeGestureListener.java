package com.oshi.libswipe;

import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;


class SwipeGestureListener extends SimpleOnGestureListener {

    public enum Sensitivity {

        HIGH(Utils.dpToPx(65)),
        MEDIUM(Utils.dpToPx(115)),
        LOW(Utils.dpToPx(165));

        private final int velocityThreshold;

        private static final int THRESHOLD = Utils.dpToPx(60);

        Sensitivity(int velocityThreshold) {
            this.velocityThreshold = velocityThreshold;
        }

        public int getThreshold() {
            return THRESHOLD;
        }

        public int getVelocityThreshold() {
            return velocityThreshold;
        }
    }

    private final Sensitivity sensitivity;

    public SwipeGestureListener() {
        this(Sensitivity.HIGH);
    }

    public SwipeGestureListener(Sensitivity sensitivity) {
        this.sensitivity = sensitivity;
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }


    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > sensitivity.getThreshold() && Math.abs(velocityX) > sensitivity.getVelocityThreshold()) {
                    if (diffX > 0) {
                        if (velocityX > 0) {
                            //velocity on the same direction of entire movement
                            result = onSwipeRight(velocityX);
                        }
                    } else {
                        if (velocityX < 0) {
                            //velocity on the same direction of entire movement
                            result = onSwipeLeft(velocityX);
                        }
                    }
                }
            } else {
                if (Math.abs(diffY) > sensitivity.getThreshold() && Math.abs(velocityY) > sensitivity.getVelocityThreshold()) {
                    if (diffY > 0) {
                        result = onSwipeBottom(velocityY);
                    } else {
                        result = onSwipeTop(velocityY);
                    }
                }
            }
        } catch (Exception exception) {
            Log.e(SwipeGestureListener.class.getSimpleName(), "onFling error");
        }
        return result;
    }

    protected boolean onSwipeRight(float velocityX) {
        return false;
    }

    protected boolean onSwipeTop(float velocityY) {
        return false;
    }

    protected boolean onSwipeBottom(float velocityY) {
        return false;

    }

    protected boolean onSwipeLeft(float velocityX) {
        return false;
    }

}
