package com.android.camera.p004ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/* renamed from: com.android.camera.ui.SelfieFlashView */
public class SelfieFlashView extends View {
    private static final String TAG = "CAM_SelfieFlashView";
    private RectF rectF;
    private Paint targetPaint = new Paint();

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return true;
    }

    public SelfieFlashView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.targetPaint.setColor(-1);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        this.rectF = new RectF();
        RectF rectF2 = this.rectF;
        rectF2.left = 0.0f;
        rectF2.right = (float) canvas.getWidth();
        RectF rectF3 = this.rectF;
        rectF3.top = 0.0f;
        rectF3.bottom = (float) canvas.getHeight();
        canvas.drawRect(this.rectF, this.targetPaint);
    }

    public void open() {
        setVisibility(0);
    }

    public void close() {
        setVisibility(8);
    }
}
