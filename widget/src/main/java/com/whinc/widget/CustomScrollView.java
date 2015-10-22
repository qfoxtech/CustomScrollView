package com.whinc.widget;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by Administrator on 2015/10/21.
 */
public class CustomScrollView extends FrameLayout{
    private static final String TAG = CustomScrollView.class.getSimpleName();
    private static final int DURATION = 500;        // ms

    /* XML layout attributes */
    private int mWidth;
    private int mHeight;
    private int mItemWidth;
    private int mItemHeight;
    private int mItemMargin;
    private int mItemLargeWidth;
    private int mItemLargeHeight;

    private int mItemLargeIndex;
    private Interpolator mInterpolator = null;
    private Context mContext;
    private int mTranslationX = 0;
    private GestureDetector mGestureDetector;

    private float getItemWHRatio() {
        return 1.0f * mItemHeight / mItemWidth;
    }

    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceX) < 1) {
                return true;
            }

            mTranslationX -= distanceX;
            float ratio = Math.abs(distanceX / getItemWM());
            int childCount = getChildCount();
            View activeView = getChildAt(mItemLargeIndex);
            Rect rect = (Rect) activeView.getTag();
            int itemCenterX = mTranslationX + rect.left + rect.width()/2;
            int screenCenterX = mContext.getResources().getDisplayMetrics().widthPixels / 2;
            if (itemCenterX < screenCenterX) {          // left of screen center line
                if (mItemLargeIndex < childCount - 1) {
                    if (distanceX > 0) {        // scroll left
                        // 中间item向左下方扩展，左边固定
                        View view1 = getChildAt(mItemLargeIndex);
                        Rect rect1 = (Rect) view1.getTag();
                        float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                        float deltaY = getItemWHRatio() * deltaX;
                        rect1.right -= Math.round(deltaX);
                        rect1.top += Math.round(deltaY);
                        print("center", rect1);

                        // 右侧item向左上方扩展，右边固定
                        View view2 = getChildAt(mItemLargeIndex + 1);
                        Rect rect2 = (Rect) view2.getTag();
                        rect2.left -= Math.round(deltaX);
                        rect2.top -= Math.round(deltaY);
                        print("right", rect2);

                        // 向左滑动一个Item后更新当前Large item指向
                        if (rect2.width() >= mItemLargeWidth ||
                                rect2.height() >= mItemLargeHeight) {
                            mItemLargeIndex += 1;
                            // 修正大小
                            mTranslationX = getTranslateX(mItemLargeIndex);
                            adjustItemSize(mItemLargeIndex);
                        }
                    } else if (distanceX < 0) {     // scroll right
                        // 中间item向右上方扩展，左边固定
                        View view1 = getChildAt(mItemLargeIndex);
                        Rect rect1 = (Rect) view1.getTag();
                        float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                        float deltaY = getItemWHRatio() * deltaX;
                        rect1.right += Math.round(deltaX);
                        rect1.top -= Math.round(deltaY);
                        print("center", rect1);

                        // 右侧item向右下方扩展， 右边固定
                        View view2 = getChildAt(mItemLargeIndex + 1);
                        Rect rect2 = (Rect) view2.getTag();
                        rect2.left += Math.round(deltaX);
                        rect2.top += Math.round(deltaY);
                        print("right", rect2);
                    }
                }
            } else if (itemCenterX > screenCenterX) {   // right of screen center line
                if (mItemLargeIndex > 0) {
                    if (distanceX > 0) {            // scroll left
                        // 中间item向左上方扩展，右边固定
                        View view1 = getChildAt(mItemLargeIndex);
                        Rect rect1 = (Rect) view1.getTag();
                        float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                        float deltaY = getItemWHRatio() * deltaX;
                        rect1.left -= Math.round(deltaX);
                        rect1.top -= Math.round(deltaY);
//                        Log.i(TAG, String.format("deltaX=%d, deltaY=%d", Math.round(deltaX), Math.round(deltaY)));
                        print("center", rect1);

                        // 左侧item向左下方扩展，左边固定
                        View view2 = getChildAt(mItemLargeIndex - 1);
                        Rect rect2 = (Rect) view2.getTag();
                        rect2.right -= Math.round(deltaX);
                        rect2.top += Math.round(deltaY);
                        print("left", rect2);
                    } else if (distanceX < 0) {     // scroll right
                        // 中间item向右下方扩展，右边固定
                        View view1 = getChildAt(mItemLargeIndex);
                        Rect rect1 = (Rect) view1.getTag();
                        float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                        float deltaY = getItemWHRatio() * deltaX;
                        rect1.left += Math.round(deltaX);
                        rect1.top += Math.round(deltaY);
                        Log.i(TAG, String.format("deltaX=%d, deltaY=%d", Math.round(deltaX), Math.round(deltaY)));
                        print("center", rect1);

                        // 左侧item向右上方扩展，左边固定
                        View view2 = getChildAt(mItemLargeIndex - 1);
                        Rect rect2 = (Rect) view2.getTag();
                        rect2.right += Math.round(deltaX);
                        rect2.top -= Math.round(deltaY);
                        print("left", rect2);

                        if (rect2.width() >= mItemLargeWidth ||
                                rect2.height() >= mItemLargeHeight) {
                            mItemLargeIndex -= 1;
                            // 修正大小
                            mTranslationX = getTranslateX(mItemLargeIndex);
                            adjustItemSize(mItemLargeIndex);
                        }
                    }
                }
            }
            requestLayout();
            return true;
        }
    };
    private ValueAnimator mAnimator;

    public CustomScrollView(Context context) {
        super(context);
        init(context, null);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /** Set the end animator TimeInterpolator, if set null it will use the default
     * {@link android.view.animation.LinearInterpolator} */
    public void setInterpolator(@Nullable Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mGestureDetector = new GestureDetector(context, mGestureListener);

        mItemWidth = 200;
        mItemHeight = 200 * 4 / 3;
        mItemMargin = 10;
        mItemLargeHeight = mItemHeight * 2;
        mItemLargeWidth = mItemWidth * 2;

        // Measure the width and height of CustomScrollView
        post(new Runnable() {
            @Override
            public void run() {
                mWidth = getMeasuredWidth();
                mHeight = getMeasuredHeight();
                Log.i(TAG, String.format("w=%d, h=%d", mWidth, mHeight));
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.i(TAG, "" +event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stopAnimation();
                break;
            case MotionEvent.ACTION_UP:
                playAnimation();
                break;
        }
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    private void playAnimation() {
        // 手指离开屏幕时，将当前最大的Item设置为中心Item
        Rect rect = (Rect)getChildAt(mItemLargeIndex).getTag();
        if (mItemLargeIndex > 0) {
            Rect leftRect = (Rect)getChildAt(mItemLargeIndex - 1).getTag();
            if (rect.width() < leftRect.width()) {
                mItemLargeIndex -= 1;
            }
        }
        if (mItemLargeIndex < getChildCount() - 1) {
            Rect rightRect = (Rect)getChildAt(mItemLargeIndex + 1).getTag();
            if (rect.width() < rightRect.width()) {
                mItemLargeIndex += 1;
            }
        }
        Log.i(TAG, "item large index:" + mItemLargeIndex);
        adjustItemSize(mItemLargeIndex);

        final int destTranslationX = getTranslateX(mItemLargeIndex);
        final int srcTranslationX = mTranslationX;
        mAnimator = ValueAnimator.ofInt(srcTranslationX, destTranslationX);
        mAnimator.setDuration(DURATION);
        mAnimator.setInterpolator(mInterpolator);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                mTranslationX = (int) (srcTranslationX + (destTranslationX - srcTranslationX) * fraction);
//                Log.i(TAG, "fraction:" + fraction);
                requestLayout();
            }
        });
        mAnimator.start();
    }

    private void stopAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

    public void addItem(int n) {
        if (n <= 0) {
            return;
        }

        for (int i = 0; i < n; ++i) {
            // create View
            ImageView child = new ImageView(mContext);
            child.setBackgroundResource(R.drawable.image);
            addView(child);

            // create layout Rect
            int itemLeft = i * getItemWM() + mItemMargin;
            Rect rect = new Rect(0, 0, 0, 0);
            if (i < n / 2) {
                rect.set(itemLeft, mHeight - mItemHeight, itemLeft + mItemWidth, mHeight);
            } else if (i > n / 2) {
                itemLeft = (i - 1) * getItemWM() + getLargeItemWM() + mItemMargin;
                rect.set(itemLeft, mHeight - mItemHeight, itemLeft + mItemWidth, mHeight);
            } else {
                rect.set(itemLeft, mHeight - mItemHeight * 2, itemLeft + mItemWidth * 2, mHeight);
            }
            child.setTag(rect);
        }

        mItemLargeIndex = n / 2;
        mTranslationX = getTranslateX(mItemLargeIndex);
        requestLayout();
    }

    /** Remove all child items  */
    public void clearItems() {
        if (getChildCount() > 0) {
            removeAllViewsInLayout();
        }
    }

    /** When the nth item is Large Item and center in screen,
     * return the first item startX position */
    private int getTranslateX(int n) {
        if (n < 0 || n > getChildCount() - 1) {
            throw new IllegalArgumentException(
                    String.format("Invalid argument n: %d ,n must be in [0, %d]", n, getChildCount()-1));
        }
        int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        int startX = screenWidth / 2;
        startX -= getLargeItemWM()/2;
        if (n > 0) {
            startX -= (getItemWM() * n);
        }
        return startX;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View view = getChildAt(i);
            Rect rect = (Rect) view.getTag();
            view.layout(mTranslationX + rect.left, rect.top, mTranslationX + rect.right, rect.bottom);
        }
    }

    /** Get item width with left and right margin */
    private int getItemWM() {
        return mItemWidth + mItemMargin * 2;
    }

    private int getLargeItemWM() {
        return mItemLargeWidth +  mItemMargin * 2;
    }

    private void print(String msg, Rect rect) {
        Log.i(TAG, String.format("%s, l=%d, r=%d, w=%d, h=%d", msg,
                rect.left, rect.right, rect.width(), rect.height()));
    }

    /** Adjust all items size. */
    private void adjustItemSize(int largeItemIndex) {
        int n = getChildCount();
        for (int i = 0; i < n; ++i) {
            int itemLeft = i * getItemWM() + mItemMargin;
            Rect rect = (Rect) getChildAt(i).getTag();
            if (i < largeItemIndex) {
                rect.set(itemLeft, mHeight - mItemHeight, itemLeft + mItemWidth, mHeight);
            } else if (i > largeItemIndex) {
                itemLeft = (i - 1) * getItemWM() + getLargeItemWM() + mItemMargin;
                rect.set(itemLeft, mHeight - mItemHeight, itemLeft + mItemWidth, mHeight);
            } else {
                rect.set(itemLeft, mHeight - mItemHeight * 2, itemLeft + mItemWidth * 2, mHeight);
            }
        }
    }
}
