package com.android.camera.imageprocessor.filter;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.util.Log;
import com.android.camera.CameraActivity;
import com.android.camera.CaptureModule;
import com.android.camera.imageprocessor.PostProcessor;
import com.android.camera.imageprocessor.filter.ImageFilter.ResultImage;
import com.android.camera.util.CameraUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class UbifocusFilter implements ImageFilter {
    private static final int FOCUS_ADJUST_TIME_OUT = 400;
    private static final int META_BYTES_SIZE = 25;
    public static final int NUM_REQUIRED_IMAGE = 5;
    private static String TAG = "UbifocusFilter";
    private static boolean mIsSupported = true;
    final String[] NAMES = {"00.jpg", "01.jpg", "02.jpg", "03.jpg", "04.jpg", "DepthMapImage.y", "AllFocusImage.jpg"};
    private CameraActivity mActivity;
    /* access modifiers changed from: private */
    public Object mClosingLock = new Object();
    private int mHeight;
    private float mMinFocusDistance = -1.0f;
    private CaptureModule mModule;
    private int mOrientation = 0;
    /* access modifiers changed from: private */
    public ByteBuffer mOutBuf;
    private PostProcessor mPostProcessor;
    /* access modifiers changed from: private */
    public int mSavedCount = 0;
    private int mStrideVU;
    private int mStrideY;
    private ResultImage mUbifocusResultImage;
    private int mWidth;
    private int temp;

    private class BitmapOutputStream extends ByteArrayOutputStream {
        public BitmapOutputStream(int i) {
            super(i);
        }

        public byte[] getArray() {
            return this.buf;
        }
    }

    private native int nativeAddImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3);

    private native int nativeDeinit();

    private native int nativeGetDepthMap(byte[] bArr, int i, int i2);

    private native int nativeInit(int i, int i2, int i3, int i4, int i5);

    private native int nativeProcessImage(byte[] bArr, int[] iArr, int[] iArr2);

    public int getNumRequiredImage() {
        return 5;
    }

    public String getStringName() {
        return "UbifocusFilter";
    }

    public boolean isFrameListener() {
        return false;
    }

    public boolean isManualMode() {
        return true;
    }

    public List<CaptureRequest> setRequiredImages(Builder builder) {
        return null;
    }

    private static void Log(String str) {
        if (ImageFilter.DEBUG) {
            Log.d(TAG, str);
        }
    }

    public UbifocusFilter(CaptureModule captureModule, CameraActivity cameraActivity, PostProcessor postProcessor) {
        this.mModule = captureModule;
        this.mActivity = cameraActivity;
        this.mPostProcessor = postProcessor;
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
        synchronized (this.mClosingLock) {
            this.mOutBuf = null;
            nativeDeinit();
        }
    }

    public void addImage(final ByteBuffer byteBuffer, final ByteBuffer byteBuffer2, final int i, Object obj) {
        Log("addImage");
        if (i == 0) {
            this.mModule.setRefocusLastTaken(false);
            this.mOrientation = CameraUtil.getJpegRotation(this.mModule.getMainCameraId(), this.mModule.getDisplayOrientation());
            this.mSavedCount = 0;
        }
        if (nativeAddImage(byteBuffer, byteBuffer2, byteBuffer.remaining(), byteBuffer2.remaining(), i) < 0) {
            Log.e(TAG, "Fail to add image");
        }
        new Thread() {
            public void run() {
                synchronized (UbifocusFilter.this.mClosingLock) {
                    if (UbifocusFilter.this.mOutBuf != null) {
                        UbifocusFilter.this.saveToPrivateFile(i, UbifocusFilter.this.getYUVBytes(byteBuffer, byteBuffer2, i));
                        UbifocusFilter.this.mSavedCount = UbifocusFilter.this.mSavedCount + 1;
                    }
                }
            }
        }.start();
    }

    public ResultImage processImage() {
        Log("processImage ");
        int[] iArr = new int[4];
        int[] iArr2 = new int[2];
        if (nativeProcessImage(this.mOutBuf.array(), iArr, iArr2) < 0) {
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Fail to process the ");
            sb.append(getStringName());
            Log.w(str, sb.toString());
        } else {
            byte[] bArr = new byte[((iArr2[0] * iArr2[1]) + 25)];
            nativeGetDepthMap(bArr, iArr2[0], iArr2[1]);
            saveToPrivateFile(this.NAMES.length - 2, bArr);
            saveToPrivateFile(this.NAMES.length - 1, nv21ToJpeg(this.mOutBuf, null, new Rect(iArr[0], iArr[1], iArr[0] + iArr[2], iArr[1] + iArr[3]), this.mOrientation, 0));
            this.mModule.setRefocusLastTaken(true);
        }
        while (this.mSavedCount < 5) {
            try {
                Thread.sleep(1);
            } catch (Exception unused) {
            }
        }
        ResultImage resultImage = new ResultImage(this.mOutBuf, new Rect(iArr[0], iArr[1], iArr[0] + iArr[2], iArr[1] + iArr[3]), this.mWidth, this.mHeight, this.mStrideY);
        Log("processImage done");
        return resultImage;
    }

    public boolean isSupported() {
        return mIsSupported;
    }

    public void manualCapture(Builder builder, CameraCaptureSession cameraCaptureSession, CaptureCallback captureCallback, Handler handler) throws CameraAccessException {
        if (this.mMinFocusDistance == -1.0f) {
            this.mMinFocusDistance = ((Float) this.mModule.getMainCameraCharacteristics().get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)).floatValue();
        }
        float f = this.mMinFocusDistance / 5.0f;
        for (int i = 0; i < 5; i++) {
            float f2 = ((float) i) * f;
            CaptureModule captureModule = this.mModule;
            captureModule.setAFModeToPreview(captureModule.getMainCameraId(), 0);
            CaptureModule captureModule2 = this.mModule;
            captureModule2.setFocusDistanceToPreview(captureModule2.getMainCameraId(), f2);
            StringBuilder sb = new StringBuilder();
            sb.append("Request:  ");
            sb.append(f2);
            Log(sb.toString());
            int i2 = 400;
            while (true) {
                try {
                    Thread.sleep(5);
                    i2 -= 5;
                    if (i2 > 0) {
                        float floatValue = ((Float) this.mModule.getPreviewCaptureResult().get(CaptureResult.LENS_FOCUS_DISTANCE)).floatValue();
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("Taken focus value :");
                        sb2.append(floatValue);
                        Log(sb2.toString());
                        if (Math.abs(floatValue - f2) < 1.0f) {
                            break;
                        }
                    } else {
                        break;
                    }
                } catch (InterruptedException | NullPointerException unused) {
                }
            }
            builder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(0));
            builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, Float.valueOf(f2));
            cameraCaptureSession.capture(builder.build(), captureCallback, handler);
        }
    }

    public static boolean isSupportedStatic() {
        return mIsSupported;
    }

    private byte[] nv21ToJpeg(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, Rect rect, int i, int i2) {
        ByteBuffer allocate = ByteBuffer.allocate(((this.mStrideY * this.mHeight) * 3) / 2);
        allocate.put(byteBuffer);
        byteBuffer.rewind();
        if (byteBuffer2 != null) {
            allocate.put(byteBuffer2);
            byteBuffer2.rewind();
        }
        BitmapOutputStream bitmapOutputStream = new BitmapOutputStream(1024);
        YuvImage yuvImage = new YuvImage(allocate.array(), 17, this.mWidth, this.mHeight, new int[]{this.mStrideY, this.mStrideVU});
        yuvImage.compressToJpeg(rect, this.mPostProcessor.getJpegQualityValue(), bitmapOutputStream);
        return PostProcessor.addExifTags(bitmapOutputStream.getArray(), i, this.mPostProcessor.waitForMetaData(i2));
    }

    /* access modifiers changed from: private */
    public void saveToPrivateFile(int i, byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mActivity.getFilesDir());
        sb.append("/Ubifocus");
        String sb2 = sb.toString();
        File file = new File(sb2);
        if (!file.exists()) {
            file.mkdir();
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append(sb2);
        sb3.append("/");
        sb3.append(this.NAMES[i]);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(sb3.toString()));
            fileOutputStream.write(bArr, 0, bArr.length);
            fileOutputStream.close();
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: private */
    public byte[] getYUVBytes(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i) {
        synchronized (this.mClosingLock) {
            if (this.mOutBuf == null) {
                return null;
            }
            ResultImage resultImage = new ResultImage(ByteBuffer.allocateDirect(((this.mStrideY * this.mHeight) * 3) / 2), new Rect(0, 0, this.mWidth, this.mHeight), this.mWidth, this.mHeight, this.mStrideY);
            this.mUbifocusResultImage = resultImage;
            byteBuffer.get(this.mUbifocusResultImage.outBuffer.array(), 0, byteBuffer.remaining());
            byteBuffer2.get(this.mUbifocusResultImage.outBuffer.array(), this.mStrideY * this.mHeight, byteBuffer2.remaining());
            byteBuffer.rewind();
            byteBuffer2.rewind();
            byte[] nv21ToJpeg = nv21ToJpeg(this.mUbifocusResultImage, this.mOrientation, this.mPostProcessor.waitForMetaData(i));
            return nv21ToJpeg;
        }
    }

    private byte[] nv21ToJpeg(ResultImage resultImage, int i, TotalCaptureResult totalCaptureResult) {
        BitmapOutputStream bitmapOutputStream = new BitmapOutputStream(1024);
        byte[] array = resultImage.outBuffer.array();
        int i2 = resultImage.width;
        int i3 = resultImage.height;
        int i4 = resultImage.stride;
        YuvImage yuvImage = new YuvImage(array, 17, i2, i3, new int[]{i4, i4});
        yuvImage.compressToJpeg(resultImage.outRoi, this.mPostProcessor.getJpegQualityValue(), bitmapOutputStream);
        return PostProcessor.addExifTags(bitmapOutputStream.getArray(), i, totalCaptureResult);
    }

    static {
        try {
            System.loadLibrary("jni_ubifocus");
            mIsSupported = true;
        } catch (UnsatisfiedLinkError unused) {
            mIsSupported = false;
        }
    }
}
