package org.catrobat.paintroid.command.implementation;

import android.graphics.Color;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.command.LayerBitmapCommandManager;
import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.tools.Tool;
import org.catrobat.paintroid.ui.DrawSurfaceTrigger;

import java.util.LinkedList;

/**
 * Contains all the commands that can be executed on the layer in question.
 * It also contains the delete flag, which is used signal whether the LayerBitmapCommandManager object should be
 * permanently deleted. Use case: once the redo operation is unavailable.
 */
public class LayerBitmapCommandManagerImpl implements LayerBitmapCommandManager {
    private Layer mLayer;

    private LinkedList<Command> mCommandList;
    private LinkedList<Command> mUndoCommandList;
    private DrawSurfaceTrigger mDrawSurfaceTrigger;
    private boolean mDeleteFlag;

    public LayerBitmapCommandManagerImpl(LayerCommand layerCommand, DrawSurfaceTrigger drawSurfaceTrigger)
    {
        mLayer = layerCommand.getCurrentLayer();
        mDrawSurfaceTrigger = drawSurfaceTrigger;
        mCommandList = new LinkedList<Command>();
        mUndoCommandList = new LinkedList<Command>();
        mDeleteFlag = false;
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
                PaintroidApplication.currentTool.resetInternalState(Tool.StateChange.RESET_INTERNAL_STATE);
            }
        }
    }

    @Override
    public void optForDelete(boolean deleteFlag) {
        mDeleteFlag = deleteFlag;
    }

    @Override
    public boolean getDeleteFlagValue() {
        return mDeleteFlag;
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
                    PaintroidApplication.currentTool.resetInternalState(Tool.StateChange.RESET_INTERNAL_STATE);
                    mDrawSurfaceTrigger.redraw();
                }
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
                PaintroidApplication.currentTool.resetInternalState(Tool.StateChange.RESET_INTERNAL_STATE);
            }

            mDrawSurfaceTrigger.redraw();
        }
    }

    private void clearLayerBitmap()
    {
        synchronized (mLayer.getLayerCanvas())
        {
            //mLayer.getLayerCanvas().drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mLayer.getBitmap().eraseColor(Color.TRANSPARENT);
        }
    }
}
