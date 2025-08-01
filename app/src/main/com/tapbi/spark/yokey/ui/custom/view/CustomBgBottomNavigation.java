package com.tapbi.spark.yokey.ui.custom.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.tapbi.spark.yokey.util.DisplayUtils;

public class CustomBgBottomNavigation extends View {

    private Path mPath;
    private Path mPathShadow;
    private Paint mPaint;
    private Paint mPaintShadow;
    private final int SIZE_24 = DisplayUtils.dp2px(24);
    private final int SIZE_5 = DisplayUtils.dp2px(5);
    private final int SIZE_4 = DisplayUtils.dp2px(4);
    private final int SIZE_3 = DisplayUtils.dp2px(3);
    private final int SIZE_2 = DisplayUtils.dp2px(2);
    private final int SIZE_1 = DisplayUtils.dp2px(1);
    private final int SIZE_10 = DisplayUtils.dp2px(10);

    private final int SIZE_45 = DisplayUtils.dp2px(45);
    private final int SIZE_25 = DisplayUtils.dp2px(25);

    public CustomBgBottomNavigation(Context context) {
        super(context);
        init();
    }

    public CustomBgBottomNavigation(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomBgBottomNavigation(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPath = new Path();
        mPathShadow = new Path();
        mPaint = new Paint();
        mPaintShadow = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaintShadow.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaintShadow.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaintShadow.setColor(Color.parseColor("#E5E5E5"));
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
//        initPathShadow();
        initPath();
    }

    public void initPathShadow() {
        float d = getWidth() / 20f;
        //  mPaintShadow.setColor(Color.RED);
        mPathShadow.moveTo(0, SIZE_24);
        mPathShadow.cubicTo(0, SIZE_24, 0, SIZE_4, SIZE_24, SIZE_4);
        mPathShadow.lineTo(getWidth() / 5f * 2 - d, SIZE_4);

        mPathShadow.cubicTo(getWidth() / 5f * 2 - d / 2, SIZE_4,
                getWidth() / 5f * 2, SIZE_4, getWidth() / 5f * 2 + d, SIZE_25);

        mPathShadow.cubicTo(getWidth() / 5f * 2 + d, SIZE_25 - 3,
                getWidth() / 2f, SIZE_45, getWidth() / 2f + d, SIZE_25 - 3);

        mPathShadow.cubicTo(getWidth() / 2f + d, SIZE_25 - 3, getWidth() / 5f * 3, SIZE_4 - 2,
                getWidth() / 5f * 3 + d / 2, SIZE_4 - 2);

        mPathShadow.lineTo(getWidth() - SIZE_24, SIZE_4 - 2);
        mPathShadow.cubicTo(getWidth() - SIZE_24, SIZE_4 - 2, getWidth(), SIZE_4 - 2,
                getWidth()
                , SIZE_24 - 4);
        mPathShadow.lineTo(getWidth(), getHeight());
        mPathShadow.lineTo(0, getHeight());
        mPathShadow.lineTo(0, SIZE_24);
        mPathShadow.close();
        int[] colors = new int[]{Color.TRANSPARENT, Color.BLACK};
        mPaintShadow.setShader(new LinearGradient(0f, 0f, 0f, DisplayUtils.dp2px(72), colors, null, Shader.TileMode.CLAMP));
        mPaintShadow.setShadowLayer(SIZE_4, 0, 0, Color.parseColor("#4D28293D"));

    }

    public void initPath() {
      //  float d = getWidth() / 20f;
        mPath = createPath(SIZE_4);
   //     mPathShadow = createPath(0);

     /*   mPath.moveTo(0, SIZE_24);
        mPath.cubicTo(0, SIZE_24, 0, SIZE_4, SIZE_24, SIZE_4);
        mPath.lineTo(getWidth() / 5f * 2 - d, SIZE_4);
        mPath.cubicTo(getWidth() / 5f * 2 - d / 2, SIZE_4, getWidth() / 5f * 2, SIZE_4, getWidth() / 5f * 2 + d, SIZE_25);
        mPath.cubicTo(getWidth() / 5f * 2 + d, SIZE_25, getWidth() / 2f, SIZE_45, getWidth() / 2f + d, SIZE_25);
        mPath.cubicTo(getWidth() / 2f + d, SIZE_25, getWidth() / 5f * 3, SIZE_4, getWidth() / 5f * 3 + d / 2, SIZE_4);
        mPath.lineTo(getWidth() - SIZE_24, SIZE_4);
        mPath.cubicTo(getWidth() - SIZE_24, SIZE_4, getWidth(), SIZE_4, getWidth(), SIZE_24);
        mPath.lineTo(getWidth(), getHeight());
        mPath.lineTo(0, getHeight());
        mPath.lineTo(0, SIZE_24);
        mPath.close();*/
//        int[] colors = new int[]{Color.TRANSPARENT,Color.BLACK};
//        float[] positions = new float[]{0.5F,1};
//        mPaint.setShader(new LinearGradient(0f,0f,0f,DisplayUtils.dp2px(72),colors,null,Shader.TileMode.CLAMP));

        mPaint.setShadowLayer(SIZE_4, 0, 0, Color.parseColor("#4D28293D"));
        setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
        // mPaint.setShadowLayer(SIZE_4,0,0, Color.YELLOW);
    }

    public Path createPath(int SIZE_4) {
        Path mPath = new Path();
        float d = getWidth() / 20f;
        mPath.moveTo(0, SIZE_24);
        mPath.cubicTo(0, SIZE_24, 0, SIZE_4, SIZE_24, SIZE_4);
        mPath.lineTo(getWidth() / 5f * 2 - d, SIZE_4);
        mPath.cubicTo(getWidth() / 5f * 2 - d / 2, SIZE_5, getWidth() / 5f * 2, SIZE_5, getWidth() / 5f * 2 + d, SIZE_25);
        mPath.cubicTo(getWidth() / 5f * 2 + d, SIZE_25, getWidth() / 2f, SIZE_45, getWidth() / 2f + d, SIZE_25);
        mPath.cubicTo(getWidth() / 2f + d + SIZE_1, SIZE_25, getWidth() / 5f * 3, SIZE_4,
                getWidth() / 5f * 3 + d / 2 + SIZE_4
                , SIZE_4);
        mPath.lineTo(getWidth() - SIZE_24 , SIZE_4);
        mPath.cubicTo(getWidth() - SIZE_24, SIZE_4, getWidth(), SIZE_4, getWidth(), SIZE_24);
        mPath.lineTo(getWidth(), getHeight());
        mPath.lineTo(0, getHeight());
        mPath.lineTo(0, SIZE_24);
        mPath.close();
        return mPath;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, DisplayUtils.dp2px(72));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //  canvas.drawPath(mPathShadow, mPaintShadow);
        canvas.save();
      //  int[] colors = new int[]{Color.TRANSPARENT, Color.BLACK};
      //  float[] positions = new float[]{0.2F, 1f};
      //  mPaint.setShader(new LinearGradient(0f, 0f, 0f, DisplayUtils.dp2px(72), colors, positions, Shader.TileMode.CLAMP));
      //  canvas.drawPath(mPathShadow, mPaint);
     //   mPaint.setShader(null);
      //  canvas.restore();
        canvas.drawPath(mPath, mPaint);
    }
}
