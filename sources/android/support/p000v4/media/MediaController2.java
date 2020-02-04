package android.support.p000v4.media;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.mediacompat.Rating2;
import android.support.p000v4.media.MediaSession2.CommandButton;
import android.text.TextUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.Executor;

@TargetApi(19)
/* renamed from: android.support.v4.media.MediaController2 */
public class MediaController2 implements AutoCloseable {
    private final SupportLibraryImpl mImpl;
    Long mTimeDiff;

    /* renamed from: android.support.v4.media.MediaController2$ControllerCallback */
    public static abstract class ControllerCallback {
        public void onAllowedCommandsChanged(@NonNull MediaController2 mediaController2, @NonNull SessionCommandGroup2 sessionCommandGroup2) {
        }

        public void onBufferingStateChanged(@NonNull MediaController2 mediaController2, @NonNull MediaItem2 mediaItem2, int i) {
        }

        public void onConnected(@NonNull MediaController2 mediaController2, @NonNull SessionCommandGroup2 sessionCommandGroup2) {
        }

        public void onCurrentMediaItemChanged(@NonNull MediaController2 mediaController2, @Nullable MediaItem2 mediaItem2) {
        }

        public void onCustomCommand(@NonNull MediaController2 mediaController2, @NonNull SessionCommand2 sessionCommand2, @Nullable Bundle bundle, @Nullable ResultReceiver resultReceiver) {
        }

        public void onCustomLayoutChanged(@NonNull MediaController2 mediaController2, @NonNull List<CommandButton> list) {
        }

        public void onDisconnected(@NonNull MediaController2 mediaController2) {
        }

        public void onError(@NonNull MediaController2 mediaController2, int i, @Nullable Bundle bundle) {
        }

        public void onPlaybackInfoChanged(@NonNull MediaController2 mediaController2, @NonNull PlaybackInfo playbackInfo) {
        }

        public void onPlaybackSpeedChanged(@NonNull MediaController2 mediaController2, float f) {
        }

        public void onPlayerStateChanged(@NonNull MediaController2 mediaController2, int i) {
        }

        public void onPlaylistChanged(@NonNull MediaController2 mediaController2, @NonNull List<MediaItem2> list, @Nullable MediaMetadata2 mediaMetadata2) {
        }

        public void onPlaylistMetadataChanged(@NonNull MediaController2 mediaController2, @Nullable MediaMetadata2 mediaMetadata2) {
        }

        public void onRepeatModeChanged(@NonNull MediaController2 mediaController2, int i) {
        }

        public void onRoutesInfoChanged(@NonNull MediaController2 mediaController2, @Nullable List<Bundle> list) {
        }

        public void onSeekCompleted(@NonNull MediaController2 mediaController2, long j) {
        }

        public void onShuffleModeChanged(@NonNull MediaController2 mediaController2, int i) {
        }
    }

    /* renamed from: android.support.v4.media.MediaController2$PlaybackInfo */
    public static final class PlaybackInfo {
        private static final String KEY_AUDIO_ATTRIBUTES = "android.media.audio_info.audio_attrs";
        private static final String KEY_CONTROL_TYPE = "android.media.audio_info.control_type";
        private static final String KEY_CURRENT_VOLUME = "android.media.audio_info.current_volume";
        private static final String KEY_MAX_VOLUME = "android.media.audio_info.max_volume";
        private static final String KEY_PLAYBACK_TYPE = "android.media.audio_info.playback_type";
        public static final int PLAYBACK_TYPE_LOCAL = 1;
        public static final int PLAYBACK_TYPE_REMOTE = 2;
        private final AudioAttributesCompat mAudioAttrsCompat;
        private final int mControlType;
        private final int mCurrentVolume;
        private final int mMaxVolume;
        private final int mPlaybackType;

        PlaybackInfo(int i, AudioAttributesCompat audioAttributesCompat, int i2, int i3, int i4) {
            this.mPlaybackType = i;
            this.mAudioAttrsCompat = audioAttributesCompat;
            this.mControlType = i2;
            this.mMaxVolume = i3;
            this.mCurrentVolume = i4;
        }

        public int getPlaybackType() {
            return this.mPlaybackType;
        }

        public AudioAttributesCompat getAudioAttributes() {
            return this.mAudioAttrsCompat;
        }

        public int getControlType() {
            return this.mControlType;
        }

        public int getMaxVolume() {
            return this.mMaxVolume;
        }

        public int getCurrentVolume() {
            return this.mCurrentVolume;
        }

