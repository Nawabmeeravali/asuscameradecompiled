package com.android.camera.mpo;

import android.util.Log;
import com.android.camera.exif.ExifTag;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MpoTag extends ExifTag {
    private static final String TAG = "MpoTag";

    static class MpEntry {
        private short mDependantImage1;
        private short mDependantImage2;
        private int mImageAttrib;
        private int mImageOffset;
        private int mImageSize;

        public MpEntry() {
            this(0, 0, 0, 0, 0);
        }

        public MpEntry(int i, int i2, int i3) {
            this(i, i2, i3, 0, 0);
        }

        public MpEntry(int i, int i2, int i3, short s, short s2) {
            this.mImageAttrib = i;
            this.mImageSize = i2;
            this.mImageOffset = i3;
            this.mDependantImage1 = s;
            this.mDependantImage2 = s2;
        }

        public MpEntry(ByteBuffer byteBuffer) {
            this.mImageAttrib = byteBuffer.getInt();
            this.mImageSize = byteBuffer.getInt();
            this.mImageOffset = byteBuffer.getInt();
            this.mDependantImage1 = byteBuffer.getShort();
            this.mDependantImage2 = byteBuffer.getShort();
        }

        public int getImageAttrib() {
            return this.mImageAttrib;
        }

        public int getImageSize() {
            return this.mImageSize;
        }

        public int getImageOffset() {
            return this.mImageOffset;
        }

        public short getDependantImage1() {
            return this.mDependantImage1;
        }

        public short getDependantImage2() {
            return this.mDependantImage2;
        }

        public void setImageAttrib(int i) {
            this.mImageAttrib = i;
        }

        public void setImageSize(int i) {
            this.mImageSize = i;
        }

        public void setImageOffset(int i) {
            this.mImageOffset = i;
        }

        public void setDependantImage1(short s) {
            this.mDependantImage1 = s;
        }

        public void setDependantImage2(short s) {
            this.mDependantImage2 = s;
        }

        public boolean getBytes(ByteBuffer byteBuffer) {
            try {
                byteBuffer.putInt(this.mImageAttrib);
                byteBuffer.putInt(this.mImageSize);
                byteBuffer.putInt(this.mImageOffset);
                byteBuffer.putShort(this.mDependantImage1);
                byteBuffer.putShort(this.mDependantImage2);
                return true;
            } catch (BufferOverflowException unused) {
                Log.w(MpoTag.TAG, "Buffer size too small");
                return false;
            }
        }
    }

    MpoTag(short s, short s2, int i, int i2, boolean z) {
        super(s, s2, i, i2, z);
    }

    public boolean setValue(List<MpEntry> list) {
        if (getTagId() != ((short) MpoInterface.TAG_MP_ENTRY)) {
            return false;
        }
        byte[] bArr = new byte[(list.size() * 16)];
        for (int i = 0; i < list.size(); i++) {
            ((MpEntry) list.get(i)).getBytes(ByteBuffer.wrap(bArr, i * 16, 16));
        }
        return setValue(bArr);
    }

    public List<MpEntry> getMpEntryValue() {
        if (getTagId() != ((short) MpoInterface.TAG_MP_ENTRY)) {
            return null;
        }
        byte[] valueAsBytes = getValueAsBytes();
        ArrayList arrayList = new ArrayList(valueAsBytes.length / 16);
        for (int i = 0; i < valueAsBytes.length; i += 16) {
            arrayList.add(new MpEntry(ByteBuffer.wrap(valueAsBytes, i, 16)));
        }
        return arrayList;
    }
}
