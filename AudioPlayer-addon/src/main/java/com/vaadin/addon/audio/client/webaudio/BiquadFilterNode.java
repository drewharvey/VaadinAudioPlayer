package com.vaadin.addon.audio.client.webaudio;



import java.util.logging.Logger;

import elemental.html.AudioContext;

public class BiquadFilterNode extends AudioNode {
	
	public enum Type {
		LOWPASS,
		HIGHPASS,
		BANDPASS,
		LOWSHELF,
		HIGHSELF,
		PEAKING,
		NOTCH,
		ALLPASS
	}
	
	private static final native elemental.html.AudioNode
	createBiquadFilterNode(AudioContext ctx) /*-{
		return ctx.createBiquadFilter();
	}-*/;
	
	protected BiquadFilterNode(AudioContext ctx) {
		super(createBiquadFilterNode(ctx));
	}
	
	public void setGain(double gain) {
		setGain(getNativeNode(), gain);
	}
	private static final native void setGain(elemental.html.AudioNode node, double gain) /*-{
		node.gain.value = gain;
	}-*/;
	
	
	public double getGain() {
		return getGain(getNativeNode());
	}
	private static final native double getGain(elemental.html.AudioNode node) /*-{
		return node.gain.value;
	}-*/;
	
	
	public void setQ(double q) {
		setQ(getNativeNode(), q);
	}
	private static final native void setQ(elemental.html.AudioNode node, double q) /*-{
		node.Q.value = q;
	}-*/;
	
	
	public double getQ() {
		return getQ(getNativeNode());
	}
	private static final native double getQ(elemental.html.AudioNode node) /*-{
		return node.Q.value;
	}-*/;
	
	
	public void setFrequency(double freq) {
		setFrequency(getNativeNode(), freq);
	}
	private static final native void setFrequency(elemental.html.AudioNode node, double freq) /*-{
		node.frequency.value = freq;
	}-*/;
	
	
	public double getFrequency() {
		return getFrequency(getNativeNode());
	}
	private static final native double getFrequency(elemental.html.AudioNode node) /*-{
		return node.frequency.value;
	}-*/;
	
	
	public void setType(Type type) {
		// type string must be all lowercase
		setType(getNativeNode(), type.name().toLowerCase());
	}
	private static final native void setType(elemental.html.AudioNode node, String type) /*-{
		node.type = type;
	}-*/;
	
	
	public Type getType() {
		String type = getType(getNativeNode());
		if (type != null && !type.isEmpty()) {
			return Type.valueOf(type.toUpperCase());
		}
		return null;
	}
	private static final native String getType(elemental.html.AudioNode node) /*-{
		return node.type;
	}-*/;
	
	@Override
	public String toString() {
		String str = "";
		str += "BiquadFilterNode:\r\n";
		str += " Gain: " + getGain() + "\n\r";
		str += " Frequency: " + getFrequency() + "\n\r";;
		str += " Q: " + getQ() + "\n\r";;
		str += " Type: " + getType().name();
		return str;
	}
	
}
