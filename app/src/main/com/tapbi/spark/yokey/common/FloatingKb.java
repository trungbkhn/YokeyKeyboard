package com.tapbi.spark.yokey.common;


import com.tapbi.spark.yokey.App;

public class FloatingKb {

    public static final float SCALE_DEFAULT = 1f;

    private float scale;
    private int marginLeft;
    private int marginBottom;

    public FloatingKb() {
        scale = SCALE_DEFAULT;
        marginLeft = (int) (App.getInstance().widthScreen * (1 - scale) / 2);
        marginBottom = (int) (App.getInstance().heightScreen * 0.2);
    }


    public FloatingKb(float scale, int marginLeft, int marginBottom) {
        this.scale = scale;
        this.marginLeft = marginLeft;
        this.marginBottom = marginBottom;
    }

    public FloatingKb(float scale) {
        this.scale = scale;

    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public int getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(int marginLeft) {
        this.marginLeft = marginLeft;
    }

    public int getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
    }
}
