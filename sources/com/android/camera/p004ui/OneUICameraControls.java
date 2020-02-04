package com.android.camera.p004ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.camera.Storage;
import com.android.camera.imageprocessor.filter.BeautificationFilter;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.OneUICameraControls */
public class OneUICameraControls extends RotatableLayout {
    private static final int HEIGHT_GRID = 7;
    private static final int HIGH_REMAINING_PHOTOS = 1000000;
    private static final int LOW_REMAINING_PHOTOS = 20;
    private static final String TAG = "CAM_Controls";
    private static final int WIDTH_GRID = 5;
    private static int mBottom;
    private static int mTop;
    private View mBokehSwitcher;
    private int mBottomLargeSize;
    private int mBottomSize;
    private int mBottomSmallSize;
    private RotateImageView mButton;
    private View mCancelButton;
    private int mCurrentRemaining;
    private View mExitBestPhotpMode;
    private RotateLayout mExposureRotateLayout;
    /* access modifiers changed from: private */
    public TextView mExposureText;
    private View mFilterModeSwitcher;
    private View mFlashButton;
    private View mFrontBackSwitcher;
    private int mHeight;
    private boolean mHideRemainingPhoto;
    private int mIntentMode;
    private boolean mIsVideoMode;
    private RotateLayout mIsoRotateLayout;
    /* access modifiers changed from: private */
    public TextView mIsoText;
    private View mMakeupCleanText;
    private View mMakeupSeekBar;
    private View mMakeupSeekBarHighText;
    private View mMakeupSeekBarLayout;
    private View mMakeupSeekBarLowText;
    private View mMakeupWhitenText;
    private RotateLayout mManualRotateLayout;
    /* access modifiers changed from: private */
    public TextView mManualText;
    private View mModeDetectSwitcher;
    private View mMute;
    private int mOrientation;
    private Paint mPaint;
    private View mPauseButton;
    private View mPreview;
    /* access modifiers changed from: private */
    public ProMode mProMode;
    private View mProModeCloseButton;
    private ViewGroup mProModeLayout;
    private boolean mProModeOn;
    private ArrowTextView mRefocusToast;
    private LinearLayout mRemainingPhotos;
    private TextView mRemainingPhotosText;
    private View mSceneModeSwitcher;
    private View mShutter;
    private View mTsMakeupSwitcher;
    private View mVideoShutter;
    private View[] mViews;
    private boolean mVisible;
    private TextView mWaitProcessText;
    private RotateLayout mWhiteBalanceRotateLayout;
    /* access modifiers changed from: private */
    public TextView mWhiteBalanceText;
    private int mWidth;

    /* renamed from: com.android.camera.ui.OneUICameraControls$ArrowTextView */
    private class ArrowTextView extends TextView {
        private static final int BACKGROUND = Integer.MIN_VALUE;
        private static final int PADDING_SIZE = 18;
        private static final int TEXT_SIZE = 14;
        private Paint mPaint = new Paint();
        private Path mPath;

        public ArrowTextView(Context context) {
            super(context);
            setText(context.getString(C0905R.string.refocus_toast));
            setBackgroundColor(Integer.MIN_VALUE);
            setVisibility(8);
            setLayoutParams(new LayoutParams(-2, -2));
            setTextSize(14.0f);
            setPadding(18, 18, 18, 18);
            this.mPaint.setStyle(Style.FILL);
            this.mPaint.setColor(Integer.MIN_VALUE);
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Path path = this.mPath;
            if (path != null) {
                canvas.drawPath(path, this.mPaint);
            }
        }

        public void setArrow(float f, float f2, float f3, float f4, float f5, float f6) {
            this.mPath = new Path();
            this.mPath.reset();
            this.mPath.moveTo(f, f2);
            this.mPath.lineTo(f3, f4);
            this.mPath.lineTo(f5, f6);
            this.mPath.lineTo(f, f2);
        }
    }

