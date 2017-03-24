package com.vaadin.addon.audio.client.effects;

import com.vaadin.addon.audio.client.Effect;
import com.vaadin.addon.audio.client.util.Log;

import elemental.html.AudioContext;

public class BalanceEffect extends Effect {

	@Override
	public void init(AudioContext context) {
		// TODO Auto-generated method stub
		Log.message(this, "init");
	}
	
	// Use an AudioPannerNode to create a balance control
	
}
