package com.android.camera.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import com.android.camera.CameraModule;

public class PhotoSphereHelper {
    public static final PanoramaMetadata NOT_PANORAMA = new PanoramaMetadata(false, false);

    public static class PanoramaMetadata {
        public final boolean mIsPanorama360;
        public final boolean mUsePanoramaViewer;

        public PanoramaMetadata(boolean z, boolean z2) {
            this.mUsePanoramaViewer = z;
            this.mIsPanorama360 = z2;
        }
    }

    public static class PanoramaViewHelper {
        public void onCreate() {
        }

        public void onStart() {
        }

        public void onStop() {
        }

        public void showPanorama(Uri uri) {
        }

        public PanoramaViewHelper(Activity activity) {
        }
    }

    public static CameraModule createPanoramaModule() {
        return null;
    }

    public static long getModifiedTimeFromURI(ContentResolver contentResolver, Uri uri) {
        return 0;
    }

    public static String getPathFromURI(ContentResolver contentResolver, Uri uri) {
        return null;
    }

    public static boolean hasLightCycleCapture(Context context) {
        return false;
    }

    public static PanoramaMetadata getPanoramaMetadata(Context context, Uri uri) {
        return NOT_PANORAMA;
    }
}
