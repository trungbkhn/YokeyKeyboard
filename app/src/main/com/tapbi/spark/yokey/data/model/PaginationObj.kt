package com.tapbi.spark.yokey.data.model

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class PaginationObj(
    @field:Expose @field:SerializedName("sort_key") var sortKey: Int,
    @field:Expose @field:SerializedName("id_category") var id_category: Int,
    @field:Expose @field:SerializedName("id_sticker") var id_sticker: Int,
    @field:Expose @field:SerializedName("last_version") var lastVersion: Int
)