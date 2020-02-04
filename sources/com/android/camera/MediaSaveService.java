package com.android.camera;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.android.camera.exif.ExifInterface;
import com.android.camera.mpo.MpoData;
import com.android.camera.mpo.MpoImageData;
import com.android.camera.mpo.MpoInterface;
import com.android.camera.util.PersistUtil;
import com.android.camera.util.XmpUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteOrder;
import org.codeaurora.snapcam.filter.GDepth;
import org.codeaurora.snapcam.filter.GImage;

public class MediaSaveService extends Service {
    private static final int SAVE_TASK_MEMORY_LIMIT = ((SAVE_TASK_MEMORY_LIMIT_IN_MB * 1024) * 1024);
    private static final int SAVE_TASK_MEMORY_LIMIT_IN_MB = PersistUtil.getSaveTaskMemoryLimitInMb();
    /* access modifiers changed from: private */
    public static final String TAG;
    public static final String VIDEO_BASE_URI = "content://media/external/video/media";
    private final IBinder mBinder = new LocalBinder();
    private Listener mListener;
    private long mMemoryUse;

    private class HEIFImageSaveTask extends AsyncTask<Void, Void, Uri> {
        private long date;
        private ExifInterface exif;
        private int height;
        private OnMediaSavedListener listener;
        private Location loc;
        private int orientation;
        private String path;
        private String pictureFormat;
        private int quality;
        private ContentResolver resolver;
        private String title;
        private int width;

        public HEIFImageSaveTask(String str, String str2, long j, Location location, int i, int i2, int i3, ExifInterface exifInterface, ContentResolver contentResolver, OnMediaSavedListener onMediaSavedListener, int i4, String str3) {
            this.path = str;
            this.title = str2;
            this.date = j;
            this.loc = location;
            this.width = i;
            this.height = i2;
            this.orientation = i3;
            this.exif = exifInterface;
            this.resolver = contentResolver;
            this.listener = onMediaSavedListener;
            this.quality = i4;
            this.pictureFormat = str3;
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
        }

        /* access modifiers changed from: protected */
        public Uri doInBackground(Void... voidArr) {
            return Storage.addHeifImage(this.resolver, this.title, this.date, this.loc, this.orientation, this.exif, this.path, this.width, this.height, this.quality, this.pictureFormat);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Uri uri) {
            OnMediaSavedListener onMediaSavedListener = this.listener;
            if (onMediaSavedListener != null) {
                onMediaSavedListener.onMediaSaved(uri);
            }
        }
    }

    private class ImageSaveTask extends AsyncTask<Void, Void, Uri> {
        private byte[] data;
        private long date;
        private ExifInterface exif;
        private int height;
        private OnMediaSavedListener listener;
        private Location loc;
        private int orientation;
        private String pictureFormat;
        private ContentResolver resolver;
        private String title;
        private int width;

        /* access modifiers changed from: protected */
        public void onPreExecute() {
        }

        public ImageSaveTask(byte[] bArr, String str, long j, Location location, int i, int i2, int i3, ExifInterface exifInterface, ContentResolver contentResolver, OnMediaSavedListener onMediaSavedListener, String str2) {
            this.data = bArr;
            this.title = str;
            this.date = j;
            this.loc = location;
            this.width = i;
            this.height = i2;
            this.orientation = i3;
            this.exif = exifInterface;
            this.resolver = contentResolver;
            this.listener = onMediaSavedListener;
            this.pictureFormat = str2;
        }

        /* access modifiers changed from: protected */
        public Uri doInBackground(Void... voidArr) {
            if (this.width == 0 || this.height == 0) {
                Options options = new Options();
                options.inJustDecodeBounds = true;
                byte[] bArr = this.data;
                BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
                this.width = options.outWidth;
                this.height = options.outHeight;
            }
            return Storage.addImage(this.resolver, this.title, this.date, this.loc, this.orientation, this.exif, this.data, this.width, this.height, this.pictureFormat);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Uri uri) {
            OnMediaSavedListener onMediaSavedListener = this.listener;
            if (onMediaSavedListener != null) {
                onMediaSavedListener.onMediaSaved(uri);
            }
            boolean isQueueFull = MediaSaveService.this.isQueueFull();
            MediaSaveService.access$022(MediaSaveService.this, (long) this.data.length);
            if (MediaSaveService.this.isQueueFull() != isQueueFull) {
                MediaSaveService.this.onQueueAvailable();
            }
        }
    }

    public interface Listener {
        void onQueueStatus(boolean z);
    }