    public OneUICameraControls(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mHideRemainingPhoto = false;
        this.mCurrentRemaining = -1;
        this.mIsVideoMode = false;
        this.mIntentMode = 0;
        this.mProModeOn = false;
        this.mPaint = new Paint(1);
        setWillNotDraw(false);
        this.mRefocusToast = new ArrowTextView(context);
        addView(this.mRefocusToast);
        setClipChildren(false);
        setMeasureAllChildren(true);
        this.mPaint.setColor(getResources().getColor(C0905R.color.camera_control_bg_transparent));
        mTop = (int) TypedValue.applyDimension(1, 80.0f, getResources().getDisplayMetrics());
        mBottom = (int) TypedValue.applyDimension(1, 100.0f, getResources().getDisplayMetrics());
        this.mVisible = true;
        Display defaultDisplay = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        this.mWidth = point.x;
    }

    public OneUICameraControls(Context context) {
        this(context, null);
    }

    public void setIntentMode(int i) {
        this.mIntentMode = i;
    }

    public int getIntentMode() {
        return this.mIntentMode;
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.mShutter = findViewById(C0905R.C0907id.shutter_button);
        this.mVideoShutter = findViewById(C0905R.C0907id.video_button);
        this.mExitBestPhotpMode = findViewById(C0905R.C0907id.exit_best_mode);
        this.mPauseButton = findViewById(C0905R.C0907id.video_pause);
        this.mFrontBackSwitcher = findViewById(C0905R.C0907id.front_back_switcher);
        this.mTsMakeupSwitcher = findViewById(C0905R.C0907id.ts_makeup_switcher);
        this.mMakeupSeekBarLowText = findViewById(C0905R.C0907id.makeup_low_text);
        this.mMakeupSeekBarHighText = findViewById(C0905R.C0907id.makeup_high_text);
        this.mMakeupWhitenText = findViewById(C0905R.C0907id.makeup_whiten_text);
        this.mMakeupCleanText = findViewById(C0905R.C0907id.makeup_clean_text);
        this.mMakeupSeekBar = findViewById(C0905R.C0907id.makeup_seekbar);
        this.mMakeupSeekBarLayout = findViewById(C0905R.C0907id.makeup_seekbar_layout);
        ((SeekBar) this.mMakeupSeekBar).setMax(100);
        this.mFlashButton = findViewById(C0905R.C0907id.flash_button);
        this.mBokehSwitcher = findViewById(C0905R.C0907id.bokeh_switcher);
        this.mModeDetectSwitcher = findViewById(C0905R.C0907id.scenemode_detect_switcher);
        this.mButton = (RotateImageView) findViewById(C0905R.C0907id.setting_button);
        this.mMute = findViewById(C0905R.C0907id.mute_button);
        this.mPreview = findViewById(C0905R.C0907id.preview_thumb);
        this.mSceneModeSwitcher = findViewById(C0905R.C0907id.scene_mode_switcher);
        this.mFilterModeSwitcher = findViewById(C0905R.C0907id.filter_mode_switcher);
        this.mRemainingPhotos = (LinearLayout) findViewById(C0905R.C0907id.remaining_photos);
        this.mRemainingPhotosText = (TextView) findViewById(C0905R.C0907id.remaining_photos_text);
        this.mCancelButton = findViewById(C0905R.C0907id.cancel_button);
        this.mProModeLayout = (ViewGroup) findViewById(C0905R.C0907id.pro_mode_layout);
        this.mProModeCloseButton = findViewById(C0905R.C0907id.promode_close_button);
        this.mShutter.setFocusable(false);
        this.mVideoShutter.setFocusable(false);
        this.mFrontBackSwitcher.setFocusable(false);
        this.mTsMakeupSwitcher.setFocusable(false);
        this.mBokehSwitcher.setFocusable(false);
        this.mFlashButton.setFocusable(false);
        this.mPreview.setFocusable(false);
        this.mSceneModeSwitcher.setFocusable(false);
        this.mFilterModeSwitcher.setFocusable(false);
        this.mExposureText = (TextView) findViewById(C0905R.C0907id.exposure_value);
        this.mManualText = (TextView) findViewById(C0905R.C0907id.manual_value);
        this.mWhiteBalanceText = (TextView) findViewById(C0905R.C0907id.white_balance_value);
        this.mIsoText = (TextView) findViewById(C0905R.C0907id.iso_value);
        this.mWaitProcessText = (TextView) findViewById(C0905R.C0907id.wait_progress_text);
        this.mProMode = (ProMode) findViewById(C0905R.C0907id.promode_slider);
        this.mProMode.initialize(this);
        this.mExposureRotateLayout = (RotateLayout) findViewById(C0905R.C0907id.exposure_rotate_layout);
        this.mManualRotateLayout = (RotateLayout) findViewById(C0905R.C0907id.manual_rotate_layout);
        this.mWhiteBalanceRotateLayout = (RotateLayout) findViewById(C0905R.C0907id.white_balance_rotate_layout);
        this.mIsoRotateLayout = (RotateLayout) findViewById(C0905R.C0907id.iso_rotate_layout);
        this.mExposureText.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                OneUICameraControls.this.resetProModeIcons();
                if (OneUICameraControls.this.mProMode.getMode() == 0) {
                    OneUICameraControls.this.mProMode.setMode(-1);
                    return;
                }
                OneUICameraControls.this.mExposureText.setSelected(true);
                OneUICameraControls.this.mProMode.setMode(0);
            }
        });
        this.mManualText.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                OneUICameraControls.this.resetProModeIcons();
                if (OneUICameraControls.this.mProMode.getMode() == 1) {
                    OneUICameraControls.this.mProMode.setMode(-1);
                    return;
                }
                OneUICameraControls.this.mManualText.setSelected(true);
                OneUICameraControls.this.mProMode.setMode(1);
            }
        });
        this.mWhiteBalanceText.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                OneUICameraControls.this.resetProModeIcons();
                if (OneUICameraControls.this.mProMode.getMode() == 2) {
                    OneUICameraControls.this.mProMode.setMode(-1);
                    return;
                }
                OneUICameraControls.this.mWhiteBalanceText.setSelected(true);
                OneUICameraControls.this.mProMode.setMode(2);
            }
        });
        this.mIsoText.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                OneUICameraControls.this.resetProModeIcons();
                if (OneUICameraControls.this.mProMode.getMode() == 3) {
                    OneUICameraControls.this.mProMode.setMode(-1);
                    return;
                }
                OneUICameraControls.this.mIsoText.setSelected(true);
                OneUICameraControls.this.mProMode.setMode(3);
            }
        });
        this.mViews = new View[]{this.mSceneModeSwitcher, this.mFilterModeSwitcher, this.mFrontBackSwitcher, this.mFlashButton, this.mBokehSwitcher, this.mButton, this.mShutter, this.mPreview, this.mVideoShutter, this.mPauseButton, this.mCancelButton};
        this.mBottomLargeSize = getResources().getDimensionPixelSize(C0905R.dimen.one_ui_bottom_large);
        this.mBottomSmallSize = getResources().getDimensionPixelSize(C0905R.dimen.one_ui_bottom_small);
        this.mBottomSize = getResources().getDimensionPixelSize(C0905R.dimen.one_ui_bottom_size);
        if (!BeautificationFilter.isSupportedStatic()) {
            this.mTsMakeupSwitcher.setEnabled(false);
        }
        setProModeParameters();
    }

    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.mWidth = i;
        this.mHeight = i2;
        View view = this.mMakeupSeekBar;
        if (view != null) {
            view.setMinimumWidth(this.mWidth / 2);
        }
        setProModeParameters();
    }

    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        setLocation((i3 - i) + 0, (i4 - i2) + 0);
        layoutRemaingPhotos();
        initializeProMode(this.mProModeOn);
    }

    public boolean isControlRegion(int i, int i2) {
        return i2 <= mTop || i2 >= this.mHeight - mBottom;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (this.mVisible) {
            int unifiedRotation = getUnifiedRotation();
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            if (unifiedRotation == 90) {
                float f = (float) height;
                canvas.drawRect(0.0f, 0.0f, (float) mTop, f, this.mPaint);
                canvas.drawRect((float) (width - mBottom), 0.0f, (float) width, f, this.mPaint);
            } else if (unifiedRotation == 180) {
                float f2 = (float) width;
                canvas.drawRect(0.0f, 0.0f, f2, (float) mBottom, this.mPaint);
                canvas.drawRect(0.0f, (float) (height - mTop), f2, (float) height, this.mPaint);
            } else if (unifiedRotation != 270) {
                float f3 = (float) width;
                canvas.drawRect(0.0f, 0.0f, f3, (float) mTop, this.mPaint);
                canvas.drawRect(0.0f, (float) (height - mBottom), f3, (float) height, this.mPaint);
            } else {
                float f4 = (float) height;
                canvas.drawRect(0.0f, 0.0f, (float) mBottom, f4, this.mPaint);
                canvas.drawRect((float) (width - mTop), 0.0f, (float) width, f4, this.mPaint);
            }
        }
    }

    private void setLocation(View view, boolean z, float f) {
        if (view != null) {
            int measuredWidth = view.getMeasuredWidth();
            int measuredHeight = view.getMeasuredHeight();
            if (z) {
                view.setY((float) ((mTop - measuredHeight) / 2));
            } else {
                int i = this.mHeight;
                int i2 = mBottom;
                view.setY((float) ((i - i2) + ((i2 - measuredHeight) / 2)));
            }
            float f2 = ((float) this.mWidth) / 5.0f;
            view.setX((f * f2) + ((f2 - ((float) measuredWidth)) / 2.0f));
        }
    }

    private void setSceneModeDetectLocation(View view) {
        if (view != null) {
            int measuredWidth = view.getMeasuredWidth();
            view.setY((float) ((mTop - view.getMeasuredHeight()) * 3));
            float f = ((float) this.mWidth) / 5.0f;
            view.setX((4.0f * f) + ((f - ((float) measuredWidth)) / 2.0f));
        }
    }

    private void setWaitProcessLocation(View view) {
        if (view != null) {
            int measuredWidth = view.getMeasuredWidth();
            view.setY((float) ((this.mHeight - mBottom) - (view.getMeasuredHeight() * 3)));
            float f = ((float) this.mWidth) / 5.0f;
            view.setX((f * 2.0f) + ((f - ((float) measuredWidth)) / 2.0f));
        }
    }

    private void setLocationCustomBottom(View view, float f, float f2) {
        if (view != null) {
            view.getMeasuredWidth();
            int measuredHeight = view.getMeasuredHeight();
            int i = this.mWidth / 5;
            int i2 = this.mHeight;
            int i3 = i2 / 6;
            int i4 = mBottom;
            view.setY(((float) ((i2 - i4) + ((i4 - measuredHeight) / 2))) - (((float) i3) * f2));
            view.setX(((float) i) * f);
        }
    }

    private void setLocation(int i, int i2) {
        int unifiedRotation = getUnifiedRotation();
        setLocation(this.mSceneModeSwitcher, true, 0.0f);
        View view = this.mSceneModeSwitcher;
        int i3 = this.mBottomSize;
        setBottomButtionSize(view, i3, i3);
        setSceneModeDetectLocation(this.mModeDetectSwitcher);
        setWaitProcessLocation(this.mWaitProcessText);
        if (this.mIsVideoMode) {
            setLocation(this.mMute, true, 2.0f);
            setLocation(this.mFlashButton, true, 3.0f);
            setLocation(this.mPauseButton, false, 3.15f);
            setLocation(this.mShutter, false, 0.85f);
            setLocation(this.mVideoShutter, false, 2.0f);
            setLocation(this.mExitBestPhotpMode, false, 4.0f);
        } else {
            setLocation(this.mFlashButton, true, 1.0f);
            setLocation(this.mBokehSwitcher, true, 2.0f);
            setLocation(this.mTsMakeupSwitcher, true, 3.0f);
            setLocation(this.mButton, true, 4.0f);
            View view2 = this.mFlashButton;
            int i4 = this.mBottomSize;
            setBottomButtionSize(view2, i4, i4);
            View view3 = this.mBokehSwitcher;
            int i5 = this.mBottomSize;
            setBottomButtionSize(view3, i5, i5);
            View view4 = this.mTsMakeupSwitcher;
            int i6 = this.mBottomSize;
            setBottomButtionSize(view4, i6, i6);
            RotateImageView rotateImageView = this.mButton;
            int i7 = this.mBottomSize;
            setBottomButtionSize(rotateImageView, i7, i7);
            int i8 = this.mIntentMode;
            if (i8 == 1) {
                setLocation(this.mCancelButton, false, 0.0f);
                setLocation(this.mFilterModeSwitcher, false, 1.0f);
                setLocation(this.mShutter, false, 2.0f);
                setLocation(this.mFrontBackSwitcher, false, 3.0f);
                this.mVideoShutter.setVisibility(8);
                View view5 = this.mShutter;
                int i9 = this.mBottomSmallSize;
                setBottomButtionSize(view5, i9, i9);
                View view6 = this.mCancelButton;
                int i10 = this.mBottomSmallSize;
                setBottomButtionSize(view6, i10, i10);
                View view7 = this.mFrontBackSwitcher;
                int i11 = this.mBottomSmallSize;
                setBottomButtionSize(view7, i11, i11);
                View view8 = this.mFilterModeSwitcher;
                int i12 = this.mBottomSmallSize;
                setBottomButtionSize(view8, i12, i12);
            } else if (i8 == 2) {
                setLocation(this.mVideoShutter, false, 2.0f);
                setLocation(this.mCancelButton, false, 0.85f);
            } else {
                setLocation(this.mShutter, false, 2.0f);
                setLocation(this.mPreview, false, 0.0f);
                setLocation(this.mFilterModeSwitcher, false, 1.0f);
                setLocation(this.mFrontBackSwitcher, false, 3.0f);
                setLocation(this.mVideoShutter, false, 4.0f);
                View view9 = this.mPreview;
                int i13 = this.mBottomSmallSize;
                setBottomButtionSize(view9, i13, i13);
                View view10 = this.mFrontBackSwitcher;
                int i14 = this.mBottomSmallSize;
                setBottomButtionSize(view10, i14, i14);
                View view11 = this.mFilterModeSwitcher;
                int i15 = this.mBottomSmallSize;
                setBottomButtionSize(view11, i15, i15);
            }
            setLocation(this.mExitBestPhotpMode, false, 4.0f);
        }
        setLocationCustomBottom(this.mMakeupSeekBarLayout, 0.0f, 1.0f);
        setLocation(this.mProModeCloseButton, false, 4.0f);
        layoutToast(this.mRefocusToast, i, i2, unifiedRotation);
    }

    private void setBottomButtionSize(View view, int i, int i2) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.height = i2;
        layoutParams.width = i;
        view.setLayoutParams(layoutParams);
    }

    private void layoutToast(View view, int i, int i2, int i3) {
        int i4;
        int i5;
        int i6;
        int i7;
        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();
        if (i3 == 90) {
            int i8 = (int) (((double) (i2 / 5)) * 4.5d);
            int i9 = measuredHeight / 2;
            int i10 = i8 - i9;
            i5 = i8 + i9;
            int i11 = (int) (((double) (i / 7)) * 5.75d);
            int i12 = i11 - measuredWidth;
            float f = (float) measuredWidth;
            float f2 = (float) measuredHeight;
            this.mRefocusToast.setArrow(f, (float) i9, (float) (measuredWidth + i9), f2, f, f2);
            i7 = i11;
            i4 = i10;
            i6 = i12;
        } else if (i3 == 180) {
            i4 = (int) (((double) (i2 / 7)) * 1.25d);
            i5 = i4 + measuredHeight;
            int i13 = (int) (((double) (i / 5)) * 4.75d);
            int i14 = i13 - measuredWidth;
            float f3 = (float) measuredWidth;
            this.mRefocusToast.setArrow((float) (measuredWidth - (measuredHeight / 2)), 0.0f, f3, 0.0f, f3, (float) ((-measuredHeight) / 2));
            i7 = i13;
            i6 = i14;
        } else if (i3 != 270) {
            i6 = (i / 5) / 4;
            i5 = (int) (((double) (i2 / 7)) * 5.75d);
            i7 = measuredWidth + i6;
            i4 = i5 - measuredHeight;
            float f4 = (float) measuredHeight;
            this.mRefocusToast.setArrow(0.0f, f4, (float) (measuredHeight / 2), f4, 0.0f, (float) ((measuredHeight * 3) / 2));
        } else {
            int i15 = (int) (((double) (i2 / 5)) * 0.5d);
            int i16 = measuredHeight / 2;
            int i17 = i15 - i16;
            i5 = i15 + i16;
            i6 = (int) (((double) (i / 7)) * 1.25d);
            i7 = measuredWidth + i6;
            this.mRefocusToast.setArrow(0.0f, 0.0f, 0.0f, (float) i16, (float) ((-measuredHeight) / 2), 0.0f);
            i4 = i17;
        }
        this.mRefocusToast.layout(i6, i4, i7, i5);
    }

    public void hideUI() {
        View[] viewArr;
        for (View view : this.mViews) {
            if (view != null) {
                view.setVisibility(4);
            }
        }
        this.mVisible = false;
    }

    public void showUI() {
        View[] viewArr;
        for (View view : this.mViews) {
            if (view != null) {
                view.setVisibility(0);
            }
        }
        this.mVisible = true;
    }

    public void setVideoMode(boolean z) {
        this.mIsVideoMode = z;
        if (this.mIsVideoMode) {
            View view = this.mVideoShutter;
            int i = this.mBottomLargeSize;
            setBottomButtionSize(view, i, i);
            View view2 = this.mShutter;
            int i2 = this.mBottomSmallSize;
            setBottomButtionSize(view2, i2, i2);
            return;
        }
        int i3 = this.mIntentMode;
        if (i3 == 1) {
            View view3 = this.mShutter;
            int i4 = this.mBottomLargeSize;
            setBottomButtionSize(view3, i4, i4);
        } else if (i3 == 2) {
            View view4 = this.mVideoShutter;
            int i5 = this.mBottomLargeSize;
            setBottomButtionSize(view4, i5, i5);
        } else {
            View view5 = this.mShutter;
            int i6 = this.mBottomLargeSize;
            setBottomButtionSize(view5, i6, i6);
            View view6 = this.mVideoShutter;
            int i7 = this.mBottomSmallSize;
            setBottomButtionSize(view6, i7, i7);
        }
    }

    public boolean getVideoMode() {
        return this.mIsVideoMode;
    }

    private void layoutRemaingPhotos() {
        int left = this.mPreview.getLeft();
        int top = this.mPreview.getTop();
        int right = this.mPreview.getRight();
        int bottom = this.mPreview.getBottom();
        int measuredWidth = this.mRemainingPhotos.getMeasuredWidth();
        int measuredHeight = this.mRemainingPhotos.getMeasuredHeight();
        int i = (left + right) / 2;
        int dimensionPixelSize = ((top + bottom) / 2) - getResources().getDimensionPixelSize(C0905R.dimen.remaining_photos_margin);
        int i2 = this.mOrientation;
        if (i2 == 90 || i2 == 270) {
            dimensionPixelSize -= measuredWidth / 2;
        }
        int i3 = measuredWidth / 2;
        if (i < i3) {
            int i4 = measuredHeight / 2;
            this.mRemainingPhotos.layout(0, dimensionPixelSize - i4, measuredWidth, dimensionPixelSize + i4);
        } else {
            int i5 = measuredHeight / 2;
            this.mRemainingPhotos.layout(i - i3, dimensionPixelSize - i5, i + i3, dimensionPixelSize + i5);
        }
        this.mRemainingPhotos.setRotation((float) (-this.mOrientation));
    }

    public void updateRemainingPhotos(int i) {
        long availableSpace = Storage.getAvailableSpace() - Storage.LOW_STORAGE_THRESHOLD_BYTES;
        if ((i >= 0 || availableSpace > 0) && !this.mHideRemainingPhoto) {
            for (int childCount = this.mRemainingPhotos.getChildCount() - 1; childCount >= 0; childCount--) {
                this.mRemainingPhotos.getChildAt(childCount).setVisibility(0);
            }
            if (i < 20) {
                this.mRemainingPhotosText.setText("<20 ");
            } else if (i >= HIGH_REMAINING_PHOTOS) {
                this.mRemainingPhotosText.setText(">1000000");
            } else {
                TextView textView = this.mRemainingPhotosText;
                StringBuilder sb = new StringBuilder();
                sb.append(i);
                sb.append(" ");
                textView.setText(sb.toString());
            }
        } else {
            this.mRemainingPhotos.setVisibility(8);
        }
        this.mCurrentRemaining = i;
    }

    public void showRefocusToast(boolean z) {
        int i = 0;
        this.mRefocusToast.setVisibility(z ? 0 : 8);
        if (this.mCurrentRemaining > 0 && !this.mHideRemainingPhoto) {
            LinearLayout linearLayout = this.mRemainingPhotos;
            if (z) {
                i = 8;
            }
            linearLayout.setVisibility(i);
        }
    }

    public void setOrientation(int i, boolean z) {
        View[] viewArr;
        this.mOrientation = i;
        for (View view : new View[]{this.mSceneModeSwitcher, this.mFilterModeSwitcher, this.mFrontBackSwitcher, this.mFlashButton, this.mBokehSwitcher, this.mButton, this.mTsMakeupSwitcher, this.mModeDetectSwitcher, this.mPreview, this.mMute, this.mShutter, this.mVideoShutter, this.mMakeupSeekBarLowText, this.mMakeupSeekBarHighText, this.mMakeupWhitenText, this.mMakeupCleanText, this.mPauseButton, this.mExitBestPhotpMode}) {
            if (view != null) {
                ((Rotatable) view).setOrientation(i, z);
            }
        }
        this.mExposureRotateLayout.setOrientation(i, z);
        this.mManualRotateLayout.setOrientation(i, z);
        this.mWhiteBalanceRotateLayout.setOrientation(i, z);
        this.mIsoRotateLayout.setOrientation(i, z);
        this.mProMode.setOrientation(i);
        layoutRemaingPhotos();
    }

    public void setProMode(boolean z) {
        this.mProModeOn = z;
        initializeProMode(this.mProModeOn);
        this.mProMode.reinit();
        resetProModeIcons();
    }

    public void setFixedFocus(boolean z) {
        this.mManualText.setEnabled(!z);
    }

    public int getPromode() {
        ProMode proMode = this.mProMode;
        if (proMode != null) {
            return proMode.getMode();
        }
        return -99;
    }

    /* access modifiers changed from: private */
    public void resetProModeIcons() {
        this.mExposureText.setSelected(false);
        this.mManualText.setSelected(false);
        this.mWhiteBalanceText.setSelected(false);
        this.mIsoText.setSelected(false);
    }

    private void setProModeParameters() {
        int i = this.mWidth;
        LayoutParams layoutParams = new LayoutParams(i / 4, i / 4);
        this.mExposureText.setLayoutParams(layoutParams);
        this.mManualText.setLayoutParams(layoutParams);
        this.mWhiteBalanceText.setLayoutParams(layoutParams);
        this.mIsoText.setLayoutParams(layoutParams);
    }

    private void initializeProMode(boolean z) {
        if (!z) {
            this.mProMode.setMode(-1);
            this.mProModeLayout.setVisibility(4);
            this.mProModeCloseButton.setVisibility(4);
            return;
        }
        this.mProModeLayout.setVisibility(0);
        this.mProModeCloseButton.setVisibility(0);
        ViewGroup viewGroup = this.mProModeLayout;
        viewGroup.setY((float) ((this.mHeight - mBottom) - viewGroup.getHeight()));
    }

    public void updateProModeText(int i, String str) {
        if (i == 0) {
            this.mExposureText.setText(str);
        } else if (i == 1) {
            this.mManualText.setText(str);
        } else if (i == 2) {
            this.mWhiteBalanceText.setText(str);
        } else if (i == 3) {
            this.mIsoText.setText(str);
        }
    }
}
