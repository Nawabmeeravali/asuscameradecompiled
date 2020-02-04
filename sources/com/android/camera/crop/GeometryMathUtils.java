package com.android.camera.crop;

import android.graphics.Rect;
import android.graphics.RectF;

public final class GeometryMathUtils {
    public static final float SHOW_SCALE = 0.9f;
    private static final String TAG = "GeometryMathUtils";

    private static int getRotationForOrientation(int i) {
        if (i == 3) {
            return 180;
        }
        if (i != 6) {
            return i != 8 ? 0 : 270;
        }
        return 90;
    }

    private GeometryMathUtils() {
    }

    public static float clamp(float f, float f2, float f3) {
        return Math.max(Math.min(f, f3), f2);
    }

    public static float[] lineIntersect(float[] fArr, float[] fArr2) {
        float f = fArr[0];
        float f2 = fArr[1];
        float f3 = fArr[2];
        float f4 = fArr[3];
        float f5 = fArr2[0];
        float f6 = fArr2[1];
        float f7 = fArr2[2];
        float f8 = fArr2[3];
        float f9 = f - f3;
        float f10 = f2 - f4;
        float f11 = f3 - f7;
        float f12 = f8 - f4;
        float f13 = f5 - f7;
        float f14 = f6 - f8;
        float f15 = (f10 * f13) - (f9 * f14);
        if (f15 == 0.0f) {
            return null;
        }
        float f16 = ((f12 * f13) + (f14 * f11)) / f15;
        return new float[]{f3 + (f9 * f16), f4 + (f16 * f10)};
    }

    public static float[] shortestVectorFromPointToLine(float[] fArr, float[] fArr2) {
        float f = fArr2[0];
        float f2 = fArr2[2];
        float f3 = fArr2[1];
        float f4 = f2 - f;
        float f5 = fArr2[3] - f3;
        if (f4 == 0.0f && f5 == 0.0f) {
            return null;
        }
        float f6 = (((fArr[0] - f) * f4) + ((fArr[1] - f3) * f5)) / ((f4 * f4) + (f5 * f5));
        float[] fArr3 = {f + (f4 * f6), f3 + (f6 * f5)};
        return new float[]{fArr3[0] - fArr[0], fArr3[1] - fArr[1]};
    }

    public static float dotProduct(float[] fArr, float[] fArr2) {
        return (fArr[0] * fArr2[0]) + (fArr[1] * fArr2[1]);
    }

    public static float[] normalize(float[] fArr) {
        float sqrt = (float) Math.sqrt((double) ((fArr[0] * fArr[0]) + (fArr[1] * fArr[1])));
        return new float[]{fArr[0] / sqrt, fArr[1] / sqrt};
    }

    public static float scalarProjection(float[] fArr, float[] fArr2) {
        return dotProduct(fArr, fArr2) / ((float) Math.sqrt((double) ((fArr2[0] * fArr2[0]) + (fArr2[1] * fArr2[1]))));
    }

    public static float[] getVectorFromPoints(float[] fArr, float[] fArr2) {
        return new float[]{fArr2[0] - fArr[0], fArr2[1] - fArr[1]};
    }

    public static float[] getUnitVectorFromPoints(float[] fArr, float[] fArr2) {
        float[] fArr3 = {fArr2[0] - fArr[0], fArr2[1] - fArr[1]};
        float sqrt = (float) Math.sqrt((double) ((fArr3[0] * fArr3[0]) + (fArr3[1] * fArr3[1])));
        fArr3[0] = fArr3[0] / sqrt;
        fArr3[1] = fArr3[1] / sqrt;
        return fArr3;
    }

    public static void scaleRect(RectF rectF, float f) {
        rectF.set(rectF.left * f, rectF.top * f, rectF.right * f, rectF.bottom * f);
    }

    public static float[] vectorSubtract(float[] fArr, float[] fArr2) {
        int length = fArr.length;
        if (length != fArr2.length) {
            return null;
        }
        float[] fArr3 = new float[length];
        for (int i = 0; i < length; i++) {
            fArr3[i] = fArr[i] - fArr2[i];
        }
        return fArr3;
    }

    public static float vectorLength(float[] fArr) {
        return (float) Math.sqrt((double) ((fArr[0] * fArr[0]) + (fArr[1] * fArr[1])));
    }

    public static float scale(float f, float f2, float f3, float f4) {
        if (f2 == 0.0f || f == 0.0f || (f == f3 && f2 == f4)) {
            return 1.0f;
        }
        return Math.min(f3 / f, f4 / f2);
    }

    public static Rect roundNearest(RectF rectF) {
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }
}
