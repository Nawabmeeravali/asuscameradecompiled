package android.support.p000v4.content.p001pm;

import android.annotation.SuppressLint;
import android.content.pm.PermissionInfo;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.p000v4.p002os.BuildCompat;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* renamed from: android.support.v4.content.pm.PermissionInfoCompat */
public final class PermissionInfoCompat {

    @RestrictTo({Scope.LIBRARY})
    @Retention(RetentionPolicy.SOURCE)
    /* renamed from: android.support.v4.content.pm.PermissionInfoCompat$Protection */
    public @interface Protection {
    }

    @SuppressLint({"UniqueConstants"})
    @RestrictTo({Scope.LIBRARY})
    @Retention(RetentionPolicy.SOURCE)
    /* renamed from: android.support.v4.content.pm.PermissionInfoCompat$ProtectionFlags */
    public @interface ProtectionFlags {
    }

    private PermissionInfoCompat() {
    }

    @SuppressLint({"WrongConstant"})
    public static int getProtection(@NonNull PermissionInfo permissionInfo) {
        if (BuildCompat.isAtLeastP()) {
            return permissionInfo.getProtection();
        }
        return permissionInfo.protectionLevel & 15;
    }

    @SuppressLint({"WrongConstant"})
    public static int getProtectionFlags(@NonNull PermissionInfo permissionInfo) {
        if (BuildCompat.isAtLeastP()) {
            return permissionInfo.getProtectionFlags();
        }
        return permissionInfo.protectionLevel & -16;
    }
}
