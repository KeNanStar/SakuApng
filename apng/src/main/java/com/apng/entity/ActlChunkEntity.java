package com.apng.entity;

import com.apng.*;

/**
 * Created by Shark0 on 2016/9/22.
 */
public class ActlChunkEntity extends ChunkEntity {

    private int frameCount;

    private int playCount;

    public void setDataBytes(byte[] dataBytes) {
        byte[] frameCountBytes = ByteUtil.subBytes(dataBytes, 0, 4);
        frameCount = ByteUtil.bytesToInt(frameCountBytes);

        byte[] playCountBytes = ByteUtil.subBytes(dataBytes, 4, 8);
        playCount = ByteUtil.bytesToInt(playCountBytes);

        super.setDataBytes(dataBytes);
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }
}
