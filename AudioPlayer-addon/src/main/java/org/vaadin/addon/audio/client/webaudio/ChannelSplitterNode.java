package org.vaadin.addon.audio.client.webaudio;

import elemental.html.AudioContext;

/**
 * Created by drewharvey on 6/16/17.
 */
public class ChannelSplitterNode extends AudioNode {

    protected ChannelSplitterNode(AudioContext ctx, int numChannels) {
        super(createChannelSplitterNode(ctx, numChannels));
    }

    private static final native elemental.html.AudioNode
    createChannelSplitterNode(AudioContext ctx, int numChannels) /*-{
        return ctx.createChannelSplitter(numChannels);
    }-*/;

    @Override
    public String toString() {
        String str = "";
        str += "ChannelSplitterNode:";
        return str;
    }
}
