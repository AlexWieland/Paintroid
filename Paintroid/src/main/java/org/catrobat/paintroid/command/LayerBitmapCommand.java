package org.catrobat.paintroid.command;

import org.catrobat.paintroid.tools.Layer;

/**
 * Describes layer commands responsible for drawing. These commands are performed on layer's bitmap.
 */
public interface LayerBitmapCommand
{
    /**
     * Retrieves layer assigned to command manager.
     * @return Layer which has been assigned to command manager.
     */
    Layer getLayer();

    /**
     * Commits command for assigned layer.
     * @param command which has been performed on layer.
     */
    void commitCommandToLayer(Command command);

    /**
     * Undo drawing command for assigned layer.
     */
    void undo();

    /**
     * Redo drawing command for assigned layer.
     */
    void redo();
}
