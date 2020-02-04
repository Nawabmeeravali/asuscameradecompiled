package com.android.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Allocation.OnBufferAvailableListener;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type.Builder;
import android.util.Size;
import android.view.Surface;

public class PanoCaptureFrameProcessor {
    /* access modifiers changed from: private */
    public Allocation mARGBOutputAllocation;
    private Activity mActivity;
    /* access modifiers changed from: private */
    public Object mAllocationLock = new Object();
    /* access modifiers changed from: private */
    public Bitmap mBitmap;
    private PanoCaptureModule mController;
    /* access modifiers changed from: private */
    public Allocation mInputAllocation;
    /* access modifiers changed from: private */
    public boolean mIsAllocationEverUsed;
    /* access modifiers changed from: private */
    public boolean mIsPanoActive = false;
    /* access modifiers changed from: private */
    public Object mPanoSwitchLock = new Object();
    /* access modifiers changed from: private */
    public Handler mProcessingHandler;
    private HandlerThread mProcessingThread;
    private RenderScript mRs;
    ScriptIntrinsicYuvToRGB mRsYuvToRGB;
    /* access modifiers changed from: private */
    public Size mSize;
    private Surface mSurface;
    public ProcessingTask mTask;
    /* access modifiers changed from: private */
    public PanoCaptureUI mUI;

    class ProcessingTask implements Runnable, OnBufferAvailableListener {
        private int mFrameCounter = 0;
        private int mPendingFrames = 0;

        public ProcessingTask() {
            PanoCaptureFrameProcessor.this.mBitmap = Bitmap.createBitmap(PanoCaptureFrameProcessor.this.mSize.getWidth(), PanoCaptureFrameProcessor.this.mSize.getHeight(), Config.ARGB_8888);
        }

