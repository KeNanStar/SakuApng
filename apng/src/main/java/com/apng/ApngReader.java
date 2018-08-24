package com.apng;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import static com.apng.ApngConst.*;

/**
 * Apng加载器（从Apng文件中读取每一帧的控制块及图像）
 *
 * @author ltf
 * @since 16/11/25, 上午8:14
 */
public class ApngReader {

    /**
     * chunks should be copied to each frame
     */
    public static final int[] COPIED_TYPE_CODES = {
            CODE_iCCP,
            CODE_sRGB,
            CODE_sBIT,
            CODE_gAMA,
            CODE_cHRM,

            CODE_PLTE,

            CODE_tRNS,
            CODE_hIST,
            CODE_bKGD,
            CODE_pHYs,
            CODE_sPLT
    };

    static {
        Arrays.sort(COPIED_TYPE_CODES);
    }

    private final MappedByteBuffer mBuffer;
    private final ApngMmapParserChunk mChunk;
    private final PngStream mPngStream = new PngStream();
    private ApngACTLChunk mActlChunk;

    public ApngReader(String apngFile) throws IOException, FormatNotSupportException {
        RandomAccessFile f = new RandomAccessFile(apngFile, "r");
        mBuffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, f.length());
        f.close();
        if (mBuffer.getInt() != PNG_SIG
                && mBuffer.getInt(4) != PNG_SIG_VER
                && mBuffer.getInt(8) != CODE_IHDR) {
            throw new FormatNotSupportException("Not a png/apng file");
        }
        mChunk = new ApngMmapParserChunk(mBuffer);
        reset();
    }

    /**
     * get the acTL chunk information
     *
     * @return animation control info
     * @throws IOException
     * @throws FormatNotSupportException
     */
    public ApngACTLChunk getACTL() throws IOException, FormatNotSupportException {
        if (mActlChunk != null) return mActlChunk;
        int pos = mBuffer.position();
        try {
            ApngMmapParserChunk tmpChunk = new ApngMmapParserChunk(mBuffer);
            // locate first chunk (IHDR)
            tmpChunk.parsePrepare(8);
            tmpChunk.parse();

            // locate ACTL chunk
            while (tmpChunk.typeCode != CODE_acTL) {
                if (tmpChunk.typeCode == CODE_IEND || tmpChunk.parseNext() < 0) {
                    throw new FormatNotSupportException("No ACTL chunk founded, not an apng file. (maybe it's a png only)");
                }
            }

            handleACTL(tmpChunk);
        } finally {
            mBuffer.position(pos);
        }
        return mActlChunk;
    }

    /**
     * hanlde actl chunk
     */
    private void handleACTL(ApngMmapParserChunk chunk) throws IOException {
        if (mActlChunk == null) {
            mActlChunk = new ApngACTLChunk();
            chunk.assignTo(mActlChunk);
        }
    }

    /**
     * handle other's chunk
     */
    private void handleOtherChunk(ApngMmapParserChunk chunk) throws IOException {
        if (Arrays.binarySearch(COPIED_TYPE_CODES, chunk.typeCode) >= 0) {
            mPngStream.setHeadData(chunk.getTypeCode(), chunk.duplicateData());
        }
    }

    /**
     * get next frame control info & bitmap
     *
     * @return next frame control info, or null if no next FCTL chunk || no next IDAT/FDAT
     * @throws IOException
     */
    public ApngFrame nextFrame() throws IOException {
        // reset read pointers from previous frame's lock
        mPngStream.clearDataChunks();
        mPngStream.resetPos();
        mChunk.unlockRead();

        // locate next FCTL chunk
        boolean ihdrCopied = false;
        while (mChunk.typeCode != CODE_fcTL) {
            switch (mChunk.typeCode) {
                case CODE_IEND:
                    return null;
                case CODE_IHDR:
                    mPngStream.setIHDR(mChunk.duplicateData());
                    break;
                case CODE_acTL:
                    handleACTL(mChunk);
                    ihdrCopied = true;
                    break;
                default:
                    handleOtherChunk(mChunk);
            }
            mChunk.parseNext();
        }

        // located at FCTL chunk
        ApngFrame frame = new ApngFrame();
        mChunk.assignTo(frame);

        // locate next IDAT or fdAt chunk
        mChunk.parseNext();// first move next from current FCTL
        while (mChunk.typeCode != CODE_IDAT && mChunk.typeCode != CODE_fdAT) {
            switch (mChunk.typeCode) {
                case CODE_IEND:
                    return null;
                case CODE_IHDR:
                    mPngStream.setIHDR(mChunk.duplicateData());
                    ihdrCopied = true;
                    break;
                case CODE_acTL:
                    handleACTL(mChunk);
                    break;
                default:
                    handleOtherChunk(mChunk);
            }
            mChunk.parseNext();
        }

        // located at first IDAT or fdAT chunk
        // collect all consecutive dat chunks
        boolean needUpdateIHDR = true;
        int dataOffset = mChunk.getOffset();
        while (mChunk.typeCode == CODE_fdAT || mChunk.typeCode == CODE_IDAT) {
            if (needUpdateIHDR && (!ihdrCopied || mChunk.typeCode == CODE_fdAT)) {
                mPngStream.updateIHDR(frame.getWidth(), frame.getHeight());
                needUpdateIHDR = false;
            }

            if (mChunk.typeCode == CODE_fdAT) {
                mPngStream.addDataChunk(new Fdat2IdatChunk(mChunk));
            } else {
                mPngStream.addDataChunk(new ApngMmapParserChunk(mChunk));
            }
            mChunk.parseNext();
        }

        // lock position for this frame's image as OutputStream
        mChunk.lockRead(dataOffset);
        frame.imageStream = mPngStream;
        return frame;
    }

    /**
     * locate to the first chunk, and parse it
     */
    public void reset() {
        mChunk.parsePrepare(8);
        mChunk.parse();
    }
}
