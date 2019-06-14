package org.vaadin.addon.audio.server;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.vaadin.addon.audio.server.state.StreamState;
import org.vaadin.addon.audio.server.state.StreamStateCallback;
import org.vaadin.addon.audio.shared.ChunkDescriptor;
import org.vaadin.addon.audio.shared.PCMFormat;

/**
 * Server-side datastream class
 */
public class Stream {

	private static final int CHUNK_LENGTH_MILLIS_DEFAULT = 5000;
	private static final int CHUNK_OVERLAP_MILLIS = 50;

	public static interface Callback {
		public void onComplete(String encodedData);
	}

	private static class ChunkRequest {
		ChunkDescriptor chunk;
		Callback callback;

		ChunkRequest(ChunkDescriptor d, Callback c) {
			this.chunk = d;
			this.callback = c;
		}
	}

	private List<StreamStateCallback> stateCallbacks = new ArrayList<>();
	private List<ChunkDescriptor> chunks = new ArrayList<ChunkDescriptor>();
	private volatile Queue<ChunkRequest> requestQueue = new ArrayDeque<>();
	private volatile Thread worker = null;

	private PCMFormat format = null;
	private ByteBuffer buffer = null;
	private Encoder encoder = null;
	private StreamState streamState = StreamState.IDLE;

	// TODO: pass in chunk length and overlap in an optional constructor
	private int chunkLength;
	private int chunkOverlapLength = CHUNK_OVERLAP_MILLIS;

	private boolean compression = false;
	private int sampleCount = 0;
	private int duration = 0;

	/**
	 * Create an audio stream. Performs on-the-fly audio encoding on a
	 * chunk-by-chunk basis.
	 *
	 * @param pcmBuffer
	 *            Buffer containing PCM data - data should start at offset 0
	 * @param format
	 *            Object describing PCM data format
	 * @param encoder
	 *            Data encoder to use. {@link WaveEncoder} forwards PCM data.
	 */
	public Stream(ByteBuffer pcmBuffer, PCMFormat format, Encoder encoder) {
		this(pcmBuffer, format, encoder, CHUNK_LENGTH_MILLIS_DEFAULT);
	}

	/**
	 * Create an audio stream. Performs on-the-fly audio encoding on a
	 * chunk-by-chunk basis.
	 * 
	 * @param pcmBuffer
	 *            Buffer containing PCM data - data should start at offset 0
	 * @param format
	 *            Object describing PCM data format
	 * @param encoder
	 *            Data encoder to use. {@link WaveEncoder} forwards PCM data.
	 * @param millisPerChunk
	 * 			  Milliseconds per audio chunk
	 */
	public Stream(ByteBuffer pcmBuffer, PCMFormat format, Encoder encoder, int millisPerChunk) {
		this.buffer = pcmBuffer;
		this.format = format;
		this.encoder = encoder;
		this.chunkLength = millisPerChunk;
		encoder.setInput(pcmBuffer, format);

		int buffersize = pcmBuffer.capacity();
		int samples = sampleCount = buffersize / format.getSampleSize();
		int samplesPerMillis = format.getSampleRate() / 1000;
		duration = samples / samplesPerMillis;

		int chunkSampleSize = samplesPerMillis * chunkLength;
		int chunkOverlapSampleSize = samplesPerMillis * chunkOverlapLength;

		// Create chunks
		{
			int from_sample, to_sample;
			int chunk = 0;

			do {
				// Calculate read area
				from_sample = ((chunk * chunkLength) - chunkOverlapLength) * samplesPerMillis;
				from_sample = Math.max(0, from_sample);
				to_sample = (((chunk + 1) * chunkLength) - 1 + chunkOverlapLength) * samplesPerMillis;
				to_sample = Math.min(samples, to_sample);

				// Calculate lead in/out duration
				int time_lead_in = Math.max((chunk * chunkLength) - (from_sample / samplesPerMillis), 0);
				int time_lead_out = Math.max((to_sample / samplesPerMillis) - ((chunk + 1) * chunkLength), 0);

				// Calculate time
				int time_start_offset = (from_sample / samplesPerMillis) + time_lead_in;
				int time_end_offset = (to_sample / samplesPerMillis) - time_lead_out;
				
				if(time_lead_out < chunkOverlapLength) {
					time_end_offset = (to_sample / samplesPerMillis);
					time_lead_out = 0;
				}
				
				// Create descriptor
				ChunkDescriptor cd = new ChunkDescriptor();
				cd.setId(chunk);
				cd.setStartSampleOffset(from_sample);
				cd.setEndSampleOffset(to_sample);
				cd.setLeadInDuration(time_lead_in);
				cd.setLeadOutDuration(time_lead_out);
				cd.setStartTimeOffset(time_start_offset);
				cd.setEndTimeOffset(time_end_offset);
				cd.setOverlapTime(chunkOverlapLength);

				// Add descriptor to list
				chunks.add(cd);

				// Advance
				chunk++;
			} while (to_sample < samples);
		}

		// Create first chunk
		{
			ChunkDescriptor cd = new ChunkDescriptor();
			cd.setId(0);
			cd.setStartSampleOffset(0);
			cd.setEndSampleOffset(Math.min(chunkSampleSize + chunkOverlapSampleSize, samples));
			cd.setLeadInDuration(0);
			cd.setLeadOutDuration(chunkOverlapSampleSize);
			cd.setStartTimeOffset(0);
			cd.setEndTimeOffset(chunkLength);
		}
	}

