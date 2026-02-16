package com.digitalwallet.wallet_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

    /*
       TODO: WalletHold
       Active - money is on hold(locked) but not yet deducted.
       Captured - hold is completed into actual debit.
       Released - hold is cancel and money is free again.

       Active --> capture ( trans. success -> deducted money)
       Active --> Release (trans. fail/cancelled -> free/release money)
       this make sure you don't overspend or loose money if services fails.

     */

@Entity
@Table(name = "wallet_holds")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many holds can belong to one wallet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, unique = true)
    private String holdReference;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private String status = "ACTIVE"; // Default status

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime expiresAt; // money will be hold till this time

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public String getHoldReference() {
        return holdReference;
    }

    public void setHoldReference(String holdReference) {
        this.holdReference = holdReference;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
