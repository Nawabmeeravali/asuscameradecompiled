package org.codeaurora.snapcam.filter;

import android.graphics.Rect;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.CaptureResult.Key;
import android.media.Image;
import android.media.Image.Plane;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.codeaurora.snapcam.filter.ClearSightNativeEngine.CamSystemCalibrationData;

public class DDMNativeEngine {
    private static final String TAG = "DDMNativeEngine";
    private static final int VU_PLANE = 2;
    private static final int Y_PLANE = 0;
    private static boolean mLibLoaded;
    private Key<byte[]> SCALE_CROP_ROTATION_REPROCESS_BLOB = new Key<>("org.codeaurora.qcamera3.hal_private_data.reprocess_data_blob", byte[].class);
    CamReprocessInfo mBayerCamReprocessInfo;
    private Image mBayerImage;
    CamSystemCalibrationData mCamSystemCalibrationData;
    private float mLensFocusDistance;
    CamReprocessInfo mMonoCamReprocessInfo;
    private Image mMonoImage;
    private ByteBuffer mPrimaryY;
    private ByteBuffer mPrivaryVU;

    public static class CamReprocessInfo {
        private final String[] SCALE_CROP_ROTATION_FORMAT_STRING = {"Sensor Crop left = %d\n", "Sensor Crop top = %d\n", "Sensor Crop width = %d\n", "Sensor Crop height = %d\n", "Sensor ROI Map left = %d\n", "Sensor ROI Map top = %d\n", "Sensor ROI Map width = %d\n", "Sensor ROI Map height = %d\n", "CAMIF Crop left = %d\n", "CAMIF Crop top = %d\n", "CAMIF Crop width = %d\n", "CAMIF Crop height = %d\n", "CAMIF ROI Map left = %d\n", "CAMIF ROI Map top = %d\n", "CAMIF ROI Map width = %d\n", "CAMIF ROI Map height = %d\n", "ISP Crop left = %d\n", "ISP Crop top = %d\n", "ISP Crop width = %d\n", "ISP Crop height = %d\n", "ISP ROI Map left = %d\n", "ISP ROI Map top = %d\n", "ISP ROI Map width = %d\n", "ISP ROI Map height = %d\n", "CPP Crop left = %d\n", "CPP Crop top = %d\n", "CPP Crop width = %d\n", "CPP Crop height = %d\n", "CPP ROI Map left = %d\n", "CPP ROI Map top = %d\n", "CPP ROI Map width = %d\n", "CPP ROI Map height = %d\n", "Focal length Ratio = %f\n", "Current pipeline mirror flip setting = %d\n", "Current pipeline rotation setting = %d\n"};
        float af_focal_length_ratio;
        CamStreamCropInfo camif_crop_info;
        CamStreamCropInfo cpp_crop_info;
        CamStreamCropInfo isp_crop_info;
        int pipeline_flip;
        CamRotationInfo rotation_info;
        CamStreamCropInfo sensor_crop_info;

        public static CamReprocessInfo createCamReprocessFromBytes(byte[] bArr) {
            ByteBuffer wrap = ByteBuffer.wrap(bArr);
            wrap.order(ByteOrder.LITTLE_ENDIAN);
            return createCamReprocessFromBytes(wrap);
        }

