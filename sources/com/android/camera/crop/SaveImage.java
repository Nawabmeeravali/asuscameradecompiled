package com.android.camera.crop;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import com.android.camera.exif.ExifInterface;
import com.android.camera.tinyplanet.TinyPlanetFragment;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class SaveImage {
    private static final String AUX_DIR_NAME = ".aux";
    public static final String DEFAULT_SAVE_DIRECTORY = "EditedOnlinePhotos";
    private static final String LOGTAG = "SaveImage";
    public static final int MAX_PROCESSING_STEPS = 6;
    private static final String POSTFIX_JPG = ".jpg";
    private static final String PREFIX_IMG = "IMG";
    private static final String PREFIX_PANO = "PANO";
    private static final String TIME_STAMP_NAME = "_yyyyMMdd_HHmmss";
    private final Callback mCallback;
    private final Context mContext;
    private int mCurrentProcessingStep = 1;
    private final File mDestinationFile;
    private final Bitmap mPreviewImage;
    private final Uri mSelectedImageUri;
    private final Uri mSourceUri;

    public interface Callback {
        void onProgress(int i, int i2);
    }

    public interface ContentResolverQueryCallback {
        void onCursorResult(Cursor cursor);
    }

    public SaveImage(Context context, Uri uri, Uri uri2, File file, Bitmap bitmap, Callback callback) {
        this.mContext = context;
        this.mSourceUri = uri;
        this.mCallback = callback;
        this.mPreviewImage = bitmap;
        if (file == null) {
            this.mDestinationFile = getNewFile(context, uri2);
        } else {
            this.mDestinationFile = file;
        }
        this.mSelectedImageUri = uri2;
    }

    public static File getFinalSaveDirectory(Context context, Uri uri) {
        File saveDirectory = getSaveDirectory(context, uri);
        if (saveDirectory == null || !saveDirectory.canWrite()) {
            saveDirectory = new File(Environment.getExternalStorageDirectory(), DEFAULT_SAVE_DIRECTORY);
        }
        if (!saveDirectory.exists()) {
            saveDirectory.mkdirs();
        }
        return saveDirectory;
    }

    public static File getNewFile(Context context, Uri uri) {
        File finalSaveDirectory = getFinalSaveDirectory(context, uri);
        String format = new SimpleDateFormat(TIME_STAMP_NAME).format(new Date(System.currentTimeMillis()));
        boolean hasPanoPrefix = hasPanoPrefix(context, uri);
        String str = ".jpg";
        if (hasPanoPrefix) {
            StringBuilder sb = new StringBuilder();
            sb.append(PREFIX_PANO);
            sb.append(format);
            sb.append(str);
            return new File(finalSaveDirectory, sb.toString());
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(PREFIX_IMG);
        sb2.append(format);
        sb2.append(str);
        return new File(finalSaveDirectory, sb2.toString());
    }

    public static void deleteAuxFiles(ContentResolver contentResolver, Uri uri) {
        final String[] strArr = new String[1];
        querySourceFromContentResolver(contentResolver, uri, new String[]{"_data"}, new ContentResolverQueryCallback() {
            public void onCursorResult(Cursor cursor) {
                strArr[0] = cursor.getString(0);
            }
        });
        if (strArr[0] != null) {
            File file = new File(strArr[0]);
            final String name = file.getName();
            int indexOf = name.indexOf(".");
            if (indexOf != -1) {
                name = name.substring(0, indexOf);
            }
            File localAuxDirectory = getLocalAuxDirectory(file);
            if (localAuxDirectory.exists()) {
                for (File delete : localAuxDirectory.listFiles(new FilenameFilter() {
                    public boolean accept(File file, String str) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(name);
                        sb.append(".");
                        return str.startsWith(sb.toString());
                    }
                })) {
                    delete.delete();
                }
            }
        }
    }

    public ExifInterface getExifData(Uri uri) {
        String str = LOGTAG;
        ExifInterface exifInterface = new ExifInterface();
        String type = this.mContext.getContentResolver().getType(this.mSelectedImageUri);
        if (type == null) {
            type = ImageLoader.getMimeType(this.mSelectedImageUri);
        }
        if (type.equals("image/jpeg")) {
            InputStream inputStream = null;
            try {
                inputStream = this.mContext.getContentResolver().openInputStream(uri);
                exifInterface.readExif(inputStream);
            } catch (FileNotFoundException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Cannot find file: ");
                sb.append(uri);
                Log.w(str, sb.toString(), e);
            } catch (IOException e2) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Cannot read exif for: ");
                sb2.append(uri);
                Log.w(str, sb2.toString(), e2);
            } catch (Throwable th) {
                Utils.closeSilently((Closeable) inputStream);
                throw th;
            }
            Utils.closeSilently((Closeable) inputStream);
        }
        return exifInterface;
    }

    public boolean putExifData(File file, ExifInterface exifInterface, Bitmap bitmap, int i) {
        String str = LOGTAG;
        OutputStream outputStream = null;
        try {
            OutputStream exifWriterStream = exifInterface.getExifWriterStream(file.getAbsolutePath());
            try {
                CompressFormat compressFormat = CompressFormat.JPEG;
                if (i <= 0) {
                    i = 1;
                }
                bitmap.compress(compressFormat, i, exifWriterStream);
                exifWriterStream.flush();
                exifWriterStream.close();
                Utils.closeSilently((Closeable) null);
                return true;
            } catch (FileNotFoundException e) {
                e = e;
                outputStream = exifWriterStream;
                StringBuilder sb = new StringBuilder();
                sb.append("File not found: ");
                sb.append(file.getAbsolutePath());
                Log.w(str, sb.toString(), e);
                Utils.closeSilently((Closeable) outputStream);
                return false;
            } catch (IOException e2) {
                e = e2;
                outputStream = exifWriterStream;
                try {
                    Log.w(str, "Could not write exif: ", e);
                    Utils.closeSilently((Closeable) outputStream);
                    return false;
                } catch (Throwable th) {
                    th = th;
                }
            } catch (Throwable th2) {
                th = th2;
                outputStream = exifWriterStream;
                Utils.closeSilently((Closeable) outputStream);
                throw th;
            }
        } catch (FileNotFoundException e3) {
            e = e3;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("File not found: ");
            sb2.append(file.getAbsolutePath());
            Log.w(str, sb2.toString(), e);
            Utils.closeSilently((Closeable) outputStream);
            return false;
        } catch (IOException e4) {
            e = e4;
            Log.w(str, "Could not write exif: ", e);
            Utils.closeSilently((Closeable) outputStream);
            return false;
        }
    }

    private void resetProgress() {
        this.mCurrentProcessingStep = 0;
    }

    private void updateProgress() {
        Callback callback = this.mCallback;
        if (callback != null) {
            int i = this.mCurrentProcessingStep + 1;
            this.mCurrentProcessingStep = i;
            callback.onProgress(6, i);
        }
    }

    private void updateExifData(ExifInterface exifInterface, long j) {
        exifInterface.addDateTimeStampTag(ExifInterface.TAG_DATE_TIME, j, TimeZone.getDefault());
        exifInterface.setTag(exifInterface.buildTag(ExifInterface.TAG_ORIENTATION, Short.valueOf(1)));
        exifInterface.removeCompressedThumbnail();
    }

    private Uri moveSrcToAuxIfNeeded(Uri uri, File file) {
        File localFileFromUri = getLocalFileFromUri(this.mContext, uri);
        String str = LOGTAG;
        if (localFileFromUri == null) {
            Log.d(str, "Source file is not a local file, no update.");
            return uri;
        }
        File localAuxDirectory = getLocalAuxDirectory(file);
        if (!localAuxDirectory.exists() && !localAuxDirectory.mkdirs()) {
            return uri;
        }
        File file2 = new File(localAuxDirectory, ".nomedia");
        if (!file2.exists()) {
            try {
                file2.createNewFile();
            } catch (IOException unused) {
                Log.e(str, "Can't create the nomedia");
                return uri;
            }
        }
        File file3 = new File(localAuxDirectory, file.getName());
        String name = file3.getName();
        String name2 = localFileFromUri.getName();
        String str2 = ".";
        String substring = name.substring(name.lastIndexOf(str2));
        String substring2 = name2.substring(name2.lastIndexOf(str2));
        if (!substring.equals(substring2)) {
            String name3 = file.getName();
            StringBuilder sb = new StringBuilder();
            sb.append(name3.substring(0, name3.lastIndexOf(str2)));
            sb.append(substring2);
            file3 = new File(localAuxDirectory, sb.toString());
        }
        if (file3.exists() || localFileFromUri.renameTo(file3)) {
            return Uri.fromFile(file3);
        }
        return uri;
    }

    private static File getLocalAuxDirectory(File file) {
        File parentFile = file.getParentFile();
        StringBuilder sb = new StringBuilder();
        sb.append(parentFile);
        sb.append("/");
        sb.append(AUX_DIR_NAME);
        return new File(sb.toString());
    }

    public static Uri makeAndInsertUri(Context context, Uri uri) {
        long currentTimeMillis = System.currentTimeMillis();
        String format = new SimpleDateFormat(TIME_STAMP_NAME).format(new Date(currentTimeMillis));
        File finalSaveDirectory = getFinalSaveDirectory(context, uri);
        StringBuilder sb = new StringBuilder();
        sb.append(format);
        sb.append(".JPG");
        return linkNewFileToUri(context, uri, new File(finalSaveDirectory, sb.toString()), currentTimeMillis, false);
    }

    public static void querySource(Context context, Uri uri, String[] strArr, ContentResolverQueryCallback contentResolverQueryCallback) {
        querySourceFromContentResolver(context.getContentResolver(), uri, strArr, contentResolverQueryCallback);
    }

    private static void querySourceFromContentResolver(ContentResolver contentResolver, Uri uri, String[] strArr, ContentResolverQueryCallback contentResolverQueryCallback) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, strArr, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                contentResolverQueryCallback.onCursorResult(cursor);
            }
            if (cursor == null) {
                return;
            }
        } catch (Exception unused) {
            if (cursor == null) {
                return;
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
    }

    private static File getSaveDirectory(Context context, Uri uri) {
        File localFileFromUri = getLocalFileFromUri(context, uri);
        if (localFileFromUri != null) {
            return localFileFromUri.getParentFile();
        }
        return null;
    }

    private static File getLocalFileFromUri(Context context, Uri uri) {
        String str = LOGTAG;
        if (uri == null) {
            Log.e(str, "srcUri is null.");
            return null;
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            Log.e(str, "scheme is null.");
            return null;
        }
        final File[] fileArr = new File[1];
        if (scheme.equals("content")) {
            if (uri.getAuthority().equals("media")) {
                querySource(context, uri, new String[]{"_data"}, new ContentResolverQueryCallback() {
                    public void onCursorResult(Cursor cursor) {
                        fileArr[0] = new File(cursor.getString(0));
                    }
                });
            }
        } else if (scheme.equals("file")) {
            fileArr[0] = new File(uri.getPath());
        }
        return fileArr[0];
    }

    private static String getTrueFilename(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        final String[] strArr = new String[1];
        querySource(context, uri, new String[]{"_data"}, new ContentResolverQueryCallback() {
            public void onCursorResult(Cursor cursor) {
                strArr[0] = new File(cursor.getString(0)).getName();
            }
        });
        return strArr[0];
    }

    private static boolean hasPanoPrefix(Context context, Uri uri) {
        String trueFilename = getTrueFilename(context, uri);
        return trueFilename != null && trueFilename.startsWith(PREFIX_PANO);
    }

    public static Uri linkNewFileToUri(Context context, Uri uri, File file, long j, boolean z) {
        File localFileFromUri = getLocalFileFromUri(context, uri);
        ContentValues contentValues = getContentValues(context, uri, file, j);
        if (isFileUri(uri) || localFileFromUri == null || !z) {
            return context.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, contentValues);
        }
        context.getContentResolver().update(uri, contentValues, null, null);
        if (!localFileFromUri.exists()) {
            return uri;
        }
        localFileFromUri.delete();
        return uri;
    }

    public static Uri updateFile(Context context, Uri uri, File file, long j) {
        context.getContentResolver().update(uri, getContentValues(context, uri, file, j), null, null);
        return uri;
    }

    private static ContentValues getContentValues(Context context, Uri uri, File file, long j) {
        final ContentValues contentValues = new ContentValues();
        long j2 = j / 1000;
        contentValues.put(TinyPlanetFragment.ARGUMENT_TITLE, file.getName());
        contentValues.put("_display_name", file.getName());
        contentValues.put("mime_type", "image/jpeg");
        String str = "datetaken";
        contentValues.put(str, Long.valueOf(j2));
        contentValues.put("date_modified", Long.valueOf(j2));
        contentValues.put("date_added", Long.valueOf(j2));
        contentValues.put("orientation", Integer.valueOf(0));
        contentValues.put("_data", file.getAbsolutePath());
        contentValues.put("_size", Long.valueOf(file.length()));
        querySource(context, uri, new String[]{str, "latitude", "longitude"}, new ContentResolverQueryCallback() {
            public void onCursorResult(Cursor cursor) {
                contentValues.put("datetaken", Long.valueOf(cursor.getLong(0)));
                double d = cursor.getDouble(1);
                double d2 = cursor.getDouble(2);
                if (d != 0.0d || d2 != 0.0d) {
                    contentValues.put("latitude", Double.valueOf(d));
                    contentValues.put("longitude", Double.valueOf(d2));
                }
            }
        });
        return contentValues;
    }

    private static boolean isFileUri(Uri uri) {
        String scheme = uri.getScheme();
        return scheme != null && scheme.equals("file");
    }
}
