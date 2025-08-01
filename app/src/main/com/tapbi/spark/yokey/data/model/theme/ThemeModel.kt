package com.tapbi.spark.yokey.data.model.theme

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tapbi.spark.yokey.util.Constant

class ThemeModel() : Parcelable {


    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("name_keyboard")
    @Expose
    var nameKeyboard: String? = null

    @SerializedName("type_keyboard")
    @Expose
    var typeKeyboard: String? = null

    @SerializedName("font")
    @Expose
    var font: String? = null

    @SerializedName("background")
    @Expose
    var background: Background? = null

    @SerializedName("key")
    @Expose
    var key: Key? = null

    @SerializedName("backgroundKey")
    @Expose
    var backgroundKey: BackgroundKey? = null

    @SerializedName("popup")
    @Expose
    var popup: Popup? = null

    @SerializedName("menu_bar")
    @Expose
    var menuBar: MenuBar? = null

    @SerializedName("effect")
    @Expose
    var effect: String = Constant.ID_NONE

    @SerializedName("sound")
    @Expose
    var sound: String = Constant.AUDIO_DEFAULT

    @SerializedName("typeKey")
    @Expose
    var typeKey: Int = Constant.TYPE_KEY_DEFAULT


    @SerializedName("color_symbol")
    @Expose
    var colorSymbol: String = ""

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        nameKeyboard = parcel.readString()
        typeKeyboard = parcel.readString()
        font = parcel.readString()
        background = parcel.readParcelable(Background::class.java.classLoader)
        key = parcel.readParcelable(Key::class.java.classLoader)
        popup = parcel.readParcelable(Popup::class.java.classLoader)
        backgroundKey = parcel.readParcelable(Popup::class.java.classLoader)
        menuBar = parcel.readParcelable(MenuBar::class.java.classLoader)
        effect = parcel.readString()!!
        sound = parcel.readString()!!
        typeKey = parcel.readInt()
        colorSymbol = parcel.readString()!!
    }


    constructor(
        id: String?,
        nameKeyboard: String?,
        typeKeyboard: String?,
        font: String?,
        background: Background?,
        key: Key?,
        popup: Popup?,
        menuBar: MenuBar?,
        effect: String,
        sound: String,
        typeKey: Int,
        colorSymbol: String,
        backgroundKey: BackgroundKey?
    ) : this() {
        this.id = id
        this.nameKeyboard = nameKeyboard
        this.typeKeyboard = typeKeyboard
        this.font = font
        this.background = Background(
            background?.backgroundColor,
            background?.backgroundImage,
            background?.startColor,
            background?.finishColor,
            background?.degree,
            0
        )
        this.key = key
        this.popup = popup
        this.menuBar = menuBar
        this.effect = effect
        this.sound = sound
        this.typeKey = typeKey
        this.backgroundKey = backgroundKey
        this.colorSymbol = colorSymbol
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nameKeyboard)
        parcel.writeString(typeKeyboard)
        parcel.writeString(font)
        parcel.writeParcelable(background, flags)
        parcel.writeParcelable(key, flags)
        parcel.writeParcelable(popup, flags)
        parcel.writeParcelable(menuBar, flags)
        parcel.writeString(effect)
        parcel.writeString(sound)
        parcel.writeInt(typeKey)
        parcel.writeString(colorSymbol)
        parcel.writeParcelable(backgroundKey, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun copy() = ThemeModel(
        id,
        nameKeyboard,
        typeKeyboard,
        font,
        background,
        key,
        popup,
        menuBar,
        effect,
        sound,
        typeKey,
        colorSymbol,
        backgroundKey
    )

    companion object CREATOR : Parcelable.Creator<ThemeModel> {
        override fun createFromParcel(parcel: Parcel): ThemeModel {
            return ThemeModel(parcel)
        }

        override fun newArray(size: Int): Array<ThemeModel?> {
            return arrayOfNulls(size)
        }
    }


}