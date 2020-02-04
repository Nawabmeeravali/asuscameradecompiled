package com.android.camera.tinyplanet;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.android.camera.CameraActivity;
import com.android.camera.MediaSaveService;
import com.android.camera.MediaSaveService.OnMediaSavedListener;
import com.android.camera.PhotoModule;
import com.android.camera.exif.ExifInterface;
import com.android.camera.tinyplanet.TinyPlanetPreview.PreviewSizeListener;
import com.android.camera.util.XmpUtil;
import com.asus.scenedetectlib.BuildConfig;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.codeaurora.snapcam.C0905R;

public class TinyPlanetFragment extends DialogFragment implements PreviewSizeListener {
    public static final String ARGUMENT_TITLE = "title";
    public static final String ARGUMENT_URI = "uri";
    public static final String CROPPED_AREA_FULL_PANO_HEIGHT_PIXELS = "FullPanoHeightPixels";
    public static final String CROPPED_AREA_FULL_PANO_WIDTH_PIXELS = "FullPanoWidthPixels";
    public static final String CROPPED_AREA_IMAGE_HEIGHT_PIXELS = "CroppedAreaImageHeightPixels";
    public static final String CROPPED_AREA_IMAGE_WIDTH_PIXELS = "CroppedAreaImageWidthPixels";
    public static final String CROPPED_AREA_LEFT = "CroppedAreaLeftPixels";
    public static final String CROPPED_AREA_TOP = "CroppedAreaTopPixels";
    private static final String FILENAME_PREFIX = "TINYPLANET_";
    public static final String GOOGLE_PANO_NAMESPACE = "http://ns.google.com/photos/1.0/panorama/";
    private static final int RENDER_DELAY_MILLIS = 50;
    private static final String TAG = "TinyPlanetActivity";
    private final Runnable mCreateTinyPlanetRunnable;
    /* access modifiers changed from: private */
    public float mCurrentAngle = 0.0f;
    /* access modifiers changed from: private */
    public float mCurrentZoom = 0.5f;
    /* access modifiers changed from: private */
    public ProgressDialog mDialog;
    private Handler mHandler = new Handler();
    /* access modifiers changed from: private */
    public String mOriginalTitle = BuildConfig.FLAVOR;
    /* access modifiers changed from: private */
    public TinyPlanetPreview mPreview;
    /* access modifiers changed from: private */
    public int mPreviewSizePx = 0;
    /* access modifiers changed from: private */
    public Boolean mRenderOneMore;
    /* access modifiers changed from: private */
    public Boolean mRendering;
    /* access modifiers changed from: private */
    public Bitmap mResultBitmap;
    /* access modifiers changed from: private */
    public Lock mResultLock = new ReentrantLock();
    /* access modifiers changed from: private */
    public Bitmap mSourceBitmap;
    private Uri mSourceImageUri;

    private static final class TinyPlanetImage {
        public final byte[] mJpegData;
        public final int mSize;

        public TinyPlanetImage(byte[] bArr, int i) {
            this.mJpegData = bArr;
            this.mSize = i;
        }
    }

