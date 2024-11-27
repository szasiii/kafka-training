//package com.sages;
//
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Service
//public class TrainingKafkaFilteringListener {
//
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaFilteringListener.class);
//
//    @KafkaListener(
//            topics = "spring-kafka-producer-topic",
//            groupId = "my-group-filtering",
//            containerFactory = "filterKafkaListenerContainerFactory"
//    )
//    public void listenBatch(Transaction transaction) {
//        log.info("Received {} transaction with thread {}", transaction.toString(), Thread.currentThread().getId());
//    }
//}
