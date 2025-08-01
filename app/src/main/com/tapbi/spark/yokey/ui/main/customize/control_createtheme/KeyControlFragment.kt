package com.tapbi.spark.yokey.ui.main.customize.control_createtheme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentKeyControlBinding
import com.google.android.material.tabs.TabLayout
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.model.PopularButton
import com.tapbi.spark.yokey.data.model.SimpleButton
import com.tapbi.spark.yokey.data.model.theme.ThemeModel
import com.tapbi.spark.yokey.ui.adapter.ChooseColorAdapter
import com.tapbi.spark.yokey.ui.adapter.PopularButtonAdapter
import com.tapbi.spark.yokey.ui.adapter.SimpleButtonAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.main.customize.CreateThemeViewModel
import com.tapbi.spark.yokey.util.Constant
import org.greenrobot.eventbus.EventBus
import java.util.*

class KeyControlFragment : BaseBindingFragment<FragmentKeyControlBinding, CreateThemeViewModel>() {

    lateinit var listColor: ArrayList<String>
    lateinit var chooseColorAdapter: ChooseColorAdapter
    lateinit var listPopularButton: ArrayList<PopularButton>
    lateinit var popularButtonAdapter: PopularButtonAdapter
    var themeModel: ThemeModel? = null
    lateinit var listSimpleButton: ArrayList<SimpleButton>
    lateinit var simpleButtonAdapter: SimpleButtonAdapter

    override fun getViewModel(): Class<CreateThemeViewModel> {
        return CreateThemeViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_key_control

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
    }

    override fun onPermissionGranted() {
    }

    companion object {
        fun newInstance(themeModel: ThemeModel): KeyControlFragment {
            val args = Bundle()
            args.putParcelable(Constant.DATA_THEMEMODEL, themeModel)
            val fragment = KeyControlFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentKeyControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) themeModel =
            savedInstanceState.getParcelable(Constant.DATA_THEMEMODEL)!!
        themeModel = arguments?.getParcelable(Constant.DATA_THEMEMODEL)!!
        if (themeModel == null) themeModel = App.instance.themeRepository?.defaultThemeModel
        if (themeModel != null) {
            initTabLayoutTextColor()
            initListSimpleButton()
            initListColor()
            initListPopularButton()
        }
    }

    private fun initListSimpleButton() {
        listSimpleButton = ArrayList()
        simpleButtonAdapter = SimpleButtonAdapter(
            listSimpleButton,
            requireContext(),
            themeModel!!.typeKey,
            object : SimpleButtonAdapter.ChangeSimpleButton {
                override fun changeSimpleButton(type: Int) {
                    App.instance.themeRepository!!.clearDrawableKeyDefault()
                    themeModel!!.typeKey = type
                    popularButtonAdapter.changePopularButton(type.toString())
                    val bundle = Bundle()
                  //  Timber.d("changeSimpleButton "+ App.instance.typeKey)
                    bundle.putInt(Constant.DATA_CHANGE_TYPE_KEY_CUSTOMZIE, type)
                    EventBus.getDefault()
                        .post(MessageEvent(Constant.ACTION_CHANGE_TYPE_KEY_CUSTOMZIE, bundle))
                }
            })
        binding.rclSimpleButton.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rclSimpleButton.adapter = simpleButtonAdapter

        viewModel.liveDataListSimpleButton.observe(viewLifecycleOwner,
            { listSimple ->
                listSimpleButton = listSimple!!
                simpleButtonAdapter.changeList(listSimpleButton)
            })
        viewModel.loadDataSimpleButton()
    }

