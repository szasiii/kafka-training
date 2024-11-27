//package com.sages;
//
//import com.sages.model.Transaction;
//import org.apache.kafka.common.serialization.Serdes;
//import org.apache.kafka.streams.StreamsBuilder;
//import org.apache.kafka.streams.kstream.Consumed;
//import org.apache.kafka.streams.kstream.KStream;
//import org.apache.kafka.streams.kstream.Printed;
//import org.apache.kafka.streams.kstream.Produced;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.support.serializer.JsonSerde;
//
//@Configuration
//public class BasicStream {
//
//    @Bean
//    public KStream<String, String> aStream(StreamsBuilder builder) {
//
//        JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
//
//        KStream<String, Transaction> transactions = builder.stream("streaming-transactions",
//                Consumed.with(Serdes.String(), transactionSerde));
//
//        final KStream<String, Transaction> filteredTransactions = transactions.filter((k, v) ->
//                v.getInstrument().equalsIgnoreCase("GOLD"));
//
//        KStream<String, String> outputStream = filteredTransactions.mapValues(s -> s.getId().toString());
//
//        outputStream.to("transaction_ids", Produced.with(Serdes.String(), Serdes.String()));
//
//        transactions.print(Printed.<String, Transaction>toSysOut().withLabel("source stream"));
//        outputStream.print(Printed.<String, String>toSysOut().withLabel("sink stream"));
//
//        return outputStream;
//    }
//
//}