package com.vaadin.addon.audio.shared;

import com.vaadin.shared.communication.ClientRpc;

// ClientRpc is used to pass events from server to client
// For sending information about the changes to component state, use State instead
public interface AudioPlayerClientRpc extends ClientRpc {

	void dataAvailable(int chunkId);
	
	void setPlaybackPosition(int position_millis);
	
	void skipPosition(int delta_millis);
	
	void startPlayback();
	
	void pausePlayback();
	
	void resumePlayback();
	
	void stopPlayback();

	void setPlaybackSpeed(double speed_multiplier);
	
	void setVolume(double volume);
	
}
