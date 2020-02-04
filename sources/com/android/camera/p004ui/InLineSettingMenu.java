package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import com.android.camera.ListPreference;
import java.util.List;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.InLineSettingMenu */
public class InLineSettingMenu extends InLineSettingItem {
    private static final String TAG = "InLineSettingMenu";
    private TextView mEntry;

    public InLineSettingMenu(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mEntry = (TextView) findViewById(C0905R.C0907id.current_setting);
    }

    public void initialize(ListPreference listPreference) {
        super.initialize(listPreference);
    }

    /* access modifiers changed from: protected */
    public void updateView() {
        String str = this.mOverrideValue;
        if (str == null) {
            this.mEntry.setText(this.mPreference.getEntry());
            return;
        }
        int findIndexOfValue = this.mPreference.findIndexOfValue(str);
        if (findIndexOfValue != -1) {
            this.mEntry.setText(this.mPreference.getEntries()[findIndexOfValue]);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Fail to find override value=");
        sb.append(this.mOverrideValue);
        Log.e(TAG, sb.toString());
        this.mPreference.print();
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        List text = accessibilityEvent.getText();
        StringBuilder sb = new StringBuilder();
        sb.append(this.mPreference.getTitle());
        sb.append(this.mPreference.getEntry());
        text.add(sb.toString());
        return true;
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        TextView textView = this.mTitle;
        if (textView != null) {
            textView.setEnabled(z);
        }
        TextView textView2 = this.mEntry;
        if (textView2 != null) {
            textView2.setEnabled(z);
        }
    }
}
