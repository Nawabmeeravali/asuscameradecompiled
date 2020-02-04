package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.android.camera.ListPreference;
import com.android.camera.PreferenceGroup;
import com.android.camera.SettingsManager;
import java.util.ArrayList;
import java.util.List;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.ListMenu */
public class ListMenu extends ListView implements com.android.camera.p004ui.ListMenuItem.Listener, OnItemClickListener, com.android.camera.p004ui.ListSubMenu.Listener {
    private static final String TAG = "ListMenu";
    /* access modifiers changed from: private */
    public boolean[] mEnabled;
    /* access modifiers changed from: private */
    public boolean mForCamera2 = false;
    /* access modifiers changed from: private */
    public int mHighlighted = -1;
    /* access modifiers changed from: private */
    public ArrayList<ListPreference> mListItem = new ArrayList<>();
    private Listener mListener;
    /* access modifiers changed from: private */
    public SettingsManager mSettingsManager;

    /* renamed from: com.android.camera.ui.ListMenu$Listener */
    public interface Listener {
        void onListMenuTouched();

        void onPreferenceClicked(ListPreference listPreference);

        void onPreferenceClicked(ListPreference listPreference, int i);

        void onSettingChanged(ListPreference listPreference);
    }

    /* renamed from: com.android.camera.ui.ListMenu$MoreSettingAdapter */
    private class MoreSettingAdapter extends ArrayAdapter<ListPreference> {
        LayoutInflater mInflater;
        String mOffString;
        String mOnString;

        private int getSettingLayoutId(ListPreference listPreference) {
            return C0905R.layout.list_menu_item;
        }

        MoreSettingAdapter() {
            super(ListMenu.this.getContext(), 0, ListMenu.this.mListItem);
            Context context = getContext();
            this.mInflater = LayoutInflater.from(context);
            this.mOnString = context.getString(C0905R.string.setting_on);
            this.mOffString = context.getString(C0905R.string.setting_off);
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            ListPreference listPreference = (ListPreference) ListMenu.this.mListItem.get(i);
            ListMenuItem listMenuItem = (ListMenuItem) view;
            ListMenuItem listMenuItem2 = (ListMenuItem) this.mInflater.inflate(getSettingLayoutId(listPreference), viewGroup, false);
            listMenuItem2.initialize(listPreference);
            listMenuItem2.setSettingChangedListener(ListMenu.this);
            if (i < 0 || i >= ListMenu.this.mEnabled.length) {
                StringBuilder sb = new StringBuilder();
                sb.append("Invalid input: enabled list length, ");
                sb.append(ListMenu.this.mEnabled.length);
                sb.append(" position ");
                sb.append(i);
                Log.w(ListMenu.TAG, sb.toString());
            } else {
                listMenuItem2.setEnabled(ListMenu.this.mEnabled[i]);
                if (ListMenu.this.mForCamera2 && !ListMenu.this.mEnabled[i]) {
                    listMenuItem2.overrideSettings(ListMenu.this.mSettingsManager.getValue(listPreference.getKey()));
                }
            }
            if (i == ListMenu.this.mHighlighted) {
                listMenuItem2.setBackgroundColor(getContext().getResources().getColor(C0905R.color.setting_color));
            }
            return listMenuItem2;
        }

        public boolean isEnabled(int i) {
            if (i < 0 || i >= ListMenu.this.mEnabled.length) {
                return true;
            }
            return ListMenu.this.mEnabled[i];
        }
    }

    /* renamed from: com.android.camera.ui.ListMenu$SettingsListener */
    public interface SettingsListener {
        void onSettingChanged(ListPreference listPreference);
    }

    public void onListPrefChanged(ListPreference listPreference) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onSettingChanged(listPreference);
        }
        SettingsManager settingsManager = this.mSettingsManager;
        if (settingsManager != null) {
            settingsManager.onSettingChanged(listPreference);
        }
    }

    public void setSettingsManager(SettingsManager settingsManager) {
        this.mSettingsManager = settingsManager;
    }

    public void setSettingChangedListener(Listener listener) {
        this.mListener = listener;
    }

    public ListMenu(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void initializeForCamera2(String[] strArr) {
        this.mForCamera2 = true;
        PreferenceGroup preferenceGroup = this.mSettingsManager.getPreferenceGroup();
        List<String> disabledList = this.mSettingsManager.getDisabledList();
        for (String findPreference : strArr) {
            ListPreference findPreference2 = preferenceGroup.findPreference(findPreference);
            if (findPreference2 != null) {
                this.mListItem.add(findPreference2);
            }
        }
        setAdapter(new MoreSettingAdapter());
        setOnItemClickListener(this);
        setSelector(17170445);
        this.mEnabled = new boolean[this.mListItem.size()];
        int i = 0;
        while (true) {
            boolean[] zArr = this.mEnabled;
            if (i >= zArr.length) {
                break;
            }
            zArr[i] = true;
            i++;
        }
        for (String preferenceEnabled : disabledList) {
            setPreferenceEnabled(preferenceEnabled, false);
        }
    }

    public void initialize(PreferenceGroup preferenceGroup, String[] strArr) {
        int i = 0;
        for (String findPreference : strArr) {
            ListPreference findPreference2 = preferenceGroup.findPreference(findPreference);
            if (findPreference2 != null) {
                this.mListItem.add(findPreference2);
            }
        }
        setAdapter(new MoreSettingAdapter());
        setOnItemClickListener(this);
        setSelector(17170445);
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
                    int firstVisiblePosition = getFirstVisiblePosition();
                    if (firstVisiblePosition >= 0) {
                        int i3 = i2 - firstVisiblePosition;
                        if (getChildCount() > i3 && i3 >= 0) {
                            getChildAt(i3).setEnabled(z);
                        }
                    }
                }
            }
        }
        reloadPreference();
    }

    public void resetHighlight() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).setBackground(null);
        }
        this.mHighlighted = -1;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 2) {
            this.mListener.onListMenuTouched();
            resetHighlight();
        }
        return super.onTouchEvent(motionEvent);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
        if (this.mListener != null) {
            resetHighlight();
            ListPreference listPreference = (ListPreference) this.mListItem.get(i);
            this.mHighlighted = i;
            view.setBackgroundColor(getContext().getResources().getColor(C0905R.color.setting_color));
            this.mListener.onPreferenceClicked(listPreference, (int) view.getY());
        }
    }

    public void reloadPreference() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (((ListPreference) this.mListItem.get(i)) != null) {
                ((ListMenuItem) getChildAt(i)).reloadPreference();
            }
        }
    }
}
