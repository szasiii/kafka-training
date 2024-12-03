# KRAFT

### Wygeneruj cluster id

```bash
docker run --rm confluentinc/cp-kafka:7.7.1 kafka-storage random-uuid
```
### Na potrzeby ćwiczenia użyjemy własnego id `PBjDYCIhSEeWDX8tWbRJkg`

### Start brokerów

```bash
docker run -d --name=kraft-1 \
  -p 30092:30092 -p 30093:30093 \
  --network mynetwork \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LOG_DIRS=/tmp/kraft-logs \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@kraft-1:30093,2@kraft-2:40093,3@kraft-3:50093 \
  -e KAFKA_LISTENERS=PLAINTEXT://:30092,CONTROLLER://:30093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kraft-1:30092 \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_MIN_INSYNC_REPLICAS=2 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=3 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=3 \
  -e CLUSTER_ID=PBjDYCIhSEeWDX8tWbRJkg \
  confluentinc/cp-kafka:7.7.1

```

```bash
docker run -d --name=kraft-2 \
  -p 40092:40092 -p 40093:40093 \
  --network mynetwork \
  -e KAFKA_NODE_ID=2 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LOG_DIRS=/tmp/kraft-logs \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@kraft-1:30093,2@kraft-2:40093,3@kraft-3:50093 \
  -e KAFKA_LISTENERS=PLAINTEXT://:40092,CONTROLLER://:40093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kraft-2:40092 \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_MIN_INSYNC_REPLICAS=2 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=3 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=3 \
  -e CLUSTER_ID=PBjDYCIhSEeWDX8tWbRJkg \
  confluentinc/cp-kafka:7.7.1

```

```bash
docker run -d --name=kraft-3 \
  -p 50092:50092 -p 50093:50093 \
  --network mynetwork \
  -e KAFKA_NODE_ID=3 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LOG_DIRS=/tmp/kraft-logs \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@kraft-1:30093,2@kraft-2:40093,3@kraft-3:50093 \
  -e KAFKA_LISTENERS=PLAINTEXT://:50092,CONTROLLER://:50093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kraft-3:50092 \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_MIN_INSYNC_REPLICAS=2 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=3 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=3 \
  -e CLUSTER_ID=PBjDYCIhSEeWDX8tWbRJkg \
  confluentinc/cp-kafka:7.7.1

```

```bash
docker exec -it kraft-2 kafka-metadata-quorum --bootstrap-server kraft-1:30092 describe --status
```

# **Zalecenia Confluent dotyczące konfiguracji klastrów Kafka KRaft**

Confluent zaleca wybór między **oddzieleniem kontrolerów i brokerów** a **mieszaniem ról** w zależności od rozmiaru klastra, obciążenia i środowiska. Oto podsumowanie:

---

## **Zalecenia według scenariuszy**

| **Scenariusz**                                   | **Rekomendacja Confluent**                            |
|--------------------------------------------------|------------------------------------------------------|
| **Duże klastry produkcyjne**                     | Oddziel kontrolery i brokery                         |
| **Wysoka liczba partycji (np. tysiące)**         | Oddziel kontrolery i brokery                         |
| **Małe/średnie klastry (np. 3-6 węzłów)**        | Można mieszać role (kontrolery + brokery)            |
| **Środowiska deweloperskie lub testowe**         | Można mieszać role (kontrolery + brokery)            |

---

## **Dlaczego warto oddzielić kontrolery od brokerów?**

| **Korzyść**                  | **Opis**                                                                                  |
|------------------------------|------------------------------------------------------------------------------------------|
| **Izolacja błędów**          | Awaria kontrolera nie wpływa na przetwarzanie danych przez brokery i odwrotnie.          |
| **Optymalizacja zasobów**    | Możesz przypisać kontrolerom więcej CPU i pamięci, a brokerom większą przepustowość dysków i sieci. |
| **Skalowalność**             | Możesz niezależnie skalować kontrolery i brokery w zależności od potrzeb.               |
| **Wysoka wydajność**         | Dedykowane kontrolery zapewniają szybkie operacje metadanych w dużych klastrach.         |
| **Stabilność w dużych klastrach** | Przy tysiącach partycji dedykowane kontrolery zapewniają spójne i szybkie zarządzanie metadanymi. |

