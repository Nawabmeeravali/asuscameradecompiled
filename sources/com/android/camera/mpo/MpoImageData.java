package com.android.camera.mpo;

import com.android.camera.exif.ExifTag;
import java.nio.ByteOrder;

public class MpoImageData {
    private static final String TAG = "MpoImageData";
    private final ByteOrder mByteOrder;
    private final byte[] mJpegData;
    private final MpoIfdData mMpAttribIfdData = new MpoIfdData(2);
    private final MpoIfdData mMpIndexIfdData = new MpoIfdData(1);

    public MpoImageData(byte[] bArr, ByteOrder byteOrder) {
        this.mJpegData = bArr;
        this.mByteOrder = byteOrder;
    }

    /* access modifiers changed from: protected */
    public byte[] getJpegData() {
        return this.mJpegData;
    }

    /* access modifiers changed from: protected */
    public ByteOrder getByteOrder() {
        return this.mByteOrder;
    }

    /* access modifiers changed from: protected */
    public MpoIfdData getAttribIfdData() {
        return this.mMpAttribIfdData;
    }

    /* access modifiers changed from: protected */
    public MpoIfdData getIndexIfdData() {
        return this.mMpIndexIfdData;
    }

    /* access modifiers changed from: protected */
    public MpoIfdData getMpIfdData(int i) {
        return i == 1 ? this.mMpIndexIfdData : this.mMpAttribIfdData;
    }

    /* access modifiers changed from: protected */
    public MpoTag getTag(short s, int i) {
        return getMpIfdData(i).getTag(s);
    }

    /* access modifiers changed from: protected */
    public MpoTag addTag(MpoTag mpoTag) {
        if (mpoTag != null) {
            return addTag(mpoTag, mpoTag.getIfd());
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public MpoTag addTag(MpoTag mpoTag, int i) {
        if (mpoTag == null || !ExifTag.isValidIfd(i)) {
            return null;
        }
        return getMpIfdData(i).setTag(mpoTag);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof MpoImageData)) {
            return false;
        }
        MpoImageData mpoImageData = (MpoImageData) obj;
        if (mpoImageData.mByteOrder != this.mByteOrder) {
            return false;
        }
        MpoIfdData mpIfdData = mpoImageData.getMpIfdData(1);
        MpoIfdData mpIfdData2 = getMpIfdData(1);
        if (mpIfdData != mpIfdData2 && mpIfdData != null && !mpIfdData.equals(mpIfdData2)) {
            return false;
        }
        MpoIfdData mpIfdData3 = mpoImageData.getMpIfdData(2);
        MpoIfdData mpIfdData4 = getMpIfdData(2);
        return mpIfdData3 == mpIfdData4 || mpIfdData3 == null || mpIfdData3.equals(mpIfdData4);
    }

    private int calculateOffsetOfIfd(MpoIfdData mpoIfdData, int i) {
        MpoTag[] allTags;
        int tagCount = i + (mpoIfdData.getTagCount() * 12) + 2 + 4;
        for (MpoTag mpoTag : mpoIfdData.getAllTags()) {
            if (mpoTag.getDataSize() > 4) {
                mpoTag.setOffset(tagCount);
                tagCount += mpoTag.getDataSize();
            }
        }
        return tagCount;
    }

    public int calculateAllIfdOffsets() {
        MpoIfdData indexIfdData = getIndexIfdData();
        int i = 8;
        if (indexIfdData.getTagCount() > 0) {
            i = calculateOffsetOfIfd(indexIfdData, 8);
        }
        MpoIfdData attribIfdData = getAttribIfdData();
        if (attribIfdData.getTagCount() <= 0) {
            return i;
        }
        indexIfdData.setOffsetToNextIfd(i);
        return calculateOffsetOfIfd(attribIfdData, i);
    }

    public int calculateImageSize() {
        return calculateAllIfdOffsets() + 8 + this.mJpegData.length;
    }
}
