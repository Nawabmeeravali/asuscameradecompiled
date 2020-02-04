package com.android.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.util.Log;
import com.android.camera.p004ui.RotateTextToast;
import org.codeaurora.snapcam.C0905R;

public class CameraErrorCallback implements ErrorCallback {
    private static final String TAG = "CameraErrorCallback";
    private static final int THERMAL_SHUTDOWN = 50;
    public CameraActivity mActivity = null;

    public void setActivity(CameraActivity cameraActivity) {
        this.mActivity = cameraActivity;
    }

    public void onError(int i, Camera camera) {
        StringBuilder sb = new StringBuilder();
        sb.append("Got camera error callback. error=");
        sb.append(i);
        Log.e(TAG, sb.toString());
        if (this.mActivity != null) {
            final int i2 = i != THERMAL_SHUTDOWN ? i != 100 ? C0905R.string.camera_unknown_error : C0905R.string.camera_server_died : C0905R.string.camera_thermal_shutdown;
            this.mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    RotateTextToast.makeText((Activity) CameraErrorCallback.this.mActivity, i2, 1).show();
                    CameraErrorCallback.this.mActivity.finish();
                }
            });
            return;
        }
        throw new RuntimeException("Unknown error");
    }
}
