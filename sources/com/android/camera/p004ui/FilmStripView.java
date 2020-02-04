package com.android.camera.p004ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.support.p000v4.media.MediaPlayer2;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;
import com.android.camera.CameraActivity;
import com.android.camera.PhotoMenu;
import com.android.camera.PreviewGestures;
import com.android.camera.VideoMenu;
import com.android.camera.data.LocalData;
import com.android.camera.p004ui.FilmstripBottomControls.BottomControlsListener;
import com.android.camera.util.PhotoSphereHelper.PanoramaViewHelper;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.FilmStripView */
public class FilmStripView extends ViewGroup implements BottomControlsListener {
    private static final int BUFFER_SIZE = 5;
    private static final int CAMERA_PREVIEW_SWIPE_THRESHOLD = 300;
    private static final int DECELERATION_FACTOR = 4;
    private static final float FILM_STRIP_SCALE = 0.5f;
    private static final float FLING_COASTING_DURATION_S = 0.05f;
    private static final float FULL_SCREEN_SCALE = 1.0f;
    private static final int GEOMETRY_ADJUST_TIME_MS = 400;
    private static final int SNAP_IN_CENTER_TIME_MS = 600;
    private static final int SWIPE_TIME_OUT = 500;
    private static final String TAG = "CAM_FilmStripView";
    private static final float TOLERANCE = 0.1f;
    private static final int ZOOM_ANIMATION_DURATION_MS = 200;
    private boolean initialClampX = false;
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    /* access modifiers changed from: private */
    public FilmstripBottomControls mBottomControls;
    private View mCameraView;
    /* access modifiers changed from: private */
    public int mCenterX = -1;
    private boolean mCheckToIntercept = true;
    /* access modifiers changed from: private */
    public MyController mController;
    private final int mCurrentItem = 2;
    /* access modifiers changed from: private */
    public DataAdapter mDataAdapter;
    /* access modifiers changed from: private */
    public int mDataIdOnUserScrolling;
    private MotionEvent mDown;
    /* access modifiers changed from: private */
    public final Rect mDrawArea = new Rect();
    private FilmStripGestureRecognizer mGestureRecognizer;
    /* access modifiers changed from: private */
    public boolean mIsLoaded = false;
    /* access modifiers changed from: private */
    public boolean mIsUserScrolling;
    private long mLastItemId = -1;
    private int mLastTotalNumber = 0;
    /* access modifiers changed from: private */
    public Listener mListener;
    /* access modifiers changed from: private */
    public float mOverScaleFactor = FULL_SCREEN_SCALE;
    private PanoramaViewHelper mPanoramaViewHelper;
    /* access modifiers changed from: private */
    public PreviewGestures mPreviewGestures;
    private RenderOverlay mRenderOverlay;
    private boolean mReset;
    /* access modifiers changed from: private */
    public float mScale;
    private boolean mSendToMenu;
    private boolean mSendToPreviewMenu;
    private int mSlop;
    private TimeInterpolator mViewAnimInterpolator;
    /* access modifiers changed from: private */
    public int mViewGap;
    /* access modifiers changed from: private */
    public ViewItem[] mViewItem = new ViewItem[5];
    private AnimatorUpdateListener mViewItemUpdateListener;
    /* access modifiers changed from: private */
    public ZoomView mZoomView = null;

    /* renamed from: com.android.camera.ui.FilmStripView$Controller */
    public interface Controller {
        void fling(float f);

        void flingInsideZoomView(float f, float f2);

        void goToFilmStrip();

        void goToFirstItem();

        void goToFullScreen();

        boolean goToNextItem();

        boolean isScaling();

        boolean isScrolling();

        void scroll(float f);

        void scrollToPosition(int i, int i2, boolean z);

        boolean stopScrolling(boolean z);
    }

    /* renamed from: com.android.camera.ui.FilmStripView$DataAdapter */
    public interface DataAdapter {

        /* renamed from: com.android.camera.ui.FilmStripView$DataAdapter$Listener */
        public interface Listener {
            void onDataInserted(int i, ImageData imageData);

            void onDataLoaded();

            void onDataRemoved(int i, ImageData imageData);

            void onDataUpdated(UpdateReporter updateReporter);
        }

        /* renamed from: com.android.camera.ui.FilmStripView$DataAdapter$UpdateReporter */
        public interface UpdateReporter {
            boolean isDataRemoved(int i);

            boolean isDataUpdated(int i);
        }

        boolean canSwipeInFullScreen(int i);

        ImageData getImageData(int i);

        int getTotalNumber();

        View getView(Activity activity, int i);

        void setListener(Listener listener);

        void suggestViewSizeBound(int i, int i2);
    }

    /* renamed from: com.android.camera.ui.FilmStripView$ImageData */
    public interface ImageData {
        public static final int ACTION_DEMOTE = 2;
        public static final int ACTION_NONE = 0;
        public static final int ACTION_PROMOTE = 1;
        public static final int ACTION_ZOOM = 4;
        public static final int SIZE_FULL = -2;
        public static final int VIEW_TYPE_NONE = 0;
        public static final int VIEW_TYPE_REMOVABLE = 2;
        public static final int VIEW_TYPE_STICKY = 1;

        /* renamed from: com.android.camera.ui.FilmStripView$ImageData$PanoramaSupportCallback */
        public interface PanoramaSupportCallback {
            void panoramaInfoAvailable(boolean z, boolean z2);
        }

        Uri getContentUri();

        int getHeight();

        double[] getLatLong();

        int getOrientation();

        int getViewType();

        int getWidth();

        boolean isPhoto();

        void isPhotoSphere(Context context, PanoramaSupportCallback panoramaSupportCallback);

        boolean isUIActionSupported(int i);

        void prepare();

        void recycle();

        void viewPhotoSphere(PanoramaViewHelper panoramaViewHelper);
    }

    /* renamed from: com.android.camera.ui.FilmStripView$Listener */
    public interface Listener {
        void onCurrentDataCentered(int i);

        void onCurrentDataOffCentered(int i);

        void onDataDemoted(int i);

        void onDataFocusChanged(int i, boolean z);

        void onDataFullScreenChange(int i, boolean z);

        void onDataPromoted(int i);

        void onReload();

        void onToggleSystemDecorsVisibility(int i);

        void setSystemDecorsVisibility(boolean z);
    }

