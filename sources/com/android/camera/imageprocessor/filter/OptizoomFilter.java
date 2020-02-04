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

public class OptizoomFilter implements ImageFilter {
    public static final int NUM_REQUIRED_IMAGE = 8;
    private static String TAG = "OptizoomFilter";
    private static boolean mIsSupported = true;
    private int mHeight;
    private CaptureModule mModule;
    private ByteBuffer mOutBuf;
    private int mStrideVU;
    private int mStrideY;
    private int mWidth;
    private int temp;

    private native int nativeAddImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3);

    private native int nativeDeinit();

    private native int nativeInit(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9);

    private native int nativeProcessImage(byte[] bArr, float f, int[] iArr);

    public int getNumRequiredImage() {
        return 8;
    }

    public String getStringName() {
        return "OptizoomFilter";
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

    public OptizoomFilter(CaptureModule captureModule) {
        this.mModule = captureModule;
    }

    public List<CaptureRequest> setRequiredImages(Builder builder) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 8; i++) {
            arrayList.add(builder.build());
        }
        return arrayList;
    }

    public void init(int i, int i2, int i3, int i4) {
        Log("init");
        this.mWidth = (i / 2) * 2;
        this.mHeight = (i2 / 2) * 2;
        this.mStrideY = (i3 / 2) * 2;
        this.mStrideVU = (i4 / 2) * 2;
        this.mOutBuf = ByteBuffer.allocate(this.mStrideY * this.mHeight * 6);
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
        nativeInit(i5, i6, this.mStrideY, this.mStrideVU, 0, 0, i5, i6, 8);
    }

    public void deinit() {
        Log("deinit");
        this.mOutBuf = null;
        nativeDeinit();
    }

    public void addImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, Object obj) {
        Log("addImage");
        nativeAddImage(byteBuffer, byteBuffer2, byteBuffer.remaining(), byteBuffer2.remaining(), i);
    }

    public ResultImage processImage() {
        StringBuilder sb = new StringBuilder();
        sb.append("processImage ");
        sb.append(this.mModule.getZoomValue());
        Log(sb.toString());
        int[] iArr = new int[4];
        int nativeProcessImage = nativeProcessImage(this.mOutBuf.array(), this.mModule.getZoomValue(), iArr);
        Log("processImage done");
        if (nativeProcessImage < 0) {
            Log.w(TAG, "Fail to process the optizoom. It only processes when zoomValue >= 1.5f");
            ResultImage resultImage = new ResultImage(this.mOutBuf, new Rect(iArr[0], iArr[1], iArr[0] + iArr[2], iArr[1] + iArr[3]), this.mWidth, this.mHeight, this.mStrideY);
            return resultImage;
        }
        ResultImage resultImage2 = new ResultImage(this.mOutBuf, new Rect(iArr[0], iArr[1], iArr[0] + iArr[2], iArr[1] + iArr[3]), this.mWidth * 2, this.mHeight * 2, this.mStrideY * 2);
        return resultImage2;
    }

    public boolean isSupported() {
        return mIsSupported;
    }

    public static boolean isSupportedStatic() {
        return mIsSupported;
    }

    static {
        try {
            System.loadLibrary("jni_optizoom");
            mIsSupported = true;
        } catch (UnsatisfiedLinkError unused) {
            mIsSupported = false;
        }
    }
}
