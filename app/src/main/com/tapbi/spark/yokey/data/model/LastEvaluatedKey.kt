package com.tapbi.spark.yokey.data.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LastEvaluatedKey(
    @field:Expose @field:SerializedName("sort_key") var sortKey: Int,
    @field:Expose @field:SerializedName("id_category") var id_category: Int,
    @field:Expose @field:SerializedName("id_sticker") var id_sticker: Int
) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(sortKey)
        parcel.writeInt(id_category)
        parcel.writeInt(id_sticker)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LastEvaluatedKey> {
        override fun createFromParcel(parcel: Parcel): LastEvaluatedKey {
            return LastEvaluatedKey(parcel)
        }

        override fun newArray(size: Int): Array<LastEvaluatedKey?> {
            return arrayOfNulls(size)
        }
    }
}