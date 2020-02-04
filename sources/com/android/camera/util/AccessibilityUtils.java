package com.android.camera.util;

import android.support.p000v4.view.accessibility.AccessibilityRecordCompat;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

public class AccessibilityUtils {
    public static void makeAnnouncement(View view, CharSequence charSequence) {
        if (view != null) {
            if (ApiHelper.HAS_ANNOUNCE_FOR_ACCESSIBILITY) {
                view.announceForAccessibility(charSequence);
            } else {
                AccessibilityManager accessibilityManager = (AccessibilityManager) view.getContext().getSystemService("accessibility");
                if (accessibilityManager.isEnabled()) {
                    AccessibilityEvent obtain = AccessibilityEvent.obtain(64);
                    new AccessibilityRecordCompat(obtain).setSource(view);
                    obtain.setClassName(view.getClass().getName());
                    obtain.setPackageName(view.getContext().getPackageName());
                    obtain.setEnabled(view.isEnabled());
                    obtain.getText().add(charSequence);
                    accessibilityManager.sendAccessibilityEvent(obtain);
                }
            }
        }
    }
}
