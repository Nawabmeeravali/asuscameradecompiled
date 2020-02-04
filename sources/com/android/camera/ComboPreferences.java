package com.android.camera;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ComboPreferences implements SharedPreferences, OnSharedPreferenceChangeListener {
    private static WeakHashMap<Context, ComboPreferences> sMap = new WeakHashMap<>();
    private CopyOnWriteArrayList<OnSharedPreferenceChangeListener> mListeners;
    private String mPackageName;
    /* access modifiers changed from: private */
    public SharedPreferences mPrefGlobal;
    /* access modifiers changed from: private */
    public SharedPreferences mPrefLocal;

    private class MyEditor implements Editor {
        private Editor mEditorGlobal;
        private Editor mEditorLocal;

        MyEditor() {
            this.mEditorGlobal = ComboPreferences.this.mPrefGlobal.edit();
            this.mEditorLocal = ComboPreferences.this.mPrefLocal.edit();
        }

        public boolean commit() {
            return this.mEditorGlobal.commit() && this.mEditorLocal.commit();
        }

        public void apply() {
            this.mEditorGlobal.apply();
            this.mEditorLocal.apply();
        }

        public Editor clear() {
            this.mEditorGlobal.clear();
            this.mEditorLocal.clear();
            return this;
        }

        public Editor remove(String str) {
            this.mEditorGlobal.remove(str);
            this.mEditorLocal.remove(str);
            return this;
        }

        public Editor putString(String str, String str2) {
            if (ComboPreferences.isGlobal(str)) {
                this.mEditorGlobal.putString(str, str2);
            } else {
                this.mEditorLocal.putString(str, str2);
            }
            return this;
        }

        public Editor putInt(String str, int i) {
            if (ComboPreferences.isGlobal(str)) {
                this.mEditorGlobal.putInt(str, i);
            } else {
                this.mEditorLocal.putInt(str, i);
            }
            return this;
        }

        public Editor putLong(String str, long j) {
            if (ComboPreferences.isGlobal(str)) {
                this.mEditorGlobal.putLong(str, j);
            } else {
                this.mEditorLocal.putLong(str, j);
            }
            return this;
        }

        public Editor putFloat(String str, float f) {
            if (ComboPreferences.isGlobal(str)) {
                this.mEditorGlobal.putFloat(str, f);
            } else {
                this.mEditorLocal.putFloat(str, f);
            }
            return this;
        }

        public Editor putBoolean(String str, boolean z) {
            if (ComboPreferences.isGlobal(str)) {
                this.mEditorGlobal.putBoolean(str, z);
            } else {
                this.mEditorLocal.putBoolean(str, z);
            }
            return this;
        }

        public Editor putStringSet(String str, Set<String> set) {
            throw new UnsupportedOperationException();
        }
    }

    public ComboPreferences(Context context) {
        this.mPackageName = context.getPackageName();
        this.mPrefGlobal = context.getSharedPreferences(getGlobalSharedPreferencesName(context), 0);
        this.mPrefGlobal.registerOnSharedPreferenceChangeListener(this);
        synchronized (sMap) {
            sMap.put(context, this);
        }
        this.mListeners = new CopyOnWriteArrayList<>();
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!this.mPrefGlobal.contains(CameraSettings.KEY_VERSION) && defaultSharedPreferences.contains(CameraSettings.KEY_VERSION)) {
            moveGlobalPrefsFrom(defaultSharedPreferences);
        }
    }

    public static ComboPreferences get(Context context) {
        ComboPreferences comboPreferences;
        synchronized (sMap) {
            comboPreferences = (ComboPreferences) sMap.get(context);
        }
        return comboPreferences;
    }

    public static String getLocalSharedPreferencesName(Context context, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getPackageName());
        sb.append("_preferences_");
        sb.append(i);
        return sb.toString();
    }

    public static String getGlobalSharedPreferencesName(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getPackageName());
        sb.append("_preferences_camera");
        return sb.toString();
    }

    private void movePrefFrom(Map<String, ?> map, String str, SharedPreferences sharedPreferences) {
        if (map.containsKey(str)) {
            Object obj = map.get(str);
            if (obj instanceof String) {
                this.mPrefGlobal.edit().putString(str, (String) obj).apply();
            } else if (obj instanceof Integer) {
                this.mPrefGlobal.edit().putInt(str, ((Integer) obj).intValue()).apply();
            } else if (obj instanceof Long) {
                this.mPrefGlobal.edit().putLong(str, ((Long) obj).longValue()).apply();
            } else if (obj instanceof Float) {
                this.mPrefGlobal.edit().putFloat(str, ((Float) obj).floatValue()).apply();
            } else if (obj instanceof Boolean) {
                this.mPrefGlobal.edit().putBoolean(str, ((Boolean) obj).booleanValue()).apply();
            }
            sharedPreferences.edit().remove(str).apply();
        }
    }

    private void moveGlobalPrefsFrom(SharedPreferences sharedPreferences) {
        Map all = sharedPreferences.getAll();
        movePrefFrom(all, CameraSettings.KEY_VERSION, sharedPreferences);
        movePrefFrom(all, CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL, sharedPreferences);
        movePrefFrom(all, CameraSettings.KEY_CAMERA_ID, sharedPreferences);
        movePrefFrom(all, "pref_camera_recordlocation_key", sharedPreferences);
        movePrefFrom(all, CameraSettings.KEY_CAMERA_FIRST_USE_HINT_SHOWN, sharedPreferences);
        movePrefFrom(all, CameraSettings.KEY_VIDEO_FIRST_USE_HINT_SHOWN, sharedPreferences);
        movePrefFrom(all, CameraSettings.KEY_VIDEO_EFFECT, sharedPreferences);
        movePrefFrom(all, "pref_camera_savepath_key", sharedPreferences);
    }

    public static String[] getSharedPreferencesNames(Context context) {
        int numberOfCameras = CameraHolder.instance().getNumberOfCameras();
        String[] strArr = new String[(numberOfCameras + 1)];
        int i = 0;
        strArr[0] = getGlobalSharedPreferencesName(context);
        while (i < numberOfCameras) {
            int i2 = i + 1;
            strArr[i2] = getLocalSharedPreferencesName(context, i);
            i = i2;
        }
        return strArr;
    }

    public void setLocalId(Context context, int i) {
        String localSharedPreferencesName = getLocalSharedPreferencesName(context, i);
        SharedPreferences sharedPreferences = this.mPrefLocal;
        if (sharedPreferences != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
        this.mPrefLocal = context.getSharedPreferences(localSharedPreferencesName, 0);
        this.mPrefLocal.registerOnSharedPreferenceChangeListener(this);
    }

    public SharedPreferences getGlobal() {
        return this.mPrefGlobal;
    }

    public SharedPreferences getLocal() {
        return this.mPrefLocal;
    }

    public Map<String, ?> getAll() {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: private */
    public static boolean isGlobal(String str) {
        return str.equals(CameraSettings.KEY_CAMERA_ID) || str.equals("pref_camera_recordlocation_key") || str.equals(CameraSettings.KEY_CAMERA_FIRST_USE_HINT_SHOWN) || str.equals(CameraSettings.KEY_VIDEO_FIRST_USE_HINT_SHOWN) || str.equals(CameraSettings.KEY_VIDEO_EFFECT) || str.equals("pref_camera_timer_key") || str.equals(CameraSettings.KEY_TIMER_SOUND_EFFECTS) || str.equals(CameraSettings.KEY_PHOTOSPHERE_PICTURESIZE) || str.equals("pref_camera_savepath_key") || str.equals(SettingsManager.KEY_CAMERA_ID) || str.equals(SettingsManager.KEY_MONO_ONLY) || str.equals(SettingsManager.KEY_MONO_PREVIEW) || str.equals(SettingsManager.KEY_SWITCH_CAMERA) || str.equals(SettingsManager.KEY_CLEARSIGHT);
    }

    public String getString(String str, String str2) {
        if (isGlobal(str) || !this.mPrefLocal.contains(str)) {
            return this.mPrefGlobal.getString(str, str2);
        }
        return this.mPrefLocal.getString(str, str2);
    }

    public int getInt(String str, int i) {
        if (isGlobal(str) || !this.mPrefLocal.contains(str)) {
            return this.mPrefGlobal.getInt(str, i);
        }
        return this.mPrefLocal.getInt(str, i);
    }

    public long getLong(String str, long j) {
        if (isGlobal(str) || !this.mPrefLocal.contains(str)) {
            return this.mPrefGlobal.getLong(str, j);
        }
        return this.mPrefLocal.getLong(str, j);
    }

    public float getFloat(String str, float f) {
        if (isGlobal(str) || !this.mPrefLocal.contains(str)) {
            return this.mPrefGlobal.getFloat(str, f);
        }
        return this.mPrefLocal.getFloat(str, f);
    }

    public boolean getBoolean(String str, boolean z) {
        if (isGlobal(str) || !this.mPrefLocal.contains(str)) {
            return this.mPrefGlobal.getBoolean(str, z);
        }
        return this.mPrefLocal.getBoolean(str, z);
    }

    public Set<String> getStringSet(String str, Set<String> set) {
        throw new UnsupportedOperationException();
    }

    public boolean contains(String str) {
        return this.mPrefLocal.contains(str) || this.mPrefGlobal.contains(str);
    }

    public Editor edit() {
        return new MyEditor();
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        this.mListeners.add(onSharedPreferenceChangeListener);
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        this.mListeners.remove(onSharedPreferenceChangeListener);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        Iterator it = this.mListeners.iterator();
        while (it.hasNext()) {
            ((OnSharedPreferenceChangeListener) it.next()).onSharedPreferenceChanged(this, str);
        }
        BackupManager.dataChanged(this.mPackageName);
    }
}
