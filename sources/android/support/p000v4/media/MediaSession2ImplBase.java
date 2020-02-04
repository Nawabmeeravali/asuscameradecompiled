package android.support.p000v4.media;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.p000v4.media.BaseMediaPlayer.PlayerEventCallback;
import android.support.p000v4.media.MediaController2.PlaybackInfo;
import android.support.p000v4.media.MediaMetadata2.Builder;
import android.support.p000v4.media.MediaPlaylistAgent.PlaylistEventCallback;
import android.support.p000v4.media.MediaSession2.CommandButton;
import android.support.p000v4.media.MediaSession2.ControllerInfo;
import android.support.p000v4.media.MediaSession2.OnDataSourceMissingHelper;
import android.support.p000v4.media.MediaSession2.SessionCallback;
import android.support.p000v4.media.session.MediaSessionCompat;
import android.support.p000v4.media.session.PlaybackStateCompat;
import android.support.p000v4.util.ObjectsCompat;
import android.text.TextUtils;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executor;

@TargetApi(19)
/* renamed from: android.support.v4.media.MediaSession2ImplBase */
class MediaSession2ImplBase implements SupportLibraryImpl {
    static final boolean DEBUG = Log.isLoggable("MS2ImplBase", 3);
    /* access modifiers changed from: private */
    public final AudioFocusHandler mAudioFocusHandler;
    private final AudioManager mAudioManager;
    /* access modifiers changed from: private */
    public final SessionCallback mCallback;
    private final Executor mCallbackExecutor;
    private final Context mContext;
    @GuardedBy("mLock")
    private OnDataSourceMissingHelper mDsmHelper;
    private final Handler mHandler;
    private final HandlerThread mHandlerThread;
    private final MediaSession2 mInstance;
    final Object mLock = new Object();
    @GuardedBy("mLock")
    private PlaybackInfo mPlaybackInfo;
    @GuardedBy("mLock")
    private BaseMediaPlayer mPlayer;
    private final PlayerEventCallback mPlayerEventCallback;
    @GuardedBy("mLock")
    private MediaPlaylistAgent mPlaylistAgent;
    private final PlaylistEventCallback mPlaylistEventCallback;
    private final MediaSession2Stub mSession2Stub;
    private final PendingIntent mSessionActivity;
    private final MediaSessionCompat mSessionCompat;
    private final MediaSessionLegacyStub mSessionLegacyStub;
    @GuardedBy("mLock")
    private SessionPlaylistAgentImplBase mSessionPlaylistAgent;
    private final SessionToken2 mSessionToken;
    @GuardedBy("mLock")
    private VolumeProviderCompat mVolumeProvider;

    /* renamed from: android.support.v4.media.MediaSession2ImplBase$MyPlayerEventCallback */
    private static class MyPlayerEventCallback extends PlayerEventCallback {
        private final WeakReference<MediaSession2ImplBase> mSession;

        private MyPlayerEventCallback(MediaSession2ImplBase mediaSession2ImplBase) {
            this.mSession = new WeakReference<>(mediaSession2ImplBase);
        }

        public void onCurrentDataSourceChanged(final BaseMediaPlayer baseMediaPlayer, final DataSourceDesc dataSourceDesc) {
            final MediaSession2ImplBase session = getSession();
            if (session != null) {
                session.getCallbackExecutor().execute(new Runnable() {
                    public void run() {
                        final MediaItem2 mediaItem2;
                        DataSourceDesc dataSourceDesc = dataSourceDesc;
                        if (dataSourceDesc == null) {
                            mediaItem2 = null;
                        } else {
                            mediaItem2 = MyPlayerEventCallback.this.getMediaItem(session, dataSourceDesc);
                            if (mediaItem2 == null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("Cannot obtain media item from the dsd=");
                                sb.append(dataSourceDesc);
                                Log.w("MS2ImplBase", sb.toString());
                                return;
                            }
                        }
                        session.getCallback().onCurrentMediaItemChanged(session.getInstance(), baseMediaPlayer, mediaItem2);
                        session.notifyToAllControllers(new NotifyRunnable() {
                            public void run(ControllerCb controllerCb) throws RemoteException {
                                controllerCb.onCurrentMediaItemChanged(mediaItem2);
                            }
                        });
                    }
                });
            }
        }

        public void onMediaPrepared(final BaseMediaPlayer baseMediaPlayer, final DataSourceDesc dataSourceDesc) {
            final MediaSession2ImplBase session = getSession();
            if (session != null && dataSourceDesc != null) {
                session.getCallbackExecutor().execute(new Runnable() {
                    public void run() {
                        MediaMetadata2 mediaMetadata2;
                        MediaItem2 access$300 = MyPlayerEventCallback.this.getMediaItem(session, dataSourceDesc);
                        if (access$300 != null) {
                            if (access$300.equals(session.getCurrentMediaItem())) {
                                long duration = session.getDuration();
                                if (duration >= 0) {
                                    MediaMetadata2 metadata = access$300.getMetadata();
                                    String str = "android.media.metadata.DURATION";
                                    if (metadata == null) {
                                        mediaMetadata2 = new Builder().putLong(str, duration).putString("android.media.metadata.MEDIA_ID", access$300.getMediaId()).build();
                                    } else if (!metadata.containsKey(str)) {
                                        mediaMetadata2 = new Builder(metadata).putLong(str, duration).build();
                                    } else {
                                        long j = metadata.getLong(str);
                                        if (duration != j) {
                                            StringBuilder sb = new StringBuilder();
                                            sb.append("duration mismatch for an item. duration from player=");
                                            sb.append(duration);
                                            sb.append(" duration from metadata=");
                                            sb.append(j);
                                            sb.append(". May be a timing issue?");
                                            Log.w("MS2ImplBase", sb.toString());
                                        }
                                        mediaMetadata2 = null;
                                    }
                                    if (mediaMetadata2 != null) {
                                        access$300.setMetadata(mediaMetadata2);
                                        session.notifyToAllControllers(new NotifyRunnable() {
                                            public void run(ControllerCb controllerCb) throws RemoteException {
                                                controllerCb.onPlaylistChanged(session.getPlaylist(), session.getPlaylistMetadata());
                                            }
                                        });
                                    }
                                } else {
                                    return;
                                }
                            }
                            session.getCallback().onMediaPrepared(session.getInstance(), baseMediaPlayer, access$300);
                        }
                    }
                });
            }
        }

