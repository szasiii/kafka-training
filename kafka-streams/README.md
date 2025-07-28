# Kafka Streams

```bash
  mvn clean package
```

```bash
  docker build -t kafka-streams .
```

```bash
  docker run --network mynetwork kafka-streams
```

### Przygotowanie środowiska


```bash
  docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic streaming-transactions --partitions 1 --replication-factor 3 --if-not-exists --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

```bash
  docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic streaming-prices --partitions 1 --replication-factor 3 --if-not-exists --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

### Topologia Kafka Streams

![img_1.png](img_1.png)

### Zkonfigiruj aplikację w pliku `application.yml`

```yaml
spring:
  kafka:
    streams:
      application-id: "kafka_streams_training"
      bootstrap-servers: kafka-1:29092,kafka-2:39092,kafka-3:49092
      state-dir: "/tmp/transaction_processor/instance1"
      replication-factor: 2
      properties:
        default.key.serde: org.apache.kafka.common.serialization.Serdes$StringSerde
        default.value.serde: org.apache.kafka.common.serialization.Serdes$DoubleSerde
```


### Zaimplementuj klasę `BasicStream.class`
### Celem zadania jest przetworzenie strumienia transakcji i wyfiltrowanie transakcji z instrumentem `BTC`.

### Stateless operations

| **Operacja**    | **Opis**                                                                                      | **Przykład**                                    |
|-----------------|----------------------------------------------------------------------------------------------|------------------------------------------------|
| **`filter()`**  | Filtruje wiadomości na podstawie warunku logicznego.                                         | Odrzucanie rekordów o wartości `null`.         |
| **`map()`**     | Przekształca każdy rekord do nowej postaci (klucz i/lub wartość).                            | Modyfikacja klucza wiadomości.                 |
| **`flatMap()`** | Przekształca jeden rekord w zero lub wiele rekordów.                                         | Rozbijanie jednego rekordu na wiele.           |
| **`peek()`**    | Wykonuje działanie dla każdego rekordu bez zmiany strumienia (np. logowanie).                | Logowanie wartości wiadomości.                 |
| **`split()`**   | Rozdziela strumień na wiele podstrumieni na podstawie warunków.                              | Rozdzielenie strumienia na "valid" i "invalid".|
| **`merge()`**   | Łączy wiele strumieni w jeden.                                                               | Scalanie dwóch strumieni.                      |
| **`to()`**      | Zapisuje dane do określonego tematu Kafka.                                                   | Zapis rekordów do `output-topic`.              |


#### Utwórz instancje JsonSerde<Transaction> aby móc przetwarzać obiekty `Transaction` w strumieniu.

```java
JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
```

#### Utwórz strumień transakcji z tematu `streaming-transactions

```java
KStream<String, Transaction> transactions = builder.stream("streaming-transactions",Consumed.with(Serdes.String(), transactionSerde));
```

#### Wyfiltruj transakcje z instrumentem `BTC`

```java
final KStream<String, Transaction> filteredTransactions = transactions.filter((k, v) -> v.getInstrument().equalsIgnoreCase("BTC"));
```

#### Zmień wartości transakcji na instrument

```java
KStream<String, String> outputStream = filteredTransactions.mapValues(s -> s.getInstrument());
```

#### Zapisz dane do tematu `instruments`

```java
outputStream.to("instruments", Produced.with(Serdes.String(), Serdes.String()));
```

#### Wyświetl dane na konsoli

```java
transactions.print(Printed.<String, Transaction>toSysOut().withLabel("source stream"));
outputStream.print(Printed.<String, String>toSysOut().withLabel("sink stream"));
return outputStream;
```

### Przebuduj i uruchom aplikację
```bash
    mvn clean package
    docker build -t kafka-streams .
    docker run --network mynetwork kafka-streams
```

### Poeksperymentuj z operacjami `flatMap()`, `peek()`, `split()`, `merge()`

### Stateful operations

| **Operacja**           | **Opis**                                                                                      | **Przykład**                                    |
|-------------------------|----------------------------------------------------------------------------------------------|------------------------------------------------|
| **`groupBy()`**         | Grupuje rekordy na podstawie klucza lub wartości.                                            | Grupowanie po kluczu `category`.               |
| **`aggregate()`**       | Tworzy stan na podstawie zgrupowanych rekordów.                                              | Sumowanie wartości w obrębie klucza.           |
| **`count()`**           | Zlicza rekordy w grupach kluczy.                                                             | Liczenie wiadomości na klucz.                  |
| **`reduce()`**          | Redukuje rekordy w grupie, łącząc je zgodnie z podaną logiką.                                | Konkatenacja wartości w grupie.                |
| **`join()`**            | Łączy dwa strumienie na podstawie wspólnego klucza.                                          | Łączenie `orders` z `payments`.                |
| **`windowedBy()`**      | Tworzy okna czasowe na zgrupowanych danych.                                                  | Grupowanie w oknach 10-minutowych.             |
| **`transform()`**       | Pozwala na niestandardowe przetwarzanie rekordów z dostępem do stanu.                        | Tworzenie własnych operacji z dostępem do stanu.|


