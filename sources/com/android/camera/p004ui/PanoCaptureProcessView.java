package com.android.camera.p004ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.support.p000v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.android.camera.CameraActivity;
import com.android.camera.PanoCaptureModule;
import com.android.camera.util.CameraUtil;
import com.asus.scenedetectlib.BuildConfig;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.PanoCaptureProcessView */
public class PanoCaptureProcessView extends View implements SensorEventListener {
    private static final boolean DEBUG = false;
    /* access modifiers changed from: private */
    public static int DECISION_MARGIN = 0;
    private static final int DEG_INIT_VALUE = 365;
    private static final int DIRECTION_GOT_LOST = -1;
    private static final int DIRECTION_LEFTRIGHT = 0;
    private static final int DIRECTION_UPDOWN = 1;
    public static int MAX_PANO_FRAME = 6;
    private static final int OBJ_DEPTH = 800;
    /* access modifiers changed from: private */
    public static String TAG = "PanoramaCapture";
    public static int mFinalPictureHeight = 0;
    /* access modifiers changed from: private */
    public static float mFinalPictureRatioToCamera = 0.0f;
    public static int mFinalPictureWidth = 0;
    private static boolean mIsSupported = false;
    /* access modifiers changed from: private */
    public static float mPanoPreviewRatioToCamera;
    public static int mPreviewThumbHeight;
    public static int mPreviewThumbWidth;
    private int[] mAargbBuffer;
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    /* access modifiers changed from: private */
    public BitmapArrayOutputStream mBitmapStream;
    /* access modifiers changed from: private */
    public Object mBitmapStreamLock = new Object();
    private Paint mCenterRectPaint = new Paint();
    private String mCompleteSentence;
    private Paint mCompleteSentencePaint;
    /* access modifiers changed from: private */
    public PanoCaptureModule mController;
    /* access modifiers changed from: private */
    public float mCurrDegX = 365.0f;
    /* access modifiers changed from: private */
    public float mCurrDegY = 365.0f;
    private byte[] mDataBuffer;
    /* access modifiers changed from: private */
    public int mDir = 0;
    /* access modifiers changed from: private */
    public int mFinalDoneLength;
    /* access modifiers changed from: private */
    public Picture mGuidePicture;
    private Handler mHandler;
    private String mIntroSentence;
    /* access modifiers changed from: private */
    public boolean mIsFirstBlend;
    /* access modifiers changed from: private */
    public boolean mIsFrameProcessing = false;
    private float[] mOldRots = new float[5];
    /* access modifiers changed from: private */
    public int mOrientation;
    private float[] mOrients = new float[3];
    /* access modifiers changed from: private */
    public PANO_STATUS mPanoStatus = PANO_STATUS.INACTIVE;
    /* access modifiers changed from: private */
    public int mPendingOrientation;
    /* access modifiers changed from: private */
    public Object mPreviewBitmapLock = new Object();
    /* access modifiers changed from: private */
    public Picture mPreviewPicture;
    private ProgressDialog mProgressDialog;
    /* access modifiers changed from: private */
    public String mProgressSentence;
    /* access modifiers changed from: private */
    public PanoQueueProcessor mQueueProcessor;

    /* renamed from: mR */
    private float[] f85mR = new float[9];
    private float[] mRR = new float[9];
    private Sensor mRotationSensor;
    private float[] mRots = new float[5];
    private SensorManager mSensorManager;
    /* access modifiers changed from: private */
    public boolean mShouldFinish = false;
    /* access modifiers changed from: private */
    public Bitmap mTempBitmap;
    /* access modifiers changed from: private */
    public int mTempOrietnation;
    private Matrix matrix = new Matrix();
    /* access modifiers changed from: private */
    public RectF rectF = new RectF();
    private Bitmap tmpBitmap;

    /* renamed from: com.android.camera.ui.PanoCaptureProcessView$BitmapArrayOutputStream */
    class BitmapArrayOutputStream extends ByteArrayOutputStream {
        public BitmapArrayOutputStream(int i) {
            super(i);
        }

        public synchronized byte[] toByteArray() {
            return this.buf;
        }

        public void close() throws IOException {
            super.close();
            this.buf = null;
        }
    }

    /* renamed from: com.android.camera.ui.PanoCaptureProcessView$BitmapTask */
    class BitmapTask {
        Bitmap bitmap;
        int dir;

        /* renamed from: x */
        int f86x;

        /* renamed from: y */
        int f87y;

        public BitmapTask(Bitmap bitmap2, int i, int i2, int i3) {
            Bitmap bitmap3;
            if (PanoCaptureProcessView.this.mOrientation == 0 || PanoCaptureProcessView.this.mOrientation == 180) {
                bitmap3 = Bitmap.createBitmap(PanoCaptureProcessView.mFinalPictureWidth, PanoCaptureProcessView.mFinalPictureHeight, Config.ARGB_8888);
            } else {
                bitmap3 = Bitmap.createBitmap(PanoCaptureProcessView.mFinalPictureHeight, PanoCaptureProcessView.mFinalPictureWidth, Config.ARGB_8888);
            }
            PanoCaptureProcessView.this.rotateAndScale(bitmap2, bitmap3, PanoCaptureProcessView.mFinalPictureRatioToCamera);
            this.bitmap = bitmap3;
            this.f86x = i;
            this.f87y = i2;
            this.dir = i3;
        }

        public void clear() {
            this.bitmap.recycle();
        }
    }

    /* renamed from: com.android.camera.ui.PanoCaptureProcessView$PANO_STATUS */
    enum PANO_STATUS {
        INACTIVE,
        ACTIVE_UNKNOWN,
        ACTIVE_LEFT,
        ACTIVE_RIGHT,
        ACTIVE_UP,
        ACTIVE_DOWN,
        COMPLETING,
        OPENING
    }

    /* renamed from: com.android.camera.ui.PanoCaptureProcessView$PanoQueueProcessor */
    class PanoQueueProcessor extends Thread {
        private Object lock = new Object();
        private ArrayBlockingQueue<BitmapTask> queue = new ArrayBlockingQueue<>(PanoCaptureProcessView.MAX_PANO_FRAME);

        public PanoQueueProcessor() {
        }

        private void waitTillNotFull() {
            do {
            } while (this.queue.size() >= PanoCaptureProcessView.MAX_PANO_FRAME);
        }

