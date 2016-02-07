package org.catrobat.paintroid.command;

import org.catrobat.paintroid.tools.Layer;

/**
 * Describes command manager responsible for maintaining draw commands which have been performed on
 * specific layer.
 */
public interface LayerDrawingCommands
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
     * Sets whether object has been chosen for deletion.
     * @param deleteFlag value of objects delete flag.
     */
    void optForDelete(boolean deleteFlag);

    /**
     * Retrieves value of delete flag.
     * @return delete flag value.
     */
    boolean getDeleteFlagValue();


    /**
     * Undo drawing command for assigned layer.
     */
    void undo();

    /**
     * Redo drawing command for assigned layer.
     */
    void redo();
}
