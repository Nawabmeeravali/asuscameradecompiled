package com.android.camera;

import android.app.Activity;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.camera.p004ui.RotateLayout;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.util.CameraUtil;
import org.codeaurora.snapcam.C0905R;

public class TsMakeupManager implements OnSeekBarChangeListener {
    private static final int CLICK_THRESHOLD = 200;
    public static final boolean HAS_TS_MAKEUP = SystemProperties.getBoolean("persist.ts.rtmakeup", false);
    public static final String MAKEUP_NONE = "none";
    public static final String MAKEUP_OFF = "Off";
    public static final String MAKEUP_ON = "On";
    private static final int MAKEUP_UI_STATUS_DISMISS = 3;
    private static final int MAKEUP_UI_STATUS_NONE = 0;
    private static final int MAKEUP_UI_STATUS_OFF = 2;
    private static final int MAKEUP_UI_STATUS_ON = 1;
    private static final int MODE_CLEAN = 2;
    private static final int MODE_NONE = 0;
    private static final int MODE_WHITEN = 1;
    private static final String TAG = "TsMakeupManager";
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    /* access modifiers changed from: private */
    public RelativeLayout mMakeupLayoutRoot;
    /* access modifiers changed from: private */
    public MakeupLevelListener mMakeupLevelListener;
    private LinearLayout mMakeupLevelRoot;
    /* access modifiers changed from: private */
    public LinearLayout mMakeupSingleRoot;
    private int mMakeupUIStatus = 0;
    private PhotoMenu mMenu;
    /* access modifiers changed from: private */
    public int mMode = 0;
    private PreferenceGroup mPreferenceGroup;
    /* access modifiers changed from: private */
    public int mSingleSelectedIndex = 0;
    private View mTsMakeupSwitcher;
    /* access modifiers changed from: private */
    public PhotoUI mUI;

    interface MakeupLevelListener {
        void onMakeupLevel(String str, String str2);
    }

    public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void setMakeupLevelListener(MakeupLevelListener makeupLevelListener) {
        this.mMakeupLevelListener = makeupLevelListener;
    }

    public TsMakeupManager(CameraActivity cameraActivity, PhotoMenu photoMenu, PhotoUI photoUI, PreferenceGroup preferenceGroup, View view) {
        this.mActivity = cameraActivity;
        this.mUI = photoUI;
        this.mMenu = photoMenu;
        this.mPreferenceGroup = preferenceGroup;
        this.mTsMakeupSwitcher = view;
        this.mMakeupLayoutRoot = (RelativeLayout) this.mUI.getRootView().findViewById(C0905R.C0907id.id_tsmakeup_level_layout_root);
        this.mMakeupUIStatus = 0;
    }

    public View getMakeupLayoutRoot() {
        return this.mMakeupLayoutRoot;
    }

    public boolean isShowMakeup() {
        RelativeLayout relativeLayout = this.mMakeupLayoutRoot;
        return relativeLayout != null && relativeLayout.isShown();
    }

    public void removeAllViews() {
        LinearLayout linearLayout = this.mMakeupSingleRoot;
        if (linearLayout != null) {
            linearLayout.removeAllViews();
            this.mMakeupSingleRoot = null;
        }
        LinearLayout linearLayout2 = this.mMakeupLevelRoot;
        if (linearLayout2 != null) {
            linearLayout2.removeAllViews();
            this.mMakeupLevelRoot = null;
        }
        RelativeLayout relativeLayout = this.mMakeupLayoutRoot;
        if (relativeLayout != null) {
            relativeLayout.removeAllViews();
        }
    }

    public void dismissMakeupUI() {
        this.mMakeupUIStatus = 3;
        removeAllViews();
        RelativeLayout relativeLayout = this.mMakeupLayoutRoot;
        if (relativeLayout != null) {
            relativeLayout.setVisibility(8);
        }
    }

