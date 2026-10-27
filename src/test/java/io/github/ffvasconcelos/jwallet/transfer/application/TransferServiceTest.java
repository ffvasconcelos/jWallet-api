package io.github.ffvasconcelos.jwallet.transfer.application;

import io.github.ffvasconcelos.jwallet.transfer.domain.*;
import io.github.ffvasconcelos.jwallet.wallet.domain.Wallet;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletNotFoundException;
import io.github.ffvasconcelos.jwallet.wallet.infrastructure.InMemoryWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

class TransferServiceTest {

    private static final UUID SOURCE_WALLET_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TARGET_WALLET_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private InMemoryWalletRepository walletRepository;
    private InMemoryTransferRepository transferRepository;
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        walletRepository = new InMemoryWalletRepository();
        walletRepository.save(new Wallet(SOURCE_WALLET_ID, new BigDecimal("100.00")));
        walletRepository.save(new Wallet(TARGET_WALLET_ID, new BigDecimal("20.00")));
        transferRepository = new InMemoryTransferRepository();
        transferService = new TransferService(walletRepository, transferRepository);
    }

    @Test
    void transfersAmountBetweenDifferentWalletsWhenSourceHasEnoughBalance() {
        Transfer transfer = transferService.transfer(new TransferCommand(
                SOURCE_WALLET_ID,
                TARGET_WALLET_ID,
                new BigDecimal("25.50")
        ));

        assertSame(transfer, transferRepository.findById(transfer.id()).orElseThrow());
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
        assertEquals(0, transferRepository.transfers.size());

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
        assertEquals(0, transferRepository.transfers.size());
    }

    @Test
    void rejectsTransferToTheSameWallet() {
        TransferCommand command = new TransferCommand(
                SOURCE_WALLET_ID,
                SOURCE_WALLET_ID,
                new BigDecimal("10.00")
        );

        assertThrows(SameWalletTransferException.class, () -> transferService.transfer(command));
        assertEquals(0, transferRepository.transfers.size());
    }

    @Test
    void findsExistingTransfer() {
        Transfer transfer = transferService.transfer(new TransferCommand(
                SOURCE_WALLET_ID, TARGET_WALLET_ID, new BigDecimal("10.00")
        ));

        assertSame(transfer, transferService.findTransfer(transfer.id()));
    }

    @Test
    void rejectsMissingTransfer() {
        assertThrows(TransferNotFoundException.class,
                () -> transferService.findTransfer(UUID.randomUUID()));
    }

    @Test
    void rejectsNegativeAmountWithoutSavingTransferOrChangingBalances() {
        assertThrows(InvalidTransferAmountException.class,
                () -> transferService.transfer(new TransferCommand(
                        SOURCE_WALLET_ID, TARGET_WALLET_ID, new BigDecimal("-1.00")
                )));

        assertEquals(0, transferRepository.transfers.size());
        assertEquals(new BigDecimal("100.00"), walletRepository.getRequired(SOURCE_WALLET_ID).balance());
        assertEquals(new BigDecimal("20.00"), walletRepository.getRequired(TARGET_WALLET_ID).balance());
    }

    @Test
    void rejectsMissingWalletWithoutSavingTransferOrChangingSourceBalance() {
        assertThrows(WalletNotFoundException.class,
                () -> transferService.transfer(new TransferCommand(
                        SOURCE_WALLET_ID, UUID.randomUUID(), new BigDecimal("10.00")
                )));

        assertEquals(0, transferRepository.transfers.size());
        assertEquals(new BigDecimal("100.00"), walletRepository.getRequired(SOURCE_WALLET_ID).balance());
    }

    private static class InMemoryTransferRepository implements TransferRepository {
        private final Map<UUID, Transfer> transfers = new HashMap<>();

        @Override
        public Transfer save(Transfer transfer) {
            transfers.put(transfer.id(), transfer);
            return transfer;
        }

        @Override
        public Optional<Transfer> findById(UUID id) {
            return Optional.ofNullable(transfers.get(id));
        }

        @Override
        public Iterable<Transfer> findAll() {
            return transfers.values();
        }
    }

}
