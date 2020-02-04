package com.android.camera.imageprocessor.filter;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.os.Handler;
import android.util.Log;
import com.android.camera.CaptureModule;
import com.android.camera.imageprocessor.filter.ImageFilter.ResultImage;
import com.android.camera.p004ui.TrackingFocusRenderer;
import java.nio.ByteBuffer;
import java.util.List;

public class TrackingFocusFrameListener implements ImageFilter {
    public static final int MAX_NUM_TRACKED_OBJECTS = 3;
    public static final long PENDING_REGISTRATION = -1;
    private static String TAG = "TrackingFocusFrameListener";
    private static boolean mIsSupported = false;
    private Rect imageRect;
    int mHeight;
    private int[] mInputCords = null;
    private boolean mIsFirstTime = true;
    private boolean mIsInitialzed = false;
    private CaptureModule mModule;
    int mStrideVU;
    int mStrideY;
    private long mTrackedId = -1;
    private TrackingFocusRenderer mTrackingFocusRender;
    int mWidth;
    byte[] yvuBytes = null;

    public enum OperationMode {
        DEFAULT,
        PERFORMANCE,
        CPU_OFFLOAD,
        LOW_POWER
    }

    public enum Precision {
        HIGH,
        LOW
    }

    public static class Result {
        public final int confidence;

        /* renamed from: id */
        public final int f83id;
        public Rect pos;

        private Result(int i, int i2, int i3, int i4, int i5, int i6) {
            this.f83id = i;
            this.confidence = i2;
            this.pos = new Rect(i3, i4, i5, i6);
        }

        public static Result Copy(Result result) {
            int i = result.f83id;
            int i2 = result.confidence;
            Rect rect = result.pos;
            Result result2 = new Result(i, i2, rect.left, rect.top, rect.right, rect.bottom);
            return result2;
        }
    }

    private native int nGetMaxRoiDimension();

    private native int nGetMinRoiDimension();

    private native int nInit(int i, int i2, int i3, int i4, int i5);

    private native long nRegisterObjectByPoint(byte[] bArr, int i, int i2, boolean z);

    private native long nRegisterObjectByRect(byte[] bArr, int i, int i2, int i3, int i4);

    private native void nRelease();

    private native int[] nTrackObjects(byte[] bArr);

    private native void nUnregisterObject(long j);

    public int getNumRequiredImage() {
        return 1;
    }

    public String getStringName() {
        return "TrackingFocusFrameListener";
    }

    public boolean isFrameListener() {
        return true;
    }

    public boolean isManualMode() {
        return false;
    }

    public void manualCapture(Builder builder, CameraCaptureSession cameraCaptureSession, CaptureCallback captureCallback, Handler handler) {
    }

    public ResultImage processImage() {
        return null;
    }

    public List<CaptureRequest> setRequiredImages(Builder builder) {
        return null;
    }

    public TrackingFocusFrameListener(CaptureModule captureModule) {
        this.mModule = captureModule;
    }

    public void init(int i, int i2, int i3, int i4) {
        this.mWidth = i;
        this.mHeight = i2;
        this.mStrideY = i3;
        this.mStrideVU = i4;
        if (!this.mIsInitialzed) {
            if (nInit(OperationMode.PERFORMANCE.ordinal(), Precision.HIGH.ordinal(), this.mWidth, this.mHeight, this.mStrideY) < 0) {
                Log.e(TAG, "Initialization failed.");
            }
            this.imageRect = new Rect(0, 0, i, i2);
            this.mTrackingFocusRender = this.mModule.getTrackingForcusRenderer();
            this.yvuBytes = new byte[(((this.mStrideY * this.mHeight) * 3) / 2)];
            this.mIsInitialzed = true;
        }
    }

    public void deinit() {
        if (this.mIsInitialzed) {
            nRelease();
            this.mIsInitialzed = false;
        }
    }

