package com.android.camera.imageprocessor.filter;

import android.graphics.Rect;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.os.Handler;
import android.util.Log;
import com.android.camera.CaptureModule;
import com.android.camera.imageprocessor.filter.ImageFilter.ResultImage;
import com.android.camera.util.PersistUtil;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class StillmoreFilter implements ImageFilter {
    public static final int NUM_REQUIRED_IMAGE = PersistUtil.getStillmoreNumRequiredImages();
    private static String TAG = "StillmoreFilter";
    private static boolean mIsSupported;
    private long mExpoTime;
    private int mHeight;
    private CaptureModule mModule;
    private ByteBuffer mOutBuf;
    private int mSenseValue = 0;
    private int mStrideVU;
    private int mStrideY;
    private int mWidth;

    private native int nativeAddImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3);

    private native int nativeConfigureStillMore(float f, float f2, float f3);

    private native int nativeDeinit();

    private native int nativeInit(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9);

    private native int nativeProcessImage(byte[] bArr, int i, int i2, int[] iArr);

    public String getStringName() {
        return "StillmoreFilter";
    }

    public boolean isFrameListener() {
        return false;
    }

    public boolean isManualMode() {
        return false;
    }

    public void manualCapture(Builder builder, CameraCaptureSession cameraCaptureSession, CaptureCallback captureCallback, Handler handler) {
    }

    static {
        mIsSupported = false;
        try {
            System.loadLibrary("jni_stillmore");
            mIsSupported = true;
        } catch (UnsatisfiedLinkError e) {
            Log.d(TAG, e.toString());
            mIsSupported = false;
        }
    }

    private static void Log(String str) {
        if (ImageFilter.DEBUG) {
            Log.d(TAG, str);
        }
    }

    public StillmoreFilter(CaptureModule captureModule) {
        this.mModule = captureModule;
    }

    public List<CaptureRequest> setRequiredImages(Builder builder) {
        this.mExpoTime = ((Long) this.mModule.getPreviewCaptureResult().get(CaptureResult.SENSOR_EXPOSURE_TIME)).longValue();
        this.mSenseValue = ((Integer) this.mModule.getPreviewCaptureResult().get(CaptureResult.SENSOR_SENSITIVITY)).intValue();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < NUM_REQUIRED_IMAGE; i++) {
            arrayList.add(builder.build());
        }
        return arrayList;
    }

    public int getNumRequiredImage() {
        return NUM_REQUIRED_IMAGE;
    }

    public void init(int i, int i2, int i3, int i4) {
        Log("init");
        this.mWidth = (i / 2) * 2;
        this.mHeight = (i2 / 2) * 2;
        this.mStrideY = (i3 / 2) * 2;
        this.mStrideVU = (i4 / 2) * 2;
        this.mOutBuf = ByteBuffer.allocate(((this.mStrideY * this.mHeight) * 3) / 2);
        StringBuilder sb = new StringBuilder();
        sb.append("width: ");
        sb.append(this.mWidth);
        sb.append(" height: ");
        sb.append(this.mHeight);
        sb.append(" strideY: ");
        sb.append(this.mStrideY);
        sb.append(" strideVU: ");
        sb.append(this.mStrideVU);
        Log(sb.toString());
        int i5 = this.mWidth;
        int i6 = this.mHeight;
        nativeInit(i5, i6, this.mStrideY, this.mStrideVU, 0, 0, i5, i6, NUM_REQUIRED_IMAGE);
        float stillmoreBrColor = PersistUtil.getStillmoreBrColor();
        float stillmoreBrIntensity = PersistUtil.getStillmoreBrIntensity();
        float stillmoreSmoothingIntensity = PersistUtil.getStillmoreSmoothingIntensity();
        nativeConfigureStillMore(stillmoreBrColor, stillmoreBrIntensity, stillmoreSmoothingIntensity);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("ConfigureStillmore brColor: ");
        sb2.append(stillmoreBrColor);
        sb2.append(" brIntensity: ");
        sb2.append(stillmoreBrIntensity);
        sb2.append(" smoothingintensity: ");
        sb2.append(stillmoreSmoothingIntensity);
        sb2.append(" NUM_REQUIRED_IMAGE: ");
        sb2.append(NUM_REQUIRED_IMAGE);
        Log(sb2.toString());
    }

    public void deinit() {
        Log("deinit");
        this.mOutBuf = null;
        nativeDeinit();
    }

    public void addImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, Object obj) {
        Log("addImage");
        if (nativeAddImage(byteBuffer, byteBuffer2, byteBuffer.remaining(), byteBuffer2.remaining(), i) != 0) {
            Log.e(TAG, "Fail to add image");
        }
    }

    public ResultImage processImage() {
        Log("processImage ");
        int[] iArr = new int[4];
        int nativeProcessImage = nativeProcessImage(this.mOutBuf.array(), (int) (this.mExpoTime / 1000000), this.mSenseValue, iArr);
        Log("processImage done");
        if (nativeProcessImage < 0) {
            Log.w(TAG, "Fail to process the image.");
        }
        ResultImage resultImage = new ResultImage(this.mOutBuf, new Rect(iArr[0], iArr[1], iArr[0] + iArr[2], iArr[1] + iArr[3]), this.mWidth, this.mHeight, this.mStrideY);
        return resultImage;
    }

    public boolean isSupported() {
        return mIsSupported;
    }

    public static boolean isSupportedStatic() {
        return mIsSupported;
    }
}
