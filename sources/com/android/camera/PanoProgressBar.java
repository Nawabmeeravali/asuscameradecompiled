package com.android.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

class PanoProgressBar extends ImageView {
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_NONE = 0;
    public static final int DIRECTION_RIGHT = 2;
    private static final String TAG = "PanoProgressBar";
    private final Paint mBackgroundPaint = new Paint();
    private int mDirection = 0;
    private final Paint mDoneAreaPaint = new Paint();
    private RectF mDrawBounds;
    private float mHeight;
    private final Paint mIndicatorPaint = new Paint();
    private float mIndicatorWidth = 0.0f;
    private float mLeftMostProgress = 0.0f;
    private OnDirectionChangeListener mListener = null;
    private float mMaxProgress = 0.0f;
    private int mOldProgress = 0;
    private float mProgress = 0.0f;
    private float mProgressOffset = 0.0f;
    private float mRightMostProgress = 0.0f;
    private float mWidth;

    public interface OnDirectionChangeListener {
        void onDirectionChange(int i);
    }

    public PanoProgressBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDoneAreaPaint.setStyle(Style.FILL);
        this.mDoneAreaPaint.setAlpha(255);
        this.mBackgroundPaint.setStyle(Style.FILL);
        this.mBackgroundPaint.setAlpha(255);
        this.mIndicatorPaint.setStyle(Style.FILL);
        this.mIndicatorPaint.setAlpha(255);
        this.mDrawBounds = new RectF();
    }

    public void setOnDirectionChangeListener(OnDirectionChangeListener onDirectionChangeListener) {
        this.mListener = onDirectionChangeListener;
    }

    private void setDirection(int i) {
        if (this.mDirection != i) {
            this.mDirection = i;
            OnDirectionChangeListener onDirectionChangeListener = this.mListener;
            if (onDirectionChangeListener != null) {
                onDirectionChangeListener.onDirectionChange(this.mDirection);
            }
            invalidate();
        }
    }

    public int getDirection() {
        return this.mDirection;
    }

    public void setBackgroundColor(int i) {
        this.mBackgroundPaint.setColor(i);
        invalidate();
    }

    public void setDoneColor(int i) {
        this.mDoneAreaPaint.setColor(i);
        invalidate();
    }

    public void setIndicatorColor(int i) {
        this.mIndicatorPaint.setColor(i);
        invalidate();
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        this.mWidth = (float) i;
        this.mHeight = (float) i2;
        this.mDrawBounds.set(0.0f, 0.0f, this.mWidth, this.mHeight);
    }

    public void setMaxProgress(int i) {
        this.mMaxProgress = (float) i;
    }

    public void setIndicatorWidth(float f) {
        this.mIndicatorWidth = f;
        invalidate();
    }

    public void setRightIncreasing(boolean z) {
        if (z) {
            this.mLeftMostProgress = 0.0f;
            this.mRightMostProgress = 0.0f;
            this.mProgressOffset = 0.0f;
            setDirection(2);
        } else {
            float f = this.mWidth;
            this.mLeftMostProgress = f;
            this.mRightMostProgress = f;
            this.mProgressOffset = f;
            setDirection(1);
        }
        invalidate();
    }

    public void setProgress(int i) {
        if (this.mDirection == 0) {
            if (i > 10) {
                setRightIncreasing(true);
            } else if (i < -10) {
                setRightIncreasing(false);
            }
        }
        if (Math.abs(this.mOldProgress) - Math.abs(i) > 10) {
            this.mListener.onDirectionChange((this.mDirection / 2) + 1);
            return;
        }
        if (this.mDirection != 0) {
            float f = (float) i;
            float f2 = this.mWidth;
            this.mProgress = ((f * f2) / this.mMaxProgress) + this.mProgressOffset;
            this.mProgress = Math.min(f2, Math.max(0.0f, this.mProgress));
            if (this.mDirection == 2) {
                this.mRightMostProgress = Math.max(this.mRightMostProgress, this.mProgress);
            }
            if (this.mDirection == 1) {
                this.mLeftMostProgress = Math.min(this.mLeftMostProgress, this.mProgress);
            }
            invalidate();
        }
        if (Math.abs(this.mOldProgress) < Math.abs(i)) {
            this.mOldProgress = i;
        }
    }

    public void reset() {
        this.mProgress = 0.0f;
        this.mProgressOffset = 0.0f;
        this.mOldProgress = 0;
        setDirection(0);
        invalidate();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        float f;
        float f2;
        canvas.drawRect(this.mDrawBounds, this.mBackgroundPaint);
        if (this.mDirection != 0) {
            float f3 = this.mLeftMostProgress;
            RectF rectF = this.mDrawBounds;
            canvas.drawRect(f3, rectF.top, this.mRightMostProgress, rectF.bottom, this.mDoneAreaPaint);
            if (this.mDirection == 2) {
                f2 = Math.max(this.mProgress - this.mIndicatorWidth, 0.0f);
                f = this.mProgress;
            } else {
                f2 = this.mProgress;
                f = Math.min(this.mIndicatorWidth + f2, this.mWidth);
            }
            float f4 = f2;
            float f5 = f;
            RectF rectF2 = this.mDrawBounds;
            canvas.drawRect(f4, rectF2.top, f5, rectF2.bottom, this.mIndicatorPaint);
        }
    }
}
