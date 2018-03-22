package org.vaadin.addon.audio.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.Deflater;

/**
 * Functions for handling encoding of data to be sent to client
 */
public final class StreamDataEncoder {

	public static String encode(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	public static byte[] compress(byte[] bytes) {
		Deflater deflater = new Deflater();
		deflater.setInput(bytes);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytes.length);
		deflater.finish();
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer); // returns the generated
													// code... index
			outputStream.write(buffer, 0, count);
		}
		try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] output = outputStream.toByteArray();
		return output;
	}

}
