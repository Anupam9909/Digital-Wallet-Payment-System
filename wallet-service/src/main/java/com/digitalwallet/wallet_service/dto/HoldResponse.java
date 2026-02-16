package com.digitalwallet.wallet_service.dto;

import com.digitalwallet.wallet_service.entity.WalletHold;
import lombok.Data;

@Data
public class HoldResponse {
    private String holdReference;
    private Long amount;
    private String status;
    private String walletCurrency;
    private Long walletId;

    public static HoldResponse fromEntity(WalletHold hold) {
        HoldResponse res = new HoldResponse();
        res.setHoldReference(hold.getHoldReference());
        res.setAmount(hold.getAmount());
        res.setStatus(hold.getStatus());
        res.setWalletId(hold.getWallet().getId());
        res.setWalletCurrency(hold.getWallet().getCurrency());
        return res;
    }
}
