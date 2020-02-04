package android.support.p000v4.media;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.p000v4.media.MediaBrowserCompat.MediaItem;
import android.support.p000v4.media.MediaBrowserServiceCompat.BrowserRoot;
import android.support.p000v4.media.MediaDescriptionCompat.Builder;
import android.support.p000v4.media.MediaSession2.CommandButton;
import java.util.ArrayList;
import java.util.List;

/* renamed from: android.support.v4.media.MediaUtils2 */
class MediaUtils2 {
    static final BrowserRoot sDefaultBrowserRoot = new BrowserRoot(MediaLibraryService2.SERVICE_INTERFACE, null);

    static int convertToPlaybackStateCompatState(int i, int i2) {
        if (i == 0) {
            return 0;
        }
        if (i == 1) {
            return 2;
        }
        if (i == 2) {
            return i2 != 2 ? 3 : 6;
        }
        if (i != 3) {
        }
        return 7;
    }

    private MediaUtils2() {
    }

    static MediaItem convertToMediaItem(MediaItem2 mediaItem2) {
        MediaDescriptionCompat mediaDescriptionCompat;
        if (mediaItem2 == null) {
            return null;
        }
        MediaMetadata2 metadata = mediaItem2.getMetadata();
        if (metadata == null) {
            mediaDescriptionCompat = new Builder().setMediaId(mediaItem2.getMediaId()).build();
        } else {
            Builder extras = new Builder().setMediaId(mediaItem2.getMediaId()).setSubtitle(metadata.getText("android.media.metadata.DISPLAY_SUBTITLE")).setDescription(metadata.getText("android.media.metadata.DISPLAY_DESCRIPTION")).setIconBitmap(metadata.getBitmap("android.media.metadata.DISPLAY_ICON")).setExtras(metadata.getExtras());
            String string = metadata.getString("android.media.metadata.TITLE");
            if (string != null) {
                extras.setTitle(string);
            } else {
                extras.setTitle(metadata.getString("android.media.metadata.DISPLAY_TITLE"));
            }
            String string2 = metadata.getString("android.media.metadata.DISPLAY_ICON_URI");
            if (string2 != null) {
                extras.setIconUri(Uri.parse(string2));
            }
            String string3 = metadata.getString("android.media.metadata.MEDIA_URI");
            if (string3 != null) {
                extras.setMediaUri(Uri.parse(string3));
            }
            mediaDescriptionCompat = extras.build();
        }
        return new MediaItem(mediaDescriptionCompat, mediaItem2.getFlags());
    }

    static List<MediaItem> convertToMediaItemList(List<MediaItem2> list) {
        if (list == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            arrayList.add(convertToMediaItem((MediaItem2) list.get(i)));
        }
        return arrayList;
    }

    static MediaItem2 convertToMediaItem2(MediaItem mediaItem) {
        if (mediaItem == null || mediaItem.getMediaId() == null) {
            return null;
        }
        return new MediaItem2.Builder(mediaItem.getFlags()).setMediaId(mediaItem.getMediaId()).setMetadata(convertToMediaMetadata2(mediaItem.getDescription())).build();
    }

    static List<MediaItem2> convertToMediaItem2List(Parcelable[] parcelableArr) {
        ArrayList arrayList = new ArrayList();
        if (parcelableArr != null) {
            for (int i = 0; i < parcelableArr.length; i++) {
                if (parcelableArr[i] instanceof Bundle) {
                    MediaItem2 fromBundle = MediaItem2.fromBundle(parcelableArr[i]);
                    if (fromBundle != null) {
                        arrayList.add(fromBundle);
                    }
                }
            }
        }
        return arrayList;
    }

    static List<MediaItem2> convertMediaItemListToMediaItem2List(List<MediaItem> list) {
        if (list == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            arrayList.add(convertToMediaItem2((MediaItem) list.get(i)));
        }
        return arrayList;
    }

    static List<MediaItem2> convertBundleListToMediaItem2List(List<Bundle> list) {
        if (list == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            Bundle bundle = (Bundle) list.get(i);
            if (bundle != null) {
                arrayList.add(MediaItem2.fromBundle(bundle));
            }
        }
        return arrayList;
    }

