package org.codeaurora.snapcam.wrapper;

import android.hardware.Camera.CameraInfo;

public class CameraInfoWrapper extends Wrapper {
    public static final int CAMERA_SUPPORT_MODE_NONZSL = Wrapper.getFieldValue(Wrapper.getField(CameraInfo.class, "CAMERA_SUPPORT_MODE_NONZSL"), 3);
    public static final int CAMERA_SUPPORT_MODE_ZSL = Wrapper.getFieldValue(Wrapper.getField(CameraInfo.class, "CAMERA_SUPPORT_MODE_ZSL"), 2);
    private static final String TAG = "CameraInfo";
}
