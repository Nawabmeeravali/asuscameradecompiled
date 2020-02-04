package com.android.camera.data;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import com.android.camera.p004ui.FilmStripView.DataAdapter.Listener;
import com.android.camera.p004ui.FilmStripView.DataAdapter.UpdateReporter;
import com.android.camera.p004ui.FilmStripView.ImageData;

public class FixedLastDataAdapter extends AbstractLocalDataAdapterWrapper {
    private LocalData mLastData;
    private Listener mListener;

    public FixedLastDataAdapter(LocalDataAdapter localDataAdapter, LocalData localData) {
        super(localDataAdapter);
        if (localData != null) {
            this.mLastData = localData;
            return;
        }
        throw new AssertionError("data is null");
    }

    public void setListener(Listener listener) {
        super.setListener(listener);
        this.mListener = listener;
    }

    public LocalData getLocalData(int i) {
        int totalNumber = this.mAdapter.getTotalNumber();
        if (i < totalNumber) {
            return this.mAdapter.getLocalData(i);
        }
        if (i == totalNumber) {
            return this.mLastData;
        }
        return null;
    }

    public void removeData(Context context, int i) {
        if (i < this.mAdapter.getTotalNumber()) {
            this.mAdapter.removeData(context, i);
        }
    }

    public int findDataByContentUri(Uri uri) {
        return this.mAdapter.findDataByContentUri(uri);
    }

    public void updateData(final int i, LocalData localData) {
        int totalNumber = this.mAdapter.getTotalNumber();
        if (i < totalNumber) {
            this.mAdapter.updateData(i, localData);
        } else if (i == totalNumber) {
            this.mLastData = localData;
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
    }

    public int getTotalNumber() {
        return this.mAdapter.getTotalNumber() + 1;
    }

    public View getView(Activity activity, int i) {
        int totalNumber = this.mAdapter.getTotalNumber();
        if (i < totalNumber) {
            return this.mAdapter.getView(activity, i);
        }
        if (i != totalNumber) {
            return null;
        }
        return this.mLastData.getView(activity, this.mSuggestedWidth, this.mSuggestedHeight, null, null);
    }

    public ImageData getImageData(int i) {
        int totalNumber = this.mAdapter.getTotalNumber();
        if (i < totalNumber) {
            return this.mAdapter.getImageData(i);
        }
        if (i == totalNumber) {
            return this.mLastData;
        }
        return null;
    }

    public boolean canSwipeInFullScreen(int i) {
        int totalNumber = this.mAdapter.getTotalNumber();
        if (i < totalNumber) {
            return this.mAdapter.canSwipeInFullScreen(i);
        }
        if (i == totalNumber) {
            return this.mLastData.canSwipeInFullScreen();
        }
        return false;
    }
}
