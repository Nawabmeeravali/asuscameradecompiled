package android.support.p000v4.database.sqlite;

import android.database.sqlite.SQLiteCursor;
import android.support.annotation.NonNull;
import android.support.p000v4.p002os.BuildCompat;

/* renamed from: android.support.v4.database.sqlite.SQLiteCursorCompat */
public final class SQLiteCursorCompat {
    private SQLiteCursorCompat() {
    }

    public void setFillWindowForwardOnly(@NonNull SQLiteCursor sQLiteCursor, boolean z) {
        if (BuildCompat.isAtLeastP()) {
            sQLiteCursor.setFillWindowForwardOnly(z);
        }
    }
}
