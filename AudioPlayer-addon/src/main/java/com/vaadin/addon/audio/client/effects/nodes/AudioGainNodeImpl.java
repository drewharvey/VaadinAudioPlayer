package com.vaadin.addon.audio.client.effects.nodes;

import com.vaadin.addon.audio.client.AudioNodeImpl;
import com.vaadin.addon.audio.client.AudioParamImpl;

import elemental.html.AudioContext;
import elemental.html.AudioGain;
import elemental.html.AudioGainNode;
import elemental.html.AudioNode;
import elemental.html.AudioParam;

public class AudioGainNodeImpl extends AudioNodeImpl implements AudioGainNode {

	AudioParam gain = new AudioParamImpl();

	@Override
	public AudioGain getGain() {
		return (AudioGain) gain;
	}

}
