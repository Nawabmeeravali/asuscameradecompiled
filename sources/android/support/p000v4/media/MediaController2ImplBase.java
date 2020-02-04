package android.support.p000v4.media;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.mediacompat.Rating2;
import android.support.p000v4.media.IMediaSession2.Stub;
import android.support.p000v4.media.MediaController2.ControllerCallback;
import android.support.p000v4.media.MediaController2.PlaybackInfo;
import android.support.p000v4.media.MediaSession2.CommandButton;
import android.util.Log;
import java.util.List;
import java.util.concurrent.Executor;

/* renamed from: android.support.v4.media.MediaController2ImplBase */
class MediaController2ImplBase implements SupportLibraryImpl {
    static final boolean DEBUG = Log.isLoggable("MC2ImplBase", 3);
    @GuardedBy("mLock")
    private SessionCommandGroup2 mAllowedCommands;
    @GuardedBy("mLock")
    private long mBufferedPositionMs;
    @GuardedBy("mLock")
    private int mBufferingState;
    /* access modifiers changed from: private */
    public final ControllerCallback mCallback;
    private final Executor mCallbackExecutor;
    private final Context mContext;
    final MediaController2Stub mControllerStub;
    @GuardedBy("mLock")
    private MediaItem2 mCurrentMediaItem;
    private final DeathRecipient mDeathRecipient;
    @GuardedBy("mLock")
    private volatile IMediaSession2 mISession2;
    /* access modifiers changed from: private */
    public final MediaController2 mInstance;
    @GuardedBy("mLock")
    private boolean mIsReleased;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private PlaybackInfo mPlaybackInfo;
    @GuardedBy("mLock")
    private float mPlaybackSpeed;
    @GuardedBy("mLock")
    private int mPlayerState;
    @GuardedBy("mLock")
    private List<MediaItem2> mPlaylist;
    @GuardedBy("mLock")
    private MediaMetadata2 mPlaylistMetadata;
    @GuardedBy("mLock")
    private long mPositionEventTimeMs;
    @GuardedBy("mLock")
    private long mPositionMs;
    @GuardedBy("mLock")
    private int mRepeatMode;
    @GuardedBy("mLock")
    private SessionServiceConnection mServiceConnection;
    @GuardedBy("mLock")
    private PendingIntent mSessionActivity;
    @GuardedBy("mLock")
    private int mShuffleMode;
    /* access modifiers changed from: private */
    public final SessionToken2 mToken;

    /* renamed from: android.support.v4.media.MediaController2ImplBase$SessionServiceConnection */
    private class SessionServiceConnection implements ServiceConnection {
        private SessionServiceConnection() {
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            String str = "MC2ImplBase";
            if (MediaController2ImplBase.DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("onServiceConnected ");
                sb.append(componentName);
                sb.append(" ");
                sb.append(this);
                Log.d(str, sb.toString());
            }
            if (!MediaController2ImplBase.this.mToken.getPackageName().equals(componentName.getPackageName())) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(componentName);
                sb2.append(" was connected, but expected pkg=");
                sb2.append(MediaController2ImplBase.this.mToken.getPackageName());
                sb2.append(" with id=");
                sb2.append(MediaController2ImplBase.this.mToken.getId());
                Log.wtf(str, sb2.toString());
                return;
            }
            MediaController2ImplBase.this.connectToSession(Stub.asInterface(iBinder));
        }

        public void onServiceDisconnected(ComponentName componentName) {
            if (MediaController2ImplBase.DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("Session service ");
                sb.append(componentName);
                sb.append(" is disconnected.");
                Log.w("MC2ImplBase", sb.toString());
            }
        }

