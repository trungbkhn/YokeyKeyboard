package com.tapbi.spark.yokey.data.model

import android.os.Parcel
import android.os.Parcelable
import com.tapbi.spark.yokey.data.local.entity.Sticker
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import java.util.ArrayList

class ListSticker(
    @field:Expose @field:SerializedName("Items") var stickerList: ArrayList<Sticker?>?,
    @field:Expose @field:SerializedName("Count") var count: Int,
    @field:Expose @field:SerializedName("ScannedCount") var scannedCount: Int,
    @field:Expose @field:SerializedName("LastEvaluatedKey") var lastEvaluatedKey: LastEvaluatedKey?,
    @field:Expose @field:SerializedName("LastVersion") var lastVersion: Int?
)  : Parcelable{
    constructor(parcel: Parcel) : this(
        TODO("stickerList"),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(LastEvaluatedKey::class.java.classLoader),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(count)
        parcel.writeInt(scannedCount)
        parcel.writeParcelable(lastEvaluatedKey, flags)
        lastVersion?.let { parcel.writeInt(it) }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ListSticker> {
        override fun createFromParcel(parcel: Parcel): ListSticker {
            return ListSticker(parcel)
        }

        override fun newArray(size: Int): Array<ListSticker?> {
            return arrayOfNulls(size)
        }
    }


}