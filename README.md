# Sample for gRPC on OpenLiberty

This is a simple demo of the gRPC capabilities provided by OpenLiberty.

To run, clone this repository, and then run:
`mvn clean package liberty:run`

Then browse to:
http://localhost:9080/libertyGrpcSample/grpcClient

Auth will be enforced if `appSecurity-3.0` is enabled [server.xml](src/main/liberty/config/server.xml). Login credentials are managed in the same file.
```
