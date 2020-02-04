package android.support.p000v4.media;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.p000v4.media.MediaSession2.OnDataSourceMissingHelper;
import java.util.List;

/* renamed from: android.support.v4.media.MediaInterface2 */
class MediaInterface2 {

    /* renamed from: android.support.v4.media.MediaInterface2$SessionPlaybackControl */
    interface SessionPlaybackControl {
        long getBufferedPosition();

        int getBufferingState();

        long getCurrentPosition();

        long getDuration();

        float getPlaybackSpeed();

        int getPlayerState();

        void pause();

        void play();

        void prepare();

        void reset();

        void seekTo(long j);

        void setPlaybackSpeed(float f);
    }

    /* renamed from: android.support.v4.media.MediaInterface2$SessionPlayer */
    interface SessionPlayer extends SessionPlaybackControl, SessionPlaylistControl {
        void notifyError(int i, @Nullable Bundle bundle);

        void skipBackward();

        void skipForward();
    }

    /* renamed from: android.support.v4.media.MediaInterface2$SessionPlaylistControl */
    interface SessionPlaylistControl {
        void addPlaylistItem(int i, MediaItem2 mediaItem2);

        void clearOnDataSourceMissingHelper();

        MediaItem2 getCurrentMediaItem();

        List<MediaItem2> getPlaylist();

        MediaMetadata2 getPlaylistMetadata();

        int getRepeatMode();

        int getShuffleMode();

        void removePlaylistItem(MediaItem2 mediaItem2);

        void replacePlaylistItem(int i, MediaItem2 mediaItem2);

        void setOnDataSourceMissingHelper(OnDataSourceMissingHelper onDataSourceMissingHelper);

        void setPlaylist(List<MediaItem2> list, MediaMetadata2 mediaMetadata2);

        void setRepeatMode(int i);

        void setShuffleMode(int i);

        void skipToNextItem();

        void skipToPlaylistItem(MediaItem2 mediaItem2);

        void skipToPreviousItem();

        void updatePlaylistMetadata(MediaMetadata2 mediaMetadata2);
    }

    private MediaInterface2() {
    }
}
