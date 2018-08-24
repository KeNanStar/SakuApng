package com.apng;

/**
 * Apng Chunk as data container
 *
 * @author ltf
 * @since 16/11/29, 下午12:16
 */
public abstract class ApngDataChunk extends ApngChunk {

    public void parse(ApngDataSupplier data) {
        length = data.readInt();
        typeCode = data.readInt();
        parseData(data);
        this.crc = data.readInt();
    }

    protected void parseData(ApngDataSupplier data) {
        data.move(length);
    }
}
