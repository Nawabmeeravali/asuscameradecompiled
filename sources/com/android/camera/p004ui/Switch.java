package com.android.camera.p004ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CompoundButton;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.Switch */
public class Switch extends CompoundButton {
    private static final int[] CHECKED_STATE_SET = {16842912};
    private static final int TOUCH_MODE_DOWN = 1;
    private static final int TOUCH_MODE_DRAGGING = 2;
    private static final int TOUCH_MODE_IDLE = 0;
    private int mMinFlingVelocity;
    private Layout mOffLayout;
    private Layout mOnLayout;
    private int mSwitchBottom;
    private int mSwitchHeight;
    private int mSwitchLeft;
    private int mSwitchMinWidth;
    private int mSwitchPadding;
    private int mSwitchRight;
    private int mSwitchTextMaxWidth;
    private int mSwitchTop;
    private int mSwitchWidth;
    private final Rect mTempRect;
    private ColorStateList mTextColors;
    private CharSequence mTextOff;
    private CharSequence mTextOn;
    private TextPaint mTextPaint;
    private Drawable mThumbDrawable;
    private float mThumbPosition;
    private int mThumbTextPadding;
    private int mThumbWidth;
    private int mTouchMode;
    private int mTouchSlop;
    private float mTouchX;
    private float mTouchY;
    private Drawable mTrackDrawable;
    private VelocityTracker mVelocityTracker;

