package com.apng;

import static com.apng.ApngConst.*;

/**
 * Apng Chunk Parser
 *
 * @author ltf
 * @since 16/11/26, 下午12:09
 */
abstract class ApngPaserChunk extends ApngChunk implements ApngDataSupplier {
    // chunk start offset, SHOULD NOT CHANGE AFTER PARSE PREPARED
    // used for parse and read
    protected int offset;

    // next chunk start offset, INITED AFTER CALL parse(), used for parseNext
    protected int nextOffset;

    ApngPaserChunk() {
    }

    ApngPaserChunk(ApngPaserChunk copyFrom) {
        super(copyFrom);
        this.offset = copyFrom.offset;
        this.nextOffset = copyFrom.nextOffset;
    }

    int getOffset() {
        return offset;
    }

    /**
     * set the offset before parse
     * !!! ATTENTION !!! parseNext() will start parse from current prepared offset
     */
    public void parsePrepare(int offset) {
        this.offset = offset;
        this.nextOffset = offset;
    }

    /**
     * parse chunk info,
     * and return next chunk's start position, or return -1 if this is the last chunk
     * ATTENTION: must call parsePrepare() to init the offset before call this function
     */
    public int parse() {
        length = readInt();
        typeCode = readInt();
        parseData();
        this.crc = readInt();
        nextOffset = typeCode == CODE_IEND ? -1 : offset + length + 12;
        return nextOffset;
    }

    /**
     * parse data
     * current read pointer is at the data start position,
     * after this function, should move read pointer to CRC's start position
     */
    protected void parseData() {
        move(length);
    }

    /**
     * relocate current chunk to next, and parse the chunk info,
     * return next chunk(the one after current parsed chunk)'s start position,
     * or return -1 if this is the last chunk
     * !!! ATTENTION !!!
     * when parsePrepare() called, parseNext() = parse(),
     * it will start parse from current prepared offset
     */
    public int parseNext() {
        parsePrepare(nextOffset);
        return parse();
    }

    /**
     * parse info from next chunk, and locate for the specified typeCode chunk
     * and return next chunk's start position, or return -1 if this is the last chunk
     */
    int locateNext(int chunkTypeCode) {
        parseNext();
        while (typeCode != chunkTypeCode && nextOffset > 0) {
            parseNext();
        }
        return nextOffset;
    }

    /**
     * read int from data and move the pointer 4 byte ahead
     */
    abstract public int readInt();

    /**
     * read int from data and move the pointer 2 byte ahead
     */
    abstract public short readShort();

    /**
     * read int from data and move the pointer 1 byte ahead
     */
    abstract public byte readByte();

    /**
     * move the pointer ahead by distance bytes
     */
    abstract public void move(int distance);
}
