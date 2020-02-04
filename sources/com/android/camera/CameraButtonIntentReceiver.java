package com.android.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CameraButtonIntentReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        CameraHolder instance = CameraHolder.instance();
        if (instance.tryOpen(null, CameraSettings.readPreferredCameraId(new ComboPreferences(context)), null) != null) {
            instance.keep();
            instance.release();
            Intent intent2 = new Intent("android.intent.action.MAIN");
            intent2.setClass(context, CameraActivity.class);
            intent2.addCategory("android.intent.category.LAUNCHER");
            intent2.setFlags(335544320);
            context.startActivity(intent2);
        }
    }
}
