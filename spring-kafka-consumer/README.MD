# Spring Kafka Consumer

### Budowanie i uruchamianie aplikacji

```bash
    mvn clean package
```

```bash
    docker build -t spring-kafka-consumer .   
```

```bash
  docker run --network  mynetwork spring-kafka-consumer
```

### Skonfiguruj globalne ustawienia aplikacji w pliku application.yml

```yaml
spring:
  kafka:
    listener:
      ack-mode: manual
    bootstrap-servers: kafka-1:29092,kafka-2:39092,kafka-3:49092
    consumer:
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring:
          json:
            trusted:
              packages: "com.sages"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

| **Tryb potwierdzenia (Ack Mode)** | **Opis**                                                                                           | **Kiedy używać**                                                                                                  |
|-----------------------------------|---------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| **`RECORD`**                     | Zatwierdza offset po przetworzeniu każdego rekordu.                                               | Gdy czas przetwarzania jest minimalny i wymagane jest dokładne zatwierdzanie po każdym rekordzie.                |
| **`BATCH`**                      | Zatwierdza offsety po przetworzeniu partii rekordów zwróconych przez jedno `poll()`.              | Odpowiedni dla przetwarzania partii, gdzie priorytetem jest wydajność nad szczegółowością.                       |
| **`TIME`**                       | Zatwierdza offsety okresowo, w oparciu o interwał czasowy (skonfigurowany w `ackTime`).           | Używane w systemach, gdzie wymagane są regularne zatwierdzenia niezależnie od czasu przetwarzania.               |
| **`COUNT`**                      | Zatwierdza offsety po przetworzeniu określonej liczby rekordów (skonfigurowane w `ackCount`).     | Idealne w systemach, gdzie offsety powinny być zatwierdzane po przetworzeniu stałej liczby rekordów.             |
| **`COUNT_TIME`**                 | Łączy tryby `TIME` i `COUNT`, zatwierdzając offsety po określonym czasie lub liczbie rekordów.    | Używane w elastycznych scenariuszach wymagających zarówno liczby rekordów, jak i warunków czasowych.             |
| **`MANUAL`**                     | Wymaga ręcznego zatwierdzenia przez dewelopera za pomocą obiektu `Acknowledgment`.               | Używane dla precyzyjnej kontroli nad zatwierdzeniami offsetów, np. w scenariuszach z obsługą błędów.             |
| **`MANUAL_IMMEDIATE`**           | Podobne do `MANUAL`, ale zatwierdza offsety natychmiast po wywołaniu `acknowledge()`.             | W scenariuszach wymagających natychmiastowego zatwierdzania offsetów w czasie rzeczywistym.                      |


### Zaimplementuj klasę `TrainingKafkaListenerSimple.class` przy użyciu adnotacji @KafkaListener

```java
private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaListenerSimple.class);


@KafkaListener(topics = "spring-kafka-producer-topic", groupId = "simple-listener")
public void listen(Transaction transaction, Acknowledgment acknowledgment) {
    log.info("Received {} transaction with thread {}", transaction.toString(), Thread.currentThread().getId());
    acknowledgment.acknowledge();
}
```

### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```

### Batch Listener

### Zaimplementuj w klasie `KafkaConfig.class` konfigurację dla batch listenera

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, Transaction> batchKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Transaction> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setBatchListener(true);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
    return factory;
}
```

### Zaimplementuj klasę `TrainingKafkaListenerBatch.class` przy użyciu adnotacji @KafkaListener

```java
private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaBatchListener.class);

@KafkaListener(
        topics = "spring-kafka-producer-topic",
        groupId = "my-group-batch",
        containerFactory = "batchKafkaListenerContainerFactory"
)
public void listenBatch(List<Transaction> transactions) {
    log.info("Received batch of {} transactions", transactions.size());
}
```
### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```

### Można prościej

```java
@KafkaListener(
        topics = "spring-kafka-producer-topic",
        groupId = "my-group-batch",
        batch = "true"
)
public void listenBatch(List<Transaction> transactions) {
    log.info("Received batch of {} transactions", transactions.size());
}
```
### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```

### Zaimplementuj w klasie `TrainingKafkaSelectiveListener.class` pobieranie tylko z 2 partycji

