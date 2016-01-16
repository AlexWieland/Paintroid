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
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;

public class BitmapCommand extends BaseCommand {

	private boolean mResetScaleAndTranslation = true;

	public BitmapCommand(Layer layer) {
		if(layer != null)
		{
			mLayer = layer;
		}
//		if (bitmap != null) {
//			mBitmap = Bitmap.createBitmap(bitmap);
//		}
	}

	public BitmapCommand(Bitmap bitmap, boolean resetScaleAndTranslation) {
//		this(bitmap);
		mResetScaleAndTranslation = resetScaleAndTranslation;
	}

	@Override
	public void run(Canvas canvas, Layer layer) {
//		if (layer == null && mFileToStoredBitmap != null) {
//			layer.setBitmap(FileIO.getBitmapFromFile(mFileToStoredBitmap));
//		}
		if (layer != null) {
			if (layer.getBitmap() != null) {
				layer.getBitmap().eraseColor(Color.TRANSPARENT);
			}
			PaintroidApplication.drawingSurface.setBitmap(layer.getBitmap().copy(
					Config.ARGB_8888, true));

			if (mResetScaleAndTranslation
					&& PaintroidApplication.perspective != null) {
				PaintroidApplication.perspective.resetScaleAndTranslation();
			}

			mLayer = layer;
//			if (mFileToStoredBitmap == null) {
//				storeBitmap(bitmap);
//			}
		}
	}
}
