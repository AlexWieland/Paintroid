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
import org.catrobat.paintroid.command.LayerBitmapCommand;
import org.catrobat.paintroid.command.UndoRedoManager;
import org.catrobat.paintroid.command.UndoRedoManager.StatusMode;
import org.catrobat.paintroid.dialog.LayersDialog;
import org.catrobat.paintroid.ui.DrawSurfaceTrigger;
import org.catrobat.paintroid.ui.button.LayersAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;

public class CommandManagerImplementation implements CommandManager, Observer
{
    private static final int MAX_COMMANDS = 512;
    private static final int INIT_APP_lAYER_COUNT = 1;

    enum CommandType {COMMIT_LAYER_BITMAP_COMMAND
                      ,ADD_LAYER, REMOVE_LAYER
                      ,MERGE_LAYERS
                      ,CHANGE_LAYER_VISIBILITY
                      ,LOCK_LAYER
                      ,RENAME_LAYER}


    enum Action {UNDO, REDO}

    private LinkedList<Pair<CommandType, LayerCommand>> mLayerCommandList;
    private LinkedList<Pair<CommandType, LayerCommand>> mLayerUndoCommandList;
    private ArrayList<LayerBitmapCommand> mLayerBitmapCommands;
    private DrawSurfaceTrigger mDrawSurfaceTrigger;
    private LayersAdapter mLayersAdapter;

    public CommandManagerImplementation(DrawSurfaceTrigger drawSurfaceTrigger, LayersAdapter layersAdapter)
    {
        mLayerCommandList = new LinkedList<Pair<CommandType, LayerCommand>>();
        mLayerUndoCommandList = new LinkedList<Pair<CommandType, LayerCommand>>();
        mLayerBitmapCommands = new ArrayList<LayerBitmapCommand>();
        mDrawSurfaceTrigger = drawSurfaceTrigger;
        mLayersAdapter = layersAdapter;
    }

    @Override
    public void commitCommandToLayer(LayerCommand layerCommand, Command bitmapCommand)
    {
        synchronized (mLayerCommandList)
        {
            clearUndoCommandList();

            ArrayList<LayerBitmapCommand> result = layerIdToOneElementBitmapCommandList(layerCommand.getLayer().getLayerID());
            result.get(0).commitCommandToLayer(bitmapCommand);
            layerCommand.setLayersBitmapCommands(result);

            mLayerCommandList.addLast(createLayerCommand(CommandType.COMMIT_LAYER_BITMAP_COMMAND, layerCommand));
        }

        mDrawSurfaceTrigger.redraw();
    }

    @Override
    public void commitAddLayerCommand(LayerCommand layerCommand)
    {
        synchronized (mLayerCommandList)
        {
            clearUndoCommandList();

            LayerBitmapCommand bitmapCommand = new LayerBitmapCommandImpl(layerCommand);
            layerCommand.setLayersBitmapCommands(layerBitmapCommandToOneElementList(bitmapCommand));

            mLayerBitmapCommands.add(bitmapCommand);
            mLayerCommandList.addLast(createLayerCommand(CommandType.ADD_LAYER, layerCommand));
        }

        mDrawSurfaceTrigger.redraw();
    }

    @Override
    public void commitRemoveLayerCommand(LayerCommand layerCommand)
    {
        synchronized (mLayerCommandList)
        {
            clearUndoCommandList();

            layerCommand.setLayersBitmapCommands(layerIdToOneElementBitmapCommandList(layerCommand.getLayer().getLayerID()));
            mLayerCommandList.addLast(createLayerCommand(CommandType.REMOVE_LAYER, layerCommand));
        }

        mDrawSurfaceTrigger.redraw();
    }

