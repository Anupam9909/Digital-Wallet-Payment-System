package com.DigitalWalletPaymentService.transaction_service.controller;

import com.DigitalWalletPaymentService.transaction_service.entity.Transaction;
import com.DigitalWalletPaymentService.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody Transaction transaction){

        Transaction createdTransaction = transactionService.createTransaction(transaction);
        return ResponseEntity.ok(createdTransaction);
    }

    @GetMapping("/all")
    public List<Transaction> getAll(){
        return transactionService.getAllTransaction();
    }

}
