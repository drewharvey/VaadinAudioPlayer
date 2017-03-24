package com.vaadin.addon.audio.client.effects;

import com.vaadin.addon.audio.client.Effect;
import com.vaadin.addon.audio.client.util.Log;

import elemental.html.AudioContext;

public class VolumeEffect extends Effect {

	// TODO: probably just wrap a gain node - customer can tell more
	// They might need to have some kind of volume doubler or something
	
	@Override
	public void init(AudioContext context) {
		Log.message(this, "init");
	}

}
