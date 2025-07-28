# Configuration Guidelines

## Apache Kafka & ZooKeeper Configuration – Tables

### Operating System Configuration

| **Deployment Size** | **Configuration**                                                                                                                                                                                |
|---------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Small (3 brokers)**  | - Use **Linux (RHEL, Ubuntu, or CentOS)** with a low-latency kernel.<br>- Set **swappiness** to `1`.<br>- Increase `ulimit` to support 65,535+ open files.<br>- Disable Transparent Huge Pages (THP). |
| **Medium (12 brokers)** | - Same as small.<br>- Optimize TCP stack (`net.core.rmem_max`, `net.core.wmem_max`, `net.ipv4.tcp_rmem`, `net.ipv4.tcp_wmem`).<br>- Enable IRQ balancing. |
| **Large (20+ brokers)** | - Same as medium.<br>- Configure **NUMA** for balanced memory and CPU usage.<br>- Reserve CPUs for OS operations using `isolcpus`. |

---

### CPU Configuration

| **Deployment Size** | **Recommended CPU**                                                                                               |
|---------------------|--------------------------------------------------------------------------------------------------------------------|
| **Small (3 brokers)**  | - **Cores**: 4 per broker.<br>- **Clock speed**: 2.5 GHz or higher.<br>- Enable Hyper-Threading.                          |
| **Medium (12 brokers)** | - **Cores**: 8–16 per broker.<br>- Ensure strong single-thread performance.                                      |
| **Large (20+ brokers)** | - **Cores**: 16+ per broker.<br>- Consider CPUs with larger L3 cache for high-load tasks.                        |

---

### Memory Configuration

| **Deployment Size** | **RAM**                                                                                                                                       |
|---------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| **Small (3 brokers)**  | - **Total RAM**: 16 GB per broker.<br>- Assign Kafka JVM heap size: 4 GB.<br>- Reserve RAM for page cache.                                     |
| **Medium (12 brokers)** | - **Total RAM**: 32 GB per broker.<br>- Assign Kafka JVM heap size: 8 GB.<br>- Ensure sufficient page cache.                                  |
| **Large (20+ brokers)** | - **Total RAM**: 64 GB per broker.<br>- Kafka JVM heap size: 8 GB.<br>- Reserve RAM for OS and page cache.                                    |

---

### Storage Configuration

| **Deployment Size** | **Recommended Storage**                                                                                                       |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------|
| **Small (3 brokers)**  | - Use **NVMe SSDs**.<br>- Plan for **2–3x disk space** relative to retention policies.<br>- Separate system and Kafka log data.   |
| **Medium (12 brokers)** | - Use **enterprise-grade SSDs** or **RAID 10**.<br>- Disk throughput: >=500 MB/s.<br>- Assign 2–4 disks per broker.          |
| **Large (20+ brokers)** | - Same as medium.<br>- Consider shared storage (e.g. SAN) for failover.<br>- Use **JBOD** to isolate disk failures.        |

---

### Kafka Configuration

| **Deployment Size** | **Kafka Settings**                                                                                                   |
|---------------------|------------------------------------------------------------------------------------------------------------------------|
| **Small (3 brokers)**  | - **Replication Factor**: 3.<br>- **Partitions**: Distribute evenly across brokers.<br>- Log retention: 7–14 days.           |
| **Medium (12 brokers)** | - **Replication Factor**: 3.<br>- **Partitions**: Increase to support parallelism.<br>- Enable **rack awareness**.           |
| **Large (20+ brokers)** | - **Replication Factor**: 3.<br>- Tune partitions for workload.<br>- Enable **log compaction** for key topics.               |

---

### ZooKeeper Recommendations

| **Deployment Size** | **ZooKeeper Nodes** | **Configuration**                                                                                                                                                                  |
|---------------------|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Small (3 brokers)**  | 3 nodes             | - Run 3 ZooKeeper nodes on separate physical or virtual machines.<br>- Use SSDs for data.<br>- Assign 4 GB RAM per node.<br>- Network: 1 Gbps NIC.                                  |
| **Medium (12 brokers)** | 5 nodes            | - Run 5 ZooKeeper nodes for higher fault tolerance.<br>- Use SSDs for data.<br>- Assign 8 GB RAM per node.<br>- Recommended network: 10 Gbps NIC.                                  |
| **Large (20+ brokers)** | 5 nodes            | - Run 5 ZooKeeper nodes for high loads.<br>- Use enterprise-grade or NVMe SSDs.<br>- Assign 16 GB RAM per node.<br>- Network: 10 Gbps NIC.                                         |

---

### ZooKeeper Configuration Parameters

