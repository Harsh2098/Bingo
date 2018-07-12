package com.hmproductions.bingo.dagger

import android.content.Context
import android.net.ConnectivityManager

import dagger.Module
import dagger.Provides

@Module
class SystemServiceModule {
    @Provides
    @BingoApplicationScope
    internal fun getConnectivityManager(context: Context) = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}
