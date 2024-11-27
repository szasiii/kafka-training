package com.sages;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception{
        KafkaProducer<String, String> producer = createProducer();
        ProducerRecord<String, String> record = new ProducerRecord<>("kafka-client-exercises", "key-" + Math.random(), "value-" + Math.random());
        producer.send(record).get();
    }

    public static KafkaProducer<String, String> createProducer() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:29092,kafka-2:39092,kafka-3:49092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, "1000");
        return new KafkaProducer<>(properties);
    }

}