---

## **Dlaczego mieszanie ról może być odpowiednie?**

| **Korzyść**                  | **Opis**                                                                                  |
|------------------------------|------------------------------------------------------------------------------------------|
| **Prostsza architektura**    | Mniej węzłów do konfiguracji, wdrożenia i monitorowania.                                 |
| **Lepsze wykorzystanie zasobów** | W mniejszych klastrach zasoby są lepiej wykorzystane, obsługując jednocześnie dane i metadane. |
| **Niższy koszt infrastruktury** | Mniej węzłów oznacza mniejsze wymagania sprzętowe i operacyjne.                        |
| **Łatwiejsze skalowanie w małych klastrach** | Dodanie nowego węzła zwiększa zarówno pojemność dla danych, jak i zarządzanie metadanymi. |

---

## **Porównanie podejść**

| **Aspekt**                    | **Oddzielne kontrolery**                             | **Mieszane role (broker + kontroler)**               |
|-------------------------------|-----------------------------------------------------|-----------------------------------------------------|
| **Izolacja błędów**           | Lepsza – awaria kontrolera nie wpływa na brokery.    | Gorsza – awaria węzła wpływa na dane i metadane.    |
| **Skalowalność**              | Lepsza – niezależne skalowanie kontrolerów i brokerów. | Ograniczona – skalowanie wspólne dla obu ról.       |
| **Wydajność w dużych klastrach** | Lepsza – kontrolery mogą obsługiwać wysokie obciążenia metadanych. | Ograniczona – obciążenie metadanych może zakłócać przetwarzanie danych. |
| **Koszt infrastruktury**      | Wyższy – wymaga więcej węzłów.                      | Niższy – mniej węzłów do uruchomienia.              |
| **Złożoność zarządzania**     | Wyższa – oddzielne konfiguracje dla kontrolerów i brokerów. | Niższa – jedna konfiguracja dla węzłów.             |

---

## **Rekomendacje Confluent**

1. **Dla dużych klastrów produkcyjnych**:
    - Oddziel kontrolery i brokery, aby zwiększyć skalowalność, stabilność i wydajność.
2. **Dla małych/średnich klastrów**:
    - Można mieszać role, aby uprościć architekturę i obniżyć koszty.
3. **Dla środowisk deweloperskich lub testowych**:
    - Mieszane role są wystarczające, aby przyspieszyć wdrożenie i zmniejszyć złożoność.

---


# Porady konfiguracyjne

## Konfiguracja Apache Kafka i ZooKeeper - Tabele

### Konfiguracja systemu operacyjnego

| **Wielkość wdrożenia** | **Konfiguracja**                                                                                                                                                                                |
|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Mała (3 brokerów)**  | - Używaj **Linuxa (RHEL, Ubuntu lub CentOS)** z niskim opóźnieniem kernela.<br>- Ustaw **swappiness** na `1`.<br>- Zwiększ `ulimit` do obsługi 65 535+ otwartych plików.<br>- Wyłącz Transparent Huge Pages (THP). |
| **Średnia (12 brokerów)** | - Jak w małej konfiguracji.<br>- Optymalizuj stos TCP (`net.core.rmem_max`, `net.core.wmem_max`, `net.ipv4.tcp_rmem`, `net.ipv4.tcp_wmem`).<br>- Włącz balansowanie przerwań (IRQ balancing).                             |
| **Duża (20+ brokerów)** | - Jak w średniej konfiguracji.<br>- Skonfiguruj **NUMA**, aby równoważyć zasoby pamięci i CPU.<br>- Zarezerwuj procesory dla operacji systemowych za pomocą `isolcpus`.                                                |