    public void addImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, Object obj) {
        byteBuffer.get(this.yvuBytes, 0, byteBuffer.remaining());
        byteBuffer2.get(this.yvuBytes, this.mStrideY * this.mHeight, byteBuffer2.remaining());
        int[] inputCords = this.mTrackingFocusRender.getInputCords(this.mWidth, this.mHeight);
        if (inputCords != null) {
            long j = this.mTrackedId;
            if (j != -1) {
                unregisterObject(j);
                this.mTrackedId = -1;
            }
            this.mIsFirstTime = true;
            this.mInputCords = inputCords;
        }
        int[] iArr = this.mInputCords;
        if (iArr != null) {
            if (this.mTrackedId == -1) {
                try {
                    this.mTrackedId = registerObject(this.yvuBytes, new Point(iArr[0], iArr[1]), this.mIsFirstTime);
                    this.mIsFirstTime = false;
                } catch (IllegalArgumentException e) {
                    this.mTrackedId = -1;
                    Log.e(TAG, e.toString());
                }
            }
            if (this.mTrackedId != -1) {
                this.mTrackingFocusRender.putRegisteredCords(trackObjects(this.yvuBytes), this.mWidth, this.mHeight);
            }
        }
    }

    public int getMinRoiDimension() {
        if (this.mIsInitialzed) {
            return nGetMinRoiDimension();
        }
        throw new IllegalArgumentException("already released");
    }

    public int getMaxRoiDimension() {
        if (this.mIsInitialzed) {
            return nGetMaxRoiDimension();
        }
        throw new IllegalArgumentException("already released");
    }

    public long registerObject(byte[] bArr, Rect rect) {
        if (bArr == null || bArr.length < getMinFrameSize()) {
            throw new IllegalArgumentException("imageDataNV21 null or too small to encode frame");
        } else if (rect == null || rect.isEmpty() || !this.imageRect.contains(rect)) {
            throw new IllegalArgumentException("rect must be non-empty and be entirely inside the frame");
        } else if (this.mIsInitialzed) {
            long nRegisterObjectByRect = nRegisterObjectByRect(bArr, rect.left, rect.top, rect.right, rect.bottom);
            if (nRegisterObjectByRect == 0) {
                nRegisterObjectByRect = -1;
            }
            this.mTrackedId = nRegisterObjectByRect;
            return this.mTrackedId;
        } else {
            throw new IllegalArgumentException("already released");
        }
    }

    public long registerObject(byte[] bArr, Point point, boolean z) {
        if (bArr == null || bArr.length < getMinFrameSize()) {
            StringBuilder sb = new StringBuilder();
            sb.append("imageDataNV21 null or too small to encode frame");
            sb.append(bArr.length);
            sb.append(" ");
            sb.append(getMinFrameSize());
            throw new IllegalArgumentException(sb.toString());
        } else if (point == null || !this.imageRect.contains(point.x, point.y)) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("point is outside the image frame: ");
            sb2.append(this.imageRect.toString());
            throw new IllegalArgumentException(sb2.toString());
        } else if (this.mIsInitialzed) {
            long nRegisterObjectByPoint = nRegisterObjectByPoint(bArr, point.x, point.y, z);
            if (nRegisterObjectByPoint == 0) {
                nRegisterObjectByPoint = -1;
            }
            this.mTrackedId = nRegisterObjectByPoint;
            return this.mTrackedId;
        } else {
            throw new IllegalArgumentException("already released");
        }
    }

    public void unregisterObject(long j) {
        if (j == -1) {
            Log.e(TAG, "There's a pending object");
        } else if (!this.mIsInitialzed) {
            Log.e(TAG, "already released");
        }
        nUnregisterObject(j);
    }

    public Result trackObjects(byte[] bArr) {
        if (bArr == null || bArr.length < getMinFrameSize()) {
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("imageDataNV21 null or too small to encode frame ");
            sb.append(bArr.length);
            sb.append(" ");
            sb.append(getMinFrameSize());
            Log.e(str, sb.toString());
        } else if (!this.mIsInitialzed) {
            Log.e(TAG, "It's released");
        }
        int[] nTrackObjects = nTrackObjects(bArr);
        Result result = new Result(nTrackObjects[0], nTrackObjects[1], nTrackObjects[2], nTrackObjects[3], nTrackObjects[4], nTrackObjects[5]);
        return result;
    }

    private int getMinFrameSize() {
        return ((this.mStrideY * this.imageRect.bottom) * 3) / 2;
    }

    public boolean isSupported() {
        return mIsSupported;
    }

    public static boolean isSupportedStatic() {
        return mIsSupported;
    }

    static {
        try {
            System.loadLibrary("jni_trackingfocus");
            mIsSupported = true;
        } catch (UnsatisfiedLinkError e) {
            Log.d(TAG, e.toString());
            mIsSupported = false;
        }
    }
}
