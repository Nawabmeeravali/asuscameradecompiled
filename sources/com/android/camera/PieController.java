package com.android.camera;

import android.app.Activity;
import android.util.Log;
import com.android.camera.CameraPreference.OnPreferenceChangedListener;
import com.android.camera.drawable.TextDrawable;
import com.android.camera.p004ui.PieItem;
import com.android.camera.p004ui.PieItem.OnClickListener;
import com.android.camera.p004ui.PieRenderer;
import com.android.camera.p004ui.RotateTextToast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codeaurora.snapcam.C0905R;

public class PieController {
    private static String TAG = "CAM_piecontrol";
    protected Activity mActivity;
    protected OnPreferenceChangedListener mListener;
    private Map<IconListPreference, String> mOverrides = new HashMap();
    protected PreferenceGroup mPreferenceGroup;
    private Map<IconListPreference, PieItem> mPreferenceMap = new HashMap();
    private List<IconListPreference> mPreferences = new ArrayList();
    protected PieRenderer mRenderer;

    public void setListener(OnPreferenceChangedListener onPreferenceChangedListener) {
        this.mListener = onPreferenceChangedListener;
    }

    public PieController(Activity activity, PieRenderer pieRenderer) {
        this.mActivity = activity;
        this.mRenderer = pieRenderer;
    }

