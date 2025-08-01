package com.tapbi.spark.yokey.data.model.theme

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BackgroundKey() : Parcelable {

    @SerializedName("shift")
    @Expose
    var shift: String? = null

    @SerializedName("enter")
    @Expose
    var enter: String? = null

    @SerializedName("delete")
    @Expose
    var delete: String? = null

    @SerializedName("symbol")
    @Expose
    var symbol: String? = null

    @SerializedName("language")
    @Expose
    var language: String? = null


    @SerializedName("comma")
    @Expose
    var comma: String? = null


    @SerializedName("period")
    @Expose
    var period: String? = null

    @SerializedName("scaleKey")
    @Expose
    var scaleKey: String? = null

    constructor(parcel: Parcel) : this() {
        shift = parcel.readString()
        enter = parcel.readString()
        delete = parcel.readString()
        symbol = parcel.readString()
        language = parcel.readString()
        comma = parcel.readString()
        period = parcel.readString()
        scaleKey = parcel.readString()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(shift)
        parcel.writeString(enter)
        parcel.writeString(delete)
        parcel.writeString(symbol)
        parcel.writeString(language)
        parcel.writeString(comma)
        parcel.writeString(period)
        parcel.writeString(scaleKey)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BackgroundKey> {
        override fun createFromParcel(parcel: Parcel): BackgroundKey {
            return BackgroundKey(parcel)
        }

        override fun newArray(size: Int): Array<BackgroundKey?> {
            return arrayOfNulls(size)
        }
    }


}