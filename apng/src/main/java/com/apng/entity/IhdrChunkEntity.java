package com.apng.entity;


import com.apng.ByteUtil;

import java.util.zip.*;

/**
 * Created by Shark0 on 2016/9/22.
 */
public class IhdrChunkEntity extends ChunkEntity {

    private int width;
    private int height;
    private int bitDepth;
    private int colourType;
    private int compressionMethod;
    private int filterMethod;
    private int interfaceMethod;

    public void setDataBytes(byte[] dataBytes) {
        byte[] widthBytes = ByteUtil.subBytes(dataBytes, 0, 4);
        width = ByteUtil.bytesToInt(widthBytes);

        byte[] heightBytes = ByteUtil.subBytes(dataBytes, 4, 8);
        height = ByteUtil.bytesToInt(heightBytes);

        bitDepth = dataBytes[8];
        colourType = dataBytes[9];
        compressionMethod = dataBytes[10];
        filterMethod  = dataBytes[11];
        interfaceMethod  = dataBytes[12];
        super.setDataBytes(dataBytes);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;

        byte[] dataByte = getDataBytes();
        byte[] widthBytes = ByteUtil.intToBytes(width);
        for(int i = 0; i < widthBytes.length; i ++) {
            dataByte[i] = widthBytes[i];
        }
        setDataBytes(dataByte);
        CRC32 crc32 = new CRC32();
        crc32.update(getTagBytes(), 0, 4);
        if(getLength() > 0) {
            crc32.update(dataByte, 0, getLength());
        }
        byte[] crcBytes = ByteUtil.intToBytes((int) crc32.getValue());
        setCrcBytes(crcBytes);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        byte[] dataByte = getDataBytes();
        byte[] heightBytes = ByteUtil.intToBytes(height);
        for(int i = 0; i < heightBytes.length; i ++) {
            dataByte[i + 4] = heightBytes[i];
        }
        setDataBytes(dataByte);
        CRC32 crc32 = new CRC32();
        crc32.update(getTagBytes(), 0, 4);
        if(getLength() > 0) {
            crc32.update(dataByte, 0, getLength());
        }
        byte[] crcBytes = ByteUtil.intToBytes((int) crc32.getValue());
        setCrcBytes(crcBytes);
    }

    public int getBitDepth() {
        return bitDepth;
    }

    public void setBitDepth(int bitDepth) {
        this.bitDepth = bitDepth;
    }

    public int getColourType() {
        return colourType;
    }

    public void setColourType(int colourType) {
        this.colourType = colourType;
    }

    public int getCompressionMethod() {
        return compressionMethod;
    }

    public void setCompressionMethod(int compressionMethod) {
        this.compressionMethod = compressionMethod;
    }

    public int getFilterMethod() {
        return filterMethod;
    }

    public void setFilterMethod(int filterMethod) {
        this.filterMethod = filterMethod;
    }

    public int getInterfaceMethod() {
        return interfaceMethod;
    }

    public void setInterfaceMethod(int interfaceMethod) {
        this.interfaceMethod = interfaceMethod;
    }
}
