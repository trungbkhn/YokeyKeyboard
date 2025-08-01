package com.tapbi.spark.yokey.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PaginationUpdate(@field:Expose @field:SerializedName("last_version") var lastVersion: Int)
