package com.android.camera;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.CameraDataCallback;
import android.hardware.Camera.CameraMetaDataCallback;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.OnZoomChangeListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import com.android.camera.CameraManager.CameraAFCallback;
import com.android.camera.CameraManager.CameraAFMoveCallback;
import com.android.camera.CameraManager.CameraFaceDetectionCallback;
import com.android.camera.CameraManager.CameraOpenErrorCallback;
import com.android.camera.CameraManager.CameraPictureCallback;
import com.android.camera.CameraManager.CameraPreviewDataCallback;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraManager.CameraShutterCallback;
import com.android.camera.util.CameraUtil;
import java.io.IOException;

class AndroidCameraManagerImpl implements CameraManager {
    private static final int ADD_CALLBACK_BUFFER = 105;
    private static final int AUTO_FOCUS = 301;
    private static final int CAMERA_HAL_API_VERSION_1_0 = 256;
    private static final int CANCEL_AUTO_FOCUS = 302;
    private static final int ENABLE_SHUTTER_SOUND = 501;
    private static final int GET_PARAMETERS = 202;
    private static final int LOCK = 5;
    private static final int OPEN_CAMERA = 1;
    private static final int RECONNECT = 3;
    private static final int REFRESH_PARAMETERS = 203;
    private static final int RELEASE = 2;
    private static final int SEND_HISTOGRAM_DATA = 602;
    private static final int SET_AUTO_FOCUS_MOVE_CALLBACK = 303;
    private static final int SET_AUTO_HDR_MODE = 801;
    private static final int SET_DISPLAY_ORIENTATION = 502;
    private static final int SET_ERROR_CALLBACK = 464;
    private static final int SET_FACE_DETECTION_LISTENER = 461;
    private static final int SET_HISTOGRAM_MODE = 601;
    private static final int SET_LONGSHOT = 701;
    private static final int SET_ONESHOT_PREVIEW_CALLBACK = 108;
    private static final int SET_PARAMETERS = 201;
    private static final int SET_PREVIEW_CALLBACK = 107;
    private static final int SET_PREVIEW_CALLBACK_WITH_BUFFER = 104;
    private static final int SET_PREVIEW_DISPLAY_ASYNC = 106;
    private static final int SET_PREVIEW_TEXTURE_ASYNC = 101;
    private static final int SET_ZOOM_CHANGE_LISTENER = 304;
    private static final int START_FACE_DETECTION = 462;
    private static final int START_PREVIEW_ASYNC = 102;
    private static final int STOP_FACE_DETECTION = 463;
    private static final int STOP_PREVIEW = 103;
    /* access modifiers changed from: private */
    public static final String TAG;
    private static final int UNLOCK = 4;
    /* access modifiers changed from: private */
    public Camera mCamera;
    /* access modifiers changed from: private */
    public CameraHandler mCameraHandler;
    /* access modifiers changed from: private */
    public Parameters mParameters;
    /* access modifiers changed from: private */
    public boolean mParametersIsDirty;
    /* access modifiers changed from: private */
    public Parameters mParamsToSet;
    /* access modifiers changed from: private */
    public IOException mReconnectIOException;
    /* access modifiers changed from: private */
    public ConditionVariable mSig = new ConditionVariable();

    private static class AFCallbackForward implements AutoFocusCallback {
        /* access modifiers changed from: private */
        public final CameraAFCallback mCallback;
        /* access modifiers changed from: private */
        public final CameraProxy mCamera;
        private final Handler mHandler;

        public static AFCallbackForward getNewInstance(Handler handler, CameraProxy cameraProxy, CameraAFCallback cameraAFCallback) {
            if (handler == null || cameraProxy == null || cameraAFCallback == null) {
                return null;
            }
            return new AFCallbackForward(handler, cameraProxy, cameraAFCallback);
        }

        private AFCallbackForward(Handler handler, CameraProxy cameraProxy, CameraAFCallback cameraAFCallback) {
            this.mHandler = handler;
            this.mCamera = cameraProxy;
            this.mCallback = cameraAFCallback;
        }

        public void onAutoFocus(final boolean z, Camera camera) {
            final Camera camera2 = this.mCamera.getCamera();
            this.mHandler.post(new Runnable() {
                public void run() {
                    Camera camera = camera2;
                    if (camera != null && camera.equals(AFCallbackForward.this.mCamera.getCamera())) {
                        AFCallbackForward.this.mCallback.onAutoFocus(z, AFCallbackForward.this.mCamera);
                    }
                }
            });
        }
    }

    @TargetApi(16)
    private static class AFMoveCallbackForward implements AutoFocusMoveCallback {
        /* access modifiers changed from: private */
        public final CameraAFMoveCallback mCallback;
        /* access modifiers changed from: private */
        public final CameraProxy mCamera;
        private final Handler mHandler;

        public static AFMoveCallbackForward getNewInstance(Handler handler, CameraProxy cameraProxy, CameraAFMoveCallback cameraAFMoveCallback) {
            if (handler == null || cameraProxy == null || cameraAFMoveCallback == null) {
                return null;
            }
            return new AFMoveCallbackForward(handler, cameraProxy, cameraAFMoveCallback);
        }

