//package com.sages;
//
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.stereotype.Component;
//
//@Component
//public class TrainingKafkaListenerSimple {
//
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaListenerSimple.class);
//
//
//    @KafkaListener(topics = "spring-kafka-producer-topic", groupId = "simple-listener")
//    public void listen(Transaction transaction, Acknowledgment acknowledgment) {
//        log.info("Received {} transaction with thread {}", transaction.toString(), Thread.currentThread().getId());
//        acknowledgment.acknowledge();
//    }
//}