package com.android.camera.imageprocessor.filter;

import android.graphics.Rect;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.os.Handler;
import android.util.Log;
import com.android.camera.CaptureModule;
import com.android.camera.imageprocessor.filter.ImageFilter.ResultImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BlurbusterFilter implements ImageFilter {
    public static final int NUM_REQUIRED_IMAGE = 5;
    private static String TAG = "BlurbusterFilter";
    private static boolean mIsSupported = false;
    private int mHeight;
    private CaptureModule mModule;
    private ByteBuffer mOutBuf;
    private int mStrideVU;
    private int mStrideY;
    private int mWidth;

    private native int nativeAddImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3);

    private native int nativeDeinit();

    private native int nativeInit(int i, int i2, int i3, int i4, int i5);

    private native int nativeProcessImage(byte[] bArr, int[] iArr);

    public int getNumRequiredImage() {
        return 5;
    }

    public boolean isFrameListener() {
        return false;
    }

    public boolean isManualMode() {
        return false;
    }

    public void manualCapture(Builder builder, CameraCaptureSession cameraCaptureSession, CaptureCallback captureCallback, Handler handler) {
    }

    private static void Log(String str) {
        if (ImageFilter.DEBUG) {
            Log.d(TAG, str);
        }
    }

    public BlurbusterFilter(CaptureModule captureModule) {
        this.mModule = captureModule;
    }

    public List<CaptureRequest> setRequiredImages(Builder builder) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 5; i++) {
            arrayList.add(builder.build());
        }
        return arrayList;
    }

    public String getStringName() {
        return TAG;
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
        nativeInit(this.mWidth, this.mHeight, this.mStrideY, this.mStrideVU, 5);
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
        int nativeProcessImage = nativeProcessImage(this.mOutBuf.array(), iArr);
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

    static {
        try {
            System.loadLibrary("jni_blurbuster");
            mIsSupported = true;
        } catch (UnsatisfiedLinkError e) {
            Log.d(TAG, e.toString());
            mIsSupported = false;
        }
    }
}
