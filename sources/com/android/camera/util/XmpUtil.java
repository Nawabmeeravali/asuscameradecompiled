package com.android.camera.util;

import android.util.Log;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.options.SerializeOptions;
import com.android.camera.Storage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class XmpUtil {
    private static final String EXTENDED_XMP_HEADER_SIGNATURE = "http://ns.adobe.com/xmp/extension/\u0000";
    private static final int EXTEND_XMP_HEADER_SIZE = 75;
    private static final String GOOGLE_PANO_NAMESPACE = "http://ns.google.com/photos/1.0/panorama/";
    private static final int MAX_EXTENDED_XMP_BUFFER_SIZE = 65000;
    private static final int MAX_XMP_BUFFER_SIZE = 65502;
    private static final int M_APP1 = 225;
    private static final int M_SOI = 216;
    private static final int M_SOS = 218;
    private static final String NOTE_PREFIX = "xmpNote";
    private static final String PANO_PREFIX = "GPano";
    private static final String TAG = "XmpUtil";
    private static final String XMP_HEADER = "http://ns.adobe.com/xap/1.0/\u0000";
    private static final int XMP_HEADER_SIZE = 29;
    private static final String XMP_NOTE_NAMESPACE = "http://ns.adobe.com/xmp/note/";

    private static class Section {
        public byte[] data;
        public int length;
        public int marker;

        private Section() {
        }
    }

    static {
        try {
            XMPMetaFactory.getSchemaRegistry().registerNamespace("http://ns.google.com/photos/1.0/panorama/", PANO_PREFIX);
            XMPMetaFactory.getSchemaRegistry().registerNamespace("http://ns.adobe.com/xmp/note/", NOTE_PREFIX);
        } catch (XMPException e) {
            e.printStackTrace();
        }
    }

    public static XMPMeta extractXMPMeta(String str) {
        boolean endsWith = str.toLowerCase().endsWith(Storage.JPEG_POSTFIX);
        String str2 = TAG;
        if (endsWith || str.toLowerCase().endsWith(".jpeg")) {
            try {
                return extractXMPMeta((InputStream) new FileInputStream(str));
            } catch (FileNotFoundException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Could not read file: ");
                sb.append(str);
                Log.e(str2, sb.toString(), e);
                return null;
            }
        } else {
            Log.d(str2, "XMP parse: only jpeg file is supported");
            return null;
        }
    }

    public static XMPMeta extractXMPMeta(InputStream inputStream) {
        List<Section> parse = parse(inputStream, true);
        if (parse == null) {
            return null;
        }
        for (Section section : parse) {
            if (hasXMPHeader(section.data)) {
                byte[] bArr = new byte[(getXMPContentEnd(section.data) - 29)];
                System.arraycopy(section.data, 29, bArr, 0, bArr.length);
                try {
                    return XMPMetaFactory.parseFromBuffer(bArr);
                } catch (XMPException e) {
                    Log.d(TAG, "XMP parse error", e);
                }
            }
        }
        return null;
    }

    public static XMPMeta createXMPMeta() {
        return XMPMetaFactory.create();
    }

    public static XMPMeta extractOrCreateXMPMeta(String str) {
        XMPMeta extractXMPMeta = extractXMPMeta(str);
        return extractXMPMeta == null ? createXMPMeta() : extractXMPMeta;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x005e A[SYNTHETIC, Splitter:B:29:0x005e] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0064 A[SYNTHETIC, Splitter:B:34:0x0064] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean writeXMPMeta(java.lang.String r5, com.adobe.xmp.XMPMeta r6) {
        /*
            java.lang.String r0 = r5.toLowerCase()
            java.lang.String r1 = ".jpg"
            boolean r0 = r0.endsWith(r1)
            java.lang.String r1 = "XmpUtil"
            r2 = 0
            if (r0 != 0) goto L_0x0021
            java.lang.String r0 = r5.toLowerCase()
            java.lang.String r3 = ".jpeg"
            boolean r0 = r0.endsWith(r3)
            if (r0 != 0) goto L_0x0021
            java.lang.String r5 = "XMP parse: only jpeg file is supported"
            android.util.Log.d(r1, r5)
            return r2
        L_0x0021:
            java.io.FileInputStream r0 = new java.io.FileInputStream     // Catch:{ FileNotFoundException -> 0x0068 }
            r0.<init>(r5)     // Catch:{ FileNotFoundException -> 0x0068 }
            java.util.List r0 = parse(r0, r2)     // Catch:{ FileNotFoundException -> 0x0068 }
            java.util.List r6 = insertXMPSection(r0, r6)     // Catch:{ FileNotFoundException -> 0x0068 }
            if (r6 != 0) goto L_0x0031
            return r2
        L_0x0031:
            r0 = 0
            java.io.FileOutputStream r3 = new java.io.FileOutputStream     // Catch:{ IOException -> 0x0047 }
            r3.<init>(r5)     // Catch:{ IOException -> 0x0047 }
            writeJpegFile(r3, r6)     // Catch:{ IOException -> 0x0042, all -> 0x003f }
            r3.close()     // Catch:{ IOException -> 0x003d }
        L_0x003d:
            r5 = 1
            return r5
        L_0x003f:
            r5 = move-exception
            r0 = r3
            goto L_0x0062
        L_0x0042:
            r6 = move-exception
            r0 = r3
            goto L_0x0048
        L_0x0045:
            r5 = move-exception
            goto L_0x0062
        L_0x0047:
            r6 = move-exception
        L_0x0048:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0045 }
            r3.<init>()     // Catch:{ all -> 0x0045 }
            java.lang.String r4 = "Write file failed:"
            r3.append(r4)     // Catch:{ all -> 0x0045 }
            r3.append(r5)     // Catch:{ all -> 0x0045 }
            java.lang.String r5 = r3.toString()     // Catch:{ all -> 0x0045 }
            android.util.Log.d(r1, r5, r6)     // Catch:{ all -> 0x0045 }
            if (r0 == 0) goto L_0x0061
            r0.close()     // Catch:{ IOException -> 0x0061 }
        L_0x0061:
            return r2
        L_0x0062:
            if (r0 == 0) goto L_0x0067
            r0.close()     // Catch:{ IOException -> 0x0067 }
        L_0x0067:
            throw r5
        L_0x0068:
            r6 = move-exception
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r3 = "Could not read file: "
            r0.append(r3)
            r0.append(r5)
            java.lang.String r5 = r0.toString()
            android.util.Log.e(r1, r5, r6)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.util.XmpUtil.writeXMPMeta(java.lang.String, com.adobe.xmp.XMPMeta):boolean");
    }

    public static boolean writeXMPMeta(InputStream inputStream, OutputStream outputStream, XMPMeta xMPMeta) {
        List insertXMPSection = insertXMPSection(parse(inputStream, false), xMPMeta);
        if (insertXMPSection == null) {
            return false;
        }
        try {
            writeJpegFile(outputStream, insertXMPSection);
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException unused) {
                }
            }
            return true;
        } catch (IOException e) {
            Log.d(TAG, "Write to stream failed", e);
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException unused2) {
                }
            }
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException unused3) {
                }
            }
        }
    }

    private static void writeJpegFile(OutputStream outputStream, List<Section> list) throws IOException {
        outputStream.write(255);
        outputStream.write(M_SOI);
        for (Section section : list) {
            outputStream.write(255);
            outputStream.write(section.marker);
            int i = section.length;
            if (i > 0) {
                int i2 = i >> 8;
                int i3 = i & 255;
                outputStream.write(i2);
                outputStream.write(i3);
            }
            outputStream.write(section.data);
        }
    }

    private static List<Section> insertXMPSection(List<Section> list, XMPMeta xMPMeta) {
        if (list != null) {
            int i = 1;
            if (list.size() > 1) {
                try {
                    SerializeOptions serializeOptions = new SerializeOptions();
                    serializeOptions.setUseCompactFormat(true);
                    serializeOptions.setOmitPacketWrapper(true);
                    byte[] serializeToBuffer = XMPMetaFactory.serializeToBuffer(xMPMeta, serializeOptions);
                    if (serializeToBuffer.length > MAX_XMP_BUFFER_SIZE) {
                        return null;
                    }
                    byte[] bArr = new byte[(serializeToBuffer.length + 29)];
                    System.arraycopy(XMP_HEADER.getBytes(), 0, bArr, 0, 29);
                    System.arraycopy(serializeToBuffer, 0, bArr, 29, serializeToBuffer.length);
                    Section section = new Section();
                    section.marker = M_APP1;
                    section.length = bArr.length + 2;
                    section.data = bArr;
                    int i2 = 0;
                    while (i2 < list.size()) {
                        if (((Section) list.get(i2)).marker != M_APP1 || !hasXMPHeader(((Section) list.get(i2)).data)) {
                            i2++;
                        } else {
                            list.set(i2, section);
                            return list;
                        }
                    }
                    ArrayList arrayList = new ArrayList();
                    if (((Section) list.get(0)).marker != M_APP1) {
                        i = 0;
                    }
                    arrayList.addAll(list.subList(0, i));
                    arrayList.add(section);
                    arrayList.addAll(list.subList(i, list.size()));
                    return arrayList;
                } catch (XMPException e) {
                    Log.d(TAG, "Serialize xmp failed", e);
                }
            }
        }
        return null;
    }

    private static boolean hasXMPHeader(byte[] bArr) {
        if (bArr.length < 29) {
            return false;
        }
        try {
            byte[] bArr2 = new byte[29];
            System.arraycopy(bArr, 0, bArr2, 0, 29);
            if (new String(bArr2, "UTF-8").equals(XMP_HEADER)) {
                return true;
            }
        } catch (UnsupportedEncodingException unused) {
        }
        return false;
    }

    private static int getXMPContentEnd(byte[] bArr) {
        for (int length = bArr.length - 1; length >= 1; length--) {
            if (bArr[length] == 62 && bArr[length - 1] != 63) {
                return length + 1;
            }
        }
        return bArr.length;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x003b, code lost:
        if (r9 != false) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r9 = new com.android.camera.util.XmpUtil.Section(null);
        r9.marker = r3;
        r9.length = -1;
        r9.data = new byte[r8.available()];
        r8.read(r9.data, 0, r9.data.length);
        r1.add(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0059, code lost:
        if (r8 == null) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r8.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.util.List<com.android.camera.util.XmpUtil.Section> parse(java.io.InputStream r8, boolean r9) {
        /*
            r0 = 0
            int r1 = r8.read()     // Catch:{ IOException -> 0x00a9 }
            r2 = 255(0xff, float:3.57E-43)
            if (r1 != r2) goto L_0x00a1
            int r1 = r8.read()     // Catch:{ IOException -> 0x00a9 }
            r3 = 216(0xd8, float:3.03E-43)
            if (r1 == r3) goto L_0x0013
            goto L_0x00a1
        L_0x0013:
            java.util.ArrayList r1 = new java.util.ArrayList     // Catch:{ IOException -> 0x00a9 }
            r1.<init>()     // Catch:{ IOException -> 0x00a9 }
        L_0x0018:
            int r3 = r8.read()     // Catch:{ IOException -> 0x00a9 }
            r4 = -1
            if (r3 == r4) goto L_0x009b
            if (r3 == r2) goto L_0x0027
            if (r8 == 0) goto L_0x0026
            r8.close()     // Catch:{ IOException -> 0x0026 }
        L_0x0026:
            return r0
        L_0x0027:
            int r3 = r8.read()     // Catch:{ IOException -> 0x00a9 }
            if (r3 != r2) goto L_0x002e
            goto L_0x0027
        L_0x002e:
            if (r3 != r4) goto L_0x0036
            if (r8 == 0) goto L_0x0035
            r8.close()     // Catch:{ IOException -> 0x0035 }
        L_0x0035:
            return r0
        L_0x0036:
            r5 = 218(0xda, float:3.05E-43)
            r6 = 0
            if (r3 != r5) goto L_0x005f
            if (r9 != 0) goto L_0x0059
            com.android.camera.util.XmpUtil$Section r9 = new com.android.camera.util.XmpUtil$Section     // Catch:{ IOException -> 0x00a9 }
            r9.<init>()     // Catch:{ IOException -> 0x00a9 }
            r9.marker = r3     // Catch:{ IOException -> 0x00a9 }
            r9.length = r4     // Catch:{ IOException -> 0x00a9 }
            int r2 = r8.available()     // Catch:{ IOException -> 0x00a9 }
            byte[] r2 = new byte[r2]     // Catch:{ IOException -> 0x00a9 }
            r9.data = r2     // Catch:{ IOException -> 0x00a9 }
            byte[] r2 = r9.data     // Catch:{ IOException -> 0x00a9 }
            byte[] r3 = r9.data     // Catch:{ IOException -> 0x00a9 }
            int r3 = r3.length     // Catch:{ IOException -> 0x00a9 }
            r8.read(r2, r6, r3)     // Catch:{ IOException -> 0x00a9 }
            r1.add(r9)     // Catch:{ IOException -> 0x00a9 }
        L_0x0059:
            if (r8 == 0) goto L_0x005e
            r8.close()     // Catch:{ IOException -> 0x005e }
        L_0x005e:
            return r1
        L_0x005f:
            int r5 = r8.read()     // Catch:{ IOException -> 0x00a9 }
            int r7 = r8.read()     // Catch:{ IOException -> 0x00a9 }
            if (r5 == r4) goto L_0x0095
            if (r7 != r4) goto L_0x006c
            goto L_0x0095
        L_0x006c:
            int r4 = r5 << 8
            r4 = r4 | r7
            if (r9 == 0) goto L_0x007d
            r5 = 225(0xe1, float:3.15E-43)
            if (r3 != r5) goto L_0x0076
            goto L_0x007d
        L_0x0076:
            int r4 = r4 + -2
            long r3 = (long) r4     // Catch:{ IOException -> 0x00a9 }
            r8.skip(r3)     // Catch:{ IOException -> 0x00a9 }
            goto L_0x0018
        L_0x007d:
            com.android.camera.util.XmpUtil$Section r5 = new com.android.camera.util.XmpUtil$Section     // Catch:{ IOException -> 0x00a9 }
            r5.<init>()     // Catch:{ IOException -> 0x00a9 }
            r5.marker = r3     // Catch:{ IOException -> 0x00a9 }
            r5.length = r4     // Catch:{ IOException -> 0x00a9 }
            int r4 = r4 + -2
            byte[] r3 = new byte[r4]     // Catch:{ IOException -> 0x00a9 }
            r5.data = r3     // Catch:{ IOException -> 0x00a9 }
            byte[] r3 = r5.data     // Catch:{ IOException -> 0x00a9 }
            r8.read(r3, r6, r4)     // Catch:{ IOException -> 0x00a9 }
            r1.add(r5)     // Catch:{ IOException -> 0x00a9 }
            goto L_0x0018
        L_0x0095:
            if (r8 == 0) goto L_0x009a
            r8.close()     // Catch:{ IOException -> 0x009a }
        L_0x009a:
            return r0
        L_0x009b:
            if (r8 == 0) goto L_0x00a0
            r8.close()     // Catch:{ IOException -> 0x00a0 }
        L_0x00a0:
            return r1
        L_0x00a1:
            if (r8 == 0) goto L_0x00a6
            r8.close()     // Catch:{ IOException -> 0x00a6 }
        L_0x00a6:
            return r0
        L_0x00a7:
            r9 = move-exception
            goto L_0x00b7
        L_0x00a9:
            r9 = move-exception
            java.lang.String r1 = "XmpUtil"
            java.lang.String r2 = "Could not parse file."
            android.util.Log.d(r1, r2, r9)     // Catch:{ all -> 0x00a7 }
            if (r8 == 0) goto L_0x00b6
            r8.close()     // Catch:{ IOException -> 0x00b6 }
        L_0x00b6:
            return r0
        L_0x00b7:
            if (r8 == 0) goto L_0x00bc
            r8.close()     // Catch:{ IOException -> 0x00bc }
        L_0x00bc:
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.util.XmpUtil.parse(java.io.InputStream, boolean):java.util.List");
    }

    private static Section createStandardXMPSection(XMPMeta xMPMeta) {
        String str = TAG;
        try {
            SerializeOptions serializeOptions = new SerializeOptions();
            serializeOptions.setUseCompactFormat(true);
            serializeOptions.setOmitPacketWrapper(true);
            byte[] serializeToBuffer = XMPMetaFactory.serializeToBuffer(xMPMeta, serializeOptions);
            if (serializeToBuffer.length > MAX_XMP_BUFFER_SIZE) {
                Log.e(str, "exceed max size");
                return null;
            }
            byte[] bArr = new byte[(serializeToBuffer.length + 29)];
            System.arraycopy(XMP_HEADER.getBytes(), 0, bArr, 0, 29);
            System.arraycopy(serializeToBuffer, 0, bArr, 29, serializeToBuffer.length);
            Section section = new Section();
            section.marker = M_APP1;
            section.length = bArr.length + 2;
            section.data = bArr;
            return section;
        } catch (XMPException e) {
            Log.d(str, "Serialize xmp failed", e);
            return null;
        }
    }

    private static Section createSection(byte[] bArr, byte[] bArr2) {
        int length = bArr.length;
        String str = TAG;
        if (length > MAX_EXTENDED_XMP_BUFFER_SIZE) {
            Log.e(str, "createSection fail exceed max size");
            return null;
        }
        byte[] bArr3 = new byte[(bArr.length + EXTEND_XMP_HEADER_SIZE)];
        System.arraycopy(bArr2, 0, bArr3, 0, bArr2.length);
        System.arraycopy(bArr, 0, bArr3, bArr2.length, bArr.length);
        Section section = new Section();
        section.marker = M_APP1;
        section.length = bArr3.length + 2;
        section.data = bArr3;
        ByteBuffer wrap = ByteBuffer.wrap(bArr3);
        StringBuilder sb = new StringBuilder();
        sb.append("fullLength=");
        sb.append(wrap.getInt(67));
        sb.append(" offset=");
        sb.append(wrap.getInt(71));
        Log.d(str, sb.toString());
        return section;
    }

    private static List<Section> splitExtendXMPMeta(byte[] bArr, String str) {
        ArrayList arrayList = new ArrayList();
        int length = bArr.length / MAX_EXTENDED_XMP_BUFFER_SIZE;
        byte[] bArr2 = new byte[MAX_EXTENDED_XMP_BUFFER_SIZE];
        ByteBuffer wrap = ByteBuffer.wrap(bArr);
        byte[] bArr3 = new byte[EXTEND_XMP_HEADER_SIZE];
        System.arraycopy(EXTENDED_XMP_HEADER_SIGNATURE.getBytes(), 0, bArr3, 0, 35);
        System.arraycopy(str.getBytes(), 0, bArr3, 35, str.length());
        int length2 = 35 + str.length();
        StringBuilder sb = new StringBuilder();
        sb.append("buffer.length=");
        sb.append(bArr.length);
        Log.d(TAG, sb.toString());
        byte[] bArr4 = new byte[4];
        ByteBuffer.wrap(bArr4).putInt(0, bArr.length);
        System.arraycopy(bArr4, 0, bArr3, length2, 4);
        int i = length2 + 4;
        byte[] bArr5 = new byte[4];
        ByteBuffer wrap2 = ByteBuffer.wrap(bArr5);
        for (int i2 = 0; i2 < length; i2++) {
            wrap2.putInt(0, i2 * MAX_EXTENDED_XMP_BUFFER_SIZE);
            System.arraycopy(bArr5, 0, bArr3, i, 4);
            wrap.get(bArr2);
            arrayList.add(createSection(bArr2, bArr3));
        }
        int i3 = length * MAX_EXTENDED_XMP_BUFFER_SIZE;
        int length3 = bArr.length - i3;
        if (length3 > 0) {
            wrap2.putInt(0, i3);
            System.arraycopy(bArr5, 0, bArr3, i, 4);
            byte[] bArr6 = new byte[length3];
            wrap.get(bArr6);
            arrayList.add(createSection(bArr6, bArr3));
        }
        return arrayList;
    }

    public static boolean writeXMPMeta(InputStream inputStream, OutputStream outputStream, XMPMeta xMPMeta, XMPMeta xMPMeta2) {
        String str = TAG;
        try {
            SerializeOptions serializeOptions = new SerializeOptions();
            serializeOptions.setUseCompactFormat(true);
            serializeOptions.setOmitPacketWrapper(true);
            byte[] serializeToBuffer = XMPMetaFactory.serializeToBuffer(xMPMeta2, serializeOptions);
            String guid = getGUID(serializeToBuffer);
            try {
                xMPMeta.setProperty("http://ns.adobe.com/xmp/note/", "HasExtendedXMP", guid);
                List parse = parse(inputStream, false);
                ArrayList arrayList = new ArrayList();
                Section createStandardXMPSection = createStandardXMPSection(xMPMeta);
                if (createStandardXMPSection == null) {
                    Log.e(str, "create standard meta section error");
                    return false;
                }
                arrayList.add(createStandardXMPSection);
                arrayList.addAll(splitExtendXMPMeta(serializeToBuffer, guid));
                List insertXMPSection = insertXMPSection(parse, (List<Section>) arrayList);
                if (insertXMPSection == null) {
                    Log.d(str, "Insert XMP fialed");
                    return false;
                }
                try {
                    writeJpegFile(outputStream, insertXMPSection);
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException unused) {
                        }
                    }
                    return true;
                } catch (IOException e) {
                    Log.d(str, "Write to stream failed", e);
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException unused2) {
                        }
                    }
                    return false;
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException unused3) {
                        }
                    }
                }
            } catch (XMPException e2) {
                Log.d(str, "set XMPMeta Property", e2);
                return false;
            }
        } catch (XMPException e3) {
            Log.d(str, "Serialize extended xmp failed", e3);
            return false;
        }
    }

    private static List<Section> insertXMPSection(List<Section> list, List<Section> list2) {
        if (list != null) {
            int i = 1;
            if (list.size() > 1) {
                ArrayList arrayList = new ArrayList();
                if (((Section) list.get(0)).marker != M_APP1) {
                    i = 0;
                }
                arrayList.addAll(list.subList(0, i));
                arrayList.addAll(list2);
                arrayList.addAll(list.subList(i, list.size()));
                return arrayList;
            }
        }
        return null;
    }

    private static String getGUID(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(bArr);
            byte[] digest = instance.digest();
            Formatter formatter = new Formatter(sb);
            for (byte b : digest) {
                formatter.format("%02x", new Object[]{Integer.valueOf((b + 256) % 256)});
            }
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("get md5 instance failure");
            sb2.append(e);
            Log.d(TAG, sb2.toString());
            return null;
        }
    }

    private XmpUtil() {
    }
}
