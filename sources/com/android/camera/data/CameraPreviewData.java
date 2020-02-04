package com.android.camera.data;

import android.view.View;

public class CameraPreviewData extends SimpleViewData {
    private boolean mPreviewLocked = true;

    public int getLocalDataType() {
        return 1;
    }

    public int getViewType() {
        return 1;
    }

    public CameraPreviewData(View view, int i, int i2) {
        super(view, i, i2, -1, -1);
    }

    public boolean canSwipeInFullScreen() {
        return !this.mPreviewLocked;
    }

    public void lockPreview(boolean z) {
        this.mPreviewLocked = z;
    }
}
