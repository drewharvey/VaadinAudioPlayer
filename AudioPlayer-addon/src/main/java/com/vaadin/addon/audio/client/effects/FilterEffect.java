package com.vaadin.addon.audio.client.effects;

import java.util.List;

import com.vaadin.addon.audio.client.Effect;
import com.vaadin.addon.audio.client.util.Log;
import com.vaadin.addon.audio.shared.SharedEffectProperty;
import com.vaadin.addon.audio.shared.SharedEffectProperty.PropertyName;

import elemental.html.AudioContext;
import elemental.html.BiquadFilterNode;

public class FilterEffect extends Effect {
	
	public enum Type {
		LOWPASS,
		HIGHPASS
	}
	
	public FilterEffect() {
		
	}
	
	public FilterEffect(List<SharedEffectProperty> props) {
		for (SharedEffectProperty prop : props) {
			if (prop.getProperty() == PropertyName.FilterType) {
				
			} else if (prop.getProperty() == PropertyName.Frequency) {
				setFrequency(Float.parseFloat(prop.getValue()));
			} else if (prop.getProperty() == PropertyName.Gain) {
				setGain(Float.parseFloat(prop.getValue()));
			} else if (prop.getProperty() == PropertyName.Q) {
				setQ(Float.parseFloat(prop.getValue()));
			}
		}
	}

	@Override
	public void init(AudioContext context) {
		Log.message(this, "Creating BiquadFilterNode");
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
	
	public void setType(String type) {
		type = type.toUpperCase();
		Type typeEnum = Type.valueOf(type);
		if (typeEnum != null) {
			setType(typeEnum);
		}
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
