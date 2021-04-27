#FROM openliberty/open-liberty:kernel-slim-java8-openj9-ubi
FROM open-liberty

# Add Liberty server configuration including all necessary features
COPY --chown=1001:0  server.xml /config/

#RUN features.sh

COPY --chown=1001:0  libertyGrpcSample.war /config/dropins/
COPY --chown=1001:0 tls.crt /etc/x509/certs/tls.crt
COPY --chown=1001:0 tls.key /etc/x509/certs/tls.key

USER root
RUN keytool -import -noprompt -v -trustcacerts -alias <<CN>> -file /etc/x509/certs/tls.crt -keystore $JAVA_HOME/lib/security/cacerts -keypass changeit -storepass changeit
USER 1001

ENV SEC_TLS_TRUSTDEFAULTCERTS=true
ENV SEC_IMPORT_K8S_CERTS=true

#RUN configure.sh