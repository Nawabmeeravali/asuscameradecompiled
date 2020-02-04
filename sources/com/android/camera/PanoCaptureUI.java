package com.android.camera;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera.Face;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.camera.CameraManager.CameraFaceDetectionCallback;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.LocationManager.Listener;
import com.android.camera.p004ui.AutoFitSurfaceView;
import com.android.camera.p004ui.CameraControls;
import com.android.camera.p004ui.CameraRootView.MyDisplayListener;
import com.android.camera.p004ui.FocusIndicator;
import com.android.camera.p004ui.ModuleSwitcher;
import com.android.camera.p004ui.PanoCaptureProcessView;
import com.android.camera.p004ui.RotateLayout;
import com.android.camera.util.CameraUtil;
import org.codeaurora.snapcam.C0905R;

public class PanoCaptureUI implements Callback, Listener, MyDisplayListener, CameraFaceDetectionCallback {
    private static final String TAG = "SnapCam_PanoCaptureUI";
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    private int mBottomMargin = 0;
    private CameraControls mCameraControls;
    /* access modifiers changed from: private */
    public PanoCaptureModule mController;
    /* access modifiers changed from: private */
    public boolean mIsSceneModeLabelClose = false;
    private OnLayoutChangeListener mLayoutListener = new OnLayoutChangeListener() {
        public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            int i9 = i3 - i;
            int i10 = i4 - i2;
            Size pictureOutputSize = PanoCaptureUI.this.mController.getPictureOutputSize();
            LayoutParams layoutParams = new LayoutParams(i9, i10, 17);
            PanoCaptureUI.this.mPreviewProcessView.setLayoutParams(layoutParams);
            PanoCaptureUI.this.mPreviewProcessView.setPanoPreviewSize(layoutParams.width, layoutParams.height, pictureOutputSize.getWidth(), pictureOutputSize.getHeight());
        }
    };
    private Matrix mMatrix = null;
    private OnScreenIndicators mOnScreenIndicators;
    private int mOrientation;
    /* access modifiers changed from: private */
    public PanoCaptureProcessView mPreviewProcessView;
    private View mRootView;
    /* access modifiers changed from: private */
    public AlertDialog mSceneModeInstructionalDialog = null;
    private ImageView mSceneModeLabelCloseIcon;
    /* access modifiers changed from: private */
    public RotateLayout mSceneModeLabelRect;
    private LinearLayout mSceneModeLabelView;
    private TextView mSceneModeName;
    /* access modifiers changed from: private */
    public ShutterButton mShutterButton;
    private SurfaceHolder mSurfaceHolder;
    private int mSurfaceMode = 0;
    private AutoFitSurfaceView mSurfaceView = null;
    private ModuleSwitcher mSwitcher;
    /* access modifiers changed from: private */
    public ImageView mThumbnail;
    private int mTopMargin = 0;
    private boolean mUIhidden = false;

    private FocusIndicator getFocusIndicator() {
        return null;
    }

    public void onCameraOpened() {
    }

    public void onErrorListener(int i) {
    }

    public void onFaceDetection(Face[] faceArr, CameraProxy cameraProxy) {
    }

    public void overrideSettings(String... strArr) {
    }

    public void clearSurfaces() {
        this.mSurfaceHolder = null;
    }

    public boolean isPanoCompleting() {
        return this.mPreviewProcessView.isPanoCompleting();
    }

    public boolean isFrameProcessing() {
        return this.mPreviewProcessView.isFrameProcessing();
    }

    public void onFrameAvailable(Bitmap bitmap, boolean z) {
        this.mPreviewProcessView.onFrameAvailable(bitmap, z);
    }

    public void onPanoStatusChange(final boolean z) {
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (z) {
                    if (PanoCaptureUI.this.mThumbnail != null) {
                        PanoCaptureUI.this.mThumbnail.setVisibility(8);
                    }
                    if (PanoCaptureUI.this.mShutterButton != null) {
                        PanoCaptureUI.this.mShutterButton.setImageResource(C0905R.C0906drawable.shutter_button_video_stop);
                        return;
                    }
                    return;
                }
                if (PanoCaptureUI.this.mThumbnail != null) {
                    PanoCaptureUI.this.mThumbnail.setVisibility(0);
                }
                if (PanoCaptureUI.this.mShutterButton != null) {
                    PanoCaptureUI.this.mShutterButton.setImageResource(C0905R.C0906drawable.btn_new_shutter_panorama);
                }
            }
        });
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001c, code lost:
        if (r4.mSurfaceMode != 1) goto L_0x001e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0022, code lost:
        if (r4.mSurfaceMode == 2) goto L_0x0024;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0025, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0038, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void applySurfaceChange(int r5, boolean r6) {
        /*
            r4 = this;
            monitor-enter(r4)
            r0 = 0
            r1 = 8
            if (r5 != 0) goto L_0x0014
            r4.clearSurfaces()     // Catch:{ all -> 0x0012 }
            com.android.camera.ui.AutoFitSurfaceView r5 = r4.mSurfaceView     // Catch:{ all -> 0x0012 }
            r5.setVisibility(r1)     // Catch:{ all -> 0x0012 }
            r4.mSurfaceMode = r0     // Catch:{ all -> 0x0012 }
            monitor-exit(r4)
            return
        L_0x0012:
            r5 = move-exception
            goto L_0x0039
        L_0x0014:
            r2 = 2
            r3 = 1
            if (r6 != 0) goto L_0x0026
            if (r5 != r3) goto L_0x001e
            int r6 = r4.mSurfaceMode     // Catch:{ all -> 0x0012 }
            if (r6 == r3) goto L_0x0024
        L_0x001e:
            if (r5 != r2) goto L_0x0026
            int r6 = r4.mSurfaceMode     // Catch:{ all -> 0x0012 }
            if (r6 != r2) goto L_0x0026
        L_0x0024:
            monitor-exit(r4)
            return
        L_0x0026:
            if (r5 != r3) goto L_0x0030
            com.android.camera.ui.AutoFitSurfaceView r5 = r4.mSurfaceView     // Catch:{ all -> 0x0012 }
            r5.setVisibility(r1)     // Catch:{ all -> 0x0012 }
            r4.mSurfaceMode = r3     // Catch:{ all -> 0x0012 }
            goto L_0x0037
        L_0x0030:
            com.android.camera.ui.AutoFitSurfaceView r5 = r4.mSurfaceView     // Catch:{ all -> 0x0012 }
            r5.setVisibility(r0)     // Catch:{ all -> 0x0012 }
            r4.mSurfaceMode = r2     // Catch:{ all -> 0x0012 }
        L_0x0037:
            monitor-exit(r4)
            return
        L_0x0039:
            monitor-exit(r4)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.PanoCaptureUI.applySurfaceChange(int, boolean):void");
    }

    public void setLayout(Size size) {
        this.mSurfaceView.setAspectRatio(size.getHeight(), size.getWidth());
    }

    public PanoCaptureUI(CameraActivity cameraActivity, PanoCaptureModule panoCaptureModule, View view) {
        this.mActivity = cameraActivity;
        this.mController = panoCaptureModule;
        this.mRootView = view;
        this.mActivity.getLayoutInflater().inflate(C0905R.layout.pano_capture_module, (ViewGroup) this.mRootView, true);
        this.mPreviewProcessView = (PanoCaptureProcessView) this.mRootView.findViewById(C0905R.C0907id.preview_process_view);
        this.mPreviewProcessView.setContext(cameraActivity, this.mController);
        this.mSurfaceView = (AutoFitSurfaceView) this.mRootView.findViewById(C0905R.C0907id.mdp_preview_content);
        this.mSurfaceView.setVisibility(0);
        this.mSurfaceView.addOnLayoutChangeListener(this.mLayoutListener);
        this.mSurfaceHolder = this.mSurfaceView.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mRootView.findViewById(C0905R.C0907id.mute_button).setVisibility(8);
        this.mRootView.findViewById(C0905R.C0907id.menu).setVisibility(8);
        applySurfaceChange(2, false);
        this.mShutterButton = (ShutterButton) this.mRootView.findViewById(C0905R.C0907id.shutter_button);
        this.mShutterButton.setLongClickable(false);
        this.mSwitcher = (ModuleSwitcher) this.mRootView.findViewById(C0905R.C0907id.camera_switcher);
        this.mSwitcher.setVisibility(8);
        this.mCameraControls = (CameraControls) this.mRootView.findViewById(C0905R.C0907id.camera_controls);
        this.mThumbnail = (ImageView) this.mRootView.findViewById(C0905R.C0907id.preview_thumb);
        this.mThumbnail.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (!CameraControls.isAnimating()) {
                    PanoCaptureUI.this.mActivity.gotoGallery();
                }
            }
        });
        this.mSceneModeLabelRect = (RotateLayout) this.mRootView.findViewById(C0905R.C0907id.scene_mode_label_rect);
        this.mSceneModeName = (TextView) this.mRootView.findViewById(C0905R.C0907id.scene_mode_label);
        this.mSceneModeName.setText(C0905R.string.pref_camera_scenemode_entry_panorama);
        this.mSceneModeLabelCloseIcon = (ImageView) this.mRootView.findViewById(C0905R.C0907id.scene_mode_label_close);
        this.mSceneModeLabelCloseIcon.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                PanoCaptureUI.this.mIsSceneModeLabelClose = true;
                PanoCaptureUI.this.mSceneModeLabelRect.setVisibility(8);
            }
        });
        initIndicators();
        Point point = new Point();
        this.mActivity.getWindowManager().getDefaultDisplay().getSize(point);
        calculateMargins(point);
        this.mCameraControls.setMargins(this.mTopMargin, this.mBottomMargin);
        if (needShowInstructional()) {
            showSceneInstructionalDialog(this.mOrientation);
        }
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

    private void setTransformMatrix(int i, int i2) {
        this.mMatrix = this.mSurfaceView.getMatrix();
        RectF rectF = new RectF(0.0f, 0.0f, (float) i, (float) i2);
        this.mMatrix.mapRect(rectF);
        this.mController.onPreviewRectChanged(CameraUtil.rectFToRect(rectF));
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        StringBuilder sb = new StringBuilder();
        sb.append("surfaceChanged: width =");
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
        this.mController.onPreviewUIDestroyed();
        this.mSurfaceHolder = null;
    }

    public View getRootView() {
        return this.mRootView;
    }

    private void initIndicators() {
        this.mOnScreenIndicators = new OnScreenIndicators(this.mActivity, this.mRootView.findViewById(C0905R.C0907id.on_screen_indicators));
    }

    public void hideUI() {
        this.mSwitcher.closePopup();
        if (!this.mUIhidden) {
            this.mUIhidden = true;
            this.mCameraControls.hideUI();
        }
    }

    public void showUI() {
        this.mUIhidden = false;
        this.mCameraControls.showUI();
    }

    public boolean arePreviewControlsVisible() {
        return !this.mUIhidden;
    }

    public void initializeShutterButton() {
        this.mShutterButton.setImageResource(C0905R.C0906drawable.btn_new_shutter_panorama);
        this.mShutterButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
            }
        });
        this.mShutterButton.setOnShutterButtonListener(this.mController);
        this.mShutterButton.setVisibility(0);
    }

    public void enableShutter(boolean z) {
        ShutterButton shutterButton = this.mShutterButton;
        if (shutterButton != null) {
            shutterButton.setEnabled(z);
        }
    }

    public boolean onBackPressed() {
        if (this.mController.isImageCaptureIntent()) {
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
        setShowMenu(z);
    }

    private void setShowMenu(boolean z) {
        OnScreenIndicators onScreenIndicators = this.mOnScreenIndicators;
        if (onScreenIndicators != null) {
            onScreenIndicators.setVisibility(z ? 0 : 8);
        }
    }

    public boolean collapseCameraControls() {
        this.mSwitcher.closePopup();
        return true;
    }

    public SurfaceHolder getSurfaceHolder() {
        return this.mSurfaceHolder;
    }

    public void onResume() {
        this.mPreviewProcessView.onResume();
        onPanoStatusChange(false);
        this.mCameraControls.getPanoramaExitButton().setVisibility(0);
        this.mCameraControls.getPanoramaExitButton().setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                try {
                    SettingsManager.getInstance().setValueIndex(SettingsManager.KEY_SCENE_MODE, 0);
                } catch (NullPointerException unused) {
                }
                PanoCaptureUI.this.mActivity.onModuleSelected(5);
            }
        });
    }

    public void onPause() {
        collapseCameraControls();
        this.mPreviewProcessView.onPause();
        this.mCameraControls.getPanoramaExitButton().setVisibility(8);
        this.mCameraControls.getPanoramaExitButton().setOnClickListener(null);
    }

    public void onDisplayChanged() {
        Log.d(TAG, "Device flip detected.");
        this.mCameraControls.checkLayoutFlip();
        this.mController.updateCameraOrientation();
    }

    public void setOrientation(int i, boolean z) {
        this.mOrientation = i;
        this.mCameraControls.setOrientation(i, z);
        this.mPreviewProcessView.setOrientation(i);
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
        AlertDialog alertDialog = this.mSceneModeInstructionalDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            this.mSceneModeInstructionalDialog.dismiss();
            this.mSceneModeInstructionalDialog = null;
            showSceneInstructionalDialog(i);
        }
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    private boolean needShowInstructional() {
        CameraActivity cameraActivity = this.mActivity;
        SharedPreferences sharedPreferences = cameraActivity.getSharedPreferences(ComboPreferences.getGlobalSharedPreferencesName(cameraActivity), 0);
        int valueIndex = SettingsManager.getInstance().getValueIndex(SettingsManager.KEY_SCENE_MODE);
        StringBuilder sb = new StringBuilder();
        sb.append("pref_camera2_scenemode_key_");
        sb.append(valueIndex);
        return !sharedPreferences.getBoolean(sb.toString(), false);
    }

    private void showSceneInstructionalDialog(int i) {
        View inflate = ((LayoutInflater) this.mActivity.getSystemService("layout_inflater")).inflate((i == 90 || i == 270) ? C0905R.layout.scene_mode_instructional_landscape : C0905R.layout.scene_mode_instructional, null);
        ((TextView) inflate.findViewById(C0905R.C0907id.scene_mode_name)).setText(C0905R.string.pref_camera_scenemode_entry_panorama);
        ((ImageView) inflate.findViewById(C0905R.C0907id.scene_mode_icon)).setImageResource(C0905R.C0906drawable.ic_scene_mode_black_panorama);
        ((TextView) inflate.findViewById(C0905R.C0907id.scene_mode_instructional)).setText(C0905R.string.pref_camera2_scene_mode_panorama_instructional_content);
        final CheckBox checkBox = (CheckBox) inflate.findViewById(C0905R.C0907id.remember_selected);
        ((Button) inflate.findViewById(C0905R.C0907id.scene_mode_instructional_ok)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SharedPreferences sharedPreferences = PanoCaptureUI.this.mActivity.getSharedPreferences(ComboPreferences.getGlobalSharedPreferencesName(PanoCaptureUI.this.mActivity), 0);
                int valueIndex = SettingsManager.getInstance().getValueIndex(SettingsManager.KEY_SCENE_MODE);
                StringBuilder sb = new StringBuilder();
                sb.append("pref_camera2_scenemode_key_");
                sb.append(valueIndex);
                String sb2 = sb.toString();
                if (checkBox.isChecked()) {
                    Editor edit = sharedPreferences.edit();
                    edit.putBoolean(sb2, true);
                    edit.commit();
                }
                PanoCaptureUI.this.mSceneModeInstructionalDialog.dismiss();
                PanoCaptureUI.this.mSceneModeInstructionalDialog = null;
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
        WindowManager.LayoutParams attributes = window.getAttributes();
        window.setGravity(17);
        attributes.height = screenWidth;
        attributes.width = screenWidth;
        window.setAttributes(attributes);
        ((RelativeLayout) view.findViewById(C0905R.C0907id.mode_layout_rect)).setLayoutParams(new LayoutParams(screenWidth, screenWidth));
    }
}
