package com.android.camera;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.android.camera.CameraManager.CameraFaceDetectionCallback;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraPreference.OnPreferenceChangedListener;
import com.android.camera.PauseButton.OnPauseButtonListener;
import com.android.camera.PhotoUI.SurfaceTextureSizeChangedListener;
import com.android.camera.PreviewGestures.SingleTapListener;
import com.android.camera.p004ui.AbstractSettingPopup;
import com.android.camera.p004ui.CameraControls;
import com.android.camera.p004ui.CameraRootView;
import com.android.camera.p004ui.CameraRootView.MyDisplayListener;
import com.android.camera.p004ui.FaceView;
import com.android.camera.p004ui.ListSubMenu;
import com.android.camera.p004ui.ModuleSwitcher;
import com.android.camera.p004ui.PieRenderer;
import com.android.camera.p004ui.PieRenderer.PieListener;
import com.android.camera.p004ui.RenderOverlay;
import com.android.camera.p004ui.RotateImageView;
import com.android.camera.p004ui.RotateLayout;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.p004ui.ZoomRenderer;
import com.android.camera.p004ui.ZoomRenderer.OnZoomChangedListener;
import com.android.camera.util.CameraUtil;
import com.asus.scenedetectlib.BuildConfig;
import java.util.List;
import org.codeaurora.snapcam.C0905R;

public class VideoUI implements PieListener, SingleTapListener, MyDisplayListener, Callback, OnPauseButtonListener, CameraFaceDetectionCallback {
    private static final String TAG = "CAM_VideoUI";
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    private final AnimationManager mAnimationManager;
    /* access modifiers changed from: private */
    public float mAspectRatio = 1.3333334f;
    /* access modifiers changed from: private */
    public boolean mAspectRatioResize;
    private int mBottomMargin = 0;
    private CameraControls mCameraControls;
    /* access modifiers changed from: private */
    public VideoController mController;
    private FaceView mFaceView;
    private View mFlashOverlay;
    private PreviewGestures mGestures;
    private boolean mIsTimeLapse = false;
    private LinearLayout mLabelsLinearLayout;
    /* access modifiers changed from: private */
    public int mMaxPreviewHeight = 0;
    /* access modifiers changed from: private */
    public int mMaxPreviewWidth = 0;
    private View mMenuButton;
    private RotateLayout mMenuLayout;
    /* access modifiers changed from: private */
    public RotateImageView mMuteButton;
    private OnScreenIndicators mOnScreenIndicators;
    /* access modifiers changed from: private */
    public int mOrientation;
    /* access modifiers changed from: private */
    public boolean mOrientationResize;
    private PauseButton mPauseButton;
    /* access modifiers changed from: private */
    public PieRenderer mPieRenderer;
    private SettingsPopup mPopup;
    /* access modifiers changed from: private */
    public boolean mPrevOrientationResize;
    private View mPreviewCover;
    private LinearLayout mPreviewMenuLayout;
    private int mPreviewOrientation = -1;
    /* access modifiers changed from: private */
    public boolean mRecordingStarted = false;
    private RotateLayout mRecordingTimeRect;
    private TextView mRecordingTimeView;
    private RenderOverlay mRenderOverlay;
    private View mReviewCancelButton;
    private View mReviewDoneButton;
    private ImageView mReviewImage;
    private View mReviewPlayButton;
    /* access modifiers changed from: private */
    public View mRootView;
    private int mScreenRatio = 0;
    private ShutterButton mShutterButton;
    private RotateLayout mSubMenuLayout;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceTextureSizeChangedListener mSurfaceTextureSizeListener;
    private float mSurfaceTextureUncroppedHeight;
    private float mSurfaceTextureUncroppedWidth;
    private SurfaceView mSurfaceView = null;
    /* access modifiers changed from: private */
    public ModuleSwitcher mSwitcher;
    private ImageView mThumbnail;
    private View mTimeLapseLabel;
    private int mTopMargin = 0;
    private boolean mUIhidden = false;
    /* access modifiers changed from: private */
    public VideoMenu mVideoMenu;
    private int mZoomMax;
    /* access modifiers changed from: private */
    public List<Integer> mZoomRatios;
    /* access modifiers changed from: private */
    public ZoomRenderer mZoomRenderer;

    public enum SURFACE_STATUS {
        HIDE,
        SURFACE_VIEW
    }

    private class SettingsPopup extends PopupWindow {
        public SettingsPopup(View view) {
            super(-2, -2);
            setBackgroundDrawable(new ColorDrawable(0));
            setOutsideTouchable(true);
            setFocusable(true);
            view.setVisibility(0);
            setContentView(view);
            showAtLocation(VideoUI.this.mRootView, 17, 0, 0);
        }

        public void dismiss(boolean z) {
            super.dismiss();
            VideoUI.this.popupDismissed();
            VideoUI.this.showUI();
            VideoUI.this.mActivity.setSystemBarsVisibility(false);
        }

        public void dismiss() {
            dismiss(true);
        }
    }

    private class ZoomChangeListener implements OnZoomChangedListener {
        public void onZoomValueChanged(float f) {
        }

        private ZoomChangeListener() {
        }

        public void onZoomValueChanged(int i) {
            int onZoomChanged = VideoUI.this.mController.onZoomChanged(i);
            if (VideoUI.this.mZoomRenderer != null) {
                VideoUI.this.mZoomRenderer.setZoomValue(((Integer) VideoUI.this.mZoomRatios.get(onZoomChanged)).intValue());
            }
        }

