package com.vaadin.addon.audio.client.webaudio;

public class AudioDestinationNode extends AudioNode {

	protected AudioDestinationNode(elemental.html.AudioNode nativeNode) {
		super(nativeNode);
	}

	@Override
	public String toString() {
		return "AudioDestinationNode";
	}
}
