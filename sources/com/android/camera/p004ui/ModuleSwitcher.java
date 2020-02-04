package com.android.camera.p004ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.PhotoSphereHelper;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.ModuleSwitcher */
public class ModuleSwitcher extends RotateImageView implements OnTouchListener {
    public static final int CAPTURE_MODULE_INDEX = 5;
    private static final int[] DRAW_IDS = {C0905R.C0906drawable.ic_switch_camera, C0905R.C0906drawable.ic_switch_video, C0905R.C0906drawable.ic_switch_pan, C0905R.C0906drawable.ic_switch_photosphere, C0905R.C0906drawable.ic_switch_gcam};
    public static final int GCAM_MODULE_INDEX = 4;
    public static final int LIGHTCYCLE_MODULE_INDEX = 3;
    public static final int PANOCAPTURE_MODULE_INDEX = 6;
    public static final int PHOTO_MODULE_INDEX = 0;
    private static final int SWITCHER_POPUP_ANIM_DURATION = 200;
    private static final String TAG = "CAM_Switcher";
    public static final int VIDEO_MODULE_INDEX = 1;
    public static final int WIDE_ANGLE_PANO_MODULE_INDEX = 2;
    private int mCurrentIndex;
    private int[] mDrawIds;
    private AnimatorListener mHideAnimationListener;
    private Drawable mIndicator;
    private boolean mIsVisible = true;
    private int mItemSize;
    private ModuleSwitchListener mListener;
    private int[] mModuleIds;
    private boolean mNeedsAnimationSetup;
    /* access modifiers changed from: private */
    public View mParent;
    /* access modifiers changed from: private */
    public View mPopup;
    private AnimatorListener mShowAnimationListener;
    private boolean mShowingPopup;
    private boolean mTouchEnabled = true;
    private float mTranslationX = 0.0f;
    private float mTranslationY = 0.0f;

    /* renamed from: com.android.camera.ui.ModuleSwitcher$ModuleSwitchListener */
    public interface ModuleSwitchListener {
        void onModuleSelected(int i);

        void onShowSwitcherPopup();
    }

    public ModuleSwitcher(Context context) {
        super(context);
        init(context);
    }

    public ModuleSwitcher(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context) {
        this.mItemSize = context.getResources().getDimensionPixelSize(C0905R.dimen.switcher_size);
        this.mIndicator = context.getResources().getDrawable(C0905R.C0906drawable.ic_switcher_menu_indicator);
        initializeDrawables(context);
    }

    public void initializeDrawables(Context context) {
        int length = DRAW_IDS.length;
        if (!PhotoSphereHelper.hasLightCycleCapture(context)) {
            length--;
        }
        int i = length - 1;
        int[] iArr = new int[i];
        int[] iArr2 = new int[i];
        int i2 = 0;
        for (int i3 = 0; i3 < DRAW_IDS.length; i3++) {
            if ((i3 != 3 || PhotoSphereHelper.hasLightCycleCapture(context)) && i3 != 4) {
                iArr2[i2] = i3;
                int i4 = i2 + 1;
                iArr[i2] = DRAW_IDS[i3];
                i2 = i4;
            }
        }
        setIds(iArr2, iArr);
    }

    public void setIds(int[] iArr, int[] iArr2) {
        this.mDrawIds = iArr2;
        this.mModuleIds = iArr;
    }

    public void setCurrentIndex(int i) {
        this.mCurrentIndex = i;
        if (i == 4) {
            setImageResource(C0905R.C0906drawable.ic_switch_camera);
        }
        if (i != 6) {
            setImageResource(this.mDrawIds[i]);
        }
    }

    public void setSwitchListener(ModuleSwitchListener moduleSwitchListener) {
        this.mListener = moduleSwitchListener;
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (this.mTouchEnabled) {
            return super.dispatchTouchEvent(motionEvent);
        }
        setBackground(null);
        return false;
    }

    public void enableTouch(boolean z) {
        this.mTouchEnabled = z;
    }

    public void showPopup() {
        showSwitcher();
        this.mListener.onShowSwitcherPopup();
    }

