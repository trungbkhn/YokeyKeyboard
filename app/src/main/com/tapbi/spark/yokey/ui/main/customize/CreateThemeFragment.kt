package com.tapbi.spark.yokey.ui.main.customize

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentCreateThemeBinding
import com.android.inputmethod.keyboard.KeyboardSwitcher
import com.android.inputmethod.latin.AudioAndHapticFeedbackManager
import com.android.inputmethod.latin.common.Constants
import com.google.android.material.tabs.TabLayoutMediator
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.common.CommonVariable
import com.tapbi.spark.yokey.common.Constant.CHECK_CROP_DONE
import com.tapbi.spark.yokey.common.Constant.OPEN_CROP_FRAGMENT
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.model.theme.ThemeModel
import com.tapbi.spark.yokey.ui.adapter.ViewPagerCreateThemeAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.custom.view.CustomLineGradient
import com.tapbi.spark.yokey.ui.custom.view.CustomTextViewGradient
import com.tapbi.spark.yokey.ui.custom.view.ViewBgKeyKb
import com.tapbi.spark.yokey.ui.dialog.DialogCancelTheme
import com.tapbi.spark.yokey.ui.dialog.DialogCreateTheme
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.ui.main.crop.CropFragment
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.Constant.*
import com.tapbi.spark.yokey.util.MySharePreferences
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

class CreateThemeFragment : BaseBindingFragment<FragmentCreateThemeBinding, CreateThemeViewModel>() {
    private var vpCreateThemeAdapter: ViewPagerCreateThemeAdapter? = null
    private var heightKeyboard: Int = 0
    private var keyboardSwitcher: KeyboardSwitcher = KeyboardSwitcher.getInstance()
    private var themeModel: ThemeModel? = null
    private val idTheme: String = System.currentTimeMillis().toString()
    private lateinit var dialogCancel: DialogCancelTheme
    private lateinit var dialogCreateTheme: DialogCreateTheme
    private var isSavePreview: Boolean = false
    private var colorCurrent = "0xFFffffff"
    private lateinit var cropImage: CropFragment
    private var listMyTheme = mutableListOf<ThemeEntity>()
    private var isCheckKillAppText = false
    private var firstCheckPreview = false
    private var timeLoad : Long = 0L
    companion object {
        // Fix bug đang tải hình nền mà lưu theme luôn ZOMJ-727
        var countDownloadBackground = 0
    }
    private val mBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!(activity as MainActivity).isFinishing) {
                dialogCancel.show()
            }
        }
    }


    override fun getViewModel(): Class<CreateThemeViewModel> {
        return CreateThemeViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_create_theme

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {

    }

    override fun onPermissionGranted() {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCreateThemeBinding.inflate(layoutInflater, container, false)

        App.instance.colorIconCustomize = -1
        EventBus.getDefault().register(this)
        return binding.root
    }

    @SuppressLint("CommitPrefEdits")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        countDownloadBackground = 0
        App.instance.changeTypeEdit(Constant.TYPE_EDIT_CUSTOMIZE)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            mBackPressedCallback
        )
        resetKeyboard()
        checkDataAfterKillApp(savedInstanceState)
        initViewPagerControl()
        initDialogCancel()
        initDialogCreate()
        listener()
        eventClick()
