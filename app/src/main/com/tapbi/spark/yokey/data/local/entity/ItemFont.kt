package com.tapbi.spark.yokey.data.local.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.tapbi.spark.yokey.data.model.DetailObject

@Entity(tableName = "ItemFontTable")
open class ItemFont : Parcelable, DetailObject {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "textFont")
    var textFont: String? = null

    @ColumnInfo(name = "favorite")
    var favorite = 0

    @ColumnInfo(name = "imgBackground")
    var imgBackground: String? = null

    @JvmField
    @ColumnInfo(name = "textDemo")
    var textDemo: String? = null

    @JvmField
    @ColumnInfo(name = "filterCategories")
    var filterCategories: String? = null

    @JvmField
    @ColumnInfo(name = "isPremium")
    var isPremium = 0

    @JvmField
    @ColumnInfo(name = "isAdd")
    var isAdd = 0

    @ColumnInfo(name = "dateModify")
    var dateModify = System.currentTimeMillis().toInt()

    constructor() {}
    constructor(
        id: Int,
        textFont: String?,
        favorite: Int,
        imgBackground: String?,
        textDemo: String?,
        filterCategories: String?,
        isPremium: Boolean,
        isAdd: Boolean
    ) {
        this.id = id
        this.textFont = textFont
        this.favorite = favorite
        this.imgBackground = imgBackground
        this.textDemo = textDemo
        this.filterCategories = filterCategories
        if (isPremium) this.isPremium = 1 else this.isPremium = 0
        if (isAdd) this.isAdd = 1 else this.isAdd = 0
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readInt()
        textFont = `in`.readString()
        favorite = `in`.readInt()
        imgBackground = `in`.readString()
        textDemo = `in`.readString()
        filterCategories = `in`.readString()
        isPremium = `in`.readInt()
        isAdd = `in`.readInt()
        dateModify = `in`.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(textFont)
        dest.writeInt(favorite)
        dest.writeString(imgBackground)
        dest.writeString(textDemo)
        dest.writeString(filterCategories)
        dest.writeByte(isPremium.toByte())
        dest.writeByte(isAdd.toByte())
        dest.writeLong(dateModify.toLong())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ItemFont?> = object : Parcelable.Creator<ItemFont?> {
            override fun createFromParcel(`in`: Parcel): ItemFont {
                return ItemFont(`in`)
            }

            override fun newArray(size: Int): Array<ItemFont?> {
                return arrayOfNulls(size)
            }
        }
    }
}