    public TinyPlanetFragment() {
        Boolean valueOf = Boolean.valueOf(false);
        this.mRendering = valueOf;
        this.mRenderOneMore = valueOf;
        this.mCreateTinyPlanetRunnable = new Runnable() {
            public void run() {
                synchronized (TinyPlanetFragment.this.mRendering) {
                    if (TinyPlanetFragment.this.mRendering.booleanValue()) {
                        TinyPlanetFragment.this.mRenderOneMore = Boolean.valueOf(true);
                        return;
                    }
                    TinyPlanetFragment.this.mRendering = Boolean.valueOf(true);
                    new AsyncTask<Void, Void, Void>() {
                        /* access modifiers changed from: protected */
                        public Void doInBackground(Void... voidArr) {
                            TinyPlanetFragment.this.mResultLock.lock();
                            try {
                                if (TinyPlanetFragment.this.mSourceBitmap != null) {
                                    if (TinyPlanetFragment.this.mResultBitmap != null) {
                                        TinyPlanetNative.process(TinyPlanetFragment.this.mSourceBitmap, TinyPlanetFragment.this.mSourceBitmap.getWidth(), TinyPlanetFragment.this.mSourceBitmap.getHeight(), TinyPlanetFragment.this.mResultBitmap, TinyPlanetFragment.this.mPreviewSizePx, TinyPlanetFragment.this.mCurrentZoom, TinyPlanetFragment.this.mCurrentAngle);
                                        TinyPlanetFragment.this.mResultLock.unlock();
                                        return null;
                                    }
                                }
                                return null;
                            } finally {
                                TinyPlanetFragment.this.mResultLock.unlock();
                            }
                        }

                        /* access modifiers changed from: protected */
                        public void onPostExecute(Void voidR) {
                            TinyPlanetFragment.this.mPreview.setBitmap(TinyPlanetFragment.this.mResultBitmap, TinyPlanetFragment.this.mResultLock);
                            synchronized (TinyPlanetFragment.this.mRendering) {
                                TinyPlanetFragment.this.mRendering = Boolean.valueOf(false);
                                if (TinyPlanetFragment.this.mRenderOneMore.booleanValue()) {
                                    TinyPlanetFragment.this.mRenderOneMore = Boolean.valueOf(false);
                                    TinyPlanetFragment.this.scheduleUpdate();
                                }
                            }
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
                }
            }
        };
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setStyle(0, 2131755060);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        getDialog().getWindow().requestFeature(1);
        getDialog().setCanceledOnTouchOutside(true);
        View inflate = layoutInflater.inflate(C0905R.layout.tinyplanet_editor, viewGroup, false);
        this.mPreview = (TinyPlanetPreview) inflate.findViewById(C0905R.C0907id.preview);
        this.mPreview.setPreviewSizeChangeListener(this);
        ((SeekBar) inflate.findViewById(C0905R.C0907id.zoomSlider)).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                TinyPlanetFragment.this.onZoomChange(i);
            }
        });
        ((SeekBar) inflate.findViewById(C0905R.C0907id.angleSlider)).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                TinyPlanetFragment.this.onAngleChange(i);
            }
        });
        ((Button) inflate.findViewById(C0905R.C0907id.creatTinyPlanetButton)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                TinyPlanetFragment.this.onCreateTinyPlanet();
            }
        });
        this.mOriginalTitle = getArguments().getString(ARGUMENT_TITLE);
        this.mSourceImageUri = Uri.parse(getArguments().getString(ARGUMENT_URI));
        this.mSourceBitmap = createPaddedSourceImage(this.mSourceImageUri, true);
        if (this.mSourceBitmap == null) {
            Log.e(TAG, "Could not decode source image.");
            dismiss();
        }
        return inflate;
    }

    private Bitmap createPaddedSourceImage(Uri uri, boolean z) {
        InputStream inputStream = getInputStream(uri);
        if (inputStream == null) {
            Log.e(TAG, "Could not create input stream for image.");
            dismiss();
        }
        Bitmap decodeStream = BitmapFactory.decodeStream(inputStream);
        XMPMeta extractXMPMeta = XmpUtil.extractXMPMeta(getInputStream(uri));
        if (extractXMPMeta == null) {
            return decodeStream;
        }
        return createPaddedBitmap(decodeStream, extractXMPMeta, z ? getDisplaySize() : decodeStream.getWidth());
    }

    /* access modifiers changed from: private */
    public void onCreateTinyPlanet() {
        synchronized (this.mRendering) {
            this.mRenderOneMore = Boolean.valueOf(false);
        }
        final String string = getActivity().getResources().getString(C0905R.string.saving_tiny_planet);
        new AsyncTask<Void, Void, TinyPlanetImage>() {
            /* access modifiers changed from: protected */
            public void onPreExecute() {
                TinyPlanetFragment tinyPlanetFragment = TinyPlanetFragment.this;
                tinyPlanetFragment.mDialog = ProgressDialog.show(tinyPlanetFragment.getActivity(), null, string, true, false);
            }

            /* access modifiers changed from: protected */
            public TinyPlanetImage doInBackground(Void... voidArr) {
                return TinyPlanetFragment.this.createTinyPlanet();
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(TinyPlanetImage tinyPlanetImage) {
                final CameraActivity cameraActivity = (CameraActivity) TinyPlanetFragment.this.getActivity();
                MediaSaveService mediaSaveService = cameraActivity.getMediaSaveService();
                C08401 r11 = new OnMediaSavedListener() {
                    public void onMediaSaved(Uri uri) {
                        cameraActivity.notifyNewMedia(uri);
                        TinyPlanetFragment.this.mDialog.dismiss();
                        TinyPlanetFragment.this.dismiss();
                    }
                };
                StringBuilder sb = new StringBuilder();
                sb.append(TinyPlanetFragment.FILENAME_PREFIX);
                sb.append(TinyPlanetFragment.this.mOriginalTitle);
                String sb2 = sb.toString();
                byte[] bArr = tinyPlanetImage.mJpegData;
                long time = new Date().getTime();
                int i = tinyPlanetImage.mSize;
                mediaSaveService.addImage(bArr, sb2, time, null, i, i, 0, null, r11, TinyPlanetFragment.this.getActivity().getContentResolver(), PhotoModule.PIXEL_FORMAT_JPEG);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    public TinyPlanetImage createTinyPlanet() {
        this.mResultLock.lock();
        try {
            this.mResultBitmap.recycle();
            this.mResultBitmap = null;
            this.mSourceBitmap.recycle();
            this.mSourceBitmap = null;
            this.mResultLock.unlock();
            Bitmap createPaddedSourceImage = createPaddedSourceImage(this.mSourceImageUri, false);
            int width = createPaddedSourceImage.getWidth();
            int height = createPaddedSourceImage.getHeight();
            int i = width / 2;
            Bitmap createBitmap = Bitmap.createBitmap(i, i, Config.ARGB_8888);
            TinyPlanetNative.process(createPaddedSourceImage, width, height, createBitmap, i, this.mCurrentZoom, this.mCurrentAngle);
            createPaddedSourceImage.recycle();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            createBitmap.compress(CompressFormat.JPEG, 100, byteArrayOutputStream);
            return new TinyPlanetImage(addExif(byteArrayOutputStream.toByteArray()), i);
        } catch (Throwable th) {
            this.mResultLock.unlock();
            throw th;
        }
    }

    private byte[] addExif(byte[] bArr) {
        ExifInterface exifInterface = new ExifInterface();
        exifInterface.addDateTimeStampTag(ExifInterface.TAG_DATE_TIME, System.currentTimeMillis(), TimeZone.getDefault());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            exifInterface.writeExif(bArr, (OutputStream) byteArrayOutputStream);
        } catch (IOException e) {
            Log.e(TAG, "Could not write EXIF", e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private int getDisplaySize() {
        Display defaultDisplay = getActivity().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        return Math.min(point.x, point.y);
    }

    /* JADX INFO: finally extract failed */
    public void onSizeChanged(int i) {
        this.mPreviewSizePx = i;
        this.mResultLock.lock();
        try {
            if (!(this.mResultBitmap != null && this.mResultBitmap.getWidth() == i && this.mResultBitmap.getHeight() == i)) {
                if (this.mResultBitmap != null) {
                    this.mResultBitmap.recycle();
                }
                this.mResultBitmap = Bitmap.createBitmap(this.mPreviewSizePx, this.mPreviewSizePx, Config.ARGB_8888);
            }
            this.mResultLock.unlock();
            this.mCreateTinyPlanetRunnable.run();
        } catch (Throwable th) {
            this.mResultLock.unlock();
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public void onZoomChange(int i) {
        this.mCurrentZoom = ((float) i) / 1000.0f;
        scheduleUpdate();
    }

    /* access modifiers changed from: private */
    public void onAngleChange(int i) {
        this.mCurrentAngle = (float) Math.toRadians((double) i);
        scheduleUpdate();
    }

    /* access modifiers changed from: private */
    public void scheduleUpdate() {
        this.mHandler.removeCallbacks(this.mCreateTinyPlanetRunnable);
        this.mHandler.postDelayed(this.mCreateTinyPlanetRunnable, 50);
    }

    private InputStream getInputStream(Uri uri) {
        try {
            return getActivity().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Could not load source image.", e);
            return null;
        }
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(3:10|11|19) */
    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        java.lang.System.gc();
        r6 = r6 / 2.0f;
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:10:0x003e */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static android.graphics.Bitmap createPaddedBitmap(android.graphics.Bitmap r10, com.adobe.xmp.XMPMeta r11, int r12) {
        /*
            java.lang.String r0 = "CroppedAreaImageWidthPixels"
            int r0 = getInt(r11, r0)     // Catch:{ XMPException -> 0x005d }
            java.lang.String r1 = "CroppedAreaImageHeightPixels"
            int r1 = getInt(r11, r1)     // Catch:{ XMPException -> 0x005d }
            java.lang.String r2 = "FullPanoWidthPixels"
            int r2 = getInt(r11, r2)     // Catch:{ XMPException -> 0x005d }
            java.lang.String r3 = "FullPanoHeightPixels"
            int r3 = getInt(r11, r3)     // Catch:{ XMPException -> 0x005d }
            java.lang.String r4 = "CroppedAreaLeftPixels"
            int r4 = getInt(r11, r4)     // Catch:{ XMPException -> 0x005d }
            java.lang.String r5 = "CroppedAreaTopPixels"
            int r11 = getInt(r11, r5)     // Catch:{ XMPException -> 0x005d }
            if (r2 == 0) goto L_0x005d
            if (r3 != 0) goto L_0x0029
            goto L_0x005d
        L_0x0029:
            float r12 = (float) r12
            float r2 = (float) r2
            float r12 = r12 / r2
            r5 = 0
            r6 = r12
            r12 = r5
        L_0x002f:
            if (r12 != 0) goto L_0x0045
            float r7 = r2 * r6
            int r7 = (int) r7
            float r8 = (float) r3
            float r8 = r8 * r6
            int r8 = (int) r8
            android.graphics.Bitmap$Config r9 = android.graphics.Bitmap.Config.ARGB_8888     // Catch:{ OutOfMemoryError -> 0x003e }
            android.graphics.Bitmap r12 = android.graphics.Bitmap.createBitmap(r7, r8, r9)     // Catch:{ OutOfMemoryError -> 0x003e }
            goto L_0x002f
        L_0x003e:
            java.lang.System.gc()     // Catch:{ XMPException -> 0x005d }
            r7 = 1073741824(0x40000000, float:2.0)
            float r6 = r6 / r7
            goto L_0x002f
        L_0x0045:
            android.graphics.Canvas r2 = new android.graphics.Canvas     // Catch:{ XMPException -> 0x005d }
            r2.<init>(r12)     // Catch:{ XMPException -> 0x005d }
            int r0 = r0 + r4
            int r1 = r1 + r11
            android.graphics.RectF r3 = new android.graphics.RectF     // Catch:{ XMPException -> 0x005d }
            float r4 = (float) r4     // Catch:{ XMPException -> 0x005d }
            float r4 = r4 * r6
            float r11 = (float) r11     // Catch:{ XMPException -> 0x005d }
            float r11 = r11 * r6
            float r0 = (float) r0     // Catch:{ XMPException -> 0x005d }
            float r0 = r0 * r6
            float r1 = (float) r1     // Catch:{ XMPException -> 0x005d }
            float r1 = r1 * r6
            r3.<init>(r4, r11, r0, r1)     // Catch:{ XMPException -> 0x005d }
            r2.drawBitmap(r10, r5, r3, r5)     // Catch:{ XMPException -> 0x005d }
            return r12
        L_0x005d:
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.tinyplanet.TinyPlanetFragment.createPaddedBitmap(android.graphics.Bitmap, com.adobe.xmp.XMPMeta, int):android.graphics.Bitmap");
    }

    private static int getInt(XMPMeta xMPMeta, String str) throws XMPException {
        String str2 = GOOGLE_PANO_NAMESPACE;
        if (xMPMeta.doesPropertyExist(str2, str)) {
            return xMPMeta.getPropertyInteger(str2, str).intValue();
        }
        return 0;
    }
}
