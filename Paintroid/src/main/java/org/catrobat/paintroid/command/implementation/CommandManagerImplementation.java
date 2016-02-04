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

import android.graphics.Canvas;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.command.CommandManager;
import org.catrobat.paintroid.command.UndoRedoManager;
import org.catrobat.paintroid.command.UndoRedoManager.StatusMode;
import org.catrobat.paintroid.dialog.IndeterminateProgressDialog;
import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.tools.Tool;

public class CommandManagerImplementation implements CommandManager, Observer
{
	private static final int MAX_COMMANDS = 512;

	private final LinkedList<Command> mCommandList;
    private LinkedList<Command> mUndo;

	public CommandManagerImplementation() {
		mCommandList = new LinkedList<Command>();
        mUndo = new LinkedList<Command>();
	}

    @Override
    public boolean hasCommands() {
        return mCommandList.size() > 0;
    }

    @Override
	public synchronized void resetAndClear() {
		mCommandList.clear();
        mUndo.clear();
		UndoRedoManager.getInstance().update(StatusMode.DISABLE_REDO);
		UndoRedoManager.getInstance().update(StatusMode.DISABLE_UNDO);
	}

	@Override
	public synchronized boolean commitCommand(Command command)
    {
        mUndo.clear();

        if (mCommandList.size() == MAX_COMMANDS)
        {
            return false;
        }

        ((BaseCommand)command).addObserver(this);
        mCommandList.addLast(command);
        return true;
	}

	@Override
	public void undo()
    {
        Canvas canvas = PaintroidApplication.drawingSurface.getWorkingCanvas();
        synchronized (canvas)
        {
            if(mCommandList.size() != 0)
            {
                Command command = mCommandList.removeLast();
                mUndo.addFirst(command);
                redraw(canvas);
            }
        }
	}

	@Override
	public synchronized void redo()
    {
        Canvas canvas = PaintroidApplication.drawingSurface.getWorkingCanvas();
        synchronized (canvas)
        {
            if(mUndo.size() != 0)
            {
                Command command = mUndo.removeFirst();
                mCommandList.addLast(command);
                redraw(canvas);
            }
        }
	}

    private void redraw(Canvas canvas)
    {
        Layer layer =  PaintroidApplication.drawingSurface.getCurrentLayer();

        for (Command cmd: mCommandList)
        {
            cmd.run(canvas, layer);
            PaintroidApplication.currentTool.resetInternalState(Tool.StateChange.RESET_INTERNAL_STATE);
        }

        PaintroidApplication.drawingSurface.getSurfaceViewDrawTrigger().redraw();
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
