package com.apng.entity;

import com.apng.ByteUtil;

/**
 * Created by Shark0 on 2016/9/21.
 */
public class FctlChunkEntity extends ChunkEntity{

    public static final int DISPOSE_OP_NONE = 0;
    public static final int DISPOSE_OP_BACKGROUND = 1;
    public static final int DISPOSE_OP_PREVIOUS = 2;
    public static final int BLEND_OP_SOURCE = 0;
    public static final int BLEND_OP_OVER = 1;

    private int sequenceNumber;
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;
    private int delayNumber;
    private int delayDen;
    private int disposeOp;
    private int blendOp;

    public void setDataBytes(byte[] dataBytes) {
//        Log.e("FctlChunk", "dataBytes: " + ByteUtil.bytesToHex(dataBytes));
        byte[] sequenceNumberBytes = ByteUtil.subBytes(dataBytes, 0, 4);
        sequenceNumber = ByteUtil.bytesToInt(sequenceNumberBytes);
//        Log.e("FctlChunk", "sequenceNumberBytes: " + ByteUtil.bytesToHex(sequenceNumberBytes));
//        Log.e("FctlChunk", "sequenceNumber: " + sequenceNumber);
        byte[] widthBytes = ByteUtil.subBytes(dataBytes, 4, 8);
        width = ByteUtil.bytesToInt(widthBytes);
//        Log.e("FctlChunk", "widthBytes: " + ByteUtil.bytesToHex(widthBytes));
//        Log.e("FctlChunk", "width: " + width);
        byte[] heightBytes = ByteUtil.subBytes(dataBytes, 8, 12);
        height = ByteUtil.bytesToInt(heightBytes);
//        Log.e("FctlChunk", "heightBytes: " + ByteUtil.bytesToHex(heightBytes));
//        Log.e("FctlChunk", "height: " + height);
        byte[] xOffsetBytes = ByteUtil.subBytes(dataBytes, 12, 16);
        xOffset = ByteUtil.bytesToInt(xOffsetBytes);
//        Log.e("FctlChunk", "xOffsetBytes: " + ByteUtil.bytesToHex(xOffsetBytes));
//        Log.e("FctlChunk", "xOffset: " + xOffset);
        byte[] yOffsetBytes = ByteUtil.subBytes(dataBytes, 16, 20);
        yOffset = ByteUtil.bytesToInt(yOffsetBytes);
//        Log.e("FctlChunk", "yOffsetBytes: " + ByteUtil.bytesToHex(yOffsetBytes));
//        Log.e("FctlChunk", "yOffset: " + yOffset);
        byte[] delayNumberBytes = ByteUtil.subBytes(dataBytes, 20, 22);
        delayNumber = ByteUtil.bytesToshort(delayNumberBytes);
//        Log.e("FctlChunk", "delayNumberBytes: " + ByteUtil.bytesToHex(delayNumberBytes));
//        Log.e("FctlChunk", "delayNumber: " + delayNumber);
        byte[] delayDenBytes = ByteUtil.subBytes(dataBytes, 22, 24);
        delayDen = ByteUtil.bytesToshort(delayDenBytes);
//        Log.e("FctlChunk", "delayDenBytes: " + ByteUtil.bytesToHex(delayDenBytes));
//        Log.e("FctlChunk", "delayDen: " + delayDen);
        disposeOp = dataBytes[24];
//        Log.e("FctlChunk", "disposeOp: " + blendOp);
        blendOp = dataBytes[25];
//        Log.e("FctlChunk", "blendOp: " + blendOp);
        super.setDataBytes(dataBytes);
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getxOffset() {
        return xOffset;
    }

    public void setxOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public int getDelayNumber() {
        return delayNumber;
    }

    public void setDelayNumber(int delayNumber) {
        this.delayNumber = delayNumber;
    }

    public int getDelayDen() {
        return delayDen;
    }

    public void setDelayDen(int delayDen) {
        this.delayDen = delayDen;
    }

    public int getDisposeOp() {
        return disposeOp;
    }

    public void setDisposeOp(int disposeOp) {
        this.disposeOp = disposeOp;
    }

    public int getBlendOp() {
        return blendOp;
    }

    public void setBlendOp(int blendOp) {
        this.blendOp = blendOp;
    }
}
