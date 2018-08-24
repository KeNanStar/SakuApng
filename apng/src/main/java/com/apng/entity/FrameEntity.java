package com.apng.entity;

/**
 * Created by Shark0 on 2016/9/13.
 */
public class FrameEntity {
    private FctlChunkEntity frameControlChunk;

    private ChunkEntity frameDataChunk;

    public FctlChunkEntity getFrameControlChunk() {
        return frameControlChunk;
    }

    public void setFrameControlChunk(FctlChunkEntity frameControlChunk) {
        this.frameControlChunk = frameControlChunk;
    }

    public ChunkEntity getFrameDataChunk() {
        return frameDataChunk;
    }

    public void setFrameDataChunk(ChunkEntity frameDataChunk) {
        this.frameDataChunk = frameDataChunk;
    }
}
