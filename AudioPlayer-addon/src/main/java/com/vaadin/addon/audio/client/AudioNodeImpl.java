package com.vaadin.addon.audio.client;

import java.util.ArrayList;
import java.util.List;

import elemental.html.AudioContext;
import elemental.html.AudioParam;

public class AudioNodeImpl implements elemental.html.AudioNode {
	
	ArrayList<AudioNodeImpl> inputs = new ArrayList<AudioNodeImpl>();
	ArrayList<AudioNodeImpl> outputs = new ArrayList<AudioNodeImpl>();
	
	@Override
	public AudioContext getContext() {
		// store as member or use static?
		return AudioPlayerConnector.getContext();
	}

	@Override
	public int getNumberOfInputs() {
		return inputs.size();
	}

	@Override
	public int getNumberOfOutputs() {
		return outputs.size();
	}

	@Override
	public void connect(elemental.html.AudioNode destination, int output, int input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connect(AudioParam destination, int output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect(int output) {
		// TODO Auto-generated method stub

	}

}
