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
|                                | **Chaos Engineering (Gremlin)**    | Testowanie odporności Kafka Streams na awarie, takie jak przerwanie komunikacji między brokerami czy awarie partycji.                                            |

---
