package com.android.camera.drawable;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.p000v4.view.ViewCompat;
import android.util.TypedValue;
import com.asus.scenedetectlib.BuildConfig;

public class TextDrawable extends Drawable {
    private static final int DEFAULT_COLOR = -1;
    private static final int DEFAULT_TEXTSIZE = 15;
    private int mIntrinsicHeight;
    private int mIntrinsicWidth;
    private Paint mPaint;
    private CharSequence mText;
    private boolean mUseDropShadow;

    public TextDrawable(Resources resources) {
        this(resources, BuildConfig.FLAVOR);
    }

    public TextDrawable(Resources resources, CharSequence charSequence) {
        this.mText = charSequence;
        updatePaint();
        this.mPaint.setTextSize(TypedValue.applyDimension(2, 15.0f, resources.getDisplayMetrics()));
        Paint paint = this.mPaint;
        CharSequence charSequence2 = this.mText;
        this.mIntrinsicWidth = (int) (((double) paint.measureText(charSequence2, 0, charSequence2.length())) + 0.5d);
        this.mIntrinsicHeight = this.mPaint.getFontMetricsInt(null);
    }

    private void updatePaint() {
        if (this.mPaint == null) {
            this.mPaint = new Paint(1);
        }
        this.mPaint.setColor(-1);
        this.mPaint.setTextAlign(Align.CENTER);
        if (this.mUseDropShadow) {
            this.mPaint.setTypeface(Typeface.DEFAULT_BOLD);
            this.mPaint.setShadowLayer(10.0f, 0.0f, 0.0f, ViewCompat.MEASURED_STATE_MASK);
            return;
        }
        this.mPaint.setTypeface(Typeface.DEFAULT);
        this.mPaint.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
    }

    public void setText(CharSequence charSequence) {
        this.mText = charSequence;
        if (charSequence == null) {
            this.mIntrinsicWidth = 0;
            this.mIntrinsicHeight = 0;
            return;
        }
        Paint paint = this.mPaint;
        CharSequence charSequence2 = this.mText;
        this.mIntrinsicWidth = (int) (((double) paint.measureText(charSequence2, 0, charSequence2.length())) + 0.5d);
        this.mIntrinsicHeight = this.mPaint.getFontMetricsInt(null);
    }

    public void draw(Canvas canvas) {
        if (this.mText != null) {
            Rect bounds = getBounds();
            CharSequence charSequence = this.mText;
            canvas.drawText(charSequence, 0, charSequence.length(), (float) bounds.centerX(), (float) bounds.centerY(), this.mPaint);
        }
    }

    public void setDropShadow(boolean z) {
        this.mUseDropShadow = z;
        updatePaint();
    }

    public int getOpacity() {
        return this.mPaint.getAlpha();
    }

    public int getIntrinsicWidth() {
        return this.mIntrinsicWidth;
    }

    public int getIntrinsicHeight() {
        return this.mIntrinsicHeight;
    }

    public void setAlpha(int i) {
        this.mPaint.setAlpha(i);
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
    }
}
