package com.android.camera.imageprocessor.filter;

import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.os.Handler;
import com.android.camera.util.PersistUtil;
import java.nio.ByteBuffer;
import java.util.List;

public interface ImageFilter {
    public static final boolean DEBUG = (PersistUtil.getCamera2Debug() == 2 || PersistUtil.getCamera2Debug() == 100);

    public static class ResultImage {
        public int height;
        public ByteBuffer outBuffer;
        public byte[] outBufferArray;
        public Rect outRoi;
        public int stride;
        public int width;

        public ResultImage(ByteBuffer byteBuffer, Rect rect, int i, int i2, int i3) {
            this.outBuffer = byteBuffer;
            this.outRoi = rect;
            this.width = i;
            this.height = i2;
            this.stride = i3;
        }

        public ResultImage(byte[] bArr, Rect rect, int i, int i2, int i3) {
            this.outBufferArray = bArr;
            this.outRoi = rect;
            this.width = i;
            this.height = i2;
            this.stride = i3;
        }
    }

    void addImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, Object obj);

    void deinit();

    int getNumRequiredImage();

    String getStringName();

    void init(int i, int i2, int i3, int i4);

    boolean isFrameListener();

    boolean isManualMode();

    boolean isSupported();

    void manualCapture(Builder builder, CameraCaptureSession cameraCaptureSession, CaptureCallback captureCallback, Handler handler) throws CameraAccessException;

    ResultImage processImage();

    List<CaptureRequest> setRequiredImages(Builder builder);
}
