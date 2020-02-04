package com.android.camera;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;
import com.android.camera.CameraManager.CameraOpenErrorCallback;
import com.android.camera.ImageTaskManager.TaskListener;
import com.android.camera.app.PanoramaStitchingManager;
import com.android.camera.app.PlaceholderManager;
import com.android.camera.crop.CropActivity;
import com.android.camera.data.CameraPreviewData;
import com.android.camera.data.InProgressDataWrapper;
import com.android.camera.data.LocalData;
import com.android.camera.data.LocalDataAdapter;
import com.android.camera.data.LocalMediaObserver;
import com.android.camera.data.MediaDetails;
import com.android.camera.p004ui.DetailsDialog;
import com.android.camera.p004ui.FilmStripView;
import com.android.camera.p004ui.FilmStripView.ImageData;
import com.android.camera.p004ui.FilmStripView.Listener;
import com.android.camera.p004ui.ModuleSwitcher.ModuleSwitchListener;
import com.android.camera.p004ui.PanoCaptureProcessView;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.tinyplanet.TinyPlanetFragment;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.IntentHelper;
import com.android.camera.util.PersistUtil;
import com.android.camera.util.PhotoSphereHelper.PanoramaViewHelper;
import java.io.File;
import java.util.List;
import org.codeaurora.snapcam.C0905R;

