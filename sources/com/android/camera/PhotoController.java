package com.android.camera;

import android.graphics.Rect;
import android.view.View;
import com.android.camera.ShutterButton.OnShutterButtonListener;

public interface PhotoController extends OnShutterButtonListener {
    public static final int FOCUSING = 2;
    public static final int IDLE = 1;
    public static final int INIT = -1;
    public static final int LONGSHOT = 5;
    public static final int PREVIEW_STOPPED = 0;
    public static final int SNAPSHOT_IN_PROGRESS = 3;
    public static final int SWITCHING_CAMERA = 4;

    void cancelAutoFocus();

    void enableRecordingLocation(boolean z);

    int getCameraState();

    boolean isCameraIdle();

    boolean isImageCaptureIntent();

    void onCaptureCancelled();

    void onCaptureDone();

    void onCaptureRetake();

    void onCountDownFinished();

    void onPreviewRectChanged(Rect rect);

    void onPreviewUIDestroyed();

    void onPreviewUIReady();

    void onScreenSizeChanged(int i, int i2);

    void onSingleTapUp(View view, int i, int i2);

    int onZoomChanged(int i);

    void onZoomChanged(float f);

    void stopPreview();

    void updateCameraOrientation();
}
