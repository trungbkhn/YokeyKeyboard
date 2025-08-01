package com.tapbi.spark.yokey.data.local.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "StickerRecentTable")
class StickerRecent() : Parcelable {

    @PrimaryKey
    var link : String =""

    @ColumnInfo(name = "timeRecent")
    var timeRecent : String = "0"

    constructor(parcel: Parcel) : this() {
        link = parcel.readString()!!
        timeRecent = parcel.readString()!!
    }

   constructor(link : String, timeRecent : String) : this() {
       this.link = link
       this.timeRecent = timeRecent
   }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(link)
        parcel.writeString(timeRecent)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StickerRecent> {
        override fun createFromParcel(parcel: Parcel): StickerRecent {
            return StickerRecent(parcel)
        }

        override fun newArray(size: Int): Array<StickerRecent?> {
            return arrayOfNulls(size)
        }
    }

}