package com.apng;

import java.io.*;

/**
 * Created by Shark0 on 2016/9/13.
 */
public class ByteUtil {
    public static int indexOf(byte[] bytes1, byte[] bytes2) {
        for (int i = 0; i < bytes1.length - bytes2.length + 1; i++) {
            boolean found = true;
            for (int j = 0; j < bytes2.length; j++) {
                if (bytes1[i + j] != bytes2[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    public static byte[] subBytes(byte[] bytes, int startIndex, int endIndex) {
        byte[] subBytes = new byte[endIndex - startIndex];
        for(int i = startIndex; i < endIndex; i ++) {
            subBytes[i - startIndex] = bytes[i];
        }
        return subBytes;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder(bytes.length * 2);
        for(byte b: bytes) {
            String hex = String.format("%02x", b & 0xff);
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }

    public static int bytesToInt(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    public static byte[] intToBytes(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value };
    }

    public static int bytesToshort(byte[] bytes) {
        return bytes[0] << 8 | (bytes[1] & 0xFF);
    }

    public static byte[] shortToBytes(int value) {
        return new byte[] {
                (byte)(value >> 8),
                (byte)value };
    }

    public static byte[] combineBytes(byte[] bytes1, byte[] bytes2) {
        byte[] bytes = new byte[bytes1.length + bytes2.length];
        for(int i = 0; i < bytes1.length; i ++) {
            bytes[i] = bytes1[i];
        }
        for(int i = 0; i < bytes2.length; i ++) {
            bytes[i + bytes1.length] = bytes2[i];
        }
        return bytes;
    }

    public static byte[] copyBytes(byte[] bytes) {
        byte[] newBytes = new byte[bytes.length];
        for(int i = 0; i < bytes.length; i ++) {
            newBytes[i] = bytes[i];
        }
        return newBytes;
    }


    public static byte[] loadBytesFromFile(File file) {
        BufferedInputStream inputStream = null;
        int size = (int) file.length();
        byte[] imageBytes = new byte[size];
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            inputStream.read(imageBytes, 0, imageBytes.length);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return imageBytes;
    }
}