    public void resetMakeupUIStatus() {
        this.mMakeupUIStatus = 0;
    }

    /* access modifiers changed from: private */
    public void changeMakeupIcon(String str) {
        if (!TextUtils.isEmpty(str)) {
            String str2 = MAKEUP_OFF;
            if (!str2.equals(str)) {
                str2 = MAKEUP_ON;
            }
            IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(CameraSettings.KEY_TS_MAKEUP_UILABLE);
            if (iconListPreference != null) {
                iconListPreference.setValue(str2);
                ((ImageView) this.mTsMakeupSwitcher).setImageResource(iconListPreference.getLargeIconIds()[iconListPreference.getCurrentIndex()]);
                iconListPreference.setMakeupSeekBarValue(str2);
            }
        }
    }

    public void hideMakeupUI() {
        IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(CameraSettings.KEY_TS_MAKEUP_UILABLE);
        if (iconListPreference != null) {
            this.mMakeupUIStatus = 0;
            String value = iconListPreference.getValue();
            StringBuilder sb = new StringBuilder();
            sb.append("TsMakeupManager.hideMakeupUI(): tsMakeupOn is ");
            sb.append(value);
            Log.d(TAG, sb.toString());
            if (MAKEUP_ON.equals(value)) {
                int findIndexOfValue = iconListPreference.findIndexOfValue(iconListPreference.getValue());
                CharSequence[] entryValues = iconListPreference.getEntryValues();
                int length = (findIndexOfValue + 1) % entryValues.length;
                iconListPreference.setMakeupSeekBarValue((String) entryValues[length]);
                ((ImageView) this.mTsMakeupSwitcher).setImageResource(iconListPreference.getLargeIconIds()[length]);
                MakeupLevelListener makeupLevelListener = this.mMakeupLevelListener;
                String value2 = iconListPreference.getValue();
                String str = CameraSettings.KEY_TS_MAKEUP_LEVEL;
                makeupLevelListener.onMakeupLevel(str, value2);
                ((IconListPreference) this.mPreferenceGroup.findPreference(str)).setValueIndex(0);
                this.mMakeupLayoutRoot.setVisibility(8);
                this.mMakeupLayoutRoot.removeAllViews();
                LinearLayout linearLayout = this.mMakeupSingleRoot;
                if (linearLayout != null) {
                    linearLayout.removeAllViews();
                    this.mMakeupSingleRoot = null;
                }
                LinearLayout linearLayout2 = this.mMakeupLevelRoot;
                if (linearLayout2 != null) {
                    linearLayout2.removeAllViews();
                    this.mMakeupLevelRoot = null;
                }
            }
        }
    }

