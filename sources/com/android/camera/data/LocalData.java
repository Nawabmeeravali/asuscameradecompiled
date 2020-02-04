package com.android.camera.data;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.android.camera.p004ui.FilmStripView.ImageData;
import java.util.Comparator;

public interface LocalData extends ImageData {
    public static final int ACTION_DELETE = 2;
    public static final int ACTION_NONE = 0;
    public static final int ACTION_PLAY = 1;
    public static final int LOCAL_360_PHOTO_SPHERE = 6;
    public static final int LOCAL_CAMERA_PREVIEW = 1;
    public static final int LOCAL_IMAGE = 3;
    public static final int LOCAL_IN_PROGRESS_DATA = 7;
    public static final int LOCAL_PHOTO_SPHERE = 5;
    public static final int LOCAL_VIDEO = 4;
    public static final int LOCAL_VIEW = 2;
    public static final String MIME_TYPE_JPEG = "image/jpeg";
    public static final String TAG = "CAM_LocalData";

    public static class NewestFirstComparator implements Comparator<LocalData> {
        private static int compareDate(long j, long j2) {
            int i = -1;
            if (j < 0 || j2 < 0) {
                int i2 = (j2 > j ? 1 : (j2 == j ? 0 : -1));
                if (i2 < 0) {
                    i = 1;
                } else if (i2 <= 0) {
                    i = 0;
                }
                return i;
            }
            int i3 = (j > j2 ? 1 : (j == j2 ? 0 : -1));
            if (i3 < 0) {
                i = 1;
            } else if (i3 <= 0) {
                i = 0;
            }
            return i;
        }

        public int compare(LocalData localData, LocalData localData2) {
            int compareDate = compareDate(localData.getDateTaken(), localData2.getDateTaken());
            if (compareDate == 0) {
                compareDate = compareDate(localData.getDateModified(), localData2.getDateModified());
            }
            return compareDate == 0 ? localData.getTitle().compareTo(localData2.getTitle()) : compareDate;
        }
    }

    boolean canSwipeInFullScreen();

    boolean delete(Context context);

    long getContentId();

    long getDateModified();

    long getDateTaken();

    int getLocalDataType();

    MediaDetails getMediaDetails(Context context);

    String getMimeType();

    String getPath();

    long getSizeInBytes();

    String getTitle();

    View getView(Activity activity, int i, int i2, Drawable drawable, LocalDataAdapter localDataAdapter);

    boolean isDataActionSupported(int i);

    void onFullScreen(boolean z);

    LocalData refresh(ContentResolver contentResolver);

    boolean rotate90Degrees(Context context, LocalDataAdapter localDataAdapter, int i, boolean z);
}