### Zaimplementuj klasę `GroupingStream.class`


### KTable vs KStream

| **KStream**                           | **KTable**                                                                                   |
|---------------------------------------|---------------------------------------------------------------------------------------------|
| Strumień nieprzetworzonych rekordów.  | Reprezentuje bieżący stan (tabelę) na podstawie tematów kompaktowanych.                     |
| Każdy rekord jest niezależny.         | Rekordy nadpisują wcześniejsze wartości dla tego samego klucza.                             |
| Nie przechowuje stanu.                | Przechowuje stan w Kafka oraz lokalnie (state store).                                       |
| Idealne do przetwarzania zdarzeń.     | Idealne do reprezentacji bieżącego stanu systemu (np. tabele z bazy danych).                |
| Przykład: strumień zamówień.          | Przykład: tabela ze stanem użytkowników (ID -> Nazwa).                                      |


### Celem zadania jest przetworzenie strumienia transakcji i zgrupowanie ich po instrumencie.

#### Utwórz instancje JsonSerde<Transaction> aby móc przetwarzać obiekty `Transaction` w strumieniu.
#### Utwórz instancje Serde<Double> aby móc przetwarzać wartości `Double` w strumieniu.
#### Utwórz strumień transakcji z tematu `streaming-transactions

```java
JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
Serde<Double> doubleSerde = Serdes.Double();

KStream<String, Transaction> txnStream = builder.stream("streaming-transactions", Consumed.with(Serdes.String(), transactionSerde));
```

#### Zmodyfikuj klucz transakcji na instrument

```java
KStream<String, Double> aggregated = txnStream
                .selectKey((k, v) -> v.getInstrument())
```

#### Zmodyfikuj wartość transakcji na kwotę

```java
KStream<String, Double> aggregated = txnStream
                .selectKey((k, v) -> v.getInstrument())
                .mapValues((k, v) -> v.getAmount().doubleValue())
```

#### Zgrupuj transakcje po instrumencie

```java
KStream<String, Double> aggregated = txnStream
                .selectKey((k, v) -> v.getInstrument())
                .mapValues((k, v) -> v.getAmount().doubleValue())
                .groupByKey()
```

#### Zsumuj wartości transakcji w obrębie grupy

```java
KStream<String, Double> aggregated = txnStream
                .selectKey((k, v) -> v.getInstrument())
                .mapValues((k, v) -> v.getAmount().doubleValue())
                .groupByKey()
                .aggregate(
                        () -> 0.0, (aggKey, newValue, aggValue) -> aggValue + newValue,
                        Materialized.with(Serdes.String(), doubleSerde))
                .toStream();
```

#### Wyświetl dane na konsoli

```java
txnStream.print(Printed.<String, Transaction>toSysOut().withLabel("source stream"));
aggregated.print(Printed.<String, Double>toSysOut().withLabel("sink stream"));

return txnStream;
```

### Spróbuj zastąpić `aggregate()` operacją `reduce()`

### Przebuduj i uruchom aplikację
```bash
    mvn clean package
    docker build -t kafka-streams .
    docker run --network mynetwork kafka-streams
```

### Zaimplementuj klasę `TimeWindowStream.class`
### Celem zadania jest przetworzenie strumienia transakcji i zliczenie transakcji w oknach czasowych po 5s.

#### Utwórz instancje JsonSerde<Transaction> aby móc przetwarzać obiekty `Transaction` w strumieniu.

```java
JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);

KStream<String, Transaction> txnStream = builder.stream("streaming-transactions", Consumed.with(Serdes.String(), transactionSerde));
```

#### Zmodyfikuj klucz transakcji na instrument

```java
KStream<Windowed<String>, Long> countInWindow = txnStream
                .selectKey((k, v) -> v.getInstrument())
```

#### Zgrupuj transakcje po instrumencie

```java
KStream<Windowed<String>, Long> countInWindow = txnStream
        .selectKey((k, v) -> v.getInstrument())
        .groupByKey()
```

#### Zdefiniuj okno czasowe 5s

