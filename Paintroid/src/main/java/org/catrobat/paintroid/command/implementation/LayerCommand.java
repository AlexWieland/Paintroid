package org.catrobat.paintroid.command.implementation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.provider.Telephony;

import org.catrobat.paintroid.tools.Layer;

public class LayerCommand extends BaseCommand {

    public enum LayerAction{
        ADD,
        REMOVE,
        MERGE,
        INSERT_IMAGE,
        SET_LOCK,
        SET_VISIBILITY
    }

    public LayerCommand(LayerAction layerAction)
    {
        switch (layerAction)
        {
            case ADD:

                break;
            case REMOVE:

                break;
            case MERGE:

                break;

            case INSERT_IMAGE:

                break;

            case SET_LOCK:

                break;

            case SET_VISIBILITY:

                break;

            default:
                break;
        }
    }
    @Override
    public void run(Canvas canvas, Layer layer) {
        notifyStatus(NOTIFY_STATES.COMMAND_STARTED);
        setChanged();
        notifyStatus(NOTIFY_STATES.COMMAND_DONE);
    }
}