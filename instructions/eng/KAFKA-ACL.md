# Secure Kafka and Zookeeper Setup with ACL Security

This comprehensive guide demonstrates how to set up and secure a production-like Kafka cluster with ZooKeeper using advanced security features including SASL authentication and Access Control Lists (ACLs).

## Overview

This setup demonstrates:
- **3-node ZooKeeper ensemble** with full SASL authentication (client + quorum)
- **3-broker Kafka cluster** with SCRAM-SHA-512 authentication
- **Interbroker security** with authenticated communication
- **ACL-based authorization** for fine-grained access control
- **Security testing** to verify authentication and authorization
- **Docker containerized** deployment for easy management

## Security Architecture

### Authentication Layers:
1. **ZooKeeper SASL**: Secures ZK client connections and server-to-server communication
2. **Kafka SCRAM-SHA-512**: Strong password-based authentication for all Kafka connections
3. **Interbroker Authentication**: Brokers authenticate to each other using SCRAM credentials

### Authorization:
- **ACL System**: Fine-grained permissions for topics, consumer groups, and cluster operations
- **Principal-based Access**: User-specific permissions (User:username format)
- **Operation-based Control**: READ, WRITE, CREATE, DELETE, ALTER permissions
- **Resource-based Security**: Topic-level and cluster-level access control

## Prerequisites and Setup

### 1. Docker Environment
Create a dedicated network for secure communication between containers:
```bash
docker network create mynetwork
```

### 2. JAAS Configuration Files
The `secrets/` directory contains authentication configurations:

#### Server Authentication:
- **`zk-jaas.conf`** - ZooKeeper server authentication with both client and quorum SASL
- **`kafka-X-jaas.conf`** - Individual broker authentication (X = 1,2,3)

#### Client Authentication (pre-created):
- **`admin-client.properties`** - Administrative client with full permissions
- **`producer-client.properties`** - Producer client with write permissions
- **`consumer-client.properties`** - Consumer client with read permissions
- **`limited-client.properties`** - Limited client with restricted access
- **`broker-test-client.properties`** - Broker credentials for testing
- **`broker-admin-client.properties`** - Broker credentials for ACL management
- **`wrong-credentials.properties`** - Invalid credentials for testing failures

### 3. Security Credentials Summary

| Component | Username | Password | Purpose |
|-----------|----------|----------|---------|
| ZooKeeper Server | zookeeper | zk-server-secret | Server-to-server auth |
| ZooKeeper Client | kafka | zk-client-secret | Kafka-to-ZK connection |
| Kafka Broker 1 | kafka-broker-1 | broker-1-secret | Broker authentication |
| Kafka Broker 2 | kafka-broker-2 | broker-2-secret | Broker authentication |
| Kafka Broker 3 | kafka-broker-3 | broker-3-secret | Broker authentication |
| Admin User | admin | admin-secret | Full cluster access |
| Producer User | producer | producer-secret | Topic write access |
| Consumer User | consumer | consumer-secret | Topic read access |

## Step 1: Start Zookeeper Ensemble

### 1.1 Start Zookeeper Node 1
```bash
docker run -d -p 22181:22181 -p 23888:23888 --name=zzk-1 --network mynetwork \
  -v $PWD/secrets/zk-jaas.conf:/etc/kafka/jaas.conf:ro \
  -e KAFKA_OPTS="-Djava.security.auth.login.config=/etc/kafka/jaas.conf" \
  -e ZOOKEEPER_SERVER_ID=1 \
  -e ZOOKEEPER_CLIENT_PORT=22181 \
  -e ZOOKEEPER_TICK_TIME=2000 \
  -e ZOOKEEPER_INIT_LIMIT=5 \
  -e ZOOKEEPER_SYNC_LIMIT=2 \
  -e ZOOKEEPER_SERVERS="zzk-1:22888:23888;zzk-2:32888:33888;zzk-3:42888:43888" \
  -e ZOOKEEPER_AUTH_PROVIDER_SASL=org.apache.zookeeper.server.auth.SASLAuthenticationProvider \
  -e ZOOKEEPER_REQUIRE_CLIENT_AUTH_SCHEME=sasl \
  -e ZOOKEEPER_JAAS_LOGIN_RENEW=3600000 \
  -e ZOOKEEPER_AUTH_PROVIDER_1=org.apache.zookeeper.server.auth.SASLAuthenticationProvider \
  -e ZOOKEEPER_AUTH_PROVIDER_2=org.apache.zookeeper.server.auth.SASLAuthenticationProvider \
  -e ZOOKEEPER_QUORUM_AUTH_ENABLE_SASL=true \
  -e ZOOKEEPER_QUORUM_AUTH_LEARNER_REQUIRE_SASL=true \
  -e ZOOKEEPER_QUORUM_AUTH_SERVER_REQUIRE_SASL=true \
  -e ZOOKEEPER_QUORUM_AUTH_LEARNER_SASL_LOGIN_CONTEXT=QuorumLearner \
  -e ZOOKEEPER_QUORUM_AUTH_SERVER_SASL_LOGIN_CONTEXT=QuorumServer \
  -e ZOOKEEPER_QUORUM_CNXN_THREADS_SIZE=20 \
  confluentinc/cp-zookeeper:7.7.1
```