        public void onZoomStart() {
            if (VideoUI.this.mPieRenderer != null) {
                if (!VideoUI.this.mRecordingStarted) {
                    VideoUI.this.mPieRenderer.hide();
                }
                VideoUI.this.mPieRenderer.setBlockFocus(true);
            }
        }

        public void onZoomEnd() {
            if (VideoUI.this.mPieRenderer != null) {
                VideoUI.this.mPieRenderer.setBlockFocus(false);
            }
        }
    }

    public void onPieMoved(int i, int i2) {
    }

    public void showPreviewBorder(boolean z) {
    }

    public void showPreviewCover() {
        this.mPreviewCover.setVisibility(0);
    }

    public void hidePreviewCover() {
        View view = this.mPreviewCover;
        if (view != null && view.getVisibility() != 8) {
            this.mPreviewCover.setVisibility(8);
        }
    }

    public boolean isPreviewCoverVisible() {
        View view = this.mPreviewCover;
        return view != null && view.getVisibility() == 0;
    }

    public synchronized void applySurfaceChange(SURFACE_STATUS surface_status) {
        if (surface_status == SURFACE_STATUS.HIDE) {
            this.mSurfaceView.setVisibility(8);
        } else {
            this.mSurfaceView.setVisibility(0);
        }
    }