        public void onBufferAvailable(Allocation allocation) {
            if (PanoCaptureFrameProcessor.this.mProcessingHandler != null) {
                synchronized (this) {
                    this.mPendingFrames++;
                    PanoCaptureFrameProcessor.this.mProcessingHandler.post(this);
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0097, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r5 = this;
                monitor-enter(r5)
                int r0 = r5.mPendingFrames     // Catch:{ all -> 0x009b }
                r1 = 0
                r5.mPendingFrames = r1     // Catch:{ all -> 0x009b }
                com.android.camera.PanoCaptureFrameProcessor r2 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x009b }
                android.os.Handler r2 = r2.mProcessingHandler     // Catch:{ all -> 0x009b }
                r2.removeCallbacks(r5)     // Catch:{ all -> 0x009b }
                monitor-exit(r5)     // Catch:{ all -> 0x009b }
                com.android.camera.PanoCaptureFrameProcessor r2 = com.android.camera.PanoCaptureFrameProcessor.this
                java.lang.Object r2 = r2.mAllocationLock
                monitor-enter(r2)
                com.android.camera.PanoCaptureFrameProcessor r3 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0098 }
                android.renderscript.Allocation r3 = r3.mInputAllocation     // Catch:{ all -> 0x0098 }
                if (r3 == 0) goto L_0x0096
                com.android.camera.PanoCaptureFrameProcessor r3 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0098 }
                android.renderscript.Allocation r3 = r3.mARGBOutputAllocation     // Catch:{ all -> 0x0098 }
                if (r3 != 0) goto L_0x0028
                goto L_0x0096
            L_0x0028:
                com.android.camera.PanoCaptureFrameProcessor r3 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0098 }
                r4 = 1
                r3.mIsAllocationEverUsed = r4     // Catch:{ all -> 0x0098 }
                r3 = r1
            L_0x002f:
                if (r3 >= r0) goto L_0x003d
                com.android.camera.PanoCaptureFrameProcessor r4 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0098 }
                android.renderscript.Allocation r4 = r4.mInputAllocation     // Catch:{ all -> 0x0098 }
                r4.ioReceive()     // Catch:{ all -> 0x0098 }
                int r3 = r3 + 1
                goto L_0x002f
            L_0x003d:
                com.android.camera.PanoCaptureFrameProcessor r0 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0098 }
                java.lang.Object r0 = r0.mPanoSwitchLock     // Catch:{ all -> 0x0098 }
                monitor-enter(r0)     // Catch:{ all -> 0x0098 }
                com.android.camera.PanoCaptureFrameProcessor r3 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0093 }
                boolean r3 = r3.mIsPanoActive     // Catch:{ all -> 0x0093 }
                if (r3 == 0) goto L_0x0090
                com.android.camera.PanoCaptureFrameProcessor r3 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0093 }
                com.android.camera.PanoCaptureUI r3 = r3.mUI     // Catch:{ all -> 0x0093 }
                boolean r3 = r3.isFrameProcessing()     // Catch:{ all -> 0x0093 }
                if (r3 != 0) goto L_0x0090
                com.android.camera.PanoCaptureFrameProcessor r3 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0093 }
                android.renderscript.ScriptIntrinsicYuvToRGB r3 = r3.mRsYuvToRGB     // Catch:{ all -> 0x0093 }
                com.android.camera.PanoCaptureFrameProcessor r4 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0093 }
                android.renderscript.Allocation r4 = r4.mInputAllocation     // Catch:{ all -> 0x0093 }
                r3.setInput(r4)     // Catch:{ all -> 0x0093 }
                com.android.camera.PanoCaptureFrameProcessor r3 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0093 }
                android.renderscript.ScriptIntrinsicYuvToRGB r3 = r3.mRsYuvToRGB     // Catch:{ all -> 0x0093 }
                com.android.camera.PanoCaptureFrameProcessor r4 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0093 }
                android.renderscript.Allocation r4 = r4.mARGBOutputAllocation     // Catch:{ all -> 0x0093 }
                r3.forEach(r4)     // Catch:{ all -> 0x0093 }
                com.android.camera.PanoCaptureFrameProcessor r3 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0093 }
                android.renderscript.Allocation r3 = r3.mARGBOutputAllocation     // Catch:{ all -> 0x0093 }
                com.android.camera.PanoCaptureFrameProcessor r4 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0093 }
                android.graphics.Bitmap r4 = r4.mBitmap     // Catch:{ all -> 0x0093 }
                r3.copyTo(r4)     // Catch:{ all -> 0x0093 }
                com.android.camera.PanoCaptureFrameProcessor r3 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0093 }
                com.android.camera.PanoCaptureUI r3 = r3.mUI     // Catch:{ all -> 0x0093 }
                com.android.camera.PanoCaptureFrameProcessor r5 = com.android.camera.PanoCaptureFrameProcessor.this     // Catch:{ all -> 0x0093 }
                android.graphics.Bitmap r5 = r5.mBitmap     // Catch:{ all -> 0x0093 }
                r3.onFrameAvailable(r5, r1)     // Catch:{ all -> 0x0093 }
            L_0x0090:
                monitor-exit(r0)     // Catch:{ all -> 0x0093 }
                monitor-exit(r2)     // Catch:{ all -> 0x0098 }
                return
            L_0x0093:
                r5 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x0093 }
                throw r5     // Catch:{ all -> 0x0098 }
            L_0x0096:
                monitor-exit(r2)     // Catch:{ all -> 0x0098 }
                return
            L_0x0098:
                r5 = move-exception
                monitor-exit(r2)     // Catch:{ all -> 0x0098 }
                throw r5
            L_0x009b:
                r0 = move-exception
                monitor-exit(r5)     // Catch:{ all -> 0x009b }
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.PanoCaptureFrameProcessor.ProcessingTask.run():void");
        }
    }

    public PanoCaptureFrameProcessor(Size size, Activity activity, PanoCaptureUI panoCaptureUI, PanoCaptureModule panoCaptureModule) {
        this.mUI = panoCaptureUI;
        this.mSize = size;
        this.mActivity = activity;
        this.mController = panoCaptureModule;
        synchronized (this.mAllocationLock) {
            this.mRs = RenderScript.create(this.mActivity);
            this.mRsYuvToRGB = ScriptIntrinsicYuvToRGB.create(this.mRs, Element.RGBA_8888(this.mRs));
            Builder builder = new Builder(this.mRs, Element.YUV(this.mRs));
            builder.setX(size.getWidth());
            builder.setY(size.getHeight());
            builder.setYuvFormat(35);
            this.mInputAllocation = Allocation.createTyped(this.mRs, builder.create(), 33);
            Builder builder2 = new Builder(this.mRs, Element.RGBA_8888(this.mRs));
            builder2.setX(size.getWidth());
            builder2.setY(size.getHeight());
            this.mARGBOutputAllocation = Allocation.createTyped(this.mRs, builder2.create(), 1);
            if (this.mProcessingThread == null) {
                this.mProcessingThread = new HandlerThread("PanoCapture_FrameProcessor");
                this.mProcessingThread.start();
                this.mProcessingHandler = new Handler(this.mProcessingThread.getLooper());
            }
            this.mTask = new ProcessingTask();
            this.mInputAllocation.setOnBufferAvailableListener(this.mTask);
            this.mIsAllocationEverUsed = false;
        }
    }

    public void clear() {
        if (this.mIsPanoActive) {
            changePanoStatus(false, true);
        }
        synchronized (this.mAllocationLock) {
            this.mInputAllocation.setOnBufferAvailableListener(null);
            if (this.mIsAllocationEverUsed) {
                this.mRs.destroy();
                this.mInputAllocation.destroy();
                this.mARGBOutputAllocation.destroy();
            }
            this.mRs = null;
            this.mInputAllocation = null;
            this.mARGBOutputAllocation = null;
        }
        this.mProcessingThread.quitSafely();
        try {
            this.mProcessingThread.join();
            this.mProcessingThread = null;
            this.mProcessingHandler = null;
        } catch (InterruptedException unused) {
        }
    }

    public Surface getInputSurface() {
        synchronized (this.mAllocationLock) {
            if (this.mInputAllocation == null) {
                return null;
            }
            Surface surface = this.mInputAllocation.getSurface();
            return surface;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
        if (r2.mIsPanoActive != false) goto L_0x0028;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0023, code lost:
        r2.mController.unlockFocus();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0028, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void changePanoStatus(boolean r3, boolean r4) {
        /*
            r2 = this;
            boolean r0 = r2.mIsPanoActive
            if (r3 != r0) goto L_0x0005
            return
        L_0x0005:
            java.lang.Object r0 = r2.mPanoSwitchLock
            monitor-enter(r0)
            com.android.camera.PanoCaptureUI r1 = r2.mUI     // Catch:{ all -> 0x0029 }
            boolean r1 = r1.isPanoCompleting()     // Catch:{ all -> 0x0029 }
            if (r1 == 0) goto L_0x0012
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            return
        L_0x0012:
            r2.mIsPanoActive = r3     // Catch:{ all -> 0x0029 }
            boolean r3 = r2.mIsPanoActive     // Catch:{ all -> 0x0029 }
            if (r3 != 0) goto L_0x001e
            com.android.camera.PanoCaptureUI r3 = r2.mUI     // Catch:{ all -> 0x0029 }
            r1 = 0
            r3.onFrameAvailable(r1, r4)     // Catch:{ all -> 0x0029 }
        L_0x001e:
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            boolean r3 = r2.mIsPanoActive
            if (r3 != 0) goto L_0x0028
            com.android.camera.PanoCaptureModule r2 = r2.mController
            r2.unlockFocus()
        L_0x0028:
            return
        L_0x0029:
            r2 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.PanoCaptureFrameProcessor.changePanoStatus(boolean, boolean):void");
    }

    public boolean isPanoActive() {
        return this.mIsPanoActive;
    }
}
