package org.codeaurora.snapcam.filter;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Base64;
import android.util.Log;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import java.io.ByteArrayOutputStream;

public class GDepth {
    public static final String FORMAT_8_BIT = "8-bit";
    public static final String FORMAT_RANGE_INVERSE = "RangeInverse";
    public static final String FORMAT_RANGLE_LINEAR = "RangeLinear";
    private static final String MIME = "image/jpeg";
    public static final String NAMESPACE_URL = "http://ns.google.com/photos/1.0/depthmap/";
    public static final String PREFIX = "GDepth";
    public static final String PROPERTY_DATA = "Data";
    public static final String PROPERTY_FAR = "Far";
    public static final String PROPERTY_FORMAT = "Format";
    public static final String PROPERTY_MIME = "Mime";
    public static final String PROPERTY_NEAR = "Near";
    public static final String PROPERTY_ROI_HEIGHT = "RoiHeight";
    public static final String PROPERTY_ROI_WIDTH = "RoiWidth";
    public static final String PROPERTY_ROI_X = "RoiX";
    public static final String PROPERTY_ROI_Y = "RoiY";
    private static final String TAG = "Flow_GDepth";
    private String mData;
    private DepthMap mDepthMap;
    private final String mFormat = FORMAT_8_BIT;
    private byte[] mGdepthJpeg;
    private int[] mMap;
    private Rect mRoi;

    public static class DepthMap {
        public byte[] buffer;
        public int height;
        public Rect roi;
        public int width;

        public DepthMap(int i, int i2) {
            this.width = i;
            this.height = i2;
        }
    }

    public int getFar() {
        return 255;
    }

    public String getFormat() {
        return FORMAT_8_BIT;
    }

    public String getMime() {
        return "image/jpeg";
    }

    public int getNear() {
        return 0;
    }

    static {
        try {
            XMPMetaFactory.getSchemaRegistry().registerNamespace(NAMESPACE_URL, PREFIX);
        } catch (XMPException e) {
            e.printStackTrace();
        }
    }

    private GDepth(DepthMap depthMap) {
        this.mDepthMap = depthMap;
        this.mRoi = depthMap.roi;
        if (depthMap != null) {
            byte[] bArr = depthMap.buffer;
            if (bArr != null) {
                this.mMap = new int[bArr.length];
                for (int i = 0; i < this.mMap.length; i++) {
                    byte b = depthMap.buffer[i] & 255;
                    this.mMap[i] = Color.rgb(b, b, b);
                }
            }
        }
    }

    private GDepth(byte[] bArr) {
        this.mGdepthJpeg = bArr;
    }

    public String getData() {
        return this.mData;
    }

    public Rect getRoi() {
        return this.mRoi;
    }

    public void setRoi(Rect rect) {
        this.mRoi = rect;
    }

    public static GDepth createGDepth(DepthMap depthMap) {
        GDepth gDepth = new GDepth(depthMap);
        if (gDepth.encoding()) {
            return gDepth;
        }
        return null;
    }

    public static GDepth createGDepth(byte[] bArr) {
        GDepth gDepth = new GDepth(bArr);
        if (gDepth.encodeDepthmapJpeg()) {
            return gDepth;
        }
        return null;
    }

