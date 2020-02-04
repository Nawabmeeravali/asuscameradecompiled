package com.android.camera;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.graphics.YuvImage;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.MosaicFrameProcessor.ProgressListener;
import com.android.camera.PanoProgressBar.OnDirectionChangeListener;
import com.android.camera.SoundClips.Player;
import com.android.camera.app.OrientationManager;
import com.android.camera.exif.ExifInterface;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.UsageStatistics;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import org.codeaurora.snapcam.C0905R;

public class WideAnglePanoramaModule implements CameraModule, WideAnglePanoramaController, OnFrameAvailableListener {
    public static final int CAPTURE_STATE_MOSAIC = 1;
    public static final int CAPTURE_STATE_VIEWFINDER = 0;
    private static final boolean DEBUG = false;
    public static final int DEFAULT_BLEND_MODE = 3;
    public static final int DEFAULT_CAPTURE_PIXELS = 691200;
    public static final int DEFAULT_SWEEP_ANGLE = 160;
    private static final int MSG_CLEAR_SCREEN_DELAY = 4;
    private static final int MSG_END_DIALOG_RESET_TO_PREVIEW = 3;
    private static final int MSG_GENERATE_FINAL_MOSAIC_ERROR = 2;
    private static final int MSG_LOW_RES_FINAL_MOSAIC_READY = 1;
    private static final int MSG_RESET_TO_PREVIEW = 5;
    private static final float PANNING_SPEED_THRESHOLD = 2.5f;
    private static final int PREVIEW_ACTIVE = 1;
    private static final int PREVIEW_STOPPED = 0;
    private static final int SCREEN_DELAY = 120000;
    private static final String TAG = "CAM_WidePanoModule";
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    private CameraProxy mCameraDevice;
    private int mCameraOrientation;
    private int mCameraPreviewHeight;
    private int mCameraPreviewWidth;
    private int mCameraState;
    private SurfaceTexture mCameraTexture;
    /* access modifiers changed from: private */
    public boolean mCancelComputation;
    /* access modifiers changed from: private */
    public int mCaptureState;
    private ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public int mDeviceOrientation;
    /* access modifiers changed from: private */
    public int mDeviceOrientationAtCapture;
    /* access modifiers changed from: private */
    public String mDialogOkString;
    /* access modifiers changed from: private */
    public String mDialogPanoramaFailedString;
    /* access modifiers changed from: private */
    public String mDialogTitle;
    private String mDialogWaitingPreviousString;
    /* access modifiers changed from: private */
    public boolean mDirectionChanged = false;
    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();
    /* access modifiers changed from: private */
    public float mHorizontalViewAngle;
    private LocationManager mLocationManager;
    /* access modifiers changed from: private */
    public Handler mMainHandler;
    /* access modifiers changed from: private */
    public MosaicFrameProcessor mMosaicFrameProcessor;
    private boolean mMosaicFrameProcessorInitialized;
    private boolean mMosaicPreviewConfigured;
    /* access modifiers changed from: private */
    public MosaicPreviewRenderer mMosaicPreviewRenderer;
    private Runnable mOnFrameAvailableRunnable;
    /* access modifiers changed from: private */
    public int mOrientationCompensation;
    private PanoOrientationEventListener mOrientationEventListener;
    /* access modifiers changed from: private */
    public boolean mOrientationLocked;
    private OrientationManager mOrientationManager;
    /* access modifiers changed from: private */
    public WakeLock mPartialWakeLock;
    /* access modifiers changed from: private */
    public boolean mPaused;
    private ComboPreferences mPreferences;
    private String mPreparePreviewString;
    private boolean mPreviewFocused = true;
    /* access modifiers changed from: private */
    public boolean mPreviewLayoutChanged = false;
    /* access modifiers changed from: private */
    public int mPreviewUIHeight;
    /* access modifiers changed from: private */
    public int mPreviewUIWidth;
    /* access modifiers changed from: private */
    public Object mRendererLock = new Object();
    /* access modifiers changed from: private */
    public View mRootView;
    private Player mSoundPlayer;
    private String mTargetFocusMode = "infinity";
    /* access modifiers changed from: private */
    public boolean mThreadRunning;
    private long mTimeTaken;
    /* access modifiers changed from: private */
    public WideAnglePanoramaUI mUI;
    private boolean mUsingFrontCamera;
    /* access modifiers changed from: private */
    public float mVerticalViewAngle;
    /* access modifiers changed from: private */
    public Object mWaitObject = new Object();
    /* access modifiers changed from: private */
    public AsyncTask<Void, Void, Void> mWaitProcessorTask;

    private class MosaicJpeg {
        public final byte[] data;
        public final int height;
        public final boolean isValid;
        public final int width;

        public MosaicJpeg(byte[] bArr, int i, int i2) {
            this.data = bArr;
            this.width = i;
            this.height = i2;
            this.isValid = true;
        }

        public MosaicJpeg() {
            this.data = null;
            this.width = 0;
            this.height = 0;
            this.isValid = false;
        }
    }

    private class PanoOrientationEventListener extends OrientationEventListener {
        public PanoOrientationEventListener(Context context) {
            super(context);
        }

        public void onOrientationChanged(int i) {
            if (i != -1) {
                int access$000 = WideAnglePanoramaModule.this.mDeviceOrientation;
                WideAnglePanoramaModule wideAnglePanoramaModule = WideAnglePanoramaModule.this;
                wideAnglePanoramaModule.mDeviceOrientation = CameraUtil.roundOrientation(i, wideAnglePanoramaModule.mDeviceOrientation);
                int access$0002 = WideAnglePanoramaModule.this.mDeviceOrientation + (CameraUtil.getDisplayRotation(WideAnglePanoramaModule.this.mActivity) % 360);
                if (WideAnglePanoramaModule.this.mOrientationCompensation != access$0002) {
                    WideAnglePanoramaModule.this.mOrientationCompensation = access$0002;
                }
                if (!(access$000 == WideAnglePanoramaModule.this.mDeviceOrientation || access$000 == -1)) {
                    WideAnglePanoramaModule.this.mPreviewLayoutChanged = true;
                    if (!WideAnglePanoramaModule.this.mOrientationLocked) {
                        WideAnglePanoramaModule.this.mUI.setOrientation(WideAnglePanoramaModule.this.mDeviceOrientation, true);
                    }
                }
            }
        }
    }