### 1.2 Start Zookeeper Node 2
```bash
docker run -d -p 32181:32181 -p 33888:33888 --name=zzk-2 --network mynetwork \
  -v $PWD/secrets/zk-jaas.conf:/etc/kafka/jaas.conf:ro \
  -e KAFKA_OPTS="-Djava.security.auth.login.config=/etc/kafka/jaas.conf" \
  -e ZOOKEEPER_SERVER_ID=2 \
  -e ZOOKEEPER_CLIENT_PORT=32181 \
  -e ZOOKEEPER_TICK_TIME=2000 \
  -e ZOOKEEPER_INIT_LIMIT=5 \
  -e ZOOKEEPER_SYNC_LIMIT=2 \
  -e ZOOKEEPER_SERVERS="zzk-1:22888:23888;zzk-2:32888:33888;zzk-3:42888:43888" \
  -e ZOOKEEPER_AUTH_PROVIDER_SASL=org.apache.zookeeper.server.auth.SASLAuthenticationProvider \
  -e ZOOKEEPER_REQUIRE_CLIENT_AUTH_SCHEME=sasl \
  -e ZOOKEEPER_JAAS_LOGIN_RENEW=3600000 \
  -e ZOOKEEPER_AUTH_PROVIDER_1=org.apache.zookeeper.server.auth.SASLAuthenticationProvider \
  -e ZOOKEEPER_AUTH_PROVIDER_2=org.apache.zookeeper.server.auth.SASLAuthenticationProvider \
  -e ZOOKEEPER_QUORUM_AUTH_ENABLE_SASL=true \
  -e ZOOKEEPER_QUORUM_AUTH_LEARNER_REQUIRE_SASL=true \
  -e ZOOKEEPER_QUORUM_AUTH_SERVER_REQUIRE_SASL=true \
  -e ZOOKEEPER_QUORUM_AUTH_LEARNER_SASL_LOGIN_CONTEXT=QuorumLearner \
  -e ZOOKEEPER_QUORUM_AUTH_SERVER_SASL_LOGIN_CONTEXT=QuorumServer \
  -e ZOOKEEPER_QUORUM_CNXN_THREADS_SIZE=20 \
  confluentinc/cp-zookeeper:7.7.1
```

