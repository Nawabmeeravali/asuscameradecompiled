package com.android.camera.mpo;

import android.util.Log;
import com.android.camera.exif.ExifInterface.ColorSpace;
import com.android.camera.exif.JpegHeader;
import com.android.camera.exif.OrderedDataOutputStream;
import com.android.camera.util.PersistUtil;
import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

class MpoOutputStream extends FilterOutputStream {
    private static final String DC_CROP_INFO = "Qualcomm Dual Camera Attributes";
    private static final int DC_CROP_INFO_BYTE_SIZE = 31;
    private static final boolean DEBUG = (PersistUtil.getCamera2Debug() == 2 || PersistUtil.getCamera2Debug() == 100);
    private static final int MAX_EXIF_SIZE = 65535;
    private static final int STATE_FRAME_HEADER = 1;
    private static final int STATE_JPEG_DATA = 3;
    private static final int STATE_SKIP_CROP = 2;
    private static final int STATE_SOI = 0;
    private static final int STREAMBUFFER_SIZE = 65536;
    private static final String TAG = "MpoOutputStream";
    private static final short TIFF_BIG_ENDIAN = 19789;
    private static final short TIFF_HEADER = 42;
    private static final short TIFF_LITTLE_ENDIAN = 18761;
    private ByteBuffer mBuffer = ByteBuffer.allocate(4);
    private int mByteToCopy;
    private int mByteToSkip;
    private ByteBuffer mCropInfo = ByteBuffer.allocate(DC_CROP_INFO_BYTE_SIZE);
    private MpoImageData mCurrentImageData;
    private MpoData mMpoData;
    private int mMpoOffsetStart = -1;
    private byte[] mSingleByteArray = new byte[1];
    private int mSize = 0;
    private boolean mSkipCropData = false;
    private int mState = 0;

    protected MpoOutputStream(OutputStream outputStream) {
        super(new BufferedOutputStream(outputStream, 65536));
    }

    /* access modifiers changed from: protected */
    public void setMpoData(MpoData mpoData) {
        this.mMpoData = mpoData;
        this.mMpoData.updateAllTags();
    }

    private void resetStates() {
        this.mState = 0;
        this.mByteToSkip = 0;
        this.mByteToCopy = 0;
        this.mBuffer.rewind();
    }

    private int requestByteToBuffer(ByteBuffer byteBuffer, int i, byte[] bArr, int i2, int i3) {
        int position = i - byteBuffer.position();
        if (i3 <= position) {
            position = i3;
        }
        byteBuffer.put(bArr, i2, position);
        return position;
    }

