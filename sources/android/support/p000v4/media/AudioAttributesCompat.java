package android.support.p000v4.media;

import android.media.AudioAttributes;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.SparseIntArray;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

/* renamed from: android.support.v4.media.AudioAttributesCompat */
public class AudioAttributesCompat {
    private static final String AUDIO_ATTRIBUTES_CONTENT_TYPE = "android.support.v4.media.audio_attrs.CONTENT_TYPE";
    private static final String AUDIO_ATTRIBUTES_FLAGS = "android.support.v4.media.audio_attrs.FLAGS";
    private static final String AUDIO_ATTRIBUTES_FRAMEWORKS = "android.support.v4.media.audio_attrs.FRAMEWORKS";
    private static final String AUDIO_ATTRIBUTES_LEGACY_STREAM_TYPE = "android.support.v4.media.audio_attrs.LEGACY_STREAM_TYPE";
    private static final String AUDIO_ATTRIBUTES_USAGE = "android.support.v4.media.audio_attrs.USAGE";
    public static final int CONTENT_TYPE_MOVIE = 3;
    public static final int CONTENT_TYPE_MUSIC = 2;
    public static final int CONTENT_TYPE_SONIFICATION = 4;
    public static final int CONTENT_TYPE_SPEECH = 1;
    public static final int CONTENT_TYPE_UNKNOWN = 0;
    private static final int FLAG_ALL = 1023;
    private static final int FLAG_ALL_PUBLIC = 273;
    public static final int FLAG_AUDIBILITY_ENFORCED = 1;
    private static final int FLAG_BEACON = 8;
    private static final int FLAG_BYPASS_INTERRUPTION_POLICY = 64;
    private static final int FLAG_BYPASS_MUTE = 128;
    private static final int FLAG_DEEP_BUFFER = 512;
    public static final int FLAG_HW_AV_SYNC = 16;
    private static final int FLAG_HW_HOTWORD = 32;
    private static final int FLAG_LOW_LATENCY = 256;
    private static final int FLAG_SCO = 4;
    private static final int FLAG_SECURE = 2;
    private static final int[] SDK_USAGES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16};
    private static final int SUPPRESSIBLE_CALL = 2;
    private static final int SUPPRESSIBLE_NOTIFICATION = 1;
    private static final SparseIntArray SUPPRESSIBLE_USAGES = new SparseIntArray();
    private static final String TAG = "AudioAttributesCompat";
    public static final int USAGE_ALARM = 4;
    public static final int USAGE_ASSISTANCE_ACCESSIBILITY = 11;
    public static final int USAGE_ASSISTANCE_NAVIGATION_GUIDANCE = 12;
    public static final int USAGE_ASSISTANCE_SONIFICATION = 13;
    public static final int USAGE_ASSISTANT = 16;
    public static final int USAGE_GAME = 14;
    public static final int USAGE_MEDIA = 1;
    public static final int USAGE_NOTIFICATION = 5;
    public static final int USAGE_NOTIFICATION_COMMUNICATION_DELAYED = 9;
    public static final int USAGE_NOTIFICATION_COMMUNICATION_INSTANT = 8;
    public static final int USAGE_NOTIFICATION_COMMUNICATION_REQUEST = 7;
    public static final int USAGE_NOTIFICATION_EVENT = 10;
    public static final int USAGE_NOTIFICATION_RINGTONE = 6;
    public static final int USAGE_UNKNOWN = 0;
    private static final int USAGE_VIRTUAL_SOURCE = 15;
    public static final int USAGE_VOICE_COMMUNICATION = 2;
    public static final int USAGE_VOICE_COMMUNICATION_SIGNALLING = 3;
    /* access modifiers changed from: private */
    public static boolean sForceLegacyBehavior;
    /* access modifiers changed from: private */
    public Wrapper mAudioAttributesWrapper;
    int mContentType;
    int mFlags;
    Integer mLegacyStream;
    int mUsage;

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    /* renamed from: android.support.v4.media.AudioAttributesCompat$AttributeContentType */
    public @interface AttributeContentType {
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    /* renamed from: android.support.v4.media.AudioAttributesCompat$AttributeUsage */
    public @interface AttributeUsage {
    }

    /* renamed from: android.support.v4.media.AudioAttributesCompat$AudioManagerHidden */
    private static abstract class AudioManagerHidden {
        public static final int STREAM_ACCESSIBILITY = 10;
        public static final int STREAM_BLUETOOTH_SCO = 6;
        public static final int STREAM_SYSTEM_ENFORCED = 7;
        public static final int STREAM_TTS = 9;

        private AudioManagerHidden() {
        }
    }

    /* renamed from: android.support.v4.media.AudioAttributesCompat$Builder */
    public static class Builder {
        private Object mAAObject;
        private int mContentType = 0;
        private int mFlags = 0;
        private Integer mLegacyStream;
        private int mUsage = 0;

        public Builder() {
        }

        public Builder(AudioAttributesCompat audioAttributesCompat) {
            this.mUsage = audioAttributesCompat.mUsage;
            this.mContentType = audioAttributesCompat.mContentType;
            this.mFlags = audioAttributesCompat.mFlags;
            this.mLegacyStream = audioAttributesCompat.mLegacyStream;
            this.mAAObject = audioAttributesCompat.unwrap();
        }

        public AudioAttributesCompat build() {
            if (AudioAttributesCompat.sForceLegacyBehavior || VERSION.SDK_INT < 21) {
                AudioAttributesCompat audioAttributesCompat = new AudioAttributesCompat();
                audioAttributesCompat.mContentType = this.mContentType;
                audioAttributesCompat.mFlags = this.mFlags;
                audioAttributesCompat.mUsage = this.mUsage;
                audioAttributesCompat.mLegacyStream = this.mLegacyStream;
                audioAttributesCompat.mAudioAttributesWrapper = null;
                return audioAttributesCompat;
            }
            Object obj = this.mAAObject;
            if (obj != null) {
                return AudioAttributesCompat.wrap(obj);
            }
            android.media.AudioAttributes.Builder usage = new android.media.AudioAttributes.Builder().setContentType(this.mContentType).setFlags(this.mFlags).setUsage(this.mUsage);
            Integer num = this.mLegacyStream;
            if (num != null) {
                usage.setLegacyStreamType(num.intValue());
            }
            return AudioAttributesCompat.wrap(usage.build());
        }

        public Builder setUsage(int i) {
            switch (i) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                    this.mUsage = i;
                    break;
                case 16:
                    if (!AudioAttributesCompat.sForceLegacyBehavior && VERSION.SDK_INT > 25) {
                        this.mUsage = i;
                        break;
                    } else {
                        this.mUsage = 12;
                        break;
                    }
                default:
                    this.mUsage = 0;
                    break;
            }
            return this;
        }

        public Builder setContentType(int i) {
            if (i == 0 || i == 1 || i == 2 || i == 3 || i == 4) {
                this.mContentType = i;
            } else {
                this.mUsage = 0;
            }
            return this;
        }

        public Builder setFlags(int i) {
            this.mFlags = (i & AudioAttributesCompat.FLAG_ALL) | this.mFlags;
            return this;
        }

        public Builder setLegacyStreamType(int i) {
            if (i != 10) {
                this.mLegacyStream = Integer.valueOf(i);
                this.mUsage = AudioAttributesCompat.usageForStreamType(i);
                return this;
            }
            throw new IllegalArgumentException("STREAM_ACCESSIBILITY is not a legacy stream type that was used for audio playback");
        }
    }

    /* access modifiers changed from: private */
    public static int usageForStreamType(int i) {
        switch (i) {
            case 0:
                return 2;
            case 1:
            case 7:
                return 13;
            case 2:
                return 6;
            case 3:
                return 1;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 2;
            case 8:
                return 3;
            case 10:
                return 11;
            default:
                return 0;
        }
    }

    static {
        SUPPRESSIBLE_USAGES.put(5, 1);
        SUPPRESSIBLE_USAGES.put(6, 2);
        SUPPRESSIBLE_USAGES.put(7, 2);
        SUPPRESSIBLE_USAGES.put(8, 1);
        SUPPRESSIBLE_USAGES.put(9, 1);
        SUPPRESSIBLE_USAGES.put(10, 1);
    }

    private AudioAttributesCompat() {
        this.mUsage = 0;
        this.mContentType = 0;
        this.mFlags = 0;
    }

    public int getVolumeControlStream() {
        if (VERSION.SDK_INT < 26 || sForceLegacyBehavior || unwrap() == null) {
            return toVolumeStreamType(true, this);
        }
        return ((AudioAttributes) unwrap()).getVolumeControlStream();
    }

    @Nullable
    public Object unwrap() {
        Wrapper wrapper = this.mAudioAttributesWrapper;
        if (wrapper != null) {
            return wrapper.unwrap();
        }
        return null;
    }

    public int getLegacyStreamType() {
        Integer num = this.mLegacyStream;
        if (num != null) {
            return num.intValue();
        }
        if (VERSION.SDK_INT < 21 || sForceLegacyBehavior) {
            return toVolumeStreamType(false, this.mFlags, this.mUsage);
        }
        return AudioAttributesCompatApi21.toLegacyStreamType(this.mAudioAttributesWrapper);
    }

    @Nullable
    public static AudioAttributesCompat wrap(@NonNull Object obj) {
        if (VERSION.SDK_INT < 21 || sForceLegacyBehavior) {
            return null;
        }
        AudioAttributesCompat audioAttributesCompat = new AudioAttributesCompat();
        audioAttributesCompat.mAudioAttributesWrapper = Wrapper.wrap((AudioAttributes) obj);
        return audioAttributesCompat;
    }

    public int getContentType() {
        if (VERSION.SDK_INT >= 21 && !sForceLegacyBehavior) {
            Wrapper wrapper = this.mAudioAttributesWrapper;
            if (wrapper != null) {
                return wrapper.unwrap().getContentType();
            }
        }
        return this.mContentType;
    }

    public int getUsage() {
        if (VERSION.SDK_INT >= 21 && !sForceLegacyBehavior) {
            Wrapper wrapper = this.mAudioAttributesWrapper;
            if (wrapper != null) {
                return wrapper.unwrap().getUsage();
            }
        }
        return this.mUsage;
    }

    public int getFlags() {
        if (VERSION.SDK_INT >= 21 && !sForceLegacyBehavior) {
            Wrapper wrapper = this.mAudioAttributesWrapper;
            if (wrapper != null) {
                return wrapper.unwrap().getFlags();
            }
        }
        int i = this.mFlags;
        int legacyStreamType = getLegacyStreamType();
        if (legacyStreamType == 6) {
            i |= 4;
        } else if (legacyStreamType == 7) {
            i |= 1;
        }
        return i & FLAG_ALL_PUBLIC;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @NonNull
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        if (VERSION.SDK_INT >= 21) {
            bundle.putParcelable(AUDIO_ATTRIBUTES_FRAMEWORKS, this.mAudioAttributesWrapper.unwrap());
        } else {
            bundle.putInt(AUDIO_ATTRIBUTES_USAGE, this.mUsage);
            bundle.putInt(AUDIO_ATTRIBUTES_CONTENT_TYPE, this.mContentType);
            bundle.putInt(AUDIO_ATTRIBUTES_FLAGS, this.mFlags);
            Integer num = this.mLegacyStream;
            if (num != null) {
                bundle.putInt(AUDIO_ATTRIBUTES_LEGACY_STREAM_TYPE, num.intValue());
            }
        }
        return bundle;
    }

    /* JADX WARNING: type inference failed for: r0v0 */
    /* JADX WARNING: type inference failed for: r0v1, types: [java.lang.Integer] */
    /* JADX WARNING: type inference failed for: r0v2, types: [java.lang.Integer] */
    /* JADX WARNING: type inference failed for: r0v3, types: [android.support.v4.media.AudioAttributesCompat] */
    /* JADX WARNING: type inference failed for: r0v4, types: [android.support.v4.media.AudioAttributesCompat] */
    /* JADX WARNING: type inference failed for: r0v5 */
    /* JADX WARNING: type inference failed for: r0v6 */
    /* JADX WARNING: Multi-variable type inference failed. Error: jadx.core.utils.exceptions.JadxRuntimeException: No candidate types for var: r0v0
      assigns: [?[int, float, boolean, short, byte, char, OBJECT, ARRAY], android.support.v4.media.AudioAttributesCompat, java.lang.Integer]
      uses: [java.lang.Integer, android.support.v4.media.AudioAttributesCompat]
      mth insns count: 30
    	at jadx.core.dex.visitors.typeinference.TypeSearch.fillTypeCandidates(TypeSearch.java:237)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    	at jadx.core.dex.visitors.typeinference.TypeSearch.run(TypeSearch.java:53)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runMultiVariableSearch(TypeInferenceVisitor.java:99)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:92)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
    	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
    	at jadx.core.ProcessClass.process(ProcessClass.java:30)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:311)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:217)
     */
    /* JADX WARNING: Unknown variable types count: 3 */
    @android.support.annotation.RestrictTo({android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP})
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static android.support.p000v4.media.AudioAttributesCompat fromBundle(android.os.Bundle r5) {
        /*
            r0 = 0
            if (r5 != 0) goto L_0x0004
            return r0
        L_0x0004:
            int r1 = android.os.Build.VERSION.SDK_INT
            r2 = 21
            if (r1 < r2) goto L_0x001a
            java.lang.String r1 = "android.support.v4.media.audio_attrs.FRAMEWORKS"
            android.os.Parcelable r5 = r5.getParcelable(r1)
            android.media.AudioAttributes r5 = (android.media.AudioAttributes) r5
            if (r5 != 0) goto L_0x0015
            goto L_0x0019
        L_0x0015:
            android.support.v4.media.AudioAttributesCompat r0 = wrap(r5)
        L_0x0019:
            return r0
        L_0x001a:
            r1 = 0
            java.lang.String r2 = "android.support.v4.media.audio_attrs.USAGE"
            int r2 = r5.getInt(r2, r1)
            java.lang.String r3 = "android.support.v4.media.audio_attrs.CONTENT_TYPE"
            int r3 = r5.getInt(r3, r1)
            java.lang.String r4 = "android.support.v4.media.audio_attrs.FLAGS"
            int r1 = r5.getInt(r4, r1)
            android.support.v4.media.AudioAttributesCompat r4 = new android.support.v4.media.AudioAttributesCompat
            r4.<init>()
            r4.mUsage = r2
            r4.mContentType = r3
            r4.mFlags = r1
            java.lang.String r1 = "android.support.v4.media.audio_attrs.LEGACY_STREAM_TYPE"
            boolean r2 = r5.containsKey(r1)
            if (r2 == 0) goto L_0x0048
            int r5 = r5.getInt(r1)
            java.lang.Integer r0 = java.lang.Integer.valueOf(r5)
        L_0x0048:
            r4.mLegacyStream = r0
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.AudioAttributesCompat.fromBundle(android.os.Bundle):android.support.v4.media.AudioAttributesCompat");
    }

    public int hashCode() {
        if (VERSION.SDK_INT >= 21 && !sForceLegacyBehavior) {
            Wrapper wrapper = this.mAudioAttributesWrapper;
            if (wrapper != null) {
                return wrapper.unwrap().hashCode();
            }
        }
        return Arrays.hashCode(new Object[]{Integer.valueOf(this.mContentType), Integer.valueOf(this.mFlags), Integer.valueOf(this.mUsage), this.mLegacyStream});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("AudioAttributesCompat:");
        if (unwrap() != null) {
            sb.append(" audioattributes=");
            sb.append(unwrap());
        } else {
            if (this.mLegacyStream != null) {
                sb.append(" stream=");
                sb.append(this.mLegacyStream);
                sb.append(" derived");
            }
            sb.append(" usage=");
            sb.append(usageToString());
            sb.append(" content=");
            sb.append(this.mContentType);
            sb.append(" flags=0x");
            sb.append(Integer.toHexString(this.mFlags).toUpperCase());
        }
        return sb.toString();
    }

    /* access modifiers changed from: 0000 */
    public String usageToString() {
        return usageToString(this.mUsage);
    }

    static String usageToString(int i) {
        switch (i) {
            case 0:
                return new String("USAGE_UNKNOWN");
            case 1:
                return new String("USAGE_MEDIA");
            case 2:
                return new String("USAGE_VOICE_COMMUNICATION");
            case 3:
                return new String("USAGE_VOICE_COMMUNICATION_SIGNALLING");
            case 4:
                return new String("USAGE_ALARM");
            case 5:
                return new String("USAGE_NOTIFICATION");
            case 6:
                return new String("USAGE_NOTIFICATION_RINGTONE");
            case 7:
                return new String("USAGE_NOTIFICATION_COMMUNICATION_REQUEST");
            case 8:
                return new String("USAGE_NOTIFICATION_COMMUNICATION_INSTANT");
            case 9:
                return new String("USAGE_NOTIFICATION_COMMUNICATION_DELAYED");
            case 10:
                return new String("USAGE_NOTIFICATION_EVENT");
            case 11:
                return new String("USAGE_ASSISTANCE_ACCESSIBILITY");
            case 12:
                return new String("USAGE_ASSISTANCE_NAVIGATION_GUIDANCE");
            case 13:
                return new String("USAGE_ASSISTANCE_SONIFICATION");
            case 14:
                return new String("USAGE_GAME");
            case 16:
                return new String("USAGE_ASSISTANT");
            default:
                StringBuilder sb = new StringBuilder();
                sb.append("unknown usage ");
                sb.append(i);
                return new String(sb.toString());
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public static void setForceLegacyBehavior(boolean z) {
        sForceLegacyBehavior = z;
    }

    static int toVolumeStreamType(boolean z, AudioAttributesCompat audioAttributesCompat) {
        return toVolumeStreamType(z, audioAttributesCompat.getFlags(), audioAttributesCompat.getUsage());
    }

    static int toVolumeStreamType(boolean z, int i, int i2) {
        int i3 = 1;
        if ((i & 1) == 1) {
            if (!z) {
                i3 = 7;
            }
            return i3;
        }
        int i4 = 0;
        if ((i & 4) == 4) {
            if (!z) {
                i4 = 6;
            }
            return i4;
        }
        int i5 = 3;
        switch (i2) {
            case 0:
                if (z) {
                    i5 = Integer.MIN_VALUE;
                }
                return i5;
            case 1:
            case 12:
            case 14:
            case 16:
                return 3;
            case 2:
                break;
            case 3:
                if (!z) {
                    i4 = 8;
                    break;
                }
                break;
            case 4:
                return 4;
            case 5:
            case 7:
            case 8:
            case 9:
            case 10:
                return 5;
            case 6:
                return 2;
            case 11:
                return 10;
            case 13:
                return 1;
            default:
                if (!z) {
                    return 3;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("Unknown usage value ");
                sb.append(i2);
                sb.append(" in audio attributes");
                throw new IllegalArgumentException(sb.toString());
        }
        return i4;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || AudioAttributesCompat.class != obj.getClass()) {
            return false;
        }
        AudioAttributesCompat audioAttributesCompat = (AudioAttributesCompat) obj;
        if (VERSION.SDK_INT >= 21 && !sForceLegacyBehavior) {
            Wrapper wrapper = this.mAudioAttributesWrapper;
            if (wrapper != null) {
                return wrapper.unwrap().equals(audioAttributesCompat.unwrap());
            }
        }
        if (this.mContentType == audioAttributesCompat.getContentType() && this.mFlags == audioAttributesCompat.getFlags() && this.mUsage == audioAttributesCompat.getUsage()) {
            Integer num = this.mLegacyStream;
            if (num == null) {
            }
        }
        z = false;
        return z;
    }
}
