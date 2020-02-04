package com.android.camera.data;

import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;
import com.android.camera.exif.ExifInterface;
import com.android.camera.exif.ExifTag;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.codeaurora.snapcam.C0905R;

public class MediaDetails implements Iterable<Entry<Integer, Object>> {
    public static final int INDEX_APERTURE = 105;
    public static final int INDEX_DATETIME = 3;
    public static final int INDEX_DESCRIPTION = 2;
    public static final int INDEX_DURATION = 8;
    public static final int INDEX_EXPOSURE_TIME = 107;
    public static final int INDEX_FLASH = 102;
    public static final int INDEX_FOCAL_LENGTH = 103;
    public static final int INDEX_HEIGHT = 6;
    public static final int INDEX_ISO = 108;
    public static final int INDEX_LOCATION = 4;
    public static final int INDEX_MAKE = 100;
    public static final int INDEX_MIMETYPE = 9;
    public static final int INDEX_MODEL = 101;
    public static final int INDEX_ORIENTATION = 7;
    public static final int INDEX_PATH = 200;
    public static final int INDEX_SHUTTER_SPEED = 106;
    public static final int INDEX_SIZE = 10;
    public static final int INDEX_TITLE = 1;
    public static final int INDEX_WHITE_BALANCE = 104;
    public static final int INDEX_WIDTH = 5;
    private static final String TAG = "MediaDetails";
    private TreeMap<Integer, Object> mDetails = new TreeMap<>();
    private SparseIntArray mUnits = new SparseIntArray();

    public static class FlashState {
        private static int FLASH_FIRED_MASK = 1;
        private static int FLASH_FUNCTION_MASK = 32;
        private static int FLASH_MODE_MASK = 24;
        private static int FLASH_RED_EYE_MASK = 64;
        private static int FLASH_RETURN_MASK = 6;
        private int mState;

        public FlashState(int i) {
            this.mState = i;
        }

        public boolean isFlashFired() {
            return (this.mState & FLASH_FIRED_MASK) != 0;
        }
    }

    public void addDetail(int i, Object obj) {
        this.mDetails.put(Integer.valueOf(i), obj);
    }

    public Object getDetail(int i) {
        return this.mDetails.get(Integer.valueOf(i));
    }

    public int size() {
        return this.mDetails.size();
    }

    public Iterator<Entry<Integer, Object>> iterator() {
        return this.mDetails.entrySet().iterator();
    }

    public void setUnit(int i, int i2) {
        this.mUnits.put(i, i2);
    }

    public boolean hasUnit(int i) {
        return this.mUnits.indexOfKey(i) >= 0;
    }

    public int getUnit(int i) {
        return this.mUnits.get(i);
    }

    private static void setExifData(MediaDetails mediaDetails, ExifTag exifTag, int i) {
        String str;
        if (exifTag != null) {
            short dataType = exifTag.getDataType();
            if (dataType == 5 || dataType == 10) {
                str = String.valueOf(exifTag.getValueAsRational(0).toDouble());
            } else if (dataType == 2) {
                str = exifTag.getValueAsString();
            } else {
                str = String.valueOf(exifTag.forceGetValueAsLong(0));
            }
            if (i == 102) {
                mediaDetails.addDetail(i, new FlashState(Integer.valueOf(str.toString()).intValue()));
            } else {
                mediaDetails.addDetail(i, str);
            }
        }
    }

    public static void extractExifInfo(MediaDetails mediaDetails, String str) {
        String str2 = TAG;
        ExifInterface exifInterface = new ExifInterface();
        try {
            exifInterface.readExif(str);
        } catch (FileNotFoundException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Could not find file to read exif: ");
            sb.append(str);
            Log.w(str2, sb.toString(), e);
        } catch (IOException e2) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Could not read exif from file: ");
            sb2.append(str);
            Log.w(str2, sb2.toString(), e2);
        }
        setExifData(mediaDetails, exifInterface.getTag(ExifInterface.TAG_FLASH), 102);
        setExifData(mediaDetails, exifInterface.getTag(ExifInterface.TAG_IMAGE_WIDTH), 5);
        setExifData(mediaDetails, exifInterface.getTag(ExifInterface.TAG_IMAGE_LENGTH), 6);
        setExifData(mediaDetails, exifInterface.getTag(ExifInterface.TAG_MAKE), 100);
        setExifData(mediaDetails, exifInterface.getTag(ExifInterface.TAG_MODEL), 101);
        setExifData(mediaDetails, exifInterface.getTag(ExifInterface.TAG_APERTURE_VALUE), 105);
        setExifData(mediaDetails, exifInterface.getTag(ExifInterface.TAG_ISO_SPEED_RATINGS), 108);
        setExifData(mediaDetails, exifInterface.getTag(ExifInterface.TAG_WHITE_BALANCE), 104);
        setExifData(mediaDetails, exifInterface.getTag(ExifInterface.TAG_EXPOSURE_TIME), 107);
        ExifTag tag = exifInterface.getTag(ExifInterface.TAG_FOCAL_LENGTH);
        if (tag != null) {
            mediaDetails.addDetail(103, Double.valueOf(tag.getValueAsRational(0).toDouble()));
            mediaDetails.setUnit(103, C0905R.string.unit_mm);
        }
    }

    public static String formatDuration(Context context, long j) {
        long j2 = j / 3600;
        long j3 = 3600 * j2;
        long j4 = (j - j3) / 60;
        long j5 = j - (j3 + (60 * j4));
        if (j2 == 0) {
            return String.format(context.getString(C0905R.string.details_ms), new Object[]{Long.valueOf(j4), Long.valueOf(j5)});
        }
        return String.format(context.getString(C0905R.string.details_hms), new Object[]{Long.valueOf(j2), Long.valueOf(j4), Long.valueOf(j5)});
    }
}
