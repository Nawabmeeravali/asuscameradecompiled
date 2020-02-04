package android.support.p000v4.content.p001pm;

import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;
import android.support.p000v4.p002os.BuildCompat;

/* renamed from: android.support.v4.content.pm.PackageInfoCompat */
public final class PackageInfoCompat {
    public static long getLongVersionCode(@NonNull PackageInfo packageInfo) {
        if (BuildCompat.isAtLeastP()) {
            return packageInfo.getLongVersionCode();
        }
        return (long) packageInfo.versionCode;
    }

    private PackageInfoCompat() {
    }
}
