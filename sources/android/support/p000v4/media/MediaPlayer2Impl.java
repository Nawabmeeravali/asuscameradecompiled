package android.support.p000v4.media;

import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.DeniedByServerException;
import android.media.MediaDataSource;
import android.media.MediaDrm.KeyRequest;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.NoDrmSchemeException;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnDrmConfigHelper;
import android.media.MediaPlayer.OnDrmInfoListener;
import android.media.MediaPlayer.OnDrmPreparedListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnMediaTimeDiscontinuityListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnSubtitleDataListener;
import android.media.MediaPlayer.OnTimedMetaDataAvailableListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.MediaPlayer.ProvisioningNetworkErrorException;
import android.media.MediaPlayer.ProvisioningServerErrorException;
import android.media.MediaPlayer.TrackInfo;
import android.media.MediaTimestamp;
import android.media.PlaybackParams;
import android.media.ResourceBusyException;
import android.media.SubtitleData;
import android.media.SyncParams;
import android.media.TimedMetaData;
import android.media.UnsupportedSchemeException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.p000v4.media.BaseMediaPlayer.PlayerEventCallback;
import android.support.p000v4.media.MediaPlayer2.DrmEventCallback;
import android.support.p000v4.media.MediaPlayer2.DrmInfo;
import android.support.p000v4.media.MediaPlayer2.EventCallback;
import android.support.p000v4.media.PlaybackParams2.Builder;
import android.support.p000v4.util.ArrayMap;
import android.support.p000v4.util.Preconditions;
import android.support.p000v4.util.SimpleArrayMap;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

@TargetApi(28)
@RestrictTo({Scope.LIBRARY_GROUP})
/* renamed from: android.support.v4.media.MediaPlayer2Impl */
public final class MediaPlayer2Impl extends MediaPlayer2 {
    private static final int SOURCE_STATE_ERROR = -1;
    private static final int SOURCE_STATE_INIT = 0;
    private static final int SOURCE_STATE_PREPARED = 2;
    private static final int SOURCE_STATE_PREPARING = 1;
    private static final String TAG = "MediaPlayer2Impl";
    /* access modifiers changed from: private */
    public static ArrayMap<Integer, Integer> sErrorEventMap = new ArrayMap<>();
    /* access modifiers changed from: private */
    public static ArrayMap<Integer, Integer> sInfoEventMap = new ArrayMap<>();
    /* access modifiers changed from: private */
    public static ArrayMap<Integer, Integer> sPrepareDrmStatusMap = new ArrayMap<>();
    /* access modifiers changed from: private */
    public static ArrayMap<Integer, Integer> sStateMap = new ArrayMap<>();
    /* access modifiers changed from: private */
    public BaseMediaPlayerImpl mBaseMediaPlayerImpl;
    /* access modifiers changed from: private */
    @GuardedBy("mTaskLock")
    public Task mCurrentTask;
    private Pair<Executor, DrmEventCallback> mDrmEventCallbackRecord;
    private final Handler mEndPositionHandler;
    private HandlerThread mHandlerThread = new HandlerThread("MediaPlayer2TaskThread");
    private final Object mLock = new Object();
    private Pair<Executor, EventCallback> mMp2EventCallbackRecord;
    @GuardedBy("mTaskLock")
    private final ArrayDeque<Task> mPendingTasks = new ArrayDeque<>();
    /* access modifiers changed from: private */
    public MediaPlayerSourceQueue mPlayer;
    private ArrayMap<PlayerEventCallback, Executor> mPlayerEventCallbackMap = new ArrayMap<>();
    private final Handler mTaskHandler;
    /* access modifiers changed from: private */
    public final Object mTaskLock = new Object();

    /* renamed from: android.support.v4.media.MediaPlayer2Impl$BaseMediaPlayerImpl */
    private class BaseMediaPlayerImpl extends BaseMediaPlayer {
        private BaseMediaPlayerImpl() {
        }

        public void play() {
            MediaPlayer2Impl.this.play();
        }

        public void prepare() {
            MediaPlayer2Impl.this.prepare();
        }

        public void pause() {
            MediaPlayer2Impl.this.pause();
        }

        public void reset() {
            MediaPlayer2Impl.this.reset();
        }

        public void skipToNext() {
            MediaPlayer2Impl.this.skipToNext();
        }

        public void seekTo(long j) {
            MediaPlayer2Impl.this.seekTo(j);
        }

        public long getCurrentPosition() {
            return MediaPlayer2Impl.this.getCurrentPosition();
        }

        public long getDuration() {
            return MediaPlayer2Impl.this.getDuration();
        }

        public long getBufferedPosition() {
            return MediaPlayer2Impl.this.getBufferedPosition();
        }

        public int getPlayerState() {
            return MediaPlayer2Impl.this.getPlayerState();
        }

        public int getBufferingState() {
            return MediaPlayer2Impl.this.getBufferingState();
        }

        public void setAudioAttributes(AudioAttributesCompat audioAttributesCompat) {
            MediaPlayer2Impl.this.setAudioAttributes(audioAttributesCompat);
        }

        public AudioAttributesCompat getAudioAttributes() {
            return MediaPlayer2Impl.this.getAudioAttributes();
        }

        public void setDataSource(DataSourceDesc dataSourceDesc) {
            MediaPlayer2Impl.this.setDataSource(dataSourceDesc);
        }

        public void setNextDataSource(DataSourceDesc dataSourceDesc) {
            MediaPlayer2Impl.this.setNextDataSource(dataSourceDesc);
        }

        public void setNextDataSources(List<DataSourceDesc> list) {
            MediaPlayer2Impl.this.setNextDataSources(list);
        }

        public DataSourceDesc getCurrentDataSource() {
            return MediaPlayer2Impl.this.getCurrentDataSource();
        }

        public void loopCurrent(boolean z) {
            MediaPlayer2Impl.this.loopCurrent(z);
        }

        public void setPlaybackSpeed(float f) {
            MediaPlayer2Impl mediaPlayer2Impl = MediaPlayer2Impl.this;
            mediaPlayer2Impl.setPlaybackParams(new Builder(mediaPlayer2Impl.getPlaybackParams().getPlaybackParams()).setSpeed(f).build());
        }

        public float getPlaybackSpeed() {
            return MediaPlayer2Impl.this.getPlaybackParams().getSpeed().floatValue();
        }

        public void setPlayerVolume(float f) {
            MediaPlayer2Impl.this.setPlayerVolume(f);
        }

        public float getPlayerVolume() {
            return MediaPlayer2Impl.this.getPlayerVolume();
        }

        public void registerPlayerEventCallback(Executor executor, PlayerEventCallback playerEventCallback) {
            MediaPlayer2Impl.this.registerPlayerEventCallback(executor, playerEventCallback);
        }

        public void unregisterPlayerEventCallback(PlayerEventCallback playerEventCallback) {
            MediaPlayer2Impl.this.unregisterPlayerEventCallback(playerEventCallback);
        }

        public void close() throws Exception {
            MediaPlayer2Impl.this.close();
        }
    }

    /* renamed from: android.support.v4.media.MediaPlayer2Impl$DataSourceError */
    private static class DataSourceError {
        final DataSourceDesc mDSD;
        final int mExtra;
        final int mWhat;

        DataSourceError(DataSourceDesc dataSourceDesc, int i, int i2) {
            this.mDSD = dataSourceDesc;
            this.mWhat = i;
            this.mExtra = i2;
        }
    }

    /* renamed from: android.support.v4.media.MediaPlayer2Impl$DrmEventNotifier */
    private interface DrmEventNotifier {
        void notify(DrmEventCallback drmEventCallback);
    }

    /* renamed from: android.support.v4.media.MediaPlayer2Impl$DrmInfoImpl */
    public static final class DrmInfoImpl extends DrmInfo {
        private Map<UUID, byte[]> mMapPssh;
        private UUID[] mSupportedSchemes;

        public Map<UUID, byte[]> getPssh() {
            return this.mMapPssh;
        }

        public List<UUID> getSupportedSchemes() {
            return Arrays.asList(this.mSupportedSchemes);
        }

        private DrmInfoImpl(Map<UUID, byte[]> map, UUID[] uuidArr) {
            this.mMapPssh = map;
            this.mSupportedSchemes = uuidArr;
        }

        private DrmInfoImpl(Parcel parcel) {
            StringBuilder sb = new StringBuilder();
            sb.append("DrmInfoImpl(");
            sb.append(parcel);
            sb.append(") size ");
            sb.append(parcel.dataSize());
            String sb2 = sb.toString();
            String str = MediaPlayer2Impl.TAG;
            Log.v(str, sb2);
            int readInt = parcel.readInt();
            byte[] bArr = new byte[readInt];
            parcel.readByteArray(bArr);
            StringBuilder sb3 = new StringBuilder();
            String str2 = "DrmInfoImpl() PSSH: ";
            sb3.append(str2);
            sb3.append(arrToHex(bArr));
            Log.v(str, sb3.toString());
            this.mMapPssh = parsePSSH(bArr, readInt);
            StringBuilder sb4 = new StringBuilder();
            sb4.append(str2);
            sb4.append(this.mMapPssh);
            Log.v(str, sb4.toString());
            int readInt2 = parcel.readInt();
            this.mSupportedSchemes = new UUID[readInt2];
            for (int i = 0; i < readInt2; i++) {
                byte[] bArr2 = new byte[16];
                parcel.readByteArray(bArr2);
                this.mSupportedSchemes[i] = bytesToUUID(bArr2);
                StringBuilder sb5 = new StringBuilder();
                sb5.append("DrmInfoImpl() supportedScheme[");
                sb5.append(i);
                sb5.append("]: ");
                sb5.append(this.mSupportedSchemes[i]);
                Log.v(str, sb5.toString());
            }
            StringBuilder sb6 = new StringBuilder();
            sb6.append("DrmInfoImpl() Parcel psshsize: ");
            sb6.append(readInt);
            sb6.append(" supportedDRMsCount: ");
            sb6.append(readInt2);
            Log.v(str, sb6.toString());
        }

