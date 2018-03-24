package org.vaadin.addon.audio.server.state;

/**
 * Created by drewharvey on 6/9/17.
 */
public interface StreamStateCallback {
    public void onStateChanged(StreamState newState);
}
