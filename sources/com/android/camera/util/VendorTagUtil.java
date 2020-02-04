package com.android.camera.util;

import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureRequest.Key;
import android.util.Log;

public class VendorTagUtil {
    private static Key<Integer> CdsModeKey = new Key<>("org.codeaurora.qcamera3.CDS.cds_mode", Integer.class);
    private static Key<Long> ISO_EXP = new Key<>("org.codeaurora.qcamera3.iso_exp_priority.use_iso_exp_priority", Long.class);
    private static Key<Byte> JpegCropEnableKey = new Key<>("org.codeaurora.qcamera3.jpeg_encode_crop.enable", Byte.class);
    private static Key<int[]> JpegCropRectKey = new Key<>("org.codeaurora.qcamera3.jpeg_encode_crop.rect", int[].class);
    private static Key<int[]> JpegRoiRectKey = new Key<>("org.codeaurora.qcamera3.jpeg_encode_crop.roi", int[].class);
    private static final int MANUAL_WB_CCT_MODE = 1;
    private static final int MANUAL_WB_DISABLE_MODE = 0;
    private static Key<float[]> MANUAL_WB_GAINS = new Key<>("org.codeaurora.qcamera3.manualWB.gains", float[].class);
    private static final int MANUAL_WB_GAINS_MODE = 2;
    private static Key<Integer> PARTIAL_MANUAL_WB_MODE = new Key<>("org.codeaurora.qcamera3.manualWB.partial_mwb_mode", Integer.class);
    private static Key<Integer> SELECT_PRIORITY = new Key<>("org.codeaurora.qcamera3.iso_exp_priority.select_priority", Integer.class);
    private static final String TAG = "VendorTagUtil";
    private static Key<Integer> USE_ISO_VALUE = new Key<>("org.codeaurora.qcamera3.iso_exp_priority.use_iso_value", Integer.class);
    private static Key<Integer> WB_COLOR_TEMPERATURE = new Key<>("org.codeaurora.qcamera3.manualWB.color_temperature", Integer.class);

    private static boolean isSupported(Builder builder, Key<?> key) {
        boolean z;
        String str = "vendor tag ";
        String str2 = TAG;
        try {
            builder.get(key);
            z = true;
        } catch (IllegalArgumentException unused) {
            z = false;
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(key.getName());
            sb.append(" is not supported");
            Log.d(str2, sb.toString());
        }
        if (z) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str);
            sb2.append(key.getName());
            sb2.append(" is supported");
            Log.d(str2, sb2.toString());
        }
        return z;
    }

    public static void setCdsMode(Builder builder, Integer num) {
        if (isCdsModeSupported(builder)) {
            builder.set(CdsModeKey, num);
        }
    }

    private static boolean isCdsModeSupported(Builder builder) {
        return isSupported(builder, CdsModeKey);
    }

    public static void setJpegCropEnable(Builder builder, Byte b) {
        if (isJpegCropEnableSupported(builder)) {
            builder.set(JpegCropEnableKey, b);
        }
    }

    private static boolean isJpegCropEnableSupported(Builder builder) {
        return isSupported(builder, JpegCropEnableKey);
    }

    public static void setJpegCropRect(Builder builder, int[] iArr) {
        if (isJpegCropRectSupported(builder)) {
            builder.set(JpegCropRectKey, iArr);
        }
    }

    private static boolean isJpegCropRectSupported(Builder builder) {
        return isSupported(builder, JpegCropRectKey);
    }

    public static void setJpegRoiRect(Builder builder, int[] iArr) {
        if (isJpegRoiRectSupported(builder)) {
            builder.set(JpegRoiRectKey, iArr);
        }
    }

    private static boolean isJpegRoiRectSupported(Builder builder) {
        return isSupported(builder, JpegRoiRectKey);
    }

    public static void setIsoExpPrioritySelectPriority(Builder builder, Integer num) {
        if (isIsoExpPrioritySelectPrioritySupported(builder)) {
            builder.set(SELECT_PRIORITY, num);
        }
    }

    private static boolean isIsoExpPrioritySelectPrioritySupported(Builder builder) {
        return isSupported(builder, SELECT_PRIORITY);
    }

    public static void setIsoExpPriority(Builder builder, Long l) {
        if (isIsoExpPrioritySupported(builder)) {
            builder.set(ISO_EXP, l);
        }
    }

    private static boolean isIsoExpPrioritySupported(Builder builder) {
        return isSupported(builder, ISO_EXP);
    }

    private static boolean isPartialWBModeSupported(Builder builder) {
        return isSupported(builder, PARTIAL_MANUAL_WB_MODE);
    }

    private static boolean isWBTemperatureSupported(Builder builder) {
        return isSupported(builder, WB_COLOR_TEMPERATURE);
    }

    private static boolean isMWBGainsSupported(Builder builder) {
        return isSupported(builder, MANUAL_WB_GAINS);
    }

    public static void setWbColorTemperatureValue(Builder builder, Integer num) {
        if (isPartialWBModeSupported(builder)) {
            builder.set(PARTIAL_MANUAL_WB_MODE, Integer.valueOf(1));
            if (isWBTemperatureSupported(builder)) {
                builder.set(WB_COLOR_TEMPERATURE, num);
            }
        }
    }

    public static void setMWBGainsValue(Builder builder, float[] fArr) {
        if (isPartialWBModeSupported(builder)) {
            builder.set(PARTIAL_MANUAL_WB_MODE, Integer.valueOf(2));
            if (isMWBGainsSupported(builder)) {
                builder.set(MANUAL_WB_GAINS, fArr);
            }
        }
    }

    public static void setMWBDisableMode(Builder builder) {
        if (isPartialWBModeSupported(builder)) {
            builder.set(PARTIAL_MANUAL_WB_MODE, Integer.valueOf(0));
        }
    }

    public static void setUseIsoValues(Builder builder, long j) {
        if (isUseIsoValueSupported(builder)) {
            builder.set(ISO_EXP, Long.valueOf(j));
        }
    }

    private static boolean isUseIsoValueSupported(Builder builder) {
        return isSupported(builder, ISO_EXP);
    }
}
