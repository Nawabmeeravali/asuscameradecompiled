package com.android.camera.p004ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import com.android.camera.util.CameraUtil;

/* renamed from: com.android.camera.ui.RotatableLayout */
public class RotatableLayout extends FrameLayout {
    private static final String TAG = "RotatableLayout";
    private static final int UNKOWN_ORIENTATION = -1;
    private int mInitialOrientation;
    private boolean mIsDefaultToPortrait = false;
    private int mPrevRotation = -1;

    private static boolean contains(int i, int i2) {
        return (i & i2) == i2;
    }

    public RotatableLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    public RotatableLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public RotatableLayout(Context context) {
        super(context);
        init();
    }

    private void init() {
        this.mInitialOrientation = getResources().getConfiguration().orientation;
    }

    public void onAttachedToWindow() {
        if (this.mPrevRotation == -1) {
            this.mIsDefaultToPortrait = CameraUtil.isDefaultToPortrait((Activity) getContext());
            int i = 0;
            if (this.mIsDefaultToPortrait) {
                if (this.mInitialOrientation != 1) {
                    i = 90;
                }
                this.mPrevRotation = i;
            } else {
                if (this.mInitialOrientation != 2) {
                    i = 270;
                }
                this.mPrevRotation = i;
            }
            rotateIfNeeded();
        }
    }

    private void rotateIfNeeded() {
        if (this.mPrevRotation != -1) {
            int displayRotation = CameraUtil.getDisplayRotation((Activity) getContext());
            int i = this.mPrevRotation;
            int i2 = ((displayRotation - i) + 360) % 360;
            if (i2 != 0) {
                if (i2 == 180) {
                    this.mPrevRotation = displayRotation;
                    flipChildren();
                    return;
                }
                boolean isClockWiseRotation = isClockWiseRotation(i, displayRotation);
                this.mPrevRotation = displayRotation;
                rotateLayout(isClockWiseRotation);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getUnifiedRotation() {
        int displayRotation = CameraUtil.getDisplayRotation((Activity) getContext());
        return !this.mIsDefaultToPortrait ? (displayRotation + 90) % 360 : displayRotation;
    }

    public void checkLayoutFlip() {
        int displayRotation = CameraUtil.getDisplayRotation((Activity) getContext());
        if (((displayRotation - this.mPrevRotation) + 360) % 360 == 180) {
            this.mPrevRotation = displayRotation;
            flipChildren();
            requestLayout();
        }
    }

    public void onWindowVisibilityChanged(int i) {
        if (i == 0) {
            checkLayoutFlip();
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        rotateIfNeeded();
    }

    /* access modifiers changed from: protected */
    public void rotateLayout(boolean z) {
        LayoutParams layoutParams = getLayoutParams();
        int i = layoutParams.width;
        int i2 = layoutParams.height;
        layoutParams.height = i;
        layoutParams.width = i2;
        setLayoutParams(layoutParams);
        rotateChildren(z);
    }

    /* access modifiers changed from: protected */
    public void rotateChildren(boolean z) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            rotate(getChildAt(i), z);
        }
    }

    /* access modifiers changed from: protected */
    public void flipChildren() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            flip(getChildAt(i));
        }
    }

    public static boolean isClockWiseRotation(int i, int i2) {
        return i == (i2 + 90) % 360;
    }

    public static void rotate(View view, boolean z) {
        if (z) {
            rotateClockwise(view);
        } else {
            rotateCounterClockwise(view);
        }
    }

    public static void rotateClockwise(View view) {
        if (view != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            int i = layoutParams.gravity;
            int i2 = 0;
            if (contains(i, 3)) {
                i2 = 48;
            }
            if (contains(i, 5)) {
                i2 |= 80;
            }
            if (contains(i, 48)) {
                i2 |= 5;
            }
            if (contains(i, 80)) {
                i2 |= 3;
            }
            if (contains(i, 17)) {
                i2 |= 17;
            }
            if (contains(i, 1)) {
                i2 |= 16;
            }
            if (contains(i, 16)) {
                i2 |= 1;
            }
            layoutParams.gravity = i2;
            int i3 = layoutParams.leftMargin;
            int i4 = layoutParams.rightMargin;
            int i5 = layoutParams.topMargin;
            layoutParams.leftMargin = layoutParams.bottomMargin;
            layoutParams.rightMargin = i5;
            layoutParams.topMargin = i3;
            layoutParams.bottomMargin = i4;
            int i6 = layoutParams.width;
            layoutParams.width = layoutParams.height;
            layoutParams.height = i6;
            view.setLayoutParams(layoutParams);
        }
    }

    public static void rotateCounterClockwise(View view) {
        if (view != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            int i = layoutParams.gravity;
            int i2 = 0;
            if (contains(i, 5)) {
                i2 = 48;
            }
            if (contains(i, 3)) {
                i2 |= 80;
            }
            if (contains(i, 48)) {
                i2 |= 3;
            }
            if (contains(i, 80)) {
                i2 |= 5;
            }
            if (contains(i, 17)) {
                i2 |= 17;
            }
            if (contains(i, 1)) {
                i2 |= 16;
            }
            if (contains(i, 16)) {
                i2 |= 1;
            }
            layoutParams.gravity = i2;
            int i3 = layoutParams.leftMargin;
            int i4 = layoutParams.rightMargin;
            int i5 = layoutParams.topMargin;
            int i6 = layoutParams.bottomMargin;
            layoutParams.leftMargin = i5;
            layoutParams.rightMargin = i6;
            layoutParams.topMargin = i4;
            layoutParams.bottomMargin = i3;
            int i7 = layoutParams.width;
            layoutParams.width = layoutParams.height;
            layoutParams.height = i7;
            view.setLayoutParams(layoutParams);
        }
    }

    public static void flip(View view) {
        rotateClockwise(view);
        rotateClockwise(view);
    }
}