```java
private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaSelectiveListener.class);

@KafkaListener(
        topicPartitions = {
                @TopicPartition(topic = "spring-kafka-producer-topic", partitions = {"2"})
        }
)
public void listen(Transaction transaction, ConsumerRecordMetadata metadata) {
    log.info("Received {} transaction from partition {}", transaction.toString(), metadata.partition());
}
```

### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```


### Zaimplementuj w klasie `KafkaConfig.class` konfiguacje dla filtrowania

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, Transaction> filterKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Transaction> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setRecordFilterStrategy(consumerRecord -> {
        Transaction transaction = consumerRecord.value();
        return transaction.getSource().equals("STOCK"); // Filter OUT!
    });

    return factory;
}
```


### Zaimplementuj w klasie `TrainingKafkaFilteringListener.class` listener z filtrowaniem

```java
private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaFilteringListener.class);

@KafkaListener(
        topics = "spring-kafka-producer-topic",
        groupId = "my-group-filtering",
        containerFactory = "filterKafkaListenerContainerFactory"
)
public void listenBatch(Transaction transaction) {
    log.info("Received {} transaction with thread {}", transaction.toString(), Thread.currentThread().getId());
}
```

### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```


### Zaimplementuj w klasie `KafkaConfig.class` konfigurację dla obsługi błędów

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, Transaction> kafkaListenerContainerFactoryWithErrorHandler() {
    ConcurrentKafkaListenerContainerFactory<String, Transaction> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setCommonErrorHandler(new DefaultErrorHandler((record, exception) -> 
        log.error("Failed to process record: {} will apply \"custom\" behaviour", record, exception), new FixedBackOff(1000L, 3L)));
    return factory;
}
```

### Zaimplementuj w klasie `TrainingKafkaCustomErrorHandlingListener.class` sztuczny błąd

```java
@KafkaListener(
        topics = "spring-kafka-producer-topic",
        groupId = "my-group-custom-error-handling",
        containerFactory = "kafkaListenerContainerFactoryWithErrorHandler"
)
public void listenBatch(Transaction transaction) {
    throw new RuntimeException("Custom error handling exception");
}
```

### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```

### Zaimplementuj w klasie `KafkaConfig.class` wiele listenerów przy pomocy parametry concurrency

```java
@KafkaListener(
        topics = "spring-kafka-producer-topic",
        groupId = "my-group-concurrency",
        concurrency = "3"
)
public void listenBatch(Transaction transaction) {
    log.info("Received {} transaction with thread {}", transaction.toString(), Thread.currentThread().getId());
}
```

### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```
# Dead Letter Queue

| **Funkcja**                     | **Opis**                                                                                                                                         | **Dlaczego używać?**                                                                                     |
|---------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| **Bezpieczeństwo danych**       | Zapewnia przechowywanie nieprzetworzonych wiadomości, które mogłyby zostać utracone.                                                             | Chroni dane przed utratą w przypadku błędów w przetwarzaniu.                                             |
| **Odciążenie systemu**          | Odrzuca problematyczne wiadomości, umożliwiając kontynuację przetwarzania innych danych.                                                          | Pozwala systemowi skupić się na danych możliwych do przetworzenia.                                       |
| **Diagnostyka i analiza**       | Przechowuje nieprzetworzone wiadomości z dodatkowymi informacjami, np. przyczyną błędu.                                                          | Ułatwia identyfikację problemów z danymi lub logiką systemu.                                             |
| **Kontrola błędów**             | Umożliwia oddzielenie "problemowych" wiadomości od głównego procesu przetwarzania.                                                               | Zwiększa stabilność systemu, minimalizując przestoje spowodowane błędami.                                |
| **Elastyczność obsługi błędów** | Pozwala na ręczne przetwarzanie, automatyczne poprawki lub analizę problematycznych wiadomości w późniejszym czasie.                              | Daje większą kontrolę nad sposobem obsługi błędów i dopasowanie do potrzeb biznesowych.                  |

### Zaimplementuj w klasie `TrainingKafkaRetryableDLQListener.class` obsługę błędów z Dead Letter Queue wraz z nieblokującym przetwarzaniem retry