```java
KStream<Windowed<String>, Long> countInWindow = txnStream
                .selectKey((k, v) -> v.getInstrument())
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(5)))
```

#### Zlicz transakcje w obrębie okna czasowego

```java
KStream<Windowed<String>, Long> countInWindow = txnStream
                .selectKey((k, v) -> v.getInstrument())
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(3)))
                .count(Materialized.with(Serdes.String(), Serdes.Long()))
                .toStream();
```

#### Wyświetl dane na konsoli

```java
txnStream.print(Printed.<String, Transaction>toSysOut().withLabel("source stream"));
countInWindow.print(Printed.<Windowed<String>, Long>toSysOut().withLabel("sink stream"));

return txnStream;
```

### Przebuduj i uruchom aplikację
```bash
    mvn clean package
    docker build -t kafka-streams .
    docker run --network mynetwork kafka-streams
```

### Inne okna czasowe?

![img.png](img.png)

### Zaimplementuj klasę `Joins.class`
### Celem zadania jest połączenie strumienia transakcji z cenami instrumentów.
### Pierwsze rozwiązanie z wykorzystaniem `join()` pozwala na połączenie dwóch strumieni na podstawie klucza.

### Odkomentuj kod w `SenderJob.class` i uruchom aplikację.

```java
//    @Scheduled(fixedRate = 5)
//    public void schedulePrices() {
//        PriceTick price = new PriceTick(UUID.randomUUID(), "GOLD", BigDecimal.valueOf(Math.random()));
//        kafkaTemplatePrice.send(PRICE_TOPIC, UUID.randomUUID().toString(), price);
//        PriceTick priceCrypto = new PriceTick(UUID.randomUUID(), "BTC", BigDecimal.valueOf(Math.random()));
//        kafkaTemplatePrice.send(PRICE_TOPIC, UUID.randomUUID().toString(), priceCrypto);
//    }
```

#### Utwórz instancje JsonSerde<Transaction> oraz JsonSerde<PriceTick> aby móc przetwarzać obiekty `Transaction` i `PriceTick` w strumieniu.


```java
JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
JsonSerde<PriceTick> priceTickSerde = new JsonSerde<>(PriceTick.class);

KStream<String, Transaction> txnStream = builder.stream(
        "streaming-transactions", Consumed.with(Serdes.String(), transactionSerde)
);
KStream<String, PriceTick> pricesStream = builder.stream(
        "streaming-prices", Consumed.with(Serdes.String(), priceTickSerde)
);
```

#### Zmodyfikuj klucz transakcji na instrument

```java
txnStream.selectKey((k, v) -> v.getInstrument())
```

#### Połącz strumień transakcji z cenami instrumentów
#### Stumienie są łączone na podstawie klucza (instrumentu) zatem strumień cen musi być przekształcony na klucz.
#### Stwórz instancje klasy `Trade` aby przechowywać dane z transakcji i ceny instrumentu.
#### Użyj operacji `join()` aby połączyć strumienie.
#### Użyj `JoinWindows.ofTimeDifferenceWithNoGrace()` aby zdefiniować okno czasowe.
#### Użyj `StreamJoined.with()` aby zdefiniować serdesy dla klucza, wartości transakcji i wartości ceny.

```java
txnStream.selectKey((k, v) -> v.getInstrument())
        .join(
        pricesStream.selectKey((k, v) -> v.getInstrument()),
        (txn, price) -> new Trade(
        txn.getInstrument(),
                                txn.getAmount().doubleValue(),
                                price.getPrice().doubleValue()
                        ),
                                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMillis(10)),
        StreamJoined.with(Serdes.String(), transactionSerde, priceTickSerde)
        )
```

#### Wyświetl dane na konsoli

```java
txnStream.selectKey((k, v) -> v.getInstrument())
         .join(
                 pricesStream.selectKey((k, v) -> v.getInstrument()),
                 (txn, price) -> new Trade(
                         txn.getInstrument(),
                         txn.getAmount().doubleValue(),
                         price.getPrice().doubleValue()
                 ),
                 JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMillis(10)),
                 StreamJoined.with(Serdes.String(), transactionSerde, priceTickSerde)
         )
         .print(Printed.<String, Trade>toSysOut().withLabel("stream-join-trade"));

return txnStream;
```

### Przebuduj i uruchom aplikację
```bash
    mvn clean package
    docker build -t kafka-streams .
    docker run --network mynetwork kafka-streams
```

