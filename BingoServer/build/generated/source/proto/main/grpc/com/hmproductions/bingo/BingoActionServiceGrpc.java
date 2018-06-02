package com.hmproductions.bingo;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.11.0)",
    comments = "Source: BingoMessage.proto")
public final class BingoActionServiceGrpc {

  private BingoActionServiceGrpc() {}

  public static final String SERVICE_NAME = "com.hmproductions.bingo.BingoActionService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getGetGridSizeMethod()} instead. 
  public static final io.grpc.MethodDescriptor<com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest,
      com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse> METHOD_GET_GRID_SIZE = getGetGridSizeMethodHelper();

  private static volatile io.grpc.MethodDescriptor<com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest,
      com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse> getGetGridSizeMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest,
      com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse> getGetGridSizeMethod() {
    return getGetGridSizeMethodHelper();
  }

  private static io.grpc.MethodDescriptor<com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest,
      com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse> getGetGridSizeMethodHelper() {
    io.grpc.MethodDescriptor<com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest, com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse> getGetGridSizeMethod;
    if ((getGetGridSizeMethod = BingoActionServiceGrpc.getGetGridSizeMethod) == null) {
      synchronized (BingoActionServiceGrpc.class) {
        if ((getGetGridSizeMethod = BingoActionServiceGrpc.getGetGridSizeMethod) == null) {
          BingoActionServiceGrpc.getGetGridSizeMethod = getGetGridSizeMethod = 
              io.grpc.MethodDescriptor.<com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest, com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "com.hmproductions.bingo.BingoActionService", "GetGridSize"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new BingoActionServiceMethodDescriptorSupplier("GetGridSize"))
                  .build();
          }
        }
     }
     return getGetGridSizeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BingoActionServiceStub newStub(io.grpc.Channel channel) {
    return new BingoActionServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BingoActionServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new BingoActionServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static BingoActionServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new BingoActionServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class BingoActionServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void getGridSize(com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest request,
        io.grpc.stub.StreamObserver<com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetGridSizeMethodHelper(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetGridSizeMethodHelper(),
            asyncUnaryCall(
              new MethodHandlers<
                com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest,
                com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse>(
                  this, METHODID_GET_GRID_SIZE)))
          .build();
    }
  }

  /**
   */
  public static final class BingoActionServiceStub extends io.grpc.stub.AbstractStub<BingoActionServiceStub> {
    private BingoActionServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BingoActionServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BingoActionServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BingoActionServiceStub(channel, callOptions);
    }

    /**
     */
    public void getGridSize(com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest request,
        io.grpc.stub.StreamObserver<com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetGridSizeMethodHelper(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class BingoActionServiceBlockingStub extends io.grpc.stub.AbstractStub<BingoActionServiceBlockingStub> {
    private BingoActionServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BingoActionServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BingoActionServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BingoActionServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse getGridSize(com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetGridSizeMethodHelper(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class BingoActionServiceFutureStub extends io.grpc.stub.AbstractStub<BingoActionServiceFutureStub> {
    private BingoActionServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BingoActionServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BingoActionServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BingoActionServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse> getGridSize(
        com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetGridSizeMethodHelper(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_GRID_SIZE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final BingoActionServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(BingoActionServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_GRID_SIZE:
          serviceImpl.getGridSize((com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest) request,
              (io.grpc.stub.StreamObserver<com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class BingoActionServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    BingoActionServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.hmproductions.bingo.BingoMessage.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("BingoActionService");
    }
  }

  private static final class BingoActionServiceFileDescriptorSupplier
      extends BingoActionServiceBaseDescriptorSupplier {
    BingoActionServiceFileDescriptorSupplier() {}
  }

  private static final class BingoActionServiceMethodDescriptorSupplier
      extends BingoActionServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    BingoActionServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (BingoActionServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new BingoActionServiceFileDescriptorSupplier())
              .addMethod(getGetGridSizeMethodHelper())
              .build();
        }
      }
    }
    return result;
  }
}