        /* access modifiers changed from: 0000 */
        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_PLAYBACK_TYPE, this.mPlaybackType);
            bundle.putInt(KEY_CONTROL_TYPE, this.mControlType);
            bundle.putInt(KEY_MAX_VOLUME, this.mMaxVolume);
            bundle.putInt(KEY_CURRENT_VOLUME, this.mCurrentVolume);
            AudioAttributesCompat audioAttributesCompat = this.mAudioAttrsCompat;
            if (audioAttributesCompat != null) {
                bundle.putBundle(KEY_AUDIO_ATTRIBUTES, audioAttributesCompat.toBundle());
            }
            return bundle;
        }

        static PlaybackInfo createPlaybackInfo(int i, AudioAttributesCompat audioAttributesCompat, int i2, int i3, int i4) {
            PlaybackInfo playbackInfo = new PlaybackInfo(i, audioAttributesCompat, i2, i3, i4);
            return playbackInfo;
        }

        static PlaybackInfo fromBundle(Bundle bundle) {
            if (bundle == null) {
                return null;
            }
            return createPlaybackInfo(bundle.getInt(KEY_PLAYBACK_TYPE), AudioAttributesCompat.fromBundle(bundle.getBundle(KEY_AUDIO_ATTRIBUTES)), bundle.getInt(KEY_CONTROL_TYPE), bundle.getInt(KEY_MAX_VOLUME), bundle.getInt(KEY_CURRENT_VOLUME));
        }
    }

    /* renamed from: android.support.v4.media.MediaController2$SupportLibraryImpl */
    interface SupportLibraryImpl extends AutoCloseable {
        void addPlaylistItem(int i, @NonNull MediaItem2 mediaItem2);

        void adjustVolume(int i, int i2);

        void fastForward();

        @Nullable
        MediaBrowserCompat getBrowserCompat();

        long getBufferedPosition();

        int getBufferingState();

        @NonNull
        ControllerCallback getCallback();

        @NonNull
        Executor getCallbackExecutor();

        @NonNull
        Context getContext();

        MediaItem2 getCurrentMediaItem();

        long getCurrentPosition();

        long getDuration();

        @NonNull
        MediaController2 getInstance();

        @Nullable
        PlaybackInfo getPlaybackInfo();

        float getPlaybackSpeed();

        int getPlayerState();

        @Nullable
        List<MediaItem2> getPlaylist();

        @Nullable
        MediaMetadata2 getPlaylistMetadata();

        int getRepeatMode();

        @Nullable
        PendingIntent getSessionActivity();

        SessionToken2 getSessionToken();

        int getShuffleMode();

        boolean isConnected();

        void pause();

        void play();

        void playFromMediaId(@NonNull String str, @Nullable Bundle bundle);

        void playFromSearch(@NonNull String str, @Nullable Bundle bundle);

        void playFromUri(@NonNull Uri uri, @Nullable Bundle bundle);

        void prepare();

        void prepareFromMediaId(@NonNull String str, @Nullable Bundle bundle);

        void prepareFromSearch(@NonNull String str, @Nullable Bundle bundle);

        void prepareFromUri(@NonNull Uri uri, @Nullable Bundle bundle);

        void removePlaylistItem(@NonNull MediaItem2 mediaItem2);

        void replacePlaylistItem(int i, @NonNull MediaItem2 mediaItem2);

        void reset();

        void rewind();

        void seekTo(long j);

        void selectRoute(@NonNull Bundle bundle);

        void sendCustomCommand(@NonNull SessionCommand2 sessionCommand2, @Nullable Bundle bundle, @Nullable ResultReceiver resultReceiver);

        void setPlaybackSpeed(float f);

        void setPlaylist(@NonNull List<MediaItem2> list, @Nullable MediaMetadata2 mediaMetadata2);

        void setRating(@NonNull String str, @NonNull Rating2 rating2);

        void setRepeatMode(int i);

        void setShuffleMode(int i);

        void setVolumeTo(int i, int i2);

        void skipBackward();

        void skipForward();

        void skipToNextItem();

        void skipToPlaylistItem(@NonNull MediaItem2 mediaItem2);

        void skipToPreviousItem();

        void subscribeRoutesInfo();

        void unsubscribeRoutesInfo();

        void updatePlaylistMetadata(@Nullable MediaMetadata2 mediaMetadata2);
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    /* renamed from: android.support.v4.media.MediaController2$VolumeDirection */
    public @interface VolumeDirection {
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    /* renamed from: android.support.v4.media.MediaController2$VolumeFlags */
    public @interface VolumeFlags {
    }

    public MediaController2(@NonNull Context context, @NonNull SessionToken2 sessionToken2, @NonNull Executor executor, @NonNull ControllerCallback controllerCallback) {
        if (context == null) {
            throw new IllegalArgumentException("context shouldn't be null");
        } else if (sessionToken2 == null) {
            throw new IllegalArgumentException("token shouldn't be null");
        } else if (controllerCallback == null) {
            throw new IllegalArgumentException("callback shouldn't be null");
        } else if (executor != null) {
            this.mImpl = createImpl(context, sessionToken2, executor, controllerCallback);
        } else {
            throw new IllegalArgumentException("executor shouldn't be null");
        }
    }

    /* access modifiers changed from: 0000 */
    public SupportLibraryImpl createImpl(@NonNull Context context, @NonNull SessionToken2 sessionToken2, @NonNull Executor executor, @NonNull ControllerCallback controllerCallback) {
        if (sessionToken2.isLegacySession()) {
            MediaController2ImplLegacy mediaController2ImplLegacy = new MediaController2ImplLegacy(context, this, sessionToken2, executor, controllerCallback);
            return mediaController2ImplLegacy;
        }
        MediaController2ImplBase mediaController2ImplBase = new MediaController2ImplBase(context, this, sessionToken2, executor, controllerCallback);
        return mediaController2ImplBase;
    }

    /* access modifiers changed from: 0000 */
    public SupportLibraryImpl getImpl() {
        return this.mImpl;
    }

    public void close() {
        try {
            this.mImpl.close();
        } catch (Exception unused) {
        }
    }

    @NonNull
    public SessionToken2 getSessionToken() {
        return this.mImpl.getSessionToken();
    }

    public boolean isConnected() {
        return this.mImpl.isConnected();
    }

    public void play() {
        this.mImpl.play();
    }

    public void pause() {
        this.mImpl.pause();
    }

    public void reset() {
        this.mImpl.reset();
    }

    public void prepare() {
        this.mImpl.prepare();
    }

    public void fastForward() {
        this.mImpl.fastForward();
    }

    public void rewind() {
        this.mImpl.rewind();
    }

    public void seekTo(long j) {
        this.mImpl.seekTo(j);
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public void skipForward() {
        this.mImpl.skipForward();
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public void skipBackward() {
        this.mImpl.skipBackward();
    }

    public void playFromMediaId(@NonNull String str, @Nullable Bundle bundle) {
        if (str != null) {
            this.mImpl.playFromMediaId(str, bundle);
            return;
        }
        throw new IllegalArgumentException("mediaId shouldn't be null");
    }

    public void playFromSearch(@NonNull String str, @Nullable Bundle bundle) {
        if (!TextUtils.isEmpty(str)) {
            this.mImpl.playFromSearch(str, bundle);
            return;
        }
        throw new IllegalArgumentException("query shouldn't be empty");
    }

    public void playFromUri(@NonNull Uri uri, @Nullable Bundle bundle) {
        if (uri != null) {
            this.mImpl.playFromUri(uri, bundle);
            return;
        }
        throw new IllegalArgumentException("uri shouldn't be null");
    }

    public void prepareFromMediaId(@NonNull String str, @Nullable Bundle bundle) {
        if (str != null) {
            this.mImpl.prepareFromMediaId(str, bundle);
            return;
        }
        throw new IllegalArgumentException("mediaId shouldn't be null");
    }

    public void prepareFromSearch(@NonNull String str, @Nullable Bundle bundle) {
        if (!TextUtils.isEmpty(str)) {
            this.mImpl.prepareFromSearch(str, bundle);
            return;
        }
        throw new IllegalArgumentException("query shouldn't be empty");
    }

    public void prepareFromUri(@NonNull Uri uri, @Nullable Bundle bundle) {
        if (uri != null) {
            this.mImpl.prepareFromUri(uri, bundle);
            return;
        }
        throw new IllegalArgumentException("uri shouldn't be null");
    }

    public void setVolumeTo(int i, int i2) {
        this.mImpl.setVolumeTo(i, i2);
    }

    public void adjustVolume(int i, int i2) {
        this.mImpl.adjustVolume(i, i2);
    }

    @Nullable
    public PendingIntent getSessionActivity() {
        return this.mImpl.getSessionActivity();
    }

    public int getPlayerState() {
        return this.mImpl.getPlayerState();
    }

    public long getDuration() {
        return this.mImpl.getDuration();
    }

    public long getCurrentPosition() {
        return this.mImpl.getCurrentPosition();
    }

    public float getPlaybackSpeed() {
        return this.mImpl.getPlaybackSpeed();
    }

    public void setPlaybackSpeed(float f) {
        this.mImpl.setPlaybackSpeed(f);
    }

    public int getBufferingState() {
        return this.mImpl.getBufferingState();
    }

    public long getBufferedPosition() {
        return this.mImpl.getBufferedPosition();
    }

    @Nullable
    public PlaybackInfo getPlaybackInfo() {
        return this.mImpl.getPlaybackInfo();
    }

    public void setRating(@NonNull String str, @NonNull Rating2 rating2) {
        if (str == null) {
            throw new IllegalArgumentException("mediaId shouldn't be null");
        } else if (rating2 != null) {
            this.mImpl.setRating(str, rating2);
        } else {
            throw new IllegalArgumentException("rating shouldn't be null");
        }
    }

    public void sendCustomCommand(@NonNull SessionCommand2 sessionCommand2, @Nullable Bundle bundle, @Nullable ResultReceiver resultReceiver) {
        if (sessionCommand2 != null) {
            this.mImpl.sendCustomCommand(sessionCommand2, bundle, resultReceiver);
            return;
        }
        throw new IllegalArgumentException("command shouldn't be null");
    }

    @Nullable
    public List<MediaItem2> getPlaylist() {
        return this.mImpl.getPlaylist();
    }

    public void setPlaylist(@NonNull List<MediaItem2> list, @Nullable MediaMetadata2 mediaMetadata2) {
        if (list != null) {
            this.mImpl.setPlaylist(list, mediaMetadata2);
            return;
        }
        throw new IllegalArgumentException("list shouldn't be null");
    }

    public void updatePlaylistMetadata(@Nullable MediaMetadata2 mediaMetadata2) {
        this.mImpl.updatePlaylistMetadata(mediaMetadata2);
    }

    @Nullable
    public MediaMetadata2 getPlaylistMetadata() {
        return this.mImpl.getPlaylistMetadata();
    }

    public void addPlaylistItem(int i, @NonNull MediaItem2 mediaItem2) {
        if (i < 0) {
            throw new IllegalArgumentException("index shouldn't be negative");
        } else if (mediaItem2 != null) {
            this.mImpl.addPlaylistItem(i, mediaItem2);
        } else {
            throw new IllegalArgumentException("item shouldn't be null");
        }
    }

    public void removePlaylistItem(@NonNull MediaItem2 mediaItem2) {
        if (mediaItem2 != null) {
            this.mImpl.removePlaylistItem(mediaItem2);
            return;
        }
        throw new IllegalArgumentException("item shouldn't be null");
    }

    public void replacePlaylistItem(int i, @NonNull MediaItem2 mediaItem2) {
        if (i < 0) {
            throw new IllegalArgumentException("index shouldn't be negative");
        } else if (mediaItem2 != null) {
            this.mImpl.replacePlaylistItem(i, mediaItem2);
        } else {
            throw new IllegalArgumentException("item shouldn't be null");
        }
    }

    public MediaItem2 getCurrentMediaItem() {
        return this.mImpl.getCurrentMediaItem();
    }

    public void skipToPreviousItem() {
        this.mImpl.skipToPreviousItem();
    }

    public void skipToNextItem() {
        this.mImpl.skipToNextItem();
    }

    public void skipToPlaylistItem(@NonNull MediaItem2 mediaItem2) {
        if (mediaItem2 != null) {
            this.mImpl.skipToPlaylistItem(mediaItem2);
            return;
        }
        throw new IllegalArgumentException("item shouldn't be null");
    }

    public int getRepeatMode() {
        return this.mImpl.getRepeatMode();
    }

    public void setRepeatMode(int i) {
        this.mImpl.setRepeatMode(i);
    }

    public int getShuffleMode() {
        return this.mImpl.getShuffleMode();
    }

    public void setShuffleMode(int i) {
        this.mImpl.setShuffleMode(i);
    }

    public void subscribeRoutesInfo() {
        this.mImpl.subscribeRoutesInfo();
    }

    public void unsubscribeRoutesInfo() {
        this.mImpl.unsubscribeRoutesInfo();
    }

    public void selectRoute(@NonNull Bundle bundle) {
        if (bundle != null) {
            this.mImpl.selectRoute(bundle);
            return;
        }
        throw new IllegalArgumentException("route shouldn't be null");
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public void setTimeDiff(Long l) {
        this.mTimeDiff = l;
    }

    /* access modifiers changed from: 0000 */
    @NonNull
    public ControllerCallback getCallback() {
        return this.mImpl.getCallback();
    }

    /* access modifiers changed from: 0000 */
    @NonNull
    public Executor getCallbackExecutor() {
        return this.mImpl.getCallbackExecutor();
    }
}
