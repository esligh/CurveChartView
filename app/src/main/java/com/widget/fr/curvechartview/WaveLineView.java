package com.widget.fr.curvechartview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WaveLineView extends SurfaceView implements SurfaceHolder.Callback{

	private static final float REGION_LENGTH = 5f;
	private static final int SLEEP_TIME =200;
	private static int SAMPLESIZE = 100;
	private static final float MAX_AMPLITUDE = 220f;
	
	private SurfaceHolder mHolder;
	private Paint mPaint;
	private final Object mSurfaceLock = new Object();
	private DrawThread mThread;
	private Path mPathLine1;
	private Path mPathLine2;	
	private Path mPathCenter;
	private Path mPathLine11;
	private Path mPathLine22;
	private float mPhase;
	private float mPhaseDis = 0.6f;
	private float mAmplitude = MAX_AMPLITUDE;
	private float mSpeed = 0.15f;
	
	private Drawable mBgDrawable;
	private volatile boolean bAnimate = false;

	private int[] mLineColors = new int[]{Color.parseColor("#e0f79646"),
			Color.parseColor("#e08BC34A"),
			Color.parseColor("#e07C4DFF")};
	
	private int[] mLineColors2 = new int[]{Color.parseColor("#40f79646"),
			Color.parseColor("#408BC34A")};
	
	public WaveLineView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}
	
	public WaveLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}
	
	public WaveLineView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	private void init()
	{
		mHolder = getHolder();
		mHolder.addCallback(this);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(1);
		mPaint.setAntiAlias(true);
		mPathLine1 = new Path();
		mPathLine2 = new Path();
		mPathCenter = new Path();
		mPathLine11 = new Path();
		mPathLine22 = new Path();
		mBgDrawable =  getResources().getDrawable(R.drawable.weather_bg);
	}
	
	public void setAmplitude(float amplitude)
	{
		if(amplitude > MAX_AMPLITUDE){
			amplitude = MAX_AMPLITUDE;
		}else{
			this.mAmplitude = amplitude;
		}
	}
	
	public float getAmplitude()
	{
		return mAmplitude;
	}
	
	public float getMaxAmplitude()
	{
		return MAX_AMPLITUDE;
	}
	
	public void startAnimation()
	{	
		//setVisibility(View.VISIBLE);
		if(!bAnimate){
			bAnimate = true;
			mThread = new DrawThread(mHolder);			  
		    mThread.setRun(true);
		    mThread.start();	 
		}
	}
	
	public void stopAnimation()
	{		
		if(mThread != null){  
		    mThread.setRun(false);
			mThread.interrupt();						
			mThread = null;
			bAnimate = false;						
		}
		Canvas canvas = mHolder.lockCanvas();
		if(canvas == null){
			return ; 
		}
        mBgDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    	mBgDrawable.draw(canvas);
        mHolder.unlockCanvasAndPost(canvas);
	}
	
	public boolean isAnimationStart()
	{
		return bAnimate;
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
        if(mBgDrawable != null){
        	Canvas canvas = mHolder.lockCanvas();
	        mBgDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    	mBgDrawable.draw(canvas);
	        mHolder.unlockCanvasAndPost(canvas);
        }
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	class DrawThread extends Thread {
	    private SurfaceHolder mHolder;
	    private volatile boolean mIsRun = false;

	    public DrawThread(SurfaceHolder holder) {
	        mHolder = holder;
	    }

	    @Override
	    public void run() {
	        while(mIsRun) {
	            synchronized (mSurfaceLock) {
	                Canvas canvas = mHolder.lockCanvas();
	                if (canvas != null) {
	                    doDraw(canvas);  
	                    mPhase = (float) ((mPhase + Math.PI * mSpeed) % (2 * Math.PI));
	                    mHolder.unlockCanvasAndPost(canvas);
	                }
	            }
	            try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }

	    public void setRun(boolean isRun) {
	        this.mIsRun = isRun;
	    }
	    /**
	     * 
	     * */
	    private void doDraw(Canvas canvas){	    	
	    	int width = canvas.getWidth();
	    	int height = canvas.getHeight();	    	
    		mBgDrawable.setBounds(0, 0, width, height);
	    	mBgDrawable.draw(canvas);
	    	if(bAnimate){
		    	canvas.translate(width/2, height/2);
		    	mPathLine1.reset();
		    	mPathLine1.moveTo(-width/2, 0);
	
		    	mPathLine2.reset();
		    	mPathLine2.moveTo(-width/2, 0);
		    	
		    	mPathCenter.reset();
		    	mPathCenter.moveTo(-width/2, 0);
		    	
		    	mPathLine11.reset();
		    	mPathLine11.moveTo(-width/2, 0);
	
		    	mPathLine22.reset();
		    	mPathLine22.moveTo(-width/2, 0);
		    	
		    	for(int i=0;i<SAMPLESIZE;i++){
		    		float x = i * (REGION_LENGTH/SAMPLESIZE) - REGION_LENGTH/2;
		    		float y = expression(x,mPhase);
		    		mPathLine1.lineTo(x*width/REGION_LENGTH, y*mAmplitude);
		    		mPathLine2.lineTo(x*width/REGION_LENGTH, -y*mAmplitude);
		    		mPathCenter.lineTo(x*width/REGION_LENGTH, y*mAmplitude/5);
		    		
		    		float y2 = expression(x,mPhase-mPhaseDis);
		    		mPathLine11.lineTo(x*width/REGION_LENGTH, y2*mAmplitude);
		    		mPathLine22.lineTo(x*width/REGION_LENGTH, -y2*mAmplitude);
		    	}
		    	
		    	mPathLine1.lineTo(width/2,0);
		    	mPathLine2.lineTo(width/2,0);
		    	mPathCenter.lineTo(width/2,0);
		    	mPathLine11.lineTo(width/2,0);
		    	mPathLine22.lineTo(width/2,0);
		    	
		    	mPaint.setColor(mLineColors2[0]);
		    	canvas.drawPath(mPathLine11, mPaint);
		    	
		    	mPaint.setColor(mLineColors2[1]);
		    	canvas.drawPath(mPathLine22, mPaint);
		    	
		    	mPaint.setColor(mLineColors[0]);
		    	canvas.drawPath(mPathLine1, mPaint);
		    	
		    	mPaint.setColor(mLineColors[1]);
		    	canvas.drawPath(mPathLine2, mPaint);
		    	
		    	mPaint.setColor(mLineColors[2]);
		    	canvas.drawPath(mPathCenter, mPaint);
	    	}
	    }
	}
		
	private float expression(float x,float offset)
	{
		return (float)(Math.sin((0.75f*Math.PI*x-offset))*0.5*Math.pow(4/(4+Math.pow(x, 4)), 2.5));
	}

}
