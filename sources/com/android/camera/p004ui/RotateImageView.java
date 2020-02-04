package com.android.camera.p004ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageView.ScaleType;
import com.android.camera.util.CameraUtil;

/* renamed from: com.android.camera.ui.RotateImageView */
public class RotateImageView extends TwoStateImageView implements Rotatable {
    private static final int ANIMATION_SPEED = 270;
    private static final String TAG = "RotateImageView";
    private long mAnimationEndTime = 0;
    private long mAnimationStartTime = 0;
    private boolean mClockwise = false;
    private int mCurrentDegree = 0;
    private boolean mEnableAnimation = true;
    private boolean mNaturalPortrait = CameraUtil.isDefaultToPortrait((Activity) getContext());
    private int mStartDegree = 0;
    private int mTargetDegree = 0;
    private Bitmap mThumb;
    private TransitionDrawable mThumbTransition;
    private Drawable[] mThumbs;

    public RotateImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public RotateImageView(Context context) {
        super(context);
    }

    private boolean isOrientationPortrait() {
        int i = this.mCurrentDegree % 180;
        boolean z = i > 45 && i < 135;
        return this.mNaturalPortrait ? !z : z;
    }

    public void setOrientation(int i, boolean z) {
        this.mEnableAnimation = z;
        int i2 = i >= 0 ? i % 360 : (i % 360) + 360;
        if (i2 != this.mTargetDegree) {
            this.mTargetDegree = i2;
            if (this.mEnableAnimation) {
                this.mStartDegree = this.mCurrentDegree;
                this.mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
                int i3 = this.mTargetDegree - this.mCurrentDegree;
                if (i3 < 0) {
                    i3 += 360;
                }
                if (i3 > 180) {
                    i3 -= 360;
                }
                this.mClockwise = i3 >= 0;
                this.mAnimationEndTime = this.mAnimationStartTime + ((long) ((Math.abs(i3) * 1000) / ANIMATION_SPEED));
            } else {
                this.mCurrentDegree = this.mTargetDegree;
            }
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        float f;
        Drawable drawable = getDrawable();
        if (drawable != null) {
            Rect bounds = drawable.getBounds();
            int i = bounds.right - bounds.left;
            int i2 = bounds.bottom - bounds.top;
            if (!(i == 0 || i2 == 0)) {
                if (this.mCurrentDegree != this.mTargetDegree) {
                    long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
                    if (currentAnimationTimeMillis < this.mAnimationEndTime) {
                        int i3 = (int) (currentAnimationTimeMillis - this.mAnimationStartTime);
                        int i4 = this.mStartDegree;
                        if (!this.mClockwise) {
                            i3 = -i3;
                        }
                        int i5 = i4 + ((i3 * ANIMATION_SPEED) / 1000);
                        this.mCurrentDegree = i5 >= 0 ? i5 % 360 : (i5 % 360) + 360;
                        invalidate();
                    } else {
                        this.mCurrentDegree = this.mTargetDegree;
                    }
                }
                int paddingLeft = getPaddingLeft();
                int paddingTop = getPaddingTop();
                int width = (getWidth() - paddingLeft) - getPaddingRight();
                int height = (getHeight() - paddingTop) - getPaddingBottom();
                int saveCount = canvas.getSaveCount();
                if (getScaleType() == ScaleType.FIT_CENTER) {
                    if (isOrientationPortrait()) {
                        f = Math.min(((float) width) / ((float) i), ((float) height) / ((float) i2));
                    } else {
                        f = Math.min(((float) height) / ((float) i), ((float) width) / ((float) i2));
                    }
                    canvas.scale(f, f, ((float) width) / 2.0f, ((float) height) / 2.0f);
                }
                canvas.translate((float) (paddingLeft + (width / 2)), (float) (paddingTop + (height / 2)));
                canvas.rotate((float) (-this.mCurrentDegree));
                canvas.translate((float) ((-i) / 2), (float) ((-i2) / 2));
                drawable.draw(canvas);
                canvas.restoreToCount(saveCount);
            }
        }
    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            this.mThumb = null;
            this.mThumbs = null;
            setImageDrawable(null);
            setVisibility(8);
            return;
        }
        LayoutParams layoutParams = getLayoutParams();
        this.mThumb = ThumbnailUtils.extractThumbnail(bitmap, (layoutParams.width - getPaddingLeft()) - getPaddingRight(), (layoutParams.height - getPaddingTop()) - getPaddingBottom());
        Drawable[] drawableArr = this.mThumbs;
        if (drawableArr == null || !this.mEnableAnimation) {
            this.mThumbs = new Drawable[2];
            this.mThumbs[1] = new BitmapDrawable(getContext().getResources(), this.mThumb);
            setImageDrawable(this.mThumbs[1]);
        } else {
            drawableArr[0] = drawableArr[1];
            drawableArr[1] = new BitmapDrawable(getContext().getResources(), this.mThumb);
            this.mThumbTransition = new TransitionDrawable(this.mThumbs);
            setImageDrawable(this.mThumbTransition);
            this.mThumbTransition.startTransition(500);
        }
        setVisibility(0);
    }
}
