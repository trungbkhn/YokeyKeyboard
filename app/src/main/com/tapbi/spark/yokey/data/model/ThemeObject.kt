package com.tapbi.spark.yokey.data.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

open class ThemeObject() : Parcelable, DetailObject {

    @SerializedName("id_category")
    @Expose
    var idCategory: Int? = null

    @SerializedName("is_hot")
    @Expose
    var isHotTheme: Int? = null

    @SerializedName("url_cover_top_theme")
    @Expose
    var urlCoverTopTheme: String? = null

    @SerializedName("downloard_count")
    @Expose
    var downloadCount: Int? = 0

    @SerializedName("id_theme")
    @Expose
    var id: Long? = null

    @SerializedName("theme_name")
    @Expose
    var name: String? = null

    @SerializedName("preview")
    @Expose
    var preview: String? = null

    @SerializedName("purchase")
    @Expose
    var purchase: Boolean? = false

    @SerializedName("category_name")
    @Expose
    var typeKeyboard: String?= null

    @SerializedName("url_theme")
    @Expose
    var urlTheme: String?= null

    constructor(parcel: Parcel) : this() {
        idCategory = parcel.readInt()
        isHotTheme = parcel.readInt()
        urlCoverTopTheme = parcel.readString()
        downloadCount = parcel.readValue(Int::class.java.classLoader) as? Int
        id = parcel.readLong()
        name = parcel.readString()
        preview = parcel.readString()
        purchase = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        typeKeyboard = parcel.readString()
        urlTheme = parcel.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if (downloadCount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(downloadCount!!)
        }
        id?.let { dest.writeLong(it) }
        dest.writeString(name)
        dest.writeString(preview)
        dest.writeByte((if (purchase == null) 0 else if (purchase as Boolean) 1 else 2).toByte())
        dest.writeString(typeKeyboard)
        dest.writeString(urlTheme)
        isHotTheme?.let { dest.writeInt(it) }
        idCategory?.let { dest.writeInt(it) }
        dest.writeString(urlCoverTopTheme)
    }


    companion object CREATOR : Parcelable.Creator<ThemeObject> {
        override fun createFromParcel(parcel: Parcel): ThemeObject {
            return ThemeObject(parcel)
        }

        override fun newArray(size: Int): Array<ThemeObject?> {
            return arrayOfNulls(size)
        }
    }


}