package com.hmproductions.bingo.data;

import com.hmproductions.bingo.datastreams.GameEventUpdate;
import com.hmproductions.bingo.models.GameSubscription;

import io.grpc.stub.StreamObserver;

public class GameEventSubscription {

    private int playerId;
    private StreamObserver<GameEventUpdate> observer;
    private GameSubscription gameSubscription;

    public GameEventSubscription(int playerId, StreamObserver<GameEventUpdate> observer, GameSubscription gameSubscription) {
        this.playerId = playerId;
        this.observer = observer;
        this.gameSubscription = gameSubscription;
    }

    public StreamObserver<GameEventUpdate> getObserver() {
        return observer;
    }

    public GameSubscription getGameSubscription() {
        return gameSubscription;
    }

    public int getPlayerId() {
        return playerId;
    }
}
