package org.vaadin.addon.audio.client;


import java.util.logging.Logger;

import org.vaadin.addon.audio.shared.util.LogUtils;
import org.vaadin.addon.audio.client.webaudio.*;

/**
 * Actual player component, plays a single Buffer.
 * 
 * We're using multiple players (two, ideally) to be able to
 * quickly change from one chunk to the next without experiencing
 * possible lag as the data buffers are swapped.
 */
public class BufferPlayer {
	
	private static Logger logger = Logger.getLogger("BufferPlayer");
	
	private enum State {
		PLAYING,
		STOPPED 
	}

	//private BalanceEffect balanceEffect;
	
	private BufferSourceNode source;
	private GainNode output;
	private State state = State.STOPPED;
	private double playbackSpeed = 1;

	private PitchShiftNode pitchShiftNode;
	private MultiChannelGainNode multiChannelGainNode;
	

	public BufferPlayer() {
		this(null);
	}
	
	public BufferPlayer(Buffer buffer) {
		//logger.info(LogUtils.prefix("create"));
		Context context = Context.get();
		source = context.createBufferSourceNode();
		multiChannelGainNode = new MultiChannelGainNode(context);
		if (buffer != null) {
			source.setNativeBuffer(buffer.getAudioBuffer());
			multiChannelGainNode.connect(source);
		}
		pitchShiftNode = new PitchShiftNode(context.getNativeContext());
		// create output audio node
		output = context.createGainNode();
		output.connect(pitchShiftNode.getInput());
		pitchShiftNode.connect(context.getDestination());
	}
	
	public AudioNode getSourceNode() {
		return source;
	}
	
	public AudioNode getOutput() {
		return output;
	}

	public boolean isPlaying() {
		return state == State.PLAYING;
	}
	
	public void play(int offset_millis) {
		logger.info(LogUtils.prefix("start playback at " + offset_millis));
		if (state == State.PLAYING) {
			stop();
		}
		double offsetSeconds = offset_millis / 1000d;
		source.start(0, offsetSeconds);
		state = State.PLAYING;
	}
	
	public void stop() {
		logger.info(LogUtils.prefix("stop playback"));
		if (state == State.STOPPED) {
			return;
		}
		Buffer buffer = source.getBuffer();
		source.stop();
		// source nodes can only be played once, so we need to 
		// create a new audio node and prep it to be played
		resetSourceNode(buffer);
		state = State.STOPPED;
	}

	private void resetSourceNode(Buffer buffer) {
		resetSourceNode(buffer, null);
	}

	private void resetSourceNode(Buffer buffer, final BufferSourceNode.BufferReadyListener cb) {
		source.disconnect();
		source.resetNode();
		if (buffer != null) {
			source.setNativeBuffer(buffer.getAudioBuffer());
			configAudioNodeChain();
		}
	}
	
	public void setBuffer(Buffer buffer) {
		setBuffer(buffer, null);
	}
	
	public void setBuffer(Buffer buffer, final BufferSourceNode.BufferReadyListener cb) {
		source.setBuffer(buffer, new BufferSourceNode.BufferReadyListener() {
			@Override
			public void onBufferReady(Buffer b) {
				configAudioNodeChain();
				if (cb != null) {
					cb.onBufferReady(b);
				}
			}
		});
	}
	
	public Buffer getBuffer() {
		return source.getBuffer();
	}
	
	public void setVolume(double volume) {
		// logger.info(LogUtils.prefix("set volume to " + volume));
		output.setGain(volume);
	}

	public void setVolume(double volume, int channel) {
		multiChannelGainNode.setGain(volume, channel);
	}
	
	public double getVolume() {
		return output.getGain();
	}

	public double getVolume(int channelIndex) {
		return multiChannelGainNode.getGain(channelIndex);
	}

