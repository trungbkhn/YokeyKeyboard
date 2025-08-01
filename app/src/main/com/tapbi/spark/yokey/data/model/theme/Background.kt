package com.tapbi.spark.yokey.data.model.theme

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class Background() : Parcelable {
    @SerializedName("background_color")
    @Expose
    var backgroundColor: String? = null

    @SerializedName("background_image")
    @Expose
    var backgroundImage: String? = null

    @SerializedName("start_color")
    @Expose
    var startColor: String? = null

    @SerializedName("finish_color")
    @Expose
    var finishColor: String? = null

    @SerializedName("degree")
    @Expose
    var degree: Int? = null

    @SerializedName("radiusBlur")
    @Expose
    var radiusBlur: Int = 0

    constructor(parcel: Parcel) : this() {
        backgroundColor = parcel.readString()
        backgroundImage = parcel.readString()
        startColor = parcel.readString()
        finishColor = parcel.readString()
        degree = parcel.readValue(Int::class.java.classLoader) as? Int
        radiusBlur = parcel.readInt()
    }


    constructor(
        backgroundColor: String?,
        backgroundImage: String?,
        startColor: String?,
        finishColor: String?,
        degree: Int?,
        radiusBlur : Int
    ) : this() {
        this.backgroundColor = backgroundColor
        this.backgroundImage = backgroundImage
        this.startColor = startColor
        this.finishColor = finishColor
        this.degree = degree
        this.radiusBlur = radiusBlur
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(backgroundColor)
        parcel.writeString(backgroundImage)
        parcel.writeString(startColor)
        parcel.writeString(finishColor)
        parcel.writeValue(degree)
        parcel.writeInt(radiusBlur)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Background> {
        override fun createFromParcel(parcel: Parcel): Background {
            return Background(parcel)
        }

        override fun newArray(size: Int): Array<Background?> {
            return arrayOfNulls(size)
        }
    }


}