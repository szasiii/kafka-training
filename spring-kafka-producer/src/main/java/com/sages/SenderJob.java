package com.sages;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class SenderJob {

    private final static String TOPIC = "spring-kafka-producer-topic";

    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    @Autowired
    public SenderJob(KafkaTemplate<String, Transaction> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 5000)
    public void schedule() {
        Transaction crypto = new Transaction(UUID.randomUUID(), "CRYPTO", BigDecimal.valueOf(Math.random()));
        kafkaTemplate.send(TOPIC, UUID.randomUUID().toString(), crypto);
        Transaction stock = new Transaction(UUID.randomUUID(), "STOCK", BigDecimal.valueOf(Math.random()));
        kafkaTemplate.send(TOPIC, UUID.randomUUID().toString(), stock);
    }
}
