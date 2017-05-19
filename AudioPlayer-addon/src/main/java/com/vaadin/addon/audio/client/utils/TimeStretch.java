package com.vaadin.addon.audio.client.utils;


import elemental.html.AudioBuffer;
import elemental.html.AudioContext;

public class TimeStretch {

    /**
     * Takes an audio buffer and time warps the buffer by the factor provided.
     * A factor of 1 will leave the audio unchanged. A lower factor will make the
     * audio shorter in duration, while a higher factor will make the audio longer.
     * @param stretchFactor
     * @param buffer
     * @param context
     * @param numChannels
     * @param quickSeek increases performance but may produce lower quality stretching
     * @return AudioBuffer with stretch applied
     */
    public static native AudioBuffer strechAudioBuffer(double stretchFactor, AudioBuffer buffer, AudioContext context,
                                                       int numChannels, boolean quickSeek) /*-{
        var inputData = buffer.getChannelData(0);

        var numInputFrames = inputData.length / numChannels;
        var bufsize = 4096 * numChannels;

        // Create a Kali instance and initialize it
        var kali = new $wnd.Kali(numChannels);
        kali.setup(context.sampleRate, stretchFactor, quickSeek);

        // Create an array for the stretched output
        var completed = new Float32Array(Math.floor((numInputFrames / stretchFactor) * numChannels + 1));

        var inputOffset = 0;
        var completedOffset = 0;
        var loopCount = 0;
        var flushed = false;

        while (completedOffset < completed.length) {
            if (loopCount % 100 == 0) {
                console.log("Stretching", completedOffset / completed.length);
            }

            // Read stretched samples into our output array
            completedOffset += kali.output(completed.subarray(completedOffset, Math.min(completedOffset + bufsize, completed.length)));

            if (inputOffset < inputData.length) { // If we have more data to write, write it
                var dataToInput = inputData.subarray(inputOffset, Math.min(inputOffset + bufsize, inputData.length));
                inputOffset += dataToInput.length;

                // Feed Kali samples
                kali.input(dataToInput);
                kali.process();
            } else if (!flushed) { // Flush if we haven't already
                kali.flush();
                flushed = true;
            }

            loopCount++;
        }

        // create new audio buffer with warped audio
        var outputAudioBuffer = context.createBuffer(1, completed.length, context.sampleRate);
        outputAudioBuffer.getChannelData(0).set(completed);
        return outputAudioBuffer;
     }-*/;

}
