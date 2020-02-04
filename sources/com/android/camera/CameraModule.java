package com.android.camera;

import android.content.Intent;
import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.View;

public interface CameraModule {
    boolean arePreviewControlsVisible();

    void enableRecordingLocation(boolean z);

    void init(CameraActivity cameraActivity, View view);

    void installIntentFilter();

    void onActivityResult(int i, int i2, Intent intent);

    boolean onBackPressed();

    void onCaptureTextureCopied();

    void onConfigurationChanged(Configuration configuration);

    void onDestroy();

    boolean onKeyDown(int i, KeyEvent keyEvent);

    boolean onKeyUp(int i, KeyEvent keyEvent);

    void onMediaSaveServiceConnected(MediaSaveService mediaSaveService);

    void onOrientationChanged(int i);

    void onPauseAfterSuper();

    void onPauseBeforeSuper();

    void onPreviewFocusChanged(boolean z);

    void onPreviewTextureCopied();

    void onResumeAfterSuper();

    void onResumeBeforeSuper();

    void onShowSwitcherPopup();

    void onSingleTapUp(View view, int i, int i2);

    void onStop();

    void onStorageNotEnoughRecordingVideo();

    void onSwitchSavePath();

    void onUserInteraction();

    void resizeForPreviewAspectRatio();

    void setPreferenceForTest(String str, String str2);

    boolean updateStorageHintOnResume();

    void waitingLocationPermissionResult(boolean z);
}