        private AFMoveCallbackForward(Handler handler, CameraProxy cameraProxy, CameraAFMoveCallback cameraAFMoveCallback) {
            this.mHandler = handler;
            this.mCamera = cameraProxy;
            this.mCallback = cameraAFMoveCallback;
        }

        public void onAutoFocusMoving(final boolean z, Camera camera) {
            final Camera camera2 = this.mCamera.getCamera();
            if (camera2 != null) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Camera camera = camera2;
                        if (camera != null && camera.equals(AFMoveCallbackForward.this.mCamera.getCamera())) {
                            AFMoveCallbackForward.this.mCallback.onAutoFocusMoving(z, AFMoveCallbackForward.this.mCamera);
                        }
                    }
                });
            }
        }
    }

    public class AndroidCameraProxyImpl implements CameraProxy {
        private AndroidCameraProxyImpl() {
            CameraUtil.Assert(AndroidCameraManagerImpl.this.mCamera != null);
        }

        public Camera getCamera() {
            return AndroidCameraManagerImpl.this.mCamera;
        }

        public void release() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(2);
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
        }

        public boolean reconnect(Handler handler, CameraOpenErrorCallback cameraOpenErrorCallback) {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(3);
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
            CameraOpenErrorCallbackForward newInstance = CameraOpenErrorCallbackForward.getNewInstance(handler, cameraOpenErrorCallback);
            if (AndroidCameraManagerImpl.this.mReconnectIOException == null) {
                return true;
            }
            if (newInstance != null) {
                newInstance.onReconnectionFailure(AndroidCameraManagerImpl.this);
            }
            return false;
        }

        public void unlock() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(4);
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
        }

        public void lock() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(5);
        }

        public void setMetadataCb(CameraMetaDataCallback cameraMetaDataCallback) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(801, cameraMetaDataCallback).sendToTarget();
        }

        public void setPreviewTexture(SurfaceTexture surfaceTexture) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(101, surfaceTexture).sendToTarget();
        }

        public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(106, surfaceHolder).sendToTarget();
        }

        public void startPreview() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(102);
        }

        public void stopPreview() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(103);
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone(200);
        }

        public void setPreviewDataCallback(Handler handler, CameraPreviewDataCallback cameraPreviewDataCallback) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(107, PreviewCallbackForward.getNewInstance(handler, this, cameraPreviewDataCallback)).sendToTarget();
        }

        public void setPreviewDataCallbackWithBuffer(Handler handler, CameraPreviewDataCallback cameraPreviewDataCallback) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(104, PreviewCallbackForward.getNewInstance(handler, this, cameraPreviewDataCallback)).sendToTarget();
        }

        public void setOneShotPreviewCallback(Handler handler, CameraPreviewDataCallback cameraPreviewDataCallback) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(108, PreviewCallbackForward.getNewInstance(handler, this, cameraPreviewDataCallback)).sendToTarget();
        }

        public void addCallbackBuffer(byte[] bArr) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(105, bArr).sendToTarget();
        }

        public void autoFocus(Handler handler, CameraAFCallback cameraAFCallback) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl.AUTO_FOCUS, AFCallbackForward.getNewInstance(handler, this, cameraAFCallback)).sendToTarget();
        }

        public void cancelAutoFocus() {
            AndroidCameraManagerImpl.this.mCameraHandler.removeMessages(AndroidCameraManagerImpl.AUTO_FOCUS);
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(AndroidCameraManagerImpl.CANCEL_AUTO_FOCUS);
        }

        @TargetApi(16)
        public void setAutoFocusMoveCallback(Handler handler, CameraAFMoveCallback cameraAFMoveCallback) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl.SET_AUTO_FOCUS_MOVE_CALLBACK, AFMoveCallbackForward.getNewInstance(handler, this, cameraAFMoveCallback)).sendToTarget();
        }

        public void takePicture(Handler handler, CameraShutterCallback cameraShutterCallback, CameraPictureCallback cameraPictureCallback, CameraPictureCallback cameraPictureCallback2, CameraPictureCallback cameraPictureCallback3) {
            AndroidCameraManagerImpl.this.mCameraHandler.requestTakePicture(ShutterCallbackForward.getNewInstance(handler, this, cameraShutterCallback), PictureCallbackForward.getNewInstance(handler, this, cameraPictureCallback), PictureCallbackForward.getNewInstance(handler, this, cameraPictureCallback2), PictureCallbackForward.getNewInstance(handler, this, cameraPictureCallback3));
        }

        public void setDisplayOrientation(int i) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl.SET_DISPLAY_ORIENTATION, i, 0).sendToTarget();
        }

        public void setZoomChangeListener(OnZoomChangeListener onZoomChangeListener) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl.SET_ZOOM_CHANGE_LISTENER, onZoomChangeListener).sendToTarget();
        }

        public void setFaceDetectionCallback(Handler handler, CameraFaceDetectionCallback cameraFaceDetectionCallback) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl.SET_FACE_DETECTION_LISTENER, FaceDetectionCallbackForward.getNewInstance(handler, this, cameraFaceDetectionCallback)).sendToTarget();
        }

        public void startFaceDetection() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(AndroidCameraManagerImpl.START_FACE_DETECTION);
        }

        public void stopFaceDetection() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(AndroidCameraManagerImpl.STOP_FACE_DETECTION);
        }

        public void setErrorCallback(ErrorCallback errorCallback) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl.SET_ERROR_CALLBACK, errorCallback).sendToTarget();
        }

        public void setParameters(Parameters parameters) {
            if (parameters == null) {
                Log.v(AndroidCameraManagerImpl.TAG, "null parameters in setParameters()");
                return;
            }
            AndroidCameraManagerImpl.this.mSig.close();
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(201, parameters).sendToTarget();
            AndroidCameraManagerImpl.this.mSig.block();
        }

        public Parameters getParameters() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(202);
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
            return AndroidCameraManagerImpl.this.mParameters;
        }

        public void refreshParameters() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(203);
        }

        public void enableShutterSound(boolean z) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl.ENABLE_SHUTTER_SOUND, z ? 1 : 0, 0).sendToTarget();
        }

        public void setLongshot(boolean z) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(701, new Boolean(z)).sendToTarget();
        }

        public void setHistogramMode(CameraDataCallback cameraDataCallback) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl.SET_HISTOGRAM_MODE, cameraDataCallback).sendToTarget();
        }

        public void sendHistogramData() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(AndroidCameraManagerImpl.SEND_HISTOGRAM_DATA);
        }
    }

    private class CameraHandler extends Handler {
        CameraOpenErrorCallbackForward errorCbInstance;

        CameraHandler(Looper looper) {
            super(looper);
        }

        private void startFaceDetection() {
            AndroidCameraManagerImpl.this.mCamera.startFaceDetection();
        }

        private void stopFaceDetection() {
            AndroidCameraManagerImpl.this.mCamera.stopFaceDetection();
        }

        private void setFaceDetectionListener(FaceDetectionListener faceDetectionListener) {
            AndroidCameraManagerImpl.this.mCamera.setFaceDetectionListener(faceDetectionListener);
        }

        private void setPreviewTexture(Object obj) {
            try {
                AndroidCameraManagerImpl.this.mCamera.setPreviewTexture((SurfaceTexture) obj);
            } catch (IOException e) {
                Log.e(AndroidCameraManagerImpl.TAG, "Could not set preview texture", e);
            }
        }

        @TargetApi(17)
        private void enableShutterSound(boolean z) {
            AndroidCameraManagerImpl.this.mCamera.enableShutterSound(z);
        }

        @TargetApi(16)
        private void setAutoFocusMoveCallback(Camera camera, Object obj) {
            camera.setAutoFocusMoveCallback((AutoFocusMoveCallback) obj);
        }

        public void requestTakePicture(ShutterCallback shutterCallback, PictureCallback pictureCallback, PictureCallback pictureCallback2, PictureCallback pictureCallback3) {
            final ShutterCallback shutterCallback2 = shutterCallback;
            final PictureCallback pictureCallback4 = pictureCallback;
            final PictureCallback pictureCallback5 = pictureCallback2;
            final PictureCallback pictureCallback6 = pictureCallback3;
            C05081 r0 = new Runnable() {
                public void run() {
                    try {
                        AndroidCameraManagerImpl.this.mCamera.takePicture(shutterCallback2, pictureCallback4, pictureCallback5, pictureCallback6);
                    } catch (RuntimeException e) {
                        Log.e(AndroidCameraManagerImpl.TAG, "take picture failed.");
                        throw e;
                    }
                }
            };
            post(r0);
        }

        public boolean waitDone() {
            final Object obj = new Object();
            C05092 r1 = new Runnable() {
                public void run() {
                    synchronized (obj) {
                        obj.notifyAll();
                    }
                }
            };
            synchronized (obj) {
                AndroidCameraManagerImpl.this.mCameraHandler.post(r1);
                try {
                    obj.wait();
                } catch (InterruptedException unused) {
                    Log.v(AndroidCameraManagerImpl.TAG, "waitDone interrupted");
                    return false;
                }
            }
            return true;
        }

        public boolean waitDone(long j) {
            final Object obj = new Object();
            C05103 r1 = new Runnable() {
                public void run() {
                    synchronized (obj) {
                        obj.notifyAll();
                    }
                }
            };
            synchronized (obj) {
                AndroidCameraManagerImpl.this.mCameraHandler.post(r1);
                try {
                    obj.wait(j);
                    AndroidCameraManagerImpl.this.mCameraHandler.removeCallbacks(r1);
                } catch (InterruptedException unused) {
                    Log.v(AndroidCameraManagerImpl.TAG, "waitDone interrupted");
                    return false;
                }
            }
            return true;
        }

        /* JADX WARNING: Can't wrap try/catch for region: R(6:56|57|58|59|(1:61)|62) */
        /* JADX WARNING: Missing exception handler attribute for start block: B:58:0x010a */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x0117 A[Catch:{ RuntimeException -> 0x0290 }] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r9) {
            /*
                r8 = this;
                r0 = 2
                r1 = 0
                r2 = 0
                r3 = 1
                int r4 = r9.what     // Catch:{ RuntimeException -> 0x0290 }
                if (r4 == r3) goto L_0x01fc
                if (r4 == r0) goto L_0x01e2
                r5 = 3
                if (r4 == r5) goto L_0x01cc
                r5 = 4
                if (r4 == r5) goto L_0x01c2
                r5 = 5
                if (r4 == r5) goto L_0x01b8
                r5 = 501(0x1f5, float:7.02E-43)
                if (r4 == r5) goto L_0x01ad
                r5 = 502(0x1f6, float:7.03E-43)
                if (r4 == r5) goto L_0x01a1
                r5 = 601(0x259, float:8.42E-43)
                if (r4 == r5) goto L_0x0192
                r5 = 602(0x25a, float:8.44E-43)
                if (r4 == r5) goto L_0x0187
                r5 = 701(0x2bd, float:9.82E-43)
                if (r4 == r5) goto L_0x0174
                r5 = 801(0x321, float:1.122E-42)
                if (r4 == r5) goto L_0x0165
                switch(r4) {
                    case 101: goto L_0x015f;
                    case 102: goto L_0x0145;
                    case 103: goto L_0x013b;
                    case 104: goto L_0x012d;
                    case 105: goto L_0x011f;
                    case 106: goto L_0x00fc;
                    case 107: goto L_0x00ee;
                    case 108: goto L_0x00e0;
                    default: goto L_0x002e;
                }     // Catch:{ RuntimeException -> 0x0290 }
            L_0x002e:
                switch(r4) {
                    case 201: goto L_0x00c3;
                    case 202: goto L_0x00a6;
                    case 203: goto L_0x00a0;
                    default: goto L_0x0031;
                }     // Catch:{ RuntimeException -> 0x0290 }
            L_0x0031:
                switch(r4) {
                    case 301: goto L_0x0092;
                    case 302: goto L_0x0088;
                    case 303: goto L_0x007c;
                    case 304: goto L_0x006e;
                    default: goto L_0x0034;
                }     // Catch:{ RuntimeException -> 0x0290 }
            L_0x0034:
                switch(r4) {
                    case 461: goto L_0x0066;
                    case 462: goto L_0x0062;
                    case 463: goto L_0x005e;
                    case 464: goto L_0x0050;
                    default: goto L_0x0037;
                }     // Catch:{ RuntimeException -> 0x0290 }
            L_0x0037:
                java.lang.RuntimeException r4 = new java.lang.RuntimeException     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x0290 }
                r5.<init>()     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.String r6 = "Invalid CameraProxy message="
                r5.append(r6)     // Catch:{ RuntimeException -> 0x0290 }
                int r6 = r9.what     // Catch:{ RuntimeException -> 0x0290 }
                r5.append(r6)     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.String r5 = r5.toString()     // Catch:{ RuntimeException -> 0x0290 }
                r4.<init>(r5)     // Catch:{ RuntimeException -> 0x0290 }
                throw r4     // Catch:{ RuntimeException -> 0x0290 }
            L_0x0050:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.Object r5 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera$ErrorCallback r5 = (android.hardware.Camera.ErrorCallback) r5     // Catch:{ RuntimeException -> 0x0290 }
                r4.setErrorCallback(r5)     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x005e:
                r8.stopFaceDetection()     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x0062:
                r8.startFaceDetection()     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x0066:
                java.lang.Object r4 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera$FaceDetectionListener r4 = (android.hardware.Camera.FaceDetectionListener) r4     // Catch:{ RuntimeException -> 0x0290 }
                r8.setFaceDetectionListener(r4)     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x006e:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.Object r5 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera$OnZoomChangeListener r5 = (android.hardware.Camera.OnZoomChangeListener) r5     // Catch:{ RuntimeException -> 0x0290 }
                r4.setZoomChangeListener(r5)     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x007c:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.Object r5 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                r8.setAutoFocusMoveCallback(r4, r5)     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x0088:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                r4.cancelAutoFocus()     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x0092:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.Object r5 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera$AutoFocusCallback r5 = (android.hardware.Camera.AutoFocusCallback) r5     // Catch:{ RuntimeException -> 0x0290 }
                r4.autoFocus(r5)     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x00a0:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                r4.mParametersIsDirty = r3     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x00a6:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                boolean r4 = r4.mParametersIsDirty     // Catch:{ RuntimeException -> 0x0290 }
                if (r4 == 0) goto L_0x00c2
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                com.android.camera.AndroidCameraManagerImpl r5 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r5 = r5.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera$Parameters r5 = r5.getParameters()     // Catch:{ RuntimeException -> 0x0290 }
                r4.mParameters = r5     // Catch:{ RuntimeException -> 0x0290 }
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                r4.mParametersIsDirty = r1     // Catch:{ RuntimeException -> 0x0290 }
            L_0x00c2:
                return
            L_0x00c3:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                r4.mParametersIsDirty = r3     // Catch:{ RuntimeException -> 0x0290 }
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.Object r5 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera$Parameters r5 = (android.hardware.Camera.Parameters) r5     // Catch:{ RuntimeException -> 0x0290 }
                r4.setParameters(r5)     // Catch:{ RuntimeException -> 0x0290 }
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.os.ConditionVariable r4 = r4.mSig     // Catch:{ RuntimeException -> 0x0290 }
                r4.open()     // Catch:{ RuntimeException -> 0x0290 }
                goto L_0x02f4
            L_0x00e0:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.Object r5 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera$PreviewCallback r5 = (android.hardware.Camera.PreviewCallback) r5     // Catch:{ RuntimeException -> 0x0290 }
                r4.setOneShotPreviewCallback(r5)     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x00ee:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.Object r5 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera$PreviewCallback r5 = (android.hardware.Camera.PreviewCallback) r5     // Catch:{ RuntimeException -> 0x0290 }
                r4.setPreviewCallback(r5)     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x00fc:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ IOException -> 0x010a }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ IOException -> 0x010a }
                java.lang.Object r5 = r9.obj     // Catch:{ IOException -> 0x010a }
                android.view.SurfaceHolder r5 = (android.view.SurfaceHolder) r5     // Catch:{ IOException -> 0x010a }
                r4.setPreviewDisplay(r5)     // Catch:{ IOException -> 0x010a }
                goto L_0x011e
            L_0x010a:
                java.lang.String r4 = com.android.camera.AndroidCameraManagerImpl.TAG     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.String r5 = "setPreviewDisplay failed, surface is destoried"
                android.util.Log.d(r4, r5)     // Catch:{ RuntimeException -> 0x0290 }
                com.android.camera.AndroidCameraManagerImpl$CameraOpenErrorCallbackForward r4 = r8.errorCbInstance     // Catch:{ RuntimeException -> 0x0290 }
                if (r4 == 0) goto L_0x011e
                com.android.camera.AndroidCameraManagerImpl$CameraOpenErrorCallbackForward r4 = r8.errorCbInstance     // Catch:{ RuntimeException -> 0x0290 }
                int r5 = r9.arg1     // Catch:{ RuntimeException -> 0x0290 }
                r4.onStartPreviewFailure(r5)     // Catch:{ RuntimeException -> 0x0290 }
            L_0x011e:
                return
            L_0x011f:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.Object r5 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                byte[] r5 = (byte[]) r5     // Catch:{ RuntimeException -> 0x0290 }
                r4.addCallbackBuffer(r5)     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x012d:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.Object r5 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera$PreviewCallback r5 = (android.hardware.Camera.PreviewCallback) r5     // Catch:{ RuntimeException -> 0x0290 }
                r4.setPreviewCallbackWithBuffer(r5)     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x013b:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                r4.stopPreview()     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x0145:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ Exception -> 0x014f }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ Exception -> 0x014f }
                r4.startPreview()     // Catch:{ Exception -> 0x014f }
                goto L_0x015e
            L_0x014f:
                r4 = move-exception
                r4.printStackTrace()     // Catch:{ RuntimeException -> 0x0290 }
                com.android.camera.AndroidCameraManagerImpl$CameraOpenErrorCallbackForward r4 = r8.errorCbInstance     // Catch:{ RuntimeException -> 0x0290 }
                if (r4 == 0) goto L_0x015e
                com.android.camera.AndroidCameraManagerImpl$CameraOpenErrorCallbackForward r4 = r8.errorCbInstance     // Catch:{ RuntimeException -> 0x0290 }
                int r5 = r9.arg1     // Catch:{ RuntimeException -> 0x0290 }
                r4.onStartPreviewFailure(r5)     // Catch:{ RuntimeException -> 0x0290 }
            L_0x015e:
                return
            L_0x015f:
                java.lang.Object r4 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                r8.setPreviewTexture(r4)     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x0165:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.Object r5 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera$CameraMetaDataCallback r5 = (android.hardware.Camera.CameraMetaDataCallback) r5     // Catch:{ RuntimeException -> 0x0290 }
                org.codeaurora.snapcam.wrapper.CameraWrapper.setMetadataCb(r4, r5)     // Catch:{ RuntimeException -> 0x0290 }
                goto L_0x02f4
            L_0x0174:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.Object r5 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.Boolean r5 = (java.lang.Boolean) r5     // Catch:{ RuntimeException -> 0x0290 }
                boolean r5 = r5.booleanValue()     // Catch:{ RuntimeException -> 0x0290 }
                org.codeaurora.snapcam.wrapper.CameraWrapper.setLongshot(r4, r5)     // Catch:{ RuntimeException -> 0x0290 }
                goto L_0x02f4
            L_0x0187:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                org.codeaurora.snapcam.wrapper.CameraWrapper.sendHistogramData(r4)     // Catch:{ RuntimeException -> 0x0290 }
                goto L_0x02f4
            L_0x0192:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.Object r5 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera$CameraDataCallback r5 = (android.hardware.Camera.CameraDataCallback) r5     // Catch:{ RuntimeException -> 0x0290 }
                org.codeaurora.snapcam.wrapper.CameraWrapper.setHistogramMode(r4, r5)     // Catch:{ RuntimeException -> 0x0290 }
                goto L_0x02f4
            L_0x01a1:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                int r5 = r9.arg1     // Catch:{ RuntimeException -> 0x0290 }
                r4.setDisplayOrientation(r5)     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x01ad:
                int r4 = r9.arg1     // Catch:{ RuntimeException -> 0x0290 }
                if (r4 != r3) goto L_0x01b3
                r4 = r3
                goto L_0x01b4
            L_0x01b3:
                r4 = r1
            L_0x01b4:
                r8.enableShutterSound(r4)     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x01b8:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                r4.lock()     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x01c2:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                r4.unlock()     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x01cc:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                r4.mReconnectIOException = r2     // Catch:{ RuntimeException -> 0x0290 }
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ IOException -> 0x01db }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ IOException -> 0x01db }
                r4.reconnect()     // Catch:{ IOException -> 0x01db }
                goto L_0x01e1
            L_0x01db:
                r4 = move-exception
                com.android.camera.AndroidCameraManagerImpl r5 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                r5.mReconnectIOException = r4     // Catch:{ RuntimeException -> 0x0290 }
            L_0x01e1:
                return
            L_0x01e2:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                if (r4 != 0) goto L_0x01eb
                return
            L_0x01eb:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                r4.release()     // Catch:{ RuntimeException -> 0x0290 }
                r8.errorCbInstance = r2     // Catch:{ RuntimeException -> 0x0290 }
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                r4.mCamera = r2     // Catch:{ RuntimeException -> 0x0290 }
                return
            L_0x01fc:
                java.lang.String r4 = "android.hardware.Camera"
                java.lang.Class r4 = java.lang.Class.forName(r4)     // Catch:{ Exception -> 0x0230 }
                java.lang.String r5 = "openLegacy"
                java.lang.Class[] r6 = new java.lang.Class[r0]     // Catch:{ Exception -> 0x0230 }
                java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ Exception -> 0x0230 }
                r6[r1] = r7     // Catch:{ Exception -> 0x0230 }
                java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ Exception -> 0x0230 }
                r6[r3] = r7     // Catch:{ Exception -> 0x0230 }
                java.lang.reflect.Method r4 = r4.getMethod(r5, r6)     // Catch:{ Exception -> 0x0230 }
                com.android.camera.AndroidCameraManagerImpl r5 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ Exception -> 0x0230 }
                java.lang.Object[] r6 = new java.lang.Object[r0]     // Catch:{ Exception -> 0x0230 }
                int r7 = r9.arg1     // Catch:{ Exception -> 0x0230 }
                java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ Exception -> 0x0230 }
                r6[r1] = r7     // Catch:{ Exception -> 0x0230 }
                r7 = 256(0x100, float:3.59E-43)
                java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ Exception -> 0x0230 }
                r6[r3] = r7     // Catch:{ Exception -> 0x0230 }
                java.lang.Object r4 = r4.invoke(r2, r6)     // Catch:{ Exception -> 0x0230 }
                android.hardware.Camera r4 = (android.hardware.Camera) r4     // Catch:{ Exception -> 0x0230 }
                r5.mCamera = r4     // Catch:{ Exception -> 0x0230 }
                goto L_0x025d
            L_0x0230:
                r4 = move-exception
                java.lang.String r5 = com.android.camera.AndroidCameraManagerImpl.TAG     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x0290 }
                r6.<init>()     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.String r7 = "openLegacy failed due to "
                r6.append(r7)     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.String r4 = r4.getMessage()     // Catch:{ RuntimeException -> 0x0290 }
                r6.append(r4)     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.String r4 = ", using open instead"
                r6.append(r4)     // Catch:{ RuntimeException -> 0x0290 }
                java.lang.String r4 = r6.toString()     // Catch:{ RuntimeException -> 0x0290 }
                android.util.Log.v(r5, r4)     // Catch:{ RuntimeException -> 0x0290 }
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                int r5 = r9.arg1     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r5 = android.hardware.Camera.open(r5)     // Catch:{ RuntimeException -> 0x0290 }
                r4.mCamera = r5     // Catch:{ RuntimeException -> 0x0290 }
            L_0x025d:
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r4 = r4.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                if (r4 == 0) goto L_0x0282
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                r4.mParametersIsDirty = r3     // Catch:{ RuntimeException -> 0x0290 }
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera$Parameters r4 = r4.mParamsToSet     // Catch:{ RuntimeException -> 0x0290 }
                if (r4 != 0) goto L_0x028f
                com.android.camera.AndroidCameraManagerImpl r4 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                com.android.camera.AndroidCameraManagerImpl r5 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera r5 = r5.mCamera     // Catch:{ RuntimeException -> 0x0290 }
                android.hardware.Camera$Parameters r5 = r5.getParameters()     // Catch:{ RuntimeException -> 0x0290 }
                r4.mParamsToSet = r5     // Catch:{ RuntimeException -> 0x0290 }
                goto L_0x028f
            L_0x0282:
                java.lang.Object r4 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                if (r4 == 0) goto L_0x028f
                java.lang.Object r4 = r9.obj     // Catch:{ RuntimeException -> 0x0290 }
                com.android.camera.CameraManager$CameraOpenErrorCallback r4 = (com.android.camera.CameraManager.CameraOpenErrorCallback) r4     // Catch:{ RuntimeException -> 0x0290 }
                int r5 = r9.arg1     // Catch:{ RuntimeException -> 0x0290 }
                r4.onDeviceOpenFailure(r5)     // Catch:{ RuntimeException -> 0x0290 }
            L_0x028f:
                return
            L_0x0290:
                int r4 = r9.what
                if (r4 == r0) goto L_0x02d3
                com.android.camera.AndroidCameraManagerImpl r0 = com.android.camera.AndroidCameraManagerImpl.this
                android.hardware.Camera r0 = r0.mCamera
                if (r0 == 0) goto L_0x02d3
                int r9 = r9.what
                r0 = 201(0xc9, float:2.82E-43)
                if (r9 != r0) goto L_0x02ba
                com.android.camera.AndroidCameraManagerImpl r9 = com.android.camera.AndroidCameraManagerImpl.this
                r9.mParametersIsDirty = r1
                java.lang.String r9 = com.android.camera.AndroidCameraManagerImpl.TAG
                java.lang.String r0 = "Fail to set parameters"
                android.util.Log.e(r9, r0)
                com.android.camera.AndroidCameraManagerImpl r8 = com.android.camera.AndroidCameraManagerImpl.this
                android.os.ConditionVariable r8 = r8.mSig
                r8.open()
                return
            L_0x02ba:
                com.android.camera.AndroidCameraManagerImpl r9 = com.android.camera.AndroidCameraManagerImpl.this     // Catch:{ Exception -> 0x02c4 }
                android.hardware.Camera r9 = r9.mCamera     // Catch:{ Exception -> 0x02c4 }
                r9.release()     // Catch:{ Exception -> 0x02c4 }
                goto L_0x02cd
            L_0x02c4:
                java.lang.String r9 = com.android.camera.AndroidCameraManagerImpl.TAG
                java.lang.String r0 = "Fail to release the camera."
                android.util.Log.e(r9, r0)
            L_0x02cd:
                com.android.camera.AndroidCameraManagerImpl r8 = com.android.camera.AndroidCameraManagerImpl.this
                r8.mCamera = r2
                goto L_0x02f4
            L_0x02d3:
                com.android.camera.AndroidCameraManagerImpl r8 = com.android.camera.AndroidCameraManagerImpl.this
                android.hardware.Camera r8 = r8.mCamera
                if (r8 != 0) goto L_0x02f4
                int r8 = r9.what
                if (r8 != r3) goto L_0x02eb
                java.lang.Object r8 = r9.obj
                if (r8 == 0) goto L_0x02f4
                com.android.camera.CameraManager$CameraOpenErrorCallback r8 = (com.android.camera.CameraManager.CameraOpenErrorCallback) r8
                int r9 = r9.arg1
                r8.onDeviceOpenFailure(r9)
                goto L_0x02f4
            L_0x02eb:
                java.lang.String r8 = com.android.camera.AndroidCameraManagerImpl.TAG
                java.lang.String r9 = "Cannot handle message, mCamera is null."
                android.util.Log.w(r8, r9)
            L_0x02f4:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.AndroidCameraManagerImpl.CameraHandler.handleMessage(android.os.Message):void");
        }
    }

    private static class CameraOpenErrorCallbackForward implements CameraOpenErrorCallback {
        /* access modifiers changed from: private */
        public final CameraOpenErrorCallback mCallback;
        private final Handler mHandler = new Handler(Looper.getMainLooper());

        public static CameraOpenErrorCallbackForward getNewInstance(Handler handler, CameraOpenErrorCallback cameraOpenErrorCallback) {
            if (handler == null || cameraOpenErrorCallback == null) {
                return null;
            }
            return new CameraOpenErrorCallbackForward(handler, cameraOpenErrorCallback);
        }

        private CameraOpenErrorCallbackForward(Handler handler, CameraOpenErrorCallback cameraOpenErrorCallback) {
            this.mCallback = cameraOpenErrorCallback;
        }

        public void onCameraDisabled(final int i) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CameraOpenErrorCallbackForward.this.mCallback.onCameraDisabled(i);
                }
            });
        }

        public void onDeviceOpenFailure(final int i) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CameraOpenErrorCallbackForward.this.mCallback.onDeviceOpenFailure(i);
                }
            });
        }

        public void onReconnectionFailure(final CameraManager cameraManager) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CameraOpenErrorCallbackForward.this.mCallback.onReconnectionFailure(cameraManager);
                }
            });
        }

        public void onStartPreviewFailure(final int i) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CameraOpenErrorCallbackForward.this.mCallback.onStartPreviewFailure(i);
                }
            });
        }
    }

    private static class FaceDetectionCallbackForward implements FaceDetectionListener {
        /* access modifiers changed from: private */
        public final CameraFaceDetectionCallback mCallback;
        /* access modifiers changed from: private */
        public final CameraProxy mCamera;
        private final Handler mHandler;

        public static FaceDetectionCallbackForward getNewInstance(Handler handler, CameraProxy cameraProxy, CameraFaceDetectionCallback cameraFaceDetectionCallback) {
            if (handler == null || cameraProxy == null || cameraFaceDetectionCallback == null) {
                return null;
            }
            return new FaceDetectionCallbackForward(handler, cameraProxy, cameraFaceDetectionCallback);
        }

        private FaceDetectionCallbackForward(Handler handler, CameraProxy cameraProxy, CameraFaceDetectionCallback cameraFaceDetectionCallback) {
            this.mHandler = handler;
            this.mCamera = cameraProxy;
            this.mCallback = cameraFaceDetectionCallback;
        }

        public void onFaceDetection(final Face[] faceArr, Camera camera) {
            final Camera camera2 = this.mCamera.getCamera();
            this.mHandler.post(new Runnable() {
                public void run() {
                    Camera camera = camera2;
                    if (camera != null && camera.equals(FaceDetectionCallbackForward.this.mCamera.getCamera())) {
                        FaceDetectionCallbackForward.this.mCallback.onFaceDetection(faceArr, FaceDetectionCallbackForward.this.mCamera);
                    }
                }
            });
        }
    }

    private static class PictureCallbackForward implements PictureCallback {
        /* access modifiers changed from: private */
        public final CameraPictureCallback mCallback;
        /* access modifiers changed from: private */
        public final CameraProxy mCamera;
        private final Handler mHandler;

        public static PictureCallbackForward getNewInstance(Handler handler, CameraProxy cameraProxy, CameraPictureCallback cameraPictureCallback) {
            if (handler == null || cameraProxy == null || cameraPictureCallback == null) {
                return null;
            }
            return new PictureCallbackForward(handler, cameraProxy, cameraPictureCallback);
        }

        private PictureCallbackForward(Handler handler, CameraProxy cameraProxy, CameraPictureCallback cameraPictureCallback) {
            this.mHandler = handler;
            this.mCamera = cameraProxy;
            this.mCallback = cameraPictureCallback;
        }

        public void onPictureTaken(final byte[] bArr, Camera camera) {
            final Camera camera2 = this.mCamera.getCamera();
            this.mHandler.post(new Runnable() {
                public void run() {
                    Camera camera = camera2;
                    if (camera != null && camera.equals(PictureCallbackForward.this.mCamera.getCamera())) {
                        PictureCallbackForward.this.mCallback.onPictureTaken(bArr, PictureCallbackForward.this.mCamera);
                    }
                }
            });
        }
    }

    private static class PreviewCallbackForward implements PreviewCallback {
        /* access modifiers changed from: private */
        public final CameraPreviewDataCallback mCallback;
        /* access modifiers changed from: private */
        public final CameraProxy mCamera;
        private final Handler mHandler;

        public static PreviewCallbackForward getNewInstance(Handler handler, CameraProxy cameraProxy, CameraPreviewDataCallback cameraPreviewDataCallback) {
            if (handler == null || cameraProxy == null || cameraPreviewDataCallback == null) {
                return null;
            }
            return new PreviewCallbackForward(handler, cameraProxy, cameraPreviewDataCallback);
        }

        private PreviewCallbackForward(Handler handler, CameraProxy cameraProxy, CameraPreviewDataCallback cameraPreviewDataCallback) {
            this.mHandler = handler;
            this.mCamera = cameraProxy;
            this.mCallback = cameraPreviewDataCallback;
        }

        public void onPreviewFrame(final byte[] bArr, Camera camera) {
            final Camera camera2 = this.mCamera.getCamera();
            this.mHandler.post(new Runnable() {
                public void run() {
                    Camera camera = camera2;
                    if (camera != null && camera.equals(PreviewCallbackForward.this.mCamera.getCamera())) {
                        PreviewCallbackForward.this.mCallback.onPreviewFrame(bArr, PreviewCallbackForward.this.mCamera);
                    }
                }
            });
        }
    }

    private static class ShutterCallbackForward implements ShutterCallback {
        /* access modifiers changed from: private */
        public final CameraShutterCallback mCallback;
        /* access modifiers changed from: private */
        public final CameraProxy mCamera;
        private final Handler mHandler;

        public static ShutterCallbackForward getNewInstance(Handler handler, CameraProxy cameraProxy, CameraShutterCallback cameraShutterCallback) {
            if (handler == null || cameraProxy == null || cameraShutterCallback == null) {
                return null;
            }
            return new ShutterCallbackForward(handler, cameraProxy, cameraShutterCallback);
        }

        private ShutterCallbackForward(Handler handler, CameraProxy cameraProxy, CameraShutterCallback cameraShutterCallback) {
            this.mHandler = handler;
            this.mCamera = cameraProxy;
            this.mCallback = cameraShutterCallback;
        }

        public void onShutter() {
            final Camera camera = this.mCamera.getCamera();
            this.mHandler.post(new Runnable() {
                public void run() {
                    Camera camera = camera;
                    if (camera != null && camera.equals(ShutterCallbackForward.this.mCamera.getCamera())) {
                        ShutterCallbackForward.this.mCallback.onShutter(ShutterCallbackForward.this.mCamera);
                    }
                }
            });
        }
    }

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("CAM_");
        sb.append(AndroidCameraManagerImpl.class.getSimpleName());
        TAG = sb.toString();
    }

    AndroidCameraManagerImpl() {
        HandlerThread handlerThread = new HandlerThread("Camera Handler Thread");
        handlerThread.start();
        this.mCameraHandler = new CameraHandler(handlerThread.getLooper());
    }

    public CameraProxy cameraOpen(Handler handler, int i, CameraOpenErrorCallback cameraOpenErrorCallback) {
        this.mCameraHandler.errorCbInstance = CameraOpenErrorCallbackForward.getNewInstance(handler, cameraOpenErrorCallback);
        CameraHandler cameraHandler = this.mCameraHandler;
        cameraHandler.obtainMessage(1, i, 0, cameraHandler.errorCbInstance).sendToTarget();
        this.mCameraHandler.waitDone();
        if (this.mCamera != null) {
            return new AndroidCameraProxyImpl();
        }
        return null;
    }
}
