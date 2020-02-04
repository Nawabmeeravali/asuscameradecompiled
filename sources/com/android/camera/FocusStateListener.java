package com.android.camera;

import android.util.Log;

public class FocusStateListener {
    private static final String TAG = "SnapCam_FocusStateListe";
    private CaptureUI mUI;

    public FocusStateListener(CaptureUI captureUI) {
        this.mUI = captureUI;
    }

    public void onFocusStatusUpdate(int i) {
        String str = TAG;
        switch (i) {
            case 0:
                Log.d(str, "CONTROL_AF_STATE_INACTIVE clearFocus");
                this.mUI.clearFocus();
                return;
            case 1:
                Log.d(str, "CONTROL_AF_STATE_PASSIVE_SCAN onFocusStarted");
                this.mUI.onFocusStarted();
                return;
            case 2:
                Log.d(str, "CONTROL_AF_STATE_PASSIVE_FOCUSED onFocusSucceeded");
                this.mUI.onFocusSucceeded(true);
                return;
            case 3:
                Log.d(str, "CONTROL_AF_STATE_ACTIVE_SCAN onFocusStarted");
                this.mUI.onFocusStarted();
                return;
            case 4:
                Log.d(str, "CONTROL_AF_STATE_FOCUSED_LOCKED onFocusSucceeded");
                this.mUI.onFocusSucceeded(false);
                return;
            case 5:
                Log.d(str, "CONTROL_AF_STATE_NOT_FOCUSED_LOCKED onFocusFailed");
                this.mUI.onFocusFailed(false);
                return;
            case 6:
                Log.d(str, "CONTROL_AF_STATE_PASSIVE_UNFOCUSED onFocusFailed");
                this.mUI.onFocusFailed(true);
                return;
            default:
                return;
        }
    }
}
