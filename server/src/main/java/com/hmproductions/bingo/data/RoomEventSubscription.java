package com.hmproductions.bingo.data;

import com.hmproductions.bingo.datastreams.RoomEventUpdate;
import com.hmproductions.bingo.models.Subscription;

import io.grpc.stub.StreamObserver;

public class RoomEventSubscription {

    private StreamObserver<RoomEventUpdate> observer;
    private Subscription subscription;

    public RoomEventSubscription(StreamObserver<RoomEventUpdate> observer, Subscription subscription) {
        this.observer = observer;
        this.subscription = subscription;
    }

    public StreamObserver<RoomEventUpdate> getObserver() {
        return observer;
    }

    public Subscription getSubscription() {
        return subscription;
    }
}
