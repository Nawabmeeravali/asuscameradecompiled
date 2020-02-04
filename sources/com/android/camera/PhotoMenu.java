package com.android.camera;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
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
import com.android.camera.app.CameraApp;
import com.android.camera.p004ui.CameraControls;
import com.android.camera.p004ui.CountdownTimerPopup;
import com.android.camera.p004ui.ListMenu;
import com.android.camera.p004ui.ListMenu.Listener;
import com.android.camera.p004ui.ListSubMenu;
import com.android.camera.p004ui.RotateLayout;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.util.CameraUtil;
import com.asus.scenedetectlib.BuildConfig;
import java.util.HashSet;
import java.util.Locale;
import org.codeaurora.snapcam.C0905R;
import org.codeaurora.snapcam.wrapper.ParametersWrapper;

public class PhotoMenu extends MenuController implements Listener, CountdownTimerPopup.Listener, ListSubMenu.Listener {
    private static final int ANIMATION_DURATION = 300;
    private static final int CLICK_THRESHOLD = 200;
    private static final int DEVELOPER_MENU_TOUCH_COUNT = 10;
    private static final int MAKEUP_MESSAGE_ID = 0;
    private static final int MODE_FILTER = 1;
    private static final int MODE_MAKEUP = 2;
    private static final int MODE_SCENE = 0;
    private static final int POPUP_FIRST_LEVEL = 1;
    private static final int POPUP_IN_ANIMATION_FADE = 4;
    private static final int POPUP_IN_ANIMATION_SLIDE = 3;
    private static final int POPUP_IN_MAKEUP = 5;
    private static final int POPUP_NONE = 0;
    private static final int POPUP_SECOND_LEVEL = 2;
    private static final int PREVIEW_MENU_IN_ANIMATION = 1;
    private static final int PREVIEW_MENU_NONE = 0;
    private static final int PREVIEW_MENU_ON = 2;
    /* access modifiers changed from: private */
    public static String TAG = "PhotoMenu";
    private CameraActivity mActivity;
    private View mBokehSwitcher;
    private View mCameraSwitcher;
    private View mFilterModeSwitcher;
    private View mFrontBackSwitcher;
    private MakeupHandler mHandler = new MakeupHandler();
    private View mHdrSwitcher;
    private boolean mIsCDSUpdated = false;
    private boolean mIsTNREnabled = false;
    private ListMenu mListMenu;
    private ListSubMenu mListSubMenu;
    private MakeupLevelListener mMakeupListener;
    private String[] mOtherKeys1;
    private String[] mOtherKeys2;
    /* access modifiers changed from: private */
    public int mPopupStatus;
    private String mPrevSavedCDS;
    private View mPreviewMenu;
    /* access modifiers changed from: private */
    public int mPreviewMenuStatus;
    private View mPreviewThumbnail;
    private View mSceneModeSwitcher;
    private int mSceneStatus;
    private View mSettingMenu;
    private final String mSettingOff;
    private final String mSettingOn;
    /* access modifiers changed from: private */
    public TsMakeupManager mTsMakeupManager;
    private View mTsMakeupSwitcher;
    /* access modifiers changed from: private */
    public PhotoUI mUI;
    private HashSet<View> mWasVisibleSet = new HashSet<>();
    /* access modifiers changed from: private */
    public int previewMenuSize;
    private int privateCounter = 0;

    protected class MakeupHandler extends Handler {
        protected MakeupHandler() {
        }

        public void handleMessage(Message message) {
            if (message.what == 0) {
                PhotoMenu.this.mTsMakeupManager.showMakeupView();
                PhotoMenu.this.mUI.adjustOrientation();
            }
        }
    }

    public PhotoMenu(CameraActivity cameraActivity, PhotoUI photoUI, MakeupLevelListener makeupLevelListener) {
        super(cameraActivity);
        this.mUI = photoUI;
        this.mSettingOff = cameraActivity.getString(C0905R.string.setting_off_value);
        this.mSettingOn = cameraActivity.getString(C0905R.string.setting_on_value);
        this.mActivity = cameraActivity;
        this.mFrontBackSwitcher = photoUI.getRootView().findViewById(C0905R.C0907id.front_back_switcher);
        this.mHdrSwitcher = photoUI.getRootView().findViewById(C0905R.C0907id.hdr_switcher);
        this.mTsMakeupSwitcher = photoUI.getRootView().findViewById(C0905R.C0907id.ts_makeup_switcher);
        this.mSceneModeSwitcher = photoUI.getRootView().findViewById(C0905R.C0907id.scene_mode_switcher);
        this.mBokehSwitcher = photoUI.getRootView().findViewById(C0905R.C0907id.bokeh_switcher);
        this.mFilterModeSwitcher = photoUI.getRootView().findViewById(C0905R.C0907id.filter_mode_switcher);
        this.mMakeupListener = makeupLevelListener;
        this.mSettingMenu = photoUI.getRootView().findViewById(C0905R.C0907id.menu);
        this.mCameraSwitcher = photoUI.getRootView().findViewById(C0905R.C0907id.camera_switcher);
        this.mPreviewThumbnail = photoUI.getRootView().findViewById(C0905R.C0907id.preview_thumb);
    }

