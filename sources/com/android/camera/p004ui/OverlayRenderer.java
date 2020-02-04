package com.android.camera.p004ui;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

/* renamed from: com.android.camera.ui.OverlayRenderer */
public abstract class OverlayRenderer implements Renderer {
    private static final String TAG = "CAM OverlayRenderer";
    protected int mBottom;
    protected int mLeft;
    protected RenderOverlay mOverlay;
    protected int mRight;
    protected int mTop;
    protected boolean mVisible;

    public boolean handlesTouch() {
        return false;
    }

    public abstract void onDraw(Canvas canvas);

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    public void setVisible(boolean z) {
        this.mVisible = z;
        update();
    }

    public boolean isVisible() {
        return this.mVisible;
    }

    public void draw(Canvas canvas) {
        if (this.mVisible) {
            onDraw(canvas);
        }
    }

    public void setOverlay(RenderOverlay renderOverlay) {
        this.mOverlay = renderOverlay;
    }

    public void layout(int i, int i2, int i3, int i4) {
        this.mLeft = i;
        this.mRight = i3;
        this.mTop = i2;
        this.mBottom = i4;
    }

    /* access modifiers changed from: protected */
    public Context getContext() {
        RenderOverlay renderOverlay = this.mOverlay;
        if (renderOverlay != null) {
            return renderOverlay.getContext();
        }
        return null;
    }

    public int getWidth() {
        return this.mRight - this.mLeft;
    }

    public int getHeight() {
        return this.mBottom - this.mTop;
    }

    /* access modifiers changed from: protected */
    public void update() {
        RenderOverlay renderOverlay = this.mOverlay;
        if (renderOverlay != null) {
            renderOverlay.update();
        }
    }
}
