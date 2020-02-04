package com.android.camera;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import com.android.camera.util.CameraUtil;
import java.util.ArrayList;
import java.util.List;
import org.codeaurora.snapcam.C0905R;

public class ListPreference extends CameraPreference {
    private static final String TAG = "ListPreference";
    private final CharSequence[] mDefaultValues;
    private CharSequence[] mDependencyList;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private CharSequence[] mInitialEntries;
    private CharSequence[] mInitialEntryValues;
    private final String mKey;
    private CharSequence[] mLabels;
    private boolean mLoaded = false;
    private String mValue;

    public ListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, C0905R.styleable.ListPreference, 0, 0);
        String string = obtainStyledAttributes.getString(4);
        CameraUtil.checkNotNull(string);
        this.mKey = string;
        TypedValue peekValue = obtainStyledAttributes.peekValue(0);
        if (peekValue == null || peekValue.type != 1) {
            this.mDefaultValues = new CharSequence[1];
            this.mDefaultValues[0] = obtainStyledAttributes.getString(0);
        } else {
            this.mDefaultValues = obtainStyledAttributes.getTextArray(0);
        }
        setEntries(obtainStyledAttributes.getTextArray(2));
        setEntryValues(obtainStyledAttributes.getTextArray(3));
        this.mInitialEntryValues = this.mEntryValues;
        this.mInitialEntries = this.mEntries;
        setLabels(obtainStyledAttributes.getTextArray(5));
        setDependencyList(obtainStyledAttributes.getTextArray(1));
        obtainStyledAttributes.recycle();
    }

    public String getKey() {
        return this.mKey;
    }

    public CharSequence[] getEntries() {
        return this.mEntries;
    }

    public CharSequence[] getEntryValues() {
        return this.mEntryValues;
    }

    public CharSequence[] getLabels() {
        return this.mLabels;
    }

    public CharSequence[] getDependencyList() {
        return this.mDependencyList;
    }

    public void setEntries(CharSequence[] charSequenceArr) {
        if (charSequenceArr == null) {
            charSequenceArr = new CharSequence[0];
        }
        this.mEntries = charSequenceArr;
    }

    public void setEntryValues(CharSequence[] charSequenceArr) {
        if (charSequenceArr == null) {
            charSequenceArr = new CharSequence[0];
        }
        this.mEntryValues = charSequenceArr;
    }

    public void setLabels(CharSequence[] charSequenceArr) {
        if (charSequenceArr == null) {
            charSequenceArr = new CharSequence[0];
        }
        this.mLabels = charSequenceArr;
    }

    public void setDependencyList(CharSequence[] charSequenceArr) {
        if (charSequenceArr == null) {
            charSequenceArr = new CharSequence[0];
        }
        this.mDependencyList = charSequenceArr;
    }

    public String getValue() {
        if (!this.mLoaded) {
            this.mValue = getSharedPreferences().getString(this.mKey, findSupportedDefaultValue());
            this.mLoaded = true;
        }
        return this.mValue;
    }

    public String getOffValue() {
        return this.mEntryValues[0].toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0025, code lost:
        r1 = r1 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String findSupportedDefaultValue() {
        /*
            r5 = this;
            r0 = 0
            r1 = r0
        L_0x0002:
            java.lang.CharSequence[] r2 = r5.mDefaultValues
            int r2 = r2.length
            if (r1 >= r2) goto L_0x0028
            r2 = r0
        L_0x0008:
            java.lang.CharSequence[] r3 = r5.mEntryValues
            int r4 = r3.length
            if (r2 >= r4) goto L_0x0025
            r3 = r3[r2]
            java.lang.CharSequence[] r4 = r5.mDefaultValues
            r4 = r4[r1]
            boolean r3 = r3.equals(r4)
            if (r3 == 0) goto L_0x0022
            java.lang.CharSequence[] r5 = r5.mDefaultValues
            r5 = r5[r1]
            java.lang.String r5 = r5.toString()
            return r5
        L_0x0022:
            int r2 = r2 + 1
            goto L_0x0008
        L_0x0025:
            int r1 = r1 + 1
            goto L_0x0002
        L_0x0028:
            r5 = 0
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.ListPreference.findSupportedDefaultValue():java.lang.String");
    }

    public void setValue(String str) {
        if (findIndexOfValue(str) < 0) {
            str = findSupportedDefaultValue();
        }
        this.mValue = str;
        persistStringValue(str);
    }

    public void setMakeupSeekBarValue(String str) {
        this.mValue = str;
        persistStringValue(str);
    }

    public void setValueIndex(int i) {
        setValue(this.mEntryValues[i].toString());
    }

    public int findIndexOfValue(String str) {
        int length = this.mEntryValues.length;
        for (int i = 0; i < length; i++) {
            if (CameraUtil.equals(this.mEntryValues[i], str)) {
                return i;
            }
        }
        return -1;
    }

    public int getCurrentIndex() {
        return findIndexOfValue(getValue());
    }

    public String getEntry() {
        int findIndexOfValue = findIndexOfValue(getValue());
        if (findIndexOfValue < 0) {
            return findSupportedDefaultValue();
        }
        return this.mEntries[findIndexOfValue].toString();
    }

    public String getLabel() {
        return this.mLabels[findIndexOfValue(getValue())].toString();
    }

    /* access modifiers changed from: protected */
    public void persistStringValue(String str) {
        Editor edit = getSharedPreferences().edit();
        edit.putString(this.mKey, str);
        edit.apply();
    }

    public void reloadValue() {
        this.mLoaded = false;
    }

    public void filterUnsupported(List<String> list) {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        int length = this.mEntryValues.length;
        for (int i = 0; i < length; i++) {
            if (list.indexOf(this.mEntryValues[i].toString()) >= 0) {
                arrayList.add(this.mEntries[i]);
                arrayList2.add(this.mEntryValues[i]);
            }
        }
        int size = arrayList.size();
        this.mEntries = (CharSequence[]) arrayList.toArray(new CharSequence[size]);
        this.mEntryValues = (CharSequence[]) arrayList2.toArray(new CharSequence[size]);
    }

    public void filterDuplicated() {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        int length = this.mEntryValues.length;
        for (int i = 0; i < length; i++) {
            if (!arrayList.contains(this.mEntries[i])) {
                arrayList.add(this.mEntries[i]);
                arrayList2.add(this.mEntryValues[i]);
            }
        }
        int size = arrayList.size();
        this.mEntries = (CharSequence[]) arrayList.toArray(new CharSequence[size]);
        this.mEntryValues = (CharSequence[]) arrayList2.toArray(new CharSequence[size]);
    }

    public void reloadInitialEntriesAndEntryValues() {
        this.mEntries = this.mInitialEntries;
        this.mEntryValues = this.mInitialEntryValues;
    }

    public void print() {
        StringBuilder sb = new StringBuilder();
        sb.append("Preference key=");
        sb.append(getKey());
        sb.append(". value=");
        sb.append(getValue());
        String sb2 = sb.toString();
        String str = TAG;
        Log.v(str, sb2);
        for (int i = 0; i < this.mEntryValues.length; i++) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("entryValues[");
            sb3.append(i);
            sb3.append("]=");
            sb3.append(this.mEntryValues[i]);
            Log.v(str, sb3.toString());
        }
    }
}
