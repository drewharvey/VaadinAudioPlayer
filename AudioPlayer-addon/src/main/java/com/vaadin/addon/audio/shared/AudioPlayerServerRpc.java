package com.vaadin.addon.audio.shared;

import com.vaadin.shared.communication.ServerRpc;

// ServerRpc is used to pass events from client to server
public interface AudioPlayerServerRpc extends ServerRpc {
	
	void requestChunk(int chunkID);
	
	void reportPlaybackPosition(int position_millis);
	
	void reportPlaybackStarted();
	
	void reportPlaybackPaused();
	
	void reportPlaybackStopped();
	
}
