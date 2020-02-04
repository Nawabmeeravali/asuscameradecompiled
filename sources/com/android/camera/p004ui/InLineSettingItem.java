package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.camera.ListPreference;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.InLineSettingItem */
public abstract class InLineSettingItem extends LinearLayout {
    protected int mIndex;
    private Listener mListener;
    protected String mOverrideValue;
    protected ListPreference mPreference;
    protected TextView mTitle;

    /* renamed from: com.android.camera.ui.InLineSettingItem$Listener */
    public interface Listener {
        void onSettingChanged(ListPreference listPreference);
    }

    /* access modifiers changed from: protected */
    public abstract void updateView();

    public InLineSettingItem(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void setTitle(ListPreference listPreference) {
        this.mTitle = (TextView) findViewById(C0905R.C0907id.title);
        this.mTitle.setText(listPreference.getTitle());
    }

    public void initialize(ListPreference listPreference) {
        setTitle(listPreference);
        if (listPreference != null) {
            this.mPreference = listPreference;
            reloadPreference();
        }
    }

    /* access modifiers changed from: protected */
    public boolean changeIndex(int i) {
        if (i >= this.mPreference.getEntryValues().length || i < 0) {
            return false;
        }
        this.mIndex = i;
        this.mPreference.setValueIndex(this.mIndex);
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onSettingChanged(this.mPreference);
        }
        updateView();
        sendAccessibilityEvent(4);
        return true;
    }

    public void reloadPreference() {
        ListPreference listPreference = this.mPreference;
        this.mIndex = listPreference.findIndexOfValue(listPreference.getValue());
        updateView();
    }

    public void setSettingChangedListener(Listener listener) {
        this.mListener = listener;
    }

    public void overrideSettings(String str) {
        this.mOverrideValue = str;
        updateView();
    }
}
