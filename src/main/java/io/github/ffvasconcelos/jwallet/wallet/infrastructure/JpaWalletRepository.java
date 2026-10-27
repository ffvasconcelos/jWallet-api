package io.github.ffvasconcelos.jwallet.wallet.infrastructure;

import io.github.ffvasconcelos.jwallet.wallet.domain.Wallet;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaWalletRepository implements WalletRepository {
    private final SpringDataWalletRepository repository;

    public JpaWalletRepository(SpringDataWalletRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Wallet> findById(UUID walletId) {
        return repository.findById(walletId);
    }

    @Override
    public Wallet save(Wallet wallet) {
        return repository.save(wallet);
    }

    @Override
    public Iterable<Wallet> findAll() {
        return repository.findAll();
    }
}
