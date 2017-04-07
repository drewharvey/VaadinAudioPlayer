package com.vaadin.addon.audio.server;

import java.util.UUID;

import com.vaadin.addon.audio.shared.SharedEffect;

public abstract class Effect {

	private UUID id;
	
	public Effect() {
		UUID.randomUUID();
	}
	
	public String getID() {
		return id.toString();
	}
	
	public abstract SharedEffect getSharedEffectObject();
	
}
