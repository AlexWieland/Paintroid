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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.ui.DrawingSurface;

import java.util.ArrayList;
import java.util.Collections;

public class LayersAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<Layer> mLayerList;
    private int mLayerCount = 0;
	private int mMaxLayer = 7;

	public LayersAdapter(Context context, boolean fromCatrobat, Layer first_layer) {
		this.mContext = context;
		initLayers(fromCatrobat, first_layer);
	}

	//is fromCatrobat relevant?!
	private void initLayers(boolean fromCatrobat, Layer first_layer)
	{
		mLayerList = new ArrayList<Layer>();
        mLayerList.add(first_layer);
		mLayerCount++;
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
			return  mLayerList.get(position);
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

		return 0;
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
				return position;
		}

		return -1;
	}
    public boolean tryAddLayer()
	{
        if(mLayerList.size() < mMaxLayer)
		{
            DrawingSurface drawingSurface = PaintroidApplication.drawingSurface;
            Bitmap image = Bitmap.createBitmap(drawingSurface.getBitmapWidth()
											   ,drawingSurface.getBitmapHeight()
											   ,Bitmap.Config.ARGB_8888);
            mLayerList.add(0, new Layer(mLayerCount, image));

			mLayerCount++;
            notifyDataSetChanged();
			return true;
        }
        else
            return false;
    }

	public void removeLayer(int layer_to_remove)
	{
		if(mLayerList.size() > 1) {
			for(int i = 0; i < mLayerList.size(); ++i)
			{
				if(mLayerList.get(i).getLayerID() == layer_to_remove) {
					mLayerList.remove(i);
					break;
				}
			}
			notifyDataSetChanged();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			rowView = inflater.inflate(R.layout.layer_button, null);
			ImageView imageView = (ImageView) rowView
					.findViewById(R.id.layer_button_image);
			imageView.setImageBitmap(mLayerList.get(position).getBitmap());
			TextView textView = (TextView) rowView
					.findViewById(R.id.layer_button_text);
            textView.setText(mLayerList.get(position).getName());
			LinearLayout linear_layout = (LinearLayout)rowView.findViewById(R.id.layer_button);

			if(mLayerList.get(position).getSelected()) {
				linear_layout.setBackgroundColor(
						mContext.getResources().getColor(R.color.color_chooser_blue1));
			} else {
				linear_layout.setBackgroundColor(
						mContext.getResources().getColor(R.color.custom_background_color));
			}
			ImageView imageVisible = (ImageView) rowView.findViewById(R.id.layer_image_visible);
			if(mLayerList.get(position).getVisible())
			{
				imageVisible.setVisibility(View.INVISIBLE);
			}else{
				imageVisible.setVisibility(View.VISIBLE);
			}

			TextView layerOpacityText = (TextView) rowView.findViewById(R.id.layer_opacity_text);
			layerOpacityText.setText(""+mLayerList.get(position).getOpacity()+"%");
			ImageView imageLock = (ImageView) rowView.findViewById(R.id.layer_image_locked);
			if(mLayerList.get(position).getLocked())
			{
				imageLock.setVisibility(View.VISIBLE);
			}else{
				imageLock.setVisibility(View.INVISIBLE);
			}
		}
		return rowView;
	}

	public Layer clearLayer() {
		if(mLayerList.size() >= 1) {
			for(int i = mLayerList.size() - 1; i >= 0; i--)
			{
					mLayerList.remove(i);
			}
		}
		mLayerCount = 0;
		tryAddLayer();
		return mLayerList.get(0);
	}

	public void copy(int currentLayer) {

		if(mLayerList.size() < mMaxLayer) {
			Bitmap image = mLayerList.get(currentLayer).getBitmap().copy(mLayerList.get(currentLayer).getBitmap().getConfig(), true);
			mLayerList.add(0, new Layer(mLayerCount, image));
			mLayerCount++;
			notifyDataSetChanged();
		}

	}

	public void swapUp(int IDcurrentLayer) {
		int PositionCurrentLayer = getPosition(IDcurrentLayer);
		if (PositionCurrentLayer > 0)
			Collections.swap(mLayerList, PositionCurrentLayer, PositionCurrentLayer - 1);

	}

	public void swapDown(int IDcurrentLayer) {
		int PositionCurrentLayer = getPosition(IDcurrentLayer);
		if (PositionCurrentLayer < mLayerList.size()-1)
			Collections.swap(mLayerList, PositionCurrentLayer, PositionCurrentLayer + 1);
	}

	public void swapTop(int IDcurrentLayer) {
		int PositionCurrentLayer = getPosition(IDcurrentLayer);
		if (PositionCurrentLayer > 0)
			Collections.swap(mLayerList, PositionCurrentLayer, 0);
	}

	public void swapBottom(int IDcurrentLayer) {
		int PositionCurrentLayer = getPosition(IDcurrentLayer);
		if (PositionCurrentLayer < mLayerList.size()-1)
			Collections.swap(mLayerList, PositionCurrentLayer, mLayerList.size()-1);
	}

	public void clearAndInitLayer(Bitmap first_layer) {

		if(mLayerList.size() >= 1) {
			for(int i = mLayerList.size() - 1; i >= 0; i--)
			{
				mLayerList.remove(i);
			}
		}
		mLayerCount = 0;

		mLayerList = new ArrayList<Layer>();

		mLayerList.add(new Layer(0, first_layer));
		mLayerCount++;
//		notifyDataSetChanged();
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
