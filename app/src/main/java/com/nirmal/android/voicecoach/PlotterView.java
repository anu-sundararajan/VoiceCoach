package com.nirmal.android.voicecoach;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AXS43 on 1/25/2016.
 * http://developer.android.com/training/custom-views/custom-drawing.html
 * Frequency of each note: http://www.phy.mtu.edu/~suits/notefreqs.html
 *
 */
public class PlotterView extends View {
    private static final String TAG = "Voice_Plotter";
    private static final int POINT_WIDTH = 3;
    private static final int AXIS_OFFSET = 0;

    private static final double C0_Hz = 16.35f;
    private static final double C_Exponent = 1;

    private Paint mFreqPaint;
    private Paint mGridPaint;
    private Paint mStairPaint;
    private Paint mBackgroundPaint;
    private boolean[] mRefLines = { true, false, false, false, false, false, false, false, false, false, false, false, false};
    private double mLoSaFreq;
    private double mMinPlotFreq;
    private double mMaxPlotFreq;
    private double mPlotFreqScale;
    private int mSeqFreqCount = 0;
    private int mSwarakalam = 0;
    private int mCurrentX = 0;
    private int mSequenceMode = 0;

    private List<Integer> mFrequencies = new ArrayList<>();
    public PlotterView(Context context) {
        this(context, null);
    }

    public PlotterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPitch(0);

