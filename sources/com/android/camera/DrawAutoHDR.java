package com.android.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

/* compiled from: PhotoModule */
class DrawAutoHDR extends View {
    private static final String TAG = "AutoHdrView";
    private PhotoModule mPhotoModule;

    public DrawAutoHDR(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        PhotoModule photoModule = this.mPhotoModule;
        if (photoModule != null) {
            if (photoModule.mAutoHdrEnable) {
                Paint paint = new Paint();
                paint.setColor(-1);
                paint.setAlpha(0);
                canvas.drawPaint(paint);
                paint.setStyle(Style.STROKE);
                paint.setColor(-65281);
                paint.setStrokeWidth(1.0f);
                paint.setTextSize(32.0f);
                paint.setAlpha(255);
                canvas.drawText("HDR On", 200.0f, 100.0f, paint);
                return;
            }
            super.onDraw(canvas);
        }
    }

    public void AutoHDR() {
        invalidate();
    }

    public void setPhotoModuleObject(PhotoModule photoModule) {
        this.mPhotoModule = photoModule;
    }
}