    private fun initListPopularButton() {
        listPopularButton = ArrayList()
        popularButtonAdapter = PopularButtonAdapter(
            listPopularButton,
            requireContext(),
            themeModel!!.typeKey.toString(),
            object : PopularButtonAdapter.ListenerChangePopularButton {
                override fun changePopular(popular: String) {
                    App.instance.themeRepository!!.clearDrawableKeyDefault()
                    themeModel!!.typeKey = popular.toInt()
                    simpleButtonAdapter.changeId(popular.toInt())
                    val bundle = Bundle()
                    App.instance.typeKey = popular.toInt()
                    bundle.putInt(Constant.DATA_CHANGE_TYPE_KEY_CUSTOMZIE, themeModel!!.typeKey)
                    EventBus.getDefault()
                        .post(MessageEvent(Constant.ACTION_CHANGE_TYPE_KEY_CUSTOMZIE, bundle))
                }
            })
        val gridLayoutManager = GridLayoutManager(requireContext(), 4)
        binding.rclPopularButton.layoutManager = gridLayoutManager
        binding.rclPopularButton.adapter = popularButtonAdapter
        viewModel.liveDataListPopular.observe(viewLifecycleOwner
        ) { listPopular ->
            if (listPopular != null && listPopular.size > 0) listPopularButton.clear()
            listPopularButton.addAll(listPopular!!)
            popularButtonAdapter.changeList(listPopularButton)
        }
        viewModel.loadListPopular()

    }

    private fun initTabLayoutTextColor() {

        binding.tablayoutTextColor.post {
            binding.tablayoutTextColor.addTab(
                binding.tablayoutTextColor.newTab()
                    .setText(requireContext().resources.getString(R.string.text_color)), 0
            )
            binding.tablayoutTextColor.addTab(
                binding.tablayoutTextColor.newTab()
                    .setText(requireContext().resources.getString(R.string.toolbar_color)), 1
            )
            for (i in 0 until  binding.tablayoutTextColor.tabCount) {
                binding.tablayoutTextColor.getTabAt(i).let {
                    TooltipCompat.setTooltipText(
                        it!!.view,
                        null
                    )
                }
            }
        }

        binding.tablayoutTextColor.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                for (i in 0 until  binding.tablayoutTextColor.tabCount) {
                  binding.tablayoutTextColor.getTabAt(i).let {
                        TooltipCompat.setTooltipText(
                                it!!.view,
                                null
                            )
                    }
                }
                if (tab != null) {
                    var color = themeModel!!.key!!.text!!.textColor!!
                    if (tab.position != 0) {
                        color = themeModel!!.menuBar!!.iconColor!!
                    }
                    chooseColorAdapter.changeColorCurrent("#" + color.substring(color.length - 6))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
    private fun initListColor() {
        listColor = ArrayList()
        chooseColorAdapter = ChooseColorAdapter(
            listColor,
            requireContext(),
            object : ChooseColorAdapter.ListenerChooseColor {
                override fun chooseColor(color: String) {
                    val formatColor = "0xFF" + color.substring(1)
                    val bundle = Bundle()
                    if (binding.tablayoutTextColor.selectedTabPosition == 0) {
                        themeModel!!.key!!.text!!.textColor = formatColor
                        bundle.putString(
                            Constant.DATA_CHANGE_COLOR_ICON_TEXT_CUSTOMZIE,
                            formatColor
                        )
                        EventBus.getDefault().post(
                            MessageEvent(
                                Constant.ACTION_CHANGE_COLOR_ICON_TEXT_CUSTOMZIE,
                                bundle
                            )
                        )
                    } else {
                        themeModel!!.menuBar!!.iconColor = formatColor
                        bundle.putString(
                            Constant.DATA_CHANGE_COLOR_ICON_MENU_CUSTOMZIE,
                            formatColor
                        )
                        EventBus.getDefault().post(
                            MessageEvent(
                                Constant.ACTION_CHANGE_COLOR_ICON_MENU_CUSTOMZIE,
                                bundle
                            )
                        )
                    }
                }
            })
        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rclColor.layoutManager = linearLayoutManager
        binding.rclColor.adapter = chooseColorAdapter

        viewModel.liveDataListColor.observe(
            viewLifecycleOwner
        ) { lColor ->
            if (lColor != null && lColor.size > 0) {
                listColor.clear()
                listColor.addAll(lColor)
                chooseColorAdapter.changeListColor(listColor)
            }
        }

        viewModel.loadListColorThread()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(Constant.DATA_THEMEMODEL, themeModel)
        super.onSaveInstanceState(outState)
    }


}