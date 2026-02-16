package com.digitalwallet.wallet_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Transaction entity used within WalletService for
 * logging all credit, debit, hold capture and release operations.
 */
@Entity
@Table(name = "wallet_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The wallet that performed this transaction
    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @NotNull
    @Column(nullable = false)
    private String type; // CREDIT, DEBIT, CAPTURE, RELEASE

    @Positive(message = "Amount must be positive")
    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String currency = "INR";

    @JsonFormat(pattern = "dd MMM yyyy, hh:mm a")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String status; // SUCCESS, FAILED, PENDING

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "SUCCESS";
        }
    }
}
