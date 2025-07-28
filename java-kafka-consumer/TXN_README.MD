# Przygotowanie środowiska

```bash
   docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic kafka-client-txn --partitions 3 --replication-factor 3 --if-not-exists --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

```java
public static KafkaConsumer<String, String> createConsumer() {
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "java-kafka-clients-consumer");
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    properties.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    return new KafkaConsumer<>(properties);
}
```

### Zaimplementuj polling wiadomości z topicu w nieskończonej pętli

```java
public static void main(String[] args) {
    KafkaConsumer<String, String> consumer = createConsumer();
    consumer.subscribe(List.of("kafka-client-txn"));
    while (true) {
        consumer.poll(Duration.ofMillis(100)).forEach(record -> 
                logger.info("Key: {}, Value: {}, Partition: {}, Offset: {}", record.key(), record.value(), record.partition(), record.offset()));
        consumer.commitSync();
    }
}
```

### Przebuduj i uruchom aplikację
```bash
  mvn clean package
  docker build -t java-kafka-consumer .   
  docker run --network  mynetwork java-kafka-consumer
```


# ENG

# Environment Setup

```bash
docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-topics \
--create --topic kafka-client-txn \
--partitions 3 --replication-factor 3 \
--if-not-exists \
--bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

```java
public static KafkaConsumer<String, String> createConsumer() {
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "java-kafka-clients-consumer");
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    properties.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    return new KafkaConsumer<>(properties);
}
```

### Implement polling of messages from the topic in an infinite loop

```java
public static void main(String[] args) {
    KafkaConsumer<String, String> consumer = createConsumer();
    consumer.subscribe(List.of("kafka-client-txn"));
    while (true) {
        consumer.poll(Duration.ofMillis(100)).forEach(record ->
            logger.info("Key: {}, Value: {}, Partition: {}, Offset: {}", 
                        record.key(), record.value(), record.partition(), record.offset()));
        consumer.commitSync();
    }
}
```

### Rebuild and run the application

```bash
mvn clean package
docker build -t java-kafka-consumer .
docker run --network mynetwork java-kafka-consumer
```