        private DrmInfoImpl makeCopy() {
            return new DrmInfoImpl(this.mMapPssh, this.mSupportedSchemes);
        }

        private String arrToHex(byte[] bArr) {
            String str = "0x";
            for (byte valueOf : bArr) {
                StringBuilder sb = new StringBuilder();
                sb.append(str);
                sb.append(String.format("%02x", new Object[]{Byte.valueOf(valueOf)}));
                str = sb.toString();
            }
            return str;
        }

        private UUID bytesToUUID(byte[] bArr) {
            long j = 0;
            long j2 = 0;
            for (int i = 0; i < 8; i++) {
                int i2 = (7 - i) * 8;
                j |= (((long) bArr[i]) & 255) << i2;
                j2 |= (((long) bArr[i + 8]) & 255) << i2;
            }
            return new UUID(j, j2);
        }

        private Map<UUID, byte[]> parsePSSH(byte[] bArr, int i) {
            byte b;
            byte b2;
            byte[] bArr2 = bArr;
            HashMap hashMap = new HashMap();
            int i2 = i;
            int i3 = 0;
            int i4 = 0;
            while (i2 > 0) {
                String str = MediaPlayer2Impl.TAG;
                if (i2 < 16) {
                    Log.w(str, String.format("parsePSSH: len is too short to parse UUID: (%d < 16) pssh: %d", new Object[]{Integer.valueOf(i2), Integer.valueOf(i)}));
                    return null;
                }
                int i5 = i3 + 16;
                UUID bytesToUUID = bytesToUUID(Arrays.copyOfRange(bArr2, i3, i5));
                int i6 = i2 - 16;
                if (i6 < 4) {
                    Log.w(str, String.format("parsePSSH: len is too short to parse datalen: (%d < 4) pssh: %d", new Object[]{Integer.valueOf(i6), Integer.valueOf(i)}));
                    return null;
                }
                int i7 = i5 + 4;
                byte[] copyOfRange = Arrays.copyOfRange(bArr2, i5, i7);
                if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                    b2 = ((copyOfRange[2] & 255) << 16) | ((copyOfRange[3] & 255) << 24) | ((copyOfRange[1] & 255) << 8);
                    b = copyOfRange[0];
                } else {
                    b2 = ((copyOfRange[1] & 255) << 16) | ((copyOfRange[0] & 255) << 24) | ((copyOfRange[2] & 255) << 8);
                    b = copyOfRange[3];
                }
                byte b3 = b2 | (b & 255);
                int i8 = i6 - 4;
                if (i8 < b3) {
                    Log.w(str, String.format("parsePSSH: len is too short to parse data: (%d < %d) pssh: %d", new Object[]{Integer.valueOf(i8), Integer.valueOf(b3), Integer.valueOf(i)}));
                    return null;
                }
                int i9 = i7 + b3;
                byte[] copyOfRange2 = Arrays.copyOfRange(bArr2, i7, i9);
                i2 = i8 - b3;
                Log.v(str, String.format("parsePSSH[%d]: <%s, %s> pssh: %d", new Object[]{Integer.valueOf(i4), bytesToUUID, arrToHex(copyOfRange2), Integer.valueOf(i)}));
                i4++;
                hashMap.put(bytesToUUID, copyOfRange2);
                i3 = i9;
            }
            return hashMap;
        }
    }

    /* renamed from: android.support.v4.media.MediaPlayer2Impl$MediaPlayerSource */
    private class MediaPlayerSource {
        final AtomicInteger mBufferedPercentage = new AtomicInteger(0);
        int mBufferingState = 0;
        volatile DataSourceDesc mDSD;
        int mMp2State = 1001;
        boolean mPlayPending;
        final MediaPlayer mPlayer = new MediaPlayer();
        int mPlayerState = 0;
        int mSourceState = 0;

        MediaPlayerSource(DataSourceDesc dataSourceDesc) {
            this.mDSD = dataSourceDesc;
            MediaPlayer2Impl.this.setUpListeners(this);
        }

        /* access modifiers changed from: 0000 */
        public DataSourceDesc getDSD() {
            return this.mDSD;
        }
    }

    /* renamed from: android.support.v4.media.MediaPlayer2Impl$MediaPlayerSourceQueue */
    private class MediaPlayerSourceQueue {
        AudioAttributesCompat mAudioAttributes;
        Integer mAudioSessionId;
        Integer mAuxEffect;
        Float mAuxEffectSendLevel;
        PlaybackParams mPlaybackParams;
        List<MediaPlayerSource> mQueue = new ArrayList();
        Surface mSurface;
        SyncParams mSyncParams;
        Float mVolume = Float.valueOf(1.0f);

        MediaPlayerSourceQueue() {
            this.mQueue.add(new MediaPlayerSource(null));
        }

        /* access modifiers changed from: 0000 */
        public synchronized MediaPlayer getCurrentPlayer() {
            return ((MediaPlayerSource) this.mQueue.get(0)).mPlayer;
        }

        /* access modifiers changed from: 0000 */
        public synchronized MediaPlayerSource getFirst() {
            return (MediaPlayerSource) this.mQueue.get(0);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void setFirst(DataSourceDesc dataSourceDesc) throws IOException {
            if (this.mQueue.isEmpty()) {
                this.mQueue.add(0, new MediaPlayerSource(dataSourceDesc));
            } else {
                ((MediaPlayerSource) this.mQueue.get(0)).mDSD = dataSourceDesc;
                MediaPlayer2Impl.this.setUpListeners((MediaPlayerSource) this.mQueue.get(0));
            }
            MediaPlayer2Impl.handleDataSource((MediaPlayerSource) this.mQueue.get(0));
        }

        /* access modifiers changed from: 0000 */
        public synchronized DataSourceError setNext(DataSourceDesc dataSourceDesc) {
            MediaPlayerSource mediaPlayerSource = new MediaPlayerSource(dataSourceDesc);
            if (this.mQueue.isEmpty()) {
                this.mQueue.add(mediaPlayerSource);
                return prepareAt(0);
            }
            this.mQueue.add(1, mediaPlayerSource);
            return prepareAt(1);
        }

        /* access modifiers changed from: 0000 */
        public synchronized DataSourceError setNextMultiple(List<DataSourceDesc> list) {
            ArrayList arrayList = new ArrayList();
            for (DataSourceDesc mediaPlayerSource : list) {
                arrayList.add(new MediaPlayerSource(mediaPlayerSource));
            }
            if (this.mQueue.isEmpty()) {
                this.mQueue.addAll(arrayList);
                return prepareAt(0);
            }
            this.mQueue.addAll(1, arrayList);
            return prepareAt(1);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void play() {
            MediaPlayerSource mediaPlayerSource = (MediaPlayerSource) this.mQueue.get(0);
            if (mediaPlayerSource.mSourceState == 2) {
                mediaPlayerSource.mPlayer.start();
                setMp2State(mediaPlayerSource.mPlayer, 1004);
            } else {
                throw new IllegalStateException();
            }
        }

        /* access modifiers changed from: 0000 */
        public synchronized void release() {
            getCurrentPlayer().release();
        }

        /* access modifiers changed from: 0000 */
        public synchronized void prepareAsync() {
            MediaPlayer currentPlayer = getCurrentPlayer();
            currentPlayer.prepareAsync();
            setBufferingState(currentPlayer, 2);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void pause() {
            MediaPlayer currentPlayer = getCurrentPlayer();
            currentPlayer.pause();
            setMp2State(currentPlayer, 1003);
        }

        /* access modifiers changed from: 0000 */
        public synchronized long getCurrentPosition() {
            return (long) getCurrentPlayer().getCurrentPosition();
        }

        /* access modifiers changed from: 0000 */
        public synchronized long getDuration() {
            return (long) getCurrentPlayer().getDuration();
        }

        /* access modifiers changed from: 0000 */
        public synchronized long getBufferedPosition() {
            MediaPlayerSource mediaPlayerSource;
            mediaPlayerSource = (MediaPlayerSource) this.mQueue.get(0);
            return (((long) mediaPlayerSource.mPlayer.getDuration()) * ((long) mediaPlayerSource.mBufferedPercentage.get())) / 100;
        }

        /* access modifiers changed from: 0000 */
        public synchronized void setAudioAttributes(AudioAttributesCompat audioAttributesCompat) {
            AudioAttributes audioAttributes;
            this.mAudioAttributes = audioAttributesCompat;
            if (this.mAudioAttributes == null) {
                audioAttributes = null;
            } else {
                audioAttributes = (AudioAttributes) this.mAudioAttributes.unwrap();
            }
            getCurrentPlayer().setAudioAttributes(audioAttributes);
        }

        /* access modifiers changed from: 0000 */
        public synchronized AudioAttributesCompat getAudioAttributes() {
            return this.mAudioAttributes;
        }

        /* access modifiers changed from: 0000 */
        public synchronized DataSourceError onPrepared(MediaPlayer mediaPlayer) {
            for (int i = 0; i < this.mQueue.size(); i++) {
                MediaPlayerSource mediaPlayerSource = (MediaPlayerSource) this.mQueue.get(i);
                if (mediaPlayer == mediaPlayerSource.mPlayer) {
                    if (i == 0) {
                        if (mediaPlayerSource.mPlayPending) {
                            mediaPlayerSource.mPlayPending = false;
                            mediaPlayerSource.mPlayer.start();
                            setMp2State(mediaPlayerSource.mPlayer, 1004);
                        } else {
                            setMp2State(mediaPlayerSource.mPlayer, 1002);
                        }
                    }
                    mediaPlayerSource.mSourceState = 2;
                    setBufferingState(mediaPlayerSource.mPlayer, 1);
                    return prepareAt(i + 1);
                }
            }
            return null;
        }

        /* access modifiers changed from: 0000 */
        public synchronized DataSourceError onCompletion(MediaPlayer mediaPlayer) {
            if (!this.mQueue.isEmpty() && mediaPlayer == getCurrentPlayer()) {
                if (this.mQueue.size() == 1) {
                    setMp2State(mediaPlayer, 1003);
                    final DataSourceDesc dsd = ((MediaPlayerSource) this.mQueue.get(0)).getDSD();
                    MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                        public void notify(EventCallback eventCallback) {
                            eventCallback.onInfo(MediaPlayer2Impl.this, dsd, 6, 0);
                        }
                    });
                    return null;
                }
                moveToNext();
            }
            return playCurrent();
        }

        /* access modifiers changed from: 0000 */
        public synchronized void moveToNext() {
            MediaPlayerSource mediaPlayerSource = (MediaPlayerSource) this.mQueue.remove(0);
            mediaPlayerSource.mPlayer.release();
            if (!this.mQueue.isEmpty()) {
                final MediaPlayerSource mediaPlayerSource2 = (MediaPlayerSource) this.mQueue.get(0);
                if (mediaPlayerSource.mPlayerState != mediaPlayerSource2.mPlayerState) {
                    MediaPlayer2Impl.this.notifyPlayerEvent(new PlayerEventNotifier() {
                        public void notify(PlayerEventCallback playerEventCallback) {
                            playerEventCallback.onPlayerStateChanged(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, mediaPlayerSource2.mPlayerState);
                        }
                    });
                }
                MediaPlayer2Impl.this.notifyPlayerEvent(new PlayerEventNotifier() {
                    public void notify(PlayerEventCallback playerEventCallback) {
                        playerEventCallback.onCurrentDataSourceChanged(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, mediaPlayerSource2.mDSD);
                    }
                });
            } else {
                throw new IllegalStateException("player/source queue emptied");
            }
        }

        /* access modifiers changed from: 0000 */
        public synchronized DataSourceError playCurrent() {
            DataSourceError dataSourceError;
            dataSourceError = null;
            final MediaPlayerSource mediaPlayerSource = (MediaPlayerSource) this.mQueue.get(0);
            if (this.mSurface != null) {
                mediaPlayerSource.mPlayer.setSurface(this.mSurface);
            }
            if (this.mVolume != null) {
                mediaPlayerSource.mPlayer.setVolume(this.mVolume.floatValue(), this.mVolume.floatValue());
            }
            if (this.mAudioAttributes != null) {
                mediaPlayerSource.mPlayer.setAudioAttributes((AudioAttributes) this.mAudioAttributes.unwrap());
            }
            if (this.mAuxEffect != null) {
                mediaPlayerSource.mPlayer.attachAuxEffect(this.mAuxEffect.intValue());
            }
            if (this.mAuxEffectSendLevel != null) {
                mediaPlayerSource.mPlayer.setAuxEffectSendLevel(this.mAuxEffectSendLevel.floatValue());
            }
            if (this.mSyncParams != null) {
                mediaPlayerSource.mPlayer.setSyncParams(this.mSyncParams);
            }
            if (this.mPlaybackParams != null) {
                mediaPlayerSource.mPlayer.setPlaybackParams(this.mPlaybackParams);
            }
            if (mediaPlayerSource.mSourceState == 2) {
                mediaPlayerSource.mPlayer.start();
                setMp2State(mediaPlayerSource.mPlayer, 1004);
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(EventCallback eventCallback) {
                        eventCallback.onInfo(MediaPlayer2Impl.this, mediaPlayerSource.getDSD(), 2, 0);
                    }
                });
            } else {
                if (mediaPlayerSource.mSourceState == 0) {
                    dataSourceError = prepareAt(0);
                }
                mediaPlayerSource.mPlayPending = true;
            }
            return dataSourceError;
        }

        /* access modifiers changed from: 0000 */
        public synchronized void onError(MediaPlayer mediaPlayer) {
            setMp2State(mediaPlayer, MediaPlayer2.MEDIAPLAYER2_STATE_ERROR);
            setBufferingState(mediaPlayer, 0);
        }

        /* access modifiers changed from: 0000 */
        /* JADX WARNING: Can't wrap try/catch for region: R(4:19|20|21|22) */
        /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
            r1 = r5.getDSD();
            setMp2State(r5.mPlayer, android.support.p000v4.media.MediaPlayer2.MEDIAPLAYER2_STATE_ERROR);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0056, code lost:
            return new android.support.p000v4.media.MediaPlayer2Impl.DataSourceError(r1, 1, android.support.p000v4.media.MediaPlayer2.MEDIA_ERROR_UNSUPPORTED);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0058, code lost:
            return null;
         */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x0043 */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized android.support.p000v4.media.MediaPlayer2Impl.DataSourceError prepareAt(int r5) {
            /*
                r4 = this;
                monitor-enter(r4)
                java.util.List<android.support.v4.media.MediaPlayer2Impl$MediaPlayerSource> r0 = r4.mQueue     // Catch:{ all -> 0x0059 }
                int r0 = r0.size()     // Catch:{ all -> 0x0059 }
                r1 = 0
                if (r5 >= r0) goto L_0x0057
                java.util.List<android.support.v4.media.MediaPlayer2Impl$MediaPlayerSource> r0 = r4.mQueue     // Catch:{ all -> 0x0059 }
                java.lang.Object r0 = r0.get(r5)     // Catch:{ all -> 0x0059 }
                android.support.v4.media.MediaPlayer2Impl$MediaPlayerSource r0 = (android.support.p000v4.media.MediaPlayer2Impl.MediaPlayerSource) r0     // Catch:{ all -> 0x0059 }
                int r0 = r0.mSourceState     // Catch:{ all -> 0x0059 }
                if (r0 != 0) goto L_0x0057
                if (r5 == 0) goto L_0x001f
                int r0 = r4.getPlayerState()     // Catch:{ all -> 0x0059 }
                if (r0 != 0) goto L_0x001f
                goto L_0x0057
            L_0x001f:
                java.util.List<android.support.v4.media.MediaPlayer2Impl$MediaPlayerSource> r0 = r4.mQueue     // Catch:{ all -> 0x0059 }
                java.lang.Object r5 = r0.get(r5)     // Catch:{ all -> 0x0059 }
                android.support.v4.media.MediaPlayer2Impl$MediaPlayerSource r5 = (android.support.p000v4.media.MediaPlayer2Impl.MediaPlayerSource) r5     // Catch:{ all -> 0x0059 }
                r0 = 1
                java.lang.Integer r2 = r4.mAudioSessionId     // Catch:{ Exception -> 0x0043 }
                if (r2 == 0) goto L_0x0037
                android.media.MediaPlayer r2 = r5.mPlayer     // Catch:{ Exception -> 0x0043 }
                java.lang.Integer r3 = r4.mAudioSessionId     // Catch:{ Exception -> 0x0043 }
                int r3 = r3.intValue()     // Catch:{ Exception -> 0x0043 }
                r2.setAudioSessionId(r3)     // Catch:{ Exception -> 0x0043 }
            L_0x0037:
                r5.mSourceState = r0     // Catch:{ Exception -> 0x0043 }
                android.support.p000v4.media.MediaPlayer2Impl.handleDataSource(r5)     // Catch:{ Exception -> 0x0043 }
                android.media.MediaPlayer r2 = r5.mPlayer     // Catch:{ Exception -> 0x0043 }
                r2.prepareAsync()     // Catch:{ Exception -> 0x0043 }
                monitor-exit(r4)
                return r1
            L_0x0043:
                android.support.v4.media.DataSourceDesc r1 = r5.getDSD()     // Catch:{ all -> 0x0059 }
                android.media.MediaPlayer r5 = r5.mPlayer     // Catch:{ all -> 0x0059 }
                r2 = 1005(0x3ed, float:1.408E-42)
                r4.setMp2State(r5, r2)     // Catch:{ all -> 0x0059 }
                android.support.v4.media.MediaPlayer2Impl$DataSourceError r5 = new android.support.v4.media.MediaPlayer2Impl$DataSourceError     // Catch:{ all -> 0x0059 }
                r2 = -1010(0xfffffffffffffc0e, float:NaN)
                r5.<init>(r1, r0, r2)     // Catch:{ all -> 0x0059 }
                monitor-exit(r4)
                return r5
            L_0x0057:
                monitor-exit(r4)
                return r1
            L_0x0059:
                r5 = move-exception
                monitor-exit(r4)
                throw r5
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.MediaPlayer2Impl.MediaPlayerSourceQueue.prepareAt(int):android.support.v4.media.MediaPlayer2Impl$DataSourceError");
        }

        /* access modifiers changed from: 0000 */
        public synchronized void skipToNext() {
            if (this.mQueue.size() > 1) {
                MediaPlayerSource mediaPlayerSource = (MediaPlayerSource) this.mQueue.get(0);
                moveToNext();
                if (mediaPlayerSource.mPlayerState == 2 || mediaPlayerSource.mPlayPending) {
                    playCurrent();
                }
            } else {
                throw new IllegalStateException("No next source available");
            }
        }

        /* access modifiers changed from: 0000 */
        public synchronized void setLooping(boolean z) {
            getCurrentPlayer().setLooping(z);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void setPlaybackParams(PlaybackParams playbackParams) {
            getCurrentPlayer().setPlaybackParams(playbackParams);
            this.mPlaybackParams = playbackParams;
        }

        /* access modifiers changed from: 0000 */
        public synchronized float getVolume() {
            return this.mVolume.floatValue();
        }

        /* access modifiers changed from: 0000 */
        public synchronized void setVolume(float f) {
            this.mVolume = Float.valueOf(f);
            getCurrentPlayer().setVolume(f, f);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void setSurface(Surface surface) {
            this.mSurface = surface;
            getCurrentPlayer().setSurface(surface);
        }

        /* access modifiers changed from: 0000 */
        public synchronized int getVideoWidth() {
            return getCurrentPlayer().getVideoWidth();
        }

        /* access modifiers changed from: 0000 */
        public synchronized int getVideoHeight() {
            return getCurrentPlayer().getVideoHeight();
        }

        /* access modifiers changed from: 0000 */
        public synchronized PersistableBundle getMetrics() {
            return getCurrentPlayer().getMetrics();
        }

        /* access modifiers changed from: 0000 */
        public synchronized PlaybackParams getPlaybackParams() {
            return getCurrentPlayer().getPlaybackParams();
        }

        /* access modifiers changed from: 0000 */
        public synchronized void seekTo(long j, int i) {
            getCurrentPlayer().seekTo(j, i);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void reset() {
            MediaPlayerSource mediaPlayerSource = (MediaPlayerSource) this.mQueue.get(0);
            mediaPlayerSource.mPlayer.reset();
            mediaPlayerSource.mBufferedPercentage.set(0);
            this.mVolume = Float.valueOf(1.0f);
            this.mSurface = null;
            this.mAuxEffect = null;
            this.mAuxEffectSendLevel = null;
            this.mAudioAttributes = null;
            this.mAudioSessionId = null;
            this.mSyncParams = null;
            this.mPlaybackParams = null;
            setMp2State(mediaPlayerSource.mPlayer, 1001);
            setBufferingState(mediaPlayerSource.mPlayer, 0);
        }

        /* access modifiers changed from: 0000 */
        public synchronized MediaTimestamp2 getTimestamp() {
            MediaTimestamp2 mediaTimestamp2;
            MediaTimestamp timestamp = getCurrentPlayer().getTimestamp();
            if (timestamp == null) {
                mediaTimestamp2 = null;
            } else {
                mediaTimestamp2 = new MediaTimestamp2(timestamp);
            }
            return mediaTimestamp2;
        }

        /* access modifiers changed from: 0000 */
        public synchronized void setAudioSessionId(int i) {
            getCurrentPlayer().setAudioSessionId(i);
        }

        /* access modifiers changed from: 0000 */
        public synchronized int getAudioSessionId() {
            return getCurrentPlayer().getAudioSessionId();
        }

        /* access modifiers changed from: 0000 */
        public synchronized void attachAuxEffect(int i) {
            getCurrentPlayer().attachAuxEffect(i);
            this.mAuxEffect = Integer.valueOf(i);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void setAuxEffectSendLevel(float f) {
            getCurrentPlayer().setAuxEffectSendLevel(f);
            this.mAuxEffectSendLevel = Float.valueOf(f);
        }

        /* access modifiers changed from: 0000 */
        public synchronized TrackInfo[] getTrackInfo() {
            return getCurrentPlayer().getTrackInfo();
        }

        /* access modifiers changed from: 0000 */
        public synchronized int getSelectedTrack(int i) {
            return getCurrentPlayer().getSelectedTrack(i);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void selectTrack(int i) {
            getCurrentPlayer().selectTrack(i);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void deselectTrack(int i) {
            getCurrentPlayer().deselectTrack(i);
        }

        /* access modifiers changed from: 0000 */
        public synchronized MediaPlayer.DrmInfo getDrmInfo() {
            return getCurrentPlayer().getDrmInfo();
        }

        /* access modifiers changed from: 0000 */
        public synchronized void prepareDrm(UUID uuid) throws ResourceBusyException, ProvisioningServerErrorException, ProvisioningNetworkErrorException, UnsupportedSchemeException {
            getCurrentPlayer().prepareDrm(uuid);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void releaseDrm() throws NoDrmSchemeException {
            getCurrentPlayer().stop();
            getCurrentPlayer().releaseDrm();
        }

        /* access modifiers changed from: 0000 */
        public synchronized byte[] provideKeyResponse(byte[] bArr, byte[] bArr2) throws DeniedByServerException, NoDrmSchemeException {
            return getCurrentPlayer().provideKeyResponse(bArr, bArr2);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void restoreKeys(byte[] bArr) throws NoDrmSchemeException {
            getCurrentPlayer().restoreKeys(bArr);
        }

        /* access modifiers changed from: 0000 */
        public synchronized String getDrmPropertyString(String str) throws NoDrmSchemeException {
            return getCurrentPlayer().getDrmPropertyString(str);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void setDrmPropertyString(String str, String str2) throws NoDrmSchemeException {
            getCurrentPlayer().setDrmPropertyString(str, str2);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void setOnDrmConfigHelper(OnDrmConfigHelper onDrmConfigHelper) {
            getCurrentPlayer().setOnDrmConfigHelper(onDrmConfigHelper);
        }

        /* access modifiers changed from: 0000 */
        public synchronized KeyRequest getKeyRequest(byte[] bArr, byte[] bArr2, String str, int i, Map<String, String> map) throws NoDrmSchemeException {
            return getCurrentPlayer().getKeyRequest(bArr, bArr2, str, i, map);
        }

        /* access modifiers changed from: 0000 */
        public synchronized void setMp2State(MediaPlayer mediaPlayer, int i) {
            for (MediaPlayerSource mediaPlayerSource : this.mQueue) {
                if (mediaPlayerSource.mPlayer == mediaPlayer) {
                    if (mediaPlayerSource.mMp2State != i) {
                        mediaPlayerSource.mMp2State = i;
                        final int intValue = ((Integer) MediaPlayer2Impl.sStateMap.get(Integer.valueOf(i))).intValue();
                        if (mediaPlayerSource.mPlayerState != intValue) {
                            mediaPlayerSource.mPlayerState = intValue;
                            MediaPlayer2Impl.this.notifyPlayerEvent(new PlayerEventNotifier() {
                                public void notify(PlayerEventCallback playerEventCallback) {
                                    playerEventCallback.onPlayerStateChanged(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, intValue);
                                }
                            });
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public synchronized void setBufferingState(MediaPlayer mediaPlayer, final int i) {
            for (final MediaPlayerSource mediaPlayerSource : this.mQueue) {
                if (mediaPlayerSource.mPlayer == mediaPlayer) {
                    if (mediaPlayerSource.mBufferingState != i) {
                        mediaPlayerSource.mBufferingState = i;
                        MediaPlayer2Impl.this.notifyPlayerEvent(new PlayerEventNotifier() {
                            public void notify(PlayerEventCallback playerEventCallback) {
                                playerEventCallback.onBufferingStateChanged(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, mediaPlayerSource.getDSD(), i);
                            }
                        });
                        return;
                    }
                    return;
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public synchronized int getMediaPlayer2State() {
            return ((MediaPlayerSource) this.mQueue.get(0)).mMp2State;
        }

        /* access modifiers changed from: 0000 */
        public synchronized int getBufferingState() {
            return ((MediaPlayerSource) this.mQueue.get(0)).mBufferingState;
        }

        /* access modifiers changed from: 0000 */
        public synchronized int getPlayerState() {
            return ((MediaPlayerSource) this.mQueue.get(0)).mPlayerState;
        }

        /* access modifiers changed from: 0000 */
        public synchronized MediaPlayerSource getSourceForPlayer(MediaPlayer mediaPlayer) {
            for (MediaPlayerSource mediaPlayerSource : this.mQueue) {
                if (mediaPlayerSource.mPlayer == mediaPlayer) {
                    return mediaPlayerSource;
                }
            }
            return null;
        }
    }

    /* renamed from: android.support.v4.media.MediaPlayer2Impl$Mp2EventNotifier */
    private interface Mp2EventNotifier {
        void notify(EventCallback eventCallback);
    }

    /* renamed from: android.support.v4.media.MediaPlayer2Impl$NoDrmSchemeExceptionImpl */
    public static final class NoDrmSchemeExceptionImpl extends MediaPlayer2.NoDrmSchemeException {
        public NoDrmSchemeExceptionImpl(String str) {
            super(str);
        }
    }

    /* renamed from: android.support.v4.media.MediaPlayer2Impl$PlayerEventNotifier */
    private interface PlayerEventNotifier {
        void notify(PlayerEventCallback playerEventCallback);
    }

    /* renamed from: android.support.v4.media.MediaPlayer2Impl$ProvisioningNetworkErrorExceptionImpl */
    public static final class ProvisioningNetworkErrorExceptionImpl extends MediaPlayer2.ProvisioningNetworkErrorException {
        public ProvisioningNetworkErrorExceptionImpl(String str) {
            super(str);
        }
    }

    /* renamed from: android.support.v4.media.MediaPlayer2Impl$ProvisioningServerErrorExceptionImpl */
    public static final class ProvisioningServerErrorExceptionImpl extends MediaPlayer2.ProvisioningServerErrorException {
        public ProvisioningServerErrorExceptionImpl(String str) {
            super(str);
        }
    }

    /* renamed from: android.support.v4.media.MediaPlayer2Impl$Task */
    private abstract class Task implements Runnable {
        /* access modifiers changed from: private */
        public DataSourceDesc mDSD;
        /* access modifiers changed from: private */
        public final int mMediaCallType;
        /* access modifiers changed from: private */
        public final boolean mNeedToWaitForEventToComplete;

        /* access modifiers changed from: 0000 */
        public abstract void process() throws IOException, MediaPlayer2.NoDrmSchemeException;

        Task(int i, boolean z) {
            this.mMediaCallType = i;
            this.mNeedToWaitForEventToComplete = z;
        }

        public void run() {
            int i;
            try {
                process();
                i = 0;
            } catch (IllegalStateException unused) {
                i = 1;
            } catch (IllegalArgumentException unused2) {
                i = 2;
            } catch (SecurityException unused3) {
                i = 3;
            } catch (IOException unused4) {
                i = 4;
            } catch (Exception unused5) {
                i = Integer.MIN_VALUE;
            }
            this.mDSD = MediaPlayer2Impl.this.getCurrentDataSource();
            if (!this.mNeedToWaitForEventToComplete || i != 0) {
                sendCompleteNotification(i);
                synchronized (MediaPlayer2Impl.this.mTaskLock) {
                    MediaPlayer2Impl.this.mCurrentTask = null;
                    MediaPlayer2Impl.this.processPendingTask_l();
                }
            }
        }

        /* access modifiers changed from: private */
        public void sendCompleteNotification(final int i) {
            if (this.mMediaCallType != 1003) {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(EventCallback eventCallback) {
                        Task task = Task.this;
                        eventCallback.onCallCompleted(MediaPlayer2Impl.this, task.mDSD, Task.this.mMediaCallType, i);
                    }
                });
            }
        }
    }

    /* renamed from: android.support.v4.media.MediaPlayer2Impl$TrackInfoImpl */
    public static final class TrackInfoImpl extends MediaPlayer2.TrackInfo {
        final MediaFormat mFormat;
        final int mTrackType;

        public int getTrackType() {
            return this.mTrackType;
        }

        public String getLanguage() {
            String string = this.mFormat.getString("language");
            return string == null ? "und" : string;
        }

        public MediaFormat getFormat() {
            int i = this.mTrackType;
            if (i == 3 || i == 4) {
                return this.mFormat;
            }
            return null;
        }

        TrackInfoImpl(int i, MediaFormat mediaFormat) {
            this.mTrackType = i;
            this.mFormat = mediaFormat;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append(TrackInfoImpl.class.getName());
            sb.append('{');
            int i = this.mTrackType;
            if (i == 1) {
                sb.append("VIDEO");
            } else if (i == 2) {
                sb.append("AUDIO");
            } else if (i == 3) {
                sb.append("TIMEDTEXT");
            } else if (i != 4) {
                sb.append("UNKNOWN");
            } else {
                sb.append("SUBTITLE");
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append(", ");
            sb2.append(this.mFormat.toString());
            sb.append(sb2.toString());
            sb.append("}");
            return sb.toString();
        }
    }

    public float getMaxPlayerVolume() {
        return 1.0f;
    }

    static {
        ArrayMap<Integer, Integer> arrayMap = sInfoEventMap;
        Integer valueOf = Integer.valueOf(1);
        arrayMap.put(valueOf, valueOf);
        ArrayMap<Integer, Integer> arrayMap2 = sInfoEventMap;
        Integer valueOf2 = Integer.valueOf(2);
        arrayMap2.put(valueOf2, valueOf2);
        ArrayMap<Integer, Integer> arrayMap3 = sInfoEventMap;
        Integer valueOf3 = Integer.valueOf(3);
        arrayMap3.put(valueOf3, valueOf3);
        ArrayMap<Integer, Integer> arrayMap4 = sInfoEventMap;
        Integer valueOf4 = Integer.valueOf(MediaPlayer2.MEDIA_INFO_VIDEO_TRACK_LAGGING);
        arrayMap4.put(valueOf4, valueOf4);
        ArrayMap<Integer, Integer> arrayMap5 = sInfoEventMap;
        Integer valueOf5 = Integer.valueOf(MediaPlayer2.MEDIA_INFO_BUFFERING_START);
        arrayMap5.put(valueOf5, valueOf5);
        ArrayMap<Integer, Integer> arrayMap6 = sInfoEventMap;
        Integer valueOf6 = Integer.valueOf(MediaPlayer2.MEDIA_INFO_BUFFERING_END);
        arrayMap6.put(valueOf6, valueOf6);
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_BAD_INTERLEAVING), Integer.valueOf(MediaPlayer2.MEDIA_INFO_BAD_INTERLEAVING));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_NOT_SEEKABLE), Integer.valueOf(MediaPlayer2.MEDIA_INFO_NOT_SEEKABLE));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_METADATA_UPDATE), Integer.valueOf(MediaPlayer2.MEDIA_INFO_METADATA_UPDATE));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_AUDIO_NOT_PLAYING), Integer.valueOf(MediaPlayer2.MEDIA_INFO_AUDIO_NOT_PLAYING));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_VIDEO_NOT_PLAYING), Integer.valueOf(MediaPlayer2.MEDIA_INFO_VIDEO_NOT_PLAYING));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_UNSUPPORTED_SUBTITLE), Integer.valueOf(MediaPlayer2.MEDIA_INFO_UNSUPPORTED_SUBTITLE));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_SUBTITLE_TIMED_OUT), Integer.valueOf(MediaPlayer2.MEDIA_INFO_SUBTITLE_TIMED_OUT));
        sErrorEventMap.put(valueOf, valueOf);
        sErrorEventMap.put(Integer.valueOf(200), Integer.valueOf(200));
        sErrorEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_ERROR_IO), Integer.valueOf(MediaPlayer2.MEDIA_ERROR_IO));
        sErrorEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_ERROR_MALFORMED), Integer.valueOf(MediaPlayer2.MEDIA_ERROR_MALFORMED));
        sErrorEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_ERROR_UNSUPPORTED), Integer.valueOf(MediaPlayer2.MEDIA_ERROR_UNSUPPORTED));
        sErrorEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_ERROR_TIMED_OUT), Integer.valueOf(MediaPlayer2.MEDIA_ERROR_TIMED_OUT));
        ArrayMap<Integer, Integer> arrayMap7 = sPrepareDrmStatusMap;
        Integer valueOf7 = Integer.valueOf(0);
        arrayMap7.put(valueOf7, valueOf7);
        sPrepareDrmStatusMap.put(valueOf, valueOf);
        sPrepareDrmStatusMap.put(valueOf2, valueOf2);
        sPrepareDrmStatusMap.put(valueOf2, valueOf2);
        sStateMap.put(Integer.valueOf(1001), valueOf7);
        sStateMap.put(Integer.valueOf(1002), valueOf);
        sStateMap.put(Integer.valueOf(1003), valueOf);
        sStateMap.put(Integer.valueOf(1004), valueOf2);
        sStateMap.put(Integer.valueOf(MediaPlayer2.MEDIAPLAYER2_STATE_ERROR), valueOf3);
    }

    /* access modifiers changed from: private */
    public void handleDataSourceError(final DataSourceError dataSourceError) {
        if (dataSourceError != null) {
            notifyMediaPlayer2Event(new Mp2EventNotifier() {
                public void notify(EventCallback eventCallback) {
                    MediaPlayer2Impl mediaPlayer2Impl = MediaPlayer2Impl.this;
                    DataSourceError dataSourceError = dataSourceError;
                    eventCallback.onError(mediaPlayer2Impl, dataSourceError.mDSD, dataSourceError.mWhat, dataSourceError.mExtra);
                }
            });
        }
    }

    public MediaPlayer2Impl() {
        this.mHandlerThread.start();
        Looper looper = this.mHandlerThread.getLooper();
        this.mEndPositionHandler = new Handler(looper);
        this.mTaskHandler = new Handler(looper);
        this.mPlayer = new MediaPlayerSourceQueue();
    }

    public BaseMediaPlayer getBaseMediaPlayer() {
        BaseMediaPlayerImpl baseMediaPlayerImpl;
        synchronized (this.mLock) {
            if (this.mBaseMediaPlayerImpl == null) {
                this.mBaseMediaPlayerImpl = new BaseMediaPlayerImpl();
            }
            baseMediaPlayerImpl = this.mBaseMediaPlayerImpl;
        }
        return baseMediaPlayerImpl;
    }

    public void close() {
        this.mPlayer.release();
        HandlerThread handlerThread = this.mHandlerThread;
        if (handlerThread != null) {
            handlerThread.quitSafely();
            this.mHandlerThread = null;
        }
    }

    public void play() {
        addTask(new Task(5, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.play();
            }
        });
    }

    public void prepare() {
        addTask(new Task(6, true) {
            /* access modifiers changed from: 0000 */
            public void process() throws IOException {
                MediaPlayer2Impl.this.mPlayer.prepareAsync();
            }
        });
    }

    public void pause() {
        addTask(new Task(4, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.pause();
            }
        });
    }

    public void skipToNext() {
        addTask(new Task(29, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.skipToNext();
            }
        });
    }

    public long getCurrentPosition() {
        return this.mPlayer.getCurrentPosition();
    }

    public long getDuration() {
        return this.mPlayer.getDuration();
    }

    public long getBufferedPosition() {
        return this.mPlayer.getBufferedPosition();
    }

    public int getState() {
        return this.mPlayer.getMediaPlayer2State();
    }

    /* access modifiers changed from: private */
    public int getPlayerState() {
        return this.mPlayer.getPlayerState();
    }

    /* access modifiers changed from: private */
    public int getBufferingState() {
        return this.mPlayer.getBufferingState();
    }

    public void setAudioAttributes(@NonNull final AudioAttributesCompat audioAttributesCompat) {
        addTask(new Task(16, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.setAudioAttributes(audioAttributesCompat);
            }
        });
    }

    @NonNull
    public AudioAttributesCompat getAudioAttributes() {
        return this.mPlayer.getAudioAttributes();
    }

    public void setDataSource(@NonNull final DataSourceDesc dataSourceDesc) {
        addTask(new Task(19, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                Preconditions.checkNotNull(dataSourceDesc, "the DataSourceDesc cannot be null");
                try {
                    MediaPlayer2Impl.this.mPlayer.setFirst(dataSourceDesc);
                } catch (IOException e) {
                    Log.e(MediaPlayer2Impl.TAG, "process: setDataSource", e);
                }
            }
        });
    }

    public void setNextDataSource(@NonNull final DataSourceDesc dataSourceDesc) {
        addTask(new Task(22, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                Preconditions.checkNotNull(dataSourceDesc, "the DataSourceDesc cannot be null");
                MediaPlayer2Impl mediaPlayer2Impl = MediaPlayer2Impl.this;
                mediaPlayer2Impl.handleDataSourceError(mediaPlayer2Impl.mPlayer.setNext(dataSourceDesc));
            }
        });
    }

    public void setNextDataSources(@NonNull final List<DataSourceDesc> list) {
        addTask(new Task(23, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                List list = list;
                if (list == null || list.size() == 0) {
                    throw new IllegalArgumentException("data source list cannot be null or empty.");
                }
                for (DataSourceDesc dataSourceDesc : list) {
                    if (dataSourceDesc == null) {
                        throw new IllegalArgumentException("DataSourceDesc in the source list cannot be null.");
                    }
                }
                MediaPlayer2Impl mediaPlayer2Impl = MediaPlayer2Impl.this;
                mediaPlayer2Impl.handleDataSourceError(mediaPlayer2Impl.mPlayer.setNextMultiple(list));
            }
        });
    }

    @NonNull
    public DataSourceDesc getCurrentDataSource() {
        return this.mPlayer.getFirst().getDSD();
    }

    public void loopCurrent(final boolean z) {
        addTask(new Task(3, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.setLooping(z);
            }
        });
    }

    public void setPlayerVolume(final float f) {
        addTask(new Task(26, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.setVolume(f);
            }
        });
    }

    public float getPlayerVolume() {
        return this.mPlayer.getVolume();
    }

    /* access modifiers changed from: private */
    public void registerPlayerEventCallback(@NonNull Executor executor, @NonNull PlayerEventCallback playerEventCallback) {
        if (playerEventCallback == null) {
            throw new IllegalArgumentException("Illegal null PlayerEventCallback");
        } else if (executor != null) {
            synchronized (this.mLock) {
                this.mPlayerEventCallbackMap.put(playerEventCallback, executor);
            }
        } else {
            throw new IllegalArgumentException("Illegal null Executor for the PlayerEventCallback");
        }
    }

    /* access modifiers changed from: private */
    public void unregisterPlayerEventCallback(@NonNull PlayerEventCallback playerEventCallback) {
        if (playerEventCallback != null) {
            synchronized (this.mLock) {
                this.mPlayerEventCallbackMap.remove(playerEventCallback);
            }
            return;
        }
        throw new IllegalArgumentException("Illegal null PlayerEventCallback");
    }

    public void notifyWhenCommandLabelReached(final Object obj) {
        addTask(new Task(1003, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(EventCallback eventCallback) {
                        C024812 r1 = C024812.this;
                        eventCallback.onCommandLabelReached(MediaPlayer2Impl.this, obj);
                    }
                });
            }
        });
    }

    public void setSurface(final Surface surface) {
        addTask(new Task(27, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.setSurface(surface);
            }
        });
    }

    public void clearPendingCommands() {
        synchronized (this.mTaskLock) {
            this.mPendingTasks.clear();
        }
    }

    private void addTask(Task task) {
        synchronized (this.mTaskLock) {
            this.mPendingTasks.add(task);
            processPendingTask_l();
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mTaskLock")
    public void processPendingTask_l() {
        if (this.mCurrentTask == null && !this.mPendingTasks.isEmpty()) {
            Task task = (Task) this.mPendingTasks.removeFirst();
            this.mCurrentTask = task;
            this.mTaskHandler.post(task);
        }
    }

    /* access modifiers changed from: private */
    public static void handleDataSource(MediaPlayerSource mediaPlayerSource) throws IOException {
        final DataSourceDesc dsd = mediaPlayerSource.getDSD();
        Preconditions.checkNotNull(dsd, "the DataSourceDesc cannot be null");
        MediaPlayer mediaPlayer = mediaPlayerSource.mPlayer;
        int type = dsd.getType();
        if (type == 1) {
            mediaPlayer.setDataSource(new MediaDataSource() {
                Media2DataSource mDataSource = dsd.getMedia2DataSource();

                public int readAt(long j, byte[] bArr, int i, int i2) throws IOException {
                    return this.mDataSource.readAt(j, bArr, i, i2);
                }

                public long getSize() throws IOException {
                    return this.mDataSource.getSize();
                }

                public void close() throws IOException {
                    this.mDataSource.close();
                }
            });
        } else if (type == 2) {
            mediaPlayer.setDataSource(dsd.getFileDescriptor(), dsd.getFileDescriptorOffset(), dsd.getFileDescriptorLength());
        } else if (type == 3) {
            mediaPlayer.setDataSource(dsd.getUriContext(), dsd.getUri(), dsd.getUriHeaders(), dsd.getUriCookies());
        }
    }

    public int getVideoWidth() {
        return this.mPlayer.getVideoWidth();
    }

    public int getVideoHeight() {
        return this.mPlayer.getVideoHeight();
    }

    public PersistableBundle getMetrics() {
        return this.mPlayer.getMetrics();
    }

    public void setPlaybackParams(@NonNull final PlaybackParams2 playbackParams2) {
        addTask(new Task(24, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.setPlaybackParamsInternal(playbackParams2.getPlaybackParams());
            }
        });
    }

    @NonNull
    public PlaybackParams2 getPlaybackParams() {
        return new Builder(this.mPlayer.getPlaybackParams()).build();
    }

    public void seekTo(long j, int i) {
        final long j2 = j;
        final int i2 = i;
        C025316 r0 = new Task(14, true) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.seekTo(j2, i2);
            }
        };
        addTask(r0);
    }

    @Nullable
    public MediaTimestamp2 getTimestamp() {
        return this.mPlayer.getTimestamp();
    }

    public void reset() {
        this.mPlayer.reset();
        synchronized (this.mLock) {
            this.mMp2EventCallbackRecord = null;
            this.mPlayerEventCallbackMap.clear();
            this.mDrmEventCallbackRecord = null;
        }
    }

    public void setAudioSessionId(final int i) {
        addTask(new Task(17, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.setAudioSessionId(i);
            }
        });
    }

    public int getAudioSessionId() {
        return this.mPlayer.getAudioSessionId();
    }

    public void attachAuxEffect(final int i) {
        addTask(new Task(1, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.attachAuxEffect(i);
            }
        });
    }

    public void setAuxEffectSendLevel(final float f) {
        addTask(new Task(18, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.setAuxEffectSendLevel(f);
            }
        });
    }

    public List<MediaPlayer2.TrackInfo> getTrackInfo() {
        TrackInfo[] trackInfo = this.mPlayer.getTrackInfo();
        ArrayList arrayList = new ArrayList();
        for (TrackInfo trackInfo2 : trackInfo) {
            arrayList.add(new TrackInfoImpl(trackInfo2.getTrackType(), trackInfo2.getFormat()));
        }
        return arrayList;
    }

    public int getSelectedTrack(int i) {
        return this.mPlayer.getSelectedTrack(i);
    }

    public void selectTrack(final int i) {
        addTask(new Task(15, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.selectTrack(i);
            }
        });
    }

    public void deselectTrack(final int i) {
        addTask(new Task(2, false) {
            /* access modifiers changed from: 0000 */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.deselectTrack(i);
            }
        });
    }

    public void setEventCallback(@NonNull Executor executor, @NonNull EventCallback eventCallback) {
        if (eventCallback == null) {
            throw new IllegalArgumentException("Illegal null EventCallback");
        } else if (executor != null) {
            synchronized (this.mLock) {
                this.mMp2EventCallbackRecord = new Pair<>(executor, eventCallback);
            }
        } else {
            throw new IllegalArgumentException("Illegal null Executor for the EventCallback");
        }
    }

    public void clearEventCallback() {
        synchronized (this.mLock) {
            this.mMp2EventCallbackRecord = null;
        }
    }

    public void setOnDrmConfigHelper(final MediaPlayer2.OnDrmConfigHelper onDrmConfigHelper) {
        this.mPlayer.setOnDrmConfigHelper(new OnDrmConfigHelper() {
            public void onDrmConfig(MediaPlayer mediaPlayer) {
                DataSourceDesc dataSourceDesc;
                MediaPlayerSource sourceForPlayer = MediaPlayer2Impl.this.mPlayer.getSourceForPlayer(mediaPlayer);
                if (sourceForPlayer == null) {
                    dataSourceDesc = null;
                } else {
                    dataSourceDesc = sourceForPlayer.getDSD();
                }
                onDrmConfigHelper.onDrmConfig(MediaPlayer2Impl.this, dataSourceDesc);
            }
        });
    }

    public void setDrmEventCallback(@NonNull Executor executor, @NonNull DrmEventCallback drmEventCallback) {
        if (drmEventCallback == null) {
            throw new IllegalArgumentException("Illegal null EventCallback");
        } else if (executor != null) {
            synchronized (this.mLock) {
                this.mDrmEventCallbackRecord = new Pair<>(executor, drmEventCallback);
            }
        } else {
            throw new IllegalArgumentException("Illegal null Executor for the EventCallback");
        }
    }

    public void clearDrmEventCallback() {
        synchronized (this.mLock) {
            this.mDrmEventCallbackRecord = null;
        }
    }

    public DrmInfo getDrmInfo() {
        MediaPlayer.DrmInfo drmInfo = this.mPlayer.getDrmInfo();
        if (drmInfo == null) {
            return null;
        }
        return new DrmInfoImpl(drmInfo.getPssh(), drmInfo.getSupportedSchemes());
    }

    public void prepareDrm(@NonNull UUID uuid) throws UnsupportedSchemeException, ResourceBusyException, MediaPlayer2.ProvisioningNetworkErrorException, MediaPlayer2.ProvisioningServerErrorException {
        try {
            this.mPlayer.prepareDrm(uuid);
        } catch (ProvisioningNetworkErrorException e) {
            throw new MediaPlayer2.ProvisioningNetworkErrorException(e.getMessage());
        } catch (ProvisioningServerErrorException e2) {
            throw new MediaPlayer2.ProvisioningServerErrorException(e2.getMessage());
        }
    }

    public void releaseDrm() throws MediaPlayer2.NoDrmSchemeException {
        try {
            this.mPlayer.releaseDrm();
        } catch (NoDrmSchemeException e) {
            throw new MediaPlayer2.NoDrmSchemeException(e.getMessage());
        }
    }

    @NonNull
    public KeyRequest getDrmKeyRequest(@Nullable byte[] bArr, @Nullable byte[] bArr2, @Nullable String str, int i, @Nullable Map<String, String> map) throws MediaPlayer2.NoDrmSchemeException {
        try {
            return this.mPlayer.getKeyRequest(bArr, bArr2, str, i, map);
        } catch (NoDrmSchemeException e) {
            throw new MediaPlayer2.NoDrmSchemeException(e.getMessage());
        }
    }

    public byte[] provideDrmKeyResponse(@Nullable byte[] bArr, @NonNull byte[] bArr2) throws MediaPlayer2.NoDrmSchemeException, DeniedByServerException {
        try {
            return this.mPlayer.provideKeyResponse(bArr, bArr2);
        } catch (NoDrmSchemeException e) {
            throw new MediaPlayer2.NoDrmSchemeException(e.getMessage());
        }
    }

    public void restoreDrmKeys(@NonNull byte[] bArr) throws MediaPlayer2.NoDrmSchemeException {
        try {
            this.mPlayer.restoreKeys(bArr);
        } catch (NoDrmSchemeException e) {
            throw new MediaPlayer2.NoDrmSchemeException(e.getMessage());
        }
    }

    @NonNull
    public String getDrmPropertyString(@NonNull String str) throws MediaPlayer2.NoDrmSchemeException {
        try {
            return this.mPlayer.getDrmPropertyString(str);
        } catch (NoDrmSchemeException e) {
            throw new MediaPlayer2.NoDrmSchemeException(e.getMessage());
        }
    }

    public void setDrmPropertyString(@NonNull String str, @NonNull String str2) throws MediaPlayer2.NoDrmSchemeException {
        try {
            this.mPlayer.setDrmPropertyString(str, str2);
        } catch (NoDrmSchemeException e) {
            throw new MediaPlayer2.NoDrmSchemeException(e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public void setPlaybackParamsInternal(final PlaybackParams playbackParams) {
        PlaybackParams playbackParams2 = this.mPlayer.getPlaybackParams();
        this.mPlayer.setPlaybackParams(playbackParams);
        if (playbackParams2.getSpeed() != playbackParams.getSpeed()) {
            notifyPlayerEvent(new PlayerEventNotifier() {
                public void notify(PlayerEventCallback playerEventCallback) {
                    playerEventCallback.onPlaybackSpeedChanged(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, playbackParams.getSpeed());
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void notifyMediaPlayer2Event(final Mp2EventNotifier mp2EventNotifier) {
        final Pair<Executor, EventCallback> pair;
        synchronized (this.mLock) {
            pair = this.mMp2EventCallbackRecord;
        }
        if (pair != null) {
            ((Executor) pair.first).execute(new Runnable() {
                public void run() {
                    mp2EventNotifier.notify((EventCallback) pair.second);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void notifyPlayerEvent(final PlayerEventNotifier playerEventNotifier) {
        ArrayMap arrayMap;
        synchronized (this.mLock) {
            arrayMap = new ArrayMap((SimpleArrayMap) this.mPlayerEventCallbackMap);
        }
        int size = arrayMap.size();
        for (int i = 0; i < size; i++) {
            final PlayerEventCallback playerEventCallback = (PlayerEventCallback) arrayMap.keyAt(i);
            ((Executor) arrayMap.valueAt(i)).execute(new Runnable() {
                public void run() {
                    playerEventNotifier.notify(playerEventCallback);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void notifyDrmEvent(final DrmEventNotifier drmEventNotifier) {
        final Pair<Executor, DrmEventCallback> pair;
        synchronized (this.mLock) {
            pair = this.mDrmEventCallbackRecord;
        }
        if (pair != null) {
            ((Executor) pair.first).execute(new Runnable() {
                public void run() {
                    drmEventNotifier.notify((DrmEventCallback) pair.second);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void setEndPositionTimerIfNeeded(final OnCompletionListener onCompletionListener, final MediaPlayerSource mediaPlayerSource, MediaTimestamp mediaTimestamp) {
        if (mediaPlayerSource == this.mPlayer.getFirst()) {
            this.mEndPositionHandler.removeCallbacksAndMessages(null);
            DataSourceDesc dsd = mediaPlayerSource.getDSD();
            if (dsd.getEndPosition() != 576460752303423487L && mediaTimestamp.getMediaClockRate() > 0.0f) {
                long endPosition = (long) (((float) (dsd.getEndPosition() - ((mediaTimestamp.getAnchorMediaTimeUs() + ((System.nanoTime() - mediaTimestamp.getAnchorSytemNanoTime()) / 1000)) / 1000))) / mediaTimestamp.getMediaClockRate());
                Handler handler = this.mEndPositionHandler;
                C026527 r2 = new Runnable() {
                    public void run() {
                        if (MediaPlayer2Impl.this.mPlayer.getFirst() == mediaPlayerSource) {
                            MediaPlayer2Impl.this.mPlayer.pause();
                            onCompletionListener.onCompletion(mediaPlayerSource.mPlayer);
                        }
                    }
                };
                long j = 0;
                if (endPosition >= 0) {
                    j = endPosition;
                }
                handler.postDelayed(r2, j);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setUpListeners(final MediaPlayerSource mediaPlayerSource) {
        MediaPlayer mediaPlayer = mediaPlayerSource.mPlayer;
        final C026628 r1 = new OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {
                MediaPlayer2Impl mediaPlayer2Impl = MediaPlayer2Impl.this;
                mediaPlayer2Impl.handleDataSourceError(mediaPlayer2Impl.mPlayer.onPrepared(mediaPlayer));
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(EventCallback eventCallback) {
                        C026628 r3 = C026628.this;
                        eventCallback.onInfo(MediaPlayer2Impl.this, mediaPlayerSource.getDSD(), 100, 0);
                    }
                });
                MediaPlayer2Impl.this.notifyPlayerEvent(new PlayerEventNotifier() {
                    public void notify(PlayerEventCallback playerEventCallback) {
                        playerEventCallback.onMediaPrepared(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, mediaPlayerSource.getDSD());
                    }
                });
                synchronized (MediaPlayer2Impl.this.mTaskLock) {
                    if (MediaPlayer2Impl.this.mCurrentTask != null && MediaPlayer2Impl.this.mCurrentTask.mMediaCallType == 6 && MediaPlayer2Impl.this.mCurrentTask.mDSD == mediaPlayerSource.getDSD() && MediaPlayer2Impl.this.mCurrentTask.mNeedToWaitForEventToComplete) {
                        MediaPlayer2Impl.this.mCurrentTask.sendCompleteNotification(0);
                        MediaPlayer2Impl.this.mCurrentTask = null;
                        MediaPlayer2Impl.this.processPendingTask_l();
                    }
                }
            }
        };
        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (mediaPlayerSource.getDSD().getStartPosition() != 0) {
                    MediaPlayerSource mediaPlayerSource = mediaPlayerSource;
                    mediaPlayerSource.mPlayer.seekTo((long) ((int) mediaPlayerSource.getDSD().getStartPosition()), 3);
                    return;
                }
                r1.onPrepared(mediaPlayer);
            }
        });
        mediaPlayer.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mediaPlayer, final int i, final int i2) {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(EventCallback eventCallback) {
                        C027130 r0 = C027130.this;
                        eventCallback.onVideoSizeChanged(MediaPlayer2Impl.this, mediaPlayerSource.getDSD(), i, i2);
                    }
                });
            }
        });
        mediaPlayer.setOnInfoListener(new OnInfoListener() {
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i2) {
                if (i == 3) {
                    MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                        public void notify(EventCallback eventCallback) {
                            C027331 r3 = C027331.this;
                            eventCallback.onInfo(MediaPlayer2Impl.this, mediaPlayerSource.getDSD(), 3, 0);
                        }
                    });
                } else if (i == 701) {
                    MediaPlayer2Impl.this.mPlayer.setBufferingState(mediaPlayer, 2);
                } else if (i == 702) {
                    MediaPlayer2Impl.this.mPlayer.setBufferingState(mediaPlayer, 1);
                }
                return false;
            }
        });
        final C027532 r2 = new OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer) {
                MediaPlayer2Impl mediaPlayer2Impl = MediaPlayer2Impl.this;
                mediaPlayer2Impl.handleDataSourceError(mediaPlayer2Impl.mPlayer.onCompletion(mediaPlayer));
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(EventCallback eventCallback) {
                        C027532 r3 = C027532.this;
                        eventCallback.onInfo(MediaPlayer2Impl.this, mediaPlayerSource.getDSD(), 5, 0);
                    }
                });
            }
        };
        mediaPlayer.setOnCompletionListener(r2);
        mediaPlayer.setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mediaPlayer, final int i, final int i2) {
                MediaPlayer2Impl.this.mPlayer.onError(mediaPlayer);
                synchronized (MediaPlayer2Impl.this.mTaskLock) {
                    if (MediaPlayer2Impl.this.mCurrentTask != null && MediaPlayer2Impl.this.mCurrentTask.mNeedToWaitForEventToComplete) {
                        MediaPlayer2Impl.this.mCurrentTask.sendCompleteNotification(Integer.MIN_VALUE);
                        MediaPlayer2Impl.this.mCurrentTask = null;
                        MediaPlayer2Impl.this.processPendingTask_l();
                    }
                }
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(EventCallback eventCallback) {
                        int intValue = ((Integer) MediaPlayer2Impl.sErrorEventMap.getOrDefault(Integer.valueOf(i), Integer.valueOf(1))).intValue();
                        C027733 r1 = C027733.this;
                        eventCallback.onError(MediaPlayer2Impl.this, mediaPlayerSource.getDSD(), intValue, i2);
                    }
                });
                return true;
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
            public void onSeekComplete(MediaPlayer mediaPlayer) {
                MediaPlayerSource mediaPlayerSource = mediaPlayerSource;
                if (mediaPlayerSource.mMp2State != 1001 || mediaPlayerSource.getDSD().getStartPosition() == 0) {
                    synchronized (MediaPlayer2Impl.this.mTaskLock) {
                        if (MediaPlayer2Impl.this.mCurrentTask != null && MediaPlayer2Impl.this.mCurrentTask.mMediaCallType == 14 && MediaPlayer2Impl.this.mCurrentTask.mNeedToWaitForEventToComplete) {
                            MediaPlayer2Impl.this.mCurrentTask.sendCompleteNotification(0);
                            MediaPlayer2Impl.this.mCurrentTask = null;
                            MediaPlayer2Impl.this.processPendingTask_l();
                        }
                    }
                    final long currentPosition = MediaPlayer2Impl.this.getCurrentPosition();
                    MediaPlayer2Impl.this.notifyPlayerEvent(new PlayerEventNotifier() {
                        public void notify(PlayerEventCallback playerEventCallback) {
                            playerEventCallback.onSeekCompleted(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, currentPosition);
                        }
                    });
                    return;
                }
                r1.onPrepared(mediaPlayer);
            }
        });
        mediaPlayer.setOnTimedMetaDataAvailableListener(new OnTimedMetaDataAvailableListener() {
            public void onTimedMetaDataAvailable(MediaPlayer mediaPlayer, final TimedMetaData timedMetaData) {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(EventCallback eventCallback) {
                        C028135 r0 = C028135.this;
                        eventCallback.onTimedMetaDataAvailable(MediaPlayer2Impl.this, mediaPlayerSource.getDSD(), timedMetaData);
                    }
                });
            }
        });
        mediaPlayer.setOnInfoListener(new OnInfoListener() {
            public boolean onInfo(MediaPlayer mediaPlayer, final int i, final int i2) {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(EventCallback eventCallback) {
                        int intValue = ((Integer) MediaPlayer2Impl.sInfoEventMap.getOrDefault(Integer.valueOf(i), Integer.valueOf(1))).intValue();
                        C028336 r1 = C028336.this;
                        eventCallback.onInfo(MediaPlayer2Impl.this, mediaPlayerSource.getDSD(), intValue, i2);
                    }
                });
                return true;
            }
        });
        mediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
            public void onBufferingUpdate(MediaPlayer mediaPlayer, final int i) {
                if (i >= 100) {
                    MediaPlayer2Impl.this.mPlayer.setBufferingState(mediaPlayer, 3);
                }
                mediaPlayerSource.mBufferedPercentage.set(i);
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(EventCallback eventCallback) {
                        C028537 r0 = C028537.this;
                        eventCallback.onInfo(MediaPlayer2Impl.this, mediaPlayerSource.getDSD(), MediaPlayer2.MEDIA_INFO_BUFFERING_UPDATE, i);
                    }
                });
            }
        });
        mediaPlayer.setOnMediaTimeDiscontinuityListener(new OnMediaTimeDiscontinuityListener() {
            public void onMediaTimeDiscontinuity(MediaPlayer mediaPlayer, final MediaTimestamp mediaTimestamp) {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(EventCallback eventCallback) {
                        C028738 r0 = C028738.this;
                        eventCallback.onMediaTimeDiscontinuity(MediaPlayer2Impl.this, mediaPlayerSource.getDSD(), new MediaTimestamp2(mediaTimestamp));
                    }
                });
                MediaPlayer2Impl.this.setEndPositionTimerIfNeeded(r2, mediaPlayerSource, mediaTimestamp);
            }
        });
        mediaPlayer.setOnSubtitleDataListener(new OnSubtitleDataListener() {
            public void onSubtitleData(MediaPlayer mediaPlayer, final SubtitleData subtitleData) {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(EventCallback eventCallback) {
                        C028939 r0 = C028939.this;
                        eventCallback.onSubtitleData(MediaPlayer2Impl.this, mediaPlayerSource.getDSD(), new SubtitleData2(subtitleData));
                    }
                });
            }
        });
        mediaPlayer.setOnDrmInfoListener(new OnDrmInfoListener() {
            public void onDrmInfo(MediaPlayer mediaPlayer, final MediaPlayer.DrmInfo drmInfo) {
                MediaPlayer2Impl.this.notifyDrmEvent(new DrmEventNotifier() {
                    public void notify(DrmEventCallback drmEventCallback) {
                        C029240 r0 = C029240.this;
                        drmEventCallback.onDrmInfo(MediaPlayer2Impl.this, mediaPlayerSource.getDSD(), new DrmInfoImpl(drmInfo.getPssh(), drmInfo.getSupportedSchemes()));
                    }
                });
            }
        });
        mediaPlayer.setOnDrmPreparedListener(new OnDrmPreparedListener() {
            public void onDrmPrepared(MediaPlayer mediaPlayer, final int i) {
                MediaPlayer2Impl.this.notifyDrmEvent(new DrmEventNotifier() {
                    public void notify(DrmEventCallback drmEventCallback) {
                        int intValue = ((Integer) MediaPlayer2Impl.sPrepareDrmStatusMap.getOrDefault(Integer.valueOf(i), Integer.valueOf(3))).intValue();
                        C029441 r3 = C029441.this;
                        drmEventCallback.onDrmPrepared(MediaPlayer2Impl.this, mediaPlayerSource.getDSD(), intValue);
                    }
                });
            }
        });
    }
}
