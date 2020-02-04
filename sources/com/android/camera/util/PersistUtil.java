package com.android.camera.util;

import android.graphics.Point;
import android.os.SystemProperties;
import com.android.camera.AnimationManager;
import com.asus.scenedetectlib.BuildConfig;

public class PersistUtil {
    public static final int CAMERA2_DEBUG_3DNDR_FEATURE = 1;
    public static final int CAMERA2_DEBUG_AI_SCENE_DETECT_FEATURE = 1;
    public static final int CAMERA2_DEBUG_DUMP_ALL = 100;
    public static final int CAMERA2_DEBUG_DUMP_IMAGE = 1;
    public static final int CAMERA2_DEBUG_DUMP_LOG = 2;
    public static final int CAMERA2_DEBUG_LAUNCH_PERFORMANCE = 1;
    public static final int CAMERA2_DEBUG_RAM_FEATURE = 1;
    private static final int CAMERA_SENSOR_HORIZONTAL_ALIGNED = 0;
    private static final int CAMERA_SENSOR_VERTICAL_ALIGNED = 1;
    private static final int CIRCULAR_BUFFER_SIZE_PERSIST = SystemProperties.getInt("persist.sys.camera.zsl.buffer.size", 3);
    private static final int PERSIST_BURST_COUNT = SystemProperties.getInt("persist.sys.camera.cs.burstcount", 4);
    private static final int PERSIST_BURST_PREVIEW_REQUEST_NUMS = SystemProperties.getInt("persist.sys.camera.burst.preview.nums", 0);
    private static final int PERSIST_CAMERA2_DEBUG = SystemProperties.getInt("persist.sys.camera2.debug", 0);
    private static final boolean PERSIST_CAMERA_CAMERA2 = SystemProperties.getBoolean("persist.sys.camera.camera2", true);
    private static final int PERSIST_CAMERA_CANCEL_TOUCHFOCUS_DELAY = SystemProperties.getInt("persist.sys.camera.focus_delay", 5000);
    private static final String PERSIST_CAMERA_CS_BRINTENSITY_KEY = SystemProperties.get("persist.sys.camera.sensor.brinten", "0.0");
    private static final String PERSIST_CAMERA_CS_SMOOTH_KEY;
    private static final int PERSIST_CAMERA_DEBUG = SystemProperties.getInt("persist.sys.camera.debug", 0);
    private static final boolean PERSIST_CAMERA_PREVIEW_RESTART_ENABLED = SystemProperties.getBoolean("persist.sys.camera.feature.restart", false);
    private static final String PERSIST_CAMERA_PREVIEW_SIZE;
    private static final boolean PERSIST_CAMERA_SAVE_IN_SD_ENABLED = SystemProperties.getBoolean("persist.sys.env.camera.saveinsd", false);
    private static final int PERSIST_CAMERA_SENSOR_ALIGN_KEY = SystemProperties.getInt("persist.sys.camera.sensor.align", 0);
    private static final String PERSIST_CAMERA_STILLMORE_BRCOLR;
    private static final String PERSIST_CAMERA_STILLMORE_BRINTENSITY = SystemProperties.get("persist.sys.camera.stm_brintensity", "0.6");
    private static final int PERSIST_CAMERA_STILLMORE_NUM_REQUIRED_IMAGE = SystemProperties.getInt("persist.sys.camera.stm_img_nums", 5);
    private static final String PERSIST_CAMERA_STILLMORE_SMOOTHINGINTENSITY = SystemProperties.get("persist.sys.camera.stm_smooth", "0");
    private static final boolean PERSIST_CAMERA_UI_AUTO_TEST_ENABLED = SystemProperties.getBoolean("persist.sys.camera.ui.auto_test", false);
    private static final boolean PERSIST_CAMERA_ZSL = SystemProperties.getBoolean("persist.sys.camera.zsl.disabled", false);
    private static final int PERSIST_CAM_AI_SCENE_DETECT_RFEATURE_DEBUG = SystemProperties.getInt("persist.sys.camera.ai.scene.detect.feature.debug", 0);
    private static final int PERSIST_CAM_FEATURE_DEBUG = SystemProperties.getInt("persist.sys.camera.feature.debug", 1);
    private static final int PERSIST_CAM_RAM_RFEATURE_DEBUG = SystemProperties.getInt("persist.sys.camera.RAM.feature.debug", 0);
    private static final boolean PERSIST_CAPTURE_ANIMATION_ENABLED = SystemProperties.getBoolean("persist.sys.camera.capture.animate", true);
    private static final int PERSIST_CS_TIMEOUT = SystemProperties.getInt("persist.sys.camera.cs.timeout", AnimationManager.FLASH_DURATION);
    private static final boolean PERSIST_DISABLE_QCOM_MISC_SETTING = SystemProperties.getBoolean("persist.sys.camera.qcom.misc.disable", false);
    private static final String PERSIST_DISPLAY_LMAX;
    private static final String PERSIST_DISPLAY_UMAX;
    private static final boolean PERSIST_DUMP_DEPTH_ENABLED = SystemProperties.getBoolean("persist.sys.camera.cs.dumpdepth", false);
    private static final boolean PERSIST_DUMP_FRAMES_ENABLED = SystemProperties.getBoolean("persist.sys.camera.cs.dumpframes", false);
    private static final boolean PERSIST_DUMP_YUV_ENABLED = SystemProperties.getBoolean("persist.sys.camera.cs.dumpyuv", false);
    private static final int PERSIST_LONGSHOT_SHOT_LIMIT = SystemProperties.getInt("persist.sys.camera.longshot.shotnum", 100);
    private static final boolean PERSIST_LONG_SAVE_ENABLED = SystemProperties.getBoolean("persist.sys.camera.longshot.save", false);
    private static final int PERSIST_MEMORY_LIMIT;
    private static final int PERSIST_PICTURE_FLIP_VALUE = SystemProperties.getInt("persist.sys.debug.camera.picture.flip", 0);
    private static final int PERSIST_PREVIEW_SIZE;
    private static final boolean PERSIST_SEND_REQUEST_AFTER_FLUSH = SystemProperties.getBoolean("persist.sys.camera.send_request_after_flush", false);
    private static final boolean PERSIST_SKIP_MEMORY_CHECK;
    private static final boolean PERSIST_SKIP_MEM_CHECK_ENABLED;
    private static final int PERSIST_STORAGE_DEBUG = SystemProperties.getInt("persist.sys.camera.storage.debug", 0);
    private static final long PERSIST_TIMESTAMP_LIMIT = SystemProperties.getLong("persist.sys.camera.cs.threshold", 10);
    private static final int PERSIST_VIDEO_FLIP_VALUE = SystemProperties.getInt("persist.sys.debug.camera.video.flip", 0);
    private static final boolean PERSIST_YV_12_FORMAT_ENABLED = SystemProperties.getBoolean("persist.sys.camera.debug.camera.yv12", false);
    private static final boolean PERSIST_ZZHDR_ENABLED = SystemProperties.getBoolean("persist.sys.camera.zzhdr.enable", false);
    private static final int PREVIEW_FLIP_VALUE = SystemProperties.getInt("persist.sys.debug.camera.preview.flip", 0);
    private static final int SAVE_TASK_MEMORY_LIMIT_IN_MB;

