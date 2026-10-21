package io.github.ffvasconcelos.jwallet.wallet.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class Wallet {

    private final UUID id;
    private BigDecimal balance;

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
