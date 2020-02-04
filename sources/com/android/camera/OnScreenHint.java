package com.android.camera;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import org.codeaurora.snapcam.C0905R;

public class OnScreenHint {
    int mGravity = 81;
    private final Handler mHandler = new Handler();
    private final Runnable mHide = new Runnable() {
        public void run() {
            OnScreenHint.this.handleHide();
        }
    };
    float mHorizontalMargin;
    View mNextView;
    private final LayoutParams mParams = new LayoutParams();
    private final Runnable mShow = new Runnable() {
        public void run() {
            OnScreenHint.this.handleShow();
        }
    };
    float mVerticalMargin;
    View mView;
    private final WindowManager mWM;

    /* renamed from: mX */
    int f72mX;

    /* renamed from: mY */
    int f73mY;

    private OnScreenHint(Context context) {
        this.mWM = (WindowManager) context.getSystemService("window");
        this.f73mY = context.getResources().getDimensionPixelSize(C0905R.dimen.hint_y_offset);
        LayoutParams layoutParams = this.mParams;
        layoutParams.height = -2;
        layoutParams.width = -2;
        layoutParams.flags = 24;
        layoutParams.format = -3;
        layoutParams.windowAnimations = C0905R.style.Animation_OnScreenHint;
        layoutParams.type = 1000;
        layoutParams.setTitle("OnScreenHint");
    }

    public void show() {
        if (this.mNextView != null) {
            this.mHandler.post(this.mShow);
            return;
        }
        throw new RuntimeException("View is not initialized");
    }

    public void cancel() {
        this.mHandler.post(this.mHide);
    }

    public static OnScreenHint makeText(Context context, CharSequence charSequence) {
        OnScreenHint onScreenHint = new OnScreenHint(context);
        View inflate = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(C0905R.layout.on_screen_hint, null);
        ((TextView) inflate.findViewById(C0905R.C0907id.message)).setText(charSequence);
        onScreenHint.mNextView = inflate;
        return onScreenHint;
    }

    public void setText(CharSequence charSequence) {
        View view = this.mNextView;
        String str = "This OnScreenHint was not created with OnScreenHint.makeText()";
        if (view != null) {
            TextView textView = (TextView) view.findViewById(C0905R.C0907id.message);
            if (textView != null) {
                textView.setText(charSequence);
                return;
            }
            throw new RuntimeException(str);
        }
        throw new RuntimeException(str);
    }

    /* access modifiers changed from: private */
    public synchronized void handleShow() {
        if (this.mView != this.mNextView) {
            handleHide();
            this.mView = this.mNextView;
            int i = this.mGravity;
            this.mParams.gravity = i;
            if ((i & 7) == 7) {
                this.mParams.horizontalWeight = 1.0f;
            }
            if ((i & 112) == 112) {
                this.mParams.verticalWeight = 1.0f;
            }
            this.mParams.x = this.f72mX;
            this.mParams.y = this.f73mY;
            this.mParams.verticalMargin = this.mVerticalMargin;
            this.mParams.horizontalMargin = this.mHorizontalMargin;
            if (this.mView.getParent() != null) {
                this.mWM.removeView(this.mView);
            }
            this.mWM.addView(this.mView, this.mParams);
        }
    }

    /* access modifiers changed from: private */
    public synchronized void handleHide() {
        if (this.mView != null) {
            if (this.mView.getParent() != null) {
                this.mWM.removeView(this.mView);
            }
            this.mView = null;
        }
    }
}
