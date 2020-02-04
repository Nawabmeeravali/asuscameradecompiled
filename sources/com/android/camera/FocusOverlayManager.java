package com.android.camera;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera.Area;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.camera.util.CameraUtil;
import java.util.ArrayList;
import java.util.List;

public class FocusOverlayManager {
    private static final int RESET_FACE_DETECTION = 1;
    private static final int RESET_FACE_DETECTION_DELAY = 3000;
    private static final int RESET_TOUCH_FOCUS = 0;
    private static final int RESET_TOUCH_FOCUS_DELAY = 3000;
    public static final int STATE_FAIL = 4;
    public static final int STATE_FOCUSING = 1;
    public static final int STATE_FOCUSING_SNAP_ON_FINISH = 2;
    public static final int STATE_IDLE = 0;
    public static final int STATE_SUCCESS = 3;
    private static final String TAG = "CAM_FocusManager";
    private boolean mAeAwbLock;
    private String[] mDefaultFocusModes;
    private int mDisplayOrientation;
    private List<Object> mFocusArea;
    private boolean mFocusAreaSupported;
    private String mFocusMode;
    private Handler mHandler;
    private boolean mInitialized;
    private boolean mIsAFRunning = false;
    Listener mListener;
    private boolean mLockAeAwbNeeded;
    private Matrix mMatrix;
    private List<Object> mMeteringArea;
    private boolean mMeteringAreaSupported;
    private boolean mMirror;
    private String mOverrideFocusMode;
    private Parameters mParameters;
    private ComboPreferences mPreferences;
    private final Rect mPreviewRect = new Rect(0, 0, 0, 0);
    private boolean mPreviousMoving;
    private int mState = 0;
    private boolean mTouchAFRunning = false;
    private FocusUI mUI;
    private boolean mZslEnabled = false;

    public interface FocusUI {
        void clearFocus();

        boolean hasFaces();

        void onFocusFailed(boolean z);

        void onFocusStarted();

        void onFocusSucceeded(boolean z);

        void pauseFaceDetection();

        void resumeFaceDetection();

        void setFocusPosition(int i, int i2);
    }

    public interface Listener {
        void autoFocus();

        void cancelAutoFocus();

        boolean capture();

        void setFocusParameters();

        void startFaceDetection();

