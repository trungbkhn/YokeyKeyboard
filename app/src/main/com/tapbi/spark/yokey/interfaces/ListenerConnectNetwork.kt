package com.tapbi.spark.yokey.interfaces

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.util.Constant
import org.greenrobot.eventbus.EventBus

class ListenerConnectNetwork : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == "android.net.conn.CONNECTIVITY_CHANGE" || intent.action == "android.net.wifi.WIFI_STATE_CHANGED") {
            val status: Int = App.instance.checkConnectivityStatus()
            if (status != -1) {
                App.instance.connectivityStatus = 1
                EventBus.getDefault().post(MessageEvent(Constant.CONNECT_INTERNET, null))
            } else {
                App.instance.connectivityStatus = -1
                EventBus.getDefault().post(MessageEvent(Constant.DISCONNECT_INTERNET, null))
            }
        }
    }
}