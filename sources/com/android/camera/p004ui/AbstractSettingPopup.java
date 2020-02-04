package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.AbstractSettingPopup */
public abstract class AbstractSettingPopup extends RotateLayout {
    protected ViewGroup mSettingList;
    protected TextView mTitle;

    public abstract void reloadPreference();

    public AbstractSettingPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mTitle = (TextView) findViewById(C0905R.C0907id.title);
        this.mSettingList = (ViewGroup) findViewById(C0905R.C0907id.settingList);
    }
}
