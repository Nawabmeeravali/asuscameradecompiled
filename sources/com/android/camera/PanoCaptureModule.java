package com.android.camera;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;
import com.android.camera.exif.ExifInterface;
import com.android.camera.util.CameraUtil;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.codeaurora.snapcam.C0905R;

public class PanoCaptureModule implements CameraModule, PhotoController {
    private static final int BAYER_CAMERA_ID = 0;
    private static final int STATE_ERROR = 2;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final String TAG = "SnapCam_PanoCaptureModule";
    public static final float TARGET_RATIO = 1.3333334f;
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    /* access modifiers changed from: private */
    public CameraDevice mCameraDevice;
    /* access modifiers changed from: private */
    public Handler mCameraHandler;
    private String mCameraId;
    /* access modifiers changed from: private */
    public Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /* access modifiers changed from: private */
    public boolean mCameraOpened = false;
    private int mCameraSensorOrientation;
    private HandlerThread mCameraThread;
    /* access modifiers changed from: private */
    public CaptureCallback mCaptureCallback = new CaptureCallback() {
        public void onCaptureProgressed(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureResult captureResult) {
        }

        private void process(CaptureResult captureResult) {
            int access$000 = PanoCaptureModule.this.mState;
            if (access$000 == 0) {
                return;
            }
            if (access$000 == 1) {
                Integer num = (Integer) captureResult.get(CaptureResult.CONTROL_AF_STATE);
                Integer num2 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                StringBuilder sb = new StringBuilder();
                sb.append("STATE_WAITING_LOCK afState:");
                sb.append(num);
                sb.append(" aeState:");
                sb.append(num2);
                Log.d(PanoCaptureModule.TAG, sb.toString());
                if (4 == num.intValue() || 5 == num.intValue()) {
                    PanoCaptureModule.this.changePanoStatus(true, false);
                    PanoCaptureModule.this.mState = 0;
                }
            }
        }

        public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
            process(totalCaptureResult);
        }
    };
    /* access modifiers changed from: private */
    public CameraCaptureSession mCaptureSession;
    private ContentResolver mContentResolver;
    private Semaphore mFocusLockSemaphore = new Semaphore(1);
    private PanoCaptureFrameProcessor mFrameProcessor;
    private boolean mIsLockFocusAttempted = false;
    private LocationManager mLocationManager;
    private int mOrientation = -1;
    private Size mOutputSize;
    /* access modifiers changed from: private */
    public CaptureRequest mPreviewRequest;
    private Builder mPreviewRequestBuilder;
    private Object mSessionLock = new Object();
    /* access modifiers changed from: private */
    public int mState = 0;
    private final StateCallback mStateCallback = new StateCallback() {
        public void onOpened(CameraDevice cameraDevice) {
            PanoCaptureModule.this.mCameraOpenCloseLock.release();
            PanoCaptureModule.this.mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    PanoCaptureModule.this.mUI.onCameraOpened();
                }
            });
            PanoCaptureModule.this.mCameraDevice = cameraDevice;
            PanoCaptureModule.this.mCameraOpened = true;
            PanoCaptureModule.this.createSession();
        }

        public void onDisconnected(CameraDevice cameraDevice) {
            PanoCaptureModule.this.mCameraOpenCloseLock.release();
            cameraDevice.close();
            PanoCaptureModule.this.mCameraDevice = null;
        }

        public void onError(CameraDevice cameraDevice, int i) {
            Integer.parseInt(cameraDevice.getId());
            PanoCaptureModule.this.mCameraOpenCloseLock.release();
            cameraDevice.close();
            PanoCaptureModule.this.mCameraDevice = null;
            if (PanoCaptureModule.this.mActivity != null) {
                PanoCaptureModule.this.mActivity.finish();
            }
        }
    };
    private boolean mSurfaceReady = false;
    /* access modifiers changed from: private */
    public PanoCaptureUI mUI;

    static class CompareSizesByArea implements Comparator<Size> {
        CompareSizesByArea() {
        }

        public int compare(Size size, Size size2) {
            return Long.signum((((long) size.getWidth()) * ((long) size.getHeight())) - (((long) size2.getWidth()) * ((long) size2.getHeight())));
        }
    }

    public boolean arePreviewControlsVisible() {
        return false;
    }

    public void cancelAutoFocus() {
    }

    public void enableRecordingLocation(boolean z) {
    }

    public int getCameraState() {
        return 0;
    }

    public void installIntentFilter() {
    }

    public boolean isCameraIdle() {
        return false;
    }

    public boolean isImageCaptureIntent() {
        return false;
    }

    public void onActivityResult(int i, int i2, Intent intent) {
    }

    public boolean onBackPressed() {
        return false;
    }

    public void onCaptureCancelled() {
    }

    public void onCaptureDone() {
    }

    public void onCaptureRetake() {
    }

    public void onCaptureTextureCopied() {
    }

    public void onConfigurationChanged(Configuration configuration) {
    }

    public void onCountDownFinished() {
    }

    public void onDestroy() {
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        return false;
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        return false;
    }

    public void onMediaSaveServiceConnected(MediaSaveService mediaSaveService) {
    }

    public void onPreviewFocusChanged(boolean z) {
    }

    public void onPreviewRectChanged(Rect rect) {
    }

    public void onPreviewTextureCopied() {
    }

    public void onResumeBeforeSuper() {
    }

    public void onScreenSizeChanged(int i, int i2) {
    }

    public void onShowSwitcherPopup() {
    }

    public void onShutterButtonFocus(boolean z) {
    }

    public void onShutterButtonLongClick() {
    }

    public void onSingleTapUp(View view, int i, int i2) {
    }

    public void onStop() {
    }

    public void onStorageNotEnoughRecordingVideo() {
    }

    public void onSwitchSavePath() {
    }

    public void onUserInteraction() {
    }

    public int onZoomChanged(int i) {
        return 0;
    }

    public void onZoomChanged(float f) {
    }

    public void resizeForPreviewAspectRatio() {
    }

    public void setPreferenceForTest(String str, String str2) {
    }

    public void stopPreview() {
    }

    public void updateCameraOrientation() {
    }

    public boolean updateStorageHintOnResume() {
        return false;
    }

    public void waitingLocationPermissionResult(boolean z) {
    }

    private void closeSession() {
        synchronized (this.mSessionLock) {
            if (this.mFrameProcessor != null) {
                this.mFrameProcessor.clear();
                this.mFrameProcessor = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public void createSession() {
        if (this.mCameraOpened && this.mSurfaceReady) {
            synchronized (this.mSessionLock) {
                LinkedList linkedList = new LinkedList();
                try {
                    SurfaceHolder surfaceHolder = this.mUI.getSurfaceHolder();
                    Surface surface = surfaceHolder != null ? surfaceHolder.getSurface() : null;
                    if (surface != null) {
                        if (this.mFrameProcessor == null) {
                            this.mFrameProcessor = new PanoCaptureFrameProcessor(this.mOutputSize, this.mActivity, this.mUI, this);
                        }
                        this.mPreviewRequestBuilder = this.mCameraDevice.createCaptureRequest(1);
                        this.mPreviewRequestBuilder.addTarget(this.mFrameProcessor.getInputSurface());
                        this.mPreviewRequestBuilder.addTarget(surface);
                        this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(4));
                        this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(2));
                        this.mPreviewRequest = this.mPreviewRequestBuilder.build();
                        linkedList.add(surface);
                        linkedList.add(this.mFrameProcessor.getInputSurface());
                        this.mCameraDevice.createCaptureSession(linkedList, new CameraCaptureSession.StateCallback() {
                            public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                                CameraDevice access$400 = PanoCaptureModule.this.mCameraDevice;
                                String str = PanoCaptureModule.TAG;
                                if (access$400 == null) {
                                    Log.e(str, "The camera is already closed.");
                                    return;
                                }
                                PanoCaptureModule.this.mCaptureSession = cameraCaptureSession;
                                try {
                                    PanoCaptureModule.this.mCaptureSession.setRepeatingRequest(PanoCaptureModule.this.mPreviewRequest, PanoCaptureModule.this.mCaptureCallback, PanoCaptureModule.this.mCameraHandler);
                                } catch (CameraAccessException e) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("createCaptureSession: ");
                                    sb.append(e.toString());
                                    Log.e(str, sb.toString());
                                }
                            }

                            public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                                Log.e(PanoCaptureModule.TAG, "Capture session configuration is failed");
                            }
                        }, null);
                    }
                } catch (CameraAccessException e) {
                    String str = TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("createSession: ");
                    sb.append(e.toString());
                    Log.e(str, sb.toString());
                    this.mActivity.finish();
                } catch (SecurityException e2) {
                    String str2 = TAG;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("createSession: ");
                    sb2.append(e2.toString());
                    Log.e(str2, sb2.toString());
                    this.mActivity.finish();
                }
            }
        }
    }

    public void init(CameraActivity cameraActivity, View view) {
        this.mCameraOpened = false;
        this.mSurfaceReady = false;
        this.mActivity = cameraActivity;
        SettingsManager.getInstance().init();
        this.mUI = new PanoCaptureUI(cameraActivity, this, view);
        this.mContentResolver = this.mActivity.getContentResolver();
        this.mLocationManager = new LocationManager(this.mActivity, null);
    }

    public void changePanoStatus(boolean z, boolean z2) {
        this.mUI.onPanoStatusChange(z);
        PanoCaptureFrameProcessor panoCaptureFrameProcessor = this.mFrameProcessor;
        if (panoCaptureFrameProcessor != null) {
            panoCaptureFrameProcessor.changePanoStatus(z, z2);
        }
    }

    public boolean isPanoActive() {
        PanoCaptureFrameProcessor panoCaptureFrameProcessor = this.mFrameProcessor;
        if (panoCaptureFrameProcessor != null) {
            return panoCaptureFrameProcessor.isPanoActive();
        }
        return false;
    }

    private void setUpCameraOutputs() {
        CameraManager cameraManager = (CameraManager) this.mActivity.getSystemService("camera");
        try {
            String str = cameraManager.getCameraIdList()[0];
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(str);
            StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (streamConfigurationMap != null) {
                this.mCameraSensorOrientation = ((Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
                Display defaultDisplay = this.mActivity.getWindowManager().getDefaultDisplay();
                Point point = new Point();
                defaultDisplay.getSize(point);
                this.mOutputSize = getOutputSize(1.3333334f, streamConfigurationMap.getOutputSizes(35), point.x, point.y);
                this.mCameraId = str;
            }
        } catch (CameraAccessException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("setUpCameraOutputs: ");
            sb.append(e.toString());
            Log.e(TAG, sb.toString());
        }
    }

    public int getCameraSensorOrientation() {
        return this.mCameraSensorOrientation;
    }

    private Size getOutputSize(float f, Size[] sizeArr, int i, int i2) {
        Size size = sizeArr[0];
        for (Size size2 : sizeArr) {
            if (((double) Math.abs((((float) size2.getWidth()) / ((float) size2.getHeight())) - f)) < 0.01d) {
                if (size2.getWidth() <= i2 && size2.getHeight() <= i) {
                    return size2;
                }
                size = size2;
            }
        }
        return size;
    }

    public Size getPictureOutputSize() {
        return this.mOutputSize;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0058  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void closeCamera() {
        /*
            r6 = this;
            r0 = 0
            java.util.concurrent.Semaphore r1 = r6.mCameraOpenCloseLock     // Catch:{ InterruptedException -> 0x0044, all -> 0x003f }
            r2 = 2500(0x9c4, double:1.235E-320)
            java.util.concurrent.TimeUnit r4 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ InterruptedException -> 0x0044, all -> 0x003f }
            boolean r1 = r1.tryAcquire(r2, r4)     // Catch:{ InterruptedException -> 0x0044, all -> 0x003f }
            if (r1 != 0) goto L_0x0016
            java.lang.String r1 = "SnapCam_PanoCaptureModule"
            java.lang.String r2 = "Time out waiting to lock camera closing."
            android.util.Log.d(r1, r2)     // Catch:{ InterruptedException -> 0x0044, all -> 0x003f }
            r1 = 1
            goto L_0x0017
        L_0x0016:
            r1 = r0
        L_0x0017:
            android.hardware.camera2.CameraCaptureSession r2 = r6.mCaptureSession     // Catch:{ InterruptedException -> 0x003d }
            r3 = 0
            if (r2 == 0) goto L_0x0023
            android.hardware.camera2.CameraCaptureSession r2 = r6.mCaptureSession     // Catch:{ InterruptedException -> 0x003d }
            r2.close()     // Catch:{ InterruptedException -> 0x003d }
            r6.mCaptureSession = r3     // Catch:{ InterruptedException -> 0x003d }
        L_0x0023:
            android.hardware.camera2.CameraDevice r2 = r6.mCameraDevice     // Catch:{ InterruptedException -> 0x003d }
            if (r2 == 0) goto L_0x0030
            android.hardware.camera2.CameraDevice r2 = r6.mCameraDevice     // Catch:{ InterruptedException -> 0x003d }
            r2.close()     // Catch:{ InterruptedException -> 0x003d }
            r6.mCameraDevice = r3     // Catch:{ InterruptedException -> 0x003d }
            r6.mCameraOpened = r0     // Catch:{ InterruptedException -> 0x003d }
        L_0x0030:
            java.util.concurrent.Semaphore r0 = r6.mCameraOpenCloseLock
            r0.release()
            if (r1 == 0) goto L_0x003c
            com.android.camera.CameraActivity r6 = r6.mActivity
            r6.finish()
        L_0x003c:
            return
        L_0x003d:
            r0 = move-exception
            goto L_0x0048
        L_0x003f:
            r1 = move-exception
            r5 = r1
            r1 = r0
            r0 = r5
            goto L_0x0051
        L_0x0044:
            r1 = move-exception
            r5 = r1
            r1 = r0
            r0 = r5
        L_0x0048:
            java.lang.RuntimeException r2 = new java.lang.RuntimeException     // Catch:{ all -> 0x0050 }
            java.lang.String r3 = "Interrupted while trying to lock camera closing."
            r2.<init>(r3, r0)     // Catch:{ all -> 0x0050 }
            throw r2     // Catch:{ all -> 0x0050 }
        L_0x0050:
            r0 = move-exception
        L_0x0051:
            java.util.concurrent.Semaphore r2 = r6.mCameraOpenCloseLock
            r2.release()
            if (r1 == 0) goto L_0x005d
            com.android.camera.CameraActivity r6 = r6.mActivity
            r6.finish()
        L_0x005d:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.PanoCaptureModule.closeCamera():void");
    }

    private void startBackgroundThread() {
        this.mCameraThread = new HandlerThread("CameraBackground");
        this.mCameraThread.start();
        this.mCameraHandler = new Handler(this.mCameraThread.getLooper());
    }

    private void stopBackgroundThread() {
        this.mCameraThread.quitSafely();
        try {
            this.mCameraThread.join();
            this.mCameraThread = null;
            this.mCameraHandler = null;
        } catch (InterruptedException unused) {
        }
    }

    private void openCamera() {
        String str = "Can't open camera, please restart it";
        String str2 = "openCamera: ";
        String str3 = TAG;
        try {
            CameraManager cameraManager = (CameraManager) this.mActivity.getSystemService("camera");
            this.mCameraId = cameraManager.getCameraIdList()[0];
            if (!this.mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                Log.d(str3, "Time out waiting to lock camera opening.");
            }
            cameraManager.openCamera(this.mCameraId, this.mStateCallback, this.mCameraHandler);
        } catch (SecurityException e) {
            StringBuilder sb = new StringBuilder();
            sb.append(str2);
            sb.append(e.toString());
            Log.e(str3, sb.toString());
            Toast.makeText(this.mActivity, str, 1).show();
            this.mActivity.finish();
        } catch (CameraAccessException e2) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str2);
            sb2.append(e2.toString());
            Log.e(str3, sb2.toString());
            Toast.makeText(this.mActivity, str, 1).show();
            this.mActivity.finish();
        } catch (InterruptedException e3) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(str2);
            sb3.append(e3.toString());
            Log.e(str3, sb3.toString());
        }
    }

    public void onPauseBeforeSuper() {
        this.mUI.applySurfaceChange(0, false);
    }

    public void onPauseAfterSuper() {
        stopBackgroundThread();
        closeCamera();
        this.mUI.onPause();
    }

    public void onResumeAfterSuper() {
        this.mUI.onResume();
        openCamera();
        setUpCameraOutputs();
        this.mUI.applySurfaceChange(2, false);
        this.mUI.setLayout(this.mOutputSize);
        startBackgroundThread();
        this.mUI.enableShutter(true);
        this.mUI.initializeShutterButton();
    }

    public Uri savePanorama(byte[] bArr, int i, int i2, int i3) {
        long currentTimeMillis = System.currentTimeMillis();
        if (bArr == null) {
            return null;
        }
        String createName = PanoUtil.createName(this.mActivity.getResources().getString(C0905R.string.pano_file_name_format), currentTimeMillis);
        String generateFilepath = Storage.generateFilepath(createName, PhotoModule.PIXEL_FORMAT_JPEG);
        Location currentLocation = this.mLocationManager.getCurrentLocation();
        ExifInterface exifInterface = new ExifInterface();
        try {
            exifInterface.readExif(bArr);
            exifInterface.addGpsDateTimeStampTag(currentTimeMillis);
            exifInterface.addDateTimeStampTag(ExifInterface.TAG_DATE_TIME, currentTimeMillis, TimeZone.getDefault());
            exifInterface.setTag(exifInterface.buildTag(ExifInterface.TAG_ORIENTATION, Integer.valueOf(i3)));
            writeLocation(currentLocation, exifInterface);
            exifInterface.writeExif(bArr, generateFilepath);
        } catch (IOException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot set exif for ");
            sb.append(generateFilepath);
            Log.e(TAG, sb.toString(), e);
            Storage.writeFile(generateFilepath, bArr);
        }
        return Storage.addImage(this.mContentResolver, createName, currentTimeMillis, currentLocation, i3, (int) new File(generateFilepath).length(), generateFilepath, i, i2, "image/jpeg");
    }

    private static void writeLocation(Location location, ExifInterface exifInterface) {
        if (location != null) {
            exifInterface.addGpsTags(location.getLatitude(), location.getLongitude());
            exifInterface.setTag(exifInterface.buildTag(ExifInterface.TAG_GPS_PROCESSING_METHOD, location.getProvider()));
        }
    }

    public void onPreviewUIReady() {
        this.mSurfaceReady = true;
        createSession();
    }

    public void onPreviewUIDestroyed() {
        closeSession();
    }

    public void onOrientationChanged(int i) {
        if (i != -1) {
            int roundOrientation = CameraUtil.roundOrientation(i, this.mOrientation);
            if (this.mOrientation != roundOrientation) {
                this.mOrientation = roundOrientation;
                this.mUI.setOrientation(roundOrientation, true);
            }
        }
    }

    public void onShutterButtonClick() {
        if (this.mFocusLockSemaphore.tryAcquire()) {
            this.mFocusLockSemaphore.release();
            if (this.mState != 1) {
                if (isPanoActive()) {
                    changePanoStatus(false, false);
                } else {
                    lockFocus();
                }
            }
        }
    }

    private void lockFocus() {
        Log.d(TAG, "lockFocus");
        Integer valueOf = Integer.valueOf(1);
        this.mIsLockFocusAttempted = true;
        try {
            this.mFocusLockSemaphore.acquire();
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(2));
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, valueOf);
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, valueOf);
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, valueOf);
            if (this.mCaptureSession != null) {
                this.mCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, this.mCameraHandler);
                this.mState = 1;
            } else {
                Toast.makeText(this.mActivity, "Session is null, can't take panorama.", 0).show();
                this.mState = 2;
            }
            this.mFocusLockSemaphore.release();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
    }

    public void unlockFocus() {
        if (this.mIsLockFocusAttempted) {
            Log.d(TAG, "unlockFocus ");
            try {
                this.mFocusLockSemaphore.acquire();
                this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(2));
                this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, Integer.valueOf(1));
                this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(4));
                this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(2));
                this.mCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, this.mCameraHandler);
                this.mState = 0;
                this.mFocusLockSemaphore.release();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (Exception unused) {
            }
            this.mIsLockFocusAttempted = false;
        }
    }
}
