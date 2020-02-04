package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/* renamed from: com.android.camera.ui.TwoStateImageView */
public class TwoStateImageView extends ImageView {
    private static final int DISABLED_ALPHA = 102;
    private static final int ENABLED_ALPHA = 255;
    private boolean mFilterEnabled;

    public TwoStateImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mFilterEnabled = true;
    }

    public TwoStateImageView(Context context) {
        this(context, null);
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (!this.mFilterEnabled) {
            return;
        }
        if (z) {
            setAlpha(255);
        } else {
            setAlpha(102);
        }
    }

    public void enableFilter(boolean z) {
        this.mFilterEnabled = z;
    }
}
