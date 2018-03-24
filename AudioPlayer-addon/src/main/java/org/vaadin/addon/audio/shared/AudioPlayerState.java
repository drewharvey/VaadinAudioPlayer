package org.vaadin.addon.audio.shared;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.shared.AbstractComponentState;

@SuppressWarnings("serial")
public class AudioPlayerState extends AbstractComponentState {

	public final List<ChunkDescriptor> chunks = new ArrayList<ChunkDescriptor>();

	public int chunkTimeMillis;

	public int numChunksPreload;
	
	public int duration;
	
	public int reportPositionRepeatTime = 500;
	
	public final List<SharedEffect> effects = new ArrayList<SharedEffect>();
	
}
