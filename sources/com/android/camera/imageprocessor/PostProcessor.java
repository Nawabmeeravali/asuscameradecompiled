package com.android.camera.imageprocessor;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.ImageWriter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import com.android.camera.CameraActivity;
import com.android.camera.CaptureModule;
import com.android.camera.Exif;
import com.android.camera.MediaSaveService.OnMediaSavedListener;
import com.android.camera.PhotoModule;
import com.android.camera.PhotoModule.NamedImages;
import com.android.camera.PhotoModule.NamedImages.NamedEntity;
import com.android.camera.RecordLocationPreference;
import com.android.camera.SettingsManager;
import com.android.camera.exif.ExifInterface;
import com.android.camera.exif.Rational;
import com.android.camera.imageprocessor.filter.BestpictureFilter;
import com.android.camera.imageprocessor.filter.BlurbusterFilter;
import com.android.camera.imageprocessor.filter.ChromaflashFilter;
import com.android.camera.imageprocessor.filter.ImageFilter;
import com.android.camera.imageprocessor.filter.ImageFilter.ResultImage;
import com.android.camera.imageprocessor.filter.OptizoomFilter;
import com.android.camera.imageprocessor.filter.SharpshooterFilter;
import com.android.camera.imageprocessor.filter.StillmoreFilter;
import com.android.camera.imageprocessor.filter.UbifocusFilter;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.PersistUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;

public class PostProcessor {
    /* access modifiers changed from: private */
    public static boolean DEBUG_DUMP_FILTER_IMG = (PersistUtil.getCamera2Debug() == 1 || PersistUtil.getCamera2Debug() == 100);
    /* access modifiers changed from: private */
    public static boolean DEBUG_ZSL = false;
    public static final double FALLOFF_DELAY = 2.0E7d;
    public static final int FILTER_BESTPICTURE = 5;
    public static final int FILTER_BLURBUSTER = 7;
    public static final int FILTER_CHROMAFLASH = 6;
    public static final int FILTER_MAX = 8;
    public static final int FILTER_NONE = 0;
    public static final int FILTER_OPTIZOOM = 1;
    public static final int FILTER_SHARPSHOOTER = 2;
    public static final int FILTER_STILLMORE = 4;
    public static final int FILTER_UBIFOCUS = 3;
    private static final int MAX_REQUIRED_IMAGE_NUM = 3;
    private static final String TAG = "PostProcessor";
    private static final int UPDATE_SHUTTER_BUTTON = 0;
    private boolean fusionStatus = false;
    /* access modifiers changed from: private */
    public Object lock = new Object();
    /* access modifiers changed from: private */
    public CameraActivity mActivity;
    private CameraDevice mCameraDevice;
    CaptureCallback mCaptureCallback = new CaptureCallback() {
        public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
            Log.d(PostProcessor.TAG, "onCaptureCompleted");
            if (PostProcessor.this.mTotalCaptureResultList.size() <= 3) {
                PostProcessor.this.mTotalCaptureResultList.add(totalCaptureResult);
            }
            if (PostProcessor.this.mIsZSLFallOff) {
                PostProcessor.this.mZSLFallOffResult = totalCaptureResult;
            }
            PostProcessor.this.onMetaAvailable(totalCaptureResult);
        }

        public void onCaptureFailed(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureFailure captureFailure) {
            Log.d(PostProcessor.TAG, "onCaptureFailed");
        }

