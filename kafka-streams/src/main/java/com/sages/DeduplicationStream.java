//package com.sages;
//
//import com.sages.model.Transaction;
//import org.apache.kafka.common.serialization.Serdes;
//import org.apache.kafka.streams.KeyValue;
//import org.apache.kafka.streams.StreamsBuilder;
//import org.apache.kafka.streams.kstream.Consumed;
//import org.apache.kafka.streams.kstream.KStream;
//import org.apache.kafka.streams.kstream.Printed;
//import org.apache.kafka.streams.kstream.Transformer;
//import org.apache.kafka.streams.processor.ProcessorContext;
//import org.apache.kafka.streams.state.KeyValueStore;
//import org.apache.kafka.streams.state.Stores;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.support.serializer.JsonSerde;
//
//import java.time.Duration;
//import java.time.Instant;
//
//import java.util.UUID;
//
//@Configuration
//public class DeduplicationStream {
//
//    @Bean
//    public KStream<String, Transaction> aStream(StreamsBuilder builder) {  }
//
//    private static class DeduplicationTransformer {}
//}
