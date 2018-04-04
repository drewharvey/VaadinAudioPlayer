package org.vaadin.addon.audio.server.effects;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.addon.audio.server.Effect;
import org.vaadin.addon.audio.shared.SharedEffect;
import org.vaadin.addon.audio.shared.SharedEffectProperty;
import org.vaadin.addon.audio.shared.SharedEffect.EffectName;
import org.vaadin.addon.audio.shared.SharedEffectProperty.PropertyName;

public class FilterEffect extends Effect {

	public enum Type {
		LOWPASS,
		HIGHPASS
	}
	
	private double q;
	private double frequency;
	private double gain;
	private Type type = Type.LOWPASS;
	
	public FilterEffect() {
		super();
	}
	
	public double getQ() {
		return q;
	}
	
	public void setQ(double q) {
		this.q = q;
	}
	
	public double getFrequency() {
		return frequency;
	}
	
	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
	
	public double getGain() {
		return gain;
	}
	
	public void setGain(double gain) {
		this.gain = gain;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	@Override
	public SharedEffect getSharedEffectObject() {
		SharedEffect sharedEffect = new SharedEffect(getID(), EffectName.FilterEffect);
		List<SharedEffectProperty> props = new ArrayList<SharedEffectProperty>();
		props.add(new SharedEffectProperty(PropertyName.Q, getQ() + ""));
		props.add(new SharedEffectProperty(PropertyName.Frequency, getFrequency() + ""));
		props.add(new SharedEffectProperty(PropertyName.Gain, getGain() + ""));
		props.add(new SharedEffectProperty(PropertyName.FilterType, getType().name()));
		sharedEffect.setProperties(props);
		return sharedEffect;
	}
	
}
