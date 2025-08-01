package com.tapbi.spark.yokey.data.model

import android.os.Bundle

class MessageEvent() {
    var key: Int =0
    var bundle: Bundle? = null
    var dataString : String? = null

    constructor(key : Int) : this() {
        this.key = key
    }

    constructor(key : Int, bundle: Bundle?) : this() {
        this.key = key
        this.bundle = bundle
    }

    constructor(key: Int, bundle: Bundle?, dataString: String) : this(){
        this.key = key
        this.bundle = bundle
        this.dataString = dataString
    }









}