| **Parameter**               | **Recommended Setting**                                                                                          |
|----------------------------|-------------------------------------------------------------------------------------------------------------------|
| `tickTime`                 | Set to `2000` (2 seconds). Basic time unit for ZooKeeper (used for pings and timeouts).                         |
| `initLimit`                | Set to `10`. Allows 10 `tickTime` units for node-to-leader sync.                                                 |
| `syncLimit`                | Set to `5`. Follower nodes can lag up to 5 `tickTime` units behind the leader.                                  |
| `dataDir`                  | Use dedicated SSD-based directory for transaction logs.                                                          |
| `maxClientCnxns`           | Set to `60` or more depending on Kafka broker count. Limits connections per client.                             |
| `autopurge.snapRetainCount` | Set to `10`. Keeps 10 snapshot copies during auto-cleanup.                                                      |
| `autopurge.purgeInterval` | Set to `24`. Purges logs and snapshots every 24 hours.                                                           |

---

### Kafka Parameters – What to Avoid

| **Parameter**                   | **Recommended Setting**             | **Reason**                                                                                             |
|----------------------------------|-------------------------------------|----------------------------------------------------------------------------------------------------------|
| `unclean.leader.election.enable` | `false`                             | Prevents data loss by avoiding unsynced replicas becoming leaders.                                       |
| `log.retention.ms`              | Tune carefully (e.g. `604800000` for 7 days) | Avoid too short retention periods that may cause unexpected data loss.                        |
| `replica.lag.time.max.ms`       | Default: `10000` (10s); adjust as needed | Higher value gives replicas more time to catch up, but delays failover in downtime.           |
| `log.segment.bytes`             | Default: `1 GB`; tune based on load  | Avoid segments that are too large (slow recovery) or too small (high I/O overhead).           |
| `min.insync.replicas`           | Set to `2` or more                  | Ensures enough replicas are in sync to confirm writes – improves durability and fault tolerance. |
| `delete.topic.enable`           | `true`                              | Enable if you plan to delete topics – prevents orphaned topics stuck in "deletion" state.       |
| `log.dirs`                      | Separate log directories            | Use multiple log directories across different disks to spread I/O load and isolate disk failures. |

---

### Additional Best Practices

**Monitoring & Scaling**:
- Deploy monitoring tools (e.g. Prometheus, Grafana).
- Adjust configs based on metrics like CPU load, disk I/O, and network latency.

**Networking**:
- Small setups can use 1 Gbps NICs.
- Medium and large setups should use 10 Gbps NICs for proper throughput.

**Cluster Maintenance**:
- Rebalance partitions after adding/removing brokers.
- Test failover scenarios regularly to ensure high availability.

**Security**:
- Use TLS for communication between Kafka brokers and ZooKeeper nodes.
- Enable ZooKeeper authentication via `digest` or `Kerberos`.

**Failure Testing**:
- Regularly test leader elections and failure scenarios to ensure cluster stability during outages.

---

# Tools for Testing Kafka and Java Apps with Kafka

| **Test Purpose**              | **Tool**                        | **Description**                                                                                                                |
|------------------------------|----------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| **Kafka Performance**        | `kafka-producer-perf-test.sh`   | Built-in tool to benchmark Kafka producers with simulated load.                                                                |
|                              | `kafka-consumer-perf-test.sh`   | Built-in tool to benchmark Kafka consumers, measuring throughput and latency.                                                  |
|                              | **JMeter with Kafka plugin**     | Performance testing tool to simulate load and test Kafka scenarios.                                                            |
|                              | **Chaos Monkey / Gremlin**       | Stability testing by simulating failures (e.g. ZooKeeper outage, broker disconnect).                                           |
|                              | **Trogdor (Kafka Benchmarks)**   | Kafka's performance testing tool for latency, throughput, and stress test scenarios.                                           |
| **Java App Testing**         | **Testcontainers**              | Run Kafka in isolated Docker containers for integration testing.                                                               |
|                              | **Embedded Kafka (Spring Kafka)**| In-memory Kafka for unit and integration testing in Spring-based apps.                                                         |
|                              | **MockProducer & MockConsumer**  | Tools for mocking producers/consumers in unit tests.                                                                           |
|                              | **JUnit with Kafka TestUtils**   | Integration testing using JUnit and Kafka helper utilities.                                                                    |
| **Kafka Streams Testing**    | **TopologyTestDriver**           | Kafka Streams unit testing tool for simulating input/output streams.                                                           |
|                              | **Embedded Kafka**               | Enables end-to-end testing between producers, consumers, and stream topologies.                                                |
|                              | **Testcontainers**               | Run Kafka Streams in Docker for isolated environment testing.                                                                  |
|                              | **Chaos Engineering (Gremlin)**  | Test fault tolerance of Kafka Streams (e.g. broker outages, partition loss, message delay).                                    |

---
