package android.support.p000v4.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.p000v4.media.session.MediaControllerCompat;
import android.support.p000v4.media.session.MediaControllerCompat.Callback;
import android.support.p000v4.media.session.MediaSessionCompat.Token;
import android.text.TextUtils;
import android.util.Log;
import com.asus.scenedetectlib.BuildConfig;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.Executor;

/* renamed from: android.support.v4.media.SessionToken2 */
public final class SessionToken2 {
    private static final String TAG = "SessionToken2";
    public static final int TYPE_LIBRARY_SERVICE = 2;
    public static final int TYPE_SESSION = 0;
    public static final int TYPE_SESSION_SERVICE = 1;
    private static final long WAIT_TIME_MS_FOR_SESSION_READY = 300;
    private final SupportLibraryImpl mImpl;

    @RestrictTo({Scope.LIBRARY_GROUP})
    /* renamed from: android.support.v4.media.SessionToken2$OnSessionToken2CreatedListener */
    public interface OnSessionToken2CreatedListener {
        void onSessionToken2Created(Token token, SessionToken2 sessionToken2);
    }

    /* renamed from: android.support.v4.media.SessionToken2$SupportLibraryImpl */
    interface SupportLibraryImpl {
        Object getBinder();

        @Nullable
        ComponentName getComponentName();

        @NonNull
        String getPackageName();

        @Nullable
        String getServiceName();

        String getSessionId();

        int getType();

        int getUid();

        Bundle toBundle();
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    /* renamed from: android.support.v4.media.SessionToken2$TokenType */
    public @interface TokenType {
    }

    public SessionToken2(@NonNull Context context, @NonNull ComponentName componentName) {
        this.mImpl = new SessionToken2ImplBase(context, componentName);
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    SessionToken2(SupportLibraryImpl supportLibraryImpl) {
        this.mImpl = supportLibraryImpl;
    }

    public int hashCode() {
        return this.mImpl.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SessionToken2)) {
            return false;
        }
        return this.mImpl.equals(((SessionToken2) obj).mImpl);
    }

    public String toString() {
        return this.mImpl.toString();
    }

    public int getUid() {
        return this.mImpl.getUid();
    }

    @NonNull
    public String getPackageName() {
        return this.mImpl.getPackageName();
    }

    @Nullable
    public String getServiceName() {
        return this.mImpl.getServiceName();
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public ComponentName getComponentName() {
        return this.mImpl.getComponentName();
    }

    public String getId() {
        return this.mImpl.getSessionId();
    }

    public int getType() {
        return this.mImpl.getType();
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public boolean isLegacySession() {
        return this.mImpl instanceof SessionToken2ImplLegacy;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public Object getBinder() {
        return this.mImpl.getBinder();
    }

    public Bundle toBundle() {
        return this.mImpl.toBundle();
    }

    public static SessionToken2 fromBundle(@NonNull Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        if (bundle.getInt("android.media.token.type", -1) == 100) {
            return new SessionToken2(SessionToken2ImplLegacy.fromBundle(bundle));
        }
        return new SessionToken2(SessionToken2ImplBase.fromBundle(bundle));
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public static void createSessionToken2(@NonNull final Context context, @NonNull final Token token, @NonNull Executor executor, @NonNull final OnSessionToken2CreatedListener onSessionToken2CreatedListener) {
        if (context == null) {
            throw new IllegalArgumentException("context shouldn't be null");
        } else if (token == null) {
            throw new IllegalArgumentException("token shouldn't be null");
        } else if (executor == null) {
            throw new IllegalArgumentException("executor shouldn't be null");
        } else if (onSessionToken2CreatedListener != null) {
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        final MediaControllerCompat mediaControllerCompat = new MediaControllerCompat(context, token);
                        mediaControllerCompat.registerCallback(new Callback() {
                            public void onSessionReady() {
                                synchronized (onSessionToken2CreatedListener) {
                                    onSessionToken2CreatedListener.onSessionToken2Created(token, mediaControllerCompat.getSessionToken2());
                                    onSessionToken2CreatedListener.notify();
                                }
                            }
                        });
                        if (mediaControllerCompat.isSessionReady()) {
                            onSessionToken2CreatedListener.onSessionToken2Created(token, mediaControllerCompat.getSessionToken2());
                        }
                        synchronized (onSessionToken2CreatedListener) {
                            onSessionToken2CreatedListener.wait(SessionToken2.WAIT_TIME_MS_FOR_SESSION_READY);
                            if (!mediaControllerCompat.isSessionReady()) {
                                SessionToken2 sessionToken2 = new SessionToken2(new SessionToken2ImplLegacy(token));
                                token.setSessionToken2(sessionToken2);
                                onSessionToken2CreatedListener.onSessionToken2Created(token, sessionToken2);
                            }
                        }
                    } catch (RemoteException e) {
                        Log.e(SessionToken2.TAG, "Failed to create session token2.", e);
                    } catch (InterruptedException e2) {
                        Log.e(SessionToken2.TAG, "Failed to create session token2.", e2);
                    }
                }
            });
        } else {
            throw new IllegalArgumentException("listener shouldn't be null");
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public static String getSessionId(ResolveInfo resolveInfo) {
        if (resolveInfo != null) {
            ServiceInfo serviceInfo = resolveInfo.serviceInfo;
            if (serviceInfo != null) {
                Bundle bundle = serviceInfo.metaData;
                String str = BuildConfig.FLAVOR;
                if (bundle == null) {
                    return str;
                }
                return resolveInfo.serviceInfo.metaData.getString(MediaSessionService2.SERVICE_META_DATA, str);
            }
        }
        return null;
    }

    private static String getSessionIdFromService(PackageManager packageManager, String str, ComponentName componentName) {
        Intent intent = new Intent(str);
        intent.setPackage(componentName.getPackageName());
        List queryIntentServices = packageManager.queryIntentServices(intent, 128);
        if (queryIntentServices != null) {
            for (int i = 0; i < queryIntentServices.size(); i++) {
                ResolveInfo resolveInfo = (ResolveInfo) queryIntentServices.get(i);
                if (resolveInfo != null) {
                    ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                    if (serviceInfo != null && TextUtils.equals(serviceInfo.name, componentName.getClassName())) {
                        return getSessionId(resolveInfo);
                    }
                }
            }
        }
        return null;
    }
}
