package com.tapbi.spark.yokey.data.model.theme

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class Menu() : Parcelable {
    @SerializedName("bg_color")
    @Expose
    var bgColor: String? = null

    @SerializedName("bg_color_selected")
    @Expose
    var bgColorSelected: String? = null

    @SerializedName("text_color")
    @Expose
    var textColor: String? = null

    @SerializedName("text_color_selected")
    @Expose
    var textColorSelected: String? = null

    constructor(parcel: Parcel) : this() {
        bgColor = parcel.readString()
        bgColorSelected = parcel.readString()
        textColor = parcel.readString()
        textColorSelected = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bgColor)
        parcel.writeString(bgColorSelected)
        parcel.writeString(textColor)
        parcel.writeString(textColorSelected)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Menu> {
        override fun createFromParcel(parcel: Parcel): Menu {
            return Menu(parcel)
        }

        override fun newArray(size: Int): Array<Menu?> {
            return arrayOfNulls(size)
        }
    }
}