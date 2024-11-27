package com.sages;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.stereotype.Service;

@Service
public class TrainingKafkaSelectiveListener {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaSelectiveListener.class);

    @KafkaListener(
            groupId = "my-group-selective",
            topicPartitions = {
                    @TopicPartition(topic = "spring-kafka-producer-topic", partitions = {"0", "1"})
            }
    )
    public void listen(Transaction transaction, ConsumerRecordMetadata metadata) {
        log.info("Received {} transaction from partition {}", transaction.toString(), metadata.partition());
    }
}
