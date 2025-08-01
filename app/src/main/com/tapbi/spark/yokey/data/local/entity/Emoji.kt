package com.tapbi.spark.yokey.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "EmojiTable")
data class Emoji(
    @PrimaryKey(autoGenerate = true)
    var id: Int =0,

    @ColumnInfo(name = "content")
    @SerializedName("content")
    @Expose
    var content: String?,

    @ColumnInfo(name = "title")
    @SerializedName("title")
    @Expose
    var title: String?,

    @ColumnInfo(name = "type")
    @SerializedName("type")
    @Expose
    var type: Int?=0,

    @ColumnInfo(name = "favourite")
    @SerializedName("favourite")
    @Expose
    var favourite: Int?=0,

    @ColumnInfo(name = "count_favourite")
    @SerializedName("count_favourite")
    @Expose
    var count_favourite: Int? = 0
)