    @Override
    public void commitMergeLayerCommand(LayerCommand layerCommand)
    {
        synchronized (mLayerCommandList)
        {
            clearUndoCommandList();

            ArrayList<LayerBitmapCommand> result = getLayerBitmapCommands(layerCommand.getLayersToMerge());
            layerCommand.setLayersBitmapCommands(result);

            for (LayerBitmapCommand manager: result)
            {
                mLayerBitmapCommands.remove(manager);
            }

            mLayerBitmapCommands.add(new LayerBitmapCommandImpl(layerCommand));
            mLayerCommandList.addLast(createLayerCommand(CommandType.MERGE_LAYERS, layerCommand));
        }

        mDrawSurfaceTrigger.redraw();

    }

    @Override
    public void commitLayerVisibilityCommand(LayerCommand layerCommand)
    {
        synchronized (mLayerCommandList)
        {
            clearUndoCommandList();
            mLayerCommandList.addLast(createLayerCommand(CommandType.CHANGE_LAYER_VISIBILITY, layerCommand));
        }

        mDrawSurfaceTrigger.redraw();
    }

    @Override
    public void commitLayerLockCommand(LayerCommand layerCommand)
    {
        synchronized (mLayerCommandList)
        {
            clearUndoCommandList();
            mLayerCommandList.addLast(createLayerCommand(CommandType.LOCK_LAYER, layerCommand));
        }

        mDrawSurfaceTrigger.redraw();
    }

    @Override
    public void commitRenameLayerCommand(LayerCommand layerCommand) {

    }

    private ArrayList<LayerBitmapCommand> layerBitmapCommandToOneElementList (LayerBitmapCommand command)
    {
        ArrayList<LayerBitmapCommand> result = new ArrayList<LayerBitmapCommand>(1);
        result.add(command);
        return  result;
    }

    private ArrayList<LayerBitmapCommand> layerIdToOneElementBitmapCommandList(int layerId)
    {
        ArrayList<Integer> ids = new ArrayList<Integer>(1);
        ids.add(layerId);
        return getLayerBitmapCommands(ids);
    }

    private ArrayList<LayerBitmapCommand> getLayerBitmapCommands(ArrayList<Integer> layerIds)
    {
        synchronized (mLayerBitmapCommands)
        {
            ArrayList<LayerBitmapCommand> result = new ArrayList<LayerBitmapCommand>();

            for (LayerBitmapCommand layerBitmapCommand : mLayerBitmapCommands)
            {
                for(int id: layerIds)
                {
                    if (layerBitmapCommand.getLayer().getLayerID() == id) {
                        result.add(layerBitmapCommand);
                    }
                }
            }

            return result;
        }
    }

    private Pair<CommandType, LayerCommand> createLayerCommand(CommandType operation, LayerCommand layerCommand)
    {
        return new Pair<CommandType, LayerCommand>(operation, layerCommand);
    }