    private class WaitProcessorTask extends AsyncTask<Void, Void, Void> {
        private WaitProcessorTask() {
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
        /* JADX WARNING: Missing exception handler attribute for start block: B:2:0x0007 */
        /* JADX WARNING: Removed duplicated region for block: B:2:0x0007 A[LOOP:0: B:2:0x0007->B:17:0x0007, LOOP_START, SYNTHETIC] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.lang.Void doInBackground(java.lang.Void... r2) {
            /*
                r1 = this;
                com.android.camera.WideAnglePanoramaModule r2 = com.android.camera.WideAnglePanoramaModule.this
                com.android.camera.MosaicFrameProcessor r2 = r2.mMosaicFrameProcessor
                monitor-enter(r2)
            L_0x0007:
                boolean r0 = r1.isCancelled()     // Catch:{ all -> 0x002f }
                if (r0 != 0) goto L_0x0023
                com.android.camera.WideAnglePanoramaModule r0 = com.android.camera.WideAnglePanoramaModule.this     // Catch:{ all -> 0x002f }
                com.android.camera.MosaicFrameProcessor r0 = r0.mMosaicFrameProcessor     // Catch:{ all -> 0x002f }
                boolean r0 = r0.isMosaicMemoryAllocated()     // Catch:{ all -> 0x002f }
                if (r0 == 0) goto L_0x0023
                com.android.camera.WideAnglePanoramaModule r0 = com.android.camera.WideAnglePanoramaModule.this     // Catch:{ Exception -> 0x0007 }
                com.android.camera.MosaicFrameProcessor r0 = r0.mMosaicFrameProcessor     // Catch:{ Exception -> 0x0007 }
                r0.wait()     // Catch:{ Exception -> 0x0007 }
                goto L_0x0007
            L_0x0023:
                monitor-exit(r2)     // Catch:{ all -> 0x002f }
                com.android.camera.WideAnglePanoramaModule r1 = com.android.camera.WideAnglePanoramaModule.this
                com.android.camera.CameraActivity r1 = r1.mActivity
                r1.updateStorageSpace()
                r1 = 0
                return r1
            L_0x002f:
                r1 = move-exception
                monitor-exit(r2)     // Catch:{ all -> 0x002f }
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.WideAnglePanoramaModule.WaitProcessorTask.doInBackground(java.lang.Void[]):java.lang.Void");
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void voidR) {
            WideAnglePanoramaModule.this.mWaitProcessorTask = null;
            WideAnglePanoramaModule.this.mUI.dismissAllDialogs();
            WideAnglePanoramaModule.this.initMosaicFrameProcessorIfNeeded();
            Point previewAreaSize = WideAnglePanoramaModule.this.mUI.getPreviewAreaSize();
            WideAnglePanoramaModule.this.mPreviewUIWidth = previewAreaSize.x;
            WideAnglePanoramaModule.this.mPreviewUIHeight = previewAreaSize.y;
            WideAnglePanoramaModule.this.configMosaicPreview();
            WideAnglePanoramaModule.this.resetToPreviewIfPossible();
            WideAnglePanoramaModule.this.mActivity.updateStorageHint(WideAnglePanoramaModule.this.mActivity.getStorageSpaceBytes());
        }
    }

    public void installIntentFilter() {
    }

    public void onActivityResult(int i, int i2, Intent intent) {
    }

    public void onCaptureTextureCopied() {
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

    public void onOrientationChanged(int i) {
    }

    public void onPreviewTextureCopied() {
    }

    public void onPreviewUIDestroyed() {
    }

    public void onShowSwitcherPopup() {
    }

    public void onSingleTapUp(View view, int i, int i2) {
    }

    public void onStop() {
    }

    public void onStorageNotEnoughRecordingVideo() {
    }

    public void resizeForPreviewAspectRatio() {
    }

    public void setPreferenceForTest(String str, String str2) {
    }

    public boolean updateStorageHintOnResume() {
        return false;
    }

    public void onPreviewUIReady() {
        configMosaicPreview();
    }

    private int getPreferredCameraId(ComboPreferences comboPreferences) {
        int cameraFacingIntentExtras = CameraUtil.getCameraFacingIntentExtras(this.mActivity);
        if (cameraFacingIntentExtras != -1) {
            return cameraFacingIntentExtras;
        }
        return CameraSettings.readPreferredCameraId(comboPreferences);
    }

    public void init(CameraActivity cameraActivity, View view) {
        this.mActivity = cameraActivity;
        this.mRootView = view;
        this.mOrientationManager = new OrientationManager(cameraActivity);
        this.mCaptureState = 0;
        this.mUI = new WideAnglePanoramaUI(this.mActivity, this, (ViewGroup) this.mRootView);
        this.mUI.setCaptureProgressOnDirectionChangeListener(new OnDirectionChangeListener() {
            public void onDirectionChange(int i) {
                if (WideAnglePanoramaModule.this.mDirectionChanged) {
                    WideAnglePanoramaModule.this.stopCapture(false);
                    return;
                }
                if (WideAnglePanoramaModule.this.mCaptureState == 1) {
                    WideAnglePanoramaModule.this.mUI.showDirectionIndicators(i);
                }
                if (i != 0) {
                    WideAnglePanoramaModule.this.mDirectionChanged = true;
                }
            }
        });
        this.mContentResolver = this.mActivity.getContentResolver();
        this.mOnFrameAvailableRunnable = new Runnable() {
            /* JADX WARNING: Code restructure failed: missing block: B:13:0x002c, code lost:
                if (com.android.camera.WideAnglePanoramaModule.access$1200(r6.this$0).getVisibility() == 0) goto L_0x003b;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:14:0x002e, code lost:
                r1.showPreviewFrameSync();
                com.android.camera.WideAnglePanoramaModule.access$1200(r6.this$0).setVisibility(0);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:16:0x0041, code lost:
                if (com.android.camera.WideAnglePanoramaModule.access$800(r6.this$0) != 0) goto L_0x0078;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:18:0x0049, code lost:
                if (com.android.camera.WideAnglePanoramaModule.access$300(r6.this$0) == false) goto L_0x0074;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:19:0x004b, code lost:
                r3 = true;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:20:0x0056, code lost:
                if (((com.android.camera.WideAnglePanoramaModule.access$000(r6.this$0) / 90) % 2) != 1) goto L_0x0059;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:21:0x0059, code lost:
                r3 = false;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:22:0x005a, code lost:
                r1.previewReset(com.android.camera.WideAnglePanoramaModule.access$1300(r6.this$0), com.android.camera.WideAnglePanoramaModule.access$1400(r6.this$0), r3, com.android.camera.WideAnglePanoramaModule.access$000(r6.this$0));
                com.android.camera.WideAnglePanoramaModule.access$302(r6.this$0, false);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:23:0x0074, code lost:
                r1.showPreviewFrame();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:24:0x0078, code lost:
                r1.alignFrameSync();
                com.android.camera.WideAnglePanoramaModule.access$1500(r6.this$0).processFrame();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:25:0x0084, code lost:
                return;
             */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                /*
                    r6 = this;
                    com.android.camera.WideAnglePanoramaModule r0 = com.android.camera.WideAnglePanoramaModule.this
                    boolean r0 = r0.mPaused
                    if (r0 == 0) goto L_0x0009
                    return
                L_0x0009:
                    com.android.camera.WideAnglePanoramaModule r0 = com.android.camera.WideAnglePanoramaModule.this
                    java.lang.Object r0 = r0.mRendererLock
                    monitor-enter(r0)
                    com.android.camera.WideAnglePanoramaModule r1 = com.android.camera.WideAnglePanoramaModule.this     // Catch:{ all -> 0x0085 }
                    com.android.camera.MosaicPreviewRenderer r1 = r1.mMosaicPreviewRenderer     // Catch:{ all -> 0x0085 }
                    if (r1 != 0) goto L_0x001a
                    monitor-exit(r0)     // Catch:{ all -> 0x0085 }
                    return
                L_0x001a:
                    com.android.camera.WideAnglePanoramaModule r1 = com.android.camera.WideAnglePanoramaModule.this     // Catch:{ all -> 0x0085 }
                    com.android.camera.MosaicPreviewRenderer r1 = r1.mMosaicPreviewRenderer     // Catch:{ all -> 0x0085 }
                    monitor-exit(r0)     // Catch:{ all -> 0x0085 }
                    com.android.camera.WideAnglePanoramaModule r0 = com.android.camera.WideAnglePanoramaModule.this
                    android.view.View r0 = r0.mRootView
                    int r0 = r0.getVisibility()
                    r2 = 0
                    if (r0 == 0) goto L_0x003b
                    r1.showPreviewFrameSync()
                    com.android.camera.WideAnglePanoramaModule r6 = com.android.camera.WideAnglePanoramaModule.this
                    android.view.View r6 = r6.mRootView
                    r6.setVisibility(r2)
                    goto L_0x0084
                L_0x003b:
                    com.android.camera.WideAnglePanoramaModule r0 = com.android.camera.WideAnglePanoramaModule.this
                    int r0 = r0.mCaptureState
                    if (r0 != 0) goto L_0x0078
                    com.android.camera.WideAnglePanoramaModule r0 = com.android.camera.WideAnglePanoramaModule.this
                    boolean r0 = r0.mPreviewLayoutChanged
                    if (r0 == 0) goto L_0x0074
                    com.android.camera.WideAnglePanoramaModule r0 = com.android.camera.WideAnglePanoramaModule.this
                    int r0 = r0.mDeviceOrientation
                    int r0 = r0 / 90
                    int r0 = r0 % 2
                    r3 = 1
                    if (r0 != r3) goto L_0x0059
                    goto L_0x005a
                L_0x0059:
                    r3 = r2
                L_0x005a:
                    com.android.camera.WideAnglePanoramaModule r0 = com.android.camera.WideAnglePanoramaModule.this
                    int r0 = r0.mPreviewUIWidth
                    com.android.camera.WideAnglePanoramaModule r4 = com.android.camera.WideAnglePanoramaModule.this
                    int r4 = r4.mPreviewUIHeight
                    com.android.camera.WideAnglePanoramaModule r5 = com.android.camera.WideAnglePanoramaModule.this
                    int r5 = r5.mDeviceOrientation
                    r1.previewReset(r0, r4, r3, r5)
                    com.android.camera.WideAnglePanoramaModule r6 = com.android.camera.WideAnglePanoramaModule.this
                    r6.mPreviewLayoutChanged = r2
                L_0x0074:
                    r1.showPreviewFrame()
                    goto L_0x0084
                L_0x0078:
                    r1.alignFrameSync()
                    com.android.camera.WideAnglePanoramaModule r6 = com.android.camera.WideAnglePanoramaModule.this
                    com.android.camera.MosaicFrameProcessor r6 = r6.mMosaicFrameProcessor
                    r6.processFrame()
                L_0x0084:
                    return
                L_0x0085:
                    r6 = move-exception
                    monitor-exit(r0)     // Catch:{ all -> 0x0085 }
                    throw r6
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.camera.WideAnglePanoramaModule.C07842.run():void");
            }
        };
        this.mPartialWakeLock = ((PowerManager) this.mActivity.getSystemService("power")).newWakeLock(1, UsageStatistics.COMPONENT_PANORAMA);
        this.mOrientationEventListener = new PanoOrientationEventListener(this.mActivity);
        this.mMosaicFrameProcessor = MosaicFrameProcessor.getInstance();
        Resources resources = this.mActivity.getResources();
        this.mPreparePreviewString = resources.getString(C0905R.string.pano_dialog_prepare_preview);
        this.mDialogTitle = resources.getString(C0905R.string.pano_dialog_title);
        this.mDialogOkString = resources.getString(C0905R.string.dialog_ok);
        this.mDialogPanoramaFailedString = resources.getString(C0905R.string.pano_dialog_panorama_failed);
        this.mDialogWaitingPreviousString = resources.getString(C0905R.string.pano_dialog_waiting_previous);
        this.mPreferences = ComboPreferences.get(this.mActivity);
        if (this.mPreferences == null) {
            this.mPreferences = new ComboPreferences(this.mActivity);
        }
        ComboPreferences comboPreferences = this.mPreferences;
        comboPreferences.setLocalId(this.mActivity, getPreferredCameraId(comboPreferences));
        CameraSettings.upgradeGlobalPreferences(this.mPreferences.getGlobal(), cameraActivity);
        this.mLocationManager = new LocationManager(this.mActivity, null);
        this.mMainHandler = new Handler() {
            public void handleMessage(Message message) {
                int i = message.what;
                if (i == 1) {
                    WideAnglePanoramaModule.this.onBackgroundThreadFinished();
                    WideAnglePanoramaModule.this.saveFinalMosaic((Bitmap) message.obj);
                    WideAnglePanoramaModule.this.saveHighResMosaic();
                } else if (i == 2) {
                    WideAnglePanoramaModule.this.onBackgroundThreadFinished();
                    if (WideAnglePanoramaModule.this.mPaused) {
                        WideAnglePanoramaModule.this.resetToPreviewIfPossible();
                    } else {
                        WideAnglePanoramaModule.this.mUI.showAlertDialog(WideAnglePanoramaModule.this.mDialogTitle, WideAnglePanoramaModule.this.mDialogPanoramaFailedString, WideAnglePanoramaModule.this.mDialogOkString, new Runnable() {
                            public void run() {
                                WideAnglePanoramaModule.this.resetToPreviewIfPossible();
                            }
                        });
                    }
                    WideAnglePanoramaModule.this.clearMosaicFrameProcessorIfNeeded();
                } else if (i == 3) {
                    WideAnglePanoramaModule.this.onBackgroundThreadFinished();
                    WideAnglePanoramaModule.this.resetToPreviewIfPossible();
                    WideAnglePanoramaModule.this.clearMosaicFrameProcessorIfNeeded();
                } else if (i == 4) {
                    WideAnglePanoramaModule.this.mActivity.getWindow().clearFlags(128);
                } else if (i == 5) {
                    WideAnglePanoramaModule.this.resetToPreviewIfPossible();
                }
            }
        };
    }

    public void onPreviewFocusChanged(boolean z) {
        this.mPreviewFocused = z;
        this.mUI.onPreviewFocusChanged(z);
    }

    public boolean arePreviewControlsVisible() {
        return this.mUI.arePreviewControlsVisible();
    }

    private boolean setupCamera() {
        if (!openCamera()) {
            return false;
        }
        Parameters parameters = this.mCameraDevice.getParameters();
        setupCaptureParams(parameters);
        configureCamera(parameters);
        return true;
    }

    private void releaseCamera() {
        if (this.mCameraDevice != null) {
            if (this.mActivity.isForceReleaseCamera()) {
                CameraHolder.instance().strongRelease();
            } else {
                CameraHolder.instance().release();
            }
            this.mCameraDevice.setErrorCallback(null);
            this.mCameraDevice = null;
            this.mCameraState = 0;
        }
    }

    private boolean openCamera() {
        int backCameraId = CameraHolder.instance().getBackCameraId();
        if (backCameraId == -1) {
            backCameraId = 0;
        }
        if (this.mCameraDevice == null) {
            CameraActivity cameraActivity = this.mActivity;
            this.mCameraDevice = CameraUtil.openCamera(cameraActivity, backCameraId, this.mMainHandler, cameraActivity.getCameraOpenErrorCallback());
            if (this.mCameraDevice == null) {
                return false;
            }
        }
        this.mCameraOrientation = CameraUtil.getCameraOrientation(backCameraId);
        if (backCameraId == CameraHolder.instance().getFrontCameraId()) {
            this.mUsingFrontCamera = true;
        }
        return true;
    }

    private boolean findBestPreviewSize(List<Size> list, boolean z, boolean z2) {
        boolean z3 = false;
        int i = 691200;
        for (Size size : list) {
            int i2 = size.height;
            int i3 = size.width;
            int i4 = DEFAULT_CAPTURE_PIXELS - (i2 * i3);
            if ((!z2 || i4 >= 0) && (!z || i2 * 4 == i3 * 3)) {
                int abs = Math.abs(i4);
                if (abs < i) {
                    this.mCameraPreviewWidth = i3;
                    this.mCameraPreviewHeight = i2;
                    z3 = true;
                    i = abs;
                }
            }
        }
        return z3;
    }

    private void setupCaptureParams(Parameters parameters) {
        List supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        boolean findBestPreviewSize = findBestPreviewSize(supportedPreviewSizes, true, true);
        String str = TAG;
        if (!findBestPreviewSize) {
            Log.w(str, "No 4:3 ratio preview size supported.");
            if (!findBestPreviewSize(supportedPreviewSizes, false, true)) {
                Log.w(str, "Can't find a supported preview size smaller than 960x720.");
                findBestPreviewSize(supportedPreviewSizes, false, false);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("camera preview h = ");
        sb.append(this.mCameraPreviewHeight);
        sb.append(" , w = ");
        sb.append(this.mCameraPreviewWidth);
        Log.d(str, sb.toString());
        parameters.setPreviewSize(this.mCameraPreviewWidth, this.mCameraPreviewHeight);
        List supportedPreviewFpsRange = parameters.getSupportedPreviewFpsRange();
        int size = supportedPreviewFpsRange.size() - 1;
        int i = ((int[]) supportedPreviewFpsRange.get(size))[0];
        int i2 = ((int[]) supportedPreviewFpsRange.get(size))[1];
        parameters.setPreviewFpsRange(i, i2);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("preview fps: ");
        sb2.append(i);
        sb2.append(", ");
        sb2.append(i2);
        Log.d(str, sb2.toString());
        if (parameters.getSupportedFocusModes().indexOf(this.mTargetFocusMode) >= 0) {
            parameters.setFocusMode(this.mTargetFocusMode);
        } else {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("Cannot set the focus mode to ");
            sb3.append(this.mTargetFocusMode);
            sb3.append(" becuase the mode is not supported.");
            Log.w(str, sb3.toString());
        }
        parameters.set(CameraUtil.RECORDING_HINT, CameraUtil.FALSE);
        this.mHorizontalViewAngle = parameters.getHorizontalViewAngle();
        this.mVerticalViewAngle = parameters.getVerticalViewAngle();
    }

    public int getPreviewBufSize() {
        PixelFormat pixelFormat = new PixelFormat();
        PixelFormat.getPixelFormatInfo(this.mCameraDevice.getParameters().getPreviewFormat(), pixelFormat);
        return (((this.mCameraPreviewWidth * this.mCameraPreviewHeight) * pixelFormat.bitsPerPixel) / 8) + 32;
    }

    private void configureCamera(Parameters parameters) {
        this.mCameraDevice.setParameters(parameters);
    }

    /* access modifiers changed from: private */
    public void configMosaicPreview() {
        if (this.mPreviewUIWidth != 0 && this.mPreviewUIHeight != 0 && this.mUI.getSurfaceTexture() != null) {
            stopCameraPreview();
            synchronized (this.mRendererLock) {
                if (this.mMosaicPreviewRenderer != null) {
                    this.mMosaicPreviewRenderer.release();
                }
                this.mMosaicPreviewRenderer = null;
            }
            boolean z = (this.mDeviceOrientation / 90) % 2 == 1;
            boolean z2 = this.mActivity.getResources().getBoolean(C0905R.bool.enable_warped_pano_preview);
            this.mUI.flipPreviewIfNeeded();
            MosaicPreviewRenderer mosaicPreviewRenderer = new MosaicPreviewRenderer(this.mUI.getSurfaceTexture(), this.mPreviewUIWidth, this.mPreviewUIHeight, z, this.mDeviceOrientation, z2);
            synchronized (this.mRendererLock) {
                this.mMosaicPreviewRenderer = mosaicPreviewRenderer;
                this.mCameraTexture = this.mMosaicPreviewRenderer.getInputSurfaceTexture();
                this.mRendererLock.notifyAll();
            }
            this.mMosaicPreviewConfigured = true;
            resetToPreviewIfPossible();
        }
    }

    public void onPreviewUILayoutChange(int i, int i2, int i3, int i4) {
        StringBuilder sb = new StringBuilder();
        sb.append("layout change: ");
        int i5 = i3 - i;
        sb.append(i5);
        sb.append("/");
        int i6 = i4 - i2;
        sb.append(i6);
        Log.d(TAG, sb.toString());
        boolean z = this.mCaptureState == 1;
        if (!(this.mPreviewUIWidth == i5 && this.mPreviewUIHeight == i6 && this.mCameraState == 1)) {
            this.mPreviewUIWidth = i5;
            this.mPreviewUIHeight = i6;
            configMosaicPreview();
        }
        if (z) {
            this.mMainHandler.post(new Runnable() {
                public void run() {
                    if (!WideAnglePanoramaModule.this.mPaused) {
                        WideAnglePanoramaModule.this.mMainHandler.removeMessages(5);
                        WideAnglePanoramaModule.this.startCapture();
                    }
                }
            });
        }
    }

    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.mActivity.runOnUiThread(this.mOnFrameAvailableRunnable);
    }

