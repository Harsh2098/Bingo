package com.hmproductions.bingo.dagger;

import com.hmproductions.bingo.ui.MainActivity;

import dagger.Component;

@BingoApplicationScope
@Component (modules = {StubModule.class, ChannelModule.class, ContextModule.class })
public interface BingoApplicationComponent {

    void inject(MainActivity mainActivity);
}
