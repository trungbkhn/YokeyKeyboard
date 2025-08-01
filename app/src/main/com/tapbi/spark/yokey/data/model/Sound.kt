package com.tapbi.spark.yokey.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Sound(
    @SerializedName("id")
    @Expose
    var id: String,
    @SerializedName("name")
    @Expose
    var name: String,
    @SerializedName("image")
    @Expose
    var image: String,
    @SerializedName("isPremium")
    @Expose
    var isPremium: Int
) {
}