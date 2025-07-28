# Java Kafka Consumer

### Budowanie aplikacji i uruchmienie aplikacji

```bash
  mvn clean package
```

```bash
  docker build -t java-kafka-consumer .   
```

```bash
  docker run --network  mynetwork java-kafka-consumer
```

### Konfiguracja i uruchomienie Kafka Consumer

![img.png](img.png)

```java
public static KafkaConsumer<String, String> createConsumer() {
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "java-kafka-clients-consumer");
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    return new KafkaConsumer<>(properties);
}
```

### Zaimplementuj polling wiadomości z topicu w nieskończonej pętli

```java
public static void main(String[] args) {
    KafkaConsumer<String, String> consumer = createConsumer();
    consumer.subscribe(List.of("kafka-client-exercises"));
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

### Zatrzymaj aplikację i przejdźmy do Kafka UI

### Resetowanie offsetów grupy

```bash
  docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-consumer-groups --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092 --group java-kafka-clients-consumer --reset-offsets --to-earliest --execute --topic kafka-client-exercises
```

### Ustaw auto.offset.reset na latest

```java
public static KafkaConsumer<String, String> createConsumer() {
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "java-kafka-clients-consumer");
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    return new KafkaConsumer<>(properties);
}
```

### Przebuduj i uruchom aplikację
```bash
  mvn clean package
  docker build -t java-kafka-consumer .   
  docker run --network  mynetwork java-kafka-consumer
```

### Consumer Group i Rebalance

### Uruchom 2 dodatkowe instancje aplikacji w innych kartach terminala i za każdym razem obserwuj logi

```bash
  docker run --network  mynetwork java-kafka-consumer
```

### Problem auto skalowania i coupling między HTTP/GRPC a Kafka!!!

### STOP THE WORLD - REBALANCE
![img_1.png](img_1.png)

### Inkrementalny rebalance

![img_2.png](img_2.png)

### Alternatywne strategie przypisywania partycji

| **Strategia**             | **Opis**                                                                                                                                                         | **Kiedy używać?**                                                                                   |
|---------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| **RangeAssignor**         | Domyślna strategia. Partycje są przypisywane do konsumentów w ramach zakresu (range) w oparciu o alfabetyczną kolejność tematów i partycji. Konsument otrzymuje ciągły zakres partycji. | Gdy liczba partycji jest proporcjonalna do liczby konsumentów, a równomierne przypisanie nie jest krytyczne.                             |
| **RoundRobinAssignor**    | Partycje są przypisywane do konsumentów w sposób okrężny (round-robin). Każdy konsument otrzymuje po jednej partycji na zmianę, aż wszystkie zostaną przydzielone. | Gdy równomierne przypisanie partycji między konsumentami jest kluczowe, np. w systemach przetwarzania strumieniowego.                   |
| **StickyAssignor**        | Partycje są przypisywane w sposób równomierny z zachowaniem przypisania z poprzedniego rebalansowania, jeśli to możliwe.                                       | Gdy minimalizacja zmian przypisania partycji pomiędzy rebalansami jest ważna dla stabilności lub optymalizacji wydajności.              |
| **CooperativeStickyAssignor** | Wersja StickyAssignor, która pozwala na bardziej stopniowe rebalansowanie. Konsumenci mogą przejmować partycje w sposób incrementalny, minimalizując przestoje. | Gdy konieczne jest ograniczenie przestojów podczas rebalansowania, np. w systemach o wysokiej dostępności.                              |
| **Custom (własna strategia)** | Możesz zaimplementować własną strategię przypisywania poprzez rozszerzenie klasy `PartitionAssignor`.                                                         | Gdy żadna z dostępnych strategii nie spełnia wymagań, np. specyficzne reguły biznesowe dla przypisywania partycji.                      |

---

### Przykłady zastosowań

1. **RangeAssignor**:
      - Tematy z partycjami przypisanymi w naturalnej kolejności, np. przetwarzanie danych z logów.
      - Prosty podział w systemach z niewielką liczbą partycji i konsumentów.

2. **RoundRobinAssignor**:
      - Systemy wymagające równomiernego rozłożenia obciążenia, np. analizy danych w czasie rzeczywistym.

3. **StickyAssignor**:
      - Przetwarzanie krytycznych danych, gdzie zmiany w przypisaniach partycji mogą prowadzić do problemów z wydajnością.

4. **CooperativeStickyAssignor**:
      - Systemy o dużej liczbie konsumentów, gdzie rebalansowanie musi być jak najmniej inwazyjne, np. przetwarzanie transakcji finansowych.


---

### Jak ustawić strategię w konsumerze?

Dodaj właściwość `partition.assignment.strategy` do konfiguracji konsumenta:

```java
public static KafkaConsumer<String, String> createConsumer() {
      Properties properties = new Properties();
      properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
      properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "java-kafka-clients-consumer");
      properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
      properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
      props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.CooperativeStickyAssignor");
      return new KafkaConsumer<>(properties);
}
```

Oraz usuń z przetwarzania rekordów `consumer.commitSync();`


### Przebuduj i uruchom aplikację
```bash
  mvn clean package
  docker build -t java-kafka-consumer .   
  docker run --network  mynetwork java-kafka-consumer
