package com.vaadin.addon.audio.server.effects;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.addon.audio.server.Effect;
import com.vaadin.addon.audio.shared.SharedEffect;
import com.vaadin.addon.audio.shared.SharedEffect.EffectName;
import com.vaadin.addon.audio.shared.SharedEffectProperty;
import com.vaadin.addon.audio.shared.SharedEffectProperty.PropertyName;

public class VolumeEffect extends Effect {
	
	private double gain;
	
	public VolumeEffect() {
		super();
	}
	
	public void setGain(double gain) {
		this.gain = gain;
	}
	
	public double getGain() {
		return gain;
	}

	@Override
	public SharedEffect getSharedEffectObject() {
		SharedEffect shared = new SharedEffect(getID(), EffectName.VolumeEffect);
		List<SharedEffectProperty> props = new ArrayList<SharedEffectProperty>();
		props.add(new SharedEffectProperty(PropertyName.Gain, getGain() + ""));
		shared.setProperties(props);
		return shared;
	}

}
