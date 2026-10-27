package io.github.ffvasconcelos.jwallet.transfer.infrastructure;

import io.github.ffvasconcelos.jwallet.transfer.domain.Transfer;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.UUID;
import java.util.Optional;

public interface SpringDataTransferRepository extends JpaRepository<Transfer, UUID> {
    @Override
    @EntityGraph(attributePaths = {"sourceWallet", "targetWallet"})
    Optional<Transfer> findById(@NonNull UUID id);
}