//        initHeightKb()
    }

    private fun resetKeyboard() {
        if (isActivateKeyboard) {
            keyboardSwitcher.setOriginalBitmapBg()
            keyboardSwitcher.setOriginalBitmapEmojiPlateView()
            keyboardSwitcher.setColorMenu("0xFFffffff")
        } else {
            mInputView?.setBitMapBackGround();
            mInputView?.mainKeyboardView?.setColorMenu("0xFFFFFFFF")
        }
    }

    private fun checkDataAfterKillApp(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            isCheckKillAppText = true
            instance.checkFirstTimeSetBg = savedInstanceState.getBoolean(CHECK_FIRST_TIME_SETBG)
            instance.changeTypeEdit(savedInstanceState.getInt(TYPE_EDITING))
            instance.mPrefs!!.edit().putBoolean(CHECK_FOCUS_KEY_TEXT_COLOR, true).apply()
            savedInstanceState.getParcelable<ThemeModel?>(DATA_THEMEMODEL)?.apply {
                themeModel = this
            }
            instance.themeRepository?.clearDrawableKeyDefault()
            instance.linkCurrentBg = savedInstanceState.getString(LINK_CURRENT, "")
            instance.typeKey = savedInstanceState.getInt(TYPE_KEY)
            instance.blurKillApp = savedInstanceState.getInt(BLUR_KILLAPP)
            App.instance.blurBg = instance.blurKillApp
            themeModel?.background?.radiusBlur = instance.blurBg
            if (instance.linkCurrentBg != "null") {
                themeModel?.background?.backgroundImage.apply {
                    instance.linkCurrentBg
                }
                themeModel?.typeKeyboard = Constants.ID_CATEGORY_WALL
                if (isActivateKeyboard) {
                    keyboardSwitcher.updateKeyboardTheme("null", instance.linkCurrentBg)
                } else {
                    mInputView?.setupBackgroundKeyboard("null", instance.linkCurrentBg)
                }
            }
            instance.themeRepository!!.clearDrawableKeyDefault()
            themeModel?.typeKey = instance.typeKey
            savedInstanceState.getString(COLOR_TEXT_KILLAPP)?.apply {
                themeModel?.key?.text?.textColor = this
                instance.colorCurrent = this
            }
            CommonUtil.hex2decimal(savedInstanceState.getString(COLOR_ICON_MENU_BAR_KILLAPP))
                .apply {
                    instance.colorIconDefault = this
                }
            savedInstanceState.getString(COLOR_ICON_MENU_BAR_KILLAPP).apply {
                themeModel?.menuBar?.iconColor = this
                colorCurrent = this.toString()
                instance.colorIconDefault = CommonUtil.hex2decimal(this)
            }
            if (isActivateKeyboard) {
                keyboardSwitcher.changeColorIcon()
            } else {
                mInputView?.changeColorIcon()
            }
            savedInstanceState.getString(EFFECT_KEY_KILLAPP)?.apply {
                instance.pathEffect = this
                themeModel?.effect = this
                if (isActivateKeyboard) {
                    keyboardSwitcher.mainKeyboardView?.changeEffect(this)
                } else {
                    mInputView?.mainKeyboardView?.changeEffect(this)
                }
            }
            MySharePreferences.putBoolean(KEY_BOARD, savedInstanceState.getBoolean(CHECK_TYPE_KEY_CURRENT), requireContext())
        }
        if (themeModel == null) init()
        if (savedInstanceState == null) {
            themeModel?.sound = AUDIO_DEFAULT
            themeModel?.key?.text?.textColor = "#FFFFFF"
            instance.mPrefs!!.edit()
                .putString(Constant.NAME_FILE_AUDIO_ASSETS_EDIT, AUDIO_DEFAULT).apply()
            AudioAndHapticFeedbackManager.getInstance().loadSound()
            themeModel?.effect = Constant.ID_NONE
            App.instance.pathEffect = Constant.ID_NONE
            if (isActivateKeyboard) {
                keyboardSwitcher.mainKeyboardView?.changeEffect(Constant.ID_NONE)
            } else {
                mInputView?.mainKeyboardView?.changeEffect(ID_NONE)
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun init() {
        if (instance.themeRepository!!.defaultThemeModel != null) {
            themeModel = instance.themeRepository!!.defaultThemeModel
            themeModel?.background?.backgroundImage = "null"
            themeModel?.menuBar?.iconColor = "0xFFFFFFFF"
            themeModel?.typeKey = 2006
            instance.mPrefs!!.edit()
                .putString(Constant.NAME_FILE_AUDIO_ASSETS_EDIT, Constant.AUDIO_DEFAULT).apply()

        }
        if (themeModel == null) (requireActivity() as MainActivity).changeStartScreen(
            R.id.fragmentHome,
            null
        )
    }

    @SuppressLint("CommitPrefEdits")
    private fun listener() {
        mainViewModel.mLiveEventKeyboardShow.observe(
            viewLifecycleOwner
        ) { height ->
            if (height != null) {
                isShowKeyboard = height > 0
                if (isShowKeyboard) {
                    if (isActivateKeyboard) {
                        keyboardSwitcher.setThemeModel(themeModel)
                        keyboardSwitcher.updateKeyboardTheme(
                            "null",
                            themeModel?.background!!.backgroundImage
                        )
                    } else {
                        if (mInputView != null) {
                            mInputView?.mainKeyboardView?.themeModel = themeModel
                            mInputView?.setupBackgroundKeyboard(
                                "null",
                                themeModel?.background?.backgroundImage
                            )
                        }
                    }
                    if (isSavePreview) {
                        App.instance.checkDownloadBg = false
                        savePreview()
                    }
                }
                calculateTranslateView((height as Int?)!!)
            }
        }

        viewModel.liveDataResultSaveTheme.observe(
            viewLifecycleOwner
        ) { result ->
            if (result != null && result) {
                CommonUtil.customToast(
                    requireActivity(),
                    requireContext().resources.getString(R.string.theme_creation_success)
                )
                instance.mPrefs!!.edit()
                    .putString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, themeModel?.id).apply()
                instance.themeRepository!!.updateCurrentThemeModel()
                val bundle = Bundle()
                bundle.putInt(Constant.KEY_OPEN_SCREEN, Constant.KEY_SCREEN_MYTHEME)
                (requireActivity() as MainActivity).currentPager = 0
                (requireActivity() as MainActivity).currentPageTheme = 5
                (requireActivity() as MainActivity).changeStartScreen(R.id.fragmentHome, bundle)
            } else {
                Toast.makeText(
                    requireContext(),
                    requireContext().resources.getString(R.string.theme_creation_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        viewModel.liveDataListMyTheme.observe(
            viewLifecycleOwner
        ) { listTheme -> listMyTheme = listTheme!! }
        mainViewModel.mLiveEventCrop.observe(viewLifecycleOwner) {
            if (it.key == OPEN_CROP_FRAGMENT) {
                if (it.bundle != null) {
                    cropImage = CropFragment()
                    cropImage.arguments = it.bundle
                    if (!(activity as MainActivity).isFinishing) {
                        cropImage.show(childFragmentManager, "cropImage")
                    }
                }
            }
        }
        viewModel.liveDataNextScreen.observe(viewLifecycleOwner) {
            if (it) {
                if (!viewModel.liveDataNameKeyboard.value.isNullOrEmpty()) {
                    val nameKeyboard = viewModel.liveDataNameKeyboard.value!!
                    createTheme(nameKeyboard)
                    viewModel.liveDataNameKeyboard.value = ""
                } else {
                    (requireActivity() as MainActivity).changeStartScreen(
                        R.id.fragmentHome,
                        null
                    )
                }
                viewModel.liveDataNextScreen.setValue(false)
            }
        }
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        viewModel.loadMyTheme()
    }

    fun savePreview() {
        Handler(Looper.getMainLooper()).postDelayed({
            var isSave = false
            if (isActivateKeyboard) {
                 isSave = keyboardSwitcher.getViewKeyBoardDemo(
                    themeModel,
                    idTheme
                )
            }else {
                val viewBgKeyKb = mInputView?.findViewById<ViewBgKeyKb>(R.id.lyKey)
                keyboardSwitcher.setViewDemoKey(viewBgKeyKb, themeModel, idTheme, mInputView?.mainKeyboardView)
                isSave = true
            }
            Timber.e("Duongcv " + isSave)
            if (!isSave) savePreview()
        }, 50)
    }


    @SuppressLint("CommitPrefEdits")
    private fun eventClick() {
        binding.txtSave.setOnClickListener {
            if (countDownloadBackground == 0) {
                if (isShowKeyboard) showHideKeyboard(binding.edtView)
                isShowKeyboard = false
                isSavePreview = true;
                if (!(activity as MainActivity).isFinishing) {
                    savePreview()
                    dialogCreateTheme.show()
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    dialogCreateTheme.changeRequest(true)
                }, 300)

            }else {
                Toast.makeText(requireContext(), requireContext().resources.getString(R.string.dictionary_downloading) + " " + requireContext().resources.getString(R.string.background).lowercase(), Toast.LENGTH_SHORT).show()
            }

        }
        binding.imgBack.setOnClickListener {
            dialogCancel.show()
        }

        binding.imgShowHideKeyboard.setOnClickListener {
            processShowHideKeyboard()
        }
    }

    private fun processShowHideKeyboard() {
        showHideKeyboard(binding.edtView)
        if (isCheckKillAppText) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (isActivateKeyboard) {
                    keyboardSwitcher.changeTextColor(instance.colorCurrent)
                    keyboardSwitcher.changeColorIcon()
                    keyboardSwitcher.changeTypeKey(instance.typeKey)
                } else {
                    mInputView?.mainKeyboardView?.changeTextColor(instance.colorCurrent)
                    mInputView?.changeColorIcon()
                    mInputView?.mainKeyboardView?.changeTypeKey(instance.typeKey)
                }
            }, 200)
            isCheckKillAppText = false
        }
    }

    private fun initViewPagerControl() {
        if (themeModel == null) return
        vpCreateThemeAdapter = ViewPagerCreateThemeAdapter(this, themeModel!!)
        binding.vpCustomize.offscreenPageLimit = 1
        binding.vpCustomize.adapter = vpCreateThemeAdapter
        binding.vpCustomize.isUserInputEnabled = false
        TabLayoutMediator(
            binding.tabLayoutCustomize,
            binding.vpCustomize,
            TabLayoutMediator.TabConfigurationStrategy { tab, _ -> tab.setCustomView(R.layout.custom_layout_tablayout) }).attach()
        binding.tabLayoutCustomize.post(Runnable {
            if (binding.tabLayoutCustomize.getTabAt(0) != null && binding.tabLayoutCustomize.getTabAt(
                    0
                )!!.customView != null
            ) {
                ((binding.tabLayoutCustomize.getTabAt(0)!!.customView)!!.findViewById(R.id.txtNameTabLayout) as CustomTextViewGradient).text =
                    resources.getString(R.string.background)
            }
            if (binding.tabLayoutCustomize.getTabAt(1) != null && binding.tabLayoutCustomize.getTabAt(
                    1
                )!!.customView != null
            ) {
                ((binding.tabLayoutCustomize.getTabAt(1)!!.customView)!!.findViewById(R.id.txtNameTabLayout) as CustomTextViewGradient).text =
                    resources.getString(R.string.key)
            }
            if (binding.tabLayoutCustomize.getTabAt(2) != null && binding.tabLayoutCustomize.getTabAt(
                    2
                )!!.customView != null
            ) {
                ((binding.tabLayoutCustomize.getTabAt(2)!!.customView)!!.findViewById(R.id.txtNameTabLayout) as CustomTextViewGradient).text =
                    resources.getString(R.string.effect)
            }
            if (binding.tabLayoutCustomize.getTabAt(3) != null && binding.tabLayoutCustomize.getTabAt(
                    3
                )!!.customView != null
            ) {
                ((binding.tabLayoutCustomize.getTabAt(3)!!.customView)!!.findViewById(R.id.txtNameTabLayout) as CustomTextViewGradient).text =
                    resources.getString(R.string.sound)
            }
            changeTabLayout(binding.vpCustomize.currentItem)
        })

        binding.vpCustomize.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                showHideSpaceBackgroundPager()
                changeTabLayout(position)
            }
        })

    }

    private fun delayShow() {
        firstCheckPreview = true
        checkLoadPreview()
        themeModel?.background?.backgroundImage?.apply {
            if (isActivateKeyboard) {
                keyboardSwitcher.updateKeyboardTheme(
                    "null",
                    this
                )
            } else {
                mInputView?.setupBackgroundKeyboard("null", this)
            }
        }
    }

    private fun changeTabLayout(position: Int) {
        for (i in 0..3) {
            initTabItem(i, false)
        }
        initTabItem(position, true)
    }

    private fun initTabItem(position: Int, active: Boolean) {
        val titleTab =
            ((binding.tabLayoutCustomize.getTabAt(position)!!.customView)!!.findViewById(R.id.txtNameTabLayout) as CustomTextViewGradient)
        val lineTab =
            ((binding.tabLayoutCustomize.getTabAt(position)!!.customView)!!.findViewById(R.id.lineTablayout) as CustomLineGradient)
        titleTab.isTextGradient(active)
        lineTab.visibility = if (active) View.VISIBLE else View.INVISIBLE
    }

    @SuppressLint("CommitPrefEdits")
    override fun onResume() {
        super.onResume()

        App.instance.changeTypeEdit(Constant.TYPE_EDIT_CUSTOMIZE)

        delayShow()
        if (isActivateKeyboard) {
            keyboardSwitcher.setThemeModel(themeModel)
            keyboardSwitcher.changeColorIcon()
            keyboardSwitcher.setOriginalBitmapBg()
        } else {
            mInputView?.mainKeyboardView?.themeModel = themeModel
            mInputView?.changeColorIcon()
            mInputView?.setBitMapBackGround()

        }
        if (themeModel != null) {
            instance.mPrefs!!.edit()
                .putString(Constant.NAME_FILE_AUDIO_ASSETS_EDIT, themeModel!!.sound).apply()
        }
        instance.checkCustomTheme = false
        AudioAndHapticFeedbackManager.getInstance().loadSound()
    }


    override fun onPause() {
        super.onPause()
        if (isShowKeyboard && isActivateKeyboard) {
            processShowHideKeyboard()
            calculateTranslateView(0)
        }
        App.instance.changeTypeEdit(Constant.TYPE_EDIT_NONE)
        instance.bitmap = null
        instance.checkCustomTheme = true
        instance.checkBackGroundEmojiPalettesView = false
    }

    private fun calculateTranslateView(heightKeyboard: Int) {
        this.heightKeyboard = heightKeyboard
        if (heightKeyboard > 0) {
            binding.imgShowHideKeyboard.setImageResource(R.drawable.ic_hide_keyboard)
            binding.spaceBottom.layoutParams.height = heightKeyboard
        } else {
            binding.imgShowHideKeyboard.setImageResource(R.drawable.ic_show_keyboard)
            binding.spaceBottom.layoutParams.height = 1
        }
        showHideSpaceBackgroundPager()
        binding.spaceBottom.requestLayout()
    }

    @SuppressLint("CommitPrefEdits")
    @Subscribe
    fun onMessageEventBus(eventMessage: MessageEvent) {
        val bundle = eventMessage.bundle
        when (eventMessage.key) {
            Constant.ACTION_CHANGE_BACKGROUND_CUSTOMZIE -> {
                if (bundle != null) {
                    val link = bundle.getString(Constant.DATA_CHANGE_BACKGROUND_CUSTOMZIE)
                    if (link != null) {
                        App.instance.checkFirstTimeSetBg = true
                        instance.checkBackGroundEmojiPalettesView = true
                        themeModel?.background?.backgroundImage = link
                        themeModel?.typeKeyboard = Constants.ID_CATEGORY_WALL
                        if (isActivateKeyboard) {
                            keyboardSwitcher.updateKeyboardTheme("null", link)
                        }else {
                            mInputView?.setupBackgroundKeyboard("null", link)
                        }
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (isAdded) {
                                if (isActivateKeyboard) {
                                    keyboardSwitcher.setOriginalEmojiPlateView()
                                }
                            }
                        }, 100)
                    }
                }
            }

            Constant.ACTION_CHANGE_BLUR_BACKGROUND_CUSTOMZIE -> {
                if (bundle != null) {
                    val radiusBlur = bundle.getInt(Constant.DATA_CHANGE_BLUR_BACKGROUND_CUSTOMZIE)
                    App.instance.blurBg = radiusBlur
                    instance.blurKillApp = radiusBlur
                    themeModel?.background!!.radiusBlur = radiusBlur
                    if (!instance.checkBackGroundEmojiPalettesView) {
                        if (isActivateKeyboard) {
                            keyboardSwitcher.setOriginalBitmapBg()
                        }else {
                            mInputView?.setBitMapBackGround()
                        }
                    }
                    if (isActivateKeyboard) {
                        keyboardSwitcher.changeBlurBackground(radiusBlur)
                    }else {
                        mInputView?.changeRadius(requireContext(), radiusBlur)
                    }
                }
            }

            Constant.ACTION_CHANGE_EFFECT_CUSTOMZIE -> {
                if (bundle != null) {
                    val effect = bundle.getString(Constant.DATA_CHANGE_EFFECT_CUSTOMZIE)
                    themeModel?.effect = effect!!
                    App.instance.pathEffect = effect
                    if (isActivateKeyboard) {
                        keyboardSwitcher.mainKeyboardView?.changeEffect(effect)
                    }else {
                        mInputView?.mainKeyboardView?.changeEffect(effect)
                    }
                }
            }

            Constant.ACTION_CHANGE_SOUND_CUSTOMZIE -> {
                if (bundle != null) {
                    val sound = bundle.getString(Constant.DATA_CHANGE_SOUND_CUSTOMZIE)
                    themeModel?.sound = sound!!
                    instance.mPrefs!!.edit()
                        .putString(Constant.NAME_FILE_AUDIO_ASSETS_EDIT, sound).apply()
                    AudioAndHapticFeedbackManager.getInstance().loadSound()
                }
            }

            Constant.ACTION_CHANGE_COLOR_ICON_MENU_CUSTOMZIE -> {
                if (bundle != null) {
                    val color = bundle.getString(Constant.DATA_CHANGE_COLOR_ICON_MENU_CUSTOMZIE)
                    themeModel?.menuBar!!.iconColor = color!!
                    instance.colorIconDefault = CommonUtil.hex2decimal(color)
                    if (isActivateKeyboard) {
                        keyboardSwitcher.changeColorIcon()
                    }else {
                        mInputView?.changeColorIcon()
                    }
                }
            }

            Constant.ACTION_CHANGE_COLOR_ICON_TEXT_CUSTOMZIE -> {
                if (bundle != null) {
                    val color = bundle.getString(Constant.DATA_CHANGE_COLOR_ICON_TEXT_CUSTOMZIE)
                    themeModel?.key!!.text!!.textColor = color!!
                    keyboardSwitcher.changeTextColor(color)
                    mInputView?.mainKeyboardView?.changeTextColor(color)
                }
            }

            Constant.ACTION_CHANGE_TYPE_KEY_CUSTOMZIE -> {
                if (bundle != null) {
                    instance.themeRepository!!.clearDrawableKeyDefault()
                    val type = bundle.getInt(Constant.DATA_CHANGE_TYPE_KEY_CUSTOMZIE)
                    themeModel?.typeKey = type
                    instance.typeKey = type
                    keyboardSwitcher.changeTypeKey(type)
                    mInputView?.mainKeyboardView?.changeTypeKey(type)
                }
            }

            CHECK_CROP_DONE -> {
                mainViewModel.mLiveEventCheckGradient.postValue(com.tapbi.spark.yokey.common.Constant.CHECK_CROP)
            }

            Constant.ACTION_CHANGE_STATE_ACTIVATE_KEYBOARD -> {
               if (firstCheckPreview) {
                    checkLoadPreview()
                }
                if (!isActivateKeyboard && isShowKeyboard){
                    showHideKeyboard(binding.edtView)
                }
            }
        }
    }

    fun checkLoadPreview(){
        if (System.currentTimeMillis() - timeLoad > 1500){
            timeLoad = System.currentTimeMillis()
            mInputView = checkActivateKeyboard(R.id.createThemeFragment, isShowKeyboard, null, null, mInputView , binding.ctlPreview, binding.cvShowHideKeyboard, binding.spaceBottom, true, themeModel)
        }
    }

    @SuppressLint("CommitPrefEdits")
    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        instance.bitmap = null
        instance.checkCustomTheme = false
        instance.checkBackGroundEmojiPalettesView = false
        instance.blurKillApp = 0
        App.instance.blurBg = 0
        instance.mPrefs!!.edit().putBoolean(CHECK_FOCUS_KEY_TEXT_COLOR, false).apply()
        //  keyboardSwitcher.changeColorText("#FFF0F0F0")
    }


    private fun initDialogCancel() {
        dialogCancel =
            DialogCancelTheme(requireContext(), object : DialogCancelTheme.IListenerDialog {

                override fun notCancel() {
                    dialogCancel.dismiss()
                }

                @SuppressLint("CommitPrefEdits")
                override fun okCancel() {
                    if (isAdded) {
                        instance.colorCurrent = "#ffffff"
                        instance.pathEffect = Constant.ID_NONE
                        instance.mPrefs!!.edit().putBoolean(CHECK_FOCUS_KEY_TEXT_COLOR, false)
                            .apply()
                        //   keyboardSwitcher.changeColorText("#FFF0F0F0")
                        dialogCancel.dismiss()
                        if (instance.themeModelSound != null) {
                            instance.mPrefs!!.edit()
                                .putString(
                                    Constant.NAME_FILE_AUDIO_ASSETS_EDIT,
                                    instance.themeModelSound!!.sound
                                ).apply()
                            AudioAndHapticFeedbackManager.getInstance().loadSound()
                        }
                        instance.checkFirstTimeSetBg = false
                        instance.pathEffect = Constant.ID_NONE
                        instance.changeTypeEdit(Constant.TYPE_EDIT_NONE)
                        showAdsFull(getString(R.string.tag_inter_exit_create_theme))
                        instance.linkCurrentBg = "null"
                    }
                }
            })
    }

    private fun initDialogCreate() {
        dialogCreateTheme = DialogCreateTheme(
            requireActivity(),
            object : DialogCreateTheme.IListenerCreate {
                @SuppressLint("CommitPrefEdits")
                override fun cancelCreate() {
                    dialogCreateTheme.changeRequest(false)
                    isSavePreview = false
                    dialogCreateTheme.dismiss()
                }

                @SuppressLint("CommitPrefEdits")
                override fun agreeCreate(nameKeyboard: String) {
                    //   keyboardSwitcher.changeColorText("#FFF0F0F0")
                    if (checkNameKeyboardDuplicate(nameKeyboard)) {
                        if (instance!!.billingManager!!.isPremium ) {
                            createTheme(nameKeyboard)
                        } else {
                            viewModel.liveDataNameKeyboard.value = nameKeyboard
                            dialogCreateTheme.dismiss()
                            showAdsFull(getString(R.string.tag_inter_create_theme))
                        }
                    } else {
                        binding.txtSave.isEnabled = true
//                        Toast.makeText(requireContext(), context!!.resources.getString(R.string.name_keyboard_exist), Toast.LENGTH_SHORT).show()
                        CommonUtil.customToast(
                            requireActivity(),
                            context!!.resources.getString(R.string.name_keyboard_exist)
                        )
                    }
                }

                override fun showDialog() {
//                    if (isSavePreview) savePreview()
                }
            })

    }

    private fun createTheme(nameTheme: String) {
        binding.txtSave.isEnabled = false
        App.instance.changeTypeEdit(Constant.TYPE_EDIT_NONE)
        App.instance.colorCurrent = "#ffffff"
        instance.pathEffect = Constant.ID_NONE
        instance.mPrefs!!.edit().putBoolean(CHECK_FOCUS_KEY_TEXT_COLOR, false).apply()
        dialogCreateTheme.changeRequest(false)
        themeModel?.id = idTheme
        themeModel?.nameKeyboard = nameTheme
        isSavePreview = false
        instance.linkCurrentBg = "null"
        viewModel.createTheme(themeModel!!)
        dialogCreateTheme.dismiss()
        App.instance.checkFirstTimeSetBg = false
        // (activity as MainActivity).currentPager=0
        EventBus.getDefault()
            .postSticky(MessageEvent(com.tapbi.spark.yokey.common.Constant.MH_MY_THEME))
    }

    override fun onDestroy() {
        super.onDestroy()
        instance.themeRepository!!.loadThemeDefault()
    }

    fun showHideSpaceBackgroundPager() {
        if (binding.vpCustomize.currentItem == 0 && isShowKeyboard) {
            binding.spaceBackgroundPager.visibility = View.VISIBLE
        } else {
            binding.spaceBackgroundPager.visibility = View.GONE
        }
    }

    fun checkNameKeyboardDuplicate(name: String): Boolean {
        if (listMyTheme.size > 0) {
            for (i in 0 until listMyTheme.size) {
                if (listMyTheme.size > i && listMyTheme[i].name!! == name) {
                    return false
                }
            }
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (themeModel != null) {
            outState.remove(Constant.DATA_THEMEMODEL)
            outState.putParcelable(Constant.DATA_THEMEMODEL, themeModel)
            outState.putString(COLOR_TEXT_KILLAPP, themeModel?.key?.text?.textColor)
            outState.putString(COLOR_ICON_MENU_BAR_KILLAPP, themeModel?.menuBar?.iconColor)
            outState.putString(EFFECT_KEY_KILLAPP, themeModel?.effect)
        }
        outState.putString(LINK_CURRENT, instance.linkCurrentBg)
        outState.putInt(TYPE_KEY, instance.typeKey)
        outState.putInt(BLUR_KILLAPP, instance.blurBg)
        outState.putInt(TYPE_EDITING, Constant.TYPE_EDIT_CUSTOMIZE)
        outState.putBoolean(CHECK_TYPE_KEY_CURRENT, true)
        outState.putBoolean(CHECK_FIRST_TIME_SETBG, App.instance.checkFirstTimeSetBg)
        super.onSaveInstanceState(outState)
    }

    @SuppressLint("CommitPrefEdits")
    override fun onStop() {
        if (App.instance.themeModelSound != null) {
            instance.mPrefs!!.edit()
                .putString(
                    Constant.NAME_FILE_AUDIO_ASSETS_EDIT,
                    App.instance.themeModelSound!!.sound
                )
                .apply()
            AudioAndHapticFeedbackManager.getInstance().loadSound()
        }
        super.onStop()
    }

    override fun nexScreenAfterAds() {
        viewModel.liveDataNextScreen.postValue(true)
    }


}