### ..3..2..1 GO

# INTRODUCTION

# ZAPOZNANIE

# PLAN NA DZISIAJ

### ARCH CENT
- opwiedziec o scentralizowanej
- problem sieciowy
- problem utraty danych
- problem skalowalnosci
- problem dostepnosci
- gereralnie SPOF

### ARCH DISTRIBUTED
- opowiedziec o rozproszonej na roznych przykladach
- CAP
- PACELC
- atrybuty jakosci w kontekscie rozproszonej
  - skalowanie horyzontalne
  - tolerancja na awarie
  - replikacja
  - wysoka dostepnosc
  - wyższa przepustowosc?
  - potencjalnie nizsze opoznienia

### HOW TO DISTRIBUTE DATA
- sharding
- replication
- partitioning

### HOW TO DISTRIBUTE COMPUTATION
- map reduce
- stream processing
- event sourcing
- CEP
- lambda architecture
- kappa architecture

### Dynamic vs static configuration

### COORDINATION PROBLEMS
- coordinator in distributed systems
- leader election
- distributed locks
- linearizability
- sequential consistency
- causal consistency
- eventual consistency

### CONSENSUS PROBLEMS
- consensus in distributed systems
- CAP theorem
- PACELC theorem
- FLP impossibility result
- Raft
- Paxos
- Zookeeper

### Consistent Core Architecture
- Zookeeper is a consistent core of distributed systems
- It provides a hierarchical namespace for storing metadata
- It is used for coordination, configuration management, and synchronization
-

EX1 ZOOKEEPER

### ZOOKEEPER GOOD PRACTICES
### ZOOKEER in DISTRIBUTED SYSTEMS

### INTRO TO KAFKA
- podstawowe zalozenia
- KOMUNIKACJA POINT TO POINT VS PUBLISH SUBSCRIBE
- QUEUE VS TOPIC
- PULL VS PUSH
- INTRO TO KAFKA, ELEMENTY PLATFORMY

### KAFKA BASICS
- skalowanie poprzed dodanie brokera
- producer consumer
- Broker discovery, komunikacja client server + sieć


### EXERCISE 2
### Start Kafka cluster and create first topics

### KAFKA BASICS
- topic, partycja
- replikacja i lider + follower
- key, value, offset
- retention 
- compaction

#### TOPICS

### EXERCISE 3 
### Start Kafka clients and produce/consume messages


- producer 
- conumser 


### KAFKA CLUSTER CONFIGURATION
- ogolne konfiguracje i parametry
- network
- storage
- memory
- cpu
- OS


# Create and connect to cluster
* Cluster name: Sages
* Bootstrap servers: kafka-1:29092


###  How Kafka relies on Zookeeper?

1. **Broker Metadata Management**: Zookeeper keeps track of all the brokers that form the Kafka cluster. It maintains a list of all the brokers and their metadata, which helps in managing the cluster.

2. **Leader Election**: Zookeeper is used to elect a leader for each partition. The leader is responsible for all reads and writes for the partition, and Zookeeper helps in electing a new leader if the current leader fails.

3. **Configuration Management**: Zookeeper stores configuration information for topics, such as the number of partitions, replication factor, and other settings.

4. **Cluster Membership**: Zookeeper keeps track of the live nodes in the cluster and helps in detecting node failures.

5. **Quotas**: Zookeeper is used to manage quotas for producers and consumers.

To check these functionalities in the Zookeeper shell, you can use the following commands:

### 1. List all brokers
```
ls /brokers/ids
```
This command lists all the broker IDs currently registered in the Kafka cluster.

### 2. Get broker metadata
```
get /brokers/ids/<broker-id>
```
This command retrieves metadata for a specific broker.

### 3. List all topics
```
ls /brokers/topics
```
This command lists all the topics in the Kafka cluster.

### 4. Get topic configuration
```
get /brokers/topics/<topic-name>
```
This command retrieves the configuration for a specific topic.

### 5. Check controller node
```
get /controller
```
This command retrieves information about the current controller node, which is responsible for managing the cluster.

