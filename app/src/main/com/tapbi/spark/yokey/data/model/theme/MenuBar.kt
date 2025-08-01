package com.tapbi.spark.yokey.data.model.theme

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class MenuBar() : Parcelable{
    @SerializedName("divider_color")
    @Expose
    var dividerColor: String? = null

    @SerializedName("content_divider_color")
    @Expose
    var contentDividerColor: String? = null

    @SerializedName("icon_color")
    @Expose
    var iconColor: String? = null

    @SerializedName("icon_color_selected")
    @Expose
    var iconColorSelected: String? = null

    @SerializedName("icon_background_color_selected")
    @Expose
    var iconBackgroundColorSelected: String? = null

    @SerializedName("text_color")
    @Expose
    var textColor: String? = null

    @SerializedName("text_color_highlighted")
    @Expose
    var textColorHighlighted: String? = null

    @SerializedName("content_text_Color")
    @Expose
    var contentTextColor: String? = null

    @SerializedName("menu")
    @Expose
    var menu: Menu? = null

    constructor(parcel: Parcel) : this() {
        dividerColor = parcel.readString()
        contentDividerColor = parcel.readString()
        iconColor = parcel.readString()
        iconColorSelected = parcel.readString()
        iconBackgroundColorSelected = parcel.readString()
        textColor = parcel.readString()
        textColorHighlighted = parcel.readString()
        contentTextColor = parcel.readString()
        menu = parcel.readParcelable(Menu::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(dividerColor)
        parcel.writeString(contentDividerColor)
        parcel.writeString(iconColor)
        parcel.writeString(iconColorSelected)
        parcel.writeString(iconBackgroundColorSelected)
        parcel.writeString(textColor)
        parcel.writeString(textColorHighlighted)
        parcel.writeString(contentTextColor)
        parcel.writeParcelable(menu, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MenuBar> {
        override fun createFromParcel(parcel: Parcel): MenuBar {
            return MenuBar(parcel)
        }

        override fun newArray(size: Int): Array<MenuBar?> {
            return arrayOfNulls(size)
        }
    }
}