package io.github.ffvasconcelos.jwallet.transfer.api;

import io.github.ffvasconcelos.jwallet.transfer.domain.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        UUID sourceWalletId,
        UUID targetWalletId,
        BigDecimal amount,
        TransferStatus status,
        Instant createdAt
) {}
