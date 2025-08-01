package com.tapbi.spark.yokey.data.local.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SymbolsTable")
class Symbols() : Parcelable {

    @PrimaryKey
    var id: Int =0

    @ColumnInfo(name = "typeSymbols")
    var typeSymbols : String? = null

    @JvmField
    @ColumnInfo(name = "contentSymbols")
    var contentSymbols : String? = null

    @JvmField
    @ColumnInfo(name = "timeRecent")
    var timeRecent : String = "0"

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        typeSymbols = parcel.readString()
        contentSymbols = parcel.readString()
        timeRecent = parcel.readString()!!
    }

    constructor(id: Int, typeSymbols: String?, contentSymbols: String?) : this() {
        this.id = id
        this.typeSymbols = typeSymbols
        this.contentSymbols = contentSymbols
        this.timeRecent = "0"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(typeSymbols)
        parcel.writeString(contentSymbols)
        parcel.writeString(timeRecent)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Symbols> {
        override fun createFromParcel(parcel: Parcel): Symbols {
            return Symbols(parcel)
        }

        override fun newArray(size: Int): Array<Symbols?> {
            return arrayOfNulls(size)
        }
    }
}