        void stopFaceDetection();
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 0) {
                FocusOverlayManager.this.cancelAutoFocus();
                FocusOverlayManager.this.mListener.startFaceDetection();
            } else if (i == 1) {
                FocusOverlayManager.this.mListener.startFaceDetection();
            }
        }
    }

    public FocusOverlayManager(ComboPreferences comboPreferences, String[] strArr, Parameters parameters, Listener listener, boolean z, Looper looper, FocusUI focusUI) {
        this.mHandler = new MainHandler(looper);
        this.mMatrix = new Matrix();
        this.mPreferences = comboPreferences;
        this.mDefaultFocusModes = strArr;
        setParameters(parameters);
        this.mListener = listener;
        setMirror(z);
        this.mUI = focusUI;
    }

    public void setPhotoUI(FocusUI focusUI) {
        this.mUI = focusUI;
    }

    public void setParameters(Parameters parameters) {
        if (parameters != null) {
            this.mParameters = parameters;
            this.mFocusAreaSupported = CameraUtil.isFocusAreaSupported(parameters);
            this.mMeteringAreaSupported = CameraUtil.isMeteringAreaSupported(parameters);
            this.mLockAeAwbNeeded = CameraUtil.isAutoExposureLockSupported(this.mParameters) || CameraUtil.isAutoWhiteBalanceLockSupported(this.mParameters);
        }
    }

    public void setPreviewSize(int i, int i2) {
        if (this.mPreviewRect.width() != i || this.mPreviewRect.height() != i2) {
            setPreviewRect(new Rect(0, 0, i, i2));
        }
    }

    public void setPreviewRect(Rect rect) {
        if (!this.mPreviewRect.equals(rect)) {
            this.mPreviewRect.set(rect);
            setMatrix();
        }
    }

    public Rect getPreviewRect() {
        return new Rect(this.mPreviewRect);
    }

    public void setMirror(boolean z) {
        this.mMirror = z;
        setMatrix();
    }

    public void setDisplayOrientation(int i) {
        this.mDisplayOrientation = i;
        setMatrix();
    }

    private void setMatrix() {
        if (this.mPreviewRect.width() != 0 && this.mPreviewRect.height() != 0) {
            Matrix matrix = new Matrix();
            CameraUtil.prepareMatrix(matrix, this.mMirror, this.mDisplayOrientation, getPreviewRect());
            matrix.invert(this.mMatrix);
            this.mInitialized = true;
        }
    }

    private void lockAeAwbIfNeeded() {
        if (this.mLockAeAwbNeeded && !this.mAeAwbLock && !this.mZslEnabled) {
            this.mAeAwbLock = true;
            this.mListener.setFocusParameters();
        }
    }

    private void unlockAeAwbIfNeeded() {
        if (this.mLockAeAwbNeeded && this.mAeAwbLock && this.mState != 2) {
            this.mAeAwbLock = false;
            this.mListener.setFocusParameters();
        }
    }

    public void onShutterDown() {
        if (this.mInitialized) {
            boolean z = false;
            if (needAutoFocusCall()) {
                int i = this.mState;
                if (!(i == 3 || i == 4)) {
                    autoFocus();
                    z = true;
                }
            }
            if (!z) {
                lockAeAwbIfNeeded();
            }
        }
    }

    public void onShutterUp() {
        if (this.mInitialized) {
            if (needAutoFocusCall()) {
                int i = this.mState;
                if (i == 3 || i == 4) {
                    cancelAutoFocus();
                }
            }
            unlockAeAwbIfNeeded();
        }
    }

    public void doSnap() {
        if (this.mInitialized) {
            if (needAutoFocusCall()) {
                int i = this.mState;
                if (!(i == 3 || i == 4)) {
                    if (i == 1) {
                        this.mState = 2;
                    } else if (i == 0) {
                        capture();
                    }
                }
            }
            capture();
        }
    }

    public void onAutoFocus(boolean z, boolean z2) {
        int i = this.mState;
        if (i == 2) {
            if (z) {
                this.mState = 3;
            } else {
                this.mState = 4;
            }
            updateFocusUI();
            capture();
        } else if (i == 1) {
            if (z) {
                this.mState = 3;
            } else {
                this.mState = 4;
            }
            updateFocusUI();
            if (this.mFocusArea != null) {
                this.mHandler.sendEmptyMessageDelayed(0, 3000);
            }
            if (z2) {
                lockAeAwbIfNeeded();
            }
        }
    }

    public void onAutoFocusMoving(boolean z) {
        if (this.mInitialized) {
            if (this.mUI.hasFaces()) {
                this.mUI.clearFocus();
                if (this.mIsAFRunning) {
                    this.mUI.onFocusSucceeded(true);
                    this.mIsAFRunning = false;
                }
            } else if (this.mState == 0) {
                if (z && !this.mPreviousMoving) {
                    this.mUI.onFocusStarted();
                    this.mIsAFRunning = true;
                } else if (!z) {
                    this.mUI.onFocusSucceeded(true);
                    this.mIsAFRunning = false;
                }
                this.mHandler.sendEmptyMessageDelayed(1, 3000);
                this.mPreviousMoving = z;
            }
        }
    }

    @TargetApi(14)
    private void initializeFocusAreas(int i, int i2) {
        if (this.mFocusArea == null) {
            this.mFocusArea = new ArrayList();
            this.mFocusArea.add(new Area(new Rect(), 1));
        }
        calculateTapArea(i, i2, 1.0f, ((Area) this.mFocusArea.get(0)).rect);
    }

    @TargetApi(14)
    private void initializeMeteringAreas(int i, int i2) {
        if (this.mMeteringArea == null) {
            this.mMeteringArea = new ArrayList();
            this.mMeteringArea.add(new Area(new Rect(), 1));
        }
        calculateTapArea(i, i2, 1.5f, ((Area) this.mMeteringArea.get(0)).rect);
    }

    private void resetMeteringAreas() {
        this.mMeteringArea = null;
    }

    public void onSingleTapUp(int i, int i2) {
        if (this.mInitialized && this.mState != 2) {
            StringBuilder sb = new StringBuilder();
            sb.append(i);
            sb.append(",");
            sb.append(i2);
            sb.toString();
            int i3 = this.mState;
            if (i3 == 1 || i3 == 3 || i3 == 4) {
                cancelAutoFocus();
            }
            if (this.mPreviewRect.width() != 0 && this.mPreviewRect.height() != 0) {
                if (this.mFocusAreaSupported) {
                    initializeFocusAreas(i, i2);
                }
                if (this.mMeteringAreaSupported) {
                    initializeMeteringAreas(i, i2);
                }
                this.mUI.setFocusPosition(i, i2);
                if (this.mZslEnabled) {
                    this.mTouchAFRunning = true;
                }
                this.mListener.stopFaceDetection();
                this.mListener.setFocusParameters();
                if (this.mFocusAreaSupported) {
                    autoFocus();
                    return;
                }
                updateFocusUI();
                this.mHandler.removeMessages(0);
                this.mHandler.sendEmptyMessageDelayed(0, 3000);
            }
        }
    }

    public void onPreviewStarted() {
        this.mState = 0;
    }

    public void onPreviewStopped() {
        this.mState = 0;
        resetTouchFocus();
        updateFocusUI();
    }

    public void onCameraReleased() {
        this.mTouchAFRunning = false;
        onPreviewStopped();
    }

    private void autoFocus() {
        Log.v(TAG, "Start autofocus.");
        this.mListener.autoFocus();
        this.mState = 1;
        updateFocusUI();
        this.mHandler.removeMessages(0);
    }

    public void cancelAutoFocus() {
        Log.v(TAG, "Cancel autofocus.");
        resetTouchFocus();
        this.mListener.cancelAutoFocus();
        this.mUI.resumeFaceDetection();
        this.mState = 0;
        updateFocusUI();
        this.mHandler.removeMessages(0);
    }

    private void capture() {
        if (this.mListener.capture()) {
            this.mState = 0;
            this.mHandler.removeMessages(0);
        }
    }

    public String getFocusMode() {
        String str = this.mOverrideFocusMode;
        if (str != null) {
            return str;
        }
        Parameters parameters = this.mParameters;
        String str2 = "auto";
        if (parameters == null) {
            return str2;
        }
        List supportedFocusModes = parameters.getSupportedFocusModes();
        if (!this.mFocusAreaSupported || this.mFocusArea == null) {
            this.mFocusMode = this.mPreferences.getString(CameraSettings.KEY_FOCUS_MODE, null);
            if (this.mFocusMode == null) {
                int i = 0;
                while (true) {
                    String[] strArr = this.mDefaultFocusModes;
                    if (i >= strArr.length) {
                        break;
                    }
                    String str3 = strArr[i];
                    if (CameraUtil.isSupported(str3, supportedFocusModes)) {
                        this.mFocusMode = str3;
                        break;
                    }
                    i++;
                }
            }
        } else {
            this.mFocusMode = str2;
        }
        if (!CameraUtil.isSupported(this.mFocusMode, supportedFocusModes)) {
            if (CameraUtil.isSupported(str2, this.mParameters.getSupportedFocusModes())) {
                this.mFocusMode = str2;
            } else {
                this.mFocusMode = this.mParameters.getFocusMode();
            }
        }
        return this.mFocusMode;
    }

    public List getFocusAreas() {
        return this.mFocusArea;
    }

    public List getMeteringAreas() {
        return this.mMeteringArea;
    }

    public void updateFocusUI() {
        if (this.mInitialized) {
            int i = this.mState;
            if (i == 0) {
                if (this.mFocusArea == null) {
                    this.mUI.clearFocus();
                } else {
                    this.mUI.onFocusStarted();
                }
            } else if (i == 1 || i == 2) {
                this.mUI.onFocusStarted();
            } else {
                if (CameraUtil.FOCUS_MODE_CONTINUOUS_PICTURE.equals(this.mFocusMode)) {
                    this.mUI.onFocusSucceeded(false);
                } else {
                    int i2 = this.mState;
                    if (i2 == 3) {
                        this.mUI.onFocusSucceeded(false);
                    } else if (i2 == 4) {
                        this.mUI.onFocusFailed(false);
                    }
                }
            }
        }
    }

    public void resetTouchFocus() {
        if (this.mInitialized) {
            FocusUI focusUI = this.mUI;
            if (focusUI != null) {
                focusUI.clearFocus();
            }
            this.mFocusArea = null;
            this.mMeteringArea = null;
            if (this.mMeteringAreaSupported) {
                resetMeteringAreas();
            }
            if (this.mTouchAFRunning && this.mZslEnabled) {
                this.mTouchAFRunning = false;
            }
        }
    }

    private void calculateTapArea(int i, int i2, float f, Rect rect) {
        int areaSize = (int) (((float) getAreaSize()) * f);
        int i3 = areaSize / 2;
        int i4 = i - i3;
        Rect rect2 = this.mPreviewRect;
        int clamp = CameraUtil.clamp(i4, rect2.left, rect2.right - areaSize);
        int i5 = i2 - i3;
        Rect rect3 = this.mPreviewRect;
        int clamp2 = CameraUtil.clamp(i5, rect3.top, rect3.bottom - areaSize);
        RectF rectF = new RectF((float) clamp, (float) clamp2, (float) (clamp + areaSize), (float) (clamp2 + areaSize));
        this.mMatrix.mapRect(rectF);
        CameraUtil.rectFToRect(rectF, rect);
    }

    private int getAreaSize() {
        return Math.max(this.mPreviewRect.width(), this.mPreviewRect.height()) / 8;
    }

    public boolean isFocusCompleted() {
        int i = this.mState;
        return i == 3 || i == 4;
    }

    public int getCurrentFocusState() {
        return this.mState;
    }

    public boolean isFocusingSnapOnFinish() {
        return this.mState == 2;
    }

    public void removeMessages() {
        this.mHandler.removeMessages(0);
    }

    public void overrideFocusMode(String str) {
        this.mOverrideFocusMode = str;
    }

    public void setAeAwbLock(boolean z) {
        this.mAeAwbLock = z;
    }

    public boolean getAeAwbLock() {
        return this.mAeAwbLock;
    }

    private boolean needAutoFocusCall() {
        String focusMode = getFocusMode();
        return !focusMode.equals("infinity") && !focusMode.equals("fixed") && !focusMode.equals("edof");
    }

    public void setZslEnable(boolean z) {
        this.mZslEnabled = z;
    }

    public boolean isZslEnabled() {
        return this.mZslEnabled;
    }

    public boolean isTouch() {
        return this.mTouchAFRunning;
    }
}
