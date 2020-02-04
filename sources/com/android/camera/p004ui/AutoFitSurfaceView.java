package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View.MeasureSpec;

/* renamed from: com.android.camera.ui.AutoFitSurfaceView */
public class AutoFitSurfaceView extends SurfaceView {
    private int mRatioHeight;
    private int mRatioWidth;

    public AutoFitSurfaceView(Context context) {
        this(context, null);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mRatioWidth = 0;
        this.mRatioHeight = 0;
    }

    public void setAspectRatio(int i, int i2) {
        if (i < 0 || i2 < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        this.mRatioWidth = i;
        this.mRatioHeight = i2;
        requestLayout();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int size = MeasureSpec.getSize(i);
        int size2 = MeasureSpec.getSize(i2);
        int i3 = this.mRatioWidth;
        if (i3 != 0) {
            int i4 = this.mRatioHeight;
            if (i4 != 0) {
                if (size < (size2 * i3) / i4) {
                    setMeasuredDimension(size, (i4 * size) / i3);
                    return;
                } else {
                    setMeasuredDimension((i3 * size2) / i4, size2);
                    return;
                }
            }
        }
        setMeasuredDimension(size, size2);
    }
}
