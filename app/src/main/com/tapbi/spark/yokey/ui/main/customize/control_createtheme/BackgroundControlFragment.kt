package com.tapbi.spark.yokey.ui.main.customize.control_createtheme

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentBackgroundControlBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.common.Constant.CHECK_CROP
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.model.theme.ThemeModel
import com.tapbi.spark.objects.BackgroundList
import com.tapbi.spark.yokey.ui.adapter.BackgroundAdapter
import com.tapbi.spark.yokey.ui.adapter.BackgroundControlAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.ui.main.customize.CreateThemeFragment
import com.tapbi.spark.yokey.ui.main.customize.CreateThemeViewModel
import com.tapbi.spark.yokey.util.ClickUtil
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.Constant.PATH_CURRENT_BG
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class BackgroundControlFragment :
    BaseBindingFragment<FragmentBackgroundControlBinding, CreateThemeViewModel>() {

    private lateinit var backgroundControlAdapter: BackgroundControlAdapter
    private var listBackgroundList = ArrayList<BackgroundList>()
    private var themeModel: ThemeModel? = null
    private var pathBgCurrent = ""
    private var idBackground = Constant.ID_STORE


    override fun getViewModel(): Class<CreateThemeViewModel> {
        return CreateThemeViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_background_control

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
    }

    override fun onPermissionGranted() {
    }

    companion object {
        fun newInstance(themeModel: ThemeModel): BackgroundControlFragment {
            val args = Bundle()
            args.putParcelable(Constant.DATA_THEMEMODEL, themeModel)
            val fragment = BackgroundControlFragment()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBackgroundControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) themeModel = arguments?.getParcelable(Constant.DATA_THEMEMODEL)
        EventBus.getDefault().register(this)
        event()
        initRclBackground()
        listener()
        loadData()

        // changeBgBtnStore()
        if (savedInstanceState != null) {
            pathBgCurrent = savedInstanceState.getString(PATH_CURRENT_BG)!!
            idBackground = savedInstanceState.getInt("key_store")
            if (idBackground == Constant.ID_STORE) {
                changeBgBtnStore()
                binding.btnGallery.checkShowGradient(false)
                binding.btnStore.checkShowGradient(true)
                backgroundControlAdapter.changePath(pathBgCurrent)
            } else if (idBackground == Constant.ID_GALLERY) {
                binding.btnGallery.checkShowGradient(true)
                binding.btnStore.checkShowGradient(false)
                changeBgButtonGallery()
                backgroundControlAdapter?.apply {
                    changePath("")
                }
            }
        } else {
            changeBgBtnStore()
        }
        // setUpView()
    }


    private fun event() {
        if (themeModel != null) {
            binding.seekbarBlur.progress = themeModel!!.background!!.radiusBlur
            binding.tvProgress.text = (binding.seekbarBlur.progress / 0.24).toInt().toString()
            if (binding.seekbarBlur.progress != 0){
                updateBlurBackground()
            }

        }
        binding.seekbarBlur.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {
                binding.tvProgress.text = (binding.seekbarBlur.progress / 0.24).toInt().toString()
                updateBlurBackground()
            }

        })
    }

    private fun updateBlurBackground(){
        val bundle = Bundle()
        bundle.putInt(
            Constant.DATA_CHANGE_BLUR_BACKGROUND_CUSTOMZIE,
            binding.seekbarBlur.progress
        )
        themeModel?.background?.radiusBlur = binding.seekbarBlur.progress
        EventBus.getDefault().post(
            MessageEvent(
                Constant.ACTION_CHANGE_BLUR_BACKGROUND_CUSTOMZIE,
                bundle
            )
        )
    }

    private fun loadData() {
        viewModel.loadDataBackground()
    }

    private fun listener() {
        viewModel.liveDataListBackground.observe(
            viewLifecycleOwner
        ) { listBackground ->
            if (listBackground != null) {
                binding.spinKitLoadBackround.visibility = View.GONE
                backgroundControlAdapter.changeList(listBackground)
            }
        }
        mainViewModel.mLiveEventKeyboardShow.observe(
            viewLifecycleOwner
        ) { height ->
            if (height != null) {
                isShowKeyboard = height > 0
                if (isShowKeyboard) {
                    binding.groupBlur.visibility = View.VISIBLE
                    if (themeModel != null) {
                        binding.seekbarBlur.progress = themeModel!!.background!!.radiusBlur
                        binding.tvProgress.text =
                            (binding.seekbarBlur.progress / 0.24).toInt().toString()
                    }
                } else {
                    binding.groupBlur.visibility = View.GONE
                }
            }
        }
        mainViewModel.mLiveEventCheckGradient.observe(viewLifecycleOwner) {
            if (it == CHECK_CROP) {
                binding.btnGallery.checkShowGradient(true)
                binding.btnStore.checkShowGradient(false)
                changeBgButtonGallery()
                idBackground = Constant.ID_GALLERY
                pathBgCurrent = ""
                backgroundControlAdapter.apply {
                    changePath("")
                }
            }
        }

        binding.btnGallery.setOnClickListener {
            if (!ClickUtil.checkTime()) {
                return@setOnClickListener
            }
            try {
                (activity as MainActivity).changeOpenGallery()
            } catch (e: Exception) {
            }
        }
        binding.btnStore.setOnClickListener {
            changeBgBtnStore()
            idBackground = Constant.ID_STORE
            binding.btnGallery.checkShowGradient(false)
            binding.btnStore.checkShowGradient(true)
        }
    }

    private fun changeBgButtonGallery() {
        binding.btnStore.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_icon_store_fill,
            0,
            0,
            0
        )
        binding.btnGallery.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_icon_gallery_choose,
            0,
            0,
            0
        )
        binding.btnGallery.setTextColor(Color.WHITE)
        binding.btnStore.setTextColor(Color.parseColor("#BDBEDB"))
    }

    override fun onResume() {
        super.onResume()
    }

    private fun changeBgBtnStore() {
        binding.btnStore.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_icon_store,
            0,
            0,
            0
        )
        binding.btnGallery.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_icon_photo,
            0,
            0,
            0
        )
        binding.btnStore.setTextColor(Color.WHITE)
        binding.btnGallery.setTextColor(Color.parseColor("#BDBEDB"))
    }


    private fun initRclBackground() {
        backgroundControlAdapter = BackgroundControlAdapter(
            listBackgroundList,
            requireContext(),
            viewLifecycleOwner,
            object : BackgroundAdapter.ListenerBackground {
                @SuppressLint("NotifyDataSetChanged")
                override fun getBackground(path: String, countDownload : Int) {
                    CreateThemeFragment.countDownloadBackground = countDownload
                    if (pathBgCurrent != path) {
                        pathBgCurrent = path
                        val bundle = Bundle()
                        instance.linkCurrentBg = path//fix bug kill app lose background
                        bundle.putString(Constant.DATA_CHANGE_BACKGROUND_CUSTOMZIE, path)
                        if (instance.connectivityStatus != -1) {
                            if (pathBgCurrent.length > 5) {
                                val checkNumber: String = pathBgCurrent.substring(
                                    pathBgCurrent.length - 4,
                                    pathBgCurrent.length
                                )
                                if (isLong(checkNumber)) {
                                    instance.idPath = checkNumber
                                    val root =
                                        instance.appDir.toString() + Constant.PATH_FILE_DOWNLOADED_BACKGROUND + instance.idPath + "a.jpg"
                                    val folder = File(root)
                                    if (!folder.exists() && !path.contains(
                                            Constant.FOLDER_ASSET,
                                            true
                                        )
                                    ) {
                                        writeBmLoaded(path)
                                    }
                                }
                            }
                        }
                        EventBus.getDefault()
                            .post(MessageEvent(Constant.ACTION_CHANGE_BACKGROUND_CUSTOMZIE, bundle))
                        backgroundControlAdapter.changePath(pathBgCurrent)
                        binding.btnGallery.checkShowGradient(false)
                        binding.btnStore.checkShowGradient(true)
                        changeBgBtnStore()
                        idBackground = Constant.ID_STORE
                        pathBgCurrent = path
                    }
                }

                override fun checkPermission() {

                }

            })
        val linearManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rclBackgroundControl.layoutManager = linearManager
        binding.rclBackgroundControl.adapter = backgroundControlAdapter
        binding.rclBackgroundControl.itemAnimator = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(PATH_CURRENT_BG, pathBgCurrent)
        outState.putInt("key_store", idBackground)
        super.onSaveInstanceState(outState)
    }

    fun writeBmLoaded(path: String) {
        Glide.with(requireContext()).asBitmap().load(path).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                viewModel.writeBmToFolder(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }
        })

    }

    fun isLong(s: String): Boolean {
        return try {
            val i = s.toLong()
            true
        } catch (er: NumberFormatException) {
            false
        }
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEventBus(eventMessage: MessageEvent) {
        when (eventMessage.key) {
            Constant.CONNECT_INTERNET -> {
                if (App.instance.checkConnectivityStatus() != -1 && listBackgroundList.size > 0 && listBackgroundList.get(0).backgroundJson.size == 0){
                    loadData()
                }
            }

        }
    }

}