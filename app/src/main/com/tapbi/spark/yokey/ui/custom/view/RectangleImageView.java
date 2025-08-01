package com.tapbi.spark.yokey.ui.custom.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.tapbi.spark.yokey.util.DisplayUtils;

@SuppressLint("AppCompatCustomView")
public class RectangleImageView extends ImageView {
    private int SIZE_78 = DisplayUtils.dp2px(78f);
    private int SIZE_45 = DisplayUtils.dp2px(45f);
    public RectangleImageView(Context context) {
        super(context);
    }

    public RectangleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RectangleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,(widthMeasureSpec*400)/600);
       // setMeasuredDimension(widthMeasureSpec,(widthMeasureSpec*SIZE_45)/SIZE_78);
    }
}
