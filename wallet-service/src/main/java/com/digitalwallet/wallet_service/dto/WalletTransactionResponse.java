package com.digitalwallet.wallet_service.dto;

import com.digitalwallet.wallet_service.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionResponse {
    private Long walletId;
    private String currency;
    private Double availableBalance;
    private List<Transaction> transactions;
}
