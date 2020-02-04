package org.codeaurora.snapcam.filter;

import android.graphics.Rect;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.Image.Plane;
import android.util.Log;
import com.android.camera.util.PersistUtil;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ClearSightNativeEngine {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (PersistUtil.getCamera2Debug() == 2 || PersistUtil.getCamera2Debug() == 100);
    private static final int METADATA_SIZE = 6;
    private static final String TAG = "ClearSightNativeEngine";
    private static final int VU_PLANE = 2;
    private static final int Y_PLANE = 0;
    private static ClearSightNativeEngine mInstance;
    private static boolean mLibLoaded;
    private final float mBrIntensity = PersistUtil.getDualCameraBrIntensity();
    private ArrayList<SourceImage> mCache = new ArrayList<>();
    private int mImageHeight;
    private int mImageWidth;
    private final boolean mIsVerticallyAlignedSensor = PersistUtil.getDualCameraSensorAlign();
    private byte[] mOtpCalibData;
    private Image mRefColorImage;
    private TotalCaptureResult mRefColorResult;
    private Image mRefMonoImage;
    private TotalCaptureResult mRefMonoResult;
    private final float mSmoothingIntensity = PersistUtil.getDualCameraSmoothingIntensity();
    private ArrayList<SourceImage> mSrcColor = new ArrayList<>();
    private ArrayList<SourceImage> mSrcMono = new ArrayList<>();
    private int mVUStride;
    private int mYStride;

    public static class CamSensorCalibrationData {
        short calibration_sensor_resolution_height;
        short calibration_sensor_resolution_width;
        float focal_length_ratio;
        short native_sensor_resolution_height;
        short native_sensor_resolution_width;
        float normalized_focal_length;

        private CamSensorCalibrationData() {
        }

        public static CamSensorCalibrationData createFromBytes(byte[] bArr) {
            return createFromByteBuff(ByteBuffer.wrap(bArr));
        }

        public static CamSensorCalibrationData createFromByteBuff(ByteBuffer byteBuffer) {
            CamSensorCalibrationData camSensorCalibrationData = new CamSensorCalibrationData();
            camSensorCalibrationData.normalized_focal_length = byteBuffer.getFloat();
            camSensorCalibrationData.native_sensor_resolution_width = byteBuffer.getShort();
            camSensorCalibrationData.native_sensor_resolution_height = byteBuffer.getShort();
            camSensorCalibrationData.calibration_sensor_resolution_width = byteBuffer.getShort();
            camSensorCalibrationData.calibration_sensor_resolution_height = byteBuffer.getShort();
            camSensorCalibrationData.focal_length_ratio = byteBuffer.getFloat();
            return camSensorCalibrationData;
        }
    }

    public static class CamSystemCalibrationData {
        private static final String[] CALIB_FMT_STRINGS = {"Calibration OTP format version = %d\n", "Main Native Sensor Resolution width = %dpx\n", "Main Native Sensor Resolution height = %dpx\n", "Main Calibration Resolution width = %dpx\n", "Main Calibration Resolution height = %dpx\n", "Main Focal length ratio = %f\n", "Aux Native Sensor Resolution width = %dpx\n", "Aux Native Sensor Resolution height = %dpx\n", "Aux Calibration Resolution width = %dpx\n", "Aux Calibration Resolution height = %dpx\n", "Aux Focal length ratio = %f\n", "Relative Rotation matrix [0] through [8] = %s\n", "Relative Geometric surface parameters [0] through [31] = %s\n", "Relative Principal point X axis offset (ox) = %fpx\n", "Relative Principal point Y axis offset (oy) = %fpx\n", "Relative position flag = %d\n", "Baseline distance = %fmm\n", "Main sensor mirror and flip setting = %d\n", "Aux sensor mirror and flip setting = %d\n", "Module orientation during calibration = %d\n", "Rotation flag = %d\n", "Main Normalized Focal length = %fpx\n", "Aux Normalized Focal length = %fpx"};
        CamSensorCalibrationData aux_cam_specific_calibration;
        short aux_sensor_mirror_and_flip_setting;
        int calibration_format_version;
        CamSensorCalibrationData main_cam_specific_calibration;
        short main_sensor_mirror_and_flip_setting;
        short module_orientation_during_calibration;
        float relative_baseline_distance;
        float[] relative_geometric_surface_parameters = new float[32];
        short relative_position_flag;
        float relative_principle_point_x_offset;
        float relative_principle_point_y_offset;
        float[] relative_rotation_matrix = new float[9];
        short rotation_flag;

        private CamSystemCalibrationData() {
        }

        public static CamSystemCalibrationData createFromBytes(byte[] bArr) {
            if (bArr == null) {
                return null;
            }
            ByteBuffer wrap = ByteBuffer.wrap(bArr);
            wrap.order(ByteOrder.LITTLE_ENDIAN);
            CamSystemCalibrationData createFromByteBuff = createFromByteBuff(wrap);
            if (ClearSightNativeEngine.DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("OTP Calib Data:");
                for (int i = 0; i < bArr.length; i++) {
                    if (i % 16 == 0) {
                        sb.append("\n");
                    }
                    sb.append(String.format("%02X ", new Object[]{Byte.valueOf(bArr[i])}));
                }
                String sb2 = sb.toString();
                String str = ClearSightNativeEngine.TAG;
                Log.d(str, sb2);
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Parsed OTP DATA:\n");
                sb3.append(createFromByteBuff.toString());
                Log.d(str, sb3.toString());
            }
            return createFromByteBuff;
        }

        public static CamSystemCalibrationData createFromByteBuff(ByteBuffer byteBuffer) {
            CamSystemCalibrationData camSystemCalibrationData = new CamSystemCalibrationData();
            camSystemCalibrationData.calibration_format_version = byteBuffer.getInt();
            camSystemCalibrationData.main_cam_specific_calibration = CamSensorCalibrationData.createFromByteBuff(byteBuffer);
            camSystemCalibrationData.aux_cam_specific_calibration = CamSensorCalibrationData.createFromByteBuff(byteBuffer);
            for (int i = 0; i < 9; i++) {
                camSystemCalibrationData.relative_rotation_matrix[i] = byteBuffer.getFloat();
            }
            for (int i2 = 0; i2 < 32; i2++) {
                camSystemCalibrationData.relative_geometric_surface_parameters[i2] = byteBuffer.getFloat();
            }
            camSystemCalibrationData.relative_principle_point_x_offset = byteBuffer.getFloat();
            camSystemCalibrationData.relative_principle_point_y_offset = byteBuffer.getFloat();
            camSystemCalibrationData.relative_position_flag = byteBuffer.getShort();
            camSystemCalibrationData.relative_baseline_distance = byteBuffer.getFloat();
            camSystemCalibrationData.main_sensor_mirror_and_flip_setting = byteBuffer.getShort();
            camSystemCalibrationData.aux_sensor_mirror_and_flip_setting = byteBuffer.getShort();
            camSystemCalibrationData.module_orientation_during_calibration = byteBuffer.getShort();
            camSystemCalibrationData.rotation_flag = byteBuffer.getShort();
            return camSystemCalibrationData;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(CALIB_FMT_STRINGS[0], new Object[]{Integer.valueOf(this.calibration_format_version)}));
            sb.append(String.format(CALIB_FMT_STRINGS[1], new Object[]{Short.valueOf(this.main_cam_specific_calibration.native_sensor_resolution_width)}));
            sb.append(String.format(CALIB_FMT_STRINGS[2], new Object[]{Short.valueOf(this.main_cam_specific_calibration.native_sensor_resolution_height)}));
            sb.append(String.format(CALIB_FMT_STRINGS[3], new Object[]{Short.valueOf(this.main_cam_specific_calibration.calibration_sensor_resolution_width)}));
            sb.append(String.format(CALIB_FMT_STRINGS[4], new Object[]{Short.valueOf(this.main_cam_specific_calibration.calibration_sensor_resolution_height)}));
            sb.append(String.format(CALIB_FMT_STRINGS[5], new Object[]{Float.valueOf(this.main_cam_specific_calibration.focal_length_ratio)}));
            sb.append(String.format(CALIB_FMT_STRINGS[6], new Object[]{Short.valueOf(this.aux_cam_specific_calibration.native_sensor_resolution_width)}));
            sb.append(String.format(CALIB_FMT_STRINGS[7], new Object[]{Short.valueOf(this.aux_cam_specific_calibration.native_sensor_resolution_height)}));
            sb.append(String.format(CALIB_FMT_STRINGS[8], new Object[]{Short.valueOf(this.aux_cam_specific_calibration.calibration_sensor_resolution_width)}));
            sb.append(String.format(CALIB_FMT_STRINGS[9], new Object[]{Short.valueOf(this.aux_cam_specific_calibration.calibration_sensor_resolution_height)}));
            sb.append(String.format(CALIB_FMT_STRINGS[10], new Object[]{Float.valueOf(this.aux_cam_specific_calibration.focal_length_ratio)}));
            sb.append(String.format(CALIB_FMT_STRINGS[11], new Object[]{buildCommaSeparatedString(this.relative_rotation_matrix)}));
            sb.append(String.format(CALIB_FMT_STRINGS[12], new Object[]{buildCommaSeparatedString(this.relative_geometric_surface_parameters)}));
            sb.append(String.format(CALIB_FMT_STRINGS[13], new Object[]{Float.valueOf(this.relative_principle_point_x_offset)}));
            sb.append(String.format(CALIB_FMT_STRINGS[14], new Object[]{Float.valueOf(this.relative_principle_point_y_offset)}));
            sb.append(String.format(CALIB_FMT_STRINGS[15], new Object[]{Short.valueOf(this.relative_position_flag)}));
            sb.append(String.format(CALIB_FMT_STRINGS[16], new Object[]{Float.valueOf(this.relative_baseline_distance)}));
            sb.append(String.format(CALIB_FMT_STRINGS[17], new Object[]{Short.valueOf(this.main_sensor_mirror_and_flip_setting)}));
            sb.append(String.format(CALIB_FMT_STRINGS[18], new Object[]{Short.valueOf(this.aux_sensor_mirror_and_flip_setting)}));
            sb.append(String.format(CALIB_FMT_STRINGS[19], new Object[]{Short.valueOf(this.module_orientation_during_calibration)}));
            sb.append(String.format(CALIB_FMT_STRINGS[20], new Object[]{Short.valueOf(this.rotation_flag)}));
            sb.append(String.format(CALIB_FMT_STRINGS[21], new Object[]{Float.valueOf(this.main_cam_specific_calibration.normalized_focal_length)}));
            sb.append(String.format(CALIB_FMT_STRINGS[22], new Object[]{Float.valueOf(this.aux_cam_specific_calibration.normalized_focal_length)}));
            return sb.toString();
        }

        private String buildCommaSeparatedString(float[] fArr) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%f", new Object[]{Float.valueOf(fArr[0])}));
            for (int i = 1; i < fArr.length; i++) {
                sb.append(String.format(",%f", new Object[]{Float.valueOf(fArr[i])}));
            }
            return sb.toString();
        }
    }

    public static class ClearsightImage {
        private Image mImage;
        private Rect mRoiRect;

        ClearsightImage(Image image) {
            this.mImage = image;
        }

        public ByteBuffer getYBuffer() {
            return this.mImage.getPlanes()[0].getBuffer();
        }

        public ByteBuffer getVUBuffer() {
            return this.mImage.getPlanes()[2].getBuffer();
        }

        public void setRoiRect(int[] iArr) {
            this.mRoiRect = new Rect(iArr[0], iArr[1], iArr[0] + iArr[2], iArr[1] + iArr[3]);
            this.mImage.setCropRect(this.mRoiRect);
        }

        public Rect getRoiRect() {
            return this.mRoiRect;
        }
    }

    private class SourceImage {
        float[] mMetadata = new float[6];
        ByteBuffer mVU;

        /* renamed from: mY */
        ByteBuffer f116mY;

        SourceImage(int i, int i2) {
            this.f116mY = ByteBuffer.allocateDirect(i);
            this.mVU = ByteBuffer.allocateDirect(i2);
        }
    }

    private final native boolean nativeClearSightProcess(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int[] iArr);

    private final native boolean nativeClearSightProcessInit2(int i, ByteBuffer[] byteBufferArr, ByteBuffer[] byteBufferArr2, float[][] fArr, int i2, int i3, int i4, int i5, ByteBuffer[] byteBufferArr3, float[][] fArr2, int i6, int i7, int i8, byte[] bArr, int i9, int i10, float f, float f2, boolean z);

    private final native boolean nativeClearSightRegisterImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, ByteBuffer byteBuffer3, int i, int i2, int i3, int i4, ByteBuffer byteBuffer4, ByteBuffer byteBuffer5, float[] fArr);

    static {
        String str = TAG;
        try {
            System.loadLibrary("jni_clearsight");
            mLibLoaded = true;
            Log.v(str, "successfully loaded clearsight lib");
        } catch (UnsatisfiedLinkError e) {
            Log.e(str, "failed to load clearsight lib");
            e.printStackTrace();
            mLibLoaded = false;
        }
    }

    private ClearSightNativeEngine() {
    }

    public static void createInstance() {
        if (mInstance == null) {
            mInstance = new ClearSightNativeEngine();
        }
    }

    public static ClearSightNativeEngine getInstance() {
        createInstance();
        return mInstance;
    }

    public void init(int i, int i2, int i3, CamSystemCalibrationData camSystemCalibrationData) {
        String camSystemCalibrationData2 = camSystemCalibrationData.toString();
        StringBuilder sb = new StringBuilder();
        sb.append("OTP calibration data: \n");
        sb.append(camSystemCalibrationData2);
        Log.d(TAG, sb.toString());
        this.mOtpCalibData = camSystemCalibrationData2.getBytes();
        this.mImageWidth = i2;
        this.mImageHeight = i3;
        this.mYStride = i2;
        this.mVUStride = i2;
        while (i > 0) {
            int i4 = i2 * i3;
            cacheSourceImage(new SourceImage(i4, i4 / 2));
            i--;
        }
    }

    public void close() {
        reset();
        this.mCache.clear();
        this.mImageWidth = 0;
        this.mImageHeight = 0;
        this.mYStride = 0;
        this.mVUStride = 0;
    }

    public boolean isLibLoaded() {
        return mLibLoaded;
    }

    public void reset() {
        while (!this.mSrcColor.isEmpty()) {
            cacheSourceImage((SourceImage) this.mSrcColor.remove(0));
        }
        while (!this.mSrcMono.isEmpty()) {
            cacheSourceImage((SourceImage) this.mSrcMono.remove(0));
        }
        setReferenceColorImage(null);
        setReferenceMonoImage(null);
        setReferenceColorResult(null);
        setReferenceMonoResult(null);
    }

    private SourceImage getNewSourceImage() {
        StringBuilder sb = new StringBuilder();
        sb.append("getNewSourceImage: ");
        sb.append(this.mCache.size());
        Log.d(TAG, sb.toString());
        return (SourceImage) this.mCache.remove(0);
    }

    private void cacheSourceImage(SourceImage sourceImage) {
        this.mCache.add(sourceImage);
        StringBuilder sb = new StringBuilder();
        sb.append("cacheSourceImage: ");
        sb.append(this.mCache.size());
        Log.d(TAG, sb.toString());
    }

    public void setReferenceResult(boolean z, TotalCaptureResult totalCaptureResult) {
        if (z) {
            setReferenceColorResult(totalCaptureResult);
        } else {
            setReferenceMonoResult(totalCaptureResult);
        }
    }

    private void setReferenceColorResult(TotalCaptureResult totalCaptureResult) {
        this.mRefColorResult = totalCaptureResult;
    }

    private void setReferenceMonoResult(TotalCaptureResult totalCaptureResult) {
        this.mRefMonoResult = totalCaptureResult;
    }

    public void setReferenceImage(boolean z, Image image) {
        if (z) {
            setReferenceColorImage(image);
        } else {
            setReferenceMonoImage(image);
        }
    }

    private void setReferenceColorImage(Image image) {
        Image image2 = this.mRefColorImage;
        if (image2 != null) {
            image2.close();
            this.mRefColorImage = null;
        }
        this.mRefColorImage = image;
        if (this.mRefColorImage != null) {
            Log.d(TAG, "setRefColorImage");
            Plane[] planes = this.mRefColorImage.getPlanes();
            SourceImage newSourceImage = getNewSourceImage();
            ByteBuffer buffer = planes[0].getBuffer();
            ByteBuffer buffer2 = planes[2].getBuffer();
            buffer.rewind();
            buffer2.rewind();
            newSourceImage.f116mY.rewind();
            newSourceImage.f116mY.put(buffer);
            newSourceImage.mVU.rewind();
            newSourceImage.mVU.put(buffer2);
            this.mSrcColor.add(newSourceImage);
        }
    }

    private void setReferenceMonoImage(Image image) {
        Image image2 = this.mRefMonoImage;
        if (image2 != null) {
            image2.close();
            this.mRefMonoImage = null;
        }
        this.mRefMonoImage = image;
        if (this.mRefMonoImage != null) {
            Log.d(TAG, "setRefMonoImage");
            Plane[] planes = this.mRefMonoImage.getPlanes();
            SourceImage newSourceImage = getNewSourceImage();
            ByteBuffer buffer = planes[0].getBuffer();
            buffer.rewind();
            newSourceImage.f116mY.rewind();
            newSourceImage.f116mY.put(buffer);
            this.mSrcMono.add(newSourceImage);
        }
    }

    public boolean hasReferenceImage(boolean z) {
        return getImageCount(z) > 0;
    }

    public int getImageCount(boolean z) {
        return (z ? this.mSrcColor : this.mSrcMono).size();
    }

    public Image getReferenceImage(boolean z) {
        return z ? this.mRefColorImage : this.mRefMonoImage;
    }

    public TotalCaptureResult getReferenceResult(boolean z) {
        return z ? this.mRefColorResult : this.mRefMonoResult;
    }

    public boolean registerImage(boolean z, Image image) {
        ByteBuffer byteBuffer;
        int i;
        ByteBuffer byteBuffer2;
        List list = z ? this.mSrcColor : this.mSrcMono;
        if (list.isEmpty()) {
            Log.w(TAG, "reference image not yet set");
            return false;
        }
        SourceImage newSourceImage = getNewSourceImage();
        SourceImage sourceImage = (SourceImage) list.get(0);
        Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        ByteBuffer byteBuffer3 = sourceImage.f116mY;
        ByteBuffer byteBuffer4 = newSourceImage.f116mY;
        int rowStride = planes[0].getRowStride();
        if (z) {
            byteBuffer2 = planes[2].getBuffer();
            byteBuffer = newSourceImage.mVU;
            i = planes[2].getRowStride();
        } else {
            i = 0;
            byteBuffer2 = null;
            byteBuffer = null;
        }
        boolean nativeClearSightRegisterImage = nativeClearSightRegisterImage(byteBuffer3, buffer, byteBuffer2, this.mImageWidth, this.mImageHeight, rowStride, i, byteBuffer4, byteBuffer, newSourceImage.mMetadata);
        if (nativeClearSightRegisterImage) {
            list.add(newSourceImage);
        }
        image.close();
        return nativeClearSightRegisterImage;
    }

    public boolean initProcessImage() {
        int size = this.mSrcColor.size();
        int size2 = this.mSrcMono.size();
        String str = TAG;
        if (size != size2) {
            StringBuilder sb = new StringBuilder();
            sb.append("processImage - numImages mismatch - bayer: ");
            sb.append(this.mSrcColor.size());
            sb.append(", mono: ");
            sb.append(this.mSrcMono.size());
            Log.d(str, sb.toString());
            return false;
        }
        int size3 = this.mSrcColor.size();
        ByteBuffer[] byteBufferArr = new ByteBuffer[size3];
        ByteBuffer[] byteBufferArr2 = new ByteBuffer[size3];
        float[][] fArr = new float[size3][];
        ByteBuffer[] byteBufferArr3 = new ByteBuffer[size3];
        float[][] fArr2 = new float[size3][];
        StringBuilder sb2 = new StringBuilder();
        sb2.append("processImage - num Images: ");
        sb2.append(size3);
        Log.d(str, sb2.toString());
        for (int i = 0; i < size3; i++) {
            SourceImage sourceImage = (SourceImage) this.mSrcColor.get(i);
            SourceImage sourceImage2 = (SourceImage) this.mSrcMono.get(i);
            byteBufferArr[i] = sourceImage.f116mY;
            byteBufferArr2[i] = sourceImage.mVU;
            fArr[i] = sourceImage.mMetadata;
            byteBufferArr3[i] = sourceImage2.f116mY;
            fArr2[i] = sourceImage2.mMetadata;
        }
        int intValue = ((Integer) this.mRefMonoResult.get(CaptureResult.SENSOR_SENSITIVITY)).intValue();
        long longValue = ((Long) this.mRefMonoResult.get(CaptureResult.SENSOR_EXPOSURE_TIME)).longValue() / 100000;
        StringBuilder sb3 = new StringBuilder();
        sb3.append("processImage - iso: ");
        sb3.append(intValue);
        sb3.append(" exposure ms: ");
        sb3.append(longValue);
        Log.d(str, sb3.toString());
        if (DEBUG) {
            StringBuilder sb4 = new StringBuilder();
            sb4.append("processImage - mBrIntensity :");
            sb4.append(this.mBrIntensity);
            sb4.append(", mSmoothingIntensity :");
            sb4.append(this.mSmoothingIntensity);
            sb4.append(", mIsVerticallyAlignedSensor :");
            sb4.append(this.mIsVerticallyAlignedSensor);
            Log.d(str, sb4.toString());
        }
        int i2 = this.mImageWidth;
        int i3 = i2;
        int i4 = this.mImageHeight;
        int i5 = i4;
        int i6 = this.mYStride;
        int i7 = intValue;
        long j = longValue;
        float[][] fArr3 = fArr2;
        return nativeClearSightProcessInit2(size3, byteBufferArr, byteBufferArr2, fArr, i3, i5, i6, this.mVUStride, byteBufferArr3, fArr3, i2, i4, i6, this.mOtpCalibData, (int) j, i7, this.mBrIntensity, this.mSmoothingIntensity, this.mIsVerticallyAlignedSensor);
    }

    public boolean processImage(ClearsightImage clearsightImage) {
        ByteBuffer yBuffer = clearsightImage.getYBuffer();
        ByteBuffer vUBuffer = clearsightImage.getVUBuffer();
        int[] iArr = new int[4];
        StringBuilder sb = new StringBuilder();
        sb.append("processImage - dst size - y: ");
        sb.append(yBuffer.capacity());
        sb.append(" vu: ");
        sb.append(vUBuffer.capacity());
        String sb2 = sb.toString();
        String str = TAG;
        Log.d(str, sb2);
        boolean nativeClearSightProcess = nativeClearSightProcess(yBuffer, vUBuffer, this.mYStride, this.mVUStride, iArr);
        clearsightImage.setRoiRect(iArr);
        StringBuilder sb3 = new StringBuilder();
        sb3.append("processImage - roiRect: ");
        sb3.append(clearsightImage.getRoiRect().toString());
        Log.d(str, sb3.toString());
        return nativeClearSightProcess;
    }
}
