FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/rest-0.0.1-SNAPSHOT.jar rest-0.0.1-SNAPSHOT.jar
COPY ssl/* /tmp
ENTRYPOINT ["java", "-jar", "/rest-0.0.1-SNAPSHOT.jar"]