package com.DigitalWalletPaymentService.transaction_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(nullable = false)
    @Positive(message = "Amount must be Positive")
    private Double amount;

    @Column(name = "time_stamp", nullable = false)
    @JsonFormat(pattern = "dd MMMM yyyy, hh:mm a")
    private LocalDateTime timeStamp;


    @Column(nullable = false)
    private String status;

    public Transaction() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @PrePersist
    public void PrePersist(){
        if(timeStamp == null)  timeStamp = LocalDateTime.now();
        if(status == null) status = "PENDING";
    }

    @Override
    public String toString(){
        return "Transaction{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", senderName=" + senderName +
                ", receiverId=" + receiverId +
                ", receiverName=" + receiverName +
                ", amount=" + amount +
                ", timeStamp=" + timeStamp +
                ", status=" + status + '\'' +
                "}";

    }
}
