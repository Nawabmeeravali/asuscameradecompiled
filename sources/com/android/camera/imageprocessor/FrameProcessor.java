package com.android.camera.imageprocessor;

import android.app.Activity;
import android.hardware.camera2.CameraCharacteristics;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type.Builder;
import android.util.Size;
import android.view.Surface;
import com.android.camera.CaptureModule;
import com.android.camera.imageprocessor.filter.BeautificationFilter;
import com.android.camera.imageprocessor.filter.ImageFilter;
import com.android.camera.imageprocessor.filter.TrackingFocusFrameListener;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

public class FrameProcessor {
    public static final int FILTER_MAKEUP = 1;
    public static final int FILTER_NONE = 0;
    public static final int LISTENER_TRACKING_FOCUS = 2;
    private Activity mActivity;
    /* access modifiers changed from: private */
    public Object mAllocationLock = new Object();
    private ArrayList<ImageFilter> mFinalFilters;
    /* access modifiers changed from: private */
    public Allocation mInputAllocation;
    private ImageReader mInputImageReader;
    /* access modifiers changed from: private */
    public boolean mIsActive = false;
    /* access modifiers changed from: private */
    public boolean mIsAllocationEverUsed;
    /* access modifiers changed from: private */
    public boolean mIsFirstIn = true;
    /* access modifiers changed from: private */
    public boolean mIsVideoOn = false;
    /* access modifiers changed from: private */
    public Handler mListeningHandler;
    /* access modifiers changed from: private */
    public ListeningTask mListeningTask;
    private HandlerThread mListeningThread;
    /* access modifiers changed from: private */
    public CaptureModule mModule;
    /* access modifiers changed from: private */
    public Handler mOutingHandler;
    private HandlerThread mOutingThread;
    /* access modifiers changed from: private */
    public Allocation mOutputAllocation;
    /* access modifiers changed from: private */
    public ArrayList<ImageFilter> mPreviewFilters;
    private Allocation mProcessAllocation;
    private Handler mProcessingHandler;
    private HandlerThread mProcessingThread;
    private RenderScript mRs;
    ScriptC_rotator mRsRotator;
    ScriptC_YuvToRgb mRsYuvToRGB;
    /* access modifiers changed from: private */
    public Size mSize;
    private Surface mSurfaceAsItIs;
    private ProcessingTask mTask;
    /* access modifiers changed from: private */
    public Allocation mVideoOutputAllocation;
    private Surface mVideoSurfaceAsItIs;

    class ListeningTask implements Runnable {
        int bVUSize;
        int bYSize;
        ByteBuffer mBVU = null;
        ByteBuffer mBY = null;
        ImageFilter mFilter;
        int mHeight;
        Semaphore mMutureLock = new Semaphore(1);
        int mStride;
        int mWidth;

        ListeningTask() {
        }

        public boolean setParam(ImageFilter imageFilter, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3) {
            if (!FrameProcessor.this.mIsActive || !this.mMutureLock.tryAcquire()) {
                return false;
            }
            this.mFilter = imageFilter;
            if (this.mBY == null || this.bYSize != byteBuffer.remaining()) {
                this.bYSize = byteBuffer.remaining();
                this.mBY = ByteBuffer.allocateDirect(this.bYSize);
            }
            if (this.mBVU == null || this.bVUSize != byteBuffer2.remaining()) {
                this.bVUSize = byteBuffer2.remaining();
                this.mBVU = ByteBuffer.allocateDirect(this.bVUSize);
            }
            this.mBY.rewind();
            this.mBVU.rewind();
            this.mBY.put(byteBuffer);
            this.mBVU.put(byteBuffer2);
            this.mWidth = i;
            this.mHeight = i2;
            this.mStride = i3;
            this.mMutureLock.release();
            return true;
        }

        public void run() {
            try {
                if (FrameProcessor.this.mIsActive) {
                    this.mMutureLock.acquire();
                    this.mBY.rewind();
                    this.mBVU.rewind();
                    this.mFilter.init(this.mWidth, this.mHeight, this.mStride, this.mStride);
                    if (this.mFilter instanceof BeautificationFilter) {
                        this.mFilter.addImage(this.mBY, this.mBVU, 0, new Boolean(false));
                    } else {
                        this.mFilter.addImage(this.mBY, this.mBVU, 0, new Boolean(true));
                    }
                    this.mMutureLock.release();
                }
            } catch (InterruptedException unused) {
            }
        }
    }

