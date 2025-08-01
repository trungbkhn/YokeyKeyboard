package com.tapbi.spark.yokey.data.model

import android.os.Parcel
import android.os.Parcelable
import java.util.ArrayList

open class StickerOnKeyboard : Parcelable {
    var listSticker: ArrayList<String> = ArrayList()
    var thumb: String = ""

    constructor(listSticker: ArrayList<String>, thumb: String) {
        this.listSticker = listSticker
        this.thumb = thumb
    }

    protected constructor(`in`: Parcel) {
        listSticker = `in`.createStringArrayList()!!
        thumb = `in`.readString()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeStringList(listSticker)
        dest.writeString(thumb)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<StickerOnKeyboard> {
            override fun createFromParcel(source: Parcel?): StickerOnKeyboard? {
                return source?.let { StickerOnKeyboard(it) }
            }

            override fun newArray(size: Int): Array<StickerOnKeyboard?> {
                return arrayOfNulls(size)
            }
        }
    }

}