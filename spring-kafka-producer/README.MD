# Spring Kafka Producer

### Budowanie i uruchamianie aplikacji

```bash
    mvn clean package
```

```bash
    docker build -t spring-kafka-producer .   
```

```bash
  docker run --network  mynetwork spring-kafka-producer
```

### Przygotowanie środowiska

```bash
    docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic spring-kafka-producer-topic --partitions 3 --replication-factor 3 --if-not-exists --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

### Zaimplementuj konfigurację producenta w klasie KafkaProducerConfig

```java
@Bean
public ProducerFactory<String, Transaction> producerFactory() {
    Map<String, Object> producerProperties =  new HashMap<>();
    producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(producerProperties);
}
```
```java
@Bean
public KafkaTemplate<String, Transaction> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
}
``` 

### Zaimplementuj wysyłanie wiadomości w klasie SenderJob wysyłanie wiadomości

```java
@Scheduled(fixedRate = 5000)
public void schedule() {
    Transaction crypto = new Transaction(UUID.randomUUID(), "CRYPTO", BigDecimal.valueOf(Math.random()));
    kafkaTemplate.send(TOPIC, UUID.randomUUID().toString(), crypto);
    Transaction stock = new Transaction(UUID.randomUUID(), "STOCK", BigDecimal.valueOf(Math.random()));
    kafkaTemplate.send(TOPIC, UUID.randomUUID().toString(), stock);
}
```

### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t spring-kafka-producer .   
    docker run --network  mynetwork spring-kafka-producer
```
    