    public void initialize(PreferenceGroup preferenceGroup) {
        this.mRenderer.clearItems();
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

    /* access modifiers changed from: protected */
    public PieItem makeItem(int i) {
        return new PieItem(this.mActivity.getResources().getDrawable(i).mutate(), 0);
    }

    /* access modifiers changed from: protected */
    public PieItem makeItem(CharSequence charSequence) {
        return new PieItem(new TextDrawable(this.mActivity.getResources(), charSequence), 0);
    }

    public PieItem makeItem(String str) {
        int i;
        PieItem pieItem;
        final IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(str);
        if (iconListPreference == null) {
            return null;
        }
        int[] largeIconIds = iconListPreference.getLargeIconIds();
        if (iconListPreference.getUseSingleIcon() || largeIconIds == null) {
            i = iconListPreference.getSingleIcon();
        } else {
            i = largeIconIds[iconListPreference.findIndexOfValue(iconListPreference.getValue())];
        }
        PieItem makeItem = makeItem(i);
        makeItem.setLabel(iconListPreference.getTitle().toUpperCase());
        this.mPreferences.add(iconListPreference);
        this.mPreferenceMap.put(iconListPreference, makeItem);
        int length = iconListPreference.getEntries().length;
        if (length > 1) {
            for (final int i2 = 0; i2 < length; i2++) {
                if (largeIconIds != null) {
                    pieItem = makeItem(largeIconIds[i2]);
                } else {
                    pieItem = makeItem(iconListPreference.getEntries()[i2]);
                }
                pieItem.setLabel(iconListPreference.getLabels()[i2]);
                makeItem.addItem(pieItem);
                pieItem.setOnClickListener(new OnClickListener() {
                    public void onClick(PieItem pieItem) {
                        iconListPreference.setValueIndex(i2);
                        PieController.this.reloadPreference(iconListPreference);
                        PieController.this.onSettingChanged(iconListPreference);
                    }
                });
            }
        }
        return makeItem;
    }

    public PieItem makeSwitchItem(final String str, boolean z) {
        int i;
        IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(str);
        if (iconListPreference == null) {
            return null;
        }
        int[] largeIconIds = iconListPreference.getLargeIconIds();
        int findIndexOfValue = iconListPreference.findIndexOfValue(iconListPreference.getValue());
        if (iconListPreference.getUseSingleIcon() || largeIconIds == null) {
            i = iconListPreference.getSingleIcon();
        } else {
            i = largeIconIds[findIndexOfValue];
        }
        final PieItem makeItem = makeItem(i);
        makeItem.setLabel(iconListPreference.getLabels()[findIndexOfValue]);
        makeItem.setImageResource(this.mActivity, i);
        this.mPreferences.add(iconListPreference);
        this.mPreferenceMap.put(iconListPreference, makeItem);
        if (z) {
            makeItem.setOnClickListener(new OnClickListener() {
                public void onClick(PieItem pieItem) {
                    if (pieItem.isEnabled()) {
                        IconListPreference iconListPreference = (IconListPreference) PieController.this.mPreferenceGroup.findPreference(str);
                        int findIndexOfValue = (iconListPreference.findIndexOfValue(iconListPreference.getValue()) + 1) % iconListPreference.getEntryValues().length;
                        iconListPreference.setValueIndex(findIndexOfValue);
                        if (findIndexOfValue == 1 && str == CameraSettings.KEY_CAMERA_HDR) {
                            RotateTextToast.makeText(PieController.this.mActivity, (int) C0905R.string.HDR_disable_continuous_shot, 1).show();
                        }
                        makeItem.setLabel(iconListPreference.getLabels()[findIndexOfValue]);
                        makeItem.setImageResource(PieController.this.mActivity, iconListPreference.getLargeIconIds()[findIndexOfValue]);
                        PieController.this.reloadPreference(iconListPreference);
                        PieController.this.onSettingChanged(iconListPreference);
                    }
                }
            });
        }
        return makeItem;
    }

    public PieItem makeDialItem(ListPreference listPreference, int i, float f, float f2) {
        return makeItem(i);
    }

    public void addItem(String str) {
        this.mRenderer.addItem(makeItem(str));
    }

    public void updateItem(PieItem pieItem, String str) {
        IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(str);
        if (iconListPreference != null) {
            int findIndexOfValue = iconListPreference.findIndexOfValue(iconListPreference.getValue());
            pieItem.setLabel(iconListPreference.getLabels()[findIndexOfValue]);
            pieItem.setImageResource(this.mActivity, iconListPreference.getLargeIconIds()[findIndexOfValue]);
        }
    }

    public void setPreferenceGroup(PreferenceGroup preferenceGroup) {
        this.mPreferenceGroup = preferenceGroup;
    }

    public void reloadPreferences() {
        this.mPreferenceGroup.reloadValue();
        for (IconListPreference reloadPreference : this.mPreferenceMap.keySet()) {
            reloadPreference(reloadPreference);
        }
    }

    /* access modifiers changed from: private */
    public void reloadPreference(IconListPreference iconListPreference) {
        int i;
        if (!iconListPreference.getUseSingleIcon()) {
            PieItem pieItem = (PieItem) this.mPreferenceMap.get(iconListPreference);
            String str = (String) this.mOverrides.get(iconListPreference);
            int[] largeIconIds = iconListPreference.getLargeIconIds();
            if (largeIconIds != null) {
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
                pieItem.setImageResource(this.mActivity, largeIconIds[i]);
            } else {
                pieItem.setImageResource(this.mActivity, iconListPreference.getSingleIcon());
            }
        }
    }

    public void overrideSettings(String... strArr) {
        if (strArr.length % 2 == 0) {
            for (IconListPreference override : this.mPreferenceMap.keySet()) {
                override(override, strArr);
            }
            return;
        }
        throw new IllegalArgumentException();
    }

    private void override(IconListPreference iconListPreference, String... strArr) {
        this.mOverrides.remove(iconListPreference);
        boolean z = false;
        int i = 0;
        while (true) {
            if (i >= strArr.length) {
                break;
            }
            String str = strArr[i];
            String str2 = strArr[i + 1];
            if (str.equals(iconListPreference.getKey())) {
                this.mOverrides.put(iconListPreference, str2);
                PieItem pieItem = (PieItem) this.mPreferenceMap.get(iconListPreference);
                if (str2 == null) {
                    z = true;
                }
                pieItem.setEnabled(z);
            } else {
                i += 2;
            }
        }
        reloadPreference(iconListPreference);
    }
}
