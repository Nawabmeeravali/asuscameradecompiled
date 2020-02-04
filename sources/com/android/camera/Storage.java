package com.android.camera;

import android.annotation.TargetApi;
import android.app.AppGlobals;
import android.app.usage.StorageStatsManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import com.android.camera.exif.ExifInterface;
import com.android.camera.tinyplanet.TinyPlanetFragment;
import com.android.camera.util.ApiHelper;
import java.io.File;
import java.io.FileOutputStream;

public class Storage {
    public static final String BUCKET_ID = String.valueOf(DIRECTORY.toLowerCase().hashCode());
    public static final String DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
    public static final String DIRECTORY;
    public static final String HEIF_POSTFIX = ".heic";
    public static final String JPEG_POSTFIX = ".jpg";
    public static final long LOW_STORAGE_THRESHOLD_BYTES = 104857600;
    public static final long PREPARING = -2;
    public static final String RAW_DIRECTORY;
    public static final float SYSTEM_KEPP_STORAGE_PERCENT = 0.1f;
    private static final String TAG = "CameraStorage";
    public static final long UNAVAILABLE = -1;
    public static final long UNKNOWN_SIZE = -3;
    private static StorageManager mStorageManager = ((StorageManager) AppGlobals.getInitialApplication().getSystemService(StorageManager.class));
    private static StorageStatsManager mStorageStatsManager = ((StorageStatsManager) AppGlobals.getInitialApplication().getSystemService(StorageStatsManager.class));
    private static boolean sSaveSDCard = false;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append(DCIM);
        sb.append("/Camera");
        DIRECTORY = sb.toString();
        StringBuilder sb2 = new StringBuilder();
        sb2.append(DCIM);
        sb2.append("/Camera/raw");
        RAW_DIRECTORY = sb2.toString();
    }

    public static boolean isSaveSDCard() {
        return sSaveSDCard;
    }

    public static void setSaveSDCard(boolean z) {
        sSaveSDCard = z;
    }

    @TargetApi(16)
    private static void setImageSize(ContentValues contentValues, int i, int i2) {
        if (ApiHelper.HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT) {
            contentValues.put("width", Integer.valueOf(i));
            contentValues.put("height", Integer.valueOf(i2));
        }
    }

    public static int writeFile(String str, byte[] bArr, ExifInterface exifInterface, String str2) {
        String str3 = PhotoModule.PIXEL_FORMAT_JPEG;
        if (exifInterface != null && (str2 == null || str2.equalsIgnoreCase(str3))) {
            try {
                return exifInterface.writeExif(bArr, str);
            } catch (Exception e) {
                Log.e(TAG, "Failed to write data", e);
            }
        } else if (bArr != null) {
            if (!str2.equalsIgnoreCase(str3) && str2 != null) {
                new File(RAW_DIRECTORY).mkdirs();
            }
            writeFile(str, bArr);
            return bArr.length;
        }
        return 0;
    }

    public static void writeFile(String str, byte[] bArr) {
        String str2 = "Failed to close file after write";
        String str3 = TAG;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream(str);
            try {
                fileOutputStream2.write(bArr);
                try {
                    fileOutputStream2.close();
                } catch (Exception e) {
                    Log.e(str3, str2, e);
                }
            } catch (Exception e2) {
                e = e2;
                fileOutputStream = fileOutputStream2;
                try {
                    Log.e(str3, "Failed to write data", e);
                    fileOutputStream.close();
                } catch (Throwable th) {
                    th = th;
                    try {
                        fileOutputStream.close();
                    } catch (Exception e3) {
                        Log.e(str3, str2, e3);
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                fileOutputStream = fileOutputStream2;
                fileOutputStream.close();
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            Log.e(str3, "Failed to write data", e);
            fileOutputStream.close();
        }
    }

    public static Uri addImage(ContentResolver contentResolver, String str, long j, Location location, int i, ExifInterface exifInterface, byte[] bArr, int i2, int i3, String str2) {
        String str3 = str;
        String str4 = str2;
        String generateFilepath = generateFilepath(str, str4);
        int writeFile = writeFile(generateFilepath, bArr, exifInterface, str4);
        File file = new File(generateFilepath);
        if (file.exists() && file.isFile()) {
            writeFile = (int) file.length();
        }
        return addImage(contentResolver, str, j, location, i, exifInterface, writeFile, generateFilepath, i2, i3, str2);
    }

    public static ContentValues getContentValuesForData(String str, long j, Location location, int i, ExifInterface exifInterface, int i2, String str2, int i3, int i4, String str3) {
        ContentValues contentValues = new ContentValues(9);
        contentValues.put(TinyPlanetFragment.ARGUMENT_TITLE, str);
        String str4 = "image/jpeg";
        String str5 = "heif";
        String str6 = "_display_name";
        if (!str3.equalsIgnoreCase(PhotoModule.PIXEL_FORMAT_JPEG) && !str3.equalsIgnoreCase(str4) && !str3.equalsIgnoreCase(str5) && str3 != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(".raw");
            contentValues.put(str6, sb.toString());
        } else if (str3.equalsIgnoreCase(str5)) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str);
            sb2.append(HEIF_POSTFIX);
            contentValues.put(str6, sb2.toString());
        } else if (str3.equalsIgnoreCase("heifs")) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(str);
            sb3.append(".heics");
            contentValues.put(str6, sb3.toString());
        } else {
            StringBuilder sb4 = new StringBuilder();
            sb4.append(str);
            sb4.append(JPEG_POSTFIX);
            contentValues.put(str6, sb4.toString());
        }
        contentValues.put("datetaken", Long.valueOf(j));
        String str7 = "mime_type";
        if (str3.equalsIgnoreCase(str5)) {
            contentValues.put(str7, "image/heif");
        } else {
            contentValues.put(str7, str4);
        }
        contentValues.put("orientation", Integer.valueOf(i));
        contentValues.put("_data", str2);
        contentValues.put("_size", Integer.valueOf(i2));
        setImageSize(contentValues, i3, i4);
        String str8 = "longitude";
        String str9 = "latitude";
        if (location != null) {
            contentValues.put(str9, Double.valueOf(location.getLatitude()));
            contentValues.put(str8, Double.valueOf(location.getLongitude()));
        } else if (exifInterface != null) {
            double[] latLongAsDoubles = exifInterface.getLatLongAsDoubles();
            if (latLongAsDoubles != null) {
                contentValues.put(str9, Double.valueOf(latLongAsDoubles[0]));
                contentValues.put(str8, Double.valueOf(latLongAsDoubles[1]));
            }
        }
        return contentValues;
    }

    public static Uri addImage(ContentResolver contentResolver, String str, long j, Location location, int i, ExifInterface exifInterface, int i2, String str2, int i3, int i4, String str3) {
        return insertImage(contentResolver, getContentValuesForData(str, j, location, i, exifInterface, i2, str2, i3, i4, str3));
    }

    public static Uri addImage(ContentResolver contentResolver, String str, long j, Location location, int i, int i2, String str2, int i3, int i4, String str3) {
        ContentResolver contentResolver2 = contentResolver;
        return insertImage(contentResolver, getContentValuesForData(str, j, location, i, null, i2, str2, i3, i4, str3));
    }

    public static long addRawImage(String str, byte[] bArr, String str2) {
        String generateFilepath = generateFilepath(str, str2);
        int writeFile = writeFile(generateFilepath, bArr, null, str2);
        File file = new File(generateFilepath);
        if (file.exists() && file.isFile()) {
            writeFile = (int) file.length();
        }
        return (long) writeFile;
    }

    public static Uri addHeifImage(ContentResolver contentResolver, String str, long j, Location location, int i, ExifInterface exifInterface, String str2, int i2, int i3, int i4, String str3) {
        File file = new File(str2);
        return addImage(contentResolver, str, j, location, i, (!file.exists() || !file.isFile()) ? 0 : (int) file.length(), str2, i2, i3, str3);
    }

    public static void updateImage(Uri uri, ContentResolver contentResolver, String str, long j, Location location, int i, ExifInterface exifInterface, byte[] bArr, int i2, int i3, String str2) {
        byte[] bArr2 = bArr;
        String str3 = str;
        String str4 = str2;
        String generateFilepath = generateFilepath(str, str4);
        writeFile(generateFilepath, bArr2, exifInterface, str4);
        updateImage(uri, contentResolver, str3, j, location, i, bArr2.length, generateFilepath, i2, i3, str4);
    }

    public static void updateImage(Uri uri, ContentResolver contentResolver, String str, long j, Location location, int i, int i2, String str2, int i3, int i4, String str3) {
        Uri uri2 = uri;
        ContentResolver contentResolver2 = contentResolver;
        ContentValues contentValuesForData = getContentValuesForData(str, j, location, i, null, i2, str2, i3, i4, str3);
        int update = contentResolver.update(uri, contentValuesForData, null, null);
        if (update == 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateImage called with no prior image at uri: ");
            sb.append(uri);
            Log.w(TAG, sb.toString());
            insertImage(contentResolver, contentValuesForData);
        } else if (update != 1) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Bad number of rows (");
            sb2.append(update);
            sb2.append(") updated for uri: ");
            sb2.append(uri);
            throw new IllegalStateException(sb2.toString());
        }
    }

    public static void deleteImage(ContentResolver contentResolver, Uri uri) {
        try {
            contentResolver.delete(uri, null, null);
        } catch (Throwable unused) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to delete image: ");
            sb.append(uri);
            Log.e(TAG, sb.toString());
        }
    }

    public static String generateFilepath(String str, String str2) {
        String str3 = "heifs";
        String str4 = "heif";
        if (str2 == null || str2.equalsIgnoreCase(PhotoModule.PIXEL_FORMAT_JPEG) || str2.equalsIgnoreCase(str4) || str2.equalsIgnoreCase(str3)) {
            String str5 = str2.equalsIgnoreCase(str4) ? HEIF_POSTFIX : str2.equalsIgnoreCase(str3) ? ".heics" : JPEG_POSTFIX;
            if (!isSaveSDCard() || !SDCard.instance().isWriteable()) {
                StringBuilder sb = new StringBuilder();
                sb.append(DIRECTORY);
                sb.append('/');
                sb.append(str);
                sb.append(str5);
                return sb.toString();
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append(SDCard.instance().getDirectory());
            sb2.append('/');
            sb2.append(str);
            sb2.append(str5);
            return sb2.toString();
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append(RAW_DIRECTORY);
        sb3.append('/');
        sb3.append(str);
        sb3.append(".raw");
        return sb3.toString();
    }

    private static long getSDCardAvailableSpace() {
        if (!SDCard.instance().isWriteable() || SDCard.instance().getDirectory() == null) {
            return -3;
        }
        new File(SDCard.instance().getDirectory()).mkdirs();
        try {
            StatFs statFs = new StatFs(SDCard.instance().getDirectory());
            return ((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize());
        } catch (Exception unused) {
            return -3;
        }
    }

    private static long getInternalStorageAvailableSpace() {
        String externalStorageState = Environment.getExternalStorageState();
        StringBuilder sb = new StringBuilder();
        sb.append("External storage state=");
        sb.append(externalStorageState);
        String sb2 = sb.toString();
        String str = TAG;
        Log.d(str, sb2);
        if ("checking".equals(externalStorageState)) {
            return -2;
        }
        if (!"mounted".equals(externalStorageState)) {
            return -1;
        }
        File file = new File(DIRECTORY);
        file.mkdirs();
        if (!file.isDirectory() || !file.canWrite()) {
            return -1;
        }
        long j = -3;
        for (VolumeInfo volumeInfo : mStorageManager.getVolumes()) {
            if (volumeInfo.getType() == 1 && volumeInfo.isMountedReadable()) {
                try {
                    j += mStorageStatsManager.getFreeBytes(volumeInfo.getFsUuid());
                } catch (Exception e) {
                    Log.w(str, e);
                }
            }
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append("privateFreeBytes : ");
        sb3.append(j);
        Log.d(str, sb3.toString());
        if (j <= 0) {
            j = 0;
        }
        return j;
    }

    public static long getAvailableSpace() {
        if (isSaveSDCard()) {
            return getSDCardAvailableSpace();
        }
        return getInternalStorageAvailableSpace();
    }

    public static boolean switchSavePath() {
        if (isSaveSDCard() || !SDCard.instance().isWriteable() || getInternalStorageAvailableSpace() > LOW_STORAGE_THRESHOLD_BYTES || getSDCardAvailableSpace() <= LOW_STORAGE_THRESHOLD_BYTES) {
            return false;
        }
        setSaveSDCard(true);
        return true;
    }

    public static void ensureOSXCompatible() {
        File file = new File(DCIM, "100ANDRO");
        if (!file.exists() && !file.mkdirs()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to create ");
            sb.append(file.getPath());
            Log.e(TAG, sb.toString());
        }
    }

    private static Uri insertImage(ContentResolver contentResolver, ContentValues contentValues) {
        try {
            return contentResolver.insert(Media.EXTERNAL_CONTENT_URI, contentValues);
        } catch (Throwable th) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to write MediaStore");
            sb.append(th);
            Log.e(TAG, sb.toString());
            return null;
        }
    }
}
