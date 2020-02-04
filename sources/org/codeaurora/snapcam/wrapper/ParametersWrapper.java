package org.codeaurora.snapcam.wrapper;

import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.List;

public class ParametersWrapper extends Wrapper {
    public static final String DENOISE_OFF;
    public static final String DENOISE_ON;
    public static final String FACE_DETECTION_OFF;
    public static final String FACE_DETECTION_ON;
    public static final String FOCUS_MODE_MANUAL_POSITION = Wrapper.getFieldValue(Wrapper.getField(Parameters.class, "FOCUS_MODE_MANUAL_POSITION"), "manual");
    public static final String ISO_AUTO = Wrapper.getFieldValue(Wrapper.getField(Parameters.class, "ISO_AUTO"), "auto");
    private static final String TAG = "ParametersWrapper";
    public static final String TOUCH_AF_AEC_OFF;
    public static final String TOUCH_AF_AEC_ON;
    public static final String ZSL_OFF;
    private static Method method_getAutoExposure = null;
    private static Method method_getContrast = null;
    private static Method method_getISOValue = null;
    private static Method method_getMaxContrast = null;
    private static Method method_getMaxSaturation = null;
    private static Method method_getMaxSharpness = null;
    private static Method method_getPowerMode = null;
    private static Method method_getSaturation = null;
    private static Method method_getSharpness = null;
    private static Method method_getSupportedAutoexposure = null;
    private static Method method_getSupportedDenoiseModes = null;
    private static Method method_getSupportedFaceDetectionModes = null;
    private static Method method_getSupportedHfrSizes = null;
    private static Method method_getSupportedHistogramModes = null;
    private static Method method_getSupportedIsoValues = null;
    private static Method method_getSupportedRedeyeReductionModes = null;
    private static Method method_getSupportedSelectableZoneAf = null;
    private static Method method_getSupportedTouchAfAec = null;
    private static Method method_getSupportedVideoHDRModes = null;
    private static Method method_getSupportedVideoHighFrameRateModes = null;
    private static Method method_getSupportedVideoRotationValues = null;
    private static Method method_getSupportedZSLModes = null;
    private static Method method_getTouchAfAec = null;
    private static Method method_getVideoHDRMode = null;
    private static Method method_getVideoHighFrameRate = null;
    private static Method method_isPowerModeSupported = null;
    private static Method method_setAutoExposure = null;
    private static Method method_setCameraMode = null;
    private static Method method_setContrast = null;
    private static Method method_setDenoise = null;
    private static Method method_setFaceDetectionMode = null;
    private static Method method_setISOValue = null;
    private static Method method_setPowerMode = null;
    private static Method method_setRedeyeReductionMode = null;
    private static Method method_setSaturation = null;
    private static Method method_setSelectableZoneAf = null;
    private static Method method_setSharpness = null;
    private static Method method_setTouchAfAec = null;
    private static Method method_setVideoHDRMode = null;
    private static Method method_setVideoHighFrameRate = null;
    private static Method method_setVideoRotation = null;
    private static Method method_setZSLMode = null;

    static {
        String str = "off";
        FACE_DETECTION_ON = Wrapper.getFieldValue(Wrapper.getField(Parameters.class, "FACE_DETECTION_ON"), str);
        FACE_DETECTION_OFF = Wrapper.getFieldValue(Wrapper.getField(Parameters.class, "FACE_DETECTION_OFF"), str);
        ZSL_OFF = Wrapper.getFieldValue(Wrapper.getField(Parameters.class, "ZSL_OFF"), str);
        String str2 = "touch-off";
        TOUCH_AF_AEC_ON = Wrapper.getFieldValue(Wrapper.getField(Parameters.class, "TOUCH_AF_AEC_ON"), str2);
        TOUCH_AF_AEC_OFF = Wrapper.getFieldValue(Wrapper.getField(Parameters.class, "TOUCH_AF_AEC_OFF"), str2);
        String str3 = "denoise-off";
        DENOISE_OFF = Wrapper.getFieldValue(Wrapper.getField(Parameters.class, "DENOISE_OFF"), str3);
        DENOISE_ON = Wrapper.getFieldValue(Wrapper.getField(Parameters.class, "DENOISE_ON"), str3);
    }

    public static boolean isPowerModeSupported(Parameters parameters) {
        boolean z = false;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no isPowerModeSupported");
            Log.e(TAG, sb.toString());
            return false;
        }
        try {
            if (method_isPowerModeSupported == null) {
                method_isPowerModeSupported = Parameters.class.getDeclaredMethod("isPowerModeSupported", new Class[0]);
            }
            z = ((Boolean) method_isPowerModeSupported.invoke(parameters, new Object[0])).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return z;
    }

