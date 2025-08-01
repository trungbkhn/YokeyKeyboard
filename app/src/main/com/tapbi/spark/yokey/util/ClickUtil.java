package com.tapbi.spark.yokey.util;

public class ClickUtil {
    public static long lastClickTime = 0;

    public static boolean checkTime() {
        long currrentTime = System.currentTimeMillis();
        if ((currrentTime - lastClickTime) < 700) {
            return false;
        }
        lastClickTime = currrentTime;
        return true;
    }
    public static boolean checkTime1s() {
        long currrentTime = System.currentTimeMillis();
        if ((currrentTime - lastClickTime) < 1000) {
            return false;
        }
        lastClickTime = currrentTime;
        return true;
    }
}