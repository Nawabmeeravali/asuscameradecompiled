package com.android.camera.data;

import android.content.Context;
import android.net.Uri;
import com.android.camera.util.PhotoSphereHelper;
import com.android.camera.util.PhotoSphereHelper.PanoramaMetadata;
import java.util.ArrayList;
import java.util.Iterator;

public class PanoramaMetadataLoader {
    private ArrayList<PanoramaMetadataCallback> mCallbacksWaiting;
    /* access modifiers changed from: private */
    public Uri mMediaUri;
    private PanoramaMetadata mPanoramaMetadata;

    public interface PanoramaMetadataCallback {
        void onPanoramaMetadataLoaded(PanoramaMetadata panoramaMetadata);
    }

    public PanoramaMetadataLoader(Uri uri) {
        this.mMediaUri = uri;
    }

    public synchronized void getPanoramaMetadata(final Context context, PanoramaMetadataCallback panoramaMetadataCallback) {
        if (this.mPanoramaMetadata != null) {
            panoramaMetadataCallback.onPanoramaMetadataLoaded(this.mPanoramaMetadata);
        } else {
            if (this.mCallbacksWaiting == null) {
                this.mCallbacksWaiting = new ArrayList<>();
                new Thread() {
                    public void run() {
                        PanoramaMetadataLoader panoramaMetadataLoader = PanoramaMetadataLoader.this;
                        panoramaMetadataLoader.onLoadingDone(PhotoSphereHelper.getPanoramaMetadata(context, panoramaMetadataLoader.mMediaUri));
                    }
                }.start();
            }
            this.mCallbacksWaiting.add(panoramaMetadataCallback);
        }
    }

    public synchronized void clearCachedValues() {
        if (this.mPanoramaMetadata != null) {
            this.mPanoramaMetadata = null;
        }
    }

    /* access modifiers changed from: private */
    public synchronized void onLoadingDone(PanoramaMetadata panoramaMetadata) {
        this.mPanoramaMetadata = panoramaMetadata;
        if (this.mPanoramaMetadata == null) {
            this.mPanoramaMetadata = PhotoSphereHelper.NOT_PANORAMA;
        }
        Iterator it = this.mCallbacksWaiting.iterator();
        while (it.hasNext()) {
            ((PanoramaMetadataCallback) it.next()).onPanoramaMetadataLoaded(this.mPanoramaMetadata);
        }
        this.mCallbacksWaiting = null;
    }
}
