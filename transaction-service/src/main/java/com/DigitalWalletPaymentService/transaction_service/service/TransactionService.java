package com.DigitalWalletPaymentService.transaction_service.service;

import com.DigitalWalletPaymentService.transaction_service.entity.Transaction;

import java.util.List;

public interface TransactionService {

    Transaction createTransaction(Transaction transaction);
    List<Transaction> getAllTransaction();

}
