package org.vaadin.addon.audio.server.state;

/**
 * Created by drewharvey on 6/9/17.
 *
 * Inteface defining methods called when a StateChange event gets fired.
 */
public interface StateChangeCallback {

    void playbackPositionChanged(int new_position_millis);

    void playbackStateChanged(PlaybackState new_state);

}