    class LocalBinder extends Binder {
        LocalBinder() {
        }

        public MediaSaveService getService() {
            return MediaSaveService.this;
        }
    }

    private class MpoSaveTask extends AsyncTask<Void, Void, Uri> {
        private byte[] bayerImage;
        private byte[] csImage;
        private long date;
        private int height;
        private OnMediaSavedListener listener;
        private Location loc;
        private byte[] monoImage;
        private int orientation;
        private String pictureFormat;
        private ContentResolver resolver;
        private String title;
        private int width;

        public MpoSaveTask(byte[] bArr, byte[] bArr2, byte[] bArr3, int i, int i2, String str, long j, Location location, int i3, OnMediaSavedListener onMediaSavedListener, ContentResolver contentResolver, String str2) {
            this.csImage = bArr;
            this.bayerImage = bArr2;
            this.monoImage = bArr3;
            this.title = str;
            this.date = j;
            this.loc = location;
            this.width = i;
            this.height = i2;
            this.orientation = i3;
            this.resolver = contentResolver;
            this.listener = onMediaSavedListener;
            this.pictureFormat = str2;
        }

        /* access modifiers changed from: protected */
        public Uri doInBackground(Void... voidArr) {
            MpoData mpoData = new MpoData();
            MpoImageData mpoImageData = new MpoImageData(this.bayerImage, ByteOrder.BIG_ENDIAN);
            MpoImageData mpoImageData2 = new MpoImageData(this.monoImage, ByteOrder.BIG_ENDIAN);
            byte[] bArr = this.csImage;
            if (bArr == null) {
                mpoData.addAuxiliaryMpoImage(mpoImageData2);
                mpoData.setPrimaryMpoImage(mpoImageData);
            } else {
                MpoImageData mpoImageData3 = new MpoImageData(bArr, ByteOrder.BIG_ENDIAN);
                mpoData.addAuxiliaryMpoImage(mpoImageData);
                mpoData.addAuxiliaryMpoImage(mpoImageData2);
                mpoData.setPrimaryMpoImage(mpoImageData3);
            }
            String generateFilepath = Storage.generateFilepath(this.title, this.pictureFormat);
            int writeMpo = MpoInterface.writeMpo(mpoData, generateFilepath);
            File file = new File(generateFilepath);
            if (file.exists() && file.isFile()) {
                writeMpo = (int) file.length();
            }
            return Storage.addImage(this.resolver, this.title, this.date, this.loc, this.orientation, null, writeMpo, generateFilepath, this.width, this.height, this.pictureFormat);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Uri uri) {
            int i;
            OnMediaSavedListener onMediaSavedListener = this.listener;
            if (onMediaSavedListener != null) {
                onMediaSavedListener.onMediaSaved(uri);
            }
            boolean isQueueFull = MediaSaveService.this.isQueueFull();
            byte[] bArr = this.csImage;
            if (bArr == null) {
                i = 0;
            } else {
                i = bArr.length;
            }
            MediaSaveService.access$022(MediaSaveService.this, (long) (i + this.bayerImage.length + this.monoImage.length));
            if (MediaSaveService.this.isQueueFull() != isQueueFull) {
                MediaSaveService.this.onQueueAvailable();
            }
        }
    }

    public interface OnMediaSavedListener {
        void onMediaSaved(Uri uri);
    }

    private class RawImageSaveTask extends AsyncTask<Void, Void, Long> {
        private byte[] data;
        private String pictureFormat;
        private String title;

        public RawImageSaveTask(byte[] bArr, String str, String str2) {
            this.data = bArr;
            this.title = str;
            this.pictureFormat = str2;
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
        }

        /* access modifiers changed from: protected */
        public Long doInBackground(Void... voidArr) {
            return new Long(Storage.addRawImage(this.title, this.data, this.pictureFormat));
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Long l) {
            boolean isQueueFull = MediaSaveService.this.isQueueFull();
            MediaSaveService.access$022(MediaSaveService.this, (long) this.data.length);
            if (MediaSaveService.this.isQueueFull() != isQueueFull) {
                MediaSaveService.this.onQueueAvailable();
            }
        }
    }

    private class VideoSaveTask extends AsyncTask<Void, Void, Uri> {
        private long duration;
        private OnMediaSavedListener listener;
        private String path;
        private ContentResolver resolver;
        private ContentValues values;

        public VideoSaveTask(String str, long j, ContentValues contentValues, OnMediaSavedListener onMediaSavedListener, ContentResolver contentResolver) {
            this.path = str;
            this.duration = j;
            this.values = new ContentValues(contentValues);
            this.listener = onMediaSavedListener;
            this.resolver = contentResolver;
        }

