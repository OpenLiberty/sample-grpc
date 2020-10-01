package io.openliberty.grpc.interceptors;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * A simple gRPC ClientInterceptor which grabs the current host and sets in in a "Client-Host" header
 */
public class GreeterClientInterceptor implements ClientInterceptor {

    private static final Logger logger = Logger.getLogger(GreeterClientInterceptor.class.getName());
    static final Metadata.Key<String> HOST_KEY = Metadata.Key.of("Client-Host", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel next) {

        return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                logger.info("GreeterClientInterceptor invoked");
                try {
                    headers.put(HOST_KEY, InetAddress.getLocalHost().getHostName());
                } catch (UnknownHostException e) {
                    logger.info("GreeterClientInterceptor failed to add host due to " + e);
                }
                super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {}, headers);
            }
        };
    }
}


