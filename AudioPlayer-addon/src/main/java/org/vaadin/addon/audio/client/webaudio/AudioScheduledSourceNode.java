package org.vaadin.addon.audio.client.webaudio;

public abstract class AudioScheduledSourceNode extends AudioNode {

	protected AudioScheduledSourceNode(elemental.html.AudioNode nativeNode) {
		super(nativeNode);
	}

	public void start() {
		start(0, 0);
	}
	
	public void start(double delay_seconds, double offset_seconds) {
		start(getNativeNode(), delay_seconds, offset_seconds);
	}
	
	private static final native void start(elemental.html.AudioNode node, double delay, double offset) /*-{
		node.start(delay, offset);
	}-*/;
	
	public void stop() {
		stop(0);
	}
	
	public void stop(double delay_seconds) {
		stop(getNativeNode(), delay_seconds);
	}

	private static final native void stop(elemental.html.AudioNode node, double delay) /*-{
		node.stop(delay);
	}-*/;

	@Override
	public String toString() {
		return "AudioScheduledSourceNode";
	}
}
