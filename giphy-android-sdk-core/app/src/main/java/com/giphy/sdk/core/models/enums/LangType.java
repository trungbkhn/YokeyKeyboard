/*
 * Created by Bogdan Tirca on 4/26/17.
 * Copyright (c) 2017 Giphy Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.giphy.sdk.core.models.enums;

import com.google.gson.annotations.SerializedName;

public enum LangType {
    @SerializedName("en")
    english("en"),

    @SerializedName("es")
    spanish("es"),

    @SerializedName("pt")
    portuguese("pt"),

    @SerializedName("id")
    indonesian("id"),

    @SerializedName("fr")
    french("fr"),

    @SerializedName("ar")
    arabic("ar"),

    @SerializedName("tr")
    turkish("tr"),

    @SerializedName("th")
    thai("th"),

    @SerializedName("vi")
    vietnamese("vi"),

    @SerializedName("de")
    german("de"),

    @SerializedName("it")
    italian("it"),

    @SerializedName("ja")
    japanese("ja"),

    @SerializedName("zh-CN")
    chineseSimplified("zh-CN"),

    @SerializedName("zh-TW")
    chineseTraditional("zh-TW"),

    @SerializedName("ru")
    russian("ru"),

    @SerializedName("ko")
    korean("ko"),

    @SerializedName("pl")
    polish("pl"),

    @SerializedName("nl")
    dutch("nl"),

    @SerializedName("ro")
    romanian("ro"),

    @SerializedName("hu")
    hungarian("hu"),

    @SerializedName("sv")
    swedish("sv"),

    @SerializedName("cs")
    czech("cs"),

    @SerializedName("hi")
    hindi("hi"),

    @SerializedName("bn")
    bengali("bn"),

    @SerializedName("da")
    danish("da"),

    @SerializedName("fa")
    farsi("fa"),

    @SerializedName("tl")
    filipino("tl"),

    @SerializedName("fi")
    finnish("fi"),

    @SerializedName("iw")
    hebrew("iw"),

    @SerializedName("ms")
    malay("ms"),

    @SerializedName("no")
    norwegian("no"),

    @SerializedName("uk")
    ukrainian("uk");

    private final String lang;

    private LangType(String lang) {
        this.lang = lang;
    }

    public String toString() {
        return this.lang;
    }
}