	public void addStateChangeListener(StreamStateCallback cb) {
		stateCallbacks.add(cb);
	}

	public void removeStateChangeListener(StreamStateCallback cb) {
		stateCallbacks.remove(cb);
	}

	private void setStreamState(StreamState s) {
		if (streamState == s) {
			return;
		}
		streamState = s;
		for (StreamStateCallback cb : stateCallbacks) {
			cb.onStateChanged(s);
		}
	}

	public StreamState getState() {
		return streamState;
	}

	public int getChunkLength() {
		return chunkLength;
	}

	public List<ChunkDescriptor> getChunks() {
		return chunks;
	}

	public ByteBuffer getInputBuffer() {
		return buffer;
	}

	public PCMFormat getInputFormat() {
		return format;
	}

	/**
	 * Enable or disable datastream compression. If compression is enabled, data
	 * retrieved from this stream is run through a fast zlib compression routine
	 * before being base64 encoded for transport.
	 * 
	 * If you're using the NullEncoder, compression is encouraged. For any other
	 * encoding scheme, use of compression should be decided on a case-by-case
	 * basis.
	 * 
	 * @param enable
	 *            true to enable, false to disable
	 */
	public void setCompression(boolean enable) {
		compression = enable;
	}

	/**
	 * 
	 * 
	 * @return true if compression is being used
	 */
	public boolean isCompressionEnabled() {
		return compression;
	}

	/**
	 * Get data for a chunk of audio as an encoded string. This method is used
	 * to facilitate audio transport.
	 * 
	 * The logic runs in its own thread, and calls cb when complete.
	 */
	public void getChunkData(ChunkDescriptor chunk, Callback cb) {
		requestQueue.add(new ChunkRequest(chunk, cb));
		serviceChunkRequests();
	}

	private void serviceChunkRequests() {
		if (worker != null) {
			return;
		}

		ChunkRequest request = requestQueue.remove();

		final ChunkDescriptor chunk = request.chunk;
		final Callback callback = request.callback;

		worker = new Thread(new Runnable() {
			@Override
			public void run() {
				setStreamState(StreamState.READING);
				int startOffset = chunk.getStartSampleOffset();
				int endOffset = chunk.getEndSampleOffset();
				int length = endOffset - startOffset;

				setStreamState(StreamState.ENCODING);
				byte[] bytes = encoder.encode(startOffset, length);

				if (compression) {
					setStreamState(StreamState.COMPRESSING);
					bytes = StreamDataEncoder.compress(bytes);
				}

				setStreamState(StreamState.SERIALIZING);
				String serialized = StreamDataEncoder.encode(bytes);
				callback.onComplete(serialized);

				// We're done, kill the worker
				worker = null;
				if (requestQueue.isEmpty()) {
					setStreamState(StreamState.IDLE);
				} else {
					setStreamState(StreamState.READING);
					serviceChunkRequests();
				}
			}
		});
		worker.run();
	}

	/**
	 * Find the chunk descriptor that contains the requested timestamp
	 * 
	 * @param position_millis
	 * @return
	 */
	public ChunkDescriptor findChunk(int position_millis) {
		for(ChunkDescriptor c : chunks) {
			if(c.getStartTimeOffset() >= position_millis && c.getEndTimeOffset() <= position_millis) {
				return c;
			}
		}
		return null;
	}

	public ChunkDescriptor getChunkById(int chunkID) {
		// TODO: actually SEARCH for the chunk, supposing the store isn't 1:1!
		// We may otherwise get a really nasty NPE situation!
		ChunkDescriptor c = chunks.get(chunkID);
		return c;
	}

	/**
	 * Get number of samples in stream
	 * 
	 * @return
	 */
	public int getSampleCount() {
		return sampleCount;
	}

	/**
	 * Get total duration of stream in milliseconds
	 * 
	 * @return
	 */
	public int getDuration() {
		return duration;
	}
	
	public String getDurationString() {
		int days = (duration / 1000) / 60 / 60 / 24;
		int hours = (duration / 1000 / 60 / 60) % 24;
		int minutes = (duration / 1000 / 60) % 60;
		int seconds = (duration / 1000) % 60;
		int millis = duration % 1000;
		
		String text = "";
		if(days > 0) {
			text += days + "d";
		}
		if(hours > 0) {
			if(!text.isEmpty()) text += ", ";
			text += hours + "h";
		}
		if(minutes > 0) {
			if(!text.isEmpty()) text += ", ";
			text += minutes + "m";
		}
		if(seconds > 0) {
			if(!text.isEmpty()) text += ", ";
			text += seconds + "s";
		}
		if(millis > 0) {
			if(!text.isEmpty()) text += ", ";
			text += millis + "ms";
		}
		
		return text;
	}

}
