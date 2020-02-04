package com.android.camera;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
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
import android.view.ViewGroup.LayoutParams;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.camera.p004ui.CameraControls;
import com.android.camera.p004ui.ListMenu;
import com.android.camera.p004ui.ListMenu.Listener;
import com.android.camera.p004ui.ListSubMenu;
import com.android.camera.p004ui.RotateLayout;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.p004ui.TimeIntervalPopup;
import com.android.camera.util.CameraUtil;
import com.asus.scenedetectlib.BuildConfig;
import java.util.Locale;
import org.codeaurora.snapcam.C0905R;

public class VideoMenu extends MenuController implements Listener, ListSubMenu.Listener, TimeIntervalPopup.Listener {
    private static final int ANIMATION_DURATION = 300;
    private static final int CLICK_THRESHOLD = 200;
    private static final int MODE_FILTER = 1;
    private static final boolean PERSIST_4K_NO_LIMIT = SystemProperties.getBoolean("persist.camcorder.4k.nolimit", false);
    private static final int POPUP_FIRST_LEVEL = 1;
    private static final int POPUP_IN_ANIMATION_FADE = 4;
    private static final int POPUP_IN_ANIMATION_SLIDE = 3;
    private static final int POPUP_NONE = 0;
    private static final int POPUP_SECOND_LEVEL = 2;
    private static final int PREVIEW_MENU_IN_ANIMATION = 1;
    private static final int PREVIEW_MENU_NONE = 0;
    private static final int PREVIEW_MENU_ON = 2;
    private static String TAG = "VideoMenu";
    private CameraActivity mActivity;
    private View mFilterModeSwitcher;
    private View mFrontBackSwitcher;
    private boolean mIsVideoCDSUpdated = false;
    private boolean mIsVideoTNREnabled = false;
    private ListMenu mListMenu;
    private ListSubMenu mListSubMenu;
    private String[] mOtherKeys1;
    private String[] mOtherKeys2;
    /* access modifiers changed from: private */
    public int mPopupStatus;
    private String mPrevSavedVideoCDS;
    private View mPreviewMenu;
    /* access modifiers changed from: private */
    public int mPreviewMenuStatus;
    private int mSceneStatus;
    /* access modifiers changed from: private */
    public VideoUI mUI;
    /* access modifiers changed from: private */
    public int previewMenuSize;

    public VideoMenu(CameraActivity cameraActivity, VideoUI videoUI) {
        super(cameraActivity);
        this.mUI = videoUI;
        this.mActivity = cameraActivity;
        this.mFrontBackSwitcher = videoUI.getRootView().findViewById(C0905R.C0907id.front_back_switcher);
        this.mFilterModeSwitcher = videoUI.getRootView().findViewById(C0905R.C0907id.filter_mode_switcher);
    }

    public void initialize(PreferenceGroup preferenceGroup) {
        super.initialize(preferenceGroup);
        this.mListMenu = null;
        this.mListSubMenu = null;
        this.mPopupStatus = 0;
        this.mPreviewMenuStatus = 0;
        initFilterModeButton(this.mFilterModeSwitcher);
        this.mOtherKeys1 = new String[]{CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE, "pref_video_quality_key", "pref_camera_video_duration_key", "pref_camera_recordlocation_key", "pref_camera_savepath_key", "pref_camera_whitebalance_key", CameraSettings.KEY_VIDEO_HIGH_FRAME_RATE, "pref_camera_dis_key"};
        this.mOtherKeys2 = new String[]{CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE, "pref_video_quality_key", "pref_camera_video_duration_key", "pref_camera_recordlocation_key", "pref_camera_savepath_key", "pref_camera_whitebalance_key", "pref_camera_facedetection_key", CameraSettings.KEY_VIDEO_HIGH_FRAME_RATE, CameraSettings.KEY_SEE_MORE, "pref_camera_noise_reduction_key", "pref_camera_dis_key", CameraSettings.KEY_VIDEO_EFFECT, CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL, "pref_camera_videoencoder_key", "pref_camera_audioencoder_key", CameraSettings.KEY_VIDEO_HDR, CameraSettings.KEY_POWER_MODE, "pref_camera_video_rotation_key", CameraSettings.KEY_VIDEO_CDS_MODE, CameraSettings.KEY_VIDEO_TNR_MODE, CameraSettings.KEY_VIDEO_SNAPSHOT_SIZE, CameraSettings.KEY_ZOOM};
        initSwitchItem(CameraSettings.KEY_CAMERA_ID, this.mFrontBackSwitcher);
    }

