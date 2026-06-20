package com.payment.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.transaction.entity.Transaction;
import com.payment.transaction.kafka.KafkaEventProducer;
import com.payment.transaction.repository.TransactionRepository;
import org.hibernate.internal.TransactionManagement;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaEventProducer kafkaEventProducer;

    public TransactionServiceImpl(TransactionRepository repository,
                                  ObjectMapper objectMapper, KafkaEventProducer kafkaEventProducer) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.kafkaEventProducer= kafkaEventProducer;
    }

    @Override
    public Transaction createTransaction(Transaction request) {
        System.out.println("🚀 Entered createTransaction()");

        Long senderId = request.getSenderId();
        Long receiverId = request.getReceiverId();
        Double amount = request.getAmount();

        Transaction transaction = new Transaction();
        transaction.setSenderId(senderId);
        transaction.setReceiverId(receiverId);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("SUCCESS");

        Transaction saved = repository.save(transaction);

        try{
            String eventpayload= objectMapper.writeValueAsString(saved);
            String key= String.valueOf(saved.getId());
            kafkaEventProducer.sendTransactionEvent(key, eventpayload);
            System.out.println("kafka message sent");
        }catch (Exception e){
            System.out.println("failed to send "+ e.getMessage());
            e.printStackTrace();
        }
        return saved;
    }

    @Override
    public Transaction getTransactionById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Transaction> getTransactionsByUser(Long userId) {
        return repository.findBySenderIdOrReceiverId(userId, userId);
    }


}
