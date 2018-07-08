package com.hmproductions.bingo.dagger

import com.hmproductions.bingo.ui.main.HomeFragment
import com.hmproductions.bingo.ui.main.RoomFragment
import com.hmproductions.bingo.ui.GameActivity
import com.hmproductions.bingo.ui.main.MainActivity
import com.hmproductions.bingo.ui.SplashActivity

import dagger.Component

@BingoApplicationScope
@Component(modules = [(StubModule::class), (ChannelModule::class), (ContextModule::class), (PreferencesModule::class)])
interface BingoApplicationComponent {

    fun inject(mainActivity: MainActivity)
    fun inject(gameActivity: GameActivity)
    fun inject(splashActivity: SplashActivity)

    fun inject(roomFragment: RoomFragment)
    fun inject(homeFragment: HomeFragment)
}
