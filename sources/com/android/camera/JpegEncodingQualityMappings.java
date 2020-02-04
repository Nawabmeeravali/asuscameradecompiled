package com.android.camera;

import android.media.CameraProfile;
import android.util.Log;
import java.util.HashMap;

/* compiled from: PhotoModule */
class JpegEncodingQualityMappings {
    private static final int DEFAULT_QUALITY = 85;
    private static final String TAG = "JpegEncodingQualityMappings";
    private static HashMap<String, Integer> mHashMap = new HashMap<>();

    JpegEncodingQualityMappings() {
    }

    static {
        mHashMap.put("normal", Integer.valueOf(0));
        mHashMap.put("fine", Integer.valueOf(1));
        mHashMap.put("superfine", Integer.valueOf(2));
    }

    public static int getQualityNumber(String str) {
        try {
            int parseInt = Integer.parseInt(str);
            return (parseInt < 0 || parseInt > 100) ? DEFAULT_QUALITY : parseInt;
        } catch (NumberFormatException unused) {
            Integer num = (Integer) mHashMap.get(str);
            if (num != null) {
                return CameraProfile.getJpegEncodingQualityParameter(num.intValue());
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Unknown Jpeg quality: ");
            sb.append(str);
            Log.w(TAG, sb.toString());
            return DEFAULT_QUALITY;
        }
    }
}
