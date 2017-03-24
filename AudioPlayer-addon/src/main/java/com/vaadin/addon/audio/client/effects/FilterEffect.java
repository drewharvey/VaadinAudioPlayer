package com.vaadin.addon.audio.client.effects;

import com.vaadin.addon.audio.client.Effect;
import com.vaadin.addon.audio.client.util.Log;

import elemental.html.AudioContext;

public class FilterEffect extends Effect {

	@Override
	public void init(AudioContext context) {
		// TODO Auto-generated method stub
		Log.message(this, "init");	
	}

	// Figure out how to implement high/lowpass filter
	// Use something like a BiquadFilterNode
	// See https://developer.mozilla.org/en-US/docs/Web/API/BiquadFilterNode
	
}
