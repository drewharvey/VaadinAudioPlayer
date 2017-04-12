package com.vaadin.addon.audio.client.webaudio;

public abstract class AudioScheduledSourceNode extends AudioNode {

	protected AudioScheduledSourceNode(elemental.html.AudioNode nativeNode) {
		super(nativeNode);
	}

	public void start() {
		start(0);
	}
	
	public void start(int delay_millis) {
		start(getNativeNode(),delay_millis);
	}
	
	private static final native void start(elemental.html.AudioNode node, int delay) /*-{
		node.start(delay);
	}-*/;
	
	public void stop() {
		stop(0);
	}
	
	public void stop(int delay_millis) {
		stop(getNativeNode(),delay_millis);
	}

	private static final native void stop(elemental.html.AudioNode node, int delay) /*-{
		node.stop(delay);
	}-*/;

}
