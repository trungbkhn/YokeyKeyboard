package com.tapbi.spark.yokey.data.model.theme

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class Popup() : Parcelable{
    @SerializedName("preview")
    @Expose
    var preview: Preview? = null

    @SerializedName("min_keyboard")
    @Expose
    var minKeyboard: MinKeyboard? = null

    constructor(parcel: Parcel) : this() {
        preview = parcel.readParcelable(Preview::class.java.classLoader)
        minKeyboard = parcel.readParcelable(MinKeyboard::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(preview, flags)
        parcel.writeParcelable(minKeyboard, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Popup> {
        override fun createFromParcel(parcel: Parcel): Popup {
            return Popup(parcel)
        }

        override fun newArray(size: Int): Array<Popup?> {
            return arrayOfNulls(size)
        }
    }
}