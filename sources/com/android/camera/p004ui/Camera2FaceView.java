package com.android.camera.p004ui;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.params.Face;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import com.android.camera.ExtendedFace;

/* renamed from: com.android.camera.ui.Camera2FaceView */
public class Camera2FaceView extends FaceView {
    private final int blink_threshold = 60;
    private Rect mCameraBound;
    /* access modifiers changed from: private */
    public ExtendedFace[] mExFaces;
    /* access modifiers changed from: private */
    public Face[] mFaces;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == 1) {
                Camera2FaceView camera2FaceView = Camera2FaceView.this;
                camera2FaceView.mStateSwitchPending = false;
                camera2FaceView.mFaces = camera2FaceView.mPendingFaces;
                Camera2FaceView camera2FaceView2 = Camera2FaceView.this;
                camera2FaceView2.mExFaces = camera2FaceView2.mPendingExFaces;
                Camera2FaceView.this.invalidate();
            }
        }
    };
    private Rect mOriginalCameraBound;
    /* access modifiers changed from: private */
    public ExtendedFace[] mPendingExFaces;
    /* access modifiers changed from: private */
    public Face[] mPendingFaces;
    private float mZoom = 1.0f;
    private final int smile_threashold_no_smile = 30;
    private final int smile_threashold_small_smile = 60;

    public Camera2FaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setCameraBound(Rect rect) {
        this.mCameraBound = rect;
    }

    public void setOriginalCameraBound(Rect rect) {
        this.mOriginalCameraBound = rect;
    }

    public void setZoom(float f) {
        this.mZoom = f;
    }

    public void setFaces(Face[] faceArr, ExtendedFace[] extendedFaceArr) {
        if (this.LOGV) {
            StringBuilder sb = new StringBuilder();
            sb.append("Num of faces=");
            sb.append(faceArr.length);
            Log.v("CAM_FaceView", sb.toString());
        }
        if (!this.mPause) {
            Face[] faceArr2 = this.mFaces;
            if (faceArr2 == null || ((faceArr.length <= 0 || faceArr2.length != 0) && (faceArr.length != 0 || this.mFaces.length <= 0))) {
                if (this.mStateSwitchPending) {
                    this.mStateSwitchPending = false;
                    this.mHandler.removeMessages(1);
                }
                this.mFaces = faceArr;
                this.mExFaces = extendedFaceArr;
                if (!this.mBlocked) {
                    Face[] faceArr3 = this.mFaces;
                    if (!(faceArr3 == null || faceArr3.length <= 0 || this.mCameraBound == null)) {
                        invalidate();
                    }
                }
                return;
            }
            this.mPendingFaces = faceArr;
            this.mPendingExFaces = extendedFaceArr;
            if (!this.mStateSwitchPending) {
                this.mStateSwitchPending = true;
                this.mHandler.sendEmptyMessageDelayed(1, 70);
            }
        }
    }

    private boolean isFDRectOutOfBound(Rect rect) {
        Rect rect2 = this.mCameraBound;
        return rect2.left > rect.left || rect2.top > rect.top || rect.right > rect2.right || rect.bottom > rect2.bottom;
    }

    public boolean faceExists() {
        Face[] faceArr = this.mFaces;
        return faceArr != null && faceArr.length > 0;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0023, code lost:
        if (r3 != 180) goto L_0x0025;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002b, code lost:
        if (r3 != 270) goto L_0x0032;
     */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0542  */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x067e  */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x06a9 A[EDGE_INSN: B:116:0x06a9->B:112:0x06a9 ?: BREAK  
    EDGE_INSN: B:116:0x06a9->B:112:0x06a9 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x010f  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0112  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x011b  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x02bd  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x034a  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0440  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x04c2  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDraw(android.graphics.Canvas r41) {
        /*
            r40 = this;
            r0 = r40
            r7 = r41
            boolean r1 = r0.mBlocked
            if (r1 != 0) goto L_0x06ae
            android.hardware.camera2.params.Face[] r1 = r0.mFaces
            if (r1 == 0) goto L_0x06ae
            int r1 = r1.length
            if (r1 <= 0) goto L_0x06ae
            android.graphics.Rect r1 = r0.mCameraBound
            if (r1 == 0) goto L_0x06ae
            int r1 = r0.mUncroppedWidth
            int r2 = r0.mUncroppedHeight
            r8 = 270(0x10e, float:3.78E-43)
            r9 = 180(0xb4, float:2.52E-43)
            r10 = 90
            if (r2 <= r1) goto L_0x0025
            int r3 = r0.mDisplayOrientation
            if (r3 == 0) goto L_0x002d
            if (r3 == r9) goto L_0x002d
        L_0x0025:
            if (r1 <= r2) goto L_0x0032
            int r3 = r0.mDisplayOrientation
            if (r3 == r10) goto L_0x002d
            if (r3 != r8) goto L_0x0032
        L_0x002d:
            r39 = r2
            r2 = r1
            r1 = r39
        L_0x0032:
            android.graphics.Rect r3 = r0.mCameraBound
            int r3 = r3.width()
            int r3 = r3 * r1
            android.graphics.Rect r4 = r0.mCameraBound
            int r4 = r4.height()
            int r4 = r4 * r2
            if (r3 == r4) goto L_0x006e
            if (r1 == r2) goto L_0x0060
            int r3 = r2 * 288
            int r4 = r1 * 352
            if (r3 == r4) goto L_0x0060
            int r3 = r2 * 480
            int r4 = r1 * 800
            if (r3 != r4) goto L_0x0051
            goto L_0x0060
        L_0x0051:
            android.graphics.Rect r1 = r0.mCameraBound
            int r1 = r1.height()
            int r1 = r1 * r2
            android.graphics.Rect r3 = r0.mCameraBound
            int r3 = r3.width()
            int r1 = r1 / r3
            goto L_0x006e
        L_0x0060:
            android.graphics.Rect r2 = r0.mCameraBound
            int r2 = r2.width()
            int r2 = r2 * r1
            android.graphics.Rect r3 = r0.mCameraBound
            int r3 = r3.height()
            int r2 = r2 / r3
        L_0x006e:
            android.graphics.Matrix r3 = r0.mMatrix
            boolean r4 = r0.mMirror
            int r5 = r0.mDisplayOrientation
            com.android.camera.util.CameraUtil.prepareMatrix(r3, r4, r5, r1, r2)
            android.graphics.Matrix r11 = new android.graphics.Matrix
            r11.<init>()
            android.graphics.Rect r3 = r0.mCameraBound
            int r3 = r3.width()
            int r3 = -r3
            float r3 = (float) r3
            r4 = 1073741824(0x40000000, float:2.0)
            float r3 = r3 / r4
            android.graphics.Rect r5 = r0.mCameraBound
            int r5 = r5.height()
            int r5 = -r5
            float r5 = (float) r5
            float r5 = r5 / r4
            r11.preTranslate(r3, r5)
            android.graphics.Rect r3 = r0.mCameraBound
            int r3 = r3.width()
            float r3 = (float) r3
            r5 = 1157234688(0x44fa0000, float:2000.0)
            float r3 = r5 / r3
            android.graphics.Rect r6 = r0.mCameraBound
            int r6 = r6.height()
            float r6 = (float) r6
            float r6 = r5 / r6
            r11.postScale(r3, r6)
            android.graphics.Matrix r12 = new android.graphics.Matrix
            r12.<init>()
            android.graphics.Rect r3 = r0.mCameraBound
            int r3 = r3.width()
            int r3 = -r3
            float r3 = (float) r3
            float r3 = r3 / r4
            float r6 = r0.mZoom
            float r3 = r3 * r6
            android.graphics.Rect r6 = r0.mCameraBound
            int r6 = r6.height()
            int r6 = -r6
            float r6 = (float) r6
            float r6 = r6 / r4
            float r4 = r0.mZoom
            float r6 = r6 * r4
            r12.preTranslate(r3, r6)
            android.graphics.Rect r3 = r0.mCameraBound
            int r3 = r3.width()
            float r3 = (float) r3
            float r3 = r5 / r3
            android.graphics.Rect r4 = r0.mCameraBound
            int r4 = r4.height()
            float r4 = (float) r4
            float r5 = r5 / r4
            r12.postScale(r3, r5)
            int r3 = r40.getWidth()
            int r4 = r0.mUncroppedWidth
            int r3 = r3 - r4
            r13 = 2
            int r3 = r3 / r13
            int r1 = r1 - r4
            int r1 = r1 / r13
            int r14 = r3 - r1
            int r1 = r40.getHeight()
            int r3 = r0.mUncroppedHeight
            int r1 = r1 - r3
            int r1 = r1 / r13
            int r2 = r2 - r3
            int r2 = r2 / r13
            int r15 = r1 - r2
            r41.save()
            android.graphics.Matrix r1 = r0.mMatrix
            int r2 = r0.mOrientation
            float r2 = (float) r2
            r1.postRotate(r2)
            int r1 = r0.mOrientation
            int r1 = -r1
            float r1 = (float) r1
            r7.rotate(r1)
            com.android.camera.ExtendedFace[] r1 = r0.mExFaces
            r16 = 0
            if (r1 != 0) goto L_0x0112
            r6 = r16
            goto L_0x0114
        L_0x0112:
            int r1 = r1.length
            r6 = r1
        L_0x0114:
            r5 = r16
        L_0x0116:
            android.hardware.camera2.params.Face[] r1 = r0.mFaces
            int r2 = r1.length
            if (r5 >= r2) goto L_0x06a9
            r1 = r1[r5]
            int r1 = r1.getScore()
            r2 = 50
            if (r1 >= r2) goto L_0x013a
        L_0x0125:
            r24 = r5
            r25 = r6
            r3 = r7
            r26 = r9
            r23 = r11
            r7 = r13
            r20 = r14
            r34 = r15
            r39 = r10
            r10 = r8
            r8 = r39
            goto L_0x0694
        L_0x013a:
            android.hardware.camera2.params.Face[] r1 = r0.mFaces
            r1 = r1[r5]
            android.graphics.Rect r1 = r1.getBounds()
            android.graphics.Rect r2 = r0.mOriginalCameraBound
            int r3 = r2.left
            int r3 = -r3
            int r2 = r2.top
            int r2 = -r2
            r1.offset(r3, r2)
            boolean r2 = r0.isFDRectOutOfBound(r1)
            if (r2 == 0) goto L_0x0154
            goto L_0x0125
        L_0x0154:
            android.graphics.RectF r2 = r0.mRect
            r2.set(r1)
            float r2 = r0.mZoom
            r3 = 1065353216(0x3f800000, float:1.0)
            int r2 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r2 == 0) goto L_0x0181
            android.graphics.RectF r2 = r0.mRect
            float r3 = r2.left
            android.graphics.Rect r4 = r0.mCameraBound
            int r8 = r4.left
            float r10 = (float) r8
            float r3 = r3 - r10
            r2.left = r3
            float r3 = r2.right
            float r8 = (float) r8
            float r3 = r3 - r8
            r2.right = r3
            float r3 = r2.top
            int r4 = r4.top
            float r8 = (float) r4
            float r3 = r3 - r8
            r2.top = r3
            float r3 = r2.bottom
            float r4 = (float) r4
            float r3 = r3 - r4
            r2.bottom = r3
        L_0x0181:
            android.graphics.RectF r2 = r0.mRect
            r11.mapRect(r2)
            boolean r2 = r0.LOGV
            if (r2 == 0) goto L_0x0191
            android.graphics.RectF r2 = r0.mRect
            java.lang.String r3 = "Original rect"
            com.android.camera.util.CameraUtil.dumpRect(r2, r3)
        L_0x0191:
            android.graphics.Matrix r2 = r0.mMatrix
            android.graphics.RectF r3 = r0.mRect
            r2.mapRect(r3)
            boolean r2 = r0.LOGV
            if (r2 == 0) goto L_0x01a3
            android.graphics.RectF r2 = r0.mRect
            java.lang.String r3 = "Transformed rect"
            com.android.camera.util.CameraUtil.dumpRect(r2, r3)
        L_0x01a3:
            android.graphics.Paint r2 = r0.mPaint
            int r3 = r0.mColor
            r2.setColor(r3)
            android.graphics.RectF r2 = r0.mRect
            float r8 = (float) r14
            float r10 = (float) r15
            r2.offset(r8, r10)
            android.graphics.RectF r2 = r0.mRect
            android.graphics.Paint r3 = r0.mPaint
            r7.drawRect(r2, r3)
            if (r5 >= r6) goto L_0x0682
            com.android.camera.ExtendedFace[] r2 = r0.mExFaces
            r3 = r2[r5]
            if (r3 == 0) goto L_0x0682
            r17 = r2[r5]
            android.hardware.camera2.params.Face[] r2 = r0.mFaces
            r18 = r2[r5]
            r2 = 4
            float[] r4 = new float[r2]
            int r2 = r1.width()
            int r2 = r2 / 12
            int r1 = r1.height()
            int r1 = r1 / 12
            float r2 = (float) r2
            float r3 = r0.mZoom
            float r2 = r2 * r3
            int r2 = (int) r2
            float r1 = (float) r1
            float r1 = r1 * r3
            int r3 = (int) r1
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r13 = "blink: ("
            r1.append(r13)
            int r13 = r17.getLeyeBlink()
            r1.append(r13)
            java.lang.String r13 = ", "
            r1.append(r13)
            int r13 = r17.getReyeBlink()
            r1.append(r13)
            java.lang.String r13 = ")"
            r1.append(r13)
            java.lang.String r1 = r1.toString()
            java.lang.String r13 = "CAM_FaceView"
            android.util.Log.e(r13, r1)
            android.graphics.Point r1 = r18.getLeftEyePosition()
            r21 = 3
            r22 = 1
            if (r1 == 0) goto L_0x02ad
            int r1 = r0.mDisplayRotation
            if (r1 == 0) goto L_0x0245
            r9 = 180(0xb4, float:2.52E-43)
            if (r1 != r9) goto L_0x021b
            goto L_0x0245
        L_0x021b:
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.x
            int r9 = r2 / 2
            int r1 = r1 - r9
            float r1 = (float) r1
            r4[r16] = r1
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.y
            float r1 = (float) r1
            r4[r22] = r1
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.x
            int r1 = r1 + r9
            float r1 = (float) r1
            r9 = 2
            r4[r9] = r1
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.y
            float r1 = (float) r1
            r4[r21] = r1
            goto L_0x026f
        L_0x0245:
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.x
            float r1 = (float) r1
            r4[r16] = r1
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.y
            int r9 = r3 / 2
            int r1 = r1 - r9
            float r1 = (float) r1
            r4[r22] = r1
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.x
            float r1 = (float) r1
            r19 = 2
            r4[r19] = r1
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.y
            int r1 = r1 + r9
            float r1 = (float) r1
            r4[r21] = r1
        L_0x026f:
            r12.mapPoints(r4)
            android.graphics.Matrix r1 = r0.mMatrix
            r1.mapPoints(r4)
            int r1 = r17.getLeyeBlink()
            r9 = 60
            if (r1 < r9) goto L_0x02ad
            r1 = r4[r16]
            float r9 = r1 + r8
            r1 = r4[r22]
            float r23 = r1 + r10
            r1 = 2
            r24 = r4[r1]
            float r24 = r24 + r8
            r1 = r4[r21]
            float r25 = r1 + r10
            android.graphics.Paint r1 = r0.mPaint
            r26 = r1
            r1 = r41
            r27 = r2
            r2 = r9
            r9 = r3
            r3 = r23
            r23 = r11
            r11 = r4
            r4 = r24
            r24 = r5
            r5 = r25
            r25 = r6
            r6 = r26
            r1.drawLine(r2, r3, r4, r5, r6)
            goto L_0x02b7
        L_0x02ad:
            r27 = r2
            r9 = r3
            r24 = r5
            r25 = r6
            r23 = r11
            r11 = r4
        L_0x02b7:
            android.graphics.Point r1 = r18.getRightEyePosition()
            if (r1 == 0) goto L_0x034a
            int r1 = r0.mDisplayRotation
            r6 = 180(0xb4, float:2.52E-43)
            if (r1 == 0) goto L_0x02f0
            if (r1 != r6) goto L_0x02c6
            goto L_0x02f0
        L_0x02c6:
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.x
            int r2 = r27 / 2
            int r1 = r1 - r2
            float r1 = (float) r1
            r11[r16] = r1
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.y
            float r1 = (float) r1
            r11[r22] = r1
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.x
            int r1 = r1 + r2
            float r1 = (float) r1
            r2 = 2
            r11[r2] = r1
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.y
            float r1 = (float) r1
            r11[r21] = r1
            goto L_0x0319
        L_0x02f0:
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.x
            float r1 = (float) r1
            r11[r16] = r1
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.y
            int r3 = r9 / 2
            int r1 = r1 - r3
            float r1 = (float) r1
            r11[r22] = r1
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.x
            float r1 = (float) r1
            r2 = 2
            r11[r2] = r1
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.y
            int r1 = r1 + r3
            float r1 = (float) r1
            r11[r21] = r1
        L_0x0319:
            r12.mapPoints(r11)
            android.graphics.Matrix r1 = r0.mMatrix
            r1.mapPoints(r11)
            int r1 = r17.getReyeBlink()
            r2 = 60
            if (r1 < r2) goto L_0x0347
            r1 = r11[r16]
            float r2 = r1 + r8
            r1 = r11[r22]
            float r3 = r1 + r10
            r1 = 2
            r4 = r11[r1]
            float r4 = r4 + r8
            r1 = r11[r21]
            float r5 = r1 + r10
            android.graphics.Paint r1 = r0.mPaint
            r20 = r1
            r1 = r41
            r26 = r6
            r6 = r20
            r1.drawLine(r2, r3, r4, r5, r6)
            goto L_0x034c
        L_0x0347:
            r26 = r6
            goto L_0x034c
        L_0x034a:
            r26 = 180(0xb4, float:2.52E-43)
        L_0x034c:
            int r1 = r17.getLeftrightGaze()
            if (r1 != 0) goto L_0x0369
            int r1 = r17.getTopbottomGaze()
            if (r1 == 0) goto L_0x0359
            goto L_0x0369
        L_0x0359:
            r37 = r8
            r36 = r9
            r35 = r10
            r20 = r14
            r34 = r15
        L_0x0363:
            r8 = 90
            r10 = 270(0x10e, float:3.78E-43)
            goto L_0x053c
        L_0x0369:
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.x
            android.graphics.Point r2 = r18.getRightEyePosition()
            int r2 = r2.x
            int r1 = r1 - r2
            android.graphics.Point r2 = r18.getLeftEyePosition()
            int r2 = r2.x
            android.graphics.Point r3 = r18.getRightEyePosition()
            int r3 = r3.x
            int r2 = r2 - r3
            int r1 = r1 * r2
            android.graphics.Point r2 = r18.getLeftEyePosition()
            int r2 = r2.y
            android.graphics.Point r3 = r18.getRightEyePosition()
            int r3 = r3.y
            int r2 = r2 - r3
            android.graphics.Point r3 = r18.getLeftEyePosition()
            int r3 = r3.y
            android.graphics.Point r4 = r18.getRightEyePosition()
            int r4 = r4.y
            int r3 = r3 - r4
            int r2 = r2 * r3
            int r1 = r1 + r2
            double r1 = (double) r1
            double r1 = java.lang.Math.sqrt(r1)
            r3 = 4611686018427387904(0x4000000000000000, double:2.0)
            double r1 = r1 / r3
            int r3 = r17.getLeftrightGaze()
            int r3 = -r3
            double r3 = (double) r3
            int r5 = r17.getTopbottomGaze()
            int r5 = -r5
            double r5 = (double) r5
            r28 = 4640537203540230144(0x4066800000000000, double:180.0)
            double r30 = r3 / r28
            r32 = 4614256656552045848(0x400921fb54442d18, double:3.141592653589793)
            double r30 = r30 * r32
            r20 = r14
            r34 = r15
            double r14 = java.lang.Math.sin(r30)
            double r14 = -r14
            int r7 = r17.getRollDirection()
            int r7 = -r7
            r36 = r9
            r35 = r10
            double r9 = (double) r7
            double r9 = r9 / r28
            double r9 = r9 * r32
            double r9 = java.lang.Math.cos(r9)
            double r14 = r14 * r9
            double r5 = r5 / r28
            double r5 = r5 * r32
            double r9 = java.lang.Math.sin(r5)
            double r37 = java.lang.Math.cos(r30)
            double r9 = r9 * r37
            int r7 = r17.getRollDirection()
            int r7 = -r7
            r37 = r8
            double r7 = (double) r7
            double r7 = r7 / r28
            double r7 = r7 * r32
            double r7 = java.lang.Math.sin(r7)
            double r9 = r9 * r7
            double r14 = r14 + r9
            double r1 = -r1
            double r14 = r14 * r1
            r7 = 4602678819172646912(0x3fe0000000000000, double:0.5)
            double r14 = r14 + r7
            float r9 = (float) r14
            double r3 = -r3
            double r3 = r3 / r28
            double r3 = r3 * r32
            double r3 = java.lang.Math.sin(r3)
            int r10 = r17.getRollDirection()
            int r10 = -r10
            double r14 = (double) r10
            double r14 = r14 / r28
            double r14 = r14 * r32
            double r14 = java.lang.Math.sin(r14)
            double r3 = r3 * r14
            double r5 = java.lang.Math.sin(r5)
            double r14 = java.lang.Math.cos(r30)
            double r5 = r5 * r14
            int r10 = r17.getRollDirection()
            int r10 = -r10
            double r14 = (double) r10
            double r14 = r14 / r28
            double r14 = r14 * r32
            double r14 = java.lang.Math.cos(r14)
            double r5 = r5 * r14
            double r3 = r3 - r5
            double r3 = r3 * r1
            double r3 = r3 + r7
            float r7 = (float) r3
            int r1 = r17.getLeyeBlink()
            r2 = 60
            if (r1 >= r2) goto L_0x04ba
            int r1 = r0.mDisplayRotation
            r2 = 90
            if (r1 == r2) goto L_0x0473
            r2 = 270(0x10e, float:3.78E-43)
            if (r1 != r2) goto L_0x044b
            goto L_0x0473
        L_0x044b:
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.x
            float r1 = (float) r1
            r11[r16] = r1
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.y
            float r1 = (float) r1
            r11[r22] = r1
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.x
            float r1 = (float) r1
            float r1 = r1 + r7
            r2 = 2
            r11[r2] = r1
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.y
            float r1 = (float) r1
            float r1 = r1 + r9
            r11[r21] = r1
            goto L_0x049a
        L_0x0473:
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.x
            float r1 = (float) r1
            r11[r16] = r1
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.y
            float r1 = (float) r1
            r11[r22] = r1
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.x
            float r1 = (float) r1
            float r1 = r1 + r9
            r2 = 2
            r11[r2] = r1
            android.graphics.Point r1 = r18.getLeftEyePosition()
            int r1 = r1.y
            float r1 = (float) r1
            float r1 = r1 + r7
            r11[r21] = r1
        L_0x049a:
            r12.mapPoints(r11)
            android.graphics.Matrix r1 = r0.mMatrix
            r1.mapPoints(r11)
            r1 = r11[r16]
            float r2 = r1 + r37
            r1 = r11[r22]
            float r3 = r1 + r35
            r1 = 2
            r4 = r11[r1]
            float r4 = r4 + r37
            r1 = r11[r21]
            float r5 = r1 + r35
            android.graphics.Paint r6 = r0.mPaint
            r1 = r41
            r1.drawLine(r2, r3, r4, r5, r6)
        L_0x04ba:
            int r1 = r17.getReyeBlink()
            r2 = 60
            if (r1 >= r2) goto L_0x0363
            int r1 = r0.mDisplayRotation
            r8 = 90
            r10 = 270(0x10e, float:3.78E-43)
            if (r1 == r8) goto L_0x04f5
            if (r1 != r10) goto L_0x04cd
            goto L_0x04f5
        L_0x04cd:
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.x
            float r1 = (float) r1
            r11[r16] = r1
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.y
            float r1 = (float) r1
            r11[r22] = r1
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.x
            float r1 = (float) r1
            float r1 = r1 + r7
            r2 = 2
            r11[r2] = r1
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.y
            float r1 = (float) r1
            float r1 = r1 + r9
            r11[r21] = r1
            goto L_0x051c
        L_0x04f5:
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.x
            float r1 = (float) r1
            r11[r16] = r1
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.y
            float r1 = (float) r1
            r11[r22] = r1
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.x
            float r1 = (float) r1
            float r1 = r1 + r9
            r2 = 2
            r11[r2] = r1
            android.graphics.Point r1 = r18.getRightEyePosition()
            int r1 = r1.y
            float r1 = (float) r1
            float r1 = r1 + r7
            r11[r21] = r1
        L_0x051c:
            r12.mapPoints(r11)
            android.graphics.Matrix r1 = r0.mMatrix
            r1.mapPoints(r11)
            r1 = r11[r16]
            float r2 = r1 + r37
            r1 = r11[r22]
            float r3 = r1 + r35
            r1 = 2
            r4 = r11[r1]
            float r4 = r4 + r37
            r1 = r11[r21]
            float r5 = r1 + r35
            android.graphics.Paint r6 = r0.mPaint
            r1 = r41
            r1.drawLine(r2, r3, r4, r5, r6)
        L_0x053c:
            android.graphics.Point r1 = r18.getMouthPosition()
            if (r1 == 0) goto L_0x067e
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "smile: "
            r1.append(r2)
            int r2 = r17.getSmileDegree()
            r1.append(r2)
            java.lang.String r2 = ","
            r1.append(r2)
            int r2 = r17.getSmileConfidence()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.e(r13, r1)
            int r1 = r17.getSmileDegree()
            r2 = 30
            if (r1 >= r2) goto L_0x05dd
            android.graphics.Point r1 = r18.getMouthPosition()
            int r1 = r1.x
            int r1 = r1 + r20
            int r1 = r1 - r27
            float r1 = (float) r1
            r11[r16] = r1
            android.graphics.Point r1 = r18.getMouthPosition()
            int r1 = r1.y
            float r1 = (float) r1
            r11[r22] = r1
            android.graphics.Point r1 = r18.getMouthPosition()
            int r1 = r1.x
            int r1 = r1 + r20
            int r1 = r1 + r27
            float r1 = (float) r1
            r2 = 2
            r11[r2] = r1
            android.graphics.Point r1 = r18.getMouthPosition()
            int r1 = r1.y
            float r1 = (float) r1
            r11[r21] = r1
            android.graphics.Matrix r1 = new android.graphics.Matrix
            r1.<init>()
            int r2 = r17.getRollDirection()
            float r2 = (float) r2
            android.graphics.Point r3 = r18.getMouthPosition()
            int r3 = r3.x
            float r3 = (float) r3
            android.graphics.Point r4 = r18.getMouthPosition()
            int r4 = r4.y
            float r4 = (float) r4
            r1.preRotate(r2, r3, r4)
            r1.mapPoints(r11)
            r12.mapPoints(r11)
            android.graphics.Matrix r1 = r0.mMatrix
            r1.mapPoints(r11)
            r1 = r11[r16]
            float r2 = r1 + r37
            r1 = r11[r22]
            float r3 = r1 + r35
            r7 = 2
            r1 = r11[r7]
            float r4 = r1 + r37
            r1 = r11[r21]
            float r5 = r1 + r35
            android.graphics.Paint r6 = r0.mPaint
            r1 = r41
            r1.drawLine(r2, r3, r4, r5, r6)
        L_0x05d9:
            r3 = r41
            goto L_0x0694
        L_0x05dd:
            r7 = 2
            int r1 = r17.getSmileDegree()
            r2 = 60
            if (r1 >= r2) goto L_0x0636
            int r1 = r0.mDisplayRotation
            int r1 = 360 - r1
            android.graphics.RectF r2 = r0.mRect
            android.graphics.Point r3 = r18.getMouthPosition()
            int r3 = r3.x
            int r3 = r3 - r27
            float r3 = (float) r3
            android.graphics.Point r4 = r18.getMouthPosition()
            int r4 = r4.y
            int r4 = r4 - r36
            float r4 = (float) r4
            android.graphics.Point r5 = r18.getMouthPosition()
            int r5 = r5.x
            int r5 = r5 + r27
            float r5 = (float) r5
            android.graphics.Point r6 = r18.getMouthPosition()
            int r6 = r6.y
            int r6 = r6 + r36
            float r6 = (float) r6
            r2.set(r3, r4, r5, r6)
            android.graphics.RectF r2 = r0.mRect
            r12.mapRect(r2)
            android.graphics.Matrix r2 = r0.mMatrix
            android.graphics.RectF r3 = r0.mRect
            r2.mapRect(r3)
            android.graphics.RectF r2 = r0.mRect
            r4 = r35
            r3 = r37
            r2.offset(r3, r4)
            android.graphics.RectF r2 = r0.mRect
            float r3 = (float) r1
            r4 = 1127481344(0x43340000, float:180.0)
            r5 = 1
            android.graphics.Paint r6 = r0.mPaint
            r1 = r41
            r1.drawArc(r2, r3, r4, r5, r6)
            goto L_0x05d9
        L_0x0636:
            r4 = r35
            r3 = r37
            android.graphics.RectF r1 = r0.mRect
            android.graphics.Point r2 = r18.getMouthPosition()
            int r2 = r2.x
            int r2 = r2 - r27
            float r2 = (float) r2
            android.graphics.Point r5 = r18.getMouthPosition()
            int r5 = r5.y
            int r5 = r5 - r36
            float r5 = (float) r5
            android.graphics.Point r6 = r18.getMouthPosition()
            int r6 = r6.x
            int r6 = r6 + r27
            float r6 = (float) r6
            android.graphics.Point r9 = r18.getMouthPosition()
            int r9 = r9.y
            int r9 = r9 + r36
            float r9 = (float) r9
            r1.set(r2, r5, r6, r9)
            android.graphics.RectF r1 = r0.mRect
            r12.mapRect(r1)
            android.graphics.Matrix r1 = r0.mMatrix
            android.graphics.RectF r2 = r0.mRect
            r1.mapRect(r2)
            android.graphics.RectF r1 = r0.mRect
            r1.offset(r3, r4)
            android.graphics.RectF r1 = r0.mRect
            android.graphics.Paint r2 = r0.mPaint
            r3 = r41
            r3.drawOval(r1, r2)
            goto L_0x0694
        L_0x067e:
            r3 = r41
            r7 = 2
            goto L_0x0694
        L_0x0682:
            r24 = r5
            r25 = r6
            r3 = r7
            r26 = r9
            r23 = r11
            r7 = r13
            r20 = r14
            r34 = r15
            r8 = 90
            r10 = 270(0x10e, float:3.78E-43)
        L_0x0694:
            int r5 = r24 + 1
            r13 = r7
            r14 = r20
            r11 = r23
            r6 = r25
            r9 = r26
            r15 = r34
            r7 = r3
            r39 = r10
            r10 = r8
            r8 = r39
            goto L_0x0116
        L_0x06a9:
            r3 = r7
            r41.restore()
            goto L_0x06af
        L_0x06ae:
            r3 = r7
        L_0x06af:
            super.onDraw(r41)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.p004ui.Camera2FaceView.onDraw(android.graphics.Canvas):void");
    }

    public void clear() {
        this.mColor = this.mFocusingColor;
        this.mFaces = null;
        this.mExFaces = null;
        invalidate();
    }
}
