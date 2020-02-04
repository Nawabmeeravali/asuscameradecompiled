package com.android.camera.crop;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import org.codeaurora.snapcam.C0905R;

public class CropView extends View {
    private static final String LOGTAG = "CropView";
    private Bitmap mBitmap;
    private Drawable mCropIndicator;
    private CropObject mCropObj = null;
    private float mDashOffLength = 10.0f;
    private float mDashOnLength = 20.0f;
    private boolean mDirty = false;
    private Matrix mDisplayMatrix = null;
    private Matrix mDisplayMatrixInverse = null;
    private boolean mDoSpot = false;
    private RectF mImageBounds = new RectF();
    private int mIndicatorSize;
    private int mMargin = 32;
    private int mMinSideSize = 90;
    private boolean mMovingBlock = false;
    private int mOverlayShadowColor = -822083584;
    private int mOverlayWPShadowColor = 1593835520;
    private Paint mPaint = new Paint();
    private float mPrevX = 0.0f;
    private float mPrevY = 0.0f;
    private int mRotation = 0;
    private RectF mScreenBounds = new RectF();
    private RectF mScreenCropBounds = new RectF();
    private RectF mScreenImageBounds = new RectF();
    private NinePatchDrawable mShadow;
    private Rect mShadowBounds = new Rect();
    private int mShadowMargin = 15;
    private float mSpotX = 0.0f;
    private float mSpotY = 0.0f;
    private Mode mState = Mode.NONE;
    private int mTouchTolerance = 40;
    private int mWPMarkerColor = Integer.MAX_VALUE;

    private enum Mode {
        NONE,
        MOVE
    }

    public CropView(Context context) {
        super(context);
        setup(context);
    }

