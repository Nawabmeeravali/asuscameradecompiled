package com.android.camera.util;

import android.media.MediaRecorder.VideoEncoder;
import java.util.HashMap;
import java.util.Map;

public class SettingTranslation {
    private static final TwoWayMap AUDIO_ENCODER_TABLE = new TwoWayMap();
    private static final TwoWayMap NOISE_REDUCTION_TABLE = new TwoWayMap();
    public static final int NOT_FOUND = -1;
    private static final TwoWayMap VIDEO_ENCODER_TABLE = new TwoWayMap();

    private static class TwoWayMap {
        private Map<Integer, String> intToStr;
        private Map<String, Integer> strToInt;

        private TwoWayMap() {
            this.strToInt = new HashMap();
            this.intToStr = new HashMap();
        }

        public void put(String str, int i) {
            this.strToInt.put(str, Integer.valueOf(i));
            this.intToStr.put(Integer.valueOf(i), str);
        }

        public int get(String str) {
            Integer num = (Integer) this.strToInt.get(str);
            if (num != null) {
                return num.intValue();
            }
            return -1;
        }

        public String get(int i) {
            return (String) this.intToStr.get(Integer.valueOf(i));
        }
    }

    static {
        String str = "default";
        VIDEO_ENCODER_TABLE.put(str, 0);
        VIDEO_ENCODER_TABLE.put("h263", 1);
        VIDEO_ENCODER_TABLE.put("h264", 2);
        int intFieldIfExists = ApiHelper.getIntFieldIfExists(VideoEncoder.class, "HEVC", null, 0);
        if (intFieldIfExists == 0) {
            intFieldIfExists = ApiHelper.getIntFieldIfExists(VideoEncoder.class, "H265", null, 0);
        }
        VIDEO_ENCODER_TABLE.put("h265", intFieldIfExists);
        VIDEO_ENCODER_TABLE.put("mpeg-4-sp", 3);
        VIDEO_ENCODER_TABLE.put("vp8", 4);
        AUDIO_ENCODER_TABLE.put("aac", 3);
        AUDIO_ENCODER_TABLE.put("aac-eld", 5);
        AUDIO_ENCODER_TABLE.put("amr-nb", 1);
        AUDIO_ENCODER_TABLE.put("amr-wb", 2);
        AUDIO_ENCODER_TABLE.put(str, 0);
        AUDIO_ENCODER_TABLE.put("he-aac", 4);
        AUDIO_ENCODER_TABLE.put("vorbis", 6);
        NOISE_REDUCTION_TABLE.put("off", 0);
        NOISE_REDUCTION_TABLE.put("fast", 1);
        NOISE_REDUCTION_TABLE.put("high-quality", 2);
        NOISE_REDUCTION_TABLE.put("minimal", 3);
        NOISE_REDUCTION_TABLE.put("zero-shutter-lag", 4);
    }

    public static int getVideoEncoder(String str) {
        return VIDEO_ENCODER_TABLE.get(str);
    }

    public static String getVideoEncoder(int i) {
        return VIDEO_ENCODER_TABLE.get(i);
    }

    public static int getAudioEncoder(String str) {
        return AUDIO_ENCODER_TABLE.get(str);
    }

    public static String getAudioEncoder(int i) {
        return AUDIO_ENCODER_TABLE.get(i);
    }

    public static int getNoiseReduction(String str) {
        return NOISE_REDUCTION_TABLE.get(str);
    }

    public static String getNoiseReduction(int i) {
        return NOISE_REDUCTION_TABLE.get(i);
    }
}
