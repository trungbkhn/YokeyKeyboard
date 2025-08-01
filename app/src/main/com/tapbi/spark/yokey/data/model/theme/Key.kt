package com.tapbi.spark.yokey.data.model.theme

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class Key() : Parcelable {
    @SerializedName("text")
    @Expose
    var text: Text? = null

    @SerializedName("number")
    @Expose
    var number: Number? = null

    @SerializedName("special")
    @Expose
    var special: Special? = null

    @SerializedName("led")
    @Expose
    var led: Led? = null

    constructor(parcel: Parcel) : this() {
        text = parcel.readParcelable(Text::class.java.classLoader)
        number = parcel.readParcelable(Number::class.java.classLoader)
        special = parcel.readParcelable(Special::class.java.classLoader)
        led = parcel.readParcelable(Led::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(text, flags)
        parcel.writeParcelable(number, flags)
        parcel.writeParcelable(special, flags)
        parcel.writeParcelable(led, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Key> {
        override fun createFromParcel(parcel: Parcel): Key {
            return Key(parcel)
        }

        override fun newArray(size: Int): Array<Key?> {
            return arrayOfNulls(size)
        }
    }
}