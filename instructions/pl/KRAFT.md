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