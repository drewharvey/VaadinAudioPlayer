package com.vaadin.addon.audio.client.effects;

import java.util.List;

import com.vaadin.addon.audio.client.Effect;
import com.vaadin.addon.audio.client.webaudio.Context;
import com.vaadin.addon.audio.client.webaudio.GainNode;
import com.vaadin.addon.audio.shared.SharedEffectProperty;
import com.vaadin.addon.audio.shared.util.Log;


public class VolumeEffect extends Effect {
	
	@Override
	public void init(Context context) {
		Log.message(this, "Creating AudioGainNode");
		this.setAudioNode(context.createGainNode());
	}
	
	@Override
	public void setProperties(List<SharedEffectProperty> props) {
		
	}
	
	@Override
	public VolumeEffect createCopy(Context context) {
		VolumeEffect e = new VolumeEffect();
		e.init(context);
		return e;
	}
	
	public GainNode getGainNode() {
		return ((GainNode) super.getAudioNode());
	}
	
	public double getGain() {
		Log.message(this, "getting gain");
		return getGainNode().getGain();
	}
	
	public void setGain(double gain) {
		Log.message(this, "setting gain to " + gain);
		getGainNode().setGain(gain);
	}
	
	public String toString() {
		return "VolumeEffect";
	}

}
