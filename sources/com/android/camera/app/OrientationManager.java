package com.android.camera.app;

import android.app.Activity;
import android.content.Context;
import android.view.OrientationEventListener;

public class OrientationManager {
    private static final int ORIENTATION_HYSTERESIS = 5;
    private static final String TAG = "CAM_OrientationManager";
    private Activity mActivity;
    private MyOrientationEventListener mOrientationListener;

    private class MyOrientationEventListener extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        public void onOrientationChanged(int i) {
            if (i != -1) {
                OrientationManager.roundOrientation(i, 0);
            }
        }
    }

    public int getCompensation() {
        return 0;
    }

    public OrientationManager(Activity activity) {
        this.mActivity = activity;
        this.mOrientationListener = new MyOrientationEventListener(activity);
    }

    public void resume() {
        this.mActivity.getContentResolver();
        this.mOrientationListener.enable();
    }

    public void pause() {
        this.mOrientationListener.disable();
    }

    private int calculateCurrentScreenOrientation() {
        int displayRotation = getDisplayRotation();
        int i = 0;
        int i2 = 1;
        boolean z = displayRotation < 180;
        if (this.mActivity.getResources().getConfiguration().orientation == 2) {
            if (!z) {
                i = 8;
            }
            return i;
        }
        if (displayRotation == 90 || displayRotation == 270) {
            z = !z;
        }
        if (!z) {
            i2 = 9;
        }
        return i2;
    }

    public int getDisplayRotation() {
        return getDisplayRotation(this.mActivity);
    }

    /* access modifiers changed from: private */
    public static int roundOrientation(int i, int i2) {
        boolean z = true;
        if (i2 != -1) {
            int abs = Math.abs(i - i2);
            if (Math.min(abs, 360 - abs) < 50) {
                z = false;
            }
        }
        return z ? (((i + 45) / 90) * 90) % 360 : i2;
    }

    private static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        if (rotation == 0) {
            return 0;
        }
        if (rotation == 1) {
            return 90;
        }
        if (rotation != 2) {
            return rotation != 3 ? 0 : 270;
        }
        return 180;
    }
}
