package com.tapbi.spark.yokey.ui.custom.view;

import static com.tapbi.spark.yokey.util.Constant.FONT_COUNTRY_CODE;
import static com.tapbi.spark.yokey.util.Constant.FONT_IN_LOVE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tapbi.spark.yokey.BuildConfig;
import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.App;

@SuppressLint("AppCompatCustomView")
public class CustomTextViewGradient extends TextView {

    private int[] colors = {
            App.getInstance().getResources().getColor(R.color.color_4355FF),
            App.getInstance().getResources().getColor(R.color.color_933DFE),
            App.getInstance().getResources().getColor(R.color.color_FF35FD),
            App.getInstance().getResources().getColor(R.color.color_FF8E61),
            App.getInstance().getResources().getColor(R.color.color_FFE600)
    };

    public CustomTextViewGradient(Context context) {
        super(context);
    }

    public CustomTextViewGradient(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextViewGradient(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void isTextGradient(boolean isGradient) {
        if (isGradient) {
            if (getText() != null) {
                boolean checkDrawGradient = false;
                if (getContentDescription() != null) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1 && (getContentDescription().equals(FONT_COUNTRY_CODE) || getContentDescription().equals(FONT_IN_LOVE))) {
                        checkDrawGradient = true;
                    }
                    if(getContentDescription().equals(FONT_IN_LOVE)){
                        setTextColor(Color.BLUE);
                    }
                }
                if (!checkDrawGradient) {
                    String s = getText().toString().trim();
                    Rect bounds = new Rect();
                    Paint textPaint = getPaint();
                    textPaint.getTextBounds(s, 0, s.length(), bounds);
                    int width = bounds.width();
//                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
//                    width = getWidth();
//                }
                    Shader textShader = new LinearGradient(0, 0, width, getTextSize(),
                            colors, null, Shader.TileMode.CLAMP);
                    getPaint().setShader(textShader);
                }
            }
        } else {
            //setTextColor(Color.BLACK);
            getPaint().setShader(null);
        }
        invalidate();

    }
}