```

### Zatrzymaj jedną z instancji aplikacji i zaobserwuj, że pozostałe instancje nadal odbierają wiadomości.

### Pozostałe interesujące właściwości konfiguracyjne

| **Ustawienie**                      | **Opis**                                                                                                                                                          | **Dlaczego jest interesujące?**                                                                                                                |
|-------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| **`max.poll.records`**              | Maksymalna liczba rekordów zwracanych przez jedno wywołanie `poll()`.                                                                                            | Pozwala kontrolować wielkość partii do przetwarzania, co wpływa na równowagę między przepustowością a opóźnieniem.                             |
| **`fetch.min.bytes`**               | Minimalna liczba bajtów, które konsument pobiera w jednym żądaniu od brokera.                                                                                     | Optymalizuje wykorzystanie sieci, zmuszając brokera do grupowania większej liczby rekordów przed odpowiedzią.                                  |
| **`fetch.max.wait.ms`**             | Maksymalny czas oczekiwania konsumenta na osiągnięcie `fetch.min.bytes` przed zwróceniem wyników.                                                                 | Umożliwia balansowanie między opóźnieniem a wykorzystaniem zasobów.                                                                            |
| **`isolation.level`**               | Określa, czy konsument widzi tylko zatwierdzone transakcje (`read_committed`) czy wszystkie wiadomości (`read_uncommitted`).                                      | Kluczowe przy pracy z dokładnie raz przetwarzaniem (exactly-once semantics) i transakcjami w Kafka.                                             |
| **`partition.assignment.strategy`** | Strategia przypisywania partycji do konsumentów, np. `RangeAssignor`, `RoundRobinAssignor`, `CooperativeStickyAssignor`.                                           | Możliwość zmiany sposobu dystrybucji partycji w grupie konsumenckiej, co ma wpływ na równowagę obciążenia i stabilność przypisań.               |
| **`max.poll.interval.ms`**          | Maksymalny czas między kolejnymi wywołaniami `poll()` przed uznaniem konsumenta za nieaktywnego i rozpoczęciem rebalansowania.                                    | Ważne w scenariuszach, gdzie przetwarzanie partii rekordów zajmuje dużo czasu.                                                                 |
| **`session.timeout.ms`**            | Czas, po którym brak heartbeatów od konsumenta powoduje uznanie go za nieaktywnego i uwolnienie jego partycji.                                                    | Kluczowe dla stabilności grupy konsumenckiej. Zbyt niski czas może powodować fałszywe rebalansowanie, a zbyt wysoki opóźnienia w wykrywaniu awarii. |
| **`client.id`**                     | Identyfikator klienta używany w logach brokera do rozróżniania konsumentów.                                                                                       | Przydatne do monitorowania i debugowania, szczególnie w dużych klastrach z wieloma konsumentami.                                               |
| **`metadata.max.age.ms`**           | Maksymalny czas przechowywania informacji o metadanych (np. partycjach) przed ich odświeżeniem.                                                                   | Ważne w dynamicznych środowiskach, gdzie partycje mogą być dodawane lub zmieniane.                                                             |
| **`allow.auto.create.topics`**      | Czy konsument może automatycznie tworzyć brakujące tematy.                                                                                                        | Przydatne w środowiskach deweloperskich, ale niezalecane w produkcji, gdzie kontrola nad tematami jest krytyczna.                              |
| **`retry.backoff.ms`**              | Czas oczekiwania przed ponownym żądaniem danych w przypadku błędu.                                                                                                | Pozwala zrozumieć, jak konsumenci radzą sobie z chwilowymi problemami, np. niedostępnością brokera.                                             |


# ENG 

# Java Kafka Consumer

### Build and Run the Application

```bash
  mvn clean package
```

```bash
  docker build -t java-kafka-consumer .
```

```bash
  docker run --network mynetwork java-kafka-consumer
```

### Configure and Run the Kafka Consumer

![img.png](img.png)

```java
public static KafkaConsumer<String, String> createConsumer() {
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "java-kafka-clients-consumer");
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    return new KafkaConsumer<>(properties);
}
```

### Poll messages from the topic in an infinite loop

```java
public static void main(String[] args) {
    KafkaConsumer<String, String> consumer = createConsumer();
    consumer.subscribe(List.of("kafka-client-exercises"));
    while (true) {
        consumer.poll(Duration.ofMillis(100)).forEach(record ->
            logger.info("Key: {}, Value: {}, Partition: {}, Offset: {}", record.key(), record.value(), record.partition(), record.offset()));
        consumer.commitSync();
    }
}
```

### Rebuild and Run the Application

```bash
mvn clean package
docker build -t java-kafka-consumer .
docker run --network mynetwork java-kafka-consumer
```

### Stop the app and open Kafka UI

### Reset Group Offsets

```bash
  docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-consumer-groups --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092 --group java-kafka-clients-consumer --reset-offsets --to-earliest --execute --topic kafka-client-exercises
