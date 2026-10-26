package io.github.ffvasconcelos.jwallet.transfer.domain;

import io.github.ffvasconcelos.jwallet.wallet.domain.Wallet;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "transfers",
        check = {
                @CheckConstraint(name = "ck_transfers_amount_positive", constraint = "amount > 0"),
                @CheckConstraint(name = "ck_transfers_different_wallets",
                        constraint = "source_wallet_id <> target_wallet_id"),
                @CheckConstraint(name = "ck_transfers_status", constraint = "status IN ('COMPLETED')")
        },
        indexes = {
                @Index(name = "idx_transfers_source_history",
                        columnList = "source_wallet_id, created_at DESC, id DESC"),
                @Index(name = "idx_transfers_target_history",
                        columnList = "target_wallet_id, created_at DESC, id DESC")
        }
)
public class Transfer {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_wallet_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_transfers_source_wallet"))
    private Wallet sourceWallet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_wallet_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_transfers_target_wallet"))
    private Wallet targetWallet;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Transfer() {
    }

    public Transfer(UUID id, Wallet sourceWallet, Wallet targetWallet, BigDecimal amount,
                    TransferStatus status) {
        this.id = Objects.requireNonNull(id, "Transfer id is required");
        this.sourceWallet = Objects.requireNonNull(sourceWallet, "Source wallet is required");
        this.targetWallet = Objects.requireNonNull(targetWallet, "Target wallet is required");
        this.amount = Objects.requireNonNull(amount, "Transfer amount is required");
        this.status = Objects.requireNonNull(status, "Transfer status is required");
    }

    public UUID id() {
        return id;
    }

    public UUID sourceWalletId() {
        return sourceWallet.id();
    }

    public UUID targetWalletId() {
        return targetWallet.id();
    }

    public BigDecimal amount() {
        return amount;
    }

    public TransferStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
