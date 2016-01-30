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

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.command.CommandManager;
import org.catrobat.paintroid.command.UndoRedoManager;
import org.catrobat.paintroid.command.UndoRedoManager.StatusMode;
import org.catrobat.paintroid.dialog.IndeterminateProgressDialog;

public class CommandManagerImplementation implements CommandManager, Observer {

    public enum CommandManagerState
    {
        COLLECTING_COMMANDS, UNDO, REDO
    }

	private static final int MAX_COMMANDS = 512;

	private final LinkedList<Command> mCommandList;
	private int mCommandCounter;
	private int mCommandIndex;
    private CommandManagerState mState;

	public CommandManagerImplementation() {
		mCommandList = new LinkedList<Command>();
        mState = CommandManagerState.COLLECTING_COMMANDS;
	}

	@Override
	public boolean hasCommands() {
		return mCommandCounter > 1;
	}

	@Override
	public synchronized boolean hasNextCommand() {
		return mCommandIndex < mCommandCounter;
	}

    @Override
	public synchronized void resetAndClear() {
		mCommandList.clear();
		UndoRedoManager.getInstance().update(StatusMode.DISABLE_REDO);
		UndoRedoManager.getInstance().update(StatusMode.DISABLE_UNDO);
	}

	@Override
	public synchronized Command getNextCommand() {
		if (mCommandIndex < mCommandCounter)
        {
			return mCommandList.get(mCommandIndex++);
		}
        else
        {
			return null;
		}
	}

	@Override
	public synchronized boolean commitCommand(Command command)
    {

        if (mCommandCounter < mCommandList.size())
        {
            for (int i = mCommandList.size(); i > mCommandCounter; i--)
            {
                mCommandList.removeLast();
            }

            UndoRedoManager.getInstance().update(StatusMode.DISABLE_REDO);
        }

        if (mCommandCounter == MAX_COMMANDS)
        {
            return false;
        }
        else
        {
            mCommandCounter++;
            UndoRedoManager.getInstance().update(UndoRedoManager.StatusMode.ENABLE_UNDO);
        }

		((BaseCommand) command).addObserver(this);
		PaintroidApplication.isSaved = false;
		return mCommandList.add(command);
	}

	@Override
	public synchronized void undo() {
		if (mCommandCounter > 1)
        {
            setCommandManagerState(CommandManagerState.UNDO);
			IndeterminateProgressDialog.getInstance().show();
			mCommandCounter--;
			mCommandIndex = 0;
			UndoRedoManager.getInstance().update(UndoRedoManager.StatusMode.ENABLE_REDO);

            if (mCommandCounter <= 1)
            {
				UndoRedoManager.getInstance().update(UndoRedoManager.StatusMode.DISABLE_UNDO);
			}
		}
	}

	@Override
	public synchronized void redo() {
		if (mCommandCounter < mCommandList.size())
        {
			IndeterminateProgressDialog.getInstance().show();
			mCommandIndex = mCommandCounter;
            mCommandCounter++;
			setCommandManagerState(CommandManagerState.REDO);
			UndoRedoManager.getInstance().update(UndoRedoManager.StatusMode.ENABLE_UNDO);

            if (mCommandCounter == mCommandList.size())
            {
				UndoRedoManager.getInstance().update(UndoRedoManager.StatusMode.DISABLE_REDO);
			}
		}
	}

	private synchronized void deleteFailedCommand(Command command)
    {
		int indexOfCommand = mCommandList.indexOf(command);
		mCommandList.remove(indexOfCommand);
		mCommandCounter--;
		mCommandIndex--;
		if (mCommandCounter == 1)
        {
			UndoRedoManager.getInstance().update(UndoRedoManager.StatusMode.DISABLE_UNDO);
		}
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

	@Override
	public int getNumberOfCommands() {
		return mCommandCounter;
	}

    @Override
    public void setCommandManagerState(CommandManagerState state) {
        mState = state;
    }

    @Override
    public CommandManagerState getCommandManagerState() {
        return mState;
    }

}
