package com.apng;

import java.io.*;

/**
 * Apng Frame Data
 *
 *
 * @since 2026/3/17, 下午1:15
 */
public class ApngFrame extends ApngFCTLChunk {

    InputStream imageStream;

    public InputStream getImageStream() {
        return imageStream;
    }
}
