package com.apng;

import android.graphics.*;
import android.util.*;
import com.apng.entity.*;

import java.nio.charset.*;
import java.util.*;
import java.util.zip.*;

/**
 * Created by Shark0 on 2016/9/13.
 */
public class ApngParser {

    private final String Tag = ApngParser.class.getCanonicalName();
    private final boolean debug = true;

    public static final byte[] PNG_TAG_BYTES = new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A};

    public static final byte[] IHDR_TAG_BYTES = new byte[]{(byte) 0x49, (byte) 0x48, (byte) 0x44, (byte) 0x52};

    public static final byte[] ACTL_TAG_BYTES = new byte[]{(byte) 0x61, (byte) 0x63, (byte) 0x54, (byte) 0x4c};

    public static final byte[] FCTL_TAG_BYTES = new byte[]{(byte) 0x66, (byte) 0x63, (byte) 0x54, (byte) 0x4c};

    public static final byte[] IDAT_TAG_BYTES = new byte[]{(byte) 0x49, (byte) 0x44, (byte) 0x41, (byte) 0x54};

    public static final byte[] FDAT_TAG_BYTES = new byte[]{(byte) 0x66, (byte) 0x64, (byte) 0x41, (byte) 0x54};

    public final String IHDR_TAG = "IHDR";
    public final String ACTL_TAG = "acTL";
    public final String IDAT_TAG = "IDAT";
    public final String FCTL_TAG = "fcTL";
    public final String FDAT_TAG = "fdAT";
    public final String IEND_TAG = "IEND";

    private final int CHUNK_DATA_LENGTH_BYTES_LENGTH = 4;
    private final int CHUNK_TAG_BYTES_LENGTH = 4;
    private final int CHUNK_CRC_BYTES_LENGTH = 4;

    private byte[] imageBytes;
    private Bitmap bitmap;
    private boolean isApng;

    private PngChunksEntity pngChunkEntity = new PngChunksEntity();


    public ApngParser(byte[] imageBytes) {
        this.imageBytes = imageBytes;
        bitmap =  BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        int actlIndex = ByteUtil.indexOf(imageBytes, ACTL_TAG_BYTES);
        isApng = actlIndex != -1;

        pngChunkEntity.setIsApng(isApng);
    }

    public void parserApng() {

        List<FrameEntity> frameList = new LinkedList<>();
        List<ChunkEntity> chunkList = new LinkedList<>();
        List<ChunkEntity> unknownChunkList = new LinkedList<>();
        pngChunkEntity.setFrameList(frameList);
        pngChunkEntity.setUnknownChunkList(unknownChunkList);

        int chunkLengthStartIndex = PNG_TAG_BYTES.length;
        int i = 0;
        while(chunkLengthStartIndex + CHUNK_DATA_LENGTH_BYTES_LENGTH < imageBytes.length) {
            int chunkLengthEndIndex = chunkLengthStartIndex + CHUNK_DATA_LENGTH_BYTES_LENGTH;
            byte[] lengthBytes = ByteUtil.subBytes(imageBytes,
                    chunkLengthStartIndex, chunkLengthEndIndex);
            int length = ByteUtil.bytesToInt(lengthBytes);
            if(debug) {
                Log.e(Tag, "i: " + i + ", length: " + length);
            }
            int tagStartIndex = chunkLengthEndIndex;
            int tagEndIndex = tagStartIndex + CHUNK_TAG_BYTES_LENGTH;
            byte[] tagBytes = ByteUtil.subBytes(imageBytes, tagStartIndex, tagEndIndex);
            if(debug) {
                Log.e(Tag, "i: " + i + ", chunkTag: " + ByteUtil.bytesToHex(tagBytes));
            }
            String tag = new String(tagBytes, Charset.forName("UTF-8"));
            if(debug) {
                Log.e(Tag, "i: " + i + ", chunkTag: " + tag);
            }
            int dataStartIndex = tagEndIndex;
            int dataEndIndex = dataStartIndex + length;
            byte[] dataBytes = ByteUtil.subBytes(imageBytes, dataStartIndex, dataEndIndex);
            if(debug) {
                Log.e(Tag, "i: " + i + ", chunkData: " + ByteUtil.bytesToHex(dataBytes));
            }
            int crcStartIndex = dataEndIndex;
            int crcEndIndex = crcStartIndex + CHUNK_CRC_BYTES_LENGTH;
            byte[] crcBytes = ByteUtil.subBytes(imageBytes, crcStartIndex, crcEndIndex);
            if(debug) {
                Log.e(Tag, "i: " + i + ", chunkCrc: " + ByteUtil.bytesToHex(crcBytes));
            }
            ChunkEntity chunkEntity;
            switch (tag) {
                case ACTL_TAG:
                    chunkEntity = new ActlChunkEntity();
                    break;
                case IHDR_TAG:
                    chunkEntity = new IhdrChunkEntity();
                    break;
                case FCTL_TAG:
                    chunkEntity = new FctlChunkEntity();
                    break;
                default:
                    chunkEntity = new ChunkEntity();
                    break;
            }
            chunkEntity.setLengthBytes(lengthBytes);
            chunkEntity.setLength(length);
            chunkEntity.setTagBytes(tagBytes);
            chunkEntity.setTag(tag);
            chunkEntity.setDataBytes(dataBytes);
            chunkEntity.setCrcBytes(crcBytes);
            chunkList.add(chunkEntity);

            switch (tag) {
                case IHDR_TAG:
                    pngChunkEntity.setIhdrChunkEntity((IhdrChunkEntity) chunkEntity);
                    break;
                case ACTL_TAG:
                    pngChunkEntity.setActlChunkEntity((ActlChunkEntity) chunkEntity);
                    break;
                case IDAT_TAG:
                    pngChunkEntity.setIdatChunkEntity(chunkEntity);
                    FrameEntity frameEntity = new FrameEntity();
                    frameEntity.setFrameControlChunk((FctlChunkEntity) chunkList.get(i - 1));
                    frameEntity.setFrameDataChunk(chunkEntity);
                    frameList.add(frameEntity);
                    break;
                case FDAT_TAG:
                    if(!(chunkList.get(i - 1) instanceof FctlChunkEntity)) {
                      break;
                    }
                    frameEntity = new FrameEntity();
                    frameEntity.setFrameControlChunk((FctlChunkEntity) chunkList.get(i - 1));
                    int newLength = chunkEntity.getLength() - 4;
                    chunkEntity.setLength(newLength);
                    chunkEntity.setLengthBytes(ByteUtil.intToBytes(newLength));
                    chunkEntity.setTag(IDAT_TAG);
                    chunkEntity.setTagBytes(IDAT_TAG_BYTES);
                    chunkEntity.setDataBytes(ByteUtil.subBytes(chunkEntity.getDataBytes(), 4, chunkEntity.getDataBytes().length));
                    CRC32 crc32 = new CRC32();
                    crc32.update(chunkEntity.getTagBytes(), 0, 4);
                    if(chunkEntity.getLength() > 0) {
                        crc32.update(chunkEntity.getDataBytes(), 0, chunkEntity.getLength());
                    }
                    byte[] fdatCrcBytes = ByteUtil.intToBytes((int) crc32.getValue());
                    chunkEntity.setCrcBytes(fdatCrcBytes);
                    frameEntity.setFrameDataChunk(chunkEntity);
                    frameList.add(frameEntity);
                    break;
                case IEND_TAG:
                    pngChunkEntity.setIendChunkEntity(chunkEntity);
                    break;
                default:
                    unknownChunkList.add(chunkEntity);
                    break;
            }
            chunkLengthStartIndex = crcEndIndex;
            i = i + 1;
        }

    }


    public Bitmap generateFrameDataBitmap(PngChunksEntity pngChunkEntity, FrameEntity frameEntity) {

        if(frameEntity == null) return null;

        FctlChunkEntity fctlChunkEntity = frameEntity.getFrameControlChunk();
        ChunkEntity frameDataChunkEntity = frameEntity.getFrameDataChunk();
        List<ChunkEntity> bitmapChunkList = new LinkedList<>();

        IhdrChunkEntity ihdrChunkEntity = pngChunkEntity.getIhdrChunkEntity();
        ihdrChunkEntity.setWidth(fctlChunkEntity.getWidth());
        ihdrChunkEntity.setHeight(fctlChunkEntity.getHeight());
        bitmapChunkList.add(ihdrChunkEntity);

        bitmapChunkList.addAll(pngChunkEntity.getUnknownChunkList());
        bitmapChunkList.add(frameDataChunkEntity);
        bitmapChunkList.add(pngChunkEntity.getIendChunkEntity());

        int imageBytesSize = PNG_TAG_BYTES.length;
        for(ChunkEntity chunkEntity: bitmapChunkList) {
            imageBytesSize = imageBytesSize + chunkEntity.getLengthBytes().length + chunkEntity.getTag().length() +
                    chunkEntity.getDataBytes().length + chunkEntity.getCrcBytes().length;
        }
        byte[] imageBytes = new byte[imageBytesSize];

        for(int i = 0; i < PNG_TAG_BYTES.length; i ++) {
            imageBytes[i ] = PNG_TAG_BYTES[i];
        }
        int startIndex = PNG_TAG_BYTES.length;
        for(ChunkEntity chunkEntity: bitmapChunkList) {
            for(int i = 0; i < chunkEntity.getLengthBytes().length; i ++) {
                imageBytes[i + startIndex] = chunkEntity.getLengthBytes()[i];
            }
            startIndex = startIndex + chunkEntity.getLengthBytes().length;
            for(int i = 0; i < chunkEntity.getTagBytes().length; i ++) {
                imageBytes[i + startIndex] = chunkEntity.getTagBytes()[i];
            }

            startIndex = startIndex + chunkEntity.getTagBytes().length;
            for(int i = 0; i < chunkEntity.getDataBytes().length; i ++) {
                imageBytes[i + startIndex] = chunkEntity.getDataBytes()[i];
            }
            startIndex = startIndex + chunkEntity.getDataBytes().length;
            for(int i = 0; i < chunkEntity.getCrcBytes().length; i ++) {
                imageBytes[i + startIndex] = chunkEntity.getCrcBytes()[i];
            }
            startIndex = startIndex + chunkEntity.getCrcBytes().length;
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        if(debug) {
            Log.e(Tag, "generateFrameDataBitmap ihdr colour type: " + ihdrChunkEntity.getColourType());
            Log.e(Tag, "generateFrameDataBitmap image bytes: " + ByteUtil.bytesToHex(imageBytes));
            Log.e(Tag, "generateFrameDataBitmap is bitmap: " + (bitmap != null));
        }
        return bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public boolean isApng() {
        return isApng;
    }

    public PngChunksEntity getPngChunkEntity() {
        return pngChunkEntity;
    }
}
