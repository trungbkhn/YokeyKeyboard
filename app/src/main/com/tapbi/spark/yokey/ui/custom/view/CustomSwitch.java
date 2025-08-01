package com.tapbi.spark.yokey.ui.custom.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.App;

public class CustomSwitch extends View {

    private boolean isCheck = false;
    private Paint paint;
    private Path path;
    private float[] corners;
    private int padding = 0;

    private boolean enable = true;
    private OnChangeCheckListener onChangeCheckListener;
    private int[] colors = {
            App.getInstance().getResources().getColor(R.color.color_4355FF),
            App.getInstance().getResources().getColor(R.color.color_933DFE),
            App.getInstance().getResources().getColor(R.color.color_FF35FD),
            App.getInstance().getResources().getColor(R.color.color_FF8E61),
            App.getInstance().getResources().getColor(R.color.color_FFE600)
    };
    private LinearGradient linearGradient;

    public CustomSwitch(Context context) {
        super(context);
        init();
    }

    public CustomSwitch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomSwitch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        linearGradient = new LinearGradient(0,0,getWidth(),0,colors,null, Shader.TileMode.CLAMP);
        int rc = getHeight() / 2;
        padding = getHeight() / 10;
        corners = new float[]{
                rc, rc,
                rc, rc,
                rc, rc,
                rc, rc
        };
        path = new Path();
        path.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), corners, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (isCheck) {
            paint.setShader(linearGradient);
            if (enable) {
                paint.setColor(Color.parseColor("#34C759"));
            }else {
                paint.setColor(Color.parseColor("#E9E9EA"));
            }
        } else {
            paint.setShader(null);
            paint.setColor(Color.parseColor("#E9E9EA"));
        }
        canvas.drawPath(path, paint);
        paint.setShader(null);
        if (isCheck) {
            paint.setColor(Color.parseColor("#ffffff"));
            canvas.drawLine(getHeight() / 2,getHeight()/2-13,getHeight() / 2, getHeight()/2+13,paint );

        } else {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#B2B2B2"));
            canvas.drawCircle(getWidth() - getHeight() / 2, getHeight() / 2, getHeight() / 4 - padding, paint);
        }


        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.WHITE);
        if (isCheck) {
            canvas.drawCircle(getWidth() - getHeight() / 2, getHeight() / 2, getHeight() / 2 - padding, paint);
        } else {
            canvas.drawCircle(getHeight() / 2, getHeight() / 2, getHeight() / 2 - padding, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            isCheck = !isCheck;
           // Toast.makeText(getContext(),"hello1 "+isCheck,Toast.LENGTH_SHORT).show();
            if (onChangeCheckListener != null) onChangeCheckListener.isCheck(this,isCheck);
            invalidate();
        }

        return enable;
    }

    public void changeEnable(boolean enable){
        this.enable = enable;
    }

    public void setCheck(boolean isCheck) {
        this.isCheck = isCheck;
        invalidate();
    }

    public boolean getCheck(){
        return isCheck;
    }

    public void setOnChangeCheckListener(OnChangeCheckListener onChangeCheckListener) {
        this.onChangeCheckListener = onChangeCheckListener;
    }

    public interface OnChangeCheckListener {
        void isCheck(CustomSwitch customSwitch,boolean isCheck);
    }


}
