package io.github.ffvasconcelos.jwallet.transfer.api;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransferRequest(
        @NotNull UUID sourceWalletId,
        @NotNull UUID targetWalletId,
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal amount
) {}
