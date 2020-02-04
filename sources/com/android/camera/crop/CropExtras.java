package com.android.camera.crop;

import android.net.Uri;

public class CropExtras {
    public static final String KEY_ASPECT_X = "aspectX";
    public static final String KEY_ASPECT_Y = "aspectY";
    public static final String KEY_CROPPED_RECT = "cropped-rect";
    public static final String KEY_DATA = "data";
    public static final String KEY_OUTPUT_FORMAT = "outputFormat";
    public static final String KEY_OUTPUT_X = "outputX";
    public static final String KEY_OUTPUT_Y = "outputY";
    public static final String KEY_RETURN_DATA = "return-data";
    public static final String KEY_SCALE = "scale";
    public static final String KEY_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
    public static final String KEY_SET_AS_WALLPAPER = "set-as-wallpaper";
    public static final String KEY_SHOW_WHEN_LOCKED = "showWhenLocked";
    public static final String KEY_SPOTLIGHT_X = "spotlightX";
    public static final String KEY_SPOTLIGHT_Y = "spotlightY";
    private int mAspectX;
    private int mAspectY;
    private Uri mExtraOutput;
    private String mOutputFormat;
    private int mOutputX;
    private int mOutputY;
    private boolean mReturnData;
    private boolean mScaleUp;
    private boolean mSetAsWallpaper;
    private boolean mShowWhenLocked;
    private float mSpotlightX;
    private float mSpotlightY;

    public CropExtras(int i, int i2, boolean z, int i3, int i4, boolean z2, boolean z3, Uri uri, String str, boolean z4, float f, float f2) {
        this.mOutputX = 0;
        this.mOutputY = 0;
        this.mScaleUp = true;
        this.mAspectX = 0;
        this.mAspectY = 0;
        this.mSetAsWallpaper = false;
        this.mReturnData = false;
        this.mExtraOutput = null;
        this.mOutputFormat = null;
        this.mShowWhenLocked = false;
        this.mSpotlightX = 0.0f;
        this.mSpotlightY = 0.0f;
        this.mOutputX = i;
        this.mOutputY = i2;
        this.mScaleUp = z;
        this.mAspectX = i3;
        this.mAspectY = i4;
        this.mSetAsWallpaper = z2;
        this.mReturnData = z3;
        this.mExtraOutput = uri;
        this.mOutputFormat = str;
        this.mShowWhenLocked = z4;
        this.mSpotlightX = f;
        this.mSpotlightY = f2;
    }

    public CropExtras(CropExtras cropExtras) {
        this(cropExtras.mOutputX, cropExtras.mOutputY, cropExtras.mScaleUp, cropExtras.mAspectX, cropExtras.mAspectY, cropExtras.mSetAsWallpaper, cropExtras.mReturnData, cropExtras.mExtraOutput, cropExtras.mOutputFormat, cropExtras.mShowWhenLocked, cropExtras.mSpotlightX, cropExtras.mSpotlightY);
    }

    public int getOutputX() {
        return this.mOutputX;
    }

    public int getOutputY() {
        return this.mOutputY;
    }

    public boolean getScaleUp() {
        return this.mScaleUp;
    }

    public int getAspectX() {
        return this.mAspectX;
    }

    public int getAspectY() {
        return this.mAspectY;
    }

    public boolean getSetAsWallpaper() {
        return this.mSetAsWallpaper;
    }

    public boolean getReturnData() {
        return this.mReturnData;
    }

    public Uri getExtraOutput() {
        return this.mExtraOutput;
    }

    public String getOutputFormat() {
        return this.mOutputFormat;
    }

    public boolean getShowWhenLocked() {
        return this.mShowWhenLocked;
    }

    public float getSpotlightX() {
        return this.mSpotlightX;
    }

    public float getSpotlightY() {
        return this.mSpotlightY;
    }
}
