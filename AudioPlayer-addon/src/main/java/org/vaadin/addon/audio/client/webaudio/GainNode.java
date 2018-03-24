package org.vaadin.addon.audio.client.webaudio;


import java.util.logging.Logger;

import org.vaadin.addon.audio.shared.util.LogUtils;
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
		node.gain.value = gain;
	}-*/;
	
	public double getGain() {
		return getGain(getNativeNode());
	}
	
	private static final native double getGain(elemental.html.AudioNode node) /*-{
		return node.gain.value;
	}-*/;
	
	public void setValueAtTime(double value, double changeDuration) {
		// the timeConstant is the amount of time it takes to get to 67% of the final value
		double timeConstant = changeDuration * 0.67d;
		setValueAtTime(getNativeNode(), value, Context.get().getCurrentTime(), timeConstant);
	}
	
	private static final native void setValueAtTime(elemental.html.AudioNode node, double value, double startTime, double timeConstant) /*-{
		console.error(node.gain.setValueAtTime(value, startTime, timeConstant));
	}-*/;
	
	public void exponentialRampToValueAtTime(double value, double time) {
		Logger.getLogger("GainNode").info(LogUtils.prefix("exponentialRampToValueAtTime(" + value + ", " + time + ") now=" + Context.get().getCurrentTime()));
		exponentialRampToValueAtTime(getNativeNode(), value, time, Context.get().getCurrentTime());
	}
	
	private static final native void exponentialRampToValueAtTime(elemental.html.AudioNode node, double value, double time, double currentTime) /*-{
		console.error(node);
		console.error(currentTime + " -> " + time);
		// according to web audio spec, 0 is an invalid value
		if (value <= 0) {
			value = 0.01;
		}
		// need to trigger start time by setting a value
		node.gain.setValueAtTime(node.gain.value, currentTime);
		// start exp curve 
		node.gain.exponentialRampToValueAtTime(value, time);
	}-*/;
	
	@Override
	public String toString() {
		String str = "";
		str += "GainNode:\n\r";
		str += " Gain: " + getGain();
		return str;
	}
}
