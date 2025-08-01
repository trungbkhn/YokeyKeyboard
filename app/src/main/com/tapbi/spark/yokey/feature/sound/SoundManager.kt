package com.tapbi.spark.yokey.feature.sound

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.preference.PreferenceManager
import android.text.TextUtils
import com.android.inputmethod.latin.settings.Settings
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.util.Constant
import java.io.IOException

class SoundManager() {

    private val MAX_STREAMS = 10
    private var stream = -1
    private var soundPool: SoundPool? = null
    private var sampleId = 0
    private var context: Context? = null
    private var checkStatusSystem = 0f
    private var audioManager: AudioManager? = null
    private var volumeKey = 0.5f
    private var mPrefs: SharedPreferences? = null
    private var idAudioForRaw: String? = ""


    constructor(context: Context) : this() {
        this.context = context
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        initSound()
        getCurrentSystemSoundSetting()
    }

    private fun getCurrentSystemSoundSetting() {
        checkStatusSystem = audioManager!!.ringerMode.toFloat()

        volumeKey = (1 - Math.log((getMaxVolumeSystem() - getVolumeSystem()).toDouble()) /
                    Math.log(getMaxVolumeSystem().toDouble())).toFloat()
    }

    private fun initSound() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val builder = SoundPool.Builder()
            builder.setAudioAttributes(audioAttributes).setMaxStreams(MAX_STREAMS)
            soundPool = builder.build()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSound(): Boolean {
        if(audioManager!=null) checkStatusSystem = audioManager!!.ringerMode.toFloat()
        if (Settings.getInstance().current != null && Settings.getInstance().current.mSoundOn && checkStatusSystem == AudioManager.RINGER_MODE_NORMAL.toFloat() || App.instance.getTypeEditing() == Constant.TYPE_EDIT_CUSTOMIZE) {
            try {
                if (soundPool != null && sampleId != -1) {
                    soundPool!!.setVolume(sampleId, volumeKey, volumeKey)
                    if (stream != -1) {
                        soundPool!!.stop(stream)
                    }
                    var currentVolume =
                        Settings.readKeypressSoundVolume(mPrefs, context!!.resources)
                    if (currentVolume < 0) {
                        currentVolume = 0.5f
                    }
                    stream = soundPool!!.play(sampleId, currentVolume, currentVolume, 1, 0, 1.1f)
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    fun loadSound() {
        getCurrentSystemSoundSetting()
        idAudioForRaw =
            if (App.instance.getTypeEditing() == Constant.TYPE_EDIT_CUSTOMIZE) {
                mPrefs!!.getString(Constant.NAME_FILE_AUDIO_ASSETS_EDIT, Constant.AUDIO_DEFAULT)
            } else {
                App.instance.themeRepository?.currentThemeModel?.sound
            }
        if (idAudioForRaw == null || idAudioForRaw!!.isEmpty()) idAudioForRaw = Constant.AUDIO_DEFAULT
        val afd: AssetFileDescriptor
        if (TextUtils.isEmpty(idAudioForRaw) || idAudioForRaw.equals(Constant.AUDIO_DEFAULT)) {
            sampleId = -1
        } else {
            if (soundPool != null) {
                try {
                    afd = context!!.resources.assets.openFd("sound/$idAudioForRaw/sound.mp3")
                    sampleId = soundPool!!.load(afd, 1)
                    afd.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getVolumeSystem(): Int {
        return try {
            if (audioManager != null) {
                audioManager!!.getStreamVolume(AudioManager.STREAM_SYSTEM)
            }else {
                1
            }
        } catch (e: java.lang.Exception) {
            1
        }
    }

    fun setVolumeSystem(value: Int) {
        audioManager!!.setStreamVolume(AudioManager.STREAM_SYSTEM, value, 0)
    }

    private fun getMaxVolumeSystem(): Int {
        return audioManager!!.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)
    }
}