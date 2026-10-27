package io.github.ffvasconcelos.jwallet.transfer.domain;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepository {
    Transfer save(Transfer transfer);

    Optional<Transfer> findById(UUID id);

    Iterable<Transfer> findAll();
}
