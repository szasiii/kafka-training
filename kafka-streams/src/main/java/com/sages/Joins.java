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
//
//        JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
//        JsonSerde<PriceTick> priceTickSerde = new JsonSerde<>(PriceTick.class);
//
//        KStream<String, Transaction> txnStream = builder.stream(
//                "streaming-transactions", Consumed.with(Serdes.String(), transactionSerde)
//        );
//        KStream<String, PriceTick> pricesStream = builder.stream(
//                "streaming-prices", Consumed.with(Serdes.String(), priceTickSerde)
//        );
//
//        txnStream.selectKey((k, v) -> v.getInstrument())
//                .join(
//                        pricesStream.selectKey((k, v) -> v.getInstrument()),
//                        (txn, price) -> new Trade(
//                                txn.getInstrument(),
//                                txn.getAmount().doubleValue(),
//                                price.getPrice().doubleValue()
//                        ),
//                        JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMillis(10)),
//                        StreamJoined.with(Serdes.String(), transactionSerde, priceTickSerde)
//                )
//                .print(Printed.<String, Trade>toSysOut().withLabel("stream-join-trade"));
//
//        KTable<String, Double> priceTable = pricesStream.selectKey((k, v) -> v.getInstrument()).mapValues(v -> v.getPrice().doubleValue()).toTable();
//        txnStream.selectKey((k, v) -> v.getInstrument())
//                .leftJoin(priceTable, (txn, price) -> new Trade(
//                        txn.getInstrument(),
//                        txn.getAmount().doubleValue(),
//                        price == null ? 0.0 : price
//                )).print(Printed.<String, Trade>toSysOut().withLabel("table-join-trade"));
//
//        return txnStream;
//    }
//}
