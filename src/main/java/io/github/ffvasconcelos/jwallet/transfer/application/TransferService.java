package io.github.ffvasconcelos.jwallet.transfer.application;

import io.github.ffvasconcelos.jwallet.transfer.domain.*;
import io.github.ffvasconcelos.jwallet.wallet.domain.Wallet;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletNotFoundException;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class TransferService {

    private final WalletRepository walletRepository;
    private final TransferRepository transferRepository;

    public TransferService(WalletRepository walletRepository,  TransferRepository transferRepository) {
        this.walletRepository = walletRepository;
        this.transferRepository = transferRepository;
    }

    @Transactional
    public Transfer transfer(TransferCommand command) {
        validate(command);

        Wallet sourceWallet = findWallet(command.sourceWalletId());
        Wallet targetWallet = findWallet(command.targetWalletId());

        if (!sourceWallet.hasSufficientBalance(command.amount())) {
            throw new InsufficientBalanceException();
        }

        sourceWallet.debit(command.amount());
        targetWallet.credit(command.amount());

        Transfer transfer = new Transfer(
                UUID.randomUUID(),
                sourceWallet,
                targetWallet,
                command.amount(),
                TransferStatus.COMPLETED
        );

        return transferRepository.save(transfer);
    }

    public Transfer findTransfer(UUID transferId) {
        return transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(transferId));
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
