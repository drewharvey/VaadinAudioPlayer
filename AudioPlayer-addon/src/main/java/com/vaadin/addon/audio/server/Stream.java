package com.vaadin.addon.audio.server;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.vaadin.addon.audio.shared.ChunkDescriptor;
import com.vaadin.addon.audio.shared.PCMFormat;

/**
 * Server-side datastream class
 */
public class Stream {

	public static interface Callback {
		public void onComplete(String encodedData);
	}
	
	public static enum StreamState {
		IDLE, READING, ENCODING, COMPRESSING, SERIALIZING
	}
	
	// TODO: use proper event system
	public static interface StreamStateCallback {
		public void onStateChanged(StreamState newState);
	}
	
	private static class ChunkRequest {
		ChunkDescriptor chunk;
		Callback callback;
		
		ChunkRequest(ChunkDescriptor d, Callback c) {
			chunk = d;
			callback = c;
		}
	}
	
	private List<StreamStateCallback> stateCallbacks = new ArrayList<>();
	private List<ChunkDescriptor> chunks = new ArrayList<ChunkDescriptor>();
	private volatile Queue<ChunkRequest> requestQueue = new ArrayDeque<>();
	private volatile Thread worker = null;
	
	private ByteBuffer buffer = null;
	private Encoder encoder = null;
	private StreamState streamState = StreamState.IDLE;
	 
	private boolean compression = false;
	private int chunkCount = 0;
	private int duration = 0;
	
	/**
	 * Create an audio stream. Performs on-the-fly audio encoding
	 * on a chunk-by-chunk basis.
	 * 
	 * @param pcmBuffer Buffer containing PCM data - data should start at offset 0
	 * @param format Object describing PCM data format
	 * @param encoder Data encoder to use. {@link WaveEncoder} forwards PCM data.
	 */
	public Stream(ByteBuffer pcmBuffer, PCMFormat format, Encoder encoder) {
		this.buffer = pcmBuffer;
		this.encoder = encoder;
		encoder.setInput(pcmBuffer, format);
		
		// TODO: create chunk descriptor list
	}

	public void addStateChangeListener(StreamStateCallback cb) {
		stateCallbacks.add(cb);
	}
	
	public void removeStateChangeListener(StreamStateCallback cb) {
		stateCallbacks.remove(cb);
	}
	
	private void setStreamState(StreamState s) {
		if(streamState == s) {
			return;
		}
		streamState = s;
		for(StreamStateCallback cb : stateCallbacks) {
			cb.onStateChanged(s);
		}
	}
	
	public StreamState getState() {
		return streamState;
	}
	
	public List<ChunkDescriptor> getChunks() {
		return chunks;
	}
	
	/**
	 * Enable or disable datastream compression. If compression is enabled,
	 * data retrieved from this stream is run through a fast zlib compression
	 * routine before being base64 encoded for transport.
	 * 
	 * If you're using the NullEncoder, compression is encouraged.
	 * For any other encoding scheme, use of compression should be decided on
	 * a case-by-case basis.
	 * 
	 * @param enable true to enable, false to disable
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
	 * Get data for a chunk of audio as an encoded string.
	 * This method is used to facilitate audio transport.
	 * 
	 * The logic runs in its own thread, and calls cb when complete.
	 */
	public void getChunkData(ChunkDescriptor chunk, Callback cb) {
		requestQueue.add(new ChunkRequest(chunk,cb));
		serviceChunkRequests();
	}
	
	private void serviceChunkRequests() {
		if(worker != null) {
			return;
		}
		
		ChunkRequest request = requestQueue.remove();
		
		final ChunkDescriptor chunk = request.chunk;
		final Callback callback = request.callback;
		
		worker = new Thread(new Runnable() {
			@Override
			public void run() {
				setStreamState(StreamState.READING);
				int startOffset = chunk.getStartStreamByteOffset();
				int endOffset = chunk.getEndStreamByteOffset();
				int length = endOffset - startOffset; 
				
				setStreamState(StreamState.ENCODING);
				byte[] bytes = encoder.encode(startOffset, length);
				
				if(compression) {
					setStreamState(StreamState.COMPRESSING);
					bytes = StreamDataEncoder.compress(bytes);
				}
				
				setStreamState(StreamState.SERIALIZING);
				String serialized = StreamDataEncoder.encode(bytes);
				callback.onComplete(serialized);
				
				// We're done, kill the worker
				worker = null;
				if(requestQueue.isEmpty()) {
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
		// Use binary search to locate the chunk responsible for position
		// TODO: maybe remove this function, we don't necessarily need it at all
		return null;
	}

	public ChunkDescriptor getChunkById(int chunkID) {
		return null;
	}
	
	public int getChunkCount() {
		return chunkCount;
	}
	
	public int getDuration() {
		return duration;
	}
	
}
