package org.vaadin.addon.audio.client;

import java.util.List;

import org.vaadin.addon.audio.client.webaudio.AudioNode;
import org.vaadin.addon.audio.client.webaudio.Context;
import org.vaadin.addon.audio.shared.SharedEffectProperty;

/**
 * Base class for a pluggable effect
 */
public abstract class Effect {

	private String id;
	private BufferPlayer player;
	private AudioNode node;
	
	
	/**
	 * Initializes the effects object and creates the required client side
	 * objects.
	 * @param context
	 */
	public abstract void init(Context context);
	
	/**
	 * Sets the effect's property values.
	 * @param props
	 */
	public abstract void setProperties(List<SharedEffectProperty> props);
	
	/**
	 * Creates new instance with the same values as the effect being copied.
	 * The new instance will have a different ID and a new AudioNode will
	 * be created.
	 * @param context
	 * @return new instance
	 */
	public abstract Effect createCopy(Context context);
	
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
	
	public String toString() {
		return "Effect";
	}
	
}
