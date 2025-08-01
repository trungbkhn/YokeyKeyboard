package com.tapbi.spark.yokey.ui.main.trykeyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.inputmethod.latin.utils.UncachedInputMethodManagerUtils
import com.bumptech.glide.Glide
import com.google.android.ads.nativetemplates.OnDecorationAds
import com.ironman.trueads.common.Common
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.data.local.entity.ItemFont
import com.tapbi.spark.yokey.data.local.entity.Sticker
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.model.ThemeObject
import com.tapbi.spark.yokey.data.model.theme.ThemeModel
import com.tapbi.spark.yokey.databinding.FragmentTryKeyboardBinding
import com.tapbi.spark.yokey.ui.adapter.FontAdapter
import com.tapbi.spark.yokey.ui.adapter.ItemsThemeAdapter
import com.tapbi.spark.yokey.ui.adapter.StickerAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.util.DisplayUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar

class TryKeyboardFragment :
    BaseBindingFragment<FragmentTryKeyboardBinding, TryKeyboardViewModel>(),
    ItemsThemeAdapter.ItemClickListener {


    private var themeModel: ThemeModel? = null
    private var isShowKb = false
    private var firstTime = true
    private var SIZE_48: Int = 0
    var itemsThemeAdapter: ItemsThemeAdapter? = null
    var listThemeObject: ArrayList<ThemeObject> = ArrayList()
    private var stickerAdapter: StickerAdapter? = null
    private var listSticker: ArrayList<Sticker> = ArrayList()
    private var listItemFont: ArrayList<ItemFont> = ArrayList()
    private var fontAdapter: FontAdapter? = null


    override fun getViewModel(): Class<TryKeyboardViewModel> {
        return TryKeyboardViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_try_keyboard

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            (requireActivity() as MainActivity).typeTryKeyboard =
                savedInstanceState.getInt(com.tapbi.spark.yokey.common.Constant.STATE_TRY_KEYBOARD)
        }
        EventBus.getDefault().register(this)
//        (requireActivity() as MainActivity).keyboardHeightProvider?.addKeyboardListener(this)
        inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        SIZE_48 = DisplayUtils.dp2px(48f)
        initView()
        listener()
        binding.spinLoading.visibility = View.VISIBLE
        loadData()
    }

    override fun onPermissionGranted() {

    }


    fun loadData() {
        if ((requireActivity() as MainActivity).typeTryKeyboard == com.tapbi.spark.yokey.common.Constant.TYPE_TRY_THEME_KEYBOARD) {
            viewModel.loadListTheme()
        } else if ((requireActivity() as MainActivity).typeTryKeyboard == com.tapbi.spark.yokey.common.Constant.TYPE_TRY_STICKER_KEYBOARD) {
            viewModel.loadListSticker()
        } else if ((requireActivity() as MainActivity).typeTryKeyboard == com.tapbi.spark.yokey.common.Constant.TYPE_TRY_FONT_KEYBOARD) {
            viewModel.loadListItemFont()
        }
        mainViewModel.dissmissDialogDetail.observe(viewLifecycleOwner) { result ->
            if (result) {
                App.instance.billingManager?.apply {
                    if (!this.isPremium && !Common.checkAdsIsDisable(
                            binding.frAdsNative.tag?.toString(),
                            Common.TYPE_ADS_NATIVE
                        )
                    ) {
                        binding.frAdsNative.hideShowAdsView(true)
                    }
                    mainViewModel.dissmissDialogDetail.value = false
                }

            }
        }
    }

    fun listener() {
        viewModel.listTheme.observe(
            this
        ) { value ->
            listThemeObject.clear()
            listThemeObject.addAll(value)
            if (itemsThemeAdapter != null) {
                itemsThemeAdapter?.changeObjectThemes(listThemeObject)
                binding.spinLoading.visibility = View.GONE
            }
        }

        viewModel.listSticker.observe(this) {
            listSticker.clear()
            listSticker.addAll(it)
            if (stickerAdapter != null) {
                Timber.d("duongcv");
                stickerAdapter?.changeList(listSticker)
                binding.spinLoading.visibility = View.GONE
            }
        }

        viewModel.listItemFont.observe(this) {
            listItemFont.clear()
            listItemFont.addAll(it)
            if (fontAdapter != null) {
                fontAdapter?.changeList(listItemFont)
                binding.spinLoading.visibility = View.GONE
            }
        }

        binding.imgBack.setOnClickListener {
            Timber.d("duongcv" + isShowKb);
            if (isShowKb) {
                showHideKeyboard()
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isAdded) {
                        (activity as MainActivity).onBackPressed()
                    }

                }, 250)
            } else {
                (activity as MainActivity).onBackPressed()
            }
        }

        binding.imgSend.setOnClickListener {
            val text = binding.edtInput.text
            if (text != null && !text.trim().isEmpty()) {
                binding.edtInput.setText("")
                binding.tvMessage.text = text
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enter_content),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.edtInput.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {
                checkDoubleClick()
            }
        }

        binding.viewClickText.setOnClickListener {
            if (checkDoubleClick()) {
                isShowKb = false
                if (isActivateKeyboard) {
                    binding.edtInput.isEnabled = true
                    showHideKeyboard()
                }
            }
        }

        mainViewModel.mLiveEventKeyboardShow.observe(viewLifecycleOwner) { height: Any? ->
            var heightKeyboard: Int = height as Int
            if (heightKeyboard <= com.tapbi.spark.yokey.common.Constant.HEIGHT_HIDE_KEYBOARD) heightKeyboard = 0
            isShowKb = heightKeyboard > 0
            if (heightKeyboard > 0) {
                binding.edtInput.requestFocus()
                inputMethodManager!!.showSoftInput(binding.edtInput, 0)
            }
            calculateTranslateView(heightKeyboard)
        }
    }

    private fun showHideKeyboard() {
        if (inputMethodManager != null) {
            if (!isShowKb) {
                binding.edtInput.requestFocus()
                inputMethodManager!!.showSoftInput(binding.edtInput, 0)
            } else {

                binding.edtInput.clearFocus()
                inputMethodManager!!.hideSoftInputFromWindow(binding.edtInput.windowToken, 0)
            }
        }
    }

    fun initView() {
        firstTime = true
        initTime()
        themeModel = App.instance!!.themeRepository!!.currentThemeModel
        if ((requireActivity() as MainActivity).typeTryKeyboard == com.tapbi.spark.yokey.common.Constant.TYPE_TRY_THEME_KEYBOARD) {
            val width = (DisplayUtils.getScreenWidth() / 2) - DisplayUtils.dp2px(20f)
            val height = width * 104 / 144 + DisplayUtils.dp2px(63f)
            binding.rcvThemeSticker.layoutParams.height = height * 3
            initTheme()
        } else if ((requireActivity() as MainActivity).typeTryKeyboard == com.tapbi.spark.yokey.common.Constant.TYPE_TRY_STICKER_KEYBOARD) {
            initSticker()
        } else if ((requireActivity() as MainActivity).typeTryKeyboard == com.tapbi.spark.yokey.common.Constant.TYPE_TRY_FONT_KEYBOARD) {
            initFont()
        }

        val mapId = Common.getMapIdAdmobApplovin(
            requireActivity(),
            R.array.admob_native_id_try_keyboard,
            R.array.applovin_native_id_try_keyboard
        )
        showAdsNative(binding.frAdsNative, mapId, object : OnDecorationAds {
            override fun onDecoration(network: String?) {
                binding.frAdsNative.getNativeAdView(network)?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.bg_ads_choose_theme
                    )
                )
                ((binding.frAdsNative.getTvHeadline(network)) as TextView?)?.setTextColor(Color.WHITE)
                ((binding.frAdsNative.getTvBody(network)) as TextView?)?.setTextColor(Color.WHITE)
                ((binding.frAdsNative.getTvAds(network)) as TextView?)?.setTextColor(Color.WHITE)
            }

        })

    }

    @SuppressLint("SimpleDateFormat")
    fun initTime() {
        val df: DateFormat = SimpleDateFormat("hh:mm a")
        val date: String = df.format(Calendar.getInstance().getTime())
        binding.tvTime.text = date
    }

    fun initTheme() {
        val gridLayoutManager = GridLayoutManager(context, 2)
        binding.rcvThemeSticker.setHasFixedSize(true)
        binding.rcvThemeSticker.layoutManager = gridLayoutManager
        binding.rcvThemeSticker.isMotionEventSplittingEnabled = false
        binding.rcvThemeSticker.itemAnimator = null
        itemsThemeAdapter = ItemsThemeAdapter(requireContext(), mutableListOf())
        binding.rcvThemeSticker.adapter = itemsThemeAdapter
        itemsThemeAdapter?.setItemClickListener(this)
    }

    fun initSticker() {
        stickerAdapter = StickerAdapter(
            listSticker,
            requireContext()
        )
        binding.rcvThemeSticker.layoutManager = GridLayoutManager(context, 2)
        binding.rcvThemeSticker.adapter = stickerAdapter
        stickerAdapter?.setListenerChangeItemFont(object :
            StickerAdapter.ListenerChangeItemSticker {
            override fun getItem(itemSticker: Sticker?, position: Int) {
                if (checkDoubleClick()) {
                    binding.edtInput.isEnabled = false
                    if (isActivateKeyboard && isShowKb) {
                        showHideKeyboard()
                    }
                    (activity as MainActivity).viewModel?.mLiveDataDetailObject?.value = itemSticker
                    (activity as MainActivity).changeStartScreen(R.id.detailStickerFragment, null)

                }
            }

        })
    }

    fun initFont() {
        fontAdapter = FontAdapter(listItemFont, requireContext())
        binding.rcvThemeSticker.layoutManager = GridLayoutManager(context, 2)
        binding.rcvThemeSticker.adapter = fontAdapter
        fontAdapter?.setListenerChangeItemFont(object : FontAdapter.ListenerChangeItemFont {
            override fun getItem(itemFont: ItemFont?, position: Int) {
                if (checkDoubleClick()) {
                    binding.edtInput.isEnabled = false
                    if (isActivateKeyboard && isShowKb) {
                        showHideKeyboard()
                    }
                    (activity as MainActivity).viewModel?.mLiveDataDetailObject?.value = itemFont
                    (activity as MainActivity).changeStartScreen(R.id.detailFontFragment, null)
                }
            }

        })
    }

    private val committedListCallback = Runnable {}


    override fun onDestroy() {
        super.onDestroy()
    }

    private fun calculateTranslateView(height: Int) {
        if (height > 0) {
            binding.spaceBottomScrv.layoutParams.height = height
            binding.spaceBottomScrv.requestLayout()
        } else {
            binding.edtInput.clearFocus()
            binding.spaceBottomScrv.layoutParams.height = 1
            binding.spaceBottomScrv.requestLayout()
        }
    }

    override fun onResume() {
        super.onResume()
//        if (listThemeObject.isEmpty() && listSticker.isEmpty() && listItemFont.isEmpty()) {
//            loadData()
//        }else {
//            if ((requireActivity() as MainActivity).typeTryKeyboard == Constant.TYPE_TRY_THEME_KEYBOARD && listThemeObject.size > 0) {
//                viewModel.listTheme.postValue(listThemeObject)
//            } else if ((requireActivity() as MainActivity).typeTryKeyboard == Constant.TYPE_TRY_STICKER_KEYBOARD){
//                viewModel.loadListSticker()
//            } else if ((requireActivity() as MainActivity).typeTryKeyboard == Constant.TYPE_TRY_FONT_KEYBOARD){
//                viewModel.loadListItemFont()
//            }
//        }
        App.instance?.isTryKeyboard = true

        setPreviewKeyboard()
    }

    fun setPreviewKeyboard() {
        if (themeModel == null) themeModel = App.instance.themeRepository?.currentThemeModel
        if (themeModel == null) {
            Timber.d("duongcv update theme null")
        }
        mInputView = checkActivateKeyboard(
            R.id.tryKeyboardFragment,
            isShowKb,
            binding.groupInputText,
            binding.edtInput,
            mInputView,
            binding.ctlPreviewInputView,
            null,
            null,
            true, themeModel
        )
    }

    override fun onPause() {
        App.instance?.isTryKeyboard = false
        if (UncachedInputMethodManagerUtils.isThisImeEnabled(
                requireActivity(),
                inputMethodManager
            ) && UncachedInputMethodManagerUtils.isThisImeCurrent(
                requireContext(),
                inputMethodManager
            )
        ) {
            if (isShowKb) {
                showHideKeyboard()
                calculateTranslateView(0)
            }
        }
        super.onPause()
    }


    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageReceived(event: MessageEvent) {
        when (event.key) {
            com.tapbi.spark.yokey.util.Constant.EVENT_SEND_STICKER -> {
                Glide.with(requireContext()).asBitmap().load(event.dataString)
                    .into(binding.imgSticker)
            }

            com.tapbi.spark.yokey.util.Constant.ACTION_CHANGE_STATE_ACTIVATE_KEYBOARD -> {
                setPreviewKeyboard()
                if (!isActivateKeyboard && isShowKb) {
                    showHideKeyboard()
                }
            }

            com.tapbi.spark.yokey.util.Constant.EVENT_UPDATE_THEME_KEYBOARD -> {
                updateThemeKeyboard()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateThemeKeyboard() {
        Timber.d("duongcv");
        loadData()
        setPreviewKeyboard()
    }

    override fun onItemClickRecycleView(view: View?, position: Int, oldPosition: Int) {
        if (checkDoubleClick() && position != RecyclerView.NO_POSITION) {
            binding.edtInput.isEnabled = false
            if (isActivateKeyboard && isShowKb) {
                showHideKeyboard()
            }
            (activity as MainActivity).viewModel?.mLiveDataDetailObject?.value = listThemeObject.get(position)
            (activity as MainActivity).changeStartScreen(R.id.detailThemeFragment, null)

        }
    }

    override fun processRemoveAds(isRemoveAds: Boolean) {
        super.processRemoveAds(isRemoveAds)
        binding.frAdsNative.visibility = if (isRemoveAds) View.GONE else View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(
            com.tapbi.spark.yokey.common.Constant.STATE_TRY_KEYBOARD,
            (requireActivity() as MainActivity).typeTryKeyboard
        )
        super.onSaveInstanceState(outState)
    }


}