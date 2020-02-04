package android.support.p000v4.media;

import android.annotation.TargetApi;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.p000v4.media.BaseMediaPlayer.PlayerEventCallback;
import android.support.p000v4.media.MediaSession2.OnDataSourceMissingHelper;
import android.support.p000v4.util.ArrayMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@TargetApi(19)
/* renamed from: android.support.v4.media.SessionPlaylistAgentImplBase */
class SessionPlaylistAgentImplBase extends MediaPlaylistAgent {
    @VisibleForTesting
    static final int END_OF_PLAYLIST = -1;
    @VisibleForTesting
    static final int NO_VALID_ITEMS = -2;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public PlayItem mCurrent;
    @GuardedBy("mLock")
    private OnDataSourceMissingHelper mDsmHelper;
    /* access modifiers changed from: private */
    public final PlayItem mEopPlayItem = new PlayItem(-1, null);
    @GuardedBy("mLock")
    private Map<MediaItem2, DataSourceDesc> mItemDsdMap = new ArrayMap();
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    @GuardedBy("mLock")
    private MediaMetadata2 mMetadata;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public BaseMediaPlayer mPlayer;
    private final MyPlayerEventCallback mPlayerCallback;
    @GuardedBy("mLock")
    private ArrayList<MediaItem2> mPlaylist = new ArrayList<>();
    @GuardedBy("mLock")
    private int mRepeatMode;
    private final MediaSession2ImplBase mSession;
    @GuardedBy("mLock")
    private int mShuffleMode;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public ArrayList<MediaItem2> mShuffledList = new ArrayList<>();

