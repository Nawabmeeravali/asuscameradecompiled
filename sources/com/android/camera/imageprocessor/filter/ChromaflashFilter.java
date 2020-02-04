package com.android.camera.imageprocessor.filter;

import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureRequest.Key;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.util.Log;
import com.android.camera.CaptureModule;
import com.android.camera.imageprocessor.filter.ImageFilter.ResultImage;
import java.nio.ByteBuffer;
import java.util.List;

public class ChromaflashFilter implements ImageFilter {
    public static final int NUM_REQUIRED_IMAGE = 6;
    /* access modifiers changed from: private */
    public static String TAG = "ChromaflashFilter";
    private static boolean mIsSupported = false;
    private int mHeight;
    private int mImageNum = -1;
    /* access modifiers changed from: private */
    public CaptureModule mModule;
    private ByteBuffer mOutBuf;
    private int mStrideVU;
    private int mStrideY;
    private int mWidth;

    private native int nativeAddImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3);

    private native int nativeDeinit();

    private native int nativeInit(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9);

    private native int nativeProcessImage(byte[] bArr, int[] iArr);

    public int getNumRequiredImage() {
        return 6;
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

    public ChromaflashFilter(CaptureModule captureModule) {
        this.mModule = captureModule;
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
        this.mImageNum = -1;
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
        nativeInit(i5, i6, this.mStrideY, this.mStrideVU, 0, 0, i5, i6, 6);
    }

    public void deinit() {
        Log("deinit");
        this.mOutBuf = null;
        this.mImageNum = -1;
        nativeDeinit();
    }

    public void addImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, Object obj) {
        Log("addImage");
        if (i == 1 || i == 2 || i == 4) {
            this.mImageNum = i;
            return;
        }
        int remaining = byteBuffer.remaining();
        int remaining2 = byteBuffer2.remaining();
        this.mImageNum = i;
        if (nativeAddImage(byteBuffer, byteBuffer2, remaining, remaining2, i) != 0) {
            Log.e(TAG, "Fail to add image");
        }
    }

    public ResultImage processImage() {
        Log("processImage ");
        int[] iArr = new int[4];
        int nativeProcessImage = nativeProcessImage(this.mOutBuf.array(), iArr);
        Log("processImage done");
        this.mImageNum = -1;
        if (nativeProcessImage < 0) {
            Log.w(TAG, "Fail to process the image.");
        }
        ResultImage resultImage = new ResultImage(this.mOutBuf, new Rect(iArr[0], iArr[1], iArr[0] + iArr[2], iArr[1] + iArr[3]), this.mWidth, this.mHeight, this.mStrideY);
        return resultImage;
    }

    public boolean isSupported() {
        return mIsSupported;
    }

    public void manualCapture(Builder builder, CameraCaptureSession cameraCaptureSession, CaptureCallback captureCallback, Handler handler) throws CameraAccessException {
        final CameraCaptureSession cameraCaptureSession2 = cameraCaptureSession;
        final Builder builder2 = builder;
        final CaptureCallback captureCallback2 = captureCallback;
        final Handler handler2 = handler;
        C08301 r0 = new Thread() {
            public void run() {
                for (int i = 0; i < 6; i++) {
                    if (i == 0) {
                        try {
                            cameraCaptureSession2.capture(builder2.build(), captureCallback2, handler2);
                            ChromaflashFilter.this.waitForImage(i);
                        } catch (CameraAccessException unused) {
                            return;
                        }
                    } else if (i == 1) {
                        builder2.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.FALSE);
                        builder2.set(CaptureRequest.FLASH_MODE, Integer.valueOf(1));
                        cameraCaptureSession2.capture(builder2.build(), captureCallback2, handler2);
                        ChromaflashFilter.this.waitForImage(i);
                    } else if (i == 2) {
                        builder2.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
                        Builder createCaptureRequest = cameraCaptureSession2.getDevice().createCaptureRequest(1);
                        CaptureRequest build = builder2.build();
                        for (Key key : build.getKeys()) {
                            createCaptureRequest.set(key, build.get(key));
                        }
                        createCaptureRequest.addTarget(ChromaflashFilter.this.mModule.getPreviewSurfaceForSession(ChromaflashFilter.this.mModule.getMainCameraId()));
                        ChromaflashFilter.this.waitForAeBlock(createCaptureRequest, builder2, captureCallback2, cameraCaptureSession2, handler2, 5);
                    } else if (i == 3) {
                        cameraCaptureSession2.capture(builder2.build(), captureCallback2, handler2);
                        ChromaflashFilter.this.waitForImage(i);
                    } else if (i == 4) {
                        builder2.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
                        builder2.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.FALSE);
                        cameraCaptureSession2.capture(builder2.build(), captureCallback2, handler2);
                        ChromaflashFilter.this.waitForImage(i);
                    } else if (i == 5) {
                        cameraCaptureSession2.capture(builder2.build(), captureCallback2, handler2);
                    }
                }
            }
        };
        r0.start();
    }

    /* access modifiers changed from: private */
    public void waitForImage(int i) {
        while (this.mImageNum < i) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException unused) {
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void waitForAeBlock(Builder builder, Builder builder2, CaptureCallback captureCallback, CameraCaptureSession cameraCaptureSession, Handler handler, int i) {
        try {
            CaptureRequest build = builder.build();
            final int i2 = i;
            final Builder builder3 = builder;
            final Builder builder4 = builder2;
            final CaptureCallback captureCallback2 = captureCallback;
            final CameraCaptureSession cameraCaptureSession2 = cameraCaptureSession;
            final Handler handler2 = handler;
            C08312 r1 = new CaptureCallback() {
                private boolean mAeStateConverged = false;

                public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
                    Integer num = (Integer) totalCaptureResult.get(CaptureResult.CONTROL_AE_STATE);
                    String access$300 = ChromaflashFilter.TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("AE tunning onCaptureCompleted aeState = ");
                    sb.append(num);
                    Log.d(access$300, sb.toString());
                    if (num != null && num.intValue() == 2) {
                        this.mAeStateConverged = true;
                    }
                    String access$3002 = ChromaflashFilter.TAG;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("AE tunning completed mAeStateConverged = ");
                    sb2.append(this.mAeStateConverged);
                    Log.d(access$3002, sb2.toString());
                    if (!this.mAeStateConverged) {
                        int i = i2;
                        if (i >= 2) {
                            ChromaflashFilter.this.waitForAeBlock(builder3, builder4, captureCallback2, cameraCaptureSession2, handler2, i - 1);
                            return;
                        }
                    }
                    try {
                        cameraCaptureSession2.capture(builder4.build(), captureCallback2, handler2);
                    } catch (CameraAccessException unused) {
                    }
                }
            };
            CameraCaptureSession cameraCaptureSession3 = cameraCaptureSession;
            Handler handler3 = handler;
            cameraCaptureSession.capture(build, r1, handler);
        } catch (CameraAccessException unused) {
        }
    }

    public static boolean isSupportedStatic() {
        return mIsSupported;
    }

    static {
        try {
            System.loadLibrary("jni_chromaflash");
            mIsSupported = true;
        } catch (UnsatisfiedLinkError e) {
            Log.d(TAG, e.toString());
            mIsSupported = false;
        }
    }
}
