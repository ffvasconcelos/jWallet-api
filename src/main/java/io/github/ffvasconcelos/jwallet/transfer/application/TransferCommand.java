package io.github.ffvasconcelos.jwallet.transfer.application;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferCommand(UUID sourceWalletId, UUID targetWalletId, BigDecimal amount) {
}