    static {
        String str = "persist.sys.camera.perf.memlimit";
        PERSIST_MEMORY_LIMIT = SystemProperties.getInt(str, 60);
        String str2 = "persist.sys.camera.perf.skip_memck";
        PERSIST_SKIP_MEMORY_CHECK = SystemProperties.getBoolean(str2, false);
        String str3 = "persist.sys.camera.preview.size";
        String str4 = BuildConfig.FLAVOR;
        PERSIST_CAMERA_PREVIEW_SIZE = SystemProperties.get(str3, str4);
        String str5 = "0.5";
        PERSIST_CAMERA_STILLMORE_BRCOLR = SystemProperties.get("persist.sys.camera.stm_brcolor", str5);
        PERSIST_CAMERA_CS_SMOOTH_KEY = SystemProperties.get("persist.sys.camera.sensor.smooth", str5);
        SAVE_TASK_MEMORY_LIMIT_IN_MB = SystemProperties.getInt(str, 60);
        PERSIST_SKIP_MEM_CHECK_ENABLED = SystemProperties.getBoolean(str2, false);
        PERSIST_PREVIEW_SIZE = SystemProperties.getInt(str3, 0);
        PERSIST_DISPLAY_UMAX = SystemProperties.get("persist.sys.camera.display.umax", str4);
        PERSIST_DISPLAY_LMAX = SystemProperties.get("persist.sys.camera.display.lmax", str4);
    }

    public static int getStorageDebug() {
        return PERSIST_STORAGE_DEBUG;
    }

    public static int getFeatureDebug() {
        return PERSIST_CAM_FEATURE_DEBUG;
    }

    public static int getAISceneDetectFeatureDebug() {
        return PERSIST_CAM_AI_SCENE_DETECT_RFEATURE_DEBUG;
    }

    public static int getRAMFeatureDebug() {
        return PERSIST_CAM_RAM_RFEATURE_DEBUG;
    }

    public static int getMemoryLimit() {
        return PERSIST_MEMORY_LIMIT;
    }

    public static boolean getSkipMemoryCheck() {
        return PERSIST_SKIP_MEMORY_CHECK;
    }

    public static int getLongshotShotLimit() {
        return PERSIST_LONGSHOT_SHOT_LIMIT;
    }

    public static int getLongshotShotLimit(int i) {
        return SystemProperties.getInt("persist.sys.camera.longshot.shotnum", i);
    }

    public static Point getCameraPreviewSize() {
        String str = PERSIST_CAMERA_PREVIEW_SIZE;
        if (str != null) {
            String[] split = str.split("x");
            if (split != null && split.length >= 2) {
                Point point = new Point();
                point.x = Integer.parseInt(split[0]);
                point.y = Integer.parseInt(split[1]);
                return point;
            }
        }
        return null;
    }

