package com.tapbi.spark.yokey.data.local.entity

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.tapbi.spark.yokey.data.model.DetailObject

@Entity(tableName = "ThemeEntityTable")
class ThemeEntity : DetailObject {

    @PrimaryKey
    var id: String=""

    @ColumnInfo(name = "name")
    var name: String? = null

    @JvmField
    @ColumnInfo(name = "isTopTheme")
    var isTopTheme: String? = null

    @JvmField
    @ColumnInfo(name = "isHotTheme")
    var isHotTheme: String? = null

    @ColumnInfo(name = "urlCoverTopTheme")
    var urlCoverTopTheme: String? = null

    @ColumnInfo(name = "downloadCount")
    var downloadCount: Int? = null

    @ColumnInfo(name = "preview")
    var preview: String? = null

    @ColumnInfo(name = "purchase")
    var purchase: String? = null

    @ColumnInfo(name = "typeKeyboard")
    var typeKeyboard: String? = null

    @ColumnInfo(name = "urlTheme")
    var urlTheme: String? = null

    @ColumnInfo(name = "viewAds")
    var viewAds: String? = null

    @JvmField
    @ColumnInfo(name = "isMyTheme")
    var isMyTheme: Int = 0
}