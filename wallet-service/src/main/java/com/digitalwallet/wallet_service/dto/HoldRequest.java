package com.digitalwallet.wallet_service.dto;

import lombok.Data;

@Data
public class HoldRequest {
    private Long userId;
    private String currency;
    private Double amount;
    private long expirySeconds;
}
