package com.iraimjanov.taxometr.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.view.LayoutInflater
import com.iraimjanov.taxometr.databinding.DialogNoInternetBinding

class ConnectionStateMonitor(private val context: Context) :
    ConnectivityManager.NetworkCallback() {
    lateinit var dialog: AlertDialog
    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    fun enable(context: Context) {
        buildNoInternetDialog()
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, this)
        if (!NetworkHelper(context).isNetworkConnected()){
            (context as Activity).runOnUiThread {
                if (!context.isFinishing) {
                    dialog.show()
                }
            }
        }
    }

    override fun onAvailable(network: Network) {
        (context as Activity).runOnUiThread {
            dialog.dismiss()
        }
    }

    override fun onLost(network: Network) {
        (context as Activity).runOnUiThread {
            if (!context.isFinishing) {
                dialog.show()
            }
        }
    }

    private fun buildNoInternetDialog() {
        val dialogBinding = DialogNoInternetBinding.inflate(LayoutInflater.from(context))
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setView(dialogBinding.root)
        dialog = alertDialog.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
    }

}