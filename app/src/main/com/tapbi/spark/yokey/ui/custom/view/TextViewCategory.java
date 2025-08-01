package com.tapbi.spark.yokey.ui.custom.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class TextViewCategory extends TextView {
    public TextViewCategory(Context context) {
        super(context);
        init();
    }

    public TextViewCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextViewCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.parseColor("#90424242"));
        paint.setStyle(Paint.Style.FILL);
        invalidate();
    }

    private Paint paint;
    private boolean isEnabled;

    @Override
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (paint != null && isEnabled) {
            canvas.drawRoundRect(0, 0, getWidth(), getHeight(), getHeight() / 2f, getHeight() / 2f, paint);
        }
        super.onDraw(canvas);
    }
}
