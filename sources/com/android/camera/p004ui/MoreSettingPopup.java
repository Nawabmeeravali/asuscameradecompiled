package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.android.camera.ListPreference;
import com.android.camera.PreferenceGroup;
import java.util.ArrayList;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.MoreSettingPopup */
public class MoreSettingPopup extends AbstractSettingPopup implements com.android.camera.p004ui.InLineSettingItem.Listener, OnItemClickListener {
    private static final String TAG = "MoreSettingPopup";
    /* access modifiers changed from: private */
    public boolean[] mEnabled;
    /* access modifiers changed from: private */
    public ArrayList<ListPreference> mListItem = new ArrayList<>();
    private Listener mListener;

    /* renamed from: com.android.camera.ui.MoreSettingPopup$Listener */
    public interface Listener {
        void onPreferenceClicked(ListPreference listPreference);

        void onSettingChanged(ListPreference listPreference);
    }

    /* renamed from: com.android.camera.ui.MoreSettingPopup$MoreSettingAdapter */
    private class MoreSettingAdapter extends ArrayAdapter<ListPreference> {
        LayoutInflater mInflater;
        String mOffString;
        String mOnString;

        MoreSettingAdapter() {
            super(MoreSettingPopup.this.getContext(), 0, MoreSettingPopup.this.mListItem);
            Context context = getContext();
            this.mInflater = LayoutInflater.from(context);
            this.mOnString = context.getString(C0905R.string.setting_on);
            this.mOffString = context.getString(C0905R.string.setting_off);
        }

        private int getSettingLayoutId(ListPreference listPreference) {
            return isOnOffPreference(listPreference) ? C0905R.layout.in_line_setting_check_box : C0905R.layout.in_line_setting_menu;
        }

        private boolean isOnOffPreference(ListPreference listPreference) {
            CharSequence[] entries = listPreference.getEntries();
            boolean z = false;
            if (entries.length != 2) {
                return false;
            }
            String charSequence = entries[0].toString();
            String charSequence2 = entries[1].toString();
            if ((charSequence.equals(this.mOnString) && charSequence2.equals(this.mOffString)) || (charSequence.equals(this.mOffString) && charSequence2.equals(this.mOnString))) {
                z = true;
            }
            return z;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            ListPreference listPreference = (ListPreference) MoreSettingPopup.this.mListItem.get(i);
            InLineSettingItem inLineSettingItem = (InLineSettingItem) view;
            InLineSettingItem inLineSettingItem2 = (InLineSettingItem) this.mInflater.inflate(getSettingLayoutId(listPreference), viewGroup, false);
            inLineSettingItem2.initialize(listPreference);
            inLineSettingItem2.setSettingChangedListener(MoreSettingPopup.this);
            if (i < 0 || i >= MoreSettingPopup.this.mEnabled.length) {
                StringBuilder sb = new StringBuilder();
                sb.append("Invalid input: enabled list length, ");
                sb.append(MoreSettingPopup.this.mEnabled.length);
                sb.append(" position ");
                sb.append(i);
                Log.w(MoreSettingPopup.TAG, sb.toString());
            } else {
                inLineSettingItem2.setEnabled(MoreSettingPopup.this.mEnabled[i]);
            }
            return inLineSettingItem2;
        }

        public boolean isEnabled(int i) {
            if (i < 0 || i >= MoreSettingPopup.this.mEnabled.length) {
                return true;
            }
            return MoreSettingPopup.this.mEnabled[i];
        }
    }

    public void setSettingChangedListener(Listener listener) {
        this.mListener = listener;
    }

    public MoreSettingPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void initialize(PreferenceGroup preferenceGroup, String[] strArr) {
        int i = 0;
        for (String findPreference : strArr) {
            ListPreference findPreference2 = preferenceGroup.findPreference(findPreference);
            if (findPreference2 != null) {
                this.mListItem.add(findPreference2);
            }
        }
        ((ListView) this.mSettingList).setAdapter(new MoreSettingAdapter());
        ((ListView) this.mSettingList).setOnItemClickListener(this);
        ((ListView) this.mSettingList).setSelector(17170445);
        this.mEnabled = new boolean[this.mListItem.size()];
        while (true) {
            boolean[] zArr = this.mEnabled;
            if (i < zArr.length) {
                zArr[i] = true;
                i++;
            } else {
                return;
            }
        }
    }

    public void setPreferenceEnabled(String str, boolean z) {
        boolean[] zArr = this.mEnabled;
        int i = 0;
        int length = zArr == null ? 0 : zArr.length;
        while (i < length) {
            ListPreference listPreference = (ListPreference) this.mListItem.get(i);
            if (listPreference == null || !str.equals(listPreference.getKey())) {
                i++;
            } else {
                this.mEnabled[i] = z;
                return;
            }
        }
    }

    public void onSettingChanged(ListPreference listPreference) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onSettingChanged(listPreference);
        }
    }

    public void overrideSettings(String... strArr) {
        boolean[] zArr = this.mEnabled;
        int length = zArr == null ? 0 : zArr.length;
        for (int i = 0; i < strArr.length; i += 2) {
            String str = strArr[i];
            String str2 = strArr[i + 1];
            for (int i2 = 0; i2 < length; i2++) {
                ListPreference listPreference = (ListPreference) this.mListItem.get(i2);
                if (listPreference != null && str.equals(listPreference.getKey())) {
                    if (str2 != null) {
                        listPreference.setValue(str2);
                    }
                    boolean z = str2 == null;
                    this.mEnabled[i2] = z;
                    if (this.mSettingList.getChildCount() > i2) {
                        this.mSettingList.getChildAt(i2).setEnabled(z);
                    }
                }
            }
        }
        reloadPreference();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
        if (this.mListener != null) {
            this.mListener.onPreferenceClicked((ListPreference) this.mListItem.get(i));
        }
    }

    public void reloadPreference() {
        int childCount = this.mSettingList.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (((ListPreference) this.mListItem.get(i)) != null) {
                ((InLineSettingItem) this.mSettingList.getChildAt(i)).reloadPreference();
            }
        }
    }
}
