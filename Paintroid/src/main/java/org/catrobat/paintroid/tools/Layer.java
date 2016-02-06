package org.catrobat.paintroid.tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Layer {
    private static final String LAYER_PREFIX = "Layer ";

    private int mLayerID;
    private int mOpacity;
    private Bitmap mBitmap;
    private String mLayerName;
    private boolean mIsLocked;
    private boolean mIsVisible;
    private boolean mIsSelected;
    private Canvas mLayerCanvas;


    public void setSelected(boolean toSet)
    {
        mIsSelected = toSet;
    }

    public boolean getSelected()
    {
        return mIsSelected;
    }

    public Layer(int layer_id, Bitmap bitmap, Canvas canvas) {
        mLayerID = layer_id;
        mBitmap = bitmap;
       mLayerCanvas = canvas;
        setSelected(false);
        mLayerName = LAYER_PREFIX + layer_id;
        mIsLocked = false;
        mIsVisible = true;
        mOpacity = 100;
    }

    public void setOpacity(int newOpacity)
    {
        mOpacity = newOpacity;
    }

    public int getOpacity()
    {
        return mOpacity;
    }

    public int getScaledOpacity()
    {
        return Math.round((mOpacity * 255)/100);
    }

    public void setLocked(boolean isLocked)
    {
        mIsLocked = isLocked;
    }

    public boolean getLocked()
    {
        return mIsLocked;
    }

    public void setVisible(boolean isVisible)
    {
        mIsVisible = isVisible;
    }

    public boolean getVisible()
    {
        return mIsVisible;
    }

    public String getName()
    {
        return mLayerName;
    }

    public void setName(String layerName)
    {
        if(layerName != null && layerName.length()>0)
        {
            mLayerName = layerName;
        }
    }

    public int getLayerID()
    {
        return mLayerID;
    }

    public Bitmap getBitmap()
    {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap)
    {
        mBitmap = bitmap;
        mLayerCanvas.setBitmap(bitmap);
    }

    public Canvas getLayerCanvas()
    {
        return mLayerCanvas;
    }

    public Layer getLayer()
    {
        return this;
    }

    public void recycleBitmap()
    {
        if(mBitmap != null && !mBitmap.isRecycled())
        {
            mBitmap.recycle();
        }
    }
}
