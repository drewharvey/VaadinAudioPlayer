package com.vaadin.addon.audio.server.encoders;

import java.nio.ByteBuffer;

import com.vaadin.addon.audio.server.Encoder;
import com.vaadin.addon.audio.shared.PCMFormat;
import com.vaadin.addon.audio.shared.util.WaveUtil;

/**
 * PCM-to-MSWave encoder.
 */
public class WaveEncoder extends Encoder {

	private PCMFormat outputFormat;
	
	public WaveEncoder() {
		this(null);
	}
	
	public WaveEncoder(PCMFormat outputFormat) {
		this.outputFormat = outputFormat;
	}
	
	@Override
	public byte[] encode(int from_offset, int length) {
		
		PCMFormat infmt = getInputFormat();

		// TODO: allow different input and output formats - we currently do no conversion
		//PCMFormat outfmt = outputFormat == null ? infmt : outputFormat;
		PCMFormat outfmt = infmt;
		
		int dataLength = infmt.getSampleSize() * length;
		
		ByteBuffer in = getInputBuffer();
		ByteBuffer out = ByteBuffer.allocate(dataLength + WaveUtil.getDataStartOffset(null));
		
		out.put(WaveUtil.generateHeader(outfmt, length));
		out.put(in.array());
		
		return out.array();
		
	}

}
