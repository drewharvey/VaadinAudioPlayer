package com.vaadin.addon.audio.shared.util;

import java.nio.ByteBuffer;

import com.vaadin.addon.audio.shared.PCMFormat;

/**
 * Utilities for working with WAVE files
 */
public final class WaveUtil {

	/**
	 * Read Wave header to get data format
	 * 
	 * @param waveFileBytes
	 *            bytebuffer containing data of wave file
	 * @return a PCMFormat object
	 */
	public static PCMFormat getDataFormat(ByteBuffer waveFileBytes) {
		PCMFormat fmt = null;

		return fmt;
	}

	/**
	 * Get byte offset of the data start. For now we assume there's always a 44
	 * byte wave header...
	 * 
	 * @param waveFileBytes
	 *            bytebuffer containing a wave file or chunk
	 * @return number of bytes until start of PCM data block
	 */
	public static int getDataStartOffset(ByteBuffer waveFileBytes) {
		return 44;
	}

	/**
	 * Get the length of the PCM data block
	 * 
	 * @param waveFileBytes
	 *            bytebuffer containing a wave file or chunk
	 * @return number of bytes in PCM data block
	 */
	public static int getDataLength(ByteBuffer waveFileBytes) {
		return Endian.readLE(waveFileBytes, 40, 4);
	}

	/**
	 * Generate a header for a WAVE file
	 * 
	 * @param format
	 *            PCM format data
	 * @param numSamples
	 *            number of samples in data (data size is inferred from this)
	 * @return the bytes of the header, to which the PCM data can be appended
	 */
	public static byte[] generateHeader(PCMFormat format, int numSamples) {
		
		// Refer to http://soundfile.sapp.org/doc/WaveFormat/
		
		byte[] header = new byte[44]; // MS Wave header is exactly 44 bytes long
		ByteBuffer buf = ByteBuffer.wrap(header);

		// RIFF header bytes
		buf.position(0);
		buf.put((byte) 'R');
		buf.put((byte) 'I');
		buf.put((byte) 'F');
		buf.put((byte) 'F');

		// Length takes bytes 4-8

		buf.position(8);
		buf.put((byte) 'W');
		buf.put((byte) 'A');
		buf.put((byte) 'V');
		buf.put((byte) 'E');

		// format subchunk

		buf.position(12);
		buf.put((byte) 'f');
		buf.put((byte) 'm');
		buf.put((byte) 't');
		buf.put((byte) ' ');

		// subchunk size
		Endian.writeLE(16, buf, 16, 4);

		// audio format (1 is for linear quantization PCM data)
		Endian.writeLE(1, buf, 20, 2);

		// number of channels
		Endian.writeLE(format.getNumChannels(), buf, 22, 2);

		// sample rate
		Endian.writeLE(format.getSampleRate(), buf, 24, 4);

		// byte rate
		Endian.writeLE(format.getByteRate(), buf, 28, 4);

		// block align
		Endian.writeLE(format.getBlockAlign(), buf, 32, 2);

		// bits per sample
		Endian.writeLE(format.getBitsPerSample(), buf, 34, 2);

		// data subchunk id
		buf.position(36);
		buf.put((byte) 'd');
		buf.put((byte) 'a');
		buf.put((byte) 't');
		buf.put((byte) 'a');

		// data size
		int dataSize = numSamples * format.getSampleSize();
		Endian.writeLE(dataSize, buf, 40, 4);
		
		// write the initial chunk length (see docs)
		Endian.writeLE(36 + dataSize, buf, 4, 4);

		return buf.array();
	}

}
