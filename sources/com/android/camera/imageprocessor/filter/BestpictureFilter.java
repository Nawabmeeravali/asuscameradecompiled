package com.android.camera.imageprocessor.filter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.TotalCaptureResult;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import com.android.camera.BestpictureActivity;
import com.android.camera.CameraActivity;
import com.android.camera.CaptureModule;
import com.android.camera.MediaSaveService.OnMediaSavedListener;
import com.android.camera.PhotoModule;
import com.android.camera.PhotoModule.NamedImages;
import com.android.camera.PhotoModule.NamedImages.NamedEntity;
import com.android.camera.imageprocessor.PostProcessor;
import com.android.camera.imageprocessor.filter.ImageFilter.ResultImage;
import com.android.camera.util.CameraUtil;
import com.asus.scenedetectlib.BuildConfig;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BestpictureFilter implements ImageFilter {
    private static final String INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE = "android.media.action.STILL_IMAGE_CAMERA_SECURE";
    public static final int NUM_REQUIRED_IMAGE = 10;
    private static String TAG = "BestpictureFilter";
    private static final int TIME_DELAY = 50;
    private static boolean mIsSupported = false;
    final String[] NAMES = {"00.jpg", "01.jpg", "02.jpg", "03.jpg", "04.jpg", "05.jpg", "06.jpg", "07.jpg", "08.jpg", "09.jpg"};
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    private ByteBuffer mBVU;
    private ByteBuffer mBY;
    private ResultImage mBestpictureResultImage;
    private Object mClosingLock = new Object();
    private int mHeight;
    private boolean mIsOn = false;
    private CaptureModule mModule;
    private NamedImages mNamedImages;
    private int mOrientation = 0;
    private PostProcessor mProcessor;
    /* access modifiers changed from: private */
    public ProgressDialog mProgressDialog;
    /* access modifiers changed from: private */
    public int mSavedCount = 0;
    private int mStrideVU;
    private int mStrideY;
    private int mWidth;

    private class BitmapOutputStream extends ByteArrayOutputStream {
        public BitmapOutputStream(int i) {
            super(i);
        }

        public byte[] getArray() {
            return this.buf;
        }
    }

    public int getNumRequiredImage() {
        return 10;
    }

    public String getStringName() {
        return "BestpictureFilter";
    }

    public boolean isFrameListener() {
        return false;
    }

    public boolean isManualMode() {
        return true;
    }

    public ResultImage processImage() {
        return null;
    }

    private static void Log(String str) {
        if (ImageFilter.DEBUG) {
            Log.d(TAG, str);
        }
    }

    public BestpictureFilter(CaptureModule captureModule, CameraActivity cameraActivity, PostProcessor postProcessor) {
        this.mModule = captureModule;
        this.mActivity = cameraActivity;
        this.mProcessor = postProcessor;
        this.mNamedImages = new NamedImages();
    }

    public List<CaptureRequest> setRequiredImages(Builder builder) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 10; i++) {
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
        this.mIsOn = true;
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
    }

    public void deinit() {
        Log("deinit");
        dismissProgressDialog();
        synchronized (this.mClosingLock) {
            this.mIsOn = false;
        }
    }

    public void addImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, Object obj) {
        String str;
        long j;
        int i2 = i;
        Log("addImage");
        if (i2 == 0) {
            showProgressDialog();
            this.mOrientation = CameraUtil.getJpegRotation(this.mModule.getMainCameraId(), this.mModule.getDisplayOrientation());
            this.mSavedCount = 0;
            this.mBY = byteBuffer;
            this.mBVU = byteBuffer2;
            byte[] yUVBytes = getYUVBytes(byteBuffer, byteBuffer2, i);
            this.mNamedImages.nameNewImage(System.currentTimeMillis());
            NamedEntity nextNameEntity = this.mNamedImages.getNextNameEntity();
            if (nextNameEntity == null) {
                str = null;
            } else {
                str = nextNameEntity.title;
            }
            if (nextNameEntity == null) {
                j = -1;
            } else {
                j = nextNameEntity.date;
            }
            this.mActivity.getMediaSaveService().addImage(yUVBytes, str, j, null, this.mWidth, this.mHeight, this.mOrientation, null, new OnMediaSavedListener() {
                public void onMediaSaved(final Uri uri) {
                    if (uri != null) {
                        BestpictureFilter.this.mActivity.notifyNewMedia(uri);
                        new Thread() {
                            public void run() {
                                while (BestpictureFilter.this.mSavedCount < 10) {
                                    try {
                                        Thread.sleep(10);
                                    } catch (Exception unused) {
                                    }
                                }
                                BestpictureFilter.this.mActivity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        BestpictureFilter.this.dismissProgressDialog();
                                        C08261 r1 = C08261.this;
                                        BestpictureFilter.this.startBestpictureActivity(uri);
                                    }
                                });
                            }
                        }.start();
                    }
                }
            }, this.mActivity.getContentResolver(), PhotoModule.PIXEL_FORMAT_JPEG);
        } else {
            ByteBuffer byteBuffer3 = byteBuffer;
            ByteBuffer byteBuffer4 = byteBuffer2;
        }
        saveBestPicture(getYUVBytes(byteBuffer, byteBuffer2, i), i2);
    }

    private void showProgressDialog() {
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                BestpictureFilter bestpictureFilter = BestpictureFilter.this;
                bestpictureFilter.mProgressDialog = ProgressDialog.show(bestpictureFilter.mActivity, BuildConfig.FLAVOR, "Saving pictures...", true, false);
                BestpictureFilter.this.mProgressDialog.show();
            }
        });
    }

    private byte[] getYUVBytes(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i) {
        synchronized (this.mClosingLock) {
            if (!this.mIsOn) {
                return null;
            }
            ResultImage resultImage = new ResultImage(ByteBuffer.allocateDirect(((this.mStrideY * this.mHeight) * 3) / 2), new Rect(0, 0, this.mWidth, this.mHeight), this.mWidth, this.mHeight, this.mStrideY);
            this.mBestpictureResultImage = resultImage;
            byteBuffer.get(this.mBestpictureResultImage.outBuffer.array(), 0, byteBuffer.remaining());
            byteBuffer2.get(this.mBestpictureResultImage.outBuffer.array(), this.mStrideY * this.mHeight, byteBuffer2.remaining());
            byteBuffer.rewind();
            byteBuffer2.rewind();
            byte[] nv21ToJpeg = nv21ToJpeg(this.mBestpictureResultImage, this.mOrientation, this.mProcessor.waitForMetaData(i));
            return nv21ToJpeg;
        }
    }

    /* access modifiers changed from: private */
    public void dismissProgressDialog() {
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (BestpictureFilter.this.mProgressDialog != null && BestpictureFilter.this.mProgressDialog.isShowing()) {
                    BestpictureFilter.this.mProgressDialog.dismiss();
                    BestpictureFilter.this.mProgressDialog = null;
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void startBestpictureActivity(Uri uri) {
        Log("Start best picture activity");
        Intent intent = new Intent();
        intent.setData(uri);
        if (this.mActivity.isSecureCamera()) {
            intent.setAction("android.media.action.STILL_IMAGE_CAMERA_SECURE");
        }
        intent.setClass(this.mActivity, BestpictureActivity.class);
        this.mActivity.startActivityForResult(intent, BestpictureActivity.BESTPICTURE_ACTIVITY_CODE);
    }

    public boolean isSupported() {
        if (this.mModule.getCurrentIntentMode() != 0) {
            return false;
        }
        return mIsSupported;
    }

    public void manualCapture(Builder builder, CameraCaptureSession cameraCaptureSession, CaptureCallback captureCallback, Handler handler) throws CameraAccessException {
        for (int i = 0; i < 10; i++) {
            cameraCaptureSession.capture(builder.build(), captureCallback, handler);
            try {
                Thread.sleep(50);
            } catch (InterruptedException unused) {
            }
        }
    }

    public static boolean isSupportedStatic() {
        return mIsSupported;
    }

    private byte[] nv21ToJpeg(ResultImage resultImage, int i, TotalCaptureResult totalCaptureResult) {
        BitmapOutputStream bitmapOutputStream = new BitmapOutputStream(1024);
        byte[] array = resultImage.outBuffer.array();
        int i2 = resultImage.width;
        int i3 = resultImage.height;
        int i4 = resultImage.stride;
        YuvImage yuvImage = new YuvImage(array, 17, i2, i3, new int[]{i4, i4});
        yuvImage.compressToJpeg(resultImage.outRoi, this.mProcessor.getJpegQualityValue(), bitmapOutputStream);
        return PostProcessor.addExifTags(bitmapOutputStream.getArray(), i, totalCaptureResult);
    }

    private void saveBestPicture(byte[] bArr, int i) {
        if (bArr != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.mActivity.getFilesDir());
            sb.append("/Bestpicture");
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
            this.mSavedCount++;
            StringBuilder sb4 = new StringBuilder();
            sb4.append(i);
            sb4.append(" image is saved");
            Log(sb4.toString());
        }
    }
}
