package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.GridView;

/* renamed from: com.android.camera.ui.ExpandedGridView */
public class ExpandedGridView extends GridView {
    public ExpandedGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, MeasureSpec.makeMeasureSpec(65536, Integer.MIN_VALUE));
    }
}
