package com.android.camera.p004ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/* renamed from: com.android.camera.ui.ZoomView */
public class ZoomView extends ImageView {
    private static final String TAG = "ZoomView";
    /* access modifiers changed from: private */
    public int mFullResImageHeight;
    /* access modifiers changed from: private */
    public int mFullResImageWidth;
    /* access modifiers changed from: private */
    public int mOrientation;
    /* access modifiers changed from: private */
    public DecodePartialBitmap mPartialDecodingTask;
    /* access modifiers changed from: private */
    public BitmapRegionDecoder mRegionDecoder;
    private Uri mUri;
    /* access modifiers changed from: private */
    public int mViewportHeight = 0;
    /* access modifiers changed from: private */
    public int mViewportWidth = 0;

    /* renamed from: com.android.camera.ui.ZoomView$DecodePartialBitmap */
    private class DecodePartialBitmap extends AsyncTask<RectF, Void, Bitmap> {
        private DecodePartialBitmap() {
        }

        /* access modifiers changed from: protected */
        public Bitmap doInBackground(RectF... rectFArr) {
            RectF rectF = rectFArr[0];
            RectF rectF2 = new RectF(0.0f, 0.0f, (float) (ZoomView.this.mFullResImageWidth - 1), (float) (ZoomView.this.mFullResImageHeight - 1));
            Matrix matrix = new Matrix();
            matrix.setRotate((float) ZoomView.this.mOrientation, 0.0f, 0.0f);
            matrix.mapRect(rectF2);
            matrix.postTranslate(-rectF2.left, -rectF2.top);
            matrix.mapRect(rectF2, new RectF(0.0f, 0.0f, (float) (ZoomView.this.mFullResImageWidth - 1), (float) (ZoomView.this.mFullResImageHeight - 1)));
            RectF rectF3 = new RectF(rectF);
            rectF3.intersect(0.0f, 0.0f, (float) (ZoomView.this.mViewportWidth - 1), (float) (ZoomView.this.mViewportHeight - 1));
            Matrix matrix2 = new Matrix();
            matrix2.setRectToRect(rectF, rectF2, ScaleToFit.CENTER);
            RectF rectF4 = new RectF();
            matrix2.mapRect(rectF4, rectF3);
            RectF rectF5 = new RectF();
            Matrix matrix3 = new Matrix();
            matrix.invert(matrix3);
            matrix3.mapRect(rectF5, rectF4);
            Rect rect = new Rect();
            rectF5.round(rect);
            rect.intersect(0, 0, ZoomView.this.mFullResImageWidth - 1, ZoomView.this.mFullResImageHeight - 1);
            int width = rect.width();
            String str = ZoomView.TAG;
            if (width == 0 || rect.height() == 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("Invalid size for partial region. Region: ");
                sb.append(rect.toString());
                Log.e(str, sb.toString());
                return null;
            } else if (isCancelled()) {
                return null;
            } else {
                Options options = new Options();
                if ((ZoomView.this.mOrientation + 360) % 180 == 0) {
                    options.inSampleSize = ZoomView.this.getSampleFactor(rect.width(), rect.height());
                } else {
                    options.inSampleSize = ZoomView.this.getSampleFactor(rect.height(), rect.width());
                }
                if (ZoomView.this.mRegionDecoder == null) {
                    InputStream access$700 = ZoomView.this.getInputStream();
                    try {
                        ZoomView.this.mRegionDecoder = BitmapRegionDecoder.newInstance(access$700, false);
                        access$700.close();
                    } catch (IOException unused) {
                        Log.e(str, "Failed to instantiate region decoder");
                    }
                }
                if (ZoomView.this.mRegionDecoder == null) {
                    return null;
                }
                Bitmap decodeRegion = ZoomView.this.mRegionDecoder.decodeRegion(rect, options);
                if (isCancelled()) {
                    return null;
                }
                Matrix matrix4 = new Matrix();
                matrix4.setRotate((float) ZoomView.this.mOrientation);
                return Bitmap.createBitmap(decodeRegion, 0, 0, decodeRegion.getWidth(), decodeRegion.getHeight(), matrix4, false);
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                ZoomView.this.setImageBitmap(bitmap);
                ZoomView.this.showPartiallyDecodedImage(true);
                ZoomView.this.mPartialDecodingTask = null;
            }
        }
    }

