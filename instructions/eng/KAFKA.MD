# Kafka

#### Start Kafka Cluster
```bash
  docker run -d --name=kafka-1 -p 29092:29092 --network mynetwork -e KAFKA_ZOOKEEPER_CONNECT=zzk-1:22181,zzk-2:32181,zzk-3:42181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka-1:29092 -e KAFKA_MIN_INSYNC_REPLICAS=2 confluentinc/cp-kafka:7.7.1
```

```bash
  docker run -d --name=kafka-2 -p 39092:39092 --network mynetwork -e KAFKA_ZOOKEEPER_CONNECT=zzk-1:22181,zzk-2:32181,zzk-3:42181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka-2:39092 -e KAFKA_MIN_INSYNC_REPLICAS=2 confluentinc/cp-kafka:7.7.1
```

```bash
  docker run -d --name=kafka-3 -p 49092:49092 --network mynetwork -e KAFKA_ZOOKEEPER_CONNECT=zzk-1:22181,zzk-2:32181,zzk-3:42181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka-3:49092 -e KAFKA_MIN_INSYNC_REPLICAS=2 confluentinc/cp-kafka:7.7.1
```

### Check container logs

```bash
    docker  ps
```

```bash
    docker  logs kafka-1 | grep started
```

```bash
    docker  logs kafka-2 | grep started
```

```bash
    docker  logs kafka-3 | grep started
```

### Cluster formation process

![img_22.png](images/img_22.png)

### Start Kafka UI

```bash
    docker  run -d -p 8080:8080 --network mynetwork --name=kafka-ui -e DYNAMIC_CONFIG_ENABLED=true provectuslabs/kafka-ui
```

### Create some topics

### Simple topic with 1 partition and 3 replicas
```bash
    docker  run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic test --partitions 1 --replication-factor 3 --if-not-exists --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

```bash
    docker  run --network mynetwork --rm confluentinc/cp-kafka:7.7.1 kafka-topics --describe --topic test --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

### Topic with multiple partitions

![img_19.png](images/img_19.png)

```bash
    docker  run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic test-multi-partitions --partitions 3 --replication-factor 3 --if-not-exists --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

```bash
    docker  run --network mynetwork --rm confluentinc/cp-kafka:7.7.1 kafka-topics --describe --topic test-multi-partitions --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

### Topic with short time-based retention

```bash
    docker  run --network mynetwork --rm confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic retention-ms-topic --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092 --partitions 3 --replication-factor 2 --config retention.ms=60000 --config segment.ms=60000
```

### Topic with small retention size

![img_20.png](images/img_20.png)

```bash
    docker  run --network mynetwork --rm confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic retention-bytes-topic --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092 --partitions 3 --replication-factor 2 --config retention.bytes=10240 --config segment.ms=60000
```

### Topic with custom min.insync.replicas

```bash
    docker  run --network mynetwork --rm confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic in-sync-topic --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092 --partitions 1 --replication-factor 3 --config min.insync.replicas=1
```

### Aggressive log compaction

![img_21.png](images/img_21.png)

```bash
    docker  run --network mynetwork --rm confluentinc/cp-kafka:7.7.1 kafka-topics --create --topic compacted-aggressive-topic --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092 --partitions 2 --replication-factor 3 --config cleanup.policy=compact --config min.cleanable.dirty.ratio=0.01 --config segment.ms=1000 --config segment.bytes=1048576
```

#### Console Producer

```bash
    docker  run -it --network mynetwork --rm confluentinc/cp-kafka:7.7.1 /bin/bash
```

```
kafka-console-producer --topic test --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092 
```

```
kafka-console-producer --topic test-multi-partitions --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092 --property "parse.key=true" --property "key.separator=:"
```

#### Console Consumer

```bash
    docker  run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-console-consumer --topic test --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092 --from-beginning
```

```bash
    docker  run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 kafka-console-consumer --topic test-multi-partitions --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092 --group my-consumer-group --from-beginning
```

### What does Zookeeper store for Kafka?

```bash
    docker  exec -it zzk-1 zookeeper-shell localhost:22181
```

#### This command shows all broker IDs currently registered in the Kafka cluster.
```
ls /brokers/ids
```

#### This command fetches metadata for a specific broker.
```
get /brokers/ids/<broker-id>
```

#### This command lists all topics in the Kafka cluster.
```
ls /brokers/topics
```

#### This command fetches configuration for a specific topic.
```
get /brokers/topics/<topic-name>
```

#### This command returns the current controller node managing the cluster.
```
get /controller
```

#### This command lists all active nodes in the cluster.
```
ls /zookeeper/quota
```

#### This command lists all consumer groups in the Kafka cluster.
```
ls /consumers
```
