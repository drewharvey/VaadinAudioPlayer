package com.vaadin.addon.audio.client.webaudio;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.Timer;

import elemental.html.AudioBuffer;
import elemental.html.AudioContext;

//See https://developer.mozilla.org/en-US/docs/Web/API/AudioBufferSourceNode
public class BufferSourceNode extends AudioScheduledSourceNode {
	
	private static final Logger logger = Logger.getLogger("BufferSourceNode");


	public static interface BufferReadyListener {
		void onBufferReady(Buffer b);
	}
	
	private Buffer buffer;
	private Timer bufferTimer;
	private double playbackRate = 1d;
	
	private static final native elemental.html.AudioNode
	createBufferSource(AudioContext ctx) /*-{
		return ctx.createBufferSource();
	}-*/;
	
	protected BufferSourceNode(AudioContext ctx) {
		super(createBufferSource(ctx));
	}

	public void resetNode() {
		// create new buffer source node and add the current buffer
		setNativeNode(createBufferSource(getNativeContext()));
	}
	
	public void setBuffer(final Buffer buffer, final BufferReadyListener cb) {
		if(buffer == this.buffer) {
			return;
		}
		
		this.buffer = buffer;
		// cancel previous buffer check timer
		if(bufferTimer != null && bufferTimer.isRunning()) {
			bufferTimer.cancel();
			bufferTimer = null;
		}
		// set timer to periodically check if buffer is ready to play
		bufferTimer = new Timer() {
			@Override
			public void run() {
				Buffer b = BufferSourceNode.this.buffer;
				if(!b.isReady()) {
					bufferTimer.schedule(20);
				} else {
					logger.log(Level.SEVERE, " === AUDIO BUFFER IS READY ==== ");
					setNativeBuffer(b.getAudioBuffer());
					if (cb != null) {
						cb.onBufferReady(b);
					}
				}
			}
		};
		// Call run instead of schedule to immediately run the timer logic
		bufferTimer.run();
	}
	
	public void setNativeBuffer(AudioBuffer buffer) {
		setBuffer(getNativeNode(), buffer);
	}
	
	private static final native void setBuffer(elemental.html.AudioNode node, AudioBuffer buffer) /*-{
		node.buffer = buffer;
	}-*/;
	
	public Buffer getBuffer() {
		return buffer;
	}
	
	public AudioBuffer getNativeBuffer() {
		return getBuffer(getNativeNode());
	}
	
	private static final native AudioBuffer getBuffer(elemental.html.AudioNode node) /*-{
		return node.buffer;
	}-*/;

	public void setDetune(double cents) {
		setDetune(getNativeNode(), cents);
	}
	
	private static final native void setDetune(elemental.html.AudioNode node, double cents) /*-{
		node.detune.value = cents;
	}-*/;
	
	public double getDetune() {
		return getDetune(getNativeNode());
	}
	
	private static final native double getDetune(elemental.html.AudioNode node) /*-{
		return node.detune.value;
	}-*/;
	
	public void setPlaybackRate(double rate) {
		// playback rate of 0 doesn't make sense and sets us up for exceptions
		if (rate <= 0) {
			rate = 0.01;
		}
		playbackRate = rate;
	}
	
	private static final native void setPlaybackRate(elemental.html.AudioNode node, double rate, elemental.html.AudioContext ctx) /*-{
		// set nodes playback rate
		node.playbackRate.value = rate;
    }-*/;
	
	public double getPlaybackRate() {
		return playbackRate;
		//return getPlaybackRate(getNativeNode());
	}
	
	private static final native double getPlaybackRate(elemental.html.AudioNode node) /*-{
		return node.playbackRate.value;
	}-*/;

	public void updateTimeStretchFactor() {
		double stretchFactor = 1d / playbackRate;
		// setTimeStretchFactor(getNativeNode(), stretchFactor);

		doStretch(stretchFactor, 1, true, getNativeNode());
	}

	private static final native void setTimeStretchFactor(elemental.html.AudioNode node, double stretchFactor) /*-{
		if (!node || !node.buffer) {
			console.error("Node buffer is null");
			return;
		}
        console.error("#### getting data for stretch ####");
		console.error(node.buffer);
        // stretch audio buffer
        var numberOfChannels = 1; //node.buffer.numberOfChannels;
        var sampleRate = node.buffer.sampleRate;
        var useQuickSearch = false; // true for performance boost
        var outputBuffer = new Float32Array(node.buffer.getChannelData(0).length);
        console.error("#### STRETCHING ####");
        // use Kali to stretch audio buffer
		if (!$wnd.kali) {
            var kali = new $wnd.Kali(numberOfChannels);
        }
        kali.setup(sampleRate, stretchFactor, useQuickSearch);
        kali.input(node.buffer.getChannelData(0));
        kali.process();
        kali.output(outputBuffer);
		console.error("OUTPUT BUFFER");
		console.error(outputBuffer);
        kali.flush();
        node.buffer.copyFromChannel(outputBuffer, 0);
        console.error(node);
        console.log(node.buffer);
    }-*/;


	private static final native void doStretch(double stretchFactor, int numChannels, boolean quickSeek,
											   elemental.html.AudioNode node) /*-{
		if (!node) {
			console.error("NODE IS NULL");
			return;
		}
		if (!node.buffer) {
			console.error("NODE BUFFEr IS NULL");
			return;
		}
        var inputData = node.buffer.getChannelData(0);


        var numInputFrames = inputData.length / numChannels;
        var bufsize = 4096 * numChannels;

        // Create a Kali instance and initialize it
        var kali = new $wnd.Kali(numChannels);
        kali.setup(node.context.sampleRate, stretchFactor, quickSeek);

        // Create an array for the stretched output
        var completed = new Float32Array(Math.floor((numInputFrames / stretchFactor) * numChannels + 1));

        var inputOffset = 0;
        var completedOffset = 0;
        var loopCount = 0;
        var flushed = false;

        while (completedOffset < completed.length) {
            if (loopCount % 100 == 0) {
                console.log("Stretching", completedOffset  / completed.length);
            }

            // Read stretched samples into our output array
            completedOffset += kali.output(completed.subarray(completedOffset, Math.min(completedOffset + bufsize, completed.length)));

            if (inputOffset < inputData.length) { // If we have more data to write, write it
                var dataToInput = inputData.subarray(inputOffset, Math.min(inputOffset + bufsize, inputData.length));
                inputOffset += dataToInput.length;

                // Feed Kali samples
                kali.input(dataToInput);
                kali.process();
            } else if (!flushed) { // Flush if we haven't already
                kali.flush();
                flushed = true;
            }

            loopCount++;
        }

        //return completed;


		output = completed;

        var outputAudioBuffer = node.context.createBuffer(1, output.length, node.context.sampleRate);

        outputAudioBuffer.getChannelData(0).set(output);

		node.buffer = outputAudioBuffer;
	}-*/;
	
	@Override
	public String toString() {
		return "BufferSourceNode";
	}
}
