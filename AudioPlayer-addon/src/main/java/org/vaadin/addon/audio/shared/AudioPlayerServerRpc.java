package org.vaadin.addon.audio.shared;

import com.vaadin.shared.communication.ServerRpc;

import java.util.HashMap;

// ServerRpc is used to pass events from client to server
public interface AudioPlayerServerRpc extends ServerRpc {

	/**
	 * Requests a chunk of audio data from the server.
	 * @param chunkID
	 */
	void requestChunk(int chunkID);

	/**
	 * Reports if the play position changes on the client side player.
	 * @param position_millis
	 */
	void reportPlaybackPosition(int position_millis);

	/**
	 * Reports if the playback started on the client side player.
	 */
	void reportPlaybackStarted();

	/**
	 * Reports if the playback was paused on the client side player.
	 */
	void reportPlaybackPaused();

	/**
	 * Reports if the playback was stopped on the client side player.
	 */
	void reportPlaybackStopped();

	/**
	 * Reports if volume changes on the client side player.
	 * @param volume
	 * @param channelVolumes
	 */
	void reportVolumeChange(double volume, double[] channelVolumes);
	
}
