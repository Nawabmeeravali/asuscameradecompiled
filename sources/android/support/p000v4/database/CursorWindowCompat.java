package android.support.p000v4.database;

import android.database.CursorWindow;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.p000v4.p002os.BuildCompat;

/* renamed from: android.support.v4.database.CursorWindowCompat */
public final class CursorWindowCompat {
    private CursorWindowCompat() {
    }

    @NonNull
    public CursorWindow create(@Nullable String str, long j) {
        if (BuildCompat.isAtLeastP()) {
            return new CursorWindow(str, j);
        }
        if (VERSION.SDK_INT >= 15) {
            return new CursorWindow(str);
        }
        return new CursorWindow(false);
    }
}
