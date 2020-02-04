package com.android.camera;

import android.util.Log;
import com.android.camera.exif.ExifInterface;
import java.io.IOException;

public class Exif {
    private static final String TAG = "CameraExif";

    public static ExifInterface getExif(byte[] bArr) {
        ExifInterface exifInterface = new ExifInterface();
        try {
            exifInterface.readExif(bArr);
        } catch (IOException e) {
            Log.w(TAG, "Failed to read EXIF data", e);
        }
        return exifInterface;
    }

    public static int getOrientation(ExifInterface exifInterface) {
        Integer tagIntValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION);
        if (tagIntValue == null) {
            return 0;
        }
        return ExifInterface.getRotationForOrientationValue(tagIntValue.shortValue());
    }

    public static int getOrientation(byte[] bArr) {
        if (bArr == null) {
            return 0;
        }
        return getOrientation(getExif(bArr));
    }
}
