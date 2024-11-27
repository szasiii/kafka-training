//package com.sages;
//
//import com.sages.model.Transaction;
//import org.apache.kafka.common.serialization.Serde;
//import org.apache.kafka.common.serialization.Serdes;
//import org.apache.kafka.streams.StreamsBuilder;
//import org.apache.kafka.streams.kstream.Consumed;
//import org.apache.kafka.streams.kstream.KStream;
//import org.apache.kafka.streams.kstream.Materialized;
//import org.apache.kafka.streams.kstream.Printed;
//import org.apache.kafka.streams.kstream.Produced;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.support.serializer.JsonSerde;
//
//
//@Configuration
//public class GroupingStream
//{
//    @Bean
//    public KStream<String, Transaction> cStream(StreamsBuilder builder) {
//
//        JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
//        final Serde<Double> doubleSerde = Serdes.Double();
//
//        KStream<String, Transaction> txnStream = builder.stream("streaming-transactions", Consumed.with(Serdes.String(), transactionSerde));
//
//        KStream<String, Double> aggregated = txnStream
//                .selectKey((k, v) -> v.getInstrument())
//                .mapValues((k, v) -> v.getAmount().doubleValue())
//                .groupByKey()
//                .aggregate(
//                        () -> 0.0, (aggKey, newValue, aggValue) -> aggValue + newValue,
//                        Materialized.with(Serdes.String(), doubleSerde))
//                .toStream();
//
//        aggregated.to("aggregates", Produced.with(Serdes.String(), doubleSerde));
//
//        txnStream.print(Printed.<String, Transaction>toSysOut().withLabel("source stream"));
//        aggregated.print(Printed.<String, Double>toSysOut().withLabel("sink stream"));
//
//        return txnStream;
//    }
//}
