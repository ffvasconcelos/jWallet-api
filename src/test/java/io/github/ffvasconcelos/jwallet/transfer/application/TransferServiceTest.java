package io.github.ffvasconcelos.jwallet.transfer.application;

import io.github.ffvasconcelos.jwallet.transfer.domain.InsufficientBalanceException;
import io.github.ffvasconcelos.jwallet.transfer.domain.InvalidTransferAmountException;
import io.github.ffvasconcelos.jwallet.transfer.domain.SameWalletTransferException;
import io.github.ffvasconcelos.jwallet.transfer.domain.Transfer;
import io.github.ffvasconcelos.jwallet.transfer.domain.TransferStatus;
import io.github.ffvasconcelos.jwallet.wallet.domain.Wallet;
import io.github.ffvasconcelos.jwallet.wallet.infrastructure.InMemoryWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferServiceTest {

    private static final UUID SOURCE_WALLET_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TARGET_WALLET_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private InMemoryWalletRepository walletRepository;
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        walletRepository = new InMemoryWalletRepository();
        walletRepository.save(new Wallet(SOURCE_WALLET_ID, new BigDecimal("100.00")));
        walletRepository.save(new Wallet(TARGET_WALLET_ID, new BigDecimal("20.00")));
        transferService = new TransferService(walletRepository);
    }

    @Test
    void transfersAmountBetweenDifferentWalletsWhenSourceHasEnoughBalance() {
        Transfer transfer = transferService.transfer(new TransferCommand(
                SOURCE_WALLET_ID,
                TARGET_WALLET_ID,
                new BigDecimal("25.50")
        ));

        assertNotNull(transfer.id());
        assertEquals(TransferStatus.COMPLETED, transfer.status());
        // CreationTimestamp is populated by Hibernate, which is absent in this unit test.
        assertNull(transfer.createdAt());
        assertEquals(SOURCE_WALLET_ID, transfer.sourceWalletId());
        assertEquals(TARGET_WALLET_ID, transfer.targetWalletId());
        assertEquals(new BigDecimal("25.50"), transfer.amount());
        assertEquals(new BigDecimal("74.50"), walletRepository.getRequired(SOURCE_WALLET_ID).balance());
        assertEquals(new BigDecimal("45.50"), walletRepository.getRequired(TARGET_WALLET_ID).balance());
    }

    @Test
    void rejectsTransferWhenSourceBalanceIsInsufficientWithoutChangingBalances() {
        TransferCommand command = new TransferCommand(
                SOURCE_WALLET_ID,
                TARGET_WALLET_ID,
                new BigDecimal("100.01")
        );

        assertThrows(InsufficientBalanceException.class, () -> transferService.transfer(command));

        assertEquals(new BigDecimal("100.00"), walletRepository.getRequired(SOURCE_WALLET_ID).balance());
        assertEquals(new BigDecimal("20.00"), walletRepository.getRequired(TARGET_WALLET_ID).balance());
    }

    @Test
    void rejectsZeroOrOverPrecisionAmounts() {
        TransferCommand zeroAmount = new TransferCommand(
                SOURCE_WALLET_ID,
                TARGET_WALLET_ID,
                BigDecimal.ZERO
        );
        TransferCommand overPrecisionAmount = new TransferCommand(
                SOURCE_WALLET_ID,
                TARGET_WALLET_ID,
                new BigDecimal("10.001")
        );

        assertThrows(InvalidTransferAmountException.class, () -> transferService.transfer(zeroAmount));
        assertThrows(InvalidTransferAmountException.class, () -> transferService.transfer(overPrecisionAmount));
    }

    @Test
    void rejectsTransferToTheSameWallet() {
        TransferCommand command = new TransferCommand(
                SOURCE_WALLET_ID,
                SOURCE_WALLET_ID,
                new BigDecimal("10.00")
        );

        assertThrows(SameWalletTransferException.class, () -> transferService.transfer(command));
    }
}
