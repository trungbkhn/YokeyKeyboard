package com.tapbi.spark.yokey.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tapbi.spark.yokey.data.model.ThemeObject
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import androidx.viewpager.widget.ViewPager
import com.tapbi.spark.yokey.databinding.ItemPagerTopListThemeBinding

class ViewPagerThemesTopAdapter(
    private val context: Context,
    private var objectThemes: List<ThemeObject>
) : PagerAdapter() {
    override fun getCount(): Int {
        return objectThemes.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemPagerTopListThemeBinding =
            ItemPagerTopListThemeBinding.inflate(inflater, container, false)
        if (objectThemes[position].urlCoverTopTheme != null && !objectThemes[position].urlCoverTopTheme.equals(
                "",
                ignoreCase = true
            )
        ) {
            Glide.with(context).load(objectThemes[position].urlCoverTopTheme)
                .into(itemPagerTopListThemeBinding.imgTopViewpager)
        } else {
            Glide.with(context).load(objectThemes[position].preview)
                .into(itemPagerTopListThemeBinding.imgTopViewpager)
        }
        itemPagerTopListThemeBinding.root.setOnClickListener { v: View? ->
            val objectTheme = objectThemes[position]
        }
        container.addView(itemPagerTopListThemeBinding.root)
        return itemPagerTopListThemeBinding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        container.removeView(`object` as View)
    }

    override fun destroyItem(container: View, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as View)
    }

    fun setListTopTheme(objectThemesTemp: List<ThemeObject>) {
        objectThemes = objectThemesTemp
        notifyDataSetChanged()
    }
}