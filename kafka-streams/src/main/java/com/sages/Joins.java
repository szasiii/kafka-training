//package com.sages;
//
//import com.sages.model.PriceTick;
//import com.sages.model.Trade;
//import com.sages.model.Transaction;
//import org.apache.kafka.common.serialization.Serdes;
//import org.apache.kafka.streams.StreamsBuilder;
//import org.apache.kafka.streams.kstream.Consumed;
//import org.apache.kafka.streams.kstream.JoinWindows;
//import org.apache.kafka.streams.kstream.KStream;
//import org.apache.kafka.streams.kstream.KTable;
//import org.apache.kafka.streams.kstream.Printed;
//import org.apache.kafka.streams.kstream.StreamJoined;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.support.serializer.JsonSerde;
//
//import java.time.Duration;
//
//@Configuration
//public class Joins
//{
//    @Bean
//    public KStream<String, Transaction> cStream(StreamsBuilder builder) {
//    }
//}
