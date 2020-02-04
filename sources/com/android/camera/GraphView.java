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
import com.android.camera.CameraManager.CameraProxy;

/* compiled from: PhotoModule */
class GraphView extends View {
    private static final int STATS_SIZE = 256;
    private static final String TAG = "GraphView";
    private Bitmap mBitmap;
    private Canvas mCanvas = new Canvas();
    private CameraProxy mGraphCameraDevice;
    private float mHeight;
    private Paint mPaint = new Paint();
    private Paint mPaintRect = new Paint();
    private PhotoModule mPhotoModule;
    private float mScale = 3.0f;
    private float mWidth;
    private float scaled;

    public GraphView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPaint.setFlags(1);
        this.mPaintRect.setColor(-1);
        this.mPaintRect.setStyle(Style.FILL);
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
        int i;
        Log.v(TAG, "in Camera.java ondraw");
        PhotoModule photoModule = this.mPhotoModule;
        if (photoModule == null || !photoModule.mHiston) {
            Log.e(TAG, "returning as histogram is off ");
            return;
        }
        if (this.mBitmap != null) {
            Paint paint = this.mPaint;
            Canvas canvas2 = this.mCanvas;
            float f = this.mHeight - 10.0f;
            float f2 = this.mWidth - 10.0f;
            float f3 = f2 / 256.0f;
            canvas2.drawColor(-5592406);
            paint.setColor(ViewCompat.MEASURED_STATE_MASK);
            for (int i2 = 0; ((float) i2) <= f / 32.0f; i2++) {
                float f4 = ((float) (i2 * 32)) + 5.0f;
                canvas2.drawLine(5.0f, f4, f2 + 5.0f, f4, paint);
            }
            for (int i3 = 0; ((float) i3) <= f2 / 32.0f; i3++) {
                float f5 = ((float) (i3 * 32)) + 5.0f;
                canvas2.drawLine(f5, 5.0f, f5, f + 5.0f, paint);
            }
            synchronized (PhotoModule.statsdata) {
                int i4 = 1;
                if (PhotoModule.statsdata[0] == 0) {
                    i = Integer.MIN_VALUE;
                    for (int i5 = 1; i5 <= 256; i5++) {
                        if (i < PhotoModule.statsdata[i5]) {
                            i = PhotoModule.statsdata[i5];
                        }
                    }
                } else {
                    i = PhotoModule.statsdata[0];
                }
                this.mScale = (float) i;
                while (i4 <= 256) {
                    this.scaled = (((float) PhotoModule.statsdata[i4]) / this.mScale) * 256.0f;
                    if (this.scaled >= 256.0f) {
                        this.scaled = 256.0f;
                    }
                    int i6 = i4 + 1;
                    float f6 = (0.0f * ((float) i6)) + (((float) i4) * f3) + 5.0f;
                    float f7 = f + 5.0f;
                    canvas2.drawRect(f6, f7, f6 + f3, f7 - this.scaled, this.mPaintRect);
                    i4 = i6;
                }
            }
            canvas.drawBitmap(this.mBitmap, 0.0f, 0.0f, null);
        }
        PhotoModule photoModule2 = this.mPhotoModule;
        if (photoModule2.mHiston && photoModule2 != null) {
            this.mGraphCameraDevice = photoModule2.getCamera();
            CameraProxy cameraProxy = this.mGraphCameraDevice;
            if (cameraProxy != null) {
                cameraProxy.sendHistogramData();
            }
        }
    }

    public void PreviewChanged() {
        invalidate();
    }

    public void setPhotoModuleObject(PhotoModule photoModule) {
        this.mPhotoModule = photoModule;
    }
}
