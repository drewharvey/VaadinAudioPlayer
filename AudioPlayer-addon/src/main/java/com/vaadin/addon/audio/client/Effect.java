package com.vaadin.addon.audio.client;

import elemental.html.AudioContext;
import elemental.html.AudioNode;

/**
 * Base class for a pluggable effect
 */
public abstract class Effect {

	private BufferPlayer player;
	private AudioNode node;

	protected void setPlayer(BufferPlayer p) {
		if(player != null) {
			player.removeEffect(this);
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
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " @" + this.hashCode();
	}
	
}
