package org.vaadin.addon.audio.server.state;

/**
 * Created by drewharvey on 6/9/17.
 *
 * States of a Stream.
 */
public enum StreamState {
    IDLE,
    READING,
    ENCODING,
    COMPRESSING,
    SERIALIZING
}
