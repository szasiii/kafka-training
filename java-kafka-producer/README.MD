# Java Kafka Producer

### Budowanie i uruchamianie aplikacji

```bash
    mvn clean package
```

```bash
    docker build -t java-kafka-producer .   
```

```bash
  docker run --network  mynetwork java-kafka-producer
```

# Przygotowanie środowiska

```bash
   docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic kafka-client-exercises --partitions 3 --replication-factor 3 --if-not-exists --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

### Przyogotwanie consumera w osobnej karcie terminala
```bash
  docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-console-consumer --topic kafka-client-exercises --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092 --from-beginning
```

### Konfiguracja i uruchomienie Kafka Producer

```java
public static KafkaProducer<String, String> createProducer() {
    // Ustawienie właściwości w properties
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    // Stworzenie instancji KafkaProducer
    return new KafkaProducer<>(properties);
}
```
### Wysłanie wiadomości do topicu

```java
public static void main(String[] args) throws Exception{
    // Stworzenie instancji KafkaProducer
    KafkaProducer<String, String> producer = createProducer();
    
    // Stworzenie instancji ProducerRecord
    ProducerRecord<String, String> record = new ProducerRecord<>("kafka-client-exercises", "key-" + Math.random(), "value-" + Math.random());
    
    // Wysłanie wiadomości do topicu !!!Uwaga!!! - metoda send jest asynchroniczna
    producer.send(record).get();
}
```
### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Sprawdź czy wiadomość została wysłana

### Partycjonowanie wiadomości

#### Partycjonowanie wiadomości w Kafce jest kluczowym mechanizmem, który pozwala na równomierne rozłożenie obciążenia na poszczególnych brokerach. Dzięki partycjonowaniu możemy również kontrolować kolejność wiadomości w obrębie jednej partycji.
#### Wartość klucza wiadomości jest używana do określenia partycji, do której zostanie wysłana wiadomość. Domyślnie Kafka używa algorytmu haszującego, aby przypisać wiadomość do konkretnej partycji.

![img.png](img.png)

#### W przypadku braku klucza, wiadomość zostanie wysłana do losowej partycji. Wartość klucza jest opcjonalna, ale zalecane jest jej użycie, aby zapewnić zachowanie kolejności wiadomości w obrębie jednej partycji.


### ACKS_CONFIG na "all" oraz RETRIES_CONFIG na "1000000"

Zmodyfikuj metodę createProducer

```java
public static KafkaProducer<String, String> createProducer() {
    // Ustawienie właściwości w properties
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.ACKS_CONFIG, "all");
    properties.put(ProducerConfig.RETRIES_CONFIG, "1000000");
    // Stworzenie instancji KafkaProducer
    return new KafkaProducer<>(properties);
}
```

### Wyłącz 2 z 3 brokerów i spróbuj wysłać wiadomość do topicu test

```bash 
  docker stop kafka-2 kafka-3
```
### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Sprawdź co loguje aplikacja??

### Włącz jeden z brokerów i sprawdź czy wiadomość została wysłana

```bash 
  docker start kafka-2
```

### Sprawdź czy wiadomość została wysłana

### Włącz drugiego brokera

```bash 
  docker start kafka-3
