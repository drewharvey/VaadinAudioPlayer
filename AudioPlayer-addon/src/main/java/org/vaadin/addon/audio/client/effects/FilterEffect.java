package org.vaadin.addon.audio.client.effects;

import java.util.List;
import java.util.logging.Logger;

import org.vaadin.addon.audio.client.Effect;
import org.vaadin.addon.audio.shared.util.LogUtils;
import org.vaadin.addon.audio.client.webaudio.BiquadFilterNode;
import org.vaadin.addon.audio.client.webaudio.Context;
import org.vaadin.addon.audio.shared.SharedEffectProperty;
import org.vaadin.addon.audio.shared.SharedEffectProperty.PropertyName;
import org.vaadin.addon.audio.shared.util.Log;


public class FilterEffect extends Effect {
	

	@Override
	public void init(Context context) {
		Log.message(this, "Creating BiquadFilterNode");
		// TODO: provide context via param?
		setAudioNode(context.createBiquadFilter());
	}
	
	@Override
	public void setProperties(List<SharedEffectProperty> props) {
		if (getAudioNode() == null) {
			return;
		}
		for (SharedEffectProperty prop : props) {
			if (prop.getProperty() == PropertyName.FilterType) {
				String type = prop.getValue();
				setType(prop.getValue());
			} else if (prop.getProperty() == PropertyName.Frequency) {
				setFrequency(Double.parseDouble(prop.getValue()));
			} else if (prop.getProperty() == PropertyName.Gain) {
				setGain(Double.parseDouble(prop.getValue()));
			} else if (prop.getProperty() == PropertyName.Q) {
				setQ(Double.parseDouble(prop.getValue()));
			}
		}
	}
	
	@Override
	public FilterEffect createCopy(Context context) {
		FilterEffect e = new FilterEffect();
		e.init(context);
		e.setFrequency(getFrequency());
		e.setGain(getGain());
		e.setPlayer(getPlayer());
		e.setQ(getQ());
		e.setType(getType());
		return e;
	}
	
	public double getQ() {
		return ((BiquadFilterNode) getAudioNode()).getQ();
	}
	
	public void setQ(double q) {
		((BiquadFilterNode) getAudioNode()).setQ(q);
	}
	
	public double getFrequency() {
		return ((BiquadFilterNode) getAudioNode()).getFrequency();
	}
	
	public void setFrequency(double frequency) {
		((BiquadFilterNode) getAudioNode()).setFrequency(frequency);
	}
	
	public double getGain() {
		return ((BiquadFilterNode) getAudioNode()).getGain();
	}
	
	public void setGain(double gain) {
		((BiquadFilterNode) getAudioNode()).setGain(gain);
	}
	
	public BiquadFilterNode.Type getType() {
		 return ((BiquadFilterNode) getAudioNode()).getType();
	}
	
	public void setType(BiquadFilterNode.Type type) {
		((BiquadFilterNode) getAudioNode()).setType(type);
	}
	
	public void setType(String type) {
		Logger.getLogger("FilterEffect").info(LogUtils.prefix("setType: " + type));
		// TODO: check if type is valid before setting
		setType(BiquadFilterNode.Type.valueOf(type.toUpperCase()));
	}
	
	@Override
	public String toString() {
		String str = "";
		str += "FilterEffect:\r\n";
		str += " Gain: " + getGain() + "\n\r";
		str += " Frequency: " + getFrequency() + "\n\r";;
		str += " Q: " + getQ() + "\n\r";;
		str += " Type: " + getType().name();
		return str;
	}

}