    public void showMakeupView() {
        int i;
        LayoutParams layoutParams;
        RelativeLayout.LayoutParams layoutParams2;
        this.mMakeupUIStatus = 2;
        this.mMakeupLayoutRoot.setVisibility(8);
        this.mMakeupLayoutRoot.removeAllViews();
        LinearLayout linearLayout = this.mMakeupSingleRoot;
        ViewGroup viewGroup = null;
        if (linearLayout != null) {
            linearLayout.removeAllViews();
            this.mMakeupSingleRoot = null;
        }
        LinearLayout linearLayout2 = this.mMakeupLevelRoot;
        if (linearLayout2 != null) {
            linearLayout2.removeAllViews();
            this.mMakeupLevelRoot = null;
        }
        LinearLayout linearLayout3 = this.mMakeupSingleRoot;
        if (linearLayout3 != null && linearLayout3.getVisibility() == 0) {
            showSingleView("none");
        } else if (this.mMakeupUIStatus != 3) {
            boolean z = false;
            this.mMakeupLayoutRoot.setVisibility(0);
            final IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(CameraSettings.KEY_TS_MAKEUP_LEVEL);
            if (iconListPreference != null) {
                LinearLayout linearLayout4 = this.mMakeupLevelRoot;
                if (linearLayout4 != null) {
                    linearLayout4.removeAllViews();
                    this.mMakeupLevelRoot = null;
                }
                this.mMakeupLayoutRoot.removeAllViews();
                this.mMakeupUIStatus = 1;
                int displayRotation = CameraUtil.getDisplayRotation(this.mActivity);
                if (!CameraUtil.isDefaultToPortrait(this.mActivity)) {
                    displayRotation = (displayRotation + 90) % 360;
                }
                CharSequence[] entries = iconListPreference.getEntries();
                int[] thumbnailIds = iconListPreference.getThumbnailIds();
                Display defaultDisplay = ((WindowManager) this.mActivity.getSystemService("window")).getDefaultDisplay();
                int width = defaultDisplay.getWidth();
                int height = defaultDisplay.getHeight();
                Resources resources = this.mActivity.getResources();
                int dimension = (int) resources.getDimension(C0905R.dimen.tsmakeup_mode_paddingBottom);
                int dimension2 = (int) resources.getDimension(C0905R.dimen.tsmakeup_mode_level_size);
                StringBuilder sb = new StringBuilder();
                sb.append("TsMakeupManager.showMakeupView(): rotation is ");
                sb.append(displayRotation);
                sb.append(", WH is (");
                sb.append(width);
                sb.append(", ");
                sb.append(height);
                sb.append("), margin is ");
                sb.append(dimension);
                sb.append(", levelBgSize is ");
                sb.append(dimension2);
                Log.d(TAG, sb.toString());
                boolean z2 = displayRotation == 0 || displayRotation == 180;
                if (z2) {
                    int i2 = width;
                    i = C0905R.layout.ts_makeup_level_view_port;
                    height = i2;
                } else {
                    i = 2131361870;
                }
                int length = height / entries.length;
                LayoutInflater layoutInflater = (LayoutInflater) this.mActivity.getSystemService("layout_inflater");
                LinearLayout linearLayout5 = (LinearLayout) layoutInflater.inflate(i, null, false);
                this.mMakeupLevelRoot = linearLayout5;
                this.mUI.setMakeupMenuLayout(linearLayout5);
                if (z2) {
                    layoutParams = new LayoutParams(length, length);
                    layoutParams.gravity = 16;
                } else {
                    layoutParams = new LayoutParams(length, length);
                    layoutParams.gravity = 1;
                }
                if (displayRotation == 0) {
                    layoutParams2 = new RelativeLayout.LayoutParams(height, dimension2);
                    layoutParams2.addRule(12);
                } else if (displayRotation == 90) {
                    layoutParams2 = new RelativeLayout.LayoutParams(dimension2, height);
                    layoutParams2.addRule(11);
                } else if (displayRotation == 180) {
                    layoutParams2 = new RelativeLayout.LayoutParams(height, dimension2);
                    layoutParams2.addRule(10);
                } else if (displayRotation == 270) {
                    layoutParams2 = new RelativeLayout.LayoutParams(dimension2, height);
                    layoutParams2.addRule(9);
                } else {
                    layoutParams2 = null;
                }
                final View[] viewArr = new View[entries.length];
                int currentIndex = iconListPreference.getCurrentIndex();
                final int i3 = 0;
                while (i3 < entries.length) {
                    RotateLayout rotateLayout = (RotateLayout) layoutInflater.inflate(C0905R.layout.ts_makeup_item_view, viewGroup, z);
                    ImageView imageView = (ImageView) rotateLayout.findViewById(C0905R.C0907id.image);
                    TextView textView = (TextView) rotateLayout.findViewById(C0905R.C0907id.label);
                    rotateLayout.setOnTouchListener(new OnTouchListener() {
                        private long startTime;

                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (motionEvent.getAction() == 0) {
                                this.startTime = System.currentTimeMillis();
                            } else if (motionEvent.getAction() == 1 && System.currentTimeMillis() - this.startTime < 200) {
                                iconListPreference.setValueIndex(i3);
                                TsMakeupManager.this.changeMakeupIcon(iconListPreference.getValue());
                                TsMakeupManager.this.mMakeupLevelListener.onMakeupLevel(iconListPreference.getKey(), iconListPreference.getValue());
                                for (View selected : viewArr) {
                                    selected.setSelected(false);
                                }
                                view.findViewById(C0905R.C0907id.image).setSelected(true);
                                TsMakeupManager.this.showSingleView(iconListPreference.getValue());
                                TsMakeupManager.this.mUI.adjustOrientation();
                                if (!iconListPreference.getValue().equalsIgnoreCase("off")) {
                                    RotateTextToast.makeText((Activity) TsMakeupManager.this.mActivity, (CharSequence) TsMakeupManager.this.mActivity.getString(C0905R.string.text_tsmakeup_beautify_toast), 0).show();
                                }
                            }
                            return true;
                        }
                    });
                    View findViewById = rotateLayout.findViewById(C0905R.C0907id.image);
                    viewArr[i3] = findViewById;
                    if (i3 == currentIndex) {
                        findViewById.setSelected(true);
                    }
                    imageView.setImageResource(thumbnailIds[i3]);
                    textView.setText(entries[i3]);
                    linearLayout5.addView(rotateLayout, layoutParams);
                    i3++;
                    viewGroup = null;
                    z = false;
                }
                this.mMakeupLayoutRoot.addView(linearLayout5, layoutParams2);
            }
        }
    }

    /* access modifiers changed from: private */
    public void showSingleView(String str) {
        if ("none".equals(str)) {
            LinearLayout linearLayout = this.mMakeupSingleRoot;
            if (linearLayout != null) {
                linearLayout.removeAllViews();
                this.mMakeupSingleRoot = null;
            }
            this.mMakeupLayoutRoot.removeAllViews();
            int displayRotation = CameraUtil.getDisplayRotation(this.mActivity);
            if (!CameraUtil.isDefaultToPortrait(this.mActivity)) {
                displayRotation = (displayRotation + 90) % 360;
            }
            Display defaultDisplay = ((WindowManager) this.mActivity.getSystemService("window")).getDefaultDisplay();
            int width = defaultDisplay.getWidth();
            int height = defaultDisplay.getHeight();
            Resources resources = this.mActivity.getResources();
            int dimension = (int) resources.getDimension(C0905R.dimen.tsmakeup_mode_paddingBottom);
            int dimension2 = (int) resources.getDimension(C0905R.dimen.tsmakeup_mode_level_size);
            StringBuilder sb = new StringBuilder();
            sb.append("TsMakeupManager.showSingleView(): rotation is ");
            sb.append(displayRotation);
            sb.append(", WH is (");
            sb.append(width);
            sb.append(", ");
            sb.append(height);
            sb.append("), margin is ");
            sb.append(dimension);
            sb.append(", levelBgSize is ");
            sb.append(dimension2);
            Log.d(TAG, sb.toString());
            final LinearLayout linearLayout2 = (LinearLayout) ((LayoutInflater) this.mActivity.getSystemService("layout_inflater")).inflate(C0905R.layout.ts_makeup_single_level_view_port, null, false);
            this.mMakeupSingleRoot = linearLayout2;
            this.mUI.setMakeupMenuLayout(linearLayout2);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, -2);
            layoutParams.addRule(12);
            this.mMakeupLayoutRoot.addView(linearLayout2, layoutParams);
            final SeekBar seekBar = (SeekBar) linearLayout2.findViewById(C0905R.C0907id.seekbar_makeup_level);
            seekBar.setOnSeekBarChangeListener(this);
            setSingleView(linearLayout2);
            this.mMode = 0;
            linearLayout2.findViewById(C0905R.C0907id.id_layout_makeup_back).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    TsMakeupManager.this.mMakeupSingleRoot.removeAllViews();
                    TsMakeupManager.this.mMakeupLayoutRoot.removeView(TsMakeupManager.this.mMakeupSingleRoot);
                    TsMakeupManager.this.mMakeupSingleRoot = null;
                    TsMakeupManager.this.mSingleSelectedIndex = 0;
                    TsMakeupManager.this.mMode = 0;
                    TsMakeupManager.this.showMakeupView();
                    TsMakeupManager.this.mUI.adjustOrientation();
                }
            });
            linearLayout2.findViewById(C0905R.C0907id.id_layout_makeup_whiten).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (TsMakeupManager.this.mMode == 1) {
                        seekBar.setVisibility(8);
                        TsMakeupManager.this.mMode = 0;
                        return;
                    }
                    TsMakeupManager.this.mSingleSelectedIndex = 1;
                    seekBar.setVisibility(0);
                    seekBar.setProgress(TsMakeupManager.this.getPrefValue(CameraSettings.KEY_TS_MAKEUP_LEVEL_WHITEN));
                    TsMakeupManager.this.mMode = 1;
                    TsMakeupManager.this.setSingleView(linearLayout2);
                }
            });
            linearLayout2.findViewById(C0905R.C0907id.id_layout_makeup_clean).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (TsMakeupManager.this.mMode == 2) {
                        seekBar.setVisibility(8);
                        TsMakeupManager.this.mMode = 0;
                        return;
                    }
                    TsMakeupManager.this.mSingleSelectedIndex = 2;
                    seekBar.setVisibility(0);
                    seekBar.setProgress(TsMakeupManager.this.getPrefValue(CameraSettings.KEY_TS_MAKEUP_LEVEL_CLEAN));
                    TsMakeupManager.this.mMode = 2;
                    TsMakeupManager.this.setSingleView(linearLayout2);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void setSingleView(LinearLayout linearLayout) {
        int i = this.mSingleSelectedIndex;
        if (i == 1) {
            linearLayout.findViewById(C0905R.C0907id.id_iv_makeup_whiten).setSelected(true);
            linearLayout.findViewById(C0905R.C0907id.id_iv_makeup_clean).setSelected(false);
        } else if (i == 2) {
            linearLayout.findViewById(C0905R.C0907id.id_iv_makeup_whiten).setSelected(false);
            linearLayout.findViewById(C0905R.C0907id.id_iv_makeup_clean).setSelected(true);
        }
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        setSeekbarValue(seekBar.getProgress());
    }

    private void setSeekbarValue(int i) {
        String str = this.mMode == 2 ? CameraSettings.KEY_TS_MAKEUP_LEVEL_CLEAN : CameraSettings.KEY_TS_MAKEUP_LEVEL_WHITEN;
        StringBuilder sb = new StringBuilder();
        sb.append("TsMakeupManager.onStopTrackingTouch(): value is ");
        sb.append(i);
        sb.append(", key is ");
        sb.append(str);
        Log.d(TAG, sb.toString());
        setEffectValue(str, String.valueOf(i));
    }

    private void setEffectValue(String str, String str2) {
        ListPreference findPreference = this.mPreferenceGroup.findPreference(str);
        if (findPreference != null) {
            findPreference.setMakeupSeekBarValue(str2);
            this.mMakeupLevelListener.onMakeupLevel(str, str2);
        }
    }

    /* access modifiers changed from: private */
    public int getPrefValue(String str) {
        String value = this.mPreferenceGroup.findPreference(str).getValue();
        StringBuilder sb = new StringBuilder();
        sb.append("TsMakeupManager.getPrefValue(): value is ");
        sb.append(value);
        sb.append(", key is ");
        sb.append(str);
        Log.d(TAG, sb.toString());
        if (TextUtils.isEmpty(value)) {
            value = this.mActivity.getString(C0905R.string.pref_camera_tsmakeup_level_default);
        }
        return Integer.parseInt(value);
    }
}