    /* renamed from: com.android.camera.ui.FilmStripView$MyController */
    private class MyController implements Controller {
        /* access modifiers changed from: private */
        public boolean mCanStopScroll;
        /* access modifiers changed from: private */
        public AnimatorSet mFlingAnimator;
        private final ValueAnimator mScaleAnimator;
        private AnimatorUpdateListener mScaleAnimatorUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (FilmStripView.this.mViewItem[2] != null) {
                    FilmStripView.this.mScale = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    FilmStripView.this.invalidate();
                }
            }
        };
        private final MyScroller mScroller;
        private final Listener mScrollerListener = new Listener() {
            public void onScrollUpdate(int i, int i2) {
                FilmStripView.this.mCenterX = i;
                boolean access$1700 = FilmStripView.this.clampCenterX();
                FilmStripView filmStripView = FilmStripView.this;
                filmStripView.checkCurrentDataCentered(filmStripView.getCurrentId());
                if (access$1700) {
                    FilmStripView.this.mController.stopScrolling(true);
                }
                FilmStripView.this.invalidate();
            }

            public void onScrollEnd() {
                MyController.this.mCanStopScroll = true;
                if (FilmStripView.this.mViewItem[2] != null) {
                    FilmStripView.this.snapInCenter();
                    if (FilmStripView.this.mCenterX == FilmStripView.this.mViewItem[2].getCenterX() && FilmStripView.this.getCurrentViewType() == 1) {
                        MyController.this.goToFullScreen();
                    }
                }
            }
        };
        /* access modifiers changed from: private */
        public ValueAnimator mZoomAnimator;

        MyController(Context context) {
            DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(1.5f);
            this.mScroller = new MyScroller(FilmStripView.this.mActivity, new Handler(FilmStripView.this.mActivity.getMainLooper()), this.mScrollerListener, decelerateInterpolator);
            this.mCanStopScroll = true;
            this.mScaleAnimator = new ValueAnimator();
            this.mScaleAnimator.addUpdateListener(this.mScaleAnimatorUpdateListener);
            this.mScaleAnimator.setInterpolator(decelerateInterpolator);
        }

        public boolean isScrolling() {
            return !this.mScroller.isFinished();
        }

        public boolean isScaling() {
            return this.mScaleAnimator.isRunning();
        }

        private int estimateMinX(int i, int i2, int i3) {
            return i2 - ((i + 100) * (i3 + FilmStripView.this.mViewGap));
        }

        private int estimateMaxX(int i, int i2, int i3) {
            return i2 + (((FilmStripView.this.mDataAdapter.getTotalNumber() - i) + 100) * (i3 + FilmStripView.this.mViewGap));
        }

        /* access modifiers changed from: private */
        public void zoomAt(final ViewItem viewItem, final float f, final float f2) {
            ValueAnimator valueAnimator = this.mZoomAnimator;
            if (valueAnimator != null) {
                valueAnimator.end();
            }
            float currentDataMaxScale = getCurrentDataMaxScale(false);
            if (FilmStripView.this.mScale >= currentDataMaxScale - (0.1f * currentDataMaxScale)) {
                currentDataMaxScale = FilmStripView.FULL_SCREEN_SCALE;
            }
            final float f3 = currentDataMaxScale;
            this.mZoomAnimator = new ValueAnimator();
            this.mZoomAnimator.setFloatValues(new float[]{FilmStripView.this.mScale, f3});
            this.mZoomAnimator.setDuration(200);
            ValueAnimator valueAnimator2 = this.mZoomAnimator;
            final ViewItem viewItem2 = viewItem;
            final float f4 = f;
            final float f5 = f2;
            C08573 r2 = new AnimatorListener() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                    if (FilmStripView.this.mScale == FilmStripView.FULL_SCREEN_SCALE) {
                        MyController.this.enterFullScreen();
                        MyController.this.setSurroundingViewsVisible(false);
                    }
                    MyController.this.cancelLoadingZoomedImage();
                }

                public void onAnimationEnd(Animator animator) {
                    float access$2300 = FilmStripView.this.mScale;
                    float f = f3;
                    if (access$2300 != f) {
                        viewItem2.postScale(f4, f5, f / FilmStripView.this.mScale, FilmStripView.this.mDrawArea.width(), FilmStripView.this.mDrawArea.height());
                        FilmStripView.this.mScale = f3;
                    }
                    if (FilmStripView.this.mScale == FilmStripView.FULL_SCREEN_SCALE) {
                        MyController.this.setSurroundingViewsVisible(true);
                        FilmStripView.this.mZoomView.setVisibility(8);
                        viewItem2.resetTransform();
                    } else {
                        FilmStripView.this.mController.loadZoomedImage();
                    }
                    MyController.this.mZoomAnimator = null;
                }
            };
            valueAnimator2.addListener(r2);
            this.mZoomAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    float access$2300 = floatValue / FilmStripView.this.mScale;
                    FilmStripView.this.mScale = floatValue;
                    viewItem.postScale(f, f2, access$2300, FilmStripView.this.mDrawArea.width(), FilmStripView.this.mDrawArea.height());
                }
            });
            this.mZoomAnimator.start();
        }

        public void scroll(float f) {
            if (stopScrolling(false)) {
                FilmStripView.access$1616(FilmStripView.this, f);
                boolean access$1700 = FilmStripView.this.clampCenterX();
                FilmStripView filmStripView = FilmStripView.this;
                filmStripView.checkCurrentDataCentered(filmStripView.getCurrentId());
                if (access$1700) {
                    FilmStripView.this.mController.stopScrolling(true);
                }
                FilmStripView.this.invalidate();
            }
        }

        public void fling(float f) {
            if (stopScrolling(false)) {
                ViewItem viewItem = FilmStripView.this.mViewItem[2];
                if (viewItem != null) {
                    float access$2300 = f / FilmStripView.this.mScale;
                    if (FilmStripView.this.inFullScreen() && FilmStripView.this.getCurrentViewType() == 1 && access$2300 < 0.0f) {
                        goToFilmStrip();
                    }
                    int width = FilmStripView.this.getWidth();
                    this.mScroller.fling(FilmStripView.this.mCenterX, 0, (int) (-f), 0, estimateMinX(viewItem.getId(), viewItem.getLeftPosition(), width), estimateMaxX(viewItem.getId(), viewItem.getLeftPosition(), width), 0, 0);
                }
            }
        }

        public void flingInsideZoomView(float f, float f2) {
            if (isZoomStarted()) {
                final ViewItem viewItem = FilmStripView.this.mViewItem[2];
                if (viewItem != null) {
                    float pow = (float) (Math.pow((double) Math.max(Math.abs(f), Math.abs(f2)), 0.3333333432674408d) * 0.05000000074505806d);
                    float translationX = viewItem.getTranslationX();
                    float translationY = viewItem.getTranslationY();
                    float f3 = pow / 4.0f;
                    final ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{translationX, translationX + (f * f3)});
                    final ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{translationY, translationY + (f3 * f2)});
                    ofFloat2.addUpdateListener(new AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            viewItem.updateTransform(((Float) ofFloat.getAnimatedValue()).floatValue(), ((Float) ofFloat2.getAnimatedValue()).floatValue(), FilmStripView.this.mScale, FilmStripView.this.mScale, FilmStripView.this.mDrawArea.width(), FilmStripView.this.mDrawArea.height());
                        }
                    });
                    this.mFlingAnimator = new AnimatorSet();
                    this.mFlingAnimator.play(ofFloat).with(ofFloat2);
                    this.mFlingAnimator.setDuration((long) ((int) (pow * 1000.0f)));
                    this.mFlingAnimator.setInterpolator(new TimeInterpolator() {
                        public float getInterpolation(float f) {
                            return (float) (1.0d - Math.pow((double) (FilmStripView.FULL_SCREEN_SCALE - f), 4.0d));
                        }
                    });
                    this.mFlingAnimator.addListener(new AnimatorListener() {
                        private boolean mCancelled = false;

                        public void onAnimationRepeat(Animator animator) {
                        }

                        public void onAnimationStart(Animator animator) {
                        }

                        public void onAnimationEnd(Animator animator) {
                            if (!this.mCancelled) {
                                MyController.this.loadZoomedImage();
                            }
                            MyController.this.mFlingAnimator = null;
                        }

                        public void onAnimationCancel(Animator animator) {
                            this.mCancelled = true;
                        }
                    });
                    this.mFlingAnimator.start();
                }
            }
        }

        public boolean stopScrolling(boolean z) {
            if (!isScrolling()) {
                return true;
            }
            if (!this.mCanStopScroll && !z) {
                return false;
            }
            this.mScroller.forceFinished(true);
            return true;
        }

        /* access modifiers changed from: private */
        public void stopScale() {
            this.mScaleAnimator.cancel();
        }

        public void scrollToPosition(int i, int i2, boolean z) {
            if (FilmStripView.this.mViewItem[2] != null) {
                this.mCanStopScroll = z;
                this.mScroller.startScroll(FilmStripView.this.mCenterX, 0, i - FilmStripView.this.mCenterX, 0, i2);
                FilmStripView filmStripView = FilmStripView.this;
                filmStripView.checkCurrentDataCentered(filmStripView.mViewItem[2].getId());
            }
        }

        public boolean goToNextItem() {
            ViewItem viewItem = FilmStripView.this.mViewItem[3];
            if (viewItem == null) {
                return false;
            }
            stopScrolling(true);
            scrollToPosition(viewItem.getCenterX(), MediaPlayer2.MEDIA_INFO_BAD_INTERLEAVING, false);
            if (FilmStripView.this.getCurrentViewType() == 1) {
                scaleTo(FilmStripView.FILM_STRIP_SCALE, 400);
            }
            return true;
        }

        private void scaleTo(float f, int i) {
            if (FilmStripView.this.mViewItem[2] != null) {
                stopScale();
                this.mScaleAnimator.setDuration((long) i);
                this.mScaleAnimator.setFloatValues(new float[]{FilmStripView.this.mScale, f});
                this.mScaleAnimator.start();
            }
        }

        public void goToFilmStrip() {
            scaleTo(FilmStripView.FILM_STRIP_SCALE, 400);
            ViewItem viewItem = FilmStripView.this.mViewItem[3];
            if (FilmStripView.this.mViewItem[2].getId() == 0 && FilmStripView.this.getCurrentViewType() == 1 && viewItem != null) {
                scrollToPosition(viewItem.getCenterX(), 400, false);
            }
            if (FilmStripView.this.mListener != null) {
                FilmStripView.this.mListener.onDataFullScreenChange(FilmStripView.this.mViewItem[2].getId(), false);
            }
        }

        public void goToFullScreen() {
            if (!FilmStripView.this.inFullScreen()) {
                enterFullScreen();
                scaleTo(FilmStripView.FULL_SCREEN_SCALE, 400);
            }
        }

        /* access modifiers changed from: private */
        public void cancelFlingAnimation() {
            if (isFlingAnimationRunning()) {
                this.mFlingAnimator.cancel();
            }
        }

        /* access modifiers changed from: private */
        public void cancelZoomAnimation() {
            if (isZoomAnimationRunning()) {
                this.mZoomAnimator.cancel();
            }
        }

        /* access modifiers changed from: private */
        public void enterFullScreen() {
            if (FilmStripView.this.mListener != null) {
                FilmStripView.this.mListener.onDataFullScreenChange(FilmStripView.this.mViewItem[2].getId(), true);
            }
        }

        /* access modifiers changed from: private */
        public void setSurroundingViewsVisible(boolean z) {
            for (int i = 0; i < 2; i++) {
                if (!(i == 2 || FilmStripView.this.mViewItem[i] == null)) {
                    FilmStripView.this.mViewItem[i].getView().setVisibility(z ? 0 : 4);
                }
            }
        }

        /* access modifiers changed from: private */
        public void leaveFullScreen() {
            if (FilmStripView.this.mListener != null) {
                FilmStripView.this.mListener.onDataFullScreenChange(FilmStripView.this.mViewItem[2].getId(), false);
            }
        }

        private Uri getCurrentContentUri() {
            ViewItem viewItem = FilmStripView.this.mViewItem[2];
            if (viewItem == null) {
                return Uri.EMPTY;
            }
            return FilmStripView.this.mDataAdapter.getImageData(viewItem.getId()).getContentUri();
        }

        /* access modifiers changed from: private */
        public float getCurrentDataMaxScale(boolean z) {
            ViewItem viewItem = FilmStripView.this.mViewItem[2];
            ImageData imageData = FilmStripView.this.mDataAdapter.getImageData(viewItem.getId());
            if (viewItem == null || !imageData.isUIActionSupported(4)) {
                return FilmStripView.FULL_SCREEN_SCALE;
            }
            float width = (float) imageData.getWidth();
            if (imageData.getOrientation() == 90 || imageData.getOrientation() == 270) {
                width = (float) imageData.getHeight();
            }
            float width2 = width / ((float) viewItem.getWidth());
            if (z) {
                width2 *= FilmStripView.this.mOverScaleFactor;
            }
            return width2;
        }

        /* access modifiers changed from: private */
        public void loadZoomedImage() {
            if (isZoomStarted()) {
                ViewItem viewItem = FilmStripView.this.mViewItem[2];
                if (viewItem != null) {
                    ImageData imageData = FilmStripView.this.mDataAdapter.getImageData(viewItem.getId());
                    if (imageData.isUIActionSupported(4)) {
                        Uri currentContentUri = getCurrentContentUri();
                        RectF viewRect = viewItem.getViewRect();
                        if (!(currentContentUri == null || currentContentUri == Uri.EMPTY)) {
                            FilmStripView.this.mZoomView.loadBitmap(currentContentUri, imageData.getOrientation(), viewRect);
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        public void cancelLoadingZoomedImage() {
            FilmStripView.this.mZoomView.cancelPartialDecodingTask();
        }

        public void goToFirstItem() {
            FilmStripView.this.resetZoomView();
            FilmStripView.this.reload();
        }

        public boolean isZoomStarted() {
            return FilmStripView.this.mScale > FilmStripView.FULL_SCREEN_SCALE;
        }

        public boolean isFlingAnimationRunning() {
            AnimatorSet animatorSet = this.mFlingAnimator;
            return animatorSet != null && animatorSet.isRunning();
        }

        public boolean isZoomAnimationRunning() {
            ValueAnimator valueAnimator = this.mZoomAnimator;
            return valueAnimator != null && valueAnimator.isRunning();
        }
    }

    /* renamed from: com.android.camera.ui.FilmStripView$MyGestureReceiver */
    private class MyGestureReceiver implements com.android.camera.p004ui.FilmStripGestureRecognizer.Listener {
        private float mMaxScale;
        private float mScaleTrend;

        private MyGestureReceiver() {
        }

        public boolean onSingleTapUp(float f, float f2) {
            ViewItem viewItem = FilmStripView.this.mViewItem[2];
            if (FilmStripView.this.inFilmStrip()) {
                if (viewItem != null && viewItem.areaContains(f, f2)) {
                    FilmStripView.this.mController.goToFullScreen();
                    return true;
                }
            } else if (FilmStripView.this.inFullScreen()) {
                int i = -1;
                if (viewItem != null) {
                    i = viewItem.getId();
                }
                FilmStripView.this.mListener.onToggleSystemDecorsVisibility(i);
                return true;
            }
            return false;
        }

        public boolean onDoubleTap(float f, float f2) {
            ViewItem viewItem = FilmStripView.this.mViewItem[2];
            if (FilmStripView.this.inFilmStrip() && viewItem != null) {
                FilmStripView.this.mController.goToFullScreen();
                return true;
            } else if (FilmStripView.this.mScale < FilmStripView.FULL_SCREEN_SCALE || FilmStripView.this.inCameraFullscreen() || viewItem == null || !FilmStripView.this.mController.stopScrolling(false)) {
                return false;
            } else {
                FilmStripView.this.mListener.setSystemDecorsVisibility(false);
                FilmStripView.this.mController.zoomAt(viewItem, f, f2);
                return true;
            }
        }

        public boolean onDown(float f, float f2) {
            FilmStripView.this.mController.cancelFlingAnimation();
            return FilmStripView.this.mController.stopScrolling(false);
        }

        public boolean onUp(float f, float f2) {
            ViewItem viewItem = FilmStripView.this.mViewItem[2];
            if (viewItem != null && !FilmStripView.this.mController.isZoomAnimationRunning() && !FilmStripView.this.mController.isFlingAnimationRunning()) {
                if (FilmStripView.this.mController.isZoomStarted()) {
                    FilmStripView.this.mController.loadZoomedImage();
                    return true;
                }
                float height = (float) (FilmStripView.this.getHeight() / 2);
                FilmStripView.this.mIsUserScrolling = false;
                for (int i = 0; i < 5; i++) {
                    if (FilmStripView.this.mViewItem[i] != null) {
                        float scaledTranslationY = FilmStripView.this.mViewItem[i].getScaledTranslationY(FilmStripView.this.mScale);
                        if (scaledTranslationY != 0.0f) {
                            int id = FilmStripView.this.mViewItem[i].getId();
                            if (FilmStripView.this.mDataAdapter.getImageData(id).isUIActionSupported(2) && scaledTranslationY > height) {
                                FilmStripView.this.demoteData(i, id);
                            } else if (!FilmStripView.this.mDataAdapter.getImageData(id).isUIActionSupported(1) || scaledTranslationY >= (-height)) {
                                FilmStripView.this.mViewItem[i].getView().animate().translationY(0.0f).alpha(FilmStripView.FULL_SCREEN_SCALE).setDuration(400).start();
                            } else {
                                FilmStripView.this.promoteData(i, id);
                            }
                        }
                    }
                }
                int id2 = viewItem.getId();
                if (FilmStripView.this.mCenterX > viewItem.getCenterX() + 300 && id2 == 0 && FilmStripView.this.getCurrentViewType() == 1 && FilmStripView.this.mDataIdOnUserScrolling == 0) {
                    FilmStripView.this.mController.goToFilmStrip();
                    if (FilmStripView.this.mViewItem[3] != null) {
                        FilmStripView.this.mController.scrollToPosition(FilmStripView.this.mViewItem[3].getCenterX(), 400, false);
                    } else {
                        FilmStripView.this.snapInCenter();
                    }
                }
                if (FilmStripView.this.mCenterX == viewItem.getCenterX() && id2 == 0 && FilmStripView.this.getCurrentViewType() == 1) {
                    FilmStripView.this.mController.goToFullScreen();
                } else {
                    if (FilmStripView.this.mDataIdOnUserScrolling == 0 && id2 != 0) {
                        FilmStripView.this.mController.goToFilmStrip();
                        FilmStripView.this.mDataIdOnUserScrolling = id2;
                    }
                    FilmStripView.this.snapInCenter();
                }
            }
            return false;
        }

        public boolean onScroll(float f, float f2, float f3, float f4) {
            if (FilmStripView.this.mPreviewGestures != null && FilmStripView.this.mPreviewGestures.waitUntilNextDown()) {
                return false;
            }
            ViewItem viewItem = FilmStripView.this.mViewItem[2];
            if (viewItem == null || !FilmStripView.this.mDataAdapter.canSwipeInFullScreen(viewItem.getId())) {
                return false;
            }
            FilmStripView.this.hideZoomView();
            if (FilmStripView.this.mController.isZoomStarted()) {
                ViewItem viewItem2 = FilmStripView.this.mViewItem[2];
                viewItem2.updateTransform(viewItem2.getTranslationX() - f3, viewItem2.getTranslationY() - f4, FilmStripView.this.mScale, FilmStripView.this.mScale, FilmStripView.this.mDrawArea.width(), FilmStripView.this.mDrawArea.height());
                return true;
            }
            int access$2300 = (int) (f3 / FilmStripView.this.mScale);
            FilmStripView.this.mController.stopScrolling(true);
            if (!FilmStripView.this.mIsUserScrolling) {
                FilmStripView.this.mIsUserScrolling = true;
                FilmStripView filmStripView = FilmStripView.this;
                filmStripView.mDataIdOnUserScrolling = filmStripView.mViewItem[2].getId();
            }
            if (FilmStripView.this.inFilmStrip()) {
                if (Math.abs(f3) > Math.abs(f4)) {
                    FilmStripView.this.mController.scroll((float) access$2300);
                } else {
                    Rect rect = new Rect();
                    int i = 0;
                    while (i < 5) {
                        if (FilmStripView.this.mViewItem[i] != null) {
                            FilmStripView.this.mViewItem[i].getView().getHitRect(rect);
                            if (rect.contains((int) f, (int) f2)) {
                                break;
                            }
                        }
                        i++;
                    }
                    if (i == 5) {
                        return false;
                    }
                    ImageData imageData = FilmStripView.this.mDataAdapter.getImageData(FilmStripView.this.mViewItem[i].getId());
                    float scaledTranslationY = FilmStripView.this.mViewItem[i].getScaledTranslationY(FilmStripView.this.mScale) - (f4 / FilmStripView.this.mScale);
                    if (!imageData.isUIActionSupported(2) && scaledTranslationY > 0.0f) {
                        scaledTranslationY = 0.0f;
                    }
                    if (!imageData.isUIActionSupported(1) && scaledTranslationY < 0.0f) {
                        scaledTranslationY = 0.0f;
                    }
                    FilmStripView.this.mViewItem[i].setTranslationY(scaledTranslationY, FilmStripView.this.mScale);
                }
            } else if (FilmStripView.this.inFullScreen()) {
                FilmStripView.this.mController.scroll((float) ((int) (((double) access$2300) * 1.2d)));
            }
            FilmStripView.this.invalidate();
            return true;
        }

        public boolean onFling(float f, float f2) {
            if (FilmStripView.this.mPreviewGestures != null && FilmStripView.this.mPreviewGestures.waitUntilNextDown()) {
                return false;
            }
            ViewItem viewItem = FilmStripView.this.mViewItem[2];
            if (viewItem == null || !FilmStripView.this.mDataAdapter.canSwipeInFullScreen(viewItem.getId())) {
                return false;
            }
            if (FilmStripView.this.mController.isZoomStarted()) {
                FilmStripView.this.mController.flingInsideZoomView(f, f2);
                return true;
            } else if (Math.abs(f) < Math.abs(f2)) {
                return true;
            } else {
                if (FilmStripView.this.mScale == FilmStripView.FULL_SCREEN_SCALE) {
                    int centerX = viewItem.getCenterX();
                    if (f > 0.0f) {
                        if (FilmStripView.this.mCenterX > centerX) {
                            FilmStripView.this.mController.scrollToPosition(centerX, 400, true);
                            return true;
                        }
                        ViewItem viewItem2 = FilmStripView.this.mViewItem[1];
                        if (viewItem2 == null) {
                            return false;
                        }
                        FilmStripView.this.mController.scrollToPosition(viewItem2.getCenterX(), 400, true);
                    } else if (FilmStripView.this.mController.stopScrolling(false)) {
                        if (FilmStripView.this.mCenterX < centerX) {
                            FilmStripView.this.mController.scrollToPosition(centerX, 400, true);
                            return true;
                        }
                        ViewItem viewItem3 = FilmStripView.this.mViewItem[3];
                        if (viewItem3 == null) {
                            return false;
                        }
                        FilmStripView.this.mController.scrollToPosition(viewItem3.getCenterX(), 400, true);
                        if (FilmStripView.this.getCurrentViewType() == 1) {
                            FilmStripView.this.mController.goToFilmStrip();
                        }
                    }
                }
                if (FilmStripView.this.mScale == FilmStripView.FILM_STRIP_SCALE) {
                    FilmStripView.this.mController.fling(f);
                }
                return true;
            }
        }

        public boolean onScaleBegin(float f, float f2) {
            if (FilmStripView.this.inCameraFullscreen()) {
                return false;
            }
            FilmStripView.this.hideZoomView();
            this.mScaleTrend = FilmStripView.FULL_SCREEN_SCALE;
            this.mMaxScale = Math.max(FilmStripView.this.mController.getCurrentDataMaxScale(true), FilmStripView.FULL_SCREEN_SCALE);
            return true;
        }

        public boolean onScale(float f, float f2, float f3) {
            if (FilmStripView.this.inCameraFullscreen()) {
                return false;
            }
            this.mScaleTrend = (this.mScaleTrend * 0.3f) + (0.7f * f3);
            float access$2300 = FilmStripView.this.mScale * f3;
            if (FilmStripView.this.mScale < FilmStripView.FULL_SCREEN_SCALE && access$2300 < FilmStripView.FULL_SCREEN_SCALE) {
                FilmStripView.this.mScale = access$2300;
                if (FilmStripView.this.mScale <= FilmStripView.FILM_STRIP_SCALE) {
                    FilmStripView.this.mScale = FilmStripView.FILM_STRIP_SCALE;
                }
                FilmStripView.this.invalidate();
            } else if (FilmStripView.this.mScale < FilmStripView.FULL_SCREEN_SCALE && access$2300 >= FilmStripView.FULL_SCREEN_SCALE) {
                FilmStripView.this.mScale = FilmStripView.FULL_SCREEN_SCALE;
                FilmStripView.this.mController.enterFullScreen();
                FilmStripView.this.invalidate();
                FilmStripView.this.mController.setSurroundingViewsVisible(false);
            } else if (FilmStripView.this.mScale < FilmStripView.FULL_SCREEN_SCALE || access$2300 >= FilmStripView.FULL_SCREEN_SCALE) {
                if (!FilmStripView.this.mController.isZoomStarted()) {
                    FilmStripView.this.mController.setSurroundingViewsVisible(false);
                }
                ViewItem viewItem = FilmStripView.this.mViewItem[2];
                float min = Math.min(access$2300, this.mMaxScale);
                if (min == FilmStripView.this.mScale) {
                    return true;
                }
                viewItem.postScale(f, f2, min / FilmStripView.this.mScale, FilmStripView.this.mDrawArea.width(), FilmStripView.this.mDrawArea.height());
                FilmStripView.this.mScale = min;
            } else {
                FilmStripView.this.mScale = access$2300;
                FilmStripView.this.mController.leaveFullScreen();
                FilmStripView.this.invalidate();
                FilmStripView.this.mController.setSurroundingViewsVisible(true);
            }
            return true;
        }

        public void onScaleEnd() {
            if (FilmStripView.this.mScale <= 1.1f) {
                FilmStripView.this.mController.setSurroundingViewsVisible(true);
                if (FilmStripView.this.mScale <= 0.6f) {
                    FilmStripView.this.mController.goToFilmStrip();
                } else if (this.mScaleTrend > FilmStripView.FULL_SCREEN_SCALE || FilmStripView.this.mScale > 0.9f) {
                    if (FilmStripView.this.mController.isZoomStarted()) {
                        FilmStripView.this.mScale = FilmStripView.FULL_SCREEN_SCALE;
                        FilmStripView.this.resetZoomView();
                    }
                    FilmStripView.this.mController.goToFullScreen();
                } else {
                    FilmStripView.this.mController.goToFilmStrip();
                }
                this.mScaleTrend = FilmStripView.FULL_SCREEN_SCALE;
            }
        }
    }

    /* renamed from: com.android.camera.ui.FilmStripView$MyScroller */
    private static class MyScroller {
        /* access modifiers changed from: private */
        public Handler mHandler;
        /* access modifiers changed from: private */
        public Listener mListener;
        private Runnable mScrollChecker = new Runnable() {
            public void run() {
                if (!MyScroller.this.mScroller.computeScrollOffset()) {
                    MyScroller.this.mListener.onScrollEnd();
                    return;
                }
                MyScroller.this.mListener.onScrollUpdate(MyScroller.this.mScroller.getCurrX(), MyScroller.this.mScroller.getCurrY());
                MyScroller.this.mHandler.removeCallbacks(this);
                MyScroller.this.mHandler.post(this);
            }
        };
        /* access modifiers changed from: private */
        public final Scroller mScroller;
        private final ValueAnimator mXScrollAnimator;
        private AnimatorListener mXScrollAnimatorListener = new AnimatorListener() {
            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                MyScroller.this.mListener.onScrollEnd();
            }
        };
        private AnimatorUpdateListener mXScrollAnimatorUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                MyScroller.this.mListener.onScrollUpdate(((Integer) valueAnimator.getAnimatedValue()).intValue(), 0);
            }
        };

        /* renamed from: com.android.camera.ui.FilmStripView$MyScroller$Listener */
        public interface Listener {
            void onScrollEnd();

            void onScrollUpdate(int i, int i2);
        }

        public MyScroller(Context context, Handler handler, Listener listener, TimeInterpolator timeInterpolator) {
            this.mHandler = handler;
            this.mListener = listener;
            this.mScroller = new Scroller(context);
            this.mXScrollAnimator = new ValueAnimator();
            this.mXScrollAnimator.addUpdateListener(this.mXScrollAnimatorUpdateListener);
            this.mXScrollAnimator.addListener(this.mXScrollAnimatorListener);
            this.mXScrollAnimator.setInterpolator(timeInterpolator);
        }

        public void fling(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            this.mScroller.fling(i, i2, i3, i4, i5, i6, i7, i8);
            runChecker();
        }

        public void startScroll(int i, int i2, int i3, int i4) {
            this.mScroller.startScroll(i, i2, i3, i4);
            runChecker();
        }

        public void startScroll(int i, int i2, int i3, int i4, int i5) {
            this.mXScrollAnimator.cancel();
            this.mXScrollAnimator.setDuration((long) i5);
            this.mXScrollAnimator.setIntValues(new int[]{i, i + i3});
            this.mXScrollAnimator.start();
        }

        public boolean isFinished() {
            return this.mScroller.isFinished() && !this.mXScrollAnimator.isRunning();
        }

        public void forceFinished(boolean z) {
            this.mScroller.forceFinished(z);
            if (z) {
                this.mXScrollAnimator.cancel();
            }
        }

        private void runChecker() {
            Handler handler = this.mHandler;
            if (handler != null && this.mListener != null) {
                handler.removeCallbacks(this.mScrollChecker);
                this.mHandler.post(this.mScrollChecker);
            }
        }
    }

    /* renamed from: com.android.camera.ui.FilmStripView$ViewItem */
    private static class ViewItem {
        private int mDataId;
        private int mLeftPosition = -1;
        private ValueAnimator mTranslationXAnimator = new ValueAnimator();
        private View mView;
        private RectF mViewArea = new RectF();

        public ViewItem(int i, View view, AnimatorUpdateListener animatorUpdateListener) {
            view.setPivotX(0.0f);
            view.setPivotY(0.0f);
            this.mDataId = i;
            this.mView = view;
            this.mTranslationXAnimator.addUpdateListener(animatorUpdateListener);
        }

        public int getId() {
            return this.mDataId;
        }

        public void setId(int i) {
            this.mDataId = i;
        }

        public void setLeftPosition(int i) {
            this.mLeftPosition = i;
        }

        public int getLeftPosition() {
            return this.mLeftPosition;
        }

        public float getScaledTranslationY(float f) {
            return this.mView.getTranslationY() / f;
        }

        public float getScaledTranslationX(float f) {
            return this.mView.getTranslationX() / f;
        }

        public float getTranslationX() {
            return this.mView.getTranslationX();
        }

        public float getTranslationY() {
            return this.mView.getTranslationY();
        }

        public void setTranslationY(float f, float f2) {
            this.mView.setTranslationY(f * f2);
        }

        public void setTranslationX(float f, float f2) {
            this.mView.setTranslationX(f * f2);
        }

        public void animateTranslationX(float f, long j, TimeInterpolator timeInterpolator) {
            this.mTranslationXAnimator.setInterpolator(timeInterpolator);
            this.mTranslationXAnimator.setDuration(j);
            this.mTranslationXAnimator.setFloatValues(new float[]{this.mView.getTranslationX(), f});
            this.mTranslationXAnimator.start();
        }

        public void translateXBy(float f, float f2) {
            View view = this.mView;
            view.setTranslationX(view.getTranslationX() + (f * f2));
        }

        public int getCenterX() {
            return this.mLeftPosition + (this.mView.getMeasuredWidth() / 2);
        }

        public View getView() {
            return this.mView;
        }

        public float getX() {
            return this.mView.getX();
        }

        public float getY() {
            return this.mView.getY();
        }

        private void layoutAt(int i, int i2) {
            try {
                this.mView.layout(i, i2, this.mView.getMeasuredWidth() + i, this.mView.getMeasuredHeight() + i2);
            } catch (NullPointerException unused) {
                Log.e(FilmStripView.TAG, "One of the view children is removed");
            }
        }

        public RectF getViewRect() {
            RectF rectF = new RectF();
            rectF.left = this.mView.getX();
            rectF.top = this.mView.getY();
            rectF.right = rectF.left + (((float) this.mView.getWidth()) * this.mView.getScaleX());
            rectF.bottom = rectF.top + (((float) this.mView.getHeight()) * this.mView.getScaleY());
            return rectF;
        }

        public void layoutIn(Rect rect, int i, float f) {
            layoutAt((int) (((float) rect.centerX()) + ((((float) (this.mLeftPosition - i)) + (this.mTranslationXAnimator.isRunning() ? ((Float) this.mTranslationXAnimator.getAnimatedValue()).floatValue() : 0.0f)) * f)), (int) (((float) rect.centerY()) - (((float) (this.mView.getMeasuredHeight() / 2)) * f)));
            this.mView.setScaleX(f);
            this.mView.setScaleY(f);
            float left = (float) this.mView.getLeft();
            float top = (float) this.mView.getTop();
            this.mViewArea.set(left, top, (((float) this.mView.getMeasuredWidth()) * f) + left, (((float) this.mView.getMeasuredHeight()) * f) + top);
        }

        public boolean areaContains(float f, float f2) {
            return this.mViewArea.contains(f, f2);
        }

        public int getWidth() {
            return this.mView.getWidth();
        }

        public void copyGeometry(ViewItem viewItem) {
            setLeftPosition(viewItem.getLeftPosition());
            View view = viewItem.getView();
            this.mView.setTranslationY(view.getTranslationY());
            this.mView.setTranslationX(view.getTranslationX());
        }

        /* access modifiers changed from: 0000 */
        public void postScale(float f, float f2, float f3, int i, int i2) {
            float x = f - getX();
            float f4 = f3 - FilmStripView.FULL_SCREEN_SCALE;
            updateTransform(getTranslationX() - (x * f4), getTranslationY() - ((f2 - getY()) * f4), this.mView.getScaleX() * f3, this.mView.getScaleY() * f3, i, i2);
        }

        /* access modifiers changed from: 0000 */
        public void updateTransform(float f, float f2, float f3, float f4, int i, int i2) {
            float left = f + ((float) this.mView.getLeft());
            float top = f2 + ((float) this.mView.getTop());
            RectF adjustToFitInBounds = ZoomView.adjustToFitInBounds(new RectF(left, top, (((float) this.mView.getWidth()) * f3) + left, (((float) this.mView.getHeight()) * f4) + top), i, i2);
            this.mView.setScaleX(f3);
            this.mView.setScaleY(f4);
            float left2 = adjustToFitInBounds.left - ((float) this.mView.getLeft());
            float top2 = adjustToFitInBounds.top - ((float) this.mView.getTop());
            this.mView.setTranslationX(left2);
            this.mView.setTranslationY(top2);
        }

        /* access modifiers changed from: 0000 */
        public void resetTransform() {
            this.mView.setScaleX(FilmStripView.FULL_SCREEN_SCALE);
            this.mView.setScaleY(FilmStripView.FULL_SCREEN_SCALE);
            this.mView.setTranslationX(0.0f);
            this.mView.setTranslationY(0.0f);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("DataID = ");
            sb.append(this.mDataId);
            sb.append("\n\t left = ");
            sb.append(this.mLeftPosition);
            sb.append("\n\t viewArea = ");
            sb.append(this.mViewArea);
            sb.append("\n\t centerX = ");
            sb.append(getCenterX());
            sb.append("\n\t view MeasuredSize = ");
            sb.append(this.mView.getMeasuredWidth());
            sb.append(',');
            sb.append(this.mView.getMeasuredHeight());
            sb.append("\n\t view Size = ");
            sb.append(this.mView.getWidth());
            sb.append(',');
            sb.append(this.mView.getHeight());
            sb.append("\n\t view scale = ");
            sb.append(this.mView.getScaleX());
            return sb.toString();
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return true;
    }

    static /* synthetic */ int access$1616(FilmStripView filmStripView, float f) {
        int i = (int) (((float) filmStripView.mCenterX) + f);
        filmStripView.mCenterX = i;
        return i;
    }

    public FilmStripView(Context context) {
        super(context);
        init((CameraActivity) context);
    }

    public FilmStripView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init((CameraActivity) context);
    }

    public FilmStripView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init((CameraActivity) context);
    }

    private void init(CameraActivity cameraActivity) {
        setWillNotDraw(false);
        this.mActivity = cameraActivity;
        this.mScale = FULL_SCREEN_SCALE;
        this.mDataIdOnUserScrolling = 0;
        this.mController = new MyController(cameraActivity);
        this.mViewAnimInterpolator = new DecelerateInterpolator();
        this.mZoomView = new ZoomView(cameraActivity);
        this.mZoomView.setVisibility(8);
        addView(this.mZoomView);
        this.mGestureRecognizer = new FilmStripGestureRecognizer(cameraActivity, new MyGestureReceiver());
        this.mSlop = (int) getContext().getResources().getDimension(C0905R.dimen.pie_touch_slop);
        this.mViewItemUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                FilmStripView.this.invalidate();
            }
        };
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        this.mOverScaleFactor = ((float) displayMetrics.densityDpi) / 160.0f;
        if (this.mOverScaleFactor < FULL_SCREEN_SCALE) {
            this.mOverScaleFactor = FULL_SCREEN_SCALE;
        }
    }

    public void setRenderOverlay(RenderOverlay renderOverlay) {
        this.mRenderOverlay = renderOverlay;
    }

    public void setPreviewGestures(PreviewGestures previewGestures) {
        this.mPreviewGestures = previewGestures;
    }

    public Controller getController() {
        return this.mController;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void setViewGap(int i) {
        this.mViewGap = i;
    }

    public void setPanoramaViewHelper(PanoramaViewHelper panoramaViewHelper) {
        this.mPanoramaViewHelper = panoramaViewHelper;
    }

    private boolean isDataAtCenter(int i) {
        ViewItem[] viewItemArr = this.mViewItem;
        if (viewItemArr[2] != null && viewItemArr[2].getId() == i && this.mViewItem[2].getCenterX() == this.mCenterX) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public int getCurrentViewType() {
        ViewItem viewItem = this.mViewItem[2];
        if (viewItem == null) {
            return 0;
        }
        return this.mDataAdapter.getImageData(viewItem.getId()).getViewType();
    }

    private int[] calculateChildDimension(int i, int i2, int i3, int i4, int i5) {
        if (i3 == 90 || i3 == 270) {
            int i6 = i2;
            i2 = i;
            i = i6;
        }
        if (i == -2 || i2 == -2) {
            i = i4;
            i2 = i5;
        }
        int[] iArr = {i4, i5};
        if (iArr[1] * i > iArr[0] * i2) {
            iArr[1] = (i2 * iArr[0]) / i;
        } else {
            iArr[0] = (i * iArr[1]) / i2;
        }
        return iArr;
    }

    private void measureViewItem(ViewItem viewItem, int i, int i2) {
        ImageData imageData = this.mDataAdapter.getImageData(viewItem.getId());
        if (imageData == null) {
            Log.e(TAG, "trying to measure a null item");
            return;
        }
        int[] calculateChildDimension = calculateChildDimension(imageData.getWidth(), imageData.getHeight(), imageData.getOrientation(), i, i2);
        viewItem.getView().measure(MeasureSpec.makeMeasureSpec(calculateChildDimension[0], 1073741824), MeasureSpec.makeMeasureSpec(calculateChildDimension[1], 1073741824));
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        ViewItem[] viewItemArr;
        super.onMeasure(i, i2);
        int size = MeasureSpec.getSize(i);
        int size2 = MeasureSpec.getSize(i2);
        if (size != 0 && size2 != 0) {
            DataAdapter dataAdapter = this.mDataAdapter;
            if (dataAdapter != null) {
                dataAdapter.suggestViewSizeBound(size / 2, size2 / 2);
            }
            for (ViewItem viewItem : this.mViewItem) {
                if (viewItem != null) {
                    measureViewItem(viewItem, size, size2);
                }
            }
            clampCenterX();
            this.mZoomView.measure(MeasureSpec.makeMeasureSpec(i, 1073741824), MeasureSpec.makeMeasureSpec(i2, 1073741824));
        }
    }

    /* access modifiers changed from: protected */
    public boolean fitSystemWindows(Rect rect) {
        super.fitSystemWindows(rect);
        return false;
    }

    private int findTheNearestView(int i) {
        int i2 = 0;
        while (i2 < 5) {
            ViewItem[] viewItemArr = this.mViewItem;
            if (viewItemArr[i2] != null && viewItemArr[i2].getLeftPosition() != -1) {
                break;
            }
            i2++;
        }
        if (i2 == 5) {
            return -1;
        }
        int abs = Math.abs(i - this.mViewItem[i2].getCenterX());
        for (int i3 = i2 + 1; i3 < 5; i3++) {
            ViewItem[] viewItemArr2 = this.mViewItem;
            if (viewItemArr2[i3] == null) {
                break;
            }
            if (viewItemArr2[i3].getLeftPosition() != -1) {
                int abs2 = Math.abs(i - this.mViewItem[i3].getCenterX());
                if (abs2 < abs) {
                    i2 = i3;
                    abs = abs2;
                }
            }
        }
        return i2;
    }

    private ViewItem buildItemFromData(int i) {
        ImageData imageData = this.mDataAdapter.getImageData(i);
        if (imageData == null) {
            return null;
        }
        imageData.prepare();
        View view = this.mDataAdapter.getView(this.mActivity, i);
        if (view == null) {
            return null;
        }
        ViewItem viewItem = new ViewItem(i, view, this.mViewItemUpdateListener);
        View view2 = viewItem.getView();
        if (view2 != this.mCameraView) {
            addView(viewItem.getView());
        } else {
            view2.setVisibility(0);
            view2.setAlpha(FULL_SCREEN_SCALE);
            view2.setTranslationX(0.0f);
            view2.setTranslationY(0.0f);
        }
        return viewItem;
    }

    private void removeItem(int i) {
        ViewItem[] viewItemArr = this.mViewItem;
        if (i < viewItemArr.length && viewItemArr[i] != null) {
            ImageData imageData = this.mDataAdapter.getImageData(viewItemArr[i].getId());
            if (imageData == null) {
                Log.e(TAG, "trying to remove a null item");
            } else {
                checkForRemoval(imageData, this.mViewItem[i].getView());
                this.mViewItem[i] = null;
            }
        }
    }

    private void stepIfNeeded() {
        if (inFilmStrip() || inFullScreen()) {
            int findTheNearestView = findTheNearestView(this.mCenterX);
            if (!(findTheNearestView == -1 || findTheNearestView == 2)) {
                Listener listener = this.mListener;
                int i = 0;
                if (listener != null) {
                    listener.onDataFocusChanged(this.mViewItem[2].getId(), false);
                }
                int i2 = findTheNearestView - 2;
                if (i2 > 0) {
                    for (int i3 = 0; i3 < i2; i3++) {
                        removeItem(i3);
                    }
                    while (true) {
                        int i4 = i + i2;
                        if (i4 >= 5) {
                            break;
                        }
                        ViewItem[] viewItemArr = this.mViewItem;
                        viewItemArr[i] = viewItemArr[i4];
                        i++;
                    }
                    for (int i5 = 5 - i2; i5 < 5; i5++) {
                        ViewItem[] viewItemArr2 = this.mViewItem;
                        viewItemArr2[i5] = null;
                        int i6 = i5 - 1;
                        if (viewItemArr2[i6] != null) {
                            viewItemArr2[i5] = buildItemFromData(viewItemArr2[i6].getId() + 1);
                        }
                    }
                    adjustChildZOrder();
                } else {
                    int i7 = 4;
                    for (int i8 = 4; i8 >= i2 + 5; i8--) {
                        removeItem(i8);
                    }
                    while (true) {
                        int i9 = i7 + i2;
                        if (i9 < 0) {
                            break;
                        }
                        ViewItem[] viewItemArr3 = this.mViewItem;
                        viewItemArr3[i7] = viewItemArr3[i9];
                        i7--;
                    }
                    for (int i10 = -1 - i2; i10 >= 0; i10--) {
                        ViewItem[] viewItemArr4 = this.mViewItem;
                        viewItemArr4[i10] = null;
                        int i11 = i10 + 1;
                        if (viewItemArr4[i11] != null) {
                            viewItemArr4[i10] = buildItemFromData(viewItemArr4[i11].getId() - 1);
                        }
                    }
                }
                invalidate();
                Listener listener2 = this.mListener;
                if (listener2 != null) {
                    listener2.onDataFocusChanged(this.mViewItem[2].getId(), true);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean clampCenterX() {
        ViewItem viewItem = this.mViewItem[2];
        boolean z = false;
        if (viewItem == null) {
            return false;
        }
        if ((viewItem.getId() == 0 && ((this.mCenterX < viewItem.getCenterX() || this.initialClampX) && this.mDataIdOnUserScrolling <= 1)) || (viewItem.getId() == 1 && this.mCenterX < viewItem.getCenterX() && this.mDataIdOnUserScrolling > 1 && this.mController.isScrolling())) {
            z = true;
        }
        if (viewItem.getId() == this.mDataAdapter.getTotalNumber() - 1 && this.mCenterX > viewItem.getCenterX()) {
            z = true;
        }
        if (z) {
            this.mCenterX = viewItem.getCenterX();
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void checkCurrentDataCentered(int i) {
        if (this.mListener == null) {
            return;
        }
        if (isDataAtCenter(i)) {
            this.mListener.onCurrentDataCentered(i);
        } else {
            this.mListener.onCurrentDataOffCentered(i);
        }
    }

    private void adjustChildZOrder() {
        for (int i = 4; i >= 0; i--) {
            ViewItem[] viewItemArr = this.mViewItem;
            if (viewItemArr[i] != null) {
                bringChildToFront(viewItemArr[i].getView());
            }
        }
        bringChildToFront(this.mZoomView);
    }

    public void onViewPhotoSphere() {
        ViewItem viewItem = this.mViewItem[2];
        if (viewItem != null) {
            this.mDataAdapter.getImageData(viewItem.getId()).viewPhotoSphere(this.mPanoramaViewHelper);
        }
    }

    public void onEdit() {
        ImageData imageData = this.mDataAdapter.getImageData(getCurrentId());
        if (imageData != null && (imageData instanceof LocalData)) {
            this.mActivity.launchEditor((LocalData) imageData);
        }
    }

    public void onTinyPlanet() {
        ImageData imageData = this.mDataAdapter.getImageData(getCurrentId());
        if (imageData != null && (imageData instanceof LocalData)) {
            this.mActivity.launchTinyPlanetEditor((LocalData) imageData);
        }
    }

    public int getCurrentId() {
        ViewItem viewItem = this.mViewItem[2];
        if (viewItem == null) {
            return -1;
        }
        return viewItem.getId();
    }

    private void updateBottomControls(boolean z) {
        if (!this.mActivity.isSecureCamera()) {
            if (this.mBottomControls == null) {
                this.mBottomControls = (FilmstripBottomControls) ((View) getParent()).findViewById(C0905R.C0907id.filmstrip_bottom_controls);
                this.mActivity.setOnActionBarVisibilityListener(this.mBottomControls);
                this.mBottomControls.setListener(this);
            }
            final int currentId = getCurrentId();
            if (currentId >= 0) {
                int totalNumber = this.mDataAdapter.getTotalNumber();
                if (z || ((long) currentId) != this.mLastItemId || this.mLastTotalNumber != totalNumber) {
                    this.mLastTotalNumber = totalNumber;
                    ImageData imageData = this.mDataAdapter.getImageData(currentId);
                    this.mBottomControls.setEditButtonVisibility(imageData.isPhoto());
                    if (imageData.getViewType() != 1) {
                        imageData.isPhotoSphere(this.mActivity, new PanoramaSupportCallback() {
                            public void panoramaInfoAvailable(boolean z, boolean z2) {
                                if (currentId == FilmStripView.this.getCurrentId()) {
                                    if (FilmStripView.this.mListener != null) {
                                        FilmStripView.this.mListener.onDataFocusChanged(currentId, true);
                                    }
                                    FilmStripView.this.mBottomControls.setViewPhotoSphereButtonVisibility(z);
                                    FilmStripView.this.mBottomControls.setTinyPlanetButtonVisibility(z2);
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void snapInCenter() {
        int centerX = this.mViewItem[2].getCenterX();
        if (!this.mController.isScrolling() && !this.mIsUserScrolling) {
            int i = this.mCenterX;
            if (i != centerX) {
                this.mController.scrollToPosition(centerX, (int) ((((float) Math.abs(i - centerX)) * 600.0f) / ((float) this.mDrawArea.width())), false);
                if (getCurrentViewType() == 1 && !this.mController.isScaling() && this.mScale != FULL_SCREEN_SCALE) {
                    this.mController.goToFullScreen();
                }
            }
        }
    }

    private void translateLeftViewItem(int i, int i2, float f) {
        String str = TAG;
        if (i < 0 || i > 4) {
            Log.e(str, "currItem id out of bound.");
            return;
        }
        ViewItem[] viewItemArr = this.mViewItem;
        ViewItem viewItem = viewItemArr[i];
        ViewItem viewItem2 = viewItemArr[i + 1];
        if (viewItem == null || viewItem2 == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid view item (curr or next == null). curr = ");
            sb.append(i);
            Log.e(str, sb.toString());
            return;
        }
        int centerX = viewItem.getCenterX();
        int centerX2 = viewItem2.getCenterX();
        int i3 = (int) (((float) ((centerX2 - i2) - centerX)) * f);
        viewItem.layoutIn(this.mDrawArea, this.mCenterX, this.mScale);
        viewItem.getView().setAlpha(FULL_SCREEN_SCALE);
        if (inFullScreen()) {
            viewItem.setTranslationX((float) ((i3 * (this.mCenterX - centerX)) / (centerX2 - centerX)), this.mScale);
        } else {
            viewItem.setTranslationX((float) i3, this.mScale);
        }
    }

    private void fadeAndScaleRightViewItem(int i) {
        String str = TAG;
        if (i < 1 || i > 5) {
            Log.e(str, "currItem id out of bound.");
            return;
        }
        ViewItem[] viewItemArr = this.mViewItem;
        ViewItem viewItem = viewItemArr[i];
        ViewItem viewItem2 = viewItemArr[i - 1];
        if (viewItem == null || viewItem2 == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid view item (curr or prev == null). curr = ");
            sb.append(i);
            Log.e(str, sb.toString());
            return;
        }
        View view = viewItem.getView();
        if (i > 3) {
            view.setVisibility(4);
            return;
        }
        int centerX = viewItem2.getCenterX();
        if (this.mCenterX <= centerX) {
            view.setVisibility(4);
            return;
        }
        int centerX2 = viewItem.getCenterX();
        float f = (((float) this.mCenterX) - ((float) centerX)) / ((float) (centerX2 - centerX));
        viewItem.layoutIn(this.mDrawArea, centerX2, (f * FILM_STRIP_SCALE) + FILM_STRIP_SCALE);
        view.setAlpha(f);
        view.setTranslationX(0.0f);
        view.setVisibility(0);
    }

    private void layoutViewItems(boolean z) {
        if (!(this.mViewItem[2] == null || this.mDrawArea.width() == 0 || this.mDrawArea.height() == 0)) {
            ViewItem[] viewItemArr = this.mViewItem;
            viewItemArr[2].setLeftPosition(this.mCenterX - (viewItemArr[2].getView().getMeasuredWidth() / 2));
            if (!this.mController.isZoomStarted()) {
                float interpolation = this.mViewAnimInterpolator.getInterpolation((this.mScale - FILM_STRIP_SCALE) / FILM_STRIP_SCALE);
                int width = this.mDrawArea.width() + this.mViewGap;
                for (int i = 1; i >= 0; i--) {
                    ViewItem[] viewItemArr2 = this.mViewItem;
                    ViewItem viewItem = viewItemArr2[i];
                    if (viewItem == null) {
                        break;
                    }
                    viewItem.setLeftPosition((viewItemArr2[i + 1].getLeftPosition() - viewItem.getView().getMeasuredWidth()) - this.mViewGap);
                }
                for (int i2 = 3; i2 < 5; i2++) {
                    ViewItem[] viewItemArr3 = this.mViewItem;
                    ViewItem viewItem2 = viewItemArr3[i2];
                    if (viewItem2 == null) {
                        break;
                    }
                    ViewItem viewItem3 = viewItemArr3[i2 - 1];
                    viewItem2.setLeftPosition(viewItem3.getLeftPosition() + viewItem3.getView().getMeasuredWidth() + this.mViewGap);
                }
                if (this.mViewItem[2].getId() == 1 && this.mDataAdapter.getImageData(0).getViewType() == 1) {
                    ViewItem viewItem4 = this.mViewItem[2];
                    viewItem4.layoutIn(this.mDrawArea, this.mCenterX, this.mScale);
                    viewItem4.setTranslationX(0.0f, this.mScale);
                    viewItem4.getView().setAlpha(FULL_SCREEN_SCALE);
                } else if (interpolation == FULL_SCREEN_SCALE) {
                    ViewItem viewItem5 = this.mViewItem[2];
                    int centerX = viewItem5.getCenterX();
                    int i3 = this.mCenterX;
                    if (i3 < centerX) {
                        fadeAndScaleRightViewItem(2);
                    } else if (i3 > centerX) {
                        translateLeftViewItem(2, width, interpolation);
                    } else {
                        viewItem5.layoutIn(this.mDrawArea, i3, this.mScale);
                        viewItem5.setTranslationX(0.0f, this.mScale);
                        viewItem5.getView().setAlpha(FULL_SCREEN_SCALE);
                    }
                } else {
                    ViewItem viewItem6 = this.mViewItem[2];
                    viewItem6.setTranslationX(viewItem6.getScaledTranslationX(this.mScale) * interpolation, this.mScale);
                    viewItem6.layoutIn(this.mDrawArea, this.mCenterX, this.mScale);
                    if (this.mViewItem[1] == null) {
                        viewItem6.getView().setAlpha(FULL_SCREEN_SCALE);
                    } else {
                        int centerX2 = viewItem6.getCenterX();
                        int centerX3 = this.mViewItem[1].getCenterX();
                        float f = (((float) this.mCenterX) - ((float) centerX3)) / ((float) (centerX2 - centerX3));
                        viewItem6.getView().setAlpha(((FULL_SCREEN_SCALE - f) * (FULL_SCREEN_SCALE - interpolation)) + f);
                    }
                }
                int i4 = 1;
                while (i4 >= 0 && this.mViewItem[i4] != null) {
                    translateLeftViewItem(i4, width, interpolation);
                    i4--;
                }
                for (int i5 = 3; i5 < 5; i5++) {
                    ViewItem viewItem7 = this.mViewItem[i5];
                    if (viewItem7 == null) {
                        break;
                    }
                    viewItem7.layoutIn(this.mDrawArea, this.mCenterX, this.mScale);
                    if (viewItem7.getId() == 1 && getCurrentViewType() == 1) {
                        viewItem7.getView().setAlpha(FULL_SCREEN_SCALE);
                    } else {
                        View view = viewItem7.getView();
                        if (interpolation == FULL_SCREEN_SCALE) {
                            fadeAndScaleRightViewItem(i5);
                        } else {
                            if (view.getVisibility() == 4) {
                                view.setVisibility(0);
                            }
                            if (i5 == 3) {
                                view.setAlpha(FULL_SCREEN_SCALE - interpolation);
                            } else if (interpolation == 0.0f) {
                                view.setAlpha(FULL_SCREEN_SCALE);
                            } else {
                                view.setVisibility(4);
                            }
                            viewItem7.setTranslationX(((float) (this.mViewItem[2].getLeftPosition() - viewItem7.getLeftPosition())) * interpolation, this.mScale);
                        }
                    }
                }
                stepIfNeeded();
                updateBottomControls(false);
                this.mLastItemId = (long) getCurrentId();
            }
        }
    }

    public void onDraw(Canvas canvas) {
        layoutViewItems(false);
        super.onDraw(canvas);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        Rect rect = this.mDrawArea;
        rect.left = i;
        rect.top = i2;
        rect.right = i3;
        rect.bottom = i4;
        this.mZoomView.layout(rect.left, rect.top, rect.right, rect.bottom);
        if (!this.mController.isZoomStarted() || z) {
            resetZoomView();
            layoutViewItems(z);
        }
    }

    /* access modifiers changed from: private */
    public void resetZoomView() {
        if (this.mController.isZoomStarted()) {
            ViewItem viewItem = this.mViewItem[2];
            if (viewItem != null) {
                this.mScale = FULL_SCREEN_SCALE;
                this.mController.cancelZoomAnimation();
                this.mController.cancelFlingAnimation();
                viewItem.resetTransform();
                this.mController.cancelLoadingZoomedImage();
                this.mZoomView.setVisibility(8);
                this.mController.setSurroundingViewsVisible(true);
            }
        }
    }

    /* access modifiers changed from: private */
    public void hideZoomView() {
        if (this.mController.isZoomStarted()) {
            this.mController.cancelLoadingZoomedImage();
            this.mZoomView.setVisibility(8);
        }
    }

    /* access modifiers changed from: private */
    public void checkForRemoval(ImageData imageData, View view) {
        if (imageData.getViewType() != 1) {
            removeView(view);
            imageData.recycle();
            return;
        }
        view.setVisibility(4);
        View view2 = this.mCameraView;
        if (!(view2 == null || view2 == view)) {
            removeView(view2);
        }
        this.mCameraView = view;
    }

    private void slideViewBack(ViewItem viewItem) {
        viewItem.animateTranslationX(0.0f, 400, this.mViewAnimInterpolator);
        viewItem.getView().animate().alpha(FULL_SCREEN_SCALE).setDuration(400).setInterpolator(this.mViewAnimInterpolator).start();
    }

    /* access modifiers changed from: private */
    public void animateItemRemoval(int i, final ImageData imageData) {
        int findItemByDataID = findItemByDataID(i);
        for (int i2 = 0; i2 < 5; i2++) {
            ViewItem[] viewItemArr = this.mViewItem;
            if (viewItemArr[i2] != null && viewItemArr[i2].getId() > i) {
                ViewItem[] viewItemArr2 = this.mViewItem;
                viewItemArr2[i2].setId(viewItemArr2[i2].getId() - 1);
            }
        }
        if (findItemByDataID != -1) {
            final View view = this.mViewItem[findItemByDataID].getView();
            int measuredWidth = view.getMeasuredWidth() + this.mViewGap;
            for (int i3 = findItemByDataID + 1; i3 < 5; i3++) {
                ViewItem[] viewItemArr3 = this.mViewItem;
                if (viewItemArr3[i3] != null) {
                    viewItemArr3[i3].setLeftPosition(viewItemArr3[i3].getLeftPosition() - measuredWidth);
                }
            }
            if (findItemByDataID < 2 || this.mViewItem[findItemByDataID].getId() >= this.mDataAdapter.getTotalNumber()) {
                this.mCenterX -= measuredWidth;
                for (int i4 = findItemByDataID; i4 > 0; i4--) {
                    ViewItem[] viewItemArr4 = this.mViewItem;
                    viewItemArr4[i4] = viewItemArr4[i4 - 1];
                }
                ViewItem[] viewItemArr5 = this.mViewItem;
                if (viewItemArr5[1] != null) {
                    viewItemArr5[0] = buildItemFromData(viewItemArr5[1].getId() - 1);
                }
                while (findItemByDataID >= 0) {
                    ViewItem[] viewItemArr6 = this.mViewItem;
                    if (viewItemArr6[findItemByDataID] != null) {
                        viewItemArr6[findItemByDataID].setTranslationX((float) (-measuredWidth), this.mScale);
                    }
                    findItemByDataID--;
                }
            } else {
                int i5 = findItemByDataID;
                while (i5 < 4) {
                    ViewItem[] viewItemArr7 = this.mViewItem;
                    int i6 = i5 + 1;
                    viewItemArr7[i5] = viewItemArr7[i6];
                    i5 = i6;
                }
                ViewItem[] viewItemArr8 = this.mViewItem;
                if (viewItemArr8[3] != null) {
                    viewItemArr8[4] = buildItemFromData(viewItemArr8[3].getId() + 1);
                }
                if (inFullScreen()) {
                    this.mViewItem[2].getView().setVisibility(0);
                    ViewItem viewItem = this.mViewItem[3];
                    if (viewItem != null) {
                        viewItem.getView().setVisibility(4);
                    }
                }
                while (findItemByDataID < 5) {
                    ViewItem[] viewItemArr9 = this.mViewItem;
                    if (viewItemArr9[findItemByDataID] != null) {
                        viewItemArr9[findItemByDataID].setTranslationX((float) measuredWidth, this.mScale);
                    }
                    findItemByDataID++;
                }
                ViewItem viewItem2 = this.mViewItem[2];
                if (viewItem2.getId() == this.mDataAdapter.getTotalNumber() - 1 && this.mCenterX > viewItem2.getCenterX()) {
                    int centerX = viewItem2.getCenterX() - this.mCenterX;
                    this.mCenterX = viewItem2.getCenterX();
                    for (int i7 = 0; i7 < 5; i7++) {
                        ViewItem[] viewItemArr10 = this.mViewItem;
                        if (viewItemArr10[i7] != null) {
                            viewItemArr10[i7].translateXBy((float) centerX, this.mScale);
                        }
                    }
                }
            }
            for (int i8 = 0; i8 < 5; i8++) {
                ViewItem[] viewItemArr11 = this.mViewItem;
                if (!(viewItemArr11[i8] == null || viewItemArr11[i8].getScaledTranslationX(this.mScale) == 0.0f)) {
                    slideViewBack(this.mViewItem[i8]);
                }
            }
            if (this.mCenterX == this.mViewItem[2].getCenterX() && getCurrentViewType() == 1) {
                this.mController.goToFullScreen();
            }
            int height = getHeight() / 8;
            if (view.getTranslationY() < 0.0f) {
                height = -height;
            }
            view.animate().alpha(0.0f).translationYBy((float) height).setInterpolator(this.mViewAnimInterpolator).setDuration(400).setListener(new AnimatorListener() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    FilmStripView.this.checkForRemoval(imageData, view);
                }
            }).start();
            adjustChildZOrder();
            invalidate();
        }
    }

    private int findItemByDataID(int i) {
        for (int i2 = 0; i2 < 5; i2++) {
            ViewItem[] viewItemArr = this.mViewItem;
            if (viewItemArr[i2] != null && viewItemArr[i2].getId() == i) {
                return i2;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public void updateInsertion(int i) {
        int findItemByDataID = findItemByDataID(i);
        if (findItemByDataID == -1 && i == this.mDataAdapter.getTotalNumber() - 1) {
            int findItemByDataID2 = findItemByDataID(i - 1);
            if (findItemByDataID2 >= 0 && findItemByDataID2 < 4) {
                findItemByDataID = findItemByDataID2 + 1;
            }
        }
        for (int i2 = 0; i2 < 5; i2++) {
            ViewItem[] viewItemArr = this.mViewItem;
            if (viewItemArr[i2] != null && viewItemArr[i2].getId() >= i) {
                ViewItem[] viewItemArr2 = this.mViewItem;
                viewItemArr2[i2].setId(viewItemArr2[i2].getId() + 1);
            }
        }
        if (findItemByDataID != -1) {
            ImageData imageData = this.mDataAdapter.getImageData(i);
            int i3 = calculateChildDimension(imageData.getWidth(), imageData.getHeight(), imageData.getOrientation(), getMeasuredWidth(), getMeasuredHeight())[0] + this.mViewGap;
            ViewItem buildItemFromData = buildItemFromData(i);
            if (findItemByDataID >= 2) {
                if (findItemByDataID == 2) {
                    buildItemFromData.setLeftPosition(this.mViewItem[2].getLeftPosition());
                }
                removeItem(4);
                for (int i4 = 4; i4 > findItemByDataID; i4--) {
                    ViewItem[] viewItemArr3 = this.mViewItem;
                    viewItemArr3[i4] = viewItemArr3[i4 - 1];
                    if (viewItemArr3[i4] != null) {
                        viewItemArr3[i4].setTranslationX((float) (-i3), this.mScale);
                        slideViewBack(this.mViewItem[i4]);
                    }
                }
            } else {
                findItemByDataID--;
                if (findItemByDataID >= 0) {
                    removeItem(0);
                    for (int i5 = 1; i5 <= findItemByDataID; i5++) {
                        ViewItem[] viewItemArr4 = this.mViewItem;
                        if (viewItemArr4[i5] != null) {
                            viewItemArr4[i5].setTranslationX((float) i3, this.mScale);
                            slideViewBack(this.mViewItem[i5]);
                            ViewItem[] viewItemArr5 = this.mViewItem;
                            viewItemArr5[i5 - 1] = viewItemArr5[i5];
                        }
                    }
                } else {
                    return;
                }
            }
            ViewItem[] viewItemArr6 = this.mViewItem;
            viewItemArr6[findItemByDataID] = buildItemFromData;
            View view = viewItemArr6[findItemByDataID].getView();
            view.setAlpha(0.0f);
            view.setTranslationY((float) (getHeight() / 8));
            view.animate().alpha(FULL_SCREEN_SCALE).translationY(0.0f).setInterpolator(this.mViewAnimInterpolator).setDuration(400).start();
            adjustChildZOrder();
            invalidate();
        }
    }

    public void setDataAdapter(DataAdapter dataAdapter) {
        this.mDataAdapter = dataAdapter;
        this.mDataAdapter.suggestViewSizeBound(getMeasuredWidth(), getMeasuredHeight());
        this.mDataAdapter.setListener(new Listener() {
            public void onDataLoaded() {
                FilmStripView.this.mActivity.updateThumbnail(false);
                if (!FilmStripView.this.mIsLoaded) {
                    FilmStripView.this.reload();
                }
                FilmStripView.this.mIsLoaded = true;
            }

            public void onDataUpdated(UpdateReporter updateReporter) {
                FilmStripView.this.update(updateReporter);
            }

            public void onDataInserted(int i, ImageData imageData) {
                if (FilmStripView.this.mViewItem[2] == null) {
                    FilmStripView.this.reload();
                    return;
                }
                FilmStripView.this.updateInsertion(i);
                FilmStripView.this.mActivity.updateThumbnail(true);
            }

            public void onDataRemoved(int i, ImageData imageData) {
                FilmStripView.this.animateItemRemoval(i, imageData);
            }
        });
    }

    public boolean inFilmStrip() {
        return this.mScale == FILM_STRIP_SCALE;
    }

    public boolean inFullScreen() {
        return this.mScale == FULL_SCREEN_SCALE;
    }

    public boolean isCameraPreview() {
        return getCurrentViewType() == 1;
    }

    public boolean inCameraFullscreen() {
        if (!isDataAtCenter(0) || !inFullScreen() || getCurrentViewType() != 1) {
            return false;
        }
        return true;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!inFullScreen() || this.mController.isScrolling()) {
            return true;
        }
        this.initialClampX = false;
        if (motionEvent.getActionMasked() == 0) {
            this.mCheckToIntercept = true;
            this.mDown = MotionEvent.obtain(motionEvent);
            ViewItem viewItem = this.mViewItem[2];
            if (viewItem != null && !this.mDataAdapter.canSwipeInFullScreen(viewItem.getId())) {
                this.mCheckToIntercept = false;
            }
            return false;
        } else if (motionEvent.getActionMasked() == 5) {
            this.mCheckToIntercept = false;
            return false;
        } else if (!this.mCheckToIntercept || motionEvent.getEventTime() - motionEvent.getDownTime() > 500) {
            return false;
        } else {
            int x = (int) (motionEvent.getX() - this.mDown.getX());
            int y = (int) (motionEvent.getY() - this.mDown.getY());
            if (motionEvent.getActionMasked() != 2 || x >= this.mSlop * -1 || Math.abs(x) >= Math.abs(y) * 2) {
            }
            return false;
        }
    }

    public boolean checkSendToModeView(MotionEvent motionEvent) {
        if (!this.mSendToPreviewMenu && !this.mSendToMenu) {
            PreviewGestures previewGestures = this.mPreviewGestures;
            if (previewGestures != null) {
                PhotoMenu photoMenu = previewGestures.getPhotoMenu();
                VideoMenu videoMenu = this.mPreviewGestures.getVideoMenu();
                if (photoMenu != null) {
                    if (photoMenu.isMenuBeingShown() && photoMenu.isMenuBeingAnimated() && photoMenu.isOverMenu(motionEvent)) {
                        this.mSendToMenu = true;
                        return true;
                    } else if (photoMenu.isPreviewMenuBeingShown() && photoMenu.isOverPreviewMenu(motionEvent)) {
                        this.mSendToPreviewMenu = true;
                        return true;
                    }
                }
                if (videoMenu != null) {
                    if (videoMenu.isMenuBeingShown() && videoMenu.isMenuBeingAnimated() && videoMenu.isOverMenu(motionEvent)) {
                        this.mSendToMenu = true;
                        return true;
                    } else if (videoMenu.isPreviewMenuBeingShown() && videoMenu.isOverPreviewMenu(motionEvent)) {
                        this.mSendToPreviewMenu = true;
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }

    public boolean sendToModeView(MotionEvent motionEvent) {
        if (this.mPreviewGestures == null) {
            return false;
        }
        if (this.mReset) {
            this.mSendToPreviewMenu = false;
            this.mSendToMenu = false;
            this.mReset = false;
        }
        if ((this.mSendToPreviewMenu || this.mSendToMenu) && (1 == motionEvent.getActionMasked() || 3 == motionEvent.getActionMasked())) {
            this.mReset = true;
        }
        PhotoMenu photoMenu = this.mPreviewGestures.getPhotoMenu();
        VideoMenu videoMenu = this.mPreviewGestures.getVideoMenu();
        if (photoMenu != null) {
            if (this.mSendToPreviewMenu) {
                return photoMenu.sendTouchToPreviewMenu(motionEvent);
            }
            if (this.mSendToMenu) {
                return photoMenu.sendTouchToMenu(motionEvent);
            }
            if (photoMenu.isMenuBeingShown()) {
                return photoMenu.sendTouchToMenu(motionEvent);
            }
            if (photoMenu.isPreviewMenuBeingShown()) {
                return photoMenu.sendTouchToPreviewMenu(motionEvent);
            }
        }
        if (videoMenu != null) {
            if (this.mSendToPreviewMenu) {
                return videoMenu.sendTouchToPreviewMenu(motionEvent);
            }
            if (this.mSendToMenu) {
                return videoMenu.sendTouchToMenu(motionEvent);
            }
            if (videoMenu.isMenuBeingShown()) {
                return videoMenu.sendTouchToMenu(motionEvent);
            }
            if (videoMenu.isPreviewMenuBeingShown()) {
                return videoMenu.sendTouchToPreviewMenu(motionEvent);
            }
        }
        return false;
    }

    private void updateViewItem(int i) {
        ViewItem viewItem = this.mViewItem[i];
        String str = TAG;
        if (viewItem == null) {
            Log.e(str, "trying to update an null item");
            return;
        }
        removeView(viewItem.getView());
        ImageData imageData = this.mDataAdapter.getImageData(viewItem.getId());
        if (imageData == null) {
            Log.e(str, "trying recycle a null item");
            return;
        }
        imageData.recycle();
        ViewItem buildItemFromData = buildItemFromData(viewItem.getId());
        if (buildItemFromData == null) {
            Log.e(str, "new item is null");
            imageData.prepare();
            addView(viewItem.getView());
            return;
        }
        buildItemFromData.copyGeometry(viewItem);
        this.mViewItem[i] = buildItemFromData;
        boolean clampCenterX = clampCenterX();
        checkCurrentDataCentered(getCurrentId());
        if (clampCenterX) {
            this.mController.stopScrolling(true);
        }
        adjustChildZOrder();
        invalidate();
    }

    /* access modifiers changed from: private */
    public void update(UpdateReporter updateReporter) {
        ViewItem[] viewItemArr = this.mViewItem;
        if (viewItemArr[2] == null) {
            reload();
            return;
        }
        ViewItem viewItem = viewItemArr[2];
        int id = viewItem.getId();
        if (updateReporter.isDataRemoved(id) || this.mDataAdapter.getTotalNumber() == 1) {
            reload();
            return;
        }
        if (updateReporter.isDataUpdated(id)) {
            resetZoomView();
            updateViewItem(2);
            ImageData imageData = this.mDataAdapter.getImageData(id);
            if (!this.mIsUserScrolling && !this.mController.isScrolling()) {
                this.mCenterX = viewItem.getLeftPosition() + (calculateChildDimension(imageData.getWidth(), imageData.getHeight(), imageData.getOrientation(), getMeasuredWidth(), getMeasuredHeight())[0] / 2);
            }
        }
        for (int i = 1; i >= 0; i--) {
            ViewItem[] viewItemArr2 = this.mViewItem;
            ViewItem viewItem2 = viewItemArr2[i];
            if (viewItem2 != null) {
                int id2 = viewItem2.getId();
                if (updateReporter.isDataRemoved(id2) || updateReporter.isDataUpdated(id2)) {
                    updateViewItem(i);
                }
            } else {
                ViewItem viewItem3 = viewItemArr2[i + 1];
                if (viewItem3 != null) {
                    viewItemArr2[i] = buildItemFromData(viewItem3.getId() - 1);
                }
            }
        }
        for (int i2 = 3; i2 < 5; i2++) {
            ViewItem[] viewItemArr3 = this.mViewItem;
            ViewItem viewItem4 = viewItemArr3[i2];
            if (viewItem4 != null) {
                int id3 = viewItem4.getId();
                if (updateReporter.isDataRemoved(id3) || updateReporter.isDataUpdated(id3)) {
                    updateViewItem(i2);
                }
            } else {
                ViewItem viewItem5 = viewItemArr3[i2 - 1];
                if (viewItem5 != null) {
                    viewItemArr3[i2] = buildItemFromData(viewItem5.getId() + 1);
                }
            }
        }
        adjustChildZOrder();
        requestLayout();
        updateBottomControls(true);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x006f A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0070  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reload() {
        /*
            r10 = this;
            com.android.camera.ui.FilmStripView$MyController r0 = r10.mController
            r1 = 1
            r0.stopScrolling(r1)
            com.android.camera.ui.FilmStripView$MyController r0 = r10.mController
            r0.stopScale()
            r0 = 0
            r10.mDataIdOnUserScrolling = r0
            com.android.camera.ui.FilmStripView$Listener r2 = r10.mListener
            r3 = 2
            if (r2 == 0) goto L_0x0034
            com.android.camera.ui.FilmStripView$ViewItem[] r2 = r10.mViewItem
            r4 = r2[r3]
            if (r4 == 0) goto L_0x0034
            r2 = r2[r3]
            int r2 = r2.getId()
            if (r2 != 0) goto L_0x0023
            r2 = r1
            goto L_0x0024
        L_0x0023:
            r2 = r0
        L_0x0024:
            if (r2 != 0) goto L_0x0035
            com.android.camera.ui.FilmStripView$Listener r4 = r10.mListener
            com.android.camera.ui.FilmStripView$ViewItem[] r5 = r10.mViewItem
            r5 = r5[r3]
            int r5 = r5.getId()
            r4.onDataFocusChanged(r5, r0)
            goto L_0x0035
        L_0x0034:
            r2 = r0
        L_0x0035:
            com.android.camera.ui.FilmStripView$ViewItem[] r4 = r10.mViewItem
            int r5 = r4.length
            r6 = r0
        L_0x0039:
            if (r6 >= r5) goto L_0x0061
            r7 = r4[r6]
            if (r7 == 0) goto L_0x005e
            com.android.camera.ui.FilmStripView$DataAdapter r8 = r10.mDataAdapter
            int r9 = r7.getId()
            com.android.camera.ui.FilmStripView$ImageData r8 = r8.getImageData(r9)
            if (r8 == 0) goto L_0x005e
            r8.recycle()
            android.view.View r7 = r7.getView()
            int r8 = r8.getViewType()
            if (r8 == r1) goto L_0x005c
            r10.removeView(r7)
            goto L_0x005e
        L_0x005c:
            r10.mCameraView = r7
        L_0x005e:
            int r6 = r6 + 1
            goto L_0x0039
        L_0x0061:
            com.android.camera.ui.FilmStripView$ViewItem[] r4 = r10.mViewItem
            r5 = 0
            java.util.Arrays.fill(r4, r5)
            com.android.camera.ui.FilmStripView$DataAdapter r4 = r10.mDataAdapter
            int r4 = r4.getTotalNumber()
            if (r4 != 0) goto L_0x0070
            return
        L_0x0070:
            com.android.camera.ui.FilmStripView$ViewItem[] r4 = r10.mViewItem
            com.android.camera.ui.FilmStripView$ViewItem r5 = r10.buildItemFromData(r0)
            r4[r3] = r5
            com.android.camera.ui.FilmStripView$ViewItem[] r4 = r10.mViewItem
            r5 = r4[r3]
            if (r5 != 0) goto L_0x007f
            return
        L_0x007f:
            r4 = r4[r3]
            r4.setLeftPosition(r0)
            r0 = 3
        L_0x0085:
            r4 = 5
            if (r0 >= r4) goto L_0x00a3
            com.android.camera.ui.FilmStripView$ViewItem[] r4 = r10.mViewItem
            int r5 = r0 + -1
            r5 = r4[r5]
            int r5 = r5.getId()
            int r5 = r5 + r1
            com.android.camera.ui.FilmStripView$ViewItem r5 = r10.buildItemFromData(r5)
            r4[r0] = r5
            com.android.camera.ui.FilmStripView$ViewItem[] r4 = r10.mViewItem
            r4 = r4[r0]
            if (r4 != 0) goto L_0x00a0
            goto L_0x00a3
        L_0x00a0:
            int r0 = r0 + 1
            goto L_0x0085
        L_0x00a3:
            r0 = -1
            r10.mCenterX = r0
            r0 = 1065353216(0x3f800000, float:1.0)
            r10.mScale = r0
            r10.initialClampX = r1
            r10.adjustChildZOrder()
            r10.invalidate()
            com.android.camera.ui.FilmStripView$Listener r0 = r10.mListener
            if (r0 == 0) goto L_0x00c8
            r0.onReload()
            if (r2 != 0) goto L_0x00c8
            com.android.camera.ui.FilmStripView$Listener r0 = r10.mListener
            com.android.camera.ui.FilmStripView$ViewItem[] r10 = r10.mViewItem
            r10 = r10[r3]
            int r10 = r10.getId()
            r0.onDataFocusChanged(r10, r1)
        L_0x00c8:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.p004ui.FilmStripView.reload():void");
    }

    /* access modifiers changed from: private */
    public void promoteData(int i, int i2) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onDataPromoted(i2);
        }
    }

    /* access modifiers changed from: private */
    public void demoteData(int i, int i2) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onDataDemoted(i2);
        }
    }
}
