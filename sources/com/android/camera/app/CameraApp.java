package com.android.camera.app;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Application;
import com.android.camera.SDCard;
import com.android.camera.SettingsManager;
import com.android.camera.util.CameraUtil;

public class CameraApp extends Application {
    private static final long LOW_MEMORY_DEVICE_THRESHOLD = 2147483648L;
    public static boolean mIsLowMemoryDevice = false;
    private static long mMaxSystemMemory;

    public void onCreate() {
        super.onCreate();
        ActivityManager activityManager = (ActivityManager) getSystemService("activity");
        MemoryInfo memoryInfo = new MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        mMaxSystemMemory = memoryInfo.totalMem;
        if (mMaxSystemMemory <= LOW_MEMORY_DEVICE_THRESHOLD) {
            mIsLowMemoryDevice = true;
        }
        SettingsManager.createInstance(this);
        CameraUtil.initialize(this);
        SDCard.initialize(this);
    }
}
