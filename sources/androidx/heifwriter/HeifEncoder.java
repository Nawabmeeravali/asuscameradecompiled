package androidx.heifwriter;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.Image;
import android.media.Image.Plane;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodec.CodecException;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HeifEncoder implements AutoCloseable, OnFrameAvailableListener {
    private static final boolean DEBUG = false;
    private static final int GRID_HEIGHT = 512;
    private static final int GRID_WIDTH = 512;
    private static final int INPUT_BUFFER_POOL_SIZE = 2;
    public static final int INPUT_MODE_BITMAP = 2;
    public static final int INPUT_MODE_BUFFER = 0;
    public static final int INPUT_MODE_SURFACE = 1;
    private static final double MAX_COMPRESS_RATIO = 0.25d;
    private static final String TAG = "HeifEncoder";
    final Callback mCallback;
    final ArrayList<Integer> mCodecInputBuffers = new ArrayList<>();
    private ByteBuffer mCurrentBuffer;
    private final Rect mDstRect;
    SurfaceEOSTracker mEOSTracker;
    private final ArrayList<ByteBuffer> mEmptyBuffers = new ArrayList<>();
    MediaCodec mEncoder;
    private EglWindowSurface mEncoderEglSurface;
    private Surface mEncoderSurface;
    private final ArrayList<ByteBuffer> mFilledBuffers = new ArrayList<>();
    final int mGridCols;
    final int mGridHeight;
    final int mGridRows;
    final int mGridWidth;
    final Handler mHandler;
    private final HandlerThread mHandlerThread;
    final int mHeight;
    boolean mInputEOS;
    private int mInputIndex;
    private final int mInputMode;
    private Surface mInputSurface;
    private SurfaceTexture mInputTexture;
    private final int mNumTiles;
    private EglRectBlt mRectBlt;
    private final Rect mSrcRect;
    private final AtomicBoolean mStopping = new AtomicBoolean(false);
    private int mTextureId;
    private final float[] mTmpMatrix = new float[16];
    final boolean mUseGrid;
    final int mWidth;

    public static abstract class Callback {
        public abstract void onComplete(@NonNull HeifEncoder heifEncoder);

        public abstract void onDrainOutputBuffer(@NonNull HeifEncoder heifEncoder, @NonNull ByteBuffer byteBuffer);

        public abstract void onError(@NonNull HeifEncoder heifEncoder, @NonNull CodecException codecException);

        public abstract void onOutputFormatChanged(@NonNull HeifEncoder heifEncoder, @NonNull MediaFormat mediaFormat);
    }

    class EncoderCallback extends android.media.MediaCodec.Callback {
        private boolean mOutputEOS;

        EncoderCallback() {
        }

        public void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat) {
            if (mediaCodec == HeifEncoder.this.mEncoder) {
                String str = "mime";
                String str2 = "image/vnd.android.heic";
                if (!str2.equals(mediaFormat.getString(str))) {
                    mediaFormat.setString(str, str2);
                    mediaFormat.setInteger("width", HeifEncoder.this.mWidth);
                    mediaFormat.setInteger("height", HeifEncoder.this.mHeight);
                    HeifEncoder heifEncoder = HeifEncoder.this;
                    if (heifEncoder.mUseGrid) {
                        mediaFormat.setInteger("tile-width", heifEncoder.mGridWidth);
                        mediaFormat.setInteger("tile-height", HeifEncoder.this.mGridHeight);
                        mediaFormat.setInteger("grid-rows", HeifEncoder.this.mGridRows);
                        mediaFormat.setInteger("grid-cols", HeifEncoder.this.mGridCols);
                    }
                }
                HeifEncoder heifEncoder2 = HeifEncoder.this;
                heifEncoder2.mCallback.onOutputFormatChanged(heifEncoder2, mediaFormat);
            }
        }

        public void onInputBufferAvailable(MediaCodec mediaCodec, int i) {
            HeifEncoder heifEncoder = HeifEncoder.this;
            if (mediaCodec == heifEncoder.mEncoder && !heifEncoder.mInputEOS) {
                heifEncoder.mCodecInputBuffers.add(Integer.valueOf(i));
                HeifEncoder.this.maybeCopyOneTileYUV();
            }
        }

        public void onOutputBufferAvailable(MediaCodec mediaCodec, int i, BufferInfo bufferInfo) {
            if (mediaCodec == HeifEncoder.this.mEncoder && !this.mOutputEOS) {
                if (bufferInfo.size > 0 && (bufferInfo.flags & 2) == 0) {
                    ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(i);
                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                    SurfaceEOSTracker surfaceEOSTracker = HeifEncoder.this.mEOSTracker;
                    if (surfaceEOSTracker != null) {
                        surfaceEOSTracker.updateLastOutputTime(bufferInfo.presentationTimeUs);
                    }
                    HeifEncoder heifEncoder = HeifEncoder.this;
                    heifEncoder.mCallback.onDrainOutputBuffer(heifEncoder, outputBuffer);
                }
                this.mOutputEOS = ((bufferInfo.flags & 4) != 0) | this.mOutputEOS;
                mediaCodec.releaseOutputBuffer(i, false);
                if (this.mOutputEOS) {
                    stopAndNotify(null);
                }
            }
        }

        public void onError(MediaCodec mediaCodec, CodecException codecException) {
            if (mediaCodec == HeifEncoder.this.mEncoder) {
                StringBuilder sb = new StringBuilder();
                sb.append("onError: ");
                sb.append(codecException);
                Log.e(HeifEncoder.TAG, sb.toString());
                stopAndNotify(codecException);
            }
        }

        private void stopAndNotify(@Nullable CodecException codecException) {
            HeifEncoder.this.stopInternal();
            if (codecException == null) {
                HeifEncoder heifEncoder = HeifEncoder.this;
                heifEncoder.mCallback.onComplete(heifEncoder);
                return;
            }
            HeifEncoder heifEncoder2 = HeifEncoder.this;
            heifEncoder2.mCallback.onError(heifEncoder2, codecException);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface InputMode {
    }

    private class SurfaceEOSTracker {
        private static final boolean DEBUG_EOS = false;
        final boolean mCopyTiles;
        long mEncoderEOSTimeUs = -1;
        long mInputEOSTimeNs = -1;
        long mLastEncoderTimeUs = -1;
        long mLastInputTimeNs = -1;
        long mLastOutputTimeUs = -1;
        boolean mSignaled;

        SurfaceEOSTracker(boolean z) {
            this.mCopyTiles = z;
        }

        /* access modifiers changed from: 0000 */
        public synchronized void updateInputEOSTime(long j) {
            if (this.mCopyTiles) {
                if (this.mInputEOSTimeNs < 0) {
                    this.mInputEOSTimeNs = j;
                }
            } else if (this.mEncoderEOSTimeUs < 0) {
                this.mEncoderEOSTimeUs = j / 1000;
            }
            updateEOSLocked();
        }

        /* access modifiers changed from: 0000 */
        /* JADX WARNING: Removed duplicated region for block: B:10:0x0015  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized boolean updateLastInputAndEncoderTime(long r5, long r7) {
            /*
                r4 = this;
                monitor-enter(r4)
                long r0 = r4.mInputEOSTimeNs     // Catch:{ all -> 0x001e }
                r2 = 0
                int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
                if (r0 < 0) goto L_0x0012
                long r0 = r4.mInputEOSTimeNs     // Catch:{ all -> 0x001e }
                int r0 = (r5 > r0 ? 1 : (r5 == r0 ? 0 : -1))
                if (r0 > 0) goto L_0x0010
                goto L_0x0012
            L_0x0010:
                r0 = 0
                goto L_0x0013
            L_0x0012:
                r0 = 1
            L_0x0013:
                if (r0 == 0) goto L_0x0017
                r4.mLastEncoderTimeUs = r7     // Catch:{ all -> 0x001e }
            L_0x0017:
                r4.mLastInputTimeNs = r5     // Catch:{ all -> 0x001e }
                r4.updateEOSLocked()     // Catch:{ all -> 0x001e }
                monitor-exit(r4)
                return r0
            L_0x001e:
                r5 = move-exception
                monitor-exit(r4)
                throw r5
            */
            throw new UnsupportedOperationException("Method not decompiled: androidx.heifwriter.HeifEncoder.SurfaceEOSTracker.updateLastInputAndEncoderTime(long, long):boolean");
        }

        /* access modifiers changed from: 0000 */
        public synchronized void updateLastOutputTime(long j) {
            this.mLastOutputTimeUs = j;
            updateEOSLocked();
        }

        private void updateEOSLocked() {
            if (!this.mSignaled) {
                if (this.mEncoderEOSTimeUs < 0) {
                    long j = this.mInputEOSTimeNs;
                    if (j >= 0 && this.mLastInputTimeNs >= j) {
                        long j2 = this.mLastEncoderTimeUs;
                        if (j2 < 0) {
                            doSignalEOSLocked();
                            return;
                        }
                        this.mEncoderEOSTimeUs = j2;
                    }
                }
                long j3 = this.mEncoderEOSTimeUs;
                if (j3 >= 0 && j3 <= this.mLastOutputTimeUs) {
                    doSignalEOSLocked();
                }
            }
        }

        private void doSignalEOSLocked() {
            HeifEncoder.this.mHandler.post(new Runnable() {
                public void run() {
                    MediaCodec mediaCodec = HeifEncoder.this.mEncoder;
                    if (mediaCodec != null) {
                        mediaCodec.signalEndOfInputStream();
                    }
                }
            });
            this.mSignaled = true;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:67:0x021d  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0272  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public HeifEncoder(int r17, int r18, boolean r19, int r20, int r21, @androidx.annotation.Nullable android.os.Handler r22, @androidx.annotation.NonNull androidx.heifwriter.HeifEncoder.Callback r23) throws java.io.IOException {
        /*
            r16 = this;
            r0 = r16
            r1 = r17
            r2 = r18
            r3 = r20
            r4 = r21
            java.lang.String r5 = "video/hevc"
            java.lang.String r6 = "image/vnd.android.heic"
            r16.<init>()
            java.util.ArrayList r7 = new java.util.ArrayList
            r7.<init>()
            r0.mEmptyBuffers = r7
            java.util.ArrayList r7 = new java.util.ArrayList
            r7.<init>()
            r0.mFilledBuffers = r7
            java.util.ArrayList r7 = new java.util.ArrayList
            r7.<init>()
            r0.mCodecInputBuffers = r7
            r7 = 16
            float[] r7 = new float[r7]
            r0.mTmpMatrix = r7
            java.util.concurrent.atomic.AtomicBoolean r7 = new java.util.concurrent.atomic.AtomicBoolean
            r8 = 0
            r7.<init>(r8)
            r0.mStopping = r7
            if (r1 < 0) goto L_0x02a3
            if (r2 < 0) goto L_0x02a3
            if (r3 < 0) goto L_0x02a3
            r7 = 100
            if (r3 > r7) goto L_0x02a3
            r7 = 512(0x200, float:7.175E-43)
            r9 = 1
            if (r1 > r7) goto L_0x0048
            if (r2 <= r7) goto L_0x0046
            goto L_0x0048
        L_0x0046:
            r10 = r8
            goto L_0x0049
        L_0x0048:
            r10 = r9
        L_0x0049:
            r10 = r19 & r10
            r11 = 0
            android.media.MediaCodec r12 = android.media.MediaCodec.createEncoderByType(r6)     // Catch:{ Exception -> 0x0076 }
            r0.mEncoder = r12     // Catch:{ Exception -> 0x0076 }
            android.media.MediaCodec r12 = r0.mEncoder     // Catch:{ Exception -> 0x0076 }
            android.media.MediaCodecInfo r12 = r12.getCodecInfo()     // Catch:{ Exception -> 0x0076 }
            android.media.MediaCodecInfo$CodecCapabilities r12 = r12.getCapabilitiesForType(r6)     // Catch:{ Exception -> 0x0076 }
            android.media.MediaCodecInfo$VideoCapabilities r13 = r12.getVideoCapabilities()     // Catch:{ Exception -> 0x0076 }
            boolean r13 = r13.isSizeSupported(r1, r2)     // Catch:{ Exception -> 0x0076 }
            if (r13 == 0) goto L_0x0069
            r13 = r12
            r12 = r9
            goto L_0x0092
        L_0x0069:
            android.media.MediaCodec r12 = r0.mEncoder     // Catch:{ Exception -> 0x0076 }
            r12.release()     // Catch:{ Exception -> 0x0076 }
            r0.mEncoder = r11     // Catch:{ Exception -> 0x0076 }
            java.lang.Exception r12 = new java.lang.Exception     // Catch:{ Exception -> 0x0076 }
            r12.<init>()     // Catch:{ Exception -> 0x0076 }
            throw r12     // Catch:{ Exception -> 0x0076 }
        L_0x0076:
            android.media.MediaCodec r12 = android.media.MediaCodec.createEncoderByType(r5)
            r0.mEncoder = r12
            android.media.MediaCodec r12 = r0.mEncoder
            android.media.MediaCodecInfo r12 = r12.getCodecInfo()
            android.media.MediaCodecInfo$CodecCapabilities r12 = r12.getCapabilitiesForType(r5)
            android.media.MediaCodecInfo$VideoCapabilities r13 = r12.getVideoCapabilities()
            boolean r13 = r13.isSizeSupported(r1, r2)
            r13 = r13 ^ r9
            r10 = r10 | r13
            r13 = r12
            r12 = r8
        L_0x0092:
            r0.mInputMode = r4
            r14 = r23
            r0.mCallback = r14
            if (r22 == 0) goto L_0x009f
            android.os.Looper r14 = r22.getLooper()
            goto L_0x00a0
        L_0x009f:
            r14 = r11
        L_0x00a0:
            if (r14 != 0) goto L_0x00b8
            android.os.HandlerThread r14 = new android.os.HandlerThread
            r15 = -2
            java.lang.String r8 = "HeifEncoderThread"
            r14.<init>(r8, r15)
            r0.mHandlerThread = r14
            android.os.HandlerThread r8 = r0.mHandlerThread
            r8.start()
            android.os.HandlerThread r8 = r0.mHandlerThread
            android.os.Looper r14 = r8.getLooper()
            goto L_0x00ba
        L_0x00b8:
            r0.mHandlerThread = r11
        L_0x00ba:
            android.os.Handler r8 = new android.os.Handler
            r8.<init>(r14)
            r0.mHandler = r8
            r8 = 2
            if (r4 == r9) goto L_0x00c9
            if (r4 != r8) goto L_0x00c7
            goto L_0x00c9
        L_0x00c7:
            r14 = 0
            goto L_0x00ca
        L_0x00c9:
            r14 = r9
        L_0x00ca:
            if (r14 == 0) goto L_0x00d0
            r15 = 2130708361(0x7f000789, float:1.701803E38)
            goto L_0x00d3
        L_0x00d0:
            r15 = 2135033992(0x7f420888, float:2.5791453E38)
        L_0x00d3:
            r0.mWidth = r1
            r0.mHeight = r2
            r0.mUseGrid = r10
            if (r10 == 0) goto L_0x00e7
            int r11 = r2 + 512
            int r11 = r11 - r9
            int r11 = r11 / r7
            int r8 = r1 + 512
            int r8 = r8 - r9
            int r8 = r8 / r7
            r9 = r11
            r11 = r8
            r8 = r7
            goto L_0x00ec
        L_0x00e7:
            int r7 = r0.mWidth
            int r8 = r0.mHeight
            r11 = r9
        L_0x00ec:
            if (r12 == 0) goto L_0x00f7
            int r5 = r0.mWidth
            int r4 = r0.mHeight
            android.media.MediaFormat r4 = android.media.MediaFormat.createVideoFormat(r6, r5, r4)
            goto L_0x00fb
        L_0x00f7:
            android.media.MediaFormat r4 = android.media.MediaFormat.createVideoFormat(r5, r7, r8)
        L_0x00fb:
            if (r10 == 0) goto L_0x0111
            java.lang.String r5 = "tile-width"
            r4.setInteger(r5, r7)
            java.lang.String r5 = "tile-height"
            r4.setInteger(r5, r8)
            java.lang.String r5 = "grid-cols"
            r4.setInteger(r5, r11)
            java.lang.String r5 = "grid-rows"
            r4.setInteger(r5, r9)
        L_0x0111:
            if (r12 == 0) goto L_0x011d
            r0.mGridWidth = r1
            r0.mGridHeight = r2
            r5 = 1
            r0.mGridRows = r5
            r0.mGridCols = r5
            goto L_0x0125
        L_0x011d:
            r0.mGridWidth = r7
            r0.mGridHeight = r8
            r0.mGridRows = r9
            r0.mGridCols = r11
        L_0x0125:
            int r5 = r0.mGridRows
            int r6 = r0.mGridCols
            int r5 = r5 * r6
            r0.mNumTiles = r5
            java.lang.String r5 = "i-frame-interval"
            r6 = 0
            r4.setInteger(r5, r6)
            java.lang.String r5 = "color-format"
            r4.setInteger(r5, r15)
            int r5 = r0.mNumTiles
            java.lang.String r6 = "frame-rate"
            r4.setInteger(r6, r5)
            int r5 = r0.mNumTiles
            int r5 = r5 * 30
            java.lang.String r6 = "capture-rate"
            r4.setInteger(r6, r5)
            android.media.MediaCodecInfo$EncoderCapabilities r5 = r13.getEncoderCapabilities()
            r6 = 0
            boolean r7 = r5.isBitrateModeSupported(r6)
            r8 = 4636737291354636288(0x4059000000000000, double:100.0)
            java.lang.String r6 = "bitrate-mode"
            java.lang.String r11 = "HeifEncoder"
            if (r7 == 0) goto L_0x01a4
            java.lang.String r1 = "Setting bitrate mode to constant quality"
            android.util.Log.d(r11, r1)
            android.util.Range r1 = r5.getQualityRange()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r5 = "Quality range: "
            r2.append(r5)
            r2.append(r1)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r11, r2)
            r2 = 0
            r4.setInteger(r6, r2)
            java.lang.Comparable r2 = r1.getLower()
            java.lang.Integer r2 = (java.lang.Integer) r2
            int r2 = r2.intValue()
            double r5 = (double) r2
            java.lang.Comparable r2 = r1.getUpper()
            java.lang.Integer r2 = (java.lang.Integer) r2
            int r2 = r2.intValue()
            java.lang.Comparable r1 = r1.getLower()
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r1 = r1.intValue()
            int r2 = r2 - r1
            int r2 = r2 * r3
            double r1 = (double) r2
            double r1 = r1 / r8
            double r5 = r5 + r1
            int r1 = (int) r5
            java.lang.String r2 = "quality"
            r4.setInteger(r2, r1)
            goto L_0x01e7
        L_0x01a4:
            r7 = 2
            boolean r5 = r5.isBitrateModeSupported(r7)
            if (r5 == 0) goto L_0x01b4
            java.lang.String r5 = "Setting bitrate mode to constant bitrate"
            android.util.Log.d(r11, r5)
            r4.setInteger(r6, r7)
            goto L_0x01bd
        L_0x01b4:
            java.lang.String r5 = "Setting bitrate mode to variable bitrate"
            android.util.Log.d(r11, r5)
            r5 = 1
            r4.setInteger(r6, r5)
        L_0x01bd:
            android.media.MediaCodecInfo$VideoCapabilities r5 = r13.getVideoCapabilities()
            android.util.Range r5 = r5.getBitrateRange()
            int r1 = r1 * r2
            double r1 = (double) r1
            r6 = 4609434218613702656(0x3ff8000000000000, double:1.5)
            double r1 = r1 * r6
            r6 = 4620693217682128896(0x4020000000000000, double:8.0)
            double r1 = r1 * r6
            r6 = 4598175219545276416(0x3fd0000000000000, double:0.25)
            double r1 = r1 * r6
            double r6 = (double) r3
            double r1 = r1 * r6
            double r1 = r1 / r8
            int r1 = (int) r1
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            java.lang.Comparable r1 = r5.clamp(r1)
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r1 = r1.intValue()
            java.lang.String r2 = "bitrate"
            r4.setInteger(r2, r1)
        L_0x01e7:
            android.media.MediaCodec r1 = r0.mEncoder
            androidx.heifwriter.HeifEncoder$EncoderCallback r2 = new androidx.heifwriter.HeifEncoder$EncoderCallback
            r2.<init>()
            android.os.Handler r3 = r0.mHandler
            r1.setCallback(r2, r3)
            android.media.MediaCodec r1 = r0.mEncoder
            r2 = 0
            r3 = 1
            r1.configure(r4, r2, r2, r3)
            if (r14 == 0) goto L_0x0277
            android.media.MediaCodec r1 = r0.mEncoder
            android.view.Surface r1 = r1.createInputSurface()
            r0.mEncoderSurface = r1
            if (r10 == 0) goto L_0x020c
            if (r12 == 0) goto L_0x0209
            goto L_0x020c
        L_0x0209:
            r1 = r21
            goto L_0x0211
        L_0x020c:
            r1 = r21
            r2 = 2
            if (r1 != r2) goto L_0x0213
        L_0x0211:
            r2 = 1
            goto L_0x0214
        L_0x0213:
            r2 = 0
        L_0x0214:
            androidx.heifwriter.HeifEncoder$SurfaceEOSTracker r3 = new androidx.heifwriter.HeifEncoder$SurfaceEOSTracker
            r3.<init>(r2)
            r0.mEOSTracker = r3
            if (r2 == 0) goto L_0x0272
            androidx.heifwriter.EglWindowSurface r2 = new androidx.heifwriter.EglWindowSurface
            android.view.Surface r3 = r0.mEncoderSurface
            r2.<init>(r3)
            r0.mEncoderEglSurface = r2
            androidx.heifwriter.EglWindowSurface r2 = r0.mEncoderEglSurface
            r2.makeCurrent()
            androidx.heifwriter.EglRectBlt r2 = new androidx.heifwriter.EglRectBlt
            androidx.heifwriter.Texture2dProgram r3 = new androidx.heifwriter.Texture2dProgram
            r4 = 2
            if (r1 != r4) goto L_0x0234
            r4 = 0
            goto L_0x0235
        L_0x0234:
            r4 = 1
        L_0x0235:
            r3.<init>(r4)
            int r4 = r0.mWidth
            int r5 = r0.mHeight
            r2.<init>(r3, r4, r5)
            r0.mRectBlt = r2
            androidx.heifwriter.EglRectBlt r2 = r0.mRectBlt
            int r2 = r2.createTextureObject()
            r0.mTextureId = r2
            r2 = 1
            if (r1 != r2) goto L_0x026c
            android.graphics.SurfaceTexture r1 = new android.graphics.SurfaceTexture
            int r3 = r0.mTextureId
            r1.<init>(r3, r2)
            r0.mInputTexture = r1
            android.graphics.SurfaceTexture r1 = r0.mInputTexture
            r1.setOnFrameAvailableListener(r0)
            android.graphics.SurfaceTexture r1 = r0.mInputTexture
            int r2 = r0.mWidth
            int r3 = r0.mHeight
            r1.setDefaultBufferSize(r2, r3)
            android.view.Surface r1 = new android.view.Surface
            android.graphics.SurfaceTexture r2 = r0.mInputTexture
            r1.<init>(r2)
            r0.mInputSurface = r1
        L_0x026c:
            androidx.heifwriter.EglWindowSurface r1 = r0.mEncoderEglSurface
            r1.makeUnCurrent()
            goto L_0x028f
        L_0x0272:
            android.view.Surface r1 = r0.mEncoderSurface
            r0.mInputSurface = r1
            goto L_0x028f
        L_0x0277:
            r1 = 0
            r2 = 2
        L_0x0279:
            if (r1 >= r2) goto L_0x028f
            java.util.ArrayList<java.nio.ByteBuffer> r3 = r0.mEmptyBuffers
            int r4 = r0.mWidth
            int r5 = r0.mHeight
            int r4 = r4 * r5
            int r4 = r4 * 3
            int r4 = r4 / r2
            java.nio.ByteBuffer r4 = java.nio.ByteBuffer.allocateDirect(r4)
            r3.add(r4)
            int r1 = r1 + 1
            goto L_0x0279
        L_0x028f:
            android.graphics.Rect r1 = new android.graphics.Rect
            int r2 = r0.mGridWidth
            int r3 = r0.mGridHeight
            r4 = 0
            r1.<init>(r4, r4, r2, r3)
            r0.mDstRect = r1
            android.graphics.Rect r1 = new android.graphics.Rect
            r1.<init>()
            r0.mSrcRect = r1
            return
        L_0x02a3:
            java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
            java.lang.String r1 = "invalid encoder inputs"
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.heifwriter.HeifEncoder.<init>(int, int, boolean, int, int, android.os.Handler, androidx.heifwriter.HeifEncoder$Callback):void");
    }

    private void copyTilesGL() {
        GLES20.glViewport(0, 0, this.mGridWidth, this.mGridHeight);
        for (int i = 0; i < this.mGridRows; i++) {
            int i2 = 0;
            while (i2 < this.mGridCols) {
                int i3 = this.mGridWidth;
                int i4 = i2 * i3;
                int i5 = this.mGridHeight;
                int i6 = i * i5;
                this.mSrcRect.set(i4, i6, i3 + i4, i5 + i6);
                try {
                    this.mRectBlt.copyRect(this.mTextureId, Texture2dProgram.V_FLIP_MATRIX, this.mSrcRect);
                    EglWindowSurface eglWindowSurface = this.mEncoderEglSurface;
                    int i7 = this.mInputIndex;
                    this.mInputIndex = i7 + 1;
                    eglWindowSurface.setPresentationTime(computePresentationTime(i7) * 1000);
                    this.mEncoderEglSurface.swapBuffers();
                    i2++;
                } catch (RuntimeException e) {
                    if (!this.mStopping.get()) {
                        throw e;
                    }
                    return;
                }
            }
        }
    }

    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            if (this.mEncoderEglSurface != null) {
                this.mEncoderEglSurface.makeCurrent();
                surfaceTexture.updateTexImage();
                surfaceTexture.getTransformMatrix(this.mTmpMatrix);
                if (this.mEOSTracker.updateLastInputAndEncoderTime(surfaceTexture.getTimestamp(), computePresentationTime((this.mInputIndex + this.mNumTiles) - 1))) {
                    copyTilesGL();
                }
                surfaceTexture.releaseTexImage();
                this.mEncoderEglSurface.makeUnCurrent();
            }
        }
    }

    public void start() {
        this.mEncoder.start();
    }

    public void addYuvBuffer(int i, @NonNull byte[] bArr) {
        if (this.mInputMode != 0) {
            throw new IllegalStateException("addYuvBuffer is only allowed in buffer input mode");
        } else if (bArr == null || bArr.length != ((this.mWidth * this.mHeight) * 3) / 2) {
            throw new IllegalArgumentException("invalid data");
        } else {
            addYuvBufferInternal(bArr);
        }
    }

    @NonNull
    public Surface getInputSurface() {
        if (this.mInputMode == 1) {
            return this.mInputSurface;
        }
        throw new IllegalStateException("getInputSurface is only allowed in surface input mode");
    }

    public void setEndOfInputStreamTimestamp(long j) {
        if (this.mInputMode == 1) {
            SurfaceEOSTracker surfaceEOSTracker = this.mEOSTracker;
            if (surfaceEOSTracker != null) {
                surfaceEOSTracker.updateInputEOSTime(j);
                return;
            }
            return;
        }
        throw new IllegalStateException("setEndOfInputStreamTimestamp is only allowed in surface input mode");
    }

    public void addBitmap(@NonNull Bitmap bitmap) {
        if (this.mInputMode != 2) {
            throw new IllegalStateException("addBitmap is only allowed in bitmap input mode");
        } else if (this.mEOSTracker.updateLastInputAndEncoderTime(computePresentationTime(this.mInputIndex) * 1000, computePresentationTime((this.mInputIndex + this.mNumTiles) - 1))) {
            synchronized (this) {
                if (this.mEncoderEglSurface != null) {
                    this.mEncoderEglSurface.makeCurrent();
                    this.mRectBlt.loadTexture(this.mTextureId, bitmap);
                    copyTilesGL();
                    this.mEncoderEglSurface.makeUnCurrent();
                }
            }
        }
    }

    public void stopAsync() {
        int i = this.mInputMode;
        if (i == 2) {
            this.mEOSTracker.updateInputEOSTime(0);
        } else if (i == 0) {
            addYuvBufferInternal(null);
        }
    }

    private long computePresentationTime(int i) {
        return ((((long) i) * 1000000) / ((long) this.mNumTiles)) + 132;
    }

    private void addYuvBufferInternal(@Nullable byte[] bArr) {
        ByteBuffer acquireEmptyBuffer = acquireEmptyBuffer();
        if (acquireEmptyBuffer != null) {
            acquireEmptyBuffer.clear();
            if (bArr != null) {
                acquireEmptyBuffer.put(bArr);
            }
            acquireEmptyBuffer.flip();
            synchronized (this.mFilledBuffers) {
                this.mFilledBuffers.add(acquireEmptyBuffer);
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    HeifEncoder.this.maybeCopyOneTileYUV();
                }
            });
        }
    }

    /* access modifiers changed from: 0000 */
    public void maybeCopyOneTileYUV() {
        int i;
        while (true) {
            ByteBuffer currentBuffer = getCurrentBuffer();
            if (currentBuffer != null && !this.mCodecInputBuffers.isEmpty()) {
                int i2 = 0;
                int intValue = ((Integer) this.mCodecInputBuffers.remove(0)).intValue();
                boolean z = this.mInputIndex % this.mNumTiles == 0 && currentBuffer.remaining() == 0;
                if (!z) {
                    Image inputImage = this.mEncoder.getInputImage(intValue);
                    int i3 = this.mGridWidth;
                    int i4 = this.mInputIndex;
                    int i5 = this.mGridCols;
                    int i6 = (i4 % i5) * i3;
                    int i7 = this.mGridHeight;
                    int i8 = ((i4 / i5) % this.mGridRows) * i7;
                    this.mSrcRect.set(i6, i8, i3 + i6, i7 + i8);
                    copyOneTileYUV(currentBuffer, inputImage, this.mWidth, this.mHeight, this.mSrcRect, this.mDstRect);
                }
                MediaCodec mediaCodec = this.mEncoder;
                if (z) {
                    i = 0;
                } else {
                    i = mediaCodec.getInputBuffer(intValue).capacity();
                }
                int i9 = this.mInputIndex;
                this.mInputIndex = i9 + 1;
                long computePresentationTime = computePresentationTime(i9);
                if (z) {
                    i2 = 4;
                }
                mediaCodec.queueInputBuffer(intValue, 0, i, computePresentationTime, i2);
                if (z || this.mInputIndex % this.mNumTiles == 0) {
                    returnEmptyBufferAndNotify(z);
                }
            } else {
                return;
            }
        }
    }

    private static void copyOneTileYUV(ByteBuffer byteBuffer, Image image, int i, int i2, Rect rect, Rect rect2) {
        int i3;
        int i4;
        Rect rect3 = rect;
        Rect rect4 = rect2;
        if (rect.width() == rect2.width() && rect.height() == rect2.height()) {
            if (i % 2 == 0 && i2 % 2 == 0) {
                int i5 = 2;
                if (rect3.left % 2 == 0 && rect3.top % 2 == 0 && rect3.right % 2 == 0 && rect3.bottom % 2 == 0 && rect4.left % 2 == 0 && rect4.top % 2 == 0 && rect4.right % 2 == 0 && rect4.bottom % 2 == 0) {
                    Plane[] planes = image.getPlanes();
                    int i6 = 0;
                    while (i6 < planes.length) {
                        ByteBuffer buffer = planes[i6].getBuffer();
                        int pixelStride = planes[i6].getPixelStride();
                        int min = Math.min(rect.width(), i - rect3.left);
                        int min2 = Math.min(rect.height(), i2 - rect3.top);
                        if (i6 > 0) {
                            i4 = ((i * i2) * (i6 + 3)) / 4;
                            i3 = i5;
                        } else {
                            i3 = 1;
                            i4 = 0;
                        }
                        for (int i7 = 0; i7 < min2 / i3; i7++) {
                            byteBuffer.position(((((rect3.top / i3) + i7) * i) / i3) + i4 + (rect3.left / i3));
                            buffer.position((((rect4.top / i3) + i7) * planes[i6].getRowStride()) + ((rect4.left * pixelStride) / i3));
                            int i8 = 0;
                            while (true) {
                                int i9 = min / i3;
                                if (i8 >= i9) {
                                    break;
                                }
                                buffer.put(byteBuffer.get());
                                if (pixelStride > 1 && i8 != i9 - 1) {
                                    buffer.position((buffer.position() + pixelStride) - 1);
                                }
                                i8++;
                            }
                        }
                        ByteBuffer byteBuffer2 = byteBuffer;
                        i6++;
                        i5 = 2;
                    }
                    return;
                }
            }
            throw new IllegalArgumentException("src or dst are not aligned!");
        }
        throw new IllegalArgumentException("src and dst rect size are different!");
    }

    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* JADX WARNING: Missing exception handler attribute for start block: B:2:0x0003 */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x0003 A[LOOP:0: B:2:0x0003->B:19:0x0003, LOOP_START, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.nio.ByteBuffer acquireEmptyBuffer() {
        /*
            r2 = this;
            java.util.ArrayList<java.nio.ByteBuffer> r0 = r2.mEmptyBuffers
            monitor-enter(r0)
        L_0x0003:
            boolean r1 = r2.mInputEOS     // Catch:{ all -> 0x0026 }
            if (r1 != 0) goto L_0x0015
            java.util.ArrayList<java.nio.ByteBuffer> r1 = r2.mEmptyBuffers     // Catch:{ all -> 0x0026 }
            boolean r1 = r1.isEmpty()     // Catch:{ all -> 0x0026 }
            if (r1 == 0) goto L_0x0015
            java.util.ArrayList<java.nio.ByteBuffer> r1 = r2.mEmptyBuffers     // Catch:{ InterruptedException -> 0x0003 }
            r1.wait()     // Catch:{ InterruptedException -> 0x0003 }
            goto L_0x0003
        L_0x0015:
            boolean r1 = r2.mInputEOS     // Catch:{ all -> 0x0026 }
            if (r1 == 0) goto L_0x001b
            r2 = 0
            goto L_0x0024
        L_0x001b:
            java.util.ArrayList<java.nio.ByteBuffer> r2 = r2.mEmptyBuffers     // Catch:{ all -> 0x0026 }
            r1 = 0
            java.lang.Object r2 = r2.remove(r1)     // Catch:{ all -> 0x0026 }
            java.nio.ByteBuffer r2 = (java.nio.ByteBuffer) r2     // Catch:{ all -> 0x0026 }
        L_0x0024:
            monitor-exit(r0)     // Catch:{ all -> 0x0026 }
            return r2
        L_0x0026:
            r2 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0026 }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.heifwriter.HeifEncoder.acquireEmptyBuffer():java.nio.ByteBuffer");
    }

    private ByteBuffer getCurrentBuffer() {
        ByteBuffer byteBuffer;
        if (!this.mInputEOS && this.mCurrentBuffer == null) {
            synchronized (this.mFilledBuffers) {
                if (this.mFilledBuffers.isEmpty()) {
                    byteBuffer = null;
                } else {
                    byteBuffer = (ByteBuffer) this.mFilledBuffers.remove(0);
                }
                this.mCurrentBuffer = byteBuffer;
            }
        }
        if (this.mInputEOS) {
            return null;
        }
        return this.mCurrentBuffer;
    }

    private void returnEmptyBufferAndNotify(boolean z) {
        synchronized (this.mEmptyBuffers) {
            this.mInputEOS = z | this.mInputEOS;
            this.mEmptyBuffers.add(this.mCurrentBuffer);
            this.mEmptyBuffers.notifyAll();
        }
        this.mCurrentBuffer = null;
    }

    /* access modifiers changed from: 0000 */
    public void stopInternal() {
        this.mStopping.set(true);
        MediaCodec mediaCodec = this.mEncoder;
        if (mediaCodec != null) {
            mediaCodec.stop();
            this.mEncoder.release();
            this.mEncoder = null;
        }
        synchronized (this.mEmptyBuffers) {
            this.mInputEOS = true;
            this.mEmptyBuffers.notifyAll();
        }
        synchronized (this) {
            if (this.mRectBlt != null) {
                this.mRectBlt.release(false);
                this.mRectBlt = null;
            }
            if (this.mEncoderEglSurface != null) {
                this.mEncoderEglSurface.release();
                this.mEncoderEglSurface = null;
            }
            if (this.mInputTexture != null) {
                this.mInputTexture.release();
                this.mInputTexture = null;
            }
        }
    }

    public void close() {
        synchronized (this.mEmptyBuffers) {
            this.mInputEOS = true;
            this.mEmptyBuffers.notifyAll();
        }
        this.mHandler.postAtFrontOfQueue(new Runnable() {
            public void run() {
                HeifEncoder.this.stopInternal();
            }
        });
    }
}
