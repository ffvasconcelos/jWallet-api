package io.github.ffvasconcelos.jwallet.transfer.application;

import io.github.ffvasconcelos.jwallet.transfer.domain.Transfer;
import io.github.ffvasconcelos.jwallet.transfer.domain.TransferRepository;
import io.github.ffvasconcelos.jwallet.transfer.domain.TransferStatus;
import io.github.ffvasconcelos.jwallet.transfer.infrastructure.JpaTransferRepository;
import io.github.ffvasconcelos.jwallet.wallet.domain.Wallet;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// No test transaction: the service itself must commit or roll back its work.
@SpringBootTest
@Import(TransferPersistenceTest.PersistenceFailureConfiguration.class)
class TransferPersistenceTest {

    @Autowired private TransferService transferService;
    @Autowired private WalletRepository walletRepository;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private FailureInjectingTransferRepository transferRepository;

    private UUID sourceId;
    private UUID targetId;

    @BeforeEach
    void setUp() {
        sourceId = UUID.randomUUID();
        targetId = UUID.randomUUID();
        transferRepository.failAfterFlush = false;
        walletRepository.save(new Wallet(sourceId, new BigDecimal("100.00")));
        walletRepository.save(new Wallet(targetId, new BigDecimal("20.00")));
    }

    @AfterEach
    void cleanUp() {
        transferRepository.failAfterFlush = false;
        // Remove only rows belonging to this test; preserve demo and user data.
        jdbc.update("DELETE FROM transfers WHERE source_wallet_id IN (?, ?) OR target_wallet_id IN (?, ?)",
                sourceId, targetId, sourceId, targetId);
        jdbc.update("DELETE FROM wallets WHERE id IN (?, ?)", sourceId, targetId);
    }

    @Test
    void commitsBalancesAndTransferWithCreationTimestamp() {
        Transfer transfer = transferService.transfer(new TransferCommand(
                sourceId, targetId, new BigDecimal("25.50")
        ));

        assertNotNull(transfer.id());
        assertNotNull(transfer.createdAt());
        assertEquals(TransferStatus.COMPLETED, transfer.status());
        assertBalance(sourceId, "74.50");
        assertBalance(targetId, "45.50");
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM transfers WHERE id = ? AND source_wallet_id = ? "
                        + "AND target_wallet_id = ? AND amount = ? AND status = 'COMPLETED' "
                        + "AND created_at IS NOT NULL",
                Integer.class, transfer.id(), sourceId, targetId, new BigDecimal("25.50")));
        assertEquals(transfer.id(), transferService.findTransfer(transfer.id()).id());
    }

    @Test
    void rollsBackBalancesAndTransferWhenSavingFailsAfterFlush() {
        transferRepository.failAfterFlush = true;

        assertThrows(IllegalStateException.class, () -> transferService.transfer(
                new TransferCommand(sourceId, targetId, new BigDecimal("25.50"))
        ));

        assertBalance(sourceId, "100.00");
        assertBalance(targetId, "20.00");
        assertEquals(0, jdbc.queryForObject(
                "SELECT count(*) FROM transfers WHERE source_wallet_id = ?", Integer.class, sourceId));
    }

    private void assertBalance(UUID walletId, String expected) {
        BigDecimal actual = jdbc.queryForObject(
                "SELECT balance FROM wallets WHERE id = ?", BigDecimal.class, walletId);
        assertNotNull(actual);
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class PersistenceFailureConfiguration {
        @Bean
        @Primary
        FailureInjectingTransferRepository failureInjectingTransferRepository(
                JpaTransferRepository delegate, EntityManager entityManager) {
            return new FailureInjectingTransferRepository(delegate, entityManager);
        }
    }

    static class FailureInjectingTransferRepository implements TransferRepository {
        private final JpaTransferRepository delegate;
        private final EntityManager entityManager;
        private boolean failAfterFlush;

        FailureInjectingTransferRepository(JpaTransferRepository delegate, EntityManager entityManager) {
            this.delegate = delegate;
            this.entityManager = entityManager;
        }

        @Override
        public Transfer save(Transfer transfer) {
            Transfer saved = delegate.save(transfer);
            if (failAfterFlush) {
                // Force real SQL writes before failing, so this proves database rollback.
                entityManager.flush();
                throw new IllegalStateException("Simulated failure after transfer flush");
            }
            return saved;
        }

        @Override
        public Optional<Transfer> findById(UUID id) {
            return delegate.findById(id);
        }

        @Override
        public Iterable<Transfer> findAll() { return delegate.findAll(); }
    }
}