    public Switch(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, C0905R.attr.switchStyle);
    }

    public Switch(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mTempRect = new Rect();
        this.mTextPaint = new TextPaint(1);
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        this.mTextPaint.density = displayMetrics.density;
        this.mThumbDrawable = resources.getDrawable(C0905R.C0906drawable.switch_inner_holo_dark);
        this.mTrackDrawable = resources.getDrawable(C0905R.C0906drawable.switch_track_holo_dark);
        this.mTextOn = resources.getString(C0905R.string.capital_on);
        this.mTextOff = resources.getString(C0905R.string.capital_off);
        this.mThumbTextPadding = resources.getDimensionPixelSize(C0905R.dimen.thumb_text_padding);
        this.mSwitchMinWidth = resources.getDimensionPixelSize(C0905R.dimen.switch_min_width);
        this.mSwitchTextMaxWidth = resources.getDimensionPixelSize(C0905R.dimen.switch_text_max_width);
        this.mSwitchPadding = resources.getDimensionPixelSize(C0905R.dimen.switch_padding);
        setSwitchTextAppearance(context, 16974081);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        refreshDrawableState();
        setChecked(isChecked());
    }

    public void setSwitchTextAppearance(Context context, int i) {
        Resources resources = getResources();
        this.mTextColors = getTextColors();
        float dimensionPixelSize = (float) resources.getDimensionPixelSize(C0905R.dimen.thumb_text_size);
        if (dimensionPixelSize != this.mTextPaint.getTextSize()) {
            this.mTextPaint.setTextSize(dimensionPixelSize);
            requestLayout();
        }
    }

    public void onMeasure(int i, int i2) {
        if (this.mOnLayout == null) {
            this.mOnLayout = makeLayout(this.mTextOn, this.mSwitchTextMaxWidth);
        }
        if (this.mOffLayout == null) {
            this.mOffLayout = makeLayout(this.mTextOff, this.mSwitchTextMaxWidth);
        }
        this.mTrackDrawable.getPadding(this.mTempRect);
        int min = Math.min(this.mSwitchTextMaxWidth, Math.max(this.mOnLayout.getWidth(), this.mOffLayout.getWidth()));
        int i3 = this.mSwitchMinWidth;
        int i4 = (min * 2) + (this.mThumbTextPadding * 4);
        Rect rect = this.mTempRect;
        int max = Math.max(i3, i4 + rect.left + rect.right);
        int intrinsicHeight = this.mTrackDrawable.getIntrinsicHeight();
        this.mThumbWidth = min + (this.mThumbTextPadding * 2);
        this.mSwitchWidth = max;
        this.mSwitchHeight = intrinsicHeight;
        super.onMeasure(i, i2);
        int measuredHeight = getMeasuredHeight();
        int measuredWidth = getMeasuredWidth();
        if (measuredHeight < intrinsicHeight) {
            setMeasuredDimension(measuredWidth, intrinsicHeight);
        }
    }

    @TargetApi(14)
    public void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onPopulateAccessibilityEvent(accessibilityEvent);
        CharSequence text = (isChecked() ? this.mOnLayout : this.mOffLayout).getText();
        if (!TextUtils.isEmpty(text)) {
            accessibilityEvent.getText().add(text);
        }
    }

    private Layout makeLayout(CharSequence charSequence, int i) {
        int ceil = (int) Math.ceil((double) Layout.getDesiredWidth(charSequence, this.mTextPaint));
        StaticLayout staticLayout = new StaticLayout(charSequence, 0, charSequence.length(), this.mTextPaint, ceil, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true, TruncateAt.END, Math.min(ceil, i));
        return staticLayout;
    }

    private boolean hitThumb(float f, float f2) {
        this.mThumbDrawable.getPadding(this.mTempRect);
        int i = this.mSwitchTop;
        int i2 = this.mTouchSlop;
        int i3 = i - i2;
        int i4 = (this.mSwitchLeft + ((int) (this.mThumbPosition + 0.5f))) - i2;
        int i5 = this.mThumbWidth + i4;
        Rect rect = this.mTempRect;
        return f > ((float) i4) && f < ((float) (((i5 + rect.left) + rect.right) + i2)) && f2 > ((float) i3) && f2 < ((float) (this.mSwitchBottom + i2));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0012, code lost:
        if (r0 != 3) goto L_0x00a6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r7) {
        /*
            r6 = this;
            android.view.VelocityTracker r0 = r6.mVelocityTracker
            r0.addMovement(r7)
            int r0 = r7.getActionMasked()
            r1 = 1
            if (r0 == 0) goto L_0x008c
            r2 = 2
            if (r0 == r1) goto L_0x007b
            if (r0 == r2) goto L_0x0016
            r3 = 3
            if (r0 == r3) goto L_0x007b
            goto L_0x00a6
        L_0x0016:
            int r0 = r6.mTouchMode
            if (r0 == 0) goto L_0x00a6
            if (r0 == r1) goto L_0x0047
            if (r0 == r2) goto L_0x0020
            goto L_0x00a6
        L_0x0020:
            float r7 = r7.getX()
            float r0 = r6.mTouchX
            float r0 = r7 - r0
            r2 = 0
            float r3 = r6.mThumbPosition
            float r3 = r3 + r0
            int r0 = r6.getThumbScrollRange()
            float r0 = (float) r0
            float r0 = java.lang.Math.min(r3, r0)
            float r0 = java.lang.Math.max(r2, r0)
            float r2 = r6.mThumbPosition
            int r2 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r2 == 0) goto L_0x0046
            r6.mThumbPosition = r0
            r6.mTouchX = r7
            r6.invalidate()
        L_0x0046:
            return r1
        L_0x0047:
            float r0 = r7.getX()
            float r3 = r7.getY()
            float r4 = r6.mTouchX
            float r4 = r0 - r4
            float r4 = java.lang.Math.abs(r4)
            int r5 = r6.mTouchSlop
            float r5 = (float) r5
            int r4 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1))
            if (r4 > 0) goto L_0x006d
            float r4 = r6.mTouchY
            float r4 = r3 - r4
            float r4 = java.lang.Math.abs(r4)
            int r5 = r6.mTouchSlop
            float r5 = (float) r5
            int r4 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1))
            if (r4 <= 0) goto L_0x00a6
        L_0x006d:
            r6.mTouchMode = r2
            android.view.ViewParent r7 = r6.getParent()
            r7.requestDisallowInterceptTouchEvent(r1)
            r6.mTouchX = r0
            r6.mTouchY = r3
            return r1
        L_0x007b:
            int r0 = r6.mTouchMode
            if (r0 != r2) goto L_0x0083
            r6.stopDrag(r7)
            return r1
        L_0x0083:
            r0 = 0
            r6.mTouchMode = r0
            android.view.VelocityTracker r0 = r6.mVelocityTracker
            r0.clear()
            goto L_0x00a6
        L_0x008c:
            float r0 = r7.getX()
            float r2 = r7.getY()
            boolean r3 = r6.isEnabled()
            if (r3 == 0) goto L_0x00a6
            boolean r3 = r6.hitThumb(r0, r2)
            if (r3 == 0) goto L_0x00a6
            r6.mTouchMode = r1
            r6.mTouchX = r0
            r6.mTouchY = r2
        L_0x00a6:
            boolean r6 = super.onTouchEvent(r7)
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.p004ui.Switch.onTouchEvent(android.view.MotionEvent):boolean");
    }

    private void cancelSuperTouch(MotionEvent motionEvent) {
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        obtain.setAction(3);
        super.onTouchEvent(obtain);
        obtain.recycle();
    }

    private void stopDrag(MotionEvent motionEvent) {
        boolean z = false;
        this.mTouchMode = 0;
        boolean z2 = motionEvent.getAction() == 1 && isEnabled();
        cancelSuperTouch(motionEvent);
        if (z2) {
            this.mVelocityTracker.computeCurrentVelocity(1000);
            float xVelocity = this.mVelocityTracker.getXVelocity();
            if (Math.abs(xVelocity) <= ((float) this.mMinFlingVelocity)) {
                z = getTargetCheckedState();
            } else if (xVelocity > 0.0f) {
                z = true;
            }
            animateThumbToCheckedState(z);
            return;
        }
        animateThumbToCheckedState(isChecked());
    }

    private void animateThumbToCheckedState(boolean z) {
        setChecked(z);
    }

    private boolean getTargetCheckedState() {
        return this.mThumbPosition >= ((float) (getThumbScrollRange() / 2));
    }

    private void setThumbPosition(boolean z) {
        this.mThumbPosition = z ? (float) getThumbScrollRange() : 0.0f;
    }

    public void setChecked(boolean z) {
        super.setChecked(z);
        setThumbPosition(z);
        invalidate();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7;
        super.onLayout(z, i, i2, i3, i4);
        setThumbPosition(isChecked());
        int width = getWidth() - getPaddingRight();
        int i8 = width - this.mSwitchWidth;
        int gravity = getGravity() & 112;
        if (gravity == 16) {
            int paddingTop = ((getPaddingTop() + getHeight()) - getPaddingBottom()) / 2;
            i7 = this.mSwitchHeight;
            i6 = paddingTop - (i7 / 2);
        } else if (gravity != 80) {
            i6 = getPaddingTop();
            i7 = this.mSwitchHeight;
        } else {
            i5 = getHeight() - getPaddingBottom();
            i6 = i5 - this.mSwitchHeight;
            this.mSwitchLeft = i8;
            this.mSwitchTop = i6;
            this.mSwitchBottom = i5;
            this.mSwitchRight = width;
        }
        i5 = i7 + i6;
        this.mSwitchLeft = i8;
        this.mSwitchTop = i6;
        this.mSwitchBottom = i5;
        this.mSwitchRight = width;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = this.mSwitchLeft;
        int i2 = this.mSwitchTop;
        int i3 = this.mSwitchRight;
        int i4 = this.mSwitchBottom;
        this.mTrackDrawable.setBounds(i, i2, i3, i4);
        this.mTrackDrawable.draw(canvas);
        canvas.save();
        this.mTrackDrawable.getPadding(this.mTempRect);
        Rect rect = this.mTempRect;
        int i5 = i + rect.left;
        int i6 = rect.top + i2;
        int i7 = i3 - rect.right;
        int i8 = i4 - rect.bottom;
        canvas.clipRect(i5, i2, i7, i4);
        this.mThumbDrawable.getPadding(this.mTempRect);
        int i9 = (int) (this.mThumbPosition + 0.5f);
        Rect rect2 = this.mTempRect;
        int i10 = (i5 - rect2.left) + i9;
        int i11 = i5 + i9 + this.mThumbWidth + rect2.right;
        this.mThumbDrawable.setBounds(i10, i2, i11, i4);
        this.mThumbDrawable.draw(canvas);
        ColorStateList colorStateList = this.mTextColors;
        if (colorStateList != null) {
            this.mTextPaint.setColor(colorStateList.getColorForState(getDrawableState(), this.mTextColors.getDefaultColor()));
        }
        this.mTextPaint.drawableState = getDrawableState();
        Layout layout = getTargetCheckedState() ? this.mOnLayout : this.mOffLayout;
        canvas.translate((float) (((i10 + i11) / 2) - (layout.getEllipsizedWidth() / 2)), (float) (((i6 + i8) / 2) - (layout.getHeight() / 2)));
        layout.draw(canvas);
        canvas.restore();
    }

    public int getCompoundPaddingRight() {
        int compoundPaddingRight = super.getCompoundPaddingRight() + this.mSwitchWidth;
        return !TextUtils.isEmpty(getText()) ? compoundPaddingRight + this.mSwitchPadding : compoundPaddingRight;
    }

    private int getThumbScrollRange() {
        Drawable drawable = this.mTrackDrawable;
        if (drawable == null) {
            return 0;
        }
        drawable.getPadding(this.mTempRect);
        int i = this.mSwitchWidth - this.mThumbWidth;
        Rect rect = this.mTempRect;
        return (i - rect.left) - rect.right;
    }

    /* access modifiers changed from: protected */
    public int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i + 1);
        if (isChecked()) {
            CompoundButton.mergeDrawableStates(onCreateDrawableState, CHECKED_STATE_SET);
        }
        return onCreateDrawableState;
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        int[] drawableState = getDrawableState();
        Drawable drawable = this.mThumbDrawable;
        if (drawable != null) {
            drawable.setState(drawableState);
        }
        Drawable drawable2 = this.mTrackDrawable;
        if (drawable2 != null) {
            drawable2.setState(drawableState);
        }
        invalidate();
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable drawable) {
        return super.verifyDrawable(drawable) || drawable == this.mThumbDrawable || drawable == this.mTrackDrawable;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        this.mThumbDrawable.jumpToCurrentState();
        this.mTrackDrawable.jumpToCurrentState();
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(Switch.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(Switch.class.getName());
        CharSequence charSequence = isChecked() ? this.mTextOn : this.mTextOff;
        if (!TextUtils.isEmpty(charSequence)) {
            CharSequence text = accessibilityNodeInfo.getText();
            if (TextUtils.isEmpty(text)) {
                accessibilityNodeInfo.setText(charSequence);
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(text);
            sb.append(' ');
            sb.append(charSequence);
            accessibilityNodeInfo.setText(sb);
        }
    }
}
