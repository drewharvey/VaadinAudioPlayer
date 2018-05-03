package org.vaadin.addon.audio.server.effects;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.addon.audio.server.Effect;
import org.vaadin.addon.audio.shared.SharedEffect;
import org.vaadin.addon.audio.shared.SharedEffectProperty;
import org.vaadin.addon.audio.shared.SharedEffectProperty.PropertyName;

public class BalanceEffect extends Effect {
	
	private double position;
	
	public BalanceEffect() {
		super();
	}
	
	public void setBalance(double position) {
		this.position = position;
	}
	
	public double getBalance() {
		return position;
	}

	@Override
	public SharedEffect getSharedEffectObject() {
		SharedEffect shared = new SharedEffect(getID(), SharedEffect.EffectName.BalanceEffect);
		List<SharedEffectProperty> props = new ArrayList<SharedEffectProperty>();
		props.add(new SharedEffectProperty(PropertyName.Balance, getBalance() + ""));
		shared.setProperties(props);
		return shared;
	}

}
