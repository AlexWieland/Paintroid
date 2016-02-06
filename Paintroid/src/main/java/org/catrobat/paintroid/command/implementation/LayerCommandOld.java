package org.catrobat.paintroid.command.implementation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.provider.Telephony;
import android.util.Log;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.command.CommandManager;
import org.catrobat.paintroid.dialog.LayersDialog;
import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.ui.button.LayersAdapter;

import java.util.LinkedList;

public class LayerCommandOld extends BaseCommand
{
/*    @Override
    public void run(Canvas canvas) {

    }*/

    private LayerAction mLayerAction;
    private Layer mLayerActualActivity;
    private Layer mLayerToMerge;

    public enum LayerAction{
        CLEAR,
        ADD,
        REMOVE,
        MERGE,
        INSERT_IMAGE,
        SET_LOCK,
        SET_VISIBILITY,
        RENAME,
        OPACITY
    }

    public LayerCommandOld(LayerAction layerAction, Layer layerActualActivity, Layer layerToMerge)
    {
        switch (layerAction)
        {
            case CLEAR:
                mLayerAction = LayerAction.CLEAR;
                mLayerActualActivity = layerActualActivity;
                break;

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


    public void run(Canvas canvas) {
/*        switch (mLayerAction)
        {
            case CLEAR:
                processClearLayerCommand(canvas, mLayerActualActivity);
            case ADD:
                processAddLayerCommand(canvas, mLayerActualActivity, flag);
                break;
            case REMOVE:
                LayersDialog.getInstance().getAdapter().removeLayer(mLayerActualActivity.getLayerID());
                LayersDialog.getInstance().refreshView();
                LayersDialog.getInstance().selectLayer(LayersDialog.getInstance().getAdapter().getLayer(0));
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
        }*/

    }

    private synchronized void processClearLayerCommand(Canvas canvas, Layer layer)
    {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        layer.getBitmap().eraseColor(Color.TRANSPARENT);
    }
/*
    private synchronized void processAddLayerCommand(Canvas canvas, Layer layer, OperationFlag flag)
    {
            switch (flag) {
                case UNDO:
                    removeLayer(layer, canvas);
                    break;
                case REDO:
                    addLayer(layer);
                    break;
        }
    }*/

    private void removeLayer(Layer layer, Canvas canvas)
    {
        LayersAdapter adapter = LayersDialog.getInstance().getAdapter();

        if (adapter.getCount() > 1)
        {
            LayersDialog.getInstance().getAdapter().removeLayer(layer.getLayerID());
            LayersDialog.getInstance().refreshView();
            LayersDialog.getInstance().selectLayer(LayersDialog.getInstance().getAdapter().getLayer(0));
        }
    }

    private void addLayer(Layer layer)
    {
        LayersDialog.getInstance().getAdapter().tryAddLayer(layer);
        LayersDialog.getInstance().refreshView();
        LayersDialog.getInstance().selectLayer(LayersDialog.getInstance().getAdapter().getLayer(0));
    }
}
