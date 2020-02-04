package com.android.camera.crop;

import android.app.ActionBar;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.codeaurora.snapcam.C0905R;

public class CropActivity extends Activity {
    public static final String CROP_ACTION = "com.android.camera.action.CROP";
    private static final int DEFAULT_COMPRESS_QUALITY = 90;
    private static final int DO_EXTRA_OUTPUT = 4;
    private static final int DO_RETURN_DATA = 2;
    private static final int DO_SET_WALLPAPER = 1;
    private static final int FLAG_CHECK = 7;
    private static final String LOGTAG = "CropActivity";
    public static final int MAX_BMAP_IN_INTENT = 750000;
    private static final int SELECT_PICTURE = 1;
    private boolean finalIOGuard = false;
    private CropExtras mCropExtras = null;
    private CropView mCropView = null;
    private LoadBitmapTask mLoadBitmapTask = null;
    private Bitmap mOriginalBitmap = null;
    private RectF mOriginalBounds = null;
    private int mOriginalRotation = 0;
    /* access modifiers changed from: private */
    public int mOutputX = 0;
    /* access modifiers changed from: private */
    public int mOutputY = 0;
    private View mSaveButton = null;
    private Uri mSourceUri = null;

    private class BitmapIOTask extends AsyncTask<Bitmap, Void, Boolean> {
        RectF mCrop = null;
        int mFlags = 0;
        InputStream mInStream = null;
        Uri mInUri = null;
        RectF mOrig = null;
        OutputStream mOutStream = null;
        Uri mOutUri = null;
        String mOutputFormat = null;
        RectF mPhoto = null;
        Intent mResultIntent = null;
        int mRotation = 0;
        private final WallpaperManager mWPManager;

        private void regenerateInputStream() {
            Uri uri = this.mInUri;
            String str = CropActivity.LOGTAG;
            if (uri == null) {
                Log.w(str, "cannot read original file, no input URI given");
                return;
            }
            Utils.closeSilently((Closeable) this.mInStream);
            try {
                this.mInStream = CropActivity.this.getContentResolver().openInputStream(this.mInUri);
            } catch (FileNotFoundException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("cannot read file: ");
                sb.append(this.mInUri.toString());
                Log.w(str, sb.toString(), e);
            }
        }

        public BitmapIOTask(Uri uri, Uri uri2, String str, int i, RectF rectF, RectF rectF2, RectF rectF3, int i2, int i3, int i4) {
            this.mOutputFormat = str;
            this.mOutStream = null;
            this.mOutUri = uri2;
            this.mInUri = uri;
            this.mFlags = i;
            this.mCrop = rectF;
            this.mPhoto = rectF2;
            this.mOrig = rectF3;
            this.mWPManager = WallpaperManager.getInstance(CropActivity.this.getApplicationContext());
            this.mResultIntent = new Intent();
            if (i2 < 0) {
                i2 = -i2;
            }
            this.mRotation = i2;
            this.mRotation %= 360;
            this.mRotation = (this.mRotation / CropActivity.DEFAULT_COMPRESS_QUALITY) * CropActivity.DEFAULT_COMPRESS_QUALITY;
            CropActivity.this.mOutputX = i3;
            CropActivity.this.mOutputY = i4;
            if ((i & 4) != 0) {
                Uri uri3 = this.mOutUri;
                String str2 = CropActivity.LOGTAG;
                if (uri3 == null) {
                    Log.w(str2, "cannot write file, no output URI given");
                } else {
                    try {
                        this.mOutStream = CropActivity.this.getContentResolver().openOutputStream(this.mOutUri);
                    } catch (FileNotFoundException e) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("cannot write file: ");
                        sb.append(this.mOutUri.toString());
                        Log.w(str2, sb.toString(), e);
                    }
                }
            }
            if ((i & 5) != 0) {
                regenerateInputStream();
            }
        }