### Drugie rozwiązanie z wykorzystaniem `leftJoin()` pozwala na połączenie strumienia z tabelą.
### W tym przypadku, jeżeli nie ma ceny dla instrumentu, to wartość ceny wynosi 0.
### Następnego Join'a dodamy pod pierwszym.

#### Utwórz tabelę cen instrumentów
#### Zmodyfikuj klucz transakcji na instrument oraz wartość na kwotę a następnie zapisz do tabeli.

```java
KTable<String, Double> priceTable = pricesStream.selectKey((k, v) -> v.getInstrument()).mapValues(v -> v.getPrice().doubleValue()).toTable();
```

#### Połącz strumień transakcji z tabelą cen instrumentów
#### Użyj `leftJoin()` aby połączyć strumienie.
#### Użyj `Printed.<String, Trade>toSysOut().withLabel("table-join-trade")` aby wyświetlić dane na konsoli.

```java
txnStream.selectKey((k, v) -> v.getInstrument())
                .leftJoin(priceTable, (txn, price) -> new Trade(
                        txn.getInstrument(),
                        txn.getAmount().doubleValue(),
                        price == null ? 0.0 : price
                )).print(Printed.<String, Trade>toSysOut().withLabel("table-join-trade"));
```

### Przebuduj i uruchom aplikację
```bash
    mvn clean package
    docker build -t kafka-streams .
    docker run --network mynetwork kafka-streams
```

### Zaimplementuj klasę `DeduplicationStream.class`
### Celem zadania jest usunięcie duplikatów transakcji w strumieniu.

### W tym przypadku, duplikaty są definiowane jako transakcje z tym samym `id` i które pojawiły się w ciągu ostatnich 10 minut.

### Od komentuj kod z duplikatami w `SenderJob.class`.

### Zaimplementuj klasę `DeduplicationTransformer.class`
### Klasa ta będzie implementować interfejs `Transformer` i będzie używana do usuwania duplikatów transakcji w strumieniu.

### Zaimplementuj metodę `init()` aby uzyskać dostęp do stanu.
### Użyj `KeyValueStore<UUID, Long>` do przechowywania ostatniego czasu widzenia transakcji.

```java
        private KeyValueStore<UUID, Long> store;
        
        @Override
        public void init(ProcessorContext context) {
            store = context.getStateStore("txn-store");
        }
```

### Zaimplementuj metodę `transform()` aby przetworzyć transakcje i usunąć duplikaty.
### W tej metodzie, sprawdź czy transakcja z danym `id` była już widziana w ciągu ostatnich 10 minut.
### Jeżeli tak, to odrzuć transakcję, w przeciwnym raz case, zapisz czas widzenia transakcji w stanie i zwróć transakcję.

```java
        @Override
        public KeyValue<String, Transaction> transform(String key, Transaction value) {
            UUID id = value.getId();
            long now = Instant.now().toEpochMilli();

            Long lastSeen = store.get(id);
            if (lastSeen == null || now - lastSeen > Duration.ofMinutes(10).toMillis()) {
                store.put(id, now);
                return new KeyValue<>(key, value);
            }
            return null; // drop duplicate
        }
```
### Zaimplementuj metodę `close()` aby zamknąć stan.

```java
        @Override
        public void close() {
            // No specific cleanup needed for this example
        }
```

### Zaimplementuj klasę `DeduplicationStream.class`
### Celem zadania jest usunięcie duplikatów transakcji w strumieniu.

```java
    @Bean
public KStream<String, Transaction> aStream(StreamsBuilder builder) {
    JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);

    KStream<String, Transaction> transactions =
            builder.stream("streaming-transactions", Consumed.with(Serdes.String(), transactionSerde));


    builder.addStateStore(
            Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore("txn-store"),
                    Serdes.UUID(),
                    Serdes.Long()
            )
    );


    KStream<String, Transaction> deduplicated = transactions.transform(DeduplicationTransformer::new, "txn-store");

    deduplicated.print(Printed.<String, Transaction>toSysOut().withLabel("deduplicated stream"));

    return deduplicated;
}
```


### Zaimplementuj klasę `AccessibleStore.class`
#### Celem zadania jest udostępnienie stanu aplikacji poprzez interaktywną zapytania.

### Odkomentuj kod w `ReaderJob.class` i uruchom aplikację.