### 1.3 Start Zookeeper Node 3
```bash
docker run -d -p 42181:42181 -p 43888:43888 --name=zzk-3 --network mynetwork \
  -v $PWD/secrets/zk-jaas.conf:/etc/kafka/jaas.conf:ro \
  -e KAFKA_OPTS="-Djava.security.auth.login.config=/etc/kafka/jaas.conf" \
  -e ZOOKEEPER_SERVER_ID=3 \
  -e ZOOKEEPER_CLIENT_PORT=42181 \
  -e ZOOKEEPER_TICK_TIME=2000 \
  -e ZOOKEEPER_INIT_LIMIT=5 \
  -e ZOOKEEPER_SYNC_LIMIT=2 \
  -e ZOOKEEPER_SERVERS="zzk-1:22888:23888;zzk-2:32888:33888;zzk-3:42888:43888" \
  -e ZOOKEEPER_AUTH_PROVIDER_SASL=org.apache.zookeeper.server.auth.SASLAuthenticationProvider \
  -e ZOOKEEPER_REQUIRE_CLIENT_AUTH_SCHEME=sasl \
  -e ZOOKEEPER_JAAS_LOGIN_RENEW=3600000 \
  -e ZOOKEEPER_AUTH_PROVIDER_1=org.apache.zookeeper.server.auth.SASLAuthenticationProvider \
  -e ZOOKEEPER_AUTH_PROVIDER_2=org.apache.zookeeper.server.auth.SASLAuthenticationProvider \
  -e ZOOKEEPER_QUORUM_AUTH_ENABLE_SASL=true \
  -e ZOOKEEPER_QUORUM_AUTH_LEARNER_REQUIRE_SASL=true \
  -e ZOOKEEPER_QUORUM_AUTH_SERVER_REQUIRE_SASL=true \
  -e ZOOKEEPER_QUORUM_AUTH_LEARNER_SASL_LOGIN_CONTEXT=QuorumLearner \
  -e ZOOKEEPER_QUORUM_AUTH_SERVER_SASL_LOGIN_CONTEXT=QuorumServer \
  -e ZOOKEEPER_QUORUM_CNXN_THREADS_SIZE=20 \
  confluentinc/cp-zookeeper:7.7.1
```

### 1.4 Verify ZooKeeper SASL Configuration
Verify that all ZooKeeper nodes are running with SASL authentication:

```bash
# Check all ZooKeeper nodes are binding to ports
docker logs zzk-1 | grep -i "binding to port"
docker logs zzk-2 | grep -i "binding to port"
docker logs zzk-3 | grep -i "binding to port"
```

**Expected Output:**
```
[timestamp] INFO binding to port 0.0.0.0/0.0.0.0:22181
[timestamp] INFO binding to port 0.0.0.0/0.0.0.0:32181
[timestamp] INFO binding to port 0.0.0.0/0.0.0.0:42181
```

```bash
# Verify SASL authentication is enabled
docker logs zzk-1 | grep -i sasl
```

## Step 2: Create SCRAM Users for Kafka Authentication

**Important:** Before starting Kafka brokers, we must create SCRAM users in ZooKeeper. This is required because our Kafka configuration has ACLs enabled (`KAFKA_ALLOW_EVERYONE_IF_NO_ACL_FOUND=false`), which means all operations require authentication.

### 2.1 Create Broker Users
These users allow brokers to authenticate with each other:

```bash
# Create user for Broker 1
docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 \
  kafka-configs --zookeeper zzk-1:22181 \
  --alter --add-config 'SCRAM-SHA-512=[password=broker-1-secret]' \
  --entity-type users --entity-name kafka-broker-1

# Create user for Broker 2
docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 \
  kafka-configs --zookeeper zzk-1:22181 \
  --alter --add-config 'SCRAM-SHA-512=[password=broker-2-secret]' \
  --entity-type users --entity-name kafka-broker-2

# Create user for Broker 3
docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 \
  kafka-configs --zookeeper zzk-1:22181 \
  --alter --add-config 'SCRAM-SHA-512=[password=broker-3-secret]' \
  --entity-type users --entity-name kafka-broker-3
```

**Expected Output:**
```
Warning: --zookeeper is deprecated and will be removed in a future version of Kafka.
Completed updating config for entity: user-principal 'kafka-broker-X'.
```

## Step 3: Start Kafka Cluster

