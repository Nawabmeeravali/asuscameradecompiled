package com.android.camera.data;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.camera.data.PanoramaMetadataLoader.PanoramaMetadataCallback;
import com.android.camera.p004ui.FilmStripView.ImageData.PanoramaSupportCallback;
import com.android.camera.tinyplanet.TinyPlanetFragment;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.PhotoSphereHelper.PanoramaMetadata;
import com.android.camera.util.PhotoSphereHelper.PanoramaViewHelper;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import org.codeaurora.snapcam.C0905R;

public abstract class LocalMediaData implements LocalData {
    protected final long mContentId;
    protected final long mDateModifiedInSeconds;
    protected final long mDateTakenInSeconds;
    protected final int mHeight;
    protected final double mLatitude;
    protected final double mLongitude;
    protected final String mMimeType;
    protected PanoramaMetadata mPanoramaMetadata;
    protected PanoramaMetadataLoader mPanoramaMetadataLoader = null;
    protected final String mPath;
    protected final long mSizeInBytes;
    protected final String mTitle;
    protected Boolean mUsing = Boolean.valueOf(false);
    protected final int mWidth;

    protected abstract class BitmapLoadTask extends AsyncTask<Void, Void, Bitmap> {
        protected ImageView mView;

        protected BitmapLoadTask(ImageView imageView) {
            this.mView = imageView;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
            if (LocalMediaData.this.isUsing()) {
                if (bitmap == null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Failed decoding bitmap for file:");
                    sb.append(LocalMediaData.this.mPath);
                    Log.e(LocalData.TAG, sb.toString());
                    return;
                }
                BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                this.mView.setScaleType(ScaleType.FIT_XY);
                this.mView.setImageDrawable(bitmapDrawable);
            }
        }
    }

    public static final class PhotoData extends LocalMediaData {
        public static final int COL_DATA = 5;
        public static final int COL_DATE_MODIFIED = 4;
        public static final int COL_DATE_TAKEN = 3;
        public static final int COL_HEIGHT = 8;
        public static final int COL_ID = 0;
        public static final int COL_LATITUDE = 10;
        public static final int COL_LONGITUDE = 11;
        public static final int COL_MIME_TYPE = 2;
        public static final int COL_ORIENTATION = 6;
        public static final int COL_SIZE = 9;
        public static final int COL_TITLE = 1;
        public static final int COL_WIDTH = 7;
        static final Uri CONTENT_URI = Media.EXTERNAL_CONTENT_URI;
        /* access modifiers changed from: private */
        public static final byte[] DECODE_TEMP_STORAGE = new byte[32768];
        static final String[] QUERY_PROJECTION = {"_id", TinyPlanetFragment.ARGUMENT_TITLE, "mime_type", "datetaken", "date_modified", "_data", "orientation", "width", "height", "_size", "latitude", "longitude"};
        private static final String TAG = "CAM_PhotoData";
        private static final int mSupportedDataActions = 2;
        private static final int mSupportedUIActions = 7;
        /* access modifiers changed from: private */
        public final int mOrientation;

        private final class PhotoBitmapLoadTask extends BitmapLoadTask {
            private final LocalDataAdapter mAdapter;
            private final int mDecodeHeight;
            private final int mDecodeWidth;
            private boolean mNeedsRefresh;
            private final ContentResolver mResolver;

            public PhotoBitmapLoadTask(ImageView imageView, int i, int i2, ContentResolver contentResolver, LocalDataAdapter localDataAdapter) {
                super(imageView);
                this.mDecodeWidth = i;
                this.mDecodeHeight = i2;
                this.mResolver = contentResolver;
                this.mAdapter = localDataAdapter;
            }

