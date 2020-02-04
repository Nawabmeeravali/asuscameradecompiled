package com.android.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.SeekBar;
import com.android.camera.CameraManager.CameraFaceDetectionCallback;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraPreference.OnPreferenceChangedListener;
import com.android.camera.FocusOverlayManager.FocusUI;
import com.android.camera.PreviewGestures.SingleTapListener;
import com.android.camera.p004ui.AbstractSettingPopup;
import com.android.camera.p004ui.CameraControls;
import com.android.camera.p004ui.CameraRootView;
import com.android.camera.p004ui.CameraRootView.MyDisplayListener;
import com.android.camera.p004ui.CountDownView;
import com.android.camera.p004ui.CountDownView.OnCountDownFinishedListener;
import com.android.camera.p004ui.FaceView;
import com.android.camera.p004ui.FocusIndicator;
import com.android.camera.p004ui.ListSubMenu;
import com.android.camera.p004ui.MenuHelp;
import com.android.camera.p004ui.ModuleSwitcher;
import com.android.camera.p004ui.PieRenderer;
import com.android.camera.p004ui.PieRenderer.PieListener;
import com.android.camera.p004ui.RenderOverlay;
import com.android.camera.p004ui.RotateImageView;
import com.android.camera.p004ui.RotateLayout;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.p004ui.SelfieFlashView;
import com.android.camera.p004ui.ZoomRenderer;
import com.android.camera.p004ui.ZoomRenderer.OnZoomChangedListener;
import com.android.camera.util.CameraUtil;
import java.util.List;
import org.codeaurora.snapcam.C0905R;

public class PhotoUI implements PieListener, SingleTapListener, FocusUI, Callback, MyDisplayListener, CameraFaceDetectionCallback {
    private static final String TAG = "CAM_UI";
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    private final AnimationManager mAnimationManager;
    /* access modifiers changed from: private */
    public float mAspectRatio = 1.3333334f;
    /* access modifiers changed from: private */
    public boolean mAspectRatioResize;
    private SeekBar mBlurDegreeProgressBar;
    private int mBottomMargin = 0;
    /* access modifiers changed from: private */
    public CameraControls mCameraControls;
    /* access modifiers changed from: private */
    public PhotoController mController;
    private CountDownView mCountDownView;
    /* access modifiers changed from: private */
    public DecodeImageForReview mDecodeTaskForReview = null;
    /* access modifiers changed from: private */
    public boolean mDismissAll = false;
    /* access modifiers changed from: private */
    public int mDownSampleFactor = 4;
    private FaceView mFaceView;
    private View mFlashOverlay;
    private PreviewGestures mGestures;
    private boolean mIsBokehMode = false;
    /* access modifiers changed from: private */
    public boolean mIsLayoutInitializedAlready = false;
    private AlertDialog mLocationDialog;
    private LinearLayout mMakeupMenuLayout;
    /* access modifiers changed from: private */
    public int mMaxPreviewHeight = 0;
    /* access modifiers changed from: private */
    public int mMaxPreviewWidth = 0;
    /* access modifiers changed from: private */
    public PhotoMenu mMenu;
    private View mMenuButton;
    /* access modifiers changed from: private */
    public MenuHelp mMenuHelp;
    public boolean mMenuInitialized = false;
    private RotateLayout mMenuLayout;
    private RotateTextToast mNotSelectableToast;
    private OnScreenIndicators mOnScreenIndicators;
    /* access modifiers changed from: private */
    public int mOrientation;
    /* access modifiers changed from: private */
    public boolean mOrientationResize;
    /* access modifiers changed from: private */
    public PieRenderer mPieRenderer;
    /* access modifiers changed from: private */
    public PopupWindow mPopup;
    /* access modifiers changed from: private */
    public boolean mPrevOrientationResize;
    private View mPreviewCover;
    private LinearLayout mPreviewMenuLayout;
    private int mPreviewOrientation = -1;
    private RenderOverlay mRenderOverlay;
    private View mReviewCancelButton;
    private View mReviewDoneButton;
    /* access modifiers changed from: private */
    public ImageView mReviewImage;
    private View mReviewRetakeButton;
    private View mRootView;
    private float mScreenBrightness = 0.0f;
    private int mScreenRatio = 0;
    private SelfieFlashView mSelfieView;
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
    private int mTopMargin = 0;
    private boolean mUIhidden = false;
    private int mZoomMax;
    /* access modifiers changed from: private */
    public List<Integer> mZoomRatios;
    /* access modifiers changed from: private */
    public ZoomRenderer mZoomRenderer;

    private class DecodeImageForReview extends DecodeTask {
        public DecodeImageForReview(byte[] bArr, int i, boolean z) {
            super(bArr, i, z);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
            if (!isCancelled()) {
                PhotoUI.this.mReviewImage.setImageBitmap(bitmap);
                PhotoUI.this.mReviewImage.setVisibility(0);
                PhotoUI.this.mDecodeTaskForReview = null;
            }
        }
    }

    private class DecodeTask extends AsyncTask<Void, Void, Bitmap> {
        private final byte[] mData;
        private boolean mMirror;
        private int mOrientation;

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
        }