public class CameraActivity extends Activity implements ModuleSwitchListener, OnMenuVisibilityListener, OnShareTargetSelectedListener {
    public static final String ACTION_IMAGE_CAPTURE_SECURE = "android.media.action.IMAGE_CAPTURE_SECURE";
    public static final String ACTION_TRIM_VIDEO = "com.android.camera.action.TRIM";
    private static final String AUTO_TEST_INTENT = "com.android.camera.autotest";
    private static final String CAMERA_API_1_SUPPORT = "camera_api_1_support";
    private static final int CAMERA_HAL_API_VERSION_1_0 = 256;
    public static final String GESTURE_CAMERA_NAME = "com.android.camera.CameraGestureActivity";
    private static final int HIDE_ACTION_BAR = 1;
    private static final String INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE = "android.media.action.STILL_IMAGE_CAMERA_SECURE";
    private static final String KEY_FROM_SNAPCAM = "from-snapcam";
    public static final String KEY_TOTAL_NUMBER = "total-number";
    public static final String MEDIA_ITEM_PATH = "media-item-path";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static boolean PIE_MENU_ENABLED = false;
    private static final int REFOCUS_ACTIVITY_CODE = 1;
    public static final int REQ_CODE_DONT_SWITCH_TO_PREVIEW = 142;
    public static final int REQ_CODE_GCAM_DEBUG_POSTCAPTURE = 999;
    private static final String SCENEMODE_ACTIVITY = "com.android.camera.SceneModeActivity";
    public static final String SECURE_CAMERA_EXTRA = "secure_camera";
    private static final String SETTINGS_ACTIVITY = "com.android.camera.SettingsActivity";
    public static int SETTING_LIST_WIDTH_1 = 250;
    public static int SETTING_LIST_WIDTH_2 = 250;
    private static final long SHOW_ACTION_BAR_TIMEOUT_MS = 3000;
    private static final int SUPPORT_ALL = -1;
    private static final int SUPPORT_CROP = 8;
    private static final int SUPPORT_DELETE = 1;
    private static final int SUPPORT_EDIT = 32;
    private static final int SUPPORT_INFO = 4;
    private static final int SUPPORT_ROTATE = 2;
    private static final int SUPPORT_SETAS = 16;
    private static final int SUPPORT_SHARE = 128;
    private static final int SUPPORT_SHARE_PANORAMA360 = 256;
    private static final int SUPPORT_SHOW_ON_MAP = 512;
    private static final int SUPPORT_TRIM = 64;
    private static final int SWITCH_SAVE_PATH = 2;
    private static final String TAG = "CAM_Activity";
    private static final int VIDEO_RECOIDING_NOT_ENOUGH = 3;
    private final int DEFAULT_SYSTEM_UI_VISIBILITY = 1024;
    private boolean isCameraOpenAndWorking = false;
    private FrameLayout mAboveFilmstripControlLayout;
    /* access modifiers changed from: private */
    public ActionBar mActionBar;
    private Menu mActionBarMenu;
    private boolean mAutoTestEnabled = false;
    private BroadcastReceiver mAutoTestReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String str = "KEY";
            if (intent.hasExtra(str)) {
                String str2 = "VALUE";
                if (intent.hasExtra(str2)) {
                    String string = intent.getExtras().getString(str);
                    String string2 = intent.getExtras().getString(str2);
                    if (CameraActivity.this.mCurrentModule != null) {
                        CameraActivity.this.mCurrentModule.setPreferenceForTest(string, string2);
                    }
                }
            }
        }
    };
    private ProgressBar mBottomProgress;
    private View mCameraCaptureModuleRootView;
    private CameraOpenErrorCallback mCameraOpenErrorCallback = new CameraOpenErrorCallback() {
        public void onCameraDisabled(int i) {
            CameraUtil.showErrorAndFinish(CameraActivity.this, C0905R.string.camera_disabled);
        }

        public void onDeviceOpenFailure(int i) {
            CameraActivity.this.showOpenCameraErrorDialog();
        }

        public void onReconnectionFailure(CameraManager cameraManager) {
            CameraActivity.this.showOpenCameraErrorDialog();
        }

        public void onStartPreviewFailure(int i) {
            CameraActivity.this.showOpenCameraErrorDialog();
        }
    };
    private View mCameraPanoModuleRootView;
    private View mCameraPhotoModuleRootView;
    private CameraPreviewData mCameraPreviewData;
    private FrameLayout mCameraRootFrame;
    private View mCameraVideoModuleRootView;
    /* access modifiers changed from: private */
    public CaptureModule mCaptureModule;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            CameraActivity.this.mMediaSaveService = ((LocalBinder) iBinder).getService();
            CameraActivity.this.mCurrentModule.onMediaSaveServiceConnected(CameraActivity.this.mMediaSaveService);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            if (CameraActivity.this.mMediaSaveService != null) {
                CameraActivity.this.mMediaSaveService.setListener(null);
                CameraActivity.this.mMediaSaveService = null;
            }
        }
    };
    /* access modifiers changed from: private */
    public CameraModule mCurrentModule;
    private int mCurrentModuleIndex;
    private Cursor mCursor;
    /* access modifiers changed from: private */
    public LocalDataAdapter mDataAdapter;
    /* access modifiers changed from: private */
    public boolean mDataRequested;
    private boolean mDeveloperMenuEnabled = false;
    private Listener mFilmStripListener = new Listener() {
        public void onDataPromoted(int i) {
            CameraActivity.this.fileNameFromDataID(i);
            CameraActivity.this.removeData(i);
        }

        public void onDataDemoted(int i) {
            CameraActivity.this.fileNameFromDataID(i);
            CameraActivity.this.removeData(i);
        }

        public void onDataFullScreenChange(int i, boolean z) {
            boolean isCameraPreview = isCameraPreview(i);
            if (z && isCameraPreview && CameraActivity.this.hasWindowFocus()) {
                CameraActivity.this.updateStorageSpaceAndHint();
            }
            if (isCameraPreview) {
                return;
            }
            if (!z) {
                CameraActivity.this.setSystemBarsVisibility(true, false);
            } else if (CameraActivity.this.mActionBar.isShowing()) {
                CameraActivity.this.mMainHandler.sendEmptyMessageDelayed(1, CameraActivity.SHOW_ACTION_BAR_TIMEOUT_MS);
            }
        }

        private boolean isCameraPreview(int i) {
            LocalData localData = CameraActivity.this.mDataAdapter.getLocalData(i);
            boolean z = false;
            if (localData == null) {
                Log.w(CameraActivity.TAG, "Current data ID not found.");
                return false;
            }
            if (localData.getLocalDataType() == 1) {
                z = true;
            }
            return z;
        }

        public void onReload() {
            CameraActivity.this.setPreviewControlsVisibility(true);
            CameraActivity.this.setSystemBarsVisibility(false);
        }

        public void onCurrentDataCentered(int i) {
            if ((i == 0 || CameraActivity.this.mFilmStripView.isCameraPreview()) && !CameraActivity.this.arePreviewControlsVisible()) {
                CameraActivity.this.setPreviewControlsVisibility(true);
                CameraActivity.this.setSystemBarsVisibility(false);
                CameraActivity.this.mFilmStripView.getController().goToFullScreen();
            }
        }

        public void onCurrentDataOffCentered(int i) {
            if ((i == 0 || CameraActivity.this.mFilmStripView.isCameraPreview()) && CameraActivity.this.arePreviewControlsVisible()) {
                CameraActivity.this.setPreviewControlsVisibility(false);
            }
        }

        public void onDataFocusChanged(final int i, final boolean z) {
            boolean isCameraPreview = isCameraPreview(i);
            if (CameraActivity.this.mFilmStripView.inFullScreen() && isCameraPreview && CameraActivity.this.hasWindowFocus()) {
                CameraActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        CameraActivity.this.updateStorageSpaceAndHint();
                    }
                });
            }
            if (CameraActivity.this.mMainHandler.hasMessages(1)) {
                CameraActivity.this.mMainHandler.removeMessages(1);
                CameraActivity.this.mMainHandler.sendEmptyMessageDelayed(1, CameraActivity.SHOW_ACTION_BAR_TIMEOUT_MS);
            }
            CameraActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    LocalData localData = CameraActivity.this.mDataAdapter.getLocalData(i);
                    if (localData == null) {
                        Log.w(CameraActivity.TAG, "Current data ID not found.");
                        CameraActivity.this.hidePanoStitchingProgress();
                        return;
                    }
                    boolean z = localData.getLocalDataType() == 1;
                    if (!z) {
                        if (z) {
                            CameraActivity.this.mCurrentModule.onPreviewFocusChanged(false);
                            CameraActivity.this.setSystemBarsVisibility(true);
                        }
                        CameraActivity.this.hidePanoStitchingProgress();
                    } else {
                        if (z) {
                            CameraActivity.this.setSystemBarsVisibility(false);
                            if (CameraActivity.this.mPendingDeletion) {
                                CameraActivity.this.performDeletion();
                            }
                        } else {
                            CameraActivity.this.updateActionBarMenu(i);
                        }
                        Uri contentUri = localData.getContentUri();
                        if (contentUri == null) {
                            CameraActivity.this.hidePanoStitchingProgress();
                            return;
                        }
                        int taskProgress = CameraActivity.this.mPanoramaManager.getTaskProgress(contentUri);
                        if (taskProgress < 0) {
                            CameraActivity.this.hidePanoStitchingProgress();
                        } else {
                            CameraActivity.this.showPanoStitchingProgress();
                            CameraActivity.this.updateStitchingProgress(taskProgress);
                        }
                    }
                }
            });
        }

        public void onToggleSystemDecorsVisibility(int i) {
            if (CameraActivity.this.mActionBar.isShowing()) {
                CameraActivity.this.setSystemBarsVisibility(false);
            } else if (!isCameraPreview(i)) {
                CameraActivity.this.setSystemBarsVisibility(true, true);
            }
        }

        public void setSystemDecorsVisibility(boolean z) {
            CameraActivity.this.setSystemBarsVisibility(z);
        }
    };
    /* access modifiers changed from: private */
    public FilmStripView mFilmStripView;
    private boolean mForceReleaseCamera = false;
    private boolean mGotoGallery = false;
    private Intent mImageShareIntent;
    private boolean mIsEditActivityInProgress = false;
    /* access modifiers changed from: private */
    public boolean mIsUndoingDeletion = false;
    /* access modifiers changed from: private */
    public int mLastRawOrientation;
    private LocalMediaObserver mLocalImagesObserver;
    private LocalMediaObserver mLocalVideosObserver;
    /* access modifiers changed from: private */
    public Handler mMainHandler;
    /* access modifiers changed from: private */
    public MediaSaveService mMediaSaveService;
    /* access modifiers changed from: private */
    public Uri[] mNfcPushUris = new Uri[1];
    private OnActionBarVisibilityListener mOnActionBarVisibilityListener = null;
    private MyOrientationEventListener mOrientationListener;
    private PanoCaptureModule mPano2Module;
    private WideAnglePanoramaModule mPanoModule;
    private View mPanoStitchingPanel;
    /* access modifiers changed from: private */
    public PanoramaStitchingManager mPanoramaManager;
    private ShareActionProvider mPanoramaShareActionProvider;
    private Intent mPanoramaShareIntent;
    private PanoramaViewHelper mPanoramaViewHelper;
    /* access modifiers changed from: private */
    public boolean mPaused = true;
    /* access modifiers changed from: private */
    public boolean mPendingDeletion = false;
    private PhotoModule mPhotoModule;
    private TaskListener mPlaceholderListener = new TaskListener() {
        public void onTaskProgress(String str, Uri uri, int i) {
        }

        public void onTaskQueued(String str, final Uri uri) {
            CameraActivity.this.mMainHandler.post(new Runnable() {
                public void run() {
                    CameraActivity.this.notifyNewMedia(uri);
                    int findDataByContentUri = CameraActivity.this.mDataAdapter.findDataByContentUri(uri);
                    if (findDataByContentUri != -1) {
                        CameraActivity.this.mDataAdapter.updateData(findDataByContentUri, new InProgressDataWrapper(CameraActivity.this.mDataAdapter.getLocalData(findDataByContentUri), true));
                    }
                }
            });
        }

        public void onTaskDone(String str, final Uri uri) {
            CameraActivity.this.mMainHandler.post(new Runnable() {
                public void run() {
                    CameraActivity.this.mDataAdapter.refresh(CameraActivity.this.getContentResolver(), uri);
                }
            });
        }
    };
    private PlaceholderManager mPlaceholderManager;
    private FrameLayout mPreviewContentLayout;
    private View mPreviewCover;
    private boolean mResetToPreviewOnResume = true;
    private int mResultCodeForTesting;
    private Intent mResultDataForTesting;
    private BroadcastReceiver mSDcardMountedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            StringBuilder sb = new StringBuilder();
            sb.append("SDcard status changed, update storage space  ");
            sb.append(intent.getAction());
            Log.d(CameraActivity.TAG, sb.toString());
            if ("android.intent.action.MEDIA_UNMOUNTED".equals(intent.getAction())) {
                if (CameraActivity.this.settingsManager != null) {
                    CameraActivity.this.settingsManager.setValue("pref_camera_savepath_key", "0");
                }
                CameraActivity cameraActivity = CameraActivity.this;
                cameraActivity.mDataAdapter = cameraActivity.mWrappedDataAdapter;
                CameraActivity.this.mFilmStripView.setDataAdapter(CameraActivity.this.mDataAdapter);
                if (!CameraActivity.this.isCaptureIntent()) {
                    CameraActivity.this.mDataAdapter.requestLoad(CameraActivity.this.getContentResolver());
                    CameraActivity.this.mDataRequested = true;
                }
                CameraActivity.this.mFilmStripView.getController().goToFirstItem();
                Storage.setSaveSDCard(false);
            }
            CameraActivity.this.updateStorageSpaceAndHint();
        }
    };
    private boolean mSecureCamera;
    /* access modifiers changed from: private */
    public boolean mShouldUpdateThumbnailFromFile = false;
    private ShareActionProvider mStandardShareActionProvider;
    private Intent mStandardShareIntent;
    private TaskListener mStitchingListener = new TaskListener() {
        public void onTaskQueued(String str, final Uri uri) {
            CameraActivity.this.mMainHandler.post(new Runnable() {
                public void run() {
                    CameraActivity.this.notifyNewMedia(uri);
                    int findDataByContentUri = CameraActivity.this.mDataAdapter.findDataByContentUri(uri);
                    if (findDataByContentUri != -1) {
                        CameraActivity.this.mDataAdapter.updateData(findDataByContentUri, new InProgressDataWrapper(CameraActivity.this.mDataAdapter.getLocalData(findDataByContentUri)));
                    }
                }
            });
        }

        public void onTaskDone(String str, final Uri uri) {
            StringBuilder sb = new StringBuilder();
            sb.append("onTaskDone:");
            sb.append(str);
            Log.v(CameraActivity.TAG, sb.toString());
            CameraActivity.this.mMainHandler.post(new Runnable() {
                public void run() {
                    if (CameraActivity.this.mFilmStripView.getCurrentId() == CameraActivity.this.mDataAdapter.findDataByContentUri(uri)) {
                        CameraActivity.this.hidePanoStitchingProgress();
                        CameraActivity.this.updateStitchingProgress(0);
                    }
                    CameraActivity.this.mDataAdapter.refresh(CameraActivity.this.getContentResolver(), uri);
                }
            });
        }

        public void onTaskProgress(String str, final Uri uri, final int i) {
            CameraActivity.this.mMainHandler.post(new Runnable() {
                public void run() {
                    int currentId = CameraActivity.this.mFilmStripView.getCurrentId();
                    if (currentId != -1 && uri.equals(CameraActivity.this.mDataAdapter.getLocalData(currentId).getContentUri())) {
                        CameraActivity.this.updateStitchingProgress(i);
                    }
                }
            });
        }
    };
    private OnScreenHint mStorageHint;
    private long mStorageSpaceBytes = Storage.LOW_STORAGE_THRESHOLD_BYTES;
    private final Object mStorageSpaceLock = new Object();
    /* access modifiers changed from: private */
    public ImageView mThumbnail;
    private CircularDrawable mThumbnailDrawable;
    /* access modifiers changed from: private */
    public ViewGroup mUndoDeletionBar;
    private UpdateThumbnailTask mUpdateThumbnailTask;
    private VideoModule mVideoModule;
    private Intent mVideoShareIntent;
    private WakeLock mWakeLock;
    /* access modifiers changed from: private */
    public LocalDataAdapter mWrappedDataAdapter;
    /* access modifiers changed from: private */
    public SettingsManager settingsManager;

    private class CircularDrawable extends Drawable {
        private final BitmapShader mBitmapShader;
        private int mLength;
        private final Paint mPaint;
        private Rect mRect;

        public int getOpacity() {
            return -3;
        }

        public CircularDrawable(Bitmap bitmap) {
            float f;
            float f2;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int dimensionPixelSize = CameraActivity.this.getResources().getDimensionPixelSize(C0905R.dimen.capture_size);
            if (Math.min(width, height) < dimensionPixelSize) {
                Matrix matrix = new Matrix();
                if (width > height) {
                    f = (float) dimensionPixelSize;
                    f2 = (float) height;
                } else {
                    f = (float) dimensionPixelSize;
                    f2 = (float) width;
                }
                float f3 = f / f2;
                matrix.postScale(f3, f3);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                width = (int) (((float) width) * f3);
                height = (int) (((float) height) * f3);
            }
            if (width > height) {
                this.mLength = height;
                bitmap = Bitmap.createBitmap(bitmap, (width - height) / 2, 0, height, height);
            } else if (width < height) {
                this.mLength = width;
                bitmap = Bitmap.createBitmap(bitmap, 0, (height - width) / 2, width, width);
            } else {
                this.mLength = width;
            }
            TileMode tileMode = TileMode.CLAMP;
            this.mBitmapShader = new BitmapShader(bitmap, tileMode, tileMode);
            this.mPaint = new Paint(1);
            this.mPaint.setShader(this.mBitmapShader);
        }

        /* access modifiers changed from: protected */
        public void onBoundsChange(Rect rect) {
            super.onBoundsChange(rect);
            this.mRect = rect;
        }

        public void draw(Canvas canvas) {
            RectF rectF = new RectF(this.mRect);
            Rect rect = this.mRect;
            canvas.drawRoundRect(rectF, (float) ((rect.right - rect.left) / 2), (float) ((rect.bottom - rect.top) / 2), this.mPaint);
        }

        public void setAlpha(int i) {
            this.mPaint.setAlpha(i);
        }

        public void setColorFilter(ColorFilter colorFilter) {
            this.mPaint.setColorFilter(colorFilter);
        }

        public int getIntrinsicWidth() {
            return this.mLength;
        }

        public int getIntrinsicHeight() {
            return this.mLength;
        }
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                removeMessages(1);
                CameraActivity.this.setSystemBarsVisibility(false);
            } else if (i == 2) {
                Log.d(CameraActivity.TAG, "if save in phone,swithc in sdcard when storage not enough");
                CameraActivity.this.mCurrentModule.onSwitchSavePath();
            } else if (i == 3) {
                CameraActivity.this.mCurrentModule.onStorageNotEnoughRecordingVideo();
            }
        }
    }

    private class MyOrientationEventListener extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        public void onOrientationChanged(int i) {
            if (i != -1) {
                CameraActivity.this.mLastRawOrientation = i;
                CameraActivity.this.mCurrentModule.onOrientationChanged(i);
            }
        }
    }

    public interface OnActionBarVisibilityListener {
        void onActionBarVisibilityChanged(boolean z);
    }

    protected interface OnStorageUpdateDoneListener {
        void onStorageUpdateDone(long j);
    }

    private class UpdateThumbnailTask extends AsyncTask<Void, Void, Bitmap> {
        private boolean mCheckOrientation;
        private byte[] mJpegData;

        public UpdateThumbnailTask(byte[] bArr, boolean z) {
            this.mJpegData = bArr;
            this.mCheckOrientation = z;
        }

        /* access modifiers changed from: protected */
        public Bitmap doInBackground(Void... voidArr) {
            if (this.mJpegData != null) {
                return decodeImageCenter(null);
            }
            ImageData imageData = CameraActivity.this.getDataAdapter().getImageData(1);
            if (imageData == null) {
                return null;
            }
            String access$2400 = CameraActivity.this.getPathFromUri(imageData.getContentUri());
            if (access$2400 == null) {
                return null;
            }
            if (imageData.isPhoto()) {
                return decodeImageCenter(access$2400);
            }
            return ThumbnailUtils.createVideoThumbnail(access$2400, 1);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                if (CameraActivity.this.mThumbnail != null) {
                    CameraActivity.this.mThumbnail.setImageResource(C0905R.C0906drawable.ic_preview_thumb);
                }
            } else if (CameraActivity.this.mShouldUpdateThumbnailFromFile || CameraActivity.this.mCaptureModule.isLongShotActive() || CameraActivity.this.mCaptureModule.isRecordingVideo() || CameraActivity.this.mCaptureModule.isSelfieMirrorOn()) {
                Log.d(CameraActivity.TAG, "updateThumbnail from file");
                CameraActivity.this.updateThumbnail(bitmap);
                CameraActivity.this.mShouldUpdateThumbnailFromFile = false;
            }
            this.mJpegData = null;
        }

        /* access modifiers changed from: protected */
        public void onCancelled(Bitmap bitmap) {
            if (bitmap != null) {
                bitmap.recycle();
            }
            this.mJpegData = null;
        }

        /* JADX WARNING: Removed duplicated region for block: B:13:0x0029  */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x002e  */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x0037  */
        /* JADX WARNING: Removed duplicated region for block: B:18:0x0039  */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x004a A[LOOP:0: B:21:0x004a->B:23:0x0050, LOOP_START, PHI: r8 
          PHI: (r8v4 int) = (r8v1 int), (r8v5 int) binds: [B:20:0x0048, B:23:0x0050] A[DONT_GENERATE, DONT_INLINE]] */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x006f A[Catch:{ IOException -> 0x009c }] */
        /* JADX WARNING: Removed duplicated region for block: B:29:0x0074 A[Catch:{ IOException -> 0x009c }] */
        /* JADX WARNING: Removed duplicated region for block: B:32:0x0083  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private android.graphics.Bitmap decodeImageCenter(java.lang.String r15) {
            /*
                r14 = this;
                boolean r0 = r14.mCheckOrientation
                r1 = 0
                if (r0 == 0) goto L_0x001c
                com.android.camera.exif.ExifInterface r0 = new com.android.camera.exif.ExifInterface
                r0.<init>()
                byte[] r2 = r14.mJpegData     // Catch:{ IOException -> 0x001c }
                if (r2 == 0) goto L_0x0014
                byte[] r2 = r14.mJpegData     // Catch:{ IOException -> 0x001c }
                r0.readExif(r2)     // Catch:{ IOException -> 0x001c }
                goto L_0x0017
            L_0x0014:
                r0.readExif(r15)     // Catch:{ IOException -> 0x001c }
            L_0x0017:
                int r0 = com.android.camera.Exif.getOrientation(r0)     // Catch:{ IOException -> 0x001c }
                goto L_0x001d
            L_0x001c:
                r0 = r1
            L_0x001d:
                android.graphics.BitmapFactory$Options r2 = new android.graphics.BitmapFactory$Options
                r2.<init>()
                r3 = 1
                r2.inJustDecodeBounds = r3
                byte[] r4 = r14.mJpegData
                if (r4 == 0) goto L_0x002e
                int r5 = r4.length
                android.graphics.BitmapFactory.decodeByteArray(r4, r1, r5, r2)
                goto L_0x0031
            L_0x002e:
                android.graphics.BitmapFactory.decodeFile(r15, r2)
            L_0x0031:
                int r4 = r2.outWidth
                int r5 = r2.outHeight
                if (r4 <= r5) goto L_0x0039
                r6 = r5
                goto L_0x003a
            L_0x0039:
                r6 = r4
            L_0x003a:
                com.android.camera.CameraActivity r7 = com.android.camera.CameraActivity.this
                android.content.res.Resources r7 = r7.getResources()
                r8 = 2131099681(0x7f060021, float:1.7811722E38)
                int r7 = r7.getDimensionPixelSize(r8)
                r8 = r3
                if (r6 <= r7) goto L_0x0053
            L_0x004a:
                int r9 = r6 / r8
                int r9 = r9 / 2
                if (r9 <= r7) goto L_0x0053
                int r8 = r8 * 2
                goto L_0x004a
            L_0x0053:
                int r7 = r7 * r8
                android.graphics.Rect r6 = new android.graphics.Rect
                int r9 = r4 - r7
                int r9 = r9 / 2
                int r10 = r5 - r7
                int r10 = r10 / 2
                int r4 = r4 + r7
                int r4 = r4 / 2
                int r5 = r5 + r7
                int r5 = r5 / 2
                r6.<init>(r9, r10, r4, r5)
                r2.inJustDecodeBounds = r1
                r2.inSampleSize = r8
                byte[] r4 = r14.mJpegData     // Catch:{ IOException -> 0x009c }
                if (r4 != 0) goto L_0x0074
                android.graphics.BitmapRegionDecoder r14 = android.graphics.BitmapRegionDecoder.newInstance(r15, r3)     // Catch:{ IOException -> 0x009c }
                goto L_0x007d
            L_0x0074:
                byte[] r15 = r14.mJpegData     // Catch:{ IOException -> 0x009c }
                byte[] r14 = r14.mJpegData     // Catch:{ IOException -> 0x009c }
                int r14 = r14.length     // Catch:{ IOException -> 0x009c }
                android.graphics.BitmapRegionDecoder r14 = android.graphics.BitmapRegionDecoder.newInstance(r15, r1, r14, r3)     // Catch:{ IOException -> 0x009c }
            L_0x007d:
                android.graphics.Bitmap r7 = r14.decodeRegion(r6, r2)
                if (r0 == 0) goto L_0x009b
                android.graphics.Matrix r12 = new android.graphics.Matrix
                r12.<init>()
                float r14 = (float) r0
                r12.setRotate(r14)
                r8 = 0
                r9 = 0
                int r10 = r7.getWidth()
                int r11 = r7.getHeight()
                r13 = 0
                android.graphics.Bitmap r7 = android.graphics.Bitmap.createBitmap(r7, r8, r9, r10, r11, r12, r13)
            L_0x009b:
                return r7
            L_0x009c:
                r14 = 0
                return r14
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CameraActivity.UpdateThumbnailTask.decodeImageCenter(java.lang.String):android.graphics.Bitmap");
        }
    }

    private void registerSDcardMountedReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_MOUNTED");
        intentFilter.addAction("android.intent.action.MEDIA_SHARED");
        intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        intentFilter.addDataScheme("file");
        registerReceiver(this.mSDcardMountedReceiver, intentFilter);
    }

    public void setOnActionBarVisibilityListener(OnActionBarVisibilityListener onActionBarVisibilityListener) {
        this.mOnActionBarVisibilityListener = onActionBarVisibilityListener;
    }

    public static boolean isPieMenuEnabled() {
        return PIE_MENU_ENABLED;
    }

    public boolean isDeveloperMenuEnabled() {
        return this.mDeveloperMenuEnabled;
    }

    public void enableDeveloperMenu() {
        this.mDeveloperMenuEnabled = true;
    }

    /* access modifiers changed from: private */
    public String fileNameFromDataID(int i) {
        return new File(this.mDataAdapter.getLocalData(i).getPath()).getName();
    }

    private boolean cameraAPICheck() {
        String str = TAG;
        String str2 = CAMERA_API_1_SUPPORT;
        boolean z = true;
        try {
            ((Camera) Class.forName("android.hardware.Camera").getMethod("openLegacy", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(0), Integer.valueOf(256)})).release();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append("cameraAPICheck message=");
            sb.append(e.getCause().getLocalizedMessage());
            Log.d(str, sb.toString());
            if (e.getCause().getLocalizedMessage().indexOf("Unknown camera error") != -1) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("cameraAPICheck support1 =");
                sb2.append(false);
                Log.d(str, sb2.toString());
            }
        } catch (Throwable unused) {
        }
        Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
        edit.putBoolean(str2, z);
        edit.commit();
        return z;
        z = false;
        Editor edit2 = PreferenceManager.getDefaultSharedPreferences(this).edit();
        edit2.putBoolean(str2, z);
        edit2.commit();
        return z;
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(2:24|25) */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        android.util.Log.w(r8, r5);
        r0 = new android.content.Intent(r7, r2);
        r0.putExtra(r6, true);
        startActivity(r0);
        r11.mGotoGallery = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00be, code lost:
        android.util.Log.w(r8, r4);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:24:0x00ad */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void gotoGallery() {
        /*
            r11 = this;
            com.android.camera.data.LocalDataAdapter r0 = r11.getDataAdapter()
            r1 = 1
            com.android.camera.ui.FilmStripView$ImageData r2 = r0.getImageData(r1)
            if (r2 != 0) goto L_0x000c
            return
        L_0x000c:
            android.net.Uri r2 = r2.getContentUri()
            com.android.camera.CameraModule r3 = r11.mCurrentModule
            boolean r4 = r3 instanceof com.android.camera.PhotoModule
            if (r4 == 0) goto L_0x002f
            com.android.camera.PhotoModule r3 = (com.android.camera.PhotoModule) r3
            boolean r3 = r3.isRefocus()
            if (r3 == 0) goto L_0x002f
            android.content.Intent r0 = new android.content.Intent
            r0.<init>()
            java.lang.Class<com.android.camera.RefocusActivity> r1 = com.android.camera.RefocusActivity.class
            r0.setClass(r11, r1)
            r0.setData(r2)
            r11.startActivity(r0)
            return
        L_0x002f:
            com.android.camera.CameraModule r3 = r11.mCurrentModule
            boolean r4 = r3 instanceof com.android.camera.CaptureModule
            if (r4 == 0) goto L_0x005c
            com.android.camera.CaptureModule r3 = (com.android.camera.CaptureModule) r3
            boolean r3 = r3.isRefocus()
            if (r3 == 0) goto L_0x005c
            android.content.Intent r0 = new android.content.Intent
            r0.<init>()
            java.lang.Class<com.android.camera.RefocusActivity> r3 = com.android.camera.RefocusActivity.class
            r0.setClass(r11, r3)
            r0.setData(r2)
            r0.setFlags(r1)
            boolean r2 = r11.isSecureCamera()
            if (r2 == 0) goto L_0x0058
            java.lang.String r2 = "android.media.action.STILL_IMAGE_CAMERA_SECURE"
            r0.setAction(r2)
        L_0x0058:
            r11.startActivityForResult(r0, r1)
            return
        L_0x005c:
            boolean r3 = r11.isSecureCamera()
            java.lang.String r4 = "No Activity could be found to open image or video"
            java.lang.String r5 = "Gallery not found"
            java.lang.String r6 = "from-snapcam"
            java.lang.String r7 = "android.intent.action.VIEW"
            java.lang.String r8 = "CAM_Activity"
            if (r3 == 0) goto L_0x00c2
            android.content.Intent r3 = com.android.camera.util.IntentHelper.getGalleryIntent(r11)     // Catch:{ ActivityNotFoundException -> 0x00ad }
            r3.setAction(r7)     // Catch:{ ActivityNotFoundException -> 0x00ad }
            r3.setData(r2)     // Catch:{ ActivityNotFoundException -> 0x00ad }
            r3.putExtra(r6, r1)     // Catch:{ ActivityNotFoundException -> 0x00ad }
            java.lang.String r9 = "total-number"
            int r10 = r0.getTotalNumber()     // Catch:{ ActivityNotFoundException -> 0x00ad }
            int r10 = r10 - r1
            r3.putExtra(r9, r10)     // Catch:{ ActivityNotFoundException -> 0x00ad }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ ActivityNotFoundException -> 0x00ad }
            r9.<init>()     // Catch:{ ActivityNotFoundException -> 0x00ad }
            java.lang.String r10 = "expose number of photos:"
            r9.append(r10)     // Catch:{ ActivityNotFoundException -> 0x00ad }
            int r10 = r0.getTotalNumber()     // Catch:{ ActivityNotFoundException -> 0x00ad }
            r9.append(r10)     // Catch:{ ActivityNotFoundException -> 0x00ad }
            java.lang.String r9 = r9.toString()     // Catch:{ ActivityNotFoundException -> 0x00ad }
            android.util.Log.d(r8, r9)     // Catch:{ ActivityNotFoundException -> 0x00ad }
            java.lang.String r9 = "showWhenLocked"
            r3.putExtra(r9, r1)     // Catch:{ ActivityNotFoundException -> 0x00ad }
            int r0 = r0.getTotalNumber()     // Catch:{ ActivityNotFoundException -> 0x00ad }
            int r0 = r0 - r1
            if (r0 <= r1) goto L_0x00d4
            r11.startActivity(r3)     // Catch:{ ActivityNotFoundException -> 0x00ad }
            r11.mGotoGallery = r1     // Catch:{ ActivityNotFoundException -> 0x00ad }
            goto L_0x00d4
        L_0x00ad:
            android.util.Log.w(r8, r5)     // Catch:{ ActivityNotFoundException -> 0x00be }
            android.content.Intent r0 = new android.content.Intent     // Catch:{ ActivityNotFoundException -> 0x00be }
            r0.<init>(r7, r2)     // Catch:{ ActivityNotFoundException -> 0x00be }
            r0.putExtra(r6, r1)     // Catch:{ ActivityNotFoundException -> 0x00be }
            r11.startActivity(r0)     // Catch:{ ActivityNotFoundException -> 0x00be }
            r11.mGotoGallery = r1     // Catch:{ ActivityNotFoundException -> 0x00be }
            goto L_0x00d4
        L_0x00be:
            android.util.Log.w(r8, r4)
            goto L_0x00d4
        L_0x00c2:
            android.util.Log.w(r8, r5)     // Catch:{ ActivityNotFoundException -> 0x00d1 }
            android.content.Intent r0 = new android.content.Intent     // Catch:{ ActivityNotFoundException -> 0x00d1 }
            r0.<init>(r7, r2)     // Catch:{ ActivityNotFoundException -> 0x00d1 }
            r0.putExtra(r6, r1)     // Catch:{ ActivityNotFoundException -> 0x00d1 }
            r11.startActivity(r0)     // Catch:{ ActivityNotFoundException -> 0x00d1 }
            goto L_0x00d4
        L_0x00d1:
            android.util.Log.w(r8, r4)
        L_0x00d4:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CameraActivity.gotoGallery():void");
    }

    public void setSystemBarsVisibility(boolean z) {
        setSystemBarsVisibility(z, false);
    }

    /* access modifiers changed from: private */
    public void setSystemBarsVisibility(boolean z, boolean z2) {
        this.mMainHandler.removeMessages(1);
        View decorView = getWindow().getDecorView();
        int i = (z ? 0 : 5) | 1024;
        if (i != decorView.getSystemUiVisibility()) {
            decorView.setSystemUiVisibility(i);
        }
        if (z != this.mActionBar.isShowing()) {
            if (z) {
                this.mActionBar.show();
            } else {
                this.mActionBar.hide();
            }
            OnActionBarVisibilityListener onActionBarVisibilityListener = this.mOnActionBarVisibilityListener;
            if (onActionBarVisibilityListener != null) {
                onActionBarVisibilityListener.onActionBarVisibilityChanged(z);
            }
        }
        if (z && z2) {
            this.mMainHandler.sendEmptyMessageDelayed(1, SHOW_ACTION_BAR_TIMEOUT_MS);
        }
    }

    /* access modifiers changed from: private */
    public void hidePanoStitchingProgress() {
        this.mPanoStitchingPanel.setVisibility(8);
    }

    /* access modifiers changed from: private */
    public void showPanoStitchingProgress() {
        this.mPanoStitchingPanel.setVisibility(0);
    }

    /* access modifiers changed from: private */
    public void updateStitchingProgress(int i) {
        this.mBottomProgress.setProgress(i);
    }

    @TargetApi(16)
    private void setupNfcBeamPush() {
        NfcAdapter defaultAdapter = NfcAdapter.getDefaultAdapter(this);
        if (defaultAdapter != null) {
            if (!ApiHelper.HAS_SET_BEAM_PUSH_URIS) {
                defaultAdapter.setNdefPushMessage(null, this, new Activity[0]);
                return;
            }
            defaultAdapter.setBeamPushUris(null, this);
            defaultAdapter.setBeamPushUrisCallback(new CreateBeamUrisCallback() {
                public Uri[] createBeamUris(NfcEvent nfcEvent) {
                    return CameraActivity.this.mNfcPushUris;
                }
            }, this);
        }
    }

    private void setNfcBeamPushUri(Uri uri) {
        this.mNfcPushUris[0] = uri;
    }

    public LocalDataAdapter getDataAdapter() {
        return this.mDataAdapter;
    }

    /* access modifiers changed from: private */
    public String getPathFromUri(Uri uri) {
        String str = "_data";
        Cursor query = getContentResolver().query(uri, new String[]{str}, null, null, null);
        String str2 = null;
        if (query == null) {
            return null;
        }
        int columnIndexOrThrow = query.getColumnIndexOrThrow(str);
        if (query.moveToFirst()) {
            str2 = query.getString(columnIndexOrThrow);
        }
        query.close();
        return str2;
    }

    public void updateThumbnail(byte[] bArr) {
        UpdateThumbnailTask updateThumbnailTask = this.mUpdateThumbnailTask;
        if (updateThumbnailTask != null) {
            updateThumbnailTask.cancel(true);
        }
        this.mUpdateThumbnailTask = new UpdateThumbnailTask(bArr, true);
        this.mUpdateThumbnailTask.execute(new Void[0]);
    }

    public void updateThumbnail(Bitmap bitmap) {
        if (bitmap != null) {
            this.mThumbnailDrawable = new CircularDrawable(bitmap);
            ImageView imageView = this.mThumbnail;
            if (imageView != null) {
                imageView.setImageDrawable(this.mThumbnailDrawable);
                if (!isSecureCamera()) {
                    this.mThumbnail.setVisibility(0);
                } else {
                    CameraModule cameraModule = this.mCurrentModule;
                    if (!(cameraModule instanceof CaptureModule)) {
                        this.mThumbnail.setVisibility(8);
                    } else if (((CaptureModule) cameraModule).isRefocus()) {
                        this.mThumbnail.setVisibility(0);
                    } else {
                        this.mThumbnail.setVisibility(0);
                    }
                }
            }
        }
    }

    public void updateThumbnail(ImageView imageView) {
        this.mThumbnail = imageView;
        ImageView imageView2 = this.mThumbnail;
        if (imageView2 != null) {
            CircularDrawable circularDrawable = this.mThumbnailDrawable;
            if (circularDrawable != null) {
                imageView2.setImageDrawable(circularDrawable);
                if (isSecureCamera() || isCaptureIntent()) {
                    CameraModule cameraModule = this.mCurrentModule;
                    if (!(cameraModule instanceof CaptureModule)) {
                        this.mThumbnail.setVisibility(8);
                    } else if (((CaptureModule) cameraModule).isRefocus()) {
                        this.mThumbnail.setVisibility(0);
                    } else {
                        this.mThumbnail.setVisibility(0);
                    }
                } else {
                    this.mThumbnail.setVisibility(0);
                }
            }
        }
    }

    public void updateThumbnail(boolean z) {
        if (z) {
            CameraModule cameraModule = this.mCurrentModule;
            if (!(cameraModule instanceof VideoModule) && (!(cameraModule instanceof CaptureModule) || !z)) {
                return;
            }
        }
        new UpdateThumbnailTask(null, true).execute(new Void[0]);
    }

    public void setShouldUpdateThumbnailFromFile(boolean z) {
        this.mShouldUpdateThumbnailFromFile = z;
    }

    private void setStandardShareIntent(Uri uri, String str) {
        this.mStandardShareIntent = getShareIntentFromType(str);
        Intent intent = this.mStandardShareIntent;
        if (intent != null) {
            intent.putExtra("android.intent.extra.STREAM", uri);
            this.mStandardShareIntent.addFlags(1);
            ShareActionProvider shareActionProvider = this.mStandardShareActionProvider;
            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(this.mStandardShareIntent);
            }
        }
    }

    private Intent getShareIntentFromType(String str) {
        String str2 = "android.intent.action.SEND";
        if (str.startsWith("video/")) {
            if (this.mVideoShareIntent == null) {
                this.mVideoShareIntent = new Intent(str2);
                this.mVideoShareIntent.setType("video/*");
            }
            return this.mVideoShareIntent;
        } else if (str.startsWith("image/")) {
            if (this.mImageShareIntent == null) {
                this.mImageShareIntent = new Intent(str2);
                this.mImageShareIntent.setType("image/*");
            }
            return this.mImageShareIntent;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("unsupported mimeType ");
            sb.append(str);
            Log.w(TAG, sb.toString());
            return null;
        }
    }

    private void setPanoramaShareIntent(Uri uri) {
        if (this.mPanoramaShareIntent == null) {
            this.mPanoramaShareIntent = new Intent("android.intent.action.SEND");
        }
        this.mPanoramaShareIntent.setType("application/vnd.google.panorama360+jpg");
        this.mPanoramaShareIntent.putExtra("android.intent.extra.STREAM", uri);
        ShareActionProvider shareActionProvider = this.mPanoramaShareActionProvider;
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(this.mPanoramaShareIntent);
        }
    }

    public void onMenuVisibilityChanged(boolean z) {
        this.mMainHandler.removeMessages(1);
        if (!z) {
            this.mMainHandler.sendEmptyMessageDelayed(1, SHOW_ACTION_BAR_TIMEOUT_MS);
        }
    }

    public boolean onShareTargetSelected(ShareActionProvider shareActionProvider, Intent intent) {
        int currentId = this.mFilmStripView.getCurrentId();
        if (currentId < 0) {
            return false;
        }
        intent.getComponent().getPackageName();
        fileNameFromDataID(currentId);
        return true;
    }

    /* access modifiers changed from: private */
    public void updateActionBarMenu(int i) {
        LocalData localData = this.mDataAdapter.getLocalData(i);
        if (localData != null) {
            int localDataType = localData.getLocalDataType();
            if (this.mActionBarMenu != null) {
                char c = 703;
                boolean z = false;
                if (localDataType != 3) {
                    if (localDataType == 4) {
                        c = 133;
                    } else if (localDataType != 5) {
                        c = localDataType != 6 ? (char) 0 : 959;
                    }
                }
                if (isSecureCamera()) {
                    c &= 1;
                }
                setMenuItemVisible(this.mActionBarMenu, C0905R.C0907id.action_delete, (c & 1) != 0);
                char c2 = c & 2;
                setMenuItemVisible(this.mActionBarMenu, C0905R.C0907id.action_rotate_ccw, c2 != 0);
                setMenuItemVisible(this.mActionBarMenu, C0905R.C0907id.action_rotate_cw, c2 != 0);
                setMenuItemVisible(this.mActionBarMenu, C0905R.C0907id.action_details, (c & 4) != 0);
                setMenuItemVisible(this.mActionBarMenu, C0905R.C0907id.action_crop, (c & 8) != 0);
                setMenuItemVisible(this.mActionBarMenu, C0905R.C0907id.action_setas, (c & 16) != 0);
                setMenuItemVisible(this.mActionBarMenu, C0905R.C0907id.action_edit, (c & ' ') != 0);
                setMenuItemVisible(this.mActionBarMenu, C0905R.C0907id.action_trim, (c & '@') != 0);
                boolean z2 = (c & 128) != 0;
                boolean z3 = (c & 256) != 0;
                setMenuItemVisible(this.mActionBarMenu, C0905R.C0907id.action_share, z2);
                setMenuItemVisible(this.mActionBarMenu, C0905R.C0907id.action_share_panorama, z3);
                if (z3) {
                    MenuItem findItem = this.mActionBarMenu.findItem(C0905R.C0907id.action_share);
                    if (findItem != null) {
                        findItem.setShowAsAction(0);
                        findItem.setTitle(getResources().getString(C0905R.string.share_as_photo));
                    }
                    MenuItem findItem2 = this.mActionBarMenu.findItem(C0905R.C0907id.action_share_panorama);
                    if (findItem2 != null) {
                        findItem2.setShowAsAction(1);
                    }
                    setPanoramaShareIntent(localData.getContentUri());
                }
                if (z2) {
                    if (!z3) {
                        MenuItem findItem3 = this.mActionBarMenu.findItem(C0905R.C0907id.action_share);
                        if (findItem3 != null) {
                            findItem3.setShowAsAction(1);
                            findItem3.setTitle(getResources().getString(C0905R.string.share));
                        }
                    }
                    setStandardShareIntent(localData.getContentUri(), localData.getMimeType());
                    setNfcBeamPushUri(localData.getContentUri());
                }
                boolean z4 = localData.getLatLong() != null;
                Menu menu = this.mActionBarMenu;
                if (z4 && (c & 512) != 0) {
                    z = true;
                }
                setMenuItemVisible(menu, C0905R.C0907id.action_show_on_map, z);
            }
        }
    }

    private void setMenuItemVisible(Menu menu, int i, boolean z) {
        MenuItem findItem = menu.findItem(i);
        if (findItem != null) {
            findItem.setVisible(z);
        }
    }

    public MediaSaveService getMediaSaveService() {
        return this.mMediaSaveService;
    }

    public void notifyNewMedia(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String type = contentResolver.getType(uri);
        String str = TAG;
        if (type == null) {
            Log.e(str, "mimeType is NULL");
            return;
        }
        if (type.startsWith("video/")) {
            sendBroadcast(new Intent(CameraUtil.ACTION_NEW_VIDEO, uri));
            this.mDataAdapter.addNewVideo(contentResolver, uri);
        } else if (type.startsWith("image/")) {
            CameraUtil.broadcastNewPicture(this, uri);
            this.mDataAdapter.addNewPhoto(contentResolver, uri);
        } else if (type.startsWith("application/stitching-preview")) {
            this.mDataAdapter.addNewPhoto(contentResolver, uri);
        } else if (type.startsWith(PlaceholderManager.PLACEHOLDER_MIME_TYPE)) {
            this.mDataAdapter.addNewPhoto(contentResolver, uri);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Unknown new media with MIME type:");
            sb.append(type);
            sb.append(", uri:");
            sb.append(uri);
            Log.w(str, sb.toString());
        }
    }

    /* access modifiers changed from: private */
    public void removeData(int i) {
        this.mDataAdapter.removeData(this, i);
        if (this.mDataAdapter.getTotalNumber() > 1) {
            showUndoDeletionBar();
            return;
        }
        this.mPendingDeletion = true;
        performDeletion();
    }

    private void bindMediaSaveService() {
        bindService(new Intent(this, MediaSaveService.class), this.mConnection, 1);
    }

    private void unbindMediaSaveService() {
        ServiceConnection serviceConnection = this.mConnection;
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0905R.menu.operations, menu);
        this.mActionBarMenu = menu;
        this.mStandardShareActionProvider = (ShareActionProvider) menu.findItem(C0905R.C0907id.action_share).getActionProvider();
        this.mStandardShareActionProvider.setShareHistoryFileName("standard_share_history.xml");
        Intent intent = this.mStandardShareIntent;
        if (intent != null) {
            this.mStandardShareActionProvider.setShareIntent(intent);
        }
        this.mPanoramaShareActionProvider = (ShareActionProvider) menu.findItem(C0905R.C0907id.action_share_panorama).getActionProvider();
        this.mPanoramaShareActionProvider.setShareHistoryFileName("panorama_share_history.xml");
        Intent intent2 = this.mPanoramaShareIntent;
        if (intent2 != null) {
            this.mPanoramaShareActionProvider.setShareIntent(intent2);
        }
        this.mStandardShareActionProvider.setOnShareTargetSelectedListener(this);
        this.mPanoramaShareActionProvider.setOnShareTargetSelectedListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int currentId = this.mFilmStripView.getCurrentId();
        if (currentId < 0) {
            return false;
        }
        final LocalData localData = this.mDataAdapter.getLocalData(currentId);
        switch (menuItem.getItemId()) {
            case 16908332:
                try {
                    startActivity(IntentHelper.getGalleryIntent(this));
                    return true;
                } catch (ActivityNotFoundException unused) {
                    Log.w(TAG, "Failed to launch gallery activity, closing");
                    finish();
                    break;
                }
            case C0905R.C0907id.action_crop /*2131230722*/:
                fileNameFromDataID(currentId);
                Intent intent = new Intent(CropActivity.CROP_ACTION);
                intent.setClass(this, CropActivity.class);
                intent.setDataAndType(localData.getContentUri(), localData.getMimeType()).setFlags(1);
                startActivityForResult(intent, 142);
                return true;
            case C0905R.C0907id.action_delete /*2131230723*/:
                break;
            case C0905R.C0907id.action_details /*2131230724*/:
                new AsyncTask<Void, Void, MediaDetails>() {
                    /* access modifiers changed from: protected */
                    public MediaDetails doInBackground(Void... voidArr) {
                        return localData.getMediaDetails(CameraActivity.this);
                    }

                    /* access modifiers changed from: protected */
                    public void onPostExecute(MediaDetails mediaDetails) {
                        if (mediaDetails != null && !CameraActivity.this.mPaused) {
                            DetailsDialog.create(CameraActivity.this, mediaDetails).show();
                        }
                    }
                }.execute(new Void[0]);
                return true;
            case C0905R.C0907id.action_edit /*2131230726*/:
                fileNameFromDataID(currentId);
                launchEditor(localData);
                return true;
            case C0905R.C0907id.action_rotate_ccw /*2131230728*/:
                localData.rotate90Degrees(this, this.mDataAdapter, currentId, false);
                return true;
            case C0905R.C0907id.action_rotate_cw /*2131230729*/:
                localData.rotate90Degrees(this, this.mDataAdapter, currentId, true);
                return true;
            case C0905R.C0907id.action_setas /*2131230731*/:
                Intent flags = new Intent("android.intent.action.ATTACH_DATA").setDataAndType(localData.getContentUri(), localData.getMimeType()).setFlags(1);
                flags.putExtra("mimeType", flags.getType());
                startActivityForResult(Intent.createChooser(flags, getString(C0905R.string.set_as)), 142);
                return true;
            case C0905R.C0907id.action_show_on_map /*2131230734*/:
                double[] latLong = localData.getLatLong();
                if (latLong != null) {
                    CameraUtil.showOnMap(this, latLong);
                }
                return true;
            case C0905R.C0907id.action_trim /*2131230737*/:
                Intent intent2 = new Intent(ACTION_TRIM_VIDEO);
                LocalData localData2 = this.mDataAdapter.getLocalData(this.mFilmStripView.getCurrentId());
                intent2.setData(localData2.getContentUri());
                intent2.putExtra(MEDIA_ITEM_PATH, localData2.getPath());
                startActivityForResult(intent2, 142);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        fileNameFromDataID(currentId);
        removeData(currentId);
        return true;
    }

    public boolean isCaptureIntent() {
        if (!"android.media.action.VIDEO_CAPTURE".equals(getIntent().getAction())) {
            if (!"android.media.action.IMAGE_CAPTURE".equals(getIntent().getAction())) {
                if (!"android.media.action.IMAGE_CAPTURE_SECURE".equals(getIntent().getAction())) {
                    return false;
                }
            }
        }
        return true;
    }

    private void registerAutoTestReceiver() {
        registerReceiver(this.mAutoTestReceiver, new IntentFilter(AUTO_TEST_INTENT));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x018c, code lost:
        if (com.android.camera.util.GcamHelper.hasGcamCapture() == false) goto L_0x0190;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x018e, code lost:
        if (r2 < 0) goto L_0x0190;
     */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x01e4  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x028c  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x02a9  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0351  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onCreate(android.os.Bundle r14) {
        /*
            r13 = this;
            super.onCreate(r14)
            r14 = 1
            r13.mShouldUpdateThumbnailFromFile = r14
            android.content.Intent r0 = r13.getIntent()
            java.lang.String r1 = r0.getAction()
            java.lang.String r2 = "android.media.action.STILL_IMAGE_CAMERA_SECURE"
            boolean r3 = r2.equals(r1)
            java.lang.String r4 = "com.android.camera.CameraGestureActivity"
            java.lang.String r5 = "android.media.action.IMAGE_CAPTURE_SECURE"
            r6 = 0
            if (r3 != 0) goto L_0x0039
            boolean r3 = r5.equals(r1)
            if (r3 != 0) goto L_0x0039
            android.content.ComponentName r3 = r0.getComponent()
            java.lang.String r3 = r3.getClassName()
            boolean r3 = r3.equals(r4)
            if (r3 == 0) goto L_0x0030
            goto L_0x0039
        L_0x0030:
            java.lang.String r3 = "secure_camera"
            boolean r3 = r0.getBooleanExtra(r3, r6)
            r13.mSecureCamera = r3
            goto L_0x003b
        L_0x0039:
            r13.mSecureCamera = r14
        L_0x003b:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r7 = "onCreate -->action :"
            r3.append(r7)
            r3.append(r1)
            java.lang.String r1 = r3.toString()
            java.lang.String r3 = "CAM_Activity"
            android.util.Log.d(r3, r1)
            boolean r1 = r13.mSecureCamera
            if (r1 == 0) goto L_0x0094
            android.view.Window r1 = r13.getWindow()
            android.view.WindowManager$LayoutParams r7 = r1.getAttributes()
            int r8 = r7.flags
            r9 = 524288(0x80000, float:7.34684E-40)
            r8 = r8 | r9
            r7.flags = r8
            android.content.ComponentName r0 = r0.getComponent()
            java.lang.String r0 = r0.getClassName()
            boolean r0 = r0.equals(r4)
            if (r0 == 0) goto L_0x0091
            int r0 = r7.flags
            r4 = 2097152(0x200000, float:2.938736E-39)
            r0 = r0 | r4
            r7.flags = r0
            java.lang.String r0 = "power"
            java.lang.Object r0 = r13.getSystemService(r0)
            android.os.PowerManager r0 = (android.os.PowerManager) r0
            android.os.PowerManager$WakeLock r0 = r0.newWakeLock(r14, r3)
            r13.mWakeLock = r0
            android.os.PowerManager$WakeLock r0 = r13.mWakeLock
            r0.acquire()
            java.lang.String r0 = "acquire wake lock"
            android.util.Log.d(r3, r0)
        L_0x0091:
            r1.setAttributes(r7)
        L_0x0094:
            boolean r0 = r13.mSecureCamera
            if (r0 == 0) goto L_0x009f
            boolean r0 = r13.hasCriticalPermissions()
            if (r0 != 0) goto L_0x009f
            return
        L_0x009f:
            boolean r0 = r13.isStartRequsetPermission()
            if (r0 == 0) goto L_0x00ae
            java.lang.String r14 = "onCreate: Missing critical permissions."
            android.util.Log.v(r3, r14)
            r13.finish()
            return
        L_0x00ae:
            android.content.SharedPreferences r0 = android.preference.PreferenceManager.getDefaultSharedPreferences(r13)
            java.lang.String r1 = "camera_api_1_support"
            boolean r3 = r0.contains(r1)
            if (r3 != 0) goto L_0x00bf
            boolean r0 = r13.cameraAPICheck()
            goto L_0x00c3
        L_0x00bf:
            boolean r0 = r0.getBoolean(r1, r14)
        L_0x00c3:
            android.content.ContentResolver r7 = r13.getContentResolver()
            android.net.Uri r8 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            android.database.Cursor r1 = r7.query(r8, r9, r10, r11, r12)
            r13.mCursor = r1
            r13.getContentResolver()
            android.view.Window r1 = r13.getWindow()
            r3 = 8
            r1.requestFeature(r3)
            android.view.LayoutInflater r1 = r13.getLayoutInflater()
            r3 = 2131361795(0x7f0a0003, float:1.8343352E38)
            r4 = 0
            android.view.View r1 = r1.inflate(r3, r4, r6)
            r3 = 2131230768(0x7f080030, float:1.8077598E38)
            android.view.View r3 = r1.findViewById(r3)
            android.widget.FrameLayout r3 = (android.widget.FrameLayout) r3
            r13.mCameraRootFrame = r3
            r3 = 2131230767(0x7f08002f, float:1.8077596E38)
            android.view.View r3 = r1.findViewById(r3)
            r13.mCameraPhotoModuleRootView = r3
            r3 = 2131230773(0x7f080035, float:1.8077608E38)
            android.view.View r3 = r1.findViewById(r3)
            r13.mCameraVideoModuleRootView = r3
            r3 = 2131230766(0x7f08002e, float:1.8077594E38)
            android.view.View r3 = r1.findViewById(r3)
            r13.mCameraPanoModuleRootView = r3
            r3 = 2131230763(0x7f08002b, float:1.8077588E38)
            android.view.View r3 = r1.findViewById(r3)
            r13.mCameraCaptureModuleRootView = r3
            android.content.Intent r3 = r13.getIntent()
            java.lang.String r3 = r3.getAction()
            java.lang.String r7 = "android.media.action.VIDEO_CAMERA"
            boolean r3 = r7.equals(r3)
            if (r3 != 0) goto L_0x01a6
            android.content.Intent r3 = r13.getIntent()
            java.lang.String r3 = r3.getAction()
            java.lang.String r7 = "android.media.action.VIDEO_CAPTURE"
            boolean r3 = r7.equals(r3)
            if (r3 == 0) goto L_0x013c
            goto L_0x01a6
        L_0x013c:
            android.content.Intent r3 = r13.getIntent()
            java.lang.String r3 = r3.getAction()
            java.lang.String r7 = "android.media.action.STILL_IMAGE_CAMERA"
            boolean r3 = r7.equals(r3)
            r7 = -1
            java.lang.String r8 = "camera.startup_module"
            r9 = 4
            if (r3 != 0) goto L_0x0192
            android.content.Intent r3 = r13.getIntent()
            java.lang.String r3 = r3.getAction()
            boolean r2 = r2.equals(r3)
            if (r2 == 0) goto L_0x015f
            goto L_0x0192
        L_0x015f:
            android.content.Intent r2 = r13.getIntent()
            java.lang.String r2 = r2.getAction()
            java.lang.String r3 = "android.media.action.IMAGE_CAPTURE"
            boolean r2 = r3.equals(r2)
            if (r2 != 0) goto L_0x0190
            android.content.Intent r2 = r13.getIntent()
            java.lang.String r2 = r2.getAction()
            boolean r2 = r5.equals(r2)
            if (r2 == 0) goto L_0x017e
            goto L_0x0190
        L_0x017e:
            android.content.SharedPreferences r2 = android.preference.PreferenceManager.getDefaultSharedPreferences(r13)
            int r2 = r2.getInt(r8, r7)
            if (r2 != r9) goto L_0x018e
            boolean r3 = com.android.camera.util.GcamHelper.hasGcamCapture()
            if (r3 == 0) goto L_0x0190
        L_0x018e:
            if (r2 >= 0) goto L_0x01a7
        L_0x0190:
            r2 = r6
            goto L_0x01a7
        L_0x0192:
            android.content.SharedPreferences r2 = android.preference.PreferenceManager.getDefaultSharedPreferences(r13)
            int r2 = r2.getInt(r8, r7)
            if (r2 != r9) goto L_0x01a3
            boolean r2 = com.android.camera.util.GcamHelper.hasGcamCapture()
            if (r2 == 0) goto L_0x01a3
            goto L_0x01a4
        L_0x01a3:
            r9 = r6
        L_0x01a4:
            r2 = r9
            goto L_0x01a7
        L_0x01a6:
            r2 = r14
        L_0x01a7:
            boolean r3 = com.android.camera.util.PersistUtil.getCamera2Mode()
            if (r3 != 0) goto L_0x01b0
            if (r0 != 0) goto L_0x01b0
            r3 = r14
        L_0x01b0:
            com.android.camera.CameraHolder.setCamera2Mode(r13, r3)
            if (r3 == 0) goto L_0x01ba
            if (r2 == 0) goto L_0x01b9
            if (r2 != r14) goto L_0x01ba
        L_0x01b9:
            r2 = 5
        L_0x01ba:
            com.android.camera.CameraActivity$MyOrientationEventListener r0 = new com.android.camera.CameraActivity$MyOrientationEventListener
            r0.<init>(r13)
            r13.mOrientationListener = r0
            r0 = 2131361797(0x7f0a0005, float:1.8343356E38)
            r13.setContentView(r0)
            r0 = 2131230818(0x7f080062, float:1.80777E38)
            android.view.View r0 = r13.findViewById(r0)
            com.android.camera.ui.FilmStripView r0 = (com.android.camera.p004ui.FilmStripView) r0
            r13.mFilmStripView = r0
            r13.setModuleFromIndex(r2)
            android.app.ActionBar r0 = r13.getActionBar()
            r13.mActionBar = r0
            android.app.ActionBar r0 = r13.mActionBar
            r0.addOnMenuVisibilityListener(r13)
            boolean r0 = com.android.camera.util.ApiHelper.HAS_ROTATION_ANIMATION
            if (r0 == 0) goto L_0x01e7
            r13.setRotationAnimation()
        L_0x01e7:
            com.android.camera.CameraActivity$MainHandler r0 = new com.android.camera.CameraActivity$MainHandler
            android.os.Looper r2 = r13.getMainLooper()
            r0.<init>(r2)
            r13.mMainHandler = r0
            r0 = 2131230762(0x7f08002a, float:1.8077586E38)
            android.view.View r0 = r13.findViewById(r0)
            android.widget.FrameLayout r0 = (android.widget.FrameLayout) r0
            r13.mAboveFilmstripControlLayout = r0
            android.widget.FrameLayout r0 = r13.mAboveFilmstripControlLayout
            r0.setFitsSystemWindows(r14)
            com.android.camera.app.AppManagerFactory r0 = com.android.camera.app.AppManagerFactory.getInstance(r13)
            com.android.camera.app.PanoramaStitchingManager r0 = r0.getPanoramaStitchingManager()
            r13.mPanoramaManager = r0
            com.android.camera.app.AppManagerFactory r0 = com.android.camera.app.AppManagerFactory.getInstance(r13)
            com.android.camera.app.PlaceholderManager r0 = r0.getGcamProcessingManager()
            r13.mPlaceholderManager = r0
            com.android.camera.app.PanoramaStitchingManager r0 = r13.mPanoramaManager
            com.android.camera.ImageTaskManager$TaskListener r2 = r13.mStitchingListener
            r0.addTaskListener(r2)
            com.android.camera.app.PlaceholderManager r0 = r13.mPlaceholderManager
            com.android.camera.ImageTaskManager$TaskListener r2 = r13.mPlaceholderListener
            r0.addTaskListener(r2)
            r0 = 2131230927(0x7f0800cf, float:1.807792E38)
            android.view.View r0 = r13.findViewById(r0)
            r13.mPanoStitchingPanel = r0
            r0 = 2131230926(0x7f0800ce, float:1.8077919E38)
            android.view.View r0 = r13.findViewById(r0)
            android.widget.ProgressBar r0 = (android.widget.ProgressBar) r0
            r13.mBottomProgress = r0
            com.android.camera.data.CameraPreviewData r0 = new com.android.camera.data.CameraPreviewData
            r2 = -2
            r0.<init>(r1, r2, r2)
            r13.mCameraPreviewData = r0
            com.android.camera.data.FixedFirstDataAdapter r0 = new com.android.camera.data.FixedFirstDataAdapter
            com.android.camera.data.CameraDataAdapter r1 = new com.android.camera.data.CameraDataAdapter
            android.graphics.drawable.ColorDrawable r2 = new android.graphics.drawable.ColorDrawable
            android.content.res.Resources r3 = r13.getResources()
            r5 = 2131034170(0x7f05003a, float:1.767885E38)
            int r3 = r3.getColor(r5)
            r2.<init>(r3)
            r1.<init>(r2)
            com.android.camera.data.CameraPreviewData r2 = r13.mCameraPreviewData
            r0.<init>(r1, r2)
            r13.mWrappedDataAdapter = r0
            com.android.camera.ui.FilmStripView r0 = r13.mFilmStripView
            android.content.res.Resources r1 = r13.getResources()
            r2 = 2131099677(0x7f06001d, float:1.7811714E38)
            int r1 = r1.getDimensionPixelSize(r2)
            r0.setViewGap(r1)
            com.android.camera.util.PhotoSphereHelper$PanoramaViewHelper r0 = new com.android.camera.util.PhotoSphereHelper$PanoramaViewHelper
            r0.<init>(r13)
            r13.mPanoramaViewHelper = r0
            com.android.camera.util.PhotoSphereHelper$PanoramaViewHelper r0 = r13.mPanoramaViewHelper
            r0.onCreate()
            com.android.camera.ui.FilmStripView r0 = r13.mFilmStripView
            com.android.camera.util.PhotoSphereHelper$PanoramaViewHelper r1 = r13.mPanoramaViewHelper
            r0.setPanoramaViewHelper(r1)
            com.android.camera.ui.FilmStripView r0 = r13.mFilmStripView
            com.android.camera.ui.FilmStripView$Listener r1 = r13.mFilmStripListener
            r0.setListener(r1)
            boolean r0 = r13.mSecureCamera
            if (r0 != 0) goto L_0x02a9
            com.android.camera.data.LocalDataAdapter r0 = r13.mWrappedDataAdapter
            r13.mDataAdapter = r0
            com.android.camera.ui.FilmStripView r0 = r13.mFilmStripView
            com.android.camera.data.LocalDataAdapter r1 = r13.mDataAdapter
            r0.setDataAdapter(r1)
            boolean r0 = r13.isCaptureIntent()
            if (r0 != 0) goto L_0x02ec
            com.android.camera.data.LocalDataAdapter r0 = r13.mDataAdapter
            android.content.ContentResolver r1 = r13.getContentResolver()
            r0.requestLoad(r1)
            r13.mDataRequested = r14
            goto L_0x02ec
        L_0x02a9:
            android.view.LayoutInflater r0 = r13.getLayoutInflater()
            r1 = 2131361862(0x7f0a0046, float:1.8343488E38)
            android.view.View r0 = r0.inflate(r1, r4)
            r8 = r0
            android.widget.ImageView r8 = (android.widget.ImageView) r8
            com.android.camera.CameraActivity$10 r0 = new com.android.camera.CameraActivity$10
            r0.<init>()
            r8.setOnClickListener(r0)
            com.android.camera.data.FixedLastDataAdapter r0 = new com.android.camera.data.FixedLastDataAdapter
            com.android.camera.data.LocalDataAdapter r1 = r13.mWrappedDataAdapter
            com.android.camera.data.SimpleViewData r2 = new com.android.camera.data.SimpleViewData
            android.graphics.drawable.Drawable r3 = r8.getDrawable()
            int r9 = r3.getIntrinsicWidth()
            android.graphics.drawable.Drawable r3 = r8.getDrawable()
            int r10 = r3.getIntrinsicHeight()
            r11 = 0
            r12 = 0
            r7 = r2
            r7.<init>(r8, r9, r10, r11, r12)
            r0.<init>(r1, r2)
            r13.mDataAdapter = r0
            com.android.camera.data.LocalDataAdapter r0 = r13.mDataAdapter
            r0.flush()
            com.android.camera.ui.FilmStripView r0 = r13.mFilmStripView
            com.android.camera.data.LocalDataAdapter r1 = r13.mDataAdapter
            r0.setDataAdapter(r1)
        L_0x02ec:
            r13.setupNfcBeamPush()
            com.android.camera.data.LocalMediaObserver r0 = new com.android.camera.data.LocalMediaObserver
            r0.<init>()
            r13.mLocalImagesObserver = r0
            com.android.camera.data.LocalMediaObserver r0 = new com.android.camera.data.LocalMediaObserver
            r0.<init>()
            r13.mLocalVideosObserver = r0
            android.content.ContentResolver r0 = r13.getContentResolver()
            android.net.Uri r1 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            com.android.camera.data.LocalMediaObserver r2 = r13.mLocalImagesObserver
            r0.registerContentObserver(r1, r14, r2)
            android.content.ContentResolver r0 = r13.getContentResolver()
            android.net.Uri r1 = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            com.android.camera.data.LocalMediaObserver r2 = r13.mLocalVideosObserver
            r0.registerContentObserver(r1, r14, r2)
            android.content.SharedPreferences r14 = android.preference.PreferenceManager.getDefaultSharedPreferences(r13)
            java.lang.String r0 = "pref_developer_menu_key"
            boolean r14 = r14.getBoolean(r0, r6)
            r13.mDeveloperMenuEnabled = r14
            android.view.WindowManager r14 = r13.getWindowManager()
            android.view.Display r14 = r14.getDefaultDisplay()
            android.graphics.Point r0 = new android.graphics.Point
            r0.<init>()
            r14.getSize(r0)
            int r14 = r0.x
            int r0 = r0.y
            int r14 = java.lang.Math.min(r14, r0)
            int r0 = r14 * 7
            int r0 = r0 / 100
            int r14 = r14 / 2
            int r1 = r14 + r0
            SETTING_LIST_WIDTH_1 = r1
            int r14 = r14 - r0
            SETTING_LIST_WIDTH_2 = r14
            r13.registerSDcardMountedReceiver()
            boolean r14 = com.android.camera.util.PersistUtil.isAutoTestEnabled()
            r13.mAutoTestEnabled = r14
            boolean r14 = r13.mAutoTestEnabled
            if (r14 == 0) goto L_0x0354
            r13.registerAutoTestReceiver()
        L_0x0354:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CameraActivity.onCreate(android.os.Bundle):void");
    }

    private void setRotationAnimation() {
        Window window = getWindow();
        LayoutParams attributes = window.getAttributes();
        attributes.rotationAnimation = 1;
        window.setAttributes(attributes);
    }

    public void onUserInteraction() {
        super.onUserInteraction();
        CameraModule cameraModule = this.mCurrentModule;
        if (cameraModule != null) {
            cameraModule.onUserInteraction();
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        boolean sendToModeView = this.mFilmStripView.checkSendToModeView(motionEvent) ? this.mFilmStripView.sendToModeView(motionEvent) : false;
        if (!sendToModeView) {
            sendToModeView = super.dispatchTouchEvent(motionEvent);
        }
        if (motionEvent.getActionMasked() == 0 && this.mPendingDeletion && !this.mIsUndoingDeletion) {
            performDeletion();
        }
        return sendToModeView;
    }

    public void onPause() {
        Log.d(TAG, "onPause");
        if (!this.mSecureCamera || hasCriticalPermissions()) {
            performDeletion();
            this.mOrientationListener.disable();
            this.mCurrentModule.onPauseBeforeSuper();
            super.onPause();
            this.mCurrentModule.onPauseAfterSuper();
            this.mPaused = true;
            this.mLocalImagesObserver.setActivityPaused(true);
            this.mLocalVideosObserver.setActivityPaused(true);
            return;
        }
        super.onPause();
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 142) {
            this.mResetToPreviewOnResume = false;
            this.mIsEditActivityInProgress = false;
        } else if (i == 1) {
            if (i2 == -1) {
                this.mCaptureModule.setRefocusLastTaken(false);
            }
        } else if (i != BestpictureActivity.BESTPICTURE_ACTIVITY_CODE) {
            super.onActivityResult(i, i2, intent);
        } else if (i2 == -1) {
            byte[] byteArrayExtra = intent.getByteArrayExtra("thumbnail");
            if (byteArrayExtra != null) {
                updateThumbnail(byteArrayExtra);
            }
        }
    }

    public void onWindowFocusChanged(boolean z) {
        if (z) {
            setSystemBarsVisibility(false);
        }
    }

    private boolean hasCriticalPermissions() {
        return checkSelfPermission("android.permission.CAMERA") == 0 && checkSelfPermission("android.permission.RECORD_AUDIO") == 0 && checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0 && checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0;
    }

    private boolean isStartRequsetPermission() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String str = CameraSettings.KEY_REQUEST_PERMISSION;
        boolean z = defaultSharedPreferences.getBoolean(str, false);
        if (this.mSecureCamera) {
            return false;
        }
        if (z && hasCriticalPermissions()) {
            return false;
        }
        Log.v(TAG, "Start Request Permission");
        Intent intent = new Intent(this, PermissionsActivity.class);
        intent.setFlags(67108864);
        startActivity(intent);
        Editor edit = defaultSharedPreferences.edit();
        edit.putBoolean(str, true);
        edit.apply();
        return true;
    }

    public void onResume() {
        String str = TAG;
        Log.d(str, "onResume");
        if (this.mSecureCamera && !hasCriticalPermissions()) {
            super.onResume();
            showOpenCameraErrorDialog();
        } else if (isStartRequsetPermission()) {
            super.onResume();
            Log.v(str, "onResume: Missing critical permissions.");
            finish();
        } else {
            this.settingsManager = SettingsManager.getInstance();
            if (this.settingsManager == null) {
                SettingsManager.createInstance(this);
            }
            setSystemBarsVisibility(false);
            getClass().getSimpleName();
            this.mOrientationListener.enable();
            this.mCurrentModule.onResumeBeforeSuper();
            super.onResume();
            this.mPaused = false;
            this.mCurrentModule.onResumeAfterSuper();
            setSwipingEnabled(true);
            if (this.mResetToPreviewOnResume) {
                this.mFilmStripView.getController().goToFirstItem();
            }
            this.mResetToPreviewOnResume = true;
            if ((this.mLocalVideosObserver.isMediaDataChangedDuringPause() || this.mLocalImagesObserver.isMediaDataChangedDuringPause()) && !this.mSecureCamera) {
                this.mDataAdapter.requestLoad(getContentResolver());
                this.mThumbnailDrawable = null;
            }
            this.mLocalImagesObserver.setActivityPaused(false);
            this.mLocalVideosObserver.setActivityPaused(false);
            Log.d(str, "send the turn off Flashlight broadcast");
            Intent intent = new Intent("org.codeaurora.snapcam.action.CLOSE_FLASHLIGHT");
            intent.putExtra("camera_led", true);
            sendBroadcast(intent);
            if (this.mSecureCamera && this.mGotoGallery) {
                for (int totalNumber = this.mDataAdapter.getTotalNumber() - 1; totalNumber >= 0; totalNumber--) {
                    fileNameFromDataID(totalNumber);
                    String path = this.mDataAdapter.getLocalData(totalNumber).getPath();
                    if (path != null && !new File(path).exists()) {
                        this.mDataAdapter.removeData(this, totalNumber);
                    }
                }
                this.mFilmStripView.setDataAdapter(this.mDataAdapter);
                this.mGotoGallery = false;
            }
        }
    }

    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (!this.mSecureCamera || hasCriticalPermissions()) {
            bindMediaSaveService();
            this.mPanoramaViewHelper.onStart();
        }
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        String str = TAG;
        Log.d(str, "onStop");
        this.mShouldUpdateThumbnailFromFile = true;
        if (!this.mSecureCamera || hasCriticalPermissions()) {
            if (shouldFinishActivityWhenOnStop()) {
                Log.d(str, "onstop---> finishing");
                finish();
            }
            this.mPanoramaViewHelper.onStop();
            unbindMediaSaveService();
        }
    }

    public void onDestroy() {
        String str = TAG;
        Log.d(str, "onDestroy");
        WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null && wakeLock.isHeld()) {
            this.mWakeLock.release();
            Log.d(str, "wake lock release");
        }
        if (this.mCursor != null) {
            getContentResolver().unregisterContentObserver(this.mLocalImagesObserver);
            getContentResolver().unregisterContentObserver(this.mLocalVideosObserver);
            unregisterReceiver(this.mSDcardMountedReceiver);
            this.mCursor.close();
            this.mCursor = null;
        }
        if (this.mAutoTestEnabled) {
            unregisterReceiver(this.mAutoTestReceiver);
        }
        CameraModule cameraModule = this.mCurrentModule;
        if (cameraModule != null) {
            cameraModule.onDestroy();
        }
        super.onDestroy();
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mCurrentModule.onConfigurationChanged(configuration);
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (!this.mFilmStripView.inCameraFullscreen() || (!this.mCurrentModule.onKeyDown(i, keyEvent) && i != 84 && i != 82)) {
            return super.onKeyDown(i, keyEvent);
        }
        return true;
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (!this.mFilmStripView.inCameraFullscreen() || !this.mCurrentModule.onKeyUp(i, keyEvent)) {
            return super.onKeyUp(i, keyEvent);
        }
        return true;
    }

    public void onBackPressed() {
        if (!this.mFilmStripView.inCameraFullscreen()) {
            this.mFilmStripView.getController().goToFirstItem();
            this.mCurrentModule.resizeForPreviewAspectRatio();
        } else if (!this.mCurrentModule.onBackPressed()) {
            super.onBackPressed();
        }
    }

    public void setPreviewGestures(PreviewGestures previewGestures) {
        this.mFilmStripView.setPreviewGestures(previewGestures);
    }

    /* access modifiers changed from: protected */
    public long updateStorageSpace() {
        long j;
        synchronized (this.mStorageSpaceLock) {
            this.mStorageSpaceBytes = Storage.getAvailableSpace();
            if (this.mCaptureModule.isRecordingVideo() && this.mStorageSpaceBytes <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
                Log.d(TAG, "isRecordingVideo send stop message");
                this.mMainHandler.sendEmptyMessage(3);
            }
            if (Storage.switchSavePath()) {
                this.mStorageSpaceBytes = Storage.getAvailableSpace();
                this.mMainHandler.sendEmptyMessage(2);
            }
            j = this.mStorageSpaceBytes;
        }
        return j;
    }

    /* access modifiers changed from: protected */
    public long getStorageSpaceBytes() {
        long j;
        synchronized (this.mStorageSpaceLock) {
            j = this.mStorageSpaceBytes;
        }
        return j;
    }

    /* access modifiers changed from: protected */
    public void updateStorageSpaceAndHint() {
        updateStorageSpace();
        updateStorageHint(this.mStorageSpaceBytes);
    }

    /* access modifiers changed from: protected */
    public void updateStorageSpaceAndHint(final OnStorageUpdateDoneListener onStorageUpdateDoneListener) {
        new AsyncTask<Void, Void, Long>() {
            /* access modifiers changed from: protected */
            public Long doInBackground(Void... voidArr) {
                return Long.valueOf(CameraActivity.this.updateStorageSpace());
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Long l) {
                CameraActivity.this.updateStorageHint(l.longValue());
                if (onStorageUpdateDoneListener == null || CameraActivity.this.mPaused) {
                    Log.v(CameraActivity.TAG, "ignoring storage callback after activity pause");
                } else {
                    onStorageUpdateDoneListener.onStorageUpdateDone(l.longValue());
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    /* access modifiers changed from: protected */
    public void updateStorageHint(long j) {
        StringBuilder sb = new StringBuilder();
        sb.append(" checkstorageSpace : ");
        sb.append(j);
        Log.d(TAG, sb.toString());
        CharSequence charSequence = j == -1 ? getString(C0905R.string.no_storage) : j == -2 ? getString(C0905R.string.preparing_sd) : j == -3 ? getString(C0905R.string.access_sd_fail) : j <= Storage.LOW_STORAGE_THRESHOLD_BYTES ? (!Storage.isSaveSDCard() || !SDCard.instance().isWriteable()) ? getString(C0905R.string.internal_spaceIsLow_content) : getString(C0905R.string.spaceIsLow_content) : null;
        if (isFinishing()) {
            return;
        }
        if (charSequence != null) {
            OnScreenHint onScreenHint = this.mStorageHint;
            if (onScreenHint == null) {
                this.mStorageHint = OnScreenHint.makeText(this, charSequence);
            } else {
                onScreenHint.setText(charSequence);
            }
            this.mStorageHint.show();
            return;
        }
        OnScreenHint onScreenHint2 = this.mStorageHint;
        if (onScreenHint2 != null) {
            onScreenHint2.cancel();
            this.mStorageHint = null;
        }
    }

    /* access modifiers changed from: protected */
    public void setResultEx(int i) {
        this.mResultCodeForTesting = i;
        setResult(i);
    }

    /* access modifiers changed from: protected */
    public void setResultEx(int i, Intent intent) {
        this.mResultCodeForTesting = i;
        this.mResultDataForTesting = intent;
        setResult(i, intent);
    }

    public int getResultCode() {
        return this.mResultCodeForTesting;
    }

    public Intent getResultData() {
        return this.mResultDataForTesting;
    }

    public boolean isSecureCamera() {
        return this.mSecureCamera;
    }

    public void requestLocationPermission() {
        String str = "android.permission.ACCESS_FINE_LOCATION";
        if (checkSelfPermission(str) != 0) {
            Log.v(TAG, "Request Location permission");
            this.mCurrentModule.waitingLocationPermissionResult(true);
            requestPermissions(new String[]{str}, 1);
        }
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 1) {
            this.mCurrentModule.waitingLocationPermissionResult(false);
            int length = iArr.length;
            String str = TAG;
            if (length <= 0 || iArr[0] != 0) {
                Log.w(str, "Location permission is denied");
                this.mCurrentModule.enableRecordingLocation(false);
                return;
            }
            Log.v(str, "Location permission is granted");
            this.mCurrentModule.enableRecordingLocation(true);
        }
    }

    public boolean isForceReleaseCamera() {
        return this.mForceReleaseCamera;
    }

    public void onModuleSelected(int i) {
        this.mForceReleaseCamera = i == 5 || (PersistUtil.getCamera2Mode() && i == 0);
        if (this.mForceReleaseCamera) {
            i = 5;
        }
        int i2 = this.mCurrentModuleIndex;
        if (i2 != i || i2 == 5) {
            CameraHolder.instance().keep();
            closeModule(this.mCurrentModule);
            setModuleFromIndex(i);
            openModule(this.mCurrentModule);
            this.mForceReleaseCamera = false;
            this.mCurrentModule.onOrientationChanged(this.mLastRawOrientation);
            MediaSaveService mediaSaveService = this.mMediaSaveService;
            if (mediaSaveService != null) {
                this.mCurrentModule.onMediaSaveServiceConnected(mediaSaveService);
            }
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(CameraSettings.KEY_STARTUP_MODULE_INDEX, i).apply();
        }
    }

    private void setModuleFromIndex(int i) {
        this.mCameraPhotoModuleRootView.setVisibility(8);
        this.mCameraVideoModuleRootView.setVisibility(8);
        this.mCameraPanoModuleRootView.setVisibility(8);
        this.mCameraCaptureModuleRootView.setVisibility(8);
        this.mCurrentModuleIndex = i;
        if (i == 0) {
            PhotoModule photoModule = this.mPhotoModule;
            if (photoModule == null) {
                this.mPhotoModule = new PhotoModule();
                this.mPhotoModule.init(this, this.mCameraPhotoModuleRootView);
            } else {
                photoModule.reinit();
            }
            this.mCurrentModule = this.mPhotoModule;
            this.mCameraPhotoModuleRootView.setVisibility(0);
        } else if (i == 1) {
            VideoModule videoModule = this.mVideoModule;
            if (videoModule == null) {
                this.mVideoModule = new VideoModule();
                this.mVideoModule.init(this, this.mCameraVideoModuleRootView);
            } else {
                videoModule.reinit();
            }
            this.mCurrentModule = this.mVideoModule;
            this.mCameraVideoModuleRootView.setVisibility(0);
        } else if (i == 2) {
            if (this.mPanoModule == null) {
                this.mPanoModule = new WideAnglePanoramaModule();
                this.mPanoModule.init(this, this.mCameraPanoModuleRootView);
            }
            this.mCurrentModule = this.mPanoModule;
            this.mCameraPanoModuleRootView.setVisibility(0);
        } else if (i != 5) {
            if (i == 6) {
                if (!PanoCaptureProcessView.isSupportedStatic()) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            RotateTextToast.makeText(this, (CharSequence) "Panocapture library is missing", 0).show();
                        }
                    });
                    this.mCurrentModuleIndex = 0;
                } else {
                    if (this.mPano2Module == null) {
                        this.mPano2Module = new PanoCaptureModule();
                        this.mPano2Module.init(this, this.mCameraPanoModuleRootView);
                    }
                    this.mCurrentModule = this.mPano2Module;
                    this.mCameraPanoModuleRootView.setVisibility(0);
                    return;
                }
            }
            PhotoModule photoModule2 = this.mPhotoModule;
            if (photoModule2 == null) {
                this.mPhotoModule = new PhotoModule();
                this.mPhotoModule.init(this, this.mCameraPhotoModuleRootView);
            } else {
                photoModule2.reinit();
            }
            this.mCurrentModule = this.mPhotoModule;
            this.mCameraPhotoModuleRootView.setVisibility(0);
        } else {
            CaptureModule captureModule = this.mCaptureModule;
            if (captureModule == null) {
                this.mCaptureModule = new CaptureModule();
                this.mCaptureModule.init(this, this.mCameraCaptureModuleRootView);
            } else {
                captureModule.reinit();
            }
            this.mCurrentModule = this.mCaptureModule;
            this.mCameraCaptureModuleRootView.setVisibility(0);
        }
    }

    public void launchEditor(LocalData localData) {
        if (!this.mIsEditActivityInProgress) {
            Intent flags = new Intent("android.intent.action.EDIT").setDataAndType(localData.getContentUri(), localData.getMimeType()).setFlags(1);
            try {
                startActivityForResult(flags, 142);
            } catch (ActivityNotFoundException unused) {
                startActivityForResult(Intent.createChooser(flags, null), 142);
            }
            this.mIsEditActivityInProgress = true;
        }
    }

    public void launchTinyPlanetEditor(LocalData localData) {
        TinyPlanetFragment tinyPlanetFragment = new TinyPlanetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TinyPlanetFragment.ARGUMENT_URI, localData.getContentUri().toString());
        bundle.putString(TinyPlanetFragment.ARGUMENT_TITLE, localData.getTitle());
        tinyPlanetFragment.setArguments(bundle);
        tinyPlanetFragment.show(getFragmentManager(), "tiny_planet");
    }

    private void openModule(CameraModule cameraModule) {
        cameraModule.onResumeBeforeSuper();
        cameraModule.onResumeAfterSuper();
    }

    private void closeModule(CameraModule cameraModule) {
        cameraModule.onPauseBeforeSuper();
        cameraModule.onPauseAfterSuper();
    }

    /* access modifiers changed from: private */
    public void performDeletion() {
        if (this.mPendingDeletion) {
            hideUndoDeletionBar(false);
            this.mDataAdapter.executeDeletion(this);
            int currentId = this.mFilmStripView.getCurrentId();
            updateActionBarMenu(currentId);
            this.mFilmStripListener.onCurrentDataCentered(currentId);
        }
    }

    public void showUndoDeletionBar() {
        if (this.mPendingDeletion) {
            performDeletion();
        }
        Log.v(TAG, "showing undo bar");
        this.mPendingDeletion = true;
        if (this.mUndoDeletionBar == null) {
            this.mUndoDeletionBar = (ViewGroup) ((ViewGroup) getLayoutInflater().inflate(C0905R.layout.undo_bar, this.mAboveFilmstripControlLayout, true)).findViewById(C0905R.C0907id.camera_undo_deletion_bar);
            View findViewById = this.mUndoDeletionBar.findViewById(C0905R.C0907id.camera_undo_deletion_button);
            findViewById.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    CameraActivity.this.mDataAdapter.undoDataRemoval();
                    CameraActivity.this.hideUndoDeletionBar(true);
                }
            });
            this.mUndoDeletionBar.setClickable(true);
            findViewById.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getActionMasked() == 0) {
                        CameraActivity.this.mIsUndoingDeletion = true;
                    } else if (motionEvent.getActionMasked() == 1) {
                        CameraActivity.this.mIsUndoingDeletion = false;
                    }
                    return false;
                }
            });
        }
        this.mUndoDeletionBar.setAlpha(0.0f);
        this.mUndoDeletionBar.setVisibility(0);
        this.mUndoDeletionBar.animate().setDuration(200).alpha(1.0f).setListener(null).start();
    }

    /* access modifiers changed from: private */
    public void hideUndoDeletionBar(boolean z) {
        Log.v(TAG, "Hiding undo deletion bar");
        this.mPendingDeletion = false;
        ViewGroup viewGroup = this.mUndoDeletionBar;
        if (viewGroup == null) {
            return;
        }
        if (z) {
            viewGroup.animate().setDuration(200).alpha(0.0f).setListener(new AnimatorListener() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    CameraActivity.this.mUndoDeletionBar.setVisibility(8);
                }
            }).start();
        } else {
            viewGroup.setVisibility(8);
        }
    }

    public void onShowSwitcherPopup() {
        this.mCurrentModule.onShowSwitcherPopup();
    }

    public void setSwipingEnabled(boolean z) {
        if (isCaptureIntent()) {
            this.mCameraPreviewData.lockPreview(true);
        } else {
            this.mCameraPreviewData.lockPreview(!z);
        }
    }

    /* access modifiers changed from: private */
    public boolean arePreviewControlsVisible() {
        return this.mCurrentModule.arePreviewControlsVisible();
    }

    /* access modifiers changed from: private */
    public void setPreviewControlsVisibility(boolean z) {
        this.mCurrentModule.onPreviewFocusChanged(z);
    }

    public long getAutoFocusTime() {
        CameraModule cameraModule = this.mCurrentModule;
        if (cameraModule instanceof PhotoModule) {
            return ((PhotoModule) cameraModule).mAutoFocusTime;
        }
        return -1;
    }

    public long getShutterLag() {
        CameraModule cameraModule = this.mCurrentModule;
        if (cameraModule instanceof PhotoModule) {
            return ((PhotoModule) cameraModule).mShutterLag;
        }
        return -1;
    }

    public long getShutterToPictureDisplayedTime() {
        CameraModule cameraModule = this.mCurrentModule;
        if (cameraModule instanceof PhotoModule) {
            return ((PhotoModule) cameraModule).mShutterToPictureDisplayedTime;
        }
        return -1;
    }

    public long getPictureDisplayedToJpegCallbackTime() {
        CameraModule cameraModule = this.mCurrentModule;
        if (cameraModule instanceof PhotoModule) {
            return ((PhotoModule) cameraModule).mPictureDisplayedToJpegCallbackTime;
        }
        return -1;
    }

    public long getJpegCallbackFinishTime() {
        CameraModule cameraModule = this.mCurrentModule;
        if (cameraModule instanceof PhotoModule) {
            return ((PhotoModule) cameraModule).mJpegCallbackFinishTime;
        }
        return -1;
    }

    public long getCaptureStartTime() {
        CameraModule cameraModule = this.mCurrentModule;
        if (cameraModule instanceof PhotoModule) {
            return ((PhotoModule) cameraModule).mCaptureStartTime;
        }
        return -1;
    }

    public boolean isRecording() {
        CameraModule cameraModule = this.mCurrentModule;
        if (cameraModule instanceof VideoModule) {
            return ((VideoModule) cameraModule).isRecording();
        }
        return false;
    }

    public CameraOpenErrorCallback getCameraOpenErrorCallback() {
        return this.mCameraOpenErrorCallback;
    }

    public CameraModule getCurrentModule() {
        return this.mCurrentModule;
    }

    /* access modifiers changed from: private */
    public void showOpenCameraErrorDialog() {
        if (!hasCriticalPermissions()) {
            CameraUtil.showErrorAndFinish(this, C0905R.string.error_permissions);
        } else {
            CameraUtil.showErrorAndFinish(this, C0905R.string.cannot_connect_camera);
        }
    }

    public void updateShutterButtonStatus(boolean z) {
        this.mCaptureModule.updateShutterButtonStatus(z);
    }

    private static String getTopActivityName(Context context) {
        List runningTasks = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
        if (runningTasks.size() > 0) {
            return ((RunningTaskInfo) runningTasks.get(0)).topActivity.getShortClassName();
        }
        return null;
    }

    private boolean shouldFinishActivityWhenOnStop() {
        if (isSecureCamera()) {
            boolean equals = SETTINGS_ACTIVITY.equals(getTopActivityName(this));
            String str = TAG;
            if (!equals) {
                if (!SCENEMODE_ACTIVITY.equals(getTopActivityName(this)) && this.isCameraOpenAndWorking && !this.mGotoGallery) {
                    Log.d(str, "finish cameraActivity if in secureCamera when onStop");
                    return true;
                }
            }
            Log.d(str, "should not finish when in settings||SceneMode||isCameraOpenAndWorking");
        }
        return false;
    }

    public void setCameraOpenAndWorking(boolean z) {
        this.isCameraOpenAndWorking = z;
    }
}
