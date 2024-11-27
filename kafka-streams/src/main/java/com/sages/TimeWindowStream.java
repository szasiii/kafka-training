//package com.sages;
//
//import com.sages.model.Transaction;
//import org.apache.kafka.common.serialization.Serdes;
//import org.apache.kafka.streams.StreamsBuilder;
//import org.apache.kafka.streams.kstream.Consumed;
//import org.apache.kafka.streams.kstream.KStream;
//import org.apache.kafka.streams.kstream.Materialized;
//import org.apache.kafka.streams.kstream.Printed;
//import org.apache.kafka.streams.kstream.TimeWindows;
//import org.apache.kafka.streams.kstream.Windowed;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.support.serializer.JsonSerde;
//
//import java.time.Duration;
//
//@Configuration
//public class TimeWindowStream
//{
//    @Bean
//    public KStream<String, Transaction> cStream(StreamsBuilder builder) {
//
//        JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
//
//        KStream<String, Transaction> txnStream = builder.stream("streaming-transactions", Consumed.with(Serdes.String(), transactionSerde));
//
//        KStream<Windowed<String>, Long> countInWindow = txnStream
//                .selectKey((k, v) -> v.getInstrument())
//                .groupByKey()
//                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(3)))
//                .count(Materialized.with(Serdes.String(), Serdes.Long()))
//                .toStream();
//
//        txnStream.print(Printed.<String, Transaction>toSysOut().withLabel("source stream"));
//        countInWindow.print(Printed.<Windowed<String>, Long>toSysOut().withLabel("sink stream"));
//
//        return txnStream;
//    }
//}
