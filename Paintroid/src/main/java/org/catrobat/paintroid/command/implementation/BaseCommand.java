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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observable;
import java.util.Random;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.tools.Layer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.util.Log;

public abstract class BaseCommand extends Observable implements Command {
	protected Paint mPaint;
    protected int mLayerId;

	public enum NOTIFY_STATES {
		COMMAND_STARTED, COMMAND_DONE, COMMAND_FAILED
	}

    public BaseCommand() {
    }

	public BaseCommand(int layerId)
    {
       mLayerId = layerId;
	}

    public BaseCommand(Paint paint, int layerId)
    {

        this(layerId);

        if (paint != null)
        {
			mPaint = new Paint(paint);
		}
        else
        {
			Log.w(PaintroidApplication.TAG,
					"Object is null falling back to default object in "	+ this.toString());
			mPaint = new Paint();
			mPaint.setColor(Color.BLACK);
			mPaint.setStrokeWidth(1);
			mPaint.setStrokeCap(Cap.SQUARE);
		}
	}

    public int getLayerId()
    {
        return mLayerId;
    }

    @Override
	public abstract void run(Canvas canvas, Layer layer);

	protected void notifyStatus(NOTIFY_STATES state) {
		setChanged();
		notifyObservers(state);
	}
}
