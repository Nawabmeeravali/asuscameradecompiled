package com.android.camera.data;

import android.database.ContentObserver;

public class LocalMediaObserver extends ContentObserver {
    private boolean mActivityPaused = false;
    private boolean mMediaDataChangedDuringPause = false;

    public LocalMediaObserver() {
        super(null);
    }

    public void onChange(boolean z) {
        if (this.mActivityPaused) {
            this.mMediaDataChangedDuringPause = true;
        }
    }

    public void setActivityPaused(boolean z) {
        this.mActivityPaused = z;
        if (!z) {
            this.mMediaDataChangedDuringPause = false;
        }
    }

    public boolean isMediaDataChangedDuringPause() {
        return this.mMediaDataChangedDuringPause;
    }
}
