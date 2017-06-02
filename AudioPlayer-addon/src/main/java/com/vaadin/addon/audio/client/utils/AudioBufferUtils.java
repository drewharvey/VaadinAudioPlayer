package com.vaadin.addon.audio.client.utils;


import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.vaadin.addon.audio.client.BufferPlayer;
import com.vaadin.addon.audio.client.JavaScriptPublicAPI;
import elemental.html.AudioBuffer;
import elemental.html.AudioContext;

public class AudioBufferUtils {

    /**
     * Takes AudioBuffer and returns a new AudioBuffer with the pitch change applied.
     * The total length of the buffer will not be affected (length of the audio).
     * @param pitchChange   pitch shift factor (above 1 up, below 1 is down).
     * @param buffer        AudioBuffer input
     * @param context       AudioContext used to create objects
     * @return              AudioBuffer with pitch change applied.
     */
    public static AudioBuffer applyPitchShiftToBuffer(double pitchChange, AudioBuffer buffer, AudioContext context) {
        JavaScriptObject pitchShiftObj = getPitchShiftObject(JavaScriptPublicAPI.NAMESPACE, context);
        return applyPitchShiftToBuffer(pitchChange, buffer, context, pitchShiftObj);
    }

    private static native AudioBuffer applyPitchShiftToBuffer(double pitchChange, AudioBuffer buffer, AudioContext context, JavaScriptObject shifter) /*-{

        var benchmark = "";
        var total = new Date();
        var t;

        var outputBuffer = context.createBuffer(buffer.numberOfChannels, buffer.length, buffer.sampleRate);

        // need to process each channel in the audio buffer
        for (var i = 0; i < buffer.numberOfChannels; i++) {
            // process input data
            var inputData = buffer.getChannelData(i);
            var outputData = outputBuffer.getChannelData(i);
            t = new Date();
            shifter.process(pitchChange, inputData.length, 4, inputData, outputData);
            benchmark += "shifter.process("+i+"): " + (new Date() - t) + "\n\r";
            // write modified data into output buffer channel
//            var outputData = outputBuffer.getChannelData(i);
//            t = new Date();
//            for (var byteIndex = 0; byteIndex < outputData.length; byteIndex++) {
//                outputData[byteIndex] = shifter.outdata[byteIndex];
//            }
//            benchmark += "write output("+i+"): " + (new Date() - t) + "\n\r";
        }

        benchmark += "Total: " + (new Date() - total);

        console.error(benchmark);

        return outputBuffer;
    }-*/;

    private static native JavaScriptObject getPitchShiftObject(String namespace, AudioContext context) /*-{
        var t = new Date();
        // make sure namespace obj is initialized
        $wnd[namespace] = $wnd[namespace] || {};
        // get existing obj or create new one if needed
        $wnd[namespace].pitchShifter = $wnd[namespace].pitchShifter || new $wnd.Pitchshift(2048, context.sampleRate, 'FFT');
        console.error("init Pitchshift: " + (new Date() - t));
        return $wnd[namespace].pitchShifter;
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
