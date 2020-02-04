package com.android.camera;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.Keyframe;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.camera.exif.ExifInterface;
import com.android.camera.util.CameraUtil;
import com.asus.scenedetectlib.BuildConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.codeaurora.snapcam.C0905R;

public class RefocusActivity extends Activity {
    public static final int MAP_ROTATED = 1;
    /* access modifiers changed from: private */
    public static final String[] NAMES = {"00", "01", "02", "03", "04", "AllFocusImage"};
    private static final String TAG = "RefocusActivity";
    /* access modifiers changed from: private */
    public View mAllInFocusView;
    private int mCurrentImage = -1;
    /* access modifiers changed from: private */
    public DepthMap mDepthMap;
    /* access modifiers changed from: private */
    public String mFilesPath;
    /* access modifiers changed from: private */
    public int mHeight;
    /* access modifiers changed from: private */
    public ImageView mImageView;
    /* access modifiers changed from: private */
    public Indicator mIndicator;
    private LoadImageTask mLoadImageTask;
    /* access modifiers changed from: private */
    public boolean mMapRotated = false;
    /* access modifiers changed from: private */
    public int mOrientation = 0;
    /* access modifiers changed from: private */
    public int mRequestedImage = -1;
    private boolean mSecureCamera;
    /* access modifiers changed from: private */
    public Uri mUri;
    /* access modifiers changed from: private */
    public int mWidth;

    private class DepthMap {
        private static final int W_SIZE = 61;
        private byte[] mData;
        private boolean mFail = true;
        private int mHeight;
        private int mWidth;

        public DepthMap(String str) {
            File file = new File(str);
            boolean z = false;
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                this.mData = new byte[((int) file.length())];
                fileInputStream.read(this.mData);
                fileInputStream.close();
            } catch (Exception unused) {
                this.mData = new byte[0];
            }
            byte[] bArr = this.mData;
            int length = bArr.length;
            if (length > 25) {
                if (bArr[length - 25] != 0) {
                    z = true;
                }
                this.mFail = z;
                this.mWidth = readInteger(length - 24);
                this.mHeight = readInteger(length - 20);
            }
            if ((this.mWidth * this.mHeight) + 25 > length) {
                this.mFail = true;
            }
        }

        public int getDepth(float f, float f2) {
            float f3;
            int i;
            if (this.mFail || f > 1.0f || f2 > 1.0f) {
                return RefocusActivity.NAMES.length - 1;
            }
            int i2 = (int) (((float) this.mWidth) * f);
            int i3 = (int) (((float) this.mHeight) * f2);
            if (RefocusActivity.this.mMapRotated) {
                if (RefocusActivity.this.mOrientation == 0) {
                    i2 = (int) (((float) this.mWidth) * f);
                    i3 = (int) (((float) this.mHeight) * f2);
                }
                if (RefocusActivity.this.mOrientation == 90) {
                    i2 = (int) (f2 * ((float) this.mWidth));
                    f3 = 1.0f - f;
                    i = this.mHeight;
                } else if (RefocusActivity.this.mOrientation == 180) {
                    i2 = (int) ((1.0f - f) * ((float) this.mWidth));
                    f3 = 1.0f - f2;
                    i = this.mHeight;
                } else if (RefocusActivity.this.mOrientation == 270) {
                    i2 = (int) ((1.0f - f2) * ((float) this.mWidth));
                    i3 = (int) (f * ((float) this.mHeight));
                }
                i3 = (int) (f3 * ((float) i));
            }
            int[] iArr = new int[256];
            for (int i4 = 0; i4 < 256; i4++) {
                iArr[i4] = 0;
            }
            int max = Math.max(i2 - 30, 0);
            int min = Math.min(max + W_SIZE, this.mWidth);
            int max2 = Math.max(i3 - 30, 0);
            int min2 = Math.min(max2 + W_SIZE, this.mHeight);
            while (max < min) {
                for (int i5 = max2; i5 < min2; i5++) {
                    byte b = this.mData[(this.mWidth * i5) + max];
                    iArr[b] = iArr[b] + 1;
                }
                max++;
            }
            int length = RefocusActivity.NAMES.length - 1;
            int i6 = 0;
            for (int i7 = 0; i7 < 256; i7++) {
                int i8 = iArr[i7];
                if (i8 != 0 && (i6 == 0 || i8 > i6)) {
                    length = i7;
                    i6 = i8;
                }
            }
            return length;
        }

