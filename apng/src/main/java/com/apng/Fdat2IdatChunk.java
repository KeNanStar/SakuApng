package com.apng;

import java.io.*;
import java.util.zip.*;

import static com.apng.PngStream.*;

/**
 * convert fdAT chunk to IDAT chunk stream
 *
 * @author ltf
 * @since 16/12/2, 下午3:56
 */
public class Fdat2IdatChunk extends ApngMmapParserChunk {
    private int mDataSigOff; // signature "fdAT" 's  offset
    private int mDataSigdEnd; // d's end(or A's position), in signature "fdAT"
    private int mDataCrcOff; // data CRC's offset
    private int mFDATSeqOff; // offset of "fdAT"'s sequence_number, only available when mIsFDAT = true
    private int mFDATSeqEnd; // end of "fdAT"'s sequence_number, only available when mIsFDAT = true
    private CRC32 mCrc = new CRC32();
    private boolean mCalCrc = true; // need to compute/calculate CRC
    private byte[] mCrcVal = new byte[4]; // used for fdAT recompute crc
    private byte[] mFDATLength = new byte[4];// used for fdAT recompute length

    Fdat2IdatChunk(ApngMmapParserChunk copyFromChunk) {
        super(copyFromChunk);
        init();
    }

    private void init() {
        mDataSigOff = offset + 4;
        mDataSigdEnd = offset + 6;
        mFDATSeqOff = offset + 8;
        mFDATSeqEnd = offset + 12;
        mDataCrcOff = nextOffset - 4;
        intToArray(length - 4, mFDATLength, 0);
    }

    @Override
    int getStreamLen() {
        return length + 8; // FDAT covert to IDAT will lost it's 4byte seq_num
    }

    // this function is optimized for performance, so it's maybe hard to read and control
    @Override
    int readAsStream(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        int pos = mBuf.position();
        int size = nextOffset - pos;
        if (pos < mFDATSeqEnd) {
            int removed = mFDATSeqEnd - pos;
            size -= removed > 4 ? 4 : removed;
        }
        if (size <= 0) return 0;
        size = size > byteCount ? byteCount : size;
        int dstEndOffset = byteOffset + size;

        for (int want = size; want > 0; ) {
            int count;
            if (pos >= mDataCrcOff) {
                // read DATA CRC
                count = nextOffset - pos;
                count = want < count ? want : count;

                // CRC only calculated for one times
                if (mCalCrc) {
                    intToArray((int) mCrc.getValue(), mCrcVal, 0);
                    mCalCrc = false;
                }
                System.arraycopy(mCrcVal, 4 - (nextOffset - pos), buffer, dstEndOffset - want, count);
                move(count);
            } else if (pos >= mFDATSeqEnd) {
                // all raw data don't need modify
                count = mDataCrcOff - pos;
                count = want < count ? want : count;
                mBuf.get(buffer, dstEndOffset - want, count);
                // compute crc for fdAT
                if (mCalCrc) mCrc.update(buffer, dstEndOffset - want, count);
                //Log.d("ApngSurfaceView", String.format("r: %d, crc: %d", read - pre, System.currentTimeMillis() - read));
            } else if (pos >= mFDATSeqOff) {
                // seq_num chunk will be skipped
                count = mFDATSeqEnd - pos;
                count = want < count ? want : count;
                want += count;
                move(count);
            } else {
                // data trunk header( length + type_code)
                count = mFDATSeqOff - pos;
                count = want < count ? want : count;
                mBuf.get(buffer, dstEndOffset - want, count);

                // update fdAT to IDAT
                if (pos < mDataSigdEnd) {
                    int dOff = mDataSigdEnd - pos - 2;
                    int cover = count - dOff;
                    if (cover >= 2) {
                        if (count > 1) buffer[dstEndOffset - want + dOff] = 'I';
                        buffer[dstEndOffset - want + dOff + 1] = 'D';
                    } else if (cover == 1) {
                        buffer[dstEndOffset - want + dOff] = 'I';
                    }
                }

                // calculate CRC
                if (pos >= mDataSigOff) {
                    if (mCalCrc) mCrc.update(buffer, dstEndOffset - want, count);
                } else {
                    // compute CRC on bytes included
                    int dOff = mDataSigOff - pos;
                    int cover = count - dOff;
                    if (mCalCrc && cover > 0) {
                        mCrc.update(buffer, dstEndOffset - want + dOff, cover);
                    }

                    // update length
                    cover = dOff < count ? dOff : count;
                    if (cover > 0) System.arraycopy(mFDATLength, 4 - dOff, buffer, dstEndOffset - want, cover);
                }
            }
            want -= count;
            pos += count;
        }
        return size;
    }
}
