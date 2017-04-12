package com.vaadin.addon.audio.client;

import elemental.html.AudioContext;
import elemental.html.AudioNode;

/**
 * Base class for a pluggable effect
 */
public abstract class Effect {

	private String id;
	private BufferPlayer player;
	private AudioNode node;
	
	public void setID(String id) {
		this.id = id;
	}
	
	public String getID() {
		return id;
	}

	protected void setPlayer(BufferPlayer p) {
		if(player != null) {
			//player.removeEffect(this);
		}
		player = p;
	}
	
	public BufferPlayer getPlayer() {
		return player;
	}
	
	protected void setAudioNode(AudioNode node) {
		this.node = node;
	}
	
	public AudioNode getAudioNode() {
		return node;
	}
	
	public abstract void init(AudioContext context);
	
	public String toString() {
		return "Effect";
	}
	
}
