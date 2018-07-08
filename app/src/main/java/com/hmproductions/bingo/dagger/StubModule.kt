package com.hmproductions.bingo.dagger

import com.hmproductions.bingo.BingoActionServiceGrpc
import com.hmproductions.bingo.BingoActionServiceGrpc.BingoActionServiceBlockingStub
import com.hmproductions.bingo.BingoStreamServiceGrpc
import com.hmproductions.bingo.BingoStreamServiceGrpc.BingoStreamServiceStub
import com.hmproductions.bingo.utils.Constants
import com.hmproductions.bingo.utils.Constants.SESSION_ID_KEY
import dagger.Module
import dagger.Provides
import io.grpc.ManagedChannel
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils

@Module(includes = [(ChannelModule::class)])
class StubModule {

    @Provides
    @BingoApplicationScope
    fun getActionServiceBlockingStub(channel: ManagedChannel): BingoActionServiceBlockingStub =
            MetadataUtils.attachHeaders<BingoActionServiceBlockingStub>(BingoActionServiceGrpc.newBlockingStub(channel), getStubMetadata())

    @Provides
    @BingoApplicationScope
    fun getStreamServiceStub(channel: ManagedChannel): BingoStreamServiceStub =
            MetadataUtils.attachHeaders<BingoStreamServiceStub>(BingoStreamServiceGrpc.newStub(channel), getStubMetadata())

    private fun getStubMetadata(): Metadata {
        val metadata = Metadata()
        val metadataKey = Metadata.Key.of(SESSION_ID_KEY, Metadata.ASCII_STRING_MARSHALLER)
        metadata.put(metadataKey, Constants.SESSION_ID)
        return metadata
    }
}