        public void onPlayerStateChanged(final BaseMediaPlayer baseMediaPlayer, final int i) {
            final MediaSession2ImplBase session = getSession();
            if (session != null) {
                session.getCallbackExecutor().execute(new Runnable() {
                    public void run() {
                        session.mAudioFocusHandler.onPlayerStateChanged(i);
                        session.getCallback().onPlayerStateChanged(session.getInstance(), baseMediaPlayer, i);
                        session.notifyToAllControllers(new NotifyRunnable() {
                            public void run(ControllerCb controllerCb) throws RemoteException {
                                controllerCb.onPlayerStateChanged(SystemClock.elapsedRealtime(), baseMediaPlayer.getCurrentPosition(), i);
                            }
                        });
                    }
                });
            }
        }

        public void onBufferingStateChanged(BaseMediaPlayer baseMediaPlayer, DataSourceDesc dataSourceDesc, int i) {
            final MediaSession2ImplBase session = getSession();
            if (session != null && dataSourceDesc != null) {
                Executor callbackExecutor = session.getCallbackExecutor();
                final DataSourceDesc dataSourceDesc2 = dataSourceDesc;
                final BaseMediaPlayer baseMediaPlayer2 = baseMediaPlayer;
                final int i2 = i;
                C03414 r0 = new Runnable() {
                    public void run() {
                        final MediaItem2 access$300 = MyPlayerEventCallback.this.getMediaItem(session, dataSourceDesc2);
                        if (access$300 != null) {
                            session.getCallback().onBufferingStateChanged(session.getInstance(), baseMediaPlayer2, access$300, i2);
                            session.notifyToAllControllers(new NotifyRunnable() {
                                public void run(ControllerCb controllerCb) throws RemoteException {
                                    MediaItem2 mediaItem2 = access$300;
                                    C03414 r4 = C03414.this;
                                    controllerCb.onBufferingStateChanged(mediaItem2, i2, baseMediaPlayer2.getBufferedPosition());
                                }
                            });
                        }
                    }
                };
                callbackExecutor.execute(r0);
            }
        }

        public void onPlaybackSpeedChanged(final BaseMediaPlayer baseMediaPlayer, final float f) {
            final MediaSession2ImplBase session = getSession();
            if (session != null) {
                session.getCallbackExecutor().execute(new Runnable() {
                    public void run() {
                        session.getCallback().onPlaybackSpeedChanged(session.getInstance(), baseMediaPlayer, f);
                        session.notifyToAllControllers(new NotifyRunnable() {
                            public void run(ControllerCb controllerCb) throws RemoteException {
                                controllerCb.onPlaybackSpeedChanged(SystemClock.elapsedRealtime(), session.getCurrentPosition(), f);
                            }
                        });
                    }
                });
            }
        }

        public void onSeekCompleted(BaseMediaPlayer baseMediaPlayer, long j) {
            final MediaSession2ImplBase session = getSession();
            if (session != null) {
                Executor callbackExecutor = session.getCallbackExecutor();
                final BaseMediaPlayer baseMediaPlayer2 = baseMediaPlayer;
                final long j2 = j;
                C03456 r0 = new Runnable() {
                    public void run() {
                        session.getCallback().onSeekCompleted(session.getInstance(), baseMediaPlayer2, j2);
                        session.notifyToAllControllers(new NotifyRunnable() {
                            public void run(ControllerCb controllerCb) throws RemoteException {
                                controllerCb.onSeekCompleted(SystemClock.elapsedRealtime(), session.getCurrentPosition(), j2);
                            }
                        });
                    }
                };
                callbackExecutor.execute(r0);
            }
        }

        private MediaSession2ImplBase getSession() {
            MediaSession2ImplBase mediaSession2ImplBase = (MediaSession2ImplBase) this.mSession.get();
            if (mediaSession2ImplBase == null && MediaSession2ImplBase.DEBUG) {
                Log.d("MS2ImplBase", "Session is closed", new IllegalStateException());
            }
            return mediaSession2ImplBase;
        }

        /* access modifiers changed from: private */
        public MediaItem2 getMediaItem(MediaSession2ImplBase mediaSession2ImplBase, DataSourceDesc dataSourceDesc) {
            MediaPlaylistAgent playlistAgent = mediaSession2ImplBase.getPlaylistAgent();
            String str = "MS2ImplBase";
            if (playlistAgent == null) {
                if (MediaSession2ImplBase.DEBUG) {
                    Log.d(str, "Session is closed", new IllegalStateException());
                }
                return null;
            }
            MediaItem2 mediaItem = playlistAgent.getMediaItem(dataSourceDesc);
            if (mediaItem == null && MediaSession2ImplBase.DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("Could not find matching item for dsd=");
                sb.append(dataSourceDesc);
                Log.d(str, sb.toString(), new NoSuchElementException());
            }
            return mediaItem;
        }
    }

    /* renamed from: android.support.v4.media.MediaSession2ImplBase$MyPlaylistEventCallback */
    private static class MyPlaylistEventCallback extends PlaylistEventCallback {
        private final WeakReference<MediaSession2ImplBase> mSession;

        private MyPlaylistEventCallback(MediaSession2ImplBase mediaSession2ImplBase) {
            this.mSession = new WeakReference<>(mediaSession2ImplBase);
        }