```

### Proces zapisu wiadomości w Kafce w zależności od wartości ACKS_CONFIG

### ACK = 0

![img_5.png](img_5.png)

### ACK = 1

![img_6.png](img_6.png)

### ACK = all

![img_1.png](img_1.png)

### Błąd w zapisie wiadomości

![img_2.png](img_2.png)

| **ACK**   | **Opis**                                                                                       | **Zastosowanie**                                                                                     |
|-----------|-----------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| `acks=0`  | Producent nie oczekuje potwierdzenia od brokera.                                               | - Najszybsze przesyłanie. <br> - Ryzyko utraty danych w przypadku awarii.                          |
| `acks=1`  | Producent oczekuje potwierdzenia od lidera partycji, ale nie od replik.                        | - Kompromis między szybkością a niezawodnością. <br> - Możliwe ryzyko utraty danych przy awarii lidera. |
| `acks=all` (lub `acks=-1`) | Producent oczekuje potwierdzenia od lidera oraz wszystkich replik w partycji. | - Największa niezawodność. <br> - Większa latencja. <br> - Gwarancja, że dane są w pełni zreplikowane. |

### Wyjaśnienia
- **`acks=0`**: Dane są wysyłane bez oczekiwania na jakiekolwiek potwierdzenie. Broker może nie otrzymać wiadomości w przypadku problemów sieciowych lub awarii.
- **`acks=1`**: Lider partycji potwierdza odbiór danych. Jeśli lider ulegnie awarii przed replikacją, dane mogą zostać utracone.
- **`acks=all` (lub `acks=-1`)**: Dane są potwierdzane dopiero po zapisaniu przez lidera i pełnej replikacji na wszystkie węzły ISR (in-sync replicas).


### Zmień wartość linger.ms na 10000

Zmodyfikuj metodę createProducer

```java
public static KafkaProducer<String, String> createProducer() {
    // Ustawienie właściwości w properties
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.ACKS_CONFIG, "all");
    properties.put(ProducerConfig.RETRIES_CONFIG, "1000000");
    properties.put(ProducerConfig.LINGER_MS_CONFIG, "20000");
}
```

### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Sprawdź w consumerze czy wiadomość została wysłana (cierpliwości)

### Czym jest relacja batch.size i linger.ms?

![img_3.png](img_3.png)

### Zmień batch.size na 5

Zmodyfikuj metodę createProducer

```java
public static KafkaProducer<String, String> createProducer() {
    // Ustawienie właściwości w properties
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.ACKS_CONFIG, "all");
    properties.put(ProducerConfig.RETRIES_CONFIG, "1000000");
    properties.put(ProducerConfig.LINGER_MS_CONFIG, "20000");
    properties.put(ProducerConfig.BATCH_SIZE_CONFIG, "5");
}
```

### Zmodyfikuj main aby wyslac 5 rekordów

### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Sprawdź w consumerze czy wiadomości zostały wysłane bez opóźnienia

### Zmień wartość idempotence na true

Zmodyfikuj metodę createProducer

```java
public static KafkaProducer<String, String> createProducer() {
    // Ustawienie właściwości w properties
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    
    // Włączenie idempotence
    properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
}
```

### Idempotence równoznaczne z acks=all, retries=Integer.MAX_VALUE

### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

![img_4.png](img_4.png)

| **Funkcja**                           | **Opis**                                                                                                                                             | **Dlaczego warto używać?**                                                                                                                                                   |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Zapobieganie duplikatom**           | Gwarancja, że każda wiadomość zostanie zapisana dokładnie raz, nawet jeśli wystąpią ponowne próby wysyłania.                                         | Uniknięcie sytuacji, w której komunikaty są zapisywane wielokrotnie, co mogłoby powodować błędy w aplikacji lub bazie danych.                                               |
| **Zachowanie kolejności wiadomości**  | Wiadomości są zapisywane w tej samej kolejności, w jakiej zostały wysłane przez producenta (w obrębie jednej partycji).                              | Kluczowe w systemach, w których kolejność wiadomości jest ważna, np. w przetwarzaniu transakcji finansowych.                                                                |
| **Bezpieczne ponowne próby (retries)**| Producent może wielokrotnie próbować wysyłać komunikaty w przypadku awarii sieci lub brokera, bez ryzyka wprowadzenia duplikatów.                    | Zwiększa niezawodność aplikacji, minimalizując ryzyko utraty danych w przypadku chwilowych błędów.                                                                           |
| **Wymuszona konfiguracja**            | Automatycznie ustawia `retries` na wysoki poziom (domyślnie `MAX_INT`) i ogranicza `max.in.flight.requests.per.connection` do 5.                    | Eliminuje konieczność ręcznej konfiguracji producenta, zapewniając, że idempotencja działa poprawnie.                                                                       |
| **Integracja z exactly-once**         | Umożliwia wykorzystanie semantyki dokładnie raz (exactly-once semantics) w połączeniu z transakcjami Kafki.                                          | Idealne rozwiązanie w przypadkach, gdy wymagane jest absolutne uniknięcie błędów, np. w przetwarzaniu krytycznych danych biznesowych.                                       |
| **Minimalizacja złożoności aplikacji**| Kafka sama zarządza mechanizmem identyfikacji i odrzucania duplikatów (za pomocą `Producer ID` i `Sequence Number`).                                | Redukuje ilość kodu, który programista musi napisać, aby uniknąć problemów z duplikatami i zachować kolejność wiadomości.                                                   |

### Przykładowe scenariusze użycia:
- **Systemy finansowe**: Przetwarzanie transakcji, gdzie duplikaty mogą prowadzić do powielonych płatności.
- **Logistyka i śledzenie przesyłek**: Zapewnienie, że zdarzenia dotyczące przesyłek są przetwarzane w kolejności.
- **Integracja danych**: Bezpieczne przesyłanie i aktualizacja danych w systemach rozproszonych.

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Zmień wartość compression type na snappy

Zmodyfikuj metodę createProducer

```java
public static KafkaProducer<String, String> createProducer() {
    // Ustawienie właściwości w properties
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    
    // Ustawienie kompresji na snappy
    properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
}
```

### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Wejdź do Kafka UI i sprawdź, czy widać zmniejszenie rozmiaru wiadomości

```
http://localhost:8080
```  

### Na koniec zaimplementuj nieskończoną pętlę, która będzie wysyłać wiadomości co 5 sekund (to tylko ćwiczenie Thread.sleep jest OK)

### Przebuduj i uruchom aplikację

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Sprawdź w consumerze czy wiadomości są wysyłane co 5 sekund i zamknij conusmera

# ENG

# Java Kafka Producer

### Building and running the application

```bash
    mvn clean package
