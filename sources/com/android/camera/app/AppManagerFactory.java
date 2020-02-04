package com.android.camera.app;

import android.content.Context;

public class AppManagerFactory {
    private static AppManagerFactory sFactory;
    private PlaceholderManager mGcamProcessingManager;
    private PanoramaStitchingManager mPanoramaStitchingManager;

    public static synchronized AppManagerFactory getInstance(Context context) {
        AppManagerFactory appManagerFactory;
        synchronized (AppManagerFactory.class) {
            if (sFactory == null) {
                sFactory = new AppManagerFactory(context.getApplicationContext());
            }
            appManagerFactory = sFactory;
        }
        return appManagerFactory;
    }

    private AppManagerFactory(Context context) {
        this.mPanoramaStitchingManager = new PanoramaStitchingManager(context);
        this.mGcamProcessingManager = new PlaceholderManager(context);
    }

    public PanoramaStitchingManager getPanoramaStitchingManager() {
        return this.mPanoramaStitchingManager;
    }

    public PlaceholderManager getGcamProcessingManager() {
        return this.mGcamProcessingManager;
    }
}
