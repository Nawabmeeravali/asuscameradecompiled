package com.android.camera.data;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import com.android.camera.p004ui.FilmStripView.DataAdapter.Listener;
import com.android.camera.p004ui.FilmStripView.DataAdapter.UpdateReporter;
import com.android.camera.p004ui.FilmStripView.ImageData;

public class FixedFirstDataAdapter extends AbstractLocalDataAdapterWrapper implements Listener {
    private static final String TAG = "CAM_FixedFirstDataAdapter";
    private LocalData mFirstData;
    private Listener mListener;

    public FixedFirstDataAdapter(LocalDataAdapter localDataAdapter, LocalData localData) {
        super(localDataAdapter);
        if (localData != null) {
            this.mFirstData = localData;
            return;
        }
        throw new AssertionError("data is null");
    }

    public LocalData getLocalData(int i) {
        if (i == 0) {
            return this.mFirstData;
        }
        return this.mAdapter.getLocalData(i - 1);
    }

    public void removeData(Context context, int i) {
        if (i > 0) {
            this.mAdapter.removeData(context, i - 1);
        }
    }

    public int findDataByContentUri(Uri uri) {
        int findDataByContentUri = this.mAdapter.findDataByContentUri(uri);
        if (findDataByContentUri != -1) {
            return findDataByContentUri + 1;
        }
        return -1;
    }

    public void updateData(int i, LocalData localData) {
        if (i == 0) {
            this.mFirstData = localData;
            Listener listener = this.mListener;
            if (listener != null) {
                listener.onDataUpdated(new UpdateReporter() {
                    public boolean isDataRemoved(int i) {
                        return false;
                    }

                    public boolean isDataUpdated(int i) {
                        return i == 0;
                    }
                });
                return;
            }
            return;
        }
        this.mAdapter.updateData(i - 1, localData);
    }

    public int getTotalNumber() {
        return this.mAdapter.getTotalNumber() + 1;
    }

    public View getView(Activity activity, int i) {
        if (i != 0) {
            return this.mAdapter.getView(activity, i - 1);
        }
        return this.mFirstData.getView(activity, this.mSuggestedWidth, this.mSuggestedHeight, null, null);
    }

    public ImageData getImageData(int i) {
        if (i == 0) {
            return this.mFirstData;
        }
        return this.mAdapter.getImageData(i - 1);
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
        this.mAdapter.setListener(listener == null ? null : this);
        Listener listener2 = this.mListener;
        if (listener2 != null) {
            listener2.onDataLoaded();
        }
    }

    public boolean canSwipeInFullScreen(int i) {
        if (i == 0) {
            return this.mFirstData.canSwipeInFullScreen();
        }
        return this.mAdapter.canSwipeInFullScreen(i - 1);
    }

    public void onDataLoaded() {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onDataUpdated(new UpdateReporter() {
                public boolean isDataRemoved(int i) {
                    return false;
                }

                public boolean isDataUpdated(int i) {
                    return i != 0;
                }
            });
            this.mListener.onDataLoaded();
        }
    }

    public void onDataUpdated(final UpdateReporter updateReporter) {
        this.mListener.onDataUpdated(new UpdateReporter() {
            public boolean isDataRemoved(int i) {
                return i != 0 && updateReporter.isDataRemoved(i - 1);
            }

            public boolean isDataUpdated(int i) {
                return i != 0 && updateReporter.isDataUpdated(i - 1);
            }
        });
    }

    public void onDataInserted(int i, ImageData imageData) {
        this.mListener.onDataInserted(i + 1, imageData);
    }

    public void onDataRemoved(int i, ImageData imageData) {
        this.mListener.onDataRemoved(i + 1, imageData);
    }
}
