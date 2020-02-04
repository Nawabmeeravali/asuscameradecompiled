package com.android.camera.p004ui;

import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.view.MotionEvent;
import com.android.camera.CameraActivity;
import com.android.camera.CaptureModule;
import com.android.camera.CaptureUI;
import com.android.camera.imageprocessor.filter.TrackingFocusFrameListener.Result;

/* renamed from: com.android.camera.ui.TrackingFocusRenderer */
public class TrackingFocusRenderer extends OverlayRenderer implements FocusIndicator {
    private static final int CIRCLE_THUMB_SIZE = 100;
    private static final boolean DEBUG = false;
    public static final int STATUS_INIT = 0;
    public static final int STATUS_INPUT = 1;
    public static final int STATUS_TRACKED = 3;
    public static final int STATUS_TRACKING = 2;
    private static final String TAG = "TrackingFocusRenderer";
    private CameraActivity mActivity;
    private FocusRequestThread mFocusRequestThread = null;
    private int mInX = -1;
    private int mInY = -1;
    private boolean mIsFlipped = false;
    /* access modifiers changed from: private */
    public Object mLock = new Object();
    /* access modifiers changed from: private */
    public CaptureModule mModule;
    private Rect mRect;
    /* access modifiers changed from: private */
    public Result mResult;
    private int mStatus = 0;
    private Rect mSurfaceDim;
    private Paint mTargetPaint;
    private CaptureUI mUI;

    /* renamed from: com.android.camera.ui.TrackingFocusRenderer$FocusRequestThread */
    private class FocusRequestThread extends Thread {
        private static final int FOCUS_DELAY = 1000;
        private static final int MIN_DIFF_CORDS = 100;
        private static final int MIN_DIFF_SIZE = 100;
        private boolean isRunning;
        private int mNewHeight;
        private int mNewWidth;
        private int mNewX;
        private int mNewY;
        private int mOldHeight;
        private int mOldWidth;
        private int mOldX;
        private int mOldY;

        private FocusRequestThread() {
            this.isRunning = true;
            this.mOldX = -100;
            this.mOldY = -100;
            this.mOldWidth = -100;
            this.mOldHeight = -100;
        }

        public void kill() {
            this.isRunning = false;
        }

