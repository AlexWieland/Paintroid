package org.catrobat.paintroid.command.implementation;

import org.catrobat.paintroid.command.LayerBitmapCommandManager;
import org.catrobat.paintroid.tools.Layer;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Describes Layer command. It can contain either simple layer on which some operation is being
 * performed, or list of command managers for merged layers.
 */
public class LayerCommand
{
    private Layer mCurrentLayer;
    private ArrayList<LayerBitmapCommandManager> mLayersToMerge;

    public LayerCommand(Layer currentLayer)
    {
        mCurrentLayer = currentLayer;
    }

    public LayerCommand(ArrayList<LayerBitmapCommandManager> layersToMerge)
    {
        mLayersToMerge = layersToMerge;
    }

    public Layer getCurrentLayer() {
        return mCurrentLayer;
    }

    public ArrayList<LayerBitmapCommandManager> getLayersToMerge() {
        return mLayersToMerge;
    }
}
