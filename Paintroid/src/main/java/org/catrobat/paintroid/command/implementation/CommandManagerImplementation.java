/**
 *  Paintroid: An image manipulation application for Android.
 *  Copyright (C) 2010-2015 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.paintroid.command.implementation;

import android.util.Pair;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.command.CommandManager;
import org.catrobat.paintroid.command.LayerBitmapCommandManager;
import org.catrobat.paintroid.command.UndoRedoManager;
import org.catrobat.paintroid.command.UndoRedoManager.StatusMode;
import org.catrobat.paintroid.dialog.LayersDialog;
import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.ui.DrawSurfaceTrigger;
import org.catrobat.paintroid.ui.button.LayersAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

public class CommandManagerImplementation implements CommandManager, Observer
{
    enum LayerOperation {COMMIT_LAYER_BITMAP_COMMAND, ADD, REMOVE, MERGE, VISIBILITY, LOCK, RENAME_LAYER}

    private static final int MAX_COMMANDS = 512;
    private static final int INIT_APP_lAYER_COUNT = 1;
    enum OperationMode{UNDO, REDO}

    private LinkedList<Pair<LayerOperation, LayerCommand>> mLayerCommandList;
    private LinkedList<Pair<LayerOperation, LayerCommand>> mLayerUndoCommandList;
    private ArrayList<LayerBitmapCommandManager> mLayerBitmapCommandManagerList;
    private DrawSurfaceTrigger mDrawSurfaceTrigger;
    private LayersAdapter mLayersAdapter;

    public CommandManagerImplementation(DrawSurfaceTrigger drawSurfaceTrigger, LayersAdapter layersAdapter)
    {
        mLayerCommandList = new LinkedList<Pair<LayerOperation, LayerCommand>>();
        mLayerUndoCommandList = new LinkedList<Pair<LayerOperation, LayerCommand>>();
        mLayerBitmapCommandManagerList = new ArrayList<LayerBitmapCommandManager>();
        mDrawSurfaceTrigger = drawSurfaceTrigger;
        mLayersAdapter = layersAdapter;
    }

    @Override
    public void commitCommandToLayer(LayerCommand layerCommand, Command command)
    {
        synchronized (mLayerCommandList)
        {
            clearUndoCommandList();
            LayerBitmapCommandManager layerBitmapCommandManager = getLayerBitmapCommandManager(layerCommand);
            layerBitmapCommandManager.commitCommandToLayer(command);
            mLayerCommandList.addLast(createLayerCommand(LayerOperation.COMMIT_LAYER_BITMAP_COMMAND, layerCommand));
        }

        mDrawSurfaceTrigger.redraw();
    }

    @Override
    public void commitAddLayerCommand(LayerCommand layerCommand)
    {
        synchronized (mLayerCommandList)
        {
            clearUndoCommandList();
            mLayerBitmapCommandManagerList.add(new LayerBitmapCommandManagerImpl(layerCommand, mDrawSurfaceTrigger));
            mLayerCommandList.addLast(createLayerCommand(LayerOperation.ADD, layerCommand));
        }

        mDrawSurfaceTrigger.redraw();
    }

    @Override
    public void commitRemoveLayerCommand(LayerCommand layerCommand)
    {
        synchronized (mLayerCommandList)
        {
            clearUndoCommandList();
            getLayerBitmapCommandManager(layerCommand).optForDelete(true);
            mLayerCommandList.addLast(createLayerCommand(LayerOperation.REMOVE, layerCommand));
        }

        mDrawSurfaceTrigger.redraw();
    }

    @Override
    public void commitMergeLayerCommand(LayerCommand layerCommand) {

    }

    @Override
    public void commitLayerVisibilityCommand(LayerCommand layerCommand) {
        synchronized (mLayerCommandList)
        {
            clearUndoCommandList();
            mLayerCommandList.addLast(createLayerCommand(LayerOperation.VISIBILITY, layerCommand));
        }

        mDrawSurfaceTrigger.redraw();
    }

    @Override
    public void commitLayerLockCommand(LayerCommand layerCommand) {
    }

    @Override
    public void commitRenameLayerCommand(LayerCommand layerCommand) {

    }

    private LayerBitmapCommandManager getLayerBitmapCommandManager(LayerCommand layerCommand)
    {
        synchronized (mLayerBitmapCommandManagerList)
        {
            for (LayerBitmapCommandManager layerBitmapCommandManager : mLayerBitmapCommandManagerList) {
                if (layerBitmapCommandManager.getLayer().getLayerID() == layerCommand.getCurrentLayer().getLayerID()) {
                    return layerBitmapCommandManager;
                }
            }

            return null;
        }
    }

    private Pair<LayerOperation, LayerCommand> createLayerCommand(LayerOperation operation, LayerCommand layerCommand)
    {
        return new Pair<LayerOperation, LayerCommand>(operation, layerCommand);
    }

    @Override
    public synchronized void resetAndClear()
    {
        mLayerCommandList.clear();
        mLayerUndoCommandList.clear();
        mLayerBitmapCommandManagerList.clear();
        UndoRedoManager.getInstance().update(StatusMode.DISABLE_REDO);
        UndoRedoManager.getInstance().update(StatusMode.DISABLE_UNDO);
    }


    @Override
    public void undo()
    {
        synchronized (mLayerCommandList)
        {
            if (mLayerCommandList.size() > INIT_APP_lAYER_COUNT)
            {
                Pair<LayerOperation, LayerCommand> command = mLayerCommandList.removeLast();
                mLayerUndoCommandList.addFirst(command);
                processCommand(command, OperationMode.UNDO);
            }
        }
    }

    @Override
    public void redo()
    {
        synchronized (mLayerUndoCommandList)
        {
            if (mLayerUndoCommandList.size() != 0)
            {
                Pair<LayerOperation, LayerCommand> command = mLayerUndoCommandList.removeFirst();
                mLayerCommandList.addLast(command);
                processCommand(command, OperationMode.REDO);
            }
        }
    }

    private void processCommand(Pair<LayerOperation, LayerCommand> command, OperationMode mode)
    {
        switch (mode)
        {
            case UNDO:
                processUndoCommand(command);
                break;
            case REDO:
                processRedoCommand(command);
                break;
        }
    }

    private void processUndoCommand(Pair<LayerOperation, LayerCommand> command)
    {
        switch (command.first)
        {
            case COMMIT_LAYER_BITMAP_COMMAND:
                getLayerBitmapCommandManager(command.second).undo();
                break;
            case ADD:
                getLayerBitmapCommandManager(command.second).optForDelete(true);
                mLayersAdapter.removeLayer(command.second.getCurrentLayer().getLayerID());
                mDrawSurfaceTrigger.redraw();
                break;
            case REMOVE:
                getLayerBitmapCommandManager(command.second).optForDelete(false);
                mLayersAdapter.tryAddLayer(command.second.getCurrentLayer());
                mDrawSurfaceTrigger.redraw();
                break;
            case MERGE:
                break;
            case VISIBILITY:
                Layer layer = command.second.getCurrentLayer();
                layer.setVisible(!layer.getVisible());
                LayersDialog.getInstance().refreshView();
                break;
            case LOCK:
                break;
            case RENAME_LAYER:
                break;
        }
    }

    private void processRedoCommand(Pair<LayerOperation, LayerCommand> command)
    {
        switch (command.first) {
            case COMMIT_LAYER_BITMAP_COMMAND:
                getLayerBitmapCommandManager(command.second).redo();
                break;
            case ADD:
                getLayerBitmapCommandManager(command.second).optForDelete(false);
                mLayersAdapter.tryAddLayer(command.second.getCurrentLayer());
                mDrawSurfaceTrigger.redraw();
                break;
            case REMOVE:
                getLayerBitmapCommandManager(command.second).optForDelete(true);
                mLayersAdapter.removeLayer(command.second.getCurrentLayer().getLayerID());
                mDrawSurfaceTrigger.redraw();
                break;
            case MERGE:
                break;
            case VISIBILITY:
                command.second.getCurrentLayer().setVisible(!command.second.getCurrentLayer().getVisible());
                LayersDialog.getInstance().refreshView();
                break;
            case LOCK:
                break;
            case RENAME_LAYER:
                break;
        }
    }

    private void clearUndoCommandList()
    {
        synchronized (mLayerCommandList)
        {
            mLayerUndoCommandList.clear();

            synchronized (mLayerBitmapCommandManagerList)
            {
                Iterator<LayerBitmapCommandManager> commandManagerIterator = mLayerBitmapCommandManagerList.iterator();
                while (commandManagerIterator.hasNext()) {
                    if (commandManagerIterator.next().getDeleteFlagValue())
                    {
                        commandManagerIterator.remove();
                    }
                }
            }
        }
    }

    private synchronized void deleteFailedCommand(Command command)
    {

    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof BaseCommand.NOTIFY_STATES)
        {
            if (BaseCommand.NOTIFY_STATES.COMMAND_FAILED == data)
            {
                if (observable instanceof Command)
                {
                    deleteFailedCommand((Command) observable);
                }
            }
        }
    }
}
