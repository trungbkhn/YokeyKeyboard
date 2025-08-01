package com.tapbi.spark.yokey.ui.main.home.maintheme.option

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentHotThemeItemBinding
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant.ID_THEME_HOT
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


class FragmentHotTheme : BaseBindingFragment<FragmentHotThemeItemBinding, ThemeViewModel>(),
        ItemsThemeAdapter.ItemClickListener {
    private var objectThemeList: List<ThemeObject>? = null
    private lateinit var itemsThemeAdapter: ItemsThemeAdapter /*by lazy {
        ItemsThemeAdapter(
            requireContext(),
                objectThemeList as MutableList<ThemeObject>?
        )
    }*/

    // var dyCurrent = 0
    private var themesService: ThemesService? = null
    private var currentPosition = 0
    private var oldPosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        objectThemeList = ArrayList()
        // themesService = ApiUtils.getHotThemesService()
        themesService = ApiUtils.getThemesService()
        super.onCreate(savedInstanceState)
    }

    override fun getViewModel(): Class<ThemeViewModel> = ThemeViewModel::class.java

    override val layoutId: Int get() = R.layout.fragment_hot_theme_item

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        setUpAdapter()
    }

    override fun onPermissionGranted() {

    }

    private val TAG = "FragmentHotTheme"
    private fun setUpAdapter() {
        itemsThemeAdapter = ItemsThemeAdapter(
                requireContext(),
                mutableListOf()
        )
        binding.recyclerViewThemesHot.adapter = itemsThemeAdapter
        binding.recyclerViewThemesHot.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewThemesHot.itemAnimator = null
        itemsThemeAdapter.setItemClickListener(this)
    }

    override fun onItemClickRecycleView(view: View?, position: Int, oldPosition: Int) {
        if(checkDoubleClick() && (objectThemeList?.size ?: -1) > position) {
            val objectTheme = objectThemeList!![position]
            mainViewModel.mLiveDataDetailObject.value = objectTheme
            showAdsFull(getString(R.string.tag_inter_item_theme))

            currentPosition = position
            this.oldPosition = oldPosition
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

    private fun updateUi(listThemeObject : List<ThemeObject>?) {
        objectThemeList = listThemeObject
        if (objectThemeList != null) {
            App.instance.themeRepository?.addThemeTryKeyboardThread(
                objectThemeList
            )

        }
        itemsThemeAdapter.setObjectThemes(objectThemeList)

        binding.spinKitHot.visibility = View.GONE
    }

    private fun loadData() {

        if (!(App.instance.mPrefs?.getBoolean(com.tapbi.spark.yokey.common.Constant.SAVE_THEME_HOT, false) ?: false) ) {
            Log.d("duongcv", "loadData: server hot")
            themesService!!.getListLEDThemes(
                PaginationTheme(0, 0, ID_THEME_HOT, 0, App.instance.mPrefs?.getInt(
                    com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION, 1) ?: 1)
            )
                .enqueue(object : Callback<ThemeObjectList> {
                    override fun onResponse(
                        call: Call<ThemeObjectList>,
                        response: Response<ThemeObjectList>
                    ) {
                        if (response.isSuccessful) {
                            objectThemeList = response.body()!!.items
                            updateUi(objectThemeList)
                            if (objectThemeList != null) {
                                App.instance.themeRepository?.addAllThemeDB(objectThemeList!!)?.subscribe()
                                App.instance.mPrefs?.edit()?.putBoolean(com.tapbi.spark.yokey.common.Constant.SAVE_THEME_HOT, true)?.apply()
                                response.body()!!.lastVersion?.let {
                                    if (it > (App.instance.mPrefs?.getInt(com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION, 1) ?: 1)) {
                                        App.instance.mPrefs?.edit()?.putInt(com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION, it)?.apply()
                                    }
                                }
                            }
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
        }else {
            Log.d("duongcv", "loadData: local hot")
            App.instance.themeRepository?.loadAllThemeByIsHotTheme(1)?.subscribe(object : SingleObserver<MutableList<ThemeEntity>> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: MutableList<ThemeEntity>) {
                    val themeObjectList = ArrayList<ThemeObject>()
                    for(i in t) {
                        themeObjectList.add(App.instance.themeRepository!!.convertThemeEntityToThemeObject(i))
                    }
                    updateUi(themeObjectList)
                }

                override fun onError(e: Throwable) {
                    if (isAdded)
                        if (App.instance.connectivityStatus != -1) CommonUtil.customToast(
                            requireContext(),
                            requireContext().resources.getString(R.string.text_check_internet)
                        )
                }

            })
        }
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }
}