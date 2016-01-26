package com.nirmal.android.voicecoach;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AXS43 on 1/25/2016.
 * http://developer.android.com/training/custom-views/custom-drawing.html
 *
 */
public class PlotterView extends View {
    private static final String TAG = "PlotterView";
    private static final int POINT_WIDTH = 5;
    private static final int AXIS_OFFSET = 20;
    private Paint mFreqPaint;
    private Paint mAxisPaint;
    private Paint mBackgroundPaint;

    private List<Integer> mFrequencies = new ArrayList<>();
    public PlotterView(Context context) {
        this(context, null);
    }

    public PlotterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mFreqPaint = new Paint();
        mFreqPaint.setStrokeWidth(POINT_WIDTH);
        mFreqPaint.setColor(Color.CYAN);
        mAxisPaint = new Paint();
        mAxisPaint.setStrokeWidth(5);
        mAxisPaint.setColor(Color.LTGRAY);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.DKGRAY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(mBackgroundPaint);
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        canvas.drawLine(AXIS_OFFSET, (h-AXIS_OFFSET), AXIS_OFFSET, AXIS_OFFSET, mAxisPaint);
        canvas.drawLine(AXIS_OFFSET, (h-AXIS_OFFSET), (w-AXIS_OFFSET), (h-AXIS_OFFSET), mAxisPaint);

        int x = AXIS_OFFSET+POINT_WIDTH;
        int max_points_plotted = (w - x) / POINT_WIDTH;
        int size = mFrequencies.size();
        int start = 0;
        if (size > max_points_plotted) start = size - max_points_plotted;
        List<Integer> subList = mFrequencies.subList(start, size);

        for (Integer y : subList) {
            x+=POINT_WIDTH;
            //if (y > h) y = h;
            y = y % h;
//            if (x > w) break;
            int y1 = h - y;
            canvas.drawPoint(x, y1, mFreqPaint);
        }
    }

    public void plotFrequency(int f) {
        mFrequencies.add(f);
        invalidate();
    }

}