        public void onPlaylistChanged(MediaPlaylistAgent mediaPlaylistAgent, List<MediaItem2> list, MediaMetadata2 mediaMetadata2) {
            MediaSession2ImplBase mediaSession2ImplBase = (MediaSession2ImplBase) this.mSession.get();
            if (mediaSession2ImplBase != null) {
                mediaSession2ImplBase.notifyPlaylistChangedOnExecutor(mediaPlaylistAgent, list, mediaMetadata2);
            }
        }

        public void onPlaylistMetadataChanged(MediaPlaylistAgent mediaPlaylistAgent, MediaMetadata2 mediaMetadata2) {
            MediaSession2ImplBase mediaSession2ImplBase = (MediaSession2ImplBase) this.mSession.get();
            if (mediaSession2ImplBase != null) {
                mediaSession2ImplBase.notifyPlaylistMetadataChangedOnExecutor(mediaPlaylistAgent, mediaMetadata2);
            }
        }

        public void onRepeatModeChanged(MediaPlaylistAgent mediaPlaylistAgent, int i) {
            MediaSession2ImplBase mediaSession2ImplBase = (MediaSession2ImplBase) this.mSession.get();
            if (mediaSession2ImplBase != null) {
                mediaSession2ImplBase.notifyRepeatModeChangedOnExecutor(mediaPlaylistAgent, i);
            }
        }

        public void onShuffleModeChanged(MediaPlaylistAgent mediaPlaylistAgent, int i) {
            MediaSession2ImplBase mediaSession2ImplBase = (MediaSession2ImplBase) this.mSession.get();
            if (mediaSession2ImplBase != null) {
                mediaSession2ImplBase.notifyShuffleModeChangedOnExecutor(mediaPlaylistAgent, i);
            }
        }
    }

    @FunctionalInterface
    /* renamed from: android.support.v4.media.MediaSession2ImplBase$NotifyRunnable */
    interface NotifyRunnable {
        void run(ControllerCb controllerCb) throws RemoteException;
    }

    public void skipBackward() {
    }

    public void skipForward() {
    }

