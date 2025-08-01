package com.tapbi.spark.yokey.ui.custom.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class CustomPreviewTheme extends ImageView {
    private boolean isMyTheme = false;

    public CustomPreviewTheme(Context context) {
        super(context);
    }

    public CustomPreviewTheme(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPreviewTheme(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isMyTheme) {
            setMeasuredDimension(widthMeasureSpec, (widthMeasureSpec * 72) / 108);
        } else {
            setMeasuredDimension(widthMeasureSpec, (widthMeasureSpec * 1040) / 1440);
        }
    }

    public void changeIsMyTheme(boolean isMyTheme){
        this.isMyTheme = isMyTheme;
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }
}
