package com.android.camera;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera.Face;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.Allocation.OnBufferAvailableListener;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.p000v4.view.GravityCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.camera.CameraManager.CameraFaceDetectionCallback;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.FocusOverlayManager.FocusUI;
import com.android.camera.PauseButton.OnPauseButtonListener;
import com.android.camera.PreviewGestures.SingleTapListener;
import com.android.camera.SettingsManager.Listener;
import com.android.camera.imageprocessor.filter.BeautificationFilter;
import com.android.camera.p004ui.AutoFitSurfaceView;
import com.android.camera.p004ui.Camera2FaceView;
import com.android.camera.p004ui.CameraControls;
import com.android.camera.p004ui.CountDownView;
import com.android.camera.p004ui.FlashToggleButton;
import com.android.camera.p004ui.FocusIndicator;
import com.android.camera.p004ui.MenuHelp;
import com.android.camera.p004ui.OneUICameraControls;
import com.android.camera.p004ui.PieRenderer;
import com.android.camera.p004ui.RenderOverlay;
import com.android.camera.p004ui.RotateImageView;
import com.android.camera.p004ui.RotateLayout;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.p004ui.SelfieFlashView;
import com.android.camera.p004ui.TrackingFocusRenderer;
import com.android.camera.p004ui.ZoomRenderer;
import com.android.camera.p004ui.ZoomRenderer.OnZoomChangedListener;
import com.android.camera.util.CameraUtil;
import com.asus.scenedetectlib.BuildConfig;
import java.util.List;
import java.util.Locale;
import org.codeaurora.snapcam.C0905R;

