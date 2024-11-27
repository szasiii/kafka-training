package com.sages;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class Main {


    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {

    }

    public static KafkaConsumer<String, String> createConsumer() {

    }
}