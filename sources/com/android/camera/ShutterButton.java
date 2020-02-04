package com.android.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import com.android.camera.p004ui.RotateImageView;

public class ShutterButton extends RotateImageView {
    /* access modifiers changed from: private */
    public OnShutterButtonListener mListener;
    private LongClickListener mLongClick = new LongClickListener();
    private boolean mOldPressed;
    private boolean mTouchEnabled = true;

    private class LongClickListener implements OnLongClickListener {
        private LongClickListener() {
        }

        public boolean onLongClick(View view) {
            if (ShutterButton.this.mListener == null) {
                return false;
            }
            ShutterButton.this.mListener.onShutterButtonLongClick();
            return true;
        }
    }

    public interface OnShutterButtonListener {
        void onShutterButtonClick();

        void onShutterButtonFocus(boolean z);

        void onShutterButtonLongClick();
    }

    public ShutterButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setOnShutterButtonListener(OnShutterButtonListener onShutterButtonListener) {
        this.mListener = onShutterButtonListener;
        setOnLongClickListener(this.mLongClick);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (this.mTouchEnabled) {
            return super.dispatchTouchEvent(motionEvent);
        }
        return false;
    }

    public void enableTouch(boolean z) {
        this.mTouchEnabled = z;
        setLongClickable(z);
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        final boolean isPressed = isPressed();
        if (isPressed != this.mOldPressed) {
            if (!isPressed) {
                post(new Runnable() {
                    public void run() {
                        ShutterButton.this.callShutterButtonFocus(isPressed);
                    }
                });
            } else {
                callShutterButtonFocus(isPressed);
            }
            this.mOldPressed = isPressed;
        }
    }

    /* access modifiers changed from: private */
    public void callShutterButtonFocus(boolean z) {
        OnShutterButtonListener onShutterButtonListener = this.mListener;
        if (onShutterButtonListener != null) {
            onShutterButtonListener.onShutterButtonFocus(z);
        }
    }

    public boolean performClick() {
        boolean performClick = super.performClick();
        if (this.mListener != null && getVisibility() == 0) {
            this.mListener.onShutterButtonClick();
        }
        return performClick;
    }
}
