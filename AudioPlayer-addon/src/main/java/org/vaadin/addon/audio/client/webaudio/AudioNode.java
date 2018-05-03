package org.vaadin.addon.audio.client.webaudio;

import org.vaadin.addon.audio.shared.util.LogUtils;
import elemental.html.AudioContext;


import java.util.logging.Logger;

public abstract class AudioNode {

	private elemental.html.AudioNode wa_node;
	
	protected AudioNode(elemental.html.AudioNode nativeNode) {
		wa_node = nativeNode;
	}
	
	protected void setNativeNode(elemental.html.AudioNode node) {
		wa_node = node;
	}
	
	protected elemental.html.AudioNode getNativeNode() {
		return wa_node;
	}

	protected AudioContext getNativeContext() {
		return getAudioContext(wa_node);
	}
	
	private static final native AudioContext getAudioContext(elemental.html.AudioNode node) /*-{
		return node.context;
	}-*/;
	
	/**
	 * Connect another AudioNode to this AudioNode
	 * @param other another AudioNode
	 */
	public void connect(AudioNode other) {
		Logger.getLogger("AudioNode").info(LogUtils.prefix("CONNECTING " + this.toString() + " to " + other.toString()));
		connect(this.wa_node,other.wa_node,0,0);
	}

	/**
	 * Connect another AudioNode to this AudioNode using specified channels
	 * @param other another AudioNode
	 * @param inputIndex channel of the node being connected to
	 * @param outputIndex channel of the node connecting
	 */
	public void connect(AudioNode other, int outputIndex, int inputIndex) {
		Logger.getLogger("AudioNode").info("CONNECTING "
				+ this.toString() + "["+outputIndex +"] to " + other.toString() + "["+inputIndex+"]");
		connect(this.wa_node, other.wa_node, outputIndex, inputIndex);
	}

	// TODO: this method is only here to allow the PitchShiftNode class to connect, take this away when its been refactored
	public void connect(elemental.html.AudioNode other) {
		Logger.getLogger("AudioNode").info(LogUtils.prefix("CONNECTING " + this.toString() + " to pitch shift node"));
		connect(this.wa_node, other, 0, 0);
	}
	
	// See https://developer.mozilla.org/en-US/docs/Web/API/AudioNode/connect
	private static final native void connect(
			elemental.html.AudioNode src,
			elemental.html.AudioNode dst,
			int outputIndex,
			int inputIndex) /*-{
		console.log("connect src: " + src + " to dst: " + dst + " output: " + outputIndex + " input: " + inputIndex); 
		src.connect(dst,outputIndex,inputIndex);
	}-*/;
	
	/**
	 * Disconnect this AudioNode from all outputs.
	 */
	public void disconnect() {
		Logger.getLogger("AudioNode").info(LogUtils.prefix("DISCONNECTING FROM ALL"));
		if (getNumberOfOutputs() > 0) {
			disconnect(this.wa_node);
		}
	}
	
	// See https://developer.mozilla.org/en-US/docs/Web/API/AudioNode/disconnect
	private static final native void disconnect(elemental.html.AudioNode src) /*-{
		src.disconnect();
	}-*/;
	
	/**
	 * Disconnect another AudioNode from this AudioNode
	 * @param other another AudioNode
	 */
	public void disconnect(AudioNode other) {
		Logger.getLogger("AudioNode").info(LogUtils.prefix("DISCONNECTING " + this.toString() + " FROM " + other.toString()));
		disconnect(this.wa_node,other.wa_node,0,0);
	}
	
	// See https://developer.mozilla.org/en-US/docs/Web/API/AudioNode/disconnect
	private static final native void disconnect(
			elemental.html.AudioNode src,
			elemental.html.AudioNode dst,
			int outputIndex,
			int inputIndex) /*-{
		src.disconnect(dst,outputIndex,inputIndex);
	}-*/;
	
	public int getNumberOfInputs() {
		return getNumberOfInputs(wa_node);
	}
	
	private static final native int getNumberOfInputs(elemental.html.AudioNode node) /*-{
		return node.numberOfInputs;
	}-*/;
	
	public int getNumberOfOutputs() {
		return getNumberOfOutputs(wa_node);
	}
	
	private static final native int getNumberOfOutputs(elemental.html.AudioNode node) /*-{
		return node.numberOfOutputs;
	}-*/;
	
	/**
	 * Prints the js audio node to the console as an error for debugging.
	 */
	public void printToConsole() {
		printToConsole(wa_node);
	}
	private static final native void printToConsole(elemental.html.AudioNode node) /*-{
		console.error(node);
	}-*/;
	
	@Override
	public String toString() {
		return "AudioNode";
	}
	
}