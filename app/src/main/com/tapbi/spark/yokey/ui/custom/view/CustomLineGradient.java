package com.tapbi.spark.yokey.ui.custom.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.util.DisplayUtils;

public class CustomLineGradient extends View {

    private Paint paint;
    private int[] colors = {
            App.getInstance().getResources().getColor(R.color.color_4355FF),
            App.getInstance().getResources().getColor(R.color.color_933DFE),
            App.getInstance().getResources().getColor(R.color.color_FF35FD),
            App.getInstance().getResources().getColor(R.color.color_FF8E61),
            App.getInstance().getResources().getColor(R.color.color_FFE600)
    };

    private Shader  shader;

    public CustomLineGradient(Context context) {
        super(context);
        init();
    }

    public CustomLineGradient(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomLineGradient(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setDither(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(DisplayUtils.dp2px(1));
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        shader = new LinearGradient(0, 0, getWidth(), getHeight(),
                colors, null, Shader.TileMode.CLAMP);
        paint.setShader(shader);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0,getHeight()/2f,getWidth(),getHeight()/2f,paint);
    }
}
