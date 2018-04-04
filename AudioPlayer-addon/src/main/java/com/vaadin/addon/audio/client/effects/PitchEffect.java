package com.vaadin.addon.audio.client.effects;

import java.util.List;

import com.vaadin.addon.audio.client.Effect;
import com.vaadin.addon.audio.client.webaudio.Context;
import com.vaadin.addon.audio.shared.SharedEffectProperty;
import com.vaadin.addon.audio.shared.util.Log;

public class PitchEffect extends Effect {
	
	@Override
	public void init(Context context) {
		// TODO Auto-generated method stub
		Log.message(this, "init");
	}
	
	@Override
	public void setProperties(List<SharedEffectProperty> props) {
		
	}
	
	@Override
	public PitchEffect createCopy(Context context) {
		PitchEffect e = new PitchEffect();
		e.init(context);
		return e;
	}

	public String toString() {
		return "PitchEffect";
	}
	
	// Figure out how to create a pitch shift effect-
	// See https://github.com/mmckegg/soundbank-pitch-shift/blob/master/index.js

}
