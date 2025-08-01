package com.tapbi.spark.yokey.ui.custom.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class CustomPreviewMyTheme extends ImageView {
    private boolean isMyTheme = false;

    public CustomPreviewMyTheme(Context context) {
        super(context);
    }

    public CustomPreviewMyTheme(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPreviewMyTheme(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, (widthMeasureSpec * 444) / 720);
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
