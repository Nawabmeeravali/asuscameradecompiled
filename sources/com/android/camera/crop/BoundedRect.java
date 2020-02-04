package com.android.camera.crop;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import java.util.Arrays;

public class BoundedRect {
    private RectF inner;
    private float[] innerRotated = CropMath.getCornersFromRect(this.inner);
    private RectF outer;
    private float rot;

    public BoundedRect(float f, Rect rect, Rect rect2) {
        this.rot = f;
        this.outer = new RectF(rect);
        this.inner = new RectF(rect2);
        rotateInner();
        if (!isConstrained()) {
            reconstrain();
        }
    }

    public BoundedRect(float f, RectF rectF, RectF rectF2) {
        this.rot = f;
        this.outer = new RectF(rectF);
        this.inner = new RectF(rectF2);
        rotateInner();
        if (!isConstrained()) {
            reconstrain();
        }
    }

    public void resetTo(float f, RectF rectF, RectF rectF2) {
        this.rot = f;
        this.outer.set(rectF);
        this.inner.set(rectF2);
        this.innerRotated = CropMath.getCornersFromRect(this.inner);
        rotateInner();
        if (!isConstrained()) {
            reconstrain();
        }
    }

    public void setInner(RectF rectF) {
        if (!this.inner.equals(rectF)) {
            this.inner = rectF;
            this.innerRotated = CropMath.getCornersFromRect(this.inner);
            rotateInner();
            if (!isConstrained()) {
                reconstrain();
            }
        }
    }

    public void setRotation(float f) {
        if (f != this.rot) {
            this.rot = f;
            this.innerRotated = CropMath.getCornersFromRect(this.inner);
            rotateInner();
            if (!isConstrained()) {
                reconstrain();
            }
        }
    }

    public void setToInner(RectF rectF) {
        rectF.set(this.inner);
    }

    public void setToOuter(RectF rectF) {
        rectF.set(this.outer);
    }

    public RectF getInner() {
        return new RectF(this.inner);
    }

    public RectF getOuter() {
        return new RectF(this.outer);
    }

    public void moveInner(float f, float f2) {
        Matrix inverseRotMatrix = getInverseRotMatrix();
        RectF rectF = new RectF(this.inner);
        rectF.offset(f, f2);
        float[] cornersFromRect = CropMath.getCornersFromRect(rectF);
        float[] cornersFromRect2 = CropMath.getCornersFromRect(this.outer);
        inverseRotMatrix.mapPoints(cornersFromRect);
        float[] fArr = {0.0f, 0.0f};
        for (int i = 0; i < cornersFromRect.length; i += 2) {
            float f3 = cornersFromRect[i] + fArr[0];
            float f4 = cornersFromRect[i + 1] + fArr[1];
            if (!CropMath.inclusiveContains(this.outer, f3, f4)) {
                float[] fArr2 = {f3, f4};
                float[] shortestVectorFromPointToLine = GeometryMathUtils.shortestVectorFromPointToLine(fArr2, CropMath.closestSide(fArr2, cornersFromRect2));
                fArr[0] = fArr[0] + shortestVectorFromPointToLine[0];
                fArr[1] = fArr[1] + shortestVectorFromPointToLine[1];
            }
        }
        for (int i2 = 0; i2 < cornersFromRect.length; i2 += 2) {
            float f5 = cornersFromRect[i2] + fArr[0];
            float f6 = cornersFromRect[i2 + 1] + fArr[1];
            if (!CropMath.inclusiveContains(this.outer, f5, f6)) {
                float[] fArr3 = {f5, f6};
                CropMath.getEdgePoints(this.outer, fArr3);
                fArr3[0] = fArr3[0] - f5;
                fArr3[1] = fArr3[1] - f6;
                fArr[0] = fArr[0] + fArr3[0];
                fArr[1] = fArr[1] + fArr3[1];
            }
        }
        for (int i3 = 0; i3 < cornersFromRect.length; i3 += 2) {
            int i4 = i3 + 1;
            float f7 = cornersFromRect[i4] + fArr[1];
            cornersFromRect[i3] = cornersFromRect[i3] + fArr[0];
            cornersFromRect[i4] = f7;
        }
        this.innerRotated = cornersFromRect;
        reconstrain();
    }

