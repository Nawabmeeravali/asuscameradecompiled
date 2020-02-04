package com.android.camera.exif;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OrderedDataOutputStream extends FilterOutputStream {
    private final ByteBuffer mByteBuffer = ByteBuffer.allocate(4);
    private int mSize = 0;

    public OrderedDataOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    public OrderedDataOutputStream setByteOrder(ByteOrder byteOrder) {
        this.mByteBuffer.order(byteOrder);
        return this;
    }

    public OrderedDataOutputStream writeShort(short s) throws IOException {
        this.mByteBuffer.rewind();
        this.mByteBuffer.putShort(s);
        this.out.write(this.mByteBuffer.array(), 0, 2);
        this.mSize += 2;
        return this;
    }

    public OrderedDataOutputStream writeInt(int i) throws IOException {
        this.mByteBuffer.rewind();
        this.mByteBuffer.putInt(i);
        this.out.write(this.mByteBuffer.array());
        this.mSize += 4;
        return this;
    }

    public OrderedDataOutputStream writeRational(Rational rational) throws IOException {
        writeInt((int) rational.getNumerator());
        writeInt((int) rational.getDenominator());
        return this;
    }

    public int size() {
        return this.mSize;
    }
}