    class ProcessingTask implements Runnable, OnImageAvailableListener {
        int height;
        int stride;
        int width;
        int ySize;
        byte[] yvuBytes = null;

        public ProcessingTask() {
        }

        /* JADX WARNING: Can't wrap try/catch for region: R(13:20|21|(1:27)|28|(4:31|(2:33|(2:35|61)(1:60))(4:36|(1:38)(1:39)|40|59)|41|29)|58|42|(1:48)|(1:50)|51|52|53|54) */
        /* JADX WARNING: Missing exception handler attribute for start block: B:52:0x0170 */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onImageAvailable(android.media.ImageReader r15) {
            /*
                r14 = this;
                com.android.camera.imageprocessor.FrameProcessor r0 = com.android.camera.imageprocessor.FrameProcessor.this
                java.lang.Object r0 = r0.mAllocationLock
                monitor-enter(r0)
                com.android.camera.imageprocessor.FrameProcessor r1 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0172 }
                android.renderscript.Allocation r1 = r1.mOutputAllocation     // Catch:{ all -> 0x0172 }
                if (r1 != 0) goto L_0x0011
                monitor-exit(r0)     // Catch:{ all -> 0x0172 }
                return
            L_0x0011:
                android.media.Image r15 = r15.acquireLatestImage()     // Catch:{ IllegalStateException -> 0x0170 }
                if (r15 != 0) goto L_0x0019
                monitor-exit(r0)     // Catch:{ all -> 0x0172 }
                return
            L_0x0019:
                com.android.camera.imageprocessor.FrameProcessor r1 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                boolean r1 = r1.mIsActive     // Catch:{ IllegalStateException -> 0x0170 }
                if (r1 != 0) goto L_0x0026
                r15.close()     // Catch:{ IllegalStateException -> 0x0170 }
                monitor-exit(r0)     // Catch:{ all -> 0x0172 }
                return
            L_0x0026:
                com.android.camera.imageprocessor.FrameProcessor r1 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                r2 = 1
                r1.mIsAllocationEverUsed = r2     // Catch:{ IllegalStateException -> 0x0170 }
                android.media.Image$Plane[] r1 = r15.getPlanes()     // Catch:{ IllegalStateException -> 0x0170 }
                r3 = 0
                r1 = r1[r3]     // Catch:{ IllegalStateException -> 0x0170 }
                java.nio.ByteBuffer r1 = r1.getBuffer()     // Catch:{ IllegalStateException -> 0x0170 }
                android.media.Image$Plane[] r4 = r15.getPlanes()     // Catch:{ IllegalStateException -> 0x0170 }
                r5 = 2
                r4 = r4[r5]     // Catch:{ IllegalStateException -> 0x0170 }
                java.nio.ByteBuffer r11 = r4.getBuffer()     // Catch:{ IllegalStateException -> 0x0170 }
                byte[] r4 = r14.yvuBytes     // Catch:{ IllegalStateException -> 0x0170 }
                if (r4 == 0) goto L_0x0062
                int r4 = r14.width     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.imageprocessor.FrameProcessor r6 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                android.util.Size r6 = r6.mSize     // Catch:{ IllegalStateException -> 0x0170 }
                int r6 = r6.getWidth()     // Catch:{ IllegalStateException -> 0x0170 }
                if (r4 != r6) goto L_0x0062
                int r4 = r14.height     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.imageprocessor.FrameProcessor r6 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                android.util.Size r6 = r6.mSize     // Catch:{ IllegalStateException -> 0x0170 }
                int r6 = r6.getHeight()     // Catch:{ IllegalStateException -> 0x0170 }
                if (r4 == r6) goto L_0x009e
            L_0x0062:
                android.media.Image$Plane[] r4 = r15.getPlanes()     // Catch:{ IllegalStateException -> 0x0170 }
                r4 = r4[r3]     // Catch:{ IllegalStateException -> 0x0170 }
                int r4 = r4.getRowStride()     // Catch:{ IllegalStateException -> 0x0170 }
                r14.stride = r4     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.imageprocessor.FrameProcessor r4 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                android.util.Size r4 = r4.mSize     // Catch:{ IllegalStateException -> 0x0170 }
                int r4 = r4.getWidth()     // Catch:{ IllegalStateException -> 0x0170 }
                r14.width = r4     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.imageprocessor.FrameProcessor r4 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                android.util.Size r4 = r4.mSize     // Catch:{ IllegalStateException -> 0x0170 }
                int r4 = r4.getHeight()     // Catch:{ IllegalStateException -> 0x0170 }
                r14.height = r4     // Catch:{ IllegalStateException -> 0x0170 }
                int r4 = r14.stride     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.imageprocessor.FrameProcessor r6 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                android.util.Size r6 = r6.mSize     // Catch:{ IllegalStateException -> 0x0170 }
                int r6 = r6.getHeight()     // Catch:{ IllegalStateException -> 0x0170 }
                int r4 = r4 * r6
                r14.ySize = r4     // Catch:{ IllegalStateException -> 0x0170 }
                int r4 = r14.ySize     // Catch:{ IllegalStateException -> 0x0170 }
                int r4 = r4 * 3
                int r4 = r4 / r5
                byte[] r4 = new byte[r4]     // Catch:{ IllegalStateException -> 0x0170 }
                r14.yvuBytes = r4     // Catch:{ IllegalStateException -> 0x0170 }
            L_0x009e:
                com.android.camera.imageprocessor.FrameProcessor r4 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                java.util.ArrayList r4 = r4.mPreviewFilters     // Catch:{ IllegalStateException -> 0x0170 }
                java.util.Iterator r12 = r4.iterator()     // Catch:{ IllegalStateException -> 0x0170 }
                r13 = r3
            L_0x00a9:
                boolean r4 = r12.hasNext()     // Catch:{ IllegalStateException -> 0x0170 }
                if (r4 == 0) goto L_0x0128
                java.lang.Object r4 = r12.next()     // Catch:{ IllegalStateException -> 0x0170 }
                r5 = r4
                com.android.camera.imageprocessor.filter.ImageFilter r5 = (com.android.camera.imageprocessor.filter.ImageFilter) r5     // Catch:{ IllegalStateException -> 0x0170 }
                boolean r4 = r5.isFrameListener()     // Catch:{ IllegalStateException -> 0x0170 }
                if (r4 == 0) goto L_0x00f0
                com.android.camera.imageprocessor.FrameProcessor r4 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.imageprocessor.FrameProcessor$ListeningTask r4 = r4.mListeningTask     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.imageprocessor.FrameProcessor r6 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                android.util.Size r6 = r6.mSize     // Catch:{ IllegalStateException -> 0x0170 }
                int r8 = r6.getWidth()     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.imageprocessor.FrameProcessor r6 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                android.util.Size r6 = r6.mSize     // Catch:{ IllegalStateException -> 0x0170 }
                int r9 = r6.getHeight()     // Catch:{ IllegalStateException -> 0x0170 }
                int r10 = r14.stride     // Catch:{ IllegalStateException -> 0x0170 }
                r6 = r1
                r7 = r11
                boolean r4 = r4.setParam(r5, r6, r7, r8, r9, r10)     // Catch:{ IllegalStateException -> 0x0170 }
                if (r4 == 0) goto L_0x0121
                com.android.camera.imageprocessor.FrameProcessor r4 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                android.os.Handler r4 = r4.mListeningHandler     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.imageprocessor.FrameProcessor r5 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.imageprocessor.FrameProcessor$ListeningTask r5 = r5.mListeningTask     // Catch:{ IllegalStateException -> 0x0170 }
                r4.post(r5)     // Catch:{ IllegalStateException -> 0x0170 }
                goto L_0x0121
            L_0x00f0:
                com.android.camera.imageprocessor.FrameProcessor r4 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                android.util.Size r4 = r4.mSize     // Catch:{ IllegalStateException -> 0x0170 }
                int r4 = r4.getWidth()     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.imageprocessor.FrameProcessor r6 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                android.util.Size r6 = r6.mSize     // Catch:{ IllegalStateException -> 0x0170 }
                int r6 = r6.getHeight()     // Catch:{ IllegalStateException -> 0x0170 }
                int r7 = r14.stride     // Catch:{ IllegalStateException -> 0x0170 }
                int r8 = r14.stride     // Catch:{ IllegalStateException -> 0x0170 }
                r5.init(r4, r6, r7, r8)     // Catch:{ IllegalStateException -> 0x0170 }
                boolean r4 = r5 instanceof com.android.camera.imageprocessor.filter.BeautificationFilter     // Catch:{ IllegalStateException -> 0x0170 }
                if (r4 == 0) goto L_0x0118
                java.lang.Boolean r4 = new java.lang.Boolean     // Catch:{ IllegalStateException -> 0x0170 }
                r4.<init>(r3)     // Catch:{ IllegalStateException -> 0x0170 }
                r5.addImage(r1, r11, r3, r4)     // Catch:{ IllegalStateException -> 0x0170 }
                goto L_0x0120
            L_0x0118:
                java.lang.Boolean r4 = new java.lang.Boolean     // Catch:{ IllegalStateException -> 0x0170 }
                r4.<init>(r2)     // Catch:{ IllegalStateException -> 0x0170 }
                r5.addImage(r1, r11, r3, r4)     // Catch:{ IllegalStateException -> 0x0170 }
            L_0x0120:
                r13 = r2
            L_0x0121:
                r1.rewind()     // Catch:{ IllegalStateException -> 0x0170 }
                r11.rewind()     // Catch:{ IllegalStateException -> 0x0170 }
                goto L_0x00a9
            L_0x0128:
                com.android.camera.imageprocessor.FrameProcessor r2 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                boolean r2 = r2.mIsFirstIn     // Catch:{ IllegalStateException -> 0x0170 }
                if (r2 == 0) goto L_0x014e
                com.android.camera.imageprocessor.FrameProcessor r2 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                boolean r2 = r2.mIsVideoOn     // Catch:{ IllegalStateException -> 0x0170 }
                if (r2 == 0) goto L_0x014e
                com.android.camera.imageprocessor.FrameProcessor r2 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                boolean r2 = r2.isFrameListnerEnabled()     // Catch:{ IllegalStateException -> 0x0170 }
                if (r2 == 0) goto L_0x014e
                com.android.camera.imageprocessor.FrameProcessor r2 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                r2.mIsFirstIn = r3     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.imageprocessor.FrameProcessor r2 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.CaptureModule r2 = r2.mModule     // Catch:{ IllegalStateException -> 0x0170 }
                r2.startMediaRecording()     // Catch:{ IllegalStateException -> 0x0170 }
            L_0x014e:
                if (r13 == 0) goto L_0x016d
                byte[] r2 = r14.yvuBytes     // Catch:{ IllegalStateException -> 0x0170 }
                int r4 = r1.remaining()     // Catch:{ IllegalStateException -> 0x0170 }
                r1.get(r2, r3, r4)     // Catch:{ IllegalStateException -> 0x0170 }
                byte[] r1 = r14.yvuBytes     // Catch:{ IllegalStateException -> 0x0170 }
                int r2 = r14.ySize     // Catch:{ IllegalStateException -> 0x0170 }
                int r3 = r11.remaining()     // Catch:{ IllegalStateException -> 0x0170 }
                r11.get(r1, r2, r3)     // Catch:{ IllegalStateException -> 0x0170 }
                com.android.camera.imageprocessor.FrameProcessor r1 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ IllegalStateException -> 0x0170 }
                android.os.Handler r1 = r1.mOutingHandler     // Catch:{ IllegalStateException -> 0x0170 }
                r1.post(r14)     // Catch:{ IllegalStateException -> 0x0170 }
            L_0x016d:
                r15.close()     // Catch:{ IllegalStateException -> 0x0170 }
            L_0x0170:
                monitor-exit(r0)     // Catch:{ all -> 0x0172 }
                return
            L_0x0172:
                r14 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x0172 }
                throw r14
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.imageprocessor.FrameProcessor.ProcessingTask.onImageAvailable(android.media.ImageReader):void");
        }

        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0076, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r6 = this;
                com.android.camera.imageprocessor.FrameProcessor r0 = com.android.camera.imageprocessor.FrameProcessor.this
                java.lang.Object r0 = r0.mAllocationLock
                monitor-enter(r0)
                com.android.camera.imageprocessor.FrameProcessor r1 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0077 }
                boolean r1 = r1.mIsActive     // Catch:{ all -> 0x0077 }
                if (r1 != 0) goto L_0x0011
                monitor-exit(r0)     // Catch:{ all -> 0x0077 }
                return
            L_0x0011:
                com.android.camera.imageprocessor.FrameProcessor r1 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0077 }
                android.renderscript.Allocation r1 = r1.mInputAllocation     // Catch:{ all -> 0x0077 }
                if (r1 != 0) goto L_0x0027
                com.android.camera.imageprocessor.FrameProcessor r1 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0077 }
                int r2 = r6.stride     // Catch:{ all -> 0x0077 }
                int r3 = r6.height     // Catch:{ all -> 0x0077 }
                int r4 = r6.stride     // Catch:{ all -> 0x0077 }
                int r5 = r6.width     // Catch:{ all -> 0x0077 }
                int r4 = r4 - r5
                r1.createAllocation(r2, r3, r4)     // Catch:{ all -> 0x0077 }
            L_0x0027:
                com.android.camera.imageprocessor.FrameProcessor r1 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0077 }
                android.renderscript.Allocation r1 = r1.mInputAllocation     // Catch:{ all -> 0x0077 }
                byte[] r2 = r6.yvuBytes     // Catch:{ all -> 0x0077 }
                r1.copyFrom(r2)     // Catch:{ all -> 0x0077 }
                com.android.camera.imageprocessor.FrameProcessor r1 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0077 }
                com.android.camera.imageprocessor.ScriptC_rotator r1 = r1.mRsRotator     // Catch:{ all -> 0x0077 }
                com.android.camera.imageprocessor.FrameProcessor r2 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0077 }
                android.renderscript.Allocation r2 = r2.mInputAllocation     // Catch:{ all -> 0x0077 }
                r1.forEach_rotate90andMerge(r2)     // Catch:{ all -> 0x0077 }
                com.android.camera.imageprocessor.FrameProcessor r1 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0077 }
                com.android.camera.imageprocessor.ScriptC_YuvToRgb r1 = r1.mRsYuvToRGB     // Catch:{ all -> 0x0077 }
                com.android.camera.imageprocessor.FrameProcessor r2 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0077 }
                android.renderscript.Allocation r2 = r2.mOutputAllocation     // Catch:{ all -> 0x0077 }
                r1.forEach_nv21ToRgb(r2)     // Catch:{ all -> 0x0077 }
                com.android.camera.imageprocessor.FrameProcessor r1 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0077 }
                android.renderscript.Allocation r1 = r1.mOutputAllocation     // Catch:{ all -> 0x0077 }
                r1.ioSend()     // Catch:{ all -> 0x0077 }
                com.android.camera.imageprocessor.FrameProcessor r1 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0077 }
                android.renderscript.Allocation r1 = r1.mVideoOutputAllocation     // Catch:{ all -> 0x0077 }
                if (r1 == 0) goto L_0x0075
                com.android.camera.imageprocessor.FrameProcessor r1 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0077 }
                android.renderscript.Allocation r1 = r1.mVideoOutputAllocation     // Catch:{ all -> 0x0077 }
                com.android.camera.imageprocessor.FrameProcessor r2 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0077 }
                android.renderscript.Allocation r2 = r2.mOutputAllocation     // Catch:{ all -> 0x0077 }
                r1.copyFrom(r2)     // Catch:{ all -> 0x0077 }
                com.android.camera.imageprocessor.FrameProcessor r6 = com.android.camera.imageprocessor.FrameProcessor.this     // Catch:{ all -> 0x0077 }
                android.renderscript.Allocation r6 = r6.mVideoOutputAllocation     // Catch:{ all -> 0x0077 }
                r6.ioSend()     // Catch:{ all -> 0x0077 }
            L_0x0075:
                monitor-exit(r0)     // Catch:{ all -> 0x0077 }
                return
            L_0x0077:
                r6 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x0077 }
                throw r6
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.imageprocessor.FrameProcessor.ProcessingTask.run():void");
        }
    }

    public FrameProcessor(Activity activity, CaptureModule captureModule) {
        this.mActivity = activity;
        this.mModule = captureModule;
        this.mPreviewFilters = new ArrayList<>();
        this.mFinalFilters = new ArrayList<>();
        this.mRs = RenderScript.create(this.mActivity);
        this.mRsYuvToRGB = new ScriptC_YuvToRgb(this.mRs);
        this.mRsRotator = new ScriptC_rotator(this.mRs);
    }

    private void init(Size size) {
        this.mIsActive = true;
        this.mSize = size;
        synchronized (this.mAllocationLock) {
            this.mInputImageReader = ImageReader.newInstance(this.mSize.getWidth(), this.mSize.getHeight(), 35, 8);
            Builder builder = new Builder(this.mRs, Element.RGBA_8888(this.mRs));
            builder.setX(this.mSize.getHeight());
            builder.setY(this.mSize.getWidth());
            this.mOutputAllocation = Allocation.createTyped(this.mRs, builder.create(), 65);
            if (this.mProcessingThread == null) {
                this.mProcessingThread = new HandlerThread("FrameProcessor");
                this.mProcessingThread.start();
                this.mProcessingHandler = new Handler(this.mProcessingThread.getLooper());
            }
            if (this.mOutingThread == null) {
                this.mOutingThread = new HandlerThread("FrameOutingThread");
                this.mOutingThread.start();
                this.mOutingHandler = new Handler(this.mOutingThread.getLooper());
            }
            if (this.mListeningThread == null) {
                this.mListeningThread = new HandlerThread("FrameListeningThread");
                this.mListeningThread.start();
                this.mListeningHandler = new Handler(this.mListeningThread.getLooper());
            }
            this.mListeningTask = new ListeningTask();
            this.mTask = new ProcessingTask();
            this.mInputImageReader.setOnImageAvailableListener(this.mTask, this.mProcessingHandler);
            this.mIsAllocationEverUsed = false;
        }
    }

    /* access modifiers changed from: private */
    public void createAllocation(int i, int i2, int i3) {
        int i4;
        RenderScript renderScript = this.mRs;
        Builder builder = new Builder(renderScript, Element.YUV(renderScript));
        builder.setX(i);
        builder.setY(i2);
        builder.setYuvFormat(17);
        this.mInputAllocation = Allocation.createTyped(this.mRs, builder.create(), 1);
        RenderScript renderScript2 = this.mRs;
        Builder builder2 = new Builder(renderScript2, Element.U8(renderScript2));
        builder2.setX(((i * i2) * 3) / 2);
        this.mProcessAllocation = Allocation.createTyped(this.mRs, builder2.create(), 1);
        this.mRsRotator.set_gIn(this.mInputAllocation);
        this.mRsRotator.set_gOut(this.mProcessAllocation);
        long j = (long) i;
        this.mRsRotator.set_width(j);
        long j2 = (long) i2;
        this.mRsRotator.set_height(j2);
        this.mRsRotator.set_pad((long) i3);
        if (this.mModule.getMainCameraCharacteristics() != null) {
            i4 = ((Integer) this.mModule.getMainCameraCharacteristics().get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
            if (this.mModule.getMainCameraId() == CaptureModule.FRONT_ID) {
                i4 = Math.abs(i4 - 90);
            }
        } else {
            i4 = 90;
        }
        this.mRsRotator.set_degree((long) i4);
        this.mRsYuvToRGB.set_gIn(this.mProcessAllocation);
        this.mRsYuvToRGB.set_width(j2);
        this.mRsYuvToRGB.set_height(j);
    }

    public ArrayList<ImageFilter> getFrameFilters() {
        return this.mFinalFilters;
    }

    private void cleanFilterSet() {
        ArrayList<ImageFilter> arrayList = this.mPreviewFilters;
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ((ImageFilter) it.next()).deinit();
            }
        }
        ArrayList<ImageFilter> arrayList2 = this.mFinalFilters;
        if (arrayList2 != null) {
            Iterator it2 = arrayList2.iterator();
            while (it2.hasNext()) {
                ((ImageFilter) it2.next()).deinit();
            }
        }
        this.mPreviewFilters = new ArrayList<>();
        this.mFinalFilters = new ArrayList<>();
    }