```

```bash
    docker build -t java-kafka-producer .   
```

```bash
  docker run --network  mynetwork java-kafka-producer
```

# Preparing the environment

```bash
   docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic kafka-client-exercises --partitions 3 --replication-factor 3 --if-not-exists --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

### Preparing the consumer in a separate terminal tab
```bash
  docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-console-consumer --topic kafka-client-exercises --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092 --from-beginning
```

### Configuration and running Kafka Producer

```java
public static KafkaProducer<String, String> createProducer() {
    // Ustawienie właściwości w properties
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    // Stworzenie instancji KafkaProducer
    return new KafkaProducer<>(properties);
}
```
### Sending a message to the topic

```java
public static void main(String[] args) throws Exception{
    // Stworzenie instancji KafkaProducer
    KafkaProducer<String, String> producer = createProducer();
    
    // Stworzenie instancji ProducerRecord
    ProducerRecord<String, String> record = new ProducerRecord<>("kafka-client-exercises", "key-" + Math.random(), "value-" + Math.random());
    
    // Wysłanie wiadomości do topicu !!!Uwaga!!! - metoda send jest asynchroniczna
    producer.send(record).get();
}
```
### Rebuild and run the application

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Check if the message was sent

### Partitioning 

#### Partitioning messages in Kafka is a key mechanism that allows for even distribution of load across brokers. It also enables control over the order of messages within a single partition.
#### The value of the message key is used to determine the partition to which the message will be sent. By default, Kafka uses a hashing algorithm to assign the message to a specific partition.

![img.png](img.png)

#### In case of no key, the message will be sent to a random partition. The key value is optional, but it is recommended to use it to ensure the order of messages within a single partition.

### Set ACKS_CONFIG to "all" and RETRIES_CONFIG to "1000000"

Modify the createProducer method

```java
public static KafkaProducer<String, String> createProducer() {
    // Ustawienie właściwości w properties
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.ACKS_CONFIG, "all");
    properties.put(ProducerConfig.RETRIES_CONFIG, "1000000");
    // Stworzenie instancji KafkaProducer
    return new KafkaProducer<>(properties);
}
```

### Stop 2 out of 3 brokers and try to send a message to the topic test

```bash 
  docker stop kafka-2 kafka-3
```
### Rebuild and run the application

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Check what the application logs??

### Start one of the brokers and check if the message was sent

```bash 
  docker start kafka-2
```

### Check if the message was sent

### Start the second broker

```bash 
  docker start kafka-3
```

### Process of writing a message in Kafka depending on the value of ACKS_CONFIG

### ACK = 0

![img_5.png](img_5.png)

### ACK = 1

![img_6.png](img_6.png)

### ACK = all

![img_1.png](img_1.png)

### Error in writing a message

![img_2.png](img_2.png)

| **ACK**   | **Description**                                                                                           | **Use Cases**                                                                                           |
|-----------|------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| `acks=0`  | The producer does not wait for an acknowledgment from the broker.                                          | - Fastest delivery. <br> - Risk of data loss in case of a failure.                                      |
| `acks=1`  | The producer waits for an acknowledgment from the partition leader, but not from the replicas.             | - Compromise between speed and reliability. <br> - Possible risk of data loss if the leader fails.     |
| `acks=all` (or `acks=-1`) | The producer waits for acknowledgments from the leader and all replicas in the partition. | - Highest reliability. <br> - Higher latency. <br> - Guarantee that the data is fully replicated.      |

