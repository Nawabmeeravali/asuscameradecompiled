package com.android.camera;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.camera.PanoProgressBar.OnDirectionChangeListener;
import com.android.camera.ShutterButton.OnShutterButtonListener;
import com.android.camera.p004ui.CameraControls;
import com.android.camera.p004ui.CameraRootView;
import com.android.camera.p004ui.CameraRootView.MyDisplayListener;
import com.android.camera.p004ui.ModuleSwitcher;
import com.android.camera.p004ui.RotateImageView;
import com.android.camera.p004ui.RotateLayout;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.util.CameraUtil;
import org.codeaurora.snapcam.C0905R;

public class WideAnglePanoramaUI implements SurfaceTextureListener, OnShutterButtonListener, MyDisplayListener, OnLayoutChangeListener {
    private static final String TAG = "CAM_WidePanoramaUI";
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    private CameraControls mCameraControls;
    private View mCaptureIndicator;
    private FrameLayout mCaptureLayout;
    private PanoProgressBar mCaptureProgressBar;
    /* access modifiers changed from: private */
    public boolean mConfigChange = false;
    /* access modifiers changed from: private */
    public WideAnglePanoramaController mController;
    private DialogHelper mDialogHelper;
    private int mIndicatorColor;
    private int mIndicatorColorFast;
    private View mLeftIndicator;
    /* access modifiers changed from: private */
    public int mOrientation;
    /* access modifiers changed from: private */
    public Button mPanoFailedButton;
    /* access modifiers changed from: private */
    public RotateLayout mPanoFailedDialog;
    private View mPreviewBorder;
    private View mPreviewCover;
    private View mPreviewLayout;
    private int mPreviewYOffset;
    private float[] mProgressAngle = new float[2];
    private Matrix mProgressDirectionMatrix = new Matrix();
    private ImageView mReview;
    private int mReviewBackground;
    private ViewGroup mReviewControl;
    private View mReviewLayout;
    private View mRightIndicator;
    private ViewGroup mRootView;
    private PanoProgressBar mSavingProgressBar;
    private ShutterButton mShutterButton;
    private SurfaceTexture mSurfaceTexture;
    /* access modifiers changed from: private */
    public ModuleSwitcher mSwitcher;
    private TextureView mTextureView;
    private ImageView mThumbnail;
    private Bitmap mThumbnailBitmap;
    private TextView mTooFastPrompt;
    /* access modifiers changed from: private */
    public RotateLayout mWaitingDialog;

    private class DialogHelper {
        DialogHelper() {
        }

        public void dismissAll() {
            if (WideAnglePanoramaUI.this.mPanoFailedDialog != null) {
                WideAnglePanoramaUI.this.mPanoFailedDialog.setVisibility(4);
            }
            if (WideAnglePanoramaUI.this.mWaitingDialog != null) {
                WideAnglePanoramaUI.this.mWaitingDialog.setVisibility(4);
            }
        }

