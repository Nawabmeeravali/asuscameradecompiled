package com.android.camera.crop;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import java.util.Arrays;

public class CropMath {
    public static int constrainedRotation(float f) {
        int i = (int) ((f % 360.0f) / 90.0f);
        if (i < 0) {
            i += 4;
        }
        return i * 90;
    }

    public static float[] getCornersFromRect(RectF rectF) {
        float f = rectF.left;
        float f2 = rectF.top;
        float f3 = rectF.right;
        float f4 = rectF.bottom;
        return new float[]{f, f2, f3, f2, f3, f4, f, f4};
    }

    public static boolean inclusiveContains(RectF rectF, float f, float f2) {
        return f <= rectF.right && f >= rectF.left && f2 <= rectF.bottom && f2 >= rectF.top;
    }

    public static RectF trapToRect(float[] fArr) {
        RectF rectF = new RectF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (int i = 1; i < fArr.length; i += 2) {
            float f = fArr[i - 1];
            float f2 = fArr[i];
            float f3 = rectF.left;
            if (f < f3) {
                f3 = f;
            }
            rectF.left = f3;
            float f4 = rectF.top;
            if (f2 < f4) {
                f4 = f2;
            }
            rectF.top = f4;
            float f5 = rectF.right;
            if (f <= f5) {
                f = f5;
            }
            rectF.right = f;
            float f6 = rectF.bottom;
            if (f2 > f6) {
                f6 = f2;
            }
            rectF.bottom = f6;
        }
        rectF.sort();
        return rectF;
    }

    public static void getEdgePoints(RectF rectF, float[] fArr) {
        if (fArr.length >= 2) {
            for (int i = 0; i < fArr.length; i += 2) {
                fArr[i] = GeometryMathUtils.clamp(fArr[i], rectF.left, rectF.right);
                int i2 = i + 1;
                fArr[i2] = GeometryMathUtils.clamp(fArr[i2], rectF.top, rectF.bottom);
            }
        }
    }

    public static float[] closestSide(float[] fArr, float[] fArr2) {
        int length = fArr2.length;
        float[] fArr3 = null;
        float f = Float.POSITIVE_INFINITY;
        int i = 0;
        while (i < length) {
            int i2 = i + 2;
            float[] fArr4 = {fArr2[i], fArr2[(i + 1) % length], fArr2[i2 % length], fArr2[(i + 3) % length]};
            float vectorLength = GeometryMathUtils.vectorLength(GeometryMathUtils.shortestVectorFromPointToLine(fArr, fArr4));
            if (vectorLength < f) {
                f = vectorLength;
                fArr3 = fArr4;
            }
            i = i2;
        }
        return fArr3;
    }

    public static boolean pointInRotatedRect(float[] fArr, RectF rectF, float f) {
        Matrix matrix = new Matrix();
        float[] copyOf = Arrays.copyOf(fArr, 2);
        matrix.setRotate(f, rectF.centerX(), rectF.centerY());
        Matrix matrix2 = new Matrix();
        if (!matrix.invert(matrix2)) {
            return false;
        }
        matrix2.mapPoints(copyOf);
        return inclusiveContains(rectF, copyOf[0], copyOf[1]);
    }

    public static boolean pointInRotatedRect(float[] fArr, float[] fArr2, float[] fArr3) {
        RectF rectF = new RectF();
        return pointInRotatedRect(fArr, rectF, getUnrotated(fArr2, fArr3, rectF));
    }

    public static void fixAspectRatio(RectF rectF, float f, float f2) {
        float min = Math.min(rectF.width() / f, rectF.height() / f2);
        float centerX = rectF.centerX();
        float centerY = rectF.centerY();
        float f3 = (f * min) / 2.0f;
        float f4 = (min * f2) / 2.0f;
        rectF.set(centerX - f3, centerY - f4, centerX + f3, centerY + f4);
    }

    public static void fixAspectRatioContained(RectF rectF, float f, float f2) {
        float width = rectF.width();
        float height = rectF.height();
        float f3 = f / f2;
        if (width / height < f3) {
            float f4 = width / f3;
            rectF.top = rectF.centerY() - (f4 / 2.0f);
            rectF.bottom = rectF.top + f4;
            return;
        }
        float f5 = height * f3;
        rectF.left = rectF.centerX() - (f5 / 2.0f);
        rectF.right = rectF.left + f5;
    }

    public static RectF getScaledCropBounds(RectF rectF, RectF rectF2, RectF rectF3) {
        Matrix matrix = new Matrix();
        matrix.setRectToRect(rectF2, rectF3, ScaleToFit.FILL);
        RectF rectF4 = new RectF(rectF);
        if (!matrix.mapRect(rectF4)) {
            return null;
        }
        return rectF4;
    }

    public static int getBitmapSize(Bitmap bitmap) {
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    private static float getUnrotated(float[] fArr, float[] fArr2, RectF rectF) {
        float atan = (float) ((Math.atan((double) ((fArr[1] - fArr[3]) / (fArr[0] - fArr[2]))) * 180.0d) / 3.141592653589793d);
        Matrix matrix = new Matrix();
        matrix.setRotate(-atan, fArr2[0], fArr2[1]);
        float[] fArr3 = new float[fArr.length];
        matrix.mapPoints(fArr3, fArr);
        rectF.set(trapToRect(fArr3));
        return atan;
    }
}