---

### Konfiguracja CPU

| **Wielkość wdrożenia** | **Zalecane CPU**                                                                                                 |
|------------------------|------------------------------------------------------------------------------------------------------------------|
| **Mała (3 brokerów)**  | - **Rdzenie**: 4 rdzenie na brokera.<br>- **Częstotliwość zegara**: 2,5 GHz lub wyższa.<br>- Włącz Hyper-Threading.             |
| **Średnia (12 brokerów)** | - **Rdzenie**: 8-16 rdzeni na brokera.<br>- Zapewnij wysoką wydajność pojedynczego wątku.                                   |
| **Duża (20+ brokerów)** | - **Rdzenie**: 16+ rdzeni na brokera.<br>- Rozważ procesory z większymi pamięciami podręcznymi L3 dla obciążonych zadań.                  |

---

### Konfiguracja pamięci

| **Wielkość wdrożenia** | **Pamięć RAM**                                                                                                                                               |
|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Mała (3 brokerów)**  | - **Całkowita pamięć RAM**: 16 GB na brokera.<br>- Przypisz Kafka JVM Heap Size: 4 GB.<br>- Zarezerwuj pamięć na pamięć podręczną stron.                     |
| **Średnia (12 brokerów)** | - **Całkowita pamięć RAM**: 32 GB na brokera.<br>- Przypisz Kafka JVM Heap Size: 8 GB.<br>- Zapewnij wystarczającą pamięć podręczną stron.                   |
| **Duża (20+ brokerów)** | - **Całkowita pamięć RAM**: 64 GB na brokera.<br>- Przypisz Kafka JVM Heap Size: 8 GB.<br>- Dedykowana pamięć dla systemu operacyjnego i pamięci podręcznej. |

---

### Konfiguracja pamięci masowej

| **Wielkość wdrożenia** | **Zalecane rozwiązania pamięci masowej**                                                                                          |
|------------------------|------------------------------------------------------------------------------------------------------------------|
| **Mała (3 brokerów)**  | - Używaj **dysków SSD NVMe**.<br>- Planuj **2x-3x więcej miejsca na dysku** w stosunku do zasad retencji.<br>- Oddziel dane systemowe od dzienników Kafka.  |
| **Średnia (12 brokerów)** | - Używaj **SSD klasy enterprise** lub **RAID 10**.<br>- Przepustowość dysku: >=500 MB/s.<br>- Przypisz 2-4 dyski na brokera.    |
| **Duża (20+ brokerów)** | - Jak w średniej konfiguracji.<br>- Rozważ pamięć współdzieloną (np. SAN) dla przełączania awaryjnego.<br>- Używaj **JBOD** do izolacji awarii dysków.  |

---

### Konfiguracja Kafka

| **Wielkość wdrożenia** | **Ustawienia Kafka**                                                                                                    |
|------------------------|-----------------------------------------------------------------------------------------------------------------------|
| **Mała (3 brokerów)**  | - **Replication Factor**: 3.<br>- **Partitions**: Rozkładaj równomiernie między brokerami.<br>- Retencja dzienników: 7-14 dni.       |
| **Średnia (12 brokerów)** | - **Replication Factor**: 3.<br>- **Partitions**: Zwiększ, aby wspierać równoległość.<br>- Włącz **rack awareness**.  |
| **Duża (20+ brokerów)** | - **Replication Factor**: 3.<br>- Optymalizuj liczbę partycji dla obciążenia.<br>- Włącz **log compaction** dla kluczowych tematów.  |

---

### Zalecenia dotyczące ZooKeeper

