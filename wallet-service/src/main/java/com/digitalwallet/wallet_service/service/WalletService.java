package com.digitalwallet.wallet_service.service;

import com.DigitalWalletPaymentService.user_service.dto.CreateWalletRequest;
import com.digitalwallet.wallet_service.dto.*;
import com.digitalwallet.wallet_service.entity.Transaction;
import com.digitalwallet.wallet_service.entity.Wallet;
import com.digitalwallet.wallet_service.entity.WalletHold;
import com.digitalwallet.wallet_service.exception.InsufficientFundsException;
import com.digitalwallet.wallet_service.repository.TransactionRepository;
import com.digitalwallet.wallet_service.repository.WalletHoldRepository;
import com.digitalwallet.wallet_service.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletHoldRepository walletHoldRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, WalletHoldRepository walletHoldRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.walletHoldRepository = walletHoldRepository;
        this.transactionRepository = transactionRepository;
    }

    // ------------------------- Create Wallet -------------------------
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        Wallet wallet = new Wallet(request.getUserId(), request.getCurrency());
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        log.info("✅ Wallet created for userId={}, currency={}", request.getUserId(), request.getCurrency());
        return WalletResponse.fromEntity(wallet);
    }

    // ------------------------- Credit -------------------------
    @Transactional
    public WalletResponse credit(CreditRequest request) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(wallet.getBalance() + request.getAmount());
        wallet.setAvailableBalance(wallet.getAvailableBalance() + request.getAmount());
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // record transaction
        Transaction txn = new Transaction();
        txn.setWalletId(wallet.getId());
        txn.setType("CREDIT");
        txn.setAmount(request.getAmount());
        txn.setCurrency(wallet.getCurrency());
        txn.setStatus("SUCCESS");
        txn.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(txn);


        log.info("💰 Credited {} {} to userId={}, newBalance={}",
                request.getAmount(), request.getCurrency(), request.getUserId(), wallet.getBalance());
        return WalletResponse.fromEntity(wallet);
    }

    // ------------------------- Debit -------------------------
    @Transactional
    public WalletResponse debit(DebitRequest request) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getAvailableBalance() < request.getAmount()) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        wallet.setBalance(wallet.getBalance() - request.getAmount());
        wallet.setAvailableBalance(wallet.getAvailableBalance() - request.getAmount());
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaction txn = new Transaction();
        txn.setWalletId(wallet.getId());
        txn.setType("DEBIT");
        txn.setAmount(request.getAmount());
        txn.setCurrency(wallet.getCurrency());
        txn.setStatus("SUCCESS");
        txn.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(txn);


        log.info("💸 Debited {} {} from userId={}, newBalance={}",
                request.getAmount(), request.getCurrency(), request.getUserId(), wallet.getBalance());
        return WalletResponse.fromEntity(wallet);
    }

    // ------------------------- Get Wallet -------------------------
    public WalletResponse getWallet(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return WalletResponse.fromEntity(wallet);
    }

    // ------------------------- Place Hold -------------------------
    @Transactional
    public HoldResponse placeHold(HoldRequest request) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getAvailableBalance() < request.getAmount()) {
            throw new InsufficientFundsException("Insufficient available balance");
        }

        // Deduct from available balance only
        wallet.setAvailableBalance(wallet.getAvailableBalance() - request.getAmount());
        wallet.setUpdatedAt(LocalDateTime.now());


        WalletHold hold = new WalletHold();
        hold.setWallet(wallet);
        hold.setAmount(request.getAmount().longValue());
        hold.setHoldReference("HOLD-"+System.currentTimeMillis());
        hold.setStatus("ACTIVE");
        hold.setCreatedAt(LocalDateTime.now());
        hold.setExpiresAt(LocalDateTime.now().plusSeconds(request.getExpirySeconds()));

        walletRepository.save(wallet);
        walletHoldRepository.save(hold);

        log.info("🔒 Placed hold={} amount={} userId={}", hold.getHoldReference(), request.getAmount(), request.getUserId());
        return HoldResponse.fromEntity(hold);
    }

    // ------------------------- Capture Hold -------------------------
    @Transactional
    public WalletResponse captureHold(CaptureRequest request) {
        WalletHold hold = walletHoldRepository.findByHoldReference(request.getHoldReference())
                .orElseThrow(() -> new RuntimeException("Hold not found"));

        Wallet wallet = hold.getWallet();

        if (!"ACTIVE".equals(hold.getStatus())) {
            throw new RuntimeException("Hold already processed");
        }

        wallet.setBalance(wallet.getBalance() - hold.getAmount());
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        hold.setStatus("CAPTURED");
        walletHoldRepository.save(hold);

        // log transaction
        Transaction txn = new Transaction();
        txn.setWalletId(wallet.getId());
        txn.setType("CAPTURE");
        txn.setAmount(hold.getAmount().doubleValue());
        txn.setCurrency(wallet.getCurrency());
        txn.setStatus("SUCCESS");
        txn.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(txn);


        log.info("✅ Captured hold={} userId={} amount={}",
                hold.getHoldReference(), wallet.getUserId(), hold.getAmount());
        return WalletResponse.fromEntity(wallet);
    }

    // ------------------------- Release Hold -------------------------
    @Transactional
    public HoldResponse releaseHold(String holdReference) {
        WalletHold hold = walletHoldRepository.findByHoldReference(holdReference)
                .orElseThrow(() -> new RuntimeException("Hold not found"));

        Wallet wallet = hold.getWallet();

        if (!"ACTIVE".equals(hold.getStatus())) {
            throw new RuntimeException("Hold already processed");
        }

        wallet.setAvailableBalance(wallet.getAvailableBalance() + hold.getAmount());
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        hold.setStatus("RELEASED");
        walletHoldRepository.save(hold);

        // record transaction
        Transaction txn = new Transaction();
        txn.setWalletId(wallet.getId());
        txn.setType("RELEASE");
        txn.setAmount(hold.getAmount().doubleValue());
        txn.setCurrency(wallet.getCurrency());
        txn.setStatus("SUCCESS");
        txn.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(txn);


        log.info("♻️ Released hold={} userId={} amount={}", hold.getHoldReference(), wallet.getUserId(), hold.getAmount());
        return HoldResponse.fromEntity(hold);
    }

    //---------------------------Get all transactions for a specific wallet-----------------

    public WalletTransactionResponse getWalletTransactions(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        List<Transaction> transactions = transactionRepository.findByWalletId(walletId);

        return new WalletTransactionResponse(
                wallet.getId(),
                wallet.getCurrency(),
                wallet.getAvailableBalance(),
                transactions
        );
    }

}
