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

package org.catrobat.paintroid.tools;

import org.catrobat.paintroid.ui.TopBar.ToolButtonIDs;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Point;
import android.graphics.PointF;

/**
 * Describes tools used for drawing.
 */
public interface Tool {

    // standard stroke widths in pixels
    static final int stroke1 = 1;
    static final int stroke5 = 5;
    static final int stroke15 = 15;
    static final int stroke25 = 25;

    enum StateChange { ALL, RESET_INTERNAL_STATE, NEW_IMAGE_LOADED, MOVE_CANCELED }

   void handleDown(PointF coordinate);

   void handleMove(PointF coordinate);

   void handleUp(PointF coordinate);

    void changePaintColor(int color);

    void changePaintStrokeWidth(int strokeWidth);

    void changePaintStrokeCap(Cap cap);

    void setDrawPaint(Paint paint);

    Paint getDrawPaint();

    /**
     * Tracks the finger motion, by drawing it on the screen with the current tool.
     * @param canvas canvas to draw finger motion on.
     */
    void trackFingerMotion(Canvas canvas);

    ToolType getToolType();

    int getAttributeButtonResource(ToolButtonIDs buttonNumber);

    int getAttributeButtonColor(ToolButtonIDs buttonNumber);

    void attributeButtonClick(ToolButtonIDs buttonNumber);

    void resetInternalState(StateChange stateChange);

    Point getAutoScrollDirection(float pointX, float pointY, int screenWidth, int screenHeight);
}