        public void showAlertDialog(CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, final Runnable runnable) {
            dismissAll();
            WideAnglePanoramaUI.this.mPanoFailedButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    runnable.run();
                    WideAnglePanoramaUI.this.mPanoFailedDialog.setVisibility(4);
                }
            });
            WideAnglePanoramaUI.this.mPanoFailedDialog.setVisibility(0);
        }

        public void showWaitingDialog(CharSequence charSequence) {
            dismissAll();
            WideAnglePanoramaUI.this.mWaitingDialog.setVisibility(0);
        }
    }

    private static class FlipBitmapDrawable extends BitmapDrawable {
        public FlipBitmapDrawable(Resources resources, Bitmap bitmap) {
            super(resources, bitmap);
        }

        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            int centerX = bounds.centerX();
            int centerY = bounds.centerY();
            canvas.save(1);
            canvas.rotate(180.0f, (float) centerX, (float) centerY);
            super.draw(canvas);
            canvas.restore();
        }
    }

    public void onShutterButtonFocus(boolean z) {
    }

    public void onShutterButtonLongClick() {
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    public WideAnglePanoramaUI(CameraActivity cameraActivity, WideAnglePanoramaController wideAnglePanoramaController, ViewGroup viewGroup) {
        this.mActivity = cameraActivity;
        this.mController = wideAnglePanoramaController;
        this.mRootView = viewGroup;
        createContentView();
        this.mSwitcher = (ModuleSwitcher) this.mRootView.findViewById(C0905R.C0907id.camera_switcher);
        this.mSwitcher.setCurrentIndex(2);
        this.mSwitcher.setSwitchListener(this.mActivity);
        if (!this.mActivity.isSecureCamera()) {
            this.mThumbnail = (ImageView) this.mRootView.findViewById(C0905R.C0907id.preview_thumb);
            this.mThumbnail.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (!CameraControls.isAnimating()) {
                        WideAnglePanoramaUI.this.mActivity.gotoGallery();
                    }
                }
            });
        }
        this.mSwitcher.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                WideAnglePanoramaUI.this.mSwitcher.showPopup();
                WideAnglePanoramaUI.this.mSwitcher.setOrientation(WideAnglePanoramaUI.this.mOrientation, false);
            }
        });
        ((RotateImageView) this.mRootView.findViewById(C0905R.C0907id.mute_button)).setVisibility(8);
    }

    public void onStartCapture() {
        hideSwitcher();
        this.mShutterButton.setImageResource(C0905R.C0906drawable.shutter_button_stop);
        this.mCaptureIndicator.setVisibility(0);
        showDirectionIndicators(0);
    }

    public void showPreviewUI() {
        this.mCaptureLayout.setVisibility(0);
        showUI();
    }

    public void onStopCapture() {
        this.mCaptureIndicator.setVisibility(4);
        hideTooFastIndication();
        hideDirectionIndicators();
    }

    public void hideSwitcher() {
        this.mSwitcher.closePopup();
        this.mSwitcher.setVisibility(4);
    }

    public void hideUI() {
        hideSwitcher();
        this.mCameraControls.setVisibility(4);
    }

    public void showUI() {
        showSwitcher();
        this.mCameraControls.setVisibility(0);
    }

    public void onPreviewFocusChanged(boolean z) {
        if (z) {
            showUI();
        } else {
            hideUI();
        }
    }

    public boolean arePreviewControlsVisible() {
        return this.mCameraControls.getVisibility() == 0;
    }

    public void showSwitcher() {
        this.mSwitcher.setVisibility(0);
    }

    public void setSwitcherIndex() {
        this.mSwitcher.setCurrentIndex(2);
    }

    public void setCaptureProgressOnDirectionChangeListener(OnDirectionChangeListener onDirectionChangeListener) {
        this.mCaptureProgressBar.setOnDirectionChangeListener(onDirectionChangeListener);
    }

    public void resetCaptureProgress() {
        this.mCaptureProgressBar.reset();
    }

    public void setMaxCaptureProgress(int i) {
        this.mCaptureProgressBar.setMaxProgress(i);
    }

    public void showCaptureProgress() {
        this.mCaptureProgressBar.setVisibility(0);
    }

    public void updateCaptureProgress(float f, float f2, float f3, float f4, float f5) {
        float f6;
        if (Math.abs(f) > f5 || Math.abs(f2) > f5) {
            showTooFastIndication();
        } else {
            hideTooFastIndication();
        }
        float[] fArr = this.mProgressAngle;
        fArr[0] = f3;
        fArr[1] = f4;
        this.mProgressDirectionMatrix.mapPoints(fArr);
        if (Math.abs(this.mProgressAngle[0]) > Math.abs(this.mProgressAngle[1])) {
            f6 = this.mProgressAngle[0];
        } else {
            f6 = this.mProgressAngle[1];
        }
        this.mCaptureProgressBar.setProgress((int) f6);
    }

    public void setProgressOrientation(int i) {
        this.mProgressDirectionMatrix.reset();
        this.mProgressDirectionMatrix.postRotate((float) i);
    }

    public void showDirectionIndicators(int i) {
        if (i == 0) {
            this.mLeftIndicator.setVisibility(0);
            this.mRightIndicator.setVisibility(0);
        } else if (i == 1) {
            this.mLeftIndicator.setVisibility(0);
            this.mRightIndicator.setVisibility(4);
        } else if (i == 2) {
            this.mLeftIndicator.setVisibility(4);
            this.mRightIndicator.setVisibility(0);
        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.mSurfaceTexture;
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        this.mSurfaceTexture = surfaceTexture;
        this.mController.onPreviewUIReady();
        this.mActivity.updateThumbnail(this.mThumbnail);
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        this.mController.onPreviewUIDestroyed();
        this.mSurfaceTexture = null;
        Log.d(TAG, "surfaceTexture is destroyed");
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        if (this.mPreviewCover.getVisibility() != 8 && !this.mConfigChange) {
            this.mPreviewCover.setVisibility(8);
        }
    }

    private void hideDirectionIndicators() {
        this.mLeftIndicator.setVisibility(4);
        this.mRightIndicator.setVisibility(4);
    }

    public Point getPreviewAreaSize() {
        return new Point(this.mTextureView.getWidth(), this.mTextureView.getHeight());
    }

    public void reset() {
        this.mShutterButton.setImageResource(C0905R.C0906drawable.btn_new_shutter_panorama);
        this.mReviewLayout.setVisibility(8);
        this.mCaptureProgressBar.setVisibility(4);
    }

    public void saveFinalMosaic(Bitmap bitmap, int i) {
        if (!(bitmap == null || i == 0)) {
            Matrix matrix = new Matrix();
            matrix.setRotate((float) i);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        }
        this.mReview.setImageBitmap(bitmap);
        this.mCaptureLayout.setVisibility(8);
        this.mReviewLayout.setVisibility(0);
        this.mSavingProgressBar.requestLayout();
        this.mThumbnailBitmap = bitmap;
    }

    public void showFinalMosaic() {
        Bitmap bitmap = this.mThumbnailBitmap;
        if (bitmap != null) {
            this.mActivity.updateThumbnail(bitmap);
            this.mThumbnailBitmap.recycle();
            this.mThumbnailBitmap = null;
        }
    }

    public void onConfigurationChanged(Configuration configuration, boolean z) {
        if (configuration.orientation == 2) {
            showPreviewCover();
            this.mConfigChange = true;
        } else {
            this.mPreviewCover.postDelayed(new Runnable() {
                public void run() {
                    WideAnglePanoramaUI.this.mConfigChange = false;
                }
            }, 300);
        }
        Drawable drawable = null;
        if (z) {
            drawable = this.mReview.getDrawable();
        }
        LayoutInflater layoutInflater = (LayoutInflater) this.mActivity.getSystemService("layout_inflater");
        this.mReviewControl.removeAllViews();
        this.mReviewControl.clearDisappearingChildren();
        layoutInflater.inflate(C0905R.layout.pano_review_control, this.mReviewControl, true);
        this.mRootView.bringChildToFront(this.mCameraControls);
        setViews(this.mActivity.getResources());
        if (z) {
            this.mReview.setImageDrawable(drawable);
            this.mCaptureLayout.setVisibility(8);
            this.mReviewLayout.setVisibility(0);
        }
    }

    private void setPanoramaPreviewView() {
        this.mActivity.getWindowManager().getDefaultDisplay().getRotation();
        Display defaultDisplay = this.mActivity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        int i = point.x;
        int i2 = (i * 4) / 3;
        int i3 = (point.y - i2) / 2;
        LayoutParams layoutParams = new LayoutParams(i, i2);
        this.mTextureView.setLayoutParams(layoutParams);
        float f = (float) 0;
        this.mTextureView.setX(f);
        float f2 = (float) i3;
        this.mTextureView.setY(f2);
        this.mPreviewBorder.setLayoutParams(layoutParams);
        this.mPreviewBorder.setX(f);
        this.mPreviewBorder.setY(f2);
        this.mPreviewYOffset = i3;
        this.mTextureView.getBottom();
        this.mTextureView.getRight();
        this.mTextureView.getBottom();
        this.mCameraControls.setPreviewRatio(1.0f, true);
    }

    public void resetSavingProgress() {
        this.mSavingProgressBar.reset();
        this.mSavingProgressBar.setRightIncreasing(true);
    }

    public void updateSavingProgress(int i) {
        this.mSavingProgressBar.setProgress(i);
    }

    public void onShutterButtonClick() {
        this.mController.onShutterButtonClick();
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        this.mController.onPreviewUILayoutChange(i, i2, i3, i4);
    }

    public void showAlertDialog(String str, String str2, String str3, Runnable runnable) {
        this.mDialogHelper.showAlertDialog(str, str2, str3, runnable);
    }

    public void showWaitingDialog(String str) {
        this.mDialogHelper.showWaitingDialog(str);
    }

    public void dismissAllDialogs() {
        this.mDialogHelper.dismissAll();
    }

    private void createContentView() {
        ((LayoutInflater) this.mActivity.getSystemService("layout_inflater")).inflate(C0905R.layout.panorama_module, this.mRootView, true);
        Resources resources = this.mActivity.getResources();
        this.mIndicatorColor = resources.getColor(C0905R.color.pano_progress_indication);
        this.mReviewBackground = resources.getColor(C0905R.color.review_background);
        this.mIndicatorColorFast = resources.getColor(C0905R.color.pano_progress_indication_fast);
        this.mPreviewCover = this.mRootView.findViewById(C0905R.C0907id.preview_cover);
        this.mPreviewLayout = this.mRootView.findViewById(C0905R.C0907id.pano_preview_layout);
        this.mReviewControl = (ViewGroup) this.mRootView.findViewById(C0905R.C0907id.pano_review_control);
        this.mReviewLayout = this.mRootView.findViewById(C0905R.C0907id.pano_review_layout);
        this.mReview = (ImageView) this.mRootView.findViewById(C0905R.C0907id.pano_reviewarea);
        this.mCaptureLayout = (FrameLayout) this.mRootView.findViewById(C0905R.C0907id.panorama_capture_layout);
        this.mCaptureProgressBar = (PanoProgressBar) this.mRootView.findViewById(C0905R.C0907id.pano_pan_progress_bar);
        this.mCaptureProgressBar.setBackgroundColor(resources.getColor(C0905R.color.pano_progress_empty));
        this.mCaptureProgressBar.setDoneColor(resources.getColor(C0905R.color.pano_progress_done));
        this.mCaptureProgressBar.setIndicatorColor(this.mIndicatorColor);
        this.mCaptureProgressBar.setIndicatorWidth(20.0f);
        this.mPreviewBorder = this.mCaptureLayout.findViewById(C0905R.C0907id.pano_preview_area_border);
        this.mLeftIndicator = this.mRootView.findViewById(C0905R.C0907id.pano_pan_left_indicator);
        this.mRightIndicator = this.mRootView.findViewById(C0905R.C0907id.pano_pan_right_indicator);
        this.mLeftIndicator.setEnabled(false);
        this.mRightIndicator.setEnabled(false);
        this.mTooFastPrompt = (TextView) this.mRootView.findViewById(C0905R.C0907id.pano_capture_too_fast_textview);
        this.mCaptureIndicator = this.mRootView.findViewById(C0905R.C0907id.pano_capture_indicator);
        this.mShutterButton = (ShutterButton) this.mRootView.findViewById(C0905R.C0907id.shutter_button);
        this.mShutterButton.setImageResource(C0905R.C0906drawable.btn_new_shutter);
        this.mShutterButton.setOnShutterButtonListener(this);
        this.mRootView.findViewById(C0905R.C0907id.menu).setVisibility(8);
        this.mRootView.findViewById(C0905R.C0907id.on_screen_indicators).setVisibility(8);
        this.mReview.setBackgroundColor(this.mReviewBackground);
        ((CameraRootView) this.mRootView).setDisplayChangeListener(null);
        this.mTextureView = (TextureView) this.mRootView.findViewById(C0905R.C0907id.pano_preview_textureview);
        this.mTextureView.setSurfaceTextureListener(this);
        this.mTextureView.addOnLayoutChangeListener(this);
        this.mCameraControls = (CameraControls) this.mRootView.findViewById(C0905R.C0907id.camera_controls);
        setPanoramaPreviewView();
        this.mWaitingDialog = (RotateLayout) this.mRootView.findViewById(C0905R.C0907id.waitingDialog);
        this.mPanoFailedDialog = (RotateLayout) this.mRootView.findViewById(C0905R.C0907id.pano_dialog_layout);
        this.mPanoFailedButton = (Button) this.mRootView.findViewById(C0905R.C0907id.pano_dialog_button1);
        this.mDialogHelper = new DialogHelper();
        setViews(resources);
    }

    private void setViews(Resources resources) {
        resources.getInteger(C0905R.integer.SRI_pano_layout_weight);
        this.mSavingProgressBar = (PanoProgressBar) this.mRootView.findViewById(C0905R.C0907id.pano_saving_progress_bar);
        this.mSavingProgressBar.setIndicatorWidth(0.0f);
        this.mSavingProgressBar.setMaxProgress(100);
        this.mSavingProgressBar.setBackgroundColor(resources.getColor(C0905R.color.pano_progress_empty));
        this.mSavingProgressBar.setDoneColor(resources.getColor(C0905R.color.pano_progress_indication));
        this.mRootView.findViewById(C0905R.C0907id.pano_review_cancel_button).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                WideAnglePanoramaUI.this.mController.cancelHighResStitching();
            }
        });
    }

    private void showTooFastIndication() {
        this.mTooFastPrompt.setVisibility(0);
        this.mPreviewBorder.setVisibility(0);
        this.mCaptureProgressBar.setIndicatorColor(this.mIndicatorColorFast);
        this.mLeftIndicator.setEnabled(true);
        this.mRightIndicator.setEnabled(true);
    }

    private void hideTooFastIndication() {
        this.mTooFastPrompt.setVisibility(8);
        this.mPreviewBorder.setVisibility(4);
        this.mCaptureProgressBar.setIndicatorColor(this.mIndicatorColor);
        this.mLeftIndicator.setEnabled(false);
        this.mRightIndicator.setEnabled(false);
    }

    public void flipPreviewIfNeeded() {
        if (((this.mController.getCameraOrientation() - CameraUtil.getDisplayRotation(this.mActivity)) + 360) % 360 >= 180) {
            this.mTextureView.setRotation(180.0f);
        } else {
            this.mTextureView.setRotation(0.0f);
        }
    }

    public void onDisplayChanged() {
        this.mCameraControls.checkLayoutFlip();
        flipPreviewIfNeeded();
    }

    public void initDisplayChangeListener() {
        ((CameraRootView) this.mRootView).setDisplayChangeListener(this);
    }

    public void removeDisplayChangeListener() {
        ((CameraRootView) this.mRootView).removeDisplayChangeListener();
    }

    public void showPreviewCover() {
        this.mPreviewCover.setVisibility(0);
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
        char c;
        char c2;
        int i2;
        char c3;
        int i3 = i;
        this.mOrientation = i3;
        View findViewById = this.mRootView.findViewById(C0905R.C0907id.pano_dummy_layout);
        int top = findViewById.getTop();
        int bottom = findViewById.getBottom();
        int right = findViewById.getRight();
        findViewById.getBottom();
        FrameLayout frameLayout = (FrameLayout) this.mRootView.findViewById(C0905R.C0907id.pano_progress_layout);
        char c4 = 0;
        int i4 = 2;
        int paddingTop = frameLayout.getPaddingTop() + (frameLayout.getChildAt(0).getHeight() / 2);
        int i5 = right / 2;
        int[] iArr = {i5, right / 10, (right * 9) / 10, i5};
        int i6 = (top + bottom) / 2;
        int[] iArr2 = {(top / 2) + paddingTop, i6, i6, bottom + paddingTop};
        int i7 = 81;
        if (i3 == 90) {
            i7 = 53;
            c = 2;
            c2 = 1;
        } else if (i3 == 180) {
            c2 = 3;
            c = 0;
        } else if (i3 != 270) {
            c = 3;
            c2 = 0;
        } else {
            i7 = 53;
            c2 = 2;
            c = 1;
        }
        View[] viewArr = {(View) this.mCaptureIndicator.getParent(), this.mRootView.findViewById(C0905R.C0907id.pano_review_indicator)};
        int length = viewArr.length;
        int i8 = 0;
        while (i8 < length) {
            View view = viewArr[i8];
            view.setTranslationX((float) (iArr[c2] - iArr[c4]));
            view.setTranslationY((float) (iArr2[c2] - iArr2[c4]));
            if (VERSION.SDK_INT >= 21) {
                try {
                    c3 = c2;
                    i2 = length;
                    try {
                        Class.forName("android.view.View").getMethod("setTranslationZ", new Class[]{Float.TYPE}).invoke(view, new Object[]{Integer.valueOf(1)});
                    } catch (Exception unused) {
                    }
                } catch (Exception unused2) {
                }
                view.setRotation((float) (-i3));
                i8++;
                c2 = c3;
                length = i2;
                c4 = 0;
                i4 = 2;
            }
            c3 = c2;
            i2 = length;
            view.setRotation((float) (-i3));
            i8++;
            c2 = c3;
            length = i2;
            c4 = 0;
            i4 = 2;
        }
        View[] viewArr2 = new View[i4];
        viewArr2[0] = frameLayout;
        viewArr2[1] = this.mReviewControl;
        for (View view2 : viewArr2) {
            view2.setPivotX((float) i5);
            view2.setPivotY((float) paddingTop);
            view2.setTranslationX((float) (iArr[c] - iArr[3]));
            view2.setTranslationY((float) (iArr2[c] - iArr2[3]));
            view2.setRotation((float) (-i3));
        }
        View findViewById2 = this.mReviewControl.findViewById(C0905R.C0907id.pano_review_cancel_button);
        LayoutParams layoutParams = (LayoutParams) findViewById2.getLayoutParams();
        layoutParams.gravity = i7;
        findViewById2.setLayoutParams(layoutParams);
        float f = (float) (-i3);
        this.mWaitingDialog.setRotation(f);
        this.mPanoFailedDialog.setRotation(f);
        this.mReview.setRotation(f);
        this.mTooFastPrompt.setRotation(f);
        this.mCameraControls.setOrientation(i3, z);
        RotateTextToast.setOrientation(i);
    }
}
