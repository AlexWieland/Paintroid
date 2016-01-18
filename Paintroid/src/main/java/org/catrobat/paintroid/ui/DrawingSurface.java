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
import org.catrobat.paintroid.command.implementation.LayerCommand;
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
    private static final int DRAW_THREAD_TIMEOUT = 20;
    private static final int SDK_VERSION = 18;

    private Layer mCurrentLayer;
    private Canvas mWorkingBitmapCanvas;
    private Rect mWorkingBitmapRect;
    private Paint mFramePaint;
    private Paint mClearPaint;
    public Bitmap mTestBitmap;

    protected boolean mIsSurfaceDrawable;
    private DrawingSurfaceThread mDrawingThread;


    public DrawingSurface(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        init();
    }

    public DrawingSurface(Context context) {
        super(context);
        init();
    }

    private void init()
    {
        getHolder().addCallback(this);

        mWorkingBitmapRect = new Rect();
        mWorkingBitmapCanvas = new Canvas();

        mFramePaint = new Paint();
        mFramePaint.setColor(Color.BLACK);
        mFramePaint.setStyle(Paint.Style.STROKE);

        mClearPaint = new Paint();
        mClearPaint.setColor(Color.TRANSPARENT);
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        Log.w(PaintroidApplication.TAG, "DrawingSurfaceView.surfaceChanged");
        PaintroidApplication.perspective.setSurfaceHolder(holder);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.w(PaintroidApplication.TAG, "DrawingSurfaceView.surfaceCreated");
        mDrawingThread = new DrawingSurfaceThread(new DrawLoop());
        mIsSurfaceDrawable = true;

        if (mCurrentLayer.getBitmap() != null && mDrawingThread != null)
        {
            starDrawingThread();
        }
    }

    @Override
    public synchronized void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.w(PaintroidApplication.TAG, "DrawingSurfaceView.surfaceDestroyed");
        mIsSurfaceDrawable = false;
        stopDrawingThread();
    }

    private synchronized void starDrawingThread()
    {
        if (mIsSurfaceDrawable)
        {
            mDrawingThread.start();
        }
    }

    private synchronized void stopDrawingThread()
    {
        if (mDrawingThread != null)
        {
            mDrawingThread.stop();
        }
    }

    private synchronized void doDraw(Canvas surfaceViewCanvas)
    {
        try
        {
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

            if (mCurrentLayer != null)
            {
                redrawAllTheLayers(surfaceViewCanvas);
            }
        }
        catch (Exception catchAllException)
        {
            Log.e(PaintroidApplication.TAG, "DrawingSurface:"
                    + catchAllException.getMessage() + "\r\n"
                    + catchAllException.toString());
            catchAllException.printStackTrace();
        }
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

        //top-bottom drawing order, otherwise changes on upper layer not visible.
        for(int position = layersAdapter.getCount() - 1; position >= 0; position--)
        {
            Layer layer = layersAdapter.getLayer(position);

            if(layer.getVisible())
            {
                Paint opacity = new Paint();
                opacity.setAlpha(layer.getScaledOpacity());
                canvas.drawBitmap(layer.getBitmap(), 0, 0, opacity);
            }
        }

        PaintroidApplication.currentTool.draw(canvas);
    }

    public synchronized void setCurrentLayer(Layer layer)
    {
        mCurrentLayer = layer;

        if (mCurrentLayer != null)
        {
            mWorkingBitmapCanvas.setBitmap(mCurrentLayer.getBitmap());
            mWorkingBitmapRect.set(0, 0, mCurrentLayer.getBitmap().getWidth(), mCurrentLayer.getBitmap().getHeight());
        }
    }

    public Layer getCurrentLayer()
    {
        return mCurrentLayer;
    }

    public boolean canDrawOnSurface()
    {
        return mCurrentLayer.getVisible() && !mCurrentLayer.getLocked();
    }

    public boolean isSurfaceLocked()
    {
        return mCurrentLayer.getLocked();
    }

    public synchronized void loadBitmapIntoCurrentLayer(Bitmap bitmap)
    {
        PaintroidApplication.perspective.resetScaleAndTranslation();
        updateBitmap(bitmap);
    }

    public synchronized void updateBitmap(Bitmap bitmap)
    {
        if (bitmap != null)
        {
            mCurrentLayer.setBitmap(bitmap);
            mWorkingBitmapCanvas.setBitmap(bitmap);
            mWorkingBitmapRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
            PaintroidApplication.commandManager
                                .commitCommand(new LayerCommand(LayerCommand.LayerAction.INSERT_IMAGE));
        }
    }

    public synchronized Bitmap getBitmapCopy()
    {
        if (mCurrentLayer != null && mCurrentLayer.getBitmap().isRecycled() == false)
        {
            return Bitmap.createBitmap(mCurrentLayer.getBitmap());
        }
        else
        {
            return null;
        }
    }

    public synchronized boolean isDrawingSurfaceBitmapValid()
    {
        if (mCurrentLayer == null || mCurrentLayer.getBitmap().isRecycled()|| mIsSurfaceDrawable)
        {
            return false;
        }

        return true;
    }

    public int getPixel(PointF coordinate)
    {
        try
        {
            if (mCurrentLayer != null)
            {
                return mCurrentLayer.getBitmap().getPixel((int) coordinate.x, (int) coordinate.y);
            }
        }
        catch (IllegalArgumentException e)
        {
            Log.w(PaintroidApplication.TAG, "getBitmapColor coordinate out of bounds");
        }
        return Color.TRANSPARENT;
    }

    public int getVisiblePixel(PointF coordinate)
    {
        try
        {
            if (mTestBitmap != null && mTestBitmap.isRecycled() == false)
            {
                return mTestBitmap.getPixel((int) coordinate.x, (int) coordinate.y);
            }
        }
        catch (IllegalArgumentException e)
        {
            Log.w(PaintroidApplication.TAG, "getBitmapColor coordinate out of bounds");
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
        if(mCurrentLayer != null)
        {
            if (mCurrentLayer.getBitmap() == null)
            {
                return -1;
            }
            return mCurrentLayer.getBitmap().getWidth();
        }

        return 0;
    }

    public int getBitmapHeight()
    {
        if(mCurrentLayer != null)
        {
            if (mCurrentLayer.getBitmap() == null)
            {
                return -1;
            }
            return mCurrentLayer.getBitmap().getHeight();
        }

        return 0;
    }

    private class DrawLoop implements Runnable {
        @Override
        public void run()
        {
            SurfaceHolder holder = getHolder();
            Canvas canvas = null;

            if (Build.VERSION.SDK_INT >= SDK_VERSION) // TODO: set build flag
            {
                try
                {
                    Thread.sleep(DRAW_THREAD_TIMEOUT);
                }
                catch (InterruptedException e)
                {
                    Log.w(PaintroidApplication.TAG, "DrawingSurface: sleeping thread was interrupted");
                }
            }

            synchronized (holder)
            {
                try
                {
                    canvas = holder.lockCanvas();

                    if (canvas != null && mIsSurfaceDrawable)
                    {
                        doDraw(canvas);
                    }
                }
                finally
                {
                    if (canvas != null)
                    {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
