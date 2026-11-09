FROM maven:3.9.9-eclipse-temurin-11 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

FROM quay.io/wildfly/wildfly:26.1.3.Final-jdk11

USER root
RUN mkdir -p /opt/jboss/wildfly/modules/system/layers/base/com/mysql/main \
    && curl -fsSL -o /opt/jboss/wildfly/modules/system/layers/base/com/mysql/main/mysql-connector-j-8.4.0.jar \
       https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar
COPY docker/mysql-module.xml /opt/jboss/wildfly/modules/system/layers/base/com/mysql/main/module.xml
COPY docker/configure-wildfly.cli /opt/jboss/wildfly/bin/configure-wildfly.cli
RUN chown -R jboss:root /opt/jboss/wildfly/modules/system/layers/base/com/mysql \
    /opt/jboss/wildfly/bin/configure-wildfly.cli

USER jboss
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/bin/configure-wildfly.cli
COPY --from=build /build/target/upr14.war /opt/jboss/wildfly/standalone/deployments/upr14.war

EXPOSE 8080
