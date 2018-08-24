package com.apng;

import java.io.*;
import java.nio.*;

/**
 * Parsable Apng Chunk Over MappedByteBuffer
 *
 * @author ltf
 * @since 16/11/26, 下午12:11
 */
public class ApngMmapParserChunk extends ApngPaserChunk {
    // data buffer
    protected final MappedByteBuffer mBuf;

    // used to store the read pointer when Read Chunk As Stream
    // when lastPos>=0, it's locked, else if lastPost=-1, it's not locked
    private int lastPos = -1;

    public ApngMmapParserChunk(MappedByteBuffer mBuf) {
        this.mBuf = mBuf;
    }

    ApngMmapParserChunk(ApngMmapParserChunk copyFromChunk) {
        super(copyFromChunk);
        this.mBuf = copyFromChunk.mBuf;
        lastPos = copyFromChunk.lastPos;
    }

    @Override
    public void parsePrepare(int offset) {
        super.parsePrepare(offset);
        mBuf.position(offset);
        lastPos = -1; // parse prepare will clear readLock
    }

    @Override
    public int readInt() {
        return mBuf.getInt();
    }

    @Override
    public short readShort() {
        return mBuf.getShort();
    }

    @Override
    public byte readByte() {
        return mBuf.get();
    }

    @Override
    public void move(int distance) {
        mBuf.position(mBuf.position() + distance);
    }

    /**
     * the total chunk as a stream's length, that's size+code+data+crc all sections' length
     */
    int getStreamLen() {
        return length + 12;
    }

    /**
     * save current read pointer, and reset it to data section's head for read
     */
    void lockRead() {
        lastPos = mBuf.position();
        mBuf.position(offset);
    }

    /**
     * save current read pointer, and set it to specified startOffset for read
     */
    void lockRead(int startOffset) {
        lastPos = mBuf.position();
        mBuf.position(startOffset);
    }

    /**
     * restore read pointer lastPosition before call readAsStream()
     */
    void unlockRead() {
        if (lastPos >= 0) {
            mBuf.position(lastPos);
            lastPos = -1;
        }
    }

    /**
     * read data to buffer array
     * <p>
     * !!! ATTENTION: must call lockRead() to move read pointer to head before call this function
     *
     * @param buffer     target buffer array
     * @param byteOffset offset at target buffer array
     * @param byteCount  bytes to read
     * @return readed bytes
     * @throws IOException
     */
    int readAsStream(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        int size = nextOffset - mBuf.position();
        if (size <= 0) return 0;
        size = size > byteCount ? byteCount : size;

        mBuf.get(buffer, byteOffset, size);
        return size;
    }

    /**
     * assign to a data chunk to holder the data
     */
    void assignTo(ApngDataChunk dataChunk) {
        int pos = mBuf.position();
        mBuf.position(offset);
        try {
            dataChunk.parse(this);
        } finally {
            mBuf.position(pos);
        }
    }

    /**
     * duplicate this chunk's data to an array
     */
    public byte[] duplicateData() throws IOException {
        byte[] data = new byte[getStreamLen()];
        lockRead();
        readAsStream(data, 0, data.length);
        unlockRead();
        return data;
    }
}