        private int readInteger(int i) {
            int i2 = this.mData[i] & 255;
            for (int i3 = 1; i3 < 4; i3++) {
                i2 = (i2 << 8) + (this.mData[i + i3] & 255);
            }
            return i2;
        }
    }

    public static class Indicator extends FrameLayout {
        private ValueAnimator mAnimator;
        private int mColor1;
        private int mColor2;
        private float mCrossLength;
        private Paint mPaint = new Paint(1);
        private float mStrokeWidth;

        /* renamed from: mX */
        private float f75mX;

        /* renamed from: mY */
        private float f76mY;

        public Indicator(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            Resources resources = context.getResources();
            float dimensionPixelSize = (float) (resources.getDimensionPixelSize(C0905R.dimen.refocus_circle_diameter_1) / 2);
            float dimensionPixelSize2 = (float) (resources.getDimensionPixelSize(C0905R.dimen.refocus_circle_diameter_2) / 2);
            float dimensionPixelSize3 = (float) (resources.getDimensionPixelSize(C0905R.dimen.refocus_circle_diameter_3) / 2);
            this.mCrossLength = (float) (resources.getDimensionPixelSize(C0905R.dimen.refocus_cross_length) / 2);
            this.mStrokeWidth = (float) (resources.getDimensionPixelSize(C0905R.dimen.refocus_stroke_width) / 2);
            this.mColor1 = resources.getColor(C0905R.color.refocus_indicator_1);
            this.mColor2 = resources.getColor(C0905R.color.refocus_indicator_2);
            this.mAnimator = ValueAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[]{PropertyValuesHolder.ofKeyframe("radius", new Keyframe[]{Keyframe.ofFloat(0.0f, dimensionPixelSize), Keyframe.ofFloat(0.41666666f, dimensionPixelSize2), Keyframe.ofFloat(0.5f, dimensionPixelSize2), Keyframe.ofFloat(0.75f, dimensionPixelSize3), Keyframe.ofFloat(1.0f, dimensionPixelSize2)})});
            this.mPaint.setStrokeCap(Cap.BUTT);
            this.mPaint.setStrokeWidth((float) resources.getDimensionPixelSize(C0905R.dimen.refocus_stroke_width));
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            ValueAnimator valueAnimator = this.mAnimator;
            if (valueAnimator != null && valueAnimator.isStarted()) {
                this.mPaint.setColor(this.mAnimator.getAnimatedFraction() < 0.5f ? this.mColor1 : this.mColor2);
                this.mPaint.setStyle(Style.STROKE);
                canvas.drawCircle(this.f75mX, this.f76mY, ((Float) this.mAnimator.getAnimatedValue()).floatValue(), this.mPaint);
                float f = this.f75mX;
                float f2 = this.mCrossLength;
                float f3 = f - f2;
                float f4 = this.f76mY;
                canvas.drawLine(f3, f4, f + f2, f4, this.mPaint);
                float f5 = this.f75mX;
                float f6 = this.f76mY;
                float f7 = this.mCrossLength;
                canvas.drawLine(f5, f6 - f7, f5, f6 + f7, this.mPaint);
            }
        }

        public void startAnimation(float f, float f2) {
            this.f75mX = f;
            this.f76mY = f2;
            ValueAnimator valueAnimator = this.mAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            this.mAnimator.setDuration(720);
            this.mAnimator.removeAllUpdateListeners();
            this.mAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Indicator.this.invalidate();
                }
            });
            this.mAnimator.removeAllListeners();
            this.mAnimator.addListener(new AnimatorListener() {
                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                    Indicator.this.setWillNotDraw(false);
                }

                public void onAnimationEnd(Animator animator) {
                    Indicator.this.setWillNotDraw(true);
                }

                public void onAnimationCancel(Animator animator) {
                    Indicator.this.setWillNotDraw(true);
                }
            });
            this.mAnimator.start();
        }
    }

    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private LoadImageTask() {
        }

        /* access modifiers changed from: protected */
        public Bitmap doInBackground(String... strArr) {
            int i;
            int i2;
            Options options = new Options();
            int i3 = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(strArr[0], options);
            ExifInterface exifInterface = new ExifInterface();
            RefocusActivity.this.mOrientation = 0;
            try {
                exifInterface.readExif(strArr[0]);
                RefocusActivity.this.mOrientation = Exif.getOrientation(exifInterface);
            } catch (IOException unused) {
            }
            int i4 = options.outHeight;
            int i5 = options.outWidth;
            if (RefocusActivity.this.getResources().getConfiguration().orientation == 1) {
                i2 = RefocusActivity.this.mWidth;
                i = RefocusActivity.this.mHeight;
            } else {
                i2 = RefocusActivity.this.mHeight;
                i = RefocusActivity.this.mWidth;
            }
            if (i4 > i2 || i5 > i) {
                while ((i4 / i3) / 2 > i2 && (i5 / i3) / 2 > i) {
                    i3 *= 2;
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("sample =  ");
            sb.append(i3);
            String sb2 = sb.toString();
            String str = RefocusActivity.TAG;
            Log.d(str, sb2);
            StringBuilder sb3 = new StringBuilder();
            sb3.append("h = ");
            sb3.append(i4);
            sb3.append("  height = ");
            sb3.append(i2);
            Log.d(str, sb3.toString());
            StringBuilder sb4 = new StringBuilder();
            sb4.append("w = ");
            sb4.append(i5);
            sb4.append("  width = ");
            sb4.append(i);
            Log.d(str, sb4.toString());
            options.inJustDecodeBounds = false;
            options.inSampleSize = i3;
            Bitmap decodeFile = BitmapFactory.decodeFile(strArr[0], options);
            if (RefocusActivity.this.mOrientation == 0) {
                return decodeFile;
            }
            Matrix matrix = new Matrix();
            matrix.setRotate((float) RefocusActivity.this.mOrientation);
            return Bitmap.createBitmap(decodeFile, 0, 0, decodeFile.getWidth(), decodeFile.getHeight(), matrix, false);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
            RefocusActivity.this.mImageView.setImageBitmap(bitmap);
        }
    }

    private class SaveImageTask extends AsyncTask<String, Void, Void> {
        private SaveImageTask() {
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(String... strArr) {
            try {
                OutputStream openOutputStream = RefocusActivity.this.getContentResolver().openOutputStream(RefocusActivity.this.mUri);
                FileInputStream fileInputStream = new FileInputStream(strArr[0]);
                byte[] bArr = new byte[4096];
                while (true) {
                    int read = fileInputStream.read(bArr);
                    if (read <= 0) {
                        break;
                    }
                    openOutputStream.write(bArr, 0, read);
                }
                fileInputStream.close();
                openOutputStream.close();
            } catch (Exception unused) {
            }
            RefocusActivity.this.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", RefocusActivity.this.mUri));
            return null;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void voidR) {
            RefocusActivity.this.finish();
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        this.mUri = intent.getData();
        String action = intent.getAction();
        if (CameraUtil.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action) || "android.media.action.IMAGE_CAPTURE_SECURE".equals(action)) {
            this.mSecureCamera = true;
        } else {
            this.mSecureCamera = intent.getBooleanExtra("secure_camera", false);
        }
        if (this.mSecureCamera) {
            Window window = getWindow();
            LayoutParams attributes = window.getAttributes();
            attributes.flags |= 524288;
            window.setAttributes(attributes);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getFilesDir());
        sb.append(BuildConfig.FLAVOR);
        this.mFilesPath = sb.toString();
        if (intent.getFlags() == 1 || this.mSecureCamera) {
            this.mMapRotated = true;
            StringBuilder sb2 = new StringBuilder();
            sb2.append(getFilesDir());
            sb2.append("/Ubifocus");
            this.mFilesPath = sb2.toString();
        }
        new Thread(new Runnable() {
            public void run() {
                RefocusActivity refocusActivity = RefocusActivity.this;
                StringBuilder sb = new StringBuilder();
                sb.append(RefocusActivity.this.mFilesPath);
                sb.append("/DepthMapImage.y");
                refocusActivity.mDepthMap = new DepthMap(sb.toString());
            }
        }).start();
        setContentView(C0905R.layout.refocus_editor);
        this.mIndicator = (Indicator) findViewById(C0905R.C0907id.refocus_indicator);
        this.mImageView = (ImageView) findViewById(C0905R.C0907id.refocus_image);
        this.mImageView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == 1) {
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    int width = view.getWidth();
                    int height = view.getHeight();
                    RefocusActivity.this.mIndicator.startAnimation(((float) RefocusActivity.this.mImageView.getLeft()) + x, ((float) RefocusActivity.this.mImageView.getTop()) + y);
                    if (RefocusActivity.this.mDepthMap != null) {
                        RefocusActivity.this.setCurrentImage(RefocusActivity.this.mDepthMap.getDepth(x / ((float) width), y / ((float) height)));
                        RefocusActivity.this.mAllInFocusView.setBackground(RefocusActivity.this.getDrawable(C0905R.C0906drawable.refocus_button_disable));
                    }
                }
                return true;
            }
        });
        this.mAllInFocusView = findViewById(C0905R.C0907id.refocus_all);
        this.mAllInFocusView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                RefocusActivity.this.allInFocus();
            }
        });
        findViewById(C0905R.C0907id.refocus_cancel).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                RefocusActivity.this.setResult(0, new Intent());
                RefocusActivity.this.finish();
            }
        });
        findViewById(C0905R.C0907id.refocus_done).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (RefocusActivity.this.mRequestedImage != RefocusActivity.NAMES.length - 1) {
                    SaveImageTask saveImageTask = new SaveImageTask();
                    StringBuilder sb = new StringBuilder();
                    sb.append(RefocusActivity.this.mFilesPath);
                    sb.append("/");
                    sb.append(RefocusActivity.NAMES[RefocusActivity.this.mRequestedImage]);
                    sb.append(Storage.JPEG_POSTFIX);
                    saveImageTask.execute(new String[]{sb.toString()});
                } else {
                    RefocusActivity.this.finish();
                }
                RefocusActivity.this.setResult(-1, new Intent());
            }
        });
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        this.mWidth = point.x;
        this.mHeight = point.y;
        allInFocus();
    }

    /* access modifiers changed from: private */
    public void setCurrentImage(int i) {
        if (i >= 0 && i < NAMES.length && i != this.mRequestedImage) {
            this.mRequestedImage = i;
            LoadImageTask loadImageTask = this.mLoadImageTask;
            if (loadImageTask != null) {
                loadImageTask.cancel(true);
            }
            if (i != this.mCurrentImage) {
                this.mCurrentImage = i;
                this.mLoadImageTask = new LoadImageTask();
                LoadImageTask loadImageTask2 = this.mLoadImageTask;
                StringBuilder sb = new StringBuilder();
                sb.append(this.mFilesPath);
                sb.append("/");
                sb.append(NAMES[i]);
                sb.append(Storage.JPEG_POSTFIX);
                loadImageTask2.execute(new String[]{sb.toString()});
            }
        }
    }

    /* access modifiers changed from: private */
    public void allInFocus() {
        setCurrentImage(NAMES.length - 1);
        this.mAllInFocusView.setBackground(getDrawable(C0905R.C0906drawable.refocus_button_enable));
    }
}
