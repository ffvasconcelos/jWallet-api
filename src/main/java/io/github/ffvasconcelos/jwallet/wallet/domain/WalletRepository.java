package io.github.ffvasconcelos.jwallet.wallet.domain;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {

    Optional<Wallet> findById(UUID walletId);

    Wallet save(Wallet wallet);
}
