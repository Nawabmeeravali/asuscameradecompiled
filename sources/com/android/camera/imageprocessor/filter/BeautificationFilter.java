package com.android.camera.imageprocessor.filter;

import android.graphics.Rect;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.params.Face;
import android.os.Handler;
import android.util.Log;
import com.android.camera.CaptureModule;
import com.android.camera.SettingsManager;
import com.android.camera.imageprocessor.filter.ImageFilter.ResultImage;
import java.nio.ByteBuffer;
import java.util.List;

public class BeautificationFilter implements ImageFilter {
    private static int FACE_TIMEOUT_VALUE = 60;
    private static String TAG = "BeautificationFilter";
    private static boolean mIsSupported = false;
    private int mFaceTimeOut = FACE_TIMEOUT_VALUE;
    int mHeight;
    private CaptureModule mModule;
    int mStrideVU;
    int mStrideY;
    int mWidth;

    private native int nativeBeautificationProcess(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9);

    public void deinit() {
    }

    public int getNumRequiredImage() {
        return 0;
    }

    public String getStringName() {
        return "BeautificationFilter";
    }

    public boolean isFrameListener() {
        return false;
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

    public BeautificationFilter(CaptureModule captureModule) {
        this.mModule = captureModule;
    }

    public void init(int i, int i2, int i3, int i4) {
        this.mWidth = i;
        this.mHeight = i2;
        this.mStrideY = i3;
        this.mStrideVU = i4;
    }

    public void addImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, Object obj) {
        Face[] faceArr;
        Rect cameraRegion = this.mModule.getCameraRegion();
        Boolean bool = (Boolean) obj;
        if (bool.booleanValue()) {
            faceArr = this.mModule.getPreviewFaces();
            if (faceArr != null && faceArr.length != 0) {
                this.mFaceTimeOut = FACE_TIMEOUT_VALUE;
            } else if (this.mFaceTimeOut > 0) {
                faceArr = this.mModule.getStickyFaces();
                this.mFaceTimeOut--;
            }
        } else {
            faceArr = this.mModule.getStickyFaces();
        }
        Face[] faceArr2 = faceArr;
        float width = ((float) this.mWidth) / ((float) cameraRegion.width());
        float height = ((float) this.mHeight) / ((float) cameraRegion.height());
        if (faceArr2 != null && faceArr2.length != 0) {
            Rect bounds = faceArr2[0].getBounds();
            int i2 = 100;
            try {
                i2 = Integer.parseInt(SettingsManager.getInstance().getValue(SettingsManager.KEY_MAKEUP));
            } catch (Exception unused) {
            }
            int i3 = i2;
            int i4 = i3;
            int nativeBeautificationProcess = nativeBeautificationProcess(byteBuffer, byteBuffer2, this.mWidth, this.mHeight, this.mStrideY, (int) (((float) bounds.left) * width), (int) (((float) bounds.top) * height), (int) (((float) bounds.right) * width), (int) (((float) bounds.bottom) * height), i3, i3);
            if (ImageFilter.DEBUG) {
                if (nativeBeautificationProcess == -1) {
                    Log.d(TAG, "library initialization is failed.");
                } else if (nativeBeautificationProcess == -2) {
                    Log.d(TAG, "No face is recognized");
                }
            }
            if (nativeBeautificationProcess >= 0 && !bool.booleanValue()) {
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("Successful beautification at ");
                sb.append(faceArr2[0].toString());
                sb.append(" widthRatio: ");
                sb.append(width);
                sb.append(" heightRatio: ");
                sb.append(height);
                sb.append(" Strength: ");
                sb.append(i4);
                Log.i(str, sb.toString());
            }
        }
    }

    public boolean isSupported() {
        return mIsSupported;
    }

    public static boolean isSupportedStatic() {
        return mIsSupported;
    }

    static {
        try {
            System.loadLibrary("jni_makeupV2");
            mIsSupported = true;
        } catch (UnsatisfiedLinkError unused) {
            mIsSupported = false;
        }
    }
}
