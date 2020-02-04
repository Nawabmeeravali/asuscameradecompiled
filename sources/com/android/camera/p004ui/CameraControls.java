package com.android.camera.p004ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.camera.ShutterButton;
import com.android.camera.Storage;
import com.android.camera.TsMakeupManager;
import com.android.camera.util.CameraUtil;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.CameraControls */
public class CameraControls extends RotatableLayout {
    private static final int ANIME_DURATION = 300;
    private static final int BOKEH_INDEX = 12;
    private static final int EXIT_PANORAMA_INDEX = 11;
    private static final int FILTER_MODE_INDEX = 3;
    private static final int FRONT_BACK_INDEX = 0;
    private static final int HDR_INDEX = 1;
    private static final int HEIGHT_GRID = 7;
    private static final int HIGH_REMAINING_PHOTOS = 1000000;
    private static final int INDICATOR_INDEX = 8;
    private static final int LOW_REMAINING_PHOTOS = 20;
    private static final int MAX_INDEX = 13;
    private static final int MENU_INDEX = 4;
    private static final int MUTE_INDEX = 9;
    private static final int PREVIEW_INDEX = 7;
    private static final int SCENE_MODE_INDEX = 2;
    private static final int SHUTTER_INDEX = 6;
    private static final int SWITCHER_INDEX = 5;
    private static final String TAG = "CAM_Controls";
    private static final int TS_MAKEUP_INDEX = 1;
    private static final int VIDEO_SHUTTER_INDEX = 10;
    private static int WIDTH_GRID = 5;
    /* access modifiers changed from: private */
    public static boolean isAnimating = false;
    private static int mBottomMargin;
    private static int mTopMargin;
    AnimatorListener inlistener;
    private View mBackgroundView;
    /* access modifiers changed from: private */
    public View mBokehSwitcher;
    private int mCurrentRemaining;
    /* access modifiers changed from: private */
    public View mExitPanorama;
    /* access modifiers changed from: private */
    public View mFilterModeSwitcher;
    /* access modifiers changed from: private */
    public View mFrontBackSwitcher;
    /* access modifiers changed from: private */
    public View mHdrSwitcher;
    private boolean mHideRemainingPhoto;
    /* access modifiers changed from: private */
    public View mIndicators;
    /* access modifiers changed from: private */
    public boolean mIsBokehMode;
    private boolean mLocSet;
    private float[][] mLocX;
    private float[][] mLocY;
    /* access modifiers changed from: private */
    public View mMenu;
    /* access modifiers changed from: private */
    public View mMute;
    private int mOrientation;
    private Paint mPaint;
    /* access modifiers changed from: private */
    public View mPreview;
    private int mPreviewRatio;
    private ArrowTextView mRefocusToast;
    private LinearLayout mRemainingPhotos;
    private TextView mRemainingPhotosText;
    private View mReviewCancelButton;
    private View mReviewDoneButton;
    private View mReviewRetakeButton;
    /* access modifiers changed from: private */
    public View mSceneModeSwitcher;
    /* access modifiers changed from: private */
    public View mShutter;
    private int mSize;
    /* access modifiers changed from: private */
    public View mSwitcher;
    /* access modifiers changed from: private */
    public View mTsMakeupSwitcher;
    /* access modifiers changed from: private */
    public View mVideoShutter;
    private ArrayList<View> mViewList;
    AnimatorListener outlistener;

    /* renamed from: com.android.camera.ui.CameraControls$ArrowTextView */
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

    public View getPanoramaExitButton() {
        return this.mExitPanorama;
    }

    public CameraControls(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLocX = (float[][]) Array.newInstance(float.class, new int[]{4, 13});
        this.mLocY = (float[][]) Array.newInstance(float.class, new int[]{4, 13});
        this.mLocSet = false;
        this.mHideRemainingPhoto = false;
        this.mCurrentRemaining = -1;
        this.mIsBokehMode = false;
        this.outlistener = new AnimatorListener() {
            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                CameraControls.this.resetLocation(0.0f, 0.0f);
                CameraControls.this.mFrontBackSwitcher.setVisibility(4);
                if (TsMakeupManager.HAS_TS_MAKEUP) {
                    CameraControls.this.mTsMakeupSwitcher.setVisibility(4);
                } else {
                    CameraControls.this.mHdrSwitcher.setVisibility(4);
                }
                if (CameraControls.this.mIsBokehMode) {
                    CameraControls.this.mBokehSwitcher.setVisibility(4);
                }
                CameraControls.this.mSceneModeSwitcher.setVisibility(4);
                CameraControls.this.mFilterModeSwitcher.setVisibility(4);
                CameraControls.this.mSwitcher.setVisibility(4);
                CameraControls.this.mShutter.setVisibility(4);
                CameraControls.this.mVideoShutter.setVisibility(4);
                CameraControls.this.mMenu.setVisibility(4);
                CameraControls.this.mMute.setVisibility(4);
                CameraControls.this.mExitPanorama.setVisibility(4);
                CameraControls.this.mIndicators.setVisibility(4);
                CameraControls.this.mPreview.setVisibility(4);
                CameraControls.isAnimating = false;
                CameraControls.this.enableTouch(true);
            }

            public void onAnimationCancel(Animator animator) {
                CameraControls.this.resetLocation(0.0f, 0.0f);
                CameraControls.this.mFrontBackSwitcher.setVisibility(4);
                if (TsMakeupManager.HAS_TS_MAKEUP) {
                    CameraControls.this.mTsMakeupSwitcher.setVisibility(4);
                } else {
                    CameraControls.this.mHdrSwitcher.setVisibility(4);
                }
                if (CameraControls.this.mIsBokehMode) {
                    CameraControls.this.mBokehSwitcher.setVisibility(4);
                }
                CameraControls.this.mSceneModeSwitcher.setVisibility(4);
                CameraControls.this.mFilterModeSwitcher.setVisibility(4);
                CameraControls.this.mSwitcher.setVisibility(4);
                CameraControls.this.mShutter.setVisibility(4);
                CameraControls.this.mVideoShutter.setVisibility(4);
                CameraControls.this.mMenu.setVisibility(4);
                CameraControls.this.mMute.setVisibility(4);
                CameraControls.this.mExitPanorama.setVisibility(4);
                CameraControls.this.mIndicators.setVisibility(4);
                CameraControls.this.mPreview.setVisibility(4);
                CameraControls.isAnimating = false;
                CameraControls.this.enableTouch(true);
            }
        };
        this.inlistener = new AnimatorListener() {
            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                CameraControls.isAnimating = false;
                CameraControls.this.resetLocation(0.0f, 0.0f);
                CameraControls.this.enableTouch(true);
            }

