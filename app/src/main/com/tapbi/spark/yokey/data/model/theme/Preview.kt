package com.tapbi.spark.yokey.data.model.theme

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class Preview() : Parcelable {
    @SerializedName("text_color")
    @Expose
    var textColor: String? = null

    @SerializedName("subkey_color")
    @Expose
    var subkeyColor: String? = null

    @SerializedName("bg_image")
    @Expose
    var bgImage: String? = null

    @SerializedName("width")
    @Expose
    var width: String? = null

    @SerializedName("height")
    @Expose
    var height: String? = null

    constructor(parcel: Parcel) : this() {
        textColor = parcel.readString()
        subkeyColor = parcel.readString()
        bgImage = parcel.readString()
        width = parcel.readString()
        height = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(textColor)
        parcel.writeString(subkeyColor)
        parcel.writeString(bgImage)
        parcel.writeString(width)
        parcel.writeString(height)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Preview> {
        override fun createFromParcel(parcel: Parcel): Preview {
            return Preview(parcel)
        }

        override fun newArray(size: Int): Array<Preview?> {
            return arrayOfNulls(size)
        }
    }
}