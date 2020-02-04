package com.android.camera;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

public class RecordLocationPreference extends IconListPreference {
    public static final String VALUE_NONE = "none";
    public static final String VALUE_OFF = "off";
    public static final String VALUE_ON = "on";
    private final ContentResolver mResolver;

    public RecordLocationPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mResolver = context.getContentResolver();
    }

    public String getValue() {
        return get(getSharedPreferences(), getKey()) ? VALUE_ON : "off";
    }

    public static boolean get(SharedPreferences sharedPreferences, String str) {
        return VALUE_ON.equals(sharedPreferences.getString(str, "none"));
    }

    public static boolean isSet(SharedPreferences sharedPreferences, String str) {
        String str2 = "none";
        return !str2.equals(sharedPreferences.getString(str, str2));
    }
}
