package com.digitalwallet.notification_service.kafka;

import com.digitalwallet.notification_service.entity.Notification;
import com.digitalwallet.notification_service.entity.Transaction;
import com.digitalwallet.notification_service.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class NotificationConsumer {
    private final NotificationRepository notificationRepository;
    private final ObjectMapper mapper;

    public NotificationConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new java.text.SimpleDateFormat("dd MMMM yyyy, hh:mm a"));
    }

    @KafkaListener(topics = "txn-initiated", groupId = "notification-group")
    public void consumeTransactionEvent(Transaction txn) {

        log.info("✅ Received Transaction from Kafka: {}", txn);

        Notification notification = new Notification();
        Long receiverUserId = txn.getReceiverId();
        Long senderUserId = txn.getSenderId();

        String notify = "Dear " + receiverUserId + ",\n" +
                "Amount: $ " + txn.getAmount() + " received from " + senderUserId;

        notification.setMessage(notify);
        notification.setSentAt(LocalDateTime.now());

        log.info("📝 Creating Notification for receiverId={} senderId={} amount={}",
                receiverUserId, senderUserId, txn.getAmount());

        notificationRepository.save(notification);
        log.info("💾 Notification saved successfully for receiverId={}", receiverUserId);
    }
}
