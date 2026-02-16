package com.digitalwallet.wallet_service.scheduler;

import com.digitalwallet.wallet_service.entity.WalletHold;
import com.digitalwallet.wallet_service.repository.WalletHoldRepository;
import com.digitalwallet.wallet_service.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class HoldExpiryScheduler {

    private final WalletHoldRepository walletHoldRepository;
    private final WalletService walletService;

    public HoldExpiryScheduler(WalletHoldRepository walletHoldRepository, WalletService walletService) {
        this.walletHoldRepository = walletHoldRepository;
        this.walletService = walletService;
    }

    @Scheduled(fixedRate = 60000) // every 1 minute
    public void expireOldHolds() {
        log.info("🕒 HoldExpiryScheduler triggered at {}", LocalDateTime.now());

        // Step 1: Find all active holds that have passed their expiry time
        List<WalletHold> expiredHolds =
                walletHoldRepository.findByStatusAndExpiresAtBefore("ACTIVE", LocalDateTime.now());

        if (expiredHolds.isEmpty()) {
            log.debug("No expired holds found at {}", LocalDateTime.now());
            return;
        }

        // Step 2: Release each expired hold
        for (WalletHold hold : expiredHolds) {
            try {
                log.info("Releasing expired hold: ref={}, walletId={}, amount={}",
                        hold.getHoldReference(), hold.getWallet().getId(), hold.getAmount());

                walletService.releaseHold(hold.getHoldReference()); // release back to wallet balance

                hold.setStatus("RELEASED");
                walletHoldRepository.save(hold);

                log.info("✅ Hold {} released successfully", hold.getHoldReference());
            } catch (Exception e) {
                log.error("❌ Failed to release hold {}: {}", hold.getHoldReference(), e.getMessage(), e);
            }
        }

        log.info("🏁 HoldExpiryScheduler completed at {}", LocalDateTime.now());
    }
}