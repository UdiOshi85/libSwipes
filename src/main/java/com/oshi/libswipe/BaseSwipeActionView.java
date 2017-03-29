package com.oshi.libswipe;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

/**
 * Created By udi.oshi on 3/21/2017.
 * This view handles swipe gesture of a given child by the users.
 */
public abstract class BaseSwipeActionView extends FrameLayout {

    static final String TAG = "BaseSwipeActionView";
    static final int ANIMATE_SWIPE_CLOSE_DURATION = 500;

    // Views
    protected View container;
    private FrameLayout childContainer;

    // Abs
    public abstract int getLeftIconResId();
    public abstract int getRightIconResId();
    public abstract int getOverlayLayoutResId();
    public abstract boolean isSwipeEnabled();


    // Prims
    protected float minimumXtoHandleEvents;
    protected boolean gestureOnSwipeDetected;
    protected boolean gestureOnScrollDetected;

    protected GestureDetector gestureDetector;

    protected OnSwipedListener onSwipedListener;

    public interface OnSwipedListener {
        void onSwipeLeft();
        void onSwipeRight();
    }


    public BaseSwipeActionView(@NonNull Context context) {
        this(context, null);
    }

    public BaseSwipeActionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseSwipeActionView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Inflating the background area
        inflate(getContext(), R.layout.view_base_swipe, BaseSwipeActionView.this);

        AppCompatImageView leftIconView = (AppCompatImageView) findViewById(R.id.leftIcon);
        leftIconView.setImageResource(getLeftIconResId());
        AppCompatImageView rightIconView = (AppCompatImageView) findViewById(R.id.rightIcon);
        rightIconView.setImageResource(getRightIconResId());

        childContainer = (FrameLayout) findViewById(R.id.childContainer);

        // Inflating the foreground area
        LayoutInflater.from(getContext()).inflate(getOverlayLayoutResId(), childContainer, true);

        container = findViewById(R.id.container);

        minimumXtoHandleEvents = getResources().getDimension(R.dimen.dimen_swipe_minumum_x);

        if (isSwipeEnabled()) {
            initGestureDetector();
        }
    }

    private void initGestureDetector() {
        gestureDetector = new GestureDetector(getContext(), new SwipeGestureListener(SwipeGestureListener.Sensitivity.LOW) {
            private boolean animShown = false;
            private boolean animRunning = false;

            @Override
            protected boolean onSwipeLeft(float velocityX) {
                if (isSwipeEnabled()) {
                    gestureOnSwipeDetected = true;
                    notifySwipedLeft();

                }
                return true;
            }

            @Override
            protected boolean onSwipeRight(float velocityX) {
                if (isSwipeEnabled()) {
                    gestureOnSwipeDetected = true;
                    notifySwipedRight();
                }
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                // New touch - reset variables

                gestureOnSwipeDetected = false;
                gestureOnScrollDetected = false;
                animShown = false;
                animRunning = false;
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!isSwipeEnabled()) {
                    return true;
                }

                // Block scroll up/down of list while item is being scrolled right/left
                requestDisallowInterceptTouchEvent(true);

                int leftMargin = (int) container.getX();

                // Allow movement only up to 1/3 of the screen
                int maxMovement = Utils.getScreenWidth() / 3;
                if (Math.abs(leftMargin - distanceX) < maxMovement) {
                    container.setX(leftMargin - distanceX);
                } else if (leftMargin != maxMovement) {
                    //first time we are passing the max movement barier - setting the X position to be exactly as max movement
                    if (leftMargin < 0) container.setX(-maxMovement);
                    else container.setX(maxMovement);
                }

                // Handle showing of SMS/Call icons animation
                if (!animShown && !animRunning) {
                    if (Math.abs(leftMargin) > Utils.getScreenWidth() / 6) {
                        Animation heartBeatAnim = AnimationUtils.loadAnimation(getContext(), R.anim.heart_beat);
                        heartBeatAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                animRunning = false;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        View icon = findViewById(leftMargin < 0 ? R.id.rightIcon : R.id.leftIcon);
                        icon.startAnimation(heartBeatAnim);
                        animShown = true;
                        animRunning = true;
                    }
                }

                // User passed back the animation threshold - reset
                if (Math.abs(leftMargin) < Utils.getScreenWidth() / 6) {
                    animShown = false;
                }

                gestureOnScrollDetected = true;

                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isSwipeEnabled()) {
            return super.dispatchTouchEvent(ev);
        }

        // Only handle touch down events that are not on the SlidingMenu's swipe margin
        // Allow handling of other events (such as up) to allow animating item closing and etc.
        if ((ev.getAction() != MotionEvent.ACTION_UP) && (ev.getRawX() <= minimumXtoHandleEvents)) {
            return false;
        }

        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);

            if (ev.getAction() == MotionEvent.ACTION_UP) {
                if (gestureOnScrollDetected) {
                    // Handle return of scroll to starting position and perform action if needed
                    int leftMargin = (int) container.getX();
                    if (Math.abs(leftMargin) > Utils.getScreenWidth() / 4) {
                        if (!gestureOnSwipeDetected) {

                            if (leftMargin < 0) {
                                notifySwipedLeft();
                            } else {
                                notifySwipedRight();
                            }
                        }
                    }
                    animateScroll((int) container.getX(), 0);
                    return true;
                }
            }

            if (gestureOnScrollDetected) {
                return true;
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    private void animateScroll(int from, int to) {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(container, View.TRANSLATION_X, from, to);
        animator1.setDuration(ANIMATE_SWIPE_CLOSE_DURATION);
        animator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
//                swipeBackgroundAreaView.setVisibility(INVISIBLE);
//                rightShadow.setVisibility(INVISIBLE);
//                leftShadow.setVisibility(INVISIBLE);
                requestDisallowInterceptTouchEvent(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator1.setInterpolator(new OvershootInterpolator(2f));
        animator1.start();
    }

    public void setOnSwipedListener(OnSwipedListener l) {
        this.onSwipedListener = l;
    }

    private void notifySwipedLeft() {
        Log.d(TAG, "Notifying swipe left");
        if (onSwipedListener != null) {
            onSwipedListener.onSwipeLeft();
        }
    }

    private void notifySwipedRight() {
        Log.d(TAG, "Notifying swipe right");
        if (onSwipedListener != null) {
            onSwipedListener.onSwipeRight();
        }
    }


}
