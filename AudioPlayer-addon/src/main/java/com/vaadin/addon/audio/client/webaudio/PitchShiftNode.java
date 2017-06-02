package com.vaadin.addon.audio.client.webaudio;


import com.google.gwt.core.client.JavaScriptObject;
import elemental.html.AudioContext;

public class PitchShiftNode {

    JavaScriptObject jungleObject;

    public PitchShiftNode(AudioContext ctx) {
        jungleObject = createJungleObject(ctx);
    }

    private static final native JavaScriptObject createJungleObject(elemental.html.AudioContext ctx) /*-{
        var j = new $wnd.Jungle(ctx);
        j.setPitchOffset(0);
        return j;
    }-*/;

    public void setPitchOffset(double pitchOffset) {
        setPitchOffset(pitchOffset, jungleObject);
    }

    public native void setPitchOffset(double pitchOffset, JavaScriptObject jungleObject) /*-{
        jungleObject.setPitchOffset(pitchOffset);
    }-*/;

    public void normalizePitchBasedOnPlaybackSpeed(double playbackSpeed) {
        // I had to manually figure out these values are find the most fitting curve equation.
        // The jungle.js file has no info on how to use it.
        /*
            speed | pitch offset
            --------------------
            0.5   | 2
            0.75  | 0.75
            1     | 0
            1.5   | -0.7
            2     | -1
            3     | -1.35
        */
        double pitchOffset = -1.725829 + (7.883079 - -1.725829)/(1 + Math.pow(playbackSpeed/0.3699, 1.51112));
        setPitchOffset(pitchOffset);
    }

    public elemental.html.AudioNode getInput() {
        return getInput(jungleObject);
    }

    public native elemental.html.AudioNode getInput(JavaScriptObject jungleObject) /*-{
        return jungleObject.input;
    }-*/;

    public elemental.html.AudioNode getOutput() {
        return getOutput(jungleObject);
    }

    public native elemental.html.AudioNode getOutput(JavaScriptObject jungleObject) /*-{
        return jungleObject.output;
    }-*/;

    /**
     * Connect another AudioNode to this AudioNode
     * @param other another AudioNode
     */
    public void connect(AudioNode other) {
        connect(getOutput(), other.getNativeNode());
    }

    private static final native void connect(elemental.html.AudioNode src, elemental.html.AudioNode dst) /*-{
        src.connect(dst,0, 0);
    }-*/;

    /**
     * Disconnect this AudioNode from all outputs.
     */
    public void disconnect() {
        if (getNumberOfOutputs() > 0) {
            disconnect(getOutput());
        }
    }

    private static final native void disconnect(elemental.html.AudioNode src) /*-{
        src.disconnect();
    }-*/;

    /**
     * Disconnect another AudioNode from this AudioNode
     * @param other another AudioNode
     */
    public void disconnect(AudioNode other) {
        disconnect(getOutput(), other.getNativeNode());
    }

    // See https://developer.mozilla.org/en-US/docs/Web/API/AudioNode/disconnect
    private static final native void disconnect(
            elemental.html.AudioNode src,
            elemental.html.AudioNode dst) /*-{
        src.disconnect(dst,0, 0);
    }-*/;

    public int getNumberOfInputs() {
        return getNumberOfInputs(getInput());
    }

    private static final native int getNumberOfInputs(elemental.html.AudioNode src) /*-{
        return src.numberOfInputs;
    }-*/;

    public int getNumberOfOutputs() {
        return getNumberOfOutputs(getOutput());
    }

    private static final native int getNumberOfOutputs(elemental.html.AudioNode src) /*-{
        return src.numberOfOutputs;
    }-*/;


}
