package org.vaadin.addon.audio.client.utils;


import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import org.vaadin.addon.audio.client.BufferPlayer;
import org.vaadin.addon.audio.shared.util.LogUtils;
import com.vaadin.client.BrowserInfo;

import java.util.logging.Logger;

public class AudioBufferUtils {

    private static final Logger logger = Logger.getLogger("AudioBufferUtils");

    // using Kali library
//    /**
//     * Takes an audio buffer and time warps the buffer by the factor provided.
//     * A factor of 1 will leave the audio unchanged. A lower factor will make the
//     * audio shorter in duration, while a higher factor will make the audio longer.
//     * @param stretchFactor
//     * @param buffer
//     * @param context
//     * @param numChannels
//     * @param quickSeek increases performance but may produce lower quality stretching
//     * @return AudioBuffer with stretch applied
//     */
//    public static native AudioBuffer timeStrechAudioBuffer(double stretchFactor, AudioBuffer buffer, AudioContext context,
//                                                           int numChannels, boolean quickSeek) /*-{
//        var channelData = [];
//        for (var i = 0; i < numChannels; i++) {
//            var inputData = buffer.getChannelData(i);
//
//            var numInputFrames = inputData.length / numChannels;
//            var bufsize = 4096 * numChannels;
//
//            // Create a Kali instance and initialize it
//            var kali = new $wnd.Kali(numChannels);
//            kali.setup(context.sampleRate, stretchFactor, quickSeek);
//
//            // Create an array for the stretched output
//            var completed = new Float32Array(Math.floor((numInputFrames / stretchFactor) * numChannels + 1));
//
//            var inputOffset = 0;
//            var completedOffset = 0;
//            var loopCount = 0;
//            var flushed = false;
//
//            while (completedOffset < completed.length) {
//                if (loopCount % 100 == 0) {
//                    console.log("Stretching", completedOffset / completed.length);
//                }
//
//                // Read stretched samples into our output array
//                completedOffset += kali.output(completed.subarray(completedOffset, Math.min(completedOffset + bufsize, completed.length)));
//
//                if (inputOffset < inputData.length) { // If we have more data to write, write it
//                    var dataToInput = inputData.subarray(inputOffset, Math.min(inputOffset + bufsize, inputData.length));
//                    inputOffset += dataToInput.length;
//
//                    // Feed Kali samples
//                    kali.input(dataToInput);
//                    kali.process();
//                } else if (!flushed) { // Flush if we haven't already
//                    kali.flush();
//                    flushed = true;
//                }
//
//                loopCount++;
//            }
//
//            channelData.push(completed);
//        }
//
//        // create new audio buffer with warped audio
//        var outputAudioBuffer = context.createBuffer(numChannels, channelData[0].length, context.sampleRate);
//        for (var i = 0; i < channelData.length; i++) {
//            outputAudioBuffer.getChannelData(i).set(channelData[i]);
//        }
//        return outputAudioBuffer;
//     }-*/;


    // using buffered-pv.js
//    public static native AudioBuffer timeStrechAudioBuffer(double stretchFactor, AudioBuffer buffer, AudioContext context,
//                                                           int numChannels, boolean quickSeek) /*-{
//
//        var warpedBuffer = context.createBuffer(numChannels, buffer.length, buffer.sampleRate);
//
//        var bufferedPv = new $wnd.BufferedPV();
//        bufferedPv.set_audio_buffer(buffer);
//        bufferedPv.alpha = stretchFactor;
//        console.log(bufferedPv);
//
//        bufferedPv.process(warpedBuffer);
//        console.log(warpedBuffer);
//        return warpedBuffer;
//    }-*/;


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
        // Different browsers handle the different crossfade implementations differently.
        // - Chrome works well with either
        // - Firefox will stall with the loop version, and basically will not crossfade
        // - Safari works with both, but the async version causes a wobble effect when used with PitchShiftNode (class)
        // - Edge/IE works with both, but the async version causes a wobble effect when used with PitchShiftNode (class)
        if (BrowserInfo.get().isChrome() || BrowserInfo.get().isFirefox()) {
            logger.info(LogUtils.prefix("using crossfade for CHROME OR FIREFOX"));
            crossFadePlayersAsync(currentPlayer, prevPlayer, currentPlayerPlayOffset, targetGain, fadeTime);
        } else {
            logger.info(LogUtils.prefix("using crossfade for SAFARI OR IE"));
            crossFadePlayersLoop(currentPlayer, prevPlayer, currentPlayerPlayOffset, targetGain, fadeTime);
        }
    }

    /**
     * Asynch implementation of the equal power crossfade between two BufferPlayer objects.
     * Uses a repeating timer (setInterval in JS) to run updates to gain levels.
     *
     * Recommended for Chrome and Firefox.
     */
    private static void crossFadePlayersAsync(final BufferPlayer currentPlayer, final BufferPlayer prevPlayer,
                                        final int currentPlayerPlayOffset, final double targetGain, final int fadeTime) {

        // if we have a prev player then we fade it out and fade our new player in
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                // setup the incoming player
                if (currentPlayer != null) {
                    currentPlayer.setVolume(0);
                    currentPlayer.play(currentPlayerPlayOffset);
                }
                // t value is used by crossfade calculation (t will increase from 0 to 1)
                final double[] t = new double[1];
                t[0] = 0;
                // set repeat options and calculate variables
                final int repeatTime = 5;
                final double changePerMs = 1d / fadeTime;
                // track amount of time that passes each loop to incriment the t value
                final Duration duration = new Duration();
                final int[] lastTime = new int[1];
                lastTime[0] = 0;
                // create repeating function
                final Timer crossfadeLoop = new Timer() {
                    @Override
                    public void run() {
                        double[] gains = getCrossFadeValues(t[0]);
                        // max and min volumes and terminate the loop
                        if (duration.elapsedMillis() >= fadeTime) {
                            Logger.getLogger("AudioBufferUtils").info(LogUtils.prefix("total time: " + duration.elapsedMillis()));
                            if (currentPlayer != null) {
                                currentPlayer.setVolume(targetGain);
                            }
                            if (prevPlayer != null) {
                                prevPlayer.setVolume(0);
                                prevPlayer.stop();
                            }
                            cancel();
                            return;
                        }
                        // update player volumes with crossfade values
                        if (prevPlayer != null) {
                            prevPlayer.setVolume(gains[0] * targetGain);
                        }
                        if (currentPlayer != null) {
                            currentPlayer.setVolume(gains[1] * targetGain);
                        }
                        // increment crossfade index value
                        t[0] += (changePerMs * (duration.elapsedMillis() - lastTime[0]));
                        lastTime[0] = duration.elapsedMillis();
                    }
                };
                crossfadeLoop.scheduleRepeating(repeatTime);
            }
        });
    }

    /**
     * Loop (blocking) implementation of the equal power crossfade between two BufferPlayer objects.
     * Uses a standard for loop with elapsed time checking to set gain levels.
     *
     * Recommended for Safari and Edge/IE.
     */
    private static void crossFadePlayersLoop(final BufferPlayer currentPlayer, final BufferPlayer prevPlayer,
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
                     prevPlayer.stop();
                 }
                 // TODO: figure out why wobbling happens with PitchShiftNode and fix it instead of this hack
                 // need to reset the playback speed to stop the wobbling effect for some browsers (Safari, Edge/IE)
                 Timer t = new Timer() {
                     @Override
                     public void run() {
                         currentPlayer.setPlaybackSpeed(currentPlayer.getPlaybackSpeed());
                     }
                 };
                 t.schedule(10);
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