    public boolean handleBackKey() {
        if (this.mPreviewMenuStatus == 2) {
            animateSlideOut(this.mPreviewMenu);
            return true;
        }
        int i = this.mPopupStatus;
        if (i == 0) {
            return false;
        }
        if (i == 1) {
            animateSlideOut(this.mListMenu, 1);
        } else if (i == 2) {
            animateFadeOut(this.mListSubMenu, 2);
            this.mListMenu.resetHighlight();
        }
        return true;
    }

    public void closeSceneMode() {
        this.mUI.removeSceneModeMenu();
    }

    public void tryToCloseSubList() {
        ListMenu listMenu = this.mListMenu;
        if (listMenu != null) {
            listMenu.resetHighlight();
        }
        if (this.mPopupStatus == 2) {
            this.mUI.dismissLevel2();
            this.mPopupStatus = 1;
        }
    }

    private void animateFadeOut(ListView listView, final int i) {
        if (listView != null && this.mPopupStatus != 4) {
            this.mPopupStatus = 4;
            ViewPropertyAnimator animate = listView.animate();
            animate.alpha(0.0f).setDuration(300);
            animate.setListener(new AnimatorListener() {
                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    int i = i;
                    if (i == 1) {
                        VideoMenu.this.mUI.dismissLevel1();
                        VideoMenu.this.initializePopup();
                        VideoMenu.this.mPopupStatus = 0;
                        VideoMenu.this.mUI.cleanupListview();
                    } else if (i == 2) {
                        VideoMenu.this.mUI.dismissLevel2();
                        VideoMenu.this.mPopupStatus = 1;
                    }
                }

                public void onAnimationCancel(Animator animator) {
                    int i = i;
                    if (i == 1) {
                        VideoMenu.this.mUI.dismissLevel1();
                        VideoMenu.this.initializePopup();
                        VideoMenu.this.mPopupStatus = 0;
                        VideoMenu.this.mUI.cleanupListview();
                    } else if (i == 2) {
                        VideoMenu.this.mUI.dismissLevel2();
                        VideoMenu.this.mPopupStatus = 1;
                    }
                }
            });
            animate.start();
        }
    }

    private void animateSlideOut(ListView listView, final int i) {
        if (listView != null && this.mPopupStatus != 3) {
            this.mPopupStatus = 3;
            ViewPropertyAnimator animate = listView.animate();
            if (1 == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
                int orientation = this.mUI.getOrientation();
                if (orientation == 0) {
                    animate.translationXBy((float) listView.getWidth());
                } else if (orientation == 90) {
                    animate.translationYBy((float) (listView.getHeight() * -2));
                } else if (orientation == 180) {
                    animate.translationXBy((float) (listView.getWidth() * -2));
                } else if (orientation == 270) {
                    animate.translationYBy((float) listView.getHeight());
                }
            } else {
                int orientation2 = this.mUI.getOrientation();
                if (orientation2 == 0) {
                    animate.translationXBy((float) (-listView.getWidth()));
                } else if (orientation2 == 90) {
                    animate.translationYBy((float) (listView.getHeight() * 2));
                } else if (orientation2 == 180) {
                    animate.translationXBy((float) (listView.getWidth() * 2));
                } else if (orientation2 == 270) {
                    animate.translationYBy((float) (-listView.getHeight()));
                }
            }
            animate.setListener(new AnimatorListener() {
                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    int i = i;
                    if (i == 1) {
                        VideoMenu.this.mUI.dismissLevel1();
                        VideoMenu.this.initializePopup();
                        VideoMenu.this.mPopupStatus = 0;
                        VideoMenu.this.mUI.cleanupListview();
                    } else if (i == 2) {
                        VideoMenu.this.mUI.dismissLevel2();
                        VideoMenu.this.mPopupStatus = 1;
                    }
                }

                public void onAnimationCancel(Animator animator) {
                    int i = i;
                    if (i == 1) {
                        VideoMenu.this.mUI.dismissLevel1();
                        VideoMenu.this.initializePopup();
                        VideoMenu.this.mPopupStatus = 0;
                        VideoMenu.this.mUI.cleanupListview();
                    } else if (i == 2) {
                        VideoMenu.this.mUI.dismissLevel2();
                        VideoMenu.this.mPopupStatus = 1;
                    }
                }
            });
            animate.setDuration(300).start();
        }
    }

    public void animateFadeIn(ListView listView) {
        ViewPropertyAnimator animate = listView.animate();
        animate.alpha(0.85f).setDuration(300);
        animate.start();
    }

    public void animateSlideIn(View view, int i, boolean z) {
        int orientation = this.mUI.getOrientation();
        if (!z) {
            orientation = 0;
        }
        ViewPropertyAnimator animate = view.animate();
        if (1 == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
            if (orientation == 0) {
                float x = view.getX();
                view.setX(-(x - ((float) i)));
                animate.translationX(x);
            } else if (orientation == 90) {
                float y = view.getY();
                view.setY(-(((float) i) + y));
                animate.translationY(y);
            } else if (orientation == 180) {
                float x2 = view.getX();
                view.setX(-(((float) i) + x2));
                animate.translationX(x2);
            } else if (orientation == 270) {
                float y2 = view.getY();
                view.setY(-(y2 - ((float) i)));
                animate.translationY(y2);
            }
        } else if (orientation == 0) {
            float x3 = view.getX();
            view.setX(x3 - ((float) i));
            animate.translationX(x3);
        } else if (orientation == 90) {
            float y3 = view.getY();
            view.setY(((float) i) + y3);
            animate.translationY(y3);
        } else if (orientation == 180) {
            float x4 = view.getX();
            view.setX(((float) i) + x4);
            animate.translationX(x4);
        } else if (orientation == 270) {
            float y4 = view.getY();
            view.setY(y4 - ((float) i));
            animate.translationY(y4);
        }
        animate.setDuration(300).start();
    }

    public void animateSlideOutPreviewMenu() {
        View view = this.mPreviewMenu;
        if (view != null) {
            animateSlideOut(view);
        }
    }

    private void animateSlideOut(View view) {
        if (view != null && this.mPreviewMenuStatus != 1) {
            this.mPreviewMenuStatus = 1;
            ViewPropertyAnimator animate = view.animate();
            if (1 == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
                animate.translationXBy((float) view.getWidth()).setDuration(300);
            } else {
                animate.translationXBy((float) (-view.getWidth())).setDuration(300);
            }
            animate.setListener(new AnimatorListener() {
                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    VideoMenu.this.closeSceneMode();
                    VideoMenu.this.mPreviewMenuStatus = 0;
                }

                public void onAnimationCancel(Animator animator) {
                    VideoMenu.this.closeSceneMode();
                    VideoMenu.this.mPreviewMenuStatus = 0;
                }
            });
            animate.setDuration(300).start();
        }
    }

    public boolean isOverMenu(MotionEvent motionEvent) {
        int i = this.mPopupStatus;
        if (i == 0 || i == 3 || i == 4 || this.mUI.getMenuLayout() == null) {
            return false;
        }
        Rect rect = new Rect();
        this.mUI.getMenuLayout().getChildAt(0).getHitRect(rect);
        return rect.contains((int) motionEvent.getX(), (int) motionEvent.getY());
    }

    public boolean isOverPreviewMenu(MotionEvent motionEvent) {
        if (this.mPreviewMenuStatus != 2 || this.mUI.getPreviewMenuLayout() == null) {
            return false;
        }
        Rect rect = new Rect();
        this.mUI.getPreviewMenuLayout().getChildAt(0).getHitRect(rect);
        rect.top += (int) this.mUI.getPreviewMenuLayout().getY();
        rect.bottom += (int) this.mUI.getPreviewMenuLayout().getY();
        return rect.contains((int) motionEvent.getX(), (int) motionEvent.getY());
    }

    public boolean isMenuBeingShown() {
        return this.mPopupStatus != 0;
    }

    public boolean isMenuBeingAnimated() {
        int i = this.mPopupStatus;
        return i == 3 || i == 4;
    }

    public boolean isPreviewMenuBeingShown() {
        return this.mPreviewMenuStatus == 2;
    }

    public boolean isPreviewMenuBeingAnimated() {
        return this.mPreviewMenuStatus == 1;
    }

    public boolean sendTouchToPreviewMenu(MotionEvent motionEvent) {
        return this.mUI.sendTouchToPreviewMenu(motionEvent);
    }

    public boolean sendTouchToMenu(MotionEvent motionEvent) {
        return this.mUI.sendTouchToMenu(motionEvent);
    }

    public void initSwitchItem(final String str, View view) {
        int i;
        IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(str);
        if (iconListPreference != null) {
            int[] largeIconIds = iconListPreference.getLargeIconIds();
            int findIndexOfValue = iconListPreference.findIndexOfValue(iconListPreference.getValue());
            if (iconListPreference.getUseSingleIcon() || largeIconIds == null) {
                i = iconListPreference.getSingleIcon();
            } else if (findIndexOfValue != -1) {
                i = largeIconIds[findIndexOfValue];
            } else {
                return;
            }
            ((ImageView) view).setImageResource(i);
            this.mPreferences.add(iconListPreference);
            this.mPreferenceMap.put(iconListPreference, view);
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    IconListPreference iconListPreference = (IconListPreference) VideoMenu.this.mPreferenceGroup.findPreference(str);
                    if (iconListPreference != null) {
                        int findIndexOfValue = (iconListPreference.findIndexOfValue(iconListPreference.getValue()) + 1) % iconListPreference.getEntryValues().length;
                        iconListPreference.setValueIndex(findIndexOfValue);
                        ((ImageView) view).setImageResource(iconListPreference.getLargeIconIds()[findIndexOfValue]);
                        if (str.equals(CameraSettings.KEY_CAMERA_ID)) {
                            VideoMenu.this.mListener.onCameraPickerClicked(findIndexOfValue);
                        }
                        VideoMenu.this.reloadPreference(iconListPreference);
                        VideoMenu.this.onSettingChanged(iconListPreference);
                    }
                }
            });
        }
    }

    public void initFilterModeButton(View view) {
        view.setVisibility(4);
        IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(CameraSettings.KEY_COLOR_EFFECT);
        if (iconListPreference != null && iconListPreference.getValue() != null) {
            changeFilterModeControlIcon(iconListPreference.getValue());
            view.setVisibility(0);
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    VideoMenu.this.addFilterMode();
                    ViewGroup previewMenuLayout = VideoMenu.this.mUI.getPreviewMenuLayout();
                    if (previewMenuLayout != null) {
                        View childAt = previewMenuLayout.getChildAt(0);
                        VideoMenu.this.mUI.adjustOrientation();
                        VideoMenu videoMenu = VideoMenu.this;
                        videoMenu.animateSlideIn(childAt, videoMenu.previewMenuSize, false);
                    }
                }
            });
        }
    }

    public void addModeBack() {
        if (this.mSceneStatus == 1) {
            addFilterMode();
        }
    }

    public void addFilterMode() {
        int i;
        final IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(CameraSettings.KEY_COLOR_EFFECT);
        if (iconListPreference != null) {
            int displayRotation = CameraUtil.getDisplayRotation(this.mActivity);
            if (!CameraUtil.isDefaultToPortrait(this.mActivity)) {
                displayRotation = (displayRotation + 90) % 360;
            }
            Display defaultDisplay = ((WindowManager) this.mActivity.getSystemService("window")).getDefaultDisplay();
            CharSequence[] entries = iconListPreference.getEntries();
            Resources resources = this.mActivity.getResources();
            int dimension = (int) (resources.getDimension(C0905R.dimen.filter_mode_height) + (resources.getDimension(C0905R.dimen.filter_mode_padding) * 2.0f) + 1.0f);
            int dimension2 = (int) (resources.getDimension(C0905R.dimen.filter_mode_width) + (resources.getDimension(C0905R.dimen.filter_mode_padding) * 2.0f) + 1.0f);
            boolean z = false;
            boolean z2 = displayRotation == 0 || displayRotation == 180;
            if (z2) {
                i = 2131361874;
            } else {
                int i2 = dimension;
                i = C0905R.layout.horiz_grid;
                dimension2 = i2;
            }
            this.previewMenuSize = dimension2;
            this.mUI.hideUI();
            this.mPreviewMenuStatus = 2;
            this.mSceneStatus = 1;
            int[] thumbnailIds = iconListPreference.getThumbnailIds();
            LayoutInflater layoutInflater = (LayoutInflater) this.mActivity.getSystemService("layout_inflater");
            FrameLayout frameLayout = (FrameLayout) layoutInflater.inflate(i, null, false);
            this.mUI.dismissSceneModeMenu();
            LinearLayout linearLayout = new LinearLayout(this.mActivity);
            this.mUI.setPreviewMenuLayout(linearLayout);
            if (z2) {
                linearLayout.setLayoutParams(new LayoutParams(dimension2, -1));
                ((ViewGroup) this.mUI.getRootView()).addView(linearLayout);
            } else {
                linearLayout.setLayoutParams(new LayoutParams(-1, dimension2));
                ((ViewGroup) this.mUI.getRootView()).addView(linearLayout);
                linearLayout.setY((float) (defaultDisplay.getHeight() - dimension2));
            }
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            LinearLayout linearLayout2 = (LinearLayout) frameLayout.findViewById(C0905R.C0907id.layout);
            final View[] viewArr = new View[entries.length];
            int currentIndex = iconListPreference.getCurrentIndex();
            final int i3 = 0;
            while (i3 < entries.length) {
                RotateLayout rotateLayout = (RotateLayout) layoutInflater.inflate(C0905R.layout.filter_mode_view, null, z);
                ImageView imageView = (ImageView) rotateLayout.findViewById(C0905R.C0907id.image);
                rotateLayout.setOnTouchListener(new OnTouchListener() {
                    private long startTime;

                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == 0) {
                            this.startTime = System.currentTimeMillis();
                        } else if (motionEvent.getAction() == 1 && System.currentTimeMillis() - this.startTime < 200) {
                            iconListPreference.setValueIndex(i3);
                            VideoMenu.this.changeFilterModeControlIcon(iconListPreference.getValue());
                            for (View background : viewArr) {
                                background.setBackground(null);
                            }
                            ((ImageView) view.findViewById(C0905R.C0907id.image)).setBackgroundColor(-13388315);
                            VideoMenu.this.onSettingChanged(iconListPreference);
                        }
                        return true;
                    }
                });
                viewArr[i3] = imageView;
                if (i3 == currentIndex) {
                    imageView.setBackgroundColor(-13388315);
                }
                TextView textView = (TextView) rotateLayout.findViewById(C0905R.C0907id.label);
                imageView.setImageResource(thumbnailIds[i3]);
                textView.setText(entries[i3]);
                linearLayout2.addView(rotateLayout);
                i3++;
                z = false;
            }
            linearLayout.addView(frameLayout);
            this.mPreviewMenu = frameLayout;
        }
    }

    /* access modifiers changed from: private */
    public void changeFilterModeControlIcon(String str) {
        if (!str.equals(BuildConfig.FLAVOR)) {
            IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(CameraSettings.KEY_FILTER_MODE);
            iconListPreference.setValue(str.equalsIgnoreCase("none") ? TsMakeupManager.MAKEUP_OFF : TsMakeupManager.MAKEUP_ON);
            ((ImageView) this.mFilterModeSwitcher).setImageResource(iconListPreference.getLargeIconIds()[iconListPreference.getCurrentIndex()]);
        }
    }

    public void openFirstLevel() {
        if (!isMenuBeingShown() && !CameraControls.isAnimating()) {
            if (this.mListMenu == null || this.mPopupStatus != 1) {
                initializePopup();
                this.mPopupStatus = 1;
            }
            this.mUI.showPopup(this.mListMenu, 1, true);
        }
    }

    public void setPreference(String str, String str2) {
        ListPreference findPreference = this.mPreferenceGroup.findPreference(str);
        if (findPreference != null && !str2.equals(findPreference.getValue())) {
            findPreference.setValue(str2);
            reloadPreferences();
        }
    }

    private void overridePreferenceAccessibility() {
        overrideMenuForLocation();
        overrideMenuFor4K();
        overrideMenuForCDSMode();
        overrideMenuForSeeMore();
        overrideMenuForVideoHighFrameRate();
    }

    private void overrideMenuForLocation() {
        if (this.mActivity.isSecureCamera()) {
            this.mListMenu.setPreferenceEnabled("pref_camera_recordlocation_key", false);
        }
    }

    private void overrideMenuFor4K() {
        if (this.mUI.is4KEnabled() && !PERSIST_4K_NO_LIMIT) {
            String str = "pref_camera_dis_key";
            this.mListMenu.setPreferenceEnabled(str, false);
            this.mListMenu.overrideSettings(str, "disable");
            ListMenu listMenu = this.mListMenu;
            String str2 = CameraSettings.KEY_SEE_MORE;
            listMenu.setPreferenceEnabled(str2, false);
            this.mListMenu.overrideSettings(str2, this.mActivity.getString(C0905R.string.pref_camera_see_more_value_off));
        }
    }

    private void overrideMenuForSeeMore() {
        PreferenceGroup preferenceGroup = this.mPreferenceGroup;
        if (preferenceGroup == null) {
            Log.d(TAG, "overrideMenuForSeeMore mPreferenceGroup is null");
            return;
        }
        ListPreference findPreference = preferenceGroup.findPreference(CameraSettings.KEY_SEE_MORE);
        if (!(findPreference == null || findPreference.getValue() == null || !findPreference.getValue().equals(RecordLocationPreference.VALUE_ON))) {
            ListMenu listMenu = this.mListMenu;
            String str = CameraSettings.KEY_VIDEO_CDS_MODE;
            listMenu.setPreferenceEnabled(str, false);
            ListMenu listMenu2 = this.mListMenu;
            String str2 = CameraSettings.KEY_VIDEO_TNR_MODE;
            listMenu2.setPreferenceEnabled(str2, false);
            String str3 = "pref_camera_noise_reduction_key";
            this.mListMenu.setPreferenceEnabled(str3, false);
            this.mListMenu.overrideSettings(str, this.mActivity.getString(C0905R.string.pref_camera_video_cds_value_off), str2, this.mActivity.getString(C0905R.string.pref_camera_video_tnr_value_off), str3, this.mActivity.getString(C0905R.string.pref_camera_noise_reduction_value_off));
        }
    }

    private void overrideMenuForCDSMode() {
        PreferenceGroup preferenceGroup = this.mPreferenceGroup;
        if (preferenceGroup == null) {
            Log.d(TAG, "overrideMenuForCDSMode mPreferenceGroup is null");
            return;
        }
        ListPreference findPreference = preferenceGroup.findPreference(CameraSettings.KEY_VIDEO_TNR_MODE);
        PreferenceGroup preferenceGroup2 = this.mPreferenceGroup;
        String str = CameraSettings.KEY_VIDEO_CDS_MODE;
        ListPreference findPreference2 = preferenceGroup2.findPreference(str);
        String str2 = null;
        String value = findPreference != null ? findPreference.getValue() : null;
        if (findPreference2 != null) {
            str2 = findPreference2.getValue();
        }
        if (this.mPrevSavedVideoCDS == null && str2 != null) {
            this.mPrevSavedVideoCDS = str2;
        }
        if (value != null && !value.equals("off")) {
            this.mListMenu.setPreferenceEnabled(str, false);
            this.mListMenu.overrideSettings(str, this.mActivity.getString(C0905R.string.pref_camera_video_cds_value_off));
            this.mIsVideoTNREnabled = true;
            if (!this.mIsVideoCDSUpdated) {
                if (str2 != null) {
                    this.mPrevSavedVideoCDS = str2;
                }
                this.mIsVideoCDSUpdated = true;
            }
        } else if (value != null) {
            this.mListMenu.setPreferenceEnabled(str, true);
            if (this.mIsVideoTNREnabled) {
                this.mListMenu.overrideSettings(str, this.mPrevSavedVideoCDS);
                this.mIsVideoTNREnabled = false;
                this.mIsVideoCDSUpdated = false;
            }
        }
    }

    private void overrideMenuForVideoHighFrameRate() {
        String str;
        String str2;
        String str3;
        int i;
        PreferenceGroup preferenceGroup = this.mPreferenceGroup;
        if (preferenceGroup == null) {
            Log.d(TAG, "overrideMenuForVideoHighFrameRate mPreferenceGroup is null");
            return;
        }
        ListPreference findPreference = preferenceGroup.findPreference("pref_camera_dis_key");
        ListPreference findPreference2 = this.mPreferenceGroup.findPreference(CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL);
        ListPreference findPreference3 = this.mPreferenceGroup.findPreference(CameraSettings.KEY_VIDEO_HDR);
        if (findPreference == null) {
            str = "disable";
        } else {
            str = findPreference.getValue();
        }
        String str4 = "off";
        if (findPreference3 == null) {
            str2 = str4;
        } else {
            str2 = findPreference3.getValue();
        }
        int parseInt = Integer.parseInt(findPreference2.getValue());
        int i2 = SystemProperties.getInt("persist.camcorder.eis.maxfps", 30);
        PreferenceGroup preferenceGroup2 = this.mPreferenceGroup;
        String str5 = CameraSettings.KEY_VIDEO_HIGH_FRAME_RATE;
        ListPreference findPreference4 = preferenceGroup2.findPreference(str5);
        if (findPreference4 == null) {
            str3 = "     ";
        } else {
            str3 = findPreference4.getValue();
        }
        boolean equals = "hfr".equals(str3.substring(0, 3));
        boolean equals2 = "hsr".equals(str3.substring(0, 3));
        if (equals || equals2) {
            i = Integer.parseInt(str3.substring(3));
        } else {
            i = 0;
        }
        if ((str.equals("enable") && i > i2) || !str2.equals(str4) || parseInt != 0) {
            this.mListMenu.setPreferenceEnabled(str5, false);
        }
    }

    public void overrideSettings(String... strArr) {
        super.overrideSettings(strArr);
        if (this.mListMenu == null) {
            initializePopup();
        } else {
            overridePreferenceAccessibility();
        }
        this.mListMenu.overrideSettings(strArr);
    }

    public void onListPrefChanged(ListPreference listPreference) {
        onSettingChanged(listPreference);
        closeView();
    }

    /* access modifiers changed from: protected */
    public void initializePopup() {
        ListMenu listMenu = (ListMenu) ((LayoutInflater) this.mActivity.getSystemService("layout_inflater")).inflate(C0905R.layout.list_menu, null, false);
        listMenu.setSettingChangedListener(this);
        String[] strArr = this.mOtherKeys1;
        if (this.mActivity.isDeveloperMenuEnabled()) {
            strArr = this.mOtherKeys2;
        }
        if (strArr != null) {
            listMenu.initialize(this.mPreferenceGroup, strArr);
        }
        this.mListMenu = listMenu;
        overridePreferenceAccessibility();
        overrideMenuForVideoHighFrameRate();
    }

    public void popupDismissed(boolean z) {
        if (this.mPopupStatus == 2) {
            initializePopup();
            this.mPopupStatus = 1;
            if (z) {
                this.mUI.showPopup(this.mListMenu, 1, false);
                return;
            }
            return;
        }
        initializePopup();
    }

    public void hideUI() {
        this.mFrontBackSwitcher.setVisibility(4);
        this.mFilterModeSwitcher.setVisibility(4);
    }

    public void showUI() {
        this.mFrontBackSwitcher.setVisibility(0);
        if (((IconListPreference) this.mPreferenceGroup.findPreference(CameraSettings.KEY_FILTER_MODE)) != null) {
            this.mFilterModeSwitcher.setVisibility(0);
        }
    }

    public void onPreferenceClicked(ListPreference listPreference) {
        onPreferenceClicked(listPreference, 0);
    }

    public void onPreferenceClicked(ListPreference listPreference, int i) {
        ListSubMenu listSubMenu = (ListSubMenu) ((LayoutInflater) this.mActivity.getSystemService("layout_inflater")).inflate(C0905R.layout.list_sub_menu, null, false);
        listSubMenu.initialize(listPreference, i);
        listSubMenu.setSettingChangedListener(this);
        this.mUI.removeLevel2();
        this.mListSubMenu = listSubMenu;
        if (this.mPopupStatus == 2) {
            this.mUI.showPopup(this.mListSubMenu, 2, false);
        } else {
            this.mUI.showPopup(this.mListSubMenu, 2, true);
        }
        this.mPopupStatus = 2;
    }

    public void onListMenuTouched() {
        this.mUI.removeLevel2();
        this.mPopupStatus = 1;
    }

    public void closeAllView() {
        VideoUI videoUI = this.mUI;
        if (videoUI != null) {
            videoUI.removeLevel2();
        }
        ListMenu listMenu = this.mListMenu;
        if (listMenu != null) {
            animateSlideOut(listMenu, 1);
        }
        animateSlideOutPreviewMenu();
    }

    public void closeView() {
        VideoUI videoUI = this.mUI;
        if (videoUI != null) {
            videoUI.removeLevel2();
        }
        ListMenu listMenu = this.mListMenu;
        if (listMenu != null) {
            animateSlideOut(listMenu, 1);
        }
    }

    private static boolean notSame(ListPreference listPreference, String str, String str2) {
        return str.equals(listPreference.getKey()) && !str2.equals(listPreference.getValue());
    }

    public void onSettingChanged(ListPreference listPreference) {
        String string = this.mActivity.getString(C0905R.string.pref_video_time_lapse_frame_interval_default);
        String str = CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL;
        boolean notSame = notSame(listPreference, str, string);
        String str2 = CameraSettings.KEY_VIDEO_HIGH_FRAME_RATE;
        String str3 = "off";
        if (notSame) {
            ListPreference findPreference = this.mPreferenceGroup.findPreference(str2);
            if (findPreference != null && !str3.equals(findPreference.getValue())) {
                RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.error_app_unsupported_hfr_selection, 1).show();
            }
            setPreference(str2, str3);
        }
        if (notSame(listPreference, str2, str3)) {
            String string2 = this.mActivity.getString(C0905R.string.pref_video_time_lapse_frame_interval_default);
            ListPreference findPreference2 = this.mPreferenceGroup.findPreference(str);
            if (findPreference2 != null && !string2.equals(findPreference2.getValue())) {
                RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.error_app_unsupported_hfr_selection, 1).show();
            }
            setPreference(str, string2);
        }
        if (notSame(listPreference, "pref_camera_recordlocation_key", str3)) {
            this.mActivity.requestLocationPermission();
        }
        super.onSettingChanged(listPreference);
    }

    public int getOrientation() {
        VideoUI videoUI = this.mUI;
        if (videoUI == null) {
            return 0;
        }
        return videoUI.getOrientation();
    }
}
