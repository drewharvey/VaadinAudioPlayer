package org.vaadin.addon.audio.shared;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.addon.audio.shared.SharedEffect;

import com.vaadin.shared.AbstractComponentState;

@SuppressWarnings("serial")
public class AudioPlayerState extends AbstractComponentState {

	public final List<ChunkDescriptor> chunks = new ArrayList<ChunkDescriptor>();

	public int chunkTimeMillis;

	public int numChunksPreload;
	
	public int duration;
	
	public final List<SharedEffect> effects = new ArrayList<SharedEffect>();
	
}
