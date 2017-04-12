package com.vaadin.addon.audio.client.effects;

import com.vaadin.addon.audio.client.Effect;
import com.vaadin.addon.audio.shared.util.Log;

import elemental.html.AudioContext;
import elemental.html.AudioPannerNode;

public class BalanceEffect extends Effect {
	
	private double position = 0;

	@Override
	public void init(AudioContext context) {
		Log.message(this, "Creating AudioPannerNode");
		setAudioNode(context.createPanner());
	}
	
	/**
	 * Sets the left/right position of the audio. 
	 * The value range is from -1 to 1.
	 * To play equal between left and right use 0.
	 * To only play the left use -1.
	 * To only play the right use 1.
	 * @param pos
	 */
	public void setBalance(double pos) {
		position = pos;
		((AudioPannerNode) getAudioNode()).setPosition((float) pos, 0, 0);
	}
	
	public double getBalance() {
		return position;
	}
	
	public String toString() {
		return "BalanceEffect";
	}

}