            /* access modifiers changed from: protected */
            /* JADX WARNING: Code restructure failed: missing block: B:9:0x0045, code lost:
                if (r0 > 0) goto L_0x004a;
             */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public android.graphics.Bitmap doInBackground(java.lang.Void... r12) {
                /*
                    r11 = this;
                    com.android.camera.data.LocalMediaData$PhotoData r12 = com.android.camera.data.LocalMediaData.PhotoData.this
                    int r0 = r12.mWidth
                    int r1 = r11.mDecodeWidth
                    r2 = 1
                    if (r0 > r1) goto L_0x0012
                    int r12 = r12.mHeight
                    int r0 = r11.mDecodeHeight
                    if (r12 <= r0) goto L_0x0010
                    goto L_0x0012
                L_0x0010:
                    r12 = r2
                    goto L_0x0030
                L_0x0012:
                    com.android.camera.data.LocalMediaData$PhotoData r12 = com.android.camera.data.LocalMediaData.PhotoData.this
                    int r12 = r12.mHeight
                    float r12 = (float) r12
                    int r0 = r11.mDecodeHeight
                    float r0 = (float) r0
                    float r12 = r12 / r0
                    int r12 = java.lang.Math.round(r12)
                    com.android.camera.data.LocalMediaData$PhotoData r0 = com.android.camera.data.LocalMediaData.PhotoData.this
                    int r0 = r0.mWidth
                    float r0 = (float) r0
                    int r1 = r11.mDecodeWidth
                    float r1 = (float) r1
                    float r0 = r0 / r1
                    int r0 = java.lang.Math.round(r0)
                    int r12 = java.lang.Math.max(r12, r0)
                L_0x0030:
                    android.graphics.BitmapFactory$Options r0 = new android.graphics.BitmapFactory$Options
                    r0.<init>()
                    r0.inJustDecodeBounds = r2
                    com.android.camera.data.LocalMediaData$PhotoData r1 = com.android.camera.data.LocalMediaData.PhotoData.this
                    java.lang.String r1 = r1.mPath
                    android.graphics.BitmapFactory.decodeFile(r1, r0)
                    int r1 = r0.outWidth
                    r3 = 0
                    if (r1 <= 0) goto L_0x0048
                    int r0 = r0.outHeight
                    if (r0 <= 0) goto L_0x0048
                    goto L_0x004a
                L_0x0048:
                    r0 = r3
                    r1 = r0
                L_0x004a:
                    r3 = 0
                    if (r1 <= 0) goto L_0x009f
                    if (r0 <= 0) goto L_0x009f
                    com.android.camera.data.LocalMediaData$PhotoData r4 = com.android.camera.data.LocalMediaData.PhotoData.this
                    int r5 = r4.mWidth
                    if (r1 != r5) goto L_0x0059
                    int r4 = r4.mHeight
                    if (r0 == r4) goto L_0x009f
                L_0x0059:
                    android.content.ContentValues r12 = new android.content.ContentValues
                    r12.<init>()
                    java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
                    java.lang.String r4 = "width"
                    r12.put(r4, r1)
                    java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
                    java.lang.String r1 = "height"
                    r12.put(r1, r0)
                    android.content.ContentResolver r0 = r11.mResolver
                    com.android.camera.data.LocalMediaData$PhotoData r1 = com.android.camera.data.LocalMediaData.PhotoData.this
                    android.net.Uri r1 = r1.getContentUri()
                    r0.update(r1, r12, r3, r3)
                    r11.mNeedsRefresh = r2
                    java.lang.StringBuilder r12 = new java.lang.StringBuilder
                    r12.<init>()
                    java.lang.String r0 = "Uri "
                    r12.append(r0)
                    com.android.camera.data.LocalMediaData$PhotoData r11 = com.android.camera.data.LocalMediaData.PhotoData.this
                    android.net.Uri r11 = r11.getContentUri()
                    r12.append(r11)
                    java.lang.String r11 = " has been updated with correct size!"
                    r12.append(r11)
                    java.lang.String r11 = r12.toString()
                    java.lang.String r12 = "CAM_PhotoData"
                    android.util.Log.w(r12, r11)
                    return r3
                L_0x009f:
                    android.graphics.BitmapFactory$Options r0 = new android.graphics.BitmapFactory$Options
                    r0.<init>()
                    r0.inSampleSize = r12
                    byte[] r12 = com.android.camera.data.LocalMediaData.PhotoData.DECODE_TEMP_STORAGE
                    r0.inTempStorage = r12
                    boolean r12 = r11.isCancelled()
                    if (r12 != 0) goto L_0x00fd
                    com.android.camera.data.LocalMediaData$PhotoData r12 = com.android.camera.data.LocalMediaData.PhotoData.this
                    boolean r12 = r12.isUsing()
                    if (r12 != 0) goto L_0x00bb
                    goto L_0x00fd
                L_0x00bb:
                    com.android.camera.data.LocalMediaData$PhotoData r12 = com.android.camera.data.LocalMediaData.PhotoData.this
                    java.lang.String r12 = r12.mPath
                    android.graphics.Bitmap r4 = android.graphics.BitmapFactory.decodeFile(r12, r0)
                    com.android.camera.data.LocalMediaData$PhotoData r12 = com.android.camera.data.LocalMediaData.PhotoData.this
                    int r12 = r12.mOrientation
                    if (r12 == 0) goto L_0x00fc
                    if (r4 == 0) goto L_0x00fc
                    boolean r12 = r11.isCancelled()
                    if (r12 != 0) goto L_0x00fb
                    com.android.camera.data.LocalMediaData$PhotoData r12 = com.android.camera.data.LocalMediaData.PhotoData.this
                    boolean r12 = r12.isUsing()
                    if (r12 != 0) goto L_0x00dc
                    goto L_0x00fb
                L_0x00dc:
                    android.graphics.Matrix r9 = new android.graphics.Matrix
                    r9.<init>()
                    com.android.camera.data.LocalMediaData$PhotoData r11 = com.android.camera.data.LocalMediaData.PhotoData.this
                    int r11 = r11.mOrientation
                    float r11 = (float) r11
                    r9.setRotate(r11)
                    r5 = 0
                    r6 = 0
                    int r7 = r4.getWidth()
                    int r8 = r4.getHeight()
                    r10 = 0
                    android.graphics.Bitmap r4 = android.graphics.Bitmap.createBitmap(r4, r5, r6, r7, r8, r9, r10)
                    goto L_0x00fc
                L_0x00fb:
                    return r3
                L_0x00fc:
                    return r4
                L_0x00fd:
                    return r3
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.camera.data.LocalMediaData.PhotoData.PhotoBitmapLoadTask.doInBackground(java.lang.Void[]):android.graphics.Bitmap");
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (this.mNeedsRefresh) {
                    LocalDataAdapter localDataAdapter = this.mAdapter;
                    if (localDataAdapter != null) {
                        localDataAdapter.refresh(this.mResolver, PhotoData.this.getContentUri());
                    }
                }
            }
        }

        public int getViewType() {
            return 2;
        }

        public boolean isDataActionSupported(int i) {
            return (i & 2) == i;
        }

        public boolean isPhoto() {
            return true;
        }

        public boolean isUIActionSupported(int i) {
            return (i & 7) == i;
        }

        public PhotoData(long j, String str, String str2, long j2, long j3, String str3, int i, int i2, int i3, long j4, double d, double d2) {
            super(j, str, str2, j2, j3, str3, i2, i3, j4, d, d2);
            this.mOrientation = i;
        }

        static PhotoData buildFromCursor(Cursor cursor) {
            int i;
            int i2;
            Cursor cursor2 = cursor;
            long j = cursor2.getLong(0);
            String string = cursor2.getString(1);
            String string2 = cursor2.getString(2);
            long j2 = cursor2.getLong(3);
            long j3 = cursor2.getLong(4);
            String string3 = cursor2.getString(5);
            int i3 = cursor2.getInt(6);
            int i4 = cursor2.getInt(7);
            int i5 = cursor2.getInt(8);
            if (i4 <= 0 || i5 <= 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("Zero dimension in ContentResolver for ");
                sb.append(string3);
                sb.append(":");
                sb.append(i4);
                sb.append("x");
                sb.append(i5);
                String sb2 = sb.toString();
                String str = TAG;
                Log.w(str, sb2);
                Options options = new Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(string3, options);
                int i6 = options.outWidth;
                if (i6 > 0) {
                    int i7 = options.outHeight;
                    if (i7 > 0) {
                        i2 = i6;
                        i = i7;
                    }
                }
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Dimension decode failed for ");
                sb3.append(string3);
                Log.w(str, sb3.toString());
                Bitmap decodeFile = BitmapFactory.decodeFile(string3);
                if (decodeFile == null) {
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append("PhotoData skipped. Decoding ");
                    sb4.append(string3);
                    sb4.append("failed.");
                    Log.w(str, sb4.toString());
                    return null;
                }
                int width = decodeFile.getWidth();
                int height = decodeFile.getHeight();
                if (width == 0 || height == 0) {
                    StringBuilder sb5 = new StringBuilder();
                    sb5.append("PhotoData skipped. Bitmap size 0 for ");
                    sb5.append(string3);
                    Log.w(str, sb5.toString());
                    return null;
                }
                i2 = width;
                i = height;
            } else {
                i = i5;
                i2 = i4;
            }
            PhotoData photoData = new PhotoData(j, string, string2, j2, j3, string3, i3, i2, i, cursor2.getLong(9), cursor2.getDouble(10), cursor2.getDouble(11));
            return photoData;
        }

        public int getOrientation() {
            return this.mOrientation;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Photo:,data=");
            sb.append(this.mPath);
            sb.append(",mimeType=");
            sb.append(this.mMimeType);
            sb.append(",");
            sb.append(this.mWidth);
            sb.append("x");
            sb.append(this.mHeight);
            sb.append(",orientation=");
            sb.append(this.mOrientation);
            sb.append(",date=");
            sb.append(new Date(this.mDateTakenInSeconds));
            return sb.toString();
        }

        public boolean delete(Context context) {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = CONTENT_URI;
            StringBuilder sb = new StringBuilder();
            sb.append("_id=");
            sb.append(this.mContentId);
            contentResolver.delete(uri, sb.toString(), null);
            return LocalMediaData.super.delete(context);
        }

        public Uri getContentUri() {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(this.mContentId)).build();
        }

        public MediaDetails getMediaDetails(Context context) {
            MediaDetails mediaDetails = LocalMediaData.super.getMediaDetails(context);
            MediaDetails.extractExifInfo(mediaDetails, this.mPath);
            mediaDetails.addDetail(7, Integer.valueOf(this.mOrientation));
            return mediaDetails;
        }

        public int getLocalDataType() {
            PanoramaMetadata panoramaMetadata = this.mPanoramaMetadata;
            if (panoramaMetadata != null) {
                if (panoramaMetadata.mIsPanorama360) {
                    return 6;
                }
                if (panoramaMetadata.mUsePanoramaViewer) {
                    return 5;
                }
            }
            return 3;
        }

        public LocalData refresh(ContentResolver contentResolver) {
            Cursor query = contentResolver.query(getContentUri(), QUERY_PROJECTION, null, null, null);
            if (query == null || !query.moveToFirst()) {
                return null;
            }
            PhotoData buildFromCursor = buildFromCursor(query);
            query.close();
            return buildFromCursor;
        }

        public boolean rotate90Degrees(Context context, LocalDataAdapter localDataAdapter, int i, boolean z) {
            new RotationTask(context, localDataAdapter, i, z).execute(new LocalData[]{this});
            return true;
        }
    }

    public static final class VideoData extends LocalMediaData {
        public static final int COL_DATA = 5;
        public static final int COL_DATE_MODIFIED = 4;
        public static final int COL_DATE_TAKEN = 3;
        public static final int COL_DURATION = 12;
        public static final int COL_HEIGHT = 7;
        public static final int COL_ID = 0;
        public static final int COL_LATITUDE = 10;
        public static final int COL_LONGITUDE = 11;
        public static final int COL_MIME_TYPE = 2;
        public static final int COL_RESOLUTION = 8;
        public static final int COL_SIZE = 9;
        public static final int COL_TITLE = 1;
        public static final int COL_WIDTH = 6;
        static final Uri CONTENT_URI = Video.Media.EXTERNAL_CONTENT_URI;
        static final String[] QUERY_PROJECTION = {"_id", TinyPlanetFragment.ARGUMENT_TITLE, "mime_type", "datetaken", "date_modified", "_data", "width", "height", "resolution", "_size", "latitude", "longitude", "duration"};
        private static final int mSupportedDataActions = 3;
        private static final int mSupportedUIActions = 3;
        private long mDurationInSeconds;

        private final class VideoBitmapLoadTask extends BitmapLoadTask {
            public VideoBitmapLoadTask(ImageView imageView) {
                super(imageView);
            }

            /* access modifiers changed from: protected */
            public Bitmap doInBackground(Void... voidArr) {
                Bitmap bitmap = null;
                if (!isCancelled() && VideoData.this.isUsing()) {
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    try {
                        mediaMetadataRetriever.setDataSource(VideoData.this.mPath);
                        byte[] embeddedPicture = mediaMetadataRetriever.getEmbeddedPicture();
                        if (!isCancelled() && VideoData.this.isUsing()) {
                            if (embeddedPicture != null) {
                                bitmap = BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length);
                            }
                            if (bitmap == null) {
                                bitmap = mediaMetadataRetriever.getFrameAtTime();
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("MediaMetadataRetriever.setDataSource() fail:");
                        sb.append(e.getMessage());
                        Log.e(LocalData.TAG, sb.toString());
                    }
                    mediaMetadataRetriever.release();
                }
                return bitmap;
            }
        }

        public int getLocalDataType() {
            return 4;
        }

        public int getViewType() {
            return 2;
        }

        public boolean isDataActionSupported(int i) {
            return (i & 3) == i;
        }

        public boolean isPhoto() {
            return false;
        }

        public boolean isUIActionSupported(int i) {
            return (i & 3) == i;
        }

        public VideoData(long j, String str, String str2, long j2, long j3, String str3, int i, int i2, long j4, double d, double d2, long j5) {
            super(j, str, str2, j2, j3, str3, i, i2, j4, d, d2);
            this.mDurationInSeconds = j5;
        }

        static VideoData buildFromCursor(Cursor cursor) {
            int i;
            Cursor cursor2 = cursor;
            long j = cursor2.getLong(0);
            String string = cursor2.getString(1);
            String string2 = cursor2.getString(2);
            long j2 = cursor2.getLong(3);
            long j3 = cursor2.getLong(4);
            String string3 = cursor2.getString(5);
            int i2 = cursor2.getInt(6);
            int i3 = cursor2.getInt(7);
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            File file = new File(string3);
            boolean exists = file.exists();
            String str = LocalData.TAG;
            if (!exists || file.length() <= 0) {
                Log.e(str, "Invalid video file");
                mediaMetadataRetriever.release();
                return null;
            }
            try {
                mediaMetadataRetriever.setDataSource(string3);
                String extractMetadata = mediaMetadataRetriever.extractMetadata(24);
                if (i2 == 0 || i3 == 0) {
                    String extractMetadata2 = mediaMetadataRetriever.extractMetadata(18);
                    if (extractMetadata2 == null) {
                        i2 = 0;
                    } else {
                        i2 = Integer.parseInt(extractMetadata2);
                    }
                    String extractMetadata3 = mediaMetadataRetriever.extractMetadata(19);
                    if (extractMetadata3 == null) {
                        i3 = 0;
                    } else {
                        i3 = Integer.parseInt(extractMetadata3);
                    }
                }
                mediaMetadataRetriever.release();
                if (i2 == 0 || i3 == 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unable to retrieve dimension of video:");
                    sb.append(string3);
                    Log.e(str, sb.toString());
                    return null;
                }
                if (extractMetadata == null || (!extractMetadata.equals("90") && !extractMetadata.equals("270"))) {
                    i = i3;
                    i3 = i2;
                } else {
                    i = i2;
                }
                VideoData videoData = new VideoData(j, string, string2, j2, j3, string3, i3, i, cursor2.getLong(9), cursor2.getDouble(10), cursor2.getDouble(11), cursor2.getLong(12) / 1000);
                return videoData;
            } catch (RuntimeException e) {
                RuntimeException runtimeException = e;
                mediaMetadataRetriever.release();
                StringBuilder sb2 = new StringBuilder();
                sb2.append("MediaMetadataRetriever.setDataSource() fail:");
                sb2.append(runtimeException.getMessage());
                Log.e(str, sb2.toString());
                return null;
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Video:,data=");
            sb.append(this.mPath);
            sb.append(",mimeType=");
            sb.append(this.mMimeType);
            sb.append(",");
            sb.append(this.mWidth);
            sb.append("x");
            sb.append(this.mHeight);
            sb.append(",date=");
            sb.append(new Date(this.mDateTakenInSeconds));
            return sb.toString();
        }

        public boolean delete(Context context) {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = CONTENT_URI;
            StringBuilder sb = new StringBuilder();
            sb.append("_id=");
            sb.append(this.mContentId);
            contentResolver.delete(uri, sb.toString(), null);
            return LocalMediaData.super.delete(context);
        }

        public Uri getContentUri() {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(this.mContentId)).build();
        }

        public MediaDetails getMediaDetails(Context context) {
            MediaDetails mediaDetails = LocalMediaData.super.getMediaDetails(context);
            mediaDetails.addDetail(8, MediaDetails.formatDuration(context, this.mDurationInSeconds));
            return mediaDetails;
        }

        public LocalData refresh(ContentResolver contentResolver) {
            Cursor query = contentResolver.query(getContentUri(), QUERY_PROJECTION, null, null, null);
            if (query == null || !query.moveToFirst()) {
                return null;
            }
            VideoData buildFromCursor = buildFromCursor(query);
            query.close();
            return buildFromCursor;
        }

        public View getView(final Activity activity, int i, int i2, Drawable drawable, LocalDataAdapter localDataAdapter) {
            ImageView imageView = new ImageView(activity);
            imageView.setLayoutParams(new LayoutParams(-1, -1, 17));
            fillImageView(activity, imageView, i, i2, drawable, localDataAdapter);
            ImageView imageView2 = new ImageView(activity);
            imageView2.setImageResource(C0905R.C0906drawable.ic_control_play);
            imageView2.setScaleType(ScaleType.CENTER);
            imageView2.setLayoutParams(new LayoutParams(-2, -2, 17));
            imageView2.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    CameraUtil.playVideo(activity, VideoData.this.getContentUri(), VideoData.this.mTitle);
                }
            });
            FrameLayout frameLayout = new FrameLayout(activity);
            frameLayout.addView(imageView);
            frameLayout.addView(imageView2);
            return frameLayout;
        }

