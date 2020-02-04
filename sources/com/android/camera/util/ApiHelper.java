package com.android.camera.util;

import android.os.Build.VERSION;

public class ApiHelper {
    public static final boolean AT_LEAST_16 = (VERSION.SDK_INT >= 16);
    public static final boolean HAS_ANNOUNCE_FOR_ACCESSIBILITY = (VERSION.SDK_INT >= 16);
    public static final boolean HAS_APP_GALLERY = (VERSION.SDK_INT >= 15);
    public static final boolean HAS_AUTO_FOCUS_MOVE_CALLBACK = (VERSION.SDK_INT >= 16);
    public static final boolean HAS_CAMERA_HDR = (VERSION.SDK_INT >= 17);
    public static final boolean HAS_CAMERA_HDR_PLUS = isKitKatOrHigher();
    public static final boolean HAS_DISPLAY_LISTENER = (VERSION.SDK_INT >= 17);
    public static final boolean HAS_FINE_RESOLUTION_QUALITY_LEVELS = (VERSION.SDK_INT >= 18);
    public static final boolean HAS_HIDEYBARS = isKitKatOrHigher();
    public static final boolean HAS_MEDIA_ACTION_SOUND = (VERSION.SDK_INT >= 16);
    public static final boolean HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT = (VERSION.SDK_INT >= 16);
    public static final boolean HAS_ORIENTATION_LOCK = (VERSION.SDK_INT >= 18);
    public static final boolean HAS_RESUME_SUPPORTED;
    public static final boolean HAS_ROTATION_ANIMATION = (VERSION.SDK_INT >= 18);
    public static final boolean HAS_SET_BEAM_PUSH_URIS = (VERSION.SDK_INT >= 16);
    public static final boolean HAS_SURFACE_TEXTURE_RECORDING = (VERSION.SDK_INT >= 16);

    static {
        boolean z = true;
        if (VERSION.SDK_INT <= 23) {
            z = false;
        }
        HAS_RESUME_SUPPORTED = z;
    }

    public static int getIntFieldIfExists(Class<?> cls, String str, Class<?> cls2, int i) {
        try {
            return cls.getDeclaredField(str).getInt(cls2);
        } catch (Exception unused) {
            return i;
        }
    }

    public static boolean isKitKatOrHigher() {
        if (VERSION.SDK_INT < 19) {
            if (!"KeyLimePie".equals(VERSION.CODENAME)) {
                return false;
            }
        }
        return true;
    }
}