    public void resizeInner(RectF rectF) {
        Matrix rotMatrix = getRotMatrix();
        Matrix inverseRotMatrix = getInverseRotMatrix();
        float[] cornersFromRect = CropMath.getCornersFromRect(this.outer);
        rotMatrix.mapPoints(cornersFromRect);
        float[] cornersFromRect2 = CropMath.getCornersFromRect(this.inner);
        float[] cornersFromRect3 = CropMath.getCornersFromRect(rectF);
        RectF rectF2 = new RectF(rectF);
        for (int i = 0; i < cornersFromRect3.length; i += 2) {
            int i2 = i + 1;
            float[] fArr = {cornersFromRect3[i], cornersFromRect3[i2]};
            float[] copyOf = Arrays.copyOf(fArr, 2);
            inverseRotMatrix.mapPoints(copyOf);
            if (!CropMath.inclusiveContains(this.outer, copyOf[0], copyOf[1])) {
                float[] fArr2 = {cornersFromRect3[i], cornersFromRect3[i2], cornersFromRect2[i], cornersFromRect2[i2]};
                float[] lineIntersect = GeometryMathUtils.lineIntersect(fArr2, CropMath.closestSide(fArr, cornersFromRect));
                if (lineIntersect == null) {
                    lineIntersect = new float[]{cornersFromRect2[i], cornersFromRect2[i2]};
                }
                switch (i) {
                    case 0:
                    case 1:
                        float f = lineIntersect[0];
                        float f2 = rectF2.left;
                        if (f > f2) {
                            f2 = lineIntersect[0];
                        }
                        rectF2.left = f2;
                        float f3 = lineIntersect[1];
                        float f4 = rectF2.top;
                        if (f3 > f4) {
                            f4 = lineIntersect[1];
                        }
                        rectF2.top = f4;
                        break;
                    case 2:
                    case 3:
                        float f5 = lineIntersect[0];
                        float f6 = rectF2.right;
                        if (f5 < f6) {
                            f6 = lineIntersect[0];
                        }
                        rectF2.right = f6;
                        float f7 = lineIntersect[1];
                        float f8 = rectF2.top;
                        if (f7 > f8) {
                            f8 = lineIntersect[1];
                        }
                        rectF2.top = f8;
                        break;
                    case 4:
                    case 5:
                        float f9 = lineIntersect[0];
                        float f10 = rectF2.right;
                        if (f9 < f10) {
                            f10 = lineIntersect[0];
                        }
                        rectF2.right = f10;
                        float f11 = lineIntersect[1];
                        float f12 = rectF2.bottom;
                        if (f11 < f12) {
                            f12 = lineIntersect[1];
                        }
                        rectF2.bottom = f12;
                        break;
                    case 6:
                    case 7:
                        float f13 = lineIntersect[0];
                        float f14 = rectF2.left;
                        if (f13 > f14) {
                            f14 = lineIntersect[0];
                        }
                        rectF2.left = f14;
                        float f15 = lineIntersect[1];
                        float f16 = rectF2.bottom;
                        if (f15 < f16) {
                            f16 = lineIntersect[1];
                        }
                        rectF2.bottom = f16;
                        break;
                }
            }
        }
        float[] cornersFromRect4 = CropMath.getCornersFromRect(rectF2);
        inverseRotMatrix.mapPoints(cornersFromRect4);
        this.innerRotated = cornersFromRect4;
        reconstrain();
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x006d A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x006e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void fixedAspectResizeInner(android.graphics.RectF r19) {
        /*
            r18 = this;
            r0 = r18
            r1 = r19
            android.graphics.Matrix r2 = r18.getRotMatrix()
            android.graphics.Matrix r3 = r18.getInverseRotMatrix()
            android.graphics.RectF r4 = r0.inner
            float r4 = r4.width()
            android.graphics.RectF r5 = r0.inner
            float r5 = r5.height()
            float r4 = r4 / r5
            android.graphics.RectF r5 = r0.outer
            float[] r5 = com.android.camera.crop.CropMath.getCornersFromRect(r5)
            r2.mapPoints(r5)
            android.graphics.RectF r2 = r0.inner
            float[] r2 = com.android.camera.crop.CropMath.getCornersFromRect(r2)
            float[] r6 = com.android.camera.crop.CropMath.getCornersFromRect(r19)
            android.graphics.RectF r7 = r0.inner
            float r8 = r7.top
            float r9 = r1.top
            int r8 = (r8 > r9 ? 1 : (r8 == r9 ? 0 : -1))
            r10 = -1
            r11 = 4
            r12 = 2
            r13 = 0
            if (r8 != 0) goto L_0x004e
            float r8 = r7.left
            float r14 = r1.left
            int r8 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1))
            if (r8 != 0) goto L_0x0044
            r7 = r13
            goto L_0x006b
        L_0x0044:
            float r7 = r7.right
            float r8 = r1.right
            int r7 = (r7 > r8 ? 1 : (r7 == r8 ? 0 : -1))
            if (r7 != 0) goto L_0x006a
            r7 = r12
            goto L_0x006b
        L_0x004e:
            float r8 = r7.bottom
            float r14 = r1.bottom
            int r8 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1))
            if (r8 != 0) goto L_0x006a
            float r8 = r7.right
            float r14 = r1.right
            int r8 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1))
            if (r8 != 0) goto L_0x0060
            r7 = r11
            goto L_0x006b
        L_0x0060:
            float r7 = r7.left
            float r8 = r1.left
            int r7 = (r7 > r8 ? 1 : (r7 == r8 ? 0 : -1))
            if (r7 != 0) goto L_0x006a
            r7 = 6
            goto L_0x006b
        L_0x006a:
            r7 = r10
        L_0x006b:
            if (r7 != r10) goto L_0x006e
            return
        L_0x006e:
            float r1 = r19.width()
            r8 = r1
            r1 = r13
        L_0x0074:
            int r10 = r6.length
            if (r1 >= r10) goto L_0x00e5
            float[] r10 = new float[r12]
            r14 = r6[r1]
            r10[r13] = r14
            int r14 = r1 + 1
            r15 = r6[r14]
            r16 = 1
            r10[r16] = r15
            float[] r15 = java.util.Arrays.copyOf(r10, r12)
            r3.mapPoints(r15)
            android.graphics.RectF r9 = r0.outer
            r12 = r15[r13]
            r15 = r15[r16]
            boolean r9 = com.android.camera.crop.CropMath.inclusiveContains(r9, r12, r15)
            if (r9 != 0) goto L_0x00e1
            if (r1 != r7) goto L_0x009b
            goto L_0x00e1
        L_0x009b:
            float[] r9 = com.android.camera.crop.CropMath.closestSide(r10, r5)
            float[] r10 = new float[r11]
            r12 = r6[r1]
            r10[r13] = r12
            r12 = r6[r14]
            r10[r16] = r12
            r12 = r2[r1]
            r15 = 2
            r10[r15] = r12
            r12 = 3
            r17 = r2[r14]
            r10[r12] = r17
            float[] r9 = com.android.camera.crop.GeometryMathUtils.lineIntersect(r10, r9)
            if (r9 != 0) goto L_0x00c3
            float[] r9 = new float[r15]
            r10 = r2[r1]
            r9[r13] = r10
            r10 = r2[r14]
            r9[r16] = r10
        L_0x00c3:
            r10 = r2[r7]
            int r12 = r7 + 1
            r12 = r2[r12]
            r14 = r9[r13]
            float r10 = r10 - r14
            float r10 = java.lang.Math.abs(r10)
            r9 = r9[r16]
            float r12 = r12 - r9
            float r9 = java.lang.Math.abs(r12)
            float r9 = r9 * r4
            float r9 = java.lang.Math.max(r10, r9)
            int r10 = (r9 > r8 ? 1 : (r9 == r8 ? 0 : -1))
            if (r10 >= 0) goto L_0x00e1
            r8 = r9
        L_0x00e1:
            int r1 = r1 + 2
            r12 = 2
            goto L_0x0074
        L_0x00e5:
            float r1 = r8 / r4
            android.graphics.RectF r2 = new android.graphics.RectF
            android.graphics.RectF r4 = r0.inner
            r2.<init>(r4)
            if (r7 != 0) goto L_0x00fb
            float r4 = r2.left
            float r4 = r4 + r8
            r2.right = r4
            float r4 = r2.top
            float r4 = r4 + r1
            r2.bottom = r4
            goto L_0x0123
        L_0x00fb:
            r4 = 2
            if (r7 != r4) goto L_0x0109
            float r4 = r2.right
            float r4 = r4 - r8
            r2.left = r4
            float r4 = r2.top
            float r4 = r4 + r1
            r2.bottom = r4
            goto L_0x0123
        L_0x0109:
            if (r7 != r11) goto L_0x0116
            float r4 = r2.right
            float r4 = r4 - r8
            r2.left = r4
            float r4 = r2.bottom
            float r4 = r4 - r1
            r2.top = r4
            goto L_0x0123
        L_0x0116:
            r4 = 6
            if (r7 != r4) goto L_0x0123
            float r4 = r2.left
            float r4 = r4 + r8
            r2.right = r4
            float r4 = r2.bottom
            float r4 = r4 - r1
            r2.top = r4
        L_0x0123:
            float[] r1 = com.android.camera.crop.CropMath.getCornersFromRect(r2)
            r3.mapPoints(r1)
            r0.innerRotated = r1
            r18.reconstrain()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.crop.BoundedRect.fixedAspectResizeInner(android.graphics.RectF):void");
    }

    private boolean isConstrained() {
        for (int i = 0; i < 8; i += 2) {
            RectF rectF = this.outer;
            float[] fArr = this.innerRotated;
            if (!CropMath.inclusiveContains(rectF, fArr[i], fArr[i + 1])) {
                return false;
            }
        }
        return true;
    }

    private void reconstrain() {
        CropMath.getEdgePoints(this.outer, this.innerRotated);
        Matrix rotMatrix = getRotMatrix();
        float[] copyOf = Arrays.copyOf(this.innerRotated, 8);
        rotMatrix.mapPoints(copyOf);
        this.inner = CropMath.trapToRect(copyOf);
    }

    private void rotateInner() {
        getInverseRotMatrix().mapPoints(this.innerRotated);
    }

    private Matrix getRotMatrix() {
        Matrix matrix = new Matrix();
        matrix.setRotate(this.rot, this.outer.centerX(), this.outer.centerY());
        return matrix;
    }

    private Matrix getInverseRotMatrix() {
        Matrix matrix = new Matrix();
        matrix.setRotate(-this.rot, this.outer.centerX(), this.outer.centerY());
        return matrix;
    }
}
