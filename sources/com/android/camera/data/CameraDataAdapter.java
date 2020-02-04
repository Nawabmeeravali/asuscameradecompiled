package com.android.camera.data;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import com.android.camera.SDCard;
import com.android.camera.Storage;
import com.android.camera.app.PlaceholderManager;
import com.android.camera.data.LocalData.NewestFirstComparator;
import com.android.camera.data.LocalMediaData.PhotoData;
import com.android.camera.data.LocalMediaData.VideoData;
import com.android.camera.p004ui.FilmStripView.DataAdapter.Listener;
import com.android.camera.p004ui.FilmStripView.DataAdapter.UpdateReporter;
import com.android.camera.p004ui.FilmStripView.ImageData;

public class CameraDataAdapter implements LocalDataAdapter {
    private static final int DEFAULT_DECODE_SIZE = 1600;
    private static final String TAG = "CAM_CameraDataAdapter";
    private LocalDataList mImages = new LocalDataList();
    private Listener mListener;
    private LocalData mLocalDataToDelete;
    private Drawable mPlaceHolder;
    private int mSuggestedHeight = DEFAULT_DECODE_SIZE;
    private int mSuggestedWidth = DEFAULT_DECODE_SIZE;

    private class DeletionTask extends AsyncTask<LocalData, Void, Void> {
        Context mContext;

        DeletionTask(Context context) {
            this.mContext = context;
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(LocalData... localDataArr) {
            for (int i = 0; i < localDataArr.length; i++) {
                if (!localDataArr[i].isDataActionSupported(2)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Deletion is not supported:");
                    sb.append(localDataArr[i]);
                    Log.v(CameraDataAdapter.TAG, sb.toString());
                } else {
                    localDataArr[i].delete(this.mContext);
                }
            }
            return null;
        }
    }

    private class QueryTask extends AsyncTask<ContentResolver, Void, LocalDataList> {
        private QueryTask() {
        }

        /* access modifiers changed from: protected */
        public LocalDataList doInBackground(ContentResolver... contentResolverArr) {
            LocalDataList localDataList = new LocalDataList();
            Cursor query = contentResolverArr[0].query(PhotoData.CONTENT_URI, PhotoData.QUERY_PROJECTION, "_data like ? or _data like ? ", CameraDataAdapter.getCameraPath(), "datetaken DESC, _id DESC");
            String str = "Error loading data:";
            String str2 = CameraDataAdapter.TAG;
            if (query != null && query.moveToFirst()) {
                while (true) {
                    PhotoData buildFromCursor = PhotoData.buildFromCursor(query);
                    if (buildFromCursor == null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(str);
                        sb.append(query.getString(5));
                        Log.e(str2, sb.toString());
                    } else if (buildFromCursor.getMimeType().equals(PlaceholderManager.PLACEHOLDER_MIME_TYPE)) {
                        localDataList.add(new InProgressDataWrapper(buildFromCursor, true));
                    } else {
                        localDataList.add(buildFromCursor);
                    }
                    if (query.isLast()) {
                        break;
                    }
                    query.moveToNext();
                }
            }
            if (query != null) {
                query.close();
            }
            Cursor query2 = contentResolverArr[0].query(VideoData.CONTENT_URI, VideoData.QUERY_PROJECTION, "_data like ? or _data like ? ", CameraDataAdapter.getCameraPath(), "datetaken DESC, _id DESC");
            if (query2 != null && query2.moveToFirst()) {
                query2.moveToFirst();
                while (true) {
                    VideoData buildFromCursor2 = VideoData.buildFromCursor(query2);
                    if (buildFromCursor2 != null) {
                        localDataList.add(buildFromCursor2);
                    } else {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(str);
                        sb2.append(query2.getString(5));
                        Log.e(str2, sb2.toString());
                    }
                    if (query2.isLast()) {
                        break;
                    }
                    query2.moveToNext();
                }
            }
            if (query2 != null) {
                query2.close();
            }
            if (localDataList.size() != 0) {
                localDataList.sort(new NewestFirstComparator());
            }
            return localDataList;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(LocalDataList localDataList) {
            CameraDataAdapter.this.replaceData(localDataList);
        }
    }

    public CameraDataAdapter(Drawable drawable) {
        this.mPlaceHolder = drawable;
    }

    public void requestLoad(ContentResolver contentResolver) {
        new QueryTask().execute(new ContentResolver[]{contentResolver});
    }

    public LocalData getLocalData(int i) {
        if (i < 0 || i >= this.mImages.size()) {
            return null;
        }
        return this.mImages.get(i);
    }

    public int getTotalNumber() {
        return this.mImages.size();
    }

    public ImageData getImageData(int i) {
        return getLocalData(i);
    }

    public void suggestViewSizeBound(int i, int i2) {
        if (i <= 0 || i2 <= 0) {
            this.mSuggestedHeight = DEFAULT_DECODE_SIZE;
            this.mSuggestedWidth = DEFAULT_DECODE_SIZE;
            return;
        }
        if (i >= DEFAULT_DECODE_SIZE) {
            i = DEFAULT_DECODE_SIZE;
        }
        this.mSuggestedWidth = i;
        if (i2 >= DEFAULT_DECODE_SIZE) {
            i2 = DEFAULT_DECODE_SIZE;
        }
        this.mSuggestedHeight = i2;
    }

