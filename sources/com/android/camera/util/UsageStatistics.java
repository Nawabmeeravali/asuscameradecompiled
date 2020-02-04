package com.android.camera.util;

import android.content.Context;
import com.asus.scenedetectlib.BuildConfig;

public class UsageStatistics {
    public static final String ACTION_CAPTURE_DONE = "CaptureDone";
    public static final String ACTION_CAPTURE_FAIL = "CaptureFail";
    public static final String ACTION_CAPTURE_START = "CaptureStart";
    public static final String ACTION_CROP = "Crop";
    public static final String ACTION_DELETE = "Delete";
    public static final String ACTION_EDIT = "Edit";
    public static final String ACTION_FILMSTRIP = "Filmstrip";
    public static final String ACTION_FOREGROUNDED = "Foregrounded";
    public static final String ACTION_GALLERY = "Gallery";
    public static final String ACTION_OPEN_FAIL = "OpenFailure";
    public static final String ACTION_PLAY_VIDEO = "PlayVideo";
    public static final String ACTION_SCREEN_CHANGED = "ScreenChanged";
    public static final String ACTION_SHARE = "Share";
    public static final String ACTION_START_PREVIEW_FAIL = "StartPreviewFailure";
    public static final String ACTION_STITCHING_DONE = "StitchingDone";
    public static final String ACTION_STITCHING_START = "StitchingStart";
    public static final String ACTION_TOUCH_FOCUS = "TouchFocus";
    public static final String CATEGORY_BUTTON_PRESS = "ButtonPress";
    public static final String CATEGORY_LIFECYCLE = "AppLifecycle";
    public static final String COMPONENT_CAMERA = "Camera";
    public static final String COMPONENT_EDITOR = "Editor";
    public static final String COMPONENT_GALLERY = "Gallery";
    public static final String COMPONENT_GCAM = "GCam";
    public static final String COMPONENT_IMPORTER = "Importer";
    public static final String COMPONENT_LIGHTCYCLE = "Lightcycle";
    public static final String COMPONENT_PANORAMA = "Panorama";
    public static final String LIFECYCLE_START = "Start";
    public static final String TRANSITION_BACK_BUTTON = "BackButton";
    public static final String TRANSITION_BUTTON_TAP = "ButtonTap";
    public static final String TRANSITION_INTENT = "Intent";
    public static final String TRANSITION_ITEM_TAP = "ItemTap";
    public static final String TRANSITION_MENU_TAP = "MenuTap";
    public static final String TRANSITION_PINCH_IN = "PinchIn";
    public static final String TRANSITION_PINCH_OUT = "PinchOut";
    public static final String TRANSITION_SWIPE = "Swipe";
    public static final String TRANSITION_UP_BUTTON = "UpButton";

    public static String hashFileName(String str) {
        return BuildConfig.FLAVOR;
    }

    public static void initialize(Context context) {
    }

    public static void onContentViewChanged(String str, String str2) {
    }

    public static void onEvent(String str, String str2, String str3) {
    }

    public static void onEvent(String str, String str2, String str3, long j) {
    }

    public static void onEvent(String str, String str2, String str3, long j, String str4) {
    }

    public static void onEvent(String str, String str2, String str3, long j, String str4, String str5) {
    }

    public static void setPendingTransitionCause(String str) {
    }
}
