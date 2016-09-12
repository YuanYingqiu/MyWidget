package com.example.qq.myweatherviewdemo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.qq.myweatherviewdemo.R;
import com.orhanobut.logger.Logger;

/**
 * author YuanYingqiu
 * email 1049852196@qq.com
 * date 16-9-11
 */
public class MyLineChartView extends View {
    public static final int DEFAULT_COLOR = 0xff009688;

    /**
     * 将屏幕宽 分成9份 每份占的宽度
     */
    private float mOnePart;

    /**
     * 整个折线图包括文字与view的上下边距
     */
    private float mTopAndBottomSpace;

    /**
     * 文字与点之间的距离
     */
    private float mTextAndDotSpace;


    /**
     * 点的半径
     */
    private float mDotRadius;


    private float[] mXAxis;
    private float[] mYAxis;

    private int[] mData;



    private Paint mLinePaint;
    private Paint mDotPaint;
    private Paint mTextPaint;

    private float mTextSize;


    public MyLineChartView(Context context) {
        this(context,null);
    }

    public MyLineChartView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MyLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyLineChartView);
        int colorLine = ta.getColor(R.styleable.MyLineChartView_color_line,DEFAULT_COLOR);
        int colorDot = ta.getColor(R.styleable.MyLineChartView_color_dot,DEFAULT_COLOR);
        int colorText = ta.getColor(R.styleable.MyLineChartView_color_text,DEFAULT_COLOR);

        mTextSize = ta.getDimension(R.styleable.MyLineChartView_size_text,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12,getResources().getDisplayMetrics()));

        ta.recycle();

        //线的宽度
        float strokeWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,1.5f,getResources().getDisplayMetrics());
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setDither(true);
        mLinePaint.setColor(colorLine);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(strokeWidth);


        mDotPaint = new Paint();
        mDotPaint.setAntiAlias(true);
        mDotPaint.setDither(true);
        mDotPaint.setColor(colorDot);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(colorText);


        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;


        //将屏幕分成9份
        mOnePart = 1.0f*screenWidth/9;

        mTopAndBottomSpace = mTextSize *1.5f;
        mDotRadius = strokeWidth*2.0f;
        mTextAndDotSpace = mDotRadius*2.0f;

    }
    public void setData(int[] data){
        this.mData = data;
        computeWidthOfThisView();
    }

    /**
     * 根据数据计算出view所需要的宽度
     *
     * |------------------------------------|
     * |                                    |
     * |1.5 |--------------------------|1.5 |
     * |------------------------------------|
     */
    private void computeWidthOfThisView() {



        float viewLength = mData.length*mOnePart+mOnePart*2;
        Logger.e("viewLength>>>>>>>>>>>"+viewLength);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = (int) viewLength;
        //但View的大小改变时会调用onSizeChanged
        setLayoutParams(params);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setXYAxis();
    }

    /**
     * 设置 x轴
     */
    private void setXYAxis() {
        if(mData == null || mData.length == 0)
            return;

        Logger.e("setXYAxis");
        setXAxis();
        setYAxis();

        invalidate();

    }

    /**
     * 设置Y轴
     */
    private void setYAxis() {

        float minData = 0.0f;
        float maxData = minData;

        for (float data : mData){
            if(data < minData)
                minData = data;
            if(data > maxData)
                maxData = data;
        }

        float range = Math.abs(maxData - minData);


        float lineToTopAndBottomPadding = mTopAndBottomSpace+mTextSize+mTextAndDotSpace+mDotRadius;

        int viewHeight = getMeasuredHeight();
        Logger.e("viewHeight>>>>>>>>>>>>"+viewHeight);

        //折线 在view中可以占的最大高度
        float maxYAxisHeight = viewHeight - lineToTopAndBottomPadding*2;

        int dataLength = mData.length;
        mYAxis = new float[dataLength];

        //数据全部等于0 或者 相等
        if(range == 0){
            for (int i = 0 ; i < dataLength ; i++ )
                mYAxis[i] = maxYAxisHeight/2 + lineToTopAndBottomPadding;
        }else {
            //防止数据大于viewHeight 导致y的坐标点在view的外面
            float partHeight = maxYAxisHeight/range;
            for (int i = 0; i < dataLength; i++) {
                mYAxis[i] = viewHeight - lineToTopAndBottomPadding-(partHeight*(mData[i]-minData));
            }
        }

    }

    private void setXAxis() {
        //1.5倍mOnePart的距离
        float mSidePaddingScale = 1.5f;

        int dataLength = mData.length;
        mXAxis = new float[dataLength];
        for (int i = 0 ; i < dataLength ; i++){
            mXAxis[i] = (mSidePaddingScale+i)*mOnePart;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawChart(canvas);
    }

    private void drawChart(Canvas canvas) {
        if(mData == null || mData.length == 0)
            return;


        int dataLength = mData.length;
        for (int i = 0 ; i < dataLength ; i++){

            int data = mData[i];
            float startX = mXAxis[i];
            float startY = mYAxis[i];

            drawDot(canvas,startX,startY);
            drawText(canvas,startX,startY,data);

            if(i<dataLength-1){
                float endX = mXAxis[i+1];
                float endY = mYAxis[i+1];
                drawLine(canvas,startX,startY,endX,endY);
            }
        }
    }

    private void drawLine(Canvas canvas, float startX, float startY, float endX, float endY) {
        Path path = new Path();
        path.moveTo(startX,startY);
        path.lineTo(endX,endY);
        canvas.drawPath(path,mLinePaint);
    }

    private void drawText(Canvas canvas, float startX, float startY, int data) {
        canvas.drawText(data+"°",startX-mTextAndDotSpace,startY-mTextAndDotSpace,mTextPaint);
    }

    private void drawDot(Canvas canvas, float startX, float startY) {
        canvas.drawCircle(startX,startY,mDotRadius,mDotPaint);
    }


}
