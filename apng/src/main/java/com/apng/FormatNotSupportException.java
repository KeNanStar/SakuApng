package com.apng;

/**
 * Not Support Apng File Format Exception
 *
 * @author ltf
 * @since 16/11/26, 下午4:16
 */
public class FormatNotSupportException extends Exception {

    public FormatNotSupportException(String detailMessage) {
        super(detailMessage);
    }
}
