package com.hmproductions.bingo.data;

import com.hmproductions.bingo.datastreams.RoomEventUpdate;
import com.hmproductions.bingo.models.RoomSubscription;

import io.grpc.stub.StreamObserver;

public class RoomEventSubscription {

    private StreamObserver<RoomEventUpdate> observer;
    private RoomSubscription subscription;

    public RoomEventSubscription(StreamObserver<RoomEventUpdate> observer, RoomSubscription subscription) {
        this.observer = observer;
        this.subscription = subscription;
    }

    public StreamObserver<RoomEventUpdate> getObserver() {
        return observer;
    }

    public RoomSubscription getSubscription() {
        return subscription;
    }
}
