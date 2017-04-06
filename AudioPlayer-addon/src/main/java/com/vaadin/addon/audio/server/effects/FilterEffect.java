package com.vaadin.addon.audio.server.effects;

import com.vaadin.addon.audio.server.Effect;

public class FilterEffect extends Effect {
	
	public enum Type {
		LOWPASS,
		HIGHPASS
	}
	
	private double q;
	private double frequency;
	private double gain;
	private Type type;
	
	public double getQ() {
		return q;
	}
	
	public void setQ(double q) {
		this.q = q;
	}
	
	public double getFrequency() {
		return frequency;
	}
	
	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
	
	public double getGain() {
		return gain;
	}
	
	public void setGain(double gain) {
		this.gain = gain;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
}
