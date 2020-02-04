package com.android.camera.imageprocessor.filter;

import android.graphics.Rect;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.os.Handler;
import android.util.Log;
import android.util.Range;
import com.android.camera.CaptureModule;
import com.android.camera.imageprocessor.filter.ImageFilter.ResultImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class SharpshooterFilter implements ImageFilter {
    public static final int NUM_REQUIRED_IMAGE = 5;
    private static String TAG = "SharpshooterFilter";
    private static boolean mIsSupported = true;
    private long mExpoTime;
    private int mHeight;
    private CaptureModule mModule;
    private ByteBuffer mOutBuf;
    private int mSenseValue = 0;
    private int mStrideVU;
    private int mStrideY;
    private int mWidth;
    private int temp;

    private native int nativeAddImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3);

    private native int nativeDeinit();

    private native int nativeInit(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9);

    private native int nativeProcessImage(byte[] bArr, int i, int i2, int[] iArr);

    public int getNumRequiredImage() {
        return 5;
    }

    public String getStringName() {
        return "SharpshooterFilter";
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

    public SharpshooterFilter(CaptureModule captureModule) {
        this.mModule = captureModule;
    }

    private void getSenseUpperValue() {
        if (this.mSenseValue == 0) {
            this.mSenseValue = ((Integer) ((Range) this.mModule.getMainCameraCharacteristics().get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)).getUpper()).intValue();
        }
    }

    public List<CaptureRequest> setRequiredImages(Builder builder) {
        getSenseUpperValue();
        this.mExpoTime = ((Long) this.mModule.getPreviewCaptureResult().get(CaptureResult.SENSOR_EXPOSURE_TIME)).longValue() / 2;
        int intValue = ((Integer) this.mModule.getPreviewCaptureResult().get(CaptureResult.SENSOR_SENSITIVITY)).intValue() * 2;
        if (intValue < this.mSenseValue) {
            this.mSenseValue = intValue;
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 5; i++) {
            builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(0));
            builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, new Long(this.mExpoTime));
            builder.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(this.mSenseValue));
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
        nativeInit(i5, i6, this.mStrideY, this.mStrideVU, 0, 0, i5, i6, 5);
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

    static {
        try {
            System.loadLibrary("jni_sharpshooter");
            mIsSupported = true;
        } catch (UnsatisfiedLinkError e) {
            Log.d(TAG, e.toString());
            mIsSupported = false;
        }
    }
}
