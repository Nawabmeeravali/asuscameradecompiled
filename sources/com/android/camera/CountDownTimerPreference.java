package com.android.camera;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import org.codeaurora.snapcam.C0905R;

public class CountDownTimerPreference extends IconListPreference {
    private static final int[] DURATIONS = {0, 2, 5, 10};

    public CountDownTimerPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initCountDownDurationChoices(context);
    }

    private void initCountDownDurationChoices(Context context) {
        int[] iArr = DURATIONS;
        CharSequence[] charSequenceArr = new CharSequence[iArr.length];
        CharSequence[] charSequenceArr2 = new CharSequence[iArr.length];
        int i = 0;
        while (true) {
            int[] iArr2 = DURATIONS;
            if (i < iArr2.length) {
                charSequenceArr[i] = Integer.toString(iArr2[i]);
                if (i == 0) {
                    charSequenceArr2[0] = context.getString(C0905R.string.setting_off);
                } else {
                    Resources resources = context.getResources();
                    int[] iArr3 = DURATIONS;
                    charSequenceArr2[i] = resources.getQuantityString(C0905R.plurals.pref_camera_timer_entry, iArr3[i], new Object[]{Integer.valueOf(iArr3[i])});
                }
                i++;
            } else {
                setEntries(charSequenceArr2);
                setEntryValues(charSequenceArr);
                return;
            }
        }
    }
}
