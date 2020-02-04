package com.android.camera.exif;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

class ExifReader {
    private static final String TAG = "ExifReader";
    private final ExifInterface mInterface;

    ExifReader(ExifInterface exifInterface) {
        this.mInterface = exifInterface;
    }

    /* access modifiers changed from: protected */
    public ExifData read(InputStream inputStream) throws ExifInvalidFormatException, IOException {
        ExifParser parse = ExifParser.parse(inputStream, this.mInterface);
        ExifData exifData = new ExifData(parse.getByteOrder());
        for (int next = parse.next(); next != 5; next = parse.next()) {
            if (next == 0) {
                exifData.addIfdData(new IfdData(parse.getCurrentIfd()));
            } else if (next == 1) {
                ExifTag tag = parse.getTag();
                if (!tag.hasValue()) {
                    parse.registerForTagValue(tag);
                } else {
                    exifData.getIfdData(tag.getIfd()).setTag(tag);
                }
            } else if (next != 2) {
                String str = TAG;
                if (next == 3) {
                    byte[] bArr = new byte[parse.getCompressedImageSize()];
                    if (bArr.length == parse.read(bArr)) {
                        exifData.setCompressedThumbnail(bArr);
                    } else {
                        Log.w(str, "Failed to read the compressed thumbnail");
                    }
                } else if (next == 4) {
                    byte[] bArr2 = new byte[parse.getStripSize()];
                    if (bArr2.length == parse.read(bArr2)) {
                        exifData.setStripBytes(parse.getStripIndex(), bArr2);
                    } else {
                        Log.w(str, "Failed to read the strip bytes");
                    }
                }
            } else {
                ExifTag tag2 = parse.getTag();
                if (tag2.getDataType() == 7) {
                    parse.readFullTagValue(tag2);
                }
                exifData.getIfdData(tag2.getIfd()).setTag(tag2);
            }
        }
        return exifData;
    }
}
