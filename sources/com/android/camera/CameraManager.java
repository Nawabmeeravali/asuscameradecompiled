package com.android.camera;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraDataCallback;
import android.hardware.Camera.CameraMetaDataCallback;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.OnZoomChangeListener;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.view.SurfaceHolder;

public interface CameraManager {

    public interface CameraAFCallback {
        void onAutoFocus(boolean z, CameraProxy cameraProxy);
    }

    public interface CameraAFMoveCallback {
        void onAutoFocusMoving(boolean z, CameraProxy cameraProxy);
    }

    public interface CameraFaceDetectionCallback {
        void onFaceDetection(Face[] faceArr, CameraProxy cameraProxy);
    }

    public interface CameraOpenErrorCallback {
        void onCameraDisabled(int i);

        void onDeviceOpenFailure(int i);

        void onReconnectionFailure(CameraManager cameraManager);

        void onStartPreviewFailure(int i);
    }

    public interface CameraPictureCallback {
        void onPictureTaken(byte[] bArr, CameraProxy cameraProxy);
    }

    public interface CameraPreviewDataCallback {
        void onPreviewFrame(byte[] bArr, CameraProxy cameraProxy);
    }

    public interface CameraProxy {
        void addCallbackBuffer(byte[] bArr);

        void autoFocus(Handler handler, CameraAFCallback cameraAFCallback);

        void cancelAutoFocus();

        void enableShutterSound(boolean z);

        Camera getCamera();

        Parameters getParameters();

        void lock();

        boolean reconnect(Handler handler, CameraOpenErrorCallback cameraOpenErrorCallback);

        void refreshParameters();

        void release();

        void sendHistogramData();

        @TargetApi(16)
        void setAutoFocusMoveCallback(Handler handler, CameraAFMoveCallback cameraAFMoveCallback);

        void setDisplayOrientation(int i);

        void setErrorCallback(ErrorCallback errorCallback);

        void setFaceDetectionCallback(Handler handler, CameraFaceDetectionCallback cameraFaceDetectionCallback);

        void setHistogramMode(CameraDataCallback cameraDataCallback);

        void setLongshot(boolean z);

        void setMetadataCb(CameraMetaDataCallback cameraMetaDataCallback);

        void setOneShotPreviewCallback(Handler handler, CameraPreviewDataCallback cameraPreviewDataCallback);

        void setParameters(Parameters parameters);

        void setPreviewDataCallback(Handler handler, CameraPreviewDataCallback cameraPreviewDataCallback);

        void setPreviewDataCallbackWithBuffer(Handler handler, CameraPreviewDataCallback cameraPreviewDataCallback);

        void setPreviewDisplay(SurfaceHolder surfaceHolder);

        void setPreviewTexture(SurfaceTexture surfaceTexture);

        void setZoomChangeListener(OnZoomChangeListener onZoomChangeListener);

        void startFaceDetection();

        void startPreview();

        void stopFaceDetection();

        void stopPreview();

        void takePicture(Handler handler, CameraShutterCallback cameraShutterCallback, CameraPictureCallback cameraPictureCallback, CameraPictureCallback cameraPictureCallback2, CameraPictureCallback cameraPictureCallback3);

        void unlock();
    }

    public interface CameraShutterCallback {
        void onShutter(CameraProxy cameraProxy);
    }

    CameraProxy cameraOpen(Handler handler, int i, CameraOpenErrorCallback cameraOpenErrorCallback);
}
