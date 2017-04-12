package com.vaadin.addon.audio.client.effects;

import com.vaadin.addon.audio.client.Effect;
import com.vaadin.addon.audio.shared.util.Log;

import elemental.html.AudioContext;

public class PitchEffect extends Effect {
	
	@Override
	public void init(AudioContext context) {
		// TODO Auto-generated method stub
		Log.message(this, "init");
	}

	public String toString() {
		return "PitchEffect";
	}
	
	// Figure out how to create a pitch shift effect-
	// See https://github.com/mmckegg/soundbank-pitch-shift/blob/master/index.js

}
