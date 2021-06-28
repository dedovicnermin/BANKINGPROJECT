FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/persistence-0.0.1-SNAPSHOT.jar persistence-0.0.1-SNAPSHOT.jar
COPY ssl/* /tmp
ENTRYPOINT ["java", "-jar", "/persistence-0.0.1-SNAPSHOT.jar"]