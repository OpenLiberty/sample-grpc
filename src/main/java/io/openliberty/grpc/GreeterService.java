package io.openliberty.grpc;

import javax.annotation.security.RolesAllowed;

import io.grpc.InternalStatus;
import io.grpc.stub.StreamObserver;
import io.openliberty.grpc.interceptors.GreeterServiceInterceptor;
import io.openliberty.sample.GreetingRequest;
import io.openliberty.sample.GreetingResponse;
import io.openliberty.sample.SampleServiceGrpc;

/**
 * Example of a gRPC service which can be deployed on Liberty. This service will return a simple greeting string.
 */
public class GreeterService extends SampleServiceGrpc.SampleServiceImplBase {

    // required public zero-arg constructor
    public GreeterService(){}

    @Override
    @RolesAllowed("Role1")
    public void getGreeting(GreetingRequest req, StreamObserver<GreetingResponse> resp) {
        System.out.println(InternalStatus.MESSAGE_KEY);
        StringBuilder builder = new StringBuilder();
        builder.append("Hello ");
        builder.append(req.getName());

        // get the context value set for GreeterClientInterceptor by GreeterServiceInterceptor
        // and add it to the response
        Object clientHostFromContext = GreeterServiceInterceptor.CLIENT_HOST_KEY.get();
        if (clientHostFromContext != null) {
            builder.append(" (");
            builder.append(clientHostFromContext.toString());
            builder.append(")");
        }

        // get the context value set by GreeterServiceInterceptor
        // and add it to the response
        Object timeFromContext = GreeterServiceInterceptor.CURRENT_TIME_KEY.get();
        if (timeFromContext != null) {
            builder.append("; the current time is ");
            builder.append(timeFromContext.toString());
        }

        // return the greeting to the client
        GreetingResponse responseString = GreetingResponse.newBuilder().setMessage(builder.toString()).build();
        resp.onNext(responseString);
        resp.onCompleted();
    }
}
