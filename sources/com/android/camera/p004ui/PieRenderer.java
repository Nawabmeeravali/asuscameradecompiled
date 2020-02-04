package com.android.camera.p004ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.p000v4.internal.view.SupportMenu;
import android.util.FloatMath;
import android.util.Size;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.camera.CameraActivity;
import com.android.camera.drawable.TextDrawable;
import com.android.camera.p004ui.ProgressRenderer.VisibilityListener;
import com.asus.scenedetectlib.BuildConfig;
import java.util.ArrayList;
import java.util.List;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.PieRenderer */
public class PieRenderer extends OverlayRenderer implements FocusIndicator {
    protected static float CENTER = 1.5707964f;
    private static final int DIAL_HORIZONTAL = 157;
    private static final int DISAPPEAR_TIMEOUT = 200;
    private static final int FPS = 30;
    private static final float MATH_PI_2 = 1.5707964f;
    private static final int MSG_CLOSE = 1;
    private static final int MSG_MOVED = 3;
    private static final int MSG_OPEN = 0;
    private static final int MSG_OPENSUBMENU = 2;
    private static final long PIE_FADE_IN_DURATION = 200;
    private static final int PIE_FADE_OUT_DURATION = 600;
    private static final long PIE_OPEN_SUB_DELAY = 400;
    private static final long PIE_SELECT_FADE_DURATION = 300;
    private static final long PIE_SLICE_DURATION = 80;
    private static final long PIE_XFADE_DURATION = 200;
    protected static float RAD24 = 0.41887903f;
    private static final int SCALING_DOWN_TIME = 100;
    private static final int SCALING_UP_TIME = 600;
    private static final int STATE_FINISHING = 2;
    private static final int STATE_FOCUSING = 1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PIE = 8;
    private static final String TAG = "PieRenderer";
    private int mAngleZone;
    private ValueAnimator mAnimator;
    private int mArcCenterY;
    private int mArcOffset;
    private int mArcRadius;
    private boolean mBlockFocus;
    private Bitmap mBokehFocusCircle;
    private int mBokehFocusIndex = 0;
    private TypedArray mBokehFocusResId;
    private float mCenterAngle;
    /* access modifiers changed from: private */
    public int mCenterX;
    /* access modifiers changed from: private */
    public int mCenterY;
    private RectF mCircle;
    private int mCircleSize;
    private PieItem mCurrentItem;
    private int mDeadZone;
    private RectF mDial;
    /* access modifiers changed from: private */
    public int mDialAngle;
    /* access modifiers changed from: private */
    public Runnable mDisappear = new Disappear();
    private Point mDown;
    /* access modifiers changed from: private */
    public ValueAnimator mFadeIn;
    /* access modifiers changed from: private */
    public ValueAnimator mFadeOut;
    private int mFailColor;
    /* access modifiers changed from: private */
    public volatile boolean mFocusCancelled;
    private Paint mFocusPaint;
    /* access modifiers changed from: private */
    public int mFocusX;
    /* access modifiers changed from: private */
    public int mFocusY;
    /* access modifiers changed from: private */
    public boolean mFocused;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            int i = message.what;
            if (i != 0) {
                if (i != 1) {
                    if (i == 2) {
                        PieRenderer.this.onEnterOpen();
                    } else if (i == 3 && PieRenderer.this.mListener != null) {
                        PieRenderer.this.mListener.onPieMoved(PieRenderer.this.mFocusX, PieRenderer.this.mFocusY);
                    }
                } else if (PieRenderer.this.mListener != null) {
                    PieRenderer.this.mListener.onPieClosed();
                }
            } else if (PieRenderer.this.mListener != null) {
                PieRenderer.this.mListener.onPieOpened(PieRenderer.this.mPieCenterX, PieRenderer.this.mPieCenterY);
            }
        }
    };
    private int mInnerOffset;
    private int mInnerStroke;
    /* access modifiers changed from: private */
    public boolean mIsBokehMode = false;
    private TextDrawable mLabel;
    /* access modifiers changed from: private */
    public PieListener mListener;
    private int mMaxArcRadius;
    private Paint mMenuArcPaint;
    private List<PieItem> mOpen;
    /* access modifiers changed from: private */
    public boolean mOpening;
    private int mOuterStroke;
    /* access modifiers changed from: private */
    public int mPieCenterX;
    /* access modifiers changed from: private */
    public int mPieCenterY;
    private Point mPoint1;
    private Point mPoint2;
    private PointF mPolar = new PointF();
    private ProgressRenderer mProgressRenderer;
    private int mRadius;
    private int mRadiusInc;
    private Paint mSelectedPaint;
    /* access modifiers changed from: private */
    public ValueAnimator mSlice;
    private int mSliceCenterY;
    private int mSliceRadius;
    /* access modifiers changed from: private */
    public int mStartAnimationAngle;
    /* access modifiers changed from: private */
    public volatile int mState;
    private Paint mSubPaint;
    private int mSuccessColor;
    private boolean mTapMode;
    private int mTouchOffset;
    private int mTouchSlopSquared;
    /* access modifiers changed from: private */
    public ValueAnimator mXFade;

    /* renamed from: com.android.camera.ui.PieRenderer$Disappear */
    private class Disappear implements Runnable {
        private Disappear() {
        }

        public void run() {
            if (PieRenderer.this.mState != 8) {
                PieRenderer.this.setVisible(false);
                PieRenderer pieRenderer = PieRenderer.this;
                pieRenderer.mFocusX = pieRenderer.mCenterX;
                PieRenderer pieRenderer2 = PieRenderer.this;
                pieRenderer2.mFocusY = pieRenderer2.mCenterY;
                PieRenderer.this.mState = 0;
                PieRenderer pieRenderer3 = PieRenderer.this;
                pieRenderer3.setCircle(pieRenderer3.mFocusX, PieRenderer.this.mFocusY);
                if (PieRenderer.this.mIsBokehMode) {
                    PieRenderer.this.mHandler.sendEmptyMessage(1);
                }
                PieRenderer.this.mFocused = false;
            }
        }
    }

    /* renamed from: com.android.camera.ui.PieRenderer$PieListener */
    public interface PieListener {
        void onPieClosed();

        void onPieMoved(int i, int i2);

        void onPieOpened(int i, int i2);
    }

    private float getDegrees(double d) {
        return (float) (360.0d - ((d * 180.0d) / 3.141592653589793d));
    }

    public boolean handlesTouch() {
        return true;
    }

    public void setPieListener(PieListener pieListener) {
        this.mListener = pieListener;
    }

    public PieRenderer(Context context) {
        init(context);
    }

    private void init(Context context) {
        setVisible(false);
        this.mOpen = new ArrayList();
        this.mOpen.add(new PieItem(null, 0));
        Resources resources = context.getResources();
        this.mRadius = resources.getDimensionPixelSize(C0905R.dimen.pie_radius_start);
        this.mRadiusInc = resources.getDimensionPixelSize(C0905R.dimen.pie_radius_increment);
        this.mCircleSize = this.mRadius - resources.getDimensionPixelSize(C0905R.dimen.focus_radius_offset);
        this.mTouchOffset = resources.getDimensionPixelSize(C0905R.dimen.pie_touch_offset);
        this.mSelectedPaint = new Paint();
        this.mSelectedPaint.setColor(Color.argb(255, 51, 181, 229));
        this.mSelectedPaint.setAntiAlias(true);
        this.mSubPaint = new Paint();
        this.mSubPaint.setAntiAlias(true);
        this.mSubPaint.setColor(Color.argb(200, 250, 230, 128));
        this.mFocusPaint = new Paint();
        this.mFocusPaint.setAntiAlias(true);
        this.mFocusPaint.setColor(-1);
        this.mFocusPaint.setStyle(Style.STROKE);
        this.mSuccessColor = -16711936;
        this.mFailColor = SupportMenu.CATEGORY_MASK;
        this.mCircle = new RectF();
        this.mDial = new RectF();
        this.mPoint1 = new Point();
        this.mPoint2 = new Point();
        this.mInnerOffset = resources.getDimensionPixelSize(C0905R.dimen.focus_inner_offset);
        this.mOuterStroke = resources.getDimensionPixelSize(C0905R.dimen.focus_outer_stroke);
        this.mInnerStroke = resources.getDimensionPixelSize(C0905R.dimen.focus_inner_stroke);
        this.mState = 0;
        this.mBlockFocus = false;
        this.mTouchSlopSquared = ViewConfiguration.get(context).getScaledTouchSlop();
        int i = this.mTouchSlopSquared;
        this.mTouchSlopSquared = i * i;
        this.mDown = new Point();
        this.mMenuArcPaint = new Paint();
        this.mMenuArcPaint.setAntiAlias(true);
        this.mMenuArcPaint.setColor(Color.argb(Const.CODE_C1_DLW, 255, 255, 255));
        this.mMenuArcPaint.setStrokeWidth(10.0f);
        this.mMenuArcPaint.setStyle(Style.STROKE);
        this.mSliceRadius = resources.getDimensionPixelSize(C0905R.dimen.pie_item_radius);
        this.mArcRadius = resources.getDimensionPixelSize(C0905R.dimen.pie_arc_radius);
        this.mMaxArcRadius = this.mArcRadius;
        this.mArcOffset = resources.getDimensionPixelSize(C0905R.dimen.pie_arc_offset);
        this.mLabel = new TextDrawable(resources);
        this.mLabel.setDropShadow(true);
        this.mDeadZone = resources.getDimensionPixelSize(C0905R.dimen.pie_deadzone_width);
        this.mAngleZone = resources.getDimensionPixelSize(C0905R.dimen.pie_anglezone_width);
        this.mProgressRenderer = new ProgressRenderer(context);
    }

    private PieItem getRoot() {
        return (PieItem) this.mOpen.get(0);
    }

    public boolean showsItems() {
        return this.mTapMode;
    }

    public void addItem(PieItem pieItem) {
        getRoot().addItem(pieItem);
    }

    public void setBokehMode(boolean z) {
        this.mIsBokehMode = z;
        if (this.mIsBokehMode) {
            this.mBokehFocusResId = getContext().getResources().obtainTypedArray(C0905R.array.bokeh_shutter_icons);
            this.mBokehFocusCircle = BitmapFactory.decodeResource(getContext().getResources(), this.mBokehFocusResId.getResourceId(this.mBokehFocusIndex, 0));
        } else {
            Bitmap bitmap = this.mBokehFocusCircle;
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        this.mOverlay.update();
    }

    public void setBokehDegree(int i) {
        if (i >= 0 && i <= 100) {
            int i2 = i / 15;
            if (this.mBokehFocusIndex != i2) {
                this.mBokehFocusIndex = i2;
                int resourceId = this.mBokehFocusResId.getResourceId(this.mBokehFocusIndex, 0);
                Bitmap bitmap = this.mBokehFocusCircle;
                if (bitmap != null) {
                    bitmap.recycle();
                }
                this.mBokehFocusCircle = BitmapFactory.decodeResource(getContext().getResources(), resourceId);
                this.mOverlay.update();
            }
        }
    }

    public Size getBokehFocusSize() {
        if (this.mIsBokehMode) {
            Bitmap bitmap = this.mBokehFocusCircle;
            if (bitmap != null) {
                return new Size(bitmap.getWidth(), this.mBokehFocusCircle.getHeight());
            }
        }
        return new Size(0, 0);
    }

    private void drawBokehFocus(Canvas canvas) {
        Bitmap bitmap = this.mBokehFocusCircle;
        if (bitmap != null) {
            canvas.drawBitmap(this.mBokehFocusCircle, (float) (this.mFocusX - (bitmap.getWidth() / 2)), (float) (this.mFocusY - (this.mBokehFocusCircle.getHeight() / 2)), new Paint());
        }
        this.mHandler.sendEmptyMessage(3);
    }

    public void clearItems() {
        getRoot().clearItems();
    }

    public void showInCenter() {
        if (this.mState != 8 || !isVisible()) {
            if (this.mState != 0) {
                cancelFocus();
            }
            this.mState = 8;
            resetPieCenter();
            setCenter(this.mPieCenterX, this.mPieCenterY);
            this.mTapMode = true;
            show(true);
            return;
        }
        this.mTapMode = false;
        show(false);
    }

    public void hide() {
        show(false);
    }

    /* access modifiers changed from: private */
    public void show(boolean z) {
        String str = BuildConfig.FLAVOR;
        if (z) {
            ValueAnimator valueAnimator = this.mXFade;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            this.mState = 8;
            this.mCurrentItem = null;
            PieItem root = getRoot();
            for (PieItem pieItem : this.mOpen) {
                if (pieItem.hasItems()) {
                    for (PieItem selected : pieItem.getItems()) {
                        selected.setSelected(false);
                    }
                }
            }
            this.mLabel.setText(str);
            this.mOpen.clear();
            this.mOpen.add(root);
            layoutPie();
            fadeIn();
        } else {
            this.mState = 0;
            this.mTapMode = false;
            ValueAnimator valueAnimator2 = this.mXFade;
            if (valueAnimator2 != null) {
                valueAnimator2.cancel();
            }
            TextDrawable textDrawable = this.mLabel;
            if (textDrawable != null) {
                textDrawable.setText(str);
            }
        }
        setVisible(z);
        this.mHandler.sendEmptyMessage(z ^ true ? 1 : 0);
    }

    public boolean isOpen() {
        return this.mState == 8 && isVisible();
    }

    public void setProgress(int i) {
        this.mProgressRenderer.setProgress(i);
    }

    private void fadeIn() {
        this.mFadeIn = new ValueAnimator();
        this.mFadeIn.setFloatValues(new float[]{0.0f, 1.0f});
        this.mFadeIn.setDuration(200);
        this.mFadeIn.setInterpolator(null);
        this.mFadeIn.addListener(new AnimatorListener() {
            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                PieRenderer.this.mFadeIn = null;
            }
        });
        this.mFadeIn.start();
    }

    public void setCenter(int i, int i2) {
        this.mPieCenterX = i;
        this.mPieCenterY = i2;
        int i3 = this.mSliceRadius + i2;
        int i4 = this.mArcOffset;
        this.mSliceCenterY = i3 - i4;
        this.mArcCenterY = (i2 - i4) + this.mArcRadius;
    }

    public void layout(int i, int i2, int i3, int i4) {
        super.layout(i, i2, i3, i4);
        int i5 = i3 - i;
        this.mCenterX = i5 / 2;
        this.mCenterY = (i4 - i2) / 2;
        if (i5 > 0) {
            int i6 = this.mMaxArcRadius;
            int i7 = this.mCenterX;
            if (i6 + i7 > i5) {
                this.mArcRadius = i5 - i7;
                this.mFocusX = this.mCenterX;
                this.mFocusY = this.mCenterY;
                resetPieCenter();
                setCircle(this.mFocusX, this.mFocusY);
                if (isVisible() && this.mState == 8) {
                    setCenter(this.mPieCenterX, this.mPieCenterY);
                    layoutPie();
                    return;
                }
            }
        }
        this.mArcRadius = this.mMaxArcRadius;
        this.mFocusX = this.mCenterX;
        this.mFocusY = this.mCenterY;
        resetPieCenter();
        setCircle(this.mFocusX, this.mFocusY);
        if (isVisible()) {
        }
    }

    private void resetPieCenter() {
        this.mPieCenterX = this.mCenterX;
        this.mPieCenterY = (int) (((float) getHeight()) - (((float) this.mDeadZone) * 2.5f));
    }

    private void layoutPie() {
        this.mCenterAngle = getCenterAngle();
        layoutItems(0, getRoot().getItems());
        layoutLabel(getLevel());
    }

    private void layoutLabel(int i) {
        int i2 = this.mPieCenterX;
        float sin = FloatMath.sin(this.mCenterAngle - CENTER);
        int i3 = this.mArcRadius;
        int i4 = i + 2;
        int i5 = this.mRadiusInc;
        int i6 = i2 - ((int) (sin * ((float) ((i4 * i5) + i3))));
        int i7 = (this.mArcCenterY - i3) - (i4 * i5);
        int intrinsicWidth = this.mLabel.getIntrinsicWidth();
        int i8 = intrinsicWidth / 2;
        int intrinsicHeight = this.mLabel.getIntrinsicHeight() / 2;
        this.mLabel.setBounds(i6 - i8, i7 - intrinsicHeight, i6 + i8, i7 + intrinsicHeight);
    }

    private void layoutItems(int i, List<PieItem> list) {
        float f = (float) 1;
        float degrees = getDegrees(0.0d) + f;
        float degrees2 = getDegrees(0.23000000417232513d) - f;
        int i2 = this.mArcRadius;
        int i3 = this.mRadiusInc;
        Path makeSlice = makeSlice(degrees, degrees2, i2, i2 + i3 + (i3 / 4), this.mPieCenterX, this.mArcCenterY - (i3 * i));
        int size = list.size();
        int i4 = 0;
        for (PieItem pieItem : list) {
            pieItem.setPath(makeSlice);
            float arcCenter = getArcCenter(pieItem, i4, size);
            int intrinsicWidth = pieItem.getIntrinsicWidth();
            int intrinsicHeight = pieItem.getIntrinsicHeight();
            double d = (double) (this.mArcRadius + ((this.mRadiusInc * 2) / 3));
            double d2 = (double) arcCenter;
            int sin = ((this.mArcCenterY - (this.mRadiusInc * i)) - ((int) (d * Math.sin(d2)))) - (intrinsicHeight / 2);
            int cos = (this.mPieCenterX + ((int) (Math.cos(d2) * d))) - (intrinsicWidth / 2);
            pieItem.setBounds(cos, sin, intrinsicWidth + cos, intrinsicHeight + sin);
            pieItem.setLevel(i);
            if (pieItem.hasItems()) {
                layoutItems(i + 1, pieItem.getItems());
            }
            i4++;
        }
    }

    private Path makeSlice(float f, float f2, int i, int i2, int i3, int i4) {
        RectF rectF = new RectF((float) (i3 - i2), (float) (i4 - i2), (float) (i3 + i2), (float) (i2 + i4));
        RectF rectF2 = new RectF((float) (i3 - i), (float) (i4 - i), (float) (i3 + i), (float) (i4 + i));
        Path path = new Path();
        path.arcTo(rectF, f, f2 - f, true);
        path.arcTo(rectF2, f2, f - f2);
        path.close();
        return path;
    }

    private float getArcCenter(PieItem pieItem, int i, int i2) {
        return getCenter(i, i2, 0.23f);
    }

    private float getSliceCenter(PieItem pieItem, int i, int i2) {
        float centerAngle = getCenterAngle();
        float f = CENTER;
        return ((((centerAngle - f) * 0.5f) + f) + ((((float) (i2 - 1)) * 0.14f) / 2.0f)) - (((float) i) * 0.14f);
    }

    private float getCenter(int i, int i2, float f) {
        return (this.mCenterAngle + ((((float) (i2 - 1)) * f) / 2.0f)) - (((float) i) * f);
    }

    private float getCenterAngle() {
        float f = CENTER;
        int i = this.mPieCenterX;
        int i2 = this.mDeadZone;
        int i3 = this.mAngleZone;
        if (i < i2 + i3) {
            return f - ((((float) ((i3 - i) + i2)) * RAD24) / ((float) i3));
        }
        if (i <= (getWidth() - this.mDeadZone) - this.mAngleZone) {
            return f;
        }
        float f2 = CENTER;
        int i4 = this.mPieCenterX;
        int width = getWidth() - this.mDeadZone;
        int i5 = this.mAngleZone;
        return f2 + ((((float) (i4 - (width - i5))) * RAD24) / ((float) i5));
    }

    private void startFadeOut(final PieItem pieItem) {
        ValueAnimator valueAnimator = this.mFadeIn;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator valueAnimator2 = this.mXFade;
        if (valueAnimator2 != null) {
            valueAnimator2.cancel();
        }
        this.mFadeOut = new ValueAnimator();
        this.mFadeOut.setFloatValues(new float[]{1.0f, 0.0f});
        this.mFadeOut.setDuration(600);
        this.mFadeOut.addListener(new AnimatorListener() {
            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                pieItem.performClick();
                PieRenderer.this.mFadeOut = null;
                PieRenderer.this.deselect();
                PieRenderer.this.show(false);
                PieRenderer.this.mOverlay.setAlpha(1.0f);
            }
        });
        this.mFadeOut.start();
    }

    private boolean hasOpenItem() {
        return this.mOpen.size() > 1;
    }

    private PieItem closeOpenItem() {
        PieItem openItem = getOpenItem();
        List<PieItem> list = this.mOpen;
        list.remove(list.size() - 1);
        return openItem;
    }

    private PieItem getOpenItem() {
        List<PieItem> list = this.mOpen;
        return (PieItem) list.get(list.size() - 1);
    }

    private PieItem getParent() {
        List<PieItem> list = this.mOpen;
        return (PieItem) list.get(Math.max(0, list.size() - 2));
    }

    private int getLevel() {
        return this.mOpen.size() - 1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x006d  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0071  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDraw(android.graphics.Canvas r18) {
        /*
            r17 = this;
            r7 = r17
            r8 = r18
            com.android.camera.ui.ProgressRenderer r0 = r7.mProgressRenderer
            int r1 = r7.mFocusX
            int r2 = r7.mFocusY
            r0.onDraw(r8, r1, r2)
            android.animation.ValueAnimator r0 = r7.mXFade
            r9 = 1065353216(0x3f800000, float:1.0)
            if (r0 == 0) goto L_0x001f
            java.lang.Object r0 = r0.getAnimatedValue()
            java.lang.Float r0 = (java.lang.Float) r0
            float r0 = r0.floatValue()
        L_0x001d:
            r10 = r0
            goto L_0x003e
        L_0x001f:
            android.animation.ValueAnimator r0 = r7.mFadeIn
            if (r0 == 0) goto L_0x002e
            java.lang.Object r0 = r0.getAnimatedValue()
            java.lang.Float r0 = (java.lang.Float) r0
            float r0 = r0.floatValue()
            goto L_0x001d
        L_0x002e:
            android.animation.ValueAnimator r0 = r7.mFadeOut
            if (r0 == 0) goto L_0x003d
            java.lang.Object r0 = r0.getAnimatedValue()
            java.lang.Float r0 = (java.lang.Float) r0
            float r0 = r0.floatValue()
            goto L_0x001d
        L_0x003d:
            r10 = r9
        L_0x003e:
            int r11 = r18.save()
            android.animation.ValueAnimator r0 = r7.mFadeIn
            if (r0 == 0) goto L_0x0057
            r0 = 1063675494(0x3f666666, float:0.9)
            r1 = 1036831949(0x3dcccccd, float:0.1)
            float r1 = r1 * r10
            float r1 = r1 + r0
            int r0 = r7.mPieCenterX
            float r0 = (float) r0
            int r2 = r7.mPieCenterY
            float r2 = (float) r2
            r8.scale(r1, r1, r0, r2)
        L_0x0057:
            int r0 = r7.mState
            r1 = 8
            if (r0 == r1) goto L_0x0068
            boolean r0 = r7.mIsBokehMode
            if (r0 == 0) goto L_0x0065
            r17.drawBokehFocus(r18)
            goto L_0x0068
        L_0x0065:
            r17.drawFocus(r18)
        L_0x0068:
            int r0 = r7.mState
            r12 = 2
            if (r0 != r12) goto L_0x0071
            r8.restoreToCount(r11)
            return
        L_0x0071:
            int r0 = r7.mState
            if (r0 == r1) goto L_0x0076
            return
        L_0x0076:
            boolean r0 = r17.hasOpenItem()
            r13 = 0
            if (r0 == 0) goto L_0x0081
            android.animation.ValueAnimator r0 = r7.mXFade
            if (r0 == 0) goto L_0x00d1
        L_0x0081:
            int r0 = r17.getLevel()
            com.android.camera.ui.PieItem r1 = r17.getParent()
            r7.drawArc(r8, r0, r1)
            com.android.camera.ui.PieItem r0 = r17.getParent()
            java.util.List r0 = r0.getItems()
            int r14 = r0.size()
            com.android.camera.ui.PieItem r0 = r17.getParent()
            java.util.List r0 = r0.getItems()
            java.util.Iterator r15 = r0.iterator()
            r16 = r13
        L_0x00a6:
            boolean r0 = r15.hasNext()
            if (r0 == 0) goto L_0x00cc
            java.lang.Object r0 = r15.next()
            r5 = r0
            com.android.camera.ui.PieItem r5 = (com.android.camera.p004ui.PieItem) r5
            java.util.List<com.android.camera.ui.PieItem> r0 = r7.mOpen
            int r0 = r0.size()
            int r0 = r0 - r12
            int r1 = java.lang.Math.max(r13, r0)
            r0 = r17
            r2 = r16
            r3 = r14
            r4 = r18
            r6 = r10
            r0.drawItem(r1, r2, r3, r4, r5, r6)
            int r16 = r16 + 1
            goto L_0x00a6
        L_0x00cc:
            com.android.camera.drawable.TextDrawable r0 = r7.mLabel
            r0.draw(r8)
        L_0x00d1:
            boolean r0 = r17.hasOpenItem()
            if (r0 == 0) goto L_0x012d
            int r12 = r17.getLevel()
            com.android.camera.ui.PieItem r0 = r17.getOpenItem()
            r7.drawArc(r8, r12, r0)
            com.android.camera.ui.PieItem r0 = r17.getOpenItem()
            java.util.List r0 = r0.getItems()
            int r14 = r0.size()
            java.util.Iterator r15 = r0.iterator()
        L_0x00f2:
            boolean r0 = r15.hasNext()
            if (r0 == 0) goto L_0x0128
            java.lang.Object r0 = r15.next()
            r5 = r0
            com.android.camera.ui.PieItem r5 = (com.android.camera.p004ui.PieItem) r5
            android.animation.ValueAnimator r0 = r7.mFadeOut
            if (r0 == 0) goto L_0x010f
            r0 = r17
            r1 = r12
            r2 = r13
            r3 = r14
            r4 = r18
            r6 = r10
            r0.drawItem(r1, r2, r3, r4, r5, r6)
            goto L_0x0125
        L_0x010f:
            android.animation.ValueAnimator r0 = r7.mXFade
            if (r0 == 0) goto L_0x011a
            r0 = 1056964608(0x3f000000, float:0.5)
            float r0 = r0 * r10
            float r0 = r9 - r0
            r6 = r0
            goto L_0x011b
        L_0x011a:
            r6 = r9
        L_0x011b:
            r0 = r17
            r1 = r12
            r2 = r13
            r3 = r14
            r4 = r18
            r0.drawItem(r1, r2, r3, r4, r5, r6)
        L_0x0125:
            int r13 = r13 + 1
            goto L_0x00f2
        L_0x0128:
            com.android.camera.drawable.TextDrawable r0 = r7.mLabel
            r0.draw(r8)
        L_0x012d:
            r8.restoreToCount(r11)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.p004ui.PieRenderer.onDraw(android.graphics.Canvas):void");
    }

    private void drawArc(Canvas canvas, int i, PieItem pieItem) {
        if (this.mState == 8) {
            int size = pieItem.getItems().size();
            float f = this.mCenterAngle;
            float f2 = (((float) size) * 0.23f) / 2.0f;
            float f3 = f + f2;
            float f4 = f - f2;
            int i2 = this.mArcCenterY - (i * this.mRadiusInc);
            int i3 = this.mPieCenterX;
            int i4 = this.mArcRadius;
            RectF rectF = new RectF((float) (i3 - i4), (float) (i2 - i4), (float) (i3 + i4), (float) (i2 + i4));
            double d = (double) f4;
            canvas.drawArc(rectF, getDegrees(d), getDegrees((double) f3) - getDegrees(d), false, this.mMenuArcPaint);
        }
    }

    private void drawItem(int i, int i2, int i3, Canvas canvas, PieItem pieItem, float f) {
        float f2;
        if (this.mState == 8 && pieItem.getPath() != null) {
            int i4 = this.mArcCenterY - (i * this.mRadiusInc);
            if (pieItem.isSelected()) {
                Paint paint = this.mSelectedPaint;
                int save = canvas.save();
                ValueAnimator valueAnimator = this.mSlice;
                if (valueAnimator != null) {
                    f2 = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                } else {
                    f2 = getArcCenter(pieItem, i2, i3) - 0.115f;
                }
                canvas.rotate(getDegrees((double) f2), (float) this.mPieCenterX, (float) i4);
                if (this.mFadeOut != null) {
                    paint.setAlpha((int) (255.0f * f));
                }
                canvas.drawPath(pieItem.getPath(), paint);
                if (this.mFadeOut != null) {
                    paint.setAlpha(255);
                }
                canvas.restoreToCount(save);
            }
            if (this.mFadeOut == null) {
                pieItem.setAlpha(f * (pieItem.isEnabled() ? 1.0f : 0.3f));
            }
            pieItem.draw(canvas);
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!CameraActivity.isPieMenuEnabled()) {
            return false;
        }
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        int actionMasked = motionEvent.getActionMasked();
        getPolar(x, y, !this.mTapMode, this.mPolar);
        if (actionMasked != 0) {
            if (1 == actionMasked) {
                if (isVisible()) {
                    PieItem pieItem = this.mCurrentItem;
                    if (this.mTapMode) {
                        pieItem = findItem(this.mPolar);
                        if (this.mOpening) {
                            this.mOpening = false;
                            return true;
                        }
                    }
                    if (pieItem == null) {
                        this.mTapMode = false;
                        show(false);
                    } else if (this.mOpening || pieItem.hasItems()) {
                        this.mTapMode = true;
                    } else {
                        startFadeOut(pieItem);
                        this.mTapMode = false;
                    }
                    return true;
                }
            } else if (3 == actionMasked) {
                if (isVisible() || this.mTapMode) {
                    show(false);
                }
                deselect();
                this.mHandler.removeMessages(2);
                return false;
            } else if (2 == actionMasked) {
                if (pulledToCenter(this.mPolar)) {
                    this.mHandler.removeMessages(2);
                    if (hasOpenItem()) {
                        PieItem pieItem2 = this.mCurrentItem;
                        if (pieItem2 != null) {
                            pieItem2.setSelected(false);
                        }
                        closeOpenItem();
                        this.mCurrentItem = null;
                    } else {
                        deselect();
                    }
                    this.mLabel.setText(BuildConfig.FLAVOR);
                    return false;
                }
                PieItem findItem = findItem(this.mPolar);
                boolean hasMoved = hasMoved(motionEvent);
                if (!(findItem == null || this.mCurrentItem == findItem || (this.mOpening && !hasMoved))) {
                    this.mHandler.removeMessages(2);
                    if (hasMoved) {
                        this.mTapMode = false;
                    }
                    onEnterSelect(findItem);
                    this.mHandler.sendEmptyMessageDelayed(2, PIE_OPEN_SUB_DELAY);
                }
            }
            return false;
        } else if (x < ((float) this.mDeadZone) || x > ((float) (getWidth() - this.mDeadZone))) {
            return false;
        } else {
            this.mDown.x = (int) motionEvent.getX();
            this.mDown.y = (int) motionEvent.getY();
            this.mOpening = false;
            if (this.mTapMode) {
                PieItem findItem2 = findItem(this.mPolar);
                if (!(findItem2 == null || this.mCurrentItem == findItem2)) {
                    this.mState = 8;
                    onEnter(findItem2);
                }
            } else {
                setCenter((int) x, (int) y);
                show(true);
            }
            return true;
        }
    }

    public boolean isVisible() {
        return super.isVisible() || this.mProgressRenderer.isVisible();
    }

    private boolean pulledToCenter(PointF pointF) {
        return pointF.y < ((float) (this.mArcRadius - this.mRadiusInc));
    }

    private boolean inside(PointF pointF, PieItem pieItem, int i, int i2) {
        float sliceCenter = getSliceCenter(pieItem, i, i2) - 0.07f;
        int i3 = this.mArcRadius;
        float f = (float) i3;
        float f2 = pointF.y;
        if (f < f2) {
            float f3 = pointF.x;
            if (sliceCenter < f3 && sliceCenter + 0.14f > f3 && (!this.mTapMode || ((float) (i3 + this.mRadiusInc)) > f2)) {
                return true;
            }
        }
        return false;
    }

    private void getPolar(float f, float f2, boolean z, PointF pointF) {
        pointF.x = MATH_PI_2;
        float f3 = f - ((float) this.mPieCenterX);
        float level = ((float) (this.mSliceCenterY - (getLevel() * this.mRadiusInc))) - f2;
        float level2 = ((float) (this.mArcCenterY - (getLevel() * this.mRadiusInc))) - f2;
        pointF.y = (float) Math.sqrt((double) ((f3 * f3) + (level2 * level2)));
        if (f3 != 0.0f) {
            pointF.x = (float) Math.atan2((double) level, (double) f3);
            float f4 = pointF.x;
            if (f4 < 0.0f) {
                pointF.x = (float) (((double) f4) + 6.283185307179586d);
            }
        }
        pointF.y += (float) (z ? this.mTouchOffset : 0);
    }

    private boolean hasMoved(MotionEvent motionEvent) {
        return ((float) this.mTouchSlopSquared) < ((motionEvent.getX() - ((float) this.mDown.x)) * (motionEvent.getX() - ((float) this.mDown.x))) + ((motionEvent.getY() - ((float) this.mDown.y)) * (motionEvent.getY() - ((float) this.mDown.y)));
    }

    private void onEnterSelect(PieItem pieItem) {
        PieItem pieItem2 = this.mCurrentItem;
        if (pieItem2 != null) {
            pieItem2.setSelected(false);
        }
        if (pieItem == null || !pieItem.isEnabled()) {
            this.mCurrentItem = null;
            return;
        }
        moveSelection(this.mCurrentItem, pieItem);
        pieItem.setSelected(true);
        this.mCurrentItem = pieItem;
        this.mLabel.setText(this.mCurrentItem.getLabel());
        layoutLabel(getLevel());
    }

    /* access modifiers changed from: private */
    public void onEnterOpen() {
        PieItem pieItem = this.mCurrentItem;
        if (pieItem != null && pieItem != getOpenItem() && this.mCurrentItem.hasItems()) {
            openCurrentItem();
        }
    }

    private void onEnter(PieItem pieItem) {
        PieItem pieItem2 = this.mCurrentItem;
        if (pieItem2 != null) {
            pieItem2.setSelected(false);
        }
        if (pieItem == null || !pieItem.isEnabled()) {
            this.mCurrentItem = null;
            return;
        }
        pieItem.setSelected(true);
        this.mCurrentItem = pieItem;
        this.mLabel.setText(this.mCurrentItem.getLabel());
        if (this.mCurrentItem != getOpenItem() && this.mCurrentItem.hasItems()) {
            openCurrentItem();
            layoutLabel(getLevel());
        }
    }

    /* access modifiers changed from: private */
    public void deselect() {
        PieItem pieItem = this.mCurrentItem;
        if (pieItem != null) {
            pieItem.setSelected(false);
        }
        if (hasOpenItem()) {
            onEnter(closeOpenItem());
        } else {
            this.mCurrentItem = null;
        }
    }

    private int getItemPos(PieItem pieItem) {
        return getOpenItem().getItems().indexOf(pieItem);
    }

    private int getCurrentCount() {
        return getOpenItem().getItems().size();
    }

    private void moveSelection(PieItem pieItem, PieItem pieItem2) {
        int currentCount = getCurrentCount();
        int itemPos = getItemPos(pieItem);
        int itemPos2 = getItemPos(pieItem2);
        if (itemPos != -1 && itemPos2 != -1) {
            float arcCenter = getArcCenter(pieItem, getItemPos(pieItem), currentCount) - 0.115f;
            float arcCenter2 = getArcCenter(pieItem2, getItemPos(pieItem2), currentCount) - 0.115f;
            this.mSlice = new ValueAnimator();
            this.mSlice.setFloatValues(new float[]{arcCenter, arcCenter2});
            this.mSlice.setInterpolator(null);
            this.mSlice.setDuration(PIE_SLICE_DURATION);
            this.mSlice.addListener(new AnimatorListener() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    PieRenderer.this.mSlice = null;
                }
            });
            this.mSlice.start();
        }
    }

    private void openCurrentItem() {
        PieItem pieItem = this.mCurrentItem;
        if (pieItem != null && pieItem.hasItems()) {
            this.mOpen.add(this.mCurrentItem);
            layoutLabel(getLevel());
            this.mOpening = true;
            ValueAnimator valueAnimator = this.mFadeIn;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            this.mXFade = new ValueAnimator();
            this.mXFade.setFloatValues(new float[]{1.0f, 0.0f});
            this.mXFade.setDuration(200);
            this.mXFade.setInterpolator(null);
            final PieItem pieItem2 = this.mCurrentItem;
            this.mXFade.addListener(new AnimatorListener() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    PieRenderer.this.mXFade = null;
                    pieItem2.setSelected(false);
                    PieRenderer.this.mOpening = false;
                }
            });
            this.mXFade.start();
        }
    }

    private PieItem findItem(PointF pointF) {
        List<PieItem> items = getOpenItem().getItems();
        int size = items.size();
        int i = 0;
        for (PieItem pieItem : items) {
            if (inside(pointF, pieItem, i, size)) {
                return pieItem;
            }
            i++;
        }
        return null;
    }

    public void setBlockFocus(boolean z) {
        this.mBlockFocus = z;
        if (z) {
            clear();
        }
    }

    public void setFocus(int i, int i2) {
        this.mOverlay.removeCallbacks(this.mDisappear);
        this.mFocusX = i;
        this.mFocusY = i2;
        setCircle(this.mFocusX, this.mFocusY);
    }

    public int getSize() {
        return this.mCircleSize * 2;
    }

    private int getRandomRange() {
        return (int) ((Math.random() * 120.0d) - 0.0703125d);
    }

    /* access modifiers changed from: private */
    public void setCircle(int i, int i2) {
        RectF rectF = this.mCircle;
        int i3 = this.mCircleSize;
        rectF.set((float) (i - i3), (float) (i2 - i3), (float) (i + i3), (float) (i3 + i2));
        RectF rectF2 = this.mDial;
        int i4 = this.mCircleSize;
        int i5 = i - i4;
        int i6 = this.mInnerOffset;
        rectF2.set((float) (i5 + i6), (float) ((i2 - i4) + i6), (float) ((i + i4) - i6), (float) ((i2 + i4) - i6));
    }

    public void drawFocus(Canvas canvas) {
        if (!this.mBlockFocus) {
            this.mFocusPaint.setStrokeWidth((float) this.mOuterStroke);
            canvas.drawCircle((float) this.mFocusX, (float) this.mFocusY, (float) this.mCircleSize, this.mFocusPaint);
            if (this.mState != 8) {
                int color = this.mFocusPaint.getColor();
                if (this.mState == 2) {
                    this.mFocusPaint.setColor(this.mFocused ? this.mSuccessColor : this.mFailColor);
                }
                this.mFocusPaint.setStrokeWidth((float) this.mInnerStroke);
                drawLine(canvas, this.mDialAngle, this.mFocusPaint);
                drawLine(canvas, this.mDialAngle + 45, this.mFocusPaint);
                drawLine(canvas, this.mDialAngle + 180, this.mFocusPaint);
                drawLine(canvas, this.mDialAngle + 225, this.mFocusPaint);
                canvas.save();
                canvas.rotate((float) this.mDialAngle, (float) this.mFocusX, (float) this.mFocusY);
                Canvas canvas2 = canvas;
                canvas2.drawArc(this.mDial, 0.0f, 45.0f, false, this.mFocusPaint);
                canvas2.drawArc(this.mDial, 180.0f, 45.0f, false, this.mFocusPaint);
                canvas.restore();
                this.mFocusPaint.setColor(color);
            }
        }
    }

    private void drawLine(Canvas canvas, int i, Paint paint) {
        convertCart(i, this.mCircleSize - this.mInnerOffset, this.mPoint1);
        int i2 = this.mCircleSize;
        int i3 = this.mInnerOffset;
        convertCart(i, (i2 - i3) + (i3 / 3), this.mPoint2);
        Point point = this.mPoint1;
        int i4 = point.x;
        int i5 = this.mFocusX;
        float f = (float) (i4 + i5);
        int i6 = point.y;
        int i7 = this.mFocusY;
        float f2 = (float) (i6 + i7);
        Point point2 = this.mPoint2;
        canvas.drawLine(f, f2, (float) (point2.x + i5), (float) (point2.y + i7), paint);
    }

    private static void convertCart(int i, int i2, Point point) {
        double d = (((double) (i % 360)) * 6.283185307179586d) / 360.0d;
        double d2 = (double) i2;
        point.x = (int) ((Math.cos(d) * d2) + 0.5d);
        point.y = (int) ((d2 * Math.sin(d)) + 0.5d);
    }

    public void showStart() {
        if (this.mState != 8) {
            cancelFocus();
            this.mStartAnimationAngle = 67;
            int randomRange = getRandomRange();
            int i = this.mStartAnimationAngle;
            startAnimation(600, false, (float) i, (float) (i + randomRange));
            this.mState = 1;
        }
    }

    public void showSuccess(final boolean z) {
        if (this.mIsBokehMode) {
            this.mOverlay.postDelayed(new Runnable() {
                public void run() {
                    if (PieRenderer.this.mState == 1) {
                        PieRenderer pieRenderer = PieRenderer.this;
                        pieRenderer.startAnimation(100, z, (float) pieRenderer.mStartAnimationAngle);
                        PieRenderer.this.mState = 2;
                        PieRenderer.this.mFocused = true;
                    }
                }
            }, 3000);
        } else if (this.mState == 1) {
            startAnimation(100, z, (float) this.mStartAnimationAngle);
            this.mState = 2;
            this.mFocused = true;
        }
    }

    public void showFail(boolean z) {
        if (this.mState == 1) {
            startAnimation(100, z, (float) this.mStartAnimationAngle);
            this.mState = 2;
            this.mFocused = false;
        }
    }

    private void cancelFocus() {
        this.mFocusCancelled = true;
        RenderOverlay renderOverlay = this.mOverlay;
        if (renderOverlay != null) {
            renderOverlay.removeCallbacks(this.mDisappear);
        }
        ValueAnimator valueAnimator = this.mAnimator;
        if (valueAnimator != null && valueAnimator.isStarted()) {
            this.mAnimator.cancel();
        }
        this.mFocusCancelled = false;
        this.mFocused = false;
        this.mState = 0;
    }

    public void clear(boolean z) {
        if (this.mState != 8) {
            cancelFocus();
            if (z) {
                this.mProgressRenderer.setVisibilityListener(new VisibilityListener() {
                    public void onHidden() {
                        PieRenderer pieRenderer = PieRenderer.this;
                        pieRenderer.mOverlay.post(pieRenderer.mDisappear);
                    }
                });
            } else {
                this.mOverlay.post(this.mDisappear);
                this.mProgressRenderer.setVisibilityListener(null);
            }
        }
    }

    public void clear() {
        clear(false);
    }

    /* access modifiers changed from: private */
    public void startAnimation(long j, boolean z, float f) {
        startAnimation(j, z, (float) this.mDialAngle, f);
    }

    private void startAnimation(long j, final boolean z, float f, float f2) {
        setVisible(true);
        ValueAnimator valueAnimator = this.mAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mAnimator = ValueAnimator.ofFloat(new float[]{f, f2});
        this.mAnimator.setDuration(j);
        this.mAnimator.setInterpolator(null);
        this.mAnimator.addUpdateListener(new AnimatorUpdateListener() {
            private long frames;

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                valueAnimator.getCurrentPlayTime();
                if (this.frames < (valueAnimator.getCurrentPlayTime() * 30) / 1000) {
                    PieRenderer.this.update();
                    this.frames++;
                    PieRenderer.this.mDialAngle = Math.round(((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            }
        });
        this.mAnimator.addListener(new AnimatorListener() {
            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                if (z && !PieRenderer.this.mFocusCancelled) {
                    PieRenderer pieRenderer = PieRenderer.this;
                    pieRenderer.mOverlay.postDelayed(pieRenderer.mDisappear, 200);
                }
            }
        });
        this.mAnimator.start();
        update();
    }
}
