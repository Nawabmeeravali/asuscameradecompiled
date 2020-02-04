package com.android.camera.data;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.android.camera.p004ui.FilmStripView.ImageData.PanoramaSupportCallback;
import com.android.camera.util.PhotoSphereHelper.PanoramaViewHelper;
import org.codeaurora.snapcam.C0905R;

public class InProgressDataWrapper implements LocalData {
    private boolean mHasProgressBar;
    final LocalData mLocalData;

    public boolean delete(Context context) {
        return false;
    }

    public int getLocalDataType() {
        return 7;
    }

    public boolean isDataActionSupported(int i) {
        return false;
    }

    public boolean isUIActionSupported(int i) {
        return false;
    }

    public boolean rotate90Degrees(Context context, LocalDataAdapter localDataAdapter, int i, boolean z) {
        return false;
    }

    public InProgressDataWrapper(LocalData localData) {
        this.mLocalData = localData;
    }

    public InProgressDataWrapper(LocalData localData, boolean z) {
        this(localData);
        this.mHasProgressBar = z;
    }

    public View getView(Activity activity, int i, int i2, Drawable drawable, LocalDataAdapter localDataAdapter) {
        View view = this.mLocalData.getView(activity, i, i2, drawable, localDataAdapter);
        if (!this.mHasProgressBar) {
            return view;
        }
        FrameLayout frameLayout = new FrameLayout(activity);
        frameLayout.setLayoutParams(new LayoutParams(i, i2));
        frameLayout.addView(view);
        activity.getLayoutInflater().inflate(C0905R.layout.placeholder_progressbar, frameLayout);
        return frameLayout;
    }

    public long getDateTaken() {
        return this.mLocalData.getDateTaken();
    }

    public long getDateModified() {
        return (long) this.mLocalData.getLocalDataType();
    }

    public String getTitle() {
        return this.mLocalData.getTitle();
    }

    public void onFullScreen(boolean z) {
        this.mLocalData.onFullScreen(z);
    }

    public boolean canSwipeInFullScreen() {
        return this.mLocalData.canSwipeInFullScreen();
    }

    public String getPath() {
        return this.mLocalData.getPath();
    }

    public String getMimeType() {
        return this.mLocalData.getMimeType();
    }

    public MediaDetails getMediaDetails(Context context) {
        return this.mLocalData.getMediaDetails(context);
    }

    public long getSizeInBytes() {
        return this.mLocalData.getSizeInBytes();
    }

    public LocalData refresh(ContentResolver contentResolver) {
        return this.mLocalData.refresh(contentResolver);
    }

    public long getContentId() {
        return this.mLocalData.getContentId();
    }

    public int getWidth() {
        return this.mLocalData.getWidth();
    }

    public int getHeight() {
        return this.mLocalData.getHeight();
    }

    public int getOrientation() {
        return this.mLocalData.getOrientation();
    }

    public int getViewType() {
        return this.mLocalData.getViewType();
    }

    public double[] getLatLong() {
        return this.mLocalData.getLatLong();
    }

    public void prepare() {
        this.mLocalData.prepare();
    }

    public void recycle() {
        this.mLocalData.recycle();
    }

    public void isPhotoSphere(Context context, PanoramaSupportCallback panoramaSupportCallback) {
        this.mLocalData.isPhotoSphere(context, panoramaSupportCallback);
    }

    public void viewPhotoSphere(PanoramaViewHelper panoramaViewHelper) {
        this.mLocalData.viewPhotoSphere(panoramaViewHelper);
    }

    public boolean isPhoto() {
        return this.mLocalData.isPhoto();
    }

    public Uri getContentUri() {
        return this.mLocalData.getContentUri();
    }
}