```java
private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaRetryableDLQListener.class);

@RetryableTopic(
        attempts = "5",
        backoff = @Backoff(delay = 2000, multiplier = 2.0),
        include = RuntimeException.class
)
@KafkaListener(topics = "spring-kafka-producer-topic", groupId = "retryable-group-with-dlq")
public void listen(Transaction transaction) {
    log.info("Received {} transaction with thread {}", transaction.toString(), Thread.currentThread().getId());
    throw new RuntimeException("Simulated exception");
}

@DltHandler
public void handleDeadLetterQueue(Transaction record) {
    log.error("Message sent to DLQ for manual inspection: {}", record);
}
```

### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```

# ENG

# Spring Kafka Consumer

### Building and Running the Application

```bash
    mvn clean package
```

```bash
    docker build -t spring-kafka-consumer .   
```

```bash
  docker run --network  mynetwork spring-kafka-consumer
```

### Configure global application settings in the `application.yml` file

```yaml
spring:
  kafka:
    listener:
      ack-mode: manual
    bootstrap-servers: kafka-1:29092,kafka-2:39092,kafka-3:49092
    consumer:
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring:
          json:
            trusted:
              packages: "com.sages"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```
| **Ack Mode**               | **Description**                                                                                          | **When to use**                                                                                                   |
|----------------------------|----------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| **`RECORD`**              | Commits the offset after processing each record.                                                         | When per-record processing time is minimal and exact, after-each-record acknowledgments are required.             |
| **`BATCH`**               | Commits offsets after processing a batch of records returned by a single `poll()`.                       | For batch processing where throughput is prioritized over per-record granularity.                                 |
| **`TIME`**                | Commits offsets periodically based on a configured time interval (`ackTime`).                           | In systems requiring regular commits regardless of processing duration per record.                               |
| **`COUNT`**               | Commits offsets after processing a configured number of records (`ackCount`).                            | Ideal when you want to commit after a fixed number of records have been processed.                                |
| **`COUNT_TIME`**          | Combines `TIME` and `COUNT`, committing offsets after either the time interval or the record count.      | For flexible scenarios requiring both time‑based and count‑based commits.                                          |
| **`MANUAL`**              | Requires manual acknowledgment by the developer via the `Acknowledgment` object.                          | When fine‑grained control over offset commits is needed, e.g., for custom error‑handling scenarios.               |
| **`MANUAL_IMMEDIATE`**    | Similar to `MANUAL`, but commits the offset immediately upon calling `acknowledge()`.                    | In scenarios requiring immediate, real‑time offset commits.                                                       |

### Implement the `TrainingKafkaListenerSimple.class` class using the @KafkaListener annotation

```java
private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaListenerSimple.class);


@KafkaListener(topics = "spring-kafka-producer-topic", groupId = "simple-listener")
public void listen(Transaction transaction, Acknowledgment acknowledgment) {
    log.info("Received {} transaction with thread {}", transaction.toString(), Thread.currentThread().getId());
    acknowledgment.acknowledge();
}
```

### Rebuild and run the application

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```

### Batch Listener

### Implement the `KafkaConfig.class` class configuration for the batch listener

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, Transaction> batchKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Transaction> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setBatchListener(true);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
    return factory;
}
```

### Implement the `TrainingKafkaListenerBatch.class` class using the @KafkaListener annotation

```java
private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaBatchListener.class);

@KafkaListener(
        topics = "spring-kafka-producer-topic",
        groupId = "my-group-batch",
        containerFactory = "batchKafkaListenerContainerFactory"
)
public void listenBatch(List<Transaction> transactions) {
    log.info("Received batch of {} transactions", transactions.size());
}
```
### Rebuild and run the application

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```

### Can be simpler

```java
@KafkaListener(
        topics = "spring-kafka-producer-topic",
        groupId = "my-group-batch",
        batch = "true"
)
public void listenBatch(List<Transaction> transactions) {
    log.info("Received batch of {} transactions", transactions.size());
}
```
### Rebuild and run the application

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```

### Implement the `TrainingKafkaSelectiveListener.class` class to consume only from partition 2

