package com.android.camera.p004ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.ProgressRenderer */
public class ProgressRenderer {
    private static final int SHOW_PROGRESS_X_ADDITIONAL_MS = 100;
    private RectF mArcBounds = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
    private int mProgressAngleDegrees = 270;
    private final Paint mProgressBasePaint;
    private final Paint mProgressPaint;
    private final int mProgressRadius;
    private long mTimeToHide = 0;
    private VisibilityListener mVisibilityListener;
    private boolean mVisible = false;

    /* renamed from: com.android.camera.ui.ProgressRenderer$VisibilityListener */
    public interface VisibilityListener {
        void onHidden();
    }

    public ProgressRenderer(Context context) {
        this.mProgressRadius = context.getResources().getDimensionPixelSize(C0905R.dimen.pie_progress_radius);
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(C0905R.dimen.pie_progress_width);
        this.mProgressBasePaint = createProgressPaint(dimensionPixelSize, 0.2f);
        this.mProgressPaint = createProgressPaint(dimensionPixelSize, 1.0f);
    }

    public void setVisibilityListener(VisibilityListener visibilityListener) {
        this.mVisibilityListener = visibilityListener;
    }

    public void setProgress(int i) {
        int min = Math.min(100, Math.max(i, 0));
        this.mProgressAngleDegrees = (int) (((float) min) * 3.6f);
        if (min < 100) {
            this.mVisible = true;
            this.mTimeToHide = System.currentTimeMillis() + 100;
        }
    }

    public void onDraw(Canvas canvas, int i, int i2) {
        if (this.mVisible) {
            int i3 = this.mProgressRadius;
            this.mArcBounds = new RectF((float) (i - i3), (float) (i2 - i3), (float) (i + i3), (float) (i3 + i2));
            canvas.drawCircle((float) i, (float) i2, (float) this.mProgressRadius, this.mProgressBasePaint);
            canvas.drawArc(this.mArcBounds, -90.0f, (float) this.mProgressAngleDegrees, false, this.mProgressPaint);
            if (this.mProgressAngleDegrees == 360 && System.currentTimeMillis() > this.mTimeToHide) {
                this.mVisible = false;
                VisibilityListener visibilityListener = this.mVisibilityListener;
                if (visibilityListener != null) {
                    visibilityListener.onHidden();
                }
            }
        }
    }

    public boolean isVisible() {
        return this.mVisible;
    }

    private static Paint createProgressPaint(int i, float f) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.argb((int) (f * 255.0f), 255, 255, 255));
        paint.setStrokeWidth((float) i);
        paint.setStyle(Style.STROKE);
        return paint;
    }
}