        public void onCaptureSequenceCompleted(CameraCaptureSession cameraCaptureSession, int i, long j) {
            Log.d(PostProcessor.TAG, "onCaptureSequenceCompleted");
            if (!PostProcessor.this.isFilterOn()) {
                PostProcessor.this.mController.unlockFocus(PostProcessor.this.mController.getMainCameraId());
            }
        }
    };
    private CameraCaptureSession mCaptureSession;
    /* access modifiers changed from: private */
    public CaptureModule mController;
    private int mCurrentNumImage = 0;
    /* access modifiers changed from: private */
    public ResultImage mDefaultResultImage;
    /* access modifiers changed from: private */
    public ImageFilter mFilter;
    /* access modifiers changed from: private */
    public int mFilterIndex;
    private ProcessorHandler mHandler;
    private HandlerThread mHandlerThread;
    /* access modifiers changed from: private */
    public int mHeight;
    private ImageHandlerTask mImageHandlerTask;
    private ImageReader mImageReader;
    private ImageWriter mImageWriter;
    /* access modifiers changed from: private */
    public Image[] mImages;
    /* access modifiers changed from: private */
    public boolean mIsZSLFallOff = false;
    private TotalCaptureResult mLatestResultForLongShot = null;
    public int mMaxRequiredImageNum;
    /* access modifiers changed from: private */
    public NamedImages mNamedImages;
    /* access modifiers changed from: private */
    public int mOrientation = 0;
    /* access modifiers changed from: private */
    public int mPendingContinuousRequestCount = 0;
    /* access modifiers changed from: private */
    public boolean mProcessZSL = true;
    /* access modifiers changed from: private */
    public boolean mSaveRaw = false;
    /* access modifiers changed from: private */
    public Handler mSavingHander;
    private HandlerThread mSavingHandlerThread;
    /* access modifiers changed from: private */
    public STATUS mStatus = STATUS.DEINIT;
    /* access modifiers changed from: private */
    public int mStride;
    /* access modifiers changed from: private */
    public LinkedList<TotalCaptureResult> mTotalCaptureResultList = new LinkedList<>();
    /* access modifiers changed from: private */
    public boolean mUseZSL = true;
    /* access modifiers changed from: private */
    public WatchdogThread mWatchdog;
    /* access modifiers changed from: private */
    public int mWidth;
    /* access modifiers changed from: private */
    public TotalCaptureResult mZSLFallOffResult = null;
    /* access modifiers changed from: private */
    public Handler mZSLHandler;
    private HandlerThread mZSLHandlerThread;
    /* access modifiers changed from: private */
    public ZSLQueue mZSLQueue;
    private ImageReader mZSLReprocessImageReader;
    /* access modifiers changed from: private */
    public Handler myHandler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == 0) {
                Log.d(PostProcessor.TAG, "UPDATE_SHUTTER_BUTTON");
                PostProcessor.this.mActivity.updateShutterButtonStatus(true);
            }
        }
    };
    OnImageAvailableListener processedImageAvailableListener = new OnImageAvailableListener() {
        public void onImageAvailable(ImageReader imageReader) {
            final Image image;
            try {
                image = imageReader.acquireNextImage();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                image = null;
            }
            if (image != null) {
                if (PostProcessor.DEBUG_ZSL) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("ZSL image Reprocess is done ");
                    sb.append(image.getTimestamp());
                    Log.d(PostProcessor.TAG, sb.toString());
                }
                PostProcessor.this.mSavingHander.post(new Runnable() {
                    public void run() {
                        String str;
                        long j;
                        PostProcessor.this.mNamedImages.nameNewImage(System.currentTimeMillis());
                        NamedEntity nextNameEntity = PostProcessor.this.mNamedImages.getNextNameEntity();
                        if (nextNameEntity == null) {
                            str = null;
                        } else {
                            str = nextNameEntity.title;
                        }
                        String str2 = str;
                        if (nextNameEntity == null) {
                            j = -1;
                        } else {
                            j = nextNameEntity.date;
                        }
                        long j2 = j;
                        image.getPlanes()[0].getBuffer().rewind();
                        int remaining = image.getPlanes()[0].getBuffer().remaining();
                        byte[] bArr = new byte[remaining];
                        image.getPlanes()[0].getBuffer().get(bArr, 0, remaining);
                        ExifInterface exif = Exif.getExif(bArr);
                        int orientation = Exif.getOrientation(exif);
                        if (PostProcessor.this.mController.getCurrentIntentMode() != 0) {
                            PostProcessor.this.mController.setJpegImageData(bArr);
                            if (PostProcessor.this.mController.isQuickCapture()) {
                                PostProcessor.this.mController.onCaptureDone();
                            } else {
                                PostProcessor.this.mController.showCapturedReview(bArr, orientation);
                            }
                        } else {
                            byte[] bArr2 = bArr;
                            PostProcessor.this.mActivity.getMediaSaveService().addImage(bArr, str2, j2, null, image.getCropRect().width(), image.getCropRect().height(), orientation, exif, PostProcessor.this.mController.getMediaSavedListener(), PostProcessor.this.mActivity.getContentResolver(), PhotoModule.PIXEL_FORMAT_JPEG);
                            PostProcessor.this.mController.updateThumbnailJpegData(bArr2);
                        }
                        image.close();
                    }
                });
            }
        }
    };

    private class BitmapOutputStream extends ByteArrayOutputStream {
        public BitmapOutputStream(int i) {
            super(i);
        }

        public byte[] getArray() {
            return this.buf;
        }
    }

    class ImageHandlerTask implements Runnable, OnImageAvailableListener {
        private ImageWrapper mImageWrapper = null;
        Semaphore mMutureLock = new Semaphore(1);
        private ImageReader mRawImageReader = null;

        ImageHandlerTask() {
        }

        public void onImageAvailable(ImageReader imageReader) {
            ImageItem imageItem;
            Message message = new Message();
            message.what = 0;
            PostProcessor.this.myHandler.sendMessage(message);
            if (!PostProcessor.this.mProcessZSL) {
                Image acquireNextImage = imageReader.acquireNextImage();
                if (acquireNextImage != null) {
                    acquireNextImage.close();
                }
                if (PostProcessor.this.mSaveRaw) {
                    ImageReader imageReader2 = this.mRawImageReader;
                    if (imageReader2 != null) {
                        Image acquireNextImage2 = imageReader2.acquireNextImage();
                        if (acquireNextImage2 != null) {
                            acquireNextImage2.close();
                        }
                    }
                }
                return;
            }
            try {
                Image image = null;
                if (!PostProcessor.this.mUseZSL) {
                    Image acquireNextImage3 = imageReader.acquireNextImage();
                    if (acquireNextImage3 != null) {
                        PostProcessor.this.onImageToProcess(acquireNextImage3);
                        if (PostProcessor.this.mSaveRaw && this.mRawImageReader != null) {
                            image = this.mRawImageReader.acquireNextImage();
                        }
                        if (image != null) {
                            PostProcessor.this.onRawImageToProcess(image);
                        }
                    }
                } else if (!PostProcessor.this.mController.isLongShotActive() || PostProcessor.this.mPendingContinuousRequestCount <= 0) {
                    if (PostProcessor.this.mIsZSLFallOff) {
                        if (PostProcessor.this.mZSLQueue != null) {
                            if (PostProcessor.this.mZSLFallOffResult != null) {
                                imageItem = PostProcessor.this.mZSLQueue.tryToGetFallOffImage(PostProcessor.this.mZSLFallOffResult, ((double) ((Long) PostProcessor.this.mZSLFallOffResult.get(CaptureResult.SENSOR_TIMESTAMP)).longValue()) + 2.0E7d);
                            } else {
                                imageItem = null;
                            }
                            if (imageItem != null) {
                                PostProcessor.this.reprocessImage(imageItem.getImage(), imageItem.getMetadata());
                                Image rawImage = imageItem.getRawImage();
                                if (rawImage != null) {
                                    PostProcessor.this.onRawImageToProcess(rawImage);
                                }
                                PostProcessor.this.mIsZSLFallOff = false;
                                PostProcessor.this.mZSLFallOffResult = null;
                            }
                        } else {
                            return;
                        }
                    }
                    Image acquireLatestImage = imageReader.acquireLatestImage();
                    if (PostProcessor.this.mSaveRaw && this.mRawImageReader != null) {
                        image = this.mRawImageReader.acquireLatestImage();
                    }
                    if (acquireLatestImage != null) {
                        if (!this.mMutureLock.tryAcquire()) {
                            acquireLatestImage.close();
                            if (image != null) {
                                image.close();
                            }
                            return;
                        }
                        if (this.mImageWrapper != null) {
                            if (!this.mImageWrapper.isTaken()) {
                                acquireLatestImage.close();
                                if (image != null) {
                                    image.close();
                                }
                                this.mMutureLock.release();
                                return;
                            }
                        }
                        if (!PostProcessor.this.mSaveRaw || image == null) {
                            this.mImageWrapper = new ImageWrapper(acquireLatestImage);
                        } else {
                            this.mImageWrapper = new ImageWrapper(acquireLatestImage, image);
                        }
                        this.mMutureLock.release();
                        if (PostProcessor.this.mZSLHandler != null) {
                            PostProcessor.this.mZSLHandler.post(this);
                        }
                    }
                } else {
                    Image acquireNextImage4 = imageReader.acquireNextImage();
                    if (PostProcessor.this.mSaveRaw && this.mRawImageReader != null) {
                        image = this.mRawImageReader.acquireNextImage();
                    }
                    ImageItem imageItem2 = new ImageItem();
                    imageItem2.setImage(acquireNextImage4, image);
                    if (PostProcessor.this.onContinuousZSLImage(imageItem2, true)) {
                        acquireNextImage4.close();
                        if (image != null) {
                            image.close();
                        }
                    }
                }
            } catch (IllegalStateException unused) {
                Log.e(PostProcessor.TAG, "Max images has been already acquired. ");
            }
        }

        public void run() {
            Image image = this.mImageWrapper.getImage();
            Image rawImage = this.mImageWrapper.getRawImage();
            try {
                this.mMutureLock.acquire();
                if (PostProcessor.this.mUseZSL && PostProcessor.this.mZSLQueue != null) {
                    PostProcessor.this.mZSLQueue.add(image, rawImage);
                }
                this.mMutureLock.release();
            } catch (InterruptedException unused) {
            }
        }

        public void setRawImageReader(ImageReader imageReader) {
            this.mRawImageReader = imageReader;
        }
    }

    private class ImageWrapper {
        Image mImage;
        boolean mIsTaken;
        Image mRawImage;

        public ImageWrapper(Image image) {
            this.mImage = image;
            this.mRawImage = null;
            this.mIsTaken = false;
        }

        public ImageWrapper(Image image, Image image2) {
            this.mImage = image;
            this.mRawImage = image2;
            this.mIsTaken = false;
        }

        public boolean isTaken() {
            return this.mIsTaken;
        }

        public Image getImage() {
            this.mIsTaken = true;
            return this.mImage;
        }

        public Image getRawImage() {
            return this.mRawImage;
        }
    }

    class ProcessorHandler extends Handler {
        boolean isRunning = true;

        public ProcessorHandler(Looper looper) {
            super(looper);
        }

        public void setInActive() {
            this.isRunning = false;
        }
    }

    enum STATUS {
        DEINIT,
        INIT,
        BUSY
    }

    class WatchdogThread extends Thread {
        private int counter = 0;
        private boolean isAlive = true;
        private boolean isMonitor = false;

        WatchdogThread() {
        }

        public void run() {
            while (this.isAlive) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException unused) {
                }
                if (this.isMonitor) {
                    this.counter++;
                    if (this.counter >= 100) {
                        bark();
                        return;
                    }
                }
            }
        }

        public void startMonitor() {
            this.isMonitor = true;
        }

        public void stopMonitor() {
            this.isMonitor = false;
            this.counter = 0;
        }

        public void kill() {
            this.isAlive = false;
        }

        private void bark() {
            Log.e(PostProcessor.TAG, "It takes too long to get the images and process the filter!");
            int filterIndex = PostProcessor.this.getFilterIndex();
            PostProcessor.this.setFilter(0);
            PostProcessor.this.setFilter(filterIndex);
        }
    }

    /* access modifiers changed from: private */
    public native int nativeFlipNV21(byte[] bArr, int i, int i2, int i3, boolean z);

    private native int nativeNV21Split(byte[] bArr, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3, int i4);

    private native int nativeResizeImage(byte[] bArr, byte[] bArr2, int i, int i2, int i3, int i4, int i5);

    static {
        boolean z = false;
        if (PersistUtil.getCamera2Debug() == 2 || PersistUtil.getCamera2Debug() == 100) {
            z = true;
        }
        DEBUG_ZSL = z;
        System.loadLibrary("jni_imageutil");
    }

    public int getMaxRequiredImageNum() {
        return this.mMaxRequiredImageNum;
    }

    public boolean isZSLEnabled() {
        return this.mUseZSL;
    }

    public void onStartCapturing() {
        this.mTotalCaptureResultList.clear();
    }

    public ImageReader getZSLReprocessImageReader() {
        return this.mZSLReprocessImageReader;
    }

    public ImageHandlerTask getImageHandler() {
        return this.mImageHandlerTask;
    }

    public void setRawImageReader(ImageReader imageReader) {
        this.mImageHandlerTask.setRawImageReader(imageReader);
    }

    public void onMetaAvailable(TotalCaptureResult totalCaptureResult) {
        if (this.mUseZSL) {
            ZSLQueue zSLQueue = this.mZSLQueue;
            if (zSLQueue != null) {
                zSLQueue.add(totalCaptureResult);
            }
        }
        this.mLatestResultForLongShot = totalCaptureResult;
        updateFusionStatus(totalCaptureResult);
    }

    private void updateFusionStatus(CaptureResult captureResult) {
        try {
            boolean z = true;
            if (captureResult.get(CaptureModule.fusionStatus) == null || ((Byte) captureResult.get(CaptureModule.fusionStatus)).byteValue() != 1) {
                z = false;
            }
            this.fusionStatus = z;
        } catch (IllegalArgumentException unused) {
            StringBuilder sb = new StringBuilder();
            sb.append("can't find vendor tag: ");
            sb.append(CaptureModule.fusionStatus.toString());
            Log.w(TAG, sb.toString());
        }
    }

    public CaptureCallback getCaptureCallback() {
        return this.mCaptureCallback;
    }

    public void onSessionConfigured(CameraDevice cameraDevice, CameraCaptureSession cameraCaptureSession) {
        this.mCameraDevice = cameraDevice;
        this.mCaptureSession = cameraCaptureSession;
        if (this.mUseZSL) {
            this.mImageWriter = ImageWriter.newInstance(cameraCaptureSession.getInputSurface(), this.mMaxRequiredImageNum);
        }
    }

    public void onImageReaderReady(ImageReader imageReader, Size size, Size size2) {
        this.mImageReader = imageReader;
        if (this.mUseZSL) {
            this.mZSLReprocessImageReader = ImageReader.newInstance(size2.getWidth(), size2.getHeight(), 256, this.mMaxRequiredImageNum);
            this.mZSLReprocessImageReader.setOnImageAvailableListener(this.processedImageAvailableListener, this.mHandler);
        }
    }

    public boolean takeZSLPicture() {
        if (this.mZSLQueue == null) {
            return false;
        }
        this.mController.setJpegImageData(null);
        ImageItem tryToGetMatchingItem = this.mZSLQueue.tryToGetMatchingItem();
        CaptureResult previewCaptureResult = this.mController.getPreviewCaptureResult();
        String str = TAG;
        if (previewCaptureResult == null || ((Integer) this.mController.getPreviewCaptureResult().get(CaptureResult.CONTROL_AE_STATE)).intValue() == 4) {
            if (DEBUG_ZSL) {
                Log.d(str, "Flash required image");
            }
            if (tryToGetMatchingItem != null) {
                tryToGetMatchingItem.closeImage();
            }
            tryToGetMatchingItem = null;
        }
        if (this.mController.isSelfieFlash()) {
            if (tryToGetMatchingItem != null) {
                tryToGetMatchingItem.closeImage();
            }
            tryToGetMatchingItem = null;
        }
        if (this.mController.isLongShotActive()) {
            if (tryToGetMatchingItem != null) {
                tryToGetMatchingItem.closeImage();
            }
            tryToGetMatchingItem = null;
        }
        if (tryToGetMatchingItem != null) {
            if (DEBUG_ZSL) {
                Log.d(str, "Got the item from the queue");
            }
            reprocessImage(tryToGetMatchingItem.getImage(), tryToGetMatchingItem.getMetadata());
            if (this.mSaveRaw && tryToGetMatchingItem.getRawImage() != null) {
                onRawImageToProcess(tryToGetMatchingItem.getRawImage());
            }
            return true;
        }
        if (DEBUG_ZSL) {
            Log.d(str, "No good item in queue, register the request for the future");
        }
        if (this.mController.isLongShotActive()) {
            if (DEBUG_ZSL) {
                Log.d(str, "Long shot active in ZSL");
            }
            this.mPendingContinuousRequestCount = PersistUtil.getLongshotShotLimit();
            return true;
        }
        this.mIsZSLFallOff = true;
        return false;
    }

    public boolean onContinuousZSLImage(ImageItem imageItem, boolean z) {
        if (!z) {
            reprocessImage(imageItem.getImage(), imageItem.getMetadata());
            if (imageItem.getRawImage() != null) {
                onRawImageToProcess(imageItem.getRawImage());
            }
            return true;
        } else if (this.mLatestResultForLongShot == null) {
            return false;
        } else {
            reprocessImage(imageItem.getImage(), this.mLatestResultForLongShot);
            if (imageItem.getRawImage() != null) {
                onRawImageToProcess(imageItem.getRawImage());
            }
            this.mPendingContinuousRequestCount--;
            return true;
        }
    }

    public void stopLongShot() {
        this.mPendingContinuousRequestCount = 0;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Missing exception handler attribute for start block: B:49:0x019e */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reprocessImage(android.media.Image r11, android.hardware.camera2.TotalCaptureResult r12) {
        /*
            r10 = this;
            com.android.camera.CaptureModule r0 = r10.mController
            boolean r0 = r0.isLongShotActive()
            if (r0 == 0) goto L_0x0011
            com.android.camera.CaptureModule r0 = r10.mController
            int r1 = r0.getMainCameraId()
            r0.checkAndPlayShutterSound(r1)
        L_0x0011:
            java.lang.Object r0 = r10.lock
            monitor-enter(r0)
            android.hardware.camera2.CameraDevice r1 = r10.mCameraDevice     // Catch:{ all -> 0x01ac }
            if (r1 == 0) goto L_0x01a0
            android.hardware.camera2.CameraCaptureSession r1 = r10.mCaptureSession     // Catch:{ all -> 0x01ac }
            if (r1 == 0) goto L_0x01a0
            android.media.ImageReader r1 = r10.mImageReader     // Catch:{ all -> 0x01ac }
            if (r1 != 0) goto L_0x0022
            goto L_0x01a0
        L_0x0022:
            android.media.ImageReader r1 = r10.mZSLReprocessImageReader     // Catch:{ all -> 0x01ac }
            if (r1 != 0) goto L_0x002b
            r11.close()     // Catch:{ all -> 0x01ac }
            monitor-exit(r0)     // Catch:{ all -> 0x01ac }
            return
        L_0x002b:
            com.android.camera.SettingsManager r1 = com.android.camera.SettingsManager.getInstance()     // Catch:{ all -> 0x01ac }
            java.lang.String r2 = "pref_camera_jpegquality_key"
            java.lang.String r1 = r1.getValue(r2)     // Catch:{ all -> 0x01ac }
            r2 = 85
            if (r1 == 0) goto L_0x003d
            int r2 = com.android.camera.CaptureModule.getQualityNumber(r1)     // Catch:{ all -> 0x01ac }
        L_0x003d:
            boolean r1 = DEBUG_ZSL     // Catch:{ all -> 0x01ac }
            if (r1 == 0) goto L_0x005b
            java.lang.String r1 = "PostProcessor"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x01ac }
            r3.<init>()     // Catch:{ all -> 0x01ac }
            java.lang.String r4 = "reprocess Image request "
            r3.append(r4)     // Catch:{ all -> 0x01ac }
            long r4 = r11.getTimestamp()     // Catch:{ all -> 0x01ac }
            r3.append(r4)     // Catch:{ all -> 0x01ac }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x01ac }
            android.util.Log.d(r1, r3)     // Catch:{ all -> 0x01ac }
        L_0x005b:
            boolean r1 = r10.fusionStatus     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            if (r1 == 0) goto L_0x0067
            android.hardware.camera2.CameraDevice r12 = r10.mCameraDevice     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r1 = 5
            android.hardware.camera2.CaptureRequest$Builder r12 = r12.createCaptureRequest(r1)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            goto L_0x006d
        L_0x0067:
            android.hardware.camera2.CameraDevice r1 = r10.mCameraDevice     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.hardware.camera2.CaptureRequest$Builder r12 = r1.createReprocessCaptureRequest(r12)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
        L_0x006d:
            android.hardware.camera2.CaptureRequest$Key r1 = android.hardware.camera2.CaptureRequest.JPEG_ORIENTATION     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            com.android.camera.CaptureModule r3 = r10.mController     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            int r3 = r3.getMainCameraId()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            com.android.camera.CaptureModule r4 = r10.mController     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            int r4 = r4.getDisplayOrientation()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            int r3 = com.android.camera.util.CameraUtil.getJpegRotation(r3, r4)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r12.set(r1, r3)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.hardware.camera2.CaptureRequest$Key r1 = android.hardware.camera2.CaptureRequest.JPEG_QUALITY     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            byte r2 = (byte) r2     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            java.lang.Byte r2 = java.lang.Byte.valueOf(r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r12.set(r1, r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.hardware.camera2.CaptureRequest$Key r1 = android.hardware.camera2.CaptureRequest.JPEG_THUMBNAIL_SIZE     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            com.android.camera.CaptureModule r2 = r10.mController     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.util.Size r2 = r2.getThumbSize()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r12.set(r1, r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.hardware.camera2.CaptureRequest$Key r1 = android.hardware.camera2.CaptureRequest.JPEG_THUMBNAIL_QUALITY     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r2 = 80
            java.lang.Byte r2 = java.lang.Byte.valueOf(r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r12.set(r1, r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.hardware.camera2.CaptureRequest$Key r1 = android.hardware.camera2.CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r2 = 2
            java.lang.Integer r3 = java.lang.Integer.valueOf(r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r12.set(r1, r3)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.hardware.camera2.CaptureRequest$Key r1 = android.hardware.camera2.CaptureRequest.EDGE_MODE     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r12.set(r1, r3)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.hardware.camera2.CaptureRequest$Key r1 = android.hardware.camera2.CaptureRequest.NOISE_REDUCTION_MODE     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r12.set(r1, r3)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r1 = 1
            java.lang.Byte r3 = java.lang.Byte.valueOf(r1)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            com.android.camera.util.VendorTagUtil.setJpegCropEnable(r12, r3)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.graphics.Rect r3 = r11.getCropRect()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r4 = 0
            if (r3 == 0) goto L_0x00d7
            boolean r5 = r3.isEmpty()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            if (r5 == 0) goto L_0x00e4
        L_0x00d7:
            android.graphics.Rect r3 = new android.graphics.Rect     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            int r5 = r11.getWidth()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            int r6 = r11.getHeight()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r3.<init>(r4, r4, r5, r6)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
        L_0x00e4:
            android.media.ImageReader r5 = r10.mZSLReprocessImageReader     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            int r5 = r5.getWidth()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.media.ImageReader r6 = r10.mZSLReprocessImageReader     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            int r6 = r6.getHeight()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            float r7 = (float) r5     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            float r8 = (float) r6     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            float r7 = r7 / r8
            android.graphics.Rect r3 = com.android.camera.util.CameraUtil.getFinalCropRect(r3, r7)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r7 = 4
            int[] r8 = new int[r7]     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            int r9 = r3.left     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r8[r4] = r9     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            int r9 = r3.top     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r8[r1] = r9     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            int r9 = r3.width()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r8[r2] = r9     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            int r3 = r3.height()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r9 = 3
            r8[r9] = r3     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            com.android.camera.util.VendorTagUtil.setJpegCropRect(r12, r8)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            int[] r3 = new int[r7]     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r3[r4] = r4     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r3[r1] = r4     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r3[r2] = r5     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r3[r9] = r6     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            com.android.camera.util.VendorTagUtil.setJpegRoiRect(r12, r3)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            com.android.camera.CaptureModule r1 = r10.mController     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            com.android.camera.LocationManager r1 = r1.getLocationManager()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.location.Location r1 = r1.getCurrentLocation()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            if (r1 == 0) goto L_0x0159
            android.location.Location r2 = new android.location.Location     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r2.<init>(r1)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            java.lang.String r1 = "PostProcessor"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r3.<init>()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            java.lang.String r4 = "sendReprocessRequest gps: "
            r3.append(r4)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            java.lang.String r4 = r2.toString()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r3.append(r4)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            java.lang.String r3 = r3.toString()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.util.Log.d(r1, r3)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            long r3 = r2.getTime()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r5 = 1000(0x3e8, double:4.94E-321)
            long r3 = r3 / r5
            r2.setTime(r3)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.hardware.camera2.CaptureRequest$Key r1 = android.hardware.camera2.CaptureRequest.JPEG_GPS_LOCATION     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r12.set(r1, r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
        L_0x0159:
            android.media.ImageReader r1 = r10.mZSLReprocessImageReader     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.view.Surface r1 = r1.getSurface()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r12.addTarget(r1)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            com.android.camera.CaptureModule r1 = r10.mController     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            com.android.camera.CaptureModule r2 = r10.mController     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            int r2 = r2.getMainCameraId()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.view.Surface r1 = r1.getPreviewSurfaceForSession(r2)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r12.addTarget(r1)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            boolean r1 = r10.fusionStatus     // Catch:{ IllegalStateException -> 0x0187, RuntimeException -> 0x017b }
            if (r1 != 0) goto L_0x018e
            android.media.ImageWriter r1 = r10.mImageWriter     // Catch:{ IllegalStateException -> 0x0187, RuntimeException -> 0x017b }
            r1.queueInputImage(r11)     // Catch:{ IllegalStateException -> 0x0187, RuntimeException -> 0x017b }
            goto L_0x018e
        L_0x017b:
            r10 = move-exception
            r10.printStackTrace()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            java.lang.IllegalStateException r10 = new java.lang.IllegalStateException     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            java.lang.String r11 = "nativeDetachImage failed for image"
            r10.<init>(r11)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            throw r10     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
        L_0x0187:
            java.lang.String r11 = "PostProcessor"
            java.lang.String r1 = "Queueing more than it can have"
            android.util.Log.e(r11, r1)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
        L_0x018e:
            android.hardware.camera2.CameraCaptureSession r11 = r10.mCaptureSession     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            android.hardware.camera2.CaptureRequest r12 = r12.build()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            com.android.camera.imageprocessor.PostProcessor$3 r1 = new com.android.camera.imageprocessor.PostProcessor$3     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r1.<init>()     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            com.android.camera.imageprocessor.PostProcessor$ProcessorHandler r10 = r10.mHandler     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
            r11.capture(r12, r1, r10)     // Catch:{ CameraAccessException | IllegalStateException -> 0x019e }
        L_0x019e:
            monitor-exit(r0)     // Catch:{ all -> 0x01ac }
            return
        L_0x01a0:
            java.lang.String r10 = "PostProcessor"
            java.lang.String r12 = "Reprocess request is called even before taking picture"
            android.util.Log.e(r10, r12)     // Catch:{ all -> 0x01ac }
            r11.close()     // Catch:{ all -> 0x01ac }
            monitor-exit(r0)     // Catch:{ all -> 0x01ac }
            return
        L_0x01ac:
            r10 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x01ac }
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.imageprocessor.PostProcessor.reprocessImage(android.media.Image, android.hardware.camera2.TotalCaptureResult):void");
    }

    /* access modifiers changed from: private */
    public void onImageToProcess(Image image) {
        String str;
        long j;
        addImage(image);
        if (isReadyToProcess()) {
            CaptureModule captureModule = this.mController;
            captureModule.unlockFocus(captureModule.getMainCameraId());
            this.mNamedImages.nameNewImage(System.currentTimeMillis());
            NamedEntity nextNameEntity = this.mNamedImages.getNextNameEntity();
            if (nextNameEntity == null) {
                str = null;
            } else {
                str = nextNameEntity.title;
            }
            String str2 = str;
            if (nextNameEntity == null) {
                j = -1;
            } else {
                j = nextNameEntity.date;
            }
            processImage(str2, j, this.mController.getMediaSavedListener(), this.mActivity.getContentResolver());
        }
    }

    /* access modifiers changed from: private */
    public void onRawImageToProcess(Image image) {
        String str;
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bArr = new byte[buffer.remaining()];
        buffer.get(bArr);
        this.mNamedImages.nameNewImage(System.currentTimeMillis());
        NamedEntity nextNameEntity = this.mNamedImages.getNextNameEntity();
        if (nextNameEntity == null) {
            str = null;
        } else {
            str = nextNameEntity.title;
        }
        this.mActivity.getMediaSaveService().addRawImage(bArr, str, "raw");
        image.close();
    }

    public PostProcessor(CameraActivity cameraActivity, CaptureModule captureModule) {
        this.mController = captureModule;
        this.mActivity = cameraActivity;
        this.mNamedImages = new NamedImages();
    }

    public boolean isItBusy() {
        return this.mStatus == STATUS.BUSY;
    }

    public List<CaptureRequest> setRequiredImages(Builder builder) {
        ImageFilter imageFilter = this.mFilter;
        if (imageFilter != null) {
            return imageFilter.setRequiredImages(builder);
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add(builder.build());
        return arrayList;
    }

    public boolean isManualMode() {
        return this.mFilter.isManualMode();
    }

    public void manualCapture(final Builder builder, final CameraCaptureSession cameraCaptureSession, final Handler handler) throws CameraAccessException {
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    PostProcessor.this.mFilter.manualCapture(builder, cameraCaptureSession, PostProcessor.this.mCaptureCallback, handler);
                } catch (IllegalStateException unused) {
                    Log.w(PostProcessor.TAG, "Session is closed while taking manual pictures ");
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isFilterOn() {
        return this.mFilter != null;
    }

    public byte[] getFrontMirrorData(int i, ResultImage resultImage, TotalCaptureResult totalCaptureResult) {
        boolean z = (i == 0 || i == 180) ? false : true;
        if (isSelfieMirrorOn()) {
            byte[] bArr = resultImage.outBufferArray;
            int i2 = resultImage.stride;
            nativeFlipNV21(bArr, i2, resultImage.height, i2 - resultImage.width, z);
        }
        return nv21ToJpeg(resultImage, i, totalCaptureResult);
    }

    public boolean isSelfieMirrorOn() {
        if (SettingsManager.getInstance() != null) {
            SettingsManager instance = SettingsManager.getInstance();
            String str = SettingsManager.KEY_SELFIEMIRROR;
            if (instance.getValue(str) != null && SettingsManager.getInstance().getValue(str).equalsIgnoreCase(RecordLocationPreference.VALUE_ON)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0076, code lost:
        if ("1".equals(com.android.camera.SettingsManager.getInstance().getValue(com.android.camera.SettingsManager.KEY_HDR_MODE)) != false) goto L_0x0078;
     */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00b2  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00c6  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onOpen(int r2, boolean r3, boolean r4, boolean r5, boolean r6, boolean r7, boolean r8) {
        /*
            r1 = this;
            com.android.camera.imageprocessor.PostProcessor$ImageHandlerTask r0 = new com.android.camera.imageprocessor.PostProcessor$ImageHandlerTask
            r0.<init>()
            r1.mImageHandlerTask = r0
            r1.mSaveRaw = r7
            boolean r2 = r1.setFilter(r2)
            r7 = 0
            if (r2 != 0) goto L_0x0091
            if (r3 != 0) goto L_0x0091
            if (r4 != 0) goto L_0x0091
            if (r5 != 0) goto L_0x0091
            if (r6 != 0) goto L_0x0091
            boolean r2 = com.android.camera.util.PersistUtil.getCameraZSLDisabled()
            if (r2 != 0) goto L_0x0091
            com.android.camera.SettingsManager r2 = com.android.camera.SettingsManager.getInstance()
            boolean r2 = r2.isZSLInAppEnabled()
            if (r2 == 0) goto L_0x0091
            com.android.camera.SettingsManager r2 = com.android.camera.SettingsManager.getInstance()
            java.lang.String r3 = "pref_camera2_scenemode_key"
            java.lang.String r2 = r2.getValue(r3)
            java.lang.String r4 = "110"
            boolean r2 = r4.equals(r2)
            if (r2 != 0) goto L_0x0091
            com.android.camera.SettingsManager r2 = com.android.camera.SettingsManager.getInstance()
            java.lang.String r4 = "pref_camera2_auto_hdr_key"
            java.lang.String r2 = r2.getValue(r4)
            java.lang.String r4 = "enable"
            boolean r2 = r4.equals(r2)
            if (r2 != 0) goto L_0x0091
            com.android.camera.SettingsManager r2 = com.android.camera.SettingsManager.getInstance()
            boolean r2 = r2.isCamera2HDRSupport()
            if (r2 != 0) goto L_0x0091
            com.android.camera.SettingsManager r2 = com.android.camera.SettingsManager.getInstance()
            java.lang.String r2 = r2.getValue(r3)
            java.lang.String r3 = "18"
            boolean r2 = r3.equals(r2)
            if (r2 == 0) goto L_0x0078
            com.android.camera.SettingsManager r2 = com.android.camera.SettingsManager.getInstance()
            java.lang.String r3 = "pref_camera2_hdr_mode_key"
            java.lang.String r2 = r2.getValue(r3)
            java.lang.String r3 = "1"
            boolean r2 = r3.equals(r2)
            if (r2 == 0) goto L_0x0091
        L_0x0078:
            com.android.camera.CaptureModule r2 = r1.mController
            int r2 = r2.getCameraMode()
            if (r2 == 0) goto L_0x0091
            if (r8 != 0) goto L_0x0091
            com.android.camera.SettingsManager r2 = com.android.camera.SettingsManager.getInstance()
            int r2 = r2.getSavePictureFormat()
            r3 = 1
            if (r2 != r3) goto L_0x008e
            goto L_0x0091
        L_0x008e:
            r1.mUseZSL = r3
            goto L_0x0093
        L_0x0091:
            r1.mUseZSL = r7
        L_0x0093:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "ZSL is "
            r2.append(r3)
            boolean r3 = r1.mUseZSL
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            java.lang.String r3 = "PostProcessor"
            android.util.Log.d(r3, r2)
            r1.startBackgroundThread()
            boolean r2 = r1.mUseZSL
            if (r2 == 0) goto L_0x00bb
            com.android.camera.imageprocessor.ZSLQueue r2 = new com.android.camera.imageprocessor.ZSLQueue
            com.android.camera.CaptureModule r3 = r1.mController
            r2.<init>(r3)
            r1.mZSLQueue = r2
        L_0x00bb:
            r2 = 3
            r1.mMaxRequiredImageNum = r2
            com.android.camera.CaptureModule r3 = r1.mController
            boolean r3 = r3.isLongShotSettingEnabled()
            if (r3 == 0) goto L_0x00d2
            int r3 = com.android.camera.util.PersistUtil.getLongshotShotLimit()
            int r3 = r3 + 2
            int r2 = java.lang.Math.max(r2, r3)
            r1.mMaxRequiredImageNum = r2
        L_0x00d2:
            r1.mPendingContinuousRequestCount = r7
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.imageprocessor.PostProcessor.onOpen(int, boolean, boolean, boolean, boolean, boolean, boolean):void");
    }

    public int getFilterIndex() {
        return this.mFilterIndex;
    }

    public void onClose() {
        synchronized (this.lock) {
            if (this.mHandler != null) {
                this.mHandler.setInActive();
            }
        }
        stopBackgroundThread();
        setFilter(0);
        ZSLQueue zSLQueue = this.mZSLQueue;
        if (zSLQueue != null) {
            zSLQueue.onClose();
            this.mZSLQueue = null;
        }
        ImageWriter imageWriter = this.mImageWriter;
        if (imageWriter != null) {
            imageWriter.close();
            this.mImageWriter = null;
        }
        ImageReader imageReader = this.mZSLReprocessImageReader;
        if (imageReader != null) {
            imageReader.close();
            this.mZSLReprocessImageReader = null;
        }
        this.mCameraDevice = null;
        this.mCaptureSession = null;
        this.mImageReader = null;
        this.mPendingContinuousRequestCount = 0;
    }

    public void enableZSLQueue(boolean z) {
        if (z) {
            this.mProcessZSL = true;
            return;
        }
        this.mProcessZSL = false;
        ZSLQueue zSLQueue = this.mZSLQueue;
        if (zSLQueue != null) {
            zSLQueue.clear();
        }
    }

    private void startBackgroundThread() {
        this.mHandlerThread = new HandlerThread("PostProcessorThread");
        this.mHandlerThread.start();
        this.mHandler = new ProcessorHandler(this.mHandlerThread.getLooper());
        this.mZSLHandlerThread = new HandlerThread("ZSLHandlerThread");
        this.mZSLHandlerThread.start();
        this.mZSLHandler = new ProcessorHandler(this.mZSLHandlerThread.getLooper());
        this.mSavingHandlerThread = new HandlerThread("SavingHandlerThread");
        this.mSavingHandlerThread.start();
        this.mSavingHander = new ProcessorHandler(this.mSavingHandlerThread.getLooper());
        this.mWatchdog = new WatchdogThread();
        this.mWatchdog.start();
    }

    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0022 */
    /* JADX WARNING: Missing exception handler attribute for start block: B:22:0x0034 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void stopBackgroundThread() {
        /*
            r3 = this;
            android.os.HandlerThread r0 = r3.mHandlerThread
            r1 = 0
            if (r0 == 0) goto L_0x0011
            r0.quitSafely()
            android.os.HandlerThread r0 = r3.mHandlerThread     // Catch:{ InterruptedException -> 0x000d }
            r0.join()     // Catch:{ InterruptedException -> 0x000d }
        L_0x000d:
            r3.mHandlerThread = r1
            r3.mHandler = r1
        L_0x0011:
            java.lang.Object r0 = r3.lock
            monitor-enter(r0)
            android.os.HandlerThread r2 = r3.mZSLHandlerThread     // Catch:{ all -> 0x0048 }
            if (r2 == 0) goto L_0x0026
            android.os.HandlerThread r2 = r3.mZSLHandlerThread     // Catch:{ all -> 0x0048 }
            r2.quitSafely()     // Catch:{ all -> 0x0048 }
            android.os.HandlerThread r2 = r3.mZSLHandlerThread     // Catch:{ InterruptedException -> 0x0022 }
            r2.join()     // Catch:{ InterruptedException -> 0x0022 }
        L_0x0022:
            r3.mZSLHandlerThread = r1     // Catch:{ all -> 0x0048 }
            r3.mZSLHandler = r1     // Catch:{ all -> 0x0048 }
        L_0x0026:
            android.os.HandlerThread r2 = r3.mSavingHandlerThread     // Catch:{ all -> 0x0048 }
            if (r2 == 0) goto L_0x0038
            android.os.HandlerThread r2 = r3.mSavingHandlerThread     // Catch:{ all -> 0x0048 }
            r2.quitSafely()     // Catch:{ all -> 0x0048 }
            android.os.HandlerThread r2 = r3.mSavingHandlerThread     // Catch:{ InterruptedException -> 0x0034 }
            r2.join()     // Catch:{ InterruptedException -> 0x0034 }
        L_0x0034:
            r3.mSavingHandlerThread = r1     // Catch:{ all -> 0x0048 }
            r3.mSavingHander = r1     // Catch:{ all -> 0x0048 }
        L_0x0038:
            com.android.camera.imageprocessor.PostProcessor$WatchdogThread r2 = r3.mWatchdog     // Catch:{ all -> 0x0048 }
            if (r2 == 0) goto L_0x0043
            com.android.camera.imageprocessor.PostProcessor$WatchdogThread r2 = r3.mWatchdog     // Catch:{ all -> 0x0048 }
            r2.kill()     // Catch:{ all -> 0x0048 }
            r3.mWatchdog = r1     // Catch:{ all -> 0x0048 }
        L_0x0043:
            r3.clear()     // Catch:{ all -> 0x0048 }
            monitor-exit(r0)     // Catch:{ all -> 0x0048 }
            return
        L_0x0048:
            r3 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0048 }
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.imageprocessor.PostProcessor.stopBackgroundThread():void");
    }

    public boolean setFilter(int i) {
        if (i < 0 || i >= 8) {
            Log.e(TAG, "Invalid scene filter ID");
            return false;
        }
        synchronized (this.lock) {
            if (this.mFilter != null) {
                this.mFilter.deinit();
            }
            this.mStatus = STATUS.DEINIT;
            switch (i) {
                case 0:
                    this.mFilter = null;
                    break;
                case 1:
                    this.mFilter = new OptizoomFilter(this.mController);
                    break;
                case 2:
                    this.mFilter = new SharpshooterFilter(this.mController);
                    break;
                case 3:
                    this.mFilter = new UbifocusFilter(this.mController, this.mActivity, this);
                    break;
                case 4:
                    this.mFilter = new StillmoreFilter(this.mController);
                    break;
                case 5:
                    this.mFilter = new BestpictureFilter(this.mController, this.mActivity, this);
                    break;
                case 6:
                    this.mFilter = new ChromaflashFilter(this.mController);
                    break;
                case 7:
                    this.mFilter = new BlurbusterFilter(this.mController);
                    break;
            }
        }
        ImageFilter imageFilter = this.mFilter;
        if (imageFilter != null && !imageFilter.isSupported()) {
            final String stringName = this.mFilter.getStringName();
            this.mFilter = null;
            this.mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    CameraActivity access$000 = PostProcessor.this.mActivity;
                    StringBuilder sb = new StringBuilder();
                    sb.append(stringName);
                    sb.append(" is not supported. ");
                    RotateTextToast.makeText((Activity) access$000, (CharSequence) sb.toString(), 0).show();
                }
            });
        }
        ImageFilter imageFilter2 = this.mFilter;
        if (imageFilter2 == null) {
            this.mFilterIndex = 0;
            return false;
        }
        this.mFilterIndex = i;
        this.mImages = new Image[imageFilter2.getNumRequiredImage()];
        return true;
    }

    private boolean isReadyToProcess() {
        synchronized (this.lock) {
            if (this.mFilter == null) {
                return true;
            }
            if (this.mCurrentNumImage >= this.mFilter.getNumRequiredImage()) {
                return true;
            }
            return false;
        }
    }

    private void addImage(final Image image) {
        final ProcessorHandler processorHandler = this.mHandler;
        if (processorHandler != null && processorHandler.isRunning) {
            if (this.mStatus == STATUS.DEINIT) {
                this.mWidth = image.getWidth();
                this.mHeight = image.getHeight();
                this.mStride = image.getPlanes()[0].getRowStride();
                this.mStatus = STATUS.INIT;
                this.mHandler.post(new Runnable() {
                    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003a, code lost:
                        return;
                     */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void run() {
                        /*
                            r5 = this;
                            com.android.camera.imageprocessor.PostProcessor r0 = com.android.camera.imageprocessor.PostProcessor.this
                            java.lang.Object r0 = r0.lock
                            monitor-enter(r0)
                            com.android.camera.imageprocessor.PostProcessor$ProcessorHandler r1 = r0     // Catch:{ all -> 0x003b }
                            boolean r1 = r1.isRunning     // Catch:{ all -> 0x003b }
                            if (r1 != 0) goto L_0x000f
                            monitor-exit(r0)     // Catch:{ all -> 0x003b }
                            return
                        L_0x000f:
                            com.android.camera.imageprocessor.PostProcessor r1 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x003b }
                            com.android.camera.imageprocessor.filter.ImageFilter r1 = r1.mFilter     // Catch:{ all -> 0x003b }
                            if (r1 != 0) goto L_0x0018
                            goto L_0x0039
                        L_0x0018:
                            com.android.camera.imageprocessor.PostProcessor r1 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x003b }
                            com.android.camera.imageprocessor.filter.ImageFilter r1 = r1.mFilter     // Catch:{ all -> 0x003b }
                            com.android.camera.imageprocessor.PostProcessor r2 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x003b }
                            int r2 = r2.mWidth     // Catch:{ all -> 0x003b }
                            com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x003b }
                            int r3 = r3.mHeight     // Catch:{ all -> 0x003b }
                            com.android.camera.imageprocessor.PostProcessor r4 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x003b }
                            int r4 = r4.mStride     // Catch:{ all -> 0x003b }
                            com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x003b }
                            int r5 = r5.mStride     // Catch:{ all -> 0x003b }
                            r1.init(r2, r3, r4, r5)     // Catch:{ all -> 0x003b }
                        L_0x0039:
                            monitor-exit(r0)     // Catch:{ all -> 0x003b }
                            return
                        L_0x003b:
                            r5 = move-exception
                            monitor-exit(r0)     // Catch:{ all -> 0x003b }
                            throw r5
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.imageprocessor.PostProcessor.C08206.run():void");
                    }
                });
            }
            if (this.mCurrentNumImage == 0) {
                this.mStatus = STATUS.BUSY;
                WatchdogThread watchdogThread = this.mWatchdog;
                if (watchdogThread != null) {
                    watchdogThread.startMonitor();
                }
                this.mOrientation = CameraUtil.getJpegRotation(this.mController.getMainCameraId(), this.mController.getDisplayOrientation());
            }
            ImageFilter imageFilter = this.mFilter;
            if (imageFilter == null || this.mCurrentNumImage < imageFilter.getNumRequiredImage()) {
                final int i = this.mCurrentNumImage;
                this.mCurrentNumImage = i + 1;
                ProcessorHandler processorHandler2 = this.mHandler;
                if (processorHandler2 != null) {
                    processorHandler2.post(new Runnable() {
                        /* JADX WARNING: Code restructure failed: missing block: B:21:0x01b9, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:23:0x01bb, code lost:
                            return;
                         */
                        /* Code decompiled incorrectly, please refer to instructions dump. */
                        public void run() {
                            /*
                                r22 = this;
                                r0 = r22
                                com.android.camera.imageprocessor.PostProcessor r1 = com.android.camera.imageprocessor.PostProcessor.this
                                java.lang.Object r1 = r1.lock
                                monitor-enter(r1)
                                com.android.camera.imageprocessor.PostProcessor$ProcessorHandler r2 = r0     // Catch:{ all -> 0x01bc }
                                boolean r2 = r2.isRunning     // Catch:{ all -> 0x01bc }
                                if (r2 == 0) goto L_0x01ba
                                com.android.camera.imageprocessor.PostProcessor r2 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor$STATUS r2 = r2.mStatus     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor$STATUS r3 = com.android.camera.imageprocessor.PostProcessor.STATUS.BUSY     // Catch:{ all -> 0x01bc }
                                if (r2 == r3) goto L_0x001b
                                goto L_0x01ba
                            L_0x001b:
                                android.media.Image r2 = r5     // Catch:{ all -> 0x01bc }
                                android.media.Image$Plane[] r2 = r2.getPlanes()     // Catch:{ all -> 0x01bc }
                                r3 = 0
                                r2 = r2[r3]     // Catch:{ all -> 0x01bc }
                                java.nio.ByteBuffer r2 = r2.getBuffer()     // Catch:{ all -> 0x01bc }
                                android.media.Image r4 = r5     // Catch:{ all -> 0x01bc }
                                android.media.Image$Plane[] r4 = r4.getPlanes()     // Catch:{ all -> 0x01bc }
                                r5 = 2
                                r4 = r4[r5]     // Catch:{ all -> 0x01bc }
                                java.nio.ByteBuffer r4 = r4.getBuffer()     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r6 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.filter.ImageFilter r6 = r6.mFilter     // Catch:{ all -> 0x01bc }
                                r7 = 3
                                if (r6 != 0) goto L_0x00b9
                                com.android.camera.imageprocessor.PostProcessor r6 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.filter.ImageFilter$ResultImage r14 = new com.android.camera.imageprocessor.filter.ImageFilter$ResultImage     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r8 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r8 = r8.mStride     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r9 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r9 = r9.mHeight     // Catch:{ all -> 0x01bc }
                                int r8 = r8 * r9
                                int r8 = r8 * r7
                                int r8 = r8 / r5
                                java.nio.ByteBuffer r9 = java.nio.ByteBuffer.allocateDirect(r8)     // Catch:{ all -> 0x01bc }
                                android.graphics.Rect r10 = new android.graphics.Rect     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r5 = r5.mWidth     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r7 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r7 = r7.mHeight     // Catch:{ all -> 0x01bc }
                                r10.<init>(r3, r3, r5, r7)     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r11 = r5.mWidth     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r12 = r5.mHeight     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r13 = r5.mStride     // Catch:{ all -> 0x01bc }
                                r8 = r14
                                r8.<init>(r9, r10, r11, r12, r13)     // Catch:{ all -> 0x01bc }
                                r6.mDefaultResultImage = r14     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.filter.ImageFilter$ResultImage r5 = r5.mDefaultResultImage     // Catch:{ all -> 0x01bc }
                                java.nio.ByteBuffer r5 = r5.outBuffer     // Catch:{ all -> 0x01bc }
                                byte[] r5 = r5.array()     // Catch:{ all -> 0x01bc }
                                int r6 = r2.remaining()     // Catch:{ all -> 0x01bc }
                                r2.get(r5, r3, r6)     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r2 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.filter.ImageFilter$ResultImage r2 = r2.mDefaultResultImage     // Catch:{ all -> 0x01bc }
                                java.nio.ByteBuffer r2 = r2.outBuffer     // Catch:{ all -> 0x01bc }
                                byte[] r2 = r2.array()     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r3 = r3.mStride     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r5 = r5.mHeight     // Catch:{ all -> 0x01bc }
                                int r3 = r3 * r5
                                int r5 = r4.remaining()     // Catch:{ all -> 0x01bc }
                                r4.get(r2, r3, r5)     // Catch:{ all -> 0x01bc }
                                android.media.Image r0 = r5     // Catch:{ all -> 0x01bc }
                                r0.close()     // Catch:{ all -> 0x01bc }
                                goto L_0x01b8
                            L_0x00b9:
                                boolean r6 = com.android.camera.imageprocessor.PostProcessor.DEBUG_DUMP_FILTER_IMG     // Catch:{ all -> 0x01bc }
                                r8 = 0
                                if (r6 == 0) goto L_0x0182
                                com.android.camera.imageprocessor.filter.ImageFilter$ResultImage r6 = new com.android.camera.imageprocessor.filter.ImageFilter$ResultImage     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r9 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r9 = r9.mStride     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r10 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r10 = r10.mHeight     // Catch:{ all -> 0x01bc }
                                int r9 = r9 * r10
                                int r9 = r9 * r7
                                int r9 = r9 / r5
                                java.nio.ByteBuffer r10 = java.nio.ByteBuffer.allocateDirect(r9)     // Catch:{ all -> 0x01bc }
                                android.graphics.Rect r11 = new android.graphics.Rect     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r5 = r5.mWidth     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r9 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r9 = r9.mHeight     // Catch:{ all -> 0x01bc }
                                r11.<init>(r3, r3, r5, r9)     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r12 = r5.mWidth     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r13 = r5.mHeight     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r14 = r5.mStride     // Catch:{ all -> 0x01bc }
                                r9 = r6
                                r9.<init>(r10, r11, r12, r13, r14)     // Catch:{ all -> 0x01bc }
                                java.nio.ByteBuffer r5 = r6.outBuffer     // Catch:{ all -> 0x01bc }
                                byte[] r5 = r5.array()     // Catch:{ all -> 0x01bc }
                                int r9 = r2.remaining()     // Catch:{ all -> 0x01bc }
                                r2.get(r5, r3, r9)     // Catch:{ all -> 0x01bc }
                                java.nio.ByteBuffer r3 = r6.outBuffer     // Catch:{ all -> 0x01bc }
                                byte[] r3 = r3.array()     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r5 = r5.mStride     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r9 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r9 = r9.mHeight     // Catch:{ all -> 0x01bc }
                                int r5 = r5 * r9
                                int r9 = r4.remaining()     // Catch:{ all -> 0x01bc }
                                r4.get(r3, r5, r9)     // Catch:{ all -> 0x01bc }
                                r2.rewind()     // Catch:{ all -> 0x01bc }
                                r4.rewind()     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r5 = r5.mOrientation     // Catch:{ all -> 0x01bc }
                                byte[] r10 = r3.nv21ToJpeg(r6, r5, r8)     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                com.android.camera.CameraActivity r3 = r3.mActivity     // Catch:{ all -> 0x01bc }
                                com.android.camera.MediaSaveService r9 = r3.getMediaSaveService()     // Catch:{ all -> 0x01bc }
                                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x01bc }
                                r3.<init>()     // Catch:{ all -> 0x01bc }
                                java.lang.String r5 = "Debug_beforeApplyingFilter"
                                r3.append(r5)     // Catch:{ all -> 0x01bc }
                                int r5 = r1     // Catch:{ all -> 0x01bc }
                                r3.append(r5)     // Catch:{ all -> 0x01bc }
                                java.lang.String r11 = r3.toString()     // Catch:{ all -> 0x01bc }
                                r12 = 0
                                r14 = 0
                                android.graphics.Rect r3 = r6.outRoi     // Catch:{ all -> 0x01bc }
                                int r15 = r3.width()     // Catch:{ all -> 0x01bc }
                                android.graphics.Rect r3 = r6.outRoi     // Catch:{ all -> 0x01bc }
                                int r16 = r3.height()     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r17 = r3.mOrientation     // Catch:{ all -> 0x01bc }
                                r18 = 0
                                com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                com.android.camera.CaptureModule r3 = r3.mController     // Catch:{ all -> 0x01bc }
                                com.android.camera.MediaSaveService$OnMediaSavedListener r19 = r3.getMediaSavedListener()     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                com.android.camera.CameraActivity r3 = r3.mActivity     // Catch:{ all -> 0x01bc }
                                android.content.ContentResolver r20 = r3.getContentResolver()     // Catch:{ all -> 0x01bc }
                                java.lang.String r21 = "jpeg"
                                r9.addImage(r10, r11, r12, r14, r15, r16, r17, r18, r19, r20, r21)     // Catch:{ all -> 0x01bc }
                            L_0x0182:
                                com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                int r3 = r3.mFilterIndex     // Catch:{ all -> 0x01bc }
                                if (r3 != r7) goto L_0x01a1
                                int r3 = r1     // Catch:{ all -> 0x01bc }
                                if (r3 <= 0) goto L_0x01a1
                                com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                com.android.camera.CaptureModule r3 = r3.mController     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                com.android.camera.CaptureModule r5 = r5.mController     // Catch:{ all -> 0x01bc }
                                int r5 = r5.getMainCameraId()     // Catch:{ all -> 0x01bc }
                                r3.checkAndPlayShutterSound(r5)     // Catch:{ all -> 0x01bc }
                            L_0x01a1:
                                com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.filter.ImageFilter r3 = r3.mFilter     // Catch:{ all -> 0x01bc }
                                int r5 = r1     // Catch:{ all -> 0x01bc }
                                r3.addImage(r2, r4, r5, r8)     // Catch:{ all -> 0x01bc }
                                com.android.camera.imageprocessor.PostProcessor r2 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x01bc }
                                android.media.Image[] r2 = r2.mImages     // Catch:{ all -> 0x01bc }
                                int r3 = r1     // Catch:{ all -> 0x01bc }
                                android.media.Image r0 = r5     // Catch:{ all -> 0x01bc }
                                r2[r3] = r0     // Catch:{ all -> 0x01bc }
                            L_0x01b8:
                                monitor-exit(r1)     // Catch:{ all -> 0x01bc }
                                return
                            L_0x01ba:
                                monitor-exit(r1)     // Catch:{ all -> 0x01bc }
                                return
                            L_0x01bc:
                                r0 = move-exception
                                monitor-exit(r1)     // Catch:{ all -> 0x01bc }
                                throw r0
                            */
                            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.imageprocessor.PostProcessor.C08217.run():void");
                        }
                    });
                }
            }
        }
    }

    public static byte[] addExifTags(byte[] bArr, int i, TotalCaptureResult totalCaptureResult) {
        ExifInterface exifInterface = new ExifInterface();
        exifInterface.addMakeAndModelTag();
        exifInterface.addOrientationTag(i);
        exifInterface.addDateTimeStampTag(ExifInterface.TAG_DATE_TIME, System.currentTimeMillis(), TimeZone.getDefault());
        if (totalCaptureResult != null) {
            if (totalCaptureResult.get(CaptureResult.FLASH_MODE) != null) {
                exifInterface.addFlashTag(((Integer) totalCaptureResult.get(CaptureResult.FLASH_MODE)).intValue() != 0);
            }
            if (totalCaptureResult.get(CaptureResult.LENS_FOCAL_LENGTH) != null) {
                exifInterface.addFocalLength(new Rational((long) ((int) (((Float) totalCaptureResult.get(CaptureResult.LENS_FOCAL_LENGTH)).floatValue() * 100.0f)), 100));
            }
            if (totalCaptureResult.get(CaptureResult.CONTROL_AWB_MODE) != null) {
                exifInterface.addWhiteBalanceMode(((Integer) totalCaptureResult.get(CaptureResult.CONTROL_AWB_MODE)).intValue());
            }
            if (totalCaptureResult.get(CaptureResult.LENS_APERTURE) != null) {
                exifInterface.addAperture(new Rational((long) ((int) (((Float) totalCaptureResult.get(CaptureResult.LENS_APERTURE)).floatValue() * 100.0f)), 100));
            }
            if (totalCaptureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME) != null) {
                exifInterface.addExposureTime(new Rational(((Long) totalCaptureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME)).longValue() / 1000000, 1000));
            }
            if (totalCaptureResult.get(CaptureResult.SENSOR_SENSITIVITY) != null) {
                exifInterface.addISO(((Integer) totalCaptureResult.get(CaptureResult.SENSOR_SENSITIVITY)).intValue());
            }
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            exifInterface.writeExif(bArr, (OutputStream) byteArrayOutputStream);
        } catch (IOException e) {
            Log.e(TAG, "Could not write EXIF", e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /* access modifiers changed from: private */
    public void clear() {
        this.mCurrentNumImage = 0;
    }

    private void processImage(String str, long j, OnMediaSavedListener onMediaSavedListener, ContentResolver contentResolver) {
        ProcessorHandler processorHandler = this.mHandler;
        if (processorHandler != null && processorHandler.isRunning && this.mStatus == STATUS.BUSY) {
            final ProcessorHandler processorHandler2 = processorHandler;
            final String str2 = str;
            final long j2 = j;
            final OnMediaSavedListener onMediaSavedListener2 = onMediaSavedListener;
            final ContentResolver contentResolver2 = contentResolver;
            C08228 r0 = new Runnable() {
                /* JADX WARNING: Code restructure failed: missing block: B:53:0x0196, code lost:
                    return;
                 */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    /*
                        r20 = this;
                        r0 = r20
                        com.android.camera.imageprocessor.PostProcessor r1 = com.android.camera.imageprocessor.PostProcessor.this
                        java.lang.Object r1 = r1.lock
                        monitor-enter(r1)
                        com.android.camera.imageprocessor.PostProcessor$ProcessorHandler r2 = r2     // Catch:{ all -> 0x0197 }
                        boolean r2 = r2.isRunning     // Catch:{ all -> 0x0197 }
                        if (r2 != 0) goto L_0x0011
                        monitor-exit(r1)     // Catch:{ all -> 0x0197 }
                        return
                    L_0x0011:
                        com.android.camera.imageprocessor.PostProcessor r2 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.filter.ImageFilter r2 = r2.mFilter     // Catch:{ all -> 0x0197 }
                        r3 = 0
                        r4 = 0
                        if (r2 != 0) goto L_0x0022
                        com.android.camera.imageprocessor.PostProcessor r2 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.filter.ImageFilter$ResultImage r2 = r2.mDefaultResultImage     // Catch:{ all -> 0x0197 }
                        goto L_0x0056
                    L_0x0022:
                        com.android.camera.imageprocessor.PostProcessor r2 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.filter.ImageFilter r2 = r2.mFilter     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.filter.ImageFilter$ResultImage r2 = r2.processImage()     // Catch:{ all -> 0x0197 }
                        r5 = r4
                    L_0x002d:
                        com.android.camera.imageprocessor.PostProcessor r6 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        android.media.Image[] r6 = r6.mImages     // Catch:{ all -> 0x0197 }
                        int r6 = r6.length     // Catch:{ all -> 0x0197 }
                        if (r5 >= r6) goto L_0x0056
                        com.android.camera.imageprocessor.PostProcessor r6 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        android.media.Image[] r6 = r6.mImages     // Catch:{ all -> 0x0197 }
                        r6 = r6[r5]     // Catch:{ all -> 0x0197 }
                        if (r6 == 0) goto L_0x0053
                        com.android.camera.imageprocessor.PostProcessor r6 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        android.media.Image[] r6 = r6.mImages     // Catch:{ all -> 0x0197 }
                        r6 = r6[r5]     // Catch:{ all -> 0x0197 }
                        r6.close()     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.PostProcessor r6 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        android.media.Image[] r6 = r6.mImages     // Catch:{ all -> 0x0197 }
                        r6[r5] = r3     // Catch:{ all -> 0x0197 }
                    L_0x0053:
                        int r5 = r5 + 1
                        goto L_0x002d
                    L_0x0056:
                        r5 = 1
                        if (r2 == 0) goto L_0x00c8
                        com.android.camera.imageprocessor.PostProcessor r6 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.CaptureModule r6 = r6.mController     // Catch:{ all -> 0x0197 }
                        java.util.ArrayList r6 = r6.getFrameFilters()     // Catch:{ all -> 0x0197 }
                        java.util.Iterator r6 = r6.iterator()     // Catch:{ all -> 0x0197 }
                    L_0x0067:
                        boolean r7 = r6.hasNext()     // Catch:{ all -> 0x0197 }
                        if (r7 == 0) goto L_0x0089
                        java.lang.Object r7 = r6.next()     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.filter.ImageFilter r7 = (com.android.camera.imageprocessor.filter.ImageFilter) r7     // Catch:{ all -> 0x0197 }
                        int r8 = r2.width     // Catch:{ all -> 0x0197 }
                        int r9 = r2.height     // Catch:{ all -> 0x0197 }
                        int r10 = r2.stride     // Catch:{ all -> 0x0197 }
                        int r11 = r2.stride     // Catch:{ all -> 0x0197 }
                        r7.init(r8, r9, r10, r11)     // Catch:{ all -> 0x0197 }
                        java.nio.ByteBuffer r8 = r2.outBuffer     // Catch:{ all -> 0x0197 }
                        java.lang.Boolean r9 = new java.lang.Boolean     // Catch:{ all -> 0x0197 }
                        r9.<init>(r4)     // Catch:{ all -> 0x0197 }
                        r7.addImage(r8, r3, r4, r9)     // Catch:{ all -> 0x0197 }
                        goto L_0x0067
                    L_0x0089:
                        com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        boolean r3 = r3.isSelfieMirrorOn()     // Catch:{ all -> 0x0197 }
                        if (r3 == 0) goto L_0x00c8
                        com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.CaptureModule r3 = r3.mController     // Catch:{ all -> 0x0197 }
                        boolean r3 = r3.isBackCamera()     // Catch:{ all -> 0x0197 }
                        if (r3 != 0) goto L_0x00c8
                        com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        int r3 = r3.mOrientation     // Catch:{ all -> 0x0197 }
                        if (r3 == 0) goto L_0x00b2
                        com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        int r3 = r3.mOrientation     // Catch:{ all -> 0x0197 }
                        r6 = 180(0xb4, float:2.52E-43)
                        if (r3 != r6) goto L_0x00b0
                        goto L_0x00b2
                    L_0x00b0:
                        r12 = r5
                        goto L_0x00b3
                    L_0x00b2:
                        r12 = r4
                    L_0x00b3:
                        com.android.camera.imageprocessor.PostProcessor r7 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        java.nio.ByteBuffer r3 = r2.outBuffer     // Catch:{ all -> 0x0197 }
                        byte[] r8 = r3.array()     // Catch:{ all -> 0x0197 }
                        int r9 = r2.stride     // Catch:{ all -> 0x0197 }
                        int r10 = r2.height     // Catch:{ all -> 0x0197 }
                        int r3 = r2.stride     // Catch:{ all -> 0x0197 }
                        int r6 = r2.width     // Catch:{ all -> 0x0197 }
                        int r11 = r3 - r6
                        r7.nativeFlipNV21(r8, r9, r10, r11, r12)     // Catch:{ all -> 0x0197 }
                    L_0x00c8:
                        com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        r3.clear()     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.PostProcessor$STATUS r6 = com.android.camera.imageprocessor.PostProcessor.STATUS.INIT     // Catch:{ all -> 0x0197 }
                        r3.mStatus = r6     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.PostProcessor$WatchdogThread r3 = r3.mWatchdog     // Catch:{ all -> 0x0197 }
                        if (r3 == 0) goto L_0x00e5
                        com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.PostProcessor$WatchdogThread r3 = r3.mWatchdog     // Catch:{ all -> 0x0197 }
                        r3.stopMonitor()     // Catch:{ all -> 0x0197 }
                    L_0x00e5:
                        if (r2 == 0) goto L_0x018e
                        android.graphics.Rect r3 = r2.outRoi     // Catch:{ all -> 0x0197 }
                        int r3 = r3.left     // Catch:{ all -> 0x0197 }
                        android.graphics.Rect r6 = r2.outRoi     // Catch:{ all -> 0x0197 }
                        int r6 = r6.width()     // Catch:{ all -> 0x0197 }
                        int r3 = r3 + r6
                        int r6 = r2.width     // Catch:{ all -> 0x0197 }
                        if (r3 > r6) goto L_0x018e
                        android.graphics.Rect r3 = r2.outRoi     // Catch:{ all -> 0x0197 }
                        int r3 = r3.top     // Catch:{ all -> 0x0197 }
                        android.graphics.Rect r6 = r2.outRoi     // Catch:{ all -> 0x0197 }
                        int r6 = r6.height()     // Catch:{ all -> 0x0197 }
                        int r3 = r3 + r6
                        int r6 = r2.height     // Catch:{ all -> 0x0197 }
                        if (r3 <= r6) goto L_0x0107
                        goto L_0x018e
                    L_0x0107:
                        com.android.camera.imageprocessor.PostProcessor r3 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.PostProcessor r6 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        int r6 = r6.mOrientation     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.PostProcessor r7 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        android.hardware.camera2.TotalCaptureResult r4 = r7.waitForMetaData(r4)     // Catch:{ all -> 0x0197 }
                        byte[] r3 = r3.nv21ToJpeg(r2, r6, r4)     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.PostProcessor r4 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.CaptureModule r4 = r4.mController     // Catch:{ all -> 0x0197 }
                        int r4 = r4.getCurrentIntentMode()     // Catch:{ all -> 0x0197 }
                        if (r4 != r5) goto L_0x0153
                        com.android.camera.imageprocessor.PostProcessor r4 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.CaptureModule r4 = r4.mController     // Catch:{ all -> 0x0197 }
                        r4.setJpegImageData(r3)     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.PostProcessor r4 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.CaptureModule r4 = r4.mController     // Catch:{ all -> 0x0197 }
                        boolean r4 = r4.isQuickCapture()     // Catch:{ all -> 0x0197 }
                        if (r4 == 0) goto L_0x0144
                        com.android.camera.imageprocessor.PostProcessor r4 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.CaptureModule r4 = r4.mController     // Catch:{ all -> 0x0197 }
                        r4.onCaptureDone()     // Catch:{ all -> 0x0197 }
                        goto L_0x0153
                    L_0x0144:
                        com.android.camera.imageprocessor.PostProcessor r4 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.CaptureModule r4 = r4.mController     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.PostProcessor r5 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        int r5 = r5.mOrientation     // Catch:{ all -> 0x0197 }
                        r4.showCapturedReview(r3, r5)     // Catch:{ all -> 0x0197 }
                    L_0x0153:
                        com.android.camera.imageprocessor.PostProcessor r4 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.CameraActivity r4 = r4.mActivity     // Catch:{ all -> 0x0197 }
                        com.android.camera.MediaSaveService r7 = r4.getMediaSaveService()     // Catch:{ all -> 0x0197 }
                        java.lang.String r9 = r3     // Catch:{ all -> 0x0197 }
                        long r10 = r4     // Catch:{ all -> 0x0197 }
                        r12 = 0
                        android.graphics.Rect r4 = r2.outRoi     // Catch:{ all -> 0x0197 }
                        int r13 = r4.width()     // Catch:{ all -> 0x0197 }
                        android.graphics.Rect r2 = r2.outRoi     // Catch:{ all -> 0x0197 }
                        int r14 = r2.height()     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.PostProcessor r2 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        int r15 = r2.mOrientation     // Catch:{ all -> 0x0197 }
                        r16 = 0
                        com.android.camera.MediaSaveService$OnMediaSavedListener r2 = r6     // Catch:{ all -> 0x0197 }
                        android.content.ContentResolver r4 = r7     // Catch:{ all -> 0x0197 }
                        java.lang.String r19 = "jpeg"
                        r8 = r3
                        r17 = r2
                        r18 = r4
                        r7.addImage(r8, r9, r10, r12, r13, r14, r15, r16, r17, r18, r19)     // Catch:{ all -> 0x0197 }
                        com.android.camera.imageprocessor.PostProcessor r0 = com.android.camera.imageprocessor.PostProcessor.this     // Catch:{ all -> 0x0197 }
                        com.android.camera.CaptureModule r0 = r0.mController     // Catch:{ all -> 0x0197 }
                        r0.updateThumbnailJpegData(r3)     // Catch:{ all -> 0x0197 }
                        goto L_0x0195
                    L_0x018e:
                        java.lang.String r0 = "PostProcessor"
                        java.lang.String r2 = "Result image is not valid."
                        android.util.Log.d(r0, r2)     // Catch:{ all -> 0x0197 }
                    L_0x0195:
                        monitor-exit(r1)     // Catch:{ all -> 0x0197 }
                        return
                    L_0x0197:
                        r0 = move-exception
                        monitor-exit(r1)     // Catch:{ all -> 0x0197 }
                        throw r0
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.camera.imageprocessor.PostProcessor.C08228.run():void");
                }
            };
            processorHandler.post(r0);
        }
    }

    public TotalCaptureResult waitForMetaData(int i) {
        for (int i2 = 10; i2 > 0; i2--) {
            if (this.mTotalCaptureResultList.size() > i) {
                return (TotalCaptureResult) this.mTotalCaptureResultList.get(i);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException unused) {
            }
        }
        if (this.mTotalCaptureResultList.size() == 0) {
            return null;
        }
        return (TotalCaptureResult) this.mTotalCaptureResultList.get(0);
    }

    private ResultImage resizeImage(ResultImage resultImage, Size size) {
        ResultImage resultImage2 = resultImage;
        ResultImage resultImage3 = new ResultImage(ByteBuffer.allocateDirect(((size.getWidth() * size.getHeight()) * 3) / 2), new Rect(0, 0, size.getWidth(), size.getHeight()), size.getWidth(), size.getHeight(), size.getWidth());
        int nativeResizeImage = nativeResizeImage(resultImage2.outBuffer.array(), resultImage3.outBuffer.array(), resultImage2.width, resultImage2.height, resultImage2.stride, size.getWidth(), size.getHeight());
        Rect rect = resultImage2.outRoi;
        resultImage3.outRoi = new Rect(rect.left / nativeResizeImage, rect.top / nativeResizeImage, rect.right / nativeResizeImage, rect.bottom / nativeResizeImage);
        if (resultImage3.width < resultImage3.outRoi.width()) {
            resultImage3.outRoi.right = resultImage3.width;
        }
        if (resultImage3.height < resultImage3.outRoi.height()) {
            resultImage3.outRoi.bottom = resultImage3.height;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Image is resized by SW with the ratio: ");
        sb.append(nativeResizeImage);
        sb.append(" oldRoi: ");
        sb.append(resultImage2.outRoi.toString());
        Log.d(TAG, sb.toString());
        return resultImage3;
    }

    /* access modifiers changed from: private */
    public byte[] nv21ToJpeg(ResultImage resultImage, int i, TotalCaptureResult totalCaptureResult) {
        YuvImage yuvImage;
        ResultImage resultImage2 = resultImage;
        BitmapOutputStream bitmapOutputStream = new BitmapOutputStream(1024);
        ByteBuffer byteBuffer = resultImage2.outBuffer;
        if (byteBuffer == null) {
            byte[] bArr = resultImage2.outBufferArray;
            int i2 = resultImage2.width;
            int i3 = resultImage2.height;
            int i4 = resultImage2.stride;
            yuvImage = new YuvImage(bArr, 17, i2, i3, new int[]{i4, i4});
        } else {
            byte[] array = byteBuffer.array();
            int i5 = resultImage2.width;
            int i6 = resultImage2.height;
            int i7 = resultImage2.stride;
            YuvImage yuvImage2 = new YuvImage(array, 17, i5, i6, new int[]{i7, i7});
            yuvImage = yuvImage2;
        }
        if (isSelfieMirrorOn() && !this.mController.isBackCamera()) {
            int i8 = resultImage2.height;
            Rect rect = resultImage2.outRoi;
            int height = i8 - (rect.top + rect.height());
            Rect rect2 = resultImage2.outRoi;
            resultImage2.outRoi = new Rect(rect2.left, height, rect2.right, rect2.height() + height);
        }
        yuvImage.compressToJpeg(resultImage2.outRoi, getJpegQualityValue(), bitmapOutputStream);
        return addExifTags(bitmapOutputStream.getArray(), i, totalCaptureResult);
    }

    public int getJpegQualityValue() {
        if (SettingsManager.getInstance() != null) {
            String str = "pref_camera_jpegquality_key";
            if (SettingsManager.getInstance().getValue(str) != null) {
                CaptureModule.getQualityNumber(SettingsManager.getInstance().getValue(str));
            }
        }
        return 55;
    }
}