    public ZoomView(Context context) {
        super(context);
        setScaleType(ScaleType.FIT_CENTER);
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                int i9 = i3 - i;
                int i10 = i4 - i2;
                if (ZoomView.this.mViewportHeight != i10 || ZoomView.this.mViewportWidth != i9) {
                    ZoomView.this.mViewportWidth = i9;
                    ZoomView.this.mViewportHeight = i10;
                }
            }
        });
    }

    public void loadBitmap(Uri uri, int i, RectF rectF) {
        if (!uri.equals(this.mUri)) {
            this.mUri = uri;
            this.mOrientation = i;
            this.mFullResImageHeight = 0;
            this.mFullResImageWidth = 0;
            decodeImageSize();
            this.mRegionDecoder = null;
        }
        startPartialDecodingTask(rectF);
    }

    /* access modifiers changed from: private */
    public void showPartiallyDecodedImage(boolean z) {
        if (z) {
            setVisibility(0);
        } else {
            setVisibility(8);
        }
        this.mPartialDecodingTask = null;
    }

    public void cancelPartialDecodingTask() {
        DecodePartialBitmap decodePartialBitmap = this.mPartialDecodingTask;
        if (decodePartialBitmap != null && !decodePartialBitmap.isCancelled()) {
            this.mPartialDecodingTask.cancel(true);
            setVisibility(8);
        }
        this.mPartialDecodingTask = null;
    }

    public static RectF adjustToFitInBounds(RectF rectF, int i, int i2) {
        float f;
        float f2;
        RectF rectF2 = new RectF(rectF);
        float f3 = (float) i;
        if (rectF2.width() < f3) {
            f = ((float) (i / 2)) - ((rectF2.left + rectF2.right) / 2.0f);
        } else {
            float f4 = rectF2.left;
            if (f4 > 0.0f) {
                f = -f4;
            } else {
                float f5 = rectF2.right;
                f = f5 < f3 ? f3 - f5 : 0.0f;
            }
        }
        float f6 = (float) i2;
        if (rectF2.height() < f6) {
            f2 = ((float) (i2 / 2)) - ((rectF2.top + rectF2.bottom) / 2.0f);
        } else {
            float f7 = rectF2.top;
            if (f7 > 0.0f) {
                f2 = -f7;
            } else {
                float f8 = rectF2.bottom;
                f2 = f8 < f6 ? f6 - f8 : 0.0f;
            }
        }
        if (!(f == 0.0f && f2 == 0.0f)) {
            rectF2.offset(f, f2);
        }
        return rectF2;
    }

    private void startPartialDecodingTask(RectF rectF) {
        cancelPartialDecodingTask();
        this.mPartialDecodingTask = new DecodePartialBitmap();
        this.mPartialDecodingTask.execute(new RectF[]{rectF});
    }

    private void decodeImageSize() {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        InputStream inputStream = getInputStream();
        BitmapFactory.decodeStream(inputStream, null, options);
        try {
            inputStream.close();
        } catch (IOException unused) {
            Log.e(TAG, "Failed to close input stream");
        }
        this.mFullResImageWidth = options.outWidth;
        this.mFullResImageHeight = options.outHeight;
    }

    /* access modifiers changed from: private */
    public InputStream getInputStream() {
        try {
            return getContext().getContentResolver().openInputStream(this.mUri);
        } catch (FileNotFoundException unused) {
            StringBuilder sb = new StringBuilder();
            sb.append("File not found at: ");
            sb.append(this.mUri);
            Log.e(TAG, sb.toString());
            return null;
        }
    }

    /* access modifiers changed from: private */
    public int getSampleFactor(int i, int i2) {
        int min = (int) (1.0f / Math.min(((float) this.mViewportHeight) / ((float) i2), ((float) this.mViewportWidth) / ((float) i)));
        if (min <= 1) {
            return 1;
        }
        int i3 = 0;
        while (true) {
            if (i3 >= 32) {
                break;
            }
            int i4 = i3 + 1;
            if ((1 << i4) > min) {
                min = 1 << i3;
                break;
            }
            i3 = i4;
        }
        return min;
    }
}
