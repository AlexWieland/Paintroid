package org.catrobat.paintroid.tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Layer {
    private static final String LAYER_PREFIX = "Layer ";

    private int mLayerID;
    private String mLayerName;

    private Bitmap mBitmap;
    private Canvas mLayerCanvas;

    private int mOpacity;
    private boolean mIsLocked;
    private boolean mIsVisible;

    private int mListPosition;
    private boolean mIsSelected;

    public Layer(int layer_id, Bitmap bitmap, int listPosition)
    {
        mLayerID = layer_id;
        mLayerName = LAYER_PREFIX + layer_id;

        mBitmap = bitmap;
        mLayerCanvas = new Canvas(mBitmap);

        mOpacity = 100;
        mIsLocked = false;
        mIsVisible = true;

        mListPosition = listPosition;
        mIsSelected = false;
    }

    public int getLayerID()
    {
        return mLayerID;
    }

    public Bitmap getBitmap()
    {
        return mBitmap;
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

    public void setBitmap(Bitmap bitmap)
    {
        mBitmap = bitmap;
        mLayerCanvas.setBitmap(bitmap);
    }

    public Canvas getLayerCanvas()
    {
        return mLayerCanvas;
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

    public void setListPosition(int listPosition)
    {
        this.mListPosition = listPosition;
    }

    public int getListPosition()
    {
        return mListPosition;
    }

    public void setSelected(boolean toSet)
    {
        mIsSelected = toSet;
    }

    public boolean getSelected()
    {
        return mIsSelected;
    }

    public void recycleBitmap()
    {
        if(mBitmap != null && !mBitmap.isRecycled())
        {
            mBitmap.recycle();
        }
    }
}