| **Wielkość wdrożenia** | **Liczba węzłów ZooKeeper** | **Konfiguracja**                                                                                                                                                                   |
|------------------------|-----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Mała (3 brokerów)**  | 3 węzły                     | - Uruchom 3 węzły ZooKeeper na oddzielnych maszynach fizycznych lub wirtualnych.<br>- Używaj SSD do przechowywania danych.<br>- Przypisz 4 GB RAM na węzeł.<br>- Sieć: 1 Gbps NIC. |
| **Średnia (12 brokerów)** | 5 węzłów                    | - Uruchom 5 węzłów ZooKeeper dla zwiększonej odporności.<br>- Używaj SSD do przechowywania danych.<br>- Przypisz 8 GB RAM na węzeł.<br>- Zalecana sieć: 10 Gbps NIC.               |
| **Duża (20+ brokerów)** | 5 węzłów                    | - Uruchom 5 węzłów ZooKeeper do obsługi dużych obciążeń.<br>- Używaj dysków SSD klasy enterprise lub NVMe.<br>- Przypisz 16 GB RAM na węzeł.<br>- Sieć: 10 Gbps NIC.               |

---

### Parametry konfiguracji ZooKeeper

| **Parametr**                  | **Zalecane ustawienie**                                                                                                                                                           |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `tickTime`                     | Ustaw na `2000` (2 sekundy). Jest to podstawowa jednostka czasu w ZooKeeper, używana do pingów i timeoutów.                                                                     |
| `initLimit`                    | Ustaw na `10`. Pozwala na 10 jednostek `tickTime` na synchronizację węzłów z liderem.                                                                                           |
| `syncLimit`                    | Ustaw na `5`. Pozwala węzłom podrzędnym opóźniać się za liderem o 5 jednostek `tickTime`.                                                                                       |
| `dataDir`                      | Używaj dedykowanego katalogu na SSD do przechowywania dzienników transakcji.                                                                                                     |
| `maxClientCnxns`               | Ustaw na `60` (lub więcej, w zależności od liczby brokerów Kafka). Ogranicza liczbę połączeń na klienta.                                                                          |
| `autopurge.snapRetainCount`    | Ustaw na `10`. Zachowuje 10 migawkowych kopii podczas automatycznego czyszczenia.                                                                                                |
| `autopurge.purgeInterval`      | Ustaw na `24` godziny. Automatyczne czyszczenie dzienników i migawek co dobę.                                                                                                    |

---

### Parametry Kafka - co unikać

| **Parametr**               | **Zalecane ustawienie**                     | **Powód**                                                                                                                                       |
|-----------------------------|---------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `unclean.leader.election.enable` | `false`                                  | Zapobiega nieczystym wyborom lidera, co może prowadzić do utraty danych, jeśli liderem zostanie niesynchronizowana replika.                      |
| `log.retention.ms`          | Ostrożnie dostosuj (np. `604800000` dla 7 dni). | Unikaj zbyt krótkich okresów retencji, co może prowadzić do nieumyślnej utraty danych.                                                           |
| `replica.lag.time.max.ms`   | Domyślnie: `10000` (10 sekund). Dostosuj w razie potrzeby. | Wyższa wartość pozwala replikom nadrobić opóźnienie, ale może opóźnić przełączanie awaryjne podczas przestojów.                                   |
| `log.segment.bytes`         | Domyślnie: `1 GB`. Dostosuj do obciążenia.  | Unikaj zbyt dużych segmentów, które mogą wydłużyć czas odzyskiwania, lub zbyt małych, które mogą zwiększyć obciążenie dysku.                     |
| `min.insync.replicas`       | Ustaw na `2` lub więcej.                   | Zapewnia, że wystarczająca liczba replik jest zsynchronizowana, aby potwierdzić zapis, poprawiając trwałość i tolerancję błędów.                  |
| `delete.topic.enable`       | `true`                                      | Upewnij się, że ta opcja jest włączona, jeśli planujesz usuwać tematy; w przeciwnym razie mogą pozostawać w stanie oczekującym na usunięcie.      |
| `log.dirs`                  | Oddziel katalogi dla dzienników.           | Używaj wielu katalogów dzienników, aby rozłożyć obciążenie I/O na wiele dysków i zapobiec wpływowi awarii pojedynczego dysku na wszystkie dane. |