    public static boolean getCamera2Mode() {
        return PERSIST_CAMERA_CAMERA2;
    }

    public static boolean getCameraZSLDisabled() {
        return PERSIST_CAMERA_ZSL;
    }

    public static int getCamera2Debug() {
        return PERSIST_CAMERA_DEBUG;
    }

    public static float getStillmoreBrColor() {
        float parseFloat = Float.parseFloat(PERSIST_CAMERA_STILLMORE_BRCOLR);
        if (parseFloat < 0.0f || parseFloat > 1.0f) {
            return 0.5f;
        }
        return parseFloat;
    }

    public static float getStillmoreBrIntensity() {
        float parseFloat = Float.parseFloat(PERSIST_CAMERA_STILLMORE_BRINTENSITY);
        if (parseFloat < 0.0f || parseFloat > 1.0f) {
            return 0.6f;
        }
        return parseFloat;
    }

    public static float getStillmoreSmoothingIntensity() {
        float parseFloat = Float.parseFloat(PERSIST_CAMERA_STILLMORE_SMOOTHINGINTENSITY);
        if (parseFloat < 0.0f || parseFloat > 1.0f) {
            return 0.0f;
        }
        return parseFloat;
    }

    public static int getStillmoreNumRequiredImages() {
        int i = PERSIST_CAMERA_STILLMORE_NUM_REQUIRED_IMAGE;
        if (i < 3 || i > 5) {
            return 5;
        }
        return i;
    }

    public static int getCancelTouchFocusDelay() {
        return PERSIST_CAMERA_CANCEL_TOUCHFOCUS_DELAY;
    }

    public static float getDualCameraBrIntensity() {
        return Float.parseFloat(PERSIST_CAMERA_CS_BRINTENSITY_KEY);
    }

    public static float getDualCameraSmoothingIntensity() {
        return Float.parseFloat(PERSIST_CAMERA_CS_SMOOTH_KEY);
    }

    public static boolean getDualCameraSensorAlign() {
        return PERSIST_CAMERA_SENSOR_ALIGN_KEY == 1;
    }

    public static int getCircularBufferSize() {
        return CIRCULAR_BUFFER_SIZE_PERSIST;
    }

    public static int isBurstShotFpsNums() {
        return PERSIST_BURST_PREVIEW_REQUEST_NUMS;
    }

    public static int getSaveTaskMemoryLimitInMb() {
        return SAVE_TASK_MEMORY_LIMIT_IN_MB;
    }

    public static boolean isAutoTestEnabled() {
        return PERSIST_CAMERA_UI_AUTO_TEST_ENABLED;
    }

    public static boolean isSaveInSdEnabled() {
        return PERSIST_CAMERA_SAVE_IN_SD_ENABLED;
    }

    public static boolean isLongSaveEnabled() {
        return PERSIST_LONG_SAVE_ENABLED;
    }

    public static boolean isPreviewRestartEnabled() {
        return PERSIST_CAMERA_PREVIEW_RESTART_ENABLED;
    }

    public static boolean isCaptureAnimationEnabled() {
        return PERSIST_CAPTURE_ANIMATION_ENABLED;
    }

    public static boolean isSkipMemoryCheckEnabled() {
        return PERSIST_SKIP_MEM_CHECK_ENABLED;
    }

    public static boolean isZzhdrEnabled() {
        return PERSIST_ZZHDR_ENABLED;
    }

    public static boolean isSendRequestAfterFlush() {
        return PERSIST_SEND_REQUEST_AFTER_FLUSH;
    }

    public static int getPreviewSize() {
        return PERSIST_PREVIEW_SIZE;
    }

    public static long getTimestampLimit() {
        return PERSIST_TIMESTAMP_LIMIT;
    }

    public static int getImageToBurst() {
        return PERSIST_BURST_COUNT;
    }

    public static boolean isDumpFramesEnabled() {
        return PERSIST_DUMP_FRAMES_ENABLED;
    }

    public static boolean isDumpYUVEnabled() {
        return PERSIST_DUMP_YUV_ENABLED;
    }

    public static int getClearSightTimeout() {
        return PERSIST_CS_TIMEOUT;
    }

    public static boolean isDumpDepthEnabled() {
        return PERSIST_DUMP_DEPTH_ENABLED;
    }

    public static boolean isDisableQcomMiscSetting() {
        return PERSIST_DISABLE_QCOM_MISC_SETTING;
    }

    public static int getPreviewFlip() {
        return PREVIEW_FLIP_VALUE;
    }

    public static int getVideoFlip() {
        return PERSIST_VIDEO_FLIP_VALUE;
    }

    public static int getPictureFlip() {
        return PERSIST_PICTURE_FLIP_VALUE;
    }

    public static boolean isYv12FormatEnable() {
        return PERSIST_YV_12_FORMAT_ENABLED;
    }

    public static String getDisplayUMax() {
        return PERSIST_DISPLAY_UMAX;
    }

    public static String getDisplayLMax() {
        return PERSIST_DISPLAY_LMAX;
    }
}
