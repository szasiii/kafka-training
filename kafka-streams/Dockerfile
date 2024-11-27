FROM maven:3.8.4-openjdk-17

COPY . /project
WORKDIR /project

ENTRYPOINT ["java", "-jar", "target/kafka-streams-1.0-SNAPSHOT.jar"]