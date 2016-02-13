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
import org.catrobat.paintroid.dialog.LayersDialog;
import org.catrobat.paintroid.eventlistener.ChangeActiveLayerEventListener;
import org.catrobat.paintroid.eventlistener.RedrawSurfaceViewEventListener;
import org.catrobat.paintroid.listener.DrawingSurfaceListener;
import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.tools.implementation.BaseTool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DrawingSurface extends SurfaceView implements SurfaceHolder.Callback
                                                            ,RedrawSurfaceViewEventListener
                                                            ,ChangeActiveLayerEventListener
{

    protected static final int BACKGROUND_COLOR = Color.LTGRAY;
    private static final int DRAW_THREAD_TIMEOUT = 20;
    private static final int SDK_VERSION = 18;

    private final Lock mDrawLock;
    private final Condition mDrawCondition;
    private boolean mDrawFlag;
    private DrawingThread mDrawingThread;
    private SurfaceHolder mHolder;
    private SurfaceViewDrawTrigger mSurfaceViewDrawTrigger;
    private DrawingSurfaceListener mDrawingSurfaceListener;


    private Layer mCurrentLayer;
    private Canvas mWorkingBitmapCanvas;
    private Rect mWorkingBitmapRect;
    private Paint mFramePaint;
    private Paint mClearPaint;
    public Bitmap mTestBitmap;

    protected boolean mIsSurfaceDrawable;

    public DrawingSurface(Context context, AttributeSet attrSet)
    {
        super(context, attrSet);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mSurfaceViewDrawTrigger = new SurfaceViewDrawTrigger();

        mDrawLock = new ReentrantLock();
        mDrawCondition = mDrawLock.newCondition();
        mDrawFlag = true;

        init();
    }

    public void initDrawSurfaceListener()
    {
        PaintroidApplication.perspective = new Perspective(mHolder);
        mDrawingSurfaceListener = new DrawingSurfaceListener(mSurfaceViewDrawTrigger);
        setOnTouchListener(mDrawingSurfaceListener);
        mDrawingSurfaceListener.setDrawListener(mSurfaceViewDrawTrigger);
    }

    private void init()
    {
        mWorkingBitmapRect = new Rect();
        mWorkingBitmapCanvas = new Canvas();

        mFramePaint = new Paint();
        mFramePaint.setColor(Color.BLACK);
        mFramePaint.setStyle(Paint.Style.STROKE);

        mClearPaint = new Paint();
        mClearPaint.setColor(Color.TRANSPARENT);
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public DrawSurfaceTrigger getSurfaceViewDrawTrigger()
    {
        return  mSurfaceViewDrawTrigger;
    }

    public Canvas getWorkingCanvas()
    {
        return  mWorkingBitmapCanvas;
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
        mIsSurfaceDrawable = true;
        PaintroidApplication.perspective.setSurfaceHolder(holder);
        if (mDrawingThread != null)
        {
            mDrawingThread.stopThread();
            mDrawingThread.mDrawingSurfaceInstance = null;
        }
        mDrawingThread = new DrawingThread(this);
        mDrawingThread.start();
    }

    @Override
    public synchronized void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.w(PaintroidApplication.TAG, "DrawingSurfaceView.surfaceDestroyed");
        mIsSurfaceDrawable = false;
        mDrawingThread.stopThread();
    }

    public void doDraw(Canvas surfaceViewCanvas)
    {
        try
        {
            if (mWorkingBitmapRect == null || surfaceViewCanvas == null || mCurrentLayer == null )
            {
                return;
            }

            drawCheckeredPatternOnCanvas(surfaceViewCanvas);

            if (mCurrentLayer != null)
            {
                redrawAllTheLayers(surfaceViewCanvas);
                PaintroidApplication.currentTool.trackFingerMotion(surfaceViewCanvas);

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
        ArrayList<Layer> layerList = LayersDialog.getInstance().getAdapter().getLayers();

        ListIterator<Layer> layerListIterator = layerList.listIterator(layerList.size());
        Layer layer;
        while (layerListIterator.hasPrevious())
        {
            layer = layerListIterator.previous();
            if(layer.getVisible())
            {
                Paint paint = new Paint();
                paint.setAlpha(layer.getScaledOpacity());
                canvas.drawBitmap(layer.getBitmap(), 0, 0, null);
            }
        }
    }

    public synchronized void setCurrentLayer(Layer layer)
    {
        mCurrentLayer = layer;

        if (mCurrentLayer != null)
        {
            //mWorkingBitmapCanvas.setBitmap(mCurrentLayer.getBitmap());
            mWorkingBitmapRect.set(0, 0, mCurrentLayer.getBitmap().getWidth(), mCurrentLayer.getBitmap().getHeight());
            mDrawingSurfaceListener.setCurrentLayer(layer);
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
            //PaintroidApplication.commandManager.commitCommand(new LayerCommandOld(LayerCommandOld.LayerAction.INSERT_IMAGE));
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

    @Override
    public void onSurfaceViewRedraw() {
        mSurfaceViewDrawTrigger.redraw();
    }

    @Override
    public void onActiveLayerChanged(Layer layer)
    {
        if(mCurrentLayer.getLayerID() != layer.getLayerID())
        {
            mCurrentLayer = layer;
        }
    }

    private static class DrawingThread extends Thread {

        private boolean mKeepRuning;
        private DrawingSurface mDrawingSurfaceInstance;

        public DrawingThread(DrawingSurface instance)
        {
            mKeepRuning = true;
            mDrawingSurfaceInstance = instance;
        }

        public void stopThread()
        {
            mKeepRuning = false;
            if (mDrawingSurfaceInstance != null && mDrawingSurfaceInstance.mSurfaceViewDrawTrigger != null) {
                mDrawingSurfaceInstance.mSurfaceViewDrawTrigger.redraw();
            }
        }

        @Override
        public void run()
        {
            while (mKeepRuning && mDrawingSurfaceInstance != null)
            {
                try
                {
                    mDrawingSurfaceInstance.mDrawLock.lock();

                    if (mKeepRuning)
                    {
                        while (!mDrawingSurfaceInstance.mDrawFlag)
                        {
                            mDrawingSurfaceInstance.mDrawCondition.await();
                        }

                        Canvas canvas = mDrawingSurfaceInstance.mHolder.lockCanvas(mDrawingSurfaceInstance.mHolder.getSurfaceFrame());

                        if (canvas != null)
                        {
                            synchronized (canvas)
                            {
                                mDrawingSurfaceInstance.doDraw(canvas);
                                mDrawingSurfaceInstance.mSurfaceViewDrawTrigger.hadRedraw();
                                mDrawingSurfaceInstance.mHolder.unlockCanvasAndPost(canvas);
                            }
                        }
                    }
                }
                catch (InterruptedException e)
                {
                    Log.d(this.getName(), e.getMessage());
                }
                finally
                {
                    mDrawingSurfaceInstance.mDrawLock.unlock();
                }
            }

            mDrawingSurfaceInstance = null;
        }
    }

    private class SurfaceViewDrawTrigger implements DrawSurfaceTrigger
    {
        public SurfaceViewDrawTrigger() {
        }

        @Override
        public void redraw()
        {
            mDrawLock.lock();
            mDrawFlag = true;
            mDrawCondition.signalAll();
            mDrawLock.unlock();
        }

        @Override
        public void hadRedraw()
        {
            mDrawLock.lock();
            mDrawFlag = false;
            mDrawLock.unlock();
        }
    }
}
