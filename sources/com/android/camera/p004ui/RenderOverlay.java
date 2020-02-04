package com.android.camera.p004ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.android.camera.PreviewGestures;
import java.util.ArrayList;
import java.util.List;

/* renamed from: com.android.camera.ui.RenderOverlay */
public class RenderOverlay extends FrameLayout {
    private static final String TAG = "CAM_Overlay";
    /* access modifiers changed from: private */
    public List<Renderer> mClients;
    private PreviewGestures mGestures;
    private int[] mPosition = new int[2];
    private RenderView mRenderView;
    /* access modifiers changed from: private */
    public List<Renderer> mTouchClients;

    /* renamed from: com.android.camera.ui.RenderOverlay$RenderView */
    private class RenderView extends View {
        private Renderer mTouchTarget;

        public RenderView(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        public void setTouchTarget(Renderer renderer) {
            this.mTouchTarget = renderer;
        }

        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            Renderer renderer = this.mTouchTarget;
            if (renderer != null) {
                return renderer.onTouchEvent(motionEvent);
            }
            boolean z = false;
            if (RenderOverlay.this.mTouchClients != null) {
                for (Renderer onTouchEvent : RenderOverlay.this.mTouchClients) {
                    z |= onTouchEvent.onTouchEvent(motionEvent);
                }
            }
            return z;
        }

        public void onLayout(boolean z, int i, int i2, int i3, int i4) {
            RenderOverlay.this.adjustPosition();
            super.onLayout(z, i, i2, i3, i4);
            if (RenderOverlay.this.mClients != null) {
                for (Renderer layout : RenderOverlay.this.mClients) {
                    layout.layout(i, i2, i3, i4);
                }
            }
        }

        public void draw(Canvas canvas) {
            super.draw(canvas);
            if (RenderOverlay.this.mClients != null) {
                for (Renderer draw : RenderOverlay.this.mClients) {
                    draw.draw(canvas);
                }
            }
        }
    }

    /* renamed from: com.android.camera.ui.RenderOverlay$Renderer */
    interface Renderer {
        void draw(Canvas canvas);

        boolean handlesTouch();

        void layout(int i, int i2, int i3, int i4);

        boolean onTouchEvent(MotionEvent motionEvent);

        void setOverlay(RenderOverlay renderOverlay);
    }

    public RenderOverlay(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRenderView = new RenderView(context);
        addView(this.mRenderView, new LayoutParams(-1, -1));
        this.mClients = new ArrayList(10);
        this.mTouchClients = new ArrayList(10);
        setWillNotDraw(false);
    }

    public void setGestures(PreviewGestures previewGestures) {
        this.mGestures = previewGestures;
    }

    public void addRenderer(Renderer renderer) {
        this.mClients.add(renderer);
        renderer.setOverlay(this);
        if (renderer.handlesTouch()) {
            this.mTouchClients.add(0, renderer);
        }
        renderer.layout(getLeft(), getTop(), getRight(), getBottom());
    }

    public void addRenderer(int i, Renderer renderer) {
        this.mClients.add(i, renderer);
        renderer.setOverlay(this);
        renderer.layout(getLeft(), getTop(), getRight(), getBottom());
    }

    public void remove(Renderer renderer) {
        this.mClients.remove(renderer);
        renderer.setOverlay(null);
    }

    public int getClientSize() {
        return this.mClients.size();
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        PreviewGestures previewGestures = this.mGestures;
        boolean z = false;
        if (previewGestures == null) {
            List<Renderer> list = this.mTouchClients;
            if (list == null) {
                return true;
            }
            for (Renderer onTouchEvent : list) {
                z |= onTouchEvent.onTouchEvent(motionEvent);
            }
            return z;
        } else if (!previewGestures.isEnabled()) {
            return false;
        } else {
            this.mGestures.dispatchTouch(motionEvent);
            return true;
        }
    }

    public boolean directDispatchTouch(MotionEvent motionEvent, Renderer renderer) {
        this.mRenderView.setTouchTarget(renderer);
        boolean dispatchTouchEvent = this.mRenderView.dispatchTouchEvent(motionEvent);
        this.mRenderView.setTouchTarget(null);
        return dispatchTouchEvent;
    }

    /* access modifiers changed from: private */
    public void adjustPosition() {
        getLocationInWindow(this.mPosition);
    }

    public int getWindowPositionX() {
        return this.mPosition[0];
    }

    public int getWindowPositionY() {
        return this.mPosition[1];
    }

    public void update() {
        this.mRenderView.invalidate();
    }
}
