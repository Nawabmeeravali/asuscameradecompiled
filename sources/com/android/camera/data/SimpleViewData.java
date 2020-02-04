package com.android.camera.data;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import com.android.camera.p004ui.FilmStripView.ImageData.PanoramaSupportCallback;
import com.android.camera.util.PhotoSphereHelper.PanoramaViewHelper;
import com.asus.scenedetectlib.BuildConfig;

public class SimpleViewData implements LocalData {
    private static final String TAG = "CAM_SimpleViewData";
    private final long mDateModified;
    private final long mDateTaken;
    private final int mHeight;
    private final View mView;
    private final int mWidth;

    public boolean canSwipeInFullScreen() {
        return true;
    }

    public boolean delete(Context context) {
        return false;
    }

    public long getContentId() {
        return -1;
    }

    public double[] getLatLong() {
        return null;
    }

    public int getLocalDataType() {
        return 2;
    }

    public MediaDetails getMediaDetails(Context context) {
        return null;
    }

    public String getMimeType() {
        return null;
    }

    public int getOrientation() {
        return 0;
    }

    public String getPath() {
        return BuildConfig.FLAVOR;
    }

    public long getSizeInBytes() {
        return 0;
    }

    public String getTitle() {
        return BuildConfig.FLAVOR;
    }

    public int getViewType() {
        return 2;
    }

    public boolean isDataActionSupported(int i) {
        return false;
    }

    public boolean isPhoto() {
        return false;
    }

    public boolean isUIActionSupported(int i) {
        return false;
    }

    public void onFullScreen(boolean z) {
    }

    public void prepare() {
    }

    public void recycle() {
    }

    public LocalData refresh(ContentResolver contentResolver) {
        return null;
    }

    public void viewPhotoSphere(PanoramaViewHelper panoramaViewHelper) {
    }

    public SimpleViewData(View view, int i, int i2, int i3, int i4) {
        this.mView = view;
        this.mWidth = i;
        this.mHeight = i2;
        this.mDateTaken = (long) i3;
        this.mDateModified = (long) i4;
    }

    public long getDateTaken() {
        return this.mDateTaken;
    }

    public long getDateModified() {
        return this.mDateModified;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public Uri getContentUri() {
        return Uri.EMPTY;
    }

    public View getView(Activity activity, int i, int i2, Drawable drawable, LocalDataAdapter localDataAdapter) {
        return this.mView;
    }

    public void isPhotoSphere(Context context, PanoramaSupportCallback panoramaSupportCallback) {
        panoramaSupportCallback.panoramaInfoAvailable(false, false);
    }

    public boolean rotate90Degrees(Context context, LocalDataAdapter localDataAdapter, int i, boolean z) {
        Log.w(TAG, "Unexpected call in rotate90Degrees()");
        return false;
    }
}
