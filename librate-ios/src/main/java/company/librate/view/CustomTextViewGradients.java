package company.librate.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;


public class CustomTextViewGradients extends AppCompatTextView {

    private int[] colors = {
            Color.parseColor("#4355FF"),
            Color.parseColor("#933DFE"),
            Color.parseColor("#FF35FD"),
            Color.parseColor("#FF8E61"),
            Color.parseColor("#FFE600")
    };

    public CustomTextViewGradients(Context context) {
        super(context);
    }

    public CustomTextViewGradients(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextViewGradients(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void isTextGradient(boolean isGradient){
        if(isGradient){
            if(getText()!=null) {
                String s = getText().toString();
                Rect bounds = new Rect();
                Paint textPaint = getPaint();
                textPaint.getTextBounds(s, 0, s.length(), bounds);
                int width = bounds.width();
                Shader textShader = new LinearGradient(0, 0, width, getTextSize(),
                        colors, null, Shader.TileMode.CLAMP);
                getPaint().setShader(textShader);
            }
        }else{
            getPaint().setShader(null);
        }

        invalidate();

    }
}