        public DecodeTask(byte[] bArr, int i, boolean z) {
            this.mData = bArr;
            this.mOrientation = i;
            this.mMirror = z;
        }

        /* access modifiers changed from: protected */
        public Bitmap doInBackground(Void... voidArr) {
            Bitmap downSample = CameraUtil.downSample(this.mData, PhotoUI.this.mDownSampleFactor);
            if ((this.mOrientation == 0 && !this.mMirror) || downSample == null) {
                return downSample;
            }
            Matrix matrix = new Matrix();
            if (this.mMirror) {
                matrix.setScale(-1.0f, 1.0f);
            }
            matrix.preRotate((float) this.mOrientation);
            return Bitmap.createBitmap(downSample, 0, 0, downSample.getWidth(), downSample.getHeight(), matrix, false);
        }
    }

    public enum SURFACE_STATUS {
        HIDE,
        SURFACE_VIEW
    }

    public interface SurfaceTextureSizeChangedListener {
        void onSurfaceTextureSizeChanged(int i, int i2);
    }

    private class ZoomChangeListener implements OnZoomChangedListener {
        public void onZoomValueChanged(float f) {
        }

        private ZoomChangeListener() {
        }

        public void onZoomValueChanged(int i) {
            int onZoomChanged = PhotoUI.this.mController.onZoomChanged(i);
            if (PhotoUI.this.mZoomRenderer != null) {
                PhotoUI.this.mZoomRenderer.setZoomValue(((Integer) PhotoUI.this.mZoomRatios.get(onZoomChanged)).intValue());
            }
        }

        public void onZoomStart() {
            if (PhotoUI.this.mPieRenderer != null) {
                PhotoUI.this.mPieRenderer.hide();
                PhotoUI.this.mPieRenderer.setBlockFocus(true);
            }
        }

        public void onZoomEnd() {
            if (PhotoUI.this.mPieRenderer != null) {
                PhotoUI.this.mPieRenderer.setBlockFocus(false);
            }
        }
    }

    public void setCameraState(int i) {
    }

    public CameraControls getCameraControls() {
        return this.mCameraControls;
    }

    public synchronized void applySurfaceChange(SURFACE_STATUS surface_status) {
        if (surface_status == SURFACE_STATUS.HIDE) {
            this.mSurfaceView.setVisibility(8);
        } else {
            this.mSurfaceView.setVisibility(0);
        }
    }

    public PhotoUI(CameraActivity cameraActivity, PhotoController photoController, View view) {
        this.mActivity = cameraActivity;
        this.mController = photoController;
        this.mRootView = view;
        this.mActivity.getLayoutInflater().inflate(C0905R.layout.photo_module, (ViewGroup) this.mRootView, true);
        this.mPreviewCover = this.mRootView.findViewById(C0905R.C0907id.preview_cover);
        this.mSurfaceView = (SurfaceView) this.mRootView.findViewById(C0905R.C0907id.mdp_preview_content);
        this.mSurfaceView.setVisibility(0);
        this.mSurfaceHolder = this.mSurfaceView.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mSurfaceHolder.setType(3);
        Log.v(TAG, "Using mdp_preview_content (MDP path)");
        this.mSurfaceView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                int i9 = i3 - i;
                int i10 = i4 - i2;
                PhotoUI.this.tryToCloseSubList();
                if (PhotoUI.this.mMaxPreviewWidth == 0 && PhotoUI.this.mMaxPreviewHeight == 0) {
                    PhotoUI.this.mMaxPreviewWidth = i9;
                    PhotoUI.this.mMaxPreviewHeight = i10;
                }
                if (PhotoUI.this.mOrientationResize != PhotoUI.this.mPrevOrientationResize || PhotoUI.this.mAspectRatioResize || !PhotoUI.this.mIsLayoutInitializedAlready) {
                    PhotoUI photoUI = PhotoUI.this;
                    photoUI.layoutPreview(photoUI.mAspectRatio);
                    PhotoUI.this.mAspectRatioResize = false;
                }
            }
        });
        this.mRenderOverlay = (RenderOverlay) this.mRootView.findViewById(C0905R.C0907id.render_overlay);
        this.mFlashOverlay = this.mRootView.findViewById(C0905R.C0907id.flash_overlay);
        this.mShutterButton = (ShutterButton) this.mRootView.findViewById(C0905R.C0907id.shutter_button);
        this.mSwitcher = (ModuleSwitcher) this.mRootView.findViewById(C0905R.C0907id.camera_switcher);
        this.mSwitcher.setCurrentIndex(0);
        this.mSwitcher.setSwitchListener(this.mActivity);
        this.mSwitcher.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (PhotoUI.this.mController.getCameraState() != 5) {
                    PhotoUI.this.mSwitcher.showPopup();
                    PhotoUI.this.mSwitcher.setOrientation(PhotoUI.this.mOrientation, false);
                }
            }
        });
        this.mMenuButton = this.mRootView.findViewById(C0905R.C0907id.menu);
        ((RotateImageView) this.mRootView.findViewById(C0905R.C0907id.mute_button)).setVisibility(8);
        this.mBlurDegreeProgressBar = (SeekBar) this.mRootView.findViewById(C0905R.C0907id.blur_degree_bar);
        this.mBlurDegreeProgressBar.setMax(100);
        this.mCameraControls = (CameraControls) this.mRootView.findViewById(C0905R.C0907id.camera_controls);
        ViewStub viewStub = (ViewStub) this.mRootView.findViewById(C0905R.C0907id.face_view_stub);
        if (viewStub != null) {
            viewStub.inflate();
            this.mFaceView = (FaceView) this.mRootView.findViewById(C0905R.C0907id.face_view);
            setSurfaceTextureSizeChangedListener(this.mFaceView);
        }
        initIndicators();
        this.mAnimationManager = new AnimationManager();
        this.mOrientationResize = false;
        this.mPrevOrientationResize = false;
        Point point = new Point();
        this.mActivity.getWindowManager().getDefaultDisplay().getSize(point);
        this.mScreenRatio = CameraUtil.determineRatio(point.x, point.y);
        calculateMargins(point);
        this.mCameraControls.setMargins(this.mTopMargin, this.mBottomMargin);
        showFirstTimeHelp();
    }

    public SeekBar getBokehDegreeBar() {
        return this.mBlurDegreeProgressBar;
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

    public void setDownFactor(int i) {
        this.mDownSampleFactor = i;
    }

    public void cameraOrientationPreviewResize(boolean z) {
        this.mPrevOrientationResize = this.mOrientationResize;
        this.mOrientationResize = z;
    }

    private void showFirstTimeHelp(int i, int i2) {
        this.mMenuHelp = (MenuHelp) this.mRootView.findViewById(C0905R.C0907id.menu_help);
        this.mMenuHelp.setVisibility(0);
        this.mMenuHelp.setMargins(i, i2);
        this.mMenuHelp.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (PhotoUI.this.mMenuHelp != null) {
                    PhotoUI.this.mMenuHelp.setVisibility(8);
                    PhotoUI.this.mMenuHelp = null;
                }
            }
        });
    }

    public void setAspectRatio(float f) {
        if (((double) f) > 0.0d) {
            if (this.mOrientationResize && this.mActivity.getResources().getConfiguration().orientation != 1) {
                f = 1.0f / f;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("setAspectRatio() ratio[");
            sb.append(f);
            sb.append("] mAspectRatio[");
            sb.append(this.mAspectRatio);
            sb.append("]");
            Log.d(TAG, sb.toString());
            if (f != this.mAspectRatio) {
                this.mAspectRatioResize = true;
                this.mAspectRatio = f;
            }
            this.mCameraControls.setPreviewRatio(this.mAspectRatio, false);
            layoutPreview(f);
            return;
        }
        throw new IllegalArgumentException();
    }

    public void layoutPreview(float f) {
        float f2;
        float f3;
        LayoutParams layoutParams;
        LayoutParams layoutParams2;
        int i;
        LayoutParams layoutParams3;
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
                    layoutParams3 = new LayoutParams(i, i5);
                    layoutParams3.setMargins(this.mBottomMargin, 0, this.mTopMargin, 0);
                }
                float f4 = (float) i5;
                f3 = (float) i2;
                f2 = f4;
            } else {
                i = (i4 * 3) / 4;
                layoutParams3 = new LayoutParams(i, i5);
                layoutParams3.setMargins(this.mTopMargin, 0, this.mBottomMargin, 0);
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
                    layoutParams2 = new LayoutParams((int) f3, (int) f2, 17);
                } else {
                    layoutParams2 = new LayoutParams((int) f2, (int) f3, 17);
                }
                if (this.mScreenRatio == 2) {
                    layoutParams.gravity = 49;
                    layoutParams.setMargins(0, this.mTopMargin, 0, this.mBottomMargin);
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
        this.mRootView.requestLayout();
        FaceView faceView = this.mFaceView;
        if (faceView != null) {
            faceView.setLayoutParams(layoutParams);
        }
        this.mIsLayoutInitializedAlready = true;
    }

    public void setSurfaceTextureSizeChangedListener(SurfaceTextureSizeChangedListener surfaceTextureSizeChangedListener) {
        this.mSurfaceTextureSizeListener = surfaceTextureSizeChangedListener;
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        StringBuilder sb = new StringBuilder();
        sb.append("surfaceChanged: width =");
        sb.append(i2);
        sb.append(", height = ");
        sb.append(i3);
        Log.v(TAG, sb.toString());
        this.mController.onPreviewRectChanged(CameraUtil.rectFToRect(new RectF((float) this.mSurfaceView.getLeft(), (float) this.mSurfaceView.getTop(), (float) this.mSurfaceView.getRight(), (float) this.mSurfaceView.getBottom())));
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

    private void initIndicators() {
        this.mOnScreenIndicators = new OnScreenIndicators(this.mActivity, this.mRootView.findViewById(C0905R.C0907id.on_screen_indicators));
    }

    public void onCameraOpened(PreferenceGroup preferenceGroup, ComboPreferences comboPreferences, Parameters parameters, OnPreferenceChangedListener onPreferenceChangedListener, MakeupLevelListener makeupLevelListener) {
        if (this.mPieRenderer == null) {
            this.mPieRenderer = new PieRenderer(this.mActivity);
            this.mPieRenderer.setPieListener(this);
            this.mRenderOverlay.addRenderer(this.mPieRenderer);
        }
        if (this.mMenu == null) {
            this.mMenu = new PhotoMenu(this.mActivity, this, makeupLevelListener);
            this.mMenu.setListener(onPreferenceChangedListener);
        }
        this.mMenu.initialize(preferenceGroup);
        this.mMenuInitialized = true;
        if (this.mZoomRenderer == null) {
            this.mZoomRenderer = new ZoomRenderer(this.mActivity);
            this.mRenderOverlay.addRenderer(this.mZoomRenderer);
        }
        if (this.mGestures == null) {
            PreviewGestures previewGestures = new PreviewGestures(this.mActivity, this, this.mZoomRenderer, this.mPieRenderer, null);
            this.mGestures = previewGestures;
            this.mRenderOverlay.setGestures(this.mGestures);
        }
        this.mGestures.setPhotoMenu(this.mMenu);
        this.mGestures.setZoomEnabled(parameters.isZoomSupported());
        this.mGestures.setRenderOverlay(this.mRenderOverlay);
        this.mRenderOverlay.requestLayout();
        initializeZoom(parameters);
        updateOnScreenIndicators(parameters, preferenceGroup, comboPreferences);
        this.mActivity.setPreviewGestures(this.mGestures);
    }

    public void animateCapture(byte[] bArr) {
        this.mActivity.updateThumbnail(bArr);
    }

    public void showRefocusToast(boolean z) {
        this.mCameraControls.showRefocusToast(z);
    }

    private void openMenu() {
        if (this.mPieRenderer != null) {
            if (this.mController.getCameraState() == 2) {
                this.mController.cancelAutoFocus();
            }
            this.mPieRenderer.showInCenter();
        }
    }

    public void initializeControlByIntent() {
        if (!this.mActivity.isSecureCamera() && !this.mActivity.isCaptureIntent()) {
            this.mThumbnail = (ImageView) this.mRootView.findViewById(C0905R.C0907id.preview_thumb);
            this.mThumbnail.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (!CameraControls.isAnimating() && PhotoUI.this.mController.getCameraState() != 3) {
                        PhotoUI.this.mActivity.gotoGallery();
                    }
                }
            });
        }
        this.mMenuButton = this.mRootView.findViewById(C0905R.C0907id.menu);
        this.mMenuButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (PhotoUI.this.mMenu != null) {
                    PhotoUI.this.mMenu.openFirstLevel();
                }
            }
        });
        if (this.mController.isImageCaptureIntent()) {
            hideSwitcher();
            this.mCameraControls.hideRemainingPhotoCnt();
            this.mSwitcher.setSwitcherVisibility(false);
            this.mActivity.getLayoutInflater().inflate(C0905R.layout.review_module_control, (ViewGroup) this.mRootView.findViewById(C0905R.C0907id.camera_controls));
            this.mReviewDoneButton = this.mRootView.findViewById(C0905R.C0907id.done_button);
            this.mReviewCancelButton = this.mRootView.findViewById(C0905R.C0907id.btn_cancel);
            this.mReviewRetakeButton = this.mRootView.findViewById(C0905R.C0907id.btn_retake);
            this.mReviewImage = (ImageView) this.mRootView.findViewById(C0905R.C0907id.review_image);
            this.mReviewCancelButton.setVisibility(0);
            this.mReviewDoneButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    PhotoUI.this.mController.onCaptureDone();
                }
            });
            this.mReviewCancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    PhotoUI.this.mController.onCaptureCancelled();
                }
            });
            this.mReviewRetakeButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    PhotoUI.this.mController.onCaptureRetake();
                    if (PhotoUI.this.mController.isImageCaptureIntent()) {
                        PhotoUI.this.mCameraControls.setTitleBarVisibility(0);
                    }
                }
            });
        }
    }

    public void hideRemainingPhotoCnt() {
        this.mCameraControls.hideRemainingPhotoCnt();
    }

    public void showRemainingPhotoCnt() {
        this.mCameraControls.showRemainingPhotoCnt();
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
            PhotoMenu photoMenu = this.mMenu;
            if (photoMenu == null || !photoMenu.isMenuBeingShown()) {
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
        this.mSwitcher.setCurrentIndex(0);
    }

    public void initializeFirstTime() {
        this.mShutterButton.setImageResource(C0905R.C0906drawable.shutter_button_anim);
        this.mShutterButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (!CameraControls.isAnimating()) {
                    PhotoUI.this.doShutterAnimation();
                }
                if (PhotoUI.this.mController.isImageCaptureIntent()) {
                    PhotoUI.this.mCameraControls.setTitleBarVisibility(0);
                }
            }
        });
        this.mShutterButton.setOnShutterButtonListener(this.mController);
        this.mShutterButton.setVisibility(0);
    }

    public void doShutterAnimation() {
        AnimationDrawable animationDrawable = (AnimationDrawable) this.mShutterButton.getDrawable();
        animationDrawable.stop();
        animationDrawable.start();
    }

    public void initializeSecondTime(Parameters parameters) {
        initializeZoom(parameters);
        if (this.mController.isImageCaptureIntent()) {
            hidePostCaptureAlert();
        }
        PhotoMenu photoMenu = this.mMenu;
        if (photoMenu != null) {
            photoMenu.reloadPreferences();
        }
        ((RotateImageView) this.mRootView.findViewById(C0905R.C0907id.mute_button)).setVisibility(8);
    }

    public void initializeZoom(Parameters parameters) {
        if (parameters != null && parameters.isZoomSupported() && this.mZoomRenderer != null) {
            this.mZoomMax = parameters.getMaxZoom();
            this.mZoomRatios = parameters.getZoomRatios();
            ZoomRenderer zoomRenderer = this.mZoomRenderer;
            if (zoomRenderer != null) {
                zoomRenderer.setZoomMax(this.mZoomMax);
                this.mZoomRenderer.setZoom(parameters.getZoom());
                this.mZoomRenderer.setZoomValue(((Integer) this.mZoomRatios.get(parameters.getZoom())).intValue());
                this.mZoomRenderer.setOnZoomChangeListener(new ZoomChangeListener());
            }
        }
    }

    public void overrideSettings(String... strArr) {
        PhotoMenu photoMenu = this.mMenu;
        if (photoMenu != null) {
            photoMenu.overrideSettings(strArr);
        }
    }

    public void updateOnScreenIndicators(Parameters parameters, PreferenceGroup preferenceGroup, ComboPreferences comboPreferences) {
        if (parameters != null && preferenceGroup != null) {
            this.mOnScreenIndicators.updateSceneOnScreenIndicator(parameters.getSceneMode());
            this.mOnScreenIndicators.updateExposureOnScreenIndicator(parameters, CameraSettings.readExposure(comboPreferences));
            this.mOnScreenIndicators.updateFlashOnScreenIndicator(parameters.getFlashMode());
            int i = -1;
            String str = "auto";
            if (str.equals(parameters.getSceneMode())) {
                str = parameters.getWhiteBalance();
            }
            ListPreference findPreference = preferenceGroup.findPreference("pref_camera_whitebalance_key");
            if (findPreference != null) {
                i = findPreference.findIndexOfValue(str);
            }
            OnScreenIndicators onScreenIndicators = this.mOnScreenIndicators;
            if (i < 0) {
                i = 2;
            }
            onScreenIndicators.updateWBIndicator(i);
            this.mOnScreenIndicators.updateLocationIndicator(RecordLocationPreference.get(comboPreferences, "pref_camera_recordlocation_key"));
        }
    }

    public void animateFlash() {
        this.mAnimationManager.startFlashAnimation(this.mFlashOverlay);
    }

    public void enableGestures(boolean z) {
        PreviewGestures previewGestures = this.mGestures;
        if (previewGestures != null) {
            previewGestures.setEnabled(z);
        }
    }

    public void onSingleTapUp(View view, int i, int i2) {
        this.mController.onSingleTapUp(view, i, i2);
    }

    public boolean onBackPressed() {
        PhotoMenu photoMenu = this.mMenu;
        if (photoMenu != null && photoMenu.handleBackKey()) {
            return true;
        }
        PieRenderer pieRenderer = this.mPieRenderer;
        if (pieRenderer != null && pieRenderer.showsItems()) {
            this.mPieRenderer.hide();
            return true;
        } else if (this.mController.isImageCaptureIntent()) {
            this.mController.onCaptureCancelled();
            return true;
        } else if (!this.mController.isCameraIdle()) {
            return true;
        } else {
            ModuleSwitcher moduleSwitcher = this.mSwitcher;
            if (moduleSwitcher == null || !moduleSwitcher.showsPopup()) {
                return false;
            }
            this.mSwitcher.closePopup();
            return true;
        }
    }

    public void onPreviewFocusChanged(boolean z) {
        if (z) {
            showUI();
        } else {
            hideUI();
        }
        FaceView faceView = this.mFaceView;
        if (faceView != null) {
            faceView.setBlockDraw(!z);
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
        setShowMenu(z);
        if (!z) {
            CountDownView countDownView = this.mCountDownView;
            if (countDownView != null) {
                countDownView.cancelCountDown();
            }
        }
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

    public void setMakeupMenuLayout(LinearLayout linearLayout) {
        this.mMakeupMenuLayout = linearLayout;
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
                this.mMenu.animateSlideIn(this.mMenuLayout, CameraActivity.SETTING_LIST_WIDTH_1, true);
            }
            if (i == 2) {
                this.mMenu.animateFadeIn(listView);
                return;
            }
            return;
        }
        listView.setAlpha(0.85f);
    }

    public void removeLevel2() {
        RotateLayout rotateLayout = this.mSubMenuLayout;
        if (rotateLayout != null) {
            this.mSubMenuLayout.removeView(rotateLayout.getChildAt(0));
        }
    }

    public void showPopup(AbstractSettingPopup abstractSettingPopup) {
        hideUI();
        if (this.mPopup == null) {
            this.mPopup = new PopupWindow(-2, -2);
            this.mPopup.setBackgroundDrawable(new ColorDrawable(0));
            this.mPopup.setOutsideTouchable(true);
            this.mPopup.setFocusable(true);
            this.mPopup.setOnDismissListener(new OnDismissListener() {
                public void onDismiss() {
                    PhotoUI.this.mPopup = null;
                    PhotoUI.this.mDismissAll = false;
                    PhotoUI.this.showUI();
                    PhotoUI.this.mActivity.setSystemBarsVisibility(false);
                }
            });
        }
        abstractSettingPopup.setVisibility(0);
        this.mPopup.setContentView(abstractSettingPopup);
        this.mPopup.showAtLocation(this.mRootView, 17, 0, 0);
    }

    public void cleanupListview() {
        showUI();
        this.mActivity.setSystemBarsVisibility(false);
    }

    public void dismissPopup() {
        PopupWindow popupWindow = this.mPopup;
        if (popupWindow != null && popupWindow.isShowing()) {
            this.mPopup.dismiss();
        }
    }

    public void dismissAllPopup() {
        this.mDismissAll = true;
        PopupWindow popupWindow = this.mPopup;
        if (popupWindow != null && popupWindow.isShowing()) {
            this.mPopup.dismiss();
        }
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
        LinearLayout linearLayout = this.mPreviewMenuLayout;
        if (linearLayout != null) {
            return linearLayout.dispatchTouchEvent(motionEvent);
        }
        return false;
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

    public void onShowSwitcherPopup() {
        PieRenderer pieRenderer = this.mPieRenderer;
        if (pieRenderer != null && pieRenderer.showsItems()) {
            this.mPieRenderer.hide();
        }
    }

    private void setShowMenu(boolean z) {
        OnScreenIndicators onScreenIndicators = this.mOnScreenIndicators;
        if (onScreenIndicators != null) {
            onScreenIndicators.setVisibility(z ? 0 : 8);
        }
    }

    public boolean collapseCameraControls() {
        boolean z;
        this.mSwitcher.closePopup();
        PhotoMenu photoMenu = this.mMenu;
        if (photoMenu != null) {
            photoMenu.removeAllView();
        }
        if (this.mPopup != null) {
            dismissAllPopup();
            z = true;
        } else {
            z = false;
        }
        onShowSwitcherPopup();
        this.mCameraControls.showRefocusToast(false);
        return z;
    }

    /* access modifiers changed from: protected */
    public void showCapturedImageForReview(byte[] bArr, int i, boolean z) {
        this.mCameraControls.hideCameraSettings();
        this.mDecodeTaskForReview = new DecodeImageForReview(bArr, i, z);
        this.mDecodeTaskForReview.execute(new Void[0]);
        this.mOnScreenIndicators.setVisibility(8);
        this.mMenuButton.setVisibility(8);
        CameraUtil.fadeIn(this.mReviewDoneButton);
        this.mShutterButton.setVisibility(4);
        CameraUtil.fadeIn(this.mReviewRetakeButton);
        setOrientation(this.mOrientation, true);
        this.mMenu.hideTopMenu(true);
        pauseFaceDetection();
    }

    /* access modifiers changed from: protected */
    public void hidePostCaptureAlert() {
        this.mCameraControls.showCameraSettings();
        DecodeImageForReview decodeImageForReview = this.mDecodeTaskForReview;
        if (decodeImageForReview != null) {
            decodeImageForReview.cancel(true);
        }
        this.mReviewImage.setVisibility(8);
        this.mOnScreenIndicators.setVisibility(0);
        this.mMenuButton.setVisibility(0);
        PhotoMenu photoMenu = this.mMenu;
        if (photoMenu != null) {
            photoMenu.hideTopMenu(false);
        }
        CameraUtil.fadeOut(this.mReviewDoneButton);
        this.mShutterButton.setVisibility(0);
        CameraUtil.fadeOut(this.mReviewRetakeButton);
        resumeFaceDetection();
    }

    public void setDisplayOrientation(int i) {
        FaceView faceView = this.mFaceView;
        if (faceView != null) {
            faceView.setDisplayOrientation(i);
        }
        int i2 = this.mPreviewOrientation;
        if (i2 == -1 || i2 != i) {
            PhotoMenu photoMenu = this.mMenu;
            if (photoMenu != null && photoMenu.isPreviewMenuBeingShown()) {
                dismissSceneModeMenu();
                this.mMenu.addModeBack();
            }
        }
        this.mPreviewOrientation = i;
    }

    public boolean isShutterPressed() {
        return this.mShutterButton.isPressed();
    }

    public void enableShutter(boolean z) {
        ShutterButton shutterButton = this.mShutterButton;
        if (shutterButton != null) {
            shutterButton.setEnabled(z);
        }
    }

    public void pressShutterButton() {
        if (this.mShutterButton.isInTouchMode()) {
            this.mShutterButton.requestFocusFromTouch();
        } else {
            this.mShutterButton.requestFocus();
        }
        this.mShutterButton.setPressed(true);
    }

    public void onPieOpened(int i, int i2) {
        setSwipingEnabled(false);
        FaceView faceView = this.mFaceView;
        if (faceView != null) {
            faceView.setBlockDraw(true);
        }
        this.mSwitcher.closePopup();
        if (this.mIsBokehMode) {
            SeekBar seekBar = this.mBlurDegreeProgressBar;
            if (seekBar != null) {
                seekBar.setVisibility(0);
            }
        }
    }

    public void onPieClosed() {
        setSwipingEnabled(true);
        FaceView faceView = this.mFaceView;
        if (faceView != null) {
            faceView.setBlockDraw(false);
        }
        SeekBar seekBar = this.mBlurDegreeProgressBar;
        if (seekBar != null) {
            seekBar.setVisibility(8);
        }
    }

    public void onPieMoved(int i, int i2) {
        int i3;
        Size bokehFocusSize = this.mPieRenderer.getBokehFocusSize();
        if (i2 > this.mPieRenderer.getHeight() / 2) {
            i3 = (i2 - (bokehFocusSize.getHeight() / 2)) - this.mBlurDegreeProgressBar.getHeight();
        } else {
            i3 = i2 + (bokehFocusSize.getHeight() / 2);
        }
        SeekBar seekBar = this.mBlurDegreeProgressBar;
        seekBar.setX((float) (i - (seekBar.getWidth() / 2)));
        this.mBlurDegreeProgressBar.setY((float) i3);
        if (this.mIsBokehMode && this.mBlurDegreeProgressBar.getVisibility() != 0 && this.mPieRenderer.isVisible()) {
            this.mBlurDegreeProgressBar.setVisibility(0);
        }
    }

    public void enableBokehRender(boolean z) {
        PieRenderer pieRenderer = this.mPieRenderer;
        if (pieRenderer != null) {
            pieRenderer.setBokehMode(z);
            this.mIsBokehMode = z;
        }
    }

    public void enableBokehFocus(boolean z) {
        PieRenderer pieRenderer = this.mPieRenderer;
        if (pieRenderer != null && this.mIsBokehMode) {
            pieRenderer.setBokehMode(z);
            if (this.mBlurDegreeProgressBar == null) {
                return;
            }
            if (!z || !this.mPieRenderer.isVisible()) {
                this.mBlurDegreeProgressBar.setVisibility(8);
            } else {
                this.mBlurDegreeProgressBar.setVisibility(0);
            }
        }
    }

    public void setBokehRenderDegree(int i) {
        PieRenderer pieRenderer = this.mPieRenderer;
        if (pieRenderer != null) {
            pieRenderer.setBokehDegree(i);
        }
    }

    public void setSwipingEnabled(boolean z) {
        this.mActivity.setSwipingEnabled(z);
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

    private void initializeCountDown() {
        this.mActivity.getLayoutInflater().inflate(C0905R.layout.count_down_to_capture, (ViewGroup) this.mRootView, true);
        this.mCountDownView = (CountDownView) this.mRootView.findViewById(C0905R.C0907id.count_down_to_capture);
        this.mCountDownView.setCountDownFinishedListener((OnCountDownFinishedListener) this.mController);
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

    public void startCountDown(int i, boolean z) {
        if (this.mCountDownView == null) {
            initializeCountDown();
        }
        this.mCountDownView.startCountDown(i, z);
        hideUIWhileCountDown();
    }

    public void startSelfieFlash() {
        if (this.mSelfieView == null) {
            this.mSelfieView = (SelfieFlashView) this.mRootView.findViewById(C0905R.C0907id.selfie_flash);
        }
        this.mSelfieView.bringToFront();
        this.mSelfieView.open();
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
        WindowManager.LayoutParams attributes = window.getAttributes();
        float f2 = attributes.screenBrightness;
        attributes.screenBrightness = f;
        window.setAttributes(attributes);
        return f2;
    }

    public void showPreferencesToast() {
        if (this.mNotSelectableToast == null) {
            this.mNotSelectableToast = RotateTextToast.makeText((Activity) this.mActivity, (CharSequence) this.mActivity.getResources().getString(C0905R.string.not_selectable_in_scene_mode), 0);
        }
        this.mNotSelectableToast.show();
    }

    public void showPreviewCover() {
        this.mPreviewCover.setVisibility(0);
    }

    public void hidePreviewCover() {
        if (this.mPreviewCover.getVisibility() != 8) {
            this.mPreviewCover.setVisibility(8);
        }
    }

    public boolean isPreviewCoverVisible() {
        View view = this.mPreviewCover;
        return view != null && view.getVisibility() == 0;
    }

    public void onPause() {
        cancelCountDown();
        collapseCameraControls();
        FaceView faceView = this.mFaceView;
        if (faceView != null) {
            faceView.clear();
        }
        AlertDialog alertDialog = this.mLocationDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            this.mLocationDialog.dismiss();
        }
        this.mLocationDialog = null;
        PhotoMenu photoMenu = this.mMenu;
        if (photoMenu != null) {
            photoMenu.animateSlideOutPreviewMenu();
        }
    }

    public void initDisplayChangeListener() {
        ((CameraRootView) this.mRootView).setDisplayChangeListener(this);
    }

    public void removeDisplayChangeListener() {
        ((CameraRootView) this.mRootView).removeDisplayChangeListener();
    }

    private FocusIndicator getFocusIndicator() {
        FaceView faceView = this.mFaceView;
        return (faceView == null || !faceView.faceExists()) ? this.mPieRenderer : this.mFaceView;
    }

    public boolean hasFaces() {
        FaceView faceView = this.mFaceView;
        return faceView != null && faceView.faceExists();
    }

    public void clearFaces() {
        FaceView faceView = this.mFaceView;
        if (faceView != null) {
            faceView.clear();
        }
    }

    public void clearFocus() {
        PieRenderer pieRenderer = this.mPieRenderer;
        if (hasFaces()) {
            this.mFaceView.showStart();
        }
        if (pieRenderer != null) {
            pieRenderer.clear();
        }
    }

    public void setFocusPosition(int i, int i2) {
        this.mPieRenderer.setFocus(i, i2);
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

    public void onFaceDetection(Face[] faceArr, CameraProxy cameraProxy) {
        if (this.mIsBokehMode) {
            this.mFaceView.clear();
        } else {
            this.mFaceView.setFaces(faceArr);
        }
    }

    public void onDisplayChanged() {
        Log.d(TAG, "Device flip detected.");
        this.mCameraControls.checkLayoutFlip();
        this.mController.updateCameraOrientation();
    }

    public void setPreference(String str, String str2) {
        this.mMenu.setPreference(str, str2);
    }

    public void updateRemainingPhotos(int i) {
        this.mCameraControls.updateRemainingPhotos(i);
    }

    public void setOrientation(int i, boolean z) {
        this.mOrientation = i;
        this.mCameraControls.setOrientation(i, z);
        MenuHelp menuHelp = this.mMenuHelp;
        if (menuHelp != null) {
            menuHelp.setOrientation(i, z);
        }
        RotateLayout rotateLayout = this.mMenuLayout;
        if (rotateLayout != null) {
            rotateLayout.setOrientation(i, z);
        }
        RotateLayout rotateLayout2 = this.mSubMenuLayout;
        if (rotateLayout2 != null) {
            rotateLayout2.setOrientation(i, z);
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
        LinearLayout linearLayout2 = this.mMakeupMenuLayout;
        if (linearLayout2 != null) {
            if (linearLayout2.getChildAt(0) instanceof RotateLayout) {
                for (int childCount2 = this.mMakeupMenuLayout.getChildCount() - 1; childCount2 >= 0; childCount2--) {
                    ((RotateLayout) this.mMakeupMenuLayout.getChildAt(childCount2)).setOrientation(i, z);
                }
            } else {
                ViewGroup viewGroup2 = (ViewGroup) this.mMakeupMenuLayout.getChildAt(1);
                if (viewGroup2 != null) {
                    for (int childCount3 = viewGroup2.getChildCount() - 1; childCount3 >= 0; childCount3--) {
                        ViewGroup viewGroup3 = (ViewGroup) viewGroup2.getChildAt(childCount3);
                        if (viewGroup3 instanceof RotateLayout) {
                            ((RotateLayout) viewGroup3).setOrientation(i, z);
                        }
                    }
                }
            }
        }
        CountDownView countDownView = this.mCountDownView;
        if (countDownView != null) {
            countDownView.setOrientation(i);
        }
        RotateTextToast.setOrientation(i);
        FaceView faceView = this.mFaceView;
        if (faceView != null) {
            faceView.setDisplayRotation(i);
        }
        ZoomRenderer zoomRenderer = this.mZoomRenderer;
        if (zoomRenderer != null) {
            zoomRenderer.setOrientation(i);
        }
        ImageView imageView = this.mReviewImage;
        if (imageView != null) {
            ((RotateImageView) imageView).setOrientation(i, z);
        }
    }

    public void tryToCloseSubList() {
        PhotoMenu photoMenu = this.mMenu;
        if (photoMenu != null) {
            photoMenu.tryToCloseSubList();
        }
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public void adjustOrientation() {
        setOrientation(this.mOrientation, true);
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

    public void showRefocusDialog() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mActivity);
        String str = CameraSettings.KEY_REFOCUS_PROMPT;
        if (defaultSharedPreferences.getInt(str, 1) == 1) {
            new Builder(this.mActivity).setTitle(C0905R.string.refocus_prompt_title).setMessage(C0905R.string.refocus_prompt_message).setPositiveButton(C0905R.string.dialog_ok, null).show();
            Editor edit = defaultSharedPreferences.edit();
            edit.putInt(str, 0);
            edit.apply();
        }
    }

    public void hideUIWhileCountDown() {
        this.mMenu.hideCameraControls(true);
        this.mGestures.setZoomOnly(true);
    }

    public void showUIAfterCountDown() {
        this.mMenu.hideCameraControls(false);
        this.mGestures.setZoomOnly(false);
    }
}
