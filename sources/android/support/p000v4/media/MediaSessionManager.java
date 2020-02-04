package android.support.p000v4.media;

import android.content.Context;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/* renamed from: android.support.v4.media.MediaSessionManager */
public final class MediaSessionManager {
    static final boolean DEBUG = Log.isLoggable("MediaSessionManager", 3);
    private static final Object sLock = new Object();
    private static volatile MediaSessionManager sSessionManager;
    MediaSessionManagerImpl mImpl;

    /* renamed from: android.support.v4.media.MediaSessionManager$MediaSessionManagerImpl */
    interface MediaSessionManagerImpl {
        Context getContext();

        boolean isTrustedForMediaControl(RemoteUserInfoImpl remoteUserInfoImpl);
    }

    /* renamed from: android.support.v4.media.MediaSessionManager$RemoteUserInfo */
    public static final class RemoteUserInfo {
        public static final String LEGACY_CONTROLLER = "android.media.session.MediaController";
        RemoteUserInfoImpl mImpl;

        public RemoteUserInfo(@NonNull String str, int i, int i2) {
            if (VERSION.SDK_INT >= 28) {
                this.mImpl = new RemoteUserInfo(str, i, i2);
            } else {
                this.mImpl = new RemoteUserInfo(str, i, i2);
            }
        }

        @NonNull
        public String getPackageName() {
            return this.mImpl.getPackageName();
        }

        public int getPid() {
            return this.mImpl.getPid();
        }

        public int getUid() {
            return this.mImpl.getUid();
        }

        public boolean equals(@Nullable Object obj) {
            return this.mImpl.equals(obj);
        }

        public int hashCode() {
            return this.mImpl.hashCode();
        }
    }

    /* renamed from: android.support.v4.media.MediaSessionManager$RemoteUserInfoImpl */
    interface RemoteUserInfoImpl {
        String getPackageName();

        int getPid();

        int getUid();
    }

    @NonNull
    public static MediaSessionManager getSessionManager(@NonNull Context context) {
        MediaSessionManager mediaSessionManager = sSessionManager;
        if (mediaSessionManager == null) {
            synchronized (sLock) {
                mediaSessionManager = sSessionManager;
                if (mediaSessionManager == null) {
                    sSessionManager = new MediaSessionManager(context.getApplicationContext());
                    mediaSessionManager = sSessionManager;
                }
            }
        }
        return mediaSessionManager;
    }

    private MediaSessionManager(Context context) {
        int i = VERSION.SDK_INT;
        if (i >= 28) {
            this.mImpl = new MediaSessionManagerImplApi28(context);
        } else if (i >= 21) {
            this.mImpl = new MediaSessionManagerImplApi21(context);
        } else {
            this.mImpl = new MediaSessionManagerImplBase(context);
        }
    }

    public boolean isTrustedForMediaControl(@NonNull RemoteUserInfo remoteUserInfo) {
        if (remoteUserInfo != null) {
            return this.mImpl.isTrustedForMediaControl(remoteUserInfo.mImpl);
        }
        throw new IllegalArgumentException("userInfo should not be null");
    }
}