        /* access modifiers changed from: protected */
        public Uri doInBackground(Void... voidArr) {
            Uri uri;
            String str = "Current video URI: ";
            this.values.put("_size", Long.valueOf(new File(this.path).length()));
            this.values.put("duration", Long.valueOf(this.duration));
            try {
                uri = this.resolver.insert(Uri.parse(MediaSaveService.VIDEO_BASE_URI), this.values);
                try {
                    String asString = this.values.getAsString("_data");
                    if (new File(this.path).renameTo(new File(asString))) {
                        this.path = asString;
                    }
                    this.resolver.update(uri, this.values, null, null);
                    String access$200 = MediaSaveService.TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append(str);
                    sb.append(uri);
                    Log.v(access$200, sb.toString());
                    return uri;
                } catch (Exception e) {
                    e = e;
                    try {
                        Log.e(MediaSaveService.TAG, "failed to add video to media store", e);
                        String access$2002 = MediaSaveService.TAG;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(str);
                        sb2.append(null);
                        Log.v(access$2002, sb2.toString());
                        return null;
                    } catch (Throwable th) {
                        th = th;
                        String access$2003 = MediaSaveService.TAG;
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append(str);
                        sb3.append(uri);
                        Log.v(access$2003, sb3.toString());
                        throw th;
                    }
                }
            } catch (Exception e2) {
                e = e2;
                uri = null;
                Log.e(MediaSaveService.TAG, "failed to add video to media store", e);
                String access$20022 = MediaSaveService.TAG;
                StringBuilder sb22 = new StringBuilder();
                sb22.append(str);
                sb22.append(null);
                Log.v(access$20022, sb22.toString());
                return null;
            } catch (Throwable th2) {
                th = th2;
                uri = null;
                String access$20032 = MediaSaveService.TAG;
                StringBuilder sb32 = new StringBuilder();
                sb32.append(str);
                sb32.append(uri);
                Log.v(access$20032, sb32.toString());
                throw th;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Uri uri) {
            OnMediaSavedListener onMediaSavedListener = this.listener;
            if (onMediaSavedListener != null) {
                onMediaSavedListener.onMediaSaved(uri);
            }
        }
    }

    private class XmpImageSaveTask extends AsyncTask<Void, Void, Uri> {
        private GImage bayer;
        private byte[] data;
        private long date;
        private ExifInterface exif;
        private GDepth gDepth;
        private int height;
        private OnMediaSavedListener listener;
        private Location loc;
        private byte[] mainImage;
        private int orientation;
        private String pictureFormat;
        private ContentResolver resolver;
        private String title;
        private int width;

        /* access modifiers changed from: protected */
        public void onPreExecute() {
        }

        public XmpImageSaveTask(byte[] bArr, GImage gImage, GDepth gDepth2, String str, long j, Location location, int i, int i2, int i3, ExifInterface exifInterface, ContentResolver contentResolver, OnMediaSavedListener onMediaSavedListener, String str2) {
            this.mainImage = bArr;
            this.gDepth = gDepth2;
            this.bayer = gImage;
            this.title = str;
            this.date = j;
            this.loc = location;
            this.width = i;
            this.height = i2;
            this.orientation = i3;
            this.exif = exifInterface;
            this.resolver = contentResolver;
            this.listener = onMediaSavedListener;
            this.pictureFormat = str2;
        }

        /* access modifiers changed from: protected */
        public Uri doInBackground(Void... voidArr) {
            this.data = embedGDepthAndBayerInClearSight(this.mainImage);
            if (this.data == null) {
                this.data = this.mainImage;
                Log.e(MediaSaveService.TAG, "embedGDepthAndBayerInClearSight fail");
            }
            if (this.width == 0 || this.height == 0) {
                Options options = new Options();
                options.inJustDecodeBounds = true;
                byte[] bArr = this.data;
                BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
                this.width = options.outWidth;
                this.height = options.outHeight;
            }
            return Storage.addImage(this.resolver, this.title, this.date, this.loc, this.orientation, this.exif, this.data, this.width, this.height, this.pictureFormat);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Uri uri) {
            OnMediaSavedListener onMediaSavedListener = this.listener;
            if (onMediaSavedListener != null) {
                onMediaSavedListener.onMediaSaved(uri);
            }
            boolean isQueueFull = MediaSaveService.this.isQueueFull();
            MediaSaveService.access$022(MediaSaveService.this, (long) this.data.length);
            if (MediaSaveService.this.isQueueFull() != isQueueFull) {
                MediaSaveService.this.onQueueAvailable();
            }
        }

