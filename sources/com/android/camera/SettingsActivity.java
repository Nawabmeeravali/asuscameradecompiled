package com.android.camera;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.camera.SettingsManager.Listener;
import com.android.camera.exif.ExifInterface.GpsMeasureMode;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.util.CameraUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.codeaurora.snapcam.C0905R;

public class SettingsActivity extends PreferenceActivity {
    private static final String LONG_SHOT_KEY = "pref_camera2_longshot_key";
    private static final String TAG = "SettingsActivity";
    private final int DEVELOPER_MENU_TOUCH_COUNT = 10;
    /* access modifiers changed from: private */
    public boolean mDeveloperMenuEnabled;
    private Listener mListener = new Listener() {
        public void onSettingsChanged(List<SettingState> list) {
            Map valuesMap = SettingsActivity.this.mSettingsManager.getValuesMap();
            for (SettingState settingState : list) {
                boolean z = ((Values) valuesMap.get(settingState.key)).overriddenValue == null;
                Preference findPreference = SettingsActivity.this.findPreference(settingState.key);
                if (findPreference != null) {
                    findPreference.setEnabled(z);
                    if (findPreference.getKey().equals(SettingsManager.KEY_QCFA) || findPreference.getKey().equals(SettingsManager.KEY_PICTURE_FORMAT)) {
                        SettingsActivity.this.mSettingsManager.updatePictureAndVideoSize();
                        SettingsActivity.this.updatePreference("pref_camera_picturesize_key");
                        SettingsActivity.this.updatePreference("pref_video_quality_key");
                    }
                    if (SettingsManager.KEY_SELFIEMIRROR.equals(findPreference.getKey())) {
                        Preference findPreference2 = SettingsActivity.this.findPreference("pref_camera_longshot_key");
                        if (findPreference2 instanceof SwitchPreference) {
                            if (((SwitchPreference) findPreference).isChecked()) {
                                SwitchPreference switchPreference = (SwitchPreference) findPreference2;
                                switchPreference.setChecked(false);
                                switchPreference.setEnabled(false);
                            } else {
                                ((SwitchPreference) findPreference2).setEnabled(true);
                            }
                        }
                    }
                    if (findPreference.getKey().equals(SettingsManager.KEY_VIDEO_HDR_VALUE)) {
                        SettingsActivity settingsActivity = SettingsActivity.this;
                        settingsActivity.mSettingsManager;
                        String str = SettingsManager.KEY_AUTO_HDR;
                        ListPreference listPreference = (ListPreference) settingsActivity.findPreference(str);
                        if (findPreference.getSummary().equals("enable")) {
                            listPreference.setEnabled(false);
                            String str2 = "disable";
                            listPreference.setValue(str2);
                            SettingsManager access$000 = SettingsActivity.this.mSettingsManager;
                            SettingsActivity.this.mSettingsManager;
                            access$000.setValue(str, str2);
                        } else {
                            listPreference.setEnabled(true);
                        }
                    }
                    if (findPreference.getKey().equals(SettingsManager.KEY_MANUAL_WB)) {
                        SettingsActivity.this.updateManualWBSettings();
                    }
                    if (findPreference.getKey().equals(SettingsManager.KEY_MANUAL_EXPOSURE)) {
                        SettingsActivity.this.UpdateManualExposureSettings();
                    }
                    if (findPreference.getKey().equals(SettingsManager.KEY_VIDEO_HIGH_FRAME_RATE)) {
                        SettingsActivity.this.updateDIS();
                    }
                }
            }
        }
    };
    private SharedPreferences mLocalSharedPref;
    private BroadcastReceiver mSDcardMountedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ListPreference listPreference = (ListPreference) SettingsActivity.this.findPreference("pref_camera_savepath_key");
            SettingsActivity.this.updateSavePathPreference();
            if (listPreference != null) {
                listPreference.onActivityDestroy();
            }
        }
    };
    /* access modifiers changed from: private */
    public SettingsManager mSettingsManager;
    private OnSharedPreferenceChangeListener mSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            Preference findPreference = SettingsActivity.this.findPreference(str);
            if (findPreference != null) {
                if (findPreference instanceof SwitchPreference) {
                    SettingsActivity.this.mSettingsManager.setValue(str, ((SwitchPreference) findPreference).isChecked() ? RecordLocationPreference.VALUE_ON : "off");
                } else if (findPreference instanceof ListPreference) {
                    SettingsActivity.this.mSettingsManager.setValue(str, ((ListPreference) findPreference).getValue());
                }
                if (str.equals("pref_video_quality_key")) {
                    SettingsActivity.this.updatePreference(SettingsManager.KEY_VIDEO_HIGH_FRAME_RATE);
                    SettingsActivity.this.updatePreference("pref_camera_videoencoder_key");
                    if (SettingsActivity.this.isVideoQuality4K()) {
                        SettingsActivity.this.updateDISForVideoQuality(false);
                    } else {
                        SettingsActivity.this.updateDISForVideoQuality(true);
                    }
                }
                List<String> dependentKeys = SettingsActivity.this.mSettingsManager.getDependentKeys(str);
                if (dependentKeys != null) {
                    for (String access$400 : dependentKeys) {
                        SettingsActivity.this.updatePreferenceButton(access$400);
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public SharedPreferences mSharedPreferences;
    String[] mSomeSceneMode = {"18", "5", "13", GpsMeasureMode.MODE_3_DIMENSIONAL, "4", "15", "10", "7", "8", "9", "12"};
    /* access modifiers changed from: private */
    public int privateCounter = 0;

    /* access modifiers changed from: private */
    public boolean isVideoQuality4K() {
        String str = "pref_video_quality_key";
        if (this.mSettingsManager.getValue(str) == null || CameraSettings.VIDEO_QUALITY_TABLE.get(this.mSettingsManager.getValue(str)) == null || ((Integer) CameraSettings.VIDEO_QUALITY_TABLE.get(this.mSettingsManager.getValue(str))).intValue() != 8) {
            return false;
        }
        return true;
    }

    private void showManualWBGainDialog(LinearLayout linearLayout, Builder builder) {
        TextView textView;
        LinearLayout linearLayout2 = linearLayout;
        Builder builder2 = builder;
        Editor edit = this.mLocalSharedPref.edit();
        TextView textView2 = new TextView(this);
        TextView textView3 = new TextView(this);
        EditText editText = new EditText(this);
        TextView textView4 = new TextView(this);
        TextView textView5 = new TextView(this);
        EditText editText2 = new EditText(this);
        TextView textView6 = new TextView(this);
        TextView textView7 = new TextView(this);
        EditText editText3 = new EditText(this);
        editText.setInputType(8194);
        editText2.setInputType(8194);
        editText3.setInputType(8194);
        float f = this.mLocalSharedPref.getFloat(SettingsManager.KEY_MANUAL_WB_R_GAIN, -1.0f);
        Editor editor = edit;
        float f2 = this.mLocalSharedPref.getFloat(SettingsManager.KEY_MANUAL_WB_G_GAIN, -1.0f);
        EditText editText4 = editText3;
        float f3 = this.mLocalSharedPref.getFloat(SettingsManager.KEY_MANUAL_WB_B_GAIN, -1.0f);
        int i = (((double) f) > -1.0d ? 1 : (((double) f) == -1.0d ? 0 : -1));
        String str = " Current rGain is ";
        if (i == 0) {
            textView3.setText(str);
            textView = textView6;
        } else {
            textView = textView6;
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(f);
            textView3.setText(sb.toString());
        }
        String str2 = " Current gGain is ";
        if (i == 0) {
            textView5.setText(str2);
        } else {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str2);
            sb2.append(f2);
            textView5.setText(sb2.toString());
        }
        String str3 = " Current bGain is ";
        if (i == 0) {
            textView7.setText(str3);
        } else {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(str3);
            sb3.append(f3);
            textView7.setText(sb3.toString());
        }
        float[] wBGainsRangeValues = this.mSettingsManager.getWBGainsRangeValues(this.mSettingsManager.getCurrentCameraId());
        if (wBGainsRangeValues == null) {
            builder2.setMessage("Enter gains value in the range get is NULL ");
        } else {
            StringBuilder sb4 = new StringBuilder();
            sb4.append("Enter gains value in the range of ");
            sb4.append(wBGainsRangeValues[0]);
            sb4.append(" to ");
            sb4.append(wBGainsRangeValues[1]);
            builder2.setMessage(sb4.toString());
        }
        linearLayout2.addView(textView2);
        linearLayout2.addView(editText);
        linearLayout2.addView(textView3);
        linearLayout2.addView(textView4);
        linearLayout2.addView(editText2);
        linearLayout2.addView(textView5);
        linearLayout2.addView(textView);
        EditText editText5 = editText4;
        linearLayout2.addView(editText5);
        linearLayout2.addView(textView7);
        builder2.setView(linearLayout2);
        final EditText editText6 = editText;
        final EditText editText7 = editText2;
        final EditText editText8 = editText5;
        final float[] fArr = wBGainsRangeValues;
        final Editor editor2 = editor;
        C07493 r0 = new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                String obj = editText6.getText().toString();
                String obj2 = editText7.getText().toString();
                String obj3 = editText8.getText().toString();
                float f = -1.0f;
                float parseFloat = obj.length() > 0 ? Float.parseFloat(obj) : -1.0f;
                float parseFloat2 = obj2.length() > 0 ? Float.parseFloat(obj2) : -1.0f;
                if (obj3.length() > 0) {
                    f = Float.parseFloat(obj3);
                }
                float[] fArr = fArr;
                if (fArr == null) {
                    RotateTextToast.makeText((Activity) SettingsActivity.this, (CharSequence) "Gains Range is NULL, Invalid gains", 0).show();
                    return;
                }
                int i2 = (parseFloat > fArr[1] ? 1 : (parseFloat == fArr[1] ? 0 : -1));
                String str = SettingsActivity.TAG;
                if (i2 > 0 || parseFloat < fArr[0]) {
                    RotateTextToast.makeText((Activity) SettingsActivity.this, (CharSequence) "Invalid rGain value:", 0).show();
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Setting rGain value : ");
                    sb.append(parseFloat);
                    Log.v(str, sb.toString());
                    editor2.putFloat(SettingsManager.KEY_MANUAL_WB_R_GAIN, parseFloat);
                }
                float[] fArr2 = fArr;
                if (parseFloat2 > fArr2[1] || parseFloat2 < fArr2[0]) {
                    RotateTextToast.makeText((Activity) SettingsActivity.this, (CharSequence) "Invalid gGain value:", 0).show();
                } else {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Setting gGain value : ");
                    sb2.append(parseFloat2);
                    Log.v(str, sb2.toString());
                    editor2.putFloat(SettingsManager.KEY_MANUAL_WB_G_GAIN, parseFloat2);
                }
                float[] fArr3 = fArr;
                if (f > fArr3[1] || f < fArr3[0]) {
                    RotateTextToast.makeText((Activity) SettingsActivity.this, (CharSequence) "Invalid bGain value:", 0).show();
                } else {
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("Setting bGain value : ");
                    sb3.append(f);
                    Log.v(str, sb3.toString());
                    editor2.putFloat(SettingsManager.KEY_MANUAL_WB_B_GAIN, f);
                }
                editor2.apply();
            }
        };
        builder2.setPositiveButton("Ok", r0);
        builder.show();
    }

    /* access modifiers changed from: private */
    public void updateManualWBSettings() {
        int currentCameraId = this.mSettingsManager.getCurrentCameraId();
        final Editor edit = this.mLocalSharedPref.edit();
        Builder builder = new Builder(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        builder.setTitle("Manual White Balance Settings");
        builder.setNegativeButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        String string = getString(C0905R.string.pref_camera_manual_wb_value_color_temperature);
        String string2 = getString(C0905R.string.pref_camera_manual_wb_value_rbgb_gains);
        String str = "-1";
        String string3 = this.mLocalSharedPref.getString(SettingsManager.KEY_MANUAL_WB_TEMPERATURE_VALUE, str);
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_MANUAL_WB);
        StringBuilder sb = new StringBuilder();
        sb.append("manualWBMode selected = ");
        sb.append(value);
        Log.v(TAG, sb.toString());
        final int[] wBColorTemperatureRangeValues = this.mSettingsManager.getWBColorTemperatureRangeValues(currentCameraId);
        if (value.equals(string)) {
            TextView textView = new TextView(this);
            final EditText editText = new EditText(this);
            editText.setInputType(2);
            String str2 = " Current CCT is ";
            if (string3.equals(str)) {
                textView.setText(str2);
            } else {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(str2);
                sb2.append(string3);
                textView.setText(sb2.toString());
            }
            if (wBColorTemperatureRangeValues == null) {
                builder.setMessage("Enter CCT value is get NULL ");
            } else {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Enter CCT value in the range of ");
                sb3.append(wBColorTemperatureRangeValues[0]);
                sb3.append(" to ");
                sb3.append(wBColorTemperatureRangeValues[1]);
                builder.setMessage(sb3.toString());
            }
            linearLayout.addView(editText);
            linearLayout.addView(textView);
            builder.setView(linearLayout);
            builder.setPositiveButton("Ok", new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    String obj = editText.getText().toString();
                    int parseInt = obj.length() > 0 ? Integer.parseInt(obj) : -1;
                    int[] iArr = wBColorTemperatureRangeValues;
                    if (iArr == null) {
                        RotateTextToast.makeText((Activity) SettingsActivity.this, (CharSequence) "CCT Range is NULL", 0).show();
                        return;
                    }
                    if (parseInt > iArr[1] || parseInt < iArr[0]) {
                        RotateTextToast.makeText((Activity) SettingsActivity.this, (CharSequence) "Invalid CCT", 0).show();
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Setting CCT value : ");
                        sb.append(parseInt);
                        Log.v(SettingsActivity.TAG, sb.toString());
                        edit.putString(SettingsManager.KEY_MANUAL_WB_TEMPERATURE_VALUE, obj);
                        edit.apply();
                    }
                }
            });
            builder.show();
        } else if (value.equals(string2)) {
            showManualWBGainDialog(linearLayout, builder);
        }
    }

    /* access modifiers changed from: private */
    public void UpdateManualExposureSettings() {
        long[] jArr;
        int currentCameraId = this.mSettingsManager.getCurrentCameraId();
        SharedPreferences sharedPreferences = getSharedPreferences(ComboPreferences.getLocalSharedPreferencesName(this, currentCameraId), 0);
        final Editor edit = sharedPreferences.edit();
        Builder builder = new Builder(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        TextView textView = new TextView(this);
        final EditText editText = new EditText(this);
        TextView textView2 = new TextView(this);
        EditText editText2 = new EditText(this);
        editText.setInputType(2);
        editText2.setInputType(2);
        builder.setTitle("Manual Exposure Settings");
        builder.setNegativeButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        String string = getString(C0905R.string.pref_camera_manual_exp_value_ISO_priority);
        String string2 = getString(C0905R.string.pref_camera_manual_exp_value_exptime_priority);
        String string3 = getString(C0905R.string.pref_camera_manual_exp_value_user_setting);
        String string4 = getString(C0905R.string.pref_camera_manual_exp_value_gains_priority);
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_MANUAL_EXPOSURE);
        String str = "-1";
        String str2 = string4;
        String string5 = sharedPreferences.getString(SettingsManager.KEY_MANUAL_ISO_VALUE, str);
        String str3 = string3;
        long[] exposureRangeValues = this.mSettingsManager.getExposureRangeValues(currentCameraId);
        EditText editText3 = editText2;
        final int[] isoRangeValues = this.mSettingsManager.getIsoRangeValues(currentCameraId);
        if (!string5.equals(str)) {
            StringBuilder sb = new StringBuilder();
            jArr = exposureRangeValues;
            sb.append("Current ISO is ");
            sb.append(string5);
            textView.setText(sb.toString());
        } else {
            jArr = exposureRangeValues;
        }
        String string6 = sharedPreferences.getString(SettingsManager.KEY_MANUAL_EXPOSURE_VALUE, str);
        if (!string6.equals(str)) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Current exposure time is ");
            sb2.append(string6);
            textView2.setText(sb2.toString());
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append("manual Exposure Mode selected = ");
        sb3.append(value);
        Log.v(TAG, sb3.toString());
        boolean equals = value.equals(string);
        String str4 = " to ";
        String str5 = "Enter ISO in the range of ";
        String str6 = "Ok";
        if (equals) {
            StringBuilder sb4 = new StringBuilder();
            sb4.append(str5);
            sb4.append(isoRangeValues[0]);
            sb4.append(str4);
            sb4.append(isoRangeValues[1]);
            builder.setMessage(sb4.toString());
            linearLayout.addView(editText);
            linearLayout.addView(textView);
            builder.setView(linearLayout);
            builder.setPositiveButton(str6, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    String obj = editText.getText().toString();
                    StringBuilder sb = new StringBuilder();
                    sb.append("string iso length ");
                    sb.append(obj.length());
                    sb.append(", iso :");
                    sb.append(obj);
                    Log.v(SettingsActivity.TAG, sb.toString());
                    int parseInt = obj.length() > 0 ? Integer.parseInt(obj) : -1;
                    int[] iArr = isoRangeValues;
                    if (parseInt > iArr[1] || parseInt < iArr[0]) {
                        RotateTextToast.makeText((Activity) SettingsActivity.this, (CharSequence) "Invalid ISO", 0).show();
                        return;
                    }
                    edit.putString(SettingsManager.KEY_MANUAL_ISO_VALUE, obj);
                    edit.apply();
                }
            });
            builder.show();
            return;
        }
        boolean equals2 = value.equals(string2);
        String str7 = "Get Exposure time range is NULL ";
        String str8 = "ns";
        SharedPreferences sharedPreferences2 = sharedPreferences;
        String str9 = "ns to ";
        TextView textView3 = textView;
        String str10 = "Enter exposure time in the range of ";
        if (equals2) {
            if (jArr == null) {
                builder.setMessage(str7);
            } else {
                StringBuilder sb5 = new StringBuilder();
                sb5.append(str10);
                sb5.append(jArr[0]);
                sb5.append(str9);
                sb5.append(jArr[1]);
                sb5.append(str8);
                builder.setMessage(sb5.toString());
            }
            final EditText editText4 = editText3;
            linearLayout.addView(editText4);
            linearLayout.addView(textView2);
            builder.setView(linearLayout);
            final long[] jArr2 = jArr;
            builder.setPositiveButton(str6, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    double d;
                    String obj = editText4.getText().toString();
                    if (obj.length() > 0) {
                        try {
                            d = Double.parseDouble(obj);
                        } catch (NumberFormatException unused) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Input expTime ");
                            sb.append(obj);
                            sb.append(" is invalid");
                            Log.w(SettingsActivity.TAG, sb.toString());
                            d = Double.parseDouble(obj) + 1.0d;
                        }
                    } else {
                        d = -1.0d;
                    }
                    long[] jArr = jArr2;
                    if (jArr == null || d > ((double) jArr[1]) || d < ((double) jArr[0])) {
                        RotateTextToast.makeText((Activity) SettingsActivity.this, (CharSequence) "Invalid exposure time", 0).show();
                        return;
                    }
                    edit.putString(SettingsManager.KEY_MANUAL_EXPOSURE_VALUE, obj);
                    edit.apply();
                }
            });
            builder.show();
            return;
        }
        EditText editText5 = editText3;
        String str11 = str6;
        String str12 = str3;
        long[] jArr3 = jArr;
        if (value.equals(str12)) {
            builder.setMessage("Full manual mode - Enter both ISO and Exposure Time");
            TextView textView4 = new TextView(this);
            TextView textView5 = new TextView(this);
            StringBuilder sb6 = new StringBuilder();
            sb6.append(str5);
            Editor editor = edit;
            sb6.append(isoRangeValues[0]);
            sb6.append(str4);
            sb6.append(isoRangeValues[1]);
            textView4.setText(sb6.toString());
            if (jArr3 == null) {
                textView5.setText(str7);
            } else {
                StringBuilder sb7 = new StringBuilder();
                sb7.append(str10);
                sb7.append(jArr3[0]);
                sb7.append(str9);
                sb7.append(jArr3[1]);
                sb7.append(str8);
                textView5.setText(sb7.toString());
            }
            linearLayout.addView(textView4);
            linearLayout.addView(editText);
            linearLayout.addView(textView3);
            linearLayout.addView(textView5);
            linearLayout.addView(editText5);
            linearLayout.addView(textView2);
            builder.setView(linearLayout);
            final EditText editText6 = editText;
            final int[] iArr = isoRangeValues;
            final Editor editor2 = editor;
            final EditText editText7 = editText5;
            final long[] jArr4 = jArr3;
            C07559 r0 = new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    String obj = editText6.getText().toString();
                    StringBuilder sb = new StringBuilder();
                    sb.append("string iso length ");
                    sb.append(obj.length());
                    sb.append(", iso :");
                    sb.append(obj);
                    String sb2 = sb.toString();
                    String str = SettingsActivity.TAG;
                    Log.v(str, sb2);
                    int parseInt = obj.length() > 0 ? Integer.parseInt(obj) : -1;
                    int[] iArr = iArr;
                    if (parseInt > iArr[1] || parseInt < iArr[0]) {
                        RotateTextToast.makeText((Activity) SettingsActivity.this, (CharSequence) "Invalid ISO", 0).show();
                    } else {
                        editor2.putString(SettingsManager.KEY_MANUAL_ISO_VALUE, obj);
                        editor2.apply();
                    }
                    double d = -1.0d;
                    String obj2 = editText7.getText().toString();
                    if (obj2.length() > 0) {
                        try {
                            d = Double.parseDouble(obj2);
                        } catch (NumberFormatException unused) {
                            StringBuilder sb3 = new StringBuilder();
                            sb3.append("Input expTime ");
                            sb3.append(obj2);
                            sb3.append(" is invalid");
                            Log.w(str, sb3.toString());
                            d = Double.parseDouble(obj2) + 1.0d;
                        }
                    }
                    long[] jArr = jArr4;
                    if (jArr == null || d > ((double) jArr[1]) || d < ((double) jArr[0])) {
                        RotateTextToast.makeText((Activity) SettingsActivity.this, (CharSequence) "Invalid exposure time", 0).show();
                        return;
                    }
                    editor2.putString(SettingsManager.KEY_MANUAL_EXPOSURE_VALUE, obj2);
                    editor2.apply();
                }
            };
            builder.setPositiveButton(str11, r0);
            builder.show();
            return;
        }
        EditText editText8 = editText5;
        TextView textView6 = textView3;
        if (value.equals(str2)) {
            handleManualGainsPriority(linearLayout, textView6, editText8, sharedPreferences2);
        }
    }

    /* access modifiers changed from: private */
    public void updateDIS() {
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_VIDEO_HIGH_FRAME_RATE);
        String str = "pref_camera_dis_key";
        SwitchPreference switchPreference = (SwitchPreference) findPreference(str);
        if (value != null) {
            String str2 = "off";
            if (!value.equals(str2) && Integer.valueOf(value.substring(3)).intValue() > 60) {
                switchPreference.setChecked(false);
                switchPreference.setEnabled(false);
                this.mSettingsManager.setValue(str, str2);
                return;
            }
        }
        switchPreference.setEnabled(true);
    }

    /* access modifiers changed from: private */
    public void updateDISForVideoQuality(boolean z) {
        String str = "pref_camera_dis_key";
        SwitchPreference switchPreference = (SwitchPreference) findPreference(str);
        if (z) {
            switchPreference.setEnabled(true);
            return;
        }
        this.mSettingsManager.setValue(str, "off");
        switchPreference.setChecked(false);
        switchPreference.setEnabled(false);
    }

    private void handleManualGainsPriority(LinearLayout linearLayout, TextView textView, final EditText editText, SharedPreferences sharedPreferences) {
        final Editor edit = sharedPreferences.edit();
        Builder builder = new Builder(this);
        int[] isoRangeValues = this.mSettingsManager.getIsoRangeValues(this.mSettingsManager.getCurrentCameraId());
        final float[] fArr = {1.0f, ((float) isoRangeValues[1]) / ((float) isoRangeValues[0])};
        float f = sharedPreferences.getFloat(SettingsManager.KEY_MANUAL_GAINS_VALUE, -1.0f);
        if (f != -1.0f) {
            StringBuilder sb = new StringBuilder();
            sb.append(" Current Gains is ");
            sb.append(f);
            textView.setText(sb.toString());
        } else {
            textView.setText(" Please enter gains value ");
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Enter gains in the range of ");
        sb2.append(fArr[0]);
        sb2.append(" to ");
        sb2.append(fArr[1]);
        builder.setMessage(sb2.toString());
        editText.setInputType(8194);
        linearLayout.addView(editText);
        linearLayout.addView(textView);
        builder.setView(linearLayout);
        builder.setPositiveButton("Ok", new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                String obj = editText.getText().toString();
                StringBuilder sb = new StringBuilder();
                sb.append("string gain length ");
                sb.append(obj.length());
                sb.append(", gain :");
                sb.append(obj);
                Log.v(SettingsActivity.TAG, sb.toString());
                float parseFloat = obj.length() > 0 ? Float.parseFloat(obj) : -1.0f;
                float[] fArr = fArr;
                if (parseFloat > fArr[1] || parseFloat < fArr[0]) {
                    RotateTextToast.makeText((Activity) SettingsActivity.this, (CharSequence) "Invalid GAINS", 0).show();
                    return;
                }
                edit.putFloat(SettingsManager.KEY_MANUAL_GAINS_VALUE, parseFloat);
                edit.apply();
            }
        });
        builder.show();
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setFlags(1024, 1024);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(C0905R.string.settings_title));
        }
        if (getIntent().getBooleanExtra(CameraUtil.KEY_IS_SECURE_CAMERA, false)) {
            setShowInLockScreen();
        }
        this.mSettingsManager = SettingsManager.getInstance();
        SettingsManager settingsManager = this.mSettingsManager;
        if (settingsManager == null) {
            finish();
            return;
        }
        settingsManager.setLightNavigationBar(this, true);
        this.mLocalSharedPref = getSharedPreferences(ComboPreferences.getLocalSharedPreferencesName(this, this.mSettingsManager.getCurrentCameraId()), 0);
        this.mSettingsManager.registerListener(this.mListener);
        addPreferencesFromResource(C0905R.xml.setting_menu_preferences);
        this.mSharedPreferences = getPreferenceManager().getSharedPreferences();
        this.mDeveloperMenuEnabled = this.mSharedPreferences.getBoolean(SettingsManager.KEY_DEVELOPER_MENU, false);
        filterPreferences();
        initializePreferences();
        this.mSharedPreferences.registerOnSharedPreferenceChangeListener(this.mSharedPreferenceChangeListener);
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            PreferenceCategory preferenceCategory = (PreferenceCategory) getPreferenceScreen().getPreference(i);
            for (int i2 = 0; i2 < preferenceCategory.getPreferenceCount(); i2++) {
                preferenceCategory.getPreference(i2).setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        if (!SettingsActivity.this.mDeveloperMenuEnabled) {
                            if (preference.getKey().equals("version_info")) {
                                SettingsActivity.this.privateCounter = SettingsActivity.this.privateCounter + 1;
                                if (SettingsActivity.this.privateCounter >= 10) {
                                    SettingsActivity.this.mDeveloperMenuEnabled = true;
                                    Editor edit = SettingsActivity.this.mSharedPreferences.edit();
                                    String str = SettingsManager.KEY_DEVELOPER_MENU;
                                    edit.putBoolean(str, true).apply();
                                    SettingsActivity settingsActivity = SettingsActivity.this;
                                    settingsActivity.getSharedPreferences(ComboPreferences.getGlobalSharedPreferencesName(settingsActivity), 0).edit().putBoolean(str, true).apply();
                                    Toast.makeText(SettingsActivity.this, "Camera developer option is enabled now", 0).show();
                                    SettingsActivity.this.recreate();
                                }
                            } else {
                                SettingsActivity.this.privateCounter = 0;
                            }
                        }
                        if (preference.getKey().equals(SettingsManager.KEY_RESTORE_DEFAULT)) {
                            SettingsActivity.this.onRestoreDefaultSettingsClick();
                        }
                        return false;
                    }
                });
            }
        }
        registerSDcardMountedReceiver();
    }

    private void filterPreferences() {
        String str = "developer";
        String[] strArr = {"photo", "video", "general", str};
        Set<String> filteredKeys = this.mSettingsManager.getFilteredKeys();
        if (!this.mDeveloperMenuEnabled) {
            if (filteredKeys != null) {
                filteredKeys.add(SettingsManager.KEY_MONO_PREVIEW);
                filteredKeys.add(SettingsManager.KEY_MONO_ONLY);
                filteredKeys.add(SettingsManager.KEY_CLEARSIGHT);
            }
            PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference(str);
            if (preferenceGroup != null) {
                getPreferenceScreen().removePreference(preferenceGroup);
            }
        }
        List asList = Arrays.asList(this.mSettingsManager.getEntries(SettingsManager.KEY_SCENE_MODE));
        if (this.mDeveloperMenuEnabled && !asList.contains("HDR")) {
            Preference findPreference = findPreference(SettingsManager.KEY_HDR);
            if (findPreference != null) {
                ((PreferenceGroup) findPreference(str)).removePreference(findPreference);
            }
        }
        if ("JP".equals(SystemProperties.get("ro.config.versatility", "WW")) && filteredKeys != null) {
            filteredKeys.add("pref_camera_shuttersound_key");
        }
        if (filteredKeys != null) {
            for (String findPreference2 : filteredKeys) {
                Preference findPreference3 = findPreference(findPreference2);
                if (findPreference3 != null) {
                    int i = 0;
                    while (i < strArr.length && !((PreferenceGroup) findPreference(strArr[i])).removePreference(findPreference3)) {
                        i++;
                    }
                }
            }
        }
    }

    private void initializePreferences() {
        updatePreference("pref_camera_picturesize_key");
        updatePreference("pref_video_quality_key");
        if (isVideoQuality4K()) {
            updateDISForVideoQuality(false);
        } else {
            updateDISForVideoQuality(true);
        }
        updatePreference("pref_camera_exposure_key");
        updatePreference(SettingsManager.KEY_VIDEO_HIGH_FRAME_RATE);
        updatePreference("pref_camera_videoencoder_key");
        updatePreference(SettingsManager.KEY_ZOOM);
        updatePreference(SettingsManager.KEY_SWITCH_CAMERA);
        updatePictureSizePreferenceButton();
        updateVideoHDRPreference();
        updateSavePathPreference();
        updateLongClickPreference(LONG_SHOT_KEY);
        for (Entry entry : this.mSettingsManager.getValuesMap().entrySet()) {
            Preference findPreference = findPreference((String) entry.getKey());
            if (findPreference != null) {
                Values values = (Values) entry.getValue();
                boolean z = values.overriddenValue != null;
                String str = z ? values.overriddenValue : values.value;
                if (findPreference instanceof SwitchPreference) {
                    if ("enable".equals(str)) {
                        str = RecordLocationPreference.VALUE_ON;
                    } else if ("disable".equals(str)) {
                        str = "off";
                    }
                    ((SwitchPreference) findPreference).setChecked(isOn(str));
                } else if (findPreference instanceof ListPreference) {
                    ListPreference listPreference = (ListPreference) findPreference;
                    listPreference.setValue(str);
                    if (listPreference.getEntryValues().length == 1) {
                        listPreference.setEnabled(false);
                    }
                }
                if (z) {
                    findPreference.setEnabled(false);
                }
            }
        }
        updateSceneModeMutualExclusivePreference();
        updateBokehModePreference();
        updateMakeupModePreference();
        try {
            String str2 = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            findPreference("version_info").setSummary(str2.substring(0, str2.indexOf(32)));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateVideoHDRPreference() {
        ListPreference listPreference = (ListPreference) findPreference(SettingsManager.KEY_VIDEO_HDR_VALUE);
        if (listPreference != null) {
            listPreference.setEnabled(this.mSettingsManager.isZZHDRSupported());
        }
    }

    /* access modifiers changed from: private */
    public void updatePreferenceButton(String str) {
        Preference findPreference = findPreference(str);
        if (findPreference == null) {
            return;
        }
        if (findPreference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) findPreference;
            if (listPreference.getEntryValues().length == 1) {
                listPreference.setEnabled(false);
            } else {
                listPreference.setEnabled(true);
            }
        } else {
            findPreference.setEnabled(false);
        }
    }

    private void updatePictureSizePreferenceButton() {
        String str = "pref_camera_picturesize_key";
        ListPreference listPreference = (ListPreference) findPreference(str);
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
        if (value != null && listPreference != null) {
            int parseInt = Integer.parseInt(value);
            String value2 = this.mSettingsManager.getValue(SettingsManager.KEY_BOKEH_MODE);
            boolean z = true;
            boolean z2 = value2 != null && value2.equals(RecordLocationPreference.VALUE_ON);
            if (z2) {
                CharSequence charSequence = listPreference.getEntryValues()[0];
                listPreference.setValue(charSequence.toString());
                this.mSettingsManager.setValue(str, charSequence.toString());
            }
            if (parseInt == 100 || z2) {
                z = false;
            }
            listPreference.setEnabled(z);
        }
    }

    /* access modifiers changed from: private */
    public void updatePreference(String str) {
        ListPreference listPreference = (ListPreference) findPreference(str);
        if (listPreference != null && this.mSettingsManager.getEntries(str) != null) {
            listPreference.setEntries(this.mSettingsManager.getEntries(str));
            listPreference.setEntryValues(this.mSettingsManager.getEntryValues(str));
            int valueIndex = this.mSettingsManager.getValueIndex(str);
            if (valueIndex < 0) {
                valueIndex = 0;
            }
            listPreference.setValueIndex(valueIndex);
        }
    }

    private boolean isOn(String str) {
        return str.equals(RecordLocationPreference.VALUE_ON) || str.equals("enable");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        this.mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this.mSharedPreferenceChangeListener);
        finish();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        this.mSettingsManager.unregisterListener(this.mListener);
        unregisterReceiver(this.mSDcardMountedReceiver);
    }

    private void setShowInLockScreen() {
        Window window = getWindow();
        LayoutParams attributes = window.getAttributes();
        attributes.flags |= 524288;
        window.setAttributes(attributes);
    }

    /* access modifiers changed from: private */
    public void onRestoreDefaultSettingsClick() {
        new Builder(this).setMessage(C0905R.string.pref_camera2_restore_default_hint).setPositiveButton(17039379, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                SettingsActivity.this.restoreSettings();
            }
        }).setNegativeButton(17039360, null).show();
    }

    /* access modifiers changed from: private */
    public void restoreSettings() {
        this.mSettingsManager.restoreSettings();
        filterPreferences();
        initializePreferences();
    }

    /* access modifiers changed from: private */
    public void updateSavePathPreference() {
        ListPreference listPreference = (ListPreference) findPreference("pref_camera_savepath_key");
        if (listPreference == null) {
            return;
        }
        if (SDCard.instance().isWriteable()) {
            listPreference.setEntries(new String[]{getResources().getString(C0905R.string.pref_camera_savepath_entry_0), getResources().getString(C0905R.string.pref_camera_savepath_entry_1)});
            return;
        }
        listPreference.setEntries(new String[]{getResources().getString(C0905R.string.pref_camera_savepath_entry_0)});
    }

    private void updateLongClickPreference(String str) {
        if (!this.mSettingsManager.isCurrentModeSupportLongClick()) {
            Preference findPreference = findPreference(str);
            if (findPreference instanceof SwitchPreference) {
                SwitchPreference switchPreference = (SwitchPreference) findPreference;
                switchPreference.setChecked(false);
                switchPreference.setEnabled(false);
            }
        }
    }

    private void registerSDcardMountedReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_MOUNTED");
        intentFilter.addAction("android.intent.action.MEDIA_SHARED");
        intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        intentFilter.addDataScheme("file");
        registerReceiver(this.mSDcardMountedReceiver, intentFilter);
    }

    private void updateSceneModeMutualExclusivePreference() {
        if (Arrays.asList(this.mSomeSceneMode).contains(this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE))) {
            Preference findPreference = findPreference("pref_camera_longshot_key");
            if (findPreference instanceof SwitchPreference) {
                SwitchPreference switchPreference = (SwitchPreference) findPreference;
                switchPreference.setChecked(false);
                switchPreference.setEnabled(false);
            }
            Preference findPreference2 = findPreference("pref_camera_exposure_key");
            if (findPreference2 instanceof ListPreference) {
                ((ListPreference) findPreference2).setEnabled(false);
            }
            Preference findPreference3 = findPreference("pref_camera_whitebalance_key");
            if (findPreference3 instanceof ListPreference) {
                ((ListPreference) findPreference3).setEnabled(false);
            }
        }
    }

    private void updateBokehModePreference() {
        Preference findPreference = findPreference("pref_camera_longshot_key");
        if (RecordLocationPreference.VALUE_ON.equals(this.mSettingsManager.getValue(SettingsManager.KEY_BOKEH_MODE)) && (findPreference instanceof SwitchPreference)) {
            SwitchPreference switchPreference = (SwitchPreference) findPreference;
            switchPreference.setChecked(false);
            switchPreference.setEnabled(false);
        }
    }

    private void updateMakeupModePreference() {
        Preference findPreference = findPreference("pref_camera_facedetection_key");
        if (RecordLocationPreference.VALUE_ON.equals(this.mSettingsManager.getValue(SettingsManager.KEY_MAKEUP_MODE)) && (findPreference instanceof SwitchPreference)) {
            ((SwitchPreference) findPreference).setEnabled(false);
        }
    }
}