        /* JADX WARNING: Can't wrap try/catch for region: R(4:0|1|(2:3|15)(4:4|13|9|16)|14) */
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0019, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            throw r0;
         */
        /* JADX WARNING: Missing exception handler attribute for start block: B:0:0x0000 */
        /* JADX WARNING: Removed duplicated region for block: B:0:0x0000 A[LOOP:0: B:0:0x0000->B:14:0x0000, LOOP_START, SYNTHETIC, Splitter:B:0:0x0000] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r2 = this;
            L_0x0000:
                java.util.concurrent.ArrayBlockingQueue<com.android.camera.ui.PanoCaptureProcessView$BitmapTask> r0 = r2.queue     // Catch:{ InterruptedException -> 0x0000 }
                java.lang.Object r0 = r0.take()     // Catch:{ InterruptedException -> 0x0000 }
                com.android.camera.ui.PanoCaptureProcessView$BitmapTask r0 = (com.android.camera.p004ui.PanoCaptureProcessView.BitmapTask) r0     // Catch:{ InterruptedException -> 0x0000 }
                com.android.camera.ui.PanoCaptureProcessView r1 = com.android.camera.p004ui.PanoCaptureProcessView.this     // Catch:{ InterruptedException -> 0x0000 }
                boolean r1 = r1.mShouldFinish     // Catch:{ InterruptedException -> 0x0000 }
                if (r1 == 0) goto L_0x0011
                goto L_0x0000
            L_0x0011:
                java.lang.Object r1 = r2.lock     // Catch:{ InterruptedException -> 0x0000 }
                monitor-enter(r1)     // Catch:{ InterruptedException -> 0x0000 }
                r2.doTask(r0)     // Catch:{ all -> 0x0019 }
                monitor-exit(r1)     // Catch:{ all -> 0x0019 }
                goto L_0x0000
            L_0x0019:
                r0 = move-exception
                monitor-exit(r1)     // Catch:{ all -> 0x0019 }
                throw r0     // Catch:{ InterruptedException -> 0x0000 }
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.p004ui.PanoCaptureProcessView.PanoQueueProcessor.run():void");
        }

        public boolean isEmpty() {
            synchronized (this.lock) {
                if (!this.queue.isEmpty()) {
                    return false;
                }
                return true;
            }
        }

        public void queueClear() {
            interrupt();
            this.queue.clear();
        }

        public void addTask(Bitmap bitmap, int i, int i2, int i3) {
            waitTillNotFull();
            BitmapTask bitmapTask = new BitmapTask(bitmap, i, i2, i3);
            this.queue.add(bitmapTask);
        }

        private void doTask(BitmapTask bitmapTask) {
            int access$900;
            synchronized (PanoCaptureProcessView.this.mBitmapStreamLock) {
                if (PanoCaptureProcessView.this.mBitmapStream == null) {
                    PanoCaptureProcessView.this.mBitmapStream = new BitmapArrayOutputStream(1232896);
                }
                PanoCaptureProcessView.this.mBitmapStream.reset();
                bitmapTask.bitmap.compress(CompressFormat.JPEG, 100, PanoCaptureProcessView.this.mBitmapStream);
                access$900 = PanoCaptureProcessView.this.callNativeProcessKeyFrame(PanoCaptureProcessView.this.mBitmapStream.toByteArray(), PanoCaptureProcessView.this.mBitmapStream.size(), bitmapTask.f86x, bitmapTask.f87y, 0, bitmapTask.dir);
            }
            if (access$900 < 0) {
                PanoCaptureProcessView.this.mShouldFinish = true;
                PanoCaptureProcessView panoCaptureProcessView = PanoCaptureProcessView.this;
                panoCaptureProcessView.stopPano(false, panoCaptureProcessView.mActivity.getResources().getString(C0905R.string.panocapture_direction_is_changed));
                String access$1200 = PanoCaptureProcessView.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("Keyframe return value: ");
                sb.append(access$900);
                Log.w(access$1200, sb.toString());
            }
            bitmapTask.clear();
        }
    }

    /* renamed from: com.android.camera.ui.PanoCaptureProcessView$Picture */
    class Picture {
        Bitmap bitmap;
        Bitmap bitmapIn;
        int height;
        int leftIn;
        Matrix mat;
        Paint paintFrameEdge = new Paint();
        Paint paintInAir = new Paint();
        float[] pts;

        /* renamed from: rF */
        RectF f88rF;
        int topIn;
        int width;
        float xDeg;
        int xPos;
        float yDeg;
        int yPos;

        public Picture(Bitmap bitmap2, float f, float f2, int i, int i2) {
            init(bitmap2, f, f2, i, i2);
        }

        public Picture(Bitmap bitmap2, float f, float f2, int i, int i2, int i3, int i4) {
            init(bitmap2, f, f2, i, i2);
            this.width = i3;
            this.height = i4;
        }

        private void init(Bitmap bitmap2, float f, float f2, int i, int i2) {
            this.bitmap = bitmap2;
            this.xDeg = f;
            this.yDeg = f2;
            this.xPos = i;
            this.yPos = i2;
            this.mat = new Matrix();
            this.f88rF = new RectF();
            this.pts = new float[8];
            this.paintInAir.setAlpha(124);
            if (bitmap2 != null) {
                this.width = bitmap2.getWidth();
                this.height = bitmap2.getHeight();
            }
            this.paintFrameEdge.setColor(-1);
            this.paintFrameEdge.setStrokeWidth(2.0f);
            this.paintFrameEdge.setStyle(Style.STROKE);
        }

        public void drawPictureInAir(Canvas canvas) {
            float access$000 = ((this.xDeg - PanoCaptureProcessView.this.mCurrDegX) + 360.0f) % 360.0f;
            if (90.0f > access$000 || access$000 > 270.0f) {
                float sin = ((float) Math.sin(Math.toRadians((double) access$000))) * 800.0f;
                float access$100 = ((this.yDeg - PanoCaptureProcessView.this.mCurrDegY) + 360.0f) % 360.0f;
                if (90.0f > access$100 || access$100 > 270.0f) {
                    float sin2 = ((float) Math.sin(Math.toRadians((double) access$100))) * 800.0f;
                    this.f88rF.left = (((float) (canvas.getWidth() / 2)) + sin) - ((float) (this.bitmap.getWidth() / 2));
                    this.f88rF.right = ((float) (canvas.getWidth() / 2)) + sin + ((float) (this.bitmap.getWidth() / 2));
                    this.f88rF.top = (((float) (canvas.getHeight() / 2)) + sin2) - ((float) (this.bitmap.getHeight() / 2));
                    this.f88rF.bottom = ((float) (canvas.getHeight() / 2)) + sin2 + ((float) (this.bitmap.getHeight() / 2));
                    skew(this.f88rF, this.pts, sin, sin2, (float) (canvas.getWidth() / 2), (float) (canvas.getHeight() / 2));
                    this.mat.reset();
                    Matrix matrix = this.mat;
                    RectF rectF = this.f88rF;
                    float f = rectF.left;
                    float f2 = rectF.top;
                    float f3 = rectF.right;
                    float f4 = rectF.bottom;
                    matrix.setPolyToPoly(new float[]{f, f2, f3, f2, f3, f4, f, f4}, 0, this.pts, 0, 4);
                    RectF rectF2 = this.f88rF;
                    canvas.translate(rectF2.left, rectF2.top);
                    canvas.drawBitmap(this.bitmap, this.mat, this.paintInAir);
                }
            }
        }

