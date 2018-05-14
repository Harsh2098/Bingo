package com.hmproductions.bingo.dagger;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.utils.Constants;

import dagger.Module;
import dagger.Provides;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

@Module (includes = ChannelModule.class)
public class StubModule {

    @Provides
    @BingoApplicationScope
    public BingoActionServiceGrpc.BingoActionServiceBlockingStub getActionServiceStub(ManagedChannel channel){
        BingoActionServiceGrpc.BingoActionServiceBlockingStub stub = BingoActionServiceGrpc.newBlockingStub(channel);
        return MetadataUtils.attachHeaders(stub, getStubMetadata());
    }
    
    private Metadata getStubMetadata() {
        Metadata metadata = new Metadata();
        Metadata.Key<String> metadataKey = Metadata.Key.of("sessionid", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(metadataKey, Constants.SAMPLE_SESSION_ID);
        return metadata;
    }
}
