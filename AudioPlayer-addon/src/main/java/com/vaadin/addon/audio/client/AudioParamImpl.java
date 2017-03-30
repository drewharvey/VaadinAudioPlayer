package com.vaadin.addon.audio.client;

import elemental.html.Float32Array;

public class AudioParamImpl implements elemental.html.AudioParam {
	
	float defaultValue = 0f;
	float maxValue = 0f;
	float minValue = 0f;
	String name;
	int units = 0;
	float value = 0f;

	@Override
	public float getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(float defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public float getMaxValue() {
		return maxValue;
	}
	
	public void setMaxValue(float maxValue) {
		this.maxValue = maxValue;
	}

	@Override
	public float getMinValue() {
		return minValue;
	}
	
	public void setMinValue(float minValue) {
		this.minValue = minValue;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getUnits() {
		return units;
	}
	
	public void setUnits(int units) {
		this.units = units;
	}

	@Override
	public float getValue() {
		return value;
	}

	@Override
	public void setValue(float value) {
		this.value = value;
	}

	@Override
	public void cancelScheduledValues(float startTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exponentialRampToValueAtTime(float value, float time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void linearRampToValueAtTime(float value, float time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTargetValueAtTime(float targetValue, float time, float timeConstant) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setValueAtTime(float value, float time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setValueCurveAtTime(Float32Array values, float time, float duration) {
		// TODO Auto-generated method stub

	}

}
