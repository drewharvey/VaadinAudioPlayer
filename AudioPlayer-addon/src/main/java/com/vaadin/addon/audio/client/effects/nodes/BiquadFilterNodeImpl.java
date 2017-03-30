package com.vaadin.addon.audio.client.effects.nodes;

import com.vaadin.addon.audio.client.AudioNodeImpl;

import elemental.html.AudioParam;
import elemental.html.BiquadFilterNode;
import elemental.html.Float32Array;

public class BiquadFilterNodeImpl extends AudioNodeImpl implements BiquadFilterNode {
	
	AudioParam q;
	AudioParam frequency;
	AudioParam gain;
	int type;


	@Override
	public AudioParam getQ() {
		return q;
	}
	
	public void setQ(AudioParam q) {
		this.q = q;
	}

	@Override
	public AudioParam getFrequency() {
		return frequency;
	}
	
	public void setFrequency(AudioParam frequency) {
		this.frequency = frequency;
	}

	@Override
	public AudioParam getGain() {
		return gain;
	}
	
	public void setGain(AudioParam gain) {
		this.gain = gain;
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
