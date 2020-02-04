package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/* renamed from: com.android.camera.ui.PieMenuButton */
public class PieMenuButton extends View {
    private boolean mPressed;
    private boolean mReadyToClick = false;

    public PieMenuButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        this.mPressed = isPressed();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        if (1 == motionEvent.getAction() && this.mPressed) {
            this.mReadyToClick = true;
            performClick();
        }
        return onTouchEvent;
    }

    public boolean performClick() {
        if (!this.mReadyToClick) {
            return false;
        }
        this.mReadyToClick = false;
        return super.performClick();
    }
}
