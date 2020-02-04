package com.android.camera;

public class CameraManagerFactory {
    private static AndroidCameraManagerImpl sAndroidCameraManager;

    public static synchronized CameraManager getAndroidCameraManager() {
        AndroidCameraManagerImpl androidCameraManagerImpl;
        synchronized (CameraManagerFactory.class) {
            if (sAndroidCameraManager == null) {
                sAndroidCameraManager = new AndroidCameraManagerImpl();
            }
            androidCameraManagerImpl = sAndroidCameraManager;
        }
        return androidCameraManagerImpl;
    }
}
