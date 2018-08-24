package com.apng;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * Png Stream Constructor to make png stream from apng frame
 *
 * @author ltf
 * @since 16/11/28, 上午8:20
 */
public class PngStream extends InputStream {
    // fast data
    public static final byte[] PNG_SIG_DAT = {-119, 80, 78, 71, 13, 10, 26, 10};
    public static final int PNG_SIG_LEN = PNG_SIG_DAT.length;
    public static final byte[] PNG_IEND_DAT = {0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
    public static final int PNG_IEND_DAT_LEN = PNG_IEND_DAT.length;
    public static final byte[] NODATA = {};

    public static final int IHDR_LEN = 25;
    public static final int IHDR_WIDTH_OFF = PNG_SIG_LEN + 8;  // width's offset in IHDR
    public static final int IHDR_HEIGHT_OFF = IHDR_WIDTH_OFF + 4;
    public static final int IHDR_CRC_OFF = PNG_SIG_LEN + IHDR_LEN - 4;

    private byte[] mHeadData = new byte[PNG_SIG_LEN + IHDR_LEN]; // cached PNG_SIG_VER and IHDR and PLTE(optional) data
    private int mHeadDataLen = PNG_SIG_LEN + IHDR_LEN;
    /**
     * block infos chain for manage the head data
     */
    private BlockInfo mBlockInfos;

    private int mIENDOffset; // IEND's offset, that's the length of all previous sections
    private int mPos = 0;
    private int mLen = 0;

    private CRC32 mCrc = new CRC32();
    private ArrayList<ApngMmapParserChunk> mDataChunks = new ArrayList<>(3);
    private int dataChunkIndex;

    public PngStream() {
        System.arraycopy(PNG_SIG_DAT, 0, mHeadData, 0, PNG_SIG_LEN);
    }

    /**
     * set IHDR data
     */
    void setIHDR(byte[] ihdrData) {
        System.arraycopy(ihdrData, 0, mHeadData, PNG_SIG_LEN, IHDR_LEN);
    }

    /**
     * remove head Data by typeCode
     */
    void removeHeadData(final int typeCode) {
        setHeadData(typeCode, NODATA);
    }

    /**
     * set(add/update) head Data by typeCode
     */
    void setHeadData(final int typeCode, byte[] data) {
        BlockInfo block = getBlockInfo(typeCode, true);
        int delta = data.length - block.len;
        int oldHeadDataLen = mHeadDataLen;
        mHeadDataLen += delta;
        int oldNextOff = block.offset + block.len;

        byte[] src = mHeadData;
        // increase mHeadData size if needed
        if (delta > 0 && mHeadData.length < mHeadDataLen) {
            mHeadData = new byte[mHeadDataLen];
            // only copy data before current chunk
            // others will be copied in next move all follows data operation
            System.arraycopy(src, 0, mHeadData, 0, block.offset);
        }

        // new data size are different, move back/ahead all follows data
        if (delta != 0) {
            System.arraycopy(
                    src, oldNextOff,
                    mHeadData, oldNextOff + delta,
                    oldHeadDataLen - oldNextOff);
            updateNextOffsetTillEnd(block, delta);
            block.len = data.length;
        }

        System.arraycopy(data, 0, mHeadData, block.offset, data.length);

        /**
         * remove blockInfo if nodata contains
         */
        if (data.length == 0) {
            if (block == mBlockInfos) {
                mBlockInfos = null;
            } else {
                BlockInfo pre = block.pre;
                pre.next = block.next;
                if (pre.next != null) pre.next.pre = pre;
            }
        }
    }

    /**
     * update ALL FOLLOWS blocks' offset, NOT include current block's offset
     *
     * @param currentBlock current block !!!NonNull !!!
     * @param delta        delta plus to current offset
     */
    private void updateNextOffsetTillEnd(BlockInfo currentBlock, int delta) {
        BlockInfo block = currentBlock.next;
        while (block != null) {
            block.offset += delta;
            block = block.next;
        }
    }

    /**
     * locate blockInfo by typeCode
     *
     * @param typeCode          typeCode
     * @param createIfNotExists create one if not exists
     * @return blockInfo, or null if not exists and not create new
     */
    private BlockInfo getBlockInfo(final int typeCode, boolean createIfNotExists) {
        BlockInfo block = mBlockInfos;
        BlockInfo last = mBlockInfos;
        while (block != null) {
            if (block.typeCode == typeCode) {
                return block;
            }
            if (block.next == null) {
                last = block;
            }
            block = block.next;
        }

        if (createIfNotExists) {
            block = new BlockInfo(typeCode);
            if (last != null) {
                block.pre = last;
                last.next = block;
                block.offset = last.offset + last.len;
            } else {
                mBlockInfos = block;
                block.offset = PNG_SIG_LEN + IHDR_LEN;
            }
            return block;
        }
        return null;
    }

    /**
     * 3rd: set data chunk each time when use this to construct a frame png stream
     */
    void addDataChunk(ApngMmapParserChunk dataChunk) {
        this.mDataChunks.add(dataChunk);
        mIENDOffset = mHeadDataLen;
        for (ApngMmapParserChunk chunk : mDataChunks)
            mIENDOffset += chunk.getStreamLen();
        mLen = mIENDOffset + PNG_IEND_DAT_LEN;
    }

    /**
     * clear data trunks and reset dataChunkIndex, mPos
     */
    void clearDataChunks() {
        mDataChunks.clear();
        dataChunkIndex = 0;
        mPos = 0;
    }

    /**
     * update IHDR width and height and chunk crc
     */
    void updateIHDR(int width, int height) {
        intToArray(width, mHeadData, IHDR_WIDTH_OFF);
        intToArray(height, mHeadData, IHDR_HEIGHT_OFF);
        mCrc.reset();
        mCrc.update(mHeadData, IHDR_WIDTH_OFF - 4, IHDR_CRC_OFF - IHDR_WIDTH_OFF + 4);
        intToArray((int) mCrc.getValue(), mHeadData, IHDR_CRC_OFF);
    }

    /**
     * reset read position to head, dataChunkIndex to 0
     */
    void resetPos() {
        dataChunkIndex = 0;
        mPos = 0;
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException("not support read by byte because of low performance");
    }

    // this function is optimized for performance, so it's maybe hard to read and control
    @Override
    public int read(byte[] buffer, final int byteOffset, final int byteCount) throws IOException {
        int size = mLen - mPos;
        if (size <= 0) return 0;
        size = size > byteCount ? byteCount : size;
        int dstEndOffset = byteOffset + size;

        for (int want = size; want > 0; ) {
            int count;
            if (mPos < mHeadDataLen) {
                // read from head data section
                count = mHeadDataLen - mPos;
                count = want < count ? want : count;
                System.arraycopy(mHeadData, mPos, buffer, dstEndOffset - want, count);
            } else if (mPos >= mIENDOffset) {
                // read from IEND data section
                count = mLen - mPos;
                count = want < count ? want : count;
                System.arraycopy(PNG_IEND_DAT, mPos - mIENDOffset, buffer, dstEndOffset - want, count);
            } else {
                // data trunk header( length + type_code)
                count = mIENDOffset - mPos;
                count = want < count ? want : count;
                int readed = mDataChunks.get(dataChunkIndex).readAsStream(buffer, dstEndOffset - want, count);
                // switch read from next data chunk
                if (readed < count) {
                    dataChunkIndex++;
                    count = readed;
                }
            }
            want -= count;
            mPos += count;
        }
        return size;
    }

    /**
     * finally generate data crc value
     */
    public static void intToArray(int val, byte[] arr, int offset) {
        arr[offset] = (byte) (val >> 24 & 0xFF);
        arr[offset + 1] = (byte) (val >> 16 & 0xFF);
        arr[offset + 2] = (byte) (val >> 8 & 0xFF);
        arr[offset + 3] = (byte) (val & 0xFF);
    }

    /**
     * Head data block info
     */
    private static class BlockInfo {
        private int typeCode;
        int offset;
        int len;
        BlockInfo pre;
        BlockInfo next;

        public BlockInfo(int typeCode) {
            this.typeCode = typeCode;
        }
    }
}
