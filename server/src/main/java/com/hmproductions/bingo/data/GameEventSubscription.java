package com.hmproductions.bingo.data;

import com.hmproductions.bingo.datastreams.GameEventUpdate;
import com.hmproductions.bingo.models.GameSubscription;

import io.grpc.stub.StreamObserver;

public class GameEventSubscription {

    private StreamObserver<GameEventUpdate> observer;
    private GameSubscription gameSubscription;

    public GameEventSubscription(StreamObserver<GameEventUpdate> observer, GameSubscription gameSubscription) {
        this.observer = observer;
        this.gameSubscription = gameSubscription;
    }

    public StreamObserver<GameEventUpdate> getObserver() {
        return observer;
    }

    public GameSubscription getGameSubscription() {
        return gameSubscription;
    }
}
