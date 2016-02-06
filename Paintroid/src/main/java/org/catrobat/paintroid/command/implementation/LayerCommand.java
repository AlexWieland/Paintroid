package org.catrobat.paintroid.command.implementation;

import org.catrobat.paintroid.tools.Layer;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Describes layer command container. It can contain either layer on which some operation is being
 * performed, or list of layers to be merged.
 */
public class LayerCommand
{
    private Layer mCurrentLayer;
    private ArrayList<Integer> mLayersToMerge;

    public LayerCommand(Layer currentLayer)
    {
        mCurrentLayer = currentLayer;
    }

    public LayerCommand(ArrayList<Integer> layersToMerge)
    {
        mLayersToMerge = layersToMerge;
    }

    public Layer getCurrentLayer() {
        return mCurrentLayer;
    }

    public ArrayList<Integer> getLayersToMerge() {
        return mLayersToMerge;
    }
}