### 3.1 Start Kafka Broker 1
**Key Configuration Explained:**
- `KAFKA_LISTENERS`: Defines two listeners - INTERNAL for broker communication, CLIENT for external access
- `KAFKA_LISTENER_SECURITY_PROTOCOL_MAP`: Both listeners use SASL_PLAINTEXT (SASL auth over plain connection)
- `KAFKA_INTER_BROKER_LISTENER_NAME=INTERNAL`: Brokers use INTERNAL listener for inter-communication
- `KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL=SCRAM-SHA-512`: Strong authentication between brokers
- `KAFKA_ALLOW_EVERYONE_IF_NO_ACL_FOUND=false`: **ACLs required** - no operations allowed without explicit permissions
```bash
docker run -d --name=kafka-1 -p 29092:29092 --network mynetwork \
  -v $PWD/secrets/kafka-1-jaas.conf:/etc/kafka/jaas.conf:ro \
  -e KAFKA_OPTS="-Djava.security.auth.login.config=/etc/kafka/jaas.conf" \
  -e KAFKA_ZOOKEEPER_CONNECT=zzk-1:22181,zzk-2:32181,zzk-3:42181 \
  -e KAFKA_LISTENERS=INTERNAL://kafka-1:19092,CLIENT://0.0.0.0:29092 \
  -e KAFKA_ADVERTISED_LISTENERS=INTERNAL://kafka-1:19092,CLIENT://localhost:29092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=INTERNAL:SASL_PLAINTEXT,CLIENT:SASL_PLAINTEXT \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=INTERNAL \
  -e KAFKA_SASL_ENABLED_MECHANISMS=SCRAM-SHA-512 \
  -e KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL=SCRAM-SHA-512 \
  -e KAFKA_ALLOW_EVERYONE_IF_NO_ACL_FOUND=false \
  -e KAFKA_MIN_INSYNC_REPLICAS=2 \
  -e KAFKA_AUTHORIZER_CLASS_NAME=kafka.security.authorizer.AclAuthorizer \
  -e KAFKA_SUPER_USERS="User:kafka-broker-1;User:kafka-broker-2;User:kafka-broker-3" \
  confluentinc/cp-kafka:7.7.1
```

### 3.2 Start Kafka Broker 2
```bash
docker run -d --name=kafka-2 -p 39092:39092 --network mynetwork \
  -v $PWD/secrets/kafka-2-jaas.conf:/etc/kafka/jaas.conf:ro \
  -e KAFKA_OPTS="-Djava.security.auth.login.config=/etc/kafka/jaas.conf" \
  -e KAFKA_ZOOKEEPER_CONNECT=zzk-1:22181,zzk-2:32181,zzk-3:42181 \
  -e KAFKA_LISTENERS=INTERNAL://kafka-2:19092,CLIENT://0.0.0.0:39092 \
  -e KAFKA_ADVERTISED_LISTENERS=INTERNAL://kafka-2:19092,CLIENT://localhost:39092 \
  -e KAFKA_BROKER_ID=2 \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=INTERNAL:SASL_PLAINTEXT,CLIENT:SASL_PLAINTEXT \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=INTERNAL \
  -e KAFKA_SASL_ENABLED_MECHANISMS=SCRAM-SHA-512 \
  -e KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL=SCRAM-SHA-512 \
  -e KAFKA_ALLOW_EVERYONE_IF_NO_ACL_FOUND=false \
  -e KAFKA_MIN_INSYNC_REPLICAS=2 \
  -e KAFKA_AUTHORIZER_CLASS_NAME=kafka.security.authorizer.AclAuthorizer \
  -e KAFKA_SUPER_USERS="User:kafka-broker-1;User:kafka-broker-2;User:kafka-broker-3" \
  confluentinc/cp-kafka:7.7.1
```

### 3.3 Start Kafka Broker 3
```bash
docker run -d --name=kafka-3 -p 49092:49092 --network mynetwork \
  -v $PWD/secrets/kafka-3-jaas.conf:/etc/kafka/jaas.conf:ro \
  -e KAFKA_OPTS="-Djava.security.auth.login.config=/etc/kafka/jaas.conf" \
  -e KAFKA_ZOOKEEPER_CONNECT=zzk-1:22181,zzk-2:32181,zzk-3:42181 \
  -e KAFKA_LISTENERS=INTERNAL://kafka-3:19092,CLIENT://0.0.0.0:49092 \
  -e KAFKA_ADVERTISED_LISTENERS=INTERNAL://kafka-3:19092,CLIENT://localhost:49092 \
  -e KAFKA_BROKER_ID=3 \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=INTERNAL:SASL_PLAINTEXT,CLIENT:SASL_PLAINTEXT \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=INTERNAL \
  -e KAFKA_SASL_ENABLED_MECHANISMS=SCRAM-SHA-512 \
  -e KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL=SCRAM-SHA-512 \
  -e KAFKA_ALLOW_EVERYONE_IF_NO_ACL_FOUND=false \
  -e KAFKA_MIN_INSYNC_REPLICAS=2 \
  -e KAFKA_AUTHORIZER_CLASS_NAME=kafka.security.authorizer.AclAuthorizer \
  -e KAFKA_SUPER_USERS="User:kafka-broker-1;User:kafka-broker-2;User:kafka-broker-3" \
  confluentinc/cp-kafka:7.7.1
```

