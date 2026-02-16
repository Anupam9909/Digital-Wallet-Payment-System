package com.digitalwallet.wallet_service.dto;

import com.digitalwallet.wallet_service.entity.Wallet;
import lombok.Data;

@Data
public class WalletResponse {
    private Long userId;
    private String currency;
    private Double balance;
    private Double availableBalance;

    public static WalletResponse fromEntity(Wallet wallet) {
        WalletResponse res = new WalletResponse();
        res.setUserId(wallet.getUserId());
        res.setCurrency(wallet.getCurrency());
        res.setBalance(wallet.getBalance());
        res.setAvailableBalance(wallet.getAvailableBalance());
        return res;
    }
}
