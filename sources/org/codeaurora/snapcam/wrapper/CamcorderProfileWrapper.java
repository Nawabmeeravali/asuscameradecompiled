package org.codeaurora.snapcam.wrapper;

import android.media.CamcorderProfile;

public class CamcorderProfileWrapper extends Wrapper {
    public static final int QUALITY_2k = Wrapper.getFieldValue(Wrapper.getField(CamcorderProfile.class, "QUALITY_2k"), -1);
    public static final int QUALITY_4KDCI = Wrapper.getFieldValue(Wrapper.getField(CamcorderProfile.class, "QUALITY_4KDCI"), -1);
    public static final int QUALITY_HIGH_SPEED_4KDCI = Wrapper.getFieldValue(Wrapper.getField(CamcorderProfile.class, "QUALITY_HIGH_SPEED_4KDCI"), -1);
    public static final int QUALITY_HIGH_SPEED_CIF = Wrapper.getFieldValue(Wrapper.getField(CamcorderProfile.class, "QUALITY_HIGH_SPEED_CIF"), -1);
    public static final int QUALITY_HIGH_SPEED_VGA = Wrapper.getFieldValue(Wrapper.getField(CamcorderProfile.class, "QUALITY_HIGH_SPEED_VGA"), -1);
    public static final int QUALITY_QHD = Wrapper.getFieldValue(Wrapper.getField(CamcorderProfile.class, "QUALITY_QHD"), -1);
    public static final int QUALITY_TIME_LAPSE_4KDCI = Wrapper.getFieldValue(Wrapper.getField(CamcorderProfile.class, "QUALITY_TIME_LAPSE_4KDCI"), -1);
    public static final int QUALITY_TIME_LAPSE_VGA = Wrapper.getFieldValue(Wrapper.getField(CamcorderProfile.class, "QUALITY_TIME_LAPSE_VGA"), -1);
    public static final int QUALITY_VGA = Wrapper.getFieldValue(Wrapper.getField(CamcorderProfile.class, "QUALITY_VGA"), -1);
}