        mLoSaFreq = C0_Hz;
        mFreqPaint = new Paint();
        mFreqPaint.setStrokeWidth(POINT_WIDTH);
        mFreqPaint.setColor(0xff960000);
        mStairPaint = new Paint();
        mStairPaint.setStrokeWidth(POINT_WIDTH);
        mStairPaint.setColor(0xffEFD090);
        mGridPaint = new Paint();
        mGridPaint.setStrokeWidth(2);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setPathEffect(new DashPathEffect(new float[]{15, 20, 15, 20}, 0));
        mGridPaint.setColor(0xffEFD090);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0x11EFD090); // 0xFFEFD090
    }

    private int getReferenceLineY(int h, int refLine) {
        return ((h / (Ragam.MAX_NOTES + 1)) * (Ragam.MAX_NOTES - refLine));
    }

    private void drawRefLine(Canvas canvas, int w, int h, int refLine) {
        int gridY = getReferenceLineY(h, refLine);
        Path mPath;
        mPath = new Path();
        mPath.moveTo(0, gridY);
        mPath.quadTo(w / 2, gridY, w, gridY);
        canvas.drawPath(mPath, mGridPaint);
    }

    private void drawRefLines(Canvas canvas, int w, int h) {
        for (int i = 0; i < Ragam.MAX_NOTES; i++) {
            if (mRefLines[i])
                drawRefLine(canvas, w, h, i);
        }
    }

    private void drawFreqLines(Canvas canvas, int w, int h) {
        int x = AXIS_OFFSET+POINT_WIDTH;

        int max_points_plotted = (w - x) / POINT_WIDTH;
        int size = mFrequencies.size();
        int start = 0;
        if (size > max_points_plotted) start = size - max_points_plotted;
        List<Integer> subList = mFrequencies.subList(start, size);

        int scale = h / ((int)mMaxPlotFreq - (int)mMinPlotFreq);

        /*
        List<Integer> fakedata = new ArrayList<>();
        for (int i = 40; i < 70; i++)
            fakedata.add(i);

        for (int i = 20; i < 70; i++)
            fakedata.add(1000);

        for (int i = 20; i < 70; i++)
            fakedata.add((i%15)+32);
        */
        int startX = AXIS_OFFSET;
        int startY = 0;
        int stopX = AXIS_OFFSET;
        int stopY = h - AXIS_OFFSET;
        for (Integer y : subList) {
        //for (Integer y : fakedata) {

            stopX = startX + POINT_WIDTH;

            // Drop out of range frequencies
            if ((y <= mMinPlotFreq) || (y >= mMaxPlotFreq)) {
                startX = stopX;
                startY = 0;
                continue;
            }

            y = y - (int)mMinPlotFreq;
            y = y * scale;
            //y1 = h - y;
            //canvas.drawPoint(x, y1, mFreqPaint);
            stopY = h - y;
            if (startY == 0) startY = stopY;
            canvas.drawLine(startX, startY, stopX, stopY, mFreqPaint);
            startX = stopX;
            startY = stopY;
        }

        mCurrentX = stopX; // Used in drawReferenceStair
    }

    private void drawSaPaSa(Canvas canvas, int w, int h) {

    }

    private void drawArohanam(Canvas canvas, int w, int h) {
        int steps = mSeqFreqCount / mSwarakalam;
        int startX = mCurrentX - (POINT_WIDTH * mSeqFreqCount);
        int startY = getReferenceLineY(h, 0); // Low Sa
        int stopX, stopY=0;

        for (int i = 0; i < Ragam.MAX_NOTES; i++) {
            if (mRefLines[i]) {
                stopX = startX + (POINT_WIDTH * mSwarakalam);
                stopY = startY;
                if (stopX > mCurrentX) {
                    stopX = mCurrentX;
                    canvas.drawLine(startX, startY, stopX, stopY, mStairPaint);
                    break;
                }
                canvas.drawLine(startX, startY, stopX, stopY, mStairPaint); // draw horizontal line
                startX = stopX;
                startY = stopY;
                // stopX does not change
                stopY = getReferenceLineY(h, i+1);
                canvas.drawLine(startX, startY, stopX, stopY, mStairPaint); // draw vertical line
            } else {
                stopX = startX;
                stopY = getReferenceLineY(h, i+1);
                canvas.drawLine(startX, startY, stopX, stopY, mStairPaint); // draw vertical line
            }
            startX = stopX;
            startY = stopY;
        }
        if (stopY <= 5) mSeqFreqCount = 0;
    }

    private void drawAvarohanam(Canvas canvas, int w, int h) {

    }

    private void drawReferenceStair(Canvas canvas, int w, int h) {
        if (mSwarakalam == 0) return;

        switch (mSequenceMode) {
            case 0: break;
            case 1: drawSaPaSa(canvas, w, h); break;
            case 2: drawArohanam(canvas, w, h); break;
            case 3: drawAvarohanam(canvas, w, h); break;
            default:break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(mBackgroundPaint);
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        drawRefLines(canvas, w, h);
        drawFreqLines(canvas, w, h);
        drawReferenceStair(canvas, w, h);
    }

    public void plotFrequency(int f) {
        mFrequencies.add(f);
        mSeqFreqCount++;
        invalidate();
    }

    private void printRefLineCoods() {
        for (int i = 0; i < Ragam.MAX_NOTES; i++) {
            if (mRefLines[i])
                Log.i(TAG, "ON");
            else Log.i(TAG, "OFF");
        }
    }

    public void drawAllReferenceLines(boolean[] refLines) {
        for(int i= 0; i < Ragam.MAX_NOTES; i++) {
            mRefLines[i] = refLines[i];
        }
        invalidate();
    }

    public void drawReferenceLine(int index) {
        for(int i= 0; i < Ragam.MAX_NOTES; i++) {
            mRefLines[i] = false;
        }
        mRefLines[index] = true;
        invalidate();
    }

    public void setPitch(int pitch) {
        double exponent = C_Exponent + pitch/12;
        double multiplier = Math.pow(2, exponent);
        mLoSaFreq = C0_Hz * multiplier;

        exponent = C_Exponent + pitch/12 - 1/12;
        multiplier = Math.pow(2, exponent);
        mMinPlotFreq = C0_Hz * multiplier;

        exponent = C_Exponent + pitch/12 + 1 + 1/12;
        multiplier = Math.pow(2, exponent);
        mMaxPlotFreq = C0_Hz * multiplier;

    }

    public void startReferenceStair(int mode, int swarakalam) {
        mSequenceMode = mode;
        mSeqFreqCount = 0;
        mSwarakalam = swarakalam;
    }

    public void stopReferenceStair() {
        mSwarakalam = 0;
    }
}
