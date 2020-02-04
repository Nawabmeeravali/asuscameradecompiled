package com.android.camera.data;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import com.android.camera.p004ui.FilmStripView.DataAdapter;

public interface LocalDataAdapter extends DataAdapter {
    void addNewPhoto(ContentResolver contentResolver, Uri uri);

    void addNewVideo(ContentResolver contentResolver, Uri uri);

    boolean executeDeletion(Context context);

    int findDataByContentUri(Uri uri);

    void flush();

    LocalData getLocalData(int i);

    void insertData(LocalData localData);

    void refresh(ContentResolver contentResolver, Uri uri);

    void removeData(Context context, int i);

    void requestLoad(ContentResolver contentResolver);

    boolean undoDataRemoval();

    void updateData(int i, LocalData localData);
}
