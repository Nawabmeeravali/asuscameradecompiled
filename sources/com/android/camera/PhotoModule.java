package com.android.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraDataCallback;
import android.hardware.Camera.CameraMetaDataCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.CameraProfile;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.support.p000v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.camera.CameraHolder.CameraInfo;
import com.android.camera.CameraManager.CameraAFCallback;
import com.android.camera.CameraManager.CameraAFMoveCallback;
import com.android.camera.CameraManager.CameraPictureCallback;
import com.android.camera.CameraManager.CameraPreviewDataCallback;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraManager.CameraShutterCallback;
import com.android.camera.CameraPreference.OnPreferenceChangedListener;
import com.android.camera.FocusOverlayManager.Listener;
import com.android.camera.MediaSaveService.OnMediaSavedListener;
import com.android.camera.PhotoUI.SURFACE_STATUS;
import com.android.camera.ShutterButton.OnShutterButtonListener;
import com.android.camera.exif.ExifInterface;
import com.android.camera.p004ui.CountDownView.OnCountDownFinishedListener;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.GcamHelper;
import com.android.camera.util.PersistUtil;
import com.android.internal.util.MemInfoReader;
import com.asus.scenedetectlib.BuildConfig;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;
import org.codeaurora.snapcam.C0905R;
import org.codeaurora.snapcam.wrapper.CameraInfoWrapper;
import org.codeaurora.snapcam.wrapper.ParametersWrapper;

public class PhotoModule implements CameraModule, PhotoController, Listener, OnPreferenceChangedListener, OnShutterButtonListener, MediaSaveService.Listener, OnCountDownFinishedListener, LocationManager.Listener, SensorEventListener, MakeupLevelListener {
    private static final int CAMERA_DISABLED = 10;
    private static final int CAMERA_OPEN_DONE = 8;
    private static final int CLEAR_SCREEN_DELAY = 3;
    private static final String DEBUG_IMAGE_PREFIX = "DEBUG_";
    private static final int DEFAULT_BRIGHTNESS = 3;
    private static final int DEPTH_EFFECT_SUCCESS = 1;
    private static final String EXTRA_QUICK_CAPTURE = "android.intent.extra.quickCapture";
    private static final int FIRST_TIME_INIT = 2;
    private static final int INSTANT_CAPTURE = 14;
    private static final String KEY_PICTURE_FORMAT = "picture-format";
    private static final String KEY_QC_RAW_PICUTRE_SIZE = "raw-size";
    private static final int LONGSHOT_CANCEL_THRESHOLD = 41943040;
    private static final int LOW_LIGHT = 4;
    private static final int MANUAL_EXPOSURE = 4;
    private static final int MANUAL_FOCUS = 1;
    private static final int MANUAL_WB = 2;
    private static final int MAXIMUM_BRIGHTNESS = 6;
    private static final int MAX_SCE_FACTOR = 10;
    private static final int MAX_SHARPNESS_LEVEL = 6;
    private static final int MAX_ZOOM = 10;
    private static final int MINIMUM_BRIGHTNESS = 0;
    private static final int MIN_SCE_FACTOR = -10;
    private static final int NO_DEPTH_EFFECT = 0;
    private static final int ON_PREVIEW_STARTED = 13;
    private static final int ON_RESUME_TASKS_DELAY_MSEC = 20;
    private static final int OPEN_CAMERA_FAIL = 9;
    private static final String PERSISI_BOKEH_DEBUG = "persist.sys.camera.bokeh.debug";
    /* access modifiers changed from: private */
    public static final boolean PERSIST_BOKEH_DEBUG_CHECK = SystemProperties.getBoolean(PERSISI_BOKEH_DEBUG, false);
    private static final boolean PERSIST_SKIP_MEM_CHECK = PersistUtil.isSkipMemoryCheckEnabled();
    public static final String PIXEL_FORMAT_JPEG = "jpeg";
    private static final int REQUEST_CROP = 1000;
    private static final int SCREEN_DELAY = 120000;
    private static final int SELFIE_FLASH_DURATION = 680;
    private static final int SETUP_PREVIEW = 1;
    private static final int SET_CAMERA_PARAMETERS_WHEN_IDLE = 4;
    private static final int SET_PHOTO_UI_PARAMS = 11;
    private static final int SHOW_TAP_TO_FOCUS_TOAST = 5;
    private static final int STATS_DATA = 257;
    private static final int SUBJECT_NOT_FOUND = 5;
    private static final int SWITCH_CAMERA = 6;
    private static final int SWITCH_CAMERA_START_ANIMATION = 7;
    private static final int SWITCH_TO_GCAM_MODULE = 12;
    private static final String TAG = "CAM_PhotoModule";
    private static final int TOO_FAR = 3;
    private static final int TOO_NEAR = 2;
    private static final int TOUCH_TO_FOCUS = 6;
    private static final int UPDATE_PARAM_ALL = -1;
    private static final int UPDATE_PARAM_INITIALIZE = 1;
    private static final int UPDATE_PARAM_PREFERENCE = 4;
    private static final int UPDATE_PARAM_ZOOM = 2;
    public static boolean mBrightnessVisible = false;
    private static final String sTempCropFilename = "crop-temp";
    public static int[] statsdata = new int[257];
    private int SCE_FACTOR_STEP = 10;
    private long SECONDARY_SERVER_MEM;
    private ProgressBar brightnessProgressBar;
    private boolean isTNREnabled;
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    private boolean mAeLockSupported;
    /* access modifiers changed from: private */
    public boolean mAnimateCapture;
    private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
    private final Object mAutoFocusMoveCallback;
    public long mAutoFocusTime;
    public boolean mAutoHdrEnable;
    private boolean mAwbLockSupported;
    private OnSeekBarChangeListener mBlurDegreeListener;
    /* access modifiers changed from: private */
    public TextView mBokehTipText;
    /* access modifiers changed from: private */
    public int mBurstSnapNum = 1;
    /* access modifiers changed from: private */
    public CameraProxy mCameraDevice;
    /* access modifiers changed from: private */
    public boolean mCameraDisabled;
    private int mCameraDisplayOrientation;
    /* access modifiers changed from: private */
    public int mCameraId;
    private boolean mCameraPreviewParamsReady;
    /* access modifiers changed from: private */
    public int mCameraState = -1;
    public long mCaptureStartTime;
    /* access modifiers changed from: private */
    public ContentResolver mContentResolver;
    private boolean mContinuousFocusSupported;
    private String mCropValue;
    private String mCurrTouchAfAec;
    /* access modifiers changed from: private */
    public Uri mDebugUri;
    /* access modifiers changed from: private */
    public boolean mDepthSuccess = false;
    /* access modifiers changed from: private */
    public int mDisplayOrientation;
    private int mDisplayRotation;
    /* access modifiers changed from: private */
    public DrawAutoHDR mDrawAutoHDR;
    private final CameraErrorCallback mErrorCallback;
    public boolean mFaceDetectionEnabled = false;
    private boolean mFaceDetectionStarted = false;
    /* access modifiers changed from: private */
    public boolean mFirstTimeInitialized;
    private boolean mFocusAreaSupported;
    /* access modifiers changed from: private */
    public FocusOverlayManager mFocusManager;
    /* access modifiers changed from: private */
    public long mFocusStartTime;
    private float[] mGData;
    /* access modifiers changed from: private */
    public GraphView mGraphView;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public int mHeading;
    public boolean mHiston = false;
    private IdleHandler mIdleHandler;
    private Parameters mInitialParams;
    /* access modifiers changed from: private */
    public boolean mInstantCaptureSnapShot = false;
    /* access modifiers changed from: private */
    public boolean mIsBokehMode = false;
    /* access modifiers changed from: private */
    public boolean mIsImageCaptureIntent;
    public long mJpegCallbackFinishTime;
    private int mJpegFileSizeEstimation;
    /* access modifiers changed from: private */
    public byte[] mJpegImageData;
    /* access modifiers changed from: private */
    public long mJpegPictureCallbackTime;
    /* access modifiers changed from: private */
    public int mJpegRotation;
    /* access modifiers changed from: private */
    public byte[] mLastJpegData;
    /* access modifiers changed from: private */
    public int mLastJpegOrientation = 0;
    /* access modifiers changed from: private */
    public boolean mLastPhotoTakenWithRefocus = false;
    private LocationManager mLocationManager;
    private boolean mLocationPromptTriggered = false;
    /* access modifiers changed from: private */
    public int mLongShotCaptureCount;
    /* access modifiers changed from: private */
    public int mLongShotCaptureCountLimit;
    /* access modifiers changed from: private */
    public boolean mLongshotActive = false;
    /* access modifiers changed from: private */
    public boolean mLongshotSave = false;
    private float[] mMData;
    private int mManual3AEnabled;
    private final MetaDataCallback mMetaDataCallback;
    private boolean mMeteringAreaSupported;
    private boolean mMirror;
    /* access modifiers changed from: private */
    public NamedImages mNamedImages;
    /* access modifiers changed from: private */
    public OnMediaSavedListener mOnMediaSavedListener;
    private long mOnResumeTime;
    /* access modifiers changed from: private */
    public boolean mOpenCameraFail;
    private OpenCameraThread mOpenCameraThread = null;
    private int mOrientation = -1;
    /* access modifiers changed from: private */
    public Parameters mParameters;
    /* access modifiers changed from: private */
    public boolean mPaused;
    protected int mPendingSwitchCameraId = -1;
    public long mPictureDisplayedToJpegCallbackTime;
    /* access modifiers changed from: private */
    public final PostViewPictureCallback mPostViewPictureCallback = new PostViewPictureCallback();
    /* access modifiers changed from: private */
    public long mPostViewPictureCallbackTime;
    /* access modifiers changed from: private */
    public PreferenceGroup mPreferenceGroup;
    /* access modifiers changed from: private */
    public ComboPreferences mPreferences;
    /* access modifiers changed from: private */
    public String mPrevSavedCDS;
    /* access modifiers changed from: private */
    public boolean mPreviewRestartSupport = false;
    /* access modifiers changed from: private */
    public boolean mQuickCapture;

    /* renamed from: mR */
    private float[] f74mR;
    /* access modifiers changed from: private */
    public final RawPictureCallback mRawPictureCallback = new RawPictureCallback();
    /* access modifiers changed from: private */
    public long mRawPictureCallbackTime;
    /* access modifiers changed from: private */
    public int mReceivedSnapNum = 0;
    /* access modifiers changed from: private */
    public boolean mRefocus = false;
    /* access modifiers changed from: private */
    public int mRefocusSound;
    private int mRemainingPhotos;
    private boolean mRestartPreview = false;
    private View mRootView;
    /* access modifiers changed from: private */
    public boolean mSaveBokehXmp = false;
    private Uri mSaveUri;
    private String mSavedFlashMode;
    /* access modifiers changed from: private */
    public String mSceneMode;
    private OnSeekBarChangeListener mSeekListener;
    private SensorManager mSensorManager;
    /* access modifiers changed from: private */
    public long mShutterCallbackTime;
    public long mShutterLag;
    public long mShutterToPictureDisplayedTime;
    /* access modifiers changed from: private */
    public int mSnapshotMode;
    private boolean mSnapshotOnIdle = false;
    /* access modifiers changed from: private */
    public SoundPool mSoundPool;
    private final StatsCallback mStatsCallback;
    private boolean mTouchAfAecFlag;
    /* access modifiers changed from: private */
    public PhotoUI mUI;
    private int mUpdateSet;
    private int[] mZoomIdxTbl;
    private int mZoomValue;
    private int mbrightness = 3;
    private int mbrightness_step = 1;
    /* access modifiers changed from: private */
    public MediaSaveNotifyThread mediaSaveNotifyThread;
    /* access modifiers changed from: private */
    public SelfieThread selfieThread;

    private final class AutoFocusCallback implements CameraAFCallback {
        private AutoFocusCallback() {
        }

        public void onAutoFocus(boolean z, CameraProxy cameraProxy) {
            if (!PhotoModule.this.mPaused) {
                PhotoModule.this.mAutoFocusTime = System.currentTimeMillis() - PhotoModule.this.mFocusStartTime;
                StringBuilder sb = new StringBuilder();
                sb.append("mAutoFocusTime = ");
                sb.append(PhotoModule.this.mAutoFocusTime);
                sb.append("ms");
                Log.v(PhotoModule.TAG, sb.toString());
                int access$3200 = PhotoModule.this.mCameraState;
                if (!(access$3200 == 3 || access$3200 == 5)) {
                    PhotoModule.this.setCameraState(1);
                }
                PhotoModule.this.mFocusManager.onAutoFocus(z, PhotoModule.this.mUI.isShutterPressed());
            }
        }
    }

    @TargetApi(16)
    private final class AutoFocusMoveCallback implements CameraAFMoveCallback {
        private AutoFocusMoveCallback() {
        }

        public void onAutoFocusMoving(boolean z, CameraProxy cameraProxy) {
            PhotoModule.this.mFocusManager.onAutoFocusMoving(z);
        }
    }

    private final class JpegPictureCallback implements CameraPictureCallback {
        public static final int GDEPTH_SIZE = 1228800;
        byte[] mBokeh;
        int mCallTime = 0;
        byte[] mDepth;
        Location mLocation;
        byte[] mOrigin;

        public JpegPictureCallback(Location location) {
            this.mLocation = location;
        }

        /* JADX WARNING: Removed duplicated region for block: B:114:0x036e  */
        /* JADX WARNING: Removed duplicated region for block: B:115:0x0370  */
        /* JADX WARNING: Removed duplicated region for block: B:118:0x0376  */
        /* JADX WARNING: Removed duplicated region for block: B:119:0x0379  */
        /* JADX WARNING: Removed duplicated region for block: B:122:0x0383  */
        /* JADX WARNING: Removed duplicated region for block: B:127:0x039f  */
        /* JADX WARNING: Removed duplicated region for block: B:128:0x03a8  */
        /* JADX WARNING: Removed duplicated region for block: B:163:0x0543  */
        /* JADX WARNING: Removed duplicated region for block: B:172:0x0580  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onPictureTaken(byte[] r29, com.android.camera.CameraManager.CameraProxy r30) {
            /*
                r28 = this;
                r0 = r28
                r1 = r29
                int r2 = r0.mCallTime
                r3 = 1
                int r2 = r2 + r3
                r0.mCallTime = r2
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mIsBokehMode
                r4 = 2
                r5 = 3
                if (r2 == 0) goto L_0x0040
                boolean r2 = com.android.camera.PhotoModule.PERSIST_BOKEH_DEBUG_CHECK
                if (r2 != 0) goto L_0x0040
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mSaveBokehXmp
                if (r2 == 0) goto L_0x0040
                if (r1 == 0) goto L_0x002a
                int r2 = r0.mCallTime
                if (r2 != r3) goto L_0x002a
                r0.mBokeh = r1
            L_0x002a:
                if (r1 == 0) goto L_0x0036
                int r2 = r0.mCallTime
                if (r2 != r4) goto L_0x0036
                byte[] r2 = r0.mOrigin
                if (r2 != 0) goto L_0x0036
                r0.mOrigin = r1
            L_0x0036:
                if (r1 == 0) goto L_0x0040
                int r2 = r0.mCallTime
                if (r2 != r5) goto L_0x0040
                r0.mDepth = r1
                byte[] r1 = r0.mBokeh
            L_0x0040:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoUI r2 = r2.mUI
                r2.stopSelfieFlash()
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoUI r2 = r2.mUI
                r2.enableShutter(r3)
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoUI r2 = r2.mUI
                boolean r2 = r2.isPreviewCoverVisible()
                if (r2 == 0) goto L_0x0067
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoUI r2 = r2.mUI
                r2.hidePreviewCover()
            L_0x0067:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mInstantCaptureSnapShot
                java.lang.String r6 = "CAM_PhotoModule"
                r7 = 0
                if (r2 != r3) goto L_0x007c
                java.lang.String r2 = "Instant capture picture taken!"
                android.util.Log.v(r6, r2)
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                r2.mInstantCaptureSnapShot = r7
            L_0x007c:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mPaused
                if (r2 == 0) goto L_0x0085
                return
            L_0x0085:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mIsImageCaptureIntent
                if (r2 == 0) goto L_0x009b
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mRefocus
                if (r2 != 0) goto L_0x00b7
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                r2.stopPreview()
                goto L_0x00b7
            L_0x009b:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                java.lang.String r2 = r2.mSceneMode
                java.lang.String r8 = "hdr"
                if (r2 != r8) goto L_0x00b7
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoUI r2 = r2.mUI
                r2.showSwitcher()
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoUI r2 = r2.mUI
                r2.setSwipingEnabled(r3)
            L_0x00b7:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                int r8 = r2.mReceivedSnapNum
                int r8 = r8 + r3
                r2.mReceivedSnapNum = r8
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                long r8 = java.lang.System.currentTimeMillis()
                r2.mJpegPictureCallbackTime = r8
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                int r2 = r2.mSnapshotMode
                int r8 = org.codeaurora.snapcam.wrapper.CameraInfoWrapper.CAMERA_SUPPORT_MODE_ZSL
                if (r2 != r8) goto L_0x00f5
                java.lang.String r2 = "JpegPictureCallback : in zslmode"
                android.util.Log.v(r6, r2)
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.CameraManager$CameraProxy r8 = r2.mCameraDevice
                android.hardware.Camera$Parameters r8 = r8.getParameters()
                r2.mParameters = r8
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                android.hardware.Camera$Parameters r8 = r2.mParameters
                java.lang.String r9 = "num-snaps-per-shutter"
                int r8 = r8.getInt(r9)
                r2.mBurstSnapNum = r8
            L_0x00f5:
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r8 = "JpegPictureCallback: Received = "
                r2.append(r8)
                com.android.camera.PhotoModule r8 = com.android.camera.PhotoModule.this
                int r8 = r8.mReceivedSnapNum
                r2.append(r8)
                java.lang.String r8 = "Burst count = "
                r2.append(r8)
                com.android.camera.PhotoModule r8 = com.android.camera.PhotoModule.this
                int r8 = r8.mBurstSnapNum
                r2.append(r8)
                java.lang.String r2 = r2.toString()
                android.util.Log.v(r6, r2)
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                long r8 = r2.mPostViewPictureCallbackTime
                r10 = 0
                int r2 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1))
                if (r2 == 0) goto L_0x0148
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                long r8 = r2.mPostViewPictureCallbackTime
                com.android.camera.PhotoModule r12 = com.android.camera.PhotoModule.this
                long r12 = r12.mShutterCallbackTime
                long r8 = r8 - r12
                r2.mShutterToPictureDisplayedTime = r8
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                long r8 = r2.mJpegPictureCallbackTime
                com.android.camera.PhotoModule r12 = com.android.camera.PhotoModule.this
                long r12 = r12.mPostViewPictureCallbackTime
                long r8 = r8 - r12
                r2.mPictureDisplayedToJpegCallbackTime = r8
                goto L_0x0166
            L_0x0148:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                long r8 = r2.mRawPictureCallbackTime
                com.android.camera.PhotoModule r12 = com.android.camera.PhotoModule.this
                long r12 = r12.mShutterCallbackTime
                long r8 = r8 - r12
                r2.mShutterToPictureDisplayedTime = r8
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                long r8 = r2.mJpegPictureCallbackTime
                com.android.camera.PhotoModule r12 = com.android.camera.PhotoModule.this
                long r12 = r12.mRawPictureCallbackTime
                long r8 = r8 - r12
                r2.mPictureDisplayedToJpegCallbackTime = r8
            L_0x0166:
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r8 = "mPictureDisplayedToJpegCallbackTime = "
                r2.append(r8)
                com.android.camera.PhotoModule r8 = com.android.camera.PhotoModule.this
                long r8 = r8.mPictureDisplayedToJpegCallbackTime
                r2.append(r8)
                java.lang.String r8 = "ms"
                r2.append(r8)
                java.lang.String r2 = r2.toString()
                android.util.Log.v(r6, r2)
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.FocusOverlayManager r2 = r2.mFocusManager
                r2.updateFocusUI()
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mIsImageCaptureIntent
                r9 = 5
                if (r2 != 0) goto L_0x01bf
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mPreviewRestartSupport
                if (r2 != 0) goto L_0x01bf
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                int r2 = r2.mCameraState
                if (r2 == r9) goto L_0x01bf
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                int r2 = r2.mSnapshotMode
                int r12 = org.codeaurora.snapcam.wrapper.CameraInfoWrapper.CAMERA_SUPPORT_MODE_ZSL
                if (r2 == r12) goto L_0x01bf
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                int r2 = r2.mReceivedSnapNum
                com.android.camera.PhotoModule r12 = com.android.camera.PhotoModule.this
                int r12 = r12.mBurstSnapNum
                if (r2 != r12) goto L_0x01bf
                r2 = r3
                goto L_0x01c0
            L_0x01bf:
                r2 = r7
            L_0x01c0:
                java.lang.String r12 = "continuous-picture"
                if (r2 == 0) goto L_0x01e3
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                r2.setupPreview()
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.FocusOverlayManager r2 = r2.mFocusManager
                java.lang.String r2 = r2.getFocusMode()
                boolean r2 = r12.equals(r2)
                if (r2 == 0) goto L_0x0236
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.CameraManager$CameraProxy r2 = r2.mCameraDevice
                r2.cancelAutoFocus()
                goto L_0x0236
            L_0x01e3:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                int r2 = r2.mReceivedSnapNum
                com.android.camera.PhotoModule r13 = com.android.camera.PhotoModule.this
                int r13 = r13.mBurstSnapNum
                if (r2 != r13) goto L_0x0236
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                int r2 = r2.mCameraState
                if (r2 == r9) goto L_0x0236
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.FocusOverlayManager r2 = r2.mFocusManager
                r2.resetTouchFocus()
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.FocusOverlayManager r2 = r2.mFocusManager
                java.lang.String r2 = r2.getFocusMode()
                boolean r2 = r12.equals(r2)
                if (r2 == 0) goto L_0x021b
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.CameraManager$CameraProxy r2 = r2.mCameraDevice
                r2.cancelAutoFocus()
            L_0x021b:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoUI r2 = r2.mUI
                r2.resumeFaceDetection()
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mIsImageCaptureIntent
                if (r2 != 0) goto L_0x0231
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                r2.setCameraState(r3)
            L_0x0231:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                r2.startFaceDetection()
            L_0x0236:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r12 = r2.mRefocus
                r2.mLastPhotoTakenWithRefocus = r12
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mRefocus
                if (r2 == 0) goto L_0x0273
                java.lang.String r12 = "00.jpg"
                java.lang.String r13 = "01.jpg"
                java.lang.String r14 = "02.jpg"
                java.lang.String r15 = "03.jpg"
                java.lang.String r16 = "04.jpg"
                java.lang.String r17 = "DepthMapImage.y"
                java.lang.String r18 = "AllFocusImage.jpg"
                java.lang.String[] r2 = new java.lang.String[]{r12, r13, r14, r15, r16, r17, r18}
                com.android.camera.PhotoModule r12 = com.android.camera.PhotoModule.this     // Catch:{ Exception -> 0x0273 }
                com.android.camera.CameraActivity r12 = r12.mActivity     // Catch:{ Exception -> 0x0273 }
                com.android.camera.PhotoModule r13 = com.android.camera.PhotoModule.this     // Catch:{ Exception -> 0x0273 }
                int r13 = r13.mReceivedSnapNum     // Catch:{ Exception -> 0x0273 }
                int r13 = r13 - r3
                r2 = r2[r13]     // Catch:{ Exception -> 0x0273 }
                java.io.FileOutputStream r2 = r12.openFileOutput(r2, r7)     // Catch:{ Exception -> 0x0273 }
                int r12 = r1.length     // Catch:{ Exception -> 0x0273 }
                r2.write(r1, r7, r12)     // Catch:{ Exception -> 0x0273 }
                r2.close()     // Catch:{ Exception -> 0x0273 }
            L_0x0273:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mRefocus
                r12 = 7
                if (r2 == 0) goto L_0x028c
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mRefocus
                if (r2 == 0) goto L_0x065c
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                int r2 = r2.mReceivedSnapNum
                if (r2 != r12) goto L_0x065c
            L_0x028c:
                com.android.camera.exif.ExifInterface r2 = com.android.camera.Exif.getExif(r1)
                int r15 = com.android.camera.Exif.getOrientation(r2)
                com.android.camera.PhotoModule r13 = com.android.camera.PhotoModule.this
                int r13 = r13.mCameraId
                com.android.camera.CameraHolder r14 = com.android.camera.CameraHolder.instance()
                int r14 = r14.getFrontCameraId()
                if (r13 != r14) goto L_0x02e2
                com.android.camera.PhotoModule r13 = com.android.camera.PhotoModule.this
                com.android.camera.PreferenceGroup r13 = r13.mPreferenceGroup
                java.lang.String r14 = "pref_camera_selfiemirror_key"
                com.android.camera.ListPreference r13 = r13.findPreference(r14)
                com.android.camera.IconListPreference r13 = (com.android.camera.IconListPreference) r13
                if (r13 == 0) goto L_0x02e2
                java.lang.String r14 = r13.getValue()
                if (r14 == 0) goto L_0x02e2
                java.lang.String r13 = r13.getValue()
                java.lang.String r14 = "enable"
                boolean r13 = r13.equalsIgnoreCase(r14)
                if (r13 == 0) goto L_0x02e2
                com.android.camera.CameraHolder r13 = com.android.camera.CameraHolder.instance()
                com.android.camera.CameraHolder$CameraInfo[] r13 = r13.getCameraInfo()
                com.android.camera.PhotoModule r14 = com.android.camera.PhotoModule.this
                int r14 = r14.mCameraId
                r13 = r13[r14]
                com.android.camera.PhotoModule r14 = com.android.camera.PhotoModule.this
                int r13 = r13.orientation
                byte[] r1 = r14.flipJpeg(r1, r13, r15)
                byte[] r1 = com.android.camera.PhotoModule.addExifTags(r1, r15)
            L_0x02e2:
                com.android.camera.PhotoModule r13 = com.android.camera.PhotoModule.this
                boolean r13 = r13.mIsImageCaptureIntent
                if (r13 != 0) goto L_0x058b
                com.android.camera.PhotoModule r13 = com.android.camera.PhotoModule.this
                int r13 = r13.mReceivedSnapNum
                if (r13 <= r3) goto L_0x0303
                com.android.camera.PhotoModule r13 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoModule$NamedImages r13 = r13.mNamedImages
                com.android.camera.PhotoModule r14 = com.android.camera.PhotoModule.this
                long r10 = r14.mCaptureStartTime
                boolean r14 = r14.mRefocus
                r13.nameNewImage(r10, r14)
            L_0x0303:
                com.android.camera.PhotoModule r10 = com.android.camera.PhotoModule.this
                android.hardware.Camera$Parameters r10 = r10.mParameters
                android.hardware.Camera$Size r10 = r10.getPictureSize()
                com.android.camera.PhotoModule r11 = com.android.camera.PhotoModule.this
                int r11 = r11.mJpegRotation
                int r11 = r11 + r15
                int r11 = r11 % 180
                if (r11 != 0) goto L_0x031d
                int r11 = r10.width
                int r10 = r10.height
                goto L_0x0321
            L_0x031d:
                int r11 = r10.height
                int r10 = r10.width
            L_0x0321:
                com.android.camera.PhotoModule r13 = com.android.camera.PhotoModule.this
                android.hardware.Camera$Parameters r13 = r13.mParameters
                java.lang.String r14 = "picture-format"
                java.lang.String r13 = r13.get(r14)
                if (r13 == 0) goto L_0x0360
                java.lang.String r4 = "jpeg"
                boolean r4 = r13.equalsIgnoreCase(r4)
                if (r4 != 0) goto L_0x0360
                com.android.camera.PhotoModule r4 = com.android.camera.PhotoModule.this
                android.hardware.Camera$Parameters r4 = r4.mParameters
                java.lang.String r13 = "raw-size"
                java.lang.String r4 = r4.get(r13)
                if (r4 == 0) goto L_0x0360
                r13 = 120(0x78, float:1.68E-43)
                int r13 = r4.indexOf(r13)
                r9 = -1
                if (r13 == r9) goto L_0x0360
                java.lang.String r9 = r4.substring(r7, r13)
                int r9 = java.lang.Integer.parseInt(r9)
                int r13 = r13 + r3
                java.lang.String r3 = r4.substring(r13)
                int r3 = java.lang.Integer.parseInt(r3)
                goto L_0x0362
            L_0x0360:
                r3 = r10
                r9 = r11
            L_0x0362:
                com.android.camera.PhotoModule r4 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoModule$NamedImages r4 = r4.mNamedImages
                com.android.camera.PhotoModule$NamedImages$NamedEntity r4 = r4.getNextNameEntity()
                if (r4 != 0) goto L_0x0370
                r10 = 0
                goto L_0x0372
            L_0x0370:
                java.lang.String r10 = r4.title
            L_0x0372:
                r16 = -1
                if (r4 != 0) goto L_0x0379
                r12 = r16
                goto L_0x037b
            L_0x0379:
                long r12 = r4.date
            L_0x037b:
                com.android.camera.PhotoModule r4 = com.android.camera.PhotoModule.this
                android.net.Uri r4 = r4.mDebugUri
                if (r4 == 0) goto L_0x039c
                com.android.camera.PhotoModule r4 = com.android.camera.PhotoModule.this
                r4.saveToDebugUri(r1)
                if (r10 == 0) goto L_0x039c
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                r4.<init>()
                java.lang.String r11 = "DEBUG_"
                r4.append(r11)
                r4.append(r10)
                java.lang.String r4 = r4.toString()
                goto L_0x039d
            L_0x039c:
                r4 = r10
            L_0x039d:
                if (r4 != 0) goto L_0x03a8
                java.lang.String r2 = "Unbalanced name/data pair"
                android.util.Log.e(r6, r2)
                r5 = r8
                r10 = r15
                goto L_0x053a
            L_0x03a8:
                int r10 = (r12 > r16 ? 1 : (r12 == r16 ? 0 : -1))
                if (r10 != 0) goto L_0x03b1
                com.android.camera.PhotoModule r10 = com.android.camera.PhotoModule.this
                long r10 = r10.mCaptureStartTime
                goto L_0x03b2
            L_0x03b1:
                r10 = r12
            L_0x03b2:
                com.android.camera.PhotoModule r12 = com.android.camera.PhotoModule.this
                int r12 = r12.mHeading
                if (r12 < 0) goto L_0x03e2
                int r12 = com.android.camera.exif.ExifInterface.TAG_GPS_IMG_DIRECTION_REF
                java.lang.String r13 = "M"
                com.android.camera.exif.ExifTag r12 = r2.buildTag(r12, r13)
                int r13 = com.android.camera.exif.ExifInterface.TAG_GPS_IMG_DIRECTION
                com.android.camera.exif.Rational r7 = new com.android.camera.exif.Rational
                com.android.camera.PhotoModule r5 = com.android.camera.PhotoModule.this
                int r5 = r5.mHeading
                r18 = r10
                long r10 = (long) r5
                r5 = r8
                r21 = r9
                r8 = 1
                r7.<init>(r10, r8)
                com.android.camera.exif.ExifTag r7 = r2.buildTag(r13, r7)
                r2.setTag(r12)
                r2.setTag(r7)
                goto L_0x03e7
            L_0x03e2:
                r5 = r8
                r21 = r9
                r18 = r10
            L_0x03e7:
                com.android.camera.PhotoModule r7 = com.android.camera.PhotoModule.this
                android.hardware.Camera$Parameters r7 = r7.mParameters
                java.lang.String r7 = r7.get(r14)
                java.lang.StringBuilder r8 = new java.lang.StringBuilder
                r8.<init>()
                java.lang.String r9 = "capture:"
                r8.append(r9)
                r8.append(r4)
                java.lang.String r9 = "."
                r8.append(r9)
                r8.append(r7)
                java.lang.String r8 = r8.toString()
                android.util.Log.d(r6, r8)
                com.android.camera.PhotoModule r8 = com.android.camera.PhotoModule.this
                boolean r8 = r8.mIsBokehMode
                if (r8 == 0) goto L_0x04ec
                boolean r8 = com.android.camera.PhotoModule.PERSIST_BOKEH_DEBUG_CHECK
                if (r8 != 0) goto L_0x048b
                com.android.camera.PhotoModule r8 = com.android.camera.PhotoModule.this
                boolean r8 = r8.mSaveBokehXmp
                if (r8 == 0) goto L_0x048b
                if (r1 == 0) goto L_0x0488
                int r8 = r0.mCallTime
                r9 = 3
                if (r8 != r9) goto L_0x0488
                byte[] r8 = r0.mOrigin
                if (r8 == 0) goto L_0x0488
                byte[] r9 = r0.mBokeh
                if (r9 == 0) goto L_0x0488
                org.codeaurora.snapcam.filter.GImage r9 = new org.codeaurora.snapcam.filter.GImage
                java.lang.String r10 = "image/jpeg"
                r9.<init>(r8, r10)
                byte[] r8 = r0.mDepth
                org.codeaurora.snapcam.filter.GDepth r8 = org.codeaurora.snapcam.filter.GDepth.createGDepth(r8)
                android.graphics.Rect r10 = new android.graphics.Rect
                r11 = r21
                r12 = 0
                r10.<init>(r12, r12, r11, r3)
                r8.setRoi(r10)
                com.android.camera.PhotoModule r10 = com.android.camera.PhotoModule.this
                com.android.camera.CameraActivity r10 = r10.mActivity
                com.android.camera.MediaSaveService r13 = r10.getMediaSaveService()
                byte[] r14 = r0.mBokeh
                java.lang.StringBuilder r10 = new java.lang.StringBuilder
                r10.<init>()
                java.lang.String r12 = "bokeh_"
                r10.append(r12)
                r10.append(r4)
                java.lang.String r17 = r10.toString()
                android.location.Location r4 = r0.mLocation
                com.android.camera.PhotoModule r10 = com.android.camera.PhotoModule.this
                com.android.camera.MediaSaveService$OnMediaSavedListener r25 = r10.mOnMediaSavedListener
                com.android.camera.PhotoModule r10 = com.android.camera.PhotoModule.this
                android.content.ContentResolver r26 = r10.mContentResolver
                r10 = r15
                r15 = r9
                r16 = r8
                r20 = r4
                r22 = r3
                r23 = r10
                r24 = r2
                r27 = r7
                r13.addXmpImage(r14, r15, r16, r17, r18, r20, r21, r22, r23, r24, r25, r26, r27)
                goto L_0x051a
            L_0x0488:
                r10 = r15
                goto L_0x051a
            L_0x048b:
                r10 = r15
                r11 = r21
                int r8 = r0.mCallTime
                r9 = 3
                if (r8 != r9) goto L_0x04c0
                com.android.camera.PhotoModule r8 = com.android.camera.PhotoModule.this
                com.android.camera.CameraActivity r8 = r8.mActivity
                com.android.camera.MediaSaveService r13 = r8.getMediaSaveService()
                byte[] r14 = r0.mDepth
                android.location.Location r8 = r0.mLocation
                com.android.camera.PhotoModule r9 = com.android.camera.PhotoModule.this
                com.android.camera.MediaSaveService$OnMediaSavedListener r23 = r9.mOnMediaSavedListener
                com.android.camera.PhotoModule r9 = com.android.camera.PhotoModule.this
                android.content.ContentResolver r24 = r9.mContentResolver
                r15 = r4
                r16 = r18
                r18 = r8
                r19 = r11
                r20 = r3
                r21 = r10
                r22 = r2
                r25 = r7
                r13.addImage(r14, r15, r16, r18, r19, r20, r21, r22, r23, r24, r25)
                goto L_0x051a
            L_0x04c0:
                com.android.camera.PhotoModule r8 = com.android.camera.PhotoModule.this
                com.android.camera.CameraActivity r8 = r8.mActivity
                com.android.camera.MediaSaveService r13 = r8.getMediaSaveService()
                android.location.Location r8 = r0.mLocation
                com.android.camera.PhotoModule r9 = com.android.camera.PhotoModule.this
                com.android.camera.MediaSaveService$OnMediaSavedListener r23 = r9.mOnMediaSavedListener
                com.android.camera.PhotoModule r9 = com.android.camera.PhotoModule.this
                android.content.ContentResolver r24 = r9.mContentResolver
                r14 = r1
                r15 = r4
                r16 = r18
                r18 = r8
                r19 = r11
                r20 = r3
                r21 = r10
                r22 = r2
                r25 = r7
                r13.addImage(r14, r15, r16, r18, r19, r20, r21, r22, r23, r24, r25)
                goto L_0x051a
            L_0x04ec:
                r10 = r15
                r11 = r21
                com.android.camera.PhotoModule r8 = com.android.camera.PhotoModule.this
                com.android.camera.CameraActivity r8 = r8.mActivity
                com.android.camera.MediaSaveService r13 = r8.getMediaSaveService()
                android.location.Location r8 = r0.mLocation
                com.android.camera.PhotoModule r9 = com.android.camera.PhotoModule.this
                com.android.camera.MediaSaveService$OnMediaSavedListener r23 = r9.mOnMediaSavedListener
                com.android.camera.PhotoModule r9 = com.android.camera.PhotoModule.this
                android.content.ContentResolver r24 = r9.mContentResolver
                r14 = r1
                r15 = r4
                r16 = r18
                r18 = r8
                r19 = r11
                r20 = r3
                r21 = r10
                r22 = r2
                r25 = r7
                r13.addImage(r14, r15, r16, r18, r19, r20, r21, r22, r23, r24, r25)
            L_0x051a:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mRefocus
                if (r2 == 0) goto L_0x053a
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                int r2 = r2.mReceivedSnapNum
                r3 = 7
                if (r2 != r3) goto L_0x053a
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoUI r2 = r2.mUI
                com.android.camera.PhotoModule r3 = com.android.camera.PhotoModule.this
                boolean r3 = r3.mRefocus
                r2.showRefocusToast(r3)
            L_0x053a:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                int r2 = r2.mCameraState
                r3 = 5
                if (r2 == r3) goto L_0x0580
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                android.hardware.Camera$Parameters r2 = r2.mParameters
                android.hardware.Camera$Size r2 = r2.getPictureSize()
                int r3 = r2.width
                r4 = 352(0x160, float:4.93E-43)
                if (r3 > r4) goto L_0x0564
                int r2 = r2.height
                r3 = 288(0x120, float:4.04E-43)
                if (r2 > r3) goto L_0x0564
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoUI r2 = r2.mUI
                r3 = 2
                r2.setDownFactor(r3)
                goto L_0x056e
            L_0x0564:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoUI r2 = r2.mUI
                r3 = 4
                r2.setDownFactor(r3)
            L_0x056e:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mAnimateCapture
                if (r2 == 0) goto L_0x05af
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoUI r2 = r2.mUI
                r2.animateCapture(r1)
                goto L_0x05af
            L_0x0580:
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                r2.mLastJpegData = r1
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this
                r1.mLastJpegOrientation = r10
                goto L_0x05af
            L_0x058b:
                r5 = r8
                r10 = r15
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                r2.stopPreview()
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                r2.mJpegImageData = r1
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                boolean r2 = r2.mQuickCapture
                if (r2 != 0) goto L_0x05aa
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoUI r2 = r2.mUI
                r3 = 0
                r2.showCapturedImageForReview(r1, r10, r3)
                goto L_0x05af
            L_0x05aa:
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this
                r1.onCaptureDone()
            L_0x05af:
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this
                boolean r1 = r1.mLongshotActive
                if (r1 != 0) goto L_0x05c6
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this
                com.android.camera.CameraActivity r1 = r1.mActivity
                com.android.camera.PhotoModule$JpegPictureCallback$1 r2 = new com.android.camera.PhotoModule$JpegPictureCallback$1
                r2.<init>()
                r1.updateStorageSpaceAndHint(r2)
                goto L_0x05d5
            L_0x05c6:
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this
                com.android.camera.PhotoUI r1 = r1.mUI
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                int r2 = com.android.camera.PhotoModule.access$8106(r2)
                r1.updateRemainingPhotos(r2)
            L_0x05d5:
                long r1 = java.lang.System.currentTimeMillis()
                com.android.camera.PhotoModule r3 = com.android.camera.PhotoModule.this
                long r7 = r3.mJpegPictureCallbackTime
                long r1 = r1 - r7
                r3.mJpegCallbackFinishTime = r1
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "mJpegCallbackFinishTime = "
                r1.append(r2)
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                long r2 = r2.mJpegCallbackFinishTime
                r1.append(r2)
                r1.append(r5)
                java.lang.String r1 = r1.toString()
                android.util.Log.v(r6, r1)
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this
                int r1 = r1.mReceivedSnapNum
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                int r2 = r2.mBurstSnapNum
                if (r1 != r2) goto L_0x0612
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this
                r2 = 0
                r1.mJpegPictureCallbackTime = r2
            L_0x0612:
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this
                boolean r2 = r1.mHiston
                if (r2 == 0) goto L_0x062e
                int r1 = r1.mSnapshotMode
                int r2 = org.codeaurora.snapcam.wrapper.CameraInfoWrapper.CAMERA_SUPPORT_MODE_ZSL
                if (r1 != r2) goto L_0x062e
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this
                com.android.camera.CameraActivity r1 = r1.mActivity
                com.android.camera.PhotoModule$JpegPictureCallback$2 r2 = new com.android.camera.PhotoModule$JpegPictureCallback$2
                r2.<init>()
                r1.runOnUiThread(r2)
            L_0x062e:
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this
                int r1 = r1.mSnapshotMode
                int r2 = org.codeaurora.snapcam.wrapper.CameraInfoWrapper.CAMERA_SUPPORT_MODE_ZSL
                if (r1 != r2) goto L_0x065c
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this
                int r1 = r1.mCameraState
                r2 = 5
                if (r1 == r2) goto L_0x065c
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this
                int r1 = r1.mReceivedSnapNum
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                int r2 = r2.mBurstSnapNum
                if (r1 != r2) goto L_0x065c
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this
                boolean r1 = r1.mIsImageCaptureIntent
                if (r1 != 0) goto L_0x065c
                com.android.camera.PhotoModule r0 = com.android.camera.PhotoModule.this
                r0.cancelAutoFocus()
            L_0x065c:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.PhotoModule.JpegPictureCallback.onPictureTaken(byte[], com.android.camera.CameraManager$CameraProxy):void");
        }
    }

    private final class LongshotPictureCallback implements CameraPictureCallback {
        Location mLocation;

        public LongshotPictureCallback(Location location) {
            this.mLocation = location;
        }

        public void onPictureTaken(byte[] bArr, CameraProxy cameraProxy) {
            String str;
            long j;
            if (!PhotoModule.this.mPaused) {
                PhotoModule.this.mFocusManager.updateFocusUI();
                String str2 = new String(bArr);
                PhotoModule.this.mNamedImages.nameNewImage(PhotoModule.this.mCaptureStartTime);
                NamedEntity nextNameEntity = PhotoModule.this.mNamedImages.getNextNameEntity();
                if (nextNameEntity == null) {
                    str = null;
                } else {
                    str = nextNameEntity.title;
                }
                String str3 = str;
                if (nextNameEntity == null) {
                    j = -1;
                } else {
                    j = nextNameEntity.date;
                }
                String str4 = PhotoModule.TAG;
                if (str3 == null) {
                    Log.e(str4, "Unbalanced name/data pair");
                } else if (j == -1) {
                    Log.e(str4, "Invalid filename date");
                } else {
                    String str5 = Storage.DIRECTORY;
                    Environment.getExternalStorageDirectory();
                    if (new File(str2).renameTo(new File(str5))) {
                        Size pictureSize = PhotoModule.this.mParameters.getPictureSize();
                        String str6 = PhotoModule.this.mParameters.get(PhotoModule.KEY_PICTURE_FORMAT);
                        StringBuilder sb = new StringBuilder();
                        sb.append("capture:");
                        sb.append(str3);
                        sb.append(".");
                        sb.append(str6);
                        Log.d(str4, sb.toString());
                        PhotoModule.this.mActivity.getMediaSaveService().addImage(null, str3, j, this.mLocation, pictureSize.width, pictureSize.height, 0, null, PhotoModule.this.mOnMediaSavedListener, PhotoModule.this.mContentResolver, str6);
                    } else {
                        Log.e(str4, "Failed to move jpeg file");
                    }
                }
            }
        }
    }

    private final class LongshotShutterCallback implements CameraShutterCallback {
        private LongshotShutterCallback() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:22:0x00eb, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x00ed, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onShutter(com.android.camera.CameraManager.CameraProxy r9) {
            /*
                r8 = this;
                com.android.camera.PhotoModule r9 = com.android.camera.PhotoModule.this
                long r0 = java.lang.System.currentTimeMillis()
                r9.mShutterCallbackTime = r0
                com.android.camera.PhotoModule r9 = com.android.camera.PhotoModule.this
                long r0 = r9.mShutterCallbackTime
                com.android.camera.PhotoModule r2 = com.android.camera.PhotoModule.this
                long r2 = r2.mCaptureStartTime
                long r0 = r0 - r2
                r9.mShutterLag = r0
                java.lang.StringBuilder r9 = new java.lang.StringBuilder
                r9.<init>()
                java.lang.String r0 = "[KPI Perf] PROFILE_SHUTTER_LAG mShutterLag = "
                r9.append(r0)
                com.android.camera.PhotoModule r0 = com.android.camera.PhotoModule.this
                long r0 = r0.mShutterLag
                r9.append(r0)
                java.lang.String r0 = "ms"
                r9.append(r0)
                java.lang.String r9 = r9.toString()
                java.lang.String r0 = "CAM_PhotoModule"
                android.util.Log.e(r0, r9)
                com.android.camera.PhotoModule r9 = com.android.camera.PhotoModule.this
                com.android.camera.CameraManager$CameraProxy r9 = r9.mCameraDevice
                monitor-enter(r9)
                com.android.camera.PhotoModule r0 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                int r0 = r0.mCameraState     // Catch:{ all -> 0x00ee }
                r1 = 5
                if (r0 != r1) goto L_0x00ec
                com.android.camera.PhotoModule r0 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                boolean r0 = r0.mLongshotActive     // Catch:{ all -> 0x00ee }
                if (r0 != 0) goto L_0x004f
                goto L_0x00ec
            L_0x004f:
                com.android.camera.PhotoModule r0 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                boolean r0 = r0.isLongshotNeedCancel()     // Catch:{ all -> 0x00ee }
                if (r0 == 0) goto L_0x0059
                monitor-exit(r9)     // Catch:{ all -> 0x00ee }
                return
            L_0x0059:
                com.android.camera.PhotoModule r0 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                int r0 = r0.mLongShotCaptureCount     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                int r1 = r1.mLongShotCaptureCountLimit     // Catch:{ all -> 0x00ee }
                if (r0 != r1) goto L_0x006f
                com.android.camera.PhotoModule r8 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                r0 = 0
                r8.mLongshotActive = r0     // Catch:{ all -> 0x00ee }
                monitor-exit(r9)     // Catch:{ all -> 0x00ee }
                return
            L_0x006f:
                com.android.camera.PhotoModule r0 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoUI r0 = r0.mUI     // Catch:{ all -> 0x00ee }
                r0.doShutterAnimation()     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r0 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                android.hardware.Camera$Parameters r1 = r1.mParameters     // Catch:{ all -> 0x00ee }
                java.lang.String r2 = "picture-format"
                java.lang.String r1 = r1.get(r2)     // Catch:{ all -> 0x00ee }
                android.location.Location r0 = r0.getLocationAccordPictureFormat(r1)     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                r1.mLongShotCaptureCount = r1.mLongShotCaptureCount + 1     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                boolean r1 = r1.mLongshotSave     // Catch:{ all -> 0x00ee }
                if (r1 == 0) goto L_0x00c1
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                com.android.camera.CameraManager$CameraProxy r2 = r1.mCameraDevice     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                android.os.Handler r3 = r1.mHandler     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule$LongshotShutterCallback r4 = new com.android.camera.PhotoModule$LongshotShutterCallback     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                r4.<init>()     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule$RawPictureCallback r5 = r1.mRawPictureCallback     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule$PostViewPictureCallback r6 = r1.mPostViewPictureCallback     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule$LongshotPictureCallback r7 = new com.android.camera.PhotoModule$LongshotPictureCallback     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r8 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                r7.<init>(r0)     // Catch:{ all -> 0x00ee }
                r2.takePicture(r3, r4, r5, r6, r7)     // Catch:{ all -> 0x00ee }
                goto L_0x00ea
            L_0x00c1:
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                com.android.camera.CameraManager$CameraProxy r2 = r1.mCameraDevice     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                android.os.Handler r3 = r1.mHandler     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule$LongshotShutterCallback r4 = new com.android.camera.PhotoModule$LongshotShutterCallback     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                r4.<init>()     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule$RawPictureCallback r5 = r1.mRawPictureCallback     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r1 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule$PostViewPictureCallback r6 = r1.mPostViewPictureCallback     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule$JpegPictureCallback r7 = new com.android.camera.PhotoModule$JpegPictureCallback     // Catch:{ all -> 0x00ee }
                com.android.camera.PhotoModule r8 = com.android.camera.PhotoModule.this     // Catch:{ all -> 0x00ee }
                r7.<init>(r0)     // Catch:{ all -> 0x00ee }
                r2.takePicture(r3, r4, r5, r6, r7)     // Catch:{ all -> 0x00ee }
            L_0x00ea:
                monitor-exit(r9)     // Catch:{ all -> 0x00ee }
                return
            L_0x00ec:
                monitor-exit(r9)     // Catch:{ all -> 0x00ee }
                return
            L_0x00ee:
                r8 = move-exception
                monitor-exit(r9)     // Catch:{ all -> 0x00ee }
                throw r8
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.PhotoModule.LongshotShutterCallback.onShutter(com.android.camera.CameraManager$CameraProxy):void");
        }
    }

    private class MainHandler extends Handler {
        public MainHandler() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    PhotoModule.this.setupPreview();
                    return;
                case 2:
                    PhotoModule.this.initializeFirstTime();
                    return;
                case 3:
                    PhotoModule.this.mActivity.getWindow().clearFlags(128);
                    return;
                case 4:
                    PhotoModule.this.setCameraParametersWhenIdle(0);
                    return;
                case 5:
                    PhotoModule.this.showTapToFocusToast();
                    return;
                case 6:
                    PhotoModule.this.switchCamera();
                    return;
                case 8:
                    PhotoModule.this.onCameraOpened();
                    return;
                case 9:
                    PhotoModule.this.mOpenCameraFail = true;
                    CameraUtil.showErrorAndFinish(PhotoModule.this.mActivity, C0905R.string.cannot_connect_camera);
                    return;
                case 10:
                    PhotoModule.this.mCameraDisabled = true;
                    CameraUtil.showErrorAndFinish(PhotoModule.this.mActivity, C0905R.string.camera_disabled);
                    return;
                case 11:
                    PhotoModule.this.setCameraParametersWhenIdle(4);
                    PhotoModule.this.mUI.updateOnScreenIndicators(PhotoModule.this.mParameters, PhotoModule.this.mPreferenceGroup, PhotoModule.this.mPreferences);
                    return;
                case 12:
                    PhotoModule.this.mActivity.onModuleSelected(4);
                    return;
                case 13:
                    PhotoModule.this.onPreviewStarted();
                    return;
                case 14:
                    PhotoModule.this.onShutterButtonClick();
                    return;
                default:
                    return;
            }
        }
    }

    private class MediaSaveNotifyThread extends Thread {
        /* access modifiers changed from: private */
        public Uri uri;

