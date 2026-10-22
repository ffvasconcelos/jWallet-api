package io.github.ffvasconcelos.jwallet.wallet.application;

import io.github.ffvasconcelos.jwallet.wallet.domain.Wallet;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletNotFoundException;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet getWalletById(UUID id) {
        return this.walletRepository.findById(id).orElseThrow(
                () -> new WalletNotFoundException(id)
        );
    }

    public Wallet createWallet(UUID id, BigDecimal amount) {
        Wallet wallet = new Wallet(id, amount);
        return this.walletRepository.save(wallet);
    }
}