    public void onOpen(ArrayList<Integer> arrayList, Size size) {
        cleanFilterSet();
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                addFilter(((Integer) it.next()).intValue());
            }
        }
        if (isFrameFilterEnabled() || isFrameListnerEnabled()) {
            init(size);
        }
    }

    private void addFilter(int i) {
        ImageFilter imageFilter = i == 1 ? new BeautificationFilter(this.mModule) : i == 2 ? new TrackingFocusFrameListener(this.mModule) : null;
        if (imageFilter != null && imageFilter.isSupported()) {
            this.mPreviewFilters.add(imageFilter);
            if (!imageFilter.isFrameListener()) {
                this.mFinalFilters.add(imageFilter);
            }
        }
    }

    public void onClose() {
        this.mIsActive = false;
        synchronized (this.mAllocationLock) {
            if (this.mIsAllocationEverUsed) {
                if (this.mInputAllocation != null) {
                    this.mInputAllocation.destroy();
                }
                if (this.mOutputAllocation != null) {
                    this.mOutputAllocation.destroy();
                }
                if (this.mProcessAllocation != null) {
                    this.mProcessAllocation.destroy();
                }
                if (this.mVideoOutputAllocation != null) {
                    this.mVideoOutputAllocation.destroy();
                }
            }
            this.mProcessAllocation = null;
            this.mOutputAllocation = null;
            this.mInputAllocation = null;
            this.mVideoOutputAllocation = null;
        }
        HandlerThread handlerThread = this.mProcessingThread;
        if (handlerThread != null) {
            handlerThread.quitSafely();
            try {
                this.mProcessingThread.join();
                this.mProcessingThread = null;
                this.mProcessingHandler = null;
            } catch (InterruptedException unused) {
            }
        }
        HandlerThread handlerThread2 = this.mOutingThread;
        if (handlerThread2 != null) {
            handlerThread2.quitSafely();
            try {
                this.mOutingThread.join();
                this.mOutingThread = null;
                this.mOutingHandler = null;
            } catch (InterruptedException unused2) {
            }
        }
        HandlerThread handlerThread3 = this.mListeningThread;
        if (handlerThread3 != null) {
            handlerThread3.quitSafely();
            try {
                this.mListeningThread.join();
                this.mListeningThread = null;
                this.mListeningHandler = null;
            } catch (InterruptedException unused3) {
            }
        }
        Iterator it = this.mPreviewFilters.iterator();
        while (it.hasNext()) {
            ((ImageFilter) it.next()).deinit();
        }
        Iterator it2 = this.mFinalFilters.iterator();
        while (it2.hasNext()) {
            ((ImageFilter) it2.next()).deinit();
        }
    }

    public void onDestory() {
        RenderScript renderScript = this.mRs;
        if (renderScript != null) {
            renderScript.destroy();
        }
        this.mRs = null;
    }

    private Surface getReaderSurface() {
        synchronized (this.mAllocationLock) {
            if (this.mInputImageReader == null) {
                return null;
            }
            Surface surface = this.mInputImageReader.getSurface();
            return surface;
        }
    }

    public List<Surface> getInputSurfaces() {
        ArrayList arrayList = new ArrayList();
        if (this.mPreviewFilters.size() == 0 && this.mFinalFilters.size() == 0) {
            arrayList.add(this.mSurfaceAsItIs);
            if (this.mIsVideoOn) {
                arrayList.add(this.mVideoSurfaceAsItIs);
            }
        } else if (this.mFinalFilters.size() == 0) {
            arrayList.add(this.mSurfaceAsItIs);
            if (this.mIsVideoOn) {
                arrayList.add(this.mVideoSurfaceAsItIs);
            }
            arrayList.add(getReaderSurface());
        } else {
            arrayList.add(getReaderSurface());
        }
        return arrayList;
    }

    public boolean isFrameFilterEnabled() {
        return this.mFinalFilters.size() != 0;
    }

    public boolean isFrameListnerEnabled() {
        return this.mPreviewFilters.size() != 0;
    }

    public void setOutputSurface(Surface surface) {
        this.mSurfaceAsItIs = surface;
        if (this.mFinalFilters.size() != 0) {
            this.mOutputAllocation.setSurface(surface);
        }
    }

    public void setVideoOutputSurface(Surface surface) {
        if (surface == null) {
            synchronized (this.mAllocationLock) {
                if (this.mVideoOutputAllocation != null) {
                    this.mVideoOutputAllocation.destroy();
                }
                this.mVideoOutputAllocation = null;
            }
            this.mIsVideoOn = false;
            return;
        }
        this.mVideoSurfaceAsItIs = surface;
        this.mIsVideoOn = true;
        this.mIsFirstIn = true;
        if (this.mFinalFilters.size() != 0) {
            synchronized (this.mAllocationLock) {
                if (this.mVideoOutputAllocation == null) {
                    Builder builder = new Builder(this.mRs, Element.RGBA_8888(this.mRs));
                    builder.setX(this.mSize.getHeight());
                    builder.setY(this.mSize.getWidth());
                    this.mVideoOutputAllocation = Allocation.createTyped(this.mRs, builder.create(), 65);
                }
                this.mVideoOutputAllocation.setSurface(surface);
            }
        }
    }
}
