package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import com.android.camera.ListPreference;
import java.util.Locale;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.CountdownTimerPopup */
public class CountdownTimerPopup extends AbstractSettingPopup {
    private static final String TAG = "TimerSettingPopup";
    private ListPreference mBeep;
    private Button mConfirmButton;
    private String[] mDurations;
    private Listener mListener;
    private NumberPicker mNumberSpinner;
    private View mPickerTitle;
    private View mSoundTitle;
    private ListPreference mTimer;
    private CheckBox mTimerSound;

    /* renamed from: com.android.camera.ui.CountdownTimerPopup$Listener */
    public interface Listener {
        void onListPrefChanged(ListPreference listPreference);
    }

    public void reloadPreference() {
    }

    public void setSettingChangedListener(Listener listener) {
        this.mListener = listener;
    }

    public CountdownTimerPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void initialize(ListPreference listPreference, ListPreference listPreference2) {
        this.mTimer = listPreference;
        this.mBeep = listPreference2;
        this.mTitle.setText(this.mTimer.getTitle());
        CharSequence[] entryValues = this.mTimer.getEntryValues();
        this.mDurations = new String[entryValues.length];
        Locale locale = getResources().getConfiguration().locale;
        this.mDurations[0] = getResources().getString(C0905R.string.setting_off);
        for (int i = 1; i < entryValues.length; i++) {
            this.mDurations[i] = String.format(locale, "%d", new Object[]{Integer.valueOf(Integer.parseInt(entryValues[i].toString()))});
        }
        int length = this.mDurations.length;
        this.mNumberSpinner = (NumberPicker) findViewById(C0905R.C0907id.duration);
        this.mNumberSpinner.setMinValue(0);
        this.mNumberSpinner.setMaxValue(length - 1);
        this.mNumberSpinner.setDisplayedValues(this.mDurations);
        this.mNumberSpinner.setWrapSelectorWheel(false);
        this.mNumberSpinner.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                CountdownTimerPopup.this.setTimeSelectionEnabled(i2 != 0);
            }
        });
        this.mConfirmButton = (Button) findViewById(C0905R.C0907id.timer_set_button);
        this.mPickerTitle = findViewById(C0905R.C0907id.set_time_interval_title);
        this.mNumberSpinner.setDescendantFocusability(393216);
        this.mConfirmButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                CountdownTimerPopup.this.updateInputState();
            }
        });
        this.mTimerSound = (CheckBox) findViewById(C0905R.C0907id.sound_check_box);
        this.mSoundTitle = findViewById(C0905R.C0907id.beep_title);
    }

    private void restoreSetting() {
        ListPreference listPreference = this.mTimer;
        int findIndexOfValue = listPreference.findIndexOfValue(listPreference.getValue());
        if (findIndexOfValue != -1) {
            boolean z = true;
            setTimeSelectionEnabled(findIndexOfValue != 0);
            this.mNumberSpinner.setValue(findIndexOfValue);
            ListPreference listPreference2 = this.mBeep;
            if (listPreference2.findIndexOfValue(listPreference2.getValue()) == 0) {
                z = false;
            }
            this.mTimerSound.setChecked(z);
            return;
        }
        Log.e(TAG, "Invalid preference value.");
        this.mTimer.print();
        throw new IllegalArgumentException();
    }

    public void setVisibility(int i) {
        if (i == 0 && getVisibility() != 0) {
            restoreSetting();
        }
        super.setVisibility(i);
    }

    /* access modifiers changed from: protected */
    public void setTimeSelectionEnabled(boolean z) {
        this.mPickerTitle.setVisibility(z ? 0 : 4);
        this.mTimerSound.setEnabled(z);
        this.mSoundTitle.setEnabled(z);
    }

    /* access modifiers changed from: private */
    public void updateInputState() {
        this.mTimer.setValueIndex(this.mNumberSpinner.getValue());
        this.mBeep.setValueIndex(this.mTimerSound.isChecked() ? 1 : 0);
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onListPrefChanged(this.mTimer);
            this.mListener.onListPrefChanged(this.mBeep);
        }
    }
}