            public void onAnimationCancel(Animator animator) {
                CameraControls.isAnimating = false;
                CameraControls.this.resetLocation(0.0f, 0.0f);
                CameraControls.this.enableTouch(true);
            }
        };
        this.mPaint = new Paint(1);
        setWillNotDraw(false);
        this.mRefocusToast = new ArrowTextView(context);
        addView(this.mRefocusToast);
        setClipChildren(false);
        setMeasureAllChildren(true);
    }

    public CameraControls(Context context) {
        this(context, null);
    }

    public static boolean isAnimating() {
        return isAnimating;
    }

    public void enableTouch(boolean z) {
        boolean z2 = false;
        if (z) {
            ((ShutterButton) this.mShutter).setPressed(false);
            this.mVideoShutter.setPressed(false);
            this.mSwitcher.setPressed(false);
            this.mMenu.setPressed(false);
            this.mMute.setPressed(false);
            this.mExitPanorama.setPressed(false);
            this.mFrontBackSwitcher.setPressed(false);
            if (TsMakeupManager.HAS_TS_MAKEUP) {
                this.mTsMakeupSwitcher.setPressed(false);
            } else {
                this.mHdrSwitcher.setPressed(false);
            }
            if (this.mIsBokehMode) {
                this.mBokehSwitcher.setPressed(false);
            }
            this.mSceneModeSwitcher.setPressed(false);
        }
        ((ShutterButton) this.mShutter).enableTouch(z);
        this.mVideoShutter.setClickable(z);
        ((ModuleSwitcher) this.mSwitcher).enableTouch(z);
        this.mMenu.setEnabled(z);
        this.mMute.setEnabled(z);
        this.mExitPanorama.setEnabled(z);
        this.mFrontBackSwitcher.setEnabled(z);
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            this.mTsMakeupSwitcher.setEnabled(z);
        } else {
            View view = this.mHdrSwitcher;
            if (z && !this.mIsBokehMode) {
                z2 = true;
            }
            view.setEnabled(z2);
        }
        if (this.mIsBokehMode) {
            this.mBokehSwitcher.setEnabled(z);
        }
        this.mPreview.setEnabled(z);
    }

    private void markVisibility() {
        this.mViewList = new ArrayList<>();
        if (this.mFrontBackSwitcher.getVisibility() == 0) {
            this.mViewList.add(this.mFrontBackSwitcher);
        }
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            if (this.mTsMakeupSwitcher.getVisibility() == 0) {
                this.mViewList.add(this.mTsMakeupSwitcher);
            }
        } else if (this.mHdrSwitcher.getVisibility() == 0) {
            this.mViewList.add(this.mHdrSwitcher);
        }
        if (this.mIsBokehMode && this.mBokehSwitcher.getVisibility() == 0) {
            this.mViewList.add(this.mBokehSwitcher);
        }
        if (this.mSceneModeSwitcher.getVisibility() == 0) {
            this.mViewList.add(this.mSceneModeSwitcher);
        }
        if (this.mFilterModeSwitcher.getVisibility() == 0) {
            this.mViewList.add(this.mFilterModeSwitcher);
        }
        if (this.mShutter.getVisibility() == 0) {
            this.mViewList.add(this.mShutter);
        }
        if (this.mVideoShutter.getVisibility() == 0) {
            this.mViewList.add(this.mVideoShutter);
        }
        if (this.mMenu.getVisibility() == 0) {
            this.mViewList.add(this.mMenu);
        }
        if (this.mMute.getVisibility() == 0) {
            this.mViewList.add(this.mMute);
        }
        if (this.mExitPanorama.getVisibility() == 0) {
            this.mViewList.add(this.mExitPanorama);
        }
        if (this.mIndicators.getVisibility() == 0) {
            this.mViewList.add(this.mIndicators);
        }
    }

    public void removeFromViewList(View view) {
        ArrayList<View> arrayList = this.mViewList;
        if (arrayList != null) {
            arrayList.remove(view);
        }
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.mBackgroundView = findViewById(C0905R.C0907id.blocker);
        this.mSwitcher = findViewById(C0905R.C0907id.camera_switcher);
        this.mShutter = findViewById(C0905R.C0907id.shutter_button);
        this.mVideoShutter = findViewById(C0905R.C0907id.video_button);
        this.mFrontBackSwitcher = findViewById(C0905R.C0907id.front_back_switcher);
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            this.mTsMakeupSwitcher = findViewById(C0905R.C0907id.ts_makeup_switcher);
        } else {
            this.mHdrSwitcher = findViewById(C0905R.C0907id.hdr_switcher);
        }
        this.mBokehSwitcher = findViewById(C0905R.C0907id.bokeh_switcher);
        this.mMenu = findViewById(C0905R.C0907id.menu);
        this.mMute = findViewById(C0905R.C0907id.mute_button);
        this.mExitPanorama = findViewById(C0905R.C0907id.exit_panorama);
        this.mExitPanorama.setVisibility(8);
        this.mIndicators = findViewById(C0905R.C0907id.on_screen_indicators);
        this.mPreview = findViewById(C0905R.C0907id.preview_thumb);
        this.mSceneModeSwitcher = findViewById(C0905R.C0907id.scene_mode_switcher);
        this.mFilterModeSwitcher = findViewById(C0905R.C0907id.filter_mode_switcher);
        this.mRemainingPhotos = (LinearLayout) findViewById(C0905R.C0907id.remaining_photos);
        this.mRemainingPhotosText = (TextView) findViewById(C0905R.C0907id.remaining_photos_text);
    }

    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5 = getResources().getConfiguration().orientation;
        getResources().getDimensionPixelSize(C0905R.dimen.camera_controls_size);
        int unifiedRotation = getUnifiedRotation();
        adjustBackground();
        int i6 = i3 - i;
        int i7 = i4 - i2;
        for (int i8 = 0; i8 < getChildCount(); i8++) {
            getChildAt(i8).layout(0, 0, i6, i7);
        }
        Rect rect = new Rect();
        int i9 = i6;
        int i10 = i7;
        int i11 = i5;
        int i12 = unifiedRotation;
        Rect rect2 = rect;
        center(this.mShutter, 0, 0, i9, i10, i11, i12, rect, 6);
        this.mSize = (int) (((float) Math.max(rect2.right - rect2.left, rect2.bottom - rect2.top)) * 1.2f);
        center(this.mBackgroundView, 0, 0, i9, i10, i11, i12, new Rect(), -1);
        this.mBackgroundView.setVisibility(8);
        setLocation(i6 - 0, i7 - 0);
        View findViewById = findViewById(C0905R.C0907id.btn_retake);
        if (findViewById != null) {
            this.mReviewRetakeButton = findViewById;
            this.mReviewCancelButton = findViewById(C0905R.C0907id.btn_cancel);
            this.mReviewDoneButton = findViewById(C0905R.C0907id.done_button);
            center(this.mReviewRetakeButton, rect2, unifiedRotation);
            toLeft(this.mReviewCancelButton, rect2, unifiedRotation);
            toRight(this.mReviewDoneButton, rect2, unifiedRotation);
        } else {
            this.mReviewRetakeButton = null;
            this.mReviewCancelButton = null;
            this.mReviewDoneButton = null;
        }
        layoutRemaingPhotos();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (mTopMargin != 0) {
            int unifiedRotation = getUnifiedRotation();
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            if (unifiedRotation == 90) {
                float f = (float) height;
                canvas.drawRect(0.0f, 0.0f, (float) mTopMargin, f, this.mPaint);
                canvas.drawRect((float) (width - mBottomMargin), 0.0f, (float) width, f, this.mPaint);
            } else if (unifiedRotation == 180) {
                float f2 = (float) width;
                canvas.drawRect(0.0f, 0.0f, f2, (float) mBottomMargin, this.mPaint);
                canvas.drawRect(0.0f, (float) (height - mTopMargin), f2, (float) height, this.mPaint);
            } else if (unifiedRotation != 270) {
                float f3 = (float) width;
                canvas.drawRect(0.0f, 0.0f, f3, (float) mTopMargin, this.mPaint);
                canvas.drawRect(0.0f, (float) (height - mBottomMargin), f3, (float) height, this.mPaint);
            } else {
                float f4 = (float) height;
                canvas.drawRect(0.0f, 0.0f, (float) mBottomMargin, f4, this.mPaint);
                canvas.drawRect((float) (width - mTopMargin), 0.0f, (float) width, f4, this.mPaint);
            }
        }
    }

    private void setLocation(int i, int i2) {
        int unifiedRotation = getUnifiedRotation();
        if (this.mIsBokehMode) {
            int i3 = i;
            int i4 = i2;
            int i5 = unifiedRotation;
            toIndex(this.mSwitcher, i3, i4, i5, 5, 6, 5);
            toIndex(this.mBokehSwitcher, i3, i4, i5, 5, 0, 12);
        } else {
            toIndex(this.mSwitcher, i, i2, unifiedRotation, 4, 6, 5);
        }
        int i6 = i;
        int i7 = i2;
        int i8 = unifiedRotation;
        toIndex(this.mVideoShutter, i6, i7, i8, 3, 6, 10);
        toIndex(this.mMenu, i6, i7, i8, 4, 0, 4);
        toIndex(this.mMute, i6, i7, i8, 3, 0, 9);
        toIndex(this.mExitPanorama, i6, i7, i8, 0, 0, 11);
        toIndex(this.mIndicators, i6, i7, i8, 0, 6, 8);
        toIndex(this.mFrontBackSwitcher, i6, i7, i8, 2, 0, 0);
        toIndex(this.mPreview, i6, i7, i8, 0, 6, 7);
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            toIndex(this.mTsMakeupSwitcher, i, i2, unifiedRotation, 3, 0, 1);
        } else {
            toIndex(this.mHdrSwitcher, i, i2, unifiedRotation, 3, 0, 1);
        }
        int i9 = i;
        int i10 = i2;
        int i11 = unifiedRotation;
        toIndex(this.mFilterModeSwitcher, i9, i10, i11, 1, 0, 3);
        toIndex(this.mSceneModeSwitcher, i9, i10, i11, 0, 0, 2);
        layoutToast(this.mRefocusToast, i, i2, unifiedRotation);
    }

    private void layoutToast(View view, int i, int i2, int i3) {
        int i4;
        int i5;
        int i6;
        int i7;
        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();
        if (i3 == 90) {
            int i8 = WIDTH_GRID;
            int i9 = (int) (((double) (i2 / i8)) * (((double) i8) - 0.5d));
            int i10 = measuredHeight / 2;
            int i11 = i9 - i10;
            i5 = i9 + i10;
            int i12 = (int) (((double) (i / 7)) * 5.75d);
            int i13 = i12 - measuredWidth;
            float f = (float) measuredWidth;
            float f2 = (float) measuredHeight;
            this.mRefocusToast.setArrow(f, (float) i10, (float) (measuredWidth + i10), f2, f, f2);
            i7 = i12;
            i4 = i11;
            i6 = i13;
        } else if (i3 == 180) {
            i4 = (int) (((double) (i2 / 7)) * 1.25d);
            i5 = i4 + measuredHeight;
            int i14 = WIDTH_GRID;
            int i15 = (int) (((double) (i / i14)) * (((double) i14) - 0.25d));
            int i16 = i15 - measuredWidth;
            float f3 = (float) measuredWidth;
            this.mRefocusToast.setArrow((float) (measuredWidth - (measuredHeight / 2)), 0.0f, f3, 0.0f, f3, (float) ((-measuredHeight) / 2));
            i7 = i15;
            i6 = i16;
        } else if (i3 != 270) {
            i6 = (i / WIDTH_GRID) / 4;
            i5 = (int) (((double) (i2 / 7)) * 5.75d);
            i7 = measuredWidth + i6;
            i4 = i5 - measuredHeight;
            float f4 = (float) measuredHeight;
            this.mRefocusToast.setArrow(0.0f, f4, (float) (measuredHeight / 2), f4, 0.0f, (float) ((measuredHeight * 3) / 2));
        } else {
            int i17 = (int) (((double) (i2 / WIDTH_GRID)) * 0.5d);
            int i18 = measuredHeight / 2;
            int i19 = i17 - i18;
            i5 = i17 + i18;
            i6 = (int) (((double) (i / 7)) * 1.25d);
            i7 = measuredWidth + i6;
            this.mRefocusToast.setArrow(0.0f, 0.0f, 0.0f, (float) i18, (float) ((-measuredHeight) / 2), 0.0f);
            i4 = i19;
        }
        this.mRefocusToast.layout(i6, i4, i7, i5);
    }

    private void center(View view, int i, int i2, int i3, int i4, int i5, int i6, Rect rect, int i7) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        int measuredWidth = layoutParams.leftMargin + view.getMeasuredWidth() + layoutParams.rightMargin;
        int measuredHeight = layoutParams.topMargin + view.getMeasuredHeight() + layoutParams.bottomMargin;
        if (i6 == 0) {
            int i8 = (i3 + i) / 2;
            int i9 = measuredWidth / 2;
            rect.left = (i8 - i9) + layoutParams.leftMargin;
            rect.right = (i8 + i9) - layoutParams.rightMargin;
            rect.bottom = i4 - layoutParams.bottomMargin;
            rect.top = (i4 - measuredHeight) + layoutParams.topMargin;
        } else if (i6 == 90) {
            rect.right = i3 - layoutParams.rightMargin;
            rect.left = (i3 - measuredWidth) + layoutParams.leftMargin;
            int i10 = (i4 + i2) / 2;
            int i11 = measuredHeight / 2;
            rect.top = (i10 - i11) + layoutParams.topMargin;
            rect.bottom = (i10 + i11) - layoutParams.bottomMargin;
        } else if (i6 == 180) {
            int i12 = (i3 + i) / 2;
            int i13 = measuredWidth / 2;
            rect.left = (i12 - i13) + layoutParams.leftMargin;
            rect.right = (i12 + i13) - layoutParams.rightMargin;
            rect.top = layoutParams.topMargin + i2;
            rect.bottom = (i2 + measuredHeight) - layoutParams.bottomMargin;
        } else if (i6 == 270) {
            rect.left = layoutParams.leftMargin + i;
            rect.right = (i + measuredWidth) - layoutParams.rightMargin;
            int i14 = (i4 + i2) / 2;
            int i15 = measuredHeight / 2;
            rect.top = (i14 - i15) + layoutParams.topMargin;
            rect.bottom = (i14 + i15) - layoutParams.bottomMargin;
        }
        view.layout(rect.left, rect.top, rect.right, rect.bottom);
        if (i7 != -1) {
            int i16 = i6 / 90;
            this.mLocX[i16][i7] = (float) rect.left;
            this.mLocY[i16][i7] = (float) rect.top;
        }
    }

    /* access modifiers changed from: private */
    public void resetLocation(float f, float f2) {
        int unifiedRotation = getUnifiedRotation() / 90;
        this.mFrontBackSwitcher.setX(this.mLocX[unifiedRotation][0] + f);
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            this.mTsMakeupSwitcher.setX(this.mLocX[unifiedRotation][1] + f);
        } else {
            this.mHdrSwitcher.setX(this.mLocX[unifiedRotation][1] + f);
        }
        if (this.mIsBokehMode) {
            this.mBokehSwitcher.setX(this.mLocX[unifiedRotation][12] + f);
        }
        this.mSceneModeSwitcher.setX(this.mLocX[unifiedRotation][2] + f);
        this.mFilterModeSwitcher.setX(this.mLocX[unifiedRotation][3] + f);
        this.mMenu.setX(this.mLocX[unifiedRotation][4] + f);
        this.mMute.setX(this.mLocX[unifiedRotation][9] + f);
        this.mExitPanorama.setX(this.mLocX[unifiedRotation][11] + f);
        this.mSwitcher.setX(this.mLocX[unifiedRotation][5] - f);
        this.mShutter.setX(this.mLocX[unifiedRotation][6] - f);
        this.mVideoShutter.setX(this.mLocX[unifiedRotation][10] - f);
        this.mIndicators.setX(this.mLocX[unifiedRotation][8] - f);
        this.mPreview.setX(this.mLocX[unifiedRotation][7] - f);
        this.mFrontBackSwitcher.setY(this.mLocY[unifiedRotation][0] + f2);
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            this.mTsMakeupSwitcher.setY(this.mLocY[unifiedRotation][1] + f2);
        } else {
            this.mHdrSwitcher.setY(this.mLocY[unifiedRotation][1] + f2);
        }
        if (this.mIsBokehMode) {
            this.mBokehSwitcher.setY(this.mLocY[unifiedRotation][12] + f2);
        }
        this.mSceneModeSwitcher.setY(this.mLocY[unifiedRotation][2] + f2);
        this.mFilterModeSwitcher.setY(this.mLocY[unifiedRotation][3] + f2);
        this.mMenu.setY(this.mLocY[unifiedRotation][4] + f2);
        this.mMute.setY(this.mLocY[unifiedRotation][9] + f2);
        this.mExitPanorama.setY(this.mLocY[unifiedRotation][11] + f2);
        this.mSwitcher.setY(this.mLocY[unifiedRotation][5] - f2);
        this.mShutter.setY(this.mLocY[unifiedRotation][6] - f2);
        this.mVideoShutter.setY(this.mLocY[unifiedRotation][10] - f2);
        this.mIndicators.setY(this.mLocY[unifiedRotation][8] - f2);
        this.mPreview.setY(this.mLocY[unifiedRotation][7] - f2);
    }

    public void setTitleBarVisibility(int i) {
        this.mFrontBackSwitcher.setVisibility(i);
        this.mMenu.setVisibility(i);
        this.mSceneModeSwitcher.setVisibility(i);
        this.mFilterModeSwitcher.setVisibility(i);
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            this.mTsMakeupSwitcher.setVisibility(i);
        } else {
            this.mHdrSwitcher.setVisibility(i);
        }
        if (this.mIsBokehMode) {
            this.mBokehSwitcher.setVisibility(i);
        }
    }

    public void setBokehMode(boolean z) {
        this.mIsBokehMode = z;
        if (this.mIsBokehMode) {
            WIDTH_GRID = 6;
        } else {
            WIDTH_GRID = 5;
            this.mBokehSwitcher.setVisibility(8);
        }
        requestLayout();
    }

    public void hideUI() {
        if (!isAnimating) {
            enableTouch(false);
        }
        isAnimating = true;
        int unifiedRotation = getUnifiedRotation();
        this.mFrontBackSwitcher.animate().cancel();
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            this.mTsMakeupSwitcher.animate().cancel();
        } else {
            this.mHdrSwitcher.animate().cancel();
        }
        if (this.mIsBokehMode) {
            this.mBokehSwitcher.animate().cancel();
        }
        this.mSceneModeSwitcher.animate().cancel();
        this.mFilterModeSwitcher.animate().cancel();
        this.mSwitcher.animate().cancel();
        this.mShutter.animate().cancel();
        this.mVideoShutter.animate().cancel();
        this.mMenu.animate().cancel();
        this.mMute.animate().cancel();
        this.mExitPanorama.animate().cancel();
        this.mIndicators.animate().cancel();
        this.mPreview.animate().cancel();
        this.mFrontBackSwitcher.animate().setListener(this.outlistener);
        ((ModuleSwitcher) this.mSwitcher).removePopup();
        resetLocation(0.0f, 0.0f);
        markVisibility();
        if (unifiedRotation == 0) {
            this.mFrontBackSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            if (TsMakeupManager.HAS_TS_MAKEUP) {
                this.mTsMakeupSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            } else {
                this.mHdrSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            }
            if (this.mIsBokehMode) {
                this.mBokehSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            }
            this.mSceneModeSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mFilterModeSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mMenu.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mMute.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mExitPanorama.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mShutter.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mVideoShutter.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mIndicators.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mPreview.animate().translationYBy((float) this.mSize).setDuration(300);
        } else if (unifiedRotation == 90) {
            this.mFrontBackSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            if (TsMakeupManager.HAS_TS_MAKEUP) {
                this.mTsMakeupSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            } else {
                this.mHdrSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            }
            if (this.mIsBokehMode) {
                this.mBokehSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            }
            this.mSceneModeSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mFilterModeSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mMenu.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mMute.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mExitPanorama.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mSwitcher.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mShutter.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mVideoShutter.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mIndicators.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mPreview.animate().translationXBy((float) this.mSize).setDuration(300);
        } else if (unifiedRotation == 180) {
            this.mFrontBackSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            if (TsMakeupManager.HAS_TS_MAKEUP) {
                this.mTsMakeupSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            } else {
                this.mHdrSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            }
            if (this.mIsBokehMode) {
                this.mBokehSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            }
            this.mSceneModeSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mFilterModeSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mMenu.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mMute.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mExitPanorama.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mShutter.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mVideoShutter.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mIndicators.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mPreview.animate().translationYBy((float) (-this.mSize)).setDuration(300);
        } else if (unifiedRotation == 270) {
            this.mFrontBackSwitcher.animate().translationXBy((float) this.mSize).setDuration(300);
            if (TsMakeupManager.HAS_TS_MAKEUP) {
                this.mTsMakeupSwitcher.animate().translationXBy((float) this.mSize).setDuration(300);
            } else {
                this.mHdrSwitcher.animate().translationXBy((float) this.mSize).setDuration(300);
            }
            if (this.mIsBokehMode) {
                this.mBokehSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            }
            this.mSceneModeSwitcher.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mFilterModeSwitcher.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mMenu.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mMute.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mExitPanorama.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mShutter.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mVideoShutter.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mIndicators.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mPreview.animate().translationXBy((float) (-this.mSize)).setDuration(300);
        }
        this.mRemainingPhotos.setVisibility(4);
        this.mRefocusToast.setVisibility(8);
    }

    public void showUI() {
        if (!isAnimating) {
            enableTouch(false);
        }
        isAnimating = true;
        int unifiedRotation = getUnifiedRotation();
        this.mFrontBackSwitcher.animate().cancel();
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            this.mTsMakeupSwitcher.animate().cancel();
        } else {
            this.mHdrSwitcher.animate().cancel();
        }
        if (this.mIsBokehMode) {
            this.mBokehSwitcher.animate().cancel();
        }
        this.mSceneModeSwitcher.animate().cancel();
        this.mFilterModeSwitcher.animate().cancel();
        this.mSwitcher.animate().cancel();
        this.mShutter.animate().cancel();
        this.mVideoShutter.animate().cancel();
        this.mMenu.animate().cancel();
        this.mMute.animate().cancel();
        this.mExitPanorama.animate().cancel();
        this.mIndicators.animate().cancel();
        this.mPreview.animate().cancel();
        ArrayList<View> arrayList = this.mViewList;
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ((View) it.next()).setVisibility(0);
            }
        }
        ((ModuleSwitcher) this.mSwitcher).removePopup();
        AnimationDrawable animationDrawable = (AnimationDrawable) this.mShutter.getBackground();
        if (animationDrawable != null) {
            animationDrawable.stop();
        }
        this.mMenu.setVisibility(0);
        this.mIndicators.setVisibility(0);
        this.mPreview.setVisibility(0);
        this.mFrontBackSwitcher.animate().setListener(this.inlistener);
        if (unifiedRotation == 0) {
            resetLocation(0.0f, (float) (-this.mSize));
            this.mFrontBackSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            if (TsMakeupManager.HAS_TS_MAKEUP) {
                this.mTsMakeupSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            } else {
                this.mHdrSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            }
            if (this.mIsBokehMode) {
                this.mBokehSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            }
            this.mSceneModeSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mFilterModeSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mMenu.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mMute.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mExitPanorama.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mShutter.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mVideoShutter.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mIndicators.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mPreview.animate().translationYBy((float) (-this.mSize)).setDuration(300);
        } else if (unifiedRotation == 90) {
            resetLocation((float) (-this.mSize), 0.0f);
            this.mFrontBackSwitcher.animate().translationXBy((float) this.mSize).setDuration(300);
            if (TsMakeupManager.HAS_TS_MAKEUP) {
                this.mTsMakeupSwitcher.animate().translationXBy((float) this.mSize).setDuration(300);
            } else {
                this.mHdrSwitcher.animate().translationXBy((float) this.mSize).setDuration(300);
            }
            if (this.mIsBokehMode) {
                this.mBokehSwitcher.animate().translationXBy((float) this.mSize).setDuration(300);
            }
            this.mSceneModeSwitcher.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mFilterModeSwitcher.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mMenu.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mMute.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mExitPanorama.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mShutter.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mVideoShutter.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mIndicators.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mPreview.animate().translationXBy((float) (-this.mSize)).setDuration(300);
        } else if (unifiedRotation == 180) {
            resetLocation(0.0f, (float) this.mSize);
            this.mFrontBackSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            if (TsMakeupManager.HAS_TS_MAKEUP) {
                this.mTsMakeupSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            } else {
                this.mHdrSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            }
            if (this.mIsBokehMode) {
                this.mBokehSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            }
            this.mSceneModeSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mFilterModeSwitcher.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mMenu.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mMute.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mExitPanorama.animate().translationYBy((float) (-this.mSize)).setDuration(300);
            this.mSwitcher.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mShutter.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mVideoShutter.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mIndicators.animate().translationYBy((float) this.mSize).setDuration(300);
            this.mPreview.animate().translationYBy((float) this.mSize).setDuration(300);
        } else if (unifiedRotation == 270) {
            resetLocation((float) this.mSize, 0.0f);
            this.mFrontBackSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            if (TsMakeupManager.HAS_TS_MAKEUP) {
                this.mTsMakeupSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            } else {
                this.mHdrSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            }
            if (this.mIsBokehMode) {
                this.mBokehSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            }
            this.mSceneModeSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mFilterModeSwitcher.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mMenu.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mMute.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mExitPanorama.animate().translationXBy((float) (-this.mSize)).setDuration(300);
            this.mSwitcher.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mShutter.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mVideoShutter.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mIndicators.animate().translationXBy((float) this.mSize).setDuration(300);
            this.mPreview.animate().translationXBy((float) this.mSize).setDuration(300);
        }
        if (this.mRemainingPhotos.getVisibility() == 4 && !this.mHideRemainingPhoto) {
            this.mRemainingPhotos.setVisibility(0);
        }
        this.mRefocusToast.setVisibility(8);
    }

    private void center(View view, Rect rect, int i) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        int i2 = (rect.left + rect.right) / 2;
        int i3 = (rect.top + rect.bottom) / 2;
        int measuredWidth = ((layoutParams.leftMargin + view.getMeasuredWidth()) + layoutParams.rightMargin) / 2;
        int measuredHeight = ((layoutParams.topMargin + view.getMeasuredHeight()) + layoutParams.bottomMargin) / 2;
        view.layout((i2 - measuredWidth) + layoutParams.leftMargin, (i3 - measuredHeight) + layoutParams.topMargin, (i2 + measuredWidth) - layoutParams.rightMargin, (i3 + measuredHeight) - layoutParams.bottomMargin);
    }

    private void toIndex(View view, int i, int i2, int i3, int i4, int i5, int i6) {
        int i7;
        int i8 = i3;
        int i9 = i6;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();
        int i10 = WIDTH_GRID;
        int i11 = 0;
        int i12 = 7;
        if (i8 != 0) {
            if (i8 == 90) {
                i7 = (i10 - i4) - 1;
                i11 = i5;
            } else if (i8 == 180) {
                i11 = (i10 - i4) - 1;
                i7 = (7 - i5) - 1;
            } else if (i8 != 270) {
                i7 = 0;
            } else {
                i11 = (7 - i5) - 1;
                i7 = i4;
            }
            i12 = i10;
            i10 = 7;
        } else {
            i11 = i4;
            i7 = i5;
        }
        int i13 = (((i11 * 2) + 1) * (i / i10)) / 2;
        int i14 = (((i7 * 2) + 1) * (i2 / i12)) / 2;
        if (i5 == 0) {
            int i15 = mTopMargin;
            if (i15 != 0) {
                if (i8 == 90) {
                    i13 = i15 / 2;
                } else if (i8 == 180) {
                    i14 = i2 - (i15 / 2);
                } else if (i8 != 270) {
                    i14 = i15 / 2;
                } else {
                    i13 = i - (i15 / 2);
                }
            }
        }
        int i16 = measuredWidth / 2;
        int i17 = i13 - i16;
        int i18 = i13 + i16;
        int i19 = measuredHeight / 2;
        int i20 = i14 - i19;
        int i21 = i14 + i19;
        if (i9 != -1) {
            int i22 = i8 / 90;
            this.mLocX[i22][i9] = (float) i17;
            this.mLocY[i22][i9] = (float) i20;
        }
        View view2 = view;
        view.layout(i17, i20, i18, i21);
    }

    private void toLeft(View view, Rect rect, int i) {
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        int measuredWidth = layoutParams.leftMargin + view.getMeasuredWidth() + layoutParams.rightMargin;
        int measuredHeight = layoutParams.topMargin + view.getMeasuredHeight() + layoutParams.bottomMargin;
        int i12 = rect.left;
        int i13 = rect.right;
        int i14 = (i12 + i13) / 2;
        int i15 = (rect.top + rect.bottom) / 2;
        int i16 = 0;
        if (i != 0) {
            if (i == 90) {
                int i17 = measuredWidth / 2;
                i16 = (i14 - i17) + layoutParams.leftMargin;
                i8 = (i14 + i17) - layoutParams.rightMargin;
                i11 = rect.bottom + layoutParams.topMargin;
                i9 = rect.bottom + measuredHeight;
                i10 = layoutParams.bottomMargin;
            } else if (i == 180) {
                i7 = i13 + layoutParams.leftMargin;
                i3 = (rect.right + measuredWidth) - layoutParams.rightMargin;
                int i18 = measuredHeight / 2;
                i2 = (i15 - i18) + layoutParams.topMargin;
                i6 = i15 + i18;
                i5 = layoutParams.bottomMargin;
            } else if (i != 270) {
                i4 = 0;
                i3 = 0;
                i2 = 0;
                view.layout(i16, i2, i3, i4);
            } else {
                int i19 = measuredWidth / 2;
                i16 = (i14 - i19) + layoutParams.leftMargin;
                i8 = (i14 + i19) - layoutParams.rightMargin;
                i11 = (rect.top - measuredHeight) + layoutParams.topMargin;
                i9 = rect.top;
                i10 = layoutParams.bottomMargin;
            }
            i4 = i9 - i10;
            i3 = i8;
            i2 = i11;
            view.layout(i16, i2, i3, i4);
        }
        i7 = (i12 - measuredWidth) + layoutParams.leftMargin;
        i3 = rect.left - layoutParams.rightMargin;
        int i20 = measuredHeight / 2;
        i2 = (i15 - i20) + layoutParams.topMargin;
        i6 = i15 + i20;
        i5 = layoutParams.bottomMargin;
        i4 = i6 - i5;
        view.layout(i16, i2, i3, i4);
    }

    private void toRight(View view, Rect rect, int i) {
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        int measuredWidth = layoutParams.leftMargin + view.getMeasuredWidth() + layoutParams.rightMargin;
        int measuredHeight = layoutParams.topMargin + view.getMeasuredHeight() + layoutParams.bottomMargin;
        int i12 = rect.left;
        int i13 = rect.right;
        int i14 = (i12 + i13) / 2;
        int i15 = (rect.top + rect.bottom) / 2;
        int i16 = 0;
        if (i != 0) {
            if (i == 90) {
                int i17 = measuredWidth / 2;
                i16 = (i14 - i17) + layoutParams.leftMargin;
                i8 = (i14 + i17) - layoutParams.rightMargin;
                i11 = (rect.top - measuredHeight) + layoutParams.topMargin;
                i9 = rect.top;
                i10 = layoutParams.bottomMargin;
            } else if (i == 180) {
                i7 = (i12 - measuredWidth) + layoutParams.leftMargin;
                i3 = rect.left - layoutParams.rightMargin;
                int i18 = measuredHeight / 2;
                i2 = (i15 - i18) + layoutParams.topMargin;
                i6 = i15 + i18;
                i5 = layoutParams.bottomMargin;
            } else if (i != 270) {
                i4 = 0;
                i3 = 0;
                i2 = 0;
                view.layout(i16, i2, i3, i4);
            } else {
                int i19 = measuredWidth / 2;
                i16 = (i14 - i19) + layoutParams.leftMargin;
                i8 = (i14 + i19) - layoutParams.rightMargin;
                i11 = rect.bottom + layoutParams.topMargin;
                i9 = rect.bottom + measuredHeight;
                i10 = layoutParams.bottomMargin;
            }
            i4 = i9 - i10;
            i3 = i8;
            i2 = i11;
            view.layout(i16, i2, i3, i4);
        }
        i7 = i13 + layoutParams.leftMargin;
        i3 = (rect.right + measuredWidth) - layoutParams.rightMargin;
        int i20 = measuredHeight / 2;
        i2 = (i15 - i20) + layoutParams.topMargin;
        i6 = i15 + i20;
        i5 = layoutParams.bottomMargin;
        i4 = i6 - i5;
        view.layout(i16, i2, i3, i4);
    }

    private void adjustBackground() {
        int unifiedRotation = getUnifiedRotation();
        this.mBackgroundView.setBackgroundDrawable(null);
        this.mBackgroundView.setRotationX(0.0f);
        this.mBackgroundView.setRotationY(0.0f);
        if (unifiedRotation == 180) {
            this.mBackgroundView.setRotationX(180.0f);
        } else if (unifiedRotation == 270) {
            this.mBackgroundView.setRotationY(180.0f);
        }
        this.mBackgroundView.setBackgroundResource(C0905R.C0906drawable.switcher_bg);
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

    public void setMargins(int i, int i2) {
        mTopMargin = i;
        mBottomMargin = i2;
    }

    public void setPreviewRatio(float f, boolean z) {
        if (z) {
            this.mPaint.setColor(0);
        } else {
            this.mPreviewRatio = CameraUtil.determineRatio(f);
            if (this.mPreviewRatio != 2 || mTopMargin == 0) {
                this.mPaint.setColor(getResources().getColor(C0905R.color.camera_control_bg_transparent));
            } else {
                this.mPaint.setColor(getResources().getColor(C0905R.color.camera_control_bg_opaque));
            }
        }
        invalidate();
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
        this.mOrientation = i;
        View[] viewArr = new View[14];
        viewArr[0] = this.mSceneModeSwitcher;
        viewArr[1] = this.mFilterModeSwitcher;
        viewArr[2] = this.mFrontBackSwitcher;
        viewArr[3] = TsMakeupManager.HAS_TS_MAKEUP ? this.mTsMakeupSwitcher : this.mHdrSwitcher;
        viewArr[4] = this.mBokehSwitcher;
        viewArr[5] = this.mMenu;
        viewArr[6] = this.mShutter;
        viewArr[7] = this.mPreview;
        viewArr[8] = this.mSwitcher;
        viewArr[9] = this.mMute;
        viewArr[10] = this.mReviewRetakeButton;
        viewArr[11] = this.mReviewCancelButton;
        viewArr[12] = this.mReviewDoneButton;
        viewArr[13] = this.mExitPanorama;
        for (View view : viewArr) {
            if (view != null) {
                ((RotateImageView) view).setOrientation(i, z);
            }
        }
        layoutRemaingPhotos();
    }

    public void hideCameraSettings() {
        this.mFrontBackSwitcher.setVisibility(4);
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            this.mTsMakeupSwitcher.setVisibility(4);
        } else {
            this.mHdrSwitcher.setVisibility(4);
        }
        if (this.mIsBokehMode) {
            this.mBokehSwitcher.setVisibility(4);
        }
        this.mSceneModeSwitcher.setVisibility(4);
        this.mFilterModeSwitcher.setVisibility(4);
        this.mMenu.setVisibility(4);
    }

    public void showCameraSettings() {
        this.mFrontBackSwitcher.setVisibility(0);
        if (TsMakeupManager.HAS_TS_MAKEUP) {
            this.mTsMakeupSwitcher.setVisibility(0);
        } else {
            this.mHdrSwitcher.setVisibility(0);
        }
        if (this.mIsBokehMode) {
            this.mBokehSwitcher.setVisibility(0);
        }
        this.mSceneModeSwitcher.setVisibility(0);
        this.mFilterModeSwitcher.setVisibility(0);
        this.mMenu.setVisibility(0);
    }

    public void hideRemainingPhotoCnt() {
        this.mHideRemainingPhoto = true;
        this.mRemainingPhotos.setVisibility(8);
        this.mRemainingPhotosText.setVisibility(8);
    }

    public void showRemainingPhotoCnt() {
        this.mHideRemainingPhoto = false;
        this.mRemainingPhotos.setVisibility(0);
        this.mRemainingPhotosText.setVisibility(0);
    }
}
