package io.github.ffvasconcelos.jwallet.wallet.domain;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "wallets",
        check = @CheckConstraint(
                name = "ck_wallets_balance_non_negative",
                constraint = "balance >= 0"
        )
)
public class Wallet {

    @Id
    private UUID id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Wallet() {
    }

    public Wallet(UUID id, BigDecimal balance) {
        this.id = Objects.requireNonNull(id, "Wallet id is required");
        this.balance = Objects.requireNonNull(balance, "Wallet balance is required");
    }

    public UUID id() {
        return id;
    }

    public BigDecimal balance() {
        return balance;
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    public void debit(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }
}
