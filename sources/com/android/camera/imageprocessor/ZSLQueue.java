package com.android.camera.imageprocessor;

import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import com.android.camera.CaptureModule;
import com.android.camera.util.PersistUtil;

public class ZSLQueue {
    private static final boolean DEBUG_QUEUE = (PersistUtil.getCamera2Debug() == 2 || PersistUtil.getCamera2Debug() == 100);
    private static final String TAG = "ZSLQueue";
    private ImageItem[] mBuffer;
    private int mCircularBufferSize = PersistUtil.getCircularBufferSize();
    private int mImageHead;
    private Object mLock = new Object();
    private int mMetaHead;
    private CaptureModule mModule;

    static class ImageItem {
        private Image mImage = null;
        private TotalCaptureResult mMetadata = null;
        private Image mRawImage = null;

        ImageItem() {
        }

        public Image getImage() {
            return this.mImage;
        }

        public Image getRawImage() {
            return this.mRawImage;
        }

        public void setImage(Image image, Image image2) {
            Image image3 = this.mImage;
            if (image3 != null) {
                image3.close();
            }
            Image image4 = this.mRawImage;
            if (image4 != null) {
                image4.close();
            }
            this.mImage = image;
            this.mRawImage = image2;
        }

        public TotalCaptureResult getMetadata() {
            return this.mMetadata;
        }

        public void setMetadata(TotalCaptureResult totalCaptureResult) {
            this.mMetadata = totalCaptureResult;
        }

        public void closeImage() {
            Image image = this.mImage;
            if (image != null) {
                image.close();
            }
            Image image2 = this.mRawImage;
            if (image2 != null) {
                image2.close();
            }
            this.mImage = null;
        }

        public void closeMeta() {
            this.mMetadata = null;
        }

        public boolean isValid() {
            return (this.mImage == null || this.mMetadata == null) ? false : true;
        }
    }

    public ZSLQueue(CaptureModule captureModule) {
        synchronized (this.mLock) {
            this.mBuffer = new ImageItem[this.mCircularBufferSize];
            this.mImageHead = 0;
            this.mMetaHead = 0;
            this.mModule = captureModule;
        }
    }

    private int findMeta(long j, int i) {
        int i2 = i;
        do {
            ImageItem[] imageItemArr = this.mBuffer;
            if (imageItemArr[i2] != null && imageItemArr[i2].getMetadata() != null && ((Long) this.mBuffer[i2].getMetadata().get(CaptureResult.SENSOR_TIMESTAMP)).longValue() == j) {
                return i2;
            }
            i2 = (i2 + 1) % this.mBuffer.length;
        } while (i2 != i);
        return -1;
    }

