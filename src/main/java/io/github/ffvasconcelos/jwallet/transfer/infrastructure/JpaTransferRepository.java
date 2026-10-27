package io.github.ffvasconcelos.jwallet.transfer.infrastructure;

import io.github.ffvasconcelos.jwallet.transfer.domain.Transfer;
import io.github.ffvasconcelos.jwallet.transfer.domain.TransferRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaTransferRepository implements TransferRepository {

    private final SpringDataTransferRepository repository;

    public JpaTransferRepository(SpringDataTransferRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Transfer> findById(UUID transferId) {
        return repository.findById(transferId);
    }

    @Override
    public Transfer save(Transfer transfer) {
        return repository.save(transfer);
    }

    @Override
    public Iterable<Transfer> findAll() {
        return repository.findAll();
    }
}
