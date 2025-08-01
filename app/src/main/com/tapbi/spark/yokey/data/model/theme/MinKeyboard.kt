package com.tapbi.spark.yokey.data.model.theme

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class MinKeyboard() : Parcelable{
    @SerializedName("bg_image")
    @Expose
    var bgImage: String? = null

    @SerializedName("text_color")
    @Expose
    var textColor: String? = null

    @SerializedName("text_color_selected")
    @Expose
    var textColorSelected: String? = null

    constructor(parcel: Parcel) : this() {
        bgImage = parcel.readString()
        textColor = parcel.readString()
        textColorSelected = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bgImage)
        parcel.writeString(textColor)
        parcel.writeString(textColorSelected)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MinKeyboard> {
        override fun createFromParcel(parcel: Parcel): MinKeyboard {
            return MinKeyboard(parcel)
        }

        override fun newArray(size: Int): Array<MinKeyboard?> {
            return arrayOfNulls(size)
        }
    }
}