    MediaSession2ImplBase(MediaSession2 mediaSession2, Context context, String str, BaseMediaPlayer baseMediaPlayer, MediaPlaylistAgent mediaPlaylistAgent, VolumeProviderCompat volumeProviderCompat, PendingIntent pendingIntent, Executor executor, SessionCallback sessionCallback) {
        Context context2 = context;
        String str2 = str;
        PendingIntent pendingIntent2 = pendingIntent;
        this.mContext = context2;
        this.mInstance = mediaSession2;
        this.mHandlerThread = new HandlerThread("MediaController2_Thread");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mSession2Stub = new MediaSession2Stub(this);
        this.mSessionLegacyStub = new MediaSessionLegacyStub(this);
        this.mSessionActivity = pendingIntent2;
        this.mCallback = sessionCallback;
        this.mCallbackExecutor = executor;
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mPlayerEventCallback = new MyPlayerEventCallback();
        this.mPlaylistEventCallback = new MyPlaylistEventCallback();
        this.mAudioFocusHandler = new AudioFocusHandler(context, getInstance());
        String serviceName = getServiceName(context, MediaLibraryService2.SERVICE_INTERFACE, str2);
        String serviceName2 = getServiceName(context, MediaSessionService2.SERVICE_INTERFACE, str2);
        if (serviceName2 == null || serviceName == null) {
            if (serviceName != null) {
                SessionToken2ImplBase sessionToken2ImplBase = new SessionToken2ImplBase(Process.myUid(), 2, context.getPackageName(), serviceName, str, this.mSession2Stub);
                this.mSessionToken = new SessionToken2(sessionToken2ImplBase);
            } else if (serviceName2 != null) {
                SessionToken2ImplBase sessionToken2ImplBase2 = new SessionToken2ImplBase(Process.myUid(), 1, context.getPackageName(), serviceName2, str, this.mSession2Stub);
                this.mSessionToken = new SessionToken2(sessionToken2ImplBase2);
            } else {
                SessionToken2ImplBase sessionToken2ImplBase3 = new SessionToken2ImplBase(Process.myUid(), 0, context.getPackageName(), null, str, this.mSession2Stub);
                this.mSessionToken = new SessionToken2(sessionToken2ImplBase3);
            }
            this.mSessionCompat = new MediaSessionCompat(context, str2, this.mSessionToken);
            this.mSessionCompat.setCallback(this.mSessionLegacyStub, this.mHandler);
            this.mSessionCompat.setSessionActivity(pendingIntent2);
            updatePlayer(baseMediaPlayer, mediaPlaylistAgent, volumeProviderCompat);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Ambiguous session type. Multiple session services define the same id=");
        sb.append(str2);
        throw new IllegalArgumentException(sb.toString());
    }

    public void updatePlayer(@NonNull BaseMediaPlayer baseMediaPlayer, @Nullable MediaPlaylistAgent mediaPlaylistAgent, @Nullable VolumeProviderCompat volumeProviderCompat) {
        boolean z;
        boolean z2;
        boolean z3;
        BaseMediaPlayer baseMediaPlayer2;
        MediaPlaylistAgent mediaPlaylistAgent2;
        if (baseMediaPlayer != null) {
            final PlaybackInfo createPlaybackInfo = createPlaybackInfo(volumeProviderCompat, baseMediaPlayer.getAudioAttributes());
            synchronized (this.mLock) {
                z = true;
                z2 = this.mPlayer != baseMediaPlayer;
                z3 = this.mPlaylistAgent != mediaPlaylistAgent;
                if (this.mPlaybackInfo == createPlaybackInfo) {
                    z = false;
                }
                baseMediaPlayer2 = this.mPlayer;
                mediaPlaylistAgent2 = this.mPlaylistAgent;
                this.mPlayer = baseMediaPlayer;
                if (mediaPlaylistAgent == null) {
                    this.mSessionPlaylistAgent = new SessionPlaylistAgentImplBase(this, this.mPlayer);
                    if (this.mDsmHelper != null) {
                        this.mSessionPlaylistAgent.setOnDataSourceMissingHelper(this.mDsmHelper);
                    }
                    mediaPlaylistAgent = this.mSessionPlaylistAgent;
                }
                this.mPlaylistAgent = mediaPlaylistAgent;
                this.mVolumeProvider = volumeProviderCompat;
                this.mPlaybackInfo = createPlaybackInfo;
            }
            if (volumeProviderCompat == null) {
                this.mSessionCompat.setPlaybackToLocal(getLegacyStreamType(baseMediaPlayer.getAudioAttributes()));
            }
            if (baseMediaPlayer != baseMediaPlayer2) {
                baseMediaPlayer.registerPlayerEventCallback(this.mCallbackExecutor, this.mPlayerEventCallback);
                if (baseMediaPlayer2 != null) {
                    baseMediaPlayer2.unregisterPlayerEventCallback(this.mPlayerEventCallback);
                }
            }
            if (mediaPlaylistAgent != mediaPlaylistAgent2) {
                mediaPlaylistAgent.registerPlaylistEventCallback(this.mCallbackExecutor, this.mPlaylistEventCallback);
                if (mediaPlaylistAgent2 != null) {
                    mediaPlaylistAgent2.unregisterPlaylistEventCallback(this.mPlaylistEventCallback);
                }
            }
            if (baseMediaPlayer2 != null) {
                if (z3) {
                    notifyAgentUpdatedNotLocked(mediaPlaylistAgent2);
                }
                if (z2) {
                    notifyPlayerUpdatedNotLocked(baseMediaPlayer2);
                }
                if (z) {
                    notifyToAllControllers(new NotifyRunnable() {
                        public void run(ControllerCb controllerCb) throws RemoteException {
                            controllerCb.onPlaybackInfoChanged(createPlaybackInfo);
                        }
                    });
                    return;
                }
                return;
            }
            return;
        }
        throw new IllegalArgumentException("player shouldn't be null");
    }

    private PlaybackInfo createPlaybackInfo(VolumeProviderCompat volumeProviderCompat, AudioAttributesCompat audioAttributesCompat) {
        int i = 2;
        if (volumeProviderCompat != null) {
            return PlaybackInfo.createPlaybackInfo(2, audioAttributesCompat, volumeProviderCompat.getVolumeControl(), volumeProviderCompat.getMaxVolume(), volumeProviderCompat.getCurrentVolume());
        }
        int legacyStreamType = getLegacyStreamType(audioAttributesCompat);
        if (VERSION.SDK_INT >= 21 && this.mAudioManager.isVolumeFixed()) {
            i = 0;
        }
        return PlaybackInfo.createPlaybackInfo(1, audioAttributesCompat, i, this.mAudioManager.getStreamMaxVolume(legacyStreamType), this.mAudioManager.getStreamVolume(legacyStreamType));
    }

    private int getLegacyStreamType(@Nullable AudioAttributesCompat audioAttributesCompat) {
        if (audioAttributesCompat == null) {
            return 3;
        }
        int legacyStreamType = audioAttributesCompat.getLegacyStreamType();
        if (legacyStreamType == Integer.MIN_VALUE) {
            return 3;
        }
        return legacyStreamType;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0044, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() {
        /*
            r3 = this;
            java.lang.Object r0 = r3.mLock
            monitor-enter(r0)
            android.support.v4.media.BaseMediaPlayer r1 = r3.mPlayer     // Catch:{ all -> 0x0045 }
            if (r1 != 0) goto L_0x0009
            monitor-exit(r0)     // Catch:{ all -> 0x0045 }
            return
        L_0x0009:
            android.support.v4.media.AudioFocusHandler r1 = r3.mAudioFocusHandler     // Catch:{ all -> 0x0045 }
            r1.close()     // Catch:{ all -> 0x0045 }
            android.support.v4.media.BaseMediaPlayer r1 = r3.mPlayer     // Catch:{ all -> 0x0045 }
            android.support.v4.media.BaseMediaPlayer$PlayerEventCallback r2 = r3.mPlayerEventCallback     // Catch:{ all -> 0x0045 }
            r1.unregisterPlayerEventCallback(r2)     // Catch:{ all -> 0x0045 }
            r1 = 0
            r3.mPlayer = r1     // Catch:{ all -> 0x0045 }
            android.support.v4.media.session.MediaSessionCompat r2 = r3.mSessionCompat     // Catch:{ all -> 0x0045 }
            r2.release()     // Catch:{ all -> 0x0045 }
            android.support.v4.media.MediaSession2ImplBase$2 r2 = new android.support.v4.media.MediaSession2ImplBase$2     // Catch:{ all -> 0x0045 }
            r2.<init>()     // Catch:{ all -> 0x0045 }
            r3.notifyToAllControllers(r2)     // Catch:{ all -> 0x0045 }
            android.os.Handler r2 = r3.mHandler     // Catch:{ all -> 0x0045 }
            r2.removeCallbacksAndMessages(r1)     // Catch:{ all -> 0x0045 }
            android.os.HandlerThread r1 = r3.mHandlerThread     // Catch:{ all -> 0x0045 }
            boolean r1 = r1.isAlive()     // Catch:{ all -> 0x0045 }
            if (r1 == 0) goto L_0x0043
            int r1 = android.os.Build.VERSION.SDK_INT     // Catch:{ all -> 0x0045 }
            r2 = 18
            if (r1 < r2) goto L_0x003e
            android.os.HandlerThread r3 = r3.mHandlerThread     // Catch:{ all -> 0x0045 }
            r3.quitSafely()     // Catch:{ all -> 0x0045 }
            goto L_0x0043
        L_0x003e:
            android.os.HandlerThread r3 = r3.mHandlerThread     // Catch:{ all -> 0x0045 }
            r3.quit()     // Catch:{ all -> 0x0045 }
        L_0x0043:
            monitor-exit(r0)     // Catch:{ all -> 0x0045 }
            return
        L_0x0045:
            r3 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0045 }
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.MediaSession2ImplBase.close():void");
    }

    @NonNull
    public BaseMediaPlayer getPlayer() {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        return baseMediaPlayer;
    }

    @NonNull
    public MediaPlaylistAgent getPlaylistAgent() {
        MediaPlaylistAgent mediaPlaylistAgent;
        synchronized (this.mLock) {
            mediaPlaylistAgent = this.mPlaylistAgent;
        }
        return mediaPlaylistAgent;
    }

    @Nullable
    public VolumeProviderCompat getVolumeProvider() {
        VolumeProviderCompat volumeProviderCompat;
        synchronized (this.mLock) {
            volumeProviderCompat = this.mVolumeProvider;
        }
        return volumeProviderCompat;
    }

    @NonNull
    public SessionToken2 getToken() {
        return this.mSessionToken;
    }

    @NonNull
    public List<ControllerInfo> getConnectedControllers() {
        return this.mSession2Stub.getConnectedControllers();
    }

    public void setCustomLayout(@NonNull ControllerInfo controllerInfo, @NonNull final List<CommandButton> list) {
        if (controllerInfo == null) {
            throw new IllegalArgumentException("controller shouldn't be null");
        } else if (list != null) {
            notifyToController(controllerInfo, new NotifyRunnable() {
                public void run(ControllerCb controllerCb) throws RemoteException {
                    controllerCb.onCustomLayoutChanged(list);
                }
            });
        } else {
            throw new IllegalArgumentException("layout shouldn't be null");
        }
    }

    public void setAllowedCommands(@NonNull ControllerInfo controllerInfo, @NonNull final SessionCommandGroup2 sessionCommandGroup2) {
        if (controllerInfo == null) {
            throw new IllegalArgumentException("controller shouldn't be null");
        } else if (sessionCommandGroup2 != null) {
            this.mSession2Stub.setAllowedCommands(controllerInfo, sessionCommandGroup2);
            notifyToController(controllerInfo, new NotifyRunnable() {
                public void run(ControllerCb controllerCb) throws RemoteException {
                    controllerCb.onAllowedCommandsChanged(sessionCommandGroup2);
                }
            });
        } else {
            throw new IllegalArgumentException("commands shouldn't be null");
        }
    }

    public void sendCustomCommand(@NonNull final SessionCommand2 sessionCommand2, @Nullable final Bundle bundle) {
        if (sessionCommand2 != null) {
            notifyToAllControllers(new NotifyRunnable() {
                public void run(ControllerCb controllerCb) throws RemoteException {
                    controllerCb.onCustomCommand(sessionCommand2, bundle, null);
                }
            });
            return;
        }
        throw new IllegalArgumentException("command shouldn't be null");
    }

    public void sendCustomCommand(@NonNull ControllerInfo controllerInfo, @NonNull final SessionCommand2 sessionCommand2, @Nullable final Bundle bundle, @Nullable final ResultReceiver resultReceiver) {
        if (controllerInfo == null) {
            throw new IllegalArgumentException("controller shouldn't be null");
        } else if (sessionCommand2 != null) {
            notifyToController(controllerInfo, new NotifyRunnable() {
                public void run(ControllerCb controllerCb) throws RemoteException {
                    controllerCb.onCustomCommand(sessionCommand2, bundle, resultReceiver);
                }
            });
        } else {
            throw new IllegalArgumentException("command shouldn't be null");
        }
    }

    public void play() {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        if (baseMediaPlayer != null) {
            if (this.mAudioFocusHandler.onPlayRequested()) {
                baseMediaPlayer.play();
            } else {
                Log.w("MS2ImplBase", "play() wouldn't be called because of the failure in audio focus");
            }
        } else if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
    }

    public void pause() {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        if (baseMediaPlayer != null) {
            if (this.mAudioFocusHandler.onPauseRequested()) {
                baseMediaPlayer.pause();
            } else {
                Log.w("MS2ImplBase", "pause() wouldn't be called of the failure in audio focus");
            }
        } else if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
    }

    public void reset() {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        if (baseMediaPlayer != null) {
            baseMediaPlayer.reset();
        } else if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
    }

    public void prepare() {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        if (baseMediaPlayer != null) {
            baseMediaPlayer.prepare();
        } else if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
    }

    public void seekTo(long j) {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        if (baseMediaPlayer != null) {
            baseMediaPlayer.seekTo(j);
        } else if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
    }

    public void notifyError(final int i, @Nullable final Bundle bundle) {
        notifyToAllControllers(new NotifyRunnable() {
            public void run(ControllerCb controllerCb) throws RemoteException {
                controllerCb.onError(i, bundle);
            }
        });
    }

    public void notifyRoutesInfoChanged(@NonNull ControllerInfo controllerInfo, @Nullable final List<Bundle> list) {
        notifyToController(controllerInfo, new NotifyRunnable() {
            public void run(ControllerCb controllerCb) throws RemoteException {
                controllerCb.onRoutesInfoChanged(list);
            }
        });
    }

    public int getPlayerState() {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        if (baseMediaPlayer != null) {
            return baseMediaPlayer.getPlayerState();
        }
        if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
        return 3;
    }

    public long getCurrentPosition() {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        if (baseMediaPlayer != null) {
            return baseMediaPlayer.getCurrentPosition();
        }
        if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
        return -1;
    }

    public long getDuration() {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        if (baseMediaPlayer != null) {
            return baseMediaPlayer.getDuration();
        }
        if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
        return -1;
    }

    public long getBufferedPosition() {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        if (baseMediaPlayer != null) {
            return baseMediaPlayer.getBufferedPosition();
        }
        if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
        return -1;
    }

    public int getBufferingState() {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        if (baseMediaPlayer != null) {
            return baseMediaPlayer.getBufferingState();
        }
        if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
        return 0;
    }

    public float getPlaybackSpeed() {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        if (baseMediaPlayer != null) {
            return baseMediaPlayer.getPlaybackSpeed();
        }
        if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
        return 1.0f;
    }

    public void setPlaybackSpeed(float f) {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        if (baseMediaPlayer != null) {
            baseMediaPlayer.setPlaybackSpeed(f);
        } else if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
    }

    public void setOnDataSourceMissingHelper(@NonNull OnDataSourceMissingHelper onDataSourceMissingHelper) {
        if (onDataSourceMissingHelper != null) {
            synchronized (this.mLock) {
                this.mDsmHelper = onDataSourceMissingHelper;
                if (this.mSessionPlaylistAgent != null) {
                    this.mSessionPlaylistAgent.setOnDataSourceMissingHelper(onDataSourceMissingHelper);
                }
            }
            return;
        }
        throw new IllegalArgumentException("helper shouldn't be null");
    }

    public void clearOnDataSourceMissingHelper() {
        synchronized (this.mLock) {
            this.mDsmHelper = null;
            if (this.mSessionPlaylistAgent != null) {
                this.mSessionPlaylistAgent.clearOnDataSourceMissingHelper();
            }
        }
    }

    public List<MediaItem2> getPlaylist() {
        MediaPlaylistAgent mediaPlaylistAgent;
        synchronized (this.mLock) {
            mediaPlaylistAgent = this.mPlaylistAgent;
        }
        if (mediaPlaylistAgent != null) {
            return mediaPlaylistAgent.getPlaylist();
        }
        if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
        return null;
    }

    public void setPlaylist(@NonNull List<MediaItem2> list, @Nullable MediaMetadata2 mediaMetadata2) {
        MediaPlaylistAgent mediaPlaylistAgent;
        if (list != null) {
            synchronized (this.mLock) {
                mediaPlaylistAgent = this.mPlaylistAgent;
            }
            if (mediaPlaylistAgent != null) {
                mediaPlaylistAgent.setPlaylist(list, mediaMetadata2);
            } else if (DEBUG) {
                Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
            }
        } else {
            throw new IllegalArgumentException("list shouldn't be null");
        }
    }

    public void skipToPlaylistItem(@NonNull MediaItem2 mediaItem2) {
        MediaPlaylistAgent mediaPlaylistAgent;
        if (mediaItem2 != null) {
            synchronized (this.mLock) {
                mediaPlaylistAgent = this.mPlaylistAgent;
            }
            if (mediaPlaylistAgent != null) {
                mediaPlaylistAgent.skipToPlaylistItem(mediaItem2);
            } else if (DEBUG) {
                Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
            }
        } else {
            throw new IllegalArgumentException("item shouldn't be null");
        }
    }

    public void skipToPreviousItem() {
        MediaPlaylistAgent mediaPlaylistAgent;
        synchronized (this.mLock) {
            mediaPlaylistAgent = this.mPlaylistAgent;
        }
        if (mediaPlaylistAgent != null) {
            mediaPlaylistAgent.skipToPreviousItem();
        } else if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
    }

    public void skipToNextItem() {
        MediaPlaylistAgent mediaPlaylistAgent;
        synchronized (this.mLock) {
            mediaPlaylistAgent = this.mPlaylistAgent;
        }
        if (mediaPlaylistAgent != null) {
            mediaPlaylistAgent.skipToNextItem();
        } else if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
    }

    public MediaMetadata2 getPlaylistMetadata() {
        MediaPlaylistAgent mediaPlaylistAgent;
        synchronized (this.mLock) {
            mediaPlaylistAgent = this.mPlaylistAgent;
        }
        if (mediaPlaylistAgent != null) {
            return mediaPlaylistAgent.getPlaylistMetadata();
        }
        if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
        return null;
    }

    public void addPlaylistItem(int i, @NonNull MediaItem2 mediaItem2) {
        MediaPlaylistAgent mediaPlaylistAgent;
        if (i < 0) {
            throw new IllegalArgumentException("index shouldn't be negative");
        } else if (mediaItem2 != null) {
            synchronized (this.mLock) {
                mediaPlaylistAgent = this.mPlaylistAgent;
            }
            if (mediaPlaylistAgent != null) {
                mediaPlaylistAgent.addPlaylistItem(i, mediaItem2);
            } else if (DEBUG) {
                Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
            }
        } else {
            throw new IllegalArgumentException("item shouldn't be null");
        }
    }

    public void removePlaylistItem(@NonNull MediaItem2 mediaItem2) {
        MediaPlaylistAgent mediaPlaylistAgent;
        if (mediaItem2 != null) {
            synchronized (this.mLock) {
                mediaPlaylistAgent = this.mPlaylistAgent;
            }
            if (mediaPlaylistAgent != null) {
                mediaPlaylistAgent.removePlaylistItem(mediaItem2);
            } else if (DEBUG) {
                Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
            }
        } else {
            throw new IllegalArgumentException("item shouldn't be null");
        }
    }

    public void replacePlaylistItem(int i, @NonNull MediaItem2 mediaItem2) {
        MediaPlaylistAgent mediaPlaylistAgent;
        if (i < 0) {
            throw new IllegalArgumentException("index shouldn't be negative");
        } else if (mediaItem2 != null) {
            synchronized (this.mLock) {
                mediaPlaylistAgent = this.mPlaylistAgent;
            }
            if (mediaPlaylistAgent != null) {
                mediaPlaylistAgent.replacePlaylistItem(i, mediaItem2);
            } else if (DEBUG) {
                Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
            }
        } else {
            throw new IllegalArgumentException("item shouldn't be null");
        }
    }

    public MediaItem2 getCurrentMediaItem() {
        MediaPlaylistAgent mediaPlaylistAgent;
        synchronized (this.mLock) {
            mediaPlaylistAgent = this.mPlaylistAgent;
        }
        if (mediaPlaylistAgent != null) {
            return mediaPlaylistAgent.getCurrentMediaItem();
        }
        if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
        return null;
    }

    public void updatePlaylistMetadata(@Nullable MediaMetadata2 mediaMetadata2) {
        MediaPlaylistAgent mediaPlaylistAgent;
        synchronized (this.mLock) {
            mediaPlaylistAgent = this.mPlaylistAgent;
        }
        if (mediaPlaylistAgent != null) {
            mediaPlaylistAgent.updatePlaylistMetadata(mediaMetadata2);
        } else if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
    }

    public int getRepeatMode() {
        MediaPlaylistAgent mediaPlaylistAgent;
        synchronized (this.mLock) {
            mediaPlaylistAgent = this.mPlaylistAgent;
        }
        if (mediaPlaylistAgent != null) {
            return mediaPlaylistAgent.getRepeatMode();
        }
        if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
        return 0;
    }

    public void setRepeatMode(int i) {
        MediaPlaylistAgent mediaPlaylistAgent;
        synchronized (this.mLock) {
            mediaPlaylistAgent = this.mPlaylistAgent;
        }
        if (mediaPlaylistAgent != null) {
            mediaPlaylistAgent.setRepeatMode(i);
        } else if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
    }

    public int getShuffleMode() {
        MediaPlaylistAgent mediaPlaylistAgent;
        synchronized (this.mLock) {
            mediaPlaylistAgent = this.mPlaylistAgent;
        }
        if (mediaPlaylistAgent != null) {
            return mediaPlaylistAgent.getShuffleMode();
        }
        if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
        return 0;
    }

    public void setShuffleMode(int i) {
        MediaPlaylistAgent mediaPlaylistAgent;
        synchronized (this.mLock) {
            mediaPlaylistAgent = this.mPlaylistAgent;
        }
        if (mediaPlaylistAgent != null) {
            mediaPlaylistAgent.setShuffleMode(i);
        } else if (DEBUG) {
            Log.d("MS2ImplBase", "API calls after the close()", new IllegalStateException());
        }
    }

    @NonNull
    public MediaSession2 getInstance() {
        return this.mInstance;
    }

    @NonNull
    public IBinder getSessionBinder() {
        return this.mSession2Stub.asBinder();
    }

    public Context getContext() {
        return this.mContext;
    }

    public Executor getCallbackExecutor() {
        return this.mCallbackExecutor;
    }

    public SessionCallback getCallback() {
        return this.mCallback;
    }

    public MediaSessionCompat getSessionCompat() {
        return this.mSessionCompat;
    }

    public AudioFocusHandler getAudioFocusHandler() {
        return this.mAudioFocusHandler;
    }

    public boolean isClosed() {
        return !this.mHandlerThread.isAlive();
    }

    public PlaybackStateCompat getPlaybackStateCompat() {
        PlaybackStateCompat build;
        synchronized (this.mLock) {
            build = new PlaybackStateCompat.Builder().setState(MediaUtils2.convertToPlaybackStateCompatState(getPlayerState(), getBufferingState()), getCurrentPosition(), getPlaybackSpeed()).setActions(3670015).setBufferedPosition(getBufferedPosition()).build();
        }
        return build;
    }

    public PlaybackInfo getPlaybackInfo() {
        PlaybackInfo playbackInfo;
        synchronized (this.mLock) {
            playbackInfo = this.mPlaybackInfo;
        }
        return playbackInfo;
    }

    public PendingIntent getSessionActivity() {
        return this.mSessionActivity;
    }

    private static String getServiceName(Context context, String str, String str2) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(str);
        intent.setPackage(context.getPackageName());
        List queryIntentServices = packageManager.queryIntentServices(intent, 128);
        String str3 = null;
        if (queryIntentServices != null) {
            for (int i = 0; i < queryIntentServices.size(); i++) {
                String sessionId = SessionToken2.getSessionId((ResolveInfo) queryIntentServices.get(i));
                if (!(sessionId == null || !TextUtils.equals(str2, sessionId) || ((ResolveInfo) queryIntentServices.get(i)).serviceInfo == null)) {
                    if (str3 == null) {
                        str3 = ((ResolveInfo) queryIntentServices.get(i)).serviceInfo.name;
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Ambiguous session type. Multiple session services define the same id=");
                        sb.append(str2);
                        throw new IllegalArgumentException(sb.toString());
                    }
                }
            }
        }
        return str3;
    }

    private void notifyAgentUpdatedNotLocked(MediaPlaylistAgent mediaPlaylistAgent) {
        List playlist = mediaPlaylistAgent.getPlaylist();
        final List playlist2 = getPlaylist();
        if (!ObjectsCompat.equals(playlist, playlist2)) {
            notifyToAllControllers(new NotifyRunnable() {
                public void run(ControllerCb controllerCb) throws RemoteException {
                    controllerCb.onPlaylistChanged(playlist2, MediaSession2ImplBase.this.getPlaylistMetadata());
                }
            });
        } else {
            MediaMetadata2 playlistMetadata = mediaPlaylistAgent.getPlaylistMetadata();
            final MediaMetadata2 playlistMetadata2 = getPlaylistMetadata();
            if (!ObjectsCompat.equals(playlistMetadata, playlistMetadata2)) {
                notifyToAllControllers(new NotifyRunnable() {
                    public void run(ControllerCb controllerCb) throws RemoteException {
                        controllerCb.onPlaylistMetadataChanged(playlistMetadata2);
                    }
                });
            }
        }
        MediaItem2 currentMediaItem = mediaPlaylistAgent.getCurrentMediaItem();
        final MediaItem2 currentMediaItem2 = getCurrentMediaItem();
        if (!ObjectsCompat.equals(currentMediaItem, currentMediaItem2)) {
            notifyToAllControllers(new NotifyRunnable() {
                public void run(ControllerCb controllerCb) throws RemoteException {
                    controllerCb.onCurrentMediaItemChanged(currentMediaItem2);
                }
            });
        }
        final int repeatMode = getRepeatMode();
        if (mediaPlaylistAgent.getRepeatMode() != repeatMode) {
            notifyToAllControllers(new NotifyRunnable() {
                public void run(ControllerCb controllerCb) throws RemoteException {
                    controllerCb.onRepeatModeChanged(repeatMode);
                }
            });
        }
        final int shuffleMode = getShuffleMode();
        if (mediaPlaylistAgent.getShuffleMode() != shuffleMode) {
            notifyToAllControllers(new NotifyRunnable() {
                public void run(ControllerCb controllerCb) throws RemoteException {
                    controllerCb.onShuffleModeChanged(shuffleMode);
                }
            });
        }
    }

    private void notifyPlayerUpdatedNotLocked(BaseMediaPlayer baseMediaPlayer) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        long currentPosition = getCurrentPosition();
        final int playerState = getPlayerState();
        final long j = elapsedRealtime;
        final long j2 = currentPosition;
        C031914 r0 = new NotifyRunnable() {
            public void run(ControllerCb controllerCb) throws RemoteException {
                controllerCb.onPlayerStateChanged(j, j2, playerState);
            }
        };
        notifyToAllControllers(r0);
        final MediaItem2 currentMediaItem = getCurrentMediaItem();
        if (currentMediaItem != null) {
            final int bufferingState = getBufferingState();
            final long bufferedPosition = getBufferedPosition();
            C032015 r02 = new NotifyRunnable() {
                public void run(ControllerCb controllerCb) throws RemoteException {
                    controllerCb.onBufferingStateChanged(currentMediaItem, bufferingState, bufferedPosition);
                }
            };
            notifyToAllControllers(r02);
        }
        final float playbackSpeed = getPlaybackSpeed();
        if (playbackSpeed != baseMediaPlayer.getPlaybackSpeed()) {
            final long j3 = elapsedRealtime;
            final long j4 = currentPosition;
            C032116 r03 = new NotifyRunnable() {
                public void run(ControllerCb controllerCb) throws RemoteException {
                    controllerCb.onPlaybackSpeedChanged(j3, j4, playbackSpeed);
                }
            };
            notifyToAllControllers(r03);
        }
    }

    /* access modifiers changed from: private */
    public void notifyPlaylistChangedOnExecutor(MediaPlaylistAgent mediaPlaylistAgent, final List<MediaItem2> list, final MediaMetadata2 mediaMetadata2) {
        synchronized (this.mLock) {
            if (mediaPlaylistAgent == this.mPlaylistAgent) {
                this.mCallback.onPlaylistChanged(this.mInstance, mediaPlaylistAgent, list, mediaMetadata2);
                notifyToAllControllers(new NotifyRunnable() {
                    public void run(ControllerCb controllerCb) throws RemoteException {
                        controllerCb.onPlaylistChanged(list, mediaMetadata2);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyPlaylistMetadataChangedOnExecutor(MediaPlaylistAgent mediaPlaylistAgent, final MediaMetadata2 mediaMetadata2) {
        synchronized (this.mLock) {
            if (mediaPlaylistAgent == this.mPlaylistAgent) {
                this.mCallback.onPlaylistMetadataChanged(this.mInstance, mediaPlaylistAgent, mediaMetadata2);
                notifyToAllControllers(new NotifyRunnable() {
                    public void run(ControllerCb controllerCb) throws RemoteException {
                        controllerCb.onPlaylistMetadataChanged(mediaMetadata2);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyRepeatModeChangedOnExecutor(MediaPlaylistAgent mediaPlaylistAgent, final int i) {
        synchronized (this.mLock) {
            if (mediaPlaylistAgent == this.mPlaylistAgent) {
                this.mCallback.onRepeatModeChanged(this.mInstance, mediaPlaylistAgent, i);
                notifyToAllControllers(new NotifyRunnable() {
                    public void run(ControllerCb controllerCb) throws RemoteException {
                        controllerCb.onRepeatModeChanged(i);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyShuffleModeChangedOnExecutor(MediaPlaylistAgent mediaPlaylistAgent, final int i) {
        synchronized (this.mLock) {
            if (mediaPlaylistAgent == this.mPlaylistAgent) {
                this.mCallback.onShuffleModeChanged(this.mInstance, mediaPlaylistAgent, i);
                notifyToAllControllers(new NotifyRunnable() {
                    public void run(ControllerCb controllerCb) throws RemoteException {
                        controllerCb.onShuffleModeChanged(i);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void notifyToController(@NonNull final ControllerInfo controllerInfo, @NonNull NotifyRunnable notifyRunnable) {
        String str = "MS2ImplBase";
        if (controllerInfo != null) {
            try {
                notifyRunnable.run(controllerInfo.getControllerCb());
            } catch (DeadObjectException e) {
                if (DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(controllerInfo.toString());
                    sb.append(" is gone");
                    Log.d(str, sb.toString(), e);
                }
                this.mSession2Stub.removeControllerInfo(controllerInfo);
                this.mCallbackExecutor.execute(new Runnable() {
                    public void run() {
                        MediaSession2ImplBase.this.mCallback.onDisconnected(MediaSession2ImplBase.this.getInstance(), controllerInfo);
                    }
                });
            } catch (RemoteException e2) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Exception in ");
                sb2.append(controllerInfo.toString());
                Log.w(str, sb2.toString(), e2);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void notifyToAllControllers(@NonNull NotifyRunnable notifyRunnable) {
        List connectedControllers = getConnectedControllers();
        for (int i = 0; i < connectedControllers.size(); i++) {
            notifyToController((ControllerInfo) connectedControllers.get(i), notifyRunnable);
        }
    }
}
