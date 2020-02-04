package com.android.camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera.Parameters;
import android.view.View;
import android.widget.ImageView;
import com.android.camera.util.CameraUtil;
import org.codeaurora.snapcam.C0905R;

public class OnScreenIndicators {
    public static final String SCENE_MODE_HDR_PLUS = "hdr_plus";
    private final ImageView mExposureIndicator;
    private final ImageView mFlashIndicator;
    private final ImageView mLocationIndicator;
    private final View mOnScreenIndicators;
    private final ImageView mSceneIndicator;
    private final ImageView mTimerIndicator;
    private final int[] mWBArray;
    private final ImageView mWBIndicator;

    public OnScreenIndicators(Context context, View view) {
        TypedArray obtainTypedArray = context.getResources().obtainTypedArray(C0905R.array.camera_wb_indicators);
        int length = obtainTypedArray.length();
        this.mWBArray = new int[length];
        for (int i = 0; i < length; i++) {
            this.mWBArray[i] = obtainTypedArray.getResourceId(i, C0905R.C0906drawable.ic_indicator_wb_off);
        }
        this.mOnScreenIndicators = view;
        this.mExposureIndicator = (ImageView) view.findViewById(C0905R.C0907id.menu_exposure_indicator);
        this.mFlashIndicator = (ImageView) view.findViewById(C0905R.C0907id.menu_flash_indicator);
        this.mSceneIndicator = (ImageView) view.findViewById(C0905R.C0907id.menu_scenemode_indicator);
        this.mLocationIndicator = (ImageView) view.findViewById(C0905R.C0907id.menu_location_indicator);
        this.mTimerIndicator = (ImageView) view.findViewById(C0905R.C0907id.menu_timer_indicator);
        this.mWBIndicator = (ImageView) view.findViewById(C0905R.C0907id.menu_wb_indicator);
        this.mExposureIndicator.setVisibility(8);
        this.mFlashIndicator.setVisibility(8);
        this.mSceneIndicator.setVisibility(8);
        this.mLocationIndicator.setVisibility(8);
        this.mTimerIndicator.setVisibility(8);
        this.mWBIndicator.setVisibility(8);
    }

    public void resetToDefault() {
        updateExposureOnScreenIndicator(0);
        updateFlashOnScreenIndicator("off");
        updateSceneOnScreenIndicator("auto");
        updateWBIndicator(2);
        updateTimerIndicator(false);
        updateLocationIndicator(false);
    }

    public void updateExposureOnScreenIndicator(Parameters parameters, int i) {
        if (this.mExposureIndicator != null) {
            updateExposureOnScreenIndicator(Math.round(((float) i) * parameters.getExposureCompensationStep()));
        }
    }

    public void updateExposureOnScreenIndicator(int i) {
        switch (i) {
        }
        this.mExposureIndicator.setImageResource(C0905R.C0906drawable.ic_settings);
    }

    public void updateWBIndicator(int i) {
        ImageView imageView = this.mWBIndicator;
        if (imageView != null) {
            imageView.setImageResource(this.mWBArray[i]);
        }
    }

    public void updateTimerIndicator(boolean z) {
        ImageView imageView = this.mTimerIndicator;
        if (imageView != null) {
            imageView.setImageResource(z ? C0905R.C0906drawable.ic_indicator_timer_on : C0905R.C0906drawable.ic_indicator_timer_off);
        }
    }

    public void updateLocationIndicator(boolean z) {
        ImageView imageView = this.mLocationIndicator;
        if (imageView != null) {
            imageView.setImageResource(z ? C0905R.C0906drawable.ic_indicator_loc_on : C0905R.C0906drawable.ic_indicator_loc_off);
        }
    }

    public void updateFlashOnScreenIndicator(String str) {
        if (this.mFlashIndicator != null) {
            if (str == null || "off".equals(str)) {
                this.mFlashIndicator.setImageResource(C0905R.C0906drawable.ic_indicator_flash_off);
            } else if ("auto".equals(str)) {
                this.mFlashIndicator.setImageResource(C0905R.C0906drawable.ic_indicator_flash_auto);
            } else if (RecordLocationPreference.VALUE_ON.equals(str) || "torch".equals(str)) {
                this.mFlashIndicator.setImageResource(C0905R.C0906drawable.ic_indicator_flash_on);
            } else {
                this.mFlashIndicator.setImageResource(C0905R.C0906drawable.ic_indicator_flash_off);
            }
        }
    }

    public void updateSceneOnScreenIndicator(String str) {
        if (this.mSceneIndicator != null) {
            if (SCENE_MODE_HDR_PLUS.equals(str)) {
                this.mSceneIndicator.setImageResource(C0905R.C0906drawable.ic_indicator_hdr_plus_on);
            } else if (str == null || "auto".equals(str)) {
                this.mSceneIndicator.setImageResource(C0905R.C0906drawable.ic_indicator_sce_off);
            } else if (CameraUtil.SCENE_MODE_HDR.equals(str)) {
                this.mSceneIndicator.setImageResource(C0905R.C0906drawable.ic_indicator_sce_hdr);
            } else {
                this.mSceneIndicator.setImageResource(C0905R.C0906drawable.ic_indicator_sce_on);
            }
        }
    }

    public void setVisibility(int i) {
        this.mOnScreenIndicators.setVisibility(i);
    }
}
