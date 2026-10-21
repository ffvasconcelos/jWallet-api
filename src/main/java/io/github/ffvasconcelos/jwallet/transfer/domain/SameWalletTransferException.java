package io.github.ffvasconcelos.jwallet.transfer.domain;

public class SameWalletTransferException extends RuntimeException {

    public SameWalletTransferException() {
        super("Source and target wallets must be different");
    }
}
