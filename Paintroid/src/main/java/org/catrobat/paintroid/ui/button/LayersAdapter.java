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

package org.catrobat.paintroid.ui.button;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.command.implementation.LayerCommand;
import org.catrobat.paintroid.eventlistener.LayerEventListener;
import org.catrobat.paintroid.tools.Layer;

import java.util.ArrayList;
import java.util.Collections;

public class LayersAdapter extends BaseAdapter implements LayerEventListener{

    private Context mContext;
    private ArrayList<Layer> mLayerList;
    private int mLayerCounter = 0;
    private int mMaxLayer;
    private Point mDisplaySize;

    public LayersAdapter(Context mContext, int maxLayer, Point displaySize)
    {
        this.mContext = mContext;
        this.mMaxLayer = maxLayer;
        this.mDisplaySize = displaySize;
        this.mLayerList = new ArrayList<Layer>(mMaxLayer);

        Layer layer = createLayer(null);
        mLayerList.add(0, layer);
    }

    public LayersAdapter(Context context, boolean fromCatrobat,  int maxLayer, Point displaySize)
    {
        this(context, maxLayer, displaySize);
        initLayers(fromCatrobat);
    }

    //is fromCatrobat relevant?!
    private void initLayers(boolean fromCatrobat)
    {
        //TODO: do catrobat relevant stuff.
    }

    private Layer createLayer(Bitmap bitmap)
    {
        if(bitmap == null)
        {
            bitmap = Bitmap.createBitmap(mDisplaySize.x, mDisplaySize.y, Bitmap.Config.ARGB_8888);
        }

        return new Layer(mLayerCounter++, bitmap); //increment will be called afterwards
    }

    public ArrayList<Layer> getLayers()
    {
        return mLayerList;
    }

    public Layer getLayer(int position)
    {
        return (Layer)getItem(position);
    }

    public int getPosition(int layerID)
    {
        for (int position = 0; position < mLayerList.size(); position++)
        {
            if (mLayerList.get(position).getLayerID() == layerID)
            {
                return position;
            }
        }

        return -1;
    }

    public boolean tryAddLayer()
    {
        if(mLayerList.size() < mMaxLayer)
        {
            Layer layer = createLayer(null);
            mLayerList.add(0, layer);

            notifyDataSetChanged();
            return true;
        }

        return false;
    }

    public boolean tryAddLayer(Layer existingLayer)
    {
        if(mLayerList.size() < mMaxLayer)
        {
            mLayerList.add(0, existingLayer);
            notifyDataSetChanged();
            return true;
        }

        return false;
    }

    public void removeLayer(Layer layer)
    {
        mLayerList.remove(layer);
        notifyDataSetChanged();
    }

    public Layer mergeLayer(Layer firstLayer, Layer secondLayer)
    {
        if(!firstLayer.getLocked() && !secondLayer.getLocked())
        {
            Bitmap mergedBitmap = null;

            if (getPosition(firstLayer.getLayerID()) > getPosition(secondLayer.getLayerID()))
            {
                mergedBitmap = mergeBitmaps(firstLayer, secondLayer);
            }
            else
            {
                mergedBitmap = mergeBitmaps(secondLayer, firstLayer);
            }

            removeLayer(firstLayer);
            removeLayer(secondLayer);

            Layer layer = createLayer(mergedBitmap);
            layer.setOpacity(100);

            tryAddLayer(layer);

            return layer;
        }

        return null;
    }

    private Bitmap mergeBitmaps(Layer firstLayer, Layer secondLayer)
    {
        Bitmap firstBitmap = firstLayer.getBitmap();
        Bitmap secondBitmap = secondLayer.getBitmap();

        Bitmap bmpOverlay = Bitmap.createBitmap(firstBitmap.getWidth(), firstBitmap.getHeight(), firstBitmap.getConfig());
        Canvas canvas = new Canvas(bmpOverlay);

        Paint overlayPaint = new Paint();
        overlayPaint.setAlpha(firstLayer.getScaledOpacity());

        canvas.drawBitmap(firstBitmap, new Matrix(), overlayPaint);
        overlayPaint.setAlpha(secondLayer.getScaledOpacity());
        canvas.drawBitmap(secondBitmap, 0, 0, overlayPaint);

        return bmpOverlay;
    }

    public Layer resetLayers()
    {
        mLayerList.clear();
        mLayerCounter = 0;
        tryAddLayer();
        return mLayerList.get(0);
    }

