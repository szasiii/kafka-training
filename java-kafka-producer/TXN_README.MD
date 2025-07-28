### Konfiguracja i uruchomienie Kafka Producer

```java
public static KafkaProducer<String, String> createProducer() {
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, UUID.randomUUID().toString());
    return new KafkaProducer<>(properties);
}
```
### Transakcje w Apache Kafka (z kodem Java)

Kafka wspiera **semantykę exactly-once (EOS)** dzięki mechanizmowi transakcji. Oto szczegółowe omówienie kodu transakcyjnego producenta:

---

### Krok po kroku

### 1. **Inicjalizacja transakcji**

```java
producer.initTransactions();
```

**Co się dzieje:**
- Producent łączy się z **Koordynatorem Transakcji** (specjalny broker odpowiedzialny za zarządzanie stanem transakcji).
- Rejestrowany jest `transactional.id` (ustawiany w konfiguracji producenta).
- Jeśli to **pierwsze uruchomienie**, Kafka inicjalizuje metadane producenta na koordynatorze.
- Przy ponownym uruchomieniu Kafka sprawdza i **odzyskuje niedokończone transakcje** (np. automatycznie je przerywa).

Koordynator zapisuje metadane transakcji w wewnętrznym temacie: `__transaction_state`

---

### 2. **Rozpoczęcie nowej transakcji**

```java
producer.beginTransaction();
```

**Co się dzieje:**
- Producent wchodzi w **tryb transakcyjny**.
- Wszystkie wiadomości wysyłane od tego momentu są **tagowane PID-em (Producer ID)** i metadanymi transakcji.
- Broker **nie udostępnia** tych wiadomości konsumentom, dopóki transakcja nie zostanie zatwierdzona.

Dane są zapisywane jako *niewidoczne* w partycjach Kafka.

---

### 3. **Wysyłanie wiadomości w ramach transakcji**

```java
for (int i = 0; i < 10; i++) {
    ProducerRecord<String, String> record = new ProducerRecord<>(topic, "key-" + i, "value-" + i);
    producer.send(record);
    Thread.sleep(3000);
}
```

**Co się dzieje:**
- Każda wiadomość trafia do odpowiedniej partycji i jest **buforowana** przez brokera.
- Producent dołącza **PID** i **numer sekwencji**, aby zapewnić kolejność i idempotencję.
- Brokery przechowują wiadomości jako **tymczasowe**, niedostępne jeszcze dla konsumentów.

Wiadomości będą widoczne dopiero po zatwierdzeniu transakcji.

---

### 4. **Zatwierdzenie transakcji**

```java
producer.commitTransaction();
```

**Co się dzieje:**
- Producent wysyła sygnał **commit** do Koordynatora Transakcji.
- Koordynator:
    - Zapisuje wpis typu **COMMIT** w temacie `__transaction_state`.
    - Informuje wszystkie brokery, że transakcja została zatwierdzona.
- Brokery **udostępniają** zapisane wiadomości konsumentom.

Kafka zapewnia **atomowość**: dane są albo w całości widoczne, albo wcale.

---

## Co dzieje się w środku (komponenty Kafka)

| Komponent                 | Rola                                                                                   |
|---------------------------|----------------------------------------------------------------------------------------|
| **Producent transakcyjny** | Taguje wiadomości metadanymi transakcji i komunikuje się z koordynatorem.              |
| **Koordynator Transakcji** | Zarządza cyklem życia transakcji, zapisuje dane w `__transaction_state`.               |
| **Broker Kafka**           | Buforuje dane transakcyjne i udostępnia je po commit/abort.                           |
| **Konsument (read_committed)** | Odczytuje tylko **zatwierdzone transakcje** – ignoruje dane niezatwierdzone lub przerwane. |
| **Temat `__transaction_state`** | Wewnętrzny temat do śledzenia statusu transakcji (BEGIN, COMMIT, ABORT).           |

---

### Scenariusze awaryjne

- Jeśli nie zostanie wywołane `commitTransaction()` (np. awaria aplikacji), koordynator **automatycznie przerwie transakcję**.
- Możesz jawnie przerwać transakcję wywołując `abortTransaction()`.
- Transakcje wygasają po czasie bezczynności (`transaction.timeout.ms`).

---

### Podsumowanie

Transakcje w Kafka pozwalają na:
- **Atomowy zapis** do wielu partycji,
- **Exactly-once processing**, gdy używamy idempotentnych producentów i konsumentów w trybie `read_committed`.

To kluczowa funkcjonalność w aplikacjach takich jak:
- Finanse i bankowość 
- Obsługa zamówień i płatności 
- Przetwarzanie danych w czasie rzeczywistym


### Kompletna implementacja producenta z transakcjami

```java
public static void main(String[] args) throws Exception{
    KafkaProducer<String, String> producer = createProducer();
    producer.initTransactions();
    String topic = "kafka-client-txn";
    producer.beginTransaction();
    for (int i = 0; i < 10; i++) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, "key-" + i, "value-" + i);
        producer.send(record);
        Thread.sleep(3000);
    }
    producer.commitTransaction();
    producer.close();
}
```

## Jak uzyskać gwarancję exactly‑once w Apache Kafka

### Włącz idempotencję producenta 
W konfiguracji ustaw `enable.idempotence=true`. Zapobiega to duplikowaniu rekordów, nawet jeśli producent ponawia wysyłkę.