	public int getNumberOfChannels() {
		return multiChannelGainNode.getNumberOfChannels();
	}

//	/**
//	 * Changes the speed at which the BufferPlayer plays the audio without changing
//	 * the pitch of the audio. The audio buffer will need to be reprocessed in order to
//	 * play at the speed requested.
//	 * @param playbackSpeed value greater than 0
//	 */
//	public void setPlaybackSpeed(double playbackSpeed) {
//		logger.info(LogUtils.prefix("set speed scale " + playbackSpeed));
//		if (this.playbackSpeed == playbackSpeed) {
//			// don't do anything if we aren't changing the scale
//			logger.info(LogUtils.prefix("playback speed did not change"));
//			return;
//		}
//		// you can only set a BufferSourceNode's buffer once, so lets reset the node and re-set the buffer
//		source.disconnect();
//		source.resetNode();
//		source.setPlaybackRate(playbackSpeed);
//		// generate warped buffer if playback speed is other than 1
//		if (unmodifiedBuffer != null) {
//			AudioBuffer buffer;
//			if (playbackSpeed != 1) {
//				logger.info(LogUtils.prefix("stretching audio chunk to fit playback speed of " + playbackSpeed));
//				double stretchFactor = 1d / playbackSpeed;
//				AudioContext context = Context.get().getNativeContext();
//				int numChannels = unmodifiedBuffer.getNumberOfChannels();
//				buffer = AudioBufferUtils.timeStrechAudioBuffer(stretchFactor, unmodifiedBuffer, context, numChannels, false);
//				logger.info(LogUtils.prefix("stretching complete"));
//			} else {
//				buffer = unmodifiedBuffer;
//			}
//			// apply our buffer ot the source BufferSourceNode
//			if (buffer != null) {
//				logger.info(LogUtils.prefix("Setting buffer"));
//				source.setNativeBuffer(buffer);
//			}
//		} else {
//			logger.severe("unmodifiedBuffer is NULL");
//		}
//	}

	/**
	 * Changes the speed at which the BufferPlayer plays the audio without changing
	 * the pitch of the audio.
	 * @param playbackSpeed value greater than 0
	 */
	public void setPlaybackSpeed(double playbackSpeed) {
		logger.info(LogUtils.prefix("set speed scale " + playbackSpeed));
		this.playbackSpeed = playbackSpeed;
		Context context = Context.get();
		if (playbackSpeed == 1) {
			if (pitchShiftNode != null) {
				pitchShiftNode.disconnect();
				pitchShiftNode = null;
			}
		} else {
			// add pitch shift node to end of chain
			pitchShiftNode = new PitchShiftNode(context.getNativeContext());
			pitchShiftNode.normalizePitchBasedOnPlaybackSpeed(playbackSpeed);
		}
		configAudioNodeChain();
		source.setPlaybackRate(playbackSpeed);
	}

	public void configAudioNodeChain() {
		// example of full chain:
		// source -> multi-channel gain node -> output -> pitch shift node -> destination
		if (source == null) {
			logger.severe("SOURCE IS NULL");
			return;
		}
		// connect source node to individual channel gain nodes
		multiChannelGainNode.connect(source);
		// connect multi channel gain nodes to output node
		multiChannelGainNode.getOutputNode().disconnect();
		multiChannelGainNode.getOutputNode().connect(output);
		// connect output node to pitch shift node if needed then to destination
		output.disconnect();
		if (pitchShiftNode != null) {
			output.connect(pitchShiftNode.getInput());
			pitchShiftNode.disconnect();
			pitchShiftNode.connect(Context.get().getDestination());
		} else {
			output.connect(Context.get().getDestination());
		}
	}
	
	public double getPlaybackSpeed() {
		return playbackSpeed;
	}
	
	public void setBalance(double balance) {
//		balanceEffect.setBalance(balance);
	}
	
	public double getBalance() {
		//return balanceEffect.getBalance();
		return 0;
	}
	
	public String toString() {
		return "BufferPlayer";
	}
	
}