```java
//    @Scheduled(fixedRate = 5000, initialDelay = 5000)
//    public void queryStore() {
//        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
//        if (kafkaStreams == null) return;
//
//        ReadOnlyKeyValueStore<String, Double> store =
//                kafkaStreams.store(
//                        StoreQueryParameters.fromNameAndType("aggregated-store", QueryableStoreTypes.keyValueStore())
//                );
//
//        System.out.println("### INTERACTIVE QUERY ###");
//        KeyValueIterator<String, Double> iterator = store.all();
//        while (iterator.hasNext()) {
//            KeyValue<String, Double> entry = iterator.next();
//            System.out.println("Instrument: " + entry.key + " => Sum: " + entry.value);
//        }
//        iterator.close();
//    }
```

### Zaimplementuj klasę `AccessibleStore.class`
### Zaimplementujemy proste zliczanie transakcji w obrębie instrumentu bez drukowania ich na konsoli.

```java
  @Bean
    public KStream<String, String> aStream(StreamsBuilder builder) {
        JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
        KStream<String, Transaction> transactions =
                builder.stream("streaming-transactions", Consumed.with(Serdes.String(), transactionSerde));

        Aggregator<String, Double, Double> sumAggregator = (key, newVal, aggVal) -> aggVal + newVal;


        transactions
                .selectKey((k, v) -> v.getInstrument())
                .mapValues((k, v) -> v.getAmount().doubleValue())
                .groupByKey()
                .aggregate(
                        () -> 0.0,
                        sumAggregator,
                        Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as("aggregated-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double())
                );

        return null;
    }
```

# ENG

# Kafka Streams

```bash
  mvn clean package
```

```bash
  docker build -t kafka-streams .
```

```bash
  docker run --network mynetwork kafka-streams
```

### Preparing the environment


```bash
  docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic streaming-transactions --partitions 1 --replication-factor 3 --if-not-exists --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

```bash
  docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic streaming-prices --partitions 1 --replication-factor 3 --if-not-exists --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

### Kafka Streams Topology

![img_1.png](img_1.png)

### Configure the application in `application.yml`

```yaml
spring:
  kafka:
    streams:
      application-id: "kafka_streams_training"
      bootstrap-servers: kafka-1:29092,kafka-2:39092,kafka-3:49092
      state-dir: "/tmp/transaction_processor/instance1"
      replication-factor: 2
      properties:
        default.key.serde: org.apache.kafka.common.serialization.Serdes$StringSerde
        default.value.serde: org.apache.kafka.common.serialization.Serdes$DoubleSerde
```


### Implement the `BasicStream.class`
### The goal of this task is to process a stream of transactions and filter out transactions with the instrument `BTC`.

### Stateless operations

| **Operacja**    | **Opis**                                                                                      | **Przykład**                                    |
|-----------------|----------------------------------------------------------------------------------------------|------------------------------------------------|
| **`filter()`**  | Filtruje wiadomości na podstawie warunku logicznego.                                         | Odrzucanie rekordów o wartości `null`.         |
| **`map()`**     | Przekształca każdy rekord do nowej postaci (klucz i/lub wartość).                            | Modyfikacja klucza wiadomości.                 |
| **`flatMap()`** | Przekształca jeden rekord w zero lub wiele rekordów.                                         | Rozbijanie jednego rekordu na wiele.           |
| **`peek()`**    | Wykonuje działanie dla każdego rekordu bez zmiany strumienia (np. logowanie).                | Logowanie wartości wiadomości.                 |
| **`split()`**   | Rozdziela strumień na wiele podstrumieni na podstawie warunków.                              | Rozdzielenie strumienia na "valid" i "invalid".|
| **`merge()`**   | Łączy wiele strumieni w jeden.                                                               | Scalanie dwóch strumieni.                      |
| **`to()`**      | Zapisuje dane do określonego tematu Kafka.                                                   | Zapis rekordów do `output-topic`.              |


#### Create an instance of `JsonSerde<Transaction>` to process `Transaction` objects in the stream.

```java
JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
```

#### Create a stream of transactions from the topic `streaming-transactions`

```java
KStream<String, Transaction> transactions = builder.stream("streaming-transactions",Consumed.with(Serdes.String(), transactionSerde));
```

#### Filter transactions with the instrument `BTC`

```java
final KStream<String, Transaction> filteredTransactions = transactions.filter((k, v) -> v.getInstrument().equalsIgnoreCase("BTC"));
```

#### Change the values of transactions to the instrument

```java
KStream<String, String> outputStream = filteredTransactions.mapValues(s -> s.getInstrument());
```

#### Save data to the topic `instruments`

```java
outputStream.to("instruments", Produced.with(Serdes.String(), Serdes.String()));
```

#### Display data on the console

```java
transactions.print(Printed.<String, Transaction>toSysOut().withLabel("source stream"));
outputStream.print(Printed.<String, String>toSysOut().withLabel("sink stream"));
return outputStream;
```

