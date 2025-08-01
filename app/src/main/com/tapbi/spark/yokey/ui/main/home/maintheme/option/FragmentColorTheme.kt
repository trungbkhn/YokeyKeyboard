package com.tapbi.spark.yokey.ui.main.home.maintheme.option

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentColorThemeItemBinding
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant.ID_THEME_COLOR
import com.tapbi.spark.yokey.common.Constant.ID_THEME_GRADIENT
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.model.PaginationTheme
import com.tapbi.spark.yokey.data.model.ThemeObject
import com.tapbi.spark.yokey.data.model.ThemeObjectList
import com.tapbi.spark.yokey.data.remote.ApiUtils
import com.tapbi.spark.yokey.data.remote.ThemesService
import com.tapbi.spark.yokey.ui.adapter.ItemsThemeAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.main.home.maintheme.ThemeViewModel
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import kotlin.collections.ArrayList

class FragmentColorTheme : BaseBindingFragment<FragmentColorThemeItemBinding, ThemeViewModel>(),
        ItemsThemeAdapter.ItemClickListener {
    private var objectThemeList: MutableList<ThemeObject>? = null
    private lateinit var itemsThemeAdapter: ItemsThemeAdapter
    private var themesService: ThemesService? = null
    private var themesServiceGradient: ThemesService? = null
    private var currentPosition = 0
    private var oldPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        objectThemeList = mutableListOf()
        //themesService = ApiUtils.getColorThemesService()
        themesService = ApiUtils.getThemesService()
        themesServiceGradient = ApiUtils.getGradientThemesService()
        super.onCreate(savedInstanceState)
    }

    override fun getViewModel(): Class<ThemeViewModel> = ThemeViewModel::class.java

    override val layoutId: Int
        get() = R.layout.fragment_color_theme_item

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        setUpAdapter()
    }

    override fun onPermissionGranted() {}

    private fun setUpAdapter() {
        itemsThemeAdapter = ItemsThemeAdapter(requireContext(), mutableListOf())
        binding.recyclerViewThemesColor.adapter = itemsThemeAdapter
        binding.recyclerViewThemesColor.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewThemesColor.itemAnimator = null
        itemsThemeAdapter.setItemClickListener(this)
    }

    private fun loadData() {

        if (!(App.instance.mPrefs?.getBoolean(com.tapbi.spark.yokey.common.Constant.SAVE_THEME_COLOR, false) ?: false) ) {
            Log.d("duongcv", "loadData: color server")
            themesService!!.getListLEDThemes(
                PaginationTheme(0, ID_THEME_COLOR, 0, 0,App.instance.mPrefs?.getInt(
                    com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION, 1) ?: 1)
            )
                .enqueue(object : Callback<ThemeObjectList> {
                    override fun onResponse(
                        call: Call<ThemeObjectList>,
                        response: Response<ThemeObjectList>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.items?.let { objectThemeList?.addAll(it) }

                            getListGradient()
                            App.instance.mPrefs?.edit()?.putBoolean(com.tapbi.spark.yokey.common.Constant.SAVE_THEME_COLOR, true)?.apply()
                            response.body()!!.lastVersion?.let {
                                if (it > (App.instance.mPrefs?.getInt(com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION, 1) ?: 1)) {
                                    App.instance.mPrefs?.edit()?.putInt(com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION, it)?.apply()
                                }
                            }
                            binding.spinKitColor.visibility = View.GONE
                        }
                    }

                    override fun onFailure(call: Call<ThemeObjectList>, t: Throwable) {
                        t.printStackTrace()
                        if (isAdded)
                            if (App.instance.connectivityStatus != -1) CommonUtil.customToast(
                                requireContext(),
                                requireContext().resources.getString(R.string.text_check_internet)
                            )
                    }
                })
        }else {
            Log.d("duongcv", "loadData: color local")
            App.instance.themeRepository?.loadAllThemeByCategory(com.tapbi.spark.yokey.common.Constant.ID_THEME_COLOR.toString())?.subscribe(object : SingleObserver<MutableList<ThemeEntity>>{
                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                    if (isAdded)
                        if (App.instance.connectivityStatus != -1) CommonUtil.customToast(
                            requireContext(),
                            requireContext().resources.getString(R.string.text_check_internet)
                        )
                }

                override fun onSuccess(t: MutableList<ThemeEntity>) {
                    val listObject = ArrayList<ThemeObject>()
                    for (i in t){
                        listObject.add(App.instance.themeRepository!!.convertThemeEntityToThemeObject(i))
                    }

                    objectThemeList?.addAll(listObject)
                    getListGradient()
                }

            })
        }
    }

    private fun getListGradient() {
        if (!(App.instance.mPrefs?.getBoolean(com.tapbi.spark.yokey.common.Constant.SAVE_THEME_GRADIENT, false) ?: false) ) {
            Log.d("duongcv", "getListGradient: gradient server")
            themesService!!.getListLEDThemes(
                PaginationTheme(0, ID_THEME_GRADIENT, 0, 0, App.instance.mPrefs?.getInt(
                    com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION, 1) ?: 1)
            )
                .enqueue(object : Callback<ThemeObjectList> {
                    override fun onResponse(
                        call: Call<ThemeObjectList>,
                        response: Response<ThemeObjectList>
                    ) {
                        if (response.isSuccessful) {
                            response.body()!!.items?.let { objectThemeList?.addAll(it) }
                            if (objectThemeList != null) {
                                Timber.d("ducNQ onResponsed: " + objectThemeList!!.size)
                                App.instance.themeRepository?.addAllThemeDB(objectThemeList)?.subscribe()
                                App.instance.mPrefs?.edit()?.putBoolean(com.tapbi.spark.yokey.common.Constant.SAVE_THEME_GRADIENT, true)?.apply()
                                response.body()!!.lastVersion?.let {
                                    if (it > (App.instance.mPrefs?.getInt(com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION, 1) ?: 1)) {
                                        App.instance.mPrefs?.edit()?.putInt(com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION, it)?.apply()
                                    }
                                }
                                App.instance.themeRepository?.listThemeColorNew?.let {
                                    objectThemeList?.addAll(it)
                                }
                                itemsThemeAdapter.setObjectThemes(objectThemeList!!)
                                if (objectThemeList != null) {
                                    App.instance.themeRepository?.addThemeTryKeyboardThread(
                                        objectThemeList
                                    )
                                }
                                binding.spinKitColor.visibility = View.GONE
                            }
                        }
                    }

                    override fun onFailure(call: Call<ThemeObjectList>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
        }else {
            Log.d("duongcv", "getListGradient: gradient local")
            App.instance.themeRepository?.loadAllThemeByCategory(com.tapbi.spark.yokey.common.Constant.ID_THEME_GRADIENT.toString())?.subscribe(object : SingleObserver<MutableList<ThemeEntity>>{
                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                }

                override fun onSuccess(t: MutableList<ThemeEntity>) {
                    val listObject = ArrayList<ThemeObject>()
                    for (i in t){
                        listObject.add(App.instance.themeRepository!!.convertThemeEntityToThemeObject(i))
                    }
                    objectThemeList?.addAll(listObject)
                    App.instance.themeRepository?.listThemeColorNew?.let {
                        objectThemeList?.addAll(it)
                    }
                    if (objectThemeList != null) {
                        itemsThemeAdapter.setObjectThemes(objectThemeList!!)
                        if (objectThemeList != null) {
                            App.instance.themeRepository?.addThemeTryKeyboardThread(
                                objectThemeList
                            )
                        }
                        binding.spinKitColor.visibility = View.GONE
                    }
                }

            })
        }
    }

    override fun onItemClickRecycleView(view: View?, position: Int, oldPosition: Int) {
        if (checkDoubleClick()) {
            Timber.e("hachung position: $position")
            val objectTheme = objectThemeList!![position]
            currentPosition = position
            this.oldPosition = oldPosition
            Timber.e("hachung onItemClickRecycleView " + objectTheme.id)
            mainViewModel.mLiveDataDetailObject.value = objectTheme
            showAdsFull(getString(R.string.tag_inter_item_theme))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        if (objectThemeList == null) objectThemeList = ArrayList()
        if (objectThemeList!!.size == 0) {
            loadData()
        } else {
            itemsThemeAdapter.notifyDataSetChanged()
        }
        EventBus.getDefault().register(this)
        super.onResume()
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(messageEvent: MessageEvent) {
        val key = messageEvent.key
        if (key == Constant.KEY_CHANGE_THEME || key == Constant.KEY_CHANGE_THEME_NOT_SHOW_PREVIEW) {
            itemsThemeAdapter.notifyDataSetChanged()
        } else if (key == Constant.CONNECT_INTERNET) {
            loadData()
        }
    }
}