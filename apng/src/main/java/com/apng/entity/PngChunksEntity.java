package com.apng.entity;


import java.io.*;
import java.util.*;

/**
 * Created by Shark0 on 2016/10/4.
 */
public class PngChunksEntity implements Serializable {

    private boolean isApng;

    private transient IhdrChunkEntity ihdrChunkEntity;

    private transient ChunkEntity idatChunkEntity;

    private transient ChunkEntity iendChunkEntity;

    private ActlChunkEntity actlChunkEntity;

    private List<FrameEntity> frameList;

    private transient List<ChunkEntity> unknownChunkList;

    public boolean isApng() {
        return isApng;
    }

    public void setIsApng(boolean isApng) {
        this.isApng = isApng;
    }

    public IhdrChunkEntity getIhdrChunkEntity() {
        return ihdrChunkEntity;
    }

    public void setIhdrChunkEntity(IhdrChunkEntity ihdrChunkEntity) {
        this.ihdrChunkEntity = ihdrChunkEntity;
    }

    public ChunkEntity getIdatChunkEntity() {
        return idatChunkEntity;
    }

    public void setIdatChunkEntity(ChunkEntity idatChunkEntity) {
        this.idatChunkEntity = idatChunkEntity;
    }

    public ChunkEntity getIendChunkEntity() {
        return iendChunkEntity;
    }

    public void setIendChunkEntity(ChunkEntity iendChunkEntity) {
        this.iendChunkEntity = iendChunkEntity;
    }

    public ActlChunkEntity getActlChunkEntity() {
        return actlChunkEntity;
    }

    public void setActlChunkEntity(ActlChunkEntity actlChunkEntity) {
        this.actlChunkEntity = actlChunkEntity;
    }

    public List<FrameEntity> getFrameList() {
        return frameList;
    }

    public void setFrameList(List<FrameEntity> frameList) {
        this.frameList = frameList;
    }

    public List<ChunkEntity> getUnknownChunkList() {
        return unknownChunkList;
    }

    public void setUnknownChunkList(List<ChunkEntity> unknownChunkList) {
        this.unknownChunkList = unknownChunkList;
    }
}
