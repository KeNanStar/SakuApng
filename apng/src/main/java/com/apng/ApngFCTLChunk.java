package com.apng;

/**
 * FCTL Chunk
 *
 * @author ltf
 * @since 16/11/28, 下午12:10
 */
public class ApngFCTLChunk extends ApngDataChunk {

    public static final byte APNG_DISPOSE_OP_NONE = 0;
    public static final byte APNG_DISPOSE_OP_BACKGROUND = 1;
    public static final byte APNG_DISPOSE_OP_PREVIOUS = 2;
    public static final byte APNG_BLEND_OP_SOURCE = 0;
    public static final byte APNG_BLEND_OP_OVER = 1;
    private int seqNum;
    private int width;
    private int height;
    private int xOff;
    private int yOff;
    private int delayNum;
    private int delayDen;
    private byte disposeOp;
    private byte blendOp;


    public int getSeqNum() {
        return seqNum;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getxOff() {
        return xOff;
    }

    public int getyOff() {
        return yOff;
    }

    public int getDelayNum() {
        return delayNum;
    }

    public int getDelayDen() {
        return delayDen;
    }

    public byte getDisposeOp() {
        return disposeOp;
    }

    public byte getBlendOp() {
        return blendOp;
    }

    @Override
    protected void parseData(ApngDataSupplier data) {
        this.seqNum = data.readInt();
        this.width = data.readInt();
        this.height = data.readInt();
        this.xOff = data.readInt();
        this.yOff = data.readInt();
        this.delayNum = data.readShort();
        this.delayDen = data.readShort();
        this.disposeOp = data.readByte();
        this.blendOp = data.readByte();
    }
}
