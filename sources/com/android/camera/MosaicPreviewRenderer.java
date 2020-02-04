package com.android.camera;

import android.graphics.SurfaceTexture;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.camera.SurfaceTextureRenderer.FrameDrawer;
import javax.microedition.khronos.opengles.GL10;

public class MosaicPreviewRenderer {
    private static final String TAG = "CAM_MosaicPreviewRenderer";
    /* access modifiers changed from: private */
    public HandlerThread mEglThread;
    /* access modifiers changed from: private */
    public ConditionVariable mEglThreadBlockVar = new ConditionVariable();
    /* access modifiers changed from: private */
    public boolean mEnableWarpedPanoPreview = false;
    private MyHandler mHandler;
    /* access modifiers changed from: private */
    public int mHeight;
    /* access modifiers changed from: private */
    public SurfaceTexture mInputSurfaceTexture;
    /* access modifiers changed from: private */
    public boolean mIsLandscape = true;
    /* access modifiers changed from: private */
    public int mOrientation = 0;
    private SurfaceTextureRenderer mSTRenderer;
    /* access modifiers changed from: private */
    public final float[] mTransformMatrix = new float[16];
    /* access modifiers changed from: private */
    public int mWidth;

    private class MyHandler extends Handler {
        public static final int MSG_ALIGN_FRAME_SYNC = 3;
        public static final int MSG_DO_PREVIEW_RESET = 5;
        public static final int MSG_INIT_SYNC = 0;
        public static final int MSG_RELEASE = 4;
        public static final int MSG_SHOW_PREVIEW_FRAME = 2;
        public static final int MSG_SHOW_PREVIEW_FRAME_SYNC = 1;

        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 0) {
                doInit();
                MosaicPreviewRenderer.this.mEglThreadBlockVar.open();
            } else if (i == 1) {
                doShowPreviewFrame();
                MosaicPreviewRenderer.this.mEglThreadBlockVar.open();
            } else if (i == 2) {
                doShowPreviewFrame();
            } else if (i == 3) {
                doAlignFrame();
                MosaicPreviewRenderer.this.mEglThreadBlockVar.open();
            } else if (i == 4) {
                doRelease();
                MosaicPreviewRenderer.this.mEglThreadBlockVar.open();
            } else if (i == 5) {
                doPreviewReset();
            }
        }

        private void doAlignFrame() {
            MosaicPreviewRenderer.this.mInputSurfaceTexture.updateTexImage();
            MosaicPreviewRenderer.this.mInputSurfaceTexture.getTransformMatrix(MosaicPreviewRenderer.this.mTransformMatrix);
            MosaicRenderer.setPreviewBackground(true);
            MosaicRenderer.preprocess(MosaicPreviewRenderer.this.mTransformMatrix);
            MosaicRenderer.step();
            MosaicRenderer.setPreviewBackground(false);
            MosaicRenderer.setWarping(true);
            MosaicRenderer.transferGPUtoCPU();
            MosaicRenderer.updateMatrix();
            MosaicRenderer.step();
        }

        private void doShowPreviewFrame() {
            MosaicPreviewRenderer.this.mInputSurfaceTexture.updateTexImage();
            MosaicPreviewRenderer.this.mInputSurfaceTexture.getTransformMatrix(MosaicPreviewRenderer.this.mTransformMatrix);
            MosaicRenderer.setWarping(false);
            MosaicRenderer.preprocess(MosaicPreviewRenderer.this.mTransformMatrix);
            MosaicRenderer.updateMatrix();
            MosaicRenderer.step();
        }

        private void doInit() {
            MosaicPreviewRenderer mosaicPreviewRenderer = MosaicPreviewRenderer.this;
            mosaicPreviewRenderer.mInputSurfaceTexture = new SurfaceTexture(MosaicRenderer.init(mosaicPreviewRenderer.mEnableWarpedPanoPreview));
            MosaicRenderer.reset(MosaicPreviewRenderer.this.mWidth, MosaicPreviewRenderer.this.mHeight, MosaicPreviewRenderer.this.mIsLandscape, MosaicPreviewRenderer.this.mOrientation);
        }

        private void doPreviewReset() {
            MosaicRenderer.reset(MosaicPreviewRenderer.this.mWidth, MosaicPreviewRenderer.this.mHeight, MosaicPreviewRenderer.this.mIsLandscape, MosaicPreviewRenderer.this.mOrientation);
        }

        private void doRelease() {
            releaseSurfaceTexture(MosaicPreviewRenderer.this.mInputSurfaceTexture);
            MosaicPreviewRenderer.this.mEglThread.quit();
        }

        private void releaseSurfaceTexture(SurfaceTexture surfaceTexture) {
            surfaceTexture.release();
        }

        public void sendMessageSync(int i) {
            MosaicPreviewRenderer.this.mEglThreadBlockVar.close();
            sendEmptyMessage(i);
            MosaicPreviewRenderer.this.mEglThreadBlockVar.block();
        }
    }

    public MosaicPreviewRenderer(SurfaceTexture surfaceTexture, int i, int i2, boolean z, int i3, boolean z2) {
        this.mIsLandscape = z;
        this.mOrientation = i3;
        this.mEnableWarpedPanoPreview = z2;
        this.mEglThread = new HandlerThread("PanoramaRealtimeRenderer");
        this.mEglThread.start();
        this.mHandler = new MyHandler(this.mEglThread.getLooper());
        this.mWidth = i;
        this.mHeight = i2;
        this.mSTRenderer = new SurfaceTextureRenderer(surfaceTexture, this.mHandler, new FrameDrawer() {
            public void onDrawFrame(GL10 gl10) {
            }
        });
        this.mHandler.sendMessageSync(0);
    }

    public void previewReset(int i, int i2, boolean z, int i3) {
        this.mWidth = i;
        this.mHeight = i2;
        this.mIsLandscape = z;
        this.mOrientation = i3;
        this.mHandler.sendEmptyMessage(5);
        this.mSTRenderer.draw(false);
    }

    public void release() {
        this.mSTRenderer.release();
        this.mHandler.sendMessageSync(4);
    }

    public void showPreviewFrameSync() {
        this.mHandler.sendMessageSync(1);
        this.mSTRenderer.draw(true);
    }

    public void showPreviewFrame() {
        this.mHandler.sendEmptyMessage(2);
        this.mSTRenderer.draw(false);
    }

    public void alignFrameSync() {
        this.mHandler.sendMessageSync(3);
        this.mSTRenderer.draw(true);
    }

    public SurfaceTexture getInputSurfaceTexture() {
        return this.mInputSurfaceTexture;
    }
}
