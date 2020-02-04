package com.android.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.support.p000v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/* compiled from: CaptureModule */
class Camera2GraphView extends View {
    private static final int STATS_SIZE = 256;
    private static final String TAG = "GraphView";
    private Bitmap mBitmap;
    private Canvas mCanvas = new Canvas();
    private CaptureModule mCaptureModule;
    private int mEnd;
    private float mHeight;
    private Paint mPaint = new Paint();
    private Paint mPaintRect = new Paint();
    private float mScale = 3.0f;
    private int mStart;
    private float mWidth;
    private float scaled;

    public Camera2GraphView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPaint.setFlags(1);
        this.mPaintRect.setColor(-1);
        this.mPaintRect.setStyle(Style.FILL);
    }

    /* access modifiers changed from: 0000 */
    public void setDataSection(int i, int i2) {
        this.mStart = i;
        this.mEnd = i2;
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        this.mBitmap = Bitmap.createBitmap(i, i2, Config.RGB_565);
        this.mCanvas.setBitmap(this.mBitmap);
        this.mWidth = (float) i;
        this.mHeight = (float) i2;
        super.onSizeChanged(i, i2, i3, i4);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        CaptureModule captureModule = this.mCaptureModule;
        if (captureModule != null || captureModule.mHiston) {
            if (this.mBitmap != null) {
                Paint paint = this.mPaint;
                Canvas canvas2 = this.mCanvas;
                float f = this.mHeight - 10.0f;
                float f2 = this.mWidth - 10.0f;
                float f3 = f2 / 256.0f;
                canvas2.drawColor(-5592406);
                paint.setColor(ViewCompat.MEASURED_STATE_MASK);
                for (int i = 0; ((float) i) <= f / 32.0f; i++) {
                    float f4 = ((float) (i * 32)) + 5.0f;
                    canvas2.drawLine(5.0f, f4, f2 + 5.0f, f4, paint);
                }
                for (int i2 = 0; ((float) i2) <= f2 / 32.0f; i2++) {
                    float f5 = ((float) (i2 * 32)) + 5.0f;
                    canvas2.drawLine(f5, 5.0f, f5, f + 5.0f, paint);
                }
                synchronized (CaptureModule.statsdata) {
                    int i3 = Integer.MIN_VALUE;
                    for (int i4 = this.mStart; i4 < this.mEnd; i4++) {
                        if (i3 < CaptureModule.statsdata[i4]) {
                            i3 = CaptureModule.statsdata[i4];
                        }
                    }
                    this.mScale = (float) i3;
                    for (int i5 = this.mStart; i5 < this.mEnd; i5++) {
                        this.scaled = (((float) CaptureModule.statsdata[i5]) / this.mScale) * 256.0f;
                        if (this.scaled >= 256.0f) {
                            this.scaled = 256.0f;
                        }
                        float f6 = (0.0f * ((float) ((i5 - this.mStart) + 1))) + (((float) (i5 - this.mStart)) * f3) + 5.0f;
                        float f7 = f + 5.0f;
                        canvas2.drawRect(f6, f7, f6 + f3, f7 - this.scaled, this.mPaintRect);
                    }
                }
                canvas.drawBitmap(this.mBitmap, 0.0f, 0.0f, null);
            }
            return;
        }
        Log.e(TAG, "returning as histogram is off ");
    }

    public void PreviewChanged() {
        invalidate();
    }

    public void setCaptureModuleObject(CaptureModule captureModule) {
        this.mCaptureModule = captureModule;
    }
}
