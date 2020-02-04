package com.android.camera.p004ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.ZoomRenderer */
public class ZoomRenderer extends OverlayRenderer implements OnScaleGestureListener {
    private static final String TAG = "CAM_Zoom";
    private boolean mCamera2 = false;
    private int mCenterX;
    private int mCenterY;
    private float mCircleSize;
    private ScaleGestureDetector mDetector;
    private int mInnerStroke;
    private OnZoomChangedListener mListener;
    private float mMaxCircle;
    private int mMaxZoom;
    private float mMinCircle;
    private int mMinZoom;
    private int mOrientation;
    private int mOuterStroke;
    private Paint mPaint;
    private Rect mTextBounds;
    private Paint mTextPaint;
    private int mZoomFraction;
    private float mZoomMaxValue;
    private float mZoomMinValue;
    private int mZoomSig;

    /* renamed from: com.android.camera.ui.ZoomRenderer$OnZoomChangedListener */
    public interface OnZoomChangedListener {
        void onZoomEnd();

        void onZoomStart();

        void onZoomValueChanged(float f);

        void onZoomValueChanged(int i);
    }

    public ZoomRenderer(Context context) {
        Resources resources = context.getResources();
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(-1);
        this.mPaint.setStyle(Style.STROKE);
        this.mTextPaint = new Paint(this.mPaint);
        this.mTextPaint.setStyle(Style.FILL);
        this.mTextPaint.setTextSize((float) resources.getDimensionPixelSize(C0905R.dimen.zoom_font_size));
        this.mTextPaint.setTextAlign(Align.LEFT);
        this.mTextPaint.setAlpha(192);
        this.mInnerStroke = resources.getDimensionPixelSize(C0905R.dimen.focus_inner_stroke);
        this.mOuterStroke = resources.getDimensionPixelSize(C0905R.dimen.focus_outer_stroke);
        this.mDetector = new ScaleGestureDetector(context, this);
        this.mMinCircle = (float) resources.getDimensionPixelSize(C0905R.dimen.zoom_ring_min);
        this.mTextBounds = new Rect();
        setVisible(false);
    }

    public void setZoomMax(int i) {
        this.mMaxZoom = i;
        this.mMinZoom = 0;
    }

    public void setZoomMax(float f) {
        this.mCamera2 = true;
        this.mZoomMaxValue = f;
        this.mZoomMinValue = 1.0f;
    }

    public void setZoom(int i) {
        float f = this.mMinCircle;
        this.mCircleSize = f + ((((float) i) * (this.mMaxCircle - f)) / ((float) (this.mMaxZoom - this.mMinZoom)));
    }

    public void setZoom(float f) {
        this.mCamera2 = true;
        this.mZoomSig = (int) f;
        this.mZoomFraction = ((int) (10.0f * f)) % 10;
        float f2 = this.mMinCircle;
        float f3 = this.mMaxCircle - f2;
        float f4 = this.mZoomMinValue;
        this.mCircleSize = (float) ((int) (f2 + ((f3 * (f - f4)) / (this.mZoomMaxValue - f4))));
    }

    public void setZoomValue(int i) {
        int i2 = i / 10;
        this.mZoomSig = i2 / 10;
        this.mZoomFraction = i2 % 10;
    }

    public void setOnZoomChangeListener(OnZoomChangedListener onZoomChangedListener) {
        this.mListener = onZoomChangedListener;
    }

    public void layout(int i, int i2, int i3, int i4) {
        super.layout(i, i2, i3, i4);
        this.mCenterX = (i3 - i) / 2;
        this.mCenterY = (i4 - i2) / 2;
        this.mMaxCircle = (float) Math.min(getWidth(), getHeight());
        this.mMaxCircle = (this.mMaxCircle - this.mMinCircle) / 2.0f;
    }

    public boolean isScaling() {
        return this.mDetector.isInProgress();
    }

    public void onDraw(Canvas canvas) {
        canvas.rotate((float) this.mOrientation, (float) this.mCenterX, (float) this.mCenterY);
        this.mPaint.setStrokeWidth((float) this.mInnerStroke);
        canvas.drawCircle((float) this.mCenterX, (float) this.mCenterY, this.mMinCircle, this.mPaint);
        canvas.drawCircle((float) this.mCenterX, (float) this.mCenterY, this.mMaxCircle, this.mPaint);
        int i = this.mCenterX;
        float f = ((float) i) - this.mMinCircle;
        int i2 = this.mCenterY;
        canvas.drawLine(f, (float) i2, (((float) i) - this.mMaxCircle) - 4.0f, (float) i2, this.mPaint);
        this.mPaint.setStrokeWidth((float) this.mOuterStroke);
        canvas.drawCircle((float) this.mCenterX, (float) this.mCenterY, this.mCircleSize, this.mPaint);
        StringBuilder sb = new StringBuilder();
        sb.append(this.mZoomSig);
        sb.append(".");
        sb.append(this.mZoomFraction);
        sb.append("x");
        String sb2 = sb.toString();
        this.mTextPaint.getTextBounds(sb2, 0, sb2.length(), this.mTextBounds);
        canvas.drawText(sb2, (float) (this.mCenterX - this.mTextBounds.centerX()), (float) (this.mCenterY - this.mTextBounds.centerY()), this.mTextPaint);
    }

    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        float scaleFactor = scaleGestureDetector.getScaleFactor();
        float min = Math.min(this.mMaxCircle, Math.max(this.mMinCircle, this.mCircleSize * scaleFactor * scaleFactor));
        OnZoomChangedListener onZoomChangedListener = this.mListener;
        if (!(onZoomChangedListener == null || min == this.mCircleSize)) {
            this.mCircleSize = min;
            if (this.mCamera2) {
                float f = this.mZoomMinValue;
                float f2 = this.mZoomMaxValue - f;
                float f3 = this.mMaxCircle;
                float f4 = this.mMinCircle;
                onZoomChangedListener.onZoomValueChanged(f + ((f2 / (f3 - f4)) * (this.mCircleSize - f4)));
            } else {
                int i = this.mMinZoom;
                float f5 = this.mCircleSize;
                float f6 = this.mMinCircle;
                onZoomChangedListener.onZoomValueChanged(i + ((int) (((f5 - f6) * ((float) (this.mMaxZoom - i))) / (this.mMaxCircle - f6))));
            }
            update();
        }
        return true;
    }

    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        setVisible(true);
        OnZoomChangedListener onZoomChangedListener = this.mListener;
        if (onZoomChangedListener != null) {
            onZoomChangedListener.onZoomStart();
        }
        update();
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        setVisible(false);
        OnZoomChangedListener onZoomChangedListener = this.mListener;
        if (onZoomChangedListener != null) {
            onZoomChangedListener.onZoomEnd();
        }
    }

    public void setOrientation(int i) {
        this.mOrientation = i;
        int i2 = this.mOrientation;
        if (i2 == 90) {
            this.mOrientation = 270;
        } else if (i2 == 270) {
            this.mOrientation = 90;
        }
    }
}
