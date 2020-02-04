package com.android.camera.p004ui;

import android.app.Activity;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.camera.util.CameraUtil;
import java.util.HashSet;
import java.util.Iterator;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.RotateTextToast */
public class RotateTextToast {
    private static final int LONG_DELAY = 3500;
    private static final int SHORT_DELAY = 2000;
    private static int mOrientation;
    /* access modifiers changed from: private */
    public static HashSet<RotateLayout> mToasts = new HashSet<>();
    private int mDuration;
    private Handler mHandler;
    /* access modifiers changed from: private */
    public ViewGroup mLayoutRoot;
    private final Runnable mRunnable;
    /* access modifiers changed from: private */
    public RotateLayout mToast;

    private RotateTextToast(Activity activity, int i) {
        this.mRunnable = new Runnable() {
            public void run() {
                CameraUtil.fadeOut(RotateTextToast.this.mToast);
                RotateTextToast.this.mLayoutRoot.removeView(RotateTextToast.this.mToast);
                RotateTextToast.mToasts.remove(RotateTextToast.this.mToast);
                RotateTextToast.this.mToast = null;
            }
        };
        this.mLayoutRoot = (ViewGroup) activity.getWindow().getDecorView();
        this.mToast = (RotateLayout) activity.getLayoutInflater().inflate(C0905R.layout.rotate_text_toast, this.mLayoutRoot).findViewById(C0905R.C0907id.rotate_toast);
        this.mToast.setOrientation(mOrientation, false);
        this.mHandler = new Handler();
        this.mDuration = i == 1 ? LONG_DELAY : SHORT_DELAY;
    }

    public RotateTextToast(Activity activity, CharSequence charSequence, int i) {
        this(activity, i);
        ((TextView) this.mToast.findViewById(C0905R.C0907id.message)).setText(charSequence);
    }

    public RotateTextToast(Activity activity, int i, int i2) {
        this(activity, i2);
        ((TextView) this.mToast.findViewById(C0905R.C0907id.message)).setText(i);
    }

    public void show() {
        mToasts.add(this.mToast);
        this.mToast.setVisibility(0);
        this.mHandler.postDelayed(this.mRunnable, (long) this.mDuration);
    }

    public static RotateTextToast makeText(Activity activity, int i, int i2) {
        return new RotateTextToast(activity, i, i2);
    }

    public static RotateTextToast makeText(Activity activity, CharSequence charSequence, int i) {
        return new RotateTextToast(activity, charSequence, i);
    }

    public static void setOrientation(int i) {
        mOrientation = i;
        Iterator it = mToasts.iterator();
        while (it.hasNext()) {
            ((RotateLayout) it.next()).setOrientation(i, false);
        }
    }
}