### Rebuild and run the application
```bash
    mvn clean package
    docker build -t kafka-streams .
    docker run --network mynetwork kafka-streams
```

### Experiment with the operations `flatMap()`, `peek()`, `split()`, `merge()`

### Stateful operations

| **Operacja**           | **Opis**                                                                                      | **Przykład**                                    |
|-------------------------|----------------------------------------------------------------------------------------------|------------------------------------------------|
| **`groupBy()`**         | Grupuje rekordy na podstawie klucza lub wartości.                                            | Grupowanie po kluczu `category`.               |
| **`aggregate()`**       | Tworzy stan na podstawie zgrupowanych rekordów.                                              | Sumowanie wartości w obrębie klucza.           |
| **`count()`**           | Zlicza rekordy w grupach kluczy.                                                             | Liczenie wiadomości na klucz.                  |
| **`reduce()`**          | Redukuje rekordy w grupie, łącząc je zgodnie z podaną logiką.                                | Konkatenacja wartości w grupie.                |
| **`join()`**            | Łączy dwa strumienie na podstawie wspólnego klucza.                                          | Łączenie `orders` z `payments`.                |
| **`windowedBy()`**      | Tworzy okna czasowe na zgrupowanych danych.                                                  | Grupowanie w oknach 10-minutowych.             |
| **`transform()`**       | Pozwala na niestandardowe przetwarzanie rekordów z dostępem do stanu.                        | Tworzenie własnych operacji z dostępem do stanu.|


### Implement the `GroupingStream.class`


### KTable vs KStream
| **KStream**                                      | **KTable**                                                                                   |
|--------------------------------------------------|---------------------------------------------------------------------------------------------|
| Unbounded stream of raw records.                 | Represents the current state (a table) from compacted topics.                               |
| Each record is independent.                      | Records overwrite previous values for the same key.                                         |
| Does not maintain state.                         | Maintains state in Kafka and locally (state store).                                         |
| Ideal for event processing.                      | Ideal for representing current system state (e.g., database tables).                        |
| Example: order events stream.                    | Example: user profile table (ID → Name).                                                    |

---

### Task: Process the transaction stream and group by instrument

#### Create a `JsonSerde<Transaction>` instance to handle `Transaction` objects in the stream.
#### Create a `Serde<Double>` instance to handle `Double` values in the stream.
#### Build the transactions stream from the topic `streaming-transactions`.

```java
JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
Serde<Double> doubleSerde = Serdes.Double();

KStream<String, Transaction> txnStream = builder.stream("streaming-transactions", Consumed.with(Serdes.String(), transactionSerde));
```

#### Modify the transaction key to the instrument

```java
KStream<String, Double> aggregated = txnStream
                .selectKey((k, v) -> v.getInstrument())
```

#### Modify the transaction value to the amount

```java
KStream<String, Double> aggregated = txnStream
                .selectKey((k, v) -> v.getInstrument())
                .mapValues((k, v) -> v.getAmount().doubleValue())
```

#### Group transactions by instrument

```java
KStream<String, Double> aggregated = txnStream
                .selectKey((k, v) -> v.getInstrument())
                .mapValues((k, v) -> v.getAmount().doubleValue())
                .groupByKey()
```

#### Sum transaction values within the group

```java
KStream<String, Double> aggregated = txnStream
                .selectKey((k, v) -> v.getInstrument())
                .mapValues((k, v) -> v.getAmount().doubleValue())
                .groupByKey()
                .aggregate(
                        () -> 0.0, (aggKey, newValue, aggValue) -> aggValue + newValue,
                        Materialized.with(Serdes.String(), doubleSerde))
                .toStream();
```

#### Display data on the console

```java
txnStream.print(Printed.<String, Transaction>toSysOut().withLabel("source stream"));
aggregated.print(Printed.<String, Double>toSysOut().withLabel("sink stream"));

return txnStream;
```

### Try replacing `aggregate()` with the `reduce()` operation

### Rebuild and run the application
```bash
    mvn clean package
    docker build -t kafka-streams .
    docker run --network mynetwork kafka-streams
```

### Implement the `TimeWindowStream.class`
### The goal of this task is to process the transaction stream and count transactions in time windows of 5 seconds.

#### Create an instance of `JsonSerde<Transaction>` to process `Transaction` objects in the stream.

```java
JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);

KStream<String, Transaction> txnStream = builder.stream("streaming-transactions", Consumed.with(Serdes.String(), transactionSerde));
```

#### Modify the transaction key to the instrument

