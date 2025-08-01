package com.tapbi.spark.yokey.util;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharePreferences {
    private static final String NEWS_ADS_PREFERENCES = "NEWS_ADS_PREFERENCES";

    public static String NUMBER_RATE = "number_rate";
    public static String VERSION_CODE = "version_code";

    public static int getInt(String key, Context context) {
        return getIntValue(key, context);
    }


    public static void setInt(String key, int value, Context context) {
        putIntValue(key, value, context);
    }

    public static float getFloat(String key, Context context) {
        return getFloatValue(key, context);
    }

    public static void setFloat(String key, float value, Context context) {
        putFloatValue(key, value, context);
    }


    public static String getString(String key, Context context) {
        return getStringValue(key, context);
    }

    public static void setString(Context context, String key, String value) {
        putStringValue(key, value, context);
    }


    public static void putStringValue(String key, String s, Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                NEWS_ADS_PREFERENCES, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, s);
        editor.commit();
    }

    public static void putBoolean(String key, boolean value, Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                NEWS_ADS_PREFERENCES, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void putFloatValue(String key, Float s, Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                NEWS_ADS_PREFERENCES, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat(key, s);
        editor.commit();
    }

    public static void putLongValue(String key, long l, Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                NEWS_ADS_PREFERENCES, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, l);
        editor.commit();
    }

    public static Long getLongValue(String key, Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                NEWS_ADS_PREFERENCES, 0);
        return pref.getLong(key, 0);
    }

    public static void putIntValue(String key, int value, Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                NEWS_ADS_PREFERENCES, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void putBooleanValue(String key, Boolean s, Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                NEWS_ADS_PREFERENCES, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, s);
        editor.commit();
    }

    public static boolean getBooleanValue(String key, Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                NEWS_ADS_PREFERENCES, 0);
        return pref.getBoolean(key, false);
    }

//    public static boolean getBoolean(String key, Context context) {
//        SharedPreferences pref = context.getSharedPreferences(
//                NEWS_ADS_PREFERENCES, 0);
//        return pref.getBoolean(key, true);
//    }
    public static String getStringValue(String key, Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                NEWS_ADS_PREFERENCES, 0);
        return pref.getString(key, null);
    }

    public static Float getFloatValue(String key, Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                NEWS_ADS_PREFERENCES, 0);
        return pref.getFloat(key, (float) 0);
    }

    public static int getIntValue(String key, Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                NEWS_ADS_PREFERENCES, 0);
        return pref.getInt(key, 0);
    }

}