```

### Set auto.offset.reset to latest

```java
public static KafkaConsumer<String, String> createConsumer() {
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "java-kafka-clients-consumer");
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    return new KafkaConsumer<>(properties);
}
```

### Rebuild and Run the Application

```bash
mvn clean package
docker build -t java-kafka-consumer .
docker run --network mynetwork java-kafka-consumer
```

### Consumer Group and Rebalancing

### Run 2 additional instances of the app in separate terminals and observe logs

```bash
  docker run --network mynetwork java-kafka-consumer
```

### Autoscaling and Coupling Problem Between HTTP/GRPC and Kafka!!!

### STOP THE WORLD – REBALANCE
![img_1.png](img_1.png)

### Incremental Rebalance

![img_2.png](img_2.png)

---

### Alternative Partition Assignment Strategies

| **Strategy**                 | **Description**                                                                                                                                 | **When to Use**                                                                                   |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| **RangeAssignor**           | Default strategy. Assigns partitions in ranges (based on topic and partition order). A consumer receives a consecutive block of partitions.      | When the number of partitions is proportional to the number of consumers and exact balance isn't critical. |
| **RoundRobinAssignor**      | Partitions are assigned round-robin. Each consumer gets one partition in turn until all are distributed.                                         | When even distribution is crucial, e.g., in stream processing.                                   |
| **StickyAssignor**          | Tries to assign partitions evenly while keeping previous assignments to minimize rebalancing.                                                    | When minimizing partition reassignment is important for performance or stability.                |
| **CooperativeStickyAssignor** | A cooperative version of StickyAssignor that supports **incremental** rebalancing to minimize disruption.                                       | When minimizing rebalancing disruption is necessary, e.g., high-availability systems.            |
| **Custom Strategy**         | You can implement your own strategy by extending `PartitionAssignor`.                                                                            | When none of the existing strategies meet specific business logic needs.                         |

---

### Usage Examples

1. **RangeAssignor**:
   - Topics with naturally ordered partitions, e.g., log processing.
   - Simple setup with few partitions/consumers.

2. **RoundRobinAssignor**:
   - Real-time analytics systems where even load distribution is key.

3. **StickyAssignor**:
   - Critical systems where reassignment can hurt performance.

4. **CooperativeStickyAssignor**:
   - High-scale systems where downtime during rebalancing must be minimized (e.g., financial transaction processing).

---

### How to Set Assignment Strategy in the Consumer

Add `partition.assignment.strategy` to the consumer properties:

```java
public static KafkaConsumer<String, String> createConsumer() {
    Properties props = new Properties();
    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "java-kafka-clients-consumer");
    props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.CooperativeStickyAssignor");
    return new KafkaConsumer<>(props);
}
```

Also, remove `consumer.commitSync();` from the polling loop if you're not committing manually anymore.

---

### Rebuild and Run the Application

```bash
mvn clean package
docker build -t java-kafka-consumer .
docker run --network mynetwork java-kafka-consumer
```

### Stop one of the running instances and observe that the remaining instances continue to consume messages.

---

### Other Interesting Kafka Consumer Configurations

| **Setting**                      | **Description**                                                                                                                               | **Why It’s Interesting**                                                                                 |
|----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| **`max.poll.records`**           | Max number of records returned in one `poll()` call.                                                                                          | Controls batch size for processing – impacts throughput vs latency.                                      |
| **`fetch.min.bytes`**            | Minimum bytes the broker should accumulate before responding to a fetch request.                                                              | Helps network efficiency by forcing batching.                                                             |
| **`fetch.max.wait.ms`**          | Max wait time before the broker responds to a fetch, even if `fetch.min.bytes` is not reached.                                               | Balances latency and resource use.                                                                       |
| **`isolation.level`**            | Whether to read only committed messages (`read_committed`) or all (`read_uncommitted`).                                                       | Essential when working with transactions and exactly-once semantics.                                     |
| **`partition.assignment.strategy`** | Assignment strategy, e.g., `RangeAssignor`, `RoundRobinAssignor`, `CooperativeStickyAssignor`.                                                | Allows flexible control of how partitions are distributed across the group.                              |
| **`max.poll.interval.ms`**       | Max delay between polls before a consumer is considered dead and rebalancing begins.                                                          | Useful in cases where message processing takes time.                                                      |
| **`session.timeout.ms`**         | Time after which lack of heartbeat causes consumer to be removed from group.                                                                  | Key for group stability – too low causes false failures; too high delays failure detection.              |
| **`client.id`**                  | Used for logging and metrics – identifies the client.                                                                                         | Helps in monitoring and debugging in multi-consumer clusters.                                             |
| **`metadata.max.age.ms`**        | Max time to cache partition/topic metadata before refreshing.                                                                                 | Important in dynamic environments where topics/partitions change often.                                   |
| **`allow.auto.create.topics`**   | Whether to allow auto-creation of missing topics.                                                                                             | Convenient in dev, dangerous in production – can lead to topic sprawl.                                   |
| **`retry.backoff.ms`**           | Time to wait before retrying after a fetch/metadata error.                                                                                     | Useful in flaky environments or when handling temporary broker issues.                                   |

---