    /* access modifiers changed from: private */
    public void onModuleSelected(int i) {
        hidePopup();
        if (i != this.mCurrentIndex && this.mListener != null) {
            setCurrentIndex(i);
            this.mListener.onModuleSelected(this.mModuleIds[i]);
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mIndicator.setBounds(getDrawable().getBounds());
        this.mIndicator.draw(canvas);
    }

    private void initPopup() {
        this.mParent = LayoutInflater.from(getContext()).inflate(C0905R.layout.switcher_popup, (ViewGroup) getParent());
        LinearLayout linearLayout = (LinearLayout) this.mParent.findViewById(C0905R.C0907id.content);
        this.mPopup = linearLayout;
        LayoutParams layoutParams = (LayoutParams) this.mPopup.getLayoutParams();
        layoutParams.gravity = ((LayoutParams) this.mParent.findViewById(C0905R.C0907id.camera_switcher).getLayoutParams()).gravity;
        this.mPopup.setLayoutParams(layoutParams);
        this.mPopup.setVisibility(4);
        this.mNeedsAnimationSetup = true;
        for (final int length = this.mDrawIds.length - 1; length >= 0; length--) {
            RotateImageView rotateImageView = new RotateImageView(getContext());
            rotateImageView.setImageResource(this.mDrawIds[length]);
            rotateImageView.setScaleType(ScaleType.CENTER);
            rotateImageView.setBackgroundResource(C0905R.C0906drawable.bg_pressed);
            rotateImageView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (ModuleSwitcher.this.showsPopup()) {
                        ModuleSwitcher.this.onModuleSelected(length);
                    }
                }
            });
            switch (this.mDrawIds[length]) {
                case C0905R.C0906drawable.ic_switch_camera /*2131165452*/:
                    rotateImageView.setContentDescription(getContext().getResources().getString(C0905R.string.accessibility_switch_to_camera));
                    break;
                case C0905R.C0906drawable.ic_switch_gcam /*2131165454*/:
                    rotateImageView.setContentDescription(getContext().getResources().getString(C0905R.string.accessibility_switch_to_gcam));
                    break;
                case C0905R.C0906drawable.ic_switch_pan /*2131165455*/:
                    rotateImageView.setContentDescription(getContext().getResources().getString(C0905R.string.accessibility_switch_to_panorama));
                    break;
                case C0905R.C0906drawable.ic_switch_photosphere /*2131165459*/:
                    rotateImageView.setContentDescription(getContext().getResources().getString(C0905R.string.accessibility_switch_to_photo_sphere));
                    break;
                case C0905R.C0906drawable.ic_switch_video /*2131165460*/:
                    rotateImageView.setContentDescription(getContext().getResources().getString(C0905R.string.accessibility_switch_to_video));
                    break;
            }
            int i = this.mItemSize;
            linearLayout.addView(rotateImageView, new LinearLayout.LayoutParams(i, i));
        }
        this.mPopup.measure(MeasureSpec.makeMeasureSpec(this.mParent.getWidth(), Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(this.mParent.getHeight(), Integer.MIN_VALUE));
    }

    public boolean showsPopup() {
        return this.mShowingPopup;
    }

    public boolean isInsidePopup(MotionEvent motionEvent) {
        boolean z = false;
        if (!showsPopup()) {
            return false;
        }
        int[] iArr = new int[2];
        this.mPopup.getLocationOnScreen(iArr);
        int i = iArr[0];
        int i2 = iArr[1];
        int height = this.mPopup.getHeight() + i2;
        int width = this.mPopup.getWidth() + i;
        if (motionEvent.getX() >= ((float) i) && motionEvent.getX() < ((float) width) && motionEvent.getY() >= ((float) i2) && motionEvent.getY() < ((float) height)) {
            z = true;
        }
        return z;
    }

    private void hidePopup() {
        this.mShowingPopup = false;
        setVisibility(0);
        if (this.mPopup != null && !animateHidePopup()) {
            this.mPopup.setVisibility(4);
        }
        this.mParent.setOnTouchListener(null);
    }

    public void setSwitcherVisibility(boolean z) {
        this.mIsVisible = z;
    }

    public void removePopup() {
        this.mShowingPopup = false;
        if (this.mIsVisible) {
            setVisibility(0);
        }
        View view = this.mPopup;
        if (view != null) {
            ((ViewGroup) this.mParent).removeView(view);
            this.mPopup = null;
        }
        setAlpha(1.0f);
    }

    public void onConfigurationChanged(Configuration configuration) {
        if (showsPopup()) {
            ((ViewGroup) this.mParent).removeView(this.mPopup);
            this.mPopup = null;
            initPopup();
            this.mPopup.setVisibility(0);
        }
    }

    private void showSwitcher() {
        this.mShowingPopup = true;
        if (this.mPopup == null) {
            initPopup();
        }
        layoutPopup();
        this.mPopup.setVisibility(0);
        if (!animateShowPopup()) {
            setVisibility(4);
        }
        this.mParent.setOnTouchListener(this);
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        closePopup();
        return true;
    }

    public void closePopup() {
        if (showsPopup()) {
            hidePopup();
        }
    }

    public void setOrientation(int i, boolean z) {
        super.setOrientation(i, z);
        ViewGroup viewGroup = (ViewGroup) this.mPopup;
        if (viewGroup != null) {
            for (int i2 = 0; i2 < viewGroup.getChildCount(); i2++) {
                ((RotateImageView) viewGroup.getChildAt(i2)).setOrientation(i, z);
            }
        }
    }

    private void layoutPopup() {
        int displayRotation = CameraUtil.getDisplayRotation((Activity) getContext());
        int measuredWidth = this.mPopup.getMeasuredWidth();
        int measuredHeight = this.mPopup.getMeasuredHeight();
        if (displayRotation == 0) {
            this.mPopup.layout(getRight() - measuredWidth, getBottom() - measuredHeight, getRight(), getBottom());
            this.mTranslationX = 0.0f;
            this.mTranslationY = (float) (measuredHeight / 3);
        } else if (displayRotation == 90) {
            this.mTranslationX = (float) (measuredWidth / 3);
            this.mTranslationY = (float) ((-measuredHeight) / 3);
            this.mPopup.layout(getRight() - measuredWidth, getTop(), getRight(), getTop() + measuredHeight);
        } else if (displayRotation == 180) {
            this.mTranslationX = (float) ((-measuredWidth) / 3);
            this.mTranslationY = (float) ((-measuredHeight) / 3);
            this.mPopup.layout(getLeft(), getTop(), getLeft() + measuredWidth, getTop() + measuredHeight);
        } else {
            this.mTranslationX = (float) ((-measuredWidth) / 3);
            this.mTranslationY = (float) (measuredHeight - getHeight());
            this.mPopup.layout(getLeft(), getBottom() - measuredHeight, getLeft() + measuredWidth, getBottom());
        }
    }

    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.mPopup != null) {
            layoutPopup();
        }
    }

    private void popupAnimationSetup() {
        layoutPopup();
        this.mPopup.setScaleX(0.3f);
        this.mPopup.setScaleY(0.3f);
        this.mPopup.setTranslationX(this.mTranslationX);
        this.mPopup.setTranslationY(this.mTranslationY);
        this.mNeedsAnimationSetup = false;
    }

    private boolean animateHidePopup() {
        if (this.mHideAnimationListener == null) {
            this.mHideAnimationListener = new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    if (!ModuleSwitcher.this.showsPopup() && ModuleSwitcher.this.mPopup != null) {
                        ModuleSwitcher.this.mPopup.setVisibility(4);
                        ((ViewGroup) ModuleSwitcher.this.mParent).removeView(ModuleSwitcher.this.mPopup);
                        ModuleSwitcher.this.mPopup = null;
                    }
                }
            };
        }
        this.mPopup.animate().alpha(0.0f).scaleX(0.3f).scaleY(0.3f).translationX(this.mTranslationX).translationY(this.mTranslationY).setDuration(200).setListener(this.mHideAnimationListener);
        animate().alpha(1.0f).setDuration(200).setListener(null);
        return true;
    }

    private boolean animateShowPopup() {
        if (this.mNeedsAnimationSetup) {
            popupAnimationSetup();
        }
        if (this.mShowAnimationListener == null) {
            this.mShowAnimationListener = new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    if (ModuleSwitcher.this.showsPopup()) {
                        ModuleSwitcher.this.setVisibility(4);
                        ModuleSwitcher.this.mPopup.requestLayout();
                    }
                }
            };
        }
        this.mPopup.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).translationX(0.0f).translationY(0.0f).setDuration(200).setListener(null);
        animate().alpha(0.0f).setDuration(200).setListener(this.mShowAnimationListener);
        return true;
    }
}