        public void onBindingDied(ComponentName componentName) {
            MediaController2ImplBase.this.close();
        }
    }

    @Nullable
    public MediaBrowserCompat getBrowserCompat() {
        return null;
    }

    public void skipBackward() {
    }

    public void skipForward() {
    }

    MediaController2ImplBase(Context context, MediaController2 mediaController2, SessionToken2 sessionToken2, Executor executor, ControllerCallback controllerCallback) {
        this.mInstance = mediaController2;
        if (context == null) {
            throw new IllegalArgumentException("context shouldn't be null");
        } else if (sessionToken2 == null) {
            throw new IllegalArgumentException("token shouldn't be null");
        } else if (controllerCallback == null) {
            throw new IllegalArgumentException("callback shouldn't be null");
        } else if (executor != null) {
            this.mContext = context;
            this.mControllerStub = new MediaController2Stub(this);
            this.mToken = sessionToken2;
            this.mCallback = controllerCallback;
            this.mCallbackExecutor = executor;
            this.mDeathRecipient = new DeathRecipient() {
                public void binderDied() {
                    MediaController2ImplBase.this.mInstance.close();
                }
            };
            IMediaSession2 asInterface = Stub.asInterface((IBinder) this.mToken.getBinder());
            if (this.mToken.getType() == 0) {
                this.mServiceConnection = null;
                connectToSession(asInterface);
                return;
            }
            this.mServiceConnection = new SessionServiceConnection();
            connectToService();
        } else {
            throw new IllegalArgumentException("executor shouldn't be null");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0040, code lost:
        if (r1 == null) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r1.asBinder().unlinkToDeath(r5.mDeathRecipient, 0);
        r1.release(r5.mControllerStub);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() {
        /*
            r5 = this;
            boolean r0 = DEBUG
            if (r0 == 0) goto L_0x001c
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "release from "
            r0.append(r1)
            android.support.v4.media.SessionToken2 r1 = r5.mToken
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "MC2ImplBase"
            android.util.Log.d(r1, r0)
        L_0x001c:
            java.lang.Object r0 = r5.mLock
            monitor-enter(r0)
            android.support.v4.media.IMediaSession2 r1 = r5.mISession2     // Catch:{ all -> 0x005c }
            boolean r2 = r5.mIsReleased     // Catch:{ all -> 0x005c }
            if (r2 == 0) goto L_0x0027
            monitor-exit(r0)     // Catch:{ all -> 0x005c }
            return
        L_0x0027:
            r2 = 1
            r5.mIsReleased = r2     // Catch:{ all -> 0x005c }
            android.support.v4.media.MediaController2ImplBase$SessionServiceConnection r2 = r5.mServiceConnection     // Catch:{ all -> 0x005c }
            r3 = 0
            if (r2 == 0) goto L_0x0038
            android.content.Context r2 = r5.mContext     // Catch:{ all -> 0x005c }
            android.support.v4.media.MediaController2ImplBase$SessionServiceConnection r4 = r5.mServiceConnection     // Catch:{ all -> 0x005c }
            r2.unbindService(r4)     // Catch:{ all -> 0x005c }
            r5.mServiceConnection = r3     // Catch:{ all -> 0x005c }
        L_0x0038:
            r5.mISession2 = r3     // Catch:{ all -> 0x005c }
            android.support.v4.media.MediaController2Stub r2 = r5.mControllerStub     // Catch:{ all -> 0x005c }
            r2.destroy()     // Catch:{ all -> 0x005c }
            monitor-exit(r0)     // Catch:{ all -> 0x005c }
            if (r1 == 0) goto L_0x0051
            android.os.IBinder r0 = r1.asBinder()     // Catch:{ RemoteException -> 0x0051 }
            android.os.IBinder$DeathRecipient r2 = r5.mDeathRecipient     // Catch:{ RemoteException -> 0x0051 }
            r3 = 0
            r0.unlinkToDeath(r2, r3)     // Catch:{ RemoteException -> 0x0051 }
            android.support.v4.media.MediaController2Stub r0 = r5.mControllerStub     // Catch:{ RemoteException -> 0x0051 }
            r1.release(r0)     // Catch:{ RemoteException -> 0x0051 }
        L_0x0051:
            java.util.concurrent.Executor r0 = r5.mCallbackExecutor
            android.support.v4.media.MediaController2ImplBase$2 r1 = new android.support.v4.media.MediaController2ImplBase$2
            r1.<init>()
            r0.execute(r1)
            return
        L_0x005c:
            r5 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x005c }
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.MediaController2ImplBase.close():void");
    }

    public SessionToken2 getSessionToken() {
        return this.mToken;
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mISession2 != null;
        }
        return z;
    }

    public void play() {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(1);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.play(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void pause() {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(2);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.pause(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void reset() {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(3);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.reset(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void prepare() {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(6);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.prepare(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void fastForward() {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(7);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.fastForward(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void rewind() {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(8);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.rewind(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void seekTo(long j) {
        if (j >= 0) {
            IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(9);
            if (sessionInterfaceIfAble != null) {
                try {
                    sessionInterfaceIfAble.seekTo(this.mControllerStub, j);
                } catch (RemoteException e) {
                    Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
                }
            }
        } else {
            throw new IllegalArgumentException("position shouldn't be negative");
        }
    }

    public void playFromMediaId(@NonNull String str, @Nullable Bundle bundle) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(22);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.playFromMediaId(this.mControllerStub, str, bundle);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void playFromSearch(@NonNull String str, @Nullable Bundle bundle) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(24);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.playFromSearch(this.mControllerStub, str, bundle);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void playFromUri(Uri uri, Bundle bundle) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(23);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.playFromUri(this.mControllerStub, uri, bundle);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void prepareFromMediaId(@NonNull String str, @Nullable Bundle bundle) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(25);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.prepareFromMediaId(this.mControllerStub, str, bundle);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void prepareFromSearch(@NonNull String str, @Nullable Bundle bundle) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(27);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.prepareFromSearch(this.mControllerStub, str, bundle);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void prepareFromUri(@NonNull Uri uri, @Nullable Bundle bundle) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(26);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.prepareFromUri(this.mControllerStub, uri, bundle);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void setVolumeTo(int i, int i2) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(10);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.setVolumeTo(this.mControllerStub, i, i2);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void adjustVolume(int i, int i2) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(11);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.adjustVolume(this.mControllerStub, i, i2);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public PendingIntent getSessionActivity() {
        PendingIntent pendingIntent;
        synchronized (this.mLock) {
            pendingIntent = this.mSessionActivity;
        }
        return pendingIntent;
    }

    public int getPlayerState() {
        int i;
        synchronized (this.mLock) {
            i = this.mPlayerState;
        }
        return i;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long getDuration() {
        /*
            r3 = this;
            java.lang.Object r0 = r3.mLock
            monitor-enter(r0)
            android.support.v4.media.MediaItem2 r3 = r3.mCurrentMediaItem     // Catch:{ all -> 0x001f }
            android.support.v4.media.MediaMetadata2 r3 = r3.getMetadata()     // Catch:{ all -> 0x001f }
            if (r3 == 0) goto L_0x001b
            java.lang.String r1 = "android.media.metadata.DURATION"
            boolean r1 = r3.containsKey(r1)     // Catch:{ all -> 0x001f }
            if (r1 == 0) goto L_0x001b
            java.lang.String r1 = "android.media.metadata.DURATION"
            long r1 = r3.getLong(r1)     // Catch:{ all -> 0x001f }
            monitor-exit(r0)     // Catch:{ all -> 0x001f }
            return r1
        L_0x001b:
            monitor-exit(r0)     // Catch:{ all -> 0x001f }
            r0 = -1
            return r0
        L_0x001f:
            r3 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x001f }
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.MediaController2ImplBase.getDuration():long");
    }

    public long getCurrentPosition() {
        long j;
        synchronized (this.mLock) {
            if (this.mISession2 == null) {
                Log.w("MC2ImplBase", "Session isn't active", new IllegalStateException());
                return -1;
            }
            if (this.mInstance.mTimeDiff != null) {
                j = this.mInstance.mTimeDiff.longValue();
            } else {
                j = SystemClock.elapsedRealtime() - this.mPositionEventTimeMs;
            }
            long max = Math.max(0, this.mPositionMs + ((long) (this.mPlaybackSpeed * ((float) j))));
            return max;
        }
    }

    public float getPlaybackSpeed() {
        synchronized (this.mLock) {
            if (this.mISession2 == null) {
                Log.w("MC2ImplBase", "Session isn't active", new IllegalStateException());
                return 0.0f;
            }
            float f = this.mPlaybackSpeed;
            return f;
        }
    }

    public void setPlaybackSpeed(float f) {
        synchronized (this.mLock) {
            IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(39);
            if (sessionInterfaceIfAble != null) {
                try {
                    sessionInterfaceIfAble.setPlaybackSpeed(this.mControllerStub, f);
                } catch (RemoteException e) {
                    Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
                }
            }
        }
    }

    public int getBufferingState() {
        synchronized (this.mLock) {
            if (this.mISession2 == null) {
                Log.w("MC2ImplBase", "Session isn't active", new IllegalStateException());
                return 0;
            }
            int i = this.mBufferingState;
            return i;
        }
    }

    public long getBufferedPosition() {
        synchronized (this.mLock) {
            if (this.mISession2 == null) {
                Log.w("MC2ImplBase", "Session isn't active", new IllegalStateException());
                return -1;
            }
            long j = this.mBufferedPositionMs;
            return j;
        }
    }

    public PlaybackInfo getPlaybackInfo() {
        PlaybackInfo playbackInfo;
        synchronized (this.mLock) {
            playbackInfo = this.mPlaybackInfo;
        }
        return playbackInfo;
    }

    public void setRating(@NonNull String str, @NonNull Rating2 rating2) {
        IMediaSession2 iMediaSession2;
        synchronized (this.mLock) {
            iMediaSession2 = this.mISession2;
        }
        if (iMediaSession2 != null) {
            try {
                iMediaSession2.setRating(this.mControllerStub, str, rating2.toBundle());
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void sendCustomCommand(@NonNull SessionCommand2 sessionCommand2, Bundle bundle, @Nullable ResultReceiver resultReceiver) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(sessionCommand2);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.sendCustomCommand(this.mControllerStub, sessionCommand2.toBundle(), bundle, resultReceiver);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public List<MediaItem2> getPlaylist() {
        List<MediaItem2> list;
        synchronized (this.mLock) {
            list = this.mPlaylist;
        }
        return list;
    }

    public void setPlaylist(@NonNull List<MediaItem2> list, @Nullable MediaMetadata2 mediaMetadata2) {
        Bundle bundle;
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(19);
        if (sessionInterfaceIfAble != null) {
            try {
                MediaController2Stub mediaController2Stub = this.mControllerStub;
                List convertMediaItem2ListToBundleList = MediaUtils2.convertMediaItem2ListToBundleList(list);
                if (mediaMetadata2 == null) {
                    bundle = null;
                } else {
                    bundle = mediaMetadata2.toBundle();
                }
                sessionInterfaceIfAble.setPlaylist(mediaController2Stub, convertMediaItem2ListToBundleList, bundle);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void updatePlaylistMetadata(@Nullable MediaMetadata2 mediaMetadata2) {
        Bundle bundle;
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(21);
        if (sessionInterfaceIfAble != null) {
            try {
                MediaController2Stub mediaController2Stub = this.mControllerStub;
                if (mediaMetadata2 == null) {
                    bundle = null;
                } else {
                    bundle = mediaMetadata2.toBundle();
                }
                sessionInterfaceIfAble.updatePlaylistMetadata(mediaController2Stub, bundle);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public MediaMetadata2 getPlaylistMetadata() {
        MediaMetadata2 mediaMetadata2;
        synchronized (this.mLock) {
            mediaMetadata2 = this.mPlaylistMetadata;
        }
        return mediaMetadata2;
    }

    public void addPlaylistItem(int i, @NonNull MediaItem2 mediaItem2) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(15);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.addPlaylistItem(this.mControllerStub, i, mediaItem2.toBundle());
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void removePlaylistItem(@NonNull MediaItem2 mediaItem2) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(16);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.removePlaylistItem(this.mControllerStub, mediaItem2.toBundle());
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void replacePlaylistItem(int i, @NonNull MediaItem2 mediaItem2) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(17);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.replacePlaylistItem(this.mControllerStub, i, mediaItem2.toBundle());
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public MediaItem2 getCurrentMediaItem() {
        MediaItem2 mediaItem2;
        synchronized (this.mLock) {
            mediaItem2 = this.mCurrentMediaItem;
        }
        return mediaItem2;
    }

    public void skipToPreviousItem() {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(5);
        synchronized (this.mLock) {
            if (sessionInterfaceIfAble != null) {
                try {
                    sessionInterfaceIfAble.skipToPreviousItem(this.mControllerStub);
                } catch (RemoteException e) {
                    Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
                }
            }
        }
    }

    public void skipToNextItem() {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(4);
        synchronized (this.mLock) {
            if (sessionInterfaceIfAble != null) {
                try {
                    this.mISession2.skipToNextItem(this.mControllerStub);
                } catch (RemoteException e) {
                    Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
                }
            }
        }
    }

    public void skipToPlaylistItem(@NonNull MediaItem2 mediaItem2) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(12);
        synchronized (this.mLock) {
            if (sessionInterfaceIfAble != null) {
                try {
                    this.mISession2.skipToPlaylistItem(this.mControllerStub, mediaItem2.toBundle());
                } catch (RemoteException e) {
                    Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
                }
            }
        }
    }

    public int getRepeatMode() {
        int i;
        synchronized (this.mLock) {
            i = this.mRepeatMode;
        }
        return i;
    }

    public void setRepeatMode(int i) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(14);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.setRepeatMode(this.mControllerStub, i);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public int getShuffleMode() {
        int i;
        synchronized (this.mLock) {
            i = this.mShuffleMode;
        }
        return i;
    }

    public void setShuffleMode(int i) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(13);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.setShuffleMode(this.mControllerStub, i);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void subscribeRoutesInfo() {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(36);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.subscribeRoutesInfo(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void unsubscribeRoutesInfo() {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(37);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.unsubscribeRoutesInfo(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void selectRoute(@NonNull Bundle bundle) {
        IMediaSession2 sessionInterfaceIfAble = getSessionInterfaceIfAble(38);
        if (sessionInterfaceIfAble != null) {
            try {
                sessionInterfaceIfAble.selectRoute(this.mControllerStub, bundle);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @NonNull
    public Context getContext() {
        return this.mContext;
    }

    @NonNull
    public ControllerCallback getCallback() {
        return this.mCallback;
    }

    @NonNull
    public Executor getCallbackExecutor() {
        return this.mCallbackExecutor;
    }

    @NonNull
    public MediaController2 getInstance() {
        return this.mInstance;
    }

    private void connectToService() {
        Intent intent = new Intent(MediaSessionService2.SERVICE_INTERFACE);
        intent.setClassName(this.mToken.getPackageName(), this.mToken.getServiceName());
        synchronized (this.mLock) {
            if (!this.mContext.bindService(intent, this.mServiceConnection, 1)) {
                StringBuilder sb = new StringBuilder();
                sb.append("bind to ");
                sb.append(this.mToken);
                sb.append(" failed");
                Log.w("MC2ImplBase", sb.toString());
            } else if (DEBUG) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("bind to ");
                sb2.append(this.mToken);
                sb2.append(" success");
                Log.d("MC2ImplBase", sb2.toString());
            }
        }
    }

    /* access modifiers changed from: private */
    public void connectToSession(IMediaSession2 iMediaSession2) {
        try {
            iMediaSession2.connect(this.mControllerStub, this.mContext.getPackageName());
        } catch (RemoteException unused) {
            Log.w("MC2ImplBase", "Failed to call connection request. Framework will retry automatically");
        }
    }

    /* access modifiers changed from: 0000 */
    public IMediaSession2 getSessionInterfaceIfAble(int i) {
        synchronized (this.mLock) {
            if (!this.mAllowedCommands.hasCommand(i)) {
                StringBuilder sb = new StringBuilder();
                sb.append("Controller isn't allowed to call command, commandCode=");
                sb.append(i);
                Log.w("MC2ImplBase", sb.toString());
                return null;
            }
            IMediaSession2 iMediaSession2 = this.mISession2;
            return iMediaSession2;
        }
    }

    /* access modifiers changed from: 0000 */
    public IMediaSession2 getSessionInterfaceIfAble(SessionCommand2 sessionCommand2) {
        synchronized (this.mLock) {
            if (!this.mAllowedCommands.hasCommand(sessionCommand2)) {
                StringBuilder sb = new StringBuilder();
                sb.append("Controller isn't allowed to call command, command=");
                sb.append(sessionCommand2);
                Log.w("MC2ImplBase", sb.toString());
                return null;
            }
            IMediaSession2 iMediaSession2 = this.mISession2;
            return iMediaSession2;
        }
    }

    /* access modifiers changed from: 0000 */
    public void notifyCurrentMediaItemChanged(final MediaItem2 mediaItem2) {
        synchronized (this.mLock) {
            this.mCurrentMediaItem = mediaItem2;
        }
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onCurrentMediaItemChanged(MediaController2ImplBase.this.mInstance, mediaItem2);
                }
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void notifyPlayerStateChanges(long j, long j2, final int i) {
        synchronized (this.mLock) {
            this.mPositionEventTimeMs = j;
            this.mPositionMs = j2;
            this.mPlayerState = i;
        }
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onPlayerStateChanged(MediaController2ImplBase.this.mInstance, i);
                }
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void notifyPlaybackSpeedChanges(long j, long j2, final float f) {
        synchronized (this.mLock) {
            this.mPositionEventTimeMs = j;
            this.mPositionMs = j2;
            this.mPlaybackSpeed = f;
        }
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onPlaybackSpeedChanged(MediaController2ImplBase.this.mInstance, f);
                }
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void notifyBufferingStateChanged(final MediaItem2 mediaItem2, final int i, long j) {
        synchronized (this.mLock) {
            this.mBufferingState = i;
            this.mBufferedPositionMs = j;
        }
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onBufferingStateChanged(MediaController2ImplBase.this.mInstance, mediaItem2, i);
                }
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void notifyPlaylistChanges(final List<MediaItem2> list, final MediaMetadata2 mediaMetadata2) {
        synchronized (this.mLock) {
            this.mPlaylist = list;
            this.mPlaylistMetadata = mediaMetadata2;
        }
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onPlaylistChanged(MediaController2ImplBase.this.mInstance, list, mediaMetadata2);
                }
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void notifyPlaylistMetadataChanges(final MediaMetadata2 mediaMetadata2) {
        synchronized (this.mLock) {
            this.mPlaylistMetadata = mediaMetadata2;
        }
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onPlaylistMetadataChanged(MediaController2ImplBase.this.mInstance, mediaMetadata2);
                }
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void notifyPlaybackInfoChanges(final PlaybackInfo playbackInfo) {
        synchronized (this.mLock) {
            this.mPlaybackInfo = playbackInfo;
        }
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onPlaybackInfoChanged(MediaController2ImplBase.this.mInstance, playbackInfo);
                }
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void notifyRepeatModeChanges(final int i) {
        synchronized (this.mLock) {
            this.mRepeatMode = i;
        }
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onRepeatModeChanged(MediaController2ImplBase.this.mInstance, i);
                }
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void notifyShuffleModeChanges(final int i) {
        synchronized (this.mLock) {
            this.mShuffleMode = i;
        }
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onShuffleModeChanged(MediaController2ImplBase.this.mInstance, i);
                }
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void notifySeekCompleted(long j, long j2, final long j3) {
        synchronized (this.mLock) {
            this.mPositionEventTimeMs = j;
            this.mPositionMs = j2;
        }
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onSeekCompleted(MediaController2ImplBase.this.mInstance, j3);
                }
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void notifyError(final int i, final Bundle bundle) {
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onError(MediaController2ImplBase.this.mInstance, i, bundle);
                }
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void notifyRoutesInfoChanged(final List<Bundle> list) {
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onRoutesInfoChanged(MediaController2ImplBase.this.mInstance, list);
                }
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void onConnectedNotLocked(IMediaSession2 iMediaSession2, final SessionCommandGroup2 sessionCommandGroup2, int i, MediaItem2 mediaItem2, long j, long j2, float f, long j3, PlaybackInfo playbackInfo, int i2, int i3, List<MediaItem2> list, PendingIntent pendingIntent) {
        IMediaSession2 iMediaSession22 = iMediaSession2;
        SessionCommandGroup2 sessionCommandGroup22 = sessionCommandGroup2;
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onConnectedNotLocked sessionBinder=");
            sb.append(iMediaSession2);
            sb.append(", allowedCommands=");
            sb.append(sessionCommandGroup2);
            Log.d("MC2ImplBase", sb.toString());
        }
        if (iMediaSession22 == null || sessionCommandGroup22 == null) {
            this.mInstance.close();
            return;
        }
        boolean z = false;
        try {
            synchronized (this.mLock) {
                try {
                    if (!this.mIsReleased) {
                        if (this.mISession2 != null) {
                            Log.e("MC2ImplBase", "Cannot be notified about the connection result many times. Probably a bug or malicious app.");
                            try {
                                this.mInstance.close();
                            } catch (Throwable th) {
                                th = th;
                                z = true;
                                throw th;
                            }
                        } else {
                            this.mAllowedCommands = sessionCommandGroup22;
                            this.mPlayerState = i;
                            this.mCurrentMediaItem = mediaItem2;
                            this.mPositionEventTimeMs = j;
                            this.mPositionMs = j2;
                            this.mPlaybackSpeed = f;
                            this.mBufferedPositionMs = j3;
                            this.mPlaybackInfo = playbackInfo;
                            this.mRepeatMode = i2;
                            this.mShuffleMode = i3;
                            this.mPlaylist = list;
                            this.mSessionActivity = pendingIntent;
                            this.mISession2 = iMediaSession22;
                            this.mISession2.asBinder().linkToDeath(this.mDeathRecipient, 0);
                            this.mCallbackExecutor.execute(new Runnable() {
                                public void run() {
                                    MediaController2ImplBase.this.mCallback.onConnected(MediaController2ImplBase.this.mInstance, sessionCommandGroup2);
                                }
                            });
                        }
                    }
                } catch (RemoteException e) {
                    if (DEBUG) {
                        Log.d("MC2ImplBase", "Session died too early.", e);
                    }
                    this.mInstance.close();
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        } catch (Throwable th3) {
            if (z) {
                this.mInstance.close();
            }
            throw th3;
        }
    }

    /* access modifiers changed from: 0000 */
    public void onCustomCommand(final SessionCommand2 sessionCommand2, final Bundle bundle, final ResultReceiver resultReceiver) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onCustomCommand cmd=");
            sb.append(sessionCommand2);
            Log.d("MC2ImplBase", sb.toString());
        }
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                MediaController2ImplBase.this.mCallback.onCustomCommand(MediaController2ImplBase.this.mInstance, sessionCommand2, bundle, resultReceiver);
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void onAllowedCommandsChanged(final SessionCommandGroup2 sessionCommandGroup2) {
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                MediaController2ImplBase.this.mCallback.onAllowedCommandsChanged(MediaController2ImplBase.this.mInstance, sessionCommandGroup2);
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public void onCustomLayoutChanged(final List<CommandButton> list) {
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                MediaController2ImplBase.this.mCallback.onCustomLayoutChanged(MediaController2ImplBase.this.mInstance, list);
            }
        });
    }
}
