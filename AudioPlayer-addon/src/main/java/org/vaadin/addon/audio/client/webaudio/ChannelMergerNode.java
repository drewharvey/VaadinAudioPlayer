package org.vaadin.addon.audio.client.webaudio;

import elemental.html.AudioContext;

/**
 * Created by drewharvey on 6/16/17.
 */
public class ChannelMergerNode extends AudioNode {

    protected ChannelMergerNode(AudioContext ctx, int numChannels) {
        super(createChannelMergerNode(ctx, numChannels));
    }

    private static final native elemental.html.AudioNode
    createChannelMergerNode(AudioContext ctx, int numChannels) /*-{
        return ctx.createChannelMerger(numChannels);
    }-*/;

    @Override
    public String toString() {
        String str = "";
        str += "ChannelMergerNode:";
        return str;
    }
}
