package com.vaadin.addon.audio.client.effects.nodes;

import com.vaadin.addon.audio.client.AudioNodeImpl;
import com.vaadin.addon.audio.client.AudioParamImpl;

import elemental.html.AudioParam;
import elemental.html.BiquadFilterNode;
import elemental.html.Float32Array;

public class BiquadFilterNodeImpl extends AudioNodeImpl implements BiquadFilterNode {
	
	AudioParam q = new AudioParamImpl();
	AudioParam frequency = new AudioParamImpl();
	AudioParam gain = new AudioParamImpl();
	int type = 0;


	@Override
	public AudioParam getQ() {
		return q;
	}

	@Override
	public AudioParam getFrequency() {
		return frequency;
	}

	@Override
	public AudioParam getGain() {
		return gain;
	}

	@Override
	public int getType() {
		return type;
	}
	
	@Override
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public void getFrequencyResponse(Float32Array frequencyHz, Float32Array magResponse, Float32Array phaseResponse) {
		// TODO Auto-generated method stub

	}

}