    static MediaMetadata2 convertToMediaMetadata2(MediaDescriptionCompat mediaDescriptionCompat) {
        if (mediaDescriptionCompat == null) {
            return null;
        }
        MediaMetadata2.Builder builder = new MediaMetadata2.Builder();
        builder.putString("android.media.metadata.MEDIA_ID", mediaDescriptionCompat.getMediaId());
        CharSequence title = mediaDescriptionCompat.getTitle();
        if (title != null) {
            builder.putText("android.media.metadata.DISPLAY_TITLE", title);
        }
        if (mediaDescriptionCompat.getDescription() != null) {
            builder.putText("android.media.metadata.DISPLAY_DESCRIPTION", mediaDescriptionCompat.getDescription());
        }
        CharSequence subtitle = mediaDescriptionCompat.getSubtitle();
        if (subtitle != null) {
            builder.putText("android.media.metadata.DISPLAY_SUBTITLE", subtitle);
        }
        Bitmap iconBitmap = mediaDescriptionCompat.getIconBitmap();
        if (iconBitmap != null) {
            builder.putBitmap("android.media.metadata.DISPLAY_ICON", iconBitmap);
        }
        Uri iconUri = mediaDescriptionCompat.getIconUri();
        if (iconUri != null) {
            builder.putText("android.media.metadata.DISPLAY_ICON_URI", iconUri.toString());
        }
        if (mediaDescriptionCompat.getExtras() != null) {
            builder.setExtras(mediaDescriptionCompat.getExtras());
        }
        Uri mediaUri = mediaDescriptionCompat.getMediaUri();
        if (mediaUri != null) {
            builder.putText("android.media.metadata.MEDIA_URI", mediaUri.toString());
        }
        return builder.build();
    }

    static List<Bundle> convertToBundleList(Parcelable[] parcelableArr) {
        if (parcelableArr == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        int length = parcelableArr.length;
        for (int i = 0; i < length; i++) {
            arrayList.add(parcelableArr[i]);
        }
        return arrayList;
    }

    static List<Bundle> convertMediaItem2ListToBundleList(List<MediaItem2> list) {
        if (list == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            MediaItem2 mediaItem2 = (MediaItem2) list.get(i);
            if (mediaItem2 != null) {
                Bundle bundle = mediaItem2.toBundle();
                if (bundle != null) {
                    arrayList.add(bundle);
                }
            }
        }
        return arrayList;
    }

    static List<Bundle> convertCommandButtonListToBundleList(List<CommandButton> list) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            Bundle bundle = ((CommandButton) list.get(i)).toBundle();
            if (bundle != null) {
                arrayList.add(bundle);
            }
        }
        return arrayList;
    }

    static Parcelable[] convertMediaItem2ListToParcelableArray(List<MediaItem2> list) {
        if (list == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            MediaItem2 mediaItem2 = (MediaItem2) list.get(i);
            if (mediaItem2 != null) {
                Bundle bundle = mediaItem2.toBundle();
                if (bundle != null) {
                    arrayList.add(bundle);
                }
            }
        }
        return (Parcelable[]) arrayList.toArray(new Parcelable[0]);
    }

    static Parcelable[] convertCommandButtonListToParcelableArray(List<CommandButton> list) {
        if (list == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            Bundle bundle = ((CommandButton) list.get(i)).toBundle();
            if (bundle != null) {
                arrayList.add(bundle);
            }
        }
        return (Parcelable[]) arrayList.toArray(new Parcelable[0]);
    }

    static List<CommandButton> convertToCommandButtonList(Parcelable[] parcelableArr) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < parcelableArr.length; i++) {
            if (parcelableArr[i] instanceof Bundle) {
                CommandButton fromBundle = CommandButton.fromBundle(parcelableArr[i]);
                if (fromBundle != null) {
                    arrayList.add(fromBundle);
                }
            }
        }
        return arrayList;
    }

    static boolean isDefaultLibraryRootHint(Bundle bundle) {
        return bundle != null && bundle.getBoolean("android.support.v4.media.root_default_root", false);
    }

    static Bundle createBundle(Bundle bundle) {
        return bundle == null ? new Bundle() : new Bundle(bundle);
    }
}
