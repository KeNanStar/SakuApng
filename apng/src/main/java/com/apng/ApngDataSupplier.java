package com.apng;

/**
 * @author ltf
 * @since 16/11/29, 下午12:32
 */
public interface ApngDataSupplier {

    /**
     * read int from data and move the pointer 4 byte ahead
     */
    int readInt();

    /**
     * read int from data and move the pointer 2 byte ahead
     */
    short readShort();

    /**
     * read int from data and move the pointer 1 byte ahead
     */
    byte readByte();

    /**
     * move the pointer ahead by distance bytes
     */
    void move(int distance);
}