    private boolean isDualCamCropInfo() {
        if (this.mCropInfo.position() != DC_CROP_INFO_BYTE_SIZE) {
            return false;
        }
        this.mCropInfo.rewind();
        for (int i = 0; i < 31; i++) {
            if (DC_CROP_INFO.charAt(i) != ((char) this.mCropInfo.get(i))) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: 0000 */
    public void writeMpoFile() throws IOException {
        this.mCurrentImageData = this.mMpoData.getPrimaryMpoImage();
        if (this.mMpoData.getAuxiliaryImageCount() > 1) {
            this.mSkipCropData = true;
        }
        write(this.mCurrentImageData.getJpegData());
        flush();
        this.mSkipCropData = false;
        for (MpoImageData mpoImageData : this.mMpoData.getAuxiliaryMpoImages()) {
            resetStates();
            this.mCurrentImageData = mpoImageData;
            write(this.mCurrentImageData.getJpegData());
            flush();
        }
    }

    public void write(byte[] bArr, int i, int i2) throws IOException {
        byte[] bArr2 = bArr;
        int i3 = i;
        int i4 = i2;
        while (true) {
            if ((this.mByteToSkip > 0 || this.mByteToCopy > 0 || this.mState != 3) && i4 > 0) {
                int i5 = this.mByteToSkip;
                if (i5 > 0) {
                    if (i4 <= i5) {
                        i5 = i4;
                    }
                    i4 -= i5;
                    this.mByteToSkip -= i5;
                    i3 += i5;
                }
                int i6 = this.mByteToCopy;
                if (i6 > 0) {
                    if (i4 <= i6) {
                        i6 = i4;
                    }
                    this.out.write(bArr2, i3, i6);
                    this.mSize += i6;
                    i4 -= i6;
                    this.mByteToCopy -= i6;
                    i3 += i6;
                }
                int i7 = i3;
                int i8 = i4;
                if (i8 != 0) {
                    int i9 = this.mState;
                    if (i9 == 0) {
                        int requestByteToBuffer = requestByteToBuffer(this.mBuffer, 2, bArr, i7, i8);
                        i7 += requestByteToBuffer;
                        i8 -= requestByteToBuffer;
                        if (this.mBuffer.position() >= 2) {
                            this.mBuffer.rewind();
                            if (this.mBuffer.getShort() == -40) {
                                this.out.write(this.mBuffer.array(), 0, 2);
                                this.mSize += 2;
                                this.mState = 1;
                                this.mBuffer.rewind();
                            } else {
                                throw new IOException("Not a valid jpeg image, cannot write exif");
                            }
                        } else {
                            return;
                        }
                    } else if (i9 == 1) {
                        int requestByteToBuffer2 = requestByteToBuffer(this.mBuffer, 4, bArr, i7, i8);
                        if (this.mBuffer.position() == 2 && this.mBuffer.getShort() == -39) {
                            this.out.write(this.mBuffer.array(), 0, 2);
                            this.mSize += 2;
                            this.mBuffer.rewind();
                        }
                        if (this.mBuffer.position() >= 4) {
                            this.mBuffer.rewind();
                            short s = this.mBuffer.getShort();
                            if (s == -31 || s == -32) {
                                this.out.write(this.mBuffer.array(), 0, 4);
                                this.mSize += 4;
                                this.mByteToCopy = (this.mBuffer.getShort() & ColorSpace.UNCALIBRATED) - 2;
                                i7 += requestByteToBuffer2;
                                i8 -= requestByteToBuffer2;
                            } else {
                                writeMpoData();
                                if (this.mSkipCropData) {
                                    this.mState = 2;
                                } else {
                                    this.mState = 3;
                                }
                            }
                            this.mBuffer.rewind();
                        } else {
                            return;
                        }
                    } else if (i9 != 2) {
                        continue;
                    } else {
                        int requestByteToBuffer3 = requestByteToBuffer(this.mBuffer, 4, bArr, i7, i8);
                        if (this.mBuffer.position() == 2 && this.mBuffer.getShort() == -39) {
                            this.out.write(this.mBuffer.array(), 0, 2);
                            this.mSize += 2;
                            this.mBuffer.rewind();
                        }
                        if (this.mBuffer.position() >= 4) {
                            i7 += requestByteToBuffer3;
                            i8 -= requestByteToBuffer3;
                            this.mBuffer.rewind();
                            if (!JpegHeader.isSofMarker(this.mBuffer.getShort())) {
                                requestByteToBuffer(this.mCropInfo, DC_CROP_INFO_BYTE_SIZE, bArr, i7, i8);
                                if (isDualCamCropInfo()) {
                                    this.out.write(this.mBuffer.array(), 0, 4);
                                    this.mSize += 4;
                                    int i10 = (this.mBuffer.getShort() & ColorSpace.UNCALIBRATED) - 2;
                                    this.mByteToSkip = i10;
                                    while (i10 > 0) {
                                        this.out.write(0);
                                        this.mSize++;
                                        i10--;
                                    }
                                    this.mState = 3;
                                } else {
                                    this.out.write(this.mBuffer.array(), 0, 4);
                                    this.mSize += 4;
                                    this.mByteToCopy = (this.mBuffer.getShort() & ColorSpace.UNCALIBRATED) - 2;
                                }
                                this.mCropInfo.rewind();
                            } else {
                                this.out.write(this.mBuffer.array(), 0, 4);
                                this.mSize += 4;
                                this.mState = 3;
                            }
                            this.mBuffer.rewind();
                        } else {
                            return;
                        }
                    }
                    i4 = i8;
                    i3 = i7;
                } else {
                    return;
                }
            }
        }
        if (i4 > 0) {
            this.out.write(bArr2, i3, i4);
            this.mSize += i4;
        }
    }

    public void write(int i) throws IOException {
        byte[] bArr = this.mSingleByteArray;
        bArr[0] = (byte) (i & 255);
        write(bArr);
    }

    public void write(byte[] bArr) throws IOException {
        write(bArr, 0, bArr.length);
    }

    private void writeMpoData() throws IOException {
        if (this.mMpoData != null) {
            if (DEBUG) {
                Log.v(TAG, "Writing mpo data...");
            }
            int calculateAllIfdOffsets = this.mCurrentImageData.calculateAllIfdOffsets() + 6;
            if (calculateAllIfdOffsets <= 65535) {
                OrderedDataOutputStream orderedDataOutputStream = new OrderedDataOutputStream(this.out);
                orderedDataOutputStream.setByteOrder(ByteOrder.BIG_ENDIAN);
                orderedDataOutputStream.writeShort(-30);
                orderedDataOutputStream.writeShort((short) calculateAllIfdOffsets);
                orderedDataOutputStream.writeInt(1297106432);
                if (this.mMpoOffsetStart == -1) {
                    this.mMpoOffsetStart = this.mSize + orderedDataOutputStream.size();
                }
                if (this.mCurrentImageData.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                    orderedDataOutputStream.writeShort(TIFF_BIG_ENDIAN);
                } else {
                    orderedDataOutputStream.writeShort(TIFF_LITTLE_ENDIAN);
                }
                orderedDataOutputStream.setByteOrder(this.mCurrentImageData.getByteOrder());
                orderedDataOutputStream.writeShort(TIFF_HEADER);
                if (calculateAllIfdOffsets > 14) {
                    orderedDataOutputStream.writeInt(8);
                    writeAllTags(orderedDataOutputStream);
                } else {
                    orderedDataOutputStream.writeInt(0);
                }
                this.mSize += orderedDataOutputStream.size();
                return;
            }
            throw new IOException("Exif header is too large (>64Kb)");
        }
    }

    private void updateIndexIfdOffsets(MpoIfdData mpoIfdData, int i) {
        MpoTag tag = this.mMpoData.getPrimaryMpoImage().getTag((short) MpoInterface.TAG_MP_ENTRY, 1);
        List mpEntryValue = tag.getMpEntryValue();
        for (int i2 = 1; i2 < mpEntryValue.size(); i2++) {
            MpEntry mpEntry = (MpEntry) mpEntryValue.get(i2);
            mpEntry.setImageOffset(mpEntry.getImageOffset() - i);
        }
        tag.setValue(mpEntryValue);
    }

    private void writeAllTags(OrderedDataOutputStream orderedDataOutputStream) throws IOException {
        MpoIfdData indexIfdData = this.mCurrentImageData.getIndexIfdData();
        if (indexIfdData.getTagCount() > 0) {
            updateIndexIfdOffsets(indexIfdData, this.mMpoOffsetStart);
            writeIfd(indexIfdData, orderedDataOutputStream);
        }
        MpoIfdData attribIfdData = this.mCurrentImageData.getAttribIfdData();
        if (attribIfdData.getTagCount() > 0) {
            writeIfd(attribIfdData, orderedDataOutputStream);
        }
    }

    private void writeIfd(MpoIfdData mpoIfdData, OrderedDataOutputStream orderedDataOutputStream) throws IOException {
        MpoTag[] allTags = mpoIfdData.getAllTags();
        orderedDataOutputStream.writeShort((short) allTags.length);
        for (MpoTag mpoTag : allTags) {
            orderedDataOutputStream.writeShort(mpoTag.getTagId());
            orderedDataOutputStream.writeShort(mpoTag.getDataType());
            orderedDataOutputStream.writeInt(mpoTag.getComponentCount());
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("\n");
                sb.append(mpoTag.toString());
                Log.v(TAG, sb.toString());
            }
            if (mpoTag.getDataSize() > 4) {
                orderedDataOutputStream.writeInt(mpoTag.getOffset());
            } else {
                writeTagValue(mpoTag, orderedDataOutputStream);
                int dataSize = 4 - mpoTag.getDataSize();
                for (int i = 0; i < dataSize; i++) {
                    orderedDataOutputStream.write(0);
                }
            }
        }
        orderedDataOutputStream.writeInt(mpoIfdData.getOffsetToNextIfd());
        for (MpoTag mpoTag2 : allTags) {
            if (mpoTag2.getDataSize() > 4) {
                writeTagValue(mpoTag2, orderedDataOutputStream);
            }
        }
    }

    static void writeTagValue(MpoTag mpoTag, OrderedDataOutputStream orderedDataOutputStream) throws IOException {
        int i = 0;
        switch (mpoTag.getDataType()) {
            case 1:
            case 7:
                byte[] bArr = new byte[mpoTag.getComponentCount()];
                mpoTag.getBytes(bArr);
                orderedDataOutputStream.write(bArr);
                return;
            case 2:
                byte[] stringByte = mpoTag.getStringByte();
                if (stringByte.length == mpoTag.getComponentCount()) {
                    stringByte[stringByte.length - 1] = 0;
                    orderedDataOutputStream.write(stringByte);
                    return;
                }
                orderedDataOutputStream.write(stringByte);
                orderedDataOutputStream.write(0);
                return;
            case 3:
                int componentCount = mpoTag.getComponentCount();
                while (i < componentCount) {
                    orderedDataOutputStream.writeShort((short) ((int) mpoTag.getValueAt(i)));
                    i++;
                }
                return;
            case 4:
            case 9:
                int componentCount2 = mpoTag.getComponentCount();
                while (i < componentCount2) {
                    orderedDataOutputStream.writeInt((int) mpoTag.getValueAt(i));
                    i++;
                }
                return;
            case 5:
            case 10:
                int componentCount3 = mpoTag.getComponentCount();
                while (i < componentCount3) {
                    orderedDataOutputStream.writeRational(mpoTag.getRational(i));
                    i++;
                }
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: 0000 */
    public int size() {
        return this.mSize;
    }
}
