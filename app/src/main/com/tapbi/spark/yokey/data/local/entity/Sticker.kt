package com.tapbi.spark.yokey.data.local.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import com.tapbi.spark.yokey.data.model.DetailObject

@Entity(tableName = "StickerTable")
open class Sticker : Parcelable, DetailObject {
    @PrimaryKey
    @ColumnInfo(name = "id_sticker")
    @SerializedName("id_sticker")
    @Expose
    var id: Int

    @ColumnInfo(name = "name_sticker")
    @SerializedName("name_sticker")
    @Expose
    var name: String?

    @ColumnInfo(name = "thumb_sticker")
    @SerializedName("thumb_sticker")
    @Expose
    var thumb: String?

    @ColumnInfo(name = "link_sticker")
    @SerializedName("link_sticker")
    @Expose
    var url: String?

    @ColumnInfo(name = "id_category")
    @SerializedName("id_category")
    @Expose
    var idCategory: Int

    @ColumnInfo(name = "category_name")
    @SerializedName("category_name")
    @Expose
    var nameCategory: String?

    @ColumnInfo(name = "isDownload")
    var isDownload: Int

    constructor(
        id: Int,
        name: String?,
        thumb: String?,
        url: String?,
        idCategory: Int,
        nameCategory: String?,
        isDownload: Int
    ) {
        this.id = id
        this.name = name
        this.thumb = thumb
        this.url = url
        this.idCategory = idCategory
        this.nameCategory = nameCategory
        this.isDownload = isDownload
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readInt()
        name = `in`.readString()
        thumb = `in`.readString()
        url = `in`.readString()
        idCategory = `in`.readInt()
        nameCategory = `in`.readString()
        isDownload = `in`.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(name)
        dest.writeString(thumb)
        dest.writeString(url)
        dest.writeInt(idCategory)
        dest.writeString(nameCategory)
        dest.writeInt(isDownload)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Sticker?> = object : Parcelable.Creator<Sticker?> {
            override fun createFromParcel(`in`: Parcel): Sticker {
                return Sticker(`in`)
            }

            override fun newArray(size: Int): Array<Sticker?> {
                return arrayOfNulls(size)
            }
        }
    }
}