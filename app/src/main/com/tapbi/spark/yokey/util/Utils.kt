package com.tapbi.spark.yokey.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.tapbi.spark.yokey.R
import com.google.android.material.internal.ViewUtils.dpToPx
import com.tapbi.spark.yokey.common.Constant
import java.io.*
import java.net.URI
import java.util.Locale


object Utils {
    fun isAtLeastSdkVersion(versionCode: Int): Boolean {
        return Build.VERSION.SDK_INT >= versionCode
    }

    var listTabName = listOf(
        R.string.text_tab_themes,
        R.string.text_tab_fonts,
        R.string.text_tab_fonts,
        R.string.text_tab_emoji,
        R.string.text_tab_setting
    )
    var listTabImage = listOf(
        R.drawable.ic_theme,
        R.drawable.ic_font,
        R.drawable.ic_font,
        R.drawable.ic_emoji,
        R.drawable.ic_setting
    )
    var listTabChooseImage = listOf(
        R.drawable.ic_theme_choose,
        R.drawable.ic_font_choose,
        R.drawable.ic_font_choose,
        R.drawable.icon_tab_emoji_clicked,
        R.drawable.ic_setting_choose
    )
    var listEmojiName = listOf<String>(Constant.NAME_EMOJI_TRENDING, Constant.NAME_EMOJI_NEW_PHASE7)
    var listTabChooseEmojiSticker = listOf(
        R.drawable.icon_emoji_chooses,
        R.drawable.icobn_sticker_chooses
    )
    var listTabEmojiSticker = listOf(
        R.drawable.icon_emoji_fill,
        R.drawable.icon_stickers
    )
    var listTabNameEmojiSticker = listOf(
        R.string.txt_tab_emojis,
        R.string.txt_tab_stickers
    )
    var listTabTheme = listOf(
        R.string.txt_name_hot_theme,
        R.string.txt_featured,
        R.string.txt_name_led_theme,
        R.string.txt_name_color_theme,
        R.string.txt_wallpaper,
        R.string.txt_MyTheme

    )
    var listTabFont = listOf(
        R.string.tab_all,
        R.string.tab_sans_serif,
        R.string.tab_serif,
        R.string.tab_display,
        R.string.tab_handwriting,
        R.string.tab_script,
        R.string.tab_tiktok,
        R.string.tab_instagram,
        R.string.tab_other
    )
    var listTabNameEmoji = listOf(
        R.string.txt_tab_trending,
        R.string.txt_tab_top,
        R.string.txt_tab_latest
    )
    var listTabSticker = listOf(
        R.string.txt_tiktok,
        R.string.txt_animal,
        R.string.tab_other
    )

    @JvmStatic
    fun writeToFileFromContentUri(file: File?, uri: Uri?, context: Context): Boolean {
        if (file == null || uri == null) return false
        try {
            val stream: InputStream? = context.contentResolver.openInputStream(uri)
            val output: OutputStream = FileOutputStream(file)
            if (stream == null) return false
            val buffer = ByteArray(4 * 1024)
            var read: Int
            while (stream.read(buffer).also { read = it } != -1) output.write(buffer, 0, read)
            output.flush()
            output.close()
            stream.close()
            return true
        } catch (e: FileNotFoundException) {
            //   Log.e(MainActivity.TAG, "Couldn't open stream: " + e.message)
        } catch (e: IOException) {
            //  Log.e(MainActivity.TAG, "IOException on stream: " + e.message)
        }
        return false
    }

    fun spToPx(sp: Float, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }

    @SuppressLint("RestrictedApi")
    fun dpToSp(dp: Float, context: Context): Float {
        return (dpToPx(context, dp.toInt()) / context.resources.displayMetrics.scaledDensity)
    }

    fun getLocaleStringResource(context: Context): String {
        // use latest api
        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales[0]
        } else {
            Resources.getSystem().configuration.locale
        }
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
//        val country = locale.country
//        Timber.d("ducNQ getLocaleStringResource: "+country);
//        if (country.isEmpty()) {
//            return locale.language.toString()
//        }
        return locale.language.toString() + "_" + locale.country.toString()
    }

    fun setTextViewColor(context: Context, textView: TextView, size: Int) {
        val colors: IntArray = context.resources.getIntArray(R.array.colors)
        val textPaint = textView.paint
        val measureWidth = textPaint.measureText(textView.text.toString())
        val shader = if (size == 2) {
            LinearGradient(0f, 0f, measureWidth * size, 0f, colors, null, Shader.TileMode.CLAMP)
        } else {
            LinearGradient(
                measureWidth,
                0f,
                measureWidth * size,
                0f,
                colors,
                null,
                Shader.TileMode.CLAMP
            )
        }
        textView.paint.shader = shader
        textView.setTextColor(colors[0])
    }


    fun showDialogPermission(context: Context) {
        AlertDialog.Builder(context, R.style.AlertDialogStyle).setMessage(
            context.resources.getString(R.string.You_need_to_enable_permissions_to_use_this_feature)
        ).setPositiveButton(
            context.resources.getString(R.string.go_to_setting)
        ) { _, _ -> // navigate to settings
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }.setNegativeButton(
            context.resources.getString(R.string.go_back)
        ) { dialog, _ -> // leave?
            dialog.dismiss()
        }.show()
    }


    @JvmStatic
    fun convertBitmap(bitmap: Bitmap, context: Context): URI {
        val file = File(context.cacheDir, "${System.currentTimeMillis()}")
        file.delete() // Delete the File, just in Case, that there was still another File
        if (!file.exists()) {
            file.createNewFile()
        }
        val fileOutputStream = file.outputStream()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val bytearray = byteArrayOutputStream.toByteArray()
        fileOutputStream.write(bytearray)
        fileOutputStream.flush()
        fileOutputStream.close()
        byteArrayOutputStream.close()
        return file.toURI()
    }

    @JvmStatic
    fun deleteCache(context: Context) {
        try {
            context.cacheDir.deleteRecursively()
        } catch (e: Exception) {
        }
    }

    @JvmStatic
    fun deleteFolder(context: Context) {
        // context.cacheDir.deleteRecursively()
        if (File(context.cacheDir, "CUSTOM NAME").exists()) {
            File(context.cacheDir, "CUSTOM NAME").deleteRecursively()
        }
    }

    @JvmStatic
    fun isOnline(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            try {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    //Log.i("update_statut", "Network is available : true");
                    return true
                }
            } catch (e: Exception) {
                //Log.i("update_statut", "" + e.getMessage());
            }
        }
        //Log.i("update_statut","Network is available : FALSE ");
        return false
    }
}