### Używaj transakcji
Skonfiguruj unikalny transactional.id dla każdej instancji producenta. Pozwala to grupować wiele wywołań send() w jedną atomową operację.

### Prześlij offsety w ramach transakcji (gdy budujesz pipeline)
Jeśli aplikacja jednocześnie czyta i pisze do Kafki, wywołaj `producer.sendOffsetsToTransaction(offsets, groupId);`, aby zapisać bieżące offsety konsumenta w tej samej transakcji.

### Zatwierdzaj lub cofaj transakcje
Po wysłaniu wszystkich rekordów wywołaj `producer.commitTransaction()`. W razie błędu użyj `producer.abortTransaction()`, aby wycofać zmiany.

### Skonfiguruj konsumenta
Ustaw `isolation.level=read_committed`, by odczytywać wyłącznie rekordy z pomyślnie zatwierdzonych transakcji.


# ENG

### Kafka Producer Configuration and Startup

```java
public static KafkaProducer<String, String> createProducer() {
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, UUID.randomUUID().toString());
    return new KafkaProducer<>(properties);
}
```

---

## Transactions in Apache Kafka (with Java Code)

Kafka supports **exactly-once semantics (EOS)** using transactions. Here's a detailed breakdown of how a transactional producer works:

---

### Step-by-Step Breakdown

#### 1. **Initialize the Transaction**

```java
producer.initTransactions();
```

**What happens:**
- The producer connects to a **Transaction Coordinator** (a special broker managing transaction state).
- The `transactional.id` is registered (set in the config).
- If it's the **first run**, Kafka initializes transaction metadata on the coordinator.
- On restart, Kafka checks and **recovers unfinished transactions** (e.g. auto-aborts them).

Coordinator writes to internal topic: `__transaction_state`.

---

#### 2. **Begin a New Transaction**

```java
producer.beginTransaction();
```

**What happens:**
- The producer enters **transactional mode**.
- All messages sent after this are **tagged with the Producer ID (PID)** and transaction metadata.
- Brokers **buffer these records**, making them invisible to consumers until commit.

---

#### 3. **Send Records in a Transaction**

```java
for (int i = 0; i < 10; i++) {
    ProducerRecord<String, String> record = new ProducerRecord<>(topic, "key-" + i, "value-" + i);
    producer.send(record);
    Thread.sleep(3000);
}
```

**What happens:**
- Messages are routed to partitions and **buffered** by brokers.
- Each message includes a **PID** and **sequence number** for ordering and idempotency.
- Messages are invisible until the transaction is committed.

---

#### 4. **Commit the Transaction**

```java
producer.commitTransaction();
```

**What happens:**
- The producer sends a **commit signal** to the Transaction Coordinator.
- The coordinator:
    - Writes a **COMMIT marker** to `__transaction_state`.
    - Notifies brokers to **make buffered messages visible** to consumers.

Kafka ensures **atomic visibility**: all or nothing.

---

## Under-the-Hood Kafka Components

| Component                  | Role                                                                 |
|---------------------------|----------------------------------------------------------------------|
| **Transactional Producer** | Tags records with metadata and communicates with coordinator.        |
| **Transaction Coordinator** | Manages transaction lifecycle, persists to `__transaction_state`.    |
| **Kafka Broker**           | Buffers transactional data and publishes upon commit/abort.          |
| **Consumer (read_committed)** | Only reads **committed transactions**, ignores in-flight or aborted. |
| **`__transaction_state` topic** | Internal topic that tracks transaction status (BEGIN, COMMIT, ABORT).  |

---

### Failure Scenarios

- If `commitTransaction()` is not called (e.g., app crash), the coordinator **automatically aborts** the transaction.
- You can explicitly abort using `abortTransaction()`.
- Transactions time out after `transaction.timeout.ms`.

---

### Summary

Kafka transactions allow:
- **Atomic writes** across multiple partitions,
- **Exactly-once processing**, when used with idempotent producers and `read_committed` consumers.

Useful for:
- Finance and banking
- Order processing and payments
- Real-time data pipelines

---

### Full Producer Transaction Example

```java
public static void main(String[] args) throws Exception {
    KafkaProducer<String, String> producer = createProducer();
    producer.initTransactions();
    String topic = "kafka-client-txn";
    producer.beginTransaction();
    for (int i = 0; i < 10; i++) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, "key-" + i, "value-" + i);
        producer.send(record);
        Thread.sleep(3000);
    }
    producer.commitTransaction();
    producer.close();
}
```

---

## How to Ensure Exactly-Once Semantics in Kafka

### ✅ Enable Idempotence

Set `enable.idempotence=true`. Ensures no duplicate messages even during retries.

---

### ✅ Use Transactions

Configure a unique `transactional.id` for each producer instance. Groups multiple `send()` calls into a single atomic unit.

---

### ✅ Commit Offsets Within Transaction (For Pipelines)

If consuming and producing within the same app, call:

```java
producer.sendOffsetsToTransaction(offsets, groupId);
```

This commits offsets as part of the transaction.

---

### ✅ Commit or Abort Explicitly

- Call `commitTransaction()` after all `send()` calls.
- Use `abortTransaction()` to rollback if needed.

---

### ✅ Configure Consumers Correctly

Set:

```java
isolation.level = read_committed
```

So consumers only see **committed** messages.

