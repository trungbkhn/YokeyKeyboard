package com.tapbi.spark.objects

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "BackgroundTable")
data class Background(

    @ColumnInfo(name = "link_bg")
    @SerializedName("link_bg")
    @Expose
    var linkBg : String,

    @ColumnInfo(name = "link_bg_thumb")
    @SerializedName("link_bg_thumb")
    @Expose
    var  linkBgThumb: String,

    @ColumnInfo(name = "version_update")
    @SerializedName("version_update")
    @Expose
    var  versionUpdate: Int,

    @PrimaryKey
    @ColumnInfo(name = "id_bg")
    @SerializedName("id_bg")
    @Expose
    var idBg : Int) {
}