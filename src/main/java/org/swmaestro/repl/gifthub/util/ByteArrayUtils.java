package org.swmaestro.repl.gifthub.util;

import java.nio.ByteBuffer;

public class ByteArrayUtils {
	public static String byteArrayToString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte abyte : bytes) {
			sb.append(abyte);
			sb.append(" ");
		}
		return sb.toString();
	}

	public static byte[] stringToByteArray(String byteString) {
		String[] split = byteString.split("\\s");
		ByteBuffer buffer = ByteBuffer.allocate(split.length);
		for (String s : split) {
			buffer.put((byte)Integer.parseInt(s));
		}
		return buffer.array();
	}
}
