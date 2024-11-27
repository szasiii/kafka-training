FROM maven:3.8.4-openjdk-17

COPY . /project
WORKDIR /project

ENTRYPOINT ["java", "-jar", "target/java-kafka-producer-1.0-SNAPSHOT-jar-with-dependencies.jar"]