    public View getView(Activity activity, int i) {
        if (i >= this.mImages.size() || i < 0) {
            return null;
        }
        return this.mImages.get(i).getView(activity, this.mSuggestedWidth, this.mSuggestedHeight, this.mPlaceHolder.getConstantState().newDrawable(), this);
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
        if (this.mImages != null) {
            this.mListener.onDataLoaded();
        }
    }

    public boolean canSwipeInFullScreen(int i) {
        if (i >= this.mImages.size() || i <= 0) {
            return true;
        }
        return this.mImages.get(i).canSwipeInFullScreen();
    }

    public void removeData(Context context, int i) {
        if (i < this.mImages.size()) {
            LocalData remove = this.mImages.remove(i);
            executeDeletion(context);
            this.mLocalDataToDelete = remove;
            this.mListener.onDataRemoved(i, remove);
        }
    }

    public void addNewVideo(ContentResolver contentResolver, Uri uri) {
        ContentResolver contentResolver2 = contentResolver;
        Uri uri2 = uri;
        Cursor query = contentResolver2.query(uri2, VideoData.QUERY_PROJECTION, "_data like ? or _data like ? ", getCameraPath(), "datetaken DESC, _id DESC");
        if (query != null && query.moveToFirst()) {
            int findDataByContentUri = findDataByContentUri(uri);
            VideoData buildFromCursor = VideoData.buildFromCursor(query);
            if (buildFromCursor == null) {
                Log.e(TAG, "video data not found");
            } else if (findDataByContentUri != -1) {
                updateData(findDataByContentUri, buildFromCursor);
            } else {
                insertData(buildFromCursor);
            }
            query.close();
        }
    }

    public void addNewPhoto(ContentResolver contentResolver, Uri uri) {
        ContentResolver contentResolver2 = contentResolver;
        Uri uri2 = uri;
        Cursor query = contentResolver2.query(uri2, PhotoData.QUERY_PROJECTION, "_data like ? or _data like ? ", getCameraPath(), "datetaken DESC, _id DESC");
        if (query != null && query.moveToFirst()) {
            int findDataByContentUri = findDataByContentUri(uri);
            PhotoData buildFromCursor = PhotoData.buildFromCursor(query);
            if (findDataByContentUri != -1) {
                Log.v(TAG, "found duplicate photo");
                updateData(findDataByContentUri, buildFromCursor);
            } else {
                insertData(buildFromCursor);
            }
            query.close();
        }
    }

    public int findDataByContentUri(Uri uri) {
        return this.mImages.indexOf(uri);
    }

    public boolean undoDataRemoval() {
        LocalData localData = this.mLocalDataToDelete;
        if (localData == null) {
            return false;
        }
        this.mLocalDataToDelete = null;
        insertData(localData);
        return true;
    }

    public boolean executeDeletion(Context context) {
        if (this.mLocalDataToDelete == null) {
            return false;
        }
        new DeletionTask(context).execute(new LocalData[]{this.mLocalDataToDelete});
        this.mLocalDataToDelete = null;
        return true;
    }

    public void flush() {
        replaceData(new LocalDataList());
    }

    public void refresh(ContentResolver contentResolver, Uri uri) {
        int findDataByContentUri = findDataByContentUri(uri);
        if (findDataByContentUri != -1) {
            LocalData refresh = this.mImages.get(findDataByContentUri).refresh(contentResolver);
            if (refresh != null) {
                updateData(findDataByContentUri, refresh);
            }
        }
    }

    public void updateData(final int i, LocalData localData) {
        this.mImages.set(i, localData);
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onDataUpdated(new UpdateReporter() {
                public boolean isDataRemoved(int i) {
                    return false;
                }

                public boolean isDataUpdated(int i) {
                    return i == i;
                }
            });
        }
    }

    public void insertData(LocalData localData) {
        NewestFirstComparator newestFirstComparator = new NewestFirstComparator();
        int i = 0;
        while (i < this.mImages.size() && newestFirstComparator.compare(localData, this.mImages.get(i)) > 0) {
            i++;
        }
        this.mImages.add(i, localData);
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onDataInserted(i, localData);
        }
    }

    /* access modifiers changed from: private */
    public void replaceData(LocalDataList localDataList) {
        if (localDataList.size() != 0 || this.mImages.size() != 0) {
            this.mImages = localDataList;
            Listener listener = this.mListener;
            if (listener != null) {
                listener.onDataLoaded();
            }
        }
    }

    /* access modifiers changed from: private */
    public static String[] getCameraPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(Storage.DIRECTORY);
        String str = "/%";
        sb.append(str);
        StringBuilder sb2 = new StringBuilder();
        sb2.append(SDCard.instance().getDirectory());
        sb2.append(str);
        return new String[]{sb.toString(), sb2.toString()};
    }
}
