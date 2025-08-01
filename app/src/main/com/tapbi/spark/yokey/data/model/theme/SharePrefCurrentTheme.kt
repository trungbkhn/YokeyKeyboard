package com.tapbi.spark.yokey.data.model.theme

import android.content.Context
import android.content.SharedPreferences

object SharePrefCurrentTheme {
    const val sharedPreferencesPrivate = "SHARE_PREFERENCES_THEME"

    // NOT USE
    private const val ID_THEME = "id_theme"
    private const val NAME_KEYBOARD_THEME = "name_keyboard"
    private const val TYPE_KEYBOARD_THEME = "type_keyboard"
    private const val FONT_THEME = "font"
    private const val KEY_LED_COLOR = "key.led.color"
    private const val KEY_LED_CROSS = "key.led.cross"
    private const val KEY_LED_RADIUS = "key.led.radius"
    private const val KEY_LED_SPEED = "key.led.speed"
    private const val KEY_LED_RANGE = "key.led.range"
    private const val KEY_LED_STROKE_WIDTH = "key.led.strokeWidth"
    private const val KEY_LED_STROKE_STYLE = "key.led.style"
    private const val KEY_LED_STROKE_STYLE_LED = "key.led.styleLed"
    private val sharePrefCurrentThemeInstance: SharePrefCurrentTheme? = null
    fun setPrefCurrentTheme(context: Context, themeModel: ThemeModel) {
        val sharePreferenceCurrentTheme =
            context.getSharedPreferences(sharedPreferencesPrivate, Context.MODE_PRIVATE)
        val editor = sharePreferenceCurrentTheme.edit()
        editor.putString(ID_THEME, themeModel.id)
        editor.putString(NAME_KEYBOARD_THEME, themeModel.nameKeyboard)

        editor.putString(TYPE_KEYBOARD_THEME, themeModel.typeKeyboard)
        editor.putString(FONT_THEME, themeModel.font)
        editor.putString(KEY_LED_COLOR, themeModel.key!!.led!!.colors)
        editor.putFloat(KEY_LED_CROSS, themeModel.key!!.led!!.cross!!)
        editor.putFloat(KEY_LED_RADIUS, themeModel.key!!.led!!.radius!!)
        editor.putFloat(KEY_LED_SPEED, themeModel.key!!.led!!.speed!!)
        editor.putFloat(KEY_LED_RANGE, themeModel.key!!.led!!.range!!)
        editor.putFloat(KEY_LED_STROKE_WIDTH, themeModel.key!!.led!!.strokeWidth!!)
        editor.putString(KEY_LED_STROKE_STYLE, themeModel.key!!.led!!.style)
        editor.putFloat(KEY_LED_STROKE_STYLE_LED, themeModel.key!!.led!!.styleLed!!.toFloat())
        editor.apply()
    }

    fun getPrefThemeIdKeyboard(sharePreferenceCurrentTheme: SharedPreferences): Float {
        return sharePreferenceCurrentTheme.getFloat(ID_THEME, 0f)
    }

    fun getPrefThemeNameKeyboard(sharePreferenceCurrentTheme: SharedPreferences): String? {
        return sharePreferenceCurrentTheme.getString(NAME_KEYBOARD_THEME, "")
    }

    fun getPrefThemeTypeKeyboard(sharePreferenceCurrentTheme: SharedPreferences): String? {
        return sharePreferenceCurrentTheme.getString(TYPE_KEYBOARD_THEME, "")
    }

    fun getPrefThemeFontKeyboard(sharePreferenceCurrentTheme: SharedPreferences): String? {
        return sharePreferenceCurrentTheme.getString(FONT_THEME, "")
    }

    fun getPrefThemeKeyLedColor(sharePreferenceCurrentTheme: SharedPreferences): Float {
        return sharePreferenceCurrentTheme.getFloat(KEY_LED_COLOR, 0f)
    }

    fun getPrefThemeKeyLedCross(sharePreferenceCurrentTheme: SharedPreferences): Float {
        return sharePreferenceCurrentTheme.getFloat(KEY_LED_CROSS, 0f)
    }

    fun getPrefThemeKeyLedRadius(sharePreferenceCurrentTheme: SharedPreferences): Float {
        return sharePreferenceCurrentTheme.getFloat(KEY_LED_RADIUS, 0f)
    }

    fun getPrefThemeKeyLedSpeed(sharePreferenceCurrentTheme: SharedPreferences): Float {
        return sharePreferenceCurrentTheme.getFloat(KEY_LED_SPEED, 0f)
    }

    fun getPrefThemeKeyLedRange(sharePreferenceCurrentTheme: SharedPreferences): Float {
        return sharePreferenceCurrentTheme.getFloat(KEY_LED_RANGE, 0f)
    }

    fun getPrefThemeKeyLedStrokeWidth(sharePreferenceCurrentTheme: SharedPreferences): Float {
        return sharePreferenceCurrentTheme.getFloat(KEY_LED_STROKE_WIDTH, 0f)
    }

    fun getPrefThemeKeyLedStrokeStyle(sharePreferenceCurrentTheme: SharedPreferences): String? {
        return sharePreferenceCurrentTheme.getString(KEY_LED_STROKE_STYLE, "")
    }

    fun getPrefThemeKeyLedStrokeStyleLed(sharePreferenceCurrentTheme: SharedPreferences): Float {
        return sharePreferenceCurrentTheme.getFloat(KEY_LED_STROKE_STYLE_LED, 0f)
    }
}