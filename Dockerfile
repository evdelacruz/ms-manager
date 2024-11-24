#Stage 1
FROM gradle:7-jre11 as builder
RUN mkdir /opt/service
RUN mkdir /opt/service/build
RUN mkdir /opt/service/src

WORKDIR /opt/service

COPY --chown=gradle:gradle src /opt/service/src
COPY --chown=gradle:gradle build.gradle /opt/service/build.gradle

RUN gradle -q shadowJar -x test

#Stage 2 Final
FROM openjdk:11-jre-slim

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS=""

RUN mkdir /opt/java-ms
#TODO for compitalions change name of jar
COPY --from=builder /opt/service/build/libs/*.jar /opt/java-ms/ms-exchanger.jar

#TODO port
EXPOSE 8080

#TODO for compitalions change name of jar
CMD java ${JAVA_OPTS} -server -XX:+UseParallelGC -XX:+UseNUMA -Djava.security.egd=file:/dev/./urandom -jar /opt/java-ms/ms-exchanger.jar

