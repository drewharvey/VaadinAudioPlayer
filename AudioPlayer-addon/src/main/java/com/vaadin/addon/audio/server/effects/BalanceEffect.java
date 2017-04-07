package com.vaadin.addon.audio.server.effects;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.addon.audio.server.Effect;
import com.vaadin.addon.audio.shared.SharedEffect;
import com.vaadin.addon.audio.shared.SharedEffect.EffectName;
import com.vaadin.addon.audio.shared.SharedEffectProperty;
import com.vaadin.addon.audio.shared.SharedEffectProperty.PropertyName;

public class BalanceEffect extends Effect {
	
	private double position;
	
	public void setBalance(double position) {
		this.position = position;
	}
	
	public double getBalance() {
		return position;
	}

	@Override
	public SharedEffect getSharedEffectObject() {
		SharedEffect shared = new SharedEffect(EffectName.BalanceEffect);
		List<SharedEffectProperty> props = new ArrayList<SharedEffectProperty>();
		props.add(new SharedEffectProperty(PropertyName.Balance, getBalance() + ""));
		shared.setProperties(props);
		return shared;
	}

}
