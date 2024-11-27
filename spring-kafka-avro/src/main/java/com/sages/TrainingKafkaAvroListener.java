package com.sages;

import com.sages.avro.Transaction;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TrainingKafkaAvroListener {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaAvroListener.class);

    @KafkaListener(
            topics = "spring-avro-topic",
            groupId = "my-group-avro",
            containerFactory = "avroKafkaListenerContainerFactory"
    )
    public void listen(Transaction transaction) {
        log.info("Received specific record {} from avro topic", transaction.toString());
    }

    @KafkaListener(
            topics = "spring-avro-topic",
            groupId = "my-group-avro-generic",
            containerFactory = "genericAvroKafkaListenerContainerFactory"
    )
    public void listenGeneric(GenericRecord genericRecord) {
        log.info("Received generic record with id = {}, source = {} from avro topic", genericRecord.get("id"), genericRecord.get("source"));
    }
}
