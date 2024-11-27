//package com.sages;
//
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Service
//public class TrainingKafkaCustomErrorHandlingListener {
//
//
//    @KafkaListener(
//            topics = "spring-kafka-producer-topic",
//            groupId = "my-group-custom-error-handling",
//            containerFactory = "kafkaListenerContainerFactoryWithErrorHandler"
//    )
//    public void listenBatch(Transaction transaction) {
//        throw new RuntimeException("Custom error handling exception");
//    }
//}
