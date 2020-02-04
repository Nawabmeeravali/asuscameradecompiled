package com.android.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.location.Location;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.EncoderCapabilities;
import android.media.EncoderCapabilities.VideoEncoderCap;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.media.MediaRecorder.VideoEncoder;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;
import com.android.camera.CameraHolder.CameraInfo;
import com.android.camera.CameraManager.CameraPictureCallback;
import com.android.camera.CameraManager.CameraPreviewDataCallback;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraPreference.OnPreferenceChangedListener;
import com.android.camera.LocationManager.Listener;
import com.android.camera.MediaSaveService.OnMediaSavedListener;
import com.android.camera.ShutterButton.OnShutterButtonListener;
import com.android.camera.VideoUI.SURFACE_STATUS;
import com.android.camera.app.OrientationManager;
import com.android.camera.exif.ExifInterface;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.tinyplanet.TinyPlanetFragment;
import com.android.camera.util.AccessibilityUtils;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.PersistUtil;
import com.asus.scenedetectlib.BuildConfig;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.codeaurora.snapcam.C0905R;
import org.codeaurora.snapcam.wrapper.CamcorderProfileWrapper;
import org.codeaurora.snapcam.wrapper.ParametersWrapper;

public class VideoModule implements CameraModule, VideoController, OnPreferenceChangedListener, OnShutterButtonListener, Listener, OnErrorListener, OnInfoListener {
    private static final DefaultHashMap<String, Integer> AUDIO_ENCODER_TABLE = new DefaultHashMap<>();
    private static final int CHECK_DISPLAY_ROTATION = 3;
    private static final int CLEAR_SCREEN_DELAY = 4;
    private static final int ENABLE_SHUTTER_BUTTON = 6;
    private static final String EXTRA_QUICK_CAPTURE = "android.intent.extra.quickCapture";
    private static final String FORMAT_NV12_VENUS = "nv12-venus";
    private static final String FORMAT_NV21 = "yuv420sp";
    private static final int HANDLE_FLASH_TORCH_DELAY = 10;
    private static final String KEY_PREVIEW_FORMAT = "preview-format";
    private static final int MAX_ZOOM = 10;
    private static final DefaultHashMap<String, Integer> OUTPUT_FORMAT_TABLE = new DefaultHashMap<>();
    private static final boolean PERSIST_4K_NO_LIMIT = SystemProperties.getBoolean("persist.camcorder.4k.nolimit", false);
    private static final int PERSIST_EIS_MAX_FPS = SystemProperties.getInt("persist.camcorder.eis.maxfps", 30);
    private static final int SCREEN_DELAY = 120000;
    private static final long SDCARD_SIZE_LIMIT = -100663296;
    private static final int SHOW_TAP_TO_SNAPSHOT_TOAST = 7;
    private static final long SHUTTER_BUTTON_TIMEOUT = 0;
    private static final int SWITCH_CAMERA = 8;
    private static final int SWITCH_CAMERA_START_ANIMATION = 9;
    private static final String TAG = "CAM_VideoModule";
    private static final int UPDATE_RECORD_TIME = 5;
    private static final DefaultHashMap<String, Integer> VIDEOQUALITY_BITRATE_TABLE = new DefaultHashMap<>();
    private static final DefaultHashMap<String, Integer> VIDEO_ENCODER_TABLE = new DefaultHashMap<>();
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    private int mAudioEncoder;
    /* access modifiers changed from: private */
    public CameraProxy mCameraDevice;
    private int mCameraDisplayOrientation;
    private int mCameraId;
    private boolean mCaptureTimeLapse = false;
    private ContentResolver mContentResolver;
    private String mCurrentVideoFilename;
    /* access modifiers changed from: private */
    public Uri mCurrentVideoUri;
    /* access modifiers changed from: private */
    public boolean mCurrentVideoUriFromMediaSaved;
    private ContentValues mCurrentVideoValues;
    private String mDefaultAntibanding = null;
    private int mDesiredPreviewHeight;
    private int mDesiredPreviewWidth;
    /* access modifiers changed from: private */
    public int mDisplayRotation;
    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();
    private boolean mFaceDetectionEnabled = false;
    private boolean mFaceDetectionStarted = false;
    /* access modifiers changed from: private */
    public final Handler mHandler = new MainHandler();
    private boolean mIsDISEnabled = false;
    private boolean mIsFlipEnabled = false;
    private boolean mIsInReviewMode;
    private boolean mIsMute = false;
    private boolean mIsVideoCDSUpdated = false;
    private boolean mIsVideoCaptureIntent;
    private boolean mIsVideoTNREnabled;
    private LocationManager mLocationManager;
    private int mMaxVideoDurationInMs;
    private MediaRecorder mMediaRecorder;
    private boolean mMediaRecorderPausing = false;
    /* access modifiers changed from: private */
    public boolean mMediaRecorderRecording = false;
    private final OnMediaSavedListener mOnPhotoSavedListener = new OnMediaSavedListener() {
        public void onMediaSaved(Uri uri) {
            if (uri != null) {
                VideoModule.this.mActivity.notifyNewMedia(uri);
            }
        }
    };
    /* access modifiers changed from: private */
    public long mOnResumeTime;
    private final OnMediaSavedListener mOnVideoSavedListener = new OnMediaSavedListener() {
        public void onMediaSaved(Uri uri) {
            if (uri != null) {
                VideoModule.this.mCurrentVideoUri = uri;
                VideoModule.this.mCurrentVideoUriFromMediaSaved = true;
                VideoModule.this.onVideoSaved();
                VideoModule.this.mActivity.notifyNewMedia(uri);
            }
        }
    };
    private int mOrientation = -1;
    private OrientationManager mOrientationManager;
    private boolean mOverrideCDS = false;
    private Parameters mParameters;
    /* access modifiers changed from: private */
    public boolean mPaused;
    private int mPendingSwitchCameraId;
    private PreferenceGroup mPreferenceGroup;
    private boolean mPreferenceRead;
    private ComboPreferences mPreferences;
    private String mPrevSavedVideoCDS = null;
    /* access modifiers changed from: private */
    public boolean mPreviewFocused = false;
    boolean mPreviewing = false;
    private CamcorderProfile mProfile;
    private boolean mQuickCapture;
    private BroadcastReceiver mReceiver = null;
    private long mRecordingStartTime;
    private boolean mRecordingTimeCountsDown = false;
    private long mRecordingTotalTime;
    private boolean mRestartPreview = false;
    private boolean mSaveToSDCard = false;
    /* access modifiers changed from: private */
    public boolean mSnapshotInProgress = false;
    private boolean mStartPrevPending = false;
    private boolean mStartRecPending = false;
    private boolean mStopPrevPending = false;
    private boolean mStopRecPending = false;
    /* access modifiers changed from: private */
    public boolean mSwitchingCamera;
    private String mTempVideoCDS = null;
    private int mTimeBetweenTimeLapseFrameCaptureMs = 0;
    /* access modifiers changed from: private */
    public VideoUI mUI;
    private boolean mUnsupportedHFRVideoCodec = false;
    private boolean mUnsupportedHFRVideoSize = false;
    private boolean mUnsupportedHSRVideoSize = false;
    boolean mUnsupportedProfile = false;
    boolean mUnsupportedResolution = false;
    private int mVideoEncoder;
    private ParcelFileDescriptor mVideoFileDescriptor;
    private String mVideoFilename;
    private boolean mWasMute = false;
    private int[] mZoomIdxTbl = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    private int mZoomValue;
    private int videoHeight;
    private int videoWidth;

    protected class CameraOpenThread extends Thread {
        protected CameraOpenThread() {
        }

        public void run() {
            VideoModule.this.openCamera();
        }
    }

    static class DefaultHashMap<K, V> extends HashMap<K, V> {
        private V mDefaultValue;

        DefaultHashMap() {
        }

        public void putDefault(V v) {
            this.mDefaultValue = v;
        }

        public V get(Object obj) {
            V v = super.get(obj);
            return v == null ? this.mDefaultValue : v;
        }

        public K getKey(V v) {
            for (K next : keySet()) {
                if (get(next).equals(v)) {
                    return next;
                }
            }
            return null;
        }
    }

    private final class JpegPictureCallback implements CameraPictureCallback {
        Location mLocation;

        public JpegPictureCallback(Location location) {
            this.mLocation = location;
        }

        public void onPictureTaken(byte[] bArr, CameraProxy cameraProxy) {
            Log.v(VideoModule.TAG, "onPictureTaken");
            if (VideoModule.this.mSnapshotInProgress && !VideoModule.this.mPaused && VideoModule.this.mCameraDevice != null) {
                VideoModule.this.mSnapshotInProgress = false;
                VideoModule.this.showVideoSnapshotUI(false);
                VideoModule.this.storeImage(bArr, this.mLocation);
            }
        }
    }