        public static CamReprocessInfo createCamReprocessFromBytes(ByteBuffer byteBuffer) {
            CamReprocessInfo camReprocessInfo = new CamReprocessInfo();
            camReprocessInfo.sensor_crop_info = CamStreamCropInfo.createFromByteBuffer(byteBuffer);
            camReprocessInfo.camif_crop_info = CamStreamCropInfo.createFromByteBuffer(byteBuffer);
            camReprocessInfo.isp_crop_info = CamStreamCropInfo.createFromByteBuffer(byteBuffer);
            camReprocessInfo.cpp_crop_info = CamStreamCropInfo.createFromByteBuffer(byteBuffer);
            camReprocessInfo.af_focal_length_ratio = byteBuffer.getFloat();
            camReprocessInfo.pipeline_flip = byteBuffer.getInt();
            camReprocessInfo.rotation_info = CamRotationInfo.createFromByteBuffer(byteBuffer);
            return camReprocessInfo;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[0], new Object[]{Integer.valueOf(this.sensor_crop_info.crop.left)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[1], new Object[]{Integer.valueOf(this.sensor_crop_info.crop.top)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[2], new Object[]{Integer.valueOf(this.sensor_crop_info.crop.width())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[3], new Object[]{Integer.valueOf(this.sensor_crop_info.crop.height())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[4], new Object[]{Integer.valueOf(this.sensor_crop_info.roi_map.left)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[5], new Object[]{Integer.valueOf(this.sensor_crop_info.roi_map.top)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[6], new Object[]{Integer.valueOf(this.sensor_crop_info.roi_map.width())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[7], new Object[]{Integer.valueOf(this.sensor_crop_info.roi_map.height())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[8], new Object[]{Integer.valueOf(this.camif_crop_info.crop.left)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[9], new Object[]{Integer.valueOf(this.camif_crop_info.crop.top)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[10], new Object[]{Integer.valueOf(this.camif_crop_info.crop.width())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[11], new Object[]{Integer.valueOf(this.camif_crop_info.crop.height())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[12], new Object[]{Integer.valueOf(this.camif_crop_info.roi_map.left)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[13], new Object[]{Integer.valueOf(this.camif_crop_info.roi_map.top)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[14], new Object[]{Integer.valueOf(this.camif_crop_info.roi_map.width())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[15], new Object[]{Integer.valueOf(this.camif_crop_info.roi_map.height())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[16], new Object[]{Integer.valueOf(this.isp_crop_info.crop.left)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[17], new Object[]{Integer.valueOf(this.isp_crop_info.crop.top)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[18], new Object[]{Integer.valueOf(this.isp_crop_info.crop.width())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[19], new Object[]{Integer.valueOf(this.isp_crop_info.crop.height())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[20], new Object[]{Integer.valueOf(this.isp_crop_info.roi_map.left)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[21], new Object[]{Integer.valueOf(this.isp_crop_info.roi_map.top)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[22], new Object[]{Integer.valueOf(this.isp_crop_info.roi_map.width())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[23], new Object[]{Integer.valueOf(this.isp_crop_info.roi_map.height())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[24], new Object[]{Integer.valueOf(this.cpp_crop_info.crop.left)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[25], new Object[]{Integer.valueOf(this.cpp_crop_info.crop.top)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[26], new Object[]{Integer.valueOf(this.cpp_crop_info.crop.width())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[27], new Object[]{Integer.valueOf(this.cpp_crop_info.crop.height())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[28], new Object[]{Integer.valueOf(this.cpp_crop_info.roi_map.left)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[29], new Object[]{Integer.valueOf(this.cpp_crop_info.roi_map.top)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[30], new Object[]{Integer.valueOf(this.cpp_crop_info.roi_map.width())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[31], new Object[]{Integer.valueOf(this.cpp_crop_info.roi_map.height())}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[32], new Object[]{Float.valueOf(this.af_focal_length_ratio)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[33], new Object[]{Integer.valueOf(this.pipeline_flip)}));
            sb.append(String.format(this.SCALE_CROP_ROTATION_FORMAT_STRING[34], new Object[]{Integer.valueOf(this.rotation_info.jpeg_rotation)}));
            return sb.toString();
        }
    }

    public static class CamRotationInfo {
        int device_rotation;
        int jpeg_rotation;
        int stream_id;

        private CamRotationInfo() {
        }

        public static CamRotationInfo createCamReprocessFromBytes(byte[] bArr) {
            ByteBuffer wrap = ByteBuffer.wrap(bArr);
            wrap.order(ByteOrder.LITTLE_ENDIAN);
            return createFromByteBuffer(wrap);
        }

        public static CamRotationInfo createFromByteBuffer(ByteBuffer byteBuffer) {
            CamRotationInfo camRotationInfo = new CamRotationInfo();
            camRotationInfo.jpeg_rotation = byteBuffer.getInt();
            camRotationInfo.device_rotation = byteBuffer.getInt();
            camRotationInfo.stream_id = byteBuffer.getInt();
            return camRotationInfo;
        }
    }

    public static class CamStreamCropInfo {
        Rect crop;
        Rect roi_map;
        float scale_ratio;
        int stream_id;
        int stream_zoom;
        int user_zoom;

        private CamStreamCropInfo() {
        }

        public static CamStreamCropInfo createFromBytes(byte[] bArr) {
            ByteBuffer wrap = ByteBuffer.wrap(bArr);
            wrap.order(ByteOrder.LITTLE_ENDIAN);
            return createFromByteBuffer(wrap);
        }

        public static CamStreamCropInfo createFromByteBuffer(ByteBuffer byteBuffer) {
            CamStreamCropInfo camStreamCropInfo = new CamStreamCropInfo();
            camStreamCropInfo.stream_id = byteBuffer.getInt();
            Rect rect = new Rect();
            rect.left = byteBuffer.getInt();
            rect.top = byteBuffer.getInt();
            rect.right = rect.left + byteBuffer.getInt();
            rect.bottom = rect.top + byteBuffer.getInt();
            camStreamCropInfo.crop = rect;
            Rect rect2 = new Rect();
            rect2.left = byteBuffer.getInt();
            rect2.top = byteBuffer.getInt();
            rect2.right = rect2.left + byteBuffer.getInt();
            rect2.bottom = rect2.top + byteBuffer.getInt();
            camStreamCropInfo.roi_map = rect2;
            camStreamCropInfo.user_zoom = byteBuffer.getInt();
            camStreamCropInfo.stream_zoom = byteBuffer.getInt();
            camStreamCropInfo.scale_ratio = byteBuffer.getFloat();
            return camStreamCropInfo;
        }
    }

    public static class DepthMap {
        private ByteBuffer buffer;
        private int height;
        private Rect roi;
        private int stride;
        private int width;
    }

    private native boolean nativeDualCameraGenerateDDM(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3, int i4, ByteBuffer byteBuffer3, ByteBuffer byteBuffer4, int i5, int i6, int i7, int i8, byte[] bArr, int i9, int[] iArr, String str, String str2, String str3, float f, boolean z);

    private native boolean nativeGetDepthMapSize(int i, int i2, int[] iArr);

    static {
        String str = TAG;
        try {
            System.loadLibrary("jni_dualcamera");
            mLibLoaded = true;
            Log.v(str, "successfully loaded jni_dualcamera lib");
        } catch (UnsatisfiedLinkError e) {
            Log.e(str, "failed to load jni_dualcamera lib");
            Log.e(str, e.toString());
            e.printStackTrace();
            mLibLoaded = false;
        }
    }

    public boolean getDepthMapSize(int[] iArr) {
        return nativeGetDepthMapSize(this.mBayerImage.getWidth(), this.mBayerImage.getHeight(), iArr);
    }

    public void setCamSystemCalibrationData(CamSystemCalibrationData camSystemCalibrationData) {
        this.mCamSystemCalibrationData = camSystemCalibrationData;
    }

    public String getOTPCalibration() {
        return this.mCamSystemCalibrationData.toString();
    }

    public void reset() {
        this.mBayerImage = null;
        this.mMonoImage = null;
        this.mBayerCamReprocessInfo = null;
        this.mMonoCamReprocessInfo = null;
        this.mLensFocusDistance = 0.0f;
    }

    public boolean isReadyForGenerateDepth() {
        return (this.mBayerImage == null || this.mMonoImage == null || this.mBayerCamReprocessInfo == null || this.mMonoCamReprocessInfo == null) ? false : true;
    }

    public void setBayerLensFocusDistance(float f) {
        this.mLensFocusDistance = f;
    }

    public void setBayerImage(Image image) {
        this.mBayerImage = image;
    }

    public void setMonoImage(Image image) {
        this.mMonoImage = image;
    }

    public void setBayerReprocessResult(CaptureResult captureResult) {
        this.mBayerCamReprocessInfo = CamReprocessInfo.createCamReprocessFromBytes((byte[]) captureResult.get(this.SCALE_CROP_ROTATION_REPROCESS_BLOB));
    }

    public String getBayerScaleCrop() {
        return this.mBayerCamReprocessInfo.toString();
    }

    public void setMonoReprocessResult(CaptureResult captureResult) {
        this.mMonoCamReprocessInfo = CamReprocessInfo.createCamReprocessFromBytes((byte[]) captureResult.get(this.SCALE_CROP_ROTATION_REPROCESS_BLOB));
    }

    public String getMonoScaleCrop() {
        return this.mMonoCamReprocessInfo.toString();
    }

    public boolean dualCameraGenerateDDM(byte[] bArr, int i, Rect rect) {
        Rect rect2 = rect;
        int i2 = (this.mLensFocusDistance > 0.0f ? 1 : (this.mLensFocusDistance == 0.0f ? 0 : -1));
        String str = TAG;
        if (i2 == 0) {
            Log.e(str, " dualCameraGenerateDDM error: mLensFocusDistance is 0");
            return false;
        }
        Image image = this.mBayerImage;
        if (image == null || this.mMonoImage == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("mBayerImage=");
            sb.append(this.mBayerImage == null);
            sb.append(" mMonoImage=");
            sb.append(this.mMonoImage == null);
            Log.e(str, sb.toString());
            return false;
        } else if (bArr == null) {
            Log.e(str, "depthMapBuffer can't be null");
            return false;
        } else if (this.mMonoCamReprocessInfo == null || this.mBayerCamReprocessInfo == null || this.mCamSystemCalibrationData == null) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("mMonoCamReprocessInfo== null:");
            sb2.append(this.mMonoCamReprocessInfo == null);
            sb2.append(" mBayerCamReprocessInfo == null:");
            sb2.append(this.mBayerCamReprocessInfo == null);
            sb2.append(" mCamSystemCalibrationData == null:");
            sb2.append(this.mCamSystemCalibrationData == null);
            Log.e(str, sb2.toString());
            return false;
        } else {
            Plane[] planes = image.getPlanes();
            Plane[] planes2 = this.mMonoImage.getPlanes();
            int[] iArr = new int[4];
            int[] iArr2 = iArr;
            boolean nativeDualCameraGenerateDDM = nativeDualCameraGenerateDDM(planes[0].getBuffer(), planes[2].getBuffer(), this.mBayerImage.getWidth(), this.mBayerImage.getHeight(), planes[0].getRowStride(), planes[2].getRowStride(), planes2[0].getBuffer(), planes2[2].getBuffer(), this.mMonoImage.getWidth(), this.mMonoImage.getHeight(), planes2[0].getRowStride(), planes2[2].getRowStride(), bArr, i, iArr, this.mBayerCamReprocessInfo.toString(), this.mMonoCamReprocessInfo.toString(), this.mCamSystemCalibrationData.toString(), this.mLensFocusDistance, true);
            Rect rect3 = rect;
            rect3.left = iArr2[0];
            rect3.top = iArr2[1];
            rect3.right = iArr2[0] + iArr2[2];
            rect3.bottom = iArr2[1] + iArr2[3];
            return nativeDualCameraGenerateDDM;
        }
    }
}
