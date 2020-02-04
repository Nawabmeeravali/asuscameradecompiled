package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.android.camera.ListPreference;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.InLineSettingCheckBox */
public class InLineSettingCheckBox extends InLineSettingItem {
    private CheckBox mCheckBox;
    OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
            InLineSettingCheckBox.this.changeIndex(z ? 1 : 0);
        }
    };

    public InLineSettingCheckBox(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mCheckBox = (CheckBox) findViewById(C0905R.C0907id.setting_check_box);
        this.mCheckBox.setOnCheckedChangeListener(this.mCheckedChangeListener);
    }

    public void initialize(ListPreference listPreference) {
        super.initialize(listPreference);
        this.mCheckBox.setContentDescription(getContext().getResources().getString(C0905R.string.accessibility_check_box, new Object[]{this.mPreference.getTitle()}));
    }

    /* access modifiers changed from: protected */
    public void updateView() {
        this.mCheckBox.setOnCheckedChangeListener(null);
        String str = this.mOverrideValue;
        boolean z = false;
        if (str == null) {
            CheckBox checkBox = this.mCheckBox;
            if (this.mIndex == 1) {
                z = true;
            }
            checkBox.setChecked(z);
        } else {
            int findIndexOfValue = this.mPreference.findIndexOfValue(str);
            CheckBox checkBox2 = this.mCheckBox;
            if (findIndexOfValue == 1) {
                z = true;
            }
            checkBox2.setChecked(z);
        }
        this.mCheckBox.setOnCheckedChangeListener(this.mCheckedChangeListener);
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        accessibilityEvent.getText().add(this.mPreference.getTitle());
        return true;
    }

    public void setEnabled(boolean z) {
        TextView textView = this.mTitle;
        if (textView != null) {
            textView.setEnabled(z);
        }
        CheckBox checkBox = this.mCheckBox;
        if (checkBox != null) {
            checkBox.setEnabled(z);
        }
    }
}
