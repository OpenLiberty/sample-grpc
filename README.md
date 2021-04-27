# Sample for gRPC on OpenLiberty

This is a simple demo of the gRPC capabilities provided by OpenLiberty.

To run, clone this repository, and then run:
`mvn clean package liberty:run`

Then browse to:
http://localhost:9080/libertyGrpcSample/grpcClient

Auth will be enforced if `appSecurity-3.0` is enabled [server.xml](src/main/liberty/config/server.xml). Login credentials are managed in the same file.

# Running on OpenShift

To run this on OpenShift you'll need to first create a container image with this app, upload it to an image registry, and then deploy into OCP.  

These instructions are heavily based on [this blog](https://kesseract.github.io/post/grpc/), which does a great job in explaining the concepts behind the needed steps.

## Creating the container

You will need a certificate for this application, which will have to reference the wildcard hostname of your OCP cluster.  

The easiest way to obtain this hostname is via the following command:
```
oc get ingresses.config/cluster -o jsonpath='{.spec.domain}'
```

Just add the prefix of `*.` to create the wildcard.  Example: `*.apps.katamari-arthurdm.cp.fyre.ibm.com`

Once you have this value, you are ready to create the certificate.

Run the following command (use the value above for the `CN` field):
```
openssl req -newkey rsa:4096 -nodes -keyout docker-ocp/tls.key -x509 -days 365 -out docker-ocp/tls.crt
```

Assuming that you've run the `mvn` command locally and built the application, you should have the app inside `./target/libertyGrpcSample.war`.  Copy this app into the `docker-ocp` folder.

Now open the file `docker-ocp/Dockerfile` and search/replace the string `<<CN>>` with your CN value from above, which will be used to add your newly generated certificate with the Java set.  

You're now ready to build the container image. Please note that we're included a version of `server.xml` inside `docker-ocp` that already has the TLS relevant portions uncommented.  If you have made further changes to your `server.xml` you'll need to synchronize the changes.

The `Dockerfile` uses the implicit `full` tag for Open Liberty for convenience, since this is a sample.  For more serious deployments you should uncomment the 3 commented lines in the `Dockerfile` that utilize the `kernel` tag and run internal scripts for feature loading and caching of classes.

Build with image:
```
cd docker-ocp
docker build -t <image_registry>/grpc:wildcard .
```

You have to replace `<image_registry>` with the image registry of your choice, for example: Docker Hub, Quay.io, OCP's internal registry, etc.

The final container related step is to `docker push` your image into the image registry you have chosen.  

## Deploying on OCP

You first need to enable `http2` in your OCP Router.  You can do so for the entire cluster via:
```
oc annotate ingresses.config/cluster ingress.operator.openshift.io/default-enable-http2=true
```

The easiest way to deploy your image into OCP is via the [Open Liberty Operator](https://github.com/OpenLiberty/open-liberty-operator). Using OCP's UI, navigate to the embedded Operator Hub, search for `Open Liberty Operator` and follow the instructions to install it.

You can then deploy your app using a  `OpenLibertyApplication` CR.  First edit the `docker-ocp/grpc.yaml` file's `applicationImage` field to point to the fully qualified image location from the previous section.  To deploy the application you can run the following command:
```
oc apply -f docker-ocp/grpc.yaml
```

Once deployed, click on the newly created Route using the UI or use `oc get route` to find the Route URL. 

You should see the Open Liberty welcome page.  The current URL is what you'll use in the next form, (minus the `http://` portion), so save that URL as `address`. 

Add `libertyGrpcSample/grpcClient` to the URL to get to the main form.  For address you'll use the `address` value stored previously, and for port use `443`.  Add your name, hit submit, and you should see the output!