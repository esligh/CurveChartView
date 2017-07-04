package com.widget.fr.curvechartview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class CurveChartView extends View {

    private static final int DEFAULT_CHART_COLOR = Color.parseColor("#6050E3C2");
    private static final int DEFAULT_COORDINATE_LINE_COLOR = Color.parseColor("#003737");
    private static final float DEFAULT_MAX_VALUE_X = 100.0f;
    private static final float DEFAULT_MAX_VALUE_Y = 100.0f;

    private static final int CIRCLE_SIZE = 8;
    private static final int STROKE_SIZE = 2;
    private static final float SMOOTHNESS = 0.3f;

    private final Paint mPaint;
    private final Path mPath;
    private final float mCircleSize;
    private final float mStrokeSize;
    private final float mBorder;
    private int mChartColor ;
    private PointF[] mValues;
    private float mMaxY;
    private float mMaxX;
    private boolean hasCoordinate ;
    private Paint mCoordinatePaint;
    private int mCoordHLineNumber ;
    private int mCoordLineColor ;

    public CurveChartView(Context context) {
        this(context, null, 0);
    }

    public CurveChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CurveChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CurveChartView, defStyle, 0);
        hasCoordinate = a.getBoolean(R.styleable.CurveChartView_show_coordinate, false);
        mChartColor = a.getColor(R.styleable.CurveChartView_chart_area_color, DEFAULT_CHART_COLOR);
        mCoordLineColor =a.getColor(R.styleable.CurveChartView_coordinate_color, DEFAULT_COORDINATE_LINE_COLOR);
        mMaxY = a.getFloat(R.styleable.CurveChartView_max_y_value, DEFAULT_MAX_VALUE_X);
        mMaxX = a.getFloat(R.styleable.CurveChartView_max_x_value, DEFAULT_MAX_VALUE_Y);
        mCoordHLineNumber = a.getInteger(R.styleable.CurveChartView_coordinate_horizontal_line_number,10);
        a.recycle();
        float scale = context.getResources().getDisplayMetrics().density;

        mCircleSize = scale * CIRCLE_SIZE;
        mStrokeSize = scale * STROKE_SIZE;
        mBorder = mCircleSize;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeSize);

        mCoordinatePaint = new Paint();
        mCoordinatePaint.setAntiAlias(true);
        mCoordinatePaint.setStrokeWidth(1);
        mCoordinatePaint.setColor(mCoordLineColor);
        mPath = new Path();
    }

    public void setData(PointF[] values) {
        mValues = values;
        invalidate();
    }

    public void setMaxY(float max)
    {
        this.mMaxY = max;
    }

    public void setMaxX(float max)
    {
        this.mMaxX = max;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);


        final float height = getMeasuredHeight() - 2*mBorder;
        final float width = getMeasuredWidth() - 2*mBorder;
        final float left = 0;

        //draw the horizontal coordinate

        if(hasCoordinate){
            float startX = mBorder;
            float startY = mBorder ;
            float h = height/mCoordHLineNumber;
            for(int i=0;i<mCoordHLineNumber;i++){
                canvas.drawLine(startX, startY, startX+width, startY, mCoordinatePaint);
                startY+=h;
            }
            canvas.drawLine(startX, startY, startX+width, startY, mCoordinatePaint);
        }

        int size = mValues.length;

        mPath.reset();

        // calculate point coordinates
        List<PointF> points = new ArrayList<PointF>(size);
        for (PointF point : mValues) {
            float x = mBorder + (point.x-left)*width/mMaxX;
            float y = mBorder + height - (point.y)*height/mMaxY;
            points.add(new PointF(x,y));
        }

        // calculate smooth path
        float lX = 0, lY = 0;
        mPath.moveTo(points.get(0).x, points.get(0).y);
        for (int i=1; i<size; i++) {
            PointF p = points.get(i);	// current point

            // first control point
            PointF p0 = points.get(i-1);	// previous point
            float d0 = (float) Math.sqrt(Math.pow(p.x - p0.x, 2)+Math.pow(p.y-p0.y, 2));	// distance between p and p0
            float x1 = Math.min(p0.x + lX*d0, (p0.x + p.x)/2); 	// min is used to avoid going too much right
            float y1 = p0.y + lY*d0;

            // second control point
            PointF p1 = points.get(i+1 < size ? i+1 : i);	// next point
            float d1 = (float) Math.sqrt(Math.pow(p1.x - p0.x, 2)+Math.pow(p1.y-p0.y, 2));	// distance between p1 and p0 (length of reference line)
            lX = (p1.x-p0.x)/d1*SMOOTHNESS;		// (lX,lY) is the slope of the reference line
            lY = (p1.y-p0.y)/d1*SMOOTHNESS;
            float x2 = Math.max(p.x - lX*d0, (p0.x + p.x)/2);	// max is used to avoid going too much left
            float y2 = p.y - lY*d0;

            // add line
            mPath.cubicTo(x1,y1,x2, y2, p.x, p.y);
        }

        // draw path
        mPaint.setColor(mChartColor);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(mPath, mPaint);

        // draw area
        if (size > 0) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mChartColor);
            mPath.lineTo(points.get(size-1).x, height+mBorder);
            mPath.lineTo(points.get(0).x, height+mBorder);
            mPath.close();
            canvas.drawPath(mPath, mPaint);
        }

        // draw circles
        mPaint.setColor(mChartColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        for (PointF point : points) {
            canvas.drawCircle(point.x, point.y, mCircleSize/2, mPaint);
        }

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        for (PointF point : points) {
            canvas.drawCircle(point.x, point.y, (mCircleSize-mStrokeSize)/2, mPaint);
        }

    }
}
