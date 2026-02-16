package com.digitalwallet.wallet_service.controller;

import com.DigitalWalletPaymentService.user_service.dto.CreateWalletRequest;
import com.digitalwallet.wallet_service.dto.*;
import com.digitalwallet.wallet_service.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // ------------------------- Create Wallet -------------------------
    @PostMapping("/create")
    public ResponseEntity<WalletResponse> createWallet(@RequestBody CreateWalletRequest request) {
        log.info("🪙 Creating wallet for userId={}", request.getUserId());
        return ResponseEntity.ok(walletService.createWallet(request));
    }

    // ------------------------- Credit -------------------------
    @PostMapping("/credit")
    public ResponseEntity<WalletResponse> credit(@RequestBody CreditRequest request) {
        log.info("💰 Crediting wallet for userId={} amount={}", request.getUserId(), request.getAmount());
        return ResponseEntity.ok(walletService.credit(request));
    }

    // ------------------------- Debit -------------------------
    @PostMapping("/debit")
    public ResponseEntity<WalletResponse> debit(@RequestBody DebitRequest request) {
        log.info("💸 Debiting wallet for userId={} amount={}", request.getUserId(), request.getAmount());
        return ResponseEntity.ok(walletService.debit(request));
    }

    // ------------------------- Get Wallet -------------------------
    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable Long userId) {
        log.info("📄 Fetching wallet details for userId={}", userId);
        return ResponseEntity.ok(walletService.getWallet(userId));
    }

    // ------------------------- Place Hold -------------------------
    @PostMapping("/hold")
    public ResponseEntity<HoldResponse> placeHold(@RequestBody HoldRequest request) {
        log.info("🔒 Placing hold on wallet for userId={} amount={}", request.getUserId(), request.getAmount());
        return ResponseEntity.ok(walletService.placeHold(request));
    }

    // ------------------------- Capture Hold -------------------------
    @PostMapping("/capture")
    public ResponseEntity<WalletResponse> captureHold(@RequestBody CaptureRequest request) {
        log.info("✅ Capturing hold for holdReference={}", request.getHoldReference());
        return ResponseEntity.ok(walletService.captureHold(request));
    }

    // ------------------------- Release Hold -------------------------
    @PostMapping("/release/{holdReference}")
    public ResponseEntity<HoldResponse> releaseHold(@PathVariable String holdReference) {
        log.info("♻️ Releasing hold with reference={}", holdReference);
        return ResponseEntity.ok(walletService.releaseHold(holdReference));
    }

    // ------------------------- Get Wallet Transactions -------------------------
    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<WalletTransactionResponse> getWalletTransactions(@PathVariable Long walletId) {
        log.info("📊 Fetching transactions for walletId={}", walletId);
        return ResponseEntity.ok(walletService.getWalletTransactions(walletId));
    }
}
