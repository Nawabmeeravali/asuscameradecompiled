package com.android.camera.p004ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

/* renamed from: com.android.camera.ui.DotsView */
public class DotsView extends View {
    private DotsViewItem mItems;
    private int mPosition;
    private float mPositionOffset;
    private Paint mTargetPaint = new Paint();

    public DotsView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTargetPaint.setColor(-1);
        this.mTargetPaint.setStyle(Style.FILL_AND_STROKE);
    }

    public void update(int i, float f) {
        this.mPosition = i;
        this.mPositionOffset = f;
        invalidate();
    }

    public void setItems(DotsViewItem dotsViewItem) {
        this.mItems = dotsViewItem;
    }

    public void update() {
        invalidate();
    }

    public void onDraw(Canvas canvas) {
        if (this.mItems != null) {
            int width = canvas.getWidth() / (this.mItems.getTotalItemNums() + 1);
            int height = canvas.getHeight() / 2;
            float min = ((float) Math.min(width, canvas.getHeight() / (this.mItems.getTotalItemNums() + 1))) / 2.0f;
            for (int i = 0; i < this.mItems.getTotalItemNums(); i++) {
                if (i - 1 == this.mPosition) {
                    float f = this.mPositionOffset;
                    if (f > 0.0f) {
                        drawDot(canvas, (float) ((i + 1) * width), (float) height, min + (f * min), this.mItems.isChosen(i));
                    }
                }
                int i2 = i + 1;
                if (i2 == this.mPosition) {
                    float f2 = this.mPositionOffset;
                    if (f2 < 0.0f) {
                        drawDot(canvas, (float) (i2 * width), (float) height, min - (f2 * min), this.mItems.isChosen(i));
                    }
                }
                if (i == this.mPosition) {
                    drawDot(canvas, (float) (i2 * width), (float) height, min + ((1.0f - Math.abs(this.mPositionOffset)) * min), this.mItems.isChosen(i));
                } else {
                    drawDot(canvas, (float) (i2 * width), (float) height, min, this.mItems.isChosen(i));
                }
            }
        }
    }

    private void drawDot(Canvas canvas, float f, float f2, float f3, boolean z) {
        if (z) {
            this.mTargetPaint.setStyle(Style.FILL_AND_STROKE);
            canvas.drawCircle(f, f2, f3, this.mTargetPaint);
            return;
        }
        this.mTargetPaint.setStyle(Style.STROKE);
        canvas.drawCircle(f, f2, f3, this.mTargetPaint);
    }
}
