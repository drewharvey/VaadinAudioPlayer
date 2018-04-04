package org.vaadin.addon.audio.server;

import java.util.UUID;

import org.vaadin.addon.audio.shared.SharedEffect;

public abstract class Effect {

	private UUID id;
	
	public Effect() {
		id = UUID.randomUUID();
	}
	
	public String getID() {
		return id.toString();
	}
	
	public abstract SharedEffect getSharedEffectObject();
	
}
