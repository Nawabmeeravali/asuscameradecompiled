package com.android.camera.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import org.codeaurora.snapcam.C0905R;

public class RotationTask extends AsyncTask<LocalData, Void, LocalData> {
    private static final String TAG = "CAM_RotationTask";
    private final LocalDataAdapter mAdapter;
    private final boolean mClockwise;
    private final Context mContext;
    private final int mCurrentDataId;
    private ProgressDialog mProgress;

    public RotationTask(Context context, LocalDataAdapter localDataAdapter, int i, boolean z) {
        this.mContext = context;
        this.mAdapter = localDataAdapter;
        this.mCurrentDataId = i;
        this.mClockwise = z;
    }

    /* access modifiers changed from: protected */
    public void onPreExecute() {
        this.mProgress = new ProgressDialog(this.mContext);
        this.mProgress.setTitle(this.mContext.getString(this.mClockwise ? C0905R.string.rotate_right : C0905R.string.rotate_left));
        this.mProgress.setMessage(this.mContext.getString(C0905R.string.please_wait));
        this.mProgress.setCancelable(false);
        this.mProgress.show();
    }

    /* access modifiers changed from: protected */
    public LocalData doInBackground(LocalData... localDataArr) {
        return rotateInJpegExif(localDataArr[0]);
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x00b2  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0108  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.android.camera.data.LocalData rotateInJpegExif(com.android.camera.data.LocalData r31) {
        /*
            r30 = this;
            r0 = r30
            r1 = r31
            boolean r2 = r1 instanceof com.android.camera.data.LocalMediaData.PhotoData
            r3 = 0
            java.lang.String r4 = "CAM_RotationTask"
            if (r2 != 0) goto L_0x0011
            java.lang.String r0 = "Rotation can only happen on PhotoData."
            android.util.Log.w(r4, r0)
            return r3
        L_0x0011:
            r2 = r1
            com.android.camera.data.LocalMediaData$PhotoData r2 = (com.android.camera.data.LocalMediaData.PhotoData) r2
            int r5 = r2.getOrientation()
            boolean r6 = r0.mClockwise
            if (r6 == 0) goto L_0x0021
            int r5 = r5 + 90
            int r5 = r5 % 360
            goto L_0x0025
        L_0x0021:
            int r5 = r5 + 270
            int r5 = r5 % 360
        L_0x0025:
            java.lang.String r6 = r2.getPath()
            android.content.ContentValues r7 = new android.content.ContentValues
            r7.<init>()
            java.lang.String r8 = r2.getMimeType()
            java.lang.String r9 = "image/jpeg"
            boolean r8 = r8.equalsIgnoreCase(r9)
            r9 = 1
            r10 = 0
            if (r8 == 0) goto L_0x00ad
            com.android.camera.exif.ExifInterface r8 = new com.android.camera.exif.ExifInterface
            r8.<init>()
            int r11 = com.android.camera.exif.ExifInterface.TAG_ORIENTATION
            short r12 = com.android.camera.exif.ExifInterface.getOrientationValueForRotation(r5)
            java.lang.Short r12 = java.lang.Short.valueOf(r12)
            com.android.camera.exif.ExifTag r11 = r8.buildTag(r11, r12)
            if (r11 == 0) goto L_0x0097
            r8.setTag(r11)
            r8.forceRewriteExif(r6)     // Catch:{ FileNotFoundException -> 0x0082, IOException -> 0x006d }
            java.io.File r8 = new java.io.File     // Catch:{ FileNotFoundException -> 0x0082, IOException -> 0x006d }
            r8.<init>(r6)     // Catch:{ FileNotFoundException -> 0x0082, IOException -> 0x006d }
            long r11 = r8.length()     // Catch:{ FileNotFoundException -> 0x0082, IOException -> 0x006d }
            java.lang.String r8 = "_size"
            java.lang.Long r11 = java.lang.Long.valueOf(r11)     // Catch:{ FileNotFoundException -> 0x0082, IOException -> 0x006d }
            r7.put(r8, r11)     // Catch:{ FileNotFoundException -> 0x0082, IOException -> 0x006d }
            r21 = r5
            r4 = r9
            goto L_0x00b0
        L_0x006d:
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r11 = "Cannot set exif data: "
            r8.append(r11)
            r8.append(r6)
            java.lang.String r6 = r8.toString()
            android.util.Log.w(r4, r6)
            goto L_0x00ad
        L_0x0082:
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r11 = "Cannot find file to set exif: "
            r8.append(r11)
            r8.append(r6)
            java.lang.String r6 = r8.toString()
            android.util.Log.w(r4, r6)
            goto L_0x00ad
        L_0x0097:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r8 = "Cannot build tag: "
            r6.append(r8)
            int r8 = com.android.camera.exif.ExifInterface.TAG_ORIENTATION
            r6.append(r8)
            java.lang.String r6 = r6.toString()
            android.util.Log.w(r4, r6)
        L_0x00ad:
            r4 = r10
            r21 = r4
        L_0x00b0:
            if (r4 == 0) goto L_0x0108
            java.lang.Integer r4 = java.lang.Integer.valueOf(r5)
            java.lang.String r5 = "orientation"
            r7.put(r5, r4)
            android.content.Context r0 = r0.mContext
            android.content.ContentResolver r0 = r0.getContentResolver()
            android.net.Uri r4 = r2.getContentUri()
            r0.update(r4, r7, r3, r3)
            double[] r0 = r31.getLatLong()
            r3 = 0
            if (r0 == 0) goto L_0x00d9
            r3 = r0[r10]
            r5 = r0[r9]
            r26 = r3
            r28 = r5
            goto L_0x00dd
        L_0x00d9:
            r26 = r3
            r28 = r26
        L_0x00dd:
            com.android.camera.data.LocalMediaData$PhotoData r0 = new com.android.camera.data.LocalMediaData$PhotoData
            r11 = r0
            long r12 = r31.getContentId()
            java.lang.String r14 = r31.getTitle()
            java.lang.String r15 = r31.getMimeType()
            long r16 = r31.getDateTaken()
            long r18 = r31.getDateModified()
            java.lang.String r20 = r31.getPath()
            int r22 = r2.getWidth()
            int r23 = r2.getHeight()
            long r24 = r31.getSizeInBytes()
            r11.<init>(r12, r14, r15, r16, r18, r20, r21, r22, r23, r24, r26, r28)
            goto L_0x0109
        L_0x0108:
            r0 = r3
        L_0x0109:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.data.RotationTask.rotateInJpegExif(com.android.camera.data.LocalData):com.android.camera.data.LocalData");
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(LocalData localData) {
        this.mProgress.dismiss();
        if (localData != null) {
            this.mAdapter.updateData(this.mCurrentDataId, localData);
        }
    }
}
