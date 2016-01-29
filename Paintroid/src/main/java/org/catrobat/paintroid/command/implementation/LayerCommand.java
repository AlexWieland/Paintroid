package org.catrobat.paintroid.command.implementation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.provider.Telephony;
import android.util.Log;

import org.catrobat.paintroid.dialog.LayersDialog;
import org.catrobat.paintroid.tools.Layer;

public class LayerCommand extends BaseCommand {

    private LayerAction mLayerAction;
    private Layer mLayerActualActivity;
    private Layer mLayerToMerge;


    public enum LayerAction{
        ADD,
        REMOVE,
        MERGE,
        INSERT_IMAGE,
        SET_LOCK,
        SET_VISIBILITY,
        RENAME,
        OPACITY
    }

    public LayerCommand(LayerAction layerAction, Layer layerActualActivity, Layer layerToMerge)
    {
        switch (layerAction)
        {
            case ADD:
                mLayerAction = LayerAction.ADD;
                mLayerActualActivity = layerActualActivity;
                break;
            case REMOVE:
                mLayerAction = LayerAction.REMOVE;
                mLayerActualActivity = layerActualActivity;
                break;
            case MERGE:
                mLayerAction = LayerAction.MERGE;
                mLayerActualActivity = layerActualActivity;
                mLayerToMerge = layerToMerge;
                break;

            case INSERT_IMAGE:
                mLayerAction = LayerAction.INSERT_IMAGE;
                mLayerActualActivity = layerActualActivity;
                break;

            case SET_LOCK:
                mLayerAction = LayerAction.SET_LOCK;
                mLayerActualActivity = layerActualActivity;
                break;

            case SET_VISIBILITY:
                mLayerAction = LayerAction.SET_VISIBILITY;
                mLayerActualActivity = layerActualActivity;
                break;

            case RENAME:
                mLayerAction = LayerAction.RENAME;
                mLayerActualActivity = layerActualActivity;
                break;

            case OPACITY:
                mLayerAction = LayerAction.OPACITY;
                mLayerActualActivity = layerActualActivity;
                break;

            default:
                break;
        }
    }

    @Override
    public void run(Canvas canvas, Layer layer) {
        notifyStatus(NOTIFY_STATES.COMMAND_STARTED);

        switch (mLayerAction)
        {
            case ADD:
                LayersDialog.getInstance().getAdapter().tryAddLayer(mLayerActualActivity);
                LayersDialog.getInstance().refreshView();
                LayersDialog.getInstance().selectLayer(LayersDialog.getInstance().getAdapter().getLayer(1));
                break;
            case REMOVE:
                LayersDialog.getInstance().getAdapter().removeLayer(mLayerActualActivity.getLayerID());
                LayersDialog.getInstance().refreshView();
                LayersDialog.getInstance().selectLayer(LayersDialog.getInstance().getAdapter().getLayer(1));
                break;
            case MERGE:
                LayersDialog.getInstance().selectLayer(LayersDialog.getInstance().getAdapter()
                        .getLayer(LayersDialog.getInstance().getAdapter().getPosition(mLayerActualActivity.getLayerID())));
                LayersDialog.getInstance().mergeLayerCalledFromCommand(mLayerToMerge.getLayerID());
                break;

            case INSERT_IMAGE:
                LayersDialog.getInstance().selectLayer(LayersDialog.getInstance().getAdapter()
                        .getLayer(LayersDialog.getInstance().getAdapter().getPosition(mLayerActualActivity.getLayerID())));
                LayersDialog.getInstance().getCurrentLayer().setBitmap(mLayerActualActivity.getBitmap());
                break;

            case SET_LOCK:
                LayersDialog.getInstance().getAdapter().getLayer(LayersDialog.getInstance().getAdapter()
                        .getPosition(mLayerActualActivity.getLayerID())).setLocked(!mLayerActualActivity.getLocked());
                break;

            case SET_VISIBILITY:
                LayersDialog.getInstance().getAdapter().getLayer(LayersDialog.getInstance().getAdapter()
                        .getPosition(mLayerActualActivity.getLayerID())).setVisible(!mLayerActualActivity.getVisible());
                break;

            case RENAME:
                LayersDialog.getInstance().getAdapter().getLayer(LayersDialog.getInstance().getAdapter()
                        .getPosition(mLayerActualActivity.getLayerID())).setName(mLayerActualActivity.getName());
                break;

            case OPACITY:
                LayersDialog.getInstance().getAdapter().getLayer(LayersDialog.getInstance().getAdapter()
                        .getPosition(mLayerActualActivity.getLayerID())).setOpacity(mLayerActualActivity.getOpacity());
                break;

            default:
                break;
        }

        setChanged();
        notifyStatus(NOTIFY_STATES.COMMAND_DONE);
    }
}
