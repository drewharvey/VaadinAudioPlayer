package com.vaadin.addon.audio.client.effects;

import com.vaadin.addon.audio.client.Effect;
import com.vaadin.addon.audio.client.webaudio.Context;
import com.vaadin.addon.audio.client.webaudio.GainNode;
import com.vaadin.addon.audio.shared.util.Log;

import elemental.html.AudioContext;

public class VolumeEffect extends Effect {
	
	@Override
	public void init(AudioContext context) {
		Log.message(this, "Creating AudioGainNode");
		this.setAudioNode(Context.get().createGainNode());
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
