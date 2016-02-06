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

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.tools.Layer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

public class StampCommand extends BaseCommand {
	protected final Point mCoordinates;
	protected final float mBoxWidth;
	protected final float mBoxHeight;
	protected final float mBoxRotation;
	protected final RectF mBoxRect;

	public StampCommand(Point position, float width, float height, float rotation, Bitmap bitmap, int layerId)
    {
		super(new Paint(Paint.DITHER_FLAG), layerId);

		if (position != null)
        {
			mCoordinates = new Point(position.x, position.y);
		}
        else
        {
			mCoordinates = null;
		}

        Layer layer = PaintroidApplication.drawingSurface.getCurrentLayer();
		if(layer != null)
        {
			if (layer.getBitmap() != null)
            {
				layer.setBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, false));
			}
		}

		mBoxWidth = width;
		mBoxHeight = height;
		mBoxRotation = rotation;
		mBoxRect = new RectF(-mBoxWidth / 2f, -mBoxHeight / 2f, mBoxWidth / 2f,	mBoxHeight / 2f);
	}

	@Override
	public void run(Canvas canvas) {

		notifyStatus(NOTIFY_STATES.COMMAND_STARTED);
//		if (mFileToStoredBitmap != null) {
//			mBitmap = FileIO.getBitmapFromFile(mFileToStoredBitmap);
//		}

/*		Bitmap bitmap = layer.getBitmap();

		if (bitmap == null) {
			setChanged();
			notifyStatus(NOTIFY_STATES.COMMAND_FAILED);
			return;
		}

		canvas.save();
		canvas.translate(mCoordinates.x, mCoordinates.y);
		canvas.rotate(mBoxRotation);
		canvas.drawBitmap(bitmap, null, mBoxRect, mPaint);

		canvas.restore();*/

//		if (mFileToStoredBitmap == null) {
//			storeBitmap();
//		} else {
//			bitmap.recycle();
//			bitmap = null;
//		}

		notifyStatus(NOTIFY_STATES.COMMAND_DONE);
	}
}
