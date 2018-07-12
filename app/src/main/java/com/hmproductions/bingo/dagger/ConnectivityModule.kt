package com.hmproductions.bingo.dagger

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest

import dagger.Module
import dagger.Provides

@Module
class ConnectivityModule {
    @Provides
    @BingoApplicationScope
    internal fun getConnectivityManager(context: Context) = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    @BingoApplicationScope
    fun getNetworkRequest() = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build()
}
