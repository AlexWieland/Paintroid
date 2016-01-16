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

package org.catrobat.paintroid.ui;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.dialog.IndeterminateProgressDialog;
import org.catrobat.paintroid.dialog.LayersDialog;
import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.tools.Tool.StateChange;
import org.catrobat.paintroid.tools.implementation.BaseTool;
import org.catrobat.paintroid.ui.button.LayersAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DrawingSurface extends SurfaceView implements 	SurfaceHolder.Callback {

    /*protected static final String BUNDLE_INSTANCE_STATE = "BUNDLE_INSTANCE_STATE";
	protected static final String BUNDLE_PERSPECTIVE = "BUNDLE_PERSPECTIVE";*/

    protected static final int BACKGROUND_COLOR = Color.LTGRAY;

    private DrawingSurfaceThread mDrawingThread;
    private Layer mCurrentLayer;
    private Canvas mWorkingBitmapCanvas;
    private Rect mWorkingBitmapRect;
    private Paint mFramePaint;
    private Paint mClearPaint;

    protected boolean mSurfaceCanBeUsed;
    private Paint mOpacityPaint;

    public Bitmap mTestBitmap;

    public DrawingSurface(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        init();
    }

    public DrawingSurface(Context context) {
        super(context);
        init();
    }

    private void init() {
        getHolder().addCallback(this);

        mWorkingBitmapRect = new Rect();
        mWorkingBitmapCanvas = new Canvas();

        mFramePaint = new Paint();
        mFramePaint.setColor(Color.BLACK);
        mFramePaint.setStyle(Paint.Style.STROKE);

        mClearPaint = new Paint();
        mClearPaint.setColor(Color.TRANSPARENT);
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mOpacityPaint = new Paint();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceCanBeUsed = true;
        Log.w(PaintroidApplication.TAG, "DrawingSurfaceView.surfaceChanged"); // TODO
        // remove
        // logging
        PaintroidApplication.perspective.setSurfaceHolder(holder);

        if (mCurrentLayer.getBitmap() != null && mDrawingThread != null) {
            mDrawingThread.start();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.w(PaintroidApplication.TAG, "DrawingSurfaceView.surfaceCreated");
        mDrawingThread = new DrawingSurfaceThread(new DrawLoop());
    }

    @Override
    public synchronized void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceCanBeUsed = false;
        Log.w(PaintroidApplication.TAG, "DrawingSurfaceView.surfaceDestroyed");
        if (mDrawingThread != null) {
            mDrawingThread.stop();
        }
    }

    public synchronized void setCurrentLayer(Layer mCurrentLayer) {
        this.mCurrentLayer = mCurrentLayer;
        if (mCurrentLayer != null) {
            mWorkingBitmapCanvas.setBitmap(mCurrentLayer.getBitmap());
            mWorkingBitmapRect.set(0, 0, mCurrentLayer.getBitmap().getWidth(), mCurrentLayer.getBitmap().getHeight());
            // PaintroidApplication.perspective.resetScaleAndTranslation();
        }
    }

    public Layer getCurrentLayer() {
        return mCurrentLayer;
    }

    public boolean isSurfaceLocked()
    {
        return mCurrentLayer.getLocked();
    }

    private boolean canDrawOnSurface()
    {
        return mCurrentLayer.getVisible() && !mCurrentLayer.getLocked()
                && !mCurrentLayer.getBitmap().isRecycled();
    }

    private void drawCheckeredPatternOnCanvas(Canvas canvas)
    {
        PaintroidApplication.perspective.applyToCanvas(canvas);
        canvas.drawColor(BACKGROUND_COLOR);
        canvas.drawRect(mWorkingBitmapRect, BaseTool.CHECKERED_PATTERN);
        canvas.drawRect(mWorkingBitmapRect, mFramePaint);
    }

    private void redrawAllTheLayers(Canvas canvas)
    {
        LayersDialog layersDialog = LayersDialog.getInstance();

        if(mCurrentLayer != layersDialog.getCurrentLayer())
        {
            mCurrentLayer = layersDialog.getCurrentLayer();
        }

        LayersAdapter layersAdapter = layersDialog.getAdapter();

        mOpacityPaint = new Paint();
        mOpacityPaint.setAlpha(layersDialog.getCurrentLayer().getScaledOpacity());

        for(int position = layersAdapter.getCount()-1; position >= 0; position--)
        {
            Layer layer = layersAdapter.getLayer(position);
            if(layer.getVisible())
            {
                mOpacityPaint.setAlpha(layer.getScaledOpacity());
                Bitmap bitmapDrawable = (layer).getBitmap();
                canvas.drawBitmap(bitmapDrawable, 0, 0, mOpacityPaint);
            }
        }

        PaintroidApplication.currentTool.draw(canvas);
    }

    public synchronized void recycleBitmap() {
        if (mCurrentLayer != null) {
            if(mCurrentLayer.getBitmap() != null){
                mCurrentLayer.getBitmap().recycle();}
        }

        mCurrentLayer = null;
    }

    private synchronized void doDraw(Canvas surfaceViewCanvas) {
        try {
            if (mWorkingBitmapRect == null || surfaceViewCanvas == null || mCurrentLayer == null )
            {
                return;
            }

            drawCheckeredPatternOnCanvas(surfaceViewCanvas);
            Command command = null;

            while (canDrawOnSurface() &&
                    (command = PaintroidApplication.commandManager.getNextCommand()) != null)
            {
                command.run(mWorkingBitmapCanvas, mCurrentLayer);
                PaintroidApplication.currentTool.resetInternalState(StateChange.RESET_INTERNAL_STATE);

                if (!PaintroidApplication.commandManager.hasNextCommand())
                {
                    IndeterminateProgressDialog.getInstance().dismiss();
                }
            }

            if (mCurrentLayer != null && canDrawOnSurface())
            {
                redrawAllTheLayers(surfaceViewCanvas);
            }
        } catch (Exception catchAllException)
        {
            Log.e(PaintroidApplication.TAG, "DrawingSurface:"
                    + catchAllException.getMessage() + "\r\n"
                    + catchAllException.toString());
            catchAllException.printStackTrace();
        }
    }

    /** parcable - doesnt seem to effect app. Need to investigate.
     @Override
     public Parcelable onSaveInstanceState() {
     Bundle bundle = new Bundle();
     bundle.putParcelable(BUNDLE_INSTANCE_STATE, super.onSaveInstanceState());
     bundle.putSerializable(BUNDLE_PERSPECTIVE,
     PaintroidApplication.perspective);
     return bundle;
     }

     @Override
     public void onRestoreInstanceState(Parcelable state) {
     if (state instanceof Bundle) {
     Bundle bundle = (Bundle) state;
     PaintroidApplication.perspective = (Perspective) bundle
     .getSerializable(BUNDLE_PERSPECTIVE);
     super.onRestoreInstanceState(bundle
     .getParcelable(BUNDLE_INSTANCE_STATE));
     } else {
     super.onRestoreInstanceState(state);
     }
     }
     */

    public synchronized void resetBitmap(Bitmap bitmap) {
        PaintroidApplication.commandManager.resetAndClear();
        PaintroidApplication.commandManager.setOriginalBitmap(bitmap);
        setBitmap(bitmap);
        PaintroidApplication.perspective.resetScaleAndTranslation();
        if (mSurfaceCanBeUsed) {
            mDrawingThread.start();
        }
    }

    public synchronized void setBitmap(Bitmap bitmap) {
//		if (mWorkingBitmap != null && bitmap != null) {
//			mWorkingBitmap.recycle();
//		}
        if (bitmap != null) {
            mCurrentLayer.setBitmap(bitmap);
            mWorkingBitmapCanvas.setBitmap(bitmap);
            mWorkingBitmapRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
            // PaintroidApplication.perspective.resetScaleAndTranslation();
        }
    }

    public synchronized Bitmap getBitmapCopy() {
        if (mCurrentLayer != null && mCurrentLayer.getBitmap().isRecycled() == false) {
            return Bitmap.createBitmap(mCurrentLayer.getBitmap());
        } else {
            return null;
        }
    }

    public synchronized boolean isDrawingSurfaceBitmapValid() {
        if (mCurrentLayer == null || mCurrentLayer.getBitmap().isRecycled()
                || mSurfaceCanBeUsed == false) {
            return false;
        }
        return true;
    }

    public int getPixel(PointF coordinate) {
        try {
            if (mCurrentLayer != null && mCurrentLayer.getBitmap().isRecycled() == false) {
                return mCurrentLayer.getBitmap().getPixel((int) coordinate.x,
                        (int) coordinate.y);
            }
        } catch (IllegalArgumentException e) {
            Log.w(PaintroidApplication.TAG,
                    "getBitmapColor coordinate out of bounds");
        }
        return Color.TRANSPARENT;
    }

    public int getVisiblePixel(PointF coordinate) {
        try {
            if (mTestBitmap != null && mTestBitmap.isRecycled() == false) {
                return mTestBitmap.getPixel((int) coordinate.x,
                        (int) coordinate.y);
            }
        } catch (IllegalArgumentException e) {
            Log.w(PaintroidApplication.TAG,
                    "getBitmapColor coordinate out of bounds");
        }
        return Color.TRANSPARENT;
    }

    public void getPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height)
    {
        if (mCurrentLayer != null && mCurrentLayer.getBitmap().isRecycled() == false)
        {
            mCurrentLayer.getBitmap().getPixels(pixels, offset, stride, x, y, width,	height);
        }
    }

    public int getBitmapWidth() {
        if(mCurrentLayer != null) {
            if (mCurrentLayer.getBitmap() == null) {
                return -1;
            }
            return mCurrentLayer.getBitmap().getWidth();
        }

        return 0;
    }

    public int getBitmapHeight() {
        if(mCurrentLayer != null) {
            if (mCurrentLayer.getBitmap() == null) {
                return -1;
            }
            return mCurrentLayer.getBitmap().getHeight();
        }

        return 0;
    }

    private class DrawLoop implements Runnable {
        @Override
        public void run() {
            SurfaceHolder holder = getHolder();
            Canvas canvas = null;

            if (Build.VERSION.SDK_INT >= 18) { // TODO: set build flag
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Log.w(PaintroidApplication.TAG, "DrawingSurface: sleeping thread was interrupted");
                }
            }

            synchronized (holder) {
                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null && mSurfaceCanBeUsed == true) {
                        doDraw(canvas);
                    }
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
