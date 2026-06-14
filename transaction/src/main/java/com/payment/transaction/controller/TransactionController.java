package com.payment.transaction.controller;

import com.payment.transaction.entity.Transaction;
import com.payment.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions/")
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Transaction transaction){
        Transaction created= service.createTransaction(transaction);
        return ResponseEntity.ok(created);
    }

}