### 3.4 Verify Cluster Status
Check that all containers are running and healthy:

```bash
# Check all containers status
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

**Expected Output:**
```
NAMES     STATUS        PORTS
kafka-3   Up X minutes  9092/tcp, 0.0.0.0:49092->49092/tcp
kafka-2   Up X minutes  9092/tcp, 0.0.0.0:39092->39092/tcp
kafka-1   Up X minutes  9092/tcp, 0.0.0.0:29092->29092/tcp
zzk-3     Up X minutes  2181/tcp, 2888/tcp, 0.0.0.0:42181->42181/tcp...
zzk-2     Up X minutes  2181/tcp, 2888/tcp, 0.0.0.0:32181->32181/tcp...
zzk-1     Up X minutes  2181/tcp, 2888/tcp, 0.0.0.0:22181->22181/tcp...
```

## Step 4: Verify Kafka Cluster Startup

### 4.1 Verify Kafka Brokers Started Successfully
```bash
docker logs zzk-1 | grep -i "binding to port"
docker logs zzk-2 | grep -i "binding to port"
docker logs zzk-3 | grep -i "binding to port"
```

### 4.2 Check Kafka Startup Success
Verify all Kafka brokers started successfully:

```bash
# Check that all brokers started
docker logs kafka-1 | grep -i "started.*KafkaServer"
docker logs kafka-2 | grep -i "started.*KafkaServer"
docker logs kafka-3 | grep -i "started.*KafkaServer"
```

**Expected Output:**
```
[timestamp] INFO [KafkaServer id=1] started (kafka.server.KafkaServer)
[timestamp] INFO [KafkaServer id=2] started (kafka.server.KafkaServer)
[timestamp] INFO [KafkaServer id=3] started (kafka.server.KafkaServer)
```

### 4.3 Test Basic Cluster Connectivity
Create a basic test topic to verify cluster is operational:

```bash
# Create a test topic using pre-created broker credentials
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-test-client.properties:/etc/kafka/broker-test-client.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-topics --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-test-client.properties \
  --create --topic test-connectivity \
  --partitions 3 --replication-factor 3
```

```bash
# Describe the topic to verify cluster health
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-test-client.properties:/etc/kafka/broker-test-client.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-topics --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-test-client.properties \
  --describe --topic test-connectivity
```

**Expected Output:**
```
Created topic test-connectivity.
Topic: test-connectivity  TopicId: xxx  PartitionCount: 3  ReplicationFactor: 3
  Topic: test-connectivity  Partition: 0  Leader: X  Replicas: X,Y,Z  Isr: X,Y,Z
  Topic: test-connectivity  Partition: 1  Leader: Y  Replicas: Y,Z,X  Isr: Y,Z,X
  Topic: test-connectivity  Partition: 2  Leader: Z  Replicas: Z,X,Y  Isr: Z,X,Y
```

✅ **If you see all partitions with full ISR (In-Sync Replicas), your secure cluster is working correctly!**

---

## Step 5: Security Testing - Demonstrating Authentication and Authorization

This section demonstrates how the security layers protect your Kafka cluster by showing failures when proper authentication is missing.

### 5.1 Test Authentication Failures

#### 5.1.1 Test Connection Without Authentication
Try to connect without any authentication configuration:

```bash
# This WILL FAIL - no authentication provided
docker exec kafka-1 kafka-topics --bootstrap-server localhost:29092 --list
```

**Expected Error:**
```
ERROR org.apache.kafka.common.errors.TimeoutException: Timed out waiting for a node assignment.
Error while executing topic command : Timed out waiting for a node assignment.
```

**Why it fails:** The broker requires SASL authentication, but no credentials were provided.

#### 5.1.2 Test Wrong Credentials
Try to connect with incorrect credentials:

```bash
# This WILL FAIL - wrong credentials
docker run --rm --network mynetwork \
  -v $PWD/secrets/wrong-credentials.properties:/etc/kafka/wrong-credentials.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-topics --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/wrong-credentials.properties --list
