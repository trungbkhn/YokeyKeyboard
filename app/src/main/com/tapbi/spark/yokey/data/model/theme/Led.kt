package com.tapbi.spark.yokey.data.model.theme

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class Led() : Parcelable {
    @SerializedName("alpha")
    @Expose
    var alpha = 0f

    @SerializedName("colors")
    @Expose
    var colors: String? = null

    @SerializedName("cross")
    @Expose
    var cross: Float? = null

    @SerializedName("radius")
    @Expose
    var radius: Float? = null

    @SerializedName("range")
    @Expose
    var range: Float? = null

    @SerializedName("speed")
    @Expose
    var speed: Float? = null

    @SerializedName("stroke_width")
    @Expose
    var strokeWidth: Float? = null

    @SerializedName("style")
    @Expose
    var style: String? = null

    @SerializedName("style_led")
    @Expose
    var styleLed: Int? = null

    constructor(parcel: Parcel) : this() {
        alpha = parcel.readFloat()
        colors = parcel.readString()
        cross = parcel.readValue(Float::class.java.classLoader) as? Float
        radius = parcel.readValue(Float::class.java.classLoader) as? Float
        range = parcel.readValue(Float::class.java.classLoader) as? Float
        speed = parcel.readValue(Float::class.java.classLoader) as? Float
        strokeWidth = parcel.readValue(Float::class.java.classLoader) as? Float
        style = parcel.readString()
        styleLed = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(alpha)
        parcel.writeString(colors)
        parcel.writeValue(cross)
        parcel.writeValue(radius)
        parcel.writeValue(range)
        parcel.writeValue(speed)
        parcel.writeValue(strokeWidth)
        parcel.writeString(style)
        parcel.writeValue(styleLed)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Led> {
        override fun createFromParcel(parcel: Parcel): Led {
            return Led(parcel)
        }

        override fun newArray(size: Int): Array<Led?> {
            return arrayOfNulls(size)
        }
    }
}