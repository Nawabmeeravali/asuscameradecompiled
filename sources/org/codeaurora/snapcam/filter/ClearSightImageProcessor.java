package org.codeaurora.snapcam.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCaptureSession.StateCallback;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.ImageWriter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.util.SparseLongArray;
import android.view.Surface;
import com.android.camera.Exif;
import com.android.camera.MediaSaveService;
import com.android.camera.MediaSaveService.OnMediaSavedListener;
import com.android.camera.PhotoModule;
import com.android.camera.PhotoModule.NamedImages;
import com.android.camera.PhotoModule.NamedImages.NamedEntity;
import com.android.camera.SettingsManager;
import com.android.camera.Storage;
import com.android.camera.exif.ExifInterface;
import com.android.camera.util.CameraUtil.CompareSizesByArea;
import com.android.camera.util.PersistUtil;
import com.android.camera.util.VendorTagUtil;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.codeaurora.snapcam.filter.ClearSightNativeEngine.CamSystemCalibrationData;
import org.codeaurora.snapcam.filter.ClearSightNativeEngine.ClearsightImage;
import org.codeaurora.snapcam.filter.GDepth.DepthMap;

public class ClearSightImageProcessor {
    private static final int CAM_TYPE_BAYER = 0;
    private static final int CAM_TYPE_MONO = 1;
    private static final int DEFAULT_CS_TIMEOUT_MS = 300;
    private static final int DEFAULT_IMAGES_TO_BURST = 4;
    private static final long DEFAULT_TIMESTAMP_THRESHOLD_MS = 10;
    private static final long MIN_MONO_AREA = 1900000;
    private static final Size[] MONO_SIZES = {new Size(1600, 1200), new Size(1920, 1080), new Size(1400, 1400)};
    private static final int MSG_CALIBRATION_DATA = 7;
    private static final int MSG_END_CAPTURE = 6;
    private static final int MSG_NEW_CAPTURE_FAIL = 3;
    private static final int MSG_NEW_CAPTURE_RESULT = 2;
    private static final int MSG_NEW_DEPTH = 9;
    private static final int MSG_NEW_IMG = 1;
    private static final int MSG_NEW_LENS_FOCUS_DISTANCE_BAYER = 8;
    private static final int MSG_NEW_REPROC_FAIL = 5;
    private static final int MSG_NEW_REPROC_RESULT = 4;
    private static final int MSG_START_CAPTURE = 0;
    private static final int NUM_CAM = 2;
    private static Key<byte[]> OTP_CALIB_BLOB = new Key<>("org.codeaurora.qcamera3.dualcam_calib_meta_data.dualcam_calib_meta_data_blob", byte[].class);
    private static final String TAG = "ClearSightImageProcessor";
    private static ClearSightImageProcessor mInstance;
    private CaptureResult.Key<byte[]> SCALE_CROP_ROTATION_REPROCESS_BLOB = new CaptureResult.Key<>("org.codeaurora.qcamera3.hal_private_data.reprocess_data_blob", byte[].class);
    /* access modifiers changed from: private */
    public Callback mCallback;
    /* access modifiers changed from: private */
    public CameraCaptureSession[] mCaptureSessions = new CameraCaptureSession[2];
    /* access modifiers changed from: private */
    public ClearsightProcessHandler mClearsightProcessHandler;
    private HandlerThread mClearsightProcessThread;
    /* access modifiers changed from: private */
    public ClearsightRegisterHandler mClearsightRegisterHandler;
    private HandlerThread mClearsightRegisterThread;
    /* access modifiers changed from: private */
    public int mCsTimeout;
    /* access modifiers changed from: private */
    public DepthProcessHandler mDepthProcessHandler;
    private HandlerThread mDepthProcessThread;
    /* access modifiers changed from: private */
    public boolean mDumpDepth;
    /* access modifiers changed from: private */
    public boolean mDumpImages;
    /* access modifiers changed from: private */
    public boolean mDumpYUV;
    /* access modifiers changed from: private */
    public ImageReader[] mEncodeImageReader = new ImageReader[2];
    /* access modifiers changed from: private */
    public Size mFinalMonoSize;
    private float mFinalPictureRatio;
    /* access modifiers changed from: private */
    public Size mFinalPictureSize;
    /* access modifiers changed from: private */
    public int mFinishReprocessNum;
    /* access modifiers changed from: private */
    public ImageEncodeHandler mImageEncodeHandler;
    private HandlerThread mImageEncodeThread;
    /* access modifiers changed from: private */
    public ImageProcessHandler mImageProcessHandler;
    private HandlerThread mImageProcessThread;
    /* access modifiers changed from: private */
    public ImageReader[] mImageReader = new ImageReader[2];
    /* access modifiers changed from: private */
    public ImageWriter[] mImageWriter = new ImageWriter[2];
    private boolean mIsClosing;
    /* access modifiers changed from: private */
    public MediaSaveService mMediaSaveService;
    /* access modifiers changed from: private */
    public OnMediaSavedListener mMediaSavedListener;
    /* access modifiers changed from: private */
    public NamedImages mNamedImages = new NamedImages();
    /* access modifiers changed from: private */
    public int mNumBurstCount;
    /* access modifiers changed from: private */
    public int mNumFrameCount;
    /* access modifiers changed from: private */
    public long mTimestampThresholdNs = (PersistUtil.getTimestampLimit() * 1000000);

    public interface Callback {
        void onClearSightFailure(byte[] bArr);

        void onClearSightSuccess(byte[] bArr);

        void onReleaseShutterLock();
    }

    private class ClearsightProcessHandler extends Handler {
        ClearsightProcessHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (!ClearSightImageProcessor.this.isClosing() && message.what == 0) {
                processClearSight((NamedEntity) message.obj);
            }
        }

        private void processClearSight(NamedEntity namedEntity) {
            if (ClearSightNativeEngine.getInstance().getReferenceImage(true) != null) {
                long timestamp = ClearSightNativeEngine.getInstance().getReferenceImage(true).getTimestamp();
                Builder createEncodeReprocRequest = createEncodeReprocRequest(ClearSightNativeEngine.getInstance().getReferenceResult(true), 0);
                createEncodeReprocRequest.setTag(new Object());
                boolean initProcessImage = ClearSightNativeEngine.getInstance().initProcessImage();
                sendReferenceMonoEncodeRequest();
                sendReferenceBayerEncodeRequest();
                short s = (short) 6;
                ClearSightNativeEngine.getInstance().reset();
                if (initProcessImage) {
                    Image dequeueInputImage = ClearSightImageProcessor.this.mImageWriter[0].dequeueInputImage();
                    ClearsightImage clearsightImage = new ClearsightImage(dequeueInputImage);
                    dequeueInputImage.setTimestamp(timestamp);
                    if (ClearSightNativeEngine.getInstance().processImage(clearsightImage)) {
                        s = (short) (s | 1);
                        sendReprocessRequest(createEncodeReprocRequest, dequeueInputImage, 0);
                    } else {
                        dequeueInputImage.close();
                    }
                }
                ClearSightImageProcessor.this.mImageEncodeHandler.obtainMessage(6, s, 0, namedEntity).sendToTarget();
            }
        }

        private void sendReferenceMonoEncodeRequest() {
            sendReprocessRequest(createEncodeReprocRequest(ClearSightNativeEngine.getInstance().getReferenceResult(false), 1), ClearSightNativeEngine.getInstance().getReferenceImage(false), 1);
        }

        private void sendReferenceBayerEncodeRequest() {
            sendReprocessRequest(createEncodeReprocRequest(ClearSightNativeEngine.getInstance().getReferenceResult(true), 0), ClearSightNativeEngine.getInstance().getReferenceImage(true), 0);
        }

