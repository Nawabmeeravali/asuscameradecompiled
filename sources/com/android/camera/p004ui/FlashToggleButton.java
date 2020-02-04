package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.camera.SettingsManager;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.FlashToggleButton */
public class FlashToggleButton extends RotateImageView {
    /* access modifiers changed from: private */
    public int[] cameraFlashIcon = {C0905R.C0906drawable.ic_flash_off, C0905R.C0906drawable.ic_flash_auto, C0905R.C0906drawable.ic_flash_on};
    /* access modifiers changed from: private */
    public int mIndex;
    /* access modifiers changed from: private */
    public boolean mIsVideoFlash;
    /* access modifiers changed from: private */
    public SettingsManager mSettingsManager;
    /* access modifiers changed from: private */
    public int[] videoFlashIcon = {C0905R.C0906drawable.ic_flash_off, C0905R.C0906drawable.ic_flash_on};

    public FlashToggleButton(Context context) {
        super(context);
    }

    public FlashToggleButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void init(boolean z) {
        this.mIsVideoFlash = z;
        String str = this.mIsVideoFlash ? SettingsManager.KEY_VIDEO_FLASH_MODE : SettingsManager.KEY_FLASH_MODE;
        this.mSettingsManager = SettingsManager.getInstance();
        this.mIndex = this.mSettingsManager.getValueIndex(str);
        String value = this.mSettingsManager.getValue(SettingsManager.KEY_SCENE_MODE);
        if (value == null || !value.equals("18")) {
            String string = this.mContext.getString(C0905R.string.pref_camera_manual_exp_value_user_setting);
            String value2 = this.mSettingsManager.getValue(SettingsManager.KEY_MANUAL_EXPOSURE);
            if (this.mIndex == -1 || value2.equals(string)) {
                setVisibility(8);
                return;
            }
            setVisibility(0);
            update();
            setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    int[] iArr;
                    String str;
                    if (FlashToggleButton.this.mIsVideoFlash) {
                        iArr = FlashToggleButton.this.videoFlashIcon;
                        str = SettingsManager.KEY_VIDEO_FLASH_MODE;
                    } else {
                        iArr = FlashToggleButton.this.cameraFlashIcon;
                        str = SettingsManager.KEY_FLASH_MODE;
                    }
                    FlashToggleButton flashToggleButton = FlashToggleButton.this;
                    flashToggleButton.mIndex = (flashToggleButton.mIndex + 1) % iArr.length;
                    FlashToggleButton.this.mSettingsManager.setValueIndex(str, FlashToggleButton.this.mIndex);
                    FlashToggleButton.this.update();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void update() {
        int[] iArr;
        if (this.mIsVideoFlash) {
            iArr = this.videoFlashIcon;
        } else {
            iArr = this.cameraFlashIcon;
        }
        setImageResource(iArr[this.mIndex]);
    }

    public void updateFlashStatus(int i) {
        this.mIndex = i;
        this.mSettingsManager.setValueIndex(SettingsManager.KEY_FLASH_MODE, this.mIndex);
        update();
    }
}
