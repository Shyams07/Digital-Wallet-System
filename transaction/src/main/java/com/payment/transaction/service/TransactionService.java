package com.payment.transaction.service;


import com.payment.transaction.entity.Transaction;

import java.util.List;

public interface TransactionService {

    Transaction createTransaction(Transaction transaction);
    public Transaction getTransactionById(Long id);

    public List<Transaction> getTransactionsByUser(Long userId);
}