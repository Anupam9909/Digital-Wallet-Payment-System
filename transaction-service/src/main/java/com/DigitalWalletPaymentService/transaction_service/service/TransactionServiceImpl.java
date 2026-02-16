package com.DigitalWalletPaymentService.transaction_service.service;

import com.DigitalWalletPaymentService.transaction_service.entity.Transaction;
import com.DigitalWalletPaymentService.transaction_service.kafka.KafkaEventProducer;
import com.DigitalWalletPaymentService.transaction_service.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService{

    private final KafkaEventProducer kafkaEventProducer;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    public TransactionServiceImpl(KafkaEventProducer kafkaEventProducer, TransactionRepository transactionRepository, ObjectMapper objectMapper) {
        this.kafkaEventProducer = kafkaEventProducer;
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Transaction createTransaction(Transaction request) {
        System.out.println("🚀 Entered CreateTransaction()");
        Long senderId = request.getSenderId();
        Long receiverId = request.getReceiverId();
        Double amount = request.getAmount();
        String senderName = request.getSenderName();
        String reciverName = request.getReceiverName();

        Transaction transaction = new Transaction();
        transaction.setSenderId(senderId);
        transaction.setReceiverId(receiverId);
        transaction.setSenderName(senderName);
        transaction.setReceiverName(reciverName);
        transaction.setAmount(amount);
        transaction.setTimeStamp(LocalDateTime.now());
        transaction.setStatus("Success");
        System.out.println("📤 Incoming Transaction Object: "+transaction);
        Transaction saved = transactionRepository.save(transaction);
        System.out.println("💾 saved transaction from DB :" +saved);

        try{
           // String eventPayLoad = objectMapper.writeValueAsString(saved);
            String key = String.valueOf(saved.getId());
            kafkaEventProducer.sendTransactionEvent(key, saved);
        } catch (Exception e){
            System.err.println("❌ Failed to send kafka event: " + e.getMessage());
            e.printStackTrace();
        }

        return saved;
    }

    @Override
    public List<Transaction> getAllTransaction() {
        return transactionRepository.findAll();
    }
}
