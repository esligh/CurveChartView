package com.widget.fr.curvechartview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class CurveChartView extends View {

    protected final float DENSITY = getResources().getDisplayMetrics().density;

    private static final int DEFAULT_CHART_COLOR = Color.parseColor("#FBC02D");
    private static final int DEFAULT_COORDINATE_LINE_COLOR = Color.parseColor("#80003737");
    private static final int DEFAULT_POINT_COLOR = Color.parseColor("#009688");
    private static final int DEFAULT_POINTER_RADIUS = 8;
    private static final int DEFAULT_STROKE_WIDTH = 2;
    private static final int DEFAULT_CURVE_COLOR = Color.parseColor("#AFB42B");
    private static final float DEFAULT_MAX_VALUE_X = 100.0f;
    private static final float DEFAULT_MAX_VALUE_Y = 100.0f;
    private static final float SMOOTHNESS = 0.3f;
    private static final int DEFAULT_HORIZONTAL_LINE_NUMBER = 10;

    private Paint mPaint;
    private Paint mAreaPaint;

    private Path mPath;

    private int mCurveColor = DEFAULT_CURVE_COLOR;
    private int mChartColor ;

    private  float mCircleSize;
    private  float mStrokeSize;
    private  float mBorder;
    private PointF[] mValues;

    private float mMaxY;
    private float mMaxX;
    private boolean hasCoordinate ;

    private int mCoordHLineNumber ;
    private int mCoordLineColor ;
    private int mPointColor;

    public CurveChartView(Context context) {
        this(context, null, 0);
        init(null,0);
    }

    public CurveChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(attrs,0);
    }

    public CurveChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs,defStyle);
    }


    private void init(AttributeSet attrs,int defStyle)
    {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CurveChartView, defStyle, 0);
        hasCoordinate = a.getBoolean(R.styleable.CurveChartView_show_coordinate, false);
        mChartColor = a.getColor(R.styleable.CurveChartView_chart_primary_color, DEFAULT_CHART_COLOR);
        mCoordLineColor =a.getColor(R.styleable.CurveChartView_coordinate_color, DEFAULT_COORDINATE_LINE_COLOR);
        mMaxY = a.getFloat(R.styleable.CurveChartView_max_y_value, DEFAULT_MAX_VALUE_X);
        mMaxX = a.getFloat(R.styleable.CurveChartView_max_x_value, DEFAULT_MAX_VALUE_Y);
        mCoordHLineNumber = a.getInteger(R.styleable.CurveChartView_coordinate_horizontal_line_number,DEFAULT_HORIZONTAL_LINE_NUMBER);
        mPointColor = a.getColor(R.styleable.CurveChartView_point_color,DEFAULT_POINT_COLOR);
        mCircleSize = a.getDimension(R.styleable.CurveChartView_point_radius,DEFAULT_POINTER_RADIUS*DENSITY);
        mStrokeSize = a.getDimension(R.styleable.CurveChartView_curve_stroke_width,DEFAULT_STROKE_WIDTH*DENSITY);
        a.recycle();

        mBorder = mCircleSize;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeSize);
        mPaint.setColor(mCurveColor);

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
        if(mAreaPaint == null) {
            LinearGradient shader = new LinearGradient(0, 0, 0, getHeight(),
                    mChartColor,
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP);
            mAreaPaint = new Paint();
            mAreaPaint.setShader(shader);
        }
        final float height = getMeasuredHeight() - 2*mBorder;
        final float width = getMeasuredWidth() - 2*mBorder;

        //draw the horizontal coordinate system

        if(hasCoordinate){
            mPaint.setStrokeWidth(1);
            mPaint.setColor(mCoordLineColor);

            float startX = mBorder;
            float startY = mBorder ;
            float h = height/mCoordHLineNumber;
            for(int i=0;i<mCoordHLineNumber;i++){
                canvas.drawLine(startX, startY, startX+width, startY, mPaint);
                startY+=h;
            }
            canvas.drawLine(startX, startY, startX+width, startY, mPaint);
        }

        if(mValues == null || mValues.length ==0){
            return;
        }

        int size = mValues.length;

        mPath.reset();

        List<PointF> points = new ArrayList<PointF>(size);
        for (PointF point : mValues) {
            float x = mBorder + point.x*width/mMaxX;
            float y = mBorder + height - (point.y)*height/mMaxY;
            points.add(new PointF(x,y));
        }

        // calculate smooth path
        float lX = 0, lY = 0;
        mPath.moveTo(points.get(0).x, points.get(0).y);
        for (int i=1; i<size; i++) {
            PointF p = points.get(i);	// current point

            // first control point
            PointF p0 = points.get(i-1);
            float d0 = (float) Math.sqrt(Math.pow(p.x - p0.x, 2)+Math.pow(p.y-p0.y, 2));
            float x1 = Math.min(p0.x + lX*d0, (p0.x + p.x)/2);
            float y1 = p0.y + lY*d0;

            // second control point
            PointF p1 = points.get(i+1 < size ? i+1 : i);
            float d1 = (float) Math.sqrt(Math.pow(p1.x - p0.x, 2)+Math.pow(p1.y-p0.y, 2));
            lX = (p1.x-p0.x)/d1*SMOOTHNESS;
            lY = (p1.y-p0.y)/d1*SMOOTHNESS;
            float x2 = Math.max(p.x - lX*d0, (p0.x + p.x)/2);
            float y2 = p.y - lY*d0;

            // add line
            mPath.cubicTo(x1,y1,x2, y2, p.x, p.y);
        }

        // draw path
        mPaint.setColor(mCurveColor);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(mPath, mPaint);

        // draw area
        if (size > 0) {
            mPath.lineTo(points.get(size-1).x, height+mBorder);
            mPath.lineTo(points.get(0).x, height+mBorder);
            mPath.close();
            canvas.drawPath(mPath, mAreaPaint);
        }

        // draw circles
        mPaint.setColor(mPointColor);
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
