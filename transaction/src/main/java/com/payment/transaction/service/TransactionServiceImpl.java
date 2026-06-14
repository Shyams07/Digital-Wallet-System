package com.payment.transaction.service;

import com.payment.transaction.entity.Transaction;
import com.payment.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final ObjectMapper objectMapper;

    public TransactionServiceImpl(TransactionRepository repository,
                                  ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("SUCCESS");

        return repository.save(transaction);
    }

    @Override
    public Transaction getTransactionById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Transaction> getTransactionsByUser(Long userId) {
        return repository.findBySenderIdOrReceiverId(userId, userId);
    }


}