```

**Expected Error:**
```
ERROR org.apache.kafka.common.errors.SaslAuthenticationException: Authentication failed: Invalid username or password
```

**Why it fails:** The credentials don't match any SCRAM user created in ZooKeeper.

#### 5.1.3 Test Correct Credentials but No ACL Permissions
The broker credentials work because brokers have implicit permissions, but regular users need explicit ACLs:

```bash
# This WORKS - using broker credentials (brokers have special privileges)
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-test-client.properties:/etc/kafka/broker-test-client.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-topics --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-test-client.properties --list
```

**Expected Success:** Shows list of topics (including test-connectivity)

---

## Step 6: Create Client Users and Configurations

Now we'll create dedicated users for different roles and demonstrate ACL-based authorization.

### 6.1 Create Application Users in ZooKeeper

```bash
# Create Admin User (will have full permissions)
docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 \
  kafka-configs --zookeeper zzk-1:22181 \
  --alter --add-config 'SCRAM-SHA-512=[password=admin-secret]' \
  --entity-type users --entity-name admin
```

```bash
# Create Producer User (will have write permissions)
docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 \
  kafka-configs --zookeeper zzk-1:22181 \
  --alter --add-config 'SCRAM-SHA-512=[password=producer-secret]' \
  --entity-type users --entity-name producer
```

```bash
# Create Consumer User (will have read permissions)
docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 \
  kafka-configs --zookeeper zzk-1:22181 \
  --alter --add-config 'SCRAM-SHA-512=[password=consumer-secret]' \
  --entity-type users --entity-name consumer
```

```bash
# Create Limited User (will have very restricted permissions)
docker run --rm --network mynetwork confluentinc/cp-kafka:7.7.1 \
  kafka-configs --zookeeper zzk-1:22181 \
  --alter --add-config 'SCRAM-SHA-512=[password=limited-secret]' \
  --entity-type users --entity-name limited