```java
private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaSelectiveListener.class);

@KafkaListener(
        topicPartitions = {
                @TopicPartition(topic = "spring-kafka-producer-topic", partitions = {"2"})
        }
)
public void listen(Transaction transaction, ConsumerRecordMetadata metadata) {
    log.info("Received {} transaction from partition {}", transaction.toString(), metadata.partition());
}
```

### Rebuild and run the application

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```


### Implement the `KafkaConfig.class` class configuration for filtering

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, Transaction> filterKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Transaction> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setRecordFilterStrategy(consumerRecord -> {
        Transaction transaction = consumerRecord.value();
        return transaction.getSource().equals("STOCK"); // Filter OUT!
    });

    return factory;
}
```


### Implement the `TrainingKafkaFilteringListener.class` class with filtering

```java
private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaFilteringListener.class);

@KafkaListener(
        topics = "spring-kafka-producer-topic",
        groupId = "my-group-filtering",
        containerFactory = "filterKafkaListenerContainerFactory"
)
public void listenBatch(Transaction transaction) {
    log.info("Received {} transaction with thread {}", transaction.toString(), Thread.currentThread().getId());
}
```

### Rebuild and run the application

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```


### Implement the `KafkaConfig.class` class configuration for error handling

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, Transaction> kafkaListenerContainerFactoryWithErrorHandler() {
    ConcurrentKafkaListenerContainerFactory<String, Transaction> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setCommonErrorHandler(new DefaultErrorHandler((record, exception) -> 
        log.error("Failed to process record: {} will apply \"custom\" behaviour", record, exception), new FixedBackOff(1000L, 3L)));
    return factory;
}
```

### Implement the `TrainingKafkaCustomErrorHandlingListener.class` class with a simulated error

```java
@KafkaListener(
        topics = "spring-kafka-producer-topic",
        groupId = "my-group-custom-error-handling",
        containerFactory = "kafkaListenerContainerFactoryWithErrorHandler"
)
public void listenBatch(Transaction transaction) {
    throw new RuntimeException("Custom error handling exception");
}
```

### Rebuild and run the application

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```

### Implement the `KafkaConfig.class` class configuration for multiple listeners with concurrency

```java
@KafkaListener(
        topics = "spring-kafka-producer-topic",
        groupId = "my-group-concurrency",
        concurrency = "3"
)
public void listenBatch(Transaction transaction) {
    log.info("Received {} transaction with thread {}", transaction.toString(), Thread.currentThread().getId());
}
```

### Rebuild and run the application

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```
# Dead Letter Queue

| **Feature**                       | **Description**                                                                                                      | **Why use it?**                                                                                                 |
|-----------------------------------|----------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| **Data safety**                   | Stores unprocessed messages that might otherwise be lost.                                                            | Protects data from loss in case of processing errors.                                                           |
| **System resilience**             | Offloads problematic messages, allowing processing of other messages to continue.                                    | Allows the system to focus on processable data.                                                                  |
| **Diagnostics & analysis**        | Keeps unprocessed messages along with additional information, such as the error cause.                               | Facilitates identification of data or logic issues.                                                             |
| **Error isolation**               | Separates “bad” messages from the main processing flow.                                                              | Improves system stability by minimizing downtime caused by errors.                                              |
| **Flexible error handling**       | Enables manual reprocessing, automated fixes, or later analysis of problematic messages.                            | Provides greater control over error‑handling strategies, tailored to business needs.                            |


### Implement the `TrainingKafkaRetryableDLQListener.class` class with Dead Letter Queue handling and non-blocking retry processing

```java
private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaRetryableDLQListener.class);

@RetryableTopic(
        attempts = "5",
        backoff = @Backoff(delay = 2000, multiplier = 2.0),
        include = RuntimeException.class
)
@KafkaListener(topics = "spring-kafka-producer-topic", groupId = "retryable-group-with-dlq")
public void listen(Transaction transaction) {
    log.info("Received {} transaction with thread {}", transaction.toString(), Thread.currentThread().getId());
    throw new RuntimeException("Simulated exception");
}

@DltHandler
public void handleDeadLetterQueue(Transaction record) {
    log.error("Message sent to DLQ for manual inspection: {}", record);
}
```

### Rebuild and run the application

```bash
    mvn clean package
    docker build -t spring-kafka-consumer .   
    docker run --network  mynetwork spring-kafka-consumer
```
