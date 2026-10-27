package io.github.ffvasconcelos.jwallet.wallet.infrastructure;

import io.github.ffvasconcelos.jwallet.wallet.domain.Wallet;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletNotFoundException;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletRepository;
//import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//@Repository
public class InMemoryWalletRepository implements WalletRepository {

    private final Map<UUID, Wallet> wallets = new ConcurrentHashMap<>();

    @Override
    public Optional<Wallet> findById(UUID walletId) {
        return Optional.ofNullable(wallets.get(walletId));
    }

    @Override
    public Wallet save(Wallet wallet) {
        wallets.put(wallet.id(), wallet);
        return wallet;
    }

    public Wallet getRequired(UUID walletId) {
        return findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    public Iterable<Wallet>  findAll() {
        return wallets.values();
    }
}
