FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/transformer-streams-0.0.1-SNAPSHOT.jar transformer-streams-0.0.1-SNAPSHOT.jar
COPY ssl/* /tmp
ENTRYPOINT ["java", "-jar", "/transformer-streams-0.0.1-SNAPSHOT.jar"]