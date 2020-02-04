package com.android.camera.util;

import android.graphics.Matrix;
import android.view.MotionEvent;

public final class MotionEventHelper {
    private MotionEventHelper() {
    }

    public static MotionEvent transformEvent(MotionEvent motionEvent, Matrix matrix) {
        return transformEventNew(motionEvent, matrix);
    }

    private static MotionEvent transformEventNew(MotionEvent motionEvent, Matrix matrix) {
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        obtain.transform(matrix);
        return obtain;
    }
}
