package com.android.camera.p004ui;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/* renamed from: com.android.camera.ui.RotateLayout */
public class RotateLayout extends ViewGroup implements Rotatable {
    private static final String TAG = "RotateLayout";
    protected View mChild;
    private Matrix mMatrix = new Matrix();
    private int mOrientation;

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public RotateLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setBackgroundResource(17170445);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        setupChild(getChildAt(0));
    }

    private void setupChild(View view) {
        if (view != null) {
            this.mChild = view;
            view.setPivotX(0.0f);
            view.setPivotY(0.0f);
        }
    }

    public void addView(View view) {
        super.addView(view);
        setupChild(view);
    }

    public void removeView(View view) {
        super.removeView(view);
        this.mOrientation = 0;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5 = i3 - i;
        int i6 = i4 - i2;
        int paddingTop = getPaddingTop();
        int i7 = this.mOrientation;
        if (i7 != 0) {
            if (i7 != 90) {
                if (i7 != 180) {
                    if (i7 != 270) {
                        return;
                    }
                }
            }
            this.mChild.layout(paddingTop, paddingTop, i6 - paddingTop, i5 - paddingTop);
            return;
        }
        this.mChild.layout(paddingTop, paddingTop, i5 - paddingTop, i6 - paddingTop);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0073  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onMeasure(int r7, int r8) {
        /*
            r6 = this;
            int r0 = r6.getPaddingTop()
            int r1 = r6.mOrientation
            r2 = 270(0x10e, float:3.78E-43)
            r3 = 180(0xb4, float:2.52E-43)
            r4 = 90
            r5 = 0
            if (r1 == 0) goto L_0x0029
            if (r1 == r4) goto L_0x0017
            if (r1 == r3) goto L_0x0029
            if (r1 == r2) goto L_0x0017
            r7 = r5
            goto L_0x003a
        L_0x0017:
            android.view.View r1 = r6.mChild
            r6.measureChild(r1, r8, r7)
            android.view.View r7 = r6.mChild
            int r5 = r7.getMeasuredHeight()
            android.view.View r7 = r6.mChild
            int r7 = r7.getMeasuredWidth()
            goto L_0x003a
        L_0x0029:
            android.view.View r1 = r6.mChild
            r6.measureChild(r1, r7, r8)
            android.view.View r7 = r6.mChild
            int r5 = r7.getMeasuredWidth()
            android.view.View r7 = r6.mChild
            int r7 = r7.getMeasuredHeight()
        L_0x003a:
            int r0 = r0 * 2
            int r8 = r5 + r0
            int r0 = r0 + r7
            r6.setMeasuredDimension(r8, r0)
            int r8 = r6.mOrientation
            r0 = 0
            if (r8 == 0) goto L_0x0073
            if (r8 == r4) goto L_0x0067
            if (r8 == r3) goto L_0x005a
            if (r8 == r2) goto L_0x004e
            goto L_0x007d
        L_0x004e:
            android.view.View r7 = r6.mChild
            float r8 = (float) r5
            r7.setTranslationX(r8)
            android.view.View r7 = r6.mChild
            r7.setTranslationY(r0)
            goto L_0x007d
        L_0x005a:
            android.view.View r8 = r6.mChild
            float r0 = (float) r5
            r8.setTranslationX(r0)
            android.view.View r8 = r6.mChild
            float r7 = (float) r7
            r8.setTranslationY(r7)
            goto L_0x007d
        L_0x0067:
            android.view.View r8 = r6.mChild
            r8.setTranslationX(r0)
            android.view.View r8 = r6.mChild
            float r7 = (float) r7
            r8.setTranslationY(r7)
            goto L_0x007d
        L_0x0073:
            android.view.View r7 = r6.mChild
            r7.setTranslationX(r0)
            android.view.View r7 = r6.mChild
            r7.setTranslationY(r0)
        L_0x007d:
            android.view.View r7 = r6.mChild
            int r6 = r6.mOrientation
            int r6 = -r6
            float r6 = (float) r6
            r7.setRotation(r6)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.p004ui.RotateLayout.onMeasure(int, int):void");
    }

    public void setOrientation(int i, boolean z) {
        int i2 = i % 360;
        if (this.mOrientation != i2) {
            if (getParent() instanceof FrameLayout) {
                int i3 = ((i2 - this.mOrientation) + 360) % 360;
                if (i3 == 90) {
                    RotatableLayout.rotateCounterClockwise(this);
                } else if (i3 == 180) {
                    RotatableLayout.rotateClockwise(this);
                    RotatableLayout.rotateClockwise(this);
                } else if (i3 == 270) {
                    RotatableLayout.rotateClockwise(this);
                }
            }
            this.mOrientation = i2;
            if (this.mChild != null) {
                requestLayout();
            }
        }
    }

    public int getOrientation() {
        return this.mOrientation;
    }
}
