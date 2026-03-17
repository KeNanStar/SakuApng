package com.apng;

/**
 * Not Support Apng File Format Exception
 *
 *
 * @since 2026/3/17, 下午4:16
 */
public class FormatNotSupportException extends Exception {

    public FormatNotSupportException(String detailMessage) {
        super(detailMessage);
    }
}
