package org.vaadin.addon.audio.client.effects;

import java.util.List;

import org.vaadin.addon.audio.client.Effect;
import org.vaadin.addon.audio.client.webaudio.Context;
import org.vaadin.addon.audio.shared.SharedEffectProperty;
import org.vaadin.addon.audio.shared.util.Log;

import elemental.html.AudioPannerNode;

public class BalanceEffect extends Effect {
	
	private double position = 0;

	@Override
	public void init(Context context) {
		Log.message(this, "Creating AudioPannerNode");

	}
	
	@Override
	public void setProperties(List<SharedEffectProperty> props) {
		
	}
	
	@Override
	public BalanceEffect createCopy(Context context) {
		BalanceEffect e = new BalanceEffect();
		e.init(context);
		return e;
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
