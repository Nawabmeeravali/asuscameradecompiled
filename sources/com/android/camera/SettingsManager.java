package com.android.camera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.EncoderCapabilities;
import android.media.EncoderCapabilities.VideoEncoderCap;
import android.media.MediaRecorder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import com.android.camera.exif.ExifInterface.GpsMeasureMode;
import com.android.camera.imageprocessor.filter.BestpictureFilter;
import com.android.camera.imageprocessor.filter.BlurbusterFilter;
import com.android.camera.imageprocessor.filter.ChromaflashFilter;
import com.android.camera.imageprocessor.filter.OptizoomFilter;
import com.android.camera.imageprocessor.filter.SharpshooterFilter;
import com.android.camera.imageprocessor.filter.TrackingFocusFrameListener;
import com.android.camera.imageprocessor.filter.UbifocusFilter;
import com.android.camera.p004ui.ListMenu.SettingsListener;
import com.android.camera.p004ui.PanoCaptureProcessView;
import com.android.camera.util.SettingTranslation;
import com.asus.scenedetectlib.BuildConfig;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.codeaurora.snapcam.C0905R;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsManager implements SettingsListener {
    public static final int BACK_CAMERA = 0;
    public static final String BOKEH_MODE_OFF = "0";
    public static final int FRONT_CAMERA = 1;
    public static final int HEIF_FORMAT = 1;
    public static final int JPEG_FORMAT = 0;
    public static final String KEY_ABORT_CAPTURES = "pref_camera2_abort_captures_key";
    public static final String KEY_AF_MODE = "pref_camera2_afmode_key";
    public static final String KEY_ANTI_BANDING_LEVEL = "pref_camera2_anti_banding_level_key";
    public static final String KEY_AUDIO_ENCODER = "pref_camera_audioencoder_key";
    public static final String KEY_AUTO_HDR = "pref_camera2_auto_hdr_key";
    public static final String KEY_BOKEH = "pref_camera2_bokeh_key";
    public static final String KEY_BOKEH_BLUR_DEGREE = "pref_camera2_bokeh_blur_key";
    public static final String KEY_BOKEH_MODE = "pref_camera2_bokeh_mode";
    public static final String KEY_BSGC_DETECTION = "pref_camera2_bsgc_key";
    public static final String KEY_CAMERA_ID = "pref_camera2_id_key";
    public static final String KEY_CAMERA_SAVEPATH = "pref_camera_savepath_key";
    public static final String KEY_CAPTURE_SWMFNR_VALUE = "pref_camera2_capture_swmfnr_key";
    public static final String KEY_CLEARSIGHT = "pref_camera2_clearsight_key";
    public static final String KEY_COLOR_EFFECT = "pref_camera2_coloreffect_key";
    public static final String KEY_DENOISE = "pref_camera2_denoise_key";
    public static final String KEY_DEVELOPER_MENU = "pref_camera2_developer_menu_key";
    public static final String KEY_DIS = "pref_camera_dis_key";
    public static final String KEY_EXPOSURE = "pref_camera_exposure_key";
    public static final String KEY_EXPOSURE_METERING_MODE = "pref_camera2_exposure_metering_key";
    public static final String KEY_FACE_DETECTION = "pref_camera_facedetection_key";
    public static final String KEY_FILTER_MODE = "pref_camera2_filter_mode_key";
    public static final String KEY_FLASH_MODE = "pref_camera2_flashmode_key";
    public static final String KEY_FOCUS_DISTANCE = "pref_camera2_focus_distance_key";
    public static final String KEY_FOCUS_MODE = "pref_camera2_focusmode_key";
    public static final String KEY_HDR = "pref_camera2_hdr_key";
    public static final String KEY_HDR_MODE = "pref_camera2_hdr_mode_key";
    public static final String KEY_HISTOGRAM = "pref_camera2_histogram_key";
    public static final String KEY_INSTANT_AEC = "pref_camera2_instant_aec_key";
    public static final String KEY_ISO = "pref_camera2_iso_key";
    public static final HashMap<String, Integer> KEY_ISO_INDEX = new HashMap<>();
    public static final String KEY_JPEG_QUALITY = "pref_camera_jpegquality_key";
    public static final String KEY_LONGSHOT = "pref_camera_longshot_key";
    public static final String KEY_MAKEUP = "pref_camera2_makeup_key";
    public static final String KEY_MAKEUP_CLEAN_DEGREE = "pref_camera2_makeup_clean_key";
    public static final String KEY_MAKEUP_MODE = "pref_camera2_makeup_mode";
    public static final String KEY_MAKEUP_WHITEN_DEGREE = "pref_camera2_makeup_whiten_key";
    public static final String KEY_MANUAL_EXPOSURE = "pref_camera2_manual_exp_key";
    public static final String KEY_MANUAL_EXPOSURE_VALUE = "pref_camera2_manual_exposure_key";
    public static final String KEY_MANUAL_GAINS_VALUE = "pref_camera2_manual_gains_key";
    public static final String KEY_MANUAL_ISO_VALUE = "pref_camera2_manual_iso_key";
    public static final String KEY_MANUAL_WB = "pref_camera2_manual_wb_key";
    public static final String KEY_MANUAL_WB_B_GAIN = "pref_camera2_manual_wb_b_gain";
    public static final String KEY_MANUAL_WB_G_GAIN = "pref_camera2_manual_wb_g_gain";
    public static final String KEY_MANUAL_WB_R_GAIN = "pref_camera2_manual_wb_r_gain";
    public static final String KEY_MANUAL_WB_TEMPERATURE_VALUE = "pref_camera2_manual_temperature_key";
    public static final String KEY_MONO_ONLY = "pref_camera2_mono_only_key";
    public static final String KEY_MONO_PREVIEW = "pref_camera2_mono_preview_key";
    public static final String KEY_MPO = "pref_camera2_mpo_key";
    public static final String KEY_NOISE_REDUCTION = "pref_camera_noise_reduction_key";
    public static final String KEY_PICTURE_FORMAT = "pref_camera2_picture_format_key";
    public static final String KEY_PICTURE_SIZE = "pref_camera_picturesize_key";
    public static final String KEY_QCFA = "pref_camera2_qcfa_key";
    public static final String KEY_RECORD_LOCATION = "pref_camera_recordlocation_key";
    public static final String KEY_REDEYE_REDUCTION = "pref_camera_redeyereduction_key";
    public static final String KEY_RESTORE_DEFAULT = "pref_camera2_restore_default_key";
    public static final String KEY_SATURATION_LEVEL = "pref_camera2_saturation_level_key";
    public static final String KEY_SAVERAW = "pref_camera2_saveraw_key";
    public static final String KEY_SCEND_MODE_INSTRUCTIONAL = "pref_camera2_scenemode_instructional";
    public static final String KEY_SCENE_MODE = "pref_camera2_scenemode_key";
    public static final String KEY_SELFIEMIRROR = "pref_camera2_selfiemirror_key";
    public static final String KEY_SELFIE_FLASH = "pref_selfie_flash_key";
    public static final String KEY_SHARPNESS_CONTROL_MODE = "pref_camera2_sharpness_control_key";
    public static final String KEY_SHUTTER_SOUND = "pref_camera_shuttersound_key";
    public static final String KEY_SWITCH_CAMERA = "pref_camera2_switch_camera_key";
    public static final String KEY_TIMER = "pref_camera_timer_key";
    public static final String KEY_VIDEO_DURATION = "pref_camera_video_duration_key";
    public static final String KEY_VIDEO_ENCODER = "pref_camera_videoencoder_key";
    public static final String KEY_VIDEO_FLASH_MODE = "pref_camera2_video_flashmode_key";
    public static final String KEY_VIDEO_HDR_VALUE = "pref_camera2_video_hdr_key";
    public static final String KEY_VIDEO_HIGH_FRAME_RATE = "pref_camera2_hfr_key";
    public static final String KEY_VIDEO_QUALITY = "pref_video_quality_key";
    public static final String KEY_VIDEO_ROTATION = "pref_camera_video_rotation_key";
    public static final String KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL = "pref_camera2_video_time_lapse_frame_interval_key";
    public static final String KEY_WHITE_BALANCE = "pref_camera_whitebalance_key";
    public static final String KEY_ZOOM = "pref_camera2_zoom_key";
    public static final String KEY_ZSL = "pref_camera2_zsl_key";
    public static final String MAKEUP_CUSTOM_MODE_OFF = "0";
    public static final String MAKEUP_MODE_OFF = "0";
    public static final String MAUNAL_ABSOLUTE_ISO_VALUE = "absolute";
    public static final int RESOURCE_TYPE_LARGEICON = 1;
    public static final int RESOURCE_TYPE_THUMBNAIL = 0;
    public static final int SCENE_MODE_AUTO_INT = 0;
    public static final int SCENE_MODE_BACKLIGHT_INT = 12;
    public static final int SCENE_MODE_BEACH_INT = 8;
    public static final int SCENE_MODE_BESTPICTURE_INT = 103;
    public static final int SCENE_MODE_BLURBUSTER_INT = 106;
    public static final int SCENE_MODE_BOKEH_INT = 110;
    public static final String SCENE_MODE_BOKEH_STRING = "110";
    public static final int SCENE_MODE_CANDLELIGHT_INT = 15;
    public static final int SCENE_MODE_CHROMAFLASH_INT = 105;
    public static final int SCENE_MODE_CUSTOM_START = 100;
    public static final int SCENE_MODE_DUAL_INT = 100;
    public static final String SCENE_MODE_DUAL_STRING = "100";
    public static final int SCENE_MODE_FLOWER_INT = 7;
    public static final int SCENE_MODE_HDR_INT = 18;
    public static final int SCENE_MODE_LANDSCAPE_INT = 4;
    public static final int SCENE_MODE_NIGHT_INT = 5;
    public static final int SCENE_MODE_OPTIZOOM_INT = 101;
    public static final int SCENE_MODE_PANORAMA_INT = 104;
    public static final int SCENE_MODE_PORTRAIT_INT = 3;
    public static final int SCENE_MODE_PROMODE_INT = 109;
    public static final int SCENE_MODE_SHARPSHOOTER_INT = 107;
    public static final int SCENE_MODE_SNOW_INT = 9;
    public static final int SCENE_MODE_SPORTS_INT = 13;
    public static final int SCENE_MODE_SUNSET_INT = 10;
    public static final int SCENE_MODE_TRACKINGFOCUS_INT = 108;
    public static final int SCENE_MODE_UBIFOCUS_INT = 102;
    private static final String TAG = "SnapCam_SettingsManager";
    private static SettingsManager sInstance;
    private int mCameraId;
    private ArrayList<CameraCharacteristics> mCharacteristics = new ArrayList<>();
    private Context mContext;
    private JSONObject mDependency;
    private Map<String, Set<String>> mDependendsOnMap;
    private int[] mExtendedHFRSize;
    private Set<String> mFilteredKeys;
    private boolean mIsBokehMode = false;
    private boolean mIsFrontCameraPresent = false;
    private boolean mIsMonoCameraPresent = false;
    private ArrayList<Listener> mListeners = new ArrayList<>();
    private PreferenceGroup mPreferenceGroup;
    private ComboPreferences mPreferences;
    private Map<String, Values> mValuesMap;

    public interface Listener {
        void onSettingsChanged(List<SettingState> list);
    }

    static class SettingState {
        String key;
        Values values;

        SettingState(String str, Values values2) {
            this.key = str;
            this.values = values2;
        }
    }

    static class Values {
        String overriddenValue;
        String value;

        Values(String str, String str2) {
            this.value = str;
            this.overriddenValue = str2;
        }
    }

    static {
        KEY_ISO_INDEX.put("auto", Integer.valueOf(0));
        KEY_ISO_INDEX.put("deblur", Integer.valueOf(1));
        KEY_ISO_INDEX.put(SCENE_MODE_DUAL_STRING, Integer.valueOf(2));
        KEY_ISO_INDEX.put("200", Integer.valueOf(3));
        KEY_ISO_INDEX.put("400", Integer.valueOf(4));
        KEY_ISO_INDEX.put("800", Integer.valueOf(5));
        KEY_ISO_INDEX.put("1600", Integer.valueOf(6));
        KEY_ISO_INDEX.put("3200", Integer.valueOf(7));
        KEY_ISO_INDEX.put(MAUNAL_ABSOLUTE_ISO_VALUE, Integer.valueOf(8));
    }

    public Map<String, Values> getValuesMap() {
        return this.mValuesMap;
    }

    public Set<String> getFilteredKeys() {
        return this.mFilteredKeys;
    }

    private SettingsManager(Context context) {
        byte b;
        this.mContext = context;
        this.mPreferences = ComboPreferences.get(this.mContext);
        if (this.mPreferences == null) {
            this.mPreferences = new ComboPreferences(this.mContext);
        }
        upgradeGlobalPreferences(this.mPreferences.getGlobal(), this.mContext);
        CameraManager cameraManager = (CameraManager) this.mContext.getSystemService("camera");
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            boolean z = true;
            for (int i = 0; i < cameraIdList.length; i++) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraIdList[i]);
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("cameraIdList size =");
                sb.append(cameraIdList.length);
                Log.d(str, sb.toString());
                try {
                    b = ((Byte) cameraCharacteristics.get(CaptureModule.MetaDataMonoOnlyKey)).byteValue();
                } catch (Exception unused) {
                    b = 0;
                }
                if (b == 1) {
                    CaptureModule.MONO_ID = i;
                    this.mIsMonoCameraPresent = true;
                }
                int intValue = ((Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)).intValue();
                if (intValue == 0) {
                    CaptureModule.FRONT_ID = i;
                    this.mIsFrontCameraPresent = true;
                } else if (intValue == 1 && z) {
                    upgradeCameraId(this.mPreferences.getGlobal(), i);
                    z = false;
                }
                this.mCharacteristics.add(i, cameraCharacteristics);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        this.mDependency = parseJson("dependency.json");
    }

    public static SettingsManager createInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SettingsManager(context.getApplicationContext());
        }
        return sInstance;
    }

    public static SettingsManager getInstance() {
        return sInstance;
    }

    public void destroyInstance() {
        if (sInstance != null) {
            sInstance = null;
        }
    }

    private void upgradeGlobalPreferences(SharedPreferences sharedPreferences, Context context) {
        CameraSettings.upgradeOldVersion(sharedPreferences, context);
    }

    private void upgradeCameraId(SharedPreferences sharedPreferences, int i) {
        CameraSettings.writePreferredCameraId(sharedPreferences, i);
    }

    public List<String> getDisabledList() {
        ArrayList arrayList = new ArrayList();
        for (String str : this.mValuesMap.keySet()) {
            if (((Values) this.mValuesMap.get(str)).overriddenValue != null) {
                arrayList.add(str);
            }
        }
        return arrayList;
    }

    public void onSettingChanged(ListPreference listPreference) {
        List checkDependencyAndUpdate = checkDependencyAndUpdate(listPreference.getKey());
        if (checkDependencyAndUpdate != null) {
            runTimeUpdateDependencyOptions(listPreference);
            notifyListeners(checkDependencyAndUpdate);
        }
    }

    public void updatePictureAndVideoSize() {
        ListPreference findPreference = this.mPreferenceGroup.findPreference("pref_camera_picturesize_key");
        ListPreference findPreference2 = this.mPreferenceGroup.findPreference("pref_video_quality_key");
        if (findPreference != null) {
            findPreference.setEntries(this.mContext.getResources().getStringArray(C0905R.array.pref_camera2_picturesize_entries));
            findPreference.setEntryValues(this.mContext.getResources().getStringArray(C0905R.array.pref_camera2_picturesize_entryvalues));
            filterUnsupportedOptions(findPreference, getSupportedPictureSize(getCurrentCameraId()));
        }
        if (findPreference2 != null) {
            findPreference2.setEntries(this.mContext.getResources().getStringArray(C0905R.array.pref_camera2_video_quality_entries));
            findPreference2.setEntryValues(this.mContext.getResources().getStringArray(C0905R.array.pref_camera2_video_quality_entryvalues));
            filterUnsupportedOptions(findPreference2, getSupportedVideoSize(getCurrentCameraId()));
        }
    }

    public void init() {
        Log.d(TAG, "SettingsManager init");
        setLocalIdAndInitialize(getInitialCameraId(this.mPreferences));
    }

    public void reinit(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("SettingsManager reinit ");
        sb.append(i);
        Log.d(TAG, sb.toString());
        setLocalIdAndInitialize(i);
    }

    private void setLocalIdAndInitialize(int i) {
        this.mPreferences.setLocalId(this.mContext, i);
        this.mCameraId = i;
        CameraSettings.upgradeLocalPreferences(this.mPreferences.getLocal());
        this.mPreferenceGroup = (PreferenceGroup) new PreferenceInflater(this.mContext).inflate((int) C0905R.xml.capture_preferences);
        this.mValuesMap = new HashMap();
        this.mDependendsOnMap = new HashMap();
        this.mFilteredKeys = new HashSet();
        try {
            if (this.mCharacteristics.size() > 0) {
                this.mExtendedHFRSize = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CaptureModule.hfrSizeList);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        filterPreferences(i);
        initDependencyTable();
        initializeValueMap();
        filterChromaflashPictureSizeOptions();
        filterHeifSizeOptions();
    }

    private Size parseSize(String str) {
        int indexOf = str.indexOf(120);
        return new Size(Integer.parseInt(str.substring(0, indexOf)), Integer.parseInt(str.substring(indexOf + 1)));
    }

    private void initDependencyTable() {
        for (int i = 0; i < this.mPreferenceGroup.size(); i++) {
            ListPreference listPreference = (ListPreference) this.mPreferenceGroup.get(i);
            String key = listPreference.getKey();
            JSONObject dependencyList = getDependencyList(key, listPreference.getValue());
            if (dependencyList != null) {
                Iterator keys = dependencyList.keys();
                while (keys.hasNext()) {
                    String str = (String) keys.next();
                    if (this.mPreferenceGroup.findPreference(str) != null) {
                        Set set = (Set) this.mDependendsOnMap.get(str);
                        if (set == null) {
                            set = new HashSet();
                        }
                        set.add(key);
                        this.mDependendsOnMap.put(str, set);
                    }
                }
            }
        }
    }

    private void initializeValueMap() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < this.mPreferenceGroup.size(); i++) {
            ListPreference listPreference = (ListPreference) this.mPreferenceGroup.get(i);
            String key = listPreference.getKey();
            Set set = (Set) this.mDependendsOnMap.get(key);
            if (!(set == null || set.size() == 0)) {
                arrayList.add(key);
            }
            this.mValuesMap.put(listPreference.getKey(), new Values(listPreference.getValue(), null));
        }
        for (String str : arrayList) {
            String str2 = (String) ((Set) this.mDependendsOnMap.get(str)).iterator().next();
            try {
                this.mValuesMap.put(str, new Values(getValue(str), getDependencyList(str2, getValue(str2)).getString(str)));
            } catch (JSONException unused) {
                StringBuilder sb = new StringBuilder();
                sb.append("initializeValueMap JSONException No value for:");
                sb.append(str);
                Log.w(TAG, sb.toString());
            }
        }
    }

    private List<SettingState> checkDependencyAndUpdate(String str) {
        ListPreference findPreference = this.mPreferenceGroup.findPreference(str);
        if (findPreference == null) {
            return null;
        }
        String value = findPreference.getValue();
        String value2 = getValue(str);
        if (value.equals(value2)) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        Values values = new Values(value, null);
        this.mValuesMap.put(str, values);
        arrayList.add(new SettingState(str, values));
        JSONObject dependencyMapForKey = getDependencyMapForKey(str);
        if (dependencyMapForKey != null && !getDependencyKey(dependencyMapForKey, value).equals(getDependencyKey(dependencyMapForKey, value2))) {
            HashSet<String> hashSet = new HashSet<>();
            HashSet<String> hashSet2 = new HashSet<>();
            JSONObject dependencyList = getDependencyList(str, value);
            JSONObject dependencyList2 = getDependencyList(str, value2);
            Iterator keys = dependencyList2.keys();
            while (keys.hasNext()) {
                hashSet.add((String) keys.next());
            }
            Iterator keys2 = dependencyList.keys();
            while (keys2.hasNext()) {
                hashSet2.add((String) keys2.next());
            }
            Iterator keys3 = dependencyList2.keys();
            while (keys3.hasNext()) {
                hashSet2.remove(keys3.next());
            }
            Iterator keys4 = dependencyList.keys();
            while (keys4.hasNext()) {
                hashSet.remove(keys4.next());
            }
            for (String str2 : hashSet) {
                Set set = (Set) this.mDependendsOnMap.get(str2);
                if (!(set == null || set.size() == 0)) {
                    Values values2 = (Values) this.mValuesMap.get(str2);
                    if (values2 != null) {
                        values2.overriddenValue = null;
                        this.mValuesMap.put(str2, values2);
                        arrayList.add(new SettingState(str2, values2));
                    }
                }
            }
            for (String str3 : hashSet2) {
                ListPreference findPreference2 = this.mPreferenceGroup.findPreference(str3);
                if (findPreference2 != null) {
                    Values values3 = (Values) this.mValuesMap.get(str3);
                    if (values3 != null && (values3 == null || values3.overriddenValue == null)) {
                        try {
                            String string = dependencyList.getString(str3);
                            if (string != null) {
                                Values values4 = new Values(findPreference2.getValue(), string);
                                this.mValuesMap.put(str3, values4);
                                arrayList.add(new SettingState(str3, values4));
                            }
                        } catch (JSONException unused) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("checkDependencyAndUpdate JSONException No value for:");
                            sb.append(str3);
                            Log.w(TAG, sb.toString());
                        }
                    }
                }
            }
            updateBackDependency(str, hashSet, hashSet2);
        }
        return arrayList;
    }

    private void updateBackDependency(String str, Set<String> set, Set<String> set2) {
        for (CharSequence charSequence : set) {
            Set set3 = (Set) this.mDependendsOnMap.get(charSequence.toString());
            if (set3 != null) {
                set3.remove(str);
            }
        }
        for (CharSequence charSequence2 : set2) {
            String charSequence3 = charSequence2.toString();
            Set set4 = (Set) this.mDependendsOnMap.get(charSequence3);
            if (set4 == null) {
                set4 = new HashSet();
                this.mDependendsOnMap.put(charSequence3, set4);
            }
            set4.add(str);
        }
    }

    public void registerListener(Listener listener) {
        this.mListeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        this.mListeners.remove(listener);
    }

    private void notifyListeners(List<SettingState> list) {
        Iterator it = this.mListeners.iterator();
        while (it.hasNext()) {
            ((Listener) it.next()).onSettingsChanged(list);
        }
    }

    public int getCurrentCameraId() {
        return this.mCameraId;
    }

    public String getValue(String str) {
        Values values = (Values) this.mValuesMap.get(str);
        if (values == null) {
            return null;
        }
        String str2 = values.overriddenValue;
        return str2 == null ? values.value : str2;
    }

    public int getValueIndex(String str) {
        ListPreference findPreference = this.mPreferenceGroup.findPreference(str);
        String value = getValue(str);
        if (value == null || findPreference == null) {
            return -1;
        }
        return findPreference.findIndexOfValue(value);
    }

    private boolean setFocusValue(String str, float f) {
        SharedPreferences sharedPreferences = this.mContext.getSharedPreferences(ComboPreferences.getLocalSharedPreferencesName(this.mContext, this.mCameraId), 0);
        if (sharedPreferences.getFloat(str, 0.5f) == f) {
            return false;
        }
        Editor edit = sharedPreferences.edit();
        edit.putFloat(str, f);
        edit.apply();
        return true;
    }

    public float getFocusValue(String str) {
        return this.mContext.getSharedPreferences(ComboPreferences.getLocalSharedPreferencesName(this.mContext, this.mCameraId), 0).getFloat(str, 0.5f);
    }

    public boolean isOverriden(String str) {
        return ((Values) this.mValuesMap.get(str)).overriddenValue != null;
    }

    public boolean setValue(String str, String str2) {
        ListPreference findPreference = this.mPreferenceGroup.findPreference(str);
        if (findPreference == null) {
            return false;
        }
        if (str.equals(KEY_MAKEUP) || str.equals(KEY_BOKEH_BLUR_DEGREE) || str.equals(KEY_MAKEUP_CLEAN_DEGREE) || str.equals(KEY_MAKEUP_WHITEN_DEGREE)) {
            findPreference.setMakeupSeekBarValue(str2);
            updateMapAndNotify(findPreference);
            return true;
        } else if (findPreference.findIndexOfValue(str2) < 0) {
            return false;
        } else {
            findPreference.setValue(str2);
            updateMapAndNotify(findPreference);
            return true;
        }
    }

    public void setValueIndex(String str, int i) {
        ListPreference findPreference = this.mPreferenceGroup.findPreference(str);
        if (findPreference != null) {
            findPreference.setValueIndex(i);
            updateMapAndNotify(findPreference);
        }
    }

    public void setFocusDistance(String str, boolean z, float f, float f2) {
        if ((f >= 0.0f ? setFocusValue(str, f) : false) || z) {
            ArrayList arrayList = new ArrayList();
            StringBuilder sb = new StringBuilder();
            sb.append(BuildConfig.FLAVOR);
            sb.append(f);
            arrayList.add(new SettingState(KEY_FOCUS_DISTANCE, new Values(sb.toString(), null)));
            notifyListeners(arrayList);
        }
    }

    private void updateMapAndNotify(ListPreference listPreference) {
        List checkDependencyAndUpdate = checkDependencyAndUpdate(listPreference.getKey());
        if (checkDependencyAndUpdate != null) {
            runTimeUpdateDependencyOptions(listPreference);
            notifyListeners(checkDependencyAndUpdate);
        }
    }

    public PreferenceGroup getPreferenceGroup() {
        return this.mPreferenceGroup;
    }

    public CharSequence[] getEntries(String str) {
        ListPreference findPreference = this.mPreferenceGroup.findPreference(str);
        if (findPreference != null) {
            return findPreference.getEntries();
        }
        return null;
    }

    public CharSequence[] getEntryValues(String str) {
        ListPreference findPreference = this.mPreferenceGroup.findPreference(str);
        if (findPreference != null) {
            return findPreference.getEntryValues();
        }
        return null;
    }

    public int[] getResource(String str, int i) {
        IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(str);
        if (i == 0) {
            return iconListPreference.getThumbnailIds();
        }
        if (i != 1) {
            return null;
        }
        return iconListPreference.getLargeIconIds();
    }

    public int getInitialCameraId(SharedPreferences sharedPreferences) {
        int parseInt = Integer.parseInt(sharedPreferences.getString(KEY_SWITCH_CAMERA, "-1"));
        CaptureModule.SWITCH_ID = parseInt;
        StringBuilder sb = new StringBuilder();
        sb.append("SWITCH_ID = ");
        sb.append(parseInt);
        Log.d(TAG, sb.toString());
        if (this.mIsBokehMode) {
            return CaptureModule.BOKEH_ID;
        }
        if (parseInt != -1) {
            return parseInt;
        }
        int parseInt2 = Integer.parseInt(sharedPreferences.getString(KEY_CAMERA_ID, "0"));
        if (parseInt2 == CaptureModule.FRONT_ID) {
            return parseInt2;
        }
        String str = "off";
        if (sharedPreferences.getString(KEY_MONO_ONLY, str).equals(str)) {
            return parseInt2;
        }
        return CaptureModule.MONO_ID;
    }

    private void filterPreferences(int i) {
        ListPreference findPreference = this.mPreferenceGroup.findPreference("pref_camera_whitebalance_key");
        PreferenceGroup preferenceGroup = this.mPreferenceGroup;
        String str = KEY_FLASH_MODE;
        ListPreference findPreference2 = preferenceGroup.findPreference(str);
        ListPreference findPreference3 = this.mPreferenceGroup.findPreference(KEY_COLOR_EFFECT);
        ListPreference findPreference4 = this.mPreferenceGroup.findPreference(KEY_SCENE_MODE);
        ListPreference findPreference5 = this.mPreferenceGroup.findPreference(KEY_SCEND_MODE_INSTRUCTIONAL);
        ListPreference findPreference6 = this.mPreferenceGroup.findPreference(KEY_CAMERA_ID);
        ListPreference findPreference7 = this.mPreferenceGroup.findPreference("pref_camera_picturesize_key");
        ListPreference findPreference8 = this.mPreferenceGroup.findPreference("pref_camera_exposure_key");
        ListPreference findPreference9 = this.mPreferenceGroup.findPreference(KEY_ISO);
        PreferenceGroup preferenceGroup2 = this.mPreferenceGroup;
        String str2 = KEY_CLEARSIGHT;
        ListPreference findPreference10 = preferenceGroup2.findPreference(str2);
        PreferenceGroup preferenceGroup3 = this.mPreferenceGroup;
        String str3 = KEY_MONO_PREVIEW;
        ListPreference findPreference11 = preferenceGroup3.findPreference(str3);
        PreferenceGroup preferenceGroup4 = this.mPreferenceGroup;
        String str4 = str3;
        String str5 = KEY_MONO_ONLY;
        ListPreference findPreference12 = preferenceGroup4.findPreference(str5);
        String str6 = str5;
        PreferenceGroup preferenceGroup5 = this.mPreferenceGroup;
        ListPreference listPreference = findPreference12;
        String str7 = KEY_MPO;
        String str8 = str7;
        ListPreference findPreference13 = preferenceGroup5.findPreference(str7);
        ListPreference findPreference14 = this.mPreferenceGroup.findPreference("pref_camera_redeyereduction_key");
        ListPreference findPreference15 = this.mPreferenceGroup.findPreference("pref_video_quality_key");
        ListPreference listPreference2 = findPreference11;
        ListPreference findPreference16 = this.mPreferenceGroup.findPreference("pref_camera_audioencoder_key");
        ListPreference findPreference17 = this.mPreferenceGroup.findPreference("pref_camera_noise_reduction_key");
        String str9 = "pref_camera_facedetection_key";
        String str10 = str9;
        ListPreference findPreference18 = this.mPreferenceGroup.findPreference(str9);
        ListPreference findPreference19 = this.mPreferenceGroup.findPreference(KEY_INSTANT_AEC);
        String str11 = str2;
        ListPreference findPreference20 = this.mPreferenceGroup.findPreference(KEY_SATURATION_LEVEL);
        ListPreference listPreference3 = findPreference10;
        ListPreference findPreference21 = this.mPreferenceGroup.findPreference(KEY_ANTI_BANDING_LEVEL);
        ListPreference listPreference4 = findPreference15;
        ListPreference findPreference22 = this.mPreferenceGroup.findPreference(KEY_HISTOGRAM);
        ListPreference listPreference5 = findPreference9;
        ListPreference findPreference23 = this.mPreferenceGroup.findPreference(KEY_HDR);
        ListPreference listPreference6 = findPreference8;
        ListPreference findPreference24 = this.mPreferenceGroup.findPreference(KEY_ZOOM);
        this.mPreferenceGroup.findPreference(KEY_QCFA);
        PreferenceGroup preferenceGroup6 = this.mPreferenceGroup;
        String str12 = KEY_BSGC_DETECTION;
        ListPreference findPreference25 = preferenceGroup6.findPreference(str12);
        ListPreference listPreference7 = findPreference6;
        PreferenceGroup preferenceGroup7 = this.mPreferenceGroup;
        ListPreference listPreference8 = findPreference5;
        String str13 = KEY_HDR_MODE;
        ListPreference findPreference26 = preferenceGroup7.findPreference(str13);
        String str14 = str13;
        if (findPreference != null && filterUnsupportedOptions(findPreference, getSupportedWhiteBalanceModes(i))) {
            this.mFilteredKeys.add(findPreference.getKey());
        }
        if (findPreference2 != null) {
            if (!isFlashAvailable(this.mCameraId)) {
                removePreference(this.mPreferenceGroup, str);
                this.mFilteredKeys.add(findPreference2.getKey());
            }
            int i2 = CaptureModule.FRONT_ID;
        }
        if (findPreference25 != null && !isBsgcAvailable(this.mCameraId)) {
            removePreference(this.mPreferenceGroup, str12);
            this.mFilteredKeys.add(findPreference25.getKey());
        }
        if (findPreference3 != null && filterUnsupportedOptions(findPreference3, getSupportedColorEffects(i))) {
            this.mFilteredKeys.add(findPreference3.getKey());
        }
        if (findPreference19 != null && filterUnsupportedOptions(findPreference19, getSupportedInstantAecAvailableModes(i))) {
            this.mFilteredKeys.add(findPreference19.getKey());
        }
        if (findPreference20 != null && filterUnsupportedOptions(findPreference20, getSupportedSaturationLevelAvailableModes(i))) {
            this.mFilteredKeys.add(findPreference20.getKey());
        }
        if (findPreference21 != null && filterUnsupportedOptions(findPreference21, getSupportedAntiBandingLevelAvailableModes(i))) {
            this.mFilteredKeys.add(findPreference21.getKey());
        }
        if (findPreference22 != null && filterUnsupportedOptions(findPreference22, getSupportedHistogramAvailableModes(i))) {
            this.mFilteredKeys.add(findPreference22.getKey());
        }
        if (findPreference23 != null && filterUnsupportedOptions(findPreference23, getSupportedHdrAvailableModes(i))) {
            this.mFilteredKeys.add(findPreference23.getKey());
        }
        if (findPreference7 != null) {
            if (filterUnsupportedOptions(findPreference7, getSupportedPictureSize(i))) {
                this.mFilteredKeys.add(findPreference7.getKey());
            } else if (filterSimilarPictureSize(this.mPreferenceGroup, findPreference7)) {
                this.mFilteredKeys.add(findPreference7.getKey());
            }
        }
        if (findPreference4 != null && filterUnsupportedOptions(findPreference4, getSupportedSceneModes(i))) {
            this.mFilteredKeys.add(findPreference4.getKey());
        }
        if (listPreference8 != null) {
            ListPreference listPreference9 = listPreference8;
            if (filterUnsupportedOptions(listPreference9, getSupportedSceneModes(i))) {
                this.mFilteredKeys.add(listPreference9.getKey());
            }
        }
        if (listPreference7 != null) {
            buildCameraId();
        }
        if (listPreference6 != null) {
            buildExposureCompensation(i);
        }
        if (listPreference5 != null) {
            ListPreference listPreference10 = listPreference5;
            if (filterUnsupportedOptions(listPreference10, getSupportedIso(i))) {
                this.mFilteredKeys.add(listPreference10.getKey());
            }
        }
        if (listPreference4 != null) {
            ListPreference listPreference11 = listPreference4;
            if (filterUnsupportedOptions(listPreference11, getSupportedVideoSize(i))) {
                this.mFilteredKeys.add(listPreference11.getKey());
            }
        }
        if (!this.mIsMonoCameraPresent) {
            if (listPreference3 != null) {
                removePreference(this.mPreferenceGroup, str11);
            }
            if (listPreference2 != null) {
                removePreference(this.mPreferenceGroup, str4);
            }
            if (listPreference != null) {
                removePreference(this.mPreferenceGroup, str6);
            }
            if (findPreference13 != null) {
                removePreference(this.mPreferenceGroup, str8);
            }
        }
        if (findPreference14 != null) {
            ListPreference listPreference12 = findPreference14;
            if (filterUnsupportedOptions(listPreference12, getSupportedRedeyeReduction(i))) {
                this.mFilteredKeys.add(listPreference12.getKey());
            }
        }
        if (findPreference16 != null) {
            ListPreference listPreference13 = findPreference16;
            if (filterUnsupportedOptions(listPreference13, getSupportedAudioEncoders(findPreference16.getEntryValues()))) {
                this.mFilteredKeys.add(listPreference13.getKey());
            }
        }
        if (findPreference17 != null) {
            ListPreference listPreference14 = findPreference17;
            if (filterUnsupportedOptions(listPreference14, getSupportedNoiseReductionModes(i))) {
                this.mFilteredKeys.add(listPreference14.getKey());
            }
        }
        if (findPreference18 != null && !isFaceDetectionSupported(i)) {
            removePreference(this.mPreferenceGroup, str10);
        }
        filterHFROptions();
        filterVideoEncoderOptions();
        filterTimeLapseOptions();
        if (!this.mIsFrontCameraPresent || !isFacingFront(this.mCameraId)) {
            removePreference(this.mPreferenceGroup, KEY_SELFIEMIRROR);
        }
        removePreference(this.mPreferenceGroup, "pref_selfie_flash_key");
        if (findPreference24 != null) {
            ListPreference listPreference15 = findPreference24;
            if (filterUnsupportedOptions(listPreference15, getSupportedZoomLevel(i))) {
                this.mFilteredKeys.add(listPreference15.getKey());
            }
        }
        if (findPreference26 != null && !isZZHDRSupported()) {
            removePreference(this.mPreferenceGroup, str14);
        }
        if (!CameraSettings.isSupportPictureFormatChange()) {
            removePreference(this.mPreferenceGroup, KEY_PICTURE_FORMAT);
        }
    }

    private void runTimeUpdateDependencyOptions(ListPreference listPreference) {
        if (listPreference.getKey().equals("pref_video_quality_key")) {
            filterHFROptions();
            filterVideoEncoderOptions();
        } else if (listPreference.getKey().equals(KEY_SCENE_MODE)) {
            filterChromaflashPictureSizeOptions();
        } else if (listPreference.getKey().equals(KEY_PICTURE_FORMAT)) {
            filterHeifSizeOptions();
        }
    }

    private void buildExposureCompensation(int i) {
        Range range = (Range) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
        int intValue = ((Integer) range.getUpper()).intValue();
        int intValue2 = ((Integer) range.getLower()).intValue();
        String str = "pref_camera_exposure_key";
        if (intValue2 == 0 && intValue == 0) {
            removePreference(this.mPreferenceGroup, str);
            return;
        }
        ListPreference findPreference = this.mPreferenceGroup.findPreference(str);
        double doubleValue = ((Rational) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)).doubleValue();
        int i2 = 1;
        while ((intValue - intValue2) / i2 > 10) {
            i2++;
        }
        if (intValue2 < 0) {
            while (Math.abs(intValue2) % i2 != 0) {
                intValue2++;
            }
        }
        int i3 = 0;
        int i4 = 0;
        for (int i5 = intValue2; i5 <= intValue; i5 += i2) {
            i4++;
        }
        CharSequence[] charSequenceArr = new CharSequence[i4];
        CharSequence[] charSequenceArr2 = new CharSequence[i4];
        while (intValue2 <= intValue) {
            charSequenceArr2[i3] = Integer.toString(intValue2);
            StringBuilder sb = new StringBuilder();
            if (intValue2 > 0) {
                sb.append('+');
            }
            sb.append(new DecimalFormat("#.##").format(((double) intValue2) * doubleValue));
            charSequenceArr[i3] = sb.toString();
            intValue2 += i2;
            i3++;
        }
        findPreference.setEntries(charSequenceArr);
        findPreference.setEntryValues(charSequenceArr2);
    }

    public CharSequence[] getExposureCompensationEntries() {
        ListPreference findPreference = this.mPreferenceGroup.findPreference("pref_camera_exposure_key");
        if (findPreference == null) {
            return null;
        }
        return findPreference.getEntries();
    }

    public CharSequence[] getExposureCompensationEntryValues() {
        ListPreference findPreference = this.mPreferenceGroup.findPreference("pref_camera_exposure_key");
        if (findPreference == null) {
            return null;
        }
        return findPreference.getEntryValues();
    }

    public long[] getExposureRangeValues(int i) {
        long[] jArr;
        String str = TAG;
        try {
            jArr = (long[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CaptureModule.EXPOSURE_RANGE);
            if (jArr == null) {
                try {
                    Log.w(str, "get exposure range modes is null.");
                    return null;
                } catch (NullPointerException unused) {
                    Log.w(str, "Supported exposure range modes is null.");
                    return jArr;
                } catch (IllegalArgumentException unused2) {
                    Log.w(str, "IllegalArgumentException Supported exposure range modes is null.");
                    return jArr;
                }
            }
        } catch (NullPointerException unused3) {
            jArr = null;
            Log.w(str, "Supported exposure range modes is null.");
            return jArr;
        } catch (IllegalArgumentException unused4) {
            jArr = null;
            Log.w(str, "IllegalArgumentException Supported exposure range modes is null.");
            return jArr;
        }
        return jArr;
    }

    public int[] getIsoRangeValues(int i) {
        String str = TAG;
        int[] iArr = new int[2];
        try {
            Range range = (Range) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
            if (range == null) {
                return null;
            }
            iArr[0] = ((Integer) range.getLower()).intValue();
            iArr[1] = ((Integer) range.getUpper()).intValue();
            return iArr;
        } catch (NullPointerException unused) {
            Log.w(str, "Supported iso range is null.");
        } catch (IllegalArgumentException unused2) {
            Log.w(str, "IllegalArgumentException Supported iso range is null.");
        }
    }

    private void buildCameraId() {
        String str;
        String str2;
        int size = this.mCharacteristics.size();
        int i = size + 1;
        CharSequence[] charSequenceArr = new CharSequence[i];
        CharSequence[] charSequenceArr2 = new CharSequence[i];
        int i2 = 0;
        while (true) {
            str = BuildConfig.FLAVOR;
            str2 = TAG;
            if (i2 >= size) {
                break;
            }
            String str3 = ((Integer) ((CameraCharacteristics) this.mCharacteristics.get(i2)).get(CameraCharacteristics.LENS_FACING)).intValue() == 0 ? "front" : "back";
            StringBuilder sb = new StringBuilder();
            sb.append("camera ");
            sb.append(i2);
            sb.append(" facing:");
            sb.append(str3);
            charSequenceArr2[i2] = sb.toString();
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str);
            sb2.append(i2);
            charSequenceArr[i2] = sb2.toString();
            StringBuilder sb3 = new StringBuilder();
            sb3.append("add ");
            sb3.append(charSequenceArr2[i2]);
            sb3.append("=");
            sb3.append(charSequenceArr[i2]);
            Log.d(str2, sb3.toString());
            i2++;
        }
        charSequenceArr2[size] = "disable";
        charSequenceArr[size] = "-1";
        ListPreference findPreference = this.mPreferenceGroup.findPreference(KEY_SWITCH_CAMERA);
        findPreference.setEntries(charSequenceArr2);
        findPreference.setEntryValues(charSequenceArr);
        boolean z = this.mIsFrontCameraPresent;
        String str4 = KEY_CAMERA_ID;
        if (!z) {
            Log.d(str2, "no front camera,remove camera id pref");
            removePreference(this.mPreferenceGroup, str4);
            return;
        }
        CharSequence[] charSequenceArr3 = new CharSequence[size];
        CharSequence[] charSequenceArr4 = new CharSequence[size];
        charSequenceArr3[0] = "0";
        charSequenceArr4[0] = "BACK";
        if (z && size > 1) {
            StringBuilder sb4 = new StringBuilder();
            sb4.append(str);
            sb4.append(CaptureModule.FRONT_ID);
            charSequenceArr3[1] = sb4.toString();
            charSequenceArr4[1] = "FRONT";
        }
        ListPreference findPreference2 = this.mPreferenceGroup.findPreference(str4);
        findPreference2.setEntryValues(charSequenceArr3);
        findPreference2.setEntries(charSequenceArr4);
    }

    private void filterVideoEncoderOptions() {
        ListPreference findPreference = this.mPreferenceGroup.findPreference("pref_camera_videoencoder_key");
        if (findPreference != null) {
            findPreference.reloadInitialEntriesAndEntryValues();
            if (filterUnsupportedOptions(findPreference, getSupportedVideoEncoders())) {
                this.mFilteredKeys.add(findPreference.getKey());
            }
        }
    }

    private void filterChromaflashPictureSizeOptions() {
        String value = getValue(KEY_SCENE_MODE);
        String str = "pref_camera_picturesize_key";
        ListPreference findPreference = this.mPreferenceGroup.findPreference(str);
        if (findPreference != null) {
            findPreference.reloadInitialEntriesAndEntryValues();
            if (Integer.parseInt(value) == 105) {
                if (filterUnsupportedOptions(findPreference, getSupportedChromaFlashPictureSize())) {
                    this.mFilteredKeys.add(findPreference.getKey());
                }
                Size parseSize = parseSize(getValue(str));
                if (parseSize.getWidth() <= 352 && parseSize.getHeight() <= 288) {
                    CharSequence[] entryValues = findPreference.getEntryValues();
                    setValue(str, entryValues[entryValues.length - 1].toString());
                }
            } else if (filterUnsupportedOptions(findPreference, getSupportedPictureSize(getCurrentCameraId()))) {
                this.mFilteredKeys.add(findPreference.getKey());
            }
        }
    }

    private void filterHeifSizeOptions() {
        ListPreference findPreference = this.mPreferenceGroup.findPreference("pref_camera_picturesize_key");
        ListPreference findPreference2 = this.mPreferenceGroup.findPreference("pref_video_quality_key");
        if (filterUnsupportedOptions(findPreference, getSupportedPictureSize(getCurrentCameraId()))) {
            this.mFilteredKeys.add(findPreference.getKey());
        }
        if (filterUnsupportedOptions(findPreference2, getSupportedVideoSize(getCurrentCameraId()))) {
            this.mFilteredKeys.add(findPreference2.getKey());
        }
    }

    private void filterHFROptions() {
        ListPreference findPreference = this.mPreferenceGroup.findPreference(KEY_VIDEO_HIGH_FRAME_RATE);
        if (findPreference != null) {
            findPreference.reloadInitialEntriesAndEntryValues();
            if (filterUnsupportedOptions(findPreference, null)) {
                this.mFilteredKeys.add(findPreference.getKey());
            }
        }
    }

    private void filterTimeLapseOptions() {
        ListPreference findPreference = this.mPreferenceGroup.findPreference(KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL);
        if (findPreference != null) {
            findPreference.reloadInitialEntriesAndEntryValues();
            if (filterUnsupportedOptions(findPreference, null)) {
                Log.d(TAG, "unsupport KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL");
            }
        }
    }

    public int[] getWBColorTemperatureRangeValues(int i) {
        int[] iArr;
        String str = TAG;
        try {
            iArr = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CaptureModule.WB_COLOR_TEMPERATURE_RANGE);
            if (iArr == null) {
                try {
                    Log.w(str, "Supported exposure range get null.");
                    return null;
                } catch (NullPointerException unused) {
                    Log.w(str, "Supported exposure range modes is null.");
                    return iArr;
                } catch (IllegalArgumentException e) {
                    e = e;
                    Log.w(str, "Supported exposure range modes occur IllegalArgumentException.");
                    e.printStackTrace();
                    return iArr;
                }
            }
        } catch (NullPointerException unused2) {
            iArr = null;
            Log.w(str, "Supported exposure range modes is null.");
            return iArr;
        } catch (IllegalArgumentException e2) {
            e = e2;
            iArr = null;
            Log.w(str, "Supported exposure range modes occur IllegalArgumentException.");
            e.printStackTrace();
            return iArr;
        }
        return iArr;
    }

    public float[] getWBGainsRangeValues(int i) {
        float[] fArr;
        String str = TAG;
        try {
            fArr = (float[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CaptureModule.WB_RGB_GAINS_RANGE);
            if (fArr == null) {
                try {
                    Log.w(str, "Supported gains range get null.");
                    return null;
                } catch (NullPointerException unused) {
                    Log.w(str, "Supported gains range modes is null.");
                    return fArr;
                } catch (IllegalArgumentException e) {
                    e = e;
                    Log.w(str, "Supported gains range modes occur IllegalArgumentException.");
                    e.printStackTrace();
                    return fArr;
                }
            }
        } catch (NullPointerException unused2) {
            fArr = null;
            Log.w(str, "Supported gains range modes is null.");
            return fArr;
        } catch (IllegalArgumentException e2) {
            e = e2;
            fArr = null;
            Log.w(str, "Supported gains range modes occur IllegalArgumentException.");
            e.printStackTrace();
            return fArr;
        }
        return fArr;
    }

    private List<String> getSupportedChromaFlashPictureSize() {
        StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) ((CameraCharacteristics) this.mCharacteristics.get(getCurrentCameraId())).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] outputSizes = streamConfigurationMap.getOutputSizes(256);
        ArrayList arrayList = new ArrayList();
        if (outputSizes != null) {
            for (int i = 0; i < outputSizes.length; i++) {
                if (outputSizes[i].getWidth() > 352 && outputSizes[i].getHeight() > 288) {
                    arrayList.add(outputSizes[i].toString());
                }
            }
        }
        Size[] highResolutionOutputSizes = streamConfigurationMap.getHighResolutionOutputSizes(256);
        if (highResolutionOutputSizes != null) {
            for (int i2 = 0; i2 < highResolutionOutputSizes.length; i2++) {
                if (outputSizes[i2].getWidth() > 352 && outputSizes[i2].getHeight() > 288) {
                    arrayList.add(highResolutionOutputSizes[i2].toString());
                }
            }
        }
        return arrayList;
    }

    private List<String> getSupportedHighFrameRate() {
        Range[] supportedHighSpeedVideoFPSRange;
        String str = "hsr";
        String str2 = "hfr";
        ArrayList arrayList = new ArrayList();
        arrayList.add("off");
        ListPreference findPreference = this.mPreferenceGroup.findPreference("pref_video_quality_key");
        if (findPreference == null) {
            return arrayList;
        }
        String value = findPreference.getValue();
        if (value != null) {
            Size parseSize = parseSize(value);
            try {
                for (Range range : getSupportedHighSpeedVideoFPSRange(this.mCameraId, parseSize)) {
                    if (((Integer) range.getUpper()).intValue() == ((Integer) range.getLower()).intValue()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(str2);
                        sb.append(String.valueOf(range.getUpper()));
                        arrayList.add(sb.toString());
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(str);
                        sb2.append(String.valueOf(range.getUpper()));
                        arrayList.add(sb2.toString());
                    }
                }
            } catch (IllegalArgumentException e) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("HFR is not supported for this resolution ");
                sb3.append(e);
                Log.w(TAG, sb3.toString());
            }
            int[] iArr = this.mExtendedHFRSize;
            if (iArr != null && iArr.length >= 3) {
                for (int i = 0; i < this.mExtendedHFRSize.length; i += 3) {
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append(str2);
                    sb4.append(this.mExtendedHFRSize[i]);
                    String sb5 = sb4.toString();
                    if (!arrayList.contains(sb5) && parseSize.getWidth() <= this.mExtendedHFRSize[i + 1] && parseSize.getHeight() <= this.mExtendedHFRSize[i + 2]) {
                        arrayList.add(sb5);
                        StringBuilder sb6 = new StringBuilder();
                        sb6.append(str);
                        sb6.append(this.mExtendedHFRSize[i]);
                        arrayList.add(sb6.toString());
                    }
                }
            }
        }
        return arrayList;
    }

    private boolean removePreference(PreferenceGroup preferenceGroup, String str) {
        this.mFilteredKeys.add(str);
        int size = preferenceGroup.size();
        int i = 0;
        while (i < size) {
            CameraPreference cameraPreference = preferenceGroup.get(i);
            if ((cameraPreference instanceof PreferenceGroup) && removePreference((PreferenceGroup) cameraPreference, str)) {
                return true;
            }
            if (!(cameraPreference instanceof ListPreference) || !((ListPreference) cameraPreference).getKey().equals(str)) {
                i++;
            } else {
                preferenceGroup.removePreference(i);
                return true;
            }
        }
        return false;
    }

    public float getMaxZoom(int i) {
        return ((Float) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)).floatValue();
    }

    public Rect getSensorActiveArraySize(int i) {
        return (Rect) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
    }

    public float getMaxZoom(List<Integer> list) {
        float f = Float.MAX_VALUE;
        for (Integer intValue : list) {
            f = Math.min(getMaxZoom(intValue.intValue()), f);
        }
        return f;
    }

    public boolean isZoomSupported(int i) {
        return ((Float) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)).floatValue() > 1.0f;
    }

    public boolean isAutoFocusRegionSupported(List<Integer> list) {
        for (Integer intValue : list) {
            if (!isAutoFocusRegionSupported(intValue.intValue())) {
                return false;
            }
        }
        return true;
    }

    public boolean isAutoExposureRegionSupported(List<Integer> list) {
        for (Integer intValue : list) {
            if (!isAutoExposureRegionSupported(intValue.intValue())) {
                return false;
            }
        }
        return true;
    }

    public boolean isZoomSupported(List<Integer> list) {
        for (Integer intValue : list) {
            if (!isZoomSupported(intValue.intValue())) {
                return false;
            }
        }
        return true;
    }

    public boolean isZZHDRSupported() {
        int[] iArr;
        try {
            iArr = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(getCurrentCameraId())).get(CaptureModule.support_video_hdr_modes);
        } catch (IllegalArgumentException unused) {
            StringBuilder sb = new StringBuilder();
            sb.append("cannot find vendor tag: ");
            sb.append(CaptureModule.support_video_hdr_modes.toString());
            Log.w(TAG, sb.toString());
            iArr = null;
        }
        if (iArr == null || iArr.length <= 1) {
            return false;
        }
        return true;
    }

    public boolean isAutoExposureRegionSupported(int i) {
        Integer num = (Integer) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE);
        return num != null && num.intValue() > 0;
    }

    public boolean isAutoFocusRegionSupported(int i) {
        Integer num = (Integer) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
        return num != null && num.intValue() > 0;
    }

    public boolean isHdrScene(int i) {
        Integer num = (Integer) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CaptureModule.isHdrScene);
        if (num == null || num.intValue() != 1) {
            return false;
        }
        return true;
    }

    public boolean isFixedFocus(int i) {
        Float f = (Float) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        return f == null || f.floatValue() == 0.0f;
    }

    public boolean isFaceDetectionSupported(int i) {
        int[] iArr = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
        if (iArr != null) {
            for (int i2 : iArr) {
                if (i2 == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isBsgcAvailable(int i) {
        boolean z = false;
        try {
            if (((CameraCharacteristics) this.mCharacteristics.get(i)).get(CaptureModule.bsgcAvailable) == null) {
                return false;
            }
            if (((Byte) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CaptureModule.bsgcAvailable)).byteValue() == 1) {
                z = true;
            }
            return z;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public boolean isLogicalCamera(int i) {
        try {
            Byte b = (Byte) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CaptureModule.logicalMode);
            if (b == null || b.byteValue() != 1) {
                return false;
            }
            return true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isFacingFront(int i) {
        return ((Integer) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.LENS_FACING)).intValue() == 0;
    }

    public boolean isFlashSupported(int i) {
        String value = getValue(KEY_SCENE_MODE);
        if ((value != null && value.equals("18")) || !((Boolean) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)).booleanValue() || this.mValuesMap.get(KEY_FLASH_MODE) == null) {
            return false;
        }
        return true;
    }

    private List<String> getSupportedPictureSize(int i) {
        StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] outputSizes = streamConfigurationMap.getOutputSizes(256);
        ArrayList arrayList = new ArrayList();
        boolean z = true;
        if (getSavePictureFormat() != 1) {
            z = false;
        }
        if (getQcfaPrefEnabled() && getIsSupportedQcfa(i)) {
            arrayList.add(getSupportedQcfaDimension(i));
        }
        if (outputSizes != null) {
            for (int i2 = 0; i2 < outputSizes.length; i2++) {
                if (!z || Math.min(outputSizes[i2].getWidth(), outputSizes[i2].getHeight()) >= 512) {
                    arrayList.add(outputSizes[i2].toString());
                }
            }
        }
        Size[] highResolutionOutputSizes = streamConfigurationMap.getHighResolutionOutputSizes(256);
        if (highResolutionOutputSizes != null) {
            for (Size size : highResolutionOutputSizes) {
                arrayList.add(size.toString());
            }
        }
        return arrayList;
    }

    public Size[] getSupportedThumbnailSizes(int i) {
        return (Size[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES);
    }

    public Size[] getSupportedOutputSize(int i, int i2) {
        return ((StreamConfigurationMap) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(i2);
    }

    public Size[] getSupportedOutputSize(int i, Class cls) {
        return ((StreamConfigurationMap) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(cls);
    }

    private List<String> getSupportedVideoSize(int i) {
        boolean z = true;
        int i2 = (i == 0 || i == 1) ? i : 0;
        Size[] outputSizes = ((StreamConfigurationMap) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(MediaRecorder.class);
        if (getSavePictureFormat() != 1) {
            z = false;
        }
        ArrayList arrayList = new ArrayList();
        for (int i3 = 0; i3 < outputSizes.length; i3++) {
            if ((!z || Math.min(outputSizes[i3].getWidth(), outputSizes[i3].getHeight()) >= 512) && CameraSettings.VIDEO_QUALITY_TABLE.containsKey(outputSizes[i3].toString()) && CamcorderProfile.hasProfile(i2, ((Integer) CameraSettings.VIDEO_QUALITY_TABLE.get(outputSizes[i3].toString())).intValue()) && getSupportedVideoEncoders(outputSizes[i3]).size() > 0) {
                arrayList.add(outputSizes[i3].toString());
            }
        }
        return arrayList;
    }

    public Size[] getSupportedHighSpeedVideoSize(int i) {
        return ((StreamConfigurationMap) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getHighSpeedVideoSizes();
    }

    public Range[] getSupportedHighSpeedVideoFPSRange(int i, Size size) {
        return ((StreamConfigurationMap) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getHighSpeedVideoFpsRangesFor(size);
    }

    public int getHighSpeedVideoEncoderBitRate(CamcorderProfile camcorderProfile, int i, int i2) {
        int intValue;
        StringBuilder sb = new StringBuilder();
        sb.append(camcorderProfile.videoFrameWidth);
        sb.append("x");
        sb.append(camcorderProfile.videoFrameHeight);
        String str = ":";
        sb.append(str);
        sb.append(i2);
        String sb2 = sb.toString();
        StringBuilder sb3 = new StringBuilder();
        sb3.append(sb2);
        sb3.append(str);
        sb3.append(camcorderProfile.videoCodec);
        String sb4 = sb3.toString();
        if (CameraSettings.VIDEO_ENCODER_BITRATE.containsKey(sb4)) {
            intValue = ((Integer) CameraSettings.VIDEO_ENCODER_BITRATE.get(sb4)).intValue();
        } else if (CameraSettings.VIDEO_ENCODER_BITRATE.containsKey(sb2)) {
            intValue = ((Integer) CameraSettings.VIDEO_ENCODER_BITRATE.get(sb2)).intValue();
        } else {
            StringBuilder sb5 = new StringBuilder();
            sb5.append("No pre-defined bitrate for ");
            sb5.append(sb2);
            Log.i(TAG, sb5.toString());
            return (int) ((long) ((camcorderProfile.videoBitRate * i) / camcorderProfile.videoFrameRate));
        }
        long j = (long) intValue;
        if (i != i2) {
            j = (j * ((long) i)) / ((long) i2);
        }
        return (int) j;
    }

    private List<String> getSupportedRedeyeReduction(int i) {
        int[] iArr = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
        ArrayList arrayList = new ArrayList();
        int i2 = 0;
        while (true) {
            if (i2 >= iArr.length) {
                break;
            } else if (iArr[i2] == 4) {
                arrayList.add("off");
                arrayList.add(RecordLocationPreference.VALUE_ON);
                break;
            } else {
                i2++;
            }
        }
        return arrayList;
    }

    public float getMinimumFocusDistance(int i) {
        return ((Float) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)).floatValue();
    }

    private List<String> getSupportedWhiteBalanceModes(int i) {
        int[] iArr = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
        ArrayList arrayList = new ArrayList();
        for (int i2 : iArr) {
            StringBuilder sb = new StringBuilder();
            sb.append(BuildConfig.FLAVOR);
            sb.append(i2);
            arrayList.add(sb.toString());
        }
        return arrayList;
    }

    private List<String> getSupportedSceneModes(int i) {
        int[] iArr = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES);
        ArrayList arrayList = new ArrayList();
        arrayList.add("0");
        if (this.mIsMonoCameraPresent) {
            arrayList.add(SCENE_MODE_DUAL_STRING);
        }
        if (OptizoomFilter.isSupportedStatic()) {
            arrayList.add("101");
        }
        if (UbifocusFilter.isSupportedStatic() && i == 0) {
            arrayList.add("102");
        }
        if (BestpictureFilter.isSupportedStatic() && i == 0) {
            arrayList.add("103");
        }
        if (PanoCaptureProcessView.isSupportedStatic() && i == 0) {
            arrayList.add("104");
        }
        if (ChromaflashFilter.isSupportedStatic() && i == 0) {
            arrayList.add("105");
        }
        if (BlurbusterFilter.isSupportedStatic()) {
            arrayList.add("106");
        }
        if (SharpshooterFilter.isSupportedStatic()) {
            arrayList.add("107");
        }
        if (TrackingFocusFrameListener.isSupportedStatic()) {
            arrayList.add("108");
        }
        if (i == 0) {
            arrayList.add("109");
        }
        arrayList.add("5");
        if (isLogicalCamera(i)) {
            arrayList.add(String.valueOf(SCENE_MODE_BOKEH_INT));
        }
        for (int i2 : iArr) {
            if (i2 == 18) {
                arrayList.add(String.valueOf(i2));
            }
            if (i2 == 13) {
                arrayList.add("13");
            }
            if (i2 == 3) {
                arrayList.add(GpsMeasureMode.MODE_3_DIMENSIONAL);
            }
            if (i2 == 4) {
                arrayList.add("4");
            }
            if (i2 == 15) {
                arrayList.add("15");
            }
            if (i2 == 10) {
                arrayList.add("10");
            }
            if (i2 == 8) {
                arrayList.add("8");
            }
            if (i2 == 9) {
                arrayList.add("9");
            }
            if (i2 == 12) {
                arrayList.add("12");
            }
            if (i2 == 7) {
                arrayList.add("7");
            }
        }
        return arrayList;
    }

    private List<String> getSupportedFlashModes(int i) {
        int[] iArr = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
        ArrayList arrayList = new ArrayList();
        for (int i2 : iArr) {
            StringBuilder sb = new StringBuilder();
            sb.append(BuildConfig.FLAVOR);
            sb.append(i2);
            arrayList.add(sb.toString());
        }
        return arrayList;
    }

    private boolean isFlashAvailable(int i) {
        return ((Boolean) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)).booleanValue();
    }

    public List<String> getSupportedColorEffects(int i) {
        int[] iArr = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
        ArrayList arrayList = new ArrayList();
        for (int i2 : iArr) {
            StringBuilder sb = new StringBuilder();
            sb.append(BuildConfig.FLAVOR);
            sb.append(i2);
            arrayList.add(sb.toString());
        }
        return arrayList;
    }

    private List<String> getSupportedIso(int i) {
        Range range = (Range) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
        int intValue = range != null ? ((Integer) range.getUpper()).intValue() : 0;
        int[] iArr = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CaptureModule.ISO_AVAILABLE_MODES);
        ArrayList arrayList = new ArrayList();
        String str = "auto";
        arrayList.add(str);
        if (iArr != null) {
            for (int i2 : iArr) {
                for (String str2 : KEY_ISO_INDEX.keySet()) {
                    if (((Integer) KEY_ISO_INDEX.get(str2)).equals(Integer.valueOf(i2))) {
                        if (str2.equals(str) || str2.equals("deblur")) {
                            arrayList.add(str2);
                        } else if (Integer.parseInt(str2) <= intValue) {
                            arrayList.add(str2);
                        }
                    }
                }
            }
        } else {
            Log.w(TAG, "Supported ISO range is null.");
        }
        return arrayList;
    }

    private boolean isVideoResolutionSupportedByEncoder(Size size, VideoEncoderCap videoEncoderCap) {
        if (size == null || videoEncoderCap == null) {
            return false;
        }
        if (size.getWidth() <= videoEncoderCap.mMaxFrameWidth && size.getWidth() >= videoEncoderCap.mMinFrameWidth && size.getHeight() <= videoEncoderCap.mMaxFrameHeight && size.getHeight() >= videoEncoderCap.mMinFrameHeight) {
            return true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Codec = ");
        sb.append(videoEncoderCap.mCodec);
        sb.append(", capabilities: mMinFrameWidth = ");
        sb.append(videoEncoderCap.mMinFrameWidth);
        sb.append(" , mMinFrameHeight = ");
        sb.append(videoEncoderCap.mMinFrameHeight);
        sb.append(" , mMaxFrameWidth = ");
        sb.append(videoEncoderCap.mMaxFrameWidth);
        sb.append(" , mMaxFrameHeight = ");
        sb.append(videoEncoderCap.mMaxFrameHeight);
        Log.e(TAG, sb.toString());
        return false;
    }

    private boolean isCurrentVideoResolutionSupportedByEncoder(VideoEncoderCap videoEncoderCap) {
        ListPreference findPreference = this.mPreferenceGroup.findPreference("pref_video_quality_key");
        boolean z = false;
        if (findPreference == null) {
            return false;
        }
        String value = findPreference.getValue();
        if (value != null) {
            Size parseSize = parseSize(value);
            if (parseSize.getWidth() > videoEncoderCap.mMaxFrameWidth || parseSize.getWidth() < videoEncoderCap.mMinFrameWidth || parseSize.getHeight() > videoEncoderCap.mMaxFrameHeight || parseSize.getHeight() < videoEncoderCap.mMinFrameHeight) {
                StringBuilder sb = new StringBuilder();
                sb.append("Codec = ");
                sb.append(videoEncoderCap.mCodec);
                sb.append(", capabilities: mMinFrameWidth = ");
                sb.append(videoEncoderCap.mMinFrameWidth);
                sb.append(" , mMinFrameHeight = ");
                sb.append(videoEncoderCap.mMinFrameHeight);
                sb.append(" , mMaxFrameWidth = ");
                sb.append(videoEncoderCap.mMaxFrameWidth);
                sb.append(" , mMaxFrameHeight = ");
                sb.append(videoEncoderCap.mMaxFrameHeight);
                Log.e(TAG, sb.toString());
            } else {
                z = true;
            }
        }
        return z;
    }

    private List<String> getSupportedVideoEncoders() {
        ArrayList arrayList = new ArrayList();
        for (VideoEncoderCap videoEncoderCap : EncoderCapabilities.getVideoEncoders()) {
            String videoEncoder = SettingTranslation.getVideoEncoder(videoEncoderCap.mCodec);
            if (videoEncoder != null && isCurrentVideoResolutionSupportedByEncoder(videoEncoderCap)) {
                arrayList.add(videoEncoder);
            }
        }
        return arrayList;
    }

    private List<String> getSupportedVideoEncoders(Size size) {
        ArrayList arrayList = new ArrayList();
        for (VideoEncoderCap videoEncoderCap : EncoderCapabilities.getVideoEncoders()) {
            String videoEncoder = SettingTranslation.getVideoEncoder(videoEncoderCap.mCodec);
            if (videoEncoder != null && isVideoResolutionSupportedByEncoder(size, videoEncoderCap)) {
                arrayList.add(videoEncoder);
            }
        }
        return arrayList;
    }

    private static List<String> getSupportedAudioEncoders(CharSequence[] charSequenceArr) {
        ArrayList arrayList = new ArrayList();
        for (CharSequence charSequence : charSequenceArr) {
            String charSequence2 = charSequence.toString();
            if (SettingTranslation.getAudioEncoder(charSequence2) != -1) {
                arrayList.add(charSequence2);
            }
        }
        return arrayList;
    }

    public List<String> getSupportedNoiseReductionModes(int i) {
        int[] iArr = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES);
        ArrayList arrayList = new ArrayList();
        if (iArr != null) {
            for (int noiseReduction : iArr) {
                String noiseReduction2 = SettingTranslation.getNoiseReduction(noiseReduction);
                if (noiseReduction2 != null) {
                    arrayList.add(noiseReduction2);
                }
            }
        }
        return arrayList;
    }

    private List<String> getSupportedZoomLevel(int i) {
        float floatValue = ((Float) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)).floatValue();
        ArrayList arrayList = new ArrayList();
        for (int i2 = 0; ((float) i2) <= floatValue; i2++) {
            arrayList.add(String.valueOf(i2));
        }
        return arrayList;
    }

    private void resetIfInvalid(ListPreference listPreference) {
        if (listPreference.findIndexOfValue(listPreference.getValue()) == -1) {
            listPreference.setValueIndex(0);
        }
    }

    private boolean filterSimilarPictureSize(PreferenceGroup preferenceGroup, ListPreference listPreference) {
        listPreference.filterDuplicated();
        if (listPreference.getEntries().length <= 1) {
            removePreference(preferenceGroup, listPreference.getKey());
            return true;
        }
        resetIfInvalid(listPreference);
        return false;
    }

    public List<String> getSupportedInstantAecAvailableModes(int i) {
        String str = "Supported instant aec modes is null.";
        String str2 = TAG;
        ArrayList arrayList = new ArrayList();
        try {
            int[] iArr = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CaptureModule.InstantAecAvailableModes);
            if (iArr == null) {
                return null;
            }
            for (int i2 : iArr) {
                StringBuilder sb = new StringBuilder();
                sb.append(BuildConfig.FLAVOR);
                sb.append(i2);
                arrayList.add(sb.toString());
            }
            return arrayList;
        } catch (NullPointerException unused) {
            Log.w(str2, str);
        } catch (IllegalArgumentException unused2) {
            Log.w(str2, str);
        }
    }

    public boolean getQcfaPrefEnabled() {
        String value = this.mPreferenceGroup.findPreference(KEY_QCFA).getValue();
        return value != null && value.equals("enable");
    }

    public boolean getIsSupportedQcfa(int i) {
        byte b;
        try {
            b = ((Byte) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CaptureModule.IS_SUPPORT_QCFA_SENSOR)).byteValue();
        } catch (Exception unused) {
            b = 0;
        }
        return b == 1;
    }

    public String getSupportedQcfaDimension(int i) {
        int[] iArr = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CaptureModule.QCFA_SUPPORT_DIMENSION);
        if (iArr == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i2 = 0; i2 < iArr.length; i2++) {
            sb.append(iArr[i2]);
            if (i2 == 0) {
                sb.append("x");
            }
        }
        return sb.toString();
    }

    public List<String> getSupportedSaturationLevelAvailableModes(int i) {
        int[] iArr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        ArrayList arrayList = new ArrayList();
        for (int i2 : iArr) {
            StringBuilder sb = new StringBuilder();
            sb.append(BuildConfig.FLAVOR);
            sb.append(i2);
            arrayList.add(sb.toString());
        }
        return arrayList;
    }

    public List<String> getSupportedAntiBandingLevelAvailableModes(int i) {
        int[] iArr = (int[]) ((CameraCharacteristics) this.mCharacteristics.get(i)).get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
        ArrayList arrayList = new ArrayList();
        for (int i2 : iArr) {
            StringBuilder sb = new StringBuilder();
            sb.append(BuildConfig.FLAVOR);
            sb.append(i2);
            arrayList.add(sb.toString());
        }
        return arrayList;
    }

    public List<String> getSupportedHistogramAvailableModes(int i) {
        String[] strArr = {"enable", "disable"};
        ArrayList arrayList = new ArrayList();
        for (String add : strArr) {
            arrayList.add(add);
        }
        return arrayList;
    }

    public List<String> getSupportedHdrAvailableModes(int i) {
        String[] strArr = {"enable", "disable"};
        ArrayList arrayList = new ArrayList();
        for (String add : strArr) {
            arrayList.add(add);
        }
        return arrayList;
    }

    public boolean isHistogramSupport() {
        String value = getValue(KEY_HISTOGRAM);
        return value != null && value.equals("enable");
    }

    public boolean isCamera2HDRSupport() {
        String value = getValue(KEY_HDR);
        return value != null && value.equals("enable");
    }

    public int getSavePictureFormat() {
        String value = getValue(KEY_PICTURE_FORMAT);
        if (value == null) {
            return 0;
        }
        return Integer.valueOf(value).intValue();
    }

    public boolean isZSLInHALEnabled() {
        String value = getValue(KEY_ZSL);
        return value != null && value.equals(this.mContext.getString(C0905R.string.pref_camera2_zsl_entryvalue_hal_zsl));
    }

    public boolean isZSLInAppEnabled() {
        String value = getValue(KEY_ZSL);
        return value != null && value.equals(this.mContext.getString(C0905R.string.pref_camera2_zsl_entryvalue_app_zsl));
    }

    private boolean filterUnsupportedOptions(ListPreference listPreference, List<String> list) {
        if (list == null) {
            removePreference(this.mPreferenceGroup, listPreference.getKey());
            return true;
        }
        listPreference.filterUnsupported(list);
        if (listPreference.getEntries().length <= 0) {
            removePreference(this.mPreferenceGroup, listPreference.getKey());
            return true;
        }
        resetIfInvalid(listPreference);
        return false;
    }

    public List<String> getDependentKeys(String str) {
        if (str.equals("pref_video_quality_key")) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(KEY_VIDEO_HIGH_FRAME_RATE);
            arrayList.add("pref_camera_videoencoder_key");
            return arrayList;
        }
        JSONObject dependencyList = getDependencyList(str, getValue(str));
        if (dependencyList == null) {
            return null;
        }
        ArrayList arrayList2 = new ArrayList();
        Iterator keys = dependencyList.keys();
        while (keys.hasNext()) {
            arrayList2.add((String) keys.next());
        }
        return arrayList2;
    }

    private JSONObject parseJson(String str) {
        try {
            InputStream open = this.mContext.getAssets().open(str);
            byte[] bArr = new byte[open.available()];
            open.read(bArr);
            open.close();
            return new JSONObject(new String(bArr, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private JSONObject getDependencyMapForKey(String str) {
        JSONObject jSONObject = this.mDependency;
        if (jSONObject == null) {
            return null;
        }
        try {
            return jSONObject.getJSONObject(str);
        } catch (JSONException unused) {
            StringBuilder sb = new StringBuilder();
            sb.append("getDependencyMapForKey JSONException No value for:");
            sb.append(str);
            Log.w(TAG, sb.toString());
            return null;
        }
    }

    private JSONObject getDependencyList(String str, String str2) {
        JSONObject dependencyMapForKey = getDependencyMapForKey(str);
        if (dependencyMapForKey == null) {
            return null;
        }
        if (!dependencyMapForKey.has(str2)) {
            str2 = "default";
        }
        if (!dependencyMapForKey.has(str2)) {
            return null;
        }
        try {
            return dependencyMapForKey.getJSONObject(getDependencyKey(dependencyMapForKey, str2));
        } catch (JSONException unused) {
            StringBuilder sb = new StringBuilder();
            sb.append("getDependencyList JSONException No value for:");
            sb.append(str);
            Log.w(TAG, sb.toString());
            return null;
        }
    }

    private String getDependencyKey(JSONObject jSONObject, String str) {
        return !jSONObject.has(str) ? "default" : str;
    }

    public void restoreSettings() {
        clearPerCameraPreferences();
        init();
    }

    private void clearPerCameraPreferences() {
        for (String sharedPreferences : ComboPreferences.getSharedPreferencesNames(this.mContext)) {
            Editor edit = this.mContext.getSharedPreferences(sharedPreferences, 0).edit();
            edit.clear();
            edit.commit();
        }
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        String str = CameraSettings.KEY_REQUEST_PERMISSION;
        boolean z = defaultSharedPreferences.getBoolean(str, false);
        Editor edit2 = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        edit2.clear();
        edit2.putBoolean(str, z);
        edit2.commit();
    }

    public boolean isDeveloperEnabled() {
        Context context = this.mContext;
        return context.getSharedPreferences(ComboPreferences.getGlobalSharedPreferencesName(context), 0).getBoolean(KEY_DEVELOPER_MENU, false);
    }

    public void setLightNavigationBar(Activity activity, boolean z) {
        int systemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
        activity.getWindow().getDecorView().setSystemUiVisibility(z ? systemUiVisibility | 16 : systemUiVisibility & -17);
    }

    public void setBokehMode(boolean z) {
        this.mIsBokehMode = z;
    }

    public boolean isCurrentModeSupportLongClick() {
        if ("HDR".equals(getCurrentMode())) {
            return false;
        }
        if ("BOKEH".equals(getCurrentMode())) {
            return false;
        }
        if ("NIGHT".equals(getCurrentMode())) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0027  */
    /* JADX WARNING: Removed duplicated region for block: B:16:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getCurrentMode() {
        /*
            r2 = this;
            java.lang.String r0 = "pref_camera2_scenemode_key"
            java.lang.String r0 = r2.getValue(r0)
            if (r0 == 0) goto L_0x0021
            int r0 = java.lang.Integer.parseInt(r0)
            r1 = 5
            if (r0 == r1) goto L_0x001e
            r1 = 18
            if (r0 == r1) goto L_0x001b
            r1 = 109(0x6d, float:1.53E-43)
            if (r0 == r1) goto L_0x0018
            goto L_0x0021
        L_0x0018:
            java.lang.String r0 = "PROMODE"
            goto L_0x0023
        L_0x001b:
            java.lang.String r0 = "HDR"
            goto L_0x0023
        L_0x001e:
            java.lang.String r0 = "NIGHT"
            goto L_0x0023
        L_0x0021:
            java.lang.String r0 = "0"
        L_0x0023:
            boolean r2 = r2.mIsBokehMode
            if (r2 == 0) goto L_0x0029
            java.lang.String r0 = "BOKEH"
        L_0x0029:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.SettingsManager.getCurrentMode():java.lang.String");
    }
}