    public VideoUI(CameraActivity cameraActivity, VideoController videoController, View view) {
        this.mActivity = cameraActivity;
        this.mController = videoController;
        this.mRootView = view;
        this.mActivity.getLayoutInflater().inflate(C0905R.layout.video_module, (ViewGroup) this.mRootView, true);
        this.mPreviewCover = this.mRootView.findViewById(C0905R.C0907id.preview_cover);
        this.mSurfaceView = (SurfaceView) this.mRootView.findViewById(C0905R.C0907id.mdp_preview_content);
        this.mSurfaceView.setVisibility(0);
        this.mSurfaceHolder = this.mSurfaceView.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mSurfaceHolder.setType(3);
        Log.v(TAG, "Using mdp_preview_content (MDP path)");
        this.mRootView.findViewById(C0905R.C0907id.preview_container).addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                int i9 = i3 - i;
                int i10 = i4 - i2;
                VideoUI.this.tryToCloseSubList();
                if (VideoUI.this.mMaxPreviewWidth == 0 && VideoUI.this.mMaxPreviewHeight == 0) {
                    VideoUI.this.mMaxPreviewWidth = i9;
                    VideoUI.this.mMaxPreviewHeight = i10;
                }
                int i11 = VideoUI.this.mActivity.getResources().getConfiguration().orientation;
                if ((i11 == 1 && i9 > i10) || (i11 == 2 && i9 < i10)) {
                    Log.d(VideoUI.TAG, "Swapping SurfaceView width & height dimensions");
                    if (!(VideoUI.this.mMaxPreviewWidth == 0 || VideoUI.this.mMaxPreviewHeight == 0)) {
                        int access$300 = VideoUI.this.mMaxPreviewWidth;
                        VideoUI videoUI = VideoUI.this;
                        videoUI.mMaxPreviewWidth = videoUI.mMaxPreviewHeight;
                        VideoUI.this.mMaxPreviewHeight = access$300;
                    }
                }
                if (VideoUI.this.mOrientationResize != VideoUI.this.mPrevOrientationResize || VideoUI.this.mAspectRatioResize) {
                    VideoUI videoUI2 = VideoUI.this;
                    videoUI2.layoutPreview(videoUI2.mAspectRatio);
                    VideoUI.this.mAspectRatioResize = false;
                }
            }
        });
        this.mFlashOverlay = this.mRootView.findViewById(C0905R.C0907id.flash_overlay);
        this.mShutterButton = (ShutterButton) this.mRootView.findViewById(C0905R.C0907id.shutter_button);
        this.mSwitcher = (ModuleSwitcher) this.mRootView.findViewById(C0905R.C0907id.camera_switcher);
        this.mSwitcher.setCurrentIndex(1);
        this.mSwitcher.setSwitchListener(this.mActivity);
        this.mSwitcher.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                VideoUI.this.mSwitcher.showPopup();
                VideoUI.this.mSwitcher.setOrientation(VideoUI.this.mOrientation, false);
            }
        });
        this.mMuteButton = (RotateImageView) this.mRootView.findViewById(C0905R.C0907id.mute_button);
        this.mMuteButton.setVisibility(0);
        if (!((VideoModule) this.mController).isAudioMute()) {
            this.mMuteButton.setImageResource(C0905R.C0906drawable.ic_unmuted_button);
        } else {
            this.mMuteButton.setImageResource(C0905R.C0906drawable.ic_muted_button);
        }
        this.mMuteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                boolean z = !((VideoModule) VideoUI.this.mController).isAudioMute();
                ((VideoModule) VideoUI.this.mController).setMute(z, true);
                if (!z) {
                    VideoUI.this.mMuteButton.setImageResource(C0905R.C0906drawable.ic_unmuted_button);
                } else {
                    VideoUI.this.mMuteButton.setImageResource(C0905R.C0906drawable.ic_muted_button);
                }
            }
        });
        initializeMiscControls();
        initializeControlByIntent();
        initializeOverlay();
        initializePauseButton();
        this.mCameraControls = (CameraControls) this.mRootView.findViewById(C0905R.C0907id.camera_controls);
        ViewStub viewStub = (ViewStub) this.mRootView.findViewById(C0905R.C0907id.face_view_stub);
        if (viewStub != null) {
            viewStub.inflate();
            this.mFaceView = (FaceView) this.mRootView.findViewById(C0905R.C0907id.face_view);
            setSurfaceTextureSizeChangedListener(this.mFaceView);
        }
        this.mAnimationManager = new AnimationManager();
        this.mOrientationResize = false;
        this.mPrevOrientationResize = false;
        Point point = new Point();
        this.mActivity.getWindowManager().getDefaultDisplay().getSize(point);
        this.mScreenRatio = CameraUtil.determineRatio(point.x, point.y);
        calculateMargins(point);
        this.mCameraControls.setMargins(this.mTopMargin, this.mBottomMargin);
        ((ViewGroup) this.mRootView).removeView(this.mRecordingTimeRect);
    }

    private void calculateMargins(Point point) {
        int i = point.x;
        int i2 = point.y;
        if (i > i2) {
            i2 = i;
        }
        int dimensionPixelSize = this.mActivity.getResources().getDimensionPixelSize(C0905R.dimen.preview_top_margin);
        int i3 = i2 / 4;
        this.mTopMargin = (i3 * dimensionPixelSize) / (dimensionPixelSize + this.mActivity.getResources().getDimensionPixelSize(C0905R.dimen.preview_bottom_margin));
        this.mBottomMargin = i3 - this.mTopMargin;
    }

    public void cameraOrientationPreviewResize(boolean z) {
        this.mPrevOrientationResize = this.mOrientationResize;
        this.mOrientationResize = z;
    }

    public void setSurfaceTextureSizeChangedListener(SurfaceTextureSizeChangedListener surfaceTextureSizeChangedListener) {
        this.mSurfaceTextureSizeListener = surfaceTextureSizeChangedListener;
    }

    public void initializeSurfaceView() {
        if (this.mSurfaceView == null) {
            this.mSurfaceView = new SurfaceView(this.mActivity);
            ((ViewGroup) this.mRootView).addView(this.mSurfaceView, 0);
            this.mSurfaceHolder = this.mSurfaceView.getHolder();
            this.mSurfaceHolder.addCallback(this);
        }
    }

    private void initializeControlByIntent() {
        this.mMenuButton = this.mRootView.findViewById(C0905R.C0907id.menu);
        this.mMenuButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                VideoUI.this.mVideoMenu.openFirstLevel();
            }
        });
        this.mCameraControls = (CameraControls) this.mRootView.findViewById(C0905R.C0907id.camera_controls);
        this.mOnScreenIndicators = new OnScreenIndicators(this.mActivity, this.mRootView.findViewById(C0905R.C0907id.on_screen_indicators));
        this.mOnScreenIndicators.resetToDefault();
        if (this.mController.isVideoCaptureIntent()) {
            hideSwitcher();
            this.mActivity.getLayoutInflater().inflate(C0905R.layout.review_module_control, this.mCameraControls);
            this.mReviewDoneButton = this.mRootView.findViewById(C0905R.C0907id.done_button);
            this.mReviewCancelButton = this.mRootView.findViewById(C0905R.C0907id.btn_cancel);
            this.mReviewPlayButton = this.mRootView.findViewById(C0905R.C0907id.btn_play);
            this.mReviewCancelButton.setVisibility(0);
            this.mReviewDoneButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    VideoUI.this.mController.onReviewDoneClicked(view);
                }
            });
            this.mReviewCancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    VideoUI.this.mController.onReviewCancelClicked(view);
                }
            });
            this.mReviewPlayButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    VideoUI.this.mController.onReviewPlayClicked(view);
                }
            });
        }
    }

    public void setPreviewSize(int i, int i2) {
        if (i == 0 || i2 == 0) {
            Log.w(TAG, "Preview size should not be 0.");
            return;
        }
        float f = i > i2 ? ((float) i) / ((float) i2) : ((float) i2) / ((float) i);
        if (this.mOrientationResize && this.mActivity.getResources().getConfiguration().orientation != 1) {
            f = 1.0f / f;
        }
        if (f != this.mAspectRatio) {
            this.mAspectRatioResize = true;
            this.mAspectRatio = f;
        }
        this.mCameraControls.setPreviewRatio(this.mAspectRatio, false);
        layoutPreview(f);
    }

    /* access modifiers changed from: private */
    public void layoutPreview(float f) {
        float f2;
        float f3;
        LayoutParams layoutParams;
        int i;
        LayoutParams layoutParams2;
        int i2;
        int displayRotation = CameraUtil.getDisplayRotation(this.mActivity);
        this.mScreenRatio = CameraUtil.determineRatio(f);
        int i3 = this.mScreenRatio;
        String str = TAG;
        if (i3 == 1 && CameraUtil.determinCloseRatio(f) == 2) {
            int i4 = (this.mTopMargin + this.mBottomMargin) * 4;
            int i5 = (i4 * 9) / 16;
            if (displayRotation != 90) {
                if (displayRotation == 180) {
                    i2 = (i4 * 3) / 4;
                    layoutParams = new LayoutParams(i5, i2);
                    layoutParams.setMargins(0, this.mBottomMargin, 0, this.mTopMargin);
                } else if (displayRotation != 270) {
                    i2 = (i4 * 3) / 4;
                    layoutParams = new LayoutParams(i5, i2);
                    layoutParams.setMargins(0, this.mTopMargin, 0, this.mBottomMargin);
                } else {
                    i = (i4 * 3) / 4;
                    layoutParams2 = new LayoutParams(i, i5);
                    layoutParams2.setMargins(this.mBottomMargin, 0, this.mTopMargin, 0);
                }
                float f4 = (float) i5;
                f3 = (float) i2;
                f2 = f4;
            } else {
                i = (i4 * 3) / 4;
                layoutParams2 = new LayoutParams(i, i5);
                layoutParams2.setMargins(this.mTopMargin, 0, this.mBottomMargin, 0);
            }
            f2 = (float) i;
            f3 = (float) i5;
        } else {
            f2 = (float) this.mMaxPreviewWidth;
            f3 = (float) this.mMaxPreviewHeight;
            if (f2 != 0.0f && f3 != 0.0f) {
                if (this.mScreenRatio == 2) {
                    f3 -= (float) (this.mTopMargin + this.mBottomMargin);
                }
                if (this.mOrientationResize) {
                    float f5 = this.mAspectRatio;
                    float f6 = f3 * f5;
                    if (f6 > f2) {
                        f3 = f2 / f5;
                    } else {
                        f2 = f6;
                    }
                } else if (f2 > f3) {
                    if (Math.max(f2, this.mAspectRatio * f3) > f2) {
                        f3 = f2 / this.mAspectRatio;
                    } else {
                        f2 = this.mAspectRatio * f3;
                    }
                } else if (Math.max(f3, this.mAspectRatio * f2) > f3) {
                    f2 = f3 / this.mAspectRatio;
                } else {
                    f3 = this.mAspectRatio * f2;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("setTransformMatrix: scaledTextureWidth = ");
                sb.append(f2);
                sb.append(", scaledTextureHeight = ");
                sb.append(f3);
                Log.v(str, sb.toString());
                if (((displayRotation == 0 || displayRotation == 180) && f2 > f3) || ((displayRotation == 90 || displayRotation == 270) && f2 < f3)) {
                    layoutParams = new LayoutParams((int) f3, (int) f2, 17);
                } else {
                    layoutParams = new LayoutParams((int) f2, (int) f3, 17);
                }
            } else {
                return;
            }
        }
        if (!(this.mSurfaceTextureUncroppedWidth == f2 && this.mSurfaceTextureUncroppedHeight == f3)) {
            this.mSurfaceTextureUncroppedWidth = f2;
            this.mSurfaceTextureUncroppedHeight = f3;
            SurfaceTextureSizeChangedListener surfaceTextureSizeChangedListener = this.mSurfaceTextureSizeListener;
            if (surfaceTextureSizeChangedListener != null) {
                surfaceTextureSizeChangedListener.onSurfaceTextureSizeChanged((int) this.mSurfaceTextureUncroppedWidth, (int) this.mSurfaceTextureUncroppedHeight);
                StringBuilder sb2 = new StringBuilder();
                sb2.append("mSurfaceTextureUncroppedWidth=");
                sb2.append(this.mSurfaceTextureUncroppedWidth);
                sb2.append("mSurfaceTextureUncroppedHeight=");
                sb2.append(this.mSurfaceTextureUncroppedHeight);
                Log.d(str, sb2.toString());
            }
        }
        this.mSurfaceView.setLayoutParams(layoutParams);
        this.mSurfaceView.requestLayout();
        FaceView faceView = this.mFaceView;
        if (faceView != null) {
            faceView.setLayoutParams(layoutParams);
        }
    }

    public void animateFlash() {
        this.mAnimationManager.startFlashAnimation(this.mFlashOverlay);
    }

    public void animateCapture() {
        animateCapture(null);
    }

    public void animateCapture(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "No valid bitmap for capture animation.");
            return;
        }
        this.mActivity.updateThumbnail(bitmap);
        this.mAnimationManager.startCaptureAnimation(this.mThumbnail);
    }

    public void cancelAnimations() {
        this.mAnimationManager.cancelAnimations();
    }

    public void hideUI() {
        this.mSwitcher.closePopup();
        if (!this.mUIhidden) {
            this.mUIhidden = true;
            this.mCameraControls.hideUI();
        }
    }

    public void showUI() {
        if (this.mUIhidden) {
            VideoMenu videoMenu = this.mVideoMenu;
            if (videoMenu == null || !videoMenu.isMenuBeingShown()) {
                this.mUIhidden = false;
                this.mCameraControls.showUI();
            }
        }
    }

    public boolean arePreviewControlsVisible() {
        return !this.mUIhidden;
    }

    public void hideSwitcher() {
        this.mSwitcher.closePopup();
        this.mSwitcher.setVisibility(4);
    }

    public void showSwitcher() {
        this.mSwitcher.setVisibility(0);
    }

    public void setSwitcherIndex() {
        this.mSwitcher.setCurrentIndex(1);
    }

    public boolean collapseCameraControls() {
        this.mSwitcher.closePopup();
        VideoMenu videoMenu = this.mVideoMenu;
        if (videoMenu != null) {
            videoMenu.closeAllView();
        }
        if (this.mPopup == null) {
            return false;
        }
        dismissPopup(false);
        return true;
    }

    public boolean removeTopLevelPopup() {
        if (this.mPopup == null) {
            return false;
        }
        dismissPopup(true);
        return true;
    }

    public void enableCameraControls(boolean z) {
        PreviewGestures previewGestures = this.mGestures;
        if (previewGestures != null) {
            previewGestures.setZoomOnly(!z);
        }
        PieRenderer pieRenderer = this.mPieRenderer;
        if (pieRenderer != null && pieRenderer.showsItems()) {
            this.mPieRenderer.hide();
        }
    }

    public void initDisplayChangeListener() {
        ((CameraRootView) this.mRootView).setDisplayChangeListener(this);
    }

    public void setDisplayOrientation(int i) {
        FaceView faceView = this.mFaceView;
        if (faceView != null) {
            faceView.setDisplayOrientation(i);
        }
        int i2 = this.mPreviewOrientation;
        if (i2 == -1 || i2 != i) {
            VideoMenu videoMenu = this.mVideoMenu;
            if (videoMenu != null && videoMenu.isPreviewMenuBeingShown()) {
                dismissSceneModeMenu();
                this.mVideoMenu.addModeBack();
            }
        }
        this.mPreviewOrientation = i;
    }

    public void removeDisplayChangeListener() {
        ((CameraRootView) this.mRootView).removeDisplayChangeListener();
    }

    public void overrideSettings(String... strArr) {
        VideoMenu videoMenu = this.mVideoMenu;
        if (videoMenu != null) {
            videoMenu.overrideSettings(strArr);
        }
    }

    public void setOrientationIndicator(int i, boolean z) {
        LinearLayout linearLayout = this.mLabelsLinearLayout;
        if (linearLayout == null) {
            return;
        }
        if (((i / 90) & 1) == 0) {
            linearLayout.setOrientation(1);
        } else {
            linearLayout.setOrientation(0);
        }
    }

    public SurfaceHolder getSurfaceHolder() {
        return this.mSurfaceHolder;
    }

    public void hideSurfaceView() {
        this.mSurfaceView.setVisibility(4);
    }

    public void showSurfaceView() {
        this.mSurfaceView.setVisibility(0);
    }

    private void initializeOverlay() {
        this.mRenderOverlay = (RenderOverlay) this.mRootView.findViewById(C0905R.C0907id.render_overlay);
        if (this.mPieRenderer == null) {
            this.mPieRenderer = new PieRenderer(this.mActivity);
            this.mPieRenderer.setPieListener(this);
        }
        if (this.mVideoMenu == null) {
            this.mVideoMenu = new VideoMenu(this.mActivity, this);
        }
        this.mRenderOverlay.addRenderer(this.mPieRenderer);
        if (this.mZoomRenderer == null) {
            this.mZoomRenderer = new ZoomRenderer(this.mActivity);
        }
        this.mRenderOverlay.addRenderer(this.mZoomRenderer);
        if (this.mGestures == null) {
            PreviewGestures previewGestures = new PreviewGestures(this.mActivity, this, this.mZoomRenderer, this.mPieRenderer, null);
            this.mGestures = previewGestures;
            this.mRenderOverlay.setGestures(this.mGestures);
        }
        this.mGestures.setVideoMenu(this.mVideoMenu);
        this.mGestures.setRenderOverlay(this.mRenderOverlay);
        if (!this.mActivity.isSecureCamera()) {
            this.mThumbnail = (ImageView) this.mRootView.findViewById(C0905R.C0907id.preview_thumb);
            this.mThumbnail.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (!VideoUI.this.mRecordingStarted && !CameraControls.isAnimating()) {
                        VideoUI.this.mActivity.gotoGallery();
                    }
                }
            });
        }
    }

    public void setPreviewGesturesVideoUI() {
        this.mActivity.setPreviewGestures(this.mGestures);
    }

    public void setPrefChangedListener(OnPreferenceChangedListener onPreferenceChangedListener) {
        this.mVideoMenu.setListener(onPreferenceChangedListener);
    }

    private void initializeMiscControls() {
        this.mReviewImage = (ImageView) this.mRootView.findViewById(C0905R.C0907id.review_image);
        this.mShutterButton.setImageResource(C0905R.C0906drawable.btn_new_shutter_video);
        this.mShutterButton.setOnShutterButtonListener(this.mController);
        this.mShutterButton.setVisibility(0);
        this.mShutterButton.requestFocus();
        this.mShutterButton.enableTouch(true);
        this.mRecordingTimeView = (TextView) this.mRootView.findViewById(C0905R.C0907id.recording_time);
        this.mRecordingTimeRect = (RotateLayout) this.mRootView.findViewById(C0905R.C0907id.recording_time_rect);
        this.mTimeLapseLabel = this.mRootView.findViewById(C0905R.C0907id.time_lapse_label);
        this.mLabelsLinearLayout = (LinearLayout) this.mRootView.findViewById(C0905R.C0907id.labels);
    }

    private void initializePauseButton() {
        this.mPauseButton = (PauseButton) this.mRootView.findViewById(C0905R.C0907id.video_pause);
        this.mPauseButton.setOnPauseButtonListener(this);
    }

    public void updateOnScreenIndicators(Parameters parameters, ComboPreferences comboPreferences) {
        this.mOnScreenIndicators.updateFlashOnScreenIndicator(parameters.getFlashMode());
        this.mOnScreenIndicators.updateLocationIndicator(RecordLocationPreference.get(comboPreferences, "pref_camera_recordlocation_key"));
    }

    public void setAspectRatio(double d) {
        if (this.mOrientationResize && this.mActivity.getResources().getConfiguration().orientation != 1) {
            d = 1.0d / d;
        }
        if (d != ((double) this.mAspectRatio)) {
            this.mAspectRatioResize = true;
            this.mAspectRatio = (float) d;
        }
        this.mCameraControls.setPreviewRatio(this.mAspectRatio, false);
        layoutPreview((float) d);
    }

    public void showTimeLapseUI(boolean z) {
        View view = this.mTimeLapseLabel;
        if (view != null) {
            view.setVisibility(z ? 0 : 8);
        }
        this.mIsTimeLapse = z;
    }

    public void dismissPopup(boolean z) {
        if (!this.mController.isInReviewMode()) {
            SettingsPopup settingsPopup = this.mPopup;
            if (settingsPopup != null) {
                settingsPopup.dismiss(z);
            }
        }
    }

    public boolean is4KEnabled() {
        VideoController videoController = this.mController;
        if (videoController != null) {
            return ((VideoModule) videoController).is4KEnabled();
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void popupDismissed() {
        this.mPopup = null;
    }

    public boolean onBackPressed() {
        VideoMenu videoMenu = this.mVideoMenu;
        if ((videoMenu == null || !videoMenu.handleBackKey()) && !hidePieRenderer()) {
            return removeTopLevelPopup();
        }
        return true;
    }

    public void cleanupListview() {
        showUI();
        this.mActivity.setSystemBarsVisibility(false);
    }

    public void dismissLevel1() {
        RotateLayout rotateLayout = this.mMenuLayout;
        if (rotateLayout != null) {
            ((ViewGroup) this.mRootView).removeView(rotateLayout);
            this.mMenuLayout = null;
        }
    }

    public void dismissLevel2() {
        RotateLayout rotateLayout = this.mSubMenuLayout;
        if (rotateLayout != null) {
            ((ViewGroup) this.mRootView).removeView(rotateLayout);
            this.mSubMenuLayout = null;
        }
    }

    public boolean sendTouchToPreviewMenu(MotionEvent motionEvent) {
        return this.mPreviewMenuLayout.dispatchTouchEvent(motionEvent);
    }

    public boolean sendTouchToMenu(MotionEvent motionEvent) {
        RotateLayout rotateLayout = this.mMenuLayout;
        if (rotateLayout != null) {
            return rotateLayout.getChildAt(0).dispatchTouchEvent(motionEvent);
        }
        return false;
    }

    public void dismissSceneModeMenu() {
        LinearLayout linearLayout = this.mPreviewMenuLayout;
        if (linearLayout != null) {
            ((ViewGroup) this.mRootView).removeView(linearLayout);
            this.mPreviewMenuLayout = null;
        }
    }

    public void removeSceneModeMenu() {
        LinearLayout linearLayout = this.mPreviewMenuLayout;
        if (linearLayout != null) {
            ((ViewGroup) this.mRootView).removeView(linearLayout);
            this.mPreviewMenuLayout = null;
        }
        cleanupListview();
    }

    public void removeLevel2() {
        RotateLayout rotateLayout = this.mSubMenuLayout;
        if (rotateLayout != null) {
            this.mSubMenuLayout.removeView(rotateLayout.getChildAt(0));
        }
    }

    public void showPopup(ListView listView, int i, boolean z) {
        LayoutParams layoutParams;
        LayoutParams layoutParams2;
        hideUI();
        listView.setVisibility(0);
        if (i == 1) {
            if (this.mMenuLayout == null) {
                this.mMenuLayout = new RotateLayout(this.mActivity, null);
                if (this.mRootView.getLayoutDirection() != 1) {
                    layoutParams2 = new LayoutParams(CameraActivity.SETTING_LIST_WIDTH_1, -2, 51);
                } else {
                    layoutParams2 = new LayoutParams(CameraActivity.SETTING_LIST_WIDTH_1, -2, 53);
                }
                this.mMenuLayout.setLayoutParams(layoutParams2);
                ((ViewGroup) this.mRootView).addView(this.mMenuLayout);
            }
            this.mMenuLayout.setOrientation(this.mOrientation, true);
            this.mMenuLayout.addView(listView);
        }
        if (i == 2) {
            if (this.mSubMenuLayout == null) {
                this.mSubMenuLayout = new RotateLayout(this.mActivity, null);
                this.mSubMenuLayout.setLayoutParams(new ViewGroup.LayoutParams(CameraActivity.SETTING_LIST_WIDTH_2, -2));
                ((ViewGroup) this.mRootView).addView(this.mSubMenuLayout);
            }
            if (this.mRootView.getLayoutDirection() != 1) {
                layoutParams = new LayoutParams(CameraActivity.SETTING_LIST_WIDTH_2, -2, 51);
            } else {
                layoutParams = new LayoutParams(CameraActivity.SETTING_LIST_WIDTH_2, -2, 53);
            }
            int i2 = this.mOrientation;
            int height = (i2 == 0 || i2 == 180) ? this.mRootView.getHeight() : this.mRootView.getWidth();
            ListSubMenu listSubMenu = (ListSubMenu) listView;
            int preCalculatedHeight = listSubMenu.getPreCalculatedHeight();
            int yBase = listSubMenu.getYBase();
            int max = Math.max(0, yBase);
            if (yBase + preCalculatedHeight > height) {
                max = Math.max(0, height - preCalculatedHeight);
            }
            if (this.mRootView.getLayoutDirection() != 1) {
                layoutParams.setMargins(CameraActivity.SETTING_LIST_WIDTH_1, max, 0, 0);
            } else {
                layoutParams.setMargins(0, max, CameraActivity.SETTING_LIST_WIDTH_1, 0);
            }
            this.mSubMenuLayout.setLayoutParams(layoutParams);
            this.mSubMenuLayout.addView(listView);
            this.mSubMenuLayout.setOrientation(this.mOrientation, true);
        }
        if (z) {
            if (i == 1) {
                this.mVideoMenu.animateSlideIn(this.mMenuLayout, CameraActivity.SETTING_LIST_WIDTH_1, true);
            }
            if (i == 2) {
                this.mVideoMenu.animateFadeIn(listView);
                return;
            }
            return;
        }
        listView.setAlpha(0.85f);
    }

    public ViewGroup getMenuLayout() {
        return this.mMenuLayout;
    }

    public void setPreviewMenuLayout(LinearLayout linearLayout) {
        this.mPreviewMenuLayout = linearLayout;
    }

    public ViewGroup getPreviewMenuLayout() {
        return this.mPreviewMenuLayout;
    }

    public void showPopup(AbstractSettingPopup abstractSettingPopup) {
        hideUI();
        SettingsPopup settingsPopup = this.mPopup;
        if (settingsPopup != null) {
            settingsPopup.dismiss(false);
        }
        this.mPopup = new SettingsPopup(abstractSettingPopup);
    }

    public void onShowSwitcherPopup() {
        hidePieRenderer();
    }

    public boolean hidePieRenderer() {
        PieRenderer pieRenderer = this.mPieRenderer;
        if (pieRenderer == null || !pieRenderer.showsItems()) {
            return false;
        }
        this.mPieRenderer.hide();
        return true;
    }

    public void setShutterPressed(boolean z) {
        PreviewGestures previewGestures = this.mGestures;
        if (previewGestures != null) {
            previewGestures.setEnabled(!z);
        }
    }

    public void enableShutter(boolean z) {
        if (this.mShutterButton != null) {
            String str = TAG;
            if (z) {
                Log.v(str, "Shutter Button enabled !!");
            } else {
                Log.v(str, "Shutter Button disabled !!");
            }
            this.mShutterButton.setEnabled(z);
        }
    }

    public void onPieOpened(int i, int i2) {
        setSwipingEnabled(false);
        this.mSwitcher.closePopup();
    }

    public void onPieClosed() {
        setSwipingEnabled(true);
    }

    public void setSwipingEnabled(boolean z) {
        this.mActivity.setSwipingEnabled(z);
    }

    public void onSingleTapUp(View view, int i, int i2) {
        this.mController.onSingleTapUp(view, i, i2);
    }

    public void showRecordingUI(boolean z) {
        this.mRecordingStarted = z;
        int i = 8;
        this.mMenuButton.setVisibility(z ? 8 : 0);
        OnScreenIndicators onScreenIndicators = this.mOnScreenIndicators;
        if (!z) {
            i = 0;
        }
        onScreenIndicators.setVisibility(i);
        if (z) {
            this.mShutterButton.setImageResource(C0905R.C0906drawable.shutter_button_video_stop);
            hideSwitcher();
            this.mRecordingTimeView.setText(BuildConfig.FLAVOR);
            ((ViewGroup) this.mRootView).addView(this.mRecordingTimeRect);
            return;
        }
        this.mShutterButton.setImageResource(C0905R.C0906drawable.btn_new_shutter_video);
        if (!this.mController.isVideoCaptureIntent()) {
            showSwitcher();
        }
        ((ViewGroup) this.mRootView).removeView(this.mRecordingTimeRect);
    }

    public void hideUIwhileRecording() {
        this.mCameraControls.setWillNotDraw(true);
        this.mVideoMenu.hideUI();
    }

    public void showUIafterRecording() {
        this.mCameraControls.setWillNotDraw(false);
        if (!this.mController.isVideoCaptureIntent()) {
            this.mVideoMenu.showUI();
        }
    }

    public void showReviewImage(Bitmap bitmap) {
        this.mReviewImage.setImageBitmap(bitmap);
        this.mReviewImage.setVisibility(0);
    }

    public void showReviewControls() {
        CameraUtil.fadeOut(this.mShutterButton);
        CameraUtil.fadeIn(this.mReviewDoneButton);
        CameraUtil.fadeIn(this.mReviewPlayButton);
        this.mReviewImage.setVisibility(0);
        this.mMenuButton.setVisibility(8);
        this.mCameraControls.hideUI();
        this.mVideoMenu.hideUI();
        this.mOnScreenIndicators.setVisibility(8);
    }

    public void hideReviewUI() {
        this.mReviewImage.setVisibility(8);
        this.mShutterButton.setEnabled(true);
        this.mMenuButton.setVisibility(0);
        this.mCameraControls.showUI();
        this.mVideoMenu.showUI();
        this.mOnScreenIndicators.setVisibility(0);
        CameraUtil.fadeOut(this.mReviewDoneButton);
        CameraUtil.fadeOut(this.mReviewPlayButton);
        CameraUtil.fadeIn(this.mShutterButton);
    }

    private void setShowMenu(boolean z) {
        if (!this.mController.isVideoCaptureIntent()) {
            OnScreenIndicators onScreenIndicators = this.mOnScreenIndicators;
            if (onScreenIndicators != null) {
                onScreenIndicators.setVisibility(z ? 0 : 8);
            }
        }
    }

    public void onPreviewFocusChanged(boolean z) {
        if (z) {
            showUI();
        } else {
            hideUI();
        }
        PreviewGestures previewGestures = this.mGestures;
        if (previewGestures != null) {
            previewGestures.setEnabled(z);
        }
        RenderOverlay renderOverlay = this.mRenderOverlay;
        if (renderOverlay != null) {
            renderOverlay.setVisibility(z ? 0 : 8);
        }
        setShowMenu(z);
    }

    public void initializePopup(PreferenceGroup preferenceGroup) {
        this.mVideoMenu.initialize(preferenceGroup);
    }

    public void initializeZoom(Parameters parameters) {
        if (parameters == null || !parameters.isZoomSupported()) {
            this.mGestures.setZoomEnabled(false);
            return;
        }
        this.mGestures.setZoomEnabled(true);
        this.mZoomMax = parameters.getMaxZoom();
        this.mZoomRatios = parameters.getZoomRatios();
        this.mZoomRenderer.setZoomMax(this.mZoomMax);
        this.mZoomRenderer.setZoom(parameters.getZoom());
        this.mZoomRenderer.setZoomValue(((Integer) this.mZoomRatios.get(parameters.getZoom())).intValue());
        this.mZoomRenderer.setOnZoomChangeListener(new ZoomChangeListener());
    }

    public void clickShutter() {
        this.mShutterButton.performClick();
    }

    public void pressShutter(boolean z) {
        this.mShutterButton.setPressed(z);
    }

    public View getShutterButton() {
        return this.mShutterButton;
    }

    public void setRecordingTime(String str) {
        this.mRecordingTimeView.setText(str);
    }

    public void setRecordingTimeTextColor(int i) {
        this.mRecordingTimeView.setTextColor(i);
    }

    public boolean isVisible() {
        return this.mCameraControls.getVisibility() == 0;
    }

    public void onDisplayChanged() {
        this.mCameraControls.checkLayoutFlip();
        this.mController.updateCameraOrientation();
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        StringBuilder sb = new StringBuilder();
        sb.append("surfaceChanged: width = ");
        sb.append(i2);
        sb.append(", height = ");
        sb.append(i3);
        Log.v(TAG, sb.toString());
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.v(TAG, "surfaceCreated");
        this.mSurfaceHolder = surfaceHolder;
        this.mController.onPreviewUIReady();
        this.mActivity.updateThumbnail(this.mThumbnail);
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.v(TAG, "surfaceDestroyed");
        this.mSurfaceHolder = null;
        this.mController.onPreviewUIDestroyed();
    }

    public View getRootView() {
        return this.mRootView;
    }

    public void onButtonPause() {
        this.mRecordingTimeView.setCompoundDrawablesWithIntrinsicBounds(C0905R.C0906drawable.ic_pausing_indicator, 0, 0, 0);
        this.mController.onButtonPause();
    }

    public void onButtonContinue() {
        this.mRecordingTimeView.setCompoundDrawablesWithIntrinsicBounds(C0905R.C0906drawable.ic_recording_indicator, 0, 0, 0);
        this.mController.onButtonContinue();
    }

    public void resetPauseButton() {
        this.mRecordingTimeView.setCompoundDrawablesWithIntrinsicBounds(C0905R.C0906drawable.ic_recording_indicator, 0, 0, 0);
        this.mPauseButton.setPaused(false);
    }

    public void setPreference(String str, String str2) {
        this.mVideoMenu.setPreference(str, str2);
    }

    public boolean hideSwitcherPopup() {
        ModuleSwitcher moduleSwitcher = this.mSwitcher;
        if (moduleSwitcher == null || !moduleSwitcher.showsPopup()) {
            return false;
        }
        this.mSwitcher.closePopup();
        return true;
    }

    public void setOrientation(int i, boolean z) {
        this.mCameraControls.setOrientation(i, z);
        RotateLayout rotateLayout = this.mMenuLayout;
        if (rotateLayout != null) {
            rotateLayout.setOrientation(i, z);
        }
        RotateLayout rotateLayout2 = this.mSubMenuLayout;
        if (rotateLayout2 != null) {
            rotateLayout2.setOrientation(i, z);
        }
        RotateLayout rotateLayout3 = this.mRecordingTimeRect;
        if (rotateLayout3 != null) {
            if (i == 180) {
                rotateLayout3.setOrientation(0, false);
                this.mRecordingTimeView.setRotation(180.0f);
            } else {
                this.mRecordingTimeView.setRotation(0.0f);
                this.mRecordingTimeRect.setOrientation(i, false);
            }
        }
        LinearLayout linearLayout = this.mPreviewMenuLayout;
        if (linearLayout != null) {
            ViewGroup viewGroup = (ViewGroup) linearLayout.getChildAt(0);
            if (viewGroup != null) {
                viewGroup = (ViewGroup) viewGroup.getChildAt(0);
            }
            if (viewGroup != null) {
                for (int childCount = viewGroup.getChildCount() - 1; childCount >= 0; childCount--) {
                    ((RotateLayout) viewGroup.getChildAt(childCount)).setOrientation(i, z);
                }
            }
        }
        ZoomRenderer zoomRenderer = this.mZoomRenderer;
        if (zoomRenderer != null) {
            zoomRenderer.setOrientation(i);
        }
        RotateTextToast.setOrientation(i);
        this.mOrientation = i;
    }

    public void tryToCloseSubList() {
        VideoMenu videoMenu = this.mVideoMenu;
        if (videoMenu != null) {
            videoMenu.tryToCloseSubList();
        }
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public void adjustOrientation() {
        setOrientation(this.mOrientation, false);
    }

    public void onFaceDetection(Face[] faceArr, CameraProxy cameraProxy) {
        Log.d(TAG, "onFacedetectopmn");
        this.mFaceView.setFaces(faceArr);
    }

    public void pauseFaceDetection() {
        FaceView faceView = this.mFaceView;
        if (faceView != null) {
            faceView.pause();
        }
    }

    public void resumeFaceDetection() {
        FaceView faceView = this.mFaceView;
        if (faceView != null) {
            faceView.resume();
        }
    }

    public void onStartFaceDetection(int i, boolean z) {
        this.mFaceView.setBlockDraw(false);
        this.mFaceView.clear();
        this.mFaceView.setVisibility(0);
        this.mFaceView.setDisplayOrientation(i);
        this.mFaceView.setMirror(z);
        this.mFaceView.resume();
    }

    public void onStopFaceDetection() {
        FaceView faceView = this.mFaceView;
        if (faceView != null) {
            faceView.setBlockDraw(true);
            this.mFaceView.clear();
        }
    }
}
