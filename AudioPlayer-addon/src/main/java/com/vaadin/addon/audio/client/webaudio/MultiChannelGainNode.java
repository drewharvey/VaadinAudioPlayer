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
        outputNode = context.createGainNode();
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
        // create nodes
        if (splitterNode == null || splitterNode.getNumberOfOutputs() != buffer.getNumberOfChannels()) {
            splitterNode = context.createChannelSplitter(buffer.getNumberOfChannels());
        }
        if (mergerNode == null || mergerNode.getNumberOfInputs() != buffer.getNumberOfChannels()) {
            mergerNode = context.createChannelMerger(buffer.getNumberOfChannels());
        }
        if (gainNodes == null || gainNodes.length != buffer.getNumberOfChannels()) {
            gainNodes = createChannelGainNodes(buffer.getNumberOfChannels());
        }
        // run source node into the spliter
        inputNode.connect(splitterNode);
        // connect gain nodes to each channel in our splitter
        for (int i = 0; i < gainNodes.length; i++) {
            splitterNode.connect(gainNodes[i], i, 0);
            gainNodes[i].connect(mergerNode, 0, i);
        }
        mergerNode.connect(outputNode);
    }

    public double getGain(int channelIndex) {
        double gain = 0;
        if (channelIndex < gainNodes.length) {
            gain = gainNodes[channelIndex].getGain();
        }
        return gain;
    }

    public void setGain(double gain, int channelIndex) {
        if (channelIndex < gainNodes.length) {
            gainNodes[channelIndex].setGain(gain);
        }
    }

    public void setGainOnAllChannels(double gain) {
        for (GainNode node : gainNodes) {
            node.setGain(gain);
        }
    }

    public AudioNode getOutputNode() {
        return mergerNode;
    }

    public int getNumberOfChannels() {
        if (gainNodes != null) {
            return gainNodes.length;
        }
        return 0;
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