```java
KStream<Windowed<String>, Long> countInWindow = txnStream
                .selectKey((k, v) -> v.getInstrument())
```

#### Group transactions by instrument

```java
KStream<Windowed<String>, Long> countInWindow = txnStream
        .selectKey((k, v) -> v.getInstrument())
        .groupByKey()
```

#### Define a time window of 5 seconds

```java
KStream<Windowed<String>, Long> countInWindow = txnStream
                .selectKey((k, v) -> v.getInstrument())
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(5)))
```

#### Count transactions within the time window

```java
KStream<Windowed<String>, Long> countInWindow = txnStream
                .selectKey((k, v) -> v.getInstrument())
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(3)))
                .count(Materialized.with(Serdes.String(), Serdes.Long()))
                .toStream();
```

#### Display data on the console

```java
txnStream.print(Printed.<String, Transaction>toSysOut().withLabel("source stream"));
countInWindow.print(Printed.<Windowed<String>, Long>toSysOut().withLabel("sink stream"));

return txnStream;
```

### Rebuild and run the application
```bash
    mvn clean package
    docker build -t kafka-streams .
    docker run --network mynetwork kafka-streams
```

### Other time windows?

![img.png](img.png)

### Implement the `Joins.class`
### The goal of this task is to join the transaction stream with instrument prices.
### The first solution using `join()` allows joining two streams based on a key.

### Uncomment the code in `SenderJob.class` and run the application.

```java
//    @Scheduled(fixedRate = 5)
//    public void schedulePrices() {
//        PriceTick price = new PriceTick(UUID.randomUUID(), "GOLD", BigDecimal.valueOf(Math.random()));
//        kafkaTemplatePrice.send(PRICE_TOPIC, UUID.randomUUID().toString(), price);
//        PriceTick priceCrypto = new PriceTick(UUID.randomUUID(), "BTC", BigDecimal.valueOf(Math.random()));
//        kafkaTemplatePrice.send(PRICE_TOPIC, UUID.randomUUID().toString(), priceCrypto);
//    }
```

#### Create instances of `JsonSerde<Transaction>` and `JsonSerde<PriceTick>` to process `Transaction` and `PriceTick` objects in the stream.

```java
JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
JsonSerde<PriceTick> priceTickSerde = new JsonSerde<>(PriceTick.class);

KStream<String, Transaction> txnStream = builder.stream(
        "streaming-transactions", Consumed.with(Serdes.String(), transactionSerde)
);
KStream<String, PriceTick> pricesStream = builder.stream(
        "streaming-prices", Consumed.with(Serdes.String(), priceTickSerde)
);
```

#### Modify the transaction key to the instrument

```java
txnStream.selectKey((k, v) -> v.getInstrument())
```

#### Join the transaction stream with the instrument price stream
#### Streams are joined by key (instrument), so the price stream must be key‑ed accordingly
#### Create a `Trade` class to hold both transaction and price data
#### Use the `join()` operation to merge the two streams
#### Use `JoinWindows.ofTimeDifferenceWithNoGrace()` to define the time window
#### Use `StreamJoined.with()` to specify SerDes for the key, transaction value, and price value

```java
txnStream.selectKey((k, v) -> v.getInstrument())
        .join(
        pricesStream.selectKey((k, v) -> v.getInstrument()),
        (txn, price) -> new Trade(
        txn.getInstrument(),
                                txn.getAmount().doubleValue(),
                                price.getPrice().doubleValue()
                        ),
                                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMillis(10)),
        StreamJoined.with(Serdes.String(), transactionSerde, priceTickSerde)
        )
```

#### Display data on the console

```java
txnStream.selectKey((k, v) -> v.getInstrument())
         .join(
                 pricesStream.selectKey((k, v) -> v.getInstrument()),
                 (txn, price) -> new Trade(
                         txn.getInstrument(),
                         txn.getAmount().doubleValue(),
                         price.getPrice().doubleValue()
                 ),
                 JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMillis(10)),
                 StreamJoined.with(Serdes.String(), transactionSerde, priceTickSerde)
         )
         .print(Printed.<String, Trade>toSysOut().withLabel("stream-join-trade"));

return txnStream;
```

### Przebuduj i uruchom aplikację
```bash
    mvn clean package
    docker build -t kafka-streams .
    docker run --network mynetwork kafka-streams
```

### Second solution using `leftJoin()` allows joining a stream with a table.
### In this case, if there is no price for an instrument, the price defaults to 0.
### We will add this join after the first one.

#### Create the instrument price table
#### Re-key the transactions stream by instrument and map values to amount, then write to a table