    public void startCapture() {
        this.mCancelComputation = false;
        this.mTimeTaken = System.currentTimeMillis();
        this.mActivity.setSwipingEnabled(false);
        this.mCaptureState = 1;
        this.mUI.onStartCapture();
        Parameters parameters = this.mCameraDevice.getParameters();
        parameters.setAutoExposureLock(true);
        parameters.setAutoWhiteBalanceLock(true);
        configureCamera(parameters);
        this.mMosaicFrameProcessor.setProgressListener(new ProgressListener() {
            public void onProgress(boolean z, float f, float f2, float f3, float f4) {
                float access$2400 = f3 * WideAnglePanoramaModule.this.mHorizontalViewAngle;
                float access$2500 = f4 * WideAnglePanoramaModule.this.mVerticalViewAngle;
                boolean z2 = WideAnglePanoramaModule.this.mDeviceOrientationAtCapture != WideAnglePanoramaModule.this.mDeviceOrientation;
                if (z || Math.abs(access$2400) >= 160.0f || Math.abs(access$2500) >= 160.0f || z2) {
                    WideAnglePanoramaModule.this.stopCapture(false);
                    return;
                }
                float access$24002 = f * WideAnglePanoramaModule.this.mHorizontalViewAngle;
                float access$25002 = f2 * WideAnglePanoramaModule.this.mVerticalViewAngle;
                if (WideAnglePanoramaModule.this.mDeviceOrientation == 180 || WideAnglePanoramaModule.this.mDeviceOrientation == 90) {
                    access$2400 = -access$2400;
                    access$2500 = -access$2500;
                }
                WideAnglePanoramaModule.this.mUI.updateCaptureProgress(access$24002, access$25002, access$2400, access$2500, WideAnglePanoramaModule.PANNING_SPEED_THRESHOLD);
            }
        });
        this.mUI.resetCaptureProgress();
        this.mUI.setMaxCaptureProgress(160);
        this.mUI.showCaptureProgress();
        this.mDeviceOrientationAtCapture = this.mDeviceOrientation;
        keepScreenOn();
        this.mOrientationLocked = true;
        this.mUI.setProgressOrientation(CameraUtil.getDisplayOrientation(CameraUtil.getDisplayRotation(this.mActivity), CameraHolder.instance().getBackCameraId()));
    }

