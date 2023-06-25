package com.humanebicycle.spirometer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class GyroscopeView extends View {

    //height is the relative to the view

    private Paint paint;
    float screenWidth;
    float viewHeight=600;
    int radiusOuter = 280, radiusInner=25;
    int outerCircleStroke = 5;
    double innerCircleX,innerCircleY;

    public GyroscopeView(Context context) {
        super(context);
        init(null);
    }

    public GyroscopeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public GyroscopeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public GyroscopeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    void init(AttributeSet attr){
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(outerCircleStroke);
        paint.setStyle(Paint.Style.STROKE);
        screenWidth= getResources().getDisplayMetrics().widthPixels;
    }


    public void updateGyroscopeView(double x,double y,double degreeZ){
        innerCircleX=x*radiusOuter;
        innerCircleY=y*radiusOuter;
        if(degreeZ<32 && degreeZ>28){
            paint.setColor(Color.GREEN);
            Log.d("abh", "updateGyroscopeView: x"+innerCircleX+" y:"+innerCircleY);
        }else{
            paint.setColor(Color.RED);
        }
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        //outer circle
        canvas.drawCircle(screenWidth/2,viewHeight/2,radiusOuter,paint);



        //inner circle
        canvas.drawCircle((int)innerCircleX+screenWidth/2,(viewHeight-2*radiusOuter)/2+radiusOuter+(int)innerCircleY,radiusInner,paint);
        super.draw(canvas);
    }

}
