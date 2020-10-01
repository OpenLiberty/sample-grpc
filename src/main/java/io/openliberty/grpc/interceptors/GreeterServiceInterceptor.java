package io.openliberty.grpc.interceptors;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * A simple gRPC ServerInterceptor which sets the current time and "clientHost" header values on the 
 * intercepted call's Context
 */
public class GreeterServiceInterceptor implements ServerInterceptor {

    public static final Context.Key<Object> CURRENT_TIME_KEY = Context.key("currentTime");
    public static final Context.Key<Object> CLIENT_HOST_KEY = Context.key("clientHost");

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                      ServerCallHandler<ReqT, RespT> next) {

        String currentTime = java.time.LocalTime.now().toString();
        String clientHost = headers.get(GreeterClientInterceptor.HOST_KEY);

        Context context = Context
                        .current()
                        .withValue(CURRENT_TIME_KEY, currentTime)
                        .withValue(CLIENT_HOST_KEY, clientHost);
        return Contexts.interceptCall(context, call, headers, next);
    }
}