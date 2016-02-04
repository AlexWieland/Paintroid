package org.catrobat.paintroid.ui;

/**
 * Created by dzombeast on 04.02.2016.
 * Describes the draw surface trigger, which used to signal whether or not the drawing surface
 * should be redrawn.
 */

public interface DrawSurfaceTrigger
{
    /**
     * Triggers drawing operation.
     */
    void redraw();

    /**
     * Stops drawing operation.
     */
    void hadRedraw();
}
