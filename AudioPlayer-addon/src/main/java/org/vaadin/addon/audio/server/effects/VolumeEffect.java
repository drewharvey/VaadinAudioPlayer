package org.vaadin.addon.audio.server.effects;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.addon.audio.server.Effect;
import org.vaadin.addon.audio.shared.SharedEffect;
import org.vaadin.addon.audio.shared.SharedEffectProperty;

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
		SharedEffect shared = new SharedEffect(getID(), SharedEffect.EffectName.VolumeEffect);
		List<SharedEffectProperty> props = new ArrayList<SharedEffectProperty>();
		props.add(new SharedEffectProperty(SharedEffectProperty.PropertyName.Gain, getGain() + ""));
		shared.setProperties(props);
		return shared;
	}

}
