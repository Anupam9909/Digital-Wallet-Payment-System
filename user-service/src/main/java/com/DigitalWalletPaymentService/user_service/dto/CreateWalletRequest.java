package com.DigitalWalletPaymentService.user_service.dto;

public class CreateWalletRequest {
    private Long UserId;
    private String currency;

    public Long getUserId() {
        return UserId;
    }

    public void setUserId(Long userId) {
        UserId = userId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
