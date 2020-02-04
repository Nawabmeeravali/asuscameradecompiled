package com.android.camera;

public interface WideAnglePanoramaController {
    void cancelHighResStitching();

    int getCameraOrientation();

    void onPreviewUIDestroyed();

    void onPreviewUILayoutChange(int i, int i2, int i3, int i4);

    void onPreviewUIReady();

    void onShutterButtonClick();
}
