package com.vaadin.addon.audio.demo;

import com.vaadin.ui.Slider;

/**
 * Standard Vaadin Slider that exposes a setValueSecretly method
 * which allows the value to be set without triggering value change
 * listeners. This is important because a position slider's value
 * should be synchronized with the Audio Player's position, but 
 * also needs to allow for users to move and set the position.
 *
 */
public class AudioPositionSlider extends Slider {
	
	public AudioPositionSlider(String name) {
		super(name);
	}
	
	public void setValueSecretly(double newValue) {
	  setInternalValue(newValue);
	  requestRepaint();
	}
	
}
