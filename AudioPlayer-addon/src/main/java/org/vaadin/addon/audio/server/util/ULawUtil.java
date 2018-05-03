package org.vaadin.addon.audio.server.util;


import com.sun.media.sound.UlawCodec;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.vaadin.addon.audio.shared.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

// TODO: create a ULawEncoder class and include the decode method there?

/**
 * Classed used to manipulate U-Law encoded audio files.
 */
public class ULawUtil {

    /**
     * Takes AudioInputStream with U-Law encoded data, converts it to PCM Signed data and
     * wraps it in a ByteBuffer.
     * @param uLawInputStream	AudioInputStream containing U-Law encoded data.
     * @return ByteBuffer containing PCM Signed data.
     * @throws IOException
     */
    public static ByteBuffer decodeULawToPcm(AudioInputStream uLawInputStream) throws IOException {
        System.out.println("Decoding u-law to signed pcm data");
        ByteBuffer buffer;
        UlawCodec codec = new UlawCodec();
        // decode u-law audio to pcm
        AudioInputStream convertedStream
                = codec.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, uLawInputStream);
        // save the decoded file to a temp file just until we can read into a byte buffer
        File tmp = File.createTempFile("tempAudioFile", ".wav");
        AudioSystem.write(convertedStream, AudioFileFormat.Type.WAVE, tmp);
        // wrap byte[] in ByteBuffer
        byte[] bytes = Files.readAllBytes(Paths.get(tmp.getPath()));
        buffer = ByteBuffer.wrap(bytes);
        // delete temp file
        tmp.delete();

        // TODO: get decoding directly to byte[] working
        // this is writing the correct number of bytes into the byte[], but
        // non of the audio format info is being read later
        //ByteArrayOutputStream output = new ByteArrayOutputStream();
        //AudioSystem.write(convertedStream, AudioFileFormat.Type.WAVE, output);
        //byte[] convertedBytes = new byte[output.size()];
        //output.write(convertedBytes);
//							fileBytes.clear();
//							fileBytes = ByteBuffer.wrap(convertedBytes);
        return buffer;
    }
}
