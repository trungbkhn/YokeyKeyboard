package com.tapbi.spark.yokey.data.model

import com.tapbi.spark.yokey.data.local.entity.ItemFont

class ChangeToFontNormal {
    var isChangeFont = false
    var itemFont: ItemFont? = null

    constructor() {}
    constructor(isChangeFont: Boolean, itemFont: ItemFont?) {
        this.isChangeFont = isChangeFont
        this.itemFont = itemFont
    }
}