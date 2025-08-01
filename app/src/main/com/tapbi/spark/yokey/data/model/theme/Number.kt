package com.tapbi.spark.yokey.data.model.theme

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class Number() : Parcelable {
    @SerializedName("normal")
    @Expose
    var normal: String? = null

    @SerializedName("pressed")
    @Expose
    var pressed: String? = null

    @SerializedName("text_color")
    @Expose
    var textColor: String? = null

    @SerializedName("text_color_pressed")
    @Expose
    var textColorPressed: String? = null

    constructor(parcel: Parcel) : this() {
        normal = parcel.readString()
        pressed = parcel.readString()
        textColor = parcel.readString()
        textColorPressed = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(normal)
        parcel.writeString(pressed)
        parcel.writeString(textColor)
        parcel.writeString(textColorPressed)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Number> {
        override fun createFromParcel(parcel: Parcel): Number {
            return Number(parcel)
        }

        override fun newArray(size: Int): Array<Number?> {
            return arrayOfNulls(size)
        }
    }


}