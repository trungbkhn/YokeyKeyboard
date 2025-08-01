package com.tapbi.spark.yokey.data.model

data class PurchaseData(var sku :String,
                        var lastTimeActive:Long,
                        var purchaseTime:Long,
                        var autoRenew:Boolean)
