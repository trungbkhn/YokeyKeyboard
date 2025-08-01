package com.tapbi.spark.yokey.data.model

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class ThemeObjectList {
    @SerializedName("Items")
    @Expose
    var items: List<ThemeObject>? = null

    @SerializedName("Count")
    @Expose
    var count: Int? = null

    @SerializedName("ScannedCount")
    @Expose
    var scannedCount: Int? = null

    @SerializedName("LastVersion")
    @Expose
    var lastVersion: Int? = null
}