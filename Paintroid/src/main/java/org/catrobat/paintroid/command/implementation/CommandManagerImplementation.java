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

import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.command.CommandManager;
import org.catrobat.paintroid.command.LayerCommandManager;
import org.catrobat.paintroid.command.UndoRedoManager;
import org.catrobat.paintroid.command.UndoRedoManager.StatusMode;
import org.catrobat.paintroid.ui.DrawSurfaceTrigger;
import org.catrobat.paintroid.ui.button.LayersAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

public class CommandManagerImplementation implements CommandManager, Observer
{
    enum LayerOperation {COMMIT_COMMAND, ADD, REMOVE, MERGE, VISIBILITY, LOCK}

    private static final int MAX_COMMANDS = 512;
    private static final int INIT_APP_lAYER_COUNT = 1;
    enum OperationMode{UNDO, REDO};

    private LinkedList<Pair<LayerOperation, LayerCommand>> mLayerCommandList;
    private LinkedList<Pair<LayerOperation, LayerCommand>> mLayerUndoCommandList;
    private ArrayList<LayerCommandManager> mLayerCommandManagerList;
    private DrawSurfaceTrigger mDrawSurfaceTrigger;
    private LayersAdapter mLayersAdapter;

    public CommandManagerImplementation(DrawSurfaceTrigger drawSurfaceTrigger, LayersAdapter layersAdapter)
    {
        mLayerCommandList = new LinkedList<Pair<LayerOperation, LayerCommand>>();
        mLayerUndoCommandList = new LinkedList<Pair<LayerOperation, LayerCommand>>();
        mLayerCommandManagerList = new ArrayList<LayerCommandManager>();
        mDrawSurfaceTrigger = drawSurfaceTrigger;
        mLayersAdapter = layersAdapter;
    }

    @Override
    public synchronized void commitCommandToLayer(LayerCommand layerCommand, Command command)
    {
        if (mLayerCommandList.size() == MAX_COMMANDS)
        {
            return;
        }

        clearUndoCommandList();
        LayerCommandManager layerCommandManager = getLayerCommandManagerForLayer(layerCommand);
        layerCommandManager.commitCommandToLayer(command);
        mLayerCommandList.addLast(createLayerCommand(LayerOperation.COMMIT_COMMAND, layerCommand));
    }

    @Override
    public synchronized void commitAddLayerCommand(LayerCommand layerCommand)
    {
        if (mLayerCommandList.size() == MAX_COMMANDS)
        {
            return;
        }

        clearUndoCommandList();
        mLayerCommandManagerList.add(new LayerCommandManagerImpl(layerCommand, mDrawSurfaceTrigger));
        mLayerCommandList.addLast(createLayerCommand(LayerOperation.ADD, layerCommand));

    }

    @Override
    public synchronized void commitRemoveLayerCommand(LayerCommand layerCommand)
    {
        if (mLayerCommandList.size() == MAX_COMMANDS)
        {
            return;
        }

        clearUndoCommandList();
        getLayerCommandManagerForLayer(layerCommand).optForDelete(true);
        mLayerCommandList.addLast(createLayerCommand(LayerOperation.REMOVE, layerCommand));

    }

    @Override
    public synchronized void commitMergeLayerCommand(LayerCommand layerCommand) {

    }

    @Override
    public synchronized void commitLayerVisibilityCommand(LayerCommand layerCommand) {

    }

    @Override
    public synchronized void commitLayerLockCommand(LayerCommand layerCommand) {

    }

    @Override
    public synchronized void commitRenameLayerCommand(LayerCommand layerCommand) {

    }

    private LayerCommandManager getLayerCommandManagerForLayer(LayerCommand layerCommand)
    {
        for (LayerCommandManager layerCommandManager : mLayerCommandManagerList)
        {
            if(layerCommandManager.getLayer().getLayerID() == layerCommand.getCurrentLayer().getLayerID())
            {
                return layerCommandManager;
            }
        }

        return null;
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
        mLayerCommandManagerList.clear();
        UndoRedoManager.getInstance().update(StatusMode.DISABLE_REDO);
        UndoRedoManager.getInstance().update(StatusMode.DISABLE_UNDO);
    }


    @Override
    public synchronized void undo()
    {
        if(mLayerCommandList.size() > INIT_APP_lAYER_COUNT)
        {
            Pair<LayerOperation, LayerCommand> command = mLayerCommandList.removeLast();
            mLayerUndoCommandList.addFirst(command);
            processCommand(command, OperationMode.UNDO);
        }
    }

    @Override
    public synchronized void redo()
    {
        if(mLayerUndoCommandList.size() != 0)
        {
            Pair<LayerOperation, LayerCommand> command = mLayerUndoCommandList.removeFirst();
            mLayerCommandList.addLast(command);
            processCommand(command, OperationMode.REDO);
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
            case COMMIT_COMMAND:
                getLayerCommandManagerForLayer(command.second).undo();
                break;
            case ADD:
                getLayerCommandManagerForLayer(command.second).optForDelete(true);
                mLayersAdapter.removeLayer(command.second.getCurrentLayer().getLayerID());
                break;
            case REMOVE:
                getLayerCommandManagerForLayer(command.second).optForDelete(false);
                mLayersAdapter.tryAddLayer(command.second.getCurrentLayer());
                break;
            case MERGE:
                break;
            case VISIBILITY:
                break;
            case LOCK:
                break;
        }
    }

    private void processRedoCommand(Pair<LayerOperation, LayerCommand> command)
    {
        switch (command.first) {
            case COMMIT_COMMAND:
                getLayerCommandManagerForLayer(command.second).redo();
                break;
            case ADD:
                getLayerCommandManagerForLayer(command.second).optForDelete(false);
                mLayersAdapter.tryAddLayer(command.second.getCurrentLayer());
                break;
            case REMOVE:
                getLayerCommandManagerForLayer(command.second).optForDelete(true);
                mLayersAdapter.removeLayer(command.second.getCurrentLayer().getLayerID());
                break;
            case MERGE:
                break;
            case VISIBILITY:
                break;
            case LOCK:
                break;
        }

    }

    private void clearUndoCommandList()
    {
        mLayerUndoCommandList.clear();
        Iterator<LayerCommandManager> commandManagerIterator = mLayerCommandManagerList.iterator();
        while (commandManagerIterator.hasNext())
        {
            if(commandManagerIterator.next().getDeleteFlagValue())
            {
                commandManagerIterator.remove();
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
