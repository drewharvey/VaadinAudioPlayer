package com.vaadin.addon.audio.client.webaudio;

import elemental.html.AudioContext;

public class GainNode extends AudioNode {

	private static final native elemental.html.AudioNode
	createGainNode(AudioContext ctx) /*-{
		return ctx.createGain();
	}-*/;
	
	protected GainNode(AudioContext ctx) {
		super(createGainNode(ctx));
	}
	
	public void setGain(double gain) {
		setGain(getNativeNode(), gain);
	}
	
	private static final native void setGain(elemental.html.AudioNode node, double gain) /*-{
		node.gain = gain;
	}-*/;
	
	public double getGain() {
		return getGain(getNativeNode());
	}
	
	private static final native double getGain(elemental.html.AudioNode node) /*-{
		return node.gain;
	}-*/;
}
