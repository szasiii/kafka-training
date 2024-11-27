//package com.sages;
//
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class TrainingKafkaBatchListener {
//
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaBatchListener.class);
//
//    @KafkaListener(
//            topics = "spring-kafka-producer-topic",
//            groupId = "my-group-batch",
//            containerFactory = "batchKafkaListenerContainerFactory"
//    )
//    public void listenBatch(List<Transaction> transactions) {
//        log.info("Received batch of {} transactions", transactions.size());
//    }
//}