    public CropView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setup(context);
    }

    public CropView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setup(context);
    }

    private void setup(Context context) {
        Resources resources = context.getResources();
        this.mShadow = (NinePatchDrawable) resources.getDrawable(C0905R.C0906drawable.geometry_shadow);
        this.mCropIndicator = resources.getDrawable(C0905R.C0906drawable.camera_crop);
        this.mIndicatorSize = (int) resources.getDimension(C0905R.dimen.crop_indicator_size);
        this.mShadowMargin = (int) resources.getDimension(C0905R.dimen.shadow_margin);
        this.mMargin = (int) resources.getDimension(C0905R.dimen.preview_margin);
        this.mMinSideSize = (int) resources.getDimension(C0905R.dimen.crop_min_side);
        this.mTouchTolerance = (int) resources.getDimension(C0905R.dimen.crop_touch_tolerance);
        this.mOverlayShadowColor = resources.getColor(C0905R.color.crop_shadow_color);
        this.mOverlayWPShadowColor = resources.getColor(C0905R.color.crop_shadow_wp_color);
        this.mWPMarkerColor = resources.getColor(C0905R.color.crop_wp_markers);
        this.mDashOnLength = resources.getDimension(C0905R.dimen.wp_selector_dash_length);
        this.mDashOffLength = resources.getDimension(C0905R.dimen.wp_selector_off_length);
    }

    public void initialize(Bitmap bitmap, RectF rectF, RectF rectF2, int i) {
        this.mBitmap = bitmap;
        CropObject cropObject = this.mCropObj;
        if (cropObject != null) {
            RectF innerBounds = cropObject.getInnerBounds();
            RectF outerBounds = this.mCropObj.getOuterBounds();
            if (innerBounds != rectF || outerBounds != rectF2 || this.mRotation != i) {
                this.mRotation = i;
                this.mCropObj.resetBoundsTo(rectF, rectF2);
                clearDisplay();
                return;
            }
            return;
        }
        this.mRotation = i;
        this.mCropObj = new CropObject(rectF2, rectF, 0);
        clearDisplay();
    }

    public RectF getCrop() {
        return this.mCropObj.getInnerBounds();
    }

    public RectF getPhoto() {
        return this.mCropObj.getOuterBounds();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        if (this.mDisplayMatrix != null) {
            Matrix matrix = this.mDisplayMatrixInverse;
            if (matrix != null) {
                float[] fArr = {x, y};
                matrix.mapPoints(fArr);
                float f = fArr[0];
                float f2 = fArr[1];
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked != 0) {
                    if (actionMasked != 1) {
                        if (actionMasked == 2 && this.mState == Mode.MOVE) {
                            this.mCropObj.moveCurrentSelection(f - this.mPrevX, f2 - this.mPrevY);
                            this.mPrevX = f;
                            this.mPrevY = f2;
                        }
                    } else if (this.mState == Mode.MOVE) {
                        this.mCropObj.selectEdge(0);
                        this.mMovingBlock = false;
                        this.mPrevX = f;
                        this.mPrevY = f2;
                        this.mState = Mode.NONE;
                    }
                } else if (this.mState == Mode.NONE) {
                    if (!this.mCropObj.selectEdge(f, f2)) {
                        this.mMovingBlock = this.mCropObj.selectEdge(16);
                    }
                    this.mPrevX = f;
                    this.mPrevY = f2;
                    this.mState = Mode.MOVE;
                }
                invalidate();
            }
        }
        return true;
    }

    private void reset() {
        Log.w(LOGTAG, "crop reset called");
        this.mState = Mode.NONE;
        this.mCropObj = null;
        this.mRotation = 0;
        this.mMovingBlock = false;
        clearDisplay();
    }

    private void clearDisplay() {
        this.mDisplayMatrix = null;
        this.mDisplayMatrixInverse = null;
        invalidate();
    }

    /* access modifiers changed from: protected */
    public void configChanged() {
        this.mDirty = true;
    }

    public void applyFreeAspect() {
        this.mCropObj.unsetAspectRatio();
        invalidate();
    }

    public void applyOriginalAspect() {
        RectF outerBounds = this.mCropObj.getOuterBounds();
        float width = outerBounds.width();
        float height = outerBounds.height();
        if (width <= 0.0f || height <= 0.0f) {
            Log.w(LOGTAG, "failed to set aspect ratio original");
            return;
        }
        applyAspect(width, height);
        this.mCropObj.resetBoundsTo(outerBounds, outerBounds);
    }

    public void applySquareAspect() {
        applyAspect(1.0f, 1.0f);
    }

    public void applyAspect(float f, float f2) {
        if (f <= 0.0f || f2 <= 0.0f) {
            throw new IllegalArgumentException("Bad arguments to applyAspect");
        }
        int i = this.mRotation;
        if (i < 0) {
            i = -i;
        }
        if (i % 180 == 90) {
            float f3 = f2;
            f2 = f;
            f = f3;
        }
        if (!this.mCropObj.setInnerAspectRatio(f, f2)) {
            Log.w(LOGTAG, "failed to set aspect ratio");
        }
        invalidate();
    }

    public void setWallpaperSpotlight(float f, float f2) {
        this.mSpotX = f;
        this.mSpotY = f2;
        if (this.mSpotX > 0.0f && this.mSpotY > 0.0f) {
            this.mDoSpot = true;
        }
    }

    public void unsetWallpaperSpotlight() {
        this.mDoSpot = false;
    }

    private int bitCycleLeft(int i, int i2, int i3) {
        int i4 = (1 << i3) - 1;
        int i5 = i & i4;
        int i6 = i2 % i3;
        return ((i5 << i6) & i4) | (i & (~i4)) | (i5 >> (i3 - i6));
    }

    private int decode(int i, float f) {
        int constrainedRotation = CropMath.constrainedRotation(f);
        if (constrainedRotation == 90) {
            return bitCycleLeft(i, 1, 4);
        }
        if (constrainedRotation == 180) {
            return bitCycleLeft(i, 2, 4);
        }
        if (constrainedRotation != 270) {
            return i;
        }
        return bitCycleLeft(i, 3, 4);
    }

    public void onDraw(Canvas canvas) {
        if (this.mBitmap != null) {
            if (this.mDirty) {
                this.mDirty = false;
                clearDisplay();
            }
            this.mImageBounds = new RectF(0.0f, 0.0f, (float) this.mBitmap.getWidth(), (float) this.mBitmap.getHeight());
            this.mScreenBounds = new RectF(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight());
            RectF rectF = this.mScreenBounds;
            int i = this.mMargin;
            rectF.inset((float) i, (float) i);
            if (this.mCropObj == null) {
                reset();
                RectF rectF2 = this.mImageBounds;
                this.mCropObj = new CropObject(rectF2, rectF2, 0);
            }
            if (this.mDisplayMatrix == null || this.mDisplayMatrixInverse == null) {
                this.mDisplayMatrix = new Matrix();
                this.mDisplayMatrix.reset();
                boolean imageToScreenMatrix = CropDrawingUtils.setImageToScreenMatrix(this.mDisplayMatrix, this.mImageBounds, this.mScreenBounds, this.mRotation);
                String str = LOGTAG;
                if (!imageToScreenMatrix) {
                    Log.w(str, "failed to get screen matrix");
                    this.mDisplayMatrix = null;
                    return;
                }
                this.mDisplayMatrixInverse = new Matrix();
                this.mDisplayMatrixInverse.reset();
                if (!this.mDisplayMatrix.invert(this.mDisplayMatrixInverse)) {
                    Log.w(str, "could not invert display matrix");
                    this.mDisplayMatrixInverse = null;
                    return;
                }
                this.mCropObj.setMinInnerSideSize(this.mDisplayMatrixInverse.mapRadius((float) this.mMinSideSize));
                this.mCropObj.setTouchTolerance(this.mDisplayMatrixInverse.mapRadius((float) this.mTouchTolerance));
            }
            this.mScreenImageBounds.set(this.mImageBounds);
            if (this.mDisplayMatrix.mapRect(this.mScreenImageBounds)) {
                int mapRadius = (int) this.mDisplayMatrix.mapRadius((float) this.mShadowMargin);
                this.mScreenImageBounds.roundOut(this.mShadowBounds);
                Rect rect = this.mShadowBounds;
                rect.set(rect.left - mapRadius, rect.top - mapRadius, rect.right + mapRadius, rect.bottom + mapRadius);
                this.mShadow.setBounds(this.mShadowBounds);
                this.mShadow.draw(canvas);
            }
            this.mPaint.setAntiAlias(true);
            this.mPaint.setFilterBitmap(true);
            canvas.drawBitmap(this.mBitmap, this.mDisplayMatrix, this.mPaint);
            this.mCropObj.getInnerBounds(this.mScreenCropBounds);
            if (this.mDisplayMatrix.mapRect(this.mScreenCropBounds)) {
                Paint paint = new Paint();
                paint.setColor(this.mOverlayShadowColor);
                paint.setStyle(Style.FILL);
                CropDrawingUtils.drawShadows(canvas, paint, this.mScreenCropBounds, this.mScreenImageBounds);
                CropDrawingUtils.drawCropRect(canvas, this.mScreenCropBounds);
                if (!this.mDoSpot) {
                    CropDrawingUtils.drawRuleOfThird(canvas, this.mScreenCropBounds);
                } else {
                    Paint paint2 = new Paint();
                    paint2.setColor(this.mWPMarkerColor);
                    paint2.setStrokeWidth(3.0f);
                    paint2.setStyle(Style.STROKE);
                    float f = this.mDashOnLength;
                    paint2.setPathEffect(new DashPathEffect(new float[]{f, f + this.mDashOffLength}, 0.0f));
                    paint.setColor(this.mOverlayWPShadowColor);
                    CropDrawingUtils.drawWallpaperSelectionFrame(canvas, this.mScreenCropBounds, this.mSpotX, this.mSpotY, paint2, paint);
                }
                CropDrawingUtils.drawIndicators(canvas, this.mCropIndicator, this.mIndicatorSize, this.mScreenCropBounds, this.mCropObj.isFixedAspect(), decode(this.mCropObj.getSelectState(), (float) this.mRotation));
            }
        }
    }
}
