package com.android.camera.crop;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;

public abstract class CropDrawingUtils {
    public static void drawRuleOfThird(Canvas canvas, RectF rectF) {
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setColor(Color.argb(128, 255, 255, 255));
        paint.setStrokeWidth(2.0f);
        float width = rectF.width() / 3.0f;
        float height = rectF.height() / 3.0f;
        float f = rectF.top + height;
        float f2 = rectF.left + width;
        for (int i = 0; i < 2; i++) {
            canvas.drawLine(f2, rectF.top, f2, rectF.bottom, paint);
            f2 += width;
        }
        for (int i2 = 0; i2 < 2; i2++) {
            canvas.drawLine(rectF.left, f, rectF.right, f, paint);
            f += height;
        }
    }

    public static void drawCropRect(Canvas canvas, RectF rectF) {
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setColor(-1);
        paint.setStrokeWidth(3.0f);
        canvas.drawRect(rectF, paint);
    }

    public static void drawShade(Canvas canvas, RectF rectF) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(-2013265920);
        RectF rectF2 = new RectF();
        float f = (float) width;
        rectF2.set(0.0f, 0.0f, f, rectF.top);
        canvas.drawRect(rectF2, paint);
        float f2 = (float) height;
        rectF2.set(0.0f, rectF.top, rectF.left, f2);
        canvas.drawRect(rectF2, paint);
        rectF2.set(rectF.left, rectF.bottom, f, f2);
        canvas.drawRect(rectF2, paint);
        rectF2.set(rectF.right, rectF.top, f, rectF.bottom);
        canvas.drawRect(rectF2, paint);
    }

    public static void drawIndicator(Canvas canvas, Drawable drawable, int i, float f, float f2) {
        int i2 = i / 2;
        int i3 = ((int) f) - i2;
        int i4 = ((int) f2) - i2;
        drawable.setBounds(i3, i4, i3 + i, i + i4);
        drawable.draw(canvas);
    }

    public static void drawIndicators(Canvas canvas, Drawable drawable, int i, RectF rectF, boolean z, int i2) {
        boolean z2 = i2 == 0;
        if (z) {
            if (i2 == 3 || z2) {
                drawIndicator(canvas, drawable, i, rectF.left, rectF.top);
            }
            if (i2 == 6 || z2) {
                drawIndicator(canvas, drawable, i, rectF.right, rectF.top);
            }
            if (i2 == 9 || z2) {
                drawIndicator(canvas, drawable, i, rectF.left, rectF.bottom);
            }
            if (i2 == 12 || z2) {
                drawIndicator(canvas, drawable, i, rectF.right, rectF.bottom);
                return;
            }
            return;
        }
        if ((i2 & 2) != 0 || z2) {
            drawIndicator(canvas, drawable, i, rectF.centerX(), rectF.top);
        }
        if ((i2 & 8) != 0 || z2) {
            drawIndicator(canvas, drawable, i, rectF.centerX(), rectF.bottom);
        }
        if ((i2 & 1) != 0 || z2) {
            drawIndicator(canvas, drawable, i, rectF.left, rectF.centerY());
        }
        if ((i2 & 4) != 0 || z2) {
            drawIndicator(canvas, drawable, i, rectF.right, rectF.centerY());
        }
    }

    public static void drawWallpaperSelectionFrame(Canvas canvas, RectF rectF, float f, float f2, Paint paint, Paint paint2) {
        float width = rectF.width() * f;
        float height = rectF.height() * f2;
        float centerX = rectF.centerX();
        float centerY = rectF.centerY();
        float f3 = width / 2.0f;
        float f4 = height / 2.0f;
        RectF rectF2 = new RectF(centerX - f3, centerY - f4, centerX + f3, centerY + f4);
        RectF rectF3 = new RectF(centerX - f4, centerY - f3, centerX + f4, centerY + f3);
        canvas.save();
        canvas.clipRect(rectF);
        canvas.clipRect(rectF2, Op.DIFFERENCE);
        canvas.clipRect(rectF3, Op.DIFFERENCE);
        canvas.drawPaint(paint2);
        canvas.restore();
        Path path = new Path();
        path.moveTo(rectF2.left, rectF2.top);
        path.lineTo(rectF2.right, rectF2.top);
        path.moveTo(rectF2.left, rectF2.top);
        path.lineTo(rectF2.left, rectF2.bottom);
        path.moveTo(rectF2.left, rectF2.bottom);
        path.lineTo(rectF2.right, rectF2.bottom);
        path.moveTo(rectF2.right, rectF2.top);
        path.lineTo(rectF2.right, rectF2.bottom);
        path.moveTo(rectF3.left, rectF3.top);
        path.lineTo(rectF3.right, rectF3.top);
        path.moveTo(rectF3.right, rectF3.top);
        path.lineTo(rectF3.right, rectF3.bottom);
        path.moveTo(rectF3.left, rectF3.bottom);
        path.lineTo(rectF3.right, rectF3.bottom);
        path.moveTo(rectF3.left, rectF3.top);
        path.lineTo(rectF3.left, rectF3.bottom);
        canvas.drawPath(path, paint);
    }

    public static void drawShadows(Canvas canvas, Paint paint, RectF rectF, RectF rectF2) {
        Canvas canvas2 = canvas;
        canvas2.drawRect(rectF2.left, rectF2.top, rectF.right, rectF.top, paint);
        Paint paint2 = paint;
        canvas.drawRect(rectF.right, rectF2.top, rectF2.right, rectF.bottom, paint2);
        canvas2.drawRect(rectF.left, rectF.bottom, rectF2.right, rectF2.bottom, paint);
        canvas.drawRect(rectF2.left, rectF.top, rectF.left, rectF2.bottom, paint2);
    }

    public static Matrix getBitmapToDisplayMatrix(RectF rectF, RectF rectF2) {
        Matrix matrix = new Matrix();
        setBitmapToDisplayMatrix(matrix, rectF, rectF2);
        return matrix;
    }

    public static boolean setBitmapToDisplayMatrix(Matrix matrix, RectF rectF, RectF rectF2) {
        matrix.reset();
        return matrix.setRectToRect(rectF, rectF2, ScaleToFit.CENTER);
    }

    public static boolean setImageToScreenMatrix(Matrix matrix, RectF rectF, RectF rectF2, int i) {
        RectF rectF3 = new RectF();
        float f = (float) i;
        matrix.setRotate(f, rectF.centerX(), rectF.centerY());
        boolean z = false;
        if (!matrix.mapRect(rectF3, rectF)) {
            return false;
        }
        boolean rectToRect = matrix.setRectToRect(rectF3, rectF2, ScaleToFit.CENTER);
        boolean preRotate = matrix.preRotate(f, rectF.centerX(), rectF.centerY());
        if (rectToRect && preRotate) {
            z = true;
        }
        return z;
    }
}
