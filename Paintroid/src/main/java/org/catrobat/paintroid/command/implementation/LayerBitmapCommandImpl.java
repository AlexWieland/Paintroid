package org.catrobat.paintroid.command.implementation;

import android.graphics.Color;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.command.LayerBitmapCommand;
import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.tools.Tool;

import java.util.LinkedList;

/**
 * Contains all the commands that are to be executed on the layer's bitmap.
 */
public class LayerBitmapCommandImpl implements LayerBitmapCommand {
    private Layer mLayer;

    private LinkedList<Command> mCommandList;
    private LinkedList<Command> mUndoCommandList;


    public LayerBitmapCommandImpl(LayerCommand layerCommand)
    {
        mLayer = layerCommand.getLayer();
        mCommandList = new LinkedList<Command>();
        mUndoCommandList = new LinkedList<Command>();
    }


    @Override
    public Layer getLayer() {
        return mLayer;
    }

    @Override
    public void commitCommandToLayer(Command command)
    {
        synchronized (mCommandList)
        {
            mUndoCommandList.clear();
            mCommandList.addLast(command);
            synchronized (mLayer.getLayerCanvas())
            {
                command.run(mLayer.getLayerCanvas(), mLayer.getBitmap());
            }

            PaintroidApplication.currentTool.resetInternalState(Tool.StateChange.RESET_INTERNAL_STATE);
        }
    }

    @Override
    public synchronized void undo()
    {
        synchronized (mCommandList)
        {
            Command command = mCommandList.removeLast();
            mUndoCommandList.addFirst(command);
            executeAllCommandsOnLayerCanvas();
        }
    }

    @Override
    public synchronized void redo() {
        synchronized (mUndoCommandList) {

            if (mUndoCommandList.size() != 0) {
                Command command = mUndoCommandList.removeFirst();
                mCommandList.addLast(command);

                synchronized (mLayer.getLayerCanvas())
                {
                    command.run(mLayer.getLayerCanvas(), mLayer.getBitmap());
                }

                PaintroidApplication.currentTool.resetInternalState(Tool.StateChange.RESET_INTERNAL_STATE);
            }
        }
    }
    private void executeAllCommandsOnLayerCanvas()
    {
        synchronized (mLayer.getLayerCanvas())
        {
            clearLayerBitmap();
            for (Command command : mCommandList)
            {
                command.run(mLayer.getLayerCanvas(), mLayer.getBitmap());
            }

            PaintroidApplication.currentTool.resetInternalState(Tool.StateChange.RESET_INTERNAL_STATE);
        }
    }

    private void clearLayerBitmap()
    {
        synchronized (mLayer.getLayerCanvas())
        {
            mLayer.getBitmap().eraseColor(Color.TRANSPARENT);
        }
    }
}
