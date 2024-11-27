package com.sages.util;


import com.sages.model.PriceTick;
import com.sages.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SenderJob {

    private final static String TOPIC = "streaming-transactions";
    private final static String PRICE_TOPIC = "streaming-prices";

    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    private final KafkaTemplate<String, PriceTick> kafkaTemplatePrice;

    @Autowired
    public SenderJob(KafkaTemplate<String, Transaction> kafkaTemplate, KafkaTemplate<String, PriceTick> kafkaTemplatePrice) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTemplatePrice = kafkaTemplatePrice;
    }

    @Scheduled(fixedRate = 300)
    public void schedule() {
            Transaction crypto = new Transaction(UUID.randomUUID(), "GOLD", BigDecimal.valueOf(Math.random()), LocalDateTime.now());
            kafkaTemplate.send(TOPIC, UUID.randomUUID().toString(), crypto);
            Transaction stock = new Transaction(UUID.randomUUID(), "BTC", BigDecimal.valueOf(Math.random()), LocalDateTime.now());
            kafkaTemplate.send(TOPIC, UUID.randomUUID().toString(), stock);
    }

//    @Scheduled(fixedRate = 5)
//    public void schedulePrices() {
//        PriceTick price = new PriceTick(UUID.randomUUID(), "GOLD", BigDecimal.valueOf(Math.random()));
//        kafkaTemplatePrice.send(PRICE_TOPIC, UUID.randomUUID().toString(), price);
//        PriceTick priceCrypto = new PriceTick(UUID.randomUUID(), "BTC", BigDecimal.valueOf(Math.random()));
//        kafkaTemplatePrice.send(PRICE_TOPIC, UUID.randomUUID().toString(), priceCrypto);
//    }
}