        private byte[] embedGDepthAndBayerInClearSight(byte[] bArr) {
            Log.d(MediaSaveService.TAG, "embedGDepthInClearSight");
            if (bArr == null || (this.gDepth == null && this.bayer == null)) {
                Log.d(MediaSaveService.TAG, "clearSightImageBytes is null");
                return null;
            }
            XMPMeta createXMPMeta = XmpUtil.createXMPMeta();
            try {
                GDepth gDepth2 = this.gDepth;
                String str = "Mime";
                String str2 = GDepth.NAMESPACE_URL;
                if (gDepth2 != null) {
                    createXMPMeta.setProperty(str2, str, this.gDepth.getMime());
                    createXMPMeta.setProperty(str2, GDepth.PROPERTY_NEAR, Integer.valueOf(this.gDepth.getNear()));
                    createXMPMeta.setProperty(str2, GDepth.PROPERTY_FAR, Integer.valueOf(this.gDepth.getFar()));
                    createXMPMeta.setProperty(str2, GDepth.PROPERTY_FORMAT, this.gDepth.getFormat());
                    Rect roi = this.gDepth.getRoi();
                    createXMPMeta.setProperty(str2, GDepth.PROPERTY_ROI_X, Integer.valueOf(roi.left));
                    createXMPMeta.setProperty(str2, GDepth.PROPERTY_ROI_Y, Integer.valueOf(roi.top));
                    createXMPMeta.setProperty(str2, GDepth.PROPERTY_ROI_WIDTH, Integer.valueOf(roi.width()));
                    createXMPMeta.setProperty(str2, GDepth.PROPERTY_ROI_HEIGHT, Integer.valueOf(roi.height()));
                }
                GImage gImage = this.bayer;
                String str3 = GImage.NAMESPACE_URL;
                if (gImage != null) {
                    createXMPMeta.setProperty(str3, str, this.bayer.getMime());
                }
                XMPMeta createXMPMeta2 = XmpUtil.createXMPMeta();
                try {
                    String str4 = "Data";
                    if (this.gDepth != null) {
                        createXMPMeta2.setProperty(str2, str4, this.gDepth.getData());
                    }
                    if (this.bayer != null) {
                        createXMPMeta2.setProperty(str3, str4, this.bayer.getData());
                    }
                } catch (XMPException e) {
                    Log.d(MediaSaveService.TAG, "create extended XMPMeta error", e);
                }
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                if (XmpUtil.writeXMPMeta(byteArrayInputStream, byteArrayOutputStream, createXMPMeta, createXMPMeta2)) {
                    return byteArrayOutputStream.toByteArray();
                }
                Log.e(MediaSaveService.TAG, "embedGDepthInClearSight failure ");
                return null;
            } catch (XMPException e2) {
                Log.d(MediaSaveService.TAG, "create XMPMeta error", e2);
                return null;
            }
        }
    }

    public void onDestroy() {
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        return 1;
    }

    static /* synthetic */ long access$022(MediaSaveService mediaSaveService, long j) {
        long j2 = mediaSaveService.mMemoryUse - j;
        mediaSaveService.mMemoryUse = j2;
        return j2;
    }

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("CAM_");
        sb.append(MediaSaveService.class.getSimpleName());
        TAG = sb.toString();
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public void onCreate() {
        this.mMemoryUse = 0;
    }

    public boolean isQueueFull() {
        return this.mMemoryUse >= ((long) SAVE_TASK_MEMORY_LIMIT);
    }

    public void addMpoImage(byte[] bArr, byte[] bArr2, byte[] bArr3, int i, int i2, String str, long j, Location location, int i3, OnMediaSavedListener onMediaSavedListener, ContentResolver contentResolver, String str2) {
        int i4;
        byte[] bArr4;
        byte[] bArr5 = bArr;
        if (isQueueFull()) {
            Log.e(TAG, "Cannot add image when the queue is full");
            return;
        }
        MpoSaveTask mpoSaveTask = r0;
        byte[] bArr6 = bArr5;
        MpoSaveTask mpoSaveTask2 = new MpoSaveTask(bArr, bArr2, bArr3, i, i2, str, j, location, i3, onMediaSavedListener, contentResolver, str2);
        if (bArr6 == null) {
            bArr4 = bArr2;
            i4 = 0;
        } else {
            i4 = bArr6.length;
            bArr4 = bArr2;
        }
        this.mMemoryUse += (long) (i4 + bArr4.length + bArr3.length);
        if (isQueueFull()) {
            onQueueFull();
        }
        mpoSaveTask.execute(new Void[0]);
    }

