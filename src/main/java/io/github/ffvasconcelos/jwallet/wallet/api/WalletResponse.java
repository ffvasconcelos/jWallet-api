package io.github.ffvasconcelos.jwallet.wallet.api;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        BigDecimal balance
) {
}
