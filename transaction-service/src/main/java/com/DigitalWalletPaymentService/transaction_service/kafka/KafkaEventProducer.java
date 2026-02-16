package com.DigitalWalletPaymentService.transaction_service.kafka;

import com.DigitalWalletPaymentService.transaction_service.entity.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.kafka.support.SendResult;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaEventProducer {

    private static final String TOPIC = "txn-initiated";
    private final KafkaTemplate<String,Transaction> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaEventProducer(KafkaTemplate<String, Transaction> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.setDateFormat(new java.text.SimpleDateFormat("dd MMMM yyyy, hh:mm a"));
    }

    // send transaction object seralize as JSON Data
    public void sendTransactionEvent(String key, Transaction transaction){
        System.out.println("📤Sending Kafka --> Topic:" + TOPIC + ", key: " + key + ", Message: "+transaction);
        CompletableFuture<SendResult<String, Transaction>> future = kafkaTemplate.send(TOPIC, key,transaction);
        future.thenAccept(result ->{
            RecordMetadata metadata = result.getRecordMetadata();
            System.out.println("✅ Kafka Message Sent Successfully! Topic:" +metadata.topic()+
                    ", Partition: "+metadata.partition());
        }).exceptionally(ex ->{
            System.err.println("❌ Failed to send Kafka message :" +ex.getMessage());
            return null;
        });
    }

}
