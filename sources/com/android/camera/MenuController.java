package com.android.camera;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.android.camera.CameraPreference.OnPreferenceChangedListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuController {
    private static String TAG = "CAM_menucontrol";
    protected Activity mActivity;
    protected OnPreferenceChangedListener mListener;
    private Map<IconListPreference, String> mOverrides = new HashMap();
    protected PreferenceGroup mPreferenceGroup;
    protected Map<IconListPreference, View> mPreferenceMap = new HashMap();
    protected List<IconListPreference> mPreferences = new ArrayList();

    public void setListener(OnPreferenceChangedListener onPreferenceChangedListener) {
        this.mListener = onPreferenceChangedListener;
    }

    public MenuController(Activity activity) {
        this.mActivity = activity;
    }

    public void initialize(PreferenceGroup preferenceGroup) {
        this.mPreferenceMap.clear();
        setPreferenceGroup(preferenceGroup);
        this.mPreferences.clear();
        this.mOverrides.clear();
    }

    public void onSettingChanged(ListPreference listPreference) {
        OnPreferenceChangedListener onPreferenceChangedListener = this.mListener;
        if (onPreferenceChangedListener != null) {
            onPreferenceChangedListener.onSharedPreferenceChanged(listPreference);
        }
    }

    public void setPreferenceGroup(PreferenceGroup preferenceGroup) {
        this.mPreferenceGroup = preferenceGroup;
    }

    public void reloadPreferences() {
        this.mPreferenceGroup.reloadValue();
        for (IconListPreference reloadPreference : this.mPreferences) {
            reloadPreference(reloadPreference);
        }
    }

    /* access modifiers changed from: protected */
    public void reloadPreference(IconListPreference iconListPreference) {
        int i;
        View view = (View) this.mPreferenceMap.get(iconListPreference);
        if (view != null) {
            String str = (String) this.mOverrides.get(iconListPreference);
            if (str == null) {
                i = iconListPreference.findIndexOfValue(iconListPreference.getValue());
            } else {
                int findIndexOfValue = iconListPreference.findIndexOfValue(str);
                if (findIndexOfValue == -1) {
                    String str2 = TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Fail to find override value=");
                    sb.append(str);
                    Log.e(str2, sb.toString());
                    iconListPreference.print();
                    return;
                }
                i = findIndexOfValue;
            }
            ((ImageView) view).setImageResource(iconListPreference.getLargeIconIds()[i]);
        }
    }

    public void overrideSettings(String... strArr) {
        if (strArr.length % 2 == 0) {
            for (IconListPreference override : this.mPreferences) {
                override(override, strArr);
            }
            return;
        }
        throw new IllegalArgumentException();
    }

    private void override(IconListPreference iconListPreference, String... strArr) {
        this.mOverrides.remove(iconListPreference);
        int i = 0;
        while (true) {
            if (i >= strArr.length) {
                break;
            }
            String str = strArr[i];
            String str2 = strArr[i + 1];
            if (str.equals(iconListPreference.getKey())) {
                this.mOverrides.put(iconListPreference, str2);
                break;
            }
            i += 2;
        }
        reloadPreference(iconListPreference);
    }
}
