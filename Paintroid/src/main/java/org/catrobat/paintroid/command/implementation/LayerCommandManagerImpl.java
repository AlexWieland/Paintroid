package org.catrobat.paintroid.command.implementation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.command.LayerCommandManager;
import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.tools.Tool;
import org.catrobat.paintroid.ui.DrawSurfaceTrigger;

import java.util.LinkedList;

/**
 * Contains all the commands that can be executed on layer in question.
 * It also contains the delete flag, which is used signal whether the LayerManager object should be
 * permanently deleted. This flag is used once redo action is not possible, to mark all the objects
 * that should be permanently removed.
 */
public class LayerCommandManagerImpl implements LayerCommandManager
{
    private Layer mLayer;

    private LinkedList<Command> mCommandList;
    private LinkedList<Command> mUndoCommandList;
    private DrawSurfaceTrigger mDrawSurfaceTrigger;
    private boolean mDeleteFlag;

    public LayerCommandManagerImpl(LayerCommand layerCommand, DrawSurfaceTrigger drawSurfaceTrigger)
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
        mUndoCommandList.clear();
        mCommandList.addLast(command);
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
        Command command = mCommandList.removeLast();
        mUndoCommandList.addFirst(command);
        executeAllCommandsOnLayerCanvas();
    }

    private void executeAllCommandsOnLayerCanvas()
    {
        clearLayersBitmap();
        for (Command command : mCommandList)
        {
            command.run(mLayer.getLayerCanvas());
            PaintroidApplication.currentTool.resetInternalState(Tool.StateChange.RESET_INTERNAL_STATE);
        }

        mDrawSurfaceTrigger.redraw();
    }

    private void clearLayersBitmap()
    {
        Canvas canvas = mLayer.getLayerCanvas();

        synchronized (canvas)
        {
            //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mLayer.getBitmap().eraseColor(Color.TRANSPARENT);
        }
    }
    @Override
    public synchronized void redo()
    {
        if (mUndoCommandList.size() > 0)
        {
            Command command = mUndoCommandList.removeFirst();
            mCommandList.addLast(command);
            command.run(mLayer.getLayerCanvas());
            PaintroidApplication.currentTool.resetInternalState(Tool.StateChange.RESET_INTERNAL_STATE);
        }

        mDrawSurfaceTrigger.redraw();
    }
}