    private boolean encoding() {
        String str = TAG;
        Log.d(str, "encoding");
        int[] iArr = this.mMap;
        DepthMap depthMap = this.mDepthMap;
        Bitmap createBitmap = Bitmap.createBitmap(iArr, depthMap.width, depthMap.height, Config.ARGB_8888);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        createBitmap.compress(CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        this.mGdepthJpeg = byteArray;
        if (byteArray != null) {
            this.mData = serializeAsBase64Str(byteArray);
            return true;
        }
        Log.e(str, "compressToJPEG failure");
        return false;
    }

    private boolean encodeDepthmapJpeg() {
        String str = TAG;
        Log.d(str, "encodeDepthmapJpeg");
        byte[] bArr = this.mGdepthJpeg;
        if (bArr != null) {
            this.mData = serializeAsBase64Str(bArr);
            return true;
        }
        Log.e(str, "compressToJPEG failure");
        return false;
    }

    public Bitmap getGdepthBitmap() {
        int[] iArr = this.mMap;
        DepthMap depthMap = this.mDepthMap;
        return Bitmap.createBitmap(iArr, depthMap.width, depthMap.height, Config.ARGB_8888);
    }

    public Bitmap getBitGdepthBitmap() {
        int[] iArr = new int[this.mMap.length];
        for (int i = 0; i < iArr.length; i++) {
            iArr[i] = (this.mMap[i] & 255) << 24;
        }
        DepthMap depthMap = this.mDepthMap;
        return Bitmap.createBitmap(iArr, depthMap.width, depthMap.height, Config.ALPHA_8);
    }

    private byte[] compressToJPEG(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        sb.append("compressToJPEG byte[].size=");
        sb.append(bArr.length);
        String sb2 = sb.toString();
        String str = TAG;
        Log.d(str, sb2);
        Bitmap decodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
        if (decodeByteArray == null) {
            Log.d(str, " buffer can't be decoded ");
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        decodeByteArray.compress(CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] getDepthJpeg() {
        return this.mGdepthJpeg;
    }

    private String serializeAsBase64Str(byte[] bArr) {
        Log.d(TAG, "serializeAsBase64Str");
        return Base64.encodeToString(bArr, 0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x005e A[SYNTHETIC, Splitter:B:17:0x005e] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x006d A[SYNTHETIC, Splitter:B:22:0x006d] */
    /* JADX WARNING: Removed duplicated region for block: B:28:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveAsFile(java.lang.String r5, java.lang.String r6) {
        /*
            r4 = this;
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r0 = "saveAsFile sdcard/DDM/Flow_GDepth"
            r4.append(r0)
            r4.append(r6)
            java.lang.String r0 = ".log"
            r4.append(r0)
            java.lang.String r4 = r4.toString()
            java.lang.String r1 = "Flow_GDepth"
            android.util.Log.d(r1, r4)
            java.io.File r4 = new java.io.File
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "sdcard/DDM/Flow_GDepth"
            r2.append(r3)
            r2.append(r6)
            r2.append(r0)
            java.lang.String r6 = r2.toString()
            r4.<init>(r6)
            byte[] r5 = r5.getBytes()
            r6 = 0
            java.io.BufferedOutputStream r0 = new java.io.BufferedOutputStream     // Catch:{ Exception -> 0x0054 }
            java.io.FileOutputStream r2 = new java.io.FileOutputStream     // Catch:{ Exception -> 0x0054 }
            r2.<init>(r4)     // Catch:{ Exception -> 0x0054 }
            r0.<init>(r2)     // Catch:{ Exception -> 0x0054 }
            r4 = 0
            int r6 = r5.length     // Catch:{ Exception -> 0x004f, all -> 0x004c }
            r0.write(r5, r4, r6)     // Catch:{ Exception -> 0x004f, all -> 0x004c }
            r0.close()     // Catch:{ Exception -> 0x0062 }
            goto L_0x006a
        L_0x004c:
            r4 = move-exception
            r6 = r0
            goto L_0x006b
        L_0x004f:
            r4 = move-exception
            r6 = r0
            goto L_0x0055
        L_0x0052:
            r4 = move-exception
            goto L_0x006b
        L_0x0054:
            r4 = move-exception
        L_0x0055:
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0052 }
            android.util.Log.d(r1, r4)     // Catch:{ all -> 0x0052 }
            if (r6 == 0) goto L_0x006a
            r6.close()     // Catch:{ Exception -> 0x0062 }
            goto L_0x006a
        L_0x0062:
            r4 = move-exception
            java.lang.String r4 = r4.toString()
            android.util.Log.d(r1, r4)
        L_0x006a:
            return
        L_0x006b:
            if (r6 == 0) goto L_0x0079
            r6.close()     // Catch:{ Exception -> 0x0071 }
            goto L_0x0079
        L_0x0071:
            r5 = move-exception
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r1, r5)
        L_0x0079:
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: org.codeaurora.snapcam.filter.GDepth.saveAsFile(java.lang.String, java.lang.String):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x004c A[SYNTHETIC, Splitter:B:17:0x004c] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x005b A[SYNTHETIC, Splitter:B:22:0x005b] */
    /* JADX WARNING: Removed duplicated region for block: B:28:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveAsJPEG(byte[] r5) {
        /*
            r4 = this;
            java.lang.String r4 = "Flow_GDepth"
            java.lang.String r0 = "saveAsJPEG"
            android.util.Log.d(r4, r0)
            java.io.File r0 = new java.io.File
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "sdcard/"
            r1.append(r2)
            long r2 = java.lang.System.currentTimeMillis()
            r1.append(r2)
            java.lang.String r2 = "_depth.JPEG"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.<init>(r1)
            r1 = 0
            java.io.BufferedOutputStream r2 = new java.io.BufferedOutputStream     // Catch:{ Exception -> 0x0042 }
            java.io.FileOutputStream r3 = new java.io.FileOutputStream     // Catch:{ Exception -> 0x0042 }
            r3.<init>(r0)     // Catch:{ Exception -> 0x0042 }
            r2.<init>(r3)     // Catch:{ Exception -> 0x0042 }
            r0 = 0
            int r1 = r5.length     // Catch:{ Exception -> 0x003d, all -> 0x003a }
            r2.write(r5, r0, r1)     // Catch:{ Exception -> 0x003d, all -> 0x003a }
            r2.close()     // Catch:{ Exception -> 0x0050 }
            goto L_0x0058
        L_0x003a:
            r5 = move-exception
            r1 = r2
            goto L_0x0059
        L_0x003d:
            r5 = move-exception
            r1 = r2
            goto L_0x0043
        L_0x0040:
            r5 = move-exception
            goto L_0x0059
        L_0x0042:
            r5 = move-exception
        L_0x0043:
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0040 }
            android.util.Log.d(r4, r5)     // Catch:{ all -> 0x0040 }
            if (r1 == 0) goto L_0x0058
            r1.close()     // Catch:{ Exception -> 0x0050 }
            goto L_0x0058
        L_0x0050:
            r5 = move-exception
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r4, r5)
        L_0x0058:
            return
        L_0x0059:
            if (r1 == 0) goto L_0x0067
            r1.close()     // Catch:{ Exception -> 0x005f }
            goto L_0x0067
        L_0x005f:
            r0 = move-exception
            java.lang.String r0 = r0.toString()
            android.util.Log.d(r4, r0)
        L_0x0067:
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: org.codeaurora.snapcam.filter.GDepth.saveAsJPEG(byte[]):void");
    }

    private GDepth(int i, int i2, String str) {
        this.mData = str;
    }

    public static GDepth createGDepth(XMPMeta xMPMeta) {
        String str = TAG;
        String str2 = NAMESPACE_URL;
        try {
            int parseInt = Integer.parseInt((String) xMPMeta.getProperty(str2, PROPERTY_NEAR).getValue());
            int parseInt2 = Integer.parseInt((String) xMPMeta.getProperty(str2, PROPERTY_FAR).getValue());
            String str3 = (String) xMPMeta.getProperty(str2, "Data").getValue();
            String str4 = (String) xMPMeta.getProperty(str2, PROPERTY_FORMAT).getValue();
            StringBuilder sb = new StringBuilder();
            sb.append("new GDepth: nerar=");
            sb.append(parseInt);
            sb.append(" far=");
            sb.append(parseInt2);
            sb.append("format=");
            sb.append(str4);
            sb.append(" data=");
            sb.append(str3);
            Log.d(str, sb.toString());
            int parseInt3 = Integer.parseInt((String) xMPMeta.getProperty(str2, PROPERTY_ROI_X).getValue());
            int parseInt4 = Integer.parseInt((String) xMPMeta.getProperty(str2, PROPERTY_ROI_Y).getValue());
            int parseInt5 = Integer.parseInt((String) xMPMeta.getProperty(str2, PROPERTY_ROI_WIDTH).getValue());
            int parseInt6 = Integer.parseInt((String) xMPMeta.getProperty(str2, PROPERTY_ROI_HEIGHT).getValue());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("x=");
            sb2.append(parseInt3);
            sb2.append(" y=");
            sb2.append(parseInt4);
            sb2.append(" width=");
            sb2.append(parseInt5);
            sb2.append(" height=");
            sb2.append(parseInt6);
            Log.d(str, sb2.toString());
            return new GDepth(parseInt, parseInt2, str3);
        } catch (XMPException e) {
            Log.e(str, e.toString());
            return null;
        } catch (Exception e2) {
            Log.e(str, e2.toString());
            return null;
        }
    }

    public boolean decode() {
        Log.d(TAG, "decode");
        byte[] decode = Base64.decode(this.mData, 0);
        saveAsJPEG(decode);
        int[] iArr = new int[decode.length];
        int[] iArr2 = new int[decode.length];
        for (int i = 0; i < iArr.length; i++) {
            iArr[i] = (decode[i] + 256) % 256;
        }
        return false;
    }
}
