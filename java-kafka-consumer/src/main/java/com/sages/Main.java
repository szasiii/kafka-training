package com.sages;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class Main {

    private final static String BOOTSTRAP_SERVERS = "kafka-1:29092,kafka-2:39092,kafka-3:49092";
    private final static String STRING_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        KafkaConsumer<String, String> consumer = createConsumer();
        consumer.subscribe(List.of("kafka-client-exercises"));
        while (true) {
            consumer.poll(Duration.ofMillis(100)).forEach(record ->
                    logger.info("Key: {}, Value: {}, Partition: {}, Offset: {}", record.key(), record.value(), record.partition(), record.offset()));
            consumer.commitSync();
        }

    }

    public static KafkaConsumer<String, String> createConsumer() {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "java-kafka-clients-consumer");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return new KafkaConsumer<>(properties);
    }
}