package com.android.camera.p004ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.hardware.Camera.Face;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.android.camera.PhotoUI.SurfaceTextureSizeChangedListener;
import com.android.camera.util.PersistUtil;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.FaceView */
public class FaceView extends View implements FocusIndicator, Rotatable, SurfaceTextureSizeChangedListener {
    protected final boolean LOGV;
    private final int blink_threshold;
    protected volatile boolean mBlocked;
    protected int mColor;
    protected int mDisplayOrientation;
    protected int mDisplayRotation;
    /* access modifiers changed from: private */
    public Face[] mFaces;
    private final int mFailColor;
    private final int mFocusedColor;
    protected final int mFocusingColor;
    private Handler mHandler;
    protected Matrix mMatrix;
    protected boolean mMirror;
    protected int mOrientation;
    protected Paint mPaint;
    protected boolean mPause;
    /* access modifiers changed from: private */
    public Face[] mPendingFaces;
    protected RectF mRect;
    protected boolean mStateSwitchPending;
    protected int mUncroppedHeight;
    protected int mUncroppedWidth;
    private final int smile_threashold_no_smile;
    private final int smile_threashold_small_smile;

    public FaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.LOGV = PersistUtil.getCamera2Debug() == 2 || PersistUtil.getCamera2Debug() == 100;
        this.mMatrix = new Matrix();
        this.mRect = new RectF();
        this.smile_threashold_no_smile = 30;
        this.smile_threashold_small_smile = 60;
        this.blink_threshold = 60;
        this.mDisplayRotation = 0;
        this.mStateSwitchPending = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message message) {
                if (message.what == 1) {
                    FaceView faceView = FaceView.this;
                    faceView.mStateSwitchPending = false;
                    faceView.mFaces = faceView.mPendingFaces;
                    FaceView.this.invalidate();
                }
            }
        };
        Resources resources = getResources();
        this.mFocusingColor = resources.getColor(C0905R.color.face_detect_start);
        this.mFocusedColor = resources.getColor(C0905R.color.face_detect_success);
        this.mFailColor = resources.getColor(C0905R.color.face_detect_fail);
        this.mColor = this.mFocusingColor;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeWidth(resources.getDimension(C0905R.dimen.face_circle_stroke));
        this.mPaint.setDither(true);
        this.mPaint.setColor(-1);
        this.mPaint.setStrokeCap(Cap.ROUND);
    }

    public void onSurfaceTextureSizeChanged(int i, int i2) {
        this.mUncroppedWidth = i;
        this.mUncroppedHeight = i2;
    }

    public void setFaces(Face[] faceArr) {
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
                if (!this.mBlocked) {
                    Face[] faceArr3 = this.mFaces;
                    if (faceArr3 != null && faceArr3.length > 0) {
                        invalidate();
                    }
                }
                return;
            }
            this.mPendingFaces = faceArr;
            if (!this.mStateSwitchPending) {
                this.mStateSwitchPending = true;
                this.mHandler.sendEmptyMessageDelayed(1, 70);
            }
        }
    }

    public void setDisplayOrientation(int i) {
        this.mDisplayOrientation = i;
        if (this.LOGV) {
            StringBuilder sb = new StringBuilder();
            sb.append("mDisplayOrientation=");
            sb.append(i);
            Log.v("CAM_FaceView", sb.toString());
        }
    }

    public void setOrientation(int i, boolean z) {
        this.mOrientation = i;
        invalidate();
    }

    public void setMirror(boolean z) {
        this.mMirror = z;
        if (this.LOGV) {
            StringBuilder sb = new StringBuilder();
            sb.append("mMirror=");
            sb.append(z);
            Log.v("CAM_FaceView", sb.toString());
        }
    }

    public boolean faceExists() {
        Face[] faceArr = this.mFaces;
        return faceArr != null && faceArr.length > 0;
    }

    public void showStart() {
        this.mColor = this.mFocusingColor;
        invalidate();
    }

    public void showSuccess(boolean z) {
        this.mColor = this.mFocusedColor;
        invalidate();
    }

    public void showFail(boolean z) {
        this.mColor = this.mFailColor;
        invalidate();
    }

    public void clear() {
        this.mColor = this.mFocusingColor;
        this.mFaces = null;
        invalidate();
    }

    public void pause() {
        this.mPause = true;
    }

    public void resume() {
        this.mPause = false;
    }

    public void setBlockDraw(boolean z) {
        this.mBlocked = z;
    }

    public void setDisplayRotation(int i) {
        this.mDisplayRotation = i;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001f, code lost:
        if (r3 != 180) goto L_0x0021;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0027, code lost:
        if (r3 != 270) goto L_0x002e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x005f  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x01b6  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0252  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x035c  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x03db  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x03e9  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x046d  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x05b2  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x05c1 A[EDGE_INSN: B:90:0x05c1->B:87:0x05c1 ?: BREAK  
    EDGE_INSN: B:90:0x05c1->B:87:0x05c1 ?: BREAK  , SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDraw(android.graphics.Canvas r36) {
        /*
            r35 = this;
            r0 = r35
            r7 = r36
            boolean r1 = r0.mBlocked
            if (r1 != 0) goto L_0x05c4
            android.hardware.Camera$Face[] r1 = r0.mFaces
            if (r1 == 0) goto L_0x05c4
            int r1 = r1.length
            if (r1 <= 0) goto L_0x05c4
            int r1 = r0.mUncroppedWidth
            int r2 = r0.mUncroppedHeight
            r8 = 270(0x10e, float:3.78E-43)
            r9 = 180(0xb4, float:2.52E-43)
            r10 = 90
            if (r2 <= r1) goto L_0x0021
            int r3 = r0.mDisplayOrientation
            if (r3 == 0) goto L_0x0029
            if (r3 == r9) goto L_0x0029
        L_0x0021:
            if (r1 <= r2) goto L_0x002e
            int r3 = r0.mDisplayOrientation
            if (r3 == r10) goto L_0x0029
            if (r3 != r8) goto L_0x002e
        L_0x0029:
            r34 = r2
            r2 = r1
            r1 = r34
        L_0x002e:
            android.graphics.Matrix r3 = r0.mMatrix
            boolean r4 = r0.mMirror
            int r5 = r0.mDisplayOrientation
            com.android.camera.util.CameraUtil.prepareMatrix(r3, r4, r5, r1, r2)
            int r3 = r35.getWidth()
            int r3 = r3 - r1
            r11 = 2
            int r12 = r3 / 2
            int r1 = r35.getHeight()
            int r1 = r1 - r2
            int r13 = r1 / 2
            r36.save()
            android.graphics.Matrix r1 = r0.mMatrix
            int r2 = r0.mOrientation
            float r2 = (float) r2
            r1.postRotate(r2)
            int r1 = r0.mOrientation
            int r1 = -r1
            float r1 = (float) r1
            r7.rotate(r1)
            r14 = 0
            r15 = r14
        L_0x005a:
            android.hardware.Camera$Face[] r1 = r0.mFaces
            int r2 = r1.length
            if (r15 >= r2) goto L_0x05c1
            r2 = r1[r15]
            int r2 = r2.score
            r3 = 50
            if (r2 >= r3) goto L_0x0070
        L_0x0067:
            r30 = r12
            r32 = r13
            r13 = r8
            r12 = r10
            r8 = r14
            goto L_0x05b4
        L_0x0070:
            android.graphics.RectF r2 = r0.mRect
            r1 = r1[r15]
            android.graphics.Rect r1 = r1.rect
            r2.set(r1)
            boolean r1 = r0.LOGV
            if (r1 == 0) goto L_0x0084
            android.graphics.RectF r1 = r0.mRect
            java.lang.String r2 = "Original rect"
            com.android.camera.util.CameraUtil.dumpRect(r1, r2)
        L_0x0084:
            android.graphics.Matrix r1 = r0.mMatrix
            android.graphics.RectF r2 = r0.mRect
            r1.mapRect(r2)
            boolean r1 = r0.LOGV
            if (r1 == 0) goto L_0x0096
            android.graphics.RectF r1 = r0.mRect
            java.lang.String r2 = "Transformed rect"
            com.android.camera.util.CameraUtil.dumpRect(r1, r2)
        L_0x0096:
            android.graphics.Paint r1 = r0.mPaint
            int r2 = r0.mColor
            r1.setColor(r2)
            android.graphics.RectF r1 = r0.mRect
            float r6 = (float) r12
            float r5 = (float) r13
            r1.offset(r6, r5)
            android.graphics.RectF r1 = r0.mRect
            android.graphics.Paint r2 = r0.mPaint
            r7.drawOval(r1, r2)
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r1 = r1[r15]
            boolean r1 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.isExtendedFaceInstance(r1)
            if (r1 == 0) goto L_0x0067
            r1 = 4
            float[] r4 = new float[r1]
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r1 = r1[r15]
            android.graphics.Rect r1 = r1.rect
            int r1 = r1.width()
            int r16 = r1 / 12
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r1 = r1[r15]
            android.graphics.Rect r1 = r1.rect
            int r1 = r1.height()
            int r17 = r1 / 12
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "blink: ("
            r1.append(r2)
            android.hardware.Camera$Face[] r2 = r0.mFaces
            r2 = r2[r15]
            int r2 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getLeftEyeBlinkDegree(r2)
            r1.append(r2)
            java.lang.String r2 = ", "
            r1.append(r2)
            android.hardware.Camera$Face[] r2 = r0.mFaces
            r2 = r2[r15]
            int r2 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getRightEyeBlinkDegree(r2)
            r1.append(r2)
            java.lang.String r2 = ")"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r3 = "CAM_FaceView"
            android.util.Log.e(r3, r1)
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            r8 = 60
            r18 = 3
            r19 = 1
            if (r2 == 0) goto L_0x01a7
            int r2 = r0.mDisplayRotation
            if (r2 == 0) goto L_0x0143
            if (r2 != r9) goto L_0x0118
            goto L_0x0143
        L_0x0118:
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            int r2 = r2.x
            int r20 = r16 / 2
            int r2 = r2 - r20
            float r2 = (float) r2
            r4[r14] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            int r2 = r2.y
            float r2 = (float) r2
            r4[r19] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            int r2 = r2.x
            int r2 = r2 + r20
            float r2 = (float) r2
            r4[r11] = r2
            r1 = r1[r15]
            android.graphics.Point r1 = r1.leftEye
            int r1 = r1.y
            float r1 = (float) r1
            r4[r18] = r1
            goto L_0x016f
        L_0x0143:
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            int r2 = r2.x
            float r2 = (float) r2
            r4[r14] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            int r2 = r2.y
            int r20 = r17 / 2
            int r2 = r2 - r20
            float r2 = (float) r2
            r4[r19] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            int r2 = r2.x
            float r2 = (float) r2
            r4[r11] = r2
            r1 = r1[r15]
            android.graphics.Point r1 = r1.leftEye
            int r1 = r1.y
            int r1 = r1 + r20
            float r1 = (float) r1
            r4[r18] = r1
        L_0x016f:
            android.graphics.Matrix r1 = r0.mMatrix
            r1.mapPoints(r4)
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r1 = r1[r15]
            int r1 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getLeftEyeBlinkDegree(r1)
            if (r1 < r8) goto L_0x01a7
            r1 = r4[r14]
            float r2 = r1 + r6
            r1 = r4[r19]
            float r20 = r1 + r5
            r1 = r4[r11]
            float r21 = r1 + r6
            r1 = r4[r18]
            float r22 = r1 + r5
            android.graphics.Paint r1 = r0.mPaint
            r23 = r1
            r1 = r36
            r24 = r3
            r3 = r20
            r10 = r4
            r4 = r21
            r25 = r5
            r5 = r22
            r26 = r6
            r6 = r23
            r1.drawLine(r2, r3, r4, r5, r6)
            goto L_0x01ae
        L_0x01a7:
            r24 = r3
            r10 = r4
            r25 = r5
            r26 = r6
        L_0x01ae:
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r2 = r1[r15]
            android.graphics.Point r2 = r2.rightEye
            if (r2 == 0) goto L_0x0244
            int r2 = r0.mDisplayRotation
            if (r2 == 0) goto L_0x01e6
            if (r2 != r9) goto L_0x01bd
            goto L_0x01e6
        L_0x01bd:
            r2 = r1[r15]
            android.graphics.Point r2 = r2.rightEye
            int r2 = r2.x
            int r3 = r16 / 2
            int r2 = r2 - r3
            float r2 = (float) r2
            r10[r14] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.rightEye
            int r2 = r2.y
            float r2 = (float) r2
            r10[r19] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.rightEye
            int r2 = r2.x
            int r2 = r2 + r3
            float r2 = (float) r2
            r10[r11] = r2
            r1 = r1[r15]
            android.graphics.Point r1 = r1.rightEye
            int r1 = r1.y
            float r1 = (float) r1
            r10[r18] = r1
            goto L_0x0210
        L_0x01e6:
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r2 = r1[r15]
            android.graphics.Point r2 = r2.rightEye
            int r2 = r2.x
            float r2 = (float) r2
            r10[r14] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.rightEye
            int r2 = r2.y
            int r3 = r17 / 2
            int r2 = r2 - r3
            float r2 = (float) r2
            r10[r19] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.rightEye
            int r2 = r2.x
            float r2 = (float) r2
            r10[r11] = r2
            r1 = r1[r15]
            android.graphics.Point r1 = r1.rightEye
            int r1 = r1.y
            int r1 = r1 + r3
            float r1 = (float) r1
            r10[r18] = r1
        L_0x0210:
            android.graphics.Matrix r1 = r0.mMatrix
            r1.mapPoints(r10)
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r1 = r1[r15]
            int r1 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getRightEyeBlinkDegree(r1)
            if (r1 < r8) goto L_0x0244
            r1 = r10[r14]
            r6 = r26
            float r2 = r1 + r6
            r1 = r10[r19]
            r5 = r25
            float r3 = r1 + r5
            r1 = r10[r11]
            float r4 = r1 + r6
            r1 = r10[r18]
            float r21 = r1 + r5
            android.graphics.Paint r1 = r0.mPaint
            r22 = r1
            r1 = r36
            r9 = r5
            r5 = r21
            r27 = r6
            r6 = r22
            r1.drawLine(r2, r3, r4, r5, r6)
            goto L_0x0248
        L_0x0244:
            r9 = r25
            r27 = r26
        L_0x0248:
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r1 = r1[r15]
            int r1 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getLeftRightGazeDegree(r1)
            if (r1 != 0) goto L_0x026b
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r1 = r1[r15]
            int r1 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getTopBottomGazeDegree(r1)
            if (r1 == 0) goto L_0x025d
            goto L_0x026b
        L_0x025d:
            r31 = r9
            r30 = r12
            r32 = r13
            r9 = r27
        L_0x0265:
            r12 = 90
            r13 = 270(0x10e, float:3.78E-43)
            goto L_0x0465
        L_0x026b:
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            int r2 = r2.x
            r3 = r1[r15]
            android.graphics.Point r3 = r3.rightEye
            int r3 = r3.x
            int r2 = r2 - r3
            r3 = r1[r15]
            android.graphics.Point r3 = r3.leftEye
            int r3 = r3.x
            r4 = r1[r15]
            android.graphics.Point r4 = r4.rightEye
            int r4 = r4.x
            int r3 = r3 - r4
            int r2 = r2 * r3
            r3 = r1[r15]
            android.graphics.Point r3 = r3.leftEye
            int r3 = r3.y
            r4 = r1[r15]
            android.graphics.Point r4 = r4.rightEye
            int r4 = r4.y
            int r3 = r3 - r4
            r4 = r1[r15]
            android.graphics.Point r4 = r4.leftEye
            int r4 = r4.y
            r1 = r1[r15]
            android.graphics.Point r1 = r1.rightEye
            int r1 = r1.y
            int r4 = r4 - r1
            int r3 = r3 * r4
            int r2 = r2 + r3
            double r1 = (double) r2
            double r1 = java.lang.Math.sqrt(r1)
            r3 = 4611686018427387904(0x4000000000000000, double:2.0)
            double r1 = r1 / r3
            android.hardware.Camera$Face[] r3 = r0.mFaces
            r3 = r3[r15]
            int r3 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getLeftRightGazeDegree(r3)
            int r3 = -r3
            double r3 = (double) r3
            android.hardware.Camera$Face[] r5 = r0.mFaces
            r5 = r5[r15]
            int r5 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getTopBottomGazeDegree(r5)
            int r5 = -r5
            double r5 = (double) r5
            r21 = 4640537203540230144(0x4066800000000000, double:180.0)
            double r25 = r3 / r21
            r28 = 4614256656552045848(0x400921fb54442d18, double:3.141592653589793)
            double r25 = r25 * r28
            r30 = r12
            double r11 = java.lang.Math.sin(r25)
            double r11 = -r11
            android.hardware.Camera$Face[] r14 = r0.mFaces
            r14 = r14[r15]
            int r14 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getRollDirection(r14)
            int r14 = -r14
            r31 = r9
            double r8 = (double) r14
            double r8 = r8 / r21
            double r8 = r8 * r28
            double r8 = java.lang.Math.cos(r8)
            double r11 = r11 * r8
            double r5 = r5 / r21
            double r5 = r5 * r28
            double r8 = java.lang.Math.sin(r5)
            double r32 = java.lang.Math.cos(r25)
            double r8 = r8 * r32
            android.hardware.Camera$Face[] r14 = r0.mFaces
            r14 = r14[r15]
            int r14 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getRollDirection(r14)
            int r14 = -r14
            r32 = r13
            double r13 = (double) r14
            double r13 = r13 / r21
            double r13 = r13 * r28
            double r13 = java.lang.Math.sin(r13)
            double r8 = r8 * r13
            double r11 = r11 + r8
            double r1 = -r1
            double r11 = r11 * r1
            r8 = 4602678819172646912(0x3fe0000000000000, double:0.5)
            double r11 = r11 + r8
            float r11 = (float) r11
            double r3 = -r3
            double r3 = r3 / r21
            double r3 = r3 * r28
            double r3 = java.lang.Math.sin(r3)
            android.hardware.Camera$Face[] r12 = r0.mFaces
            r12 = r12[r15]
            int r12 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getRollDirection(r12)
            int r12 = -r12
            double r12 = (double) r12
            double r12 = r12 / r21
            double r12 = r12 * r28
            double r12 = java.lang.Math.sin(r12)
            double r3 = r3 * r12
            double r5 = java.lang.Math.sin(r5)
            double r12 = java.lang.Math.cos(r25)
            double r5 = r5 * r12
            android.hardware.Camera$Face[] r12 = r0.mFaces
            r12 = r12[r15]
            int r12 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getRollDirection(r12)
            int r12 = -r12
            double r12 = (double) r12
            double r12 = r12 / r21
            double r12 = r12 * r28
            double r12 = java.lang.Math.cos(r12)
            double r5 = r5 * r12
            double r3 = r3 - r5
            double r3 = r3 * r1
            double r3 = r3 + r8
            float r8 = (float) r3
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r1 = r1[r15]
            int r1 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getLeftEyeBlinkDegree(r1)
            r2 = 60
            if (r1 >= r2) goto L_0x03db
            int r1 = r0.mDisplayRotation
            r2 = 90
            if (r1 == r2) goto L_0x0392
            r2 = 270(0x10e, float:3.78E-43)
            if (r1 != r2) goto L_0x0367
            goto L_0x0392
        L_0x0367:
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            int r2 = r2.x
            float r2 = (float) r2
            r3 = 0
            r10[r3] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            int r2 = r2.y
            float r2 = (float) r2
            r10[r19] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            int r2 = r2.x
            float r2 = (float) r2
            float r2 = r2 + r8
            r3 = 2
            r10[r3] = r2
            r1 = r1[r15]
            android.graphics.Point r1 = r1.leftEye
            int r1 = r1.y
            float r1 = (float) r1
            float r1 = r1 + r11
            r10[r18] = r1
            goto L_0x03bc
        L_0x0392:
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            int r2 = r2.x
            float r2 = (float) r2
            r3 = 0
            r10[r3] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            int r2 = r2.y
            float r2 = (float) r2
            r10[r19] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.leftEye
            int r2 = r2.x
            float r2 = (float) r2
            float r2 = r2 + r11
            r3 = 2
            r10[r3] = r2
            r1 = r1[r15]
            android.graphics.Point r1 = r1.leftEye
            int r1 = r1.y
            float r1 = (float) r1
            float r1 = r1 + r8
            r10[r18] = r1
        L_0x03bc:
            android.graphics.Matrix r1 = r0.mMatrix
            r1.mapPoints(r10)
            r1 = 0
            r2 = r10[r1]
            r9 = r27
            float r2 = r2 + r9
            r1 = r10[r19]
            float r3 = r1 + r31
            r1 = 2
            r4 = r10[r1]
            float r4 = r4 + r9
            r1 = r10[r18]
            float r5 = r1 + r31
            android.graphics.Paint r6 = r0.mPaint
            r1 = r36
            r1.drawLine(r2, r3, r4, r5, r6)
            goto L_0x03dd
        L_0x03db:
            r9 = r27
        L_0x03dd:
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r1 = r1[r15]
            int r1 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getRightEyeBlinkDegree(r1)
            r2 = 60
            if (r1 >= r2) goto L_0x0265
            int r1 = r0.mDisplayRotation
            r12 = 90
            r13 = 270(0x10e, float:3.78E-43)
            if (r1 == r12) goto L_0x041f
            if (r1 != r13) goto L_0x03f4
            goto L_0x041f
        L_0x03f4:
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r2 = r1[r15]
            android.graphics.Point r2 = r2.rightEye
            int r2 = r2.x
            float r2 = (float) r2
            r3 = 0
            r10[r3] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.rightEye
            int r2 = r2.y
            float r2 = (float) r2
            r10[r19] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.rightEye
            int r2 = r2.x
            float r2 = (float) r2
            float r2 = r2 + r8
            r3 = 2
            r10[r3] = r2
            r1 = r1[r15]
            android.graphics.Point r1 = r1.rightEye
            int r1 = r1.y
            float r1 = (float) r1
            float r1 = r1 + r11
            r10[r18] = r1
            goto L_0x0449
        L_0x041f:
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r2 = r1[r15]
            android.graphics.Point r2 = r2.rightEye
            int r2 = r2.x
            float r2 = (float) r2
            r3 = 0
            r10[r3] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.rightEye
            int r2 = r2.y
            float r2 = (float) r2
            r10[r19] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.rightEye
            int r2 = r2.x
            float r2 = (float) r2
            float r2 = r2 + r11
            r3 = 2
            r10[r3] = r2
            r1 = r1[r15]
            android.graphics.Point r1 = r1.rightEye
            int r1 = r1.y
            float r1 = (float) r1
            float r1 = r1 + r8
            r10[r18] = r1
        L_0x0449:
            android.graphics.Matrix r1 = r0.mMatrix
            r1.mapPoints(r10)
            r1 = 0
            r2 = r10[r1]
            float r2 = r2 + r9
            r1 = r10[r19]
            float r3 = r1 + r31
            r1 = 2
            r4 = r10[r1]
            float r4 = r4 + r9
            r1 = r10[r18]
            float r5 = r1 + r31
            android.graphics.Paint r6 = r0.mPaint
            r1 = r36
            r1.drawLine(r2, r3, r4, r5, r6)
        L_0x0465:
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r1 = r1[r15]
            android.graphics.Point r1 = r1.mouth
            if (r1 == 0) goto L_0x05b2
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "smile: "
            r1.append(r2)
            android.hardware.Camera$Face[] r2 = r0.mFaces
            r2 = r2[r15]
            int r2 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getSmileDegree(r2)
            r1.append(r2)
            java.lang.String r2 = ","
            r1.append(r2)
            android.hardware.Camera$Face[] r2 = r0.mFaces
            r2 = r2[r15]
            int r2 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getSmileScore(r2)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r2 = r24
            android.util.Log.e(r2, r1)
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r1 = r1[r15]
            int r1 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getSmileDegree(r1)
            r2 = 30
            if (r1 >= r2) goto L_0x0518
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r2 = r1[r15]
            android.graphics.Point r2 = r2.mouth
            int r2 = r2.x
            int r2 = r2 + r30
            int r2 = r2 - r16
            float r2 = (float) r2
            r3 = 0
            r10[r3] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.mouth
            int r2 = r2.y
            float r2 = (float) r2
            r10[r19] = r2
            r2 = r1[r15]
            android.graphics.Point r2 = r2.mouth
            int r2 = r2.x
            int r2 = r2 + r30
            int r2 = r2 + r16
            float r2 = (float) r2
            r3 = 2
            r10[r3] = r2
            r1 = r1[r15]
            android.graphics.Point r1 = r1.mouth
            int r1 = r1.y
            float r1 = (float) r1
            r10[r18] = r1
            android.graphics.Matrix r1 = new android.graphics.Matrix
            android.graphics.Matrix r2 = r0.mMatrix
            r1.<init>(r2)
            android.hardware.Camera$Face[] r2 = r0.mFaces
            r2 = r2[r15]
            int r2 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getRollDirection(r2)
            float r2 = (float) r2
            android.hardware.Camera$Face[] r3 = r0.mFaces
            r4 = r3[r15]
            android.graphics.Point r4 = r4.mouth
            int r4 = r4.x
            float r4 = (float) r4
            r3 = r3[r15]
            android.graphics.Point r3 = r3.mouth
            int r3 = r3.y
            float r3 = (float) r3
            r1.preRotate(r2, r4, r3)
            r1.mapPoints(r10)
            r8 = 0
            r1 = r10[r8]
            float r2 = r1 + r9
            r1 = r10[r19]
            float r3 = r1 + r31
            r11 = 2
            r1 = r10[r11]
            float r4 = r1 + r9
            r1 = r10[r18]
            float r5 = r1 + r31
            android.graphics.Paint r6 = r0.mPaint
            r1 = r36
            r1.drawLine(r2, r3, r4, r5, r6)
            goto L_0x05b4
        L_0x0518:
            r8 = 0
            r11 = 2
            android.hardware.Camera$Face[] r1 = r0.mFaces
            r1 = r1[r15]
            int r1 = org.codeaurora.snapcam.wrapper.ExtendedFaceWrapper.getSmileDegree(r1)
            r2 = 60
            if (r1 >= r2) goto L_0x0571
            int r1 = r0.mDisplayRotation
            int r1 = 360 - r1
            android.graphics.RectF r2 = r0.mRect
            android.hardware.Camera$Face[] r3 = r0.mFaces
            r4 = r3[r15]
            android.graphics.Point r4 = r4.mouth
            int r4 = r4.x
            int r4 = r4 - r16
            float r4 = (float) r4
            r5 = r3[r15]
            android.graphics.Point r5 = r5.mouth
            int r5 = r5.y
            int r5 = r5 - r17
            float r5 = (float) r5
            r6 = r3[r15]
            android.graphics.Point r6 = r6.mouth
            int r6 = r6.x
            int r6 = r6 + r16
            float r6 = (float) r6
            r3 = r3[r15]
            android.graphics.Point r3 = r3.mouth
            int r3 = r3.y
            int r3 = r3 + r17
            float r3 = (float) r3
            r2.set(r4, r5, r6, r3)
            android.graphics.Matrix r2 = r0.mMatrix
            android.graphics.RectF r3 = r0.mRect
            r2.mapRect(r3)
            android.graphics.RectF r2 = r0.mRect
            r3 = r31
            r2.offset(r9, r3)
            android.graphics.RectF r2 = r0.mRect
            float r3 = (float) r1
            r4 = 1127481344(0x43340000, float:180.0)
            r5 = 1
            android.graphics.Paint r6 = r0.mPaint
            r1 = r36
            r1.drawArc(r2, r3, r4, r5, r6)
            goto L_0x05b4
        L_0x0571:
            r3 = r31
            android.graphics.RectF r1 = r0.mRect
            android.hardware.Camera$Face[] r2 = r0.mFaces
            r4 = r2[r15]
            android.graphics.Point r4 = r4.mouth
            int r4 = r4.x
            int r4 = r4 - r16
            float r4 = (float) r4
            r5 = r2[r15]
            android.graphics.Point r5 = r5.mouth
            int r5 = r5.y
            int r5 = r5 - r17
            float r5 = (float) r5
            r6 = r2[r15]
            android.graphics.Point r6 = r6.mouth
            int r6 = r6.x
            int r6 = r6 + r16
            float r6 = (float) r6
            r2 = r2[r15]
            android.graphics.Point r2 = r2.mouth
            int r2 = r2.y
            int r2 = r2 + r17
            float r2 = (float) r2
            r1.set(r4, r5, r6, r2)
            android.graphics.Matrix r1 = r0.mMatrix
            android.graphics.RectF r2 = r0.mRect
            r1.mapRect(r2)
            android.graphics.RectF r1 = r0.mRect
            r1.offset(r9, r3)
            android.graphics.RectF r1 = r0.mRect
            android.graphics.Paint r2 = r0.mPaint
            r7.drawOval(r1, r2)
            goto L_0x05b4
        L_0x05b2:
            r8 = 0
            r11 = 2
        L_0x05b4:
            int r15 = r15 + 1
            r14 = r8
            r10 = r12
            r8 = r13
            r12 = r30
            r13 = r32
            r9 = 180(0xb4, float:2.52E-43)
            goto L_0x005a
        L_0x05c1:
            r36.restore()
        L_0x05c4:
            super.onDraw(r36)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.p004ui.FaceView.onDraw(android.graphics.Canvas):void");
    }
}
