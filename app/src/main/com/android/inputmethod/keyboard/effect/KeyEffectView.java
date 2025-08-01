package com.android.inputmethod.keyboard.effect;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class KeyEffectView extends androidx.appcompat.widget.AppCompatImageView {

    private int widthMeasureSpec = 300;
    private int heightMeasureSpec = 300;

    public KeyEffectView(Context context) {
        super(context);
    }

    public KeyEffectView(Context context, @Nullable AttributeSet attrs, double widthMeasureSpec, double heightMeasureSpec, Drawable drawableEffect) {
        super(context, attrs);
        this.widthMeasureSpec = (int) widthMeasureSpec;
        this.heightMeasureSpec = (int) heightMeasureSpec;
        init(drawableEffect);
    }

    public KeyEffectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(this.widthMeasureSpec,MeasureSpec.EXACTLY ), MeasureSpec.makeMeasureSpec(this.heightMeasureSpec,MeasureSpec.EXACTLY ));
    }

    private void init(Drawable drawable) {
        try {
            setImageDrawable(drawable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //setImageResource(R.drawable.ic_download_cirle);
        setScaleType(ScaleType.FIT_XY);
    }


    private int propertyName = 50;

    /* your code */

    public int getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(int propertyName) {
        this.propertyName = propertyName;
    }

    /*
    There is no need to declare method for your animation, you
    can, of course, freely do it outside of this class. I'm including code
    here just for simplicity of answer.
    */
    public void animateProperty() {
//        ObjectAnimator.ofInt(this, "propertyName", 123).start();

        float values_end_tran_x = ThreadLocalRandom.current().nextInt(-100, 100 + 1);
        float values_end_tran_y = ThreadLocalRandom.current().nextInt(-100, 100 + 1);
        //float values_end_tran_z = ThreadLocalRandom.current().nextInt(-1, 1 + 1);
        float values_end_rotation = ThreadLocalRandom.current().nextInt(-360, 360 + 1);
        float values_end_scale_x = (float) ThreadLocalRandom.current().nextDouble(-0.8, 1.5 + 1);
        float values_end_scale_y = (float) ThreadLocalRandom.current().nextDouble(-0.8, 1.5 + 1);

        ObjectAnimator anim_tran_x = ObjectAnimator.ofFloat(this, TRANSLATION_X, 0f, values_end_tran_x);
        ObjectAnimator anim_tran_y = ObjectAnimator.ofFloat(this, TRANSLATION_Y, 0f, values_end_tran_y);
        //ObjectAnimator anim_tran_z = ObjectAnimator.ofFloat(this, TRANSLATION_Z, 0f, values_end_tran_z);
        ObjectAnimator anim_alpha = ObjectAnimator.ofFloat(this, ALPHA, 1f, 0f);
        ObjectAnimator anim_rotation = ObjectAnimator.ofFloat(this, ROTATION, 0f, values_end_rotation);
        ObjectAnimator anim_scale_x = ObjectAnimator.ofFloat(this, SCALE_X, 1f, values_end_scale_x);
        ObjectAnimator anim_scale_y = ObjectAnimator.ofFloat(this, SCALE_Y, 1f, values_end_scale_x);


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(anim_tran_x, anim_tran_y, anim_alpha, anim_rotation, anim_scale_x, anim_scale_y);
        animatorSet.setDuration(1000);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animatorSet.start();


    }

}
