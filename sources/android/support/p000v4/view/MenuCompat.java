package android.support.p000v4.view;

import android.annotation.SuppressLint;
import android.support.p000v4.internal.view.SupportMenu;
import android.support.p000v4.p002os.BuildCompat;
import android.view.Menu;
import android.view.MenuItem;

/* renamed from: android.support.v4.view.MenuCompat */
public final class MenuCompat {
    @Deprecated
    public static void setShowAsAction(MenuItem menuItem, int i) {
        menuItem.setShowAsAction(i);
    }

    @SuppressLint({"NewApi"})
    public static void setGroupDividerEnabled(Menu menu, boolean z) {
        if (menu instanceof SupportMenu) {
            ((SupportMenu) menu).setGroupDividerEnabled(z);
        } else if (BuildCompat.isAtLeastP()) {
            menu.setGroupDividerEnabled(z);
        }
    }

    private MenuCompat() {
    }
}
