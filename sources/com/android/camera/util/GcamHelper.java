package com.android.camera.util;

import android.content.ContentResolver;
import com.android.camera.CameraModule;

public class GcamHelper {
    public static CameraModule createGcamModule() {
        return null;
    }

    public static boolean hasGcamAsHDRMode() {
        return false;
    }

    public static boolean hasGcamCapture() {
        return false;
    }

    public static void init(ContentResolver contentResolver) {
    }
}