    private int findImage(long j, int i) {
        int i2 = i;
        do {
            ImageItem[] imageItemArr = this.mBuffer;
            if (imageItemArr[i2] != null && imageItemArr[i2].getImage() != null && this.mBuffer[i2].getImage().getTimestamp() == j) {
                return i2;
            }
            i2 = (i2 + 1) % this.mBuffer.length;
        } while (i2 != i);
        return -1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00e7, code lost:
        if (DEBUG_QUEUE == false) goto L_0x0107;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00e9, code lost:
        r9 = new java.lang.StringBuilder();
        r9.append("imageIndex: ");
        r9.append(r4);
        r9.append(" ");
        r9.append(r0);
        android.util.Log.d(TAG, r9.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0107, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void add(android.media.Image r10, android.media.Image r11) {
        /*
            r9 = this;
            long r0 = r10.getTimestamp()
            java.lang.Object r2 = r9.mLock
            monitor-enter(r2)
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            if (r3 != 0) goto L_0x000d
            monitor-exit(r2)     // Catch:{ all -> 0x0108 }
            return
        L_0x000d:
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r4 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            r3 = r3[r4]     // Catch:{ all -> 0x0108 }
            if (r3 == 0) goto L_0x001f
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r4 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            r3 = r3[r4]     // Catch:{ all -> 0x0108 }
            r3.closeImage()     // Catch:{ all -> 0x0108 }
            goto L_0x002a
        L_0x001f:
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r4 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            com.android.camera.imageprocessor.ZSLQueue$ImageItem r5 = new com.android.camera.imageprocessor.ZSLQueue$ImageItem     // Catch:{ all -> 0x0108 }
            r5.<init>()     // Catch:{ all -> 0x0108 }
            r3[r4] = r5     // Catch:{ all -> 0x0108 }
        L_0x002a:
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r4 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            r3 = r3[r4]     // Catch:{ all -> 0x0108 }
            android.hardware.camera2.TotalCaptureResult r3 = r3.getMetadata()     // Catch:{ all -> 0x0108 }
            r4 = -1
            if (r3 == 0) goto L_0x00cf
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r5 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            r3 = r3[r5]     // Catch:{ all -> 0x0108 }
            android.hardware.camera2.TotalCaptureResult r3 = r3.getMetadata()     // Catch:{ all -> 0x0108 }
            android.hardware.camera2.CaptureResult$Key r5 = android.hardware.camera2.CaptureResult.SENSOR_TIMESTAMP     // Catch:{ all -> 0x0108 }
            java.lang.Object r3 = r3.get(r5)     // Catch:{ all -> 0x0108 }
            java.lang.Long r3 = (java.lang.Long) r3     // Catch:{ all -> 0x0108 }
            long r5 = r3.longValue()     // Catch:{ all -> 0x0108 }
            long r7 = r10.getTimestamp()     // Catch:{ all -> 0x0108 }
            int r3 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r3 != 0) goto L_0x006c
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r4 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            r3 = r3[r4]     // Catch:{ all -> 0x0108 }
            r3.setImage(r10, r11)     // Catch:{ all -> 0x0108 }
            int r4 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            int r10 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            int r10 = r10 + 1
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r11 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r11 = r11.length     // Catch:{ all -> 0x0108 }
            int r10 = r10 % r11
            r9.mImageHead = r10     // Catch:{ all -> 0x0108 }
            goto L_0x00e4
        L_0x006c:
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r5 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            r3 = r3[r5]     // Catch:{ all -> 0x0108 }
            android.hardware.camera2.TotalCaptureResult r3 = r3.getMetadata()     // Catch:{ all -> 0x0108 }
            android.hardware.camera2.CaptureResult$Key r5 = android.hardware.camera2.CaptureResult.SENSOR_TIMESTAMP     // Catch:{ all -> 0x0108 }
            java.lang.Object r3 = r3.get(r5)     // Catch:{ all -> 0x0108 }
            java.lang.Long r3 = (java.lang.Long) r3     // Catch:{ all -> 0x0108 }
            long r5 = r3.longValue()     // Catch:{ all -> 0x0108 }
            long r7 = r10.getTimestamp()     // Catch:{ all -> 0x0108 }
            int r3 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r3 <= 0) goto L_0x008e
            r10.close()     // Catch:{ all -> 0x0108 }
            goto L_0x00e4
        L_0x008e:
            long r5 = r10.getTimestamp()     // Catch:{ all -> 0x0108 }
            int r3 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            int r3 = r9.findMeta(r5, r3)     // Catch:{ all -> 0x0108 }
            if (r3 != r4) goto L_0x00b8
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r5 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            r3 = r3[r5]     // Catch:{ all -> 0x0108 }
            r3.setImage(r10, r11)     // Catch:{ all -> 0x0108 }
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r10 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r11 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            r10 = r10[r11]     // Catch:{ all -> 0x0108 }
            r11 = 0
            r10.setMetadata(r11)     // Catch:{ all -> 0x0108 }
            int r10 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            int r10 = r10 + 1
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r11 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r11 = r11.length     // Catch:{ all -> 0x0108 }
            int r10 = r10 % r11
            r9.mImageHead = r10     // Catch:{ all -> 0x0108 }
            goto L_0x00e4
        L_0x00b8:
            r9.mImageHead = r3     // Catch:{ all -> 0x0108 }
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r4 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r5 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            r4 = r4[r5]     // Catch:{ all -> 0x0108 }
            r4.setImage(r10, r11)     // Catch:{ all -> 0x0108 }
            int r10 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            int r10 = r10 + 1
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r11 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r11 = r11.length     // Catch:{ all -> 0x0108 }
            int r10 = r10 % r11
            r9.mImageHead = r10     // Catch:{ all -> 0x0108 }
            r4 = r3
            goto L_0x00e4
        L_0x00cf:
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r4 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            r3 = r3[r4]     // Catch:{ all -> 0x0108 }
            r3.setImage(r10, r11)     // Catch:{ all -> 0x0108 }
            int r4 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            int r10 = r9.mImageHead     // Catch:{ all -> 0x0108 }
            int r10 = r10 + 1
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r11 = r9.mBuffer     // Catch:{ all -> 0x0108 }
            int r11 = r11.length     // Catch:{ all -> 0x0108 }
            int r10 = r10 % r11
            r9.mImageHead = r10     // Catch:{ all -> 0x0108 }
        L_0x00e4:
            monitor-exit(r2)     // Catch:{ all -> 0x0108 }
            boolean r9 = DEBUG_QUEUE
            if (r9 == 0) goto L_0x0107
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "imageIndex: "
            r9.append(r10)
            r9.append(r4)
            java.lang.String r10 = " "
            r9.append(r10)
            r9.append(r0)
            java.lang.String r9 = r9.toString()
            java.lang.String r10 = "ZSLQueue"
            android.util.Log.d(r10, r9)
        L_0x0107:
            return
        L_0x0108:
            r9 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0108 }
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.imageprocessor.ZSLQueue.add(android.media.Image, android.media.Image):void");
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(3:36|37|38) */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00d7, code lost:
        if (DEBUG_QUEUE == false) goto L_0x00fd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00d9, code lost:
        r7 = new java.lang.StringBuilder();
        r7.append("Meta: ");
        r7.append(r4);
        r7.append(" ");
        r7.append(r8.get(android.hardware.camera2.CaptureResult.SENSOR_TIMESTAMP));
        android.util.Log.d(TAG, r7.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00fd, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ff, code lost:
        return;
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:36:0x00fe */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void add(android.hardware.camera2.TotalCaptureResult r8) {
        /*
            r7 = this;
            java.lang.Object r0 = r7.mLock
            monitor-enter(r0)
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r1 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            if (r1 != 0) goto L_0x0009
            monitor-exit(r0)     // Catch:{ all -> 0x0100 }
            return
        L_0x0009:
            android.hardware.camera2.CaptureResult$Key r1 = android.hardware.camera2.CaptureResult.SENSOR_TIMESTAMP     // Catch:{ IllegalStateException -> 0x00fe }
            java.lang.Object r1 = r8.get(r1)     // Catch:{ IllegalStateException -> 0x00fe }
            java.lang.Long r1 = (java.lang.Long) r1     // Catch:{ IllegalStateException -> 0x00fe }
            long r1 = r1.longValue()     // Catch:{ IllegalStateException -> 0x00fe }
            r3 = -1
            int r3 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r3 != 0) goto L_0x001d
            monitor-exit(r0)     // Catch:{ all -> 0x0100 }
            return
        L_0x001d:
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r4 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            r3 = r3[r4]     // Catch:{ all -> 0x0100 }
            if (r3 != 0) goto L_0x0031
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r4 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            com.android.camera.imageprocessor.ZSLQueue$ImageItem r5 = new com.android.camera.imageprocessor.ZSLQueue$ImageItem     // Catch:{ all -> 0x0100 }
            r5.<init>()     // Catch:{ all -> 0x0100 }
            r3[r4] = r5     // Catch:{ all -> 0x0100 }
            goto L_0x003a
        L_0x0031:
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r4 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            r3 = r3[r4]     // Catch:{ all -> 0x0100 }
            r3.closeMeta()     // Catch:{ all -> 0x0100 }
        L_0x003a:
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r4 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            r3 = r3[r4]     // Catch:{ all -> 0x0100 }
            android.media.Image r3 = r3.getImage()     // Catch:{ all -> 0x0100 }
            r4 = -1
            if (r3 == 0) goto L_0x00bf
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r5 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            r3 = r3[r5]     // Catch:{ all -> 0x0100 }
            android.media.Image r3 = r3.getImage()     // Catch:{ all -> 0x0100 }
            long r5 = r3.getTimestamp()     // Catch:{ all -> 0x0100 }
            int r3 = (r5 > r1 ? 1 : (r5 == r1 ? 0 : -1))
            if (r3 != 0) goto L_0x006f
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r1 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r2 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            r1 = r1[r2]     // Catch:{ all -> 0x0100 }
            r1.setMetadata(r8)     // Catch:{ all -> 0x0100 }
            int r4 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            int r1 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            int r1 = r1 + 1
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r2 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r2 = r2.length     // Catch:{ all -> 0x0100 }
            int r1 = r1 % r2
            r7.mMetaHead = r1     // Catch:{ all -> 0x0100 }
            goto L_0x00d4
        L_0x006f:
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r5 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            r3 = r3[r5]     // Catch:{ all -> 0x0100 }
            android.media.Image r3 = r3.getImage()     // Catch:{ all -> 0x0100 }
            long r5 = r3.getTimestamp()     // Catch:{ all -> 0x0100 }
            int r3 = (r5 > r1 ? 1 : (r5 == r1 ? 0 : -1))
            if (r3 <= 0) goto L_0x0082
            goto L_0x00d4
        L_0x0082:
            int r3 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            int r1 = r7.findImage(r1, r3)     // Catch:{ all -> 0x0100 }
            if (r1 != r4) goto L_0x00a8
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r1 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r2 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            r1 = r1[r2]     // Catch:{ all -> 0x0100 }
            r2 = 0
            r1.setImage(r2, r2)     // Catch:{ all -> 0x0100 }
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r1 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r2 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            r1 = r1[r2]     // Catch:{ all -> 0x0100 }
            r1.setMetadata(r8)     // Catch:{ all -> 0x0100 }
            int r1 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            int r1 = r1 + 1
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r2 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r2 = r2.length     // Catch:{ all -> 0x0100 }
            int r1 = r1 % r2
            r7.mMetaHead = r1     // Catch:{ all -> 0x0100 }
            goto L_0x00d4
        L_0x00a8:
            r7.mMetaHead = r1     // Catch:{ all -> 0x0100 }
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r2 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r3 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            r2 = r2[r3]     // Catch:{ all -> 0x0100 }
            r2.setMetadata(r8)     // Catch:{ all -> 0x0100 }
            int r2 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            int r2 = r2 + 1
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r3 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r3 = r3.length     // Catch:{ all -> 0x0100 }
            int r2 = r2 % r3
            r7.mMetaHead = r2     // Catch:{ all -> 0x0100 }
            r4 = r1
            goto L_0x00d4
        L_0x00bf:
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r1 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r2 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            r1 = r1[r2]     // Catch:{ all -> 0x0100 }
            r1.setMetadata(r8)     // Catch:{ all -> 0x0100 }
            int r4 = r7.mImageHead     // Catch:{ all -> 0x0100 }
            int r1 = r7.mMetaHead     // Catch:{ all -> 0x0100 }
            int r1 = r1 + 1
            com.android.camera.imageprocessor.ZSLQueue$ImageItem[] r2 = r7.mBuffer     // Catch:{ all -> 0x0100 }
            int r2 = r2.length     // Catch:{ all -> 0x0100 }
            int r1 = r1 % r2
            r7.mMetaHead = r1     // Catch:{ all -> 0x0100 }
        L_0x00d4:
            monitor-exit(r0)     // Catch:{ all -> 0x0100 }
            boolean r7 = DEBUG_QUEUE
            if (r7 == 0) goto L_0x00fd
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r0 = "Meta: "
            r7.append(r0)
            r7.append(r4)
            java.lang.String r0 = " "
            r7.append(r0)
            android.hardware.camera2.CaptureResult$Key r0 = android.hardware.camera2.CaptureResult.SENSOR_TIMESTAMP
            java.lang.Object r8 = r8.get(r0)
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            java.lang.String r8 = "ZSLQueue"
            android.util.Log.d(r8, r7)
        L_0x00fd:
            return
        L_0x00fe:
            monitor-exit(r0)     // Catch:{ all -> 0x0100 }
            return
        L_0x0100:
            r7 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0100 }
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.imageprocessor.ZSLQueue.add(android.hardware.camera2.TotalCaptureResult):void");
    }

    public ImageItem tryToGetMatchingItem() {
        synchronized (this.mLock) {
            int i = this.mImageHead;
            do {
                ImageItem imageItem = this.mBuffer[i];
                if (imageItem == null || !imageItem.isValid() || !checkImageRequirement(imageItem.getMetadata())) {
                    i--;
                    if (i < 0) {
                        i = this.mBuffer.length - 1;
                    }
                } else {
                    this.mBuffer[i] = null;
                    return imageItem;
                }
            } while (i != this.mImageHead);
            return null;
        }
    }

    public ImageItem tryToGetFallOffImage(TotalCaptureResult totalCaptureResult, double d) {
        synchronized (this.mLock) {
            int i = this.mImageHead;
            do {
                ImageItem imageItem = this.mBuffer[i];
                if (imageItem == null || !imageItem.isValid() || (totalCaptureResult.getFrameNumber() != imageItem.getMetadata().getFrameNumber() && d >= ((double) ((Long) imageItem.getMetadata().get(CaptureResult.SENSOR_TIMESTAMP)).longValue()))) {
                    i--;
                    if (i < 0) {
                        i = this.mBuffer.length - 1;
                    }
                } else {
                    this.mBuffer[i] = null;
                    return imageItem;
                }
            } while (i != this.mImageHead);
            return null;
        }
    }

    public void onClose() {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mBuffer.length; i++) {
                if (this.mBuffer[i] != null) {
                    this.mBuffer[i].closeImage();
                    this.mBuffer[i].closeMeta();
                    this.mBuffer[i] = null;
                }
            }
            this.mBuffer = null;
            this.mImageHead = 0;
            this.mMetaHead = 0;
        }
    }

    public void clear() {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mBuffer.length; i++) {
                if (this.mBuffer[i] != null) {
                    this.mBuffer[i].closeImage();
                    this.mBuffer[i].closeMeta();
                }
            }
            this.mImageHead = 0;
            this.mMetaHead = 0;
        }
    }

    private boolean checkImageRequirement(TotalCaptureResult totalCaptureResult) {
        if ((totalCaptureResult.get(CaptureResult.LENS_STATE) == null || ((Integer) totalCaptureResult.get(CaptureResult.LENS_STATE)).intValue() != 1) && ((totalCaptureResult.get(CaptureResult.CONTROL_AE_STATE) == null || (((Integer) totalCaptureResult.get(CaptureResult.CONTROL_AE_STATE)).intValue() != 1 && ((Integer) totalCaptureResult.get(CaptureResult.CONTROL_AE_STATE)).intValue() != 5)) && (totalCaptureResult.get(CaptureResult.CONTROL_AF_STATE) == null || (((Integer) totalCaptureResult.get(CaptureResult.CONTROL_AF_STATE)).intValue() != 3 && ((Integer) totalCaptureResult.get(CaptureResult.CONTROL_AF_STATE)).intValue() != 1)))) {
            return ((totalCaptureResult.get(CaptureResult.CONTROL_AE_STATE) == null || totalCaptureResult.get(CaptureResult.FLASH_MODE) == null || ((Integer) totalCaptureResult.get(CaptureResult.CONTROL_AE_STATE)).intValue() != 4 || ((Integer) totalCaptureResult.get(CaptureResult.FLASH_MODE)).intValue() == 0) && totalCaptureResult.get(CaptureResult.CONTROL_AWB_STATE) != null && ((Integer) totalCaptureResult.get(CaptureResult.CONTROL_AWB_STATE)).intValue() == 1) ? false : true;
        }
        return false;
    }
}
