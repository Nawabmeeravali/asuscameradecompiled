package com.android.camera.p004ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;

/* renamed from: com.android.camera.ui.Arrows */
public class Arrows extends View {
    private static final int ARROW_COLOR = -1;
    private static final double ARROW_END_DEGREE = 15.0d;
    private static final int ARROW_END_LENGTH = 50;
    private Paint mPaint = new Paint();
    private ArrayList<Path> mPaths = new ArrayList<>();

    public Arrows(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setColor(-1);
        this.mPaint.setStrokeWidth(2.0f);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mPaths != null) {
            for (int i = 0; i < this.mPaths.size(); i++) {
                canvas.drawPath((Path) this.mPaths.get(i), this.mPaint);
            }
        }
    }

    public void addPath(float[] fArr, float[] fArr2) {
        float[] fArr3 = fArr;
        Path path = new Path();
        path.reset();
        path.moveTo(fArr3[0], fArr2[0]);
        for (int i = 1; i < fArr3.length; i++) {
            if (i == fArr3.length - 1) {
                path.lineTo(fArr3[i], fArr2[i]);
                int i2 = i - 1;
                double degrees = ((Math.toDegrees(Math.atan2((double) (fArr2[i] - fArr2[i2]), (double) (fArr3[i] - fArr3[i2]))) + ARROW_END_DEGREE) + 360.0d) % 360.0d;
                path.lineTo(fArr3[i] - ((float) (Math.cos(Math.toRadians(degrees)) * 50.0d)), fArr2[i] - ((float) (Math.sin(Math.toRadians(degrees)) * 50.0d)));
                path.lineTo(fArr3[i], fArr2[i]);
                double d = ((degrees - 30.0d) + 360.0d) % 360.0d;
                path.lineTo(fArr3[i] - ((float) (Math.cos(Math.toRadians(d)) * 50.0d)), fArr2[i] - ((float) (Math.sin(Math.toRadians(d)) * 50.0d)));
            } else {
                int i3 = i + 1;
                path.quadTo(fArr3[i], fArr2[i], fArr3[i3], fArr2[i3]);
            }
        }
        this.mPaths.add(path);
        invalidate();
    }
}
