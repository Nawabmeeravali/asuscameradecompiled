package com.android.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import org.codeaurora.snapcam.C0905R;

public abstract class CameraPreference {
    private final Context mContext;
    private SharedPreferences mSharedPreferences;
    private final String mTitle;

    public interface OnPreferenceChangedListener {
        void onCameraPickerClicked(int i);

        void onOverriddenPreferencesClicked();

        void onRestorePreferencesClicked();

        void onSharedPreferenceChanged();

        void onSharedPreferenceChanged(ListPreference listPreference);
    }

    public abstract void reloadValue();

    public CameraPreference(Context context, AttributeSet attributeSet) {
        this.mContext = context;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, C0905R.styleable.CameraPreference, 0, 0);
        this.mTitle = obtainStyledAttributes.getString(0);
        obtainStyledAttributes.recycle();
    }

    public String getTitle() {
        return this.mTitle;
    }

    public SharedPreferences getSharedPreferences() {
        if (this.mSharedPreferences == null) {
            this.mSharedPreferences = ComboPreferences.get(this.mContext);
        }
        return this.mSharedPreferences;
    }
}
