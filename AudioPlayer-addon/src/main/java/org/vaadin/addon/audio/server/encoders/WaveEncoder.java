package org.vaadin.addon.audio.server.encoders;

import java.nio.ByteBuffer;

import org.vaadin.addon.audio.server.Encoder;
import org.vaadin.addon.audio.server.util.WaveUtil;
import org.vaadin.addon.audio.shared.PCMFormat;
import org.vaadin.addon.audio.shared.util.Log;

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
		
		Log.message(this, "writing samples from offset " + from_offset + " to "  + (from_offset + length));
		
		PCMFormat infmt = getInputFormat();

		// TODO: allow different input and output formats - we currently do no conversion
		//PCMFormat outfmt = outputFormat == null ? infmt : outputFormat;
		PCMFormat outfmt = infmt;
		
		int byteOffset = infmt.getSampleSize() * from_offset;
		int dataLength = infmt.getSampleSize() * length;
		
		Log.message(this, "data length is " + dataLength);
		
		ByteBuffer in = getInputBuffer();
		int dataStartOffset = 44;  // this is NOT WaveUtil.getDataStartOffset(getInputBuffer());
		ByteBuffer out = ByteBuffer.allocate(dataLength + dataStartOffset);
		
		out.position(0);
		out.put(WaveUtil.generateHeader(outfmt, length));
		if (byteOffset < in.limit()) {
			in.position(byteOffset);
			in.get(out.array(), out.position(), Math.min(in.remaining(),dataLength));
		}
		
		return out.array();
		
	}

}
