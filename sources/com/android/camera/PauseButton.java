package com.android.camera;

import android.content.Context;
import android.util.AttributeSet;
import com.android.camera.p004ui.RotateImageView;

public class PauseButton extends RotateImageView {
    private OnPauseButtonListener mListener;

    public interface OnPauseButtonListener {
        void onButtonContinue();

        void onButtonPause();
    }

    public PauseButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setClickable(true);
        setSelected(false);
    }

    public void setPaused(boolean z) {
        setSelected(z);
    }

    public boolean performClick() {
        boolean performClick = super.performClick();
        if (isSelected()) {
            setSelected(false);
            if (this.mListener != null && getVisibility() == 0) {
                this.mListener.onButtonContinue();
            }
        } else {
            setSelected(true);
            if (this.mListener != null && getVisibility() == 0) {
                this.mListener.onButtonPause();
            }
        }
        return performClick;
    }

    public void setOnPauseButtonListener(OnPauseButtonListener onPauseButtonListener) {
        this.mListener = onPauseButtonListener;
    }
}