        public MediaSaveNotifyThread(Uri uri2) {
            this.uri = uri2;
        }

        public void setUri(Uri uri2) {
            this.uri = uri2;
        }

        public void run() {
            while (PhotoModule.this.mLongshotActive) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException unused) {
                }
            }
            PhotoModule.this.mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    if (MediaSaveNotifyThread.this.uri != null) {
                        PhotoModule.this.mActivity.notifyNewMedia(MediaSaveNotifyThread.this.uri);
                    }
                    PhotoModule.this.mActivity.updateStorageSpaceAndHint();
                    PhotoModule.this.updateRemainingPhotos();
                }
            });
            PhotoModule.this.mediaSaveNotifyThread = null;
        }
    }

    private final class MetaDataCallback implements CameraMetaDataCallback {
        private static final int QCAMERA_METADATA_HDR = 3;
        private static final int QCAMERA_METADATA_RTB = 5;
        private int mLastMessage;

        private MetaDataCallback() {
            this.mLastMessage = -1;
        }

        public void onCameraMetaData(byte[] bArr, Camera camera) {
            final String str;
            int[] iArr = new int[3];
            if (bArr.length >= 12) {
                boolean z = false;
                for (int i = 0; i < 3; i++) {
                    iArr[i] = byteToInt(bArr, i * 4);
                }
                if (iArr[0] == 3) {
                    if (iArr[2] == 1) {
                        PhotoModule photoModule = PhotoModule.this;
                        photoModule.mAutoHdrEnable = true;
                        photoModule.mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                if (PhotoModule.this.mDrawAutoHDR != null) {
                                    PhotoModule.this.mDrawAutoHDR.AutoHDR();
                                }
                            }
                        });
                        return;
                    }
                    PhotoModule photoModule2 = PhotoModule.this;
                    photoModule2.mAutoHdrEnable = false;
                    photoModule2.mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (PhotoModule.this.mDrawAutoHDR != null) {
                                PhotoModule.this.mDrawAutoHDR.AutoHDR();
                            }
                        }
                    });
                } else if (iArr[0] == 5) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("QCAMERA_METADATA_RTB msgtype =");
                    sb.append(iArr[2]);
                    Log.d(PhotoModule.TAG, sb.toString());
                    int i2 = iArr[2];
                    if (i2 == 0) {
                        str = "NO depth effect";
                    } else if (i2 == 1) {
                        str = "Depth effect success";
                    } else if (i2 == 2) {
                        str = "Too near";
                    } else if (i2 == 3) {
                        str = "Too far";
                    } else if (i2 == 4) {
                        str = "Low light";
                    } else if (i2 != 5) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("Message type =");
                        sb2.append(iArr[2]);
                        str = sb2.toString();
                    } else {
                        str = "Object not found";
                    }
                    PhotoModule photoModule3 = PhotoModule.this;
                    if (iArr[2] == 1) {
                        z = true;
                    }
                    photoModule3.mDepthSuccess = z;
                    PhotoModule.this.mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (PhotoModule.this.mBokehTipText != null) {
                                if (!PhotoModule.this.mDepthSuccess) {
                                    PhotoModule.this.mBokehTipText.setVisibility(0);
                                    PhotoModule.this.mBokehTipText.setText(str);
                                } else {
                                    PhotoModule.this.mBokehTipText.setVisibility(8);
                                }
                            }
                            PhotoModule.this.mUI.enableBokehFocus(PhotoModule.this.mDepthSuccess);
                        }
                    });
                }
            }
        }

        private int byteToInt(byte[] bArr, int i) {
            int i2 = 0;
            for (int i3 = 0; i3 < 4; i3++) {
                int i4 = 3 - i3;
                i2 += (bArr[i4 + i] & 255) << (i4 * 8);
            }
            return i2;
        }
    }

    public static class NamedImages {
        /* access modifiers changed from: private */
        public Vector<NamedEntity> mQueue = new Vector<>();

        public static class NamedEntity {
            public long date;
            public String title;
        }

        public void nameNewImage(long j) {
            NamedEntity namedEntity = new NamedEntity();
            namedEntity.title = CameraUtil.createJpegName(j);
            if (namedEntity.title != null) {
                SettingsManager instance = SettingsManager.getInstance();
                if ("18".equals(instance.getValue(SettingsManager.KEY_SCENE_MODE))) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(namedEntity.title);
                    sb.append("_HDR");
                    namedEntity.title = sb.toString();
                } else {
                    if (!"0".equals(instance.getValue(SettingsManager.KEY_MAKEUP))) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(namedEntity.title);
                        sb2.append("_BEAUTY");
                        namedEntity.title = sb2.toString();
                    }
                }
            }
            namedEntity.date = j;
            this.mQueue.add(namedEntity);
        }

        public void nameNewImage(long j, boolean z) {
            NamedEntity namedEntity = new NamedEntity();
            namedEntity.title = CameraUtil.createJpegName(j, z);
            namedEntity.date = j;
            this.mQueue.add(namedEntity);
        }

        public NamedEntity getNextNameEntity() {
            synchronized (this.mQueue) {
                if (this.mQueue.isEmpty()) {
                    return null;
                }
                NamedEntity namedEntity = (NamedEntity) this.mQueue.remove(0);
                return namedEntity;
            }
        }
    }

    private class OpenCameraThread extends Thread {
        private OpenCameraThread() {
        }

        public void run() {
            PhotoModule.this.openCamera();
            PhotoModule.this.startPreview();
        }
    }

    private final class PostViewPictureCallback implements CameraPictureCallback {
        private PostViewPictureCallback() {
        }

        public void onPictureTaken(byte[] bArr, CameraProxy cameraProxy) {
            PhotoModule.this.mPostViewPictureCallbackTime = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            sb.append("mShutterToPostViewCallbackTime = ");
            sb.append(PhotoModule.this.mPostViewPictureCallbackTime - PhotoModule.this.mShutterCallbackTime);
            sb.append("ms");
            Log.v(PhotoModule.TAG, sb.toString());
        }
    }

    private final class RawPictureCallback implements CameraPictureCallback {
        private RawPictureCallback() {
        }

        public void onPictureTaken(byte[] bArr, CameraProxy cameraProxy) {
            PhotoModule.this.mRawPictureCallbackTime = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            sb.append("mShutterToRawCallbackTime = ");
            sb.append(PhotoModule.this.mRawPictureCallbackTime - PhotoModule.this.mShutterCallbackTime);
            sb.append("ms");
            Log.v(PhotoModule.TAG, sb.toString());
        }
    }

    private class SelfieThread extends Thread {
        private SelfieThread() {
        }

        public void run() {
            try {
                Thread.sleep(680);
                PhotoModule.this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        PhotoModule.this.mFocusManager.doSnap();
                    }
                });
            } catch (InterruptedException unused) {
            }
            PhotoModule.this.selfieThread = null;
        }
    }

    private final class ShutterCallback implements CameraShutterCallback {
        private boolean mNeedsAnimation;

        public ShutterCallback(boolean z) {
            this.mNeedsAnimation = z;
        }

        public void onShutter(CameraProxy cameraProxy) {
            PhotoModule.this.mShutterCallbackTime = System.currentTimeMillis();
            PhotoModule photoModule = PhotoModule.this;
            photoModule.mShutterLag = photoModule.mShutterCallbackTime - PhotoModule.this.mCaptureStartTime;
            StringBuilder sb = new StringBuilder();
            sb.append("[KPI Perf] PROFILE_SHUTTER_LAG mShutterLag = ");
            sb.append(PhotoModule.this.mShutterLag);
            sb.append("ms");
            Log.e(PhotoModule.TAG, sb.toString());
            if (this.mNeedsAnimation) {
                PhotoModule.this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        PhotoModule.this.animateAfterShutter();
                    }
                });
            }
            if (PhotoModule.this.mRefocus && PhotoModule.this.isShutterSoundOn()) {
                PhotoModule.this.mSoundPool.play(PhotoModule.this.mRefocusSound, 1.0f, 1.0f, 0, 0, 1.0f);
            }
        }
    }

    private final class StatsCallback implements CameraDataCallback {
        private StatsCallback() {
        }

        public void onCameraData(int[] iArr, Camera camera) {
            PhotoModule photoModule = PhotoModule.this;
            if (photoModule.mHiston && photoModule.mFirstTimeInitialized) {
                synchronized (PhotoModule.statsdata) {
                    System.arraycopy(iArr, 0, PhotoModule.statsdata, 0, 257);
                }
                PhotoModule.this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (PhotoModule.this.mGraphView != null) {
                            PhotoModule.this.mGraphView.PreviewChanged();
                        }
                    }
                });
            }
        }
    }

    public void installIntentFilter() {
    }

    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void onCaptureTextureCopied() {
    }

    public void onDestroy() {
    }

    public void onRestorePreferencesClicked() {
    }

    public void onStop() {
    }

    public void onStorageNotEnoughRecordingVideo() {
    }

    public PhotoModule() {
        this.mAutoFocusMoveCallback = ApiHelper.HAS_AUTO_FOCUS_MOVE_CALLBACK ? new AutoFocusMoveCallback() : null;
        this.mErrorCallback = new CameraErrorCallback();
        this.mStatsCallback = new StatsCallback();
        this.mMetaDataCallback = new MetaDataCallback();
        this.mCurrTouchAfAec = ParametersWrapper.TOUCH_AF_AEC_ON;
        this.mSavedFlashMode = null;
        this.mHandler = new MainHandler();
        this.mIdleHandler = null;
        this.mGData = new float[3];
        this.mMData = new float[3];
        this.f74mR = new float[16];
        this.mHeading = -1;
        this.mZoomIdxTbl = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        this.mCameraPreviewParamsReady = false;
        this.mManual3AEnabled = 0;
        this.mAnimateCapture = true;
        this.mJpegFileSizeEstimation = 0;
        this.mRemainingPhotos = -1;
        this.mOnMediaSavedListener = new OnMediaSavedListener() {
            public void onMediaSaved(Uri uri) {
                if (PhotoModule.this.mLongshotActive) {
                    if (PhotoModule.this.mediaSaveNotifyThread == null) {
                        PhotoModule photoModule = PhotoModule.this;
                        photoModule.mediaSaveNotifyThread = new MediaSaveNotifyThread(uri);
                        PhotoModule.this.mediaSaveNotifyThread.start();
                        return;
                    }
                    PhotoModule.this.mediaSaveNotifyThread.setUri(uri);
                } else if (uri != null) {
                    PhotoModule.this.mActivity.notifyNewMedia(uri);
                }
            }
        };
        this.mSeekListener = new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
        this.mBlurDegreeListener = new OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (PhotoModule.this.mParameters != null) {
                    PhotoModule.this.mParameters.set(CameraSettings.KEY_QC_BOKEH_BLUR_VALUE, i);
                    PhotoModule.this.mCameraDevice.setParameters(PhotoModule.this.mParameters);
                    StringBuilder sb = new StringBuilder();
                    sb.append("seekbar bokeh degree = ");
                    sb.append(i);
                    Log.d(PhotoModule.TAG, sb.toString());
                    PhotoModule.this.mUI.setBokehRenderDegree(i);
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Editor edit = PreferenceManager.getDefaultSharedPreferences(PhotoModule.this.mActivity).edit();
                edit.putInt(CameraSettings.KEY_BOKEH_BLUR_VALUE, seekBar.getProgress());
                edit.apply();
            }
        };
    }

    static /* synthetic */ int access$8106(PhotoModule photoModule) {
        int i = photoModule.mRemainingPhotos - 1;
        photoModule.mRemainingPhotos = i;
        return i;
    }

    static /* synthetic */ int access$8976(PhotoModule photoModule, int i) {
        int i2 = i | photoModule.mManual3AEnabled;
        photoModule.mManual3AEnabled = i2;
        return i2;
    }

    /* access modifiers changed from: private */
    public void checkDisplayRotation() {
        if (CameraUtil.getDisplayRotation(this.mActivity) != this.mDisplayRotation) {
            setDisplayOrientation();
        }
        if (SystemClock.uptimeMillis() - this.mOnResumeTime < 5000) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    PhotoModule.this.checkDisplayRotation();
                }
            }, 100);
        }
    }

    public Parameters getParameters() {
        return this.mParameters;
    }

    public void reinit() {
        this.mPreferences = ComboPreferences.get(this.mActivity);
        if (this.mPreferences == null) {
            this.mPreferences = new ComboPreferences(this.mActivity);
        }
        CameraSettings.upgradeGlobalPreferences(this.mPreferences.getGlobal(), this.mActivity);
        this.mCameraId = getPreferredCameraId(this.mPreferences);
        this.mPreferences.setLocalId(this.mActivity, this.mCameraId);
        CameraSettings.upgradeLocalPreferences(this.mPreferences.getLocal());
    }

    public void init(CameraActivity cameraActivity, View view) {
        this.mActivity = cameraActivity;
        this.mRootView = view;
        this.mPreferences = ComboPreferences.get(this.mActivity);
        if (this.mPreferences == null) {
            this.mPreferences = new ComboPreferences(this.mActivity);
        }
        CameraSettings.upgradeGlobalPreferences(this.mPreferences.getGlobal(), cameraActivity);
        this.mCameraId = getPreferredCameraId(this.mPreferences);
        this.mContentResolver = this.mActivity.getContentResolver();
        this.mIsImageCaptureIntent = isImageCaptureIntent();
        this.mPreferences.setLocalId(this.mActivity, this.mCameraId);
        CameraSettings.upgradeLocalPreferences(this.mPreferences.getLocal());
        this.mUI = new PhotoUI(cameraActivity, this, view);
        if (this.mOpenCameraThread == null) {
            this.mOpenCameraThread = new OpenCameraThread();
            this.mOpenCameraThread.start();
        }
        initializeControlByIntent();
        this.mQuickCapture = this.mActivity.getIntent().getBooleanExtra(EXTRA_QUICK_CAPTURE, false);
        this.mLocationManager = new LocationManager(this.mActivity, this);
        this.mSensorManager = (SensorManager) this.mActivity.getSystemService("sensor");
        this.brightnessProgressBar = (ProgressBar) this.mRootView.findViewById(C0905R.C0907id.progress);
        this.mBokehTipText = (TextView) this.mRootView.findViewById(C0905R.C0907id.bokeh_tip_text);
        ProgressBar progressBar = this.brightnessProgressBar;
        if (progressBar instanceof SeekBar) {
            ((SeekBar) progressBar).setOnSeekBarChangeListener(this.mSeekListener);
        }
        this.brightnessProgressBar.setMax(6);
        this.mbrightness = this.mPreferences.getInt(CameraSettings.KEY_BRIGHTNESS, 3);
        this.brightnessProgressBar.setProgress(this.mbrightness);
        this.brightnessProgressBar.setVisibility(4);
        Storage.setSaveSDCard(this.mPreferences.getString("pref_camera_savepath_key", "0").equals("1"));
    }

    private void initializeControlByIntent() {
        this.mUI.initializeControlByIntent();
        if (this.mIsImageCaptureIntent) {
            setupCaptureParams();
        }
    }

    /* access modifiers changed from: private */
    public void onPreviewStarted() {
        if (this.mCameraState != 3) {
            setCameraState(1);
            this.mFocusManager.onPreviewStarted();
            startFaceDetection();
            locationFirstRun();
            this.mUI.enableShutter(true);
        }
    }

    private void locationFirstRun() {
        boolean z = this.mActivity.checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == 0;
        if ((!RecordLocationPreference.isSet(this.mPreferences, "pref_camera_recordlocation_key") || !z) && !this.mActivity.isSecureCamera() && !this.mLocationPromptTriggered && CameraHolder.instance().getBackCameraId() != -1) {
            this.mLocationPromptTriggered = true;
            enableRecordingLocation(z);
        }
    }

    public void waitingLocationPermissionResult(boolean z) {
        this.mLocationManager.waitingLocationPermissionResult(z);
    }

    public void enableRecordingLocation(boolean z) {
        setLocationPreference(z ? RecordLocationPreference.VALUE_ON : "off");
        this.mLocationManager.recordLocation(z);
    }

    public void setPreferenceForTest(String str, String str2) {
        this.mUI.setPreference(str, str2);
        onSharedPreferenceChanged();
    }

    public void onPreviewUIReady() {
        if (!this.mPaused && this.mCameraDevice != null) {
            Log.v(TAG, "onPreviewUIReady");
            if (this.mCameraState == 0) {
                startPreview();
            } else {
                synchronized (this.mCameraDevice) {
                    SurfaceHolder surfaceHolder = this.mUI.getSurfaceHolder();
                    if (surfaceHolder == null) {
                        Log.w(TAG, "startPreview: holder for preview are not ready.");
                        return;
                    }
                    this.mCameraDevice.setPreviewDisplay(surfaceHolder);
                }
            }
        }
    }

    public void onPreviewUIDestroyed() {
        if (this.mCameraDevice != null) {
            try {
                if (this.mOpenCameraThread != null) {
                    this.mOpenCameraThread.join();
                    this.mOpenCameraThread = null;
                }
            } catch (InterruptedException unused) {
            }
            stopPreview();
        }
    }

    private void setLocationPreference(String str) {
        this.mPreferences.edit().putString("pref_camera_recordlocation_key", str).apply();
        if (this.mUI.mMenuInitialized) {
            onSharedPreferenceChanged();
        }
    }

    /* access modifiers changed from: private */
    public void onCameraOpened() {
        if (!this.mPaused) {
            Log.v(TAG, "onCameraOpened");
            openCameraCommon();
            resizeForPreviewAspectRatio();
            updateFocusManager(this.mUI);
        }
    }

    /* access modifiers changed from: private */
    public void switchCamera() {
        if (!this.mPaused) {
            this.mUI.applySurfaceChange(SURFACE_STATUS.HIDE);
            StringBuilder sb = new StringBuilder();
            sb.append("Start to switch camera. id=");
            sb.append(this.mPendingSwitchCameraId);
            String sb2 = sb.toString();
            String str = TAG;
            Log.v(str, sb2);
            this.mCameraId = this.mPendingSwitchCameraId;
            this.mPendingSwitchCameraId = -1;
            this.mSnapshotOnIdle = false;
            setCameraId(this.mCameraId);
            try {
                if (this.mOpenCameraThread != null) {
                    this.mOpenCameraThread.join();
                    this.mOpenCameraThread = null;
                }
            } catch (InterruptedException unused) {
            }
            closeCamera();
            this.mUI.collapseCameraControls();
            this.mUI.clearFaces();
            FocusOverlayManager focusOverlayManager = this.mFocusManager;
            if (focusOverlayManager != null) {
                focusOverlayManager.removeMessages();
            }
            this.mPreferences.setLocalId(this.mActivity, this.mCameraId);
            CameraSettings.upgradeLocalPreferences(this.mPreferences.getLocal());
            CameraActivity cameraActivity = this.mActivity;
            this.mCameraDevice = CameraUtil.openCamera(cameraActivity, this.mCameraId, this.mHandler, cameraActivity.getCameraOpenErrorCallback());
            CameraProxy cameraProxy = this.mCameraDevice;
            if (cameraProxy == null) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Failed to open camera:");
                sb3.append(this.mCameraId);
                sb3.append(", aborting.");
                Log.e(str, sb3.toString());
                return;
            }
            this.mParameters = cameraProxy.getParameters();
            this.mInitialParams = this.mCameraDevice.getParameters();
            initializeCapabilities();
            this.mMirror = CameraHolder.instance().getCameraInfo()[this.mCameraId].facing == 0;
            this.mFocusManager.setMirror(this.mMirror);
            this.mFocusManager.setParameters(this.mInitialParams);
            setupPreview();
            this.mUI.applySurfaceChange(SURFACE_STATUS.SURFACE_VIEW);
            this.mZoomValue = 0;
            resizeForPreviewAspectRatio();
            openCameraCommon();
            this.mHandler.sendEmptyMessage(7);
        }
    }

    /* access modifiers changed from: protected */
    public void setCameraId(int i) {
        ListPreference findPreference = this.mPreferenceGroup.findPreference(CameraSettings.KEY_CAMERA_ID);
        StringBuilder sb = new StringBuilder();
        sb.append(BuildConfig.FLAVOR);
        sb.append(i);
        findPreference.setValue(sb.toString());
    }

    private void openCameraCommon() {
        loadCameraPreferences();
        this.mUI.onCameraOpened(this.mPreferenceGroup, this.mPreferences, this.mParameters, this, this);
        if (this.mIsImageCaptureIntent) {
            this.mUI.overrideSettings(CameraSettings.KEY_CAMERA_HDR_PLUS, this.mActivity.getString(C0905R.string.setting_off_value));
        }
        updateCameraSettings();
        showTapToFocusToastIfNeeded();
        resetManual3ASettings();
        resetMiscSettings();
    }

    public void onScreenSizeChanged(int i, int i2) {
        FocusOverlayManager focusOverlayManager = this.mFocusManager;
        if (focusOverlayManager != null) {
            focusOverlayManager.setPreviewSize(i, i2);
        }
    }

    public void onPreviewRectChanged(Rect rect) {
        FocusOverlayManager focusOverlayManager = this.mFocusManager;
        if (focusOverlayManager != null) {
            focusOverlayManager.setPreviewRect(rect);
        }
    }

    private void resetExposureCompensation() {
        String str = "pref_camera_exposure_key";
        String str2 = "0";
        if (!str2.equals(this.mPreferences.getString(str, str2))) {
            Editor edit = this.mPreferences.edit();
            edit.putString(str, str2);
            edit.apply();
        }
    }

    private void resetManual3ASettings() {
        String string = this.mActivity.getString(C0905R.string.pref_camera_manual_exp_default);
        ComboPreferences comboPreferences = this.mPreferences;
        String str = CameraSettings.KEY_MANUAL_EXPOSURE;
        if (!comboPreferences.getString(str, string).equals(string)) {
            this.mUI.setPreference(str, string);
            UpdateManualExposureSettings();
        }
        String string2 = this.mActivity.getString(C0905R.string.pref_camera_manual_focus_default);
        ComboPreferences comboPreferences2 = this.mPreferences;
        String str2 = CameraSettings.KEY_MANUAL_FOCUS;
        if (!comboPreferences2.getString(str2, string2).equals(string2)) {
            this.mUI.setPreference(str2, string2);
            UpdateManualFocusSettings();
        }
        String string3 = this.mActivity.getString(C0905R.string.pref_camera_manual_wb_default);
        ComboPreferences comboPreferences3 = this.mPreferences;
        String str3 = CameraSettings.KEY_MANUAL_WB;
        if (!comboPreferences3.getString(str3, string3).equals(string3)) {
            this.mUI.setPreference(str3, string3);
            UpdateManualWBSettings();
        }
        this.mManual3AEnabled = 0;
    }

    private void resetMiscSettings() {
        if (PersistUtil.isDisableQcomMiscSetting()) {
            this.mUI.setPreference(CameraSettings.KEY_ZSL, ParametersWrapper.ZSL_OFF);
            this.mUI.setPreference("pref_camera_facedetection_key", ParametersWrapper.FACE_DETECTION_OFF);
            this.mUI.setPreference(CameraSettings.KEY_TOUCH_AF_AEC, ParametersWrapper.TOUCH_AF_AEC_OFF);
            this.mUI.setPreference(CameraSettings.KEY_FOCUS_MODE, "auto");
            this.mUI.setPreference(CameraSettings.KEY_FLASH_MODE, "off");
            this.mUI.setPreference(CameraSettings.KEY_DENOISE, ParametersWrapper.DENOISE_OFF);
            onSharedPreferenceChanged();
        }
    }

    /* access modifiers changed from: 0000 */
    public void setPreviewFrameLayoutCameraOrientation() {
        if (CameraHolder.instance().getCameraInfo()[this.mCameraId].orientation % 180 == 0) {
            this.mUI.cameraOrientationPreviewResize(true);
        } else {
            this.mUI.cameraOrientationPreviewResize(false);
        }
    }

    public void resizeForPreviewAspectRatio() {
        CameraProxy cameraProxy = this.mCameraDevice;
        String str = TAG;
        if (cameraProxy == null || this.mParameters == null) {
            Log.e(str, "Camera not yet initialized");
            return;
        }
        setPreviewFrameLayoutCameraOrientation();
        Size previewSize = this.mParameters.getPreviewSize();
        StringBuilder sb = new StringBuilder();
        sb.append("Using preview width = ");
        sb.append(previewSize.width);
        sb.append("& height = ");
        sb.append(previewSize.height);
        Log.i(str, sb.toString());
        this.mUI.setAspectRatio(((float) previewSize.width) / ((float) previewSize.height));
    }

    public void onSwitchSavePath() {
        PhotoUI photoUI = this.mUI;
        String str = "1";
        String str2 = "pref_camera_savepath_key";
        if (photoUI.mMenuInitialized) {
            photoUI.setPreference(str2, str);
        } else {
            this.mPreferences.edit().putString(str2, str).apply();
        }
        RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.on_switch_save_path_to_sdcard, 0).show();
    }

    /* access modifiers changed from: private */
    public void initializeFirstTime() {
        if (!this.mFirstTimeInitialized && !this.mPaused) {
            this.mLocationManager.recordLocation(RecordLocationPreference.get(this.mPreferences, "pref_camera_recordlocation_key"));
            this.mUI.initializeFirstTime();
            MediaSaveService mediaSaveService = this.mActivity.getMediaSaveService();
            if (mediaSaveService != null) {
                mediaSaveService.setListener(this);
            }
            this.mNamedImages = new NamedImages();
            this.mGraphView = (GraphView) this.mRootView.findViewById(C0905R.C0907id.graph_view);
            this.mDrawAutoHDR = (DrawAutoHDR) this.mRootView.findViewById(C0905R.C0907id.autohdr_view);
            GraphView graphView = this.mGraphView;
            String str = TAG;
            if (graphView == null || this.mDrawAutoHDR == null) {
                Log.e(str, "mGraphView or mDrawAutoHDR is null");
            } else {
                graphView.setPhotoModuleObject(this);
                this.mDrawAutoHDR.setPhotoModuleObject(this);
            }
            this.mFirstTimeInitialized = true;
            Log.d(str, "addIdleHandler in first time initialization");
            addIdleHandler();
        }
    }

    private void initializeSecondTime() {
        this.mLocationManager.recordLocation(RecordLocationPreference.get(this.mPreferences, "pref_camera_recordlocation_key"));
        MediaSaveService mediaSaveService = this.mActivity.getMediaSaveService();
        if (mediaSaveService != null) {
            mediaSaveService.setListener(this);
        }
        this.mNamedImages = new NamedImages();
        if (!this.mIsImageCaptureIntent) {
            this.mUI.showSwitcher();
        }
        this.mUI.initializeSecondTime(this.mParameters);
    }

    private void showTapToFocusToastIfNeeded() {
        if (this.mFocusAreaSupported && this.mPreferences.getBoolean(CameraSettings.KEY_CAMERA_FIRST_USE_HINT_SHOWN, true)) {
            this.mHandler.sendEmptyMessageDelayed(5, 1000);
        }
    }

    private void addIdleHandler() {
        if (this.mIdleHandler == null) {
            this.mIdleHandler = new IdleHandler() {
                public boolean queueIdle() {
                    Storage.ensureOSXCompatible();
                    return false;
                }
            };
            Looper.myQueue().addIdleHandler(this.mIdleHandler);
        }
    }

    private void removeIdleHandler() {
        if (this.mIdleHandler != null) {
            Looper.myQueue().removeIdleHandler(this.mIdleHandler);
            this.mIdleHandler = null;
        }
    }

    public void startFaceDetection() {
        if (this.mCameraDevice != null && this.mFaceDetectionEnabled && !this.mFaceDetectionStarted) {
            boolean z = true;
            if (this.mCameraState == 1 && this.mParameters.getMaxNumDetectedFaces() > 0) {
                this.mFaceDetectionStarted = true;
                CameraInfo cameraInfo = CameraHolder.instance().getCameraInfo()[this.mCameraId];
                PhotoUI photoUI = this.mUI;
                int i = this.mDisplayOrientation;
                if (cameraInfo.facing != 0) {
                    z = false;
                }
                photoUI.onStartFaceDetection(i, z);
                this.mCameraDevice.setFaceDetectionCallback(this.mHandler, this.mUI);
                this.mCameraDevice.startFaceDetection();
            }
        }
    }

    public void stopFaceDetection() {
        if (this.mFaceDetectionEnabled && this.mFaceDetectionStarted && this.mParameters.getMaxNumDetectedFaces() > 0) {
            this.mFaceDetectionStarted = false;
            this.mCameraDevice.setFaceDetectionCallback(null, null);
            this.mUI.pauseFaceDetection();
            this.mCameraDevice.stopFaceDetection();
            this.mUI.onStopFaceDetection();
        }
    }

    /* access modifiers changed from: private */
    public boolean isLongshotNeedCancel() {
        if (PERSIST_SKIP_MEM_CHECK) {
            return false;
        }
        int i = (Storage.getAvailableSpace() > Storage.LOW_STORAGE_THRESHOLD_BYTES ? 1 : (Storage.getAvailableSpace() == Storage.LOW_STORAGE_THRESHOLD_BYTES ? 0 : -1));
        String str = TAG;
        if (i <= 0) {
            Log.w(str, "current storage is full");
            return true;
        }
        if (this.SECONDARY_SERVER_MEM == 0) {
            ActivityManager activityManager = (ActivityManager) this.mActivity.getSystemService("activity");
            MemoryInfo memoryInfo = new MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            this.SECONDARY_SERVER_MEM = memoryInfo.secondaryServerThreshold;
        }
        long maxMemory = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
        MemInfoReader memInfoReader = new MemInfoReader();
        memInfoReader.readMemInfo();
        long[] rawInfo = memInfoReader.getRawInfo();
        if ((rawInfo[1] + rawInfo[3]) * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID > this.SECONDARY_SERVER_MEM && maxMemory > 41943040) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("cancel longshot: free=");
        sb.append(rawInfo[1] * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID);
        sb.append(" cached=");
        sb.append(rawInfo[3] * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID);
        sb.append(" threshold=");
        sb.append(this.SECONDARY_SERVER_MEM);
        Log.e(str, sb.toString());
        this.mLongshotActive = false;
        RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.msg_cancel_longshot_for_limited_memory, 0).show();
        return true;
    }

    /* access modifiers changed from: private */
    public byte[] flipJpeg(byte[] bArr, int i, int i2) {
        Bitmap decodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
        Matrix matrix = new Matrix();
        if (i == 270 || i == 90) {
            if (i2 == 0 || i2 == 180) {
                matrix.preScale(-1.0f, 1.0f);
            } else {
                matrix.preScale(1.0f, -1.0f);
            }
        }
        Bitmap createBitmap = Bitmap.createBitmap(decodeByteArray, 0, 0, decodeByteArray.getWidth(), decodeByteArray.getHeight(), matrix, false);
        createBitmap.setDensity(160);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(createBitmap.getWidth() * createBitmap.getHeight());
        createBitmap.compress(CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] addExifTags(byte[] bArr, int i) {
        ExifInterface exifInterface = new ExifInterface();
        exifInterface.addOrientationTag(i);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            exifInterface.writeExif(bArr, (OutputStream) byteArrayOutputStream);
        } catch (IOException e) {
            Log.e(TAG, "Could not write EXIF", e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /* access modifiers changed from: private */
    public void setCameraState(int i) {
        this.mCameraState = i;
        if (i != 0) {
            if (i == 1) {
                this.mUI.enableGestures(true);
                return;
            } else if (!(i == 3 || i == 4 || i == 5)) {
                return;
            }
        }
        this.mUI.enableGestures(false);
    }

    /* access modifiers changed from: private */
    public void animateAfterShutter() {
        if (!this.mIsImageCaptureIntent) {
            this.mUI.animateFlash();
        }
    }

    public boolean capture() {
        if (this.mCameraDevice != null) {
            int i = this.mCameraState;
            if (!(i == 3 || i == 4 || i == 0 || this.mActivity.getMediaSaveService() == null || this.mActivity.getMediaSaveService().isQueueFull())) {
                this.mCaptureStartTime = System.currentTimeMillis();
                this.mPostViewPictureCallbackTime = 0;
                this.mJpegImageData = null;
                boolean z = this.mSceneMode == CameraUtil.SCENE_MODE_HDR;
                if (this.mHiston) {
                    if (this.mSnapshotMode != CameraInfoWrapper.CAMERA_SUPPORT_MODE_ZSL) {
                        this.mHiston = false;
                        this.mCameraDevice.setHistogramMode(null);
                    }
                    this.mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (PhotoModule.this.mGraphView != null) {
                                PhotoModule.this.mGraphView.setVisibility(4);
                            }
                        }
                    });
                }
                if (z) {
                    animateAfterShutter();
                }
                if (this.mCameraState == 5) {
                    this.mCameraDevice.setLongshot(true);
                }
                this.mJpegRotation = CameraUtil.getJpegRotationForCamera1(this.mCameraId, this.mOrientation);
                String str = this.mParameters.get(KEY_PICTURE_FORMAT);
                Location locationAccordPictureFormat = getLocationAccordPictureFormat(str);
                synchronized (this.mCameraDevice) {
                    this.mParameters.setRotation(this.mJpegRotation);
                    CameraUtil.setGpsParameters(this.mParameters, locationAccordPictureFormat);
                    if (this.mRefocus) {
                        this.mParameters.set(CameraSettings.KEY_QC_LEGACY_BURST, 7);
                    } else {
                        this.mParameters.remove(CameraSettings.KEY_QC_LEGACY_BURST);
                    }
                    this.mFocusManager.setAeAwbLock(false);
                    setAutoExposureLockIfSupported();
                    setAutoWhiteBalanceLockIfSupported();
                    this.mCameraDevice.setParameters(this.mParameters);
                    this.mParameters = this.mCameraDevice.getParameters();
                }
                try {
                    this.mBurstSnapNum = this.mParameters.getInt("num-snaps-per-shutter");
                } catch (NumberFormatException unused) {
                    this.mBurstSnapNum = 1;
                }
                this.mReceivedSnapNum = 0;
                this.mPreviewRestartSupport = PersistUtil.isPreviewRestartEnabled();
                this.mPreviewRestartSupport &= CameraSettings.isInternalPreviewSupported(this.mParameters);
                this.mPreviewRestartSupport &= this.mBurstSnapNum == 1;
                this.mPreviewRestartSupport &= !CameraUtil.SCENE_MODE_HDR.equals(this.mSceneMode);
                this.mPreviewRestartSupport = PIXEL_FORMAT_JPEG.equalsIgnoreCase(str) & this.mPreviewRestartSupport;
                if (this.mCameraState != 5) {
                    this.mUI.enableShutter(false);
                }
                if (!isShutterSoundOn()) {
                    this.mCameraDevice.enableShutterSound(false);
                } else {
                    this.mCameraDevice.enableShutterSound(!this.mRefocus);
                }
                this.mSaveBokehXmp = this.mIsBokehMode && this.mDepthSuccess;
                if (this.mCameraState == 5) {
                    this.mLongShotCaptureCountLimit = SystemProperties.getInt("persist.sys.camera.longshot.shotnum", 0);
                    this.mLongShotCaptureCount = 1;
                    if (this.mLongshotSave) {
                        this.mCameraDevice.takePicture(this.mHandler, new LongshotShutterCallback(), this.mRawPictureCallback, this.mPostViewPictureCallback, new LongshotPictureCallback(locationAccordPictureFormat));
                    } else {
                        this.mCameraDevice.takePicture(this.mHandler, new LongshotShutterCallback(), this.mRawPictureCallback, this.mPostViewPictureCallback, new JpegPictureCallback(locationAccordPictureFormat));
                    }
                } else {
                    this.mCameraDevice.takePicture(this.mHandler, new ShutterCallback(!z), this.mRawPictureCallback, this.mPostViewPictureCallback, new JpegPictureCallback(locationAccordPictureFormat));
                    setCameraState(3);
                }
                this.mNamedImages.nameNewImage(this.mCaptureStartTime, this.mRefocus);
                if (this.mSnapshotMode != CameraInfoWrapper.CAMERA_SUPPORT_MODE_ZSL) {
                    this.mFaceDetectionStarted = false;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(((NamedEntity) this.mNamedImages.mQueue.lastElement()).title);
                sb.append(Storage.JPEG_POSTFIX);
                sb.toString();
                return true;
            }
        }
        return false;
    }

    public void setFocusParameters() {
        setCameraParameters(4);
    }

    /* access modifiers changed from: private */
    public Location getLocationAccordPictureFormat(String str) {
        if (str == null || !PIXEL_FORMAT_JPEG.equalsIgnoreCase(str)) {
            return null;
        }
        return this.mLocationManager.getCurrentLocation();
    }

    private int getPreferredCameraId(ComboPreferences comboPreferences) {
        int cameraFacingIntentExtras = CameraUtil.getCameraFacingIntentExtras(this.mActivity);
        if (cameraFacingIntentExtras != -1) {
            return cameraFacingIntentExtras;
        }
        return CameraSettings.readPreferredCameraId(comboPreferences);
    }

    /* access modifiers changed from: private */
    public void updateCommonManual3ASettings() {
        int i;
        String str = ParametersWrapper.TOUCH_AF_AEC_OFF;
        this.mSceneMode = "auto";
        String string = this.mActivity.getString(C0905R.string.pref_camera_redeyereduction_default);
        String string2 = this.mActivity.getString(C0905R.string.pref_camera_ae_bracket_hdr_default);
        String string3 = this.mActivity.getString(C0905R.string.pref_camera_coloreffect_default);
        String str2 = "pref_camera_longshot_key";
        if (this.mManual3AEnabled > 0) {
            overrideCameraSettings("off", null, null, "0", str, ParametersWrapper.getAutoExposure(this.mParameters), Integer.toString(ParametersWrapper.getSaturation(this.mParameters)), Integer.toString(ParametersWrapper.getContrast(this.mParameters)), Integer.toString(ParametersWrapper.getSharpness(this.mParameters)), string3, this.mSceneMode, string, string2);
            this.mUI.overrideSettings(str2, this.mActivity.getString(C0905R.string.setting_off_value));
        } else {
            overrideCameraSettings(null, null, null, null, this.mActivity.getString(C0905R.string.pref_camera_touchafaec_default), null, null, null, null, null, null, null, null);
            this.mUI.overrideSettings(str2, null);
        }
        if (ParametersWrapper.getISOValue(this.mParameters).equals("manual")) {
            ComboPreferences comboPreferences = this.mPreferences;
            String string4 = this.mActivity.getString(C0905R.string.pref_camera_iso_default);
            String str3 = CameraSettings.KEY_ISO;
            String string5 = comboPreferences.getString(str3, string4);
            i = 2;
            this.mUI.overrideSettings(str3, string5);
        } else {
            i = 2;
        }
        if ((this.mManual3AEnabled & i) != 0) {
            String str4 = "pref_camera_whitebalance_key";
            String string6 = this.mPreferences.getString(str4, this.mActivity.getString(C0905R.string.pref_camera_whitebalance_default));
            PhotoUI photoUI = this.mUI;
            String[] strArr = new String[i];
            strArr[0] = str4;
            strArr[1] = string6;
            photoUI.overrideSettings(strArr);
        }
        if ((this.mManual3AEnabled & 1) != 0) {
            PhotoUI photoUI2 = this.mUI;
            String[] strArr2 = new String[i];
            strArr2[0] = CameraSettings.KEY_FOCUS_MODE;
            strArr2[1] = this.mFocusManager.getFocusMode();
            photoUI2.overrideSettings(strArr2);
        }
    }

    /* JADX WARNING: type inference failed for: r15v4, types: [int] */
    /* JADX WARNING: type inference failed for: r15v5, types: [boolean] */
    /* JADX WARNING: type inference failed for: r3v4, types: [boolean, int] */
    /* JADX WARNING: type inference failed for: r3v6 */
    /* JADX WARNING: type inference failed for: r3v8 */
    /* JADX WARNING: type inference failed for: r15v6 */
    /* JADX WARNING: type inference failed for: r15v7 */
    /* JADX WARNING: type inference failed for: r15v8 */
    /* JADX WARNING: type inference failed for: r15v9 */
    /* JADX WARNING: type inference failed for: r15v10 */
    /* JADX WARNING: type inference failed for: r15v12 */
    /* JADX WARNING: type inference failed for: r15v13 */
    /* JADX WARNING: type inference failed for: r3v13 */
    /* JADX WARNING: type inference failed for: r3v14 */
    /* JADX WARNING: type inference failed for: r15v22 */
    /* JADX WARNING: type inference failed for: r15v23 */
    /* JADX WARNING: type inference failed for: r15v24 */
    /* JADX WARNING: type inference failed for: r15v25 */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x014d, code lost:
        if (r17.equals(r16) != false) goto L_0x014f;
     */
    /* JADX WARNING: Multi-variable type inference failed. Error: jadx.core.utils.exceptions.JadxRuntimeException: No candidate types for var: r15v5, types: [boolean]
      assigns: []
      uses: [?[int, short, byte, char], boolean]
      mth insns count: 395
    	at jadx.core.dex.visitors.typeinference.TypeSearch.fillTypeCandidates(TypeSearch.java:237)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    	at jadx.core.dex.visitors.typeinference.TypeSearch.run(TypeSearch.java:53)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runMultiVariableSearch(TypeInferenceVisitor.java:99)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:92)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
    	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
    	at jadx.core.ProcessClass.process(ProcessClass.java:30)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:49)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:49)
    	at jadx.core.ProcessClass.process(ProcessClass.java:35)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:311)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:217)
     */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x0340  */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x034d  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x0392  */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x03e3  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x01bd  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x024d  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x02c8  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x02f7  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0314  */
    /* JADX WARNING: Unknown variable types count: 8 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateCameraSettings() {
        /*
            r26 = this;
            r14 = r26
            com.android.camera.CameraActivity r0 = r14.mActivity
            r1 = 2131689954(0x7f0f01e2, float:1.9008938E38)
            r0.getString(r1)
            com.android.camera.CameraActivity r0 = r14.mActivity
            r1 = 2131690718(0x7f0f04de, float:1.9010488E38)
            java.lang.String r0 = r0.getString(r1)
            com.android.camera.CameraActivity r1 = r14.mActivity
            r2 = 2131689948(0x7f0f01dc, float:1.9008926E38)
            java.lang.String r1 = r1.getString(r2)
            com.android.camera.CameraActivity r2 = r14.mActivity
            r3 = 2131689941(0x7f0f01d5, float:1.9008912E38)
            java.lang.String r2 = r2.getString(r3)
            com.android.camera.CameraActivity r3 = r14.mActivity
            r4 = 2131689946(0x7f0f01da, float:1.9008922E38)
            java.lang.String r3 = r3.getString(r4)
            com.android.camera.CameraActivity r4 = r14.mActivity
            r5 = 2131689939(0x7f0f01d3, float:1.9008908E38)
            java.lang.String r4 = r4.getString(r5)
            com.android.camera.CameraActivity r5 = r14.mActivity
            r6 = 2131689952(0x7f0f01e0, float:1.9008934E38)
            java.lang.String r5 = r5.getString(r6)
            com.android.camera.CameraActivity r6 = r14.mActivity
            r7 = 2131689943(0x7f0f01d7, float:1.9008916E38)
            java.lang.String r6 = r6.getString(r7)
            android.hardware.Camera$Parameters r7 = r14.mParameters
            java.lang.String r8 = "opti-zoom"
            java.lang.String r7 = r7.get(r8)
            android.hardware.Camera$Parameters r8 = r14.mParameters
            java.lang.String r9 = "chroma-flash"
            java.lang.String r8 = r8.get(r9)
            android.hardware.Camera$Parameters r9 = r14.mParameters
            java.lang.String r10 = "af-bracket"
            r9.get(r10)
            android.hardware.Camera$Parameters r9 = r14.mParameters
            java.lang.String r10 = "FSSR"
            java.lang.String r9 = r9.get(r10)
            android.hardware.Camera$Parameters r10 = r14.mParameters
            java.lang.String r11 = "true-portrait"
            java.lang.String r10 = r10.get(r11)
            android.hardware.Camera$Parameters r11 = r14.mParameters
            java.lang.String r12 = "multi-touch-focus"
            java.lang.String r11 = r11.get(r12)
            com.android.camera.CameraActivity r12 = r14.mActivity
            r13 = 2131689950(0x7f0f01de, float:1.900893E38)
            java.lang.String r12 = r12.getString(r13)
            android.hardware.Camera$Parameters r13 = r14.mParameters
            java.lang.String r15 = "still-more"
            java.lang.String r13 = r13.get(r15)
            android.hardware.Camera$Parameters r15 = r14.mParameters
            r16 = r12
            java.lang.String r12 = "long-shot"
            java.lang.String r12 = r15.get(r12)
            int r15 = r14.mManual3AEnabled
            r17 = r13
            if (r15 <= 0) goto L_0x009b
            r15 = 1
            goto L_0x009c
        L_0x009b:
            r15 = 0
        L_0x009c:
            java.lang.String r13 = "pref_camera_pictureformat_key"
            r21 = r15
            if (r12 == 0) goto L_0x00c2
            boolean r0 = r12.equals(r0)
            if (r0 == 0) goto L_0x00c2
            com.android.camera.CameraActivity r0 = r14.mActivity
            r12 = 2131690272(0x7f0f0320, float:1.9009583E38)
            java.lang.String r0 = r0.getString(r12)
            com.android.camera.PhotoUI r12 = r14.mUI
            r22 = r5
            r15 = 2
            java.lang.String[] r5 = new java.lang.String[r15]
            r15 = 0
            r5[r15] = r13
            r15 = 1
            r5[r15] = r0
            r12.overrideSettings(r5)
            goto L_0x00ce
        L_0x00c2:
            r22 = r5
            com.android.camera.PhotoUI r0 = r14.mUI
            r5 = 0
            java.lang.String[] r12 = new java.lang.String[]{r13, r5}
            r0.overrideSettings(r12)
        L_0x00ce:
            android.hardware.Camera$Parameters r0 = r14.mParameters
            java.lang.String r5 = "re-focus"
            java.lang.String r0 = r0.get(r5)
            com.android.camera.FocusOverlayManager r5 = r14.mFocusManager
            boolean r5 = r5.isZslEnabled()
            if (r5 == 0) goto L_0x00f9
            com.android.camera.CameraActivity r5 = r14.mActivity
            r12 = 2131690272(0x7f0f0320, float:1.9009583E38)
            java.lang.String r5 = r5.getString(r12)
            com.android.camera.PhotoUI r12 = r14.mUI
            r20 = r10
            r15 = 2
            java.lang.String[] r10 = new java.lang.String[r15]
            r15 = 0
            r10[r15] = r13
            r18 = 1
            r10[r18] = r5
            r12.overrideSettings(r10)
            goto L_0x0108
        L_0x00f9:
            r20 = r10
            r15 = 0
            r18 = 1
            com.android.camera.PhotoUI r5 = r14.mUI
            r10 = 0
            java.lang.String[] r12 = new java.lang.String[]{r13, r10}
            r5.overrideSettings(r12)
        L_0x0108:
            r13 = 2131690008(0x7f0f0218, float:1.9009047E38)
            java.lang.String r12 = "auto"
            java.lang.String r10 = "off"
            if (r11 == 0) goto L_0x0117
            boolean r5 = r11.equals(r6)
            if (r5 != 0) goto L_0x014f
        L_0x0117:
            if (r8 == 0) goto L_0x011f
            boolean r2 = r8.equals(r2)
            if (r2 != 0) goto L_0x014f
        L_0x011f:
            if (r7 == 0) goto L_0x0127
            boolean r2 = r7.equals(r3)
            if (r2 != 0) goto L_0x014f
        L_0x0127:
            if (r0 == 0) goto L_0x012f
            boolean r2 = r0.equals(r1)
            if (r2 != 0) goto L_0x014f
        L_0x012f:
            if (r9 == 0) goto L_0x0137
            boolean r2 = r9.equals(r4)
            if (r2 != 0) goto L_0x014f
        L_0x0137:
            if (r20 == 0) goto L_0x0143
            r2 = r20
            r4 = r22
            boolean r2 = r2.equals(r4)
            if (r2 != 0) goto L_0x014f
        L_0x0143:
            if (r17 == 0) goto L_0x01a6
            r4 = r16
            r2 = r17
            boolean r2 = r2.equals(r4)
            if (r2 == 0) goto L_0x01a6
        L_0x014f:
            if (r7 == 0) goto L_0x0157
            boolean r2 = r7.equals(r3)
            if (r2 != 0) goto L_0x015f
        L_0x0157:
            if (r0 == 0) goto L_0x0162
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0162
        L_0x015f:
            r16 = 0
            goto L_0x0166
        L_0x0162:
            r14.mSceneMode = r12
            r16 = r12
        L_0x0166:
            java.lang.String r17 = "infinity"
            com.android.camera.CameraActivity r0 = r14.mActivity
            r1 = 2131690383(0x7f0f038f, float:1.9009808E38)
            java.lang.String r19 = r0.getString(r1)
            com.android.camera.CameraActivity r0 = r14.mActivity
            r1 = 2131689956(0x7f0f01e4, float:1.9008942E38)
            java.lang.String r20 = r0.getString(r1)
            com.android.camera.CameraActivity r0 = r14.mActivity
            java.lang.String r21 = r0.getString(r13)
            java.lang.String r22 = "0"
            r1 = 0
            r2 = 0
            r6 = 0
            r7 = 0
            r8 = 0
            r9 = 0
            r5 = 0
            r0 = r26
            r3 = r17
            r4 = r22
            r11 = r10
            r10 = r21
            r15 = r11
            r11 = r16
            r23 = r12
            r12 = r19
            r24 = r15
            r15 = r13
            r13 = r20
            r0.overrideCameraSettings(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
            r16 = r24
            r21 = 1
            goto L_0x01b3
        L_0x01a6:
            r24 = r10
            r23 = r12
            r15 = r13
            r3 = 0
            r4 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r16 = 0
        L_0x01b3:
            java.lang.String r0 = r14.mSceneMode
            r9 = r23
            boolean r0 = r9.equals(r0)
            if (r0 != 0) goto L_0x024d
            com.android.camera.FocusOverlayManager r0 = r14.mFocusManager
            java.lang.String r3 = r0.getFocusMode()
            android.hardware.Camera$Parameters r0 = r14.mParameters
            java.lang.String r0 = r0.getColorEffect()
            com.android.camera.CameraActivity r1 = r14.mActivity
            java.lang.String r1 = r1.getString(r15)
            java.lang.String r2 = r14.mSceneMode
            java.lang.String r4 = "hdr"
            boolean r2 = r4.equals(r2)
            if (r2 == 0) goto L_0x020c
            boolean r2 = com.android.camera.util.PersistUtil.isZzhdrEnabled()
            if (r2 != 0) goto L_0x01e1
            r21 = 1
        L_0x01e1:
            if (r0 == 0) goto L_0x01e5
            r2 = 1
            goto L_0x01e6
        L_0x01e5:
            r2 = 0
        L_0x01e6:
            boolean r4 = r0.equals(r1)
            r15 = 1
            r4 = r4 ^ r15
            r2 = r2 & r4
            if (r2 == 0) goto L_0x020d
            com.android.camera.PhotoUI r0 = r14.mUI
            java.lang.String r2 = "pref_camera_coloreffect_key"
            r0.setPreference(r2, r1)
            android.hardware.Camera$Parameters r0 = r14.mParameters
            r0.setColorEffect(r1)
            com.android.camera.CameraManager$CameraProxy r0 = r14.mCameraDevice
            android.hardware.Camera$Parameters r2 = r14.mParameters
            r0.setParameters(r2)
            com.android.camera.CameraManager$CameraProxy r0 = r14.mCameraDevice
            android.hardware.Camera$Parameters r0 = r0.getParameters()
            r14.mParameters = r0
            r10 = r1
            goto L_0x020e
        L_0x020c:
            r15 = 1
        L_0x020d:
            r10 = r0
        L_0x020e:
            android.hardware.Camera$Parameters r0 = r14.mParameters
            int r0 = r0.getExposureCompensation()
            java.lang.String r4 = java.lang.Integer.toString(r0)
            java.lang.String r5 = r14.mCurrTouchAfAec
            r1 = 0
            android.hardware.Camera$Parameters r0 = r14.mParameters
            java.lang.String r6 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getAutoExposure(r0)
            android.hardware.Camera$Parameters r0 = r14.mParameters
            int r0 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSaturation(r0)
            java.lang.String r7 = java.lang.Integer.toString(r0)
            android.hardware.Camera$Parameters r0 = r14.mParameters
            int r0 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getContrast(r0)
            java.lang.String r8 = java.lang.Integer.toString(r0)
            android.hardware.Camera$Parameters r0 = r14.mParameters
            int r0 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSharpness(r0)
            java.lang.String r16 = java.lang.Integer.toString(r0)
            java.lang.String r2 = "auto"
            r0 = r26
            r25 = r9
            r9 = r16
            r0.overrideCameraSettings(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
            r10 = r24
            goto L_0x0281
        L_0x024d:
            r25 = r9
            r15 = 1
            com.android.camera.FocusOverlayManager r0 = r14.mFocusManager
            boolean r0 = r0.isZslEnabled()
            if (r0 == 0) goto L_0x026b
            android.hardware.Camera$Parameters r0 = r14.mParameters
            java.lang.String r3 = r0.getFocusMode()
            r1 = 0
            r2 = 0
            r6 = 0
            r7 = 0
            r8 = 0
            r9 = 0
            r5 = 0
            r0 = r26
            r0.overrideCameraSettings(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
            goto L_0x027f
        L_0x026b:
            int r0 = r14.mManual3AEnabled
            if (r0 <= 0) goto L_0x0273
            r26.updateCommonManual3ASettings()
            goto L_0x027f
        L_0x0273:
            r1 = 0
            r2 = 0
            r6 = 0
            r7 = 0
            r8 = 0
            r9 = 0
            r5 = 0
            r0 = r26
            r0.overrideCameraSettings(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
        L_0x027f:
            r10 = r16
        L_0x0281:
            android.hardware.Camera$Parameters r0 = r14.mParameters
            java.lang.String r1 = "ae-bracket-hdr"
            java.lang.String r0 = r0.get(r1)
            r1 = r24
            if (r0 == 0) goto L_0x0299
            boolean r0 = r0.equalsIgnoreCase(r1)
            if (r0 != 0) goto L_0x0299
            android.hardware.Camera$Parameters r0 = r14.mParameters
            r0.setFlashMode(r1)
            r10 = r1
        L_0x0299:
            java.lang.String r0 = "pref_camera_longshot_key"
            if (r21 != 0) goto L_0x02ae
            boolean r2 = r14.mIsBokehMode
            if (r2 == 0) goto L_0x02a2
            goto L_0x02ae
        L_0x02a2:
            com.android.camera.PhotoUI r2 = r14.mUI
            r3 = 0
            java.lang.String[] r4 = new java.lang.String[]{r0, r3}
            r2.overrideSettings(r4)
            r3 = 0
            goto L_0x02c4
        L_0x02ae:
            com.android.camera.PhotoUI r2 = r14.mUI
            r3 = 2
            java.lang.String[] r4 = new java.lang.String[r3]
            r3 = 0
            r4[r3] = r0
            com.android.camera.CameraActivity r5 = r14.mActivity
            r6 = 2131690716(0x7f0f04dc, float:1.9010483E38)
            java.lang.String r5 = r5.getString(r6)
            r4[r15] = r5
            r2.overrideSettings(r4)
        L_0x02c4:
            boolean r2 = com.android.camera.TsMakeupManager.HAS_TS_MAKEUP
            if (r2 == 0) goto L_0x02f0
            com.android.camera.PreferenceGroup r2 = r14.mPreferenceGroup
            java.lang.String r4 = "pref_camera_tsmakeup_level_key"
            com.android.camera.ListPreference r2 = r2.findPreference(r4)
            com.android.camera.IconListPreference r2 = (com.android.camera.IconListPreference) r2
            if (r2 == 0) goto L_0x02f0
            java.lang.String r2 = r2.getValue()
            java.lang.String r4 = "Off"
            boolean r2 = r2.equalsIgnoreCase(r4)
            if (r2 != 0) goto L_0x02f0
            com.android.camera.PhotoUI r2 = r14.mUI
            r4 = 2
            java.lang.String[] r5 = new java.lang.String[r4]
            java.lang.String r4 = "pref_camera_facedetection_key"
            r5[r3] = r4
            java.lang.String r4 = org.codeaurora.snapcam.wrapper.ParametersWrapper.FACE_DETECTION_ON
            r5[r15] = r4
            r2.overrideSettings(r5)
        L_0x02f0:
            r2 = 2131690064(0x7f0f0250, float:1.9009161E38)
            java.lang.String r4 = "pref_camera_flashmode_key"
            if (r10 != 0) goto L_0x0314
            java.lang.String r5 = r14.mSavedFlashMode
            if (r5 != 0) goto L_0x0309
            com.android.camera.ComboPreferences r5 = r14.mPreferences
            com.android.camera.CameraActivity r6 = r14.mActivity
            java.lang.String r2 = r6.getString(r2)
            java.lang.String r2 = r5.getString(r4, r2)
            r14.mSavedFlashMode = r2
        L_0x0309:
            com.android.camera.PhotoUI r2 = r14.mUI
            java.lang.String r5 = r14.mSavedFlashMode
            r2.setPreference(r4, r5)
            r2 = 0
            r14.mSavedFlashMode = r2
            goto L_0x0332
        L_0x0314:
            java.lang.String r5 = r14.mSavedFlashMode
            if (r5 != 0) goto L_0x0326
            com.android.camera.ComboPreferences r5 = r14.mPreferences
            com.android.camera.CameraActivity r6 = r14.mActivity
            java.lang.String r2 = r6.getString(r2)
            java.lang.String r2 = r5.getString(r4, r2)
            r14.mSavedFlashMode = r2
        L_0x0326:
            com.android.camera.PhotoUI r2 = r14.mUI
            r5 = 2
            java.lang.String[] r6 = new java.lang.String[r5]
            r6[r3] = r4
            r6[r15] = r10
            r2.overrideSettings(r6)
        L_0x0332:
            int r2 = r14.mCameraId
            com.android.camera.CameraHolder r5 = com.android.camera.CameraHolder.instance()
            int r5 = r5.getFrontCameraId()
            java.lang.String r6 = "pref_camera_selfiemirror_key"
            if (r2 == r5) goto L_0x034d
            com.android.camera.PreferenceGroup r0 = r14.mPreferenceGroup
            java.lang.String r2 = "pref_selfie_flash_key"
            com.android.camera.CameraSettings.removePreferenceFromScreen(r0, r2)
            com.android.camera.PreferenceGroup r0 = r14.mPreferenceGroup
            com.android.camera.CameraSettings.removePreferenceFromScreen(r0, r6)
            goto L_0x0370
        L_0x034d:
            com.android.camera.PreferenceGroup r2 = r14.mPreferenceGroup
            com.android.camera.ListPreference r2 = r2.findPreference(r6)
            if (r2 == 0) goto L_0x0370
            java.lang.String r5 = r2.getValue()
            if (r5 == 0) goto L_0x0370
            java.lang.String r2 = r2.getValue()
            java.lang.String r5 = "enable"
            boolean r2 = r2.equalsIgnoreCase(r5)
            if (r2 == 0) goto L_0x0370
            com.android.camera.PhotoUI r2 = r14.mUI
            java.lang.String[] r0 = new java.lang.String[]{r0, r1}
            r2.overrideSettings(r0)
        L_0x0370:
            com.android.camera.ComboPreferences r0 = r14.mPreferences
            com.android.camera.CameraActivity r2 = r14.mActivity
            r5 = 2131689988(0x7f0f0204, float:1.9009007E38)
            java.lang.String r2 = r2.getString(r5)
            java.lang.String r5 = "pref_camera_bokeh_mode_key"
            java.lang.String r0 = r0.getString(r5, r2)
            com.android.camera.CameraActivity r2 = r14.mActivity
            r5 = 2131689991(0x7f0f0207, float:1.9009013E38)
            java.lang.String r2 = r2.getString(r5)
            boolean r0 = r0.equals(r2)
            java.lang.String r2 = "pref_camera_bokeh_blur_degree_key"
            if (r0 != 0) goto L_0x03e3
            r14.mIsBokehMode = r15
            com.android.camera.CameraManager$CameraProxy r0 = r14.mCameraDevice
            if (r0 == 0) goto L_0x039d
            com.android.camera.PhotoModule$MetaDataCallback r5 = r14.mMetaDataCallback
            r0.setMetadataCb(r5)
        L_0x039d:
            com.android.camera.PhotoUI r0 = r14.mUI
            java.lang.String[] r1 = new java.lang.String[]{r4, r1}
            r0.overrideSettings(r1)
            com.android.camera.PhotoUI r0 = r14.mUI
            java.lang.String r1 = "pref_camera_scenemode_key"
            r4 = r25
            java.lang.String[] r1 = new java.lang.String[]{r1, r4}
            r0.overrideSettings(r1)
            com.android.camera.CameraActivity r0 = r14.mActivity
            android.content.SharedPreferences r0 = android.preference.PreferenceManager.getDefaultSharedPreferences(r0)
            r1 = 50
            int r0 = r0.getInt(r2, r1)
            com.android.camera.PhotoUI r1 = r14.mUI
            android.widget.SeekBar r1 = r1.getBokehDegreeBar()
            r1.setProgress(r0)
            com.android.camera.PhotoUI r1 = r14.mUI
            android.widget.SeekBar r1 = r1.getBokehDegreeBar()
            android.widget.SeekBar$OnSeekBarChangeListener r2 = r14.mBlurDegreeListener
            r1.setOnSeekBarChangeListener(r2)
            com.android.camera.PhotoUI r1 = r14.mUI
            r1.enableBokehRender(r15)
            com.android.camera.PhotoUI r1 = r14.mUI
            r1.setBokehRenderDegree(r0)
            android.widget.TextView r0 = r14.mBokehTipText
            r0.setVisibility(r3)
            goto L_0x0437
        L_0x03e3:
            r14.mIsBokehMode = r3
            com.android.camera.CameraManager$CameraProxy r0 = r14.mCameraDevice
            if (r0 == 0) goto L_0x03ed
            r1 = 0
            r0.setMetadataCb(r1)
        L_0x03ed:
            com.android.camera.PhotoUI r0 = r14.mUI
            r1 = 2
            java.lang.String[] r4 = new java.lang.String[r1]
            java.lang.String r5 = "pref_camera_bokeh_mpo_key"
            r4[r3] = r5
            com.android.camera.CameraActivity r5 = r14.mActivity
            r6 = 2131689994(0x7f0f020a, float:1.900902E38)
            java.lang.String r5 = r5.getString(r6)
            r4[r15] = r5
            r0.overrideSettings(r4)
            com.android.camera.PhotoUI r0 = r14.mUI
            java.lang.String[] r1 = new java.lang.String[r1]
            r1[r3] = r2
            com.android.camera.CameraActivity r2 = r14.mActivity
            r4 = 2131689986(0x7f0f0202, float:1.9009003E38)
            java.lang.String r2 = r2.getString(r4)
            r1[r15] = r2
            r0.overrideSettings(r1)
            com.android.camera.PhotoUI r0 = r14.mUI
            android.widget.SeekBar r0 = r0.getBokehDegreeBar()
            r1 = 0
            r0.setOnSeekBarChangeListener(r1)
            com.android.camera.PhotoUI r0 = r14.mUI
            android.widget.SeekBar r0 = r0.getBokehDegreeBar()
            r1 = 8
            r0.setVisibility(r1)
            com.android.camera.PhotoUI r0 = r14.mUI
            r0.enableBokehRender(r3)
            android.widget.TextView r0 = r14.mBokehTipText
            r0.setVisibility(r1)
        L_0x0437:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.PhotoModule.updateCameraSettings():void");
    }

    private void overrideCameraSettings(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, String str10, String str11, String str12, String str13) {
        this.mUI.overrideSettings(CameraSettings.KEY_FLASH_MODE, str, "pref_camera_whitebalance_key", str2, CameraSettings.KEY_FOCUS_MODE, str3, "pref_camera_exposure_key", str4, CameraSettings.KEY_TOUCH_AF_AEC, str5, CameraSettings.KEY_AUTOEXPOSURE, str6, CameraSettings.KEY_SATURATION, str7, CameraSettings.KEY_CONTRAST, str8, CameraSettings.KEY_SHARPNESS, str9, CameraSettings.KEY_COLOR_EFFECT, str10, CameraSettings.KEY_SCENE_MODE, str11, "pref_camera_redeyereduction_key", str12, CameraSettings.KEY_AE_BRACKET_HDR, str13);
    }

    private void loadCameraPreferences() {
        this.mPreferenceGroup = new CameraSettings(this.mActivity, this.mInitialParams, this.mCameraId, CameraHolder.instance().getCameraInfo()).getPreferenceGroup(C0905R.xml.camera_preferences);
        int numberOfCameras = CameraHolder.instance().getNumberOfCameras();
        Log.e(TAG, "loadCameraPreferences() updating camera_id pref");
        IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(CameraSettings.KEY_CAMERA_ID);
        if (iconListPreference != null) {
            int[] iArr = new int[numberOfCameras];
            String[] strArr = new String[numberOfCameras];
            String[] strArr2 = new String[numberOfCameras];
            int[] iArr2 = new int[numberOfCameras];
            for (int i = 0; i < numberOfCameras; i++) {
                if (CameraHolder.instance().getCameraInfo()[i].facing == 1) {
                    iArr[i] = C0905R.C0906drawable.ic_switch_back;
                    strArr[i] = this.mActivity.getResources().getString(C0905R.string.pref_camera_id_entry_back);
                    strArr2[i] = this.mActivity.getResources().getString(C0905R.string.pref_camera_id_label_back);
                    iArr2[i] = C0905R.C0906drawable.ic_switch_back;
                } else {
                    iArr[i] = C0905R.C0906drawable.ic_switch_front;
                    strArr[i] = this.mActivity.getResources().getString(C0905R.string.pref_camera_id_entry_front);
                    strArr2[i] = this.mActivity.getResources().getString(C0905R.string.pref_camera_id_label_front);
                    iArr2[i] = C0905R.C0906drawable.ic_switch_front;
                }
            }
            iconListPreference.setIconIds(iArr);
            iconListPreference.setEntries(strArr);
            iconListPreference.setLabels(strArr2);
            iconListPreference.setLargeIconIds(iArr2);
        }
    }

    public void onOrientationChanged(int i) {
        if (i != -1) {
            int i2 = this.mOrientation;
            this.mOrientation = CameraUtil.roundOrientation(i, i2);
            if (i2 != this.mOrientation) {
                if (!(this.mParameters == null || this.mCameraDevice == null || this.mCameraState != 1)) {
                    Log.v(TAG, "onOrientationChanged, update parameters");
                    synchronized (this.mCameraDevice) {
                        setFlipValue();
                        this.mCameraDevice.setParameters(this.mParameters);
                    }
                }
                this.mUI.tryToCloseSubList();
                this.mUI.setOrientation(this.mOrientation, true);
                GraphView graphView = this.mGraphView;
                if (graphView != null) {
                    graphView.setRotation((float) (-this.mOrientation));
                }
            }
            if (this.mHandler.hasMessages(5)) {
                this.mHandler.removeMessages(5);
                showTapToFocusToast();
            }
            this.mGraphView = (GraphView) this.mRootView.findViewById(C0905R.C0907id.graph_view);
            GraphView graphView2 = this.mGraphView;
            if (graphView2 != null) {
                graphView2.setAlpha(0.75f);
                this.mGraphView.setPhotoModuleObject(this);
                this.mGraphView.PreviewChanged();
            }
        }
    }

    public void onCaptureCancelled() {
        this.mActivity.setResultEx(0, new Intent());
        this.mActivity.finish();
    }

    public void onCaptureRetake() {
        if (!this.mPaused) {
            this.mUI.hidePostCaptureAlert();
            setupPreview();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00c4, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        r6.mActivity.setResultEx(0);
        r6.mActivity.finish();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00d3, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r6.mActivity.setResultEx(0);
        r6.mActivity.finish();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00de, code lost:
        com.android.camera.util.CameraUtil.closeSilently(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00e1, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00e2, code lost:
        com.android.camera.util.CameraUtil.closeSilently(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00e5, code lost:
        throw r6;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:31:0x00c6, B:35:0x00d4] */
    /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x00c6 */
    /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x00d4 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onCaptureDone() {
        /*
            r6 = this;
            java.lang.String r0 = "crop-temp"
            boolean r1 = r6.mPaused
            if (r1 == 0) goto L_0x0007
            return
        L_0x0007:
            byte[] r1 = r6.mJpegImageData
            java.lang.String r2 = r6.mCropValue
            r3 = 0
            if (r2 != 0) goto L_0x005f
            android.net.Uri r0 = r6.mSaveUri
            r2 = -1
            if (r0 == 0) goto L_0x0034
            android.content.ContentResolver r4 = r6.mContentResolver     // Catch:{ IOException -> 0x002f, all -> 0x002a }
            java.io.OutputStream r3 = r4.openOutputStream(r0)     // Catch:{ IOException -> 0x002f, all -> 0x002a }
            r3.write(r1)     // Catch:{ IOException -> 0x002f, all -> 0x002a }
            r3.close()     // Catch:{ IOException -> 0x002f, all -> 0x002a }
            com.android.camera.CameraActivity r0 = r6.mActivity     // Catch:{ IOException -> 0x002f, all -> 0x002a }
            r0.setResultEx(r2)     // Catch:{ IOException -> 0x002f, all -> 0x002a }
            com.android.camera.CameraActivity r6 = r6.mActivity     // Catch:{ IOException -> 0x002f, all -> 0x002a }
            r6.finish()     // Catch:{ IOException -> 0x002f, all -> 0x002a }
            goto L_0x002f
        L_0x002a:
            r6 = move-exception
            com.android.camera.util.CameraUtil.closeSilently(r3)
            throw r6
        L_0x002f:
            com.android.camera.util.CameraUtil.closeSilently(r3)
            goto L_0x00c3
        L_0x0034:
            com.android.camera.exif.ExifInterface r0 = com.android.camera.Exif.getExif(r1)
            int r0 = com.android.camera.Exif.getOrientation(r0)
            r3 = 51200(0xc800, float:7.1746E-41)
            android.graphics.Bitmap r1 = com.android.camera.util.CameraUtil.makeBitmap(r1, r3)
            android.graphics.Bitmap r0 = com.android.camera.util.CameraUtil.rotate(r1, r0)
            com.android.camera.CameraActivity r1 = r6.mActivity
            android.content.Intent r3 = new android.content.Intent
            java.lang.String r4 = "inline-data"
            r3.<init>(r4)
            java.lang.String r4 = "data"
            android.content.Intent r0 = r3.putExtra(r4, r0)
            r1.setResultEx(r2, r0)
            com.android.camera.CameraActivity r6 = r6.mActivity
            r6.finish()
            goto L_0x00c3
        L_0x005f:
            r2 = 0
            com.android.camera.CameraActivity r4 = r6.mActivity     // Catch:{ FileNotFoundException -> 0x00d4, IOException -> 0x00c6 }
            java.io.File r4 = r4.getFileStreamPath(r0)     // Catch:{ FileNotFoundException -> 0x00d4, IOException -> 0x00c6 }
            r4.delete()     // Catch:{ FileNotFoundException -> 0x00d4, IOException -> 0x00c6 }
            com.android.camera.CameraActivity r5 = r6.mActivity     // Catch:{ FileNotFoundException -> 0x00d4, IOException -> 0x00c6 }
            java.io.FileOutputStream r3 = r5.openFileOutput(r0, r2)     // Catch:{ FileNotFoundException -> 0x00d4, IOException -> 0x00c6 }
            r3.write(r1)     // Catch:{ FileNotFoundException -> 0x00d4, IOException -> 0x00c6 }
            r3.close()     // Catch:{ FileNotFoundException -> 0x00d4, IOException -> 0x00c6 }
            android.net.Uri r0 = android.net.Uri.fromFile(r4)     // Catch:{ FileNotFoundException -> 0x00d4, IOException -> 0x00c6 }
            com.android.camera.util.CameraUtil.closeSilently(r3)
            android.os.Bundle r1 = new android.os.Bundle
            r1.<init>()
            java.lang.String r2 = r6.mCropValue
            java.lang.String r3 = "circle"
            boolean r2 = r2.equals(r3)
            if (r2 == 0) goto L_0x0092
            java.lang.String r2 = "circleCrop"
            java.lang.String r3 = "true"
            r1.putString(r2, r3)
        L_0x0092:
            android.net.Uri r2 = r6.mSaveUri
            r3 = 1
            if (r2 == 0) goto L_0x009d
            java.lang.String r4 = "output"
            r1.putParcelable(r4, r2)
            goto L_0x00a2
        L_0x009d:
            java.lang.String r2 = "return-data"
            r1.putBoolean(r2, r3)
        L_0x00a2:
            com.android.camera.CameraActivity r2 = r6.mActivity
            boolean r2 = r2.isSecureCamera()
            if (r2 == 0) goto L_0x00af
            java.lang.String r2 = "showWhenLocked"
            r1.putBoolean(r2, r3)
        L_0x00af:
            android.content.Intent r2 = new android.content.Intent
            java.lang.String r3 = "com.android.camera.action.CROP"
            r2.<init>(r3)
            r2.setData(r0)
            r2.putExtras(r1)
            com.android.camera.CameraActivity r6 = r6.mActivity
            r0 = 1000(0x3e8, float:1.401E-42)
            r6.startActivityForResult(r2, r0)
        L_0x00c3:
            return
        L_0x00c4:
            r6 = move-exception
            goto L_0x00e2
        L_0x00c6:
            com.android.camera.CameraActivity r0 = r6.mActivity     // Catch:{ all -> 0x00c4 }
            r0.setResultEx(r2)     // Catch:{ all -> 0x00c4 }
            com.android.camera.CameraActivity r6 = r6.mActivity     // Catch:{ all -> 0x00c4 }
            r6.finish()     // Catch:{ all -> 0x00c4 }
            com.android.camera.util.CameraUtil.closeSilently(r3)
            return
        L_0x00d4:
            com.android.camera.CameraActivity r0 = r6.mActivity     // Catch:{ all -> 0x00c4 }
            r0.setResultEx(r2)     // Catch:{ all -> 0x00c4 }
            com.android.camera.CameraActivity r6 = r6.mActivity     // Catch:{ all -> 0x00c4 }
            r6.finish()     // Catch:{ all -> 0x00c4 }
            com.android.camera.util.CameraUtil.closeSilently(r3)
            return
        L_0x00e2:
            com.android.camera.util.CameraUtil.closeSilently(r3)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.PhotoModule.onCaptureDone():void");
    }

    public void onShutterButtonFocus(boolean z) {
        if (this.mCameraDevice != null && !this.mPaused && !this.mUI.collapseCameraControls()) {
            int i = this.mCameraState;
            if (!(i == 3 || i == 0 || this.mFocusManager == null)) {
                synchronized (this.mCameraDevice) {
                    if (this.mCameraState == 5) {
                        this.mLongshotActive = false;
                        this.mCameraDevice.setLongshot(false);
                        this.mUI.animateCapture(this.mLastJpegData);
                        this.mLastJpegData = null;
                        if (!this.mFocusManager.isZslEnabled()) {
                            setupPreview();
                        } else {
                            setCameraState(1);
                            this.mFocusManager.resetTouchFocus();
                            if (CameraUtil.FOCUS_MODE_CONTINUOUS_PICTURE.equals(this.mFocusManager.getFocusMode())) {
                                this.mCameraDevice.cancelAutoFocus();
                            }
                            this.mUI.resumeFaceDetection();
                        }
                    }
                }
                if (!z || canTakePicture()) {
                    if (z) {
                        this.mFocusManager.onShutterDown();
                    } else if (!this.mUI.isCountingDown()) {
                        this.mFocusManager.onShutterUp();
                    }
                    return;
                }
                return;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("onShutterButtonFocus error case mCameraState = ");
        sb.append(this.mCameraState);
        sb.append("mCameraDevice = ");
        sb.append(this.mCameraDevice);
        sb.append("mPaused =");
        sb.append(this.mPaused);
        Log.v(TAG, sb.toString());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:54:0x011b, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x011d, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void onShutterButtonClick() {
        /*
            r8 = this;
            monitor-enter(r8)
            com.android.camera.CameraManager$CameraProxy r0 = r8.mCameraDevice     // Catch:{ all -> 0x011e }
            if (r0 == 0) goto L_0x011c
            boolean r0 = r8.mPaused     // Catch:{ all -> 0x011e }
            if (r0 != 0) goto L_0x011c
            com.android.camera.PhotoUI r0 = r8.mUI     // Catch:{ all -> 0x011e }
            boolean r0 = r0.collapseCameraControls()     // Catch:{ all -> 0x011e }
            if (r0 != 0) goto L_0x011c
            com.android.camera.PhotoUI r0 = r8.mUI     // Catch:{ all -> 0x011e }
            boolean r0 = r0.mMenuInitialized     // Catch:{ all -> 0x011e }
            if (r0 == 0) goto L_0x011c
            int r0 = r8.mCameraState     // Catch:{ all -> 0x011e }
            r1 = 4
            if (r0 == r1) goto L_0x011c
            int r0 = r8.mCameraState     // Catch:{ all -> 0x011e }
            if (r0 == 0) goto L_0x011c
            int r0 = r8.mCameraState     // Catch:{ all -> 0x011e }
            r1 = 5
            if (r0 == r1) goto L_0x011c
            com.android.camera.FocusOverlayManager r0 = r8.mFocusManager     // Catch:{ all -> 0x011e }
            if (r0 != 0) goto L_0x002b
            goto L_0x011c
        L_0x002b:
            com.android.camera.CameraActivity r0 = r8.mActivity     // Catch:{ all -> 0x011e }
            long r0 = r0.getStorageSpaceBytes()     // Catch:{ all -> 0x011e }
            r2 = 104857600(0x6400000, double:5.1806538E-316)
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 > 0) goto L_0x0056
            java.lang.String r0 = "CAM_PhotoModule"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x011e }
            r1.<init>()     // Catch:{ all -> 0x011e }
            java.lang.String r2 = "Not enough space or storage not ready. remaining="
            r1.append(r2)     // Catch:{ all -> 0x011e }
            com.android.camera.CameraActivity r2 = r8.mActivity     // Catch:{ all -> 0x011e }
            long r2 = r2.getStorageSpaceBytes()     // Catch:{ all -> 0x011e }
            r1.append(r2)     // Catch:{ all -> 0x011e }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x011e }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x011e }
            monitor-exit(r8)
            return
        L_0x0056:
            java.lang.String r0 = "CAM_PhotoModule"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x011e }
            r1.<init>()     // Catch:{ all -> 0x011e }
            java.lang.String r2 = "onShutterButtonClick: mCameraState="
            r1.append(r2)     // Catch:{ all -> 0x011e }
            int r2 = r8.mCameraState     // Catch:{ all -> 0x011e }
            r1.append(r2)     // Catch:{ all -> 0x011e }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x011e }
            android.util.Log.v(r0, r1)     // Catch:{ all -> 0x011e }
            java.lang.String r0 = r8.mSceneMode     // Catch:{ all -> 0x011e }
            java.lang.String r1 = "hdr"
            r2 = 0
            if (r0 != r1) goto L_0x007f
            com.android.camera.PhotoUI r0 = r8.mUI     // Catch:{ all -> 0x011e }
            r0.hideSwitcher()     // Catch:{ all -> 0x011e }
            com.android.camera.PhotoUI r0 = r8.mUI     // Catch:{ all -> 0x011e }
            r0.setSwipingEnabled(r2)     // Catch:{ all -> 0x011e }
        L_0x007f:
            com.android.camera.FocusOverlayManager r0 = r8.mFocusManager     // Catch:{ all -> 0x011e }
            r1 = 1
            if (r0 == 0) goto L_0x0095
            int r0 = r8.mSnapshotMode     // Catch:{ all -> 0x011e }
            int r3 = org.codeaurora.snapcam.wrapper.CameraInfoWrapper.CAMERA_SUPPORT_MODE_ZSL     // Catch:{ all -> 0x011e }
            if (r0 != r3) goto L_0x0090
            com.android.camera.FocusOverlayManager r0 = r8.mFocusManager     // Catch:{ all -> 0x011e }
            r0.setZslEnable(r1)     // Catch:{ all -> 0x011e }
            goto L_0x0095
        L_0x0090:
            com.android.camera.FocusOverlayManager r0 = r8.mFocusManager     // Catch:{ all -> 0x011e }
            r0.setZslEnable(r2)     // Catch:{ all -> 0x011e }
        L_0x0095:
            com.android.camera.FocusOverlayManager r0 = r8.mFocusManager     // Catch:{ all -> 0x011e }
            if (r0 == 0) goto L_0x00a1
            com.android.camera.FocusOverlayManager r0 = r8.mFocusManager     // Catch:{ all -> 0x011e }
            boolean r0 = r0.isFocusingSnapOnFinish()     // Catch:{ all -> 0x011e }
            if (r0 != 0) goto L_0x00a6
        L_0x00a1:
            int r0 = r8.mCameraState     // Catch:{ all -> 0x011e }
            r3 = 3
            if (r0 != r3) goto L_0x00ae
        L_0x00a6:
            boolean r0 = r8.mIsImageCaptureIntent     // Catch:{ all -> 0x011e }
            if (r0 != 0) goto L_0x00ae
            r8.mSnapshotOnIdle = r1     // Catch:{ all -> 0x011e }
            monitor-exit(r8)
            return
        L_0x00ae:
            com.android.camera.ComboPreferences r0 = r8.mPreferences     // Catch:{ all -> 0x011e }
            java.lang.String r3 = "pref_camera_timer_key"
            com.android.camera.CameraActivity r4 = r8.mActivity     // Catch:{ all -> 0x011e }
            r5 = 2131690492(0x7f0f03fc, float:1.901003E38)
            java.lang.String r4 = r4.getString(r5)     // Catch:{ all -> 0x011e }
            java.lang.String r0 = r0.getString(r3, r4)     // Catch:{ all -> 0x011e }
            com.android.camera.ComboPreferences r3 = r8.mPreferences     // Catch:{ all -> 0x011e }
            java.lang.String r4 = "pref_camera_timer_sound_key"
            com.android.camera.CameraActivity r5 = r8.mActivity     // Catch:{ all -> 0x011e }
            r6 = 2131690493(0x7f0f03fd, float:1.9010031E38)
            java.lang.String r5 = r5.getString(r6)     // Catch:{ all -> 0x011e }
            java.lang.String r3 = r3.getString(r4, r5)     // Catch:{ all -> 0x011e }
            com.android.camera.CameraActivity r4 = r8.mActivity     // Catch:{ all -> 0x011e }
            r5 = 2131690718(0x7f0f04de, float:1.9010488E38)
            java.lang.String r4 = r4.getString(r5)     // Catch:{ all -> 0x011e }
            boolean r3 = r3.equals(r4)     // Catch:{ all -> 0x011e }
            int r0 = java.lang.Integer.parseInt(r0)     // Catch:{ all -> 0x011e }
            com.android.camera.PhotoUI r4 = r8.mUI     // Catch:{ all -> 0x011e }
            boolean r4 = r4.isCountingDown()     // Catch:{ all -> 0x011e }
            if (r4 == 0) goto L_0x00ee
            com.android.camera.PhotoUI r4 = r8.mUI     // Catch:{ all -> 0x011e }
            r4.cancelCountDown()     // Catch:{ all -> 0x011e }
        L_0x00ee:
            if (r0 <= 0) goto L_0x0115
            com.android.camera.ComboPreferences r4 = r8.mPreferences     // Catch:{ all -> 0x011e }
            java.lang.String r5 = "pref_camera_zsl_key"
            com.android.camera.CameraActivity r6 = r8.mActivity     // Catch:{ all -> 0x011e }
            r7 = 2131690589(0x7f0f045d, float:1.9010226E38)
            java.lang.String r6 = r6.getString(r7)     // Catch:{ all -> 0x011e }
            java.lang.String r4 = r4.getString(r5, r6)     // Catch:{ all -> 0x011e }
            com.android.camera.PhotoUI r5 = r8.mUI     // Catch:{ all -> 0x011e }
            r6 = 2
            java.lang.String[] r6 = new java.lang.String[r6]     // Catch:{ all -> 0x011e }
            java.lang.String r7 = "pref_camera_zsl_key"
            r6[r2] = r7     // Catch:{ all -> 0x011e }
            r6[r1] = r4     // Catch:{ all -> 0x011e }
            r5.overrideSettings(r6)     // Catch:{ all -> 0x011e }
            com.android.camera.PhotoUI r1 = r8.mUI     // Catch:{ all -> 0x011e }
            r1.startCountDown(r0, r3)     // Catch:{ all -> 0x011e }
            goto L_0x011a
        L_0x0115:
            r8.mSnapshotOnIdle = r2     // Catch:{ all -> 0x011e }
            r8.initiateSnap()     // Catch:{ all -> 0x011e }
        L_0x011a:
            monitor-exit(r8)
            return
        L_0x011c:
            monitor-exit(r8)
            return
        L_0x011e:
            r0 = move-exception
            monitor-exit(r8)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.PhotoModule.onShutterButtonClick():void");
    }

    /* access modifiers changed from: private */
    public boolean isShutterSoundOn() {
        IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference("pref_camera_shuttersound_key");
        return iconListPreference == null || iconListPreference.getValue() == null || !iconListPreference.getValue().equalsIgnoreCase("disable");
    }

    private void initiateSnap() {
        if (!this.mPreferences.getString("pref_selfie_flash_key", this.mActivity.getString(C0905R.string.pref_selfie_flash_default)).equalsIgnoreCase(RecordLocationPreference.VALUE_ON) || this.mCameraId != CameraHolder.instance().getFrontCameraId()) {
            this.mFocusManager.doSnap();
            return;
        }
        this.mUI.startSelfieFlash();
        if (this.selfieThread == null) {
            this.selfieThread = new SelfieThread();
            this.selfieThread.start();
        }
    }

    public void onShutterButtonLongClick() {
        int i = (this.mActivity.getStorageSpaceBytes() > Storage.LOW_STORAGE_THRESHOLD_BYTES ? 1 : (this.mActivity.getStorageSpaceBytes() == Storage.LOW_STORAGE_THRESHOLD_BYTES ? 0 : -1));
        String str = TAG;
        if (i <= 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Not enough space or storage not ready. remaining=");
            sb.append(this.mActivity.getStorageSpaceBytes());
            Log.i(str, sb.toString());
            return;
        }
        if (this.mCameraDevice != null) {
            int i2 = this.mCameraState;
            if (i2 == 1 || i2 == 2) {
                String string = this.mPreferences.getString("pref_camera_longshot_key", this.mActivity.getString(C0905R.string.pref_camera_longshot_default));
                StringBuilder sb2 = new StringBuilder();
                sb2.append("longshot_enable = ");
                sb2.append(string);
                Log.d(str, sb2.toString());
                if (string.equals(RecordLocationPreference.VALUE_ON)) {
                    this.mLongshotSave = PersistUtil.isLongSaveEnabled();
                    if (this.mUI.isCountingDown()) {
                        this.mUI.cancelCountDown();
                    }
                    if (!isLongshotNeedCancel()) {
                        this.mLongshotActive = true;
                        setCameraState(5);
                        this.mFocusManager.doSnap();
                    }
                }
            }
        }
    }

    public boolean updateStorageHintOnResume() {
        return this.mFirstTimeInitialized;
    }

    public void onResumeBeforeSuper() {
        this.mPaused = false;
    }

    /* access modifiers changed from: private */
    public void openCamera() {
        if (!this.mPaused) {
            String str = TAG;
            Log.v(str, "Open camera device.");
            CameraActivity cameraActivity = this.mActivity;
            this.mCameraDevice = CameraUtil.openCamera(cameraActivity, this.mCameraId, this.mHandler, cameraActivity.getCameraOpenErrorCallback());
            CameraProxy cameraProxy = this.mCameraDevice;
            if (cameraProxy == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to open camera:");
                sb.append(this.mCameraId);
                Log.e(str, sb.toString());
                this.mHandler.sendEmptyMessage(9);
                return;
            }
            this.mParameters = cameraProxy.getParameters();
            this.mCameraPreviewParamsReady = true;
            this.mInitialParams = this.mCameraDevice.getParameters();
            FocusOverlayManager focusOverlayManager = this.mFocusManager;
            if (focusOverlayManager == null) {
                initializeFocusManager();
            } else {
                focusOverlayManager.setParameters(this.mInitialParams);
            }
            initializeCapabilities();
            this.mHandler.sendEmptyMessageDelayed(8, 100);
        }
    }

    public void onResumeAfterSuper() {
        this.mLastPhotoTakenWithRefocus = false;
        this.mUI.showSurfaceView();
        String action = this.mActivity.getIntent().getAction();
        boolean equals = "android.media.action.STILL_IMAGE_CAMERA".equals(action);
        String str = TAG;
        if (equals || CameraUtil.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action)) {
            Log.v(str, "On resume, from lock screen.");
            if (isInstantCaptureEnabled()) {
                this.mInstantCaptureSnapShot = true;
            }
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    PhotoModule.this.onResumeTasks();
                }
            }, 20);
        } else {
            Log.v(str, "On resume.");
            onResumeTasks();
        }
        this.mUI.setSwitcherIndex();
        if (this.mSoundPool == null) {
            this.mSoundPool = new SoundPool(1, 5, 0);
            this.mRefocusSound = this.mSoundPool.load(this.mActivity, C0905R.raw.camera_click_x5, 1);
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                PhotoModule.this.mActivity.updateStorageSpaceAndHint();
                PhotoModule.this.updateRemainingPhotos();
                PhotoModule.this.mUI.hideUI();
                PhotoModule.this.mUI.showUI();
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateRemainingPhotos() {
        if (this.mJpegFileSizeEstimation != 0) {
            this.mRemainingPhotos = (int) ((this.mActivity.getStorageSpaceBytes() - Storage.LOW_STORAGE_THRESHOLD_BYTES) / ((long) this.mJpegFileSizeEstimation));
        } else {
            this.mRemainingPhotos = -1;
        }
        this.mUI.updateRemainingPhotos(this.mRemainingPhotos);
    }

    /* access modifiers changed from: private */
    public void onResumeTasks() {
        Log.v(TAG, "Executing onResumeTasks.");
        if (!this.mOpenCameraFail && !this.mCameraDisabled) {
            if (this.mOpenCameraThread == null) {
                this.mOpenCameraThread = new OpenCameraThread();
                this.mOpenCameraThread.start();
            }
            this.mUI.applySurfaceChange(SURFACE_STATUS.SURFACE_VIEW);
            this.mJpegPictureCallbackTime = 0;
            this.mZoomValue = 0;
            if (!this.mFirstTimeInitialized) {
                this.mHandler.sendEmptyMessage(2);
            } else {
                initializeSecondTime();
            }
            this.mUI.initDisplayChangeListener();
            keepScreenOnAwhile();
            this.mUI.updateOnScreenIndicators(this.mParameters, this.mPreferenceGroup, this.mPreferences);
            Sensor defaultSensor = this.mSensorManager.getDefaultSensor(1);
            if (defaultSensor != null) {
                this.mSensorManager.registerListener(this, defaultSensor, 3);
            }
            Sensor defaultSensor2 = this.mSensorManager.getDefaultSensor(2);
            if (defaultSensor2 != null) {
                this.mSensorManager.registerListener(this, defaultSensor2, 3);
            }
            this.mOnResumeTime = SystemClock.uptimeMillis();
            checkDisplayRotation();
            this.mAnimateCapture = PersistUtil.isCaptureAnimationEnabled();
        }
    }

    public void onPauseBeforeSuper() {
        this.mPaused = true;
        this.mUI.applySurfaceChange(SURFACE_STATUS.HIDE);
        Sensor defaultSensor = this.mSensorManager.getDefaultSensor(1);
        if (defaultSensor != null) {
            this.mSensorManager.unregisterListener(this, defaultSensor);
        }
        Sensor defaultSensor2 = this.mSensorManager.getDefaultSensor(2);
        if (defaultSensor2 != null) {
            this.mSensorManager.unregisterListener(this, defaultSensor2);
        }
        SoundPool soundPool = this.mSoundPool;
        if (soundPool != null) {
            soundPool.release();
            this.mSoundPool = null;
        }
        SelfieThread selfieThread2 = this.selfieThread;
        if (selfieThread2 != null) {
            selfieThread2.interrupt();
        }
        this.mUI.stopSelfieFlash();
        Log.d(TAG, "remove idle handleer in onPause");
        removeIdleHandler();
    }

    public void onPauseAfterSuper() {
        Log.v(TAG, "On pause.");
        this.mUI.showPreviewCover();
        this.mUI.hideSurfaceView();
        try {
            if (this.mOpenCameraThread != null) {
                this.mOpenCameraThread.join();
            }
        } catch (InterruptedException unused) {
        }
        this.mOpenCameraThread = null;
        CameraProxy cameraProxy = this.mCameraDevice;
        if (!(cameraProxy == null || this.mCameraState == 0)) {
            cameraProxy.cancelAutoFocus();
        }
        resetManual3ASettings();
        stopPreview();
        this.mNamedImages = null;
        LocationManager locationManager = this.mLocationManager;
        if (locationManager != null) {
            locationManager.recordLocation(false);
        }
        this.mJpegImageData = null;
        this.mHandler.removeCallbacksAndMessages(null);
        closeCamera();
        resetScreenOn();
        this.mUI.onPause();
        this.mPendingSwitchCameraId = -1;
        FocusOverlayManager focusOverlayManager = this.mFocusManager;
        if (focusOverlayManager != null) {
            focusOverlayManager.removeMessages();
        }
        MediaSaveService mediaSaveService = this.mActivity.getMediaSaveService();
        if (mediaSaveService != null) {
            mediaSaveService.setListener(null);
        }
        this.mUI.removeDisplayChangeListener();
    }

    private void initializeFocusManager() {
        FocusOverlayManager focusOverlayManager = this.mFocusManager;
        if (focusOverlayManager != null) {
            focusOverlayManager.removeMessages();
            return;
        }
        this.mMirror = CameraHolder.instance().getCameraInfo()[this.mCameraId].facing == 0;
        FocusOverlayManager focusOverlayManager2 = new FocusOverlayManager(this.mPreferences, this.mActivity.getResources().getStringArray(C0905R.array.pref_camera_focusmode_default_array), this.mInitialParams, this, this.mMirror, this.mActivity.getMainLooper(), this.mUI);
        this.mFocusManager = focusOverlayManager2;
    }

    private void updateFocusManager(PhotoUI photoUI) {
        FocusOverlayManager focusOverlayManager = this.mFocusManager;
        if (focusOverlayManager != null && photoUI != null) {
            focusOverlayManager.setPhotoUI(photoUI);
            View rootView = photoUI.getRootView();
            this.mFocusManager.setPreviewSize(rootView.getWidth(), rootView.getHeight());
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        Log.v(TAG, "onConfigurationChanged");
        setDisplayOrientation();
        resizeForPreviewAspectRatio();
    }

    public void updateCameraOrientation() {
        if (this.mDisplayRotation != CameraUtil.getDisplayRotation(this.mActivity)) {
            setDisplayOrientation();
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1000) {
            Intent intent2 = new Intent();
            if (intent != null) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    intent2.putExtras(extras);
                }
            }
            this.mActivity.setResultEx(i2, intent2);
            this.mActivity.finish();
            this.mActivity.getFileStreamPath(sTempCropFilename).delete();
        }
    }

    /* access modifiers changed from: protected */
    public CameraProxy getCamera() {
        return this.mCameraDevice;
    }

    private boolean canTakePicture() {
        return isCameraIdle() && this.mActivity.getStorageSpaceBytes() > Storage.LOW_STORAGE_THRESHOLD_BYTES;
    }

    public void autoFocus() {
        this.mFocusStartTime = System.currentTimeMillis();
        this.mCameraDevice.autoFocus(this.mHandler, this.mAutoFocusCallback);
        setCameraState(2);
    }

    public void cancelAutoFocus() {
        CameraProxy cameraProxy = this.mCameraDevice;
        if (cameraProxy != null) {
            cameraProxy.cancelAutoFocus();
            setCameraState(1);
            setCameraParameters(4);
        }
    }

    public void onSingleTapUp(View view, int i, int i2) {
        if (!this.mPaused && this.mCameraDevice != null && this.mFirstTimeInitialized) {
            int i3 = this.mCameraState;
            if (i3 != 3 && i3 != 4 && i3 != 0 && this.mTouchAfAecFlag) {
                if ((this.mFocusAreaSupported || this.mMeteringAreaSupported) && this.mFocusManager.getPreviewRect().contains(i, i2)) {
                    this.mFocusManager.onSingleTapUp(i, i2);
                }
            }
        }
    }

    public boolean onBackPressed() {
        return this.mUI.onBackPressed();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00f1, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onKeyDown(int r6, android.view.KeyEvent r7) {
        /*
            r5 = this;
            r0 = 27
            r1 = 1
            if (r6 == r0) goto L_0x0101
            r0 = 80
            r2 = 0
            if (r6 == r0) goto L_0x00f2
            r0 = 4
            r3 = 2
            switch(r6) {
                case 21: goto L_0x0090;
                case 22: goto L_0x002d;
                case 23: goto L_0x001a;
                case 24: goto L_0x0011;
                case 25: goto L_0x0011;
                default: goto L_0x000f;
            }
        L_0x000f:
            goto L_0x00f1
        L_0x0011:
            com.android.camera.CameraActivity r6 = r5.mActivity
            boolean r6 = com.android.camera.util.CameraUtil.volumeKeyShutterDisable(r6)
            if (r6 == 0) goto L_0x00f2
            return r2
        L_0x001a:
            boolean r6 = r5.mFirstTimeInitialized
            if (r6 == 0) goto L_0x002c
            int r6 = r7.getRepeatCount()
            if (r6 != 0) goto L_0x002c
            r5.onShutterButtonFocus(r1)
            com.android.camera.PhotoUI r5 = r5.mUI
            r5.pressShutterButton()
        L_0x002c:
            return r1
        L_0x002d:
            int r6 = r5.mCameraState
            if (r6 == 0) goto L_0x00f1
            com.android.camera.FocusOverlayManager r6 = r5.mFocusManager
            if (r6 == 0) goto L_0x00f1
            int r6 = r6.getCurrentFocusState()
            com.android.camera.FocusOverlayManager r7 = r5.mFocusManager
            if (r6 == r1) goto L_0x00f1
            int r6 = r7.getCurrentFocusState()
            if (r6 == r3) goto L_0x00f1
            int r6 = r5.mbrightness
            r7 = 6
            if (r6 >= r7) goto L_0x0071
            int r7 = r5.mbrightness_step
            int r6 = r6 + r7
            r5.mbrightness = r6
            com.android.camera.CameraManager$CameraProxy r6 = r5.mCameraDevice
            monitor-enter(r6)
            com.android.camera.CameraManager$CameraProxy r7 = r5.mCameraDevice     // Catch:{ all -> 0x006e }
            android.hardware.Camera$Parameters r7 = r7.getParameters()     // Catch:{ all -> 0x006e }
            r5.mParameters = r7     // Catch:{ all -> 0x006e }
            android.hardware.Camera$Parameters r7 = r5.mParameters     // Catch:{ all -> 0x006e }
            java.lang.String r3 = "luma-adaptation"
            int r4 = r5.mbrightness     // Catch:{ all -> 0x006e }
            java.lang.String r4 = java.lang.String.valueOf(r4)     // Catch:{ all -> 0x006e }
            r7.set(r3, r4)     // Catch:{ all -> 0x006e }
            com.android.camera.CameraManager$CameraProxy r7 = r5.mCameraDevice     // Catch:{ all -> 0x006e }
            android.hardware.Camera$Parameters r3 = r5.mParameters     // Catch:{ all -> 0x006e }
            r7.setParameters(r3)     // Catch:{ all -> 0x006e }
            monitor-exit(r6)     // Catch:{ all -> 0x006e }
            goto L_0x0071
        L_0x006e:
            r5 = move-exception
            monitor-exit(r6)     // Catch:{ all -> 0x006e }
            throw r5
        L_0x0071:
            android.widget.ProgressBar r6 = r5.brightnessProgressBar
            int r7 = r5.mbrightness
            r6.setProgress(r7)
            com.android.camera.ComboPreferences r6 = r5.mPreferences
            android.content.SharedPreferences$Editor r6 = r6.edit()
            int r7 = r5.mbrightness
            java.lang.String r3 = "pref_camera_brightness_key"
            r6.putInt(r3, r7)
            r6.apply()
            android.widget.ProgressBar r5 = r5.brightnessProgressBar
            r5.setVisibility(r0)
            mBrightnessVisible = r1
            goto L_0x00f1
        L_0x0090:
            int r6 = r5.mCameraState
            if (r6 == 0) goto L_0x00f1
            com.android.camera.FocusOverlayManager r6 = r5.mFocusManager
            if (r6 == 0) goto L_0x00f1
            int r6 = r6.getCurrentFocusState()
            com.android.camera.FocusOverlayManager r7 = r5.mFocusManager
            if (r6 == r1) goto L_0x00f1
            int r6 = r7.getCurrentFocusState()
            if (r6 == r3) goto L_0x00f1
            int r6 = r5.mbrightness
            if (r6 <= 0) goto L_0x00d3
            int r7 = r5.mbrightness_step
            int r6 = r6 - r7
            r5.mbrightness = r6
            com.android.camera.CameraManager$CameraProxy r6 = r5.mCameraDevice
            monitor-enter(r6)
            com.android.camera.CameraManager$CameraProxy r7 = r5.mCameraDevice     // Catch:{ all -> 0x00d0 }
            android.hardware.Camera$Parameters r7 = r7.getParameters()     // Catch:{ all -> 0x00d0 }
            r5.mParameters = r7     // Catch:{ all -> 0x00d0 }
            android.hardware.Camera$Parameters r7 = r5.mParameters     // Catch:{ all -> 0x00d0 }
            java.lang.String r3 = "luma-adaptation"
            int r4 = r5.mbrightness     // Catch:{ all -> 0x00d0 }
            java.lang.String r4 = java.lang.String.valueOf(r4)     // Catch:{ all -> 0x00d0 }
            r7.set(r3, r4)     // Catch:{ all -> 0x00d0 }
            com.android.camera.CameraManager$CameraProxy r7 = r5.mCameraDevice     // Catch:{ all -> 0x00d0 }
            android.hardware.Camera$Parameters r3 = r5.mParameters     // Catch:{ all -> 0x00d0 }
            r7.setParameters(r3)     // Catch:{ all -> 0x00d0 }
            monitor-exit(r6)     // Catch:{ all -> 0x00d0 }
            goto L_0x00d3
        L_0x00d0:
            r5 = move-exception
            monitor-exit(r6)     // Catch:{ all -> 0x00d0 }
            throw r5
        L_0x00d3:
            android.widget.ProgressBar r6 = r5.brightnessProgressBar
            int r7 = r5.mbrightness
            r6.setProgress(r7)
            com.android.camera.ComboPreferences r6 = r5.mPreferences
            android.content.SharedPreferences$Editor r6 = r6.edit()
            int r7 = r5.mbrightness
            java.lang.String r3 = "pref_camera_brightness_key"
            r6.putInt(r3, r7)
            r6.apply()
            android.widget.ProgressBar r5 = r5.brightnessProgressBar
            r5.setVisibility(r0)
            mBrightnessVisible = r1
        L_0x00f1:
            return r2
        L_0x00f2:
            boolean r6 = r5.mFirstTimeInitialized
            if (r6 == 0) goto L_0x0100
            int r6 = r7.getRepeatCount()
            if (r6 != 0) goto L_0x00ff
            r5.onShutterButtonFocus(r1)
        L_0x00ff:
            return r1
        L_0x0100:
            return r2
        L_0x0101:
            boolean r6 = r5.mFirstTimeInitialized
            if (r6 == 0) goto L_0x010e
            int r6 = r7.getRepeatCount()
            if (r6 != 0) goto L_0x010e
            r5.onShutterButtonClick()
        L_0x010e:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.PhotoModule.onKeyDown(int, android.view.KeyEvent):boolean");
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (i == 24 || i == 25) {
            if (!this.mFirstTimeInitialized || CameraUtil.volumeKeyShutterDisable(this.mActivity)) {
                return false;
            }
            onShutterButtonClick();
            return true;
        } else if (i != 80) {
            return false;
        } else {
            if (this.mFirstTimeInitialized) {
                onShutterButtonFocus(false);
            }
            return true;
        }
    }

    private void closeCamera() {
        Log.v(TAG, "Close camera device.");
        CameraProxy cameraProxy = this.mCameraDevice;
        if (cameraProxy != null) {
            cameraProxy.setZoomChangeListener(null);
            this.mCameraDevice.setFaceDetectionCallback(null, null);
            this.mCameraDevice.setErrorCallback(null);
            if (this.mActivity.isSecureCamera() || this.mActivity.isForceReleaseCamera()) {
                CameraHolder.instance().strongRelease();
            } else {
                CameraHolder.instance().release();
            }
            this.mFaceDetectionStarted = false;
            this.mCameraDevice = null;
            setCameraState(0);
            FocusOverlayManager focusOverlayManager = this.mFocusManager;
            if (focusOverlayManager != null) {
                focusOverlayManager.onCameraReleased();
            }
        }
    }

    private void setDisplayOrientation() {
        this.mDisplayRotation = CameraUtil.getDisplayRotation(this.mActivity);
        this.mDisplayOrientation = CameraUtil.getDisplayOrientation(this.mDisplayRotation, this.mCameraId);
        this.mCameraDisplayOrientation = this.mDisplayOrientation;
        if (this.mUI != null) {
            this.mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    PhotoModule.this.mUI.setDisplayOrientation(PhotoModule.this.mDisplayOrientation);
                }
            });
        }
        FocusOverlayManager focusOverlayManager = this.mFocusManager;
        if (focusOverlayManager != null) {
            focusOverlayManager.setDisplayOrientation(this.mDisplayOrientation);
        }
        CameraProxy cameraProxy = this.mCameraDevice;
        if (cameraProxy != null) {
            cameraProxy.setDisplayOrientation(this.mCameraDisplayOrientation);
        }
    }

    /* access modifiers changed from: private */
    public void setupPreview() {
        this.mFocusManager.resetTouchFocus();
        startPreview();
    }

    /* access modifiers changed from: private */
    public void startPreview() {
        if (!this.mPaused) {
            CameraProxy cameraProxy = this.mCameraDevice;
            if (!(cameraProxy == null || this.mParameters == null)) {
                synchronized (cameraProxy) {
                    SurfaceHolder surfaceHolder = null;
                    Log.v(TAG, "startPreview: SurfaceHolder (MDP path)");
                    if (this.mUI != null) {
                        surfaceHolder = this.mUI.getSurfaceHolder();
                    }
                    this.mCameraDevice.setPreviewDisplay(surfaceHolder);
                }
                if (!this.mCameraPreviewParamsReady) {
                    Log.w(TAG, "startPreview: parameters for preview are not ready.");
                    return;
                }
                this.mErrorCallback.setActivity(this.mActivity);
                this.mCameraDevice.setErrorCallback(this.mErrorCallback);
                int i = this.mCameraState;
                if (!(i == 0 || i == -1)) {
                    stopPreview();
                }
                if (!this.mSnapshotOnIdle) {
                    this.mFocusManager.setAeAwbLock(false);
                }
                setCameraParameters(-1);
                this.mCameraDevice.setOneShotPreviewCallback(this.mHandler, new CameraPreviewDataCallback() {
                    public void onPreviewFrame(byte[] bArr, CameraProxy cameraProxy) {
                        PhotoModule.this.mUI.hidePreviewCover();
                    }
                });
                this.mCameraDevice.startPreview();
                this.mHandler.sendEmptyMessage(13);
                setDisplayOrientation();
                if (this.mSnapshotOnIdle || this.mInstantCaptureSnapShot) {
                    Log.v(TAG, "Trigger snapshot from start preview.");
                    this.mHandler.sendEmptyMessageDelayed(14, 1500);
                } else {
                    if (CameraUtil.FOCUS_MODE_CONTINUOUS_PICTURE.equals(this.mFocusManager.getFocusMode()) && this.mCameraState != -1) {
                        this.mCameraDevice.cancelAutoFocus();
                    }
                }
            }
        }
    }

    public void stopPreview() {
        if (!(this.mCameraDevice == null || this.mCameraState == 0)) {
            Log.v(TAG, "stopPreview");
            this.mCameraDevice.stopPreview();
        }
        setCameraState(0);
        FocusOverlayManager focusOverlayManager = this.mFocusManager;
        if (focusOverlayManager != null) {
            focusOverlayManager.onPreviewStopped();
        }
        stopFaceDetection();
    }

    private void updateCameraParametersInitialize() {
        int[] photoPreviewFpsRange = CameraUtil.getPhotoPreviewFpsRange(this.mParameters);
        if (photoPreviewFpsRange != null && photoPreviewFpsRange.length > 0) {
            this.mParameters.setPreviewFpsRange(photoPreviewFpsRange[0], photoPreviewFpsRange[1]);
        }
        Parameters parameters = this.mParameters;
        String str = CameraUtil.FALSE;
        parameters.set(CameraUtil.RECORDING_HINT, str);
        if (CameraUtil.TRUE.equals(this.mParameters.get("video-stabilization-supported"))) {
            this.mParameters.set("video-stabilization", str);
        }
    }

    private void updateCameraParametersZoom() {
        if (this.mParameters.isZoomSupported()) {
            this.mZoomValue = this.mCameraDevice.getParameters().getZoom();
            this.mParameters.setZoom(this.mZoomValue);
        }
    }

    private boolean needRestart() {
        this.mRestartPreview = false;
        String string = this.mPreferences.getString(CameraSettings.KEY_ZSL, this.mActivity.getString(C0905R.string.pref_camera_zsl_default));
        boolean equals = string.equals(RecordLocationPreference.VALUE_ON);
        String str = TAG;
        if (equals && this.mSnapshotMode != CameraInfoWrapper.CAMERA_SUPPORT_MODE_ZSL && this.mCameraState != 0) {
            Log.v(str, "Switching to ZSL Camera Mode. Restart Preview");
            this.mRestartPreview = true;
            return this.mRestartPreview;
        } else if (!string.equals("off") || this.mSnapshotMode == CameraInfoWrapper.CAMERA_SUPPORT_MODE_NONZSL || this.mCameraState == 0) {
            return this.mRestartPreview;
        } else {
            Log.v(str, "Switching to Normal Camera Mode. Restart Preview");
            this.mRestartPreview = true;
            return this.mRestartPreview;
        }
    }

    private boolean isInstantCaptureEnabled() {
        return !this.mPreferences.getString(CameraSettings.KEY_INSTANT_CAPTURE, this.mActivity.getString(C0905R.string.pref_camera_instant_capture_default)).equals(this.mActivity.getString(C0905R.string.pref_camera_instant_capture_value_disable));
    }

    private void qcomUpdateAdvancedFeatures(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8) {
        if (CameraUtil.isSupported(str, CameraSettings.getSupportedAFBracketingModes(this.mParameters))) {
            this.mParameters.set(CameraSettings.KEY_QC_AF_BRACKETING, str);
        }
        if (CameraUtil.isSupported(str2, CameraSettings.getSupportedChromaFlashModes(this.mParameters))) {
            this.mParameters.set(CameraSettings.KEY_QC_CHROMA_FLASH, str2);
        }
        if (CameraUtil.isSupported(str4, CameraSettings.getSupportedOptiZoomModes(this.mParameters))) {
            this.mParameters.set(CameraSettings.KEY_QC_OPTI_ZOOM, str4);
        }
        if (CameraUtil.isSupported(str3, CameraSettings.getSupportedRefocusModes(this.mParameters))) {
            this.mParameters.set(CameraSettings.KEY_QC_RE_FOCUS, str3);
        }
        if (CameraUtil.isSupported(str5, CameraSettings.getSupportedFSSRModes(this.mParameters))) {
            this.mParameters.set(CameraSettings.KEY_QC_FSSR, str5);
        }
        if (CameraUtil.isSupported(str6, CameraSettings.getSupportedTruePortraitModes(this.mParameters))) {
            this.mParameters.set(CameraSettings.KEY_QC_TP, str6);
        }
        if (CameraUtil.isSupported(str7, CameraSettings.getSupportedMultiTouchFocusModes(this.mParameters))) {
            this.mParameters.set(CameraSettings.KEY_QC_MULTI_TOUCH_FOCUS, str7);
        }
        if (CameraUtil.isSupported(str8, CameraSettings.getSupportedStillMoreModes(this.mParameters))) {
            this.mParameters.set(CameraSettings.KEY_QC_STILL_MORE, str8);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:100:0x045e  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x05c5  */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x05dd  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x0606  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x0682  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0090 A[Catch:{ Exception -> 0x0096 }] */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x06b8  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x06e7  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0093 A[Catch:{ Exception -> 0x0096 }] */
    /* JADX WARNING: Removed duplicated region for block: B:175:0x07a1  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x07e5  */
    /* JADX WARNING: Removed duplicated region for block: B:194:0x0870  */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x0884  */
    /* JADX WARNING: Removed duplicated region for block: B:208:0x0916  */
    /* JADX WARNING: Removed duplicated region for block: B:223:0x0968  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0104  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x010a  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x016c  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0179  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x01ac  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x01b7  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x020a  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x02f1  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0329  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0357  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0379  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x03f6  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x042e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void qcomUpdateCameraParametersPreference() {
        /*
            r21 = this;
            r9 = r21
            java.lang.String r10 = "CAM_PhotoModule"
            android.hardware.Camera$Parameters r0 = r9.mParameters
            int r1 = r9.mbrightness
            java.lang.String r1 = java.lang.String.valueOf(r1)
            java.lang.String r2 = "luma-adaptation"
            r0.set(r2, r1)
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r1 = r9.mActivity
            r2 = 2131690160(0x7f0f02b0, float:1.9009356E38)
            java.lang.String r1 = r1.getString(r2)
            java.lang.String r2 = "pref_camera_longshot_key"
            java.lang.String r0 = r0.getString(r2, r1)
            android.hardware.Camera$Parameters r1 = r9.mParameters
            java.lang.String r11 = "long-shot"
            r1.set(r11, r0)
            com.android.camera.CameraActivity r0 = r9.mActivity
            r1 = 2131689946(0x7f0f01da, float:1.9008922E38)
            java.lang.String r0 = r0.getString(r1)
            java.lang.String r2 = r9.mSceneMode
            java.lang.String r12 = "auto"
            boolean r2 = r12.equals(r2)
            if (r2 != 0) goto L_0x005c
            java.lang.String r2 = r9.mSceneMode
            java.lang.String r3 = "hdr"
            boolean r2 = r3.equals(r2)
            if (r2 != 0) goto L_0x005c
            java.lang.String r2 = r9.mSceneMode
            boolean r0 = r0.equals(r2)
            if (r0 == 0) goto L_0x004f
            goto L_0x005c
        L_0x004f:
            android.hardware.Camera$Parameters r0 = r9.mParameters
            java.lang.String r2 = org.codeaurora.snapcam.wrapper.ParametersWrapper.TOUCH_AF_AEC_OFF
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setTouchAfAec(r0, r2)
            com.android.camera.FocusOverlayManager r0 = r9.mFocusManager
            r0.resetTouchFocus()
            goto L_0x0080
        L_0x005c:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690502(0x7f0f0406, float:1.901005E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_touchafaec_key"
            java.lang.String r0 = r0.getString(r3, r2)
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.util.List r2 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedTouchAfAec(r2)
            boolean r2 = com.android.camera.util.CameraUtil.isSupported(r0, r2)
            if (r2 == 0) goto L_0x0080
            r9.mCurrTouchAfAec = r0
            android.hardware.Camera$Parameters r2 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setTouchAfAec(r2, r0)
        L_0x0080:
            r13 = 0
            r14 = 1
            android.hardware.Camera$Parameters r0 = r9.mParameters     // Catch:{ Exception -> 0x0096 }
            java.lang.String r0 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getTouchAfAec(r0)     // Catch:{ Exception -> 0x0096 }
            java.lang.String r2 = org.codeaurora.snapcam.wrapper.ParametersWrapper.TOUCH_AF_AEC_ON     // Catch:{ Exception -> 0x0096 }
            boolean r0 = r0.equals(r2)     // Catch:{ Exception -> 0x0096 }
            if (r0 == 0) goto L_0x0093
            r9.mTouchAfAecFlag = r14     // Catch:{ Exception -> 0x0096 }
            goto L_0x009b
        L_0x0093:
            r9.mTouchAfAecFlag = r13     // Catch:{ Exception -> 0x0096 }
            goto L_0x009b
        L_0x0096:
            java.lang.String r0 = "Handled NULL pointer Exception"
            android.util.Log.e(r10, r0)
        L_0x009b:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690199(0x7f0f02d7, float:1.9009435E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_pictureformat_key"
            java.lang.String r0 = r0.getString(r3, r2)
            boolean r2 = r9.mIsImageCaptureIntent
            java.lang.String r15 = "jpeg"
            if (r2 == 0) goto L_0x00cf
            boolean r2 = r0.equals(r15)
            if (r2 != 0) goto L_0x00cf
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            android.content.SharedPreferences$Editor r0 = r0.edit()
            com.android.camera.CameraActivity r2 = r9.mActivity
            r4 = 2131690272(0x7f0f0320, float:1.9009583E38)
            java.lang.String r2 = r2.getString(r4)
            r0.putString(r3, r2)
            r0.apply()
            r8 = r15
            goto L_0x00d0
        L_0x00cf:
            r8 = r0
        L_0x00d0:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "Picture format value ="
            r0.append(r2)
            r0.append(r8)
            java.lang.String r0 = r0.toString()
            android.util.Log.v(r10, r0)
            android.hardware.Camera$Parameters r0 = r9.mParameters
            java.lang.String r7 = "picture-format"
            r0.set(r7, r8)
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690151(0x7f0f02a7, float:1.9009338E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_jpegquality_key"
            java.lang.String r0 = r0.getString(r3, r2)
            android.hardware.Camera$Parameters r2 = r9.mParameters
            android.hardware.Camera$Size r2 = r2.getPictureSize()
            if (r2 != 0) goto L_0x010a
            java.lang.String r0 = "error getPictureSize: size is null"
            android.util.Log.e(r10, r0)
            goto L_0x014a
        L_0x010a:
            java.lang.String r3 = "100"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0123
            int r3 = r2.width
            r4 = 3200(0xc80, float:4.484E-42)
            if (r3 < r4) goto L_0x0123
            android.os.Handler r0 = r9.mHandler
            com.android.camera.PhotoModule$11 r2 = new com.android.camera.PhotoModule$11
            r2.<init>()
            r0.post(r2)
            goto L_0x014a
        L_0x0123:
            android.os.Handler r3 = r9.mHandler
            com.android.camera.PhotoModule$12 r4 = new com.android.camera.PhotoModule$12
            r4.<init>()
            r3.post(r4)
            android.hardware.Camera$Parameters r3 = r9.mParameters
            int r4 = com.android.camera.JpegEncodingQualityMappings.getQualityNumber(r0)
            r3.setJpegQuality(r4)
            int r0 = r9.estimateJpegFileSize(r2, r0)
            int r2 = r9.mJpegFileSizeEstimation
            if (r0 == r2) goto L_0x014a
            r9.mJpegFileSizeEstimation = r0
            android.os.Handler r0 = r9.mHandler
            com.android.camera.PhotoModule$13 r2 = new com.android.camera.PhotoModule$13
            r2.<init>()
            r0.post(r2)
        L_0x014a:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690458(0x7f0f03da, float:1.900996E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_selectablezoneaf_key"
            java.lang.String r0 = r0.getString(r3, r2)
            android.hardware.Camera$Parameters r2 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedSelectableZoneAf(r2)
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.util.List r2 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedSelectableZoneAf(r2)
            boolean r2 = com.android.camera.util.CameraUtil.isSupported(r0, r2)
            if (r2 == 0) goto L_0x0171
            android.hardware.Camera$Parameters r2 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setSelectableZoneAf(r2, r0)
        L_0x0171:
            android.hardware.Camera$Parameters r0 = r9.mParameters
            java.util.List r0 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedDenoiseModes(r0)
            if (r0 == 0) goto L_0x018f
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690043(0x7f0f023b, float:1.9009118E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_denoise_key"
            java.lang.String r0 = r0.getString(r3, r2)
            android.hardware.Camera$Parameters r2 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setDenoise(r2, r0)
        L_0x018f:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690383(0x7f0f038f, float:1.9009808E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_redeyereduction_key"
            java.lang.String r0 = r0.getString(r3, r2)
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.util.List r2 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedRedeyeReductionModes(r2)
            boolean r2 = com.android.camera.util.CameraUtil.isSupported(r0, r2)
            if (r2 == 0) goto L_0x01b1
            android.hardware.Camera$Parameters r2 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setRedeyeReductionMode(r2, r0)
        L_0x01b1:
            int r0 = r9.mManual3AEnabled
            r0 = r0 & 4
            if (r0 != 0) goto L_0x01d9
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690135(0x7f0f0297, float:1.9009305E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_iso_key"
            java.lang.String r0 = r0.getString(r3, r2)
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.util.List r2 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedIsoValues(r2)
            boolean r2 = com.android.camera.util.CameraUtil.isSupported(r0, r2)
            if (r2 == 0) goto L_0x01d9
            android.hardware.Camera$Parameters r2 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setISOValue(r2, r0)
        L_0x01d9:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690008(0x7f0f0218, float:1.9009047E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_coloreffect_key"
            java.lang.String r0 = r0.getString(r3, r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Color effect value ="
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.v(r10, r2)
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.util.List r2 = r2.getSupportedColorEffects()
            boolean r2 = com.android.camera.util.CameraUtil.isSupported(r0, r2)
            if (r2 == 0) goto L_0x020f
            android.hardware.Camera$Parameters r2 = r9.mParameters
            r2.setColorEffect(r0)
        L_0x020f:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690387(0x7f0f0393, float:1.9009816E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_saturation_key"
            java.lang.String r0 = r0.getString(r3, r2)
            int r0 = java.lang.Integer.parseInt(r0)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Saturation value ="
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.v(r10, r2)
            if (r0 < 0) goto L_0x0247
            android.hardware.Camera$Parameters r2 = r9.mParameters
            int r2 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getMaxSaturation(r2)
            if (r0 > r2) goto L_0x0247
            android.hardware.Camera$Parameters r2 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setSaturation(r2, r0)
        L_0x0247:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690025(0x7f0f0229, float:1.9009082E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_contrast_key"
            java.lang.String r0 = r0.getString(r3, r2)
            int r0 = java.lang.Integer.parseInt(r0)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Contrast value ="
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.v(r10, r2)
            if (r0 < 0) goto L_0x027f
            android.hardware.Camera$Parameters r2 = r9.mParameters
            int r2 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getMaxContrast(r2)
            if (r0 > r2) goto L_0x027f
            android.hardware.Camera$Parameters r2 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setContrast(r2, r0)
        L_0x027f:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690473(0x7f0f03e9, float:1.900999E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_sharpness_key"
            java.lang.String r0 = r0.getString(r3, r2)
            int r0 = java.lang.Integer.parseInt(r0)
            android.hardware.Camera$Parameters r2 = r9.mParameters
            int r2 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getMaxSharpness(r2)
            int r2 = r2 / 6
            int r0 = r0 * r2
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Sharpness value ="
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.v(r10, r2)
            if (r0 < 0) goto L_0x02c0
            android.hardware.Camera$Parameters r2 = r9.mParameters
            int r2 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getMaxSharpness(r2)
            if (r0 > r2) goto L_0x02c0
            android.hardware.Camera$Parameters r2 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setSharpness(r2, r0)
        L_0x02c0:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690057(0x7f0f0249, float:1.9009147E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_facerc_key"
            java.lang.String r0 = r0.getString(r3, r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Face Recognition value = "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.v(r10, r2)
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.util.List r2 = com.android.camera.CameraSettings.getSupportedFaceRecognitionModes(r2)
            boolean r2 = com.android.camera.util.CameraUtil.isSupported(r0, r2)
            if (r2 == 0) goto L_0x02f8
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.lang.String r3 = "face-recognition"
            r2.set(r3, r0)
        L_0x02f8:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131689956(0x7f0f01e4, float:1.9008942E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_ae_bracket_hdr_key"
            java.lang.String r0 = r0.getString(r3, r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "AE Bracketing value ="
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.v(r10, r2)
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.util.List r2 = com.android.camera.CameraSettings.getSupportedAEBracketingModes(r2)
            boolean r2 = com.android.camera.util.CameraUtil.isSupported(r0, r2)
            if (r2 == 0) goto L_0x0330
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.lang.String r3 = "ae-bracket-hdr"
            r2.set(r3, r0)
        L_0x0330:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690000(0x7f0f0210, float:1.9009031E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_cds_mode_key"
            java.lang.String r0 = r0.getString(r3, r2)
            java.lang.String r2 = r9.mPrevSavedCDS
            if (r2 != 0) goto L_0x0349
            if (r0 == 0) goto L_0x0349
            r9.mPrevSavedCDS = r0
        L_0x0349:
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.util.List r2 = com.android.camera.CameraSettings.getSupportedCDSModes(r2)
            boolean r2 = com.android.camera.util.CameraUtil.isSupported(r0, r2)
            java.lang.String r3 = "cds-mode"
            if (r2 == 0) goto L_0x035c
            android.hardware.Camera$Parameters r2 = r9.mParameters
            r2.set(r3, r0)
        L_0x035c:
            com.android.camera.ComboPreferences r2 = r9.mPreferences
            com.android.camera.CameraActivity r4 = r9.mActivity
            r5 = 2131690496(0x7f0f0400, float:1.9010037E38)
            java.lang.String r4 = r4.getString(r5)
            java.lang.String r5 = "pref_camera_tnr_mode_key"
            java.lang.String r2 = r2.getString(r5, r4)
            android.hardware.Camera$Parameters r4 = r9.mParameters
            java.util.List r4 = com.android.camera.CameraSettings.getSupportedTNRModes(r4)
            boolean r4 = com.android.camera.util.CameraUtil.isSupported(r2, r4)
            if (r4 == 0) goto L_0x03c5
            com.android.camera.CameraActivity r4 = r9.mActivity
            r5 = 2131690500(0x7f0f0404, float:1.9010045E38)
            java.lang.String r4 = r4.getString(r5)
            boolean r4 = r2.equals(r4)
            if (r4 != 0) goto L_0x03a7
            android.hardware.Camera$Parameters r4 = r9.mParameters
            com.android.camera.CameraActivity r5 = r9.mActivity
            r6 = 2131690006(0x7f0f0216, float:1.9009043E38)
            java.lang.String r5 = r5.getString(r6)
            r4.set(r3, r5)
            com.android.camera.CameraActivity r3 = r9.mActivity
            com.android.camera.PhotoModule$14 r4 = new com.android.camera.PhotoModule$14
            r4.<init>()
            r3.runOnUiThread(r4)
            if (r0 == 0) goto L_0x03a4
            r9.mPrevSavedCDS = r0
        L_0x03a4:
            r9.isTNREnabled = r14
            goto L_0x03be
        L_0x03a7:
            boolean r0 = r9.isTNREnabled
            if (r0 == 0) goto L_0x03be
            android.hardware.Camera$Parameters r0 = r9.mParameters
            java.lang.String r4 = r9.mPrevSavedCDS
            r0.set(r3, r4)
            com.android.camera.CameraActivity r0 = r9.mActivity
            com.android.camera.PhotoModule$15 r3 = new com.android.camera.PhotoModule$15
            r3.<init>()
            r0.runOnUiThread(r3)
            r9.isTNREnabled = r13
        L_0x03be:
            android.hardware.Camera$Parameters r0 = r9.mParameters
            java.lang.String r3 = "tnr-mode"
            r0.set(r3, r2)
        L_0x03c5:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690085(0x7f0f0265, float:1.9009204E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_hdr_mode_key"
            java.lang.String r0 = r0.getString(r3, r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "HDR Mode value ="
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.v(r10, r2)
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.util.List r2 = com.android.camera.CameraSettings.getSupportedHDRModes(r2)
            boolean r2 = com.android.camera.util.CameraUtil.isSupported(r0, r2)
            if (r2 == 0) goto L_0x03fd
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.lang.String r3 = "hdr-mode"
            r2.set(r3, r0)
        L_0x03fd:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131690089(0x7f0f0269, float:1.9009212E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_hdr_need_1x_key"
            java.lang.String r0 = r0.getString(r3, r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "HDR need 1x value ="
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.v(r10, r2)
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.util.List r2 = com.android.camera.CameraSettings.getSupportedHDRNeed1x(r2)
            boolean r2 = com.android.camera.util.CameraUtil.isSupported(r0, r2)
            if (r2 == 0) goto L_0x0435
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.lang.String r3 = "hdr-need-1x"
            r2.set(r3, r0)
        L_0x0435:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r2 = r9.mActivity
            r3 = 2131689928(0x7f0f01c8, float:1.9008885E38)
            java.lang.String r2 = r2.getString(r3)
            java.lang.String r3 = "pref_camera_advanced_features_key"
            java.lang.String r6 = r0.getString(r3, r2)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = " advancedFeature value ="
            r0.append(r2)
            r0.append(r6)
            java.lang.String r0 = r0.toString()
            android.util.Log.e(r10, r0)
            r9.mRefocus = r13
            if (r6 == 0) goto L_0x05c5
            com.android.camera.CameraActivity r0 = r9.mActivity
            r2 = 2131689953(0x7f0f01e1, float:1.9008936E38)
            java.lang.String r2 = r0.getString(r2)
            com.android.camera.CameraActivity r0 = r9.mActivity
            r3 = 2131689940(0x7f0f01d4, float:1.900891E38)
            java.lang.String r3 = r0.getString(r3)
            com.android.camera.CameraActivity r0 = r9.mActivity
            r4 = 2131689945(0x7f0f01d9, float:1.900892E38)
            java.lang.String r4 = r0.getString(r4)
            com.android.camera.CameraActivity r0 = r9.mActivity
            r5 = 2131689947(0x7f0f01db, float:1.9008924E38)
            java.lang.String r5 = r0.getString(r5)
            com.android.camera.CameraActivity r0 = r9.mActivity
            r13 = 2131689938(0x7f0f01d2, float:1.9008906E38)
            java.lang.String r13 = r0.getString(r13)
            com.android.camera.CameraActivity r0 = r9.mActivity
            r1 = 2131689951(0x7f0f01df, float:1.9008932E38)
            java.lang.String r16 = r0.getString(r1)
            com.android.camera.CameraActivity r0 = r9.mActivity
            r1 = 2131689942(0x7f0f01d6, float:1.9008914E38)
            java.lang.String r17 = r0.getString(r1)
            com.android.camera.CameraActivity r0 = r9.mActivity
            r1 = 2131689949(0x7f0f01dd, float:1.9008928E38)
            java.lang.String r18 = r0.getString(r1)
            com.android.camera.CameraActivity r0 = r9.mActivity
            r1 = 2131689954(0x7f0f01e2, float:1.9008938E38)
            java.lang.String r0 = r0.getString(r1)
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x04cb
            r0 = r21
            r1 = r6
            r2 = r3
            r3 = r5
            r5 = r13
            r13 = r6
            r6 = r16
            r19 = r7
            r7 = r17
            r20 = r8
            r8 = r18
            r0.qcomUpdateAdvancedFeatures(r1, r2, r3, r4, r5, r6, r7, r8)
            goto L_0x05ca
        L_0x04cb:
            r19 = r7
            r20 = r8
            r8 = r6
            com.android.camera.CameraActivity r0 = r9.mActivity
            r1 = 2131689941(0x7f0f01d5, float:1.9008912E38)
            java.lang.String r0 = r0.getString(r1)
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x04f1
            r0 = r21
            r1 = r2
            r2 = r8
            r3 = r5
            r5 = r13
            r6 = r16
            r7 = r17
            r13 = r8
            r8 = r18
            r0.qcomUpdateAdvancedFeatures(r1, r2, r3, r4, r5, r6, r7, r8)
            goto L_0x05ca
        L_0x04f1:
            com.android.camera.CameraActivity r0 = r9.mActivity
            r1 = 2131689948(0x7f0f01dc, float:1.9008926E38)
            java.lang.String r0 = r0.getString(r1)
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0514
            r0 = r21
            r1 = r2
            r2 = r3
            r3 = r8
            r5 = r13
            r6 = r16
            r7 = r17
            r13 = r8
            r8 = r18
            r0.qcomUpdateAdvancedFeatures(r1, r2, r3, r4, r5, r6, r7, r8)
            r9.mRefocus = r14
            goto L_0x05ca
        L_0x0514:
            com.android.camera.CameraActivity r0 = r9.mActivity
            r1 = 2131689946(0x7f0f01da, float:1.9008922E38)
            java.lang.String r0 = r0.getString(r1)
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0536
            r0 = r21
            r1 = r2
            r2 = r3
            r3 = r5
            r4 = r8
            r5 = r13
            r6 = r16
            r7 = r17
            r13 = r8
            r8 = r18
            r0.qcomUpdateAdvancedFeatures(r1, r2, r3, r4, r5, r6, r7, r8)
            goto L_0x05ca
        L_0x0536:
            com.android.camera.CameraActivity r0 = r9.mActivity
            r1 = 2131689939(0x7f0f01d3, float:1.9008908E38)
            java.lang.String r0 = r0.getString(r1)
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0557
            r0 = r21
            r1 = r2
            r2 = r3
            r3 = r5
            r5 = r8
            r6 = r16
            r7 = r17
            r13 = r8
            r8 = r18
            r0.qcomUpdateAdvancedFeatures(r1, r2, r3, r4, r5, r6, r7, r8)
            goto L_0x05ca
        L_0x0557:
            com.android.camera.CameraActivity r0 = r9.mActivity
            r1 = 2131689952(0x7f0f01e0, float:1.9008934E38)
            java.lang.String r0 = r0.getString(r1)
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0577
            r0 = r21
            r1 = r2
            r2 = r3
            r3 = r5
            r5 = r13
            r6 = r8
            r7 = r17
            r13 = r8
            r8 = r18
            r0.qcomUpdateAdvancedFeatures(r1, r2, r3, r4, r5, r6, r7, r8)
            goto L_0x05ca
        L_0x0577:
            com.android.camera.CameraActivity r0 = r9.mActivity
            r1 = 2131689943(0x7f0f01d7, float:1.9008916E38)
            java.lang.String r0 = r0.getString(r1)
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x0596
            r0 = r21
            r1 = r2
            r2 = r3
            r3 = r5
            r5 = r13
            r6 = r16
            r7 = r8
            r13 = r8
            r8 = r18
            r0.qcomUpdateAdvancedFeatures(r1, r2, r3, r4, r5, r6, r7, r8)
            goto L_0x05ca
        L_0x0596:
            com.android.camera.CameraActivity r0 = r9.mActivity
            r1 = 2131689950(0x7f0f01de, float:1.900893E38)
            java.lang.String r0 = r0.getString(r1)
            boolean r0 = r8.equals(r0)
            if (r0 == 0) goto L_0x05b4
            r0 = r21
            r1 = r2
            r2 = r3
            r3 = r5
            r5 = r13
            r6 = r16
            r7 = r17
            r13 = r8
            r0.qcomUpdateAdvancedFeatures(r1, r2, r3, r4, r5, r6, r7, r8)
            goto L_0x05ca
        L_0x05b4:
            r0 = r21
            r1 = r2
            r2 = r3
            r3 = r5
            r5 = r13
            r6 = r16
            r7 = r17
            r13 = r8
            r8 = r18
            r0.qcomUpdateAdvancedFeatures(r1, r2, r3, r4, r5, r6, r7, r8)
            goto L_0x05ca
        L_0x05c5:
            r13 = r6
            r19 = r7
            r20 = r8
        L_0x05ca:
            com.android.camera.CameraActivity r0 = r9.mActivity
            r1 = 2131689952(0x7f0f01e0, float:1.9008934E38)
            java.lang.String r0 = r0.getString(r1)
            boolean r0 = r0.equals(r13)
            java.lang.String r1 = "on"
            java.lang.String r2 = "off"
            if (r0 == 0) goto L_0x0606
            java.lang.String r0 = org.codeaurora.snapcam.wrapper.ParametersWrapper.FACE_DETECTION_ON
            android.hardware.Camera$Parameters r3 = r9.mParameters
            java.util.List r3 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedFaceDetectionModes(r3)
            boolean r0 = com.android.camera.util.CameraUtil.isSupported(r0, r3)
            if (r0 == 0) goto L_0x0651
            com.android.camera.CameraActivity r0 = r9.mActivity
            com.android.camera.PhotoModule$16 r3 = new com.android.camera.PhotoModule$16
            r3.<init>()
            r0.runOnUiThread(r3)
            android.hardware.Camera$Parameters r0 = r9.mParameters
            java.lang.String r3 = org.codeaurora.snapcam.wrapper.ParametersWrapper.FACE_DETECTION_ON
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setFaceDetectionMode(r0, r3)
            boolean r0 = r9.mFaceDetectionEnabled
            if (r0 != 0) goto L_0x0651
            r9.mFaceDetectionEnabled = r14
            r21.startFaceDetection()
            goto L_0x0651
        L_0x0606:
            com.android.camera.CameraActivity r0 = r9.mActivity
            com.android.camera.PhotoModule$17 r3 = new com.android.camera.PhotoModule$17
            r3.<init>()
            r0.runOnUiThread(r3)
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r3 = r9.mActivity
            r4 = 2131690053(0x7f0f0245, float:1.9009139E38)
            java.lang.String r3 = r3.getString(r4)
            java.lang.String r4 = "pref_camera_facedetection_key"
            java.lang.String r0 = r0.getString(r4, r3)
            android.hardware.Camera$Parameters r3 = r9.mParameters
            java.util.List r3 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedFaceDetectionModes(r3)
            boolean r3 = com.android.camera.util.CameraUtil.isSupported(r0, r3)
            if (r3 == 0) goto L_0x0651
            android.hardware.Camera$Parameters r3 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setFaceDetectionMode(r3, r0)
            boolean r3 = r0.equals(r1)
            if (r3 == 0) goto L_0x0641
            boolean r3 = r9.mFaceDetectionEnabled
            if (r3 != 0) goto L_0x0641
            r9.mFaceDetectionEnabled = r14
            r21.startFaceDetection()
        L_0x0641:
            boolean r0 = r0.equals(r2)
            if (r0 == 0) goto L_0x0651
            boolean r0 = r9.mFaceDetectionEnabled
            if (r0 != r14) goto L_0x0651
            r21.stopFaceDetection()
            r0 = 0
            r9.mFaceDetectionEnabled = r0
        L_0x0651:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r3 = r9.mActivity
            r4 = 2131689978(0x7f0f01fa, float:1.9008987E38)
            java.lang.String r3 = r3.getString(r4)
            java.lang.String r4 = "pref_camera_autoexposure_key"
            java.lang.String r0 = r0.getString(r4, r3)
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "autoExposure value ="
            r3.append(r4)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r10, r3)
            android.hardware.Camera$Parameters r3 = r9.mParameters
            java.util.List r3 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedAutoexposure(r3)
            boolean r3 = com.android.camera.util.CameraUtil.isSupported(r0, r3)
            if (r3 == 0) goto L_0x0687
            android.hardware.Camera$Parameters r3 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setAutoExposure(r3, r0)
        L_0x0687:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r3 = r9.mActivity
            r4 = 2131689962(0x7f0f01ea, float:1.9008954E38)
            java.lang.String r3 = r3.getString(r4)
            java.lang.String r4 = "pref_camera_antibanding_key"
            java.lang.String r0 = r0.getString(r4, r3)
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "antiBanding value ="
            r3.append(r4)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r10, r3)
            android.hardware.Camera$Parameters r3 = r9.mParameters
            java.util.List r3 = r3.getSupportedAntibanding()
            boolean r3 = com.android.camera.util.CameraUtil.isSupported(r0, r3)
            if (r3 == 0) goto L_0x06bd
            android.hardware.Camera$Parameters r3 = r9.mParameters
            r3.setAntibanding(r0)
        L_0x06bd:
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            com.android.camera.CameraActivity r3 = r9.mActivity
            r4 = 2131690589(0x7f0f045d, float:1.9010226E38)
            java.lang.String r3 = r3.getString(r4)
            java.lang.String r4 = "pref_camera_zsl_key"
            java.lang.String r0 = r0.getString(r4, r3)
            com.android.camera.ComboPreferences r3 = r9.mPreferences
            com.android.camera.CameraActivity r4 = r9.mActivity
            r5 = 2131690083(0x7f0f0263, float:1.90092E38)
            java.lang.String r4 = r4.getString(r5)
            java.lang.String r5 = "pref_camera_auto_hdr_key"
            java.lang.String r3 = r3.getString(r5, r4)
            android.hardware.Camera$Parameters r4 = r9.mParameters
            boolean r4 = com.android.camera.util.CameraUtil.isAutoHDRSupported(r4)
            if (r4 == 0) goto L_0x071c
            android.hardware.Camera$Parameters r4 = r9.mParameters
            java.lang.String r5 = "auto-hdr-enable"
            r4.set(r5, r3)
            java.lang.String r4 = "enable"
            boolean r3 = r3.equals(r4)
            if (r3 == 0) goto L_0x070f
            com.android.camera.CameraActivity r3 = r9.mActivity
            com.android.camera.PhotoModule$18 r4 = new com.android.camera.PhotoModule$18
            r4.<init>()
            r3.runOnUiThread(r4)
            android.hardware.Camera$Parameters r3 = r9.mParameters
            java.lang.String r4 = "asd"
            r3.setSceneMode(r4)
            com.android.camera.CameraManager$CameraProxy r3 = r9.mCameraDevice
            com.android.camera.PhotoModule$MetaDataCallback r4 = r9.mMetaDataCallback
            r3.setMetadataCb(r4)
            goto L_0x071c
        L_0x070f:
            r3 = 0
            r9.mAutoHdrEnable = r3
            com.android.camera.CameraActivity r3 = r9.mActivity
            com.android.camera.PhotoModule$19 r4 = new com.android.camera.PhotoModule$19
            r4.<init>()
            r3.runOnUiThread(r4)
        L_0x071c:
            android.hardware.Camera$Parameters r3 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setZSLMode(r3, r0)
            boolean r3 = r0.equals(r1)
            r4 = 0
            java.lang.String r5 = "continuous-picture"
            if (r3 == 0) goto L_0x079b
            android.hardware.Camera$Parameters r3 = r9.mParameters
            java.util.List r3 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedZSLModes(r3)
            if (r3 == 0) goto L_0x079b
            int r3 = org.codeaurora.snapcam.wrapper.CameraInfoWrapper.CAMERA_SUPPORT_MODE_ZSL
            r9.mSnapshotMode = r3
            android.hardware.Camera$Parameters r3 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setCameraMode(r3, r14)
            com.android.camera.FocusOverlayManager r3 = r9.mFocusManager
            r3.setZslEnable(r14)
            android.hardware.Camera$Parameters r3 = r9.mParameters
            r6 = r19
            r3.set(r6, r15)
            android.hardware.Camera$Parameters r3 = r9.mParameters
            java.util.List r3 = r3.getSupportedFocusModes()
            boolean r3 = com.android.camera.util.CameraUtil.isSupported(r5, r3)
            if (r3 == 0) goto L_0x0766
            com.android.camera.FocusOverlayManager r3 = r9.mFocusManager
            boolean r3 = r3.isTouch()
            if (r3 != 0) goto L_0x0766
            com.android.camera.FocusOverlayManager r3 = r9.mFocusManager
            r3.overrideFocusMode(r5)
            android.hardware.Camera$Parameters r3 = r9.mParameters
            r3.setFocusMode(r5)
            goto L_0x0788
        L_0x0766:
            com.android.camera.FocusOverlayManager r3 = r9.mFocusManager
            boolean r3 = r3.isTouch()
            if (r3 == 0) goto L_0x077f
            com.android.camera.FocusOverlayManager r3 = r9.mFocusManager
            r3.overrideFocusMode(r4)
            android.hardware.Camera$Parameters r3 = r9.mParameters
            com.android.camera.FocusOverlayManager r6 = r9.mFocusManager
            java.lang.String r6 = r6.getFocusMode()
            r3.setFocusMode(r6)
            goto L_0x0788
        L_0x077f:
            com.android.camera.FocusOverlayManager r3 = r9.mFocusManager
            java.lang.String r6 = r3.getFocusMode()
            r3.overrideFocusMode(r6)
        L_0x0788:
            r3 = r20
            boolean r3 = r3.equals(r15)
            if (r3 != 0) goto L_0x07c5
            com.android.camera.CameraActivity r3 = r9.mActivity
            com.android.camera.PhotoModule$20 r6 = new com.android.camera.PhotoModule$20
            r6.<init>()
            r3.runOnUiThread(r6)
            goto L_0x07c5
        L_0x079b:
            boolean r3 = r0.equals(r2)
            if (r3 == 0) goto L_0x07c5
            int r3 = org.codeaurora.snapcam.wrapper.CameraInfoWrapper.CAMERA_SUPPORT_MODE_NONZSL
            r9.mSnapshotMode = r3
            android.hardware.Camera$Parameters r3 = r9.mParameters
            r6 = 0
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setCameraMode(r3, r6)
            com.android.camera.FocusOverlayManager r3 = r9.mFocusManager
            r3.setZslEnable(r6)
            int r3 = r9.mManual3AEnabled
            r3 = r3 & r14
            if (r3 != 0) goto L_0x07c5
            com.android.camera.FocusOverlayManager r3 = r9.mFocusManager
            r3.overrideFocusMode(r4)
            android.hardware.Camera$Parameters r3 = r9.mParameters
            com.android.camera.FocusOverlayManager r6 = r9.mFocusManager
            java.lang.String r6 = r6.getFocusMode()
            r3.setFocusMode(r6)
        L_0x07c5:
            com.android.camera.ComboPreferences r3 = r9.mPreferences
            com.android.camera.CameraActivity r6 = r9.mActivity
            r7 = 2131690126(0x7f0f028e, float:1.9009287E38)
            java.lang.String r6 = r6.getString(r7)
            java.lang.String r7 = "pref_camera_instant_capture_key"
            java.lang.String r3 = r3.getString(r7, r6)
            com.android.camera.CameraActivity r6 = r9.mActivity
            r7 = 2131690133(0x7f0f0295, float:1.90093E38)
            java.lang.String r6 = r6.getString(r7)
            boolean r6 = r3.equals(r6)
            if (r6 != 0) goto L_0x0822
            boolean r6 = r0.equals(r1)
            if (r6 == 0) goto L_0x0805
            com.android.camera.CameraActivity r6 = r9.mActivity
            r8 = 2131689944(0x7f0f01d8, float:1.9008918E38)
            java.lang.String r6 = r6.getString(r8)
            boolean r6 = r13.equals(r6)
            if (r6 == 0) goto L_0x0805
            boolean r6 = r9.mInstantCaptureSnapShot
            if (r6 != 0) goto L_0x0822
            com.android.camera.CameraActivity r3 = r9.mActivity
            java.lang.String r3 = r3.getString(r7)
            goto L_0x0822
        L_0x0805:
            android.hardware.Camera$Parameters r3 = r9.mParameters
            com.android.camera.CameraActivity r6 = r9.mActivity
            java.lang.String r6 = r6.getString(r7)
            java.lang.String r8 = "instant-capture"
            r3.set(r8, r6)
            com.android.camera.CameraActivity r3 = r9.mActivity
            java.lang.String r3 = r3.getString(r7)
            com.android.camera.CameraActivity r6 = r9.mActivity
            com.android.camera.PhotoModule$21 r7 = new com.android.camera.PhotoModule$21
            r7.<init>()
            r6.runOnUiThread(r7)
        L_0x0822:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "Instant capture = "
            r6.append(r7)
            r6.append(r3)
            java.lang.String r7 = ", mInstantCaptureSnapShot = "
            r6.append(r7)
            boolean r7 = r9.mInstantCaptureSnapShot
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            android.util.Log.v(r10, r6)
            android.hardware.Camera$Parameters r6 = r9.mParameters
            java.lang.String r7 = "instant-capture"
            r6.set(r7, r3)
            com.android.camera.ComboPreferences r3 = r9.mPreferences
            com.android.camera.CameraActivity r6 = r9.mActivity
            r7 = 2131690114(0x7f0f0282, float:1.9009262E38)
            java.lang.String r6 = r6.getString(r7)
            java.lang.String r7 = "pref_camera_histogram_key"
            java.lang.String r3 = r3.getString(r7, r6)
            android.hardware.Camera$Parameters r6 = r9.mParameters
            java.util.List r6 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedHistogramModes(r6)
            boolean r6 = com.android.camera.util.CameraUtil.isSupported(r3, r6)
            if (r6 == 0) goto L_0x0896
            com.android.camera.CameraManager$CameraProxy r6 = r9.mCameraDevice
            if (r6 == 0) goto L_0x0896
            java.lang.String r6 = "enable"
            boolean r3 = r3.equals(r6)
            if (r3 == 0) goto L_0x0884
            com.android.camera.CameraActivity r3 = r9.mActivity
            com.android.camera.PhotoModule$22 r4 = new com.android.camera.PhotoModule$22
            r4.<init>()
            r3.runOnUiThread(r4)
            com.android.camera.CameraManager$CameraProxy r3 = r9.mCameraDevice
            com.android.camera.PhotoModule$StatsCallback r4 = r9.mStatsCallback
            r3.setHistogramMode(r4)
            r9.mHiston = r14
            goto L_0x0896
        L_0x0884:
            r3 = 0
            r9.mHiston = r3
            com.android.camera.CameraActivity r3 = r9.mActivity
            com.android.camera.PhotoModule$23 r6 = new com.android.camera.PhotoModule$23
            r6.<init>()
            r3.runOnUiThread(r6)
            com.android.camera.CameraManager$CameraProxy r3 = r9.mCameraDevice
            r3.setHistogramMode(r4)
        L_0x0896:
            r21.setFlipValue()
            android.hardware.Camera$Parameters r3 = r9.mParameters
            java.lang.String r4 = "ae-bracket-hdr"
            java.lang.String r3 = r3.get(r4)
            if (r3 == 0) goto L_0x08ae
            boolean r3 = r3.equalsIgnoreCase(r2)
            if (r3 != 0) goto L_0x08ae
            android.hardware.Camera$Parameters r3 = r9.mParameters
            r3.setFlashMode(r2)
        L_0x08ae:
            com.android.camera.FocusOverlayManager r3 = r9.mFocusManager
            java.lang.String r3 = r3.getFocusMode()
            boolean r3 = r3.equals(r5)
            if (r3 != 0) goto L_0x08c7
            com.android.camera.FocusOverlayManager r3 = r9.mFocusManager
            boolean r3 = r3.isFocusCompleted()
            if (r3 != 0) goto L_0x08c7
            com.android.camera.PhotoUI r3 = r9.mUI
            r3.clearFocus()
        L_0x08c7:
            com.android.camera.ComboPreferences r3 = r9.mPreferences
            com.android.camera.CameraActivity r4 = r9.mActivity
            r5 = 2131689988(0x7f0f0204, float:1.9009007E38)
            java.lang.String r4 = r4.getString(r5)
            java.lang.String r5 = "pref_camera_bokeh_mode_key"
            java.lang.String r3 = r3.getString(r5, r4)
            com.android.camera.ComboPreferences r4 = r9.mPreferences
            com.android.camera.CameraActivity r5 = r9.mActivity
            r6 = 2131689994(0x7f0f020a, float:1.900902E38)
            java.lang.String r5 = r5.getString(r6)
            java.lang.String r6 = "pref_camera_bokeh_mpo_key"
            java.lang.String r4 = r4.getString(r6, r5)
            com.android.camera.CameraActivity r5 = r9.mActivity
            android.content.SharedPreferences r5 = android.preference.PreferenceManager.getDefaultSharedPreferences(r5)
            r6 = 50
            java.lang.String r7 = "pref_camera_bokeh_blur_degree_key"
            int r5 = r5.getInt(r7, r6)
            android.hardware.Camera$Parameters r6 = r9.mParameters
            boolean r6 = com.android.camera.CameraSettings.isBokehModeSupported(r6)
            com.android.camera.CameraActivity r7 = r9.mActivity
            com.android.camera.PhotoModule$24 r8 = new com.android.camera.PhotoModule$24
            r8.<init>(r6)
            r7.runOnUiThread(r8)
            com.android.camera.CameraActivity r6 = r9.mActivity
            r7 = 2131689991(0x7f0f0207, float:1.9009013E38)
            java.lang.String r6 = r6.getString(r7)
            boolean r6 = r3.equals(r6)
            if (r6 != 0) goto L_0x0968
            r9.mIsBokehMode = r14
            boolean r0 = r0.equals(r1)
            if (r0 != 0) goto L_0x0923
            android.hardware.Camera$Parameters r0 = r9.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setZSLMode(r0, r1)
        L_0x0923:
            android.hardware.Camera$Parameters r0 = r9.mParameters
            java.lang.String r0 = r0.getSceneMode()
            if (r0 == r12) goto L_0x0930
            android.hardware.Camera$Parameters r0 = r9.mParameters
            r0.setSceneMode(r12)
        L_0x0930:
            android.hardware.Camera$Parameters r0 = r9.mParameters
            java.lang.String r0 = r0.getFlashMode()
            if (r0 == r2) goto L_0x093d
            android.hardware.Camera$Parameters r0 = r9.mParameters
            r0.setFlashMode(r2)
        L_0x093d:
            android.hardware.Camera$Parameters r0 = r9.mParameters
            java.lang.String r0 = r0.get(r11)
            com.android.camera.CameraActivity r1 = r9.mActivity
            r2 = 2131690718(0x7f0f04de, float:1.9010488E38)
            java.lang.String r1 = r1.getString(r2)
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0960
            android.hardware.Camera$Parameters r0 = r9.mParameters
            com.android.camera.CameraActivity r1 = r9.mActivity
            r2 = 2131690716(0x7f0f04dc, float:1.9010483E38)
            java.lang.String r1 = r1.getString(r2)
            r0.set(r11, r1)
        L_0x0960:
            int r0 = r9.mManual3AEnabled
            if (r0 == 0) goto L_0x096b
            r0 = 0
            r9.mManual3AEnabled = r0
            goto L_0x096b
        L_0x0968:
            r0 = 0
            r9.mIsBokehMode = r0
        L_0x096b:
            android.hardware.Camera$Parameters r0 = r9.mParameters
            java.lang.String r1 = "bokeh-mode"
            r0.set(r1, r3)
            android.hardware.Camera$Parameters r0 = r9.mParameters
            java.lang.String r1 = "bokeh-mpo-mode"
            r0.set(r1, r4)
            android.hardware.Camera$Parameters r0 = r9.mParameters
            java.lang.String r1 = "bokeh-blur-value"
            r0.set(r1, r5)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "Bokeh Mode = "
            r0.append(r1)
            r0.append(r3)
            java.lang.String r1 = " bokehMpo = "
            r0.append(r1)
            r0.append(r4)
            java.lang.String r1 = " bokehBlurDegree = "
            r0.append(r1)
            r0.append(r5)
            java.lang.String r0 = r0.toString()
            android.util.Log.v(r10, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.PhotoModule.qcomUpdateCameraParametersPreference():void");
    }

    private int estimateJpegFileSize(Size size, String str) {
        int i;
        int[] intArray = this.mActivity.getResources().getIntArray(C0905R.array.jpegquality_compression_ratio);
        String[] stringArray = this.mActivity.getResources().getStringArray(C0905R.array.pref_camera_jpegquality_entryvalues);
        int length = intArray.length;
        while (true) {
            length--;
            if (length < 0) {
                i = 0;
                break;
            } else if (stringArray[length].equals(str)) {
                i = intArray[length];
                break;
            }
        }
        if (i == 0) {
            return 0;
        }
        return ((size.width * size.height) * 3) / i;
    }

    private void setFlipValue() {
        int previewFlip = PersistUtil.getPreviewFlip();
        int videoFlip = PersistUtil.getVideoFlip();
        int pictureFlip = PersistUtil.getPictureFlip();
        int jpegRotationForCamera1 = CameraUtil.getJpegRotationForCamera1(this.mCameraId, this.mOrientation);
        this.mParameters.setRotation(jpegRotationForCamera1);
        if (jpegRotationForCamera1 == 90 || jpegRotationForCamera1 == 270) {
            if (previewFlip == 1) {
                previewFlip = 2;
            } else if (previewFlip == 2) {
                previewFlip = 1;
            }
            if (videoFlip == 1) {
                videoFlip = 2;
            } else if (videoFlip == 2) {
                videoFlip = 1;
            }
            if (pictureFlip == 1) {
                pictureFlip = 2;
            } else if (pictureFlip == 2) {
                pictureFlip = 1;
            }
        }
        String filpModeString = CameraUtil.getFilpModeString(previewFlip);
        String filpModeString2 = CameraUtil.getFilpModeString(videoFlip);
        String filpModeString3 = CameraUtil.getFilpModeString(pictureFlip);
        if (CameraUtil.isSupported(filpModeString, CameraSettings.getSupportedFlipMode(this.mParameters))) {
            this.mParameters.set(CameraSettings.KEY_QC_PREVIEW_FLIP, filpModeString);
        }
        if (CameraUtil.isSupported(filpModeString2, CameraSettings.getSupportedFlipMode(this.mParameters))) {
            this.mParameters.set(CameraSettings.KEY_QC_VIDEO_FLIP, filpModeString2);
        }
        if (CameraUtil.isSupported(filpModeString3, CameraSettings.getSupportedFlipMode(this.mParameters))) {
            this.mParameters.set(CameraSettings.KEY_QC_SNAPSHOT_PICTURE_FLIP, filpModeString3);
        }
    }

    @TargetApi(16)
    private void setAutoExposureLockIfSupported() {
        if (this.mAeLockSupported) {
            this.mParameters.setAutoExposureLock(this.mFocusManager.getAeAwbLock());
        }
    }

    @TargetApi(16)
    private void setAutoWhiteBalanceLockIfSupported() {
        if (this.mAwbLockSupported) {
            this.mParameters.setAutoWhiteBalanceLock(this.mFocusManager.getAeAwbLock());
        }
    }

    private void setFocusAreasIfSupported() {
        if (this.mFocusAreaSupported) {
            this.mParameters.setFocusAreas(this.mFocusManager.getFocusAreas());
        }
    }

    private void setMeteringAreasIfSupported() {
        if (this.mMeteringAreaSupported) {
            this.mParameters.setMeteringAreas(this.mFocusManager.getMeteringAreas());
        }
    }

    private void setZoomMenuValue() {
        String string = this.mPreferences.getString(CameraSettings.KEY_ZOOM, this.mActivity.getString(C0905R.string.pref_camera_zoom_default));
        if (!string.equals("0")) {
            int parseInt = Integer.parseInt(string);
            int i = this.mZoomIdxTbl[0];
            String str = TAG;
            int i2 = -1;
            if (i == -1) {
                Log.d(str, "Update the zoom index table.");
                List zoomRatios = this.mParameters.getZoomRatios();
                int i3 = 0;
                for (int i4 = 1; i4 <= 10; i4++) {
                    int i5 = i4 * 100;
                    int indexOf = zoomRatios.indexOf(Integer.valueOf(i5));
                    if (indexOf == -1) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Can't find matching zoom value ");
                        sb.append(i4);
                        Log.d(str, sb.toString());
                        int i6 = 0;
                        while (true) {
                            i3++;
                            if (i3 < zoomRatios.size() && i6 < i5) {
                                i6 = ((Integer) zoomRatios.get(i3)).intValue();
                            }
                        }
                        if (i3 >= zoomRatios.size()) {
                            break;
                        }
                        i3--;
                    } else {
                        i3 = indexOf;
                    }
                    this.mZoomIdxTbl[i4 - 1] = i3;
                }
            }
            int[] iArr = this.mZoomIdxTbl;
            if (parseInt <= iArr.length) {
                int i7 = parseInt - 1;
                if (iArr[i7] != -1) {
                    int zoom = this.mParameters.getZoom();
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("zoom index = ");
                    sb2.append(this.mZoomIdxTbl[i7]);
                    sb2.append(", cur index = ");
                    sb2.append(zoom);
                    Log.d(str, sb2.toString());
                    if (zoom <= this.mZoomIdxTbl[i7]) {
                        i2 = 1;
                    }
                    while (true) {
                        int[] iArr2 = this.mZoomIdxTbl;
                        if (zoom != iArr2[i7]) {
                            zoom += i2;
                            this.mParameters.setZoom(zoom);
                            try {
                                Thread.sleep(25);
                            } catch (InterruptedException unused) {
                            }
                        } else {
                            this.mParameters.setZoom(iArr2[i7]);
                            return;
                        }
                    }
                }
            }
            StringBuilder sb3 = new StringBuilder();
            sb3.append("Zoom value ");
            sb3.append(parseInt);
            sb3.append(" is not supported!");
            Log.e(str, sb3.toString());
        }
    }

    private boolean updateCameraParametersPreference() {
        int i;
        setAutoExposureLockIfSupported();
        setAutoWhiteBalanceLockIfSupported();
        setFocusAreasIfSupported();
        setMeteringAreasIfSupported();
        if ((this.mManual3AEnabled & 1) == 0) {
            this.mFocusManager.overrideFocusMode(null);
            this.mParameters.setFocusMode(this.mFocusManager.getFocusMode());
        }
        String string = this.mPreferences.getString("pref_camera_picturesize_key", null);
        String str = TAG;
        if (string == null) {
            CameraSettings.initialCameraPictureSize(this.mActivity, this.mParameters);
        } else {
            Size pictureSize = this.mParameters.getPictureSize();
            StringBuilder sb = new StringBuilder();
            sb.append("old picture_size = ");
            sb.append(pictureSize.width);
            String str2 = " x ";
            sb.append(str2);
            sb.append(pictureSize.height);
            Log.v(str, sb.toString());
            CameraSettings.setCameraPictureSize(string, this.mParameters.getSupportedPictureSizes(), this.mParameters);
            Size pictureSize2 = this.mParameters.getPictureSize();
            StringBuilder sb2 = new StringBuilder();
            sb2.append("new picture_size = ");
            sb2.append(pictureSize2.width);
            sb2.append(str2);
            sb2.append(pictureSize2.height);
            Log.v(str, sb2.toString());
            if (!(pictureSize == null || pictureSize2 == null || pictureSize2.equals(pictureSize) || this.mCameraState == 0)) {
                Log.v(str, "Picture Size changed. Restart Preview");
                this.mRestartPreview = true;
            }
        }
        Size pictureSize3 = this.mParameters.getPictureSize();
        Size optimalPreviewSize = CameraUtil.getOptimalPreviewSize((Activity) this.mActivity, this.mParameters.getSupportedPreviewSizes(), ((double) pictureSize3.width) / ((double) pictureSize3.height));
        Point cameraPreviewSize = PersistUtil.getCameraPreviewSize();
        if (cameraPreviewSize != null) {
            optimalPreviewSize.width = cameraPreviewSize.x;
            optimalPreviewSize.height = cameraPreviewSize.y;
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append("updateCameraParametersPreference final preview size = ");
        sb3.append(optimalPreviewSize.width);
        sb3.append(", ");
        sb3.append(optimalPreviewSize.height);
        Log.d(str, sb3.toString());
        if (!this.mParameters.getPreviewSize().equals(optimalPreviewSize)) {
            this.mParameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
            if (this.mHandler.getLooper() == Looper.myLooper()) {
                setupPreview();
            } else {
                this.mCameraDevice.setParameters(this.mParameters);
            }
            this.mParameters = this.mCameraDevice.getParameters();
            Log.v(str, "Preview Size changed. Restart Preview");
            this.mRestartPreview = true;
        }
        StringBuilder sb4 = new StringBuilder();
        sb4.append("Preview size is ");
        sb4.append(optimalPreviewSize.width);
        String str3 = "x";
        sb4.append(str3);
        sb4.append(optimalPreviewSize.height);
        Log.v(str, sb4.toString());
        Size pictureSize4 = this.mParameters.getPictureSize();
        Size optimalJpegThumbnailSize = CameraUtil.getOptimalJpegThumbnailSize(this.mParameters.getSupportedJpegThumbnailSizes(), ((double) pictureSize4.width) / ((double) pictureSize4.height));
        if (!this.mParameters.getJpegThumbnailSize().equals(optimalJpegThumbnailSize)) {
            this.mParameters.setJpegThumbnailSize(optimalJpegThumbnailSize.width, optimalJpegThumbnailSize.height);
        }
        StringBuilder sb5 = new StringBuilder();
        sb5.append("Thumbnail size is ");
        sb5.append(optimalJpegThumbnailSize.width);
        sb5.append(str3);
        sb5.append(optimalJpegThumbnailSize.height);
        Log.v(str, sb5.toString());
        String string2 = this.mActivity.getString(C0905R.string.setting_on_value);
        String string3 = this.mPreferences.getString(CameraSettings.KEY_CAMERA_HDR, this.mActivity.getString(C0905R.string.pref_camera_hdr_default));
        String string4 = this.mPreferences.getString(CameraSettings.KEY_CAMERA_HDR_PLUS, this.mActivity.getString(C0905R.string.pref_camera_hdr_plus_default));
        boolean equals = string2.equals(string3);
        boolean equals2 = string2.equals(string4);
        boolean z = false;
        String str4 = "auto";
        if (equals2 && GcamHelper.hasGcamCapture()) {
            z = true;
        } else if (equals) {
            String str5 = CameraUtil.SCENE_MODE_HDR;
            this.mSceneMode = str5;
            if (!str4.equals(this.mParameters.getSceneMode()) && !str5.equals(this.mParameters.getSceneMode())) {
                this.mParameters.setSceneMode(str4);
                this.mCameraDevice.setParameters(this.mParameters);
                this.mParameters = this.mCameraDevice.getParameters();
            }
        } else {
            this.mSceneMode = this.mPreferences.getString(CameraSettings.KEY_SCENE_MODE, this.mActivity.getString(C0905R.string.pref_camera_scenemode_default));
        }
        String string5 = this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_refocus_on);
        String string6 = this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_optizoom_on);
        String str6 = this.mSceneMode;
        boolean equals3 = string5.equals(str6);
        String str7 = CameraSettings.KEY_ADVANCED_FEATURES;
        if (equals3) {
            try {
                this.mSceneMode = str4;
                if (this.mHandler.getLooper() == Looper.myLooper()) {
                    this.mUI.setPreference(str7, string5);
                    this.mUI.showRefocusDialog();
                }
            } catch (NullPointerException unused) {
            }
        } else if (string6.equals(this.mSceneMode)) {
            this.mSceneMode = str4;
            if (this.mHandler.getLooper() == Looper.myLooper()) {
                this.mUI.setPreference(str7, string6);
            }
        } else if (this.mSceneMode == null) {
            this.mSceneMode = str4;
        }
        if (CameraUtil.isSupported(this.mSceneMode, this.mParameters.getSupportedSceneModes()) && !this.mParameters.getSceneMode().equals(this.mSceneMode)) {
            if (this.mHandler.getLooper() == Looper.myLooper()) {
                this.mUI.setPreference(str7, str6);
            }
            this.mParameters.setSceneMode(this.mSceneMode);
            this.mCameraDevice.setParameters(this.mParameters);
            this.mParameters = this.mCameraDevice.getParameters();
        }
        int i2 = this.mCameraId;
        if (i2 > 1) {
            i = 95;
        } else {
            i = CameraProfile.getJpegEncodingQualityParameter(i2, 2);
        }
        this.mParameters.setJpegQuality(i);
        int readExposure = CameraSettings.readExposure(this.mPreferences);
        int maxExposureCompensation = this.mParameters.getMaxExposureCompensation();
        if (readExposure < this.mParameters.getMinExposureCompensation() || readExposure > maxExposureCompensation) {
            StringBuilder sb6 = new StringBuilder();
            sb6.append("invalid exposure range: ");
            sb6.append(readExposure);
            Log.w(str, sb6.toString());
        } else {
            this.mParameters.setExposureCompensation(readExposure);
        }
        if (str4.equals(this.mSceneMode)) {
            String str8 = this.mSavedFlashMode;
            if (str8 == null) {
                str8 = this.mPreferences.getString(CameraSettings.KEY_FLASH_MODE, this.mActivity.getString(C0905R.string.pref_camera_flashmode_default));
            }
            if (CameraUtil.isSupported(str8, this.mParameters.getSupportedFlashModes())) {
                this.mParameters.setFlashMode(str8);
            } else if (this.mParameters.getFlashMode() == null) {
                this.mActivity.getString(C0905R.string.pref_camera_flashmode_no_flash);
            }
            if ((this.mManual3AEnabled & 2) == 0) {
                String string7 = this.mPreferences.getString("pref_camera_whitebalance_key", this.mActivity.getString(C0905R.string.pref_camera_whitebalance_default));
                if (CameraUtil.isSupported(string7, this.mParameters.getSupportedWhiteBalance())) {
                    this.mParameters.setWhiteBalance(string7);
                } else {
                    String whiteBalance = this.mParameters.getWhiteBalance();
                }
            }
            if (this.mInstantCaptureSnapShot) {
                Log.v(str, "Change the focuse mode to infinity");
                String str9 = "infinity";
                this.mFocusManager.overrideFocusMode(str9);
                this.mParameters.setFocusMode(str9);
            } else if ((this.mManual3AEnabled & 1) == 0) {
                this.mFocusManager.overrideFocusMode(null);
                this.mParameters.setFocusMode(this.mFocusManager.getFocusMode());
            }
        } else {
            this.mFocusManager.overrideFocusMode(this.mParameters.getFocusMode());
            String str10 = "off";
            if (CameraUtil.isSupported(str10, this.mParameters.getSupportedFlashModes())) {
                this.mParameters.setFlashMode(str10);
            }
            if (CameraUtil.isSupported(str4, this.mParameters.getSupportedWhiteBalance())) {
                this.mParameters.setWhiteBalance(str4);
            }
        }
        if (this.mContinuousFocusSupported && ApiHelper.HAS_AUTO_FOCUS_MOVE_CALLBACK) {
            updateAutoFocusMoveCallback();
        }
        String string8 = this.mPreferences.getString(CameraSettings.KEY_TS_MAKEUP_UILABLE, this.mActivity.getString(C0905R.string.pref_camera_tsmakeup_default));
        this.mParameters.set(CameraSettings.KEY_TS_MAKEUP_PARAM, string8);
        StringBuilder sb7 = new StringBuilder();
        sb7.append("updateCameraParametersPreference(): TSMakeup tsmakeup value = ");
        sb7.append(string8);
        Log.v(str, sb7.toString());
        if (TsMakeupManager.MAKEUP_ON.equals(string8)) {
            String string9 = this.mPreferences.getString(CameraSettings.KEY_TS_MAKEUP_LEVEL_WHITEN, this.mActivity.getString(C0905R.string.pref_camera_tsmakeup_level_default));
            String string10 = this.mPreferences.getString(CameraSettings.KEY_TS_MAKEUP_LEVEL_CLEAN, this.mActivity.getString(C0905R.string.pref_camera_tsmakeup_level_default));
            this.mParameters.set(CameraSettings.KEY_TS_MAKEUP_PARAM_WHITEN, string9);
            this.mParameters.set(CameraSettings.KEY_TS_MAKEUP_PARAM_CLEAN, string10);
        }
        setZoomMenuValue();
        qcomUpdateCameraParametersPreference();
        return z;
    }

    @TargetApi(16)
    private void updateAutoFocusMoveCallback() {
        if (this.mParameters.getFocusMode().equals(CameraUtil.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            this.mCameraDevice.setAutoFocusMoveCallback(this.mHandler, (CameraAFMoveCallback) this.mAutoFocusMoveCallback);
        } else {
            this.mCameraDevice.setAutoFocusMoveCallback(null, null);
        }
    }

    private void setCameraParameters(int i) {
        CameraProxy cameraProxy = this.mCameraDevice;
        if (cameraProxy != null) {
            synchronized (cameraProxy) {
                boolean z = false;
                if ((i & 1) != 0) {
                    try {
                        updateCameraParametersInitialize();
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                if ((i & 2) != 0) {
                    updateCameraParametersZoom();
                }
                if ((i & 4) != 0) {
                    z = updateCameraParametersPreference();
                }
                this.mCameraDevice.setParameters(this.mParameters);
                if (z && !this.mIsImageCaptureIntent) {
                    this.mHandler.sendEmptyMessage(12);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setCameraParametersWhenIdle(int i) {
        this.mUpdateSet = i | this.mUpdateSet;
        if (this.mCameraDevice == null) {
            this.mUpdateSet = 0;
            return;
        }
        if (isCameraIdle()) {
            setCameraParameters(this.mUpdateSet);
            if (this.mRestartPreview && this.mCameraState != 0) {
                Log.v(TAG, "Restarting Preview...");
                stopPreview();
                resizeForPreviewAspectRatio();
                startPreview();
                setCameraState(1);
            }
            this.mRestartPreview = false;
            updateCameraSettings();
            this.mUpdateSet = 0;
        } else if (!this.mHandler.hasMessages(4)) {
            this.mHandler.sendEmptyMessageDelayed(4, 1000);
        }
    }

    public boolean isCameraIdle() {
        int i = this.mCameraState;
        if (i == 1 || i == 0) {
            return true;
        }
        FocusOverlayManager focusOverlayManager = this.mFocusManager;
        if (focusOverlayManager == null || !focusOverlayManager.isFocusCompleted() || this.mCameraState == 4) {
            return false;
        }
        return true;
    }

    public boolean isImageCaptureIntent() {
        String action = this.mActivity.getIntent().getAction();
        return "android.media.action.IMAGE_CAPTURE".equals(action) || "android.media.action.IMAGE_CAPTURE_SECURE".equals(action);
    }

    private void setupCaptureParams() {
        Bundle extras = this.mActivity.getIntent().getExtras();
        if (extras != null) {
            this.mSaveUri = (Uri) extras.getParcelable("output");
            this.mCropValue = extras.getString("crop");
        }
    }

    private void UpdateManualFocusSettings() {
        this.mUI.collapseCameraControls();
        Builder builder = new Builder(this.mActivity);
        LinearLayout linearLayout = new LinearLayout(this.mActivity);
        linearLayout.setOrientation(1);
        builder.setTitle("Manual Focus Settings");
        builder.setNegativeButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        final TextView textView = new TextView(this.mActivity);
        String string = this.mActivity.getString(C0905R.string.pref_camera_manual_focus_value_scale_mode);
        String string2 = this.mActivity.getString(C0905R.string.pref_camera_manual_focus_value_diopter_mode);
        String string3 = this.mPreferences.getString(CameraSettings.KEY_MANUAL_FOCUS, this.mActivity.getString(C0905R.string.pref_camera_manual_focus_default));
        StringBuilder sb = new StringBuilder();
        sb.append("manualFocusMode selected = ");
        sb.append(string3);
        Log.v(TAG, sb.toString());
        String str = "Ok";
        String str2 = " to ";
        String str3 = "Enter focus position in the range of ";
        String str4 = "Current focus position is ";
        if (string3.equals(string)) {
            final SeekBar seekBar = new SeekBar(this.mActivity);
            int i = this.mParameters.getInt(CameraSettings.KEY_MIN_FOCUS_SCALE);
            int i2 = this.mParameters.getInt(CameraSettings.KEY_MAX_FOCUS_SCALE);
            this.mParameters = this.mCameraDevice.getParameters();
            int i3 = this.mParameters.getInt(CameraSettings.KEY_MANUAL_FOCUS_SCALE);
            seekBar.setProgress(i3);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str4);
            sb2.append(i3);
            textView.setText(sb2.toString());
            StringBuilder sb3 = new StringBuilder();
            sb3.append(str3);
            sb3.append(i);
            sb3.append(str2);
            sb3.append(i2);
            builder.setMessage(sb3.toString());
            seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                    TextView textView = textView;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Current focus position is ");
                    sb.append(i);
                    textView.setText(sb.toString());
                }
            });
            linearLayout.addView(seekBar);
            linearLayout.addView(textView);
            builder.setView(linearLayout);
            builder.setPositiveButton(str, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    int progress = seekBar.getProgress();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Setting focus position : ");
                    sb.append(progress);
                    Log.v(PhotoModule.TAG, sb.toString());
                    PhotoModule.access$8976(PhotoModule.this, 1);
                    PhotoModule.this.mParameters.setFocusMode(ParametersWrapper.FOCUS_MODE_MANUAL_POSITION);
                    PhotoModule.this.mParameters.set(CameraSettings.KEY_MANUAL_FOCUS_TYPE, 2);
                    PhotoModule.this.mParameters.set(CameraSettings.KEY_MANUAL_FOCUS_POSITION, progress);
                    PhotoModule.this.updateCommonManual3ASettings();
                    PhotoModule.this.onSharedPreferenceChanged();
                }
            });
            builder.show();
        } else if (string3.equals(string2)) {
            String str5 = this.mParameters.get(CameraSettings.KEY_MIN_FOCUS_DIOPTER);
            String str6 = this.mParameters.get(CameraSettings.KEY_MAX_FOCUS_DIOPTER);
            double parseDouble = Double.parseDouble(str5);
            final double parseDouble2 = Double.parseDouble(str6);
            EditText editText = new EditText(this.mActivity);
            editText.setInputType(8194);
            StringBuilder sb4 = new StringBuilder();
            sb4.append(str3);
            sb4.append(parseDouble);
            sb4.append(str2);
            sb4.append(parseDouble2);
            builder.setMessage(sb4.toString());
            this.mParameters = this.mCameraDevice.getParameters();
            String str7 = this.mParameters.get(CameraSettings.KEY_MANUAL_FOCUS_DIOPTER);
            StringBuilder sb5 = new StringBuilder();
            sb5.append(str4);
            sb5.append(str7);
            textView.setText(sb5.toString());
            linearLayout.addView(editText);
            linearLayout.addView(textView);
            builder.setView(linearLayout);
            final EditText editText2 = editText;
            final double d = parseDouble;
            C069528 r0 = new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    double d;
                    String str = PhotoModule.TAG;
                    String obj = editText2.getText().toString();
                    String str2 = "Invalid focus position";
                    if (obj.length() > 0) {
                        try {
                            d = Double.parseDouble(obj);
                        } catch (NumberFormatException unused) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Input foucspos ");
                            sb.append(0.0d);
                            sb.append(" is invalid");
                            Log.w(str, sb.toString());
                            d = parseDouble2 + 1.0d;
                        }
                        if (d < d || d > parseDouble2) {
                            RotateTextToast.makeText((Activity) PhotoModule.this.mActivity, (CharSequence) str2, 0).show();
                        } else {
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("Setting focus position : ");
                            sb2.append(obj);
                            Log.v(str, sb2.toString());
                            PhotoModule.access$8976(PhotoModule.this, 1);
                            PhotoModule.this.mParameters.setFocusMode(ParametersWrapper.FOCUS_MODE_MANUAL_POSITION);
                            PhotoModule.this.mParameters.set(CameraSettings.KEY_MANUAL_FOCUS_TYPE, 3);
                            PhotoModule.this.mParameters.set(CameraSettings.KEY_MANUAL_FOCUS_POSITION, obj);
                            PhotoModule.this.updateCommonManual3ASettings();
                            PhotoModule.this.onSharedPreferenceChanged();
                        }
                        return;
                    }
                    RotateTextToast.makeText((Activity) PhotoModule.this.mActivity, (CharSequence) str2, 0).show();
                }
            };
            builder.setPositiveButton(str, r0);
            builder.show();
        } else {
            this.mManual3AEnabled &= -2;
            this.mParameters.setFocusMode(this.mFocusManager.getFocusMode());
            this.mUI.overrideSettings(CameraSettings.KEY_FOCUS_MODE, null);
            updateCommonManual3ASettings();
            onSharedPreferenceChanged();
        }
    }

    private void UpdateManualWBSettings() {
        this.mUI.collapseCameraControls();
        Builder builder = new Builder(this.mActivity);
        LinearLayout linearLayout = new LinearLayout(this.mActivity);
        linearLayout.setOrientation(1);
        builder.setTitle("Manual White Balance Settings");
        builder.setNegativeButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        String string = this.mActivity.getString(C0905R.string.pref_camera_manual_wb_value_color_temperature);
        String string2 = this.mActivity.getString(C0905R.string.pref_camera_manual_wb_value_rbgb_gains);
        String string3 = this.mPreferences.getString(CameraSettings.KEY_MANUAL_WB, this.mActivity.getString(C0905R.string.pref_camera_manual_wb_default));
        String str = "pref_camera_whitebalance_key";
        this.mPreferences.getString(str, this.mActivity.getString(C0905R.string.pref_camera_whitebalance_default));
        StringBuilder sb = new StringBuilder();
        sb.append("manualWBMode selected = ");
        sb.append(string3);
        Log.v(TAG, sb.toString());
        String str2 = "Ok";
        String str3 = " to ";
        if (string3.equals(string)) {
            TextView textView = new TextView(this.mActivity);
            final EditText editText = new EditText(this.mActivity);
            editText.setInputType(2);
            final int i = this.mParameters.getInt(CameraSettings.KEY_MIN_WB_CCT);
            final int i2 = this.mParameters.getInt(CameraSettings.KEY_MAX_WB_CCT);
            this.mParameters = this.mCameraDevice.getParameters();
            String str4 = this.mParameters.get(CameraSettings.KEY_MANUAL_WB_CCT);
            if (str4 != null) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Current CCT is ");
                sb2.append(str4);
                textView.setText(sb2.toString());
            }
            StringBuilder sb3 = new StringBuilder();
            sb3.append("Enter CCT value in the range of ");
            sb3.append(i);
            sb3.append(str3);
            sb3.append(i2);
            builder.setMessage(sb3.toString());
            linearLayout.addView(editText);
            linearLayout.addView(textView);
            builder.setView(linearLayout);
            builder.setPositiveButton(str2, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    String obj = editText.getText().toString();
                    int parseInt = obj.length() > 0 ? Integer.parseInt(obj) : -1;
                    if (parseInt > i2 || parseInt < i) {
                        RotateTextToast.makeText((Activity) PhotoModule.this.mActivity, (CharSequence) "Invalid CCT", 0).show();
                        return;
                    }
                    PhotoModule.access$8976(PhotoModule.this, 2);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Setting CCT value : ");
                    sb.append(parseInt);
                    Log.v(PhotoModule.TAG, sb.toString());
                    PhotoModule.this.mParameters.setWhiteBalance("manual");
                    PhotoModule.this.mParameters.set(CameraSettings.KEY_MANUAL_WB_TYPE, 0);
                    PhotoModule.this.mParameters.set(CameraSettings.KEY_MANUAL_WB_VALUE, parseInt);
                    PhotoModule.this.updateCommonManual3ASettings();
                    PhotoModule.this.onSharedPreferenceChanged();
                }
            });
            builder.show();
        } else if (string3.equals(string2)) {
            TextView textView2 = new TextView(this.mActivity);
            EditText editText2 = new EditText(this.mActivity);
            editText2.setHint("Enter R gain here");
            EditText editText3 = new EditText(this.mActivity);
            editText3.setHint("Enter G gain here");
            EditText editText4 = new EditText(this.mActivity);
            editText4.setHint("Enter B gain here");
            editText2.setInputType(8194);
            editText3.setInputType(8194);
            editText4.setInputType(8194);
            final double parseDouble = Double.parseDouble(this.mParameters.get(CameraSettings.KEY_MIN_WB_GAIN));
            double parseDouble2 = Double.parseDouble(this.mParameters.get(CameraSettings.KEY_MAX_WB_GAIN));
            this.mParameters = this.mCameraDevice.getParameters();
            String str5 = this.mParameters.get(CameraSettings.KEY_MANUAL_WB_GAINS);
            if (str5 != null) {
                StringBuilder sb4 = new StringBuilder();
                sb4.append("Current RGB gains are ");
                sb4.append(str5);
                textView2.setText(sb4.toString());
            }
            StringBuilder sb5 = new StringBuilder();
            sb5.append("Enter RGB gains in the range of ");
            sb5.append(parseDouble);
            sb5.append(str3);
            sb5.append(parseDouble2);
            builder.setMessage(sb5.toString());
            linearLayout.addView(editText2);
            linearLayout.addView(editText3);
            linearLayout.addView(editText4);
            linearLayout.addView(textView2);
            builder.setView(linearLayout);
            final EditText editText5 = editText2;
            final EditText editText6 = editText3;
            final EditText editText7 = editText4;
            final double d = parseDouble2;
            C069931 r0 = new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    double d;
                    double d2;
                    double d3;
                    String str = PhotoModule.TAG;
                    String obj = editText5.getText().toString();
                    String obj2 = editText6.getText().toString();
                    String obj3 = editText7.getText().toString();
                    String str2 = "Invalid RGB gains";
                    if (obj.length() <= 0 || obj2.length() <= 0 || obj3.length() <= 0) {
                        RotateTextToast.makeText((Activity) PhotoModule.this.mActivity, (CharSequence) str2, 0).show();
                        return;
                    }
                    try {
                        d3 = Double.parseDouble(obj);
                        double parseDouble = Double.parseDouble(obj2);
                        d2 = Double.parseDouble(obj3);
                        d = parseDouble;
                    } catch (NumberFormatException unused) {
                        Log.w(str, "Input RGB gain is invalid");
                        double d4 = d;
                        d = d4 + 1.0d;
                        double d5 = d4 + 1.0d;
                        d3 = d4 + 1.0d;
                        d2 = d5;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(obj);
                    String str3 = ",";
                    sb.append(str3);
                    sb.append(obj2);
                    sb.append(str3);
                    sb.append(obj3);
                    String sb2 = sb.toString();
                    double d6 = d;
                    if (d3 <= d6) {
                        double d7 = parseDouble;
                        if (d3 >= d7 && d <= d6 && d >= d7 && d2 <= d6 && d2 >= d7) {
                            StringBuilder sb3 = new StringBuilder();
                            sb3.append("Setting RGB gains : ");
                            sb3.append(sb2);
                            Log.v(str, sb3.toString());
                            PhotoModule.access$8976(PhotoModule.this, 2);
                            PhotoModule.this.mParameters.setWhiteBalance("manual");
                            PhotoModule.this.mParameters.set(CameraSettings.KEY_MANUAL_WB_TYPE, 1);
                            PhotoModule.this.mParameters.set(CameraSettings.KEY_MANUAL_WB_VALUE, sb2);
                            PhotoModule.this.updateCommonManual3ASettings();
                            PhotoModule.this.onSharedPreferenceChanged();
                            return;
                        }
                    }
                    RotateTextToast.makeText((Activity) PhotoModule.this.mActivity, (CharSequence) str2, 0).show();
                }
            };
            builder.setPositiveButton(str2, r0);
            builder.show();
        } else {
            this.mManual3AEnabled &= -3;
            this.mUI.overrideSettings(str, null);
            updateCommonManual3ASettings();
            onSharedPreferenceChanged();
        }
    }

    private void UpdateManualExposureSettings() {
        this.mUI.collapseCameraControls();
        Builder builder = new Builder(this.mActivity);
        LinearLayout linearLayout = new LinearLayout(this.mActivity);
        linearLayout.setOrientation(1);
        TextView textView = new TextView(this.mActivity);
        final EditText editText = new EditText(this.mActivity);
        TextView textView2 = new TextView(this.mActivity);
        final EditText editText2 = new EditText(this.mActivity);
        editText.setInputType(2);
        editText2.setInputType(8194);
        builder.setTitle("Manual Exposure Settings");
        builder.setNegativeButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        this.mParameters = this.mCameraDevice.getParameters();
        final int i = this.mParameters.getInt(CameraSettings.KEY_MIN_ISO);
        final int i2 = this.mParameters.getInt(CameraSettings.KEY_MAX_ISO);
        ParametersWrapper.getISOValue(this.mParameters);
        String str = this.mParameters.get(CameraSettings.KEY_CURRENT_ISO);
        if (str != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Current ISO is ");
            sb.append(str);
            textView.setText(sb.toString());
        }
        final String str2 = this.mParameters.get(CameraSettings.KEY_MIN_EXPOSURE_TIME);
        final String str3 = this.mParameters.get(CameraSettings.KEY_MAX_EXPOSURE_TIME);
        String str4 = this.mParameters.get(CameraSettings.KEY_CURRENT_EXPOSURE_TIME);
        if (str4 != null) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Current exposure time is ");
            sb2.append(str4);
            textView2.setText(sb2.toString());
        }
        String string = this.mActivity.getString(C0905R.string.pref_camera_manual_exp_value_ISO_priority);
        String string2 = this.mActivity.getString(C0905R.string.pref_camera_manual_exp_value_exptime_priority);
        String string3 = this.mActivity.getString(C0905R.string.pref_camera_manual_exp_value_user_setting);
        String str5 = string3;
        String string4 = this.mPreferences.getString(CameraSettings.KEY_MANUAL_EXPOSURE, this.mActivity.getString(C0905R.string.pref_camera_manual_exp_default));
        StringBuilder sb3 = new StringBuilder();
        sb3.append("manual Exposure Mode selected = ");
        sb3.append(string4);
        Log.v(TAG, sb3.toString());
        String str6 = " to ";
        String str7 = "Enter ISO in the range of ";
        TextView textView3 = textView2;
        String str8 = "Ok";
        if (string4.equals(string)) {
            StringBuilder sb4 = new StringBuilder();
            sb4.append(str7);
            sb4.append(i);
            sb4.append(str6);
            sb4.append(i2);
            builder.setMessage(sb4.toString());
            linearLayout.addView(editText);
            linearLayout.addView(textView);
            builder.setView(linearLayout);
            builder.setPositiveButton(str8, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    String obj = editText.getText().toString();
                    StringBuilder sb = new StringBuilder();
                    sb.append("string iso length ");
                    sb.append(obj.length());
                    String sb2 = sb.toString();
                    String str = PhotoModule.TAG;
                    Log.v(str, sb2);
                    int parseInt = obj.length() > 0 ? Integer.parseInt(obj) : -1;
                    if (parseInt > i2 || parseInt < i) {
                        RotateTextToast.makeText((Activity) PhotoModule.this.mActivity, (CharSequence) "Invalid ISO", 0).show();
                        return;
                    }
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("Setting ISO : ");
                    sb3.append(parseInt);
                    Log.v(str, sb3.toString());
                    PhotoModule.access$8976(PhotoModule.this, 4);
                    ParametersWrapper.setISOValue(PhotoModule.this.mParameters, "manual");
                    PhotoModule.this.mParameters.set(CameraSettings.KEY_CONTINUOUS_ISO, parseInt);
                    PhotoModule.this.mParameters.set(CameraSettings.KEY_EXPOSURE_TIME, "0");
                    PhotoModule.this.updateCommonManual3ASettings();
                    PhotoModule.this.onSharedPreferenceChanged();
                }
            });
            builder.show();
            return;
        }
        boolean equals = string4.equals(string2);
        String str9 = "ms";
        TextView textView4 = textView;
        String str10 = "ms to ";
        EditText editText3 = editText;
        String str11 = "Enter exposure time in the range of ";
        if (equals) {
            StringBuilder sb5 = new StringBuilder();
            sb5.append(str11);
            sb5.append(str2);
            sb5.append(str10);
            sb5.append(str3);
            sb5.append(str9);
            builder.setMessage(sb5.toString());
            linearLayout.addView(editText2);
            linearLayout.addView(textView3);
            builder.setView(linearLayout);
            builder.setPositiveButton(str8, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    double d;
                    String obj = editText2.getText().toString();
                    int length = obj.length();
                    String str = PhotoModule.TAG;
                    if (length > 0) {
                        try {
                            d = Double.parseDouble(obj);
                        } catch (NumberFormatException unused) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Input expTime ");
                            sb.append(obj);
                            sb.append(" is invalid");
                            Log.w(str, sb.toString());
                            d = Double.parseDouble(str3) + 1.0d;
                        }
                    } else {
                        d = -1.0d;
                    }
                    if (d > Double.parseDouble(str3) || d < Double.parseDouble(str2)) {
                        RotateTextToast.makeText((Activity) PhotoModule.this.mActivity, (CharSequence) "Invalid exposure time", 0).show();
                        return;
                    }
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Setting Exposure time : ");
                    sb2.append(d);
                    Log.v(str, sb2.toString());
                    PhotoModule.access$8976(PhotoModule.this, 4);
                    PhotoModule.this.mParameters.set(CameraSettings.KEY_EXPOSURE_TIME, obj);
                    ParametersWrapper.setISOValue(PhotoModule.this.mParameters, ParametersWrapper.ISO_AUTO);
                    PhotoUI access$2700 = PhotoModule.this.mUI;
                    String str2 = ParametersWrapper.ISO_AUTO;
                    String str3 = CameraSettings.KEY_ISO;
                    access$2700.setPreference(str3, str2);
                    PhotoModule.this.mUI.overrideSettings(str3, null);
                    PhotoModule.this.updateCommonManual3ASettings();
                    PhotoModule.this.onSharedPreferenceChanged();
                }
            });
            builder.show();
            return;
        }
        TextView textView5 = textView3;
        String str12 = str5;
        String str13 = str8;
        if (string4.equals(str12)) {
            builder.setMessage("Full manual mode - Enter both ISO and Exposure Time");
            TextView textView6 = new TextView(this.mActivity);
            Builder builder2 = builder;
            TextView textView7 = new TextView(this.mActivity);
            StringBuilder sb6 = new StringBuilder();
            sb6.append(str7);
            sb6.append(i);
            sb6.append(str6);
            sb6.append(i2);
            textView6.setText(sb6.toString());
            StringBuilder sb7 = new StringBuilder();
            sb7.append(str11);
            sb7.append(str2);
            sb7.append(str10);
            sb7.append(str3);
            sb7.append(str9);
            textView7.setText(sb7.toString());
            linearLayout.addView(textView6);
            final EditText editText4 = editText3;
            linearLayout.addView(editText4);
            linearLayout.addView(textView4);
            linearLayout.addView(textView7);
            linearLayout.addView(editText2);
            linearLayout.addView(textView5);
            Builder builder3 = builder2;
            builder3.setView(linearLayout);
            final EditText editText5 = editText2;
            String str14 = str13;
            final String str15 = str3;
            final int i3 = i2;
            final String str16 = str2;
            C070335 r0 = new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    String obj = editText4.getText().toString();
                    StringBuilder sb = new StringBuilder();
                    sb.append("string iso length ");
                    sb.append(obj.length());
                    String sb2 = sb.toString();
                    String str = PhotoModule.TAG;
                    Log.v(str, sb2);
                    int parseInt = obj.length() > 0 ? Integer.parseInt(obj) : -1;
                    double d = -1.0d;
                    String obj2 = editText5.getText().toString();
                    if (obj2.length() > 0) {
                        try {
                            d = Double.parseDouble(obj2);
                        } catch (NumberFormatException unused) {
                            StringBuilder sb3 = new StringBuilder();
                            sb3.append("input newExpTime ");
                            sb3.append(-1.0d);
                            sb3.append(" is invalid");
                            Log.w(str, sb3.toString());
                            d = Double.parseDouble(str15) + 1.0d;
                        }
                    }
                    if (parseInt > i3 || parseInt < i || d > Double.parseDouble(str15) || d < Double.parseDouble(str16)) {
                        RotateTextToast.makeText((Activity) PhotoModule.this.mActivity, (CharSequence) "Invalid input", 0).show();
                        return;
                    }
                    PhotoModule.access$8976(PhotoModule.this, 4);
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append("Setting ISO : ");
                    sb4.append(parseInt);
                    Log.v(str, sb4.toString());
                    ParametersWrapper.setISOValue(PhotoModule.this.mParameters, "manual");
                    PhotoModule.this.mParameters.set(CameraSettings.KEY_CONTINUOUS_ISO, parseInt);
                    StringBuilder sb5 = new StringBuilder();
                    sb5.append("Setting Exposure time : ");
                    sb5.append(d);
                    Log.v(str, sb5.toString());
                    PhotoModule.this.mParameters.set(CameraSettings.KEY_EXPOSURE_TIME, obj2);
                    PhotoModule.this.updateCommonManual3ASettings();
                    PhotoModule.this.onSharedPreferenceChanged();
                }
            };
            builder3.setPositiveButton(str14, r0);
            builder3.show();
            return;
        }
        this.mManual3AEnabled &= -5;
        this.mParameters.set(CameraSettings.KEY_EXPOSURE_TIME, "0");
        this.mUI.overrideSettings(CameraSettings.KEY_ISO, null);
        updateCommonManual3ASettings();
        onSharedPreferenceChanged();
    }

    private static boolean notSame(ListPreference listPreference, String str, String str2) {
        return str.equals(listPreference.getKey()) && !str2.equals(listPreference.getValue());
    }

    public void onSharedPreferenceChanged(ListPreference listPreference) {
        if (!this.mPaused) {
            String string = this.mActivity.getString(C0905R.string.setting_off_value);
            boolean isZSLHDRSupported = CameraSettings.isZSLHDRSupported(this.mParameters);
            String str = CameraSettings.KEY_CAMERA_HDR;
            if (!isZSLHDRSupported) {
                boolean notSame = notSame(listPreference, str, string);
                String str2 = CameraSettings.KEY_ZSL;
                if (!notSame) {
                    String str3 = CameraSettings.KEY_AE_BRACKET_HDR;
                    if (!notSame(listPreference, str3, string)) {
                        if (notSame(listPreference, str2, string)) {
                            this.mUI.setPreference(str, string);
                            this.mUI.setPreference(str3, string);
                        }
                    }
                }
                this.mUI.setPreference(str2, string);
            }
            if (CameraSettings.KEY_MANUAL_EXPOSURE.equals(listPreference.getKey())) {
                UpdateManualExposureSettings();
                return;
            }
            if (CameraSettings.KEY_MANUAL_WB.equals(listPreference.getKey())) {
                UpdateManualWBSettings();
                return;
            }
            if (CameraSettings.KEY_MANUAL_FOCUS.equals(listPreference.getKey())) {
                UpdateManualFocusSettings();
                return;
            }
            String str4 = "pref_camera_savepath_key";
            if (str4.equals(listPreference.getKey())) {
                Storage.setSaveSDCard(this.mPreferences.getString(str4, "0").equals("1"));
                this.mActivity.updateStorageSpaceAndHint();
                updateRemainingPhotos();
            }
            String key = listPreference.getKey();
            String str5 = CameraSettings.KEY_QC_CHROMA_FLASH;
            boolean equals = str5.equals(key);
            String str6 = CameraSettings.KEY_ADVANCED_FEATURES;
            if (equals) {
                this.mUI.setPreference(str6, listPreference.getValue());
            }
            if (str6.equals(listPreference.getKey())) {
                this.mUI.setPreference(str5, listPreference.getValue());
                this.mUI.setPreference(CameraSettings.KEY_SCENE_MODE, listPreference.getValue());
            }
            if (str.equals(listPreference.getKey())) {
                this.mUI.setPreference(str6, listPreference.getValue());
            }
            String string2 = this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_ubifocus_off);
            this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_chromaflash_off);
            String string3 = this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_optizoom_off);
            String string4 = this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_refocus_off);
            String string5 = this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_FSSR_off);
            String string6 = this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_trueportrait_off);
            String string7 = this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_multi_touch_focus_off);
            String string8 = this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_stillmore_off);
            String string9 = this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_none);
            if (notSame(listPreference, CameraSettings.KEY_QC_OPTI_ZOOM, string3) || notSame(listPreference, CameraSettings.KEY_QC_AF_BRACKETING, string2) || notSame(listPreference, CameraSettings.KEY_QC_FSSR, string5) || notSame(listPreference, CameraSettings.KEY_QC_TP, string6) || notSame(listPreference, CameraSettings.KEY_QC_MULTI_TOUCH_FOCUS, string7) || notSame(listPreference, CameraSettings.KEY_QC_STILL_MORE, string8) || notSame(listPreference, CameraSettings.KEY_QC_RE_FOCUS, string4) || notSame(listPreference, str6, string9)) {
                RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.advanced_capture_disable_continuous_shot, 1).show();
            }
            onSharedPreferenceChanged();
        }
    }

    public void onSharedPreferenceChanged() {
        if (!this.mPaused) {
            this.mLocationManager.recordLocation(RecordLocationPreference.get(this.mPreferences, "pref_camera_recordlocation_key"));
            if (needRestart()) {
                Log.v(TAG, "Restarting Preview... Camera Mode Changed");
                stopPreview();
                startPreview();
                setCameraState(1);
                this.mRestartPreview = false;
            }
            if (this.mUI.mMenuInitialized) {
                setCameraParametersWhenIdle(4);
                this.mUI.updateOnScreenIndicators(this.mParameters, this.mPreferenceGroup, this.mPreferences);
            } else {
                this.mHandler.sendEmptyMessage(11);
            }
            resizeForPreviewAspectRatio();
        }
    }

    public void onCameraPickerClicked(int i) {
        if (!this.mPaused && this.mPendingSwitchCameraId == -1) {
            this.mPendingSwitchCameraId = i;
            StringBuilder sb = new StringBuilder();
            sb.append("Start to switch camera. cameraId=");
            sb.append(i);
            Log.v(TAG, sb.toString());
            switchCamera();
        }
    }

    public void onPreviewTextureCopied() {
        this.mHandler.sendEmptyMessage(6);
    }

    public void onUserInteraction() {
        if (!this.mActivity.isFinishing()) {
            keepScreenOnAwhile();
        }
    }

    private void resetScreenOn() {
        this.mHandler.removeMessages(3);
        this.mActivity.getWindow().clearFlags(128);
    }

    private void keepScreenOnAwhile() {
        this.mHandler.removeMessages(3);
        this.mActivity.getWindow().addFlags(128);
        this.mHandler.sendEmptyMessageDelayed(3, 120000);
    }

    public void onOverriddenPreferencesClicked() {
        if (!this.mPaused) {
            this.mUI.showPreferencesToast();
        }
    }

    /* access modifiers changed from: private */
    public void showTapToFocusToast() {
        new RotateTextToast((Activity) this.mActivity, (int) C0905R.string.tap_to_focus, 0).show();
        Editor edit = this.mPreferences.edit();
        edit.putBoolean(CameraSettings.KEY_CAMERA_FIRST_USE_HINT_SHOWN, false);
        edit.apply();
    }

    private void initializeCapabilities() {
        this.mFocusAreaSupported = CameraUtil.isFocusAreaSupported(this.mInitialParams);
        this.mMeteringAreaSupported = CameraUtil.isMeteringAreaSupported(this.mInitialParams);
        this.mAeLockSupported = CameraUtil.isAutoExposureLockSupported(this.mInitialParams);
        this.mAwbLockSupported = CameraUtil.isAutoWhiteBalanceLockSupported(this.mInitialParams);
        this.mContinuousFocusSupported = this.mInitialParams.getSupportedFocusModes().contains(CameraUtil.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    public void onCountDownFinished() {
        this.mSnapshotOnIdle = false;
        initiateSnap();
        this.mFocusManager.onShutterUp();
        this.mUI.overrideSettings(CameraSettings.KEY_ZSL, null);
        this.mUI.showUIAfterCountDown();
    }

    public void onShowSwitcherPopup() {
        this.mUI.onShowSwitcherPopup();
    }

    public int onZoomChanged(int i) {
        if (this.mPaused) {
            return i;
        }
        this.mZoomValue = i;
        if (this.mParameters == null || this.mCameraDevice == null) {
            return i;
        }
        FocusOverlayManager focusOverlayManager = this.mFocusManager;
        if (focusOverlayManager != null && focusOverlayManager.getCurrentFocusState() == 1) {
            this.mFocusManager.cancelAutoFocus();
        }
        synchronized (this.mCameraDevice) {
            this.mParameters.setZoom(this.mZoomValue);
            this.mCameraDevice.setParameters(this.mParameters);
            Parameters parameters = this.mCameraDevice.getParameters();
            if (parameters == null) {
                return i;
            }
            int zoom = parameters.getZoom();
            return zoom;
        }
    }

    public void onZoomChanged(float f) {
        FocusOverlayManager focusOverlayManager = this.mFocusManager;
        if (focusOverlayManager != null && focusOverlayManager.getCurrentFocusState() == 1) {
            this.mFocusManager.cancelAutoFocus();
        }
    }

    public int getCameraState() {
        return this.mCameraState;
    }

    public void onQueueStatus(boolean z) {
        this.mUI.enableShutter(!z);
    }

    public void onMediaSaveServiceConnected(MediaSaveService mediaSaveService) {
        if (this.mFirstTimeInitialized) {
            mediaSaveService.setListener(this);
        }
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] fArr;
        int type = sensorEvent.sensor.getType();
        if (type == 1) {
            fArr = this.mGData;
        } else if (type == 2) {
            fArr = this.mMData;
        } else {
            return;
        }
        for (int i = 0; i < 3; i++) {
            fArr[i] = sensorEvent.values[i];
        }
        float[] fArr2 = new float[3];
        SensorManager.getRotationMatrix(this.f74mR, null, this.mGData, this.mMData);
        SensorManager.getOrientation(this.f74mR, fArr2);
        this.mHeading = ((int) (((double) (fArr2[0] * 180.0f)) / 3.141592653589793d)) % 360;
        int i2 = this.mHeading;
        if (i2 < 0) {
            this.mHeading = i2 + 360;
        }
    }

    public void onPreviewFocusChanged(boolean z) {
        this.mUI.onPreviewFocusChanged(z);
    }

    public boolean arePreviewControlsVisible() {
        return this.mUI.arePreviewControlsVisible();
    }

    public void setDebugUri(Uri uri) {
        this.mDebugUri = uri;
    }

    /* access modifiers changed from: private */
    public void saveToDebugUri(byte[] bArr) {
        Uri uri = this.mDebugUri;
        if (uri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = this.mContentResolver.openOutputStream(uri);
                outputStream.write(bArr);
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception while writing debug jpeg file", e);
            } catch (Throwable th) {
                CameraUtil.closeSilently(outputStream);
                throw th;
            }
            CameraUtil.closeSilently(outputStream);
        }
    }

    public boolean isRefocus() {
        return this.mLastPhotoTakenWithRefocus;
    }

    public void onMakeupLevel(String str, String str2) {
        CameraProxy cameraProxy = this.mCameraDevice;
        if (cameraProxy == null) {
            Log.d(TAG, "MakeupLevel failed CameraDevice not yet initialized");
            return;
        }
        synchronized (cameraProxy) {
            onMakeupLevelSync(str, str2);
        }
    }

    public void onMakeupLevelSync(String str, String str2) {
        StringBuilder sb = new StringBuilder();
        sb.append("PhotoModule.onMakeupLevel(): key is ");
        sb.append(str);
        sb.append(", value is ");
        sb.append(str2);
        String sb2 = sb.toString();
        String str3 = TAG;
        Log.d(str3, sb2);
        if (!TextUtils.isEmpty(str2)) {
            String str4 = TsMakeupManager.MAKEUP_OFF;
            if (!str4.equals(str2)) {
                str4 = TsMakeupManager.MAKEUP_ON;
            }
            StringBuilder sb3 = new StringBuilder();
            sb3.append("onMakeupLevel(): prefValue is ");
            sb3.append(str4);
            Log.d(str3, sb3.toString());
            this.mParameters.set(CameraSettings.KEY_TS_MAKEUP_PARAM, str4);
            boolean isDigitsOnly = TextUtils.isDigitsOnly(str2);
            String str5 = CameraSettings.KEY_TS_MAKEUP_PARAM_CLEAN;
            String str6 = CameraSettings.KEY_TS_MAKEUP_PARAM_WHITEN;
            String str7 = CameraSettings.KEY_TS_MAKEUP_LEVEL_CLEAN;
            String str8 = CameraSettings.KEY_TS_MAKEUP_LEVEL_WHITEN;
            if (!isDigitsOnly) {
                if ("none".equals(str2)) {
                    ListPreference findPreference = this.mPreferenceGroup.findPreference(str8);
                    if (findPreference != null) {
                        String value = findPreference.getValue();
                        if (TextUtils.isEmpty(value)) {
                            value = this.mActivity.getString(C0905R.string.pref_camera_tsmakeup_level_default);
                        }
                        findPreference.setMakeupSeekBarValue(value);
                        this.mParameters.set(str6, Integer.parseInt(value));
                    }
                    ListPreference findPreference2 = this.mPreferenceGroup.findPreference(str7);
                    if (findPreference2 != null) {
                        String value2 = findPreference2.getValue();
                        if (TextUtils.isEmpty(value2)) {
                            value2 = this.mActivity.getString(C0905R.string.pref_camera_tsmakeup_level_default);
                        }
                        findPreference2.setMakeupSeekBarValue(value2);
                        this.mParameters.set(str5, Integer.parseInt(value2));
                    }
                }
            } else if (CameraSettings.KEY_TS_MAKEUP_LEVEL.equals(str)) {
                Parameters parameters = this.mParameters;
                if (parameters != null) {
                    parameters.set(str6, Integer.parseInt(str2));
                    this.mParameters.set(str5, Integer.parseInt(str2));
                }
                ListPreference findPreference3 = this.mPreferenceGroup.findPreference(str8);
                if (findPreference3 != null) {
                    findPreference3.setMakeupSeekBarValue(str2);
                }
                ListPreference findPreference4 = this.mPreferenceGroup.findPreference(str7);
                if (findPreference4 != null) {
                    findPreference4.setMakeupSeekBarValue(str2);
                }
            } else if (str8.equals(str)) {
                Parameters parameters2 = this.mParameters;
                if (parameters2 != null) {
                    parameters2.set(str6, Integer.parseInt(str2));
                }
            } else if (str7.equals(str)) {
                Parameters parameters3 = this.mParameters;
                if (parameters3 != null) {
                    parameters3.set(str5, Integer.parseInt(str2));
                }
            }
            this.mCameraDevice.setParameters(this.mParameters);
            this.mParameters = this.mCameraDevice.getParameters();
        }
    }

    public void onErrorListener(int i) {
        enableRecordingLocation(false);
    }
}
