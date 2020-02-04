package com.android.camera;

import android.util.Log;

public class MosaicFrameProcessor {
    private static final int FRAME_COUNT_INDEX = 9;
    private static final int HR_TO_LR_DOWNSAMPLE_FACTOR = 4;
    private static final int MAX_NUMBER_OF_FRAMES = 100;
    private static final int MOSAIC_RET_CODE_INDEX = 10;
    private static final int NUM_FRAMES_IN_BUFFER = 2;
    private static final String TAG = "MosaicFrameProcessor";
    private static final int WINDOW_SIZE = 3;
    private static final int X_COORD_INDEX = 2;
    private static final int Y_COORD_INDEX = 5;
    private static MosaicFrameProcessor sMosaicFrameProcessor;
    private int mCurrProcessFrameIdx = -1;
    private float[] mDeltaX = new float[3];
    private float[] mDeltaY = new float[3];
    private int mFillIn = 0;
    private boolean mFirstRun;
    private boolean mIsMosaicMemoryAllocated = false;
    private int mLastProcessFrameIdx = -1;
    private Mosaic mMosaicer = new Mosaic();
    private int mOldestIdx = 0;
    private float mPanningRateX;
    private float mPanningRateY;
    private int mPreviewBufferSize;
    private int mPreviewHeight;
    private int mPreviewWidth;
    private ProgressListener mProgressListener;
    private int mTotalFrameCount = 0;
    private float mTotalTranslationX = 0.0f;
    private float mTotalTranslationY = 0.0f;
    private float mTranslationLastX;
    private float mTranslationLastY;

    public interface ProgressListener {
        void onProgress(boolean z, float f, float f2, float f3, float f4);
    }

    public static MosaicFrameProcessor getInstance() {
        if (sMosaicFrameProcessor == null) {
            sMosaicFrameProcessor = new MosaicFrameProcessor();
        }
        return sMosaicFrameProcessor;
    }

    private MosaicFrameProcessor() {
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.mProgressListener = progressListener;
    }

    public int reportProgress(boolean z, boolean z2) {
        return this.mMosaicer.reportProgress(z, z2);
    }

    public void initialize(int i, int i2, int i3) {
        this.mPreviewWidth = i;
        this.mPreviewHeight = i2;
        this.mPreviewBufferSize = i3;
        setupMosaicer(this.mPreviewWidth, this.mPreviewHeight, this.mPreviewBufferSize);
        setStripType(1);
    }

    public void clear() {
        if (this.mIsMosaicMemoryAllocated) {
            this.mMosaicer.freeMosaicMemory();
            this.mIsMosaicMemoryAllocated = false;
        }
        synchronized (this) {
            notify();
        }
    }

    public boolean isMosaicMemoryAllocated() {
        return this.mIsMosaicMemoryAllocated;
    }

    public void setStripType(int i) {
        this.mMosaicer.setStripType(i);
    }

    private void setupMosaicer(int i, int i2, int i3) {
        StringBuilder sb = new StringBuilder();
        sb.append("setupMosaicer w, h=");
        sb.append(i);
        sb.append(',');
        sb.append(i2);
        sb.append(',');
        sb.append(i3);
        Log.v(TAG, sb.toString());
        if (!this.mIsMosaicMemoryAllocated) {
            this.mIsMosaicMemoryAllocated = true;
            this.mMosaicer.allocateMosaicMemory(i, i2);
            return;
        }
        throw new RuntimeException("MosaicFrameProcessor in use!");
    }

    public void reset() {
        this.mFirstRun = true;
        this.mTotalFrameCount = 0;
        this.mFillIn = 0;
        this.mTotalTranslationX = 0.0f;
        this.mTranslationLastX = 0.0f;
        this.mTotalTranslationY = 0.0f;
        this.mTranslationLastY = 0.0f;
        this.mPanningRateX = 0.0f;
        this.mPanningRateY = 0.0f;
        this.mLastProcessFrameIdx = -1;
        this.mCurrProcessFrameIdx = -1;
        for (int i = 0; i < 3; i++) {
            this.mDeltaX[i] = 0.0f;
            this.mDeltaY[i] = 0.0f;
        }
        this.mMosaicer.reset();
    }

    public int createMosaic(boolean z) {
        return this.mMosaicer.createMosaic(z);
    }

    public byte[] getFinalMosaicNV21() {
        return this.mMosaicer.getFinalMosaicNV21();
    }

    public void processFrame() {
        if (this.mIsMosaicMemoryAllocated) {
            int i = this.mFillIn;
            this.mCurrProcessFrameIdx = i;
            this.mFillIn = (i + 1) % 2;
            int i2 = this.mCurrProcessFrameIdx;
            if (i2 != this.mLastProcessFrameIdx) {
                this.mLastProcessFrameIdx = i2;
                if (this.mTotalFrameCount < 100) {
                    calculateTranslationRate();
                    ProgressListener progressListener = this.mProgressListener;
                    if (progressListener != null) {
                        progressListener.onProgress(false, this.mPanningRateX, this.mPanningRateY, (this.mTranslationLastX * 4.0f) / ((float) this.mPreviewWidth), (this.mTranslationLastY * 4.0f) / ((float) this.mPreviewHeight));
                    }
                } else {
                    ProgressListener progressListener2 = this.mProgressListener;
                    if (progressListener2 != null) {
                        progressListener2.onProgress(true, this.mPanningRateX, this.mPanningRateY, (this.mTranslationLastX * 4.0f) / ((float) this.mPreviewWidth), (this.mTranslationLastY * 4.0f) / ((float) this.mPreviewHeight));
                    }
                }
            }
        }
    }

    public void calculateTranslationRate() {
        float[] sourceImageFromGPU = this.mMosaicer.setSourceImageFromGPU();
        float f = sourceImageFromGPU[10];
        this.mTotalFrameCount = (int) sourceImageFromGPU[9];
        float f2 = sourceImageFromGPU[2];
        float f3 = sourceImageFromGPU[5];
        if (this.mFirstRun) {
            this.mTranslationLastX = f2;
            this.mTranslationLastY = f3;
            this.mFirstRun = false;
            return;
        }
        int i = this.mOldestIdx;
        float f4 = this.mTotalTranslationX;
        float[] fArr = this.mDeltaX;
        this.mTotalTranslationX = f4 - fArr[i];
        this.mTotalTranslationY -= this.mDeltaY[i];
        fArr[i] = Math.abs(f2 - this.mTranslationLastX);
        this.mDeltaY[i] = Math.abs(f3 - this.mTranslationLastY);
        this.mTotalTranslationX += this.mDeltaX[i];
        this.mTotalTranslationY += this.mDeltaY[i];
        this.mPanningRateX = (this.mTotalTranslationX / ((float) (this.mPreviewWidth / 4))) / 3.0f;
        this.mPanningRateY = (this.mTotalTranslationY / ((float) (this.mPreviewHeight / 4))) / 3.0f;
        this.mTranslationLastX = f2;
        this.mTranslationLastY = f3;
        this.mOldestIdx = (this.mOldestIdx + 1) % 3;
    }
}