        private Builder createEncodeReprocRequest(TotalCaptureResult totalCaptureResult, int i) {
            Builder builder = null;
            try {
                builder = ClearSightImageProcessor.this.mCaptureSessions[i].getDevice().createReprocessCaptureRequest(totalCaptureResult);
                builder.addTarget(ClearSightImageProcessor.this.mEncodeImageReader[i].getSurface());
                builder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, Integer.valueOf(0));
                builder.set(CaptureRequest.EDGE_MODE, Integer.valueOf(0));
                builder.set(CaptureRequest.NOISE_REDUCTION_MODE, Integer.valueOf(0));
                return builder;
            } catch (CameraAccessException e) {
                e.printStackTrace();
                return builder;
            }
        }

        private void sendReprocessRequest(Builder builder, Image image, final int i) {
            try {
                VendorTagUtil.setJpegCropEnable(builder, Byte.valueOf(1));
                Rect cropRect = image.getCropRect();
                if (cropRect == null || cropRect.isEmpty()) {
                    cropRect = new Rect(0, 0, image.getWidth(), image.getHeight());
                }
                Rect access$2000 = ClearSightImageProcessor.this.getFinalCropRect(cropRect);
                VendorTagUtil.setJpegCropRect(builder, new int[]{access$2000.left, access$2000.top, access$2000.width(), access$2000.height()});
                if (i == 1) {
                    VendorTagUtil.setJpegRoiRect(builder, new int[]{0, 0, ClearSightImageProcessor.this.mFinalMonoSize.getWidth(), ClearSightImageProcessor.this.mFinalMonoSize.getHeight()});
                } else {
                    VendorTagUtil.setJpegRoiRect(builder, new int[]{0, 0, ClearSightImageProcessor.this.mFinalPictureSize.getWidth(), ClearSightImageProcessor.this.mFinalPictureSize.getHeight()});
                }
                ClearSightImageProcessor.this.mImageWriter[i].queueInputImage(image);
                ClearSightImageProcessor.this.mCaptureSessions[i].capture(builder.build(), new CaptureCallback() {
                    public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
                        super.onCaptureCompleted(cameraCaptureSession, captureRequest, totalCaptureResult);
                        StringBuilder sb = new StringBuilder();
                        sb.append("encode - onCaptureCompleted: ");
                        sb.append(i);
                        Log.d(ClearSightImageProcessor.TAG, sb.toString());
                        ClearSightImageProcessor.this.mImageEncodeHandler.obtainMessage(2, i, 0, totalCaptureResult).sendToTarget();
                    }

                    public void onCaptureFailed(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureFailure captureFailure) {
                        super.onCaptureFailed(cameraCaptureSession, captureRequest, captureFailure);
                        StringBuilder sb = new StringBuilder();
                        sb.append("encode - onCaptureFailed: ");
                        sb.append(i);
                        Log.d(ClearSightImageProcessor.TAG, sb.toString());
                        ClearSightImageProcessor.this.mImageEncodeHandler.obtainMessage(3, i, 0, captureFailure).sendToTarget();
                    }
                }, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (IllegalStateException e2) {
                e2.printStackTrace();
            }
        }
    }

    private class ClearsightRegisterHandler extends Handler {
        private NamedEntity mNamedEntity;

        ClearsightRegisterHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (!ClearSightImageProcessor.this.isClosing()) {
                int i = message.what;
                if (i == 0) {
                    this.mNamedEntity = (NamedEntity) message.obj;
                } else if (i == 1) {
                    registerImage(message);
                } else if (i == 6) {
                    if (message.arg2 == 1) {
                        Log.d(ClearSightImageProcessor.TAG, "ClearsightRegisterHandler - handleTimeout");
                        ClearSightNativeEngine.getInstance().reset();
                        if (ClearSightImageProcessor.this.mCallback != null) {
                            ClearSightImageProcessor.this.mCallback.onClearSightFailure(null);
                        }
                    } else {
                        ClearSightImageProcessor.this.mClearsightProcessHandler.obtainMessage(0, message.arg1, 0, this.mNamedEntity).sendToTarget();
                    }
                }
            }
        }

        private void registerImage(Message message) {
            boolean z = message.arg1 == 0;
            Image image = (Image) message.obj;
            if (!ClearSightNativeEngine.getInstance().hasReferenceImage(z)) {
                ClearSightNativeEngine.getInstance().setReferenceImage(z, image);
                ClearSightImageProcessor.this.mDepthProcessHandler.obtainMessage(1, message.arg1, 0, message.obj).sendToTarget();
            } else if (!ClearSightNativeEngine.getInstance().registerImage(z, image)) {
                Log.w(ClearSightImageProcessor.TAG, "registerImage : terminal error with input image");
            }
        }
    }

    private class DepthProcessHandler extends Handler {
        private DDMNativeEngine mDDMNativeEngine = new DDMNativeEngine();
        private TotalCaptureResult mReprocessCaptureResult;

        public DepthProcessHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 0) {
                resetParams();
            } else if (i == 1) {
                registerImage(message);
            } else if (i == 4) {
                registerReprocessResult(message);
            } else if (i == 7) {
                setCalibrationdata(message);
            } else if (i == 8) {
                setBayerLensFocusDistance(message);
            }
        }

        private void setCalibrationdata(Message message) {
            this.mDDMNativeEngine.setCamSystemCalibrationData((CamSystemCalibrationData) message.obj);
        }

        private void resetParams() {
            Log.d(ClearSightImageProcessor.TAG, "resetParams");
            this.mDDMNativeEngine.reset();
        }

        private void setBayerLensFocusDistance(Message message) {
            this.mDDMNativeEngine.setBayerLensFocusDistance(((Float) message.obj).floatValue());
        }

        private void registerImage(Message message) {
            boolean z = message.arg1 == 0;
            Image image = (Image) message.obj;
            if (z) {
                this.mDDMNativeEngine.setBayerImage(image);
            } else {
                this.mDDMNativeEngine.setMonoImage(image);
            }
            if (this.mDDMNativeEngine.isReadyForGenerateDepth()) {
                generateDepthmap();
            }
        }

        private void registerReprocessResult(Message message) {
            boolean z = message.arg1 == 0;
            StringBuilder sb = new StringBuilder();
            sb.append("registerReprocessResult bayer=");
            sb.append(z);
            Log.d(ClearSightImageProcessor.TAG, sb.toString());
            TotalCaptureResult totalCaptureResult = (TotalCaptureResult) message.obj;
            if (z) {
                this.mDDMNativeEngine.setBayerReprocessResult(totalCaptureResult);
            } else {
                this.mDDMNativeEngine.setMonoReprocessResult(totalCaptureResult);
            }
            if (this.mDDMNativeEngine.isReadyForGenerateDepth()) {
                generateDepthmap();
            }
        }

        private void generateDepthmap() {
            ClearSightImageProcessor.this.mImageEncodeHandler.obtainMessage(0).sendToTarget();
            int[] iArr = new int[2];
            boolean depthMapSize = this.mDDMNativeEngine.getDepthMapSize(iArr);
            String str = ClearSightImageProcessor.TAG;
            DepthMap depthMap = null;
            if (depthMapSize) {
                int i = iArr[0];
                int i2 = iArr[1];
                int rowBytes = Bitmap.createBitmap(i, i2, Config.ALPHA_8).getRowBytes();
                byte[] bArr = new byte[(rowBytes * i2)];
                StringBuilder sb = new StringBuilder();
                sb.append("depthMapWidth=");
                sb.append(i);
                sb.append(" depthMapHeight=");
                sb.append(i2);
                sb.append(" stride=");
                sb.append(rowBytes);
                Log.d(str, sb.toString());
                Rect rect = new Rect();
                if (this.mDDMNativeEngine.dualCameraGenerateDDM(bArr, rowBytes, rect)) {
                    if (ClearSightImageProcessor.this.mDumpDepth) {
                        ClearSightImageProcessor.this.saveAsRGB(bArr, i, i2);
                    }
                    DepthMap depthMap2 = new DepthMap(i, i2);
                    depthMap2.roi = rect;
                    depthMap2.buffer = bArr;
                    depthMap = depthMap2;
                } else {
                    Log.e(str, "dualCameraGenerateDDM failure");
                }
            } else {
                Log.e(str, "getDepthMapSize failure");
            }
            if (ClearSightImageProcessor.this.mDumpDepth) {
                dumpCameraParam();
            }
            ClearSightImageProcessor.this.mImageEncodeHandler.obtainMessage(9, 0, 0, depthMap).sendToTarget();
        }

        private void dumpCameraParam() {
            String str = "txt";
            ClearSightImageProcessor.this.saveToFile(this.mDDMNativeEngine.getOTPCalibration().getBytes(), "OTPdata", str);
            ClearSightImageProcessor.this.saveToFile(this.mDDMNativeEngine.getBayerScaleCrop().getBytes(), "BayerScaleCrop", str);
            ClearSightImageProcessor.this.saveToFile(this.mDDMNativeEngine.getMonoScaleCrop().getBytes(), "MonoScaleCrop", str);
        }
    }

    private class ImageEncodeHandler extends Handler {
        private long CLEAR_SIGHT_IMAGE_SAVE_DELAY = 500;
        private Image mBayerImage;
        private Image mClearSightImage;
        private boolean mClearSightReady;
        private DepthMap mDepthMap;
        private boolean mDepthMapReady;
        private short mEncodeRequest;
        private short mEncodeResults;
        private GImage mGImage;
        private boolean mHasFailure;
        private Image mMonoImage;
        private NamedEntity mNamedEntity;

        public ImageEncodeHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (!ClearSightImageProcessor.this.isClosing()) {
                int i = message.what;
                String str = ClearSightImageProcessor.TAG;
                if (i == 0) {
                    Log.d(str, "ImageEncodeEvent - START_CAPTURE");
                    resetParams();
                } else if (i == 1 || i == 2 || i == 3) {
                    processNewEvent(message);
                    saveClearSightImage();
                } else if (i == 6) {
                    Log.d(str, "ImageEncodeEvent - END_CAPTURE");
                    this.mNamedEntity = (NamedEntity) message.obj;
                    this.mEncodeRequest = (short) message.arg1;
                    this.mClearSightReady = true;
                    saveClearSightImage();
                } else if (i == 9) {
                    processNewGDepth(message);
                    saveClearSightImage();
                }
            }
        }

        private void processNewEvent(Message message) {
            int i = message.what;
            String str = ClearSightImageProcessor.TAG;
            if (i == 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("processNewEncodeEvent - newImg: ");
                sb.append(message.arg1);
                Log.d(str, sb.toString());
                String str2 = "jpg";
                if (message.arg1 == 1) {
                    this.mMonoImage = (Image) message.obj;
                    this.mEncodeResults = (short) (this.mEncodeResults | 4);
                    if (ClearSightImageProcessor.this.mDumpDepth) {
                        ClearSightImageProcessor clearSightImageProcessor = ClearSightImageProcessor.this;
                        clearSightImageProcessor.saveToFile(clearSightImageProcessor.getJpegData(this.mMonoImage), "mono", str2);
                    }
                } else if (this.mBayerImage == null) {
                    this.mBayerImage = (Image) message.obj;
                    this.mEncodeResults = (short) (this.mEncodeResults | 2);
                    this.mGImage = new GImage(ClearSightImageProcessor.this.getJpegData(this.mBayerImage), "image/jpeg");
                    if (ClearSightImageProcessor.this.mDumpDepth) {
                        ClearSightImageProcessor clearSightImageProcessor2 = ClearSightImageProcessor.this;
                        clearSightImageProcessor2.saveToFile(clearSightImageProcessor2.getJpegData(this.mBayerImage), "bayer", str2);
                    }
                } else {
                    this.mClearSightImage = (Image) message.obj;
                    this.mEncodeResults = (short) (this.mEncodeResults | 1);
                }
            } else if (i == 2) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("processNewEncodeEvent - newResult: ");
                sb2.append(message.arg1);
                Log.d(str, sb2.toString());
            } else {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("processNewEncodeEvent - newFailure: ");
                sb3.append(message.arg1);
                Log.d(str, sb3.toString());
                this.mHasFailure = true;
                if (message.arg1 == 1) {
                    this.mEncodeResults = (short) (this.mEncodeResults | 4);
                } else if (((CaptureFailure) message.obj).getRequest().getTag() != null) {
                    this.mEncodeResults = (short) (this.mEncodeResults | 1);
                } else {
                    this.mEncodeResults = (short) (this.mEncodeResults | 2);
                }
            }
        }

        private void processNewGDepth(Message message) {
            this.mDepthMap = (DepthMap) message.obj;
            this.mDepthMapReady = true;
        }

        private void saveClearSightImage() {
            int i;
            int i2;
            boolean isReadyToSave = isReadyToSave();
            String str = ClearSightImageProcessor.TAG;
            if (!isReadyToSave || this.mEncodeRequest != this.mEncodeResults) {
                Log.d(str, "saveClearSightImage - not yet ready to save");
            } else if (this.mHasFailure) {
                Log.d(str, "saveClearSightImage has failure - aborting.");
                if (ClearSightImageProcessor.this.mCallback != null) {
                    ClearSightImageProcessor.this.mCallback.onClearSightFailure(null);
                }
                resetParams();
            } else {
                Log.d(str, "saveClearSightImage");
                byte[] jpegData = ClearSightImageProcessor.this.getJpegData(this.mClearSightImage);
                NamedEntity namedEntity = this.mNamedEntity;
                String str2 = namedEntity == null ? null : namedEntity.title;
                NamedEntity namedEntity2 = this.mNamedEntity;
                long j = namedEntity2 == null ? -1 : namedEntity2.date;
                Image image = this.mBayerImage;
                if (image != null) {
                    image.getWidth();
                    this.mBayerImage.getHeight();
                }
                Image image2 = this.mClearSightImage;
                if (image2 != null) {
                    i2 = image2.getWidth();
                    i = this.mClearSightImage.getHeight();
                } else {
                    i2 = 0;
                    i = 0;
                }
                byte[] jpegData2 = ClearSightImageProcessor.this.getJpegData(this.mBayerImage);
                if (jpegData2 != null) {
                    ExifInterface exif = Exif.getExif(jpegData2);
                    int orientation = Exif.getOrientation(exif);
                    if (jpegData != null) {
                        if (ClearSightImageProcessor.this.mCallback != null) {
                            ClearSightImageProcessor.this.mCallback.onClearSightSuccess(jpegData);
                        }
                    } else if (jpegData2 != null) {
                        if (ClearSightImageProcessor.this.mCallback != null) {
                            ClearSightImageProcessor.this.mCallback.onClearSightFailure(jpegData2);
                        }
                    } else if (ClearSightImageProcessor.this.mCallback != null) {
                        ClearSightImageProcessor.this.mCallback.onClearSightFailure(null);
                    }
                    ClearSightImageProcessor.this.mMediaSaveService.addXmpImage(jpegData != null ? jpegData : jpegData2, this.mGImage, GDepth.createGDepth(this.mDepthMap), str2, j, null, i2, i, orientation, exif, ClearSightImageProcessor.this.mMediaSavedListener, ClearSightImageProcessor.this.mMediaSaveService.getContentResolver(), PhotoModule.PIXEL_FORMAT_JPEG);
                }
                resetParams();
            }
        }

        private boolean isReadyToSave() {
            return this.mDepthMapReady && this.mClearSightReady;
        }

        /* access modifiers changed from: 0000 */
        public void resetParams() {
            Image image = this.mBayerImage;
            if (image != null) {
                image.close();
                this.mBayerImage = null;
            }
            Image image2 = this.mMonoImage;
            if (image2 != null) {
                image2.close();
                this.mMonoImage = null;
            }
            Image image3 = this.mClearSightImage;
            if (image3 != null) {
                image3.close();
                this.mClearSightImage = null;
            }
            this.mNamedEntity = null;
            this.mHasFailure = false;
            this.mEncodeRequest = 0;
            this.mEncodeResults = 0;
            this.mGImage = null;
            this.mDepthMapReady = false;
            this.mClearSightReady = false;
        }
    }

    private class ImageProcessHandler extends Handler {
        private ArrayDeque<TotalCaptureResult> mBayerCaptureResults = new ArrayDeque<>(ClearSightImageProcessor.this.mNumBurstCount);
        private ArrayDeque<ReprocessableImage> mBayerFrames = new ArrayDeque<>(ClearSightImageProcessor.this.mNumBurstCount);
        private ArrayDeque<Image> mBayerImages = new ArrayDeque<>(ClearSightImageProcessor.this.mNumBurstCount);
        private boolean mCaptureDone;
        private boolean mHasFailures;
        private ArrayDeque<TotalCaptureResult> mMonoCaptureResults = new ArrayDeque<>(ClearSightImageProcessor.this.mNumBurstCount);
        private ArrayDeque<ReprocessableImage> mMonoFrames = new ArrayDeque<>(ClearSightImageProcessor.this.mNumBurstCount);
        private ArrayDeque<Image> mMonoImages = new ArrayDeque<>(ClearSightImageProcessor.this.mNumBurstCount);
        private NamedEntity mNamedEntity;
        private int[] mNumImagesToProcess = new int[2];
        private int mReprocessedBayerCount;
        private int mReprocessedMonoCount;
        private SparseLongArray[] mReprocessingFrames = new SparseLongArray[2];
        private int mReprocessingPairCount;
        private ArrayList<CaptureRequest> mReprocessingRequests = new ArrayList<>();

        ImageProcessHandler(Looper looper) {
            super(looper);
            this.mReprocessingFrames[0] = new SparseLongArray();
            this.mReprocessingFrames[1] = new SparseLongArray();
        }

        public void handleMessage(Message message) {
            if (!ClearSightImageProcessor.this.isClosing()) {
                switch (message.what) {
                    case 0:
                        this.mCaptureDone = false;
                        ClearSightImageProcessor.this.mFinishReprocessNum = 0;
                        this.mHasFailures = false;
                        this.mReprocessingPairCount = 0;
                        this.mReprocessedBayerCount = 0;
                        this.mReprocessedMonoCount = 0;
                        this.mNumImagesToProcess[message.arg1] = message.arg2;
                        ClearSightImageProcessor.this.mNamedImages.nameNewImage(System.currentTimeMillis());
                        this.mNamedEntity = ClearSightImageProcessor.this.mNamedImages.getNextNameEntity();
                        ClearSightImageProcessor.this.mClearsightRegisterHandler.obtainMessage(0, 0, 0, this.mNamedEntity).sendToTarget();
                        ClearSightImageProcessor.this.mDepthProcessHandler.obtainMessage(0).sendToTarget();
                        break;
                    case 1:
                        processImg(message);
                        break;
                    case 2:
                        processNewCaptureEvent(message);
                        break;
                    case 3:
                        processNewCaptureEvent(message);
                        break;
                    case 4:
                        processNewReprocessResult(message);
                        break;
                    case 5:
                        processNewReprocessFailure(message);
                        break;
                    case 6:
                        handleTimeout();
                        break;
                }
            }
        }

        private void handleTimeout() {
            Log.d(ClearSightImageProcessor.TAG, "handleTimeout");
            releaseBayerFrames();
            releaseMonoFrames();
            this.mReprocessingFrames[0].clear();
            this.mReprocessingFrames[1].clear();
            this.mReprocessingRequests.clear();
            removeMessages(2);
            removeMessages(3);
            removeMessages(4);
            removeMessages(5);
            removeMessages(6);
            this.mCaptureDone = true;
            ClearSightImageProcessor.this.mClearsightRegisterHandler.obtainMessage(6, 0, 1).sendToTarget();
        }

        private void kickTimeout() {
            removeMessages(6);
            sendEmptyMessageDelayed(6, (long) ClearSightImageProcessor.this.mCsTimeout);
        }

        private void processImg(Message message) {
            int i = message.arg1;
            StringBuilder sb = new StringBuilder();
            sb.append("processImg: ");
            sb.append(i);
            Log.d(ClearSightImageProcessor.TAG, sb.toString());
            Image image = (Image) message.obj;
            if (this.mReprocessingFrames[i].size() <= 0 || this.mReprocessingFrames[i].indexOfValue(image.getTimestamp()) < 0) {
                processNewCaptureEvent(message);
            } else {
                processNewReprocessImage(message);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:40:0x01a0, code lost:
            if (r9[1] == 0) goto L_0x01a2;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void processNewCaptureEvent(android.os.Message r9) {
            /*
                r8 = this;
                r8.kickTimeout()
                boolean r0 = r8.mCaptureDone
                r1 = 1
                java.lang.String r2 = "ClearSightImageProcessor"
                if (r0 == 0) goto L_0x0044
                java.lang.StringBuilder r8 = new java.lang.StringBuilder
                r8.<init>()
                java.lang.String r0 = "processNewCaptureEvent - captureDone - we already have required frame pairs "
                r8.append(r0)
                int r0 = r9.arg1
                r8.append(r0)
                java.lang.String r8 = r8.toString()
                android.util.Log.d(r2, r8)
                int r8 = r9.what
                if (r8 != r1) goto L_0x0043
                java.lang.Object r8 = r9.obj
                android.media.Image r8 = (android.media.Image) r8
                java.lang.StringBuilder r9 = new java.lang.StringBuilder
                r9.<init>()
                java.lang.String r0 = "processNewCaptureEvent - captureDone - tossed frame ts: "
                r9.append(r0)
                long r0 = r8.getTimestamp()
                r9.append(r0)
                java.lang.String r9 = r9.toString()
                android.util.Log.d(r2, r9)
                r8.close()
            L_0x0043:
                return
            L_0x0044:
                int r0 = r9.arg1
                if (r0 != 0) goto L_0x004f
                java.util.ArrayDeque<android.media.Image> r0 = r8.mBayerImages
                java.util.ArrayDeque<android.hardware.camera2.TotalCaptureResult> r3 = r8.mBayerCaptureResults
                java.util.ArrayDeque<org.codeaurora.snapcam.filter.ClearSightImageProcessor$ReprocessableImage> r4 = r8.mBayerFrames
                goto L_0x0055
            L_0x004f:
                java.util.ArrayDeque<android.media.Image> r0 = r8.mMonoImages
                java.util.ArrayDeque<android.hardware.camera2.TotalCaptureResult> r3 = r8.mMonoCaptureResults
                java.util.ArrayDeque<org.codeaurora.snapcam.filter.ClearSightImageProcessor$ReprocessableImage> r4 = r8.mMonoFrames
            L_0x0055:
                int r5 = r9.what
                if (r5 != r1) goto L_0x0077
                java.lang.StringBuilder r5 = new java.lang.StringBuilder
                r5.<init>()
                java.lang.String r6 = "processNewCaptureEvent - newImg: "
                r5.append(r6)
                int r6 = r9.arg1
                r5.append(r6)
                java.lang.String r5 = r5.toString()
                android.util.Log.d(r2, r5)
                java.lang.Object r5 = r9.obj
                android.media.Image r5 = (android.media.Image) r5
                r0.add(r5)
                goto L_0x00b7
            L_0x0077:
                r6 = 3
                if (r5 != r6) goto L_0x009a
                java.lang.StringBuilder r5 = new java.lang.StringBuilder
                r5.<init>()
                java.lang.String r6 = "processNewCaptureEvent - new failed result: "
                r5.append(r6)
                int r6 = r9.arg1
                r5.append(r6)
                java.lang.String r5 = r5.toString()
                android.util.Log.d(r2, r5)
                int[] r5 = r8.mNumImagesToProcess
                int r6 = r9.arg1
                r7 = r5[r6]
                int r7 = r7 - r1
                r5[r6] = r7
                goto L_0x00b7
            L_0x009a:
                java.lang.StringBuilder r5 = new java.lang.StringBuilder
                r5.<init>()
                java.lang.String r6 = "processNewCaptureEvent - newResult: "
                r5.append(r6)
                int r6 = r9.arg1
                r5.append(r6)
                java.lang.String r5 = r5.toString()
                android.util.Log.d(r2, r5)
                java.lang.Object r5 = r9.obj
                android.hardware.camera2.TotalCaptureResult r5 = (android.hardware.camera2.TotalCaptureResult) r5
                r3.add(r5)
            L_0x00b7:
                java.lang.StringBuilder r5 = new java.lang.StringBuilder
                r5.<init>()
                java.lang.String r6 = "processNewCaptureEvent - cam: "
                r5.append(r6)
                int r6 = r9.arg1
                r5.append(r6)
                java.lang.String r6 = " num imgs: "
                r5.append(r6)
                int r6 = r0.size()
                r5.append(r6)
                java.lang.String r6 = " num results: "
                r5.append(r6)
                int r6 = r3.size()
                r5.append(r6)
                java.lang.String r5 = r5.toString()
                android.util.Log.d(r2, r5)
                boolean r5 = r0.isEmpty()
                if (r5 != 0) goto L_0x0111
                boolean r5 = r3.isEmpty()
                if (r5 != 0) goto L_0x0111
                java.lang.Object r0 = r0.poll()
                android.media.Image r0 = (android.media.Image) r0
                java.lang.Object r3 = r3.poll()
                android.hardware.camera2.TotalCaptureResult r3 = (android.hardware.camera2.TotalCaptureResult) r3
                org.codeaurora.snapcam.filter.ClearSightImageProcessor$ReprocessableImage r5 = new org.codeaurora.snapcam.filter.ClearSightImageProcessor$ReprocessableImage
                r5.<init>(r0, r3)
                r4.add(r5)
                int[] r0 = r8.mNumImagesToProcess
                int r9 = r9.arg1
                r3 = r0[r9]
                int r3 = r3 - r1
                r0[r9] = r3
                r8.checkForValidFramePairAndReprocess()
            L_0x0111:
                java.lang.StringBuilder r9 = new java.lang.StringBuilder
                r9.<init>()
                java.lang.String r0 = "processNewCaptureEvent - imagestoprocess[bayer] "
                r9.append(r0)
                int[] r0 = r8.mNumImagesToProcess
                r3 = 0
                r0 = r0[r3]
                r9.append(r0)
                java.lang.String r0 = " imagestoprocess[mono]: "
                r9.append(r0)
                int[] r0 = r8.mNumImagesToProcess
                r0 = r0[r1]
                r9.append(r0)
                java.lang.String r0 = " mReprocessingPairCount: "
                r9.append(r0)
                int r0 = r8.mReprocessingPairCount
                r9.append(r0)
                java.lang.String r0 = " mNumFrameCount: "
                r9.append(r0)
                org.codeaurora.snapcam.filter.ClearSightImageProcessor r0 = org.codeaurora.snapcam.filter.ClearSightImageProcessor.this
                int r0 = r0.mNumFrameCount
                r9.append(r0)
                java.lang.String r0 = " mFinishReprocessNum: "
                r9.append(r0)
                org.codeaurora.snapcam.filter.ClearSightImageProcessor r0 = org.codeaurora.snapcam.filter.ClearSightImageProcessor.this
                int r0 = r0.mFinishReprocessNum
                r9.append(r0)
                java.lang.String r9 = r9.toString()
                android.util.Log.d(r2, r9)
                int[] r9 = r8.mNumImagesToProcess
                r0 = r9[r3]
                if (r0 != 0) goto L_0x018e
                r9 = r9[r1]
                if (r9 != 0) goto L_0x018e
                int r9 = r8.mReprocessingPairCount
                org.codeaurora.snapcam.filter.ClearSightImageProcessor r0 = org.codeaurora.snapcam.filter.ClearSightImageProcessor.this
                int r0 = r0.mNumFrameCount
                if (r9 == r0) goto L_0x018e
            L_0x0170:
                java.util.ArrayDeque<org.codeaurora.snapcam.filter.ClearSightImageProcessor$ReprocessableImage> r9 = r8.mBayerFrames
                boolean r9 = r9.isEmpty()
                if (r9 != 0) goto L_0x018e
                java.util.ArrayDeque<org.codeaurora.snapcam.filter.ClearSightImageProcessor$ReprocessableImage> r9 = r8.mMonoFrames
                boolean r9 = r9.isEmpty()
                if (r9 != 0) goto L_0x018e
                int r9 = r8.mReprocessingPairCount
                org.codeaurora.snapcam.filter.ClearSightImageProcessor r0 = org.codeaurora.snapcam.filter.ClearSightImageProcessor.this
                int r0 = r0.mNumFrameCount
                if (r9 == r0) goto L_0x018e
                r8.checkForValidFramePairAndReprocess()
                goto L_0x0170
            L_0x018e:
                int r9 = r8.mReprocessingPairCount
                org.codeaurora.snapcam.filter.ClearSightImageProcessor r0 = org.codeaurora.snapcam.filter.ClearSightImageProcessor.this
                int r0 = r0.mNumFrameCount
                if (r9 == r0) goto L_0x01a2
                int[] r9 = r8.mNumImagesToProcess
                r0 = r9[r3]
                if (r0 != 0) goto L_0x01b8
                r9 = r9[r1]
                if (r9 != 0) goto L_0x01b8
            L_0x01a2:
                r8.processFinalPair()
                int r9 = r8.mReprocessingPairCount
                if (r9 == 0) goto L_0x01b8
                org.codeaurora.snapcam.filter.ClearSightImageProcessor r9 = org.codeaurora.snapcam.filter.ClearSightImageProcessor.this
                int r9 = r9.mFinishReprocessNum
                int r0 = r8.mReprocessingPairCount
                int r0 = r0 * 2
                if (r9 != r0) goto L_0x01b8
                r8.checkReprocessDone()
            L_0x01b8:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.codeaurora.snapcam.filter.ClearSightImageProcessor.ImageProcessHandler.processNewCaptureEvent(android.os.Message):void");
        }

        private void checkForValidFramePairAndReprocess() {
            StringBuilder sb = new StringBuilder();
            sb.append("checkForValidFramePair - num bayer frames: ");
            sb.append(this.mBayerFrames.size());
            sb.append(" num mono frames: ");
            sb.append(this.mMonoFrames.size());
            String sb2 = sb.toString();
            String str = ClearSightImageProcessor.TAG;
            Log.d(str, sb2);
            if (!this.mBayerFrames.isEmpty() && !this.mMonoFrames.isEmpty()) {
                ReprocessableImage reprocessableImage = (ReprocessableImage) this.mBayerFrames.peek();
                ReprocessableImage reprocessableImage2 = (ReprocessableImage) this.mMonoFrames.peek();
                long longValue = ((Long) reprocessableImage.mCaptureResult.get(CaptureResult.SENSOR_TIMESTAMP)).longValue();
                long longValue2 = ((Long) reprocessableImage.mCaptureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME)).longValue() + longValue;
                long longValue3 = ((Long) reprocessableImage2.mCaptureResult.get(CaptureResult.SENSOR_TIMESTAMP)).longValue();
                long longValue4 = ((Long) reprocessableImage2.mCaptureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME)).longValue() + longValue3;
                StringBuilder sb3 = new StringBuilder();
                sb3.append("checkForValidFramePair - bayer ts SOF: ");
                sb3.append(longValue);
                String str2 = ", EOF: ";
                sb3.append(str2);
                sb3.append(longValue2);
                sb3.append(", mono ts SOF: ");
                sb3.append(longValue3);
                sb3.append(str2);
                sb3.append(longValue4);
                Log.d(str, sb3.toString());
                StringBuilder sb4 = new StringBuilder();
                sb4.append("checkForValidFramePair - difference SOF: ");
                long j = longValue - longValue3;
                sb4.append(Math.abs(j));
                sb4.append(str2);
                long j2 = longValue2 - longValue4;
                sb4.append(Math.abs(j2));
                Log.d(str, sb4.toString());
                if (Math.abs(j) <= ClearSightImageProcessor.this.mTimestampThresholdNs || Math.abs(j2) <= ClearSightImageProcessor.this.mTimestampThresholdNs) {
                    sendReprocessRequest(0, (ReprocessableImage) this.mBayerFrames.poll());
                    sendReprocessRequest(1, (ReprocessableImage) this.mMonoFrames.poll());
                    this.mReprocessingPairCount++;
                } else if (longValue > longValue3) {
                    Log.d(str, "checkForValidFramePair - toss mono");
                    ((ReprocessableImage) this.mMonoFrames.poll()).mImage.close();
                } else {
                    Log.d(str, "checkForValidFramePair - toss bayer");
                    ((ReprocessableImage) this.mBayerFrames.poll()).mImage.close();
                }
            }
        }

        private void sendReprocessRequest(final int i, ReprocessableImage reprocessableImage) {
            String str = ClearSightImageProcessor.TAG;
            CameraCaptureSession cameraCaptureSession = ClearSightImageProcessor.this.mCaptureSessions[i];
            CameraDevice device = cameraCaptureSession.getDevice();
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("sendReprocessRequest - cam: ");
                sb.append(i);
                Log.d(str, sb.toString());
                Builder createReprocessCaptureRequest = device.createReprocessCaptureRequest(reprocessableImage.mCaptureResult);
                createReprocessCaptureRequest.addTarget(ClearSightImageProcessor.this.mImageReader[i].getSurface());
                createReprocessCaptureRequest.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, Integer.valueOf(2));
                createReprocessCaptureRequest.set(CaptureRequest.EDGE_MODE, Integer.valueOf(2));
                createReprocessCaptureRequest.set(CaptureRequest.NOISE_REDUCTION_MODE, Integer.valueOf(2));
                Long valueOf = Long.valueOf(reprocessableImage.mImage.getTimestamp());
                Integer valueOf2 = Integer.valueOf(valueOf.hashCode());
                createReprocessCaptureRequest.setTag(valueOf2);
                this.mReprocessingFrames[i].put(valueOf2.intValue(), valueOf.longValue());
                StringBuilder sb2 = new StringBuilder();
                sb2.append("sendReprocessRequest - adding reproc frame - hash: ");
                sb2.append(valueOf2);
                sb2.append(", ts: ");
                sb2.append(valueOf);
                Log.d(str, sb2.toString());
                ClearSightImageProcessor.this.mImageWriter[i].queueInputImage(reprocessableImage.mImage);
                CaptureRequest build = createReprocessCaptureRequest.build();
                cameraCaptureSession.capture(build, new CaptureCallback() {
                    public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
                        super.onCaptureCompleted(cameraCaptureSession, captureRequest, totalCaptureResult);
                        StringBuilder sb = new StringBuilder();
                        sb.append("reprocess - onCaptureCompleted: ");
                        sb.append(i);
                        Log.d(ClearSightImageProcessor.TAG, sb.toString());
                        ImageProcessHandler.this.obtainMessage(4, i, ((Integer) captureRequest.getTag()).intValue(), totalCaptureResult).sendToTarget();
                    }

                    public void onCaptureFailed(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureFailure captureFailure) {
                        super.onCaptureFailed(cameraCaptureSession, captureRequest, captureFailure);
                        StringBuilder sb = new StringBuilder();
                        sb.append("reprocess - onCaptureFailed: ");
                        sb.append(i);
                        Log.d(ClearSightImageProcessor.TAG, sb.toString());
                        ImageProcessHandler.this.obtainMessage(5, i, ((Integer) captureRequest.getTag()).intValue(), captureFailure).sendToTarget();
                    }
                }, null);
                this.mReprocessingRequests.add(build);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        private void releaseBayerFrames() {
            Iterator it = this.mBayerFrames.iterator();
            while (it.hasNext()) {
                ((ReprocessableImage) it.next()).mImage.close();
            }
            this.mBayerFrames.clear();
            Iterator it2 = this.mBayerImages.iterator();
            while (it2.hasNext()) {
                ((Image) it2.next()).close();
            }
            this.mBayerImages.clear();
            this.mBayerCaptureResults.clear();
        }

        private void releaseMonoFrames() {
            Iterator it = this.mMonoFrames.iterator();
            while (it.hasNext()) {
                ((ReprocessableImage) it.next()).mImage.close();
            }
            this.mMonoFrames.clear();
            Iterator it2 = this.mMonoImages.iterator();
            while (it2.hasNext()) {
                ((Image) it2.next()).close();
            }
            this.mMonoImages.clear();
            this.mMonoCaptureResults.clear();
        }

        private void processFinalPair() {
            String str = ClearSightImageProcessor.TAG;
            Log.d(str, "processFinalPair");
            releaseBayerFrames();
            releaseMonoFrames();
            removeMessages(2);
            removeMessages(3);
            this.mCaptureDone = true;
            if (this.mReprocessingPairCount == 0) {
                Log.w(str, "processFinalPair - no matching pairs found");
                removeMessages(6);
                if (ClearSightImageProcessor.this.mCallback != null) {
                    ClearSightImageProcessor.this.mCallback.onClearSightFailure(null);
                }
            }
        }

        private void processNewReprocessImage(Message message) {
            int i;
            int i2;
            Message message2 = message;
            boolean z = message2.arg1 == 0;
            Image image = (Image) message2.obj;
            long timestamp = image.getTimestamp();
            int i3 = message2.arg1;
            StringBuilder sb = new StringBuilder();
            sb.append("processNewReprocessImage - cam: ");
            sb.append(i3);
            sb.append(", ts: ");
            sb.append(timestamp);
            Log.d(ClearSightImageProcessor.TAG, sb.toString());
            if (z) {
                i = this.mReprocessedBayerCount + 1;
                this.mReprocessedBayerCount = i;
            } else {
                i = this.mReprocessedMonoCount + 1;
                this.mReprocessedMonoCount = i;
            }
            int i4 = i;
            if (ClearSightImageProcessor.this.mDumpImages) {
                ClearSightImageProcessor clearSightImageProcessor = ClearSightImageProcessor.this;
                i2 = i3;
                clearSightImageProcessor.saveDebugImageAsJpeg(clearSightImageProcessor.mMediaSaveService, image, z, this.mNamedEntity, i4, timestamp / 1000000);
            } else {
                i2 = i3;
            }
            if (ClearSightImageProcessor.this.mDumpYUV) {
                ClearSightImageProcessor.this.saveDebugImageAsNV21(image, z, this.mNamedEntity, i4, timestamp / 1000000);
            }
            ClearSightImageProcessor.this.mClearsightRegisterHandler.obtainMessage(1, message2.arg1, 0, message2.obj).sendToTarget();
            SparseLongArray[] sparseLongArrayArr = this.mReprocessingFrames;
            sparseLongArrayArr[i2].removeAt(sparseLongArrayArr[i2].indexOfValue(timestamp));
            checkReprocessDone();
        }

        private void processNewReprocessResult(Message message) {
            StringBuilder sb = new StringBuilder();
            sb.append("processNewReprocessResult: ");
            sb.append(message.arg1);
            String sb2 = sb.toString();
            String str = ClearSightImageProcessor.TAG;
            Log.d(str, sb2);
            boolean z = message.arg1 == 0;
            TotalCaptureResult totalCaptureResult = (TotalCaptureResult) message.obj;
            this.mReprocessingRequests.remove(totalCaptureResult.getRequest());
            if (ClearSightNativeEngine.getInstance().getReferenceResult(z) == null) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("reprocess - setReferenceResult: ");
                sb3.append(message.obj);
                Log.d(str, sb3.toString());
                ClearSightNativeEngine.getInstance().setReferenceResult(z, totalCaptureResult);
                ClearSightImageProcessor.this.mDepthProcessHandler.obtainMessage(4, message.arg1, 0, message.obj).sendToTarget();
            }
            ClearSightImageProcessor.this.mFinishReprocessNum = ClearSightImageProcessor.this.mFinishReprocessNum + 1;
            checkReprocessDone();
        }

        private void processNewReprocessFailure(Message message) {
            int i = message.arg1;
            StringBuilder sb = new StringBuilder();
            sb.append("processNewReprocessFailure: ");
            sb.append(i);
            Log.d(ClearSightImageProcessor.TAG, sb.toString());
            this.mReprocessingRequests.remove(((CaptureFailure) message.obj).getRequest());
            this.mReprocessingFrames[i].delete(message.arg2);
            this.mHasFailures = true;
            ClearSightImageProcessor.this.mFinishReprocessNum = ClearSightImageProcessor.this.mFinishReprocessNum + 1;
            checkReprocessDone();
        }

        private void checkReprocessDone() {
            StringBuilder sb = new StringBuilder();
            sb.append("checkReprocessDone capture done: ");
            sb.append(this.mCaptureDone);
            sb.append(", reproc frames[bay]: ");
            sb.append(this.mReprocessingFrames[0].size());
            sb.append(", reproc frames[mono]: ");
            sb.append(this.mReprocessingFrames[1].size());
            sb.append(", mReprocessingRequests: ");
            sb.append(this.mReprocessingRequests.size());
            Log.d(ClearSightImageProcessor.TAG, sb.toString());
            if (!this.mCaptureDone || this.mReprocessingFrames[0].size() != 0 || this.mReprocessingFrames[1].size() != 0 || !this.mReprocessingRequests.isEmpty()) {
                kickTimeout();
                return;
            }
            ClearSightImageProcessor.this.mClearsightRegisterHandler.obtainMessage(6, this.mHasFailures ? 1 : 0, 0).sendToTarget();
            removeMessages(4);
            removeMessages(5);
            this.mCaptureDone = false;
            this.mHasFailures = false;
            removeMessages(6);
        }
    }

    private static class ReprocessableImage {
        final TotalCaptureResult mCaptureResult;
        final Image mImage;

        ReprocessableImage(Image image, TotalCaptureResult totalCaptureResult) {
            this.mImage = image;
            this.mCaptureResult = totalCaptureResult;
        }
    }

    private ClearSightImageProcessor() {
        StringBuilder sb = new StringBuilder();
        sb.append("mTimestampThresholdNs: ");
        sb.append(this.mTimestampThresholdNs);
        String sb2 = sb.toString();
        String str = TAG;
        Log.d(str, sb2);
        this.mNumBurstCount = PersistUtil.getImageToBurst();
        StringBuilder sb3 = new StringBuilder();
        sb3.append("mNumBurstCount: ");
        sb3.append(this.mNumBurstCount);
        Log.d(str, sb3.toString());
        this.mNumFrameCount = this.mNumBurstCount - 1;
        StringBuilder sb4 = new StringBuilder();
        sb4.append("mNumFrameCount: ");
        sb4.append(this.mNumFrameCount);
        Log.d(str, sb4.toString());
        this.mDumpImages = PersistUtil.isDumpFramesEnabled();
        StringBuilder sb5 = new StringBuilder();
        sb5.append("mDumpImages: ");
        sb5.append(this.mDumpImages);
        Log.d(str, sb5.toString());
        this.mDumpYUV = PersistUtil.isDumpYUVEnabled();
        StringBuilder sb6 = new StringBuilder();
        sb6.append("mDumpYUV: ");
        sb6.append(this.mDumpYUV);
        Log.d(str, sb6.toString());
        this.mDumpDepth = PersistUtil.isDumpDepthEnabled();
        StringBuilder sb7 = new StringBuilder();
        sb7.append("mDumpDepth: ");
        sb7.append(this.mDumpDepth);
        Log.d(str, sb7.toString());
        this.mCsTimeout = PersistUtil.getClearSightTimeout();
        StringBuilder sb8 = new StringBuilder();
        sb8.append("mCsTimeout: ");
        sb8.append(this.mCsTimeout);
        Log.d(str, sb8.toString());
    }

    public static void createInstance() {
        if (mInstance == null) {
            mInstance = new ClearSightImageProcessor();
            ClearSightNativeEngine.createInstance();
        }
    }

    public static ClearSightImageProcessor getInstance() {
        if (mInstance == null) {
            createInstance();
        }
        return mInstance;
    }

    public void init(StreamConfigurationMap streamConfigurationMap, Context context, OnMediaSavedListener onMediaSavedListener) {
        Size findMaxOutputSize = findMaxOutputSize(streamConfigurationMap);
        int width = findMaxOutputSize.getWidth();
        int height = findMaxOutputSize.getHeight();
        SettingsManager instance = SettingsManager.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(width));
        sb.append("x");
        sb.append(String.valueOf(height));
        instance.setValue("pref_camera_picturesize_key", sb.toString());
        init(streamConfigurationMap, width, height, context, onMediaSavedListener);
    }

    public void init(StreamConfigurationMap streamConfigurationMap, int i, int i2, Context context, OnMediaSavedListener onMediaSavedListener) {
        String str = TAG;
        Log.d(str, "init() start");
        this.mIsClosing = false;
        this.mImageProcessThread = new HandlerThread("CameraImageProcess");
        this.mImageProcessThread.start();
        this.mClearsightRegisterThread = new HandlerThread("ClearsightRegister");
        this.mClearsightRegisterThread.start();
        this.mClearsightProcessThread = new HandlerThread("ClearsightProcess");
        this.mClearsightProcessThread.start();
        this.mImageEncodeThread = new HandlerThread("CameraImageEncode");
        this.mImageEncodeThread.start();
        this.mDepthProcessThread = new HandlerThread("DepthProcess");
        this.mDepthProcessThread.start();
        this.mImageProcessHandler = new ImageProcessHandler(this.mImageProcessThread.getLooper());
        this.mClearsightRegisterHandler = new ClearsightRegisterHandler(this.mClearsightRegisterThread.getLooper());
        this.mClearsightProcessHandler = new ClearsightProcessHandler(this.mClearsightProcessThread.getLooper());
        this.mImageEncodeHandler = new ImageEncodeHandler(this.mImageEncodeThread.getLooper());
        this.mDepthProcessHandler = new DepthProcessHandler(this.mImageEncodeThread.getLooper());
        this.mFinalPictureSize = new Size(i, i2);
        this.mFinalPictureRatio = ((float) i) / ((float) i2);
        this.mFinalMonoSize = getFinalMonoSize();
        Size findMaxOutputSize = findMaxOutputSize(streamConfigurationMap);
        int width = findMaxOutputSize.getWidth();
        int height = findMaxOutputSize.getHeight();
        this.mImageReader[0] = createImageReader(0, width, height);
        this.mImageReader[1] = createImageReader(1, width, height);
        this.mEncodeImageReader[0] = createEncodeImageReader(0, width, height);
        this.mEncodeImageReader[1] = createEncodeImageReader(1, width, height);
        this.mMediaSavedListener = onMediaSavedListener;
        try {
            CamSystemCalibrationData createFromBytes = CamSystemCalibrationData.createFromBytes((byte[]) ((CameraManager) context.getSystemService("camera")).getCameraCharacteristics("0").get(OTP_CALIB_BLOB));
            ClearSightNativeEngine.getInstance().init(this.mNumFrameCount * 2, width, height, createFromBytes);
            this.mDepthProcessHandler.obtainMessage(7, 0, 0, createFromBytes).sendToTarget();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d(str, "init() done");
    }

    public void close() {
        String str = TAG;
        Log.d(str, "close() start");
        this.mIsClosing = true;
        HandlerThread handlerThread = this.mImageProcessThread;
        if (handlerThread != null) {
            handlerThread.quit();
            try {
                this.mImageProcessThread.join();
                this.mImageProcessThread = null;
                this.mImageProcessHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        HandlerThread handlerThread2 = this.mClearsightRegisterThread;
        if (handlerThread2 != null) {
            handlerThread2.quit();
            try {
                this.mClearsightRegisterThread.join();
                this.mClearsightRegisterThread = null;
                this.mClearsightRegisterHandler = null;
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }
        HandlerThread handlerThread3 = this.mClearsightProcessThread;
        if (handlerThread3 != null) {
            handlerThread3.quit();
            try {
                this.mClearsightProcessThread.join();
                this.mClearsightProcessThread = null;
                this.mClearsightProcessHandler = null;
            } catch (InterruptedException e3) {
                e3.printStackTrace();
            }
        }
        HandlerThread handlerThread4 = this.mImageEncodeThread;
        if (handlerThread4 != null) {
            handlerThread4.quit();
            try {
                this.mImageEncodeThread.join();
                this.mImageEncodeThread = null;
                this.mImageEncodeHandler = null;
            } catch (InterruptedException e4) {
                e4.printStackTrace();
            }
        }
        HandlerThread handlerThread5 = this.mDepthProcessThread;
        if (handlerThread5 != null) {
            handlerThread5.quit();
            try {
                this.mDepthProcessThread.join();
                this.mDepthProcessThread = null;
                this.mDepthProcessHandler = null;
            } catch (InterruptedException e5) {
                e5.printStackTrace();
            }
        }
        int i = 0;
        while (true) {
            ImageReader[] imageReaderArr = this.mImageReader;
            if (i < imageReaderArr.length) {
                if (imageReaderArr[i] != null) {
                    imageReaderArr[i].close();
                    this.mImageReader[i] = null;
                }
                ImageReader[] imageReaderArr2 = this.mEncodeImageReader;
                if (imageReaderArr2[i] != null) {
                    imageReaderArr2[i].close();
                    this.mEncodeImageReader[i] = null;
                }
                ImageWriter[] imageWriterArr = this.mImageWriter;
                if (imageWriterArr[i] != null) {
                    imageWriterArr[i].close();
                    this.mImageWriter[i] = null;
                }
                i++;
            } else {
                CameraCaptureSession[] cameraCaptureSessionArr = this.mCaptureSessions;
                cameraCaptureSessionArr[1] = null;
                cameraCaptureSessionArr[0] = null;
                this.mMediaSaveService = null;
                this.mMediaSavedListener = null;
                ClearSightNativeEngine.getInstance().close();
                Log.d(str, "close() done");
                return;
            }
        }
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setMediaSaveService(MediaSaveService mediaSaveService) {
        this.mMediaSaveService = mediaSaveService;
    }

    public void createCaptureSession(boolean z, CameraDevice cameraDevice, List<Surface> list, StateCallback stateCallback) throws CameraAccessException {
        StringBuilder sb = new StringBuilder();
        sb.append("createCaptureSession: ");
        sb.append(z);
        Log.d(TAG, sb.toString());
        char c = !z;
        list.add(this.mImageReader[c].getSurface());
        list.add(this.mEncodeImageReader[c].getSurface());
        cameraDevice.createReprocessableCaptureSession(new InputConfiguration(this.mImageReader[c].getWidth(), this.mImageReader[c].getHeight(), this.mImageReader[c].getImageFormat()), list, stateCallback, null);
    }

    public void onCaptureSessionConfigured(boolean z, CameraCaptureSession cameraCaptureSession) {
        StringBuilder sb = new StringBuilder();
        sb.append("onCaptureSessionConfigured: ");
        sb.append(z);
        Log.d(TAG, sb.toString());
        this.mCaptureSessions[!z] = cameraCaptureSession;
        this.mImageWriter[!z] = ImageWriter.newInstance(cameraCaptureSession.getInputSurface(), this.mNumBurstCount);
    }

    public Builder createCaptureRequest(CameraDevice cameraDevice) throws CameraAccessException {
        Log.d(TAG, "createCaptureRequest");
        return cameraDevice.createCaptureRequest(5);
    }

    public void capture(boolean z, CameraCaptureSession cameraCaptureSession, Builder builder, Handler handler) throws CameraAccessException {
        StringBuilder sb = new StringBuilder();
        sb.append("capture: ");
        sb.append(z);
        Log.d(TAG, sb.toString());
        final int i = !z;
        C09081 r0 = new CaptureCallback() {
            public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
                StringBuilder sb = new StringBuilder();
                sb.append("capture - onCaptureCompleted: ");
                sb.append(i);
                String sb2 = sb.toString();
                String str = ClearSightImageProcessor.TAG;
                Log.d(str, sb2);
                if (ClearSightImageProcessor.this.isClosing()) {
                    Log.d(str, "capture - onCaptureCompleted - closing");
                    return;
                }
                ClearSightImageProcessor.this.mImageProcessHandler.obtainMessage(2, i, 0, totalCaptureResult).sendToTarget();
                if (i == 0) {
                    float floatValue = ((Float) totalCaptureResult.get(CaptureResult.LENS_FOCUS_DISTANCE)).floatValue();
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("lensFocusDistance=");
                    sb3.append(floatValue);
                    Log.d(str, sb3.toString());
                    ClearSightImageProcessor.this.mDepthProcessHandler.obtainMessage(8, 0, 0, Float.valueOf(floatValue)).sendToTarget();
                }
            }

            public void onCaptureFailed(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureFailure captureFailure) {
                StringBuilder sb = new StringBuilder();
                sb.append("capture - onCaptureFailed: ");
                sb.append(i);
                String sb2 = sb.toString();
                String str = ClearSightImageProcessor.TAG;
                Log.d(str, sb2);
                if (ClearSightImageProcessor.this.isClosing()) {
                    Log.d(str, "capture - onCaptureFailed - closing");
                } else {
                    ClearSightImageProcessor.this.mImageProcessHandler.obtainMessage(3, i, 0, captureFailure).sendToTarget();
                }
            }

            public void onCaptureSequenceCompleted(CameraCaptureSession cameraCaptureSession, int i, long j) {
                StringBuilder sb = new StringBuilder();
                sb.append("capture - onCaptureSequenceCompleted: ");
                sb.append(i);
                Log.d(ClearSightImageProcessor.TAG, sb.toString());
            }
        };
        ArrayList arrayList = new ArrayList();
        builder.addTarget(this.mImageReader[i].getSurface());
        for (int i2 = 0; i2 < this.mNumBurstCount; i2++) {
            builder.setTag(new Object());
            arrayList.add(builder.build());
        }
        this.mImageProcessHandler.obtainMessage(0, i, arrayList.size(), Integer.valueOf(0)).sendToTarget();
        cameraCaptureSession.captureBurst(arrayList, r0, handler);
    }

    /* access modifiers changed from: private */
    public boolean isClosing() {
        return this.mIsClosing;
    }

    private ImageReader createImageReader(final int i, int i2, int i3) {
        ImageReader newInstance = ImageReader.newInstance(i2, i3, 35, this.mNumBurstCount + this.mNumFrameCount);
        newInstance.setOnImageAvailableListener(new OnImageAvailableListener() {
            public void onImageAvailable(ImageReader imageReader) {
                StringBuilder sb = new StringBuilder();
                sb.append("onImageAvailable for cam: ");
                sb.append(i);
                String sb2 = sb.toString();
                String str = ClearSightImageProcessor.TAG;
                Log.d(str, sb2);
                if (ClearSightImageProcessor.this.isClosing()) {
                    Log.d(str, "onImageAvailable - closing");
                } else {
                    ClearSightImageProcessor.this.mImageProcessHandler.obtainMessage(1, i, 0, imageReader.acquireNextImage()).sendToTarget();
                }
            }
        }, null);
        return newInstance;
    }

    private ImageReader createEncodeImageReader(final int i, int i2, int i3) {
        ImageReader newInstance = ImageReader.newInstance(i2, i3, 256, this.mNumFrameCount + 1);
        newInstance.setOnImageAvailableListener(new OnImageAvailableListener() {
            public void onImageAvailable(ImageReader imageReader) {
                StringBuilder sb = new StringBuilder();
                sb.append("jpeg image available for cam: ");
                sb.append(i);
                Log.d(ClearSightImageProcessor.TAG, sb.toString());
                ClearSightImageProcessor.this.mImageEncodeHandler.obtainMessage(1, i, 0, imageReader.acquireNextImage()).sendToTarget();
            }
        }, null);
        return newInstance;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0084 A[SYNTHETIC, Splitter:B:26:0x0084] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0093 A[SYNTHETIC, Splitter:B:31:0x0093] */
    /* JADX WARNING: Removed duplicated region for block: B:40:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void saveAsRGB(byte[] r5, int r6, int r7) {
        /*
            r4 = this;
            int r4 = r5.length
            int[] r4 = new int[r4]
            r0 = 0
            r1 = r0
        L_0x0005:
            int r2 = r4.length
            if (r1 >= r2) goto L_0x0013
            byte r2 = r5[r1]
            int r2 = r2 + 256
            int r2 = r2 % 256
            r4[r1] = r2
            int r1 = r1 + 1
            goto L_0x0005
        L_0x0013:
            android.graphics.Bitmap$Config r5 = android.graphics.Bitmap.Config.ARGB_8888
            android.graphics.Bitmap r5 = android.graphics.Bitmap.createBitmap(r6, r7, r5)
            r1 = r0
        L_0x001a:
            if (r1 >= r7) goto L_0x0031
            r2 = r0
        L_0x001d:
            if (r2 >= r6) goto L_0x002e
            int r3 = r1 * r6
            int r3 = r3 + r2
            r3 = r4[r3]
            int r3 = android.graphics.Color.rgb(r3, r3, r3)
            r5.setPixel(r2, r1, r3)
            int r2 = r2 + 1
            goto L_0x001d
        L_0x002e:
            int r1 = r1 + 1
            goto L_0x001a
        L_0x0031:
            java.io.ByteArrayOutputStream r4 = new java.io.ByteArrayOutputStream
            r4.<init>()
            android.graphics.Bitmap$CompressFormat r6 = android.graphics.Bitmap.CompressFormat.JPEG
            r7 = 100
            r5.compress(r6, r7, r4)
            java.io.File r5 = new java.io.File
            java.lang.String r6 = "sdcard/depthmap_rgb.jpg"
            r5.<init>(r6)
            byte[] r4 = r4.toByteArray()
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "jpeg.size="
            r6.append(r7)
            int r7 = r4.length
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            java.lang.String r7 = "ClearSightImageProcessor"
            android.util.Log.d(r7, r6)
            r6 = 0
            java.io.BufferedOutputStream r1 = new java.io.BufferedOutputStream     // Catch:{ Exception -> 0x007a }
            java.io.FileOutputStream r2 = new java.io.FileOutputStream     // Catch:{ Exception -> 0x007a }
            r2.<init>(r5)     // Catch:{ Exception -> 0x007a }
            r1.<init>(r2)     // Catch:{ Exception -> 0x007a }
            int r5 = r4.length     // Catch:{ Exception -> 0x0075, all -> 0x0072 }
            r1.write(r4, r0, r5)     // Catch:{ Exception -> 0x0075, all -> 0x0072 }
            r1.close()     // Catch:{ Exception -> 0x0088 }
            goto L_0x0090
        L_0x0072:
            r4 = move-exception
            r6 = r1
            goto L_0x0091
        L_0x0075:
            r4 = move-exception
            r6 = r1
            goto L_0x007b
        L_0x0078:
            r4 = move-exception
            goto L_0x0091
        L_0x007a:
            r4 = move-exception
        L_0x007b:
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0078 }
            android.util.Log.d(r7, r4)     // Catch:{ all -> 0x0078 }
            if (r6 == 0) goto L_0x0090
            r6.close()     // Catch:{ Exception -> 0x0088 }
            goto L_0x0090
        L_0x0088:
            r4 = move-exception
            java.lang.String r4 = r4.toString()
            android.util.Log.d(r7, r4)
        L_0x0090:
            return
        L_0x0091:
            if (r6 == 0) goto L_0x009f
            r6.close()     // Catch:{ Exception -> 0x0097 }
            goto L_0x009f
        L_0x0097:
            r5 = move-exception
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r7, r5)
        L_0x009f:
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: org.codeaurora.snapcam.filter.ClearSightImageProcessor.saveAsRGB(byte[], int, int):void");
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0046 A[SYNTHETIC, Splitter:B:17:0x0046] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0055 A[SYNTHETIC, Splitter:B:22:0x0055] */
    /* JADX WARNING: Removed duplicated region for block: B:28:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void saveToFile(byte[] r4, java.lang.String r5, java.lang.String r6) {
        /*
            r3 = this;
            java.lang.String r3 = "ClearSightImageProcessor"
            java.io.File r0 = new java.io.File
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "sdcard/"
            r1.append(r2)
            r1.append(r5)
            java.lang.String r5 = "."
            r1.append(r5)
            r1.append(r6)
            java.lang.String r5 = r1.toString()
            r0.<init>(r5)
            r5 = 0
            java.io.BufferedOutputStream r6 = new java.io.BufferedOutputStream     // Catch:{ Exception -> 0x003c }
            java.io.FileOutputStream r1 = new java.io.FileOutputStream     // Catch:{ Exception -> 0x003c }
            r1.<init>(r0)     // Catch:{ Exception -> 0x003c }
            r6.<init>(r1)     // Catch:{ Exception -> 0x003c }
            r5 = 0
            int r0 = r4.length     // Catch:{ Exception -> 0x0037, all -> 0x0034 }
            r6.write(r4, r5, r0)     // Catch:{ Exception -> 0x0037, all -> 0x0034 }
            r6.close()     // Catch:{ Exception -> 0x004a }
            goto L_0x0052
        L_0x0034:
            r4 = move-exception
            r5 = r6
            goto L_0x0053
        L_0x0037:
            r4 = move-exception
            r5 = r6
            goto L_0x003d
        L_0x003a:
            r4 = move-exception
            goto L_0x0053
        L_0x003c:
            r4 = move-exception
        L_0x003d:
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x003a }
            android.util.Log.d(r3, r4)     // Catch:{ all -> 0x003a }
            if (r5 == 0) goto L_0x0052
            r5.close()     // Catch:{ Exception -> 0x004a }
            goto L_0x0052
        L_0x004a:
            r4 = move-exception
            java.lang.String r4 = r4.toString()
            android.util.Log.d(r3, r4)
        L_0x0052:
            return
        L_0x0053:
            if (r5 == 0) goto L_0x0061
            r5.close()     // Catch:{ Exception -> 0x0059 }
            goto L_0x0061
        L_0x0059:
            r5 = move-exception
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r3, r5)
        L_0x0061:
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: org.codeaurora.snapcam.filter.ClearSightImageProcessor.saveToFile(byte[], java.lang.String, java.lang.String):void");
    }

    public void saveDebugImageAsJpeg(MediaSaveService mediaSaveService, byte[] bArr, int i, int i2, boolean z, NamedEntity namedEntity, int i3, long j) {
        NamedEntity namedEntity2 = namedEntity;
        mediaSaveService.addImage(bArr, String.format("%s_%s%02d_%d", new Object[]{namedEntity2.title, z ? "b" : "m", Integer.valueOf(i3), Long.valueOf(j)}), namedEntity2 == null ? -1 : namedEntity2.date, null, i, i2, 0, null, null, mediaSaveService.getContentResolver(), PhotoModule.PIXEL_FORMAT_JPEG);
    }

    public void saveDebugImageAsJpeg(MediaSaveService mediaSaveService, YuvImage yuvImage, boolean z, NamedEntity namedEntity, int i, long j) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        YuvImage yuvImage2 = yuvImage;
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, byteArrayOutputStream);
        saveDebugImageAsJpeg(mediaSaveService, byteArrayOutputStream.toByteArray(), yuvImage.getWidth(), yuvImage.getHeight(), z, namedEntity, i, j);
    }

    public void saveDebugImageAsJpeg(MediaSaveService mediaSaveService, Image image, boolean z, NamedEntity namedEntity, int i, long j) {
        Image image2 = image;
        if (image.getFormat() == 35) {
            saveDebugImageAsJpeg(mediaSaveService, createYuvImage(image), z, namedEntity, i, j);
        } else if (image.getFormat() == 256) {
            saveDebugImageAsJpeg(mediaSaveService, getJpegData(image), image.getWidth(), image.getHeight(), z, namedEntity, i, j);
        }
    }

    public void saveDebugImageAsNV21(Image image, boolean z, NamedEntity namedEntity, int i, long j) {
        if (image.getFormat() != 35) {
            Log.d(TAG, "saveDebugImageAsNV21 - invalid param");
        }
        String format = String.format("%s_%dx%d_NV21_%s%02d_%d", new Object[]{namedEntity.title, Integer.valueOf(image.getWidth()), Integer.valueOf(image.getHeight()), z ? "b" : "m", Integer.valueOf(i), Long.valueOf(j)});
        YuvImage createYuvImage = createYuvImage(image);
        String str = "yuv";
        Storage.writeFile(Storage.generateFilepath(format, str), createYuvImage.getYuvData(), null, str);
    }

    public YuvImage createYuvImage(Image image) {
        if (image == null) {
            Log.d(TAG, "createYuvImage - invalid param");
            return null;
        }
        Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        ByteBuffer buffer2 = planes[2].getBuffer();
        int capacity = buffer.capacity();
        int capacity2 = buffer2.capacity();
        byte[] bArr = new byte[(((image.getPlanes()[0].getRowStride() * image.getHeight()) * 3) / 2)];
        buffer.rewind();
        buffer.get(bArr, 0, capacity);
        buffer2.rewind();
        buffer2.get(bArr, capacity, capacity2);
        YuvImage yuvImage = new YuvImage(bArr, 17, image.getWidth(), image.getHeight(), new int[]{planes[0].getRowStride(), planes[2].getRowStride()});
        return yuvImage;
    }

    public byte[] getJpegData(Image image) {
        if (image == null) {
            Log.d(TAG, "getJpegData - invalid param");
            return null;
        }
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        int capacity = buffer.capacity();
        byte[] bArr = new byte[capacity];
        buffer.rewind();
        buffer.get(bArr, 0, capacity);
        return bArr;
    }

    private Size findMaxOutputSize(StreamConfigurationMap streamConfigurationMap) {
        Size[] outputSizes = streamConfigurationMap.getOutputSizes(35);
        Arrays.sort(outputSizes, new CompareSizesByArea());
        return outputSizes[outputSizes.length - 1];
    }

    private Size getFinalMonoSize() {
        Size size = null;
        if (((long) (this.mFinalPictureSize.getWidth() * this.mFinalPictureSize.getHeight())) > MIN_MONO_AREA) {
            Size[] sizeArr = MONO_SIZES;
            int length = sizeArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                Size size2 = sizeArr[i];
                float width = ((float) size2.getWidth()) / ((float) size2.getHeight());
                float f = this.mFinalPictureRatio;
                if (width == f) {
                    size = size2;
                    break;
                }
                if (Math.abs(width - f) < 0.1f) {
                    int width2 = size2.getWidth();
                    int height = size2.getHeight();
                    float f2 = this.mFinalPictureRatio;
                    if (width > f2) {
                        height = (int) ((((float) width2) / f2) + 0.5f);
                    } else if (width < f2) {
                        width2 = (int) ((((float) height) * f2) + 0.5f);
                    }
                    size = new Size(width2, height);
                }
                i++;
            }
        }
        return size == null ? this.mFinalPictureSize : size;
    }

    /* access modifiers changed from: private */
    public Rect getFinalCropRect(Rect rect) {
        Rect rect2;
        Rect rect3 = new Rect(rect);
        float width = ((float) rect.width()) / ((float) rect.height());
        StringBuilder sb = new StringBuilder();
        sb.append("getFinalCropRect - rect: ");
        sb.append(rect.toString());
        String sb2 = sb.toString();
        String str = TAG;
        Log.d(str, sb2);
        StringBuilder sb3 = new StringBuilder();
        sb3.append("getFinalCropRect - ratios: ");
        sb3.append(width);
        sb3.append(", ");
        sb3.append(this.mFinalPictureRatio);
        Log.d(str, sb3.toString());
        float f = this.mFinalPictureRatio;
        if (width > f) {
            int height = (int) ((((float) rect.height()) * this.mFinalPictureRatio) + 0.5f);
            int width2 = ((rect.width() - height) / 2) + rect.left;
            rect3.left = width2;
            rect3.right = width2 + height;
        } else if (width < f) {
            int width3 = (int) ((((float) rect.width()) / this.mFinalPictureRatio) + 0.5f);
            int height2 = ((rect.height() - width3) / 2) + rect.top;
            rect3.top = height2;
            rect3.bottom = height2 + width3;
        }
        if (rect3.width() % 2 == 0 && rect3.height() % 2 == 0) {
            rect2 = rect3;
        } else {
            int i = rect3.left;
            int i2 = rect3.top;
            int i3 = rect3.width() % 2 == 0 ? rect3.right : rect3.right + 1;
            int height3 = rect3.height() % 2;
            int i4 = rect3.bottom;
            if (height3 != 0) {
                i4++;
            }
            rect2 = new Rect(i, i2, i3, i4);
        }
        StringBuilder sb4 = new StringBuilder();
        sb4.append("getFinalCropRect - final rect: ");
        sb4.append(rect2.toString());
        Log.d(str, sb4.toString());
        return rect2;
    }
}
