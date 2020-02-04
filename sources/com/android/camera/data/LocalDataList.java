package com.android.camera.data;

import android.net.Uri;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

public class LocalDataList {
    private LinkedList<LocalData> mList = new LinkedList<>();
    private HashMap<Uri, LocalData> mUriMap = new HashMap<>();

    private static class UriWrapper {
        private final Uri mUri;

        public UriWrapper(Uri uri) {
            this.mUri = uri;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof LocalData)) {
                return false;
            }
            return this.mUri.equals(((LocalData) obj).getContentUri());
        }
    }

    public LocalData get(int i) {
        return (LocalData) this.mList.get(i);
    }

    public LocalData remove(int i) {
        LocalData localData = (LocalData) this.mList.remove(i);
        this.mUriMap.remove(localData);
        return localData;
    }

    public LocalData get(Uri uri) {
        return (LocalData) this.mUriMap.get(uri);
    }

    public void set(int i, LocalData localData) {
        this.mList.set(i, localData);
        this.mUriMap.put(localData.getContentUri(), localData);
    }

    public void add(LocalData localData) {
        this.mList.add(localData);
        this.mUriMap.put(localData.getContentUri(), localData);
    }

    public void add(int i, LocalData localData) {
        this.mList.add(i, localData);
        this.mUriMap.put(localData.getContentUri(), localData);
    }

    public int size() {
        return this.mList.size();
    }

    public void sort(Comparator<LocalData> comparator) {
        Collections.sort(this.mList, comparator);
    }

    public int indexOf(Uri uri) {
        if (!this.mUriMap.containsKey(uri)) {
            return -1;
        }
        return this.mList.indexOf(new UriWrapper(uri));
    }
}