        public void drawGuideInAir(Canvas canvas) {
            float access$000 = ((this.xDeg - PanoCaptureProcessView.this.mCurrDegX) + 360.0f) % 360.0f;
            if (90.0f > access$000 || access$000 > 270.0f) {
                float sin = ((float) Math.sin(Math.toRadians((double) access$000))) * 800.0f;
                float access$100 = ((this.yDeg - PanoCaptureProcessView.this.mCurrDegY) + 360.0f) % 360.0f;
                if (90.0f > access$100 || access$100 > 270.0f) {
                    float sin2 = ((float) Math.sin(Math.toRadians((double) access$100))) * 800.0f;
                    this.f88rF.left = (((float) (canvas.getWidth() / 2)) + sin) - ((float) this.width);
                    this.f88rF.right = ((float) (canvas.getWidth() / 2)) + sin + ((float) this.width);
                    this.f88rF.top = (((float) (canvas.getHeight() / 2)) + sin2) - ((float) this.height);
                    this.f88rF.bottom = ((float) (canvas.getHeight() / 2)) + sin2 + ((float) this.height);
                    skew(this.f88rF, this.pts, sin, sin2, (float) (canvas.getWidth() / 2), (float) (canvas.getHeight() / 2));
                    for (int i = 1; i < 4; i++) {
                        float[] fArr = this.pts;
                        int i2 = (i - 1) * 2;
                        float f = fArr[i2];
                        float f2 = fArr[i2 + 1];
                        int i3 = i * 2;
                        canvas.drawLine(f, f2, fArr[i3], fArr[i3 + 1], this.paintFrameEdge);
                    }
                    float[] fArr2 = this.pts;
                    canvas.drawLine(fArr2[0], fArr2[1], fArr2[6], fArr2[7], this.paintFrameEdge);
                }
            }
        }

        public void drawMasterPanoPreview(Canvas canvas) {
            if (PanoCaptureProcessView.this.mPanoStatus == PANO_STATUS.ACTIVE_LEFT || PanoCaptureProcessView.this.mPanoStatus == PANO_STATUS.ACTIVE_RIGHT) {
                int width2 = PanoCaptureProcessView.this.mPreviewPicture.bitmap.getWidth();
                int height2 = PanoCaptureProcessView.this.mPreviewPicture.bitmap.getHeight();
                int i = width2 / 2;
                PanoCaptureProcessView.this.rectF.left = (float) ((canvas.getWidth() / 2) - i);
                PanoCaptureProcessView.this.rectF.right = (float) ((canvas.getWidth() / 2) + i);
                PanoCaptureProcessView.this.rectF.top = (float) (((canvas.getHeight() * 4) / 5) - height2);
                PanoCaptureProcessView.this.rectF.bottom = (float) ((canvas.getHeight() * 4) / 5);
                canvas.drawBitmap(PanoCaptureProcessView.this.mPreviewPicture.bitmap, null, PanoCaptureProcessView.this.rectF, null);
                canvas.drawRect(PanoCaptureProcessView.this.rectF, this.paintFrameEdge);
            } else if (PanoCaptureProcessView.this.mPanoStatus == PANO_STATUS.ACTIVE_UP || PanoCaptureProcessView.this.mPanoStatus == PANO_STATUS.ACTIVE_DOWN) {
                int width3 = PanoCaptureProcessView.this.mPreviewPicture.bitmap.getWidth();
                int height3 = PanoCaptureProcessView.this.mPreviewPicture.bitmap.getHeight();
                int i2 = width3 / 2;
                PanoCaptureProcessView.this.rectF.left = (float) ((canvas.getWidth() / 4) - i2);
                PanoCaptureProcessView.this.rectF.right = (float) ((canvas.getWidth() / 4) + i2);
                int i3 = height3 / 2;
                PanoCaptureProcessView.this.rectF.top = (float) ((canvas.getHeight() / 2) - i3);
                PanoCaptureProcessView.this.rectF.bottom = (float) ((canvas.getHeight() / 2) + i3);
                canvas.drawBitmap(PanoCaptureProcessView.this.mPreviewPicture.bitmap, null, PanoCaptureProcessView.this.rectF, null);
                canvas.drawRect(PanoCaptureProcessView.this.rectF, this.paintFrameEdge);
            }
            if (PanoCaptureProcessView.this.mOrientation == 0 || PanoCaptureProcessView.this.mOrientation == 180) {
                RectF access$400 = PanoCaptureProcessView.this.rectF;
                access$400.left += (float) this.leftIn;
                PanoCaptureProcessView.this.rectF.right = PanoCaptureProcessView.this.rectF.left + ((float) PanoCaptureProcessView.mPreviewThumbWidth);
                RectF access$4002 = PanoCaptureProcessView.this.rectF;
                access$4002.top += (float) this.topIn;
                PanoCaptureProcessView.this.rectF.bottom = PanoCaptureProcessView.this.rectF.top + ((float) PanoCaptureProcessView.mPreviewThumbHeight);
            } else {
                RectF access$4003 = PanoCaptureProcessView.this.rectF;
                access$4003.left += (float) this.leftIn;
                PanoCaptureProcessView.this.rectF.right = PanoCaptureProcessView.this.rectF.left + ((float) PanoCaptureProcessView.mPreviewThumbHeight);
                RectF access$4004 = PanoCaptureProcessView.this.rectF;
                access$4004.top += (float) this.topIn;
                PanoCaptureProcessView.this.rectF.bottom = PanoCaptureProcessView.this.rectF.top + ((float) PanoCaptureProcessView.mPreviewThumbWidth);
            }
            canvas.drawBitmap(PanoCaptureProcessView.this.mPreviewPicture.bitmapIn, null, PanoCaptureProcessView.this.rectF, null);
            canvas.drawRect(PanoCaptureProcessView.this.rectF, this.paintFrameEdge);
        }

        private void skew(RectF rectF, float[] fArr, float f, float f2, float f3, float f4) {
            float f5;
            float f6;
            float height2 = rectF.height();
            float width2 = rectF.width();
            if (f < 0.0f) {
                float f7 = ((((-f) / f3) / 2.0f) + 1.0f) * height2;
                f5 = height2;
                height2 = f7;
            } else {
                f5 = (((f / f3) / 2.0f) + 1.0f) * height2;
            }
            if (f2 < 0.0f) {
                f6 = ((((-f2) / f4) / 2.0f) + 1.0f) * width2;
            } else {
                float f8 = width2;
                width2 = (((f2 / f4) / 2.0f) + 1.0f) * width2;
                f6 = f8;
            }
            float f9 = f6 / 2.0f;
            fArr[0] = rectF.centerX() - f9;
            float f10 = height2 / 2.0f;
            fArr[1] = rectF.centerY() - f10;
            fArr[2] = rectF.centerX() + f9;
            float f11 = f5 / 2.0f;
            fArr[3] = rectF.centerY() - f11;
            float f12 = width2 / 2.0f;
            fArr[4] = rectF.centerX() + f12;
            fArr[5] = rectF.centerY() + f11;
            fArr[6] = rectF.centerX() - f12;
            fArr[7] = rectF.centerY() + f10;
        }
    }

