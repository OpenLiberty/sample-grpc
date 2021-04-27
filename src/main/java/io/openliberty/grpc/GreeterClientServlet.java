package io.openliberty.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.openliberty.sample.GreetingRequest;
import io.openliberty.sample.GreetingResponse;
import io.openliberty.sample.SampleServiceGrpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Example of a Servlet making outbound gRPC calls during service
 */
@WebServlet(name = "grpcClient", urlPatterns = { "/grpcClient" })
public class GreeterClientServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(GreeterClientServlet.class.getName());

    /**
     * Build the gRPC channel needed for RPCs.
     * TLS is used if port 9443 or 443 is specified.
     */
    private ManagedChannel createChannel(ManagedChannel channel, String address, int port) {
        if (port != 9443 && port != 443) {
            return ManagedChannelBuilder
                .forAddress(address, port)
                .usePlaintext()
                .build();
        } else {
            return ManagedChannelBuilder
                .forAddress(address, port)
                .build();
        }
    }

    /**
     * Stop the gRPC channel
     */
    private void stopService(ManagedChannel channel) {
        channel.shutdownNow();
    }

    @Override
    protected void doGet(HttpServletRequest reqest, HttpServletResponse response) 
            throws ServletException, IOException {

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.append("<!DOCTYPE html>\r\n")
            .append("<html>\r\n")
            .append("		<head>\r\n")
            .append("			<title>gRPC Client</title>\r\n")
            .append("		</head>\r\n")
            .append("		<body>\r\n")
            .append("			<h3>gRPC Servlet client example</h3>\r\n")
            .append("			<form action=\"grpcClient\" method=\"POST\">\r\n")
            .append("				Enter your name: \r\n")
            .append("				<input type=\"text\" name=\"user\" />\r\n\r\n")
            .append("				<br/>")
            .append("				Enter the address of the target service: \r\n")
            .append("				<input type=\"text\" value=\"localhost\" name=\"address\" />\r\n\r\n")
            .append("				<br/>")
            .append("				Enter the port of the target service: \r\n")
            .append("				<input type=\"text\" value=\"9080\" name=\"port\" />\r\n\r\n")
            .append("				<br/>")
            .append("				<br/>")
            .append("				<input type=\"submit\" value=\"Submit\" />\r\n")
            .append("			</form>\r\n")
            .append("		</body>\r\n")
            .append("</html>\r\n");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String user = request.getParameter("user");
        String address = request.getParameter("address");
        int port = Integer.parseInt(request.getParameter("port"));
        ManagedChannel channel = null;
        SampleServiceGrpc.SampleServiceBlockingStub greetingService = null;

        try {
            channel = createChannel(channel, address, port);
            greetingService = SampleServiceGrpc.newBlockingStub(channel);

            // client side of the gRPC service is accessed via this servlet
            // create a gRPC User message to send to the server side service
            // the User class is derived from the HelloWorld.proto file being compiled into java code		
            GreetingRequest person = GreetingRequest.newBuilder().setName(user).build();
    
            // greetingService class is derived from HelloWorld.proto file being compiled into java code
            // Remote Procedure Call the greetUser method on the greetingService, more than one gRPC
            // message can be return, so read all responses into an Iterator
            GreetingResponse greeting = greetingService.getGreeting(person);
    
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            
            // create HTML response
            PrintWriter writer = response.getWriter();
            writer.append("<!DOCTYPE html>\r\n")
                .append("<html>\r\n")
                .append("		<head>\r\n")
                .append("			<title>Welcome message</title>\r\n")
                .append("		</head>\r\n")
                .append("		<body>\r\n");
            if (user != null && !user.trim().isEmpty()) {
                writer.append("<h3>gRPC service response</h3>\r\n");
                writer.append(greeting.toString());
            } else {
                writer.append("	You did not enter a name!\r\n");
            }
            writer.append("\r\n")
                .append("				<br/>\r\n")
                .append("				<br/>\r\n")
                .append("<form><input type=\"button\" value=\"Go back!\" onclick=\"history.back()\"></form>")
                .append("		</body>\r\n")
                .append("</html>\r\n");
        } catch (Exception e) {
            logger.info("Exception servicing gRPC request: " + e);
            PrintWriter writer = response.getWriter();
            writer.append("<!DOCTYPE html>\r\n")
                .append("<html>\r\n")
                .append("		<head>\r\n")
                .append("			<title>Welcome message</title>\r\n")
                .append("		</head>\r\n")
                .append("		<body>\r\n")
                .append("<h3>gRPC service Exception</h3>\r\n")
                .append(e.toString())
                .append(e.getMessage())
                .append("\r\n")
                .append("				<br/>\r\n")
                .append("				<br/>\r\n")
                .append("<form><input type=\"button\" value=\"Go back!\" onclick=\"history.back()\"></form>")
                .append("		</body>\r\n")
                .append("</html>\r\n");
            throw e;
        }finally {
            stopService(channel);
        }
    }
}