public class CaptureUI implements FocusUI, SingleTapListener, CameraFaceDetectionCallback, Listener, OnPauseButtonListener {
    private static final int ANIMATION_DURATION = 300;
    private static final int AUTOMATIC_MODE = 0;
    private static final int CLICK_THRESHOLD = 200;
    private static final int DEFAULT_MAKEUP_LEVEL = 50;
    private static final int FILTER_MENU_IN_ANIMATION = 1;
    private static final int FILTER_MENU_NONE = 0;
    private static final int FILTER_MENU_ON = 2;
    private static final int FLASH_STATUS_ERROR = -1;
    private static final int FLASH_STATUS_OFF = 0;
    private static final int HIGHLIGHT_COLOR = -13388315;
    private static final String TAG = "SnapCam_CaptureUI";
    public int SCENE_DETECT_PEOPLE_NUMBER = 0;
    private Callback callback = new Callback() {
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
            StringBuilder sb = new StringBuilder();
            sb.append("surfaceChanged: width =");
            sb.append(i2);
            sb.append(", height = ");
            sb.append(i3);
            Log.v(CaptureUI.TAG, sb.toString());
        }

        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.v(CaptureUI.TAG, "surfaceCreated");
            CaptureUI.this.mSurfaceHolder = surfaceHolder;
            CaptureUI.this.previewUIReady();
            if (CaptureUI.this.mTrackingFocusRenderer != null && CaptureUI.this.mTrackingFocusRenderer.isVisible()) {
                CaptureUI.this.mTrackingFocusRenderer.setSurfaceDim(CaptureUI.this.mSurfaceView.getLeft(), CaptureUI.this.mSurfaceView.getTop(), CaptureUI.this.mSurfaceView.getRight(), CaptureUI.this.mSurfaceView.getBottom());
            }
        }

        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.v(CaptureUI.TAG, "surfaceDestroyed");
            CaptureUI.this.mSurfaceHolder = null;
            CaptureUI.this.previewUIDestroyed();
        }
    };
    private Callback callbackMono = new Callback() {
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
        }

        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        }

        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
            CaptureUI.this.mSurfaceHolderMono = surfaceHolder;
            if (CaptureUI.this.mMonoDummyOutputAllocation != null) {
                CaptureUI.this.mMonoDummyOutputAllocation.setSurface(CaptureUI.this.mSurfaceHolderMono.getSurface());
            }
        }
    };
    public boolean isBokehMode = false;
    /* access modifiers changed from: private */
    public boolean isFrontBackSwitcherOn = false;
    public boolean isMakeUp = false;
    /* access modifiers changed from: private */
    public boolean isVideoButtonOn = false;
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    /* access modifiers changed from: private */
    public ImageView mBokehImageView;
    private SeekBar mBokehSeekBar;
    /* access modifiers changed from: private */
    public View mBokehSwitcher;
    private RotateLayout mBokehTipRect;
    private TextView mBokehTipText;
    private int mBottomMargin = 0;
    private RotateImageView mButton;
    /* access modifiers changed from: private */
    public OneUICameraControls mCameraControls;
    private ImageView mCancelButton;
    private CountDownView mCountDownView;
    private LinearLayout mCustomBodyView;
    private boolean mCustomViewVisibility = false;
    /* access modifiers changed from: private */
    public DecodeImageForReview mDecodeTaskForReview = null;
    private Point mDisplaySize = new Point();
    /* access modifiers changed from: private */
    public int mDownSampleFactor = 4;
    private ImageView mExitBestMode;
    /* access modifiers changed from: private */
    public Camera2FaceView mFaceView;
    private ViewGroup mFilterLayout;
    private int mFilterMenuStatus;
    private View mFilterModeSwitcher;
    private FlashToggleButton mFlashButton;
    private int mFlashStatus = -1;
    private View mFrontBackSwitcher;
    private PreviewGestures mGestures;
    private boolean mIsMediaSaved = true;
    /* access modifiers changed from: private */
    public boolean mIsMonoDummyAllocationEverUsed = false;
    /* access modifiers changed from: private */
    public boolean mIsSceneModeLabelClose = false;
    private boolean mIsTouchAF = false;
    private boolean mIsUpdateFlashStatus = false;
    private boolean mIsUpdateMakeupStatus = false;
    private boolean mIsVideoUI = false;
    /* access modifiers changed from: private */
    public ImageView mMakeupButton;
    /* access modifiers changed from: private */
    public SeekBar mMakeupCleanSeekBar;
    /* access modifiers changed from: private */
    public SeekBar mMakeupSeekBar;
    /* access modifiers changed from: private */
    public View mMakeupSeekBarLayout;
    /* access modifiers changed from: private */
    public SeekBar mMakeupWhitenSeekBar;
    /* access modifiers changed from: private */
    public MenuHelp mMenuHelp;
    private View mModeDetectSwitcher;
    /* access modifiers changed from: private */
    public CaptureModule mModule;
    /* access modifiers changed from: private */
    public Allocation mMonoDummyAllocation;
    /* access modifiers changed from: private */
    public Allocation mMonoDummyOutputAllocation;
    private RotateImageView mMuteButton;
    private int mOrientation;
    private PauseButton mPauseButton;
    /* access modifiers changed from: private */
    public PieRenderer mPieRenderer;
    private View mPreviewCover;
    int mPreviewHeight;
    /* access modifiers changed from: private */
    public FrameLayout mPreviewLayout;
    int mPreviewWidth;
    private View mProModeCloseButton;
    private RotateLayout mRecordingTimeRect;
    private TextView mRecordingTimeView;
    private RenderOverlay mRenderOverlay;
    private View mReviewCancelButton;
    private View mReviewDoneButton;
    /* access modifiers changed from: private */
    public ImageView mReviewImage;
    private View mReviewPlayButton;
    private View mReviewRetakeButton;
    private View mRootView;
    /* access modifiers changed from: private */
    public int mSceneModeIndex = 0;
    /* access modifiers changed from: private */
    public AlertDialog mSceneModeInstructionalDialog = null;
    private ImageView mSceneModeLabelCloseIcon;
    /* access modifiers changed from: private */
    public RotateLayout mSceneModeLabelRect;
    private LinearLayout mSceneModeLabelView;
    private TextView mSceneModeName;
    private View mSceneModeSwitcher;
    private float mScreenBrightness = 0.0f;
    private int mScreenRatio = 0;
    /* access modifiers changed from: private */
    public View mSeekbarBody;
    private ImageView mSeekbarToggleButton;
    private SelfieFlashView mSelfieView;
    /* access modifiers changed from: private */
    public SettingsManager mSettingsManager;
    private ShutterButton mShutterButton;
    /* access modifiers changed from: private */
    public SurfaceHolder mSurfaceHolder;
    /* access modifiers changed from: private */
    public SurfaceHolder mSurfaceHolderMono;
    /* access modifiers changed from: private */
    public AutoFitSurfaceView mSurfaceView;
    /* access modifiers changed from: private */
    public AutoFitSurfaceView mSurfaceViewMono;
    private ImageView mThumbnail;
    private View mTimeLapseLabel;
    private int mTopMargin = 0;
    /* access modifiers changed from: private */
    public TrackingFocusRenderer mTrackingFocusRenderer;
    private boolean mUIhidden = false;
    /* access modifiers changed from: private */
    public ImageView mVideoButton;
    private TextView mWaitProcessText;
    /* access modifiers changed from: private */
    public ZoomRenderer mZoomRenderer;

    /* renamed from: com.android.camera.CaptureUI$36 */
    static /* synthetic */ class C063836 {
        static final /* synthetic */ int[] $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam = new int[SceneTypeParam.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(30:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|30) */
        /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x0040 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x004b */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0056 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0062 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x007a */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0086 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0092 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x009e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0014 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x002a */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0035 */
        static {
            /*
                com.android.camera.CaptureUI$SceneTypeParam[] r0 = com.android.camera.CaptureUI.SceneTypeParam.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam = r0
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x0014 }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.OTHERS     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x001f }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.CAT     // Catch:{ NoSuchFieldError -> 0x001f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x002a }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.DOG     // Catch:{ NoSuchFieldError -> 0x002a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x002a }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x002a }
            L_0x002a:
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x0035 }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.FOOD     // Catch:{ NoSuchFieldError -> 0x0035 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0035 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0035 }
            L_0x0035:
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x0040 }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.GREEN_LAND     // Catch:{ NoSuchFieldError -> 0x0040 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0040 }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0040 }
            L_0x0040:
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x004b }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.PLANT     // Catch:{ NoSuchFieldError -> 0x004b }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x004b }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x004b }
            L_0x004b:
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x0056 }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.OCEAN     // Catch:{ NoSuchFieldError -> 0x0056 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0056 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0056 }
            L_0x0056:
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x0062 }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.PEOPLE     // Catch:{ NoSuchFieldError -> 0x0062 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0062 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0062 }
            L_0x0062:
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x006e }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.SKY     // Catch:{ NoSuchFieldError -> 0x006e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006e }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006e }
            L_0x006e:
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x007a }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.SNOW     // Catch:{ NoSuchFieldError -> 0x007a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x007a }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x007a }
            L_0x007a:
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x0086 }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.STAGE     // Catch:{ NoSuchFieldError -> 0x0086 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0086 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0086 }
            L_0x0086:
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x0092 }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.SUNSET     // Catch:{ NoSuchFieldError -> 0x0092 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0092 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0092 }
            L_0x0092:
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x009e }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.FLOWER     // Catch:{ NoSuchFieldError -> 0x009e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009e }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009e }
            L_0x009e:
                int[] r0 = $SwitchMap$com$android$camera$CaptureUI$SceneTypeParam     // Catch:{ NoSuchFieldError -> 0x00aa }
                com.android.camera.CaptureUI$SceneTypeParam r1 = com.android.camera.CaptureUI.SceneTypeParam.TEXT     // Catch:{ NoSuchFieldError -> 0x00aa }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00aa }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00aa }
            L_0x00aa:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CaptureUI.C063836.<clinit>():void");
        }
    }

    private class DecodeImageForReview extends DecodeTask {
        public DecodeImageForReview(byte[] bArr, int i) {
            super(bArr, i);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
            if (!isCancelled()) {
                CaptureUI.this.mReviewImage.setImageBitmap(bitmap);
                CaptureUI.this.mReviewImage.setVisibility(0);
                CaptureUI.this.mDecodeTaskForReview = null;
            }
        }
    }

    private class DecodeTask extends AsyncTask<Void, Void, Bitmap> {
        private final byte[] mData;
        private int mOrientation;

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
        }

        public DecodeTask(byte[] bArr, int i) {
            this.mData = bArr;
            this.mOrientation = i;
        }

        /* access modifiers changed from: protected */
        public Bitmap doInBackground(Void... voidArr) {
            Bitmap downSample = CameraUtil.downSample(this.mData, CaptureUI.this.mDownSampleFactor);
            if (this.mOrientation == 0 || downSample == null) {
                return downSample;
            }
            Matrix matrix = new Matrix();
            matrix.preRotate((float) this.mOrientation);
            return Bitmap.createBitmap(downSample, 0, 0, downSample.getWidth(), downSample.getHeight(), matrix, false);
        }
    }

    private class MonoDummyListener implements OnBufferAvailableListener {
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;

        public MonoDummyListener(ScriptIntrinsicYuvToRGB scriptIntrinsicYuvToRGB) {
            this.yuvToRgbIntrinsic = scriptIntrinsicYuvToRGB;
        }

        public void onBufferAvailable(Allocation allocation) {
            if (CaptureUI.this.mMonoDummyAllocation != null) {
                CaptureUI.this.mMonoDummyAllocation.ioReceive();
                CaptureUI.this.mIsMonoDummyAllocationEverUsed = true;
                if (CaptureUI.this.mSurfaceViewMono.getVisibility() == 0) {
                    try {
                        this.yuvToRgbIntrinsic.forEach(CaptureUI.this.mMonoDummyOutputAllocation);
                        CaptureUI.this.mMonoDummyOutputAllocation.ioSend();
                    } catch (Exception e) {
                        Log.e(CaptureUI.TAG, e.toString());
                    }
                }
            }
        }
    }

    public enum SceneTypeParam {
        OTHERS,
        CAT,
        DOG,
        FOOD,
        GREEN_LAND,
        PLANT,
        OCEAN,
        PEOPLE,
        SKY,
        SNOW,
        STAGE,
        SUNSET,
        FLOWER,
        TEXT
    }

    private class ZoomChangeListener implements OnZoomChangedListener {
        public void onZoomValueChanged(int i) {
        }

        private ZoomChangeListener() {
        }

        public void onZoomValueChanged(float f) {
            CaptureUI.this.mModule.onZoomChanged(f);
            if (CaptureUI.this.mZoomRenderer != null) {
                CaptureUI.this.mZoomRenderer.setZoom(f);
            }
        }

        public void onZoomStart() {
            if (CaptureUI.this.mPieRenderer != null) {
                CaptureUI.this.mPieRenderer.hide();
                CaptureUI.this.mPieRenderer.setBlockFocus(true);
            }
        }

        public void onZoomEnd() {
            if (CaptureUI.this.mPieRenderer != null) {
                CaptureUI.this.mPieRenderer.setBlockFocus(false);
            }
        }
    }

    public void onFaceDetection(Face[] faceArr, CameraProxy cameraProxy) {
    }

    public void onOrientationChanged() {
    }

    public void pauseFaceDetection() {
    }

    public void resumeFaceDetection() {
    }

    /* access modifiers changed from: private */
    public void previewUIReady() {
        SurfaceHolder surfaceHolder = this.mSurfaceHolder;
        if (surfaceHolder != null && surfaceHolder.getSurface().isValid()) {
            this.mModule.onPreviewUIReady();
            if (this.mIsVideoUI || this.mModule.getCurrentIntentMode() != 0) {
                ImageView imageView = this.mThumbnail;
                if (imageView != null) {
                    imageView.setVisibility(4);
                    this.mThumbnail = null;
                    this.mActivity.updateThumbnail(this.mThumbnail);
                    return;
                }
            }
            if (!this.mIsVideoUI && this.mModule.getCurrentIntentMode() == 0) {
                if (this.mThumbnail == null) {
                    this.mThumbnail = (ImageView) this.mRootView.findViewById(C0905R.C0907id.preview_thumb);
                }
                this.mActivity.updateThumbnail(this.mThumbnail);
            }
        }
    }

    /* access modifiers changed from: private */
    public void previewUIDestroyed() {
        this.mModule.onPreviewUIDestroyed();
    }

    public TrackingFocusRenderer getTrackingFocusRenderer() {
        return this.mTrackingFocusRenderer;
    }

    public Point getDisplaySize() {
        return this.mDisplaySize;
    }

    public CaptureUI(CameraActivity cameraActivity, final CaptureModule captureModule, View view) {
        this.mActivity = cameraActivity;
        this.mModule = captureModule;
        this.mRootView = view;
        this.mSettingsManager = SettingsManager.getInstance();
        this.mSettingsManager.registerListener(this);
        this.mActivity.getLayoutInflater().inflate(C0905R.layout.capture_module, (ViewGroup) this.mRootView, true);
        this.mPreviewCover = this.mRootView.findViewById(C0905R.C0907id.preview_cover);
        this.mSurfaceView = (AutoFitSurfaceView) this.mRootView.findViewById(C0905R.C0907id.mdp_preview_content);
        this.mSurfaceHolder = this.mSurfaceView.getHolder();
        this.mSurfaceHolder.addCallback(this.callback);
        this.mSurfaceView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                int i9 = i3 - i;
                int i10 = i4 - i2;
                if (CaptureUI.this.mFaceView != null) {
                    CaptureUI.this.mFaceView.onSurfaceTextureSizeChanged(i9, i10);
                }
            }
        });
        this.mSurfaceViewMono = (AutoFitSurfaceView) this.mRootView.findViewById(C0905R.C0907id.mdp_preview_content_mono);
        this.mSurfaceViewMono.setZOrderMediaOverlay(true);
        this.mSurfaceHolderMono = this.mSurfaceViewMono.getHolder();
        this.mSurfaceHolderMono.addCallback(this.callbackMono);
        this.mRenderOverlay = (RenderOverlay) this.mRootView.findViewById(C0905R.C0907id.render_overlay);
        this.mShutterButton = (ShutterButton) this.mRootView.findViewById(C0905R.C0907id.shutter_button);
        this.mVideoButton = (ImageView) this.mRootView.findViewById(C0905R.C0907id.video_button);
        this.mExitBestMode = (ImageView) this.mRootView.findViewById(C0905R.C0907id.exit_best_mode);
        this.mFilterModeSwitcher = this.mRootView.findViewById(C0905R.C0907id.filter_mode_switcher);
        this.mModeDetectSwitcher = this.mRootView.findViewById(C0905R.C0907id.scenemode_detect_switcher);
        this.mBokehSwitcher = this.mRootView.findViewById(C0905R.C0907id.bokeh_switcher);
        this.mSceneModeSwitcher = this.mRootView.findViewById(C0905R.C0907id.scene_mode_switcher);
        this.mFrontBackSwitcher = this.mRootView.findViewById(C0905R.C0907id.front_back_switcher);
        this.mMakeupButton = (ImageView) this.mRootView.findViewById(C0905R.C0907id.ts_makeup_switcher);
        this.mMakeupSeekBarLayout = this.mRootView.findViewById(C0905R.C0907id.makeup_seekbar_layout);
        this.mSeekbarBody = this.mRootView.findViewById(C0905R.C0907id.seekbar_body);
        this.mCustomBodyView = (LinearLayout) this.mRootView.findViewById(C0905R.C0907id.seekbar_custom_body);
        this.mButton = (RotateImageView) this.mRootView.findViewById(C0905R.C0907id.setting_button);
        this.mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                CaptureUI.this.clearFocus();
                CaptureUI.this.removeFilterMenu(false);
                Intent intent = new Intent(CaptureUI.this.mActivity, SettingsActivity.class);
                intent.putExtra(CameraUtil.KEY_IS_SECURE_CAMERA, CaptureUI.this.mActivity.isSecureCamera());
                CaptureUI.this.mActivity.startActivity(intent);
            }
        });
        this.mMakeupWhitenSeekBar = (SeekBar) this.mRootView.findViewById(C0905R.C0907id.makeup_whiten_seekbar);
        this.mMakeupWhitenSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                captureModule.setMakeUpWhitenDegree(i);
            }
        });
        this.mMakeupCleanSeekBar = (SeekBar) this.mRootView.findViewById(C0905R.C0907id.makeup_clean_seekbar);
        this.mMakeupCleanSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                captureModule.setMakeUpCleanDegree(i);
            }
        });
        this.mSeekbarToggleButton = (ImageView) this.mRootView.findViewById(C0905R.C0907id.seekbar_toggle);
        this.mWaitProcessText = (TextView) this.mRootView.findViewById(C0905R.C0907id.wait_progress_text);
        this.mSeekbarToggleButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                String str = "50";
                if (CaptureUI.this.mSeekbarBody.getVisibility() == 0) {
                    CaptureUI.this.setCustomViewVisibility(true);
                    CaptureUI.this.mSettingsManager.setValue(SettingsManager.KEY_MAKEUP_CLEAN_DEGREE, str);
                    CaptureUI.this.mSettingsManager.setValue(SettingsManager.KEY_MAKEUP_WHITEN_DEGREE, str);
                    CaptureUI.this.mMakeupWhitenSeekBar.setProgress(CaptureUI.DEFAULT_MAKEUP_LEVEL);
                    CaptureUI.this.mMakeupCleanSeekBar.setProgress(CaptureUI.DEFAULT_MAKEUP_LEVEL);
                    return;
                }
                CaptureUI.this.setCustomViewVisibility(false);
                CaptureUI.this.mSettingsManager.setValue(SettingsManager.KEY_MAKEUP, str);
                CaptureUI.this.mMakeupSeekBar.setProgress(CaptureUI.DEFAULT_MAKEUP_LEVEL);
            }
        });
        this.mMakeupSeekBar = (SeekBar) this.mRootView.findViewById(C0905R.C0907id.makeup_seekbar);
        this.mMakeupSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                int i2 = (i * 9) / 10;
                SettingsManager access$1000 = CaptureUI.this.mSettingsManager;
                StringBuilder sb = new StringBuilder();
                sb.append(i);
                sb.append(BuildConfig.FLAVOR);
                access$1000.setValue(SettingsManager.KEY_MAKEUP, sb.toString());
                captureModule.setMakeUpDegree(i);
            }
        });
        this.mMakeupButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                CaptureModule captureModule = captureModule;
                if (captureModule != null && !captureModule.isAllSessionClosed()) {
                    String value = CaptureUI.this.mSettingsManager.getValue("pref_camera_facedetection_key");
                    if (value == null || !value.equals("off")) {
                        CaptureUI.this.toggleMakeup();
                        CaptureUI.this.updateMenus();
                        return;
                    }
                    CaptureUI.this.showAlertDialog();
                }
            }
        });
        setMakeupButtonIcon();
        this.mFlashButton = (FlashToggleButton) this.mRootView.findViewById(C0905R.C0907id.flash_button);
        this.mProModeCloseButton = this.mRootView.findViewById(C0905R.C0907id.promode_close_button);
        this.mProModeCloseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                CaptureUI.this.mSettingsManager.setValue(SettingsManager.KEY_SCENE_MODE, "0");
            }
        });
        this.mBokehSeekBar = (SeekBar) this.mRootView.findViewById(C0905R.C0907id.bokeh_seekbar);
        this.mBokehSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                captureModule.setBokehBlurDegree(i / 10);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                PreferenceManager.getDefaultSharedPreferences(CaptureUI.this.mActivity).edit().putInt(SettingsManager.KEY_BOKEH_BLUR_DEGREE, seekBar.getProgress()).apply();
            }
        });
        this.mBokehTipText = (TextView) this.mRootView.findViewById(C0905R.C0907id.bokeh_status);
        this.mBokehTipRect = (RotateLayout) this.mRootView.findViewById(C0905R.C0907id.bokeh_tip_rect);
        updateBokehTipRectPosition(this.mOrientation);
        initFilterModeButton();
        initBokehModeButton();
        initSceneModeButton();
        initSwitchCamera();
        initFlashButton();
        updateMenus();
        this.mRecordingTimeView = (TextView) this.mRootView.findViewById(C0905R.C0907id.recording_time);
        this.mRecordingTimeRect = (RotateLayout) this.mRootView.findViewById(C0905R.C0907id.recording_time_rect);
        this.mTimeLapseLabel = this.mRootView.findViewById(C0905R.C0907id.time_lapse_label);
        this.mTimeLapseLabel.setVisibility(8);
        this.mPauseButton = (PauseButton) this.mRootView.findViewById(C0905R.C0907id.video_pause);
        this.mPauseButton.setOnPauseButtonListener(this);
        this.mMuteButton = (RotateImageView) this.mRootView.findViewById(C0905R.C0907id.mute_button);
        this.mMuteButton.setVisibility(0);
        setMuteButtonResource(!this.mModule.isAudioMute());
        this.mMuteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                boolean z = true;
                boolean z2 = !CaptureUI.this.mModule.isAudioMute();
                CaptureUI.this.mModule.setMute(z2, true);
                CaptureUI captureUI = CaptureUI.this;
                if (z2) {
                    z = false;
                }
                captureUI.setMuteButtonResource(z);
            }
        });
        this.mExitBestMode.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SettingsManager.getInstance().setValueIndex(SettingsManager.KEY_SCENE_MODE, 0);
            }
        });
        ((RotateImageView) this.mRootView.findViewById(C0905R.C0907id.mute_button)).setVisibility(8);
        this.mSceneModeLabelRect = (RotateLayout) this.mRootView.findViewById(C0905R.C0907id.scene_mode_label_rect);
        this.mSceneModeName = (TextView) this.mRootView.findViewById(C0905R.C0907id.scene_mode_label);
        this.mSceneModeLabelCloseIcon = (ImageView) this.mRootView.findViewById(C0905R.C0907id.scene_mode_label_close);
        this.mSceneModeLabelCloseIcon.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                CaptureUI.this.mIsSceneModeLabelClose = true;
                CaptureUI.this.mSceneModeLabelRect.setVisibility(8);
            }
        });
        this.mCameraControls = (OneUICameraControls) this.mRootView.findViewById(C0905R.C0907id.camera_controls);
        this.mCameraControls.setFocusable(false);
        this.mFaceView = (Camera2FaceView) this.mRootView.findViewById(C0905R.C0907id.face_view);
        this.mCancelButton = (ImageView) this.mRootView.findViewById(C0905R.C0907id.cancel_button);
        final int currentIntentMode = this.mModule.getCurrentIntentMode();
        if (currentIntentMode != 0) {
            this.mCameraControls.setIntentMode(currentIntentMode);
            this.mCameraControls.setVideoMode(false);
            this.mCancelButton.setVisibility(0);
            this.mReviewCancelButton = this.mRootView.findViewById(C0905R.C0907id.preview_btn_cancel);
            this.mReviewDoneButton = this.mRootView.findViewById(C0905R.C0907id.done_button);
            this.mReviewRetakeButton = this.mRootView.findViewById(C0905R.C0907id.preview_btn_retake);
            this.mReviewPlayButton = this.mRootView.findViewById(C0905R.C0907id.preview_play);
            this.mPreviewLayout = (FrameLayout) this.mRootView.findViewById(C0905R.C0907id.preview_of_intent);
            this.mReviewImage = (ImageView) this.mRootView.findViewById(C0905R.C0907id.preview_content);
            this.mReviewCancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    CaptureUI.this.mActivity.setResultEx(0, new Intent());
                    CaptureUI.this.mActivity.finish();
                }
            });
            this.mReviewRetakeButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    CaptureUI.this.mPreviewLayout.setVisibility(8);
                    CaptureUI.this.mReviewImage.setImageBitmap(null);
                    CaptureUI.this.mModule.setJpegImageData(null);
                }
            });
            this.mReviewDoneButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    int i = currentIntentMode;
                    if (i == 1) {
                        CaptureUI.this.mModule.onCaptureDone();
                    } else if (i == 2) {
                        CaptureUI.this.mModule.onRecordingDone(true);
                    }
                }
            });
            this.mReviewPlayButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    CaptureUI.this.mModule.startPlayVideoActivity();
                }
            });
            this.mCancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    CaptureUI.this.mModule.cancelCapture();
                }
            });
        }
        this.mActivity.getWindowManager().getDefaultDisplay().getSize(this.mDisplaySize);
        Point point = this.mDisplaySize;
        this.mScreenRatio = CameraUtil.determineRatio(point.x, point.y);
        if (this.mScreenRatio == 1) {
            Point point2 = this.mDisplaySize;
            int i = point2.x;
            int i2 = point2.y;
            if (i > i2) {
                i2 = i;
            }
            int dimensionPixelSize = this.mActivity.getResources().getDimensionPixelSize(C0905R.dimen.preview_top_margin);
            int i3 = i2 / 4;
            this.mTopMargin = (i3 * dimensionPixelSize) / (dimensionPixelSize + this.mActivity.getResources().getDimensionPixelSize(C0905R.dimen.preview_bottom_margin));
            this.mBottomMargin = i3 - this.mTopMargin;
        }
        if (this.mPieRenderer == null) {
            this.mPieRenderer = new PieRenderer(this.mActivity);
            this.mRenderOverlay.addRenderer(this.mPieRenderer);
        }
        if (this.mZoomRenderer == null) {
            this.mZoomRenderer = new ZoomRenderer(this.mActivity);
            this.mRenderOverlay.addRenderer(this.mZoomRenderer);
        }
        if (this.mTrackingFocusRenderer == null) {
            this.mTrackingFocusRenderer = new TrackingFocusRenderer(this.mActivity, this.mModule, this);
            this.mRenderOverlay.addRenderer(this.mTrackingFocusRenderer);
        }
        if (this.mModule.isTrackingFocusSettingOn()) {
            this.mTrackingFocusRenderer.setVisible(true);
        } else {
            this.mTrackingFocusRenderer.setVisible(false);
        }
        if (this.mGestures == null) {
            PreviewGestures previewGestures = new PreviewGestures(this.mActivity, this, this.mZoomRenderer, this.mPieRenderer, this.mTrackingFocusRenderer);
            this.mGestures = previewGestures;
            this.mRenderOverlay.setGestures(this.mGestures);
        }
        this.mButton.setFocusable(false);
        this.mSeekbarToggleButton.setFocusable(false);
        this.mMakeupSeekBar.setFocusable(false);
        this.mBokehSeekBar.setFocusable(false);
        this.mMakeupWhitenSeekBar.setFocusable(false);
        this.mMakeupCleanSeekBar.setFocusable(false);
        this.mGestures.setRenderOverlay(this.mRenderOverlay);
        this.mRenderOverlay.requestLayout();
        this.mActivity.setPreviewGestures(this.mGestures);
        this.mRecordingTimeRect.setVisibility(8);
        showFirstTimeHelp();
    }

    /* access modifiers changed from: private */
    public void showAlertDialog() {
        if (!this.mActivity.isFinishing()) {
            new Builder(this.mActivity).setIcon(17301543).setMessage(C0905R.string.text_tsmakeup_alert_msg).setPositiveButton(C0905R.string.text_tsmakeup_alert_continue, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    CaptureUI.this.toggleMakeup();
                    CaptureUI.this.updateMenus();
                    CaptureUI.this.mSettingsManager.setValue("pref_camera_facedetection_key", RecordLocationPreference.VALUE_ON);
                }
            }).setNegativeButton(C0905R.string.text_tsmakeup_alert_quit, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).show();
        }
    }

    /* access modifiers changed from: protected */
    public void showCapturedImageForReview(byte[] bArr, int i) {
        this.mDecodeTaskForReview = new DecodeImageForReview(bArr, i);
        this.mDecodeTaskForReview.execute(new Void[0]);
        if (getCurrentIntentMode() != 0) {
            if (this.mFilterMenuStatus == 2) {
                removeFilterMenu(false);
            }
            this.mPreviewLayout.setVisibility(0);
            CameraUtil.fadeIn(this.mReviewDoneButton);
            CameraUtil.fadeIn(this.mReviewRetakeButton);
        }
    }

    /* access modifiers changed from: protected */
    public void showRecordVideoForReview(Bitmap bitmap) {
        if (getCurrentIntentMode() != 0) {
            if (this.mFilterMenuStatus == 2) {
                removeFilterMenu(false);
            }
            this.mReviewImage.setImageBitmap(bitmap);
            this.mPreviewLayout.setVisibility(0);
            this.mReviewPlayButton.setVisibility(0);
            CameraUtil.fadeIn(this.mReviewDoneButton);
            CameraUtil.fadeIn(this.mReviewRetakeButton);
        }
    }

    private int getCurrentIntentMode() {
        return this.mModule.getCurrentIntentMode();
    }

    /* access modifiers changed from: private */
    public void toggleMakeup() {
        SettingsManager settingsManager = this.mSettingsManager;
        String str = SettingsManager.KEY_MAKEUP;
        String value = settingsManager.getValue(str);
        if (this.isMakeUp) {
            this.isMakeUp = false;
        } else {
            this.isMakeUp = true;
        }
        if (value != null && !this.mIsVideoUI) {
            if (this.isMakeUp) {
                this.mSettingsManager.setValue(str, "50");
                this.mMakeupSeekBar.setProgress(DEFAULT_MAKEUP_LEVEL);
                this.mMakeupSeekBarLayout.setVisibility(0);
                this.mSeekbarBody.setVisibility(0);
                this.mSeekbarToggleButton.setImageResource(C0905R.C0906drawable.seekbar_hide);
                this.isMakeUp = true;
                setCustomViewVisibility(false);
            } else {
                this.mSettingsManager.setValue(str, "0");
                this.mMakeupSeekBar.setProgress(0);
                this.mMakeupSeekBarLayout.setVisibility(8);
                this.isMakeUp = false;
                removeFilterMenu();
            }
            setMakeupButtonIcon();
            this.mModule.restartSession(true);
        }
    }

    /* access modifiers changed from: private */
    public void removeFilterMenu() {
        this.mFilterMenuStatus = 0;
        ViewGroup viewGroup = this.mFilterLayout;
        if (viewGroup != null) {
            ((ViewGroup) this.mRootView).removeView(viewGroup);
            this.mFilterLayout = null;
        }
    }

    /* access modifiers changed from: private */
    public void setMakeupButtonIcon() {
        final String value = this.mSettingsManager.getValue(SettingsManager.KEY_MAKEUP);
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                String str = value;
                if (((str == null || str.equals("0")) && !CaptureUI.this.isMakeUp) || (CaptureUI.this.mModule.isBackCamera() && CaptureUI.this.isBokehMode)) {
                    CaptureUI.this.mMakeupButton.setImageResource(C0905R.C0906drawable.ic_ts_makeup_off_new);
                    CaptureUI.this.mMakeupSeekBarLayout.setVisibility(8);
                    return;
                }
                CaptureUI.this.mMakeupButton.setImageResource(C0905R.C0906drawable.ic_ts_makeup_on_new);
                CaptureUI.this.mMakeupSeekBarLayout.setVisibility(0);
            }
        });
    }

    private void setMakeupSeekBar() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_MAKEUP);
        if (value != null && !value.equals("0")) {
            if (!this.mModule.isBackCamera() || !this.isBokehMode) {
                this.mMakeupSeekBar.setProgress(Integer.parseInt(value));
                this.isMakeUp = true;
            }
        }
    }

    public void onCameraOpened(List<Integer> list) {
        this.mGestures.setCaptureUI(this);
        this.mGestures.setZoomEnabled(this.mSettingsManager.isZoomSupported(list));
        initializeZoom(list);
    }

    public void reInitUI() {
        initSceneModeButton();
        initFilterModeButton();
        initFlashButton();
        setMakeupButtonIcon();
        setMakeupSeekBar();
        showSceneModeLabel();
        updateMenus();
        if (this.mModule.isTrackingFocusSettingOn()) {
            this.mTrackingFocusRenderer.setVisible(false);
            this.mTrackingFocusRenderer.setVisible(true);
        } else {
            this.mTrackingFocusRenderer.setVisible(false);
        }
        if (this.mSurfaceViewMono != null) {
            SettingsManager settingsManager = this.mSettingsManager;
            if (settingsManager != null) {
                String str = SettingsManager.KEY_MONO_PREVIEW;
                if (settingsManager.getValue(str) != null && this.mSettingsManager.getValue(str).equalsIgnoreCase(RecordLocationPreference.VALUE_ON)) {
                    this.mSurfaceViewMono.setVisibility(0);
                    return;
                }
            }
            this.mSurfaceViewMono.setVisibility(8);
        }
    }

    public void initializeProMode(boolean z) {
        this.mCameraControls.setProMode(z);
        this.mCameraControls.setFixedFocus(this.mSettingsManager.isFixedFocus(this.mModule.getMainCameraId()));
        initializeChildModeUIUpdate(z);
    }

    public void initializeChildModeUIUpdate(boolean z) {
        if (z) {
            this.mVideoButton.setVisibility(4);
        } else if (this.mModule.getCurrentIntentMode() == 0) {
            this.mVideoButton.setVisibility(0);
        }
    }

    public void initializeBokehMode(boolean z) {
        if (z) {
            String str = "0";
            int parseInt = Integer.parseInt(str);
            String value = this.mSettingsManager.getValue(SettingsManager.KEY_BOKEH_BLUR_DEGREE);
            if (value != null) {
                parseInt = Integer.parseInt(value);
            }
            if (parseInt == Integer.parseInt(str)) {
                parseInt = DEFAULT_MAKEUP_LEVEL;
            }
            this.mBokehSeekBar.setProgress(parseInt);
            this.mBokehSeekBar.setVisibility(0);
            this.mVideoButton.setVisibility(4);
            updateBokehTipRectPosition(this.mOrientation);
            this.mBokehTipRect.setVisibility(0);
            this.mBokehTipText.setVisibility(0);
            if (this.mModule.isBackCamera()) {
                this.mBokehTipText.setText(C0905R.string.dual_camera_bokeh);
            } else if (this.isMakeUp) {
                this.mBokehTipText.setText(C0905R.string.single_beauty_bokeh);
            } else {
                this.mBokehTipText.setText(C0905R.string.single_camera_bokeh);
            }
        } else {
            RotateLayout rotateLayout = this.mBokehTipRect;
            if (rotateLayout != null && !this.isMakeUp) {
                rotateLayout.setVisibility(4);
                this.mBokehTipText.setVisibility(4);
            }
            this.mBokehSeekBar.setVisibility(4);
        }
    }

    public TextView getBokehTipView() {
        return this.mBokehTipText;
    }

    public RotateLayout getBokehTipRct() {
        return this.mBokehTipRect;
    }

    public void initializeFirstTime() {
        int currentIntentMode = this.mModule.getCurrentIntentMode();
        if (currentIntentMode == 1) {
            this.mVideoButton.setVisibility(4);
        } else if (currentIntentMode == 2) {
            this.mShutterButton.setVisibility(4);
        } else {
            this.mShutterButton.setVisibility(0);
            this.mVideoButton.setVisibility(0);
        }
        this.mShutterButton.setOnShutterButtonListener(this.mModule);
        this.mShutterButton.setImageResource(C0905R.C0906drawable.shutter_button_anim);
        this.mShutterButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                CaptureUI.this.doShutterAnimation();
            }
        });
        this.mVideoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (CaptureUI.this.isVideoButtonOn) {
                    CaptureUI.this.isVideoButtonOn = false;
                    CaptureUI.this.mBokehSwitcher.setVisibility(0);
                    CaptureUI.this.mMakeupButton.setVisibility(0);
                } else {
                    CaptureUI.this.isVideoButtonOn = true;
                    CaptureUI.this.mBokehSwitcher.setVisibility(8);
                    CaptureUI.this.mMakeupButton.setVisibility(8);
                    CaptureUI.this.removeFilterMenu(false);
                }
                CaptureUI.this.cancelCountDown();
                CaptureUI.this.mModule.onVideoButtonClick();
            }
        });
    }

    public void initializeZoom(List<Integer> list) {
        if (this.mSettingsManager.isZoomSupported(list) && this.mZoomRenderer != null) {
            this.mZoomRenderer.setZoomMax(Float.valueOf(this.mSettingsManager.getMaxZoom(list)).floatValue());
            this.mZoomRenderer.setZoom(1.0f);
            this.mZoomRenderer.setOnZoomChangeListener(new ZoomChangeListener());
        }
    }

    public void enableGestures(boolean z) {
        PreviewGestures previewGestures = this.mGestures;
        if (previewGestures != null) {
            previewGestures.setEnabled(z);
        }
    }

    public boolean isPreviewMenuBeingShown() {
        return this.mFilterMenuStatus == 2;
    }

    public void removeFilterMenu(boolean z) {
        if (z) {
            animateSlideOut(this.mFilterLayout);
        } else {
            this.mFilterMenuStatus = 0;
            ViewGroup viewGroup = this.mFilterLayout;
            if (viewGroup != null) {
                ((ViewGroup) this.mRootView).removeView(viewGroup);
                this.mFilterLayout = null;
            }
        }
        updateMenus();
    }

    public void openSettingsMenu() {
        FrameLayout frameLayout = this.mPreviewLayout;
        if (frameLayout == null || frameLayout.getVisibility() != 0) {
            clearFocus();
            removeFilterMenu(false);
            this.mActivity.startActivity(new Intent(this.mActivity, SettingsActivity.class));
        }
    }

    public void initSwitchCamera() {
        this.mFrontBackSwitcher.setVisibility(4);
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_CAMERA_ID);
        StringBuilder sb = new StringBuilder();
        sb.append("value of KEY_CAMERA_ID is null? ");
        sb.append(value == null);
        Log.d(TAG, sb.toString());
        if (value != null) {
            this.mFrontBackSwitcher.setVisibility(0);
            this.mFrontBackSwitcher.setOnClickListener(new OnClickListener() {
                /* JADX WARNING: Removed duplicated region for block: B:21:0x009b  */
                /* JADX WARNING: Removed duplicated region for block: B:22:0x00a0  */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void onClick(android.view.View r6) {
                    /*
                        r5 = this;
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        com.android.camera.CaptureModule r6 = r6.mModule
                        boolean r6 = r6.getCameraModeSwitcherAllowed()
                        if (r6 != 0) goto L_0x000d
                        return
                    L_0x000d:
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        com.android.camera.CaptureModule r6 = r6.mModule
                        r0 = 0
                        r6.setCameraModeSwitcherAllowed(r0)
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        r6.removeFilterMenu(r0)
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        com.android.camera.SettingsManager r6 = r6.mSettingsManager
                        java.lang.String r1 = "pref_camera2_id_key"
                        java.lang.String r6 = r6.getValue(r1)
                        if (r6 != 0) goto L_0x002b
                        return
                    L_0x002b:
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        com.android.camera.CaptureModule r6 = r6.mModule
                        boolean r6 = r6.isBackCamera()
                        r2 = 1
                        if (r6 == 0) goto L_0x004c
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        boolean r3 = r6.isBokehMode
                        if (r3 == 0) goto L_0x004c
                        r6.isFrontBackSwitcherOn = r2
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        r6.closeBokeh()
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        r6.isFrontBackSwitcherOn = r0
                        goto L_0x0061
                    L_0x004c:
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        com.android.camera.CaptureModule r6 = r6.mModule
                        boolean r6 = r6.isBackCamera()
                        if (r6 != 0) goto L_0x0061
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        boolean r3 = r6.isBokehMode
                        if (r3 == 0) goto L_0x0061
                        r6.closeBokeh()
                    L_0x0061:
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        com.android.camera.SettingsManager r6 = r6.mSettingsManager
                        int r6 = r6.getValueIndex(r1)
                        com.android.camera.CaptureUI r3 = com.android.camera.CaptureUI.this
                        com.android.camera.SettingsManager r3 = r3.mSettingsManager
                        java.lang.CharSequence[] r3 = r3.getEntries(r1)
                    L_0x0075:
                        int r6 = r6 + r2
                        int r4 = r3.length
                        int r6 = r6 % r4
                        r4 = r3[r6]
                        if (r4 == 0) goto L_0x0075
                        com.android.camera.CaptureUI r3 = com.android.camera.CaptureUI.this
                        r3.isMakeUp = r0
                        com.android.camera.SettingsManager r3 = r3.mSettingsManager
                        r3.setValueIndex(r1, r6)
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        com.android.camera.SettingsManager r6 = r6.mSettingsManager
                        java.lang.String r1 = "pref_camera2_makeup_key"
                        java.lang.String r6 = r6.getValue(r1)
                        java.lang.String r1 = "0"
                        boolean r6 = r6.equals(r1)
                        if (r6 == 0) goto L_0x00a0
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        r6.isMakeUp = r0
                        goto L_0x00a4
                    L_0x00a0:
                        com.android.camera.CaptureUI r6 = com.android.camera.CaptureUI.this
                        r6.isMakeUp = r2
                    L_0x00a4:
                        com.android.camera.CaptureUI r5 = com.android.camera.CaptureUI.this
                        r5.setMakeupButtonIcon()
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CaptureUI.C062625.onClick(android.view.View):void");
                }
            });
        }
    }

    public void initFlashButton() {
        this.mFlashButton.init(false);
        enableView(this.mFlashButton, SettingsManager.KEY_FLASH_MODE);
    }

    public void initSceneModeButton() {
        this.mSceneModeSwitcher.setVisibility(4);
        SettingsManager settingsManager = this.mSettingsManager;
        String str = SettingsManager.KEY_SCENE_MODE;
        String value = settingsManager.getValue(str);
        if (value != null) {
            int parseInt = Integer.parseInt(value);
            if (parseInt == 109 || parseInt == 18) {
                this.mSettingsManager.setValue(SettingsManager.KEY_MAKEUP, "0");
                this.mMakeupSeekBar.setProgress(0);
                this.mMakeupSeekBarLayout.setVisibility(8);
                this.isMakeUp = false;
                removeFilterMenu();
            }
            this.mSceneModeSwitcher.setVisibility(0);
            ((ImageView) this.mSceneModeSwitcher).setImageResource(this.mSettingsManager.getResource(str, 0)[this.mSettingsManager.getValueIndex(str)]);
            this.mSceneModeSwitcher.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    CaptureUI.this.clearFocus();
                    CaptureUI.this.removeFilterMenu(false);
                    Intent intent = new Intent(CaptureUI.this.mActivity, SceneModeActivity.class);
                    intent.putExtra(CameraUtil.KEY_IS_SECURE_CAMERA, CaptureUI.this.mActivity.isSecureCamera());
                    CaptureUI.this.mActivity.startActivity(intent);
                }
            });
        }
    }

    public void initFilterModeButton() {
        this.mFilterModeSwitcher.setVisibility(4);
        SettingsManager settingsManager = this.mSettingsManager;
        String str = SettingsManager.KEY_COLOR_EFFECT;
        if (settingsManager.getValue(str) != null) {
            enableView(this.mFilterModeSwitcher, str);
            this.mFilterModeSwitcher.setVisibility(0);
            this.mFilterModeSwitcher.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    CaptureUI.this.addFilterMode();
                    CaptureUI.this.adjustOrientation();
                    CaptureUI.this.updateMenus();
                }
            });
        }
    }

    public void initBokehModeButton() {
        this.mBokehSwitcher.setVisibility(0);
        View view = this.mBokehSwitcher;
        this.mBokehImageView = (ImageView) view;
        view.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (!CaptureUI.this.mModule.getSessionComplete()) {
                    Log.d(CaptureUI.TAG, "session is not complete");
                    return;
                }
                CaptureUI.this.mModule.setSessionComplete(false);
                CaptureUI captureUI = CaptureUI.this;
                boolean z = captureUI.isBokehMode;
                String str = SettingsManager.KEY_BOKEH_BLUR_DEGREE;
                String str2 = SettingsManager.KEY_BOKEH;
                String str3 = SettingsManager.KEY_SCENE_MODE;
                if (z) {
                    captureUI.mSettingsManager.setValue(str2, "off");
                    CaptureUI.this.closeBokeh();
                    if (!CaptureUI.this.mModule.isBackCamera()) {
                        CaptureUI.this.mSettingsManager.setValue(str, "0");
                        CaptureUI.this.removeFilterMenu();
                    }
                    if (CaptureUI.this.mSceneModeIndex != 0) {
                        SettingsManager.getInstance().setValueIndex(str3, CaptureUI.this.mSceneModeIndex);
                        CaptureUI.this.mSceneModeIndex = 0;
                    }
                } else {
                    if (!captureUI.mModule.isBackCamera()) {
                        CaptureUI.this.mSettingsManager.setValue(str, "50");
                        CaptureUI.this.mSettingsManager.setValueIndex(SettingsManager.KEY_COLOR_EFFECT, 0);
                        CaptureUI.this.mSettingsManager.setValueIndex("pref_camera_picturesize_key", 0);
                    }
                    int valueIndex = CaptureUI.this.mSettingsManager.getValueIndex(str3);
                    if (valueIndex != 0) {
                        CaptureUI.this.isBokehMode = true;
                        SettingsManager.getInstance().setValueIndex(str3, 0);
                        CaptureUI.this.mSceneModeIndex = valueIndex;
                    }
                    CaptureUI.this.openBokeh();
                    CaptureUI.this.mSettingsManager.setValue(str2, RecordLocationPreference.VALUE_ON);
                }
                CaptureUI.this.mSettingsManager.getValue(str3);
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateMenuStatus(boolean z) {
        this.mSceneModeSwitcher.setEnabled(z);
        this.mFlashButton.setEnabled(z);
        if (this.mModule.isBackCamera()) {
            UpdateMakeupLayoutStatus(z);
            this.mMakeupButton.setEnabled(z);
        }
    }

    public void UpdateMakeupLayoutStatus(boolean z) {
        if (z) {
            if (this.mIsUpdateMakeupStatus) {
                this.mMakeupSeekBarLayout.setVisibility(0);
                if (!this.mCustomViewVisibility) {
                    this.mSeekbarBody.setVisibility(0);
                    this.mMakeupSeekBar.setProgress(Integer.parseInt(this.mSettingsManager.getValue(SettingsManager.KEY_MAKEUP)));
                    this.mSeekbarToggleButton.setImageResource(C0905R.C0906drawable.seekbar_hide);
                } else {
                    setCustomViewVisibility(true);
                    this.mSeekbarToggleButton.setVisibility(0);
                }
                this.mMakeupButton.setImageResource(C0905R.C0906drawable.ic_ts_makeup_on_new);
                this.isMakeUp = true;
                this.mIsUpdateMakeupStatus = false;
            }
        } else if (this.isMakeUp) {
            if (!this.mCustomViewVisibility) {
                this.mMakeupSeekBarLayout.setVisibility(8);
            } else {
                this.mCustomBodyView.setVisibility(8);
                this.mSeekbarToggleButton.setVisibility(8);
            }
            this.mMakeupButton.setImageResource(C0905R.C0906drawable.ic_ts_makeup_off_new);
            this.isMakeUp = false;
            this.mIsUpdateMakeupStatus = true;
        }
    }

    public void openBokeh() {
        this.isBokehMode = true;
        removeFilterMenu();
        if (this.mModule.isBackCamera()) {
            this.mSettingsManager.setBokehMode(true);
            this.mModule.restartAll();
        } else if (!this.mCameraControls.getVideoMode()) {
            this.mModule.restartSession(true);
        }
        initializeBokehMode(true);
        this.mBokehImageView.setImageResource(C0905R.C0906drawable.ic_bokehs_on);
        this.mVideoButton.setVisibility(0);
        if (!this.mCameraControls.getVideoMode()) {
            updateMenuStatus(false);
        }
        updateMenus();
    }

    public void openBokehWithoutRestartSession() {
        Log.d(TAG, "openBokehWithoutRestartSession");
        this.isBokehMode = true;
        removeFilterMenu();
        if (this.mModule.isBackCamera()) {
            this.mSettingsManager.setBokehMode(true);
            this.mModule.restartAll();
        }
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                CaptureUI.this.initializeBokehMode(true);
                CaptureUI.this.mBokehImageView.setImageResource(C0905R.C0906drawable.ic_bokehs_on);
                CaptureUI.this.mVideoButton.setVisibility(0);
                if (!CaptureUI.this.mCameraControls.getVideoMode()) {
                    CaptureUI.this.updateMenuStatus(false);
                }
                CaptureUI.this.updateMenus();
            }
        });
    }

    public void closeBokeh() {
        this.isBokehMode = false;
        removeFilterMenu();
        if (this.mModule.isBackCamera()) {
            this.mSettingsManager.setBokehMode(false);
            if (!this.isFrontBackSwitcherOn) {
                this.mModule.restartAll();
            }
        } else {
            this.mModule.restartSession(true);
        }
        initializeBokehMode(false);
        this.mBokehImageView.setImageResource(C0905R.C0906drawable.ic_bokehs_off);
        if (!this.mCameraControls.getVideoMode()) {
            updateMenuStatus(true);
        }
        updateMenus();
    }

    private void enableView(View view, String str) {
        Values values = (Values) this.mSettingsManager.getValuesMap().get(str);
        if (values != null) {
            view.setEnabled(values.overriddenValue == null);
        }
    }

    public void showTimeLapseUI(boolean z) {
        View view = this.mTimeLapseLabel;
        if (view != null) {
            view.setVisibility(z ? 0 : 8);
        }
    }

    public void showRecordingUI(boolean z, boolean z2) {
        if (z) {
            if (z2) {
                this.mFlashButton.setVisibility(8);
            } else {
                this.mFlashButton.init(true);
            }
            this.mVideoButton.setImageResource(C0905R.C0906drawable.shutter_button_video_stop_new);
            this.mRecordingTimeView.setText(BuildConfig.FLAVOR);
            this.mRecordingTimeRect.setVisibility(0);
            this.mMuteButton.setVisibility(0);
            setMuteButtonResource(true ^ this.mModule.isAudioMute());
            return;
        }
        this.mFlashButton.setVisibility(0);
        this.mFlashButton.init(false);
        this.mVideoButton.setImageResource(C0905R.C0906drawable.ic_switch_video_new);
        this.mRecordingTimeRect.setVisibility(8);
        this.mMuteButton.setVisibility(4);
    }

    /* access modifiers changed from: private */
    public void setMuteButtonResource(boolean z) {
        if (z) {
            this.mMuteButton.setImageResource(C0905R.C0906drawable.ic_unmuted_button_new);
        } else {
            this.mMuteButton.setImageResource(C0905R.C0906drawable.ic_muted_button_new);
        }
    }

    private boolean needShowInstructional() {
        AlertDialog alertDialog = this.mSceneModeInstructionalDialog;
        if (alertDialog == null || !alertDialog.isShowing()) {
            CameraActivity cameraActivity = this.mActivity;
            SharedPreferences sharedPreferences = cameraActivity.getSharedPreferences(ComboPreferences.getGlobalSharedPreferencesName(cameraActivity), 0);
            int valueIndex = this.mSettingsManager.getValueIndex(SettingsManager.KEY_SCENE_MODE);
            if (valueIndex >= 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("pref_camera2_scenemode_key_");
                sb.append(valueIndex);
                sharedPreferences.getBoolean(sb.toString(), false);
            }
            return false;
        }
        Log.d(TAG, "DismissSceneInstructionalDialog");
        this.mSceneModeInstructionalDialog.dismiss();
        this.mSceneModeInstructionalDialog = null;
        return false;
    }

    private void showSceneInstructionalDialog(int i) {
        Log.d(TAG, "showSceneInstructionalDialog");
        View inflate = ((LayoutInflater) this.mActivity.getSystemService("layout_inflater")).inflate((i == 90 || i == 270) ? C0905R.layout.scene_mode_instructional_landscape : C0905R.layout.scene_mode_instructional, null);
        SettingsManager settingsManager = this.mSettingsManager;
        String str = SettingsManager.KEY_SCENE_MODE;
        final int valueIndex = settingsManager.getValueIndex(str);
        ((TextView) inflate.findViewById(C0905R.C0907id.scene_mode_name)).setText(this.mSettingsManager.getEntries(str)[valueIndex]);
        ImageView imageView = (ImageView) inflate.findViewById(C0905R.C0907id.scene_mode_icon);
        SettingsManager settingsManager2 = this.mSettingsManager;
        String str2 = SettingsManager.KEY_SCEND_MODE_INSTRUCTIONAL;
        imageView.setImageResource(settingsManager2.getResource(str2, 0)[valueIndex]);
        TextView textView = (TextView) inflate.findViewById(C0905R.C0907id.scene_mode_instructional);
        CharSequence[] entries = this.mSettingsManager.getEntries(str2);
        if (entries[valueIndex].length() != 0) {
            textView.setText(entries[valueIndex]);
            final CheckBox checkBox = (CheckBox) inflate.findViewById(C0905R.C0907id.remember_selected);
            ((Button) inflate.findViewById(C0905R.C0907id.scene_mode_instructional_ok)).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (checkBox.isChecked()) {
                        SharedPreferences sharedPreferences = CaptureUI.this.mActivity.getSharedPreferences(ComboPreferences.getGlobalSharedPreferencesName(CaptureUI.this.mActivity), 0);
                        StringBuilder sb = new StringBuilder();
                        sb.append("pref_camera2_scenemode_key_");
                        sb.append(valueIndex);
                        String sb2 = sb.toString();
                        Editor edit = sharedPreferences.edit();
                        edit.putBoolean(sb2, true);
                        edit.commit();
                    }
                    CaptureUI.this.mSceneModeInstructionalDialog.dismiss();
                    CaptureUI.this.mSceneModeInstructionalDialog = null;
                }
            });
            this.mSceneModeInstructionalDialog = new Builder(this.mActivity, 3).setView(inflate).create();
            try {
                this.mSceneModeInstructionalDialog.show();
                if (i != 0) {
                    rotationSceneModeInstructionalDialog(inflate, i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int i = displayMetrics.widthPixels;
        int i2 = displayMetrics.heightPixels;
        return i < i2 ? i : i2;
    }

    private void rotationSceneModeInstructionalDialog(View view, int i) {
        view.setRotation((float) (-i));
        int screenWidth = (getScreenWidth() * 9) / 10;
        Window window = this.mSceneModeInstructionalDialog.getWindow();
        LayoutParams attributes = window.getAttributes();
        window.setGravity(17);
        attributes.height = screenWidth;
        attributes.width = screenWidth;
        window.setAttributes(attributes);
        ((RelativeLayout) view.findViewById(C0905R.C0907id.mode_layout_rect)).setLayoutParams(new FrameLayout.LayoutParams(screenWidth, screenWidth));
    }

    private void showSceneModeLabel() {
        this.mIsSceneModeLabelClose = false;
        SettingsManager settingsManager = this.mSettingsManager;
        String str = SettingsManager.KEY_SCENE_MODE;
        int valueIndex = settingsManager.getValueIndex(str);
        CharSequence[] entries = this.mSettingsManager.getEntries(str);
        if (valueIndex <= 0 || valueIndex >= entries.length) {
            this.mSceneModeLabelRect.setVisibility(8);
            this.mExitBestMode.setVisibility(8);
            return;
        }
        this.mSceneModeName.setText(entries[valueIndex]);
        this.mSceneModeLabelRect.setVisibility(0);
        this.mExitBestMode.setVisibility(0);
    }

    public void resetTrackingFocus() {
        if (this.mModule.isTrackingFocusSettingOn()) {
            this.mTrackingFocusRenderer.setVisible(false);
            this.mTrackingFocusRenderer.setVisible(true);
        }
    }

    public void hideUIwhileRecording() {
        this.mCameraControls.setVideoMode(true);
        this.mSceneModeLabelRect.setVisibility(4);
        this.mFrontBackSwitcher.setVisibility(4);
        this.mFilterModeSwitcher.setVisibility(4);
        this.mSceneModeSwitcher.setVisibility(4);
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_MAKEUP);
        if (value != null && value.equals("0")) {
            this.mMakeupButton.setVisibility(8);
        }
        this.mBokehSwitcher.setVisibility(8);
        this.mButton.setVisibility(8);
        this.mIsVideoUI = true;
        this.mPauseButton.setVisibility(0);
    }

    public void showUIafterRecording() {
        this.mCameraControls.setVideoMode(false);
        this.mFrontBackSwitcher.setVisibility(0);
        this.mFilterModeSwitcher.setVisibility(0);
        this.mSceneModeSwitcher.setVisibility(0);
        this.mMakeupButton.setVisibility(0);
        this.mBokehSwitcher.setVisibility(0);
        this.mButton.setVisibility(0);
        this.mIsVideoUI = false;
        this.mPauseButton.setVisibility(4);
        showSceneModeLabel();
    }

    public void addFilterMode() {
        int i;
        SettingsManager settingsManager = this.mSettingsManager;
        String str = SettingsManager.KEY_COLOR_EFFECT;
        if (settingsManager.getValue(str) != null) {
            int displayRotation = CameraUtil.getDisplayRotation(this.mActivity);
            if (!CameraUtil.isDefaultToPortrait(this.mActivity)) {
                displayRotation = (displayRotation + 90) % 360;
            }
            Display defaultDisplay = ((WindowManager) this.mActivity.getSystemService("window")).getDefaultDisplay();
            CharSequence[] entries = this.mSettingsManager.getEntries(str);
            Resources resources = this.mActivity.getResources();
            int dimension = (int) (resources.getDimension(C0905R.dimen.filter_mode_height) + (resources.getDimension(C0905R.dimen.filter_mode_padding) * 2.0f) + 1.0f);
            int dimension2 = (int) (resources.getDimension(C0905R.dimen.filter_mode_width) + (resources.getDimension(C0905R.dimen.filter_mode_padding) * 2.0f) + 1.0f);
            boolean z = displayRotation == 0 || displayRotation == 180;
            if (!z) {
                dimension = dimension2;
                i = C0905R.layout.vertical_grid;
            } else {
                i = 2131361810;
            }
            int[] resource = this.mSettingsManager.getResource(str, 0);
            LayoutInflater layoutInflater = (LayoutInflater) this.mActivity.getSystemService("layout_inflater");
            FrameLayout frameLayout = (FrameLayout) layoutInflater.inflate(i, null, false);
            frameLayout.setBackgroundColor(17170445);
            removeFilterMenu(false);
            this.mFilterMenuStatus = 2;
            this.mFilterLayout = new LinearLayout(this.mActivity);
            if (!z) {
                this.mFilterLayout.setLayoutParams(new ViewGroup.LayoutParams(dimension, -1));
                ((ViewGroup) this.mRootView).addView(this.mFilterLayout);
            } else {
                this.mFilterLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, dimension));
                ((ViewGroup) this.mRootView).addView(this.mFilterLayout);
                this.mFilterLayout.setY((float) (defaultDisplay.getHeight() - (dimension * 2)));
            }
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            LinearLayout linearLayout = (LinearLayout) frameLayout.findViewById(C0905R.C0907id.layout);
            final View[] viewArr = new View[entries.length];
            int valueIndex = this.mSettingsManager.getValueIndex(str);
            for (final int i2 = 0; i2 < entries.length; i2++) {
                RotateLayout rotateLayout = (RotateLayout) layoutInflater.inflate(C0905R.layout.filter_mode_view, null, false);
                ImageView imageView = (ImageView) rotateLayout.findViewById(C0905R.C0907id.image);
                rotateLayout.setOnTouchListener(new OnTouchListener() {
                    private long startTime;

                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == 0) {
                            this.startTime = System.currentTimeMillis();
                        } else if (motionEvent.getAction() == 1 && System.currentTimeMillis() - this.startTime < 200) {
                            CaptureUI.this.mSettingsManager.setValueIndex(SettingsManager.KEY_COLOR_EFFECT, i2);
                            for (View background : viewArr) {
                                background.setBackground(null);
                            }
                            ((ImageView) view.findViewById(C0905R.C0907id.image)).setBackgroundColor(CaptureUI.HIGHLIGHT_COLOR);
                        }
                        return true;
                    }
                });
                viewArr[i2] = imageView;
                if (i2 == valueIndex) {
                    imageView.setBackgroundColor(HIGHLIGHT_COLOR);
                }
                TextView textView = (TextView) rotateLayout.findViewById(C0905R.C0907id.label);
                imageView.setImageResource(resource[i2]);
                textView.setText(entries[i2]);
                linearLayout.addView(rotateLayout);
            }
            this.mFilterLayout.addView(frameLayout);
        }
    }

    public void removeAndCleanUpFilterMenu() {
        removeFilterMenu(false);
        cleanUpMenus();
    }

    public void animateFadeIn(View view) {
        ViewPropertyAnimator animate = view.animate();
        animate.alpha(0.85f).setDuration(300);
        animate.start();
    }

    private void animateSlideOut(View view) {
        if (view != null && this.mFilterMenuStatus != 1) {
            this.mFilterMenuStatus = 1;
            ViewPropertyAnimator animate = view.animate();
            if (1 == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
                animate.translationXBy((float) view.getWidth()).setDuration(300);
            } else {
                animate.translationXBy((float) (-view.getWidth())).setDuration(300);
            }
            animate.setListener(new AnimatorListener() {
                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    CaptureUI.this.removeAndCleanUpFilterMenu();
                }

                public void onAnimationCancel(Animator animator) {
                    CaptureUI.this.removeAndCleanUpFilterMenu();
                }
            });
            animate.start();
        }
    }

    public void animateSlideIn(View view, int i, boolean z) {
        int orientation = getOrientation();
        if (!z) {
            orientation = 0;
        }
        ViewPropertyAnimator animate = view.animate();
        if (1 == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
            if (orientation == 0) {
                float x = view.getX();
                view.setX(-(x - ((float) i)));
                animate.translationX(x);
            } else if (orientation == 90) {
                float y = view.getY();
                view.setY(-(((float) i) + y));
                animate.translationY(y);
            } else if (orientation == 180) {
                float x2 = view.getX();
                view.setX(-(((float) i) + x2));
                animate.translationX(x2);
            } else if (orientation == 270) {
                float y2 = view.getY();
                view.setY(-(y2 - ((float) i)));
                animate.translationY(y2);
            }
        } else if (orientation == 0) {
            float x3 = view.getX();
            view.setX(x3 - ((float) i));
            animate.translationX(x3);
        } else if (orientation == 90) {
            float y3 = view.getY();
            view.setY(((float) i) + y3);
            animate.translationY(y3);
        } else if (orientation == 180) {
            float x4 = view.getX();
            view.setX(((float) i) + x4);
            animate.translationX(x4);
        } else if (orientation == 270) {
            float y4 = view.getY();
            view.setY(y4 - ((float) i));
            animate.translationY(y4);
        }
        animate.setDuration(300).start();
    }

    public void hideUIWhileCountDown() {
        hideCameraControls(true);
        this.mGestures.setZoomOnly(true);
    }

    public void showUIAfterCountDown() {
        hideCameraControls(false);
        this.mGestures.setZoomOnly(false);
    }

    public void hideCameraControls(boolean z) {
        int i = 4;
        int i2 = z ? 4 : 0;
        FlashToggleButton flashToggleButton = this.mFlashButton;
        if (flashToggleButton != null) {
            if (!z && this.mSettingsManager.isFlashSupported(this.mModule.getMainCameraId())) {
                i = 0;
            }
            flashToggleButton.setVisibility(i);
            if (this.mSettingsManager.getCurrentMode().equals("HDR")) {
                this.mFlashButton.setVisibility(i2);
                this.mFlashButton.setEnabled(false);
            }
        }
        View view = this.mFrontBackSwitcher;
        if (view != null) {
            view.setVisibility(i2);
        }
        View view2 = this.mSceneModeSwitcher;
        if (view2 != null) {
            view2.setVisibility(i2);
        }
        View view3 = this.mFilterModeSwitcher;
        if (view3 != null) {
            view3.setVisibility(i2);
        }
        View view4 = this.mFilterModeSwitcher;
        if (view4 != null) {
            view4.setVisibility(i2);
        }
        ImageView imageView = this.mMakeupButton;
        if (imageView != null) {
            imageView.setVisibility(i2);
        }
        View view5 = this.mBokehSwitcher;
        if (view5 != null) {
            view5.setVisibility(i2);
        }
        RotateImageView rotateImageView = this.mButton;
        if (rotateImageView != null) {
            rotateImageView.setVisibility(i2);
        }
        ImageView imageView2 = this.mThumbnail;
        if (imageView2 != null) {
            imageView2.setVisibility(i2);
        }
        ImageView imageView3 = this.mVideoButton;
        if (imageView3 != null) {
            imageView3.setVisibility(i2);
        }
    }

    public void initializeControlByIntent() {
        this.mThumbnail = (ImageView) this.mRootView.findViewById(C0905R.C0907id.preview_thumb);
        this.mThumbnail.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (!CameraControls.isAnimating() && !CaptureUI.this.mModule.isTakingPicture() && !CaptureUI.this.mModule.isRecordingVideo() && CaptureUI.this.isMediaSaved()) {
                    CaptureUI.this.setMediaSaved(true);
                    CaptureUI.this.mActivity.gotoGallery();
                }
            }
        });
        if (this.mModule.getCurrentIntentMode() != 0) {
            this.mCameraControls.setIntentMode(this.mModule.getCurrentIntentMode());
        }
    }

    public boolean isMediaSaved() {
        return this.mIsMediaSaved;
    }

    public void setMediaSaved(boolean z) {
        this.mIsMediaSaved = z;
    }

    public void doShutterAnimation() {
        AnimationDrawable animationDrawable = (AnimationDrawable) this.mShutterButton.getDrawable();
        animationDrawable.stop();
        animationDrawable.start();
    }

    public void showUI() {
        if (this.mUIhidden) {
            this.mUIhidden = false;
            this.mPieRenderer.setBlockFocus(false);
            this.mCameraControls.showUI();
        }
    }

    public void hideUI() {
        if (!this.mUIhidden) {
            this.mUIhidden = true;
            this.mPieRenderer.setBlockFocus(true);
            this.mCameraControls.hideUI();
        }
    }

    public void cleanUpMenus() {
        showUI();
        updateMenus();
        this.mActivity.setSystemBarsVisibility(false);
    }

    public void updateMenus() {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_MAKEUP);
        int valueIndex = this.mSettingsManager.getValueIndex(SettingsManager.KEY_COLOR_EFFECT);
        String value2 = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
        String str = "0";
        boolean z5 = true;
        boolean z6 = false;
        if (value == null || value.equals(str)) {
            if ((valueIndex == 0 && this.mFilterMenuStatus != 2) || this.isBokehMode) {
                if (value2 != null && !value2.equals(str)) {
                    boolean z7 = !value2.equals(Integer.toString(SettingsManager.SCENE_MODE_PROMODE_INT)) && !value2.equals(Integer.toString(18));
                    z = value2.equals(String.valueOf(SettingsManager.SCENE_MODE_PROMODE_INT));
                    z2 = true;
                    z5 = z7;
                    z3 = false;
                } else if (this.isBokehMode) {
                    z5 = true ^ this.mModule.isBackCamera();
                    z4 = false;
                    z2 = z3;
                    z = z2;
                }
            }
            z4 = true;
            z2 = z3;
            z = z2;
        } else {
            if (!this.isBokehMode) {
                z3 = true;
            } else if (this.mModule.isBackCamera()) {
                z3 = false;
                z2 = false;
                z5 = false;
                z = false;
                if (value2 != null && !value2.equals(str)) {
                    z3 = false;
                    z = false;
                }
            } else {
                z3 = false;
            }
            z2 = z3;
            z = z2;
            z3 = false;
            z = false;
        }
        if (!this.mModule.isSceneMode() || value2.equals(String.valueOf(SettingsManager.SCENE_MODE_PROMODE_INT))) {
            z6 = z;
        } else {
            this.mFlashButton.updateFlashStatus(0);
        }
        this.mFlashButton.setEnabled(z6);
        this.mMakeupButton.setEnabled(z5);
        BeautificationFilter.isSupportedStatic();
        this.mFilterModeSwitcher.setEnabled(z3);
        this.mSceneModeSwitcher.setEnabled(z2);
    }

    public void updateFlashUi(boolean z) {
        this.mFlashButton.setEnabled(z);
    }

    public boolean arePreviewControlsVisible() {
        return !this.mUIhidden;
    }

    public void enableShutter(boolean z) {
        ShutterButton shutterButton = this.mShutterButton;
        if (shutterButton != null) {
            shutterButton.setEnabled(z);
        }
    }

    public boolean isShutterEnabled() {
        return this.mShutterButton.isEnabled();
    }

    public void enableVideo(boolean z) {
        ImageView imageView = this.mVideoButton;
        if (imageView != null) {
            imageView.setEnabled(z);
        }
    }

    private boolean handleBackKeyOnMenu() {
        if (this.mFilterMenuStatus != 2) {
            return false;
        }
        removeFilterMenu(true);
        return true;
    }

    public boolean onBackPressed() {
        if (handleBackKeyOnMenu()) {
            return true;
        }
        PieRenderer pieRenderer = this.mPieRenderer;
        if (pieRenderer != null && pieRenderer.showsItems()) {
            this.mPieRenderer.hide();
            return true;
        } else if (!this.mModule.isCameraIdle()) {
            return true;
        } else {
            return false;
        }
    }

    public SurfaceHolder getSurfaceHolder() {
        return this.mSurfaceHolder;
    }

    public Surface getMonoDummySurface() {
        if (this.mMonoDummyAllocation == null) {
            RenderScript create = RenderScript.create(this.mActivity);
            Type.Builder builder = new Type.Builder(create, Element.YUV(create));
            builder.setX(this.mPreviewWidth);
            builder.setY(this.mPreviewHeight);
            builder.setYuvFormat(35);
            this.mMonoDummyAllocation = Allocation.createTyped(create, builder.create(), 33);
            ScriptIntrinsicYuvToRGB create2 = ScriptIntrinsicYuvToRGB.create(create, Element.RGBA_8888(create));
            create2.setInput(this.mMonoDummyAllocation);
            if (this.mSettingsManager.getValue(SettingsManager.KEY_MONO_PREVIEW).equalsIgnoreCase(RecordLocationPreference.VALUE_ON)) {
                Type.Builder builder2 = new Type.Builder(create, Element.RGBA_8888(create));
                builder2.setX(this.mPreviewWidth);
                builder2.setY(this.mPreviewHeight);
                this.mMonoDummyOutputAllocation = Allocation.createTyped(create, builder2.create(), 65);
                this.mMonoDummyOutputAllocation.setSurface(this.mSurfaceHolderMono.getSurface());
                this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        SurfaceHolder access$000 = CaptureUI.this.mSurfaceHolderMono;
                        CaptureUI captureUI = CaptureUI.this;
                        access$000.setFixedSize(captureUI.mPreviewWidth, captureUI.mPreviewHeight);
                        CaptureUI.this.mSurfaceViewMono.setVisibility(0);
                    }
                });
            }
            this.mMonoDummyAllocation.setOnBufferAvailableListener(new MonoDummyListener(create2));
            this.mIsMonoDummyAllocationEverUsed = false;
        }
        return this.mMonoDummyAllocation.getSurface();
    }

    public void showPreviewCover() {
        this.mPreviewCover.setVisibility(0);
    }

    public void hidePreviewCover() {
        if (this.mPreviewCover.getVisibility() != 8) {
            this.mPreviewCover.setVisibility(8);
        }
    }

    private void initializeCountDown() {
        this.mActivity.getLayoutInflater().inflate(C0905R.layout.count_down_to_capture, (ViewGroup) this.mRootView, true);
        this.mCountDownView = (CountDownView) this.mRootView.findViewById(C0905R.C0907id.count_down_to_capture);
        this.mCountDownView.setCountDownFinishedListener(this.mModule);
        this.mCountDownView.bringToFront();
        this.mCountDownView.setOrientation(this.mOrientation);
    }

    public boolean isCountingDown() {
        CountDownView countDownView = this.mCountDownView;
        return countDownView != null && countDownView.isCountingDown();
    }

    public void cancelCountDown() {
        CountDownView countDownView = this.mCountDownView;
        if (countDownView != null) {
            countDownView.cancelCountDown();
            showUIAfterCountDown();
        }
    }

    public void initCountDownView() {
        if (this.mCountDownView == null) {
            initializeCountDown();
        }
    }

    public void releaseSoundPool() {
        CountDownView countDownView = this.mCountDownView;
        if (countDownView != null) {
            countDownView.releaseSoundPool();
            this.mCountDownView = null;
        }
    }

    public void startCountDown(int i, boolean z) {
        this.mCountDownView.startCountDown(i, z);
        hideUIWhileCountDown();
    }

    public void onPause() {
        cancelCountDown();
        collapseCameraControls();
        Camera2FaceView camera2FaceView = this.mFaceView;
        if (camera2FaceView != null) {
            camera2FaceView.clear();
        }
        TrackingFocusRenderer trackingFocusRenderer = this.mTrackingFocusRenderer;
        if (trackingFocusRenderer != null) {
            trackingFocusRenderer.setVisible(false);
        }
        Allocation allocation = this.mMonoDummyAllocation;
        if (allocation != null && this.mIsMonoDummyAllocationEverUsed) {
            allocation.setOnBufferAvailableListener(null);
            this.mMonoDummyAllocation.destroy();
            this.mMonoDummyAllocation = null;
        }
        Allocation allocation2 = this.mMonoDummyOutputAllocation;
        if (allocation2 != null && this.mIsMonoDummyAllocationEverUsed) {
            allocation2.destroy();
            this.mMonoDummyOutputAllocation = null;
        }
        this.mSurfaceViewMono.setVisibility(8);
    }

    public boolean collapseCameraControls() {
        this.mCameraControls.showRefocusToast(false);
        return false;
    }

    public void showRefocusToast(boolean z) {
        this.mCameraControls.showRefocusToast(z);
    }

    private FocusIndicator getFocusIndicator() {
        FocusIndicator focusIndicator;
        if (this.mModule.isTrackingFocusSettingOn()) {
            PieRenderer pieRenderer = this.mPieRenderer;
            if (pieRenderer != null) {
                pieRenderer.clear();
            }
            return this.mTrackingFocusRenderer;
        }
        Camera2FaceView camera2FaceView = this.mFaceView;
        if (camera2FaceView == null || !camera2FaceView.faceExists() || this.mIsTouchAF) {
            focusIndicator = this.mPieRenderer;
        } else {
            PieRenderer pieRenderer2 = this.mPieRenderer;
            if (pieRenderer2 != null) {
                pieRenderer2.clear();
            }
            focusIndicator = this.mFaceView;
        }
        return focusIndicator;
    }

    public boolean hasFaces() {
        Camera2FaceView camera2FaceView = this.mFaceView;
        return camera2FaceView != null && camera2FaceView.faceExists();
    }

    public void clearFaces() {
        Camera2FaceView camera2FaceView = this.mFaceView;
        if (camera2FaceView != null) {
            camera2FaceView.clear();
        }
    }

    public void clearFocus() {
        FocusIndicator focusIndicator = getFocusIndicator();
        if (focusIndicator != null) {
            focusIndicator.clear();
        }
        this.mIsTouchAF = false;
    }

    public void setFocusPosition(int i, int i2) {
        int[] iArr = new int[2];
        this.mSurfaceView.getLocationInWindow(iArr);
        int i3 = iArr[0];
        int i4 = iArr[1];
        if (i < 216) {
            i = 216;
        } else if ((this.mSurfaceView.getWidth() + i3) - 216 < i && i < this.mSurfaceView.getWidth() + i3) {
            i = (i3 + this.mSurfaceView.getWidth()) - 216;
        }
        if (i4 < i2) {
            int i5 = i4 + 216;
            if (i2 < i5) {
                i2 = i5;
                this.mPieRenderer.setFocus(i, i2);
                this.mIsTouchAF = true;
            }
        }
        if ((this.mSurfaceView.getHeight() + i4) - 216 < i2 && i2 < this.mSurfaceView.getHeight() + i4) {
            i2 = (i4 + this.mSurfaceView.getHeight()) - 216;
        }
        this.mPieRenderer.setFocus(i, i2);
        this.mIsTouchAF = true;
    }

    public void onFocusStarted() {
        FocusIndicator focusIndicator = getFocusIndicator();
        if (focusIndicator != null) {
            focusIndicator.showStart();
        }
    }

    public void onFocusSucceeded(boolean z) {
        FocusIndicator focusIndicator = getFocusIndicator();
        if (focusIndicator != null) {
            focusIndicator.showSuccess(z);
        }
    }

    public void onFocusFailed(boolean z) {
        FocusIndicator focusIndicator = getFocusIndicator();
        if (focusIndicator != null) {
            focusIndicator.showFail(z);
        }
    }

    public void onStartFaceDetection(int i, boolean z, Rect rect, Rect rect2) {
        this.mFaceView.setBlockDraw(false);
        this.mFaceView.clear();
        this.mFaceView.setVisibility(0);
        this.mFaceView.setDisplayOrientation(i);
        this.mFaceView.setMirror(z);
        this.mFaceView.setCameraBound(rect);
        this.mFaceView.setOriginalCameraBound(rect2);
        this.mFaceView.setZoom(this.mModule.getZoomValue());
        this.mFaceView.resume();
    }

    public void updateFaceViewCameraBound(Rect rect) {
        this.mFaceView.setCameraBound(rect);
        this.mFaceView.setZoom(this.mModule.getZoomValue());
    }

    public void onStopFaceDetection() {
        Camera2FaceView camera2FaceView = this.mFaceView;
        if (camera2FaceView != null) {
            camera2FaceView.setBlockDraw(true);
            this.mFaceView.clear();
        }
    }

    public void onFaceDetection(android.hardware.camera2.params.Face[] faceArr, ExtendedFace[] extendedFaceArr) {
        this.SCENE_DETECT_PEOPLE_NUMBER = faceArr.length;
        this.mFaceView.setFaces(faceArr, extendedFaceArr);
    }

    public Point getSurfaceViewSize() {
        Point point = new Point();
        AutoFitSurfaceView autoFitSurfaceView = this.mSurfaceView;
        if (autoFitSurfaceView != null) {
            point.set(autoFitSurfaceView.getWidth(), this.mSurfaceView.getHeight());
        }
        return point;
    }

    public void adjustOrientation() {
        setOrientation(this.mOrientation, true);
    }

    public void setOrientation(int i, boolean z) {
        this.mOrientation = i;
        this.mCameraControls.setOrientation(i, z);
        MenuHelp menuHelp = this.mMenuHelp;
        if (menuHelp != null) {
            menuHelp.setOrientation(i, z);
        }
        ViewGroup viewGroup = this.mFilterLayout;
        if (viewGroup != null) {
            ViewGroup viewGroup2 = (ViewGroup) viewGroup.getChildAt(0);
            if (viewGroup2 != null) {
                viewGroup2 = (ViewGroup) viewGroup2.getChildAt(0);
            }
            if (viewGroup2 != null) {
                for (int childCount = viewGroup2.getChildCount() - 1; childCount >= 0; childCount--) {
                    ((RotateLayout) viewGroup2.getChildAt(childCount)).setOrientation(i, z);
                }
            }
        }
        if (this.mRecordingTimeRect != null) {
            this.mRecordingTimeView.setRotation((float) (-i));
        }
        Camera2FaceView camera2FaceView = this.mFaceView;
        if (camera2FaceView != null) {
            camera2FaceView.setDisplayRotation(i);
        }
        CountDownView countDownView = this.mCountDownView;
        if (countDownView != null) {
            countDownView.setOrientation(i);
        }
        RotateTextToast.setOrientation(i);
        ZoomRenderer zoomRenderer = this.mZoomRenderer;
        if (zoomRenderer != null) {
            zoomRenderer.setOrientation(i);
        }
        if (this.mSceneModeLabelRect != null) {
            if (i == 180) {
                this.mSceneModeName.setRotation(180.0f);
                this.mSceneModeLabelCloseIcon.setRotation(180.0f);
                this.mSceneModeLabelRect.setOrientation(0, false);
            } else {
                this.mSceneModeName.setRotation(0.0f);
                this.mSceneModeLabelCloseIcon.setRotation(0.0f);
                this.mSceneModeLabelRect.setOrientation(i, false);
            }
        }
        updateBokehTipRectPosition(i);
        AlertDialog alertDialog = this.mSceneModeInstructionalDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            this.mSceneModeInstructionalDialog.dismiss();
            this.mSceneModeInstructionalDialog = null;
            showSceneInstructionalDialog(i);
        }
    }

    public void updateBokehTipRectPosition(int i) {
        if (this.mBokehTipRect != null) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
            if (i == 270) {
                this.mBokehTipRect.setRotation((float) (i + 180));
                layoutParams.gravity = 5;
                layoutParams.topMargin = this.mSurfaceView.getWidth() - 100;
                if ((this.isMakeUp && !this.isBokehMode) || (!this.isMakeUp && this.isBokehMode)) {
                    layoutParams.rightMargin = 75;
                }
                if (this.mModule.getMainCameraId() == 3) {
                    layoutParams.rightMargin = 40;
                }
                this.mBokehTipRect.setLayoutParams(layoutParams);
            } else if (i == 90) {
                this.mBokehTipRect.setRotation((float) (i + 180));
                layoutParams.gravity = GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
                layoutParams.topMargin = this.mSurfaceView.getWidth() - 100;
                if ((this.isMakeUp && !this.isBokehMode) || (!this.isMakeUp && this.isBokehMode)) {
                    layoutParams.leftMargin = 75;
                }
                if (this.mModule.getMainCameraId() == 3) {
                    layoutParams.leftMargin = 40;
                }
                this.mBokehTipRect.setLayoutParams(layoutParams);
            } else if (i == 0 || i == 180) {
                this.mBokehTipRect.setRotation((float) i);
                layoutParams.topMargin = 240;
                layoutParams.leftMargin = 0;
                layoutParams.rightMargin = 0;
                layoutParams.gravity = 49;
                this.mBokehTipRect.setLayoutParams(layoutParams);
            }
        }
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public void showFirstTimeHelp() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mActivity);
        String str = CameraSettings.KEY_SHOW_MENU_HELP;
        if (!defaultSharedPreferences.getBoolean(str, false)) {
            showFirstTimeHelp(this.mTopMargin, this.mBottomMargin);
            Editor edit = defaultSharedPreferences.edit();
            edit.putBoolean(str, true);
            edit.apply();
        }
    }

    private void showFirstTimeHelp(int i, int i2) {
        this.mMenuHelp = (MenuHelp) this.mRootView.findViewById(C0905R.C0907id.menu_help);
        this.mMenuHelp.setForCamera2(true);
        this.mMenuHelp.setVisibility(0);
        this.mMenuHelp.setMargins(i, i2);
        this.mMenuHelp.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (CaptureUI.this.mMenuHelp != null) {
                    CaptureUI.this.mMenuHelp.setVisibility(8);
                    CaptureUI.this.mMenuHelp = null;
                }
            }
        });
    }

    public void onSingleTapUp(View view, int i, int i2) {
        this.mModule.onSingleTapUp(view, i, i2);
    }

    public boolean isOverControlRegion(int[] iArr) {
        return this.mCameraControls.isControlRegion(iArr[0], iArr[1]);
    }

    public boolean isOverSurfaceView(int[] iArr) {
        int i = iArr[0];
        int i2 = iArr[1];
        int[] iArr2 = new int[2];
        this.mSurfaceView.getLocationInWindow(iArr2);
        int i3 = iArr2[0];
        int i4 = iArr2[1];
        iArr[0] = i - i3;
        iArr[1] = i2 - i4;
        if (i <= i3 || i >= i3 + this.mSurfaceView.getWidth() || i2 <= i4 || i2 >= i4 + this.mSurfaceView.getHeight()) {
            return false;
        }
        return true;
    }

    public void onPreviewFocusChanged(boolean z) {
        if (z) {
            showUI();
        } else {
            hideUI();
        }
        Camera2FaceView camera2FaceView = this.mFaceView;
        if (camera2FaceView != null) {
            camera2FaceView.setBlockDraw(!z);
        }
        PreviewGestures previewGestures = this.mGestures;
        if (previewGestures != null) {
            previewGestures.setEnabled(z);
        }
        RenderOverlay renderOverlay = this.mRenderOverlay;
        if (renderOverlay != null) {
            renderOverlay.setVisibility(z ? 0 : 8);
        }
        PieRenderer pieRenderer = this.mPieRenderer;
        if (pieRenderer != null) {
            pieRenderer.setBlockFocus(!z);
        }
        if (!z) {
            CountDownView countDownView = this.mCountDownView;
            if (countDownView != null) {
                countDownView.cancelCountDown();
            }
        }
    }

    public boolean isShutterPressed() {
        return this.mShutterButton.isPressed();
    }

    public void pressShutterButton() {
        if (this.mShutterButton.isInTouchMode()) {
            this.mShutterButton.requestFocusFromTouch();
        } else {
            this.mShutterButton.requestFocus();
        }
        this.mShutterButton.setPressed(true);
    }

    public void setRecordingTime(String str) {
        this.mRecordingTimeView.setText(str);
    }

    public void setRecordingTimeTextColor(int i) {
        this.mRecordingTimeView.setTextColor(i);
    }

    public void resetPauseButton() {
        this.mRecordingTimeView.setCompoundDrawablesWithIntrinsicBounds(C0905R.C0906drawable.ic_recording_indicator, 0, 0, 0);
        this.mPauseButton.setPaused(false);
    }

    public void onButtonPause() {
        this.mRecordingTimeView.setCompoundDrawablesWithIntrinsicBounds(C0905R.C0906drawable.ic_pausing_indicator, 0, 0, 0);
        this.mModule.onButtonPause();
    }

    public void onButtonContinue() {
        this.mRecordingTimeView.setCompoundDrawablesWithIntrinsicBounds(C0905R.C0906drawable.ic_recording_indicator, 0, 0, 0);
        this.mModule.onButtonContinue();
    }

    public void onSettingsChanged(List<SettingState> list) {
        for (SettingState settingState : list) {
            String str = settingState.key;
            String str2 = SettingsManager.KEY_COLOR_EFFECT;
            if (str.equals(str2)) {
                enableView(this.mFilterModeSwitcher, str2);
            } else {
                String str3 = settingState.key;
                String str4 = SettingsManager.KEY_SCENE_MODE;
                if (str3.equals(str4)) {
                    String value = this.mSettingsManager.getValue(str4);
                    if (value.equals("104")) {
                        this.mSceneModeLabelRect.setVisibility(8);
                    } else {
                        if (needShowInstructional()) {
                            showSceneInstructionalDialog(this.mOrientation);
                        }
                        showSceneModeLabel();
                        if (this.isMakeUp && value.equals(String.valueOf(SettingsManager.SCENE_MODE_PROMODE_INT))) {
                            toggleMakeup();
                        }
                    }
                } else {
                    String str5 = settingState.key;
                    String str6 = SettingsManager.KEY_FLASH_MODE;
                    if (str5.equals(str6)) {
                        this.mFlashButton.updateFlashStatus(this.mSettingsManager.getValueIndex(str6));
                        enableView(this.mFlashButton, str6);
                    } else if (settingState.key.equals(SettingsManager.KEY_FOCUS_DISTANCE)) {
                        PieRenderer pieRenderer = this.mPieRenderer;
                        if (pieRenderer != null) {
                            pieRenderer.setVisible(false);
                        }
                    }
                }
            }
        }
    }

    public void startSelfieFlash() {
        if (this.mSelfieView == null) {
            this.mSelfieView = (SelfieFlashView) this.mRootView.findViewById(C0905R.C0907id.selfie_flash);
        }
        this.mSelfieView.bringToFront();
        this.mScreenBrightness = setScreenBrightness(1.0f);
    }

    public void stopSelfieFlash() {
        if (this.mSelfieView == null) {
            this.mSelfieView = (SelfieFlashView) this.mRootView.findViewById(C0905R.C0907id.selfie_flash);
        }
        this.mSelfieView.close();
        float f = this.mScreenBrightness;
        if (f != 0.0f) {
            setScreenBrightness(f);
        }
    }

    private float setScreenBrightness(float f) {
        Window window = this.mActivity.getWindow();
        LayoutParams attributes = window.getAttributes();
        float f2 = attributes.screenBrightness;
        attributes.screenBrightness = f;
        window.setAttributes(attributes);
        return f2;
    }

    public void hideSurfaceView() {
        this.mSurfaceView.setVisibility(4);
    }

    public void showSurfaceView() {
        Log.d(TAG, "showSurfaceView");
        this.mSurfaceView.getHolder().setFixedSize(this.mPreviewWidth, this.mPreviewHeight);
        this.mSurfaceView.setAspectRatio(this.mPreviewHeight, this.mPreviewWidth);
        this.mSurfaceView.setVisibility(0);
        this.mIsVideoUI = false;
    }

    public boolean setPreviewSize(int i, int i2) {
        StringBuilder sb = new StringBuilder();
        sb.append("setPreviewSize ");
        sb.append(i);
        sb.append(" ");
        sb.append(i2);
        Log.d(TAG, sb.toString());
        boolean z = (i == this.mPreviewWidth && i2 == this.mPreviewHeight) ? false : true;
        this.mPreviewWidth = i;
        this.mPreviewHeight = i2;
        if (z) {
            showSurfaceView();
        }
        return z;
    }

    public ImageView getVideoButton() {
        return this.mVideoButton;
    }

    public int getCurrentProMode() {
        return this.mCameraControls.getPromode();
    }

    public View getRootView() {
        return this.mRootView;
    }

    public boolean isVideoRecording() {
        return this.mModule.isRecordingVideo();
    }

    public boolean isSecureCamera() {
        return this.mActivity.isSecureCamera();
    }

    public void updateSceneDetectIcon(String str) {
        SceneTypeParam valueOf = SceneTypeParam.valueOf(str);
        StringBuilder sb = new StringBuilder();
        sb.append("mSceneTypeParam:");
        sb.append(valueOf);
        Log.v(TAG, sb.toString());
        switch (C063836.$SwitchMap$com$android$camera$CaptureUI$SceneTypeParam[valueOf.ordinal()]) {
            case 1:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(0);
                return;
            case 2:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(C0905R.C0906drawable.mode_detect_cat);
                return;
            case 3:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(C0905R.C0906drawable.mode_detect_dog);
                return;
            case 4:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(C0905R.C0906drawable.mode_detect_food);
                return;
            case 5:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(C0905R.C0906drawable.mode_detect_grassy);
                return;
            case 6:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(C0905R.C0906drawable.mode_detect_leaf);
                return;
            case 7:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(C0905R.C0906drawable.mode_detect_ocean);
                return;
            case 8:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(C0905R.C0906drawable.mode_detect_portrait);
                return;
            case 9:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(C0905R.C0906drawable.mode_detect_sky);
                return;
            case 10:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(C0905R.C0906drawable.mode_detect_snow);
                return;
            case 11:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(C0905R.C0906drawable.mode_detect_stage);
                return;
            case 12:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(C0905R.C0906drawable.mode_detect_sunset);
                return;
            case 13:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(C0905R.C0906drawable.mode_detect_flower);
                return;
            case 14:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(C0905R.C0906drawable.mode_detect_text);
                return;
            default:
                ((ImageView) this.mModeDetectSwitcher).setImageResource(0);
                return;
        }
    }

    public void setBokehTipVisibility(boolean z) {
        if (z) {
            this.mBokehTipRect.setVisibility(0);
            this.mBokehTipText.setVisibility(0);
            return;
        }
        this.mBokehTipRect.setVisibility(8);
        this.mBokehTipText.setVisibility(8);
    }

    public void setBokehTipTextTitle(int i) {
        updateBokehTipRectPosition(this.mOrientation);
        this.mBokehTipText.setText(i);
    }

    public void setCustomViewVisibility(boolean z) {
        String str = SettingsManager.KEY_MAKEUP_WHITEN_DEGREE;
        String str2 = SettingsManager.KEY_MAKEUP_CLEAN_DEGREE;
        String str3 = "0";
        if (z) {
            this.mSeekbarBody.setVisibility(8);
            this.mCustomBodyView.setVisibility(0);
            String value = this.mSettingsManager.getValue(str2);
            String value2 = this.mSettingsManager.getValue(str);
            if (!(value2 == null || value == null)) {
                this.mMakeupWhitenSeekBar.setProgress(Integer.parseInt(value2));
                this.mMakeupCleanSeekBar.setProgress(Integer.parseInt(value));
                this.mMakeupSeekBar.setProgress(Integer.parseInt(str3));
            }
            this.mSeekbarToggleButton.setY(60.0f);
            this.mSeekbarToggleButton.setImageResource(C0905R.C0906drawable.seekbar_show);
            this.mCustomViewVisibility = true;
            return;
        }
        this.mSeekbarBody.setVisibility(0);
        this.mCustomBodyView.setVisibility(8);
        this.mMakeupWhitenSeekBar.setProgress(Integer.parseInt(str3));
        this.mMakeupCleanSeekBar.setProgress(Integer.parseInt(str3));
        this.mSettingsManager.setValue(str2, str3);
        this.mSettingsManager.setValue(str, str3);
        this.mSeekbarToggleButton.setY(0.0f);
        this.mSeekbarToggleButton.setImageResource(C0905R.C0906drawable.seekbar_hide);
        this.mCustomViewVisibility = false;
    }

    public void showWaitingProgress(boolean z) {
        TextView textView = this.mWaitProcessText;
        if (textView != null) {
            textView.setVisibility(z ? 0 : 8);
        }
    }
}
