package com.android.camera;

import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import com.android.camera.p004ui.PieRenderer;
import com.android.camera.p004ui.RenderOverlay;
import com.android.camera.p004ui.TrackingFocusRenderer;
import com.android.camera.p004ui.ZoomRenderer;

public class PreviewGestures implements OnScaleGestureListener {
    public static final int DIR_DOWN = 1;
    public static final int DIR_LEFT = 2;
    public static final int DIR_RIGHT = 3;
    public static final int DIR_UP = 0;
    private static final int MODE_NONE = 0;
    private static final int MODE_ZOOM = 2;
    private static final String TAG = "CAM_gestures";
    /* access modifiers changed from: private */
    public CaptureUI mCaptureUI;
    private MotionEvent mCurrent;
    private MotionEvent mDown;
    private boolean mEnabled;
    private GestureDetector mGestureDetector;
    private SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener() {
        public void onLongPress(MotionEvent motionEvent) {
            if (!PreviewGestures.this.mZoomOnly && PreviewGestures.this.mPie != null && !PreviewGestures.this.mPie.showsItems()) {
                PreviewGestures.this.openPie();
            }
        }

        public boolean onSingleTapUp(MotionEvent motionEvent) {
            if (PreviewGestures.this.mPie != null && PreviewGestures.this.mPie.showsItems()) {
                return false;
            }
            PreviewGestures.this.mTapListener.onSingleTapUp(null, (int) motionEvent.getX(), (int) motionEvent.getY());
            return true;
        }

        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            if (!(motionEvent == null || PreviewGestures.this.mZoomOnly || PreviewGestures.this.mMode == 2)) {
                if (isLeftSwipe(PreviewGestures.this.mCaptureUI != null ? PreviewGestures.this.mCaptureUI.getOrientation() : 0, (int) (motionEvent.getX() - motionEvent2.getX()), (int) (motionEvent.getY() - motionEvent2.getY()))) {
                    PreviewGestures.this.waitUntilNextDown = true;
                    if (PreviewGestures.this.mCaptureUI == null || PreviewGestures.this.mCaptureUI.isVideoRecording() || PreviewGestures.this.mCaptureUI.isSecureCamera()) {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }

        private boolean isLeftSwipe(int i, int i2, int i3) {
            boolean z = true;
            if (i == 90) {
                if (i3 <= 0 || Math.abs(i3) <= Math.abs(i2) * 2) {
                    z = false;
                }
                return z;
            } else if (i == 180) {
                if (i2 <= 0 || Math.abs(i2) <= Math.abs(i3) * 2) {
                    z = false;
                }
                return z;
            } else if (i != 270) {
                if (i2 >= 0 || Math.abs(i2) <= Math.abs(i3) * 2) {
                    z = false;
                }
                return z;
            } else {
                if (i3 >= 0 || Math.abs(i3) <= Math.abs(i2) * 2) {
                    z = false;
                }
                return z;
            }
        }
    };
    /* access modifiers changed from: private */
    public int mMode;
    private RenderOverlay mOverlay;
    private PhotoMenu mPhotoMenu;
    /* access modifiers changed from: private */
    public PieRenderer mPie;
    private ScaleGestureDetector mScale;
    /* access modifiers changed from: private */
    public SingleTapListener mTapListener;
    private TrackingFocusRenderer mTrackingFocus;
    private VideoMenu mVideoMenu;
    private ZoomRenderer mZoom;
    private boolean mZoomEnabled;
    /* access modifiers changed from: private */
    public boolean mZoomOnly;
    private boolean setToFalse;
    /* access modifiers changed from: private */
    public boolean waitUntilNextDown;

    public interface SingleTapListener {
        void onSingleTapUp(View view, int i, int i2);
    }

    public PreviewGestures(CameraActivity cameraActivity, SingleTapListener singleTapListener, ZoomRenderer zoomRenderer, PieRenderer pieRenderer, TrackingFocusRenderer trackingFocusRenderer) {
        this.mTapListener = singleTapListener;
        this.mPie = pieRenderer;
        this.mTrackingFocus = trackingFocusRenderer;
        this.mZoom = zoomRenderer;
        this.mMode = 0;
        this.mScale = new ScaleGestureDetector(cameraActivity, this);
        this.mEnabled = true;
        this.mGestureDetector = new GestureDetector(this.mGestureListener);
    }

    public void setRenderOverlay(RenderOverlay renderOverlay) {
        this.mOverlay = renderOverlay;
    }

    public void setEnabled(boolean z) {
        this.mEnabled = z;
        if (!z) {
            setZoomScaleEnd();
        }
    }

    public void setZoomScaleEnd() {
        ZoomRenderer zoomRenderer = this.mZoom;
        if (zoomRenderer != null) {
            ScaleGestureDetector scaleGestureDetector = this.mScale;
            if (scaleGestureDetector != null) {
                zoomRenderer.onScaleEnd(scaleGestureDetector);
            }
        }
    }

    public void setZoomEnabled(boolean z) {
        this.mZoomEnabled = z;
    }

    public void setZoomOnly(boolean z) {
        this.mZoomOnly = z;
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public void setCaptureUI(CaptureUI captureUI) {
        this.mCaptureUI = captureUI;
    }

    public void setPhotoMenu(PhotoMenu photoMenu) {
        this.mPhotoMenu = photoMenu;
    }

    public void setVideoMenu(VideoMenu videoMenu) {
        this.mVideoMenu = videoMenu;
    }

    public PhotoMenu getPhotoMenu() {
        return this.mPhotoMenu;
    }

    public VideoMenu getVideoMenu() {
        return this.mVideoMenu;
    }

    public boolean dispatchTouch(MotionEvent motionEvent) {
        if (this.setToFalse) {
            this.waitUntilNextDown = false;
            this.setToFalse = false;
        }
        if (this.waitUntilNextDown) {
            if (1 != motionEvent.getActionMasked() && 3 != motionEvent.getActionMasked()) {
                return true;
            }
            this.setToFalse = true;
            return true;
        } else if (!this.mEnabled) {
            return false;
        } else {
            this.mCurrent = motionEvent;
            if (motionEvent.getActionMasked() == 0) {
                this.mMode = 0;
                this.mDown = MotionEvent.obtain(motionEvent);
            }
            PieRenderer pieRenderer = this.mPie;
            if (pieRenderer != null && pieRenderer.isOpen()) {
                return sendToPie(motionEvent);
            }
            TrackingFocusRenderer trackingFocusRenderer = this.mTrackingFocus;
            if (trackingFocusRenderer != null && trackingFocusRenderer.isVisible()) {
                return sendToTrackingFocus(motionEvent);
            }
            CaptureUI captureUI = this.mCaptureUI;
            if (captureUI == null || !captureUI.isPreviewMenuBeingShown()) {
                PhotoMenu photoMenu = this.mPhotoMenu;
                if (photoMenu != null) {
                    if (photoMenu.isMenuBeingShown()) {
                        if (!this.mPhotoMenu.isMenuBeingAnimated()) {
                            this.waitUntilNextDown = true;
                            this.mPhotoMenu.closeView();
                        }
                        return true;
                    } else if (this.mPhotoMenu.isPreviewMenuBeingShown()) {
                        this.waitUntilNextDown = true;
                        this.mPhotoMenu.animateSlideOutPreviewMenu();
                        return true;
                    }
                }
                VideoMenu videoMenu = this.mVideoMenu;
                if (videoMenu != null) {
                    if (videoMenu.isMenuBeingShown()) {
                        if (!this.mVideoMenu.isMenuBeingAnimated()) {
                            this.waitUntilNextDown = true;
                            this.mVideoMenu.closeView();
                        }
                        return true;
                    } else if (this.mVideoMenu.isPreviewMenuBeingShown()) {
                        this.waitUntilNextDown = true;
                        this.mVideoMenu.animateSlideOutPreviewMenu();
                        return true;
                    }
                }
                this.mGestureDetector.onTouchEvent(motionEvent);
                if (this.mZoom != null) {
                    this.mScale.onTouchEvent(motionEvent);
                    if (5 == motionEvent.getActionMasked()) {
                        this.mMode = 2;
                        if (this.mZoomEnabled) {
                            this.mZoom.onScaleBegin(this.mScale);
                        }
                    } else if (6 == motionEvent.getActionMasked()) {
                        this.mZoom.onScaleEnd(this.mScale);
                    }
                }
                return true;
            }
            this.waitUntilNextDown = true;
            this.mCaptureUI.removeFilterMenu(true);
            return true;
        }
    }

    public boolean waitUntilNextDown() {
        return this.waitUntilNextDown;
    }

    private MotionEvent makeCancelEvent(MotionEvent motionEvent) {
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        obtain.setAction(3);
        return obtain;
    }

    /* access modifiers changed from: private */
    public void openPie() {
        this.mGestureDetector.onTouchEvent(makeCancelEvent(this.mDown));
        this.mScale.onTouchEvent(makeCancelEvent(this.mDown));
        this.mOverlay.directDispatchTouch(this.mDown, this.mPie);
    }

    private boolean sendToPie(MotionEvent motionEvent) {
        return this.mOverlay.directDispatchTouch(motionEvent, this.mPie);
    }

    private boolean sendToTrackingFocus(MotionEvent motionEvent) {
        return this.mOverlay.directDispatchTouch(motionEvent, this.mTrackingFocus);
    }

    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        return this.mZoom.onScale(scaleGestureDetector);
    }

    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        PieRenderer pieRenderer = this.mPie;
        if (pieRenderer != null && pieRenderer.isOpen()) {
            return false;
        }
        this.mMode = 2;
        this.mGestureDetector.onTouchEvent(makeCancelEvent(this.mCurrent));
        if (!this.mZoomEnabled) {
            return false;
        }
        return this.mZoom.onScaleBegin(scaleGestureDetector);
    }

    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        this.mZoom.onScaleEnd(scaleGestureDetector);
    }
}
