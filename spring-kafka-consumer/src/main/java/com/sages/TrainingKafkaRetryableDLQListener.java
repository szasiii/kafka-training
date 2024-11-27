//package com.sages;
//
//import org.springframework.kafka.annotation.DltHandler;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.annotation.RetryableTopic;
//import org.springframework.retry.annotation.Backoff;
//import org.springframework.stereotype.Component;
//
//@Component
//public class TrainingKafkaRetryableDLQListener {
//
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainingKafkaRetryableDLQListener.class);
//
//    @RetryableTopic(
//            attempts = "5",
//            backoff = @Backoff(delay = 2000, multiplier = 2.0),
//            include = RuntimeException.class
//    )
//    @KafkaListener(topics = "spring-kafka-producer-topic", groupId = "retryable-group-with-dlq")
//    public void listen(Transaction transaction) {
//        log.info("Received {} transaction with thread {}", transaction.toString(), Thread.currentThread().getId());
//        throw new RuntimeException("Simulated exception");
//    }
//
//    @DltHandler
//    public void handleDeadLetterQueue(Transaction record) {
//        log.error("Message sent to DLQ for manual inspection: {}", record);
//    }
//}
