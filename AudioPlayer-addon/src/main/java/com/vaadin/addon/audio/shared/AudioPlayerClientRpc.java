package com.vaadin.addon.audio.shared;

import java.util.List;

import com.vaadin.shared.communication.ClientRpc;

// ClientRpc is used to pass events from server to client
// For sending information about the changes to component state, use State instead
public interface AudioPlayerClientRpc extends ClientRpc {

	/**
	 * Send data from server to client. In other words, the server will
	 * encode a chunk of data as Base64 and then send it to the client side
	 * as the "encodedData" parameter.
	 * 
	 * @param chunkId numeric ID of chunk the data belongs to
	 * @param compressed set to true if data should be inflated with zlib after base64 decode
	 * @param encodedData Base64 encoded data
	 */
	void sendData(int chunkId, boolean compressed, String encodedData);

	void requestAndCacheAudioChunks(int startTime, int endTime);
	
	void setPlaybackPosition(int position_millis);
	
	void skipPosition(int delta_millis);
	
	void startPlayback();
	
	void pausePlayback();
	
	void resumePlayback();
	
	void stopPlayback();

	void setPlaybackSpeed(double speed_multiplier);
	
	void setVolume(double volume);

	// TODO: merge setVolumes into single method
	void setVolume(double volume, int[] channels);
	
	void setBalance(double balance);
	
	void updateEffects(List<SharedEffect> effects);
	
}
