package com.android.camera;

public class Mosaic {
    public static final int BLENDTYPE_CYLINDERPAN = 2;
    public static final int BLENDTYPE_FULL = 0;
    public static final int BLENDTYPE_HORIZONTAL = 3;
    public static final int BLENDTYPE_PAN = 1;
    public static final int MOSAIC_RET_CANCELLED = -2;
    public static final int MOSAIC_RET_ERROR = -1;
    public static final int MOSAIC_RET_FEW_INLIERS = 2;
    public static final int MOSAIC_RET_LOW_TEXTURE = -3;
    public static final int MOSAIC_RET_OK = 1;
    public static final int STRIPTYPE_THIN = 0;
    public static final int STRIPTYPE_WIDE = 1;

    public native void allocateMosaicMemory(int i, int i2);

    public native int createMosaic(boolean z);

    public native void freeMosaicMemory();

    public native int[] getFinalMosaic();

    public native byte[] getFinalMosaicNV21();

    public native int reportProgress(boolean z, boolean z2);

    public native void reset();

    public native void setBlendingType(int i);

    public native float[] setSourceImage(byte[] bArr);

    public native float[] setSourceImageFromGPU();

    public native void setStripType(int i);

    static {
        System.loadLibrary("jni_snapcammosaic");
    }
}
