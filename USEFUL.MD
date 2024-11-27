### Delete all containers

```bash

docker rm -v -f $(docker ps -qa) 

```
### Delete topic

```bash 

docker run \
--rm \
--network mynetwork \
confluentinc/cp-kafka:7.7.1 \
kafka-topics --delete --topic <TOPIC> --bootstrap-server kafka-1:29092,kafka-2:39092,kafka-3:49092
```

