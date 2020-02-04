package com.android.camera.tinyplanet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.concurrent.locks.Lock;

public class TinyPlanetPreview extends View {
    private Lock mLock;
    private Paint mPaint = new Paint();
    private Bitmap mPreview;
    private PreviewSizeListener mPreviewSizeListener;
    private int mSize = 0;

    public interface PreviewSizeListener {
        void onSizeChanged(int i);
    }

    public TinyPlanetPreview(Context context) {
        super(context);
    }

    public TinyPlanetPreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public TinyPlanetPreview(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setBitmap(Bitmap bitmap, Lock lock) {
        this.mPreview = bitmap;
        this.mLock = lock;
        invalidate();
    }

    public void setPreviewSizeChangeListener(PreviewSizeListener previewSizeListener) {
        this.mPreviewSizeListener = previewSizeListener;
        int i = this.mSize;
        if (i > 0) {
            this.mPreviewSizeListener.onSizeChanged(i);
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Lock lock = this.mLock;
        if (lock != null && lock.tryLock()) {
            try {
                if (this.mPreview != null && !this.mPreview.isRecycled()) {
                    canvas.drawBitmap(this.mPreview, 0.0f, 0.0f, this.mPaint);
                }
            } finally {
                this.mLock.unlock();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int min = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(min, min);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (z && this.mPreviewSizeListener != null) {
            int min = Math.min(i3 - i, i4 - i2);
            if (min > 0) {
                PreviewSizeListener previewSizeListener = this.mPreviewSizeListener;
                if (previewSizeListener != null) {
                    previewSizeListener.onSizeChanged(min);
                }
            }
        }
    }
}