```

### 6.2 Verify Client Configuration Files

All client configuration files are pre-created in the `secrets/` directory:

```bash
# List all available client configurations
ls -la secrets/*client*.properties secrets/wrong-credentials.properties
```

**Available configurations:**
- `admin-client.properties` - Full administrative access
- `producer-client.properties` - Producer with serializers configured
- `consumer-client.properties` - Consumer with deserializers and group configured
- `limited-client.properties` - Limited user with basic authentication only
- `broker-test-client.properties` - Broker credentials for testing
- `wrong-credentials.properties` - Invalid credentials for testing failures

### 6.3 Test Authentication with New Users (Before ACLs)

```bash
# Test admin user 
docker run --rm --network mynetwork \
  -v $PWD/secrets/admin-client.properties:/etc/kafka/admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-topics --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/admin.properties --list
```

```bash
# Test producer user 
docker run --rm --network mynetwork \
  -v $PWD/secrets/producer-client.properties:/etc/kafka/producer.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-topics --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/producer.properties --list
```

**Expected output for both:**
```

```

**Why it fails:** With `KAFKA_ALLOW_EVERYONE_IF_NO_ACL_FOUND=false`, all operations require explicit ACL permissions.

---

## Step 7: Configure ACL Permissions

Now we'll grant specific permissions to demonstrate fine-grained access control.

### 7.1 Grant Admin Permissions

```bash
# Grant cluster admin permissions to admin user
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-admin-client.properties:/etc/kafka/broker-admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-acls --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-admin.properties \
  --add --allow-principal User:admin \
  --operation All --cluster
```

```bash
# Grant topic administration permissions
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-admin-client.properties:/etc/kafka/broker-admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-acls --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-admin.properties \
  --add --allow-principal User:admin \
  --operation All --topic '*'
```

```bash
# Grant consumer group permissions
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-admin-client.properties:/etc/kafka/broker-admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-acls --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-admin.properties \
  --add --allow-principal User:admin \
  --operation All --group '*'
```

### 7.2 Create a Demo Topic with Admin User

```bash
# Now admin can create topics
docker run --rm --network mynetwork \
  -v $PWD/secrets/admin-client.properties:/etc/kafka/admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-topics --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/admin.properties \
  --create --topic demo-topic \
  --partitions 3 --replication-factor 3
```

```bash
# Admin can list topics
docker run --rm --network mynetwork \
  -v $PWD/secrets/admin-client.properties:/etc/kafka/admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-topics --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/admin.properties --list
```

**Expected Success:** Topic created and listed successfully.

### 7.3 Grant Producer Permissions

```bash
# Grant write permission to producer user for demo-topic only
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-admin-client.properties:/etc/kafka/broker-admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-acls --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-admin.properties \
  --add --allow-principal User:producer \
  --operation Write --topic demo-topic
```

```bash
# Grant describe permission (needed for metadata)
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-admin-client.properties:/etc/kafka/broker-admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-acls --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-admin.properties \
  --add --allow-principal User:producer \
  --operation Describe --topic demo-topic
```

### 7.4 Grant Consumer Permissions

```bash
# Grant read permission to consumer user for demo-topic
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-admin-client.properties:/etc/kafka/broker-admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-acls --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-admin.properties \
  --add --allow-principal User:consumer \
  --operation Read --topic demo-topic
```

```bash
# Grant describe permission for metadata
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-admin-client.properties:/etc/kafka/broker-admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-acls --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-admin.properties \
  --add --allow-principal User:consumer \
  --operation Describe --topic demo-topic
```

```bash
# Grant consumer group permission
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-admin-client.properties:/etc/kafka/broker-admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-acls --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-admin.properties \
  --add --allow-principal User:consumer \
  --operation Read --group demo-consumer-group
```

---

## Step 8: Demonstrate ACL Security in Action

### 8.1 Test Producer Operations

```bash
# Producer can write to demo-topic (SUCCESS)
  docker run --rm -i --network mynetwork \
  -v $PWD/secrets/producer-client.properties:/etc/kafka/producer.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-console-producer --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --topic demo-topic --producer.config /etc/kafka/producer.properties
```

```bash
# Producer CANNOT list topics (FAILS - no cluster permissions)
  docker run --rm --network mynetwork \
  -v $PWD/secrets/producer-client.properties:/etc/kafka/producer.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-topics --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/producer.properties --list
```

**Expected Results:**
- Write operation: SUCCESS
- List topics: FAILS with `ClusterAuthorizationException`

### 8.2 Test Consumer Operations

```bash
# Consumer can read from demo-topic (SUCCESS)
timeout 10s docker run --rm --network mynetwork \
  -v $PWD/secrets/consumer-client.properties:/etc/kafka/consumer.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-console-consumer --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --topic demo-topic --consumer.config /etc/kafka/consumer.properties \
  --from-beginning || echo "Consumer test completed"
```

```bash
# Consumer CANNOT write to topics (will fail if attempted)
  docker run --rm -i --network mynetwork \
  -v $PWD/secrets/consumer-client.properties:/etc/kafka/consumer.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-console-producer --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --topic demo-topic --producer.config /etc/kafka/consumer.properties
```

**Expected Results:**
- Read operation: SUCCESS (shows the message "Hello Secure Kafka!")
- Write operation: FAILS with authorization exception

### 8.3 Test Limited User (No Permissions)

```bash
# Limited user CANNOT do anything
docker run --rm --network mynetwork \
  -v $PWD/secrets/limited-client.properties:/etc/kafka/limited.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-topics --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/limited.properties --list
```

**Expected Result:** FAILS with `ClusterAuthorizationException`

### 8.4 View All ACLs

```bash
# List all configured ACLs
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-admin-client.properties:/etc/kafka/broker-admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-acls --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-admin.properties --list
```

**Expected Output:** Shows all the ACL rules we created for admin, producer, and consumer users.

---

## Step 9: Advanced ACL Scenarios

### 9.1 Topic-Specific Permissions

```bash
# Create another topic for advanced testing
docker run --rm --network mynetwork \
  -v $PWD/secrets/admin-client.properties:/etc/kafka/admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-topics --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/admin.properties \
  --create --topic private-topic \
  --partitions 1 --replication-factor 3
```

```bash
# Give limited user access ONLY to private-topic, not demo-topic
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-admin-client.properties:/etc/kafka/broker-admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-acls --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-admin.properties \
  --add --allow-principal User:limited \
  --operation Write --topic private-topic
```

```bash
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-admin-client.properties:/etc/kafka/broker-admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-acls --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-admin.properties \
  --add --allow-principal User:limited \
  --operation Describe --topic private-topic
```

### 9.2 Test Topic Isolation

```bash
# Limited user can write to private-topic (SUCCESS)
  docker run --rm -i --network mynetwork \
  -v $PWD/secrets/limited-client.properties:/etc/kafka/limited.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-console-producer --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --topic private-topic --producer.config /etc/kafka/limited.properties
```

```bash
# Limited user CANNOT write to demo-topic (FAILS)
  docker run --rm -i --network mynetwork \
  -v $PWD/secrets/limited-client.properties:/etc/kafka/limited.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-console-producer --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --topic demo-topic --producer.config /etc/kafka/limited.properties
```

**Expected Results:**
- Write to private-topic: SUCCESS
- Write to demo-topic: FAILS with `TopicAuthorizationException`

## Important Security Considerations

- **SASL Authentication**: All communication requires authentication
- **Secure Interbroker Communication**: Brokers authenticate to each other
- **ZooKeeper Security**: Full SASL authentication for client and quorum communication
- **Network Isolation**: Docker network provides container isolation
- **Secret Management**: Passwords should be managed securely in production

---

## Summary of Security Demonstration

### ✅ What We've Proven:

1. **Authentication Works**: 
   - No authentication = Connection timeouts
   - Wrong credentials = SASL authentication failures
   - Correct credentials = Successful connections

2. **Authorization Required**: 
   - Even with valid authentication, operations require ACL permissions
   - `KAFKA_ALLOW_EVERYONE_IF_NO_ACL_FOUND=false` enforces strict authorization

3. **Broker Privileges**: 
   - Kafka brokers have implicit permissions for cluster operations
   - Regular users need explicit ACL grants

### 🎯 Next Steps for Full ACL Demo:

The cluster is now ready for ACL configuration. To complete the ACL demonstration:

1. **Enable ACL Management**: Create admin ACLs using broker credentials
2. **Grant Role-based Permissions**: Assign specific permissions to producer/consumer users  
3. **Test Permission Boundaries**: Verify users can only perform authorized operations
4. **Demonstrate Topic Isolation**: Show how ACLs prevent cross-topic access

### 🔧 ACL Management Commands:

**Note**: ACL management requires either:
- Broker credentials (kafka-broker-1, kafka-broker-2, kafka-broker-3)
- Admin user with existing ACL permissions
- Super user configuration (if enabled)

```bash
# Example: Grant admin permissions using broker credentials
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-admin-client.properties:/etc/kafka/broker-admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-acls --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-admin.properties \
  --add --allow-principal User:admin \
  --operation All --cluster
```

```bash
# Example: Grant topic permissions  
docker run --rm --network mynetwork \
  -v $PWD/secrets/broker-admin-client.properties:/etc/kafka/broker-admin.properties \
  confluentinc/cp-kafka:7.7.1 \
  kafka-acls --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --command-config /etc/kafka/broker-admin.properties \
  --add --allow-principal User:producer \
  --operation Write --topic demo-topic
```

## JAAS Configuration Files Reference

The setup requires the following JAAS configuration files in the `secrets/` directory:

### `zk-jaas.conf`
```
Server {
  org.apache.zookeeper.server.auth.DigestLoginModule required
  user_zookeeper="zk-server-secret"
  user_kafka="zk-client-secret";
};

QuorumServer {
  org.apache.zookeeper.server.auth.DigestLoginModule required
  username="zookeeper"
  password="zk-server-secret";
};

QuorumLearner {
  org.apache.zookeeper.server.auth.DigestLoginModule required
  username="zookeeper"
  password="zk-server-secret";
};
```

### `kafka-X-jaas.conf` (for each broker)
```
KafkaServer {
  org.apache.kafka.common.security.scram.ScramLoginModule required
  username="kafka-broker-X"
  password="broker-X-secret";
};

Client {
  org.apache.zookeeper.server.auth.DigestLoginModule required
  username="kafka"
  password="zk-client-secret";
};
```

