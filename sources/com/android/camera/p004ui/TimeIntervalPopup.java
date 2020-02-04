package com.android.camera.p004ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import com.android.camera.IconListPreference;
import com.android.camera.ListPreference;
import java.text.NumberFormat;
import java.util.Locale;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.TimeIntervalPopup */
public class TimeIntervalPopup extends AbstractSettingPopup {
    private static final String TAG = "TimeIntervalPopup";
    private Button mConfirmButton;
    private final String[] mDurations;
    private TextView mHelpText;
    private Listener mListener;
    private NumberPicker mNumberSpinner;
    private IconListPreference mPreference;
    private Switch mTimeLapseSwitch;
    private View mTimePicker;
    private NumberPicker mUnitSpinner;
    private final String[] mUnits;

    /* renamed from: com.android.camera.ui.TimeIntervalPopup$Listener */
    public interface Listener {
        void onListPrefChanged(ListPreference listPreference);
    }

    public void reloadPreference() {
    }

    public void setSettingChangedListener(Listener listener) {
        this.mListener = listener;
    }

    public TimeIntervalPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Resources resources = context.getResources();
        this.mUnits = resources.getStringArray(C0905R.array.pref_video_time_lapse_frame_interval_units);
        this.mDurations = localizeNumbers(resources.getStringArray(C0905R.array.pref_video_time_lapse_frame_interval_duration_values));
    }

    private static String[] localizeNumbers(String[] strArr) {
        NumberFormat instance = NumberFormat.getInstance(Locale.getDefault());
        String[] strArr2 = new String[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            strArr2[i] = instance.format(Double.valueOf(strArr[i]).doubleValue());
        }
        return strArr2;
    }

    public void initialize(IconListPreference iconListPreference) {
        this.mPreference = iconListPreference;
        this.mTitle.setText(this.mPreference.getTitle());
        int length = this.mDurations.length;
        this.mNumberSpinner = (NumberPicker) findViewById(C0905R.C0907id.duration);
        this.mNumberSpinner.setMinValue(0);
        this.mNumberSpinner.setMaxValue(length - 1);
        this.mNumberSpinner.setDisplayedValues(this.mDurations);
        this.mNumberSpinner.setWrapSelectorWheel(false);
        this.mUnitSpinner = (NumberPicker) findViewById(C0905R.C0907id.duration_unit);
        this.mUnitSpinner.setMinValue(0);
        this.mUnitSpinner.setMaxValue(this.mUnits.length - 1);
        this.mUnitSpinner.setDisplayedValues(this.mUnits);
        this.mUnitSpinner.setWrapSelectorWheel(false);
        this.mTimePicker = findViewById(C0905R.C0907id.time_interval_picker);
        this.mTimeLapseSwitch = (Switch) findViewById(C0905R.C0907id.time_lapse_switch);
        this.mHelpText = (TextView) findViewById(C0905R.C0907id.set_time_interval_help_text);
        this.mConfirmButton = (Button) findViewById(C0905R.C0907id.time_lapse_interval_set_button);
        this.mNumberSpinner.setDescendantFocusability(393216);
        this.mUnitSpinner.setDescendantFocusability(393216);
        this.mTimeLapseSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                TimeIntervalPopup.this.setTimeSelectionEnabled(z);
            }
        });
        this.mConfirmButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                TimeIntervalPopup.this.updateInputState();
            }
        });
    }

    private void restoreSetting() {
        IconListPreference iconListPreference = this.mPreference;
        int findIndexOfValue = iconListPreference.findIndexOfValue(iconListPreference.getValue());
        if (findIndexOfValue == -1) {
            Log.e(TAG, "Invalid preference value.");
            this.mPreference.print();
            throw new IllegalArgumentException();
        } else if (findIndexOfValue == 0) {
            this.mTimeLapseSwitch.setChecked(false);
            setTimeSelectionEnabled(false);
        } else {
            this.mTimeLapseSwitch.setChecked(true);
            setTimeSelectionEnabled(true);
            int maxValue = this.mNumberSpinner.getMaxValue() + 1;
            int i = findIndexOfValue - 1;
            int i2 = i / maxValue;
            int i3 = i % maxValue;
            this.mUnitSpinner.setValue(i2);
            this.mNumberSpinner.setValue(i3);
        }
    }

    public void setVisibility(int i) {
        if (i == 0 && getVisibility() != 0) {
            restoreSetting();
        }
        super.setVisibility(i);
    }

    /* access modifiers changed from: protected */
    public void setTimeSelectionEnabled(boolean z) {
        int i = 8;
        this.mHelpText.setVisibility(z ? 8 : 0);
        View view = this.mTimePicker;
        if (z) {
            i = 0;
        }
        view.setVisibility(i);
    }

    /* access modifiers changed from: private */
    public void updateInputState() {
        if (this.mTimeLapseSwitch.isChecked()) {
            this.mPreference.setValueIndex((this.mUnitSpinner.getValue() * (this.mNumberSpinner.getMaxValue() + 1)) + this.mNumberSpinner.getValue() + 1);
        } else {
            this.mPreference.setValueIndex(0);
        }
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onListPrefChanged(this.mPreference);
        }
    }
}
