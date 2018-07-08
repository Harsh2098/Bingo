package com.hmproductions.bingo.dagger

import android.content.Context

import dagger.Module
import dagger.Provides

@Module
class ContextModule(private val context: Context) {

    @Provides
    @BingoApplicationScope
    fun context() = context
}