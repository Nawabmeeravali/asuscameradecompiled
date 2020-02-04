package com.android.camera;

import android.view.View;
import com.android.camera.PauseButton.OnPauseButtonListener;
import com.android.camera.ShutterButton.OnShutterButtonListener;

public interface VideoController extends OnShutterButtonListener, OnPauseButtonListener {
    boolean isInReviewMode();

    boolean isVideoCaptureIntent();

    void onPreviewUIDestroyed();

    void onPreviewUIReady();

    void onReviewCancelClicked(View view);

    void onReviewDoneClicked(View view);

    void onReviewPlayClicked(View view);

    void onSingleTapUp(View view, int i, int i2);

    int onZoomChanged(int i);

    void stopPreview();

    void updateCameraOrientation();
}
