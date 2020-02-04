package com.android.camera.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import com.asus.scenedetectlib.BuildConfig;

public class IntentHelper {
    private static final String GALLERY_ACTIVITY_CLASS = "com.android.gallery3d.app.GalleryActivity";
    private static final String GALLERY_PACKAGE_NAME = "com.android.gallery3d";
    private static final String SNAPDRAGON_GALLERY_PACKAGE_NAME = "org.codeaurora.gallery";

    public static Intent getGalleryIntent(Context context) {
        String str = SNAPDRAGON_GALLERY_PACKAGE_NAME;
        if (!packageExist(context, str)) {
            str = GALLERY_PACKAGE_NAME;
        }
        return new Intent("android.intent.action.MAIN").setClassName(str, GALLERY_ACTIVITY_CLASS);
    }

    public static Intent getVideoPlayerIntent(Context context, Uri uri) {
        return new Intent("android.intent.action.VIEW").setDataAndType(uri, "video/*");
    }

    private static boolean packageExist(Context context, String str) {
        if (str != null && !BuildConfig.FLAVOR.equals(str)) {
            try {
                context.getPackageManager().getApplicationInfo(str, 0);
                return true;
            } catch (NameNotFoundException unused) {
            }
        }
        return false;
    }
}