    private native int nativeCancelPanorama();

    private native int nativeCompletePanorama(byte[] bArr, int i);

    private native int nativeGetResultSize();

    private native int nativeInstanceInit(int i, int i2, int i3, int i4, int i5);

    private native int nativeInstanceRelease();

    private native int nativeProcessKeyFrame(byte[] bArr, int i, int i2, int i3, int i4, int i5);

    private native int nativeProcessPreviewFrame(byte[] bArr, boolean[] zArr, int[] iArr, int[] iArr2);

    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return true;
    }

    public void setContext(CameraActivity cameraActivity, PanoCaptureModule panoCaptureModule) {
        this.mActivity = cameraActivity;
        this.mController = panoCaptureModule;
        this.mSensorManager = (SensorManager) this.mActivity.getSystemService("sensor");
        this.mRotationSensor = this.mSensorManager.getDefaultSensor(11);
        this.mCenterRectPaint.setColor(-16711681);
        this.mCenterRectPaint.setStrokeWidth(2.0f);
        this.mCenterRectPaint.setStyle(Style.STROKE);
        this.mCompleteSentencePaint.setColor(-1);
        this.mCompleteSentencePaint.setTextSize(45.0f);
        this.mQueueProcessor = new PanoQueueProcessor();
        this.mQueueProcessor.start();
        this.mHandler = new Handler();
        this.mIntroSentence = this.mActivity.getResources().getString(C0905R.string.panocapture_intro);
    }

    public void onPause() {
        this.mSensorManager.unregisterListener(this, this.mRotationSensor);
        synchronized (this.mBitmapStreamLock) {
            if (this.mBitmapStream != null) {
                try {
                    this.mBitmapStream.close();
                } catch (IOException unused) {
                }
                this.mBitmapStream = null;
            }
        }
    }

    public void onResume() {
        this.mSensorManager.registerListener(this, this.mRotationSensor, 3);
    }

    public void setPanoPreviewSize(int i, int i2, int i3, int i4) {
        int i5 = MAX_PANO_FRAME;
        mPreviewThumbWidth = ((i / (i5 + 2)) / 2) * 2;
        mPreviewThumbHeight = ((i2 / (i5 + 2)) / 2) * 2;
        mFinalPictureWidth = (i / 2) * 2;
        mFinalPictureHeight = (i2 / 2) * 2;
        int i6 = mPreviewThumbWidth;
        int i7 = mPreviewThumbHeight;
        this.mAargbBuffer = new int[(i6 * i7)];
        this.mDataBuffer = new byte[(((i6 * i7) * 3) / 2)];
        DECISION_MARGIN = (int) (((double) i7) * 0.2d);
        mPanoPreviewRatioToCamera = ((float) Math.min(i6, i7)) / ((float) Math.min(i3, i4));
        mFinalPictureRatioToCamera = ((float) Math.min(mFinalPictureWidth, mFinalPictureHeight)) / ((float) Math.min(i3, i4));
    }

