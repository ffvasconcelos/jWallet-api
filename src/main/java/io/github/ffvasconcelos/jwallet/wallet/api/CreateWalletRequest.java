package io.github.ffvasconcelos.jwallet.wallet.api;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateWalletRequest(
        @NotNull @PositiveOrZero @Digits(integer = 17, fraction = 2) BigDecimal balance
) {}