    private class MainHandler extends Handler {
        private MainHandler() {
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 3:
                    if (CameraUtil.getDisplayRotation(VideoModule.this.mActivity) != VideoModule.this.mDisplayRotation && !VideoModule.this.mMediaRecorderRecording && !VideoModule.this.mSwitchingCamera) {
                        VideoModule.this.startPreview();
                    }
                    if (SystemClock.uptimeMillis() - VideoModule.this.mOnResumeTime < 5000) {
                        VideoModule.this.mHandler.sendEmptyMessageDelayed(3, 100);
                        return;
                    }
                    return;
                case 4:
                    VideoModule.this.mActivity.getWindow().clearFlags(128);
                    return;
                case 5:
                    VideoModule.this.updateRecordingTime();
                    return;
                case 6:
                    VideoModule.this.mUI.enableShutter(true);
                    return;
                case 7:
                    VideoModule.this.showTapToSnapshotToast();
                    return;
                case 8:
                    VideoModule.this.switchCamera();
                    return;
                case 9:
                    VideoModule.this.mSwitchingCamera = false;
                    return;
                case 10:
                    VideoModule videoModule = VideoModule.this;
                    videoModule.forceFlashOff(!videoModule.mPreviewFocused);
                    return;
                default:
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unhandled message: ");
                    sb.append(message.what);
                    Log.v(VideoModule.TAG, sb.toString());
                    return;
            }
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private MyBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.MEDIA_EJECT") || action.equals("android.intent.action.SCREEN_OFF")) {
                VideoModule.this.stopVideoRecording();
                RotateTextToast.makeText((Activity) VideoModule.this.mActivity, (CharSequence) VideoModule.this.mActivity.getResources().getString(C0905R.string.video_recording_stopped), 0).show();
            }
        }
    }

    private String convertOutputFormatToFileExt(int i) {
        return i == 2 ? ".mp4" : ".3gp";
    }

    private String convertOutputFormatToMimeType(int i) {
        return i == 2 ? "video/mp4" : "video/3gpp";
    }

    public void onActivityResult(int i, int i2, Intent intent) {
    }

    public void onCaptureTextureCopied() {
    }

    public void onDestroy() {
    }

    public void onMediaSaveServiceConnected(MediaSaveService mediaSaveService) {
    }

    public void onOverriddenPreferencesClicked() {
    }

    public void onPauseAfterSuper() {
    }

    public void onProtectiveCurtainClick(View view) {
    }

    public void onRestorePreferencesClicked() {
    }

    public void onShutterButtonLongClick() {
    }

    public void onStop() {
    }

    public void onStorageNotEnoughRecordingVideo() {
    }

    public boolean updateStorageHintOnResume() {
        return true;
    }

    static {
        Integer valueOf = Integer.valueOf(0);
        DefaultHashMap<String, Integer> defaultHashMap = OUTPUT_FORMAT_TABLE;
        Integer valueOf2 = Integer.valueOf(1);
        defaultHashMap.put("3gp", valueOf2);
        DefaultHashMap<String, Integer> defaultHashMap2 = OUTPUT_FORMAT_TABLE;
        Integer valueOf3 = Integer.valueOf(2);
        defaultHashMap2.put("mp4", valueOf3);
        OUTPUT_FORMAT_TABLE.putDefault(valueOf);
        VIDEO_ENCODER_TABLE.put("h263", valueOf2);
        VIDEO_ENCODER_TABLE.put("h264", valueOf3);
        int intFieldIfExists = ApiHelper.getIntFieldIfExists(VideoEncoder.class, "HEVC", null, 0);
        if (intFieldIfExists == 0) {
            intFieldIfExists = ApiHelper.getIntFieldIfExists(VideoEncoder.class, "H265", null, 0);
        }
        VIDEO_ENCODER_TABLE.put("h265", Integer.valueOf(intFieldIfExists));
        VIDEO_ENCODER_TABLE.put("m4v", Integer.valueOf(3));
        VIDEO_ENCODER_TABLE.putDefault(valueOf);
        AUDIO_ENCODER_TABLE.put("amrnb", valueOf2);
        AUDIO_ENCODER_TABLE.put("amrwb", valueOf3);
        AUDIO_ENCODER_TABLE.put("aac", Integer.valueOf(3));
        AUDIO_ENCODER_TABLE.putDefault(valueOf);
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

    /* access modifiers changed from: private */
    public void openCamera() {
        if (this.mCameraDevice == null) {
            CameraActivity cameraActivity = this.mActivity;
            this.mCameraDevice = CameraUtil.openCamera(cameraActivity, this.mCameraId, this.mHandler, cameraActivity.getCameraOpenErrorCallback());
        }
        CameraProxy cameraProxy = this.mCameraDevice;
        if (cameraProxy != null) {
            this.mParameters = cameraProxy.getParameters();
            this.mPreviewFocused = arePreviewControlsVisible();
        }
    }

    private String createName(long j) {
        return new SimpleDateFormat(this.mActivity.getString(C0905R.string.video_file_name_format)).format(new Date(j));
    }

    private int getPreferredCameraId(ComboPreferences comboPreferences) {
        int cameraFacingIntentExtras = CameraUtil.getCameraFacingIntentExtras(this.mActivity);
        if (cameraFacingIntentExtras != -1) {
            return cameraFacingIntentExtras;
        }
        return CameraSettings.readPreferredCameraId(comboPreferences);
    }

    private void initializeSurfaceView() {
        if (!ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {
            this.mUI.initializeSurfaceView();
        }
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
        this.mUI = new VideoUI(cameraActivity, this, view);
        this.mPreferences = ComboPreferences.get(this.mActivity);
        if (this.mPreferences == null) {
            this.mPreferences = new ComboPreferences(this.mActivity);
        }
        CameraSettings.upgradeGlobalPreferences(this.mPreferences.getGlobal(), cameraActivity);
        this.mCameraId = getPreferredCameraId(this.mPreferences);
        this.mPreferences.setLocalId(this.mActivity, this.mCameraId);
        CameraSettings.upgradeLocalPreferences(this.mPreferences.getLocal());
        this.mOrientationManager = new OrientationManager(this.mActivity);
        CameraOpenThread cameraOpenThread = new CameraOpenThread();
        cameraOpenThread.start();
        this.mContentResolver = this.mActivity.getContentResolver();
        Storage.setSaveSDCard(this.mPreferences.getString("pref_camera_savepath_key", "0").equals("1"));
        this.mSaveToSDCard = Storage.isSaveSDCard();
        this.mIsVideoCaptureIntent = isVideoCaptureIntent();
        initializeSurfaceView();
        try {
            cameraOpenThread.join();
            if (this.mCameraDevice == null) {
                return;
            }
        } catch (InterruptedException unused) {
        }
        readVideoPreferences();
        this.mUI.setPrefChangedListener(this);
        this.mQuickCapture = this.mActivity.getIntent().getBooleanExtra(EXTRA_QUICK_CAPTURE, false);
        this.mLocationManager = new LocationManager(this.mActivity, this);
        this.mUI.setOrientationIndicator(0, false);
        setDisplayOrientation();
        this.mUI.showTimeLapseUI(this.mCaptureTimeLapse);
        initializeVideoSnapshot();
        resizeForPreviewAspectRatio();
        initializeVideoControl();
        this.mPendingSwitchCameraId = -1;
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

    public void setPreferenceForTest(String str, String str2) {
        this.mUI.setPreference(str, str2);
        onSharedPreferenceChanged();
    }

    public void onSingleTapUp(View view, int i, int i2) {
        if (!this.mMediaRecorderPausing) {
            takeASnapshot();
        }
    }

    private void takeASnapshot() {
        if (CameraUtil.isVideoSnapshotSupported(this.mParameters) && !this.mIsVideoCaptureIntent && this.mMediaRecorderRecording && !this.mPaused && !this.mSnapshotInProgress) {
            MediaSaveService mediaSaveService = this.mActivity.getMediaSaveService();
            if (mediaSaveService != null && !mediaSaveService.isQueueFull()) {
                this.mParameters.setRotation(CameraUtil.getJpegRotationForCamera1(this.mCameraId, this.mOrientation));
                Location currentLocation = this.mLocationManager.getCurrentLocation();
                CameraUtil.setGpsParameters(this.mParameters, currentLocation);
                this.mCameraDevice.setParameters(this.mParameters);
                Log.v(TAG, "Video snapshot start");
                this.mCameraDevice.takePicture(this.mHandler, null, null, null, new JpegPictureCallback(currentLocation));
                showVideoSnapshotUI(true);
                this.mSnapshotInProgress = true;
            }
        }
    }

    private void loadCameraPreferences() {
        PreferenceGroup preferenceGroup = new CameraSettings(this.mActivity, this.mParameters, this.mCameraId, CameraHolder.instance().getCameraInfo()).getPreferenceGroup(C0905R.xml.video_preferences);
        filterPreferenceScreenByIntent(preferenceGroup);
        this.mPreferenceGroup = preferenceGroup;
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

    private void initializeVideoControl() {
        loadCameraPreferences();
        this.mUI.initializePopup(this.mPreferenceGroup);
    }

    public void onOrientationChanged(int i) {
        if (i != -1) {
            int roundOrientation = CameraUtil.roundOrientation(i, this.mOrientation);
            if (this.mOrientation != roundOrientation) {
                this.mOrientation = roundOrientation;
                Log.v(TAG, "onOrientationChanged, update parameters");
                if (this.mCameraDevice != null && this.mParameters != null && true == this.mPreviewing && !this.mMediaRecorderRecording) {
                    setFlipValue();
                    updatePowerMode();
                    this.mCameraDevice.setParameters(this.mParameters);
                }
                this.mUI.tryToCloseSubList();
                this.mUI.setOrientation(roundOrientation, true);
            }
            if (this.mHandler.hasMessages(7)) {
                this.mHandler.removeMessages(7);
                showTapToSnapshotToast();
            }
        }
    }

    private void startPlayVideoActivity() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(this.mCurrentVideoUri, convertOutputFormatToMimeType(this.mProfile.fileFormat));
        try {
            this.mActivity.startActivityForResult(intent, 142);
        } catch (ActivityNotFoundException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Couldn't view video ");
            sb.append(this.mCurrentVideoUri);
            Log.e(TAG, sb.toString(), e);
        }
    }

    @OnClickAttr
    public void onReviewPlayClicked(View view) {
        startPlayVideoActivity();
    }

    @OnClickAttr
    public void onReviewDoneClicked(View view) {
        this.mIsInReviewMode = false;
        doReturnToCaller(true);
    }

    @OnClickAttr
    public void onReviewCancelClicked(View view) {
        if (this.mCurrentVideoUriFromMediaSaved) {
            this.mContentResolver.delete(this.mCurrentVideoUri, null, null);
        }
        this.mIsInReviewMode = false;
        doReturnToCaller(false);
    }

    public boolean isInReviewMode() {
        return this.mIsInReviewMode;
    }

    private void onStopVideoRecording() {
        boolean stopVideoRecording = stopVideoRecording();
        if (this.mIsVideoCaptureIntent) {
            if (this.mQuickCapture) {
                doReturnToCaller(!stopVideoRecording);
            } else if (!stopVideoRecording) {
                showCaptureResult();
            } else if (stopVideoRecording) {
                this.mUI.enableShutter(true);
            }
        } else if (!stopVideoRecording && !this.mPaused && ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {
            this.mUI.animateFlash();
            this.mUI.animateCapture();
        }
        this.mUI.showUIafterRecording();
    }

    public void onVideoSaved() {
        if (this.mIsVideoCaptureIntent) {
            showCaptureResult();
        }
    }

    public boolean isPreviewReady() {
        return (this.mStartPrevPending || this.mStopPrevPending) ? false : true;
    }

    public boolean isRecorderReady() {
        return (this.mStartRecPending || this.mStopRecPending) ? false : true;
    }

    public void onShutterButtonClick() {
        if (!this.mPaused && !this.mUI.collapseCameraControls() && !this.mSwitchingCamera) {
            boolean z = this.mMediaRecorderRecording;
            if (isPreviewReady() && isRecorderReady()) {
                this.mUI.enableShutter(false);
                if (z) {
                    onStopVideoRecording();
                } else if (!startVideoRecording()) {
                    this.mUI.showUIafterRecording();
                }
                if (!this.mIsVideoCaptureIntent || !z) {
                    this.mHandler.sendEmptyMessageDelayed(6, 0);
                }
            }
        }
    }

    public void onShutterButtonFocus(boolean z) {
        this.mUI.setShutterPressed(z);
    }

    private void qcomReadVideoPreferences() {
        int i;
        this.mVideoEncoder = ((Integer) VIDEO_ENCODER_TABLE.get(this.mPreferences.getString("pref_camera_videoencoder_key", this.mActivity.getString(C0905R.string.pref_camera_videoencoder_default)))).intValue();
        StringBuilder sb = new StringBuilder();
        sb.append("Video Encoder selected = ");
        sb.append(this.mVideoEncoder);
        String sb2 = sb.toString();
        String str = TAG;
        Log.v(str, sb2);
        this.mAudioEncoder = ((Integer) AUDIO_ENCODER_TABLE.get(this.mPreferences.getString("pref_camera_audioencoder_key", this.mActivity.getString(C0905R.string.pref_camera_audioencoder_default)))).intValue();
        StringBuilder sb3 = new StringBuilder();
        sb3.append("Audio Encoder selected = ");
        sb3.append(this.mAudioEncoder);
        Log.v(str, sb3.toString());
        try {
            i = Integer.parseInt(this.mPreferences.getString("pref_camera_video_duration_key", this.mActivity.getString(C0905R.string.pref_camera_video_duration_default)));
        } catch (NumberFormatException unused) {
            i = Integer.parseInt(this.mActivity.getString(C0905R.string.pref_camera_video_duration_default));
        }
        if (i == -1) {
            this.mMaxVideoDurationInMs = 30000;
        } else {
            this.mMaxVideoDurationInMs = i * 60000;
        }
        if (ParametersWrapper.isPowerModeSupported(this.mParameters)) {
            String string = this.mPreferences.getString(CameraSettings.KEY_POWER_MODE, this.mActivity.getString(C0905R.string.pref_camera_powermode_default));
            StringBuilder sb4 = new StringBuilder();
            sb4.append("read videopreferences power mode =");
            sb4.append(string);
            Log.v(str, sb4.toString());
            if (!ParametersWrapper.getPowerMode(this.mParameters).equals(string) && this.mPreviewing) {
                this.mRestartPreview = true;
            }
            ParametersWrapper.setPowerMode(this.mParameters, string);
        }
        if (ParametersWrapper.getSupportedDenoiseModes(this.mParameters) != null) {
            ParametersWrapper.setDenoise(this.mParameters, this.mPreferences.getString(CameraSettings.KEY_DENOISE, this.mActivity.getString(C0905R.string.pref_camera_denoise_default)));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00bb, code lost:
        if ("hsr".equals(r2.substring(0, 3)) != false) goto L_0x00bd;
     */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00fb  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0100  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0131  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0133  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0165  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readVideoPreferences() {
        /*
            r9 = this;
            com.android.camera.ComboPreferences r0 = r9.mPreferences
            java.lang.String r1 = "pref_video_quality_key"
            r2 = 0
            java.lang.String r0 = r0.getString(r1, r2)
            if (r0 != 0) goto L_0x004c
            com.android.camera.CameraManager$CameraProxy r0 = r9.mCameraDevice
            android.hardware.Camera$Parameters r0 = r0.getParameters()
            r9.mParameters = r0
            com.android.camera.CameraActivity r0 = r9.mActivity
            android.content.res.Resources r0 = r0.getResources()
            r2 = 2131690608(0x7f0f0470, float:1.9010264E38)
            java.lang.String r0 = r0.getString(r2)
            java.lang.String r2 = ""
            boolean r2 = r0.equals(r2)
            if (r2 != 0) goto L_0x0037
            int r2 = r9.mCameraId
            android.hardware.Camera$Parameters r3 = r9.mParameters
            java.util.ArrayList r2 = com.android.camera.CameraSettings.getSupportedVideoQualities(r2, r3)
            boolean r2 = com.android.camera.util.CameraUtil.isSupported(r0, r2)
            if (r2 == 0) goto L_0x0037
            goto L_0x003f
        L_0x0037:
            int r0 = r9.mCameraId
            android.hardware.Camera$Parameters r2 = r9.mParameters
            java.lang.String r0 = com.android.camera.CameraSettings.getSupportedHighestVideoQuality(r0, r2)
        L_0x003f:
            com.android.camera.ComboPreferences r2 = r9.mPreferences
            android.content.SharedPreferences$Editor r2 = r2.edit()
            android.content.SharedPreferences$Editor r1 = r2.putString(r1, r0)
            r1.apply()
        L_0x004c:
            java.util.HashMap<java.lang.String, java.lang.Integer> r1 = com.android.camera.CameraSettings.VIDEO_QUALITY_TABLE
            java.lang.Object r0 = r1.get(r0)
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r0 = r0.intValue()
            com.android.camera.CameraActivity r1 = r9.mActivity
            android.content.Intent r1 = r1.getIntent()
            java.lang.String r2 = "android.intent.extra.videoQuality"
            boolean r3 = r1.hasExtra(r2)
            r4 = 1
            r5 = 0
            if (r3 == 0) goto L_0x0071
            int r0 = r1.getIntExtra(r2, r5)
            if (r0 <= 0) goto L_0x0070
            r0 = r4
            goto L_0x0071
        L_0x0070:
            r0 = r5
        L_0x0071:
            com.android.camera.ComboPreferences r2 = r9.mPreferences
            com.android.camera.CameraActivity r3 = r9.mActivity
            r6 = 2131690663(0x7f0f04a7, float:1.9010376E38)
            java.lang.String r3 = r3.getString(r6)
            java.lang.String r6 = "pref_video_time_lapse_frame_interval_key"
            java.lang.String r2 = r2.getString(r6, r3)
            int r2 = java.lang.Integer.parseInt(r2)
            r9.mTimeBetweenTimeLapseFrameCaptureMs = r2
            int r2 = r9.mTimeBetweenTimeLapseFrameCaptureMs
            if (r2 == 0) goto L_0x008e
            r2 = r4
            goto L_0x008f
        L_0x008e:
            r2 = r5
        L_0x008f:
            r9.mCaptureTimeLapse = r2
            com.android.camera.ComboPreferences r2 = r9.mPreferences
            com.android.camera.CameraActivity r3 = r9.mActivity
            r6 = 2131690094(0x7f0f026e, float:1.9009222E38)
            java.lang.String r3 = r3.getString(r6)
            java.lang.String r6 = "pref_camera_hfr_key"
            java.lang.String r2 = r2.getString(r6, r3)
            r3 = 3
            java.lang.String r6 = r2.substring(r5, r3)
            java.lang.String r7 = "hfr"
            boolean r6 = r7.equals(r6)
            java.lang.String r7 = "CAM_VideoModule"
            if (r6 != 0) goto L_0x00bd
            java.lang.String r6 = r2.substring(r5, r3)
            java.lang.String r8 = "hsr"
            boolean r6 = r8.equals(r6)
            if (r6 == 0) goto L_0x00f6
        L_0x00bd:
            java.lang.String r3 = r2.substring(r3)
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r8 = "HFR :"
            r6.append(r8)
            r6.append(r2)
            java.lang.String r2 = " : rate = "
            r6.append(r2)
            r6.append(r3)
            java.lang.String r2 = r6.toString()
            android.util.Log.i(r7, r2)
            int r2 = java.lang.Integer.parseInt(r3)     // Catch:{ NumberFormatException -> 0x00e2 }
            goto L_0x00f7
        L_0x00e2:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r6 = "Invalid hfr rate "
            r2.append(r6)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.e(r7, r2)
        L_0x00f6:
            r2 = r5
        L_0x00f7:
            boolean r3 = r9.mCaptureTimeLapse
            if (r3 == 0) goto L_0x0100
            int r2 = com.android.camera.CameraSettings.getTimeLapseQualityFor(r0)
            goto L_0x0129
        L_0x0100:
            if (r2 <= 0) goto L_0x0128
            int r2 = com.android.camera.CameraSettings.getHighSpeedQualityFor(r0)
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r6 = "NOTE: HighSpeed quality ("
            r3.append(r6)
            r3.append(r2)
            java.lang.String r6 = ") for ("
            r3.append(r6)
            r3.append(r0)
            java.lang.String r6 = ")"
            r3.append(r6)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r7, r3)
            goto L_0x0129
        L_0x0128:
            r2 = r0
        L_0x0129:
            int r3 = r9.mCameraId
            boolean r3 = android.media.CamcorderProfile.hasProfile(r3, r2)
            if (r3 == 0) goto L_0x0133
            r0 = r2
            goto L_0x014f
        L_0x0133:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r6 = "NOTE: Quality "
            r3.append(r6)
            r3.append(r2)
            java.lang.String r2 = " is not supported ! Will use "
            r3.append(r2)
            r3.append(r0)
            java.lang.String r2 = r3.toString()
            android.util.Log.e(r7, r2)
        L_0x014f:
            int r2 = r9.mCameraId
            android.media.CamcorderProfile r0 = android.media.CamcorderProfile.get(r2, r0)
            r9.mProfile = r0
            r9.getDesiredPreviewSize()
            r9.qcomReadVideoPreferences()
            java.lang.String r0 = "android.intent.extra.durationLimit"
            boolean r2 = r1.hasExtra(r0)
            if (r2 == 0) goto L_0x016d
            int r0 = r1.getIntExtra(r0, r5)
            int r0 = r0 * 1000
            r9.mMaxVideoDurationInMs = r0
        L_0x016d:
            r9.mPreferenceRead = r4
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.VideoModule.readVideoPreferences():void");
    }

    public boolean is4KEnabled() {
        int i = this.mProfile.quality;
        return i == 8 || i == 1008 || i == CamcorderProfileWrapper.QUALITY_4KDCI;
    }

    private boolean is1080pEnabled() {
        return this.mProfile.quality == 6;
    }

    private boolean is720pEnabled() {
        return this.mProfile.quality == 5;
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

    /* access modifiers changed from: 0000 */
    public boolean isHFREnabled(int i, int i2) {
        ComboPreferences comboPreferences = this.mPreferences;
        if (!(comboPreferences == null || this.mParameters == null)) {
            String string = comboPreferences.getString(CameraSettings.KEY_VIDEO_HIGH_FRAME_RATE, this.mActivity.getString(C0905R.string.pref_camera_hfr_default));
            if (!"off".equals(string)) {
                try {
                    if (isSupported(string.substring(3), ParametersWrapper.getSupportedVideoHighFrameRateModes(this.mParameters))) {
                        Size size = (Size) ParametersWrapper.getSupportedHfrSizes(this.mParameters).get(ParametersWrapper.getSupportedVideoHighFrameRateModes(this.mParameters).indexOf(string.substring(3)));
                        if (size != null && i <= size.width && i2 <= size.height) {
                            return isSessionSupportedByEncoder(i, i2, Integer.parseInt(string.substring(3)));
                        }
                    }
                } catch (IndexOutOfBoundsException | NullPointerException unused) {
                }
            }
        }
        return false;
    }

    @TargetApi(11)
    private void getDesiredPreviewSize() {
        CameraProxy cameraProxy = this.mCameraDevice;
        if (cameraProxy != null) {
            this.mParameters = cameraProxy.getParameters();
            if (this.mParameters.getSupportedVideoSizes() != null) {
                CamcorderProfile camcorderProfile = this.mProfile;
                if (!isHFREnabled(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight)) {
                    List supportedPreviewSizes = this.mParameters.getSupportedPreviewSizes();
                    Size preferredPreviewSizeForVideo = this.mParameters.getPreferredPreviewSizeForVideo();
                    int i = preferredPreviewSizeForVideo.width * preferredPreviewSizeForVideo.height;
                    Iterator it = supportedPreviewSizes.iterator();
                    while (it.hasNext()) {
                        Size size = (Size) it.next();
                        if (size.width * size.height > i) {
                            it.remove();
                        }
                    }
                    CameraActivity cameraActivity = this.mActivity;
                    CamcorderProfile camcorderProfile2 = this.mProfile;
                    Size optimalPreviewSize = CameraUtil.getOptimalPreviewSize((Activity) cameraActivity, supportedPreviewSizes, ((double) camcorderProfile2.videoFrameWidth) / ((double) camcorderProfile2.videoFrameHeight));
                    this.mDesiredPreviewWidth = optimalPreviewSize.width;
                    this.mDesiredPreviewHeight = optimalPreviewSize.height;
                    this.mUI.setPreviewSize(this.mDesiredPreviewWidth, this.mDesiredPreviewHeight);
                    StringBuilder sb = new StringBuilder();
                    sb.append("mDesiredPreviewWidth=");
                    sb.append(this.mDesiredPreviewWidth);
                    sb.append(". mDesiredPreviewHeight=");
                    sb.append(this.mDesiredPreviewHeight);
                    Log.v(TAG, sb.toString());
                }
            }
            CamcorderProfile camcorderProfile3 = this.mProfile;
            this.mDesiredPreviewWidth = camcorderProfile3.videoFrameWidth;
            this.mDesiredPreviewHeight = camcorderProfile3.videoFrameHeight;
            this.mUI.setPreviewSize(this.mDesiredPreviewWidth, this.mDesiredPreviewHeight);
            StringBuilder sb2 = new StringBuilder();
            sb2.append("mDesiredPreviewWidth=");
            sb2.append(this.mDesiredPreviewWidth);
            sb2.append(". mDesiredPreviewHeight=");
            sb2.append(this.mDesiredPreviewHeight);
            Log.v(TAG, sb2.toString());
        }
    }

    /* access modifiers changed from: 0000 */
    public void setPreviewFrameLayoutCameraOrientation() {
        try {
            if (CameraHolder.instance().getCameraInfo()[this.mCameraId].orientation % 180 == 0) {
                this.mUI.cameraOrientationPreviewResize(true);
            } else {
                this.mUI.cameraOrientationPreviewResize(false);
            }
        } catch (ArrayIndexOutOfBoundsException unused) {
            Log.w(TAG, "getCameraInfo occur ArrayIndexOutOfBoundsException");
        }
    }

    public void resizeForPreviewAspectRatio() {
        setPreviewFrameLayoutCameraOrientation();
        CamcorderProfile camcorderProfile = this.mProfile;
        if (camcorderProfile != null) {
            this.mUI.setAspectRatio(((double) camcorderProfile.videoFrameWidth) / ((double) camcorderProfile.videoFrameHeight));
        }
    }

    public void onSwitchSavePath() {
        this.mUI.setPreference("pref_camera_savepath_key", "1");
        RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.on_switch_save_path_to_sdcard, 0).show();
    }

    public void installIntentFilter() {
        if (this.mReceiver == null) {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_EJECT");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.MEDIA_SCANNER_STARTED");
            intentFilter.addDataScheme("file");
            this.mReceiver = new MyBroadcastReceiver();
            this.mActivity.registerReceiver(this.mReceiver, intentFilter);
        }
    }

    public void onResumeBeforeSuper() {
        this.mPaused = false;
    }

    public void onResumeAfterSuper() {
        this.mUI.enableShutter(false);
        this.mZoomValue = 0;
        this.mUI.showSurfaceView();
        this.mWasMute = ((AudioManager) this.mActivity.getSystemService("audio")).isMicrophoneMute();
        boolean z = this.mWasMute;
        boolean z2 = this.mIsMute;
        if (z != z2) {
            setMute(z2, false);
        }
        showVideoSnapshotUI(false);
        installIntentFilter();
        if (!this.mPreviewing) {
            openCamera();
            if (this.mCameraDevice != null) {
                readVideoPreferences();
                resizeForPreviewAspectRatio();
                startPreview();
            } else {
                return;
            }
        } else {
            this.mUI.enableShutter(true);
        }
        initializeVideoControl();
        this.mUI.applySurfaceChange(SURFACE_STATUS.SURFACE_VIEW);
        this.mUI.initDisplayChangeListener();
        this.mUI.initializeZoom(this.mParameters);
        this.mUI.setPreviewGesturesVideoUI();
        this.mUI.setSwitcherIndex();
        keepScreenOnAwhile();
        this.mOrientationManager.resume();
        this.mLocationManager.recordLocation(RecordLocationPreference.get(this.mPreferences, "pref_camera_recordlocation_key"));
        if (this.mPreviewing) {
            this.mOnResumeTime = SystemClock.uptimeMillis();
            this.mHandler.sendEmptyMessageDelayed(3, 100);
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                VideoModule.this.mActivity.updateStorageSpaceAndHint();
            }
        });
    }

    private void setDisplayOrientation() {
        this.mDisplayRotation = CameraUtil.getDisplayRotation(this.mActivity);
        this.mCameraDisplayOrientation = CameraUtil.getDisplayOrientation(this.mDisplayRotation, this.mCameraId);
        this.mUI.setDisplayOrientation(this.mCameraDisplayOrientation);
        CameraProxy cameraProxy = this.mCameraDevice;
        if (cameraProxy != null) {
            cameraProxy.setDisplayOrientation(this.mCameraDisplayOrientation);
        }
    }

    public void updateCameraOrientation() {
        if (!this.mMediaRecorderRecording && this.mDisplayRotation != CameraUtil.getDisplayRotation(this.mActivity)) {
            setDisplayOrientation();
        }
    }

    public int onZoomChanged(int i) {
        if (this.mPaused) {
            return i;
        }
        this.mZoomValue = i;
        Parameters parameters = this.mParameters;
        if (!(parameters == null || this.mCameraDevice == null)) {
            parameters.setZoom(this.mZoomValue);
            this.mCameraDevice.setParameters(this.mParameters);
            Parameters parameters2 = this.mCameraDevice.getParameters();
            if (parameters2 != null) {
                return parameters2.getZoom();
            }
        }
        return i;
    }

    /* access modifiers changed from: private */
    public void startPreview() {
        String str = TAG;
        Log.v(str, "startPreview");
        this.mStartPrevPending = true;
        Log.v(str, "startPreview: SurfaceHolder (MDP path)");
        SurfaceHolder surfaceHolder = this.mUI.getSurfaceHolder();
        if (!this.mPreferenceRead || this.mPaused || this.mCameraDevice == null) {
            this.mStartPrevPending = false;
            return;
        }
        this.mErrorCallback.setActivity(this.mActivity);
        this.mCameraDevice.setErrorCallback(this.mErrorCallback);
        if (this.mPreviewing) {
            stopPreview();
        }
        setDisplayOrientation();
        this.mCameraDevice.setDisplayOrientation(this.mCameraDisplayOrientation);
        setCameraParameters(true);
        try {
            this.mCameraDevice.setPreviewDisplay(surfaceHolder);
            this.mCameraDevice.setOneShotPreviewCallback(this.mHandler, new CameraPreviewDataCallback() {
                public void onPreviewFrame(byte[] bArr, CameraProxy cameraProxy) {
                    VideoModule.this.mUI.hidePreviewCover();
                }
            });
            this.mCameraDevice.startPreview();
            this.mPreviewing = true;
            onPreviewStarted();
            this.mStartPrevPending = false;
        } catch (Throwable th) {
            closeCamera();
            throw new RuntimeException("startPreview failed", th);
        }
    }

    private void onPreviewStarted() {
        this.mUI.enableShutter(true);
        startFaceDetection();
    }

    public void stopPreview() {
        this.mStopPrevPending = true;
        if (!this.mPreviewing) {
            this.mStopPrevPending = false;
            return;
        }
        this.mCameraDevice.stopPreview();
        this.mPreviewing = false;
        this.mStopPrevPending = false;
        this.mUI.enableShutter(false);
        stopFaceDetection();
    }

    private void closeCamera() {
        String str = TAG;
        Log.v(str, "closeCamera");
        CameraProxy cameraProxy = this.mCameraDevice;
        if (cameraProxy == null) {
            Log.d(str, "already stopped.");
            return;
        }
        cameraProxy.setZoomChangeListener(null);
        this.mCameraDevice.setErrorCallback(null);
        this.mCameraDevice.setFaceDetectionCallback(null, null);
        if (this.mActivity.isForceReleaseCamera()) {
            CameraHolder.instance().strongRelease();
        } else {
            CameraHolder.instance().release();
        }
        this.mCameraDevice = null;
        this.mPreviewing = false;
        this.mSnapshotInProgress = false;
        this.mPreviewFocused = false;
        this.mFaceDetectionStarted = false;
    }

    private void releasePreviewResources() {
        if (!ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {
            this.mUI.hideSurfaceView();
        }
    }

    public void onPauseBeforeSuper() {
        this.mPaused = true;
        this.mUI.showPreviewCover();
        this.mUI.hideSurfaceView();
        if (this.mMediaRecorderRecording) {
            onStopVideoRecording();
        } else {
            closeCamera();
            releaseMediaRecorder();
        }
        closeVideoFileDescriptor();
        releasePreviewResources();
        BroadcastReceiver broadcastReceiver = this.mReceiver;
        if (broadcastReceiver != null) {
            this.mActivity.unregisterReceiver(broadcastReceiver);
            this.mReceiver = null;
        }
        resetScreenOn();
        LocationManager locationManager = this.mLocationManager;
        if (locationManager != null) {
            locationManager.recordLocation(false);
        }
        this.mOrientationManager.pause();
        this.mHandler.removeMessages(3);
        this.mHandler.removeMessages(8);
        this.mHandler.removeMessages(9);
        this.mHandler.removeMessages(10);
        this.mPendingSwitchCameraId = -1;
        this.mSwitchingCamera = false;
        this.mPreferenceRead = false;
        this.mUI.collapseCameraControls();
        this.mUI.removeDisplayChangeListener();
        boolean z = this.mWasMute;
        if (z != this.mIsMute) {
            setMute(z, false);
        }
        this.mUI.applySurfaceChange(SURFACE_STATUS.HIDE);
    }

    public void onUserInteraction() {
        if (!this.mMediaRecorderRecording && !this.mActivity.isFinishing()) {
            keepScreenOnAwhile();
        }
    }

    public boolean onBackPressed() {
        if (this.mPaused) {
            return true;
        }
        if (this.mMediaRecorderRecording) {
            onStopVideoRecording();
            return true;
        } else if (this.mUI.hideSwitcherPopup()) {
            return true;
        } else {
            return this.mUI.onBackPressed();
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (this.mPaused) {
            return true;
        }
        if (i != 23) {
            if (i != 27) {
                if (i == 82 && this.mMediaRecorderRecording) {
                    return true;
                }
            } else if (keyEvent.getRepeatCount() == 0) {
                this.mUI.clickShutter();
                return true;
            }
        } else if (keyEvent.getRepeatCount() == 0) {
            this.mUI.clickShutter();
            return true;
        }
        return false;
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (i != 27) {
            return false;
        }
        this.mUI.pressShutter(false);
        return true;
    }

    public boolean isVideoCaptureIntent() {
        return "android.media.action.VIDEO_CAPTURE".equals(this.mActivity.getIntent().getAction());
    }

    private void doReturnToCaller(boolean z) {
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

    private void setupMediaRecorderPreviewDisplay() {
        if (!ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {
            stopPreview();
            this.mCameraDevice.setPreviewDisplay(this.mUI.getSurfaceHolder());
            this.mCameraDevice.setDisplayOrientation(CameraUtil.getDisplayOrientation(this.mDisplayRotation, this.mCameraId));
            this.mCameraDevice.startPreview();
            this.mPreviewing = true;
            this.mMediaRecorder.setPreviewDisplay(this.mUI.getSurfaceHolder().getSurface());
        }
    }

    private int getHighSpeedVideoEncoderBitRate(CamcorderProfile camcorderProfile, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append(camcorderProfile.videoFrameWidth);
        sb.append("x");
        sb.append(camcorderProfile.videoFrameHeight);
        String str = ":";
        sb.append(str);
        sb.append(i);
        String sb2 = sb.toString();
        StringBuilder sb3 = new StringBuilder();
        sb3.append(sb2);
        sb3.append(str);
        sb3.append(camcorderProfile.videoCodec);
        String sb4 = sb3.toString();
        if (CameraSettings.VIDEO_ENCODER_BITRATE.containsKey(sb4)) {
            return ((Integer) CameraSettings.VIDEO_ENCODER_BITRATE.get(sb4)).intValue();
        }
        if (CameraSettings.VIDEO_ENCODER_BITRATE.containsKey(sb2)) {
            return ((Integer) CameraSettings.VIDEO_ENCODER_BITRATE.get(sb2)).intValue();
        }
        StringBuilder sb5 = new StringBuilder();
        sb5.append("No pre-defined bitrate for ");
        sb5.append(sb2);
        Log.i(TAG, sb5.toString());
        return camcorderProfile.videoBitRate * (i / camcorderProfile.videoFrameRate);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0059, code lost:
        if (r3 >= r4.mMinFrameHeight) goto L_0x00cd;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initializeRecorder() {
        /*
            r13 = this;
            java.lang.String r0 = "CAM_VideoModule"
            java.lang.String r1 = "initializeRecorder"
            android.util.Log.v(r0, r1)
            com.android.camera.CameraManager$CameraProxy r1 = r13.mCameraDevice
            if (r1 != 0) goto L_0x000c
            return
        L_0x000c:
            boolean r1 = com.android.camera.util.ApiHelper.HAS_SURFACE_TEXTURE_RECORDING
            if (r1 != 0) goto L_0x0015
            com.android.camera.VideoUI r1 = r13.mUI
            r1.showSurfaceView()
        L_0x0015:
            com.android.camera.CameraActivity r1 = r13.mActivity
            android.content.Intent r1 = r1.getIntent()
            android.os.Bundle r1 = r1.getExtras()
            android.media.CamcorderProfile r2 = r13.mProfile
            int r3 = r2.videoFrameWidth
            r13.videoWidth = r3
            int r2 = r2.videoFrameHeight
            r13.videoHeight = r2
            r2 = 0
            r13.mUnsupportedResolution = r2
            java.util.List r3 = android.media.EncoderCapabilities.getVideoEncoders()
            java.util.Iterator r3 = r3.iterator()
        L_0x0034:
            boolean r4 = r3.hasNext()
            r5 = 1
            if (r4 == 0) goto L_0x00cd
            java.lang.Object r4 = r3.next()
            android.media.EncoderCapabilities$VideoEncoderCap r4 = (android.media.EncoderCapabilities.VideoEncoderCap) r4
            int r6 = r4.mCodec
            int r7 = r13.mVideoEncoder
            if (r6 != r7) goto L_0x0034
            int r3 = r13.videoWidth
            int r6 = r4.mMaxFrameWidth
            if (r3 > r6) goto L_0x005b
            int r6 = r4.mMinFrameWidth
            if (r3 < r6) goto L_0x005b
            int r3 = r13.videoHeight
            int r6 = r4.mMaxFrameHeight
            if (r3 > r6) goto L_0x005b
            int r6 = r4.mMinFrameHeight
            if (r3 >= r6) goto L_0x00cd
        L_0x005b:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Selected codec "
            r1.append(r2)
            int r2 = r13.mVideoEncoder
            r1.append(r2)
            java.lang.String r2 = " does not support "
            r1.append(r2)
            int r2 = r13.videoWidth
            r1.append(r2)
            java.lang.String r2 = "x"
            r1.append(r2)
            int r2 = r13.videoHeight
            r1.append(r2)
            java.lang.String r2 = " resolution"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.e(r0, r1)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Codec capabilities: mMinFrameWidth = "
            r1.append(r2)
            int r2 = r4.mMinFrameWidth
            r1.append(r2)
            java.lang.String r2 = " , mMinFrameHeight = "
            r1.append(r2)
            int r2 = r4.mMinFrameHeight
            r1.append(r2)
            java.lang.String r2 = " , mMaxFrameWidth = "
            r1.append(r2)
            int r2 = r4.mMaxFrameWidth
            r1.append(r2)
            java.lang.String r2 = " , mMaxFrameHeight = "
            r1.append(r2)
            int r2 = r4.mMaxFrameHeight
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.e(r0, r1)
            r13.mUnsupportedResolution = r5
            com.android.camera.CameraActivity r13 = r13.mActivity
            r0 = 2131689587(0x7f0f0073, float:1.9008194E38)
            com.android.camera.ui.RotateTextToast r13 = com.android.camera.p004ui.RotateTextToast.makeText(r13, r0, r5)
            r13.show()
            return
        L_0x00cd:
            r13.closeVideoFileDescriptor()
            r13.mCurrentVideoUriFromMediaSaved = r2
            boolean r3 = r13.mIsVideoCaptureIntent
            r6 = 0
            if (r3 == 0) goto L_0x0100
            if (r1 == 0) goto L_0x0100
            java.lang.String r3 = "output"
            android.os.Parcelable r3 = r1.getParcelable(r3)
            android.net.Uri r3 = (android.net.Uri) r3
            if (r3 == 0) goto L_0x00f9
            android.content.ContentResolver r4 = r13.mContentResolver     // Catch:{ FileNotFoundException -> 0x00f1 }
            java.lang.String r8 = "rw"
            android.os.ParcelFileDescriptor r4 = r4.openFileDescriptor(r3, r8)     // Catch:{ FileNotFoundException -> 0x00f1 }
            r13.mVideoFileDescriptor = r4     // Catch:{ FileNotFoundException -> 0x00f1 }
            r13.mCurrentVideoUri = r3     // Catch:{ FileNotFoundException -> 0x00f1 }
            goto L_0x00f9
        L_0x00f1:
            r3 = move-exception
            java.lang.String r3 = r3.toString()
            android.util.Log.e(r0, r3)
        L_0x00f9:
            java.lang.String r3 = "android.intent.extra.sizeLimit"
            long r3 = r1.getLong(r3)
            goto L_0x0101
        L_0x0100:
            r3 = r6
        L_0x0101:
            android.media.MediaRecorder r1 = new android.media.MediaRecorder
            r1.<init>()
            r13.mMediaRecorder = r1
            com.android.camera.CameraManager$CameraProxy r1 = r13.mCameraDevice
            r1.unlock()
            android.media.MediaRecorder r1 = r13.mMediaRecorder
            com.android.camera.CameraManager$CameraProxy r8 = r13.mCameraDevice
            android.hardware.Camera r8 = r8.getCamera()
            r1.setCamera(r8)
            android.hardware.Camera$Parameters r1 = r13.mParameters
            java.lang.String r1 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getVideoHighFrameRate(r1)
            android.hardware.Camera$Parameters r8 = r13.mParameters
            java.lang.String r9 = "video-hsr"
            java.lang.String r8 = r8.get(r9)
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "NOTE: hfr = "
            r9.append(r10)
            r9.append(r1)
            java.lang.String r10 = " : hsr = "
            r9.append(r10)
            r9.append(r8)
            java.lang.String r9 = r9.toString()
            android.util.Log.i(r0, r9)
            java.lang.String r9 = "off"
            if (r1 == 0) goto L_0x014e
            boolean r10 = r1.equals(r9)
            if (r10 != 0) goto L_0x014e
            r10 = r5
            goto L_0x014f
        L_0x014e:
            r10 = r2
        L_0x014f:
            if (r8 == 0) goto L_0x0159
            boolean r9 = r8.equals(r9)
            if (r9 != 0) goto L_0x0159
            r9 = r5
            goto L_0x015a
        L_0x0159:
            r9 = r2
        L_0x015a:
            if (r10 == 0) goto L_0x0161
            int r1 = java.lang.Integer.parseInt(r1)     // Catch:{ NumberFormatException -> 0x0168 }
            goto L_0x018a
        L_0x0161:
            if (r9 == 0) goto L_0x0189
            int r1 = java.lang.Integer.parseInt(r8)     // Catch:{ NumberFormatException -> 0x0168 }
            goto L_0x018a
        L_0x0168:
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r12 = "Invalid hfr("
            r11.append(r12)
            r11.append(r1)
            java.lang.String r1 = ") or hsr("
            r11.append(r1)
            r11.append(r8)
            java.lang.String r1 = ")"
            r11.append(r1)
            java.lang.String r1 = r11.toString()
            android.util.Log.e(r0, r1)
        L_0x0189:
            r1 = r2
        L_0x018a:
            android.media.MediaRecorder r8 = r13.mMediaRecorder
            r8.setVideoSource(r5)
            android.media.CamcorderProfile r8 = r13.mProfile
            int r11 = r13.mVideoEncoder
            r8.videoCodec = r11
            int r11 = r13.mAudioEncoder
            r8.audioCodec = r11
            int r11 = r13.mMaxVideoDurationInMs
            r8.duration = r11
            int r11 = r8.audioCodec
            if (r11 != r5) goto L_0x01a9
            boolean r11 = r13.mCaptureTimeLapse
            if (r11 != 0) goto L_0x01a9
            if (r10 != 0) goto L_0x01a9
            r8.fileFormat = r5
        L_0x01a9:
            r5 = 5
            if (r10 != 0) goto L_0x01ae
            if (r9 == 0) goto L_0x020c
        L_0x01ae:
            if (r1 <= 0) goto L_0x020c
            if (r9 == 0) goto L_0x01bc
            java.lang.String r8 = "Enabling audio for HSR"
            android.util.Log.i(r0, r8)
            android.media.MediaRecorder r8 = r13.mMediaRecorder
            r8.setAudioSource(r5)
        L_0x01bc:
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            android.media.CamcorderProfile r8 = r13.mProfile
            int r8 = r8.fileFormat
            r5.setOutputFormat(r8)
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            android.media.CamcorderProfile r8 = r13.mProfile
            int r8 = r8.videoFrameRate
            r5.setVideoFrameRate(r8)
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            android.media.CamcorderProfile r8 = r13.mProfile
            int r8 = r8.videoBitRate
            r5.setVideoEncodingBitRate(r8)
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            android.media.CamcorderProfile r8 = r13.mProfile
            int r8 = r8.videoCodec
            r5.setVideoEncoder(r8)
            if (r9 == 0) goto L_0x021c
            java.lang.String r5 = "Configuring audio for HSR"
            android.util.Log.i(r0, r5)
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            android.media.CamcorderProfile r8 = r13.mProfile
            int r8 = r8.audioBitRate
            r5.setAudioEncodingBitRate(r8)
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            android.media.CamcorderProfile r8 = r13.mProfile
            int r8 = r8.audioChannels
            r5.setAudioChannels(r8)
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            android.media.CamcorderProfile r8 = r13.mProfile
            int r8 = r8.audioSampleRate
            r5.setAudioSamplingRate(r8)
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            android.media.CamcorderProfile r8 = r13.mProfile
            int r8 = r8.audioCodec
            r5.setAudioEncoder(r8)
            goto L_0x021c
        L_0x020c:
            boolean r8 = r13.mCaptureTimeLapse
            if (r8 != 0) goto L_0x0215
            android.media.MediaRecorder r8 = r13.mMediaRecorder
            r8.setAudioSource(r5)
        L_0x0215:
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            android.media.CamcorderProfile r8 = r13.mProfile
            r5.setProfile(r8)
        L_0x021c:
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            android.media.CamcorderProfile r8 = r13.mProfile
            int r11 = r8.videoFrameWidth
            int r8 = r8.videoFrameHeight
            r5.setVideoSize(r11, r8)
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            int r8 = r13.mMaxVideoDurationInMs
            r5.setMaxDuration(r8)
            boolean r5 = r13.mCaptureTimeLapse
            if (r5 == 0) goto L_0x0241
            r8 = 4652007308841189376(0x408f400000000000, double:1000.0)
            int r1 = r13.mTimeBetweenTimeLapseFrameCaptureMs
            double r10 = (double) r1
            double r8 = r8 / r10
            android.media.MediaRecorder r1 = r13.mMediaRecorder
            setCaptureRate(r1, r8)
            goto L_0x02a1
        L_0x0241:
            if (r1 <= 0) goto L_0x02a1
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r8 = "Setting capture-rate = "
            r5.append(r8)
            r5.append(r1)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r0, r5)
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            double r11 = (double) r1
            r5.setCaptureRate(r11)
            if (r9 == 0) goto L_0x0260
            goto L_0x0269
        L_0x0260:
            if (r10 == 0) goto L_0x0265
            r1 = 30
            goto L_0x0269
        L_0x0265:
            android.media.CamcorderProfile r1 = r13.mProfile
            int r1 = r1.videoFrameRate
        L_0x0269:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r8 = "Setting target fps = "
            r5.append(r8)
            r5.append(r1)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r0, r5)
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            r5.setVideoFrameRate(r1)
            android.media.CamcorderProfile r5 = r13.mProfile
            int r1 = r13.getHighSpeedVideoEncoderBitRate(r5, r1)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r8 = "Scaled Video bitrate : "
            r5.append(r8)
            r5.append(r1)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r0, r5)
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            r5.setVideoEncodingBitRate(r1)
        L_0x02a1:
            r13.setRecordLocation()
            android.os.ParcelFileDescriptor r1 = r13.mVideoFileDescriptor
            if (r1 == 0) goto L_0x02b2
            android.media.MediaRecorder r5 = r13.mMediaRecorder
            java.io.FileDescriptor r1 = r1.getFileDescriptor()
            r5.setOutputFile(r1)
            goto L_0x02c0
        L_0x02b2:
            android.media.CamcorderProfile r1 = r13.mProfile
            int r1 = r1.fileFormat
            r13.generateVideoFilename(r1)
            android.media.MediaRecorder r1 = r13.mMediaRecorder
            java.lang.String r5 = r13.mVideoFilename
            r1.setOutputFile(r5)
        L_0x02c0:
            com.android.camera.CameraActivity r1 = r13.mActivity
            long r8 = r1.getStorageSpaceBytes()
            r10 = 104857600(0x6400000, double:5.1806538E-316)
            long r8 = r8 - r10
            int r1 = (r3 > r6 ? 1 : (r3 == r6 ? 0 : -1))
            if (r1 <= 0) goto L_0x02d3
            int r1 = (r3 > r8 ? 1 : (r3 == r8 ? 0 : -1))
            if (r1 >= 0) goto L_0x02d3
            goto L_0x02d4
        L_0x02d3:
            r3 = r8
        L_0x02d4:
            boolean r1 = com.android.camera.Storage.isSaveSDCard()
            r5 = -100663296(0xfffffffffa000000, double:NaN)
            if (r1 == 0) goto L_0x02e2
            int r1 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
            if (r1 <= 0) goto L_0x02e2
            r3 = r5
        L_0x02e2:
            android.media.MediaRecorder r1 = r13.mMediaRecorder     // Catch:{ RuntimeException -> 0x02e7 }
            r1.setMaxFileSize(r3)     // Catch:{ RuntimeException -> 0x02e7 }
        L_0x02e7:
            int r1 = r13.mOrientation
            r3 = -1
            if (r1 == r3) goto L_0x030d
            com.android.camera.CameraHolder r1 = com.android.camera.CameraHolder.instance()
            com.android.camera.CameraHolder$CameraInfo[] r1 = r1.getCameraInfo()
            int r2 = r13.mCameraId
            r1 = r1[r2]
            int r2 = r1.facing
            if (r2 != 0) goto L_0x0306
            int r1 = r1.orientation
            int r2 = r13.mOrientation
            int r1 = r1 - r2
            int r1 = r1 + 360
            int r2 = r1 % 360
            goto L_0x030d
        L_0x0306:
            int r1 = r1.orientation
            int r2 = r13.mOrientation
            int r1 = r1 + r2
            int r2 = r1 % 360
        L_0x030d:
            android.media.MediaRecorder r1 = r13.mMediaRecorder
            r1.setOrientationHint(r2)
            r13.setupMediaRecorderPreviewDisplay()
            android.media.MediaRecorder r1 = r13.mMediaRecorder     // Catch:{ IOException -> 0x0325 }
            r1.prepare()     // Catch:{ IOException -> 0x0325 }
            android.media.MediaRecorder r0 = r13.mMediaRecorder
            r0.setOnErrorListener(r13)
            android.media.MediaRecorder r0 = r13.mMediaRecorder
            r0.setOnInfoListener(r13)
            return
        L_0x0325:
            r1 = move-exception
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "prepare failed for "
            r2.append(r3)
            java.lang.String r3 = r13.mVideoFilename
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.e(r0, r2, r1)
            r13.releaseMediaRecorder()
            java.lang.RuntimeException r13 = new java.lang.RuntimeException
            r13.<init>(r1)
            throw r13
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.VideoModule.initializeRecorder():void");
    }

    private static void setCaptureRate(MediaRecorder mediaRecorder, double d) {
        mediaRecorder.setCaptureRate(d);
    }

    private void setRecordLocation() {
        Location currentLocation = this.mLocationManager.getCurrentLocation();
        if (currentLocation != null) {
            this.mMediaRecorder.setLocation((float) currentLocation.getLatitude(), (float) currentLocation.getLongitude());
        }
    }

    private void releaseMediaRecorder() {
        Log.v(TAG, "Releasing media recorder.");
        if (this.mMediaRecorder != null) {
            cleanupEmptyFile();
            this.mMediaRecorder.reset();
            this.mMediaRecorder.release();
            this.mMediaRecorder = null;
        }
        this.mVideoFilename = null;
    }

    private void generateVideoFilename(int i) {
        String str;
        long currentTimeMillis = System.currentTimeMillis();
        String createName = createName(currentTimeMillis);
        StringBuilder sb = new StringBuilder();
        sb.append(createName);
        sb.append(convertOutputFormatToFileExt(i));
        String sb2 = sb.toString();
        String convertOutputFormatToMimeType = convertOutputFormatToMimeType(i);
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
        sb5.append(Integer.toString(this.mProfile.videoFrameWidth));
        sb5.append("x");
        sb5.append(Integer.toString(this.mProfile.videoFrameHeight));
        contentValues.put("resolution", sb5.toString());
        Location currentLocation = this.mLocationManager.getCurrentLocation();
        if (currentLocation != null) {
            this.mCurrentVideoValues.put("latitude", Double.valueOf(currentLocation.getLatitude()));
            this.mCurrentVideoValues.put("longitude", Double.valueOf(currentLocation.getLongitude()));
        }
        this.mVideoFilename = str;
        StringBuilder sb6 = new StringBuilder();
        sb6.append("New video filename: ");
        sb6.append(this.mVideoFilename);
        Log.v(TAG, sb6.toString());
    }

    private void saveVideo() {
        if (this.mVideoFileDescriptor == null) {
            File file = new File(this.mCurrentVideoFilename);
            boolean exists = file.exists();
            String str = TAG;
            if (exists) {
                long j = 0;
                if (file.length() > 0) {
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    try {
                        mediaMetadataRetriever.setDataSource(this.mCurrentVideoFilename);
                        j = Long.valueOf(mediaMetadataRetriever.extractMetadata(9)).longValue();
                    } catch (IllegalArgumentException unused) {
                        Log.e(str, "cannot access the file");
                    }
                    long j2 = j;
                    mediaMetadataRetriever.release();
                    this.mActivity.getMediaSaveService().addVideo(this.mCurrentVideoFilename, j2, this.mCurrentVideoValues, this.mOnVideoSavedListener, this.mContentResolver);
                }
            }
            Log.e(str, "Invalid file");
            this.mCurrentVideoValues = null;
            return;
        }
        this.mCurrentVideoValues = null;
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

    private PreferenceGroup filterPreferenceScreenByIntent(PreferenceGroup preferenceGroup) {
        Intent intent = this.mActivity.getIntent();
        String str = "pref_video_quality_key";
        if (intent.hasExtra("android.intent.extra.videoQuality")) {
            CameraSettings.removePreferenceFromScreen(preferenceGroup, str);
        }
        if (intent.hasExtra("android.intent.extra.durationLimit")) {
            CameraSettings.removePreferenceFromScreen(preferenceGroup, str);
        }
        return preferenceGroup;
    }

    public void onError(MediaRecorder mediaRecorder, int i, int i2) {
        StringBuilder sb = new StringBuilder();
        sb.append("MediaRecorder error. what=");
        sb.append(i);
        sb.append(". extra=");
        sb.append(i2);
        Log.e(TAG, sb.toString());
        stopVideoRecording();
        this.mUI.showUIafterRecording();
        if (i == 1) {
            this.mActivity.updateStorageSpaceAndHint();
        }
    }

    public void onInfo(MediaRecorder mediaRecorder, int i, int i2) {
        if (i == 800) {
            if (this.mMediaRecorderRecording) {
                onStopVideoRecording();
            }
        } else if (i == 801) {
            if (this.mMediaRecorderRecording) {
                onStopVideoRecording();
            }
            RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.video_reach_size_limit, 1).show();
        }
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

    public boolean isRecording() {
        return this.mMediaRecorderRecording;
    }

    private boolean startVideoRecording() {
        String str = TAG;
        Log.v(str, "startVideoRecording");
        this.mStartRecPending = true;
        this.mUI.cancelAnimations();
        this.mUI.setSwipingEnabled(false);
        this.mUI.hideUIwhileRecording();
        if (this.mUI.isPreviewCoverVisible()) {
            this.mUI.hidePreviewCover();
        }
        this.mActivity.updateStorageSpaceAndHint();
        if (this.mActivity.getStorageSpaceBytes() <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
            Log.v(str, "Storage issue, ignore the start request");
            this.mStartRecPending = false;
            return false;
        } else if (this.mUnsupportedHFRVideoSize) {
            Log.e(str, "Unsupported HFR and video size combinations");
            RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.error_app_unsupported_hfr, 0).show();
            this.mStartRecPending = false;
            return false;
        } else if (this.mUnsupportedHSRVideoSize) {
            Log.e(str, "Unsupported HSR and video size combinations");
            RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.error_app_unsupported_hsr, 0).show();
            this.mStartRecPending = false;
            return false;
        } else if (this.mUnsupportedHFRVideoCodec) {
            Log.e(str, "Unsupported HFR and video codec combinations");
            RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.error_app_unsupported_hfr_codec, 0).show();
            this.mStartRecPending = false;
            return false;
        } else if (this.mUnsupportedProfile) {
            Log.e(str, "Unsupported video profile");
            RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.error_app_unsupported_profile, 0).show();
            this.mStartRecPending = false;
            return false;
        } else {
            this.mCurrentVideoUri = null;
            initializeRecorder();
            if (this.mUnsupportedResolution) {
                Log.v(str, "Unsupported Resolution according to target");
                this.mStartRecPending = false;
                return false;
            } else if (this.mMediaRecorder == null) {
                Log.e(str, "Fail to initialize media recorder");
                this.mStartRecPending = false;
                return false;
            } else {
                requestAudioFocus();
                try {
                    this.mMediaRecorder.start();
                    AccessibilityUtils.makeAnnouncement(this.mUI.getShutterButton(), this.mActivity.getString(C0905R.string.video_recording_started));
                    this.mCameraDevice.refreshParameters();
                    this.mParameters = this.mCameraDevice.getParameters();
                    this.mUI.enableCameraControls(false);
                    this.mMediaRecorderRecording = true;
                    this.mMediaRecorderPausing = false;
                    this.mUI.resetPauseButton();
                    this.mRecordingTotalTime = 0;
                    this.mRecordingStartTime = SystemClock.uptimeMillis();
                    this.mUI.showRecordingUI(true);
                    updateRecordingTime();
                    keepScreenOn();
                    this.mStartRecPending = false;
                    return true;
                } catch (RuntimeException unused) {
                    Toast.makeText(this.mActivity, "Could not start media recorder.\n Can't start video recording.", 1).show();
                    releaseMediaRecorder();
                    releaseAudioFocus();
                    this.mCameraDevice.lock();
                    this.mStartRecPending = false;
                    return false;
                }
            }
        }
    }

    private Bitmap getVideoThumbnail() {
        Bitmap bitmap;
        ParcelFileDescriptor parcelFileDescriptor = this.mVideoFileDescriptor;
        if (parcelFileDescriptor != null) {
            bitmap = Thumbnail.createVideoThumbnailBitmap(parcelFileDescriptor.getFileDescriptor(), this.mDesiredPreviewWidth);
        } else {
            Uri uri = this.mCurrentVideoUri;
            if (uri != null) {
                try {
                    this.mVideoFileDescriptor = this.mContentResolver.openFileDescriptor(uri, "r");
                    bitmap = Thumbnail.createVideoThumbnailBitmap(this.mVideoFileDescriptor.getFileDescriptor(), this.mDesiredPreviewWidth);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.toString());
                }
            }
            bitmap = null;
        }
        if (bitmap == null) {
            return bitmap;
        }
        return CameraUtil.rotateAndMirror(bitmap, 0, CameraHolder.instance().getCameraInfo()[this.mCameraId].facing == 0);
    }

    private void showCaptureResult() {
        this.mIsInReviewMode = true;
        Bitmap videoThumbnail = getVideoThumbnail();
        if (videoThumbnail != null) {
            this.mUI.showReviewImage(videoThumbnail);
        }
        this.mUI.showReviewControls();
        this.mUI.enableCameraControls(false);
        this.mUI.showTimeLapseUI(false);
    }

    private void pauseVideoRecording() {
        Log.v(TAG, "pauseVideoRecording");
        this.mMediaRecorderPausing = true;
        this.mRecordingTotalTime += SystemClock.uptimeMillis() - this.mRecordingStartTime;
        this.mMediaRecorder.pause();
    }

    private void resumeVideoRecording() {
        String str = TAG;
        Log.v(str, "resumeVideoRecording");
        this.mMediaRecorderPausing = false;
        this.mRecordingStartTime = SystemClock.uptimeMillis();
        updateRecordingTime();
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

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x006c  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x007b  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0087  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x009c  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00a0  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00b3  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00cb  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00d8  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean stopVideoRecording() {
        /*
            r6 = this;
            java.lang.String r0 = "CAM_VideoModule"
            java.lang.String r1 = "stopVideoRecording"
            android.util.Log.v(r0, r1)
            r1 = 1
            r6.mStopRecPending = r1
            com.android.camera.VideoUI r2 = r6.mUI
            r2.setSwipingEnabled(r1)
            boolean r2 = r6.isVideoCaptureIntent()
            if (r2 != 0) goto L_0x001a
            com.android.camera.VideoUI r2 = r6.mUI
            r2.showSwitcher()
        L_0x001a:
            boolean r2 = r6.mMediaRecorderRecording
            r3 = 0
            if (r2 == 0) goto L_0x00a8
            android.media.MediaRecorder r2 = r6.mMediaRecorder     // Catch:{ RuntimeException -> 0x0061 }
            r4 = 0
            r2.setOnErrorListener(r4)     // Catch:{ RuntimeException -> 0x0061 }
            android.media.MediaRecorder r2 = r6.mMediaRecorder     // Catch:{ RuntimeException -> 0x0061 }
            r2.setOnInfoListener(r4)     // Catch:{ RuntimeException -> 0x0061 }
            android.media.MediaRecorder r2 = r6.mMediaRecorder     // Catch:{ RuntimeException -> 0x0061 }
            r2.stop()     // Catch:{ RuntimeException -> 0x0061 }
            java.lang.String r2 = r6.mVideoFilename     // Catch:{ RuntimeException -> 0x005e }
            r6.mCurrentVideoFilename = r2     // Catch:{ RuntimeException -> 0x005e }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x005e }
            r2.<init>()     // Catch:{ RuntimeException -> 0x005e }
            java.lang.String r4 = "stopVideoRecording: Setting current video filename: "
            r2.append(r4)     // Catch:{ RuntimeException -> 0x005e }
            java.lang.String r4 = r6.mCurrentVideoFilename     // Catch:{ RuntimeException -> 0x005e }
            r2.append(r4)     // Catch:{ RuntimeException -> 0x005e }
            java.lang.String r2 = r2.toString()     // Catch:{ RuntimeException -> 0x005e }
            android.util.Log.v(r0, r2)     // Catch:{ RuntimeException -> 0x005e }
            com.android.camera.VideoUI r2 = r6.mUI     // Catch:{ RuntimeException -> 0x005e }
            android.view.View r2 = r2.getShutterButton()     // Catch:{ RuntimeException -> 0x005e }
            com.android.camera.CameraActivity r4 = r6.mActivity     // Catch:{ RuntimeException -> 0x005e }
            r5 = 2131690785(0x7f0f0521, float:1.9010623E38)
            java.lang.String r4 = r4.getString(r5)     // Catch:{ RuntimeException -> 0x005e }
            com.android.camera.util.AccessibilityUtils.makeAnnouncement(r2, r4)     // Catch:{ RuntimeException -> 0x005e }
            r4 = r1
            r0 = r3
            goto L_0x0070
        L_0x005e:
            r2 = move-exception
            r4 = r1
            goto L_0x0063
        L_0x0061:
            r2 = move-exception
            r4 = r3
        L_0x0063:
            java.lang.String r5 = "stop fail"
            android.util.Log.e(r0, r5, r2)
            java.lang.String r0 = r6.mVideoFilename
            if (r0 == 0) goto L_0x006f
            r6.deleteVideoFile(r0)
        L_0x006f:
            r0 = r1
        L_0x0070:
            r6.mMediaRecorderRecording = r3
            r6.mSnapshotInProgress = r3
            r6.showVideoSnapshotUI(r3)
            boolean r2 = r6.mPaused
            if (r2 == 0) goto L_0x007e
            r6.closeCamera()
        L_0x007e:
            com.android.camera.VideoUI r2 = r6.mUI
            r2.showRecordingUI(r3)
            boolean r2 = r6.mIsVideoCaptureIntent
            if (r2 != 0) goto L_0x008c
            com.android.camera.VideoUI r2 = r6.mUI
            r2.enableCameraControls(r1)
        L_0x008c:
            com.android.camera.VideoUI r2 = r6.mUI
            r2.setOrientationIndicator(r3, r1)
            r6.keepScreenOnAwhile()
            if (r4 == 0) goto L_0x00a9
            if (r0 != 0) goto L_0x00a9
            android.os.ParcelFileDescriptor r1 = r6.mVideoFileDescriptor
            if (r1 != 0) goto L_0x00a0
            r6.saveVideo()
            goto L_0x00a9
        L_0x00a0:
            boolean r1 = r6.mIsVideoCaptureIntent
            if (r1 == 0) goto L_0x00a9
            r6.showCaptureResult()
            goto L_0x00a9
        L_0x00a8:
            r0 = r3
        L_0x00a9:
            r6.releaseMediaRecorder()
            r6.releaseAudioFocus()
            boolean r1 = r6.mPaused
            if (r1 != 0) goto L_0x00c7
            com.android.camera.CameraManager$CameraProxy r1 = r6.mCameraDevice
            r1.lock()
            boolean r1 = com.android.camera.util.ApiHelper.HAS_SURFACE_TEXTURE_RECORDING
            if (r1 != 0) goto L_0x00c7
            r6.stopPreview()
            com.android.camera.VideoUI r1 = r6.mUI
            r1.hideSurfaceView()
            r6.startPreview()
        L_0x00c7:
            boolean r1 = r6.mPaused
            if (r1 != 0) goto L_0x00d3
            com.android.camera.CameraManager$CameraProxy r1 = r6.mCameraDevice
            android.hardware.Camera$Parameters r1 = r1.getParameters()
            r6.mParameters = r1
        L_0x00d3:
            boolean r1 = r6.mMediaRecorderPausing
            if (r1 == 0) goto L_0x00d8
            goto L_0x00db
        L_0x00d8:
            android.os.SystemClock.uptimeMillis()
        L_0x00db:
            r6.mStopRecPending = r3
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.VideoModule.stopVideoRecording():boolean");
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

    private void keepScreenOn() {
        this.mHandler.removeMessages(4);
        this.mActivity.getWindow().addFlags(128);
    }

    private static String millisecondToTimeString(long j, boolean z) {
        long j2 = j / 1000;
        long j3 = j2 / 60;
        long j4 = j3 / 60;
        long j5 = j3 - (j4 * 60);
        long j6 = j2 - (j3 * 60);
        StringBuilder sb = new StringBuilder();
        if (j4 > 0) {
            if (j4 < 10) {
                sb.append('0');
            }
            sb.append(j4);
            sb.append(':');
        }
        if (j5 < 10) {
            sb.append('0');
        }
        sb.append(j5);
        sb.append(':');
        if (j6 < 10) {
            sb.append('0');
        }
        sb.append(j6);
        if (z) {
            sb.append('.');
            long j7 = (j - (j2 * 1000)) / 10;
            if (j7 < 10) {
                sb.append('0');
            }
            sb.append(j7);
        }
        return sb.toString();
    }

    private long getTimeLapseVideoLength(long j) {
        return (long) (((((double) j) / ((double) this.mTimeBetweenTimeLapseFrameCaptureMs)) / ((double) this.mProfile.videoFrameRate)) * 1000.0d);
    }

    /* access modifiers changed from: private */
    public void updateRecordingTime() {
        long j;
        String str;
        if (this.mMediaRecorderRecording && !this.mMediaRecorderPausing) {
            long uptimeMillis = (SystemClock.uptimeMillis() - this.mRecordingStartTime) + this.mRecordingTotalTime;
            int i = this.mMaxVideoDurationInMs;
            boolean z = i != 0 && uptimeMillis >= ((long) (i - 60000));
            long max = z ? Math.max(0, ((long) this.mMaxVideoDurationInMs) - uptimeMillis) + 999 : uptimeMillis;
            if (!this.mCaptureTimeLapse) {
                str = millisecondToTimeString(max, false);
                j = 1000;
            } else {
                str = millisecondToTimeString(getTimeLapseVideoLength(uptimeMillis), true);
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

    private static boolean isSupported(String str, List<String> list) {
        return list != null && list.indexOf(str) >= 0;
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
        if (previewFlip == 0 && videoFlip == 0 && pictureFlip == 0) {
            this.mIsFlipEnabled = false;
        } else {
            this.mIsFlipEnabled = true;
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

    /* JADX WARNING: Code restructure failed: missing block: B:139:0x0621, code lost:
        if (r8 > PERSIST_EIS_MAX_FPS) goto L_0x062e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x0629, code lost:
        if (r1.equals(r13) == false) goto L_0x062e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void qcomSetCameraParameters() {
        /*
            r18 = this;
            r0 = r18
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "NOTE: qcomSetCameraParameters "
            r1.append(r2)
            int r2 = r0.videoWidth
            r1.append(r2)
            java.lang.String r2 = " x "
            r1.append(r2)
            int r2 = r0.videoHeight
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r2 = "CAM_VideoModule"
            android.util.Log.i(r2, r1)
            r18.setZoomMenuValue()
            com.android.camera.ComboPreferences r1 = r0.mPreferences
            com.android.camera.CameraActivity r3 = r0.mActivity
            r4 = 2131690008(0x7f0f0218, float:1.9009047E38)
            java.lang.String r3 = r3.getString(r4)
            java.lang.String r4 = "pref_camera_coloreffect_key"
            java.lang.String r1 = r1.getString(r4, r3)
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "Color effect value ="
            r3.append(r4)
            r3.append(r1)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r2, r3)
            android.hardware.Camera$Parameters r3 = r0.mParameters
            java.util.List r3 = r3.getSupportedColorEffects()
            boolean r3 = isSupported(r1, r3)
            if (r3 == 0) goto L_0x005d
            android.hardware.Camera$Parameters r3 = r0.mParameters
            r3.setColorEffect(r1)
        L_0x005d:
            com.android.camera.ComboPreferences r1 = r0.mPreferences
            com.android.camera.CameraActivity r3 = r0.mActivity
            r4 = 2131690047(0x7f0f023f, float:1.9009127E38)
            java.lang.String r3 = r3.getString(r4)
            java.lang.String r4 = "pref_camera_dis_key"
            java.lang.String r1 = r1.getString(r4, r3)
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r5 = "DIS value ="
            r3.append(r5)
            r3.append(r1)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r2, r3)
            java.lang.String r3 = "enable"
            boolean r5 = r1.equals(r3)
            r0.mIsDISEnabled = r5
            boolean r5 = r18.is4KEnabled()
            r6 = 2
            r7 = 1
            r8 = 0
            if (r5 == 0) goto L_0x00e8
            boolean r5 = PERSIST_4K_NO_LIMIT
            if (r5 != 0) goto L_0x00e8
            com.android.camera.CameraActivity r5 = r0.mActivity
            r9 = 2131690051(0x7f0f0243, float:1.9009135E38)
            java.lang.String r5 = r5.getString(r9)
            android.hardware.Camera$Parameters r10 = r0.mParameters
            java.util.List r10 = com.android.camera.CameraSettings.getSupportedDISModes(r10)
            boolean r5 = isSupported(r5, r10)
            if (r5 == 0) goto L_0x00cd
            android.hardware.Camera$Parameters r5 = r0.mParameters
            com.android.camera.CameraActivity r10 = r0.mActivity
            java.lang.String r10 = r10.getString(r9)
            java.lang.String r11 = "dis"
            r5.set(r11, r10)
            com.android.camera.VideoUI r5 = r0.mUI
            java.lang.String[] r10 = new java.lang.String[r6]
            r10[r8] = r4
            com.android.camera.CameraActivity r11 = r0.mActivity
            java.lang.String r9 = r11.getString(r9)
            r10[r7] = r9
            r5.overrideSettings(r10)
            r0.mIsDISEnabled = r8
            goto L_0x0110
        L_0x00cd:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r10 = "Not supported IS mode = "
            r5.append(r10)
            com.android.camera.CameraActivity r10 = r0.mActivity
            java.lang.String r9 = r10.getString(r9)
            r5.append(r9)
            java.lang.String r5 = r5.toString()
            android.util.Log.e(r2, r5)
            goto L_0x0110
        L_0x00e8:
            android.hardware.Camera$Parameters r5 = r0.mParameters
            java.util.List r5 = com.android.camera.CameraSettings.getSupportedDISModes(r5)
            boolean r5 = isSupported(r1, r5)
            if (r5 == 0) goto L_0x00fc
            android.hardware.Camera$Parameters r5 = r0.mParameters
            java.lang.String r9 = "dis"
            r5.set(r9, r1)
            goto L_0x0110
        L_0x00fc:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r9 = "Not supported IS mode = "
            r5.append(r9)
            r5.append(r1)
            java.lang.String r5 = r5.toString()
            android.util.Log.e(r2, r5)
        L_0x0110:
            java.lang.String r5 = r0.mDefaultAntibanding
            if (r5 != 0) goto L_0x0132
            android.hardware.Camera$Parameters r5 = r0.mParameters
            java.lang.String r5 = r5.getAntibanding()
            r0.mDefaultAntibanding = r5
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r9 = "default antibanding value = "
            r5.append(r9)
            java.lang.String r9 = r0.mDefaultAntibanding
            r5.append(r9)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r2, r5)
        L_0x0132:
            boolean r5 = r1.equals(r3)
            if (r5 == 0) goto L_0x0153
            java.lang.String r5 = "dis is enabled, set antibanding to auto."
            android.util.Log.d(r2, r5)
            android.hardware.Camera$Parameters r5 = r0.mParameters
            java.util.List r5 = r5.getSupportedAntibanding()
            java.lang.String r9 = "auto"
            boolean r5 = isSupported(r9, r5)
            if (r5 == 0) goto L_0x0168
            android.hardware.Camera$Parameters r5 = r0.mParameters
            java.lang.String r9 = "auto"
            r5.setAntibanding(r9)
            goto L_0x0168
        L_0x0153:
            java.lang.String r5 = r0.mDefaultAntibanding
            android.hardware.Camera$Parameters r9 = r0.mParameters
            java.util.List r9 = r9.getSupportedAntibanding()
            boolean r5 = isSupported(r5, r9)
            if (r5 == 0) goto L_0x0168
            android.hardware.Camera$Parameters r5 = r0.mParameters
            java.lang.String r9 = r0.mDefaultAntibanding
            r5.setAntibanding(r9)
        L_0x0168:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r9 = "antiBanding value = "
            r5.append(r9)
            android.hardware.Camera$Parameters r9 = r0.mParameters
            java.lang.String r9 = r9.getAntibanding()
            r5.append(r9)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r2, r5)
            r0.mUnsupportedHFRVideoSize = r8
            r0.mUnsupportedHFRVideoCodec = r8
            r0.mUnsupportedHSRVideoSize = r8
            boolean r5 = com.android.camera.util.PersistUtil.isYv12FormatEnable()
            if (r5 == 0) goto L_0x019b
            java.lang.String r5 = "preview format set to YV12"
            android.util.Log.v(r2, r5)
            android.hardware.Camera$Parameters r5 = r0.mParameters
            r9 = 842094169(0x32315659, float:1.0322389E-8)
            r5.setPreviewFormat(r9)
        L_0x019b:
            android.hardware.Camera$Parameters r5 = r0.mParameters
            java.lang.String r9 = "preview-format"
            java.lang.String r10 = "yuv420sp"
            r5.set(r9, r10)
            java.lang.String r5 = "preview format set to NV21"
            android.util.Log.v(r2, r5)
            com.android.camera.ComboPreferences r5 = r0.mPreferences
            com.android.camera.CameraActivity r9 = r0.mActivity
            r10 = 2131690094(0x7f0f026e, float:1.9009222E38)
            java.lang.String r9 = r9.getString(r10)
            java.lang.String r10 = "pref_camera_hfr_key"
            java.lang.String r5 = r5.getString(r10, r9)
            r9 = 3
            java.lang.String r10 = r5.substring(r8, r9)
            java.lang.String r11 = "hfr"
            boolean r10 = r11.equals(r10)
            java.lang.String r11 = r5.substring(r8, r9)
            java.lang.String r12 = "hsr"
            boolean r11 = r12.equals(r11)
            java.lang.String r12 = "video-hsr"
            java.lang.String r13 = "off"
            if (r10 != 0) goto L_0x01e4
            if (r11 == 0) goto L_0x01d8
            goto L_0x01e4
        L_0x01d8:
            android.hardware.Camera$Parameters r10 = r0.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setVideoHighFrameRate(r10, r13)
            android.hardware.Camera$Parameters r10 = r0.mParameters
            r10.set(r12, r13)
            goto L_0x02b0
        L_0x01e4:
            java.lang.String r11 = r5.substring(r9)
            if (r10 == 0) goto L_0x01ed
            r0.mUnsupportedHFRVideoSize = r7
            goto L_0x01ef
        L_0x01ed:
            r0.mUnsupportedHSRVideoSize = r7
        L_0x01ef:
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            int r15 = r0.videoWidth
            r14.append(r15)
            java.lang.String r15 = "x"
            r14.append(r15)
            int r15 = r0.videoHeight
            r14.append(r15)
            java.lang.String r14 = r14.toString()
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            java.lang.String r9 = "current set resolution is : "
            r15.append(r9)
            r15.append(r14)
            java.lang.String r9 = " : Rate is : "
            r15.append(r9)
            r15.append(r11)
            java.lang.String r9 = r15.toString()
            android.util.Log.v(r2, r9)
            r9 = 0
            android.hardware.Camera$Parameters r14 = r0.mParameters     // Catch:{ NullPointerException -> 0x0261 }
            java.util.List r14 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedVideoHighFrameRateModes(r14)     // Catch:{ NullPointerException -> 0x0261 }
            boolean r14 = isSupported(r11, r14)     // Catch:{ NullPointerException -> 0x0261 }
            if (r14 == 0) goto L_0x0246
            android.hardware.Camera$Parameters r9 = r0.mParameters     // Catch:{ NullPointerException -> 0x0261 }
            java.util.List r9 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedVideoHighFrameRateModes(r9)     // Catch:{ NullPointerException -> 0x0261 }
            int r9 = r9.indexOf(r11)     // Catch:{ NullPointerException -> 0x0261 }
            android.hardware.Camera$Parameters r14 = r0.mParameters     // Catch:{ NullPointerException -> 0x0261 }
            java.util.List r14 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedHfrSizes(r14)     // Catch:{ NullPointerException -> 0x0261 }
            java.lang.Object r9 = r14.get(r9)     // Catch:{ NullPointerException -> 0x0261 }
            android.hardware.Camera$Size r9 = (android.hardware.Camera.Size) r9     // Catch:{ NullPointerException -> 0x0261 }
        L_0x0246:
            if (r9 == 0) goto L_0x0266
            int r14 = r0.videoWidth     // Catch:{ NullPointerException -> 0x0261 }
            int r15 = r9.width     // Catch:{ NullPointerException -> 0x0261 }
            if (r14 > r15) goto L_0x0266
            int r14 = r0.videoHeight     // Catch:{ NullPointerException -> 0x0261 }
            int r9 = r9.height     // Catch:{ NullPointerException -> 0x0261 }
            if (r14 > r9) goto L_0x0266
            if (r10 == 0) goto L_0x0259
            r0.mUnsupportedHFRVideoSize = r8     // Catch:{ NullPointerException -> 0x0261 }
            goto L_0x025b
        L_0x0259:
            r0.mUnsupportedHSRVideoSize = r8     // Catch:{ NullPointerException -> 0x0261 }
        L_0x025b:
            java.lang.String r9 = "Current hfr resolution is supported"
            android.util.Log.v(r2, r9)     // Catch:{ NullPointerException -> 0x0261 }
            goto L_0x0266
        L_0x0261:
            java.lang.String r9 = "supported hfr sizes is null"
            android.util.Log.e(r2, r9)
        L_0x0266:
            int r9 = java.lang.Integer.parseInt(r11)
            int r14 = r0.videoWidth
            int r15 = r0.videoHeight
            boolean r9 = r0.isSessionSupportedByEncoder(r14, r15, r9)
            if (r9 != 0) goto L_0x027b
            if (r10 == 0) goto L_0x0279
            r0.mUnsupportedHFRVideoSize = r7
            goto L_0x027b
        L_0x0279:
            r0.mUnsupportedHSRVideoSize = r7
        L_0x027b:
            if (r10 == 0) goto L_0x0297
            android.hardware.Camera$Parameters r9 = r0.mParameters
            r9.set(r12, r13)
            boolean r9 = r0.mUnsupportedHFRVideoSize
            if (r9 == 0) goto L_0x0291
            android.hardware.Camera$Parameters r9 = r0.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setVideoHighFrameRate(r9, r13)
            java.lang.String r9 = "Unsupported hfr resolution"
            android.util.Log.v(r2, r9)
            goto L_0x02b0
        L_0x0291:
            android.hardware.Camera$Parameters r9 = r0.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setVideoHighFrameRate(r9, r11)
            goto L_0x02b0
        L_0x0297:
            android.hardware.Camera$Parameters r9 = r0.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setVideoHighFrameRate(r9, r13)
            boolean r9 = r0.mUnsupportedHSRVideoSize
            if (r9 == 0) goto L_0x02ab
            java.lang.String r9 = "Unsupported hsr resolution"
            android.util.Log.v(r2, r9)
            android.hardware.Camera$Parameters r9 = r0.mParameters
            r9.set(r12, r13)
            goto L_0x02b0
        L_0x02ab:
            android.hardware.Camera$Parameters r9 = r0.mParameters
            r9.set(r12, r11)
        L_0x02b0:
            r18.setFlipValue()
            com.android.camera.ComboPreferences r9 = r0.mPreferences
            com.android.camera.CameraActivity r10 = r0.mActivity
            r11 = 2131690517(0x7f0f0415, float:1.901008E38)
            java.lang.String r10 = r10.getString(r11)
            java.lang.String r11 = "pref_camera_video_cds_mode_key"
            java.lang.String r9 = r9.getString(r11, r10)
            java.lang.String r10 = r0.mPrevSavedVideoCDS
            if (r10 != 0) goto L_0x02cc
            if (r9 == 0) goto L_0x02cc
            r0.mPrevSavedVideoCDS = r9
        L_0x02cc:
            boolean r10 = r0.mOverrideCDS
            if (r10 == 0) goto L_0x02d4
            java.lang.String r9 = r0.mPrevSavedVideoCDS
            r0.mOverrideCDS = r8
        L_0x02d4:
            android.hardware.Camera$Parameters r10 = r0.mParameters
            java.util.List r10 = com.android.camera.CameraSettings.getSupportedVideoCDSModes(r10)
            boolean r10 = com.android.camera.util.CameraUtil.isSupported(r9, r10)
            java.lang.String r14 = "video-cds-mode"
            if (r10 == 0) goto L_0x02e7
            android.hardware.Camera$Parameters r10 = r0.mParameters
            r10.set(r14, r9)
        L_0x02e7:
            com.android.camera.ComboPreferences r10 = r0.mPreferences
            com.android.camera.CameraActivity r15 = r0.mActivity
            r7 = 2131690549(0x7f0f0435, float:1.9010145E38)
            java.lang.String r7 = r15.getString(r7)
            java.lang.String r15 = "pref_camera_video_tnr_mode_key"
            java.lang.String r7 = r10.getString(r15, r7)
            android.hardware.Camera$Parameters r10 = r0.mParameters
            java.util.List r10 = com.android.camera.CameraSettings.getSupportedVideoTNRModes(r10)
            boolean r10 = com.android.camera.util.CameraUtil.isSupported(r7, r10)
            r8 = 2131690553(0x7f0f0439, float:1.9010153E38)
            r6 = 2131690523(0x7f0f041b, float:1.9010092E38)
            if (r10 == 0) goto L_0x038a
            com.android.camera.CameraActivity r10 = r0.mActivity
            java.lang.String r10 = r10.getString(r8)
            boolean r10 = r7.equals(r10)
            if (r10 != 0) goto L_0x034b
            android.hardware.Camera$Parameters r10 = r0.mParameters
            com.android.camera.CameraActivity r8 = r0.mActivity
            java.lang.String r8 = r8.getString(r6)
            r10.set(r14, r8)
            com.android.camera.VideoUI r8 = r0.mUI
            r10 = 2
            java.lang.String[] r6 = new java.lang.String[r10]
            r10 = 0
            r6[r10] = r11
            com.android.camera.CameraActivity r10 = r0.mActivity
            r16 = r4
            r4 = 2131690523(0x7f0f041b, float:1.9010092E38)
            java.lang.String r10 = r10.getString(r4)
            r4 = 1
            r6[r4] = r10
            r8.overrideSettings(r6)
            boolean r6 = r0.mIsVideoCDSUpdated
            if (r6 != 0) goto L_0x0348
            if (r9 == 0) goto L_0x0344
            java.lang.String r6 = r0.mTempVideoCDS
            r0.mPrevSavedVideoCDS = r6
        L_0x0344:
            r0.mIsVideoTNREnabled = r4
            r0.mIsVideoCDSUpdated = r4
        L_0x0348:
            r6 = r4
            r4 = 0
            goto L_0x0374
        L_0x034b:
            r16 = r4
            boolean r4 = r0.mIsVideoTNREnabled
            if (r4 == 0) goto L_0x0370
            android.hardware.Camera$Parameters r4 = r0.mParameters
            java.lang.String r6 = r0.mPrevSavedVideoCDS
            r4.set(r14, r6)
            com.android.camera.VideoUI r4 = r0.mUI
            r6 = 2
            java.lang.String[] r8 = new java.lang.String[r6]
            r6 = 0
            r8[r6] = r11
            java.lang.String r10 = r0.mPrevSavedVideoCDS
            r6 = 1
            r8[r6] = r10
            r4.overrideSettings(r8)
            r4 = 0
            r0.mIsVideoTNREnabled = r4
            r0.mIsVideoCDSUpdated = r4
            r0.mOverrideCDS = r6
            goto L_0x0374
        L_0x0370:
            r4 = 0
            r6 = 1
            r0.mTempVideoCDS = r9
        L_0x0374:
            android.hardware.Camera$Parameters r8 = r0.mParameters
            java.lang.String r10 = "video-tnr-mode"
            r8.set(r10, r7)
            com.android.camera.VideoUI r8 = r0.mUI
            r17 = r1
            r10 = 2
            java.lang.String[] r1 = new java.lang.String[r10]
            r1[r4] = r15
            r1[r6] = r7
            r8.overrideSettings(r1)
            goto L_0x038e
        L_0x038a:
            r17 = r1
            r16 = r4
        L_0x038e:
            com.android.camera.ComboPreferences r1 = r0.mPreferences
            com.android.camera.CameraActivity r4 = r0.mActivity
            r6 = 2131690191(0x7f0f02cf, float:1.9009419E38)
            java.lang.String r4 = r4.getString(r6)
            java.lang.String r6 = "pref_camera_noise_reduction_key"
            java.lang.String r1 = r1.getString(r6, r4)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r6 = "Noise ReductionMode ="
            r4.append(r6)
            r4.append(r1)
            java.lang.String r4 = r4.toString()
            android.util.Log.v(r2, r4)
            android.hardware.Camera$Parameters r4 = r0.mParameters
            java.util.List r4 = com.android.camera.CameraSettings.getSupportedNoiseReductionModes(r4)
            boolean r4 = isSupported(r1, r4)
            if (r4 == 0) goto L_0x0460
            com.android.camera.CameraActivity r4 = r0.mActivity
            r6 = 2131690197(0x7f0f02d5, float:1.900943E38)
            java.lang.String r4 = r4.getString(r6)
            boolean r4 = r1.equals(r4)
            if (r4 == 0) goto L_0x040b
            com.android.camera.CameraActivity r4 = r0.mActivity
            r6 = 2131690524(0x7f0f041c, float:1.9010094E38)
            java.lang.String r4 = r4.getString(r6)
            boolean r4 = r9.equals(r4)
            if (r4 == 0) goto L_0x040b
            android.hardware.Camera$Parameters r4 = r0.mParameters
            com.android.camera.CameraActivity r6 = r0.mActivity
            r8 = 2131690523(0x7f0f041b, float:1.9010092E38)
            java.lang.String r6 = r6.getString(r8)
            r4.set(r14, r6)
            com.android.camera.VideoUI r4 = r0.mUI
            r6 = 2
            java.lang.String[] r10 = new java.lang.String[r6]
            r6 = 0
            r10[r6] = r11
            com.android.camera.CameraActivity r6 = r0.mActivity
            java.lang.String r6 = r6.getString(r8)
            r8 = 1
            r10[r8] = r6
            r4.overrideSettings(r10)
            com.android.camera.CameraActivity r4 = r0.mActivity
            r6 = 2131689561(0x7f0f0059, float:1.900814E38)
            android.widget.Toast r4 = android.widget.Toast.makeText(r4, r6, r8)
            r4.show()
        L_0x040b:
            com.android.camera.CameraActivity r4 = r0.mActivity
            r6 = 2131690197(0x7f0f02d5, float:1.900943E38)
            java.lang.String r4 = r4.getString(r6)
            boolean r4 = r1.equals(r4)
            if (r4 == 0) goto L_0x0459
            com.android.camera.CameraActivity r4 = r0.mActivity
            r6 = 2131690554(0x7f0f043a, float:1.9010155E38)
            java.lang.String r4 = r4.getString(r6)
            boolean r4 = r7.equals(r4)
            if (r4 == 0) goto L_0x0459
            android.hardware.Camera$Parameters r4 = r0.mParameters
            com.android.camera.CameraActivity r6 = r0.mActivity
            r8 = 2131690553(0x7f0f0439, float:1.9010153E38)
            java.lang.String r6 = r6.getString(r8)
            java.lang.String r10 = "video-tnr-mode"
            r4.set(r10, r6)
            com.android.camera.VideoUI r4 = r0.mUI
            r6 = 2
            java.lang.String[] r10 = new java.lang.String[r6]
            r6 = 0
            r10[r6] = r15
            com.android.camera.CameraActivity r6 = r0.mActivity
            java.lang.String r6 = r6.getString(r8)
            r8 = 1
            r10[r8] = r6
            r4.overrideSettings(r10)
            com.android.camera.CameraActivity r4 = r0.mActivity
            r6 = 2131689564(0x7f0f005c, float:1.9008147E38)
            android.widget.Toast r4 = android.widget.Toast.makeText(r4, r6, r8)
            r4.show()
        L_0x0459:
            android.hardware.Camera$Parameters r4 = r0.mParameters
            java.lang.String r6 = "noise-reduction-mode"
            r4.set(r6, r1)
        L_0x0460:
            com.android.camera.ComboPreferences r4 = r0.mPreferences
            com.android.camera.CameraActivity r6 = r0.mActivity
            r8 = 2131690452(0x7f0f03d4, float:1.9009948E38)
            java.lang.String r6 = r6.getString(r8)
            java.lang.String r8 = "pref_camera_see_more_key"
            java.lang.String r4 = r4.getString(r8, r6)
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r8 = "See More value ="
            r6.append(r8)
            r6.append(r4)
            java.lang.String r6 = r6.toString()
            android.util.Log.v(r2, r6)
            android.hardware.Camera$Parameters r6 = r0.mParameters
            java.util.List r6 = com.android.camera.CameraSettings.getSupportedSeeMoreModes(r6)
            boolean r6 = isSupported(r4, r6)
            if (r6 == 0) goto L_0x0585
            com.android.camera.CameraActivity r6 = r0.mActivity
            r8 = 2131690457(0x7f0f03d9, float:1.9009958E38)
            java.lang.String r6 = r6.getString(r8)
            boolean r6 = r4.equals(r6)
            if (r6 == 0) goto L_0x04dd
            com.android.camera.CameraActivity r6 = r0.mActivity
            r8 = 2131690524(0x7f0f041c, float:1.9010094E38)
            java.lang.String r6 = r6.getString(r8)
            boolean r6 = r9.equals(r6)
            if (r6 == 0) goto L_0x04dd
            android.hardware.Camera$Parameters r6 = r0.mParameters
            com.android.camera.CameraActivity r8 = r0.mActivity
            r9 = 2131690523(0x7f0f041b, float:1.9010092E38)
            java.lang.String r8 = r8.getString(r9)
            r6.set(r14, r8)
            com.android.camera.VideoUI r6 = r0.mUI
            r8 = 2
            java.lang.String[] r10 = new java.lang.String[r8]
            r8 = 0
            r10[r8] = r11
            com.android.camera.CameraActivity r8 = r0.mActivity
            java.lang.String r8 = r8.getString(r9)
            r9 = 1
            r10[r9] = r8
            r6.overrideSettings(r10)
            com.android.camera.CameraActivity r6 = r0.mActivity
            r8 = 2131689562(0x7f0f005a, float:1.9008143E38)
            android.widget.Toast r6 = android.widget.Toast.makeText(r6, r8, r9)
            r6.show()
        L_0x04dd:
            com.android.camera.CameraActivity r6 = r0.mActivity
            r8 = 2131690457(0x7f0f03d9, float:1.9009958E38)
            java.lang.String r6 = r6.getString(r8)
            boolean r6 = r4.equals(r6)
            if (r6 == 0) goto L_0x052b
            com.android.camera.CameraActivity r6 = r0.mActivity
            r8 = 2131690554(0x7f0f043a, float:1.9010155E38)
            java.lang.String r6 = r6.getString(r8)
            boolean r6 = r7.equals(r6)
            if (r6 == 0) goto L_0x052b
            android.hardware.Camera$Parameters r6 = r0.mParameters
            com.android.camera.CameraActivity r7 = r0.mActivity
            r8 = 2131690553(0x7f0f0439, float:1.9010153E38)
            java.lang.String r7 = r7.getString(r8)
            java.lang.String r9 = "video-tnr-mode"
            r6.set(r9, r7)
            com.android.camera.VideoUI r6 = r0.mUI
            r7 = 2
            java.lang.String[] r9 = new java.lang.String[r7]
            r7 = 0
            r9[r7] = r15
            com.android.camera.CameraActivity r7 = r0.mActivity
            java.lang.String r7 = r7.getString(r8)
            r8 = 1
            r9[r8] = r7
            r6.overrideSettings(r9)
            com.android.camera.CameraActivity r6 = r0.mActivity
            r7 = 2131689565(0x7f0f005d, float:1.9008149E38)
            android.widget.Toast r6 = android.widget.Toast.makeText(r6, r7, r8)
            r6.show()
        L_0x052b:
            com.android.camera.CameraActivity r6 = r0.mActivity
            r7 = 2131690457(0x7f0f03d9, float:1.9009958E38)
            java.lang.String r6 = r6.getString(r7)
            boolean r6 = r4.equals(r6)
            if (r6 == 0) goto L_0x057e
            com.android.camera.CameraActivity r6 = r0.mActivity
            r7 = 2131690198(0x7f0f02d6, float:1.9009433E38)
            java.lang.String r6 = r6.getString(r7)
            boolean r1 = r1.equals(r6)
            if (r1 != 0) goto L_0x057e
            android.hardware.Camera$Parameters r1 = r0.mParameters
            com.android.camera.CameraActivity r6 = r0.mActivity
            r7 = 2131690198(0x7f0f02d6, float:1.9009433E38)
            java.lang.String r6 = r6.getString(r7)
            java.lang.String r7 = "noise-reduction-mode"
            r1.set(r7, r6)
            com.android.camera.VideoUI r1 = r0.mUI
            r6 = 2
            java.lang.String[] r6 = new java.lang.String[r6]
            java.lang.String r7 = "pref_camera_noise_reduction_key"
            r8 = 0
            r6[r8] = r7
            com.android.camera.CameraActivity r7 = r0.mActivity
            r8 = 2131690198(0x7f0f02d6, float:1.9009433E38)
            java.lang.String r7 = r7.getString(r8)
            r8 = 1
            r6[r8] = r7
            r1.overrideSettings(r6)
            com.android.camera.CameraActivity r1 = r0.mActivity
            r6 = 2131689563(0x7f0f005b, float:1.9008145E38)
            android.widget.Toast r1 = android.widget.Toast.makeText(r1, r6, r8)
            r1.show()
        L_0x057e:
            android.hardware.Camera$Parameters r1 = r0.mParameters
            java.lang.String r6 = "see-more"
            r1.set(r6, r4)
        L_0x0585:
            com.android.camera.ComboPreferences r1 = r0.mPreferences
            com.android.camera.CameraActivity r4 = r0.mActivity
            r6 = 2131690532(0x7f0f0424, float:1.901011E38)
            java.lang.String r4 = r4.getString(r6)
            java.lang.String r6 = "pref_camera_video_hdr_key"
            java.lang.String r1 = r1.getString(r6, r4)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r6 = "Video HDR Setting ="
            r4.append(r6)
            r4.append(r1)
            java.lang.String r4 = r4.toString()
            android.util.Log.v(r2, r4)
            android.hardware.Camera$Parameters r4 = r0.mParameters
            java.util.List r4 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedVideoHDRModes(r4)
            boolean r4 = isSupported(r1, r4)
            if (r4 == 0) goto L_0x05bc
            android.hardware.Camera$Parameters r4 = r0.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setVideoHDRMode(r4, r1)
            goto L_0x05c1
        L_0x05bc:
            android.hardware.Camera$Parameters r1 = r0.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setVideoHDRMode(r1, r13)
        L_0x05c1:
            android.hardware.Camera$Parameters r1 = r0.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.getVideoHighFrameRate(r1)
            android.hardware.Camera$Parameters r1 = r0.mParameters
            r1.get(r12)
            android.hardware.Camera$Parameters r1 = r0.mParameters
            java.lang.String r1 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getVideoHDRMode(r1)
            boolean r4 = r13.equals(r5)
            if (r4 != 0) goto L_0x0649
            com.android.camera.ComboPreferences r4 = r0.mPreferences
            com.android.camera.CameraActivity r6 = r0.mActivity
            r7 = 2131690663(0x7f0f04a7, float:1.9010376E38)
            java.lang.String r6 = r6.getString(r7)
            java.lang.String r7 = "pref_video_time_lapse_frame_interval_key"
            java.lang.String r4 = r4.getString(r7, r6)
            int r4 = java.lang.Integer.parseInt(r4)
            r6 = 3
            java.lang.String r7 = r5.substring(r6)
            boolean r7 = r0.isDigit(r7)
            if (r7 == 0) goto L_0x0600
            java.lang.String r5 = r5.substring(r6)
            int r8 = java.lang.Integer.parseInt(r5)
            goto L_0x0601
        L_0x0600:
            r8 = 0
        L_0x0601:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "rate = "
            r5.append(r6)
            r5.append(r8)
            java.lang.String r5 = r5.toString()
            android.util.Log.v(r2, r5)
            if (r4 != 0) goto L_0x062c
            r4 = r17
            boolean r5 = r4.equals(r3)
            if (r5 == 0) goto L_0x0623
            int r5 = PERSIST_EIS_MAX_FPS
            if (r8 > r5) goto L_0x062e
        L_0x0623:
            if (r1 == 0) goto L_0x064b
            boolean r1 = r1.equals(r13)
            if (r1 != 0) goto L_0x064b
            goto L_0x062e
        L_0x062c:
            r4 = r17
        L_0x062e:
            java.lang.String r1 = "HDR/DIS/Time Lapse ON for HFR/HSR selection, turning HFR/HSR off"
            android.util.Log.v(r2, r1)
            android.hardware.Camera$Parameters r1 = r0.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setVideoHighFrameRate(r1, r13)
            android.hardware.Camera$Parameters r1 = r0.mParameters
            r1.set(r12, r13)
            com.android.camera.VideoUI r1 = r0.mUI
            java.lang.String r5 = "pref_camera_hfr_key"
            java.lang.String[] r5 = new java.lang.String[]{r5, r13}
            r1.overrideSettings(r5)
            goto L_0x064b
        L_0x0649:
            r4 = r17
        L_0x064b:
            android.hardware.Camera$Parameters r1 = r0.mParameters
            java.util.List r1 = r1.getSupportedPictureSizes()
            r5 = 0
            java.lang.Object r1 = r1.get(r5)
            android.hardware.Camera$Size r1 = (android.hardware.Camera.Size) r1
            int r5 = r1.width
            int r6 = r0.videoWidth
            if (r5 <= r6) goto L_0x0664
            int r1 = r1.height
            int r5 = r0.videoHeight
            if (r1 > r5) goto L_0x0695
        L_0x0664:
            boolean r1 = r4.equals(r3)
            if (r1 == 0) goto L_0x0695
            java.lang.String r1 = "DIS is not supported for this video quality"
            android.util.Log.v(r2, r1)
            com.android.camera.CameraActivity r1 = r0.mActivity
            r3 = 2131689588(0x7f0f0074, float:1.9008196E38)
            r4 = 1
            com.android.camera.ui.RotateTextToast r1 = com.android.camera.p004ui.RotateTextToast.makeText(r1, r3, r4)
            r1.show()
            android.hardware.Camera$Parameters r1 = r0.mParameters
            java.lang.String r3 = "dis"
            java.lang.String r4 = "disable"
            r1.set(r3, r4)
            com.android.camera.VideoUI r1 = r0.mUI
            java.lang.String r3 = "disable"
            r4 = r16
            java.lang.String[] r3 = new java.lang.String[]{r4, r3}
            r1.overrideSettings(r3)
            r1 = 0
            r0.mIsDISEnabled = r1
        L_0x0695:
            com.android.camera.ComboPreferences r1 = r0.mPreferences
            com.android.camera.CameraActivity r3 = r0.mActivity
            r4 = 2131690538(0x7f0f042a, float:1.9010122E38)
            java.lang.String r3 = r3.getString(r4)
            java.lang.String r4 = "pref_camera_video_rotation_key"
            java.lang.String r1 = r1.getString(r4, r3)
            android.hardware.Camera$Parameters r3 = r0.mParameters
            java.util.List r3 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedVideoRotationValues(r3)
            boolean r3 = isSupported(r1, r3)
            if (r3 == 0) goto L_0x06b7
            android.hardware.Camera$Parameters r3 = r0.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setVideoRotation(r3, r1)
        L_0x06b7:
            r18.updatePowerMode()
            com.android.camera.ComboPreferences r1 = r0.mPreferences
            com.android.camera.CameraActivity r3 = r0.mActivity
            r4 = 2131690053(0x7f0f0245, float:1.9009139E38)
            java.lang.String r3 = r3.getString(r4)
            java.lang.String r4 = "pref_camera_facedetection_key"
            java.lang.String r1 = r1.getString(r4, r3)
            android.hardware.Camera$Parameters r3 = r0.mParameters
            java.util.List r3 = org.codeaurora.snapcam.wrapper.ParametersWrapper.getSupportedFaceDetectionModes(r3)
            boolean r3 = com.android.camera.util.CameraUtil.isSupported(r1, r3)
            if (r3 == 0) goto L_0x0714
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "setFaceDetectionMode "
            r3.append(r4)
            r3.append(r1)
            java.lang.String r3 = r3.toString()
            android.util.Log.d(r2, r3)
            android.hardware.Camera$Parameters r2 = r0.mParameters
            org.codeaurora.snapcam.wrapper.ParametersWrapper.setFaceDetectionMode(r2, r1)
            java.lang.String r2 = "on"
            boolean r2 = r1.equals(r2)
            if (r2 == 0) goto L_0x0703
            boolean r2 = r0.mFaceDetectionEnabled
            if (r2 != 0) goto L_0x0703
            r2 = 1
            r0.mFaceDetectionEnabled = r2
            r18.startFaceDetection()
            goto L_0x0714
        L_0x0703:
            r2 = 1
            boolean r1 = r1.equals(r13)
            if (r1 == 0) goto L_0x0714
            boolean r1 = r0.mFaceDetectionEnabled
            if (r1 != r2) goto L_0x0714
            r18.stopFaceDetection()
            r1 = 0
            r0.mFaceDetectionEnabled = r1
        L_0x0714:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.VideoModule.qcomSetCameraParameters():void");
    }

    private boolean isDigit(String str) {
        return Pattern.compile("[1-9][0-9]*").matcher(str).matches();
    }

    private void setCameraParameters(boolean z) {
        Size size;
        StringBuilder sb = new StringBuilder();
        sb.append("Preview dimension in App->");
        sb.append(this.mDesiredPreviewWidth);
        sb.append("X");
        sb.append(this.mDesiredPreviewHeight);
        String sb2 = sb.toString();
        String str = TAG;
        Log.d(str, sb2);
        this.mParameters.setPreviewSize(this.mDesiredPreviewWidth, this.mDesiredPreviewHeight);
        Parameters parameters = this.mParameters;
        StringBuilder sb3 = new StringBuilder();
        sb3.append(this.mProfile.videoFrameWidth);
        String str2 = "x";
        sb3.append(str2);
        sb3.append(this.mProfile.videoFrameHeight);
        String str3 = "video-size";
        parameters.set(str3, sb3.toString());
        int[] maxPreviewFpsRange = CameraUtil.getMaxPreviewFpsRange(this.mParameters);
        boolean z2 = false;
        if (maxPreviewFpsRange.length > 0) {
            this.mParameters.setPreviewFpsRange(maxPreviewFpsRange[0], maxPreviewFpsRange[1]);
        } else {
            this.mParameters.setPreviewFrameRate(this.mProfile.videoFrameRate);
        }
        if (z) {
            this.mHandler.sendEmptyMessageDelayed(10, 800);
        } else {
            forceFlashOffIfSupported(!this.mPreviewFocused);
        }
        CamcorderProfile camcorderProfile = this.mProfile;
        this.videoWidth = camcorderProfile.videoFrameWidth;
        this.videoHeight = camcorderProfile.videoFrameHeight;
        StringBuilder sb4 = new StringBuilder();
        sb4.append("NOTE: SetCameraParameters ");
        sb4.append(this.videoWidth);
        sb4.append(" x ");
        sb4.append(this.videoHeight);
        Log.i(str, sb4.toString());
        StringBuilder sb5 = new StringBuilder();
        sb5.append(this.videoWidth);
        sb5.append(str2);
        sb5.append(this.videoHeight);
        String sb6 = sb5.toString();
        StringBuilder sb7 = new StringBuilder();
        sb7.append("Video dimension in App->");
        sb7.append(sb6);
        Log.e(str, sb7.toString());
        this.mParameters.set(str3, sb6);
        String string = this.mPreferences.getString("pref_camera_whitebalance_key", this.mActivity.getString(C0905R.string.pref_camera_whitebalance_default));
        if (isSupported(string, this.mParameters.getSupportedWhiteBalance())) {
            this.mParameters.setWhiteBalance(string);
        } else {
            String whiteBalance = this.mParameters.getWhiteBalance();
        }
        if (this.mParameters.isZoomSupported()) {
            this.mZoomValue = this.mCameraDevice.getParameters().getZoom();
            this.mParameters.setZoom(this.mZoomValue);
        }
        String str4 = "continuous-video";
        if (isSupported(str4, this.mParameters.getSupportedFocusModes())) {
            this.mParameters.setFocusMode(str4);
        }
        Parameters parameters2 = this.mParameters;
        String str5 = CameraUtil.TRUE;
        parameters2.set(CameraUtil.RECORDING_HINT, str5);
        if (str5.equals(this.mParameters.get("video-stabilization-supported"))) {
            this.mParameters.set("video-stabilization", str5);
        }
        String string2 = this.mPreferences.getString(CameraSettings.KEY_VIDEO_SNAPSHOT_SIZE, this.mActivity.getString(C0905R.string.pref_camera_videosnapsize_default));
        if (string2.equals("auto")) {
            size = CameraUtil.getOptimalVideoSnapshotPictureSize(this.mParameters.getSupportedPictureSizes(), ((double) this.mDesiredPreviewWidth) / ((double) this.mDesiredPreviewHeight));
            if (!this.mParameters.getPictureSize().equals(size)) {
                this.mParameters.setPictureSize(size.width, size.height);
            }
        } else {
            CameraSettings.setCameraPictureSize(string2, this.mParameters.getSupportedPictureSizes(), this.mParameters);
            size = this.mParameters.getPictureSize();
        }
        StringBuilder sb8 = new StringBuilder();
        sb8.append("Video snapshot size is ");
        sb8.append(size.width);
        sb8.append(str2);
        sb8.append(size.height);
        Log.v(str, sb8.toString());
        Size pictureSize = this.mParameters.getPictureSize();
        Size optimalJpegThumbnailSize = CameraUtil.getOptimalJpegThumbnailSize(this.mParameters.getSupportedJpegThumbnailSizes(), ((double) pictureSize.width) / ((double) pictureSize.height));
        if (!this.mParameters.getJpegThumbnailSize().equals(optimalJpegThumbnailSize)) {
            this.mParameters.setJpegThumbnailSize(optimalJpegThumbnailSize.width, optimalJpegThumbnailSize.height);
        }
        StringBuilder sb9 = new StringBuilder();
        sb9.append("Thumbnail size is ");
        sb9.append(optimalJpegThumbnailSize.width);
        sb9.append(str2);
        sb9.append(optimalJpegThumbnailSize.height);
        Log.v(str, sb9.toString());
        this.mParameters.setJpegQuality(CameraProfile.getJpegEncodingQualityParameter(this.mCameraId, 2));
        qcomSetCameraParameters();
        if (this.mPreviewing) {
            stopPreview();
            z2 = true;
        }
        this.mCameraDevice.setParameters(this.mParameters);
        if (z2) {
            startPreview();
        }
        this.mParameters = this.mCameraDevice.getParameters();
        this.mUI.updateOnScreenIndicators(this.mParameters, this.mPreferences);
    }

    public void onConfigurationChanged(Configuration configuration) {
        Log.v(TAG, "onConfigurationChanged");
        setDisplayOrientation();
        resizeForPreviewAspectRatio();
    }

    public void onSharedPreferenceChanged(ListPreference listPreference) {
        if (listPreference != null) {
            if ("pref_video_quality_key".equals(listPreference.getKey()) && !PERSIST_4K_NO_LIMIT) {
                String value = listPreference.getValue();
                if (CameraSettings.VIDEO_QUALITY_TABLE.containsKey(value)) {
                    int intValue = ((Integer) CameraSettings.VIDEO_QUALITY_TABLE.get(value)).intValue();
                    if ((intValue == 8 || intValue == CamcorderProfileWrapper.QUALITY_4KDCI) && this.mPreferences != null) {
                        String string = this.mActivity.getString(C0905R.string.pref_camera_dis_value_disable);
                        if (!string.equals(this.mPreferences.getString("pref_camera_dis_key", string))) {
                            RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.video_quality_4k_disable_IS, 1).show();
                        }
                    }
                }
            }
        }
        onSharedPreferenceChanged();
    }

    public void onSharedPreferenceChanged() {
        if (!this.mPaused) {
            synchronized (this.mPreferences) {
                if (this.mCameraDevice != null) {
                    this.mLocationManager.recordLocation(RecordLocationPreference.get(this.mPreferences, "pref_camera_recordlocation_key"));
                    readVideoPreferences();
                    this.mUI.showTimeLapseUI(this.mCaptureTimeLapse);
                    Size previewSize = this.mParameters.getPreviewSize();
                    if (previewSize.width == this.mDesiredPreviewWidth && previewSize.height == this.mDesiredPreviewHeight) {
                        if (!this.mRestartPreview) {
                            setCameraParameters(false);
                            this.mRestartPreview = false;
                            this.mUI.updateOnScreenIndicators(this.mParameters, this.mPreferences);
                            Storage.setSaveSDCard(this.mPreferences.getString("pref_camera_savepath_key", "0").equals("1"));
                            this.mActivity.updateStorageSpaceAndHint();
                        }
                    }
                    stopPreview();
                    resizeForPreviewAspectRatio();
                    startPreview();
                    this.mRestartPreview = false;
                    this.mUI.updateOnScreenIndicators(this.mParameters, this.mPreferences);
                    Storage.setSaveSDCard(this.mPreferences.getString("pref_camera_savepath_key", "0").equals("1"));
                    this.mActivity.updateStorageSpaceAndHint();
                }
            }
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

    /* access modifiers changed from: private */
    public void switchCamera() {
        if (!this.mPaused) {
            Log.d(TAG, "Start to switch camera.");
            this.mUI.applySurfaceChange(SURFACE_STATUS.HIDE);
            this.mCameraId = this.mPendingSwitchCameraId;
            this.mPendingSwitchCameraId = -1;
            setCameraId(this.mCameraId);
            closeCamera();
            this.mUI.collapseCameraControls();
            this.mPreferences.setLocalId(this.mActivity, this.mCameraId);
            CameraSettings.upgradeLocalPreferences(this.mPreferences.getLocal());
            openCamera();
            readVideoPreferences();
            this.mUI.applySurfaceChange(SURFACE_STATUS.SURFACE_VIEW);
            startPreview();
            initializeVideoSnapshot();
            resizeForPreviewAspectRatio();
            initializeVideoControl();
            this.mZoomValue = 0;
            this.mUI.initializeZoom(this.mParameters);
            this.mUI.setOrientationIndicator(0, false);
            this.mHandler.sendEmptyMessage(9);
            this.mUI.updateOnScreenIndicators(this.mParameters, this.mPreferences);
            this.mUI.showTimeLapseUI(this.mCaptureTimeLapse);
        }
    }

    public void onPreviewTextureCopied() {
        this.mHandler.sendEmptyMessage(8);
    }

    private void initializeVideoSnapshot() {
        Parameters parameters = this.mParameters;
        if (parameters != null && CameraUtil.isVideoSnapshotSupported(parameters) && !this.mIsVideoCaptureIntent && this.mPreferences.getBoolean(CameraSettings.KEY_VIDEO_FIRST_USE_HINT_SHOWN, true)) {
            this.mHandler.sendEmptyMessageDelayed(7, 1000);
        }
    }

    /* access modifiers changed from: 0000 */
    public void showVideoSnapshotUI(boolean z) {
        Parameters parameters = this.mParameters;
        if (parameters != null && CameraUtil.isVideoSnapshotSupported(parameters) && !this.mIsVideoCaptureIntent) {
            if (z) {
                this.mUI.animateFlash();
                this.mUI.animateCapture();
            } else {
                this.mUI.showPreviewBorder(z);
            }
            this.mUI.enableShutter(!z);
        }
    }

    private void forceFlashOffIfSupported(boolean z) {
        String str;
        if (!z) {
            str = this.mPreferences.getString(CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE, this.mActivity.getString(C0905R.string.pref_camera_video_flashmode_default));
        } else {
            str = "off";
        }
        if (isSupported(str, this.mParameters.getSupportedFlashModes())) {
            this.mParameters.setFlashMode(str);
        } else if (this.mParameters.getFlashMode() == null) {
            this.mActivity.getString(C0905R.string.pref_camera_flashmode_no_flash);
        }
    }

    /* access modifiers changed from: private */
    public void forceFlashOff(boolean z) {
        if (this.mPreviewing && this.mParameters.getFlashMode() != null) {
            forceFlashOffIfSupported(z);
            this.mCameraDevice.setParameters(this.mParameters);
            this.mUI.updateOnScreenIndicators(this.mParameters, this.mPreferences);
        }
    }

    public void onPreviewFocusChanged(boolean z) {
        this.mUI.onPreviewFocusChanged(z);
        this.mHandler.sendEmptyMessageDelayed(10, 800);
        this.mPreviewFocused = z;
    }

    public boolean arePreviewControlsVisible() {
        return this.mUI.arePreviewControlsVisible();
    }

    /* access modifiers changed from: private */
    public void storeImage(byte[] bArr, Location location) {
        long currentTimeMillis = System.currentTimeMillis();
        String createJpegName = CameraUtil.createJpegName(currentTimeMillis);
        ExifInterface exif = Exif.getExif(bArr);
        int orientation = Exif.getOrientation(exif);
        Size pictureSize = this.mParameters.getPictureSize();
        this.mActivity.getMediaSaveService().addImage(bArr, createJpegName, currentTimeMillis, location, pictureSize.width, pictureSize.height, orientation, exif, this.mOnPhotoSavedListener, this.mContentResolver, PhotoModule.PIXEL_FORMAT_JPEG);
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

    /* access modifiers changed from: private */
    public void showTapToSnapshotToast() {
        new RotateTextToast((Activity) this.mActivity, (int) C0905R.string.video_snapshot_hint, 0).show();
        Editor edit = this.mPreferences.edit();
        edit.putBoolean(CameraSettings.KEY_VIDEO_FIRST_USE_HINT_SHOWN, false);
        edit.apply();
    }

    public void onCameraPickerClicked(int i) {
        if (!this.mPaused && this.mPendingSwitchCameraId == -1) {
            this.mPendingSwitchCameraId = i;
            Log.d(TAG, "Start to copy texture.");
            this.mSwitchingCamera = true;
            switchCamera();
        }
    }

    public void onShowSwitcherPopup() {
        this.mUI.onShowSwitcherPopup();
    }

    public void onPreviewUIReady() {
        if (!this.mPaused && this.mCameraDevice != null) {
            Log.v(TAG, "onPreviewUIReady");
            if (!this.mPreviewing) {
                startPreview();
            } else {
                synchronized (this.mCameraDevice) {
                    SurfaceHolder surfaceHolder = this.mUI.getSurfaceHolder();
                    if (surfaceHolder == null) {
                        Log.w(TAG, "holder for preview is not ready.");
                        return;
                    }
                    this.mCameraDevice.setPreviewDisplay(surfaceHolder);
                }
            }
        }
    }

    public void onPreviewUIDestroyed() {
        if (this.mMediaRecorderRecording) {
            onStopVideoRecording();
        }
        stopPreview();
    }

    public void onButtonPause() {
        pauseVideoRecording();
    }

    public void onButtonContinue() {
        resumeVideoRecording();
    }

    private void updatePowerMode() {
        String str = this.mParameters.get("low-power-mode-supported");
        if (str != null && CameraUtil.TRUE.equals(str)) {
            String str2 = "low-power-mode";
            if (this.mIsDISEnabled || this.mIsFlipEnabled) {
                this.mParameters.set(str2, "disable");
            } else {
                this.mParameters.set(str2, "enable");
            }
        }
    }

    public void startFaceDetection() {
        if (this.mCameraDevice != null && this.mFaceDetectionEnabled && !this.mFaceDetectionStarted && this.mParameters.getMaxNumDetectedFaces() > 0) {
            boolean z = true;
            this.mFaceDetectionStarted = true;
            CameraInfo cameraInfo = CameraHolder.instance().getCameraInfo()[this.mCameraId];
            VideoUI videoUI = this.mUI;
            int i = this.mCameraDisplayOrientation;
            if (cameraInfo.facing != 0) {
                z = false;
            }
            videoUI.onStartFaceDetection(i, z);
            this.mCameraDevice.setFaceDetectionCallback(this.mHandler, this.mUI);
            StringBuilder sb = new StringBuilder();
            sb.append("start face detection Video ");
            sb.append(this.mParameters.getMaxNumDetectedFaces());
            Log.d(TAG, sb.toString());
            this.mCameraDevice.startFaceDetection();
        }
    }

    public void stopFaceDetection() {
        Log.d(TAG, "stop face detection");
        if (this.mFaceDetectionEnabled && this.mFaceDetectionStarted && this.mParameters.getMaxNumDetectedFaces() > 0) {
            this.mFaceDetectionStarted = false;
            this.mCameraDevice.setFaceDetectionCallback(null, null);
            this.mUI.pauseFaceDetection();
            this.mCameraDevice.stopFaceDetection();
            this.mUI.onStopFaceDetection();
        }
    }

    public void onErrorListener(int i) {
        enableRecordingLocation(false);
    }
}
