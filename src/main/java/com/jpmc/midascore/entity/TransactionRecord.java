package com.jpmc.midascore.entity;

import jakarta.persistence.*;

@Entity
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UserRecord sender;

    @ManyToOne(optional = false)
    private UserRecord recipient;

    @Column(nullable = false)
    private float amount;

    @Column(nullable = false)
    private float incentive;

    protected TransactionRecord() {}

    public TransactionRecord(UserRecord sender, UserRecord recipient, float amount, float incentive) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.incentive = incentive;
    }

    // Getters
    public Long getId() { return id; }

    public UserRecord getSender() { return sender; }

    public UserRecord getRecipient() { return recipient; }

    public float getAmount() { return amount; }

    public float getIncentive() { return incentive; }

    // Optional setters if needed
    public void setSender(UserRecord sender) { this.sender = sender; }

    public void setRecipient(UserRecord recipient) { this.recipient = recipient; }

    public void setAmount(float amount) { this.amount = amount; }

    public void setIncentive(float incentive) { this.incentive = incentive; }

    @Override
    public String toString() {
        return String.format(
                "TransactionRecord[id=%d, sender=%s, recipient=%s, amount=%.2f, incentive=%.2f]",
                id,
                sender != null ? sender.getName() : "null",
                recipient != null ? recipient.getName() : "null",
                amount,
                incentive
        );
    }
}
