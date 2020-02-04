package com.android.camera.p004ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import java.util.List;

/* renamed from: com.android.camera.ui.PieItem */
public class PieItem {
    private static final float DISABLED_ALPHA = 0.3f;
    private static final float ENABLED_ALPHA = 1.0f;
    private int level;
    private float mAlpha;
    private boolean mChangeAlphaWhenDisabled = true;
    private Drawable mDrawable;
    private boolean mEnabled;
    private List<PieItem> mItems;
    private CharSequence mLabel;
    private OnClickListener mOnClickListener;
    private Path mPath;
    private boolean mSelected;

    /* renamed from: com.android.camera.ui.PieItem$OnClickListener */
    public interface OnClickListener {
        void onClick(PieItem pieItem);
    }

    public PieItem(Drawable drawable, int i) {
        this.mDrawable = drawable;
        this.level = i;
        if (drawable != null) {
            setAlpha(ENABLED_ALPHA);
        }
        this.mEnabled = true;
    }

    public void setLabel(CharSequence charSequence) {
        this.mLabel = charSequence;
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public boolean hasItems() {
        return this.mItems != null;
    }

    public List<PieItem> getItems() {
        return this.mItems;
    }

    public void addItem(PieItem pieItem) {
        if (this.mItems == null) {
            this.mItems = new ArrayList();
        }
        this.mItems.add(pieItem);
    }

    public void clearItems() {
        this.mItems = null;
    }

    public void setLevel(int i) {
        this.level = i;
    }

    public void setPath(Path path) {
        this.mPath = path;
    }

    public Path getPath() {
        return this.mPath;
    }

    public void setChangeAlphaWhenDisabled(boolean z) {
        this.mChangeAlphaWhenDisabled = z;
    }

    public void setAlpha(float f) {
        this.mAlpha = f;
        this.mDrawable.setAlpha((int) (f * 255.0f));
    }

    public void setEnabled(boolean z) {
        this.mEnabled = z;
        if (!this.mChangeAlphaWhenDisabled) {
            return;
        }
        if (this.mEnabled) {
            setAlpha(ENABLED_ALPHA);
        } else {
            setAlpha(0.3f);
        }
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public void setSelected(boolean z) {
        this.mSelected = z;
    }

    public boolean isSelected() {
        return this.mSelected;
    }

    public int getLevel() {
        return this.level;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public void performClick() {
        OnClickListener onClickListener = this.mOnClickListener;
        if (onClickListener != null) {
            onClickListener.onClick(this);
        }
    }

    public int getIntrinsicWidth() {
        return this.mDrawable.getIntrinsicWidth();
    }

    public int getIntrinsicHeight() {
        return this.mDrawable.getIntrinsicHeight();
    }

    public void setBounds(int i, int i2, int i3, int i4) {
        this.mDrawable.setBounds(i, i2, i3, i4);
    }

    public void draw(Canvas canvas) {
        this.mDrawable.draw(canvas);
    }

    public void setImageResource(Context context, int i) {
        Drawable mutate = context.getResources().getDrawable(i).mutate();
        mutate.setBounds(this.mDrawable.getBounds());
        this.mDrawable = mutate;
        setAlpha(this.mAlpha);
    }
}