### Explanations
- **`acks=0`**: Data is sent without waiting for any acknowledgment. The broker may not receive the message in the event of network issues or a failure.
- **`acks=1`**: The partition leader acknowledges receipt of the data. If the leader fails before replication, the data may be lost.
- **`acks=all` (or `acks=-1`)**: Data is acknowledged only after being written by the leader and fully replicated to all ISR (in-sync replicas) nodes.

### Change `linger.ms` value to `10000`

Modify the `createProducer` method.

```java
public static KafkaProducer<String, String> createProducer() {
    // Ustawienie właściwości w properties
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.ACKS_CONFIG, "all");
    properties.put(ProducerConfig.RETRIES_CONFIG, "1000000");
    properties.put(ProducerConfig.LINGER_MS_CONFIG, "20000");
}
```

### Rebuild and run the application

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Check in the consumer if the message was sent (be patient)

### What is the relationship between `batch.size` and `linger.ms`?

![img_3.png](img_3.png)

### Change `batch.size` to `5`

Zmodyfikuj metodę createProducer

```java
public static KafkaProducer<String, String> createProducer() {
    // Ustawienie właściwości w properties
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.ACKS_CONFIG, "all");
    properties.put(ProducerConfig.RETRIES_CONFIG, "1000000");
    properties.put(ProducerConfig.LINGER_MS_CONFIG, "20000");
    properties.put(ProducerConfig.BATCH_SIZE_CONFIG, "5");
}
```

### Modify the `main` method to send 5 records

### Rebuild and run the application

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Check in the consumer if the messages were sent without delay

### Change the value of `idempotence` to `true`

Zmodyfikuj metodę createProducer

```java
public static KafkaProducer<String, String> createProducer() {
    // Ustawienie właściwości w properties
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    
    // Włączenie idempotence
    properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
}
```

### Idempotence is equivalent to `acks=all`, `retries=Integer.MAX_VALUE`

### Rebuild and run the application

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

![img_4.png](img_4.png)

| **Feature**                           | **Description**                                                                                                                                      | **Why use it?**                                                                                                                                                               |
|---------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Duplicate prevention**              | Guarantees that each message is written exactly once, even if send retries occur.                                                                    | Avoids situations where messages are written multiple times, which could cause errors in the application or database.                                                         |
| **Preserving message order**          | Messages are written in the same order they were sent by the producer (within a single partition).                                                   | Critical in systems where message order matters, e.g., processing financial transactions.                                                                                     |
| **Safe retries**                      | The producer can repeatedly attempt to send messages in case of network or broker failures, without risking duplicates.                              | Increases application reliability by minimizing the risk of data loss in the event of transient errors.                                                                       |
| **Enforced configuration**            | Automatically sets `retries` to a high value (by default `MAX_INT`) and limits `max.in.flight.requests.per.connection` to 5.                         | Eliminates the need for manual producer configuration, ensuring idempotence works correctly.                                                                                  |
| **Integration with exactly-once**     | Enables the use of exactly-once semantics in combination with Kafka transactions.                                                                    | Ideal when strict avoidance of errors is required, e.g., when processing critical business data.                                                                              |
| **Minimizing application complexity** | Kafka itself manages the mechanism for identifying and rejecting duplicates (using `Producer ID` and `Sequence Number`).                             | Reduces the amount of code a developer must write to avoid duplicates and preserve message order.                                                                             |

### Example use cases:
- **Financial systems**: Processing transactions where duplicates could lead to duplicate payments.
- **Logistics & shipment tracking**: Ensuring that shipment events are processed in order.
- **Data integration**: Safely transmitting and updating data in distributed systems.


```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Change the compression type to snappy

Modify the `createProducer` method

```java
public static KafkaProducer<String, String> createProducer() {
    // Ustawienie właściwości w properties
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    
    // Ustawienie kompresji na snappy
    properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
}
```

### Rebuild and run the application

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Enter Kafka UI and check if you can see a reduction in message size

```
http://localhost:8080
```  

### Finally, implement an infinite loop that will send messages every 5 seconds (this is just an exercise, `Thread.sleep` is OK)

### Rebuild and run the application

```bash
    mvn clean package
    docker build -t java-kafka-producer .   
    docker run --network  mynetwork java-kafka-producer
```

### Check in the consumer if messages are sent every 5 seconds and close the consumer

