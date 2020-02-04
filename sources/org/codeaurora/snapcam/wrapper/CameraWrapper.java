package org.codeaurora.snapcam.wrapper;

import android.hardware.Camera;
import android.hardware.Camera.CameraDataCallback;
import android.hardware.Camera.CameraMetaDataCallback;
import android.util.Log;
import com.asus.scenedetectlib.BuildConfig;
import java.lang.reflect.Method;

public class CameraWrapper extends Wrapper {
    private static Method method_sendHistogramData;
    private static Method method_setHistogramMode;
    private static Method method_setLongshot;
    private static Method method_setMetadataCb;

    public static final void setMetadataCb(Camera camera, CameraMetaDataCallback cameraMetaDataCallback) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append(BuildConfig.FLAVOR);
            sb.append(Camera.class);
            sb.append(" no setMetadataCb");
            Log.e("Wrapper", sb.toString());
            return;
        }
        try {
            if (method_setMetadataCb == null) {
                method_setMetadataCb = Camera.class.getMethod("setMetadataCb", new Class[]{CameraMetaDataCallback.class});
            }
            method_setMetadataCb.invoke(camera, new Object[]{cameraMetaDataCallback});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void setHistogramMode(Camera camera, CameraDataCallback cameraDataCallback) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append(BuildConfig.FLAVOR);
            sb.append(Camera.class);
            sb.append(" no setHistogramMode");
            Log.e("Wrapper", sb.toString());
            return;
        }
        try {
            if (method_setHistogramMode == null) {
                method_setHistogramMode = Camera.class.getMethod("setHistogramMode", new Class[]{CameraDataCallback.class});
            }
            method_setHistogramMode.invoke(camera, new Object[]{cameraDataCallback});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void sendHistogramData(Camera camera) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append(BuildConfig.FLAVOR);
            sb.append(Camera.class);
            sb.append(" no sendHistogramData");
            Log.e("Wrapper", sb.toString());
            return;
        }
        try {
            if (method_sendHistogramData == null) {
                method_sendHistogramData = Camera.class.getMethod("sendHistogramData", new Class[0]);
            }
            method_sendHistogramData.invoke(camera, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void setLongshot(Camera camera, boolean z) {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append(BuildConfig.FLAVOR);
            sb.append(Camera.class);
            sb.append(" no setLongshot");
            Log.e("Wrapper", sb.toString());
            return;
        }
        try {
            if (method_setLongshot == null) {
                method_setLongshot = Camera.class.getDeclaredMethod("setLongshot", new Class[]{Boolean.TYPE});
            }
            method_setLongshot.invoke(camera, new Object[]{Boolean.valueOf(z)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
