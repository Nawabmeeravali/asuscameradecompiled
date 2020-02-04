package androidx.heifwriter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodec.CodecException;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.heifwriter.HeifEncoder.Callback;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HeifWriter implements AutoCloseable {
    private static final boolean DEBUG = false;
    public static final int INPUT_MODE_BITMAP = 2;
    public static final int INPUT_MODE_BUFFER = 0;
    public static final int INPUT_MODE_SURFACE = 1;
    private static final int MUXER_DATA_FLAG = 16;
    private static final String TAG = "HeifWriter";
    private final List<Pair<Integer, ByteBuffer>> mExifList = new ArrayList();
    private final Handler mHandler;
    private final HandlerThread mHandlerThread;
    private HeifEncoder mHeifEncoder;
    private final int mInputMode;
    final int mMaxImages;
    MediaMuxer mMuxer;
    final AtomicBoolean mMuxerStarted = new AtomicBoolean(false);
    int mNumTiles;
    int mOutputIndex;
    final int mPrimaryIndex;
    final ResultWaiter mResultWaiter = new ResultWaiter();
    final int mRotation;
    private boolean mStarted;
    int[] mTrackIndexArray;

    public static final class Builder {
        private final FileDescriptor mFd;
        private boolean mGridEnabled;
        private Handler mHandler;
        private final int mHeight;
        private final int mInputMode;
        private int mMaxImages;
        private final String mPath;
        private int mPrimaryIndex;
        private int mQuality;
        private int mRotation;
        private final int mWidth;

        public Builder(@NonNull String str, int i, int i2, int i3) {
            this(str, null, i, i2, i3);
        }

        public Builder(@NonNull FileDescriptor fileDescriptor, int i, int i2, int i3) {
            this(null, fileDescriptor, i, i2, i3);
        }

        private Builder(String str, FileDescriptor fileDescriptor, int i, int i2, int i3) {
            this.mGridEnabled = true;
            this.mQuality = 100;
            this.mMaxImages = 1;
            this.mPrimaryIndex = 0;
            this.mRotation = 0;
            if (i <= 0 || i2 <= 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("Invalid image size: ");
                sb.append(i);
                sb.append("x");
                sb.append(i2);
                throw new IllegalArgumentException(sb.toString());
            }
            this.mPath = str;
            this.mFd = fileDescriptor;
            this.mWidth = i;
            this.mHeight = i2;
            this.mInputMode = i3;
        }

        public Builder setRotation(int i) {
            if (i == 0 || i == 90 || i == 180 || i == 270) {
                this.mRotation = i;
                return this;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid rotation angle: ");
            sb.append(i);
            throw new IllegalArgumentException(sb.toString());
        }

        public Builder setGridEnabled(boolean z) {
            this.mGridEnabled = z;
            return this;
        }

        public Builder setQuality(int i) {
            if (i < 0 || i > 100) {
                StringBuilder sb = new StringBuilder();
                sb.append("Invalid quality: ");
                sb.append(i);
                throw new IllegalArgumentException(sb.toString());
            }
            this.mQuality = i;
            return this;
        }

        public Builder setMaxImages(int i) {
            if (i > 0) {
                this.mMaxImages = i;
                return this;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid maxImage: ");
            sb.append(i);
            throw new IllegalArgumentException(sb.toString());
        }

        public Builder setPrimaryIndex(int i) {
            if (i >= 0) {
                this.mPrimaryIndex = i;
                return this;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid primaryIndex: ");
            sb.append(i);
            throw new IllegalArgumentException(sb.toString());
        }

        public Builder setHandler(@Nullable Handler handler) {
            this.mHandler = handler;
            return this;
        }

        public HeifWriter build() throws IOException {
            HeifWriter heifWriter = new HeifWriter(this.mPath, this.mFd, this.mWidth, this.mHeight, this.mRotation, this.mGridEnabled, this.mQuality, this.mMaxImages, this.mPrimaryIndex, this.mInputMode, this.mHandler);
            return heifWriter;
        }
    }

    class HeifCallback extends Callback {
        private boolean mEncoderStopped;

        HeifCallback() {
        }

        public void onOutputFormatChanged(@NonNull HeifEncoder heifEncoder, @NonNull MediaFormat mediaFormat) {
            if (!this.mEncoderStopped) {
                if (HeifWriter.this.mTrackIndexArray != null) {
                    stopAndNotify(new IllegalStateException("Output format changed after muxer started"));
                    return;
                }
                try {
                    HeifWriter.this.mNumTiles = mediaFormat.getInteger("grid-rows") * mediaFormat.getInteger("grid-cols");
                } catch (ClassCastException | NullPointerException unused) {
                    HeifWriter.this.mNumTiles = 1;
                }
                HeifWriter heifWriter = HeifWriter.this;
                heifWriter.mTrackIndexArray = new int[heifWriter.mMaxImages];
                if (heifWriter.mRotation > 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("setting rotation: ");
                    sb.append(HeifWriter.this.mRotation);
                    Log.d(HeifWriter.TAG, sb.toString());
                    HeifWriter heifWriter2 = HeifWriter.this;
                    heifWriter2.mMuxer.setOrientationHint(heifWriter2.mRotation);
                }
                int i = 0;
                while (true) {
                    HeifWriter heifWriter3 = HeifWriter.this;
                    if (i < heifWriter3.mTrackIndexArray.length) {
                        mediaFormat.setInteger("is-default", i == heifWriter3.mPrimaryIndex ? 1 : 0);
                        HeifWriter heifWriter4 = HeifWriter.this;
                        heifWriter4.mTrackIndexArray[i] = heifWriter4.mMuxer.addTrack(mediaFormat);
                        i++;
                    } else {
                        heifWriter3.mMuxer.start();
                        HeifWriter.this.mMuxerStarted.set(true);
                        HeifWriter.this.processExifData();
                        return;
                    }
                }
            }
        }

        public void onDrainOutputBuffer(@NonNull HeifEncoder heifEncoder, @NonNull ByteBuffer byteBuffer) {
            if (!this.mEncoderStopped) {
                HeifWriter heifWriter = HeifWriter.this;
                if (heifWriter.mTrackIndexArray == null) {
                    stopAndNotify(new IllegalStateException("Output buffer received before format info"));
                    return;
                }
                if (heifWriter.mOutputIndex < heifWriter.mMaxImages * heifWriter.mNumTiles) {
                    BufferInfo bufferInfo = new BufferInfo();
                    bufferInfo.set(byteBuffer.position(), byteBuffer.remaining(), 0, 0);
                    HeifWriter heifWriter2 = HeifWriter.this;
                    heifWriter2.mMuxer.writeSampleData(heifWriter2.mTrackIndexArray[heifWriter2.mOutputIndex / heifWriter2.mNumTiles], byteBuffer, bufferInfo);
                }
                HeifWriter heifWriter3 = HeifWriter.this;
                heifWriter3.mOutputIndex++;
                if (heifWriter3.mOutputIndex == heifWriter3.mMaxImages * heifWriter3.mNumTiles) {
                    stopAndNotify(null);
                }
            }
        }

        public void onComplete(@NonNull HeifEncoder heifEncoder) {
            stopAndNotify(null);
        }

        public void onError(@NonNull HeifEncoder heifEncoder, @NonNull CodecException codecException) {
            stopAndNotify(codecException);
        }

        private void stopAndNotify(@Nullable Exception exc) {
            if (!this.mEncoderStopped) {
                this.mEncoderStopped = true;
                HeifWriter.this.mResultWaiter.signalResult(exc);
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface InputMode {
    }

    static class ResultWaiter {
        private boolean mDone;
        private Exception mException;

        ResultWaiter() {
        }

        /* access modifiers changed from: 0000 */
        /* JADX WARNING: Can't wrap try/catch for region: R(5:15|16|17|18|11) */
        /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0020 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:4:0x0009 */
        /* JADX WARNING: Removed duplicated region for block: B:4:0x0009 A[LOOP:0: B:4:0x0009->B:34:0x0009, LOOP_START, SYNTHETIC] */
        /* JADX WARNING: Unknown top exception splitter block from list: {B:17:0x0020=Splitter:B:17:0x0020, B:4:0x0009=Splitter:B:4:0x0009} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized void waitForResult(long r7) throws java.lang.Exception {
            /*
                r6 = this;
                monitor-enter(r6)
                r0 = 0
                int r2 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
                if (r2 < 0) goto L_0x0040
                if (r2 != 0) goto L_0x0011
            L_0x0009:
                boolean r7 = r6.mDone     // Catch:{ all -> 0x0048 }
                if (r7 != 0) goto L_0x0027
                r6.wait()     // Catch:{ InterruptedException -> 0x0009 }
                goto L_0x0009
            L_0x0011:
                long r2 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x0048 }
            L_0x0015:
                boolean r4 = r6.mDone     // Catch:{ all -> 0x0048 }
                if (r4 != 0) goto L_0x0027
                int r4 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
                if (r4 <= 0) goto L_0x0027
                r6.wait(r7)     // Catch:{ InterruptedException -> 0x0020 }
            L_0x0020:
                long r4 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x0048 }
                long r4 = r4 - r2
                long r7 = r7 - r4
                goto L_0x0015
            L_0x0027:
                boolean r7 = r6.mDone     // Catch:{ all -> 0x0048 }
                if (r7 != 0) goto L_0x0037
                r7 = 1
                r6.mDone = r7     // Catch:{ all -> 0x0048 }
                java.util.concurrent.TimeoutException r7 = new java.util.concurrent.TimeoutException     // Catch:{ all -> 0x0048 }
                java.lang.String r8 = "timed out waiting for result"
                r7.<init>(r8)     // Catch:{ all -> 0x0048 }
                r6.mException = r7     // Catch:{ all -> 0x0048 }
            L_0x0037:
                java.lang.Exception r7 = r6.mException     // Catch:{ all -> 0x0048 }
                if (r7 != 0) goto L_0x003d
                monitor-exit(r6)
                return
            L_0x003d:
                java.lang.Exception r7 = r6.mException     // Catch:{ all -> 0x0048 }
                throw r7     // Catch:{ all -> 0x0048 }
            L_0x0040:
                java.lang.IllegalArgumentException r7 = new java.lang.IllegalArgumentException     // Catch:{ all -> 0x0048 }
                java.lang.String r8 = "timeoutMs is negative"
                r7.<init>(r8)     // Catch:{ all -> 0x0048 }
                throw r7     // Catch:{ all -> 0x0048 }
            L_0x0048:
                r7 = move-exception
                monitor-exit(r6)
                throw r7
            */
            throw new UnsupportedOperationException("Method not decompiled: androidx.heifwriter.HeifWriter.ResultWaiter.waitForResult(long):void");
        }

        /* access modifiers changed from: 0000 */
        public synchronized void signalResult(@Nullable Exception exc) {
            if (!this.mDone) {
                this.mDone = true;
                this.mException = exc;
                notifyAll();
            }
        }
    }

    @SuppressLint({"WrongConstant"})
    HeifWriter(@NonNull String str, @NonNull FileDescriptor fileDescriptor, int i, int i2, int i3, boolean z, int i4, int i5, int i6, int i7, @Nullable Handler handler) throws IOException {
        MediaMuxer mediaMuxer;
        String str2 = str;
        int i8 = i5;
        int i9 = i6;
        if (i9 < i8) {
            MediaFormat.createVideoFormat("image/vnd.android.heic", i, i2);
            this.mNumTiles = 1;
            this.mRotation = i3;
            this.mInputMode = i7;
            this.mMaxImages = i8;
            this.mPrimaryIndex = i9;
            Looper looper = handler != null ? handler.getLooper() : null;
            if (looper == null) {
                this.mHandlerThread = new HandlerThread("HeifEncoderThread", -2);
                this.mHandlerThread.start();
                looper = this.mHandlerThread.getLooper();
            } else {
                this.mHandlerThread = null;
            }
            this.mHandler = new Handler(looper);
            if (str2 != null) {
                mediaMuxer = new MediaMuxer(str, 3);
            } else {
                FileDescriptor fileDescriptor2 = fileDescriptor;
                mediaMuxer = new MediaMuxer(fileDescriptor, 3);
            }
            this.mMuxer = mediaMuxer;
            HeifEncoder heifEncoder = new HeifEncoder(i, i2, z, i4, this.mInputMode, this.mHandler, new HeifCallback());
            this.mHeifEncoder = heifEncoder;
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Invalid maxImages (");
        sb.append(i8);
        sb.append(") or primaryIndex (");
        sb.append(i9);
        sb.append(")");
        throw new IllegalArgumentException(sb.toString());
    }

    public void start() {
        checkStarted(false);
        this.mStarted = true;
        this.mHeifEncoder.start();
    }

    public void addYuvBuffer(int i, @NonNull byte[] bArr) {
        checkStartedAndMode(0);
        synchronized (this) {
            if (this.mHeifEncoder != null) {
                this.mHeifEncoder.addYuvBuffer(i, bArr);
            }
        }
    }

    @NonNull
    public Surface getInputSurface() {
        checkStarted(false);
        checkMode(1);
        return this.mHeifEncoder.getInputSurface();
    }

    public void setInputEndOfStreamTimestamp(long j) {
        checkStartedAndMode(1);
        synchronized (this) {
            if (this.mHeifEncoder != null) {
                this.mHeifEncoder.setEndOfInputStreamTimestamp(j);
            }
        }
    }

    public void addBitmap(@NonNull Bitmap bitmap) {
        checkStartedAndMode(2);
        synchronized (this) {
            if (this.mHeifEncoder != null) {
                this.mHeifEncoder.addBitmap(bitmap);
            }
        }
    }

    public void addExifData(int i, @NonNull byte[] bArr, int i2, int i3) {
        checkStarted(true);
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(i3);
        allocateDirect.put(bArr, i2, i3);
        allocateDirect.flip();
        synchronized (this.mExifList) {
            this.mExifList.add(new Pair(Integer.valueOf(i), allocateDirect));
        }
        processExifData();
    }

    /* access modifiers changed from: 0000 */
    @SuppressLint({"WrongConstant"})
    public void processExifData() {
        Pair pair;
        if (this.mMuxerStarted.get()) {
            while (true) {
                synchronized (this.mExifList) {
                    if (!this.mExifList.isEmpty()) {
                        pair = (Pair) this.mExifList.remove(0);
                    } else {
                        return;
                    }
                }
                BufferInfo bufferInfo = new BufferInfo();
                bufferInfo.set(((ByteBuffer) pair.second).position(), ((ByteBuffer) pair.second).remaining(), 0, 16);
                this.mMuxer.writeSampleData(this.mTrackIndexArray[((Integer) pair.first).intValue()], (ByteBuffer) pair.second, bufferInfo);
            }
            while (true) {
            }
        }
    }

    public void stop(long j) throws Exception {
        checkStarted(true);
        synchronized (this) {
            if (this.mHeifEncoder != null) {
                this.mHeifEncoder.stopAsync();
            }
        }
        this.mResultWaiter.waitForResult(j);
        processExifData();
        closeInternal();
    }

    private void checkStarted(boolean z) {
        if (this.mStarted != z) {
            throw new IllegalStateException("Already started");
        }
    }

    private void checkMode(int i) {
        if (this.mInputMode != i) {
            StringBuilder sb = new StringBuilder();
            sb.append("Not valid in input mode ");
            sb.append(this.mInputMode);
            throw new IllegalStateException(sb.toString());
        }
    }

    private void checkStartedAndMode(int i) {
        checkStarted(true);
        checkMode(i);
    }

    /* access modifiers changed from: 0000 */
    public void closeInternal() {
        MediaMuxer mediaMuxer = this.mMuxer;
        if (mediaMuxer != null) {
            mediaMuxer.stop();
            this.mMuxer.release();
            this.mMuxer = null;
        }
        HeifEncoder heifEncoder = this.mHeifEncoder;
        if (heifEncoder != null) {
            heifEncoder.close();
            synchronized (this) {
                this.mHeifEncoder = null;
            }
        }
    }

    public void close() {
        this.mHandler.postAtFrontOfQueue(new Runnable() {
            public void run() {
                try {
                    HeifWriter.this.closeInternal();
                } catch (Exception unused) {
                }
            }
        });
    }
}