        public boolean rotate90Degrees(Context context, LocalDataAdapter localDataAdapter, int i, boolean z) {
            Log.e(LocalData.TAG, "Unexpected call in rotate90Degrees()");
            return false;
        }
    }

    public boolean canSwipeInFullScreen() {
        return true;
    }

    public int getOrientation() {
        return 0;
    }

    public abstract int getViewType();

    public boolean isDataActionSupported(int i) {
        return false;
    }

    public boolean isUIActionSupported(int i) {
        return false;
    }

    public void onFullScreen(boolean z) {
    }

    public LocalMediaData(long j, String str, String str2, long j2, long j3, String str3, int i, int i2, long j4, double d, double d2) {
        this.mContentId = j;
        String str4 = str;
        this.mTitle = new String(str);
        String str5 = str2;
        this.mMimeType = new String(str2);
        this.mDateTakenInSeconds = j2;
        this.mDateModifiedInSeconds = j3;
        String str6 = str3;
        this.mPath = new String(str3);
        this.mWidth = i;
        this.mHeight = i2;
        this.mSizeInBytes = j4;
        this.mLatitude = d;
        this.mLongitude = d2;
    }

    public long getDateTaken() {
        return this.mDateTakenInSeconds;
    }

    public long getDateModified() {
        return this.mDateModifiedInSeconds;
    }

    public long getContentId() {
        return this.mContentId;
    }

    public String getTitle() {
        return new String(this.mTitle);
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public String getPath() {
        return this.mPath;
    }

    public long getSizeInBytes() {
        return this.mSizeInBytes;
    }

    public boolean delete(Context context) {
        return new File(this.mPath).delete();
    }

    public void viewPhotoSphere(PanoramaViewHelper panoramaViewHelper) {
        panoramaViewHelper.showPanorama(getContentUri());
    }

    public void isPhotoSphere(Context context, final PanoramaSupportCallback panoramaSupportCallback) {
        PanoramaMetadata panoramaMetadata = this.mPanoramaMetadata;
        if (panoramaMetadata != null) {
            panoramaSupportCallback.panoramaInfoAvailable(panoramaMetadata.mUsePanoramaViewer, panoramaMetadata.mIsPanorama360);
        }
        if (this.mPanoramaMetadataLoader == null) {
            this.mPanoramaMetadataLoader = new PanoramaMetadataLoader(getContentUri());
        }
        this.mPanoramaMetadataLoader.getPanoramaMetadata(context, new PanoramaMetadataCallback() {
            public void onPanoramaMetadataLoaded(PanoramaMetadata panoramaMetadata) {
                LocalMediaData localMediaData = LocalMediaData.this;
                localMediaData.mPanoramaMetadata = panoramaMetadata;
                localMediaData.mPanoramaMetadataLoader = null;
                panoramaSupportCallback.panoramaInfoAvailable(panoramaMetadata.mUsePanoramaViewer, panoramaMetadata.mIsPanorama360);
            }
        });
    }

    /* access modifiers changed from: protected */
    public ImageView fillImageView(Context context, ImageView imageView, int i, int i2, Drawable drawable, LocalDataAdapter localDataAdapter) {
        imageView.setScaleType(ScaleType.FIT_XY);
        imageView.setImageDrawable(drawable);
        return imageView;
    }

    public View getView(Activity activity, int i, int i2, Drawable drawable, LocalDataAdapter localDataAdapter) {
        return fillImageView(activity, new ImageView(activity), i, i2, drawable, localDataAdapter);
    }

    public void prepare() {
        synchronized (this.mUsing) {
            this.mUsing = Boolean.valueOf(true);
        }
    }

    public void recycle() {
        synchronized (this.mUsing) {
            this.mUsing = Boolean.valueOf(false);
        }
    }

    public double[] getLatLong() {
        if (this.mLatitude == 0.0d && this.mLongitude == 0.0d) {
            return null;
        }
        return new double[]{this.mLatitude, this.mLongitude};
    }

    /* access modifiers changed from: protected */
    public boolean isUsing() {
        boolean booleanValue;
        synchronized (this.mUsing) {
            booleanValue = this.mUsing.booleanValue();
        }
        return booleanValue;
    }

    public String getMimeType() {
        return this.mMimeType;
    }

    public MediaDetails getMediaDetails(Context context) {
        DateFormat dateTimeInstance = DateFormat.getDateTimeInstance();
        MediaDetails mediaDetails = new MediaDetails();
        mediaDetails.addDetail(1, this.mTitle);
        mediaDetails.addDetail(5, Integer.valueOf(this.mWidth));
        mediaDetails.addDetail(6, Integer.valueOf(this.mHeight));
        mediaDetails.addDetail(200, this.mPath);
        mediaDetails.addDetail(3, dateTimeInstance.format(new Date(this.mDateModifiedInSeconds * 1000)));
        long j = this.mSizeInBytes;
        if (j > 0) {
            mediaDetails.addDetail(10, Long.valueOf(j));
        }
        if (!(this.mLatitude == 0.0d || this.mLongitude == 0.0d)) {
            mediaDetails.addDetail(4, String.format(Locale.getDefault(), "%f, %f", new Object[]{Double.valueOf(this.mLatitude), Double.valueOf(this.mLongitude)}));
        }
        return mediaDetails;
    }
}
