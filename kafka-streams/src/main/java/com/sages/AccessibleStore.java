//package com.sages;
//
//import com.sages.model.Transaction;
//import org.apache.kafka.common.serialization.Serdes;
//import org.apache.kafka.common.utils.Bytes;
//import org.apache.kafka.streams.StreamsBuilder;
//import org.apache.kafka.streams.kstream.Aggregator;
//import org.apache.kafka.streams.kstream.Branched;
//import org.apache.kafka.streams.kstream.Consumed;
//import org.apache.kafka.streams.kstream.KStream;
//import org.apache.kafka.streams.kstream.KTable;
//import org.apache.kafka.streams.kstream.Materialized;
//import org.apache.kafka.streams.kstream.Named;
//import org.apache.kafka.streams.kstream.Printed;
//import org.apache.kafka.streams.state.KeyValueStore;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.support.serializer.JsonSerde;
//
//import java.util.Map;
//
//@Configuration
//public class AccessibleStore {
//
//    @Bean
//    public KStream<String, String> aStream(StreamsBuilder builder) { }
//}
