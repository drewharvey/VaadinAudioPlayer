package com.vaadin.addon.audio.client.effects;

import com.vaadin.addon.audio.client.Effect;

import elemental.html.AudioContext;
import elemental.html.BiquadFilterNode;

public class FilterEffect extends Effect {
	
	public enum Type {
		LOWPASS,
		HIGHPASS
	}

	@Override
	public void init(AudioContext context) {
		setAudioNode(context.createBiquadFilter());
	}
	
	public float getQ() {
		return ((BiquadFilterNode) getAudioNode()).getQ().getValue();
	}
	
	public void setQ(float q) {
		((BiquadFilterNode) getAudioNode()).getQ().setValue(q);
	}
	
	public float getFrequency() {
		return ((BiquadFilterNode) getAudioNode()).getFrequency().getValue();
	}
	
	public void setFrequency(float frequency) {
		((BiquadFilterNode) getAudioNode()).getFrequency().setValue(frequency);
	}
	
	public float getGain() {
		return ((BiquadFilterNode) getAudioNode()).getGain().getValue();
	}
	
	public void setGain(float gain) {
		((BiquadFilterNode) getAudioNode()).getGain().setValue(gain);
	}
	
	public Type getType() {
		 return typeIntToEnum(((BiquadFilterNode) getAudioNode()).getType());
	}
	
	public void setType(Type type) {
		((BiquadFilterNode) getAudioNode()).setType(typeEnumToInt(type));
	}
	
	private int typeEnumToInt(Type type) {
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
	
}
