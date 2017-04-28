package com.vaadin.addon.audio.client.effects;

import java.util.List;

import com.vaadin.addon.audio.client.Effect;
import com.vaadin.addon.audio.client.webaudio.BiquadFilterNode;
import com.vaadin.addon.audio.client.webaudio.Context;
import com.vaadin.addon.audio.shared.SharedEffectProperty;
import com.vaadin.addon.audio.shared.SharedEffectProperty.PropertyName;
import com.vaadin.addon.audio.shared.util.Log;

import elemental.html.AudioContext;


public class FilterEffect extends Effect {
	
	public FilterEffect(String id) {
		
	}
	
	public FilterEffect(List<SharedEffectProperty> props) {
		for (SharedEffectProperty prop : props) {
			if (prop.getProperty() == PropertyName.FilterType) {
				// make sure we have a value Type
				String type = prop.getValue();
				for (BiquadFilterNode.Type t : BiquadFilterNode.Type.values()) {
					if (t.name().equalsIgnoreCase(type)) {
						setType(t);
						break;
					}
				}
			} else if (prop.getProperty() == PropertyName.Frequency) {
				setFrequency(Float.parseFloat(prop.getValue()));
			} else if (prop.getProperty() == PropertyName.Gain) {
				setGain(Float.parseFloat(prop.getValue()));
			} else if (prop.getProperty() == PropertyName.Q) {
				setQ(Float.parseFloat(prop.getValue()));
			}
		}
	}

	@Override
	public void init(AudioContext context) {
		Log.message(this, "Creating BiquadFilterNode");
		// TODO: provide context via param?
		setAudioNode(Context.get().createBiquadFilter());
	}
	
	@Override
	public BiquadFilterNode getAudioNode() {
		return ((BiquadFilterNode) getAudioNode());
	}
	
	public double getQ() {
		return getAudioNode().getQ();
	}
	
	public void setQ(double q) {
		getAudioNode().setQ(q);
	}
	
	public double getFrequency() {
		return getAudioNode().getFrequency();
	}
	
	public void setFrequency(double frequency) {
		getAudioNode().setFrequency(frequency);
	}
	
	public double getGain() {
		return getAudioNode().getGain();
	}
	
	public void setGain(double gain) {
		getAudioNode().setGain(gain);
	}
	
	public BiquadFilterNode.Type getType() {
		 return getAudioNode().getType();
	}
	
	public void setType(BiquadFilterNode.Type type) {
		getAudioNode().setType(type);
	}
	
	public String toString() {
		return "FilterEffect";
	}

}
