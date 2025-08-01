package com.tapbi.spark.yokey.data.model

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import com.tapbi.spark.yokey.data.model.theme.ThemeModel

class ThemesListModel {
    @SerializedName("Items")
    @Expose
    var items: List<ThemeModel>? = null

    @SerializedName("Count")
    @Expose
    var count: Int? = null

    @SerializedName("ScannedCount")
    @Expose
    var scannedCount: Int? = null
}