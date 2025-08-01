package com.tapbi.spark.objects

import com.tapbi.spark.yokey.R
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tapbi.spark.yokey.App
import java.util.ArrayList

class BackgroundList {
     var title: String= App.instance.resources.getString(R.string.txt_name_hot_theme)


    @SerializedName("Items")
    @Expose
     var backgroundJson: ArrayList<Background> = ArrayList()

    @SerializedName("LastVersion")
    @Expose
    var lastVersion : Int = 0

    @SerializedName("Count")
    var count : Int? = null

    @SerializedName("ScannedCount")
    var scannedCount : Int? = null

    constructor(title: String, backgroundJson: ArrayList<Background>) {
        this.title = title
        this.backgroundJson = backgroundJson
    }

    constructor()

}