    public static void setPowerMode(Parameters parameters, String str) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setPowerMode");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setPowerMode == null) {
                method_setPowerMode = Parameters.class.getDeclaredMethod("setPowerMode", new Class[]{String.class});
            }
            method_setPowerMode.invoke(parameters, new Object[]{str});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getPowerMode(Parameters parameters) {
        String str;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getPowerMode");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getPowerMode == null) {
                method_getPowerMode = Parameters.class.getDeclaredMethod("getPowerMode", new Class[0]);
            }
            str = (String) method_getPowerMode.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            str = null;
        }
        return str;
    }

    public static void setCameraMode(Parameters parameters, int i) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setCameraMode");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setCameraMode == null) {
                method_setCameraMode = Parameters.class.getDeclaredMethod("setCameraMode", new Class[]{Integer.TYPE});
            }
            method_setCameraMode.invoke(parameters, new Object[]{Integer.valueOf(i)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getSupportedIsoValues(Parameters parameters) {
        List<String> list;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSupportedIsoValues");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getSupportedIsoValues == null) {
                method_getSupportedIsoValues = Parameters.class.getDeclaredMethod("getSupportedIsoValues", new Class[0]);
            }
            list = (List) method_getSupportedIsoValues.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static String getISOValue(Parameters parameters) {
        String str;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getISOValue");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getISOValue == null) {
                method_getISOValue = Parameters.class.getDeclaredMethod("getISOValue", new Class[0]);
            }
            str = (String) method_getISOValue.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            str = null;
        }
        return str;
    }

    public static void setISOValue(Parameters parameters, String str) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setISOValue");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setISOValue == null) {
                method_setISOValue = Parameters.class.getDeclaredMethod("setISOValue", new Class[]{String.class});
            }
            method_setISOValue.invoke(parameters, new Object[]{str});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getSupportedAutoexposure(Parameters parameters) {
        List<String> list;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSupportedAutoexposure");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getSupportedAutoexposure == null) {
                method_getSupportedAutoexposure = Parameters.class.getDeclaredMethod("getSupportedAutoexposure", new Class[0]);
            }
            list = (List) method_getSupportedAutoexposure.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static String getAutoExposure(Parameters parameters) {
        String str;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getAutoExposure");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getAutoExposure == null) {
                method_getAutoExposure = Parameters.class.getDeclaredMethod("getAutoExposure", new Class[0]);
            }
            str = (String) method_getAutoExposure.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            str = null;
        }
        return str;
    }

    public static void setAutoExposure(Parameters parameters, String str) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setAutoExposure");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setAutoExposure == null) {
                method_setAutoExposure = Parameters.class.getDeclaredMethod("setAutoExposure", new Class[]{String.class});
            }
            method_setAutoExposure.invoke(parameters, new Object[]{str});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getSupportedTouchAfAec(Parameters parameters) {
        List<String> list;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSupportedTouchAfAec");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getSupportedTouchAfAec == null) {
                method_getSupportedTouchAfAec = Parameters.class.getDeclaredMethod("getSupportedTouchAfAec", new Class[0]);
            }
            list = (List) method_getSupportedTouchAfAec.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static String getTouchAfAec(Parameters parameters) {
        String str;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getTouchAfAec");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getTouchAfAec == null) {
                method_getTouchAfAec = Parameters.class.getDeclaredMethod("getTouchAfAec", new Class[0]);
            }
            str = (String) method_getTouchAfAec.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            str = null;
        }
        return str;
    }

    public static void setTouchAfAec(Parameters parameters, String str) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setTouchAfAec");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setTouchAfAec == null) {
                method_setTouchAfAec = Parameters.class.getDeclaredMethod("setTouchAfAec", new Class[]{String.class});
            }
            method_setTouchAfAec.invoke(parameters, new Object[]{str});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getSupportedSelectableZoneAf(Parameters parameters) {
        List<String> list;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSupportedSelectableZoneAf");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getSupportedSelectableZoneAf == null) {
                method_getSupportedSelectableZoneAf = Parameters.class.getDeclaredMethod("getSupportedSelectableZoneAf", new Class[0]);
            }
            list = (List) method_getSupportedSelectableZoneAf.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static void setSelectableZoneAf(Parameters parameters, String str) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setSelectableZoneAf");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setSelectableZoneAf == null) {
                method_setSelectableZoneAf = Parameters.class.getDeclaredMethod("setSelectableZoneAf", new Class[]{String.class});
            }
            method_setSelectableZoneAf.invoke(parameters, new Object[]{str});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getSupportedRedeyeReductionModes(Parameters parameters) {
        List<String> list;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSupportedRedeyeReductionModes");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getSupportedRedeyeReductionModes == null) {
                method_getSupportedRedeyeReductionModes = Parameters.class.getDeclaredMethod("getSupportedRedeyeReductionModes", new Class[0]);
            }
            list = (List) method_getSupportedRedeyeReductionModes.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static void setRedeyeReductionMode(Parameters parameters, String str) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setRedeyeReductionMode");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setRedeyeReductionMode == null) {
                method_setRedeyeReductionMode = Parameters.class.getDeclaredMethod("setRedeyeReductionMode", new Class[]{String.class});
            }
            method_setRedeyeReductionMode.invoke(parameters, new Object[]{str});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getSupportedDenoiseModes(Parameters parameters) {
        List<String> list;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSupportedDenoiseModes");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getSupportedDenoiseModes == null) {
                method_getSupportedDenoiseModes = Parameters.class.getDeclaredMethod("getSupportedDenoiseModes", new Class[0]);
            }
            list = (List) method_getSupportedDenoiseModes.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static void setDenoise(Parameters parameters, String str) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setDenoise");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setDenoise == null) {
                method_setDenoise = Parameters.class.getDeclaredMethod("setDenoise", new Class[]{String.class});
            }
            method_setDenoise.invoke(parameters, new Object[]{str});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getSupportedVideoHDRModes(Parameters parameters) {
        List<String> list;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSupportedVideoHDRModes");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getSupportedVideoHDRModes == null) {
                method_getSupportedVideoHDRModes = Parameters.class.getDeclaredMethod("getSupportedVideoHDRModes", new Class[0]);
            }
            list = (List) method_getSupportedVideoHDRModes.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static String getVideoHDRMode(Parameters parameters) {
        String str;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getVideoHDRMode");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getVideoHDRMode == null) {
                method_getVideoHDRMode = Parameters.class.getDeclaredMethod("getVideoHDRMode", new Class[0]);
            }
            str = (String) method_getVideoHDRMode.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            str = null;
        }
        return str;
    }

    public static void setVideoHDRMode(Parameters parameters, String str) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setVideoHDRMode");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setVideoHDRMode == null) {
                method_setVideoHDRMode = Parameters.class.getDeclaredMethod("setVideoHDRMode", new Class[]{String.class});
            }
            method_setVideoHDRMode.invoke(parameters, new Object[]{str});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getSupportedHistogramModes(Parameters parameters) {
        List<String> list;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSupportedHistogramModes");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getSupportedHistogramModes == null) {
                method_getSupportedHistogramModes = Parameters.class.getDeclaredMethod("getSupportedHistogramModes", new Class[0]);
            }
            list = (List) method_getSupportedHistogramModes.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static List<Size> getSupportedHfrSizes(Parameters parameters) {
        List<Size> list;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSupportedHfrSizes");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getSupportedHfrSizes == null) {
                method_getSupportedHfrSizes = Parameters.class.getDeclaredMethod("getSupportedHfrSizes", new Class[0]);
            }
            list = (List) method_getSupportedHfrSizes.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static List<String> getSupportedVideoHighFrameRateModes(Parameters parameters) {
        List<String> list;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSupportedVideoHighFrameRateModes");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getSupportedVideoHighFrameRateModes == null) {
                method_getSupportedVideoHighFrameRateModes = Parameters.class.getDeclaredMethod("getSupportedVideoHighFrameRateModes", new Class[0]);
            }
            list = (List) method_getSupportedVideoHighFrameRateModes.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static String getVideoHighFrameRate(Parameters parameters) {
        String str;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getVideoHighFrameRate");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getVideoHighFrameRate == null) {
                method_getVideoHighFrameRate = Parameters.class.getDeclaredMethod("getVideoHighFrameRate", new Class[0]);
            }
            str = (String) method_getVideoHighFrameRate.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            str = null;
        }
        return str;
    }

    public static void setVideoHighFrameRate(Parameters parameters, String str) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setVideoHighFrameRate");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setVideoHighFrameRate == null) {
                method_setVideoHighFrameRate = Parameters.class.getDeclaredMethod("setVideoHighFrameRate", new Class[]{String.class});
            }
            method_setVideoHighFrameRate.invoke(parameters, new Object[]{str});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getSupportedVideoRotationValues(Parameters parameters) {
        List<String> list;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSupportedVideoRotationValues");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getSupportedVideoRotationValues == null) {
                method_getSupportedVideoRotationValues = Parameters.class.getDeclaredMethod("getSupportedVideoRotationValues", new Class[0]);
            }
            list = (List) method_getSupportedVideoRotationValues.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static void setVideoRotation(Parameters parameters, String str) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setVideoRotation");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setVideoRotation == null) {
                method_setVideoRotation = Parameters.class.getDeclaredMethod("setVideoRotation", new Class[]{String.class});
            }
            method_setVideoRotation.invoke(parameters, new Object[]{str});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setFaceDetectionMode(Parameters parameters, String str) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setFaceDetectionMode");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setFaceDetectionMode == null) {
                method_setFaceDetectionMode = Parameters.class.getDeclaredMethod("setFaceDetectionMode", new Class[]{String.class});
            }
            method_setFaceDetectionMode.invoke(parameters, new Object[]{str});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getSupportedFaceDetectionModes(Parameters parameters) {
        List<String> list;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSupportedFaceDetectionModes");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getSupportedFaceDetectionModes == null) {
                method_getSupportedFaceDetectionModes = Parameters.class.getDeclaredMethod("getSupportedFaceDetectionModes", new Class[0]);
            }
            list = (List) method_getSupportedFaceDetectionModes.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static List<String> getSupportedZSLModes(Parameters parameters) {
        List<String> list;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSupportedZSLModes");
            Log.e(TAG, sb.toString());
            return null;
        }
        try {
            if (method_getSupportedZSLModes == null) {
                method_getSupportedZSLModes = Parameters.class.getDeclaredMethod("getSupportedZSLModes", new Class[0]);
            }
            list = (List) method_getSupportedZSLModes.invoke(parameters, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static void setZSLMode(Parameters parameters, String str) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setZSLMode");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setZSLMode == null) {
                method_setZSLMode = Parameters.class.getDeclaredMethod("setZSLMode", new Class[]{String.class});
            }
            method_setZSLMode.invoke(parameters, new Object[]{str});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getSharpness(Parameters parameters) {
        int i = -1;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSharpness");
            Log.e(TAG, sb.toString());
            return -1;
        }
        try {
            if (method_getSharpness == null) {
                method_getSharpness = Parameters.class.getDeclaredMethod("getSharpness", new Class[0]);
            }
            i = ((Integer) method_getSharpness.invoke(parameters, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public static void setSharpness(Parameters parameters, int i) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setSharpness");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setSharpness == null) {
                method_setSharpness = Parameters.class.getDeclaredMethod("setSharpness", new Class[]{Integer.TYPE});
            }
            method_setSharpness.invoke(parameters, new Object[]{Integer.valueOf(i)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getMaxSharpness(Parameters parameters) {
        int i = -1;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getMaxSharpness");
            Log.e(TAG, sb.toString());
            return -1;
        }
        try {
            if (method_getMaxSharpness == null) {
                method_getMaxSharpness = Parameters.class.getDeclaredMethod("getMaxSharpness", new Class[0]);
            }
            i = ((Integer) method_getMaxSharpness.invoke(parameters, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public static int getSaturation(Parameters parameters) {
        int i = -1;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getSaturation");
            Log.e(TAG, sb.toString());
            return -1;
        }
        try {
            if (method_getSaturation == null) {
                method_getSaturation = Parameters.class.getDeclaredMethod("getSaturation", new Class[0]);
            }
            i = ((Integer) method_getSaturation.invoke(parameters, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public static void setSaturation(Parameters parameters, int i) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setSaturation");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setSaturation == null) {
                method_setSaturation = Parameters.class.getDeclaredMethod("setSaturation", new Class[]{Integer.TYPE});
            }
            method_setSaturation.invoke(parameters, new Object[]{Integer.valueOf(i)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getMaxSaturation(Parameters parameters) {
        int i = -1;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getMaxSaturation");
            Log.e(TAG, sb.toString());
            return -1;
        }
        try {
            if (method_getMaxSaturation == null) {
                method_getMaxSaturation = Parameters.class.getDeclaredMethod("getMaxSaturation", new Class[0]);
            }
            i = ((Integer) method_getMaxSaturation.invoke(parameters, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public static int getContrast(Parameters parameters) {
        int i = -1;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getContrast");
            Log.e(TAG, sb.toString());
            return -1;
        }
        try {
            if (method_getContrast == null) {
                method_getContrast = Parameters.class.getDeclaredMethod("getContrast", new Class[0]);
            }
            i = ((Integer) method_getContrast.invoke(parameters, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public static void setContrast(Parameters parameters, int i) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no setContrast");
            Log.e(TAG, sb.toString());
            return;
        }
        try {
            if (method_setContrast == null) {
                method_setContrast = Parameters.class.getDeclaredMethod("setContrast", new Class[]{Integer.TYPE});
            }
            method_setContrast.invoke(parameters, new Object[]{Integer.valueOf(i)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getMaxContrast(Parameters parameters) {
        int i = -1;
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:");
            sb.append(Parameters.class);
            sb.append(" no getMaxContrast");
            Log.e(TAG, sb.toString());
            return -1;
        }
        try {
            if (method_getMaxContrast == null) {
                method_getMaxContrast = Parameters.class.getDeclaredMethod("getMaxContrast", new Class[0]);
            }
            i = ((Integer) method_getMaxContrast.invoke(parameters, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }
}
