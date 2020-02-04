package com.android.camera;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DisableCameraReceiver extends BroadcastReceiver {
    private static final String[] ACTIVITIES = {"com.android.camera.CameraLauncher"};
    private static final boolean CHECK_BACK_CAMERA_ONLY = false;
    private static final String TAG = "DisableCameraReceiver";

    public void onReceive(Context context, Intent intent) {
        if (!hasCamera()) {
            Log.i(TAG, "disable all camera activities");
            int i = 0;
            while (true) {
                String[] strArr = ACTIVITIES;
                if (i >= strArr.length) {
                    break;
                }
                disableComponent(context, strArr[i]);
                i++;
            }
        }
        disableComponent(context, "com.android.camera.DisableCameraReceiver");
    }

    private boolean hasCamera() {
        int numberOfCameras = CameraHolder.instance().getNumberOfCameras();
        StringBuilder sb = new StringBuilder();
        sb.append("number of camera: ");
        sb.append(numberOfCameras);
        Log.i(TAG, sb.toString());
        return numberOfCameras > 0;
    }

    private boolean hasBackCamera() {
        String str;
        int backCameraId = CameraHolder.instance().getBackCameraId();
        if (backCameraId == -1) {
            str = "no back camera";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("back camera found: ");
            sb.append(backCameraId);
            str = sb.toString();
        }
        Log.i(TAG, str);
        return backCameraId != -1;
    }

    private void disableComponent(Context context, String str) {
        context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, str), 2, 1);
    }
}
