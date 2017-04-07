package com.vaadin.addon.audio.shared;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.addon.audio.shared.SharedEffect;
import com.vaadin.shared.AbstractComponentState;

@SuppressWarnings("serial")
public class AudioPlayerState extends AbstractComponentState {

	// NOTE: set this once and then don't touch it
	public final List<ChunkDescriptor> chunks = new ArrayList<ChunkDescriptor>();
	
	public List<SharedEffect> effects = new ArrayList<SharedEffect>();
	
}
