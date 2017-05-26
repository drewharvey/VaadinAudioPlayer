package com.vaadin.addon.audio.shared;

import java.io.Serializable;

/**
 * Format parameters for PCM data
 */
@SuppressWarnings("serial")
public class PCMFormat implements Serializable {

	private int numChannels;   // 1: mono, 2: stereo
	private int sampleRate;    // samples (values) per second of audio
	private int byteRate;      // sampleRate * numChannels * (bitsPerSample / 8)
	private int blockAlign;    // numChannels * (bitsPerSample / 8)
	private int bitsPerSample; // usually either 8 or 16

	public PCMFormat(int numChannels, int sampleRate, int bitsPerSample) {
		this.numChannels = numChannels;
		this.sampleRate = sampleRate;
		this.bitsPerSample = bitsPerSample;
		
		this.blockAlign = numChannels * (bitsPerSample / 8);
		this.byteRate = blockAlign * sampleRate;
	}
	
	public int getNumChannels() {
		return numChannels;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public int getByteRate() {
		return byteRate;
	}

	public int getBlockAlign() {
		return blockAlign;
	}
	
	/**
	 * Get size of a single sample in bytes
	 */
	public int getSampleSize() {
		return blockAlign;
	}

	public int getBitsPerSample() {
		return bitsPerSample;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("numChannels: ").append(numChannels).append("\n\r");
		sb.append("sampleRate: ").append(sampleRate).append("\n\r");
		sb.append("byteRate: ").append(byteRate).append("\n\r");
		sb.append("blockAlign: ").append(blockAlign).append("\n\r");
		sb.append("bitsPerSample: ").append(bitsPerSample).append("\n\r");
		return sb.toString();
	}

}