    /* access modifiers changed from: private */
    public void stopCapture(boolean z) {
        this.mDirectionChanged = false;
        this.mCaptureState = 0;
        this.mUI.onStopCapture();
        Parameters parameters = this.mCameraDevice.getParameters();
        parameters.setAutoExposureLock(false);
        parameters.setAutoWhiteBalanceLock(false);
        configureCamera(parameters);
        this.mMosaicFrameProcessor.setProgressListener(null);
        stopCameraPreview();
        this.mCameraTexture.setOnFrameAvailableListener(null);
        if (!z && !this.mThreadRunning) {
            this.mUI.showWaitingDialog(this.mPreparePreviewString);
            this.mUI.hideUI();
            runBackgroundThread(new Thread() {
                public void run() {
                    MosaicJpeg generateFinalMosaic = WideAnglePanoramaModule.this.generateFinalMosaic(false);
                    if (generateFinalMosaic == null || !generateFinalMosaic.isValid) {
                        WideAnglePanoramaModule.this.mMainHandler.sendMessage(WideAnglePanoramaModule.this.mMainHandler.obtainMessage(3));
                        return;
                    }
                    byte[] bArr = generateFinalMosaic.data;
                    WideAnglePanoramaModule.this.mMainHandler.sendMessage(WideAnglePanoramaModule.this.mMainHandler.obtainMessage(1, BitmapFactory.decodeByteArray(bArr, 0, bArr.length)));
                }
            });
        }
        keepScreenOnAwhile();
    }

