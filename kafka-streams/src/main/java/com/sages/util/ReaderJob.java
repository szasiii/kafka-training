
package com.sages.util;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ReaderJob {

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    public ReaderJob(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

//    @Scheduled(fixedRate = 5000, initialDelay = 5000)
//    public void queryStore() {
//        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
//        if (kafkaStreams == null) return;
//
//        ReadOnlyKeyValueStore<String, Double> store =
//                kafkaStreams.store(
//                        StoreQueryParameters.fromNameAndType("aggregated-store", QueryableStoreTypes.keyValueStore())
//                );
//
//        System.out.println("### INTERACTIVE QUERY ###");
//        KeyValueIterator<String, Double> iterator = store.all();
//        while (iterator.hasNext()) {
//            KeyValue<String, Double> entry = iterator.next();
//            System.out.println("Instrument: " + entry.key + " => Sum: " + entry.value);
//        }
//        iterator.close();
//    }

}
