package android.support.p000v4.media.subtitle;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;

@RestrictTo({Scope.LIBRARY_GROUP})
/* renamed from: android.support.v4.media.subtitle.MediaTimeProvider */
public interface MediaTimeProvider {
    public static final long NO_TIME = -1;

    /* renamed from: android.support.v4.media.subtitle.MediaTimeProvider$OnMediaTimeListener */
    public interface OnMediaTimeListener {
        void onSeek(long j);

        void onStop();

        void onTimedEvent(long j);
    }

    void cancelNotifications(OnMediaTimeListener onMediaTimeListener);

    long getCurrentTimeUs(boolean z, boolean z2) throws IllegalStateException;

    void notifyAt(long j, OnMediaTimeListener onMediaTimeListener);

    void scheduleUpdate(OnMediaTimeListener onMediaTimeListener);
}