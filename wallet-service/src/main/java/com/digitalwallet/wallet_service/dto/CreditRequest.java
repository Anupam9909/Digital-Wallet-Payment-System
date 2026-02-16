package com.digitalwallet.wallet_service.dto;

import lombok.Data;

@Data
public class CreditRequest {
    private Long userId;
    private String currency;
    private Double amount;
}
