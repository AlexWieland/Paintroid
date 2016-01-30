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

package org.catrobat.paintroid.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.catrobat.paintroid.MainActivity;
import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.command.implementation.ClearCommand;
import org.catrobat.paintroid.command.implementation.LayerCommand;
import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.ui.button.LayersAdapter;

public final class LayersDialog extends BaseDialog implements OnItemClickListener
		,OnItemLongClickListener
		,DialogInterface.OnDismissListener,
		SeekBar.OnSeekBarChangeListener {

	private static final String NOT_INITIALIZED_ERROR_MESSAGE = "LayerDialog has not been " +
			"initialized. Call init() first!";

	private static LayersDialog instance;

	private LayersAdapter mLayerButtonAdapter;
	private ImageButton mNewLayerButton;
	private ImageButton mDeleteLayerButton;
	private ImageButton mNameLayerButton;
	private ImageButton mVisibleLayerButton;
	private ImageButton mLockLayerButton;
	private ImageButton mMergeLayerButton;
	private MainActivity mParent;
	private Layer mCurrentLayer;
	private Context mContext;
	private Layer firstLayertoMerge;
	private Boolean mergeClicked;
	private SeekBar mOpacitySeekbar;
	private TextView mOpacitySeekbarLabel;

	public LayersAdapter getAdapter()
	{
		return mLayerButtonAdapter;
	}

	private LayersDialog(Context context, Layer first_layer)
    {
		super(context);
		mContext = context;
		mParent = (MainActivity) context;
		mLayerButtonAdapter = new LayersAdapter(context, PaintroidApplication.openedFromCatroid, first_layer);
		selectFirstLayer();
	}

	public static void init(MainActivity mainActivity, Layer first_layer)
	{
		instance = new LayersDialog(mainActivity, first_layer);
	}

	public static LayersDialog getInstance()
	{
		if (instance == null)
        {
			throw new IllegalStateException(NOT_INITIALIZED_ERROR_MESSAGE);
		}

		return instance;
	}

	private void selectFirstLayer()
	{
		if(mLayerButtonAdapter == null)
		{
			return;
		}

		mCurrentLayer = mLayerButtonAdapter.getLayer(0);
		if (mCurrentLayer != null)
		{
			selectLayer(mCurrentLayer);
			return;
		}

		Log.d("DEBUG", "CURRENT LAYER NOT INITIALIZED");
	}

	public Layer getCurrentLayer()
	{
		if(mCurrentLayer == null)
		{
			selectFirstLayer();
		}
		return mCurrentLayer;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layers_menu);
		setTitle(R.string.layers_title);
		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);
		GridView gridView = (GridView) findViewById(R.id.gridview_layers_menu);

		gridView.setAdapter(mLayerButtonAdapter);
		gridView.setOnItemClickListener(this);
		gridView.setOnItemLongClickListener(this);

		mNewLayerButton = (ImageButton) findViewById(R.id.mButtonLayerNew);
		mNewLayerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				createLayer();
			}
		});

		mDeleteLayerButton = (ImageButton)findViewById(R.id.mButtonLayerDelete);
		mDeleteLayerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				deleteLayer();
			}
		});

		mNameLayerButton = (ImageButton)findViewById(R.id.mButtonLayerRename);
		mNameLayerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				renameLayer();
			}
		});

		mVisibleLayerButton = (ImageButton)	findViewById(R.id.mButtonLayerVisible);
		mVisibleLayerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				toggleLayerVisible();
			}
		});

		mLockLayerButton = (ImageButton)findViewById(R.id.mButtonLayerLock);
		mLockLayerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				toggleLayerLocked();
			}
		});

		mOpacitySeekbarLabel = (TextView) findViewById(R.id.layer_opacity_seekbar_label	);
		mOpacitySeekbarLabel.setText(R.string.layer_opacity);

		mergeClicked = false;
		mMergeLayerButton = (ImageButton)findViewById(R.id.mButtonLayerMerge);
		mMergeLayerButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(!mCurrentLayer.getLocked())
				{
					if (mergeClicked)
					{
						mergeButtonDisabled();
					}
					else
					{
						mergeButtonEnabled();
						firstLayertoMerge = mCurrentLayer;
					}

					mergeLayer();
				}
			}
		});

		mOpacitySeekbar = (SeekBar) findViewById(R.id.seekbar_layer_opacity);
		mOpacitySeekbar.setProgress(mCurrentLayer.getOpacity());
		mOpacitySeekbar.setOnSeekBarChangeListener(this);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View button,	int position, long id)
	{
		selectLayer(mLayerButtonAdapter.getLayer(position));

		AlertDialog.Builder alertChooseNewBuilder = new AlertDialog.Builder(this.getContext());

		alertChooseNewBuilder.setTitle(mCurrentLayer.getName()).setItems(R.array.edit_layer
									   ,new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which)
				{
					case 0:
						copyLayer();
						break;
					case 1:
						moveLayerUp();
						break;
					case 2:
						moveLayerDown();
						break;
					case 3:
						moveLayerOnTop();
						break;
					case 4:
						moveLayerToBottom();
						break;
				}
			}
		});

		AlertDialog alertNew = alertChooseNewBuilder.create();
		alertNew.show();
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View button, int position, long id)
	{
		selectLayer(mLayerButtonAdapter.getLayer(position));
		if(mergeClicked)
		{
			mergeLayer();
		}
	}

	public void createLayer()
	{
		if(mLayerButtonAdapter.tryAddLayer())
		{
			refreshView();
            Layer layer = mLayerButtonAdapter.getLayer(0);
			PaintroidApplication.commandManager.commitCommand(new LayerCommand(LayerCommand.LayerAction.ADD, layer, null));
		}
		else
		{
			Log.d(PaintroidApplication.ERROR_TAG
					, String.format("Could not add layer. Current layer count %d"
					, mLayerButtonAdapter.getCount()));
		}
	}

	public void deleteLayer()
	{
		int layerCount = mLayerButtonAdapter.getCount();

		if(layerCount == 1 || mCurrentLayer == null)
		{
			return;
		}

		int currentLayerPosition = mLayerButtonAdapter.getPosition(mCurrentLayer.getLayerID());
		int adjacentLayerPosition = currentLayerPosition;

		if (currentLayerPosition == layerCount - 1 && layerCount > 1)
		{
			adjacentLayerPosition = currentLayerPosition - 1;
		}

		mLayerButtonAdapter.removeLayer(mCurrentLayer.getLayerID());
		selectLayer(mLayerButtonAdapter.getLayer(adjacentLayerPosition));
		refreshView();

		Command command = new LayerCommand(LayerCommand.LayerAction.REMOVE, mCurrentLayer, null);
		PaintroidApplication.commandManager.commitCommand(command);
	}

	public void selectLayer(Layer layer)
	{
		if(mCurrentLayer != null)
		{
			mCurrentLayer.setSelected(false);
		}

		mCurrentLayer = layer;
		mCurrentLayer.setSelected(true);

		mOpacitySeekbar = (SeekBar) findViewById(R.id.seekbar_layer_opacity);
		if(mOpacitySeekbar != null)
		{
			mOpacitySeekbar.setProgress(mCurrentLayer.getOpacity());
		}
		else
		{
			Log.d("DEBUG", "OPACITYSEEKBAR NOT INTIALIZED");
		}

		refreshView();

		PaintroidApplication.drawingSurface.setCurrentLayer(mCurrentLayer);
	}

	public void renameLayer()
	{
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		alertBuilder.setTitle(R.string.layer_rename_title);

		final EditText input = new EditText(mContext);
		input.setTextColor(Color.WHITE);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		input.requestFocus();

		final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

		alertBuilder.setView(input);

		alertBuilder.setPositiveButton(R.string.layer_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				mCurrentLayer.setName(input.getText().toString());
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				Command command = new LayerCommand(LayerCommand.LayerAction.RENAME, mCurrentLayer, null);
				PaintroidApplication.commandManager.commitCommand(command);
				refreshView();
			}
		});

		alertBuilder.setNegativeButton(R.string.layer_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				dialog.cancel();
			}
		});

		alertBuilder.show();
	}

	public void refreshView()
	{
		if(mLayerButtonAdapter != null)
		{
			GridView gridView = (GridView) findViewById(R.id.gridview_layers_menu);
			if (gridView != null)
			{
				gridView.setAdapter(mLayerButtonAdapter);
			}
			else
			{
				Log.d("DEBUG", "LAYERGRIDVIEW NOT INITIALIZED");
			}
		}
		else
		{
			Log.d("DEBUG","LAYERBUTTONADAPTER NOT INITIALIZED");
		}
	}

	public void toggleLayerVisible()
	{
		mCurrentLayer.setVisible(!mCurrentLayer.getVisible());
		Command command = new LayerCommand(LayerCommand.LayerAction.SET_VISIBILITY, mCurrentLayer, null);
		PaintroidApplication.commandManager.commitCommand(command);
		refreshView();
	}

	public void toggleLayerLocked()
	{
		mCurrentLayer.setLocked(!mCurrentLayer.getLocked());
		Command command = new LayerCommand(LayerCommand.LayerAction.SET_LOCK, mCurrentLayer, null);
		PaintroidApplication.commandManager.commitCommand(command);
		refreshView();
	}

	public void mergeLayer()
	{
		if(mCurrentLayer.getLayerID() != firstLayertoMerge.getLayerID())
		{
			if(!mCurrentLayer.getLocked())
			{
				Bitmap mergedBitmap = null;
				if (mLayerButtonAdapter.getPosition(firstLayertoMerge.getLayerID())
						< mLayerButtonAdapter.getPosition(firstLayertoMerge.getLayerID()))
				{
					mergedBitmap = overlay(firstLayertoMerge, mCurrentLayer);
				}
				else
				{
					mergedBitmap =  overlay(mCurrentLayer, firstLayertoMerge);
				}

				mCurrentLayer.setOpacity(100);
				mCurrentLayer.setBitmap(mergedBitmap);
				mCurrentLayer.setName(mCurrentLayer.getName());

				mLayerButtonAdapter.removeLayer(firstLayertoMerge.getLayerID());

				Command command = new LayerCommand(LayerCommand.LayerAction.MERGE, mCurrentLayer, firstLayertoMerge);
				PaintroidApplication.commandManager.commitCommand(command);

				mergeButtonDisabled();
				refreshView();
			}
		}
	}

	public void mergeLayerCalledFromCommand(int LayerToMergeID)
	{
		Layer LayerToMerge = mLayerButtonAdapter.getLayer(mLayerButtonAdapter.getPosition(LayerToMergeID));
		if(mCurrentLayer.getLayerID() != LayerToMerge.getLayerID())
		{
			if(!mCurrentLayer.getLocked())
			{
				Bitmap mergedBitmap = null;
				if (mLayerButtonAdapter.getPosition(mCurrentLayer.getLayerID())
						< mLayerButtonAdapter.getPosition(LayerToMerge.getLayerID()))
				{
					mergedBitmap = overlay(LayerToMerge, mCurrentLayer);
				}
				else
				{
					mergedBitmap =  overlay(mCurrentLayer, LayerToMerge);
				}

				mCurrentLayer.setOpacity(100);
				mCurrentLayer.setBitmap(mergedBitmap);
				mCurrentLayer.setName(mCurrentLayer.getName());

				mLayerButtonAdapter.removeLayer(LayerToMergeID);
				refreshView();
			}
		}

	}

	public static Bitmap overlay(Layer layer1, Layer layer2)
	{
		Bitmap bmp1 = layer1.getBitmap();
		Bitmap bmp2 = layer2.getBitmap();

		Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
		Canvas canvas = new Canvas(bmOverlay);

		Paint overlayPaint = new Paint();
		overlayPaint.setAlpha(layer1.getScaledOpacity());

		canvas.drawBitmap(bmp1, new Matrix(), overlayPaint);
		overlayPaint.setAlpha(layer2.getScaledOpacity());
		canvas.drawBitmap(bmp2, 0, 0, overlayPaint);

		return bmOverlay;
	}

	public void onDismiss(DialogInterface dialog)
    {
		mergeButtonDisabled();
	}

	private void mergeButtonEnabled()
	{
		mMergeLayerButton.setBackgroundColor(Color.rgb(0, 180, 241));
		mergeClicked = true;
	}

	private void mergeButtonDisabled()
	{
		mMergeLayerButton.setBackgroundColor(Color.BLACK);
		mergeClicked = false;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		mCurrentLayer.setOpacity(seekBar.getProgress());
		refreshView();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { }

    public void resetLayers()
	{
		selectLayer(mLayerButtonAdapter.resetLayers());
		refreshView();
	}

	public void copyLayer()
	{
		mLayerButtonAdapter.copyLayer(mCurrentLayer.getLayerID());
		refreshView();
	}

	public void moveLayerUp()
	{
		mLayerButtonAdapter.moveLayerUp(mCurrentLayer.getLayerID());
		refreshView();
	}

	public void moveLayerDown()
	{
		mLayerButtonAdapter.moveLayerDown(mCurrentLayer.getLayerID());
		refreshView();
	}

	public void moveLayerOnTop()
	{
		mLayerButtonAdapter.moveLayerOnTop(mCurrentLayer.getLayerID());
		refreshView();
	}

	public void moveLayerToBottom()
	{
		mLayerButtonAdapter.moveLayerToBottom(mCurrentLayer.getLayerID());
		refreshView();
	}

	public void resetForIntent(Context context, Layer first_Layer)
	{
		mContext = context;
		mParent = (MainActivity) context;
		mLayerButtonAdapter = new LayersAdapter(context, PaintroidApplication.openedFromCatroid
												,first_Layer);
	}
}
