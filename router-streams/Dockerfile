FROM openjdk:8-jdk-alpine
RUN apk update && apk add --no-cache gcompat
VOLUME /tmp
COPY target/router-streams-0.0.1-SNAPSHOT.jar router-streams-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/router-streams-0.0.1-SNAPSHOT.jar"]

