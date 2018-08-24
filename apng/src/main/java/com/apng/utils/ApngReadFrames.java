package com.apng.utils;

import ar.com.hjg.pngj.*;
import ar.com.hjg.pngj.chunks.*;

import java.io.*;
import java.util.*;

/**
 * @author xing.hu@renren-inc.com
 * @since 2016/12/11, 18:25
 *  读取Apng扩展类
 *  方案1:直接读取Apng的每一帧序列,然后存到本地,退出直播间时再删除;
 *  方案2:在需要用到Apng序列时才读取(在刚开始读取Apng序列信息时耗时很久,如果将所有序列的Bitmap存到临时变量时会出现OOM)。
 */
public class ApngReadFrames {

    static class PngReaderBuffered extends PngReader {
        private File orig;
        //解压Apng后的文件路径
        private String mWorkingPath;

        public PngReaderBuffered(File file) {
            super(file);
            this.orig = file;
        }

        public PngReaderBuffered(File file, String workingPath) {
            super(file);
            this.orig = file;
            this.mWorkingPath = workingPath;
        }

        FileOutputStream fo = null;
        File dest;
        ImageInfo frameInfo;
        int frameIndex = -1;

        @Override
        protected ChunkSeqReaderPng createChunkSeqReader() {
            return new ChunkSeqReaderPng(false) {
                @Override
                public boolean shouldSkipContent(int len, String id) {
                    return false; // we dont skip anything!
                }

                @Override
                protected boolean isIdatKind(String id) {
                    return false; // dont treat idat as special, jsut buffer it as is
                }

                @Override
                protected void postProcessChunk(ChunkReader chunkR) {
                    super.postProcessChunk(chunkR);
                    try {
                        String id = chunkR.getChunkRaw().id;
                        PngChunk lastChunk = chunksList.getChunks().get(chunksList.getChunks().size() - 1);
                        if (id.equals(PngChunkFCTL.ID)) {
                            frameIndex++;
                            frameInfo = ((PngChunkFCTL) lastChunk).getEquivImageInfo();
                            startNewFile();
                        }
                        if (id.equals(PngChunkFDAT.ID) || id.equals(PngChunkIDAT.ID)) {
                            if (id.equals(PngChunkIDAT.ID)) {
                                // copy IDAT as is (only if file is open == if FCTL previous == if IDAT is part of the animation
                                if (fo != null)
                                    chunkR.getChunkRaw().writeChunk(fo);
                            } else {
                                // copy fDAT as IDAT, trimming the first 4 bytes
                                ChunkRaw crawi =
                                        new ChunkRaw(chunkR.getChunkRaw().len - 4, ChunkHelper.b_IDAT, true);
                                System.arraycopy(chunkR.getChunkRaw().data, 4, crawi.data, 0, crawi.data.length);
                                crawi.writeChunk(fo);
                            }
                            chunkR.getChunkRaw().data = null; // be kind, release memory
                        }
                        if (id.equals(PngChunkIEND.ID)) {
                            if (fo != null)
                                endFile(); // end last file
                        }
                    } catch (Exception e) {
                        throw new PngjException(e);
                    }
                }
            };
        }

        private void startNewFile() throws Exception {
            if (fo != null) endFile();
            dest = createOutputName();
            fo = new FileOutputStream(dest);
            fo.write(PngHelperInternal.getPngIdSignature());
            PngChunkIHDR ihdr = new PngChunkIHDR(frameInfo);
            ihdr.createRawChunk().writeChunk(fo);

            for (PngChunk chunk : getChunksList(false).getChunks()) {// copy all except actl and fctl, until IDAT
                String id = chunk.id;

                if (id.equals(PngChunkIHDR.ID) || id.equals(PngChunkFCTL.ID) || id.equals(PngChunkACTL.ID)) {
                    continue;
                }

                if (id.equals(PngChunkIDAT.ID)) {
                    break;
                }

                chunk.getRaw().writeChunk(fo);
            }
        }

        private void endFile() throws IOException {
            new PngChunkIEND(null).createRawChunk().writeChunk(fo);
            fo.close();
            fo = null;
        }

        private File createOutputName() {
            return new File(mWorkingPath, getFileName(orig, frameIndex));
        }
    }

    /**
     * Get a formatted file name for a PNG file, which is extracted from the source at a specific frame index
     *
     * @param sourceFile Source file
     * @param frameIndex Position of the frame
     * @return File name
     */
    public static String getFileName(File sourceFile, int frameIndex) {
        String filename = sourceFile.getName();
        String baseName = FileUtils.getBaseName(filename);
        String extension = FileUtils.getExtension(filename);
        return String.format(Locale.ENGLISH, "%s_%03d.%s", baseName, frameIndex, extension);
    }

    /**
     * Reads a APNG file and tries to split it into its frames - low level! Returns number of animation frames extracted
     */
    public static int process(final File orig, String workingPath) {
        // we extend PngReader, to have a custom behavior: load all chunks opaquely, buffering all, and react to some
        // special chnks
        PngReaderBuffered pngr = new PngReaderBuffered(orig, workingPath);
        pngr.end(); // read till end - this consumes all the input stream and does all!
        return pngr.frameIndex + 1;
    }




}