    public PanoCaptureProcessView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        String str = BuildConfig.FLAVOR;
        this.mCompleteSentence = str;
        this.mProgressSentence = str;
        this.mIntroSentence = str;
        this.mCompleteSentencePaint = new Paint();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int i = this.mOrientation;
        if (i == 0 || i == 180) {
            this.rectF.left = (float) ((canvas.getWidth() / 2) - mPreviewThumbWidth);
            this.rectF.right = (float) ((canvas.getWidth() / 2) + mPreviewThumbWidth);
            this.rectF.top = (float) ((canvas.getHeight() / 2) - mPreviewThumbHeight);
            this.rectF.bottom = (float) ((canvas.getHeight() / 2) + mPreviewThumbHeight);
        } else {
            this.rectF.left = (float) ((canvas.getWidth() / 2) - mPreviewThumbHeight);
            this.rectF.right = (float) ((canvas.getWidth() / 2) + mPreviewThumbHeight);
            this.rectF.top = (float) ((canvas.getHeight() / 2) - mPreviewThumbWidth);
            this.rectF.bottom = (float) ((canvas.getHeight() / 2) + mPreviewThumbWidth);
        }
        if (this.mPanoStatus != PANO_STATUS.INACTIVE) {
            canvas.rotate((float) (-this.mOrientation), (float) (canvas.getWidth() / 2), (float) (canvas.getHeight() / 2));
            if (!this.mProgressSentence.equals(BuildConfig.FLAVOR)) {
                canvas.drawText(this.mProgressSentence, this.rectF.centerX() - ((float) (((int) this.mCompleteSentencePaint.measureText(this.mProgressSentence)) / 2)), (float) ((canvas.getHeight() * 4) / 5), this.mCompleteSentencePaint);
            }
            if (this.mPanoStatus == PANO_STATUS.COMPLETING) {
                canvas.drawText(this.mCompleteSentence, this.rectF.centerX() - ((float) (((int) this.mCompleteSentencePaint.measureText(this.mCompleteSentence)) / 2)), this.rectF.centerY(), this.mCompleteSentencePaint);
                return;
            }
            canvas.drawRect(this.rectF, this.mCenterRectPaint);
            if (this.mGuidePicture != null) {
                canvas.save();
                this.mGuidePicture.drawGuideInAir(canvas);
                canvas.restore();
            }
            synchronized (this.mPreviewBitmapLock) {
                if (this.mPreviewPicture != null) {
                    this.mPreviewPicture.drawMasterPanoPreview(canvas);
                }
            }
            return;
        }
        canvas.rotate((float) (-this.mPendingOrientation), (float) (canvas.getWidth() / 2), (float) (canvas.getHeight() / 2));
        canvas.drawText(this.mIntroSentence, this.rectF.centerX() - ((float) (((int) this.mCompleteSentencePaint.measureText(this.mIntroSentence)) / 2)), (float) ((canvas.getHeight() * 4) / 5), this.mCompleteSentencePaint);
    }

    /* access modifiers changed from: private */
    public void bitmapToDataNV21(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int i = width * height;
        int[] iArr = this.mAargbBuffer;
        if (iArr.length >= i) {
            bitmap.getPixels(iArr, 0, width, 0, 0, width, height);
            int i2 = 0;
            int i3 = 0;
            int i4 = 0;
            while (i2 < height) {
                int i5 = i3;
                int i6 = 0;
                while (i6 < width) {
                    int[] iArr2 = this.mAargbBuffer;
                    int i7 = iArr2[i4];
                    int i8 = (iArr2[i4] & 16711680) >> 16;
                    int i9 = (iArr2[i4] & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8;
                    int i10 = 255;
                    int i11 = (iArr2[i4] & 255) >> 0;
                    int i12 = (((((i8 * 66) + (i9 * Const.CODE_C1_CW1)) + (i11 * 25)) + 128) >> 8) + 16;
                    int i13 = (((((i8 * -38) - (i9 * 74)) + (i11 * 112)) + 128) >> 8) + 128;
                    int i14 = (((((i8 * 112) - (i9 * 94)) - (i11 * 18)) + 128) >> 8) + 128;
                    byte[] bArr = this.mDataBuffer;
                    int i15 = i5 + 1;
                    if (i12 < 0) {
                        i12 = 0;
                    } else if (i12 > 255) {
                        i12 = 255;
                    }
                    bArr[i5] = (byte) i12;
                    if (i2 % 2 == 0 && i4 % 2 == 0) {
                        byte[] bArr2 = this.mDataBuffer;
                        int i16 = i + 1;
                        if (i14 < 0) {
                            i14 = 0;
                        } else if (i14 > 255) {
                            i14 = 255;
                        }
                        bArr2[i] = (byte) i14;
                        byte[] bArr3 = this.mDataBuffer;
                        i = i16 + 1;
                        if (i13 < 0) {
                            i10 = 0;
                        } else if (i13 <= 255) {
                            i10 = i13;
                        }
                        bArr3[i16] = (byte) i10;
                    }
                    i4++;
                    i6++;
                    i5 = i15;
                }
                i2++;
                i3 = i5;
            }
        }
    }

    /* access modifiers changed from: private */
    public void waitForQueueDone() {
        while (!this.mQueueProcessor.isEmpty()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException unused) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void processPreviewFrame(boolean[] zArr, int[] iArr, int[] iArr2) {
        if (callNativeProcessPreviewFrame(this.mDataBuffer, zArr, iArr, iArr2) < 0) {
            Log.e(TAG, "Preview processing is failed.");
        }
    }

    public boolean isPanoCompleting() {
        return this.mPanoStatus == PANO_STATUS.COMPLETING;
    }

    public boolean isFrameProcessing() {
        return this.mIsFrameProcessing;
    }

    public void onFrameAvailable(final Bitmap bitmap, final boolean z) {
        PANO_STATUS pano_status = this.mPanoStatus;
        if (!(pano_status == PANO_STATUS.COMPLETING || pano_status == PANO_STATUS.OPENING)) {
            if (bitmap == null) {
                if (z) {
                    this.mCompleteSentence = "Cancelling...";
                } else {
                    this.mCompleteSentence = "Processing...";
                }
                this.mPanoStatus = PANO_STATUS.COMPLETING;
                invalidate();
                this.mHandler.post(new Runnable() {
                    public void run() {
                        if (PanoCaptureProcessView.this.mPreviewPicture != null) {
                            PanoCaptureProcessView.this.waitForQueueDone();
                            if (!z) {
                                int access$1600 = PanoCaptureProcessView.this.callNativeGetResultSize();
                                if (access$1600 <= 0) {
                                    PanoCaptureProcessView.this.callNativeCancelPanorama();
                                } else {
                                    byte[] bArr = new byte[access$1600];
                                    PanoCaptureProcessView.this.callNativeCompletePanorama(bArr, access$1600);
                                    int i = 270;
                                    if (PanoCaptureProcessView.this.mDir == 1) {
                                        i = 0;
                                    }
                                    final Bitmap rotate = CameraUtil.rotate(BitmapFactory.decodeByteArray(bArr, 0, bArr.length), i);
                                    final Uri savePanorama = PanoCaptureProcessView.this.mController.savePanorama(bArr, rotate.getWidth(), rotate.getHeight(), i);
                                    if (savePanorama != null) {
                                        PanoCaptureProcessView.this.mActivity.runOnUiThread(new Runnable() {
                                            public void run() {
                                                PanoCaptureProcessView.this.mActivity.updateThumbnail(rotate);
                                                PanoCaptureProcessView.this.mActivity.notifyNewMedia(savePanorama);
                                            }
                                        });
                                    } else {
                                        String access$1200 = PanoCaptureProcessView.TAG;
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("Image uri is null, size : ");
                                        sb.append(access$1600);
                                        sb.append(" jpegData: ");
                                        sb.append(bArr);
                                        Log.d(access$1200, sb.toString());
                                    }
                                }
                            } else {
                                PanoCaptureProcessView.this.callNativeCancelPanorama();
                                PanoCaptureProcessView.this.mQueueProcessor.queueClear();
                            }
                        } else {
                            PanoCaptureProcessView.this.callNativeCancelPanorama();
                        }
                        synchronized (PanoCaptureProcessView.this.mPreviewBitmapLock) {
                            if (PanoCaptureProcessView.this.mPreviewPicture != null) {
                                PanoCaptureProcessView.this.mPreviewPicture.bitmap.recycle();
                                PanoCaptureProcessView.this.mPreviewPicture.bitmapIn.recycle();
                            }
                            PanoCaptureProcessView.this.mPreviewPicture = null;
                            PanoCaptureProcessView.this.mGuidePicture = null;
                        }
                        PanoCaptureProcessView.this.callNativeInstanceRelease();
                        PanoCaptureProcessView.this.mPanoStatus = PANO_STATUS.INACTIVE;
                        PanoCaptureProcessView.this.mShouldFinish = false;
                        PanoCaptureProcessView panoCaptureProcessView = PanoCaptureProcessView.this;
                        panoCaptureProcessView.mOrientation = panoCaptureProcessView.mPendingOrientation;
                    }
                });
                return;
            }
            PANO_STATUS pano_status2 = PANO_STATUS.INACTIVE;
            if (pano_status == pano_status2) {
                this.mPanoStatus = PANO_STATUS.OPENING;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        int i;
                        int i2;
                        if (PanoCaptureProcessView.this.mOrientation == 0 || PanoCaptureProcessView.this.mOrientation == 180) {
                            i2 = PanoCaptureProcessView.mPreviewThumbWidth;
                            i = PanoCaptureProcessView.mPreviewThumbHeight;
                        } else {
                            i2 = PanoCaptureProcessView.mPreviewThumbHeight;
                            i = PanoCaptureProcessView.mPreviewThumbWidth;
                        }
                        int i3 = i2;
                        if (PanoCaptureProcessView.this.callNativeInstanceInit(i3, i, i3, 0, 1) < 0) {
                            Log.e(PanoCaptureProcessView.TAG, "Failed to create panorama native instance");
                            PanoCaptureProcessView.this.mPanoStatus = PANO_STATUS.INACTIVE;
                            PanoCaptureProcessView panoCaptureProcessView = PanoCaptureProcessView.this;
                            panoCaptureProcessView.mOrientation = panoCaptureProcessView.mPendingOrientation;
                            return;
                        }
                        PanoCaptureProcessView.this.mPanoStatus = PANO_STATUS.ACTIVE_UNKNOWN;
                    }
                });
            } else if (pano_status != pano_status2 && !this.mIsFrameProcessing) {
                this.mIsFrameProcessing = true;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Bitmap bitmap;
                        Bitmap bitmap2;
                        if (PanoCaptureProcessView.this.mTempBitmap == null || PanoCaptureProcessView.this.mTempOrietnation != PanoCaptureProcessView.this.mOrientation) {
                            if (PanoCaptureProcessView.this.mOrientation == 0 || PanoCaptureProcessView.this.mOrientation == 180) {
                                PanoCaptureProcessView.this.mTempBitmap = Bitmap.createBitmap(PanoCaptureProcessView.mPreviewThumbWidth, PanoCaptureProcessView.mPreviewThumbHeight, Config.ARGB_8888);
                            } else {
                                PanoCaptureProcessView.this.mTempBitmap = Bitmap.createBitmap(PanoCaptureProcessView.mPreviewThumbHeight, PanoCaptureProcessView.mPreviewThumbWidth, Config.ARGB_8888);
                            }
                            PanoCaptureProcessView panoCaptureProcessView = PanoCaptureProcessView.this;
                            panoCaptureProcessView.mTempOrietnation = panoCaptureProcessView.mOrientation;
                        }
                        PanoCaptureProcessView panoCaptureProcessView2 = PanoCaptureProcessView.this;
                        panoCaptureProcessView2.rotateAndScale(bitmap, panoCaptureProcessView2.mTempBitmap, PanoCaptureProcessView.mPanoPreviewRatioToCamera);
                        PanoCaptureProcessView panoCaptureProcessView3 = PanoCaptureProcessView.this;
                        panoCaptureProcessView3.bitmapToDataNV21(panoCaptureProcessView3.mTempBitmap);
                        boolean[] zArr = new boolean[1];
                        int[] iArr = new int[3];
                        PanoCaptureProcessView.this.processPreviewFrame(zArr, iArr, new int[1]);
                        if (iArr[2] == -1) {
                            PanoCaptureProcessView panoCaptureProcessView4 = PanoCaptureProcessView.this;
                            panoCaptureProcessView4.mProgressSentence = panoCaptureProcessView4.mActivity.getResources().getString(C0905R.string.panocapture_direction_is_not_determined);
                        } else {
                            PanoCaptureProcessView.this.mProgressSentence = BuildConfig.FLAVOR;
                            PanoCaptureProcessView.this.mDir = iArr[2];
                        }
                        if (zArr[0]) {
                            PanoCaptureProcessView.this.mQueueProcessor.addTask(bitmap, iArr[0], iArr[1], iArr[2]);
                        }
                        PanoCaptureProcessView panoCaptureProcessView5 = PanoCaptureProcessView.this;
                        Picture picture = new Picture(panoCaptureProcessView5.mTempBitmap, PanoCaptureProcessView.this.mCurrDegX, PanoCaptureProcessView.this.mCurrDegY, iArr[0], iArr[1]);
                        if (PanoCaptureProcessView.this.mPanoStatus == PANO_STATUS.ACTIVE_UNKNOWN) {
                            if (iArr[0] < (-PanoCaptureProcessView.DECISION_MARGIN)) {
                                PanoCaptureProcessView.this.mPanoStatus = PANO_STATUS.ACTIVE_RIGHT;
                            } else if (iArr[0] > PanoCaptureProcessView.DECISION_MARGIN) {
                                PanoCaptureProcessView.this.mPanoStatus = PANO_STATUS.ACTIVE_LEFT;
                            } else if (iArr[1] < (-PanoCaptureProcessView.DECISION_MARGIN)) {
                                PanoCaptureProcessView.this.mPanoStatus = PANO_STATUS.ACTIVE_DOWN;
                            } else if (iArr[1] > PanoCaptureProcessView.DECISION_MARGIN) {
                                PanoCaptureProcessView.this.mPanoStatus = PANO_STATUS.ACTIVE_UP;
                            }
                        }
                        if (PanoCaptureProcessView.this.mPreviewPicture == null && PanoCaptureProcessView.this.mPanoStatus != PANO_STATUS.ACTIVE_UNKNOWN) {
                            if (PanoCaptureProcessView.this.mPanoStatus == PANO_STATUS.ACTIVE_RIGHT || PanoCaptureProcessView.this.mPanoStatus == PANO_STATUS.ACTIVE_LEFT) {
                                if (PanoCaptureProcessView.this.mOrientation == 0 || PanoCaptureProcessView.this.mOrientation == 180) {
                                    PanoCaptureProcessView.this.mFinalDoneLength = PanoCaptureProcessView.mPreviewThumbWidth * PanoCaptureProcessView.MAX_PANO_FRAME;
                                    bitmap2 = Bitmap.createBitmap(PanoCaptureProcessView.this.mFinalDoneLength, PanoCaptureProcessView.mPreviewThumbHeight, Config.ARGB_8888);
                                    bitmap = Bitmap.createBitmap(PanoCaptureProcessView.mPreviewThumbWidth, PanoCaptureProcessView.mPreviewThumbHeight, Config.ARGB_8888);
                                } else {
                                    PanoCaptureProcessView.this.mFinalDoneLength = PanoCaptureProcessView.mPreviewThumbHeight * PanoCaptureProcessView.MAX_PANO_FRAME;
                                    bitmap2 = Bitmap.createBitmap(PanoCaptureProcessView.this.mFinalDoneLength, PanoCaptureProcessView.mPreviewThumbWidth, Config.ARGB_8888);
                                    bitmap = Bitmap.createBitmap(PanoCaptureProcessView.mPreviewThumbHeight, PanoCaptureProcessView.mPreviewThumbWidth, Config.ARGB_8888);
                                }
                            } else if (PanoCaptureProcessView.this.mOrientation == 0 || PanoCaptureProcessView.this.mOrientation == 180) {
                                PanoCaptureProcessView.this.mFinalDoneLength = PanoCaptureProcessView.mPreviewThumbHeight * PanoCaptureProcessView.MAX_PANO_FRAME;
                                bitmap2 = Bitmap.createBitmap(PanoCaptureProcessView.mPreviewThumbWidth, PanoCaptureProcessView.this.mFinalDoneLength, Config.ARGB_8888);
                                bitmap = Bitmap.createBitmap(PanoCaptureProcessView.mPreviewThumbWidth, PanoCaptureProcessView.mPreviewThumbHeight, Config.ARGB_8888);
                            } else {
                                PanoCaptureProcessView.this.mFinalDoneLength = PanoCaptureProcessView.mPreviewThumbWidth * PanoCaptureProcessView.MAX_PANO_FRAME;
                                bitmap2 = Bitmap.createBitmap(PanoCaptureProcessView.mPreviewThumbHeight, PanoCaptureProcessView.this.mFinalDoneLength, Config.ARGB_8888);
                                bitmap = Bitmap.createBitmap(PanoCaptureProcessView.mPreviewThumbHeight, PanoCaptureProcessView.mPreviewThumbWidth, Config.ARGB_8888);
                            }
                            Bitmap bitmap3 = bitmap2;
                            PanoCaptureProcessView panoCaptureProcessView6 = PanoCaptureProcessView.this;
                            Picture picture2 = new Picture(null, panoCaptureProcessView6.mCurrDegX, PanoCaptureProcessView.this.mCurrDegY, 0, 0, bitmap.getWidth(), bitmap.getHeight());
                            panoCaptureProcessView6.mGuidePicture = picture2;
                            PanoCaptureProcessView panoCaptureProcessView7 = PanoCaptureProcessView.this;
                            Picture picture3 = new Picture(bitmap3, panoCaptureProcessView7.mCurrDegX, PanoCaptureProcessView.this.mCurrDegY, 0, 0, 0, 0);
                            synchronized (PanoCaptureProcessView.this.mPreviewBitmapLock) {
                                PanoCaptureProcessView.this.mPreviewPicture = picture3;
                                PanoCaptureProcessView.this.mPreviewPicture.bitmapIn = bitmap;
                            }
                            PanoCaptureProcessView.this.mIsFirstBlend = true;
                        }
                        if (PanoCaptureProcessView.this.mPreviewPicture != null) {
                            PanoCaptureProcessView panoCaptureProcessView8 = PanoCaptureProcessView.this;
                            panoCaptureProcessView8.blendToPreviewPicture(picture, zArr[0], panoCaptureProcessView8.mIsFirstBlend);
                            if (PanoCaptureProcessView.this.isAllTaken()) {
                                PanoCaptureProcessView.this.stopPano(false, null);
                            }
                            PanoCaptureProcessView.this.mIsFirstBlend = false;
                        }
                        PanoCaptureProcessView.this.mIsFrameProcessing = false;
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    public void stopPano(final boolean z, String str) {
        if (str != null) {
            this.mProgressSentence = str;
            Log.w(TAG, str);
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                PanoCaptureProcessView.this.mController.changePanoStatus(false, z);
            }
        });
    }

    /* access modifiers changed from: private */
    public boolean isAllTaken() {
        if (this.mFinalDoneLength == 0) {
            return false;
        }
        PANO_STATUS pano_status = this.mPanoStatus;
        if (pano_status == PANO_STATUS.ACTIVE_LEFT || pano_status == PANO_STATUS.ACTIVE_RIGHT) {
            if (this.mPreviewPicture.width >= this.mFinalDoneLength) {
                return true;
            }
        } else if ((pano_status == PANO_STATUS.ACTIVE_UP || pano_status == PANO_STATUS.ACTIVE_DOWN) && this.mPreviewPicture.height >= this.mFinalDoneLength) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void blendToPreviewPicture(Picture picture, boolean z, boolean z2) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        Picture picture2 = picture;
        new Canvas(this.mPreviewPicture.bitmapIn).drawBitmap(picture2.bitmap, 0.0f, 0.0f, null);
        Picture picture3 = this.mPreviewPicture;
        PANO_STATUS pano_status = this.mPanoStatus;
        int i6 = 0;
        if (pano_status == PANO_STATUS.ACTIVE_RIGHT || pano_status == PANO_STATUS.ACTIVE_LEFT) {
            int i7 = picture2.xPos - picture3.xPos;
            picture3.topIn = -picture2.yPos;
            if ((i7 <= 0 || this.mPanoStatus != PANO_STATUS.ACTIVE_RIGHT) && (i7 >= 0 || this.mPanoStatus != PANO_STATUS.ACTIVE_LEFT)) {
                int abs = picture2.width - Math.abs(i7);
                if (z2) {
                    abs = 0;
                }
                int i8 = picture3.width;
                int i9 = picture2.width;
                int i10 = (i8 + i9) - abs;
                if (this.mPanoStatus == PANO_STATUS.ACTIVE_RIGHT) {
                    picture3.leftIn = i10 - i9;
                } else {
                    picture3.leftIn = picture3.bitmap.getWidth() - i10;
                }
                if (picture3.leftIn < 0) {
                    i = 0;
                    picture3.leftIn = 0;
                } else {
                    i = 0;
                }
                if (picture3.leftIn > picture3.bitmap.getWidth() - picture2.bitmap.getWidth()) {
                    picture3.leftIn = picture3.bitmap.getWidth() - picture2.bitmap.getWidth();
                }
                if (z || z2) {
                    new Canvas(picture3.bitmap).drawBitmap(picture2.bitmap, (float) picture3.leftIn, 0.0f, null);
                    if (this.mPanoStatus == PANO_STATUS.ACTIVE_RIGHT) {
                        int i11 = picture2.width;
                        i2 = i10 - i11;
                        i3 = (i10 - i11) + abs;
                    } else {
                        i2 = ((picture3.bitmap.getWidth() - i10) + picture2.width) - abs;
                        i3 = (picture3.bitmap.getWidth() - i10) + picture2.width;
                    }
                    int i12 = i2;
                    while (i12 < i3 && i12 < picture3.bitmap.getWidth()) {
                        int i13 = i12 - i2;
                        if (i13 >= picture2.bitmap.getWidth()) {
                            break;
                        }
                        int i14 = i;
                        while (i14 < picture3.height && i14 < picture3.bitmap.getHeight() && i14 < picture2.bitmap.getHeight()) {
                            int pixel = picture3.bitmap.getPixel(i12, i14);
                            int pixel2 = picture2.bitmap.getPixel(i13, i14);
                            int i15 = (i3 - i12) / abs;
                            int i16 = 1 - i15;
                            picture3.bitmap.setPixel(i12, i14, Color.argb(255, (Color.red(pixel) * i15) + (Color.red(pixel2) * i16), (Color.green(pixel) * i15) + (Color.green(pixel2) * i16), (i15 * Color.blue(pixel)) + (i16 * Color.blue(pixel2))));
                            i14++;
                        }
                        i12++;
                        i = 0;
                    }
                }
                picture3.width = i10;
            } else {
                return;
            }
        } else {
            int i17 = picture2.yPos - picture3.yPos;
            picture3.leftIn = -picture2.xPos;
            if ((i17 <= 0 || pano_status != PANO_STATUS.ACTIVE_DOWN) && (i17 >= 0 || this.mPanoStatus != PANO_STATUS.ACTIVE_UP)) {
                int abs2 = picture2.height - Math.abs(i17);
                if (z2) {
                    abs2 = 0;
                }
                int i18 = picture3.height;
                int i19 = picture2.height;
                int i20 = (i18 + i19) - abs2;
                if (this.mPanoStatus == PANO_STATUS.ACTIVE_DOWN) {
                    picture3.topIn = i20 - i19;
                } else {
                    picture3.topIn = picture3.bitmap.getHeight() - i20;
                }
                if (picture3.topIn < 0) {
                    picture3.topIn = 0;
                }
                if (picture3.topIn > picture3.bitmap.getHeight() - picture2.bitmap.getHeight()) {
                    picture3.topIn = picture3.bitmap.getHeight() - picture2.bitmap.getHeight();
                }
                if (z || z2) {
                    new Canvas(picture3.bitmap).drawBitmap(picture2.bitmap, 0.0f, (float) picture3.topIn, null);
                    if (this.mPanoStatus == PANO_STATUS.ACTIVE_DOWN) {
                        int i21 = picture2.height;
                        i4 = i20 - i21;
                        i5 = (i20 - i21) + abs2;
                    } else {
                        i4 = ((picture3.bitmap.getHeight() - i20) + picture2.height) - abs2;
                        i5 = (picture3.bitmap.getHeight() - i20) + picture2.height;
                    }
                    int i22 = i4;
                    while (i22 < i5 && i22 < picture3.bitmap.getHeight()) {
                        int i23 = i22 - i4;
                        if (i23 >= picture2.bitmap.getHeight()) {
                            break;
                        }
                        int i24 = i6;
                        while (i24 < picture3.width && i24 < picture3.bitmap.getWidth() && i24 < picture2.bitmap.getWidth()) {
                            int pixel3 = picture3.bitmap.getPixel(i24, i22);
                            int pixel4 = picture2.bitmap.getPixel(i24, i23);
                            int i25 = (i5 - i22) / abs2;
                            int i26 = 1 - i25;
                            picture3.bitmap.setPixel(i24, i22, Color.argb(255, (Color.red(pixel3) * i25) + (Color.red(pixel4) * i26), (Color.green(pixel3) * i25) + (Color.green(pixel4) * i26), (i25 * Color.blue(pixel3)) + (i26 * Color.blue(pixel4))));
                            i24++;
                        }
                        i22++;
                        i6 = 0;
                    }
                }
                picture3.height = i20;
            } else {
                return;
            }
        }
        picture3.xPos = picture2.xPos;
        picture3.yPos = picture2.yPos;
    }

    /* access modifiers changed from: private */
    public void rotateAndScale(Bitmap bitmap, Bitmap bitmap2, float f) {
        Canvas canvas = new Canvas(bitmap2);
        this.matrix.reset();
        int cameraSensorOrientation = this.mController.getCameraSensorOrientation();
        this.matrix.setScale(f, f);
        int i = this.mOrientation;
        float f2 = (float) ((cameraSensorOrientation + i) % 360);
        if (i == 0) {
            if (cameraSensorOrientation == 90) {
                this.matrix.postRotate(f2, (float) (bitmap2.getWidth() / 2), (float) (bitmap2.getWidth() / 2));
            } else if (cameraSensorOrientation == 270) {
                this.matrix.postRotate(f2, (float) (bitmap2.getHeight() / 2), (float) (bitmap2.getHeight() / 2));
            }
        } else if (i == 180) {
            if (cameraSensorOrientation == 90) {
                this.matrix.postRotate(f2, (float) (bitmap2.getHeight() / 2), (float) (bitmap2.getHeight() / 2));
            } else if (cameraSensorOrientation == 270) {
                this.matrix.postRotate(f2, (float) (bitmap2.getWidth() / 2), (float) (bitmap2.getWidth() / 2));
            }
        } else if (i == 270 || i == 90) {
            this.matrix.postRotate(f2, (float) (bitmap2.getWidth() / 2), (float) (bitmap2.getHeight() / 2));
        }
        canvas.drawBitmap(bitmap, this.matrix, null);
    }

    public void setOrientation(int i) {
        if (this.mPanoStatus != PANO_STATUS.INACTIVE) {
            this.mPendingOrientation = i;
            return;
        }
        this.mPendingOrientation = i;
        this.mOrientation = i;
    }

    private boolean isPortrait() {
        int i = this.mOrientation;
        return i == 0 || i == 180;
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == 11) {
            float[] fArr = sensorEvent.values;
            System.arraycopy(fArr, 0, this.mOldRots, 0, fArr.length);
            SensorManager.getRotationMatrixFromVector(this.f85mR, this.mOldRots);
            if (isPortrait()) {
                SensorManager.remapCoordinateSystem(this.f85mR, 1, 3, this.mRR);
            } else {
                SensorManager.remapCoordinateSystem(this.f85mR, 3, 1, this.mRR);
            }
            SensorManager.getOrientation(this.mRR, this.mOrients);
            this.mCurrDegX = (((float) Math.toDegrees((double) this.mOrients[0])) + 360.0f) % 360.0f;
            this.mCurrDegY = (((float) Math.toDegrees((double) this.mOrients[1])) + 360.0f) % 360.0f;
            if (!isPortrait()) {
                this.mCurrDegX = (this.mCurrDegX + 180.0f) % 360.0f;
                this.mCurrDegY = ((-this.mCurrDegY) + 360.0f) % 360.0f;
            }
            invalidate();
        }
    }

    private void lowPassFilteredCopy(float[] fArr, float[] fArr2) {
        for (int i = 0; i < 3; i++) {
            fArr2[i] = fArr2[i] + ((fArr[i] - fArr2[i]) * 0.45f);
        }
    }

    private void highPassFilteredCopy(float[] fArr, float[] fArr2, float[] fArr3, boolean z) {
        if (!z) {
            System.arraycopy(fArr, 0, fArr2, 0, fArr.length);
            return;
        }
        for (int i = 0; i < fArr.length; i++) {
            fArr2[i] = ((fArr2[i] + fArr[i]) - fArr3[i]) * 1.2f;
        }
    }

    /* access modifiers changed from: private */
    public int callNativeInstanceInit(int i, int i2, int i3, int i4, int i5) {
        return nativeInstanceInit(i, i2, i3, i4, i5);
    }

    /* access modifiers changed from: private */
    public int callNativeInstanceRelease() {
        return nativeInstanceRelease();
    }

    private int callNativeProcessPreviewFrame(byte[] bArr, boolean[] zArr, int[] iArr, int[] iArr2) {
        return nativeProcessPreviewFrame(bArr, zArr, iArr, iArr2);
    }

    /* access modifiers changed from: private */
    public int callNativeProcessKeyFrame(byte[] bArr, int i, int i2, int i3, int i4, int i5) {
        return nativeProcessKeyFrame(bArr, i, i2, i3, i4, i5);
    }

    /* access modifiers changed from: private */
    public int callNativeCancelPanorama() {
        return nativeCancelPanorama();
    }

    /* access modifiers changed from: private */
    public int callNativeGetResultSize() {
        return nativeGetResultSize();
    }

    /* access modifiers changed from: private */
    public int callNativeCompletePanorama(byte[] bArr, int i) {
        return nativeCompletePanorama(bArr, i);
    }

    public static boolean isSupportedStatic() {
        return mIsSupported;
    }

    static {
        try {
            System.loadLibrary("jni_panorama");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, e.toString());
        }
    }
}
