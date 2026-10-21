package io.github.ffvasconcelos.jwallet.transfer.application;

import io.github.ffvasconcelos.jwallet.transfer.domain.InsufficientBalanceException;
import io.github.ffvasconcelos.jwallet.transfer.domain.InvalidTransferAmountException;
import io.github.ffvasconcelos.jwallet.transfer.domain.SameWalletTransferException;
import io.github.ffvasconcelos.jwallet.transfer.domain.Transfer;
import io.github.ffvasconcelos.jwallet.transfer.domain.TransferStatus;
import io.github.ffvasconcelos.jwallet.wallet.domain.Wallet;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletNotFoundException;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class TransferService {

    private final WalletRepository walletRepository;

    public TransferService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Transfer transfer(TransferCommand command) {
        validate(command);

        Wallet sourceWallet = findWallet(command.sourceWalletId());
        Wallet targetWallet = findWallet(command.targetWalletId());

        if (!sourceWallet.hasSufficientBalance(command.amount())) {
            throw new InsufficientBalanceException();
        }

        sourceWallet.debit(command.amount());
        targetWallet.credit(command.amount());

        return new Transfer(
                UUID.randomUUID(),
                sourceWallet.id(),
                targetWallet.id(),
                command.amount(),
                TransferStatus.COMPLETED,
                Instant.now()
        );
    }

    private void validate(TransferCommand command) {
        Objects.requireNonNull(command, "Transfer command is required");

        if (command.sourceWalletId() == null || command.targetWalletId() == null) {
            throw new IllegalArgumentException("Source and target wallet ids are required");
        }
        if (command.sourceWalletId().equals(command.targetWalletId())) {
            throw new SameWalletTransferException();
        }
        if (command.amount() == null || command.amount().signum() <= 0 || command.amount().scale() > 2) {
            throw new InvalidTransferAmountException();
        }
    }

    private Wallet findWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }
}
