package org.vaadin.addon.audio.server.state;

import java.util.HashMap;

// TODO: this is way too general and should be refactored

/**
 * Created by drewharvey on 6/20/17.
 *
 * General interface used to handle changes on all AudioPlayer values.
 */
public interface VolumeChangeCallback {
    void onVolumeChange(double volume, double[] channelVolumes);
}