---

### Dodatkowe najlepsze praktyki

**Monitoring i skalowanie**:
    - Wdroż narzędzia monitorujące (np. Prometheus, Grafana).
    - Dostosowuj konfiguracje na podstawie metryk, takich jak obciążenie CPU, I/O dysku i opóźnienia sieciowe.

**Sieci**:
    - Małe wdrożenia mogą używać NIC 1 Gbps.
    - Średnie i duże wdrożenia powinny używać NIC 10 Gbps dla odpowiedniej przepustowości.

**Utrzymanie klastra**:
    - Regularnie równoważ partycje po dodaniu/usunięciu brokerów.
    - Testuj scenariusze przełączania awaryjnego, aby zapewnić wysoką dostępność.

**Bezpieczeństwo**:
    - Używaj TLS do komunikacji między brokerami Kafka i węzłami ZooKeeper.
    - Włącz uwierzytelnianie ZooKeeper za pomocą `digest` lub `Kerberos`.

**Testowanie awarii**:
    - Regularnie testuj wybory lidera i scenariusze awarii, aby upewnić się, że klaster działa poprawnie w przypadku awarii węzła.

# Narzędzia do Testowania Kafka i Aplikacji Java z Kafka

| **Cel Testowania**            | **Narzędzie**                      | **Opis**                                                                                                                                                          |
|--------------------------------|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Wydajność Kafka**            | `kafka-producer-perf-test.sh`      | Wbudowane narzędzie do testowania producentów Kafka; pozwala symulować duże obciążenia.                                                                           |
|                                | `kafka-consumer-perf-test.sh`      | Wbudowane narzędzie do testowania konsumentów Kafka; mierzy przepustowość i opóźnienia.                                                                          |
|                                | **JMeter z wtyczką Kafka**         | Narzędzie do testów wydajnościowych z możliwością symulacji dużych obciążeń i scenariuszy testowych.                                                              |
|                                | **Chaos Monkey/Gremlin**           | Testowanie stabilności poprzez symulowanie awarii (np. awaria ZooKeeper, przerwanie połączenia).                                                                 |
|                                | **Trogdor (Kafka Benchmarks)**     | Narzędzie Kafka do testowania wydajności, opóźnień i obciążenia klastra. Możliwość tworzenia scenariuszy testowych.                                              |
| **Testy aplikacji Java**       | **Testcontainers**                 | Umożliwia uruchamianie izolowanych instancji Kafka w kontenerach Docker w ramach testów integracyjnych.                                                          |
|                                | **Embedded Kafka (Spring Kafka)**  | Wbudowany serwer Kafka do testów jednostkowych i integracyjnych w aplikacjach Spring.                                                                            |
|                                | **MockProducer i MockConsumer**    | Narzędzia do testowania aplikacji z wykorzystaniem symulowanych producentów i konsumentów.                                                                       |
|                                | **JUnit z Kafka Testutils**        | Narzędzie do testowania aplikacji z użyciem JUnit i pomocniczych narzędzi Kafka.                                                                                 |
| **Testy Kafka Streams**        | **TopologyTestDriver**             | Narzędzie Kafka Streams do testów jednostkowych topologii przetwarzania; umożliwia symulację strumieni wejściowych i wyjściowych.                                 |
|                                | **Embedded Kafka**                 | Umożliwia testy end-to-end z rzeczywistą komunikacją między producentami, konsumentami i topologiami Streams.                                                    |
|                                | **Testcontainers**                 | Testowanie aplikacji Kafka Streams w izolowanym środowisku kontenerów Docker.                                                                                    |
|                                | **WireMock**                       | Mockowanie zewnętrznych usług HTTP używanych przez aplikacje Kafka Streams.                                                                                      |
|                                | **Chaos Engineering (Gremlin)**    | Testowanie odporności Kafka Streams na awarie, takie jak przerwanie komunikacji między brokerami czy awarie partycji.                                            |

---
