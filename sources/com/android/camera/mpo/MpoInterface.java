package com.android.camera.mpo;

import android.util.Log;
import com.android.camera.exif.ExifInterface;
import com.android.camera.util.CameraUtil;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MpoInterface {
    private static final String NULL_ARGUMENT_STRING = "Argument is null";
    private static final String TAG = "MpoInterface";
    public static final int TAG_AXIS_DISTANCE_X = ExifInterface.defineTag(2, -19960);
    public static final int TAG_AXIS_DISTANCE_Y = ExifInterface.defineTag(2, -19959);
    public static final int TAG_AXIS_DISTANCE_Z = ExifInterface.defineTag(2, -19958);
    public static final int TAG_BASELINE_LEN = ExifInterface.defineTag(2, -19962);
    public static final int TAG_BASE_VIEWPOINT_NUM = ExifInterface.defineTag(2, -19964);
    public static final int TAG_CONVERGE_ANGLE = ExifInterface.defineTag(2, -19963);
    public static final int TAG_DIVERGE_ANGLE = ExifInterface.defineTag(2, -19961);
    public static final int TAG_IMAGE_NUMBER = ExifInterface.defineTag(2, -20223);
    public static final int TAG_IMAGE_UNIQUE_ID_LIST = ExifInterface.defineTag(1, -20477);
    public static final int TAG_MP_ENTRY = ExifInterface.defineTag(1, -20478);
    public static final int TAG_MP_FORMAT_VERSION = ExifInterface.defineTag(3, -20480);
    public static final int TAG_NUM_CAPTURED_FRAMES = ExifInterface.defineTag(1, -20476);
    public static final int TAG_NUM_IMAGES = ExifInterface.defineTag(1, -20479);
    public static final int TAG_PAN_ORIENTATION = ExifInterface.defineTag(2, -19967);
    public static final int TAG_PAN_OVERLAP_H = ExifInterface.defineTag(2, -19966);
    public static final int TAG_PAN_OVERLAP_V = ExifInterface.defineTag(2, -19965);
    public static final int TAG_PITCH_ANGLE = ExifInterface.defineTag(2, -19956);
    public static final int TAG_ROLL_ANGLE = ExifInterface.defineTag(2, -19955);
    public static final int TAG_YAW_ANGLE = ExifInterface.defineTag(2, -19957);

    public static int writeMpo(MpoData mpoData, OutputStream outputStream) {
        if (mpoData == null || outputStream == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
        }
        MpoOutputStream mpoWriterStream = getMpoWriterStream(outputStream);
        mpoWriterStream.setMpoData(mpoData);
        try {
            mpoWriterStream.writeMpoFile();
            CameraUtil.closeSilently(mpoWriterStream);
            return mpoWriterStream.size();
        } catch (IOException unused) {
            CameraUtil.closeSilently(mpoWriterStream);
            Log.w(TAG, "IO Exception when writing mpo image");
            return -1;
        }
    }

    public static int writeMpo(MpoData mpoData, String str) {
        if (mpoData != null && str != null) {
            return writeMpo(mpoData, getFileWriterStream(str));
        }
        throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
    }

    private static MpoOutputStream getMpoWriterStream(OutputStream outputStream) {
        if (outputStream != null) {
            return new MpoOutputStream(outputStream);
        }
        throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
    }

    private static OutputStream getFileWriterStream(String str) {
        if (str != null) {
            try {
                return new FileOutputStream(str);
            } catch (FileNotFoundException unused) {
                CameraUtil.closeSilently(null);
                Log.w(TAG, "File not found");
                return null;
            }
        } else {
            throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
        }
    }

    private static short getShort(byte[] bArr, int i) {
        return (short) ((bArr[i + 1] & 255) | (bArr[i] << 8));
    }

    private static byte[] openNewStream(ByteArrayOutputStream byteArrayOutputStream) {
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.reset();
        return byteArray;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0049 A[Catch:{ IOException -> 0x007c }] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x006b A[Catch:{ IOException -> 0x007c }] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x006d A[Catch:{ IOException -> 0x007c }] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0070 A[Catch:{ IOException -> 0x007c }] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x001b A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0067 A[EDGE_INSN: B:42:0x0067->B:27:0x0067 ?: BREAK  
    EDGE_INSN: B:42:0x0067->B:27:0x0067 ?: BREAK  , SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.util.ArrayList<byte[]> generateXmpFromMpo(java.lang.String r13) {
        /*
            java.io.File r0 = new java.io.File
            r0.<init>(r13)
            java.util.ArrayList r13 = new java.util.ArrayList
            r13.<init>()
            r1 = 1024(0x400, float:1.435E-42)
            byte[] r1 = new byte[r1]
            java.io.ByteArrayOutputStream r2 = new java.io.ByteArrayOutputStream
            r2.<init>()
            java.io.FileInputStream r3 = new java.io.FileInputStream     // Catch:{ IOException -> 0x007c }
            r3.<init>(r0)     // Catch:{ IOException -> 0x007c }
            r0 = 0
            r4 = r0
            r5 = r4
        L_0x001b:
            int r6 = r3.read(r1)     // Catch:{ IOException -> 0x007c }
            r7 = -1
            if (r6 == r7) goto L_0x0075
            r8 = 2
            r9 = -39
            r10 = 1
            if (r4 == 0) goto L_0x0040
            byte r4 = r1[r0]     // Catch:{ IOException -> 0x007c }
            if (r4 != r9) goto L_0x0040
            int r4 = r5 + 1
            if (r4 != r8) goto L_0x0041
            r2.write(r9)     // Catch:{ IOException -> 0x007c }
            if (r6 != r10) goto L_0x0036
            goto L_0x0075
        L_0x0036:
            byte[] r4 = openNewStream(r2)     // Catch:{ IOException -> 0x007c }
            r13.add(r4)     // Catch:{ IOException -> 0x007c }
            r4 = r0
            r5 = r10
            goto L_0x0042
        L_0x0040:
            r4 = r5
        L_0x0041:
            r5 = r0
        L_0x0042:
            r11 = r5
            r5 = r4
            r4 = r0
        L_0x0045:
            int r12 = r6 + -1
            if (r4 >= r12) goto L_0x0067
            short r12 = getShort(r1, r4)     // Catch:{ IOException -> 0x007c }
            if (r12 == r9) goto L_0x0050
            goto L_0x0064
        L_0x0050:
            int r5 = r5 + 1
            if (r5 != r8) goto L_0x0064
            int r5 = r4 + 2
            int r12 = r5 - r11
            r2.write(r1, r11, r12)     // Catch:{ IOException -> 0x007c }
            byte[] r11 = openNewStream(r2)     // Catch:{ IOException -> 0x007c }
            r13.add(r11)     // Catch:{ IOException -> 0x007c }
            r11 = r5
            r5 = r0
        L_0x0064:
            int r4 = r4 + 1
            goto L_0x0045
        L_0x0067:
            byte r4 = r1[r12]     // Catch:{ IOException -> 0x007c }
            if (r4 != r7) goto L_0x006d
            r4 = r10
            goto L_0x006e
        L_0x006d:
            r4 = r0
        L_0x006e:
            if (r11 >= r6) goto L_0x001b
            int r6 = r6 - r11
            r2.write(r1, r11, r6)     // Catch:{ IOException -> 0x007c }
            goto L_0x001b
        L_0x0075:
            r3.close()     // Catch:{ IOException -> 0x007c }
            r2.close()     // Catch:{ IOException -> 0x007c }
            goto L_0x0080
        L_0x007c:
            r0 = move-exception
            r0.printStackTrace()
        L_0x0080:
            return r13
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.mpo.MpoInterface.generateXmpFromMpo(java.lang.String):java.util.ArrayList");
    }

    public static ArrayList<byte[]> generateXmpFromMpo(byte[] bArr) {
        ArrayList<byte[]> arrayList = new ArrayList<>();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        int i2 = 0;
        while (i < bArr.length - 1) {
            try {
                if (getShort(bArr, i) == -39) {
                    if (i >= bArr.length - 3 || getShort(bArr, i + 2) == -40) {
                        int i3 = i + 2;
                        byteArrayOutputStream.write(bArr, i2, i3 - i2);
                        arrayList.add(openNewStream(byteArrayOutputStream));
                        i2 = i3;
                    }
                }
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        byteArrayOutputStream.close();
        return arrayList;
    }
}
