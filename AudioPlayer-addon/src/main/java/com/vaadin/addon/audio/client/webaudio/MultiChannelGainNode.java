package com.vaadin.addon.audio.client.webaudio;


import elemental.html.AudioBuffer;
import elemental.html.AudioContext;

import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: move to effects (this is not an actual Web Audio API object)

/**
 * Created by drewharvey on 6/16/17.
 */
public class MultiChannelGainNode {

    private static final Logger logger = Logger.getLogger("MultiChannelGainNode");

    private Context context;
    private AudioNode inputNode;
    private AudioNode outputNode;
    private ChannelSplitterNode splitterNode;
    private ChannelMergerNode mergerNode;
    private GainNode[] gainNodes;

    public MultiChannelGainNode(Context context) {
        this.context = context;
    }

    /**
     * SourceNode -> Splitter -> GainNode per channel -> Merger.
     * @param sourceNode
     */
    public void connect(BufferSourceNode sourceNode) {
        AudioBuffer buffer = sourceNode.getNativeBuffer();
        if (buffer == null) {
            logger.log(Level.SEVERE, "Failed to connect because audio buffer is null");
            return;
        }
        inputNode = sourceNode;
        outputNode = context.createGainNode();
        splitterNode = context.createChannelSplitter(buffer.getNumberOfChannels());
        mergerNode = context.createChannelMerger(buffer.getNumberOfChannels());
        gainNodes = createChannelGainNodes(buffer.getNumberOfChannels());
        // run source node into the spliter
        inputNode.connect(splitterNode);
        // connect gain nodes to each channel in our splitter
        for (int i = 0; i < gainNodes.length; i++) {
            splitterNode.connect(gainNodes[i], i, 0);
            gainNodes[i].connect(mergerNode, 0, i);
        }
        mergerNode.connect(outputNode);
    }

    public AudioNode getOutputNode() {
        return mergerNode;
    }

    public GainNode getGainNode(int channelIndex) {
        if (channelIndex >= gainNodes.length) {
            return null;
        }
        return gainNodes[channelIndex];
    }

    private GainNode[] createChannelGainNodes(int numberOfChannels) {
        logger.log(Level.SEVERE, "Creating " + numberOfChannels + " gain nodes");
        GainNode[] nodes = new GainNode[numberOfChannels];
        for (int i = 0; i < numberOfChannels; i++) {
            nodes[i] = context.createGainNode();
        }
        return nodes;
    }

}
