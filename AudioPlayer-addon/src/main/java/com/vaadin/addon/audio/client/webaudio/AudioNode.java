package com.vaadin.addon.audio.client.webaudio;

import elemental.html.AudioContext;

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
		connect(this.wa_node,other.wa_node,0,0);
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
	 * Disconnect another AudioNode from this AudioNode
	 * @param other another AudioNode
	 */
	public void disconnect(AudioNode other) {
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
	
	
	
	
}