    public void addImage(byte[] bArr, String str, long j, Location location, int i, int i2, int i3, ExifInterface exifInterface, OnMediaSavedListener onMediaSavedListener, ContentResolver contentResolver, String str2) {
        Location location2;
        Location location3 = location;
        if (isQueueFull()) {
            Log.e(TAG, "Cannot add image when the queue is full");
            return;
        }
        if (location3 == null) {
            location2 = null;
        } else {
            location2 = new Location(location3);
        }
        byte[] bArr2 = bArr;
        ImageSaveTask imageSaveTask = new ImageSaveTask(bArr2, str, j, location2, i, i2, i3, exifInterface, contentResolver, onMediaSavedListener, str2);
        this.mMemoryUse += (long) bArr2.length;
        if (isQueueFull()) {
            onQueueFull();
        }
        imageSaveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public void addRawImage(byte[] bArr, String str, String str2) {
        if (isQueueFull()) {
            Log.e(TAG, "Cannot add image when the queue is full");
            return;
        }
        RawImageSaveTask rawImageSaveTask = new RawImageSaveTask(bArr, str, str2);
        this.mMemoryUse += (long) bArr.length;
        if (isQueueFull()) {
            onQueueFull();
        }
        rawImageSaveTask.execute(new Void[0]);
    }

    public void addHEIFImage(String str, String str2, long j, Location location, int i, int i2, int i3, ExifInterface exifInterface, ContentResolver contentResolver, OnMediaSavedListener onMediaSavedListener, int i4, String str3) {
        if (isQueueFull()) {
            Log.e(TAG, "Cannot add image when the queue is full");
            return;
        }
        HEIFImageSaveTask hEIFImageSaveTask = new HEIFImageSaveTask(str, str2, j, location, i, i2, i3, exifInterface, contentResolver, onMediaSavedListener, i4, str3);
        hEIFImageSaveTask.execute(new Void[0]);
    }

    public void addXmpImage(byte[] bArr, GImage gImage, GDepth gDepth, String str, long j, Location location, int i, int i2, int i3, ExifInterface exifInterface, OnMediaSavedListener onMediaSavedListener, ContentResolver contentResolver, String str2) {
        Location location2;
        Location location3 = location;
        if (isQueueFull()) {
            Log.e(TAG, "Cannot add image when the queue is full");
            return;
        }
        if (location3 == null) {
            location2 = null;
        } else {
            location2 = new Location(location3);
        }
        XmpImageSaveTask xmpImageSaveTask = r0;
        XmpImageSaveTask xmpImageSaveTask2 = new XmpImageSaveTask(bArr, gImage, gDepth, str, j, location2, i, i2, i3, exifInterface, contentResolver, onMediaSavedListener, str2);
        this.mMemoryUse += (long) bArr.length;
        if (isQueueFull()) {
            onQueueFull();
        }
        xmpImageSaveTask.execute(new Void[0]);
    }

    public void addImage(byte[] bArr, String str, long j, Location location, int i, ExifInterface exifInterface, OnMediaSavedListener onMediaSavedListener, ContentResolver contentResolver) {
        addImage(bArr, str, j, location, 0, 0, i, exifInterface, onMediaSavedListener, contentResolver, PhotoModule.PIXEL_FORMAT_JPEG);
    }

    public void addImage(byte[] bArr, String str, Location location, int i, int i2, int i3, ExifInterface exifInterface, OnMediaSavedListener onMediaSavedListener, ContentResolver contentResolver) {
        addImage(bArr, str, System.currentTimeMillis(), location, i, i2, i3, exifInterface, onMediaSavedListener, contentResolver, PhotoModule.PIXEL_FORMAT_JPEG);
    }

    public void addVideo(String str, long j, ContentValues contentValues, OnMediaSavedListener onMediaSavedListener, ContentResolver contentResolver) {
        VideoSaveTask videoSaveTask = new VideoSaveTask(str, j, contentValues, onMediaSavedListener, contentResolver);
        videoSaveTask.execute(new Void[0]);
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
        if (listener != null) {
            listener.onQueueStatus(isQueueFull());
        }
    }

    private void onQueueFull() {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onQueueStatus(true);
        }
    }

    /* access modifiers changed from: private */
    public void onQueueAvailable() {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onQueueStatus(false);
        }
    }
}