    /* renamed from: android.support.v4.media.SessionPlaylistAgentImplBase$MyPlayerEventCallback */
    private class MyPlayerEventCallback extends PlayerEventCallback {
        private MyPlayerEventCallback() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0035, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onCurrentDataSourceChanged(@android.support.annotation.NonNull android.support.p000v4.media.BaseMediaPlayer r4, @android.support.annotation.Nullable android.support.p000v4.media.DataSourceDesc r5) {
            /*
                r3 = this;
                android.support.v4.media.SessionPlaylistAgentImplBase r0 = android.support.p000v4.media.SessionPlaylistAgentImplBase.this
                java.lang.Object r0 = r0.mLock
                monitor-enter(r0)
                android.support.v4.media.SessionPlaylistAgentImplBase r1 = android.support.p000v4.media.SessionPlaylistAgentImplBase.this     // Catch:{ all -> 0x0036 }
                android.support.v4.media.BaseMediaPlayer r1 = r1.mPlayer     // Catch:{ all -> 0x0036 }
                if (r1 == r4) goto L_0x0011
                monitor-exit(r0)     // Catch:{ all -> 0x0036 }
                return
            L_0x0011:
                if (r5 != 0) goto L_0x0034
                android.support.v4.media.SessionPlaylistAgentImplBase r4 = android.support.p000v4.media.SessionPlaylistAgentImplBase.this     // Catch:{ all -> 0x0036 }
                android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r4 = r4.mCurrent     // Catch:{ all -> 0x0036 }
                if (r4 == 0) goto L_0x0034
                android.support.v4.media.SessionPlaylistAgentImplBase r4 = android.support.p000v4.media.SessionPlaylistAgentImplBase.this     // Catch:{ all -> 0x0036 }
                android.support.v4.media.SessionPlaylistAgentImplBase r5 = android.support.p000v4.media.SessionPlaylistAgentImplBase.this     // Catch:{ all -> 0x0036 }
                android.support.v4.media.SessionPlaylistAgentImplBase r1 = android.support.p000v4.media.SessionPlaylistAgentImplBase.this     // Catch:{ all -> 0x0036 }
                android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r1 = r1.mCurrent     // Catch:{ all -> 0x0036 }
                int r1 = r1.shuffledIdx     // Catch:{ all -> 0x0036 }
                r2 = 1
                android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r5 = r5.getNextValidPlayItemLocked(r1, r2)     // Catch:{ all -> 0x0036 }
                r4.mCurrent = r5     // Catch:{ all -> 0x0036 }
                android.support.v4.media.SessionPlaylistAgentImplBase r3 = android.support.p000v4.media.SessionPlaylistAgentImplBase.this     // Catch:{ all -> 0x0036 }
                r3.updateCurrentIfNeededLocked()     // Catch:{ all -> 0x0036 }
            L_0x0034:
                monitor-exit(r0)     // Catch:{ all -> 0x0036 }
                return
            L_0x0036:
                r3 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x0036 }
                throw r3
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.SessionPlaylistAgentImplBase.MyPlayerEventCallback.onCurrentDataSourceChanged(android.support.v4.media.BaseMediaPlayer, android.support.v4.media.DataSourceDesc):void");
        }
    }

    /* renamed from: android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem */
    private class PlayItem {
        public DataSourceDesc dsd;
        public MediaItem2 mediaItem;
        public int shuffledIdx;

        PlayItem(SessionPlaylistAgentImplBase sessionPlaylistAgentImplBase, int i) {
            this(i, null);
        }

        PlayItem(int i, DataSourceDesc dataSourceDesc) {
            this.shuffledIdx = i;
            if (i >= 0) {
                this.mediaItem = (MediaItem2) SessionPlaylistAgentImplBase.this.mShuffledList.get(i);
                if (dataSourceDesc == null) {
                    synchronized (SessionPlaylistAgentImplBase.this.mLock) {
                        this.dsd = SessionPlaylistAgentImplBase.this.retrieveDataSourceDescLocked(this.mediaItem);
                    }
                    return;
                }
                this.dsd = dataSourceDesc;
            }
        }

        /* access modifiers changed from: 0000 */
        public boolean isValid() {
            if (this == SessionPlaylistAgentImplBase.this.mEopPlayItem) {
                return true;
            }
            MediaItem2 mediaItem2 = this.mediaItem;
            if (mediaItem2 == null || this.dsd == null) {
                return false;
            }
            if (mediaItem2.getDataSourceDesc() != null && !this.mediaItem.getDataSourceDesc().equals(this.dsd)) {
                return false;
            }
            synchronized (SessionPlaylistAgentImplBase.this.mLock) {
                if (this.shuffledIdx >= SessionPlaylistAgentImplBase.this.mShuffledList.size()) {
                    return false;
                }
                if (this.mediaItem != SessionPlaylistAgentImplBase.this.mShuffledList.get(this.shuffledIdx)) {
                    return false;
                }
                return true;
            }
        }
    }

    private static int clamp(int i, int i2) {
        if (i < 0) {
            return 0;
        }
        if (i > i2) {
            i = i2;
        }
        return i;
    }

    public MediaItem2 getMediaItem(DataSourceDesc dataSourceDesc) {
        return null;
    }

    SessionPlaylistAgentImplBase(@NonNull MediaSession2ImplBase mediaSession2ImplBase, @NonNull BaseMediaPlayer baseMediaPlayer) {
        if (mediaSession2ImplBase == null) {
            throw new IllegalArgumentException("sessionImpl shouldn't be null");
        } else if (baseMediaPlayer != null) {
            this.mSession = mediaSession2ImplBase;
            this.mPlayer = baseMediaPlayer;
            this.mPlayerCallback = new MyPlayerEventCallback();
            this.mPlayer.registerPlayerEventCallback(this.mSession.getCallbackExecutor(), this.mPlayerCallback);
        } else {
            throw new IllegalArgumentException("player shouldn't be null");
        }
    }

    public void setPlayer(@NonNull BaseMediaPlayer baseMediaPlayer) {
        if (baseMediaPlayer != null) {
            synchronized (this.mLock) {
                if (baseMediaPlayer != this.mPlayer) {
                    this.mPlayer.unregisterPlayerEventCallback(this.mPlayerCallback);
                    this.mPlayer = baseMediaPlayer;
                    this.mPlayer.registerPlayerEventCallback(this.mSession.getCallbackExecutor(), this.mPlayerCallback);
                    updatePlayerDataSourceLocked();
                    return;
                }
                return;
            }
        }
        throw new IllegalArgumentException("player shouldn't be null");
    }

    public void setOnDataSourceMissingHelper(OnDataSourceMissingHelper onDataSourceMissingHelper) {
        synchronized (this.mLock) {
            this.mDsmHelper = onDataSourceMissingHelper;
        }
    }

    public void clearOnDataSourceMissingHelper() {
        synchronized (this.mLock) {
            this.mDsmHelper = null;
        }
    }

    @Nullable
    public List<MediaItem2> getPlaylist() {
        List<MediaItem2> unmodifiableList;
        synchronized (this.mLock) {
            unmodifiableList = Collections.unmodifiableList(this.mPlaylist);
        }
        return unmodifiableList;
    }

    public void setPlaylist(@NonNull List<MediaItem2> list, @Nullable MediaMetadata2 mediaMetadata2) {
        if (list != null) {
            synchronized (this.mLock) {
                this.mItemDsdMap.clear();
                this.mPlaylist.clear();
                this.mPlaylist.addAll(list);
                applyShuffleModeLocked();
                this.mMetadata = mediaMetadata2;
                this.mCurrent = getNextValidPlayItemLocked(-1, 1);
                updatePlayerDataSourceLocked();
            }
            notifyPlaylistChanged();
            return;
        }
        throw new IllegalArgumentException("list shouldn't be null");
    }

    @Nullable
    public MediaMetadata2 getPlaylistMetadata() {
        MediaMetadata2 mediaMetadata2;
        synchronized (this.mLock) {
            mediaMetadata2 = this.mMetadata;
        }
        return mediaMetadata2;
    }

    public void updatePlaylistMetadata(@Nullable MediaMetadata2 mediaMetadata2) {
        synchronized (this.mLock) {
            if (mediaMetadata2 != this.mMetadata) {
                this.mMetadata = mediaMetadata2;
                notifyPlaylistMetadataChanged();
            }
        }
    }

    public MediaItem2 getCurrentMediaItem() {
        MediaItem2 mediaItem2;
        synchronized (this.mLock) {
            mediaItem2 = this.mCurrent == null ? null : this.mCurrent.mediaItem;
        }
        return mediaItem2;
    }

    public void addPlaylistItem(int i, @NonNull MediaItem2 mediaItem2) {
        if (mediaItem2 != null) {
            synchronized (this.mLock) {
                int clamp = clamp(i, this.mPlaylist.size());
                this.mPlaylist.add(clamp, mediaItem2);
                if (this.mShuffleMode == 0) {
                    this.mShuffledList.add(clamp, mediaItem2);
                } else {
                    this.mShuffledList.add((int) (Math.random() * ((double) (this.mShuffledList.size() + 1))), mediaItem2);
                }
                if (!hasValidItem()) {
                    this.mCurrent = getNextValidPlayItemLocked(-1, 1);
                    updatePlayerDataSourceLocked();
                } else {
                    updateCurrentIfNeededLocked();
                }
            }
            notifyPlaylistChanged();
            return;
        }
        throw new IllegalArgumentException("item shouldn't be null");
    }

    public void removePlaylistItem(@NonNull MediaItem2 mediaItem2) {
        if (mediaItem2 != null) {
            synchronized (this.mLock) {
                if (this.mPlaylist.remove(mediaItem2)) {
                    this.mShuffledList.remove(mediaItem2);
                    this.mItemDsdMap.remove(mediaItem2);
                    updateCurrentIfNeededLocked();
                    notifyPlaylistChanged();
                    return;
                }
                return;
            }
        }
        throw new IllegalArgumentException("item shouldn't be null");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0051, code lost:
        notifyPlaylistChanged();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0054, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void replacePlaylistItem(int r6, @android.support.annotation.NonNull android.support.p000v4.media.MediaItem2 r7) {
        /*
            r5 = this;
            if (r7 == 0) goto L_0x0058
            java.lang.Object r0 = r5.mLock
            monitor-enter(r0)
            java.util.ArrayList<android.support.v4.media.MediaItem2> r1 = r5.mPlaylist     // Catch:{ all -> 0x0055 }
            int r1 = r1.size()     // Catch:{ all -> 0x0055 }
            if (r1 > 0) goto L_0x000f
            monitor-exit(r0)     // Catch:{ all -> 0x0055 }
            return
        L_0x000f:
            java.util.ArrayList<android.support.v4.media.MediaItem2> r1 = r5.mPlaylist     // Catch:{ all -> 0x0055 }
            int r1 = r1.size()     // Catch:{ all -> 0x0055 }
            r2 = 1
            int r1 = r1 - r2
            int r6 = clamp(r6, r1)     // Catch:{ all -> 0x0055 }
            java.util.ArrayList<android.support.v4.media.MediaItem2> r1 = r5.mShuffledList     // Catch:{ all -> 0x0055 }
            java.util.ArrayList<android.support.v4.media.MediaItem2> r3 = r5.mPlaylist     // Catch:{ all -> 0x0055 }
            java.lang.Object r3 = r3.get(r6)     // Catch:{ all -> 0x0055 }
            int r1 = r1.indexOf(r3)     // Catch:{ all -> 0x0055 }
            java.util.Map<android.support.v4.media.MediaItem2, android.support.v4.media.DataSourceDesc> r3 = r5.mItemDsdMap     // Catch:{ all -> 0x0055 }
            java.util.ArrayList<android.support.v4.media.MediaItem2> r4 = r5.mShuffledList     // Catch:{ all -> 0x0055 }
            java.lang.Object r4 = r4.get(r1)     // Catch:{ all -> 0x0055 }
            r3.remove(r4)     // Catch:{ all -> 0x0055 }
            java.util.ArrayList<android.support.v4.media.MediaItem2> r3 = r5.mShuffledList     // Catch:{ all -> 0x0055 }
            r3.set(r1, r7)     // Catch:{ all -> 0x0055 }
            java.util.ArrayList<android.support.v4.media.MediaItem2> r1 = r5.mPlaylist     // Catch:{ all -> 0x0055 }
            r1.set(r6, r7)     // Catch:{ all -> 0x0055 }
            boolean r6 = r5.hasValidItem()     // Catch:{ all -> 0x0055 }
            if (r6 != 0) goto L_0x004d
            r6 = -1
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r6 = r5.getNextValidPlayItemLocked(r6, r2)     // Catch:{ all -> 0x0055 }
            r5.mCurrent = r6     // Catch:{ all -> 0x0055 }
            r5.updatePlayerDataSourceLocked()     // Catch:{ all -> 0x0055 }
            goto L_0x0050
        L_0x004d:
            r5.updateCurrentIfNeededLocked()     // Catch:{ all -> 0x0055 }
        L_0x0050:
            monitor-exit(r0)     // Catch:{ all -> 0x0055 }
            r5.notifyPlaylistChanged()
            return
        L_0x0055:
            r5 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0055 }
            throw r5
        L_0x0058:
            java.lang.IllegalArgumentException r5 = new java.lang.IllegalArgumentException
            java.lang.String r6 = "item shouldn't be null"
            r5.<init>(r6)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.SessionPlaylistAgentImplBase.replacePlaylistItem(int, android.support.v4.media.MediaItem2):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002d, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void skipToPlaylistItem(@android.support.annotation.NonNull android.support.p000v4.media.MediaItem2 r3) {
        /*
            r2 = this;
            if (r3 == 0) goto L_0x0031
            java.lang.Object r0 = r2.mLock
            monitor-enter(r0)
            boolean r1 = r2.hasValidItem()     // Catch:{ all -> 0x002e }
            if (r1 == 0) goto L_0x002c
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r1 = r2.mCurrent     // Catch:{ all -> 0x002e }
            android.support.v4.media.MediaItem2 r1 = r1.mediaItem     // Catch:{ all -> 0x002e }
            boolean r1 = r3.equals(r1)     // Catch:{ all -> 0x002e }
            if (r1 == 0) goto L_0x0016
            goto L_0x002c
        L_0x0016:
            java.util.ArrayList<android.support.v4.media.MediaItem2> r1 = r2.mShuffledList     // Catch:{ all -> 0x002e }
            int r3 = r1.indexOf(r3)     // Catch:{ all -> 0x002e }
            if (r3 >= 0) goto L_0x0020
            monitor-exit(r0)     // Catch:{ all -> 0x002e }
            return
        L_0x0020:
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r1 = new android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem     // Catch:{ all -> 0x002e }
            r1.<init>(r2, r3)     // Catch:{ all -> 0x002e }
            r2.mCurrent = r1     // Catch:{ all -> 0x002e }
            r2.updateCurrentIfNeededLocked()     // Catch:{ all -> 0x002e }
            monitor-exit(r0)     // Catch:{ all -> 0x002e }
            return
        L_0x002c:
            monitor-exit(r0)     // Catch:{ all -> 0x002e }
            return
        L_0x002e:
            r2 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x002e }
            throw r2
        L_0x0031:
            java.lang.IllegalArgumentException r2 = new java.lang.IllegalArgumentException
            java.lang.String r3 = "item shouldn't be null"
            r2.<init>(r3)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.SessionPlaylistAgentImplBase.skipToPlaylistItem(android.support.v4.media.MediaItem2):void");
    }

    public void skipToPreviousItem() {
        synchronized (this.mLock) {
            if (hasValidItem()) {
                PlayItem nextValidPlayItemLocked = getNextValidPlayItemLocked(this.mCurrent.shuffledIdx, -1);
                if (nextValidPlayItemLocked != this.mEopPlayItem) {
                    this.mCurrent = nextValidPlayItemLocked;
                }
                updateCurrentIfNeededLocked();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0025, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void skipToNextItem() {
        /*
            r3 = this;
            java.lang.Object r0 = r3.mLock
            monitor-enter(r0)
            boolean r1 = r3.hasValidItem()     // Catch:{ all -> 0x0026 }
            if (r1 == 0) goto L_0x0024
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r1 = r3.mCurrent     // Catch:{ all -> 0x0026 }
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r2 = r3.mEopPlayItem     // Catch:{ all -> 0x0026 }
            if (r1 != r2) goto L_0x0010
            goto L_0x0024
        L_0x0010:
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r1 = r3.mCurrent     // Catch:{ all -> 0x0026 }
            int r1 = r1.shuffledIdx     // Catch:{ all -> 0x0026 }
            r2 = 1
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r1 = r3.getNextValidPlayItemLocked(r1, r2)     // Catch:{ all -> 0x0026 }
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r2 = r3.mEopPlayItem     // Catch:{ all -> 0x0026 }
            if (r1 == r2) goto L_0x001f
            r3.mCurrent = r1     // Catch:{ all -> 0x0026 }
        L_0x001f:
            r3.updateCurrentIfNeededLocked()     // Catch:{ all -> 0x0026 }
            monitor-exit(r0)     // Catch:{ all -> 0x0026 }
            return
        L_0x0024:
            monitor-exit(r0)     // Catch:{ all -> 0x0026 }
            return
        L_0x0026:
            r3 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0026 }
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.SessionPlaylistAgentImplBase.skipToNextItem():void");
    }

    public int getRepeatMode() {
        int i;
        synchronized (this.mLock) {
            i = this.mRepeatMode;
        }
        return i;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0044, code lost:
        notifyRepeatModeChanged();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0047, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setRepeatMode(int r5) {
        /*
            r4 = this;
            if (r5 < 0) goto L_0x004b
            r0 = 3
            if (r5 <= r0) goto L_0x0006
            goto L_0x004b
        L_0x0006:
            java.lang.Object r1 = r4.mLock
            monitor-enter(r1)
            int r2 = r4.mRepeatMode     // Catch:{ all -> 0x0048 }
            if (r2 != r5) goto L_0x000f
            monitor-exit(r1)     // Catch:{ all -> 0x0048 }
            return
        L_0x000f:
            r4.mRepeatMode = r5     // Catch:{ all -> 0x0048 }
            if (r5 == 0) goto L_0x003d
            r2 = 1
            if (r5 == r2) goto L_0x002d
            r3 = 2
            if (r5 == r3) goto L_0x001c
            if (r5 == r0) goto L_0x001c
            goto L_0x0043
        L_0x001c:
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r5 = r4.mCurrent     // Catch:{ all -> 0x0048 }
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r0 = r4.mEopPlayItem     // Catch:{ all -> 0x0048 }
            if (r5 != r0) goto L_0x003d
            r5 = -1
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r5 = r4.getNextValidPlayItemLocked(r5, r2)     // Catch:{ all -> 0x0048 }
            r4.mCurrent = r5     // Catch:{ all -> 0x0048 }
            r4.updatePlayerDataSourceLocked()     // Catch:{ all -> 0x0048 }
            goto L_0x003d
        L_0x002d:
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r5 = r4.mCurrent     // Catch:{ all -> 0x0048 }
            if (r5 == 0) goto L_0x0043
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r5 = r4.mCurrent     // Catch:{ all -> 0x0048 }
            android.support.v4.media.SessionPlaylistAgentImplBase$PlayItem r0 = r4.mEopPlayItem     // Catch:{ all -> 0x0048 }
            if (r5 == r0) goto L_0x0043
            android.support.v4.media.BaseMediaPlayer r5 = r4.mPlayer     // Catch:{ all -> 0x0048 }
            r5.loopCurrent(r2)     // Catch:{ all -> 0x0048 }
            goto L_0x0043
        L_0x003d:
            android.support.v4.media.BaseMediaPlayer r5 = r4.mPlayer     // Catch:{ all -> 0x0048 }
            r0 = 0
            r5.loopCurrent(r0)     // Catch:{ all -> 0x0048 }
        L_0x0043:
            monitor-exit(r1)     // Catch:{ all -> 0x0048 }
            r4.notifyRepeatModeChanged()
            return
        L_0x0048:
            r4 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0048 }
            throw r4
        L_0x004b:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.SessionPlaylistAgentImplBase.setRepeatMode(int):void");
    }

    public int getShuffleMode() {
        int i;
        synchronized (this.mLock) {
            i = this.mShuffleMode;
        }
        return i;
    }

    public void setShuffleMode(int i) {
        if (i >= 0 && i <= 2) {
            synchronized (this.mLock) {
                if (this.mShuffleMode != i) {
                    this.mShuffleMode = i;
                    applyShuffleModeLocked();
                    updateCurrentIfNeededLocked();
                    notifyShuffleModeChanged();
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    @VisibleForTesting
    public int getCurShuffledIndex() {
        int i;
        synchronized (this.mLock) {
            i = hasValidItem() ? this.mCurrent.shuffledIdx : -2;
        }
        return i;
    }

    private boolean hasValidItem() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mCurrent != null;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public DataSourceDesc retrieveDataSourceDescLocked(MediaItem2 mediaItem2) {
        DataSourceDesc dataSourceDesc = mediaItem2.getDataSourceDesc();
        if (dataSourceDesc != null) {
            this.mItemDsdMap.put(mediaItem2, dataSourceDesc);
            return dataSourceDesc;
        }
        DataSourceDesc dataSourceDesc2 = (DataSourceDesc) this.mItemDsdMap.get(mediaItem2);
        if (dataSourceDesc2 != null) {
            return dataSourceDesc2;
        }
        OnDataSourceMissingHelper onDataSourceMissingHelper = this.mDsmHelper;
        if (onDataSourceMissingHelper != null) {
            dataSourceDesc2 = onDataSourceMissingHelper.onDataSourceMissing(this.mSession.getInstance(), mediaItem2);
            if (dataSourceDesc2 != null) {
                this.mItemDsdMap.put(mediaItem2, dataSourceDesc2);
            }
        }
        return dataSourceDesc2;
    }

    /* access modifiers changed from: private */
    public PlayItem getNextValidPlayItemLocked(int i, int i2) {
        int size = this.mPlaylist.size();
        if (i == -1) {
            i = i2 > 0 ? -1 : size;
        }
        int i3 = i;
        int i4 = 0;
        while (true) {
            PlayItem playItem = null;
            if (i4 >= size) {
                return null;
            }
            i3 += i2;
            if (i3 < 0 || i3 >= this.mPlaylist.size()) {
                if (this.mRepeatMode == 0) {
                    if (i4 != size - 1) {
                        playItem = this.mEopPlayItem;
                    }
                    return playItem;
                }
                i3 = i3 < 0 ? this.mPlaylist.size() - 1 : 0;
            }
            DataSourceDesc retrieveDataSourceDescLocked = retrieveDataSourceDescLocked((MediaItem2) this.mShuffledList.get(i3));
            if (retrieveDataSourceDescLocked != null) {
                return new PlayItem(i3, retrieveDataSourceDescLocked);
            }
            i4++;
        }
    }

    /* access modifiers changed from: private */
    public void updateCurrentIfNeededLocked() {
        if (hasValidItem() && !this.mCurrent.isValid()) {
            int indexOf = this.mShuffledList.indexOf(this.mCurrent.mediaItem);
            if (indexOf >= 0) {
                this.mCurrent.shuffledIdx = indexOf;
                return;
            }
            if (this.mCurrent.shuffledIdx >= this.mShuffledList.size()) {
                this.mCurrent = getNextValidPlayItemLocked(this.mShuffledList.size() - 1, 1);
            } else {
                PlayItem playItem = this.mCurrent;
                playItem.mediaItem = (MediaItem2) this.mShuffledList.get(playItem.shuffledIdx);
                if (retrieveDataSourceDescLocked(this.mCurrent.mediaItem) == null) {
                    this.mCurrent = getNextValidPlayItemLocked(this.mCurrent.shuffledIdx, 1);
                }
            }
            updatePlayerDataSourceLocked();
        }
    }

    private void updatePlayerDataSourceLocked() {
        PlayItem playItem = this.mCurrent;
        if (playItem != null && playItem != this.mEopPlayItem) {
            DataSourceDesc currentDataSource = this.mPlayer.getCurrentDataSource();
            DataSourceDesc dataSourceDesc = this.mCurrent.dsd;
            if (currentDataSource != dataSourceDesc) {
                this.mPlayer.setDataSource(dataSourceDesc);
                BaseMediaPlayer baseMediaPlayer = this.mPlayer;
                boolean z = true;
                if (this.mRepeatMode != 1) {
                    z = false;
                }
                baseMediaPlayer.loopCurrent(z);
            }
        }
    }

    private void applyShuffleModeLocked() {
        this.mShuffledList.clear();
        this.mShuffledList.addAll(this.mPlaylist);
        int i = this.mShuffleMode;
        if (i == 1 || i == 2) {
            Collections.shuffle(this.mShuffledList);
        }
    }
}
