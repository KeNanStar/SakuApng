package com.apng.utils;


import java.io.*;
import java.security.*;

public final class Md5 {

	public static String getFileMD5String(File file) throws IOException {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
			InputStream fis;
			fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int numRead = 0;
			while ((numRead = fis.read(buffer)) > 0) {
				md5.update(buffer, 0, numRead);
			}
			fis.close();
			return bufferToHex(md5.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String toMD5(String s) {
		if (s != null) {
			try {
				byte[] bs = s.getBytes("UTF-8");
				return encrypt(bs);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String md5Hex(String s) {
		if (s != null) {
			try {
				byte[] bs = s.getBytes("UTF-8");
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				md5.update(bs);
				byte[] md5Bytes = md5.digest();
				return bufferToHex(md5Bytes);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private synchronized static String encrypt(byte[] obj) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(obj);
			byte[] bs = md5.digest();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bs.length; i++) {
				sb.append(Integer.toHexString((0x000000ff & bs[i]) | 0xffffff00).substring(6));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4]; // 取字节中高 4 位的数字转换,
												// >>>为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
		char c1 = hexDigits[bt & 0xf]; // 取字节中低 4 位的数字转换
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

}
