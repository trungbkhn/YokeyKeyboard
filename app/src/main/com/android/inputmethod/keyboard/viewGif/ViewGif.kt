package com.android.inputmethod.keyboard.viewGif

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.ViewGifBinding
import com.android.inputmethod.keyboard.KeyboardSwitcher
import com.android.inputmethod.keyboard.emoji.EmojiPalettesView
import com.giphy.sdk.core.models.Image
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.GPHSettings
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.GiphyLoadingProvider
import com.giphy.sdk.ui.pagination.GPHContent
import com.giphy.sdk.ui.themes.GPHTheme
import com.giphy.sdk.ui.views.GPHGridCallback
import com.giphy.sdk.ui.views.GPHSearchGridCallback
import com.giphy.sdk.ui.views.GifView
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant.GPHY_EMOJI
import com.tapbi.spark.yokey.common.Constant.GPHY_STICKER
import com.tapbi.spark.yokey.common.Constant.GPHY_TEXT
import com.tapbi.spark.yokey.common.Constant.GPHY_TRENDING
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.DisplayUtils
import com.koushikdutta.ion.Ion
import timber.log.Timber
import java.io.File

class ViewGif : ConstraintLayout {
    private lateinit var binding: ViewGifBinding
    private var iListenerCLickGif: IListenerCLickGif? = null
    private var cachePath: File? = null
    val settings = GPHSettings(GPHTheme.Automatic)
    var timeShowToast : Long = 0
    var mKeyboardSwitcher : KeyboardSwitcher = KeyboardSwitcher.getInstance()
    var ctlDownloadGif : ConstraintLayout? = null
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }


    private fun init() {
        cachePath = File(App.instance.cacheDir, "gifs")
        Giphy.configure(App.instance, GifConfig.YOUR_API_KEY_GIPHY, true)
        val inflate = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DataBindingUtil.inflate(inflate, R.layout.view_gif, this, true)
        setUpGif()
    }

    fun setViewDownloadGif(ctlDownloadGif : ConstraintLayout){
        this.ctlDownloadGif = ctlDownloadGif
    }

    fun setListenerGif(iListenerCLickGif: IListenerCLickGif) {
        this.iListenerCLickGif = iListenerCLickGif
    }

    private fun setUpGif() {
        //settings.showCheckeredBackground = true// enable/disable plaid background for stickers and text media types.
        binding.apply {
            gifsGridView.elevation = DisplayUtils.dp2px(12f).toFloat()
            gifsGridView.direction = GifConfig.direction
            gifsGridView.spanCount = GifConfig.spanCount
            gifsGridView.cellPadding = GifConfig.cellPadding
            gifsGridView.fixedSizeCells = GifConfig.fixedSizeCells
            gifsGridView.showCheckeredBackground = GifConfig.showCheckeredBackground
            setTrendingQuery()
        }
        binding.gifsGridView.callback = object : GPHGridCallback {
            override fun contentDidUpdate(resultCount: Int) {
                Timber.d("ducNQcontentDidUpdate1 "+resultCount);
                if (iListenerCLickGif != null)
                    iListenerCLickGif?.showProgressBar(resultCount)
            }

            override fun didSelectMedia(media: Media) {
                val file = File(cachePath, "/" + media.id + ".gif")
                val image: Image? = media.images.downsizedMedium
                val url = image?.gifUrl
                Timber.e("hachung didSelectMedia:  $url")
                if (url != null) {
                    if (!file.exists()) {
                        writeFileToCache(url, file, media)
                    } else {
                        if (CommonUtil.checkTime())
                            iListenerCLickGif?.clickGif(media)
                    }
                }
            }
        }
        binding.gifsGridView.searchCallback = object : GPHSearchGridCallback {
            override fun didTapUsername(username: String) {
                Timber.d("ducNQcontentDidUpdate 1");
            }

            override fun didLongPressCell(cell: GifView) {
                Timber.d("ducNQcontentDidUpdate 2");
            }

            override fun didScroll(dx: Int, dy: Int) {
                Timber.d("ducNQcontentDidUpdate 3");
            }
        }
        binding.gifsGridView.setGiphyLoadingProvider(loadingProviderClient)
    }

    fun getGifByTrending() {
        binding.gifsGridView.content = GPHContent.trendingGifs
    }

    fun searchGif(category: String) {
        binding.gifsGridView.content =
            GPHContent.searchQuery(category, GifConfig.mediaType)
    }

    fun getGifByCategory(category: String) {
        if (checkTypeCategoryGPHY(category)) {
            when (category) {
                GPHY_STICKER -> {
                    binding.gifsGridView.content = GPHContent.trendingStickers
                }
                GPHY_TEXT -> {
                    binding.gifsGridView.content = GPHContent.trendingText
                }
                GPHY_EMOJI -> {
                    binding.gifsGridView.content = GPHContent.emoji
                }
            }
        } else {
            if (category == GPHY_TRENDING) {
                getGifByTrending()
            } else {
                searchGif(category)
            }
        }
    }

    private fun checkTypeCategoryGPHY(category: String): Boolean {
        if (category == GPHY_STICKER || category == GPHY_TEXT || category == GPHY_EMOJI) {
            return true
        }
        return false
    }

    private fun setTrendingQuery() {
        binding.gifsGridView.content = when (GifConfig.contentType) {
            GPHContentType.clips -> GPHContent.trendingVideos
            GPHContentType.gif -> GPHContent.trendingGifs
            GPHContentType.sticker -> GPHContent.trendingStickers
            GPHContentType.text -> GPHContent.trendingText
            GPHContentType.emoji -> GPHContent.emoji
            GPHContentType.recents -> GPHContent.recents
            else -> throw Exception("MediaType ${GifConfig.mediaType} not supported ")
        }
    }

    private val loadingProviderClient = object : GiphyLoadingProvider {
        override fun getLoadingDrawable(position: Int): Drawable {
            return LoadingDrawable(if (position % 2 == 0) LoadingDrawable.Shape.Rect else LoadingDrawable.Shape.Circle)
        }

    }

    fun writeFileToCache(url: String, file: File, media: Media) {
        if (!mKeyboardSwitcher.getmLatinIME().isGifSupport) {
            if (System.currentTimeMillis() - timeShowToast > 1000) {
                if (mKeyboardSwitcher.visibleKeyboardView is EmojiPalettesView) {
                    (mKeyboardSwitcher.visibleKeyboardView as EmojiPalettesView).showTextNotSupportGif(R.string.not_support_gif)
                }
                timeShowToast = System.currentTimeMillis()
            }
            return
        }else {
            iListenerCLickGif?.downloadGif()
            Log.d("duongcv", "writeFileToCache: show")
            Ion.with(context).load(url).write(file).setCallback { e, result ->
                iListenerCLickGif?.finishDownloadGif()
                if (result != null) {
                        iListenerCLickGif?.clickGif(media)
                }
            }
        }

    }

    interface IListenerCLickGif {
        fun clickGif(media: Media)
        fun showProgressBar(count : Int)
        fun downloadGif()
        fun finishDownloadGif()
    }
}