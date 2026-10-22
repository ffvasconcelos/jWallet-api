package io.github.ffvasconcelos.jwallet.wallet.application;

import io.github.ffvasconcelos.jwallet.wallet.domain.Wallet;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletNotFoundException;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletRepository;
import io.github.ffvasconcelos.jwallet.wallet.infrastructure.InMemoryWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Service
public class WalletServiceTest {
    private final static UUID WALLET_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final static BigDecimal WALLET_AMOUNT = new BigDecimal("100.00");

    private WalletService walletService;
    private WalletRepository walletRepository;

    @BeforeEach
    public void setup() {
        walletRepository = new InMemoryWalletRepository();
        walletService = new WalletService(walletRepository);
    }

    @Test
    public void createWallet() {
        Wallet wallet = walletService.createWallet(WALLET_UUID, WALLET_AMOUNT);

        assertEquals(WALLET_UUID, wallet.id());
        assertEquals(WALLET_AMOUNT, wallet.balance());

        Wallet savedWallet = walletRepository.findById(WALLET_UUID).orElseThrow();
        assertEquals(WALLET_AMOUNT, savedWallet.balance());
        assertEquals(WALLET_UUID, savedWallet.id());
    }

    @Test
    public void findExistingWallet() {
        walletRepository.save(new Wallet(WALLET_UUID, WALLET_AMOUNT));
        Wallet wallet = walletService.getWalletById(WALLET_UUID);

        assertEquals(WALLET_UUID, wallet.id());
        assertEquals(WALLET_AMOUNT, wallet.balance());
    }

    @Test
    public void findNonExistingWallet() {
        assertThrows(WalletNotFoundException.class, () -> walletService.getWalletById(UUID.randomUUID()));
    }
}