        public void run() {
            while (this.isRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException unused) {
                }
                synchronized (TrackingFocusRenderer.this.mLock) {
                    if (!(TrackingFocusRenderer.this.mResult == null || TrackingFocusRenderer.this.mResult.pos == null)) {
                        if (TrackingFocusRenderer.this.mResult.pos.centerX() != 0 || TrackingFocusRenderer.this.mResult.pos.centerY() != 0) {
                            this.mNewX = TrackingFocusRenderer.this.mResult.pos.centerX();
                            this.mNewY = TrackingFocusRenderer.this.mResult.pos.centerY();
                            this.mNewWidth = TrackingFocusRenderer.this.mResult.pos.width();
                            this.mNewHeight = TrackingFocusRenderer.this.mResult.pos.height();
                            if (Math.abs(this.mOldX - this.mNewX) >= 100 || Math.abs(this.mOldY - this.mNewY) >= 100 || Math.abs(this.mOldWidth - this.mNewWidth) >= 100 || Math.abs(this.mOldHeight - this.mNewHeight) >= 100) {
                                try {
                                    TrackingFocusRenderer.this.mModule.onSingleTapUp(null, this.mNewX, this.mNewY);
                                    this.mOldX = this.mNewX;
                                    this.mOldY = this.mNewY;
                                    this.mOldWidth = this.mNewWidth;
                                    this.mOldHeight = this.mNewHeight;
                                } catch (Exception unused2) {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void printErrorLog(String str) {
    }

    public void clear() {
    }

    public boolean handlesTouch() {
        return true;
    }

    public void showFail(boolean z) {
    }

    public void showStart() {
    }

    public void showSuccess(boolean z) {
    }

    public void setVisible(boolean z) {
        super.setVisible(z);
        if (this.mModule.getMainCameraCharacteristics() == null || ((Integer) this.mModule.getMainCameraCharacteristics().get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue() != 270) {
            this.mIsFlipped = false;
        } else {
            this.mIsFlipped = true;
        }
        if (!z) {
            synchronized (this.mLock) {
                this.mStatus = 0;
                this.mResult = null;
                this.mInX = 0;
                this.mInY = 0;
            }
            FocusRequestThread focusRequestThread = this.mFocusRequestThread;
            if (focusRequestThread != null) {
                focusRequestThread.kill();
                this.mFocusRequestThread = null;
                return;
            }
            return;
        }
        this.mFocusRequestThread = new FocusRequestThread();
        this.mFocusRequestThread.start();
    }

    public void setSurfaceDim(int i, int i2, int i3, int i4) {
        this.mSurfaceDim = new Rect(i, i2, i3, i4);
    }

    public TrackingFocusRenderer(CameraActivity cameraActivity, CaptureModule captureModule, CaptureUI captureUI) {
        this.mActivity = cameraActivity;
        this.mModule = captureModule;
        this.mUI = captureUI;
        this.mTargetPaint = new Paint();
        this.mTargetPaint.setStrokeWidth(4.0f);
        this.mTargetPaint.setStyle(Style.STROKE);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mSurfaceDim == null) {
            return true;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0 && actionMasked == 1) {
            synchronized (this.mLock) {
                this.mInX = (int) motionEvent.getX();
                this.mInY = (int) motionEvent.getY();
                if (this.mSurfaceDim.contains(this.mInX, this.mInY)) {
                    this.mStatus = 1;
                    update();
                }
            }
        }
        return true;
    }

    public int[] getInputCords(int i, int i2) {
        synchronized (this.mLock) {
            if (this.mStatus != 1) {
                return null;
            }
            this.mStatus = 2;
            int i3 = (this.mUI.getDisplaySize().y - 1) - this.mInY;
            int height = (int) (((float) (i3 - (this.mUI.getDisplaySize().y - this.mSurfaceDim.bottom))) * (((float) i) / ((float) this.mSurfaceDim.height())));
            int width = (int) (((float) (this.mInX - this.mSurfaceDim.left)) * (((float) i2) / ((float) this.mSurfaceDim.width())));
            if (this.mModule.isBackCamera()) {
                if (!this.mIsFlipped) {
                    height = (i - 1) - height;
                }
                int[] iArr = {height, width};
                return iArr;
            } else if (!this.mIsFlipped) {
                height = (i - 1) - height;
                int[] iArr2 = {height, width};
                return iArr2;
            }
            width = (i2 - 1) - width;
            int[] iArr22 = {height, width};
            return iArr22;
        }
    }

    public void putRegisteredCords(Result result, int i, int i2) {
        synchronized (this.mLock) {
            if (result != null) {
                if (!(result.pos == null || (result.pos.width() == 0 && result.pos.height() == 0))) {
                    result.pos = translateToSurface(result.pos, i, i2);
                    this.mResult = result;
                    this.mStatus = 3;
                }
            }
            this.mStatus = 2;
        }
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                TrackingFocusRenderer.this.update();
            }
        });
    }

    private Rect translateToSurface(Rect rect, int i, int i2) {
        int i3;
        int i4;
        int centerY = (i2 - 1) - rect.centerY();
        int centerX = rect.centerX();
        if (this.mModule.isBackCamera()) {
            if (this.mIsFlipped) {
                centerY = rect.centerY();
                i4 = i - 1;
                i3 = rect.centerX();
            }
            float f = (float) i2;
            int height = (int) (((float) rect.height()) * (((float) this.mSurfaceDim.width()) / f));
            float f2 = (float) i;
            int width = (int) (((float) rect.width()) * (((float) this.mSurfaceDim.height()) / f2));
            Rect rect2 = this.mSurfaceDim;
            int width2 = rect2.left + ((int) (((float) centerY) * (((float) rect2.width()) / f)));
            Rect rect3 = this.mSurfaceDim;
            int height2 = rect3.top + ((int) (((float) centerX) * (((float) rect3.height()) / f2)));
            Rect rect4 = new Rect();
            rect4.left = width2 - (height / 2);
            rect4.top = height2 - (width / 2);
            rect4.right = rect4.left + height;
            rect4.bottom = rect4.top + width;
            return rect4;
        } else if (this.mIsFlipped) {
            i4 = i - 1;
            i3 = rect.centerX();
        } else {
            centerY = rect.centerY();
            float f3 = (float) i2;
            int height3 = (int) (((float) rect.height()) * (((float) this.mSurfaceDim.width()) / f3));
            float f22 = (float) i;
            int width3 = (int) (((float) rect.width()) * (((float) this.mSurfaceDim.height()) / f22));
            Rect rect22 = this.mSurfaceDim;
            int width22 = rect22.left + ((int) (((float) centerY) * (((float) rect22.width()) / f3)));
            Rect rect32 = this.mSurfaceDim;
            int height22 = rect32.top + ((int) (((float) centerX) * (((float) rect32.height()) / f22)));
            Rect rect42 = new Rect();
            rect42.left = width22 - (height3 / 2);
            rect42.top = height22 - (width3 / 2);
            rect42.right = rect42.left + height3;
            rect42.bottom = rect42.top + width3;
            return rect42;
        }
        centerX = i4 - i3;
        float f32 = (float) i2;
        int height32 = (int) (((float) rect.height()) * (((float) this.mSurfaceDim.width()) / f32));
        float f222 = (float) i;
        int width32 = (int) (((float) rect.width()) * (((float) this.mSurfaceDim.height()) / f222));
        Rect rect222 = this.mSurfaceDim;
        int width222 = rect222.left + ((int) (((float) centerY) * (((float) rect222.width()) / f32)));
        Rect rect322 = this.mSurfaceDim;
        int height222 = rect322.top + ((int) (((float) centerX) * (((float) rect322.height()) / f222)));
        Rect rect422 = new Rect();
        rect422.left = width222 - (height32 / 2);
        rect422.top = height222 - (width32 / 2);
        rect422.right = rect422.left + height32;
        rect422.bottom = rect422.top + width32;
        return rect422;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0015, code lost:
        r0 = r3.mStatus;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0017, code lost:
        if (r0 != 3) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001b, code lost:
        if (r3.mRect == null) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001d, code lost:
        r3.mTargetPaint.setColor(-16711936);
        r4.drawRect(r3.mRect, r3.mTargetPaint);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0030, code lost:
        if (r0 != 2) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0034, code lost:
        if (r3.mRect == null) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0036, code lost:
        r3.mTargetPaint.setColor(android.support.p000v4.internal.view.SupportMenu.CATEGORY_MASK);
        r4.drawRect(r3.mRect, r3.mTargetPaint);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0044, code lost:
        if (r0 != 1) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0046, code lost:
        r3.mTargetPaint.setColor(android.support.p000v4.internal.view.SupportMenu.CATEGORY_MASK);
        r4.drawCircle((float) r3.mInX, (float) r3.mInY, 100.0f, r3.mTargetPaint);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0058, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDraw(android.graphics.Canvas r4) {
        /*
            r3 = this;
            java.lang.Object r0 = r3.mLock
            monitor-enter(r0)
            com.android.camera.imageprocessor.filter.TrackingFocusFrameListener$Result r1 = r3.mResult     // Catch:{ all -> 0x0059 }
            if (r1 != 0) goto L_0x0009
            monitor-exit(r0)     // Catch:{ all -> 0x0059 }
            return
        L_0x0009:
            int r1 = r3.mStatus     // Catch:{ all -> 0x0059 }
            r2 = 3
            if (r1 != r2) goto L_0x0014
            com.android.camera.imageprocessor.filter.TrackingFocusFrameListener$Result r1 = r3.mResult     // Catch:{ all -> 0x0059 }
            android.graphics.Rect r1 = r1.pos     // Catch:{ all -> 0x0059 }
            r3.mRect = r1     // Catch:{ all -> 0x0059 }
        L_0x0014:
            monitor-exit(r0)     // Catch:{ all -> 0x0059 }
            int r0 = r3.mStatus
            if (r0 != r2) goto L_0x002d
            android.graphics.Rect r0 = r3.mRect
            if (r0 == 0) goto L_0x0058
            android.graphics.Paint r0 = r3.mTargetPaint
            r1 = -16711936(0xffffffffff00ff00, float:-1.7146522E38)
            r0.setColor(r1)
            android.graphics.Rect r0 = r3.mRect
            android.graphics.Paint r3 = r3.mTargetPaint
            r4.drawRect(r0, r3)
            goto L_0x0058
        L_0x002d:
            r1 = 2
            r2 = -65536(0xffffffffffff0000, float:NaN)
            if (r0 != r1) goto L_0x0043
            android.graphics.Rect r0 = r3.mRect
            if (r0 == 0) goto L_0x0058
            android.graphics.Paint r0 = r3.mTargetPaint
            r0.setColor(r2)
            android.graphics.Rect r0 = r3.mRect
            android.graphics.Paint r3 = r3.mTargetPaint
            r4.drawRect(r0, r3)
            goto L_0x0058
        L_0x0043:
            r1 = 1
            if (r0 != r1) goto L_0x0058
            android.graphics.Paint r0 = r3.mTargetPaint
            r0.setColor(r2)
            int r0 = r3.mInX
            float r0 = (float) r0
            int r1 = r3.mInY
            float r1 = (float) r1
            r2 = 1120403456(0x42c80000, float:100.0)
            android.graphics.Paint r3 = r3.mTargetPaint
            r4.drawCircle(r0, r1, r2, r3)
        L_0x0058:
            return
        L_0x0059:
            r3 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0059 }
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.p004ui.TrackingFocusRenderer.onDraw(android.graphics.Canvas):void");
    }
}
