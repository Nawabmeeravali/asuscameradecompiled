package com.android.camera;

public class MosaicRenderer {
    public static native int init(boolean z);

    public static native void preprocess(float[] fArr);

    public static native void reset(int i, int i2, boolean z, int i3);

    public static native void setPreviewBackground(boolean z);

    public static native void setWarping(boolean z);

    public static native void step();

    public static native void transferGPUtoCPU();

    public static native void updateMatrix();

    static {
        System.loadLibrary("jni_snapcammosaic");
    }
}
