package com.hmproductions.bingo.dagger

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dagger.Module
import dagger.Provides

@Module
class PreferencesModule {

    @Provides
    @BingoApplicationScope
    fun getSharedPreferences(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
}
