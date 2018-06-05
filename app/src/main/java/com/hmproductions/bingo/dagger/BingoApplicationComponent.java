package com.hmproductions.bingo.dagger;

import com.hmproductions.bingo.ui.GameActivity;
import com.hmproductions.bingo.ui.MainActivity;
import com.hmproductions.bingo.ui.SplashActivity;

import dagger.Component;

@BingoApplicationScope
@Component (modules = {StubModule.class, ChannelModule.class, ContextModule.class, PreferencesModule.class })
public interface BingoApplicationComponent {

    void inject(MainActivity mainActivity);
    void inject(GameActivity gameActivity);
    void inject(SplashActivity splashActivity);

}