    public void onShutterButtonClick() {
        if (!this.mPaused && !this.mThreadRunning && this.mCameraTexture != null) {
            int i = this.mCaptureState;
            String str = TAG;
            if (i == 0) {
                long storageSpaceBytes = this.mActivity.getStorageSpaceBytes();
                if (storageSpaceBytes <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Low storage warning: ");
                    sb.append(storageSpaceBytes);
                    Log.w(str, sb.toString());
                    return;
                }
                this.mSoundPlayer.play(1);
                startCapture();
            } else if (i != 1) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Unknown capture state: ");
                sb2.append(this.mCaptureState);
                Log.w(str, sb2.toString());
            } else {
                this.mSoundPlayer.play(2);
                stopCapture(false);
            }
        }
    }

    public void reportProgress() {
        this.mUI.resetSavingProgress();
        new Thread() {
            public void run() {
                while (WideAnglePanoramaModule.this.mThreadRunning) {
                    final int reportProgress = WideAnglePanoramaModule.this.mMosaicFrameProcessor.reportProgress(true, WideAnglePanoramaModule.this.mCancelComputation);
                    try {
                        synchronized (WideAnglePanoramaModule.this.mWaitObject) {
                            WideAnglePanoramaModule.this.mWaitObject.wait(50);
                        }
                        WideAnglePanoramaModule.this.mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                WideAnglePanoramaModule.this.mUI.updateSavingProgress(reportProgress);
                            }
                        });
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Panorama reportProgress failed", e);
                    }
                }
            }
        }.start();
    }

    /* access modifiers changed from: private */
    public int getCaptureOrientation() {
        if (this.mUsingFrontCamera) {
            return ((this.mDeviceOrientationAtCapture - this.mCameraOrientation) - 360) % 360;
        }
        return (this.mDeviceOrientationAtCapture + this.mCameraOrientation) % 360;
    }

    public int getCameraOrientation() {
        return this.mCameraOrientation;
    }

    public void saveHighResMosaic() {
        runBackgroundThread(new Thread() {
            /* JADX INFO: finally extract failed */
            public void run() {
                WideAnglePanoramaModule.this.mPartialWakeLock.acquire();
                try {
                    MosaicJpeg generateFinalMosaic = WideAnglePanoramaModule.this.generateFinalMosaic(true);
                    WideAnglePanoramaModule.this.mPartialWakeLock.release();
                    if (generateFinalMosaic == null) {
                        WideAnglePanoramaModule.this.mMainHandler.sendEmptyMessage(3);
                    } else if (!generateFinalMosaic.isValid) {
                        WideAnglePanoramaModule.this.mMainHandler.sendEmptyMessage(2);
                    } else {
                        final Uri access$3200 = WideAnglePanoramaModule.this.savePanorama(generateFinalMosaic.data, generateFinalMosaic.width, generateFinalMosaic.height, WideAnglePanoramaModule.this.getCaptureOrientation());
                        if (access$3200 != null) {
                            WideAnglePanoramaModule.this.mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    WideAnglePanoramaModule.this.mUI.showFinalMosaic();
                                    WideAnglePanoramaModule.this.mActivity.notifyNewMedia(access$3200);
                                }
                            });
                        }
                        WideAnglePanoramaModule.this.mMainHandler.sendMessage(WideAnglePanoramaModule.this.mMainHandler.obtainMessage(3));
                    }
                } catch (Throwable th) {
                    WideAnglePanoramaModule.this.mPartialWakeLock.release();
                    throw th;
                }
            }
        });
        reportProgress();
    }

    private void runBackgroundThread(Thread thread) {
        this.mThreadRunning = true;
        thread.start();
    }

    /* access modifiers changed from: private */
    public void onBackgroundThreadFinished() {
        this.mThreadRunning = false;
        this.mUI.dismissAllDialogs();
    }

    private void cancelHighResComputation() {
        this.mCancelComputation = true;
        synchronized (this.mWaitObject) {
            this.mWaitObject.notify();
        }
    }

    private void reset() {
        this.mCaptureState = 0;
        this.mDirectionChanged = false;
        this.mOrientationLocked = false;
        this.mUI.setOrientation(this.mDeviceOrientation, true);
        this.mUI.reset();
        this.mActivity.setSwipingEnabled(true);
        if (this.mPreviewFocused) {
            this.mUI.showPreviewUI();
        }
        this.mMosaicFrameProcessor.reset();
    }

    /* access modifiers changed from: private */
    public void resetToPreviewIfPossible() {
        reset();
        if (this.mMosaicFrameProcessorInitialized && this.mUI.getSurfaceTexture() != null && this.mMosaicPreviewConfigured && !this.mPaused) {
            startCameraPreview();
        }
    }

    /* access modifiers changed from: private */
    public void saveFinalMosaic(Bitmap bitmap) {
        this.mUI.saveFinalMosaic(bitmap, getCaptureOrientation());
    }

    /* access modifiers changed from: private */
    public Uri savePanorama(byte[] bArr, int i, int i2, int i3) {
        byte[] bArr2 = bArr;
        if (bArr2 == null) {
            return null;
        }
        String createName = PanoUtil.createName(this.mActivity.getResources().getString(C0905R.string.pano_file_name_format), this.mTimeTaken);
        String generateFilepath = Storage.generateFilepath(createName, PhotoModule.PIXEL_FORMAT_JPEG);
        StringBuilder sb = new StringBuilder();
        sb.append(createName);
        sb.append(Storage.JPEG_POSTFIX);
        sb.toString();
        Location currentLocation = this.mLocationManager.getCurrentLocation();
        ExifInterface exifInterface = new ExifInterface();
        try {
            exifInterface.readExif(bArr2);
            exifInterface.addMakeAndModelTag();
            exifInterface.addGpsDateTimeStampTag(this.mTimeTaken);
            exifInterface.addDateTimeStampTag(ExifInterface.TAG_DATE_TIME, this.mTimeTaken, TimeZone.getDefault());
            exifInterface.setTag(exifInterface.buildTag(ExifInterface.TAG_ORIENTATION, Short.valueOf(ExifInterface.getOrientationValueForRotation(i3))));
            writeLocation(currentLocation, exifInterface);
            exifInterface.writeExif(bArr2, generateFilepath);
        } catch (IOException e) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Cannot set exif for ");
            sb2.append(generateFilepath);
            Log.e(TAG, sb2.toString(), e);
            Storage.writeFile(generateFilepath, bArr2);
        }
        return Storage.addImage(this.mContentResolver, createName, this.mTimeTaken, currentLocation, i3, (int) new File(generateFilepath).length(), generateFilepath, i, i2, "image/jpeg");
    }

    private static void writeLocation(Location location, ExifInterface exifInterface) {
        if (location != null) {
            exifInterface.addGpsTags(location.getLatitude(), location.getLongitude());
            exifInterface.setTag(exifInterface.buildTag(ExifInterface.TAG_GPS_PROCESSING_METHOD, location.getProvider()));
        }
    }

    /* access modifiers changed from: private */
    public void clearMosaicFrameProcessorIfNeeded() {
        if (this.mPaused && !this.mThreadRunning && this.mMosaicFrameProcessorInitialized) {
            this.mMosaicFrameProcessor.clear();
            this.mMosaicFrameProcessorInitialized = false;
        }
    }

    /* access modifiers changed from: private */
    public void initMosaicFrameProcessorIfNeeded() {
        if (!this.mPaused && !this.mThreadRunning) {
            int integer = ((ActivityManager) this.mActivity.getSystemService("activity")).isLowRamDevice() ? this.mActivity.getResources().getInteger(C0905R.integer.panorama_frame_size_reduction) : 100;
            int i = (this.mCameraPreviewWidth * integer) / 100;
            int i2 = (this.mCameraPreviewHeight * integer) / 100;
            if (i <= 0 || i2 <= 0) {
                throw new RuntimeException("Invalid preview dimension");
            }
            this.mMosaicFrameProcessor.initialize(i, i2, getPreviewBufSize());
            this.mMosaicFrameProcessorInitialized = true;
        }
    }

    public void waitingLocationPermissionResult(boolean z) {
        this.mLocationManager.waitingLocationPermissionResult(z);
    }

    public void enableRecordingLocation(boolean z) {
        String str = z ? RecordLocationPreference.VALUE_ON : "off";
        ComboPreferences comboPreferences = this.mPreferences;
        if (comboPreferences != null) {
            comboPreferences.edit().putString("pref_camera_recordlocation_key", str).apply();
        }
        this.mLocationManager.recordLocation(z);
    }

    public void onPauseBeforeSuper() {
        this.mPaused = true;
        LocationManager locationManager = this.mLocationManager;
        if (locationManager != null) {
            locationManager.recordLocation(false);
        }
        this.mOrientationManager.pause();
    }

    public void onPauseAfterSuper() {
        this.mOrientationEventListener.disable();
        if (this.mCameraDevice != null) {
            if (this.mCaptureState == 1) {
                stopCapture(true);
                reset();
            }
            this.mUI.showPreviewCover();
            releaseCamera();
            synchronized (this.mRendererLock) {
                this.mCameraTexture = null;
                if (this.mMosaicPreviewRenderer != null) {
                    this.mMosaicPreviewRenderer.release();
                    this.mMosaicPreviewRenderer = null;
                }
            }
            clearMosaicFrameProcessorIfNeeded();
            AsyncTask<Void, Void, Void> asyncTask = this.mWaitProcessorTask;
            if (asyncTask != null) {
                asyncTask.cancel(true);
                this.mWaitProcessorTask = null;
            }
            resetScreenOn();
            this.mUI.removeDisplayChangeListener();
            Player player = this.mSoundPlayer;
            if (player != null) {
                player.release();
                this.mSoundPlayer = null;
            }
            System.gc();
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        this.mUI.onConfigurationChanged(configuration, this.mThreadRunning);
    }

    public void onSwitchSavePath() {
        this.mPreferences.getGlobal().edit().putString("pref_camera_savepath_key", "1").apply();
        RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.on_switch_save_path_to_sdcard, 0).show();
    }

    public void onResumeBeforeSuper() {
        this.mPaused = false;
        this.mPreferences = ComboPreferences.get(this.mActivity);
        if (this.mPreferences == null) {
            this.mPreferences = new ComboPreferences(this.mActivity);
        }
        CameraSettings.upgradeGlobalPreferences(this.mPreferences.getGlobal(), this.mActivity);
        ComboPreferences comboPreferences = this.mPreferences;
        comboPreferences.setLocalId(this.mActivity, getPreferredCameraId(comboPreferences));
        CameraSettings.upgradeLocalPreferences(this.mPreferences.getLocal());
    }

    public void onResumeAfterSuper() {
        this.mOrientationEventListener.enable();
        this.mCaptureState = 0;
        if (!setupCamera()) {
            CameraUtil.showErrorAndFinish(this.mActivity, C0905R.string.cannot_connect_camera);
            Log.e(TAG, "Failed to open camera, aborting");
            return;
        }
        this.mSoundPlayer = SoundClips.getPlayer(this.mActivity);
        this.mUI.dismissAllDialogs();
        if (!this.mThreadRunning || !this.mMosaicFrameProcessor.isMosaicMemoryAllocated()) {
            if (!this.mMosaicFrameProcessorInitialized) {
                initMosaicFrameProcessorIfNeeded();
            }
            Point previewAreaSize = this.mUI.getPreviewAreaSize();
            this.mPreviewUIWidth = previewAreaSize.x;
            this.mPreviewUIHeight = previewAreaSize.y;
            configMosaicPreview();
            this.mMainHandler.post(new Runnable() {
                public void run() {
                    WideAnglePanoramaModule.this.mActivity.updateStorageSpaceAndHint();
                }
            });
        } else {
            this.mUI.showWaitingDialog(this.mDialogWaitingPreviousString);
            this.mUI.hideUI();
            this.mWaitProcessorTask = new WaitProcessorTask().execute(new Void[0]);
        }
        this.mUI.setSwitcherIndex();
        keepScreenOnAwhile();
        this.mOrientationManager.resume();
        this.mLocationManager.recordLocation(RecordLocationPreference.get(this.mPreferences, "pref_camera_recordlocation_key"));
        this.mUI.initDisplayChangeListener();
    }

    public MosaicJpeg generateFinalMosaic(boolean z) {
        int createMosaic = this.mMosaicFrameProcessor.createMosaic(z);
        if (createMosaic == -2) {
            return null;
        }
        if (createMosaic == -1) {
            return new MosaicJpeg();
        }
        byte[] finalMosaicNV21 = this.mMosaicFrameProcessor.getFinalMosaicNV21();
        String str = TAG;
        if (finalMosaicNV21 == null) {
            Log.e(str, "getFinalMosaicNV21() returned null.");
            return new MosaicJpeg();
        }
        int length = finalMosaicNV21.length - 8;
        int i = (finalMosaicNV21[length + 0] << 24) + ((finalMosaicNV21[length + 1] & 255) << 16) + ((finalMosaicNV21[length + 2] & 255) << 8) + (finalMosaicNV21[length + 3] & 255);
        int i2 = (finalMosaicNV21[length + 4] << 24) + ((finalMosaicNV21[length + 5] & 255) << 16) + ((finalMosaicNV21[length + 6] & 255) << 8) + (finalMosaicNV21[length + 7] & 255);
        StringBuilder sb = new StringBuilder();
        sb.append("ImLength = ");
        sb.append(length);
        String str2 = ", W = ";
        sb.append(str2);
        sb.append(i);
        String str3 = ", H = ";
        sb.append(str3);
        sb.append(i2);
        Log.d(str, sb.toString());
        if (i <= 0 || i2 <= 0) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("width|height <= 0!!, len = ");
            sb2.append(length);
            sb2.append(str2);
            sb2.append(i);
            sb2.append(str3);
            sb2.append(i2);
            Log.e(str, sb2.toString());
            return new MosaicJpeg();
        }
        YuvImage yuvImage = new YuvImage(finalMosaicNV21, 17, i, i2, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, i, i2), 100, byteArrayOutputStream);
        try {
            byteArrayOutputStream.close();
            return new MosaicJpeg(byteArrayOutputStream.toByteArray(), i, i2);
        } catch (Exception e) {
            Log.e(str, "Exception in storing final mosaic", e);
            return new MosaicJpeg();
        }
    }

    private void startCameraPreview() {
        if (this.mCameraDevice != null && this.mUI.getSurfaceTexture() != null) {
            this.mErrorCallback.setActivity(this.mActivity);
            this.mCameraDevice.setErrorCallback(this.mErrorCallback);
            synchronized (this.mRendererLock) {
                if (this.mCameraTexture != null) {
                    if (this.mCameraState != 0) {
                        stopCameraPreview();
                    }
                    this.mCameraDevice.setDisplayOrientation(0);
                    if (this.mCameraTexture != null) {
                        this.mCameraTexture.setOnFrameAvailableListener(this);
                    }
                    this.mCameraDevice.setPreviewTexture(this.mCameraTexture);
                    this.mCameraDevice.startPreview();
                    this.mCameraState = 1;
                }
            }
        }
    }

    private void stopCameraPreview() {
        CameraProxy cameraProxy = this.mCameraDevice;
        if (!(cameraProxy == null || this.mCameraState == 0)) {
            cameraProxy.stopPreview();
        }
        this.mCameraState = 0;
    }

    public void onUserInteraction() {
        if (this.mCaptureState != 1) {
            keepScreenOnAwhile();
        }
    }

    public boolean onBackPressed() {
        if (!this.mThreadRunning && !this.mUI.hideSwitcherPopup()) {
            return false;
        }
        return true;
    }

    private void resetScreenOn() {
        this.mMainHandler.removeMessages(4);
        this.mActivity.getWindow().clearFlags(128);
    }

    private void keepScreenOnAwhile() {
        this.mMainHandler.removeMessages(4);
        this.mActivity.getWindow().addFlags(128);
        this.mMainHandler.sendEmptyMessageDelayed(4, 120000);
    }

    private void keepScreenOn() {
        this.mMainHandler.removeMessages(4);
        this.mActivity.getWindow().addFlags(128);
    }

    public void cancelHighResStitching() {
        if (!this.mPaused && this.mCameraTexture != null) {
            cancelHighResComputation();
        }
    }
}
