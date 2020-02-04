package com.android.camera.p004ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.android.camera.util.ApiHelper;

@SuppressLint({"NewApi"})
/* renamed from: com.android.camera.ui.CameraRootView */
public class CameraRootView extends FrameLayout {
    private int mBottomMargin = 0;
    private final Rect mCurrentInsets = new Rect(0, 0, 0, 0);
    private Object mDisplayListener;
    private int mLeftMargin = 0;
    /* access modifiers changed from: private */
    public MyDisplayListener mListener;
    private int mOffset = 0;
    private int mRightMargin = 0;
    private int mTopMargin = 0;

    /* renamed from: com.android.camera.ui.CameraRootView$MyDisplayListener */
    public interface MyDisplayListener {
        void onDisplayChanged();
    }

    public CameraRootView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initDisplayListener();
    }

    /* access modifiers changed from: protected */
    public boolean fitSystemWindows(Rect rect) {
        if (this.mCurrentInsets.equals(rect)) {
            return false;
        }
        if (this.mOffset == 0) {
            int i = rect.bottom;
            if (i > 0) {
                this.mOffset = i;
            } else {
                int i2 = rect.right;
                if (i2 > 0) {
                    this.mOffset = i2;
                }
            }
        }
        this.mCurrentInsets.set(rect);
        requestLayout();
        return false;
    }

    public void initDisplayListener() {
        if (ApiHelper.HAS_DISPLAY_LISTENER) {
            this.mDisplayListener = new DisplayListener() {
                public void onDisplayAdded(int i) {
                }

                public void onDisplayRemoved(int i) {
                }

                public void onDisplayChanged(int i) {
                    if (CameraRootView.this.mListener != null) {
                        CameraRootView.this.mListener.onDisplayChanged();
                    }
                }
            };
        }
    }

    public void removeDisplayChangeListener() {
        this.mListener = null;
    }

    public void setDisplayChangeListener(MyDisplayListener myDisplayListener) {
        this.mListener = myDisplayListener;
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (ApiHelper.HAS_DISPLAY_LISTENER) {
            ((DisplayManager) getContext().getSystemService("display")).registerDisplayListener((DisplayListener) this.mDisplayListener, null);
        }
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (ApiHelper.HAS_DISPLAY_LISTENER) {
            ((DisplayManager) getContext().getSystemService("display")).unregisterDisplayListener((DisplayListener) this.mDisplayListener);
        }
    }
}
