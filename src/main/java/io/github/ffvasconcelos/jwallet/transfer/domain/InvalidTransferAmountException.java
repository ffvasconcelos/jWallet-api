package io.github.ffvasconcelos.jwallet.transfer.domain;

public class InvalidTransferAmountException extends RuntimeException {

    public InvalidTransferAmountException() {
        super("Transfer amount must be positive and have no more than two decimal places");
    }
}
