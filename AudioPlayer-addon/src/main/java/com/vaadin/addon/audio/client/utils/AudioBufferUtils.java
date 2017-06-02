package com.vaadin.addon.audio.client.utils;


import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.Scheduler;
import com.vaadin.addon.audio.client.BufferPlayer;
import elemental.html.AudioBuffer;
import elemental.html.AudioContext;

public class AudioBufferUtils {

    public static native AudioBuffer pitchShiftBuffer(double pitchChange, AudioBuffer buffer, AudioContext context) /*-{

        var outputBuffer = context.createBuffer(buffer.numberOfChannels, buffer.length, buffer.sampleRate);

        // reuse same instance of PitchShifter obj
        var shifter = $wnd.shifter || new $wnd.Pitchshift(2048, context.sampleRate, 'FFT');

        // need to process each channel in the audio buffer
        for (var i = 0; i < buffer.numberOfChannels; i++) {
            // process input data
            var inputData = buffer.getChannelData(i);
            shifter.process(pitchChange, inputData.length, 4, inputData);
            // write modified data into output buffer channel
            var outputData = outputBuffer.getChannelData(i);
            for (var byteIndex = 0; byteIndex < outputData.length; byteIndex++) {
                outputData[byteIndex] = shifter.outdata[byteIndex];
            }
        }

        return outputBuffer;
    }-*/;

    /**
     * Uses a equal power crossfade curve to blend two BufferPlayer audios together.
     * After the crossfade the currentPlayer will be playing and the prevPlayer will
     * stop playing.
     * @param currentPlayer BufferPlayer that is fading in
     * @param prevPlayer    BufferPlayer that is fading out
     * @param currentPlayerPlayOffset   Offset in milliseconds to start playback for currentPlayer. Usually this is 0.
     * @param targetGain    Final gain level for the currentPlayer
     * @param fadeTime      Total time the fade should take
     */
    public static void crossFadePlayers(final BufferPlayer currentPlayer, final BufferPlayer prevPlayer,
                                  final int currentPlayerPlayOffset, final double targetGain, final int fadeTime) {

        // if we have a prev player then we fade it out and fade our new player in
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                if (currentPlayer != null) {
                    currentPlayer.setVolume(0);
                    currentPlayer.play(currentPlayerPlayOffset);
                }

                double increment = 1d / fadeTime;
                int lastTime = 0;
                Duration duration = new Duration();

                for (double t = 0; t < 1; ) {
                    // only update gain if at least a millisecond has passed
                    if (lastTime != duration.elapsedMillis()) {
                        lastTime = duration.elapsedMillis();
                        double[] gains = getCrossFadeValues(t);
                        if (prevPlayer != null) {
                            prevPlayer.setVolume(gains[0] * targetGain);
                        }
                        if (currentPlayer != null) {
                            currentPlayer.setVolume(gains[1] * targetGain);
                        }
                        // increment crossfade index value
                        t += increment;
                    }
                }
                // make sure we are at max/min volumes by the end
                if (currentPlayer != null) {
                    currentPlayer.setVolume(targetGain);
                }
                if (prevPlayer != null) {
                    prevPlayer.setVolume(0);
                }
            }
        });
    }

    /**
     * Takes a time value (between 0 and 1) and calculates
     * the volume level following a equal power crossfade curve.
     *
     * @param t
     * @return volume (between 0 and 1)
     */
    private static double[] getCrossFadeValues(double t) {
        double[] val = new double[2];
        // decreasing
        val[0] = Math.cos(t * 0.5 * Math.PI);
        // increasing
        val[1] = Math.cos((1.0 - t) * 0.5 * Math.PI);
        return val;
    }

}
