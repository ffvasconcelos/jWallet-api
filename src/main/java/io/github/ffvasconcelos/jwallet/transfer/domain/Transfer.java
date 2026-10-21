package io.github.ffvasconcelos.jwallet.transfer.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transfer(
        UUID id,
        UUID sourceWalletId,
        UUID targetWalletId,
        BigDecimal amount,
        TransferStatus status,
        Instant createdAt
) {
}
