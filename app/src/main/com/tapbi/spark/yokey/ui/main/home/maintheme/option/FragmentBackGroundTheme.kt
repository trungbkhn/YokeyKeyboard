package com.tapbi.spark.yokey.ui.main.home.maintheme.option

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.common.Constant.ID_THEME_BACKGROUND
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.model.PaginationTheme
import com.tapbi.spark.yokey.data.model.ThemeObject
import com.tapbi.spark.yokey.data.model.ThemeObjectList
import com.tapbi.spark.yokey.data.remote.ApiUtils
import com.tapbi.spark.yokey.data.remote.ThemesService
import com.tapbi.spark.yokey.databinding.FragmentBackgroundThemeItemBinding
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

class FragmentBackGroundTheme :
    BaseBindingFragment<FragmentBackgroundThemeItemBinding, ThemeViewModel>(),
    ItemsThemeAdapter.ItemClickListener {
    private var objectThemeList: List<ThemeObject>? = null
    private lateinit var itemsThemeAdapter: ItemsThemeAdapter
    private var themesService: ThemesService? = null
    private var currentPosition = 0
    private var oldPosition = 0
    override fun getViewModel(): Class<ThemeViewModel> = ThemeViewModel::class.java

    override val layoutId: Int
        get() = R.layout.fragment_background_theme_item

    override fun onCreate(savedInstanceState: Bundle?) {
        objectThemeList = ArrayList()
        // themesService = ApiUtils.getBackgroundThemesService()
        themesService = ApiUtils.getThemesService()
        super.onCreate(savedInstanceState)
    }

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        setUpAdapterBackGround()
    }

    override fun onPermissionGranted() {
    }


    private fun setUpAdapterBackGround() {
        itemsThemeAdapter = ItemsThemeAdapter(
            requireContext(),
            mutableListOf()
        )
        binding.recyclerViewThemesBackground.adapter = itemsThemeAdapter
        binding.recyclerViewThemesBackground.itemAnimator = null
        binding.recyclerViewThemesBackground.layoutManager = GridLayoutManager(requireContext(), 2)
        itemsThemeAdapter.setItemClickListener(this)
    }

    override fun onItemClickRecycleView(view: View?, position: Int, oldPosition: Int) {
        if (checkDoubleClick()) {
            val objectTheme = objectThemeList!![position]
            mainViewModel.mLiveDataDetailObject.value = objectTheme
            showAdsFull(getString(R.string.tag_inter_item_theme))
            // Timber.d("onItemClickRecycleView "+objectTheme.preview)
            currentPosition = position
            this.oldPosition = oldPosition
        }
    }

    private fun loadData() {
        if (!(App.instance.mPrefs?.getBoolean(
                com.tapbi.spark.yokey.common.Constant.SAVE_THEME_BACKGROUND,
                false
            ) ?: false)
        ) {
            themesService!!.getListLEDThemes(
                PaginationTheme(
                    0,
                    ID_THEME_BACKGROUND,
                    0,
                    0,
                    App.instance.mPrefs?.getInt(
                        com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION,
                        1
                    ) ?: 1
                )
            )
                .enqueue(object : Callback<ThemeObjectList> {
                    override fun onResponse(
                        call: Call<ThemeObjectList>,
                        response: Response<ThemeObjectList>
                    ) {

                        if (response.isSuccessful) {
                            val listObject = ArrayList<ThemeObject>()
                            response.body()!!.items?.let { listObject.addAll(it) }
                            Timber.e("hachung listObject1 ${listObject.size}")
                            App.instance.themeRepository?.listThemeBackgroundNew?.let {
                                listObject.addAll(it)
                            }
                            Timber.e("hachung listObject2 ${listObject.size}")
                            objectThemeList = listObject
                            itemsThemeAdapter.setObjectThemes(objectThemeList)
                            if (objectThemeList != null) {
                                App.instance.themeRepository?.addThemeTryKeyboardThread(
                                    objectThemeList
                                )
                                App.instance.themeRepository?.addAllThemeDB(objectThemeList)
                                    ?.subscribe()
                                App.instance.mPrefs?.edit()?.putBoolean(
                                    com.tapbi.spark.yokey.common.Constant.SAVE_THEME_BACKGROUND,
                                    true
                                )?.apply()
                                response.body()!!.lastVersion?.let {
                                    if (it > (App.instance.mPrefs?.getInt(
                                            com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION,
                                            1
                                        ) ?: 1)
                                    ) {
                                        App.instance.mPrefs?.edit()?.putInt(
                                            com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION,
                                            it
                                        )?.apply()
                                    }
                                }
                            }
                            binding.spinKitBackground.visibility = View.GONE
                        }
                    }

                    override fun onFailure(call: Call<ThemeObjectList>, t: Throwable) {
                        //Timber.d("ducNQloadData error");
                        t.printStackTrace()
                        if (isAdded)
                            if (App.instance.connectivityStatus != -1) CommonUtil.customToast(
                                requireContext(),
                                requireContext().resources.getString(R.string.text_check_internet)
                            )
                    }
                })
        } else {
            App.instance.themeRepository?.loadAllThemeByCategory(com.tapbi.spark.yokey.common.Constant.ID_THEME_BACKGROUND.toString())
                ?.subscribe(object :
                    SingleObserver<MutableList<ThemeEntity>> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                    }

                    override fun onSuccess(t: MutableList<ThemeEntity>) {
                        val listObject = ArrayList<ThemeObject>()
                        for (i in t) {
                            listObject.add(
                                App.instance.themeRepository!!.convertThemeEntityToThemeObject(
                                    i
                                )
                            )
                        }
                        Timber.e("hachung listObject3 ${listObject.size}")
                        App.instance.themeRepository?.listThemeBackgroundNew?.let {
                            listObject.addAll(it)
                        }
                        Timber.e("hachung listObject4 ${listObject.size}")
                        objectThemeList = listObject
                        if (objectThemeList != null) {
                            itemsThemeAdapter.setObjectThemes(objectThemeList!!)
                            if (objectThemeList != null) {
                                App.instance.themeRepository?.addThemeTryKeyboardThread(
                                    objectThemeList
                                )
                            }
                            binding.spinKitBackground.visibility = View.GONE
                        }
                    }

                })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        if (objectThemeList == null) objectThemeList = ArrayList()
        if (objectThemeList!!.isEmpty()) {
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