    @Override
    public synchronized void resetAndClear()
    {
        mLayerCommandList.clear();
        mLayerUndoCommandList.clear();
        mLayerBitmapCommands.clear();
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
                Pair<CommandType, LayerCommand> command = mLayerCommandList.removeLast();
                mLayerUndoCommandList.addFirst(command);
                processCommand(command, Action.UNDO);
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
                Pair<CommandType, LayerCommand> command = mLayerUndoCommandList.removeFirst();
                mLayerCommandList.addLast(command);
                processCommand(command, Action.REDO);
            }
        }
    }

    private void clearUndoCommandList()
    {
        synchronized (mLayerCommandList)
        {
            mLayerUndoCommandList.clear();
        }
    }

    private void processCommand(Pair<CommandType, LayerCommand> command, Action action)
    {
        switch (action)
        {
            case UNDO:
                processUndoCommand(command);
                break;
            case REDO:
                processRedoCommand(command);
                break;
        }
    }

    private void processUndoCommand(Pair<CommandType, LayerCommand> command)
    {
        switch (command.first)
        {
            case COMMIT_LAYER_BITMAP_COMMAND:
                command.second.getLayersBitmapCommands().get(0).undo();
                break;
            case ADD_LAYER:
                mLayerBitmapCommands.remove(command.second.getLayersBitmapCommands().get(0));
                mLayersAdapter.removeLayer(command.second.getLayer().getLayerID());
                mDrawSurfaceTrigger.redraw();
                break;
            case REMOVE_LAYER:
                mLayerBitmapCommands.add(command.second.getLayersBitmapCommands().get(0));
                mLayersAdapter.tryAddLayer(command.second.getLayer());
                mDrawSurfaceTrigger.redraw();
                break;
            case MERGE_LAYERS:
                handleUnmerge(command.second);
                LayersDialog.getInstance().refreshView();
                break;
            case CHANGE_LAYER_VISIBILITY:
                command.second.getLayer().setVisible(!command.second.getLayer().getVisible());
                LayersDialog.getInstance().refreshView();
                mDrawSurfaceTrigger.redraw();
                break;
            case LOCK_LAYER:
                command.second.getLayer().setLocked(!command.second.getLayer().getLocked());
                LayersDialog.getInstance().refreshView();
                mDrawSurfaceTrigger.redraw();
                break;
            case RENAME_LAYER:
                break;
        }
    }

    private void processRedoCommand(Pair<CommandType, LayerCommand> command)
    {
        switch (command.first) {
            case COMMIT_LAYER_BITMAP_COMMAND:
                command.second.getLayersBitmapCommands().get(0).redo();
                break;
            case ADD_LAYER:
                mLayerBitmapCommands.add(command.second.getLayersBitmapCommands().get(0));
                mLayersAdapter.tryAddLayer(command.second.getLayer());

                break;
            case REMOVE_LAYER:
                mLayerBitmapCommands.remove(command.second.getLayersBitmapCommands().get(0));
                mLayersAdapter.removeLayer(command.second.getLayer().getLayerID());
                mDrawSurfaceTrigger.redraw();
                break;
            case MERGE_LAYERS:
                handleMerge(command.second);
                LayersDialog.getInstance().refreshView();
                break;
            case CHANGE_LAYER_VISIBILITY:
                command.second.getLayer().setVisible(!command.second.getLayer().getVisible());
                LayersDialog.getInstance().refreshView();
                mDrawSurfaceTrigger.redraw();
                break;
            case LOCK_LAYER:
                command.second.getLayer().setLocked(!command.second.getLayer().getLocked());
                LayersDialog.getInstance().refreshView();

                break;
            case RENAME_LAYER:
                break;
        }


    }

    private void handleMerge(LayerCommand command)
    {
        mLayersAdapter.tryAddLayer(command.getLayer());
        mLayerBitmapCommands.add(command.getLayersBitmapCommands().get(0));

        ArrayList<LayerBitmapCommand> result = getLayerBitmapCommands(command.getLayersToMerge());

        for (LayerBitmapCommand bitmapCommand: result)
        {
            mLayersAdapter.removeLayer(bitmapCommand.getLayer().getLayerID());
            mLayerBitmapCommands.remove(bitmapCommand);
        }

        command.setLayersBitmapCommands(result);

        mDrawSurfaceTrigger.redraw();
    }

    private void handleUnmerge(LayerCommand command)
    {
        ListIterator<LayerBitmapCommand> iterator = command.getLayersBitmapCommands().listIterator();
        LayerBitmapCommand bitmapCommand;
        while (iterator.hasNext())
        {
            bitmapCommand = iterator.next();
            mLayersAdapter.tryAddLayer(bitmapCommand.getLayer());
            mLayerBitmapCommands.add(bitmapCommand);
            iterator.remove();
        }

        ArrayList<LayerBitmapCommand> result = layerIdToOneElementBitmapCommandList(command.getLayer().getLayerID());
        command.setLayersBitmapCommands(result);

        mLayerBitmapCommands.remove(result.get(0));
        mLayersAdapter.removeLayer(command.getLayer().getLayerID());

        mDrawSurfaceTrigger.redraw();
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
