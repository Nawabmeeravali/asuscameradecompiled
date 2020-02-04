package com.android.camera.p004ui;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;

/* renamed from: com.android.camera.ui.FilmStripGestureRecognizer */
public class FilmStripGestureRecognizer {
    private static final String TAG = "FilmStripGestureRecognizer";
    private final GestureDetector mGestureDetector;
    /* access modifiers changed from: private */
    public final Listener mListener;
    private final ScaleGestureDetector mScaleDetector;

    /* renamed from: com.android.camera.ui.FilmStripGestureRecognizer$Listener */
    public interface Listener {
        boolean onDoubleTap(float f, float f2);

        boolean onDown(float f, float f2);

        boolean onFling(float f, float f2);

        boolean onScale(float f, float f2, float f3);

        boolean onScaleBegin(float f, float f2);

        void onScaleEnd();

        boolean onScroll(float f, float f2, float f3, float f4);

        boolean onSingleTapUp(float f, float f2);

        boolean onUp(float f, float f2);
    }

    /* renamed from: com.android.camera.ui.FilmStripGestureRecognizer$MyDoubleTapListener */
    private class MyDoubleTapListener implements OnDoubleTapListener {
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return true;
        }

        private MyDoubleTapListener() {
        }

        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            return FilmStripGestureRecognizer.this.mListener.onSingleTapUp(motionEvent.getX(), motionEvent.getY());
        }

        public boolean onDoubleTap(MotionEvent motionEvent) {
            return FilmStripGestureRecognizer.this.mListener.onDoubleTap(motionEvent.getX(), motionEvent.getY());
        }
    }

    /* renamed from: com.android.camera.ui.FilmStripGestureRecognizer$MyGestureListener */
    private class MyGestureListener extends SimpleOnGestureListener {
        private MyGestureListener() {
        }

        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            return FilmStripGestureRecognizer.this.mListener.onScroll(motionEvent2.getX(), motionEvent2.getY(), f, f2);
        }

        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            return FilmStripGestureRecognizer.this.mListener.onFling(f, f2);
        }

        public boolean onDown(MotionEvent motionEvent) {
            FilmStripGestureRecognizer.this.mListener.onDown(motionEvent.getX(), motionEvent.getY());
            return super.onDown(motionEvent);
        }
    }

    /* renamed from: com.android.camera.ui.FilmStripGestureRecognizer$MyScaleListener */
    private class MyScaleListener extends SimpleOnScaleGestureListener {
        private MyScaleListener() {
        }

        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return FilmStripGestureRecognizer.this.mListener.onScaleBegin(scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
        }

        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            return FilmStripGestureRecognizer.this.mListener.onScale(scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY(), scaleGestureDetector.getScaleFactor());
        }

        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            FilmStripGestureRecognizer.this.mListener.onScaleEnd();
        }
    }

    public FilmStripGestureRecognizer(Context context, Listener listener) {
        this.mListener = listener;
        this.mGestureDetector = new GestureDetector(context, new MyGestureListener(), null, true);
        this.mGestureDetector.setOnDoubleTapListener(new MyDoubleTapListener());
        this.mScaleDetector = new ScaleGestureDetector(context, new MyScaleListener());
    }

    public void onTouchEvent(MotionEvent motionEvent) {
        this.mGestureDetector.onTouchEvent(motionEvent);
        this.mScaleDetector.onTouchEvent(motionEvent);
        if (motionEvent.getAction() == 1) {
            this.mListener.onUp(motionEvent.getX(), motionEvent.getY());
        }
    }
}
