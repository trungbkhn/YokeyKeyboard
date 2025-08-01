package com.tapbi.spark.yokey.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import com.tapbi.spark.yokey.R
import com.android.inputmethod.compat.InputMethodSubtypeCompatUtils
import com.android.inputmethod.keyboard.KeyboardId
import com.android.inputmethod.keyboard.KeyboardLayoutSet
import com.android.inputmethod.latin.InputView
import com.android.inputmethod.latin.RichInputMethodSubtype
import com.android.inputmethod.latin.common.Constants
import com.android.inputmethod.latin.utils.ResourceUtils
import com.android.inputmethod.latin.utils.SubtypeLocaleUtils
import com.google.gson.Gson
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant
import com.tapbi.spark.yokey.common.LiveEvent
import com.tapbi.spark.yokey.data.local.entity.Emoji
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity
import com.tapbi.spark.yokey.data.model.DetailObject
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.model.theme.ThemeModel
import com.tapbi.spark.yokey.interfaces.ActiveKeyboardListener
import com.tapbi.spark.yokey.ui.base.BaseViewModel
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.DisplayUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : BaseViewModel() {
    val mLiveDataRemoveAds: LiveEvent<Boolean> = LiveEvent()
    var mLiveEventKeyboardShow: LiveEvent<Int> = LiveEvent()
    var mLiveEventScreenSticker: LiveEvent<Boolean> = LiveEvent()
    var mFlowEvent = MutableSharedFlow<Boolean>()
    var mLiveEventCrop: LiveEvent<MessageEvent> = LiveEvent()
    var mLiveEventSoundKey: LiveEvent<MessageEvent> = LiveEvent()
    var mLiveEventScreenMyTheme: LiveEvent<MessageEvent> = LiveEvent()
    var mLiveEventCheckGradient: LiveEvent<Int> = LiveEvent()
    var mLiveEventDismissPremium: LiveEvent<Boolean> = LiveEvent()
    var mLiveDataDetailObject = MutableLiveData<DetailObject?>()
    var mLiveDataNextAfterAds = MutableLiveData<Boolean>()
    var mLiveDataThemeEntity = MutableLiveData<ThemeEntity?>()
    var mLiveEventScreen = LiveEvent<MessageEvent>()
    var eventDismissLoadingAds: LiveEvent<Boolean> = LiveEvent()
    var dissmissDialogDetail = MutableLiveData<Boolean>()
    @SuppressLint("CheckResult")
    fun loadEmojiDBPhase7() {
        App.instance.stickerRepository!!.loadEmoji()
            .subscribe({
                if (!it.isNullOrEmpty()) {
                    App.instance.listEmojiDb.addAll(it)
                }
            }, {
                print("")
            }).let {
                compositeDisposable.add(it)
            }
    }

    @SuppressLint("CommitPrefEdits")
    fun loadEmojiFromJson() {
      //  if (App.instance.mPrefs!!.getBoolean(Constant.CHECK_EMOJI_UPDATE_NEW_PHASE7, true)) {
        coroutineScope.launch(Dispatchers.Default) {
                val listEmojiSticker = ArrayList<Emoji>()
                if (App.instance.listEmojiDb.isNullOrEmpty()) {
                    listEmojiSticker.addAll(loadEmoji(Constant.NAME_EMOJI_TRENDING, Constant.NAME_OBJECT_EMOJI_TRENDING))
                    listEmojiSticker.addAll(loadEmoji(Constant.NAME_EMOJI_NEW_PHASE7, Constant.NAME_OBJECT_EMOJI_NEW_PHASE7))
                        insertDataEmojiToDB(listEmojiSticker)
                    App.instance.mPrefs!!.edit().putBoolean(Constant.CHECK_UPDATE_EMOJI_PHASE7, false)
                        .apply()
                } else {
                    listEmojiSticker.addAll(App.instance.listEmojiDb)
                    if (App.instance.mPrefs!!.getBoolean(Constant.CHECK_UPDATE_EMOJI_PHASE7, true)) {
                        listEmojiSticker.addAll(
                            addEmojiUpdate(
                                Constant.NAME_EMOJI_NEW_PHASE7,
                                Constant.NAME_OBJECT_EMOJI_NEW_PHASE7
                            )
                        )
                        App.instance.mPrefs!!.edit().putBoolean(Constant.CHECK_UPDATE_EMOJI_PHASE7, false)
                            .apply()
                    }
                    App.instance.listEmojiDb.clear()
                }
            }
      //  }
    }

    private fun addEmojiUpdate(
        emojiName: String,
        objectsEmoji: String
    ): ArrayList<Emoji> {
        val listEmojiUpdate =
            loadEmoji(emojiName, objectsEmoji)
        insertDataEmojiToDB(listEmojiUpdate)
        return listEmojiUpdate
    }

    private fun insertDataEmojiToDB(listEmoji: ArrayList<Emoji>) {
        coroutineScope.launch {
            Timber.d("ducNQ insertDataEmojiToDB:1 "+Thread.currentThread().name);
            withContext(Dispatchers.IO) {
                Timber.d("ducNQ insertDataEmojiToDB:2 "+Thread.currentThread().name);
                App.instance.stickerRepository!!.insertDataEmoji(listEmoji)
            }
        }
    }

    private fun loadEmoji(emojiName: String, objectsEmoji: String): ArrayList<Emoji> {
        val jsonArray = CommonUtil.getDataAssetLocal(
            App.instance,
            emojiName,
            objectsEmoji
        )
        val listEmoji: ArrayList<Emoji> = ArrayList()
        if (jsonArray != null) {
            for (j in 0 until jsonArray.length()) {
                val gson = Gson()
                val key = gson.fromJson(
                    jsonArray[j].toString(),
                    Emoji::class.java
                )
                listEmoji.add(key)
            }
        }
        return listEmoji
    }



    fun initPreview(context: Context, viewParent : ConstraintLayout, themeModel : ThemeModel, isChangeBuilder : Boolean, activeKeyboardListener: ActiveKeyboardListener) : InputView {
        var mCurrentInputView : InputView? = App.instance?.mInputView
        val keyboardWidth = DisplayUtils.getScreenWidth()
//        val keyboardHeight = DisplayUtils.dp2px(Constant.HEIGHT_VIEW_KEYBOARD)
        val keyboardHeight = ResourceUtils.getKeyboardHeight(context.resources, null)

        if (mCurrentInputView == null) {
            mCurrentInputView =
                LayoutInflater.from(context)
                    .inflate(R.layout.input_view, null) as InputView
            mCurrentInputView.mainKeyboardView.setKeyPreviewPopupEnabled(
                true,
                0
            )
            mCurrentInputView.mainKeyboardView.setKeyPreviewAnimationParams(
                true,
                1.2f,
                1.2f,
                1000,
                1f,
                1f,
                100
            )
            mCurrentInputView.mainKeyboardView.updateShortcutKey(true)
            mCurrentInputView.mainKeyboardView.startDisplayLanguageOnSpacebar(
                false, 10,
                false
            )


        }
        if (App.instance?.mInputView == null) {
            App.instance?.mInputView = mCurrentInputView
        }
        val layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        if (mCurrentInputView.parent != null && mCurrentInputView.parent is ViewGroup) {
            (mCurrentInputView.parent as ViewGroup).removeView(mCurrentInputView)
        }
        Timber.d("duongcv active keyboard add");
        viewParent.addView(mCurrentInputView, layoutParams)
        if (isChangeBuilder) {
            val builder = KeyboardLayoutSet.Builder(context, null)
            builder.setKeyboardGeometry(keyboardWidth, keyboardHeight)
            builder.setSubtype(
                RichInputMethodSubtype(
                    InputMethodSubtypeCompatUtils.newInputMethodSubtype(
                        R.string.subtype_no_language_qwerty, R.drawable.ic_ime_switcher_dark,
                        SubtypeLocaleUtils.NO_LANGUAGE, Constants.Subtype.KEYBOARD_MODE,
                        RichInputMethodSubtype.EXTRA_VALUE_OF_DUMMY_NO_LANGUAGE_SUBTYPE,
                        false /* isAuxiliary */, false /* overridesImplicitlyEnabledSubtype */,
                        RichInputMethodSubtype.SUBTYPE_ID_OF_DUMMY_NO_LANGUAGE_SUBTYPE
                    )
                )
            )
            builder.setVoiceInputKeyEnabled(false)
            builder.setLanguageSwitchKeyEnabled(true)
//            builder.setRowNumberEnabled(false)
//            builder.setQwertyVietNamese(false)
            builder.setSplitLayoutEnabledByUser(false)
            val mKeyboardLayoutSet = builder.build()
            KeyboardLayoutSet.onKeyboardChanged()
            val newKeyboard = mKeyboardLayoutSet.getKeyboard(KeyboardId.ELEMENT_ALPHABET)
//            mCurrentInputView.mainKeyboardView.changeCheckAccessibility(false)
            mCurrentInputView.mainKeyboardView.keyboard = newKeyboard
            mCurrentInputView.mainKeyboardView.setFullLanguageDisplay("English")
        }

        mCurrentInputView.ctlOpenSetting.visibility = View.VISIBLE
        mCurrentInputView.tvEnabelKeyboard.setOnClickListener {
            activeKeyboardListener.active()
        }

        mCurrentInputView.mainKeyboardView.setThemeForKeyboard()
        mCurrentInputView.updateBackgroundKeyboard(themeModel?.background?.backgroundColor, themeModel?.background?.backgroundImage)
//        mCurrentInputView.changeColorIcon()
        mCurrentInputView.updateColorIconMenu()
        mCurrentInputView.mainKeyboardView.invalidateAllKeys()
        return mCurrentInputView
    }



}