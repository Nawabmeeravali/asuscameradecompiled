package com.android.camera.crop;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.android.camera.exif.ExifInterface;
import com.android.camera.exif.ExifTag;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class ImageLoader {
    private static final int BITMAP_LOAD_BACKOUT_ATTEMPTS = 5;
    public static final int DEFAULT_COMPRESS_QUALITY = 95;
    public static final String JPEG_MIME_TYPE = "image/jpeg";
    private static final String LOGTAG = "ImageLoader";
    public static final int ORI_FLIP_HOR = 2;
    public static final int ORI_FLIP_VERT = 4;
    public static final int ORI_NORMAL = 1;
    public static final int ORI_ROTATE_180 = 3;
    public static final int ORI_ROTATE_270 = 8;
    public static final int ORI_ROTATE_90 = 6;
    public static final int ORI_TRANSPOSE = 5;
    public static final int ORI_TRANSVERSE = 7;
    private static final float OVERDRAW_ZOOM = 1.2f;

    private ImageLoader() {
    }

    public static String getMimeType(Uri uri) {
        String fileExtensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (fileExtensionFromUrl != null) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtensionFromUrl);
        }
        return null;
    }

    public static String getLocalPathFromUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String str = "_data";
        Cursor query = contentResolver.query(uri, new String[]{str}, null, null, null);
        if (query == null) {
            return null;
        }
        int columnIndexOrThrow = query.getColumnIndexOrThrow(str);
        query.moveToFirst();
        String string = query.getString(columnIndexOrThrow);
        Utils.closeSilently(query);
        return string;
    }

    public static int getMetadataOrientation(Context context, Uri uri) {
        int i;
        if (uri == null || context == null) {
            throw new IllegalArgumentException("bad argument to getOrientation");
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, new String[]{"orientation"}, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                int i2 = cursor.getInt(0);
                if (i2 == 90) {
                    i = 6;
                } else if (i2 == 180) {
                    i = 3;
                } else if (i2 != 270) {
                    Utils.closeSilently(cursor);
                    return 1;
                } else {
                    i = 8;
                }
                Utils.closeSilently(cursor);
                return i;
            }
        } catch (SQLiteException | IllegalArgumentException | IllegalStateException unused) {
        } catch (Throwable th) {
            Utils.closeSilently((Cursor) null);
            throw th;
        }
        Utils.closeSilently(cursor);
        if ("file".equals(uri.getScheme())) {
            if (!"image/jpeg".equals(getMimeType(uri))) {
                return 1;
            }
            String path = uri.getPath();
            ExifInterface exifInterface = new ExifInterface();
            try {
                exifInterface.readExif(path);
                Integer tagIntValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION);
                if (tagIntValue != null) {
                    int intValue = tagIntValue.intValue();
                    switch (intValue) {
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                            return intValue;
                        default:
                            return 1;
                    }
                }
            } catch (IOException e) {
                Log.w(LOGTAG, "Failed to read EXIF orientation", e);
            }
        }
        return 1;
    }

    public static int getMetadataRotation(Context context, Uri uri) {
        int metadataOrientation = getMetadataOrientation(context, uri);
        if (metadataOrientation == 3) {
            return 180;
        }
        if (metadataOrientation != 6) {
            return metadataOrientation != 8 ? 0 : 270;
        }
        return 90;
    }

    public static Bitmap orientBitmap(Bitmap bitmap, int i) {
        Matrix matrix = new Matrix();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (i == 6 || i == 8 || i == 5 || i == 7) {
            int i2 = height;
            height = width;
            width = i2;
        }
        switch (i) {
            case 2:
                matrix.preScale(-1.0f, 1.0f);
                break;
            case 3:
                matrix.setRotate(180.0f, ((float) width) / 2.0f, ((float) height) / 2.0f);
                break;
            case 4:
                matrix.preScale(1.0f, -1.0f);
                break;
            case 5:
                matrix.setRotate(90.0f, ((float) width) / 2.0f, ((float) height) / 2.0f);
                matrix.preScale(1.0f, -1.0f);
                break;
            case 6:
                matrix.setRotate(90.0f, ((float) width) / 2.0f, ((float) height) / 2.0f);
                break;
            case 7:
                matrix.setRotate(270.0f, ((float) width) / 2.0f, ((float) height) / 2.0f);
                matrix.preScale(1.0f, -1.0f);
                break;
            case 8:
                matrix.setRotate(270.0f, ((float) width) / 2.0f, ((float) height) / 2.0f);
                break;
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Rect loadBitmapBounds(Context context, Uri uri) {
        Options options = new Options();
        loadBitmap(context, uri, options);
        return new Rect(0, 0, options.outWidth, options.outHeight);
    }

    public static Bitmap loadDownsampledBitmap(Context context, Uri uri, int i) {
        Options options = new Options();
        options.inMutable = true;
        options.inSampleSize = i;
        return loadBitmap(context, uri, options);
    }

    public static Bitmap loadBitmap(Context context, Uri uri, Options options) {
        InputStream inputStream;
        if (uri == null || context == null) {
            throw new IllegalArgumentException("bad argument to loadBitmap");
        }
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            try {
                Bitmap decodeStream = BitmapFactory.decodeStream(inputStream, null, options);
                Utils.closeSilently((Closeable) inputStream);
                return decodeStream;
            } catch (FileNotFoundException e) {
                e = e;
                String str = LOGTAG;
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("FileNotFoundException for ");
                    sb.append(uri);
                    Log.e(str, sb.toString(), e);
                    Utils.closeSilently((Closeable) inputStream);
                    return null;
                } catch (Throwable th) {
                    th = th;
                    Utils.closeSilently((Closeable) inputStream);
                    throw th;
                }
            }
        } catch (FileNotFoundException e2) {
            e = e2;
            inputStream = null;
            String str2 = LOGTAG;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("FileNotFoundException for ");
            sb2.append(uri);
            Log.e(str2, sb2.toString(), e);
            Utils.closeSilently((Closeable) inputStream);
            return null;
        } catch (Throwable th2) {
            th = th2;
            inputStream = null;
            Utils.closeSilently((Closeable) inputStream);
            throw th;
        }
    }

    public static Bitmap loadConstrainedBitmap(Uri uri, Context context, int i, Rect rect, boolean z) {
        int i2;
        if (i <= 0 || uri == null || context == null) {
            throw new IllegalArgumentException("bad argument to getScaledBitmap");
        }
        Rect loadBitmapBounds = loadBitmapBounds(context, uri);
        if (rect != null) {
            rect.set(loadBitmapBounds);
        }
        int width = loadBitmapBounds.width();
        int height = loadBitmapBounds.height();
        if (width > 0 && height > 0) {
            if (z) {
                i2 = Math.min(width, height);
            } else {
                i2 = Math.max(width, height);
            }
            int i3 = 1;
            while (i2 > i) {
                i2 >>>= 1;
                i3 <<= 1;
            }
            if (i3 > 0 && Math.min(width, height) / i3 > 0) {
                return loadDownsampledBitmap(context, uri, i3);
            }
        }
        return null;
    }

    public static Bitmap loadOrientedConstrainedBitmap(Uri uri, Context context, int i, int i2, Rect rect) {
        Bitmap loadConstrainedBitmap = loadConstrainedBitmap(uri, context, i, rect, false);
        if (loadConstrainedBitmap == null) {
            return loadConstrainedBitmap;
        }
        Bitmap orientBitmap = orientBitmap(loadConstrainedBitmap, i2);
        Config config = orientBitmap.getConfig();
        Config config2 = Config.ARGB_8888;
        return config != config2 ? orientBitmap.copy(config2, true) : orientBitmap;
    }

    public static Bitmap loadBitmapWithBackouts(Context context, Uri uri, int i) {
        boolean z = true;
        if (i <= 0) {
            i = 1;
        }
        int i2 = i;
        Bitmap bitmap = null;
        int i3 = 0;
        while (z) {
            try {
                bitmap = loadDownsampledBitmap(context, uri, i2);
                z = false;
            } catch (OutOfMemoryError e) {
                i3++;
                if (i3 < 5) {
                    System.gc();
                    i2 *= 2;
                    bitmap = null;
                } else {
                    throw e;
                }
            }
        }
        return bitmap;
    }

    public static Bitmap loadOrientedBitmapWithBackouts(Context context, Uri uri, int i) {
        Bitmap loadBitmapWithBackouts = loadBitmapWithBackouts(context, uri, i);
        if (loadBitmapWithBackouts == null) {
            return null;
        }
        return orientBitmap(loadBitmapWithBackouts, getMetadataOrientation(context, uri));
    }

    public static Bitmap decodeResourceWithBackouts(Resources resources, Options options, int i) {
        boolean z = true;
        if (options.inSampleSize < 1) {
            options.inSampleSize = 1;
        }
        Bitmap bitmap = null;
        int i2 = 0;
        while (z) {
            try {
                bitmap = BitmapFactory.decodeResource(resources, i, options);
                z = false;
            } catch (OutOfMemoryError e) {
                i2++;
                if (i2 < 5) {
                    System.gc();
                    options.inSampleSize *= 2;
                    bitmap = null;
                } else {
                    throw e;
                }
            }
        }
        return bitmap;
    }

    public static List<ExifTag> getExif(Context context, Uri uri) {
        String localPathFromUri = getLocalPathFromUri(context, uri);
        if (localPathFromUri != null) {
            if (!"image/jpeg".equals(getMimeType(Uri.parse(localPathFromUri)))) {
                return null;
            }
            try {
                ExifInterface exifInterface = new ExifInterface();
                exifInterface.readExif(localPathFromUri);
                return exifInterface.getAllTags();
            } catch (IOException e) {
                Log.w(LOGTAG, "Failed to read EXIF tags", e);
            }
        }
        return null;
    }
}
