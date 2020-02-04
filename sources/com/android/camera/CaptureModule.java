package com.android.camera;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.EncoderCapabilities;
import android.media.EncoderCapabilities.VideoEncoderCap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.os.Trace;
import android.support.p000v4.media.session.PlaybackStateCompat;
import android.support.p000v4.view.MotionEventCompat;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;
import androidx.heifwriter.HeifWriter;
import com.android.camera.MediaSaveService.Listener;
import com.android.camera.MediaSaveService.OnMediaSavedListener;
import com.android.camera.PhotoModule.NamedImages;
import com.android.camera.PhotoModule.NamedImages.NamedEntity;
import com.android.camera.SoundClips.Player;
import com.android.camera.exif.ExifInterface;
import com.android.camera.imageprocessor.FrameProcessor;
import com.android.camera.imageprocessor.PostProcessor;
import com.android.camera.imageprocessor.filter.BlurbusterFilter;
import com.android.camera.imageprocessor.filter.ChromaflashFilter;
import com.android.camera.imageprocessor.filter.ImageFilter;
import com.android.camera.imageprocessor.filter.ImageFilter.ResultImage;
import com.android.camera.imageprocessor.filter.SharpshooterFilter;
import com.android.camera.imageprocessor.filter.StillmoreFilter;
import com.android.camera.imageprocessor.filter.UbifocusFilter;
import com.android.camera.mpo.MpoInterface;
import com.android.camera.p004ui.CountDownView.OnCountDownFinishedListener;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.p004ui.TrackingFocusRenderer;
import com.android.camera.tinyplanet.TinyPlanetFragment;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.PersistUtil;
import com.android.camera.util.SettingTranslation;
import com.android.camera.util.VendorTagUtil;
import com.android.internal.util.MemInfoReader;
import com.asus.scenedetectlib.AISceneDetectInterface.SceneType;
import com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition;
import com.asus.scenedetectlib.BuildConfig;
import com.asus.scenedetectlib.detector.SceneDetector;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import org.codeaurora.snapcam.C0905R;
import org.codeaurora.snapcam.filter.ClearSightImageProcessor;
import org.codeaurora.snapcam.filter.ClearSightImageProcessor.Callback;
import org.codeaurora.snapcam.filter.GDepth;
import org.codeaurora.snapcam.filter.GImage;

public class CaptureModule implements CameraModule, PhotoController, Listener, Callback, SettingsManager.Listener, LocationManager.Listener, OnCountDownFinishedListener, OnErrorListener, OnInfoListener {
    private static final int AI_SCENE_DETECT_RESULT = 22;
    private static final int AI_SCENE_DETECT_SKIP = 20;
    private static final int BACK_MODE = 0;
    public static final int BAYER_ID = 0;
    public static final int BAYER_MODE = 1;
    public static int BOKEH_ID = 3;
    public static final int BOKEH_MODE = 4;
    private static final int CANCEL_TOUCH_FOCUS = 1;
    private static final int CANCEL_TOUCH_FOCUS_DELAY = PersistUtil.getCancelTouchFocusDelay();
    private static final int CHECK_STORAGE_WHEN_VIDEO = 0;
    private static final int CLEAR_SCREEN_DELAY = 4;
    private static final int COLOR_FormatI420 = 1;
    private static final int COLOR_FormatNV21 = 2;
    public static final boolean DEBUG;
    private static final int DELAY_MANUAL_CAPTURE = 8;
    private static final int DEPTH_EFFECT_SUCCESS = 1;
    public static final int DUAL_MODE = 0;
    public static Key<long[]> EXPOSURE_RANGE = new Key<>("org.codeaurora.qcamera3.iso_exp_priority.exposure_time_range", long[].class);
    private static final String EXTRA_QUICK_CAPTURE = "android.intent.extra.quickCapture";
    private static final String FLASH_MODE_OFF = "off";
    public static int FRONT_ID = -1;
    private static final int FRONT_MODE = 1;
    public static final CaptureRequest.Key<Integer> INSTANT_AEC_MODE = new CaptureRequest.Key<>("org.codeaurora.qcamera3.instant_aec.instant_aec_mode", Integer.class);
    public static final int INTENT_MODE_CAPTURE = 1;
    public static final int INTENT_MODE_CAPTURE_SECURE = 3;
    public static final int INTENT_MODE_NORMAL = 0;
    public static final int INTENT_MODE_VIDEO = 2;
    private static final int ISO50 = 50;
    private static final int ISO500 = 500;
    public static Key<int[]> ISO_AVAILABLE_MODES = new Key<>("org.codeaurora.qcamera3.iso_exp_priority.iso_available_modes", int[].class);
    public static Key<Byte> IS_SUPPORT_QCFA_SENSOR = new Key<>("org.codeaurora.qcamera3.quadra_cfa.is_qcfa_sensor", Byte.class);
    public static Key<int[]> InstantAecAvailableModes = new Key<>("org.codeaurora.qcamera3.instant_aec.instant_aec_available_modes", int[].class);
    private static final int LONGSHOT_CANCEL_THRESHOLD = 41943040;
    private static final int LOW_LIGHT = 4;
    private static final int MAX_IMAGE_BUFFER_SIZE = 10;
    private static final int MAX_NUM_CAM = 4;
    private static final int MAX_REQUIRED_IMAGE_NUM = 3;
    public static int MONO_ID = -1;
    public static final int MONO_MODE = 2;
    public static Key<Byte> MetaDataMonoOnlyKey = new Key<>("org.codeaurora.qcamera3.sensor_meta_data.is_mono_only", Byte.class);
    private static final int NORMAL_SESSION_MAX_FPS = 30;
    private static final int NO_DEPTH_EFFECT = 0;
    private static final int OPEN_CAMERA = 0;
    public static Key<int[]> QCFA_SUPPORT_DIMENSION = new Key<>("org.codeaurora.qcamera3.quadra_cfa.qcfa_dimension", int[].class);
    private static final int REQUEST_CROP = 1000;
    public static final CaptureRequest.Key<Integer> SATURATION = new CaptureRequest.Key<>("org.codeaurora.qcamera3.saturation.use_saturation", Integer.class);
    private static final int SCREEN_DELAY = 120000;
    private static final int SDCARD_SIZE_LIMIT = -100663296;
    private static final int SELFIE_FLASH_DURATION = 680;
    private static final int STATE_AF_AE_LOCKED = 6;
    private static final int STATE_PICTURE_TAKEN = 4;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_AE_LOCK = 3;
    private static final int STATE_WAITING_AF_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_TOUCH_FOCUS = 5;
    private static final int SUBJECT_NOT_FOUND = 5;
    public static int SWITCH_ID = -1;
    public static final int SWITCH_MODE = 3;
    private static final String TAG = "SnapCam_CaptureModule";
    private static final int TOO_FAR = 3;
    private static final int TOO_NEAR = 2;
    private static final int TOUCH_TO_FOCUS = 6;
    private static final int UPDATE_BOKEH = 7;
    private static final int UPDATE_RECORD_TIME = 5;
    private static final int UPDATE_TERRIBLE_THUMBNAIL = 23;
    private static final int UPDATE_VIDEO = 6;
    public static Key<int[]> WB_COLOR_TEMPERATURE_RANGE = new Key<>("org.codeaurora.qcamera3.manualWB.color_temperature_range", int[].class);
    public static Key<float[]> WB_RGB_GAINS_RANGE = new Key<>("org.codeaurora.qcamera3.manualWB.gains_range", float[].class);
    private static final MeteringRectangle[] ZERO_WEIGHT_3A_REGION;
    public static CaptureResult.Key<byte[]> blinkDegree = new CaptureResult.Key<>("org.codeaurora.qcamera3.stats.blink_degree", byte[].class);
    public static CaptureResult.Key<byte[]> blinkDetected = new CaptureResult.Key<>("org.codeaurora.qcamera3.stats.blink_detected", byte[].class);
    public static final CaptureRequest.Key<Integer> bokeh_blur_level = new CaptureRequest.Key<>("org.codeaurora.qcamera3.bokeh.blurLevel", Integer.class);
    public static final CaptureRequest.Key<Boolean> bokeh_enable = new CaptureRequest.Key<>("org.codeaurora.qcamera3.bokeh.enable", Boolean.class);
    public static final CaptureResult.Key<Integer> bokeh_status = new CaptureResult.Key<>("org.codeaurora.qcamera3.bokeh.status", Integer.class);
    public static Key<Byte> bsgcAvailable = new Key<>("org.codeaurora.qcamera3.stats.bsgc_available", Byte.class);
    public static Key<Integer> buckets = new Key<>("org.codeaurora.qcamera3.histogram.buckets", Integer.class);
    public static final CaptureRequest.Key<Byte> earlyPCR = new CaptureRequest.Key<>("org.quic.camera.EarlyPCRenable.EarlyPCRenable", Byte.TYPE);
    public static final CaptureRequest.Key<Integer> exposure_metering = new CaptureRequest.Key<>("org.codeaurora.qcamera3.exposure_metering.exposure_metering_mode", Integer.class);
    public static CaptureResult.Key<Byte> fusionStatus = new CaptureResult.Key<>("org.codeaurora.qcamera3.fusion.status", Byte.TYPE);
    public static CaptureResult.Key<byte[]> gazeAngle = new CaptureResult.Key<>("org.codeaurora.qcamera3.stats.gaze_angle", byte[].class);
    public static CaptureResult.Key<byte[]> gazeDegree = new CaptureResult.Key<>("org.codeaurora.qcamera3.stats.gaze_degree", byte[].class);
    public static CaptureResult.Key<int[]> gazeDirection = new CaptureResult.Key<>("org.codeaurora.qcamera3.stats.gaze_direction", int[].class);
    public static final Key<int[]> hfrSizeList = new Key<>("org.codeaurora.qcamera3.hfr.sizes", int[].class);
    public static final CaptureRequest.Key<Byte> histMode = new CaptureRequest.Key<>("org.codeaurora.qcamera3.histogram.enable", Byte.TYPE);
    public static CaptureResult.Key<int[]> histogramStats = new CaptureResult.Key<>("org.codeaurora.qcamera3.histogram.stats", int[].class);
    public static Key<Integer> isHdrScene = new Key<>("org.codeaurora.qcamera3.stats.is_hdr_scene", Integer.class);
    public static Key<Byte> logicalMode = new Key<>("org.codeaurora.qcamera3.logical.mode", Byte.class);
    /* access modifiers changed from: private */
    public static final int mLongShotLimitNums = PersistUtil.getLongshotShotLimit();
    public static final CaptureRequest.Key<Integer> makeup_clean_level = new CaptureRequest.Key<>("org.codeaurora.qcamera3.makeup.soft_level", Integer.class);
    public static final CaptureRequest.Key<Boolean> makeup_enable = new CaptureRequest.Key<>("org.codeaurora.qcamera3.makeup.enable", Boolean.class);
    public static final CaptureRequest.Key<Integer> makeup_level = new CaptureRequest.Key<>("org.codeaurora.qcamera3.makeup.Level", Integer.class);
    public static final CaptureRequest.Key<Integer> makeup_whiten_level = new CaptureRequest.Key<>("org.codeaurora.qcamera3.makeup.white_level", Integer.class);
    public static Key<Integer> maxCount = new Key<>("org.codeaurora.qcamera3.histogram.max_count", Integer.class);
    public static final CaptureRequest.Key<Boolean> night_enable = new CaptureRequest.Key<>("org.codeaurora.qcamera3.night.enable", Boolean.class);
    public static final CaptureRequest.Key<Byte> recording_end_stream = new CaptureRequest.Key<>("org.quic.camera.recording.endOfStream", Byte.TYPE);
    private static final String sTempCropFilename = "crop-temp";
    public static final CaptureRequest.Key<Boolean> sat_enable = new CaptureRequest.Key<>("org.codeaurora.qcamera3.sat.on", Boolean.class);
    public static final CaptureRequest.Key<Integer> sharpness_control = new CaptureRequest.Key<>("org.codeaurora.qcamera3.sharpness.strength", Integer.class);
    public static final CaptureRequest.Key<Integer> single_bokeh_blur_level = new CaptureRequest.Key<>("org.codeaurora.qcamera3.singlebokeh.blurLevel", Integer.class);
    public static final CaptureRequest.Key<Boolean> singlebokeh_enable = new CaptureRequest.Key<>("org.codeaurora.qcamera3.singlebokeh.enable", Boolean.class);
    public static CaptureResult.Key<byte[]> smileConfidence = new CaptureResult.Key<>("org.codeaurora.qcamera3.stats.smile_confidence", byte[].class);
    public static CaptureResult.Key<byte[]> smileDegree = new CaptureResult.Key<>("org.codeaurora.qcamera3.stats.smile_degree", byte[].class);
    public static int[] statsdata = new int[1024];
    public static Key<int[]> support_video_hdr_modes = new Key<>("org.codeaurora.qcamera3.video_hdr_mode.vhdr_supported_modes", int[].class);
    public static CaptureRequest.Key<Integer> support_video_hdr_values = new CaptureRequest.Key<>("org.codeaurora.qcamera3.video_hdr_mode.vhdr_mode", Integer.class);
    public static final CaptureRequest.Key<Byte> swMFNR = new CaptureRequest.Key<>("org.codeaurora.qcamera3.swmfnr.enable", Byte.TYPE);
    CaptureRequest.Key<Byte> BayerMonoLinkEnableKey = new CaptureRequest.Key<>("org.codeaurora.qcamera3.dualcam_link_meta_data.enable", Byte.class);
    CaptureRequest.Key<Byte> BayerMonoLinkMainKey = new CaptureRequest.Key<>("org.codeaurora.qcamera3.dualcam_link_meta_data.is_main", Byte.class);
    CaptureRequest.Key<Integer> BayerMonoLinkSessionIdKey = new CaptureRequest.Key<>("org.codeaurora.qcamera3.dualcam_link_meta_data.related_camera_id", Integer.class);
    private long SECONDARY_SERVER_MEM;
    private int bokehBlurDegree;
    /* access modifiers changed from: private */
    public boolean isManualCapture = false;
    private boolean isOpenBokehMode;
    private boolean isOpenMakeUpMode;
    private boolean ismanual = false;
    MeteringRectangle[][] mAERegions = new MeteringRectangle[4][];
    MeteringRectangle[][] mAFRegions = new MeteringRectangle[4][];
    /* access modifiers changed from: private */
    public AISenceDetectThread mAISenceDetectThread;
    /* access modifiers changed from: private */
    public ExecutorService mAISenceSingleThreadExecutor;
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    private boolean mAutoExposureRegionSupported;
    private boolean mAutoFocusRegionSupported;
    private Rect mBayerCameraRegion;
    /* access modifiers changed from: private */
    public boolean mBokehEnabled = false;
    private Builder mBokehRequestBuilder;
    /* access modifiers changed from: private */
    public long[] mBufferLostFrameNumbers = new long[5];
    /* access modifiers changed from: private */
    public int mBufferLostIndex = 0;
    /* access modifiers changed from: private */
    public CameraDevice[] mCameraDevice = new CameraDevice[4];
    /* access modifiers changed from: private */
    public Handler mCameraHandler;
    private String[] mCameraId = new String[4];
    /* access modifiers changed from: private */
    public List<Integer> mCameraIdList;
    private boolean mCameraModeSwitcherAllowed = true;
    /* access modifiers changed from: private */
    public boolean[] mCameraOpened = new boolean[4];
    private HandlerThread mCameraThread;
    /* access modifiers changed from: private */
    public boolean mCamerasOpened = false;
    /* access modifiers changed from: private */
    public CaptureCallback mCaptureCallback;
    private Handler mCaptureCallbackHandler;
    private HandlerThread mCaptureCallbackThread;
    private boolean mCaptureHDRTestEnable = false;
    /* access modifiers changed from: private */
    public CameraCaptureSession[] mCaptureSession = new CameraCaptureSession[4];
    /* access modifiers changed from: private */
    public long mCaptureStartTime;
    private boolean mCaptureTimeLapse = false;
    /* access modifiers changed from: private */
    public Handler mCheckStorageHandler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == 0) {
                CaptureModule.this.mActivity.updateStorageSpaceAndHint();
                CaptureModule.this.mCheckStorageHandler.sendEmptyMessageDelayed(0, 5000);
            }
        }
    };
    /* access modifiers changed from: private */
    public int mChosenImageFormat;
    /* access modifiers changed from: private */
    public ContentResolver mContentResolver;
    private int mControlAFMode = 4;
    /* access modifiers changed from: private */
    public Rect[] mCropRegion = new Rect[4];
    private String mCropValue;
    /* access modifiers changed from: private */
    public CameraCaptureSession mCurrentSession;
    /* access modifiers changed from: private */
    public SceneType mCurrentType;
    /* access modifiers changed from: private */
    public Uri mCurrentVideoUri;
    private ContentValues mCurrentVideoValues;
    private SceneDetector mDetector;
    /* access modifiers changed from: private */
    public int mDisplayOrientation;
    private int mDisplayRotation;
    private ExtendedFace[] mExFaces = null;
    /* access modifiers changed from: private */
    public boolean mFirstPreviewLoaded;
    private boolean mFirstTimeInitialized;
    /* access modifiers changed from: private */
    public FocusStateListener mFocusStateListener;
    /* access modifiers changed from: private */
    public FrameProcessor mFrameProcessor;
    /* access modifiers changed from: private */
    public AtomicInteger mFrameSendNums = new AtomicInteger(0);
    /* access modifiers changed from: private */
    public Camera2GraphView mGraphViewB;
    /* access modifiers changed from: private */
    public Camera2GraphView mGraphViewGB;
    /* access modifiers changed from: private */
    public Camera2GraphView mGraphViewGR;
    /* access modifiers changed from: private */
    public Camera2GraphView mGraphViewR;
    /* access modifiers changed from: private */
    public final Handler mHandler = new MainHandler();
    /* access modifiers changed from: private */
    public HeifImage mHeifImage;
    /* access modifiers changed from: private */
    public OutputConfiguration mHeifOutput;
    private boolean mHighSpeedCapture = false;
    private int mHighSpeedCaptureRate;
    private Range mHighSpeedFPSRange;
    private boolean mHighSpeedRecordingMode = false;
    boolean mHiston = false;
    /* access modifiers changed from: private */
    public AtomicInteger mImageArrivedNums = new AtomicInteger(0);
    private Handler mImageAvailableHandler;
    private HandlerThread mImageAvailableThread;
    /* access modifiers changed from: private */
    public ImageReader[] mImageReader = new ImageReader[4];
    private HeifWriter mInitHeifWriter;
    /* access modifiers changed from: private */
    public int mIntentMode = 0;
    /* access modifiers changed from: private */
    public boolean mIsCloseBokeh;
    private boolean mIsCloseMakeUp = false;
    /* access modifiers changed from: private */
    public boolean mIsLinked = false;
    private boolean mIsMute = false;
    private boolean mIsNightMode = false;
    private boolean mIsRecordingVideo;
    private boolean mIsRefocus = false;
    /* access modifiers changed from: private */
    public boolean mIsSessionComplete = true;
    /* access modifiers changed from: private */
    public boolean mIsSupportedQcfa = false;
    /* access modifiers changed from: private */
    public boolean mIsToNightMode;
    /* access modifiers changed from: private */
    public boolean mIsToNightModeSuccess;
    private long mIsoExposureTime;
    private int mIsoSensitivity;
    private int mJpegFileSizeEstimation;
    /* access modifiers changed from: private */
    public byte[] mJpegImageData;
    /* access modifiers changed from: private */
    public byte[] mLastJpegData;
    /* access modifiers changed from: private */
    public SceneType mLastLastType;
    private int mLastResultAFState = -1;
    /* access modifiers changed from: private */
    public SceneType mLastType;
    /* access modifiers changed from: private */
    public HeifImage mLiveShotImage;
    private HeifWriter mLiveShotInitHeifWriter;
    /* access modifiers changed from: private */
    public OutputConfiguration mLiveShotOutput;
    private LocationManager mLocationManager;
    private int[] mLockRequestHashCode = new int[4];
    /* access modifiers changed from: private */
    public boolean mLongshotActive = false;
    private CaptureCallback mLongshotCallBack;
    private CameraCharacteristics mMainCameraCharacteristics;
    private Builder mMakeUpRequestBuilder;
    private int mMaxVideoDurationInMs;
    private MediaRecorder mMediaRecorder;
    private boolean mMediaRecorderPausing = false;
    /* access modifiers changed from: private */
    public Handler mMpoSaveHandler;
    private HandlerThread mMpoSaveThread;
    /* access modifiers changed from: private */
    public NamedImages mNamedImages;
    /* access modifiers changed from: private */
    public OnMediaSavedListener mOnMediaSavedListener;
    private final OnMediaSavedListener mOnVideoSavedListener;
    /* access modifiers changed from: private */
    public int mOpenCameraId;
    /* access modifiers changed from: private */
    public int mOrientation = -1;
    private Rect[] mOriginalCropRegion = new Rect[4];
    /* access modifiers changed from: private */
    public boolean mPaused = true;
    /* access modifiers changed from: private */
    public Size mPictureSize;
    private Size mPictureThumbSize;
    /* access modifiers changed from: private */
    public PostProcessor mPostProcessor;
    private int[] mPrecaptureRequestHashCode = new int[4];
    /* access modifiers changed from: private */
    public CaptureResult mPreviewCaptureResult;
    private Face[] mPreviewFaces = null;
    private ImageReader mPreviewImageReader;
    /* access modifiers changed from: private */
    public ReentrantLock mPreviewImageReaderLock = new ReentrantLock();
    /* access modifiers changed from: private */
    public Builder[] mPreviewRequestBuilder;
    /* access modifiers changed from: private */
    public Size mPreviewSize;
    private CamcorderProfile mProfile;
    /* access modifiers changed from: private */
    public boolean mQuickCapture;
    private ImageReader[] mRawImageReader = new ImageReader[4];
    /* access modifiers changed from: private */
    public long mRecordingStartTime;
    private boolean mRecordingTimeCountsDown = false;
    /* access modifiers changed from: private */
    public long mRecordingTotalTime;
    private View mRootView;
    private Builder mSaturationRequest;
    private boolean mSaveRaw = false;
    private Uri mSaveUri;
    /* access modifiers changed from: private */
    public SettingsManager mSettingsManager;
    private Builder mSharpnessRequest;
    /* access modifiers changed from: private */
    public boolean mShouldUpdateStorageStatus;
    /* access modifiers changed from: private */
    public boolean mSingleshotActive = false;
    private Player mSoundPlayer;
    /* access modifiers changed from: private */
    public boolean mStartRecPending = false;
    private long mStartRecordingTime;
    private int[] mState;
    private final StateCallback mStateCallback;
    private ExtendedFace[] mStickyExFaces = null;
    private Face[] mStickyFaces = null;
    private boolean mStopRecPending = false;
    private long mStopRecordingTime;
    private Size mSupportedMaxPictureSize;
    private Size mSupportedRawPictureSize;
    private boolean mSurfaceReady = true;
    private Semaphore mSurfaceReadyLock = new Semaphore(1);
    private boolean[] mTakingPicture = new boolean[4];
    /* access modifiers changed from: private */
    public Bitmap mTerribleThumbnailBitmap;
    private ExecutorService mTerribleThumbnailSingleExecutor;
    private TerribleThumbnailThread mTerribleThumbnailThread;
    private int mTimeBetweenTimeLapseFrameCaptureMs = 0;
    private int mTimer;
    private Toast mToast;
    /* access modifiers changed from: private */
    public LinkedList<TotalCaptureResult> mTotalCaptureResultList;
    /* access modifiers changed from: private */
    public CaptureUI mUI;
    boolean mUnsupportedResolution = false;
    /* access modifiers changed from: private */
    public UpdateStorageSpaceAndHintThread mUpdateStorageSpaceThread;
    private boolean mUseFrontCamera;
    private int mVideoEncoder;
    private ParcelFileDescriptor mVideoFileDescriptor;
    private String mVideoFilename;
    private Builder mVideoPreviewRequestBuilder;
    private Size mVideoPreviewSize;
    /* access modifiers changed from: private */
    public Builder mVideoRequestBuilder;
    /* access modifiers changed from: private */
    public Size mVideoSize;
    private ImageReader mVideoSnapshotImageReader;
    private Size mVideoSnapshotSize;
    private Size mVideoSnapshotThumbSize;
    /* access modifiers changed from: private */
    public ConditionVariable mWaitCameraClosed;
    /* access modifiers changed from: private */
    public ConditionVariable mWaitCameraOpened;
    /* access modifiers changed from: private */
    public byte[] mYuvByte;
    private float mZoomValue = 1.0f;
    private CameraManager manager;
    /* access modifiers changed from: private */
    public MediaSaveNotifyThread mediaSaveNotifyThread;
    /* access modifiers changed from: private */
    public SelfieThread selfieThread;

    private class AISenceDetectThread extends Thread {
        private int locHeight;
        private int locScanline;
        private int locStride;
        private int locWidth;
        private Bitmap mBitmap;
        private int orientation;

        private AISenceDetectThread() {
        }

        public void setImageData(Image image) {
            try {
                CaptureModule.this.mPreviewImageReaderLock.lock();
                CaptureModule.this.mYuvByte = CaptureModule.getDataFromImage(image, 2);
                if (image != null) {
                    image.close();
                    image = null;
                }
            } finally {
                if (CaptureModule.this.mPreviewImageReaderLock != null) {
                    CaptureModule.this.mPreviewImageReaderLock.unlock();
                }
                if (image != null) {
                    image.close();
                }
            }
        }

        public void setOrientation(int i) {
            if (CaptureModule.DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("setOrientation:");
                sb.append(i);
                Log.d(CaptureModule.TAG, sb.toString());
            }
            this.orientation = i;
        }

        public void setSize(int i, int i2, int i3, int i4) {
            this.locWidth = i;
            this.locHeight = i2;
            this.locStride = i3;
            this.locScanline = i4;
        }

        public void run() {
            if (CaptureModule.this.isSceneMode() || CaptureModule.this.mYuvByte == null) {
                CaptureModule.this.mCurrentType = SceneType.OTHERS;
                CaptureModule.this.mHandler.sendEmptyMessage(22);
                return;
            }
            CaptureModule captureModule = CaptureModule.this;
            Bitmap transYUVtoBitmap = captureModule.transYUVtoBitmap(captureModule.mYuvByte, this.locStride, this.locScanline, this.locWidth, this.locHeight, this.orientation);
            SceneType sceneDetect = CaptureModule.this.sceneDetect(transYUVtoBitmap, this.orientation);
            if (sceneDetect == CaptureModule.this.mLastType && CaptureModule.this.mLastType == CaptureModule.this.mLastLastType) {
                CaptureModule.this.mCurrentType = sceneDetect;
                CaptureModule.this.mHandler.sendEmptyMessage(22);
            } else {
                CaptureModule captureModule2 = CaptureModule.this;
                captureModule2.mLastLastType = captureModule2.mLastType;
                CaptureModule.this.mLastType = sceneDetect;
            }
            transYUVtoBitmap.recycle();
        }
    }

    static abstract class CameraCaptureCallback extends CaptureCallback {
    }

    public static class HeifImage {
        private long mDate;
        private Surface mInputSurface;
        private int mOrientation;
        private String mPath;
        private int mQuality;
        private String mTitle;
        private HeifWriter mWriter;

        public HeifImage(HeifWriter heifWriter, String str, String str2, long j, int i, int i2) {
            this.mWriter = heifWriter;
            this.mPath = str;
            this.mTitle = str2;
            this.mDate = j;
            this.mQuality = i2;
            this.mOrientation = i;
            this.mInputSurface = heifWriter.getInputSurface();
        }

        public HeifWriter getWriter() {
            return this.mWriter;
        }

        public String getPath() {
            return this.mPath;
        }

        public String getTitle() {
            return this.mTitle;
        }

        public long getDate() {
            return this.mDate;
        }

        public int getQuality() {
            return this.mQuality;
        }

        public Surface getInputSurface() {
            return this.mInputSurface;
        }

        public int getOrientation() {
            return this.mOrientation;
        }
    }

    static abstract class ImageAvailableListener implements OnImageAvailableListener {
        int mCamId;

        ImageAvailableListener(int i) {
            this.mCamId = i;
        }
    }

    private class MainHandler extends Handler {
        public MainHandler() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i != 22) {
                if (i != 23) {
                    switch (i) {
                        case 4:
                            CaptureModule.this.mActivity.getWindow().clearFlags(128);
                            return;
                        case 5:
                            CaptureModule.this.updateRecordingTime();
                            return;
                        case 6:
                            CaptureModule.this.startOrStopVideo();
                            return;
                        case 7:
                            CaptureModule.this.mUI.openBokeh();
                            return;
                        case 8:
                            CaptureModule.this.checkAfAeStatesAndCapture(0);
                            return;
                        default:
                            return;
                    }
                } else {
                    CaptureModule.this.mActivity.updateThumbnail(CaptureModule.this.mTerribleThumbnailBitmap);
                }
            } else if (CaptureModule.this.mOpenCameraId == 0) {
                if (PersistUtil.getAISceneDetectFeatureDebug() == 1) {
                    Log.d(CaptureModule.TAG, "adjustParamsForAI");
                    CaptureModule captureModule = CaptureModule.this;
                    captureModule.adjustParamsForAI(captureModule.mCurrentType);
                }
                String str = CaptureModule.this.mCurrentType.toString();
                String upperCase = CaptureModule.this.mActivity.getString(C0905R.string.pref_camera_scenemode_detect_others).toUpperCase();
                if (CaptureModule.this.mUI.SCENE_DETECT_PEOPLE_NUMBER > 0 && str.equals(upperCase)) {
                    str = CaptureModule.this.mActivity.getString(C0905R.string.pref_camera_scenemode_detect_people).toUpperCase();
                }
                CaptureModule.this.mUI.updateSceneDetectIcon(str);
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
            if (CaptureModule.this.mLongshotActive) {
                CaptureModule.this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (CaptureModule.this.mLastJpegData != null) {
                            CaptureModule.this.mActivity.updateThumbnail(CaptureModule.this.mLastJpegData);
                        }
                    }
                });
            } else {
                CaptureModule.this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (MediaSaveNotifyThread.this.uri != null) {
                            CaptureModule.this.mActivity.notifyNewMedia(MediaSaveNotifyThread.this.uri);
                        }
                        CaptureModule.this.mActivity.updateStorageSpaceAndHint();
                        if (CaptureModule.this.mLastJpegData != null) {
                            CaptureModule.this.mActivity.updateThumbnail(CaptureModule.this.mLastJpegData);
                        }
                    }
                });
            }
            CaptureModule.this.mediaSaveNotifyThread = null;
        }
    }

    private class MpoSaveHandler extends Handler {
        private Image bayerImage;
        private Long captureStartTime;
        private Image monoImage;

        public MpoSaveHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 0) {
                this.captureStartTime = (Long) message.obj;
            } else if (i == 1) {
                processNewImage(message);
            }
        }

        private void processNewImage(Message message) {
            StringBuilder sb = new StringBuilder();
            sb.append("MpoSaveHandler:processNewImage for cam id: ");
            sb.append(message.arg1);
            Log.d(CaptureModule.TAG, sb.toString());
            if (message.arg1 == CaptureModule.MONO_ID) {
                this.monoImage = (Image) message.obj;
            } else if (this.bayerImage == null) {
                this.bayerImage = (Image) message.obj;
            }
            if (this.monoImage != null && this.bayerImage != null) {
                saveMpoImage();
            }
        }

        private void saveMpoImage() {
            String str;
            long j;
            CaptureModule.this.mNamedImages.nameNewImage(this.captureStartTime.longValue());
            NamedEntity nextNameEntity = CaptureModule.this.mNamedImages.getNextNameEntity();
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
            long j2 = j;
            int width = this.bayerImage.getWidth();
            int height = this.bayerImage.getHeight();
            byte[] access$9400 = CaptureModule.this.getJpegData(this.bayerImage);
            byte[] bArr = access$9400;
            CaptureModule.this.mActivity.getMediaSaveService().addMpoImage(null, bArr, CaptureModule.this.getJpegData(this.monoImage), width, height, str, j2, null, Exif.getOrientation(Exif.getExif(access$9400)), CaptureModule.this.mOnMediaSavedListener, CaptureModule.this.mContentResolver, PhotoModule.PIXEL_FORMAT_JPEG);
            CaptureModule.this.mActivity.updateThumbnail(access$9400);
            this.bayerImage.close();
            this.bayerImage = null;
            this.monoImage.close();
            this.monoImage = null;
        }
    }

    private class MyCameraHandler extends Handler {
        public MyCameraHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.arg1;
            int i2 = message.what;
            if (i2 == 0) {
                CaptureModule.this.openCamera(i);
            } else if (i2 == 1) {
                CaptureModule.this.cancelTouchFocus(i);
            }
        }
    }

    private class SelfieThread extends Thread {
        private SelfieThread() {
        }

        public void run() {
            try {
                Thread.sleep(680);
                CaptureModule.this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        CaptureModule.this.takePicture();
                    }
                });
            } catch (InterruptedException unused) {
            }
            CaptureModule.this.selfieThread = null;
        }
    }

    private class TerribleThumbnailThread extends Thread {
        private TerribleThumbnailThread() {
        }

        public void run() {
            int i;
            if (CaptureModule.this.isBackCamera()) {
                i = (CaptureModule.this.mOrientation + 90) % 360;
            } else {
                int access$1100 = CaptureModule.this.mOrientation;
                i = (access$1100 == 0 || access$1100 == 180) ? (CaptureModule.this.mOrientation + 270) % 360 : (CaptureModule.this.mOrientation + 90) % 360;
            }
            int width = CaptureModule.this.mPreviewSize.getWidth();
            int height = CaptureModule.this.mPreviewSize.getHeight();
            CaptureModule captureModule = CaptureModule.this;
            captureModule.mTerribleThumbnailBitmap = captureModule.transYUVtoBitmap(captureModule.mYuvByte, width, height, width, height, i);
            if (i != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate((float) i);
                CaptureModule captureModule2 = CaptureModule.this;
                captureModule2.mTerribleThumbnailBitmap = Bitmap.createBitmap(captureModule2.mTerribleThumbnailBitmap, 0, 0, width, height, matrix, false);
            }
            if (CaptureModule.this.mTerribleThumbnailBitmap != null) {
                CaptureModule.this.mHandler.sendEmptyMessage(23);
            }
        }
    }

    private class UpdateStorageSpaceAndHintThread extends Thread {
        private UpdateStorageSpaceAndHintThread() {
        }

        public void run() {
            CaptureModule.this.mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    CaptureModule.this.mActivity.updateStorageSpaceAndHint();
                }
            });
            CaptureModule.this.mUpdateStorageSpaceThread = null;
        }
    }

    private void applyEarlyPCR(Builder builder) {
    }

    public boolean arePreviewControlsVisible() {
        return false;
    }

    public void cancelAutoFocus() {
    }

    /* access modifiers changed from: 0000 */
    public boolean checkSessionAndBuilder(CameraCaptureSession cameraCaptureSession, Builder builder) {
        return (cameraCaptureSession == null || builder == null) ? false : true;
    }

    public int getCameraState() {
        return 0;
    }

    public void installIntentFilter() {
    }

    public boolean isCameraIdle() {
        return true;
    }

    public boolean isImageCaptureIntent() {
        return false;
    }

    public void onActivityResult(int i, int i2, Intent intent) {
    }

    public void onCaptureCancelled() {
    }

    public void onCaptureRetake() {
    }

    public void onCaptureTextureCopied() {
    }

    public void onPreviewRectChanged(Rect rect) {
    }

    public void onPreviewTextureCopied() {
    }

    public void onScreenSizeChanged(int i, int i2) {
    }

    public void onShowSwitcherPopup() {
    }

    public void onStop() {
    }

    public void onUserInteraction() {
    }

    public int onZoomChanged(int i) {
        return 0;
    }

    public void resizeForPreviewAspectRatio() {
    }

    public void stopPreview() {
    }

    public boolean updateStorageHintOnResume() {
        return false;
    }

    public CaptureModule() {
        SceneType sceneType = SceneType.OTHERS;
        this.mCurrentType = sceneType;
        this.mLastType = sceneType;
        this.mLastLastType = sceneType;
        this.mOpenCameraId = -1;
        this.mIsToNightMode = false;
        this.mIsToNightModeSuccess = false;
        this.mShouldUpdateStorageStatus = true;
        this.mTotalCaptureResultList = new LinkedList<>();
        this.mTerribleThumbnailThread = new TerribleThumbnailThread();
        this.mTerribleThumbnailSingleExecutor = Executors.newSingleThreadExecutor();
        this.mTerribleThumbnailBitmap = null;
        this.mAISenceDetectThread = new AISenceDetectThread();
        this.mAISenceSingleThreadExecutor = Executors.newSingleThreadExecutor();
        this.mYuvByte = null;
        this.mOnVideoSavedListener = new OnMediaSavedListener() {
            public void onMediaSaved(Uri uri) {
                if (uri != null) {
                    CaptureModule.this.mActivity.notifyNewMedia(uri);
                    CaptureModule.this.mCurrentVideoUri = uri;
                }
            }
        };
        this.mOnMediaSavedListener = new OnMediaSavedListener() {
            public void onMediaSaved(Uri uri) {
                CaptureModule.this.mUI.setMediaSaved(true);
                if (!CaptureModule.this.mLongshotActive) {
                    if (uri != null) {
                        CaptureModule.this.mActivity.notifyNewMedia(uri);
                    }
                    if (CaptureModule.this.mUpdateStorageSpaceThread == null) {
                        CaptureModule captureModule = CaptureModule.this;
                        captureModule.mUpdateStorageSpaceThread = new UpdateStorageSpaceAndHintThread();
                        CaptureModule.this.mUpdateStorageSpaceThread.start();
                    }
                } else if (CaptureModule.this.mediaSaveNotifyThread == null) {
                    CaptureModule captureModule2 = CaptureModule.this;
                    captureModule2.mediaSaveNotifyThread = new MediaSaveNotifyThread(uri);
                    CaptureModule.this.mediaSaveNotifyThread.start();
                } else {
                    CaptureModule.this.mediaSaveNotifyThread.setUri(uri);
                }
            }
        };
        this.mPreviewRequestBuilder = new Builder[4];
        this.mState = new int[4];
        this.mWaitCameraOpened = new ConditionVariable(false);
        this.mWaitCameraClosed = new ConditionVariable(true);
        this.mCaptureCallback = new CaptureCallback() {
            private void processCaptureResult(CaptureResult captureResult) {
                int intValue = ((Integer) captureResult.getRequest().getTag()).intValue();
                if (!CaptureModule.this.mFirstPreviewLoaded) {
                    CaptureModule.this.mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            CaptureModule.this.mUI.hidePreviewCover();
                        }
                    });
                    CaptureModule.this.mFirstPreviewLoaded = true;
                }
                if (intValue == CaptureModule.this.getMainCameraId()) {
                    CaptureModule.this.mPreviewCaptureResult = captureResult;
                }
                CaptureModule.this.updateCaptureStateMachine(intValue, captureResult);
            }

            public void onCaptureProgressed(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureResult captureResult) {
                if (captureResult.getRequest().getTag() != null) {
                    int intValue = ((Integer) captureResult.getRequest().getTag()).intValue();
                    if (intValue == CaptureModule.this.getMainCameraId()) {
                        Face[] faceArr = (Face[]) captureResult.get(CaptureResult.STATISTICS_FACES);
                        if (faceArr == null || !CaptureModule.this.isBsgcDetecionOn()) {
                            CaptureModule.this.updateFaceView(faceArr, null);
                        } else {
                            CaptureModule captureModule = CaptureModule.this;
                            captureModule.updateFaceView(faceArr, captureModule.getBsgcInfo(captureResult, faceArr.length));
                        }
                    }
                    CaptureModule.this.updateCaptureStateMachine(intValue, captureResult);
                }
            }

            /* JADX WARNING: Code restructure failed: missing block: B:47:0x00fb, code lost:
                r7 = false;
             */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onCaptureCompleted(android.hardware.camera2.CameraCaptureSession r6, android.hardware.camera2.CaptureRequest r7, android.hardware.camera2.TotalCaptureResult r8) {
                /*
                    r5 = this;
                    android.hardware.camera2.CaptureRequest r6 = r8.getRequest()
                    java.lang.Object r6 = r6.getTag()
                    if (r6 != 0) goto L_0x000b
                    return
                L_0x000b:
                    android.hardware.camera2.CaptureResult$Key r6 = android.hardware.camera2.CaptureResult.SENSOR_SENSITIVITY
                    java.lang.Object r6 = r8.get(r6)
                    r7 = 1
                    r0 = 0
                    if (r6 == 0) goto L_0x002b
                    android.hardware.camera2.CaptureResult$Key r6 = android.hardware.camera2.CaptureResult.SENSOR_SENSITIVITY
                    java.lang.Object r6 = r8.get(r6)
                    java.lang.Integer r6 = (java.lang.Integer) r6
                    int r6 = r6.intValue()
                    r1 = 500(0x1f4, float:7.0E-43)
                    if (r6 <= r1) goto L_0x002b
                    com.android.camera.CaptureModule r6 = com.android.camera.CaptureModule.this
                    r6.mIsToNightMode = r7
                    goto L_0x0030
                L_0x002b:
                    com.android.camera.CaptureModule r6 = com.android.camera.CaptureModule.this
                    r6.mIsToNightMode = r0
                L_0x0030:
                    android.hardware.camera2.CaptureRequest r6 = r8.getRequest()
                    java.lang.Object r6 = r6.getTag()
                    java.lang.Integer r6 = (java.lang.Integer) r6
                    int r6 = r6.intValue()
                    com.android.camera.CaptureModule r1 = com.android.camera.CaptureModule.this
                    int r1 = r1.getMainCameraId()
                    if (r6 != r1) goto L_0x006e
                    com.android.camera.CaptureModule r1 = com.android.camera.CaptureModule.this
                    r1.updateFocusStateChange(r8)
                    android.hardware.camera2.CaptureResult$Key r1 = android.hardware.camera2.CaptureResult.STATISTICS_FACES
                    java.lang.Object r1 = r8.get(r1)
                    android.hardware.camera2.params.Face[] r1 = (android.hardware.camera2.params.Face[]) r1
                    if (r1 == 0) goto L_0x0068
                    com.android.camera.CaptureModule r2 = com.android.camera.CaptureModule.this
                    boolean r2 = r2.isBsgcDetecionOn()
                    if (r2 == 0) goto L_0x0068
                    com.android.camera.CaptureModule r2 = com.android.camera.CaptureModule.this
                    int r3 = r1.length
                    com.android.camera.ExtendedFace[] r3 = r2.getBsgcInfo(r8, r3)
                    r2.updateFaceView(r1, r3)
                    goto L_0x006e
                L_0x0068:
                    com.android.camera.CaptureModule r2 = com.android.camera.CaptureModule.this
                    r3 = 0
                    r2.updateFaceView(r1, r3)
                L_0x006e:
                    com.android.camera.SettingsManager r1 = com.android.camera.SettingsManager.getInstance()
                    boolean r1 = r1.isHistogramSupport()
                    if (r1 == 0) goto L_0x009c
                    android.hardware.camera2.CaptureResult$Key<int[]> r1 = com.android.camera.CaptureModule.histogramStats
                    java.lang.Object r1 = r8.get(r1)
                    int[] r1 = (int[]) r1
                    if (r1 == 0) goto L_0x009c
                    com.android.camera.CaptureModule r2 = com.android.camera.CaptureModule.this
                    boolean r2 = r2.mHiston
                    if (r2 == 0) goto L_0x009c
                    int[] r2 = com.android.camera.CaptureModule.statsdata
                    monitor-enter(r2)
                    int[] r3 = com.android.camera.CaptureModule.statsdata     // Catch:{ all -> 0x0099 }
                    r4 = 1024(0x400, float:1.435E-42)
                    java.lang.System.arraycopy(r1, r0, r3, r0, r4)     // Catch:{ all -> 0x0099 }
                    monitor-exit(r2)     // Catch:{ all -> 0x0099 }
                    com.android.camera.CaptureModule r1 = com.android.camera.CaptureModule.this
                    r1.updateGraghView()
                    goto L_0x009c
                L_0x0099:
                    r5 = move-exception
                    monitor-exit(r2)     // Catch:{ all -> 0x0099 }
                    throw r5
                L_0x009c:
                    com.android.camera.CaptureModule r1 = com.android.camera.CaptureModule.this
                    r1.showBokehStatusMessage(r6, r8)
                    r5.processCaptureResult(r8)
                    com.android.camera.CaptureModule r1 = com.android.camera.CaptureModule.this
                    long r2 = r8.getFrameNumber()
                    boolean r1 = r1.isBufferLostFrame(r2)
                    if (r1 == 0) goto L_0x00b1
                    return
                L_0x00b1:
                    com.android.camera.CaptureModule r1 = com.android.camera.CaptureModule.this
                    com.android.camera.imageprocessor.PostProcessor r1 = r1.mPostProcessor
                    boolean r1 = r1.isZSLEnabled()
                    if (r1 == 0) goto L_0x0108
                    com.android.camera.CaptureModule r1 = com.android.camera.CaptureModule.this
                    int r1 = r1.getCameraMode()
                    if (r1 == 0) goto L_0x0108
                    java.util.List r1 = r8.getPartialResults()
                    java.util.Iterator r1 = r1.iterator()
                L_0x00cd:
                    boolean r2 = r1.hasNext()
                    if (r2 == 0) goto L_0x00fb
                    java.lang.Object r2 = r1.next()
                    android.hardware.camera2.CaptureResult r2 = (android.hardware.camera2.CaptureResult) r2
                    com.android.camera.CaptureModule r3 = com.android.camera.CaptureModule.this
                    android.media.ImageReader[] r3 = r3.mImageReader
                    r3 = r3[r6]
                    if (r3 != 0) goto L_0x00e4
                    goto L_0x00fb
                L_0x00e4:
                    android.hardware.camera2.CaptureRequest r2 = r2.getRequest()
                    com.android.camera.CaptureModule r3 = com.android.camera.CaptureModule.this
                    android.media.ImageReader[] r3 = r3.mImageReader
                    r3 = r3[r6]
                    android.view.Surface r3 = r3.getSurface()
                    boolean r2 = r2.containsTarget(r3)
                    if (r2 == 0) goto L_0x00cd
                    goto L_0x00fc
                L_0x00fb:
                    r7 = r0
                L_0x00fc:
                    if (r7 == 0) goto L_0x0111
                    com.android.camera.CaptureModule r5 = com.android.camera.CaptureModule.this
                    com.android.camera.imageprocessor.PostProcessor r5 = r5.mPostProcessor
                    r5.onMetaAvailable(r8)
                    goto L_0x0111
                L_0x0108:
                    com.android.camera.CaptureModule r5 = com.android.camera.CaptureModule.this
                    com.android.camera.imageprocessor.PostProcessor r5 = r5.mPostProcessor
                    r5.onMetaAvailable(r8)
                L_0x0111:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CaptureModule.C05964.onCaptureCompleted(android.hardware.camera2.CameraCaptureSession, android.hardware.camera2.CaptureRequest, android.hardware.camera2.TotalCaptureResult):void");
            }

            public void onCaptureBufferLost(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, Surface surface, long j) {
                super.onCaptureBufferLost(cameraCaptureSession, captureRequest, surface, j);
                if (!CaptureModule.this.mPaused && captureRequest.getTag() != null) {
                    int intValue = ((Integer) captureRequest.getTag()).intValue();
                    if (CaptureModule.this.mImageReader[intValue] != null && surface == CaptureModule.this.mImageReader[intValue].getSurface()) {
                        CaptureModule.this.mBufferLostFrameNumbers[CaptureModule.this.mBufferLostIndex] = j;
                        CaptureModule captureModule = CaptureModule.this;
                        captureModule.mBufferLostIndex = CaptureModule.access$3904(captureModule) % CaptureModule.this.mBufferLostFrameNumbers.length;
                    }
                }
            }
        };
        this.mStateCallback = new StateCallback() {
            public void onOpened(CameraDevice cameraDevice) {
                int parseInt = Integer.parseInt(cameraDevice.getId());
                StringBuilder sb = new StringBuilder();
                sb.append("onOpened ");
                sb.append(parseInt);
                sb.append(", cameraDevice=");
                sb.append(cameraDevice);
                sb.append(", mPaused=");
                sb.append(CaptureModule.this.mPaused);
                sb.append(", cameraMode=");
                sb.append(CaptureModule.this.getCameraMode());
                Log.d(CaptureModule.TAG, sb.toString());
                CaptureModule.this.mCameraDevice[parseInt] = cameraDevice;
                CaptureModule.this.mCameraOpened[parseInt] = true;
                CaptureModule.this.mWaitCameraOpened.open();
                if (CaptureModule.this.isBackCamera() && CaptureModule.this.getCameraMode() == 0 && parseInt == 0) {
                    CaptureModule.this.mCameraHandler.sendMessage(CaptureModule.this.mCameraHandler.obtainMessage(0, CaptureModule.MONO_ID, 0));
                    return;
                }
                CaptureModule.this.mCamerasOpened = true;
                CaptureModule.this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        CaptureModule.this.mUI.onCameraOpened(CaptureModule.this.mCameraIdList);
                        CaptureModule.this.setBokehModeVisible();
                    }
                });
                CaptureModule.this.createSessions();
            }

            public void onDisconnected(CameraDevice cameraDevice) {
                int parseInt = Integer.parseInt(cameraDevice.getId());
                StringBuilder sb = new StringBuilder();
                sb.append("onDisconnected ");
                sb.append(parseInt);
                Log.d(CaptureModule.TAG, sb.toString());
                cameraDevice.close();
                CaptureModule.this.mCameraDevice[parseInt] = null;
                CaptureModule.this.mWaitCameraOpened.open();
                CaptureModule.this.mCamerasOpened = false;
                if (CaptureModule.this.mActivity != null) {
                    CameraActivity access$000 = CaptureModule.this.mActivity;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("open camera error id =");
                    sb2.append(parseInt);
                    Toast.makeText(access$000, sb2.toString(), 1).show();
                    CaptureModule.this.mActivity.finish();
                }
            }

            public void onError(CameraDevice cameraDevice, int i) {
                int parseInt = Integer.parseInt(cameraDevice.getId());
                StringBuilder sb = new StringBuilder();
                sb.append("onError ");
                sb.append(parseInt);
                sb.append(" ");
                sb.append(i);
                Log.e(CaptureModule.TAG, sb.toString());
                if (CaptureModule.this.mCamerasOpened) {
                    CaptureModule.this.mCameraDevice[parseInt].close();
                    CaptureModule.this.mCameraDevice[parseInt] = null;
                }
                CaptureModule.this.mWaitCameraOpened.open();
                CaptureModule.this.mCamerasOpened = false;
                if (CaptureModule.this.mActivity != null) {
                    CameraActivity access$000 = CaptureModule.this.mActivity;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("open camera error id =");
                    sb2.append(parseInt);
                    Toast.makeText(access$000, sb2.toString(), 1).show();
                    CaptureModule.this.mActivity.finish();
                }
            }

            public void onClosed(CameraDevice cameraDevice) {
                int parseInt = Integer.parseInt(cameraDevice.getId());
                StringBuilder sb = new StringBuilder();
                sb.append("onClosed ");
                sb.append(parseInt);
                Log.d(CaptureModule.TAG, sb.toString());
                CaptureModule.this.mCameraDevice[parseInt] = null;
                CaptureModule.this.clearBufferLostFrames();
                CaptureModule.this.mWaitCameraClosed.open();
                CaptureModule.this.mCamerasOpened = false;
            }
        };
        this.mLongshotCallBack = new CaptureCallback() {
            public void onCaptureStarted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, long j, long j2) {
                if (CaptureModule.this.mLongshotActive) {
                    CaptureModule.this.mFrameSendNums.incrementAndGet();
                    Log.d(CaptureModule.TAG, "captureStillPictureForLongshot onCaptureStarted");
                    if (CaptureModule.this.mFrameSendNums.get() >= CaptureModule.mLongShotLimitNums) {
                        CaptureModule.this.mUI.enableGestures(true);
                        CaptureModule.this.mLongshotActive = false;
                        CaptureModule.this.mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                CaptureModule.this.mUI.updateFlashUi(!CaptureModule.this.mLongshotActive);
                            }
                        });
                    }
                }
            }

            public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
                int mainCameraId = CaptureModule.this.getMainCameraId();
                StringBuilder sb = new StringBuilder();
                sb.append("captureStillPictureForLongshot onCaptureCompleted: ");
                sb.append(mainCameraId);
                String sb2 = sb.toString();
                String str = CaptureModule.TAG;
                Log.d(str, sb2);
                if (CaptureModule.DEBUG) {
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("captureStillPictureForLongshot onCaptureCompleted mFrameSendNums : ");
                    sb3.append(CaptureModule.this.mFrameSendNums.get());
                    sb3.append(", mLongShotLimitNums :");
                    sb3.append(CaptureModule.mLongShotLimitNums);
                    Log.d(str, sb3.toString());
                }
                if (CaptureModule.this.mLongshotActive) {
                    CaptureModule.this.checkAndPlayShutterSound(mainCameraId);
                    CaptureModule.this.mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            CaptureModule.this.mUI.doShutterAnimation();
                        }
                    });
                }
            }

            public void onCaptureFailed(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureFailure captureFailure) {
                Log.d(CaptureModule.TAG, "captureStillPictureForLongshot onCaptureFailed: ");
                if (CaptureModule.this.mLongshotActive) {
                    CaptureModule.this.mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            CaptureModule.this.mUI.doShutterAnimation();
                        }
                    });
                }
            }

            public void onCaptureSequenceCompleted(CameraCaptureSession cameraCaptureSession, int i, long j) {
                CameraCaptureSession cameraCaptureSession2 = cameraCaptureSession;
                int mainCameraId = CaptureModule.this.getMainCameraId();
                if (CaptureModule.this.mSettingsManager.getSavePictureFormat() == 1 && CaptureModule.this.mHeifImage != null) {
                    try {
                        CaptureModule.this.mHeifImage.getWriter().stop(5000);
                        CaptureModule.this.mHeifImage.getWriter().close();
                        CaptureModule.this.mActivity.getMediaSaveService().addHEIFImage(CaptureModule.this.mHeifImage.getPath(), CaptureModule.this.mHeifImage.getTitle(), CaptureModule.this.mHeifImage.getDate(), null, CaptureModule.this.mPictureSize.getWidth(), CaptureModule.this.mPictureSize.getHeight(), CaptureModule.this.mHeifImage.getOrientation(), null, CaptureModule.this.mContentResolver, CaptureModule.this.mOnMediaSavedListener, CaptureModule.this.mHeifImage.getQuality(), "heifs");
                        try {
                            CaptureModule.this.mHeifOutput.removeSurface(CaptureModule.this.mHeifImage.getInputSurface());
                            cameraCaptureSession2.updateOutputConfiguration(CaptureModule.this.mHeifOutput);
                            CaptureModule.this.mHeifImage = null;
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    } catch (Exception e3) {
                        e3.printStackTrace();
                        CaptureModule.this.mHeifOutput.removeSurface(CaptureModule.this.mHeifImage.getInputSurface());
                        cameraCaptureSession2.updateOutputConfiguration(CaptureModule.this.mHeifOutput);
                        CaptureModule.this.mHeifImage = null;
                    } catch (Throwable th) {
                        Throwable th2 = th;
                        try {
                            CaptureModule.this.mHeifOutput.removeSurface(CaptureModule.this.mHeifImage.getInputSurface());
                            cameraCaptureSession2.updateOutputConfiguration(CaptureModule.this.mHeifOutput);
                            CaptureModule.this.mHeifImage = null;
                        } catch (CameraAccessException e4) {
                            e4.printStackTrace();
                        } catch (Exception e5) {
                            e5.printStackTrace();
                        }
                        throw th2;
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append("captureStillPictureForLongshot onCaptureSequenceCompleted: ");
                sb.append(mainCameraId);
                Log.d(CaptureModule.TAG, sb.toString());
                CaptureModule.this.mUI.enableGestures(true);
                CaptureModule.this.mLongshotActive = false;
                CaptureModule.this.unlockFocus(mainCameraId);
            }
        };
        this.mIsCloseBokeh = false;
        this.isOpenBokehMode = false;
        this.isOpenMakeUpMode = false;
        this.bokehBlurDegree = 5;
    }

    static /* synthetic */ int access$3904(CaptureModule captureModule) {
        int i = captureModule.mBufferLostIndex + 1;
        captureModule.mBufferLostIndex = i;
        return i;
    }

    static {
        boolean z = true;
        MeteringRectangle meteringRectangle = new MeteringRectangle(0, 0, 0, 0, 0);
        ZERO_WEIGHT_3A_REGION = new MeteringRectangle[]{meteringRectangle};
        if (!(PersistUtil.getCamera2Debug() == 2 || PersistUtil.getCamera2Debug() == 100)) {
            z = false;
        }
        DEBUG = z;
    }

    public Bitmap transYUVtoBitmap(byte[] bArr, int i, int i2, int i3, int i4, int i5) {
        YuvImage yuvImage = new YuvImage(bArr, 17, i, i2, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, i3, i4), 85, byteArrayOutputStream);
        return BitmapFactory.decodeByteArray(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());
    }

    public SceneType sceneDetect(Bitmap bitmap, int i) {
        String str = TAG;
        Log.d(str, "sceneDetect bitmap");
        if (this.mDetector == null) {
            this.mDetector = new SceneDetector(this.mActivity);
        }
        List recognizeImage = this.mDetector.recognizeImage(bitmap);
        if (recognizeImage == null) {
            return SceneType.OTHERS;
        }
        int size = recognizeImage.size();
        float f = 0.0f;
        SceneType sceneType = SceneType.OTHERS;
        for (int i2 = 0; i2 < size; i2++) {
            SceneRecognition sceneRecognition = (SceneRecognition) recognizeImage.get(i2);
            StringBuilder sb = new StringBuilder();
            sb.append("i = ");
            sb.append(i2);
            sb.append(", scene:");
            sb.append(sceneRecognition.toString());
            Log.d(str, sb.toString());
            float f2 = sceneRecognition.confidence;
            if (f2 > f) {
                sceneType = sceneRecognition.sceneType;
                f = f2;
            }
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("sceneDetect result:");
        sb2.append(sceneType);
        Log.d(str, sb2.toString());
        return sceneType;
    }

    /* access modifiers changed from: private */
    public void adjustParamsForAI(SceneType sceneType) {
        if (this.mCameraDevice != null) {
            if (sceneType == SceneType.TEXT) {
                Builder builder = this.mSharpnessRequest;
                if (builder != null) {
                    builder.set(sharpness_control, Integer.valueOf(24));
                }
            } else if (sceneType == SceneType.FOOD || sceneType == SceneType.PLANT || sceneType == SceneType.FLOWER) {
                Builder builder2 = this.mSaturationRequest;
                if (builder2 != null) {
                    builder2.set(SATURATION, Integer.valueOf(10));
                }
            } else if (this.mSharpnessRequest != null) {
                Builder builder3 = this.mSaturationRequest;
                if (builder3 != null) {
                    builder3.set(SATURATION, Integer.valueOf(5));
                    this.mSharpnessRequest.set(sharpness_control, Integer.valueOf(6));
                }
            }
        }
    }

    public void updateThumbnailJpegData(byte[] bArr) {
        this.mLastJpegData = bArr;
    }

    public OnMediaSavedListener getMediaSavedListener() {
        return this.mOnMediaSavedListener;
    }

    public Face[] getPreviewFaces() {
        return this.mPreviewFaces;
    }

    public Face[] getStickyFaces() {
        return this.mStickyFaces;
    }

    public CaptureResult getPreviewCaptureResult() {
        return this.mPreviewCaptureResult;
    }

    public Rect getCameraRegion() {
        return this.mBayerCameraRegion;
    }

    /* access modifiers changed from: private */
    public boolean isBufferLostFrame(long j) {
        long[] jArr = this.mBufferLostFrameNumbers;
        int length = jArr.length;
        for (int i = 0; i < length; i++) {
            if (jArr[i] == j) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void clearBufferLostFrames() {
        int i = 0;
        while (true) {
            long[] jArr = this.mBufferLostFrameNumbers;
            if (i < jArr.length) {
                jArr[i] = -1;
                i++;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void showBokehStatusMessage(int i, CaptureResult captureResult) {
        Integer num;
        final boolean z = true;
        this.mBokehEnabled = true;
        if (this.mBokehEnabled && captureResult != null) {
            Integer valueOf = Integer.valueOf(-1);
            try {
                num = (Integer) captureResult.get(bokeh_status);
                if (num == null) {
                    return;
                }
            } catch (IllegalArgumentException unused) {
                StringBuilder sb = new StringBuilder();
                sb.append("cannot find vendor tag: ");
                sb.append(bokeh_status);
                Log.d(TAG, sb.toString());
                num = valueOf;
            }
            int intValue = num.intValue();
            if (!(intValue == 0 || intValue == 1 || intValue == 2 || intValue == 3 || intValue == 4 || intValue == 5)) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Message type =");
                sb2.append(num);
                sb2.toString();
            }
            if (num.intValue() != 1) {
                z = false;
            }
            this.mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    if (CaptureModule.this.mUI.getBokehTipView() != null && !z) {
                        CaptureModule.this.mBokehEnabled;
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void updateCaptureStateMachine(int i, CaptureResult captureResult) {
        int i2 = this.mState[i];
        if (i2 != 0) {
            String str = " afState:";
            String str2 = " aeState:";
            String str3 = TAG;
            if (i2 != 1) {
                String str4 = " afState: ";
                if (i2 == 2) {
                    Integer num = (Integer) captureResult.get(CaptureResult.CONTROL_AF_STATE);
                    Integer num2 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                    StringBuilder sb = new StringBuilder();
                    sb.append("STATE_WAITING_PRECAPTURE id: ");
                    sb.append(i);
                    sb.append(str4);
                    sb.append(num);
                    sb.append(str2);
                    sb.append(num2);
                    Log.d(str3, sb.toString());
                    if (num2 == null || num2.intValue() == 5 || num2.intValue() == 4 || num2.intValue() == 2) {
                        if (this.mUI.getCurrentProMode() == 1) {
                            lockExposure(i);
                        } else if (this.mPrecaptureRequestHashCode[i] != captureResult.getRequest().hashCode() && this.mPrecaptureRequestHashCode[i] != 0) {
                        } else {
                            if (!this.mLongshotActive || !isFlashOn(i)) {
                                lockExposure(i);
                            } else {
                                checkAfAeStatesAndCapture(i);
                            }
                        }
                    } else if (num2 == null || num2.intValue() == 0) {
                        checkAfAeStatesAndCapture(i);
                    } else if (this.mPrecaptureRequestHashCode[i] == captureResult.getRequest().hashCode()) {
                        Log.i(str3, "AE trigger request result received, but not converged");
                        this.mPrecaptureRequestHashCode[i] = 0;
                    }
                } else if (i2 == 3) {
                    Integer num3 = (Integer) captureResult.get(CaptureResult.CONTROL_AF_STATE);
                    Integer num4 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                    String str5 = "STATE_WAITING_AE_LOCK id: ";
                    if (this.mUI.getCurrentProMode() != 1 || this.isManualCapture) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(str5);
                        sb2.append(i);
                        sb2.append(str4);
                        sb2.append(num3);
                        sb2.append(str2);
                        sb2.append(num4);
                        Log.d(str3, sb2.toString());
                        if ((num4 == null || num4.intValue() == 3) && this.mUI.getCurrentProMode() != 1) {
                            checkAfAeStatesAndCapture(i);
                        }
                    } else if (num4.intValue() == 4 || num4.intValue() == 2 || num4.intValue() == 3) {
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append(str5);
                        sb3.append(i);
                        sb3.append(str4);
                        sb3.append(num3);
                        sb3.append(str2);
                        sb3.append(num4);
                        Log.d(str3, sb3.toString());
                        if (!this.ismanual) {
                            runPrecaptureSequence(i);
                            this.ismanual = true;
                            return;
                        }
                        this.ismanual = false;
                        this.isManualCapture = true;
                        this.mHandler.sendEmptyMessageDelayed(8, 300);
                    }
                } else if (i2 == 5) {
                    Integer num5 = (Integer) captureResult.get(CaptureResult.CONTROL_AF_STATE);
                    Integer num6 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append("STATE_WAITING_TOUCH_FOCUS id: ");
                    sb4.append(i);
                    sb4.append(str);
                    sb4.append(num5);
                    sb4.append(str2);
                    sb4.append(num6);
                    Log.d(str3, sb4.toString());
                } else if (i2 == 6) {
                    Integer num7 = (Integer) captureResult.get(CaptureResult.CONTROL_AF_STATE);
                    Integer num8 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                    StringBuilder sb5 = new StringBuilder();
                    sb5.append("STATE_AF_AE_LOCKED id: ");
                    sb5.append(i);
                    sb5.append(str);
                    sb5.append(num7);
                    sb5.append(str2);
                    sb5.append(num8);
                    Log.d(str3, sb5.toString());
                }
            } else {
                Integer num9 = (Integer) captureResult.get(CaptureResult.CONTROL_AF_STATE);
                Integer num10 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                StringBuilder sb6 = new StringBuilder();
                sb6.append("STATE_WAITING_AF_LOCK id: ");
                sb6.append(i);
                sb6.append(str);
                sb6.append(num9);
                sb6.append(str2);
                sb6.append(num10);
                Log.d(str3, sb6.toString());
                if (num9 != null) {
                    if (4 == num9.intValue() || 5 == num9.intValue() || (this.mLockRequestHashCode[i] == captureResult.getRequest().hashCode() && num9.intValue() == 0)) {
                        if (i == MONO_ID && getCameraMode() == 0 && isBackCamera()) {
                            if (num10.intValue() == 3) {
                                checkAfAeStatesAndCapture(i);
                            } else {
                                this.mState[i] = 3;
                            }
                        } else if (this.mLockRequestHashCode[i] != captureResult.getRequest().hashCode() && this.mLockRequestHashCode[i] != 0) {
                        } else {
                            if (num10 == null || (num10.intValue() == 2 && isFlashOff(i))) {
                                lockExposure(i);
                            } else {
                                runPrecaptureSequence(i);
                            }
                        }
                    } else if (this.mLockRequestHashCode[i] == captureResult.getRequest().hashCode()) {
                        Log.i(str3, "AF lock request result received, but not focused");
                        this.mLockRequestHashCode[i] = 0;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void checkAfAeStatesAndCapture(int i) {
        if (!this.mPaused && this.mCamerasOpened) {
            if (!isBackCamera() || getCameraMode() != 0) {
                this.mState[i] = 4;
                captureStillPicture(i);
                captureStillPictureForHDRTest(i);
                return;
            }
            this.mState[i] = 6;
            try {
                if (i == MONO_ID && !canStartMonoPreview()) {
                    this.mCaptureSession[i].stopRepeating();
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            int[] iArr = this.mState;
            if (iArr[0] == 6) {
                int i2 = MONO_ID;
                if (iArr[i2] == 6) {
                    iArr[0] = 4;
                    iArr[i2] = 4;
                    captureStillPicture(0);
                    captureStillPicture(MONO_ID);
                }
            }
        }
    }

    private void captureStillPictureForHDRTest(int i) {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
        if (SettingsManager.getInstance().isCamera2HDRSupport() && value != null && value.equals("18")) {
            this.mCaptureHDRTestEnable = true;
            captureStillPicture(i);
        }
        this.mCaptureHDRTestEnable = false;
    }

    /* access modifiers changed from: private */
    public boolean canStartMonoPreview() {
        return getCameraMode() == 2 || (getCameraMode() == 0 && isMonoPreviewOn());
    }

    private boolean isMonoPreviewOn() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_MONO_PREVIEW);
        if (value != null && value.equals(RecordLocationPreference.VALUE_ON)) {
            return true;
        }
        return false;
    }

    public boolean isBackCamera() {
        if (this.mUseFrontCamera) {
            return false;
        }
        SettingsManager settingsManager = this.mSettingsManager;
        String str = SettingsManager.KEY_SWITCH_CAMERA;
        String value = settingsManager.getValue(str);
        if (value == null || value.equals("-1")) {
            String value2 = this.mSettingsManager.getValue(SettingsManager.KEY_CAMERA_ID);
            if (value2 == null || Integer.parseInt(value2) == 0) {
                return true;
            }
            return false;
        } else if (this.mSettingsManager.getEntryValues(str).toString().contains("front")) {
            return false;
        } else {
            return true;
        }
    }

    public int getCameraMode() {
        if (this.mUI.isBokehMode) {
            return 4;
        }
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SWITCH_CAMERA);
        if (value != null && !value.equals("-1")) {
            return 3;
        }
        String value2 = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
        if (value2 != null && value2.equals(SettingsManager.SCENE_MODE_DUAL_STRING)) {
            return 0;
        }
        String value3 = this.mSettingsManager.getValue(SettingsManager.KEY_MONO_ONLY);
        return (value3 == null || !value3.equals(RecordLocationPreference.VALUE_ON)) ? 1 : 2;
    }

    /* access modifiers changed from: private */
    public boolean isClearSightOn() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_CLEARSIGHT);
        boolean z = false;
        if (value == null) {
            return false;
        }
        if (isBackCamera() && getCameraMode() == 0 && value.equals(RecordLocationPreference.VALUE_ON)) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public boolean isBsgcDetecionOn() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_BSGC_DETECTION);
        if (value == null) {
            return false;
        }
        return value.equals("enable");
    }

    private boolean isRawCaptureOn() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SAVERAW);
        if (value == null) {
            return false;
        }
        return value.equals("enable");
    }

    /* access modifiers changed from: private */
    public boolean isMpoOn() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_MPO);
        boolean z = false;
        if (value == null) {
            return false;
        }
        if (isBackCamera() && getCameraMode() == 0 && value.equals(RecordLocationPreference.VALUE_ON)) {
            z = true;
        }
        return z;
    }

    public static int getQualityNumber(String str) {
        try {
            int parseInt = Integer.parseInt(str);
            if (parseInt < 0 || parseInt > 100) {
                return 85;
            }
            return parseInt;
        } catch (NumberFormatException unused) {
            char c = 65535;
            int hashCode = str.hashCode();
            int i = 0;
            if (hashCode != -1039745817) {
                if (hashCode != -332320843) {
                    if (hashCode == 3143098 && str.equals("fine")) {
                        c = 1;
                    }
                } else if (str.equals("superfine")) {
                    c = 0;
                }
            } else if (str.equals("normal")) {
                c = 2;
            }
            if (c == 0) {
                i = 2;
            } else if (c == 1) {
                i = 1;
            } else if (c != 2) {
                return 85;
            }
            return CameraProfile.getJpegEncodingQualityParameter(i);
        }
    }

    public LocationManager getLocationManager() {
        return this.mLocationManager;
    }

    private void initializeFirstTime() {
        if (!this.mFirstTimeInitialized && !this.mPaused) {
            this.mLocationManager.recordLocation(getRecordLocation());
            this.mUI.initializeFirstTime();
            MediaSaveService mediaSaveService = this.mActivity.getMediaSaveService();
            if (mediaSaveService != null) {
                mediaSaveService.setListener(this);
                if (isClearSightOn()) {
                    ClearSightImageProcessor.getInstance().setMediaSaveService(mediaSaveService);
                }
            }
            this.mNamedImages = new NamedImages();
            this.mGraphViewR = (Camera2GraphView) this.mRootView.findViewById(C0905R.C0907id.graph_view_r);
            this.mGraphViewGR = (Camera2GraphView) this.mRootView.findViewById(C0905R.C0907id.graph_view_gr);
            this.mGraphViewGB = (Camera2GraphView) this.mRootView.findViewById(C0905R.C0907id.graph_view_gb);
            this.mGraphViewB = (Camera2GraphView) this.mRootView.findViewById(C0905R.C0907id.graph_view_b);
            this.mGraphViewR.setDataSection(0, 256);
            this.mGraphViewGR.setDataSection(256, 512);
            this.mGraphViewGB.setDataSection(512, 768);
            this.mGraphViewB.setDataSection(768, 1024);
            Camera2GraphView camera2GraphView = this.mGraphViewR;
            if (camera2GraphView != null) {
                camera2GraphView.setCaptureModuleObject(this);
            }
            Camera2GraphView camera2GraphView2 = this.mGraphViewGR;
            if (camera2GraphView2 != null) {
                camera2GraphView2.setCaptureModuleObject(this);
            }
            Camera2GraphView camera2GraphView3 = this.mGraphViewGB;
            if (camera2GraphView3 != null) {
                camera2GraphView3.setCaptureModuleObject(this);
            }
            Camera2GraphView camera2GraphView4 = this.mGraphViewB;
            if (camera2GraphView4 != null) {
                camera2GraphView4.setCaptureModuleObject(this);
            }
            this.mFirstTimeInitialized = true;
        }
    }

    private void initializeSecondTime() {
        this.mLocationManager.recordLocation(getRecordLocation());
        MediaSaveService mediaSaveService = this.mActivity.getMediaSaveService();
        if (mediaSaveService != null) {
            mediaSaveService.setListener(this);
            if (isClearSightOn()) {
                ClearSightImageProcessor.getInstance().setMediaSaveService(mediaSaveService);
            }
        }
        this.mNamedImages = new NamedImages();
    }

    public ArrayList<ImageFilter> getFrameFilters() {
        FrameProcessor frameProcessor = this.mFrameProcessor;
        if (frameProcessor == null) {
            return new ArrayList<>();
        }
        return frameProcessor.getFrameFilters();
    }

    private void applyFocusDistance(Builder builder, String str) {
        if (str != null) {
            float floatValue = Float.valueOf(str).floatValue();
            SettingsManager settingsManager = this.mSettingsManager;
            float minimumFocusDistance = floatValue * settingsManager.getMinimumFocusDistance(settingsManager.getCurrentCameraId());
            if (minimumFocusDistance >= 0.0f) {
                builder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(0));
                builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, Float.valueOf(minimumFocusDistance));
            }
        }
    }

    /* access modifiers changed from: private */
    public void createSessions() {
        Trace.beginSection("CaptureModule createSessions");
        if (!this.mPaused && this.mCamerasOpened) {
            if (isBackCamera()) {
                int cameraMode = getCameraMode();
                if (cameraMode == 0) {
                    createSession(0);
                    createSession(MONO_ID);
                } else if (cameraMode == 1) {
                    createSession(0);
                } else if (cameraMode == 2) {
                    createSession(MONO_ID);
                } else if (cameraMode == 3) {
                    createSession(SWITCH_ID);
                } else if (cameraMode == 4) {
                    createSession(BOKEH_ID);
                }
            } else {
                int i = SWITCH_ID;
                if (i == -1) {
                    i = FRONT_ID;
                }
                createSession(i);
            }
            Trace.endSection();
        }
    }

    /* access modifiers changed from: private */
    public Builder getRequestBuilder(int i) throws CameraAccessException {
        if (!this.mPostProcessor.isZSLEnabled() || i != getMainCameraId()) {
            return this.mCameraDevice[i].createCaptureRequest(1);
        }
        return this.mCameraDevice[i].createCaptureRequest(5);
    }

    private void waitForPreviewSurfaceReady() {
        String str = "Time out waiting for surface.";
        try {
            if (this.mSurfaceReady) {
                return;
            }
            if (!this.mSurfaceReadyLock.tryAcquire(2000, TimeUnit.MILLISECONDS)) {
                boolean z = this.mPaused;
                String str2 = TAG;
                if (z) {
                    Log.d(str2, "mPaused status occur Time out waiting for surface.");
                    throw new IllegalStateException("Paused Time out waiting for surface.");
                } else {
                    Log.d(str2, str);
                    throw new RuntimeException(str);
                }
            } else {
                this.mSurfaceReadyLock.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updatePreviewSurfaceReadyState(boolean z) {
        if (z != this.mSurfaceReady) {
            this.mUI.showWaitingProgress(false);
            String str = TAG;
            if (z) {
                Log.i(str, "Preview Surface is ready!");
                this.mSurfaceReadyLock.release();
                this.mSurfaceReady = true;
                return;
            }
            try {
                Log.i(str, "Preview Surface is not ready!");
                this.mSurfaceReady = false;
                this.mSurfaceReadyLock.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void createSession(final int i) {
        List list;
        if (!this.mPaused && this.mCameraOpened[i]) {
            StringBuilder sb = new StringBuilder();
            sb.append("createSession ");
            sb.append(i);
            String sb2 = sb.toString();
            String str = TAG;
            Log.d(str, sb2);
            LinkedList<Surface> linkedList = new LinkedList<>();
            try {
                this.mPreviewRequestBuilder[i] = getRequestBuilder(i);
                this.mPreviewRequestBuilder[i].setTag(Integer.valueOf(i));
                C06017 r3 = new CameraCaptureSession.StateCallback() {
                    public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                        boolean z = true;
                        CaptureModule.this.mIsSessionComplete = true;
                        if (!CaptureModule.this.mPaused && CaptureModule.this.mCameraDevice[i] != null) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("cameraCaptureSession - onConfigured ");
                            sb.append(i);
                            Log.d(CaptureModule.TAG, sb.toString());
                            CaptureModule.this.mActivity.setCameraOpenAndWorking(true);
                            CaptureModule.this.setCameraModeSwitcherAllowed(true);
                            CameraCaptureSession[] access$5200 = CaptureModule.this.mCaptureSession;
                            int i = i;
                            access$5200[i] = cameraCaptureSession;
                            if (i == CaptureModule.this.getMainCameraId()) {
                                CaptureModule.this.mCurrentSession = cameraCaptureSession;
                            }
                            String str = "0";
                            int parseInt = Integer.parseInt(str);
                            String value = CaptureModule.this.mSettingsManager.getValue(SettingsManager.KEY_BOKEH_BLUR_DEGREE);
                            if (value != null) {
                                parseInt = Integer.parseInt(value);
                            }
                            if (parseInt != Integer.parseInt(str) && i == 1 && !CaptureModule.this.mUI.isBokehMode) {
                                CaptureModule.this.mUI.openBokehWithoutRestartSession();
                            }
                            if (CaptureModule.this.mIsCloseBokeh) {
                                CaptureModule.this.mHandler.sendEmptyMessage(6);
                            }
                            CaptureModule.this.initializePreviewConfiguration(i);
                            CaptureModule.this.setDisplayOrientation();
                            CaptureModule.this.updateFaceDetection();
                            try {
                                if (CaptureModule.this.isBackCamera() && CaptureModule.this.getCameraMode() == 0) {
                                    CaptureModule.this.linkBayerMono(i);
                                    CaptureModule.this.mIsLinked = true;
                                }
                                if (i == CaptureModule.MONO_ID && !CaptureModule.this.canStartMonoPreview() && CaptureModule.this.getCameraMode() == 0) {
                                    if (CaptureModule.this.mCaptureSession[i] != null) {
                                        CaptureModule.this.mCaptureSession[i].capture(CaptureModule.this.mPreviewRequestBuilder[i].build(), CaptureModule.this.mCaptureCallback, CaptureModule.this.mCameraHandler);
                                    }
                                } else if (CaptureModule.this.mPostProcessor.isZSLEnabled() && CaptureModule.this.getCameraMode() != 0) {
                                    CaptureModule.this.setRepeatingBurstForZSL(i);
                                } else if (CaptureModule.this.mCaptureSession[i] != null) {
                                    CaptureModule.this.mCaptureSession[i].setRepeatingRequest(CaptureModule.this.mPreviewRequestBuilder[i].build(), CaptureModule.this.mCaptureCallback, CaptureModule.this.mCameraHandler);
                                }
                                if (CaptureModule.this.isClearSightOn()) {
                                    ClearSightImageProcessor instance = ClearSightImageProcessor.getInstance();
                                    if (i != 0) {
                                        z = false;
                                    }
                                    instance.onCaptureSessionConfigured(z, cameraCaptureSession);
                                } else if (CaptureModule.this.mChosenImageFormat == 34 && i == CaptureModule.this.getMainCameraId()) {
                                    CaptureModule.this.mPostProcessor.onSessionConfigured(CaptureModule.this.mCameraDevice[i], CaptureModule.this.mCaptureSession[i]);
                                }
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            } catch (IllegalStateException e2) {
                                e2.printStackTrace();
                            }
                        }
                    }

                    public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("cameracapturesession - onConfigureFailed ");
                        sb.append(i);
                        Log.e(CaptureModule.TAG, sb.toString());
                        CaptureModule.this.mIsSessionComplete = true;
                        CaptureModule.this.mActivity.setCameraOpenAndWorking(false);
                        CaptureModule.this.setCameraModeSwitcherAllowed(true);
                        if (!CaptureModule.this.mActivity.isFinishing()) {
                            new AlertDialog.Builder(CaptureModule.this.mActivity).setTitle("Camera Initialization Failed").setMessage("Closing SnapdragonCamera").setPositiveButton(17039379, new OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    CaptureModule.this.closeCamera();
                                    CaptureModule.this.mActivity.finish();
                                }
                            }).setCancelable(false).setIcon(17301543).show();
                        }
                    }

                    public void onClosed(CameraCaptureSession cameraCaptureSession) {
                        Log.d(CaptureModule.TAG, "cameracapturesession - onClosed");
                        CaptureModule.this.mIsSessionComplete = true;
                        CaptureModule.this.setCameraModeSwitcherAllowed(true);
                    }
                };
                waitForPreviewSurfaceReady();
                Surface previewSurfaceForSession = getPreviewSurfaceForSession(i);
                if (i == getMainCameraId()) {
                    this.mFrameProcessor.setOutputSurface(previewSurfaceForSession);
                }
                if (isClearSightOn()) {
                    this.mPreviewRequestBuilder[i].addTarget(previewSurfaceForSession);
                    linkedList.add(previewSurfaceForSession);
                    ClearSightImageProcessor.getInstance().createCaptureSession(i == 0, this.mCameraDevice[i], linkedList, r3);
                } else if (i == getMainCameraId()) {
                    if (this.mFrameProcessor.isFrameFilterEnabled()) {
                        this.mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                if (CaptureModule.this.mUI.getSurfaceHolder() != null) {
                                    CaptureModule.this.mUI.getSurfaceHolder().setFixedSize(CaptureModule.this.mPreviewSize.getHeight(), CaptureModule.this.mPreviewSize.getWidth());
                                }
                            }
                        });
                    }
                    for (Surface surface : this.mFrameProcessor.getInputSurfaces()) {
                        this.mPreviewRequestBuilder[i].addTarget(surface);
                        linkedList.add(surface);
                    }
                    if (this.mSettingsManager.getSavePictureFormat() == 0) {
                        linkedList.add(this.mImageReader[i].getSurface());
                    }
                    if (this.mSaveRaw) {
                        linkedList.add(this.mRawImageReader[i].getSurface());
                    }
                    if (this.mSettingsManager.getSavePictureFormat() == 1) {
                        list = new ArrayList();
                        for (Surface outputConfiguration : linkedList) {
                            list.add(new OutputConfiguration(outputConfiguration));
                        }
                        if (this.mInitHeifWriter != null) {
                            this.mHeifOutput = new OutputConfiguration(this.mInitHeifWriter.getInputSurface());
                            this.mHeifOutput.enableSurfaceSharing();
                            list.add(this.mHeifOutput);
                        }
                    } else {
                        list = null;
                    }
                    if (this.mChosenImageFormat != 35) {
                        if (this.mChosenImageFormat != 34) {
                            if (this.mSettingsManager.getSavePictureFormat() == 1) {
                                this.mCameraDevice[i].createCaptureSessionByOutputConfigurations(list, r3, null);
                            } else {
                                linkedList.add(this.mPreviewImageReader.getSurface());
                                this.mPreviewRequestBuilder[i].addTarget(this.mPreviewImageReader.getSurface());
                                this.mCameraDevice[i].createCaptureSession(linkedList, r3, null);
                            }
                        }
                    }
                    if (this.mPostProcessor.isZSLEnabled()) {
                        this.mPreviewRequestBuilder[i].addTarget(this.mImageReader[i].getSurface());
                        linkedList.add(this.mPostProcessor.getZSLReprocessImageReader().getSurface());
                        if (this.mSaveRaw) {
                            this.mPreviewRequestBuilder[i].addTarget(this.mRawImageReader[i].getSurface());
                        }
                        this.mCameraDevice[i].createReprocessableCaptureSession(new InputConfiguration(this.mImageReader[i].getWidth(), this.mImageReader[i].getHeight(), this.mImageReader[i].getImageFormat()), linkedList, r3, null);
                    } else if (this.mSettingsManager.getSavePictureFormat() != 1 || list == null) {
                        this.mCameraDevice[i].createCaptureSession(linkedList, r3, null);
                    } else {
                        this.mCameraDevice[i].createCaptureSessionByOutputConfigurations(list, r3, null);
                    }
                } else {
                    this.mPreviewRequestBuilder[i].addTarget(previewSurfaceForSession);
                    linkedList.add(previewSurfaceForSession);
                    linkedList.add(this.mImageReader[i].getSurface());
                    this.mCameraDevice[i].createCaptureSession(linkedList, r3, null);
                }
            } catch (CameraAccessException unused) {
                setCameraModeSwitcherAllowed(true);
            } catch (IllegalStateException unused2) {
                Log.v(str, "createSession: mPaused status occur Time out waiting for surface ");
                setCameraModeSwitcherAllowed(true);
            } catch (NullPointerException e) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("NullPointerException occurred error =");
                sb3.append(e.getMessage());
                Log.e(str, sb3.toString());
                setCameraModeSwitcherAllowed(true);
            } catch (IllegalArgumentException e2) {
                StringBuilder sb4 = new StringBuilder();
                sb4.append("IllegalArgumentException occurred error =");
                sb4.append(e2.getMessage());
                Log.e(str, sb4.toString());
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (CaptureModule.this.mShouldUpdateStorageStatus) {
                        int storageDebug = PersistUtil.getStorageDebug();
                        String str = CaptureModule.TAG;
                        if (storageDebug != 1) {
                            Log.d(str, "update storage status after createSession");
                            CaptureModule.this.mActivity.updateStorageSpaceAndHint();
                            CaptureModule.this.mShouldUpdateStorageStatus = false;
                        } else {
                            Log.d(str, "don't update storage status after createSession");
                        }
                    }
                    CaptureModule.this.mActivity.updateThumbnail(false);
                }
            });
        }
    }

    public void setAFModeToPreview(int i, int i2) {
        if (checkSessionAndBuilder(this.mCaptureSession[i], this.mPreviewRequestBuilder[i])) {
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("setAFModeToPreview ");
                sb.append(i2);
                Log.d(TAG, sb.toString());
            }
            if (this.mUI.getCurrentProMode() != 1) {
                this.mPreviewRequestBuilder[i].set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(i2));
                applyAFRegions(this.mPreviewRequestBuilder[i], i);
            } else {
                this.mPreviewRequestBuilder[i].set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Integer.valueOf(2));
                applyFocusDistance(this.mPreviewRequestBuilder[i], String.valueOf(this.mSettingsManager.getFocusValue(SettingsManager.KEY_FOCUS_DISTANCE)));
            }
            applyAERegions(this.mPreviewRequestBuilder[i], i);
            this.mPreviewRequestBuilder[i].setTag(Integer.valueOf(i));
            try {
                if (!this.mPostProcessor.isZSLEnabled() || getCameraMode() == 0) {
                    this.mPreviewRequestBuilder[i].set(makeup_level, Integer.valueOf(Integer.parseInt(this.mSettingsManager.getValue(SettingsManager.KEY_MAKEUP))));
                    this.mCaptureSession[i].setRepeatingRequest(this.mPreviewRequestBuilder[i].build(), this.mCaptureCallback, this.mCameraHandler);
                } else {
                    setRepeatingBurstForZSL(i);
                }
            } catch (CameraAccessException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    public void setFlashModeToPreview(int i, boolean z) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("setFlashModeToPreview ");
            sb.append(z);
            Log.d(TAG, sb.toString());
        }
        if (checkSessionAndBuilder(this.mCaptureSession[i], this.mPreviewRequestBuilder[i])) {
            if (z) {
                this.mPreviewRequestBuilder[i].set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(3));
                this.mPreviewRequestBuilder[i].set(CaptureRequest.FLASH_MODE, Integer.valueOf(1));
            } else {
                this.mPreviewRequestBuilder[i].set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
                this.mPreviewRequestBuilder[i].set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
            }
            applyAFRegions(this.mPreviewRequestBuilder[i], i);
            applyAERegions(this.mPreviewRequestBuilder[i], i);
            this.mPreviewRequestBuilder[i].setTag(Integer.valueOf(i));
            try {
                if (!this.mPostProcessor.isZSLEnabled() || getCameraMode() == 0) {
                    this.mCaptureSession[i].setRepeatingRequest(this.mPreviewRequestBuilder[i].build(), this.mCaptureCallback, this.mCameraHandler);
                } else {
                    setRepeatingBurstForZSL(0);
                }
            } catch (CameraAccessException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    public void setFocusDistanceToPreview(int i, float f) {
        if (checkSessionAndBuilder(this.mCaptureSession[i], this.mPreviewRequestBuilder[i])) {
            this.mPreviewRequestBuilder[i].set(CaptureRequest.LENS_FOCUS_DISTANCE, Float.valueOf(f));
            this.mPreviewRequestBuilder[i].setTag(Integer.valueOf(i));
            try {
                if (i != MONO_ID || canStartMonoPreview()) {
                    this.mCaptureSession[i].setRepeatingRequest(this.mPreviewRequestBuilder[i].build(), this.mCaptureCallback, this.mCameraHandler);
                } else {
                    this.mCaptureSession[i].capture(this.mPreviewRequestBuilder[i].build(), this.mCaptureCallback, this.mCameraHandler);
                }
            } catch (CameraAccessException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    public void reinit() {
        this.mSettingsManager.init();
    }

    public boolean isRefocus() {
        return this.mIsRefocus;
    }

    public boolean getRecordLocation() {
        String value = this.mSettingsManager.getValue("pref_camera_recordlocation_key");
        if (value == null) {
            value = "none";
        }
        return RecordLocationPreference.VALUE_ON.equals(value);
    }

    public void init(CameraActivity cameraActivity, View view) {
        Trace.beginSection("CaptureModule init");
        this.mActivity = cameraActivity;
        this.mRootView = view;
        this.mSettingsManager = SettingsManager.getInstance();
        this.mSettingsManager.registerListener(this);
        this.mSettingsManager.init();
        this.mFirstPreviewLoaded = false;
        Log.d(TAG, "init");
        for (int i = 0; i < 4; i++) {
            this.mCameraOpened[i] = false;
            this.mTakingPicture[i] = false;
        }
        for (int i2 = 0; i2 < 4; i2++) {
            this.mState[i2] = 0;
        }
        this.mPostProcessor = new PostProcessor(this.mActivity, this);
        this.mFrameProcessor = new FrameProcessor(this.mActivity, this);
        this.mContentResolver = this.mActivity.getContentResolver();
        initModeByIntent();
        this.mUI = new CaptureUI(cameraActivity, this, view);
        this.mUI.initializeControlByIntent();
        this.mFocusStateListener = new FocusStateListener(this.mUI);
        this.mLocationManager = new LocationManager(this.mActivity, this);
        startBackgroundThread();
        Trace.endSection();
    }

    private void initModeByIntent() {
        String action = this.mActivity.getIntent().getAction();
        if ("android.media.action.IMAGE_CAPTURE".equals(action)) {
            this.mIntentMode = 1;
        } else if ("android.media.action.IMAGE_CAPTURE_SECURE".equals(action)) {
            this.mIntentMode = 3;
        } else if ("android.media.action.VIDEO_CAPTURE".equals(action)) {
            this.mIntentMode = 2;
        }
        this.mQuickCapture = this.mActivity.getIntent().getBooleanExtra(EXTRA_QUICK_CAPTURE, false);
        Bundle extras = this.mActivity.getIntent().getExtras();
        if (extras != null) {
            this.mSaveUri = (Uri) extras.getParcelable("output");
            this.mCropValue = extras.getString("crop");
            this.mUseFrontCamera = extras.getBoolean("android.intent.extra.USE_FRONT_CAMERA", false);
            this.mTimer = extras.getInt("android.intent.extra.TIMER_DURATION_SECONDS", 0);
            StringBuilder sb = new StringBuilder();
            sb.append("mUseFrontCamera :");
            sb.append(this.mUseFrontCamera);
            sb.append(", mTimer :");
            sb.append(this.mTimer);
            Log.d(TAG, sb.toString());
        }
    }

    public boolean isQuickCapture() {
        return this.mQuickCapture;
    }

    public void setJpegImageData(byte[] bArr) {
        this.mJpegImageData = bArr;
    }

    public void showCapturedReview(final byte[] bArr, final int i) {
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                CaptureModule.this.mUI.showCapturedImageForReview(bArr, i);
            }
        });
    }

    public int getCurrentIntentMode() {
        return this.mIntentMode;
    }

    public void cancelCapture() {
        this.mActivity.finish();
    }

    /* access modifiers changed from: private */
    public void takePicture() {
        Log.d(TAG, "takePicture");
        if (!isLongShotActive() && !isSelfieMirrorOn()) {
            this.mTerribleThumbnailSingleExecutor.submit(this.mTerribleThumbnailThread);
        }
        this.mUI.enableShutter(false);
        this.mUI.setMediaSaved(false);
        if (this.mSettingsManager.isZSLInHALEnabled() && !isFlashOn(getMainCameraId())) {
            CaptureResult captureResult = this.mPreviewCaptureResult;
            if (!(captureResult == null || ((Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE)).intValue() == 4 || this.mPreviewCaptureResult.getRequest().get(CaptureRequest.CONTROL_AE_LOCK) == Boolean.TRUE)) {
                takeZSLPictureInHAL();
            }
        }
        if (isBackCamera()) {
            int cameraMode = getCameraMode();
            if (cameraMode == 0) {
                lockFocus(0);
                lockFocus(MONO_ID);
            } else if (cameraMode != 1) {
                if (cameraMode == 2) {
                    lockFocus(MONO_ID);
                } else if (cameraMode != 3) {
                    if (cameraMode == 4) {
                        lockFocus(BOKEH_ID);
                    }
                } else if (!takeZSLPicture(SWITCH_ID)) {
                    lockFocus(SWITCH_ID);
                }
            } else if (!takeZSLPicture(0)) {
                if (this.mLongshotActive) {
                    captureStillPicture(0);
                } else if (this.mUI.getCurrentProMode() == 1) {
                    this.isManualCapture = false;
                    this.ismanual = false;
                    lockAE(0);
                } else {
                    lockFocus(0);
                }
            }
        } else {
            int i = SWITCH_ID;
            if (i == -1) {
                i = FRONT_ID;
            }
            if (!takeZSLPicture(i)) {
                lockFocus(i);
            }
        }
    }

    private void lockAE(int i) {
        if (this.mActivity == null || this.mCameraDevice[i] == null || !checkSessionAndBuilder(this.mCaptureSession[i], this.mPreviewRequestBuilder[i])) {
            this.mUI.enableShutter(true);
            warningToast("Camera is not ready yet to take a picture.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("lockAE ");
        sb.append(i);
        Log.d(TAG, sb.toString());
        this.mTakingPicture[i] = true;
        try {
            Builder requestBuilder = getRequestBuilder(i);
            requestBuilder.setTag(Integer.valueOf(i));
            addPreviewSurface(requestBuilder, null, i);
            applyFocusDistance(requestBuilder, String.valueOf(this.mSettingsManager.getFocusValue(SettingsManager.KEY_FOCUS_DISTANCE)));
            requestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Integer.valueOf(1));
            applyFlash(requestBuilder, i);
            CaptureRequest build = requestBuilder.build();
            this.mState[i] = 3;
            this.mCaptureSession[i].capture(build, this.mCaptureCallback, this.mCameraHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private boolean isActionImageCapture() {
        return this.mIntentMode == 1;
    }

    private boolean takeZSLPicture(int i) {
        if (!this.mPostProcessor.isZSLEnabled() || !this.mPostProcessor.takeZSLPicture()) {
            return false;
        }
        checkAndPlayShutterSound(getMainCameraId());
        this.mUI.enableShutter(true);
        return true;
    }

    private void takeZSLPictureInHAL() {
        int i;
        Log.d(TAG, "takeHALZSLPicture");
        if (isBackCamera()) {
            int cameraMode = getCameraMode();
            i = 0;
            if (cameraMode == 0) {
                captureStillPicture(0);
                captureStillPicture(MONO_ID);
                return;
            } else if (cameraMode != 1) {
                if (cameraMode == 2) {
                    i = MONO_ID;
                } else if (cameraMode == 3) {
                    i = SWITCH_ID;
                } else if (cameraMode == 4) {
                    i = BOKEH_ID;
                }
            }
        } else {
            int i2 = SWITCH_ID;
            if (i2 == -1) {
                i2 = FRONT_ID;
            }
            i = i2;
        }
        captureStillPicture(i);
        captureStillPictureForHDRTest(i);
    }

    public boolean isLongShotActive() {
        return this.mLongshotActive;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x009c  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00af A[SYNTHETIC, Splitter:B:29:0x00af] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void lockFocus(int r6) {
        /*
            r5 = this;
            com.android.camera.CameraActivity r0 = r5.mActivity
            r1 = 1
            if (r0 == 0) goto L_0x00ea
            android.hardware.camera2.CameraDevice[] r0 = r5.mCameraDevice
            r0 = r0[r6]
            if (r0 == 0) goto L_0x00ea
            android.hardware.camera2.CameraCaptureSession[] r0 = r5.mCaptureSession
            r0 = r0[r6]
            android.hardware.camera2.CaptureRequest$Builder[] r2 = r5.mPreviewRequestBuilder
            r2 = r2[r6]
            boolean r0 = r5.checkSessionAndBuilder(r0, r2)
            if (r0 != 0) goto L_0x001b
            goto L_0x00ea
        L_0x001b:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "lockFocus "
            r0.append(r2)
            r0.append(r6)
            java.lang.String r0 = r0.toString()
            java.lang.String r2 = "SnapCam_CaptureModule"
            android.util.Log.d(r2, r0)
            int r0 = MONO_ID     // Catch:{ CameraAccessException -> 0x008d }
            if (r6 != r0) goto L_0x004f
            boolean r0 = r5.canStartMonoPreview()     // Catch:{ CameraAccessException -> 0x008d }
            if (r0 != 0) goto L_0x004f
            android.hardware.camera2.CameraCaptureSession[] r0 = r5.mCaptureSession     // Catch:{ CameraAccessException -> 0x008d }
            r0 = r0[r6]     // Catch:{ CameraAccessException -> 0x008d }
            android.hardware.camera2.CaptureRequest$Builder[] r2 = r5.mPreviewRequestBuilder     // Catch:{ CameraAccessException -> 0x008d }
            r2 = r2[r6]     // Catch:{ CameraAccessException -> 0x008d }
            android.hardware.camera2.CaptureRequest r2 = r2.build()     // Catch:{ CameraAccessException -> 0x008d }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r3 = r5.mCaptureCallback     // Catch:{ CameraAccessException -> 0x008d }
            android.os.Handler r4 = r5.mCameraHandler     // Catch:{ CameraAccessException -> 0x008d }
            r0.setRepeatingRequest(r2, r3, r4)     // Catch:{ CameraAccessException -> 0x008d }
            goto L_0x0091
        L_0x004f:
            boolean r0 = r5.mLongshotActive     // Catch:{ CameraAccessException -> 0x008d }
            if (r0 == 0) goto L_0x0091
            boolean r0 = r5.isFlashOn(r6)     // Catch:{ CameraAccessException -> 0x008d }
            if (r0 == 0) goto L_0x0091
            android.hardware.camera2.CameraCaptureSession[] r0 = r5.mCaptureSession     // Catch:{ CameraAccessException -> 0x008d }
            r0 = r0[r6]     // Catch:{ CameraAccessException -> 0x008d }
            r0.stopRepeating()     // Catch:{ CameraAccessException -> 0x008d }
            android.hardware.camera2.CaptureRequest$Builder[] r0 = r5.mPreviewRequestBuilder     // Catch:{ CameraAccessException -> 0x008d }
            r0 = r0[r6]     // Catch:{ CameraAccessException -> 0x008d }
            r5.applyFlash(r0, r6)     // Catch:{ CameraAccessException -> 0x008d }
            com.android.camera.imageprocessor.PostProcessor r0 = r5.mPostProcessor     // Catch:{ CameraAccessException -> 0x008d }
            boolean r0 = r0.isZSLEnabled()     // Catch:{ CameraAccessException -> 0x008d }
            if (r0 == 0) goto L_0x0079
            int r0 = r5.getCameraMode()     // Catch:{ CameraAccessException -> 0x008d }
            if (r0 == 0) goto L_0x0079
            r5.setRepeatingBurstForZSL(r6)     // Catch:{ CameraAccessException -> 0x008d }
            goto L_0x0091
        L_0x0079:
            android.hardware.camera2.CameraCaptureSession[] r0 = r5.mCaptureSession     // Catch:{ CameraAccessException -> 0x008d }
            r0 = r0[r6]     // Catch:{ CameraAccessException -> 0x008d }
            android.hardware.camera2.CaptureRequest$Builder[] r2 = r5.mPreviewRequestBuilder     // Catch:{ CameraAccessException -> 0x008d }
            r2 = r2[r6]     // Catch:{ CameraAccessException -> 0x008d }
            android.hardware.camera2.CaptureRequest r2 = r2.build()     // Catch:{ CameraAccessException -> 0x008d }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r3 = r5.mCaptureCallback     // Catch:{ CameraAccessException -> 0x008d }
            android.os.Handler r4 = r5.mCameraHandler     // Catch:{ CameraAccessException -> 0x008d }
            r0.setRepeatingRequest(r2, r3, r4)     // Catch:{ CameraAccessException -> 0x008d }
            goto L_0x0091
        L_0x008d:
            r0 = move-exception
            r0.printStackTrace()
        L_0x0091:
            boolean[] r0 = r5.mTakingPicture
            r0[r6] = r1
            int[] r0 = r5.mState
            r0 = r0[r6]
            r2 = 5
            if (r0 != r2) goto L_0x00af
            android.os.Handler r0 = r5.mCameraHandler
            java.lang.String[] r2 = r5.mCameraId
            r2 = r2[r6]
            r0.removeMessages(r1, r2)
            int[] r0 = r5.mState
            r0[r6] = r1
            int[] r5 = r5.mLockRequestHashCode
            r0 = 0
            r5[r6] = r0
            return
        L_0x00af:
            android.hardware.camera2.CaptureRequest$Builder r0 = r5.getRequestBuilder(r6)     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r6)     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            r0.setTag(r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            r2 = 0
            r5.addPreviewSurface(r0, r2, r6)     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            r5.applySettingsForLockFocus(r0, r6)     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            android.hardware.camera2.CaptureRequest r0 = r0.build()     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            int[] r2 = r5.mLockRequestHashCode     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            int r3 = r0.hashCode()     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            r2[r6] = r3     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            int[] r2 = r5.mState     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            r2[r6] = r1     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            android.hardware.camera2.CameraCaptureSession[] r1 = r5.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            r6 = r1[r6]     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r1 = r5.mCaptureCallback     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            android.os.Handler r2 = r5.mCameraHandler     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            r6.capture(r0, r1, r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            boolean r6 = r5.mHiston     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            if (r6 == 0) goto L_0x00e9
            r6 = 4
            r5.updateGraghViewVisibility(r6)     // Catch:{ CameraAccessException | IllegalStateException -> 0x00e5 }
            goto L_0x00e9
        L_0x00e5:
            r5 = move-exception
            r5.printStackTrace()
        L_0x00e9:
            return
        L_0x00ea:
            com.android.camera.CaptureUI r6 = r5.mUI
            r6.enableShutter(r1)
            java.lang.String r6 = "Camera is not ready yet to take a picture."
            r5.warningToast(r6)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CaptureModule.lockFocus(int):void");
    }

    private void autoFocusTrigger(int i) {
        boolean z = DEBUG;
        String str = TAG;
        if (z) {
            StringBuilder sb = new StringBuilder();
            sb.append("autoFocusTrigger ");
            sb.append(i);
            Log.d(str, sb.toString());
        }
        if (this.mActivity == null || this.mCameraDevice[i] == null || !checkSessionAndBuilder(this.mCaptureSession[i], this.mPreviewRequestBuilder[i])) {
            warningToast("Camera is not ready yet to take a picture.");
            return;
        }
        try {
            Builder requestBuilder = getRequestBuilder(i);
            requestBuilder.setTag(Integer.valueOf(i));
            addPreviewSurface(requestBuilder, null, i);
            requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(2));
            try {
                this.mCaptureSession[i].capture(requestBuilder.build(), null, this.mCameraHandler);
            } catch (Throwable th) {
                Log.e(str, "cancelAutoFocus() - Fail to cancel autofocus", th);
            }
            requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
            this.mControlAFMode = 1;
            applySettingsForAutoFocus(requestBuilder, i);
            this.mState[i] = 5;
            this.mCaptureSession[i].capture(requestBuilder.build(), this.mCaptureCallback, this.mCameraHandler);
            setAFModeToPreview(i, this.mControlAFMode);
            this.mCameraHandler.sendMessageDelayed(this.mCameraHandler.obtainMessage(1, i, 0, this.mCameraId[i]), (long) CANCEL_TOUCH_FOCUS_DELAY);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void linkBayerMono(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("linkBayerMono ");
        sb.append(i);
        Log.d(TAG, sb.toString());
        Byte valueOf = Byte.valueOf(1);
        if (i == 0) {
            this.mPreviewRequestBuilder[i].set(this.BayerMonoLinkEnableKey, valueOf);
            this.mPreviewRequestBuilder[i].set(this.BayerMonoLinkMainKey, valueOf);
            this.mPreviewRequestBuilder[i].set(this.BayerMonoLinkSessionIdKey, Integer.valueOf(MONO_ID));
        } else if (i == MONO_ID) {
            this.mPreviewRequestBuilder[i].set(this.BayerMonoLinkEnableKey, valueOf);
            this.mPreviewRequestBuilder[i].set(this.BayerMonoLinkMainKey, Byte.valueOf(0));
            this.mPreviewRequestBuilder[i].set(this.BayerMonoLinkSessionIdKey, Integer.valueOf(0));
        }
    }

    public void unLinkBayerMono(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("unlinkBayerMono ");
        sb.append(i);
        Log.d(TAG, sb.toString());
        Byte valueOf = Byte.valueOf(0);
        if (i == 0) {
            this.mPreviewRequestBuilder[i].set(this.BayerMonoLinkEnableKey, valueOf);
        } else if (i == MONO_ID) {
            this.mPreviewRequestBuilder[i].set(this.BayerMonoLinkEnableKey, valueOf);
        }
    }

    public PostProcessor getPostProcessor() {
        return this.mPostProcessor;
    }

    private void applyCaptureSWMFNR(Builder builder) {
        boolean isSWMFNREnabled = isSWMFNREnabled();
        StringBuilder sb = new StringBuilder();
        sb.append("applyCaptureSWMFNR swmfnrEnable :");
        sb.append(isSWMFNREnabled);
        String sb2 = sb.toString();
        String str = TAG;
        Log.v(str, sb2);
        try {
            builder.set(swMFNR, Byte.valueOf((byte) (isSWMFNREnabled ? 1 : 0)));
        } catch (IllegalArgumentException unused) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("cannot find vendor tag: ");
            sb3.append(swMFNR.toString());
            Log.w(str, sb3.toString());
        }
    }

    private void captureStillPicture(int i) {
        long j;
        int i2 = i;
        String str = SettingsManager.KEY_MAKEUP_WHITEN_DEGREE;
        String str2 = SettingsManager.KEY_MAKEUP_CLEAN_DEGREE;
        StringBuilder sb = new StringBuilder();
        sb.append("captureStillPicture ");
        sb.append(i2);
        String sb2 = sb.toString();
        String str3 = TAG;
        Log.d(str3, sb2);
        String str4 = null;
        this.mJpegImageData = null;
        this.mIsRefocus = false;
        try {
            if (this.mActivity != null) {
                if (this.mCameraDevice[i2] != null) {
                    Builder createCaptureRequest = this.mCameraDevice[i2].createCaptureRequest(2);
                    if (this.mSettingsManager.isZSLInHALEnabled()) {
                        createCaptureRequest.set(CaptureRequest.CONTROL_ENABLE_ZSL, Boolean.valueOf(true));
                    } else {
                        createCaptureRequest.set(CaptureRequest.CONTROL_ENABLE_ZSL, Boolean.valueOf(false));
                    }
                    String value = this.mSettingsManager.getValue(SettingsManager.KEY_FLASH_MODE);
                    if (!this.mIsToNightMode || this.mLongshotActive || isSceneMode() || this.mUI.isBokehMode || this.mUI.isMakeUp || PersistUtil.getFeatureDebug() != 1 || !"off".equals(value)) {
                        Log.d(str3, "captureStillPicture night disable");
                    } else {
                        Log.d(str3, "captureStillPicture night enable");
                        createCaptureRequest.set(night_enable, Boolean.valueOf(true));
                        this.mIsToNightModeSuccess = true;
                    }
                    if (this.mSettingsManager.getValue(str2) != null) {
                        int parseInt = Integer.parseInt(this.mSettingsManager.getValue(str2));
                        if (this.mUI.isMakeUp && parseInt != 0) {
                            createCaptureRequest.set(makeup_clean_level, Integer.valueOf(parseInt));
                        }
                    }
                    if (this.mSettingsManager.getValue(str) != null) {
                        int parseInt2 = Integer.parseInt(this.mSettingsManager.getValue(str));
                        if (this.mUI.isMakeUp && parseInt2 != 0) {
                            createCaptureRequest.set(makeup_whiten_level, Integer.valueOf(parseInt2));
                        }
                    }
                    applySettingsForJpegInformation(createCaptureRequest, i2);
                    if (!this.mIsSupportedQcfa) {
                        addPreviewSurface(createCaptureRequest, null, i2);
                    }
                    if (this.mUI.getCurrentProMode() != 1) {
                        applyAFRegions(createCaptureRequest, i2);
                        applyAERegions(createCaptureRequest, i2);
                    }
                    if (!isLongShotSettingEnabled()) {
                        applyCaptureSWMFNR(createCaptureRequest);
                    }
                    if (this.mUI.getCurrentProMode() == 1) {
                        float focusValue = this.mSettingsManager.getFocusValue(SettingsManager.KEY_FOCUS_DISTANCE);
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append("captureStillPicture : ");
                        sb3.append(focusValue);
                        Log.d(str3, sb3.toString());
                        applyFocusDistance(createCaptureRequest, String.valueOf(focusValue));
                        applyFlash(createCaptureRequest, i2);
                    } else {
                        applySettingsForCapture(createCaptureRequest, i2);
                    }
                    String value2 = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
                    if (value2 != null && value2.equals("18")) {
                        this.mUI.showWaitingProgress(true);
                    }
                    if (isClearSightOn()) {
                        captureStillPictureForClearSight(i);
                    } else if (i2 != getMainCameraId() || !this.mPostProcessor.isFilterOn()) {
                        if (this.mSaveRaw) {
                            createCaptureRequest.addTarget(this.mRawImageReader[i2].getSurface());
                        }
                        if (this.mSettingsManager.getSavePictureFormat() == 1) {
                            this.mNamedImages.nameNewImage(System.currentTimeMillis());
                            NamedEntity nextNameEntity = this.mNamedImages.getNextNameEntity();
                            if (nextNameEntity != null) {
                                str4 = nextNameEntity.title;
                            }
                            String str5 = str4;
                            if (nextNameEntity == null) {
                                j = -1;
                            } else {
                                j = nextNameEntity.date;
                            }
                            long j2 = j;
                            String generateFilepath = Storage.generateFilepath(str5, this.mLongshotActive ? "heifs" : "heif");
                            int qualityNumber = getQualityNumber(this.mSettingsManager.getValue("pref_camera_jpegquality_key"));
                            int jpegRotation = CameraUtil.getJpegRotation(i2, this.mOrientation);
                            HeifWriter createHEIFEncoder = createHEIFEncoder(generateFilepath, this.mPictureSize.getWidth(), this.mPictureSize.getHeight(), jpegRotation, this.mLongshotActive ? mLongShotLimitNums : 1, qualityNumber);
                            if (createHEIFEncoder != null) {
                                HeifImage heifImage = new HeifImage(createHEIFEncoder, generateFilepath, str5, j2, jpegRotation, qualityNumber);
                                this.mHeifImage = heifImage;
                                Surface inputSurface = createHEIFEncoder.getInputSurface();
                                this.mHeifOutput.addSurface(inputSurface);
                                try {
                                    if (this.mCaptureSession[i2] != null) {
                                        this.mCaptureSession[i2].updateOutputConfiguration(this.mHeifOutput);
                                    }
                                    createCaptureRequest.addTarget(inputSurface);
                                    createHEIFEncoder.start();
                                } catch (IllegalArgumentException | IllegalStateException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (this.mImageReader[i2] != null) {
                            createCaptureRequest.addTarget(this.mImageReader[i2].getSurface());
                        }
                        if (!this.mPaused && this.mCamerasOpened) {
                            if (this.mCurrentSession != null) {
                                if (!(this.mIsSupportedQcfa || this.mUI.getCurrentProMode() == 1 || this.mCaptureSession[i2] == null)) {
                                    this.mCaptureSession[i2].stopRepeating();
                                }
                                if (this.mLongshotActive) {
                                    captureStillPictureForLongshot(createCaptureRequest, i2);
                                } else {
                                    captureStillPictureForCommon(createCaptureRequest, i2);
                                }
                            }
                        }
                        return;
                    } else {
                        captureStillPictureForFilter(createCaptureRequest, i2);
                    }
                    return;
                }
            }
            warningToast("Camera is not ready yet to take a picture.");
        } catch (CameraAccessException e2) {
            Log.d(str3, "Capture still picture has failed");
            e2.printStackTrace();
        }
    }

    private void captureStillPictureForClearSight(int i) throws CameraAccessException {
        Builder createCaptureRequest = ClearSightImageProcessor.getInstance().createCaptureRequest(this.mCameraDevice[i]);
        boolean z = true;
        if (this.mSettingsManager.isZSLInHALEnabled()) {
            createCaptureRequest.set(CaptureRequest.CONTROL_ENABLE_ZSL, Boolean.valueOf(true));
        } else {
            createCaptureRequest.set(CaptureRequest.CONTROL_ENABLE_ZSL, Boolean.valueOf(false));
        }
        applySettingsForJpegInformation(createCaptureRequest, i);
        addPreviewSurface(createCaptureRequest, null, i);
        VendorTagUtil.setCdsMode(createCaptureRequest, Integer.valueOf(0));
        applySettingsForCapture(createCaptureRequest, i);
        applySettingsForLockExposure(createCaptureRequest, i);
        checkAndPlayShutterSound(i);
        if (!this.mPaused && this.mCamerasOpened) {
            ClearSightImageProcessor instance = ClearSightImageProcessor.getInstance();
            if (i != 0) {
                z = false;
            }
            instance.capture(z, this.mCaptureSession[i], createCaptureRequest, this.mCaptureCallbackHandler);
        }
    }

    private void captureStillPictureForFilter(Builder builder, int i) throws CameraAccessException {
        applySettingsForLockExposure(builder, i);
        checkAndPlayShutterSound(i);
        if (!this.mPaused && this.mCamerasOpened) {
            this.mCaptureSession[i].stopRepeating();
            builder.addTarget(this.mImageReader[i].getSurface());
            if (this.mSaveRaw) {
                builder.addTarget(this.mRawImageReader[i].getSurface());
            }
            this.mPostProcessor.onStartCapturing();
            if (this.mPostProcessor.isManualMode()) {
                this.mPostProcessor.manualCapture(builder, this.mCaptureSession[i], this.mCaptureCallbackHandler);
                return;
            }
            this.mCaptureSession[i].captureBurst(this.mPostProcessor.setRequiredImages(builder), this.mPostProcessor.getCaptureCallback(), this.mCaptureCallbackHandler);
        }
    }

    private void captureStillPictureForLongshot(Builder builder, int i) throws CameraAccessException {
        StringBuilder sb = new StringBuilder();
        sb.append("captureStillPictureForLongshot ");
        sb.append(i);
        Log.d(TAG, sb.toString());
        ArrayList arrayList = new ArrayList();
        int isBurstShotFpsNums = PersistUtil.isBurstShotFpsNums();
        for (int i2 = 0; i2 < mLongShotLimitNums; i2++) {
            for (int i3 = 0; i3 < isBurstShotFpsNums; i3++) {
                this.mPreviewRequestBuilder[i].setTag("preview");
                arrayList.add(this.mPreviewRequestBuilder[i].build());
            }
            builder.setTag("capture");
            arrayList.add(builder.build());
        }
        this.mCaptureSession[i].captureBurst(arrayList, this.mLongshotCallBack, this.mCaptureCallbackHandler);
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                CaptureModule.this.mUI.enableVideo(false);
            }
        });
    }

    private void captureStillPictureForCommon(Builder builder, final int i) throws CameraAccessException {
        checkAndPlayShutterSound(i);
        if (isMpoOn()) {
            this.mCaptureStartTime = System.currentTimeMillis();
            this.mMpoSaveHandler.obtainMessage(0, Long.valueOf(this.mCaptureStartTime)).sendToTarget();
        }
        if (this.mCaptureSession[i] != null) {
            int i2 = this.mChosenImageFormat;
            if (i2 == 35 || i2 == 34) {
                this.mPostProcessor.onStartCapturing();
                this.mCaptureSession[i].capture(builder.build(), this.mPostProcessor.getCaptureCallback(), this.mCaptureCallbackHandler);
            } else {
                if (this.mPostProcessor.isSelfieMirrorOn()) {
                    onStartCapturing();
                }
                this.mCaptureSession[i].capture(builder.build(), new CaptureCallback() {
                    public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("captureStillPictureForCommon onCaptureCompleted: ");
                        sb.append(i);
                        Log.d(CaptureModule.TAG, sb.toString());
                        if (CaptureModule.this.mTotalCaptureResultList.size() <= 3) {
                            CaptureModule.this.mTotalCaptureResultList.add(totalCaptureResult);
                        }
                    }

                    public void onCaptureFailed(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureFailure captureFailure) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("captureStillPictureForCommon onCaptureFailed: ");
                        sb.append(i);
                        Log.d(CaptureModule.TAG, sb.toString());
                    }

                    public void onCaptureSequenceCompleted(CameraCaptureSession cameraCaptureSession, int i, long j) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("captureStillPictureForCommon onCaptureSequenceCompleted: ");
                        sb.append(i);
                        Log.d(CaptureModule.TAG, sb.toString());
                        if (CaptureModule.this.mUI.getCurrentProMode() == 1) {
                            CaptureModule.this.unlockFocus(i);
                            CaptureModule.this.enableShutterAndVideoOnUiThread(i);
                            CaptureModule.this.isManualCapture = false;
                        } else {
                            CaptureModule.this.unlockFocus(i);
                        }
                        if (CaptureModule.this.mSettingsManager.getSavePictureFormat() == 1 && CaptureModule.this.mHeifImage != null) {
                            try {
                                CaptureModule.this.mHeifImage.getWriter().stop(3000);
                                CaptureModule.this.mHeifImage.getWriter().close();
                                CaptureModule.this.mActivity.getMediaSaveService().addHEIFImage(CaptureModule.this.mHeifImage.getPath(), CaptureModule.this.mHeifImage.getTitle(), CaptureModule.this.mHeifImage.getDate(), null, CaptureModule.this.mPictureSize.getWidth(), CaptureModule.this.mPictureSize.getHeight(), CaptureModule.this.mHeifImage.getOrientation(), null, CaptureModule.this.mContentResolver, CaptureModule.this.mOnMediaSavedListener, CaptureModule.this.mHeifImage.getQuality(), "heif");
                                try {
                                    CaptureModule.this.mHeifOutput.removeSurface(CaptureModule.this.mHeifImage.getInputSurface());
                                    CaptureModule.this.mCaptureSession[i].updateOutputConfiguration(CaptureModule.this.mHeifOutput);
                                    CaptureModule.this.mHeifImage = null;
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            } catch (Exception e3) {
                                e3.printStackTrace();
                                CaptureModule.this.mHeifOutput.removeSurface(CaptureModule.this.mHeifImage.getInputSurface());
                                CaptureModule.this.mCaptureSession[i].updateOutputConfiguration(CaptureModule.this.mHeifOutput);
                                CaptureModule.this.mHeifImage = null;
                            } catch (Throwable th) {
                                Throwable th2 = th;
                                try {
                                    CaptureModule.this.mHeifOutput.removeSurface(CaptureModule.this.mHeifImage.getInputSurface());
                                    CaptureModule.this.mCaptureSession[i].updateOutputConfiguration(CaptureModule.this.mHeifOutput);
                                    CaptureModule.this.mHeifImage = null;
                                } catch (CameraAccessException e4) {
                                    e4.printStackTrace();
                                } catch (Exception e5) {
                                    e5.printStackTrace();
                                }
                                throw th2;
                            }
                        }
                    }
                }, this.mCaptureCallbackHandler);
            }
        }
    }

    private void captureVideoSnapshot(int i) {
        String str;
        long j;
        final int i2 = i;
        StringBuilder sb = new StringBuilder();
        sb.append("captureVideoSnapshot ");
        sb.append(i2);
        String sb2 = sb.toString();
        String str2 = TAG;
        Log.d(str2, sb2);
        try {
            if (!(this.mActivity == null || this.mCameraDevice[i2] == null)) {
                if (this.mCurrentSession != null) {
                    this.mUI.enableShutter(false);
                    this.mUI.setMediaSaved(false);
                    Builder createCaptureRequest = this.mCameraDevice[i2].createCaptureRequest(4);
                    createCaptureRequest.set(CaptureRequest.JPEG_ORIENTATION, Integer.valueOf(CameraUtil.getJpegRotation(i2, this.mOrientation)));
                    createCaptureRequest.set(CaptureRequest.JPEG_THUMBNAIL_SIZE, this.mVideoSnapshotThumbSize);
                    createCaptureRequest.set(CaptureRequest.JPEG_THUMBNAIL_QUALITY, Byte.valueOf(80));
                    applyVideoSnapshot(createCaptureRequest, i2);
                    applyZoom(createCaptureRequest, i2);
                    if (this.mSettingsManager.getSavePictureFormat() == 1) {
                        this.mNamedImages.nameNewImage(System.currentTimeMillis());
                        NamedEntity nextNameEntity = this.mNamedImages.getNextNameEntity();
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
                        long j2 = j;
                        String generateFilepath = Storage.generateFilepath(str3, "heif");
                        int qualityNumber = getQualityNumber(this.mSettingsManager.getValue("pref_camera_jpegquality_key"));
                        int jpegRotation = CameraUtil.getJpegRotation(i2, this.mOrientation);
                        HeifWriter createHEIFEncoder = createHEIFEncoder(generateFilepath, this.mVideoSize.getWidth(), this.mVideoSize.getHeight(), jpegRotation, 1, qualityNumber);
                        if (createHEIFEncoder != null) {
                            HeifImage heifImage = r8;
                            HeifImage heifImage2 = new HeifImage(createHEIFEncoder, generateFilepath, str3, j2, jpegRotation, qualityNumber);
                            this.mLiveShotImage = heifImage;
                            Surface inputSurface = createHEIFEncoder.getInputSurface();
                            this.mLiveShotOutput.addSurface(inputSurface);
                            try {
                                this.mCurrentSession.updateOutputConfiguration(this.mLiveShotOutput);
                                createCaptureRequest.addTarget(inputSurface);
                                createHEIFEncoder.start();
                            } catch (IllegalArgumentException | IllegalStateException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        createCaptureRequest.addTarget(this.mVideoSnapshotImageReader.getSurface());
                    }
                    Surface previewSurfaceForSession = getPreviewSurfaceForSession(i);
                    if (getFrameProcFilterId().size() == 1 && ((Integer) getFrameProcFilterId().get(0)).intValue() == 1) {
                        createCaptureRequest.addTarget((Surface) this.mFrameProcessor.getInputSurfaces().get(0));
                    } else {
                        createCaptureRequest.addTarget(previewSurfaceForSession);
                    }
                    addPreviewSurface(createCaptureRequest, new ArrayList(), i2);
                    this.mCurrentSession.capture(createCaptureRequest.build(), new CaptureCallback() {
                        public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("captureVideoSnapshot onCaptureCompleted: ");
                            sb.append(i2);
                            Log.d(CaptureModule.TAG, sb.toString());
                        }

                        public void onCaptureFailed(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureFailure captureFailure) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("captureVideoSnapshot onCaptureFailed: ");
                            sb.append(i2);
                            Log.d(CaptureModule.TAG, sb.toString());
                        }

                        public void onCaptureSequenceCompleted(CameraCaptureSession cameraCaptureSession, int i, long j) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("captureVideoSnapshot onCaptureSequenceCompleted: ");
                            sb.append(i2);
                            Log.d(CaptureModule.TAG, sb.toString());
                            if (CaptureModule.this.mSettingsManager.getSavePictureFormat() == 1) {
                                if (CaptureModule.this.mLiveShotImage != null) {
                                    try {
                                        CaptureModule.this.mLiveShotImage.getWriter().stop(3000);
                                        CaptureModule.this.mLiveShotImage.getWriter().close();
                                        CaptureModule.this.mLiveShotOutput.removeSurface(CaptureModule.this.mLiveShotImage.getInputSurface());
                                        CaptureModule.this.mCurrentSession.updateOutputConfiguration(CaptureModule.this.mLiveShotOutput);
                                        CaptureModule.this.mActivity.getMediaSaveService().addHEIFImage(CaptureModule.this.mLiveShotImage.getPath(), CaptureModule.this.mLiveShotImage.getTitle(), CaptureModule.this.mLiveShotImage.getDate(), null, CaptureModule.this.mVideoSize.getWidth(), CaptureModule.this.mVideoSize.getHeight(), CaptureModule.this.mLiveShotImage.getOrientation(), null, CaptureModule.this.mContentResolver, CaptureModule.this.mOnMediaSavedListener, CaptureModule.this.mLiveShotImage.getQuality(), "heif");
                                        CaptureModule.this.mLiveShotImage = null;
                                    } catch (IllegalStateException | TimeoutException e) {
                                        e.printStackTrace();
                                    } catch (Exception e2) {
                                        e2.printStackTrace();
                                    }
                                }
                                CaptureModule.this.mActivity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Log.d(CaptureModule.TAG, "heif image available then enable shutter button ");
                                        CaptureModule.this.mUI.enableShutter(true);
                                    }
                                });
                            }
                        }
                    }, this.mCaptureCallbackHandler);
                    return;
                }
            }
            warningToast("Camera is not ready yet to take a video snapshot.");
        } catch (CameraAccessException | IllegalStateException e2) {
            Log.d(str2, "captureVideoSnapshot failed");
            e2.printStackTrace();
        }
    }

    private void runPrecaptureSequence(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("runPrecaptureSequence: ");
        sb.append(i);
        Log.d(TAG, sb.toString());
        if (checkSessionAndBuilder(this.mCaptureSession[i], this.mPreviewRequestBuilder[i])) {
            try {
                Builder requestBuilder = getRequestBuilder(i);
                requestBuilder.setTag(Integer.valueOf(i));
                addPreviewSurface(requestBuilder, null, i);
                applySettingsForPrecapture(requestBuilder, i);
                if (this.mUI.getCurrentProMode() == 1) {
                    applyFocusDistance(requestBuilder, String.valueOf(this.mSettingsManager.getFocusValue(SettingsManager.KEY_FOCUS_DISTANCE)));
                }
                applyFlash(requestBuilder, i);
                CaptureRequest build = requestBuilder.build();
                this.mPrecaptureRequestHashCode[i] = build.hashCode();
                this.mState[i] = 2;
                this.mCaptureSession[i].capture(build, this.mCaptureCallback, this.mCameraHandler);
            } catch (CameraAccessException | IllegalStateException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public CameraCharacteristics getMainCameraCharacteristics() {
        return this.mMainCameraCharacteristics;
    }

    private void setUpCameraOutputs(int i) {
        Log.d(TAG, "setUpCameraOutputs");
        CameraManager cameraManager = (CameraManager) this.mActivity.getSystemService("camera");
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            if (this.mSettingsManager.getSavePictureFormat() == 1) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.mActivity.getCacheDir().getPath());
                sb.append("/heif.tmp");
                String sb2 = sb.toString();
                if (this.mInitHeifWriter != null) {
                    this.mInitHeifWriter.close();
                }
                this.mInitHeifWriter = createHEIFEncoder(sb2, this.mPictureSize.getWidth(), this.mPictureSize.getHeight(), 0, 1, 85);
            }
            for (int i2 = 0; i2 < cameraIdList.length; i2++) {
                String str = cameraIdList[i2];
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(str);
                if (isInMode(i2)) {
                    this.mCameraIdList.add(Integer.valueOf(i2));
                }
                if (i2 == getMainCameraId()) {
                    this.mBayerCameraRegion = (Rect) cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    this.mMainCameraCharacteristics = cameraCharacteristics;
                }
                StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (streamConfigurationMap != null) {
                    this.mCameraId[i2] = str;
                    if (isClearSightOn()) {
                        if (i2 == getMainCameraId()) {
                            ClearSightImageProcessor.getInstance().init(streamConfigurationMap, this.mActivity, this.mOnMediaSavedListener);
                            ClearSightImageProcessor.getInstance().setCallback(this);
                        }
                    } else if ((i == 35 || i == 34) && i2 == getMainCameraId()) {
                        Size size = this.mPictureSize;
                        if (this.mPostProcessor.isZSLEnabled() && this.mPictureSize.getWidth() * this.mPictureSize.getHeight() < 5038848 && !this.mSettingsManager.getIsSupportedQcfa(i2)) {
                            size = new Size(2592, 1944);
                        }
                        this.mImageReader[i2] = ImageReader.newInstance(size.getWidth(), size.getHeight(), i, 10);
                        if (this.mSaveRaw) {
                            this.mRawImageReader[i2] = ImageReader.newInstance(this.mSupportedRawPictureSize.getWidth(), this.mSupportedRawPictureSize.getHeight(), 37, this.mPostProcessor.getMaxRequiredImageNum() + 1);
                            this.mPostProcessor.setRawImageReader(this.mRawImageReader[i2]);
                        }
                        this.mImageReader[i2].setOnImageAvailableListener(this.mPostProcessor.getImageHandler(), this.mImageAvailableHandler);
                        this.mPostProcessor.onImageReaderReady(this.mImageReader[i2], this.mSupportedMaxPictureSize, this.mPictureSize);
                    } else if (i2 == getMainCameraId()) {
                        this.mImageReader[i2] = ImageReader.newInstance(this.mPictureSize.getWidth(), this.mPictureSize.getHeight(), i, 10);
                        this.mPreviewImageReader = ImageReader.newInstance(this.mPreviewSize.getWidth(), this.mPreviewSize.getHeight(), 35, 8);
                        this.mPreviewImageReader.setOnImageAvailableListener(new ImageAvailableListener(i2) {
                            public void onImageAvailable(ImageReader imageReader) {
                                Image acquireNextImage = imageReader.acquireNextImage();
                                try {
                                    CaptureModule.this.mAISenceDetectThread.setImageData(acquireNextImage);
                                    CaptureModule.this.mAISenceDetectThread.setOrientation((CaptureModule.this.mOrientation + 90) % 360);
                                    CaptureModule.this.mAISenceDetectThread.setSize(CaptureModule.this.mPreviewSize.getWidth(), CaptureModule.this.mPreviewSize.getHeight(), CaptureModule.this.mPreviewSize.getWidth(), CaptureModule.this.mPreviewSize.getHeight());
                                    CaptureModule.this.mAISenceSingleThreadExecutor.submit(CaptureModule.this.mAISenceDetectThread);
                                    if (acquireNextImage == null) {
                                        return;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    if (acquireNextImage == null) {
                                        return;
                                    }
                                } catch (Throwable th) {
                                    if (acquireNextImage != null) {
                                        acquireNextImage.close();
                                    }
                                    throw th;
                                }
                                acquireNextImage.close();
                            }
                        }, this.mImageAvailableHandler);
                        C056916 r5 = new ImageAvailableListener(i2) {
                            public void onImageAvailable(ImageReader imageReader) {
                                long j;
                                if (CaptureModule.this.mIsSupportedQcfa || CaptureModule.this.mBokehEnabled) {
                                    CaptureModule.this.mHandler.post(new Runnable() {
                                        public void run() {
                                            CaptureModule.this.mUI.enableShutter(true);
                                        }
                                    });
                                }
                                boolean z = CaptureModule.DEBUG;
                                String str = CaptureModule.TAG;
                                if (z) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("Image available mFrameSendNums :");
                                    sb.append(CaptureModule.this.mFrameSendNums.get());
                                    sb.append(", mImageArrivedNums :");
                                    sb.append(CaptureModule.this.mImageArrivedNums.get());
                                    Log.v(str, sb.toString());
                                }
                                Image acquireNextImage = imageReader.acquireNextImage();
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("image available for cam: ");
                                sb2.append(this.mCamId);
                                Log.d(str, sb2.toString());
                                CaptureModule.this.mHandler.post(new Runnable() {
                                    public void run() {
                                        CaptureModule.this.mUI.showWaitingProgress(false);
                                    }
                                });
                                String str2 = null;
                                if (CaptureModule.this.mIsToNightModeSuccess) {
                                    try {
                                        Builder access$8700 = CaptureModule.this.getRequestBuilder(CaptureModule.this.mOpenCameraId);
                                        access$8700.setTag(Integer.valueOf(CaptureModule.this.mOpenCameraId));
                                        CaptureModule.this.addPreviewSurface(access$8700, null, CaptureModule.this.mOpenCameraId);
                                        access$8700.set(CaptureModule.night_enable, Boolean.valueOf(false));
                                        CaptureModule.this.mCaptureSession[CaptureModule.this.mOpenCameraId].capture(access$8700.build(), CaptureModule.this.mCaptureCallback, CaptureModule.this.mCameraHandler);
                                    } catch (Exception unused) {
                                    }
                                    CaptureModule.this.mIsToNightModeSuccess = false;
                                }
                                if (CaptureModule.this.mLongshotActive || CaptureModule.this.mSingleshotActive || CaptureModule.this.mFrameSendNums.get() != CaptureModule.this.mImageArrivedNums.get()) {
                                    CaptureModule.this.mImageArrivedNums.incrementAndGet();
                                    if (CaptureModule.this.isMpoOn()) {
                                        CaptureModule.this.mMpoSaveHandler.obtainMessage(1, this.mCamId, 0, acquireNextImage).sendToTarget();
                                    } else {
                                        CaptureModule.this.mCaptureStartTime = System.currentTimeMillis();
                                        CaptureModule.this.mNamedImages.nameNewImage(CaptureModule.this.mCaptureStartTime);
                                        NamedEntity nextNameEntity = CaptureModule.this.mNamedImages.getNextNameEntity();
                                        if (nextNameEntity != null) {
                                            str2 = nextNameEntity.title;
                                        }
                                        if (nextNameEntity == null) {
                                            j = -1;
                                        } else {
                                            j = nextNameEntity.date;
                                        }
                                        long j2 = j;
                                        try {
                                            byte[] access$9400 = CaptureModule.this.getJpegData(acquireNextImage);
                                            int width = acquireNextImage.getWidth();
                                            int height = acquireNextImage.getHeight();
                                            int format = acquireNextImage.getFormat();
                                            acquireNextImage.close();
                                            if (format == 37) {
                                                CaptureModule.this.mActivity.getMediaSaveService().addRawImage(access$9400, str2, "raw");
                                            } else {
                                                ExifInterface exif = Exif.getExif(access$9400);
                                                int orientation = Exif.getOrientation(exif);
                                                if (CaptureModule.this.mIntentMode != 0) {
                                                    CaptureModule.this.mJpegImageData = access$9400;
                                                    if (!CaptureModule.this.mQuickCapture) {
                                                        CaptureModule.this.showCapturedReview(access$9400, orientation);
                                                    } else {
                                                        CaptureModule.this.onCaptureDone();
                                                    }
                                                } else {
                                                    ArrayList generateXmpFromMpo = MpoInterface.generateXmpFromMpo(access$9400);
                                                    if (!CaptureModule.this.mBokehEnabled || generateXmpFromMpo == null || generateXmpFromMpo.size() <= 2) {
                                                        int i = orientation;
                                                        CaptureModule.this.mActivity.getMediaSaveService().addImage(access$9400, str2, j2, null, width, height, i, exif, CaptureModule.this.mOnMediaSavedListener, CaptureModule.this.mContentResolver, PhotoModule.PIXEL_FORMAT_JPEG);
                                                    } else {
                                                        GImage gImage = new GImage((byte[]) generateXmpFromMpo.get(1), "image/jpeg");
                                                        GDepth createGDepth = GDepth.createGDepth((byte[]) generateXmpFromMpo.get(generateXmpFromMpo.size() - 1));
                                                        try {
                                                            createGDepth.setRoi(new Rect(0, 0, width, height));
                                                            CaptureModule.this.mActivity.getMediaSaveService().addXmpImage((byte[]) generateXmpFromMpo.get(0), gImage, createGDepth, str2, j2, null, width, height, orientation, exif, CaptureModule.this.mOnMediaSavedListener, CaptureModule.this.mContentResolver, PhotoModule.PIXEL_FORMAT_JPEG);
                                                        } catch (IllegalStateException e) {
                                                            e.printStackTrace();
                                                            return;
                                                        }
                                                    }
                                                    if (CaptureModule.this.mLongshotActive) {
                                                        CaptureModule.this.mLastJpegData = access$9400;
                                                    } else {
                                                        CaptureModule.this.mActivity.updateThumbnail(access$9400);
                                                    }
                                                }
                                            }
                                        } catch (IllegalStateException e2) {
                                            e2.printStackTrace();
                                            return;
                                        }
                                    }
                                    return;
                                }
                                acquireNextImage.close();
                            }
                        };
                        this.mImageReader[i2].setOnImageAvailableListener(r5, this.mImageAvailableHandler);
                        if (this.mSaveRaw) {
                            this.mRawImageReader[i2] = ImageReader.newInstance(this.mSupportedRawPictureSize.getWidth(), this.mSupportedRawPictureSize.getHeight(), 37, 10);
                            this.mRawImageReader[i2].setOnImageAvailableListener(r5, this.mImageAvailableHandler);
                        }
                    }
                }
            }
            this.mMediaRecorder = new MediaRecorder();
            this.mAutoFocusRegionSupported = this.mSettingsManager.isAutoFocusRegionSupported(this.mCameraIdList);
            this.mAutoExposureRegionSupported = this.mSettingsManager.isAutoExposureRegionSupported(this.mCameraIdList);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public static byte[] getDataFromImage(Image image, int i) {
        Rect rect;
        int i2;
        int i3 = i;
        int i4 = 2;
        int i5 = 1;
        if (i3 != 1 && i3 != 2) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 and COLOR_FormatNV21");
        } else if (isImageFormatSupported(image)) {
            Rect cropRect = image.getCropRect();
            int format = image.getFormat();
            int width = cropRect.width();
            int height = cropRect.height();
            Plane[] planes = image.getPlanes();
            int i6 = width * height;
            byte[] bArr = new byte[((ImageFormat.getBitsPerPixel(format) * i6) / 8)];
            int i7 = 0;
            byte[] bArr2 = new byte[planes[0].getRowStride()];
            int i8 = 1;
            int i9 = 0;
            int i10 = 0;
            while (i9 < planes.length) {
                if (i9 != 0) {
                    if (i9 != i5) {
                        if (i9 == i4) {
                            if (i3 == i5) {
                                i10 = (int) (((double) i6) * 1.25d);
                                i8 = i5;
                            } else if (i3 == i4) {
                                i8 = i4;
                            }
                        }
                    } else if (i3 == i5) {
                        i8 = i5;
                    } else if (i3 == i4) {
                        i10 = i6 + 1;
                        i8 = i4;
                    }
                    i10 = i6;
                } else {
                    i8 = i5;
                    i10 = i7;
                }
                ByteBuffer buffer = planes[i9].getBuffer();
                int rowStride = planes[i9].getRowStride();
                int pixelStride = planes[i9].getPixelStride();
                int i11 = i9 == 0 ? i7 : i5;
                int i12 = width >> i11;
                int i13 = height >> i11;
                int i14 = width;
                buffer.position(((cropRect.top >> i11) * rowStride) + ((cropRect.left >> i11) * pixelStride));
                int i15 = 0;
                while (i15 < i13) {
                    if (pixelStride == 1 && i8 == 1) {
                        buffer.get(bArr, i10, i12);
                        i10 += i12;
                        rect = cropRect;
                        i2 = i12;
                    } else {
                        rect = cropRect;
                        i2 = ((i12 - 1) * pixelStride) + 1;
                        buffer.get(bArr2, 0, i2);
                        int i16 = i10;
                        for (int i17 = 0; i17 < i12; i17++) {
                            bArr[i16] = bArr2[i17 * pixelStride];
                            i16 += i8;
                        }
                        i10 = i16;
                    }
                    if (i15 < i13 - 1) {
                        buffer.position((buffer.position() + rowStride) - i2);
                    }
                    i15++;
                    cropRect = rect;
                }
                Rect rect2 = cropRect;
                i9++;
                i3 = i;
                width = i14;
                i4 = 2;
                i5 = 1;
                i7 = 0;
            }
            return bArr;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("can't convert Image to byte array, format ");
            sb.append(image.getFormat());
            throw new RuntimeException(sb.toString());
        }
    }

    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        return format == 17 || format == 35 || format == 842094169;
    }

    public static HeifWriter createHEIFEncoder(String str, int i, int i2, int i3, int i4, int i5) {
        try {
            HeifWriter.Builder builder = new HeifWriter.Builder(str, i, i2, 1);
            builder.setQuality(i5);
            builder.setMaxImages(i4);
            builder.setPrimaryIndex(0);
            builder.setRotation(i3);
            builder.setGridEnabled(true);
            return builder.build();
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private void createVideoSnapshotImageReader() {
        ImageReader imageReader = this.mVideoSnapshotImageReader;
        if (imageReader != null) {
            imageReader.close();
        }
        if (this.mSettingsManager.getSavePictureFormat() == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.mActivity.getCacheDir().getPath());
            sb.append("/liveshot_heif.tmp");
            this.mLiveShotInitHeifWriter = createHEIFEncoder(sb.toString(), this.mVideoSize.getWidth(), this.mVideoSize.getHeight(), 0, 1, 85);
            return;
        }
        this.mVideoSnapshotImageReader = ImageReader.newInstance(this.mVideoSnapshotSize.getWidth(), this.mVideoSnapshotSize.getHeight(), 256, 2);
        this.mVideoSnapshotImageReader.setOnImageAvailableListener(new OnImageAvailableListener() {
            public void onImageAvailable(ImageReader imageReader) {
                String str;
                long j;
                Image acquireNextImage = imageReader.acquireNextImage();
                CaptureModule.this.mCaptureStartTime = System.currentTimeMillis();
                CaptureModule.this.mNamedImages.nameNewImage(CaptureModule.this.mCaptureStartTime);
                NamedEntity nextNameEntity = CaptureModule.this.mNamedImages.getNextNameEntity();
                if (nextNameEntity == null) {
                    str = null;
                } else {
                    str = nextNameEntity.title;
                }
                String str2 = str;
                if (nextNameEntity == null) {
                    j = -1;
                } else {
                    j = nextNameEntity.date;
                }
                long j2 = j;
                ByteBuffer buffer = acquireNextImage.getPlanes()[0].getBuffer();
                byte[] bArr = new byte[buffer.remaining()];
                buffer.get(bArr);
                ExifInterface exif = Exif.getExif(bArr);
                byte[] bArr2 = bArr;
                CaptureModule.this.mActivity.getMediaSaveService().addImage(bArr2, str2, j2, null, acquireNextImage.getWidth(), acquireNextImage.getHeight(), Exif.getOrientation(exif), exif, CaptureModule.this.mOnMediaSavedListener, CaptureModule.this.mContentResolver, PhotoModule.PIXEL_FORMAT_JPEG);
                CaptureModule.this.mActivity.updateThumbnail(bArr);
                acquireNextImage.close();
                CaptureModule.this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(CaptureModule.TAG, "image available then enable shutter button ");
                        CaptureModule.this.mUI.enableShutter(true);
                    }
                });
            }
        }, this.mImageAvailableHandler);
    }

    public void unlockFocus(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("unlockFocus ");
        sb.append(i);
        String sb2 = sb.toString();
        String str = TAG;
        Log.d(str, sb2);
        if (checkSessionAndBuilder(this.mCaptureSession[i], this.mPreviewRequestBuilder[i])) {
            try {
                Builder requestBuilder = getRequestBuilder(i);
                if (this.mUI.getCurrentProMode() != 1) {
                    applySettingsForUnlockFocus(requestBuilder, i);
                } else {
                    applyFocusDistance(requestBuilder, String.valueOf(this.mSettingsManager.getFocusValue(SettingsManager.KEY_FOCUS_DISTANCE)));
                }
                requestBuilder.setTag(Integer.valueOf(i));
                addPreviewSurface(requestBuilder, null, i);
                applyFlash(requestBuilder, i);
                this.mCaptureSession[i].capture(requestBuilder.build(), this.mCaptureCallback, this.mCameraHandler);
                this.mState[i] = 0;
                if (i == getMainCameraId()) {
                    this.mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (CaptureModule.this.mUI.getCurrentProMode() != 1) {
                                CaptureModule.this.mUI.clearFocus();
                            }
                        }
                    });
                }
                this.mControlAFMode = 4;
                applyFlash(this.mPreviewRequestBuilder[i], i);
                applySettingsForUnlockExposure(this.mPreviewRequestBuilder[i], i);
                if (this.mSettingsManager.isDeveloperEnabled()) {
                    applyCommonSettings(this.mPreviewRequestBuilder[i], i);
                }
                setAFModeToPreview(i, this.mControlAFMode);
                this.mTakingPicture[i] = false;
                enableShutterAndVideoOnUiThread(i);
            } catch (CameraAccessException | IllegalStateException | NullPointerException e) {
                Log.w(str, "unlock exception occurred");
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    public void enableShutterAndVideoOnUiThread(int i) {
        if (i == getMainCameraId()) {
            this.mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    CaptureModule.this.mUI.stopSelfieFlash();
                    if (!CaptureModule.this.mIsSupportedQcfa && !CaptureModule.this.mBokehEnabled) {
                        CaptureModule.this.mUI.enableShutter(true);
                    }
                    CaptureModule.this.mUI.enableVideo(true);
                }
            });
        }
    }

    private Size parsePictureSize(String str) {
        int indexOf = str.indexOf(120);
        return new Size(Integer.parseInt(str.substring(0, indexOf)), Integer.parseInt(str.substring(indexOf + 1)));
    }

    private void closeProcessors() {
        PostProcessor postProcessor = this.mPostProcessor;
        if (postProcessor != null) {
            postProcessor.onClose();
        }
        FrameProcessor frameProcessor = this.mFrameProcessor;
        if (frameProcessor != null) {
            frameProcessor.onClose();
        }
    }

    public boolean isAllSessionClosed() {
        for (int i = 3; i >= 0; i--) {
            if (this.mCaptureSession[i] != null) {
                return false;
            }
        }
        return true;
    }

    private boolean isSWMFNREnabled() {
        SettingsManager settingsManager = this.mSettingsManager;
        if (settingsManager != null) {
            String value = settingsManager.getValue(SettingsManager.KEY_CAPTURE_SWMFNR_VALUE);
            if (value != null) {
                return value.equals("1");
            }
        }
        return false;
    }

    private void closeSessions() {
        for (int i = 3; i >= 0; i--) {
            CameraCaptureSession[] cameraCaptureSessionArr = this.mCaptureSession;
            if (cameraCaptureSessionArr[i] != null) {
                if (this.mCamerasOpened) {
                    try {
                        cameraCaptureSessionArr[i].capture(this.mPreviewRequestBuilder[i].build(), null, this.mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e2) {
                        e2.printStackTrace();
                    }
                }
                this.mCaptureSession[i].close();
                this.mCaptureSession[i] = null;
            }
            ImageReader[] imageReaderArr = this.mImageReader;
            if (imageReaderArr[i] != null) {
                imageReaderArr[i].close();
                this.mImageReader[i] = null;
            }
            if (this.mPreviewImageReader != null) {
                this.mPreviewImageReaderLock.lock();
                this.mPreviewImageReader.close();
                this.mPreviewImageReaderLock.unlock();
                this.mPreviewImageReader = null;
            }
        }
    }

    private void resetAudioMute() {
        if (isAudioMute()) {
            setMute(false, true);
        }
    }

    /* access modifiers changed from: private */
    public void closeCamera() {
        String str = TAG;
        Log.d(str, "closeCamera");
        closeProcessors();
        try {
            Log.d(str, "closeCamera before block");
            this.mWaitCameraOpened.block(2000);
            this.mWaitCameraOpened.close();
            Log.d(str, "closeCamera after block");
            for (int i = 3; i >= 0; i--) {
                if (this.mCameraDevice[i] != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Closing camera: ");
                    sb.append(this.mCameraDevice[i].getId());
                    Log.d(str, sb.toString());
                    if (this.mCaptureSession[i] != null) {
                        if (isAbortCapturesEnable()) {
                            this.mCaptureSession[i].stopRepeating();
                            this.mCaptureSession[i].abortCaptures();
                            Log.d(str, "Closing camera call abortCaptures ");
                        }
                        if (isSendRequestAfterFlushEnable()) {
                            Log.v(str, "Closing camera call setRepeatingRequest");
                            this.mCaptureSession[i].setRepeatingRequest(this.mPreviewRequestBuilder[i].build(), this.mCaptureCallback, this.mCameraHandler);
                        }
                    }
                    this.mCameraDevice[i].close();
                    this.mCameraDevice[i] = null;
                    this.mCameraOpened[i] = false;
                    this.mCaptureSession[i] = null;
                }
                if (this.mImageReader[i] != null) {
                    this.mImageReader[i].close();
                    this.mImageReader[i] = null;
                }
                if (this.mPreviewImageReader != null) {
                    this.mPreviewImageReaderLock.lock();
                    this.mPreviewImageReader.close();
                    this.mPreviewImageReaderLock.unlock();
                    this.mPreviewImageReader = null;
                }
            }
            this.mIsLinked = false;
            if (this.mMediaRecorder != null) {
                this.mMediaRecorder.release();
                this.mMediaRecorder = null;
            }
            if (this.mVideoSnapshotImageReader != null) {
                this.mVideoSnapshotImageReader.close();
                this.mVideoSnapshotImageReader = null;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void lockExposure(int i) {
        if (checkSessionAndBuilder(this.mCaptureSession[i], this.mPreviewRequestBuilder[i])) {
            StringBuilder sb = new StringBuilder();
            sb.append("lockExposure: ");
            sb.append(i);
            Log.d(TAG, sb.toString());
            try {
                applySettingsForLockExposure(this.mPreviewRequestBuilder[i], i);
                if (this.mUI.getCurrentProMode() == 1) {
                    applyFocusDistance(this.mPreviewRequestBuilder[i], String.valueOf(this.mSettingsManager.getFocusValue(SettingsManager.KEY_FOCUS_DISTANCE)));
                }
                this.mState[i] = 3;
                if (!this.mPostProcessor.isZSLEnabled() || getCameraMode() == 0) {
                    this.mCaptureSession[i].setRepeatingRequest(this.mPreviewRequestBuilder[i].build(), this.mCaptureCallback, this.mCameraHandler);
                } else {
                    setRepeatingBurstForZSL(i);
                }
            } catch (CameraAccessException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void applySettingsForLockFocus(Builder builder, int i) {
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(1));
        applyAFRegions(builder, i);
        applyAERegions(builder, i);
        applyCommonSettings(builder, i);
    }

    private void applySettingsForCapture(Builder builder, int i) {
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
        applyJpegQuality(builder);
        applyFlash(builder, i);
        applyCommonSettings(builder, i);
    }

    private void applySettingsForPrecapture(Builder builder, int i) {
        builder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Integer.valueOf(1));
        if (!this.mLongshotActive) {
            applyFlash(builder, i);
        }
        applyCommonSettings(builder, i);
    }

    private void applySettingsForLockExposure(Builder builder, int i) {
        builder.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.TRUE);
    }

    private void applySettingsForUnlockExposure(Builder builder, int i) {
        builder.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.FALSE);
    }

    private void applySettingsForUnlockFocus(Builder builder, int i) {
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(2));
        applyCommonSettings(builder, i);
    }

    private void applySettingsForAutoFocus(Builder builder, int i) {
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(1));
        applyAFRegions(builder, i);
        applyAERegions(builder, i);
        applyCommonSettings(builder, i);
    }

    private void applySettingsForJpegInformation(Builder builder, int i) {
        Location currentLocation = this.mLocationManager.getCurrentLocation();
        String str = TAG;
        if (currentLocation != null) {
            Location location = new Location(currentLocation);
            location.setTime(location.getTime() / 1000);
            builder.set(CaptureRequest.JPEG_GPS_LOCATION, location);
            StringBuilder sb = new StringBuilder();
            sb.append("gps: ");
            sb.append(location.toString());
            Log.d(str, sb.toString());
        } else {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("no location - getRecordLocation: ");
            sb2.append(getRecordLocation());
            Log.d(str, sb2.toString());
        }
        builder.set(CaptureRequest.JPEG_ORIENTATION, Integer.valueOf(CameraUtil.getJpegRotation(i, this.mOrientation)));
        builder.set(CaptureRequest.JPEG_THUMBNAIL_SIZE, this.mPictureThumbSize);
        builder.set(CaptureRequest.JPEG_THUMBNAIL_QUALITY, Byte.valueOf(80));
    }

    private void applyVideoSnapshot(Builder builder, int i) {
        builder.set(CaptureRequest.CONTROL_MODE, Integer.valueOf(1));
        applyColorEffect(builder);
        applyVideoFlash(builder);
    }

    private void applyCommonSettings(Builder builder, int i) {
        builder.set(CaptureRequest.CONTROL_MODE, Integer.valueOf(1));
        if (this.mUI.getCurrentProMode() != 1) {
            builder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(this.mControlAFMode));
            applyAfModes(builder);
        } else {
            applyFocusDistance(builder, String.valueOf(this.mSettingsManager.getFocusValue(SettingsManager.KEY_FOCUS_DISTANCE)));
        }
        applyFaceDetection(builder);
        applyWhiteBalance(builder);
        applyExposure(builder);
        applyIso(builder);
        applyColorEffect(builder);
        applySceneMode(builder);
        applyZoom(builder, i);
        applyInstantAEC(builder);
        applySaturationLevel(builder);
        applyAntiBandingLevel(builder);
        applyDenoise(builder);
        applyHistogram(builder);
        applySharpnessControlModes(builder);
        applyExposureMeteringModes(builder);
        enableBokeh(builder);
        enableMakeUp(builder);
        enableSat(builder, i);
        applyWbColorTemperature(builder);
    }

    private void enableSat(Builder builder, int i) {
        boolean isLogicalCamera = this.mSettingsManager.isLogicalCamera(i);
        if (!this.mUI.isBokehMode && isLogicalCamera) {
            try {
                builder.set(sat_enable, Boolean.valueOf(true));
            } catch (IllegalArgumentException unused) {
                Log.e(TAG, "can not find vendor tag : org.codeaurora.qcamera3.sat");
            }
        }
    }

    private void startBackgroundThread() {
        this.mCameraThread = new HandlerThread("CameraBackground");
        this.mCameraThread.start();
        this.mImageAvailableThread = new HandlerThread("CameraImageAvailable");
        this.mImageAvailableThread.start();
        this.mCaptureCallbackThread = new HandlerThread("CameraCaptureCallback");
        this.mCaptureCallbackThread.start();
        this.mMpoSaveThread = new HandlerThread("MpoSaveHandler");
        this.mMpoSaveThread.start();
        this.mCameraHandler = new MyCameraHandler(this.mCameraThread.getLooper());
        this.mImageAvailableHandler = new Handler(this.mImageAvailableThread.getLooper());
        this.mCaptureCallbackHandler = new Handler(this.mCaptureCallbackThread.getLooper());
        this.mMpoSaveHandler = new MpoSaveHandler(this.mMpoSaveThread.getLooper());
    }

    private void stopBackgroundThread() {
        this.mCameraThread.quitSafely();
        this.mImageAvailableThread.quitSafely();
        this.mCaptureCallbackThread.quitSafely();
        this.mMpoSaveThread.quitSafely();
        try {
            this.mCameraThread.join();
            this.mCameraThread = null;
            this.mCameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            this.mImageAvailableThread.join();
            this.mImageAvailableThread = null;
            this.mImageAvailableHandler = null;
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
        try {
            this.mCaptureCallbackThread.join();
            this.mCaptureCallbackThread = null;
            this.mCaptureCallbackHandler = null;
        } catch (InterruptedException e3) {
            e3.printStackTrace();
        }
        try {
            this.mMpoSaveThread.join();
            this.mMpoSaveThread = null;
            this.mMpoSaveHandler = null;
        } catch (InterruptedException e4) {
            e4.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void openCamera(int i) {
        Trace.beginSection("CaptureModule openCamera");
        if (!this.mPaused) {
            StringBuilder sb = new StringBuilder();
            sb.append("openCamera ");
            sb.append(i);
            String sb2 = sb.toString();
            String str = TAG;
            Log.d(str, sb2);
            this.mOpenCameraId = i;
            try {
                this.manager = (CameraManager) this.mActivity.getSystemService("camera");
                if (this.manager.getCameraIdList().length == 0) {
                    Message obtain = Message.obtain();
                    obtain.what = 0;
                    obtain.arg1 = i;
                    this.mCameraHandler.sendMessage(obtain);
                    return;
                }
                this.mCameraId[i] = this.manager.getCameraIdList()[i];
                Log.d(str, "openCamera before block");
                this.mWaitCameraClosed.block(5000);
                this.mWaitCameraClosed.close();
                Log.d(str, "openCamera after block");
                this.manager.openCamera(this.mCameraId[i], this.mStateCallback, this.mCameraHandler);
                Trace.endSection();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void onPreviewFocusChanged(boolean z) {
        this.mUI.onPreviewFocusChanged(z);
    }

    public void onPauseBeforeSuper() {
        cancelTouchFocus();
        this.mPaused = true;
        this.mToast = null;
        this.mUI.onPause();
        if (this.mIsRecordingVideo) {
            stopRecordingVideo(getMainCameraId());
        }
        Player player = this.mSoundPlayer;
        if (player != null) {
            player.release();
            this.mSoundPlayer = null;
        }
        SelfieThread selfieThread2 = this.selfieThread;
        if (selfieThread2 != null) {
            selfieThread2.interrupt();
        }
        resetScreenOn();
        this.mUI.stopSelfieFlash();
    }

    public void onPauseAfterSuper() {
        Log.d(TAG, "onPause");
        LocationManager locationManager = this.mLocationManager;
        if (locationManager != null) {
            locationManager.recordLocation(false);
        }
        if (isClearSightOn()) {
            ClearSightImageProcessor.getInstance().close();
        }
        HeifWriter heifWriter = this.mInitHeifWriter;
        if (heifWriter != null) {
            heifWriter.close();
        }
        closeCamera();
        this.mUI.hideSurfaceView();
        resetAudioMute();
        this.mUI.releaseSoundPool();
        this.mUI.showPreviewCover();
        this.mFirstPreviewLoaded = false;
        this.mLastJpegData = null;
        setProModeVisible();
        childModesUIUpdate();
        setBokehModeVisible();
        if (!(this.mIntentMode == 0 || this.mJpegImageData == null)) {
            this.mActivity.setResultEx(0, new Intent());
            this.mActivity.finish();
        }
        this.mJpegImageData = null;
        closeVideoFileDescriptor();
    }

    public void onResumeBeforeSuper() {
        this.mPaused = false;
        for (int i = 0; i < 4; i++) {
            this.mCameraOpened[i] = false;
            this.mTakingPicture[i] = false;
        }
        for (int i2 = 0; i2 < 4; i2++) {
            this.mState[i2] = 0;
        }
        this.mUI.enableGestures(true);
        this.mLongshotActive = false;
        this.mSingleshotActive = false;
        updateZoom();
        updatePreviewSurfaceReadyState(false);
        String str = "pref_camera_savepath_key";
        if (this.mSettingsManager.getValue(str).equals("1") && !SDCard.instance().isWriteable()) {
            Log.d(TAG, "change savepath if have no sdcard");
            SettingsManager settingsManager = this.mSettingsManager;
            if (settingsManager != null) {
                settingsManager.setValue(str, "0");
            }
            Storage.setSaveSDCard(false);
        }
    }

    private void cancelTouchFocus() {
        if (getCameraMode() == 0) {
            int[] iArr = this.mState;
            if (iArr[0] == 5) {
                cancelTouchFocus(0);
                return;
            }
            int i = MONO_ID;
            if (iArr[i] == 5) {
                cancelTouchFocus(i);
            }
        } else if (this.mState[getMainCameraId()] == 5) {
            cancelTouchFocus(getMainCameraId());
        }
    }

    private ArrayList<Integer> getFrameProcFilterId() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_MAKEUP);
        if (value != null && !value.equalsIgnoreCase("0")) {
            arrayList.add(Integer.valueOf(1));
        }
        if (isTrackingFocusSettingOn()) {
            arrayList.add(Integer.valueOf(2));
        }
        return arrayList;
    }

    public boolean isTrackingFocusSettingOn() {
        try {
            if (Integer.parseInt(this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE)) == 108) {
                return true;
            }
        } catch (Exception unused) {
        }
        return false;
    }

    public void setRefocusLastTaken(final boolean z) {
        this.mIsRefocus = z;
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                CaptureModule.this.mUI.showRefocusToast(z);
            }
        });
    }

    private int getPostProcFilterId(int i) {
        if (i == 101) {
            return 1;
        }
        if (i == 5 && StillmoreFilter.isSupportedStatic()) {
            return 4;
        }
        if (i == 105 && ChromaflashFilter.isSupportedStatic()) {
            return 6;
        }
        if (i == 106 && BlurbusterFilter.isSupportedStatic()) {
            return 7;
        }
        if (i == 102 && UbifocusFilter.isSupportedStatic()) {
            return 3;
        }
        if (i == 107 && SharpshooterFilter.isSupportedStatic()) {
            return 2;
        }
        if (i == 103) {
            return 5;
        }
        return 0;
    }

    private void initializeValues() {
        updatePictureSize();
        updateVideoSize();
        updateVideoSnapshotSize();
        updateTimeLapseSetting();
        estimateJpegFileSize();
        updateMaxVideoDuration();
    }

    private void updatePreviewSize() {
        int width = this.mPreviewSize.getWidth();
        int height = this.mPreviewSize.getHeight();
        Point cameraPreviewSize = PersistUtil.getCameraPreviewSize();
        if (cameraPreviewSize != null) {
            width = cameraPreviewSize.x;
            height = cameraPreviewSize.y;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("updatePreviewSize final Picture preview size = ");
        sb.append(width);
        sb.append(", ");
        sb.append(height);
        Log.d(TAG, sb.toString());
        this.mPreviewSize = new Size(width, height);
        this.mUI.setPreviewSize(this.mPreviewSize.getWidth(), this.mPreviewSize.getHeight());
    }

    private void openProcessors() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
        this.mIsSupportedQcfa = this.mSettingsManager.getQcfaPrefEnabled() && this.mSettingsManager.getIsSupportedQcfa(getMainCameraId()) && this.mPictureSize.toString().equals(this.mSettingsManager.getSupportedQcfaDimension(getMainCameraId()));
        if (this.mPostProcessor != null) {
            String value2 = this.mSettingsManager.getValue(SettingsManager.KEY_SELFIEMIRROR);
            String str = RecordLocationPreference.VALUE_ON;
            boolean z = value2 != null && value2.equalsIgnoreCase(str);
            String value3 = this.mSettingsManager.getValue(SettingsManager.KEY_MAKEUP);
            boolean z2 = value3 != null && !value3.equals("0");
            String value4 = this.mSettingsManager.getValue(SettingsManager.KEY_FLASH_MODE);
            boolean z3 = value4 != null && value4.equalsIgnoreCase(str);
            this.mSaveRaw = isRawCaptureOn();
            if (value != null) {
                int parseInt = Integer.parseInt(value);
                StringBuilder sb = new StringBuilder();
                sb.append("Chosen postproc filter id : ");
                sb.append(getPostProcFilterId(parseInt));
                Log.d(TAG, sb.toString());
                this.mPostProcessor.onOpen(getPostProcFilterId(parseInt), z3, isTrackingFocusSettingOn(), z2, z, this.mSaveRaw, this.mIsSupportedQcfa);
            } else {
                this.mPostProcessor.onOpen(0, z3, isTrackingFocusSettingOn(), z2, z, this.mSaveRaw, this.mIsSupportedQcfa);
            }
        }
        FrameProcessor frameProcessor = this.mFrameProcessor;
        if (frameProcessor != null) {
            frameProcessor.onOpen(getFrameProcFilterId(), this.mPreviewSize);
        }
        if (this.mPostProcessor.isZSLEnabled() && !isActionImageCapture()) {
            this.mChosenImageFormat = 34;
        } else if (this.mPostProcessor.isFilterOn() || getFrameFilters().size() != 0 || this.mPostProcessor.isSelfieMirrorOn()) {
            CaptureUI captureUI = this.mUI;
            if (captureUI.isBokehMode || captureUI.isMakeUp) {
                this.mChosenImageFormat = 256;
            } else {
                this.mChosenImageFormat = 35;
            }
        } else {
            this.mChosenImageFormat = 256;
        }
        setUpCameraOutputs(this.mChosenImageFormat);
    }

    public boolean isSelfieMirrorOn() {
        return this.mPostProcessor.isSelfieMirrorOn();
    }

    private void loadSoundPoolResource() {
        if (Integer.parseInt(this.mSettingsManager.getValue("pref_camera_timer_key")) > 0) {
            this.mUI.initCountDownView();
        }
    }

    public void onResumeAfterSuper() {
        Trace.beginSection("CaptureModule onResumeAfterSuper");
        StringBuilder sb = new StringBuilder();
        sb.append("onResume ");
        sb.append(getCameraMode());
        Log.d(TAG, sb.toString());
        reinit();
        initializeValues();
        updatePreviewSize();
        this.mCameraIdList = new ArrayList();
        if (this.mSoundPlayer == null) {
            this.mSoundPlayer = SoundClips.getPlayer(this.mActivity);
        }
        updateSaveStorageState();
        setDisplayOrientation();
        openProcessors();
        loadSoundPoolResource();
        Message obtain = Message.obtain();
        obtain.what = 0;
        if (isBackCamera()) {
            int cameraMode = getCameraMode();
            if (cameraMode == 0 || cameraMode == 1) {
                obtain.arg1 = 0;
                this.mCameraHandler.sendMessage(obtain);
            } else if (cameraMode == 2) {
                obtain.arg1 = MONO_ID;
                this.mCameraHandler.sendMessage(obtain);
            } else if (cameraMode == 3) {
                obtain.arg1 = SWITCH_ID;
                this.mCameraHandler.sendMessage(obtain);
            } else if (cameraMode == 4) {
                obtain.arg1 = BOKEH_ID;
                this.mCameraHandler.sendMessage(obtain);
            }
        } else {
            int i = SWITCH_ID;
            if (i == -1) {
                i = FRONT_ID;
            }
            obtain.arg1 = i;
            this.mCameraHandler.sendMessage(obtain);
        }
        this.mUI.showSurfaceView();
        if (!this.mFirstTimeInitialized) {
            initializeFirstTime();
        } else {
            initializeSecondTime();
        }
        this.mUI.reInitUI();
        this.mUI.enableShutter(true);
        this.mUI.enableVideo(true);
        setProModeVisible();
        childModesUIUpdate();
        setBokehModeVisible();
        SettingsManager settingsManager = this.mSettingsManager;
        String str = SettingsManager.KEY_SCENE_MODE;
        String value = settingsManager.getValue(str);
        if (Integer.parseInt(value) != 102) {
            setRefocusLastTaken(false);
        }
        if (isPanoSetting(value)) {
            if (this.mIntentMode != 0) {
                this.mSettingsManager.setValue(str, "0");
                showToast("Pano Capture is not supported in this mode");
            } else {
                this.mActivity.onModuleSelected(6);
            }
        }
        Trace.endSection();
    }

    public void onConfigurationChanged(Configuration configuration) {
        Log.v(TAG, "onConfigurationChanged");
        setDisplayOrientation();
    }

    public void onDestroy() {
        FrameProcessor frameProcessor = this.mFrameProcessor;
        if (frameProcessor != null) {
            frameProcessor.onDestory();
        }
        this.mSettingsManager.unregisterListener(this);
        this.mSettingsManager.unregisterListener(this.mUI);
        if (this.mOpenCameraId == BOKEH_ID) {
            this.mSettingsManager.setBokehMode(false);
        }
        stopBackgroundThread();
    }

    public boolean onBackPressed() {
        return this.mUI.onBackPressed();
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0031  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x003b A[RETURN] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onKeyDown(int r4, android.view.KeyEvent r5) {
        /*
            r3 = this;
            r0 = 27
            r1 = 1
            if (r4 == r0) goto L_0x004a
            r0 = 130(0x82, float:1.82E-43)
            if (r4 == r0) goto L_0x003c
            r0 = 79
            r2 = 0
            if (r4 == r0) goto L_0x0024
            r0 = 80
            if (r4 == r0) goto L_0x002d
            switch(r4) {
                case 23: goto L_0x0016;
                case 24: goto L_0x0024;
                case 25: goto L_0x0024;
                default: goto L_0x0015;
            }
        L_0x0015:
            return r2
        L_0x0016:
            boolean r4 = r3.mFirstTimeInitialized
            if (r4 == 0) goto L_0x0023
            int r4 = r5.getRepeatCount()
            if (r4 != 0) goto L_0x0023
            r3.onShutterButtonClick()
        L_0x0023:
            return r1
        L_0x0024:
            com.android.camera.CameraActivity r4 = r3.mActivity
            boolean r4 = com.android.camera.util.CameraUtil.volumeKeyShutterDisable(r4)
            if (r4 == 0) goto L_0x002d
            return r2
        L_0x002d:
            boolean r4 = r3.mFirstTimeInitialized
            if (r4 == 0) goto L_0x003b
            int r4 = r5.getRepeatCount()
            if (r4 != 0) goto L_0x003a
            r3.onShutterButtonFocus(r1)
        L_0x003a:
            return r1
        L_0x003b:
            return r2
        L_0x003c:
            boolean r4 = r3.mFirstTimeInitialized
            if (r4 == 0) goto L_0x0049
            int r4 = r5.getRepeatCount()
            if (r4 != 0) goto L_0x0049
            r3.onVideoButtonClick()
        L_0x0049:
            return r1
        L_0x004a:
            boolean r4 = r3.mFirstTimeInitialized
            if (r4 == 0) goto L_0x0057
            int r4 = r5.getRepeatCount()
            if (r4 != 0) goto L_0x0057
            r3.onShutterButtonClick()
        L_0x0057:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CaptureModule.onKeyDown(int, android.view.KeyEvent):boolean");
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (i == 24 || i == 25 || i == 79) {
            if (!this.mFirstTimeInitialized || CameraUtil.volumeKeyShutterDisable(this.mActivity)) {
                return false;
            }
            if (this.mIsRecordingVideo) {
                CameraCaptureSession cameraCaptureSession = this.mCurrentSession;
                if (cameraCaptureSession != null) {
                    stopRecordingVideo(Integer.parseInt(cameraCaptureSession.getDevice().getId()));
                    return true;
                }
            }
            if (this.mUI.isShutterEnabled()) {
                onShutterButtonClick();
            }
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

    public void onZoomChanged(float f) {
        this.mZoomValue = f;
        if (isBackCamera()) {
            int cameraMode = getCameraMode();
            if (cameraMode == 0) {
                applyZoomAndUpdate(0);
                applyZoomAndUpdate(MONO_ID);
            } else if (cameraMode == 1) {
                applyZoomAndUpdate(0);
            } else if (cameraMode == 2) {
                applyZoomAndUpdate(MONO_ID);
            } else if (cameraMode == 3) {
                applyZoomAndUpdate(SWITCH_ID);
            } else if (cameraMode == 4) {
                applyZoomAndUpdate(BOKEH_ID);
            }
        } else {
            int i = SWITCH_ID;
            if (i == -1) {
                i = FRONT_ID;
            }
            applyZoomAndUpdate(i);
        }
        this.mUI.updateFaceViewCameraBound(this.mCropRegion[getMainCameraId()]);
    }

    private boolean isInMode(int i) {
        boolean z = false;
        if (isBackCamera()) {
            int cameraMode = getCameraMode();
            if (cameraMode == 0) {
                if (i == 0 || i == MONO_ID) {
                    z = true;
                }
                return z;
            } else if (cameraMode == 1) {
                if (i == 0) {
                    z = true;
                }
                return z;
            } else if (cameraMode == 2) {
                if (i == MONO_ID) {
                    z = true;
                }
                return z;
            } else if (cameraMode == 3) {
                if (i == SWITCH_ID) {
                    z = true;
                }
                return z;
            } else if (cameraMode != 4) {
                return false;
            } else {
                if (i == BOKEH_ID) {
                    z = true;
                }
                return z;
            }
        } else {
            int i2 = SWITCH_ID;
            if (i2 != -1) {
                if (i == i2) {
                    z = true;
                }
                return z;
            }
            if (i == FRONT_ID) {
                z = true;
            }
            return z;
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CaptureModule.onCaptureDone():void");
    }

    public void onRecordingDone(boolean z) {
        int i;
        Intent intent = new Intent();
        if (z) {
            i = -1;
            intent.setData(this.mCurrentVideoUri);
            intent.setFlags(1);
        } else {
            i = 0;
        }
        this.mActivity.setResultEx(i, intent);
        this.mActivity.finish();
    }

    public void onSingleTapUp(View view, int i, int i2) {
        if (!this.mPaused && this.mCamerasOpened && this.mFirstTimeInitialized && this.mAutoFocusRegionSupported && this.mAutoExposureRegionSupported && isTouchToFocusAllowed()) {
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("onSingleTapUp ");
                sb.append(i);
                sb.append(" ");
                sb.append(i2);
                Log.d(TAG, sb.toString());
            }
            int[] iArr = {i, i2};
            if (!this.mUI.isOverControlRegion(iArr) && this.mUI.isOverSurfaceView(iArr)) {
                this.mUI.setFocusPosition(i, i2);
                int i3 = iArr[0];
                int i4 = iArr[1];
                this.mUI.onFocusStarted();
                if (isBackCamera()) {
                    int cameraMode = getCameraMode();
                    if (cameraMode == 0) {
                        float f = (float) i3;
                        float f2 = (float) i4;
                        triggerFocusAtPoint(f, f2, 0);
                        triggerFocusAtPoint(f, f2, MONO_ID);
                    } else if (cameraMode == 1) {
                        triggerFocusAtPoint((float) i3, (float) i4, 0);
                    } else if (cameraMode == 2) {
                        triggerFocusAtPoint((float) i3, (float) i4, MONO_ID);
                    } else if (cameraMode == 3) {
                        triggerFocusAtPoint((float) i3, (float) i4, SWITCH_ID);
                    } else if (cameraMode == 4) {
                        triggerFocusAtPoint((float) i3, (float) i4, BOKEH_ID);
                    }
                } else {
                    int i5 = SWITCH_ID;
                    if (i5 == -1) {
                        i5 = FRONT_ID;
                    }
                    triggerFocusAtPoint((float) i3, (float) i4, i5);
                }
            }
        }
    }

    public int getMainCameraId() {
        if (isBackCamera()) {
            int cameraMode = getCameraMode();
            if (cameraMode == 0 || cameraMode == 1) {
                return 0;
            }
            if (cameraMode == 2) {
                return MONO_ID;
            }
            if (cameraMode == 3) {
                return SWITCH_ID;
            }
            if (cameraMode != 4) {
                return 0;
            }
            return BOKEH_ID;
        }
        int i = SWITCH_ID;
        if (i == -1) {
            i = FRONT_ID;
        }
        return i;
    }

    public boolean isTakingPicture() {
        int i = 0;
        while (true) {
            boolean[] zArr = this.mTakingPicture;
            if (i >= zArr.length) {
                return false;
            }
            if (zArr[i]) {
                return true;
            }
            i++;
        }
    }

    private boolean isTouchToFocusAllowed() {
        return !isTakingPicture() && !this.mIsRecordingVideo && !isTouchAfEnabledSceneMode();
    }

    private boolean isTouchAfEnabledSceneMode() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
        if (value == null) {
            return false;
        }
        int parseInt = Integer.parseInt(value);
        if (parseInt == 18 || parseInt == 13 || parseInt == 12 || parseInt == 0 || parseInt >= 100) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public ExtendedFace[] getBsgcInfo(CaptureResult captureResult, int i) {
        ExtendedFace[] extendedFaceArr = new ExtendedFace[i];
        byte[] bArr = (byte[]) captureResult.get(blinkDetected);
        byte[] bArr2 = (byte[]) captureResult.get(blinkDegree);
        int[] iArr = (int[]) captureResult.get(gazeDirection);
        byte[] bArr3 = (byte[]) captureResult.get(gazeAngle);
        byte[] bArr4 = (byte[]) captureResult.get(smileDegree);
        byte[] bArr5 = (byte[]) captureResult.get(smileConfidence);
        for (int i2 = 0; i2 < i; i2++) {
            ExtendedFace extendedFace = new ExtendedFace(i2);
            extendedFace.setBlinkDetected(bArr[i2]);
            int i3 = i2 * 2;
            extendedFace.setBlinkDegree(bArr2[i3], bArr2[i3 + 1]);
            int i4 = i2 * 3;
            extendedFace.setGazeDirection(iArr[i4], iArr[i4 + 1], iArr[i4 + 2]);
            extendedFace.setGazeAngle(bArr3[i2]);
            extendedFace.setSmileDegree(bArr4[i2]);
            extendedFace.setSmileConfidence(bArr5[i2]);
            extendedFaceArr[i2] = extendedFace;
        }
        return extendedFaceArr;
    }

    /* access modifiers changed from: private */
    public void updateFaceView(final Face[] faceArr, final ExtendedFace[] extendedFaceArr) {
        this.mPreviewFaces = faceArr;
        this.mExFaces = extendedFaceArr;
        if (faceArr != null) {
            if (faceArr.length != 0) {
                this.mStickyFaces = faceArr;
                this.mStickyExFaces = extendedFaceArr;
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    CaptureModule.this.mUI.onFaceDetection(faceArr, extendedFaceArr);
                }
            });
        }
    }

    public boolean isSelfieFlash() {
        String value = this.mSettingsManager.getValue("pref_selfie_flash_key");
        return value != null && value.equals(RecordLocationPreference.VALUE_ON) && getMainCameraId() == FRONT_ID;
    }

    private void checkSelfieFlashAndTakePicture() {
        if (isSelfieFlash()) {
            this.mUI.startSelfieFlash();
            if (this.selfieThread == null) {
                this.selfieThread = new SelfieThread();
                this.selfieThread.start();
                return;
            }
            return;
        }
        takePicture();
    }

    public void onCountDownFinished() {
        checkSelfieFlashAndTakePicture();
        this.mUI.showUIAfterCountDown();
    }

    public void updateCameraOrientation() {
        if (this.mDisplayRotation != CameraUtil.getDisplayRotation(this.mActivity)) {
            setDisplayOrientation();
        }
    }

    public void waitingLocationPermissionResult(boolean z) {
        this.mLocationManager.waitingLocationPermissionResult(z);
    }

    public void enableRecordingLocation(boolean z) {
        this.mSettingsManager.setValue("pref_camera_recordlocation_key", z ? RecordLocationPreference.VALUE_ON : "off");
        this.mLocationManager.recordLocation(z);
    }

    public void setPreferenceForTest(String str, String str2) {
        this.mSettingsManager.setValue(str, str2);
    }

    public void onPreviewUIReady() {
        updatePreviewSurfaceReadyState(true);
        if (this.mPaused || this.mIsRecordingVideo) {
        }
    }

    public void onPreviewUIDestroyed() {
        updatePreviewSurfaceReadyState(false);
    }

    public void onOrientationChanged(int i) {
        if (i != -1) {
            int i2 = this.mOrientation;
            this.mOrientation = CameraUtil.roundOrientation(i, i2);
            if (i2 != this.mOrientation) {
                this.mUI.onOrientationChanged();
                this.mUI.setOrientation(this.mOrientation, true);
                Camera2GraphView camera2GraphView = this.mGraphViewR;
                if (camera2GraphView != null) {
                    camera2GraphView.setRotation((float) (-this.mOrientation));
                }
                Camera2GraphView camera2GraphView2 = this.mGraphViewGR;
                if (camera2GraphView2 != null) {
                    camera2GraphView2.setRotation((float) (-this.mOrientation));
                }
                Camera2GraphView camera2GraphView3 = this.mGraphViewGB;
                if (camera2GraphView3 != null) {
                    camera2GraphView3.setRotation((float) (-this.mOrientation));
                }
                Camera2GraphView camera2GraphView4 = this.mGraphViewB;
                if (camera2GraphView4 != null) {
                    camera2GraphView4.setRotation((float) (-this.mOrientation));
                }
            }
            this.mGraphViewR = (Camera2GraphView) this.mRootView.findViewById(C0905R.C0907id.graph_view_r);
            this.mGraphViewGR = (Camera2GraphView) this.mRootView.findViewById(C0905R.C0907id.graph_view_gr);
            this.mGraphViewGB = (Camera2GraphView) this.mRootView.findViewById(C0905R.C0907id.graph_view_gb);
            this.mGraphViewB = (Camera2GraphView) this.mRootView.findViewById(C0905R.C0907id.graph_view_b);
            this.mGraphViewR.setDataSection(0, 256);
            this.mGraphViewGR.setDataSection(256, 512);
            this.mGraphViewGB.setDataSection(512, 768);
            this.mGraphViewB.setDataSection(768, 1024);
            Camera2GraphView camera2GraphView5 = this.mGraphViewR;
            if (camera2GraphView5 != null) {
                camera2GraphView5.setAlpha(0.75f);
                this.mGraphViewR.setCaptureModuleObject(this);
                this.mGraphViewR.PreviewChanged();
            }
            Camera2GraphView camera2GraphView6 = this.mGraphViewGR;
            if (camera2GraphView6 != null) {
                camera2GraphView6.setAlpha(0.75f);
                this.mGraphViewGR.setCaptureModuleObject(this);
                this.mGraphViewGR.PreviewChanged();
            }
            Camera2GraphView camera2GraphView7 = this.mGraphViewGB;
            if (camera2GraphView7 != null) {
                camera2GraphView7.setAlpha(0.75f);
                this.mGraphViewGB.setCaptureModuleObject(this);
                this.mGraphViewGB.PreviewChanged();
            }
            Camera2GraphView camera2GraphView8 = this.mGraphViewB;
            if (camera2GraphView8 != null) {
                camera2GraphView8.setAlpha(0.75f);
                this.mGraphViewB.setCaptureModuleObject(this);
                this.mGraphViewB.PreviewChanged();
            }
        }
    }

    public int getDisplayOrientation() {
        return this.mOrientation;
    }

    public void onMediaSaveServiceConnected(MediaSaveService mediaSaveService) {
        if (this.mFirstTimeInitialized) {
            mediaSaveService.setListener(this);
            if (isClearSightOn()) {
                ClearSightImageProcessor.getInstance().setMediaSaveService(mediaSaveService);
            }
        }
    }

    public void onSwitchSavePath() {
        this.mSettingsManager.setValue("pref_camera_savepath_key", "1");
        RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.on_switch_save_path_to_sdcard, 0).show();
    }

    public void onStorageNotEnoughRecordingVideo() {
        Log.d(TAG, "onStorageNotEnoughRecordingVideo");
        if (isRecordingVideo()) {
            stopRecordingVideo(getMainCameraId());
        }
        RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.video_reach_size_limit, 1).show();
    }

    public void onShutterButtonFocus(boolean z) {
        if (!z && this.mLongshotActive) {
            Log.d(TAG, "Longshot button up");
            this.mUI.enableGestures(true);
            this.mLongshotActive = false;
            this.mPostProcessor.stopLongShot();
            this.mUI.enableVideo(!this.mLongshotActive);
            this.mUI.updateFlashUi(!this.mLongshotActive);
        }
    }

    private void updatePictureSize() {
        this.mPictureSize = parsePictureSize(this.mSettingsManager.getValue("pref_camera_picturesize_key"));
        Size[] supportedOutputSize = this.mSettingsManager.getSupportedOutputSize(getMainCameraId(), SurfaceHolder.class);
        this.mSupportedMaxPictureSize = supportedOutputSize[0];
        this.mSupportedRawPictureSize = this.mSettingsManager.getSupportedOutputSize(getMainCameraId(), 37)[0];
        this.mPreviewSize = getOptimalPreviewSize(this.mPictureSize, supportedOutputSize);
        this.mPictureThumbSize = getOptimalPreviewSize(this.mPictureSize, this.mSettingsManager.getSupportedThumbnailSizes(getMainCameraId()));
    }

    public Size getThumbSize() {
        return this.mPictureThumbSize;
    }

    public boolean isRecordingVideo() {
        return this.mIsRecordingVideo;
    }

    public void setMute(boolean z, boolean z2) {
        ((AudioManager) this.mActivity.getSystemService("audio")).setMicrophoneMute(z);
        if (z2) {
            this.mIsMute = z;
        }
    }

    public boolean isAudioMute() {
        return this.mIsMute;
    }

    private void updateVideoSize() {
        String value = this.mSettingsManager.getValue("pref_video_quality_key");
        if (value != null) {
            this.mVideoSize = parsePictureSize(value);
            this.mVideoPreviewSize = getOptimalPreviewSize(this.mVideoSize, this.mSettingsManager.getSupportedOutputSize(getMainCameraId(), MediaRecorder.class));
            Point cameraPreviewSize = PersistUtil.getCameraPreviewSize();
            if (cameraPreviewSize != null) {
                this.mVideoPreviewSize = new Size(cameraPreviewSize.x, cameraPreviewSize.y);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("updateVideoSize Final Video preview size = ");
            sb.append(this.mVideoPreviewSize.getWidth());
            sb.append(", ");
            sb.append(this.mVideoPreviewSize.getHeight());
            Log.d(TAG, sb.toString());
        }
    }

    private void updateVideoSnapshotSize() {
        Size size = this.mVideoSize;
        this.mVideoSnapshotSize = size;
        if (!is4kSize(size) && this.mHighSpeedCaptureRate == 0) {
            this.mVideoSnapshotSize = getMaxPictureSizeLiveshot();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("mVideoSnapshotSize: ");
        sb.append(this.mVideoSnapshotSize.getWidth());
        sb.append(", ");
        sb.append(this.mVideoSnapshotSize.getHeight());
        Log.d(TAG, sb.toString());
        this.mVideoSnapshotThumbSize = getOptimalPreviewSize(this.mVideoSnapshotSize, this.mSettingsManager.getSupportedThumbnailSizes(getMainCameraId()));
    }

    private boolean is4kSize(Size size) {
        return size.getHeight() >= 2160 || size.getWidth() >= 3840;
    }

    private void updateMaxVideoDuration() {
        int parseInt = Integer.parseInt(this.mSettingsManager.getValue("pref_camera_video_duration_key"));
        if (parseInt == -1) {
            this.mMaxVideoDurationInMs = 30000;
        } else {
            this.mMaxVideoDurationInMs = parseInt * 60000;
        }
    }

    private void updateZoom() {
        int parseInt = Integer.parseInt(this.mSettingsManager.getValue(SettingsManager.KEY_ZOOM));
        if (parseInt != 0) {
            this.mZoomValue = (float) parseInt;
        } else {
            this.mZoomValue = 1.0f;
        }
    }

    private boolean startRecordingVideo(final int i) {
        if (this.mCameraDevice[i] == null || !getCameraModeSwitcherAllowed()) {
            return false;
        }
        this.mStartRecordingTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append("StartRecordingVideo ");
        sb.append(i);
        String sb2 = sb.toString();
        String str = TAG;
        Log.d(str, sb2);
        setCameraModeSwitcherAllowed(false);
        this.mStartRecPending = true;
        this.mIsRecordingVideo = true;
        this.mMediaRecorderPausing = false;
        checkAndPlayRecordSound(i, true);
        this.mActivity.updateStorageSpaceAndHint();
        if (this.mActivity.getStorageSpaceBytes() <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
            Log.w(str, "Storage issue, ignore the start request");
            this.mStartRecPending = false;
            this.mIsRecordingVideo = false;
            return false;
        }
        updateHFRSetting();
        updateVideoEncoder();
        if (!isSessionSupportedByEncoder(this.mVideoSize.getWidth(), this.mVideoSize.getHeight(), this.mHighSpeedCaptureRate)) {
            this.mStartRecPending = false;
            this.mIsRecordingVideo = false;
            RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.error_app_unsupported_hfr, 0).show();
            return false;
        }
        this.mCheckStorageHandler.sendEmptyMessageDelayed(0, 5000);
        try {
            setUpMediaRecorder(i);
            this.mUI.clearFocus();
            this.mUI.hideUIwhileRecording();
            this.mCameraHandler.removeMessages(1, this.mCameraId[i]);
            if (isAbortCapturesEnable() && this.mCaptureSession[i] != null) {
                this.mCaptureSession[i].stopRepeating();
                this.mCaptureSession[i].abortCaptures();
                Log.d(str, "startRecordingVideo call abortCaptures befor close preview ");
            }
            this.mState[i] = 0;
            this.mControlAFMode = 4;
            closePreviewSession();
            this.mFrameProcessor.onClose();
            if (this.mPostProcessor != null) {
                this.mPostProcessor.enableZSLQueue(false);
            }
            Size size = this.mVideoPreviewSize;
            if (this.mHighSpeedCapture) {
                size = this.mVideoSize;
            }
            if (this.mUI.setPreviewSize(size.getWidth(), size.getHeight())) {
                this.mUI.hideSurfaceView();
                this.mUI.showSurfaceView();
            }
            if (this.mHiston) {
                updateGraghViewVisibility(8);
            }
            this.mUI.resetTrackingFocus();
            createVideoSnapshotImageReader();
            this.mVideoRequestBuilder = this.mCameraDevice[i].createCaptureRequest(3);
            this.mVideoRequestBuilder.setTag(Integer.valueOf(i));
            this.mPreviewRequestBuilder[i] = this.mVideoRequestBuilder;
            ArrayList<Surface> arrayList = new ArrayList<>();
            Surface previewSurfaceForSession = getPreviewSurfaceForSession(i);
            this.mFrameProcessor.onOpen(getFrameProcFilterId(), this.mVideoSize);
            setUpVideoPreviewRequestBuilder(previewSurfaceForSession, i);
            if (this.mFrameProcessor.isFrameFilterEnabled()) {
                this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        CaptureModule.this.mUI.getSurfaceHolder().setFixedSize(CaptureModule.this.mVideoSize.getHeight(), CaptureModule.this.mVideoSize.getWidth());
                    }
                });
            }
            this.mFrameProcessor.setOutputSurface(previewSurfaceForSession);
            this.mFrameProcessor.setVideoOutputSurface(this.mMediaRecorder.getSurface());
            addPreviewSurface(this.mVideoRequestBuilder, arrayList, i);
            if (this.mHighSpeedCapture) {
                this.mVideoRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, this.mHighSpeedFPSRange);
            }
            if (!this.mHighSpeedCapture || ((Integer) this.mHighSpeedFPSRange.getUpper()).intValue() <= 30) {
                C058124 r3 = new CameraCaptureSession.StateCallback() {
                    public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                        Log.d(CaptureModule.TAG, "StartRecordingVideo session onConfigured");
                        CaptureModule.this.setCameraModeSwitcherAllowed(true);
                        CaptureModule.this.mCurrentSession = cameraCaptureSession;
                        CaptureModule.this.mCaptureSession[i] = cameraCaptureSession;
                        try {
                            CaptureModule.this.setUpVideoCaptureRequestBuilder(CaptureModule.this.mVideoRequestBuilder, i);
                            CaptureModule.this.removeImageReaderSurfaces(CaptureModule.this.mVideoRequestBuilder);
                            CaptureModule.this.mCurrentSession.setRepeatingRequest(CaptureModule.this.mVideoRequestBuilder.build(), CaptureModule.this.mCaptureCallback, CaptureModule.this.mCameraHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        } catch (IllegalStateException e2) {
                            e2.printStackTrace();
                        }
                        if (CaptureModule.this.mFrameProcessor.isFrameListnerEnabled() || CaptureModule.this.startMediaRecorder()) {
                            CaptureModule.this.mUI.clearFocus();
                            CaptureModule.this.mUI.resetPauseButton();
                            CaptureModule.this.mRecordingTotalTime = 0;
                            CaptureModule.this.mRecordingStartTime = SystemClock.uptimeMillis();
                            CaptureModule.this.mUI.enableShutter(true);
                            CaptureModule.this.mUI.showRecordingUI(true, false);
                            CaptureModule.this.updateRecordingTime();
                            CaptureModule.this.keepScreenOn();
                            CaptureModule.this.mStartRecPending = false;
                            return;
                        }
                        CaptureModule.this.mUI.showUIafterRecording();
                        CaptureModule.this.releaseMediaRecorder();
                        CaptureModule.this.mFrameProcessor.setVideoOutputSurface(null);
                        CaptureModule.this.restartSession(true);
                        CaptureModule.this.mStartRecPending = false;
                    }

                    public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                        CaptureModule.this.setCameraModeSwitcherAllowed(true);
                        Toast.makeText(CaptureModule.this.mActivity, "Video Failed", 0).show();
                        CaptureModule.this.mStartRecPending = false;
                    }
                };
                if (this.mSettingsManager.getSavePictureFormat() != 1 || this.mLiveShotInitHeifWriter == null) {
                    arrayList.add(this.mVideoSnapshotImageReader.getSurface());
                    this.mCameraDevice[i].createCaptureSession(arrayList, r3, null);
                    return true;
                }
                ArrayList arrayList2 = new ArrayList();
                for (Surface outputConfiguration : arrayList) {
                    arrayList2.add(new OutputConfiguration(outputConfiguration));
                }
                this.mLiveShotOutput = new OutputConfiguration(this.mLiveShotInitHeifWriter.getInputSurface());
                this.mLiveShotOutput.enableSurfaceSharing();
                arrayList2.add(this.mLiveShotOutput);
                this.mCameraDevice[i].createCaptureSessionByOutputConfigurations(arrayList2, r3, null);
                return true;
            }
            this.mCameraDevice[i].createConstrainedHighSpeedCaptureSession(arrayList, new CameraCaptureSession.StateCallback() {
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    String str = "Failed to start high speed video recording ";
                    String str2 = CaptureModule.TAG;
                    CaptureModule.this.setCameraModeSwitcherAllowed(true);
                    CaptureModule.this.mCurrentSession = cameraCaptureSession;
                    CaptureModule.this.mCaptureSession[i] = cameraCaptureSession;
                    CameraConstrainedHighSpeedCaptureSession cameraConstrainedHighSpeedCaptureSession = (CameraConstrainedHighSpeedCaptureSession) CaptureModule.this.mCurrentSession;
                    try {
                        CaptureModule.this.setUpVideoCaptureRequestBuilder(CaptureModule.this.mVideoRequestBuilder, i);
                        CaptureModule.this.removeImageReaderSurfaces(CaptureModule.this.mVideoRequestBuilder);
                        cameraConstrainedHighSpeedCaptureSession.setRepeatingBurst(cameraConstrainedHighSpeedCaptureSession.createHighSpeedRequestList(CaptureModule.this.mVideoRequestBuilder.build()), CaptureModule.this.mCaptureCallback, CaptureModule.this.mCameraHandler);
                    } catch (CameraAccessException e) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(str);
                        sb.append(e.getMessage());
                        Log.e(str2, sb.toString());
                        e.printStackTrace();
                    } catch (IllegalArgumentException e2) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(str);
                        sb2.append(e2.getMessage());
                        Log.e(str2, sb2.toString());
                        e2.printStackTrace();
                    } catch (IllegalStateException e3) {
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append(str);
                        sb3.append(e3.getMessage());
                        Log.e(str2, sb3.toString());
                        e3.printStackTrace();
                    }
                    if (CaptureModule.this.mFrameProcessor.isFrameListnerEnabled() || CaptureModule.this.startMediaRecorder()) {
                        CaptureModule.this.mUI.clearFocus();
                        CaptureModule.this.mUI.resetPauseButton();
                        CaptureModule.this.mRecordingTotalTime = 0;
                        CaptureModule.this.mRecordingStartTime = SystemClock.uptimeMillis();
                        CaptureModule.this.mUI.enableShutter(false);
                        CaptureModule.this.mUI.setMediaSaved(false);
                        CaptureModule.this.mUI.showRecordingUI(true, true);
                        CaptureModule.this.updateRecordingTime();
                        CaptureModule.this.keepScreenOn();
                        CaptureModule.this.mStartRecPending = false;
                        return;
                    }
                    CaptureModule.this.mUI.showUIafterRecording();
                    CaptureModule.this.releaseMediaRecorder();
                    CaptureModule.this.mFrameProcessor.setVideoOutputSurface(null);
                    CaptureModule.this.restartSession(true);
                    CaptureModule.this.mStartRecPending = false;
                }

                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    CaptureModule.this.setCameraModeSwitcherAllowed(true);
                    CaptureModule.this.mCheckStorageHandler.removeMessages(0);
                    Toast.makeText(CaptureModule.this.mActivity, "Failed", 0).show();
                    CaptureModule.this.mStartRecPending = false;
                }
            }, null);
            return true;
        } catch (CameraAccessException | IOException | IllegalStateException e) {
            this.mCheckStorageHandler.removeMessages(0);
            setCameraModeSwitcherAllowed(true);
            e.printStackTrace();
            this.mStartRecPending = false;
        }
    }

    private boolean isSessionSupportedByEncoder(int i, int i2, int i3) {
        int i4 = i * i2 * i3;
        for (VideoEncoderCap videoEncoderCap : EncoderCapabilities.getVideoEncoders()) {
            if (videoEncoderCap.mCodec == this.mVideoEncoder) {
                if (i4 <= videoEncoderCap.mMaxFrameWidth * videoEncoderCap.mMaxFrameHeight * videoEncoderCap.mMaxFrameRate) {
                    return true;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("Selected codec ");
                sb.append(this.mVideoEncoder);
                sb.append(" does not support width(");
                sb.append(i);
                sb.append(") X height (");
                sb.append(i2);
                sb.append("@ ");
                sb.append(i3);
                sb.append(" fps");
                String sb2 = sb.toString();
                String str = TAG;
                Log.e(str, sb2);
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Max capabilities: MaxFrameWidth = ");
                sb3.append(videoEncoderCap.mMaxFrameWidth);
                sb3.append(" , MaxFrameHeight = ");
                sb3.append(videoEncoderCap.mMaxFrameHeight);
                sb3.append(" , MaxFrameRate = ");
                sb3.append(videoEncoderCap.mMaxFrameRate);
                Log.e(str, sb3.toString());
                return false;
            }
        }
        return false;
    }

    private void updateTimeLapseSetting() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL);
        if (value != null) {
            this.mTimeBetweenTimeLapseFrameCaptureMs = Integer.parseInt(value);
            this.mCaptureTimeLapse = this.mTimeBetweenTimeLapseFrameCaptureMs != 0;
            this.mUI.showTimeLapseUI(this.mCaptureTimeLapse);
        }
    }

    private void updateVideoEncoder() {
        this.mVideoEncoder = SettingTranslation.getVideoEncoder(this.mSettingsManager.getValue("pref_camera_videoencoder_key"));
    }

    private void updateHFRSetting() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_VIDEO_HIGH_FRAME_RATE);
        if (value != null) {
            if (value.equals("off")) {
                this.mHighSpeedCapture = false;
                this.mHighSpeedCaptureRate = 0;
            } else {
                this.mHighSpeedCapture = true;
                this.mHighSpeedRecordingMode = value.substring(0, 3).equals("hsr");
                this.mHighSpeedCaptureRate = Integer.parseInt(value.substring(3));
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean startMediaRecorder() {
        boolean z = this.mUnsupportedResolution;
        String str = TAG;
        if (z) {
            Log.v(str, "Unsupported Resolution according to target");
            this.mStartRecPending = false;
            this.mIsRecordingVideo = false;
            return false;
        } else if (this.mMediaRecorder == null) {
            Log.e(str, "Fail to initialize media recorder");
            this.mStartRecPending = false;
            this.mIsRecordingVideo = false;
            return false;
        } else {
            requestAudioFocus();
            try {
                this.mMediaRecorder.start();
                StringBuilder sb = new StringBuilder();
                sb.append("StartRecordingVideo done. Time=");
                sb.append(System.currentTimeMillis() - this.mStartRecordingTime);
                sb.append("ms");
                Log.d(str, sb.toString());
                return true;
            } catch (RuntimeException unused) {
                Toast.makeText(this.mActivity, "Could not start media recorder.\n Can't start video recording.", 1).show();
                releaseMediaRecorder();
                releaseAudioFocus();
                this.mStartRecPending = false;
                this.mIsRecordingVideo = false;
                return false;
            }
        }
    }

    public void startMediaRecording() {
        if (!startMediaRecorder()) {
            this.mUI.showUIafterRecording();
            releaseMediaRecorder();
            this.mFrameProcessor.setVideoOutputSurface(null);
            restartSession(true);
        }
    }

    /* access modifiers changed from: private */
    public void setUpVideoCaptureRequestBuilder(Builder builder, int i) {
        builder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(3));
        applyVideoCommentSettings(builder, i);
    }

    private void setUpVideoPreviewRequestBuilder(Surface surface, int i) {
        try {
            this.mVideoPreviewRequestBuilder = this.mCameraDevice[i].createCaptureRequest(1);
            this.mVideoPreviewRequestBuilder.setTag(Integer.valueOf(i));
            this.mVideoPreviewRequestBuilder.addTarget(surface);
            this.mVideoPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(this.mControlAFMode));
            if (this.mHighSpeedCapture) {
                this.mVideoPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, this.mHighSpeedFPSRange);
            } else {
                this.mHighSpeedFPSRange = new Range(Integer.valueOf(30), Integer.valueOf(30));
                this.mVideoPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, this.mHighSpeedFPSRange);
            }
            if (!this.mHighSpeedCapture || ((Integer) this.mHighSpeedFPSRange.getUpper()).intValue() <= 30) {
                applyVideoCommentSettings(this.mVideoPreviewRequestBuilder, i);
            }
        } catch (CameraAccessException unused) {
            Log.w(TAG, "setUpVideoPreviewRequestBuilder, Camera access failed");
        }
    }

    private void applyVideoCommentSettings(Builder builder, int i) {
        CaptureRequest.Key key = CaptureRequest.CONTROL_MODE;
        Integer valueOf = Integer.valueOf(1);
        builder.set(key, valueOf);
        builder.set(CaptureRequest.CONTROL_AE_MODE, valueOf);
        applyVideoStabilization(builder);
        applyAntiBandingLevel(builder);
        applyNoiseReduction(builder);
        applyColorEffect(builder);
        applyVideoFlash(builder);
        applyFaceDetection(builder);
        applyZoom(builder, i);
        applyVideoHDR(builder);
        applyEarlyPCR(builder);
        applyWhiteBalance(builder);
        enableSat(builder, i);
    }

    private void applyVideoHDR(Builder builder) {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_VIDEO_HDR_VALUE);
        if (value != null) {
            try {
                builder.set(support_video_hdr_values, Integer.valueOf(Integer.parseInt(value)));
            } catch (IllegalArgumentException unused) {
                StringBuilder sb = new StringBuilder();
                sb.append("cannot find vendor tag: ");
                sb.append(support_video_hdr_values.toString());
                Log.w(TAG, sb.toString());
            }
        }
    }

    private void updateVideoFlash() {
        if (this.mIsRecordingVideo && !this.mHighSpeedCapture) {
            applyVideoFlash(this.mVideoRequestBuilder);
            applyVideoFlash(this.mVideoPreviewRequestBuilder);
            try {
                this.mCurrentSession.setRepeatingRequest((this.mMediaRecorderPausing ? this.mVideoPreviewRequestBuilder : this.mVideoRequestBuilder).build(), this.mCaptureCallback, this.mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void applyVideoFlash(Builder builder) {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_VIDEO_FLASH_MODE);
        if (value != null) {
            if (value.equals("torch")) {
                builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(2));
            } else {
                builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
            }
        }
    }

    private void applyNoiseReduction(Builder builder) {
        String value = this.mSettingsManager.getValue("pref_camera_noise_reduction_key");
        if (value != null) {
            builder.set(CaptureRequest.NOISE_REDUCTION_MODE, Integer.valueOf(SettingTranslation.getNoiseReduction(value)));
        }
    }

    private void applyVideoStabilization(Builder builder) {
        String value = this.mSettingsManager.getValue("pref_camera_dis_key");
        if (value != null) {
            if (!value.equals(RecordLocationPreference.VALUE_ON) || (this.mHighSpeedCapture && ((Integer) this.mHighSpeedFPSRange.getUpper()).intValue() > 60)) {
                builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, Integer.valueOf(0));
            } else {
                builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, Integer.valueOf(1));
            }
        }
    }

    private long getTimeLapseVideoLength(long j) {
        return (long) (((((double) j) / ((double) this.mTimeBetweenTimeLapseFrameCaptureMs)) / ((double) this.mProfile.videoFrameRate)) * 1000.0d);
    }

    /* access modifiers changed from: private */
    public void updateRecordingTime() {
        long j;
        String str;
        if (this.mIsRecordingVideo && !this.mMediaRecorderPausing) {
            long uptimeMillis = (SystemClock.uptimeMillis() - this.mRecordingStartTime) + this.mRecordingTotalTime;
            int i = this.mMaxVideoDurationInMs;
            boolean z = i != 0 && uptimeMillis >= ((long) (i - 60000));
            long max = z ? Math.max(0, ((long) this.mMaxVideoDurationInMs) - uptimeMillis) + 999 : uptimeMillis;
            if (!this.mCaptureTimeLapse) {
                str = CameraUtil.millisecondToTimeString(max, false);
                j = 1000;
            } else {
                str = CameraUtil.millisecondToTimeString(getTimeLapseVideoLength(uptimeMillis), true);
                j = (long) this.mTimeBetweenTimeLapseFrameCaptureMs;
            }
            this.mUI.setRecordingTime(str);
            if (this.mRecordingTimeCountsDown != z) {
                this.mRecordingTimeCountsDown = z;
                this.mUI.setRecordingTimeTextColor(this.mActivity.getResources().getColor(z ? C0905R.color.recording_time_remaining_text : C0905R.color.recording_time_elapsed_text));
            }
            this.mHandler.sendEmptyMessageDelayed(5, j - (uptimeMillis % j));
        }
    }

    private void pauseVideoRecording() {
        Log.v(TAG, "pauseVideoRecording");
        this.mMediaRecorderPausing = true;
        this.mRecordingTotalTime += SystemClock.uptimeMillis() - this.mRecordingStartTime;
        setEndOfStream(false, false);
    }

    private void resumeVideoRecording() {
        String str = TAG;
        Log.v(str, "resumeVideoRecording");
        this.mMediaRecorderPausing = false;
        this.mRecordingStartTime = SystemClock.uptimeMillis();
        updateRecordingTime();
        setEndOfStream(true, false);
        if (!ApiHelper.HAS_RESUME_SUPPORTED) {
            this.mMediaRecorder.start();
            return;
        }
        try {
            Class.forName("android.media.MediaRecorder").getMethod("resume", new Class[0]).invoke(this.mMediaRecorder, new Object[0]);
        } catch (Exception unused) {
            Log.v(str, "resume method not implemented");
        }
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(6:5|6|7|8|9|10) */
    /* JADX WARNING: Can't wrap try/catch for region: R(7:17|18|19|20|21|22|(1:24)(1:25)) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:20:0x003b */
    /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0019 */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:9:0x0019=Splitter:B:9:0x0019, B:20:0x003b=Splitter:B:20:0x003b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setEndOfStream(boolean r6, boolean r7) {
        /*
            r5 = this;
            android.hardware.camera2.CameraCaptureSession r0 = r5.mCurrentSession
            if (r0 != 0) goto L_0x0005
            return
        L_0x0005:
            r0 = 0
            java.lang.String r1 = "can not find vendor tag: org.quic.camera.recording.endOfStream"
            java.lang.String r2 = "SnapCam_CaptureModule"
            if (r6 == 0) goto L_0x001d
            android.hardware.camera2.CaptureRequest$Builder r0 = r5.mVideoRequestBuilder     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CaptureRequest$Key<java.lang.Byte> r6 = recording_end_stream     // Catch:{ IllegalArgumentException -> 0x0019 }
            r3 = 0
            java.lang.Byte r3 = java.lang.Byte.valueOf(r3)     // Catch:{ IllegalArgumentException -> 0x0019 }
            r0.set(r6, r3)     // Catch:{ IllegalArgumentException -> 0x0019 }
            goto L_0x007b
        L_0x0019:
            android.util.Log.w(r2, r1)     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            goto L_0x007b
        L_0x001d:
            boolean r6 = r5.mMediaRecorderPausing     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            if (r6 != 0) goto L_0x0025
            boolean r6 = r5.mStopRecPending     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            if (r6 == 0) goto L_0x006b
        L_0x0025:
            android.hardware.camera2.CameraCaptureSession r6 = r5.mCurrentSession     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            if (r6 == 0) goto L_0x006b
            android.hardware.camera2.CameraCaptureSession r6 = r5.mCurrentSession     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            r6.stopRepeating()     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CaptureRequest$Builder r6 = r5.mVideoRequestBuilder     // Catch:{ IllegalArgumentException -> 0x003b }
            android.hardware.camera2.CaptureRequest$Key<java.lang.Byte> r3 = recording_end_stream     // Catch:{ IllegalArgumentException -> 0x003b }
            r4 = 1
            java.lang.Byte r4 = java.lang.Byte.valueOf(r4)     // Catch:{ IllegalArgumentException -> 0x003b }
            r6.set(r3, r4)     // Catch:{ IllegalArgumentException -> 0x003b }
            goto L_0x003e
        L_0x003b:
            android.util.Log.w(r2, r1)     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
        L_0x003e:
            android.hardware.camera2.CameraCaptureSession r6 = r5.mCurrentSession     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            boolean r6 = r6 instanceof android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            if (r6 == 0) goto L_0x005c
            android.hardware.camera2.CameraCaptureSession r6 = r5.mCurrentSession     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession r6 = (android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession) r6     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CaptureRequest$Builder r1 = r5.mVideoRequestBuilder     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CaptureRequest r1 = r1.build()     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            java.util.List r6 = r6.createHighSpeedRequestList(r1)     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CameraCaptureSession r1 = r5.mCurrentSession     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r2 = r5.mCaptureCallback     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.os.Handler r3 = r5.mCameraHandler     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            r1.captureBurst(r6, r2, r3)     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            goto L_0x006b
        L_0x005c:
            android.hardware.camera2.CameraCaptureSession r6 = r5.mCurrentSession     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CaptureRequest$Builder r1 = r5.mVideoRequestBuilder     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CaptureRequest r1 = r1.build()     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r2 = r5.mCaptureCallback     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.os.Handler r3 = r5.mCameraHandler     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            r6.capture(r1, r2, r3)     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
        L_0x006b:
            if (r7 != 0) goto L_0x007b
            android.media.MediaRecorder r6 = r5.mMediaRecorder     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            r6.pause()     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CaptureRequest$Builder r0 = r5.mVideoPreviewRequestBuilder     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            int r6 = r5.getMainCameraId()     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            r5.applyVideoCommentSettings(r0, r6)     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
        L_0x007b:
            if (r0 == 0) goto L_0x00c2
            android.hardware.camera2.CameraCaptureSession r6 = r5.mCurrentSession     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            if (r6 == 0) goto L_0x00c2
            if (r7 != 0) goto L_0x00c2
            android.hardware.camera2.CameraCaptureSession r6 = r5.mCurrentSession     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            boolean r6 = r6 instanceof android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            if (r6 == 0) goto L_0x009f
            android.hardware.camera2.CameraCaptureSession r6 = r5.mCurrentSession     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession r6 = (android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession) r6     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CaptureRequest r7 = r0.build()     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            java.util.List r6 = r6.createHighSpeedRequestList(r7)     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CameraCaptureSession r7 = r5.mCurrentSession     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r0 = r5.mCaptureCallback     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.os.Handler r1 = r5.mCameraHandler     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            r7.setRepeatingBurst(r6, r0, r1)     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            goto L_0x00c2
        L_0x009f:
            android.hardware.camera2.CameraCaptureSession r6 = r5.mCurrentSession     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CaptureRequest r7 = r0.build()     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r0 = r5.mCaptureCallback     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            android.os.Handler r1 = r5.mCameraHandler     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            r6.setRepeatingRequest(r7, r0, r1)     // Catch:{ CameraAccessException -> 0x00b7, IllegalStateException -> 0x00b2, NullPointerException -> 0x00ad }
            goto L_0x00c2
        L_0x00ad:
            r5 = move-exception
            r5.printStackTrace()
            goto L_0x00c2
        L_0x00b2:
            r5 = move-exception
            r5.printStackTrace()
            goto L_0x00c2
        L_0x00b7:
            r6 = move-exception
            int r7 = r5.getMainCameraId()
            r5.stopRecordingVideo(r7)
            r6.printStackTrace()
        L_0x00c2:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CaptureModule.setEndOfStream(boolean, boolean):void");
    }

    public void onButtonPause() {
        pauseVideoRecording();
    }

    public void onButtonContinue() {
        resumeVideoRecording();
    }

    private boolean isAbortCapturesEnable() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_ABORT_CAPTURES);
        boolean equals = value != null ? value.equals(this.mActivity.getResources().getString(C0905R.string.pref_camera2_abort_captures_entry_value_enable)) : false;
        StringBuilder sb = new StringBuilder();
        sb.append("isAbortCapturesEnable :");
        sb.append(equals);
        Log.v(TAG, sb.toString());
        return equals;
    }

    private boolean isSendRequestAfterFlushEnable() {
        return PersistUtil.isSendRequestAfterFlush();
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00cb  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00ed  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0104  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0111  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0128  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0136  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void stopRecordingVideo(int r9) {
        /*
            r8 = this;
            boolean r0 = r8.mIsCloseBokeh
            r1 = 0
            if (r0 == 0) goto L_0x000c
            r8.mIsCloseBokeh = r1
            com.android.camera.CaptureUI r0 = r8.mUI
            r0.openBokeh()
        L_0x000c:
            boolean r0 = r8.mIsCloseMakeUp
            r2 = 1
            if (r0 == 0) goto L_0x0018
            r8.mIsCloseMakeUp = r1
            com.android.camera.CaptureUI r0 = r8.mUI
            r0.UpdateMakeupLayoutStatus(r2)
        L_0x0018:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r3 = "stopRecordingVideo "
            r0.append(r3)
            r0.append(r9)
            java.lang.String r0 = r0.toString()
            java.lang.String r3 = "SnapCam_CaptureModule"
            android.util.Log.d(r3, r0)
            android.os.Handler r0 = r8.mCheckStorageHandler
            r0.removeMessages(r1)
            boolean r0 = r8.getCameraModeSwitcherAllowed()
            if (r0 != 0) goto L_0x004e
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r0 = "waiting for session config "
            r8.append(r0)
            r8.append(r9)
            java.lang.String r8 = r8.toString()
            android.util.Log.d(r3, r8)
            return
        L_0x004e:
            long r4 = java.lang.System.currentTimeMillis()
            r8.mStopRecordingTime = r4
            r8.mStopRecPending = r2
            r8.checkAndPlayRecordSound(r9, r1)
            r8.setEndOfStream(r1, r2)
            com.android.camera.imageprocessor.FrameProcessor r9 = r8.mFrameProcessor
            r0 = 0
            r9.setVideoOutputSurface(r0)
            com.android.camera.imageprocessor.FrameProcessor r9 = r8.mFrameProcessor
            r9.onClose()
            androidx.heifwriter.HeifWriter r9 = r8.mLiveShotInitHeifWriter
            if (r9 == 0) goto L_0x006e
            r9.close()
        L_0x006e:
            boolean r9 = r8.mPaused
            if (r9 != 0) goto L_0x0075
            r8.closePreviewSession()
        L_0x0075:
            android.media.MediaRecorder r9 = r8.mMediaRecorder     // Catch:{ RuntimeException -> 0x00bb }
            r9.setOnErrorListener(r0)     // Catch:{ RuntimeException -> 0x00bb }
            android.media.MediaRecorder r9 = r8.mMediaRecorder     // Catch:{ RuntimeException -> 0x00bb }
            r9.setOnInfoListener(r0)     // Catch:{ RuntimeException -> 0x00bb }
            android.media.MediaRecorder r9 = r8.mMediaRecorder     // Catch:{ RuntimeException -> 0x00bb }
            r9.stop()     // Catch:{ RuntimeException -> 0x00bb }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x00b8 }
            r9.<init>()     // Catch:{ RuntimeException -> 0x00b8 }
            java.lang.String r0 = "stopRecordingVideo done. Time="
            r9.append(r0)     // Catch:{ RuntimeException -> 0x00b8 }
            long r4 = java.lang.System.currentTimeMillis()     // Catch:{ RuntimeException -> 0x00b8 }
            long r6 = r8.mStopRecordingTime     // Catch:{ RuntimeException -> 0x00b8 }
            long r4 = r4 - r6
            r9.append(r4)     // Catch:{ RuntimeException -> 0x00b8 }
            java.lang.String r0 = "ms"
            r9.append(r0)     // Catch:{ RuntimeException -> 0x00b8 }
            java.lang.String r9 = r9.toString()     // Catch:{ RuntimeException -> 0x00b8 }
            android.util.Log.d(r3, r9)     // Catch:{ RuntimeException -> 0x00b8 }
            com.android.camera.CaptureUI r9 = r8.mUI     // Catch:{ RuntimeException -> 0x00b8 }
            android.widget.ImageView r9 = r9.getVideoButton()     // Catch:{ RuntimeException -> 0x00b8 }
            com.android.camera.CameraActivity r0 = r8.mActivity     // Catch:{ RuntimeException -> 0x00b8 }
            r4 = 2131690785(0x7f0f0521, float:1.9010623E38)
            java.lang.String r0 = r0.getString(r4)     // Catch:{ RuntimeException -> 0x00b8 }
            com.android.camera.util.AccessibilityUtils.makeAnnouncement(r9, r0)     // Catch:{ RuntimeException -> 0x00b8 }
            r0 = r2
            goto L_0x00c9
        L_0x00b8:
            r9 = move-exception
            r0 = r2
            goto L_0x00bd
        L_0x00bb:
            r9 = move-exception
            r0 = r1
        L_0x00bd:
            java.lang.String r4 = "MediaRecoder stop fail"
            android.util.Log.w(r3, r4, r9)
            java.lang.String r9 = r8.mVideoFilename
            if (r9 == 0) goto L_0x00c9
            r8.deleteVideoFile(r9)
        L_0x00c9:
            if (r0 == 0) goto L_0x00ce
            r8.saveVideo()
        L_0x00ce:
            r8.keepScreenOnAwhile()
            r8.releaseMediaRecorder()
            r8.releaseAudioFocus()
            com.android.camera.CameraActivity r9 = r8.mActivity
            r9.setShouldUpdateThumbnailFromFile(r2)
            com.android.camera.CaptureUI r9 = r8.mUI
            r9.showRecordingUI(r1, r1)
            com.android.camera.CaptureUI r9 = r8.mUI
            r9.enableShutter(r2)
            r8.mIsRecordingVideo = r1
            int r9 = r8.mIntentMode
            r0 = 2
            if (r9 != r0) goto L_0x0100
            boolean r9 = r8.isQuickCapture()
            if (r9 == 0) goto L_0x00f7
            r8.onRecordingDone(r2)
            goto L_0x0100
        L_0x00f7:
            android.graphics.Bitmap r9 = r8.getVideoThumbnail()
            com.android.camera.CaptureUI r0 = r8.mUI
            r0.showRecordVideoForReview(r9)
        L_0x0100:
            com.android.camera.imageprocessor.FrameProcessor r9 = r8.mFrameProcessor
            if (r9 == 0) goto L_0x010d
            java.util.ArrayList r0 = r8.getFrameProcFilterId()
            android.util.Size r3 = r8.mPreviewSize
            r9.onOpen(r0, r3)
        L_0x010d:
            com.android.camera.imageprocessor.PostProcessor r9 = r8.mPostProcessor
            if (r9 == 0) goto L_0x0114
            r9.enableZSLQueue(r2)
        L_0x0114:
            com.android.camera.CaptureUI r9 = r8.mUI
            android.util.Size r0 = r8.mPreviewSize
            int r0 = r0.getWidth()
            android.util.Size r2 = r8.mPreviewSize
            int r2 = r2.getHeight()
            boolean r9 = r9.setPreviewSize(r0, r2)
            if (r9 == 0) goto L_0x0132
            com.android.camera.CaptureUI r9 = r8.mUI
            r9.hideSurfaceView()
            com.android.camera.CaptureUI r9 = r8.mUI
            r9.showSurfaceView()
        L_0x0132:
            boolean r9 = r8.mPaused
            if (r9 != 0) goto L_0x0139
            r8.createSessions()
        L_0x0139:
            com.android.camera.CaptureUI r9 = r8.mUI
            r9.showUIafterRecording()
            com.android.camera.CaptureUI r9 = r8.mUI
            r9.resetTrackingFocus()
            r8.mStopRecPending = r1
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CaptureModule.stopRecordingVideo(int):void");
    }

    private void closePreviewSession() {
        Log.d(TAG, "closePreviewSession");
        if (this.mCurrentSession != null) {
            int mainCameraId = getMainCameraId();
            this.mCurrentSession.close();
            if (this.mCurrentSession.equals(this.mCaptureSession[mainCameraId])) {
                this.mCaptureSession[mainCameraId] = null;
            }
            this.mCurrentSession = null;
        }
    }

    private String createName(long j) {
        return new SimpleDateFormat(this.mActivity.getString(C0905R.string.video_file_name_format)).format(new Date(j));
    }

    /* access modifiers changed from: private */
    public void removeImageReaderSurfaces(Builder builder) {
        for (int i = 0; i < 4; i++) {
            ImageReader[] imageReaderArr = this.mImageReader;
            if (imageReaderArr[i] != null) {
                builder.removeTarget(imageReaderArr[i].getSurface());
            }
        }
    }

    private String generateVideoFilename(int i) {
        String str;
        long currentTimeMillis = System.currentTimeMillis();
        String createName = createName(currentTimeMillis);
        StringBuilder sb = new StringBuilder();
        sb.append(createName);
        sb.append(CameraUtil.convertOutputFormatToFileExt(i));
        String sb2 = sb.toString();
        String convertOutputFormatToMimeType = CameraUtil.convertOutputFormatToMimeType(i);
        if (!Storage.isSaveSDCard() || !SDCard.instance().isWriteable()) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(Storage.DIRECTORY);
            sb3.append('/');
            sb3.append(sb2);
            str = sb3.toString();
        } else {
            StringBuilder sb4 = new StringBuilder();
            sb4.append(SDCard.instance().getDirectory());
            sb4.append('/');
            sb4.append(sb2);
            str = sb4.toString();
        }
        this.mCurrentVideoValues = new ContentValues(9);
        this.mCurrentVideoValues.put(TinyPlanetFragment.ARGUMENT_TITLE, createName);
        this.mCurrentVideoValues.put("_display_name", sb2);
        this.mCurrentVideoValues.put("datetaken", Long.valueOf(currentTimeMillis));
        this.mCurrentVideoValues.put("date_modified", Long.valueOf(currentTimeMillis / 1000));
        this.mCurrentVideoValues.put("mime_type", convertOutputFormatToMimeType);
        this.mCurrentVideoValues.put("_data", str);
        ContentValues contentValues = this.mCurrentVideoValues;
        StringBuilder sb5 = new StringBuilder();
        sb5.append(BuildConfig.FLAVOR);
        sb5.append(this.mVideoSize.getWidth());
        sb5.append("x");
        sb5.append(this.mVideoSize.getHeight());
        contentValues.put("resolution", sb5.toString());
        Location currentLocation = this.mLocationManager.getCurrentLocation();
        if (currentLocation != null) {
            this.mCurrentVideoValues.put("latitude", Double.valueOf(currentLocation.getLatitude()));
            this.mCurrentVideoValues.put("longitude", Double.valueOf(currentLocation.getLongitude()));
        }
        this.mVideoFilename = str;
        return str;
    }

    private void saveVideo() {
        if (this.mVideoFileDescriptor == null) {
            File file = new File(this.mVideoFilename);
            boolean exists = file.exists();
            String str = TAG;
            if (exists) {
                long j = 0;
                if (file.length() > 0) {
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    try {
                        mediaMetadataRetriever.setDataSource(this.mVideoFilename);
                        j = Long.valueOf(mediaMetadataRetriever.extractMetadata(9)).longValue();
                    } catch (IllegalArgumentException unused) {
                        Log.e(str, "cannot access the file");
                    }
                    long j2 = j;
                    mediaMetadataRetriever.release();
                    this.mActivity.getMediaSaveService().addVideo(this.mVideoFilename, j2, this.mCurrentVideoValues, this.mOnVideoSavedListener, this.mContentResolver);
                }
            }
            Log.e(str, "Invalid file");
            this.mCurrentVideoValues = null;
            return;
        }
        this.mCurrentVideoValues = null;
    }

    private void setUpMediaRecorder(int i) throws IOException {
        String str = TAG;
        Log.d(str, "setUpMediaRecorder");
        int intValue = ((Integer) CameraSettings.VIDEO_QUALITY_TABLE.get(this.mSettingsManager.getValue("pref_video_quality_key"))).intValue();
        Intent intent = this.mActivity.getIntent();
        String str2 = "android.intent.extra.videoQuality";
        if (intent.hasExtra(str2)) {
            intValue = intent.getIntExtra(str2, 0) > 0 ? 1 : 0;
        }
        if (this.mCaptureTimeLapse) {
            intValue = CameraSettings.getTimeLapseQualityFor(intValue);
        }
        Bundle extras = intent.getExtras();
        if (this.mMediaRecorder == null) {
            this.mMediaRecorder = new MediaRecorder();
        }
        boolean z = this.mHighSpeedCapture && !this.mHighSpeedRecordingMode;
        if (CamcorderProfile.hasProfile(i, intValue)) {
            this.mProfile = CamcorderProfile.get(i, intValue);
        } else {
            if (!"-1".equals(this.mSettingsManager.getValue(SettingsManager.KEY_SWITCH_CAMERA))) {
                this.mProfile = CamcorderProfile.get(1);
            } else {
                RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.error_app_unsupported_profile, 1).show();
                return;
            }
        }
        CamcorderProfile camcorderProfile = this.mProfile;
        int i2 = camcorderProfile.videoFrameWidth;
        int i3 = camcorderProfile.videoFrameHeight;
        this.mUnsupportedResolution = false;
        int audioEncoder = SettingTranslation.getAudioEncoder(this.mSettingsManager.getValue("pref_camera_audioencoder_key"));
        this.mProfile.videoCodec = this.mVideoEncoder;
        if (!this.mCaptureTimeLapse && !z) {
            this.mMediaRecorder.setAudioSource(5);
            CamcorderProfile camcorderProfile2 = this.mProfile;
            camcorderProfile2.audioCodec = audioEncoder;
            if (camcorderProfile2.audioCodec == 1) {
                camcorderProfile2.fileFormat = 1;
            }
        }
        this.mMediaRecorder.setVideoSource(2);
        this.mMediaRecorder.setOutputFormat(this.mProfile.fileFormat);
        closeVideoFileDescriptor();
        if (this.mIntentMode != 2 || extras == null) {
            String generateVideoFilename = generateVideoFilename(this.mProfile.fileFormat);
            StringBuilder sb = new StringBuilder();
            sb.append("New video filename: ");
            sb.append(generateVideoFilename);
            Log.v(str, sb.toString());
            this.mMediaRecorder.setOutputFile(generateVideoFilename);
        } else {
            Uri uri = (Uri) extras.getParcelable("output");
            if (uri != null) {
                try {
                    this.mCurrentVideoUri = uri;
                    this.mVideoFileDescriptor = this.mContentResolver.openFileDescriptor(uri, "rw");
                    this.mCurrentVideoUri = uri;
                } catch (FileNotFoundException e) {
                    Log.e(str, e.toString());
                }
            }
            this.mMediaRecorder.setOutputFile(this.mVideoFileDescriptor.getFileDescriptor());
        }
        this.mMediaRecorder.setVideoFrameRate(this.mProfile.videoFrameRate);
        this.mMediaRecorder.setVideoEncodingBitRate(this.mProfile.videoBitRate);
        if (this.mFrameProcessor.isFrameFilterEnabled()) {
            MediaRecorder mediaRecorder = this.mMediaRecorder;
            CamcorderProfile camcorderProfile3 = this.mProfile;
            mediaRecorder.setVideoSize(camcorderProfile3.videoFrameHeight, camcorderProfile3.videoFrameWidth);
        } else {
            MediaRecorder mediaRecorder2 = this.mMediaRecorder;
            CamcorderProfile camcorderProfile4 = this.mProfile;
            mediaRecorder2.setVideoSize(camcorderProfile4.videoFrameWidth, camcorderProfile4.videoFrameHeight);
        }
        this.mMediaRecorder.setVideoEncoder(this.mVideoEncoder);
        if (!this.mCaptureTimeLapse && !z) {
            this.mMediaRecorder.setAudioEncodingBitRate(this.mProfile.audioBitRate);
            this.mMediaRecorder.setAudioChannels(this.mProfile.audioChannels);
            this.mMediaRecorder.setAudioSamplingRate(this.mProfile.audioSampleRate);
            this.mMediaRecorder.setAudioEncoder(audioEncoder);
        }
        this.mMediaRecorder.setMaxDuration(this.mMaxVideoDurationInMs);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Profile video bitrate: ");
        sb2.append(this.mProfile.videoBitRate);
        Log.i(str, sb2.toString());
        StringBuilder sb3 = new StringBuilder();
        sb3.append("Profile video frame rate: ");
        sb3.append(this.mProfile.videoFrameRate);
        Log.i(str, sb3.toString());
        if (this.mCaptureTimeLapse) {
            this.mMediaRecorder.setCaptureRate(1000.0d / ((double) this.mTimeBetweenTimeLapseFrameCaptureMs));
        } else if (this.mHighSpeedCapture) {
            this.mHighSpeedFPSRange = new Range(Integer.valueOf(this.mHighSpeedCaptureRate), Integer.valueOf(this.mHighSpeedCaptureRate));
            int intValue2 = ((Integer) this.mHighSpeedFPSRange.getUpper()).intValue();
            this.mMediaRecorder.setCaptureRate((double) intValue2);
            int i4 = this.mHighSpeedRecordingMode ? intValue2 : 30;
            this.mMediaRecorder.setVideoFrameRate(i4);
            StringBuilder sb4 = new StringBuilder();
            sb4.append("Capture rate: ");
            sb4.append(intValue2);
            sb4.append(", Target rate: ");
            sb4.append(i4);
            Log.i(str, sb4.toString());
            int highSpeedVideoEncoderBitRate = this.mSettingsManager.getHighSpeedVideoEncoderBitRate(this.mProfile, i4, intValue2);
            StringBuilder sb5 = new StringBuilder();
            sb5.append("Scaled video bitrate : ");
            sb5.append(highSpeedVideoEncoderBitRate);
            Log.i(str, sb5.toString());
            this.mMediaRecorder.setVideoEncodingBitRate(highSpeedVideoEncoderBitRate);
        }
        long j = (!isVideoCaptureIntent() || extras == null) ? 0 : extras.getLong("android.intent.extra.sizeLimit");
        Iterator it = EncoderCapabilities.getVideoEncoders().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            VideoEncoderCap videoEncoderCap = (VideoEncoderCap) it.next();
            if (videoEncoderCap.mCodec == this.mVideoEncoder) {
                if (i2 > videoEncoderCap.mMaxFrameWidth || i2 < videoEncoderCap.mMinFrameWidth || i3 > videoEncoderCap.mMaxFrameHeight || i3 < videoEncoderCap.mMinFrameHeight) {
                    StringBuilder sb6 = new StringBuilder();
                    sb6.append("Selected codec ");
                    sb6.append(this.mVideoEncoder);
                    sb6.append(" does not support ");
                    sb6.append(i2);
                    sb6.append("x");
                    sb6.append(i3);
                    sb6.append(" resolution");
                    Log.e(str, sb6.toString());
                    StringBuilder sb7 = new StringBuilder();
                    sb7.append("Codec capabilities: mMinFrameWidth = ");
                    sb7.append(videoEncoderCap.mMinFrameWidth);
                    sb7.append(" , mMinFrameHeight = ");
                    sb7.append(videoEncoderCap.mMinFrameHeight);
                    sb7.append(" , mMaxFrameWidth = ");
                    sb7.append(videoEncoderCap.mMaxFrameWidth);
                    sb7.append(" , mMaxFrameHeight = ");
                    sb7.append(videoEncoderCap.mMaxFrameHeight);
                    Log.e(str, sb7.toString());
                    this.mUnsupportedResolution = true;
                    RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.error_app_unsupported, 1).show();
                    return;
                }
            }
        }
        long storageSpaceBytes = this.mActivity.getStorageSpaceBytes() - Storage.LOW_STORAGE_THRESHOLD_BYTES;
        if (j <= 0 || j >= storageSpaceBytes) {
            j = storageSpaceBytes;
        }
        if (Storage.isSaveSDCard() && j > -100663296) {
            j = -100663296;
        }
        StringBuilder sb8 = new StringBuilder();
        sb8.append("MediaRecorder setMaxFileSize: ");
        sb8.append(j);
        Log.i(str, sb8.toString());
        try {
            this.mMediaRecorder.setMaxFileSize(j);
        } catch (RuntimeException unused) {
        }
        Location currentLocation = this.mLocationManager.getCurrentLocation();
        if (currentLocation != null) {
            this.mMediaRecorder.setLocation((float) currentLocation.getLatitude(), (float) currentLocation.getLongitude());
        }
        int jpegRotation = CameraUtil.getJpegRotation(i, this.mOrientation);
        String value = this.mSettingsManager.getValue("pref_camera_video_rotation_key");
        if (value != null) {
            jpegRotation = (jpegRotation + Integer.parseInt(value)) % 360;
        }
        if (this.mFrameProcessor.isFrameFilterEnabled()) {
            this.mMediaRecorder.setOrientationHint(0);
        } else {
            this.mMediaRecorder.setOrientationHint(jpegRotation);
        }
        try {
            this.mMediaRecorder.prepare();
            this.mMediaRecorder.setOnErrorListener(this);
            this.mMediaRecorder.setOnInfoListener(this);
        } catch (IOException e2) {
            StringBuilder sb9 = new StringBuilder();
            sb9.append("prepare failed for ");
            sb9.append(this.mVideoFilename);
            Log.e(str, sb9.toString(), e2);
            releaseMediaRecorder();
            throw new RuntimeException(e2);
        }
    }

    public void onVideoButtonClick() {
        if (isRecorderReady() && getCameraMode() != 0) {
            if (!this.mUI.isBokehMode || !isBackCamera()) {
                boolean z = this.mUI.isBokehMode;
                String str = "0";
                String str2 = "can not find vendor tag : org.codeaurora.qcamera3.makeup.enable";
                String str3 = TAG;
                if (!z || isBackCamera()) {
                    CaptureUI captureUI = this.mUI;
                    if (captureUI.isMakeUp) {
                        this.mIsCloseMakeUp = true;
                        captureUI.UpdateMakeupLayoutStatus(false);
                        try {
                            this.mMakeUpRequestBuilder.set(makeup_enable, Boolean.valueOf(false));
                            this.mMakeUpRequestBuilder.set(makeup_level, Integer.valueOf(Integer.parseInt(str)));
                            this.mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    CaptureModule.this.mUI.setBokehTipVisibility(false);
                                }
                            });
                            this.mCaptureSession[getMainCameraId()].setRepeatingRequest(this.mMakeUpRequestBuilder.build(), this.mCaptureCallback, this.mCameraHandler);
                        } catch (Exception unused) {
                            Log.e(str3, str2);
                        }
                        startOrStopVideo();
                    } else {
                        startOrStopVideo();
                    }
                } else {
                    CaptureUI captureUI2 = this.mUI;
                    if (captureUI2.isMakeUp) {
                        this.mIsCloseMakeUp = true;
                        captureUI2.UpdateMakeupLayoutStatus(false);
                        try {
                            this.mMakeUpRequestBuilder.set(makeup_enable, Boolean.valueOf(false));
                            this.mMakeUpRequestBuilder.set(makeup_level, Integer.valueOf(Integer.parseInt(str)));
                            this.mCaptureSession[getMainCameraId()].setRepeatingRequest(this.mMakeUpRequestBuilder.build(), this.mCaptureCallback, this.mCameraHandler);
                        } catch (Exception unused2) {
                            Log.e(str3, str2);
                        }
                    }
                    startOrStopVideo();
                    this.mIsCloseBokeh = true;
                    this.mUI.closeBokeh();
                }
            } else {
                this.mIsCloseBokeh = true;
                this.mUI.closeBokeh();
            }
        }
    }

    /* access modifiers changed from: private */
    public void startOrStopVideo() {
        if (this.mIsRecordingVideo) {
            stopRecordingVideo(getMainCameraId());
        } else if (!startRecordingVideo(getMainCameraId())) {
            this.mUI.showUIafterRecording();
            releaseMediaRecorder();
        }
    }

    public void onShutterButtonClick() {
        if (this.mActivity.getStorageSpaceBytes() <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
            StringBuilder sb = new StringBuilder();
            sb.append("Not enough space or storage not ready. remaining=");
            sb.append(this.mActivity.getStorageSpaceBytes());
            Log.i(TAG, sb.toString());
        } else if (getSessionComplete()) {
            if (!this.mIsRecordingVideo) {
                int parseInt = Integer.parseInt(this.mSettingsManager.getValue("pref_camera_timer_key"));
                int i = this.mTimer;
                if (i > 0) {
                    parseInt = i;
                }
                if (this.mUI.isCountingDown()) {
                    this.mUI.cancelCountDown();
                    checkSelfieFlashAndTakePicture();
                    return;
                }
                this.mSingleshotActive = true;
                if (parseInt > 0) {
                    this.mUI.startCountDown(parseInt, true);
                } else if (this.mChosenImageFormat != 35 || !this.mPostProcessor.isItBusy()) {
                    checkSelfieFlashAndTakePicture();
                } else {
                    warningToast("It's still busy processing previous scene mode request.");
                }
            } else if (this.mUI.isShutterEnabled()) {
                captureVideoSnapshot(getMainCameraId());
            }
        }
    }

    private void warningToast(final String str) {
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                RotateTextToast.makeText((Activity) CaptureModule.this.mActivity, (CharSequence) str, 0).show();
            }
        });
    }

    public boolean isLongShotSettingEnabled() {
        return this.mSettingsManager.getValue("pref_camera_longshot_key").equals(RecordLocationPreference.VALUE_ON);
    }

    public void onShutterButtonLongClick() {
        if (!this.mUI.isBokehMode) {
            if ((isSceneMode() && !isProMode()) || this.mIntentMode == 1) {
                return;
            }
            if (!isBackCamera() || getCameraMode() != 0) {
                boolean isCurrentModeSupportLongClick = this.mSettingsManager.isCurrentModeSupportLongClick();
                String str = TAG;
                if (!isCurrentModeSupportLongClick) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.mSettingsManager.getCurrentMode());
                    sb.append(" mode is not support longClick to takePicture");
                    Log.d(str, sb.toString());
                    return;
                }
                if (isLongShotSettingEnabled()) {
                    if (this.mUI.isCountingDown()) {
                        this.mUI.cancelCountDown();
                    }
                    this.mActivity.updateStorageSpaceAndHint();
                    long storageSpaceBytes = this.mActivity.getStorageSpaceBytes();
                    if (storageSpaceBytes <= ((long) (mLongShotLimitNums * this.mJpegFileSizeEstimation)) + Storage.LOW_STORAGE_THRESHOLD_BYTES) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("Not enough space or storage not ready. remaining=");
                        sb2.append(storageSpaceBytes);
                        Log.i(str, sb2.toString());
                    } else if (isLongshotNeedCancel()) {
                        this.mLongshotActive = false;
                        try {
                            setRepeatingBurstForZSL(getMainCameraId());
                        } catch (CameraAccessException | IllegalStateException e) {
                            e.printStackTrace();
                        }
                        this.mUI.enableVideo(!this.mLongshotActive);
                        this.mUI.updateFlashUi(!this.mLongshotActive);
                    } else {
                        Log.d(str, "Start Longshot");
                        this.mUI.enableGestures(false);
                        this.mLongshotActive = true;
                        this.mSingleshotActive = false;
                        this.mFrameSendNums.getAndSet(0);
                        this.mImageArrivedNums.getAndSet(0);
                        try {
                            setRepeatingBurstForZSL(getMainCameraId());
                        } catch (CameraAccessException | IllegalStateException e2) {
                            e2.printStackTrace();
                        }
                        this.mUI.enableVideo(!this.mLongshotActive);
                        this.mUI.updateFlashUi(true ^ this.mLongshotActive);
                        takePicture();
                    }
                }
            }
        }
    }

    private void estimateJpegFileSize() {
        int i;
        String value = this.mSettingsManager.getValue("pref_camera_jpegquality_key");
        int[] intArray = this.mActivity.getResources().getIntArray(C0905R.array.jpegquality_compression_ratio);
        String[] stringArray = this.mActivity.getResources().getStringArray(C0905R.array.pref_camera_jpegquality_entryvalues);
        int length = intArray.length - 1;
        while (true) {
            if (length < 0) {
                i = 0;
                break;
            } else if (stringArray[length].equals(value)) {
                i = intArray[length];
                break;
            } else {
                length--;
            }
        }
        Size parsePictureSize = parsePictureSize(this.mSettingsManager.getValue("pref_camera_picturesize_key"));
        String str = TAG;
        if (i == 0) {
            Log.d(str, "mJpegFileSizeEstimation 0");
            return;
        }
        this.mJpegFileSizeEstimation = ((parsePictureSize.getWidth() * parsePictureSize.getHeight()) * 3) / i;
        StringBuilder sb = new StringBuilder();
        sb.append("mJpegFileSizeEstimation ");
        sb.append(this.mJpegFileSizeEstimation);
        Log.d(str, sb.toString());
    }

    private boolean isLongshotNeedCancel() {
        if (PersistUtil.getSkipMemoryCheck()) {
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
        long j = (rawInfo[1] + rawInfo[3]) * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
        boolean z = 1 == PersistUtil.getRAMFeatureDebug() && maxMemory <= 41943040;
        if (j <= this.SECONDARY_SERVER_MEM || z) {
            StringBuilder sb = new StringBuilder();
            sb.append("cancel longshot: free=");
            sb.append(rawInfo[1] * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID);
            sb.append(" cached=");
            sb.append(rawInfo[3] * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID);
            sb.append(" threshold=");
            sb.append(this.SECONDARY_SERVER_MEM);
            Log.e(str, sb.toString());
            RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.msg_cancel_longshot_for_limited_memory, 0).show();
            return true;
        } else if (!this.mIsRecordingVideo) {
            return false;
        } else {
            Log.e(str, " cancel longshot:not supported when recording");
            return true;
        }
    }

    private boolean isFlashOff(int i) {
        if (!this.mSettingsManager.isFlashSupported(i)) {
            return true;
        }
        return this.mSettingsManager.getValue(SettingsManager.KEY_FLASH_MODE).equals("off");
    }

    private boolean isFlashOn(int i) {
        if (!this.mSettingsManager.isFlashSupported(i)) {
            return false;
        }
        return this.mSettingsManager.getValue(SettingsManager.KEY_FLASH_MODE).equals(RecordLocationPreference.VALUE_ON);
    }

    /* access modifiers changed from: private */
    public void initializePreviewConfiguration(int i) {
        this.mPreviewRequestBuilder[i].set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
        applyFlash(this.mPreviewRequestBuilder[i], i);
        applyCommonSettings(this.mPreviewRequestBuilder[i], i);
    }

    public float getZoomValue() {
        return this.mZoomValue;
    }

    public Rect cropRegionForZoom(int i) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("cropRegionForZoom ");
            sb.append(i);
            Log.d(TAG, sb.toString());
        }
        Rect sensorActiveArraySize = this.mSettingsManager.getSensorActiveArraySize(i);
        Rect rect = new Rect();
        int width = sensorActiveArraySize.width() / 2;
        int height = sensorActiveArraySize.height() / 2;
        int width2 = (int) (((float) sensorActiveArraySize.width()) / (this.mZoomValue * 2.0f));
        int height2 = (int) (((float) sensorActiveArraySize.height()) / (this.mZoomValue * 2.0f));
        rect.set(width - width2, height - height2, width + width2, height + height2);
        if (this.mZoomValue == 1.0f) {
            this.mOriginalCropRegion[i] = rect;
        }
        Rect[] rectArr = this.mCropRegion;
        rectArr[i] = rect;
        return rectArr[i];
    }

    private void applyZoom(Builder builder, int i) {
        builder.set(CaptureRequest.SCALER_CROP_REGION, cropRegionForZoom(i));
    }

    private void applyInstantAEC(Builder builder) {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_INSTANT_AEC);
        if (value != null && !value.equals("0")) {
            builder.set(INSTANT_AEC_MODE, Integer.valueOf(Integer.parseInt(value)));
        }
    }

    private void applySaturationLevel(Builder builder) {
        this.mSaturationRequest = builder;
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SATURATION_LEVEL);
        if (value != null) {
            builder.set(SATURATION, Integer.valueOf(Integer.parseInt(value)));
        }
    }

    private void applyAntiBandingLevel(Builder builder) {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_ANTI_BANDING_LEVEL);
        if (value != null) {
            builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, Integer.valueOf(Integer.parseInt(value)));
        }
    }

    private void applyDenoise(Builder builder) {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_DENOISE);
        if (value != null && value.equals("denoise-off")) {
            builder.set(CaptureRequest.NOISE_REDUCTION_MODE, Integer.valueOf(0));
        }
    }

    private void applyHistogram(Builder builder) {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_HISTOGRAM);
        if (value == null || !value.equals("enable")) {
            this.mHiston = false;
            updateGraghViewVisibility(8);
            return;
        }
        builder.set(histMode, Byte.valueOf(1));
        this.mHiston = true;
        updateGraghViewVisibility(0);
        updateGraghView();
    }

    private void applySharpnessControlModes(Builder builder) {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SHARPNESS_CONTROL_MODE);
        this.mSharpnessRequest = builder;
        if (value != null) {
            try {
                builder.set(sharpness_control, Integer.valueOf(Integer.parseInt(value)));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void applyAfModes(Builder builder) {
        if (getDevAfMode() != -1) {
            builder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(getDevAfMode()));
        }
    }

    private int getDevAfMode() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_AF_MODE);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return -1;
    }

    private void applyExposureMeteringModes(Builder builder) {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_EXPOSURE_METERING_MODE);
        if (value != null) {
            builder.set(exposure_metering, Integer.valueOf(Integer.parseInt(value)));
        }
    }

    private void enableBokeh(Builder builder) {
        this.mBokehRequestBuilder = builder;
        boolean z = this.mUI.isBokehMode;
        String str = SettingsManager.KEY_BOKEH_MODE;
        if (z) {
            this.mSettingsManager.setValue(str, RecordLocationPreference.VALUE_ON);
        } else {
            this.mSettingsManager.setValue(str, "off");
        }
        try {
            String str2 = "0";
            if (isBackCamera()) {
                if (this.mUI.isBokehMode) {
                    builder.set(bokeh_enable, Boolean.valueOf(true));
                    builder.set(bokeh_blur_level, Integer.valueOf(this.bokehBlurDegree));
                    return;
                }
                builder.set(bokeh_enable, Boolean.valueOf(false));
                builder.set(bokeh_blur_level, Integer.valueOf(Integer.parseInt(str2)));
            } else if (this.mUI.isBokehMode) {
                builder.set(singlebokeh_enable, Boolean.valueOf(true));
                int parseInt = Integer.parseInt(str2);
                String value = this.mSettingsManager.getValue(SettingsManager.KEY_BOKEH_BLUR_DEGREE);
                if (value != null) {
                    parseInt = Integer.parseInt(value);
                }
                if (parseInt != Integer.parseInt(str2)) {
                    builder.set(single_bokeh_blur_level, Integer.valueOf(parseInt / 10));
                } else {
                    builder.set(single_bokeh_blur_level, Integer.valueOf(this.bokehBlurDegree));
                }
                this.isOpenBokehMode = true;
            } else {
                builder.set(singlebokeh_enable, Boolean.valueOf(false));
                builder.set(single_bokeh_blur_level, Integer.valueOf(Integer.parseInt(str2)));
                this.isOpenBokehMode = false;
            }
        } catch (IllegalArgumentException unused) {
        }
    }

    public void openBokehMode() {
        String str = "0";
        try {
            if (isBackCamera()) {
                this.isOpenBokehMode = true;
                return;
            }
            this.mBokehRequestBuilder.set(singlebokeh_enable, Boolean.valueOf(true));
            int parseInt = Integer.parseInt(str);
            String value = this.mSettingsManager.getValue(SettingsManager.KEY_BOKEH_BLUR_DEGREE);
            if (value != null) {
                parseInt = Integer.parseInt(value);
            }
            if (parseInt != Integer.parseInt(str)) {
                this.mBokehRequestBuilder.set(single_bokeh_blur_level, Integer.valueOf(parseInt / 10));
            } else {
                this.mBokehRequestBuilder.set(single_bokeh_blur_level, Integer.valueOf(this.bokehBlurDegree));
            }
            this.mCaptureSession[getMainCameraId()].setRepeatingRequest(this.mBokehRequestBuilder.build(), this.mCaptureCallback, this.mCameraHandler);
            this.isOpenBokehMode = true;
        } catch (Exception unused) {
            StringBuilder sb = new StringBuilder();
            sb.append("can not find vendor tag : org.codeaurora.qcamera3.bokeh: ");
            sb.append(single_bokeh_blur_level);
            Log.e(TAG, sb.toString());
        }
    }

    public void closeBokehMode() {
        try {
            if (isBackCamera()) {
                this.isOpenBokehMode = false;
                return;
            }
            this.mBokehRequestBuilder.set(singlebokeh_enable, Boolean.valueOf(false));
            this.mBokehRequestBuilder.set(single_bokeh_blur_level, Integer.valueOf(Integer.parseInt("0")));
            this.mCaptureSession[getMainCameraId()].setRepeatingRequest(this.mBokehRequestBuilder.build(), this.mCaptureCallback, this.mCameraHandler);
            this.isOpenBokehMode = false;
        } catch (Exception unused) {
            Log.e(TAG, "can not find vendor tag : org.codeaurora.qcamera3.bokeh");
        }
    }

    private void enableMakeUp(Builder builder) {
        this.mMakeUpRequestBuilder = builder;
        boolean z = this.mUI.isMakeUp;
        String str = SettingsManager.KEY_MAKEUP_MODE;
        if (z) {
            this.mSettingsManager.setValue(str, RecordLocationPreference.VALUE_ON);
        } else {
            this.mSettingsManager.setValue(str, "off");
        }
        boolean z2 = this.mUI.isMakeUp;
        String str2 = "can not find vendor tag : org.codeaurora.qcamera3.makeup.enable";
        String str3 = TAG;
        if (z2) {
            try {
                builder.set(makeup_enable, Boolean.valueOf(true));
                builder.set(makeup_level, Integer.valueOf(Integer.parseInt(this.mSettingsManager.getValue(SettingsManager.KEY_MAKEUP))));
                String value = this.mSettingsManager.getValue(SettingsManager.KEY_MAKEUP_CLEAN_DEGREE);
                String value2 = this.mSettingsManager.getValue(SettingsManager.KEY_MAKEUP_WHITEN_DEGREE);
                if (!(value == null || value2 == null)) {
                    this.mMakeUpRequestBuilder.set(makeup_whiten_level, Integer.valueOf(Integer.parseInt(value2)));
                    this.mMakeUpRequestBuilder.set(makeup_clean_level, Integer.valueOf(Integer.parseInt(value)));
                }
                this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (CaptureModule.this.mUI.isBokehMode) {
                            CaptureModule.this.mUI.setBokehTipVisibility(true);
                            CaptureModule.this.mUI.setBokehTipTextTitle(C0905R.string.single_beauty_bokeh);
                            return;
                        }
                        CaptureModule.this.mUI.setBokehTipVisibility(true);
                        CaptureModule.this.mUI.setBokehTipTextTitle(C0905R.string.beauty_mode);
                    }
                });
                this.isOpenMakeUpMode = true;
            } catch (IllegalArgumentException unused) {
                Log.e(str3, str2);
            }
        } else if (!z2 && this.isOpenMakeUpMode) {
            try {
                builder.set(makeup_enable, Boolean.valueOf(false));
                builder.set(makeup_level, Integer.valueOf(Integer.parseInt("0")));
                this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (!CaptureModule.this.mUI.isBokehMode) {
                            CaptureModule.this.mUI.setBokehTipVisibility(false);
                        } else if (!CaptureModule.this.isBackCamera()) {
                            CaptureModule.this.mUI.setBokehTipVisibility(true);
                            CaptureModule.this.mUI.setBokehTipTextTitle(C0905R.string.single_camera_bokeh);
                        }
                    }
                });
                this.isOpenMakeUpMode = false;
            } catch (Exception unused2) {
                Log.e(str3, str2);
            }
        }
    }

    public void setBokehBlurDegree(int i) {
        String str = TAG;
        if (checkSessionAndBuilder(this.mCaptureSession[getMainCameraId()], this.mBokehRequestBuilder)) {
            try {
                if (isBackCamera()) {
                    this.mBokehRequestBuilder.set(bokeh_blur_level, Integer.valueOf(i));
                    this.mCaptureSession[getMainCameraId()].setRepeatingRequest(this.mBokehRequestBuilder.build(), this.mCaptureCallback, this.mCameraHandler);
                } else {
                    this.bokehBlurDegree = i;
                    SettingsManager settingsManager = this.mSettingsManager;
                    String str2 = SettingsManager.KEY_BOKEH_BLUR_DEGREE;
                    StringBuilder sb = new StringBuilder();
                    sb.append(i * 10);
                    sb.append(BuildConfig.FLAVOR);
                    settingsManager.setValue(str2, sb.toString());
                    this.mBokehRequestBuilder.set(single_bokeh_blur_level, Integer.valueOf(i));
                    this.mCaptureSession[getMainCameraId()].setRepeatingRequest(this.mBokehRequestBuilder.build(), this.mCaptureCallback, this.mCameraHandler);
                }
            } catch (IllegalArgumentException unused) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("can not find vendor tag : ");
                sb2.append(bokeh_blur_level);
                Log.e(str, sb2.toString());
            } catch (CameraAccessException unused2) {
                Log.e(str, "Camera Access Exception in setBokehBlurDegree");
            }
        }
    }

    public void setMakeUpDegree(int i) {
        String str = TAG;
        try {
            this.mMakeUpRequestBuilder = this.mPreviewRequestBuilder[getMainCameraId()];
            if (this.mMakeUpRequestBuilder != null && this.mCaptureSession[getMainCameraId()] != null) {
                applyFlash(this.mMakeUpRequestBuilder, this.mSettingsManager.getValue(SettingsManager.KEY_FLASH_MODE));
                this.mMakeUpRequestBuilder.set(makeup_level, Integer.valueOf(i));
                this.mCaptureSession[getMainCameraId()].setRepeatingRequest(this.mMakeUpRequestBuilder.build(), this.mCaptureCallback, this.mCameraHandler);
            }
        } catch (IllegalArgumentException unused) {
            StringBuilder sb = new StringBuilder();
            sb.append("can not find vendor tag : ");
            sb.append(bokeh_blur_level);
            Log.e(str, sb.toString());
        } catch (CameraAccessException unused2) {
            Log.e(str, "Camera Access Exception in setBokehBlurDegree");
        }
    }

    public void setMakeUpWhitenDegree(int i) {
        String str = TAG;
        try {
            if (this.mMakeUpRequestBuilder != null && this.mCaptureSession[getMainCameraId()] != null) {
                this.mMakeUpRequestBuilder.set(makeup_whiten_level, Integer.valueOf(i));
                SettingsManager settingsManager = this.mSettingsManager;
                String str2 = SettingsManager.KEY_MAKEUP_WHITEN_DEGREE;
                StringBuilder sb = new StringBuilder();
                sb.append(i);
                sb.append(BuildConfig.FLAVOR);
                settingsManager.setValue(str2, sb.toString());
                this.mCaptureSession[getMainCameraId()].setRepeatingRequest(this.mMakeUpRequestBuilder.build(), this.mCaptureCallback, this.mCameraHandler);
            }
        } catch (IllegalArgumentException unused) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("can not find vendor tag : ");
            sb2.append(bokeh_blur_level);
            Log.e(str, sb2.toString());
        } catch (CameraAccessException unused2) {
            Log.e(str, "Camera Access Exception in setBokehBlurDegree");
        }
    }

    public void setMakeUpCleanDegree(int i) {
        String str = TAG;
        try {
            if (this.mMakeUpRequestBuilder != null && this.mCaptureSession[getMainCameraId()] != null) {
                this.mMakeUpRequestBuilder.set(makeup_clean_level, Integer.valueOf(i));
                SettingsManager settingsManager = this.mSettingsManager;
                String str2 = SettingsManager.KEY_MAKEUP_CLEAN_DEGREE;
                StringBuilder sb = new StringBuilder();
                sb.append(i);
                sb.append(BuildConfig.FLAVOR);
                settingsManager.setValue(str2, sb.toString());
                this.mCaptureSession[getMainCameraId()].setRepeatingRequest(this.mMakeUpRequestBuilder.build(), this.mCaptureCallback, this.mCameraHandler);
            }
        } catch (IllegalArgumentException unused) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("can not find vendor tag : ");
            sb2.append(bokeh_blur_level);
            Log.e(str, sb2.toString());
        } catch (CameraAccessException unused2) {
            Log.e(str, "Camera Access Exception in setBokehBlurDegree");
        }
    }

    private void updateGraghViewVisibility(final int i) {
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (CaptureModule.this.mGraphViewR != null) {
                    CaptureModule.this.mGraphViewR.setVisibility(i);
                }
                if (CaptureModule.this.mGraphViewGR != null) {
                    CaptureModule.this.mGraphViewGR.setVisibility(i);
                }
                if (CaptureModule.this.mGraphViewGB != null) {
                    CaptureModule.this.mGraphViewGB.setVisibility(i);
                }
                if (CaptureModule.this.mGraphViewB != null) {
                    CaptureModule.this.mGraphViewB.setVisibility(i);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateGraghView() {
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (CaptureModule.this.mGraphViewR != null) {
                    CaptureModule.this.mGraphViewR.PreviewChanged();
                }
                if (CaptureModule.this.mGraphViewGR != null) {
                    CaptureModule.this.mGraphViewGR.PreviewChanged();
                }
                if (CaptureModule.this.mGraphViewGB != null) {
                    CaptureModule.this.mGraphViewGB.PreviewChanged();
                }
                if (CaptureModule.this.mGraphViewB != null) {
                    CaptureModule.this.mGraphViewB.PreviewChanged();
                }
            }
        });
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean applyPreferenceToPreview(int r5, java.lang.String r6, java.lang.String r7) {
        /*
            r4 = this;
            android.hardware.camera2.CameraCaptureSession[] r0 = r4.mCaptureSession
            r0 = r0[r5]
            android.hardware.camera2.CaptureRequest$Builder[] r1 = r4.mPreviewRequestBuilder
            r1 = r1[r5]
            boolean r0 = r4.checkSessionAndBuilder(r0, r1)
            r1 = 0
            if (r0 != 0) goto L_0x0010
            return r1
        L_0x0010:
            r0 = -1
            int r2 = r6.hashCode()
            r3 = 1
            switch(r2) {
                case -1628809851: goto L_0x0060;
                case -345946486: goto L_0x0056;
                case -239431062: goto L_0x004c;
                case 366612902: goto L_0x0042;
                case 459028727: goto L_0x0038;
                case 883522161: goto L_0x002e;
                case 1202266657: goto L_0x0024;
                case 1806490085: goto L_0x001a;
                default: goto L_0x0019;
            }
        L_0x0019:
            goto L_0x006a
        L_0x001a:
            java.lang.String r2 = "pref_camera_exposure_key"
            boolean r6 = r6.equals(r2)
            if (r6 == 0) goto L_0x006a
            r6 = 3
            goto L_0x006b
        L_0x0024:
            java.lang.String r2 = "pref_camera2_scenemode_key"
            boolean r6 = r6.equals(r2)
            if (r6 == 0) goto L_0x006a
            r6 = 2
            goto L_0x006b
        L_0x002e:
            java.lang.String r2 = "pref_camera_whitebalance_key"
            boolean r6 = r6.equals(r2)
            if (r6 == 0) goto L_0x006a
            r6 = r1
            goto L_0x006b
        L_0x0038:
            java.lang.String r2 = "pref_camera2_iso_key"
            boolean r6 = r6.equals(r2)
            if (r6 == 0) goto L_0x006a
            r6 = 4
            goto L_0x006b
        L_0x0042:
            java.lang.String r2 = "pref_camera2_coloreffect_key"
            boolean r6 = r6.equals(r2)
            if (r6 == 0) goto L_0x006a
            r6 = r3
            goto L_0x006b
        L_0x004c:
            java.lang.String r2 = "pref_camera_facedetection_key"
            boolean r6 = r6.equals(r2)
            if (r6 == 0) goto L_0x006a
            r6 = 5
            goto L_0x006b
        L_0x0056:
            java.lang.String r2 = "pref_camera2_focus_distance_key"
            boolean r6 = r6.equals(r2)
            if (r6 == 0) goto L_0x006a
            r6 = 6
            goto L_0x006b
        L_0x0060:
            java.lang.String r2 = "pref_camera2_flashmode_key"
            boolean r6 = r6.equals(r2)
            if (r6 == 0) goto L_0x006a
            r6 = 7
            goto L_0x006b
        L_0x006a:
            r6 = r0
        L_0x006b:
            java.lang.String r0 = "SnapCam_CaptureModule"
            switch(r6) {
                case 0: goto L_0x0125;
                case 1: goto L_0x00d2;
                case 2: goto L_0x00ca;
                case 3: goto L_0x00c2;
                case 4: goto L_0x00ba;
                case 5: goto L_0x00b2;
                case 6: goto L_0x007c;
                case 7: goto L_0x0073;
                default: goto L_0x0070;
            }
        L_0x0070:
            r3 = r1
            goto L_0x012c
        L_0x0073:
            android.hardware.camera2.CaptureRequest$Builder[] r6 = r4.mPreviewRequestBuilder
            r6 = r6[r5]
            r4.applyFlash(r6, r5)
            goto L_0x012c
        L_0x007c:
            com.android.camera.CaptureUI r6 = r4.mUI
            int r6 = r6.getCurrentProMode()
            if (r6 != r3) goto L_0x00a1
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r1 = "applyPreferenceToPreview : "
            r6.append(r1)
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            android.util.Log.d(r0, r6)
            android.hardware.camera2.CaptureRequest$Builder[] r6 = r4.mPreviewRequestBuilder
            r5 = r6[r5]
            r4.applyFocusDistance(r5, r7)
            goto L_0x012c
        L_0x00a1:
            android.hardware.camera2.CaptureRequest$Builder[] r6 = r4.mPreviewRequestBuilder
            r5 = r6[r5]
            android.hardware.camera2.CaptureRequest$Key r6 = android.hardware.camera2.CaptureRequest.CONTROL_AF_MODE
            int r4 = r4.mControlAFMode
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r5.set(r6, r4)
            goto L_0x012c
        L_0x00b2:
            android.hardware.camera2.CaptureRequest$Builder[] r6 = r4.mPreviewRequestBuilder
            r5 = r6[r5]
            r4.applyFaceDetection(r5)
            goto L_0x012c
        L_0x00ba:
            android.hardware.camera2.CaptureRequest$Builder[] r6 = r4.mPreviewRequestBuilder
            r5 = r6[r5]
            r4.applyIso(r5)
            goto L_0x012c
        L_0x00c2:
            android.hardware.camera2.CaptureRequest$Builder[] r6 = r4.mPreviewRequestBuilder
            r5 = r6[r5]
            r4.applyExposure(r5)
            goto L_0x012c
        L_0x00ca:
            android.hardware.camera2.CaptureRequest$Builder[] r6 = r4.mPreviewRequestBuilder
            r5 = r6[r5]
            r4.applySceneMode(r5)
            goto L_0x012c
        L_0x00d2:
            android.hardware.camera2.CaptureRequest$Builder[] r6 = r4.mPreviewRequestBuilder
            r5 = r6[r5]
            r4.applyColorEffect(r5)
            com.android.camera.CameraActivity r5 = r4.mActivity
            com.android.camera.CaptureModule$31 r6 = new com.android.camera.CaptureModule$31
            r6.<init>()
            r5.runOnUiThread(r6)
            android.hardware.camera2.CaptureRequest$Builder r5 = r4.mMakeUpRequestBuilder     // Catch:{ Exception -> 0x011f }
            android.hardware.camera2.CaptureRequest$Key<java.lang.Boolean> r6 = makeup_enable     // Catch:{ Exception -> 0x011f }
            java.lang.Boolean r7 = java.lang.Boolean.valueOf(r1)     // Catch:{ Exception -> 0x011f }
            r5.set(r6, r7)     // Catch:{ Exception -> 0x011f }
            android.hardware.camera2.CaptureRequest$Builder r5 = r4.mMakeUpRequestBuilder     // Catch:{ Exception -> 0x011f }
            android.hardware.camera2.CaptureRequest$Key<java.lang.Integer> r6 = makeup_level     // Catch:{ Exception -> 0x011f }
            java.lang.String r7 = "0"
            int r7 = java.lang.Integer.parseInt(r7)     // Catch:{ Exception -> 0x011f }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ Exception -> 0x011f }
            r5.set(r6, r7)     // Catch:{ Exception -> 0x011f }
            com.android.camera.CameraActivity r5 = r4.mActivity     // Catch:{ Exception -> 0x011f }
            com.android.camera.CaptureModule$32 r6 = new com.android.camera.CaptureModule$32     // Catch:{ Exception -> 0x011f }
            r6.<init>()     // Catch:{ Exception -> 0x011f }
            r5.runOnUiThread(r6)     // Catch:{ Exception -> 0x011f }
            android.hardware.camera2.CameraCaptureSession[] r5 = r4.mCaptureSession     // Catch:{ Exception -> 0x011f }
            int r6 = r4.getMainCameraId()     // Catch:{ Exception -> 0x011f }
            r5 = r5[r6]     // Catch:{ Exception -> 0x011f }
            android.hardware.camera2.CaptureRequest$Builder r6 = r4.mMakeUpRequestBuilder     // Catch:{ Exception -> 0x011f }
            android.hardware.camera2.CaptureRequest r6 = r6.build()     // Catch:{ Exception -> 0x011f }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r7 = r4.mCaptureCallback     // Catch:{ Exception -> 0x011f }
            android.os.Handler r4 = r4.mCameraHandler     // Catch:{ Exception -> 0x011f }
            r5.setRepeatingRequest(r6, r7, r4)     // Catch:{ Exception -> 0x011f }
            goto L_0x012c
        L_0x011f:
            java.lang.String r4 = "can not find vendor tag : org.codeaurora.qcamera3.makeup.enable"
            android.util.Log.e(r0, r4)
            goto L_0x012c
        L_0x0125:
            android.hardware.camera2.CaptureRequest$Builder[] r6 = r4.mPreviewRequestBuilder
            r5 = r6[r5]
            r4.applyWhiteBalance(r5)
        L_0x012c:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CaptureModule.applyPreferenceToPreview(int, java.lang.String, java.lang.String):boolean");
    }

    private void applyZoomAndUpdate(int i) {
        if (checkSessionAndBuilder(this.mCaptureSession[i], this.mPreviewRequestBuilder[i])) {
            applyZoom(this.mPreviewRequestBuilder[i], i);
            try {
                if (i != MONO_ID || canStartMonoPreview()) {
                    CameraCaptureSession cameraCaptureSession = this.mCaptureSession[i];
                    if (cameraCaptureSession instanceof CameraConstrainedHighSpeedCaptureSession) {
                        ((CameraConstrainedHighSpeedCaptureSession) cameraCaptureSession).setRepeatingBurst(((CameraConstrainedHighSpeedCaptureSession) cameraCaptureSession).createHighSpeedRequestList(this.mPreviewRequestBuilder[i].build()), this.mCaptureCallback, this.mCameraHandler);
                    } else if (!this.mPostProcessor.isZSLEnabled() || getCameraMode() == 0 || this.mIsRecordingVideo) {
                        this.mCaptureSession[i].setRepeatingRequest(this.mPreviewRequestBuilder[i].build(), this.mCaptureCallback, this.mCameraHandler);
                    } else {
                        setRepeatingBurstForZSL(i);
                    }
                }
                this.mCaptureSession[i].capture(this.mPreviewRequestBuilder[i].build(), this.mCaptureCallback, this.mCameraHandler);
            } catch (CameraAccessException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void applyJpegQuality(Builder builder) {
        String value = this.mSettingsManager.getValue("pref_camera_jpegquality_key");
        if (value != null) {
            builder.set(CaptureRequest.JPEG_QUALITY, Byte.valueOf((byte) getQualityNumber(value)));
        }
    }

    private void applyAFRegions(Builder builder, int i) {
        if (this.mControlAFMode == 1) {
            builder.set(CaptureRequest.CONTROL_AF_REGIONS, this.mAFRegions[i]);
        } else {
            builder.set(CaptureRequest.CONTROL_AF_REGIONS, ZERO_WEIGHT_3A_REGION);
        }
    }

    private void applyAERegions(Builder builder, int i) {
        if (this.mControlAFMode == 1) {
            builder.set(CaptureRequest.CONTROL_AE_REGIONS, this.mAERegions[i]);
        } else {
            builder.set(CaptureRequest.CONTROL_AE_REGIONS, ZERO_WEIGHT_3A_REGION);
        }
    }

    private void applySceneMode(Builder builder) {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
        String value2 = this.mSettingsManager.getValue(SettingsManager.KEY_AUTO_HDR);
        String value3 = this.mSettingsManager.getValue("pref_camera_facedetection_key");
        if (value != null) {
            int parseInt = Integer.parseInt(value);
            if (parseInt == 5) {
                this.mIsNightMode = true;
                builder.set(night_enable, Boolean.valueOf(true));
                return;
            }
            if (this.mIsNightMode) {
                this.mIsNightMode = false;
                builder.set(night_enable, Boolean.valueOf(false));
            }
            if (value2 != null && "enable".equals(value2) && "0".equals(value)) {
                if (this.mSettingsManager.isHdrScene(getMainCameraId())) {
                    builder.set(CaptureRequest.CONTROL_SCENE_MODE, Integer.valueOf(18));
                    builder.set(CaptureRequest.CONTROL_MODE, Integer.valueOf(2));
                }
            } else if (getPostProcFilterId(parseInt) != 0 || this.mCaptureHDRTestEnable) {
                builder.set(CaptureRequest.CONTROL_MODE, Integer.valueOf(1));
            } else {
                if (parseInt != 0 && parseInt != 100 && parseInt != 109 && parseInt != 110) {
                    if (parseInt == 7) {
                        parseInt = 4;
                    }
                    builder.set(CaptureRequest.CONTROL_SCENE_MODE, Integer.valueOf(parseInt));
                    builder.set(CaptureRequest.CONTROL_MODE, Integer.valueOf(2));
                } else if (parseInt == 110) {
                    setSceneModeForBokeh(builder);
                } else if (value3 == null || !value3.equals(RecordLocationPreference.VALUE_ON)) {
                    builder.set(CaptureRequest.CONTROL_MODE, Integer.valueOf(1));
                }
            }
        }
    }

    private void setSceneModeForBokeh(Builder builder) {
        String value = this.mSettingsManager.getValue("pref_camera_facedetection_key");
        Integer valueOf = Integer.valueOf(1);
        if (value == null || !value.equals(RecordLocationPreference.VALUE_ON)) {
            builder.set(CaptureRequest.CONTROL_MODE, valueOf);
            return;
        }
        builder.set(CaptureRequest.CONTROL_SCENE_MODE, valueOf);
        builder.set(CaptureRequest.CONTROL_MODE, Integer.valueOf(2));
    }

    private void applyExposure(Builder builder) {
        String value = this.mSettingsManager.getValue("pref_camera_exposure_key");
        if (value != null) {
            builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, Integer.valueOf(Integer.parseInt(value)));
        }
    }

    private void applyIso(Builder builder) {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_ISO);
        if (!applyManualIsoExposure(builder) && value != null) {
            String value2 = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
            if (!(value2 != null && Integer.parseInt(value2) == 109) || value.equals("auto")) {
                if (builder.get(CaptureRequest.SENSOR_EXPOSURE_TIME) == null) {
                    builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf(this.mIsoExposureTime));
                }
                if (builder.get(CaptureRequest.SENSOR_SENSITIVITY) == null) {
                    builder.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(this.mIsoSensitivity));
                }
                VendorTagUtil.setIsoExpPriority(builder, Long.valueOf(0));
            } else {
                long intValue = (long) ((Integer) SettingsManager.KEY_ISO_INDEX.get(value)).intValue();
                VendorTagUtil.setIsoExpPrioritySelectPriority(builder, Integer.valueOf(0));
                VendorTagUtil.setIsoExpPriority(builder, Long.valueOf(intValue));
                if (builder.get(CaptureRequest.SENSOR_EXPOSURE_TIME) != null) {
                    this.mIsoExposureTime = ((Long) builder.get(CaptureRequest.SENSOR_EXPOSURE_TIME)).longValue();
                }
                if (builder.get(CaptureRequest.SENSOR_SENSITIVITY) != null) {
                    this.mIsoSensitivity = ((Integer) builder.get(CaptureRequest.SENSOR_SENSITIVITY)).intValue();
                }
                builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, null);
                builder.set(CaptureRequest.SENSOR_SENSITIVITY, null);
            }
        }
    }

    private boolean applyManualIsoExposure(Builder builder) {
        boolean z;
        long j;
        long j2;
        Builder builder2 = builder;
        CameraActivity cameraActivity = this.mActivity;
        String localSharedPreferencesName = ComboPreferences.getLocalSharedPreferencesName(cameraActivity, getMainCameraId());
        Integer valueOf = Integer.valueOf(0);
        SharedPreferences sharedPreferences = cameraActivity.getSharedPreferences(localSharedPreferencesName, 0);
        String string = this.mActivity.getString(C0905R.string.pref_camera_manual_exp_value_ISO_priority);
        String string2 = this.mActivity.getString(C0905R.string.pref_camera_manual_exp_value_exptime_priority);
        String string3 = this.mActivity.getString(C0905R.string.pref_camera_manual_exp_value_user_setting);
        String string4 = this.mActivity.getString(C0905R.string.pref_camera_manual_exp_value_gains_priority);
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_MANUAL_EXPOSURE);
        if (value == null) {
            return false;
        }
        boolean equals = value.equals(string);
        String str = SettingsManager.MAUNAL_ABSOLUTE_ISO_VALUE;
        String str2 = SettingsManager.SCENE_MODE_DUAL_STRING;
        String str3 = SettingsManager.KEY_MANUAL_ISO_VALUE;
        String str4 = TAG;
        if (equals) {
            long parseLong = Long.parseLong(sharedPreferences.getString(str3, str2));
            VendorTagUtil.setIsoExpPrioritySelectPriority(builder2, valueOf);
            VendorTagUtil.setIsoExpPriority(builder2, Long.valueOf((long) ((Integer) SettingsManager.KEY_ISO_INDEX.get(str)).intValue()));
            VendorTagUtil.setUseIsoValues(builder2, parseLong);
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("manual ISO value :");
                sb.append(parseLong);
                Log.v(str4, sb.toString());
            }
            if (builder2.get(CaptureRequest.SENSOR_EXPOSURE_TIME) != null) {
                this.mIsoExposureTime = ((Long) builder2.get(CaptureRequest.SENSOR_EXPOSURE_TIME)).longValue();
            }
            if (builder2.get(CaptureRequest.SENSOR_SENSITIVITY) != null) {
                this.mIsoSensitivity = ((Integer) builder2.get(CaptureRequest.SENSOR_SENSITIVITY)).intValue();
            }
            builder2.set(CaptureRequest.SENSOR_EXPOSURE_TIME, null);
            builder2.set(CaptureRequest.SENSOR_SENSITIVITY, null);
            z = true;
        } else {
            boolean equals2 = value.equals(string2);
            String str5 = " is invalid";
            String str6 = "Input expTime ";
            String str7 = "0";
            String str8 = SettingsManager.KEY_MANUAL_EXPOSURE_VALUE;
            if (equals2) {
                String string5 = sharedPreferences.getString(str8, str7);
                try {
                    j2 = Long.parseLong(string5);
                } catch (NumberFormatException unused) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(str6);
                    sb2.append(string5);
                    sb2.append(str5);
                    Log.w(str4, sb2.toString());
                    j2 = Long.parseLong(string5);
                }
                if (DEBUG) {
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("manual Exposure value :");
                    sb3.append(j2);
                    Log.v(str4, sb3.toString());
                }
                z = true;
                VendorTagUtil.setIsoExpPrioritySelectPriority(builder2, Integer.valueOf(1));
                VendorTagUtil.setIsoExpPriority(builder2, Long.valueOf(j2));
                builder2.set(CaptureRequest.SENSOR_SENSITIVITY, null);
            } else {
                z = true;
                if (value.equals(string3)) {
                    this.mSettingsManager.setValue(SettingsManager.KEY_FLASH_MODE, "off");
                    builder2.set(CaptureRequest.CONTROL_AE_MODE, valueOf);
                    builder2.set(CaptureRequest.FLASH_MODE, valueOf);
                    int parseInt = Integer.parseInt(sharedPreferences.getString(str3, str2));
                    String string6 = sharedPreferences.getString(str8, str7);
                    try {
                        j = Long.parseLong(string6);
                    } catch (NumberFormatException unused2) {
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append(str6);
                        sb4.append(string6);
                        sb4.append(str5);
                        Log.w(str4, sb4.toString());
                        j = Long.parseLong(string6);
                    }
                    if (DEBUG) {
                        StringBuilder sb5 = new StringBuilder();
                        sb5.append("manual ISO value : ");
                        sb5.append(parseInt);
                        sb5.append(", Exposure value :");
                        sb5.append(j);
                        Log.v(str4, sb5.toString());
                    }
                    builder2.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf(j));
                    builder2.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(parseInt));
                } else if (value.equals(string4)) {
                    float f = sharedPreferences.getFloat(SettingsManager.KEY_MANUAL_GAINS_VALUE, 1.0f);
                    int[] isoRangeValues = this.mSettingsManager.getIsoRangeValues(getMainCameraId());
                    VendorTagUtil.setIsoExpPrioritySelectPriority(builder2, valueOf);
                    int i = 100;
                    if (isoRangeValues != null) {
                        i = (int) (f * ((float) isoRangeValues[0]));
                    }
                    VendorTagUtil.setIsoExpPriority(builder2, Long.valueOf((long) ((Integer) SettingsManager.KEY_ISO_INDEX.get(str)).intValue()));
                    VendorTagUtil.setUseIsoValues(builder2, (long) i);
                    if (DEBUG) {
                        StringBuilder sb6 = new StringBuilder();
                        sb6.append("manual Gain value :");
                        sb6.append(i);
                        Log.v(str4, sb6.toString());
                    }
                    if (builder2.get(CaptureRequest.SENSOR_EXPOSURE_TIME) != null) {
                        this.mIsoExposureTime = ((Long) builder2.get(CaptureRequest.SENSOR_EXPOSURE_TIME)).longValue();
                    }
                    if (builder2.get(CaptureRequest.SENSOR_SENSITIVITY) != null) {
                        this.mIsoSensitivity = ((Integer) builder2.get(CaptureRequest.SENSOR_SENSITIVITY)).intValue();
                    }
                    builder2.set(CaptureRequest.SENSOR_EXPOSURE_TIME, null);
                    builder2.set(CaptureRequest.SENSOR_SENSITIVITY, null);
                } else {
                    z = false;
                }
            }
        }
        return z;
    }

    private void applyColorEffect(Builder builder) {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_COLOR_EFFECT);
        if (value != null) {
            builder.set(CaptureRequest.CONTROL_EFFECT_MODE, Integer.valueOf(Integer.parseInt(value)));
        }
    }

    private void applyWhiteBalance(Builder builder) {
        String str = "pref_camera_whitebalance_key";
        String value = this.mSettingsManager.getValue(str);
        if (value != null) {
            int parseInt = Integer.parseInt(value);
            if (isHDRMode()) {
                builder.set(CaptureRequest.CONTROL_AWB_MODE, Integer.valueOf(1));
                this.mSettingsManager.setValue(str, "1");
            } else {
                builder.set(CaptureRequest.CONTROL_AWB_MODE, Integer.valueOf(parseInt));
            }
        }
    }

    private void applyFlash(Builder builder, String str) {
        this.mSettingsManager.getValue("pref_camera_redeyereduction_key");
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("applyFlash: ");
            sb.append(str);
            Log.d(TAG, sb.toString());
        }
        char c = 65535;
        int hashCode = str.hashCode();
        if (hashCode != 3551) {
            if (hashCode != 109935) {
                if (hashCode == 3005871 && str.equals("auto")) {
                    c = 1;
                }
            } else if (str.equals("off")) {
                c = 2;
            }
        } else if (str.equals(RecordLocationPreference.VALUE_ON)) {
            c = 0;
        }
        if (c != 0) {
            if (c != 1) {
                if (c == 2) {
                    builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
                    builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
                }
            } else if (this.mLongshotActive) {
                builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
                builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
            } else {
                builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(2));
                builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(1));
            }
        } else if (this.mLongshotActive) {
            builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
            builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
        } else {
            builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(3));
            builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(1));
        }
    }

    private void applyFaceDetection(Builder builder) {
        String value = this.mSettingsManager.getValue("pref_camera_facedetection_key");
        if (value != null && value.equals(RecordLocationPreference.VALUE_ON)) {
            builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, Integer.valueOf(1));
        }
    }

    private void applyFlash(Builder builder, int i) {
        if (this.mSettingsManager.isFlashSupported(i)) {
            applyFlash(builder, this.mSettingsManager.getValue(SettingsManager.KEY_FLASH_MODE));
        } else {
            builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
        }
    }

    /* access modifiers changed from: private */
    public void addPreviewSurface(Builder builder, List<Surface> list, int i) {
        if (isBackCamera() && getCameraMode() == 0 && i == MONO_ID) {
            if (list != null) {
                list.add(this.mUI.getMonoDummySurface());
            }
            builder.addTarget(this.mUI.getMonoDummySurface());
            return;
        }
        for (Surface surface : this.mFrameProcessor.getInputSurfaces()) {
            if (list != null) {
                list.add(surface);
            }
            builder.addTarget(surface);
        }
    }

    private void checkAndPlayRecordSound(int i, boolean z) {
        if (i == getMainCameraId()) {
            String value = this.mSettingsManager.getValue("pref_camera_shuttersound_key");
            if (value != null && value.equals(RecordLocationPreference.VALUE_ON)) {
                Player player = this.mSoundPlayer;
                if (player != null) {
                    player.play(z ? 1 : 2);
                }
            }
        }
    }

    public void checkAndPlayShutterSound(int i) {
        if (i == getMainCameraId()) {
            String value = this.mSettingsManager.getValue("pref_camera_shuttersound_key");
            if (value != null && value.equals(RecordLocationPreference.VALUE_ON)) {
                Player player = this.mSoundPlayer;
                if (player != null) {
                    player.play(3);
                }
            }
        }
    }

    public Surface getPreviewSurfaceForSession(int i) {
        if (!isBackCamera()) {
            return this.mUI.getSurfaceHolder().getSurface();
        }
        if (getCameraMode() == 0 && i == MONO_ID) {
            return this.mUI.getMonoDummySurface();
        }
        return this.mUI.getSurfaceHolder().getSurface();
    }

    public void onQueueStatus(final boolean z) {
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                CaptureModule.this.mUI.enableShutter(!z);
            }
        });
    }

    public void triggerFocusAtPoint(float f, float f2, int i) {
        boolean z = DEBUG;
        String str = TAG;
        if (z) {
            StringBuilder sb = new StringBuilder();
            sb.append("triggerFocusAtPoint ");
            sb.append(f);
            String str2 = " ";
            sb.append(str2);
            sb.append(f2);
            sb.append(str2);
            sb.append(i);
            Log.d(str, sb.toString());
        }
        if (this.mCropRegion[i] == null) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("crop region is null at ");
            sb2.append(i);
            Log.d(str, sb2.toString());
            return;
        }
        Point surfaceViewSize = this.mUI.getSurfaceViewSize();
        int i2 = surfaceViewSize.x;
        int i3 = surfaceViewSize.y;
        float f3 = f;
        float f4 = f2;
        int i4 = i2;
        int i5 = i3;
        int i6 = i;
        this.mAFRegions[i] = afaeRectangle(f3, f4, i4, i5, 1.0f, this.mCropRegion[i], i6);
        this.mAERegions[i] = afaeRectangle(f3, f4, i4, i5, 1.5f, this.mCropRegion[i], i6);
        this.mCameraHandler.removeMessages(1, this.mCameraId[i]);
        autoFocusTrigger(i);
    }

    /* access modifiers changed from: private */
    public void cancelTouchFocus(int i) {
        if (!this.mPaused) {
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("cancelTouchFocus ");
                sb.append(i);
                Log.v(TAG, sb.toString());
            }
            this.mState[i] = 0;
            this.mControlAFMode = 4;
            setAFModeToPreview(i, this.mControlAFMode);
        }
    }

    private MeteringRectangle[] afaeRectangle(float f, float f2, int i, int i2, float f3, Rect rect, int i3) {
        float max = (float) (((int) (((float) (Math.max(i, i2) / 8)) * f3)) / 2);
        RectF rectF = new RectF(f - max, f2 - max, f + max, f2 + max);
        Matrix matrix = new Matrix();
        CameraUtil.prepareMatrix(matrix, !isBackCamera(), this.mDisplayOrientation, i, i2);
        matrix.invert(matrix);
        Matrix matrix2 = new Matrix();
        matrix2.preTranslate(((float) (-this.mOriginalCropRegion[i3].width())) / 2.0f, ((float) (-this.mOriginalCropRegion[i3].height())) / 2.0f);
        matrix2.postScale(2000.0f / ((float) this.mOriginalCropRegion[i3].width()), 2000.0f / ((float) this.mOriginalCropRegion[i3].height()));
        matrix2.invert(matrix2);
        matrix.mapRect(rectF);
        matrix2.mapRect(rectF);
        rectF.left = ((rectF.left * ((float) rect.width())) / ((float) this.mOriginalCropRegion[i3].width())) + ((float) rect.left);
        rectF.top = ((rectF.top * ((float) rect.height())) / ((float) this.mOriginalCropRegion[i3].height())) + ((float) rect.top);
        rectF.right = ((rectF.right * ((float) rect.width())) / ((float) this.mOriginalCropRegion[i3].width())) + ((float) rect.left);
        rectF.bottom = ((rectF.bottom * ((float) rect.height())) / ((float) this.mOriginalCropRegion[i3].height())) + ((float) rect.top);
        Rect rect2 = new Rect((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
        rect2.left = CameraUtil.clamp(rect2.left, rect.left, rect.right);
        rect2.top = CameraUtil.clamp(rect2.top, rect.top, rect.bottom);
        rect2.right = CameraUtil.clamp(rect2.right, rect.left, rect.right);
        rect2.bottom = CameraUtil.clamp(rect2.bottom, rect.top, rect.bottom);
        return new MeteringRectangle[]{new MeteringRectangle(rect2, 1)};
    }

    /* access modifiers changed from: private */
    public void updateFocusStateChange(CaptureResult captureResult) {
        final Integer num = (Integer) captureResult.get(CaptureResult.CONTROL_AF_STATE);
        if (num != null) {
            if (!(num.intValue() == this.mLastResultAFState || this.mFocusStateListener == null)) {
                this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        CaptureModule.this.mFocusStateListener.onFocusStatusUpdate(num.intValue());
                    }
                });
            }
            this.mLastResultAFState = num.intValue();
        }
    }

    /* access modifiers changed from: private */
    public void setDisplayOrientation() {
        this.mDisplayRotation = CameraUtil.getDisplayRotation(this.mActivity);
        this.mDisplayOrientation = CameraUtil.getDisplayOrientationCamera2(this.mDisplayRotation, getMainCameraId());
    }

    /* JADX WARNING: type inference failed for: r9v5 */
    /* JADX WARNING: type inference failed for: r9v12 */
    /* JADX WARNING: type inference failed for: r9v15 */
    /* JADX WARNING: type inference failed for: r9v18 */
    /* JADX WARNING: type inference failed for: r9v21 */
    /* JADX WARNING: type inference failed for: r9v24 */
    /* JADX WARNING: type inference failed for: r9v27 */
    /* JADX WARNING: type inference failed for: r9v30 */
    /* JADX WARNING: type inference failed for: r9v33 */
    /* JADX WARNING: type inference failed for: r9v36 */
    /* JADX WARNING: type inference failed for: r9v39 */
    /* JADX WARNING: type inference failed for: r9v42 */
    /* JADX WARNING: type inference failed for: r9v45 */
    /* JADX WARNING: type inference failed for: r9v48 */
    /* JADX WARNING: type inference failed for: r9v51 */
    /* JADX WARNING: type inference failed for: r9v54 */
    /* JADX WARNING: type inference failed for: r9v57 */
    /* JADX WARNING: type inference failed for: r9v60 */
    /* JADX WARNING: type inference failed for: r9v63 */
    /* JADX WARNING: type inference failed for: r9v66 */
    /* JADX WARNING: type inference failed for: r9v69 */
    /* JADX WARNING: type inference failed for: r9v70 */
    /* JADX WARNING: type inference failed for: r9v71 */
    /* JADX WARNING: type inference failed for: r9v72 */
    /* JADX WARNING: type inference failed for: r9v73 */
    /* JADX WARNING: type inference failed for: r9v74 */
    /* JADX WARNING: type inference failed for: r9v75 */
    /* JADX WARNING: type inference failed for: r9v76 */
    /* JADX WARNING: type inference failed for: r9v77 */
    /* JADX WARNING: type inference failed for: r9v78 */
    /* JADX WARNING: type inference failed for: r9v79 */
    /* JADX WARNING: type inference failed for: r9v80 */
    /* JADX WARNING: type inference failed for: r9v81 */
    /* JADX WARNING: type inference failed for: r9v82 */
    /* JADX WARNING: type inference failed for: r9v83 */
    /* JADX WARNING: type inference failed for: r9v84 */
    /* JADX WARNING: type inference failed for: r9v85 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 17 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onSettingsChanged(java.util.List<com.android.camera.SettingsManager.SettingState> r17) {
        /*
            r16 = this;
            r1 = r16
            boolean r0 = r1.mPaused
            if (r0 == 0) goto L_0x0007
            return
        L_0x0007:
            java.util.Iterator r0 = r17.iterator()
            r2 = 0
            r3 = r2
            r4 = r3
            r5 = r4
            r6 = r5
            r7 = r6
            r8 = r7
        L_0x0012:
            boolean r9 = r0.hasNext()
            if (r9 == 0) goto L_0x01af
            java.lang.Object r9 = r0.next()
            com.android.camera.SettingsManager$SettingState r9 = (com.android.camera.SettingsManager.SettingState) r9
            java.lang.String r10 = r9.key
            com.android.camera.SettingsManager$Values r9 = r9.values
            java.lang.String r11 = r9.overriddenValue
            if (r11 == 0) goto L_0x0027
            goto L_0x0029
        L_0x0027:
            java.lang.String r11 = r9.value
        L_0x0029:
            int r9 = r10.hashCode()
            r12 = 2
            r14 = -1
            r15 = 1
            switch(r9) {
                case -1871644511: goto L_0x00fd;
                case -1636048443: goto L_0x00f2;
                case -1628809851: goto L_0x00e7;
                case -1131618819: goto L_0x00dc;
                case -885525953: goto L_0x00d2;
                case -855141656: goto L_0x00c7;
                case -415152727: goto L_0x00bd;
                case -239431062: goto L_0x00b3;
                case -155899654: goto L_0x00a8;
                case 80247945: goto L_0x009d;
                case 265811584: goto L_0x0092;
                case 926982977: goto L_0x0086;
                case 1202266657: goto L_0x007a;
                case 1807237364: goto L_0x006e;
                case 1827597058: goto L_0x0062;
                case 1832948986: goto L_0x0057;
                case 1900071606: goto L_0x004c;
                case 1934228025: goto L_0x0041;
                case 2040447870: goto L_0x0035;
                default: goto L_0x0033;
            }
        L_0x0033:
            goto L_0x0108
        L_0x0035:
            java.lang.String r9 = "pref_camera2_clearsight_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 8
            goto L_0x0109
        L_0x0041:
            java.lang.String r9 = "pref_camera_jpegquality_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = r15
            goto L_0x0109
        L_0x004c:
            java.lang.String r9 = "pref_camera_video_duration_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = r12
            goto L_0x0109
        L_0x0057:
            java.lang.String r9 = "pref_camera2_mono_only_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 7
            goto L_0x0109
        L_0x0062:
            java.lang.String r9 = "pref_camera2_switch_camera_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 9
            goto L_0x0109
        L_0x006e:
            java.lang.String r9 = "pref_camera2_auto_hdr_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 14
            goto L_0x0109
        L_0x007a:
            java.lang.String r9 = "pref_camera2_scenemode_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 17
            goto L_0x0109
        L_0x0086:
            java.lang.String r9 = "pref_camera2_video_flashmode_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 11
            goto L_0x0109
        L_0x0092:
            java.lang.String r9 = "pref_camera_savepath_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = r2
            goto L_0x0109
        L_0x009d:
            java.lang.String r9 = "pref_camera2_video_time_lapse_frame_interval_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 4
            goto L_0x0109
        L_0x00a8:
            java.lang.String r9 = "pref_camera2_mono_preview_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 10
            goto L_0x0109
        L_0x00b3:
            java.lang.String r9 = "pref_camera_facedetection_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 5
            goto L_0x0109
        L_0x00bd:
            java.lang.String r9 = "pref_camera2_id_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 6
            goto L_0x0109
        L_0x00c7:
            java.lang.String r9 = "pref_camera2_hdr_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 16
            goto L_0x0109
        L_0x00d2:
            java.lang.String r9 = "pref_video_quality_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 3
            goto L_0x0109
        L_0x00dc:
            java.lang.String r9 = "pref_camera2_saveraw_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 15
            goto L_0x0109
        L_0x00e7:
            java.lang.String r9 = "pref_camera2_flashmode_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 12
            goto L_0x0109
        L_0x00f2:
            java.lang.String r9 = "pref_camera2_zsl_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 13
            goto L_0x0109
        L_0x00fd:
            java.lang.String r9 = "pref_camera_picturesize_key"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x0108
            r9 = 18
            goto L_0x0109
        L_0x0108:
            r9 = r14
        L_0x0109:
            switch(r9) {
                case 0: goto L_0x0151;
                case 1: goto L_0x014d;
                case 2: goto L_0x0149;
                case 3: goto L_0x0145;
                case 4: goto L_0x0141;
                case 5: goto L_0x013d;
                case 6: goto L_0x0137;
                case 7: goto L_0x0137;
                case 8: goto L_0x0137;
                case 9: goto L_0x0137;
                case 10: goto L_0x0137;
                case 11: goto L_0x0133;
                case 12: goto L_0x011b;
                case 13: goto L_0x0115;
                case 14: goto L_0x0115;
                case 15: goto L_0x0115;
                case 16: goto L_0x0115;
                case 17: goto L_0x0111;
                case 18: goto L_0x010d;
                default: goto L_0x010c;
            }
        L_0x010c:
            goto L_0x0162
        L_0x010d:
            r16.updatePictureSize()
            goto L_0x0162
        L_0x0111:
            r16.restartAll()
            return
        L_0x0115:
            if (r4 != 0) goto L_0x011a
            r1.restartSession(r2)
        L_0x011a:
            return
        L_0x011b:
            com.android.camera.CameraActivity r9 = r1.mActivity
            r2 = 2131690173(0x7f0f02bd, float:1.9009382E38)
            java.lang.String r2 = r9.getString(r2)
            com.android.camera.SettingsManager r9 = r1.mSettingsManager
            java.lang.String r13 = "pref_camera2_manual_exp_key"
            java.lang.String r9 = r9.getValue(r13)
            boolean r2 = r9.equals(r2)
            if (r2 == 0) goto L_0x0162
            return
        L_0x0133:
            r16.updateVideoFlash()
            return
        L_0x0137:
            if (r4 != 0) goto L_0x013c
            r16.restartAll()
        L_0x013c:
            return
        L_0x013d:
            r16.updateFaceDetection()
            goto L_0x0162
        L_0x0141:
            r16.updateTimeLapseSetting()
            goto L_0x015f
        L_0x0145:
            r16.updateVideoSize()
            goto L_0x015f
        L_0x0149:
            r16.updateMaxVideoDuration()
            goto L_0x015f
        L_0x014d:
            r16.estimateJpegFileSize()
            goto L_0x015f
        L_0x0151:
            java.lang.String r2 = "1"
            boolean r2 = r11.equals(r2)
            com.android.camera.Storage.setSaveSDCard(r2)
            com.android.camera.CameraActivity r2 = r1.mActivity
            r2.updateStorageSpaceAndHint()
        L_0x015f:
            r2 = 0
            goto L_0x0012
        L_0x0162:
            int r2 = SWITCH_ID
            if (r2 == r14) goto L_0x016c
            boolean r2 = r1.applyPreferenceToPreview(r2, r10, r11)
            r8 = r2
            goto L_0x01ac
        L_0x016c:
            boolean r2 = r16.isBackCamera()
            if (r2 == 0) goto L_0x01a4
            int r2 = r16.getCameraMode()
            if (r2 == 0) goto L_0x0196
            if (r2 == r15) goto L_0x018f
            if (r2 == r12) goto L_0x0188
            r9 = 4
            if (r2 == r9) goto L_0x0180
            goto L_0x01ac
        L_0x0180:
            int r2 = BOKEH_ID
            boolean r2 = r1.applyPreferenceToPreview(r2, r10, r11)
            r3 = r3 | r2
            goto L_0x01ac
        L_0x0188:
            int r2 = MONO_ID
            boolean r2 = r1.applyPreferenceToPreview(r2, r10, r11)
            goto L_0x01a2
        L_0x018f:
            r2 = 0
            boolean r9 = r1.applyPreferenceToPreview(r2, r10, r11)
            r6 = r6 | r9
            goto L_0x01ac
        L_0x0196:
            r2 = 0
            boolean r9 = r1.applyPreferenceToPreview(r2, r10, r11)
            r6 = r6 | r9
            int r2 = MONO_ID
            boolean r2 = r1.applyPreferenceToPreview(r2, r10, r11)
        L_0x01a2:
            r7 = r7 | r2
            goto L_0x01ac
        L_0x01a4:
            int r2 = FRONT_ID
            boolean r2 = r1.applyPreferenceToPreview(r2, r10, r11)
            r2 = r2 | r5
            r5 = r2
        L_0x01ac:
            int r4 = r4 + 1
            goto L_0x015f
        L_0x01af:
            if (r3 == 0) goto L_0x01eb
            android.hardware.camera2.CameraCaptureSession[] r0 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            int r2 = BOKEH_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            r0 = r0[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            android.hardware.camera2.CaptureRequest$Builder[] r2 = r1.mPreviewRequestBuilder     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            int r3 = BOKEH_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            r2 = r2[r3]     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            boolean r0 = r1.checkSessionAndBuilder(r0, r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            if (r0 == 0) goto L_0x01eb
            boolean r0 = r16.isBackCamera()     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            if (r0 == 0) goto L_0x01eb
            com.android.camera.CaptureUI r0 = r1.mUI     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            boolean r0 = r0.isBokehMode     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            if (r0 == 0) goto L_0x01eb
            android.hardware.camera2.CameraCaptureSession[] r0 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            int r2 = BOKEH_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            r0 = r0[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            android.hardware.camera2.CaptureRequest$Builder[] r2 = r1.mPreviewRequestBuilder     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            int r3 = BOKEH_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            r2 = r2[r3]     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            android.hardware.camera2.CaptureRequest r2 = r2.build()     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r3 = r1.mCaptureCallback     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            android.os.Handler r4 = r1.mCameraHandler     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            r0.setRepeatingRequest(r2, r3, r4)     // Catch:{ CameraAccessException | IllegalStateException -> 0x01e7 }
            goto L_0x01eb
        L_0x01e7:
            r0 = move-exception
            r0.printStackTrace()
        L_0x01eb:
            if (r6 == 0) goto L_0x025f
            android.hardware.camera2.CameraCaptureSession[] r0 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            r2 = 0
            r0 = r0[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            android.hardware.camera2.CaptureRequest$Builder[] r3 = r1.mPreviewRequestBuilder     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            r3 = r3[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            boolean r0 = r1.checkSessionAndBuilder(r0, r3)     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            if (r0 == 0) goto L_0x025f
            boolean r0 = r1.mIsRecordingVideo     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            if (r0 == 0) goto L_0x022d
            boolean r0 = r1.mHighSpeedCapture     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            if (r0 == 0) goto L_0x022d
            android.hardware.camera2.CameraCaptureSession[] r0 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            r2 = 0
            r0 = r0[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            boolean r0 = r0 instanceof android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            if (r0 == 0) goto L_0x025f
            android.hardware.camera2.CameraCaptureSession[] r0 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            r0 = r0[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession r0 = (android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession) r0     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            android.hardware.camera2.CaptureRequest$Builder[] r3 = r1.mPreviewRequestBuilder     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            r3 = r3[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            android.hardware.camera2.CaptureRequest r3 = r3.build()     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            java.util.List r0 = r0.createHighSpeedRequestList(r3)     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            android.hardware.camera2.CameraCaptureSession[] r3 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            r2 = r3[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession r2 = (android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession) r2     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r3 = r1.mCaptureCallback     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            android.os.Handler r4 = r1.mCameraHandler     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            r2.setRepeatingBurst(r0, r3, r4)     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            goto L_0x025f
        L_0x022d:
            com.android.camera.imageprocessor.PostProcessor r0 = r1.mPostProcessor     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            boolean r0 = r0.isZSLEnabled()     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            if (r0 == 0) goto L_0x0246
            int r0 = r16.getCameraMode()     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            if (r0 == 0) goto L_0x0246
            boolean r0 = r16.isRecordingVideo()     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            if (r0 != 0) goto L_0x0246
            r2 = 0
            r1.setRepeatingBurstForZSL(r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            goto L_0x025f
        L_0x0246:
            android.hardware.camera2.CameraCaptureSession[] r0 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            r2 = 0
            r0 = r0[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            android.hardware.camera2.CaptureRequest$Builder[] r3 = r1.mPreviewRequestBuilder     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            r2 = r3[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            android.hardware.camera2.CaptureRequest r2 = r2.build()     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r3 = r1.mCaptureCallback     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            android.os.Handler r4 = r1.mCameraHandler     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            r0.setRepeatingRequest(r2, r3, r4)     // Catch:{ CameraAccessException | IllegalStateException -> 0x025b }
            goto L_0x025f
        L_0x025b:
            r0 = move-exception
            r0.printStackTrace()
        L_0x025f:
            if (r7 == 0) goto L_0x02ad
            android.hardware.camera2.CameraCaptureSession[] r0 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            int r2 = MONO_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            r0 = r0[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            android.hardware.camera2.CaptureRequest$Builder[] r2 = r1.mPreviewRequestBuilder     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            int r3 = MONO_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            r2 = r2[r3]     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            boolean r0 = r1.checkSessionAndBuilder(r0, r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            if (r0 == 0) goto L_0x02ad
            boolean r0 = r16.canStartMonoPreview()     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            if (r0 == 0) goto L_0x0291
            android.hardware.camera2.CameraCaptureSession[] r0 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            int r2 = MONO_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            r0 = r0[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            android.hardware.camera2.CaptureRequest$Builder[] r2 = r1.mPreviewRequestBuilder     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            int r3 = MONO_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            r2 = r2[r3]     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            android.hardware.camera2.CaptureRequest r2 = r2.build()     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r3 = r1.mCaptureCallback     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            android.os.Handler r4 = r1.mCameraHandler     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            r0.setRepeatingRequest(r2, r3, r4)     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            goto L_0x02ad
        L_0x0291:
            android.hardware.camera2.CameraCaptureSession[] r0 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            int r2 = MONO_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            r0 = r0[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            android.hardware.camera2.CaptureRequest$Builder[] r2 = r1.mPreviewRequestBuilder     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            int r3 = MONO_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            r2 = r2[r3]     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            android.hardware.camera2.CaptureRequest r2 = r2.build()     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r3 = r1.mCaptureCallback     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            android.os.Handler r4 = r1.mCameraHandler     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            r0.capture(r2, r3, r4)     // Catch:{ CameraAccessException | IllegalStateException -> 0x02a9 }
            goto L_0x02ad
        L_0x02a9:
            r0 = move-exception
            r0.printStackTrace()
        L_0x02ad:
            if (r5 == 0) goto L_0x02dd
            android.hardware.camera2.CameraCaptureSession[] r0 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            int r2 = FRONT_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            r0 = r0[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            android.hardware.camera2.CaptureRequest$Builder[] r2 = r1.mPreviewRequestBuilder     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            int r3 = FRONT_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            r2 = r2[r3]     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            boolean r0 = r1.checkSessionAndBuilder(r0, r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            if (r0 == 0) goto L_0x02dd
            android.hardware.camera2.CameraCaptureSession[] r0 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            int r2 = FRONT_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            r0 = r0[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            android.hardware.camera2.CaptureRequest$Builder[] r2 = r1.mPreviewRequestBuilder     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            int r3 = FRONT_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            r2 = r2[r3]     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            android.hardware.camera2.CaptureRequest r2 = r2.build()     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r3 = r1.mCaptureCallback     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            android.os.Handler r4 = r1.mCameraHandler     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            r0.setRepeatingRequest(r2, r3, r4)     // Catch:{ CameraAccessException | IllegalStateException -> 0x02d9 }
            goto L_0x02dd
        L_0x02d9:
            r0 = move-exception
            r0.printStackTrace()
        L_0x02dd:
            if (r8 == 0) goto L_0x030d
            android.hardware.camera2.CameraCaptureSession[] r0 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            int r2 = SWITCH_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            r0 = r0[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            android.hardware.camera2.CaptureRequest$Builder[] r2 = r1.mPreviewRequestBuilder     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            int r3 = SWITCH_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            r2 = r2[r3]     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            boolean r0 = r1.checkSessionAndBuilder(r0, r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            if (r0 == 0) goto L_0x030d
            android.hardware.camera2.CameraCaptureSession[] r0 = r1.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            int r2 = SWITCH_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            r0 = r0[r2]     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            android.hardware.camera2.CaptureRequest$Builder[] r2 = r1.mPreviewRequestBuilder     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            int r3 = SWITCH_ID     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            r2 = r2[r3]     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            android.hardware.camera2.CaptureRequest r2 = r2.build()     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            android.hardware.camera2.CameraCaptureSession$CaptureCallback r3 = r1.mCaptureCallback     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            android.os.Handler r1 = r1.mCameraHandler     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            r0.setRepeatingRequest(r2, r3, r1)     // Catch:{ CameraAccessException | IllegalStateException -> 0x0309 }
            goto L_0x030d
        L_0x0309:
            r0 = move-exception
            r0.printStackTrace()
        L_0x030d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CaptureModule.onSettingsChanged(java.util.List):void");
    }

    /* access modifiers changed from: private */
    public void setRepeatingBurstForZSL(int i) throws CameraAccessException, IllegalStateException {
        if (this.mPostProcessor.isZSLEnabled()) {
            ArrayList arrayList = new ArrayList();
            CaptureRequest build = this.mPreviewRequestBuilder[i].build();
            this.mPreviewRequestBuilder[i].removeTarget(this.mImageReader[i].getSurface());
            CaptureRequest build2 = this.mPreviewRequestBuilder[i].build();
            arrayList.add(build);
            if (!isLongShotActive()) {
                arrayList.add(build2);
            }
            this.mPreviewRequestBuilder[i].addTarget(this.mImageReader[i].getSurface());
            CameraCaptureSession[] cameraCaptureSessionArr = this.mCaptureSession;
            if (cameraCaptureSessionArr[i] != null) {
                cameraCaptureSessionArr[i].setRepeatingBurst(arrayList, this.mCaptureCallback, this.mCameraHandler);
            }
        }
    }

    private boolean isPanoSetting(String str) {
        try {
            if (Integer.parseInt(str) == 104) {
                return true;
            }
        } catch (Exception unused) {
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void updateFaceDetection() {
        final String value = this.mSettingsManager.getValue("pref_camera_facedetection_key");
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                String str = value;
                if (str == null || str.equals("off")) {
                    CaptureModule.this.mUI.onStopFaceDetection();
                } else {
                    CaptureModule.this.mUI.onStartFaceDetection(CaptureModule.this.mDisplayOrientation, CaptureModule.this.mSettingsManager.isFacingFront(CaptureModule.this.getMainCameraId()), CaptureModule.this.mCropRegion[CaptureModule.this.getMainCameraId()], CaptureModule.this.mSettingsManager.getSensorActiveArraySize(CaptureModule.this.getMainCameraId()));
                }
            }
        });
    }

    public void restartAll() {
        reinit();
        onPauseBeforeSuper();
        onPauseAfterSuper();
        onResumeBeforeSuper();
        onResumeAfterSuper();
        setRefocusLastTaken(false);
    }

    public void restartSession(boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append("restartSession isSurfaceChanged = ");
        sb.append(z);
        Log.d(TAG, sb.toString());
        if (!isAllSessionClosed()) {
            closeProcessors();
            closeSessions();
            if (z) {
                this.mUI.hideSurfaceView();
                this.mUI.showSurfaceView();
            }
            initializeValues();
            updatePreviewSize();
            openProcessors();
            createSessions();
            if (isTrackingFocusSettingOn()) {
                this.mUI.resetTrackingFocus();
            }
            resetStateMachine();
        }
    }

    private void resetStateMachine() {
        for (int i = 0; i < 4; i++) {
            this.mState[i] = 0;
        }
        this.mUI.enableShutter(true);
    }

    private Size getOptimalPreviewSize(Size size, Size[] sizeArr) {
        Point[] pointArr = new Point[sizeArr.length];
        double width = ((double) size.getWidth()) / ((double) size.getHeight());
        int length = sizeArr.length;
        int i = 0;
        int i2 = 0;
        while (i < length) {
            Size size2 = sizeArr[i];
            int i3 = i2 + 1;
            pointArr[i2] = new Point(size2.getWidth(), size2.getHeight());
            i++;
            i2 = i3;
        }
        int optimalPreviewSize = CameraUtil.getOptimalPreviewSize((Activity) this.mActivity, pointArr, width);
        if (optimalPreviewSize == -1) {
            return null;
        }
        return sizeArr[optimalPreviewSize];
    }

    private Size getMaxPictureSizeLiveshot() {
        Size[] supportedOutputSize = this.mSettingsManager.getSupportedOutputSize(getMainCameraId(), 256);
        float width = ((float) this.mVideoSize.getWidth()) / ((float) this.mVideoSize.getHeight());
        Size size = null;
        for (Size size2 : supportedOutputSize) {
            if (((double) Math.abs((((float) size2.getWidth()) / ((float) size2.getHeight())) - width)) <= 0.01d && (size == null || size2.getWidth() > size.getWidth())) {
                size = size2;
            }
        }
        if (size == null) {
            Log.w(TAG, "getMaxPictureSizeLiveshot: no picture size match the aspect ratio");
            for (Size size3 : supportedOutputSize) {
                if (size == null || size3.getWidth() > size.getWidth()) {
                    size = size3;
                }
            }
        }
        return size;
    }

    private Size getMaxPictureSizeLessThan4k() {
        Size[] supportedOutputSize = this.mSettingsManager.getSupportedOutputSize(getMainCameraId(), 256);
        float width = ((float) this.mVideoSize.getWidth()) / ((float) this.mVideoSize.getHeight());
        Size size = null;
        for (Size size2 : supportedOutputSize) {
            if (!is4kSize(size2) && ((double) Math.abs((((float) size2.getWidth()) / ((float) size2.getHeight())) - width)) <= 0.01d && (size == null || size2.getWidth() > size.getWidth())) {
                size = size2;
            }
        }
        if (size == null) {
            Log.w(TAG, "No picture size match the aspect ratio");
            for (Size size3 : supportedOutputSize) {
                if (!is4kSize(size3) && (size == null || size3.getWidth() > size.getWidth())) {
                    size = size3;
                }
            }
        }
        return size;
    }

    public TrackingFocusRenderer getTrackingForcusRenderer() {
        return this.mUI.getTrackingFocusRenderer();
    }

    public void onReleaseShutterLock() {
        Log.d(TAG, "onReleaseShutterLock");
        unlockFocus(0);
        unlockFocus(MONO_ID);
    }

    public void onClearSightSuccess(byte[] bArr) {
        Log.d(TAG, "onClearSightSuccess");
        onReleaseShutterLock();
        if (bArr != null) {
            this.mActivity.updateThumbnail(bArr);
        }
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                RotateTextToast.makeText((Activity) CaptureModule.this.mActivity, (int) C0905R.string.clearsight_capture_success, 0).show();
            }
        });
    }

    public void onClearSightFailure(byte[] bArr) {
        Log.d(TAG, "onClearSightFailure");
        if (bArr != null) {
            this.mActivity.updateThumbnail(bArr);
        }
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                RotateTextToast.makeText((Activity) CaptureModule.this.mActivity, (int) C0905R.string.clearsight_capture_fail, 0).show();
            }
        });
        onReleaseShutterLock();
    }

    public void onErrorListener(int i) {
        enableRecordingLocation(false);
    }

    public void onError(MediaRecorder mediaRecorder, int i, int i2) {
        StringBuilder sb = new StringBuilder();
        sb.append("MediaRecorder error. what=");
        sb.append(i);
        sb.append(". extra=");
        sb.append(i2);
        Log.e(TAG, sb.toString());
        stopRecordingVideo(getMainCameraId());
        this.mUI.showUIafterRecording();
        if (i == 1) {
            this.mActivity.updateStorageSpaceAndHint();
        }
    }

    public void onInfo(MediaRecorder mediaRecorder, int i, int i2) {
        if (i == 800) {
            if (this.mIsRecordingVideo) {
                stopRecordingVideo(getMainCameraId());
            }
        } else if (i == 801) {
            if (this.mIsRecordingVideo) {
                stopRecordingVideo(getMainCameraId());
            }
            RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.video_reach_size_limit, 1).show();
        }
    }

    /* access modifiers changed from: private */
    public byte[] getJpegData(Image image) {
        int i;
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bArr = new byte[buffer.remaining()];
        buffer.get(bArr);
        if (!this.mPostProcessor.isSelfieMirrorOn()) {
            return bArr;
        }
        CaptureUI captureUI = this.mUI;
        if ((!captureUI.isBokehMode && !captureUI.isMakeUp) || 1 != getMainCameraId()) {
            return bArr;
        }
        Bitmap decodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
        byte[] bitmapToNv21 = bitmapToNv21(decodeByteArray, decodeByteArray.getWidth(), decodeByteArray.getHeight());
        int width = decodeByteArray.getWidth();
        int height = decodeByteArray.getHeight();
        int i2 = ((width + 64) - 1) & -64;
        StringBuilder sb = new StringBuilder();
        sb.append("onImageToProcess: mStride = ");
        sb.append(i2);
        sb.append(",mWidth = ");
        sb.append(width);
        sb.append(",mHeight = ");
        sb.append(height);
        Log.i(TAG, sb.toString());
        ResultImage resultImage = new ResultImage(bitmapToNv21, new Rect(0, 0, width, height), width, height, i2);
        try {
            CameraCharacteristics cameraCharacteristics = this.manager.getCameraCharacteristics("1");
            if (((Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)).intValue() == 0) {
                i = ((((Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue() - this.mOrientation) + 360) % 360;
            } else {
                i = (((Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue() + this.mOrientation) % 360;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            i = 0;
        }
        decodeByteArray.recycle();
        return this.mPostProcessor.getFrontMirrorData(i, resultImage, waitForMetaData(0));
    }

    public void onStartCapturing() {
        this.mTotalCaptureResultList.clear();
    }

    public TotalCaptureResult waitForMetaData(int i) {
        for (int i2 = 10; i2 > 0; i2--) {
            if (this.mTotalCaptureResultList.size() > i) {
                return (TotalCaptureResult) this.mTotalCaptureResultList.get(i);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException unused) {
            }
        }
        if (this.mTotalCaptureResultList.size() == 0) {
            return null;
        }
        return (TotalCaptureResult) this.mTotalCaptureResultList.get(0);
    }

    public byte[] bitmapToNv21(Bitmap bitmap, int i, int i2) {
        if (bitmap == null || bitmap.getWidth() < i || bitmap.getHeight() < i2) {
            return null;
        }
        int[] iArr = new int[(i * i2)];
        bitmap.getPixels(iArr, 0, i, 0, 0, i, i2);
        return argbToNv21(iArr, i, i2);
    }

    private byte[] argbToNv21(int[] iArr, int i, int i2) {
        int i3 = i;
        int i4 = i2;
        int i5 = i3 * i4;
        byte[] bArr = new byte[((i5 * 3) / 2)];
        int i6 = i5;
        int i7 = 0;
        int i8 = 0;
        int i9 = 0;
        while (i7 < i4) {
            int i10 = i6;
            int i11 = i8;
            int i12 = 0;
            while (i12 < i3) {
                int i13 = (iArr[i9] & 16711680) >> 16;
                int i14 = (iArr[i9] & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8;
                int i15 = 255;
                int i16 = iArr[i9] & 255;
                int i17 = (((((i13 * 66) + (i14 * Const.CODE_C1_CW1)) + (i16 * 25)) + 128) >> 8) + 16;
                int i18 = (((((i13 * -38) - (i14 * 74)) + (i16 * 112)) + 128) >> 8) + 128;
                int i19 = (((((i13 * 112) - (i14 * 94)) - (i16 * 18)) + 128) >> 8) + 128;
                int i20 = i11 + 1;
                if (i17 < 0) {
                    i17 = 0;
                } else if (i17 > 255) {
                    i17 = 255;
                }
                bArr[i11] = (byte) i17;
                if (i7 % 2 == 0 && i9 % 2 == 0 && i10 < bArr.length - 2) {
                    int i21 = i10 + 1;
                    if (i19 < 0) {
                        i19 = 0;
                    } else if (i19 > 255) {
                        i19 = 255;
                    }
                    bArr[i10] = (byte) i19;
                    i10 = i21 + 1;
                    if (i18 < 0) {
                        i15 = 0;
                    } else if (i18 <= 255) {
                        i15 = i18;
                    }
                    bArr[i21] = (byte) i15;
                }
                i9++;
                i12++;
                i11 = i20;
            }
            i7++;
            i8 = i11;
            i6 = i10;
        }
        return bArr;
    }

    private void updateSaveStorageState() {
        Storage.setSaveSDCard(this.mSettingsManager.getValue("pref_camera_savepath_key").equals("1"));
    }

    public void startPlayVideoActivity() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(this.mCurrentVideoUri, CameraUtil.convertOutputFormatToMimeType(this.mProfile.fileFormat));
        intent.addFlags(1);
        try {
            this.mActivity.startActivityForResult(intent, 142);
        } catch (ActivityNotFoundException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Couldn't view video ");
            sb.append(this.mCurrentVideoUri);
            Log.e(TAG, sb.toString(), e);
        }
    }

    private void closeVideoFileDescriptor() {
        ParcelFileDescriptor parcelFileDescriptor = this.mVideoFileDescriptor;
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } catch (IOException e) {
                Log.e(TAG, "Fail to close fd", e);
            }
            this.mVideoFileDescriptor = null;
        }
    }

    private Bitmap getVideoThumbnail() {
        Bitmap bitmap;
        ParcelFileDescriptor parcelFileDescriptor = this.mVideoFileDescriptor;
        if (parcelFileDescriptor != null) {
            bitmap = Thumbnail.createVideoThumbnailBitmap(parcelFileDescriptor.getFileDescriptor(), this.mVideoPreviewSize.getWidth());
        } else {
            Uri uri = this.mCurrentVideoUri;
            if (uri != null) {
                try {
                    this.mVideoFileDescriptor = this.mContentResolver.openFileDescriptor(uri, "r");
                    bitmap = Thumbnail.createVideoThumbnailBitmap(this.mVideoFileDescriptor.getFileDescriptor(), this.mVideoPreviewSize.getWidth());
                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.toString());
                }
            }
            bitmap = null;
        }
        return bitmap != null ? CameraUtil.rotateAndMirror(bitmap, 0, this.mPostProcessor.isSelfieMirrorOn()) : bitmap;
    }

    private void deleteVideoFile(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("Deleting video ");
        sb.append(str);
        String sb2 = sb.toString();
        String str2 = TAG;
        Log.v(str2, sb2);
        if (!new File(str).delete()) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("Could not delete ");
            sb3.append(str);
            Log.v(str2, sb3.toString());
        }
    }

    /* access modifiers changed from: private */
    public void releaseMediaRecorder() {
        Log.v(TAG, "Releasing media recorder.");
        if (this.mMediaRecorder != null) {
            cleanupEmptyFile();
            this.mMediaRecorder.reset();
            this.mMediaRecorder.release();
            this.mMediaRecorder = null;
        }
        this.mVideoFilename = null;
    }

    private void cleanupEmptyFile() {
        String str = this.mVideoFilename;
        if (str != null) {
            File file = new File(str);
            if (file.length() == 0 && file.delete()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Empty video file deleted: ");
                sb.append(this.mVideoFilename);
                Log.v(TAG, sb.toString());
                this.mVideoFilename = null;
            }
        }
    }

    private void showToast(String str) {
        if (this.mToast == null) {
            this.mToast = Toast.makeText(this.mActivity, str, 1);
            this.mToast.setGravity(17, 0, 0);
        }
        this.mToast.setText(str);
        this.mToast.show();
    }

    private boolean isRecorderReady() {
        return (this.mStartRecPending || this.mStopRecPending) ? false : true;
    }

    private void requestAudioFocus() {
        if (((AudioManager) this.mActivity.getSystemService("audio")).requestAudioFocus(null, 3, 2) == 0) {
            Log.v(TAG, "Audio focus request failed");
        }
    }

    private void releaseAudioFocus() {
        if (((AudioManager) this.mActivity.getSystemService("audio")).abandonAudioFocus(null) == 0) {
            Log.v(TAG, "Audio focus release failed");
        }
    }

    private boolean isVideoCaptureIntent() {
        return "android.media.action.VIDEO_CAPTURE".equals(this.mActivity.getIntent().getAction());
    }

    private void resetScreenOn() {
        this.mHandler.removeMessages(4);
        this.mActivity.getWindow().clearFlags(128);
    }

    private void keepScreenOnAwhile() {
        this.mHandler.removeMessages(4);
        this.mActivity.getWindow().addFlags(128);
        this.mHandler.sendEmptyMessageDelayed(4, 120000);
    }

    /* access modifiers changed from: private */
    public void keepScreenOn() {
        this.mHandler.removeMessages(4);
        this.mActivity.getWindow().addFlags(128);
    }

    private void setProModeVisible() {
        this.mUI.initializeProMode(!this.mPaused && isProMode());
    }

    private void childModesUIUpdate() {
        this.mUI.initializeChildModeUIUpdate(!this.mPaused && isSceneMode());
    }

    private boolean isProMode() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
        return value != null && Integer.parseInt(value) == 109;
    }

    private boolean isHDRMode() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
        return value != null && Integer.parseInt(value) == 18;
    }

    public boolean isSceneMode() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
        return (value == null || Integer.parseInt(value) == 0) ? false : true;
    }

    /* access modifiers changed from: private */
    public void setBokehModeVisible() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
        if (value != null) {
            this.mBokehEnabled = Integer.parseInt(value) == 110;
        }
        boolean z = this.mPaused;
        if (this.mBokehEnabled) {
            keepScreenOn();
        } else {
            keepScreenOnAwhile();
        }
    }

    private void applyWbColorTemperature(Builder builder) {
        CameraActivity cameraActivity = this.mActivity;
        String localSharedPreferencesName = ComboPreferences.getLocalSharedPreferencesName(cameraActivity, getMainCameraId());
        Integer valueOf = Integer.valueOf(0);
        SharedPreferences sharedPreferences = cameraActivity.getSharedPreferences(localSharedPreferencesName, 0);
        String string = sharedPreferences.getString(SettingsManager.KEY_MANUAL_WB, "off");
        String string2 = this.mActivity.getString(C0905R.string.pref_camera_manual_wb_value_color_temperature);
        String string3 = this.mActivity.getString(C0905R.string.pref_camera_manual_wb_value_rbgb_gains);
        if (string.equals(string2)) {
            int parseInt = Integer.parseInt(sharedPreferences.getString(SettingsManager.KEY_MANUAL_WB_TEMPERATURE_VALUE, "-1"));
            if (parseInt != -1) {
                builder.set(CaptureRequest.CONTROL_AWB_MODE, valueOf);
                VendorTagUtil.setWbColorTemperatureValue(builder, Integer.valueOf(parseInt));
            }
        } else if (string.equals(string3)) {
            float f = sharedPreferences.getFloat(SettingsManager.KEY_MANUAL_WB_R_GAIN, -1.0f);
            float f2 = sharedPreferences.getFloat(SettingsManager.KEY_MANUAL_WB_G_GAIN, -1.0f);
            float f3 = sharedPreferences.getFloat(SettingsManager.KEY_MANUAL_WB_B_GAIN, -1.0f);
            if (((double) f) != -1.0d && ((double) f2) != -1.0d && f3 != -1.0f) {
                builder.set(CaptureRequest.CONTROL_AWB_MODE, valueOf);
                VendorTagUtil.setMWBGainsValue(builder, new float[]{f, f2, f3});
            }
        } else {
            VendorTagUtil.setWbColorTemperatureValue(builder, Integer.valueOf(5000));
            VendorTagUtil.setMWBGainsValue(builder, new float[]{2.0f, 3.0f, 2.5f});
            VendorTagUtil.setMWBDisableMode(builder);
        }
    }

    public void setCameraModeSwitcherAllowed(boolean z) {
        this.mCameraModeSwitcherAllowed = z;
    }

    public boolean getCameraModeSwitcherAllowed() {
        return this.mCameraModeSwitcherAllowed;
    }

    public void updateShutterButtonStatus(boolean z) {
        this.mUI.enableShutter(z);
    }

    public void setSessionComplete(boolean z) {
        this.mIsSessionComplete = z;
    }

    public boolean getSessionComplete() {
        return this.mIsSessionComplete;
    }
}