```java
KTable<String, Double> priceTable = pricesStream.selectKey((k, v) -> v.getInstrument()).mapValues(v -> v.getPrice().doubleValue()).toTable();
```

#### Join the transaction stream with the instrument price table
#### Use `leftJoin()` to perform the join
#### Use `Printed.<String, Trade>toSysOut().withLabel("table-join-trade")` to print the results to the console

```java
txnStream.selectKey((k, v) -> v.getInstrument())
                .leftJoin(priceTable, (txn, price) -> new Trade(
                        txn.getInstrument(),
                        txn.getAmount().doubleValue(),
                        price == null ? 0.0 : price
                )).print(Printed.<String, Trade>toSysOut().withLabel("table-join-trade"));
```

### Rebuild and run the application
```bash
    mvn clean package
    docker build -t kafka-streams .
    docker run --network mynetwork kafka-streams
```

### Implement the `DeduplicationStream.class`
### The goal of this task is to remove duplicate transactions in the stream.

### In this case, duplicates are defined as transactions with the same `id` that appeared within the last 10 minutes.

### Uncomment the code with duplicates in `SenderJob.class`.

### Implement the `DeduplicationTransformer.class`
### This class will implement the `Transformer` interface and will be used to remove duplicate transactions in the stream.

### Implement the `init()` method to access the state.
### Use `KeyValueStore<UUID, Long>` to store the last seen time of transactions.

```java
        private KeyValueStore<UUID, Long> store;
        
        @Override
        public void init(ProcessorContext context) {
            store = context.getStateStore("txn-store");
        }
```

### Implement the `transform()` method to process transactions and remove duplicates.
### In this method, check if a transaction with a given `id` has been seen in the last 10 minutes.
### If so, drop the transaction; otherwise, store the current time in the state and return the transaction.

```java
        @Override
        public KeyValue<String, Transaction> transform(String key, Transaction value) {
            UUID id = value.getId();
            long now = Instant.now().toEpochMilli();

            Long lastSeen = store.get(id);
            if (lastSeen == null || now - lastSeen > Duration.ofMinutes(10).toMillis()) {
                store.put(id, now);
                return new KeyValue<>(key, value);
            }
            return null; // drop duplicate
        }
```
### Implement the `close()` method to close the state.

```java
        @Override
        public void close() {
            // No specific cleanup needed for this example
        }
```

### Implement the `DeduplicationStream.class`

```java
    @Bean
public KStream<String, Transaction> aStream(StreamsBuilder builder) {
    JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);

    KStream<String, Transaction> transactions =
            builder.stream("streaming-transactions", Consumed.with(Serdes.String(), transactionSerde));


    builder.addStateStore(
            Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore("txn-store"),
                    Serdes.UUID(),
                    Serdes.Long()
            )
    );


    KStream<String, Transaction> deduplicated = transactions.transform(DeduplicationTransformer::new, "txn-store");

    deduplicated.print(Printed.<String, Transaction>toSysOut().withLabel("deduplicated stream"));

    return deduplicated;
}
```

### Implement the `AccessibleStore.class`
#### The goal of this task is to expose the application state through interactive queries.

### Uncomment the code in `ReaderJob.class` and run the application.

```java
//    @Scheduled(fixedRate = 5000, initialDelay = 5000)
//    public void queryStore() {
//        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
//        if (kafkaStreams == null) return;
//
//        ReadOnlyKeyValueStore<String, Double> store =
//                kafkaStreams.store(
//                        StoreQueryParameters.fromNameAndType("aggregated-store", QueryableStoreTypes.keyValueStore())
//                );
//
//        System.out.println("### INTERACTIVE QUERY ###");
//        KeyValueIterator<String, Double> iterator = store.all();
//        while (iterator.hasNext()) {
//            KeyValue<String, Double> entry = iterator.next();
//            System.out.println("Instrument: " + entry.key + " => Sum: " + entry.value);
//        }
//        iterator.close();
//    }
```

```java
  @Bean
    public KStream<String, String> aStream(StreamsBuilder builder) {
        JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
        KStream<String, Transaction> transactions =
                builder.stream("streaming-transactions", Consumed.with(Serdes.String(), transactionSerde));

        Aggregator<String, Double, Double> sumAggregator = (key, newVal, aggVal) -> aggVal + newVal;


        transactions
                .selectKey((k, v) -> v.getInstrument())
                .mapValues((k, v) -> v.getAmount().doubleValue())
                .groupByKey()
                .aggregate(
                        () -> 0.0,
                        sumAggregator,
                        Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as("aggregated-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double())
                );

        return null;
    }
```
