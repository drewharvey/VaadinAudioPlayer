package com.vaadin.addon.audio.client.effects;

import com.vaadin.addon.audio.client.AudioParamImpl;
import com.vaadin.addon.audio.client.Effect;
import com.vaadin.addon.audio.client.effects.nodes.BiquadFilterNodeImpl;
import com.vaadin.addon.audio.client.util.Log;

import elemental.html.AudioContext;
import elemental.html.AudioParam;
import elemental.html.BiquadFilterNode;

public class FilterEffect extends Effect {
	
	public enum Type {
		LOWPASS,
		HIGHPASS
	}

	@Override
	public void init(AudioContext context) {
		setAudioNode(new BiquadFilterNodeImpl());
	}
	
	public float getQ() {
		return ((BiquadFilterNodeImpl) getAudioNode()).getQ().getValue();
	}
	
	public void setQ(float q) {
		AudioParam param = new AudioParamImpl();
		param.setValue(q);
		((BiquadFilterNodeImpl) getAudioNode()).setQ(param);
	}
	
	public float getFrequency() {
		return ((BiquadFilterNodeImpl) getAudioNode()).getFrequency().getValue();
	}
	
	public void setFrequency(float frequency) {
		AudioParam param = new AudioParamImpl();
		param.setValue(frequency);
		((BiquadFilterNodeImpl) getAudioNode()).setFrequency(param);
	}
	
	public float getGain() {
		return ((BiquadFilterNodeImpl) getAudioNode()).getGain().getValue();
	}
	
	public void setGain(float gain) {
		AudioParam param = new AudioParamImpl();
		param.setValue(gain);
		((BiquadFilterNodeImpl) getAudioNode()).setGain(param);
	}
	
	public Type getType() {
		 return typeIntToEnum(((BiquadFilterNodeImpl) getAudioNode()).getType());
	}
	
	public void setType(Type type) {
		((BiquadFilterNodeImpl) getAudioNode()).setType(typeEnumToInt(type));
	}
	
	private int typeEnumToInt(Type type) {
		// TODO: better way to map this?
		if (type == Type.HIGHPASS) {
			return BiquadFilterNode.HIGHPASS;
		}
		if (type == Type.LOWPASS) {
			return BiquadFilterNode.LOWPASS;
		}
		return 0;
	}
	
	private Type typeIntToEnum(int type) {
		if (type == BiquadFilterNode.HIGHPASS) {
			return Type.HIGHPASS;
		}
		if (type == BiquadFilterNode.LOWPASS) {
			return Type.LOWPASS;
		}
		return null;
	}

	// Figure out how to implement high/lowpass filter
	// Use something like a BiquadFilterNode
	// See https://developer.mozilla.org/en-US/docs/Web/API/BiquadFilterNode
	
}
