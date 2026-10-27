package io.github.ffvasconcelos.jwallet.wallet.infrastructure;

import io.github.ffvasconcelos.jwallet.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataWalletRepository extends JpaRepository<Wallet, UUID> {
}