        /* access modifiers changed from: protected */
        public Boolean doInBackground(Bitmap... bitmapArr) {
            BitmapRegionDecoder bitmapRegionDecoder;
            Bitmap bitmap;
            boolean z = false;
            Boolean valueOf = Boolean.valueOf(false);
            Bitmap bitmap2 = bitmapArr[0];
            RectF rectF = this.mCrop;
            if (rectF != null) {
                RectF rectF2 = this.mPhoto;
                if (rectF2 != null) {
                    RectF rectF3 = this.mOrig;
                    if (rectF3 != null) {
                        RectF scaledCropBounds = CropMath.getScaledCropBounds(rectF, rectF2, rectF3);
                        Matrix matrix = new Matrix();
                        matrix.setRotate((float) this.mRotation);
                        matrix.mapRect(scaledCropBounds);
                        if (scaledCropBounds != null) {
                            Rect rect = new Rect();
                            scaledCropBounds.roundOut(rect);
                            this.mResultIntent.putExtra(CropExtras.KEY_CROPPED_RECT, rect);
                        }
                    }
                }
            }
            int i = this.mFlags & 2;
            String str = CropActivity.LOGTAG;
            if (i != 0) {
                Bitmap croppedImage = CropActivity.getCroppedImage(bitmap2, this.mCrop, this.mPhoto);
                if (croppedImage != null) {
                    croppedImage = CropActivity.getDownsampledBitmap(croppedImage, CropActivity.MAX_BMAP_IN_INTENT);
                }
                if (croppedImage == null) {
                    Log.w(str, "could not downsample bitmap to return in data");
                    z = true;
                } else {
                    if (this.mRotation > 0) {
                        Matrix matrix2 = new Matrix();
                        matrix2.setRotate((float) this.mRotation);
                        Bitmap createBitmap = Bitmap.createBitmap(croppedImage, 0, 0, croppedImage.getWidth(), croppedImage.getHeight(), matrix2, true);
                        if (createBitmap != null) {
                            croppedImage = createBitmap;
                        }
                    }
                    this.mResultIntent.putExtra(CropExtras.KEY_DATA, croppedImage);
                }
            }
            if (!((this.mFlags & 5) == 0 || this.mInStream == null)) {
                RectF scaledCropBounds2 = CropMath.getScaledCropBounds(this.mCrop, this.mPhoto, this.mOrig);
                if (scaledCropBounds2 == null) {
                    Log.w(str, "cannot find crop for full size image");
                    return valueOf;
                }
                Rect rect2 = new Rect();
                scaledCropBounds2.roundOut(rect2);
                if (rect2.width() <= 0 || rect2.height() <= 0) {
                    Log.w(str, "crop has bad values for full size image");
                    return valueOf;
                }
                Bitmap bitmap3 = null;
                try {
                    bitmapRegionDecoder = BitmapRegionDecoder.newInstance(this.mInStream, true);
                } catch (IOException e) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("cannot open region decoder for file: ");
                    sb.append(this.mInUri.toString());
                    Log.w(str, sb.toString(), e);
                    bitmapRegionDecoder = null;
                }
                if (bitmapRegionDecoder != null) {
                    Options options = new Options();
                    options.inMutable = true;
                    bitmap = bitmapRegionDecoder.decodeRegion(rect2, options);
                    bitmapRegionDecoder.recycle();
                } else {
                    bitmap = null;
                }
                if (bitmap == null) {
                    regenerateInputStream();
                    InputStream inputStream = this.mInStream;
                    if (inputStream != null) {
                        bitmap3 = BitmapFactory.decodeStream(inputStream);
                    }
                    if (bitmap3 != null) {
                        bitmap = Bitmap.createBitmap(bitmap3, rect2.left, rect2.top, rect2.width(), rect2.height());
                    }
                }
                if (bitmap == null) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("cannot decode file: ");
                    sb2.append(this.mInUri.toString());
                    Log.w(str, sb2.toString());
                    return valueOf;
                }
                if (CropActivity.this.mOutputX > 0 && CropActivity.this.mOutputY > 0) {
                    Matrix matrix3 = new Matrix();
                    RectF rectF4 = new RectF(0.0f, 0.0f, (float) bitmap.getWidth(), (float) bitmap.getHeight());
                    int i2 = this.mRotation;
                    if (i2 > 0) {
                        matrix3.setRotate((float) i2);
                        matrix3.mapRect(rectF4);
                    }
                    RectF rectF5 = new RectF(0.0f, 0.0f, (float) CropActivity.this.mOutputX, (float) CropActivity.this.mOutputY);
                    matrix3.setRectToRect(rectF4, rectF5, ScaleToFit.FILL);
                    matrix3.preRotate((float) this.mRotation);
                    Bitmap createBitmap2 = Bitmap.createBitmap((int) rectF5.width(), (int) rectF5.height(), Config.ARGB_8888);
                    if (createBitmap2 != null) {
                        new Canvas(createBitmap2).drawBitmap(bitmap, matrix3, new Paint());
                        bitmap = createBitmap2;
                    }
                } else if (this.mRotation > 0) {
                    Matrix matrix4 = new Matrix();
                    matrix4.setRotate((float) this.mRotation);
                    Bitmap createBitmap3 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix4, true);
                    if (createBitmap3 != null) {
                        bitmap = createBitmap3;
                    }
                }
                CompressFormat convertExtensionToCompressFormat = CropActivity.convertExtensionToCompressFormat(CropActivity.getFileExtension(this.mOutputFormat));
                String str2 = "failed to compress bitmap to file: ";
                if (this.mFlags == 4) {
                    OutputStream outputStream = this.mOutStream;
                    if (outputStream == null || !bitmap.compress(convertExtensionToCompressFormat, CropActivity.DEFAULT_COMPRESS_QUALITY, outputStream)) {
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append(str2);
                        sb3.append(this.mOutUri.toString());
                        Log.w(str, sb3.toString());
                    } else {
                        this.mResultIntent.setData(this.mOutUri);
                    }
                } else {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(2048);
                    if (bitmap.compress(convertExtensionToCompressFormat, CropActivity.DEFAULT_COMPRESS_QUALITY, byteArrayOutputStream)) {
                        if ((this.mFlags & 4) != 0) {
                            OutputStream outputStream2 = this.mOutStream;
                            if (outputStream2 == null) {
                                StringBuilder sb4 = new StringBuilder();
                                sb4.append(str2);
                                sb4.append(this.mOutUri.toString());
                                Log.w(str, sb4.toString());
                            } else {
                                try {
                                    outputStream2.write(byteArrayOutputStream.toByteArray());
                                    this.mResultIntent.setData(this.mOutUri);
                                } catch (IOException e2) {
                                    StringBuilder sb5 = new StringBuilder();
                                    sb5.append(str2);
                                    sb5.append(this.mOutUri.toString());
                                    Log.w(str, sb5.toString(), e2);
                                }
                            }
                            z = true;
                        }
                        if ((this.mFlags & 1) != 0) {
                            WallpaperManager wallpaperManager = this.mWPManager;
                            if (wallpaperManager != null) {
                                if (wallpaperManager == null) {
                                    Log.w(str, "no wallpaper manager");
                                } else {
                                    try {
                                        wallpaperManager.setStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                                    } catch (IOException e3) {
                                        Log.w(str, "cannot write stream to wallpaper", e3);
                                    }
                                }
                            }
                        }
                    } else {
                        Log.w(str, "cannot compress bitmap");
                    }
                }
                z = true;
            }
            return Boolean.valueOf(!z);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Boolean bool) {
            Utils.closeSilently((Closeable) this.mOutStream);
            Utils.closeSilently((Closeable) this.mInStream);
            CropActivity.this.doneBitmapIO(bool.booleanValue(), this.mResultIntent);
        }
    }

    private class LoadBitmapTask extends AsyncTask<Uri, Void, Bitmap> {
        int mBitmapSize;
        Context mContext;
        int mOrientation = 0;
        Rect mOriginalBounds = new Rect();

        public LoadBitmapTask() {
            this.mBitmapSize = CropActivity.this.getScreenImageSize();
            this.mContext = CropActivity.this.getApplicationContext();
        }

        /* access modifiers changed from: protected */
        public Bitmap doInBackground(Uri... uriArr) {
            Uri uri = uriArr[0];
            Bitmap loadConstrainedBitmap = ImageLoader.loadConstrainedBitmap(uri, this.mContext, this.mBitmapSize, this.mOriginalBounds, false);
            this.mOrientation = ImageLoader.getMetadataRotation(this.mContext, uri);
            return loadConstrainedBitmap;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
            CropActivity.this.doneLoadBitmap(bitmap, new RectF(this.mOriginalBounds), this.mOrientation);
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        setResult(0, new Intent());
        this.mCropExtras = getExtrasFromIntent(intent);
        CropExtras cropExtras = this.mCropExtras;
        if (cropExtras != null && cropExtras.getShowWhenLocked()) {
            getWindow().addFlags(524288);
        }
        setContentView(C0905R.layout.crop_activity);
        this.mCropView = (CropView) findViewById(C0905R.C0907id.cropView);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(16);
            actionBar.setCustomView(C0905R.layout.crop_actionbar);
            actionBar.getCustomView().setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    CropActivity.this.startFinishOutput();
                }
            });
        }
        if (intent.getData() != null) {
            this.mSourceUri = intent.getData();
            startLoadBitmap(this.mSourceUri);
        }
    }

    private void enableSave(boolean z) {
        View view = this.mSaveButton;
        if (view != null) {
            view.setEnabled(z);
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        LoadBitmapTask loadBitmapTask = this.mLoadBitmapTask;
        if (loadBitmapTask != null) {
            loadBitmapTask.cancel(false);
        }
        super.onDestroy();
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mCropView.configChanged();
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        if (i2 == -1 && i == 1) {
            this.mSourceUri = intent.getData();
            startLoadBitmap(this.mSourceUri);
        }
    }

    /* access modifiers changed from: private */
    public int getScreenImageSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return Math.max(displayMetrics.heightPixels, displayMetrics.widthPixels);
    }

    private void startLoadBitmap(Uri uri) {
        if (uri != null) {
            enableSave(false);
            findViewById(C0905R.C0907id.loading).setVisibility(0);
            this.mLoadBitmapTask = new LoadBitmapTask();
            this.mLoadBitmapTask.execute(new Uri[]{uri});
            return;
        }
        cannotLoadImage();
        done();
    }

    /* access modifiers changed from: private */
    public void doneLoadBitmap(Bitmap bitmap, RectF rectF, int i) {
        findViewById(C0905R.C0907id.loading).setVisibility(8);
        this.mOriginalBitmap = bitmap;
        this.mOriginalBounds = rectF;
        this.mOriginalRotation = i;
        if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
            Log.w(LOGTAG, "could not load image for cropping");
            cannotLoadImage();
            setResult(0, new Intent());
            done();
            return;
        }
        RectF rectF2 = new RectF(0.0f, 0.0f, (float) bitmap.getWidth(), (float) bitmap.getHeight());
        this.mCropView.initialize(bitmap, rectF2, rectF2, i);
        CropExtras cropExtras = this.mCropExtras;
        if (cropExtras != null) {
            int aspectX = cropExtras.getAspectX();
            int aspectY = this.mCropExtras.getAspectY();
            this.mOutputX = this.mCropExtras.getOutputX();
            this.mOutputY = this.mCropExtras.getOutputY();
            int i2 = this.mOutputX;
            if (i2 > 0) {
                int i3 = this.mOutputY;
                if (i3 > 0) {
                    this.mCropView.applyAspect((float) i2, (float) i3);
                }
            }
            float spotlightX = this.mCropExtras.getSpotlightX();
            float spotlightY = this.mCropExtras.getSpotlightY();
            if (spotlightX > 0.0f && spotlightY > 0.0f) {
                this.mCropView.setWallpaperSpotlight(spotlightX, spotlightY);
            }
            if (aspectX > 0 && aspectY > 0) {
                this.mCropView.applyAspect((float) aspectX, (float) aspectY);
            }
        }
        enableSave(true);
    }

    private void cannotLoadImage() {
        Toast.makeText(this, getString(C0905R.string.cannot_load_image), 0).show();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0052  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startFinishOutput() {
        /*
            r14 = this;
            boolean r0 = r14.finalIOGuard
            if (r0 == 0) goto L_0x0005
            return
        L_0x0005:
            r0 = 1
            r14.finalIOGuard = r0
            r0 = 0
            r14.enableSave(r0)
            android.graphics.Bitmap r1 = r14.mOriginalBitmap
            r2 = 0
            if (r1 == 0) goto L_0x003e
            com.android.camera.crop.CropExtras r1 = r14.mCropExtras
            if (r1 == 0) goto L_0x003e
            android.net.Uri r1 = r1.getExtraOutput()
            if (r1 == 0) goto L_0x0027
            com.android.camera.crop.CropExtras r1 = r14.mCropExtras
            android.net.Uri r1 = r1.getExtraOutput()
            if (r1 == 0) goto L_0x0025
            r3 = 4
            goto L_0x0029
        L_0x0025:
            r3 = r0
            goto L_0x0029
        L_0x0027:
            r3 = r0
            r1 = r2
        L_0x0029:
            com.android.camera.crop.CropExtras r4 = r14.mCropExtras
            boolean r4 = r4.getSetAsWallpaper()
            if (r4 == 0) goto L_0x0033
            r3 = r3 | 1
        L_0x0033:
            com.android.camera.crop.CropExtras r4 = r14.mCropExtras
            boolean r4 = r4.getReturnData()
            if (r4 == 0) goto L_0x0040
            r3 = r3 | 2
            goto L_0x0040
        L_0x003e:
            r3 = r0
            r1 = r2
        L_0x0040:
            if (r3 != 0) goto L_0x004c
            android.net.Uri r1 = r14.mSourceUri
            android.net.Uri r1 = com.android.camera.crop.SaveImage.makeAndInsertUri(r14, r1)
            if (r1 == 0) goto L_0x004c
            r3 = r3 | 4
        L_0x004c:
            r8 = r1
            r5 = r3
            r1 = r5 & 7
            if (r1 == 0) goto L_0x0083
            android.graphics.Bitmap r1 = r14.mOriginalBitmap
            if (r1 == 0) goto L_0x0083
            android.graphics.RectF r10 = new android.graphics.RectF
            int r0 = r1.getWidth()
            float r0 = (float) r0
            android.graphics.Bitmap r1 = r14.mOriginalBitmap
            int r1 = r1.getHeight()
            float r1 = (float) r1
            r3 = 0
            r10.<init>(r3, r3, r0, r1)
            android.graphics.RectF r9 = r14.getBitmapCrop(r10)
            android.graphics.Bitmap r6 = r14.mOriginalBitmap
            android.net.Uri r7 = r14.mSourceUri
            android.graphics.RectF r11 = r14.mOriginalBounds
            com.android.camera.crop.CropExtras r0 = r14.mCropExtras
            if (r0 != 0) goto L_0x0077
            goto L_0x007b
        L_0x0077:
            java.lang.String r2 = r0.getOutputFormat()
        L_0x007b:
            r12 = r2
            int r13 = r14.mOriginalRotation
            r4 = r14
            r4.startBitmapIO(r5, r6, r7, r8, r9, r10, r11, r12, r13)
            return
        L_0x0083:
            android.content.Intent r1 = new android.content.Intent
            r1.<init>()
            r14.setResult(r0, r1)
            r14.done()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.crop.CropActivity.startFinishOutput():void");
    }

    private void startBitmapIO(int i, Bitmap bitmap, Uri uri, Uri uri2, RectF rectF, RectF rectF2, RectF rectF3, String str, int i2) {
        if (rectF != null && rectF2 != null && bitmap != null && bitmap.getWidth() != 0 && bitmap.getHeight() != 0 && rectF.width() != 0.0f && rectF.height() != 0.0f && rectF2.width() != 0.0f && rectF2.height() != 0.0f && (i & 7) != 0) {
            if ((i & 1) != 0) {
                Toast.makeText(this, C0905R.string.setting_wallpaper, 1).show();
            }
            findViewById(C0905R.C0907id.loading).setVisibility(0);
            BitmapIOTask bitmapIOTask = new BitmapIOTask(uri, uri2, str, i, rectF, rectF2, rectF3, i2, this.mOutputX, this.mOutputY);
            bitmapIOTask.execute(new Bitmap[]{bitmap});
        }
    }

    /* access modifiers changed from: private */
    public void doneBitmapIO(boolean z, Intent intent) {
        findViewById(C0905R.C0907id.loading).setVisibility(8);
        if (z) {
            setResult(-1, intent);
        } else {
            setResult(0, intent);
        }
        done();
    }

    private void done() {
        finish();
    }

    protected static Bitmap getCroppedImage(Bitmap bitmap, RectF rectF, RectF rectF2) {
        RectF scaledCropBounds = CropMath.getScaledCropBounds(rectF, rectF2, new RectF(0.0f, 0.0f, (float) bitmap.getWidth(), (float) bitmap.getHeight()));
        if (scaledCropBounds == null) {
            return null;
        }
        Rect rect = new Rect();
        scaledCropBounds.roundOut(rect);
        return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
    }

    protected static Bitmap getDownsampledBitmap(Bitmap bitmap, int i) {
        if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0 || i < 16) {
            throw new IllegalArgumentException("Bad argument to getDownsampledBitmap()");
        }
        int i2 = 0;
        for (int bitmapSize = CropMath.getBitmapSize(bitmap); bitmapSize > i; bitmapSize /= 4) {
            i2++;
        }
        Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() >> i2, bitmap.getHeight() >> i2, true);
        if (createScaledBitmap == null) {
            return null;
        }
        if (CropMath.getBitmapSize(createScaledBitmap) > i) {
            createScaledBitmap = Bitmap.createScaledBitmap(createScaledBitmap, createScaledBitmap.getWidth() >> 1, createScaledBitmap.getHeight() >> 1, true);
        }
        return createScaledBitmap;
    }

    protected static CropExtras getExtrasFromIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return null;
        }
        int i = extras.getInt(CropExtras.KEY_OUTPUT_X, 0);
        int i2 = extras.getInt(CropExtras.KEY_OUTPUT_Y, 0);
        boolean z = true;
        if (!extras.getBoolean(CropExtras.KEY_SCALE, true) || !extras.getBoolean(CropExtras.KEY_SCALE_UP_IF_NEEDED, false)) {
            z = false;
        }
        CropExtras cropExtras = new CropExtras(i, i2, z, extras.getInt(CropExtras.KEY_ASPECT_X, 0), extras.getInt(CropExtras.KEY_ASPECT_Y, 0), extras.getBoolean(CropExtras.KEY_SET_AS_WALLPAPER, false), extras.getBoolean("return-data", false), (Uri) extras.getParcelable("output"), extras.getString(CropExtras.KEY_OUTPUT_FORMAT), extras.getBoolean("showWhenLocked", false), extras.getFloat(CropExtras.KEY_SPOTLIGHT_X), extras.getFloat(CropExtras.KEY_SPOTLIGHT_Y));
        return cropExtras;
    }

    protected static CompressFormat convertExtensionToCompressFormat(String str) {
        return str.equals("png") ? CompressFormat.PNG : CompressFormat.JPEG;
    }

    protected static String getFileExtension(String str) {
        String str2 = "jpg";
        if (str == null) {
            str = str2;
        }
        String lowerCase = str.toLowerCase();
        String str3 = "png";
        return (lowerCase.equals(str3) || lowerCase.equals("gif")) ? str3 : str2;
    }

    private RectF getBitmapCrop(RectF rectF) {
        RectF crop = this.mCropView.getCrop();
        RectF photo = this.mCropView.getPhoto();
        if (crop != null && photo != null) {
            return CropMath.getScaledCropBounds(crop, photo, rectF);
        }
        Log.w(LOGTAG, "could not get crop");
        return null;
    }
}
