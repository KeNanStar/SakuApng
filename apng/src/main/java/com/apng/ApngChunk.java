package com.apng;

/**
 * Base Apng Chunk Object
 *
 *
 * @since 2026/3/17, 上午11:19
 */
abstract class ApngChunk {

    public int getLength() {
        return length;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public int getCrc() {
        return crc;
    }

    // data sections
    protected int length;
    protected int typeCode;
    protected int crc;


    ApngChunk() {

    }

    ApngChunk(ApngChunk copyFrom) {
        this.length = copyFrom.length;
        this.typeCode = copyFrom.typeCode;
        this.crc = copyFrom.crc;
    }
}
