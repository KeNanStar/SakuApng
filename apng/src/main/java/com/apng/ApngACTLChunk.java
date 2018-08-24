package com.apng;

/**
 * ACTL chunk
 *
 * @author ltf
 * @since 16/11/28, 下午12:09
 */
public class ApngACTLChunk extends ApngDataChunk {
    private int numFrames;
    private int numPlays;

    public int getNumPlays() {
        return numPlays;
    }

    public int getNumFrames() {
        return numFrames;
    }

    protected void parseData(ApngDataSupplier data) {
        this.numFrames = data.readInt();
        this.numPlays = data.readInt();
    }
}
