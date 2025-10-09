package com.cccinfotech.fooddeliverypoc.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast

class NetworkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(network)

        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (!isConnected) {
            val broadcastIntent = Intent("NETWORK_STATUS_CHANGED")
            broadcastIntent.putExtra("isConnected", false)
            context.sendBroadcast(broadcastIntent)
        }else{
            val broadcastIntent = Intent("NETWORK_STATUS_CHANGED")
            broadcastIntent.putExtra("isConnected", true)
            context.sendBroadcast(broadcastIntent)
        }
    }
}
