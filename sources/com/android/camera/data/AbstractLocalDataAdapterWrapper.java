package com.android.camera.data;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import com.android.camera.p004ui.FilmStripView.DataAdapter.Listener;

public abstract class AbstractLocalDataAdapterWrapper implements LocalDataAdapter {
    protected final LocalDataAdapter mAdapter;
    protected int mSuggestedHeight;
    protected int mSuggestedWidth;

    AbstractLocalDataAdapterWrapper(LocalDataAdapter localDataAdapter) {
        if (localDataAdapter != null) {
            this.mAdapter = localDataAdapter;
            return;
        }
        throw new AssertionError("data adapter is null");
    }

    public void suggestViewSizeBound(int i, int i2) {
        this.mSuggestedWidth = i;
        this.mSuggestedHeight = i2;
        this.mAdapter.suggestViewSizeBound(i, i2);
    }

    public void setListener(Listener listener) {
        this.mAdapter.setListener(listener);
    }

    public void requestLoad(ContentResolver contentResolver) {
        this.mAdapter.requestLoad(contentResolver);
    }

    public void addNewVideo(ContentResolver contentResolver, Uri uri) {
        this.mAdapter.addNewVideo(contentResolver, uri);
    }

    public void addNewPhoto(ContentResolver contentResolver, Uri uri) {
        this.mAdapter.addNewPhoto(contentResolver, uri);
    }

    public void insertData(LocalData localData) {
        this.mAdapter.insertData(localData);
    }

    public void flush() {
        this.mAdapter.flush();
    }

    public boolean executeDeletion(Context context) {
        return this.mAdapter.executeDeletion(context);
    }

    public boolean undoDataRemoval() {
        return this.mAdapter.undoDataRemoval();
    }

    public void refresh(ContentResolver contentResolver, Uri uri) {
        this.mAdapter.refresh(contentResolver, uri);
    }
}