    public void initialize(PreferenceGroup preferenceGroup) {
        super.initialize(preferenceGroup);
        this.mListSubMenu = null;
        this.mListMenu = null;
        this.mPopupStatus = 0;
        this.mPreviewMenuStatus = 0;
        Locale locale = this.mActivity.getResources().getConfiguration().locale;
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            TsMakeupManager tsMakeupManager = this.mTsMakeupManager;
            if (tsMakeupManager != null) {
                tsMakeupManager.removeAllViews();
                this.mTsMakeupManager = null;
            }
            if (this.mTsMakeupManager == null) {
                TsMakeupManager tsMakeupManager2 = new TsMakeupManager(this.mActivity, this, this.mUI, this.mPreferenceGroup, this.mTsMakeupSwitcher);
                this.mTsMakeupManager = tsMakeupManager2;
                this.mTsMakeupManager.setMakeupLevelListener(this.mMakeupListener);
            }
        }
        initSceneModeButton(this.mSceneModeSwitcher);
        initFilterModeButton(this.mFilterModeSwitcher);
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            initMakeupModeButton(this.mTsMakeupSwitcher);
        } else {
            this.mHdrSwitcher.setVisibility(4);
        }
        initBokehModeButton(this.mBokehSwitcher);
        this.mFrontBackSwitcher.setVisibility(4);
        if (!TsMakeupManager.HAS_TS_MAKEUP) {
            String str = CameraSettings.KEY_CAMERA_HDR;
            if (preferenceGroup.findPreference(str) != null) {
                this.mHdrSwitcher.setVisibility(0);
                initSwitchItem(str, this.mHdrSwitcher);
            } else {
                this.mHdrSwitcher.setVisibility(4);
            }
        }
        this.mOtherKeys1 = new String[]{"pref_selfie_flash_key", CameraSettings.KEY_FLASH_MODE, "pref_camera_recordlocation_key", "pref_camera_picturesize_key", "pref_camera_jpegquality_key", "pref_camera_timer_key", "pref_camera_savepath_key", "pref_camera_longshot_key", "pref_camera_facedetection_key", CameraSettings.KEY_ISO, "pref_camera_exposure_key", "pref_camera_whitebalance_key", CameraSettings.KEY_QC_CHROMA_FLASH, "pref_camera_redeyereduction_key", CameraSettings.KEY_SELFIE_MIRROR, "pref_camera_shuttersound_key"};
        this.mOtherKeys2 = new String[]{"pref_selfie_flash_key", CameraSettings.KEY_FLASH_MODE, "pref_camera_recordlocation_key", "pref_camera_picturesize_key", "pref_camera_jpegquality_key", "pref_camera_timer_key", "pref_camera_savepath_key", "pref_camera_longshot_key", "pref_camera_facedetection_key", CameraSettings.KEY_ISO, "pref_camera_exposure_key", "pref_camera_whitebalance_key", CameraSettings.KEY_QC_CHROMA_FLASH, CameraSettings.KEY_FOCUS_MODE, "pref_camera_redeyereduction_key", CameraSettings.KEY_AUTO_HDR, CameraSettings.KEY_HDR_MODE, CameraSettings.KEY_HDR_NEED_1X, CameraSettings.KEY_CDS_MODE, CameraSettings.KEY_TNR_MODE, CameraSettings.KEY_HISTOGRAM, CameraSettings.KEY_ZSL, CameraSettings.KEY_TIMER_SOUND_EFFECTS, CameraSettings.KEY_FACE_RECOGNITION, CameraSettings.KEY_TOUCH_AF_AEC, CameraSettings.KEY_SELECTABLE_ZONE_AF, CameraSettings.KEY_PICTURE_FORMAT, CameraSettings.KEY_SATURATION, CameraSettings.KEY_CONTRAST, CameraSettings.KEY_SHARPNESS, CameraSettings.KEY_AUTOEXPOSURE, CameraSettings.KEY_ANTIBANDING, CameraSettings.KEY_DENOISE, CameraSettings.KEY_ADVANCED_FEATURES, CameraSettings.KEY_AE_BRACKET_HDR, CameraSettings.KEY_INSTANT_CAPTURE, CameraSettings.KEY_BOKEH_MODE, CameraSettings.KEY_BOKEH_MPO, CameraSettings.KEY_MANUAL_EXPOSURE, CameraSettings.KEY_MANUAL_WB, CameraSettings.KEY_MANUAL_FOCUS, CameraSettings.KEY_SELFIE_MIRROR, "pref_camera_shuttersound_key", CameraSettings.KEY_ZOOM};
        initSwitchItem(CameraSettings.KEY_CAMERA_ID, this.mFrontBackSwitcher);
    }

    public void onListPrefChanged(ListPreference listPreference) {
        onSettingChanged(listPreference);
        closeView();
    }

    public boolean handleBackKey() {
        if (TsMakeupManager.HAS_TS_MAKEUP && this.mTsMakeupManager.isShowMakeup()) {
            this.mTsMakeupManager.dismissMakeupUI();
            closeMakeupMode(true);
            this.mTsMakeupManager.resetMakeupUIStatus();
            this.mPopupStatus = 0;
            this.mPreviewMenuStatus = 0;
            return true;
        } else if (this.mPreviewMenuStatus == 2) {
            animateSlideOut(this.mPreviewMenu);
            return true;
        } else {
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
    }

    public void closeSceneMode() {
        this.mUI.removeSceneModeMenu();
    }

    public void closeMakeupMode(boolean z) {
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
                        PhotoMenu.this.mUI.dismissLevel1();
                        PhotoMenu.this.initializePopup();
                        PhotoMenu.this.mPopupStatus = 0;
                        PhotoMenu.this.mUI.cleanupListview();
                    } else if (i == 2) {
                        PhotoMenu.this.mUI.dismissLevel2();
                        PhotoMenu.this.mPopupStatus = 1;
                    }
                }

                public void onAnimationCancel(Animator animator) {
                    int i = i;
                    if (i == 1) {
                        PhotoMenu.this.mUI.dismissLevel1();
                        PhotoMenu.this.initializePopup();
                        PhotoMenu.this.mPopupStatus = 0;
                        PhotoMenu.this.mUI.cleanupListview();
                    } else if (i == 2) {
                        PhotoMenu.this.mUI.dismissLevel2();
                        PhotoMenu.this.mPopupStatus = 1;
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
                        PhotoMenu.this.mUI.dismissLevel1();
                        PhotoMenu.this.initializePopup();
                        PhotoMenu.this.mPopupStatus = 0;
                        PhotoMenu.this.mUI.cleanupListview();
                    } else if (i == 2) {
                        PhotoMenu.this.mUI.dismissLevel2();
                        PhotoMenu.this.mPopupStatus = 1;
                    }
                }

                public void onAnimationCancel(Animator animator) {
                    int i = i;
                    if (i == 1) {
                        PhotoMenu.this.mUI.dismissLevel1();
                        PhotoMenu.this.initializePopup();
                        PhotoMenu.this.mPopupStatus = 0;
                        PhotoMenu.this.mUI.cleanupListview();
                    } else if (i == 2) {
                        PhotoMenu.this.mUI.dismissLevel2();
                        PhotoMenu.this.mPopupStatus = 1;
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
        if (TsMakeupManager.HAS_TS_MAKEUP && this.mTsMakeupManager.isShowMakeup()) {
            this.mPreviewMenuStatus = 0;
            this.mTsMakeupManager.dismissMakeupUI();
            closeMakeupMode(true);
            this.mTsMakeupManager.resetMakeupUIStatus();
        }
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
                    PhotoMenu.this.closeSceneMode();
                    PhotoMenu.this.mPreviewMenuStatus = 0;
                }

                public void onAnimationCancel(Animator animator) {
                    PhotoMenu.this.closeSceneMode();
                    PhotoMenu.this.mPreviewMenuStatus = 0;
                }
            });
            animate.start();
        }
    }

    private void buttonSetEnabled(View view, boolean z) {
        view.setEnabled(z);
        if (view instanceof ViewGroup) {
            View childAt = ((ViewGroup) view).getChildAt(0);
            if (childAt != null) {
                childAt.setEnabled(z);
            }
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
        if (1 == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
            rect.left = this.mUI.getRootView().getWidth() - (rect.right - rect.left);
            rect.right = this.mUI.getRootView().getWidth();
        }
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

    public void overrideSettings(String... strArr) {
        if (this.mListMenu != null) {
            ListPreference findPreference = this.mPreferenceGroup.findPreference(CameraSettings.KEY_TNR_MODE);
            PreferenceGroup preferenceGroup = this.mPreferenceGroup;
            String str = CameraSettings.KEY_CDS_MODE;
            ListPreference findPreference2 = preferenceGroup.findPreference(str);
            String str2 = null;
            Object value = findPreference != null ? findPreference.getValue() : null;
            if (findPreference2 != null) {
                str2 = findPreference2.getValue();
            }
            if (this.mPrevSavedCDS == null && str2 != null) {
                this.mPrevSavedCDS = str2;
            }
            if (value != null && !this.mActivity.getString(C0905R.string.pref_camera_tnr_default).equals(value)) {
                this.mListMenu.setPreferenceEnabled(str, false);
                this.mListMenu.overrideSettings(str, this.mActivity.getString(C0905R.string.pref_camera_cds_value_off));
                this.mIsTNREnabled = true;
                if (!this.mIsCDSUpdated) {
                    if (str2 != null) {
                        this.mPrevSavedCDS = str2;
                    }
                    this.mIsCDSUpdated = true;
                }
            } else if (value != null) {
                this.mListMenu.setPreferenceEnabled(str, true);
                if (this.mIsTNREnabled) {
                    String str3 = this.mPrevSavedCDS;
                    if (str3 != str2) {
                        this.mListMenu.overrideSettings(str, str3);
                        this.mIsTNREnabled = false;
                        this.mIsCDSUpdated = false;
                    }
                }
            }
        }
        for (int i = 0; i < strArr.length; i += 2) {
            if (strArr[i].equals(CameraSettings.KEY_SCENE_MODE)) {
                buttonSetEnabled(this.mSceneModeSwitcher, strArr[i + 1] == null);
            }
        }
        super.overrideSettings(strArr);
        if (this.mListMenu == null) {
            initializePopup();
        }
        this.mListMenu.overrideSettings(strArr);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0248  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x024d  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0256  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x02cd  */
    /* JADX WARNING: Removed duplicated region for block: B:97:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void initializePopup() {
        /*
            r23 = this;
            r0 = r23
            com.android.camera.CameraActivity r1 = r0.mActivity
            java.lang.String r2 = "layout_inflater"
            java.lang.Object r1 = r1.getSystemService(r2)
            android.view.LayoutInflater r1 = (android.view.LayoutInflater) r1
            r2 = 0
            r3 = 0
            r4 = 2131361814(0x7f0a0016, float:1.834339E38)
            android.view.View r1 = r1.inflate(r4, r2, r3)
            com.android.camera.ui.ListMenu r1 = (com.android.camera.p004ui.ListMenu) r1
            r1.setSettingChangedListener(r0)
            java.lang.String[] r4 = r0.mOtherKeys1
            com.android.camera.CameraActivity r5 = r0.mActivity
            boolean r5 = r5.isDeveloperMenuEnabled()
            if (r5 == 0) goto L_0x0026
            java.lang.String[] r4 = r0.mOtherKeys2
        L_0x0026:
            com.android.camera.PreferenceGroup r5 = r0.mPreferenceGroup
            r1.initialize(r5, r4)
            com.android.camera.CameraActivity r4 = r0.mActivity
            boolean r4 = r4.isSecureCamera()
            if (r4 == 0) goto L_0x0038
            java.lang.String r4 = "pref_camera_recordlocation_key"
            r1.setPreferenceEnabled(r4, r3)
        L_0x0038:
            r0.mListMenu = r1
            com.android.camera.PreferenceGroup r4 = r0.mPreferenceGroup
            java.lang.String r5 = "pref_camera_scenemode_key"
            com.android.camera.ListPreference r4 = r4.findPreference(r5)
            com.android.camera.PreferenceGroup r6 = r0.mPreferenceGroup
            java.lang.String r7 = "pref_camera_hdr_key"
            com.android.camera.ListPreference r6 = r6.findPreference(r7)
            r0.updateFilterModeIcon(r4, r6)
            if (r4 == 0) goto L_0x0054
            java.lang.String r4 = r4.getValue()
            goto L_0x0055
        L_0x0054:
            r4 = r2
        L_0x0055:
            com.android.camera.PreferenceGroup r6 = r0.mPreferenceGroup
            java.lang.String r8 = "pref_camera_facedetection_key"
            com.android.camera.ListPreference r6 = r6.findPreference(r8)
            if (r6 == 0) goto L_0x0064
            java.lang.String r6 = r6.getValue()
            goto L_0x0065
        L_0x0064:
            r6 = r2
        L_0x0065:
            com.android.camera.PreferenceGroup r8 = r0.mPreferenceGroup
            java.lang.String r9 = "pref_camera_zsl_key"
            com.android.camera.ListPreference r8 = r8.findPreference(r9)
            if (r8 == 0) goto L_0x0074
            java.lang.String r8 = r8.getValue()
            goto L_0x0075
        L_0x0074:
            r8 = r2
        L_0x0075:
            com.android.camera.PreferenceGroup r10 = r0.mPreferenceGroup
            java.lang.String r11 = "pref_camera_auto_hdr_key"
            com.android.camera.ListPreference r10 = r10.findPreference(r11)
            if (r10 == 0) goto L_0x0084
            java.lang.String r10 = r10.getValue()
            goto L_0x0085
        L_0x0084:
            r10 = r2
        L_0x0085:
            java.lang.String r11 = "pref_camera_touchafaec_key"
            java.lang.String r12 = "pref_camera_focusmode_key"
            java.lang.String r13 = "chroma-flash"
            java.lang.String r14 = "enable"
            java.lang.String r15 = "pref_camera_flashmode_key"
            java.lang.String r2 = "pref_camera_coloreffect_key"
            if (r4 == 0) goto L_0x009e
            java.lang.String r3 = "auto"
            boolean r3 = r3.equals(r4)
            if (r3 == 0) goto L_0x009c
            goto L_0x009e
        L_0x009c:
            r3 = 0
            goto L_0x00a7
        L_0x009e:
            if (r10 == 0) goto L_0x00d5
            boolean r3 = r10.equals(r14)
            if (r3 == 0) goto L_0x00d5
            goto L_0x009c
        L_0x00a7:
            r1.setPreferenceEnabled(r12, r3)
            java.lang.String r4 = "pref_camera_autoexposure_key"
            r1.setPreferenceEnabled(r4, r3)
            r1.setPreferenceEnabled(r11, r3)
            java.lang.String r4 = "pref_camera_saturation_key"
            r1.setPreferenceEnabled(r4, r3)
            java.lang.String r4 = "pref_camera_contrast_key"
            r1.setPreferenceEnabled(r4, r3)
            java.lang.String r4 = "pref_camera_sharpness_key"
            r1.setPreferenceEnabled(r4, r3)
            r1.setPreferenceEnabled(r2, r3)
            r1.setPreferenceEnabled(r15, r3)
            java.lang.String r4 = "pref_camera_whitebalance_key"
            r1.setPreferenceEnabled(r4, r3)
            java.lang.String r4 = "pref_camera_exposure_key"
            r1.setPreferenceEnabled(r4, r3)
            r1.setPreferenceEnabled(r13, r3)
            goto L_0x00d6
        L_0x00d5:
            r3 = 0
        L_0x00d6:
            if (r10 == 0) goto L_0x00e1
            boolean r4 = r10.equals(r14)
            if (r4 == 0) goto L_0x00e1
            r1.setPreferenceEnabled(r5, r3)
        L_0x00e1:
            if (r6 == 0) goto L_0x00f0
            java.lang.String r4 = org.codeaurora.snapcam.wrapper.ParametersWrapper.FACE_DETECTION_ON
            boolean r4 = r4.equals(r6)
            if (r4 != 0) goto L_0x00f0
            java.lang.String r4 = "pref_camera_facerc_key"
            r1.setPreferenceEnabled(r4, r3)
        L_0x00f0:
            com.android.camera.PhotoUI r3 = r0.mUI
            boolean r3 = r3.isCountingDown()
            r4 = 1
            r3 = r3 ^ r4
            r1.setPreferenceEnabled(r9, r3)
            com.android.camera.PreferenceGroup r3 = r0.mPreferenceGroup
            java.lang.String r6 = "pref_camera_advanced_features_key"
            com.android.camera.ListPreference r3 = r3.findPreference(r6)
            if (r3 == 0) goto L_0x010a
            java.lang.String r3 = r3.getValue()
            goto L_0x010b
        L_0x010a:
            r3 = 0
        L_0x010b:
            com.android.camera.CameraActivity r4 = r0.mActivity
            r16 = r13
            r13 = 2131689954(0x7f0f01e2, float:1.9008938E38)
            java.lang.String r4 = r4.getString(r13)
            com.android.camera.CameraActivity r13 = r0.mActivity
            r17 = r9
            r9 = 2131689948(0x7f0f01dc, float:1.9008926E38)
            java.lang.String r9 = r13.getString(r9)
            com.android.camera.CameraActivity r13 = r0.mActivity
            r18 = r14
            r14 = 2131689941(0x7f0f01d5, float:1.9008912E38)
            java.lang.String r13 = r13.getString(r14)
            com.android.camera.CameraActivity r14 = r0.mActivity
            r19 = r10
            r10 = 2131689946(0x7f0f01da, float:1.9008922E38)
            java.lang.String r10 = r14.getString(r10)
            com.android.camera.CameraActivity r14 = r0.mActivity
            r20 = r7
            r7 = 2131689939(0x7f0f01d3, float:1.9008908E38)
            java.lang.String r7 = r14.getString(r7)
            com.android.camera.CameraActivity r14 = r0.mActivity
            r21 = r5
            r5 = 2131689952(0x7f0f01e0, float:1.9008934E38)
            java.lang.String r5 = r14.getString(r5)
            com.android.camera.CameraActivity r14 = r0.mActivity
            r22 = r11
            r11 = 2131689943(0x7f0f01d7, float:1.9008916E38)
            java.lang.String r11 = r14.getString(r11)
            if (r8 == 0) goto L_0x0196
            java.lang.String r14 = org.codeaurora.snapcam.wrapper.ParametersWrapper.ZSL_OFF
            boolean r8 = r14.equals(r8)
            if (r8 == 0) goto L_0x0196
            r3 = 2
            java.lang.String[] r3 = new java.lang.String[r3]
            r4 = 0
            r3[r4] = r6
            com.android.camera.CameraActivity r5 = r0.mActivity
            r7 = 2131689928(0x7f0f01c8, float:1.9008885E38)
            java.lang.String r5 = r5.getString(r7)
            r7 = 1
            r3[r7] = r5
            r1.overrideSettings(r3)
            r1.setPreferenceEnabled(r6, r4)
            java.lang.String r3 = "pref_camera_instant_capture_key"
            r1.setPreferenceEnabled(r3, r4)
            boolean r3 = com.android.camera.TsMakeupManager.HAS_TS_MAKEUP
            if (r3 != 0) goto L_0x0190
            android.view.View r3 = r0.mHdrSwitcher
            int r3 = r3.getVisibility()
            if (r3 != 0) goto L_0x0190
            android.view.View r3 = r0.mHdrSwitcher
            r0.buttonSetEnabled(r3, r7)
        L_0x0190:
            r6 = r20
            r4 = r21
            goto L_0x0219
        L_0x0196:
            if (r3 == 0) goto L_0x0203
            boolean r4 = r3.equals(r4)
            if (r4 != 0) goto L_0x01c2
            boolean r4 = r3.equals(r13)
            if (r4 != 0) goto L_0x01c2
            boolean r4 = r3.equals(r9)
            if (r4 != 0) goto L_0x01c2
            boolean r4 = r3.equals(r10)
            if (r4 != 0) goto L_0x01c2
            boolean r4 = r3.equals(r7)
            if (r4 != 0) goto L_0x01c2
            boolean r4 = r3.equals(r5)
            if (r4 != 0) goto L_0x01c2
            boolean r3 = r3.equals(r11)
            if (r3 == 0) goto L_0x0203
        L_0x01c2:
            r3 = 0
            r1.setPreferenceEnabled(r12, r3)
            r1.setPreferenceEnabled(r15, r3)
            java.lang.String r4 = "pref_camera_ae_bracket_hdr_key"
            r1.setPreferenceEnabled(r4, r3)
            java.lang.String r4 = "pref_camera_redeyereduction_key"
            r1.setPreferenceEnabled(r4, r3)
            java.lang.String r4 = "pref_camera_exposure_key"
            r1.setPreferenceEnabled(r4, r3)
            r1.setPreferenceEnabled(r2, r3)
            r4 = r22
            r1.setPreferenceEnabled(r4, r3)
            r4 = r21
            r1.setPreferenceEnabled(r4, r3)
            java.lang.String r5 = "pref_camera_instant_capture_key"
            r1.setPreferenceEnabled(r5, r3)
            java.lang.String r5 = r0.mSettingOff
            r6 = r20
            r0.setPreference(r6, r5)
            boolean r5 = com.android.camera.TsMakeupManager.HAS_TS_MAKEUP
            if (r5 != 0) goto L_0x0219
            android.view.View r5 = r0.mHdrSwitcher
            int r5 = r5.getVisibility()
            if (r5 != 0) goto L_0x0219
            android.view.View r5 = r0.mHdrSwitcher
            r0.buttonSetEnabled(r5, r3)
            goto L_0x0219
        L_0x0203:
            r6 = r20
            r4 = r21
            boolean r3 = com.android.camera.TsMakeupManager.HAS_TS_MAKEUP
            if (r3 != 0) goto L_0x0219
            android.view.View r3 = r0.mHdrSwitcher
            int r3 = r3.getVisibility()
            if (r3 != 0) goto L_0x0219
            android.view.View r3 = r0.mHdrSwitcher
            r5 = 1
            r0.buttonSetEnabled(r3, r5)
        L_0x0219:
            if (r19 == 0) goto L_0x0238
            r3 = r18
            r10 = r19
            boolean r3 = r10.equals(r3)
            if (r3 == 0) goto L_0x0238
            android.view.View r3 = r0.mHdrSwitcher
            r5 = 8
            r3.setVisibility(r5)
            com.android.camera.PhotoUI r3 = r0.mUI
            com.android.camera.ui.CameraControls r3 = r3.getCameraControls()
            android.view.View r5 = r0.mHdrSwitcher
            r3.removeFromViewList(r5)
            goto L_0x023e
        L_0x0238:
            android.view.View r3 = r0.mHdrSwitcher
            r5 = 0
            r3.setVisibility(r5)
        L_0x023e:
            com.android.camera.PreferenceGroup r3 = r0.mPreferenceGroup
            java.lang.String r5 = "pref_camera_bokeh_mode_key"
            com.android.camera.ListPreference r3 = r3.findPreference(r5)
            if (r3 == 0) goto L_0x024d
            java.lang.String r3 = r3.getValue()
            goto L_0x024e
        L_0x024d:
            r3 = 0
        L_0x024e:
            java.lang.String r5 = "1"
            boolean r3 = r5.equals(r3)
            if (r3 == 0) goto L_0x02c9
            android.view.View r3 = r0.mHdrSwitcher
            r5 = 0
            r0.buttonSetEnabled(r3, r5)
            android.view.View r3 = r0.mSceneModeSwitcher
            r0.buttonSetEnabled(r3, r5)
            android.view.View r3 = r0.mFilterModeSwitcher
            r0.buttonSetEnabled(r3, r5)
            r1.setPreferenceEnabled(r4, r5)
            r1.setPreferenceEnabled(r6, r5)
            r3 = r17
            r1.setPreferenceEnabled(r3, r5)
            r1.setPreferenceEnabled(r15, r5)
            java.lang.String r7 = "pref_camera_longshot_key"
            r1.setPreferenceEnabled(r7, r5)
            r1.setPreferenceEnabled(r2, r5)
            r7 = r16
            r1.setPreferenceEnabled(r7, r5)
            java.lang.String r8 = "pref_camera_picturesize_key"
            r1.setPreferenceEnabled(r8, r5)
            com.android.camera.CameraActivity r1 = r0.mActivity
            r5 = 2131690408(0x7f0f03a8, float:1.9009859E38)
            java.lang.String r1 = r1.getString(r5)
            r0.setPreference(r4, r1)
            java.lang.String r1 = "off"
            r0.setPreference(r6, r1)
            com.android.camera.CameraActivity r4 = r0.mActivity
            r5 = 2131690594(0x7f0f0462, float:1.9010236E38)
            java.lang.String r4 = r4.getString(r5)
            r0.setPreference(r3, r4)
            r0.setPreference(r15, r1)
            java.lang.String r3 = "pref_camera_longshot_key"
            r0.setPreference(r3, r1)
            java.lang.String r3 = "none"
            r0.setPreference(r2, r3)
            r0.setPreference(r7, r1)
            com.android.camera.PreferenceGroup r1 = r0.mPreferenceGroup
            com.android.camera.ListPreference r1 = r1.findPreference(r8)
            java.lang.CharSequence[] r1 = r1.getEntryValues()
            r2 = 0
            r1 = r1[r2]
            if (r1 == 0) goto L_0x02c9
            java.lang.String r1 = r1.toString()
            r0.setPreference(r8, r1)
        L_0x02c9:
            com.android.camera.CameraPreference$OnPreferenceChangedListener r0 = r0.mListener
            if (r0 == 0) goto L_0x02d0
            r0.onSharedPreferenceChanged()
        L_0x02d0:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.PhotoMenu.initializePopup():void");
    }

    private void updateFilterModeIcon(ListPreference listPreference, ListPreference listPreference2) {
        if (listPreference != null && listPreference2 != null) {
            String str = "auto";
            String str2 = CameraSettings.KEY_SCENE_MODE;
            if (!notSame(listPreference, str2, str)) {
                String str3 = this.mSettingOff;
                String str4 = CameraSettings.KEY_CAMERA_HDR;
                if (!notSame(listPreference2, str4, str3)) {
                    if (!same(listPreference, str2, str)) {
                        return;
                    }
                    if (same(listPreference2, str4, this.mSettingOff) || !listPreference2.getKey().equals(str4)) {
                        buttonSetEnabled(this.mFilterModeSwitcher, true);
                        return;
                    }
                    return;
                }
            }
            buttonSetEnabled(this.mFilterModeSwitcher, false);
            changeFilterModeControlIcon("none");
        }
    }

    public void initSwitchItem(final String str, View view) {
        int i;
        IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(str);
        if (iconListPreference != null) {
            int[] largeIconIds = iconListPreference.getLargeIconIds();
            int findIndexOfValue = iconListPreference.findIndexOfValue(iconListPreference.getValue());
            if (iconListPreference.getUseSingleIcon() || largeIconIds == null) {
                i = iconListPreference.getSingleIcon();
            } else {
                i = largeIconIds[findIndexOfValue];
            }
            ((ImageView) view).setImageResource(i);
            view.setVisibility(0);
            this.mPreferences.add(iconListPreference);
            this.mPreferenceMap.put(iconListPreference, view);
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    IconListPreference iconListPreference = (IconListPreference) PhotoMenu.this.mPreferenceGroup.findPreference(str);
                    if (iconListPreference != null) {
                        String str = str;
                        String str2 = CameraSettings.KEY_CAMERA_ID;
                        if (str.equals(str2)) {
                            PhotoMenu.this.mUI.hideUI();
                        }
                        int findIndexOfValue = (iconListPreference.findIndexOfValue(iconListPreference.getValue()) + 1) % iconListPreference.getEntryValues().length;
                        iconListPreference.setValueIndex(findIndexOfValue);
                        ((ImageView) view).setImageResource(iconListPreference.getLargeIconIds()[findIndexOfValue]);
                        if (str.equals(str2)) {
                            PhotoMenu.this.mListener.onCameraPickerClicked(findIndexOfValue);
                        }
                        PhotoMenu.this.reloadPreference(iconListPreference);
                        PhotoMenu.this.onSettingChanged(iconListPreference);
                    }
                }
            });
        }
    }

    public void initBokehModeButton(View view) {
        int i;
        view.setVisibility(4);
        final IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(CameraSettings.KEY_BOKEH_MODE);
        if (iconListPreference == null) {
            view.setVisibility(8);
            return;
        }
        int[] largeIconIds = iconListPreference.getLargeIconIds();
        int findIndexOfValue = iconListPreference.findIndexOfValue(iconListPreference.getValue());
        if (iconListPreference.getUseSingleIcon() || largeIconIds == null) {
            i = iconListPreference.getSingleIcon();
        } else {
            i = largeIconIds[findIndexOfValue];
        }
        ((ImageView) view).setImageResource(i);
        view.setVisibility(0);
        view.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ListPreference findPreference = PhotoMenu.this.mPreferenceGroup.findPreference(CameraSettings.KEY_BOKEH_MODE);
                if ((findPreference != null ? findPreference.getValue() : null) != null) {
                    int currentIndex = (findPreference.getCurrentIndex() + 1) % findPreference.getEntryValues().length;
                    findPreference.setValueIndex(currentIndex);
                    ((ImageView) view).setImageResource(iconListPreference.getLargeIconIds()[currentIndex]);
                    PhotoMenu.this.reloadPreference(iconListPreference);
                    PhotoMenu.this.initializePopup();
                    PhotoMenu.this.onSettingChanged(findPreference);
                }
            }
        });
    }

    public void initMakeupModeButton(View view) {
        int i;
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            view.setVisibility(4);
            IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(CameraSettings.KEY_TS_MAKEUP_UILABLE);
            if (iconListPreference != null) {
                int[] largeIconIds = iconListPreference.getLargeIconIds();
                int findIndexOfValue = iconListPreference.findIndexOfValue(iconListPreference.getValue());
                if (iconListPreference.getUseSingleIcon() || largeIconIds == null) {
                    i = iconListPreference.getSingleIcon();
                } else {
                    i = largeIconIds[findIndexOfValue];
                }
                ((ImageView) this.mTsMakeupSwitcher).setImageResource(i);
                view.setVisibility(0);
                String value = iconListPreference.getValue();
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("PhotoMenu.initMakeupModeButton():current init makeupOn is ");
                sb.append(value);
                Log.d(str, sb.toString());
                view.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        ListPreference findPreference = PhotoMenu.this.mPreferenceGroup.findPreference("pref_camera_facedetection_key");
                        String value = findPreference != null ? findPreference.getValue() : null;
                        String access$400 = PhotoMenu.TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("initMakeupModeButton().onClick(): faceDetection is ");
                        sb.append(value);
                        Log.d(access$400, sb.toString());
                        if (value == null || !ParametersWrapper.FACE_DETECTION_OFF.equals(value)) {
                            PhotoMenu.this.toggleMakeupSettings();
                        } else {
                            PhotoMenu.this.showAlertDialog(findPreference);
                        }
                    }
                });
            }
        }
    }

    private void initMakeupMenu() {
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            this.mPopupStatus = 0;
            this.mHandler.removeMessages(0);
            this.mSceneStatus = 2;
            this.mPreviewMenuStatus = 2;
            this.mHandler.sendEmptyMessageDelayed(0, 300);
        }
    }

    /* access modifiers changed from: private */
    public void showAlertDialog(final ListPreference listPreference) {
        if (!this.mActivity.isFinishing()) {
            new Builder(this.mActivity).setIcon(17301543).setMessage(C0905R.string.text_tsmakeup_alert_msg).setPositiveButton(C0905R.string.text_tsmakeup_alert_continue, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    PhotoMenu.this.toggleMakeupSettings();
                    listPreference.setValue(ParametersWrapper.FACE_DETECTION_ON);
                    PhotoMenu.this.onSettingChanged(listPreference);
                }
            }).setNegativeButton(C0905R.string.text_tsmakeup_alert_quit, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).show();
        }
    }

    /* access modifiers changed from: private */
    public void toggleMakeupSettings() {
        this.mUI.hideUI();
        initMakeupMenu();
    }

    /* access modifiers changed from: private */
    public void closeMakeup() {
        if (!TsMakeupManager.HAS_TS_MAKEUP) {
            return;
        }
        if (this.mTsMakeupManager.isShowMakeup()) {
            this.mTsMakeupManager.hideMakeupUI();
            closeMakeupMode(false);
            this.mPreviewMenuStatus = 0;
            return;
        }
        this.mTsMakeupManager.hideMakeupUI();
    }

    public void initSceneModeButton(View view) {
        view.setVisibility(4);
        IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(CameraSettings.KEY_SCENE_MODE);
        if (iconListPreference != null) {
            updateSceneModeIcon(iconListPreference);
            view.setVisibility(0);
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    PhotoMenu.this.addSceneMode();
                    ViewGroup previewMenuLayout = PhotoMenu.this.mUI.getPreviewMenuLayout();
                    if (previewMenuLayout != null) {
                        View childAt = previewMenuLayout.getChildAt(0);
                        PhotoMenu.this.mUI.adjustOrientation();
                        PhotoMenu photoMenu = PhotoMenu.this;
                        photoMenu.animateSlideIn(childAt, photoMenu.previewMenuSize, false);
                    }
                }
            });
        }
    }

    public void addModeBack() {
        if (this.mSceneStatus == 0) {
            addSceneMode();
        }
        if (this.mSceneStatus == 1) {
            addFilterMode();
        }
    }

    public void addSceneMode() {
        int i;
        final IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(CameraSettings.KEY_SCENE_MODE);
        if (iconListPreference != null) {
            int displayRotation = CameraUtil.getDisplayRotation(this.mActivity);
            if (!CameraUtil.isDefaultToPortrait(this.mActivity)) {
                displayRotation = (displayRotation + 90) % 360;
            }
            Display defaultDisplay = ((WindowManager) this.mActivity.getSystemService("window")).getDefaultDisplay();
            CharSequence[] entries = iconListPreference.getEntries();
            CharSequence[] entryValues = iconListPreference.getEntryValues();
            int[] thumbnailIds = iconListPreference.getThumbnailIds();
            Resources resources = this.mActivity.getResources();
            int dimension = (int) (resources.getDimension(C0905R.dimen.scene_mode_height) + (resources.getDimension(C0905R.dimen.scene_mode_padding) * 2.0f) + 1.0f);
            int dimension2 = (int) (resources.getDimension(C0905R.dimen.scene_mode_width) + (resources.getDimension(C0905R.dimen.scene_mode_padding) * 2.0f) + 1.0f);
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
            this.mSceneStatus = 0;
            LayoutInflater layoutInflater = (LayoutInflater) this.mActivity.getSystemService("layout_inflater");
            ViewGroup viewGroup = null;
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
                RotateLayout rotateLayout = (RotateLayout) layoutInflater.inflate(C0905R.layout.scene_mode_view, viewGroup, z);
                ImageView imageView = (ImageView) rotateLayout.findViewById(C0905R.C0907id.image);
                TextView textView = (TextView) rotateLayout.findViewById(C0905R.C0907id.label);
                rotateLayout.setOnTouchListener(new OnTouchListener() {
                    private long startTime;

                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == 0) {
                            this.startTime = System.currentTimeMillis();
                        } else if (motionEvent.getAction() == 1 && System.currentTimeMillis() - this.startTime < 200) {
                            iconListPreference.setValueIndex(i3);
                            PhotoMenu.this.onSettingChanged(iconListPreference);
                            PhotoMenu.this.updateSceneModeIcon(iconListPreference);
                            for (View backgroundResource : viewArr) {
                                backgroundResource.setBackgroundResource(C0905R.C0906drawable.scene_mode_view_border);
                            }
                            view.findViewById(C0905R.C0907id.border).setBackgroundResource(C0905R.C0906drawable.scene_mode_view_border_selected);
                            PhotoMenu.this.animateSlideOutPreviewMenu();
                        }
                        return true;
                    }
                });
                View findViewById = rotateLayout.findViewById(C0905R.C0907id.border);
                viewArr[i3] = findViewById;
                IconListPreference iconListPreference2 = iconListPreference;
                if (i3 == currentIndex) {
                    findViewById.setBackgroundResource(C0905R.C0906drawable.scene_mode_view_border_selected);
                }
                imageView.setImageResource(thumbnailIds[i3]);
                textView.setText(entries[i3]);
                linearLayout2.addView(rotateLayout);
                int i4 = 8;
                if (entryValues[i3].equals("asd")) {
                    if (this.mActivity.isDeveloperMenuEnabled()) {
                        i4 = 0;
                    }
                    rotateLayout.setVisibility(i4);
                } else if (entryValues[i3].equals(CameraUtil.SCENE_MODE_HDR)) {
                    ListPreference findPreference = this.mPreferenceGroup.findPreference(CameraSettings.KEY_AUTO_HDR);
                    if (findPreference != null && findPreference.getValue().equalsIgnoreCase("enable")) {
                        rotateLayout.setVisibility(8);
                    }
                } else if (CameraApp.mIsLowMemoryDevice && (entryValues[i3].equals(this.mActivity.getResources().getString(C0905R.string.pref_camera_advanced_feature_value_refocus_on)) || entryValues[i3].equals(this.mActivity.getResources().getString(C0905R.string.pref_camera_advanced_feature_value_optizoom_on)))) {
                    rotateLayout.setVisibility(8);
                }
                i3++;
                iconListPreference = iconListPreference2;
                z = false;
                viewGroup = null;
            }
            linearLayout.addView(frameLayout);
            this.mPreviewMenu = frameLayout;
        }
    }

    public void updateSceneModeIcon(IconListPreference iconListPreference) {
        int[] thumbnailIds = iconListPreference.getThumbnailIds();
        int currentIndex = iconListPreference.getCurrentIndex();
        if (currentIndex == -1) {
            currentIndex = 0;
        }
        ((ImageView) this.mSceneModeSwitcher).setImageResource(thumbnailIds[currentIndex]);
    }

    public void initFilterModeButton(View view) {
        view.setVisibility(4);
        IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference(CameraSettings.KEY_COLOR_EFFECT);
        if (iconListPreference != null && iconListPreference.getValue() != null) {
            changeFilterModeControlIcon(iconListPreference.getValue());
            view.setVisibility(0);
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    PhotoMenu.this.closeMakeup();
                    PhotoMenu.this.addFilterMode();
                    if (PhotoMenu.this.mUI.getPreviewMenuLayout() != null) {
                        View childAt = PhotoMenu.this.mUI.getPreviewMenuLayout().getChildAt(0);
                        PhotoMenu.this.mUI.adjustOrientation();
                        PhotoMenu photoMenu = PhotoMenu.this;
                        photoMenu.animateSlideIn(childAt, photoMenu.previewMenuSize, false);
                    }
                }
            });
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
                            PhotoMenu.this.changeFilterModeControlIcon(iconListPreference.getValue());
                            PhotoMenu.this.onSettingChanged(iconListPreference);
                            for (View background : viewArr) {
                                background.setBackground(null);
                            }
                            ((ImageView) view.findViewById(C0905R.C0907id.image)).setBackgroundColor(-13388315);
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
            if (TsMakeupManager.HAS_TS_MAKEUP) {
                if (this.mTsMakeupManager.isShowMakeup()) {
                    this.mTsMakeupManager.dismissMakeupUI();
                    closeMakeupMode(false);
                    this.mPreviewMenuStatus = 0;
                } else {
                    this.mTsMakeupManager.dismissMakeupUI();
                }
                this.mTsMakeupManager.resetMakeupUIStatus();
            }
            if (this.mListMenu == null || this.mPopupStatus != 1) {
                initializePopup();
                this.mPopupStatus = 1;
            }
            this.mUI.showPopup(this.mListMenu, 1, true);
        }
    }

    public void popupDismissed(boolean z) {
        if (z || this.mPopupStatus != 2) {
            initializePopup();
            return;
        }
        initializePopup();
        this.mPopupStatus = 1;
        this.mUI.showPopup(this.mListMenu, 1, false);
        if (this.mListMenu != null) {
            this.mListMenu = null;
        }
    }

    public void onPreferenceClicked(ListPreference listPreference) {
        onPreferenceClicked(listPreference, 0);
    }

    public void onPreferenceClicked(ListPreference listPreference, int i) {
        if (!this.mActivity.isDeveloperMenuEnabled()) {
            if (listPreference.getKey().equals("pref_camera_redeyereduction_key")) {
                this.privateCounter++;
                if (this.privateCounter >= 10) {
                    this.mActivity.enableDeveloperMenu();
                    PreferenceManager.getDefaultSharedPreferences(this.mActivity).edit().putBoolean(CameraSettings.KEY_DEVELOPER_MENU, true).apply();
                    RotateTextToast.makeText((Activity) this.mActivity, (CharSequence) "Camera developer option is enabled now", 0).show();
                }
            } else {
                this.privateCounter = 0;
            }
        }
        ListSubMenu listSubMenu = (ListSubMenu) ((LayoutInflater) this.mActivity.getSystemService("layout_inflater")).inflate(C0905R.layout.list_sub_menu, null, false);
        listSubMenu.initialize(listPreference, i);
        listSubMenu.setSettingChangedListener(this);
        listSubMenu.setAlpha(0.0f);
        this.mListSubMenu = listSubMenu;
        this.mUI.removeLevel2();
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

    public void removeAllView() {
        PhotoUI photoUI = this.mUI;
        if (photoUI != null) {
            photoUI.removeLevel2();
        }
        if (this.mListMenu != null) {
            this.mUI.dismissLevel1();
            this.mPopupStatus = 0;
        }
        closeSceneMode();
        this.mPreviewMenuStatus = 0;
    }

    public void closeAllView() {
        PhotoUI photoUI = this.mUI;
        if (photoUI != null) {
            photoUI.removeLevel2();
        }
        ListMenu listMenu = this.mListMenu;
        if (listMenu != null) {
            animateSlideOut(listMenu, 1);
        }
        animateSlideOutPreviewMenu();
    }

    public void closeView() {
        PhotoUI photoUI = this.mUI;
        if (photoUI != null) {
            photoUI.removeLevel2();
        }
        ListMenu listMenu = this.mListMenu;
        if (listMenu != null && this.mPopupStatus != 0) {
            animateSlideOut(listMenu, 1);
        }
    }

    private static boolean notSame(ListPreference listPreference, String str, String str2) {
        return str.equals(listPreference.getKey()) && !str2.equals(listPreference.getValue());
    }

    private static boolean same(ListPreference listPreference, String str, String str2) {
        return str.equals(listPreference.getKey()) && str2.equals(listPreference.getValue());
    }

    public void setPreference(String str, String str2) {
        ListPreference findPreference = this.mPreferenceGroup.findPreference(str);
        if (findPreference != null && !str2.equals(findPreference.getValue())) {
            findPreference.setValue(str2);
            reloadPreferences();
        }
    }

    public void onSettingChanged(ListPreference listPreference) {
        String str = CameraUtil.SCENE_MODE_HDR;
        String str2 = CameraSettings.KEY_SCENE_MODE;
        boolean same = same(listPreference, str2, str);
        String str3 = "off";
        String str4 = CameraSettings.KEY_FLASH_MODE;
        String str5 = "auto";
        String str6 = CameraSettings.KEY_CAMERA_HDR;
        String str7 = CameraSettings.KEY_ADVANCED_FEATURES;
        if (same) {
            ListPreference findPreference = this.mPreferenceGroup.findPreference(str6);
            if (findPreference != null && same(findPreference, str6, this.mSettingOff)) {
                setPreference(str6, this.mSettingOn);
            }
        } else if (notSame(listPreference, str2, str)) {
            ListPreference findPreference2 = this.mPreferenceGroup.findPreference(str6);
            if (findPreference2 != null && notSame(findPreference2, str6, this.mSettingOff)) {
                setPreference(str6, this.mSettingOff);
            }
        } else if (same(listPreference, str6, this.mSettingOff)) {
            ListPreference findPreference3 = this.mPreferenceGroup.findPreference(str2);
            if (findPreference3 != null && notSame(findPreference3, str2, str5)) {
                setPreference(str2, str5);
            }
            updateSceneModeIcon((IconListPreference) findPreference3);
            updateFilterModeIcon(findPreference3, listPreference);
        } else if (same(listPreference, str6, this.mSettingOn)) {
            ListPreference findPreference4 = this.mPreferenceGroup.findPreference(str2);
            if (findPreference4 != null && notSame(findPreference4, str2, str)) {
                setPreference(str2, str);
            }
            updateSceneModeIcon((IconListPreference) findPreference4);
        } else {
            String str8 = TsMakeupManager.MAKEUP_OFF;
            String str9 = CameraSettings.KEY_AE_BRACKET_HDR;
            if (notSame(listPreference, str9, str8)) {
                RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.flash_aebracket_message, 0).show();
                setPreference(str4, str3);
            } else if (notSame(listPreference, str4, str8)) {
                ListPreference findPreference5 = this.mPreferenceGroup.findPreference(str9);
                if (findPreference5 != null && notSame(findPreference5, str9, str8)) {
                    RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.flash_aebracket_message, 0).show();
                }
            } else {
                String str10 = "pref_camera_longshot_key";
                if (notSame(listPreference, str10, this.mSettingOff)) {
                    ListPreference findPreference6 = this.mPreferenceGroup.findPreference(str7);
                    if (findPreference6 != null) {
                        if (notSame(findPreference6, str7, this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_default))) {
                            RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.longshot_enable_message, 1).show();
                        }
                        setPreference(str7, this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_default));
                    }
                } else if (notSame(listPreference, str7, this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_default))) {
                    ListPreference findPreference7 = this.mPreferenceGroup.findPreference(str10);
                    if (findPreference7 != null) {
                        if (notSame(findPreference7, str10, this.mSettingOff)) {
                            RotateTextToast.makeText((Activity) this.mActivity, (int) C0905R.string.advance_feature_enable_msg, 1).show();
                        }
                        setPreference(str10, this.mSettingOff);
                    }
                }
            }
        }
        String string = this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_refocus_on);
        if (notSame(listPreference, str2, string)) {
            ListPreference findPreference8 = this.mPreferenceGroup.findPreference(str7);
            if (findPreference8 != null && string.equals(findPreference8.getValue())) {
                setPreference(str7, this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_default));
            }
        }
        String string2 = this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_optizoom_on);
        if (notSame(listPreference, str2, string2)) {
            ListPreference findPreference9 = this.mPreferenceGroup.findPreference(str7);
            if (findPreference9 != null && string2.equals(findPreference9.getValue())) {
                setPreference(str7, this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_default));
            }
        }
        String string3 = this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_chromaflash_on);
        if (notSame(listPreference, str2, str5)) {
            ListPreference findPreference10 = this.mPreferenceGroup.findPreference(str7);
            if (findPreference10 != null && string3.equals(findPreference10.getValue())) {
                setPreference(CameraSettings.KEY_QC_CHROMA_FLASH, this.mSettingOff);
                setPreference(str7, this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_default));
            }
        }
        if (notSame(listPreference, str2, str5)) {
            setPreference(CameraSettings.KEY_COLOR_EFFECT, this.mActivity.getString(C0905R.string.pref_camera_coloreffect_default));
        }
        if (same(listPreference, str7, this.mActivity.getString(C0905R.string.pref_camera_advanced_feature_value_stillmore_on))) {
            setPreference(str4, str3);
        }
        ListPreference findPreference11 = this.mPreferenceGroup.findPreference(CameraSettings.KEY_AUTO_HDR);
        if (findPreference11 == null || !findPreference11.getValue().equalsIgnoreCase("enable")) {
            this.mHdrSwitcher.setVisibility(0);
        } else {
            this.mHdrSwitcher.setVisibility(8);
            this.mUI.getCameraControls().removeFromViewList(this.mHdrSwitcher);
        }
        updateFilterModeIcon(listPreference, listPreference);
        if (same(listPreference, "pref_camera_recordlocation_key", RecordLocationPreference.VALUE_ON)) {
            this.mActivity.requestLocationPermission();
        }
        if (same(listPreference, CameraSettings.KEY_BOKEH_MODE, "1")) {
            updateSceneModeIcon((IconListPreference) this.mPreferenceGroup.findPreference(str2));
            changeFilterModeControlIcon("none");
            buttonSetEnabled(this.mHdrSwitcher, false);
            buttonSetEnabled(this.mSceneModeSwitcher, false);
            buttonSetEnabled(this.mFilterModeSwitcher, false);
        }
        super.onSettingChanged(listPreference);
    }

    public int getOrientation() {
        PhotoUI photoUI = this.mUI;
        if (photoUI == null) {
            return 0;
        }
        return photoUI.getOrientation();
    }

    public void hideTopMenu(boolean z) {
        if (z) {
            this.mSceneModeSwitcher.setVisibility(8);
            this.mFilterModeSwitcher.setVisibility(8);
            this.mFrontBackSwitcher.setVisibility(8);
            this.mTsMakeupSwitcher.setVisibility(8);
            return;
        }
        this.mSceneModeSwitcher.setVisibility(0);
        this.mFilterModeSwitcher.setVisibility(0);
        this.mFrontBackSwitcher.setVisibility(0);
        this.mTsMakeupSwitcher.setVisibility(0);
    }

    public void hideCameraControls(boolean z) {
        int i = z ? 4 : 0;
        this.mSettingMenu.setVisibility(i);
        this.mFrontBackSwitcher.setVisibility(i);
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            this.mTsMakeupSwitcher.setVisibility(i);
        } else {
            this.mHdrSwitcher.setVisibility(i);
        }
        this.mSceneModeSwitcher.setVisibility(i);
        this.mFilterModeSwitcher.setVisibility(i);
        if (i == 4) {
            if (this.mCameraSwitcher.getVisibility() == 0) {
                this.mWasVisibleSet.add(this.mCameraSwitcher);
            }
            this.mCameraSwitcher.setVisibility(i);
        } else if (this.mWasVisibleSet.contains(this.mCameraSwitcher)) {
            this.mCameraSwitcher.setVisibility(i);
            this.mWasVisibleSet.remove(this.mCameraSwitcher);
        }
        this.mPreviewThumbnail.setVisibility(i);
    }
}