### 6. Check live nodes
```
ls /zookeeper/quota
```
This command lists all the live nodes in the cluster.

### 7. Check consumer groups
```bash
ls /consumers
```
This command lists all the consumer groups in the Kafka cluster.

### KRAFT
- nowa implementacja KAFKI bez ZOOKEEPERA

### EXERCISE 5
### KAFKA CLIENTS PRODUCER

- key, value, offset & hashing
- ACK 
- LINGER & BATCH SIZE
- RETRIES & retry backoff & delivery timeout ms
- IDEMPOTENCE
- Compression

1. Konfiguracja 
2. Producer

### EXERCISE 6
### KAFKA CLIENTS CONSUMER

3. Consumer ( subscribe vs assign ) (poll vs commit) delivery semantics
consumer group,
poll, 
commit offset (strategie),
rebalance

### EXERCISE 7
### KAFKA SPRING PRODUCER

Konfiguracja factory
KafkaTemplate + JSON

### EXERCISE 8
### KAFKA SPRING CONSUMER

Config
KafkaListener + JSON
Error Handling
Dead letter queue

### TRANSACTIONAL OUTBOX PATTERN - teoria

----------
# DAY 2

### REST API VS EVENT DRIVEN API - teoria
- REST API pros and cons
- Event-driven architecture pros and cons
- CQRS
- Event sourcing
- Event-driven microservices
- Polyglot persistence

### ALTERNATIVE MESSAGING SYSTEMS - teoria (RabbitMQ, ActiveMQ, Pulsar, etc.)

### JSON LIMITATIONS - teoria
- What Json gives and what it takes away

### SERIALIZATION FRAMEWORKS - teoria
- What are serialization frameworks
- Avro, Protobuf
- Speed, size, schema evolution

### SCHEMA REGISTRY THEORY & ARCHITECTURE - teoria
- What is schema registry
- Why do we need schema registry
- How schema registry works
- Schema evolution
- Compatibility levels

### SCHEMA EVOLUTION - teoria
- Schema validation
- Schema versioning
- Schema history

### EXERCISE 9 
### KAFKA SPRING WITH AVRO

### KAFKA CONNECT THEORY & ARCHITECTURE - teoria
- What is Kafka Connect
- Why do we need Kafka Connect
- How Kafka Connect works
- Source vs Sink connectors
- Single message transforms
- Connectors vs Producers/Consumers
- Connectors vs Tasks

### KAFKA CONNECT API - teoria
- REST API
- Configuration
- Status
- Tasks
- Connectors

### EXERCISE 10
### KAFKA CONNECT IN PRACTICE WITH POSTGRES SOURCE


### EXERCISE 11
### KAFKA CONNECT IN PRACTICE WITH POSTGRES SINK

### EXERCISE 12
### KAFKA REST PROXY - teoria + praktyka
- What is Kafka REST Proxy
- Why do we need Kafka REST Proxy
- How Kafka REST Proxy works
- REST API

----------
# DAY 3

### KAFKA STREAMS THEORY & ARCHITECTURE - teoria
- What is Kafka Streams
- Why do we need Kafka Streams
- How Kafka Streams works
- Stream processing vs batch processing vs micro-batching
- Stateful vs stateless processing
- Exactly-once processing
- Fault tolerance
- State stores

### EXERCISE 11 
### KAFKA STREAMS SIMPLE TOPOLOGY

### EXERCISE 12
### KAFKA STREAMS KTABLE
- KTable
- KS

### KAFKA STREAMS WINDOWING - teoria
- Time-based windowing
- Session-based windowing
- Windowing operations

### EXERCISE 13
### KAFKA STREAMS WINDOWING

### KAFKA STREAMS JOIN basics - teoria
- Joins in Kafka Streams
- KStream-KTable join

### EXERCISE 14
### KAFKA STREAMS KTable - KStream JOIN

### KAFKA STREAMS SCALING
- Scaling Kafka Streams
- Partitioning