    public boolean copyLayer(Layer layer)
    {
        if(mLayerList.size() < mMaxLayer)
        {
            Bitmap bitmap = getLayer(getPosition(layer.getLayerID())).getBitmap();
            Layer layerCopy = createLayer(bitmap.copy(bitmap.getConfig(), true));
            mLayerList.add(layerCopy);

            notifyDataSetChanged();
            return true;
        }

        return false;
    }

    public void moveLayerUp(int currentLayerId)
    {
        int PositionCurrentLayer = getPosition(currentLayerId);

        if (PositionCurrentLayer > 0) //because layer is always added to 0 position.
        {
            Collections.swap(mLayerList, PositionCurrentLayer, PositionCurrentLayer - 1);
        }
    }

    public void moveLayerDown(int currentLayerId)
    {
        int PositionCurrentLayer = getPosition(currentLayerId);

        if (PositionCurrentLayer < mMaxLayer)//because layer is always added to 0 position.
        {
            Collections.swap(mLayerList, PositionCurrentLayer, PositionCurrentLayer + 1);
        }
    }

    public void moveLayerOnTop(int currentLayerId)
    {
        int PositionCurrentLayer = getPosition(currentLayerId);

        if (PositionCurrentLayer > 0) //because layer is always added to 0 position.
        {
            Collections.swap(mLayerList, PositionCurrentLayer, 0);
        }
    }

    public void moveLayerToBottom(int currentLayerId)
    {
        int PositionCurrentLayer = getPosition(currentLayerId);

        if (PositionCurrentLayer < mMaxLayer) //because layer is always added to 0 position.
        {
            Collections.swap(mLayerList, PositionCurrentLayer, mLayerList.size() - 1);
        }
    }

    @Override
    public int getCount()
    {
        return mLayerList.size();
    }

    @Override
    public Object getItem(int position)
    {
        if((mLayerList.size() - 1) >= position)
        {
            return mLayerList.get(position);
        }

        return null;
    }

    @Override
    public long getItemId(int position)
    {
        if((mLayerList.size() - 1) >= position)
        {
            return  mLayerList.get(position).getLayerID();
        }

        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View rowView = convertView;

        if (rowView == null)
        {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            rowView = inflater.inflate(R.layout.layer_button, null);

            ImageView imageView = (ImageView) rowView.findViewById(R.id.layer_button_image);
            imageView.setImageBitmap(mLayerList.get(position).getBitmap());

            TextView textView = (TextView) rowView.findViewById(R.id.layer_button_text);
            textView.setText(mLayerList.get(position).getName());

            LinearLayout linear_layout = (LinearLayout)rowView.findViewById(R.id.layer_button);

            if(mLayerList.get(position).getSelected())
            {
                linear_layout.setBackgroundColor(mContext.getResources().getColor(R.color.color_chooser_blue1));
            }
            else
            {
                linear_layout.setBackgroundColor(mContext.getResources().getColor(R.color.custom_background_color));
            }

            ImageView imageVisible = (ImageView) rowView.findViewById(R.id.layer_image_visible);

            if(mLayerList.get(position).getVisible())
            {
                imageVisible.setVisibility(View.INVISIBLE);
            }
            else
            {
                imageVisible.setVisibility(View.VISIBLE);
            }

            TextView layerOpacityText = (TextView) rowView.findViewById(R.id.layer_opacity_text);
            layerOpacityText.setText(mLayerList.get(position).getOpacity() + "%");

            ImageView imageLock = (ImageView) rowView.findViewById(R.id.layer_image_locked);

            if(mLayerList.get(position).getLocked())
            {
                imageLock.setVisibility(View.VISIBLE);
            }
            else
            {
                imageLock.setVisibility(View.INVISIBLE);
            }
        }

        return rowView;
    }

    @Override
    public void onLayerAdded(Layer layer)
    {
        tryAddLayer(layer);
    }

    @Override
    public void onLayerRemoved(Layer layer)
    {
        removeLayer(layer);
    }

	/* EXCLUDE PREFERENCES FOR RELEASE */
    // private void deactivateToolsFromPreferences() {
    // SharedPreferences sharedPreferences = PreferenceManager
    // .getDefaultSharedPreferences(mContext);
    // for (int toolsIndex = 0; toolsIndex < mButtonsList.size(); toolsIndex++)
    // {
    // final String toolButtonText = mContext.getString(mButtonsList.get(
    // toolsIndex).getNameResource());
    // if (sharedPreferences.getBoolean(toolButtonText, false) == false) {
    // mButtonsList.remove(toolsIndex);
    // toolsIndex--;
    // }
    // }
    // }

}
